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

//check this example: http://www.docjar.com/html/api/org/bouncycastle/openpgp/examples/DetachedSignatureProcessor.java.html

public class KeyRingCollection {
	File f = null;
	boolean this_is_private_keyring_collection = false;
	
	public static KeyRingCollection generateNewKeyRingOnFile(File f, boolean isprivate) throws Exception {
		KeyRingCollection k = new KeyRingCollection();
		k.f = f;
		k.this_is_private_keyring_collection = isprivate;
		
		return k;
	}
	public static KeyRingCollection fromFile(File f, char[] pass, boolean isprivate) {
		KeyRingCollection k = new KeyRingCollection();
		k.f = f;
		k.this_is_private_keyring_collection = isprivate;
		
		
		return k;
	}
}


