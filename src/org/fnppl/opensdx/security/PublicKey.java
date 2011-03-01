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
import java.math.BigInteger;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;

import java.security.Key;

import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.crypto.encodings.OAEPEncoding;
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.RSABlindedEngine;
import org.bouncycastle.crypto.engines.RSABlindingEngine;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.generators.RSABlindingFactorGenerator;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.RSAKeyParameters;


public class PublicKey {
	static {
		SecurityHelper.ensureBC();
	}
	
//	private PGPPublicKey key;
	private RSAKeyParameters pub;
//	private BigInteger modulus = null;
//	private BigInteger exponent = null;
	
	private PublicKey() {
		
	}

	public BigInteger getModulus() {
		return pub.getModulus();
	}
	public BigInteger getExponent() {
		return pub.getExponent();
	}
	
	public int getBitCount() {
		return pub.getModulus().bitLength();
	}
	public String getModulusAsHex() {
		return SecurityHelper.HexDecoder.encode(pub.getModulus().toByteArray(), '\0', -1);
	}
	public byte[] getModulusBytes() {
		return pub.getModulus().toByteArray();
	}
	public String getPublicExponentAsHex() {
		return SecurityHelper.HexDecoder.encode(pub.getExponent().toByteArray(), '\0', -1);
	}
	
//	org.bouncycastle.crypto.params.RSAKeyParameters pub = new RSAKeyParameters(false, new BigInteger(modulus), new BigInteger(pub_exponent));
	public PublicKey(BigInteger modulus, BigInteger exponent) {
//		this.modulus = modulus;
//		this.exponent = exponent;
		this.pub = new RSAKeyParameters(false, modulus, exponent);
	}
	public PublicKey(RSAKeyParameters key) {
		this.pub = key;
	}
//	public static BigInteger generateBlindingFactor(PublicKey pubKey) {
//		return generateBlindingFactor(pubKey.pub);
//	}
//	public static BigInteger generateBlindingFactor(RSAKeyParameters pubKey) {
////		http://forums.oracle.com/forums/thread.jspa?threadID=1525914
//			
//		RSABlindingFactorGenerator gen = new RSABlindingFactorGenerator();
// 
//        gen.init(pubKey);
// 
//        return gen.generateBlindingFactor();
//    }
	public byte[] encrypt(byte[] data) throws Exception {
//		RSABlindingEngine rsae = new RSABlindingEngine();
		RSABlindedEngine rsab = new RSABlindedEngine();
		
//		RSABlindingParameters bp = new RSABlindingParameters(
//				pub, 
//				generateBlindingFactor(pub)
//			);
		
//		OAEPEncoding oaep = new OAEPEncoding(rsae);
		OAEPEncoding oaep = new OAEPEncoding(rsab);
		oaep.init(
				true, //für encrypt: true
				pub
//				bp
			);
		
		if(data.length > rsab.getInputBlockSize()) {
			throw new RuntimeException("PublicKey.encrypt::data.length("+data.length+") too long - max is: "+oaep.getInputBlockSize());
		}
		
		return oaep.processBlock(data, 0, data.length);
	}
	public byte[] decrypt(byte[] data) throws Exception {
//		RSABlindingEngine rsae = new RSABlindingEngine();
		
		RSABlindedEngine rsae = new RSABlindedEngine();
		
//		RSABlindingParameters bp = new RSABlindingParameters(
//				pub, 
//				generateBlindingFactor(pub)
//			);
		
		OAEPEncoding oaep = new OAEPEncoding(rsae);
		oaep.init(
				false, //für encrypt: true
				pub
//				bp
			);
		
		if(data.length > rsae.getInputBlockSize()) {
			throw new RuntimeException("PublicKey.decrypt::data.length("+data.length+") too long - max is: "+oaep.getInputBlockSize());
		}
		
		return oaep.processBlock(data, 0, data.length);
	}
	public byte[] encryptPKCSed7(byte[] data) throws Exception {
		//input here is a full rsa-block...
		RSAEngine rsae = new RSAEngine();
//		org.bouncycastle.crypto.encodings.PKCS1Encoding enc = new PKCS1Encoding(rsae);
//		enc.init(true, pub);
//	    return enc.processBlock(data, 0, data.length);

		rsae.init(true, pub);
//		System.out.println("pub.encryptpkcs7.outputLength: "+rsae.getOutputBlockSize());
//		System.out.println("pub.encryptpkcs7.inputBlockSize: "+rsae.getInputBlockSize());
		
		
//		PKCS7Padding pad = new PKCS7Padding();
//		byte[] kk = new byte[rsae.getInputBlockSize()];
//		System.arraycopy(data, 0, kk, 0, data.length);
//		pad.addPadding(kk, data.length);
		
//		return rsae.processBlock(kk, 0, kk.length);
		
		return rsae.processBlock(data, 0, data.length);
		
//		OAEPEncoding oaep = new OAEPEncoding(rsae);
//		oaep.init(
//				false, //für encrypt: true
////				bp
//				priv
//			);
//		if(data.length > rsae.getInputBlockSize()) {
//			throw new RuntimeException("PrivateKey.encrypt::data.length("+data.length+") too long - max is: "+rsae.getInputBlockSize());
//		}
//		
//		return oaep.processBlock(data, 0, data.length);
	}
	
	public String getKeyID() {
		String keyid = SecurityHelper.HexDecoder.encode(SecurityHelper.getSHA1(pub.getModulus().toByteArray()), '\0', -1);
		return keyid;
	}
}
