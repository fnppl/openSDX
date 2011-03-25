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

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.util.*;

import org.fnppl.opensdx.security.*;
import org.fnppl.opensdx.tsas.*;
import org.fnppl.opensdx.xml.*;

public class OSDXKeyServerClient {
	private Socket socket = null;
	private long timeout = 2000;
	private String host = null;
	private int port = -1;
	private String message = null;
	
	public OSDXKeyServerClient(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public boolean connect() throws Exception {
		socket = new Socket(host, port);
		if (socket.isConnected()) {
			System.out.println("Connection established.");
			return true;
		} else {
			System.out.println("ERROR: Connection to server could NOT be established!");
			return false;
		}
	}

	public void close() throws Exception {
		if (socket != null)
			socket.close();
	}
	
	public String getMessage() {
		return message;
	}
	public OSDXKeyServerClientResponse send(OSDXKeyServerClientRequest req) throws Exception {
		if (!connect()) {
			throw new RuntimeException("ERROR: Can not connect to keyserver.");
		}
		System.out.println("OSDXKeyServerClient | start "+req.getURI());
		
		System.out.println("--- sending ---");
		req.toOutput(System.out);
		System.out.println("\n--- end of sending ---");
		
		req.send(socket);
		
		//processing response
	    System.out.println("OSDXKeyServerClient | waiting for response");
	    BufferedInputStream bin = new BufferedInputStream(socket.getInputStream());
	    
	    OSDXKeyServerClientResponse re = OSDXKeyServerClientResponse.fromStream(bin, timeout);
	    close();
	    
	    if(re == null) {
	    	throw new RuntimeException("ERROR: Keyserver does not respond.");
	    }
	    return re;
	}
	
	// 1. Ich, als fremder user, möchte beim keyserver (z.B. keys.fnppl.org) den/die (MASTER) pubkey(s) zu der identity thiess@finetunes.net suchen können
	public Vector<String> requestMasterPubKeys(final String idemail, Vector<OSDXKeyObject> trustedKeys) throws Exception {
		OSDXKeyServerClientRequest req = OSDXKeyServerClientRequest.getRequestMasterPubKeys(host, idemail);
		OSDXKeyServerClientResponse resp = send(req);
		
		Element e = resp.doc.getRootElement();
		if (!e.getName().equals("masterpubkeys_response")) {
			throw new RuntimeException("ERROR: Wrong format in keyserver's response");
		}
		Vector<String> ret = new Vector<String>();
		Element er = e.getChild("related_keys");
		if (er!=null) {
			Vector<Element> keyids = er.getChildren("keyid");
			for (Element k : keyids) {
				ret.add(k.getText());
			}
			//verify signature
			boolean verify = SecurityHelper.checkElementsSHA1localproofAndSignature(e, trustedKeys);
			if (!verify) {
				throw new RuntimeException("ERROR at requestKeyStatus: signature could NOT be verfied.");
			}
		}
		return ret;
	}
	
	
//	public Vector<OSDXKeyObject> requestPubKeys(final String idemail) throws Exception {
//		OSDXKeyServerClientRequest req = OSDXKeyServerClientRequest.getRequestPubKeys(host, idemail);
//		OSDXKeyServerClientResponse resp = send(req);
//		
//		Element e = resp.doc.getRootElement();
//		if (!e.getName().equals("pubkeys")) {
//			throw new RuntimeException("ERROR: Wrong format in keyserver's response");
//		}
//		Vector<OSDXKeyObject> ret = new Vector<OSDXKeyObject>();
//		Vector<Element> pks = e.getChildren("keypair");
//		for (Element pk : pks) {
//			OSDXKeyObject key = OSDXKeyObject.fromElement(pk);
//			ret.add(key);
//		}
//		return ret;
//	}
	
	
	//2. Ich, als fremder user, möchte beim keyserver die weiteren identities (identity-details) zu einem pubkey bekommen können
	public Vector<Identity> requestIdentities(String keyid, Vector<OSDXKeyObject> trustedKeys) throws Exception {
		OSDXKeyServerClientRequest req = OSDXKeyServerClientRequest.getRequestIdentities(host, keyid);
		OSDXKeyServerClientResponse resp = send(req);
		
	    Element e = resp.doc.getRootElement();
	    if (!e.getName().equals("identities_response")) {
	    	resp.doc.output(System.out);
	    	throw new RuntimeException("ERROR: Wrong format in keyserver's response");
	    }
	    Vector<Identity> ret = new Vector<Identity>();
	 	Vector<Element> eid = e.getChildren("identity");
	 	for (Element id : eid) {
	 		ret.add(Identity.fromElement(id));
	 	}
	 	if (e.getChild("identity")!=null) {
		 	//verify signature
			boolean verify = SecurityHelper.checkElementsSHA1localproofAndSignature(e, trustedKeys);
			if (!verify) {
				throw new RuntimeException("ERROR at requestKeyStatus: signature could NOT be verfied.");
			}
		    return ret;
	 	}
	 	return null;
	} 
	
	//3. Ich, als fremder user, möchte beim keyserver den aktuellen (beim keyserver bekannten) status zu einem pubkey bekommen können (valid/revoked/etc.)
	public KeyStatus requestKeyStatus(String keyid, Vector<OSDXKeyObject> trustedKeys) throws Exception {
		OSDXKeyServerClientRequest req = OSDXKeyServerClientRequest.getRequestKeyStatus(host, keyid);
		OSDXKeyServerClientResponse resp = send(req);
		if (resp==null || resp.status == null) return null;
		
		Element e = resp.doc.getRootElement();
		if (!e.getName().equals("keystatus_response")) {
			throw new RuntimeException("ERROR: Wrong format in keyserver's response");
		}
		Element eks = e.getChild("keystatus");
		if (eks != null) {
			KeyStatus ks = KeyStatus.fromElement(eks);
			//verify signature
			boolean verify = SecurityHelper.checkElementsSHA1localproofAndSignature(e, trustedKeys);
			if (!verify) {
				throw new RuntimeException("ERROR at requestKeyStatus: signature could NOT be verfied.");
			}
			return ks;
		}
		return null;
	}
	
	//4. Ich, als fremder user, möchte beim keyserver die keylogs eines (beliebigen) pubkeys bekommen können
	public Vector<KeyLog> requestKeyLogs(String keyid, Vector<OSDXKeyObject> trustedKeys) throws Exception {
		OSDXKeyServerClientRequest req = OSDXKeyServerClientRequest.getRequestKeyLogs(host, keyid);
		OSDXKeyServerClientResponse resp = send(req);
		if (resp==null || resp.status == null) return null;
		
		Element e = resp.doc.getRootElement();
		if (!e.getName().equals("keylogs_response")) {
			throw new RuntimeException("ERROR: Wrong format in keyserver's response");
		}
		Vector<Element> ekls = e.getChildren("keylog");
		Vector<KeyLog> vkl = new Vector<KeyLog>();
		for (Element ekl : ekls) {
			vkl.add(KeyLog.fromElement(ekl));
		}
		//verify signature
		boolean verify = SecurityHelper.checkElementsSHA1localproofAndSignature(e, trustedKeys);
		if (!verify) {
			throw new RuntimeException("ERROR at requestKeyStatus: signature could NOT be verfied.");
		}
		return vkl;
	}
	
	//5. Ich, als fremder user, möchte beim keyserver die weiteren pubkeys zu einem parent-pubkey (MASTER) bekommen können
	public Vector<String> requestSubKeys(String masterkeyid, Vector<OSDXKeyObject> trustedKeys) throws Exception {
		OSDXKeyServerClientRequest req = OSDXKeyServerClientRequest.getRequestSubkeys(host, masterkeyid);
		OSDXKeyServerClientResponse resp = send(req);

		Element e = resp.doc.getRootElement();
		if (!e.getName().equals("subkeys_response")) {
			throw new RuntimeException("ERROR: Wrong format in keyserver's response");
		}
		Vector<String> ret = new Vector<String>();
		Vector<Element> keyids = e.getChildren("keyid");
		for (Element k : keyids) {
			ret.add(k.getText());
		}		
		//verify signature
		boolean verify = SecurityHelper.checkElementsSHA1localproofAndSignature(e, trustedKeys);
		if (!verify) {
			throw new RuntimeException("ERROR at requestKeyStatus: signature could NOT be verfied.");
		}
		return ret;
	}
	
	public OSDXKeyObject requestPublicKey(String keyid, Vector<OSDXKeyObject> trustedKeys) throws Exception {
		OSDXKeyServerClientRequest req = OSDXKeyServerClientRequest.getRequestPublicKey(host, keyid);
		OSDXKeyServerClientResponse resp = send(req);

		Element e = resp.doc.getRootElement();
		if (!e.getName().equals("pubkey_response")) {
			throw new RuntimeException("ERROR: Wrong format in keyserver's response");
		}
		Vector<String> ret = new Vector<String>();
		Element key = e.getChild("pubkey");
		if (key!=null) {
			if (trustedKeys==null) {
				//dont verify signature
				System.out.println("CAUTION: DID NOT TRY SIGNATURE VERIFICATION!");
			} else {
				//verify signature
				boolean verify = SecurityHelper.checkElementsSHA1localproofAndSignature(e, trustedKeys);
				if (!verify) {
					throw new RuntimeException("ERROR at requestKeyStatus: signature could NOT be verfied.");
				}
			}
			OSDXKeyObject pubkey = OSDXKeyObject.fromPubKeyElement(key);
			pubkey.addDataSourceStep(new DataSourceStep(host, System.currentTimeMillis()));
			return pubkey;
		}	
		return null;
	}
	
	//   1. Ich, als user, möchte auf dem keyserver meinen MASTER-pubkey ablegen können
	//   includes  2. Ich, als user, möchte, daß der keyserver meinen MASTER-pubkey per email-verifikation (der haupt-identity) akzeptiert (sonst ist der status pending oder so -> erst, wenn die email mit irgendeinem token-link drin aktiviert wurde, wird der pubkey akzeptiert)
	public boolean putMasterKey(OSDXKeyObject masterkey, Identity id) throws Exception {
		try {
			OSDXKeyServerClientRequest req = OSDXKeyServerClientRequest.getRequestPutMasterKey(host, masterkey, id);
			OSDXKeyServerClientResponse resp = send(req);
			if (resp==null || resp.status == null) return false;
			if (resp.status.endsWith("OK"))	return true;
			if (resp.hasErrorMessage()) {
				message = resp.getErrorMessage();
			}
		} catch (Exception ex) {
			message = ex.getMessage();
		}
		return false;
	}
	
	//3. Ich, als user, möchte auf dem keyserver meinen REVOKE-key für meinen master-key abspeichern können (der sollte sogar nicht sichtbar für irgendwen sonst sein!!!)
	public boolean putRevokeKey(OSDXKeyObject revokekey, OSDXKeyObject relatedMasterKey) throws Exception {
		OSDXKeyServerClientRequest req = OSDXKeyServerClientRequest.getRequestPutRevokeKey(host, revokekey, relatedMasterKey);
		OSDXKeyServerClientResponse resp = send(req);
		if (resp==null || resp.status == null) {
			message = "Keyserver does not respond.";
			return false;
		}
		if (resp.status.endsWith("OK"))	return true;
		if (resp.hasErrorMessage()) {
			message = resp.getErrorMessage();
		}
		return false;
	}
	
	public boolean putSubKey(OSDXKeyObject subkey, OSDXKeyObject relatedMasterKey) throws Exception {
		OSDXKeyServerClientRequest req = OSDXKeyServerClientRequest.getRequestPutSubKey(host, subkey, relatedMasterKey);
		OSDXKeyServerClientResponse resp = send(req);
		if (resp==null || resp.status == null) return false;
		if (resp.status.endsWith("OK"))	return true;
		if (resp.hasErrorMessage()) {
			message = resp.getErrorMessage();
		}
		return false;
	}

	//   5. Ich, als user, möchte meine keylogs auf dem server ablegen können (ein löschen von keylogs ist NICHT möglich - für einen aktuellen status ist die "kette ist chronologisch abzuarbeiten")
	
	public boolean putKeyLog(KeyLog keylog, OSDXKeyObject signingKey) throws Exception {
		Vector<KeyLog> keylogs = new Vector<KeyLog>();
		keylogs.add(keylog);
		return putKeyLogs(keylogs, signingKey);
		
	}
	public boolean putKeyLogs(Vector<KeyLog> keylogs, OSDXKeyObject signingKey) throws Exception {
		OSDXKeyServerClientRequest req = OSDXKeyServerClientRequest.getRequestPutKeyLogs(host, keylogs, signingKey);
		OSDXKeyServerClientResponse resp = send(req);
		if (resp==null || resp.status == null) return false;
		if (resp.status.endsWith("OK"))	return true;
		if (resp.hasErrorMessage()) {
			message = resp.getErrorMessage();
		}
		return false;
	}

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

	
	//	String[] status = c.requestKeyStatus("C930CEEF52E4D6808A4253AC2C0EF5F6E578C603");
	//	System.out.println("key status: "+status[0]+" from date: "+status[1]);


//		boolean ok2 = c.putRevokeKey(key.getPubKey(), key);
//		System.out.println("put revoke key: "+(ok2?"OK":"FAILED"));

		
	}
	
}
