package org.fnppl.opensdx.ftp_bridge;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletResult;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
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
////			public FtpletResult onLogin(FtpSession session, FtpRequest request) throws FtpException, IOException {
////				System.out.println("Login event: "+session.getUserArgument());
////				return super.onLogin(session, request);
////			}
//			
//			public FtpletResult onMkdirStart(FtpSession session, FtpRequest request) throws FtpException, IOException {
//				System.out.println("start mkdir: "+request.getArgument());
//				FtpletResult result = null;
//				
//				return result;
//			}
//			public FtpletResult onMkdirEnd(FtpSession session, FtpRequest request) throws FtpException, IOException {
//				
//				System.out.println("end mkdir: "+request.getArgument());
//				//return super.onMkdirStart(session, request);
//				FtpletResult result = null;
//				
//				return result;
//			}
//			
//		};
//		Map<String,Ftplet> ftplets = new HashMap<String,Ftplet>();
//		ftplets.put("myhook",hook);
//		serverFactory.setFtplets(ftplets);
		
		final OSDXFileSystemManager fsManager = new OSDXFileSystemManager();
		//fsManager.readConfig(); //TODO
		
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
