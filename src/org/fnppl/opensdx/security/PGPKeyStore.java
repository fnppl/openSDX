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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Security;
import java.util.Iterator;
import java.util.Vector;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.provider.JCERSAPrivateKey;
import org.bouncycastle.jce.provider.JCERSAPublicKey;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
import org.fnppl.opensdx.gui.DefaultMessageHandler;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;


public class PGPKeyStore {

	private File publicKeyFile = null;
	private File privateKeyFile =null;
	private PGPPublicKeyRingCollection keyringcollectionPub;
	private PGPSecretKeyRingCollection keyringcollectionSec; 

	public PGPKeyStore(File public_key_file, File private_key_file) {
		Security.addProvider(new BouncyCastleProvider());
		publicKeyFile = public_key_file;
		privateKeyFile = private_key_file;
		
		if (privateKeyFile==null || !privateKeyFile.exists() || publicKeyFile == null || !publicKeyFile.exists()) {
			throw new RuntimeException("missing keyfiles.");
		}
	}
	
	public PGPKeyStore() {
		Security.addProvider(new BouncyCastleProvider());
		File path = new File(System.getProperty("user.home"));
		if (path.exists()) {
			path = new File(path,".gnupg");
		}
		if (path.exists()) {
			publicKeyFile = new File(path, "pubring.gpg");
			privateKeyFile = new File(path, "secring.gpg");
		}
		
		if (privateKeyFile==null || !privateKeyFile.exists() || publicKeyFile == null || !publicKeyFile.exists()) {
			throw new RuntimeException("missing keyfiles.");
		}
	}

	public static byte[] getExponentFromSecretKey(byte[] sec_key_bytes, char[] password) {
		PGPSecretKey key = null;
		try {
			PGPSecretKeyRing ring = new PGPSecretKeyRing(sec_key_bytes);
			Iterator<PGPSecretKey> iterKeys = ring.getSecretKeys();
			while (iterKeys.hasNext() && key==null) {
				key = iterKeys.next();
			}
			if (key != null) {
				try {
					PGPPrivateKey priv_key = key.extractPrivateKey(password, "BC");
					if (priv_key != null) {
						JCERSAPrivateKey priv_keyparam = (JCERSAPrivateKey)priv_key.getKey(); 
						byte[] priv_exp_bytes = priv_keyparam.getPrivateExponent().toByteArray();
						//System.out.println("skeyparam: exp :: "+SecurityHelper.HexDecoder.encode(s_exp_bytes));
						System.out.println("private key unlocked");
						return priv_exp_bytes;
					}
				} catch (Exception ex) {
					System.out.println("Error unlocking private key, wrong password?");					
				}
			} else {
				System.out.println("format error, no private key found");
			}
		} catch (Exception ex) {
			if (key!=null) {
				System.out.println("could NOT unlock private key");
			}
			ex.printStackTrace();
		}
		return null;
	}

	public PGPSecretKey findSecretKey(long keyID) throws Exception {
		if (keyringcollectionSec==null) {
			InputStream keyIn = new FileInputStream(privateKeyFile);
			keyringcollectionSec = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(keyIn));
		}
		PGPSecretKey secretKey = keyringcollectionSec.getSecretKey(keyID);
		return secretKey;
	}

	public Vector<OSDXKey> parseOSDXKeys() throws Exception {
		Vector<OSDXKey> keys = new Vector<OSDXKey>();
		InputStream keyIn = new FileInputStream(publicKeyFile);
		keyringcollectionPub = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(keyIn));

		Iterator<PGPPublicKeyRing> iterKeyRings = keyringcollectionPub.getKeyRings();
		while (iterKeyRings.hasNext()) {
			PGPPublicKeyRing ring = iterKeyRings.next();
			Iterator<PGPPublicKey> iterKeys = ring.getPublicKeys();
			while (iterKeys.hasNext()) {
				try {
					PGPPublicKey key = iterKeys.next();
					String userID = "";
					Iterator iterUserIDs = key.getUserIDs();
					Element eIds = new Element("identities");
					int idcount = 0;
					while (iterUserIDs.hasNext()) {
						Object o = iterUserIDs.next();
						String sid = o.toString();
						userID += ", "+sid;
						Identity id = Identity.newEmptyIdentity();
						id.setIdentNum(idcount+1);
						int email_start = sid.lastIndexOf("<");
						int email_end = sid.lastIndexOf(">");
						id.setMnemonic("pgp/gpg import");
						id.set_mnemonic_restricted(false);
						id.setNote(sid);
						id.set_note_restricted(false);
						if (email_start>0 && email_end>0 && email_start < email_end) {
							id.setEmail(sid.substring(email_start+1,email_end));
						}
						eIds.addContent(id.toElement(true));
						idcount++;
					}
					if (userID.length()>2) userID = userID.substring(2);
					System.out.println("\n\nkey :: "+Long.toHexString(key.getKeyID())+", "+key.getAlgorithm()+", "+key.getBitStrength()+", "+SecurityHelper.HexDecoder.encode(key.getFingerprint())+", "+userID+", "+key.isEncryptionKey()+", "+key.isMasterKey()+", "+key.isRevoked());
					JCERSAPublicKey keyparam = (JCERSAPublicKey)key.getKey("BC"); 
					//System.out.println("keyparam: "+keyparam.getModulus()+", "+keyparam.getPublicExponent());
					
					long valid_from = key.getCreationTime().getTime();
					long valid_until = valid_from+key.getValidSeconds()*1000;
	
					int a = key.getAlgorithm();
					String algo = "num="+a;
					if (a == PGPPublicKey.RSA_GENERAL || a == PGPPublicKey.RSA_ENCRYPT || a == PGPPublicKey.RSA_SIGN) {
						algo = "RSA";
					}
					byte[] modulus_bytes = keyparam.getModulus().toByteArray();
					byte[] exp_bytes = keyparam.getPublicExponent().toByteArray();
					System.out.println(" keyparam: mod :: "+SecurityHelper.HexDecoder.encode(modulus_bytes)+"\n keyparam: exp :: "+SecurityHelper.HexDecoder.encode(exp_bytes));
					byte[] keyid = SecurityHelper.getSHA1(modulus_bytes);
	
					Element ekp = new Element("keypair");
					if (eIds.getChildren().size()>0) {
						ekp.addContent(eIds);
					}
					ekp.addContent("sha1fingerprint", SecurityHelper.HexDecoder.encode(keyid));
					ekp.addContent("authoritativekeyserver", "pgp_import");
					ekp.addContent("valid_from",SecurityHelper.getFormattedDate(valid_from));
					ekp.addContent("valid_until",SecurityHelper.getFormattedDate(valid_until));
					ekp.addContent("level",(key.isMasterKey()?OSDXKey.level_name.get(OSDXKey.LEVEL_MASTER):OSDXKey.level_name.get(OSDXKey.LEVEL_SUB)));
					ekp.addContent("usage",(key.isEncryptionKey()?OSDXKey.usage_name.get(OSDXKey.USAGE_WHATEVER):OSDXKey.usage_name.get(OSDXKey.USAGE_SIGN)));
					ekp.addContent("algo", algo);
					ekp.addContent("bits", ""+key.getBitStrength());
					ekp.addContent("modulus", SecurityHelper.HexDecoder.encode(modulus_bytes));
					Element epubkey = new Element("pubkey");
					epubkey.addContent("exponent",SecurityHelper.HexDecoder.encode(exp_bytes));
					ekp.addContent(epubkey);
					ekp.addContent("gpgkeyserverid", SecurityHelper.HexDecoder.encode(key.getFingerprint()));
					
					try {
						PGPSecretKey seckey = findSecretKey(key.getKeyID());
						if (seckey!=null) {
							ByteArrayOutputStream bout = new ByteArrayOutputStream();
							seckey.encode(bout);
							byte[] sec_key_bytes = bout.toByteArray();
							Element eexp = new Element("exponent");
							eexp.addContent("pgp",SecurityHelper.HexDecoder.encode(sec_key_bytes));
							Element esk = new Element("privkey");
							esk.addContent(eexp);
							ekp.addContent(esk);
							System.out.println(" priv enc: exp :: "+SecurityHelper.HexDecoder.encode(sec_key_bytes));
						}
					} catch (Exception ex) {
						System.out.println("error adding private key");
					}

					OSDXKey osdxkey = OSDXKey.fromElement(ekp);
					keys.add(osdxkey);
//					if (osdxkey.hasPrivateKey()) {
//						Document.buildDocument(osdxkey.toElement(new DefaultMessageHandler())).output(System.out);
//					}
				
				} catch (Exception e) {
					e.printStackTrace();
				}				
			}
		}
		return keys;
	}
	
	
	//testing
	public static void main(String[] args) {
		PGPKeyStore pgp = new PGPKeyStore();
		try {
			Vector<OSDXKey> keys = pgp.parseOSDXKeys();
			for (OSDXKey key : keys) {
				System.out.println(key.getKeyID());
			}
			OSDXKey key = keys.lastElement();
			try {
				key.unlockPrivateKey(new DefaultMessageHandler());
			} catch (Exception e) {}
			
			if (!key.isPrivateKeyUnlocked()) return;
			
			File testFile = new File("fnppl_contributor_license.pdf");
			Signature sig = Signature.createSignature(testFile, key);
			
			Result res = sig.tryVerificationFile(testFile);
			
			if (res.report!=null) {
				Document.buildDocument(res.report).output(System.out);
			}
			
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
