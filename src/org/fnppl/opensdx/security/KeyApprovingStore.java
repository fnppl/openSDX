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
	private Vector<OSDXKey> keys = null;
	private Vector<KeyLog> keylogs = null;
	private boolean unsavedChanges = false;
	private OSDXKey keystoreSigningKey = null;
	
	private KeyApprovingStore() {
	}
	
	public static KeyApprovingStore createNewKeyApprovingStore(File f, MessageHandler mh) throws Exception {
		KeyApprovingStore kas = new KeyApprovingStore();
		kas.f = f;
		kas.keys = new Vector<OSDXKey>();
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
		kas.keys = new Vector<OSDXKey>();
		kas.keylogs = new Vector<KeyLog>();
		
		Element keys = e.getChild("keys");
		
		//add all keypairs as OSDXKey
		Vector<Element> ves = keys.getChildren("keypair");
		if (ves.size()>0) {
			System.out.println("keystore contains "+ves.size()+" keypairs.");
			for(int i=0; i<ves.size(); i++) {
				Element ee = ves.elementAt(i);
				OSDXKey osdxk = OSDXKey.fromElement(ee);
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
				Result v = s.tryVerificationMD5SHA1SHA256(sha1localproof); 
				ok = v.succeeded;
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
		//connet subkeys to masterkeys
		Vector<MasterKey> masterkeys = kas.getAllMasterKeys();
		for (OSDXKey k : kas.keys) {
			if (k instanceof SubKey) {
				SubKey sk = (SubKey)k;
				byte[] parentkeyid = SecurityHelper.HexDecoder.decode(OSDXKey.getFormattedKeyIDModulusOnly(sk.getParentKeyID()));
				for (MasterKey mk : masterkeys) {
					//System.out.println("comparing: "+SecurityHelper.HexDecoder.encode(parentkeyid, '\0', -1)+" - "+SecurityHelper.HexDecoder.encode(mk.getKeyModulusSHA1bytes(), '\0', -1));
					if (Arrays.equals(mk.getKeyModulusSHA1bytes(), parentkeyid)) {
						sk.setParentKey(mk);
						if (sk.isRevoke()) {
							mk.addRevokeKey((RevokeKey)sk);
						} else {
							mk.addSubKey(sk);
						}
						break;
					}
				}
			}
		}
		
		
		//add keylog (includes verify localproof and signoff)
		Vector<Element> vkl = e.getChildren("keylog");
		if (vkl.size()>0) {
			System.out.println("keystore contains "+vkl.size()+" keylogs.");
			for(int i=0; i<vkl.size(); i++) {
				Element ee = vkl.elementAt(i);
				try {
					KeyLog kl = KeyLog.fromElement(ee,true);
					kas.keylogs.add(kl);
					if (!kl.isVerified()) kas.unsavedChanges = true;
				} catch (Exception ex) {
					if (ex.getMessage()!=null && ex.getMessage().startsWith("KeyStore:  localproof and signoff of keylog failed.")) {
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
			for (OSDXKey k : keys) {
				if (k.hasUnsavedChanges()) {
					System.out.println("unsaved changes in key: "+k.getKeyID());
					return true;
				}
			}
		}
		return false;
	}
	
	public Vector<OSDXKey> getAllKeys() {
		return keys;
	}
	
	public Vector<SubKey> getSubKeys(String keyid) {
		Vector<SubKey> ret = new Vector<SubKey>();
		for (OSDXKey k : keys) {
			try {
				if (k instanceof SubKey && k.isSub() && ((SubKey)k).getParentKeyID().equals(keyid)) {
					ret.add((SubKey)k);
				}
			} catch (Exception ex) {}
		}
		return ret;
	}
	
	public OSDXKey getKey(String keyid) {
		byte[] idbytes = SecurityHelper.HexDecoder.decode(OSDXKey.getFormattedKeyIDModulusOnly(keyid));
		if (idbytes==null) return null;
		for (OSDXKey k : keys) {
			if (Arrays.equals(k.getKeyModulusSHA1bytes(), idbytes)) {
				return k;
			}
		}
		return null;
	}
	
	public void removeKey(OSDXKey key) {
		boolean ok = keys.remove(key);
		//System.out.println("removing key: "+ok);
		unsavedChanges = true;
	}
	
	public void removeKeyLog(KeyLog keylog) {
		if (keylogs != null) {
			keylogs.remove(keylog);
			unsavedChanges = true;
		}
	}
	
	public Vector<RevokeKey> getRevokeKeys(String keyid) {
		Vector<RevokeKey> ret = new Vector<RevokeKey>();
		for (OSDXKey k : keys) {
			if (k instanceof RevokeKey && k.isRevoke() && ((RevokeKey)k).getParentKeyID().equals(keyid)) {
				ret.add((RevokeKey)k);
			}
		}
		return ret;
	}
	
	public Vector<SubKey> getAllSigningSubKeys() {
		Vector<SubKey> skeys = new Vector<SubKey>();
		for (OSDXKey k : keys) {
			if (k instanceof SubKey && k.isSub() && k.allowsSigning()) {
				skeys.add((SubKey)k);
			}
		}
		return skeys;
	}
	
	public Vector<MasterKey> getAllSigningMasterKeys() {
		Vector<MasterKey> skeys = new Vector<MasterKey>();
		for (OSDXKey k : keys) {
			if (k instanceof MasterKey && k.isMaster() && k.allowsSigning()) {
				skeys.add((MasterKey)k);
			}
		}
		return skeys;
	}
	
	public Vector<MasterKey> getAllMasterKeys() {
		Vector<MasterKey> skeys = new Vector<MasterKey>();
		for (OSDXKey k : keys) {
			if (k instanceof MasterKey && k.isMaster()) {
				skeys.add((MasterKey)k);
			}
		}
		return skeys;
	}
	
	public Vector<SubKey> getAllDecyrptionSubKeys() {
		Vector<SubKey> skeys = new Vector<SubKey>();
		for (OSDXKey k : keys) {
			if (k instanceof SubKey && k.isSub()) {
				int u = k.getUsage();
				if ((u==OSDXKey.USAGE_CRYPT || u==OSDXKey.USAGE_WHATEVER) && k.hasPrivateKey()) {
					skeys.add((SubKey)k);
				}
			}
		}
		return skeys;
	}
	
	public Vector<SubKey> getAllEncyrptionSubKeys() {
		Vector<SubKey> skeys = new Vector<SubKey>();
		for (OSDXKey k : keys) {
			if (k instanceof SubKey && k.isSub()) {
				int u = k.getUsage();
				if (u==OSDXKey.USAGE_CRYPT || u==OSDXKey.USAGE_WHATEVER) {
					skeys.add((SubKey)k);
				}
			}
		}
		return skeys;
	}
	
	public boolean toFile(File file) throws Exception {
		this.f = file;
		Element root = new Element("keystore");

		Element ek = new Element("keys");
		for (OSDXKey k : keys) {
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
		if (!keystoreSigningKey.isPrivateKeyUnlocked()) {
			return false;
		}
		Signature s = Signature.createSignature(md5,sha1,sha256, "signature of sha1localproof of keys", keystoreSigningKey);
		ek.addContent(s.toElement());
		
		
		//keylog
		if (keylogs!=null && keylogs.size()>0) {
			for (KeyLog kl : keylogs) {
				boolean v = false;
				try {
					Result vr = kl.verifySHA1localproofAndSignoff(); 
					v = vr.succeeded;
				} catch (Exception e) {
					//e.printStackTrace();
					System.out.println("KeyLog signature NOT verified!");
				}
				if (v) {
					root.addContent(kl.toFullElement());
				}
			}
		}
		
		Document d = Document.buildDocument(root);
		
		if (!file.exists() || messageHandler.requestOverwriteFile(file)) {
			d.writeToFile(file);
			unsavedChanges = false;
			return true;
		}
		return false;
	}
	
	
	
	
	public void addKey(OSDXKey key) {
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
		String akeyid = OSDXKey.getFormattedKeyIDModulusOnly(keyid);
		
		Vector<KeyLog> ret = new Vector<KeyLog>();
		for (KeyLog kl : keylogs) {
			String keyidto = OSDXKey.getFormattedKeyIDModulusOnly(kl.getKeyIDTo());
			if (keyidto.equals(akeyid)) {
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
		String status = kl.getAction();
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
	
	public void setSigningKey(OSDXKey key) {
		keystoreSigningKey = key;
	}
}

