package org.fnppl.opensdx.securesocket;

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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Vector;
import java.util.Map.Entry;

import org.fnppl.opensdx.http.HTTPServerRequest;
import org.fnppl.opensdx.http.HTTPServerResponse;
import org.fnppl.opensdx.security.MasterKey;
import org.fnppl.opensdx.security.OSDXKey;
import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.uploadserver.UploadServer;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;

public class OSDXSocketServer {

	protected int port = -1;

	private OSDXKey mySigningKey = null;
	private OSDXSocketDataHandler dataHandler = null;


	public OSDXSocketServer(int port, OSDXKey mySigningKey) throws Exception {
		this.port = port;
		this.mySigningKey = mySigningKey;
		if (!mySigningKey.hasPrivateKey() && !mySigningKey.isPrivateKeyUnlocked()) {
			throw new Exception("signing key not accessable");
		}
	}
	
	public void setDataHandler(OSDXSocketDataHandler handler) {
		dataHandler = handler;
	}
	
	private Object o = new Object();
	
	public void startService() throws Exception {
		//System.out.println("Starting Server at "+address.getHostAddress()+" on port " + port +"  at "+SecurityHelper.getFormattedDate(System.currentTimeMillis()));
		ServerSocket so = new ServerSocket(port);
		while (true) {
			
			try {
				synchronized (o) {
					final Socket me = so.accept();
					OSDXSocketServerThread t = new OSDXSocketServerThread(me, mySigningKey, dataHandler);
					t.start();	
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				Thread.sleep(250);// cooldown...
			}
		}
	}
	
}

