package com.funmmix.app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.apache.log4j.Logger;
import com.funmix.common.DBMgr;
import com.funmix.common.Log4jLogger;

public class ParsePhoneInfo {

	protected static Logger	log	= Log4jLogger.getLogger(ParsePhoneInfo.class);

	public static void main(String[] args) {
		try {
			Connection conn = DBMgr.getCon("helper");
			PreparedStatement stmt = conn.prepareStatement("select info,id from tauthdata_20151222 where id > ? order by id limit 1000 ");
			PreparedStatement instmt = conn.prepareStatement("insert into tmobstore values (?,?,?,?,?,?,?,?,?,?,?,?)");
			String info = null;
			String tmp[];
			ResultSet rs = null;
			int total = 0;
			int count = 0;
			int start = 0;
			long st;			
			do {
				st = System.currentTimeMillis();	
				count = 0;
				stmt.setInt(1, start);
				rs = stmt.executeQuery();
				while (rs.next()) {
					info = rs.getString(1);
					try {
						count++;
						start = rs.getInt(2);
						tmp = info.split(";");
						if (tmp.length == 15) {
							instmt.setString(1, tmp[12]);
							instmt.setString(2, tmp[1]);
							instmt.setString(3, tmp[2]);
							instmt.setString(4, tmp[3]);
							instmt.setString(5, tmp[4]);
							instmt.setString(6, tmp[5]);
							instmt.setString(7, tmp[6]);
							instmt.setString(8, tmp[7]);
							instmt.setString(9, tmp[9]);
							instmt.setString(10, tmp[8]);
							instmt.setString(11, tmp[10]);
							instmt.setString(12, tmp[11]);
							// log.info(instmt.toString());
							instmt.executeUpdate();
						}
					} catch (Exception e) {
						log.error(e);
						log.info(instmt.toString());
					}
				}
				total = total + count;
				log.info("Used:" + ( System.currentTimeMillis()-st) + " ms,lastid:" + start + ",total processed:" + total);
			} while (count > 0);
		} catch (Exception e) {
			log.error(e);
		}
	}

}
