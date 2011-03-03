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
import java.math.*;
import java.security.*;
import java.util.*;

import org.bouncycastle.crypto.*;
import org.bouncycastle.crypto.engines.*;
import org.bouncycastle.crypto.generators.*;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.params.*;

import org.bouncycastle.openpgp.*;

/*
 * 
 */

public class AsymmetricKeyPair {
	static {
		SecurityHelper.ensureBC();
	}

	private PublicKey pubkey = null;
	private PrivateKey privkey = null;
		
	private int bitcount = 0;
	private int type = 0;  //TODO what for? algo? padding?
	
	private AsymmetricKeyPair() {		
	}
	
	String keyid = null;
	public String getKeyID() {		
		if(keyid == null) {
			keyid = SecurityHelper.HexDecoder.encode(SecurityHelper.getSHA1(pubkey.getModulus().toByteArray()), '\0', -1);
		}
		
		return keyid;
	}

	String keyhex = null;
	public String getKeyIDHex() {
		if(keyhex == null) {
			keyhex = SecurityHelper.HexDecoder.encode(SecurityHelper.getSHA1(pubkey.getModulus().toByteArray()), ':', -1);
		}
		return keyhex;		
	}

	public AsymmetricKeyPair(byte[] modulus, byte[] pub_exponent, byte[] priv_exponent) {
		type = OSDXKeyObject.ALGO_RSA;
		//public key
		org.bouncycastle.crypto.params.RSAKeyParameters rpub = new RSAKeyParameters(false, new BigInteger(modulus), new BigInteger(pub_exponent));	
		this.pubkey = new PublicKey(rpub.getModulus(), rpub.getExponent());
		this.bitcount = rpub.getModulus().bitLength();
		
		//private key
		if (priv_exponent!=null) { //only if priv_exponent available
			org.bouncycastle.crypto.params.RSAKeyParameters rpriv = new RSAKeyParameters(true, new BigInteger(modulus), new BigInteger(priv_exponent));
			this.privkey = new PrivateKey(rpriv.getModulus(), rpriv.getExponent());
		} else {
			privkey = null;
		}
	}
	
	public AsymmetricKeyPair(AsymmetricCipherKeyPair keyPair) {
		type = OSDXKeyObject.ALGO_RSA;
		
		CipherParameters pub = keyPair.getPublic();
		CipherParameters priv = keyPair.getPrivate();
		
		RSAKeyParameters rpub = (RSAKeyParameters)pub;
		RSAPrivateCrtKeyParameters rpriv = (RSAPrivateCrtKeyParameters)priv;
		
		this.pubkey = new PublicKey(rpub.getModulus(), rpub.getExponent());
		this.privkey = new PrivateKey(rpriv.getModulus(), rpriv.getExponent());
		
		this.bitcount = rpub.getModulus().bitLength();
	}
	
	public boolean hasPrivateKey() {
		if (privkey==null) return false;
		return true;
	}
	
//	public AsymmetricKeyPair(PGPKeyPair kp) {
//		keypair = kp;
//		int algo = keypair.getPublicKey().getAlgorithm();
//		if (algo == PGPPublicKey.RSA_GENERAL || algo == PGPPublicKey.RSA_SIGN || algo == PGPPublicKey.RSA_ENCRYPT) {
//			type = TYPE_RSA;
//		} else if (algo == PGPPublicKey.DSA) {
//			type = TYPE_DSA;
//		} else {
//			type = TYPE_UNDEFINED;
//		}
//		
//	}
//	
	
//	public PGPPublicKey getPGPPublicKey() {
//		return keypair.getPublicKey();
//	}
//	
//	public PGPPrivateKey getPGPPrivateKey() {
//		return keypair.getPrivateKey();
//	}
//	
//	public PGPKeyPair getPGPKeyPair() {
//		return keypair;	
//	}
//	
	public int getType() {
		return type;
	}
	public int getBitCount() {
		return bitcount;
	}
	public String getModulusAsHex() {
		return SecurityHelper.HexDecoder.encode(pubkey.getModulus().toByteArray(), '\0', -1);
	}
	public byte[] getModulus() {
		return pubkey.getModulus().toByteArray();
	}
	public byte[] getPublicExponent() {
		return pubkey.getExponent().toByteArray();
	}
	public String getPublicExponentAsHex() {
		return SecurityHelper.HexDecoder.encode(pubkey.getExponent().toByteArray(), '\0', -1);
	}
	
	public boolean isRSA() {
		if (type == OSDXKeyObject.ALGO_RSA) {
			return true;
		}
		return false;
	}

//	public byte[] encryptWithPubKey(byte[] in) throws Exception {
//		//HT 20.02.2011 - to check wether this is encrypting-allowed RSA
//		
//		CipherParameters cp = new KeyParameter(ukey.getRawEncoded());
////		Key kkey = key.getKey("BC");
//				
////		AsymmetricKeyParameter key = this.key.get;
//		RSAEngine e = new RSAEngine();
//		e.init(true, cp);
//		int blockSize = e.getInputBlockSize();
//		ByteArrayOutputStream bout = new ByteArrayOutputStream();
//		
//		for (int chunkPosition = 0; chunkPosition < in.length; chunkPosition += blockSize) {
//			int chunkSize = Math.min(blockSize, in.length - (chunkPosition * blockSize));
//			bout.write(
//					e.processBlock(in, chunkPosition, chunkSize)
//				);
//		 }
//		
//		return bout.toByteArray();
//	}
	
	public static AsymmetricKeyPair generateAsymmetricKeyPair() throws Exception {
		SecurityHelper.ensureBC();
		
//		SecureRandom sc = new SecureRandom();
//		KeyGenerationParameters kp = new KeyGenerationParameters(sc, 256);
//		
//		RSAKeyPairGenerator rsak = new RSAKeyPairGenerator();
//		rsak.init(kp);
		
		RSAKeyGenerationParameters kk = new RSAKeyGenerationParameters(
		    	BigInteger.valueOf(65537),//publicExponent
		        SecureRandom.getInstance("SHA1PRNG"),//prng
		        3072,//strength
		        80//certainty
		    );
		
		RSAKeyPairGenerator generator = new RSAKeyPairGenerator();
		generator.init(kk);

		long j = System.currentTimeMillis();
		System.out.println("Starting RSA keypairgeneration...");
		AsymmetricCipherKeyPair keyPair = generator.generateKeyPair();
		j = System.currentTimeMillis() - j;
		System.out.println("ENDED RSA keypairgeneration... "+j+"ms -> "+keyPair.getClass().getName());
		
		CipherParameters pub = keyPair.getPublic();
		CipherParameters priv = keyPair.getPrivate();

		RSAKeyParameters rpub = (RSAKeyParameters)pub;
		RSAPrivateCrtKeyParameters rpriv = (RSAPrivateCrtKeyParameters)priv;
		
//		System.out.println("***PRIV***\nEXP: "+rpriv.getExponent()+"\nMOD: "+rpriv.getModulus());
//		System.out.println("\n\n***PUB***\nEXP: "+rpub.getExponent()+"\nMOD: "+rpub.getModulus());
		
//		System.out.println("BITCOUNT_PRIV_EXP: "+rpriv.getExponent().bitLength());
//		System.out.println("BITCOUNT_PRIV_MOD: "+rpriv.getModulus().bitLength());
//		System.out.println("BITCOUNT_PUB_EXP: "+rpub.getExponent().bitLength());
//		System.out.println("BITCOUNT_PUB_MOD: "+rpub.getModulus().bitLength());

		return new AsymmetricKeyPair(keyPair );
	}
	
	public byte[] encryptWithPublicKey(byte[] me) throws Exception {
		return pubkey.encrypt(me);
	}
	public byte[] decryptWithPrivateKey(byte[] me) throws Exception {
		return privkey.decrypt(me);
	}
	
	public byte[] sign(byte[] plain) throws Exception {
		return privkey.sign(plain);
	}
	
	public boolean verify(byte[] signature, byte[] plain) throws Exception {
		return pubkey.verify(signature, plain);
	}
	
	public static void main(String[] args) throws Exception {
		AsymmetricKeyPair ak = generateAsymmetricKeyPair();
		System.out.println("BitCount: "+ak.getBitCount());
		
		String tc = new String("I am to encode...");
		
		byte[] data = ak.encryptWithPublicKey(tc.getBytes());
		System.out.println("ENCODED: "+SecurityHelper.HexDecoder.encode(data, ':', 80));
		byte[] dec = ak.decryptWithPrivateKey(data);
		System.out.println("DECODED: "+SecurityHelper.HexDecoder.encode(dec, ':', 80)+" -> "+(new String(dec)));
		
		System.out.println("\n\n\n");
		
		byte[] sha1andmd5 = SecurityHelper.getSHA1MD5(tc.getBytes());
		
		String s = SecurityHelper.HexDecoder.encode(sha1andmd5, ':', 80);
		System.out.println("SHA1_AND_MD5 length:\t"+sha1andmd5.length);//sollten 36 bytes sein
		System.out.println("SHA1_AND_MD5:\t\t"+s);
		
		System.out.println("\nSignature creation stage...");
		
		byte[] signature = ak.sign(sha1andmd5);
		System.out.println("SIGNATURE(sha1andmd5).length: "+signature.length);
		System.out.println("SIGNATURE(sha1andmd5): "+SecurityHelper.HexDecoder.encode(signature, ':', 80));
		
		System.out.println("\nVerification-Stage...");
		System.out.println("SIGNATURE_VERIFIED: "+ak.verify(signature, sha1andmd5));
	}
	
	public byte[] getEncrytedPrivateKey(SymmetricKey sk) throws Exception {
		return privkey.getEncrytedPrivateKey(sk);
	}
}

