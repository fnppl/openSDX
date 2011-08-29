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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.AuthorizationRequest;
import org.apache.ftpserver.ftplet.DefaultFtplet;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletResult;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.SaltedPasswordEncryptor;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

public class FTPBridge {

	public static void startFTPBridge() {
		
		FtpServerFactory serverFactory = new FtpServerFactory(); 
				
		//user managment
		PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
		userManagerFactory.setPasswordEncryptor(new SaltedPasswordEncryptor());
		UserManager userManager = userManagerFactory.createUserManager();

		//add test user
//		BaseUser user = new BaseUser();
//		user.setName("test");
//		user.setPassword("abcdefghi");
//		user.setHomeDirectory(new File(System.getProperty("user.home")).getAbsolutePath());
//		List<Authority> auths = new ArrayList<Authority>();
//		auths.add(new WritePermission());
//		user.setAuthorities(auths);
//		try {
//			userManager.save(user);
//		} catch (FtpException e1) { 
//			e1.printStackTrace();
//		}
		
		ListenerFactory listenerFactory = new ListenerFactory();
		listenerFactory.setPort(2221);
		serverFactory.addListener("default", listenerFactory.createListener());
		serverFactory.setUserManager(userManager);

		final OSDXFileSystemManager fsManager = new OSDXFileSystemManager();
		Vector<User> users = fsManager.readConfig();
		for (User user : users) {
			try {
				userManager.save(user);
			} catch (FtpException e1) { 
				e1.printStackTrace();
			}
		}
		
		//hook in via Ftplet
		Ftplet hook = new DefaultFtplet() {
			public FtpletResult beforeCommand(FtpSession session, FtpRequest request) throws FtpException, IOException {
				System.out.println("beforeCommand: "+request.getCommand());
				if (session.getFileSystemView() instanceof OSDXFileSystemView) {
					OSDXFileSystemView v = (OSDXFileSystemView)session.getFileSystemView();
					v.noop(); //THIS WILL TEST IF THE CONNECTION IS STILL ALIVE
					if (!v.isConnected()) {
						System.out.println("osdx filetransfer client NOT connected.");
						System.out.println("trying to reconnect...");
						boolean ok = v.reconnect();
						if (ok)  {
							System.out.println("re-connected.");
						} else {
							System.out.println("re-connection failed.");
						}
					} else {
//						if (request.getCommand().equals("NOOP")) {
//							boolean ok = v.noop();
//						}
					}
				}
				return super.beforeCommand(session, request);
			}
			
		};
		Map<String,Ftplet> ftplets = new HashMap<String,Ftplet>();
		ftplets.put("myhook",hook);
		serverFactory.setFtplets(ftplets);
		
	
		FileSystemFactory fsfactory = new FileSystemFactory() {
			public FileSystemView createFileSystemView(User user) throws FtpException {
				return fsManager.getFileSystemView(user);
			}
		};

		serverFactory.setFileSystem(fsfactory);
		
		FtpServer server = serverFactory.createServer();
		
		try {
			server.start();
		} catch (FtpException ex) { 
			ex.printStackTrace();
		}
	}
	
	
	public static void startFTPServer() {
		
		FtpServerFactory serverFactory = new FtpServerFactory(); 
				
		//user managment
		PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
		userManagerFactory.setPasswordEncryptor(new SaltedPasswordEncryptor());
		UserManager userManager = userManagerFactory.createUserManager();

		//add test user
		BaseUser user = new BaseUser();
		user.setName("test");
		user.setPassword("abcdefghi");
		user.setHomeDirectory(new File(System.getProperty("user.home")).getAbsolutePath());
		List<Authority> auths = new ArrayList<Authority>();
		auths.add(new WritePermission());
		user.setAuthorities(auths);
		try {
			userManager.save(user);
		} catch (FtpException e1) { 
			e1.printStackTrace();
		}
		
		ListenerFactory listenerFactory = new ListenerFactory();
		listenerFactory.setPort(2221);
		serverFactory.addListener("default", listenerFactory.createListener());
		serverFactory.setUserManager(userManager);

//		//hook in via Ftplet
//		Ftplet hook = new DefaultFtplet() {
//			public FtpletResult onLogin(FtpSession session, FtpRequest request) throws FtpException, IOException {
//				System.out.println("Login event: "+session.getUserArgument());
//				return super.onLogin(session, request);
//			}
//			
//		};
//		Map<String,Ftplet> ftplets = new HashMap<String,Ftplet>();
//		ftplets.put("myhook",hook);
//		serverFactory.setFtplets(ftplets);
		
//		final OSDXFileSystemManager fsManager = new OSDXFileSystemManager();
//		
//		FileSystemFactory fsfactory = new FileSystemFactory() {
//			public FileSystemView createFileSystemView(User user) throws FtpException {
//				return fsManager.getFileSystemView(user);
//			}
//		};
//		serverFactory.setFileSystem(fsfactory);
		
		FtpServer server = serverFactory.createServer();
		
		try {
			server.start();
		} catch (FtpException ex) { 
			ex.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		try {
			//startFTPServer();
			startFTPBridge();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
