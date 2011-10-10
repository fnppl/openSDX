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
import java.net.URL;
import java.util.*;

import org.fnppl.opensdx.gui.Dialogs;
import org.fnppl.opensdx.gui.MessageHandler;
import org.fnppl.opensdx.xml.*;

public class KeyApprovingStore {
	
	private MessageHandler messageHandler = null;
	private File f = null;
	private Vector<OSDXKey> keys = null;
	private Vector<KeyLog> keylogs = null;
	private Vector<KeyServerIdentity> keyservers;
	private boolean unsavedChanges = false;
	private OSDXKey keystoreSigningKey = null;
	private byte[] keystore_sha256_proof = null;
	
	private KeyApprovingStore() {
	}
	
	public static KeyApprovingStore createNewKeyApprovingStore(File f, MessageHandler mh) throws Exception {
		KeyApprovingStore kas = new KeyApprovingStore();
		kas.f = f;
		kas.keys = new Vector<OSDXKey>();
		kas.keylogs = new Vector<KeyLog>();
		kas.keyservers = new Vector<KeyServerIdentity>();
		kas.unsavedChanges = true;
		kas.messageHandler = mh;
		return kas;
	}
	
	public void addKeyserverAndPublicKeysFromConfig(URL configURL) {
		try {
//			if (!configFile.exists() || configFile.isDirectory()) {
//				return;
//			}
			Element root = Document.fromURL(configURL).getRootElement();
			
			if (keys==null) keys = new Vector<OSDXKey>();
			if (keyservers==null) keyservers = new Vector<KeyServerIdentity>();
			
			if (root.getChild("defaultkeyservers")!=null) {
				keyservers = new Vector<KeyServerIdentity>();
				Vector<Element> v = root.getChild("defaultkeyservers").getChildren("keyserver");
				for (Element e : v) {
					unsavedChanges = true;
					keyservers.add(KeyServerIdentity.fromElement(e));
					Element eKnownKeys = e.getChild("knownkeys");
					if (eKnownKeys!=null) {
						Vector<Element> epks = eKnownKeys.getChildren("pubkey");
						if (epks!=null) {
							for (Element epk : epks) {
								keys.add(OSDXKey.fromPubKeyElement(epk));
							}
						}
					}
				}
			}
			if (root.getChild("knownapprovedkeys")!=null) {
				Vector<Element> v = root.getChild("knownapprovedkeys").getChildren("pubkey");
				for (Element e : v) {
					unsavedChanges = true;
					keys.add(OSDXKey.fromPubKeyElement(e));
				}
			}
			//TODO check localproofs and signatures 

		} catch (Exception ex) {
			Dialogs.showMessage("ERROR: init of known keyservers failed.\n"+configURL.toString());
			ex.printStackTrace();
		}
	}
	
	public void addKeyServer(KeyServerIdentity keyserver) {
		if (keyservers== null) {
			keyservers = new Vector<KeyServerIdentity>();	
		}
		keyservers.add(keyserver);
		unsavedChanges = true;
	}
	
	
	public static KeyApprovingStore fromFile(File f, MessageHandler mh) throws Exception {
		System.out.println("loading keystore from file : "+f.getAbsolutePath());
		Document d = Document.fromFile(f);
		Element e = d.getRootElement();
		if(!e.getName().equals("keystore")) {
			throw new Exception("KeyStorefile must have \"keystore\" as root-element");
		}
		
		//check signature
		boolean ignore = false;
		
		Vector<Element> ves = e.getChildren();
		
		Element eSig = null;
		Element eProof = null;
		Signature storeProof = null;
		for (Element el : ves) {
			if (el.getName().equals("signature") && storeProof==null) {
				eSig = el;
				storeProof = Signature.fromElement(eSig);
				if (!storeProof.getDataName().equals("signature of complete keystore")) {
					storeProof = null;
				}
			}
			else if (el.getName().equals("keystore_sha256_proof")) {
				eProof = el;
			}
		}
		if (eSig == null || storeProof == null) {
			if (!ignore) ignore = mh.requestIgnoreVerificationFailure();
			if (!ignore) 
				throw new Exception("KeyStore: proof of keystore failed.");
		}
		ves.remove(eSig);
		ves.remove(eProof);
		
		byte[] givenProof = null;
		if (!ignore) {
			givenProof = SecurityHelper.HexDecoder.decode(eProof.getText());
			byte[] bsha256 = SecurityHelper.getSHA256LocalProof(ves);
			
			if (!Arrays.equals(bsha256, givenProof)) {
				System.out.println("sha256localproof given     :  \t"+SecurityHelper.HexDecoder.encode(givenProof, ':', -1));
				System.out.println("sha256localproof calculated:  \t"+SecurityHelper.HexDecoder.encode(bsha256, ':', -1));
				if (!ignore) ignore = mh.requestIgnoreVerificationFailure();
				if (!ignore) 
					throw new Exception("KeyStore: proof of keystore failed.");
			}
			boolean ok = false;
			try {
				Result v = storeProof.tryVerificationMD5SHA1SHA256(givenProof); 
				ok = v.succeeded;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if(!ok) {
				if (!ignore) ignore = mh.requestIgnoreVerificationFailure();
				if (!ignore) 
					throw new Exception("KeyStore: proof of keystore failed.");
			}
		}
		KeyApprovingStore kas = new KeyApprovingStore();
		kas.f = f;
		kas.unsavedChanges = false;
		kas.messageHandler = mh;
		kas.keys = new Vector<OSDXKey>();
		kas.keylogs = new Vector<KeyLog>();
		kas.keystore_sha256_proof = givenProof;
		
		Element keys = e.getChild("keys");
		
		//add all keypairs as OSDXKey
		ves = keys.getChildren("keypair");
		if (ves.size()>0) {
			System.out.println("keystore contains "+ves.size()+" keypairs.");
			for(int i=0; i<ves.size(); i++) {
				Element ee = ves.elementAt(i);
				OSDXKey osdxk = OSDXKey.fromElement(ee);
				kas.keys.add(osdxk);
			}
			
			//check sha1localproof
//			kas.keysSHA1localproof =  SecurityHelper.HexDecoder.decode(keys.getChildText("sha1localproof"));
			byte[] sha256localproof = SecurityHelper.HexDecoder.decode(keys.getChildText("sha256localproof"));
			byte[] bsha256 = SecurityHelper.getSHA256LocalProof(ves);
			
			if (!Arrays.equals(bsha256, sha256localproof)) {
				System.out.println("sha256localproof given_in_xml:\t"+SecurityHelper.HexDecoder.encode(sha256localproof, ':', -1));
				System.out.println("sha256localproof calculated:  \t"+SecurityHelper.HexDecoder.encode(bsha256, ':', -1));
				throw new Exception("KeyStore: localproof of keypairs failed.");
			}
			
			Signature s = null;
			boolean ok = false;
			try {
				s = Signature.fromElement(keys.getChild("signature"));
				Result v = s.tryVerificationMD5SHA1SHA256(sha256localproof); 
				ok = v.succeeded;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if(!ok) {
				
				ignore = mh.requestIgnoreVerificationFailure();
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
				if (sk.getParentKeyID()!=null && sk.getParentKeyID().length()>0) {
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
							sk.setUnsavedChanges(false);
							mk.setUnsavedChanges(false);
							break;
						}
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
					KeyLog kl = KeyLog.fromElement(ee);
					Result verified = kl.verify();
					if (!verified.succeeded) {
						ignore = mh.requestIgnoreKeyLogVerificationFailure();
						if (ignore) {
							kas.keylogs.add(kl);
							kas.unsavedChanges = true;		
						}
					} else {
						kas.keylogs.add(kl);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				
			}
		}
		
		//add keyservers
		kas.keyservers = null;
		Element eK = e.getChild("keyservers");
		if (eK!=null) {
			kas.keyservers = new Vector<KeyServerIdentity>();
			Vector<Element> ekss = eK.getChildren("keyserver");
			for (Element eks : ekss) {
				KeyServerIdentity ks = KeyServerIdentity.fromElement(eks);
				kas.keyservers.add(ks);
			}
		}

		return kas;
	}
	
	public String getKeyServerNameForKey(String keyid) {
		for (KeyServerIdentity ks : keyservers) {
			if (ks.hasKnownKey(keyid)) {
				return ks.getHost();
			}
		}
		return null;
	}
	
//	public String getEmail(OSDXKey key) {
//		if (key.isMaster()) {
//			Identity id = ((MasterKey)key).getCurrentIdentity();
//			if (id!=null)  {
//				return id.getEmail();
//			}
//		} else if (key.isSub()) {
//			MasterKey mkey = ((SubKey)key).getParentKey();
//			if (mkey==null) return null;
//			return getEmail(mkey);
//		}
//		//get from KeyLogs
//		String akeyid = key.getKeyID();
//		String email = null;
//		long date = Long.MIN_VALUE;
//		for (KeyLog kl : keylogs) {
//			String keyidto = kl.getKeyIDTo();
//			if (keyidto.equals(akeyid)) {
//				if (kl.getAction().equals(KeyLogAction.APPROVAL)) {
//					Identity id = kl.getIdentity();
//					if (id!=null) {
//						if (id.getEmail() != null && kl.getActionDatetime()>date) {
//							email = id.getEmail();
//							date = kl.getActionDatetime();
//						}
//					}
//				}
//			}
//		}
//		return email;
//	}
	
	public String getEmailAndMnemonic(String keyid) {
		OSDXKey key = getKey(keyid);
		if (key!=null) {
			if (key.isMaster()) {
				Identity id = ((MasterKey)key).getCurrentIdentity();
				if (id!=null)  {
					String email = id.getEmail();
					String mnemonic = null;
					if (!id.isMnemonicRestricted()) {
						mnemonic = id.getMnemonic(); 
					}
					if (email!=null) {
						if (mnemonic!=null) {
							return email + " | "+mnemonic;
						} else {
							return email;
						}
					} else {
						if (mnemonic!=null) {
							return mnemonic;
						} else {
							return null;
						}
					}
				}
			} else if (key.isSub()) {
				MasterKey mkey = ((SubKey)key).getParentKey();
				if (mkey==null) return null;
				return getEmailAndMnemonic(mkey.getKeyID());
			}
		}
		//get from KeyLogs
		String email = null;
		String mnemonic = null; 
		long date = Long.MIN_VALUE;
		for (KeyLog kl : keylogs) {
			String keyidto = kl.getKeyIDTo();
			if (keyidto.equals(keyid)) {
				if (kl.getAction().equals(KeyLogAction.APPROVAL)) {
					Identity id = kl.getIdentity();
					if (id!=null) {
						if (id.getEmail() != null && kl.getActionDatetime()>date) {
							email = id.getEmail();
							date = kl.getActionDatetime();
							if (id.getMnemonic()!=null && !id.isMnemonicRestricted()) {
								mnemonic = id.getMnemonic();
							}
						}
					}
				}
			}
		}
		if (email!=null) {
			if (mnemonic!=null) {
				return email + " | "+mnemonic;
			} else {
				return email;
			}
		} else {
			if (mnemonic!=null) {
				return mnemonic;
			} else {
				return null;
			}
		}
	}
	
	public boolean hasUnsavedChanges() {
		if (unsavedChanges) return true;
		else {
			for (OSDXKey k : keys) {
				if (k.hasUnsavedChanges()) {
					System.out.println("unsaved changes in key: "+k.getKeyID());
					//return true;
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
	
	public Vector<KeyServerIdentity> getKeyServer() {
		return keyservers;
	}
	
	public KeyServerIdentity getKeyServer(String servername) {
		for (KeyServerIdentity ks : keyservers) {
			if (ks.getHost().equals(servername))
				return ks;
		}
		return null;
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
	
	public void removeKeyServer(KeyServerIdentity keyserver) {
		if (keyservers!=null) {
			boolean ok = keyservers.remove(keyserver);
			unsavedChanges = true;
		}
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
	
	public Vector<OSDXKey> getAllPrivateSigningKeys() {
		Vector<OSDXKey> result = new Vector<OSDXKey>();
		for (OSDXKey key : getAllSigningMasterKeys()) {
			if (key.hasPrivateKey()) {
				result.add(key);
			}
		}
		for (OSDXKey key : getAllSigningSubKeys()) {
			if (key.hasPrivateKey()) {
				result.add(key);
			}
		}
		return result;
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
		
		Element root = toElement();
		if (root == null) return false;
		Document d = Document.buildDocument(root);
		
		if (!file.exists() || messageHandler.requestOverwriteFile(file)) {
			d.writeToFile(file);
			unsavedChanges = false;
			return true;
		}
		return false;
	}
	
	public Element toElement() throws Exception {
		Element root = new Element("keystore");

		Element ek = new Element("keys");
		for (OSDXKey k : keys) {
			ek.addContent(k.toElement(messageHandler));
		}
		
		byte[] sha256localproof = SecurityHelper.getSHA256LocalProof(ek.getChildren("keypair"));
		ek.addContent("sha256localproof", SecurityHelper.HexDecoder.encode(sha256localproof, ':',-1));
		root.addContent(ek);
		
		if (keystoreSigningKey == null) {
			keystoreSigningKey = messageHandler.requestMasterSigningKey(this);
		}
		if (!keystoreSigningKey.isPrivateKeyUnlocked()) {
			keystoreSigningKey.unlockPrivateKey(messageHandler);
		}
		if (!keystoreSigningKey.isPrivateKeyUnlocked()) {
			return null;
		}
		Signature s = Signature.createSignatureFromLocalProof(sha256localproof, "signature of sha256localproof of keys", keystoreSigningKey);
		ek.addContent(s.toElement());
	
		//keylog
		if (keylogs!=null && keylogs.size()>0) {
			for (KeyLog kl : keylogs) {
				Result vr = Result.error("unknown error");
				try {
					vr = kl.verify(); 
				} catch (Exception e) {
					//e.printStackTrace();
					System.out.println("KeyLog signature NOT verified!");
				}
				if (vr.succeeded) {
					root.addContent(kl.toElement(true));
				} else {
					System.out.println("KeyLog signature NOT verified! from_keyid: "+kl.getKeyIDFrom());
					System.out.println("msg: "+vr.errorMessage);
					//Document.buildDocument(kl.toElement(true)).output(System.out);
				}
			}
		}
		
		//keyserver
		if (keyservers!=null && keyservers.size()>0) {
			Element eK = new Element("keyservers");
			for (KeyServerIdentity ks : keyservers) {
				eK.addContent(ks.toElement());
			}
			root.addContent(eK);
		}
		
		//complete signoff
		byte[] proof = SecurityHelper.getSHA256LocalProof(root.getChildren());
		root.addContent("keystore_sha256_proof",SecurityHelper.HexDecoder.encode(proof,':',-1));
		
		Signature sign = Signature.createSignatureFromLocalProof(proof, "signature of complete keystore", keystoreSigningKey);
		root.addContent(sign.toElement());
		return root;
	}
	
	
	
	public void addKey(OSDXKey key) {
		if (keys==null) {
			keys = new Vector<OSDXKey>();
		}
		//check if already in keystore
		for (int i=0;i<keys.size();i++) {
			OSDXKey k = keys.get(i);
			if (k.getKeyID().equals(key.getKeyID())) {
//				if (!k.hasPrivateKey() && k.hasPrivateKey()) {
//				}
				return;
			}
		}
		unsavedChanges = true;
		keys.add(key);
	}
	
	public void addKeyLog(KeyLog kl) {
		if (keylogs==null) keylogs = new Vector<KeyLog>();
		//check if keystore already contains keylog
		boolean add = true;
		for (int i=0;i<keylogs.size() && add;i++) {
			KeyLog log = keylogs.get(i);
			if (	kl.getActionDatetime() == log.getActionDatetime()
				 && kl.getKeyIDFrom().equals(log.getKeyIDFrom())
				 && kl.getKeyIDTo().equals(log.getKeyIDTo())) 			{
				
				//look if log has restricted fields that are unrestricted in log
				if (log.hasRestrictedFields() && !kl.hasRestrictedFields()) {
					//replace
					unsavedChanges = true;
					keylogs.remove(i);
					keylogs.add(i,kl);
				}
				add = false;
			}
		}
		if (add) {
			unsavedChanges = true;
			keylogs.add(kl);
		}
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
		SecurityHelper.sortKeyLogsbyDate(ret);
		return ret;
	}
	
	public KeyStatus getKeyStatus(String keyid) throws Exception {
		Vector<KeyLog> kls = getKeyLogs(keyid);
		if (kls==null || kls.size()==0) return null;
		for (KeyLog kl : kls) {
			System.out.println("found keylog... "+kl.getActionDatetimeString());
		}
		KeyLog kl = kls.lastElement();
		String status = kl.getAction();
		int validity = -1;
		if (status.equals(KeyLogAction.APPROVAL)) {
			validity =  KeyStatus.STATUS_VALID;
		}
		else if (status.equals(KeyLogAction.DISAPPROVAL)) {
			validity =  KeyStatus.STATUS_UNAPPROVED;
		}
		else if (status.equals(KeyLogAction.APPROVAL_PENDING)) {
			validity =  KeyStatus.STATUS_UNAPPROVED;
		}
		else if (status.equals(KeyLogAction.REVOCATION)) {
			validity =  KeyStatus.STATUS_REVOKED;
		}
		
		int approvalPoints = 100;
		int datetimeValidFrom = 0;  //TODO get from key
		int datetimeValidUntil = 0; //TODO
		
		KeyStatus ks = new KeyStatus(validity, approvalPoints, datetimeValidFrom, datetimeValidUntil, kl);
		return ks;
	}

	
	public File getFile() {
		return f;
	}
	
	public void setSigningKey(OSDXKey key) {
		keystoreSigningKey = key;
	}
	
}

