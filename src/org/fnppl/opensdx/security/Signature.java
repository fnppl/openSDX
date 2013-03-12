package org.fnppl.opensdx.security;


/*
 * Copyright (C) 2010-2013 
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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;

import org.fnppl.opensdx.gui.DefaultMessageHandler;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;

import com.sun.jndi.cosnaming.IiopUrl.Address;

/*
 * @author Henning Thieß <ht@fnppl.org>
 * 
 * moved a lot from "SignAndVerify"
 */

public class Signature {
	static {
		SecurityHelper.ensureBC();
	}
	
	private byte[] datamd5 = null;
	private byte[] datasha1 = null;
	private byte[] datasha256 = null;	
	private String dataname = null;
	private long signdatetime = -1;
	
	private OSDXKey key = null;
	private byte[] signaturebytes = null;
	
	private Signature() {
		
	}
	
	public static Signature fromFile(File f) throws Exception {
		Document d = Document.fromFile(f);
		return fromElement(d.getRootElement());
	}
	
	public static Signature fromElement(Element e) throws Exception {
		Element ed = e.getChild("data");
		Element epk = e.getChild("pubkey");
		
		Signature s = new Signature();
		//data
		s.datamd5 = SecurityHelper.HexDecoder.decode(ed.getChildText("md5"));
		s.datasha1 = SecurityHelper.HexDecoder.decode(ed.getChildText("sha1"));
		s.datasha256 = SecurityHelper.HexDecoder.decode(ed.getChildText("sha256"));
		s.signdatetime = SecurityHelper.parseDate(ed.getChildText("signdatetime"));
		s.dataname = ed.getChildText("dataname");
		
		s.key = OSDXKey.fromPubKeyElement(epk);
		s.signaturebytes = SecurityHelper.HexDecoder.decode(e.getChildText("signaturebytes"));
		return s;
	}
	
	public Element toElement() {
		Element e = new Element("signature");
		//data
		Element ed = new Element("data");
		if(datamd5 != null) ed.addContent("md5", SecurityHelper.HexDecoder.encode(datamd5,':',-1));
		if(datasha1 != null) ed.addContent("sha1", SecurityHelper.HexDecoder.encode(datasha1,':',-1));
		if(datasha256 != null) ed.addContent("sha256", SecurityHelper.HexDecoder.encode(datasha256,':',-1));
		ed.addContent("signdatetime", SecurityHelper.getFormattedDate(signdatetime));
		ed.addContent("dataname",dataname);
		e.addContent(ed);
		
		//key
		e.addContent(key.getSimplePubKeyElement());
		//signature bytes
		e.addContent("signaturebytes", SecurityHelper.HexDecoder.encode(signaturebytes,':',-1));
		return e;
	}
	
	public static Signature createSignatureFromLocalProof(byte[] localproof, String dataname,  OSDXKey key) throws Exception {
		byte[][] md5sha1sha256 = SecurityHelper.getMD5SHA1SHA256(localproof);
		return Signature.createSignature(md5sha1sha256[1], md5sha1sha256[2], md5sha1sha256[3], dataname, key);
	}
			
	public static Signature createSignature(
			byte[] md5, 
			byte[] sha1, 
			byte[] sha256,
			String dataname, 
			OSDXKey key) throws Exception {
		
		if (!key.allowsSigning()) {
			throw new RuntimeException("ERROR: key does not allow signing.");
		}
		
		Signature s = new Signature();
		s.datamd5 = md5;
		s.datasha1 = sha1;
		s.datasha256 = sha256;
		s.dataname = dataname;
		s.signdatetime = System.currentTimeMillis();//HT 2011-03-06 TODO: Signing_TIMESERVER
		s.signdatetime = s.signdatetime - s.signdatetime%1000; //BB 2011-03-07 no milliseconds in datemeGMT format
		s.signaturebytes = key.sign(md5, sha1, sha256, s.signdatetime);
		s.key = key;
		
		return s;
	}
	
	public static Signature createSignature(byte[] data, String filename, OSDXKey key) throws Exception {
		byte[][] kk = SecurityHelper.getMD5SHA1SHA256(data);
	//	byte[] md5sha1sha256 = kk[0];
		byte[] md5 = kk[1];
		byte[] sha1 = kk[2];
		byte[] sha256 = kk[3];
		
		return createSignature(md5, sha1, sha256, filename, key);
	}
	public static Signature createSignature(File toSign, OSDXKey key) throws Exception {
		byte[][] kk = SecurityHelper.getMD5SHA1SHA256(toSign);
	//	byte[] md5sha1sha256 = kk[0];
		byte[] md5 = kk[1];
		byte[] sha1 = kk[2];
		byte[] sha256 = kk[3];
		
		return createSignature(md5, sha1, sha256, toSign.getName(), key);
	}
	
	public static void createSignatureFile(File toSign, File output, OSDXKey key) throws Exception {
		Signature s = createSignature(toSign, key);
		Document doc = Document.buildDocument(s.toElement());
		doc.writeToFile(output);
	}
	
	public static void createSignatureFile(File toSign, File output, OSDXKey key, String tsa_server) throws Exception {
		Signature s = createSignature(toSign, key);
		String host = tsa_server;
		int port = TSAClient.OSDX_TSASERVER_DEFAULT_PORT;
		int ind = tsa_server.indexOf(':'); 
		if (ind>0) {
			try {
				port = Integer.parseInt(tsa_server.substring(ind+1));
			} catch (Exception ex) {
				port = TSAClient.OSDX_TSASERVER_DEFAULT_PORT;
				ex.printStackTrace();
			}
			host = tsa_server.substring(0,ind);
		}
		TSAClient tsa = new TSAClient(host,port);
		tsa.connect();
		Signature sig_tsa = tsa.getTSASignature(s);
		tsa.close();
		Element e = new Element("signatures");
		e.addContent(s.toElement());
		e.addContent(sig_tsa.toElement());
		Document doc = Document.buildDocument(e);
		doc.writeToFile(output);
	}
	
	public OSDXKey getKey() {
		return key;
	}
	
	public long getSignDatetime() {
		return signdatetime;
	}
	
	public byte[] getSignatureBytes() {
		return signaturebytes;
	}
	
	public byte[] getMD5() {
		return datamd5;
	}
	public byte[] getSHA1() {
		return datasha1;
	}
	public byte[] getSHA256() {
		return datasha256;
	}
	public Result tryVerificationFile(File f) throws Exception {
		FileInputStream in = new FileInputStream(f);
		BufferedInputStream bin = new BufferedInputStream(in);
		Result verified = tryVerificationMD5SHA1SHA256(bin);
		in.close();
		return verified;
	}
	
	public Result tryVerificationMD5SHA1SHA256(byte[] in) throws Exception {
		return tryVerificationMD5SHA1SHA256(new ByteArrayInputStream(in));
	}
	
	public Result tryVerificationMD5SHA1SHA256(InputStream in) throws Exception {
		byte[][] kk = SecurityHelper.getMD5SHA1SHA256(in);
		byte[] md5sha1sha256 = kk[0];
		byte[] md5 = kk[1];
		byte[] sha1 = kk[2];
		byte[] sha256 = kk[3];
		
		
//		System.out.println("md5            : "+SecurityHelper.HexDecoder.encode(md5,'\0',-1));
//		System.out.println("sha1           : "+SecurityHelper.HexDecoder.encode(sha1,'\0',-1));
//		System.out.println("sha256         : "+SecurityHelper.HexDecoder.encode(sha256,'\0',-1));
//		System.out.println("signdatetime   : "+OSDXKey.datemeGMT.format((new Date(signdatetime)))+" long = "+signdatetime);
		return tryVerification(md5, sha1, sha256);
	}
	
	public Result tryVerification(byte[] md5, byte[] sha1, byte[] sha256) throws Exception {
		Element report = new Element("signature_verification_report");
		report.addContent("keyid",key.getKeyID());
		report.addContent("check_datetime", SecurityHelper.getFormattedDate(System.currentTimeMillis()));
		report.addContent("dataname", dataname);
		report.addContent("signature_datetime", SecurityHelper.getFormattedDate(signdatetime));
		if (datamd5!=null) {
			report.addContent("md5",SecurityHelper.HexDecoder.encode(datamd5, ':',-1));
		}
		if (datasha1!=null) {
			report.addContent("sha1",SecurityHelper.HexDecoder.encode(datasha1, ':',-1));
		}
		if (datasha256!=null) {
			report.addContent("sha256",SecurityHelper.HexDecoder.encode(datasha256, ':',-1));
		}
		report.addContent("signature_datetime", SecurityHelper.getFormattedDate(getSignDatetime()));
		report.addContent("key_valid_from", SecurityHelper.getFormattedDate(key.getValidFrom()));
		report.addContent("key_valid_until", SecurityHelper.getFormattedDate(key.getValidUntil()));
		
		if (datamd5!=null && md5!=null) {
			addReportCheck(report,"md5 hash matches", Arrays.equals(datamd5, md5));
		}
		if (datasha1!=null && sha1!=null) {
			addReportCheck(report,"sha1 hash matches", Arrays.equals(datasha1, sha1));
		}
		if (datasha256!=null && sha256!=null) {
			addReportCheck(report,"sha256 hash matches", Arrays.equals(datasha256, sha256));
		}
		boolean ok = true;
		//key allows signing
		if (key.allowsSignatures()) {
			addReportCheck(report,"key allows signing",true);
		} else {
			addReportCheck(report,"key allows signing",false);
			ok = false;
		}
		//sha1 of key modulus = keyid
		byte[] keyid = SecurityHelper.HexDecoder.decode(OSDXKey.getFormattedKeyIDModulusOnly(key.getKeyID()));
		if (!Arrays.equals(keyid, SecurityHelper.getSHA1(key.getPublicModulusBytes()))) {
			addReportCheck(report,"key id matches sha1 of modulus",false);
			ok = false;
		} else {
			addReportCheck(report,"key id matches sha1 of modulus",true);
		}
		//datetime
		boolean inDatetime = true;
		if (key.getValidFrom()>signdatetime) {
			inDatetime = false;
		}
		if (key.getValidUntil()<signdatetime) {
			inDatetime = false;
		}
		if (inDatetime) {
			addReportCheck(report,"key valid at signature datetime",true);
		} else {
			ok = false;
			addReportCheck(report,"key valid at signature datetime",false);
		}
		try {
			boolean verify = key.verify(signaturebytes,md5,sha1,sha256,signdatetime);
			if (!verify) {
				ok = false;
				addReportCheck(report,"signature bytes sign hashes and datetime",false);
			} else {
				addReportCheck(report,"signature bytes sign hashes and datetime",true);
			}
		} catch (Exception ex) {
			addReportCheck(report,"signature bytes sign hashes and datetime",false);
			Result r = Result.error(ex);
			r.report = report;
			return r;
		}
		if (ok) {
			return Result.succeeded(report);
		} else {
			return Result.error(report);
		}
	}
	
	private void addReportCheck(Element report, String msg, boolean ok) {
		Element e = new Element("check");
		e.addContent("message",msg);
		e.addContent("result", (ok?"OK":"FAILED"));
		report.addContent(e);
	}
	
	public String getDataName() {
		return dataname;
	}
	
//	public boolean tryVerification(byte[] data) throws Exception {
//		//check md5 and sha256
//		byte[] md5 = SecurityHelper.getMD5(data);
//		if (!md5.equals(datamd5)) return false;
//		byte[] sha256bytes = SecurityHelper.getSHA256(data);
//		if (!sha256bytes.equals(datasha256)) return false;
//		
//		//check signature
//		byte[] concat = new byte[sha256bytes.length+md5.length];
//		System.arraycopy(sha256bytes, 0, concat, 0, sha256bytes.length);
//		System.arraycopy(md5, 0, concat, sha256bytes.length, md5.length);
//		return pubkey.verify(signaturebytes, concat); 
		
		//return false;
//		
//		BufferedInputStream inData = new BufferedInputStream(new FileInputStream(signedFile));
//    	BufferedInputStream inSig = new BufferedInputStream(PGPUtil.getDecoderStream(new FileInputStream(signature)));
//    	
//    	PGPObjectFactory pgpFact = new PGPObjectFactory(inSig);
//    	PGPSignatureList p3 = null;
//    	Object o = pgpFact.nextObject();//TODO HT 20.02.2011 @beboe - what else can there be???
//    	if (o instanceof PGPCompressedData) {
//    		PGPCompressedData c1 = (PGPCompressedData)o;
//    		pgpFact = new PGPObjectFactory(c1.getDataStream());
//    		p3 = (PGPSignatureList)pgpFact.nextObject();
//    	} else {
//    		p3 = (PGPSignatureList)o;
//    	}
//    	PGPSignature sig = p3.get(0);
//    	
//    	try {
//	    	PublicKey pk = keycoll.getPublicKey(sig.getKeyID());
//	    	
//	    	PGPPublicKey key = pk.getPGPPublicKey();
//	    	sig.initVerify(key, "BC");
//	    	int read = 0;
//	    	byte[] buff = new byte[1024];
//	    	while ((read=inData.read(buff)) != -1) {
//	    		sig.update(buff, 0, read);
//	    	}
//	    	return sig.verify();
//    	} catch (Exception ex) {
//    		if (ex.getMessage().startsWith("NO MATCHING KEY FOUND")) {
//    			System.out.println("NO MATCHING KEY FOUND!");
//    		} else {
//    			ex.printStackTrace();
//    		}
//    		return false;
//    	}
    	
//    	inData.close();
//    	inSig.close();
//    }
	
	public static void main(String arg[]) {
		try {
			File toSign = new File("src/org/fnppl/opensdx/security/resources/example_keystore.xml");
			File output = new File("example_keystore_signature.xml");
			
			KeyApprovingStore store = KeyApprovingStore.fromFile(new File("src/org/fnppl/opensdx/security/resources/example_keystore.xml"), new DefaultMessageHandler());
			OSDXKey key = store.getAllKeys().firstElement();
			
			System.out.println("\n\ncreating signature");
			Signature.createSignatureFile(toSign, output, key);
			
			
			System.out.println("\n\nverifing signature:");
			Signature s = Signature.fromFile(output);
			Result v = s.tryVerificationFile(toSign);
			if (v.succeeded) {
				System.out.println("signature verified.");
			} else {
				System.out.println("signature NOT verified.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
