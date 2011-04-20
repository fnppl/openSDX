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

import java.util.Arrays;
import java.util.Vector;


/**
 * Provides methods for the verification of keys by resolving a "chain-of-trust"
 * to given keys trusted by the user
 *
 * @author Bertram Boedeker <boedeker@it-is-awesome.de>
 */
public class KeyVerificator {
	
	private static Vector<OSDXKey> trustedKeys = new Vector<OSDXKey>();
	
	
	public static void addTrustedKey(OSDXKey key) {
		trustedKeys.add(key);
	}
	
	public static Result verifyKey(OSDXKey key) {
		//sha1 of key modulus = keyid
		byte[] keyid = SecurityHelper.HexDecoder.decode(OSDXKey.getFormattedKeyIDModulusOnly(key.getKeyID()));
		if (!Arrays.equals(keyid, SecurityHelper.getSHA1(key.getPublicModulusBytes()))) {
			return Result.error("keyid dos not match sha1 of key modulus");
		}
		//already trusted key
		if (isTrustedKey(key.getKeyID())) return Result.succeeded();
		
		//
		
		
		return Result.error("unknown error");
	}
	
	private static boolean isTrustedKey(String keyid) {
		for (OSDXKey t : trustedKeys) {
			if (t.getKeyID().equals(keyid)) {
				return true;
			}
		}
		return false;
	}
	
	private static Vector<KeyLog> requestKeyLogs(OSDXKey key) {
		Vector<KeyLog> result = new Vector<KeyLog>();
		
		
		return result;
	}
	
}
