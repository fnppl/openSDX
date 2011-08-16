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
import org.fnppl.opensdx.security.MasterKey;
import org.fnppl.opensdx.security.OSDXKey;
import org.fnppl.opensdx.security.OSDXMessage;
import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;

public class OSDXFileTransferServer implements OSDXSocketDataHandler {

	private OSDXSocketServer serverSocket;
	
	private File configFile = new File("osdxserver_config.xml"); 
	private File alterConfigFile = new File("src/org/fnppl/opensdx/securesocket/resources/config.xml"); 

	protected int port = -1;
	protected String prepath = "";
	private String serverid = "OSDXFileTransferServer v0.1";
	
	protected InetAddress address = null;

	private OSDXKey mySigningKey = null;
	private OSDXKey myEncryptionKey = null;
	//private File path_uploaded_files = null;
	private HashMap<OSDXSocketSender, FileTransferState> states = null;
	private HashMap<String, ClientSettings> clients = null;
	
	
	public OSDXFileTransferServer(String pwSigning, String pwEncryption) {
		readConfig();
		try {
			mySigningKey.unlockPrivateKey(pwSigning);
			myEncryptionKey.unlockPrivateKey(pwEncryption);
			serverSocket = new OSDXSocketServer(port, prepath, serverid, mySigningKey, myEncryptionKey, clients);
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
			prepath = ks.getChildTextNN("prepath");
			
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
			//path_uploaded_files = new File(ks.getChildText("path_uploaded_files"));
//			System.out.println("path for uploaded files: "+pathUploadedFiles.getAbsolutePath());
			
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
					
					//TODO e.g. generate a new key when server starts
					//should be subkey of signing key for easier verification by client
					myEncryptionKey = OSDXKey.fromElement(Document.fromString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>  <keypair><sha1fingerprint>83:16:8A:C4:97:0F:9C:6A:E5:3F:F9:F5:DF:87:8D:E1:EA:94:E1:D5</sha1fingerprint><authoritativekeyserver>localhost</authoritativekeyserver><datapath><step1><datasource>LOCAL</datasource><datainsertdatetime>2011-06-08 14:32:45 GMT+00:00</datainsertdatetime></step1></datapath><valid_from>2011-06-08 14:32:45 GMT+00:00</valid_from><valid_until>2036-06-07 20:32:45 GMT+00:00</valid_until><usage>ONLYCRYPT</usage><level>SUB</level><parentkeyid>27:61:62:78:C1:29:F3:C6:A9:03:44:D2:18:36:37:22:E2:9F:63:BF@localhost</parentkeyid><algo>RSA</algo><bits>3072</bits><modulus>00:9F:0E:67:EF:48:C3:59:48:71:F0:9E:8D:41:A4:44:3D:A5:3B:14:0B:35:C8:EE:95:6F:C6:9B:35:DE:F1:2F:BA:D6:97:BB:34:BE:92:F1:7F:A8:B9:D2:27:87:64:02:21:6E:32:DF:AD:E1:C7:66:DA:1D:71:75:07:E7:AC:7E:56:2D:EC:B9:F8:67:A2:66:98:82:61:83:1D:86:23:E4:2D:28:C2:6E:A3:F5:1D:9F:AA:24:A9:FD:84:3A:D8:1D:8B:DC:0B:EC:34:04:6B:6B:57:58:21:47:5C:41:2E:09:15:79:08:7F:01:CC:AB:E4:28:1C:CE:D7:8F:D6:C6:7E:5C:CC:D4:E0:74:47:51:0D:40:0B:0B:DD:D3:03:8C:18:56:68:88:C4:B5:DC:48:BB:36:32:C6:4A:B3:EF:08:E6:81:3F:80:96:68:25:93:58:EE:76:8F:DB:3B:39:B0:9B:8E:29:40:67:8D:C5:02:1F:F1:4A:C5:6A:D0:2A:02:F8:5C:DD:B3:0A:8C:2B:04:A5:4A:AF:25:39:89:DB:D8:4A:7F:4A:4D:10:28:10:88:6D:A4:0B:31:50:D8:C7:2E:9E:3F:EF:C8:A0:D0:19:97:EB:80:CE:DE:A0:1B:2D:4B:C7:D9:FA:39:8B:8E:10:D8:05:40:29:FD:71:EF:0D:7D:2B:8F:B6:2E:F5:FE:A7:51:84:22:CF:BA:CA:A8:63:78:E1:23:8E:F3:4D:D7:66:2F:6A:D5:CC:ED:AC:5D:35:86:E6:A9:9C:2D:7E:93:3F:77:87:6A:34:04:4D:58:97:6D:C1:67:B4:AE:17:5D:8D:75:5E:59:C7:18:82:AA:51:C7:E1:27:24:90:FE:3E:9D:1D:39:83:5A:27:61:BC:89:03:47:04:53:D6:58:0B:D1:A7:97:D3:6E:BD:BE:B6:0E:40:9D:87:76:C7:11:4A:A6:39:68:23:FB:11:21:96:43:34:C2:7F:09:C4:F4:D4:59:B0:10:01:04:9B:0F:CA:5D:FF:01:57</modulus><pubkey><exponent>01:00:01</exponent></pubkey><privkey><exponent><locked><mantraname>password</mantraname><algo>AES@256</algo><initvector>44:18:F8:24:D3:B5:FB:01:DD:DE:96:90:4D:2B:A9:E8</initvector><padding>CBC/PKCS#5</padding><bytes>93:2F:3D:72:BC:29:D5:5F:CA:01:3A:08:90:E4:5C:43:E1:69:A0:0D:41:23:18:F5:E9:5C:D1:54:68:2B:84:5E:B2:9B:0B:F4:C6:D7:C9:CE:68:FC:37:10:75:B5:11:47:4D:D8:42:D6:9D:70:14:81:75:29:17:C7:0A:26:19:25:99:FA:46:C4:F8:BE:56:B1:31:F7:BF:22:5F:3D:7F:D7:AB:E5:F0:86:18:71:C4:7C:EC:24:77:61:28:E0:7B:27:6B:A3:45:3E:50:9E:F4:03:F5:E3:68:D7:DF:1D:D3:F9:9F:1B:21:FC:4C:6B:DA:3A:65:27:00:94:43:50:14:C0:92:F9:6E:02:DC:BA:56:42:91:2B:5B:6D:FE:15:41:CF:E4:B6:4C:47:F4:27:03:9F:59:65:CC:62:55:93:D5:72:9C:B9:FF:3B:26:D1:0A:20:B4:5B:5D:21:9A:E0:8A:CD:A3:A4:FE:14:8A:42:56:59:14:8E:79:05:33:09:F3:F1:85:8A:51:5C:22:7B:BD:AA:60:E9:A2:4D:85:98:75:BE:C5:F4:30:91:58:AA:A4:F5:AB:5B:BD:E0:D4:1E:A0:25:5C:D2:EC:1A:F5:9B:23:74:1A:14:5C:7E:ED:0C:1E:E0:65:83:46:F1:D4:F2:E7:E0:52:5C:81:A7:93:D5:F6:C7:3C:66:83:13:BF:E3:B7:32:D6:06:1F:4E:22:27:CE:90:1E:A0:2F:66:76:6A:ED:E7:1B:A9:45:49:28:F0:75:AC:15:DA:EE:8C:01:78:50:C3:70:53:0C:89:7A:FF:FE:BA:28:8A:D8:6E:45:D4:EC:93:7F:3B:EE:22:6C:5D:0E:A8:D6:9D:61:6F:B1:62:C5:46:10:3E:AC:6B:F9:9A:A7:87:EC:D7:7C:8B:8A:8E:F1:70:81:52:8C:2E:83:7C:1C:A7:72:FF:7D:02:82:46:BA:E1:4F:0F:57:CF:A8:C1:61:7F:DC:6B:CC:C1:97:57:95:B8:BE:46:A2:8B:53:D6:32:E7:5C:DC:74:5B:0C:94:79:33:D9:3B:6A:CD:B7:52:C0:1F</bytes></locked></exponent></privkey><gpgkeyserverid /></keypair>  ").getRootElement());
					//myEncryptionKey.unlockPrivateKey("password");
					
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
		String pwE = "password";
		
		OSDXFileTransferServer s = new OSDXFileTransferServer(pwS,pwE);
		s.startService();
	}
	
	public void handle_command_not_implemented(String command, String param, OSDXSocketSender sender) {
		sender.sendEncryptedText("COMMAND \""+command+"\" NOT IMPLEMENTED");
	}
	
	
	
	
	// -- implementation of commands starts here --------------------------------------------
	

	//first message from client: HOST [prepath] e.g HOST /
	public void handle_host(String param, OSDXSocketSender sender) {
		if (param!=null) {
			//TODO handle prepath routing here
			
			//sendHello
			try {
				Element e = new Element("hello_client");
				e.addContent("message","please choose a symmetric key and send it encrypted with my following public key");
				e.addContent(myEncryptionKey.getSimplePubKeyElement());
				OSDXMessage msg = OSDXMessage.buildMessage(e, mySigningKey);
				sender.sendPlainText(Document.buildDocument(msg.toElement()).toString());
			} catch (Exception e1) {
				e1.printStackTrace();
			}
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
		FileTransferState state = getState(sender);
		boolean ok = state.cdup();
		if (ok) {
			sender.sendEncryptedText("ACK CDUP :: "+state.getRelativPath());
		} else {
			sender.sendEncryptedText("ERROR IN CDUP :: PWD is "+state.getRelativPath());
		}
	}
	
	public void handle_pwd(String param, OSDXSocketSender sender) {
		FileTransferState state = getState(sender);
		System.out.println("ACK PWD :: "+state.getRelativPath());
		sender.sendEncryptedText("ACK PWD :: "+state.getRelativPath());
	}
	

	public void handle_list(String param, OSDXSocketSender sender) {
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
		FileTransferState state = getState(sender);
		if (param!=null) {
			File f = null;
			if (param.startsWith("/")) {
				f = new File(state.getRootPath()+param);
			} else {
				f = new File(state.getCurrentPath(),param);
			}
			if (f.exists()) {
				sender.sendEncryptedText("ERROR IN PUT :: FILE ALREADY EXISTS");
			} else {
				state.setWriteFile(f);
				sender.sendEncryptedText("ACK GET :: WAITING FOR DATA");
			}
		}
	}
	
	public void handle_noop(String param, OSDXSocketSender sender) {
		//DO NO OPERATION
	}
	
	public void handle_get(String param, OSDXSocketSender sender) {
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
