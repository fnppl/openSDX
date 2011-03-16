package org.fnppl.opensdx.security.test;

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
 * For those parts of this file, which are identified as software, rather than documentation, this software-license applies / shall be applied. 
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
 * For those parts of this file, which are identified as documentation, rather than software, this documentation-license applies / shall be applied.
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

/**
 * 
 * @author Bertram Bödeker <bboedeker@gmx.de>
 *
 */

import java.io.File;
import java.math.BigInteger;
import java.util.Vector;

import org.fnppl.opensdx.gui.DefaultMessageHandler;
import org.fnppl.opensdx.security.AsymmetricKeyPair;
import org.fnppl.opensdx.security.Identity;
import org.fnppl.opensdx.security.KeyApprovingStore;
import org.fnppl.opensdx.security.PublicKey;
import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.security.SymmetricKey;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;


public class Test {
	

	
	/**
	 * for Testing
	 * @param args
	 */
	public static void main(String[] args) {
		
		//try to read from example_keystore.xml
		try {
			KeyApprovingStore store = KeyApprovingStore.fromFile(new File("src/org/fnppl/opensdx/security/resources/example_keystore.xml"), new DefaultMessageHandler());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//testGenerateMasterKeyPair();
		//testGeneratePublicKey();
	}
	
	public static void testGenerateMasterKeyPair() {
		try {
			String mantraname = "masterkeypassword";
			String password = "password";
			String initv = "00112233445566778899AABBCCDDEEFF";
			SymmetricKey sk = SymmetricKey.getKeyFromPass(password.toCharArray(), SecurityHelper.HexDecoder.decode(initv));
			Vector<Identity> ids = new Vector<Identity>();
			Element id = new Element("identity");
			id.addContent("email", "test@fnppl.org");
			ids.add(Identity.fromElement(id));
			
			Element ekp = generateMasterKeyPair(ids, mantraname, sk);
			
			Document.buildDocument(ekp).output(System.out);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static Element generateMasterKeyPair(Vector<Identity> ids, String mantraname, SymmetricKey sk) throws Exception {
		
		AsymmetricKeyPair kp = AsymmetricKeyPair.generateAsymmetricKeyPair();
		
		//build structure
		Element ekp = new Element("keypair");
		//id part
		Element eids = new Element("identities");
		for (int i=0;i<ids.size();i++) {
			eids.addContent(ids.get(0).toElement());	
		}
		ekp.addContent(eids);
		
	  //key part
		//keyid
		ekp.addContent("sha1fingerprint", kp.getKeyID());
		ekp.addContent("authoritativekeyserver","keys.fnppl.org");
		
		//datapath
		Element edp = new Element("datapath");
		Element es1 = new Element("step1");
		es1.addContent("datasource", "keys.fnppl.org");
		es1.addContent("datainsertdatetime","2011-02-21 00:00:00 GMT+00:00");
		Element es2 = new Element("step2");
		es2.addContent("datasource", "keys.fnppl.org");
		es2.addContent("datainsertdatetime","2011-02-21 00:00:00 GMT+00:00");
		edp.addContent(es1);
		edp.addContent(es2);
		ekp.addContent(edp);
		
		//
		ekp.addContent("usage","ONLYSIGN");
		ekp.addContent("level","MASTER");
		ekp.addContent("parentkeyid","N.A.");
		
		ekp.addContent("algo","RSA");
		ekp.addContent("bits","3072");
		ekp.addContent("modulus",kp.getModulusAsHex());
		
		//public key
		Element epk = new Element("pubkey");
		epk.addContent("exponent", kp.getPublicExponentAsHex());
		ekp.addContent(epk);
		
		//private key
		Element esk = new Element("privkey");
		Element eexp = new Element("exponent");
		Element eloc = new Element("locked");
		
		eloc.addContent("mantraname",mantraname);
		eloc.addContent("algo","AES@256");
		eloc.addContent("initvector",SecurityHelper.HexDecoder.encode(sk.getInitVector(),'\0',-1));
		eloc.addContent("padding","CBC/PKCS#7");
		eloc.addContent("bytes",SecurityHelper.HexDecoder.encode(kp.getEncrytedPrivateKey(sk),'\0',-1));
		eexp.addContent(eloc);
		esk.addContent(eexp);
		ekp.addContent(esk);
		
		//gpgkeyserverid
		ekp.addContent("gpgkeyserverid","");
		
		return ekp;
	}
	
	public static void testGeneratePublicKey() {
		try {
			String mod = "00B1C4337FE2E77E251D86334B1578C0BCA46AEB9CFE1BE7001ADA8E4C2C8BC2EE557296C46EC3D0470A6DEEC09634A424243B576F3DCA41E372F9AC2FEAA0B668F2AA000CCDCC0396BE4517B2F4B179FCCB7ACB9B3AF027DDAC3466AE80D70BEFBBC0C97E9E4AAF7D184DFE183F74BC9FFA5A5F85149B5A9808C7E12EFDEF42C4A936661F06BA15844DD0BCAF3C0CB8E04949263660A1E71DC1B4A0056519A6E662CEAB25F1B42DA537C21AD6584C2BF72092A0EC57A5C7E9A6458CC8BC06102B5902D90BD86850DA411DB004D66399F5B362EEA0DD5178AC89423FA60E63405290536067AF3EBF9F26E1DFE66E11B23209B62E062ED7F177B6F41CC97F1D6517F2542F40660ABC8D17D7B99778997013D69837FE410B137A283B461D6323F5042A49A59CFEF343B4829B751495400151514CD77ADCB9011F0054D6DF5E6B073EE2A96ECCDDE9029F18DA6C6361DA0147DD7FA59A44B7C87B1A82BDFEFD0DD8E6FCEB696E4883E27EDE204B669887B37D6C927071CFAE555BD235A3E19165C9B7";
			String exp = "010001";
			PublicKey k = new PublicKey(
					new BigInteger(SecurityHelper.HexDecoder.decode(mod)),
					new BigInteger(SecurityHelper.HexDecoder.decode(exp)));
			String parentkeyid = "BEEE542006AF8301096BF0305AB4632E9982AA94@keys.fnppl.org";

			Element epk = generateGeneratePublicKeyElement(k, parentkeyid);

			Document.buildDocument(epk).output(System.out);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	public static Element generateGeneratePublicKeyElement(PublicKey k, String parentkeyid) throws Exception {
		
		//build structure
		Element ekp = new Element("keypair");
		//no id part
		
	  //key part
		//keyid
		ekp.addContent("sha1fingerprint", k.getKeyID());
		ekp.addContent("authoritativekeyserver","keys.fnppl.org");
		
		//datapath
		Element edp = new Element("datapath");
		Element es1 = new Element("step1");
		es1.addContent("datasource", "keys.fnppl.org");
		es1.addContent("datainsertdatetime","2011-02-21 00:00:00 GMT+00:00");
		Element es2 = new Element("step2");
		es2.addContent("datasource", "keys.fnppl.org");
		es2.addContent("datainsertdatetime","2011-02-21 00:00:00 GMT+00:00");
		edp.addContent(es1);
		edp.addContent(es2);
		ekp.addContent(edp);
		
		//
		ekp.addContent("usage","ONLYSIGN");
		ekp.addContent("level","SUB");
		ekp.addContent("parentkeyid",parentkeyid);
		
		ekp.addContent("algo","RSA");
		ekp.addContent("bits","3072");
		ekp.addContent("modulus",k.getModulusAsHex());
		
		//public key
		Element epk = new Element("pubkey");
		epk.addContent("exponent", k.getPublicExponentAsHex());
		ekp.addContent(epk);
		
		//no private key
		
		//gpgkeyserverid
		ekp.addContent("gpgkeyserverid","");
		
		return ekp;
	}
	
}

