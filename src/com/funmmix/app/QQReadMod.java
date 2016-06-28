package com.funmmix.app;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import javax.imageio.ImageIO;

import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.log4j.Logger;
import com.funmix.common.Log4jLogger;
import com.funmix.exception.TimeoutException;
import com.funmix.model.ADBWork;
import com.funmix.model.Device;
import com.funmix.model.Phone;
import com.funmix.utils.ADBUtils;
import com.funmix.utils.ImageUtils;
import com.funmix.utils.ImgCompareUtils;
import com.funmix.utils.Utils;
import com.funmix.utils.VCodeUtils;

public class QQReadMod extends WorkMod {

	protected static Logger	log		= Log4jLogger.getLogger(QQReadMod.class);	
	protected ImgCompareUtils		imgComp;
	private Device dev;
	private String account;
	private String password;
	private String ip;
	
	public QQReadMod(ADBWork work, ADBUtils adb) {
		this.work = work;
		this.adb = adb;	
		this.dev = adb.getDevice();
		try {
			imgComp = new ImgCompareUtils(adb.getDevice().getDevice());
			log.info("imgCompare init end!");
			vcodeTmp = adb.getWorkDir() + "//vcode.png";
			cmd = adb.getAdbPath() + " -s " + dev.getDevice() + " logcat -s mqq:d ActivityManager:i MTKTool:d";
			String tmps[] = work.getExtra().split("#");
			account = tmps[0];
			password = tmps[1];
			ip = tmps[2];
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	public void run() throws TimeoutException, SQLException {		
		//startMTKTools(getPhoneData(work.getAccount()));		
		adb.waitDeviceOn();
		adb.clearLogcat();
		adblog = new ADBLogger(adb.getQueue(), cmd);
		adblog.start();
		adb.waitDeviceReady(5);			
				
		//adb.execADB(" shell rm /storage/sdcard0/tencent/mta/.mid.txt");
		//adb.execADB(" shell sqlite3 /data/data/com.android.providers.settings/databases/settings.db \"delete from system where name like '%MTA%';\"");
		clearData();
		//wait 
		startQQRead();
		QQread(work);
		
		stopQQRead();		
	}
	
	private Phone getPhoneData(String account) throws SQLException{
		return adb.getQrun().query(adb.getConn(), "select * from tqqread where id=" + account, new BeanHandler<Phone>(Phone.class));
	}
	
	private void QQread(ADBWork work) throws TimeoutException {		
		log.info("start sim guide");
		simGuide();
		log.info("sim guide end");
		openQQLogin();
		if(loginQQ(work)==1){
			simReader();
			saveReadLog(work);
		}
	}
	
	private void openQQLogin() throws TimeoutException{
		adb.waitKey("精选@");
		log.info("openQQLogin 1");
		int[] ctrlpos;
		ctrlpos = getControl("登录后，向你推荐更多优质内容@[");
		log.info(ctrlpos[0] + "," + ctrlpos[1]);
		while(getKey("Displayed com.qq.reader/.activity.NewLoginActivity") == - 1){
			adb.tap(ctrlpos[0]+5, ctrlpos[1]+10);
			sleep(1000);
		}
		log.info("openQQLogin 2");
		ctrlpos = getControl("QQ登录@[");
		log.info(ctrlpos[0] + "," + ctrlpos[1]);
		while(getKey("Displayed com.tencent.mobileqq/com.tencent.qqconnect.wtlogin.Login") == - 1){
			adb.tap(ctrlpos[0]+5, ctrlpos[1]+5);
			sleep(2000);
		}
		log.info("openQQLogin 3");
	}
	
	private int[] getControl(String key) throws TimeoutException{
		String ctrl = null;
		int[] ctrlpos = {0,0};
		int k= 0;
		while( (ctrl = Utils.getKeyValue(key, adb.getQueue()))== null ){
			sleep(1000);
			if(k++>60)
				throw new TimeoutException("get " + key + "Failed!");
		}		
		if(ctrl!=null){
			String fbtn = ctrl.substring(ctrl.indexOf("[")+1, ctrl.indexOf("]"));
			ctrlpos[0]= Integer.parseInt(fbtn.substring(0,fbtn.indexOf(",")));
			ctrlpos[1]= Integer.parseInt(fbtn.substring(fbtn.indexOf(",")+1,fbtn.length()));
		}
		return ctrlpos;
	}
	
	private void simGuide() throws TimeoutException{
		adb.waitKey("[AppLaunch] Displayed Displayed com.qq.reader/.activity.GuideActivity");
		for(int i=0;i<4;i++){
			sleep(500);
			adb.swipe(Utils.getRandom(480, 520), Utils.getRandom(440, 520), Utils.getRandom(20, 60), Utils.getRandom(440, 520));
		}
		sleep(1000);
		if(Utils.getRandom(0, 1)==0)
			adb.tap(135, 400);
		else
			adb.tap(540-135, 400);
	}
	
	private void simReader(){
		adb.tap(100, Utils.getRandom(400, 800));
		sleep(3000);
		adb.tap(270, 900);
		sleep(2000);
		adb.tap(270, 900);
		sleep(2000);
		for(int i=0;i<15;i++){
			sleep(2000);
			//adb.swipe(Utils.getRandom(480, 520), Utils.getRandom(440, 520), Utils.getRandom(20, 60), Utils.getRandom(440, 520));
			adb.tap(440,180);
		}
	}

	private void clearData() {
		adb.clearAppData("com.qq.reader");
		adb.clearAppData("com.tencent.mobileqq");
		sleep(2000);
	}

	private void startQQRead() throws TimeoutException {
		adb.startActivity("com.qq.reader/.activity.SplashActivity", "Start proc com.qq.reader for activity com.qq.reader/.activity.SplashActivity:");
		log.info("start QQReader success");
	}

	private void stopQQRead() {
		adb.stopActivity("com.qq.reader");
	}	
	
	private int loginQQ(ADBWork work) {
		int qqstatus = 0;
		try {
			//tap("qq_input");// input account
			adb.inputText(account);
			adb.tapKey("KEYCODE_TAB");
			adb.inputText(password);
			adb.tapKey("KEYCODE_ENTER");
			int[] btn = getControl("Button/登 录@[");
			tap(270, btn[1]+20);// confirm
			int status = -1;
			int k =0;
			String keys[] = {"输入验证码@","com.qq.reader/android.widget.FrameLayout/ @["};
			while ( (status=Utils.getKey(keys, adb.getQueue())) == -1){
				Utils.sleep(1000);
				if(k++>30)
					throw new TimeoutException("Submit account Failed!");
			}
			if(status == 0){				//vcode
				enterVCode(0);				
			}else{// 1 2
				throw new TimeoutException("loginsucc");
			}
		} catch (Exception e) {
			log.error(e);
			if (e.getMessage() != null && e.getMessage().equals("loginsucc")) {
				qqstatus = 1;
			} else if (e.getMessage() != null && e.getMessage().equals("loginfailed")) {
				qqstatus = -1;
			} else {
				qqstatus = 0;
			}
		}
		saveAccountLog(work, qqstatus);
		//stopQQ();
		return qqstatus;
	}

	private int enterVCode(int count) throws TimeoutException {
		if(count++>3){
			throw new TimeoutException("getVCodeFailed!");
		}
		//输入验证码@
		String fbtn = Utils.getKeyValue("完成@[", adb.getQueue());
		fbtn = fbtn.substring(fbtn.indexOf("[")+1, fbtn.indexOf("]"));
		int fx = Integer.parseInt(fbtn.substring(0,fbtn.indexOf(",")));
		int fy = Integer.parseInt(fbtn.substring(fbtn.indexOf(",")+1,fbtn.length()));
		
		String vcodeimg = Utils.getKeyValue("android.widget.ImageView", adb.getQueue());
		vcodeimg = vcodeimg.substring(vcodeimg.indexOf("@[")+2, vcodeimg.indexOf("]"));
		int vx = Integer.parseInt(vcodeimg.substring(0,vcodeimg.indexOf(",")));
		int vy = Integer.parseInt(vcodeimg.substring(vcodeimg.indexOf(",")+1,vcodeimg.length()));
		
		String ref = Utils.getKeyValue("看不清？换一张@", adb.getQueue());
		ref = ref.substring(ref.indexOf("[")+1, ref.indexOf("]"));
		int rx = Integer.parseInt(ref.substring(0,ref.indexOf(",")));
		int ry = Integer.parseInt(ref.substring(ref.indexOf(",")+1,ref.length()));
		
		scbuff = adb.getScreenShot();
		BufferedImage imgTmp = ImageUtils.SplitImage(scbuff, vx, vy, 194, 81);
		try {
			ImageIO.write(imgTmp, "png", new File(vcodeTmp));
		} catch (IOException e) {
			log.error(e);
		}
		if (imgComp.compare("imgVCode", imgTmp)) {
			log.info("vcode not change,ref code!");
			tap(rx+10,ry+2);
			sleep(3000);
			enterVCode(count);
		}
		imgComp.setImgVCode(ImageUtils.getImage(vcodeTmp));
		log.info("get vcode by YunSu");
		String result = VCodeUtils.getVCode(vcodeTmp);
		if (result == null) {
			throw new TimeoutException("getVCodeFailed!");
		}		
		sleep(1000);
		adb.inputText(result);
		adb.tapKey("KEYCODE_ENTER");
		sleep(200);
		tap(fx+5,fy+5);
		sleep(2000);
		int status = -1,k=0;
		String keys[] = {"搜索@","com.qq.reader/android.widget.FrameLayout/ @[","帐号无法登录@"};
		while ( (status=Utils.getKey(keys, adb.getQueue())) == -1){
			Utils.sleep(1000);
			if(k++>30)
				throw new TimeoutException("Submit account Failed!");
		}
		if(status == 2){				//vcode
			//enterVCode(count);		
			throw new TimeoutException("loginfailed");
		}else{// 1 2
			throw new TimeoutException("loginsucc");
		}
		//return count;
	}

/*	private void tap(String obj) {
		log.info("tap:" + obj);
		if (obj.equals("qq")) {// 与qq好友玩
			tap(158, 851);
		} else if (obj.equals("qq_input")) {// 切换账号/清除账号输入
			tap(77, 345);
		} else if (obj.equals("qq_code")) {// 验证码
			tap(926, 190);
		} else if (obj.equals("qq_code_ref")) {// 刷新验证码
			tap(283, 186);
		} else if (obj.equals("qq_code_sub")) {// 验证码确认
			tap(500, 76);
		} else {
			log.error("Error:tap obj not exist!!");
		}
	}*/
	
	private void saveReadLog(ADBWork work) {
		try {
			if(work.getKeyid().equals("0") || work.getKeyid().equals("-1") )
				return;			
			String updateSQL = "update tqqread set status=1,lastupdate=now(),_1st=now() where account='" + account + "'";
			log.info(updateSQL);
			adb.getQrun().update(adb.getConn(), updateSQL);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void saveAccountLog(ADBWork work, int status) {
		try {
			if(work.getKeyid().equals("0") || work.getKeyid().equals("-1") )
				return;
			String updateSQL = "update tqqaccounta set status=" + status + ",lastip='" +ip + "',lastlogin=now() where account='" + account + "'";
			log.info(updateSQL);
			adb.getQrun().update(adb.getConn(), updateSQL);			
			updateSQL = "update tqqread set status=" + status + ",lastupdate=now() where account='" + account + "'";
			log.info(updateSQL);
			adb.getQrun().update(adb.getConn(), updateSQL);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
