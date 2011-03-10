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

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;

import java.security.Key;
import java.util.Arrays;

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
import org.fnppl.opensdx.xml.Element;


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
	
//	public boolean verify(byte[] signature, byte[] plain) throws Exception {
//		byte[] ka = encryptPKCSed7(signature);
//		
//		//System.out.println("PubKey_verify: SIGNATURE_DEC (length: "+ka.length+") \t:"+SecurityHelper.HexDecoder.encode(ka, ':', 80));
//		
//		byte[] real = new byte[ka.length-1];
//		System.arraycopy(ka, 1, real, 0, real.length);
//		
//		byte[] filleddata = new byte[real.length];
//		for(int i=0; i<filleddata.length; i++) {
//			filleddata[i] = plain[i % plain.length];//HT 2011-03-03 better some initvectorpadddup!!!
//		}
//		
//		//System.out.println("PubKey_verify: PLAIN(COMPARE1; length: "+filleddata.length+")\n:"+SecurityHelper.HexDecoder.encode(filleddata, ':', 80));
//		//System.out.println("PubKey_verify: SIGNATURE_DEC_cut (COMPARE2; length: "+real.length+")\n:"+SecurityHelper.HexDecoder.encode(real, ':', 80));
//		return Arrays.equals(
//				filleddata, 
//				real
//			);
//	}
	
	public Element getSimplePubKeyElement() throws Exception {
		Element ret = new Element("pubkey");
		ret.addContent("algo", "RSA");
		ret.addContent("bits", ""+getBitCount());
		ret.addContent("modulus", getModulusAsHex());
		ret.addContent("exponent", getPublicExponentAsHex());
		return ret;
	}
	
	
	public boolean verify(
			byte[] signature, 
			byte[] md5, 
			byte[] sha1, 
			byte[] sha256,
			long timestamp
			) throws Exception {
		
		byte[] ka = encryptPKCSed7(signature);
		
		//System.out.println("PubKey_verify: SIGNATURE_DEC (length: "+ka.length+") \t:"+SecurityHelper.HexDecoder.encode(ka, ':', 80));
		
		byte[] real = new byte[ka.length-1];
		System.arraycopy(ka, 1, real, 0, real.length);//0x00 header killr
		
		byte[] md5Check = new byte[16];
		byte[] sha1Check = new byte[20];
		byte[] sha256Check = new byte[32];
		byte[] tscheck = new byte[6];
		
		
		System.arraycopy(real, 0, md5Check, 0, md5Check.length);
		System.arraycopy(real, 16, sha1Check, 0, sha1Check.length);
		System.arraycopy(real, 16+20, sha256Check, 0, sha256Check.length);
		System.arraycopy(real, 16+20+32, tscheck, 0, tscheck.length);
		long ts = (new BigInteger(tscheck)).longValue();
		
		boolean succeededone = false;
		if(md5 != null) {
			if(!Arrays.equals(md5, md5Check)) {
				return false;
			}
			succeededone = true;
		}
		if(sha1 != null) {
			if(!Arrays.equals(sha1, sha1Check)) {
				return false;
			}
			succeededone = true;
		}
		if(sha256 != null) {
			if(!Arrays.equals(sha256, sha256Check)) {
				return false;
			}
			succeededone = true;
		}
		if(timestamp != -1) {
			if(ts != timestamp) {
				return false;
			}
//			succeededone = true; //naht. this is no hash
		}
		
		return succeededone; //false if all hash-arrays were NULL ; also false if the timestamp mathed...
		
		//System.out.println("PubKey_verify: PLAIN(COMPARE1; length: "+filleddata.length+")\n:"+SecurityHelper.HexDecoder.encode(filleddata, ':', 80));
		//System.out.println("PubKey_verify: SIGNATURE_DEC_cut (COMPARE2; length: "+real.length+")\n:"+SecurityHelper.HexDecoder.encode(real, ':', 80));
	}
	
	public String getKeyID() {
		String keyid = SecurityHelper.HexDecoder.encode(SecurityHelper.getSHA1(pub.getModulus().toByteArray()), '\0', -1);
		return keyid;
	}
}
