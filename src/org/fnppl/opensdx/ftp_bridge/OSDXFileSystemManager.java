package org.fnppl.opensdx.ftp_bridge;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.AuthorizationRequest;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.fnppl.opensdx.security.OSDXKey;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;

public class OSDXFileSystemManager {

	private File configFile = new File("ftp_bridge_config.xml"); 
	private File alterConfigFile = new File("src/org/fnppl/opensdx/ftp_bridge/resources/ftp_bridge_config.xml"); 
	
	private HashMap<String, OSDXUser> users = new HashMap<String, OSDXUser>();
	private HashMap<User,FileSystemView> views = new HashMap<User, FileSystemView>();
	
	public OSDXFileSystemManager() {
		
			
		
	}
	
	public Vector<User> readConfig() {
		Vector<User> userlist = new Vector<User>();
		
		try {
			if (!configFile.exists()) {
				configFile = alterConfigFile;
			}
			if (!configFile.exists()) {
				System.out.println("Sorry, uploadserver_config.xml not found.");
				System.exit(0);
			}
			Element root = Document.fromFile(configFile).getRootElement();
			
			Vector<Element> userConfig =  root.getChildren("user");
			if (users == null) return null;
		
			for (Element e : userConfig) {
				try {
					BaseUser user = new BaseUser();
					String ftp_username = e.getChildTextNN("ftp_username"); 
					user.setName(ftp_username);
					user.setPassword(e.getChildTextNN("ftp_password"));
					user.setHomeDirectory(new File(System.getProperty("user.home")).getAbsolutePath());
					List<Authority> auths = new ArrayList<Authority>();
					auths.add(new WritePermission());
					user.setAuthorities(auths);
					
					OSDXKey mysigning = OSDXKey.fromElement(e.getChild("keypair"));
					mysigning.unlockPrivateKey(e.getChildTextNN("password"));
					String username = e.getChildTextNN("username");
					
					OSDXUser c = new OSDXUser();
					c.host = e.getChildTextNN("host");
					c.port = Integer.parseInt(e.getChildTextNN("port"));
					c.prepath = e.getChildTextNN("prepath");
					c.signingKey = mysigning;
					c.username = username;
					
					System.out.println("adding user: "+ftp_username+" -> "+username+"::"+mysigning.getKeyID());
					users.put(ftp_username, c);
					userlist.add(user);
					
				} catch (Exception exIn) {
					exIn.printStackTrace();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return userlist;
	}
	
	public void closeSession(User user) {
		synchronized (o) {
			FileSystemView v = views.get(user);
			if  (v!=null) {
				v.dispose();
				views.remove(v);
			}
		}
	}
	
	public OSDXUser getUserClient(String userid) {
		return users.get(userid);
	}
	
	private Object o = new Object();
	
	public FileSystemView getFileSystemView(User user) {
		synchronized (o) {
			FileSystemView v = views.get(user);
			if (v==null) {
				System.out.println("user login: "+user.getName());
				OSDXUser client = getUserClient(user.getName());
				v = new OSDXFileSystemView(client);
				views.put(user, v);
			} else {
				if (!((OSDXFileSystemView)v).isConnected()) {
					System.out.println("user re-login: "+user.getName());
					OSDXUser client = getUserClient(user.getName());
					v = new OSDXFileSystemView(client);
					views.put(user, v);
				}
			}
			return v;
		}
	}
	
	
}
