package org.fnppl.opensdx.security;

/*
 * Copyright (C) 2010-2015 
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JDialog;

import org.fnppl.opensdx.gui.DefaultMessageHandler;
import org.fnppl.opensdx.gui.Dialogs;
import org.fnppl.opensdx.gui.MessageHandler;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;

public class SecurityControl {

	private KeyApprovingStore currentKeyStore = null;
	private KeyVerificator keyverificator = null;
	private HashMap<String, KeyClient> keyclients = new HashMap<String, KeyClient>();
	private MessageHandler messageHandler = new DefaultMessageHandler();
	private File lastDir = getDefaultDir();

	public SecurityControl() {

	}

	public Result verifyFileSignature(File signatureFile) {
		boolean ok = true;
		Element report = new Element("file_signature_verification_report");
		try {
			report.addContent("check_datetime", SecurityHelper.getFormattedDate(System.currentTimeMillis()));
			Element content = Document.fromFile(signatureFile).getRootElement();
			if (content.getName().equals("signature")) {
				Signature s = Signature.fromElement(content);
				File origFile = getSignaturesOrigFile(signatureFile, s.getDataName());
				ok = buildSignatureVerificationReportElement(s, signatureFile, origFile, null, report);
				if (ok) {
					return Result.succeeded(report);
				} else {
					return Result.error(report);
				}
			} else if (content.getName().equals("signatures")) {
				boolean verifyKeys = true;
				Vector<Element> sList = content.getChildren("signature");
				if (sList==null || sList.size()==0) {
					//Dialogs.showMessage("ERROR: unknown signature format in: "+signatureFile.getAbsolutePath()+".");
					report.addContent("error", "ERROR: unknown signature format in: "+signatureFile.getAbsolutePath()+".");
					return Result.error(report);
				}
				Signature sFirst = Signature.fromElement(sList.get(0));
				File origFile = getSignaturesOrigFile(signatureFile, sFirst.getDataName());
				if (origFile != null) {
					Vector<Signature> signatures = new Vector<Signature>();
					signatures.add(sFirst);
					for (int i=1;i<sList.size();i++) {
						Signature s = Signature.fromElement(sList.get(i));
						signatures.add(s);
					}
					//Element report = new Element("signatures_verification_report");
					for (int i=0;i<signatures.size();i++) {
						Signature signature = signatures.get(i);
						if (i==0) {
							boolean sigOK = buildSignatureVerificationReportElement(signature, signatureFile, origFile, null, report);
							if (!sigOK) {
								ok = false;
							}
						} else {
							boolean sigOK = buildSignatureVerificationReportElement(signature, signatureFile, null, signatures.get(i-1).getSignatureBytes(), report);
							if (!sigOK) {
								ok = false;
							}
						}
					}
				}
				if (ok) {
					return Result.succeeded(report);
				} else {
					return Result.error(report);
				}
			} else {
				report.addContent("error", "ERROR: unknown signature format in: "+signatureFile.getAbsolutePath()+".");
				return Result.error(report);
			}
		} catch (Exception e) {
			e.printStackTrace();
			report.addContent("error", "ERROR: verifying signature for file: "+signatureFile.getAbsolutePath()+" failed");
			return Result.error(report);
			//Dialogs.showMessage("ERROR: verifying signature for file: "+signatureFile.getAbsolutePath()+" failed");	
		}
	}
	
	private File getSignaturesOrigFile(File f, String dataname) {
		File origFile = null;
		if (f.getName().endsWith("_signature.xml")) {
			origFile = new File(f.getAbsolutePath().substring(0,f.getAbsolutePath().length()-14));
			System.out.println("checking file: "+origFile.getAbsolutePath());
			if (!origFile.exists()) origFile = null;
		}
		if (origFile == null) {
			origFile = new File(f.getParent(),dataname);
			if (!origFile.exists()) origFile = null;
		}
		if (origFile == null) {
			origFile = Dialogs.chooseOpenFile("Please select original file for signature verification", lastDir, "");
		}
		return origFile;
	}

	private boolean buildSignatureVerificationReportElement(Signature s,  File signatureFile, File origFile, byte[] signatureBytes, Element report) throws Exception {
		Result res = null;
		if (origFile != null) { // verify File
			//calc md5, sha1, sha256
			FileInputStream in = new FileInputStream(origFile);
			BufferedInputStream bin = new BufferedInputStream(in);
			byte[][] kk = SecurityHelper.getMD5SHA1SHA256(in);
			byte[] md5 = kk[1];
			byte[] sha1 = kk[2];
			byte[] sha256 = kk[3];
			in.close();
			report.addContent("signed_filename", origFile.getName());
			report.addContent("signature_filename", signatureFile.getName());
			if (md5!=null) {
				report.addContent("md5",SecurityHelper.HexDecoder.encode(md5, ':',-1));
			}
			if (sha1!=null) {
				report.addContent("sha1",SecurityHelper.HexDecoder.encode(sha1, ':',-1));
			}
			if (sha256!=null) {
				report.addContent("sha256",SecurityHelper.HexDecoder.encode(sha256, ':',-1));
			}
			res = s.tryVerification(md5, sha1, sha256);
		} else { //e.g. TSA Signature -> verify former signature bytes, 
			res = s.tryVerificationMD5SHA1SHA256(signatureBytes);
		}
		Vector<Identity> ids = null;
		if (s.getKey().isMaster()) {
			ids = requestIdentitiyDetails(s.getKey().getKeyID(), null);	
		} else {
			if(s.getKey().isSub()) {
				try {
					String parentkeyid = ((SubKey)s.getKey()).getParentKeyID();
					if (parentkeyid==null) {
						OSDXKey key = s.getKey();
						KeyClient client =  getKeyClient(key.getAuthoritativekeyserver());
						if (client!=null) {
							MasterKey masterkey = client.requestMasterPubKey(key.getKeyID());
							if (masterkey!=null) {
								ids = requestIdentitiyDetails(masterkey.getKeyID(), null);
							}
						}
					} else {
						ids = requestIdentitiyDetails(parentkeyid, null);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		if (ids!=null && ids.size()>0) {
			res.report.addContent(ids.lastElement().toElement(true));
		} else { 
			res.report.addContent("msg","no identity details found.");
		}
		report.addContent(res.report);
		if (res.succeeded) {
			//Dialogs.showMessage("Signature verified!");

			//key data matches key data on keyserver
			Result keymatch = keyverificator.matchKeyDataWithKeyServer(s.getKey());
			res.report.addContent(keymatch.report);

			//verify signature key
			Result r = keyverificator.verifyKey(s.getKey(), s.getSignDatetime());
			res.report.addContent(r.report);
			
			//keylogs
			Element keylogs_report = new Element("keylogs_report");
			keylogs_report.addContent("keyid", s.getKey().getKeyID());
			
			Vector<KeyLog> logs = null;
			if (s.getKey().isMaster()) {
				logs = requestKeyLogs(s.getKey().getKeyID(), null);
			} else {
				if (s.getKey().isSub()) {
					String pkid = ((SubKey)s.getKey()).getParentKeyID();
					if (pkid!=null && pkid.length()>0) {
						logs = requestKeyLogs(pkid, null);
					}
				}
			}
			if (logs!=null && logs.size()>0) {
				for (KeyLog kl : logs) {
					Element ekl = new Element("keylog_entry");
					ekl.addContent("keyid_from", kl.getKeyIDFrom());
					ekl.addContent("action", kl.getAction());
					ekl.addContent("date", kl.getActionDatetimeString());
					Identity id = requestCurrentIdentitiyDetails(kl.getKeyIDFrom(), null);
					if (id!=null) {
						ekl.addContent("email", id.getEmail());
					} else {
						ekl.addContent("email", "[not found]");
					}
					keylogs_report.addContent(ekl);
				}
			} else {
				keylogs_report.addContent("msg","no keylogs found");
			}
			res.report.addContent(keylogs_report);
			return r.succeeded;
		} else {
			//Dialogs.showMessage("Signature NOT verified!");
			return res.succeeded;
		}
		
		
	}
	
	public Vector<Identity> requestIdentitiyDetails(String keyid, OSDXKey signingKey) {
		if (currentKeyStore.getKeyServer() == null) {
			//Dialogs.showMessage("Sorry, no keyservers found.");
			return null;
		}
		String authServer = keyid.substring(keyid.indexOf('@')+1);
		KeyClient client =  getKeyClient(authServer);
		if (client == null) {
			return null;
		}
		try {
			client.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		Vector<Identity> ids = null;
		try {
			ids = client.requestIdentities(keyid, signingKey);
		} catch (Exception ex) {
			if (ex.getMessage()!=null && ex.getMessage().startsWith("Connection refused")) {
				//Dialogs.showMessage("Sorry, could not connect to server.");
				return null;
			} else {
				ex.printStackTrace();
			}
		}
		if (ids!=null) {
			return ids;
		}
		//Dialogs.showMessage("No identities found for "+keyid);
		return null;
	}
	
	public Identity requestCurrentIdentitiyDetails(String keyid, OSDXKey signingKey) {
		if (currentKeyStore.getKeyServer() == null) {
			//Dialogs.showMessage("Sorry, no keyservers found.");
			return null;
		}
		String authServer = OSDXKey.getKeyServerFromKeyID(keyid);
		KeyClient client =  getKeyClient(authServer);
		if (client == null) {
			return null;
		}
		try {
			client.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			Identity id = client.requestCurrentIdentity(keyid, signingKey);
			return id;
		} catch (Exception ex) {
			if (ex.getMessage()!=null && ex.getMessage().startsWith("Connection refused")) {
				//Dialogs.showMessage("Sorry, could not connect to server.");
				return null;
			} else {
				ex.printStackTrace();
			}
		}
		//Dialogs.showMessage("No identities found for "+keyid);
		return null;
	}

	
	public Vector<KeyLog> requestKeyLogs(String keyid, OSDXKey sign) {
		final Vector<KeyLog> logs = new Vector<KeyLog>();
		if (sign!=null) {
			sign.unlockPrivateKey(messageHandler);
		}
		KeyClient client = getKeyClient(OSDXKey.getKeyServerFromKeyID(keyid));
		try {
			Vector<KeyLog> rlogs = client.requestKeyLogs(keyid,sign);
			if (rlogs!=null && rlogs.size()>0) {
				logs.addAll(rlogs);
			}
		} catch (Exception ex) {
			if (ex.getMessage()!=null && ex.getLocalizedMessage().startsWith("Connection refused")) {
				return null;
			} else {
				ex.printStackTrace();
			}
		}
		return logs;
	}

	public KeyClient getKeyClient(String servername) {
		if (!keyclients.containsKey(servername)) {
			KeyServerIdentity ks = null;
			if (currentKeyStore!=null) {
				ks = currentKeyStore.getKeyServer(servername);
				if (ks==null) {
					Dialogs.showMessage("Unknown keyserver: "+servername+"\nPlease add this keyserver to your config, first.");
					return null;
				}
				keyverificator.addKeyServer(ks);
				Vector<OSDXKey> knownKeys = ks.getKnownKeys();
				for (OSDXKey k : knownKeys) {
					keyverificator.addKeyRating(k, TrustRatingOfKey.RATING_MARGINAL);
				}
			}
			if (ks==null) {
				ks = KeyServerIdentity.make(servername, KeyClient.OSDX_KEYSERVER_DEFAULT_PORT, "");
			}
			KeyClient client = new KeyClient(ks, keyverificator);
			if (ks.getKnownKeys()==null || ks.getKnownKeys().size()==0) { 
				int ans = Dialogs.showYES_NO_Dialog("Add keys?", "No trusted keys found for keyserver "+ks.getHost()+"\nDo you want to trust all public keys from keyserver settings?");
				if (ans == Dialogs.YES) {
					//request keyserver signing key and add to keyverificator
					try {
						client.connect();
						KeyServerIdentity ksid = client.requestKeyServerIdentity();
						Vector<OSDXKey> serverSigning = ksid.getKnownKeys();
						for (OSDXKey k : serverSigning) {
							ks.addKnownKey(k);
							keyverificator.addKeyRating(k, TrustRatingOfKey.RATING_MARGINAL);
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
			keyclients.put(servername, client);
			return client;
		}
		return keyclients.get(servername);
	}
	
	public File[] encryptFileDetached(File f, SymmetricKey key, Document d) throws Exception {
		File fenc = new File(f.getAbsolutePath()+".osdx.enc");
		FileInputStream in = new FileInputStream(f);
		FileOutputStream out = new FileOutputStream(fenc);
		key.encrypt(in, out);
		in.close();
		out.close();

		File fxml = new File(f.getAbsolutePath()+".osdx.enc.xml");
		d.writeToFile(fxml);
		return new File[] {fenc,fxml};
	}

	public File encryptFileInline(File f, SymmetricKey key, Document d) throws Exception {
		File fenc = new File(f.getAbsolutePath()+".enc.osdx");

		FileInputStream in = new FileInputStream(f);
		FileOutputStream out = new FileOutputStream(fenc);
		out.write("#### openSDX symmetrical encrypted file ####\n".getBytes("UTF-8"));
		d.output(out);
		//		out.write("\n".getBytes("UTF-8"));
		out.write("#### openSDX symmetrical encrypted file ####\n".getBytes("UTF-8"));
		key.encrypt(in, out);
		in.close();
		out.close();
		return fenc;
	}
	
//	public File asymmetricEncryptFileInline(File f, OSDXKey key, Document d, int blockSize) throws Exception {
//		if (blockSize>342) {
//			//max 342 bytes can be encrypted with asymmeric encryption -> use block sizes <= 342
//			throw new RuntimeException("max blocksize is 342");
//		}
//		File fenc = new File(f.getAbsolutePath()+".aenc.osdx");
//		FileOutputStream out = new FileOutputStream(fenc);
//		out.write("#### openSDX asymmetrical encrypted file ####\n".getBytes("UTF-8"));
//		d.output(out);
//		//		out.write("\n".getBytes("UTF-8"));
//		out.write("#### openSDX asymmetrical encrypted file ####\n".getBytes("UTF-8"));
//
//		FileInputStream in = new FileInputStream(f);
//		byte[] buffer = new byte[blockSize];
//		int read = -1;
//		while ((read = in.read(buffer))>0) {
//			byte[] crypt;
//			if (read==blockSize) {
//				crypt = key.encrypt(buffer);
//			} else {
//				crypt = key.encrypt(Arrays.copyOf(buffer, read));
//			}
//			//System.out.println("crpyt len="+crypt.length+"\tread = "+read);
//			out.write(crypt);	
//		}
//		in.close();
//		out.close();
//		return fenc;
//	}
//	
//	public File[] asymmetricEncryptFileDetached(File f, OSDXKey key, Document d, int blockSize) throws Exception {
//		if (blockSize>342) {
//			//max 342 bytes can be encrypted with asymmeric encryption -> use block sizes <= 342
//			throw new RuntimeException("max blocksize is 342");
//		}
//
//		File fenc = new File(f.getAbsolutePath()+".osdx.aenc");
//		FileInputStream in = new FileInputStream(f);
//		FileOutputStream out = new FileOutputStream(fenc);
//
//		byte[] buffer = new byte[blockSize];
//		int read = -1;
//		while ((read = in.read(buffer))>0) {
//			byte[] crypt;
//			if (read==blockSize) {
//				crypt = key.encrypt(buffer);
//			} else {
//				crypt = key.encrypt(Arrays.copyOf(buffer, read));
//			}
//			out.write(crypt);	
//		}
//		in.close();
//		out.close();
//
//		File fxml = new File(f.getAbsolutePath()+".osdx.aenc.xml");
//		d.writeToFile(fxml);
//		return new File[] {fenc,fxml};
//	}


	public static File getDefaultDir() {
		File f = new File(System.getProperty("user.home"));
		f = new File(f, "openSDX");
		if(!f.exists()) {
			f.mkdirs();
		}
		return f;
	}

	public KeyApprovingStore getKeyStore() {
		return currentKeyStore;
	}

	public void setKeyStore(KeyApprovingStore currentKeyStore) {
		this.currentKeyStore = currentKeyStore;
	}


	public KeyVerificator getKeyverificator() {
		return keyverificator;
	}

	public void setKeyverificator(KeyVerificator keyverificator) {
		this.keyverificator = keyverificator;
	}

	public HashMap<String, KeyClient> getKeyclients() {
		return keyclients;
	}
	public void resetKeyClients() {
		keyclients = new HashMap<String, KeyClient>();
	}

	public void setKeyclients(HashMap<String, KeyClient> keyclients) {
		this.keyclients = keyclients;
	}

	public File getLastDir() {
		return lastDir;
	}

	public void setLastDir(File lastDir) {
		this.lastDir = lastDir;
	}

	public MessageHandler getMessageHandler() {
		return messageHandler;
	}

	public void setMessageHandler(MessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}

}
