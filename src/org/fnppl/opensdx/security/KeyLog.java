package org.fnppl.opensdx.security;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Vector;

import javax.print.Doc;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.fnppl.opensdx.gui.Dialogs;
import org.fnppl.opensdx.security.SecurityHelper.HexDecoder;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;
import org.fnppl.opensdx.xml.XMLHelper;

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

public class KeyLog {

	public static String APPROVAL = "approval"; 
	public static String APPROVAL_PENDING = "approval_pending";
	public static String DISAPPROVAL = "disapproval";
	public static String REVOCATION = "revocation";
	private static String[] checkForAction = new String[] {APPROVAL, APPROVAL_PENDING, DISAPPROVAL, REVOCATION};
	
	private String ipv4 = null;
	private String ipv6 = null;
	private KeyLogAction action;
	private byte[] sha256localproof = null;
	private Signature signature = null;
	private Vector<DataSourceStep> datapath = null;
	
	//private Element ekeylog;
	private boolean verified = false;
	
	private KeyLog() {
		
	}
	
	
	public static KeyLog buildNewKeyLog(KeyLogAction keylogAction, String ip4, String ip6, OSDXKey signingKey) throws Exception {
		KeyLog kl = new KeyLog();
		kl.ipv4 = ip4;
		kl.ipv6 = ip6;
		kl.action = keylogAction;
		
		
		return kl;
	}
	
	public static KeyLog buildNewKeyLog(String action, OSDXKey from, String toKeyID, String ip4, String ip6, Identity id) throws Exception {
		KeyLog kl = new KeyLog();
		kl.action = action;
		kl.datetime = System.currentTimeMillis();
		kl.ipv4 = ip4;
		kl.ipv6 = ip6;
		kl.fromKeyid = from.getKeyID();
		kl.toKeyid = toKeyID;
		kl.id = id;
		kl.datapath = new Vector<DataSourceStep>();
		kl.signoffAction(from);
		kl.sha256localproof = null;
		kl.signature = null;
			
		return kl;
	}
	
	public Result uploadToKeyServer(String host, int port, String prepath, OSDXKey signingKey, KeyVerificator keyverificator) {
		try {
			KeyClient client =  new KeyClient(host, port, prepath, keyverificator);
			boolean ok = client.putKeyLog(this, signingKey);
			if (ok) return Result.succeeded();
			else Result.error(client.getMessage());
		} catch (Exception ex) {
			return Result.error(ex);
		}
		return Result.error("unknown error");
	}
	
	public static KeyLog buildNewRevocationKeyLog(String fromKeyID, String toKeyID, String message, byte[] actionproof, Signature actionSignature, String ip4, String ip6, OSDXKey serverSignoffKey) throws Exception {
		KeyLog kl = new KeyLog();
		kl.action = KeyLog.REVOCATION;
		kl.datetime = System.currentTimeMillis();
		kl.ipv4 = ip4;
		kl.ipv6 = ip6;
		kl.fromKeyid = fromKeyID;
		kl.toKeyid = toKeyID;
		kl.id = null;
		kl.message = message;
		kl.datapath = new Vector<DataSourceStep>();
		kl.actionSha256localproof = actionproof;
		kl.actionSignature = actionSignature;
		kl.signoffKeyServer(serverSignoffKey);
		
		return kl;
	}

//	public Vector<String[]> getStatusElements() {
//		Vector<String[]> v = new Vector<String[]>();
//		if (message!=null) {
//			v.add(new String[]{"message", message});
//		}
//		if (id!=null) {
//			v.add(new String[]{"identnum", id.getIdentNumString()});
//			v.add(new String[]{"email", id.getEmail()});
//			v.add(new String[]{"mnemonic", id.getMnemonic()});
//			
//			v.add(new String[]{"country", id.getCountry()});
//			v.add(new String[]{"region", id.getRegion()});
//			v.add(new String[]{"city", id.getCity()});
//			v.add(new String[]{"postcode", id.getPostcode()});
//			
//			v.add(new String[]{"company", id.getCompany()});
//			v.add(new String[]{"unit", id.getUnit()});
//			v.add(new String[]{"subunit", id.getSubunit()});
//			v.add(new String[]{"function", id.getFunction()});
//			
//			v.add(new String[]{"surname", id.getSurname()});
//			v.add(new String[]{"middlename", id.getMiddlename()});
//			v.add(new String[]{"name", id.getFirstNames()});
//			
//			v.add(new String[]{"phone", id.getPhone()});
//			v.add(new String[]{"note", id.getNote()});
//		}
//		return v;
//	}

	public Result verify() throws Exception {
		Result v = verifyActionSHA256localproofAndSignoff();
		if (v.succeeded && signature!=null) {
			v = verifyKeyServerSHA256localproofAndSignoff();
		}
		return v;
	}
	
	public Result verifyActionSHA256localproofAndSignoff() throws Exception {
		if (actionSignature==null) return  Result.error("missing action signature");
		if (actionSha256localproof == null) return  Result.error("missing action localproof");
		
		//check localproof
		byte[] bsha256 = calcActionSha256LocalProof();
		if (!Arrays.equals(bsha256, actionSha256localproof)) {
			System.out.println("sha256localproof target: "+SecurityHelper.HexDecoder.encode(actionSha256localproof, '\0', -1));
			System.out.println("sha256localproof real  : "+SecurityHelper.HexDecoder.encode(bsha256, '\0', -1));
			return Result.error("verification of sha1localproof failed");
		}	
		//check signoff
		return actionSignature.tryVerificationMD5SHA1SHA256(new ByteArrayInputStream(bsha256));
	}
	public Result verifyKeyServerSHA256localproofAndSignoff() throws Exception {
		if (signature==null) return  Result.error("missing signature");
		if (sha256localproof == null) return  Result.error("missing keyserver localproof");
		//check localproof
		byte[] bsha256 = calcKeyServerSha256LocalProof();
		if (!Arrays.equals(bsha256, sha256localproof)) {
			System.out.println("sha256localproof target: "+SecurityHelper.HexDecoder.encode(sha256localproof, '\0', -1));
			System.out.println("sha256localproof real  : "+SecurityHelper.HexDecoder.encode(bsha256, '\0', -1));
			return Result.error("verification of sha256localproof failed");
		}
		//check signoff
		return signature.tryVerificationMD5SHA1SHA256(new ByteArrayInputStream(bsha256));
	}
	
	public byte[] calcSha256LocalProof() throws Exception {
		//localproof without datapath!
		byte[] ret = new byte[32];  //256 bit = 32 byte
		SHA256Digest sha256 = new org.bouncycastle.crypto.digests.SHA256Digest();
		
		byte[] data = ipv4.getBytes("UTF-8");
		if (ipv4!=null && ipv4.length()>0) {
			sha256.update(data, 0,data.length);
		}
		sha256.doFinal(ret, 16+20);
		
		//updateSha1(getDateString(), sha1);
//		updateSha1(ipv4, sha1);
//		updateSha1(ipv6, sha1);
//		
//		updateSha1(fromKeyid, sha1);
//		updateSha1(toKeyid, sha1);
//		if (action.equals(REVOCATION)) {
//			updateSha1(message, sha1);
//		} else {
//			updateSha1(action, sha1);
//			Vector<String[]> idFields = getStatusElements();
//			for (String[] f : idFields) {
//				updateSha1(f[1], sha1);
//			}
//		}
//		sha1.doFinal(ret, 0);
//		//System.out.println("--- end ---\n");
//		//System.out.println("calc sha1: "+SecurityHelper.HexDecoder.encode(ret, ':', -1));
//		return ret;
	}
	
	private void updateSha1(String field, org.bouncycastle.crypto.digests.SHA1Digest sha1) throws Exception {
		if (field==null || field.length()==0) return;
		byte[] k = field.getBytes("UTF-8");
		if (k.length>0) {
			sha1.update(k, 0, k.length);
			//System.out.println("update: "+field);//+"::"+SecurityHelper.HexDecoder.encode(k, ':', -1));
		}
	}
	
	public void signoffAction(OSDXKey key) throws Exception {
		actionSha256localproof = calcActionSha256LocalProof();
		actionSignature = Signature.createSignatureFromLocalProof(actionSha256localproof, "signature of sha1localproof", key);
		verified = true;
	}
	
	public void signoffKeyServer(OSDXKey key) throws Exception {
		sha256localproof = calcKeyServerSha256LocalProof();
		signature = Signature.createSignatureFromLocalProof(sha256localproof, "signature of sha1localproof", key);
		verified = true;
	}
	
	public static KeyLog fromElement(Element e)  throws Exception {
		return fromElement(e, true);
	}
	
	public static KeyLog fromElement(Element e, boolean tryVerification)  throws Exception {
		//Document.buildDocument(e).output(System.out);
		KeyLog kl = new KeyLog();
		kl.action = "UNKNOWN";
		
		Element ea = null;
		if (e.getName().equals("keylog")) {
			//full keylog
			ea = e.getChild("keylogaction");
			String sDate = e.getChildText("date");
			if (sDate!=null && sDate.length()>0) kl.datetime = SecurityHelper.parseDate(sDate);
			kl.ipv4 = e.getChildText("ipv4");
			kl.ipv6 = e.getChildText("ipv6");
			String sSha256 = e.getChildText("sha256localproof");
			if (sSha256!=null && sSha256.length()>0) {
				kl.sha256localproof = SecurityHelper.HexDecoder.decode(sSha256);
				kl.signature = Signature.fromElement(e.getChild("signature"));
			}
			//System.out.println(sDate+" -> datetime::"+kl.datetime);
		} else {
			//keylogaction only
			ea = e;
			kl.datetime = -1L;
			kl.ipv4 = null;
			kl.ipv6 = null;
			kl.sha256localproof = null;
			kl.signature = null;
		}
		
		//handle <keylogaction> part
		for (String c : checkForAction) {
			if (ea.getChild(c)!=null) {
				kl.action = c;
				break;
			}
		}
		kl.fromKeyid = ea.getChildText("from_keyid");
		kl.toKeyid = ea.getChildText("to_keyid");
		Element est = ea.getChild(kl.action);
		if (est!=null) {
			kl.message = est.getChildTextNN("message");
			if (kl.message.length()==0) kl.message = null;
			Element eId = est.getChild("identity");
			if (eId!=null) kl.id = Identity.fromElement(eId);
		}
		
		//build datapath
		kl.datapath = new Vector<DataSourceStep>();
		Element dp = ea.getChild("datapath");
		if (dp!=null) {
			Vector<Element> steps = dp.getChildren();
			for (Element st : steps)
			if (st.getName().startsWith("step")) {
				DataSourceStep dst = DataSourceStep.fromElemet(st);
				kl.datapath.add(dst);
			}
		}
		
		//check signatures
		kl.actionSha256localproof = SecurityHelper.HexDecoder.decode(ea.getChildText("sha256localproof"));
		kl.actionSignature = Signature.fromElement(ea.getChild("signature"));
		if (tryVerification) {
			Result v = kl.verifyActionSHA256localproofAndSignoff();
			kl.verified = v.succeeded;
			if(!kl.verified) {
				throw new Exception("KeyLog:  localproof and signoff of action failed.");
			}
		}
		
	
		if (tryVerification && kl.signature!=null) {
			Result v = kl.verifyKeyServerSHA256localproofAndSignoff();
			kl.verified = v.succeeded;
			if(!kl.verified) {
				throw new Exception("KeyLog:  localproof and signoff from keyserver failed.");
			}
		}
		return kl;
	}
	
	
	public boolean isVerified() {
		return verified;
	}
	
	public Element toFullElement() {
		Element e = new Element("keylog");
		for (Element h : getFullKeyLogElementsWithoutLocalProofAndDataPath()) {
			e.addContent(h);
		}
		if (sha256localproof!=null) {
			e.addContent("sha1256ocalproof", SecurityHelper.HexDecoder.encode(sha256localproof, ':', -1));
			if (signature!=null) {
				e.addContent(signature.toElement());
			}
		}		
		Element edp = new Element("datapath");
		for (int i=0;i<datapath.size();i++) {
			edp.addContent(datapath.get(i).toElement(i));
		}
		e.addContent(edp);

		//Document doc = Document.buildDocument(e);
		//doc.output(System.out);
		
		return e;
	}
	
	private Vector<Element> getFullKeyLogElementsWithoutLocalProofAndDataPath() {
		Vector<Element> ea = new Vector<Element>();
		ea.add(new Element("date", getDateString()));
		ea.add(new Element("ipv4", ipv4));
		ea.add(new Element("ipv6", ipv6));
		ea.add(toKeyLogActionElement());
		return ea;
	}
	
	public Element toKeyLogActionElement() {
		Element ea = new Element("keylogaction");
		for (Element e : getKeyLogActionElementsWithoutLocalProof()) {
			ea.addContent(e);
		}
		if (actionSha256localproof!=null) {
			ea.addContent("sha256localproof", SecurityHelper.HexDecoder.encode(actionSha256localproof, ':', -1));
			if (actionSignature!=null) {
				ea.addContent(actionSignature.toElement());
			}
		}
		//Document doc = Document.buildDocument(e);
		//doc.output(System.out);
		
		return ea;
	}
	
	private Vector<Element> getKeyLogActionElementsWithoutLocalProof() {
		Vector<Element> ea = new Vector<Element>();
		ea.add(new Element("from_keyid",fromKeyid));
		ea.add(new Element("to_keyid",toKeyid));
		Element eAction = new Element(action);
		if (message!=null) {
			eAction.addContent("message",message);
		}
		if (id!=null) {
			for (Element eIDContent : id.getContentElements(true)) {
				eAction.addContent(eIDContent);
			}
		}
		ea.add(eAction);
		return ea;
	}
	
	public long getDate() throws Exception {
		return datetime;
	}
	public String getDateString() {
		return SecurityHelper.getFormattedDate(datetime);
	}
	
	public String getKeyIDFrom() {
		return fromKeyid;
	}
	
	public String getKeyIDTo() {
		return toKeyid;
	}
		
	public String getIPv4() {
		return ipv4;
	}
	public String getIPv6() {
		return ipv6;
	}
	public String getAction() {
		return action;
	}
	
	public void addDataPath(DataSourceStep step) {
		datapath.add(step);
	}
	
	public Vector<DataSourceStep> getDataPath() {
		return datapath;
	}
	
	public Identity getIdentity() {
		return id;
	}
	
	public void setIdentity(Identity id) {
		this.id = id;
	}

	public void setDatetime(long datetime) {
		this.datetime = datetime;
	}
	public void setIPv4(String ip) {
		ipv4 = ip;
	}
	public void setIPv6(String ip) {
		ipv6 = ip;
	}
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public OSDXKey getActionSignatureKey() {
		return actionSignature.getKey();
	}
}
//<keylog>
//	<date>2011-04-28 13:54:14 GMT+00:00</date>
//	<ipv4>127.0.0.1</ipv4>
//	<ipv6>127.0.0.1</ipv6>
//	<keylogaction>
//  	<from_keyid>55:32:7D:CD:4E:37:D1:88:EB:02:B9:B9:5A:9F:8A:CF:45:2D:44:D3@localhost</from_keyid>
//  	<to_keyid>D9:1C:42:50:94:E3:BF:B1:2E:86:91:07:D8:54:95:CB:C5:8F:07:71@localhost</to_keyid>
//  	<approval>
//    		<identity>
//	      		<identnum>0001</identnum>
//      		<email>debug_key_0@it-is-awesome.de</email>
//      		<sha1>BC:3A:6D:C7:F5:69:E1:39:A6:BC:6B:A5:96:A6:08:F7:E0:6F:3F:32</sha1>
//    		</identity>
//  	</approval>
//  	<sha1localproof>DB:B5:D0:B4:00:26:07:83:21:1E:3A:B9:DD:E4:DA:84:4A:20:C4:61</sha1localproof>
//  	<signature>
//			[of from_key] 
//    	</signature>
//	</keylogaction>
//	<sha1localproof>A2:C4:B6:96:5B:19:C6:99:D5:FD:05:72:5D:DB:B6:00:02:E0:42:AB</sha1localproof>
//	<signature>
//  	[of keyserver]	
//	</signature>
//	<datapath />
//</keylog>
