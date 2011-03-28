package org.fnppl.opensdx.security;


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

import java.io.*;
import java.util.*;

import org.fnppl.opensdx.gui.MessageHandler;
import org.fnppl.opensdx.xml.*;

public class KeyApprovingStore {
	
	private MessageHandler messageHandler = null;
	private File f = null;
	private Vector<OSDXKeyObject> keys = null;
	private Vector<KeyLog> keylogs = null;
	private boolean unsavedChanges = false;
	private OSDXKeyObject keystoreSigningKey = null;
	
	private KeyApprovingStore() {
	}
	
	public static KeyApprovingStore createNewKeyApprovingStore(File f, MessageHandler mh) throws Exception {
		KeyApprovingStore kas = new KeyApprovingStore();
		kas.f = f;
		kas.keys = new Vector<OSDXKeyObject>();
		kas.keylogs = new Vector<KeyLog>();
		kas.unsavedChanges = true;
		kas.messageHandler = mh;
		return kas;
	}
	
	
	public static KeyApprovingStore fromFile(File f, MessageHandler mh) throws Exception {
		System.out.println("loading keystore from file : "+f.getAbsolutePath());
		Document d = Document.fromFile(f);
		Element e = d.getRootElement();
		if(!e.getName().equals("keystore")) {
			throw new Exception("KeyStorefile must have \"keystore\" as root-element");
		}
		
		KeyApprovingStore kas = new KeyApprovingStore();
		kas.f = f;
		kas.unsavedChanges = false;
		kas.messageHandler = mh;
		
		Element keys = e.getChild("keys");
		
		//add all keypairs as OSDXKeyObject
		Vector<Element> ves = keys.getChildren("keypair");
		if (ves.size()>0) {
			System.out.println("keypairs found: "+ves.size());
			kas.keys = new Vector<OSDXKeyObject>();
			for(int i=0; i<ves.size(); i++) {
				Element ee = ves.elementAt(i);
				OSDXKeyObject osdxk = OSDXKeyObject.fromElement(ee);
				kas.keys.add(osdxk);
			}
			
			//check sha1localproof
//			kas.keysSHA1localproof =  SecurityHelper.HexDecoder.decode(keys.getChildText("sha1localproof"));
			byte[] sha1localproof = SecurityHelper.HexDecoder.decode(keys.getChildText("sha1localproof"));
			byte[] bsha1 = SecurityHelper.getSHA1LocalProof(ves);
			
			if (!Arrays.equals(bsha1, sha1localproof)) {
				System.out.println("sha1localproof given_in_xml:\t"+SecurityHelper.HexDecoder.encode(sha1localproof, ':', -1));
				System.out.println("sha1localproof calculated:  \t"+SecurityHelper.HexDecoder.encode(bsha1, ':', -1));
				throw new Exception("KeyStore: localproof of keypairs failed.");
			}
			
			Signature s = null;
			boolean ok = false;
			try {
				s = Signature.fromElement(keys.getChild("signature"));
				ok = s.tryVerificationMD5SHA1SHA256(sha1localproof);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if(!ok) {
				
				boolean ignore = mh.requestIgnoreVerificationFailure();
				if (!ignore) 
					throw new Exception("KeyStore:  signoff of localproof of keypairs failed.");
				kas.unsavedChanges = true;
			}
			
		} else {
			//TODO no keypairs in store
		}
		
		//add keylog (includes verify localproof and signoff)
		Vector<Element> vkl = e.getChildren("keylog");
		if (vkl.size()>0) {
			System.out.println("keylogs found: "+vkl.size());
			kas.keylogs = new Vector<KeyLog>();
			for(int i=0; i<vkl.size(); i++) {
				Element ee = vkl.elementAt(i);
				try {
					KeyLog kl = KeyLog.fromElement(ee,true);
					kas.keylogs.add(kl);
					if (!kl.isVerified()) kas.unsavedChanges = true;
				} catch (Exception ex) {
					if (ex.getMessage().startsWith("KeyStore:  localproof and signoff of keylog failed.")) {
						boolean ignore = mh.requestIgnoreKeyLogVerificationFailure();
						if (ignore) {
							KeyLog kl = KeyLog.fromElement(ee,false);
							kas.keylogs.add(kl);
							kas.unsavedChanges = true;		
						}
					} else {
						ex.printStackTrace();
					}
				}
				
			}
		}
		
			
		return kas;
	}
	
	public boolean hasUnsavedChanges() {
		if (unsavedChanges) return true;
		else {
			for (OSDXKeyObject k : keys) {
				if (k.hasUnsavedChanges()) {
					System.out.println("unsaved changes in key: "+k.getKeyID());
					return true;
				}
			}
		}
		return false;
	}
	
	public Vector<OSDXKeyObject> getAllKeys() {
		return keys;
	}
	
	public Vector<OSDXKeyObject> getSubKeys(String keyid) {
		Vector<OSDXKeyObject> ret = new Vector<OSDXKeyObject>();
		for (OSDXKeyObject k : keys) {
			try {
				if (k.isSub() && k.getParentKeyID().equals(keyid)) {
					ret.add(k);
				}
			} catch (Exception ex) {}
		}
		return ret;
	}
	
	public OSDXKeyObject getKey(String keyid) {
		byte[] idbytes = SecurityHelper.getBytesFromKeyID(keyid);
		if (idbytes==null) return null;
		for (OSDXKeyObject k : keys) {
			if (Arrays.equals(k.getKeyModulusSHA1bytes(), idbytes)) {
				return k;
			}
		}
		return null;
	}
	
	public void removeKey(OSDXKeyObject key) {
		keys.remove(key);
		unsavedChanges = true;
	}
	
	public void removeKeyLog(KeyLog keylog) {
		if (keylogs != null) {
			keylogs.remove(keylog);
			unsavedChanges = true;
		}
	}
	
	public Vector<OSDXKeyObject> getRevokeKeys(String keyid) {
		Vector<OSDXKeyObject> ret = new Vector<OSDXKeyObject>();
		for (OSDXKeyObject k : keys) {
			if (k.isRevoke() && k.getParentKeyID().equals(keyid)) {
				ret.add(k);
			}
		}
		return ret;
	}
	
	public Vector<OSDXKeyObject> getAllSigningSubKeys() {
		Vector<OSDXKeyObject> skeys = new Vector<OSDXKeyObject>();
		for (OSDXKeyObject k : keys) {
			if (k.isSub() && k.allowsSigning()) {
				skeys.add(k);
			}
		}
		return skeys;
	}
	
	public Vector<OSDXKeyObject> getAllSigningMasterKeys() {
		Vector<OSDXKeyObject> skeys = new Vector<OSDXKeyObject>();
		for (OSDXKeyObject k : keys) {
			if (k.isMaster() && k.allowsSigning()) {
				skeys.add(k);
			}
		}
		return skeys;
	}
	
	
	public Vector<OSDXKeyObject> getAllDecyrptionSubKeys() {
		Vector<OSDXKeyObject> skeys = new Vector<OSDXKeyObject>();
		for (OSDXKeyObject k : keys) {
			if (k.isSub()) {
				int u = k.getUsage();
				if ((u==OSDXKeyObject.USAGE_CRYPT || u==OSDXKeyObject.USAGE_WHATEVER) && k.hasPrivateKey()) {
					skeys.add(k);
				}
			}
		}
		return skeys;
	}
	
	public Vector<OSDXKeyObject> getAllEncyrptionSubKeys() {
		Vector<OSDXKeyObject> skeys = new Vector<OSDXKeyObject>();
		for (OSDXKeyObject k : keys) {
			if (k.isSub()) {
				int u = k.getUsage();
				if (u==OSDXKeyObject.USAGE_CRYPT || u==OSDXKeyObject.USAGE_WHATEVER) {
					skeys.add(k);
				}
			}
		}
		return skeys;
	}
	
	public void toFile(File file) throws Exception {
		this.f = file;
		Element root = new Element("keystore");

		Element ek = new Element("keys");
		for (OSDXKeyObject k : keys) {
			ek.addContent(k.toElement(messageHandler));
		}
		
		byte[] sha1localproof = SecurityHelper.getSHA1LocalProof(ek.getChildren("keypair"));
		ek.addContent("sha1localproof", SecurityHelper.HexDecoder.encode(sha1localproof, ':',-1));
		root.addContent(ek);
		
		byte[][] kk = SecurityHelper.getMD5SHA1SHA256(sha1localproof);
		byte[] md5sha1sha256 = kk[0];
		byte[] md5 = kk[1];
		byte[] sha1 = kk[2];
		byte[] sha256 = kk[3];
		
		if (keystoreSigningKey == null) {
			keystoreSigningKey = messageHandler.requestMasterSigningKey(this);
		}
		if (!keystoreSigningKey.isPrivateKeyUnlocked()) {
			keystoreSigningKey.unlockPrivateKey(messageHandler);
		}
		
		Signature s = Signature.createSignature(md5,sha1,sha256, "sign of localproof-sha1 of keys", keystoreSigningKey);
		ek.addContent(s.toElement());
		
		
		//keylog
		if (keylogs!=null && keylogs.size()>0) {
			for (KeyLog kl : keylogs) {
				boolean v = false;
				try { 
					v = kl.verifySHA1localproofAndSignoff();
				} catch (Exception e) {
					//e.printStackTrace();
					System.out.println("new signature for keylog needed...");
				}
				if (!v) {
					kl.signoff(keystoreSigningKey);
				}
				root.addContent(kl.toElement());
			}
		}
		
		Document d = Document.buildDocument(root);
		
		if (!file.exists() || messageHandler.requestOverwriteFile(file)) {
			d.writeToFile(file);
			unsavedChanges = false;
		}
	}
	
	
	
	
	public void addKey(OSDXKeyObject key) {
		unsavedChanges = true;
		keys.add(key);
	}
	
	public void addKeyLog(KeyLog kl) {
		unsavedChanges = true;
		if (keylogs==null) keylogs = new Vector<KeyLog>();
		keylogs.add(kl);
	}
	
	public Vector<KeyLog> getKeyLogs() {
		return keylogs;
	}
	
	public Vector<KeyLog> getKeyLogs(String keyid) {
		if (keylogs==null) return null;
		byte[] kidBytes = SecurityHelper.getBytesFromKeyID(keyid);
		
		Vector<KeyLog> ret = new Vector<KeyLog>();
		for (KeyLog kl : keylogs) {
			if (Arrays.equals(kidBytes, SecurityHelper.getBytesFromKeyID(kl.getKeyIDTo()))) {
				ret.add(kl);
			}
		}
		sortKeyLogsbyDate(ret);
		return ret;
	}
	
	public KeyStatus getKeyStatus(String keyid) throws Exception {
		Vector<KeyLog> kls = getKeyLogs(keyid);
		if (kls==null || kls.size()==0) return null;
		for (KeyLog kl : kls) {
			System.out.println("found keylog... "+kl.getDateString());
		}
		KeyLog kl = kls.lastElement();
		String status = kl.getStatus();
		int validity = -1;
		if (status.equals(KeyLog.APPROVAL)) validity =  KeyStatus.STATUS_VALID;
		else if (status.equals(KeyLog.DISAPPROVAL)) validity =  KeyStatus.STATUS_UNAPPROVED;
		else if (status.equals(KeyLog.APPROVAL_PENDING)) validity =  KeyStatus.STATUS_UNAPPROVED;
		else if (status.equals(KeyLog.REVOCATION)) validity =  KeyStatus.STATUS_REVOKED;
		
		int approvalPoints = 100;
		int datetimeValidFrom = 0;  //TODO get from key
		int datetimeValidUntil = 0; //TODO
		
		KeyStatus ks = new KeyStatus(validity, approvalPoints, datetimeValidFrom, datetimeValidUntil);
		return ks;
	}
	
	public static void sortKeyLogsbyDate(Vector<KeyLog> keylogs) {
		Collections.sort(keylogs, new Comparator<KeyLog>() { 
			
			public int compare(KeyLog kl1, KeyLog kl2) {
				try {
					return (int)(kl1.getDate()-kl2.getDate());
				}	catch (Exception ex) {
					ex.printStackTrace();
				}
				return 0;
			}
		});
	}
	
	public File getFile() {
		return f;
	}
	
	public void setSigningKey(OSDXKeyObject key) {
		keystoreSigningKey = key;
	}
}

