/*
 * $Id: MyAction.java 1881 2010-04-01 11:57:54Z SP $
 */


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


import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;

import java.io.*;

import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.*;
import org.apache.velocity.*;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import org.fnppl.dbaccess.*;
import org.fnppl.opensdx.common.*;

@SuppressWarnings("unchecked")
public abstract class MyAction {
	public final static int RESPONSECODE_OK = 0;
	public final static int RESPONSECODE_SHIZZLE = 1;
	public final static int RESPONSECODE_SHOULD_RELOADCONFIG = 2;
	public final static int RESPONSECODE_MUST_RELOADCONFIG = 3;
	public final static int RESPONSECODE_REPORTING_ALREADY_EXISTS = 50;
	public final static int RESPONSECODE_FAIL = 100;
	public final static int RESPONSECODE_FAIL_DUE_TO_RECONFIG = 102;
	
	public boolean admin = false;
	
	public String mode = "UNDEFINED";  
	public Vector<String>[] parameter = new Vector[2];

	protected OutputStream outStream = null; 

	public MultiTypeRequest request;
	public HttpServletResponse response;

	public String encoding = "UTF-8";

	public String server = null;
	public int port = 0;

	public static Element config;
	public static File storagedir = null;

	public ActiveUser user;//HT 02.04.2009 - das ist ja meine kapsel für session und user

	private static final void readConfig() throws Exception {
		SAXBuilder sax = new SAXBuilder();        
		Class<MyAction> c = MyAction.class;        
		config = sax.build(new InputStreamReader(c.getResourceAsStream("resources/config.xml"))).getRootElement();
		
		storagedir = new File(config.getChildText("storagedir"));
		storagedir.mkdirs();
		
		System.out.println("StorageDir: "+storagedir.getPath());
	}

	static {
		try {
			readConfig();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	public final static boolean validClientSession(Client cl, ActiveUser au, String tnappuid) {
		System.out.println("MyAction::validClientSession::Client==null: "+(cl==null));
		System.out.println("MyAction::validClientSession::ActiveUser==null: "+(au==null));
		
		if(cl!=null && au!=null) {
			System.out.println("MyAction::validClientSession::given tnappuid="+tnappuid+" equals cl.tnappuid="+cl.get("tnappuid")+" "+(cl.get("tnappuid").equals(tnappuid)));
			System.out.println("MyAction::validClientSession::au.loggedin: "+au.loggedin);
			System.out.println("MyAction::validClientSession::au.logintime: "+au.logintime+ "maxlogin("+au.logintime+ActiveUser.MAX_LOGINTIME+")>current: "+(au.logintime+ActiveUser.MAX_LOGINTIME > System.currentTimeMillis()));
    		
			if(cl.get("tnappuid").equals(tnappuid) && au.loggedin && au.logintime+ActiveUser.MAX_LOGINTIME > System.currentTimeMillis()) {
    			return true;
    		}
    	}
    	
    	return false;    	
	}

	public final static String replace(String inwhat, String what, String withme) {
		StringBuffer ret = new StringBuffer();
		StringBuffer runbuff = new StringBuffer();
		try {
			for(int i=0;i<inwhat.length();i++) {
				char c = inwhat.charAt(i);

				runbuff.append(c);
				if(runbuff.toString().equals(what)) {
					ret.append(withme);
					runbuff.setLength(0);
				}
				else {
					if(runbuff.length()==what.length() && runbuff.length()>0) {
						ret.append(runbuff.charAt(0));
						runbuff.deleteCharAt(0);
					}
				}
			}

			ret.append(runbuff.toString());
		} catch(Exception ex) {
			//            ex.printStackTrace();
		}

		return ret.toString();
	}
	
	public static String getHeader(HttpServletRequest request, String ident) {
		StringBuffer ret = new StringBuffer();
		
		ret.append("****************\n");
    	Enumeration en = request.getHeaderNames();
    	while(en.hasMoreElements()) {
    		String n = en.nextElement().toString();
    		String v = request.getHeader(n);
    		
    		ret.append(ident+"::Header["+n+"] -> "+v+"\n");
    	}
    	ret.append("****************\n");
    	
    	return ret.toString();
	}

	public final static String urlEncode(String s) {
		if(s==null) return "";

		try {
			return java.net.URLEncoder.encode(s,"UTF-8");
		} catch(Exception ex) {
			ex.printStackTrace();
		}

		return "";
	}
	public final static String urlDecode(String s) {
		if(s==null) return "";

		try {
			return java.net.URLDecoder.decode(s,"UTF-8");
		} catch(Exception ex) {
			ex.printStackTrace();
		}

		return "";
	}

	public MyAction(MultiTypeRequest request, HttpServletResponse response) {
		this(request, response, false);
	}
	public MyAction(MultiTypeRequest request, HttpServletResponse response, boolean createHeader) {
		parameter[0] = new Vector<String>();
		parameter[1] = new Vector<String>();

		this.request = request;
		this.response = response;

		//        if(createHeader) {
			//            if(request.getParameter("xmlrequest")!=null && request.getParameter("xmlrequest").equals("true")) {
		//                encoding = "UTF-8";
		//                prepareOut(response, "text/xml;charset="+encoding);
		//            }
		//            else {
		////                response.addHeader("X-HEADADD", "2");
		//                prepareOut(response, "text/html;charset="+encoding);
		//            }
		//        }


		if(createHeader) {
			prepareOut(response, "text/html;charset="+encoding);
		}
	}

	public void conjoinParams(Vector<String>[] args) {
		Vector<String> s1 = args[0];
		Vector<String> s2 = args[1];

		//PATHINFO OVERRIDES get/post-args!!!
		for(int i=0; i<s1.size(); i++) {    		
			try {
				String v1=null;
				String n1 = s1.elementAt(i);//name
				if (s1.size() == s2.size() )
					v1 = s2.elementAt(i);//wahhluuhs 
				else
					break;
				int nn = parameter[0].indexOf(n1);
				if(nn >= 0) {
					parameter[1].set(nn, v1);
				}
				else {
					parameter[0].addElement(n1);
					parameter[1].addElement(v1);
				}
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	public final static Vector[] sortVectors(Vector[] toSort, int whichOneIsSorting) {
		Vector sortVector = (Vector)toSort[whichOneIsSorting].clone();

		Vector[] ret = new Vector[toSort.length];        

		Vector snums = new Vector();//kommen die indexe als string rein...

		while(snums.size() != sortVector.size()) {
			String least = null;
			int w = -1;
			for(int x=0;x<sortVector.size();x++) {
				if(!snums.contains(""+x)) {

					least = (String)sortVector.elementAt(x);
					w = x;

					//System.out.println("found free at "+w);
					break;
				}
			}

			if(w==-1) {
				break;
			}

			for(int i=0;i<sortVector.size();i++) {
				String me = (String)sortVector.elementAt(i);
				if(!snums.contains(""+i)) {
					if(me.compareTo(least) <= 0) {
						//System.out.println("true");
						w = i;
						least = me;
					}
					else {
						//System.out.println("false");
					}
				}
				else {
					//System.out.println("snums contains "+w);
				}
			}

			//System.out.println("added "+w+" to snums");
			snums.addElement(""+w);
			//sortVector.removeElementAt(w);
		}

		//System.out.println("sortVector.size(): "+sortVector.size());
		//System.out.println("snums.size(): "+snums.size());

		for(int i=0;i<ret.length;i++) {
			ret[i] = new Vector();
		}

		for(int i=0;i<toSort[0].size();i++) {
			int grabAt = Integer.parseInt((String)snums.elementAt(i));
			for(int x=0;x<ret.length;x++) {
				ret[x].addElement(toSort[x].elementAt(grabAt));
			}
		}

		return ret;
	}

	public final static String getStringXML(Element e) {
		return getStringXML(e, "UTF-8");

	}
	public final static String getStringXML(Element e, String encoding) {
		org.jdom.output.Format f = org.jdom.output.Format.getCompactFormat();
		f.setEncoding(encoding);
		XMLOutputter xout = new XMLOutputter(f);

		return xout.outputString(e);

	}
	public final static String getCuteXML(Element e) {
		org.jdom.output.Format f = org.jdom.output.Format.getPrettyFormat();
		f.setEncoding("UTF-8");
		XMLOutputter xout = new XMLOutputter(f);

		return xout.outputString(e);

	}


	public abstract void performAction(String reqAddress, VelocityContext c) throws Exception;

	public void performAllAction() throws Exception {
		//String clienti p = request.getRemoteAddr();
		try {
			Enumeration<String> e = request.getParameterNames();            
			while(e.hasMoreElements()) {
				String schnauze = e.nextElement();
				if(request.getParameter(schnauze).length()!=0) {
					parameter[0].addElement(schnauze);
				}
			}

			for(int i=0; i<parameter[0].size(); i++) {
				parameter[1].addElement(request.getParameter((String)parameter[0].elementAt(i)));
			}

		} catch(Exception e) {
			System.out.println("Fehler: "+e.getMessage()); 
			//this.errortext=e.getMessage();
			e.printStackTrace();
		}

		user = new ActiveUser();
		//TODO HT 26.08.2010 - hier jetzt aus den post-daten den user ziehen...
		
		
//		Cookie[] cooked = request.getCookies();
//		if(cooked!=null) {
//			for(int z=0;z<cooked.length;z++) {
//				Cookie c = cooked[z];
//				if(c.getName().equals("sessionid")) {
//					user.sessionid = c.getValue();		
//					break;
//				}
//			}
//		}
		
		long aff = 333; //default "nonsense"-affiliateid 
			
//		if(isset("sessionid") && !gimmeValueOf("sessionid").equals("null")) {
//			user.sessionid = gimmeValueOf("sessionid");
//			aff = Long.parseLong(user.sessionid.substring(user.sessionid.indexOf("-")+1));
//			user.affiliateid = aff;
//		}
//		
//		if(user.sessionid == null) {
//			aff = Long.parseLong(gimmeValueOf("affiliateid"));
//			
//			user.sessionid = SessionKeyGenerator.getInstance().generateTimedKeyString(SessionKeyGenerator.keylength)+"-"+aff;
//			user.affiliateid = aff;//wirklich ein long!
//		}
		
//		try {
//			Cookie ccc = new Cookie("sessionid", user.sessionid);
//			ccc.setMaxAge(60*60*24*5);//5 tage in sekunden...
//			response.addCookie(ccc);
//		} catch(Exception ex) {
//			ex.printStackTrace();
//		}
//		
//		if(user.sessionid != null) {
//			user.sessiondata = ActiveUser.getSessionFromDB(user.sessionid);
//			//System.out.println("sessiondata:"+sessiondata);
//			if(user.sessiondata != null && user.sessiondata.get("user_userid") != null){
//				user.loggedin = user.sessiondata.get("user_loggedin").toString().indexOf("t")==0;
//
//				if(user.loggedin) {
////					MandantUser u = MandantUser.byId(Long.parseLong((String)user.sessiondata.get("user_userid")));
////					user.user = u;
//					//Mandant m = u.getMandant();
//				}
//
//				ActiveUser.updateSessionData(user); //, sessiondata, loggedin, sessionid);
//				//ActiveUser.assignDBSessionToUser(user.sessionid, u.getUserid()); //HT 02.04.2009 - hier nicht nötig. wird ja beim login gemacht.
//			}
//		}
	}
	
	public void makeOutput(Template t, VelocityContext c) throws Exception {        
		if(outStream == null) {
			prepareOut(response, "text/html; charset="+encoding);
		}
		
		OutputStreamWriter ow = new OutputStreamWriter(outStream, encoding);
		
		t.merge(c, ow);
		
		ow.flush();
	}   

	public final String gimmeValueAt(int index) {
		if(index < parameter[1].size()) {
			return (String)parameter[1].elementAt(index) ;
		}
		return null;
	}

	public final int gimmeParameterCount() {
		return parameter[0].size() ;
	}

	public final String gimmeNameAt(int index) {
		if(index < parameter[0].size())
			return (String)parameter[0].elementAt(index) ;

		return null;
	}

	public final String gimmeValueOf(String name) {
		for(int i=0;i<parameter[0].size();i++) {
			if(name.equals((String)parameter[0].elementAt(i))) {                                
				return (String)parameter[1].elementAt(i) ;                
			}
		}
		//return "";
		return null;
	}
	
	public final boolean isset(String name) {
		for(int i=0;i<parameter[0].size();i++) {
			if(name.equals((String)parameter[0].elementAt(i))) {                                
				return true;
			}
		}
		return false;
	}

	public final void sendRedirect(String wohin) {
		try {
			response.sendRedirect(wohin);
		} catch (Exception e) {
			try {
				sendJavaRedirect(wohin);
			}catch(Exception e2) {
				e.printStackTrace();
				e2.printStackTrace();
			}
			e.printStackTrace();
		}
	}


	public final void sendJavaRedirect(String wohin) throws Exception {
		outStream.write(("<script type=\"text/javascript\">location.href=\""+wohin+"\"</script>redirecting to: "+wohin).getBytes());
	}

	public final void prepareOut(HttpServletResponse response, String mime) {
		long ll = System.currentTimeMillis() - 1000*60*60*24;
//		if(!response.containsHeader("expires")) {
//			response.addDateHeader("expires", ll);//5 sekunden
//		}
		if(!response.containsHeader("Last-Modified")) {
			response.setDateHeader("Last-Modified", ll);
		}
		if(!response.containsHeader("Date")) {
			response.setDateHeader("Date", ll);
		}

		response.setContentType(mime);
		response.setHeader("Connection", "close");

		try  {
			outStream = response.getOutputStream();
		} catch(Exception ex) {
			ex.printStackTrace();
		}        
	}


	public final static void printHashtable(Hashtable<Object, Object> hash) {
		Enumeration<Object> en = hash.keys();
		while(en.hasMoreElements()) {
			Object key = en.nextElement();
			Object value = hash.get(key);

			System.out.println(key.toString()+": "+value.toString());
		}
	}

	public static String dropParameterFromURLString(String url, String valuename) {
		String ret = url;

		while(ret.indexOf(valuename+"=")>=0) {
			String s1 = ret.substring(0,ret.indexOf(valuename+"="));
			String s2 = ret.substring(ret.indexOf(valuename+"=") + (valuename+"=").length());

			if(s2.indexOf("&amp;")>=0) {
				s2 = s2.substring(s2.indexOf("&amp;")+5);

				//s1 = s1.substring(0, s1.length()-1);//& abschneiden...
			}
			else if(s2.indexOf("&")>=0) {
				s2 = s2.substring(s2.indexOf("&")+1);
			}
			else {
				s2 ="";
			}

			ret = s1 + s2;
		}

		return ret;
	}

	public static String setParameterInURL(String url, String valuename, String value) {
		String ret = dropParameterFromURLString(url, valuename);

		if(ret.indexOf('?')>=0) {
			ret += "&"+valuename+"="+urlEncode(value);
		}
		else {
			ret += "?"+valuename+"="+urlEncode(value);
		}

		return ret;
	}
	
	 
	public static String dropParameterFromURL(String url, String parameter) {
		if(url.indexOf('/'+parameter+'/')>=0){
			int startpos =url.indexOf("/"+parameter+"/");
			String sub = url.substring(startpos+("/"+parameter+"/").length());
			int endpos;
			if(sub.indexOf("/")>0){
				//falls weitere parameter vorhanden sind
				System.out.println("next / at - " + sub.indexOf("/"));
				endpos = sub.indexOf("/") + startpos + ("/"+parameter+"/").length();
			}
			else {
				//ansonsten bis zum ende des strings
				endpos=sub.length() + startpos + ("/"+parameter+"/").length();
			}
			String newurl=url.substring(0,startpos);
			newurl+=url.substring(endpos);
			return newurl;
		}
		return url;
	}
	
	public void writeXML(Element e) throws Exception {
		if(outStream == null) {
			prepareOut(response, "text/xml; charset="+encoding);
		}
		
		//TODO HT 26.08.2010 - oder auf den stream
		
		BufferedOutputStream bout = new BufferedOutputStream(outStream);		
		OutputStreamWriter ow = new OutputStreamWriter(bout, encoding);
		
		System.out.println("MyAction :: writeXML :: start");
    	Format f = Format.getPrettyFormat();
    	f.setEncoding("UTF-8");
    	
    	XMLOutputter xout = new XMLOutputter(f);    	
    	xout.output(e, ow);
    	ow.flush();
    	ow.close();
    }
	
	public void mirrorParams(Element e, String reqAddress) {
		Vector<String> names = parameter[0];
		Vector<String> values = parameter[1];
		
		for(int i=0;i<names.size();i++) {
			Element r = new Element(names.elementAt(i));
			r.setText(values.elementAt(i));
			e.addContent(r);
		}
		
		e.addContent((new Element("requestaddress")).setText(reqAddress));
		e.addContent((new Element("requesttime")).setText(""+System.currentTimeMillis()));
	}
}

