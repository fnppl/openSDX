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
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;

public class PublicKey {

	private PGPPublicKey key;
	
	public PublicKey(PGPPublicKey key) {
		this.key = key;
	}
	
	public PublicKey(String fromASC) throws Exception {
		//TODO really that complicated?
		InputStream keyIn = PGPUtil.getDecoderStream(new ByteArrayInputStream(fromASC.getBytes()));
		PGPPublicKeyRingCollection pgpRings = new PGPPublicKeyRingCollection(keyIn);
		key = ((PGPPublicKeyRing)pgpRings.getKeyRings().next()).getPublicKey();
	}
	
	public PGPPublicKey getPGPPublicKey() {
		return key;
	}
}
