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

public class ZJNewsMod extends WorkMod {

	protected static Logger	log		= Log4jLogger.getLogger(ZJNewsMod.class);	
	protected ImgCompareUtils		imgComp;
	private Device dev;
	
	public ZJNewsMod(ADBWork work, ADBUtils adb) {
		this.work = work;
		this.adb = adb;	
		this.dev = adb.getDevice();
		try {
			imgComp = new ImgCompareUtils(adb.getDevice().getDevice());
			log.info("imgCompare init end!");
			vcodeTmp = adb.getWorkDir() + "//vcode.png";
			cmd = adb.getAdbPath() + " -s " + dev.getDevice() + " logcat -s mqq:d ActivityManager:i MTKTool:d";
//			String tmps[] = work.getExtra().split("#");
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
	
	
	private void QQread(ADBWork work) throws TimeoutException {		
		log.info("start sim guide");
		simGuide();
		log.info("sim guide end");		
	}
	
	private void simGuide() throws TimeoutException{
		adb.waitKey("[AppLaunch] Displayed Displayed com.cmstop.qjwb/com.wondertek.activity.AppFakeActivity");
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

	private void clearData() {
		adb.clearAppData("com.cmstop.qjwb");
		sleep(2000);
	}

	private void startQQRead() throws TimeoutException {
		adb.startActivity("com.cmstop.qjwb/com.wondertek.activity.AppFakeActivity", "Start proc com.cmstop.qjwb for activity com.cmstop.qjwb/com.wondertek.activity.AppFakeActivity:");
		log.info("start news success");
	}

	private void stopQQRead() {
		adb.stopActivity("com.cmstop.qjwb");
	}	
	

}
