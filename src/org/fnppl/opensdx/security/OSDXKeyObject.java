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

import java.text.SimpleDateFormat;
import java.util.*;

import org.fnppl.opensdx.gui.Dialogs;
import org.fnppl.opensdx.xml.Element;


public class OSDXKeyObject {
	final static String RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";
	final static String RFC1123_CUT = "yyyy-MM-dd HH:mm:ss zzz";
	final static String RFC1036 = "EEEE, dd-MMM-yy HH:mm:ss zzz";
	final static String ASCTIME = "EEE MMM dd HH:mm:ss yyyy zzz";
	
	final static Locale ml = new Locale("en", "DE");
	static SimpleDateFormat datemeGMT = new SimpleDateFormat(RFC1123_CUT, ml);
	static {
		datemeGMT.setTimeZone(java.util.TimeZone.getTimeZone("GMT+00:00"));
	}
	
	
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
	private String modulussha1 = null;
	private Vector<Identity> identities = new Vector<Identity>();
	private Vector<DataSourceStep> datapath = new Vector<DataSourceStep>();
	private String gpgkeyserverid = null;
	
	private int	level = LEVEL_MASTER;
	private int	usage = USAGE_WHATEVER;
	private int	algo = ALGO_RSA;

	private char[] storepass = null;
	
	private AsymmetricKeyPair akp = null;
	private Element lockedPrivateKey = null;
	
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
		System.out.println("adding keyobject");
		
		//first check sha1fingerprint
		String Sshafp = kp.getChildText("sha1fingerprint");
		byte[] sha1fp = SecurityHelper.HexDecoder.decode(Sshafp);//sha1-checksum of moduluss
		String Smodulus = kp.getChildText("modulus");
		byte[] modulus = SecurityHelper.HexDecoder.decode(Smodulus);
		byte[] modsha1 = SecurityHelper.getSHA1(modulus);
		if(!Arrays.equals(modsha1, sha1fp)) {
			System.err.println("Uargsn. sha1fingerprint given does not match calculated sha1 for given modulus ("+sha1fp+"!="+modsha1+")");
			return null;
		}
		ret.modulussha1 = Sshafp;
		
		//fingerprint ok -> go on with identities
		Element ids = kp.getChild("identities");
		if (ids!=null) {
			Vector<Element> idc = ids.getChildren("identity");
			if (idc!=null) {
				System.out.println("identities found: "+idc.size());
				for(int j=0;j<idc.size();j++) {
					Element id = idc.elementAt(j);
					
					Identity idd = Identity.fromElement(id);
					System.out.println("adding id: "+idd.email);
					
					boolean ok = idd.validate(SecurityHelper.HexDecoder.decode(id.getChildText("sha1")));
					if(ok) {
						ret.identities.addElement(idd);
					} else {
						System.out.println(" -> ERROR adding "+idd.email+": SHA1 NOT VALID");
					}
				}
			}
		} //identities
		
		//go on with other fields
		
		String authoritativekeyserver = kp.getChildText("authoritativekeyserver");
		ret.authoritativekeyserver = authoritativekeyserver;
		System.out.println("authoritativekeyserver: "+authoritativekeyserver);
		
		
		//datapath
		Element dp = kp.getChild("datapath");
		boolean dsOK = false;
		if (dp!=null) {
			ret.datapath = new Vector<DataSourceStep>();
			Vector<Element> steps = dp.getChildren();
			for (Element st : steps)
			if (st.getName().startsWith("step")) {
				DataSourceStep dst = DataSourceStep.fromElemet(st);
				ret.datapath.add(dst);
				dsOK = true;
			}
		}
		if (!dsOK) {
			System.out.println("CAUTION datasource and datainsertdatetime NOT found.");
		}
		
		
		String usage = kp.getChildText("usage");
		ret.usage = usage_name.indexOf(usage);
		
		String level = kp.getChildText("level");
		ret.level = level_name.indexOf(level);
		
		String parentkeyid = kp.getChildText("parentkeyid");
		ret.parentkeyid = parentkeyid;
		
		String Salgo = kp.getChildText("algo");
		int bits = kp.getChildInt("bits");
		ret.algo = algo_name.indexOf(Salgo);
		
		
		//add asymetric keypair or public key part only
		Element pubkey = kp.getChild("pubkey");
		String pubkey_exponentS = pubkey.getChildText("exponent");
		byte[] pubkey_exponent = SecurityHelper.HexDecoder.decode(pubkey_exponentS);
		byte[] exponent = null;
		
		Element privkey = kp.getChild("privkey");
		
		if (privkey!=null) {
			//asymetric keypair
			Element Eexponent = privkey.getChild("exponent");
			if(Eexponent.getChild("locked") != null) {
				//only ask for password when key is used for the first time -> see unlockPrivateKey
				ret.lockedPrivateKey = Eexponent.getChild("locked");
				
//				Element lk = Eexponent.getChild("locked");
//				String mantraname = lk.getChildText("mantraname");
//				String Sinitv = lk.getChildText("initvector");
//				String Sbytes = lk.getChildText("bytes");
//				
//				//check algo and padding
//				String Slock_algo = lk.getChildText("algo");
//				String Spadding = lk.getChildText("padding");
//				if (!Slock_algo.equals("AES@256")||!Spadding.equals("CBC/PKCS#5")) {
//					throw new RuntimeException("UNLOCKING METHOD NOT IMPLEMENTED, please use AES@265 encryption with CBC/PKCS#5 padding");
//				}
//				
//				byte[] bytes = SecurityHelper.HexDecoder.decode(Sbytes);
//				
//				try {
//					String pp = null;
//					System.out.print("!!!! ENSURE NOONE IS WATCHING YOUR SCREEN !!!! \n\nKeyID "+Sshafp+"@"+authoritativekeyserver+"\nPlease enter Passphrase for Mantra: \""+mantraname+"\": ");
//					BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//					pp = br.readLine();
//				
//					SymmetricKey sk = SymmetricKey.getKeyFromPass(pp.toCharArray(), SecurityHelper.HexDecoder.decode(Sinitv));
//					
//					exponent = sk.decrypt(bytes);
//				} catch(Exception ex) {
//					ex.printStackTrace();
//				}				
			} else {
				//never should go here!!!
				System.err.println("You should never see me - there seems to be a private key unlocked in your keystore: "+Sshafp+"@"+authoritativekeyserver);
				exponent = SecurityHelper.HexDecoder.decode(Eexponent.getText());
			}
		}
		//exponent == null if no private key present or private key is locked
		AsymmetricKeyPair askp = new AsymmetricKeyPair(modulus, pubkey_exponent, exponent);
		ret.akp = askp;
		
		//go on
		
		ret.gpgkeyserverid = kp.getChildText("gpgkeyserverid");
		
		return ret;
	}//fromElement
	
	public boolean allowsSigning() {
		//double check: signing not possible without private key
		if (akp.hasPrivateKey() || lockedPrivateKey != null) {
			return usage == USAGE_SIGN || usage == USAGE_WHATEVER;
		}
		return false;
	}
	public String getKeyID() {
		return modulussha1+"@"+authoritativekeyserver;
	}
	
	
	public Element getSimplePubKeyElement() {
		if (akp!=null) {
			Element ret = new Element("pubkey");
			ret.addContent("algo", algo_name.elementAt(algo));
			ret.addContent("bits", ""+akp.getBitCount());
			ret.addContent("modulus", akp.getModulusAsHex());
			ret.addContent("exponent", akp.getPublicExponentAsHex());
			return ret;
		}
		
//		<bits>3072</bits><!-- well, yes, count yourself, but nice to *see* it -->
//		<modulus></modulus><!-- as hex-string with or without leading 0x ; only for RSA?! -->
//		<exponent></exponent><!-- as hex-string with or without leading 0x -->
//		</pubkey><!-- given, but should be verified from server/yourself... -->
//
		return null;
	}
	
	public byte[] signSHA1(byte[] sha1) throws Exception {
		unlockPrivateKey();
		return akp.sign(sha1);
	}
	
	private final void unlockPrivateKey() {
		if (!akp.hasPrivateKey() && lockedPrivateKey != null) { //only once
			String mantraname = lockedPrivateKey.getChildText("mantraname");
			String Sinitv = lockedPrivateKey.getChildText("initvector");
			String Sbytes = lockedPrivateKey.getChildText("bytes");
			
			//check algo and padding
			String Slock_algo = lockedPrivateKey.getChildText("algo");
			String Spadding = lockedPrivateKey.getChildText("padding");
			if (!Slock_algo.equals("AES@256")||!Spadding.equals("CBC/PKCS#5")) {
				throw new RuntimeException("UNLOCKING METHOD NOT IMPLEMENTED, please use AES@265 encryption with CBC/PKCS#5 padding");
			}
			
			byte[] bytes = SecurityHelper.HexDecoder.decode(Sbytes);

			try {
				//String pp = null;
				//System.out.print("!!!! ENSURE NOONE IS WATCHING YOUR SCREEN !!!! \n\nKeyID "+modulussha1+"@"+authoritativekeyserver+"\nPlease enter Passphrase for Mantra: \""+mantraname+"\": ");
				//BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				//pp = br.readLine();
				String pp = Dialogs.showPasswordDialog("UNLOCK PRIVATE KEY", "KeyID: "+modulussha1+"@"+authoritativekeyserver+"\nPlease enter passphrase for mantra: \""+mantraname+"\"");
				//System.out.println(pp);
				if (pp!=null) {
					SymmetricKey sk = SymmetricKey.getKeyFromPass(pp.toCharArray(), SecurityHelper.HexDecoder.decode(Sinitv));
					byte[] exponent = sk.decrypt(bytes);
					byte[] modulus = akp.getModulus();
					byte[] pubkey_exponent = akp.getPublicExponent();
					akp = new AsymmetricKeyPair(modulus, pubkey_exponent, exponent);
				}
			} catch(Exception ex) {
				ex.printStackTrace();
			}			
		}
	}
	
	public Element toElement() throws Exception {
		Element e = new Element("key");
		Element ekp = new Element("keypair");
		e.addContent(ekp);
		//identities
		if (identities!=null && identities.size()>0) {
			Element eids = new Element("identities");
			for (Identity id : identities) {
				eids.addContent(id.toElement());
			}
		}
		ekp.addContent("sha1fingerprint",modulussha1);
		ekp.addContent("authoritativekeyserver",authoritativekeyserver);
		
		//datapath
		Element edp = new Element("datapath");
		for (int i=0;i<datapath.size();i++) {
			Element edss = new Element("step"+(i+1));
			edss.addContent("datasource",datapath.get(i).getDataSource());
			edss.addContent("datainsertdatetime", datapath.get(i).getDataInsertDatetimeString());
			edp.addContent(edss);
		}
		ekp.addContent(edp);
		
		ekp.addContent("usage",usage_name.get(usage));
		ekp.addContent("level",level_name.get(level));
		ekp.addContent("parentkeyid",parentkeyid);
		ekp.addContent("algo",algo_name.get(algo));
		ekp.addContent("bits",""+akp.getBitCount());
		ekp.addContent("modulus",akp.getModulusAsHex());
		
		//pubkey
		Element epk = new Element("pubkey");
		epk.addContent("exponent",akp.getPublicExponentAsHex());
		ekp.addContent(epk);
		
		//privkey
		if (lockedPrivateKey!=null) {
			
			Element el = new Element("locked");
			el.addContent("mantraname",lockedPrivateKey.getChildText("mantraname"));
			el.addContent("algo",lockedPrivateKey.getChildText("algo"));
			el.addContent("initvector",lockedPrivateKey.getChildText("initvector"));
			el.addContent("padding",lockedPrivateKey.getChildText("padding"));
			el.addContent("bytes",lockedPrivateKey.getChildText("bytes"));
			
			Element eexp = new Element("exponent");
			eexp.addContent(el);
			Element esk = new Element("privkey");
			esk.addContent(eexp);
			ekp.addContent(esk);
			
		} else if (akp.hasPrivateKey()) {
			String[] ans = Dialogs.showNewMantraPasswordDialog();
			if (ans!=null) {
				byte[] iv = SecurityHelper.getRandomBytes(16);
				SymmetricKey sk = SymmetricKey.getKeyFromPass(ans[1].toCharArray(), iv);
				byte[] encprivkey = akp.getEncrytedPrivateKey(sk);
				Element el = new Element("locked");
				el.addContent("mantraname",ans[0]);
				el.addContent("algo","AES@256");
				el.addContent("initvector",SecurityHelper.HexDecoder.encode(iv, '\0',-1));
				el.addContent("padding","CBC/PKCS#5");
				el.addContent("bytes",SecurityHelper.HexDecoder.encode(encprivkey, '\0',-1));
				
				Element eexp = new Element("exponent");
				eexp.addContent(el);
				Element esk = new Element("privkey");
				esk.addContent(eexp);
				ekp.addContent(esk);
			} else {
				System.out.println("CAUTION: private key NOT saved.");
			}
		}// -- end privkey
		
		ekp.addContent("gpgkeyserverid", gpgkeyserverid);
		
		return e;
	}
	
	public static void main(String[] args) throws Exception {
		String l = "2011-02-24 21:21:36 GMT+00:00";
		String l2 = "2011-02-24 15:00:00 GMT+01:00";		
		System.out.println("1: "+datemeGMT.format(new Date()));
		System.out.println("2: "+datemeGMT.parse(l));		
		System.out.println("3: "+datemeGMT.parse(l2));
		
		System.out.println("4: "+datemeGMT.format(datemeGMT.parse(l)));		
		System.out.println("5: "+datemeGMT.format(datemeGMT.parse(l2)));
		
//		1: 2011-02-24 21:26:51 GMT+00:00
//		2: Thu Feb 24 22:21:36 CET 2011
//		3: Thu Feb 24 15:00:00 CET 2011
//		4: 2011-02-24 21:21:36 GMT+00:00
//		5: 2011-02-24 14:00:00 GMT+00:00

	}
}

