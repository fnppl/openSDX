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
import org.fnppl.opensdx.xml.*;

public class KeyApprovingStore {
	private File f = null;
	
	public KeyApprovingStore() {
		
	}
	public static KeyApprovingStore fromFile(File f) throws Exception {
		Document d = Document.fromFile(f);
		Element e = d.getRootElement();
		if(!e.getName().equals("keystore")) {
			throw new Exception("KeyStorefile must have \"keystore\" as root-element");
		}
		KeyApprovingStore kas = new KeyApprovingStore();
		Element keys = e.getChild("keys");
		Vector<Element> ves = e.getChildren("keypair");
		
		for(int i=0;i<ves.size();i++) {
			Element kp = ves.elementAt(i);
			Vector<Element> ids = kp.getChildren("identities");
			for(int j=0;j<ids.size();j++) {
				Element id = ids.elementAt(j);
				//TODO
				
//				<identity>
//				<email>jaja@kakak.nät</email><!-- MUST -->
//				<mnemonic></mnemonic><!-- SHOULD ; shorthandle for this identities-purpose "residency" or "work" or whatever -->
//				<phone>+44 99 00202021</phone><!-- COULD -->
//				<country></country><!-- COULD -->
//				<region></region><!-- COULD -->
//				<postcode></postcode><!-- COULD -->		
//				<company></company><!-- COULD -->
//				<unit></unit><!-- COULD -->
//				<subunit></subunit><!-- COULD -->
//				
//				<function></function><!-- COULD ; function of that person -->
//				<surname></surname><!-- COULD -->
//				<middlename></middlename><!-- COULD -->
//				<name></name><!-- COULD -->
//				
//				<note></note><!-- COULD -->
//				<sha1></sha1><!-- sha1 as hex of concat of all above fields (also empty ones) -->
//				<!-- please be aware of the exact order of these fields... -->
//			</identity>
			} //identities
			
//			AsymmetricKeyPair akp = new AsymmetricKeyPair();
			String shafp = kp.getChildText("sha1fingerprint");
			String authoritativekeyserver = kp.getChildText("authoritativekeyserver");
			String usage = kp.getChildText("usage");
			String type = kp.getChildText("type");
			String parentkeyid = kp.getChildText("parentkeyid");
			String Salgo = kp.getChildText("algo");
			int bits = kp.getChildInt("bits");
			String Smodulus = kp.getChildText("modulus");
			
			Element pubkey = kp.getChild("pubkey");
			String pubkey_exponent = pubkey.getChildText("exponent");
			
			Element privkey = kp.getChild("privkey");
			Element exponent = kp.getChild("exponent");
			if(exponent.getChild("locked") != null) {
				Element lk = exponent.getChild("locked");
				String mantraname = lk.getChildText("mantraname");
				String Slock_algo = lk.getChildText("algo");
				String Sinitv = lk.getChildText("initvector");
				String Spadding = lk.getChildText("padding");
				String Sbytes = lk.getChildText("bytes");
				byte[] bytes = SecurityHelper.HexDecoder.decode(Sbytes);
				
				try {
					String pp = null;
					System.out.println("Please enter Passphrase for "+mantraname+": ");
					BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
					pp = br.readLine();
				
					SymmetricKey sk = SymmetricKey.getKeyFromPass(pp.toCharArray(), SecurityHelper.HexDecoder.decode(Sinitv));
					
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
			
			String gpgkeyserverid = kp.getChildText("gpgkeyserverid");
		}
		
		return kas;
	}
}


