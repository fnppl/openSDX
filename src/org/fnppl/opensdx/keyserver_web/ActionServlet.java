package org.fnppl.opensdx.keyserver_web;
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
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.*;

import javax.servlet.http.*;

import org.apache.velocity.*;
import org.apache.velocity.app.*;
import org.fnppl.opensdx.keyserver.KeyServerBackend;
import org.fnppl.opensdx.keyserver.PostgresBackend;
import org.fnppl.opensdx.security.Identity;
import org.fnppl.opensdx.security.KeyLog;
import org.fnppl.opensdx.security.MasterKey;
import org.fnppl.opensdx.security.OSDXKey;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;


@SuppressWarnings("serial")
public class ActionServlet extends HttpServlet {
    private static Object velocityCatch;   
    private static Element config = null;
    private static KeyServerBackend backend = null;
    
    public static Element getConfig() {
    	if (config == null) {
    		try  {
    			config = Document.fromStream(ActionServlet.class.getResourceAsStream("resources/config.xml")).getRootElement(); 
    			System.out.println("Config loaded from resources");
    		} catch (Exception ex) {
    			System.out.println("WARNING: config resource not found: using STATIC config!");
    			config = new Element("config");
    			String vmtemp = "/var/lib/tomcat6/webapps/osdx_keyserver/vm_templates";
    			System.out.println("vmtemplatepath: "+vmtemp);
    	    	config.addContent("vmtemplatepath",vmtemp);
    	 		Element eDB  = new Element("db");
    	 		eDB.addContent("name","jdbc:postgresql://localhost:5432/keyserverdb");
    	 		eDB.addContent("user","keyserver");
    	 		eDB.addContent("password","oo0oo_keyserverDBpassword_..Q..");
    	 		eDB.addContent("data_path","/home/neo/db_data");
    	 		config.addContent(eDB);	
    	 		
    		}
    	}
    	return config;
    }

    public static KeyServerBackend getBackend() {
    	if (backend==null) {
    		Element eDB = getConfig().getChild("db");
    		if (eDB!=null) {
    			File dataPath = null;
    			String sDP = eDB.getChildText("data_path");
    			if (sDP!=null && sDP.length()>0) {
    				dataPath = new File(sDP);
    				if (!dataPath.exists() || !dataPath.isDirectory()) {
    					dataPath = null;
    				}
    			}
    			if (dataPath!=null) {
    				System.out.println("data_path = "+dataPath.getAbsolutePath());
    			}
    			backend = PostgresBackend.init(eDB.getChildTextNN("user"), eDB.getChildTextNN("password"), eDB.getChildTextNN("name"), dataPath);
    		} else {
    			throw new RuntimeException("missing DB configuration");
    		}
    	}
    	return backend;
    }
 
    // Init
    public ActionServlet() throws Exception{        
        super();
    }
    
    // HEAD not allowed
    public void doHead(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    // doGet, doPost delegate to this method  
	@SuppressWarnings("unchecked")
	public void doPGU(HttpServletRequest request, HttpServletResponse response, String method) {
		initVelocity();
		
		//parse command and arguments from request path
    	String pathinfo = request.getPathInfo();
    	if(pathinfo == null) {
    		pathinfo = "";
    	}
    	
    	System.out.println("REQUEST: "+pathinfo);
    	
    	String cmd = null;
    	
    	StringTokenizer kk = new StringTokenizer(pathinfo, "/");
    	int i = 0;
    	Vector[] args = {new Vector<String>(), new Vector<String>()};
    	if(kk.countTokens()!=0) {
        	cmd = kk.nextToken();
	    	while(kk.hasMoreTokens()) {
	    		String kkk = URLDecoder.decode(kk.nextToken());
	    		System.out.println("["+method+"] PATHINFO["+i+"]: "+kkk);
	    		args[i%2].add(kkk);
	    		i++;
	    	}
		}
        
        // Default-cmd == index
        if(cmd==null || cmd.equals("")) {
        	cmd="index";
        }

        if("keyinfo".equals(cmd)){
        	handleKeyInfo(request, response, method, cmd, args);
        }
        else if("keygraph".equals(cmd)){
        	handleKeyGraph(request, response, method, cmd, args);
        }
        else  if("index".equals(cmd)){
    		RenderVelocityAction action = new RenderVelocityAction(request, response, method, "index.vm");
    		handleAction(action, cmd, args, request);
        }
        else if("echo".equals(cmd)){
        	RenderVelocityAction action = new RenderVelocityAction(request, response, method, "echo.vm");
        	handleAction(action, cmd, args, request);
        }
        else if ("ajax_nodes_edges".equals(cmd)) {
        	
        	Element res = new Element("nodes_edges");
        	//add key
			String keyid = getValueString("keyid", args);
			if (keyid!=null) {
				System.out.println("ajax_nodes_edges:: keyid="+keyid);
				Vector<String> keyids = new Vector<String>();
				//key basics
				OSDXKey key = getBackend().getKey(keyid);
				if (key!=null) {
					res.addContent(key.getSimplePubKeyElement());
					keyids.add(key.getKeyID());
					
					//add keylogs and fromKeys
					Vector<KeyLog> logs = getBackend().getKeyLogsToID(key.getKeyID());
					if (logs!=null && logs.size()>0) {
						for (KeyLog l : logs) {
							if (!keyids.contains(l.getKeyIDFrom())) {
								OSDXKey fromkey = getBackend().getKey(l.getKeyIDFrom());
								if (fromkey!=null) {
									res.addContent(fromkey.getSimplePubKeyElement());
									keyids.add(fromkey.getKeyID());
								}	
							}
							res.addContent(l.toElement(false));
						}
					}
				}
			}
            response.setContentType("text/xml");
			try {
				PrintWriter out = response.getWriter();
	            out.println(Document.buildDocument(res).toStringCompact());
	            out.flush();
	            out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }
	
	private String getValueString(String key, Vector[] args) {
		Vector<String> keys = args[0];
		for (int i=0;i<keys.size();i++) {
			if (keys.get(i).equals(key)) {
				return (String)args[1].get(i);
			}
		}
		return null;
	}
	 

	private void handleKeyGraph(HttpServletRequest request, HttpServletResponse response, String method, String cmd, Vector[] args) {
		
		RenderVelocityAction action = new RenderVelocityAction(request, response, method, "keygraph.vm") {
			@Override
			protected void process() {
				
				HttpSession session = request.getSession(true);
				UserSession sess = (UserSession)session.getAttribute("usersession");
				if (sess==null) {
					//init a new session
					sess = new UserSession();
					sess.keys = new Vector<OSDXKey>();
					sess.logs = new Vector<KeyLog>();
				}
				
				//add a key
				String keyid = getParamString("addkeyid");
				if (keyid!=null) {
					//key basics
					OSDXKey key = getBackend().getKey(keyid);
					if (key!=null && sess.getKey(key.getKeyID())==null) {
						sess.keys.add(key);
						
					}
				}
				
			}
		};
		handleAction(action, cmd, args, request);
	}

	private void handleKeyInfo(HttpServletRequest request, HttpServletResponse response, String method, String cmd, Vector[] args) {
		RenderVelocityAction action = new RenderVelocityAction(request, response, method, "keyinfo.vm") {
			@Override
			protected void process() {        		
				//get key
				String keyid = getParamString("keyid");
				if (keyid==null) {
					putObject("key_found", false);
					putObject("keyowner_found", false);
				} else {
					//key basics
					OSDXKey key = getBackend().getKey(keyid);
					if (key==null) {
						putObject("key_found", false);
						putObject("keyowner_found", false);
					} else {
						putObject("key_found", true);
						putObject("key",key);
						
						//key owner
						Identity owner = null;
						if (key.isMaster()) {
							Vector<Identity> ids = ((MasterKey)key).getIdentities();
							if (ids!=null && ids.size()>0) {
								owner = ids.lastElement();
							}
						} else {
							//TODO get id of masterkey of subkey 
						}
						if (owner!=null) {
							putObject("keyowner_found", true);
		    				putObject("keyowner",owner);
						} else {
							putObject("keyowner_found", false);
						}
						
						//keylogs
						Vector<KeyLog> logs = getBackend().getKeyLogsToID(keyid);
						if (logs==null || logs.size()==0) {
							putObject("keylogs_found", false);
						} else {
							putObject("keylogs_found", true);
							putObject("keylogs",logs);
							for (KeyLog kl : logs) {
								System.out.println("KeyLog:"+kl.getKeyIDFrom()+", "+kl.getActionDatetimeString()+", "+kl.getAction());
							}
						}
					}
					
				}
			}
		};
		handleAction(action, cmd, args, request);
	}
	
	@SuppressWarnings("unchecked")
	private void handleAction(RenderVelocityAction action, String cmd, Vector[] args, HttpServletRequest request) {
		 if(action != null) {
	        	//add parameter from path
	    		for (int p=0;p<args[0].size();p++) {
	    			 action.putParam((String)args[0].get(p), (String)args[1].get(p));
	    		}
	    		
	        	VelocityContext c = new VelocityContext();        	
	        	try {
	                c.put("action",action);
	                c.put("encoding", action.encoding);
	                c.put("cmd", cmd);
	                
	                //c.put("au", action.user);
	                
	                c.put("reqaddress", request.getRemoteAddr());
	                c.put("scheme", request.getScheme());
	                c.put("querystring",request.getQueryString());
	                if(c.get("querystring") == null) {
	                    c.put("querystring","");
	                }
	                
	                String requesturl = request.getRequestURL().toString();
	                
	                //c.put("mid", ma.user.mandantid);
	                c.put("request", request);
	                c.put("requesturl", requesturl);
	                c.put("server", request.getServerName());
	                c.put("port", request.getServerPort());
	                
	                String ref = request.getHeader("Referer");                
	                if(ref == null) { 
	                	ref="#"; 
	                }             
	                c.put("referer", ref);
	                
	                //process and render template 
	        		action.process(c);
	        		
	        	} catch(Exception ex) {
	        		ex.printStackTrace();
//	        		action.makeErrorOutput("Internal Error", null, c);    
	        	}
	        } 
	}
    
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
    	doPGU(request, response, "GET");
    }
    
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
    	doPGU(request, response, "POST");
    }
	
	 // Velocity init
    public static void initVelocity() {
    	if(velocityCatch == null) {
    		System.out.println("init VM");
        	velocityCatch = new Object();

        	Properties props = new Properties();
        	props.setProperty("input.encoding", "UTF-8");
        	props.setProperty("output.encoding", "UTF-8");
        	
        	if(getConfig().getChild("vmtemplatepath")!=null) {
                props.setProperty("file.resource.loader.path", getConfig().getChildText("vmtemplatepath"));
        	}
        	if(System.getProperty("vmtemplatepath")!=null) {
        		props.setProperty("file.resource.loader.path", System.getProperty("vmtemplatepath"));
        		//Velocity.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, System.getProperty("vmtemplatepath"));//oder new FIle()?
        	}
        	System.out.println("vmtemplatepath="+props.getProperty("vmtemplatepath"));
            try {
            	Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM, VeloLog.getInstance());
            	Velocity.init(props);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}

