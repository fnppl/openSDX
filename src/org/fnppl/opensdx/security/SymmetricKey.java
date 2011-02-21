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
import java.util.*;
import java.security.*;

import org.bouncycastle.crypto.engines.*;
import org.bouncycastle.crypto.*; 
import org.bouncycastle.crypto.modes.*;
import org.bouncycastle.crypto.encodings.*;
import org.bouncycastle.crypto.paddings.*;
import org.bouncycastle.crypto.params.*;

import com.sun.crypto.provider.AESCipher;



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
		byte[] aes_key_bytes = new byte[keybits/8]; //yep. please be aware of non-8-dividable bits - however, should be 128 for various reasons
        
		byte[] iv = new byte[blockbits/8];
        sc.nextBytes(aes_key_bytes);
        sc.nextBytes(iv);
        
        //now should swirl those byte one more time...
        
        return new SymmetricKey(aes_key_bytes, iv);
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
	
	public static void main(String[] args) throws Exception {
		SymmetricKey l = SymmetricKey.getRandomKey();
		System.out.println("INITVECTOR: "+SecurityHelper.HexDecoder.encode(l.initVector));
		System.out.println("KEY: "+SecurityHelper.HexDecoder.encode(l.keyBytes));
		
		
//		INITVECTOR: 1D8BEE695B7F4EFF6F7B947F1B197B97
//		KEY: 9034F3A02E7DBD9870D7FC23FCD0E3CA5B9292F7F2314B495DBF042078632B24
		
//		byte[] key = SecurityHelper.HexDecoder.decode("9034F3A02E7DBD9870D7FC23FCD0E3CA5B9292F7F2314B495DBF042078632B24");
//		byte[] init = SecurityHelper.HexDecoder.decode("2A8BEE695B7F4EFF6F7B947F1B197B97");
		
//		SymmetricKey l = new SymmetricKey(key, init);
//		
		byte[] test = "ich asda will encoded werden...".getBytes();
		
		byte[] enc = l.encrypt(test); 
		byte[] dec = l.decrypt(enc);
		
		System.out.println("BEFORE: "+(new String(test)));
		System.out.println("ENC: "+SecurityHelper.HexDecoder.encode(enc));
		System.out.println("AFTER: "+(new String(dec)));
		
	}
}


//COPIED from
//http://tirl.org/blogs/media-lab-blog/47/
	
//import java.io.IOException;
//import java.io.InputStream;
//import java.math.BigInteger;
//import java.security.SecureRandom;
//
//import org.bouncycastle.crypto.AsymmetricBlockCipher;
//import org.bouncycastle.crypto.BlockCipher;
//import org.bouncycastle.crypto.InvalidCipherTextException;
//import org.bouncycastle.crypto.encodings.PKCS1Encoding;
//import org.bouncycastle.crypto.engines.AESLightEngine;
//import org.bouncycastle.crypto.engines.RSAEngine;
//import org.bouncycastle.crypto.modes.CBCBlockCipher;
//import org.bouncycastle.crypto.params.KeyParameter;
//import org.bouncycastle.crypto.params.ParametersWithIV;
//import org.bouncycastle.crypto.params.RSAKeyParameters;
//
//public class UploadCipher {
//    private static final int AES_KEY_LENGTH = 16; // 16 bytes for AES-128
//    private static final RSAPublicKey RSA_KEY = new RSAPublicKey("/rsa_public_key.res");
//
//    private ParametersWithIV aes_key;
//    private BlockCipher symmetricBlockCipher;
//    private AsymmetricBlockCipher asymmetricBlockCipher;
//    private int symmetricBlockSize;
//    private SecureRandom secureRandom;
//
//    public UploadCipher() {
//        secureRandom = new SecureRandom();
//        // Prepare symmetric block cipher for message
//        symmetricBlockCipher = new CBCBlockCipher(new AESLightEngine());
//        symmetricBlockSize = symmetricBlockCipher.getBlockSize();
//        createAESKey();
//
//        // Prepare asymmetric block cipher for key
//        asymmetricBlockCipher = new PKCS1Encoding(new RSAEngine());
//        asymmetricBlockCipher.init(true, new RSAKeyParameters(false, RSA_KEY.MODULUS, RSA_KEY.EXPONENT));
//    }
//
//    private void createAESKey() {
//        byte[] aes_key_bytes = new byte[AES_KEY_LENGTH];
//        byte[] iv = new byte[symmetricBlockSize];
//        secureRandom.nextBytes(aes_key_bytes);
//        secureRandom.nextBytes(iv);
//        aes_key = new ParametersWithIV(new KeyParameter(aes_key_bytes), iv);
//    }
//    public byte[] encrypt(byte[] message) {
//        // initialize block cipher in "encryption" mode
//        symmetricBlockCipher.init(true, aes_key);  
//
//        // pad the message to a multiple of the block size
//        int numBlocks = (message.length / symmetricBlockSize) + 1;
//        byte[] plaintext = new byte[numBlocks * symmetricBlockSize];
//        System.arraycopy(message, 0, plaintext, 0, message.length);
//
//        // encrypt!
//        byte[] ciphertext = new byte[numBlocks * symmetricBlockSize];
//        for (int i = 0; i < ciphertext.length; i += symmetricBlockSize) {
//            symmetricBlockCipher.processBlock(plaintext, i, ciphertext, i);
//        }
//        return ciphertext;
//    }
//    public byte[] getKey() {
//        try {
//            byte[] key = ((KeyParameter) aes_key.getParameters()).getKey();
//            return asymmetricBlockCipher.processBlock(key, 0, key.length);
//        } catch (InvalidCipherTextException icte) {
//            icte.printStackTrace();
//            return null;
//        }
//    }
//    public byte[] getIV() {
//        // Encryption here is probably optional; some sources say the IV can be
//        // sent in plaintext.
//        try {
//            byte[] iv = aes_key.getIV();
//            return asymmetricBlockCipher.processBlock(iv, 0, iv.length);
//        } catch (InvalidCipherTextException icte) {
//            icte.printStackTrace();
//            return null;
//        }
//    }
//}
//class RSAPublicKey {
//    public BigInteger EXPONENT;
//    public BigInteger MODULUS;
//    public RSAPublicKey(String filename) {
//        InputStream in = this.getClass().getResourceAsStream(filename);
//        String contents = new String();
//        try {
//            int c;
//            while ((c = in.read()) != -1) {
//                contents += (char) c;
//            }
//        } catch (IOException e) {
//            System.err.println("Could not read RSA key resource.");
//        }
//        int linebreak = contents.indexOf("\n");
//        EXPONENT = new BigInteger(contents.substring(0, linebreak).trim());
//        MODULUS = new BigInteger(contents.substring(linebreak + 1).trim());
//    }
//}

//private static byte[] cipherData(PaddedBufferedBlockCipher cipher, byte[] data)
//throws Exception
//{
//int minSize = cipher.getOutputSize(data.length);
//byte[] outBuf = new byte[minSize];
//int length1 = cipher.processBytes(data, 0, data.length, outBuf, 0);
//int length2 = cipher.doFinal(outBuf, length1);
//int actualLength = length1 + length2;
//byte[] result = new byte[actualLength];
//System.arraycopy(outBuf, 0, result, 0, result.length);
//return result;
//}
//
//private static byte[] decrypt(byte[] cipher, byte[] key, byte[] iv) throws Exception
//{
//PaddedBufferedBlockCipher aes = new PaddedBufferedBlockCipher(new CBCBlockCipher(
//    new AESEngine()));
//CipherParameters ivAndKey = new ParametersWithIV(new KeyParameter(key), iv);
//aes.init(false, ivAndKey);
//return cipherData(aes, cipher);
//}
//
//private static byte[] encrypt(byte[] plain, byte[] key, byte[] iv) throws Exception
//{
//PaddedBufferedBlockCipher aes = new PaddedBufferedBlockCipher(new CBCBlockCipher(
//    new AESEngine()));
//CipherParameters ivAndKey = new ParametersWithIV(new KeyParameter(key), iv);
//aes.init(true, ivAndKey);
//return cipherData(aes, plain);
//}