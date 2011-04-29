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

import org.fnppl.opensdx.security.*;
import org.fnppl.opensdx.tsas.*;
import org.fnppl.opensdx.xml.*;

public class KeyClient {
	
	public static int OSDX_DEFAULT_PORT = 8889;
	private Socket socket = null;
	private long timeout = 2000;
	private String host = null;
	private int port = -1;
	private String message = null;
	public OutputStream log = null;
	
	public final static String ERROR_WRONG_RESPONE_FORMAT = "ERROR: Wrong format in keyserver's response.";
	public final static String ERROR_NO_RESPONSE = "ERROR: keyserver does not respond.";
	
	
	public KeyClient(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public boolean connect() throws Exception {
		socket = new Socket(host, port);
		if (socket.isConnected()) {
			//System.out.println("Connection established.");
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
	public KeyClientResponse send(KeyClientRequest req) throws Exception {
		if (!connect()) {
			throw new RuntimeException("ERROR: Can not connect to keyserver.");
		}
		//System.out.println("OSDXKeyServerClient | start "+req.getURI());
		
		//System.out.println("--- sending ---");
		//req.toOutput(System.out);
		//System.out.println("\n--- end of sending ---");
		
		req.send(socket);
		
		//processing response
	    //System.out.println("OSDXKeyServerClient | waiting for response");
	    BufferedInputStream bin = new BufferedInputStream(socket.getInputStream());
	    
	    KeyClientResponse re = KeyClientResponse.fromStream(bin, timeout);
	    close();
	    
	    if(re == null) {
	    	throw new RuntimeException("ERROR: Keyserver does not respond.");
	    }
	    return re;
	}
	
	
	// 1. Ich, als fremder user, möchte beim keyserver (z.B. keys.fnppl.org) den/die (MASTER) pubkey(s) zu der identity thiess@finetunes.net suchen können
	public Vector<String> requestMasterPubKeys(final String idemail) throws Exception {
		KeyClientRequest req = KeyClientMessageFactory.buildRequestMasterPubKeys(host, idemail);
		KeyClientResponse resp = send(req);
		if (log!=null) {
			log.write("--- REQUEST MASTERPUBKEY ----------\n".getBytes());
			req.toOutputNOT_URL_ENCODED_FOR_TESTING(log);
			log.write("--- END of REQUEST MASTERPUBKEY ---\n".getBytes());
			if (resp == null) {
				log.write(("-> --- "+ERROR_NO_RESPONSE+" ---\n").getBytes());
			} else {
				log.write("\n--- RESPONSE MASTERPUBKEY ----------\n".getBytes());
				resp.toOutput(log);
				log.write("--- END of RESPONSE MASTERPUBKEY ---\n".getBytes());
			}
		}
		OSDXMessage msg = OSDXMessage.fromElement(resp.doc.getRootElement());
		Result result = msg.verifySignatures();
		
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
		KeyClientRequest req = KeyClientMessageFactory.buildRequestMasterPubKey(host, keyid);
		KeyClientResponse resp = send(req);
		if (log!=null) {
			log.write("--- REQUEST SUBKEYS MASTERPUBKEY ----------\n".getBytes());
			req.toOutputNOT_URL_ENCODED_FOR_TESTING(log);
			log.write("--- END of REQUEST SUBKEYS MASTERPUBKEY ---\n".getBytes());
			if (resp == null) {
				log.write(("-> --- "+ERROR_NO_RESPONSE+" ---\n").getBytes());
			} else {
				log.write("\n--- RESPONSE SUBKEYS MASTERPUBKEY ----------\n".getBytes());
				resp.toOutput(log);
				log.write("--- END of RESPONSE SUBKEYS MASTERPUBKEY ---\n".getBytes());
			}
		}
		OSDXMessage msg = OSDXMessage.fromElement(resp.doc.getRootElement());
		Result result = msg.verifySignatures();
		
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
		KeyClientRequest req = KeyClientMessageFactory.buildRequestKeyServerIdentity(host);
		KeyClientResponse resp = send(req);
		if (log!=null) {
			log.write("--- REQUEST KEYSERVER IDENTITY ----------\n".getBytes());
			req.toOutputNOT_URL_ENCODED_FOR_TESTING(log);
			log.write("--- END of REQUEST KEYSERVER IDENTITY ---\n".getBytes());
			if (resp == null) {
				log.write(("-> --- "+ERROR_NO_RESPONSE+" ---\n").getBytes());
			} else {
				log.write("\n--- RESPONSE KEYSERVER IDENTITY ----------\n".getBytes());
				resp.toOutput(log);
				log.write("--- END of RESPONSE KEYSERVER IDENTITY ---\n".getBytes());
			}
		}
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
	public Vector<Identity> requestIdentities(String keyid) throws Exception {
		KeyClientRequest req = KeyClientMessageFactory.buildRequestIdentities(host, keyid);
		KeyClientResponse resp = send(req);
		if (log!=null) {
			log.write("--- REQUEST IDENTITIES ----------\n".getBytes());
			req.toOutputNOT_URL_ENCODED_FOR_TESTING(log);
			log.write("--- END of REQUEST IDENTITIES ---\n".getBytes());
			if (resp == null) {
				log.write(("-> --- "+ERROR_NO_RESPONSE+" ---\n").getBytes());
			} else {
				log.write("\n--- RESPONSE IDENTITIES ----------\n".getBytes());
				resp.toOutput(log);
				log.write("--- END of RESPONSE IDENTITIES ---\n".getBytes());
			}
		}
		if (resp==null || resp.status == null) {
			message = ERROR_NO_RESPONSE;
			return null;
		}
		OSDXMessage msg = OSDXMessage.fromElement(resp.doc.getRootElement());
		Result result = msg.verifySignatures();
		
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
	
	//3. Ich, als fremder user, möchte beim keyserver den aktuellen (beim keyserver bekannten) status zu einem pubkey bekommen können (valid/revoked/etc.)
	public KeyStatus requestKeyStatus(String keyid) throws Exception {
		KeyClientRequest req = KeyClientMessageFactory.buildRequestKeyStatus(host, keyid);
		KeyClientResponse resp = send(req);
		if (log!=null) {
			log.write("--- REQUEST KEYSTATUS ----------\n".getBytes());
			req.toOutputNOT_URL_ENCODED_FOR_TESTING(log);
			log.write("--- END of REQUEST KEYSTATUS ---\n".getBytes());
			if (resp == null) {
				log.write(("-> --- "+ERROR_NO_RESPONSE+" ---\n").getBytes());
			} else {
				log.write("\n--- RESPONSE KEYSTATUS ----------\n".getBytes());
				resp.toOutput(log);
				log.write("--- END of RESPONSE KEYSTATUS ---\n".getBytes());
			}
		}
		if (resp==null || resp.status == null) {
			message = ERROR_NO_RESPONSE;
			return null;
		}
		OSDXMessage msg = OSDXMessage.fromElement(resp.doc.getRootElement());
		Result result = msg.verifySignatures();
		
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
	public Vector<KeyLog> requestKeyLogs(String keyid) throws Exception {
		KeyClientRequest req = KeyClientMessageFactory.buildRequestKeyLogs(host, keyid);
		KeyClientResponse resp = send(req);
		if (log!=null) {
			log.write("--- REQUEST KeyLogs ----------\n".getBytes());
			req.toOutputNOT_URL_ENCODED_FOR_TESTING(log);
			log.write("--- END of REQUEST KeyLogs ---\n".getBytes());
			if (resp == null) {
				log.write(("-> --- "+ERROR_NO_RESPONSE+" ---\n").getBytes());
			} else {
				log.write("\n--- RESPONSE KeyLogs ----------\n".getBytes());
				resp.toOutput(log);
				log.write("--- END of RESPONSE KeyLogs ---\n".getBytes());
			}
		}
		if (resp==null || resp.status == null) {
			message = ERROR_NO_RESPONSE;
			return null;
		}
		OSDXMessage msg = OSDXMessage.fromElement(resp.doc.getRootElement());
		Result result = msg.verifySignatures();
		
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
		KeyClientRequest req = KeyClientMessageFactory.buildRequestSubkeys(host, masterkeyid);
		KeyClientResponse resp = send(req);
		if (log!=null) {
			log.write("--- REQUEST SubKeys ----------\n".getBytes());
			req.toOutputNOT_URL_ENCODED_FOR_TESTING(log);
			log.write("--- END of REQUEST SubKeys ---\n".getBytes());
			if (resp == null) {
				log.write(("-> --- "+ERROR_NO_RESPONSE+" ---\n").getBytes());
			} else {
				log.write("\n--- RESPONSE SubKeys ----------\n".getBytes());
				resp.toOutput(log);
				log.write("--- END of RESPONSE SubKeys ---\n".getBytes());
			}
		}
		if (resp==null || resp.status == null) {
			message = ERROR_NO_RESPONSE;
			return null;
		}
		OSDXMessage msg = OSDXMessage.fromElement(resp.doc.getRootElement());
		Result result = msg.verifySignatures();
		
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
		KeyClientRequest req = KeyClientMessageFactory.buildRequestPublicKey(host, keyid);
		KeyClientResponse resp = send(req);
		if (log!=null) {
			log.write("--- REQUEST PublicKey ----------\n".getBytes());
			req.toOutputNOT_URL_ENCODED_FOR_TESTING(log);
			log.write("--- END of REQUEST PublicKey ---\n".getBytes());
			if (resp == null) {
				log.write(("-> --- "+ERROR_NO_RESPONSE+" ---\n").getBytes());
			} else {
				log.write("\n--- RESPONSE PublicKey ----------\n".getBytes());
				resp.toOutput(log);
				log.write("--- END of RESPONSE PublicKey ---\n".getBytes());
			}
		}
		if (resp==null || resp.status == null) {
			message = ERROR_NO_RESPONSE;
			return null;
		}
		OSDXMessage msg = OSDXMessage.fromElement(resp.doc.getRootElement());
		Result result = msg.verifySignatures();
		
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
			KeyClientRequest req = KeyClientMessageFactory.buildPutRequestMasterKey(host, masterkey, id);
			KeyClientResponse resp = send(req);
			if (log!=null) {
				log.write("--- REQUEST PUT MasterKey ----------\n".getBytes());
				req.toOutputNOT_URL_ENCODED_FOR_TESTING(log);
				log.write("--- END of REQUEST PUT MasterKey ---\n".getBytes());
				if (resp == null) {
					log.write(("-> --- "+ERROR_NO_RESPONSE+" ---\n").getBytes());
				} else {
					log.write("\n--- RESPONSE PUT MasterKey ----------\n".getBytes());
					resp.toOutput(log);
					log.write("--- END of RESPONSE PUT MasterKey ---\n".getBytes());
				}
			}
			return checkResponse(resp);
		} catch (Exception ex) {
			ex.printStackTrace();
			message = ex.getMessage();
		}
		return false;
	}
	
	//3. Ich, als user, möchte auf dem keyserver meinen REVOKE-key für meinen master-key abspeichern können (der sollte sogar nicht sichtbar für irgendwen sonst sein!!!)
	public boolean putRevokeKey(RevokeKey revokekey, MasterKey relatedMasterKey) throws Exception {
		KeyClientRequest req = KeyClientMessageFactory.buildPutRequestRevokeKey(host, revokekey, relatedMasterKey);
		KeyClientResponse resp = send(req);
		if (log!=null) {
			log.write("--- REQUEST PUT RevokeKey ----------\n".getBytes());
			req.toOutputNOT_URL_ENCODED_FOR_TESTING(log);
			log.write("--- END of REQUEST PUT RevokeKey ---\n".getBytes());
			if (resp == null) {
				log.write(("-> --- "+ERROR_NO_RESPONSE+" ---\n").getBytes());
			} else {
				log.write("\n--- RESPONSE PUT RevokeKey ----------\n".getBytes());
				resp.toOutput(log);
				log.write("--- END of RESPONSE PUT RevokeKey ---\n".getBytes());
			}
		}
		return checkResponse(resp);
	}
	
	public boolean putRevokeMasterKeyRequest(RevokeKey revokekey, MasterKey relatedMasterKey, String message) throws Exception {
		KeyClientRequest req = KeyClientMessageFactory.buildPutRequestRevokeMasterKey(host, revokekey, relatedMasterKey, message);
		KeyClientResponse resp = send(req);
		if (log!=null) {
			log.write("--- REQUEST Revoke-MasterKey ----------\n".getBytes());
			req.toOutputNOT_URL_ENCODED_FOR_TESTING(log);
			log.write("--- END of REQUEST Revoke-MasterKey ---\n".getBytes());
			if (resp == null) {
				log.write(("-> --- "+ERROR_NO_RESPONSE+" ---\n").getBytes());
			} else {
				log.write("\n--- RESPONSE Revoke-MasterKey ----------\n".getBytes());
				resp.toOutput(log);
				log.write("--- END of RESPONSE Revoke-MasterKey ---\n".getBytes());
			}
		}
		return checkResponse(resp);
	}
	
	public boolean putSubKey(SubKey subkey, MasterKey relatedMasterKey) throws Exception {
		KeyClientRequest req = KeyClientMessageFactory.buildPutRequestSubKey(host, subkey, relatedMasterKey);
		KeyClientResponse resp = send(req);
		if (log!=null) {
			log.write("--- REQUEST PUT SubKey ----------\n".getBytes());
			req.toOutputNOT_URL_ENCODED_FOR_TESTING(log);
			log.write("--- END of REQUEST PUT SubKey ---\n".getBytes());
			if (resp == null) {
				log.write(("-> --- "+ERROR_NO_RESPONSE+" ---\n").getBytes());
			} else {
				log.write("\n--- RESPONSE PUT SubKey ----------\n".getBytes());
				resp.toOutput(log);
				log.write("--- END of RESPONSE PUT SubKey ---\n".getBytes());
			}
		}
		return checkResponse(resp);
	}

	public boolean putRevokeSubKeyRequest(SubKey subkey, MasterKey relatedMasterKey, String message) throws Exception {
		KeyClientRequest req = KeyClientMessageFactory.buildPutRequestRevokeSubKey(host, subkey, relatedMasterKey, message);
		KeyClientResponse resp = send(req);
		if (log!=null) {
			log.write("--- REQUEST Revoke-SubKey ----------\n".getBytes());
			req.toOutputNOT_URL_ENCODED_FOR_TESTING(log);
			log.write("--- END of REQUEST Revoke-SubKey ---\n".getBytes());
			if (resp == null) {
				log.write(("-> --- "+ERROR_NO_RESPONSE+" ---\n").getBytes());
			} else {
				log.write("\n--- RESPONSE Revoke-SubKey ----------\n".getBytes());
				resp.toOutput(log);
				log.write("--- END of RESPONSE Revoke-SubKey ---\n".getBytes());
			}
		}
		return checkResponse(resp);
	}
	
	//   5. Ich, als user, möchte meine keylogs auf dem server ablegen können (ein löschen von keylogs ist NICHT möglich - für einen aktuellen status ist die "kette ist chronologisch abzuarbeiten")
	
	public boolean putKeyLog(KeyLog keylog, OSDXKey signingKey) throws Exception {
		Vector<KeyLog> keylogs = new Vector<KeyLog>();
		keylogs.add(keylog);
		return putKeyLogs(keylogs, signingKey);	
	}
	
	public boolean putKeyLogs(Vector<KeyLog> keylogs, OSDXKey signingKey) throws Exception {
		KeyClientRequest req = KeyClientMessageFactory.getPutRequestKeyLogs(host, keylogs, signingKey);
		KeyClientResponse resp = send(req);
		if (log!=null) {
			log.write("--- REQUEST PUT KeyLogs ----------\n".getBytes());
			req.toOutputNOT_URL_ENCODED_FOR_TESTING(log);
			log.write("--- END of REQUEST PUT KeyLogs ---\n".getBytes());
			if (resp == null) {
				log.write(("-> --- "+ERROR_NO_RESPONSE+" ---\n").getBytes());
			} else {
				log.write("\n--- RESPONSE PUT KeyLogs ----------\n".getBytes());
				resp.toOutput(log);
				log.write("--- END of RESPONSE PUT KeyLogs ---\n".getBytes());
			}
		}
		return checkResponse(resp);
	}

	private boolean checkResponse(KeyClientResponse resp) {
		if (resp==null || resp.status == null) {
			message = ERROR_NO_RESPONSE;
			return false;
		}
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
