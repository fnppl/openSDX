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
import java.math.BigInteger;
import java.net.*;
import java.util.*;

import org.fnppl.opensdx.http.HTTPClientRequest;
import org.fnppl.opensdx.http.HTTPClientResponse;
import org.fnppl.opensdx.security.*;
import org.fnppl.opensdx.tsaserver.*;
import org.fnppl.opensdx.xml.*;

public class HTTPClient {
	
	public static boolean DEBUG = false;
	
	protected Socket socket = null;
	private long timeout = 2000;
	protected String host = null;
	private int port = -1;
	protected String message = null;
	public OutputStream log = null;
	
	public final static String ERROR_NO_RESPONSE = "ERROR: server does not respond.";
	
	
	public HTTPClient(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public boolean connect() throws Exception {
		socket = new Socket(host, port);
		if (socket.isConnected()) {
			//System.out.println("Connection established.");
			return true;
		} else {
			System.out.println("ERROR: Connection to server could NOT be established!");
			return false;
		}
	}
	
	public void writeLog(HTTPClientRequest req, HTTPClientResponse resp, String name) {
		if (log!=null) {
			try {
				log.write(("--- REQUEST "+name+" ----------\n").getBytes());
				req.toOutput(log);
				log.write(("--- END of REQUEST "+name+" ---\n").getBytes());
				if (resp == null) {
					log.write(("-> --- "+ERROR_NO_RESPONSE+" ---\n").getBytes());
				} else {
					log.write(("\n--- RESPONSE "+name+" ----------\n").getBytes());
					resp.toOutput(log);
					log.write(("--- END of RESPONSE "+name+" ---\n").getBytes());
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void close() throws Exception {
		if (socket != null)
			socket.close();
	}
	
	public String getMessage() {
		return message;
	}
	public HTTPClientResponse send(HTTPClientRequest req) throws Exception {
		if (!connect()) {
			RuntimeException l = new RuntimeException("ERROR: Can not connect to server.");
			l.printStackTrace();
			throw l;
		}
		
		if (DEBUG) {
			System.out.println("OSDXKeyServerClient | start "+req.getURI());
			
			System.out.println("--- sending ---");
			req.toOutput(System.out);
			System.out.println("\n--- end of sending ---");
		}
		
		req.send(socket);
		
		//processing response
	    //System.out.println("OSDXKeyServerClient | waiting for response");
	    BufferedInputStream bin = new BufferedInputStream(socket.getInputStream());
	    
	    HTTPClientResponse re = HTTPClientResponse.fromStream(bin, timeout);
	    close();
	    
	    if(re == null) {
	    	throw new RuntimeException("ERROR: Server does not respond.");
	    }
	    return re;
	}
	
	public HTTPClientResponse sendPut(HTTPClientPutRequest req) throws Exception {
		if (!connect()) {
			RuntimeException l = new RuntimeException("ERROR: Can not connect to server.");
			l.printStackTrace();
			throw l;
		}
		req.send(socket);
		
		//processing response
	    //System.out.println("OSDXKeyServerClient | waiting for response");
	    BufferedInputStream bin = new BufferedInputStream(socket.getInputStream());
	    
	    HTTPClientResponse re = HTTPClientResponse.fromStream(bin, timeout);
	    close();
	    
	    if(re == null) {
	    	throw new RuntimeException("ERROR: Server does not respond.");
	    }
	    return re;
	}
	
	protected boolean checkResponse(HTTPClientResponse resp) {
		if (resp==null || resp.status == null) {
			message = ERROR_NO_RESPONSE;
			return false;
		}
		if (resp.status.endsWith("OK"))	return true;
		if (resp.hasErrorMessage()) {
			message = resp.getErrorMessage();
		}
		return false;
	}

	public String getHost() {
		return host;
	}
	
}
