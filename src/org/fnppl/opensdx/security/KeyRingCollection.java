package org.fnppl.opensdx.security;

/*
 * Copyright (C) 2010-2011 
 * 							fine people e.V. <opensdx@fnppl.org> 
 * 							Henning Thieß <ht@fnppl.org>
 * 
 * 							http://fnppl.org
 * 
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

import java.io.*;
import java.util.*;
import java.security.*;

import org.bouncycastle.bcpg.*;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.fnppl.opensdx.common.Util;

//check this example: http://www.docjar.com/html/api/org/bouncycastle/openpgp/examples/DetachedSignatureProcessor.java.html

/**
 * @author Bertram Boedeker <bbodeker@gmx.de>
 * @author Henning Thieß <ht@fnppl.org>
 */

public class KeyRingCollection {
	static {
		SecurityHelper.ensureBC();
	}
	
	private File f = null;
	private PGPSecretKeyRingCollection  pgpSecretRingCollection = null;
	private PGPPublicKeyRingCollection  pgpPublicRingCollection = null;
	
	private char[] pass_mantra = null;
	private boolean this_is_private_keyring_collection = false;
	
	private KeyRingCollection() {}
	
	
	
	public static KeyRingCollection generateNewKeyRingOnFile(File f, char[] pass_mantra, boolean isprivate) throws Exception {
		KeyRingCollection k = new KeyRingCollection();
		k.pass_mantra = pass_mantra;
		k.this_is_private_keyring_collection = isprivate;
		k.f = f;
		if (f.exists()) {
			throw new Exception("FILE ALREADY EXISTS!");
		} else {
			k.initEmptyKeyRingCollection();
		}
		return k;
	}
	
	public void save() throws Exception {
		//HT 17.02.2011 - please check::compatibilty between BC-file-format AND GPG-file-format
		//when private-key-ring:: ENCRYPT with pass_mantra !!!
		
		ArmoredOutputStream out = new ArmoredOutputStream(new FileOutputStream(f));
    	BCPGOutputStream bOut = new BCPGOutputStream(out);
    	
		if (this_is_private_keyring_collection) {
			pgpSecretRingCollection.encode(bOut);
		} else {
			pgpPublicRingCollection.encode(bOut);
		}
		bOut.close();
		out.close();
	}
	
	public static KeyRingCollection fromFile(File f, char[] pass_mantra) throws Exception {
		KeyRingCollection k = new KeyRingCollection();
		k.pass_mantra = pass_mantra;
		k.f = f;
		if (f.exists()) {
			//System.out.println("reading from file: "+f.getName());
			k.readFromFile();
		} else {
			throw new Exception("FILE DOES NOT EXIST!");
		}
		return k;
	}
	
// --- public methods ----
	
	
	//first there is one keyring for every Asymmetric KeyPair
	public void addAsymmetricKeyPair(String identity, AsymmetricKeyPair keypair) throws Exception {
		addNewSecretKeyRing(identity, pass_mantra, keypair.getPGPKeyPair(), null);
	}
	
	public AsymmetricKeyPair getSomeRandomKeyPair() throws Exception {
		PGPSecretKey skey = getSomeRandomSecretKey();
		PGPKeyPair kp = new PGPKeyPair(skey.getPublicKey(), skey.extractPrivateKey(pass_mantra, "BC"));
		return new AsymmetricKeyPair(kp);
	}
	
	/**
	 * 
	 * @param no number of key CAUTION: START COUNTING AT 1!
	 * @return
	 * @throws Exception
	 */
	public AsymmetricKeyPair getAsymmetricKeyPair(int no) throws Exception {
		if (this_is_private_keyring_collection) {
			Iterator rIt = pgpSecretRingCollection.getKeyRings();
			int nr = 1;
			while (rIt.hasNext()) {
				PGPSecretKeyRing kRing = (PGPSecretKeyRing)rIt.next();
				Iterator kIt = kRing.getSecretKeys();
				while (kIt.hasNext()) {
					PGPSecretKey k = (PGPSecretKey)kIt.next();
					if (nr == no) {
						PGPKeyPair kp = new PGPKeyPair(k.getPublicKey(), k.extractPrivateKey(pass_mantra, "BC"));
						return new AsymmetricKeyPair(kp);
					}
					nr++;
				}
			}
		}
		throw new Exception("NOT POSSIBLE IN PUBLIC COLLECTION");
	}
	
	public void addNewPublicKey(PublicKey key) throws Exception {
		InputStream keyIn = PGPUtil.getDecoderStream(new ByteArrayInputStream(key.getPGPPublicKey().getEncoded()));
		PGPPublicKeyRingCollection newkrc = new PGPPublicKeyRingCollection(keyIn);
		Iterator it = newkrc.getKeyRings();
        while(it.hasNext()) {
        	pgpPublicRingCollection=PGPPublicKeyRingCollection.addPublicKeyRing(pgpPublicRingCollection, (PGPPublicKeyRing) it.next());
        }
	}
	
	public PublicKey getPublicKey(long id) throws Exception {
		if (this_is_private_keyring_collection) {
			PGPPublicKey pk = pgpSecretRingCollection.getSecretKey(id).getPublicKey();
			if (pk!=null) return new PublicKey(pk);
		} else {
			PGPPublicKey pk = pgpPublicRingCollection.getPublicKey(id);
			if (pk!=null) return new PublicKey(pk);
		}
		throw new Exception("NO MATCHING KEY FOUND!");
	}
	
	public void printAllKeys(boolean more) {
		if (this_is_private_keyring_collection) {
			System.out.println("--- Private KeyRingCollection keys:");
			Iterator rIt = pgpSecretRingCollection.getKeyRings();
			int c=0;
			int nr = 1;
			while (rIt.hasNext()) {
				c++;
				PGPSecretKeyRing kRing = (PGPSecretKeyRing)rIt.next();
				//System.out.println("KeyRing "+c);
				Iterator kIt = kRing.getSecretKeys();
				while (kIt.hasNext()) {
					PGPSecretKey k = (PGPSecretKey)kIt.next();
					System.out.println(" - ("+nr+") keyid: 0x"+Long.toHexString(k.getKeyID())+(k.isMasterKey()?" masterkey":"")+(k.isSigningKey()?" signing key":""));
					//System.out.println("public key id: 0x"+Long.toHexString(k.getPublicKey().getKeyID()));
					if (more) printPublicKey(k.getPublicKey());
					nr++;
				}
			}
			System.out.println("--- end ---\n");
		} else {
			System.out.println("--- Public KeyRingCollection keys:");
			Iterator rIt = pgpPublicRingCollection.getKeyRings();
			int c=0;
			int nr = 1;
			while (rIt.hasNext()) {
				c++;
				PGPPublicKeyRing kRing = (PGPPublicKeyRing)rIt.next();
				//System.out.println("KeyRing "+c);
				Iterator kIt = kRing.getPublicKeys();
				while (kIt.hasNext()) {
					PGPPublicKey k = (PGPPublicKey)kIt.next();
					System.out.println(" - ("+nr+") keyid: 0x"+Long.toHexString(k.getKeyID())+(k.isMasterKey()?" masterkey":""));
					if (more) printPublicKey(k);
					nr++;
				}
			}
			System.out.println("--- end ---\n");
		}
	}
	
	public static void printPublicKey(PGPPublicKey key) {
		try {
			//ArmoredOutputStream out = new ArmoredOutputStream(new FileOutputStream(f));
			ArmoredOutputStream out = new ArmoredOutputStream(System.out);
	    	BCPGOutputStream bOut = new BCPGOutputStream(out);
			key.encode(bOut);
			bOut.close();
			out.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
// -- end public methods
	
	private void readFromFile() throws Exception {
		
		InputStream keyIn = new FileInputStream(f);
		
		ArmoredInputStream aIn=new ArmoredInputStream(keyIn);
        InputStream newin = PGPUtil.getDecoderStream(aIn);
        PGPObjectFactory pgpF = new PGPObjectFactory(newin);
        Object obj;
        while((obj=pgpF.nextObject())!=null) {
        	//System.out.println("reading object: "+obj.getClass().toString());
            if(obj instanceof PGPSecretKeyRingCollection) {
            	this_is_private_keyring_collection = true;
            	pgpSecretRingCollection=((PGPSecretKeyRingCollection)obj);
            } else if(obj instanceof PGPSecretKeyRing) {
            	this_is_private_keyring_collection = true;
            	PGPSecretKeyRing kr = (PGPSecretKeyRing)obj;
            	if (pgpSecretRingCollection==null) {
            		pgpSecretRingCollection = new PGPSecretKeyRingCollection(new Vector());
            	}
            	Iterator it = kr.getSecretKeys();
            	while(it.hasNext()) {
            		PGPSecretKey k = (PGPSecretKey)it.next();
            		addNewSecretKey(k);
            	}
            }
            else if(obj instanceof PGPPublicKeyRingCollection) {
            	pgpPublicRingCollection=((PGPPublicKeyRingCollection)obj);
            	this_is_private_keyring_collection = false;
            } else if(obj instanceof PGPPublicKeyRing) {
            	PGPPublicKeyRing kr = (PGPPublicKeyRing)obj;
            	this_is_private_keyring_collection = false;
            	if (pgpPublicRingCollection==null) {
            		pgpPublicRingCollection = new PGPPublicKeyRingCollection(new Vector());
            	}
            	Iterator it = kr.getPublicKeys();
            	while(it.hasNext()) {
            		PGPPublicKey k = (PGPPublicKey)it.next();
            		addNewPublicKey(new PublicKey(k));
            	}
            }
        }
	}
	
	private void initEmptyKeyRingCollection() throws Exception {
		
		if (this_is_private_keyring_collection) {
			pgpSecretRingCollection = new PGPSecretKeyRingCollection(new Vector());
		} else {
			pgpPublicRingCollection = new PGPPublicKeyRingCollection(new Vector());
		}
	}
	
	private void addNewSecretKeyRing(PGPSecretKeyRing ring) throws Exception {
		
		if (this_is_private_keyring_collection) {
			pgpSecretRingCollection = PGPSecretKeyRingCollection.addSecretKeyRing(pgpSecretRingCollection, ring);
		} else {
			throw new Exception("CANNOT ADD SECRET KEY TO PUBLIC COLLECTION!"); 
		}
	}
	
	private void addNewSecretKey(PGPSecretKey key) throws Exception {
		InputStream keyIn = PGPUtil.getDecoderStream(new ByteArrayInputStream(key.getEncoded()));
		PGPSecretKeyRingCollection newkrc = new PGPSecretKeyRingCollection(keyIn);
		Iterator it = newkrc.getKeyRings();
        while(it.hasNext()) {
        	pgpSecretRingCollection=PGPSecretKeyRingCollection.addSecretKeyRing(pgpSecretRingCollection, (PGPSecretKeyRing) it.next());
        }
	}
	
	
	
	/**
	 * generation of a new keyring for ever new keypair -> master key
	 * @param identity
	 * @param pass_mantra
	 * @param masterKey
	 * @param subKeys
	 * @throws Exception
	 */
	private void addNewSecretKeyRing(String identity, char[] pass_mantra, PGPKeyPair masterKey, Collection<PGPKeyPair> subKeys) throws Exception {
		if (!this_is_private_keyring_collection) {
			throw new Exception("CANNOT ADD SECRET KEY TO PUBLIC COLLECTION!"); 
		}
		
		int certificationLevel = PGPSignature.POSITIVE_CERTIFICATION; //DEFAULT_CERTIFICATION; 
		
		int encAlgorithm = PGPEncryptedData.AES_128; //TODO AES_256 not allowed
		
		boolean useSHA1 = true;
		PGPSignatureSubpacketVector hashedPcks = null;
		PGPSignatureSubpacketVector unhashedPcks = null;
		SecureRandom rand = new SecureRandom();
		String provider = "BC";
		PGPKeyRingGenerator g = new PGPKeyRingGenerator(certificationLevel, masterKey, identity,
				encAlgorithm, pass_mantra, useSHA1, hashedPcks, unhashedPcks, rand, provider);
		
		if (subKeys!=null) {
			for (PGPKeyPair sub : subKeys) {
				g.addSubKey(sub);
			}
		}
		PGPSecretKeyRing secKeyRing = g.generateSecretKeyRing();
		addNewSecretKeyRing(secKeyRing);
		
		//PGPPublicKeyRing pubKeyRing = g.generatePublicKeyRing();	
	}
	
		
	/**
	 * runs through the collection of all secret keys rings and all secret keys
	 * and returns a random key for signing
	 *  
	 * @return PGPSecretKey
	 * @throws Exception
	 */
	private PGPSecretKey getSomeRandomSecretKey() throws Exception {
		//save all matching in possible
		Vector<PGPSecretKey> possible = new Vector<PGPSecretKey>();
		Iterator rIt = pgpSecretRingCollection.getKeyRings();
		while (rIt.hasNext()) {
			PGPSecretKeyRing kRing = (PGPSecretKeyRing)rIt.next();    
			Iterator kIt = kRing.getSecretKeys();
			while (kIt.hasNext()) {
				PGPSecretKey k = (PGPSecretKey)kIt.next();
				if (k.isSigningKey()) {
					possible.add(k);
				}
			}
		}
		//choose one and return
		if (possible.size()==0) throw new Exception("NO MATCHING KEY IN COLLECTION!");
		if (possible.size()==1) return possible.get(0);
		else {
			int rand = (int)((Math.random()*possible.size()));
			return possible.get(rand);
		}
	}
	
}


