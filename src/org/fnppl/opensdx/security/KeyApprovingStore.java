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
import java.math.BigInteger;
import java.util.*;

import org.fnppl.opensdx.gui.Dialogs;
import org.fnppl.opensdx.xml.*;

public class KeyApprovingStore {
	private File f = null;
	private Vector<OSDXKeyObject> keys = null;
//	private byte[] keysSHA1localproof = null;	
//	private Element keysSignoff = null;
	
//	private Signature keySignature = null;
	private Vector<KeyLog> keylogs = null;
	private boolean unsavedChanges = false;
	
	public KeyApprovingStore() {
		
	}
	
	public static KeyApprovingStore createNewKeyApprovingStore(File f) throws Exception {
		KeyApprovingStore kas = new KeyApprovingStore();
		kas.f = f;
		kas.keys = new Vector<OSDXKeyObject>();
		kas.keylogs = new Vector<KeyLog>();
		kas.unsavedChanges = true;
		return kas;
	}
	
	
	public static KeyApprovingStore fromFile(File f) throws Exception {
		System.out.println("loading keystore from file : "+f.getAbsolutePath());
		Document d = Document.fromFile(f);
		Element e = d.getRootElement();
		if(!e.getName().equals("keystore")) {
			throw new Exception("KeyStorefile must have \"keystore\" as root-element");
		}
		
		KeyApprovingStore kas = new KeyApprovingStore();
		kas.f = f;
		kas.unsavedChanges = false;
		
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
				int a = Dialogs.showYES_NO_Dialog("Verification failed", "KeyStore:  localproof and signoff of keypairs failed.\nIgnore?");
				
				if (a!=Dialogs.YES) 
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
				KeyLog kl = KeyLog.fromElement(ee);
				kas.keylogs.add(kl);
				if (!kl.isVerified()) kas.unsavedChanges = true;
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
	
	public Vector<OSDXKeyObject> getAllSigningKeys() {
		Vector<OSDXKeyObject> skeys = new Vector<OSDXKeyObject>();
		for (OSDXKeyObject k : keys) {
			if (k.allowsSigning()) {
				skeys.add(k);
			}
		}
		return skeys;
	}
	public Vector<OSDXKeyObject> getAllDecyrptionKeys() {
		Vector<OSDXKeyObject> skeys = new Vector<OSDXKeyObject>();
		for (OSDXKeyObject k : keys) {
			int u = k.getUsage();
			if ((u==OSDXKeyObject.USAGE_CRYPT || u==OSDXKeyObject.USAGE_WHATEVER) && k.hasPrivateKey()) {
				skeys.add(k);
			}
		}
		return skeys;
	}
	
	public Vector<OSDXKeyObject> getAllEncyrptionKeys() {
		Vector<OSDXKeyObject> skeys = new Vector<OSDXKeyObject>();
		for (OSDXKeyObject k : keys) {
			int u = k.getUsage();
			if (u==OSDXKeyObject.USAGE_CRYPT || u==OSDXKeyObject.USAGE_WHATEVER) {
				skeys.add(k);
			}
		}
		return skeys;
	}
	
	public void toFile(File file) throws Exception {
		this.f = file;
		Element root = new Element("keystore");
		//keys
		Element ek = new Element("keys");
		for (OSDXKeyObject k : keys) {
			ek.addContent(k.toElement());
		}
		
		byte[] sha1localproof = SecurityHelper.getSHA1LocalProof(ek.getChildren("keypair"));
		ek.addContent("sha1localproof", SecurityHelper.HexDecoder.encode(sha1localproof, ':',-1));
		root.addContent(ek);
		
		byte[][] kk = SecurityHelper.getMD5SHA1SHA256(sha1localproof);
		byte[] md5sha1sha256 = kk[0];
		byte[] md5 = kk[1];
		byte[] sha1 = kk[2];
		byte[] sha256 = kk[3];
		
		Vector<OSDXKeyObject> signoffkeys = getAllSigningKeys();
		if (signoffkeys.size()==0) {
			throw new Exception("KeyStore:  No signing keys available");
		}
		
		Vector<String> keynames = new Vector<String>();
		for (int i=0; i<signoffkeys.size(); i++) {
			keynames.add(signoffkeys.get(i).getKeyID());
		}
		
		int ans = Dialogs.showSelectDialog("Select signing key", "Please select a key to sign all keypairs in keystore", keynames);
		if (ans >= 0) {
			Signature s = Signature.createSignature(md5,sha1,sha256, "sign of localproof-sha1 of keys", signoffkeys.elementAt(ans));
			ek.addContent(s.toElement());
			
		} else {
			throw new Exception("KeyStore:  signoff of localproof of keypairs failed.");
		}
			
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
					signoffkeys = getAllSigningKeys();
					keynames = new Vector<String>();
					for (int i=0;i<signoffkeys.size();i++) {
						keynames.add(signoffkeys.get(i).getKeyID());
					}
					ans = Dialogs.showSelectDialog("Select signing key", "Please select a key to sign keylog in keystore", keynames);
					if (ans >= 0) {
						kl.signoff(signoffkeys.elementAt(ans));
					} else {
						throw new Exception("KeyStore:  signoff of localproof of keylog failed.");
					}
				}
				root.addContent(kl.toElement());
			}
		}
		
		Document d = Document.buildDocument(root);
		
		if (!file.exists() || Dialogs.YES == Dialogs.showYES_NO_Dialog("OVERWRITE?", "File \""+file.getName()+"\" exits?\nAll comments will be deleted.\nDo you really want to overwrite?")) {
			d.writeToFile(file);
			unsavedChanges = false;
		}
	}
	
	public void addKey(OSDXKeyObject key) {
		unsavedChanges = true;
		keys.add(key);
	}
	
	public File getFile() {
		return f;
	}
}

