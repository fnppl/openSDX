package org.fnppl.opensdx.keyserver;

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
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;
import java.util.Map.Entry;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;

import org.fnppl.opensdx.gui.DefaultMessageHandler;
import org.fnppl.opensdx.gui.MessageHandler;
import org.fnppl.opensdx.http.HTTPServer;
import org.fnppl.opensdx.http.HTTPServerRequest;
import org.fnppl.opensdx.http.HTTPServerResponse;
import org.fnppl.opensdx.security.AsymmetricKeyPair;
import org.fnppl.opensdx.security.DataSourceStep;
import org.fnppl.opensdx.security.Identity;
import org.fnppl.opensdx.security.KeyApprovingStore;
import org.fnppl.opensdx.security.KeyLog;
import org.fnppl.opensdx.security.KeyStatus;
import org.fnppl.opensdx.security.KeyVerificator;
import org.fnppl.opensdx.security.MasterKey;
import org.fnppl.opensdx.security.OSDXKey;
import org.fnppl.opensdx.security.OSDXMessage;
import org.fnppl.opensdx.security.Result;
import org.fnppl.opensdx.security.RevokeKey;
import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.security.Signature;
import org.fnppl.opensdx.security.SubKey;
import org.fnppl.opensdx.security.TrustRatingOfKey;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;



/*
 * HT 2011-02-20
 * I am a bit bored of twiggling around with annoying formats, frameworks and paddings and whatever
 * 
 * I think, it would be nice to have a good (which does not mean, those other solutions are not good), clean, clean-room-implementation of what suits ME best...
 * 
 * of yourse, now it makes sense to separate the http-parts from tsas AND keyserver into an own package... later...
 * 
 */

/*
 * 1. registering a new publickey should always be possible (confirmation-email is sent and has to be accepted)
 * 2. adding an approval to any publickey should be possible - if that approving key is also registered
 * 3. it would be good to multiplex to/from gpgkeyser.de or such
 * 4. a strict separation between publickey and approval(chains) is desired
 * 5. different verification-levels should be available on each key/approval
 * 6. such things as "certificate" are much appreciated to be done better here. horrible: x509v3 - why on earth? 
 * 7. communication with this server is to be done via client-api (commandline) - although browser would also be possible - but i am not into giving ssl a try... 
 * 
 */

//http://de.wikipedia.org/wiki/Hypertext_Transfer_Protocol

public class KeyServerMain extends HTTPServer {
	
	private String serverid = "OSDX KeyServer v0.2";
	
	private File configFile = new File("keyserver_config.xml"); 
	private File alterConfigFile = new File("src/org/fnppl/opensdx/keyserver/resources/config.xml"); 
	
	private Properties mailProps = null;
	private MailAuthenticator mailAuth = null;
	private KeyApprovingStore keystore;
	
	private MessageHandler messageHandler = new DefaultMessageHandler() {
		public boolean requestOverwriteFile(File file) {//dont ask, just overwrite
			return true;
		}
		public boolean requestIgnoreKeyLogVerificationFailure() {//dont ignore faild keylog verification
			return false;
		}
		public MasterKey requestMasterSigningKey(KeyApprovingStore keystore) throws Exception {
			return keyServerSigningKey;
		}
	};
	
	private HashMap<String, Vector<OSDXKey>> id_keys;
	private HashMap<String, OSDXKey> keyid_key;
	private HashMap<String, Vector<KeyLog>> keyid_log;
	private HashMap<String, Vector<OSDXKey>> keyid_subkeys;
	private HashMap<String, KeyLog> openTokens;
	
	protected MasterKey keyServerSigningKey = null;
	
	public KeyServerMain(String pwSigning, String pwMail) throws Exception {
		super();
		init(pwSigning);
		if (signingKey instanceof MasterKey) {
			keyServerSigningKey = (MasterKey)signingKey;
		} else {
			throw new RuntimeException("ERROR: root signing key must be on MASTER level!");
		}
		id_keys = new HashMap<String, Vector<OSDXKey>>();
		keyid_key = new HashMap<String, OSDXKey>();
		keyid_log = new HashMap<String, Vector<KeyLog>>();
		keyid_subkeys = new HashMap<String, Vector<OSDXKey>>();
		openTokens = new HashMap<String, KeyLog>();
		
		if (mailProps!=null) {
			if (pwMail !=null) {
				mailProps.setProperty("mail.password", pwMail);	
			}
			mailAuth = new MailAuthenticator(mailProps.getProperty("mail.user"), mailProps.getProperty("mail.password"));
		}
		openDefaultKeyStore();
		keystore.setSigningKey(keyServerSigningKey);
		updateCache(keyServerSigningKey, null);
	}
	
	public OSDXKey createNewSigningKey(String pwSigning) {
		try {
			System.out.println("Creating new SigningKey:");
			//generate new keypair
			MasterKey keyServerSigningKey = MasterKey.buildNewMasterKeyfromKeyPair(AsymmetricKeyPair.generateAsymmetricKeyPair());
			keyServerSigningKey.setAuthoritativeKeyServer(host);
			Identity id = Identity.newEmptyIdentity();
			id.setEmail("debug_signing@keyserver.fnppl.org");
			id.setIdentNum(1);
			id.createSHA256();
			keyServerSigningKey.addIdentity(id);
			keyServerSigningKey.createLockedPrivateKey("", pwSigning);
			
			Document d = Document.buildDocument(keyServerSigningKey.toElement(messageHandler));
			System.out.println("\nKeyServerSigningKey:");
			d.output(System.out);
			return keyServerSigningKey;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
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
				System.out.println("Sorry, keyserver_config.xml not found.");
				exit();
			}
			Element root = Document.fromFile(configFile).getRootElement();
			//keyserver base
			Element ks = root.getChild("keyserver");
			host = ks.getChildText("host");
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
			//mail properties
			Element eMail = ks.getChild("mail");
			mailProps = new Properties();
			mailProps.setProperty("mail.user", eMail.getChildText("user"));
			String pw = eMail.getChildText("password");
			if (pw!=null && pw.length()>0) {
				mailProps.setProperty("mail.password", pw);	
			}
			mailProps.setProperty("mail.transport.protocol", "smtp");
			mailProps.setProperty("mail.smtp.host", eMail.getChildText("smtp_host"));
			mailProps.setProperty("mail.smtp.auth", "true");
			mailProps.setProperty("senderAddress", eMail.getChildText("sender"));
			
			//keyServerSigningKey
			try {
				OSDXKey k = OSDXKey.fromElement(root.getChild("rootsigningkey").getChild("keypair"));
				signingKey = k;
			} catch (Exception e) {
				System.out.println("ERROR: no signing key in config."); 
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
		f = new File(f, "keyserver_keystore.xml");
		if (f.exists()) {
			try {
				keystore = KeyApprovingStore.fromFile(f, messageHandler);
				Vector<OSDXKey> keys = keystore.getAllKeys();
				if (keys != null) {
					for (OSDXKey k : keys) {
						updateCache(k, null);
					}
				}
				Vector<KeyLog> keylogs = keystore.getKeyLogs();
				if (keylogs != null) {
					for (KeyLog l : keylogs) {
						updateCache(null, l);
					}
				}
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
	
	private void updateCache(OSDXKey k, KeyLog l) {
		if (k!=null) {
			keyid_key.put(k.getKeyModulusSHA1(), k);
			System.out.println("adding keyid_key: "+k.getKeyModulusSHA1()+"::OSDXKey");
			if (k instanceof MasterKey) {
				Vector<Identity> ids = ((MasterKey)k).getIdentities();
				if (ids != null) {
					for (Identity id : ids) {
						if (!id_keys.containsKey(id.getEmail())) {
							id_keys.put(id.getEmail(),
									new Vector<OSDXKey>());
						}
						id_keys.get(id.getEmail()).add(k);
						System.out.println("adding id_keys: "+id.getEmail()+"::"+k.getKeyModulusSHA1());
					}
				}
			}
			if (k instanceof SubKey) {
				String parentKeyID = ((SubKey)k).getParentKeyID();
				if (parentKeyID!=null && parentKeyID.length()>0) {
					parentKeyID = OSDXKey.getFormattedKeyIDModulusOnly(parentKeyID);
					if (!keyid_subkeys.containsKey(parentKeyID)) {
						keyid_subkeys.put(parentKeyID, new Vector<OSDXKey>());
					}
					keyid_subkeys.get(parentKeyID).add(k);
					System.out.println("adding subkey: "+k.getKeyModulusSHA1()+" for parent key: "+parentKeyID);
				}
			}
		}
		if (l!=null) {
			//String keyid = l.getKeyIDTo();
			String keyid = OSDXKey.getFormattedKeyIDModulusOnly(l.getKeyIDTo());
			if (!keyid_log.containsKey(keyid)) {
				keyid_log.put(keyid, new Vector<KeyLog>());
			}
			keyid_log.get(keyid).add(l);
		}
	}

	private void saveKeyStore() {
		try {
			keystore.toFile(keystore.getFile());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private KeyServerResponse errorMessage(String msg) {
		KeyServerResponse resp = new KeyServerResponse(serverid);
		resp.setRetCode(404, "FAILED");
		resp.createErrorMessageContent(msg);
		return resp;
	}
	
	private KeyServerResponse handlePutMasterKeyRequest(HTTPServerRequest request) throws Exception {
		OSDXMessage msg;
		try {
			msg = OSDXMessage.fromElement(request.xml.getRootElement());
		} catch (Exception ex) {
			return errorMessage("ERROR in opensdx_message");
		}
		Result verified = msg.verifySignaturesWithoutKeyVerification();
		if (!verified.succeeded) {
			return errorMessage("verification of signature failed"+(verified.errorMessage!=null?": "+verified.errorMessage:""));
		}
		Document.buildDocument(msg.toElement()).output(System.out);
		Element content = msg.getContent();
		if (!content.getName().equals("masterpubkey")) {
			return errorMessage("missing masterpubkey");
		}
		
		OSDXKey pubkey = OSDXKey.fromPubKeyElement(content.getChild("pubkey"));
		
		//check key already on server
		String newkeyid = OSDXKey.getFormattedKeyIDModulusOnly(pubkey.getKeyID());
		if (keyid_key.containsKey(newkeyid)) {
			return errorMessage("A key with this id is already registered.");
//			OSDXKey old = keyid_key.get(newkeyid);
//			keystore.removeKey(old);
//			for (RevokeKey k : ((MasterKey)old).getRevokeKeys()) {
//				key.addRevokeKey(k);
//			}
//			for (SubKey k : ((MasterKey)old).getSubKeys()) {
//				key.addSubKey(k);
//			}
		}
		
		
		MasterKey key = null;
		if (pubkey instanceof MasterKey) {
			key = (MasterKey)pubkey;
		} else {
			return errorMessage("Key must be on MASTER Key level");
		}
		
		Identity idd = Identity.fromElement(content.getChild("identity"));
		key.addIdentity(idd);
		key.addDataSourceStep(new DataSourceStep(request.ipv4, request.datetime));
		
		//generate keylog for approve_pending of email
		Identity apid = Identity.newEmptyIdentity();
		apid.setIdentNum(idd.getIdentNum());
		apid.setEmail(idd.getEmail());
		KeyLog kl = KeyLog.buildNewKeyLog(KeyLog.APPROVAL_PENDING, keyServerSigningKey, key.getKeyID(), request.ipv4, request.ipv4, apid);
		kl.signoffKeyServer(keyServerSigningKey);
		
		//send email with token
		byte[] tokenbytes = SecurityHelper.getRandomBytes(20);
		String token = SecurityHelper.HexDecoder.encode(tokenbytes, '\0',-1);
		String verificationMsg = "Please verify your mail-address by clicking on the following link:\nhttp://"+host+":"+port+"/approve_mail?id="+token;
		openTokens.put(token, kl);
		try {
			sendMail(idd.getEmail(), "email address verification", verificationMsg);
		} catch (Exception ex) {
			System.out.println("ERROR while sendind mail :: "+ex.getMessage());
			//ex.printStackTrace();
		}
		
		//save to keystore
		keystore.addKey(key);
		keystore.addKeyLog(kl);
		updateCache(key, kl);
		saveKeyStore();
		
		//send response
		KeyServerResponse resp = new KeyServerResponse(serverid);
		return resp;
		
	}
	
	private KeyServerResponse handleVerifyRequest(HTTPServerRequest request) throws Exception {
		//System.out.println("KeyServerResponse | ::handle verify request");
		String id = request.getParamValue("id");
		System.out.println("Token ID: "+id);
		KeyLog kl = openTokens.get(id);
		if (kl!=null) {
			//derive approval of email keylog from approval pending keylog 
			Identity idd = Identity.newEmptyIdentity();
			idd.setIdentNum(kl.getIdentity().getIdentNum());
			idd.setEmail(kl.getIdentity().getEmail());
			
			KeyLog klApprove = KeyLog.buildNewKeyLog(KeyLog.APPROVAL, keyServerSigningKey, kl.getKeyIDTo(), request.ipv4, request.ipv4, idd);
			keystore.addKeyLog(klApprove);
			openTokens.remove(id);
			
			//save to keystore
			updateCache(null, klApprove);
			saveKeyStore();
			
			//add key to trusted keys
			OSDXKey key = keyid_key.get(kl.getKeyIDTo());
			if (key!=null) {
				KeyVerificator.addRatedKey(key, TrustRatingOfKey.RATING_MARGINAL);
			}
			
			//send response
			KeyServerResponse resp = new KeyServerResponse(serverid);
			String html = "<HTML><BODY>Thank you. Approval of mail address successful.</BODY></HTML>";
			resp.setHTML(html);
			return resp;
		} else {
			return errorMessage("id "+id+" not recognized");
		}
	}
	
	private KeyServerResponse handlePutRevokeKeyRequest(HTTPServerRequest request) throws Exception {
		OSDXMessage msg;
		try {
			msg = OSDXMessage.fromElement(request.xml.getRootElement());
		} catch (Exception ex) {
			return errorMessage("ERROR in opensdx_message");
		}
		Result verified = msg.verifySignaturesWithoutKeyVerification(); //check keys later
		if (!verified.succeeded) {
			return errorMessage("verification of signature failed"+(verified.errorMessage!=null?": "+verified.errorMessage:""));
		}
		Element content = msg.getContent();
		if (!content.getName().equals("revokekey")) {
			return errorMessage("missing revokekey");
		}
		
		//check masterkey on server
		String masterkeyid = content.getChildText("masterkeyid");
		OSDXKey masterkey = keystore.getKey(masterkeyid);
		if (masterkey==null || !(masterkey instanceof MasterKey)) {
			return errorMessage("associatied masterkey is not on server.");
		}
		
		//check masterkey approved
		KeyStatus ks = keystore.getKeyStatus(masterkey.getKeyID());
		//if (ks==null || !ks.isValid()) {
		if (ks==null || !(ks.isValid() || ks.isUnapproved())) { //TODO for testing with approval_pending
			return errorMessage("associatied masterkey is not approved.");
		}
		
		System.out.println("masterkey status: "+ks.getValidityStatusName());
		
		OSDXKey revokekey = OSDXKey.fromPubKeyElement(content.getChild("pubkey"));
		if (!(revokekey instanceof RevokeKey)) {
			return errorMessage("wrong key level: Revoke Key needed.");
		}
		
		//put key in keystore
		
		//check key already on server
		String newkeyid = OSDXKey.getFormattedKeyIDModulusOnly(revokekey.getKeyID());
		if (keyid_key.containsKey(newkeyid)) {
			OSDXKey old = keyid_key.get(newkeyid);
			keystore.removeKey(old);
		}
		((MasterKey)masterkey).addRevokeKey((RevokeKey)revokekey);
		((RevokeKey)revokekey).setParentKey((MasterKey)masterkey);
		
		//save
		keystore.addKey(revokekey);
		//keystore.addKeyLog(kl);
		updateCache(revokekey, null);
		saveKeyStore();
		
		//add revokekey to trusted keys
		KeyVerificator.addRatedKey(revokekey, TrustRatingOfKey.RATING_MARGINAL);
		
		KeyServerResponse resp = new KeyServerResponse(serverid); 
		return resp;
	}

	private KeyServerResponse handlePutRevokeMasterkeyRequest(HTTPServerRequest request) throws Exception {
		OSDXMessage msg;
		try {
			msg = OSDXMessage.fromElement(request.xml.getRootElement());
		} catch (Exception ex) {
			return errorMessage("ERROR in opensdx_message");
		}
		Result verified = msg.verifySignatures();
		if (!verified.succeeded) {
			return errorMessage("verification of signature failed"+(verified.errorMessage!=null?": "+verified.errorMessage:""));
		}
		Element content = msg.getContent();
		if (!content.getName().equals("revokemasterkey")) {
			return errorMessage("missing revokemasterkey element");
		}
		
		String fromKeyID = content.getChildText("from_keyid");
		String toKeyID = content.getChildText("to_keyid");
		String message = content.getChildText("message");

		
		OSDXKey revokekey = keyid_key.get(OSDXKey.getFormattedKeyIDModulusOnly(fromKeyID)); 
		if (revokekey==null || !(revokekey instanceof RevokeKey)) {
			return errorMessage("revokekey not registered on keyserver");
		}
		
		//check toKeyID is parent of revokekey
		if (   !    OSDXKey.getFormattedKeyIDModulusOnly(((RevokeKey)revokekey).getParentKeyID())
			.equals(OSDXKey.getFormattedKeyIDModulusOnly(toKeyID))) {
			return errorMessage("revokekey is not registered as child of masterkey");
		}
		
		Signature sig = msg.getSignatures().get(0);
		byte[] givenSha256localproof = msg.getSha256LocalProof();
		
		KeyLog log = KeyLog.buildNewRevocationKeyLog(fromKeyID, toKeyID, message, givenSha256localproof, sig, request.ipv4, request.ipv4, keyServerSigningKey);
		log.verify();
		
		//save
		updateCache(null,log);
		keystore.addKeyLog(log);
		saveKeyStore();
		
		KeyServerResponse resp = new KeyServerResponse(serverid); 
		return resp;
	}
	
	private KeyServerResponse handlePutRevokeSubkeyRequest(HTTPServerRequest request) throws Exception {
		OSDXMessage msg;
		try {
			msg = OSDXMessage.fromElement(request.xml.getRootElement());
		} catch (Exception ex) {
			return errorMessage("ERROR in opensdx_message");
		}
		Result verified = msg.verifySignaturesWithoutKeyVerification();
		if (!verified.succeeded) {
			return errorMessage("verification of signature failed"+(verified.errorMessage!=null?": "+verified.errorMessage:""));
		}
		Element content = msg.getContent();
		if (!content.getName().equals("revokesubkey")) {
			return errorMessage("missing revokesubkey element");
		}
		
		String fromKeyID = content.getChildText("from_keyid");
		String toKeyID = content.getChildText("to_keyid");
		String message = content.getChildText("message");

		
		OSDXKey subkey = keyid_key.get(OSDXKey.getFormattedKeyIDModulusOnly(toKeyID)); 
		if (subkey==null || !(subkey instanceof SubKey)) {
			return errorMessage("subkey not registered on keyserver");
		}
		
		//check fromKeyID is parent of subkey
		if (   !    OSDXKey.getFormattedKeyIDModulusOnly(((SubKey)subkey).getParentKeyID())
			.equals(OSDXKey.getFormattedKeyIDModulusOnly(fromKeyID))) {
			return errorMessage("subkey is not registered as child of masterkey");
		}
		
		Signature sig = msg.getSignatures().get(0);
		byte[] givenSha256localproof = msg.getSha256LocalProof();
		
		KeyLog log = KeyLog.buildNewRevocationKeyLog(fromKeyID, toKeyID, message, givenSha256localproof, sig, request.ipv4, request.ipv4, keyServerSigningKey);
		log.verify();
		
		//save
		updateCache(null,log);
		keystore.addKeyLog(log);
		saveKeyStore();
		
		KeyServerResponse resp = new KeyServerResponse(serverid); 
		return resp;
	}

	public HTTPServerResponse handlePutSubKeyRequest(HTTPServerRequest request) throws Exception {
		OSDXMessage msg;
		try {
			msg = OSDXMessage.fromElement(request.xml.getRootElement());
		} catch (Exception ex) {
			return errorMessage("ERROR in opensdx_message");
		}
		Result verified = msg.verifySignaturesWithoutKeyVerification(); //check keys later
		if (!verified.succeeded) {
			return errorMessage("verification of signature failed"+(verified.errorMessage!=null?": "+verified.errorMessage:""));
		}
		Element content = msg.getContent();
		if (!content.getName().equals("subkey")) {
			return errorMessage("missing subkey element");
		}
		
		//check masterkey on server
		String masterkeyid = content.getChildText("masterkeyid");
		OSDXKey masterkey = keystore.getKey(masterkeyid);
		if (masterkey==null || !(masterkey instanceof MasterKey)) {
			return errorMessage("associatied masterkey is not on server.");
		}
		
		//check masterkey approved
		KeyStatus ks = keystore.getKeyStatus(masterkey.getKeyID());
		//if (ks==null || !ks.isValid()) {
		if (ks==null || !(ks.isValid() || ks.isUnapproved())) { //TODO for testing with approval_pending
			return errorMessage("associatied masterkey is not approved.");
		}
		
		System.out.println("masterkey status: "+ks.getValidityStatusName());
		
		OSDXKey subkey = OSDXKey.fromPubKeyElement(content.getChild("pubkey"));
		if (!(subkey instanceof SubKey) && subkey.isSub()) {
			return errorMessage("wrong key level: SUB Key needed.");
		}
		
		//put key in keystore
		
		//check key already on server
		String newkeyid = OSDXKey.getFormattedKeyIDModulusOnly(subkey.getKeyID());
		if (keyid_key.containsKey(newkeyid)) {
			OSDXKey old = keyid_key.get(newkeyid);
			keystore.removeKey(old);
			//resp.setRetCode(404, "FAILED");
			//resp.createErrorMessageContent("A key with this id is already registered.");
			//return resp;
		}
		((MasterKey)masterkey).addSubKey((SubKey)subkey);
		((SubKey)subkey).setParentKey((MasterKey)masterkey);
		
		//save
		keystore.addKey(subkey);
		//keystore.addKeyLog(kl);
		updateCache(subkey, null);
		saveKeyStore();
		
		//add to trusted keys
		KeyVerificator.addRatedKey(subkey, TrustRatingOfKey.RATING_MARGINAL);
		
		KeyServerResponse resp = new KeyServerResponse(serverid); 
		return resp;
	}
	
	private KeyServerResponse handlePutKeyLogsRequest(HTTPServerRequest request) throws Exception {
		OSDXMessage msg;
		try {
			msg = OSDXMessage.fromElement(request.xml.getRootElement());
		} catch (Exception ex) {
			return errorMessage("ERROR in opensdx_message");
		}
		Result verified = msg.verifySignaturesWithoutKeyVerification(); //check keys later
		if (!verified.succeeded) {
			return errorMessage("verification of signature failed"+(verified.errorMessage!=null?": "+verified.errorMessage:""));
		}
		Element content = msg.getContent();
		if (!content.getName().equals("keylogactions")) {
			return errorMessage("missing keylogactions element");
		}

		Vector<Element> elogs = content.getChildren("keylogaction");
		if (elogs==null || elogs.size()<=0) {
			return errorMessage("missing keylogaction element");
		}
		for (Element el : elogs) {
			KeyLog log = KeyLog.fromElement(el);
			Result v = log.verifyActionSHA256localproofAndSignoff();
			if (!v.succeeded) {
				return errorMessage("verification of keylogaction signature failed.");
			}
			//check toKey not revoked
			KeyStatus ks = keystore.getKeyStatus(log.getKeyIDTo());
			if (ks!=null && ks.getValidityStatus()==KeyStatus.STATUS_REVOKED) {
				return errorMessage("key is already revoked.");	
			}
			
			long datetime = System.currentTimeMillis();
			log.setDatetime(datetime);
			log.setIPv4(request.ipv4);
			log.setIPv6(request.ipv4);
			keystore.addKeyLog(log);
			log.signoffKeyServer(keyServerSigningKey);
			updateCache(null,log);

		}
		//save
		saveKeyStore();
		
		KeyServerResponse resp = new KeyServerResponse(serverid); 
		return resp;
	}
	
	private KeyServerResponse handleGetKeyServerSettingsRequest(HTTPServerRequest request) throws Exception {
		KeyServerResponse resp = new KeyServerResponse(serverid);
		try {
			Element e = new Element("keyserver");
			e.addContent("host",host);
			e.addContent("port",""+port);
			Element k = new Element("knownkeys");
			Element pk = keyServerSigningKey.getSimplePubKeyElement();
			k.addContent(pk);
			e.addContent(k);
			OSDXMessage msg = OSDXMessage.buildMessage(e, keyServerSigningKey);
			resp.setContentElement(msg.toElement());
		} catch (Exception ex) {
			resp.setRetCode(404, "FAILED");
			resp.createErrorMessageContent("Internal Error"); //should/could never happen
		}
		return resp;
	}

	public HTTPServerResponse prepareResponse(HTTPServerRequest request) throws Exception {
		if (request.method==null) return null;
		// yeah, switch cmd/method - stuff whatever...
		
		String cmd = request.cmd;
		
		if (request.method.equals("POST")) {
			if (cmd.equals("/masterkey")) {
				return handlePutMasterKeyRequest(request);
			} 
			else if (cmd.equals("/revokekey")) {
				return handlePutRevokeKeyRequest(request);
			}
			else if (cmd.equals("/subkey")) {
				return handlePutSubKeyRequest(request);
			}
			else if (cmd.equals("/keylogs")) {
				return handlePutKeyLogsRequest(request);
			}
			else if (cmd.equals("/revokemasterkey")) {
				return handlePutRevokeMasterkeyRequest(request);
			}
			else if (cmd.equals("/revokesubkey")) {
				return handlePutRevokeSubkeyRequest(request);
			}
		} 
		else if (request.method.equals("GET")) {
			if (cmd.equals("/masterpubkeys")) {
				return KeyServerResponse.createMasterPubKeyResponse(serverid,request, id_keys, keyServerSigningKey);
			}
			else if (cmd.equals("/masterpubkey")) {
				return KeyServerResponse.createMasterPubKeyToSubKeyResponse(serverid,request, keyid_key, keyServerSigningKey);
			}
			else if (cmd.equals("/identities")) {
				return KeyServerResponse.createIdentityResponse(serverid, request, keyid_key, keyServerSigningKey);
			}
			else if (cmd.equals("/keystatus")) {
				return KeyServerResponse.createKeyStatusyResponse(serverid, request, keystore, keyServerSigningKey);
			}
			else if (cmd.equals("/keylogs")) {
				return KeyServerResponse.createKeyLogResponse(serverid, request, keyid_log, keyServerSigningKey);
			}
			else if (cmd.equals("/subkeys")) {
				return KeyServerResponse.createSubKeyResponse(serverid, request, keyid_subkeys, keyServerSigningKey);
			}
			else if (cmd.equals("/pubkey")) {
				return KeyServerResponse.createPubKeyResponse(serverid, request, keyid_key, keyServerSigningKey);
			}
			else if (cmd.equals("/approve_mail")) {
				return handleVerifyRequest(request);
			}
			else if (cmd.equals("/keyserversettings")) {
				return handleGetKeyServerSettingsRequest(request);
			}
		}
		else {
			throw new Exception("NOT IMPLEMENTED::METHOD: "+request.method); // correct would be to fire a HTTP_ERR
		}
		
		System.err.println("KeyServerResponse| ::request command not recognized:: "+cmd);
		return null;
	}
	
	public void sendMail(String recipient, String subject,String message)  throws Exception {
		if (mailAuth == null) throw new RuntimeException("ERROR: mail authenticator not found.");
		if (mailProps == null) throw new RuntimeException("ERROR: mail properties not found.");
		
		//generate compressed message
		MimeBodyPart body = new MimeBodyPart();
		body.setText(message);
		
		//SMIMECompressedGenerator  gen = new SMIMECompressedGenerator();
		//MimeBodyPart mp = gen.generate(body, SMIMECompressedGenerator.ZLIB);

		Session session = Session.getDefaultInstance(mailProps, mailAuth);	
		Message msg = new MimeMessage(session);

		Address fromUser = new InternetAddress(mailProps.getProperty("senderAddress"));
		Address toUser = new InternetAddress(recipient);
		msg.setFrom(fromUser);
		msg.setRecipient(Message.RecipientType.TO, toUser);
		msg.setSubject(subject);
		msg.setContent(body.getContent(), body.getContentType());
		msg.setSentDate(new Date());
		msg.saveChanges();

		System.out.println("\nsending mail:");
		System.out.println("-------------");
		System.out.println("from    :" +mailProps.getProperty("senderAddress"));
		System.out.println("to      :" +recipient);
		System.out.println("subject :" +subject);
		System.out.println("Message :\n"+ message);
		System.out.println("\n-------------\n");
		
		Transport.send(msg);
	}
	
	public static void main(String[] args) throws Exception {
		if (args==null || args.length!=4) {
			System.out.println("usage: KeysServer -s \"password signingkey\" -m \"password mail\"");
			return;
		}
		String pwS = null;
		String pwM = null;
		if (args[0].equals("-s")) {
			pwS = args[1];
			if (args[2].equals("-m")) {
				pwM = args[3];
			}
		} else {
			if (args[0].equals("-m")) {
				pwM = args[1];
			}
			if (args[2].equals("-s")) {
				pwS = args[3];
			}
		}
		if (pwS==null || pwM == null) {
			System.out.println("usage: KeysServer -s \"password signingkey\" -m \"password mail\"");
			return;
		}
		
		KeyServerMain ks = new KeyServerMain(pwS, pwM);
		ks.startService();
	}
	
	static class MailAuthenticator extends Authenticator {

		private final String user;
		private final String password;

		public MailAuthenticator(String user, String password) {
			this.user = user;
			this.password = password;
		}

		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(this.user, this.password);
		}
	}
}


