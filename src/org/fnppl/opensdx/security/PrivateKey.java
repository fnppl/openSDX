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
import java.security.Key;
import java.security.SecureRandom;

import org.bouncycastle.crypto.*;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.crypto.encodings.*;
import org.bouncycastle.crypto.engines.*;
import org.bouncycastle.crypto.modes.*;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.*;


public class PrivateKey {
	static {
		SecurityHelper.ensureBC();
	}
	
	private RSAKeyParameters priv;
	
	public PrivateKey(BigInteger modulus, BigInteger exponent) {
//		this.modulus = modulus;
//		this.exponent = exponent;
		this.priv = new RSAKeyParameters(true, modulus, exponent);
	}
	public PrivateKey(RSAKeyParameters key) {
		this.priv = key;
	}
	
//	public PrivateKey(String fromASC) throws Exception {
//		//TODO really that complicated?
//		InputStream keyIn = PGPUtil.getDecoderStream(new ByteArrayInputStream(fromASC.getBytes()));
//		PGPSecretKeyRingCollection pgpRings = new PGPSecretKeyRingCollection(keyIn);
//		
//		key = ((PGPSecretKeyRingCollection)pgpRings.getKeyRings().next())
//	}
	
//	public PGPPrivateKey getPGPPrivateKey() {
//		return key;
//	}
//	
//	public byte[] getRawEncoded() throws Exception {
//		return key.getKey().getEncoded();
//	}
	
	
	public byte[] sign(byte[] data) throws Exception {
		RSAEngine rsae = new RSAEngine();
		rsae.init(
				false, 
				priv
			);
		byte[] filleddata = new byte[rsae.getInputBlockSize()-1];
		
		System.arraycopy(data, 0, filleddata, 0, data.length);
		PKCS7Padding pad = new PKCS7Padding();
		int k = pad.addPadding(filleddata, data.length);

//		System.out.println("SIGN_PLAINBLOATED:\t"+SecurityHelper.HexDecoder.encode(filleddata, ':', -1));
//		System.out.println("SIGN_PLAINBLOATED.length:\t"+filleddata.length);
		
		return rsae.processBlock(filleddata, 0, filleddata.length);
		
//		return ret;
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
	public byte[] decrypt(byte[] data, PublicKey pubkey) throws Exception {
//		RSABlindingEngine rsae = new RSABlindingEngine();
		RSABlindedEngine rsae = new RSABlindedEngine();
		
//		RSABlindingParameters bp = new RSABlindingParameters(
//				priv, 
//				PublicKey.generateBlindingFactor(pubkey)
//			);
		
		OAEPEncoding oaep = new OAEPEncoding(rsae);
		oaep.init(
				false, //für encrypt: true
//				bp
				priv
			);
		if(data.length > rsae.getInputBlockSize()) {
			throw new RuntimeException("PrivateKey.encrypt::data.length("+data.length+") too long - max is: "+rsae.getInputBlockSize());
		}
		
		return oaep.processBlock(data, 0, data.length);
	}
	public byte[] encrypt(byte[] data, PublicKey pubkey) throws Exception {
		RSABlindedEngine rsae = new RSABlindedEngine();
		
//		RSABlindingEngine rsae = new RSABlindingEngine();
//		
//		RSABlindingParameters bp = new RSABlindingParameters(
//				priv, 
//				PublicKey.generateBlindingFactor(pubkey)
//			);
		
		OAEPEncoding oaep = new OAEPEncoding(rsae);
		oaep.init(
				true, //für encrypt: true
//				bp
				priv
			);
		if(data.length > rsae.getInputBlockSize()) {
			throw new RuntimeException("PrivateKey.encrypt::data.length("+data.length+") too long - max is: "+rsae.getInputBlockSize());
		}
		
		return oaep.processBlock(data, 0, data.length);
	}
}

