package com.funmix.common;
import java.sql.Connection;
import java.util.HashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

public class DBMgr {
	
    static HashMap<String,DataSource> poolmap = new HashMap<String,DataSource>();

    //private static String defaultDsname = "service";
    public static Connection getCon(String dsName) {
        //dsName= "servicelocal";
        Connection con = null;
        try {
        	DataSource pool = poolmap.get(dsName);
            if (pool == null) {
            	Context env = null;
                try{
                	env = (Context) new InitialContext().lookup("java:comp/env");
                }catch(Exception envError){            	
                	//System.out.println(envError.getMessage());
                }
                if(env == null){ //数据源不存在
                	BasicDataSource bds = new BasicDataSource();
                	bds.setUrl("jdbc:mysql://192.168.99.10:3306/" + dsName + "?characterEncoding=UTF-8");
                	//bds.setUrl("jdbc:mysql://localhost:3306/" + dsName + "?characterEncoding=UTF-8");
                    bds.setDriverClassName("com.mysql.jdbc.Driver");  
                    bds.setUsername("root");  
                    bds.setPassword("funmix"); 
                    pool = (DataSource)bds;
                }else{
                	pool = (DataSource) env.lookup("jdbc/" + dsName.trim());
                }
                System.out.println("数据源:" + dsName);                
                poolmap.put(dsName,pool);
            }
            con = pool.getConnection();
        } catch (Exception e) {
            System.out.println("DBMgr getCon error:" +  e.getMessage());
        }
        return con;
    }
    
    public static void main(String[] args) {
    	System.out.println(getCon("helper"));
	}
    
}