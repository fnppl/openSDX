package org.fnppl.dbaccess;
/*
 * Copyright (C) 2010-2013
 * 							fine people e.V. <opensdx@fnppl.org> 
 * 							Henning Thieß <ht@fnppl.org>
 * 
 * 							http://fnppl.org
*/

/*
 * Software license
 *
 * As far as this file or parts of this file is/are software, rather than documentation, this software-license applies / shall be applied.
 *  
 * This file is part of openSDX
 * openSDX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * openSDX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * and GNU General Public License along with openSDX.
 * If not, see <http://www.gnu.org/licenses/>.
 *      
 */

/*
 * Documentation license
 * 
 * As far as this file or parts of this file is/are documentation, rather than software, this documentation-license applies / shall be applied.
 * 
 * This file is part of openSDX.
 * Permission is granted to copy, distribute and/or modify this document 
 * under the terms of the GNU Free Documentation License, Version 1.3 
 * or any later version published by the Free Software Foundation; 
 * with no Invariant Sections, no Front-Cover Texts, and no Back-Cover Texts. 
 * A copy of the license is included in the section entitled "GNU 
 * Free Documentation License" resp. in the file called "FDL.txt".
 * 
 */
import java.util.*;
import java.io.*;

import org.jdom.*;

import org.apache.commons.dbcp.cpdsadapter.*;
import org.apache.commons.dbcp.datasources.*;

import java.sql.Connection;


//import PooledConnection;

//INCLUDE IN CLASSPATH for compile/run
 //commons-pool-1.3.jar commons-dbcp-1.2.2.jar commons-collections-3.2.jar

public class BalancingConnectionManager {
    private static BalancingConnectionManager instance;
    public static boolean echoSql = false;
    
    private SharedPoolDataSource ds;
    
    public void shutdown() {
    	try {
    		ds.close();
    	} catch(Exception ex) {
//    		ex.printStackTrace();
    	}
    }
    
    private BalancingConnectionManager() {
    }
    public static void init(
    		String drivermanager, 
    		String dbserver, 
    		int dbport, 
    		String dbname,
    		String dbdbname, 
    		String dbusername, 
    		String dbpassword, 
    		int initialconnections, 
    		int maxconnections
    	) {

    	if(instance == null) {
            instance = new BalancingConnectionManager();
            
                         
            Element e = new Element("pgpool");
            e.addContent((new Element("drivermanager")).setText(drivermanager));
            e.addContent((new Element("dbserver")).setText(dbserver));
            e.addContent((new Element("dbport")).setText(""+dbport));
            e.addContent((new Element("dbdbname")).setText(dbdbname));
            e.addContent((new Element("dbusername")).setText(dbusername));
            e.addContent((new Element("dbpassword")).setText(dbpassword));
            e.addContent((new Element("dbname")).setText(dbname));
            e.addContent((new Element("initialconnections")).setText(""+initialconnections));
            e.addContent((new Element("maxconnections")).setText(""+maxconnections));
            
            try {
            	instance.createPool(e, false, maxconnections);
            }catch(Exception ex) {
            	ex.printStackTrace();
            }            
        }
    }
      
    private Vector confs = new Vector();
    public static String hostname = null;
    
    public void createPool(Element config, boolean local, int limitconns) throws Exception {
        String drivermanager = config.getChildText("drivermanager");
        String dbserver = local ? config.getChildText("dblocal") : config.getChildText("dbserver");
        
//TODO
//        System.err.println("hostname: "+hostname);
//        if((System.getProperty("isrzdb")!=null && System.getProperty("isrzdb").equals("true")) || hostname.indexOf("finestblade")>=0  || hostname.equals("fineblade2.finetunes.net") || hostname.equals("finestblade.finetunes.net") || hostname.equals("raidblade2.finetunes.net") ) {
//            dbserver = config.getChildText("dbserver");
//        }
        
        String dbdbname=config.getChildText("dbdbname");
        String dbusername=config.getChildText("dbusername");
        String dbpassword=config.getChildText("dbpassword");
        int dbport = Integer.parseInt(config.getChildText("dbport"));
        String dbname=config.getChildText("dbname");
        String initialConnections=config.getChildText("initialconnections");
        int maxConnections=Integer.parseInt(config.getChildText("maxconnections"));

        initialConnections = "1";
        
        Properties prop = new Properties();
        
        System.err.println("Trying to init connectionManager limitcons: "+limitconns+" dbserver: "+dbserver);
        prop.setProperty("persistence.ConnectionURL", "jdbc:"+dbname+"://"+dbserver + ":"+dbport +"/"+dbdbname+"?charSet=UTF8");
        
        prop.setProperty("persistence.DbUsername", dbusername);
        prop.setProperty("persistence.DbPassword", dbpassword);
        prop.setProperty("persistence.NumberInitialConnections", initialConnections);
        prop.setProperty("persistence.NumberMaxConnections", ""+maxConnections);
            
        System.out.print(prop.toString());            
        
        DriverAdapterCPDS cpds = new DriverAdapterCPDS();
        cpds.setDriver(drivermanager);
        cpds.setUrl(prop.get("persistence.ConnectionURL").toString());
        cpds.setUser(prop.get("persistence.DbUsername").toString());
        cpds.setPassword(prop.get("persistence.DbPassword").toString());


        SharedPoolDataSource tds = new SharedPoolDataSource();
        

//        tds.setDefaultAutoCommit(true);
//        tds.setDefaultTransactionIsolation(java.sql.Connection.TRANSACTION_READ_COMMITTED);
//        tds.setDefaultTransactionIsolation(java.sql.Connection.TRANSACTION_READ_UNCOMMITTED);
        //oder uncommitted????
        
        tds.setConnectionPoolDataSource(cpds);
        
        tds.setMaxActive(maxConnections);                
        tds.setMaxIdle((int) Math.ceil(maxConnections/2));//1 + numberMaxConnections/2);
        
        tds.setMaxWait(5 * 1000);
        tds.setValidationQuery("select 1");
        
        tds.setTestOnBorrow(false);//rausnehmen für speedup
        tds.setTestWhileIdle(true);        
        tds.setTestOnReturn(true);//onBorrow reicht - nur mit denen arbeite ich ja...
        
//                tds.setLoginTimeout();
        tds.setTimeBetweenEvictionRunsMillis(180 * 1000);//60 sekunden
        tds.setNumTestsPerEvictionRun(-1);
        tds.setMinEvictableIdleTimeMillis(3*60*1000);//3min.

        ds = tds;
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
        	public void run() {
        		System.out.println("Shutting down Connection pool...");
        		BalancingConnectionManager.instance.shutdown();
        	}
        });
    }
    
    public static void init(Element lbconfig, boolean local, int limitconns) {
        if(instance == null) {
            instance = new BalancingConnectionManager();
            
             try {
                Process p = Runtime.getRuntime().exec("hostname");
                BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String n = b.readLine().trim();
                int r = p.waitFor();

                hostname = n.trim();
                
//                System.err.println("hostname: "+hostname);
            } catch(Exception ex) {
//                ex.printStackTrace();
            }
            
            Element config = lbconfig.getChild("pgpool");
            
            try {
                instance.createPool(config, local, limitconns);
            }catch(Exception ex) {
                ex.printStackTrace();
            }
            
        }
    }
    
    public static int execUpdate(String sql) {
        Connection con = null;

        int r = -1;

        try {
            con = instance.ds.getConnection();
            DBStatement stmt = new DBStatement(con);
            
            r = stmt.execUpdate(sql);
            
            stmt.closeStatement();
            
            if(con!=null) con.close();

        } catch(Exception ex) {
//            ex.printStackTrace();
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
        	PrintWriter pw = new PrintWriter(ba);
        	ex.printStackTrace(pw);
        	
        	java.util.Date d = new java.util.Date();
        	System.err.println(d.toString()+"\n"+ba.toString());
            
            try {
            	if(con!=null) con.close();
            } catch(Exception exc) {
            	exc.printStackTrace();
            }
        }


//        try {
//            if(con != null) {
//            	con.close();
//            }
//        }catch(Exception ex){
//            ex.printStackTrace();
//        }

        if(echoSql) {
        	System.out.println("SQL: "+sql+" \t-> "+r);
        }
        return r;
        
    }
    
    
    
//    static SQLOutDatingCache sqlcache = new SQLOutDatingCache();
    
    public static DBResultSet execQuery(String sql) {
        return execQuery(sql, false, null);
    }
    
    
    public static DBResultSet execQuery(String sql, boolean cacheon) {
    	return execQuery(sql, cacheon, null);
    }
    public static DBResultSet execQuery(String sql, boolean cacheon, String measure) {
    	long j = System.currentTimeMillis();
    	
        DBResultSet Rs = null;
        if(cacheon) {
//            Rs = (DBResultSet)sqlcache.get(sql);
        }
        
        if(Rs==null) {
            Connection con = null;
            try {
                con = instance.ds.getConnection();
                DBStatement stmt = new DBStatement(con);
                
                //stmt.execQuery("select count(*) from \"TRACKS\"");
                                
                Rs = stmt.execQuery(sql);                
                
                stmt.closeStatement();

                if(cacheon) {
//                    sqlcache.put(sql, Rs);
                }
                
                if(con!=null) con.close();

            } catch(Exception ex) {
            	ByteArrayOutputStream ba = new ByteArrayOutputStream();
            	PrintWriter pw = new PrintWriter(ba);
            	ex.printStackTrace(pw);
            	
            	java.util.Date d = new java.util.Date();
            	System.err.println(d.toString()+"\n"+ba.toString());
                
                try {
                	if(con!=null) con.close();
                } catch(Exception exc) {
                	exc.printStackTrace();
                }
            }
            
//            try {
//                if(con!=null) con.close();
//            }catch(Exception ex) {
//                ex.printStackTrace();
//            }
        }

        j = System.currentTimeMillis()-j;
        if(measure != null) {
        	System.out.println("SQL_MEASURE::"+measure+"::"+sql+" -> "+j+"ms");
        }
        
        return Rs;
    }
    
    public static String getInfo() {
        StringBuffer sb = new StringBuffer();
        
        sb.append("getNumActive: "+instance.ds.getNumActive()+"\n");
        sb.append("getNumIdle: "+instance.ds.getNumIdle()+"\n");
        sb.append("getMaxActive: "+instance.ds.getMaxActive()+"\n");
        sb.append("getMaxIdle: "+instance.ds.getMaxIdle()+"\n");
        sb.append("getMaxWait: "+instance.ds.getMaxWait()+"\n");
        sb.append("getMinEvictableIdleTimeMillis: "+instance.ds.getMinEvictableIdleTimeMillis()+"\n");
        sb.append("getNumTestsPerEvictionRun: "+instance.ds.getNumTestsPerEvictionRun()+"\n");
        sb.append("getTimeBetweenEvictionRunsMillis: "+instance.ds.getTimeBetweenEvictionRunsMillis()+"\n");
        sb.append("VALIDATIONQUERY: "+instance.ds.getValidationQuery()+"\n");
        
        return sb.toString();
    }
    
}

