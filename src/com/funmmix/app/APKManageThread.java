package com.funmmix.app;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.log4j.Logger;

import com.funmix.common.DBMgr;
import com.funmix.common.Log4jLogger;
import com.funmix.model.ADBWork;
import com.funmix.model.Device;
import com.funmix.utils.ADBUtils;
import com.funmix.utils.Utils;

public class APKManageThread  extends Thread {

	protected static Logger		log			= Log4jLogger.getLogger(WorkThread.class);
	private Connection			conn;
	private QueryRunner			runner		= new QueryRunner();
	private LinkedList<String>	queue		= new LinkedList<String>();
	private Device				device		= new Device();
	private ADBUtils			adb;
	String activity = "";
	public static void main(String[] args) {
		String deviceid = "540";
		if (args.length > 0 && args[0] != null) {
			deviceid = args[0];
		}
		new APKManageThread(deviceid).start();
	}

	public APKManageThread(String deviceid) {
		this.device.setId(Integer.parseInt(deviceid));
	}

	private boolean init() {
		log.info("start init ");
		try {
			conn = DBMgr.getCon("helper");
			activity = (String) runner.query(conn, "select activity from tcmcctask where left(worker,3)<=" + device.getId() + " and right(worker,3)>="+ device.getId() , 
					new MapHandler()).get("activity");	
			log.info(activity);			
			device = runner.query(conn, "select * from tdevice where id=" + device.getId(), new BeanHandler<Device>(Device.class));
			log.info("database init end!");
			if (device != null) {
				log.info("line:" + device.getLine());
				if(device.getLine().equalsIgnoreCase("UKN"))
					return false;				
				adb = new ADBUtils(device, queue, conn);
				log.info("adbUtils init end!");
				log.info("workPath :" + adb.getWorkDir());
				
			} else {
				return false;
			}
		} catch (Exception e) {
			log.error(e);
			return false;
		}
		log.info("init end");
		return true;
	}

	public void run() {
		if (init() == false) {
			log.info("init Failed");
			return;
		}
		try {			
			String cmd = "adb -s " + device.getDevice() + " shell logcat |grep 'ActivityManager' ";
			log.info(cmd);
			adb.waitDeviceOn();
			adb.clearLogcat();			
			ADBLogger adblog = new ADBLogger(adb.getQueue(), cmd);
			adblog.start();
			String tmp;
			String pack = activity.substring(0,activity.indexOf("/"));			
			do {
				sleep(1000);
				while(adb.getQueue().size()>0){
					tmp = adb.getQueue().poll();
					//log.info(tmp);
					if(tmp.indexOf("died")>-1 && tmp.indexOf(activity)>-1){
						log.info("Game died,restart it!");
						adb.startActivity(activity, "Start proc " + pack + " for activity");
					}else{
						//log.info("wait");
						sleep(1000);
					}
				}				
			} while (1==1);
		} catch (Exception e) {
			log.info(e);
		} finally {
			if (log != null) {
				Utils.sleep(3000);
				log = null;
			}
			DbUtils.closeQuietly(conn);
		}
	}
}
