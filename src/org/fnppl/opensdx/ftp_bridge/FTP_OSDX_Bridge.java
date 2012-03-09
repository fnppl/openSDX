package org.fnppl.opensdx.ftp_bridge;
/*
 * Copyright (C) 2010-2012 
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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Vector;

import org.fnppl.opensdx.security.OSDXKey;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;


public class FTP_OSDX_Bridge {
	
	private File configFile = new File("ftp_bridge_config.xml"); 
	private File alterConfigFile = new File("src/org/fnppl/opensdx/ftp_bridge/resources/ftp_bridge_config.xml"); 
	
	private int port = 2221;
	private HashMap<String, FTP_OSDX_BridgeUser> users = new HashMap<String, FTP_OSDX_BridgeUser>();
	
	
	public FTP_OSDX_Bridge() {
		readConfig();
	}
	
	public void readConfig() {
		users = new HashMap<String, FTP_OSDX_BridgeUser>();
		try {
			if (!configFile.exists()) {
				configFile = alterConfigFile;
			}
			if (!configFile.exists()) {
				System.out.println("Sorry, uploadserver_config.xml not found.");
				System.exit(0);
			}
			Element root = Document.fromFile(configFile).getRootElement();
			String sPort  = root.getChildText("ftp_server_port");
			if (sPort!=null) {
				try {
					port = Integer.parseInt(sPort); 
				} catch (Exception ex) {
					System.out.println("error in config: could not parse ftp_server_port: "+sPort);
				}
			}
			
			Vector<Element> userConfig =  root.getChildren("user");
			if (userConfig == null) return;
		
			for (Element e : userConfig) {
				try {
					String ftp_username = e.getChildTextNN("ftp_username"); 
					String ftp_password = e.getChildTextNN("ftp_password");
					OSDXKey mysigning = OSDXKey.fromElement(e.getChild("keypair"));
					mysigning.unlockPrivateKey(e.getChildTextNN("password"));
					String username = e.getChildTextNN("username");
					
					FTP_OSDX_BridgeUser c = new FTP_OSDX_BridgeUser();
					c.ftpusername = ftp_username;
					c.ftppassword = ftp_password;
					c.host = e.getChildTextNN("host");
					c.port = Integer.parseInt(e.getChildTextNN("port"));
					c.prepath = e.getChildTextNN("prepath");
					c.signingKey = mysigning;
					c.username = username;
					System.out.println("adding user: "+ftp_username+" -> "+username+"::"+mysigning.getKeyID());
					users.put(ftp_username, c);
				} catch (Exception exIn) {
					exIn.printStackTrace();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	public void startService() {
		try {
			System.out.println("Starting FTP-to-OSDXFiletransfer-Bridge on port "+port);
			ServerSocket s = new ServerSocket(port);
			while(true) {
				Socket incoming = s.accept();
				new FTP_OSDX_BridgeThread(incoming, this).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public FTP_OSDX_BridgeUser getUser(String username) {
		return users.get(username);
	}
	
	public static void main(String[] args) {
		new FTP_OSDX_Bridge().startService();
	}

}
