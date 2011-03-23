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

import java.io.*;
import java.math.BigInteger;
import java.util.*;

import org.bouncycastle.bcpg.ArmoredInputStream;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.fnppl.opensdx.gui.DefaultMessageHandler;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;

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
	
	private PublicKey pubkey = null;
	private byte[] signaturebytes = null;
	
	private Signature() {
		
	}
	
	public static Signature fromFile(File f) throws Exception {
		Document d = Document.fromFile(f);
		return fromElement(d.getRootElement());
	}
	
	public static Signature fromElement(Element e) throws Exception {
		Element ed = e.getChild("data");
		Element es = e.getChild("signoff");
		Element epk = es.getChild("pubkey");
		
		Signature s = new Signature();
		s.datamd5 = SecurityHelper.HexDecoder.decode(ed.getChildText("md5"));
		s.datasha1 = SecurityHelper.HexDecoder.decode(ed.getChildText("sha1"));
		s.datasha256 = SecurityHelper.HexDecoder.decode(ed.getChildText("sha256"));
		
		s.signdatetime = SecurityHelper.parseDate(ed.getChildText("signdatetime"));
		
		s.dataname = ed.getChildText("dataname");
		BigInteger mod = new BigInteger(SecurityHelper.HexDecoder.decode(epk.getChildText("modulus")));
		BigInteger exp = new BigInteger(SecurityHelper.HexDecoder.decode(epk.getChildText("exponent")));
		s.pubkey = new PublicKey(mod, exp);
//		s.signoffsha1 = SecurityHelper.HexDecoder.decode(es.getChildText("sha1"));
		
		s.signaturebytes = SecurityHelper.HexDecoder.decode(es.getChildText("signaturebytes"));
		return s;
	}
	
	public Element toElement() {
		Element e = new Element("signature");
		Element ed = new Element("data");
		if(datamd5 != null) ed.addContent("md5", SecurityHelper.HexDecoder.encode(datamd5,':',-1));
		if(datasha1 != null) ed.addContent("sha1", SecurityHelper.HexDecoder.encode(datasha1,':',-1));
		if(datasha256 != null) ed.addContent("sha256", SecurityHelper.HexDecoder.encode(datasha256,':',-1));
		
		ed.addContent("signdatetime", SecurityHelper.getFormattedDate(signdatetime));
		
		ed.addContent("dataname",dataname);
		e.addContent(ed);
		
		Element es = new Element("signoff");
		es.addContent("keyid", pubkey.getKeyID());
		Element ep = new Element("pubkey");
		ep.addContent("algo", "RSA"); //TODO check algo
		ep.addContent("bits", ""+pubkey.getBitCount());
		ep.addContent("modulus", pubkey.getModulusAsHex());
		ep.addContent("exponent", pubkey.getPublicExponentAsHex());
		es.addContent(ep);
//		es.addContent("sha1",SecurityHelper.HexDecoder.encode(signoffsha1,':',-1));
		es.addContent("signaturebytes", SecurityHelper.HexDecoder.encode(signaturebytes,':',-1));
		e.addContent(es);
		return e;
	}
	
	public static Signature createSignatureFromLocalProof(byte[] sha1localproof, String dataname,  OSDXKeyObject key) throws Exception {
		byte[][] md5sha1sha256 = SecurityHelper.getMD5SHA1SHA256(sha1localproof);
		return Signature.createSignature(md5sha1sha256[1], md5sha1sha256[2], md5sha1sha256[3], dataname, key);
	}
			
	public static Signature createSignature(
			byte[] md5, 
			byte[] sha1, 
			byte[] sha256,
			String dataname, 
			OSDXKeyObject key) throws Exception {
		
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
		
//		byte[] data = SecurityHelper.concat(sha256, md5);
//		byte[][] kk = SecurityHelper.getMD5SHA1SHA256(tc.getBytes());
//		byte[] md5sha1sha256 = kk[0];
//		byte[] md5 = kk[1];
//		byte[] sha1 = kk[2];
//		byte[] sha256 = kk[3];
		s.signaturebytes = key.sign(md5, sha1, sha256, s.signdatetime);
		
		s.pubkey = key.getPubKey();
		
//		SignoffElement es = SignoffElement.getSignoffElement(data, key);
//		Element epk = es.getChild("pubkey");
//		BigInteger mod = new BigInteger(SecurityHelper.HexDecoder.decode(epk.getChildText("modulus")));
//		BigInteger exp = new BigInteger(SecurityHelper.HexDecoder.decode(epk.getChildText("exponent")));
//		s.pubkey = new PublicKey(mod, exp);
//		s.signoffsha1 = SecurityHelper.HexDecoder.decode(es.getChildText("sha1"));
//		s.signaturebytes = SecurityHelper.HexDecoder.decode(es.getChildText("signaturebytes"));
		
//		System.out.println("md5            : "+SecurityHelper.HexDecoder.encode(s.datamd5,'\0',-1));
//		System.out.println("sha1           : "+SecurityHelper.HexDecoder.encode(s.datasha1,'\0',-1));
//		System.out.println("sha256         : "+SecurityHelper.HexDecoder.encode(s.datasha256,'\0',-1));
//		System.out.println("signdatetime   : "+OSDXKeyObject.datemeGMT.format((new Date(s.signdatetime)))+" long = "+s.signdatetime);
////		System.out.println("signoffsha1    : "+SecurityHelper.HexDecoder.encode(s.signoffsha1,'\0',-1));
//		System.out.println("signaturebytes : "+SecurityHelper.HexDecoder.encode(s.signaturebytes,'\0',-1));
//		
		return s;
	}
	
	
	public static Signature createSignature(File toSign, OSDXKeyObject key) throws Exception {
		byte[][] kk = SecurityHelper.getMD5SHA1SHA256(toSign);
		byte[] md5sha1sha256 = kk[0];
		byte[] md5 = kk[1];
		byte[] sha1 = kk[2];
		byte[] sha256 = kk[3];
		
		return createSignature(md5, sha1, sha256, toSign.getName(), key);
	}
	
	public static void createSignatureFile(File toSign, File output, OSDXKeyObject key) throws Exception {
		Signature s = createSignature(toSign, key);
		Document doc = Document.buildDocument(s.toElement());
		doc.writeToFile(output);
	}
		
	
	 
//	public Signature(File signature, File signedfile, KeyApprovingStore keystore) {
//		this.signature = signature;
//		this.signedFile = signedfile;
//		this.keystore = keystore;
//	}
//	
//	public Signature(File signature, File signedfile, AsymmetricKeyPair keypair) {
//		this.signature = signature;
//		this.signedFile = signedfile;
//		this.keypair = keypair;
//	}
//	
//	public static Signature createSignature(File tosign, AsymmetricKeyPair keypair) throws Exception {
//		File s = new File(tosign.getParentFile(), tosign.getName()+".asc");
//		return createSignature(tosign, s, keypair);
//	}
//	public static Signature createSignature(File tosign, File signaturefile, AsymmetricKeyPair keypair) throws Exception {
//		FileInputStream in = new FileInputStream(tosign);
//		ArmoredOutputStream out = new ArmoredOutputStream(new FileOutputStream(signaturefile));
//    	BCPGOutputStream bOut = new BCPGOutputStream(out);
//		PGPSignature sig = createSignature(in, keypair);
//		sig.encode(bOut);
//		bOut.close();
//		out.close();
//		
//		return new Signature(signaturefile, tosign, keypair);
//	}
//	
//	private static PGPSignature createSignature(InputStream in, AsymmetricKeyPair kp) throws Exception {
//		return null;
//		BufferedInputStream bin = null;
//		if(in instanceof BufferedInputStream) {
//			bin = (BufferedInputStream)in;
//		}
//		else {
//			bin = new BufferedInputStream(in);
//		}
//    	PGPPrivateKey pgpPrivKey = kp.getPGPPrivateKey();
//    	PGPPublicKey pgpPubKey = kp.getPGPPublicKey();
//    	PGPSignatureGenerator sGen = new PGPSignatureGenerator(
//    			pgpPubKey.getAlgorithm(), //HT 20.02.2011 @beboe: warum der pubkey-algo??? 
////    			pgpPrivKey.getKey().getAlgorithm(), //HT 20.02.2011 warum geht das hier nicht???
//    			PGPUtil.SHA1,
//    			"BC"
//    		);
//    	sGen.initSign(PGPSignature.BINARY_DOCUMENT, pgpPrivKey);
//    	byte[] buff = new byte[512];
//    	int read = 0;
//    	while ((read=in.read(buff)) != -1) {
//    		sGen.update(buff, 0, read);
//    	}
//    	return sGen.generate();
//    }
	
	public PublicKey getPubKey() {
		return pubkey;
	}
	
	public boolean tryVerificationFile(File f) throws Exception {
		FileInputStream in = new FileInputStream(f);
		BufferedInputStream bin = new BufferedInputStream(in);
		boolean verified = tryVerificationMD5SHA1SHA256(bin);
		in.close();
		
		return verified;
		
	}
	
	public boolean tryVerificationMD5SHA1SHA256(byte[] in) throws Exception {
		return tryVerificationMD5SHA1SHA256(new ByteArrayInputStream(in));
	}
	
	public boolean tryVerificationMD5SHA1SHA256(InputStream in) throws Exception {
		byte[][] kk = SecurityHelper.getMD5SHA1SHA256(in);
		byte[] md5sha1sha256 = kk[0];
		byte[] md5 = kk[1];
		byte[] sha1 = kk[2];
		byte[] sha256 = kk[3];
		
//		System.out.println("md5            : "+SecurityHelper.HexDecoder.encode(md5,'\0',-1));
//		System.out.println("sha1           : "+SecurityHelper.HexDecoder.encode(sha1,'\0',-1));
//		System.out.println("sha256         : "+SecurityHelper.HexDecoder.encode(sha256,'\0',-1));
//		System.out.println("signdatetime   : "+OSDXKeyObject.datemeGMT.format((new Date(signdatetime)))+" long = "+signdatetime);
		
		
		boolean ok = pubkey.verify(
				signaturebytes, 
				md5, 
				sha1, 
				sha256, 
				signdatetime
			);
		return ok;
		
//		ak.verify(
//				signature, 
//				md5,
//				sha1,
//				sha256,
//				ll
//			)
//		);
		
//		byte[] concat = SecurityHelper.getSHA256MD5(in);
//		byte[] sha256bytes = Arrays.copyOfRange(concat, 0, 32);
//		if (!Arrays.equals(sha256bytes, datasha256)) {
//			System.out.println("|"+SecurityHelper.HexDecoder.encode(sha256bytes, '\0',-1)+"|");
//			System.out.println("|"+SecurityHelper.HexDecoder.encode(datasha256, '\0',-1)+"|");
//			System.out.println("sha256 does NOT match.");
//			return false;
//		}
//		byte[] md5 = Arrays.copyOfRange(concat, 32, concat.length);
//		if (!Arrays.equals(md5, datamd5)) {
//			System.out.println("|"+SecurityHelper.HexDecoder.encode(md5, '\0',-1)+"|");
//			System.out.println("|"+SecurityHelper.HexDecoder.encode(datamd5, '\0',-1)+"|");
//			System.out.println("md5 does NOT match.");
//			return false;
//		}
//		
//		//check signature
//		byte[] sha1 = SecurityHelper.getSHA1(concat);
//		System.out.println("sha1        : "+SecurityHelper.HexDecoder.encode(sha1, '\0', -1));
//		System.out.println("signoffsha1 : "+SecurityHelper.HexDecoder.encode(signoffsha1, '\0', -1));
//		
//		if (!Arrays.equals(sha1, signoffsha1)) return false;
//		return pubkey.verify(signaturebytes, sha1);
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
			OSDXKeyObject key = store.getAllKeys().firstElement();
			
			System.out.println("\n\ncreating signature");
			Signature.createSignatureFile(toSign, output, key);
			
			
			System.out.println("\n\nverifing signature:");
			Signature s = Signature.fromFile(output);
			boolean v = s.tryVerificationFile(toSign);
			if (v) {
				System.out.println("signature verified.");
			} else {
				System.out.println("signature NOT verified.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
