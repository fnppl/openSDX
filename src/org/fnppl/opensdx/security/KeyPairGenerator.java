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
import java.util.*;
import java.security.*;

import org.bouncycastle.bcpg.*;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;

public class KeyPairGenerator {

	public static AsymmetricKeyPair generateKeyPair() { //always RSA_2048 !!!
		return null;
	}
	
	
	//TODO save to KeyRing
	public static void generateRSAKeyPair(String identity, String passPhrase, boolean asc) throws Exception {
		Security.addProvider(new BouncyCastleProvider());
		java.security.KeyPairGenerator    kpg = java.security.KeyPairGenerator.getInstance("RSA", "BC");
		kpg.initialize(2048);
		KeyPair kp = kpg.generateKeyPair();

		String ending = "bpg";
		if (asc) {
			ending = "asc";
		}
		File userdir = new File(System.getProperty("user.home"));
		File keydir = new File(userdir, "keypairs");
		keydir.mkdirs();
		
		//TODO save to KeyRing 
		File fsKey = new File(keydir, "secret."+ending);
		File fpKey = new File(keydir, "public."+ending);
		
		OutputStream secretOut = new FileOutputStream(fsKey);
		OutputStream publicOut = new FileOutputStream(fpKey);
		
		if (asc) {
			secretOut = new ArmoredOutputStream(secretOut);
			publicOut = new ArmoredOutputStream(publicOut);
		}

		PGPSecretKey secretKey = new PGPSecretKey(
				PGPSignature.DEFAULT_CERTIFICATION, 
				PGPPublicKey.RSA_GENERAL, 
				kp.getPublic(), 
				kp.getPrivate(), 
				new Date(), 
				identity, 
				PGPEncryptedData.CAST5, 
				passPhrase.toCharArray(), 
				null, 
				null, 
				new SecureRandom(), 
				"BC"
			);
		secretKey.encode(secretOut);
		secretOut.close();
		
		PGPPublicKey key = secretKey.getPublicKey();
		key.encode(publicOut);
		publicOut.close();
	}
	
	

}
