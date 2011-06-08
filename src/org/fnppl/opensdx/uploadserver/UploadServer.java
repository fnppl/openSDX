package org.fnppl.opensdx.uploadserver;

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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;

import org.fnppl.opensdx.gui.DefaultMessageHandler;
import org.fnppl.opensdx.gui.MessageHandler;
import org.fnppl.opensdx.http.HTTPServer;
import org.fnppl.opensdx.http.HTTPServerRequest;
import org.fnppl.opensdx.http.HTTPServerResponse;
import org.fnppl.opensdx.security.KeyApprovingStore;
import org.fnppl.opensdx.security.KeyVerificator;
import org.fnppl.opensdx.security.MasterKey;
import org.fnppl.opensdx.security.OSDXKey;
import org.fnppl.opensdx.security.OSDXMessage;
import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.security.Signature;
import org.fnppl.opensdx.security.SymmetricKey;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;

import sun.misc.BASE64Encoder;

public class UploadServer extends HTTPServer {
	
	private String serverid = "OSDX UploadServer v0.1";
	private String servername = null;
	
	private File configFile = new File("uploadserver_config.xml"); 
	private File alterConfigFile = new File("src/org/fnppl/opensdx/uploadserver/resources/config.xml"); 
		
	private KeyVerificator keyverificator = null;
	private KeyApprovingStore keystore = null;
	protected MasterKey signingKey = null;
	
	protected File pathUploadedFiles = null;
	private HashMap<String, FileUpload> uploads = new HashMap<String,FileUpload>();
	
	private MessageHandler messageHandler = new DefaultMessageHandler() {
		public boolean requestOverwriteFile(File file) {//dont ask, just overwrite
			return true;
		}
		public boolean requestIgnoreKeyLogVerificationFailure() {//dont ignore faild keylog verification
			return false;
		}
		public MasterKey requestMasterSigningKey(KeyApprovingStore keystore) throws Exception {
			return signingKey;
		}
	};
	
	public void init(String pwSigning) {
		try {
			readConfig();
			signingKey.unlockPrivateKey(pwSigning);
			
			Document d = Document.buildDocument(signingKey.getSimplePubKeyElement());
			System.out.println("\nServer Public SigningKey:");
			d.output(System.out);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	public UploadServer(String pwSigning, String servername) throws Exception {
		super();
		this.servername = servername;
		keyverificator = KeyVerificator.make();
		init(pwSigning);
		openDefaultKeyStore();
		keystore.setSigningKey(signingKey);
	}
	
	public String getServerID() {
		return serverid;
	}
	
	public void readConfig() {
		try {
			if (!configFile.exists()) {
				configFile = alterConfigFile;
			}
			if (!configFile.exists()) {
				System.out.println("Sorry, uploadserver_config.xml not found.");
				exit();
			}
			Element root = Document.fromFile(configFile).getRootElement();
			
			//uploadserver base
			Element ks = root.getChild("uploadserver");
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
			pathUploadedFiles = new File(ks.getChildText("path_uploaded_files"));
			System.out.println("path for uploaded files: "+pathUploadedFiles.getAbsolutePath());
			
			//SigningKey
			try {
				OSDXKey k = OSDXKey.fromElement(root.getChild("rootsigningkey").getChild("keypair"));
				if (k instanceof MasterKey) {
					signingKey = (MasterKey)k;
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
	
	public static File getDefaultDir() {
		File f = new File(System.getProperty("user.home"));
		f = new File(f, "openSDX");
		if(!f.exists()) {
			f.mkdirs();
		}
		return f;
	}
	
	public boolean openDefaultKeyStore() {
		File f = getDefaultDir();
		f = new File(f, "uploadserver_keystore.xml");
		if (f.exists()) {
			try {
				keystore = KeyApprovingStore.fromFile(f, messageHandler);
				return true;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			try {
				keystore = KeyApprovingStore.createNewKeyApprovingStore(f, messageHandler);
				saveKeyStore();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return false;
	}
	
	private void saveKeyStore() {
		try {
			keystore.toFile(keystore.getFile());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public HTTPServerResponse prepareResponse(HTTPServerRequest request) throws Exception {
		if (request.method==null) return null;
	
		String cmd = request.cmd;
		if (request.method.equals("POST")) {
			if (cmd.equals("/request_upload")) {
				return handleUploadRequest(request);
			}
			else if (cmd.equals("/file_data")) {
				return handleUploadFileData(request);
			}
		}
		else if (request.method.equals("GET")) {
			if (cmd.equals("/serversettings")) {
				return handleGetServerSettingsRequest(request);
			}
		}
		else {
			throw new Exception("NOT IMPLEMENTED::METHOD: "+request.method); // correct would be to fire a HTTP_ERR
		}
		
		System.err.println("KeyServerResponse| ::request command not recognized:: "+cmd);
		return null;
	}
	
	private HTTPServerResponse handleUploadRequest(HTTPServerRequest request) throws Exception {
		if (request.xml==null) {
			return errorMessage("Wrong format!");
		}
		Element e = request.xml.getRootElement();
		
		Signature s = Signature.fromElement(e.getChild("signature"));
		String filename = e.getChildText("filename");
		File path = new File(pathUploadedFiles, s.getKey().getKeyID()+"/"+SecurityHelper.getFormattedDate(s.getSignDatetime()).substring(0,19));
		
		OSDXKey clientEncrypt = OSDXKey.fromPubKeyElement(e.getChild("encrypt_with_key").getChild("pubkey"));
		
		SymmetricKey symcrypt = SymmetricKey.getRandomKey();
		
		String token = SecurityHelper.HexDecoder.encode(SecurityHelper.getRandomBytes(20), '\0', -1);
		while (uploads.containsKey(token)) {
			//if the token already exists -> generate new one (should never really happen, but remember Murphy's law)
			token = SecurityHelper.HexDecoder.encode(SecurityHelper.getRandomBytes(20), '\0', -1);
		}
		
		e.addContent("token_id", token);
		path.mkdirs();
		Document.buildDocument(e).writeToFile(new File(path,"request.xml"));
		
		FileUpload upload = new FileUpload(filename, path, s.getMD5(),s.getSHA1(), s.getSHA256(), symcrypt);
		uploads.put(token, upload);
		
		HTTPServerResponse resp = new HTTPServerResponse(serverid);
		Element er = new Element("upload_file_encryption_key");
		er.addContent("init_vector", SecurityHelper.HexDecoder.encode(symcrypt.getInitVector(), '\0', -1));
		er.addContent("key_bytes", SecurityHelper.HexDecoder.encode(symcrypt.getKeyBytes(), '\0', -1));
		er.addContent("token_id",token);
		
		OSDXMessage msg = OSDXMessage.buildEncryptedMessage(er, clientEncrypt, signingKey);
		resp.setContentElement(msg.toElement());
		
		return resp;
	}
	
	private HTTPServerResponse handleUploadFileData(HTTPServerRequest request) throws Exception {
		String token_id = request.getHeaderValue("token_id");
		FileUpload upload = uploads.get(token_id);
		if (upload==null) {
			return errorMessage("Wrong or missing token_id!");
		}
		byte[] data = request.contentData;
		upload.setData(data);
		if (!upload.decrypt()) return errorMessage("decryption error.");
		if (!upload.verifyChecksums()) return errorMessage("checksum could not be verified.");
		if (!upload.saveToFile()) return errorMessage("error saving file");
		
		uploads.remove(token_id);
		
		HTTPServerResponse resp = new HTTPServerResponse(serverid);
		return resp;
	}
	
	private HTTPServerResponse handleGetServerSettingsRequest(HTTPServerRequest request) throws Exception {
		HTTPServerResponse resp = new HTTPServerResponse(serverid);
		try {
			Element e = new Element("uploadserver");
			e.addContent("host", servername);
			e.addContent("port",""+port);
			e.addContent("prepath", prepath);
			Element k = new Element("knownkeys");
			Element pk = signingKey.getSimplePubKeyElement();
			k.addContent(pk);
			e.addContent(k);
			OSDXMessage msg = OSDXMessage.buildMessage(e, signingKey);
			resp.setContentElement(msg.toElement());
		} catch (Exception ex) {
			resp.setRetCode(404, "FAILED");
			resp.createErrorMessageContent("Internal Error"); //should/could never happen
		}
		return resp;
	}
	
	private HTTPServerResponse errorMessage(String msg) {
		HTTPServerResponse resp = new HTTPServerResponse(serverid);
		resp.setRetCode(404, "FAILED");
		resp.createErrorMessageContent(msg);
		return resp;
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
		String servername = "localhost";
		
		UploadServer us = new UploadServer(pwS,servername);
		us.startService();
	}
	
	private class FileUpload {
		public String filename = null;
		public File path = null;
		public long requestDatetime;
		public byte[] md5 = null;
		public byte[] sha1 = null;
		public byte[] sha256 = null;
		public byte[] data = null;
		public byte[] decrpyted = null;
		
		public SymmetricKey key;
		
		public FileUpload(String filename, File path, byte[] md5, byte[] sha1, byte[] sha256, SymmetricKey key) {
			requestDatetime = System.currentTimeMillis();
			this.filename = filename;
			this.path = path;
			this.md5 = md5;
			this.sha1 = sha1;
			this.sha256 = sha256;
			this.data = null;
			this.key = key;
		}
		
		public void setData(byte[] data) {
			this.data = data;
		}
		
		public boolean decrypt() {
			try {
				decrpyted = key.decrypt(data);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		
		public boolean verifyChecksums() {
			if (decrpyted==null) return false;
			if (md5!=null    && !Arrays.equals(SecurityHelper.getMD5(decrpyted),md5)) return false;
			if (sha1!=null   && !Arrays.equals(SecurityHelper.getSHA1(decrpyted),sha1)) return false;
			if (sha256!=null && !Arrays.equals(SecurityHelper.getSHA256(decrpyted),sha256)) return false;			
			return true;
		}
		
		public boolean saveToFile() {
			if (decrpyted==null) return false;
			try {
				path.mkdirs();
				File save = new File(path,filename);
				System.out.println("Saving to file: "+save.getAbsolutePath());
				FileOutputStream fout = new FileOutputStream(save);
				fout.write(decrpyted);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		
	}
}



