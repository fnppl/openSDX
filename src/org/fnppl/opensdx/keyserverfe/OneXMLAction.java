
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

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import org.jdom.*;
import org.apache.velocity.*;
import org.apache.velocity.app.*;


import javax.servlet.http.*;



import javax.servlet.http.*;

/**
 *
 * @author  thiess
 */
public class OneXMLAction extends MyAction {
//	SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US); 
//	TimeZone tz = TimeZone.getTimeZone("GMT");
//	dateFormatter.setTimeZone(tz);
//	return dateFormatter;
	
	final static String RFC1123_1 = "EEE, dd MMM yyyy HH:mm:ss z";
	final static String RFC1123_3 = "EEE, dd MMM yyyy HH:mm:ss zzz";		
	final static String RFC1036 = "EEEE, dd-MMM-yy HH:mm:ss zzz";
	final static String ASCTIME = "EEE MMM dd HH:mm:ss yyyy zzz";
	
	final static Locale germany_english = new Locale("US","DE");
	final static TimeZone gmttimezone = TimeZone.getTimeZone("GMT+1");
		
	final static SimpleDateFormat sdf = new SimpleDateFormat(RFC1123_1, germany_english);
	static {
		sdf.setTimeZone(gmttimezone);
	}
	
	long now = System.currentTimeMillis();
	
    public OneXMLAction(MultiTypeRequest request, HttpServletResponse response) {
        super(request, response, false);
    }
    
    public void performAction(String reqAddress, VelocityContext c) throws Exception {
    	String tnappuid = gimmeValueOf("uuid");
//    	String imei = gimmeValueOf("imei");
    	String sessionid = gimmeValueOf("sessionid");
    	    	    	
    	Element reqresp = new Element("response");
    	int code = RESPONSECODE_OK; //ok
    	
    	Element req = new Element("request");
    	reqresp.addContent(req);
    	mirrorParams(req, reqAddress);
    	
    	Element resp = new Element("onexml");    	
    	reqresp.addContent(resp);
    	
    	ActiveUser au = ActiveUser.fromDB(sessionid);
    	if(au == null) {
    		System.out.println("OneXMLAction::ActiveUser == null for sessionid"+sessionid);
    	}
    	else {
//    		System.out.println("OneXMLAction::ActiveUser: "+Helper.elementToString(au.toElement());
    		System.out.println("OneXMLAction::ActiveUser: sessionid="+sessionid+",tnappuid="+tnappuid+",clientid="+au.clientid);
    	}
    	
    	//au==null =!
//    	Client cl = Client.getClient(au.clientid);
    	//client==null =!
//    	if(cl == null) {
//    		System.out.println("OneXMLAction::Client == null for sessionid"+sessionid);
//    	}
//    	else {
//    		System.out.println("OneXMLAction::Client: "+Helper.elementToString(cl.toElement()));
//    	}
    	
//    	System.out.println("OneXMLAction::Client.toString: "+cl.toString());
    	
    	
//    	if(!validClientSession(cl, au, tnappuid)) {
//    		System.out.println("OneXMLAction::invalidClientSession: "+cl.toString());
//    		code = RESPONSECODE_FAIL;
//    	}
//    	else {
//    		Element[] data = makeMyData(cl, au);
//    	
//    		Element projects_el = data[0];
//    		
//    	    Element aktions_el = data[1];
//    	    Element locations_el = data[2];
//    	    Element auftrag_el = data[3];
//    	    Element ansprechpartner_el = data[4];
//    	    
//    	    Element reportings = makeReportingsElement(cl, au);
//    	    
//    	    resp.addContent(projects_el);
//    	    resp.addContent(aktions_el);
//    	    resp.addContent(locations_el);
//    	    resp.addContent(auftrag_el);
//    	    resp.addContent(ansprechpartner_el);
//    	    
//    	    resp.addContent(reportings);
//    	}
    	
    	Element responsecode = new Element("responsecode");
    	responsecode.setText(""+code);
    	reqresp.addContent(responsecode);
//    	System.out.println(Helper.elementToString(reqresp));

    	writeXML(reqresp);
    }
    
    public static void main(String[] args) throws Exception {
//    	ActionServlet.initOFFDB();//initialisiert die db-connection mit dem "dblocal"-hostname aus resources/config.xml
//
//    	ActiveUser au = ActiveUser.fromDB("74111189393522320822513323415182204411512721918545671822483395");
//    	Client cl = Client.getClient(au.clientid);
//    	
////        <uuid>177551777322118651147252160144571831722002101572539217815117266</uuid>
////        <sessionid></sessionid>
////        <requestaddress>88.77.66.189</requestaddress>
////        <requesttime>1316260997240</requesttime>
//
//    	Element[] data = makeMyData(cl, au);
//		for(int i=0;i<data.length;i++) {
//			System.out.println(Helper.elementToString(data[i]));
//		}
    }
}
