package org.fnppl.opensdx.file_transfer;

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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Vector;

import org.fnppl.opensdx.securesocket.ClientSettings;
import org.fnppl.opensdx.securesocket.OSDXSocketDataHandler;
import org.fnppl.opensdx.securesocket.OSDXSocketSender;
import org.fnppl.opensdx.securesocket.OSDXSocketServer;
import org.fnppl.opensdx.securesocket.OSDXSocketServerThread;
import org.fnppl.opensdx.security.MasterKey;
import org.fnppl.opensdx.security.OSDXKey;
import org.fnppl.opensdx.security.OSDXMessage;
import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;

public class OSDXFileTransferServer implements OSDXSocketDataHandler {

	private OSDXSocketServer serverSocket;
	
	private File configFile = new File("osdxserver_config.xml"); 
	private File alterConfigFile = new File("src/org/fnppl/opensdx/file_transfer/resources/osdxfiletransferserver_config.xml"); 

	protected int port = -1;
	
	protected InetAddress address = null;

	private OSDXKey mySigningKey = null;
	
	private HashMap<OSDXSocketSender, FileTransferState> states = null;
	private HashMap<String, ClientSettings> clients = null; //client id := username::keyid
	
	
	public OSDXFileTransferServer(String pwSigning) {
		readConfig();
		try {
			mySigningKey.unlockPrivateKey(pwSigning);
			serverSocket = new OSDXSocketServer(port, mySigningKey);
			serverSocket.setDataHandler(this);
			states = new HashMap<OSDXSocketSender, FileTransferState>();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void handleNewText(String text, OSDXSocketSender sender) {
		String command = text;
		String param = null;
		int ind = text.indexOf(' ');
		if (ind>0) {
			command = text.substring(0,ind);
			if (text.length()>ind+1) {
				param = text.substring(ind+1);
			}
		}
		command = command.toLowerCase();
		try {
			Method commandHandler = getClass().getMethod("handle_"+command, String.class, OSDXSocketSender.class);
			commandHandler.invoke(this, param, sender);
			
		} catch (NoSuchMethodException ex) {
			handle_command_not_implemented(command, param, sender);
		} catch (InvocationTargetException ex) {
			handle_command_not_implemented(command, param, sender);
		} catch (IllegalAccessException ex) {
			handle_command_not_implemented(command, param, sender);
		}
		
	}

	private FileTransferState getState(OSDXSocketSender sender) {
		FileTransferState s = states.get(sender);
		if (s==null) {
			s = new FileTransferState();
			System.out.println("looking for client key id: "+sender.getID());
			ClientSettings settings = clients.get(sender.getID());
			if (settings == null) {
				return null;
			}
			s.setRootPath(settings.getLocalRootPath());
			//s.setRootPath(new File(path_uploaded_files,sender.getID()));
			states.put(sender,s);
		}
		return s;
	}
	
	public void handleNewData(byte[] data, OSDXSocketSender sender) {
		//if data arrives, it must be a file
		FileTransferState state = getState(sender);
		if (state.getWriteFile() == null) {
			sender.sendEncryptedText("ERROR IN DATASTREAM :: PLEASE SEND PUT REQUEST FIRST");
		}
		else {
			try {
				File save = state.getWriteFile();
				System.out.println("Saving to file: "+save.getAbsolutePath());
				FileOutputStream fout = new FileOutputStream(save);
				fout.write(data);
				state.setWriteFile(null);
				sender.sendEncryptedText("ACK FILE UPLOAD");
			} catch (Exception e) {
				e.printStackTrace();
				sender.sendEncryptedText("ERROR WRITING TO FILE");
			}
		}
	}
	
	public void startService() {
		System.out.println("Starting Server at "+address.getHostAddress()+" on port " + port +"  at "+SecurityHelper.getFormattedDate(System.currentTimeMillis()));
		//System.out.println("directory for uploaded files : "+path_uploaded_files.getAbsolutePath());
		try {
			serverSocket.startService();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void readConfig() {
		try {
			if (!configFile.exists()) {
				configFile = alterConfigFile;
			}
			if (!configFile.exists()) {
				System.out.println("Sorry, uploadserver_config.xml not found.");
				System.exit(0);
			}
			Element root = Document.fromFile(configFile).getRootElement();
			
			//uploadserver base
			Element ks = root.getChild("osdxfiletransferserver");
//			host = ks.getChildText("host");
			port = ks.getChildInt("port");
			
			String ip4 = ks.getChildText("ipv4");
			try {
				byte[] addr = new byte[4];
				String[] sa = ip4.split("[.]");
				for (int i=0;i<4;i++) {
					int b = Integer.parseInt(sa[i]);
					if (b>127) b = -256+b;
					addr[i] = (byte)b;
				}
				address = InetAddress.getByAddress(addr);
			} catch (Exception ex) {
				System.out.println("CAUTION: error while parsing ip adress");
				ex.printStackTrace();
			}
		
			///Clients
			clients = new HashMap<String, ClientSettings>();
			//System.out.println("init clients");
			Element eClients = root.getChild("clients");
			Vector<Element> ecClients = eClients.getChildren("client");
			for (Element e : ecClients) {
				try {
					ClientSettings cs = ClientSettings.fromElement(e);
					clients.put(cs.getSettingsID(),cs);
					System.out.println("adding client: "+cs.getSettingsID()+" -> "+cs.getLocalRootPath().getAbsolutePath());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
				
	
			//SigningKey
			try {
				OSDXKey k = OSDXKey.fromElement(root.getChild("rootsigningkey").getChild("keypair"));
				if (k instanceof MasterKey) {
					mySigningKey = (MasterKey)k;					
				} else {
					System.out.println("ERROR: no master signing key in config.");	
				}
			} catch (Exception e) {
				System.out.println("ERROR: no master signing key in config."); 
			}
			//TODO check localproofs and signatures 

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private static void makeConfig() {
//		Console console = System.console();
//	    if (console == null) {
//	      return;
//	    }
//	    String host = console.readLine("host: ");
//	    String port = console.readLine("port: ");
//	    String prepath = console.readLine("prepath: ");
//	    String ipv4 = console.readLine("ipv4: ");
//	    String ipv6 = console.readLine("ipv6: ");
//	    String mail_user = console.readLine("mail user: ");
//	    String mail_sender = console.readLine("mail sender: ");
//	    String mail_smtp_host = console.readLine("mail smtp host: ");
//	    String id_email = console.readLine("id email: ");
//	    String id_mnemonic = console.readLine("id mnemonic: ");
//	    String pass = console.readLine("key password: ");
//	    
//	    Element root = new Element("opensdxkeyserver");
//	    Element eKeyServer = new Element("keyserver");
//	    eKeyServer.addContent("port", port);
//	    eKeyServer.addContent("prepath",prepath);
//	    eKeyServer.addContent("ipv4",ipv4);
//	    eKeyServer.addContent("ipv6",ipv6);
//	    Element eMail = new Element("mail");
//	    eMail.addContent("user", mail_user);
//	    eMail.addContent("sender", mail_sender);
//	    eMail.addContent("smtp_host", mail_smtp_host);
//	    eKeyServer.addContent(eMail);
//	    root.addContent(eKeyServer);
//	    
//	    try {
//	    	Element eSig = new Element("rootsigningkey");
//	    	MasterKey key = MasterKey.buildNewMasterKeyfromKeyPair(AsymmetricKeyPair.generateAsymmetricKeyPair());
//			Identity id = Identity.newEmptyIdentity();
//			id.setIdentNum(1);
//			id.setEmail(id_email);
//			id.setMnemonic(id_mnemonic);
//			key.addIdentity(id);
//			key.setAuthoritativeKeyServer(host);
//			key.createLockedPrivateKey("", pass);
//			eSig.addContent(key.toElement(null));
//			root.addContent(eSig);
//			Document.buildDocument(root).writeToFile(new File("keyserver_config.xml"));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
	
	public static void main(String[] args) throws Exception {
		if (args!=null && args.length==1 && args[0].equals("--makeconfig")) {
			makeConfig();
			return;
		}
		
		//debug
		String pwS = "upload";
		
		OSDXFileTransferServer s = new OSDXFileTransferServer(pwS);
		s.startService();
	}
	
	public void handle_command_not_implemented(String command, String param, OSDXSocketSender sender) {
		sender.sendEncryptedText("COMMAND \""+command+"\" NOT IMPLEMENTED");
	}
	
	
	
	
	// -- implementation of commands starts here --------------------------------------------
	

	
	public void handle_login(String username, OSDXSocketSender sender) {
		if (sender instanceof OSDXSocketServerThread) {
			OSDXSocketServerThread sst = (OSDXSocketServerThread)sender;
			if (username!=null) {
				String userid = username+"::"+sst.getClientKeyID();
				ClientSettings cs  = clients.get(userid);
				if (cs!=null) {
					sender.setID(userid);
				}
			}
		}
		if (sender.getID().equals("unknown_user")) {
			sender.sendEncryptedText("ERROR IN LOGIN :: ACCESS DENIED");
		} else {
			sender.sendEncryptedText("ACK LOGIN :: "+sender.getID());
		}
	}
	
	
	//echo command for testing
	public void handle_echo(String param, OSDXSocketSender sender) {
		if (param!=null) {
			System.out.println("ECHO::"+param);
			sender.sendEncryptedText(param);
		}
	}
	
	//change working directory: CWD directory_name
	public void handle_cd(String param, OSDXSocketSender sender) {
		ClientSettings cs = clients.get(sender.getID());
		if (cs==null) {
			sender.sendEncryptedText("ERROR IN CD :: PLEASE LOGIN");
			return;
		}
		if (cs.getRightsAndDuties()==null || !cs.getRightsAndDuties().allowsCD()) {
			sender.sendEncryptedText("ERROR IN CD :: NOT ALLOWED");
			return;
		}
		if (param!=null) {
			FileTransferState state = getState(sender);
			File path = null; 
			if (param.startsWith("/")) {
				path = new File(state.getRootPath(),param.substring(1));
			} else {
				path = new File(state.getCurrentPath(),param);
			}
			if (path.exists()) {
				state.setCurrentPath(path);
				sender.sendEncryptedText("ACK CD :: "+state.getRelativPath());
			} else {
				sender.sendEncryptedText("ERROR IN CD :: PWD is "+state.getRelativPath());
			}
		}
	}
	
	public void handle_mkdir(String param, OSDXSocketSender sender) {
		ClientSettings cs = clients.get(sender.getID());
		if (cs==null) {
			sender.sendEncryptedText("ERROR IN MKDIR :: PLEASE LOGIN");
			return;
		}
		if (cs.getRightsAndDuties()==null || !cs.getRightsAndDuties().allowsMkdir()) {
			sender.sendEncryptedText("ERROR IN MKDIR :: NOT ALLOWED");
			return;
		}
		if (param!=null) {
			FileTransferState state = getState(sender);
			File path = null;
			if (param.startsWith("/")) {
				path = new File(state.getRootPath(),param.substring(1));
			} else {
				path = new File(state.getCurrentPath(),param);
			}
			if (!state.isAllowed(path)) {
				sender.sendEncryptedText("ERROR IN MKDIR :: RESTRICTED PATH");
			}
			else if (path.exists()) {
				sender.sendEncryptedText("ERROR IN MKDIR :: PATH ALREADY EXISTS");
			}
			else {
				path.mkdirs();
				sender.sendEncryptedText("ACK MKDIR :: "+state.getRelativPath(path));
			}
		}
	}
	
	//change directory up
	public void handle_cdup(String param, OSDXSocketSender sender) {
		ClientSettings cs = clients.get(sender.getID());
		if (cs==null) {
			sender.sendEncryptedText("ERROR IN CDUP :: PLEASE LOGIN");
			return;
		}
		if (cs.getRightsAndDuties()==null || !cs.getRightsAndDuties().allowsCD()) {
			sender.sendEncryptedText("ERROR IN CDUP :: NOT ALLOWED");
			return;
		}
		FileTransferState state = getState(sender);
		boolean ok = state.cdup();
		if (ok) {
			sender.sendEncryptedText("ACK CDUP :: "+state.getRelativPath());
		} else {
			sender.sendEncryptedText("ERROR IN CDUP :: PWD is "+state.getRelativPath());
		}
	}
	
	public void handle_pwd(String param, OSDXSocketSender sender) {
		ClientSettings cs = clients.get(sender.getID());
		if (cs==null) {
			sender.sendEncryptedText("ERROR IN PWD :: PLEASE LOGIN");
			return;
		}
		if (cs.getRightsAndDuties()==null || !cs.getRightsAndDuties().allowsPWD()) {
			sender.sendEncryptedText("ERROR IN PWD :: NOT ALLOWED");
			return;
		}
		FileTransferState state = getState(sender);
		System.out.println("ACK PWD :: "+state.getRelativPath());
		sender.sendEncryptedText("ACK PWD :: "+state.getRelativPath());
	}
	

	public void handle_list(String param, OSDXSocketSender sender) {
		ClientSettings cs = clients.get(sender.getID());
		if (cs==null) {
			sender.sendEncryptedText("ERROR IN LIST :: PLEASE LOGIN");
			return;
		}
		if (cs.getRightsAndDuties()==null || !cs.getRightsAndDuties().allowsList()) {
			sender.sendEncryptedText("ERROR IN LIST :: NOT ALLOWED");
			return;
		}
		FileTransferState state = getState(sender);
		File f = null;
		if (param!=null) {
			if (param.startsWith("/")) {
				f = new File(state.getRootPath()+param);
			} else {
				f = new File(state.getCurrentPath(),param);
			}
			
		} else {
			f = state.getCurrentPath();
		}
		if (!f.exists() || !f.isDirectory()) {
			sender.sendEncryptedText("ERROR IN LIST :: DIRECTORY \""+param+"\" DOES NOT EXIST.");
		} else {
			File[] list = f.listFiles();
			String files = "";
			for (int i=0;i<list.length;i++) {
				if (i>0) files += ";;";
				String path = RemoteFileSystem.makeEscapeChars(state.getRelativPath(list[i].getParentFile()));
				String name = RemoteFileSystem.makeEscapeChars(list[i].getName());
				files += path+",,"+name+",,"+list[i].length()+",,"+list[i].lastModified()+",,"+list[i].isDirectory();
			}
			//System.out.println("ACK LIST :: "+files);
			sender.sendEncryptedText("ACK LIST :: "+files);
		}
	}
	
	public void handle_delete(String param, OSDXSocketSender sender) {
		ClientSettings cs = clients.get(sender.getID());
		if (cs==null) {
			sender.sendEncryptedText("ERROR IN DELETE :: PLEASE LOGIN");
			return;
		}
		if (cs.getRightsAndDuties()==null || !cs.getRightsAndDuties().allowsDelete()) {
			sender.sendEncryptedText("ERROR IN DELETE :: NOT ALLOWED");
			return;
		}
		FileTransferState state = getState(sender);
		File f = null;
		if (param!=null) {
			if (param.startsWith("/")) {
				f = new File(state.getRootPath()+param);
			} else {
				f = new File(state.getCurrentPath(),param);
			}
		}
		System.out.println("deleting "+f.getAbsolutePath());
		if (f==null || !f.exists() || !state.isAllowed(f)) {
			sender.sendEncryptedText("ERROR IN DELETE :: FILE \""+param+"\" DOES NOT EXIST.");
		} else {
			if (f.isDirectory()) {
				boolean ok = deleteDirectory(f);
				if (ok) {
					sender.sendEncryptedText("ACK DELETE :: "+param);
				} else {
					sender.sendEncryptedText("ERROR IN DELETE :: DIRECTORY \""+param+"\" COULD NOT BE DELETED.");
				}
			} else {
				boolean ok = f.delete();
				if (ok) {
					sender.sendEncryptedText("ACK DELETE :: "+param);
				} else {
					sender.sendEncryptedText("ERROR IN DELETE :: FILE \""+param+"\" COULD NOT BE DELETED.");
				}
			}
		}
	}
	
	public static boolean deleteDirectory(File path) {
		if( path.exists() ) {
			File[] list = path.listFiles();
			for(int i=0; i<list.length; i++) {
				if(list[i].isDirectory()) {
					deleteDirectory(list[i]);
				}
				else {
					list[i].delete();
				}
			}
		}
		return(path.delete());
	}
	
	public void handle_put(String param, OSDXSocketSender sender) {
		ClientSettings cs = clients.get(sender.getID());
		if (cs==null) {
			sender.sendEncryptedText("ERROR IN PUT :: PLEASE LOGIN");
			return;
		}
		if (cs.getRightsAndDuties()==null || !cs.getRightsAndDuties().allowsUpload()) {
			sender.sendEncryptedText("ERROR IN PUT :: NOT ALLOWED");
			return;
		}
		FileTransferState state = getState(sender);
		if (param!=null) {
			//TODO check if signature in param, extract and save
			boolean hasSignature = false;
			
			File f = null;
			if (param.startsWith("/")) {
				f = new File(state.getRootPath()+param);
			} else {
				f = new File(state.getCurrentPath(),param);
			}
			if (f.exists()) {
				sender.sendEncryptedText("ERROR IN PUT :: FILE ALREADY EXISTS");
			} else {
				boolean needsSignature = cs.getRightsAndDuties().needsSignature(f.getName());
				if (needsSignature && !hasSignature) {
					sender.sendEncryptedText("ERROR IN PUT :: FILE NEEDS SIGNATURE");
				} else {
					state.setWriteFile(f);
					sender.sendEncryptedText("ACK PUT :: WAITING FOR DATA");
				}
			}
		}
	}
	
	public void handle_noop(String param, OSDXSocketSender sender) {
		//DO NO OPERATION
	}
	
	public void handle_get(String param, OSDXSocketSender sender) {
		ClientSettings cs = clients.get(sender.getID());
		if (cs==null) {
			sender.sendEncryptedText("ERROR IN GET :: PLEASE LOGIN");
			return;
		}
		if (cs.getRightsAndDuties()==null || !cs.getRightsAndDuties().allowsDownload()) {
			sender.sendEncryptedText("ERROR IN GET :: NOT ALLOWED");
			return;
		}
		FileTransferState state = getState(sender);
		if (param!=null) {
			File f = null;
			if (param.startsWith("/")) {
				f = new File(state.getRootPath()+param);
			} else {
				f = new File(state.getCurrentPath(),param);
			}
			if (f.exists()) {
				if (!f.isDirectory()) {
					try {
						ByteArrayOutputStream bOut = new ByteArrayOutputStream();
						FileInputStream fin = new FileInputStream(f);
						byte[] buffer = new byte[1024];
						int read;
						while ((read = fin.read(buffer))>0) {
							bOut.write(buffer, 0, read);
						}
						sender.sendEncryptedData(bOut.toByteArray());
					} catch (Exception ex) {
						sender.sendEncryptedText("ERROR IN GET :: ERROR READING FROM FILE");
					}
				} else {
					sender.sendEncryptedText("ERROR IN GET :: GIVEN FILE IS A DIRECTORY");
				}
			} else {
				sender.sendEncryptedText("ERROR IN GET :: FILE DOES NOT EXIST");
			}
		}
	}

}
