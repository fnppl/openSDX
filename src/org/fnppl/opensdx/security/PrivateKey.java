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
import java.security.Key;
import java.security.SecureRandom;

import org.bouncycastle.crypto.*;
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
		this.priv = new RSAKeyParameters(true, modulus, exponent);
	}
	public PrivateKey(RSAKeyParameters key) {
		this.priv = key;
	}
	
	
	public byte[] sign(byte[] data) throws Exception {
		RSAEngine rsae = new RSAEngine();
		rsae.init(
				false,
				priv
			);
		byte[] filleddata = new byte[rsae.getInputBlockSize()-1];
		for(int i=0; i<filleddata.length; i++) {
			filleddata[i] = data[i % data.length];//HT 2011-03-03 better some initvectorpadddup!!!
		}
		
		//System.out.println("PrivKey_SIGN_PLAINBLOATED:\t"+SecurityHelper.HexDecoder.encode(filleddata, ':', 80));
		
		return rsae.processBlock(filleddata, 0, filleddata.length);
	}
	public byte[] decrypt(byte[] data) throws Exception {
		RSABlindedEngine rsae = new RSABlindedEngine();
		
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
	
	public byte[] getEncrytedPrivateKey(SymmetricKey sk) throws Exception {
		return sk.encrypt(priv.getExponent().toByteArray());
	}
	
}

