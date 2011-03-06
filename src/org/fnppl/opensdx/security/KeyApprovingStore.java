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
	private byte[] keysSHA1localproof = null;
	private Element keysSignoff = null;
	private Vector<KeyLog> keylogs = null;
	
	
	public KeyApprovingStore() {
		
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
			kas.keysSHA1localproof =  SecurityHelper.HexDecoder.decode(keys.getChildText("sha1localproof"));
			byte[] bsha1 = SecurityHelper.getSHA1LocalProof(ves);
			
			if (!Arrays.equals(bsha1, kas.keysSHA1localproof)) {
				System.out.println("sha1localproof target: "+SecurityHelper.HexDecoder.encode(kas.keysSHA1localproof, '\0', -1));
				System.out.println("sha1localproof real  : "+SecurityHelper.HexDecoder.encode(bsha1, '\0', -1));
				throw new Exception("KeyStore: localproof of keypairs failed.");
			}
			
			//check signoff
			kas.keysSignoff = keys.getChild("signoff");
			boolean verifyKeysSignoff = SignoffElement.verifySignoff(
					kas.keysSignoff, 
					kas.keysSHA1localproof
				);
			//verifyKeysSignoff = true;
			if(!verifyKeysSignoff) {
				throw new Exception("KeyStore:  signoff of localproof of keypairs failed.");
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
			}
		}
		
			
		return kas;
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
	
	public void toFile(File file) throws Exception {
		Element root = new Element("keystore");
		//keys
		Element ek = new Element("keys");
		for (OSDXKeyObject k : keys) {
			ek.addContent(k.toElement());
		}
		
		byte[] sha1localproof = SecurityHelper.getSHA1LocalProof(ek.getChildren("keypair"));
		ek.addContent("sha1localproof",SecurityHelper.HexDecoder.encode(sha1localproof, '\0',-1));
		root.addContent(ek);
		
		//keysSignoff = null; //for testing
		if (keysSignoff!=null && Arrays.equals(keysSHA1localproof, sha1localproof)) {
			//old signature still fits
			ek.addContent(XMLHelper.cloneElement(keysSignoff));
		} else {
			//new signature needed
			if (keysSignoff!=null)
				System.out.println("old sig keys localproof: "+SecurityHelper.HexDecoder.encode(keysSHA1localproof,'\0',-1));
			System.out.println("new sig keys localproof: "+SecurityHelper.HexDecoder.encode(sha1localproof,'\0',-1));
			keysSHA1localproof = sha1localproof;
			Vector<OSDXKeyObject> signoffkeys = getAllSigningKeys();
			Vector<String> keynames = new Vector<String>();
			for (int i=0;i<signoffkeys.size();i++) {
				keynames.add(signoffkeys.get(i).getKeyID());
			}
			int ans = Dialogs.showSelectDialog("Select signing key", "Please select a key to sign all keypairs in keystore", keynames);
			if (ans >= 0) {
				keysSignoff = SignoffElement.getSignoffElement(sha1localproof, signoffkeys.get(ans));
				ek.addContent(keysSignoff);
			} else {
				throw new Exception("KeyStore:  signoff of localproof of keypairs failed.");
			}
		}
		
		
		//keylog
		if (keylogs!=null && keylogs.size()>0) {
			for (KeyLog kl : keylogs) {
				boolean v = kl.verifySHA1localproofAndSignoff();
				//v  = false; //for testing
				if (!v) {
					Vector<OSDXKeyObject> signoffkeys = getAllSigningKeys();
					Vector<String> keynames = new Vector<String>();
					for (int i=0;i<signoffkeys.size();i++) {
						keynames.add(signoffkeys.get(i).getKeyID());
					}
					int ans = Dialogs.showSelectDialog("Select signing key", "Please select a key to sign keylog in keystore", keynames);
					if (ans>=0) {
						kl.signoff(signoffkeys.get(ans));
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
		}
		
	}
}

