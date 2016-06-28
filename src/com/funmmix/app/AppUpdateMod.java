package com.funmmix.app;

import org.apache.log4j.Logger;
import com.funmix.common.Log4jLogger;
import com.funmix.exception.TimeoutException;
import com.funmix.model.ADBWork;
import com.funmix.model.Device;
import com.funmix.utils.ADBUtils;
import com.funmix.utils.Utils;

public class AppUpdateMod extends WorkMod {

	protected static Logger	log		= Log4jLogger.getLogger(AppUpdateMod.class);	
	private Device dev;

	public AppUpdateMod(ADBWork work, ADBUtils adb) {
		this.work = work;
		this.adb = adb;	
		this.dev = adb.getDevice();
		cmd = adb.getAdbPath() + " -s " + dev.getDevice() + " logcat -s ActivityManager:i";
	}

	public void run() throws TimeoutException {
		adb.clearLogcat();
		adblog = new ADBLogger(adb.getQueue(), cmd);
		adblog.start();
		adb.waitDeviceReady(5);		
		updateApp(work);
	}
	
	private void updateApp(ADBWork work) {		
		try {
			String[] tmp = work.getExtra().split("#");			
			log.info( adb.execADB(" uninstall " + tmp[0]) );
			log.info( adb.execADB(" install -r " + Utils.getCurrentPath() + "//" + tmp[1]) );			
		} catch (Exception e) {	
			log.error(e);
		}
	}
}
