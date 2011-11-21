/*
 * Copyright (C) 2010-2011 
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

package org.fnppl.dbaccess;

import java.sql.*;
import java.io.PrintStream;
import java.util.*;
import java.io.*;


public class DBStatement {
        public static final int TYPE_MYSQL = 0;
        public static final int TYPE_PSQL = 1;
        
        public static final int TYPE_STD = TYPE_PSQL;
    
        final boolean cacheOn = false;
        public static boolean statme = false;
        
        public static boolean logon = false;
        public static BufferedWriter logwriter = null;
    
        String errortext = null;
        Statement Stmt=null;
        PrintStream out = null;
        
        Connection conn;
      
        private static int cacheCounter= 0;
        private final static long maxCacheTime = 30 * 60 * 1000;//30 min.
        private final static long maxCacheSize = 30 * 60 * 1000;//30 min.
        //private static final Hashtable cache = new Hashtable();
        private static final WeakHashMap cache = new WeakHashMap();
                
        
        public final static HashSet currentexecs = new HashSet();
        
        public boolean hufflepuff = false;
        protected DBStatement(Connection Conn) {            
            this.conn=Conn;
            if(conn == null) {
                hufflepuff = true;
                
            }
            else {
                try {
                            Stmt = conn.createStatement();               
                            

                            if(logon && logwriter == null) {
                                //eigentlich synchronizen - argh!
                                logwriter = new BufferedWriter(new FileWriter(new File("/root/finetunes/dbstatement.log"), true));
                            }
                        }                    
                        catch (SQLException E) {
                                this.errortext = "SQLException: " + E.getMessage()+"SQLState: " + E.getSQLState()+"VendorError: " + E.getErrorCode() ;
                                E.printStackTrace();
                        }
                        catch(Exception ex) {
    //                        ex.printStackTrace();
                        }

//                ensureRunningConnection();
            }
        }
        
        
//        public void ensureRunningConnection() {
//            try {
//                if(conn.isClosed()) {
//                    System.err.println("SQL-ERR: DBStatemet::Connection was dead. fetched a new one... (Thread "+Thread.currentThread().getName()+")");
//                    Thread.dumpStack();
//
//                    BalancingConnectionManager.checkIn(conn);
//                    conn = BalancingConnectionManager.checkOut(false);
//                    
////                    ConnectionManager cm = ConnectionManager.getDefaultInstance();
////                    cm.checkIn(conn);
////                    conn = cm.checkOut();
//
//                    try {
//                        Stmt = conn.createStatement();                        
//                    }
//                    catch (SQLException E) {
//                            this.errortext = "SQLException: " + E.getMessage()+"SQLState: " + E.getSQLState()+"VendorError: " + E.getErrorCode() ;
//                            E.printStackTrace();
//                    }
//                }
//            } catch(Exception ex) {
//                System.err.println("SQL-ERR: ");
//                ex.printStackTrace(System.err);
//            }
//        }
        
        public Connection getConnection(){
            if(hufflepuff) {
                return null;
            }
//            ensureRunningConnection();
            
            return conn;
        }
        
        public void closeStatement() {
//            System.err.println("Trying to close STMT");
                try {
                        if(Stmt!=null) {
                                Stmt.close() ;
                        }
                }
                catch(SQLException e) {
                    e.printStackTrace();
                        errortext ="SQLException: " + e.getMessage()+" SQLState: " + e.getSQLState() + " VendorError: " + e.getErrorCode() ;                        
                }
//            System.err.println("Trying to close STMT 2");
        }
        
        public void setOutputStream(PrintStream out){
            this.out = out;
        }

        private void output(String raus){
            if(out!=null)
                out.println(raus);
        }
        
        
        
        public static boolean isCacheValid(Object[] o) {
            Long t = (Long)o[0];
            if(System.currentTimeMillis() - t.longValue() > maxCacheTime) {
                return false;
            }
            
            return true;
        }
        
        public static void cleanupCache() {
            cacheCounter = 0;
            
            Set en = cache.keySet();
            System.err.println("SQLCACHE::SIZE: "+en.size());
            
            Iterator it = en.iterator();
            while(it.hasNext()) {
                String key = (String)it.next();
                
                Object[] o = (Object[])cache.get(key);
                if(o!=null) {
                    if(!isCacheValid(o)) {
                        System.err.println("SQLCACHE::DROPPED: "+key);
                        cache.remove(key);
                    }
                }
            }            
            
//            System.gc();
        }
        
        
        
        public DBResultSet execQuery(String sql) {
            return execQuery(sql, false, TYPE_STD);
        }
        public DBResultSet execQuery(String sql, int type) {
            return execQuery(sql,false, type);
        }
        public DBResultSet execQuery(String sql, boolean cache) {
            return execQuery(sql, cache, TYPE_STD);
        }
        public DBResultSet execQuery(String sql, boolean cacheable, int type) {
                //cacheable = cacheable & cacheOn;
                if(hufflepuff) {
                    //HT 19.12.2007
                    return BalancingConnectionManager.execQuery(sql, cacheable);
                }
            
                DBResultSet Rs = null;
                boolean addCache = false;
                if(Stmt!=null) {
                        output("SQL: "+sql);
                        
//                        ensureRunningConnection();
                        
                        if(cacheable && cacheOn) {
                            if(cacheCounter > 100) {
                                
                                synchronized(cache) {
                                    cleanupCache();
                                }
                            }
                            cacheCounter++;
                            
                            Object[] o = (Object[])cache.get(sql);
                            
                            if(o!=null) {
                                Long t = (Long)o[0];
                                Rs = (DBResultSet)o[1];
                                
                                System.err.println("SQLCACHE::GET: "+sql);
                                
                                if(!isCacheValid(o)) {
                                    System.err.println("SQLCACHE::DROP: "+sql);
                                    cache.remove(sql);
                                    Rs = null;
                                }
                            }
                        }
                        
                        if(Rs==null) {
                            long l = System.currentTimeMillis();
                            Rs = new DBResultSet(Stmt,sql) ;
                            long l2 = System.currentTimeMillis();
                            
//                            try {
//                                Stmt.close();
//                            } catch(Exception ex) {
//                                ex.printStackTrace();
//                            }
                            
                            if(l2-l > 1000) {
                                try {
                                    if(logwriter!=null) logwriter.write("S ::: "+(l2-l)+"ms ::: "+sql+"\n");
                                }catch(Exception ex) {
//                                    ex.printStackTrace();
                                }
                            }
                            
                            if(cacheable && cacheOn) {
                                addCache = true;
                            }
                        }
                        
                        if(Rs.errorOccured()) {
                            System.err.println("SQL-ERROR:: "+sql);
                            errortext = Rs.gimmeErrorText();
                            System.err.println("SQL_ERRCODE: "+errortext);
                            
                            try {
                                if(logwriter!=null) logwriter.write("SQL-ERROR:: "+sql+"\n"+"SQL_ERRORCODE: "+errortext);
                            }catch(Exception ex){ex.printStackTrace();}
                            
                            Thread.currentThread();
							Thread.dumpStack();
                        }
                        else if(addCache) {
                            System.err.println("SQLCACHE::PUT: "+sql);
                            //Thread.currentThread().dumpStack();
                            
                            Object[] o = new Object[]{new Long(System.currentTimeMillis()), Rs};
                            cache.put(sql, o);
                        }
                        return Rs;
                }

                return null;
        }

        public int execUpdate(String stmt) {
            return execUpdate(stmt, TYPE_STD);
        }
        public int execUpdate(String stmt, int type) {
                if(hufflepuff) {
                    //HT 19.12.2007
                    return BalancingConnectionManager.execUpdate(stmt);//NIX CACHEABLE
                }
            
//                System.err.println("TRYING TO UPDATE: "+stmt);
                
                int Rs = -1;
                if(Stmt!=null) {
                        this.errortext=null;
                        
                        Object[] key = new Object[]{stmt,new Long(System.currentTimeMillis())};
                        
                        try {   
                                output("SQL: "+stmt);     
//                                ensureRunningConnection();
                                
                                long l = System.currentTimeMillis();
                                
                                if(DBStatement.statme) {                            
                                    DBStatement.currentexecs.add(key);
                                }
                                Rs = Stmt.executeUpdate(stmt);
                                if(DBStatement.statme) {
                                    DBStatement.currentexecs.remove(key);
                                }
//                                Stmt.close();
                                long l2 = System.currentTimeMillis();
                                
                                if(l2-l > 1000) {
                                    try {
                                        if(logwriter!=null) logwriter.write("U ::: "+(l2-l)+"ms ::: "+stmt+"\n");
                                    }catch(Exception ex) {
//                                        ex.printStackTrace();
                                    }
                                }
                        }
                        catch(SQLException e) {
                                System.err.println("SQL-ERROR: "+stmt);
                                errortext ="SQLException: " + e.getMessage()+"\nSQLState: " + e.getSQLState() + "\nVendorError: " + e.getErrorCode() ;
                                                                
                                System.err.println("SQL_ERRCODE: "+errortext);
                                
                                Thread.currentThread();
								Thread.dumpStack();
                                
                                try {
                                    if(logwriter!=null) logwriter.write("SQL-ERROR:: "+stmt+"\n"+"SQL_ERRORCODE: "+errortext);
                                }catch(Exception ex){ex.printStackTrace();}
                                
                                if(DBStatement.statme) {                            
                                    DBStatement.currentexecs.remove(key);
                                }
                                
                                return -1;
                        }

//                        System.err.println("TRYING TO UPDATE 2: "+stmt);
                        return Rs;
                }

//                System.err.println("TRYING TO UPDATE 3: "+stmt);
                return -1;
        }


        public Statement gimmeStatement() {
                return Stmt;
        }

        @Override
		public String toString() {
                if(Stmt!=null)
                        return Stmt.toString();

                return null;
        }

        public boolean errorOccured() {
                if(errortext==null)
                        return false;
                else
                        return true;
        }

        public String gimmeErrorText() {
                return this.errortext;
        }
        
        public static String transformToMysql(String s) {
//            int type = 0;
//            if(s.indexOf("select")!=0 && s.indexOf("SELECT")!=0) {
//                type = 1;
//            }
            
            StringBuffer sb = new StringBuffer();
            
            boolean escaped = false;
            boolean instring = false;
            
            for(int i=0;i<s.length();i++) {
//                if(type==1) {
//                    //update oder so
//                }
//                else {
//                    //select-type
//
//                }
                char c = s.charAt(i);
                
                if(c=='\\' && !escaped) {
                    escaped = true;
                    sb.append("\\");
                    continue;
                }
                
                if(c=='\"') {
                    if(escaped || instring) {                
                        sb.append(c);
                    }
                    else {
                        continue;
                    }
                }
                if(c=='\'') {
                    if(!escaped) {
                        instring = !instring;
                    }
                }
                
                sb.append(c);
                escaped = false;
            }
            
            return sb.toString();
        }
        
        public static void main(String[] args) throws Exception {
            BufferedReader br = new BufferedReader(new FileReader(new File(args[0])));
            String zeile = "";
            while((zeile=br.readLine())!=null) {
                StringTokenizer k = new StringTokenizer(zeile,":::");
                String s = k.nextToken().trim();
                String sql = k.nextToken().trim();
                String t = k.nextToken().trim();
                
                System.out.println(transformToMysql(sql));
            }
        }
        
        
        public static String getCurrent() {
            if(statme == false) {
                return "DISABLED";
            }
            
            StringBuffer sb = new StringBuffer();
            Iterator it = new HashSet(DBStatement.currentexecs).iterator();
            
            long jetzt = System.currentTimeMillis();
            
            while(it.hasNext()) {
                Object[] o = (Object[])it.next();
                sb.append((jetzt - ((Long)o[1]).longValue()));
                sb.append("\t");
                sb.append((String)o[0]);
                sb.append("\n");
            }
                        
            return sb.toString();
        }
}