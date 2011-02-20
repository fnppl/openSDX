package org.fnppl.opensdx.security;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;

import org.bouncycastle.openpgp.*;

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

/*
 * 
 */

public class AsymmetricKeyPair {
	static {
		SecurityHelper.ensureBC();
	}
	public static final int TYPE_UNDEFINED = -1;
	public static final int TYPE_RSA = 0;
	public static final int TYPE_DSA = 1; //dont want this...
	
	private PublicKey pubkey = null;
	private PrivateKey privkey = null;
	
	private AsymmetricCipherKeyPair keypair = null;
	private RSAKeyParameters rpub = null;
	private RSAPrivateCrtKeyParameters rpriv = null;
	
	private int	type = -1; 
	
	private AsymmetricKeyPair() {		
	}
	
	int keyid = -1;
	public long getKeyID() {
		if(keyid == -1) {
			org.bouncycastle.crypto.digests.SHA1Digest sha1 = new org.bouncycastle.crypto.digests.SHA1Digest();
			byte[] kk = rpub.getModulus().toByteArray();
			keyid = sha1.doFinal(kk, 0);			
		}
		
		return keyid;
	}
	
	public String getKeyIDHex() {
		return SecurityHelper.HexDecoder.encode(BigInteger.valueOf(getKeyID()).toByteArray());		
	}

	public AsymmetricKeyPair(AsymmetricCipherKeyPair keyPair) {
		int algo = TYPE_RSA;

		CipherParameters pub = keyPair.getPublic();
		CipherParameters priv = keyPair.getPrivate();

		this.rpub = (RSAKeyParameters)pub;
		this.rpriv = (RSAPrivateCrtKeyParameters)priv;
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
	
	public boolean isRSA() {
		if (type==TYPE_RSA) return true;
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
	
	public static void main(String[] args) throws Exception {
		byte[] enc = "me is to encode".getBytes();
		AsymmetricKeyPair ak = KeyPairGenerator.generateAsymmetricKeyPair();
		
//		ak.encrypt(enc, ak.getPGPPublicKey());
	}
}

