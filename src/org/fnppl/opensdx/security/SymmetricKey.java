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
import org.bouncycastle.crypto.encodings.*;
import org.bouncycastle.crypto.paddings.*;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.jce.provider.symmetric.AES.KeyGen;



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
	
//	private byte[] initVector = null;
	private byte[] keyBytes = null;
	
	public SymmetricKey(byte[] key_bytes
//			, byte[] iv
			) {
		this.keyBytes = key_bytes;
//		this.initVector = iv;
	}
	
	public static SymmetricKey getRandomKey() {		
		SecureRandom sc = new SecureRandom();//TODO HT 20.02.2011 - quite good, but should swirl it twice with tiger, or aes/rijndael itself		
		byte[] aes_key_bytes = new byte[keybits/8]; //yep. please be aware of non-8-dividable bits - however, should be 128 for various reasons
//        byte[] iv = new byte[blockbits/8];
        sc.nextBytes(aes_key_bytes);
//        sc.nextBytes(iv);
        
        //now should swirl those byte one more time...
        
        return new SymmetricKey(aes_key_bytes);
	}
	
	public void encrypt(InputStream in, OutputStream out, byte[] k) throws Exception {
	    if(k.length != 32) {
	    	throw new Exception("Me wants 256 bit-key (=32 bytes)");
	    }
	    
	    //http://www.bouncycastle.org/docs/docs1.6/org/bouncycastle/crypto/BlockCipher.html
	    CipherParameters cp = new KeyParameter(keyBytes);
	    BlockCipher aes = new AESEngine();

	    // (http://www.bouncycastle.org/docs/docs1.6/org/bouncycastle/crypto/paddings/BlockCipherPadding.html):
	    BlockCipherPadding padd = new ZeroBytePadding();
	    BufferedBlockCipher bufferedAES = new PaddedBufferedBlockCipher(aes, padd);

	    aes.init(true, cp);

//	    int inputOffset = 0;
//	    int inputLength = input.length;
//	    int maximumOutputLength = bufferedAES.getOutputSize(inputLength);	    
//	    byte[] output = new byte[maximumOutputLength];
//	    int outputOffset = 0;
//	    int outputLength = 0;

	    int bytesProcessed;

	    int read = 0;
	    byte[] buff = new byte[128];
	    while((read=in.read(buff)) != -1) {
	    	byte[] bt = new byte[bufferedAES.getOutputSize(read)];
	    	
	    	bufferedAES.processBytes(buff, 0, read, bt, 0);
	    	
	    	out.write(bt);
	    }
	    out.flush();
	    in.close();
	    //out.close()
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
