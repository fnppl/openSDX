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



@SuppressWarnings("serial")
public class ActionServlet extends MyServlet {
    private static Object velocityCatch;   
    public static boolean maintenance = false;
    public static boolean allowsearchengines = false;
    
    // Init
    public ActionServlet() throws Exception{        
        super();                
    }
    
    // HEAD nich erlaubt
    public void doHead(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
    
    // Verlocity initialisieren
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
    
    // doGet, doPost delegieren an diese Methode weiter - siehe param method  
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
        // Default-cmd == index
        if(cmd==null || cmd.equals("")) {
        	cmd="index";
        }
        
//        String useragent = "";
//        Enumeration en = request.getHeaderNames();
//        while(en.hasMoreElements()) {
//            String n = (String)en.nextElement();
//            String v = request.getHeader(n);
//            if(n.toLowerCase().equals("user-agent")) {
//                useragent = v;
//            }
//        }
        
        // Prüfen ob es sich um einen Bot/eine Suchmaschine handelt und ggf. nichts ausliefern
        String useragent = request.getHeader("User-Agent");        
//        System.out.println("useragent: "+useragent);
        
        boolean detectedsearchengine = false;
        String ua = useragent.toLowerCase();
        for(int ui=0;ui<searchengines.size();ui++) {
            String se = (String)searchengines.elementAt(ui);
            if(ua.indexOf(se)>=0) {
                detectedsearchengine = true;
                System.out.println("SEARCHENGINEREQUEST :: "+cmd+" :: "+useragent);
                break;
            }
        }
        
        if(detectedsearchengine && !allowsearchengines) {
        	//throw new Exception("Search-Engine "+useragent+" not allowed here...");
    		System.err.println("Search-Engine "+useragent+" not allowed at all...");
        	return;
        }
        
        if("index".equals(cmd)){
            EchoPageAction epa = new EchoPageAction(request, response);
            epa.tmpl = "index.vm";
            epa.admin = false;
            ma = epa;
        }
        else if("echo".equals(cmd)){
            EchoPageAction epa = new EchoPageAction(request, response);        	
            epa.admin = false;
            ma = epa;
        }
        
        if(ma != null) {
        	ma.mode = method;
        	if(ma.needssessionid && detectedsearchengine) {
        		System.err.println("Search-Engine "+useragent+" not allowed here ("+cmd+"...");
            	return;
        	}
        	
        	VelocityContext c = new VelocityContext();        	
        	try {
        		ma.performAllAction(args);
        		
//        		ma.conjoinParams(args); //das hier fügt aus den per get/post übergebenen parametern die zu per pathinfo angegebenen hinzu
        		
        		for(int zi=0, to=ma.gimmeParameterCount();zi<to;zi++) {
                    c.put("param_"+ma.gimmeNameAt(zi), ma.gimmeValueAt(zi));
                }
        		
//        		c.put("broker", ObjectBroker.getInstance());
                c.put("ma",ma);
                c.put("encoding", ma.encoding);
                c.put("cmd", cmd);
                
                c.put("au", ma.user);
                
                c.put("reqaddress", request.getRemoteAddr());
                c.put("scheme", request.getScheme());
                c.put("querystring",request.getQueryString());
                if(c.get("querystring") == null) {
                    c.put("querystring","");
                }
                
                String requesturl = request.getRequestURL().toString();
//                System.out.println("RequestURL: "+requesturl+" from "+request.getRemoteAddr());
                
                c.put("mid", ma.user.mandantid);
                c.put("request", request);
                c.put("requesturl", requesturl);
                c.put("server", request.getServerName());
                c.put("port", request.getServerPort());
                
                String ref = request.getHeader("Referer");                
                if(ref == null) { 
                	ref="#"; 
                }             
                c.put("referer", ref);
                                
        		ma.performAction(request.getRemoteAddr(), c);
        	} catch(Exception ex) {
        		ex.printStackTrace();
//        		ma.makeErrorOutput("Ein interner Fehler ist aufgetreten", null, c);    
        	}
        } //ma!=null
    }
    
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
    	doPGU(request, response, "GET");
    }
    
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
    	doPGU(request, response, "POST");
    }
}

