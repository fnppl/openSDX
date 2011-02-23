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
import java.math.*;
import java.util.*;

import org.fnppl.opensdx.xml.Element;

public class OSDXKeyObject {
//	public static final int ALGO_UNDEFINED = -1;
	public static final int ALGO_RSA = 0;
//	public static final int ALGO_DSA = 1; //dont want this...
	private static final Vector<String> algo_name = new Vector<String>();
	static {
		algo_name.addElement("RSA");		
	};
	
	public static final int USAGE_SIGN = 0;
	public static final int USAGE_CRYPT = 1;
	public static final int USAGE_WHATEVER = 2;
	private static final Vector<String> usage_name = new Vector<String>();
	static {
		usage_name.addElement("ONLYSIGN");
		usage_name.addElement("ONLYCRYPT");
		usage_name.addElement("BOTH");
	};
	
	public static final int LEVEL_MASTER = 0;
	public static final int LEVEL_REVOKE = 1;
	public static final int LEVEL_SUB = 2;
	private static final Vector<String> level_name = new Vector<String>();
	static {
		level_name.addElement("MASTER");
		level_name.addElement("REVOKE");
		level_name.addElement("SUB");
	};
	
	private OSDXKeyObject parentosdxkeyobject = null;
	private String parentkeyid = null;//could be the parentkey is not loaded - then *only* the id is present
	private String authoritativekeyserver = null;
	
	private int	level = LEVEL_MASTER;
	private int	usage = USAGE_WHATEVER;
	private int	algo = ALGO_RSA;

	private char[] storepass = null;
	private Vector<Identity> identities = new Vector<Identity>();
	
	private AsymmetricKeyPair akp ;
	
//	private AsymmetricCipherKeyPair keypair = null;
//	private RSAKeyParameters rpub = null;
//	private RSAPrivateCrtKeyParameters rpriv = null;
	
	
	private OSDXKeyObject() {
		
	}
//	public OSDXKeyObject(AsymmetricKeyPair akp) {
//		this.akp = akp;
//	}
	
	public static OSDXKeyObject fromElement(Element kp) throws Exception {
		OSDXKeyObject ret = new OSDXKeyObject();
		
		String Sshafp = kp.getChildText("sha1fingerprint");
		byte[] sha1fp = SecurityHelper.HexDecoder.decode(Sshafp);//sha1-checksum of moduluss
		String Smodulus = kp.getChildText("modulus");
		byte[] modulus = SecurityHelper.HexDecoder.decode(Smodulus);
		byte[] modsha1 = SecurityHelper.getSHA1(modulus);
		if(!Arrays.equals(modsha1, sha1fp)) {
			System.err.println("Uargsn. sha1fingerprint given does not match calculated sha1 for given modulus ("+sha1fp+"!="+modsha1+")");
			return null;
		}

		Vector<Element> ids = kp.getChildren("identities");
		for(int j=0;j<ids.size();j++) {
			Element id = ids.elementAt(j);
			//TODO
			Identity idd = Identity.fromElement(id);
			
			boolean ok = idd.validate(SecurityHelper.HexDecoder.decode(id.getChildText("sha1")));
			if(ok) {
				ret.identities.addElement(idd);
			}
			else {
				
			}
		} //identities
		
		
		
		String authoritativekeyserver = kp.getChildText("authoritativekeyserver");
		ret.authoritativekeyserver = authoritativekeyserver;
		
		String usage = kp.getChildText("usage");
		ret.usage = usage_name.indexOf(usage);
		
		String level = kp.getChildText("level");
		ret.level = level_name.indexOf(level);
		
		String parentkeyid = kp.getChildText("parentkeyid");
		ret.parentkeyid = parentkeyid;
		
		String Salgo = kp.getChildText("algo");
		int bits = kp.getChildInt("bits");
		ret.algo = algo_name.indexOf("Salgo");
		
		Element pubkey = kp.getChild("pubkey");
		String pubkey_exponentS = pubkey.getChildText("exponent");
		byte[] pubkey_exponent = SecurityHelper.HexDecoder.decode(pubkey_exponentS);
		
		Element privkey = kp.getChild("privkey");
		Element Eexponent = kp.getChild("exponent");
		
		byte[] exponent = null;
		if(Eexponent.getChild("locked") != null) {
			Element lk = Eexponent.getChild("locked");
			String mantraname = lk.getChildText("mantraname");
			String Slock_algo = lk.getChildText("algo");
			String Sinitv = lk.getChildText("initvector");
			String Spadding = lk.getChildText("padding");
			String Sbytes = lk.getChildText("bytes");
			byte[] bytes = SecurityHelper.HexDecoder.decode(Sbytes);
			
			try {
				String pp = null;
				System.out.print("!!!! ENSURE NOONE IS WATCHING YOUR SCREEN !!!! \n\nKeyID "+Sshafp+"@"+authoritativekeyserver+"\nPlease enter Passphrase for Mantra: \""+mantraname+"\": ");
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				pp = br.readLine();
			
				SymmetricKey sk = SymmetricKey.getKeyFromPass(pp.toCharArray(), SecurityHelper.HexDecoder.decode(Sinitv));
				exponent = sk.decrypt(bytes);
			} catch(Exception ex) {
				ex.printStackTrace();
			}				
		}
		else {
			//never should go here!!!
			System.err.println("You should never see me - there seems to be a private key unlocked in your keystore: "+Sshafp+"@"+authoritativekeyserver);
			exponent = SecurityHelper.HexDecoder.decode(Eexponent.getText());
		}
		
		AsymmetricKeyPair askp = new AsymmetricKeyPair(modulus, pubkey_exponent, exponent);
		
		String gpgkeyserverid = kp.getChildText("gpgkeyserverid");
		
		return ret;
	}
}

