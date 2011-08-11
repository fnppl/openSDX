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
import java.net.*;
import java.util.*;

import org.fnppl.opensdx.http.HTTPClient;
import org.fnppl.opensdx.http.HTTPClientRequest;
import org.fnppl.opensdx.http.HTTPClientResponse;
import org.fnppl.opensdx.security.*;
import org.fnppl.opensdx.tsaserver.*;
import org.fnppl.opensdx.xml.*;

public class KeyClient extends HTTPClient {
	public static int OSDX_KEYSERVER_DEFAULT_PORT = 8889;
	public final static String ERROR_WRONG_RESPONE_FORMAT = "ERROR: Wrong format in keyserver's response.";
	
	private String prepath;
	private KeyVerificator keyverificator = null;
	
	public KeyClient(String host, int port, String prepath, KeyVerificator keyverificator) {
		super(host, port);
		this.prepath = prepath;
		this.keyverificator = keyverificator;
		log = System.out;
	}
	
	public KeyClient(KeyServerIdentity keyserver, KeyVerificator keyverificator) {
		super(keyserver.getHost(), keyserver.getPort());
		this.prepath = keyserver.getPrepath();
		this.keyverificator = keyverificator;
		log = System.out;
	}
	
	public void setKeyVerificator(KeyVerificator keyverificator) {
		this.keyverificator = keyverificator;
	}
	
	// 1. Ich, als fremder user, möchte beim keyserver (z.B. keys.fnppl.org) den/die (MASTER) pubkey(s) zu der identity thiess@finetunes.net suchen können
	public Vector<String> requestMasterPubKeys(final String idemail) throws Exception {
		HTTPClientRequest req = KeyClientMessageFactory.buildRequestMasterPubKeys(host, prepath, idemail);
		HTTPClientResponse resp = send(req);
		writeLog(req, resp, "MASTERPUBKEY");
		
		OSDXMessage msg = OSDXMessage.fromElement(resp.doc.getRootElement());
		Result result = msg.verifySignatures(keyverificator);
		
		if (result.succeeded) {
			Element content = msg.getContent();
			if (!content.getName().equals(KeyClientMessageFactory.MASTERPUBKEYS_RESPONSE)) {
				message = ERROR_WRONG_RESPONE_FORMAT;
				return null;
			}
			Vector<String> ret = new Vector<String>();
			Element er = content.getChild("related_keys");
			if (er!=null) {
				Vector<Element> keyids = er.getChildren("keyid");
				for (Element k : keyids) {
					ret.add(k.getText());
				}
			}
			return ret;
		} else {
			message = result.errorMessage;
			return null;
		}
	}
	
	
	public MasterKey requestMasterPubKey(final String keyid) throws Exception {
		HTTPClientRequest req = KeyClientMessageFactory.buildRequestMasterPubKey(host, prepath, keyid);
		HTTPClientResponse resp = send(req);
		writeLog(req, resp, "SUBKEYS MASTERPUBKEY");
		
		OSDXMessage msg = OSDXMessage.fromElement(resp.doc.getRootElement());
		Result result = msg.verifySignatures(keyverificator);
		
		if (result.succeeded) {
			Element content = msg.getContent();
			if (!content.getName().equals(KeyClientMessageFactory.MASTERPUBKEY_RESPONSE)) {
				message = ERROR_WRONG_RESPONE_FORMAT;
				return null;
			}
			Element key = content.getChild("pubkey");
			OSDXKey pubkey = OSDXKey.fromPubKeyElement(key);
			pubkey.addDataSourceStep(new DataSourceStep(host, System.currentTimeMillis()));
			if (pubkey instanceof MasterKey) return (MasterKey)pubkey;
			message = "resulted key is not a MASTER key";
			return null;
		} else {
			message = result.errorMessage;
			return null;
		}
	}
	
//	public Vector<OSDXKey> requestPubKeys(final String idemail) throws Exception {
//		OSDXKeyServerClientRequest req = OSDXKeyServerClientRequest.getRequestPubKeys(host, idemail);
//		OSDXKeyServerClientResponse resp = send(req);
//		
//		Element e = resp.doc.getRootElement();
//		if (!e.getName().equals("pubkeys")) {
//			throw new RuntimeException("ERROR: Wrong format in keyserver's response");
//		}
//		Vector<OSDXKey> ret = new Vector<OSDXKey>();
//		Vector<Element> pks = e.getChildren("keypair");
//		for (Element pk : pks) {
//			OSDXKey key = OSDXKey.fromElement(pk);
//			ret.add(key);
//		}
//		return ret;
//	}
	
	public KeyServerIdentity requestKeyServerIdentity() throws Exception {
		HTTPClientRequest req = KeyClientMessageFactory.buildRequestKeyServerIdentity(host, prepath);
		HTTPClientResponse resp = send(req);
		writeLog(req, resp, "KEYSERVER IDENTITY");
		
		if (resp==null || resp.status == null) {
			message = ERROR_NO_RESPONSE;
			return null;
		}
		OSDXMessage msg = OSDXMessage.fromElement(resp.doc.getRootElement());
		Result result = msg.verifySignaturesWithoutKeyVerification();
		
		if (result.succeeded) {
			Element content = msg.getContent();
			if (!content.getName().equals(KeyClientMessageFactory.KEYSERVER_SETTINGS_RESPONSE)) {
				message = ERROR_WRONG_RESPONE_FORMAT;
				return null;
			}
			try {
				KeyServerIdentity id = KeyServerIdentity.fromElement(content);
				return id;
			} catch (Exception ex) {
				message = "error in keyserver response";
				return null;
			}
		} else {
			message = result.errorMessage;
			return null;
		}
	}
	
	
	//2. Ich, als fremder user, möchte beim keyserver die weiteren identities (identity-details) zu einem pubkey bekommen können
	public Vector<Identity> requestIdentities(String keyid, OSDXKey signingKey) throws Exception {
		HTTPClientRequest req = KeyClientMessageFactory.buildRequestIdentities(host, prepath, keyid, signingKey);
		HTTPClientResponse resp = send(req);
		writeLog(req, resp, "IDENTITIES");
		if (resp==null || resp.status == null) {
			message = ERROR_NO_RESPONSE;
			return null;
		}
		OSDXMessage msg = OSDXMessage.fromElement(resp.doc.getRootElement());
		Result result = msg.verifySignatures(keyverificator);
		
		if (result.succeeded) {
			Element content = msg.getContent();
			if (!content.getName().equals(KeyClientMessageFactory.IDENTITIES_RESPONSE)) {
				message = ERROR_WRONG_RESPONE_FORMAT;
				return null;
			}
			Vector<Identity> ret = new Vector<Identity>();
		 	Vector<Element> eid = content.getChildren("identity");
		 	for (Element id : eid) {
		 		ret.add(Identity.fromElement(id));
		 	}
		 	return ret;
		} else {
			message = result.errorMessage;
			return null;
		}
	} 
	
	public Identity requestCurrentIdentity(String keyid, OSDXKey signingKey) throws Exception {
		HTTPClientRequest req = KeyClientMessageFactory.buildRequestIdentities(host, prepath, keyid, signingKey);
		HTTPClientResponse resp = send(req);
		writeLog(req, resp, "IDENTITY");
		if (resp==null || resp.status == null) {
			message = ERROR_NO_RESPONSE;
			return null;
		}
		OSDXMessage msg = OSDXMessage.fromElement(resp.doc.getRootElement());
		Result result = msg.verifySignatures(keyverificator);
		
		if (result.succeeded) {
			Element content = msg.getContent();
			if (!content.getName().equals(KeyClientMessageFactory.IDENTITIES_RESPONSE)) {
				message = ERROR_WRONG_RESPONE_FORMAT;
				return null;
			}
		 	Vector<Element> eid = content.getChildren("identity");
		 	if (eid.size()>0) {
		 		return (Identity.fromElement(eid.get(0)));
		 	}
		 	return null;
		} else {
			message = result.errorMessage;
			return null;
		}
	} 
	
	//3. Ich, als fremder user, möchte beim keyserver den aktuellen (beim keyserver bekannten) status zu einem pubkey bekommen können (valid/revoked/etc.)
	public KeyStatus requestKeyStatus(String keyid) throws Exception {
		HTTPClientRequest req = KeyClientMessageFactory.buildRequestKeyStatus(host, prepath, keyid);
		HTTPClientResponse resp = send(req);
		writeLog(req, resp, "KEYSTATUS");
		
		if (resp==null || resp.status == null) {
			message = ERROR_NO_RESPONSE;
			return null;
		}
		OSDXMessage msg = OSDXMessage.fromElement(resp.doc.getRootElement());
		Result result = msg.verifySignatures(keyverificator);
		
		if (result.succeeded) {
			Element content = msg.getContent();
			if (!content.getName().equals(KeyClientMessageFactory.KEYSTATUS_RESPONSE)) {
				message = ERROR_WRONG_RESPONE_FORMAT;
				return null;
			}
			Element eks = content.getChild("keystatus");
			if (eks != null) {
				KeyStatus ks = KeyStatus.fromElement(eks);
				return ks;
			}
			return null;
		} else {
			message = result.errorMessage;
			return null;
		}
	}
	
	//4. Ich, als fremder user, möchte beim keyserver die keylogs eines (beliebigen) pubkeys bekommen können
	public Vector<KeyLog> requestKeyLogs(String keyid, OSDXKey sign) throws Exception {
		HTTPClientRequest req = KeyClientMessageFactory.buildRequestKeyLogs(host, prepath, keyid, sign);
		HTTPClientResponse resp = send(req);
		writeLog(req, resp, "KeyLogs");
		if (resp==null || resp.status == null) {
			message = ERROR_NO_RESPONSE;
			return null;
		}
		OSDXMessage msg = OSDXMessage.fromElement(resp.doc.getRootElement());
		Result result = msg.verifySignatures(keyverificator);
		
		if (result.succeeded) {
			Element content = msg.getContent();
			if (!content.getName().equals(KeyClientMessageFactory.KEYLOGS_RESPONSE)) {
				message = ERROR_WRONG_RESPONE_FORMAT;
				return null;
			}
			Vector<Element> ekls = content.getChildren("keylog");
			Vector<KeyLog> vkl = new Vector<KeyLog>();
			for (Element ekl : ekls) {
				KeyLog kl = KeyLog.fromElement(ekl);
				vkl.add(kl);
			}
			return vkl;
		} else {
			message = result.errorMessage;
			return null;
		}
	}
	
	//5. Ich, als fremder user, möchte beim keyserver die weiteren pubkeys zu einem parent-pubkey (MASTER) bekommen können
	public Vector<String> requestSubKeys(String masterkeyid) throws Exception {
		HTTPClientRequest req = KeyClientMessageFactory.buildRequestSubkeys(host, prepath, masterkeyid);
		HTTPClientResponse resp = send(req);
		writeLog(req, resp, "SubKeys");
		if (resp==null || resp.status == null) {
			message = ERROR_NO_RESPONSE;
			return null;
		}
		OSDXMessage msg = OSDXMessage.fromElement(resp.doc.getRootElement());
		Result result = msg.verifySignatures(keyverificator);
		
		if (result.succeeded) {
			Element content = msg.getContent();
			if (!content.getName().equals(KeyClientMessageFactory.SUBKEYS_RESPONSE)) {
				message = ERROR_WRONG_RESPONE_FORMAT;
				return null;
			}
			Vector<String> ret = new Vector<String>();
			Vector<Element> keyids = content.getChildren("keyid");
			for (Element k : keyids) {
				ret.add(k.getText());
			}
			return ret;
		} else {
			message = result.errorMessage;
			return null;
		}
	}
	
	public OSDXKey requestPublicKey(String keyid) throws Exception {
		HTTPClientRequest req = KeyClientMessageFactory.buildRequestPublicKey(host, prepath, keyid);
		HTTPClientResponse resp = send(req);
		writeLog(req, resp, "PublicKey");
		if (resp==null || resp.status == null) {
			message = ERROR_NO_RESPONSE;
			return null;
		}
		OSDXMessage msg = OSDXMessage.fromElement(resp.doc.getRootElement());
		Result result = msg.verifySignatures(keyverificator);
		
		if (result.succeeded) {
			Element content = msg.getContent();
			if (!content.getName().equals(KeyClientMessageFactory.PUBLICKEY_RESPONSE)) {
				message = ERROR_WRONG_RESPONE_FORMAT;
				return null;
			}
			Element key = content.getChild("pubkey");
			OSDXKey pubkey = OSDXKey.fromPubKeyElement(key);
			pubkey.addDataSourceStep(new DataSourceStep(host, System.currentTimeMillis()));
			return pubkey;
		} else {
			message = result.errorMessage;
			return null;
		}
	}
	
	
	
	
	
	//   1. Ich, als user, möchte auf dem keyserver meinen MASTER-pubkey ablegen können
	//   includes  2. Ich, als user, möchte, daß der keyserver meinen MASTER-pubkey per email-verifikation (der haupt-identity) akzeptiert (sonst ist der status pending oder so -> erst, wenn die email mit irgendeinem token-link drin aktiviert wurde, wird der pubkey akzeptiert)
	public boolean putMasterKey(MasterKey masterkey, Identity id) throws Exception {
		try {
			HTTPClientRequest req = KeyClientMessageFactory.buildPutRequestMasterKey(host, prepath, masterkey, id);
			HTTPClientResponse resp = send(req);
			writeLog(req, resp, "PUT MasterKey");
			return checkResponse(resp);
		} catch (Exception ex) {
			ex.printStackTrace();
			message = ex.getMessage();
		}
		return false;
	}
	
	//3. Ich, als user, möchte auf dem keyserver meinen REVOKE-key für meinen master-key abspeichern können (der sollte sogar nicht sichtbar für irgendwen sonst sein!!!)
	public boolean putRevokeKey(RevokeKey revokekey, MasterKey relatedMasterKey) throws Exception {
		HTTPClientRequest req = KeyClientMessageFactory.buildPutRequestRevokeKey(host, prepath, revokekey, relatedMasterKey);
		HTTPClientResponse resp = send(req);
		writeLog(req, resp, "PUT RevokeKey");
		return checkResponse(resp);
	}
	
	public boolean putRevokeMasterKeyRequest(RevokeKey revokekey, MasterKey relatedMasterKey, String message) throws Exception {
		HTTPClientRequest req = KeyClientMessageFactory.buildPutRequestRevokeMasterKey(host, prepath, revokekey, relatedMasterKey, message);
		HTTPClientResponse resp = send(req);
		writeLog(req, resp, "PUT Revoke-MasterKey");
		return checkResponse(resp);
	}
	
	public boolean putSubKey(SubKey subkey, MasterKey relatedMasterKey) throws Exception {
		HTTPClientRequest req = KeyClientMessageFactory.buildPutRequestSubKey(host, prepath, subkey, relatedMasterKey);
		HTTPClientResponse resp = send(req);
		writeLog(req, resp, "PUT SubKey");
		return checkResponse(resp);
	}

	public boolean putRevokeSubKeyRequest(SubKey subkey, MasterKey relatedMasterKey, String message) throws Exception {
		HTTPClientRequest req = KeyClientMessageFactory.buildPutRequestRevokeSubKey(host, prepath, subkey, relatedMasterKey, message);
		HTTPClientResponse resp = send(req);
		writeLog(req, resp, "Revoke SubKey");
		return checkResponse(resp);
	}
	
	//   5. Ich, als user, möchte meine keylogs auf dem server ablegen können (ein löschen von keylogs ist NICHT möglich - für einen aktuellen status ist die "kette ist chronologisch abzuarbeiten")
	
	public boolean putKeyLogAction(KeyLogAction keylogAction, OSDXKey signingKey) throws Exception {
		Vector<KeyLogAction> keylogActions = new Vector<KeyLogAction>();
		keylogActions.add(keylogAction);
		return putKeyLogActions(keylogActions, signingKey);	
	}
	
	public boolean putKeyLogActions(Vector<KeyLogAction> keylogActions, OSDXKey signingKey) throws Exception {
		HTTPClientRequest req = KeyClientMessageFactory.getPutRequestKeyLogs(host, prepath, keylogActions, signingKey);
		HTTPClientResponse resp = send(req);
		writeLog(req, resp, "PUT KeyLogs");
		return checkResponse(resp);
	}

	
	//   4. Ich, als user, möchte eigentlich, daß alle meine Aktionen auf meinem MASTER-key - bzw. allen meiner keys durch einen entsprechenden signature-proof des entsprechenden private-keys validierbar sind
	//   6. Ich, als user, möchte alle kommunikation vom/zum keyserver mit einem vom keyserver definierten root-MASTER-key approveden key signiert wissen
	//   7. Ich, als user, möchte diese keyserver-root-keys vordefiniert in meiner openSDX-suite finden, aber auch simpelst selbst nachrüsten können

	
	public static void main(String[] args) throws Exception {
		
		
//		OSDXKeyServerClient c = new OSDXKeyServerClient("localhost", 8889);
		

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
//		OSDXKey key = OSDXKey.fromKeyPair(kp);
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
