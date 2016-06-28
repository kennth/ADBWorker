package com.funmmix.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.log4j.Logger;

import com.funmix.common.Log4jLogger;
import com.funmix.exception.TimeoutException;
import com.funmix.model.ADBWork;
import com.funmix.model.AuthData;
import com.funmix.model.Device;
import com.funmix.utils.ADBUtils;
import com.funmix.utils.Utils;
import com.tencent.msdk.tea.DesUtils;
import com.tencent.msdk.tea.TEACoding;

public class DragonAuthMod extends WorkMod {

	protected static Logger	log	= Log4jLogger.getLogger(DragonAuthMod.class);
	private Device			dev;

	public DragonAuthMod(ADBWork work, ADBUtils adb) {
		this.work = work;
		this.adb = adb;
		this.dev = adb.getDevice();
		cmd = adb.getAdbPath() + " -s " + dev.getDevice() + " logcat -s ActivityManager:i MTKTool:d Unity:i";
	}

	public void run() throws Exception {
		adb.clearLogcat();
		adb.waitDeviceOn();
		adblog = new ADBLogger(adb.getQueue(), cmd);
		adblog.start();
		adb.waitDeviceReady(5);
		// write imei& phone info
		AuthData authdata = getAuthdata(work.getKeyid());
		startMTKTools(authdata);
		adb.waitDeviceOn();
		adb.clearLogcat();
		adblog = new ADBLogger(adb.getQueue(), cmd);
		adblog.start();
		adb.waitDeviceReady(5);
		clearMID();
		clearData();
		writeCFGFile();
		emuGame(work, authdata);
	}

	private void writeCFGFile() {
		adb.execADB(" shell mkdir /storage/sdcard0/Android/data/com.tencent.tmgp.dragonar3d");
		adb.execADB(" shell mkdir /storage/sdcard0/Android/data/com.tencent.tmgp.dragonar3d/files");
		adb.execADB(" shell echo \"1\" > /storage/sdcard0/Android/data/com.tencent.tmgp.dragonar3d/files/Device.ini");
		log.info("writeCFGFile finished!");
	}

	protected void startMTKTools(AuthData authdata) {
		// log.info(authdata.getImei() + "," + authdata.getPhone()+ "," +
		// authdata.getVer());
		log.info("startMTKTools start");
		adb.reRwritePhoneInfo(authdata.getImei(), authdata.getPhone(), authdata.getVer());
		log.info("startMTKTools succ");
	}

	private void clearData() {
		adb.clearAppData("com.tencent.mobileqq");
		adb.clearAppData("com.tencent.tmgp.dragonar3d");
		sleep(2000);
	}

	private void emuGame(ADBWork work, AuthData authdata) {
		log.info("start emuGame!");
		// String start = Utils.getTime();
		try {
			addGameLog(0);
			int waittime = Integer.parseInt(work.getExtra());
			log.info("wait " + waittime * 3 + " seconds!");
			sleep(waittime * 3000);
			updateGameStatus(1);
			startGame();
			stopGame();
			updateGameStatus(2);
			writeAuthFile(authdata);
			addTryCount(work);
			updateGameStatus(3);
			startGame();
			log.info("startGame succ!");
			updateGameStatus(4);
			enterGame();
			log.info("enterGame succ!");
			updateGameStatus(5);
			simGameTime();
			log.info("simGameTime end!");
			updateGameStatus(6);
			stopGame();
			log.info("stopGame succ!");
			updateGameStatus(9);
			// addGameLog(work, start, 1);
		} catch (TimeoutException e) {
			log.error(e);
		} catch (SQLException e) {
			log.error(e);
		} catch (ClassNotFoundException e) {
			log.error(e);
		}
		log.info("end emuGame!");
	}

	private void clearMID() throws ClassNotFoundException, SQLException {
		adb.execADB(" shell rm /storage/sdcard0/tencent/mta/.mid.txt");
		/*
		 * adb.execADB(
		 * " pull /data/data/com.android.providers.settings/databases/settings.db "
		 * + adb.getWorkDir() + "//."); Class.forName("org.sqlite.JDBC");
		 * Connection connection = DriverManager.getConnection("jdbc:sqlite:" +
		 * adb.getWorkDir() + "//settings.db"); Statement statement =
		 * connection.createStatement(); String upSQL =
		 * "delete from system where name like '%MTA%'";
		 * statement.executeUpdate(upSQL); DbUtils.closeQuietly(connection);
		 * adb.execADB(" push " + adb.getWorkDir() +
		 * "//settings.db /data/data/com.android.providers.settings/databases/."
		 * );
		 */
	}

	private void writeAuthFile(AuthData authdata) throws ClassNotFoundException, SQLException {
		adb.execADB(" pull /data/data/com.tencent.tmgp.dragonar3d/databases/WEGAMEDB2 " + adb.getWorkDir() + "//.");
		updateAuthFile(authdata);
		adb.execADB(" push " + adb.getWorkDir() + "//WEGAMEDB2 /data/data/com.tencent.tmgp.dragonar3d/databases/.");
		log.info("writeAuthFile finished!");
	}

	private void updateAuthFile(AuthData authdata) throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");
		String inSQL = "", inSQL1 = "", upSQL = "";
		double aexpire, pexpire;
		// create a database connection
		Connection connection = DriverManager.getConnection("jdbc:sqlite:" + adb.getWorkDir() + "//WEGAMEDB2");
		Statement statement = connection.createStatement();
		statement.setQueryTimeout(30); // set timeout to 30 sec.
		ResultSet rs = statement.executeQuery("select msdkVersion from app_info");
		String msdkver = "2.7.1a";
		if (rs.next())
			msdkver = rs.getString(1);
		TEACoding teaCode = new TEACoding(DesUtils.DB_KEY);
		if (authdata.getPlatformid() == 1) {// wx
			aexpire = authdata.getLastupdate() + 60 * 60 * 24 * 30;
			inSQL = "insert into wx_login_info (open_id,access_token,access_token_expire,refresh_token,refresh_token_expire,pf,pf_key,create_at) values ('" + authdata.getOpenid()
					+ "','" + teaCode.encode2HexBase64(authdata.getAccess_token().getBytes()) + "'," + aexpire + ",'" + teaCode.encode2HexBase64(authdata.getPay_token().getBytes())
					+ "'," + aexpire + ",'" + teaCode.encode2HexBase64(authdata.getPf().getBytes()) + "','" + teaCode.encode2HexBase64(authdata.getPf_key().getBytes()) + "',"
					+ (authdata.getLastupdate() * 1000 + Utils.getRandom(1, 9999)) + ")";
			inSQL1 = "INSERT INTO app_info VALUES('wx9a14459a98464cad','" + authdata.getOpenid() + "','" + authdata.getImei() + "','" + msdkver
					+ "','com.tencent.tmgp.dragonar3d',NULL);";
		} else {// qq
			aexpire = authdata.getLastupdate() + 60 * 60 * 24 * 90;
			pexpire = authdata.getLastupdate() + 60 * 60 * 24 * 7;
			inSQL = "insert into qq_login_info (open_id,access_token,access_token_expire,pay_token,pay_token_expire,pf,pf_key,create_at) values ('" + authdata.getOpenid() + "','"
					+ teaCode.encode2HexBase64(authdata.getAccess_token().getBytes()) + "'," + aexpire + ",'" + teaCode.encode2HexBase64(authdata.getPay_token().getBytes()) + "',"
					+ pexpire + ",'" + teaCode.encode2HexBase64(authdata.getPf().getBytes()) + "','" + teaCode.encode2HexBase64(authdata.getPf_key().getBytes()) + "',"
					+ (authdata.getLastupdate() * 1000 + Utils.getRandom(1, 9999)) + ")";
			inSQL1 = "INSERT INTO app_info VALUES(1103965864,'" + authdata.getOpenid() + "','" + authdata.getImei() + "','" + msdkver + "','com.tencent.tmgp.dragonar3d',NULL);";
		}
		statement.executeUpdate(inSQL);
		statement.executeUpdate(inSQL1);
		// String msdk = statement.executeQuery(sql)
		upSQL = "update app_info set matid='" + authdata.getImei() + "'";
		statement.executeUpdate(upSQL);
		DbUtils.closeQuietly(connection);
	}

	private AuthData getAuthdata(String key) throws SQLException {
		return adb.getQrun().query(adb.getConn(), "select * from tauthdata where id=" + key, new BeanHandler<AuthData>(AuthData.class));
	}

	private void enterGame() throws TimeoutException {
		long estart = System.currentTimeMillis();
		adb.waitKey("_______  SelectServer");
		sleep(1000);
		tap("entergame");
		sleep(1000);
		int k = 0,result = -1;
		while ((result = getKey(new String[] { "#Need Register", "#Has Register","Network is unreachable" })) == -1) {
			if(result == 0){
				updateGameStatus(-3);
				throw new TimeoutException("Need Register!");
			}else if(result==2){
				tap("netretry");
				sleep(50);
				tap("netretry");
				sleep(100);
			}
			tap("entergame");
			sleep(50);
			tap("entergame");
			sleep(100);
			if (k++ > 30 || System.currentTimeMillis() - estart > 1000 * 75) {
				updateGameStatus(-2);
				throw new TimeoutException("auth after failed!");
			}
			if (k % 5 == 0)
				sleep(1000);
		}
	}

	private void simGameTime() {
		for (int i = 0; i < 15; i++) {
			sleep(1000);
		}
	}

	private void startGame() throws TimeoutException {
		adb.startActivity("com.tencent.tmgp.dragonar3d/.U3DActivity", "Start proc com.tencent.tmgp.dragonar3d for activity");
		int key = -1, k = 0;
		while ((key = getKey(new String[] { "LoginPanel--ShowQQWXBtn", "_______  SelectServer" })) == -1) {
			sleep(2000);
			if (k++ > 45)
				throw new TimeoutException("start game Failed!");
		}
		if (key == 1) {
			sleep(10000);
			adb.getQueue().offer("_______  SelectServer");
		}
	}

	private void stopGame() throws TimeoutException {
		adb.stopActivity("com.tencent.tmgp.dragonar3d");
	}

	private void tap(String obj) {
		log.info("tap:" + obj);
		if (obj.equals("entergame")) {// 进入游戏
			tap(287, 885);// tap(678,568);
		}else if (obj.equals("netretry")) {//重试网络
			tap(287, 553);// tap(678,568); 
		} else {
			log.error("Error:tap obj not exist!!");
		}
	}

	private void updateGameStatus(int status) {
		adb.sqlUpdate("update tdralog set status = " + status + ",lastupdate=now() where to_days(now())=to_days(lastupdate) and workid=" + work.getId());
		if (status == 6) {
			//int day = work.getSpec() % 100;
			// sql = "update tauthdata set trycount=1," + Utils.logincol[day]+
			// "=now() where id=" + work.getKeyid();
			String sql = "insert into tpostlog (timestamp,eventid,zoneid,os,openid,accesstoken,paytoken,pf,pfkey,channelid,platformid,ip,subtime,info,amount,day)  "
					+ "select UNIX_TIMESTAMP(now()),1,1,1,openid,access_token,pay_token,pf,pf_key,0,platformid,ip,UNIX_TIMESTAMP(now()),concat(info,'DEBUG'),0,to_days(now()) from tauthdata where id="
					+ work.getKeyid();
			log.info(sql);
			adb.sqlUpdate(sql);
			adb.sqlUpdate("update tauthdata set trycount=0 where id=" + work.getKeyid());
		}else if(status == 4){
			adb.sqlUpdate("update tauthdata set trycount=trycount+1 where id=" + work.getKeyid());
		}else if(status == -3){
			adb.sqlUpdate("update tauthdata set trycount=9999 where id=" + work.getKeyid());
		}
	}

	private long addGameLog(int status) {
		String updateSQL = "insert into tdralog (keyid,start,workid,spec) values (" + work.getKeyid() + ",now()," + work.getId() + "," + work.getSpec() + ")";
		// adb.sqlUpdate("update tauthdata set trycount=trycount+1 where id=" +
		// work.getKeyid());
		log.info(updateSQL);
		return adb.sqlUpdate(updateSQL);
	}

	private void addTryCount(ADBWork work) {
		adb.sqlUpdate("update tauthdata set trycount=trycount+1 where id=" + work.getKeyid());
	}
}
