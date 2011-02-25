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
	private int type = 0;
	
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
		int algo = OSDXKeyObject.ALGO_RSA;
		
//		RSAPublicKeySpec sp = new RSAPublicKeySpec(new BigInteger(modulus), new BigInteger(pub_exponent));
//		RSAPKeySpec sp = new RSAPublicKeySpec(new BigInteger(modulus), new BigInteger(pub_exponent));
		
		org.bouncycastle.crypto.params.RSAKeyParameters pub = new RSAKeyParameters(false, new BigInteger(modulus), new BigInteger(pub_exponent));	
		org.bouncycastle.crypto.params.RSAKeyParameters priv = new RSAKeyParameters(true, new BigInteger(modulus), new BigInteger(priv_exponent));
		
//		RSAPrivateCrtKey kk = new RSAPrivateCrtKeyParameters(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7)
	}
	public AsymmetricKeyPair(AsymmetricCipherKeyPair keyPair) {
		int algo = OSDXKeyObject.ALGO_RSA;
		
		CipherParameters pub = keyPair.getPublic();
		CipherParameters priv = keyPair.getPrivate();
		
		RSAKeyParameters rpub = (RSAKeyParameters)pub;
		RSAPrivateCrtKeyParameters rpriv = (RSAPrivateCrtKeyParameters)priv;
		
		this.pubkey = new PublicKey(rpub.getModulus(), rpub.getExponent());
		this.privkey = new PrivateKey(rpriv.getModulus(), rpriv.getExponent());
		
		this.bitcount = rpub.getModulus().bitLength();
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
		return privkey.decrypt(me, pubkey);
	}
	public byte[] sign(byte[] me) throws Exception {
		return privkey.sign(me);
	}
	public boolean verify(byte[] signature, byte[] plain) throws Exception {
		byte[] ka = pubkey.encryptPKCSed7(signature);
		
//		System.out.println("SIGNATURE_DEC:\t"+SecurityHelper.HexDecoder.encode(ka, ':', -1));
		int pad = ka[ka.length-1];
		byte[] real = new byte[ka.length-1 - pad];
		for(int i=0;i<real.length;i++) {
			real[i] = ka[i+1];
		}
		
		return Arrays.equals(
				plain, 
				real
			);
	}
	
	public static void main(String[] args) throws Exception {
		AsymmetricKeyPair ak = generateAsymmetricKeyPair();
		System.out.println("BitCount: "+ak.getBitCount());
		
		String tc = new String("I am to encode...");
		
		byte[] data = ak.encryptWithPublicKey(tc.getBytes());
		System.out.println("ENCODED: "+SecurityHelper.HexDecoder.encode(data, '\0', -1));
		byte[] dec = ak.decryptWithPrivateKey(data);
		System.out.println("DECODED: "+SecurityHelper.HexDecoder.encode(dec, '\0', -1)+" -> "+(new String(dec)));
		
		byte[] sha256bytes = SecurityHelper.getSHA256(tc.getBytes());
		byte[] md5 = SecurityHelper.getMD5(tc.getBytes());
		
		byte[] mega = new byte[sha256bytes.length+md5.length];
		System.arraycopy(sha256bytes, 0, mega, 0, sha256bytes.length);
		System.arraycopy(md5, 0, mega, sha256bytes.length, md5.length);
		
		String s = SecurityHelper.HexDecoder.encode(mega, ':', -1);
		System.out.println("\n\nMEGA_STRING:\t\t"+s);
		
		byte[] signature = ak.sign(mega);
		System.out.println("SIGNATURE(mega).length: "+signature.length);
		System.out.println("SIGNATURE(mega): "+SecurityHelper.HexDecoder.encode(signature, ':', -1));
		
//		byte[] testsig = ak.pubkey.encryptPKCS7(signature);
//		System.out.println("TEST_SIGNATURE.length: "+testsig.length);
//		System.out.println("TEST_SIGNATURE: "+SecurityHelper.HexDecoder.encode(testsig, ':', -1));
		
		System.out.println("SIGNATURE_VERIFIED: "+ak.verify(signature, mega));
	}
}

