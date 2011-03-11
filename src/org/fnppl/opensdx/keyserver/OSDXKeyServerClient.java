package org.fnppl.opensdx.keyserver;

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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Vector;
import java.util.logging.Logger;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGInputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.crypto.RuntimeCryptoException;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.fnppl.opensdx.security.AsymmetricKeyPair;
import org.fnppl.opensdx.security.Identity;
import org.fnppl.opensdx.security.KeyLog;
import org.fnppl.opensdx.security.OSDXKeyObject;
import org.fnppl.opensdx.security.PublicKey;
import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.security.Signature;
import org.fnppl.opensdx.tsas.TsaServerRequest;
import org.fnppl.opensdx.tsas.TsaServerResponse;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;
import org.w3c.dom.ranges.RangeException;

public class OSDXKeyServerClient {

	private Socket socket = null;
	private long timeout = 2000;
	private String host = null;
	private int port = -1;
	
	public OSDXKeyServerClient(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public void connect() throws Exception {
		socket = new Socket(host, port);
		if (socket.isConnected()) {
			System.out.println("Connection established.");
		} else {
			System.out.println("ERROR: Connection to server could NOT be established!");
		}
	}
	
	public void close() throws Exception {
		if (socket!=null)
			socket.close();
	}
	
	// 1. Ich, als fremder user, möchte beim keyserver (z.B. keys.fnppl.org) den/die (MASTER) pubkey(s) zu der identity thiess@finetunes.net suchen können
	public Vector<PublicKey> requestMasterPubKeys(final String idemail) throws Exception {
		connect();
		System.out.println("OSDXKeyServerClient | start requestMasterPubKeys");

		//request
		OSDXKeyServerClientRequest req = new OSDXKeyServerClientRequest();
		req.setRequest("GET /masterpubkeys HTTP/1.1");
		req.addHeaderValue("Host", host);
		req.addHeaderValue("Identity",idemail);
		req.send(socket);
		
		//processing response
	    System.out.println("OSDXKeyServerClient | waiting for response");
	    OSDXKeyServerClientResponse re = OSDXKeyServerClientResponse.fromStream(socket.getInputStream(), timeout);
	    close();
	    
	    if (re==null) throw new RuntimeException("ERROR: Keyserver does not respond.");
	    if (re.doc!=null) {
	    	Element e = re.doc.getRootElement();
	    	if (!e.getName().equals("pubkeys")) throw new RuntimeException("ERROR: Wrong format in keyserver's response");
	    	Vector<PublicKey> ret = new Vector<PublicKey>();
	 	    Vector<Element> pks = e.getChildren("pubkey");
	 	    for (Element pk : pks) {
	 	    	BigInteger modulus = new BigInteger(SecurityHelper.HexDecoder.decode(pk.getChildText("modulus")));
	 	    	BigInteger exponent = new BigInteger(SecurityHelper.HexDecoder.decode(pk.getChildText("exponent")));
	 	    	ret.add(new PublicKey(modulus, exponent));
	 	    }
	    	return ret;	 
	    }
	    return null;

	    //example response

		//HTTP/1.1 200 OK
		//Server: OSDX KeyServer v0.1
		//Response: masterpubkeys
		//Identity: bla@fnppl.org
		//Content-Type: text/xml
		//Content-Length: 1024
		//
		//<?xml version= "1.0" encoding="UTF-8"?>
		//<pubkeys>
		//  <pubkey>
		//    <algo>RSA</algo>
		//    <bits>3072</bits>
		//    <modulus>00AEAE639723B8E18EF2452C2457B92F91504FE7FC42FAD34E17D684B71CAA7DD277B876553F73F9D170326E8B7842BAEFB8D7A9FDBE84E5516EBCE93752CC6F33D382EC19799AB66AC19442F7E50DBCB266541319BE2E12D169EFCE9119BA3D196E6CA0CB48D3EECB69FD81C5C48E719F9B309B194397B668095BB797947AB8A052C134207D9774F3B84BBB75A19F3A3E99CAD65D8C05FC6E84FEA2092861251D2DD5EBCF011E24A01D9CD4E5EEA62FDF5402B46A3B6BDE22B66DD9D5EE72679CC65C73F4112EC2A8E89F18F4521AB1E39AEA919C40874FB25434527AA4687CE994AEECBE26010B6B9E239B5DBC5334131EBE3F58D6356516654D27ABCB5D80AE1245FB1AD05D621C7FBAA36ADCD1AF0FC4B89060907DCDEF8570D55BD7B435CE88AEB4F94BCF0411FE2499408FD5A987AFA3A6EFFD4F2B4E2FAE25A4A2269C0493F0A96F7DAB1E24E17EC1799FF664B74A020D17DD76169A91540084CFE1B988778B9DE515205C83A0259121749EF86329761394C7C47D90B518CD11F2928539</modulus>
		//    <exponent>0x010001</exponent>
		//  </pubkey>
		//</pubkeys>

	    
	}
	
	//2. Ich, als fremder user, möchte beim keyserver die weiteren identities (identity-details) zu einem pubkey bekommen können
	public Vector<Identity> requestIdentities(String keyid) throws Exception {
		connect();
		System.out.println("OSDXKeyServerClient | start requestMasterPubKeys");

		//request
		OSDXKeyServerClientRequest req = new OSDXKeyServerClientRequest();
		req.setRequest("GET /identities HTTP/1.1");
		req.addHeaderValue("Host", host);
		req.addHeaderValue("KeyID", keyid);
		req.send(socket);
		
		//processing response
	    System.out.println("OSDXKeyServerClient | waiting for response");
	    OSDXKeyServerClientResponse re = OSDXKeyServerClientResponse.fromStream(socket.getInputStream(), timeout);
	    close();
	    
	    if (re==null) throw new RuntimeException("ERROR: Keyserver does not respond.");
	    if (re.doc!=null) {
	    	Element e = re.doc.getRootElement();
	    	if (!e.getName().equals("identities")) {
	    		re.doc.output(System.out);
	    		throw new RuntimeException("ERROR: Wrong format in keyserver's response");
	    	}
	    	Vector<Identity> ret = new Vector<Identity>();
	 	    Vector<Element> eid = e.getChildren("identity");
	 	    for (Element id : eid) {
	 	    	ret.add(Identity.fromElement(id));
	 	    }
	    	return ret;	 
	    }
	    return null;
		
		//	    //example response
		//
		//HTTP/1.1 200 OK
		//Server: OSDX KeyServer v0.1
		//Response: identities
		//Identity: bla@fnppl.org
		//Content-Type: text/xml
		//Content-Length: 1024
		//
		//<?xml version= "1.0" encoding="UTF-8"?>
		//<identities>
		//  <Identity>
		//    ...
		//  </Identitiy>
		//</identities>
	}
	
	//3. Ich, als fremder user, möchte beim keyserver den aktuellen (beim keyserver bekannten) status zu einem pubkey bekommen können (valid/revoked/etc.)
	public String[] requestKeyStatus(String keyid) throws Exception {
		connect();
		//request
		System.out.println("OSDXKeyServerClient | start request keystatus");
		OSDXKeyServerClientRequest req = new OSDXKeyServerClientRequest();
		req.setRequest("GET /keystatus HTTP/1.1");
		req.addHeaderValue("Host", host);
		req.addHeaderValue("KeyID", keyid);
		req.send(socket);
		
		//processing response
	    System.out.println("OSDXKeyServerClient | waiting for response");
	    OSDXKeyServerClientResponse re = OSDXKeyServerClientResponse.fromStream(socket.getInputStream(), timeout);
	    close();
	    
	    if (re==null) throw new RuntimeException("ERROR: Keyserver does not respond.");
	    if (re.doc!=null) {
	    	Element e = re.doc.getRootElement();
	    	if (!e.getName().equals("keyid_keystatus")) throw new RuntimeException("ERROR: Wrong format in keyserver's response");
	    	String status = e.getChildText("keystatus");
	    	String date = e.getChildText("keystatus_date");
	    	if (status!=null && date!=null) {
	    		//TODO verify signature, really signed by rootkey from server?
	    		//Signature sig = Signature.fromElement(e.getChild("signature"));
		    	return new String[] {status,date};	
	    	}
	    }
	    return null;
	    
	    //example response

//	    HTTP/1.1 200 OK
//	    Server: OSDX KeyServer v0.1
//	    Response: keystatus
//		KeyID: 001152352352643376@keys.fnppl.org
//	    Content-Type: text/xml
//	    Content-Length: 1024
//
//	    <?xml version= "1.0" encoding="UTF-8"?>
//	    <keyid_keystatus>
//	    	<keyid>001152352352643376@keys.fnppl.org</keyid>
//	     	<keystatus>valid</keystatus>  <!--valid / revoked / approve pending -->
//			<keystatus_date>2011-02-21 00:00:00 GMT+00:00</keystatus_date>
//			<sha1localproof>85:31:63:9E:83:F8:94:BA:39:91:6E:2E:B5:86:1C:57:1D:41:9A:90</sha1localproof>	    
//			<signature>
//				<data>
//					<md5>kalksdlkad</md5>
//					<sha1>adadasdasd</sha1>
//					<sha256>adadasdasd</sha256>
//					<signdatetime>2011-01-01 23:20:00 GMT+00:00</signdatetime><!-- MUST -->
//					<dataname>keyid and keystatus and date</dataname>
//				</data>
//				<signoff>
//					<keyid>kjakdjadkjajd@keys.fnppl.org</keyid>
//					<pubkey>
//						<algo>RSA</algo>
//						<bits>3072</bits>
//						<modulus></modulus>
//						<exponent></exponent>
//					</pubkey>
//					<signaturebytes>asdasd</signaturebytes><!-- as hex-string with ":" or " " separation... looks nicer... -->
//				</signoff>
//			</signature>	    
//	    </keyid_keystatus>
	    
	}
	
	//4. Ich, als fremder user, möchte beim keyserver die keylogs eines (beliebigen) pubkeys bekommen können
	public Vector<KeyLog> requestKeyLogs(String keyid) throws Exception {
		connect();
		//request
		System.out.println("OSDXKeyServerClient | start request keylogs");
		OSDXKeyServerClientRequest req = new OSDXKeyServerClientRequest();
		req.setRequest("GET /keylogs HTTP/1.1");
		req.addHeaderValue("Host", host);
		req.addHeaderValue("KeyID", keyid);
		req.send(socket);
		
		//processing response
	    System.out.println("OSDXKeyServerClient | waiting for response");
	    OSDXKeyServerClientResponse re = OSDXKeyServerClientResponse.fromStream(socket.getInputStream(), timeout);
	    close();
	    
	    if (re==null) throw new RuntimeException("ERROR: Keyserver does not respond.");
	    if (re.doc!=null) {
	    	Element e = re.doc.getRootElement();
	    	if (!e.getName().equals("keylogs")) throw new RuntimeException("ERROR: Wrong format in keyserver's response");
	    	Vector<Element> ekls = e.getChildren("keylog");
	    	Vector<KeyLog> vkl = new Vector<KeyLog>();
	    	for (Element ekl : ekls) {
	    		vkl.add(KeyLog.fromElement(ekl));
	    	}
	    	return vkl;
	    }
	    return null;
		
	    //example response

//	    HTTP/1.1 200 OK
//	    Server: OSDX KeyServer v0.1
//	    Response: keylogs
//		KeyID: 001152352352643376@keys.fnppl.org
//	    Content-Type: text/xml
//	    Content-Length: 1024
//
//	    <?xml version= "1.0" encoding="UTF-8"?>
//	    <keylogs>
//	    	<keylog>
//	    		...
//	    	</keylog>
//	    	<sha1localproof>bla</sha1localproof>
//	    	<signature>
//	    		...
//	    	</signature>
//	    </keylogs>
	    		
	}
	
	//5. Ich, als fremder user, möchte beim keyserver die weiteren pubkeys zu einem parent-pubkey (MASTER) bekommen können
	public Vector<PublicKey> requestSubKeys(String masterkeyid) throws Exception {
		connect();
		System.out.println("OSDXKeyServerClient | start requestSubKeys");

		//request
		OSDXKeyServerClientRequest req = new OSDXKeyServerClientRequest();
		req.setRequest("GET /subkeys HTTP/1.1");
		req.addHeaderValue("Host", host);
		req.addHeaderValue("KeyID",masterkeyid);
		req.send(socket);
		
		//processing response
	    System.out.println("OSDXKeyServerClient | waiting for response");
	    OSDXKeyServerClientResponse re = OSDXKeyServerClientResponse.fromStream(socket.getInputStream(), timeout);
	    close();
	    
	    if (re==null) throw new RuntimeException("ERROR: Keyserver does not respond.");
	    if (re.doc!=null) {
	    	Element e = re.doc.getRootElement();
	    	if (!e.getName().equals("pubkeys")) throw new RuntimeException("ERROR: Wrong format in keyserver's response");
	    	Vector<PublicKey> ret = new Vector<PublicKey>();
	 	    Vector<Element> pks = e.getChildren("pubkey");
	 	    for (Element pk : pks) {
	 	    	BigInteger modulus = new BigInteger(SecurityHelper.HexDecoder.decode(pk.getChildText("modulus")));
	 	    	BigInteger exponent = new BigInteger(SecurityHelper.HexDecoder.decode(pk.getChildText("exponent")));
	 	    	ret.add(new PublicKey(modulus, exponent));
	 	    }
	    	return ret;	 
	    }
	    return null;
		
	    //example response

//		HTTP/1.1 200 OK
//		Server: OSDX KeyServer v0.1
//		Response: subkeys
//		Parent KeyID: 001152352352643376@keys.fnppl.org
//		Content-Type: text/xml
//		Content-Length: 1024
//		
//		<?xml version= "1.0" encoding="UTF-8"?>
//		<pubkeys>
//		  <pubkey>
//		    <algo>RSA</algo>
//		    <bits>3072</bits>
//		    <modulus>00AEAE639723B8E18EF2452C2457B92F91504FE7FC42FAD34E17D684B71CAA7DD277B876553F73F9D170326E8B7842BAEFB8D7A9FDBE84E5516EBCE93752CC6F33D382EC19799AB66AC19442F7E50DBCB266541319BE2E12D169EFCE9119BA3D196E6CA0CB48D3EECB69FD81C5C48E719F9B309B194397B668095BB797947AB8A052C134207D9774F3B84BBB75A19F3A3E99CAD65D8C05FC6E84FEA2092861251D2DD5EBCF011E24A01D9CD4E5EEA62FDF5402B46A3B6BDE22B66DD9D5EE72679CC65C73F4112EC2A8E89F18F4521AB1E39AEA919C40874FB25434527AA4687CE994AEECBE26010B6B9E239B5DBC5334131EBE3F58D6356516654D27ABCB5D80AE1245FB1AD05D621C7FBAA36ADCD1AF0FC4B89060907DCDEF8570D55BD7B435CE88AEB4F94BCF0411FE2499408FD5A987AFA3A6EFFD4F2B4E2FAE25A4A2269C0493F0A96F7DAB1E24E17EC1799FF664B74A020D17DD76169A91540084CFE1B988778B9DE515205C83A0259121749EF86329761394C7C47D90B518CD11F2928539</modulus>
//		    <exponent>0x010001</exponent>
//		  </pubkey>
//		  <sha1localproof>bla</sha1localproof>
//	      <signature>
//	    	...
//	      </signature>
//		</pubkeys>
		
	}
	
	//   1. Ich, als user, möchte auf dem keyserver meinen MASTER-pubkey ablegen können
	//   includes  2. Ich, als user, möchte, daß der keyserver meinen MASTER-pubkey per email-verifikation (der haupt-identity) akzeptiert (sonst ist der status pending oder so -> erst, wenn die email mit irgendeinem token-link drin aktiviert wurde, wird der pubkey akzeptiert)
	public boolean putMasterKey(PublicKey masterkey, Identity id) throws Exception {
		connect();
		System.out.println("OSDXKeyServerClient | start put master key: "+masterkey.getKeyID());

		//request
		OSDXKeyServerClientRequest req = new OSDXKeyServerClientRequest();
		req.setRequest("POST /masterkey HTTP/1.1");
		req.addHeaderValue("Host", host);
		req.addHeaderValue("Identity", id.getEmail());
		req.addHeaderValue("KeyID",masterkey.getKeyID());
		Element e = new Element("masterpubkey");
		e.addContent(masterkey.getSimplePubKeyElement());
		e.addContent(id.toElement());
		req.setContentElement(e);
		req.send(socket);
		
		//processing response
		System.out.println("OSDXKeyServerClient | waiting for response");
		InputStream in = socket.getInputStream();
		OSDXKeyServerClientResponse re = OSDXKeyServerClientResponse.fromStream(in, timeout);
		close();
		
		if (re==null) throw new RuntimeException("ERROR: Keyserver does not respond.");
		close();
		if (re.status.endsWith("OK")) return true;
		return false;
		    
		//example response

		//HTTP/1.1 200 OK
		//Server: OSDX KeyServer v0.1
		//Response: masterkey
		//KeyID: 001152352352643376@keys.fnppl.org
		//Identitiy: bla@fnppl.org
		//Message: please authenticate via email
		//
		
		
		//example request
		
//		POST /masterkey HTTP/1.1
//		Host: keys.fnppl.org
//		Identity: bla@fnppl.org	
//		KeyID: 001152352352643376@keys.fnppl.org
//		Content-Type: text/xml
//		Content-Length: 23523
//
//		<masterpubkey>
//			<pubkey>
//				<algo>RSA</algo>
//				<bits>3072</bits>
//				<modulus>0x010001</modulus>
//				<exponent>425465254651654654984985644894</exponent>
//			</pubkey>
//			
//			<identity>
//				<email>jaja@kakak.nät</email><!-- MUST -->
//				<mnemonic></mnemonic><!-- SHOULD ; shorthandle for this identities-purpose "residency" or "work" or whatever -->
//				<phone>+44 99 00202021</phone><!-- COULD -->
//				<country></country><!-- COULD -->
//				<region></region><!-- COULD -->
//				<postcode></postcode><!-- COULD -->		
//				<company></company><!-- COULD -->
//				<unit></unit><!-- COULD -->
//				<subunit></subunit><!-- COULD -->
//				
//				<function></function><!-- COULD ; function of that person -->
//				<surname></surname><!-- COULD -->
//				<middlename></middlename><!-- COULD -->
//				<name></name><!-- COULD -->
//				
//				<note></note><!-- COULD -->
//				<sha1>A7:DC:99:4D:64:7D:57:95:2A:4F:0F:D3:52:4E:29:6F:02:32:10:5A</sha1><!-- sha1 as hex of concat of all above fields (also empty ones) -->
//				<!-- please be aware of the exact order of these fields... -->
//				
//				<!-- this data is set on client-side -->
//				<datapath>
//					<step1>
//						<datasource>keys.fnppl.org</datasource><!-- keyserver or local -->
//						<datainsertdatetime>2011-02-21 00:00:00 GMT+00:00</datainsertdatetime>
//					</step1>
//					<step2>
//						<datasource>keys.fnppl.org</datasource><!-- step2 only valid when step1 != local -->
//						<datainsertdatetime>2011-02-21 00:00:00 GMT+00:00</datainsertdatetime>
//					</step2>
//				</datapath>
//		    </identity>
//		</masterpubkey>
		
	}
	
	//3. Ich, als user, möchte auf dem keyserver meinen REVOKE-key für meinen master-key abspeichern können (der sollte sogar nicht sichtbar für irgendwen sonst sein!!!)
	public boolean putRevokeKey(PublicKey revokekey, OSDXKeyObject relatedMasterKey) throws Exception {
		connect();
		System.out.println("OSDXKeyServerClient | start put revoke key");

		//request
		OSDXKeyServerClientRequest req = new OSDXKeyServerClientRequest();
		req.setRequest("POST /revokekey HTTP/1.1");
		req.addHeaderValue("Host", host);
		Element e = new Element("revokekey");
		e.addContent("masterkeyid", relatedMasterKey.getKeyModulusSHA1());
		e.addContent(revokekey.getSimplePubKeyElement());
		 //signoff with own masterkey
		Vector<Element> toProof = new Vector<Element>();
		toProof.add(e);
		byte[] sha1 = SecurityHelper.getSHA1LocalProof(toProof);
		byte[][] md5sha1sha256 = SecurityHelper.getMD5SHA1SHA256(sha1);
		Signature sig = Signature.createSignature(md5sha1sha256[1], md5sha1sha256[2], md5sha1sha256[3], "masterkeyid and pubkey", relatedMasterKey);
		e.addContent("sha1localproof", SecurityHelper.HexDecoder.encode(sha1, ':', -1));
		e.addContent(sig.toElement());
		req.send(socket);
		
		//processing response
		System.out.println("OSDXKeyServerClient | waiting for response");
		OSDXKeyServerClientResponse re = OSDXKeyServerClientResponse.fromStream(socket.getInputStream(), timeout);
		close();
		
		if (re==null) throw new RuntimeException("ERROR: Keyserver does not respond.");
		if (re.status.endsWith("OK")) return true;
		return false;
		
		//example request
			
//		POST /revokekey HTTP/1.1
//		Host: keys.fnppl.org
//		Content-Type: text/xml
//		Content-Length: 235
//
//		<revokekey>
//			<masterkeyid>001152352352643376@keys.fnppl.org</masterkeyid>
//			<pubkey>
//				<algo>RSA</algo>
//				<bits>3072</bits>
//				<modulus>0x010001</modulus>
//				<exponent>425465254651654654984985644894</exponent>
//			</pubkey>
//			<sha1localproof>85:31:63:9E:83:F8:94:BA:39:91:6E:2E:B5:86:1C:57:1D:41:9A:90</sha1localproof>
//			<signature>
//				<data>
//					<md5>kalksdlkad</md5>
//					<sha1>adadasdasd</sha1>
//					<sha256>adadasdasd</sha256>
//					<signdatetime>2011-01-01 23:20:00 GMT+00:00</signdatetime><!-- MUST -->
//					<dataname>masterkeyid and pubkey</dataname>
//				</data>
//				<signoff>
//					<keyid>kjakdjadkjajd@keys.fnppl.org</keyid>
//					<pubkey>
//						<algo>RSA</algo>
//						<bits>3072</bits>
//						<modulus></modulus>
//						<exponent></exponent>
//					</pubkey>
//					<signaturebytes>asdasd</signaturebytes><!-- as hex-string with ":" or " " separation... looks nicer... -->
//				</signoff>
//			</signature>
//		</revokekey>
		
	}

	//   5. Ich, als user, möchte meine keylogs auf dem server ablegen können (ein löschen von keylogs ist NICHT möglich - für einen aktuellen status ist die "kette ist chronologisch abzuarbeiten")
	public boolean putKeyLog(KeyLog keylog) throws Exception {
		throw new RuntimeException("not implemented");
		//TODO why should i do that?
	
		//example request
		
//		POST / HTTP/1.1
//		Host: keys.fnppl.org
//		Request: keylogdelivery
//		Content-Type: text/xml
//		Content-Length: 235
//
//		<keylogdelivery>
//		  <keylog>
//		    <action>
//		      <date>2011-01-01 00:00:00 GMT+00:00</date>
//		      <ipv4>919191</ipv4>
//		      <ipv6>91921929</ipv6>
//		      <from>
//		        <keyid>kakaka@llalal.nät</keyid>
//		        <sha1fingerprint />
//		      </from>
//		      <to>
//		        <keyid>kakaka2@llalal.nät</keyid>
//		        <sha1fingerprint />
//		      </to>
//		      <approval>
//		        <of>
//		          <identity>
//		            <email>akakak@lalal</email>
//		            <function />
//		            <sha1 />
//		          </identity>
//		        </of>
//		      </approval>
//		      <datapath>
//		        <step1>
//		          <datasource>keys.fnppl.org</datasource>
//		          <datainsertdatetime>2011-02-21 00:00:00 GMT+00:00</datainsertdatetime>
//		        </step1>
//		        <step2>
//		          <datasource>keys.fnppl.org</datasource>
//		          <datainsertdatetime>2011-02-21 00:00:00 GMT+00:00</datainsertdatetime>
//		        </step2>
//		      </datapath>
//		    </action>
//		    <sha1localproof>85:31:63:9E:83:F8:94:BA:39:91:6E:2E:B5:86:1C:57:1D:41:9A:90</sha1localproof>
//		    <signature>
//		      <data>
//		        <sha1>85:31:63:9E:83:F8:94:BA:39:91:6E:2E:B5:86:1C:57:1D:41:9A:90</sha1>
//		        <signdatetime>2011-03-09 09:27:44 GMT+00:00</signdatetime>
//		        <dataname>localsignoff for sha1 85:31:63:9E:83:F8:94:BA:39:91:6E:2E:B5:86:1C:57:1D:41:9A:90</dataname>
//		      </data>
//		      <signoff>
//		        <keyid>BEEE542006AF8301096BF0305AB4632E9982AA94</keyid>
//		        <pubkey>
//		          <algo>RSA</algo>
//		          <bits>3072</bits>
//		          <modulus>00CBFC6AE1B8C3B2E31DF52214F1CFB4EFBB9E77CAF63A61F85B8AAD1BB43A3C138F1FC1C8E7D6F3368E7985AC1719A07F77F16C4D26E7BCC0A3EE079F9132BA1ACA7E9E279852F6D2821EE3FCD9C3519D15B7DA34345597D1EA38B716891793E76D4C34270257C010E03B4D8F39BE1025931B6D104C4E52D542DA7A8A1CCD2368C3075D5C71DEE146FBBB77A0DD72DB14A444D556B12681497565B7D82B5F2DA30B667AE8D8E364993E9F2746AB3AA92FBF2B0A8376870CC9C890805EA27541B997B9196916DBAFF7D3F180A2DBD4D5A54F23006FEF4544345A0FD390CCEEEDB4C8C505DF7D11C4A8687E9BF865F1A28008355B17EDB31509A5F122856AB11DDC56A4D51F02A79908E2E89D0EA7EE80DEEAE2119C89D9C25BEDE96386504CEA40C95DCF630E78514FB28F8F9EC9BC280B4F0D2C798988B8E27348AAEA94A3FA58BDE172BE6D07D1FDFD493C632DCC2E45B79353231A92A1CD3924DF5DFB0AB20D889D89FCD26F0A32421CC669BC89F239FDC91636A638B55A784BF5846CFAD007</modulus>
//		          <exponent>010001</exponent>
//		        </pubkey>
//		        <signaturebytes>19:17:23:BC:6A:0D:85:E2:78:77:89:62:81:4C:EB:15:9C:D9:BF:24:E4:95:48:B0:20:62:57:C1:85:4F:34:BE:AD:67:0F:6E:C9:9A:41:F1:BC:2F:80:4A:B5:2E:77:0B:80:1F:3E:B3:54:4A:FD:1A:2F:79:E7:76:6E:39:4C:0C:55:DB:C9:FC:E4:C8:49:F4:7D:02:2D:E3:D4:03:B3:47:70:7E:92:EB:9E:3B:E9:B8:BF:BE:87:2F:E3:C6:94:C9:42:84:D8:EB:49:C3:35:89:F8:A5:04:5E:41:0A:01:C2:93:96:90:E2:E6:60:53:AA:03:06:3E:EC:C9:7F:71:A9:84:84:51:A7:A4:EA:B0:D2:98:8B:87:7C:02:E5:91:93:0B:78:86:B5:5B:8D:05:44:EB:68:EA:1E:17:A4:60:49:49:F3:D7:27:1D:72:A5:BE:0C:31:B0:90:CE:17:1F:9F:84:27:8D:38:28:7C:A3:EA:BD:51:04:85:B1:82:AF:EE:62:16:CC:0D:14:F7:2A:7D:32:93:E8:36:80:E0:2D:6F:19:90:B9:22:10:84:C0:37:C3:FE:B8:21:FE:39:77:D9:41:D8:E7:86:68:F3:9E:8F:24:40:46:02:4F:71:49:DD:FD:7D:6F:2D:75:0F:5E:EA:FB:AB:A5:93:2E:FE:C8:1F:A9:D5:D6:C6:6D:E6:84:AF:BE:4B:01:B8:94:42:1B:53:69:BE:80:45:C8:43:06:45:0C:07:4E:FE:D4:35:92:38:EA:14:65:F8:26:71:82:26:B5:EC:38:24:90:D0:CD:50:1B:84:B5:B6:4F:46:B4:F8:FF:90:99:83:1F:9D:CA:7F:7C:21:46:10:EC:A5:3C:F2:2A:C2:05:9E:56:50:2A:56:7D:57:AA:A5:0D:4F:C8:0A:27:34:25:DA:1B:03:9B:E2:45:FC:55:B6:82:D7:8E:80:C9:4E:BE:33:7A:40:B5:03:73:30:FF:49:A0:FE:98:73:27:24:EA:20:47:DC:43:03</signaturebytes>
//		      </signoff>
//		    </signature>
//		</keylogdelivery>
		
	}
	
	//TODO
	//   4. Ich, als user, möchte eigentlich, daß alle meine Aktionen auf meinem MASTER-key - bzw. allen meiner keys durch einen entsprechenden signature-proof des entsprechenden private-keys validierbar sind
	//   6. Ich, als user, möchte alle kommunikation vom/zum keyserver mit einem vom keyserver definierten root-MASTER-key approveden key signiert wissen
	//   7. Ich, als user, möchte diese keyserver-root-keys vordefiniert in meiner openSDX-suite finden, aber auch simpelst selbst nachrüsten können

	
	public static void main(String[] args) throws Exception {
		//testing
		OSDXKeyServerClient c = new OSDXKeyServerClient("localhost", 8889);
		

//		Vector<PublicKey> keys = c.requestMasterPubKeys("test@fnppl.org");
//		for (PublicKey k : keys) {
//			System.out.println("public key: "+k.getKeyID());
//		}

		
//		Vector<Identity> ids = c.requestIdentities("7610FF13E234ED7694333FF67F312E0DEA45AC99");
//		for (Identity id : ids) {
//			System.out.println("identity email: "+id.getEmail());
//		}

		
//		Vector<KeyLog> keylogs = c.requestKeyLogs("85D9EB452CA5CD270CA9EC73724ACDAC9E6A6281@LOCAL");
//		System.out.println("received "+keylogs.size()+" keylogs");

		
//		Vector<PublicKey> keys = c.requestSubKeys("85D9EB452CA5CD270CA9EC73724ACDAC9E6A6281@LOCAL");
//		for (PublicKey k : keys) {
//			System.out.println("public sub key: "+k.getKeyID());
//		}

		
//		AsymmetricKeyPair kp =  AsymmetricKeyPair.generateAsymmetricKeyPair();
//		OSDXKeyObject key = OSDXKeyObject.fromKeyPair(kp);
//		Identity id = Identity.newEmptyIdentity();
//		id.setEmail("test@fnppl.org");


//		boolean ok = c.putMasterKey(key.getPubKey(), id);
//		System.out.println("put master key: "+(ok?"OK":"FAILED"));

	
		String[] status = c.requestKeyStatus("C930CEEF52E4D6808A4253AC2C0EF5F6E578C603");
		System.out.println("key status: "+status[0]+" from date: "+status[1]);


//		boolean ok2 = c.putRevokeKey(key.getPubKey(), key);
//		System.out.println("put revoke key: "+(ok2?"OK":"FAILED"));

		
	}
	
}
