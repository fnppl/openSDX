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
	private static String[] checkFor = new String[] {APPROVAL, APPROVAL_PENDING, DISAPPROVAL, REVOCATION};
	
	
	private String status;
	private long datetime;
	private String ipv4;
	private String ipv6;
	private String fromKeyid;
	private String fromKeyidSha1;
	private String toKeyid;
	private String toKeyidSha1;
	private Identity id;
	private Vector<DataSourceStep> datapath;
	private byte[] sha1localproof;
	private Signature signature;
	
	//private Element ekeylog;
	private boolean verified = false;
	
	private KeyLog() {
		
	}
	
	public static KeyLog buildNewKeyLog(String status, OSDXKeyObject from, String toKeyID, String toKeyIDSha1, String ip4, String ip6, Identity id) throws Exception {
		KeyLog kl = new KeyLog();
		kl.status = status;
		kl.datetime = System.currentTimeMillis();
		kl.ipv4 = ip4;
		kl.ipv6 = ip6;
		kl.fromKeyid = from.getKeyID();
		kl.fromKeyidSha1 = from.getKeyModulusSHA1();
		kl.toKeyid = id.getIdentNumString()+":"+toKeyID;
		kl.toKeyidSha1 = toKeyIDSha1;
		kl.id = id;
		kl.datapath = new Vector<DataSourceStep>();
		kl.signoff(from);
		
		
//		Element e = new Element("keylog");
//		Element ea = new Element("action");
//		ea.addContent("date", SecurityHelper.getFormattedDate(System.currentTimeMillis()));
//		ea.addContent("ipv4",ip4);
//		ea.addContent("ipv6",ip6);
//		Element eFrom = new Element("from");
//		eFrom.addContent("keyid",from.getKeyID());
//		eFrom.addContent("sha1fingerprint", from.getKeyModulusSHA1());
//		ea.addContent(eFrom);
//		Element eTo = new Element("to");
//		eTo.addContent("keyid",to.getKeyID());
//		eTo.addContent("sha1fingerprint", to.getKeyModulusSHA1());
//		ea.addContent(eTo);
//		Element eStatus = new Element(status);
//		Element eid = new Element("identity");
//		eStatus.addContent(eid);
//		Vector<Element> eids = id.toElement().getChildren();
//		for (Element elid : eids) {
//			if (elid.getText()!=null && elid.getText().length()>0) {
//				eid.addContent(elid.getName(), elid.getText());
//			}
//		}
//		
//		ea.addContent(eStatus);
//		
//		Element edp = new Element("datapath");
//		//TODO datapath
//		ea.addContent(edp);
//		e.addContent(ea);
//		
//		Document doc = Document.buildDocument(e);
//		doc.output(System.out);
//		
//		KeyLog kl = KeyLog.fromElement(e,false);
//		kl.signoff(from);
		
		return kl;
	}
	
	public KeyLog deriveNewKeyLog(String status, OSDXKeyObject signoff) throws Exception {
		KeyLog kl = new KeyLog();
		kl.status = status;
		kl.datetime = datetime;
		kl.ipv4 = ""+ipv4;
		kl.ipv6 = ""+ipv6;
		kl.fromKeyid = ""+signoff.getKeyID();
		kl.fromKeyidSha1 = ""+signoff.getKeyModulusSHA1();
		kl.toKeyid = ""+toKeyid;
		kl.toKeyidSha1 = ""+toKeyidSha1;
		kl.id = id;
		kl.datapath = new Vector<DataSourceStep>();
		kl.datapath.addAll(datapath);
		kl.signoff(signoff);
		
//		Document.buildDocument(ekeylog).output(System.out);
//		Element e = new Element("keylog");
//		Element ea = new Element("action");
//		ea.addContent("date", SecurityHelper.getFormattedDate(System.currentTimeMillis()));
//		ea.addContent("ipv4",this.getIPv4());
//		ea.addContent("ipv6",this.getIPv6());
//		Element eFrom = new Element("from");
//		eFrom.addContent("keyid",this.getKeyIDFrom());
//		eFrom.addContent("sha1fingerprint", this.getKeyIDFromSha1());
//		ea.addContent(eFrom);
//		Element eTo = new Element("to");
//		eTo.addContent("keyid",this.getKeyIDTo());
//		eTo.addContent("sha1fingerprint", this.getKeyIDToSha1());
//		ea.addContent(eTo);
//		Element eStatus = new Element(status);
//		Element eid = ekeylog.getChild("action").getChild(getStatus()).getChild("identity");
//		eStatus.addContent(XMLHelper.cloneElement(eid));
//		
//		ea.addContent(eStatus);
//		
//		Element edp = new Element("datapath");
//		//TODO datapath
//		ea.addContent(edp);
//		e.addContent(ea);
//		
//		Document doc = Document.buildDocument(e);
//		doc.output(System.out);
//		
//		KeyLog kl = KeyLog.fromElement(e,false);
//		kl.signoff(signoff);
		
		return kl;
	}
	
	public Vector<String[]> getStatusElements() {
		Vector<String[]> v = new Vector<String[]>();
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
		
		return v;
	}

	public boolean verifySHA1localproofAndSignoff() throws Exception {
		if (signature==null || sha1localproof == null) return false;
		
		//check localproof
		byte[] bsha1 = calcSha1LocalProof();
		if (!Arrays.equals(bsha1, sha1localproof)) {
			System.out.println("sha1localproof target: "+SecurityHelper.HexDecoder.encode(sha1localproof, '\0', -1));
			System.out.println("sha1localproof real  : "+SecurityHelper.HexDecoder.encode(bsha1, '\0', -1));
			return false;
		}
		
		//check signoff
		return signature.tryVerificationMD5SHA1SHA256(new ByteArrayInputStream(bsha1));
		
		
//		byte[][] kk = SecurityHelper.getMD5SHA1SHA256(bsha1);
//		byte[] md5sha1sha256 = kk[0];
//		byte[] md5 = kk[1];
//		byte[] sha1 = kk[2];
//		byte[] sha256 = kk[3];
		

		
//		return SignoffElement.verifySignoff(signoff, bsha1);
	}
	
	public byte[] calcSha1LocalProof() throws Exception {
		//localproof without datapath!
		
		System.out.println("\ncalc sha1 local proof");
		byte[] ret = new byte[20];  //160bit = 20 byte
		org.bouncycastle.crypto.digests.SHA1Digest sha1 = new org.bouncycastle.crypto.digests.SHA1Digest();
		updateSha1(status, sha1);
		updateSha1(getDateString(), sha1);
		updateSha1(ipv4, sha1);
		updateSha1(ipv6, sha1);
		updateSha1(fromKeyid, sha1);
		updateSha1(fromKeyidSha1, sha1);
		updateSha1(toKeyid, sha1);
		updateSha1(toKeyidSha1, sha1);
		Vector<String[]> idFields = getStatusElements();
		for (String[] f : idFields) {
			updateSha1(f[1], sha1);
		}
		sha1.doFinal(ret, 0);
		System.out.println("--- end ---\n");
		//System.out.println("calc sha1: "+SecurityHelper.HexDecoder.encode(ret, ':', -1));
		return ret;
	}
	private void updateSha1(String field, org.bouncycastle.crypto.digests.SHA1Digest sha1) throws Exception {
		if (field==null || field.length()==0) return;
		byte[] k = field.getBytes("UTF-8");
		if (k.length>0) {
			sha1.update(k, 0, k.length);
			System.out.println("update: "+field);//+"::"+SecurityHelper.HexDecoder.encode(k, ':', -1));
		}
	}
	
	public void signoff(OSDXKeyObject key) throws Exception {
		sha1localproof = calcSha1LocalProof();
		signature = Signature.createSignatureFromLocalProof(sha1localproof, "signature of sha1localproof", key);
		verified = true;
	}
	
	public static KeyLog fromElement(Element e)  throws Exception {
		return fromElement(e, true);
	}
	
	public static KeyLog fromElement(Element e, boolean tryVerification)  throws Exception {
		//Document.buildDocument(e).output(System.out);
		KeyLog kl = new KeyLog();
		kl.status = "UNKNOWN";
		Element ea = e.getChild("action");
		for (String c : checkFor) {
			if (ea.getChild(c)!=null) {
				kl.status = c;
				break;
			}
		}
		kl.datetime = SecurityHelper.parseDate(ea.getChildText("date"));
		kl.ipv4 = ea.getChildText("ipv4");
		kl.ipv6 = ea.getChildText("ipv6");
		Element efrom = ea.getChild("from");
		kl.fromKeyid = efrom.getChildText("keyid");
		kl.fromKeyidSha1 = efrom.getChildText("sha1fingerprint");
		Element eto = ea.getChild("to");
		kl.toKeyid = eto.getChildText("keyid");
		kl.toKeyidSha1 = eto.getChildText("sha1fingerprint");
		Element est = ea.getChild(kl.status);
		kl.id = Identity.fromElement(est.getChild("identity"));
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
		kl.sha1localproof = SecurityHelper.HexDecoder.decode(e.getChildText("sha1localproof"));
		kl.signature = Signature.fromElement(e.getChild("signature"));
		if (tryVerification) {
			kl.verified = kl.verifySHA1localproofAndSignoff();
			if(!kl.verified) {
				throw new Exception("KeyStore:  localproof and signoff of keylog failed.");
			}
		}
		return kl;
	}
	
	
	public boolean isVerified() {
		return verified;
	}
	
	public Element toElement() {
		Element e = new Element("keylog");
		Element ea = new Element("action");
		ea.addContent("date", getDateString());
		ea.addContent("ipv4", ipv4);
		ea.addContent("ipv6",ipv6);
		Element eFrom = new Element("from");
		eFrom.addContent("keyid",fromKeyid);
		eFrom.addContent("sha1fingerprint", fromKeyidSha1);
		ea.addContent(eFrom);
		Element eTo = new Element("to");
		eTo.addContent("keyid",toKeyid);
		eTo.addContent("sha1fingerprint", toKeyidSha1);
		ea.addContent(eTo);
		Element eStatus = new Element(status);
		eStatus.addContent(id.toElement());
		ea.addContent(eStatus);
		
		Element edp = new Element("datapath");
		for (int i=0;i<datapath.size();i++) {
			edp.addContent(datapath.get(i).toElement(i));
		}
		ea.addContent(edp);
		e.addContent(ea);
		
		if (sha1localproof!=null) {
			e.addContent("sha1localproof", SecurityHelper.HexDecoder.encode(sha1localproof, ':', -1));
			if (signature!=null) {
				e.addContent(signature.toElement());
			}
		}
		//Document doc = Document.buildDocument(e);
		//doc.output(System.out);
		
		return e;
	
	
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
	public String getKeyIDFromSha1() {
		return fromKeyidSha1;
	}
	
	public String getKeyIDTo() {
		return toKeyid;
	}
	public String getKeyIDToSha1() {
		return toKeyidSha1;
	}
	
	public String getIPv4() {
		return ipv4;
	}
	public String getIPv6() {
		return ipv6;
	}
	public String getStatus() {
		return status;
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
}
