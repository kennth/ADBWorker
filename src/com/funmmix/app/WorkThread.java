package com.funmmix.app;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.log4j.Logger;
import com.funmix.common.DBMgr;
import com.funmix.common.Log4jLogger;
import com.funmix.model.ADBWork;
import com.funmix.model.Device;
import com.funmix.utils.ADBUtils;
import com.funmix.utils.Utils;

public class WorkThread extends Thread {

	protected static Logger		log			= Log4jLogger.getLogger(WorkThread.class);
	private Connection			conn;
	private QueryRunner			runner		= new QueryRunner();
	private LinkedList<String>	queue		= new LinkedList<String>();
	private Device				device		= new Device();
	private ADBUtils			adb;
	private boolean				stop		= false;
	private int					worktime	= 360;
	private WorkMod				wm;
	private String				lastwork;
	private boolean				debug		= false;
	
	public static void main(String[] args) {
		String deviceid = "545";
		if (args.length > 0 && args[0] != null) {
			deviceid = args[0];
		}
		new WorkThread(deviceid).start();
	}

	public WorkThread(String deviceid) {
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
				regWorkDev(device);
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
		String workSQL = "select * from twork where now()<(lastupdate - interval " + worktime + " second) and status =0 and deviceid=" + device.getId() + " limit 1";
		//log.info(workSQL);
		try {
			if (!debug) {
				work = runner.query(conn, workSQL, new BeanHandler<ADBWork>(ADBWork.class));
			} else {
				work = new ADBWork();
				//work.setKeyid("6336383");
				//work.setExtra("861116466#1234566#864264023799072#16#1#84:38:38:37:AE:D3");
				//work.setExtra("839229037#1234566#863654025709593#15#1#d0:7a:b5:aa:ef:20");
				work.setKeyid("6336583");
				work.setExtra("839397739#1234566");
				work.setSpec(1);
				work.setWork("FBLogin");
			}
		} catch (Exception e) {
			log.error(e);
		}
		return work;
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
				int k = 1;
				while ((work = getWork()) == null) {
					log.info("wait for work!");
					sleep(5000);
					if(k++ % 12 == 0){
						refWorkDevStatus();
					}
				}
				log.info("key:" + work.getKeyid() + ",work:" + work.getWork() + ",extra:" + work.getExtra() + ",spec:" + work.getSpec());
				updateWorkDevStatus(work, 2);
				doWork(work);
				updateWorkDevStatus(work, 1);
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
			try {
				removeWorker(device);
			} catch (SQLException e) {
				log.info(e.getMessage());
			}
			DbUtils.closeQuietly(conn);
		}
	}

	private void doWork(ADBWork work) throws Exception {
		if (!lastwork.equals(work.getWork())) { // work changed
			worktime = runner.query(conn, "select worktime from tworkprop where work='" + work.getWork() + "'", new ScalarHandler<Number>(1)).intValue();
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
			} else if (work.getWork().equalsIgnoreCase("FBLogin")|| work.getWork().equalsIgnoreCase("FBLoginr") ) {
				wm = new FBZYUCLoginMod(work, adb);
			}
		} else {
			wm.setWork(work);
		}
		wm.run();
	}

	private void removeWorker(Device device) throws SQLException {
		runner.update(conn, "delete from tworkdev where deviceid=" + device.getId());
	}
	
	private void refWorkDevStatus() throws SQLException{
		runner.update(conn, "update tworkdev set lastupdate=now(),status=1 where deviceid=" + device.getId());
	}

	private void updateWorkDevStatus(ADBWork work, int status) throws SQLException {				
		if (!debug){
			//runner.update(conn, "update twork set lastupdate=now(),status=" + status + " where id=" + work.getId());
			if(status == 1){ // work finished
				runner.update(conn,"update tline set jobs = jobs -1 where line = '" + device.getLine() + "'");				
			}
			runner.update(conn, "update tworkdev set line='" + device.getLine() + "',lastupdate=now(),status=" + status + ",work='" + work.getWork() + "' where deviceid=" + device.getId());
			runner.update(conn,"update twork set status=" + status + " where id= " + work.getId());
		}
	}

	private void regWorkDev(Device device) {
		try {
			if (!debug){
				runner.update(conn, "delete from tworkdev where deviceid = " + device.getId());
				runner.update(conn, "insert into tworkdev (line,deviceid,status) values ('" + device.getLine() + "','" + device.getId() + "',1)");
			}
		} catch (SQLException e) {
			log.info("updateWorkDevStatus error:" + e.getMessage());
		}
	}
}
