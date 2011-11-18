
package org.fnppl.opensdx.keyserverfe;



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

import org.fnppl.dbaccess.*;
import org.fnppl.opensdx.common.*;
import org.fnppl.opensdx.keyserver.helper.SQLStatement;

import java.io.*;
import java.util.*;

import org.apache.velocity.VelocityContext;
import org.jdom.Element;

public class ActiveUser {
	public final static long MAX_LOGINTIME = 16*60*60*1000;//16h
	
//	public Client client = null; //reference on android-client-spec (if present)
	
    public String sessionid = null;
    public long mandantid = -1;
    public String ip = null;
    
    public boolean loggedin = false;
    public long logintime = -1;
    public long lastalive = -1;
    public long userid = -1;
    public long clientid = -1;
    
    public Hashtable<String, Object> sessiondata = null; //new Hashtable<String, Object>();
    
    public ActiveUser() {}

    public String sessiondataAsString() {
    	return buildSessionXml(sessiondata);
    }
    
    public static ActiveUser logIn( //Auftragnehmer aun, Client cl, 
    		String sessionid, String ip) {
    	ActiveUser au = new ActiveUser();
    	au.loggedin = true;
//    	au.client = cl;
    	au.ip = ip;
    	au.sessionid = sessionid;
//    	au.userid = aun.getId();
    	au.logintime = System.currentTimeMillis();
//    	au.clientid = cl.getLong("clientid");
    	au.sessiondata = new Hashtable<String, Object>();
    	// delete old Sessions for user
//    	flushSessions(aun.getId());
    	
    	au.generateNewDBSession();
    	
    	return au;
    }
    
    public static ActiveUser logInTNuser(
//    		TrendnetUser tu, 
    		String sessionid, String ip, Hashtable<String, Object> sessiondata) {
    	ActiveUser au = new ActiveUser();
    	au.loggedin = true;
    	au.ip = ip;
    	au.sessionid = sessionid;
//    	au.userid = tu.getId();
    	au.logintime = System.currentTimeMillis();
    	au.sessiondata = sessiondata;
    	// delete old Sessions for user
//    	flushSessions(tu.getId());
    	
    	au.generateNewDBSession();
    	
    	return au;
    } 
    
    public static ActiveUser logInKundenUser(
//    		KundenUser tu, 
    		String sessionid, String ip, Hashtable<String, Object> sessiondata) {
    	ActiveUser au = new ActiveUser();
    	au.loggedin = true;
    	au.ip = ip;
    	au.sessionid = sessionid;
//    	au.userid = tu.getId();
    	au.logintime = System.currentTimeMillis();
    	au.sessiondata = sessiondata;
    	// delete old Sessions for user
//    	flushSessions(tu.getId());
    	
    	au.generateNewDBSession();
    	
    	return au;
    }    
    
    public void save() {
    	String sql = null;
    	boolean alreadyExists = false ;
//    	BalancingConnectionManager.execQuery(
//    		"select exists(select * from activeusers where " +
//    		"sessionid='"+SQLStatement.dbEncodeInApostrophes(sessionid)+"' and mandantid="+mandantid+")"
//    	).getBooleanAt(0, 0);

//    	System.out.println("Does this activeuser already exists in ther Database?");
    	if(alreadyExists){
//    		System.out.println("YES! So just update.");
    		//Wenn vorhanden dann reicht ein Update
//        	sql = "update activeusers set " +
//			"logintime="+logintime+", "+
//			"clientid="+clientid+", "+
//			"lastalive="+System.currentTimeMillis()+", "+
//			"sessiondata='"+Helper.dbEncode(buildSessionXml(sessiondata))+"' " +
//			"where sessionid='"+Helper.dbEncode(sessionid)+"' and mandantid="+mandantid;
//        	
//        	int r = BalancingConnectionManager.execUpdate(sql);
    	} else{
    		generateNewDBSession();
    	}
    }
    
    public static boolean detectSessionIdInDB(String sessionid) {
    	return false;
//    	DBResultSet Rs = BalancingConnectionManager.execQuery("select exists(select sessionid from activeusers where sessionid='"+Helper.dbEncode(sessionid)+"') ");
//		return Rs.getBooleanAt(0,0);
    }
    
    public static ActiveUser fromDB(String sessionid) {
//    	DBResultSet Rs = BalancingConnectionManager.execQuery("select * from activeusers where sessionid='"+Helper.dbEncode(sessionid)+"' ");
//		ActiveUser au = new ActiveUser();
//    	au.ip = Rs.getValueOf(0, "ip");
//    	au.sessionid = Rs.getValueOf(0, "sessionid");
//    	au.mandantid = Rs.getLongOf(0, "mandantid");
//    	au.userid = Rs.getLongOf(0, "userid");
//    	au.lastalive = Rs.getLongOf(0, "lastalive");
//    	au.logintime = Rs.getLongOf(0, "logintime");
//    	au.sessiondata = buildSessionHash(Rs.getValueOf(0, "sessiondata"));
//    	au.clientid = Rs.getLongOf(0, "clientid");
//    	au.loggedin = au.logintime > 0;
//    	
//    	return au;
    	return null;
    }
    
    /**
     * Löscht die aktuelle Session des Users
     */
    public static void deleteSession(String sessionid){
//    	BalancingConnectionManager.execUpdate(
//    		"delete from activeusers where sessionid = '"+Helper.dbEncode(sessionid)+"'");
    }
    
    public Object get(String k) { //das hier muß sein, um aus velocity daten zu kriegen...
    	if(k.equalsIgnoreCase("sessionid")) {
    		return sessionid;
    	}
    	else if(k.equalsIgnoreCase("mandantid")) {
    		return ""+mandantid;
    	}
    	else if(k.equalsIgnoreCase("loggedin")) {
    		return ""+loggedin;
    	}
    	else if(k.equalsIgnoreCase("logintime")) {
    		return ""+logintime;
    	}
    	else if(k.equalsIgnoreCase("lastalive")) {
    		return ""+lastalive;
    	}
    	else if(k.equalsIgnoreCase("clientid")) {
    		return ""+clientid;
    	}
    	else if(k.equalsIgnoreCase("userid")) {
    		return ""+userid;
    	}    	
    	return null;
    }
        
//    boolean sessionloaded = false;
//    public void ensureSessionData() {
//    	if(!sessionloaded) {
//    		sessiondata = getSessionFromDB(sessionid);
//    		sessionloaded = true;
//    	}
//    }
    public void generateNewDBSession() {
    	System.out.println("generateNewDBSession() :: IN!");
//    	long userid = -1;
    	lastalive = System.currentTimeMillis();
    	
//    	BalancingConnectionManager.execUpdate("insert into activeusers values (" +
//    			"'"+Helper.dbEncode(sessionid)+"', " +
//    			""+mandantid+", " +
//    			""+userid+", " +
//    			"'"+Helper.dbEncode(ip)+"', " +
//    			""+lastalive+", " +
//    			""+logintime+", " +
//    			""+clientid+", " +
//    			"'"+Helper.dbEncode(buildSessionXml(sessiondata))+"' " + //sessiondata
//    		")");
    }
    
    public static void flushSessions(long userid) {
//    	BalancingConnectionManager.execUpdate("delete from activeusers where userid="+userid);
    }   
    
//    private Hashtable<String, Object> getSessionFromDB(String sessionid) {
//	    Hashtable<String, Object> session = null;
//	
//	    DBResultSet Rs = BalancingConnectionManager.execQuery("select * from activeusers where sessionid='"+Helper.dbEncode(sessionid)+"' and mandantid="+mandantid);
//	
//	    if(Rs.height()==1) {
//	        try {
//	            session = buildSessionHash(Rs.getValueOf(0, "sessiondata"));
//	            lastalive = Long.parseLong(Rs.getValueOf(0, "lastalive"));
//	            logintime = Long.parseLong(Rs.getValueOf(0, "logintime"));
//	            loggedin = logintime > 0;
//	            
//	            if(System.currentTimeMillis() - lastalive > 1000l*60l*15) { //15min.
//	                BalancingConnectionManager.execUpdate("update activeusers set lastalive='"+System.currentTimeMillis()+"' where sessionid='"+Helper.dbEncode(sessionid)+"' and mandantid="+mandantid);
//	            }
//	        } catch(Exception ex) {
//	            ex.printStackTrace();
//	            session = null;
//	        }
//	    }
//	
//	    return session;
//    }
    
    public static Hashtable<String, Object> buildSessionHash(String xml) {
	    Hashtable<String, Object> hash = new Hashtable<String, Object>();
	    org.jdom.input.SAXBuilder sax = new org.jdom.input.SAXBuilder(false);
	    
	    try {
	    	//System.out.println("XML: "+xml);
	    	
	        Element e = sax.build(new StringReader(xml)).getRootElement();
	        Iterator<Element> it = e.getChildren().iterator();
	        while(it.hasNext()) {
	            Element ee = it.next();
	            hash.put(ee.getName(), ee.getText());
	        }
	    } catch(Exception ex) {
	        ex.printStackTrace();
	        hash = null;
	    }

	    return hash;
	}
    
    private static String buildSessionXml(Hashtable<String, Object> hash) {
    	Element e = new Element("sessiondata");

        Enumeration<String> en = hash.keys();
        while(en.hasMoreElements()) {
            String key = (String)en.nextElement();
            Object value= hash.get(key);

            if(value instanceof String) {
                e.addContent((new Element(key)).setText((String)value));
            }
            else if(value instanceof Long) {
            	e.addContent((new Element(key)).setText(""+value));
            }
            else if(value instanceof Boolean) {
            	e.addContent((new Element(key)).setText(""+value));
            }
            else if(value instanceof Integer) {
            	e.addContent((new Element(key)).setText(""+value));
            }
            else if(value instanceof Element) {
                Element ke = (Element)((Element)value).clone();
                ke.setName(key);//dirty?!                
                e.addContent(ke);
            }
        }
        
        org.jdom.output.XMLOutputter xout = new org.jdom.output.XMLOutputter();
        return xout.outputString(e);
    }
}

