package com.funmmix.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.log4j.Logger;

import com.funmix.common.Log4jLogger;
import com.funmix.exception.TimeoutException;
import com.funmix.model.ADBWork;
import com.funmix.model.Device;
import com.funmix.utils.ADBUtils;
import com.funmix.utils.Utils;

public class FBZYUCLoginMod extends WorkMod {

	protected static Logger	log		= Log4jLogger.getLogger(FBZYUCLoginMod.class);	
	private Device dev;
	private String account;
	private String passwd;
	private String imei;
	private int phone;
	private int ver;
	private String mac;
	private String uname;
	private String aid;
	private String tcookieid;
	
	public FBZYUCLoginMod(ADBWork work, ADBUtils adb) {
		this.work = work;
		this.adb = adb;	
		this.dev = adb.getDevice();
		try {						
			cmd = adb.getAdbPath() + " -s " + dev.getDevice() + " logcat -s ActivityManager:i MTKTool:d GSOFTGAME:v UCGameSdk:d";
			String tmps[] = work.getExtra().split("#");
			account = tmps[0];
			passwd = tmps[1];
			Map<String, Object> rs = adb.getQrun().query(adb.getConn(),"select imei,phone,ver,mac,androidid,imsi,simserial,serialno,tcookieid from taccount_fbzy where id=" + work.getKeyid(), new MapHandler());
			imei = rs.get("imei").toString();
			phone = Integer.parseInt(rs.get("phone").toString());
			ver = Integer.parseInt(rs.get("ver").toString());
			mac = rs.get("mac").toString();
			aid = "£º2|{MAC}|" + rs.get("androidid").toString() + "|{IMEI}|" + rs.get("imsi").toString() + "|" + rs.get("simserial").toString() + "|{ID}|null|" + rs.get("serialno").toString();
			tcookieid = rs.get("tcookieid") + "";
			log.info("tcookieid:" + tcookieid);
			log.info("device init end!");
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	public void run() throws TimeoutException, SQLException {
		clearData();
		adb.reRwritePhoneInfo(imei,phone,ver,mac,aid);	
		
		adb.waitDeviceOn();
		adb.clearLogcat();
		adblog = new ADBLogger(adb.getQueue(), cmd);
		adblog.start();
		adb.waitDeviceReady(6);			
		
		clearData();
		//wait 
		emuGame(work);
		
	}
	
	private void emuGame(ADBWork work) {
		log.info("start emuGame!");
		// String start = Utils.getTime();
		try {
			if(work.getSpec()>0 && !tcookieid.equalsIgnoreCase("null")){
				writeTcookieid();
			}
			addGameLog(0);
			startGame();
			updateGameStatus(1);	
			UCLogin(work);	
			updateGameStatus(2);
			saveTcookieid();
			enterGame();			
			updateGameStatus(3);
			if(work.getSpec()==0){				
				clickDialog();
				updateGameStatus(4);
				renameChar();
			}else{
				clearDialog();				
			}
			updateGameStatus(6);
			simGame();
			updateGameStatus(9);			
			stopGame();
		} catch (TimeoutException e) {
			log.error(e);
		} 
		log.info("end emuGame!");
	}
	
	private void writeTcookieid() {
		log.info("write tcookieid:" + tcookieid);
		adb.execADB(" shell echo \"" + tcookieid + "\" > /storage/sdcard0/.tcookieid");		
	}

	private void saveTcookieid(){
		log.info("save tcookieid");
		adb.execADB(" pull /sdcard/.tcookieid " + adb.getWorkDir() + "//.");
		File file = new File(adb.getWorkDir() + "//.tcookieid");
	    BufferedReader reader = null;
	    String tempString = null;
	    String data = "";
	    try {
	        reader = new BufferedReader(new FileReader(file));
	        while ((tempString = reader.readLine()) != null) {
	            data = tempString;
	        }
	        reader.close();
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }finally{
	        if(reader != null){
	            try {
	                reader.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	    }
	    log.info("tcookieid:" + data);
	    if(data.length()>0)
	    	adb.sqlUpdate("update taccount_fbzy set tcookieid='" + data + "' where id=" + work.getKeyid());
	}
	
	private void UCLogin(ADBWork work) throws TimeoutException {		
		log.info("sim login");
		int status = -1;
		int k = 0;
		while(status == -1){
			status = getKey(new String[]{"ÊÖ»úµÇÂ¼(ÎÞÐè×¢²á)@","UCºÅµÇÂ¼@"});
			log.info(status);
			if(status == 0){//ÊÖ»úµÇÂ¼
				k = 0;
				status = -1;
				adb.tap(430, 448);//UCºÅµÇÂ¼
				sleep(2000);
			}else if(status == 1){//UCºÅµÇÂ¼
				log.info("start login");
				//enter account
				adb.tap(485, 220);
				sleep(1500);				
				log.info("input account");
				adb.clearInput();				
				adb.inputText(account);
				adb.tap(900, 151);
				log.info("input passwd");
				sleep(1000);
				adb.inputText(passwd);
				adb.tap(910, 180);
				log.info("submit login");
				int m =0;
				while((status=getKey(new String[]{"µÇÂ¼³É¹¦£¬µÇÂ¼µÄUCÕË»§Îª","ÕËºÅ»òÃÜÂë´íÎó"}))==-1){					
					if(m++>10)
						throw new TimeoutException("Login Fail,Wait too long!");
					sleep(1000);
				}
				log.info("LOGIN Status:" + status);
				if(status==0){
					adb.sqlUpdate("update taccount_fbzy set status = 1,lastupdate=now() where id=" + work.getKeyid());
					sleep(2000);
					break;
				}else {
					adb.sqlUpdate("update taccount_fbzy set status = -1,lastupdate=now() where id=" + work.getKeyid());
					throw new TimeoutException("loginfail");						
				}
			}else{
				sleep(1000);
				adb.tap(515, 484);
			}
			if(k++>=60)
				throw new TimeoutException("Login Fail");
		}
	}
	
	private void enterGame() throws TimeoutException{
		log.info("start enterGame");
		int k=0;
		while(getKey("OnConnect ok")==-1){//do enter game			
			adb.tap(173,490);
			adb.tap(173,490);
			sleep(1000);			
			adb.tap(515, 484);
			sleep(500);
			if(k++>30)
				throw new TimeoutException("enter game failed");
		}	
		log.info("enterGame succ");
	}
	
	private void clearDialog() throws TimeoutException{
		log.info("start clear Diaglog");
		adb.waitKey("pGameCre->m_wModelId[37]");
		log.info("process dialog");
		for(int i=0;i<3;i++){
			adb.tap(876,78);sleep(1000);
			adb.tap(852,73);sleep(1000);
			adb.tap(808,99);sleep(1000);
			adb.tap(808,99);sleep(1000);
			adb.tap(808,99);sleep(1000);;
		}
		log.info("clear Diaglog end");
	}
	
	private void clickDialog() throws TimeoutException{
		log.info("start click Diaglog");
		adb.waitKey("pGameCre->m_wModelId[37]");
		log.info("process dialog");
		int k=0;
		int status = -1;
		while((status = getKey("Create Actor begin"))==-1){
			adb.tap(876,78);sleep(1000);
			adb.tap(852,73);sleep(1000);
			adb.tap(808,99);sleep(1000);
			adb.tap(808,99);sleep(1000);
			adb.tap(808,99);sleep(1000);
			if(k++>5)
				break;
		}
		if(status == 0){
			adb.getQueue().offer("Create Actor begin");
		}
		log.info("click Diaglog end");
	}
	
	private void renameChar() throws TimeoutException{
		if(getKey("Create Actor begin")==-1){
			return;
		}
		log.info("start create name");
		updateGameStatus(5);
		int k=0,succ = -1;
		while(succ==-1){
			adb.tap(542, 288);
			sleep(1000);		
			uname = Utils.enames[Utils.getRandom(0, 285)] + Utils.getRandom(0, 999);
			adb.clearInput();
			adb.inputText(uname);
			adb.tapKey("KEYCODE_ENTER");
			sleep(2000);
			adb.tap(548, 435);	
			sleep(2000);
			adb.tap(664,363);	
			succ = getKey(new String[]{"Create Actor Err:","Create Actor Name Succ"});
			if(succ == 0){
				succ = -1;
			}else if(succ==1){				
				adb.sqlUpdate("update taccount_fbzy set status = 1,_1st=now(),lastupdate=now(),uname = '" + uname + "' where id=" + work.getKeyid());
				log.info("create char succ!");					
			}
			if(k++>10){
				throw new TimeoutException("create char name failed!");
			}					
		}
		log.info("create name end");
	}
	
	private void simGame() throws TimeoutException{		
		log.info("start sim game");
		simRandomTap();
		log.info("sim game end!");
	}
	
	private void simRandomTap(){
		for(int i=0;i<40;i++){
			adb.tap(Utils.getRandom(200, 700), Utils.getRandom(150, 300));
			sleep(2500);
		}
	}

	private void clearData() {
		adb.clearAppData("com.gsoftcn.moba.uc");
		adb.execADB(" shell rm -f -r /storage/sdcard0/gsoft");
		adb.execADB(" shell rm -f -r /storage/sdcard0/ucgamesdk");
		adb.execADB(" shell rm -f /storage/sdcard0/.tcookieid");
		sleep(2000);
	}

	private void startGame() throws TimeoutException {
		adb.startActivity("com.gsoftcn.moba.uc/com.gsoftcn.MobaGame.SplashActivity", "Start proc com.gsoftcn.moba.uc for activity com.gsoftcn.moba.uc/com.gsoftcn.MobaGame.SplashActivity:");
		adb.waitKey("LoadPlugin [UserLogin]");
		log.info("start FBZY_UC success");
	}

	private void stopGame() {
		adb.stopActivity("com.gsoftcn.moba.uc");
		log.info("stop FBZY_UC success");
	}	
	
	private void updateGameStatus(int status) {
		if(status==3)//enter game succ
			adb.sqlUpdate("update taccount_fbzy set status = 1," + Utils.logincol[work.getSpec()]+ "=(now()-interval 5 minute),lastupdate=now() where id=" + work.getKeyid());
		adb.sqlUpdate("update tfblog set status = " + status + ",lastupdate=now() where to_days(now())=to_days(lastupdate) and workid=" + work.getId());
	}

	private long addGameLog(int status) {
		String updateSQL = "insert into tfblog (keyid,start,workid,spec) values (" + work.getKeyid() + ",now()," + work.getId() + "," + work.getSpec() + ")";
		log.info(updateSQL);
		return adb.sqlUpdate(updateSQL);
	}
	

}
