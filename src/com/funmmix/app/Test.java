package com.funmmix.app;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.xml.sax.InputSource;

import com.funmix.common.DBMgr;
import com.funmix.model.Device;
import com.funmix.model.FakePhone;
import com.funmix.utils.ADBUtils;
import com.funmix.utils.FakeUtils;
import com.funmix.utils.TypeUtil;
import com.funmix.utils.Utils;
import com.tencent.msdk.tea.Base64;

public class Test {

	public static void main(String[] args) {
		// System.out.println(Utils.genAndrodid());
		// fixWIFI(args[0]);
		// addDevices();
		// clearReport();
		startMTKTools(args[0]);
		// startMTKTools("70");
		// genIpconfigs();
		// genReport();
		// fixreporttrue();
		// genIpconfigs();
		// calRemainRate();
		// genFBZYAccount();
		// fixFBTenddata();		
		//genGameInfo(13);
		//parseAPK("kjyshw");
		//parseAPK("ksjsbbg");
		//parseAPK("xcmbd");
		//genGameInfo(34);
		//genGameList();
		/*for(int i=13;i<=18;i++)
			genRerunScript(i);*/
		//System.out.println(getDevfromIP("192.169.33.104"));
		/*for(int i=1;i<=28;i++){
			//genStopScript(i);
			genRerunScript(i);
		}*/
		/*genStopScript(34);
		genRerunScript(34);*/
	}
	
	private static int getDevfromIP(String ip){
		String[] tmps=ip.split("\\.");
		//System.out.println(TypeUtil.typeToString("", tmps));
		int id = (Integer.parseInt(tmps[2])-30)*100 +Integer.parseInt(tmps[3])-100; 
		return id;
	}
	
	private static void genGameList() {
		try {
			Connection conn = DBMgr.getCon("helper");
			PreparedStatement stmt = conn.prepareStatement("select game,cid,chid,worker,activity from tcmcctask where game<>''   order by game");
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				System.out.println(rs.getString(2) + "-" + rs.getString(3) + "\t" + rs.getString(1) + "\t" + rs.getString(4) + "\t" + rs.getString(5) + "");;
			}
			DbUtils.closeQuietly(conn);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	private static void genStopScript(int id) {
		try {
			Connection conn = DBMgr.getCon("helper");
			PreparedStatement stmt = conn.prepareStatement("select worker,activity from tcmcctask where id = ? and game<>''");
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				System.out.println("./stopapp.sh " + rs.getString(1) + " " + rs.getString(2).substring(0,rs.getString(2).indexOf("/")) + " >> rerun.log ");;
			}
			DbUtils.closeQuietly(conn);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	private static void genRerunScript(int id) {
		try {
			Connection conn = DBMgr.getCon("helper");
			PreparedStatement stmt = conn.prepareStatement("select worker,activity from tcmcctask where id = ? and game<>''");
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				System.out.println("./startapp.sh " + rs.getString(1) + " " + rs.getString(2) + " >> rerun.log &");;
			}
			DbUtils.closeQuietly(conn);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	private static void genGameInfo(int id) {
		try {
			Connection conn = DBMgr.getCon("helper");
			PreparedStatement stmt = conn.prepareStatement("select game,cid,chid,cpid,filename from tcmcctask where id = ?");
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				System.out.println(rs.getString(5));
				System.out.println("CID:" + rs.getString(2));
				System.out.println("CHID:" + rs.getString(3));
				System.out.println("CPID:" + rs.getString(4));
			}
			DbUtils.closeQuietly(conn);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	private static void parseAPK(String apkname){
		String CHARGE_FILENAME = "Charge.xml";
		String CONSUMECODEINFO_FILENAME = "ConsumeCodeInfo.xml";
		PublicKey publicKey = getPublicKeyFromX509();
		try {
			byte[] decryptChargeString = Utils.getFileContent("D:\\" + apkname + "\\assets\\" + CHARGE_FILENAME);
			byte[] decryptConsumeString = Utils.getFileContent("D:\\" + apkname + "\\assets\\" + CONSUMECODEINFO_FILENAME);
			byte[] realConsume = decryptData(publicKey, decryptConsumeString);
			byte[] realCharge = decryptData(publicKey, decryptChargeString);
			String consumeXml = new String(realConsume,"UTF8");
			System.out.println(consumeXml);			
			String ChargeXML = new String(realCharge,"UTF8");
			//System.out.println(ChargeXML);
			String cid,chid,cpid,pkg,activity;
			int pos,pos1;
			pos = ChargeXML.indexOf("<USR-TB-CID>");
			pos1 = ChargeXML.indexOf("</USR-TB-CID>");
			cid = ChargeXML.substring(pos+"<USR-TB-CID>".length(),pos1);
			pos = ChargeXML.indexOf("<USR-TB-CHID>");
			pos1 = ChargeXML.indexOf("</USR-TB-CHID>");
			chid = ChargeXML.substring(pos+"<USR-TB-CHID>".length(),pos1);
			pos = ChargeXML.indexOf("<USR-TB-CPID>");
			pos1 = ChargeXML.indexOf("</USR-TB-CPID>");
			cpid = ChargeXML.substring(pos+"<USR-TB-CPID>".length(),pos1);
			System.out.println(cid + "," + chid + "," + cpid);
			/*String AndroidManifest = new String(Utils.getFileContent("D:\\" + apkname + "\\AndroidManifest"),"UTF8");
			pos = AndroidManifest.indexOf("package=\"");
			pos1 = AndroidManifest.indexOf("\">",pos);
			pkg = AndroidManifest.substring(pos +"package=\"".length(),pos1);
			pos = AndroidManifest.indexOf("<action android:name=\"android.intent.action.MAIN\"/>");
			pos = AndroidManifest.indexOf("",pos);*/
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static PublicKey getPublicKeyFromX509() {
		try {
			String CERT = "MIICITCCAYqgAwIBAgIEUlUcDDANBgkqhkiG9w0BAQUFADBUMQswCQYDVQQGEwJDTjELMAkGA1UECBMCSlMxCzAJBgNVBAcTAk5KMQ0wCwYDVQQKEwRjbWNjMQ0wCwYDVQQLEwRjbWNjMQ0wCwYDVQQDEwRjbWNjMCAXDTEzMTAwOTA5MDQxMloYDzIxMTMwOTE1MDkwNDEyWjBUMQswCQYDVQQGEwJDTjELMAkGA1UECBMCSlMxCzAJBgNVBAcTAk5KMQ0wCwYDVQQKEwRjbWNjMQ0wCwYDVQQLEwRjbWNjMQ0wCwYDVQQDEwRjbWNjMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCByTi+HI6nYLJGWQPDhTmoR1kjatJVapS5Ic3L/ht6CCTzgIwywu7tOnwDR1LXSy9KoEOHuTigmgnVlNYFTPpzqq+R4lPsmBThVf54wHK3L2Xjz2AqtFj7IcAacPrKZ5/Q4WXPsizDxcA3spWwqvQXkyEjwxg44KQuC0Oavpd84wIDAQABMA0GCSqGSIb3DQEBBQUAA4GBAGQg1Qs6KVU1adTwdJ9rLIqnxyy9XlA8QpEvI5n9AR6pcS7Skkplga1bmA+qmwek569fGvZ7XRhwpVTD/FaPjTJD7LD2rQ8UadUSaQG8c1PiXmjTU3beo3a441aPeA6DBkXo6ZNWTTbD6gW9JGjMcKNYI/22ep6Wg2bkoBDY534Y";
			byte[] data = Base64.decode(CERT, Base64.DEFAULT);
			InputStream in = new ByteArrayInputStream(data);
			CertificateFactory f;

			f = CertificateFactory.getInstance("X.509");
			X509Certificate certificate = (X509Certificate) f.generateCertificate(in);
			return certificate.getPublicKey();
		} catch (CertificateException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static byte[] decryptData(PublicKey publicKey, byte[] cipherData) throws Exception {
		try {
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(2, publicKey);
			int inputLen = cipherData.length;
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int offSet = 0;

			int i = 0;
			while (inputLen - offSet > 0) {
				byte[] cache;
				if (inputLen - offSet > 128)
					cache = cipher.doFinal(cipherData, offSet, 128);
				else {
					cache = cipher.doFinal(cipherData, offSet, inputLen - offSet);
				}
				out.write(cache, 0, cache.length);
				i++;
				offSet = i * 128;
			}
			byte[] decryptedData = out.toByteArray();
			out.close();
			return decryptedData;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static void checkModel() {
		try {
			Connection conn = DBMgr.getCon("helper");
			PreparedStatement stmt = conn.prepareStatement("select model,count(*) from tmobstore where model= ? and used=0");
			FakeUtils fake = new FakeUtils();
			for (int i = 0; i < 18; i++) {
				stmt.setString(1, fake.getFakePhone(i, 0).getModel());
				ResultSet rs = stmt.executeQuery();
				if (rs.next())
					System.out.println(rs.getString(1) + ":" + rs.getString(2));
			}
			DbUtils.closeQuietly(conn);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private static void fixFBTenddata() {
		try {
			Connection conn = DBMgr.getCon("helper");
			PreparedStatement stmt = conn.prepareStatement("select id,phone,ver from taccount_fbzy where imsi is null limit 1");
			PreparedStatement upstmt = conn.prepareStatement("update taccount_fbzy set imsi=?,simserial=?,serialno=? where id = ? ");
			ResultSet rs = stmt.executeQuery();
			FakeUtils fake = new FakeUtils();
			while (rs.next()) {
				upstmt.setString(1, Utils.genImsi());
				upstmt.setString(2, Utils.genSimserial());
				upstmt.setString(3, Utils.genSerialno(fake.getFakePhone(rs.getInt(2), rs.getInt(3)).getModel()));
				upstmt.setString(4, rs.getString(1));
				upstmt.executeUpdate();
				rs = stmt.executeQuery();
			}
			DbUtils.closeQuietly(conn);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private static void genFBZYAccount() {
		String areas[] = new String[] { "GUD", "JSU", "ZHJ", "SHD", "HUB", "HEB", "HEN", "SCH", "FUJ" };
		int pers[] = new int[] { 4, 3, 3, 2, 2, 2, 1, 2, 1 };
		int total = 6000;
		FakeUtils fake = new FakeUtils();
		String area;
		int count;
		FakePhone phone;
		ResultSet rs;
		int pid, ver;
		try {
			Connection conn = DBMgr.getCon("helper");
			PreparedStatement stmt = conn.prepareStatement("select imei,mac from tmobstore where model = ? and used=0 limit 1");
			PreparedStatement stmt1 = conn.prepareStatement("select account,passwd from tucstore where status=0 limit 1");
			PreparedStatement stmt2 = conn.prepareStatement("select count(*) from tucstore where status=0");
			PreparedStatement instmt = conn.prepareStatement("insert into taccount_fbzy (account,passwd,area,imei,phone,ver,mac,androidid) values (?,?,?,?,?,?,?,?)");
			PreparedStatement upstmt1 = conn.prepareStatement("update tucstore set status=1 where account = ?");
			PreparedStatement upstmt2 = conn.prepareStatement("update tmobstore set used=1 where imei = ?");
			rs = stmt2.executeQuery();
			if (rs.next())
				total = rs.getInt(1);
			for (int i = 0; i < areas.length; i++) {
				area = areas[i];
				count = total * pers[i] / 20;
				for (int j = 0; j < count; j++) {
					rs = stmt1.executeQuery();
					if (rs.next()) {
						instmt.setString(1, rs.getString(1));
						instmt.setString(2, rs.getString(2));
						upstmt1.setString(1, rs.getString(1));
					} else {
						count = -1;
						System.out.println("All account be used!");
						break;
					}
					instmt.setString(3, area);
					pid = Utils.getRandom(0, 18);
					ver = Utils.getRandom(0, 1);
					phone = fake.getFakePhone(pid, ver);
					stmt.setString(1, phone.getModel());
					rs = stmt.executeQuery();
					if (rs.next()) {
						instmt.setString(4, rs.getString(1));
						instmt.setInt(5, pid);
						instmt.setInt(6, ver);
						instmt.setString(7, rs.getString(2));
						upstmt2.setString(1, rs.getString(1));
					} else {
						count = -1;
						System.out.println("All " + phone.getModel() + " be used!");
						continue;
					}
					instmt.setString(8, Utils.genAndrodid());
					instmt.executeUpdate();
					upstmt1.executeUpdate();
					upstmt2.executeUpdate();
				}
				if (count == -1)
					break;
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private static void calRemainRate() {
		try {
			Connection conn = DBMgr.getCon("helper");
			QueryRunner run = new QueryRunner();
			String checkdate = "2015-10-27";
			String sql = "";
			int spec;
			int fnum[][] = { { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } };
			for (int i = 0; i < 14; i++) {
				sql = sql + Utils.logincol[i] + ",";
			}
			sql = "select " + sql + "countday from treportp where platformid=";
			List<Map<String, Object>> maps;
			double rate;
			for (int k = 0; k < 20; k++) {
				for (int l = 2; l <= 2; l++) {
					for (int i = 0; i <= 2; i++) {
						for (int j = 0; j < 14; j++) {
							fnum[i][j] = 0;
						}
					}
					maps = run.query(conn, "select spec,count(*) as count from tdralog_20151115 where status>=6 and to_days(start)=to_days('2015-10-27') +" + k + " group by spec",
							new MapListHandler());
					for (Map<String, Object> map : maps) {
						spec = Integer.parseInt(map.get("spec").toString());
						fnum[spec / 100][spec % 100] = Integer.parseInt(map.get("count").toString());
					}
					Map<String, Object> map = run.query(conn, sql + l + " and to_days(countday)=to_days('2015-10-27') +" + k, new MapHandler());
					System.out.print(l + ":\t");
					for (int i = 1; i < 14; i++) {
						// System.out.print(map.get(Utils.logincol[i]));
						rate = Math.round(
								(Double.parseDouble(map.get(Utils.logincol[i]).toString()) - fnum[l][i]) / Double.parseDouble(map.get(Utils.logincol[0]).toString()) * 1000) / 10.0;
						System.out.print(rate + "\t");
					}
					System.out.println();
				}
			}
		} catch (Exception e) {
			System.out.println("error:" + e.getMessage());
		}
	}

	private static void fakeAPP(String id) {
		try {
			Connection conn = DBMgr.getCon("helper");
			QueryRunner runner = new QueryRunner();
			Device dev = runner.query(conn, "select * from tdevice where id=" + id, new BeanHandler<Device>(Device.class));
			ADBUtils adb;
			LinkedList<String> queue = new LinkedList<String>();
			String cmd = "adb -s " + dev.getDevice() + " logcat -s ActivityManager:i MTKTool:d";
			adb = new ADBUtils(dev, queue, conn);
			ADBLogger adblog = new ADBLogger(adb.getQueue(), cmd);
			adblog.start();
			Utils.sleep(2000);
			// adb.reRwritePhoneInfo();
			adb.clearAppData("com.shangchang.mq");
			Utils.sleep(2000);
			adb.reRwritePhoneInfo(randIMEI(), -1, -1);
			adblog.setStop(true);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	private static void fixPayExpire() {
		int lastid = 0;
		ResultSet rs, ars;
		try {
			Connection conn = DBMgr.getCon("helper");
			String sql = "select id,openid,pay_token from tauthdata where id > ? limit 500";
			PreparedStatement stmta = conn.prepareStatement(sql);
			PreparedStatement stmt = conn.prepareStatement("select min(subtime),pfkey from tpostlog where openid= ? and paytoken = ?");
			PreparedStatement upstmt = conn.prepareStatement("update tauthdata set payexpire = from_unixtime(?),pf_key=? where id = ?");
			long start = System.currentTimeMillis();

			while (lastid < 3274101) {
				stmta.setInt(1, lastid);
				ars = stmta.executeQuery();

				while (ars.next()) {
					lastid = ars.getInt(1);
					stmt.setString(1, ars.getString(2));
					stmt.setString(2, ars.getString(3));
					rs = stmt.executeQuery();
					if (rs.next()) {
						upstmt.setString(1, rs.getString(1));
						upstmt.setString(2, rs.getString(2));
						upstmt.setString(3, ars.getString(1));
						// System.out.println(upstmt.toString());
						upstmt.executeUpdate();
					}
				}

				System.out.println("process reach:" + lastid + ",used:" + (System.currentTimeMillis() - start) + " ms.");
				start = System.currentTimeMillis();
			}
		} catch (Exception e) {
			System.out.println("error:" + e.getMessage());
		}
	}

	private static void fixReporta() {
		long start = System.currentTimeMillis();
		List<Map<String, Object>> maplist;
		String countday = null, area;
		int total, platform;
		int backday = 0;
		String logincol[] = Utils.logincol;
		try {
			Connection conn = DBMgr.getCon("helper");
			PreparedStatement stmt;
			PreparedStatement upstmtarea;
			PreparedStatement upstmtp;
			PreparedStatement upstmt;
			ResultSet rs;
			// for (backday = 38; backday >= 7; backday--)
			for (int i = 0; i < 21; i++) {
				String sql = "select area,platformid," + logincol[i] + "- interval " + i + " day as countday,count(*) as total from tauthdata " + "where  to_days(" + logincol[i]
						+ ")=to_days(now())-" + backday + " and " + logincol[i] + " is not null  group by area,platformid,to_days(" + logincol[i] + ")";
				// System.out.println(sql);
				stmt = conn.prepareStatement(sql);
				rs = stmt.executeQuery();
				int qq = 0, wx = 0;
				sql = "update treporta set " + logincol[i] + "=? where to_days(countday)=to_days(?) and platformid=? and area=?";
				upstmtarea = conn.prepareStatement(sql);
				sql = "update treport set " + logincol[i] + "=? where to_days(countday)=to_days(?)";
				upstmt = conn.prepareStatement(sql);
				sql = "update treportp set " + logincol[i] + "=? where to_days(countday)=to_days(?) and platformid= ?";
				upstmtp = conn.prepareStatement(sql);
				while (rs.next()) {
					area = rs.getString(1);
					platform = rs.getInt(2);
					countday = rs.getString(3);
					total = rs.getInt(4);
					if (platform == 1) {
						wx = wx + total;
					} else if (platform == 2) {
						qq = qq + total;
					}
					if (area == null || area.toLowerCase().equals("null")) {
						continue;
					} else {
						upstmtarea.setInt(1, total);
						upstmtarea.setString(2, countday);
						upstmtarea.setInt(3, platform);
						upstmtarea.setString(4, area);
						upstmtarea.executeUpdate();
					}
				}
				upstmtp.setInt(1, wx);
				upstmtp.setString(2, countday);
				upstmtp.setInt(3, 1);
				upstmtp.executeUpdate();

				upstmtp.setInt(1, qq);
				upstmtp.setString(2, countday);
				upstmtp.setInt(3, 2);
				upstmtp.executeUpdate();

				upstmt.setInt(1, qq + wx);
				upstmt.setString(2, countday);
				upstmt.executeUpdate();
			}
			System.out.println(System.currentTimeMillis() - start);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private static void fixreporttrue() {
		Connection conn = DBMgr.getCon("helper");
		QueryRunner run = new QueryRunner();
		try {
			String sql = "select distinct keyid from tdralog where status>=6 and to_days(lastupdate)<to_days(now())";
			sql = "select openid from tauthdata where id";

			sql = "select to_days(_1st) as day,platformid,count(*) as count from tauthdata where _7count>=2 group by to_days(_1st),platformid";
			List<Map<String, Object>> maps = run.query(conn, sql, new MapListHandler());
			for (Map<String, Object> map : maps) {
				if (map.get("day") != null) {
					sql = "update treportp set _2count = " + map.get("count").toString() + " where platformid=" + map.get("platformid").toString() + " and to_days(countday)="
							+ map.get("day").toString();
					run.update(conn, sql);
				}
			}
			sql = "select to_days(_1st) as day,platformid,count(*) as count from tauthdata where _7count>=3 group by to_days(_1st),platformid";
			maps = run.query(conn, sql, new MapListHandler());
			for (Map<String, Object> map : maps) {
				if (map.get("day") != null) {
					sql = "update treportp set _7count = " + map.get("count") + " where platformid=" + map.get("platformid") + " and to_days(countday)=" + map.get("day");
					run.update(conn, sql);
				}
			}
			sql = "select to_days(_1st) as day,platformid,count(*) as count from tauthdata where _7count>=3 and _14count>=1 group by to_days(_1st),platformid";
			maps = run.query(conn, sql, new MapListHandler());
			for (Map<String, Object> map : maps) {
				if (map.get("day") != null) {
					sql = "update treportp set _14count = " + map.get("count") + " where platformid=" + map.get("platformid") + " and to_days(countday)=" + map.get("day");
					run.update(conn, sql);
				}
			}

			/*
			 * sql =
			 * "select to_days(_1st) as day,count(*) as count from tauthdata where _7count>=2 group by to_days(_1st)"
			 * ; maps = run.query(conn,sql,new MapListHandler());
			 * for(Map<String,Object> map:maps){ if(map.get("day")!=null){ sql =
			 * "update treport set _2count = " + map.get("count").toString() +
			 * " where  to_days(countday)=" + map.get("day").toString();
			 * run.update(conn,sql); } } sql =
			 * "select to_days(_1st) as day,count(*) as count from tauthdata where _7count>=3 group by to_days(_1st)"
			 * ; maps = run.query(conn,sql,new MapListHandler());
			 * for(Map<String,Object> map:maps){ if(map.get("day")!=null){ sql =
			 * "update treport set _7count = " + map.get("count") +
			 * " where to_days(countday)=" + map.get("day");
			 * run.update(conn,sql); } } sql =
			 * "select to_days(_1st) as day,count(*) as count from tauthdata where _7count>=3 and _14count>=1 group by to_days(_1st)"
			 * ; maps = run.query(conn,sql,new MapListHandler());
			 * for(Map<String,Object> map:maps){ if(map.get("day")!=null){ sql =
			 * "update treport set _14count = " + map.get("count") +
			 * " where to_days(countday)=" + map.get("day");
			 * run.update(conn,sql); } }
			 */

		} catch (Exception e) {
			System.out.println("error:" + e.getMessage());
		}
	}

	private static void genReport() {
		try {
			Connection conn = DBMgr.getCon("helper");
			QueryRunner run = new QueryRunner();
			/*
			 * run.update(conn,"delete from treporta"); run.update(conn,
			 * "delete from treport"); run.update(conn,"delete from treportp");
			 * run.update(conn,"delete from treport_true"); run.update(conn,
			 * "delete from treportp_true");
			 */

			for (int i = 0; i < 31; i++) {
				run.update(conn, "insert into treport (countday) values ('2016-04-03' + interval " + i + " day)");
				run.update(conn, "insert into treport_true (countday) values ('2016-04-03' + interval " + i + " day)");
				run.update(conn, "insert into treports (platformid,countday) values (1,'2016-04-03' + interval " + i + " day)");
				run.update(conn, "insert into treports (platformid,countday) values (2,'2016-04-03' + interval " + i + " day)");
				run.update(conn, "insert into treportp (platformid,countday) values (1,'2016-04-03' + interval " + i + " day)");
				run.update(conn, "insert into treportp (platformid,countday) values (2,'2016-04-03' + interval " + i + " day)");
				run.update(conn, "insert into treportp_true (platformid,countday) values (1,'2016-04-03' + interval " + i + " day)");
				run.update(conn, "insert into treportp_true (platformid,countday) values (2,'2016-04-03' + interval " + i + " day)");

				for (int j = 0; j < Utils.areas.length; j++) {
					run.update(conn, "insert into treporta (area,platformid,countday) values ('" + Utils.areas[j][1] + "',1,'2016-04-03' + interval " + i + " day)");
					run.update(conn, "insert into treporta (area,platformid,countday) values ('" + Utils.areas[j][1] + "',2,'2016-04-03' + interval " + i + " day)");
				}
			}
		} catch (Exception e) {
			System.out.println("error:" + e.getMessage());
		}
	}

	private static void genIpconfigs() {
		File ipFile = null;
		ipFile = new File("d://ipconfig//ipconfig_x.txt");
		InputStream in = null;
		/*
		 * for(int k=1;k<=99;k++){ System.out.println(k); try { String devip =
		 * "" + k; if (k < 10) { devip = "0" + devip; }
		 * 
		 * in = new FileInputStream(ipFile); FileOutputStream outFile = new
		 * FileOutputStream(new File("d://ipconfig//ipconfig" + devip +
		 * ".txt")); int tempbyte; int i = 0; while ((tempbyte = in.read()) !=
		 * -1) { if (i == 53) { outFile.write(devip.substring(0, 1).getBytes());
		 * } else if (i == 54) { outFile.write(devip.substring(1,
		 * 2).getBytes()); } else { outFile.write(tempbyte); } i++; }
		 * outFile.close(); in.close(); } catch (IOException e) {
		 * System.out.println(e.getMessage()); } }
		 * 
		 * ipFile = new File("d://ipconfig//ipconfig_x1.txt"); for(int
		 * k=100;k<=199;k++){ System.out.println(k); try { in = new
		 * FileInputStream(ipFile); FileOutputStream outFile = new
		 * FileOutputStream(new File("d://ipconfig//ipconfig" + k + ".txt"));
		 * int tempbyte; int i = 0; String devip = "" + (k-100); if ((k-100)<
		 * 10) { devip = "0" + devip; } while ((tempbyte = in.read()) != -1) {
		 * if (i == 53) { outFile.write(devip.substring(0, 1).getBytes()); }
		 * else if (i == 54) { outFile.write(devip.substring(1, 2).getBytes());
		 * } else { outFile.write(tempbyte); } i++; } outFile.close();
		 * in.close(); } catch (IOException e) {
		 * System.out.println(e.getMessage()); } }
		 */

		ipFile = new File("d://ipconfig//ipconfig_x5.txt");
		for (int k = 500; k <= 599; k++) {
			System.out.println(k);
			try {
				in = new FileInputStream(ipFile);
				FileOutputStream outFile = new FileOutputStream(new File("d://ipconfig//ipconfig" + k + ".txt"));
				int tempbyte;
				int i = 0;
				String devip = "" + (k - 500);
				if ((k - 500) < 10) {
					devip = "0" + devip;
				}
				while ((tempbyte = in.read()) != -1) {
					if (i == 53) {
						outFile.write(devip.substring(0, 1).getBytes());
					} else if (i == 54) {
						outFile.write(devip.substring(1, 2).getBytes());
					} else {
						outFile.write(tempbyte);
					}
					i++;
				}
				outFile.close();
				in.close();
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	private static void clearReport() {
		String sql = "";
		try {
			Connection conn = DBMgr.getCon("helper");
			for (int i = 0; i < 21; i++) {
				sql = sql + Utils.logincol[i] + " = 0,";
			}
			sql = sql.substring(0, sql.length() - 1);
			QueryRunner run = new QueryRunner();
			run.update(conn, "update treporta set " + sql);
			run.update(conn, "update treportp set " + sql);
			run.update(conn, "update treportp_true set " + sql);
			run.update(conn, "update treport set _2count=0,_7count=0,_14count=0," + sql);
			run.update(conn, "update treport_true set _2count=0,_7count=0,_14count=0," + sql);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private static void fixWIFI(String id) {
		try {
			Connection conn = DBMgr.getCon("helper");
			QueryRunner runner = new QueryRunner();
			Device dev = runner.query(conn, "select * from tdevice where id=" + id, new BeanHandler<Device>(Device.class));
			ADBUtils adb;
			LinkedList<String> queue = new LinkedList<String>();
			adb = new ADBUtils(dev, queue, conn);
			adb.execADB("push ipconfig" + id + ".txt /data/misc/wifi/ipconfig.txt");
			adb.execADB("shell chown system.wifi /data/misc/wifi/ipconfig.txt");
			adb.execADB("shell svc wifi disable");
			adb.execADB("shell svc wifi enable");
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	private static void startMTKTools(String id) {
		try {
			Connection conn = DBMgr.getCon("helper");
			QueryRunner runner = new QueryRunner();
			Device dev = runner.query(conn, "select * from tdevice where id=" + id, new BeanHandler<Device>(Device.class));
			ADBUtils adb;
			LinkedList<String> queue = new LinkedList<String>();
			String cmd = "adb -s " + dev.getDevice() + " shell logcat |grep 'Activity' ";
			adb = new ADBUtils(dev, queue, conn);
			ADBLogger adblog = new ADBLogger(adb.getQueue(), cmd);
			adblog.start();			
			adb.reRwritePhoneInfo();
			// adb.reRwritePhoneInfo("860968054563449",10,1);
			adblog.setStop(true);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public static String randIMEI() {
		return "860" + Utils.getRandom(1000, 9999) + "" + Utils.getRandom(1000, 9999) + "" + Utils.getRandom(1000, 9999);
	}

	private static void addDevices() {
		try {
			Connection conn = DBMgr.getCon("helper");
			PreparedStatement stmt = conn.prepareStatement("insert into tdevice (id,device) values (?,concat('E3CD20',?))");
			for (int i = 241; i <= 600; i++) {
				stmt.setInt(1, i);
				stmt.setString(2, "" + i);
				if (i < 10)
					stmt.setString(2, "0" + i);
				stmt.executeUpdate();
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
