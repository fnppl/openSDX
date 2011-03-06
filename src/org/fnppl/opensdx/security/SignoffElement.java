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
import java.math.*;
import java.text.SimpleDateFormat;
import java.util.*;

import org.fnppl.opensdx.xml.Element;

public class SignoffElement extends Element {
	private SignoffElement() {
		super("signoff");
	}
	
	//HT 2011-03-06 quite unused - overruled by signature.java - most probably to be erased soon.
	
	
//	public static SignoffElement getSignoffElement(Signature s) {
//		return null;
//	}
//	public static SignoffElement fromElement(Element s) {
//		return null;
//	}
	
//	public static SignoffElement getSignoffElement(byte[] data, OSDXKeyObject key) throws Exception {
//		ByteArrayInputStream bin = new ByteArrayInputStream(data);
//		
//		return getSignoffElement(bin, key);
//	}
//	public static SignoffElement getSignoffElement(InputStream in, OSDXKeyObject key) throws Exception {
//		if(!key.allowsSigning()) {
//			return null;
//		}
//		byte[] sha256 = SecurityHelper.getSHA256(in);
//		SignoffElement ret = new SignoffElement();
//		String keyid = key.getKeyID();
//		Element e = key.getSimplePubKeyElement();
//		
//		ret.addContent("keyid", keyid);
//		ret.addContent(e);
//	
//		byte[] bytes = key.sign(md5, sha1, sha256, datetime);
//		ret.addContent("sha256", SecurityHelper.HexDecoder.encode(sha256, '\0', -1));		
//		ret.addContent("signaturebytes", SecurityHelper.HexDecoder.encode(bytes, '\0', -1));
//		
////		<signoff>
////		<keyid>kjakdjadkjajd@keys.fnppl.org</keyid>
////		<pubkey>
////			<algo>RSA</algo><!-- RSA ; others not implemented yet -->
////			<bits>3072</bits><!-- well, yes, count yourself, but nice to *see* it -->
////			<modulus></modulus><!-- as hex-string with or without leading 0x ; only for RSA?! -->
////			<exponent></exponent><!-- as hex-string with or without leading 0x -->
////			</pubkey><!-- given, but should be verified from server/yourself... -->
////	
////			<bytes>asdasd</bytes><!-- as hex-string with ":" or " " separation... looks nicer... -->
////			</signoff>
//		
//		return ret;
//	}
//	
//	public static boolean verifySignoff(Element signoff, byte[] data) throws Exception {
//		byte[] datasha1 = SecurityHelper.getSHA1(data);
//		byte[] signoffsha1 = SecurityHelper.HexDecoder.decode(signoff.getChildText("sha1"));
//		if (!Arrays.equals(datasha1, signoffsha1)) {
//			System.out.println("data SHA1 does not match signature SHA1");
//			System.out.println("data SHA1: "+SecurityHelper.HexDecoder.encode(datasha1, '\0', -1));
//			System.out.println("sign SHA1: "+SecurityHelper.HexDecoder.encode(signoffsha1, '\0', -1));
//			return false;
//		}
//		
//		Element epk = signoff.getChild("pubkey");
//		BigInteger mod = new BigInteger(SecurityHelper.HexDecoder.decode(epk.getChildText("modulus")));
//		BigInteger exp = new BigInteger(SecurityHelper.HexDecoder.decode(epk.getChildText("exponent")));
//		PublicKey pubkey = new PublicKey(mod, exp);
//		byte[] signaturebytes = SecurityHelper.HexDecoder.decode(signoff.getChildText("signaturebytes"));
//		return pubkey.verify(signaturebytes, signoffsha1);
//	}
}
