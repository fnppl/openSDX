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
import java.util.*;

import javax.servlet.http.*;

import org.apache.velocity.*;
import org.apache.velocity.app.*;
import org.jdom.Element;

import org.fnppl.dbaccess.*;
import org.fnppl.opensdx.common.*;


	@SuppressWarnings("serial")
	public class ActionServlet extends MyServlet {
	    //private static WM staticWM;
	    private static Object velocityCatch;
	    //private static HashSet<MyAction> unfinished = new HashSet<MyAction>();
	    public static boolean maintenance = false;
	    
	    public ActionServlet() throws Exception{        
	        super();                
	    }
	    
	    public void doHead(HttpServletRequest request, HttpServletResponse response) {
	    	System.out.println((new Date())+"\n"+request.getRequestURL()+"\n"+MyAction.getHeader(request, "ActionServlet::doHead"));
	    	
	    	
	        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
//	        response.setStatus(HttpServletResponse.SC_OK);
//	        
//	        long ll = System.currentTimeMillis();
//	        response.setDateHeader("Date", ll);
//	        response.setHeader("Connection", "close");
//	        response.setDateHeader("expires", ll);
//	        response.setDateHeader("Last-Modified", ll);
//	        String cmd = request.getParameter("cmd");
//	        
//	        if(cmd==null) {
//	            cmd = "loginpage";
//	        }
//	        if(cmd.equals("rssmaster")) {
//	            response.setContentType("application/rss+xml;charset="+"UTF-8");
//	        }
//	        else {
//	            response.setContentType("text/html");
//	        }
	    }
	    
	    public static void initVM() {
	    	if(velocityCatch == null) {
	        	velocityCatch = new Object();

	        	Properties props = new Properties();
	        	props.setProperty("input.encoding", "UTF-8");
	        	props.setProperty("output.encoding", "UTF-8");
	        	
	        	if(config.getChild("vmtemplatepath")!=null) {
	                props.setProperty("file.resource.loader.path", config.getChildText("vmtemplatepath"));
	        	}
	        	if(System.getProperty("vmtemplatepath")!=null) {
	        		props.setProperty("file.resource.loader.path", System.getProperty("vmtemplatepath"));
	        		//Velocity.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, System.getProperty("vmtemplatepath"));//oder new FIle()?
	        	}
	        	
	            try {
	            	Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM, VeloLog.getInstance());
	            	Velocity.init(props);            	            	
	            	
	            }catch(Exception ex) {
	                ex.printStackTrace();
	            }
	        }
	    }
	    
		@SuppressWarnings("unchecked")
		public void doPGU(HttpServletRequest request_, HttpServletResponse response, String method) {
			MultiTypeRequest request = new MultiTypeRequest(request_);
			
			initVM();
	    	
	    	String pathinfo = request.getPathInfo();
	    	if(pathinfo == null) {
	    		pathinfo = "";
	    	}
	    	
	    	String cmd = null;
	    	
	    	StringTokenizer kk = new StringTokenizer(pathinfo, "/");
	    	int i = 0;
	    	Vector[] args = {new Vector<String>(), new Vector<String>()};
	    	if(kk.countTokens()!=0){
	        	cmd = kk.nextToken();
	        }
	    	while(kk.hasMoreTokens()) {
	    		String kkk = kk.nextToken();
	    		System.out.println("["+method+"] PATHINFO["+i+"]: "+kkk);
	    		args[i%2].add(kkk);
	    		i++;
	    	}
	        
	        MyAction ma = null;
	        if(cmd==null || cmd.equals("")) {
	        	cmd="index";
	        }
	        
	        System.out.println("MyAction::"+cmd+" from "+request.getRemoteAddr());
	        
	        if("onexml".equalsIgnoreCase(cmd)){
	            ma = new OneXMLAction(request, response);
	        }
	        
	        
	        if(ma != null) {
	        	ma.mode = method;        	
	        	VelocityContext c = new VelocityContext();        	
	        	try {
	        		System.out.println("ActionServlet::going to performAllAction on "+ma.getClass().getName());
	        		ma.performAllAction();
	        		System.out.println("ActionServlet::going to conjoinParams on "+ma.getClass().getName());
	        		ma.conjoinParams(args); //das hier fügt aus den per get/post übergebenen parametern die zu per pathinfo angegebenen hinzu
	        		
	        		for(int zi=0, to=ma.gimmeParameterCount();zi<to;zi++) {
	                    c.put("param_"+ma.gimmeNameAt(zi), ma.gimmeValueAt(zi));
	                }
	        		
	                c.put("ma",ma);
	                c.put("encoding", ma.encoding);
	                c.put("cmd", cmd);
//	                c.put("sessionid", ma.user.sessionid);//
	                c.put("reqaddress", request.getRemoteAddr());
	                c.put("scheme", request.getScheme());
	                c.put("querystring",request.getQueryString());
	                if(c.get("querystring") == null) {
	                    c.put("querystring","");
	                }
	                String requesturl = request.getRequestURL().toString();

	                c.put("request", request);
	                c.put("requesturl", requesturl);
	                c.put("server", request.getServerName());
	                c.put("port", request.getServerPort());
	                
	                ma.server = request.getServerName();
	                ma.port = request.getServerPort();
	                
	                String ref = request.getHeader("Referer");
	                
	                if(ref == null) { 
	                	ref="#"; 
	                }             
	                c.put("referer", ref);
	                
	                System.out.println((new Date())+"ActionServlet::going to performAction on "+ma.getClass().getName());
	        		ma.performAction(request.getRemoteAddr(), c);
	        		System.out.println((new Date())+"ActionServlet::performed on "+ma.getClass().getName());
	        	} catch(Exception ex) {
	        		ex.printStackTrace();
//	        		ma.makeErrorOutput("Ein interner Fehler ist aufgetreten", null, c);  
	        		
	        		Element reqresp = new Element("reqresp");
	        		int code = MyAction.RESPONSECODE_FAIL; //ok
	            	
	            	Element req = new Element("request");
	            	reqresp.addContent(req);
	            	ma.mirrorParams(req, request.getRemoteAddr());
	            	
	            	Element resp = new Element("response");    	
	            	reqresp.addContent(resp);
	            	
	            	Element responsecode = new Element("responsecode");
	            	resp.addContent(responsecode);
	            	responsecode.setText(""+code);
	            	
	            	
	            	Element responsemessage = new Element("responsemessage");
	            	resp.addContent(responsemessage );
	            	responsemessage.setText(ex.getMessage());
	            	
	            	try {
	            		ma.writeXML(reqresp);
	            	}catch(Exception exx) {
	            		exx.printStackTrace();
	            	}
	        	}
	        } //ma!=null
	    }
	    
	    public void doGet(HttpServletRequest request, HttpServletResponse response) {
	    	doPGU(request, response, "GET");
	    }
	    
		public void doPost(HttpServletRequest request, HttpServletResponse response) {
	    	doPGU(request, response, "POST");
	    }

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
//		//TNMOB-17
//		//für lokales geshizzle:
//		ActionServlet.initOFFDB();//initialisiert die db-connection mit dem "dblocal"-hostname aus resources/config.xml
//		
//		DBResultSet Rs = BalancingConnectionManager.execQuery("select count(*) from tnuser");
//		//u dont have to check-in and check-out connections - they are just taken for ONE call... but please make sure not to use prepare-statments!!!!
//		System.out.println("User in DB: "+Rs.height());
//		
//		int r = BalancingConnectionManager.execUpdate("update tnuser set username='root' where username='root");
//		
//		//auf dem server (production)
////		ActionServlet.initLoadDB()://normales geshizzle
	}

}
