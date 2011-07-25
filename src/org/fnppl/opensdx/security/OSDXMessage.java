package org.fnppl.opensdx.security;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;
import org.fnppl.opensdx.xml.XMLHelper;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


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
public class OSDXMessage {

	private Element content = null;
	private byte[] sha256localproof = null;
	private Vector<Signature> signatures = null;
	private OSDXKey contentEncryptionKey = null;
	
	private OSDXMessage() {
		
	}

	public static OSDXMessage buildMessage(Element content) throws Exception {
		if (content==null) throw new RuntimeException("ERROR: OSDXMessage::empty content");
		OSDXMessage m = new OSDXMessage();
		m.content = content;
		m.sha256localproof = SecurityHelper.getSHA256LocalProof(content);
		m.signatures = null;
		return m;
	}
	public static OSDXMessage buildMessage(Element content, OSDXKey signingkey) throws Exception {
		OSDXMessage m = buildMessage(content);
		m.signContent(signingkey);
		return m;
	}
	public static OSDXMessage buildEncryptedMessage(Element content, OSDXKey contentEncryptionKey) throws Exception {
		if (content==null) throw new RuntimeException("ERROR: OSDXMessage::empty content");
		OSDXMessage m = new OSDXMessage();
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		Document.buildDocument(content).outputCompact(bout);
		byte[] encrypted = contentEncryptionKey.encrypt(bout.toByteArray());
		
		Element encryptedContent = new Element("encrypted_content");
		encryptedContent.setText(new BASE64Encoder().encode(encrypted));
		
		m.content = encryptedContent;
		m.sha256localproof = SecurityHelper.getSHA256LocalProof(m.content);
		m.signatures = null;
		return m;
	}
	
	public static OSDXMessage buildEncryptedMessage(Element content, OSDXKey contentEncryptionKey, OSDXKey signingkey) throws Exception {
		OSDXMessage m = buildEncryptedMessage(content, contentEncryptionKey);
		m.signContent(signingkey);
		return m;
	}
	
	
	public static OSDXMessage fromElement(Element osdxMessage) throws Exception {
		if (!osdxMessage.getName().equals("opensdx_message")) {
			throw new RuntimeException("ERROR: OSDXMessage::wrong format");
		}
		if (osdxMessage.getChild("content").getChildren().size()==0) {
			throw new RuntimeException("ERROR: OSDXMessage::no content");
		}
		if (osdxMessage.getChild("content").getChildren().size()>1) {
			throw new RuntimeException("ERROR: OSDXMessage::wrong content format");
		}
		Element content = XMLHelper.cloneElement(osdxMessage.getChild("content").getChildren().get(0));
		OSDXMessage m = buildMessage(content);
		
		Element esha256 = osdxMessage.getChild("sha256localproof");
		byte[] givenLocalproof = null;
		if (esha256!=null) {
			givenLocalproof = SecurityHelper.HexDecoder.decode(esha256.getText());
		}
		if (givenLocalproof==null || !Arrays.equals(m.sha256localproof, givenLocalproof)) {
			System.out.println("givenLocalproof : "+SecurityHelper.HexDecoder.encode(givenLocalproof, '\0', -1));
			System.out.println("calc  localproof: "+SecurityHelper.HexDecoder.encode(m.sha256localproof, '\0', -1));
			System.out.println("calc2 localproof: "+SecurityHelper.HexDecoder.encode(SecurityHelper.getSHA256LocalProof(content), '\0', -1));
			Document.buildDocument(m.toElement()).output(System.out);
			
			throw new RuntimeException("ERROR: OSDXMessage::wrong or missing sha256localproof");
		}
		Element eSignatures = osdxMessage.getChild("signatures");
		m.signatures = new Vector<Signature>();
		if (eSignatures!=null) {
			for (Element eSignature : eSignatures.getChildren("signature")) {
				m.signatures.add(Signature.fromElement(eSignature));
			}
		}
		return m;
	}
	
	public Element getContent() {
		return content;
	}
	
	public Element getDecryptedContent(OSDXKey decryptionKey) {
		try {
			if (!content.getName().equals("encrypted_content")) {
				return content;
			}
			byte[] encrypt = new BASE64Decoder().decodeBuffer(content.getText());
			
			byte[] decrypt = decryptionKey.decrypt(encrypt);
			String s = new String(decrypt, "UTF-8");
			//System.out.println(s);
			return Document.fromString(s).getRootElement();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void signContent(OSDXKey signingkey) throws Exception {
		signatures = new Vector<Signature>();
		Signature signature = Signature.createSignatureFromLocalProof(sha256localproof, "signature of sha256localproof", signingkey);
		signatures.add(signature);
	}
	
	public void signLastSignature(OSDXKey signingkey, String dataname) throws Exception {
		if (signatures==null || signatures.size()<1) {
			throw new RuntimeException("ERROR: OSDXMessage::missing signature");
		}
		Signature lastSignature = signatures.lastElement();
		byte[] lastSignatureByters = lastSignature.getSignatureBytes();
		Signature signature = Signature.createSignatureFromLocalProof(lastSignatureByters, dataname, signingkey);
		signatures.add(signature);
	}
	public Result verifySignatures(KeyVerificator keyverificator) throws Exception {
		return verifySignatures(true, keyverificator);
	}
	
	public Result verifySignaturesWithoutKeyVerification() throws Exception {
		return verifySignatures(false, null);
	}
	
	private Result verifySignatures(boolean verifyKeys, KeyVerificator keyverificator) throws Exception {
	//	if (1==1) throw new RuntimeException("ERROR: OSDXMessage::verifySignatures not implemented");
		for (int i=0;i<signatures.size();i++) {
			Signature signature = signatures.get(i);
			Result verified = null;
			//verify internal signature
			if (i==0) {
				verified = signature.tryVerificationMD5SHA1SHA256(sha256localproof);
			} else {
				verified = signature.tryVerificationMD5SHA1SHA256(signatures.get(i-1).getSignatureBytes());
			}
			if (verified.succeeded && verifyKeys) {
				//verify key from signature
				verified = keyverificator.verifyKey(signature.getKey(), signature.getSignDatetime());
			}
			if (!verified.succeeded) return verified;
		}
		return Result.succeeded();
	}
	
	public Element toElement() {
		if (content==null) return null;
		Element e = new Element("opensdx_message");

		Element c = new Element("content");
		c.addContent(content);			
		
		Element s = new Element("signatures");
		if (signatures!=null) {
			for (Signature signature : signatures) {
				s.addContent(signature.toElement());
			}
		}
		e.addContent(c);
		e.addContent("sha256localproof",SecurityHelper.HexDecoder.encode(sha256localproof, ':',-1));
		e.addContent(s);
		return e;
	}
	
	public Vector<Signature> getSignatures() {
		return signatures;
	}
	
	public byte[] getSha256LocalProof() {
		return sha256localproof;
	}
	
	
}
