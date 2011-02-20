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
import java.security.Key;

import org.bouncycastle.crypto.*;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.crypto.params.*;




public class PrivateKey {
	static {
		SecurityHelper.ensureBC();
	}
	
	private RSAPrivateCrtKeyParameters key;
	
	
	public PrivateKey(RSAPrivateCrtKeyParameters key) {
//	public PrivateKey(PGPPrivateKey key) {
		this.key = key;
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
}

