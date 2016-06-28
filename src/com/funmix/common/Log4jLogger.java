package com.funmix.common;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.log4j.xml.DOMConfigurator;

import java.io.IOException;


/**
 * 通用 log4j 封装类
 * 外部类调用 Log4jLogger.getLogger() 即可
 */
public class Log4jLogger /* extends org.apache.log4j.Logger */ {
	public static final String DEFAULT_USER_WORKPATH = System.getProperty("user.dir");
	public static final String DEFAULT_XML_CONFIGURATION_FILE = "log4j.xml";
	public static final String DEFAULT_FILEAPPENDER_NAME = "Log.All.File";

	static {
		System.setProperty("WorkPath", DEFAULT_USER_WORKPATH);
		DOMConfigurator.configure(DEFAULT_USER_WORKPATH + "/" + DEFAULT_XML_CONFIGURATION_FILE);
	}

	/**
	 * 继承父类的构造函数
	 *
	 * @param sName
	 */
	//protected Log4jLogger(String sName)
	//{
	//    super(sName);
	//}
	protected Log4jLogger() {
	}

	public static Logger getLogger(Class<?> clsClass) {
		return LogManager.getLogger(clsClass.getName());
	}

	public static Logger getLogger(String sClassName) {
		return LogManager.getLogger(sClassName);
	}

	public static Logger getLogger(String sClassName, LoggerFactory lfLoggerFactory) {
		return LogManager.getLogger(sClassName, lfLoggerFactory);
	}

	public static Logger getRootLogger() {
		return LogManager.getRootLogger();
	}

	/**
	 * 设置 Log4j FileAppender 文件日志输出路径
	 *
	 * @param sAppenderName : Log4j.xml 中的 FileAppender 名称, 默认 DEFAULT_FILEAPPENDER_NAME
	 * @param sPathName     : 可采用 Log4jLogger.DEFAULT_USER_WORKPATH + "/app20091110.log" 的形式以使用相对路径
	 * @return boolean
	 */
	public static boolean setFileAppenderPath(String sAppenderName, String sPathName) {
		if (sAppenderName != null && !sAppenderName.trim().equals("") && sPathName != null && !sPathName.trim().equals("")) {
			sPathName = sPathName.trim();

			Appender l4jAppender = LogManager.getLoggerRepository().getRootLogger().getAppender(sAppenderName);
			if (l4jAppender != null && (l4jAppender instanceof FileAppender)) {
				FileAppender l4jFileAppender = (FileAppender) l4jAppender;
				l4jFileAppender.setFile(sPathName);
				l4jFileAppender.activateOptions();
				return true;
			}
		}
		return false;
	}

	/**
	 * 设置 Log4j FileAppender 文件日志输出路径
	 *
	 * @param sAppenderName : Log4j.xml 中的 FileAppender 名称, 默认 DEFAULT_FILEAPPENDER_NAME
	 * @param sPathName     : 可采用 Log4jLogger.DEFAULT_USER_WORKPATH + "/app20091110.log" 的形式以使用相对路径
	 * @param bAppend       : 日志是否为追加模式
	 * @return boolean
	 */
	public static boolean setFileAppenderPath(String sAppenderName, String sPathName, boolean bAppend) {
		if (sAppenderName != null && !sAppenderName.trim().equals("") && sPathName != null && !sPathName.trim().equals("")) {
			sPathName = sPathName.trim();

			Appender l4jAppender = LogManager.getLoggerRepository().getRootLogger().getAppender(sAppenderName);
			if (l4jAppender != null && (l4jAppender instanceof FileAppender)) {
				FileAppender l4jFileAppender = (FileAppender) l4jAppender;
				try {
					l4jFileAppender.setFile(sPathName, bAppend, l4jFileAppender.getBufferedIO(), l4jFileAppender.getBufferSize());
				}
				catch (IOException ioex) {
					LogLog.error("Log4jLogger.setFileAppenderPath() : PathName = '" + sPathName + "', Append = " + String.valueOf(bAppend) + ", IOException !", ioex);
					return false;
				}
				l4jFileAppender.activateOptions();
				return true;
			}
		}
		return false;
	}
}
