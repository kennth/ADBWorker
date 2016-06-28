package com.funmmix.app;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import org.apache.log4j.Logger;
import com.funmix.common.Log4jLogger;
import com.funmix.exception.TimeoutException;
import com.funmix.model.ADBWork;
import com.funmix.model.Device;
import com.funmix.utils.ADBUtils;
import com.funmix.utils.ImageUtils;
import com.funmix.utils.ImgCompareUtils;
import com.funmix.utils.Utils;
import com.funmix.utils.VCodeUtils;

public class QQLoginMod extends WorkMod {

	protected static Logger	log		= Log4jLogger.getLogger(QQLoginMod.class);	
	protected ImgCompareUtils		imgComp;
	private Device dev;
	private String account;
	private String password;
	private String ip;
	
	public QQLoginMod(ADBWork work, ADBUtils adb) {
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

	public void run() throws TimeoutException {
		updateWorkStatus(2);
		adb.clearLogcat();
		adblog = new ADBLogger(adb.getQueue(), cmd);
		adblog.start();
		adb.waitDeviceReady(5);		
		loginQQ(work);
		startMTKTools();		
		updateWorkStatus(1);						
		adblog.setStop(true);
	}

	private void clearData() {
		adb.clearAppData("com.tencent.mobileqq");
		sleep(2000);
	}

	private void startQQ() throws TimeoutException {
		adb.startActivity("com.tencent.mobileqq/.activity.SplashActivity", "Start proc com.tencent.mobileqq for activity com.tencent.mobileqq");
		log.info("start mobileQQ success");
	}

	private void stopQQ() {
		adb.stopActivity("com.tencent.mobileqq");
	}

	private void loginQQ(ADBWork work) {
		int qqstatus = 0;
		try {
			clearData();
			startQQ();
			int k=0;
			sleep(3000);
			do {
				sleep(2000);
				tap("qq");// login				
				if(k++>10)
					throw new TimeoutException("Start qqLogin Failed!");
			} while (Utils.getKey("Displayed com.tencent.mobileqq/.activity.LoginActivity",adb.getQueue()) == -1);
			tap("qq_input");// input account
			adb.inputText(account);
			adb.tapKey("KEYCODE_TAB");
			adb.inputText(password);
			adb.tapKey("KEYCODE_ENTER");
			tap(282, 500);// confirm
			int status = -1;
			k =0;
			String keys[] = {"输入验证码@","搜索@","启用通讯录","帐号无法登录@"};
			while ( (status=Utils.getKey(keys, adb.getQueue())) == -1){
				Utils.sleep(1000);
				if(k++>30)
					throw new TimeoutException("Submit account Failed!");
			}
			if(status == 0){				//vcode
				enterVCode(0);				
			}else if(status == 3){
				throw new TimeoutException("loginfailed");
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
		stopQQ();
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
		String keys[] = {"搜索@","启用通讯录","帐号无法登录@"};
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

	private void tap(String obj) {
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
	}

	private void saveAccountLog(ADBWork work, int status) {
		try {
			if(work.getKeyid().equals("0") || work.getKeyid().equals("-1") )
				return;
			String updateSQL = "update tqqaccounta set status=" + status + ",lastip='" +ip + "',lastlogin=now() where account='" + account + "'";
			log.info(updateSQL);
			adb.getQrun().update(adb.getConn(), updateSQL);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
