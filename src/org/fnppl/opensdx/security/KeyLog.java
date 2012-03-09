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
 * Copyright (C) 2010-2012 
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
	
	private String ipv4 = null;
	private String ipv6 = null;
	private KeyLogAction action;
	private byte[] sha256localproof = null;
	private Signature signature = null;
	private Vector<DataSourceStep> datapath = null;
	
	private KeyLog() {
		
	}
	
	
	public static KeyLog buildNewKeyLog(KeyLogAction keylogAction, String ip4, String ip6, OSDXKey signingKey) throws Exception {
		KeyLog kl = new KeyLog();
		kl.ipv4 = ip4;
		kl.ipv6 = ip6;
		kl.action = keylogAction;
		kl.sha256localproof = kl.calcSha256LocalProof();
		kl.signature = Signature.createSignatureFromLocalProof(kl.sha256localproof, "signature of ipv4, ipv6 and signaturebytes", signingKey);
		return kl;
	}
	
	public Result verify() throws Exception {
		Result v = action.verifySignature();
		if (v.succeeded && signature!=null) {
			v = verifyLocalproofAndSignature();
		}
		return v;
	}
	
	public byte[] calcSha256LocalProof() throws Exception {
		//localproof of ipv4, ipv6, action.signature.signaturesbytes
		byte[] ret = new byte[32];  //256 bit = 32 byte
		SHA256Digest sha256 = new org.bouncycastle.crypto.digests.SHA256Digest();
		
		byte[] data;
		if (ipv4!=null && ipv4.length()>0) {
			data = ipv4.getBytes("UTF-8");
			//System.out.println("ipv4: "+ipv4);
			sha256.update(data, 0,data.length);
		}
		if (ipv6!=null && ipv4.length()>0) {
			data = ipv6.getBytes("UTF-8");
			//System.out.println("ipv6: "+ipv6);
			sha256.update(data, 0,data.length);
		}
		data = action.getSignatureBytes();
		if (data!=null) {
			//System.out.println("sigbytes: "+SecurityHelper.HexDecoder.encode(data, '\0', -1));
			sha256.update(data, 0,data.length);
		}
		sha256.doFinal(ret, 0);
		return ret;
	}
	
//	public static KeyLog buildNewKeyLog(String action, OSDXKey from, String toKeyID, String ip4, String ip6, Identity id) throws Exception {
//		KeyLog kl = new KeyLog();
//		kl.action = action;
//		kl.datetime = System.currentTimeMillis();
//		kl.ipv4 = ip4;
//		kl.ipv6 = ip6;
//		kl.fromKeyid = from.getKeyID();
//		kl.toKeyid = toKeyID;
//		kl.id = id;
//		kl.datapath = new Vector<DataSourceStep>();
//		kl.signoffAction(from);
//		kl.sha256localproof = null;
//		kl.signature = null;
//			
//		return kl;
//	}
//	
//	public static KeyLog buildNewRevocationKeyLog(String fromKeyID, String toKeyID, String message, byte[] actionproof, Signature actionSignature, String ip4, String ip6, OSDXKey serverSignoffKey) throws Exception {
//		KeyLog kl = new KeyLog();
//		kl.action = KeyLog.REVOCATION;
//		kl.datetime = System.currentTimeMillis();
//		kl.ipv4 = ip4;
//		kl.ipv6 = ip6;
//		kl.fromKeyid = fromKeyID;
//		kl.toKeyid = toKeyID;
//		kl.id = null;
//		kl.message = message;
//		kl.datapath = new Vector<DataSourceStep>();
//		kl.actionSha256localproof = actionproof;
//		kl.actionSignature = actionSignature;
//		kl.signoffKeyServer(serverSignoffKey);
//		
//		return kl;
//	}

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

	
	
	private Result verifyLocalproofAndSignature() throws Exception {
		Element report = new Element("signature_verification_report");
		
		if (signature == null) {
			report.addContent("error", "missing signature");
			return  Result.error(report);
		}
		if (sha256localproof == null) {
			report.addContent("error", "missing keyserver localproof");
			return  Result.error(report);
		}
		
		//check localproof
		byte[] bsha256 = calcSha256LocalProof();
		if (!Arrays.equals(bsha256, sha256localproof)) {
			System.out.println("sha256localproof given      : "+SecurityHelper.HexDecoder.encode(sha256localproof, '\0', -1));
			System.out.println("sha256localproof calculated : "+SecurityHelper.HexDecoder.encode(bsha256, '\0', -1));
			report.addContent("error", "verification of sha256localproof failed");
			return  Result.error(report);
		}
		//check signoff
		Result r = signature.tryVerificationMD5SHA1SHA256(bsha256);
		if (r.report != null) {
			//copy report content
			for (Element e : r.report.getChildren()) {
				report.addContent(XMLHelper.cloneElement(e));
			}
		} else {
			throw new RuntimeException("signature verification DID NOT return a report!");
		}
		if (r.succeeded) {
			return Result.succeeded(report);
		} else {
			return Result.error(report);
		}
	}
	
//	private void updateSha1(String field, org.bouncycastle.crypto.digests.SHA1Digest sha1) throws Exception {
//		if (field==null || field.length()==0) return;
//		byte[] k = field.getBytes("UTF-8");
//		if (k.length>0) {
//			sha1.update(k, 0, k.length);
//			//System.out.println("update: "+field);//+"::"+SecurityHelper.HexDecoder.encode(k, ':', -1));
//		}
//	}
	
	public static KeyLog fromElement(Element e)  throws Exception {
		return fromElement(e, true);
	}
	
	private static KeyLog fromElement(Element e, boolean tryVerification)  throws Exception {
		//Document.buildDocument(e).output(System.out);
		KeyLog kl = new KeyLog();
		kl.action = KeyLogAction.fromElement(e.getChild("keylogaction"));
		
		
		if (e.getName().equals("keylog")) {
			//full keylog
			kl.ipv4 = e.getChildText("ipv4");
			kl.ipv6 = e.getChildText("ipv6");
			String sSha256 = e.getChildText("sha256localproof");
			if (sSha256!=null && sSha256.length()>0) {
				kl.sha256localproof = SecurityHelper.HexDecoder.decode(sSha256);
				kl.signature = Signature.fromElement(e.getChild("signature"));
				//System.out.println("NO SIGNATURE FOUND");
			}
			//System.out.println(sDate+" -> datetime::"+kl.datetime);
		} else {
			throw new RuntimeException("wrong keyname");
		}
		
		//build datapath
		kl.datapath = new Vector<DataSourceStep>();
		Element dp = e.getChild("datapath");
		if (dp!=null) {
			Vector<Element> steps = dp.getChildren();
			for (Element st : steps)
			if (st.getName().startsWith("step")) {
				DataSourceStep dst = DataSourceStep.fromElemet(st);
				kl.datapath.add(dst);
			}
		}
		
		if (tryVerification) {
			Result v = kl.verify();
			//Document.buildDocument(kl.toElement(true)).output(System.out);
			if(!v.succeeded) {
				if (v.errorMessage!=null) {
					System.out.println(v.errorMessage);
				}
				if (v.report!=null) {
					Document.buildDocument(v.report).output(System.out);
				}
				throw new Exception("KeyLog:  verification of localproof and signature failed.");
			}
		}
		return kl;
	}
	
	public String getActionElementString() {
		Element e = action.getActionElement(true);
		return Document.buildDocument(e).toStringCompact(); 
	}
	
	public Element toElement(boolean showRestricted) {
		Element e = new Element("keylog");
		e.addContent("ipv4",ipv4);
		e.addContent("ipv6",ipv6);
		e.addContent(action.toElement(showRestricted));
		if (sha256localproof!=null) {
			e.addContent("sha256localproof", SecurityHelper.HexDecoder.encode(sha256localproof, ':', -1));
			if (signature!=null) {
				e.addContent(signature.toElement());
			}
		}		
		Element edp = new Element("datapath");
		if (datapath!=null) {
			for (int i=0;i<datapath.size();i++) {
				edp.addContent(datapath.get(i).toElement(i));
			}
		}
		e.addContent(edp);
		
		//Document doc = Document.buildDocument(e);
		//doc.output(System.out);
		
		return e;
	}
	
//	private Vector<Element> getFullKeyLogElementsWithoutLocalProofAndDataPath() {
//		Vector<Element> ea = new Vector<Element>();
//		ea.add(new Element("date", getDateString()));
//		ea.add(new Element("ipv4", ipv4));
//		ea.add(new Element("ipv6", ipv6));
//		ea.add(toKeyLogActionElement());
//		return ea;
//	}
//	
//	private Vector<Element> getKeyLogActionElementsWithoutLocalProof() {
//		Vector<Element> ea = new Vector<Element>();
//		ea.add(new Element("from_keyid",fromKeyid));
//		ea.add(new Element("to_keyid",toKeyid));
//		Element eAction = new Element(action);
//		if (message!=null) {
//			eAction.addContent("message",message);
//		}
//		if (id!=null) {
//			for (Element eIDContent : id.getContentElements(true)) {
//				eAction.addContent(eIDContent);
//			}
//		}
//		ea.add(eAction);
//		return ea;
//	}
	
	public long getSignDatetime() {
		return signature.getSignDatetime();
	}
	
	public long getActionDatetime() {
		return action.getSignDatetime();
	}
	
	public String getActionDatetimeString() {
		return SecurityHelper.getFormattedDate(action.getSignDatetime());
	}
		
	public String getKeyIDFrom() {
		return action.getKeyIDFrom();
	}
	
	public String getKeyIDTo() {
		return action.getKeyIDTo();
	}
		
	public String getIPv4() {
		return ipv4;
	}
	public String getIPv6() {
		return ipv6;
	}
	public String getAction() {
		return action.getAction();
	}
	
	public byte[] getActionSha256ProofComplete() {
		return action.getSha256localproof_complete();
	}
	
	public byte[] getActionSha256ProofRestricted() {
		return action.getSha256localproof_restricted();
	}
	
	public Signature getActionSignature() {
		return action.getSignature();
	}
	
	public Signature getSignature() {
		return signature;
	}
	
	public byte[] getSHA256LocalProof() {
		return sha256localproof;
	}
	
	public void addDataPath(DataSourceStep step) {
		datapath.add(step);
	}
	
	public Vector<DataSourceStep> getDataPath() {
		return datapath;
	}
	
	public Identity getIdentity() {
		return action.getIdentity();
	}
	
	public String getMessage() {
		return action.getMessage();
	}

	
	public OSDXKey getActionSignatureKey() {
		return action.getSignatureKey();
	}
	
	public boolean hasRestrictedFields() {
		return action.hasRestrictedFields();
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
