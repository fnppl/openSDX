package org.fnppl.opensdx.security;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Vector;

import javax.print.Doc;

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
	
	
	private String fromKeyid = null;
	private OSDXKey fromKey = null;
	private String toKeyid = null;
	private String action; // approval/disapproval/revocation/approval pending
	
	private long datetime = -1L;
	private String ipv4 = null;
	private String ipv6 = null;

	private Identity id = null;
	private String message = null;
	private byte[] actionSha1localproof = null;
	private Signature actionSignature = null;
	private Vector<DataSourceStep> datapath = null;
	private byte[] keyserverSha1localproof = null;
	private Signature keyserverSignature = null;
	
	
	//private Element ekeylog;
	private boolean verified = false;
	
	private KeyLog() {
		
	}
	
	public static KeyLog buildKeyLogAction(String action, OSDXKey from, String toKeyID, Identity id) throws Exception {
		KeyLog kl = new KeyLog();
		kl.action = action;
		kl.datetime = System.currentTimeMillis();
		kl.ipv4 = "LOCAL";
		kl.ipv6 = "LOCAL";
		kl.fromKeyid = from.getKeyID();
		kl.fromKey = from;
		kl.toKeyid = toKeyID;
		kl.id = id;
		kl.datapath = new Vector<DataSourceStep>();
		kl.signoffAction(from);
		kl.keyserverSha1localproof = null;
		kl.keyserverSignature = null;
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
		kl.keyserverSha1localproof = null;
		kl.keyserverSignature = null;
			
		return kl;
	}
	
	public Result uploadToKeyServer(String host, int port, OSDXKey signingKey) {
		try {
			KeyClient client =  new KeyClient(host, port);
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
		kl.actionSha1localproof = actionproof;
		kl.actionSignature = actionSignature;
		kl.signoffKeyServer(serverSignoffKey);
		
		return kl;
	}

	public Vector<String[]> getStatusElements() {
		Vector<String[]> v = new Vector<String[]>();
		if (message!=null) {
			v.add(new String[]{"message", message});
		}
		if (id!=null) {
			v.add(new String[]{"identnum", id.getIdentNumString()});
			v.add(new String[]{"email", id.email});
			v.add(new String[]{"mnemonic", id.mnemonic});
			
			v.add(new String[]{"country", id.country});
			v.add(new String[]{"region", id.region});
			v.add(new String[]{"city", id.city});
			v.add(new String[]{"postcode", id.postcode});
			
			v.add(new String[]{"company", id.company});
			v.add(new String[]{"unit", id.unit});
			v.add(new String[]{"subunit", id.subunit});
			v.add(new String[]{"function", id.function});
			
			v.add(new String[]{"surname", id.surname});
			v.add(new String[]{"middlename", id.middlename});
			v.add(new String[]{"name", id.name});
			
			v.add(new String[]{"phone", id.phone});
			v.add(new String[]{"note", id.note});
		}
		return v;
	}

	public Result verify() throws Exception {
		Result v = verifyActionSHA1localproofAndSignoff();
		if (v.succeeded && keyserverSignature!=null) {
			v = verifyKeyServerSHA1localproofAndSignoff();
		}
		return v;
	}
	
	public Result verifyActionSHA1localproofAndSignoff() throws Exception {
		if (actionSignature==null) return  Result.error("missing action signature");
		if (actionSha1localproof == null) return  Result.error("missing action localproof");
		
		//check localproof
		byte[] bsha1 = calcActionSha1LocalProof();
		if (!Arrays.equals(bsha1, actionSha1localproof)) {
			System.out.println("sha1localproof target: "+SecurityHelper.HexDecoder.encode(actionSha1localproof, '\0', -1));
			System.out.println("sha1localproof real  : "+SecurityHelper.HexDecoder.encode(bsha1, '\0', -1));
			return Result.error("verification of sha1localproof failed");
		}	
		//check signoff
		return actionSignature.tryVerificationMD5SHA1SHA256(new ByteArrayInputStream(bsha1));
	}
	public Result verifyKeyServerSHA1localproofAndSignoff() throws Exception {
		if (keyserverSignature==null) return  Result.error("missing signature");
		if (keyserverSha1localproof == null) return  Result.error("missing keyserver localproof");
		//check localproof
		byte[] bsha1 = calcKeyServerSha1LocalProof();
		if (!Arrays.equals(bsha1, keyserverSha1localproof)) {
			System.out.println("sha1localproof target: "+SecurityHelper.HexDecoder.encode(keyserverSha1localproof, '\0', -1));
			System.out.println("sha1localproof real  : "+SecurityHelper.HexDecoder.encode(bsha1, '\0', -1));
			return Result.error("verification of sha1localproof failed");
		}
		//check signoff
		return keyserverSignature.tryVerificationMD5SHA1SHA256(new ByteArrayInputStream(bsha1));
	}
	
	public byte[] calcActionSha1LocalProof() throws Exception {
		//localproof of keylogaction
		//System.out.println("\ncalc sha1 local proof");
		byte[] ret = new byte[20];  //160bit = 20 byte
		org.bouncycastle.crypto.digests.SHA1Digest sha1 = new org.bouncycastle.crypto.digests.SHA1Digest();
		updateSha1(fromKeyid, sha1);
		updateSha1(toKeyid, sha1);
		if (action.equals(REVOCATION)) {
			updateSha1(message, sha1);
		} else {
			updateSha1(action, sha1);
			Vector<String[]> idFields = getStatusElements();
			for (String[] f : idFields) {
				updateSha1(f[1], sha1);
			}
		}
		sha1.doFinal(ret, 0);
		//System.out.println("--- end ---\n");
		//System.out.println("calc sha1: "+SecurityHelper.HexDecoder.encode(ret, ':', -1));
		return ret;
	}
	
	public byte[] calcKeyServerSha1LocalProof() throws Exception {
		//localproof without datapath!
		//System.out.println("\ncalc sha1 local proof");
		byte[] ret = new byte[20];  //160bit = 20 byte
		org.bouncycastle.crypto.digests.SHA1Digest sha1 = new org.bouncycastle.crypto.digests.SHA1Digest();
		updateSha1(getDateString(), sha1);
		updateSha1(ipv4, sha1);
		updateSha1(ipv6, sha1);
		
		updateSha1(fromKeyid, sha1);
		updateSha1(toKeyid, sha1);
		if (action.equals(REVOCATION)) {
			updateSha1(message, sha1);
		} else {
			updateSha1(action, sha1);
			Vector<String[]> idFields = getStatusElements();
			for (String[] f : idFields) {
				updateSha1(f[1], sha1);
			}
		}
		sha1.doFinal(ret, 0);
		//System.out.println("--- end ---\n");
		//System.out.println("calc sha1: "+SecurityHelper.HexDecoder.encode(ret, ':', -1));
		return ret;
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
		actionSha1localproof = calcActionSha1LocalProof();
		actionSignature = Signature.createSignatureFromLocalProof(actionSha1localproof, "signature of sha1localproof", key);
		verified = true;
	}
	
	public void signoffKeyServer(OSDXKey key) throws Exception {
		keyserverSha1localproof = calcKeyServerSha1LocalProof();
		keyserverSignature = Signature.createSignatureFromLocalProof(keyserverSha1localproof, "signature of sha1localproof", key);
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
			String sSha1 = e.getChildText("sha1localproof");
			if (sSha1!=null && sSha1.length()>0) {
				kl.keyserverSha1localproof = SecurityHelper.HexDecoder.decode(sSha1);
				kl.keyserverSignature = Signature.fromElement(e.getChild("signature"));
			}
		} else {
			//keylogaction only
			ea = e;
			kl.datetime = -1L;
			kl.ipv4 = null;
			kl.ipv6 = null;
			kl.keyserverSha1localproof = null;
			kl.keyserverSignature = null;
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
			kl.message = est.getChildText("message");
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
		kl.actionSha1localproof = SecurityHelper.HexDecoder.decode(ea.getChildText("sha1localproof"));
		kl.actionSignature = Signature.fromElement(ea.getChild("signature"));
		if (tryVerification) {
			Result v = kl.verifyActionSHA1localproofAndSignoff();
			kl.verified = v.succeeded;
			if(!kl.verified) {
				throw new Exception("KeyLog:  localproof and signoff of action failed.");
			}
		}
		
	
		if (tryVerification && kl.keyserverSignature!=null) {
			Result v = kl.verifyKeyServerSHA1localproofAndSignoff();
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
		e.addContent("date", getDateString());
		e.addContent("ipv4", ipv4);
		e.addContent("ipv6",ipv6);
		e.addContent(toKeyLogActionElement());
		if (keyserverSha1localproof!=null) {
			e.addContent("sha1localproof", SecurityHelper.HexDecoder.encode(keyserverSha1localproof, ':', -1));
			if (keyserverSignature!=null) {
				e.addContent(keyserverSignature.toElement());
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
	
	public Element toKeyLogActionElement() {
		Element ea = new Element("keylogaction");
		ea.addContent("from_keyid",fromKeyid);
		ea.addContent("to_keyid",toKeyid);
		Element eAction = new Element(action);
		if (message!=null) {
			eAction.addContent("message",message);
		}
		if (id!=null) {
			eAction.addContent(id.toElementOfNotNull());
		}
		ea.addContent(eAction);
		if (actionSha1localproof!=null) {
			ea.addContent("sha1localproof", SecurityHelper.HexDecoder.encode(actionSha1localproof, ':', -1));
			if (actionSignature!=null) {
				ea.addContent(actionSignature.toElement());
			}
		}
		
		//Document doc = Document.buildDocument(e);
		//doc.output(System.out);
		
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
