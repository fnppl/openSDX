package org.fnppl.opensdx.security;

import org.bouncycastle.openpgp.PGPKeyPair;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;

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
	
	public static final int TYPE_UNDEFINED = -1;
	public static final int TYPE_RSA = 0;
	public static final int TYPE_DSA = 1; //dont want this...
	
	
	private PGPKeyPair keypair = null;
	private int	type = -1; 
	
	public AsymmetricKeyPair() {
		
	}
	
	public long getKeyID() {
		return keypair.getKeyID();
	}
	
	public String getKeyIDHex() {
		return "0x"+Long.toHexString(getKeyID());
	}
	
	public AsymmetricKeyPair(PGPKeyPair kp) {
		keypair = kp;
		int algo = keypair.getPublicKey().getAlgorithm();
		if (algo == PGPPublicKey.RSA_GENERAL || algo == PGPPublicKey.RSA_SIGN || algo == PGPPublicKey.RSA_ENCRYPT) {
			type = TYPE_RSA;
		} else if (algo == PGPPublicKey.DSA) {
			type = TYPE_DSA;
		} else {
			type = TYPE_UNDEFINED;
		}
		
	}
	
	public PGPPublicKey getPGPPublicKey() {
		return keypair.getPublicKey();
	}
	
	public PGPPrivateKey getPGPPrivateKey() {
		return keypair.getPrivateKey();
	}
	
	public PGPKeyPair getPGPKeyPair() {
		return keypair;	
	}
	
	public int getType() {
		return type;
	}
	
	public boolean isRSA() {
		if (type==TYPE_RSA) return true;
		return false;
	}
	
}
