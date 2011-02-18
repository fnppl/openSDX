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

	/**
	 * Generates a RSA_2048 KeyPair
	 *  
	 * @param identity
	 * @param passPhrase secret passphrase
	 * @return the asymetricKeyPair
	 */
	
	public static AsymmetricKeyPair generateAsymmetricKeyPair() throws Exception {
		Security.addProvider(new BouncyCastleProvider());
		java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA", "BC");
		kpg.initialize(2048);
		KeyPair kp = kpg.generateKeyPair();
		PGPKeyPair pgpkp = new PGPKeyPair(PGPPublicKey.RSA_GENERAL, kp, new Date());
		//System.out.println(pgpkp.getKeyID());
		return new AsymmetricKeyPair(pgpkp);
	}
	
	

}
