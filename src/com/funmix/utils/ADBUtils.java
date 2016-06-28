package com.funmix.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.io.FileUtils;
// import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.funmix.common.Log4jLogger;
import com.funmix.exception.TimeoutException;
import com.funmix.model.Device;

public class ADBUtils {

	protected static Logger		log		= Log4jLogger.getLogger(ADBUtils.class);
	private String				adbPath	= "adb";
	private LinkedList<String>	queue;
	private String				workDir;
	private Connection			conn;
	private Device				dev;
	private QueryRunner			qrun	= new QueryRunner();
	private FakeUtils			fake	= new FakeUtils();
	
	public QueryRunner getQrun() {
		return qrun;
	}

	public Device getDevice() {
		return dev;
	}

	public LinkedList<String> getQueue() {
		return queue;
	}

	public Connection getConn() {
		return conn;
	}

	public String getWorkDir() {
		return workDir;
	}

	public String getAdbPath() {
		return adbPath;
	}

	public ADBUtils(Device dev, LinkedList<String> queue, Connection conn) throws IOException, SQLException {
		this.queue = queue;
		this.conn = conn;
		this.dev = dev;
		workDir = Utils.getCurrentPath() + "//" + dev.getDevice();
		if (new File(workDir).exists() == false) {
			FileUtils.forceMkdir(new File(workDir));			
		}
		FileUtils.copyFile(new File(Utils.getCurrentPath() + "/WIFI"), new File(workDir+"/WIFI"));
	}	

	public int sqlUpdate(String sql){
		try {
			return qrun.update(conn, sql);
		} catch (SQLException e) {
			log.error(e);
			log.error(sql);
		}
		return -1;
	}
	
	//HashMap<String,String> areas;

	public boolean checkNet(String area) {
	/*		for(int i=0;i<Utils.areas.length;i++){
				if(Utils.areas[i][0].equals(area)){
					try {
						area = new String(Utils.areas[i][1].getBytes(),"UTF8");
					} catch (UnsupportedEncodingException e) {
						log.error(e);
					}
					break;
				}
			}
		String result = "";
		for(int i=0;i<=3;i++){
			result = execADB(" shell /data/curl --connect-timeout 3 http://ip.cn");
			if(result.indexOf(area)>-1){
				return true;
			}else{
				log.error("NET ERROR:" + result);
			}			
		}
		sqlUpdate("update tworkdev set log = '" + result + "' where deviceid=" + dev.getId());		
		return false;*/
		return true;
	}

	public void reboot() {
		execADB(" reboot");
		chgDeviceStatus(0);
	}
	
	public void clearInput(){
		for(int i=0;i<10;i++){
			execADB("shell input keyevent KEYCODE_DEL");
			sleep(50);
		}
	}

	public void inputText(String text) {
		execADB("shell input text " + text);
		sleep(1000);
	}

	public void tapKey(String keycode) {
		execADB("shell input keyevent " + keycode);
		sleep(1000);
	}

	public void tap(int x, int y) {
		execADB("shell input tap " + x + " " + y);
		Utils.sleep(500);
	}

	public void swipe(int x1, int y1, int x2, int y2) {
		execADB("shell input swipe " + x1 + " " + y1 + " " + x2 + " " + y2);
	}

	public String execADB(String cmd) {// adb
		return Utils.execCMD(adbPath + " -s " + dev.getDevice() + " " + cmd);
	}

	public boolean DeviceOnboard() {
		String result = Utils.execCMD(adbPath + " devices");
		int pos = result.indexOf(dev.getDevice());
		if (pos > -1) {
			int pos1 = result.indexOf("\r\n", pos);
			String device = result.substring(pos, pos1);
			if (device.indexOf("device") > -1)
				return true;
			else {
				pos = result.indexOf(dev.getDevice(), pos1);
				if (pos > -1) {
					pos1 = result.indexOf("\r\n", pos);
					device = result.substring(pos, pos1);
					if (device.indexOf("device") > -1)
						return true;
				}
			}
		}
		return false;
	}

	public void waitDeviceOn() {
		int trycount = 0;
		while (!DeviceOnboard()) {
			log.info("wait for device on:" + dev.getDevice() + ",try count:" + trycount++);
			sleep(3000);
		}
		chgDeviceStatus(1);
		log.info("device:" + dev.getDevice() + " is online!");
	}

	public void waitDeviceReady(int trycount) {
		int status = -1;
		for (int i = 1; i <= trycount; i++) {
			log.info("wait for device ready!try count:" + i);
			status = Utils.getKey(new String[]{"No longer want com.android.settings", "No longer want com.android.music", "Process com.mediatek.mtklogger", "[E]doInit finished"}, queue);
			if (status > -1) {
				break;
			} else {
				Utils.sleep(5000);
			}
		}
		log.info("device:" + dev.getDevice() + " is ready!");
		chgDeviceStatus(2);
	}

	public void startActivity(String activity, String key) {
		log.info("Start " + activity);
		execADB("shell am start -n " + activity);
		sleep(2000);
		if (getKey(key) == -1) {
			sleep(10000);
			execADB("shell am start -n " + activity);
		}
	}

	public void stopActivity(String activity) {
		log.info("Force stop " + activity);
		do {
			execADB("shell am force-stop " + activity);
			sleep(2000);
		} while (getKey("Force stopping package " + activity) == -1);
		sleep(3000);
	}

	public boolean clearAppData(String app) {
		log.info(app + " clearAppData start!");
		execADB("shell pm clear " + app);
		sleep(3000);
		log.info(app + " clearAppData end!");
		return true;
	}

	private int getKey(String key) {
		return Utils.getKey(key, queue);
	}
	
	/*private String getKeyvalue(String key){
		return
	}*/

	public void waitKey(String key) throws TimeoutException {
		int k = 0;
		while (getKey(key) == -1) {
			sleep(1000);
			if (k++ > 75) {
				throw new TimeoutException("Wait Key Timeout,key=" + key);
			}
		}
	}

	private void sleep(int delay) {
		Utils.sleep(delay);
	}

	public void clearLogcat() {
		execADB(" logcat -c");
		sleep(2000);
	}

	private void chgDeviceStatus(int status) {
		try {
			qrun.update(conn, "update tdevice set status=" + status + ",lastupdate=now() where id=" + dev.getId());
		} catch (Exception e) {
			log.error(e);
		}
	}

	public void refreshWifi() {
		execADB("  shell svc wifi disable");
		// execADB("  push " + workDir + "//ipconfig.txt /data/misc/wifi/ipconfig.txt");
		// execADB("  shell chown system.wifi /data/misc/wifi/ipconfig.txt");
		execADB("  shell svc wifi enable");
	}

	public BufferedImage getScreenShot() {
		execADB("shell /system/bin/screencap -p /sdcard/screenshot.png");
		File sf = new File(workDir + "//screenshot.png");
		sf.delete();
		execADB("pull /sdcard/screenshot.png " + workDir);
		do {
			sleep(500);
		} while (!sf.exists());
		log.info("get screenshot ok");
		return ImageUtils.getImage(workDir + "//screenshot.png");
	}

	public void reRwritePhoneInfo() {
		reRwritePhoneInfo(fake.randIMEI(),-1,-1,null,Utils.genAndrodid());
	}

	public void reRwritePhoneInfo(String imei, int phone, int ver) {	
		log.info(imei + "," + phone + "," + ver);	
		writeIMEI(imei);
		writeBuildProp(phone,ver);
		writeMAC(null);
		reboot();
	}
	
	public void reRwritePhoneInfo(String imei, int phone, int ver,String mac,String aid) {	
		log.info(imei + "," + phone + "," + ver + "," + mac + "," + aid);	
		writeTenddata(aid);
		writeIMEI(imei);
		writeBuildProp(phone,ver);
		writeMAC(mac);
		reboot();
	}

	private void writeTenddata(String aid) {
		FileWriter writer;
        try {
            writer = new FileWriter(getWorkDir() +"/tenddata.txt");
            writer.write(aid + "\r\n");
            writer.flush();
            writer.close();
            execADB(" push " + getWorkDir() + "/tenddata.txt /sdcard/tenddata.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }	
	}

	private void writeMAC(String mac) {// write mac
		execADB(" pull /data/nvram/APCFG/APRDEB/WIFI " + getWorkDir() + "/.");		
		if(mac == null){
			fake.writeWIFIFile(getWorkDir() +"/WIFI");
		}else{
			fake.writeWIFIFile(getWorkDir() +"/WIFI",fake.getMacBytes(mac));
		}
		execADB(" push " + workDir + "/WIFI /data/nvram/APCFG/APRDEB/WIFI");
		execADB(" shell chmod 660 /data/nvram/APCFG/APRDEB/WIFI");
		Utils.sleep(1000);
		execADB("  shell svc wifi disable");
		execADB("  shell svc wifi enable");
		Utils.sleep(2000);
		log.info(execADB(" shell cat /sys/class/net/wlan0/address "));
		log.error("writeWIFIFile SUCCESS!");
	}

	private void writeBuildProp(int phone, int ver) {//write build prop
		execADB(" pull /system/build.prop " + getWorkDir() + "/.");			
		fake.writeBuildProp(getWorkDir() + "/build.prop",fake.getFakePhone(phone, ver));
		execADB(" shell mount -o remount,rw /system");			
		execADB(" push " + getWorkDir() + "/build.prop /system/build.prop");
		execADB(" shell chmod 644 /system/build.prop");
		execADB(" shell mount -o remount,ro /system");		
	}
	
	private void writeIMEI(String imei){
		try{
			int k=0;
			while (getKey("Write IMEI Finish") == -1 && k++<3){
				execADB("shell am force-stop com.funmix.imei");
				sleep(2000);
				execADB("shell am start -n com.funmix.imei/.WriteIMEI --es imei " + imei);
				sleep(2000);
			}
			if(k==3)
				log.error("Run MTKTools Failed!");
		}catch(Exception e){
			log.error("Run MTKTools Failed!");
		}
	}

	/*public void setRoute() {		
		if(dev.getId()<100){
			router.setRoute("192.169.30." + (100+dev.getId()),dev.getLine());
		}else if(dev.getId()>=100 && dev.getId()<200){
			router.setRoute("192.169.31." + dev.getId(),dev.getLine());
		}else if(dev.getId()>=200 && dev.getId()<300){
			router.setRoute("192.169.32." + (dev.getId()-100),dev.getLine());
		}
	}*/
}
