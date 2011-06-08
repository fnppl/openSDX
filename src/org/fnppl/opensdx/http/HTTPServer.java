package org.fnppl.opensdx.http;


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

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;

import org.fnppl.opensdx.gui.DefaultMessageHandler;
import org.fnppl.opensdx.gui.MessageHandler;
import org.fnppl.opensdx.http.HTTPServerRequest;
import org.fnppl.opensdx.http.HTTPServerResponse;
import org.fnppl.opensdx.keyserver.KeyServerResponse;
import org.fnppl.opensdx.security.*;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;

public abstract class HTTPServer {
	
	
//	protected String host = "localhost";
	protected int port = -1;
	protected String prepath = "";
	private int maxRequestsPerMinute = 100;
	private int maxThreadCount = 30;
	protected InetAddress address = null;

	private HashMap<String, int[]> ipRequests = new HashMap<String, int[]>();
	private HashMap<String, Thread> currentWorkingThreads = new HashMap<String, Thread>();	
	
	protected OSDXKey signingKey = null;
	private String serverid = "serverid";
	
	
	public abstract String getServerID();
	public abstract void readConfig();
	public abstract HTTPServerResponse prepareResponse(HTTPServerRequest request) throws Exception;
	
	public HTTPServer() {
			
	}


	public void exit() {
		System.exit(0);
	}
	
	public void handleSocket(final Socket s) throws Exception {
		Thread t = new Thread() {
			public void run() {
				String threadID = null;
				try {
					InetAddress addr = s.getInetAddress();
					String remoteIP = addr.getHostAddress();
					int remotePort = s.getPort();
					// check on *too* many requests from one ip
					int[] rc = ipRequests.get(remoteIP);
					if (rc==null) {
						ipRequests.put(remoteIP, new int[]{1});
					} else {
						rc[0]++;
						//System.out.println("anz req: "+rc[0]);
						if (rc[0]>maxRequestsPerMinute) {
							if (rc[0]<=100) {
								System.out.println("WARNING: too many requests ("+rc[0]+") from ip: "+remoteIP);
							}
							return;
						}
					}
					threadID = remoteIP+remotePort;
					currentWorkingThreads.put(threadID,this);
					
					try {
						InputStream _in = s.getInputStream();
						BufferedInputStream in = new BufferedInputStream(_in);
						HTTPServerRequest request = HTTPServerRequest.fromInputStream(in, addr.getHostAddress());
						HTTPServerResponse response = prepareResponse(request);
						
						System.out.println("ServerSocket  | ::response ready");
						if (response == null) {
							//send error
							response = new HTTPServerResponse(serverid);
							response.setRetCode(400, "BAD REQUEST");
						}
						if (response != null) {
							System.out.println("SENDING THIS::");response.toOutput(System.out);System.out.println("::/SENDING_THIS");
							
							OutputStream out = s.getOutputStream();
							BufferedOutputStream bout = new BufferedOutputStream(out);
							response.toOutput(bout);
							bout.flush();
							bout.close();
						} 
						else {
							Exception ex = new Exception("RESPONSE COULD NOT BE CREATED");
							throw ex;
						}
					} catch(Exception ex2) {
						ex2.printStackTrace();
					}
					currentWorkingThreads.remove(threadID);
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		};
		t.start();
	}
	
	
	public void startService() throws Exception {
		System.out.println("Starting Server at "+address.getHostAddress()+" on port " + port +"  at "+SecurityHelper.getFormattedDate(System.currentTimeMillis()));
		ServerSocket so = new ServerSocket(port);
		Thread requestMonitor = new Thread() {
			public void run() {
				while (true) {
					updateIPRequestCounter();
					try {
						Thread.sleep(60000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		requestMonitor.start();
		while (true) {
			try {
				while (currentWorkingThreads.size()>maxThreadCount) {
					Thread.sleep(100);
				}
				final Socket me = so.accept();
				handleSocket(me);
			} catch (Exception ex) {
				ex.printStackTrace();
				Thread.sleep(250);// cooldown...
			}
		}
		//System.out.println("Service closed at "+SecurityHelper.getFormattedDate(System.currentTimeMillis()));
	}
	
	private void updateIPRequestCounter() {
		try {
			Vector<String> remove = new Vector<String>();
			for (Entry<String, int[]> e : ipRequests.entrySet()) {
				int[] v = e.getValue();
				v[0] -= maxRequestsPerMinute;
				//System.out.println(e.getKey()+ "  v[0] = "+v[0]);
				if (v[0] < 0) {
					remove.add(e.getKey());
				}
			}
			for (String r : remove) {
				ipRequests.remove(r);
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}

