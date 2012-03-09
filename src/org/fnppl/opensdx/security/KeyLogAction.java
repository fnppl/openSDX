package org.fnppl.opensdx.security;

import java.util.Arrays;
import java.util.Vector;

import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;
import org.fnppl.opensdx.xml.XMLHelper;

import com.sun.org.apache.bcel.internal.generic.GETSTATIC;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

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

public class KeyLogAction {

	public static String APPROVAL = "approval"; 
	public static String APPROVAL_PENDING = "approval_pending";
	public static String DISAPPROVAL = "disapproval";
	public static String REVOCATION = "revocation";
	
	private static String[] checkForAction = new String[] {APPROVAL, APPROVAL_PENDING, DISAPPROVAL, REVOCATION};
	
	private String fromKeyid = null;
	private OSDXKey fromKey = null;
	private String toKeyid = null;
	
	private String action; // approval/disapproval/revocation/approval pending
	
	private Identity id = null;
	private String message = null;
	private byte[] sha256localproof_complete = null;
	private byte[] sha256localproof_restricted = null;
	private Signature signature = null;

	private KeyLogAction() {
		
	}
	
	public static KeyLogAction buildKeyLogAction(String action, OSDXKey from, String toKeyID, Identity id, String message) throws Exception {
		KeyLogAction a  = new KeyLogAction();
		a.action = action;
		a.id = id;
		a.message = message;
		a.fromKey = from;
		a.fromKeyid = from.getKeyID();
		a.toKeyid = toKeyID;
		a.sha256localproof_complete = a.getSha256LocalProof(true);
		a.sha256localproof_restricted = a.getSha256LocalProof(false);
		
		//signature
		byte[] localproof = SecurityHelper.concat(a.sha256localproof_complete, a.sha256localproof_restricted);
		a.signature = Signature.createSignatureFromLocalProof(localproof, "signature of sha256localproof_complete + sha256localproof_restricted", from); 
		return a;
	}
	
	public static KeyLogAction buildRevocationKeyLogAction(OSDXKey from, String toKeyID, String message) throws Exception {
		KeyLogAction a  = new KeyLogAction();
		a.action = REVOCATION;
		a.id = null;
		a.message = message;
		a.fromKey = from;
		a.fromKeyid = from.getKeyID();
		a.toKeyid = toKeyID;
		a.sha256localproof_complete = a.getSha256LocalProof(true);
		a.sha256localproof_restricted = a.getSha256LocalProof(false);
		
		//signature
		byte[] localproof = SecurityHelper.concat(a.sha256localproof_complete, a.sha256localproof_restricted);
		a.signature = Signature.createSignatureFromLocalProof(localproof, "signature of sha256localproof_complete + sha256localproof_restricted", from); 
		return a;
	}
	
	public static KeyLogAction fromElement(Element ea) {
		if (ea==null) return null;
		KeyLogAction a  = new KeyLogAction();
		a.action = "UNKNOWN";
		for (String c : checkForAction) {
			if (ea.getChild(c)!=null) {
				a.action = c;
				break;
			}
		}
		
		Element est = ea.getChild(a.action);
		if (est!=null) {
			a.message = est.getChildText("message");
			Element eId = est.getChild("identity");
			try {
				if (eId!=null) a.id = Identity.fromElement(eId);
			} catch (Exception ex) {
				ex.printStackTrace();
				throw new RuntimeException("ERROR parsing identity");
			}
		} 
		a.fromKey = null;
		a.fromKeyid = ea.getChildText("from_keyid");
		a.toKeyid = ea.getChildText("to_keyid");
		
		String sha256 = ea.getChildText("sha256localproof_complete");
		if (sha256!=null && sha256.length()>0) {
			a.sha256localproof_complete = SecurityHelper.HexDecoder.decode(sha256);
		}
		sha256 = ea.getChildText("sha256localproof_restricted");
		if (sha256!=null && sha256.length()>0) {
			a.sha256localproof_restricted = SecurityHelper.HexDecoder.decode(sha256);
		}
		try {
			a.signature = Signature.fromElement(ea.getChild("signature"));
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("ERROR parsing signature");
		}
		return a;
	}
	
	public Result verifySignature() {
		Element report = new Element("signature_verification_report");
		if (signature==null) {
			report.addContent("error","missing signature of keylog action");
			return  Result.error(report);
		}
		if (sha256localproof_complete == null) {
			report.addContent("error","missing localproof_complete of keylog action");
			return  Result.error(report);
		}
		if (sha256localproof_restricted == null) {
			report.addContent("error","missing localproof_restricted of keylog action");
			return  Result.error(report);
		}
		
		//check signatures
		try {
			byte[] calcLocalProof = getSha256LocalProof(true);
			if (Arrays.equals(calcLocalProof, sha256localproof_complete) ||  Arrays.equals(calcLocalProof, sha256localproof_restricted)) {
				byte[] localproof = SecurityHelper.concat(sha256localproof_complete, sha256localproof_restricted);
				Result res = signature.tryVerificationMD5SHA1SHA256(localproof);
				if (res.report != null) {
					//copy report content
					for (Element e : res.report.getChildren()) {
						report.addContent(XMLHelper.cloneElement(e));
					}
				} else {
					throw new RuntimeException("signature verification DID NOT return a report!");
				}
				if (res.succeeded) {
					return Result.succeeded(report);
				} else {
					return Result.error(report);
				}
			} else {
				System.out.println("sha256localproof complete    : "+SecurityHelper.HexDecoder.encode(sha256localproof_complete, '\0', -1));
				System.out.println("sha256localproof restricted  : "+SecurityHelper.HexDecoder.encode(sha256localproof_restricted, '\0', -1));
				System.out.println("sha256localproof calculated  : "+SecurityHelper.HexDecoder.encode(calcLocalProof, '\0', -1));
				
				Document.buildDocument(this.toElement(true)).output(System.out);
				
				report.addContent("error","localproof in keylog action does NOT match sha256localproof complete or restricted");
				return  Result.error(report);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			report.addContent("error","unknown error when checking localproof in keylog action");
			Result r = Result.error(report);
			r.exception = ex;
			return r;
		}
	}
	
	public Element toElement(boolean showRestricted) {
		Element e = getElementWithoutSignature(showRestricted);
		e.addContent("sha256localproof_complete", SecurityHelper.HexDecoder.encode(sha256localproof_complete,':',-1));
		e.addContent("sha256localproof_restricted", SecurityHelper.HexDecoder.encode(sha256localproof_restricted,':',-1));
		e.addContent(signature.toElement());
		return e;
	}
	
	private byte[] getSha256LocalProof(boolean showRestricted) throws Exception {
		return SecurityHelper.getSHA256LocalProof(getElementWithoutSignature(showRestricted));
	}
	
	private Element getElementWithoutSignature(boolean showRestricted) {
		Element e = new Element("keylogaction");
		e.addContent("from_keyid",fromKeyid);
		e.addContent("to_keyid",toKeyid);
		e.addContent(getActionElement(showRestricted));
		return e;
	}
	
	public Element getActionElement(boolean showRestricted) {
		Element ea =  new Element(action);
		if (id!=null) {
			//ea.addContent(id.toElement(true));
			Element eID = new Element("identity");
			Vector<Element> content = id.getContentElements(showRestricted);
			for (Element ide : content) {
				eID.addContent(ide);
			}
			ea.addContent(eID);
		}
		if (message!=null) {
			ea.addContent("message",message);
		}
		return ea;
	}
	
	public Signature getSignature() {
		return signature;
	}
	
	public byte[] getSignatureBytes() {
		if (signature==null) return null;
		return signature.getSignatureBytes();
	}
	
	public Result uploadToKeyServer(KeyClient client, OSDXKey signingKey) {
		try {
			boolean ok = client.putKeyLogAction(this, signingKey);
			if (ok) return Result.succeeded();
			else Result.error(client.getMessage());
		} catch (Exception ex) {
			return Result.error(ex);
		}
		return Result.error("unknown error");
	}
	
	public long getSignDatetime() {
		return signature.getSignDatetime();
	}
	
	public String getKeyIDFrom() {
		return fromKeyid;
	}
	
	public String getKeyIDTo() {
		return toKeyid;
	}
	
	public String getAction() {
		return action;
	}
	
	public Identity getIdentity() {
		return id;
	}
	
	public String getMessage() {
		return message;
	}

	
	public OSDXKey getSignatureKey() {
		return signature.getKey();
	}
	
	public boolean hasRestrictedFields() {
		if (id==null) return false;
		return id.hasRestrictedFields();
	}
	
	public byte[] getSha256localproof_complete() {
		return sha256localproof_complete;
	}

	public byte[] getSha256localproof_restricted() {
		return sha256localproof_restricted;
	}

}
