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

public class QQReadThread  extends Thread {

	protected static Logger		log			= Log4jLogger.getLogger(WorkThread.class);
	private Connection			conn;
	private QueryRunner			runner		= new QueryRunner();
	private LinkedList<String>	queue		= new LinkedList<String>();
	private Device				device		= new Device();
	private ADBUtils			adb;
	private boolean				stop		= false;
	private int					worktime	= 240;
	private WorkMod				wm;
	private String				lastwork;
	private boolean				debug		= false;
	String ip = "";
	public static void main(String[] args) {
		String deviceid = "549";
		if (args.length > 0 && args[0] != null) {
			deviceid = args[0];
		}
		new QQReadThread(deviceid).start();
	}

	public QQReadThread(String deviceid) {
		this.device.setId(Integer.parseInt(deviceid));
	}

	private boolean init() {
		log.info("start init ");
		try {
			conn = DBMgr.getCon("helper");
			device = runner.query(conn, "select * from tdevice where id=" + device.getId(), new BeanHandler<Device>(Device.class));
			log.info("database init end!");
			if (device != null) {
				log.info("line:" + device.getLine());
				if(device.getLine().equalsIgnoreCase("UKN"))
					return false;
				adb = new ADBUtils(device, queue, conn);
				log.info("adbUtils init end!");
				log.info("workPath :" + adb.getWorkDir());		
				lastwork = "";
				log.info("reg work device end!");
			} else {
				return false;
			}
		} catch (Exception e) {
			log.error(e);
		}
		log.info("init end");
		return true;
	}

	private ADBWork getWork() {
		ADBWork work = null;
		String workSQL = "select account,passwd from tqqread where _1st is null and status=0 and area='" + device.getLine().substring(0, 3) + "' order by lastupdate limit 1";
		//log.info(workSQL);
		try {
			Map<String, Object> map = runner.query(conn, workSQL, new MapHandler());
			if(map == null)
				return null;
			work = new ADBWork();
			/*work.setKeyid(map.get("account").toString());
			work.setExtra(map.get("account").toString() + "#" + map.get("passwd").toString() + "#" + ip);
			work.setWork("qqread");			*/
			work.setWork("ZJNews");
			work.setKeyid("");
			work.setExtra("");
		} catch (Exception e) {
			log.error(e);
		}
		return work;
	}
	


	private String getIP() throws SQLException {
		Map<String, Object> map = runner.query(conn, "select ip from tline where line='" + device.getLine() + "'", new MapHandler());
		return map.get("ip").toString();
	}

	public void run() {
		if (init() == false) {
			log.info("init Failed");
			stop = true;
			return;
		}
		try {
			ADBWork work = null;
			do {
				adb.waitDeviceOn();
				while (getIP().equals(ip)) {
					log.info("wait for new ip!");
					sleep(10000);
				}
				ip = getIP();
				if((work = getWork()) == null) {
					log.info("all work done!");
					stop = true;
					break;
				}
				log.info("key:" + work.getKeyid() + ",work:" + work.getWork() + ",extra:" + work.getExtra() + ",spec:" + work.getSpec());
				
				doWork(work);
				
				log.info(device.getLine() + " " + device.getDevice() + " wait for next work!");
				sleep(10000);
				if (debug)
					break;
			} while (!stop);
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

	private void doWork(ADBWork work) throws Exception {
		if (!lastwork.equals(work.getWork())) { // work changed
			//worktime = runner.query(conn, "select worktime from tworkprop where work='" + work.getWork() + "'", new ScalarHandler<Number>(1)).intValue();
			if (work.getWork().equalsIgnoreCase("QQlogin")) {
				wm = new QQLoginMod(work, adb);
			}else if (work.getWork().equalsIgnoreCase("DragonLogin")) {
				 //wm = new DragonLoginMod(work, adb);
			} else if (work.getWork().equalsIgnoreCase("DragonAuth")) {
				wm = new DragonAuthMod(work, adb);
			} else if (work.getWork().equalsIgnoreCase("UpdateApp")) {
				wm = new AppUpdateMod(work, adb);
			} else if (work.getWork().equalsIgnoreCase("QQread") || work.getWork().equalsIgnoreCase("QQreadr")) {
				wm = new QQReadMod(work, adb);
			} else if (work.getWork().equalsIgnoreCase("ZJNews")) {
				wm = new ZJNewsMod(work, adb);
			}
		} else {
			wm.setWork(work);
		}
		wm.run();
	}



}
