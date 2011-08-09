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

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;

import org.fnppl.opensdx.gui.MessageHandler;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;


public class OSDXKey {
		
	public static final int ALGO_RSA = 0;
	
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
	public static final int LEVEL_UNKNOWN = 3;
	
	public static final Vector<String> level_name = new Vector<String>();
	static {
		level_name.addElement("MASTER");
		level_name.addElement("REVOKE");
		level_name.addElement("SUB");
		level_name.addElement("UNKNOWN");
	};
	
	

	private int	level = LEVEL_MASTER;
	protected int	usage = USAGE_WHATEVER;
	protected int	algo = ALGO_RSA;
	protected long validFrom = Long.MIN_VALUE;
	protected long validUntil = Long.MAX_VALUE;
	
	private String usage_restriction = null;
	private String usage_note = null;
	
	
	protected String authoritativekeyserver = null;
	//protected int authoritativekeyserverPort = 8889;
	protected byte[] modulussha1 = null;

	protected String gpgkeyserverid = null;
	protected Vector<DataSourceStep> datapath = new Vector<DataSourceStep>();
	
	protected char[] storepass = null;
	
	protected AsymmetricKeyPair akp = null;
	protected Element lockedPrivateKey = null;
	protected boolean unsavedChanges = false;
	
	protected OSDXKey() {
		validFrom = System.currentTimeMillis();
		validFrom = validFrom - validFrom%1000; //no milliseconds in datemeGMT format;
		validUntil = validFrom + 25L*ONE_YEAR; //25 years
	}
	
	public PublicKey getPubKey() {
		PublicKey ll = new PublicKey(
				new BigInteger(akp.getModulus()), 
				new BigInteger(akp.getPublicExponent())
			);
		return ll;
	}
	
	public boolean verify(byte[] signature, byte[] md5, byte[] sha1, byte[] sha256,	long timestamp) throws Exception {
		return getPubKey().verify(signature, md5, sha1, sha256, timestamp);
	}
	
	public byte[] getPublicModulusBytes() {
		return akp.getModulus();
	}
	
	public byte[] encrypt(byte[] bytes) {
		try {
			return akp.encryptWithPublicKey(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public byte[] decrypt(byte[] bytes) {
		try {
			return akp.decryptWithPrivateKey(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static OSDXKey fromPubKeyElement(Element e) throws Exception {
		OSDXKey ret = null;
		String levelName = e.getChildText("level");
		int level = level_name.indexOf(levelName);
		if (level == LEVEL_MASTER) {
			ret = new MasterKey();
		}
		else if (level == LEVEL_SUB) {
			ret  = new SubKey();
		}
		else if (level == LEVEL_REVOKE) {
			ret  = new RevokeKey();
		}
		else {
			ret = new OSDXKey();
		}
		ret.level = level;
		
		ret.usage = USAGE_WHATEVER;
		String usageName = e.getChildText("usage");
		if (usageName!=null && !usageName.equals("")) {
			int usage = usage_name.indexOf(usageName);
			if (usage>=0) ret.usage = usage;
		}
		ret.usage_restriction = e.getChildText("usage_restriction");
		ret.usage_note = e.getChildText("usage_note");
	
		String keyid = e.getChildText("keyid");
		String authServer = e.getChildText("authoritativekeyserver");
		if (authServer!=null && !authServer.equals("")) {
			ret.authoritativekeyserver = authServer;
		} else {
			if (keyid.indexOf('@')>0) {
				ret.authoritativekeyserver = keyid.substring(keyid.indexOf('@')+1);	
			}
		}
		//int port = e.getChildInt("authoritativekeyserver_port");
		//if (port > 0) ret.authoritativekeyserverPort = port;
		
		
		ret.datapath = new Vector<DataSourceStep>();
		ret.validFrom = SecurityHelper.parseDate(e.getChildText("valid_from"));
		ret.validUntil = SecurityHelper.parseDate(e.getChildText("valid_until"));
		ret.unsavedChanges = true;
		
		String Salgo = e.getChildText("algo");
		ret.algo = algo_name.indexOf(Salgo);
		
		byte[] modulus = SecurityHelper.HexDecoder.decode(e.getChildText("modulus"));
		byte[] pubkey_exponent = SecurityHelper.HexDecoder.decode(e.getChildText("exponent"));
		byte[] exponent = null;
		
		AsymmetricKeyPair askp = new AsymmetricKeyPair(modulus, pubkey_exponent, exponent);
		ret.akp = askp;
		ret.modulussha1 = SecurityHelper.getSHA1(askp.getModulus());
		
		return ret;
	}//fromElement
	
	
	public static OSDXKey fromElement(Element kp) throws Exception {
		
		OSDXKey ret = null;
		String levelName = kp.getChildText("level");
		int level = level_name.indexOf(levelName);
		if (level == LEVEL_MASTER) {
			ret = new MasterKey();
		}
		else if (level == LEVEL_SUB) {
			ret  = new SubKey();
		}
		else if (level == LEVEL_REVOKE) {
			ret  = new RevokeKey();
		}
		else {
			ret = new OSDXKey();
		}
		ret.level = level;
		
		
		
		//System.out.println("adding keyobject");
		
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
		if (level == LEVEL_MASTER) {
			Element ids = kp.getChild("identities");
			if (ids!=null) {
				Vector<Element> idc = ids.getChildren("identity");
				if (idc!=null) {
					//System.out.println("identities found: "+idc.size());
					for(int j=0;j<idc.size();j++) {
						Element elementID = idc.elementAt(j);
						
						Identity id = Identity.fromElement(elementID);
						//System.out.println("adding id: "+idd.email);
						//System.out.println("sha1: "+id.getChildText("sha1"));
						boolean ok = id.validate(SecurityHelper.HexDecoder.decode(elementID.getChildText("sha256")));
						if(ok) {
							((MasterKey)ret).identities.addElement(id);
						} else {
							System.out.println(" -> ERROR adding "+id.getIdentNumString()+" "+id.getEmail()+": SHA256 NOT VALID");
							return null;
						}
					}
				}
			} //identities
		}
		//go on with other fields
		
		String authoritativekeyserver = kp.getChildText("authoritativekeyserver");
		ret.authoritativekeyserver = authoritativekeyserver;
		//int port = kp.getChildInt("authoritativekeyserver_port");
		//if (port > 0) ret.authoritativekeyserverPort = port;
		
		//System.out.println("authoritativekeyserver: "+authoritativekeyserver);
		
		
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
			//System.out.println("CAUTION datasource and datainsertdatetime NOT found.");
		}
		String sValidFrom = kp.getChildText("valid_from");
		String sValidUntil = kp.getChildText("valid_until");
		ret.validFrom = SecurityHelper.parseDate(sValidFrom);
		ret.validUntil = SecurityHelper.parseDate(sValidUntil);
		
		String usage = kp.getChildText("usage");
		ret.usage = usage_name.indexOf(usage);
		
		ret.usage_restriction = kp.getChildText("usage_restriction");
		ret.usage_note = kp.getChildText("usage_note");
		
		if (level == LEVEL_SUB || level == LEVEL_REVOKE) {
			String parentkeyid = kp.getChildText("parentkeyid");
			int iAt = parentkeyid.indexOf('@');
			if (iAt>0) {
				byte[] parentid = SecurityHelper.HexDecoder.decode(parentkeyid.substring(0,iAt));
				((SubKey)ret).parentkeyid = SecurityHelper.HexDecoder.encode(parentid,':',-1)+parentkeyid.substring(iAt);
			} else {
				byte[] parentid = SecurityHelper.HexDecoder.decode(parentkeyid);
				((SubKey)ret).parentkeyid = SecurityHelper.HexDecoder.encode(parentid,':',-1);
			}
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
			Element eExponent = privkey.getChild("exponent");
			if(eExponent.getChild("locked") != null) {
				//only ask for password when key is used for the first time -> see unlockPrivateKey
				ret.lockedPrivateKey = eExponent.getChild("locked");		
			} else {
				//never should go here!!!
				System.err.println("You should never see me - there seems to be a private key unlocked in your keystore: "+Sshafp+"@"+authoritativekeyserver);
				exponent = SecurityHelper.HexDecoder.decode(eExponent.getText());
			}
		}
		//exponent == null if no private key present or private key is locked
		AsymmetricKeyPair askp = new AsymmetricKeyPair(modulus, pubkey_exponent, exponent);
		ret.akp = askp;
		
		//go on
		
		ret.gpgkeyserverid = kp.getChildText("gpgkeyserverid");
		ret.unsavedChanges = false;
		return ret;
	}//fromElement
	
	
	public String getUsageRestriction() {
		return usage_restriction;
	}
	
	public void setUsageRestricton(String value) {
		unsavedChanges = true;
		usage_restriction = value;
	}
	
	public String getUsageNote() {
		return usage_note;
	}
	
	public void setUsageNote(String value) {
		unsavedChanges = true;
		usage_note = value;
	}
	
	public boolean allowsSigning() {
		//double check: signing not possible without private key
		if (akp.hasPrivateKey() || lockedPrivateKey != null) {
			return usage == USAGE_SIGN || usage == USAGE_WHATEVER;
		}
		return false;
	}
	
	public boolean allowsSignatures() {
		return usage == USAGE_SIGN || usage == USAGE_WHATEVER;
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
			ret.addContent("level",getLevelName());
			ret.addContent("usage",getUsageName());
			if (getUsageRestriction()!=null) {
				ret.addContent("usage_restriction",getUsageRestriction());
			}
			if (getUsageNote()!=null) {
				ret.addContent("usage_note",getUsageNote());
			}
			//ret.addContent("authoritativekeyserver",authoritativekeyserver);
			//ret.addContent("authoritativekeyserver_port",""+authoritativekeyserverPort);
			ret.addContent("valid_from",getValidFromString());
			ret.addContent("valid_until",getValidUntilString());
			ret.addContent("algo", algo_name.elementAt(algo));
			ret.addContent("bits", ""+akp.getBitCount());
			ret.addContent("modulus", akp.getModulusAsHex());
			ret.addContent("exponent", akp.getPublicExponentAsHex());
			return ret;
		}
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
			String[] ans = mh.requestNewPasswordAndMantra("Saving Key: "+getKeyID()+"\n\nLevel: "+getLevelName()+"\n\n");
			if (ans != null) {
				createLockedPrivateKey(ans[0],ans[1]);
			} else {
				System.out.println("CAUTION: private key NOT saved.");
			}
		}
	}
	
	public void createLockedPrivateKey(String mantra, String password) throws Exception {
		if (akp.hasPrivateKey()) {
			byte[] iv = SecurityHelper.getRandomBytes(16);
			SymmetricKey sk = SymmetricKey.getKeyFromPass(password.toCharArray(), iv);
			byte[] encprivkey = akp.getEncrytedPrivateKey(sk);
			Element el = new Element("locked");
			el.addContent("mantraname",mantra);
			el.addContent("algo","AES@256");
			el.addContent("initvector", SecurityHelper.HexDecoder.encode(iv, ':',-1));
			el.addContent("padding", "CBC/PKCS#5");
			el.addContent("bytes", SecurityHelper.HexDecoder.encode(encprivkey, ':',-1));				
			lockedPrivateKey = el;
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
		return toElement(mh, true);
	}
	
	public Element toElementWithoutPrivateKey() throws Exception {
		return toElement(null, false);
	}
	
	private Element toElement(MessageHandler mh, boolean withPrivateKey) throws Exception {
		Element ekp = new Element("keypair");
		if (this instanceof MasterKey) {
			MasterKey mk = (MasterKey)this;
			//identities
			if (mk.identities!=null && mk.identities.size()>0) {
				Element eids = new Element("identities");
				for (Identity id : mk.identities) {
					eids.addContent(id.toElement(true));
				}
				ekp.addContent(eids);
			}
		}
		
		ekp.addContent("sha1fingerprint", getKeyModulusSHA1());
		ekp.addContent("authoritativekeyserver", authoritativekeyserver);
		//ekp.addContent("authoritativekeyserver_port", ""+authoritativekeyserverPort);
		
		
		//datapath
		Element edp = new Element("datapath");
		for (int i=0;i<datapath.size();i++) {
			edp.addContent(datapath.get(i).toElement(i));
		}
		ekp.addContent(edp);
		ekp.addContent("valid_from",getValidFromString());
		ekp.addContent("valid_until",getValidUntilString());
		ekp.addContent("usage",usage_name.get(usage));
		if (usage_restriction!=null) {
			ekp.addContent("usage_restriction",usage_restriction);
		}
		if (usage_note!=null) {
			ekp.addContent("usage_note",usage_note);
		}
		ekp.addContent("level",level_name.get(level));
		if (this instanceof SubKey) {
			ekp.addContent("parentkeyid", ((SubKey)this).getParentKeyID());
		} else {
			ekp.addContent("parentkeyid", "");
		}
		ekp.addContent("algo",algo_name.get(algo));
		ekp.addContent("bits", ""+akp.getBitCount());
		ekp.addContent("modulus", SecurityHelper.HexDecoder.encode(akp.getModulus(), ':', -1));
		
		//pubkey
		Element epk = new Element("pubkey");
		epk.addContent("exponent", SecurityHelper.HexDecoder.encode(akp.getPublicExponent(), ':', -1));
		ekp.addContent(epk);
		
		if (withPrivateKey) {
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
			} else if (akp.hasPrivateKey()) {
				System.out.println("CAUTION: private key NOT saved.");
			}// -- end privkey
		}
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
		validFrom = validFrom - validFrom%1000; //no milliseconds in datemeGMT format;
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
	
	public int getLevel() {
		return level;
	}
	public String getLevelName() {
		if (level>=0) {
			return level_name.get(level);
		}
		return "NOT SET";
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
	
	public Vector<DataSourceStep> getDatapath() {
		return datapath;
	}
	
	public String getAuthoritativekeyserver() {
		return authoritativekeyserver;
	}
//	public int getAuthoritativekeyserverPort() {
//		return authoritativekeyserverPort;
//	}
	
	public void addDataSourceStep(DataSourceStep ds) {
		unsavedChanges = true;
		if (datapath == null) datapath = new Vector<DataSourceStep>();
		datapath.add(ds);
	}
	
//	private void setAuthoritativeKeyServer(String aks) {
//		authoritativekeyserver = aks;
//		unsavedChanges = true;
//	}
	
	public void setUnsavedChanges(boolean b) {
		unsavedChanges = b;
	}
	
	public boolean hasUnsavedChanges() {
		if (unsavedChanges) return true;
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
	
	public String getJavaCodeString() {
		try {
			OutputStream out = new OutputStream() {
				private StringBuilder string = new StringBuilder();
				public void write(int b) throws IOException {
					this.string.append((char) b );
				}
				public String toString(){
					return string.toString();
				}
			};
			Document.buildDocument(toElement(null)).outputCompact(out);
			StringBuffer s  = new StringBuffer();
			s.append("OSDXKey key = OSDXKey.fromElement(Document.fromString(\"");
			s.append(out.toString().replace('\n',' ').replace('\r',' ').replace("\"", "\\\""));
			s.append("\").getRootElement());\n");
			return s.toString();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public static String getFormattedKeyIDModulusOnly(String id) {
		if (id.charAt(4)==':') {//starts with idnum e.g. 0001:
			id = id.substring(4);
		}
		int iat = id.indexOf('@');
		if (iat>0) {
			return SecurityHelper.HexDecoder.encode(SecurityHelper.HexDecoder.decode(id.substring(0,iat)), ':', -1);
		} else {
			return SecurityHelper.HexDecoder.encode(SecurityHelper.HexDecoder.decode(id), ':', -1);
		}
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

