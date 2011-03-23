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

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;

import org.fnppl.opensdx.gui.MessageHandler;
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
	public static final Vector<String> usage_name = new Vector<String>();
	static {
		usage_name.addElement("ONLYSIGN");
		usage_name.addElement("ONLYCRYPT");
		usage_name.addElement("BOTH");
	};
	public static final long ONE_YEAR = 31557600000L; //1 year = 31557600000 = 1000*60*60*24*365.25
	public static final int LEVEL_MASTER = 0;
	public static final int LEVEL_REVOKE = 1;
	public static final int LEVEL_SUB = 2;
	public static final Vector<String> level_name = new Vector<String>();
	static {
		level_name.addElement("MASTER");
		level_name.addElement("REVOKE");
		level_name.addElement("SUB");
	};
	
	private OSDXKeyObject parentosdxkeyobject = null;
	private String parentkeyid = null;//could be the parentkey is not loaded - then *only* the id is present
	
	private String authoritativekeyserver = null;
	//private String modulussha1 = null;
	private byte[] modulussha1 = null;
	private Vector<Identity> identities = new Vector<Identity>();
	private Vector<DataSourceStep> datapath = new Vector<DataSourceStep>();
	private String gpgkeyserverid = null;
	
	private int	level = LEVEL_MASTER;
	private int	usage = USAGE_WHATEVER;
	private int	algo = ALGO_RSA;
	private long validFrom = System.currentTimeMillis();
	private long validUntil = validFrom + 25L*ONE_YEAR; //25 years
	
	private char[] storepass = null;
	
	private AsymmetricKeyPair akp = null;
	private Element lockedPrivateKey = null;
	private boolean unsavedChanges = false;
	
//	private AsymmetricCipherKeyPair keypair = null;
//	private RSAKeyParameters rpub = null;
//	private RSAPrivateCrtKeyParameters rpriv = null;
	
	
	private OSDXKeyObject() {
		
	}
//	public OSDXKeyObject(AsymmetricKeyPair akp) {
//		this.akp = akp;
//	}
	
	public PublicKey getPubKey() {
		PublicKey ll = new PublicKey(
				new BigInteger(akp.getModulus()), 
				new BigInteger(akp.getPublicExponent())
			);
		return ll;
	}
	
	public static OSDXKeyObject buildNewMasterKeyfromKeyPair(AsymmetricKeyPair kp) throws Exception {

		OSDXKeyObject ret = new OSDXKeyObject();
		ret.akp = kp;
		ret.level = LEVEL_MASTER;
		ret.usage = USAGE_SIGN;
		ret.authoritativekeyserver = "LOCAL";
		ret.modulussha1 = SecurityHelper.getSHA1(kp.getModulus());
		ret.datapath = new Vector<DataSourceStep>();
		long now = System.currentTimeMillis();
		ret.validFrom = now;
		ret.validUntil = now + 25L*ONE_YEAR;
		ret.datapath.add(new DataSourceStep("LOCAL", now));
		ret.unsavedChanges = true;
		
		return ret;
	}
	
	
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
		ret.modulussha1 = modsha1;
		
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
					System.out.println("sha1: "+id.getChildText("sha1"));
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
		String sValidFrom = kp.getChildText("valid_from");
		String sValidUntil = kp.getChildText("valid_until");
		ret.validFrom = SecurityHelper.parseDate(sValidFrom);
		ret.validUntil = SecurityHelper.parseDate(sValidUntil);
		
		String usage = kp.getChildText("usage");
		ret.usage = usage_name.indexOf(usage);
		
		String level = kp.getChildText("level");
		ret.level = level_name.indexOf(level);
		
		String parentkeyid = kp.getChildText("parentkeyid");
		int iAt = parentkeyid.indexOf('@');
		if (iAt>0) {
			byte[] parentid = SecurityHelper.HexDecoder.decode(parentkeyid.substring(0,iAt));
			ret.parentkeyid = SecurityHelper.HexDecoder.encode(parentid,':',-1)+parentkeyid.substring(iAt);
		} else {
			byte[] parentid = SecurityHelper.HexDecoder.decode(parentkeyid);
			ret.parentkeyid = SecurityHelper.HexDecoder.encode(parentid,':',-1);
		}
		
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
		return getKeyModulusSHA1()+"@"+authoritativekeyserver;
	}
	
	public String getKeyModulusSHA1() {
		//return modulussha1;
		return SecurityHelper.HexDecoder.encode(modulussha1, ':', -1);
	}
	
	public byte[] getKeyModulusSHA1bytes() {
		return modulussha1;
	}
	
	public Element getSimplePubKeyElement() {
		if (akp!=null) {
			Element ret = new Element("pubkey");
			ret.addContent("keyid",getKeyID());
			ret.addContent("valid_from",getValidFromString());
			ret.addContent("valid_until",getValidUntilString());
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
	
	public byte[] sign(byte[] md5,byte[] sha1,byte[] sha256,long datetime) throws Exception {
		if (!isPrivateKeyUnlocked()) throw new RuntimeException("ERROR: Private Key is locked!");
		return akp.sign(
				md5, 
				sha1, 
				sha256, 
				datetime
			);
	}
	
	public String getIDEmails() {
		if (identities!=null && identities.size()>0) {
			String ids = identities.get(0).getEmail();
			for (int i=1;i<identities.size();i++) {
				ids += ", "+identities.get(i).getEmail();
			}
			return ids;
		}
		return null;
	}
	
	public boolean isPrivateKeyUnlocked() {
		return akp.hasPrivateKey(); 
	}
	
	public final void unlockPrivateKey(MessageHandler mh) {
		if (!akp.hasPrivateKey() && lockedPrivateKey != null) { //only once
			String mantraname = lockedPrivateKey.getChildText("mantraname");
			
			//check algo and padding
			String Slock_algo = lockedPrivateKey.getChildText("algo");
			String Spadding = lockedPrivateKey.getChildText("padding");
			if (!Slock_algo.equals("AES@256")||!Spadding.equals("CBC/PKCS#5")) {
				throw new RuntimeException("UNLOCKING METHOD NOT IMPLEMENTED, please use AES@265 encryption with CBC/PKCS#7 padding");
			}
			
			try {
				String pp = mh.requestPassword(getKeyID(), mantraname);
				unlockPrivateKey(pp);
			} catch(Exception ex) {
				if (ex.getMessage().startsWith("pad block corrupted")) {
					mh.fireWrongPasswordMessage();
				} else {
					ex.printStackTrace();
				}
			}
		}
	}
	
	public void createLockedPrivateKey(MessageHandler mh) throws Exception {
		if (akp.hasPrivateKey()) {
			String[] ans = mh.requestNewPasswordAndMantra("Saving Key: "+getKeyID()+"\nLevel: "+getLevelName()+"\n");
			if (ans != null) {
				byte[] iv = SecurityHelper.getRandomBytes(16);
				SymmetricKey sk = SymmetricKey.getKeyFromPass(ans[1].toCharArray(), iv);
				byte[] encprivkey = akp.getEncrytedPrivateKey(sk);
				Element el = new Element("locked");
				el.addContent("mantraname",ans[0]);
				el.addContent("algo","AES@256");
				el.addContent("initvector", SecurityHelper.HexDecoder.encode(iv, ':',-1));
				el.addContent("padding", "CBC/PKCS#5");
				el.addContent("bytes", SecurityHelper.HexDecoder.encode(encprivkey, ':',-1));				
				lockedPrivateKey = el;
			} else {
				System.out.println("CAUTION: private key NOT saved.");
			}
		}
	}
	
	public void unlockPrivateKey(String password) throws Exception{
		if (password!=null) {
			String Sinitv = lockedPrivateKey.getChildText("initvector");
			String Sbytes = lockedPrivateKey.getChildText("bytes");
			byte[] bytes = SecurityHelper.HexDecoder.decode(Sbytes);
			
			SymmetricKey sk = SymmetricKey.getKeyFromPass(password.toCharArray(), SecurityHelper.HexDecoder.decode(Sinitv));
			byte[] exponent = sk.decrypt(bytes);
			byte[] modulus = akp.getModulus();
			byte[] pubkey_exponent = akp.getPublicExponent();
			akp = new AsymmetricKeyPair(modulus, pubkey_exponent, exponent);
		}
	}
	
	public Element toElement(MessageHandler mh) throws Exception {
		Element ekp = new Element("keypair");
		//identities
		if (identities!=null && identities.size()>0) {
			Element eids = new Element("identities");
			for (Identity id : identities) {
				eids.addContent(id.toElement());
			}
			ekp.addContent(eids);
		}
		
		ekp.addContent("sha1fingerprint", getKeyModulusSHA1());
		ekp.addContent("authoritativekeyserver", authoritativekeyserver);
		
		//datapath
		Element edp = new Element("datapath");
		for (int i=0;i<datapath.size();i++) {
			Element edss = new Element("step"+(i+1));
			edss.addContent("datasource",datapath.get(i).getDataSource());
			edss.addContent("datainsertdatetime", datapath.get(i).getDataInsertDatetimeString());
			edp.addContent(edss);
		}
		ekp.addContent(edp);
		ekp.addContent("valid_from",getValidFromString());
		ekp.addContent("valid_until",getValidUntilString());
		ekp.addContent("usage",usage_name.get(usage));
		ekp.addContent("level",level_name.get(level));
		ekp.addContent("parentkeyid", getParentKeyID());
		ekp.addContent("algo",algo_name.get(algo));
		ekp.addContent("bits", ""+akp.getBitCount());
		ekp.addContent("modulus", SecurityHelper.HexDecoder.encode(akp.getModulus(), ':', -1));
		
		//pubkey
		Element epk = new Element("pubkey");
		epk.addContent("exponent", SecurityHelper.HexDecoder.encode(akp.getPublicExponent(), ':', -1));
		ekp.addContent(epk);
		
		//privkey
		if (lockedPrivateKey == null) {
			createLockedPrivateKey(mh);
		}
		
		if (lockedPrivateKey != null) {
			Element el = new Element("locked");
			el.addContent("mantraname",lockedPrivateKey.getChildText("mantraname"));
			el.addContent("algo",lockedPrivateKey.getChildText("algo"));
			el.addContent("initvector", lockedPrivateKey.getChildText("initvector"));
			el.addContent("padding",lockedPrivateKey.getChildText("padding"));
			el.addContent("bytes",lockedPrivateKey.getChildText("bytes"));
			
			Element eexp = new Element("exponent");
			eexp.addContent(el);
			Element esk = new Element("privkey");
			esk.addContent(eexp);
			ekp.addContent(esk);
		} else {
			System.out.println("CAUTION: private key NOT saved.");
		}// -- end privkey
		
		ekp.addContent("gpgkeyserverid", gpgkeyserverid);
		
		unsavedChanges = false;
		return ekp;
	}
	
	public Element toElementWithoutPrivateKey() throws Exception {
		Element ekp = new Element("keypair");
		//identities
		if (identities!=null && identities.size()>0) {
			Element eids = new Element("identities");
			for (Identity id : identities) {
				eids.addContent(id.toElement());
			}
			ekp.addContent(eids);
		}
		
		ekp.addContent("sha1fingerprint", getKeyModulusSHA1());
		ekp.addContent("authoritativekeyserver", authoritativekeyserver);
		
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
		ekp.addContent("parentkeyid", getParentKeyID());
		ekp.addContent("algo",algo_name.get(algo));
		ekp.addContent("bits", ""+akp.getBitCount());
		ekp.addContent("modulus", SecurityHelper.HexDecoder.encode(akp.getModulus(), ':', -1));
		
		//pubkey
		Element epk = new Element("pubkey");
		epk.addContent("exponent", SecurityHelper.HexDecoder.encode(akp.getPublicExponent(), ':', -1));
		ekp.addContent(epk);
		
		ekp.addContent("gpgkeyserverid", gpgkeyserverid);
		
		unsavedChanges = false;
		return ekp;
	}
	
	public String getValidFromString() {
		return SecurityHelper.getFormattedDate(validFrom);
	}
	
	public String getValidUntilString() {
		return SecurityHelper.getFormattedDate(validUntil);
	}
	
	public void setValidFrom(long datetime) {
		unsavedChanges = true;
		validFrom = datetime;
	}
	
	public long getValidFrom() {
		return validFrom;
	}
	
	public long getValidUntil() {
		return validUntil;
	}
	public void setValidUntil(long datetime) {
		unsavedChanges = true;
		validUntil = datetime;
	}
	
	public String getLevelName() {
		return level_name.get(level);
	}
	
	public void setLevel(int level) {
		unsavedChanges = true;
		this.level = level;
	}
	
	public String getUsageName() {
		return usage_name.get(usage);
	}
	
	public int getUsage() {
		return usage;
	}
	
	public void setUsage(int u) {
		unsavedChanges = true;
		usage = u;
	}
	
	public Vector<Identity> getIdentities() {
		return identities;
	}
	
	public Identity getIdentity0001() {
		for (Identity id : identities) {
			if (id.getIdentNum()==1) return id;
		}
		return null;
	}
	public Vector<DataSourceStep> getDatapath() {
		return datapath;
	}
	
	public String getAuthoritativekeyserver() {
		return authoritativekeyserver;
	}
	
	public void addIdentity(Identity id) {
		unsavedChanges = true;
		identities.add(id);
	}
	
	public void removeIdentity(Identity id) {
		unsavedChanges = true;
		identities.remove(id);
	}
	
	public void moveIdentityAtPositionUp(int oldPosition) {
		if (oldPosition>0 && oldPosition<identities.size()) {
			Identity id = identities.remove(oldPosition);
			identities.add(oldPosition-1, id);
			unsavedChanges = true;
		}
	}
	public void moveIdentityAtPositionDown(int oldPosition) {
		if (oldPosition>=0 && oldPosition<identities.size()-1) {
			Identity id = identities.remove(oldPosition);
			identities.add(oldPosition+1, id);
			unsavedChanges = true;
		}
	}
	
	public String getParentKeyID() {
		if (parentosdxkeyobject!=null) return parentosdxkeyobject.getKeyID();
		else return parentkeyid;
	}
	
	public void setParentKey(OSDXKeyObject parent) {
		unsavedChanges = true;
		parentosdxkeyobject = parent;
		parentkeyid = parent.getKeyID();
	}
	
	public void setParentKeyID(String id) {
		unsavedChanges = true;
		parentkeyid = id;
		parentosdxkeyobject = null;
	}
	
	
	public void setAuthoritativeKeyServer(String aks) {
		authoritativekeyserver = aks;
		unsavedChanges = true;
	}
	public boolean hasUnsavedChanges() {
		if (unsavedChanges) return true;
		else {
			for (Identity id : identities) {
				if (id.hasUnsavedChanges()) {
					System.out.println("unsaved changes in id: "+id.getEmail());
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isMaster() {
		if (level == LEVEL_MASTER) return true;
		else return false;
	}
	
	public boolean isRevoke() {
		if (level == LEVEL_REVOKE) return true;
		else return false;
	}
	
	public boolean isSub() {
		if (level == LEVEL_SUB) return true;
		else return false;
	}
	public boolean hasPrivateKey() {
		return lockedPrivateKey!=null || akp.hasPrivateKey();
	}
	
	
	public static void main(String[] args) throws Exception {
		String l = "2011-02-24 21:21:36 GMT+00:00";
		String l2 = "2011-02-24 15:00:00 GMT+01:00";		
//		System.out.println("1: "+SecurityHelper.datemeGMT.format(new Date()));
//		System.out.println("2: "+SecurityHelper.datemeGMT.parse(l));		
//		System.out.println("3: "+SecurityHelper.datemeGMT.parse(l2));
//		
//		System.out.println("4: "+SecurityHelper.datemeGMT.format(SecurityHelper.datemeGMT.parse(l)));
//		System.out.println("5: "+SecurityHelper.datemeGMT.format(SecurityHelper.datemeGMT.parse(l2)));
//		
//		long now = System.currentTimeMillis();
//		now = now - now%1000;
//		System.out.println("6: "+SecurityHelper.datemeGMT.format(now));		
//		System.out.println("7: "+SecurityHelper.datemeGMT.format(new Date(now)));		
//		
		
//		1: 2011-02-24 21:26:51 GMT+00:00
//		2: Thu Feb 24 22:21:36 CET 2011
//		3: Thu Feb 24 15:00:00 CET 2011
//		4: 2011-02-24 21:21:36 GMT+00:00
//		5: 2011-02-24 14:00:00 GMT+00:00

	}
}

