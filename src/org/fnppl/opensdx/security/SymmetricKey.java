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
import java.util.*;
import java.security.*;

import org.bouncycastle.crypto.engines.*;
import org.bouncycastle.crypto.*; 
import org.bouncycastle.crypto.modes.*;
import org.bouncycastle.crypto.encodings.*;
import org.bouncycastle.crypto.paddings.*;
import org.bouncycastle.crypto.params.*;

import com.sun.crypto.provider.AESCipher;
import com.sun.org.apache.bcel.internal.generic.AASTORE;



/*
 * hrmpf. most probably not used in this context - hooray for Rijndael_256 !!!
 */

/*
 * @author Henning Thieß <ht@fnppl.org>
 * 
 */

public class SymmetricKey {
	static {
		SecurityHelper.ensureBC();
	}
	
	private final static int keybits = 256;//ok, doing so fails the aes128-rule and may fall into US-weapons-regulation
	private final static int blockbits = 128;
	
	private byte[] initVector = null;
	private byte[] keyBytes = null;
	
	public SymmetricKey(byte[] key_bytes, byte[] iv
			) {
		this.keyBytes = key_bytes;
		this.initVector = iv;
	}
	
	public static SymmetricKey getRandomKey() {
		SecureRandom sc = new SecureRandom();//TODO HT 20.02.2011 - quite good, but should swirl it twice with tiger, or aes/rijndael itself		
		byte[] aes_key_bytes = new byte[keybits / 8]; //yep. please be aware of non-8-dividable bits - however, should be 128 for various reasons
        
		byte[] iv = new byte[blockbits/8];
        sc.nextBytes(aes_key_bytes);
        sc.nextBytes(iv);
        
        //now should swirl those byte one more time...
        
        return new SymmetricKey(aes_key_bytes, iv);
	}
	
	
	public static SymmetricKey getKeyFromPass(char[] pass, byte[] iv) throws Exception {
		if(iv.length != blockbits/8) {
			throw new RuntimeException("Invalid InitVector-Size: "+iv.length+" expected: "+(blockbits/8));
		}
		byte[] aes_key_bytes = new byte[keybits / 8];
		
		byte[] sha256 = SecurityHelper.getSHA256(String.valueOf(pass).getBytes("UTF-8"));
		//System.err.println("getKeyFromPass:: ll.length:"+sha256.length+"\taes_key_bytes.length:"+aes_key_bytes.length);
		for(int i=0; i<aes_key_bytes.length; i++) {
			aes_key_bytes[i] = sha256[i];
		}
		
		SymmetricKey sk = new SymmetricKey(aes_key_bytes, iv);
		
		//System.out.println("INITVECTOR: "+SecurityHelper.HexDecoder.encode(sk.initVector,':',-1));
		//System.out.println("KEY: "+SecurityHelper.HexDecoder.encode(sk.keyBytes,':',-1));
		return sk;
	}
	
	public void encrypt(InputStream in, OutputStream out) throws Exception {
//		if(key.length!=initvector.length || key.length!=keybits/8) {
//			throw new Exception("invalid params");
//		}
		
		CBCBlockCipher aesCBC = new CBCBlockCipher(new AESEngine());
		KeyParameter kp = new KeyParameter(keyBytes);
		ParametersWithIV aesCBCParams = new ParametersWithIV(kp, initVector);
		
	    PaddedBufferedBlockCipher aesCipher = new PaddedBufferedBlockCipher(
	    		aesCBC,
	            new PKCS7Padding()
	    	);	    
	    aesCipher.init(true, aesCBCParams);
	    
	    int read = -1;
	    int or = 0;
	    int rr = 0;
	    byte[] buff = new byte[16];
	    byte[] buff2 = new byte[48];
	    while((read=in.read(buff)) != -1) {
	    	rr += read;
			int rg = aesCipher.processBytes(buff, 0, read, buff2, 0);
//			System.err.println("READ: "+read);
//			System.err.println("PROCESS_BYTES_RETURN: "+rg);
			
			out.write(buff2, 0, rg);
			or += rg;
		}
//		int oss = aesCipher.getOutputSize(rr);
		
//		System.err.println("BYTES_WRITTEN_OVERALL: "+or);
//		System.err.println("BYTES_READ_OVERALL: "+rr);
//		System.err.println("AESCIPHER.getOutputSize("+rr+"): "+oss);
		
//		int rest = oss - or;
		read = aesCipher.doFinal(buff2, 0);
//		System.err.println("READ_LAST: "+read);
		
		out.write(buff2, 0, read);	
	}
	public byte[] encrypt(byte[] b) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		encrypt(new ByteArrayInputStream(b), out);
		
		return out.toByteArray();
	 }
	public byte[] decrypt(byte[] b) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		decrypt(new ByteArrayInputStream(b), out);
		
		return out.toByteArray();
	 }
	public void decrypt(InputStream in, OutputStream out) throws Exception {
//		if(key.length!=initvector.length || key.length!=keybits/8) {
//			throw new Exception("invalid params");
//		}
		
		CBCBlockCipher aesCBC = new CBCBlockCipher(new AESEngine());
		
		KeyParameter kp = new KeyParameter(keyBytes);
		ParametersWithIV aesCBCParams = new ParametersWithIV(kp, initVector);
		
	    PaddedBufferedBlockCipher aesCipher = new PaddedBufferedBlockCipher(
	    		aesCBC,
	            new PKCS7Padding()
	    	);
	    
	    aesCipher.init(false, aesCBCParams);
	   // aesCipher.init(true, aesCBCParams); //TODO pad block corrupted error when false. WHY??
	    
	    int read = -1;
	    byte[] buff = new byte[128/8];//blocksize
		while((read=in.read(buff)) != -1) {
			byte[] ou = new byte[buff.length];
//			System.err.println("read: "+read);

			int rg = aesCipher.processBytes(buff, 0, read, ou, 0);
			out.write(ou, 0, rg);
//			System.err.println("rg: "+rg);
		}

		buff = new byte[2*128/8];//blocksize
		read = aesCipher.doFinal(buff, 0);
		out.write(buff, 0, read);
	}
	
	public byte[] getInitVector() {
		return initVector;
	}
	public byte[] getKeyBytes() {
		return keyBytes;
	}
	public static void main(String[] args) throws Exception {
	
		//test encryption of private key
		AsymmetricKeyPair akp = AsymmetricKeyPair.generateAsymmetricKeyPair();
		
		String initv = "00112233445566778899AABBCCDDEEFF";
		String pp = "password";
		SymmetricKey sk = SymmetricKey.getKeyFromPass(pp.toCharArray(), SecurityHelper.HexDecoder.decode(initv));

		byte[] encPrivKey =  akp.getEncrytedPrivateKey(sk);
		byte[] decPrivKey = sk.decrypt(encPrivKey);
		
		System.out.println("PUB_KEY_MODULUS     : "+SecurityHelper.HexDecoder.encode(akp.getModulus(),':',-1));
		System.out.println("PUB_KEY_EXP     : "+SecurityHelper.HexDecoder.encode(akp.getPublicExponent(),':',-1));
		System.out.println("ENC_PRIV_KEY_EXP: "+SecurityHelper.HexDecoder.encode(encPrivKey,':',-1));
		System.out.println("DEC_PRIV_KEY_EXP: "+SecurityHelper.HexDecoder.encode(decPrivKey,':',-1));
		System.out.println("keyid           : "+akp.getKeyID());             
		
		
//		SymmetricKey l = SymmetricKey.getRandomKey();
//		System.out.println("INITVECTOR: "+SecurityHelper.HexDecoder.encode(l.initVector,':',-1));
//		System.out.println("KEY: "+SecurityHelper.HexDecoder.encode(l.keyBytes,':',-1));
		
		
//		byte[] sha256 = SecurityHelper.getSHA256(String.valueOf(pp.toCharArray()).getBytes("UTF-8"));
//		System.out.println("pass: "+pp);
//		System.out.println("key: "+SecurityHelper.HexDecoder.encode(sha256,'\0',-1));
		
		
//		INITVECTOR: 1D8BEE695B7F4EFF6F7B947F1B197B97
//		KEY: 9034F3A02E7DBD9870D7FC23FCD0E3CA5B9292F7F2314B495DBF042078632B24
		
//		byte[] key = SecurityHelper.HexDecoder.decode("9034F3A02E7DBD9870D7FC23FCD0E3CA5B9292F7F2314B495DBF042078632B24");
//		byte[] init = SecurityHelper.HexDecoder.decode("2A8BEE695B7F4EFF6F7B947F1B197B97");
//		
//		SymmetricKey l = new SymmetricKey(key, init);
////		
		byte[] test = "ich asda will encoded werden...".getBytes();
//		
		byte[] enc = sk.encrypt(test); 
		byte[] dec = sk.decrypt(enc);
		
		System.out.println("BEFORE: "+(new String(test)));
		System.out.println("ENC: "+SecurityHelper.HexDecoder.encode(enc,':',-1));
		System.out.println("AFTER: "+(new String(dec)));
		
	}
}