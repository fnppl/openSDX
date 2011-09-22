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

/**
 * @author Bertram Bödeker <bboedeker@gmx.de>
 */

import java.io.Console;
import java.io.File;
import java.util.Arrays;
import java.util.Vector;

import org.fnppl.opensdx.common.Util;
import org.fnppl.opensdx.gui.MessageHandler;
import org.fnppl.opensdx.securesocket.ClientSettings;
import org.fnppl.opensdx.security.KeyApprovingStore;
import org.fnppl.opensdx.security.MasterKey;
import org.fnppl.opensdx.security.OSDXKey;
import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;

public class OSDXFileTransferServerConfig extends OSDXFileTransferClient {

	private String[] clientsID = null;
	
	public OSDXFileTransferServerConfig() {
	
	}
	
	
	public void listClients() {
		if (!rights_duties.isAdmin()) {
			System.out.println("NOT ALLOWED");
			return;
		}
		sendEncryptedText("LISTCLIENTS");
		String resp = null;
		long timeout = System.currentTimeMillis()+2000;
		
		String[] clients = null;
		while (resp==null && System.currentTimeMillis()<timeout) {
			for (int i=0;i<textQueue.size();i++) {
				if (textQueue.get(i).startsWith("ACK LISTCLIENTS :: ")) {
					resp = textQueue.remove(i).substring(19);
					clients = Util.getParams(resp);
				}
			}
		}
		clientsID = clients;
		if (clients == null) return;
		System.out.println("List of clients");
		for (int i=0;i<clients.length;i++) {
			System.out.println(String.format("%5d   %s",i,clients[i]));
		}
	}
	
	private ClientSettings getClient(String id) {
		if (!rights_duties.isAdmin()) {
			System.out.println("NOT ALLOWED");
			return null;
		}
		System.out.println("Getting client details: "+id);
		sendEncryptedText("GETCLIENT "+id);
		ClientSettings cs = null;
		long timeout = System.currentTimeMillis()+2000;

		while (cs==null && System.currentTimeMillis()<timeout) {
			for (int i=0;i<textQueue.size();i++) {
				if (textQueue.get(i).startsWith("ACK GETCLIENT :: ")) {
					try {
						cs = ClientSettings.fromElement(Document.fromString(textQueue.get(i).substring(17)).getRootElement());
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}
		if (cs==null) {
			System.out.println("TIMEOUT");
			return null;
		}
		
		return cs;
	}
	
	public void listClient(String id) {
		ClientSettings cs = getClient(id);
		Document.buildDocument(cs.toElement()).output(System.out);
		
	}
	
	
	public void addClient() {
		System.out.println("adding client");
		Console c = System.console();
		String username = c.readLine("username: ");
		String keyid = c.readLine("keyid: ");
		String local_path = c.readLine("local_path: ");
		String admin =    c.readLine("admin          (y/n) [enter for default=false]: "); 
		String mkdir =    c.readLine("allow mkdir    (y/n) [enter for default= true]: ");
		String delete =   c.readLine("allow delete   (y/n) [enter for default= true]: ");
		String pwd =      c.readLine("allow pwd      (y/n) [enter for default= true]: ");
		String cd =       c.readLine("allow cd       (y/n) [enter for default= true]: ");
		String list =     c.readLine("allow list     (y/n) [enter for default= true]: ");
		String upload =   c.readLine("allow upload   (y/n) [enter for default= true]: ");
		String download = c.readLine("allow download (y/n) [enter for default= true]: ");
		String signature_needed = c.readLine("signature_needed (separated by ';' [enter for none]: ");
		
		Element e = new Element("client");
		if (username.length()>0) e.addContent("username", username);
		if (keyid.length()>0) e.addContent("keyid",keyid);
		if (local_path.length()>0) e.addContent("local_path",local_path);
		e.addContent("auth_type","keyfile");
		Element r = new Element("rights_and_duties");
		if (admin.length()>0 && admin.equals("y")) r.addContent("admin", "true");
		if (mkdir.length()>0 && mkdir.equals("n")) r.addContent("allow_mkdir", "false");
		if (delete.length()>0 && delete.equals("n")) r.addContent("allow_delete", "false");
		if (pwd.length()>0 && pwd.equals("n")) r.addContent("allow_pwd", "false");
		if (cd.length()>0 && cd.equals("n")) r.addContent("allow_cd", "false");
		if (list.length()>0 && list.equals("n")) r.addContent("allow_list", "false");
		if (upload.length()>0 && upload.equals("n")) r.addContent("allow_upload", "false");
		if (download.length()>0 && download.equals("n")) r.addContent("allow_download", "false");
		
		if (signature_needed.length()>0) {
			String[] sig = signature_needed.split(";");
			for (String s : sig) {
				r.addContent("signature_needed", s);
			}
		}
		e.addContent(r);
		
		ClientSettings cs = ClientSettings.fromElement(e);
		System.out.println("\nReally add the following client?");
		System.out.println("------------------------------");
		Document.buildDocument(cs.toElement()).output(System.out);
		String really = c.readLine("add (y/n): ");
		
		if (really.equals("y")) {
			//putclient
			sendEncryptedText("PUTCLIENT "+Document.buildDocument(cs.toElement()).toStringCompact());
		}
	}
	
	public void addClient(String username, String keyid, String local_path, String rights, String signature_needed) {
		final boolean[] go_on = new boolean[] {true};
		Thread t = new Thread() {
			public void run() {
				while(go_on[0]) {
					try {
						sleep(5000);
						noop();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
		t.start();
		System.out.println("adding client");
		if (local_path.equalsIgnoreCase("null")) local_path = "";
		if (rights.equalsIgnoreCase("null")) rights = "";
		if (signature_needed.equalsIgnoreCase("null")) signature_needed = "";
		
		Element e = new Element("client");
		if (username.length()>0) e.addContent("username", username);
		if (keyid.length()>0) e.addContent("keyid",keyid);
		if (local_path.length()>0) e.addContent("local_path",local_path);
		e.addContent("auth_type","keyfile");
		Element r = new Element("rights_and_duties");
		if (rights!=null && rights.length()>1) {
			if (rights.contains("+a")) r.addContent("admin", "true");
			if (rights.contains("-m")) r.addContent("allow_mkdir", "false");
			if (rights.contains("-r")) r.addContent("allow_delete", "false");
			if (rights.contains("-p")) r.addContent("allow_pwd", "false");
			if (rights.contains("-c")) r.addContent("allow_cd", "false");
			if (rights.contains("-l")) r.addContent("allow_list", "false");
			if (rights.contains("-u")) r.addContent("allow_upload", "false");
			if (rights.contains("-d")) r.addContent("allow_download", "false");
		}
		if (signature_needed.length()>0) {
			String[] sig = signature_needed.split(";");
			for (String s : sig) {
				r.addContent("signature_needed", s);
			}
		}
		e.addContent(r);
		
		ClientSettings cs = ClientSettings.fromElement(e);
		System.out.println("\nReally add the following client?");
		System.out.println("------------------------------");
		Document.buildDocument(cs.toElement()).output(System.out);
		String really = System.console().readLine("add (y/n): ");
		go_on[0] = false;
		
		if (really.equals("y")) {
			//putclient
			sendEncryptedText("PUTCLIENT "+Document.buildDocument(cs.toElement()).toStringCompact());
		}
	}
	
	public void changeClient(ClientSettings cs) {
		System.out.println("adding ");
		Console c = System.console();
		//String username = c.readLine("username: ");
		//String keyid = c.readLine("keyid: ");
		RightsAndDuties r = cs.getRightsAndDuties();
		String local_path = c.readLine("local_path [enter = "+cs.getLocalRootPath().getAbsolutePath()+" ] : ");
		String admin =    c.readLine("admin          (y/n) [enter = "+r.isAdmin()+"]: "); 
		String mkdir =    c.readLine("allow mkdir    (y/n) [enter = "+r.allowsMkdir()+"]: ");
		String delete =   c.readLine("allow delete   (y/n) [enter = "+r.allowsDelete()+"]: ");
		String pwd =      c.readLine("allow pwd      (y/n) [enter = "+r.allowsPWD()+"]: ");
		String cd =       c.readLine("allow cd       (y/n) [enter = "+r.allowsCD()+"]: ");
		String list =     c.readLine("allow list     (y/n) [enter = "+r.allowsList()+"]: ");
		String upload =   c.readLine("allow upload   (y/n) [enter = "+r.allowsUpload()+"]: ");
		String download = c.readLine("allow download (y/n) [enter = "+r.allowsDownload()+"]: ");
		
		String signature_needed = c.readLine("signature_needed (separated by ';' [enter = "+r.getSignaturesNeededAsList()+"]: ");
		
		if (local_path.length()>0) cs.setLocalRootPath(new File(local_path));
		if (admin.length()>0) {
			if (admin.equals("y")) cs.getRightsAndDuties().setAdmin(true);
			else if (admin.equals("n")) cs.getRightsAndDuties().setAdmin(false);
			else System.out.println("wrong input: admin = "+admin);
		}
		if (mkdir.length()>0) {
			if (mkdir.equals("y")) cs.getRightsAndDuties().setAllow_mkdir(true);
			else if (mkdir.equals("n")) cs.getRightsAndDuties().setAllow_mkdir(false);
			else System.out.println("wrong input: mkdir = "+mkdir);
		}
		if (delete.length()>0) {
			if (delete.equals("y")) cs.getRightsAndDuties().setAllow_delete(true);
			else if (delete.equals("n")) cs.getRightsAndDuties().setAllow_delete(false);
			else System.out.println("wrong input: delete = "+delete);
		}
		if (pwd.length()>0) {
			if (pwd.equals("y")) cs.getRightsAndDuties().setAllow_pwd(true);
			else if (pwd.equals("n")) cs.getRightsAndDuties().setAllow_pwd(false);
			else System.out.println("wrong input: pwd = "+pwd);
		}
		if (cd.length()>0) {
			if (cd.equals("y")) cs.getRightsAndDuties().setAllow_cd(true);
			else if (cd.equals("n")) cs.getRightsAndDuties().setAllow_cd(false);
			else System.out.println("wrong input: cd = "+cd);
		}
		if (list.length()>0) {
			if (list.equals("y")) cs.getRightsAndDuties().setAllow_list(true);
			else if (list.equals("n")) cs.getRightsAndDuties().setAllow_list(false);
			else System.out.println("wrong input: list = "+list);
		}
		if (upload.length()>0) {
			if (upload.equals("y")) cs.getRightsAndDuties().setAllow_upload(true);
			else if (upload.equals("n")) cs.getRightsAndDuties().setAllow_upload(false);
			else System.out.println("wrong input: upload = "+upload);
		}
		if (download.length()>0) {
			if (download.equals("y")) cs.getRightsAndDuties().setAllow_download(true);
			else if (download.equals("n")) cs.getRightsAndDuties().setAllow_download(false);
			else System.out.println("wrong input: download = "+download);
		}
		
		if (signature_needed.length()>0) {
			cs.getRightsAndDuties().removeAllSignatureNeeded();
			String[] sig = signature_needed.split(";");
			for (String s : sig) {
				cs.getRightsAndDuties().addSignatureNeeded(s);
			}
		}
		
		
		System.out.println("\nReally change the following user?");
		System.out.println("------------------------------");
		Document.buildDocument(cs.toElement()).output(System.out);
		String really = c.readLine("change (y/n): ");
		
		if (really.equals("y")) {
			//putclient
			sendEncryptedText("PUTCLIENT "+Document.buildDocument(cs.toElement()).toStringCompact());
		}
	}
	
	private void mainmenu() {
		final boolean[] go_on = new boolean[] {true};
		Thread t = new Thread() {
			public void run() {
				while(go_on[0]) {
					try {
						sleep(5000);
						noop();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
		t.start();
		while (go_on[0]) {
			showMainMenueText();
			String line = System.console().readLine(": ");
			if (line.equals("q")) {
				go_on[0] = false;			}
			else if (line.equals("1")) {
				listClients();
			}
			else if (line.startsWith("2 ")) {
				try {
					int no = Integer.parseInt(line.substring(2));
					listClient(clientsID[no]);	
				} catch (Exception ex) {
					System.out.println("Error");
				}
				
			}
			else if (line.equals("3")) {
				addClient();
			}
			else if (line.startsWith("4 ")) {
				try {
					int no = Integer.parseInt(line.substring(2));
					ClientSettings cs = getClient(clientsID[no]);
					if (cs!=null) {
						changeClient(cs);
					}
				} catch (Exception ex) {
					System.out.println("Error");
				}
				
			}
		}
		System.exit(0);
	}
	
	private void showMainMenueText() {
		System.out.println("----------------------------------------------");
		System.out.println("OSDX File Transfer Server Config :: Main Menue");
		System.out.println("----------------------------------------------");
		System.out.println("(1) list clients");
		System.out.println("(2) list clients detail [no] (only after list clients)");
		System.out.println("(3) add client");
		System.out.println("(4) change client [no]");
		
		
		//System.out.println("(3) remove client [no]");
	}
	
	
	
	public static void main(String[] args) {
		OSDXFileTransferServerConfig conf = new OSDXFileTransferServerConfig();
		OSDXKey key = null;
		String host = null; 	// --host
		int port = -1;			// --port
		String prepath = "/";	// --prepath
		String username = null; // --user
		String remotepath =null;// --remotepath
		String keystore = null; // --keystore
		String keyid = null;	// --keyid
		String keypw = null;	// --keypw
		String keypwfile = null;// --keypwfile
		String config = null;	// --config
		String[] add = null;		// --add [username] [keyid] [local_path] [+a -m -r -p -c -l -u -d] [signatures needed]
		
		
		int i=0;
		try {
			while (i<args.length) {
				String s = args[i];
				if (s.equals("--host")) {
					host = args[i+1];
					i+=2;
				}
				else if (s.equals("--port")) {
					port = Integer.parseInt(args[i+1]);
					i+=2;
				}
				else if (s.equals("--prepath")) {
					prepath = args[i+1];
					i+=2;
				}
				else if (s.equals("--user")) {
					username = args[i+1];
					i+=2;
				}
				else if (s.equals("--remotepath")) {
					remotepath = args[i+1];
					i+=2;
				}
				else if (s.equals("--keystore")) {
					keystore = args[i+1];
					i+=2;
				}
				else if (s.equals("--keyid")) {
					keyid = args[i+1];
					i+=2;
				}
				else if (s.equals("--keypw")) {
					keypw = args[i+1];
					i+=2;
				}
				else if (s.equals("--keypwfile")) {
					keypwfile = args[i+1];
					i+=2;
				}
				else if (s.equals("--config")) {
					config = args[i+1];
					i+=2;
				}
				else if (s.equals("--add")) {
					add = new String[5];
					for (int j=0;j<5;j++) {
						add[j] = args[i+1+j];
					}
					i+=6;
				}
				else {
					System.out.println("CANT UNDERSTAND ARGUMENT: "+s);
					i+=1;
				}
			}
			if (config!=null) {
				//read config
				Element ec = Document.fromFile(new File(config)).getRootElement();
				if (host==null && ec.getChild("host")!=null) host = ec.getChildText("host");
				if (port == -1 && ec.getChild("port")!=null) port = Integer.parseInt(ec.getChildText("port"));
				if (prepath==null && ec.getChild("prepath")!=null) prepath = ec.getChildText("prepath");
				if (remotepath==null && ec.getChild("remotepath")!=null) remotepath = ec.getChildText("remotepath");
				if (username==null) {
					if (ec.getChild("username")!=null) username = ec.getChildText("username");
					if (ec.getChild("user")!=null) username = ec.getChildText("user");
				}
				if (keystore==null && ec.getChild("keystore")!=null) keystore = ec.getChildText("keystore");
				if (keyid==null && ec.getChild("keyid")!=null) keyid = ec.getChildText("keyid");
				if (keypw==null && ec.getChild("keypw")!=null) keypw = ec.getChildText("keypw");
				if (keypwfile==null && ec.getChild("keypwfile")!=null) keypwfile = ec.getChildText("keypwfile");
				
				//private key
				if (ec.getChild("keypair")!=null) {
					key = OSDXKey.fromElement(ec.getChild("keypair"));
				}
			}
			//message handler
			MessageHandler mh = new MessageHandler() {
				public char[] requestPassword(String keyid, String mantra) {
					System.out.println("please enter password for keyid: "+keyid+", mantra: "+mantra);
					System.out.print("password: ");
					char[] pw = System.console().readPassword();
					return pw;
				}
				
				public boolean requestOverwriteFile(File file) {
					return false;
				}
				
				public String[] requestNewPasswordAndMantra(String message) {
					return null;
				}
				
				public MasterKey requestMasterSigningKey(KeyApprovingStore keystore)
						throws Exception {
					return null;
				}
				public boolean requestIgnoreVerificationFailure() {
					System.out.println("verification of keystore failed.");
					return false;
				}
				public boolean requestIgnoreKeyLogVerificationFailure() {
					return false;
				}
				public void fireWrongPasswordMessage() {
					System.out.println("Sorry, wrong password.");
					System.exit(1);
				}
				public File chooseOriginalFileForSignature(File dir, String selectFile) {
					return null;
				}
				public File requestOpenKeystore() {
					return null;
				}
				public char[] requestPasswordTitleAndMessage(String title, String message) {
					return null;
				}
			};
		
		
			//check if we have everything we need
			if (host==null) error("missing paramenter: host");
			if (port==-1) error("missing parameter: port");
			if (username==null) error("missing paramenter: user");
			
			//init key
			if (key==null) {
				if (keystore==null) {
					error("missing paramenter: key in configfile or keystore");
				}
				if (keyid==null) {
					error("missing paramenter: key in configfile or keyid");
				}
				KeyApprovingStore ks = KeyApprovingStore.fromFile(new File(keystore), mh);
				key = ks.getKey(keyid);
				if (key==null) error("error: keyid: "+keyid+" not found in given keystore.");
			}
			
			//unlock key
			if (keypw!=null) {
				key.unlockPrivateKey(keypw);
			} else if (keypwfile!=null) {
				keypw = Util.loadText(keypwfile);
				key.unlockPrivateKey(keypw);
			} else {
				key.unlockPrivateKey(mh);
			}
			if (!key.isPrivateKeyUnlocked()) {
				error("can not unlock private key");
			}
			
			
			conf.connect(host, port, prepath, key, username);
			System.out.println("connected");
			if (add!=null) {
				System.out.println("add "+Arrays.toString(add));
				conf.addClient(add[0], add[1], add[2], add[3], add[4]);
				conf.closeConnection();
			} else {
				conf.mainmenu();	
			}
			
			
		} catch (Exception ex) {
			System.out.println("usage: OSDXFileTransferServerConfig --host localhost --port 4221 --prepath \"/\" --user username --keystore defautlKeyStore.xml --keyid 11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE:FF:11:22:33:44:55 --keypw key-password");
			System.out.println("usage: OSDXFileTransferServerConfig --config configfile.xml");
			ex.printStackTrace();
		}
		
	}
	
		private static void error(String msg) {
			System.out.println(msg);
			System.exit(1);
		}

}

