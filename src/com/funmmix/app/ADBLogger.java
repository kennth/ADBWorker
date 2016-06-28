package com.funmmix.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import org.apache.log4j.Logger;
import com.funmix.common.Log4jLogger;

class ADBLogger extends Thread {
	protected static Logger log = Log4jLogger.getLogger(ADBLogger.class);
	private LinkedList<String> queue;
	private String cmd;
	private boolean stop = false;
	private boolean debug = false;
	
	public static void main(String[] args) {
		String cmd = "adb -s E3CD20540 logcat -s ActivityManager:i MTKTool:d GSOFTGAME:v";
		if(args !=null && args.length>0)
			cmd = args[1];
		ADBLogger adblog = new ADBLogger(cmd);
		adblog.start();
	}

	public ADBLogger(LinkedList<String> queue, String cmd) {
		this.queue = queue;
		this.cmd = cmd;
		this.stop = false;
	}
	
	public ADBLogger(String cmd ){
		this.queue = new LinkedList<String>();
		this.cmd = cmd;
		this.stop = false;
		this.debug = true;
	}
	
	public void setStop(boolean stop) {
		this.stop = stop;
	}

	public void run() {
		Process process = null;
		BufferedReader reader = null;
		BufferedReader ereader = null;
		try {
			String line = null;
			queue.clear();
			while(!stop){
				log.info("### adb logcat start!");
				process = Runtime.getRuntime().exec(cmd);
				reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF8"));
				ereader = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF8"));
				while ((line = reader.readLine()) != null) {
					if(debug == true)
						System.out.println(line);
					line = line.trim();
					if (line.length() > 0 && line.length() > line.indexOf(":") + 1) {
						//log.info(line);
						queue.offer(line);
					}
				}
				while ((line = ereader.readLine()) != null) {
					// log.error(line);
				}
				log.info("### adb logcat lost!");
				process.getOutputStream().close();
				process.destroy();
				stop = true;
			}			
		} catch (Exception e) {
			log.error("logThread Error:", e);
		} finally {
			if (ereader != null) {
				try {
					ereader.close();
				} catch (IOException e) {
					log.error(e);
				}
				ereader = null;
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					log.error(e);
				}
				reader = null;
			}
			if (process != null) {
				process.destroy();
			}
		}
	}
}