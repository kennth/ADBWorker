package com.funmmix.app;

import java.awt.image.BufferedImage;
import org.apache.log4j.Logger;
import com.funmix.common.Log4jLogger;
import com.funmix.exception.TimeoutException;
import com.funmix.model.ADBWork;
import com.funmix.utils.ADBUtils;
import com.funmix.utils.Utils;

public class WorkMod {

	protected static Logger			log		= Log4jLogger.getLogger(WorkMod.class);
	protected BufferedImage			scbuff;
	protected String				vcodeTmp;
	protected String				cmd;
	protected ADBUtils				adb;
	protected ADBWork				work;
	protected ADBLogger				adblog;

	public void setWork(ADBWork work) {
		this.work = work;
	}

	public void run() throws Exception {

	}

	protected void tap(int x, int y) {
		adb.tap(x, y);
	}

	protected void sleep(int delay) {
		Utils.sleep(delay);
	}
	
	protected int getKey(String[] keys) {
		return Utils.getKey(keys, adb.getQueue());
	}
	
	protected int getKey(String key) {
		return Utils.getKey(new String[]{key}, adb.getQueue());
	}

	protected void startMTKTools() {
		log.info("startMTKTools start");
		adb.reRwritePhoneInfo();
		log.info("startMTKTools succ");
	}
	
	protected void updateWorkStatus(int status){
		adb.sqlUpdate("update twork set lastupdate=now(),status=" + status + " where id=" + work.getId());
	}
}
