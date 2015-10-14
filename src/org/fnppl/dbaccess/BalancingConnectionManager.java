package org.fnppl.dbaccess;
/*
 * Copyright (C) 2010-2015
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
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.jdom2.*;
import org.apache.commons.dbcp.cpdsadapter.*;
import org.apache.commons.dbcp.datasources.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;


//import PooledConnection;

//INCLUDE IN CLASSPATH for compile/run
 //commons-pool-1.3.jar commons-dbcp-1.2.2.jar commons-collections-3.2.jar

public class BalancingConnectionManager {
	public final static String DEFAULT_POOLNAME = "default";
	public final static int DEFAULT_MAX_CONNECTIONS = 1;
	public final static int DEFAULT_MIN_IDLE = 1;
	public final static int DEFAULT_INITIAL_CONNECTIONS = 1;
	
    public static boolean echoSql = false;
    private SharedPoolDataSource ds;
    
    private static Hashtable<String, BalancingConnectionManager> instances = new Hashtable<String, BalancingConnectionManager>();
    static {
    	Runtime.getRuntime().addShutdownHook(new Thread(){
    		public void run() {
    			shutdownAll();
    		}
    	});
    }
    public static void shutdownAll() {
    	if(instances == null) {
    		return;
    	}
    	
    	Iterator<BalancingConnectionManager> insts = instances.values().iterator();
    	while(insts.hasNext()) {
    		try {
    			BalancingConnectionManager e = insts.next();
    			e.shutdown();
    		} catch(Exception ex) {
    			ex.printStackTrace();
    		}
    	}
    }
    
    
    
    
    public void shutdown() {
    	try {
    		ds.close();
    	} catch(Exception ex) {
//    		ex.printStackTrace();
    	}
    }
    
    private BalancingConnectionManager() {
    }
    
    private Vector confs = new Vector();
    public static String hostname = null;
    
//    public static void init(Element config, int limitconns) {
//    	RuntimeException ex = null;
//    	synchronized(instances) {
//    		if(instances.size() == 0) { // || !hasPool(DEFAULT_POOLNAME)) {
//    			try {
//    				addPool(DEFAULT_POOLNAME, config, limitconns);
//    			} catch(RuntimeException e) {
//    				ex = e;
//    			}
//    		}
//    	}
//    	throw ex;
//    }
    
    public static void initDefaultPool(
    		String drivermanager, 
    		String dbserver, 
    		int dbport, 
    		String dbname,
    		String dbdbname, 
    		String applicationname,
    		String dbusername, 
    		String dbpassword, 
    		int initialconnections, 
    		int maxconnections) {
    	
    	RuntimeException ex = null;
    	synchronized(instances) {
    		if(instances.size() == 0) { // || !hasPool(DEFAULT_POOLNAME)) {
    			try {
    				addPool(DEFAULT_POOLNAME, drivermanager, dbserver, dbport, dbname, dbdbname, applicationname, dbusername, dbpassword, initialconnections, maxconnections);
    			} catch(RuntimeException e) {
    				ex = e;
    			}
    		}
    	}
    	
    	if(ex != null) {
    		throw ex;
    	}
    }
    
    public static void addPool(
    		String poolname,
    		String drivermanager, 
    		String dbserver, 
    		int dbport, 
    		String dbname,
    		String dbdbname, 
    		String applicationname,
    		String dbusername, 
    		String dbpassword,
    		int initialconnections, 
    		int maxconnections
    	) {

    	if(hasPool(poolname)) {
    		throw new RuntimeException("DBConnectionPool-Container already contains pool with name="+poolname);
    	}
    	
    	BalancingConnectionManager instance = new BalancingConnectionManager();
        
        
        try {
//        	instance.createPool(e, false, maxconnections);
        	
//        	String drivermanager = config.getChildText("drivermanager");
//            //String dbserver = local ? config.getChildText("dblocal") : config.getChildText("dbserver");
//        	String dbserver = config.getChildText("dbserver");                                
//            String dbdbname = config.getChildText("dbdbname");
//            String dbusername = config.getChildText("dbusername");
//            String dbpassword = config.getChildText("dbpassword");
//            int dbport = Integer.parseInt(config.getChildText("dbport"));
//            String dbname = config.getChildText("dbname");
            
//            int initialConnections = Integer.parseInt(config.getChildText("initialconnections"));
//            int maxConnections = Integer.parseInt(config.getChildText("maxconnections"));
        	if(maxconnections<0) {
        		maxconnections = DEFAULT_MAX_CONNECTIONS;
        	}
        	
            String connectionURL = "jdbc:"+dbname+"://"+dbserver + ":"+dbport +"/"+dbdbname+"?charSet=UTF8&ApplicationName="+applicationname; //&user="+dbusername+"&password="+dbpassword;
            
//            Properties prop = new Properties();
//            
////            System.err.println("Trying to init connectionManager limitcons: "+limitconns+" dbserver: "+dbserver);
//            prop.setProperty("persistence.ConnectionURL", "jdbc:"+dbname+"://"+dbserver + ":"+dbport +"/"+dbdbname+"?charSet=UTF8");
//            
//            prop.setProperty("persistence.DbUsername", dbusername);
//            prop.setProperty("persistence.DbPassword", dbpassword);
//            prop.setProperty("persistence.NumberInitialConnections", ""+initialConnections);
//            prop.setProperty("persistence.NumberMaxConnections", ""+maxConnections);
                
//            System.out.print(prop.toString());
            
            DriverAdapterCPDS cpds = new DriverAdapterCPDS();
            if(dbusername !=null && dbpassword !=null) {
            	cpds.setUser(dbusername);
            	cpds.setPassword(dbpassword);
            }
            
            cpds.setDriver(drivermanager);
            cpds.setUrl(connectionURL);
            cpds.setUser(dbusername);
            cpds.setPassword(dbpassword);

            SharedPoolDataSource tds = new SharedPoolDataSource();
            tds.setDescription(poolname);
//            tds.setDefaultAutoCommit(true);
//            tds.setDefaultTransactionIsolation(java.sql.Connection.TRANSACTION_READ_COMMITTED);
//            tds.setDefaultTransactionIsolation(java.sql.Connection.TRANSACTION_READ_UNCOMMITTED);
            //oder uncommitted????
            
            tds.setConnectionPoolDataSource(cpds);
            
            tds.setMaxActive(maxconnections);
            int mi = (int) Math.ceil(maxconnections/2);
            if(mi<1) {
            	mi =1;
            }
            tds.setMaxIdle(mi); //1 + numberMaxConnections/2);
            
            
            tds.setMaxWait(5 * 1000);
            tds.setValidationQuery("select 1");
            
            tds.setTestOnBorrow(true);
            tds.setTestWhileIdle(false);        
            tds.setTestOnReturn(false); 
            
//          tds.setLoginTimeout();
            tds.setTimeBetweenEvictionRunsMillis(10 * 1000); //10 sekunden
            tds.setNumTestsPerEvictionRun(-1);
            tds.setMinEvictableIdleTimeMillis(30*1000); //30 sekunden

            instance.ds = tds;
            
            System.out.println(getInfo(instance));
            
            instances.put(poolname, instance);
        } catch(Exception ex) {
        	ex.printStackTrace();
        }            
//        
//    	Element e = new Element("pgpool");
//        e.addContent((new Element("drivermanager")).setText(drivermanager));
//        e.addContent((new Element("dbserver")).setText(dbserver));
//        e.addContent((new Element("dbport")).setText(""+dbport));
//        e.addContent((new Element("dbdbname")).setText(dbdbname));
//        e.addContent((new Element("dbusername")).setText(dbusername));
//        e.addContent((new Element("dbpassword")).setText(dbpassword));
//        e.addContent((new Element("dbname")).setText(dbname));
//        e.addContent((new Element("initialconnections")).setText(""+initialconnections));
//        e.addContent((new Element("maxconnections")).setText(""+maxconnections));
//        
//        addPool(poolname, e, maxconnections);
    } 
    public static boolean hasPool(String poolname) {
    	if(instances.containsKey(poolname)) {
    		return true;
    	}
    	return false;
    }
//    public static void addPool(String poolname, Element config, int limitconns) {
//    	asd
//    }
    
//    public void createPool(Element config, int limitconns) throws Exception {
//        String drivermanager = config.getChildText("drivermanager");
//        String dbserver = config.getChildText("dbserver");
//        
//
////        System.err.println("hostname: "+hostname);
////        if((System.getProperty("isrzdb")!=null && System.getProperty("isrzdb").equals("true")) || hostname.indexOf("finestblade")>=0  || hostname.equals("fineblade2.finetunes.net") || hostname.equals("finestblade.finetunes.net") || hostname.equals("raidblade2.finetunes.net") ) {
////            dbserver = config.getChildText("dbserver");
////        }
//        
//        String dbdbname=config.getChildText("dbdbname");
//        String dbusername=config.getChildText("dbusername");
//        String dbpassword=config.getChildText("dbpassword");
//        int dbport = Integer.parseInt(config.getChildText("dbport"));
//        String dbname=config.getChildText("dbname");
//        String initialConnections=config.getChildText("initialconnections");
//        int maxConnections=Integer.parseInt(config.getChildText("maxconnections"));
//
//        initialConnections = "1";
//        
//        Properties prop = new Properties();
//        
//        System.err.println("Trying to init connectionManager limitcons: "+limitconns+" dbserver: "+dbserver);
//        prop.setProperty("persistence.ConnectionURL", "jdbc:"+dbname+"://"+dbserver + ":"+dbport +"/"+dbdbname+"?charSet=UTF8");
//        
//        prop.setProperty("persistence.DbUsername", dbusername);
//        prop.setProperty("persistence.DbPassword", dbpassword);
//        prop.setProperty("persistence.NumberInitialConnections", initialConnections);
//        prop.setProperty("persistence.NumberMaxConnections", ""+maxConnections);
//            
//        System.out.print(prop.toString());            
//        
//        DriverAdapterCPDS cpds = new DriverAdapterCPDS();
//        cpds.setDriver(drivermanager);
//        cpds.setUrl(prop.get("persistence.ConnectionURL").toString());
//        cpds.setUser(prop.get("persistence.DbUsername").toString());
//        cpds.setPassword(prop.get("persistence.DbPassword").toString());
//
//
//        SharedPoolDataSource tds = new SharedPoolDataSource();
//        
//
////        tds.setDefaultAutoCommit(true);
////        tds.setDefaultTransactionIsolation(java.sql.Connection.TRANSACTION_READ_COMMITTED);
////        tds.setDefaultTransactionIsolation(java.sql.Connection.TRANSACTION_READ_UNCOMMITTED);
//        //oder uncommitted????
//        
//        tds.setConnectionPoolDataSource(cpds);
//        
//        tds.setMaxActive(maxConnections);                
//        //tds.setMaxIdle((int) Math.ceil(maxConnections/2));//1 + numberMaxConnections/2);
//        tds.setMaxIdle((int) Math.ceil(maxConnections/2));//1 + numberMaxConnections/2);
//        
//        tds.setMaxWait(5 * 1000);
//        tds.setValidationQuery("select 1");
//        
//        tds.setTestOnBorrow(true);//rausnehmen für speedup
//        tds.setTestWhileIdle(false);        
//        tds.setTestOnReturn(false);//onBorrow reicht - nur mit denen arbeite ich ja...
//        
////        tds.setDefaultAutoCommit(true);
////        tds.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
//        
////                tds.setLoginTimeout();
//        tds.setTimeBetweenEvictionRunsMillis(10 * 1000);//10 sekunden
//        tds.setNumTestsPerEvictionRun(-1);
//        tds.setMinEvictableIdleTimeMillis(30*1000);//30 sekunden
//
//        ds = tds;
//        
////        Runtime.getRuntime().addShutdownHook(new Thread() {
////        	public void run() {
////        		System.out.println("Shutting down Connection pool...");
////        		BalancingConnectionManager.instance.shutdown();
////        	}
////        });
//    }
    
    
    
//    public static void init(
//    		String drivermanager, 
//    		String dbserver, 
//    		int dbport, 
//    		String dbname,
//    		String dbdbname, 
//    		String dbusername, 
//    		String dbpassword, 
//    		int initialconnections, 
//    		int maxconnections
//    	) {
//
//    	if(instance == null) {
//            instance = new BalancingConnectionManager();
//            
//                         
//            Element e = new Element("pgpool");
//            e.addContent((new Element("drivermanager")).setText(drivermanager));
//            e.addContent((new Element("dbserver")).setText(dbserver));
//            e.addContent((new Element("dbport")).setText(""+dbport));
//            e.addContent((new Element("dbdbname")).setText(dbdbname));
//            e.addContent((new Element("dbusername")).setText(dbusername));
//            e.addContent((new Element("dbpassword")).setText(dbpassword));
//            e.addContent((new Element("dbname")).setText(dbname));
//            e.addContent((new Element("initialconnections")).setText(""+initialconnections));
//            e.addContent((new Element("maxconnections")).setText(""+maxconnections));
//            
//            try {
//            	instance.createPool(e, maxconnections);
//            }catch(Exception ex) {
//            	ex.printStackTrace();
//            }            
//        }
//    }
      
//    private Vector confs = new Vector();
//    public static String hostname = null;
//    
//    public void createPool(Element config, int limitconns) throws Exception {
//        String drivermanager = config.getChildText("drivermanager");
//        String dbserver = config.getChildText("dbserver");
//        
//
////        System.err.println("hostname: "+hostname);
////        if((System.getProperty("isrzdb")!=null && System.getProperty("isrzdb").equals("true")) || hostname.indexOf("finestblade")>=0  || hostname.equals("fineblade2.finetunes.net") || hostname.equals("finestblade.finetunes.net") || hostname.equals("raidblade2.finetunes.net") ) {
////            dbserver = config.getChildText("dbserver");
////        }
//        
//        String dbdbname=config.getChildText("dbdbname");
//        String dbusername=config.getChildText("dbusername");
//        String dbpassword=config.getChildText("dbpassword");
//        int dbport = Integer.parseInt(config.getChildText("dbport"));
//        String dbname=config.getChildText("dbname");
//        String initialConnections=config.getChildText("initialconnections");
//        int maxConnections=Integer.parseInt(config.getChildText("maxconnections"));
//
//        initialConnections = "1";
//        
//        Properties prop = new Properties();
//        
//        System.err.println("Trying to init connectionManager limitcons: "+limitconns+" dbserver: "+dbserver);
//        prop.setProperty("persistence.ConnectionURL", "jdbc:"+dbname+"://"+dbserver + ":"+dbport +"/"+dbdbname+"?charSet=UTF8");
//        
//        prop.setProperty("persistence.DbUsername", dbusername);
//        prop.setProperty("persistence.DbPassword", dbpassword);
//        prop.setProperty("persistence.NumberInitialConnections", initialConnections);
//        prop.setProperty("persistence.NumberMaxConnections", ""+maxConnections);
//            
//        System.out.print(prop.toString());            
//        
//        DriverAdapterCPDS cpds = new DriverAdapterCPDS();
//        cpds.setDriver(drivermanager);
//        cpds.setUrl(prop.get("persistence.ConnectionURL").toString());
//        cpds.setUser(prop.get("persistence.DbUsername").toString());
//        cpds.setPassword(prop.get("persistence.DbPassword").toString());
//
//
//        SharedPoolDataSource tds = new SharedPoolDataSource();
//        
//
////        tds.setDefaultAutoCommit(true);
////        tds.setDefaultTransactionIsolation(java.sql.Connection.TRANSACTION_READ_COMMITTED);
////        tds.setDefaultTransactionIsolation(java.sql.Connection.TRANSACTION_READ_UNCOMMITTED);
//        //oder uncommitted????
//        
//        tds.setConnectionPoolDataSource(cpds);
//        
//        tds.setMaxActive(maxConnections);                
//        //tds.setMaxIdle((int) Math.ceil(maxConnections/2));//1 + numberMaxConnections/2);
//        tds.setMaxIdle((int) Math.ceil(maxConnections/2));//1 + numberMaxConnections/2);
//        
//        tds.setMaxWait(5 * 1000);
//        tds.setValidationQuery("select 1");
//        
//        tds.setTestOnBorrow(true);//rausnehmen für speedup
//        tds.setTestWhileIdle(false);        
//        tds.setTestOnReturn(false);//onBorrow reicht - nur mit denen arbeite ich ja...
//        
////        tds.setDefaultAutoCommit(true);
////        tds.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
//        
////                tds.setLoginTimeout();
//        tds.setTimeBetweenEvictionRunsMillis(10 * 1000);//10 sekunden
//        tds.setNumTestsPerEvictionRun(-1);
//        tds.setMinEvictableIdleTimeMillis(30*1000);//30 sekunden
//
//        ds = tds;
//        
//        Runtime.getRuntime().addShutdownHook(new Thread() {
//        	public void run() {
//        		System.out.println("Shutting down Connection pool...");
//        		BalancingConnectionManager.instance.shutdown();
//        	}
//        });
//    }
    
//    public static void init(Element lbconfig, int limitconns) {
//        if(instance == null) {
//            instance = new BalancingConnectionManager();
//            
////            Element config = lbconfig.getChild("pgpool");
//            Element config = lbconfig;
//            
//            try {
//                instance.createPool(config, limitconns);
//            }catch(Exception ex) {
//                ex.printStackTrace();
//            }
//            
//        }
//    }
    
    public static int execUpdate(String sql, SQLResult res) {
    	return execUpdate(DEFAULT_POOLNAME, sql, res);
    }
    public static SQLResult execUpdate(String sql) {
    	SQLResult res = new SQLResult();
    	execUpdate(DEFAULT_POOLNAME, sql, res);
    	return res;
    }
    
    
    public static SQLResult execUpdate(String poolname, String sql) {
    	SQLResult res = new SQLResult();
    	execUpdate(poolname, sql, res);
    	return res;
    }
    public static int execUpdate(String poolname, String sql, SQLResult res) {
    	BalancingConnectionManager instance = instances.get(poolname);
    	if(instance == null) {
    		throw new RuntimeException("There is no pool with name="+poolname);
    	}
    	
        Connection conn = null;
        Statement Stmt = null;

        int r = -1;

        try {
            conn = instance.ds.getConnection();
//            DBStatement stmt = new DBStatement(con);
            Stmt = conn.createStatement(); //ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            
            r = Stmt.executeUpdate(sql);
            if(res != null) {
            	res.affected = r;
            }
            
            if(Stmt != null) {
            	Stmt.close();
            }
            if(conn!=null) {
            	conn.close();
            }

        } catch(Exception ex) {
//            ex.printStackTrace();
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
        	PrintWriter pw = new PrintWriter(ba);
        	ex.printStackTrace(pw);
        	
        	if(res != null) {
        		res.ex = ex;
        		res.error_occured = true;
        		res.error_text = ex.getMessage();
        	}
        	
        	java.util.Date d = new java.util.Date();
        	System.err.println(d.toString()+"\n\t[SQL: "+sql+"]\n\t"+ba.toString());
            
        	try {
            	if(Stmt != null) {
            		Stmt.close();
            	}
            } catch(Exception exc) {
            	exc.printStackTrace();
            }
        	
            try {
            	if(conn != null) {
            		conn.close();
            	}
            } catch(Exception exc) {
            	exc.printStackTrace();
            }
        }

        if(echoSql) {
        	System.out.println("SQL: "+sql+" \t-> "+r);
        }
        return r;
        
    }
    
    
    
private SQLOutDatingCache sqlcache = new SQLOutDatingCache(1000*60*10); //10 min. cacheout
    
    public static DBResultSet execQuery(String sql) {
        return execQuery(DEFAULT_POOLNAME, sql, false, null, null);
    }
    public static DBResultSet execQuery(String sql, boolean cacheon) {
        return execQuery(DEFAULT_POOLNAME, sql, cacheon, null, null);
    }
    
    public static DBResultSet execQuery(String sql, SQLResult res) {
        return execQuery(DEFAULT_POOLNAME, sql, false, null, res);
    }    
    public static DBResultSet execQuery(String poolname, String sql, boolean cacheon, SQLResult res) {
    	return execQuery(poolname, sql, cacheon, null, res);
    }
    public static DBResultSet execQuery(String poolname, String sql, boolean cacheon, String measure, SQLResult res) {
    	BalancingConnectionManager instance = instances.get(poolname);
    	if(instance == null) {
    		throw new RuntimeException("There is no pool with name="+poolname);
    	}
    	
    	long j = System.currentTimeMillis();
    	
    	SQLOutDatingCache.ComposedKey ck = null;
    	if(cacheon) {
    		//ck = SQLOutDatingCache.ComposedKey.createNew(poolname).addKeyElement(sql);
    		ck = SQLOutDatingCache.ComposedKey.createNew(sql);
    	}
    	
        DBResultSet Rs = null;
        if(cacheon) {
            Rs = instance.sqlcache.get(ck);
        }
        
        if(Rs == null) {
            Connection conn = null;
            Statement Stmt = null;
            
            try {
                conn = instance.ds.getConnection();
//                DBStatement stmt = new DBStatement(conn);
                Stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);                  
//                Rs = stmt.execQuery(sql);                
                
                Rs = new DBResultSet(Stmt, sql) ;
                
                Stmt.close();

                if(cacheon) {
                    instance.sqlcache.put(ck, Rs);
                }
                
                if(Stmt != null) {
                	Stmt.close();
                }
                if(conn != null) {
                	conn.close();
                }

//            catch (SQLException E) {
            } catch(Exception ex) {
            	ByteArrayOutputStream ba = new ByteArrayOutputStream();
            	PrintWriter pw = new PrintWriter(ba);
            	ex.printStackTrace(pw);
            	
            	java.util.Date d = new java.util.Date();
            	System.err.println(d.toString()+"\n\t[SQL: "+sql+"]\n\t"+ba.toString());
            	
            	if(res != null) {
            		res.ex = ex;
            		res.error_occured = true;
            		res.error_text = ex.getMessage();
            	}
            	
            	try {
                	if(Stmt != null) {
                		Stmt.close();
                	}
                } catch(Exception exc) {
                	exc.printStackTrace();
                }
            	
            	
                try {
                	if(conn!=null) {
                		conn.close();
                	}
                } catch(Exception exc) {
                	exc.printStackTrace();
                }
            }
        }

        j = System.currentTimeMillis()-j;
        if(measure != null) {
        	System.out.println("SQL_MEASURE::"+measure+"::"+sql+" -> "+j+"ms");
        }
        
        return Rs;
    }
    
    
    public static String getInfo(BalancingConnectionManager instance) {
    	StringBuffer sb = new StringBuffer();

    	sb.append("Pool[name="+instance.ds.getDescription()+"]:\n");
    	sb.append("\tgetNumActive: "+instance.ds.getNumActive()+"\n");
    	sb.append("\tgetNumIdle: "+instance.ds.getNumIdle()+"\n");
    	sb.append("\tgetMaxActive: "+instance.ds.getMaxActive()+"\n");
    	sb.append("\tgetMaxIdle: "+instance.ds.getMaxIdle()+"\n");
    	sb.append("\tgetMaxWait: "+instance.ds.getMaxWait()+"\n");
    	sb.append("\tgetMinEvictableIdleTimeMillis: "+instance.ds.getMinEvictableIdleTimeMillis()+"\n");
    	sb.append("\tgetNumTestsPerEvictionRun: "+instance.ds.getNumTestsPerEvictionRun()+"\n");
    	sb.append("\tgetTimeBetweenEvictionRunsMillis: "+instance.ds.getTimeBetweenEvictionRunsMillis()+"\n");
    	sb.append("\tVALIDATIONQUERY: "+instance.ds.getValidationQuery()+"\n\n");

    	return sb.toString();
    }
        
        
    public static String getInfo() {
        StringBuffer sb = new StringBuffer();
        
        Enumeration<String> keys = instances.keys();
    	while(keys.hasMoreElements()) {
    		try {
    			String k = keys.nextElement();
    			BalancingConnectionManager instance = instances.get(k);
    			sb.append(getInfo(instance));
//    			sb.append("Pool[name="+k+"]:\n");
//    			sb.append("\tgetNumActive: "+instance.ds.getNumActive()+"\n");
//    	        sb.append("\tgetNumIdle: "+instance.ds.getNumIdle()+"\n");
//    	        sb.append("\tgetMaxActive: "+instance.ds.getMaxActive()+"\n");
//    	        sb.append("\tgetMaxIdle: "+instance.ds.getMaxIdle()+"\n");
//    	        sb.append("\tgetMaxWait: "+instance.ds.getMaxWait()+"\n");
//    	        sb.append("\tgetMinEvictableIdleTimeMillis: "+instance.ds.getMinEvictableIdleTimeMillis()+"\n");
//    	        sb.append("\tgetNumTestsPerEvictionRun: "+instance.ds.getNumTestsPerEvictionRun()+"\n");
//    	        sb.append("\tgetTimeBetweenEvictionRunsMillis: "+instance.ds.getTimeBetweenEvictionRunsMillis()+"\n");
//    	        sb.append("\tVALIDATIONQUERY: "+instance.ds.getValidationQuery()+"\n\n");
    			
    		} catch(Exception ex) {
    			ex.printStackTrace();
    		}
    	}
        
        
        
        return sb.toString();
    }
 
    public static class SQLResult {
    	public int affected = -1;
    	public boolean error_occured = false;
    	public String error_text = null;
    	public Exception ex = null;
    }

    public static void main(String[] args) {
//        <drivermanager>org.postgresql.Driver</drivermanager>
//        <dbserver>192.168.42.4</dbserver><!-- 192.168.0.114 -->
//        <dbport>5432</dbport>
//        <dbdbname>INDIELOADS</dbdbname>
//        <dbusername>root</dbusername>
//        <dbpassword>fucker</dbpassword><!-- fucker -->
//        <dbname>postgresql</dbname>
//        <initialconnections>10</initialconnections>
//        <maxconnections>100</maxconnections>
    	
    	if(args.length != 5 && args.length != 4) {
			System.err.println("Usage: BalancingConnectionManager DBSERVER DBNAME APPLICATIONNAME USERNAME PASSWORD");
			System.err.println("Usage: BalancingConnectionManager DBSERVER DBNAME USERNAME PASSWORD");
//			 java -cp .:bin:lib/combined_poi.jar:lib/commons-dbcp.jar:lib/commons-net-3.3.jar:lib/commons-pool.jar:lib/itext.jar:lib/jai_codec.jar:lib/jai_core.jar:lib/jdom-2.0.5.jar:lib/json-simple-1.1.1.jar:lib/jzlib.jar:lib/odfdom-0.8.7.jar:lib/psql.jar:lib/servlet.jar:lib/simple-odf-0.6.6.jar:lib/slf4j-api-1.6.2.jar:lib/sshj.jar:lib/xercesImpl.jar:lib/xml-apis.jar:lib/opensdx/bcpg-jdk15on-151.jar:lib/opensdx/bcpkix-jdk15on-151.jar:lib/opensdx/bcprov-jdk15on-151.jar:lib/opensdx/opensdx_secgui.jar net.finetunes.dbaccess.BalancingConnectionManager ftdbserver INDIELOADS TESTAPP root fucker
			System.exit(1);
		}
    	
    	String host = args[0];
    	String dbdbname = args[1];
    	String applicationname = null;
    	String username = null; 
		String password = null;
		
    	try {
        	applicationname = System.getProperty("user.name")+"@"+InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
    	
    	if(args.length == 5) {
    		applicationname = args[2];
    		username = args[3]; 
    		password = args[4];
    	}
    	else {
    		username = args[2]; 
    		password = args[3];
    	}
    	
		initDefaultPool(
				"org.postgresql.Driver", 
				host, 
				5432, 
				"postgresql", 
				dbdbname,
				applicationname,
				username, 
				password, 
				DEFAULT_INITIAL_CONNECTIONS, 
				DEFAULT_MAX_CONNECTIONS
			);
		
		SQLResult sr = new SQLResult();

		for (int i=0;i<100;i++) {
			String sql = "select 20+"+i;
			DBResultSet Rs = BalancingConnectionManager.execQuery(sql, sr);
			if(sr.error_occured ) {
				sr.ex.printStackTrace();
				break;
			}
			System.out.println(sql+"["+i+"] -> "+Rs.getValueAt(0, 0));
		}
		
		
		System.exit(0);
    }
}

