package org.fnppl.opensdx.security.test;
/*
 * Copyright (C) 2010-2011 
 * 							fine people e.V. <opensdx@fnppl.org> 
 * 							Bertram Boedeker <bboedeker@gmx.de>
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
 *      
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Identity;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Date;
import java.util.Vector;

import org.bouncycastle.asn1.x509.V1TBSCertificateGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPKeyPair;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureSubpacketVector;
import org.bouncycastle.openpgp.PGPUtil;
import org.fnppl.opensdx.security.AsymmetricKeyPair;
import org.fnppl.opensdx.security.KeyPairGenerator;
import org.fnppl.opensdx.security.KeyRingCollection;
import org.fnppl.opensdx.security.PublicKey;
import org.fnppl.opensdx.security.SignAndVerify;


import com.sun.corba.se.impl.oa.poa.ActiveObjectMap.Key;

public class Test {
	
//	public static void testKeyRingCollection() {
//		File file = new File("test-set/private_key_ring_collection");
//		File filePub = new File("test-set/public_key_ring_collection");
//		String passPhrase = "pow490sdfm298346u323q46erbserg34";
//		String identity = "memememememeeeee";
//		
//		boolean isPrivate = false;
//		filePub.delete();
//		boolean debug_createNew = !filePub.exists();
//		int debug_add = 1;
//		try {
//			KeyRingCollection c = null;
//			
//			if (debug_createNew) {
//				System.out.println("creating new keyringcollection");
//				c = KeyRingCollection.generateNewKeyRingOnFile(filePub, passPhrase.toCharArray(), isPrivate);
//			} else {
//				System.out.println("loading keyring collection");
//				c = KeyRingCollection.fromFile(filePub, passPhrase.toCharArray());
//			}
//			
//			for (int i=0;i<debug_add;i++) {
//				if (isPrivate) {
//					c.addAsymmetricKeyPair(identity, KeyPairGenerator.generateAsymmetricKeyPair());
//				} else {
//					System.out.println("Adding PublicKey");
//					PublicKey key = new PublicKey(KeyPairGenerator.generateAsymmetricKeyPair().getPGPPublicKey());
//					c.addNewPublicKey(key);
//				}
//			}
//			if (debug_add>0) c.save();
//			
//			c.printAllKeys(true);
//			
//			
//			
//			if (isPrivate) {
//				//test sign file
//				File testFile = new File("test-set/testdata.txt");
//				
//				AsymmetricKeyPair kp = c.getSomeRandomKeyPair();
//	
//				
//				System.out.println("\nrandom key id: "+kp.getPGPPublicKey().getKeyID());
//				SignAndVerify.createSignatureFile(testFile, kp);
//				
//				//test verify
//				boolean v = SignAndVerify.verifySignature(testFile, c);
//				if (v) {
//					System.out.println("File verified!");
//				} else {
//					System.out.println("File NOT verified!");
//				}
//				
//				
//				//test sign stream
//				FileInputStream in = new FileInputStream(testFile);
//				PGPSignature sig = SignAndVerify.createSignature(in, kp);
//			}
//			
//			
//			
//			
//			
//			//test TSA Client Server
//			//TSAClient tsa = new TSAClient();
//			//tsa.connect("localhost", 8889);
//			//PGPSignature tsaSig =tsa.requestTSASigniture(sig);
//			
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
//	/**
//	 * for Testing
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		testKeyRingCollection();
//		/*try {
//			Security.addProvider(new BouncyCastleProvider());
//			//KeyPairGenerator.generateRSAKeyPair("bla","bla", true);
//			
//			PGPPublicKey pk = KeyPairGenerator.generateAsymmetricKeyPair().getPGPPublicKey();
//			StringBuffer
//			pk.encode(arg0)
//			
//			InputStream inSig = PGPUtil.getDecoderStream(new FileInputStream("keypairs/public.asc"));
//			PGPPublicKeyRing pkr = new PGPPublicKeyRing(inSig);
//			
//			PGPPublicKeyRing.insertPublicKey(pkr, pk);
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}*/
//		
//		
//	}
	
}

