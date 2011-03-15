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
import org.fnppl.opensdx.xml.*;

/*
 * HT 2011-02-20
 * I am a bit bored of twiggling around with annoying formats, frameworks and paddings and whatever
 * 
 * I think, it would be nice to have a good (which does not mean, those other solutions are not good), clean, clean-room-implementation of what suits ME best...
 * 
 * of yourse, now it makes sense to separate the http-parts from tsas AND keyserver into an own package... later...
 * 
 */

/*
 * 1. registering a new publickey should always be possible (confirmation-email is sent and has to be accepted)
 * 2. adding an approval to any publickey should be possible - if that approving key is also registered
 * 3. it would be good to multiplex to/from gpgkeyser.de or such
 * 4. a strict separation between publickey and approval(chains) is desired
 * 5. different verification-levels should be available on each key/approval
 * 6. such things as "certificate" are much appreciated to be done better here. horrible: x509v3 - why on earth? 
 * 7. communication with this server is to be done via client-api (commandline) - although browser would also be possible - but i am not into giving ssl a try... 
 * 
 */

//http://de.wikipedia.org/wiki/Hypertext_Transfer_Protocol

public class KeyServerMain {
	private static String serverid = "OSDX KeyServer v0.1";
	
	int port = -1;
	Inet4Address address = null;

	private KeyApprovingStore keystore;
	
	private HashMap<String, Vector<OSDXKeyObject>> id_keys;
	private HashMap<String, OSDXKeyObject> keyid_key;
	private HashMap<String, Vector<KeyLog>> keyid_log;
	private HashMap<String, Vector<OSDXKeyObject>> keyid_subkeys;
	
	private OSDXKeyObject keyServerSigningKey = null;
	private String serverIDemail = "root_signing_key@fnppl.org";
	private String serverSigningKeyPassword = "bla";
	
	public KeyServerMain() throws Exception {
		// read keystore
		File f = new File("server_testkeystore.xml");

		keystore = KeyApprovingStore.fromFile(f);
		id_keys = new HashMap<String, Vector<OSDXKeyObject>>();
		keyid_key = new HashMap<String, OSDXKeyObject>();
		keyid_log = new HashMap<String, Vector<KeyLog>>();
		keyid_subkeys = new HashMap<String, Vector<OSDXKeyObject>>();
		Vector<OSDXKeyObject> keys = keystore.getAllKeys();
		if (keys != null) {
			for (OSDXKeyObject k : keys) {
				updateCache(k, null);
			}
		}
		Vector<KeyLog> keylogs = keystore.getKeyLogs();
		if (keylogs != null) {
			for (KeyLog l : keylogs) {
				updateCache(null, l);
			}
		}
		
		//serverSigningKey
		if (id_keys.get(serverIDemail) != null) {
			keyServerSigningKey = id_keys.get(serverIDemail).firstElement();
			keyServerSigningKey.unlockPrivateKey(serverSigningKeyPassword);
		} else {
			//generate new keypair if keystore not contains serverIDemail key
			keyServerSigningKey = OSDXKeyObject.fromKeyPair(AsymmetricKeyPair.generateAsymmetricKeyPair());
			Identity id = Identity.newEmptyIdentity();
			id.setEmail(serverIDemail);
			keyServerSigningKey.addIdentity(id);
			keystore.addKey(keyServerSigningKey);
			keystore.toFile(keystore.getFile());
		}
		keystore.setSigningKey(keyServerSigningKey);
		
	}
	
	private void updateCache(OSDXKeyObject k, KeyLog l) {
		if (k!=null) {
			keyid_key.put(k.getKeyModulusSHA1(), k);
			System.out.println("adding keyid_key: "+k.getKeyModulusSHA1()+"::OSDXKeyObject");
			Vector<Identity> ids = k.getIdentities();
			if (ids != null) {
				for (Identity id : ids) {
					if (!id_keys.containsKey(id.getEmail())) {
						id_keys.put(id.getEmail(),
								new Vector<OSDXKeyObject>());
					}
					id_keys.get(id.getEmail()).add(k);
					System.out.println("adding id_keys: "+id.getEmail()+"::"+k.getKeyModulusSHA1());
				}
			}
			String parentKeyID = k.getParentKeyID();
			if (parentKeyID!=null && parentKeyID.length()>0) {
				if (!keyid_subkeys.containsKey(parentKeyID)) {
					keyid_subkeys.put(parentKeyID, new Vector<OSDXKeyObject>());
				}
				keyid_subkeys.get(parentKeyID).add(k);
				System.out.println("adding subkey: "+k.getKeyModulusSHA1()+" for parent key: "+parentKeyID);
			}
		}
		if (l!=null) {
			String keyid = l.getKeyIDTo();
			if (!keyid_log.containsKey(keyid)) {
				keyid_log.put(keyid, new Vector<KeyLog>());
			}
			keyid_log.get(keyid).add(l);
		}
	}

	

	private KeyServerResponse handleMasterPubKeyRequest(KeyServerRequest request) {
		System.out.println("KeyServerResponse | ::handleMasterPubKeyRequest");
		String id = request.getHeaderValue("Identity");
		if (id != null) {
			KeyServerResponse resp = new KeyServerResponse(serverid);
			
			//HT 2011-15-03 why needed?
//			resp.addHeaderValue("Response", "masterpubkeys");
//			resp.addHeaderValue("Identity", id);

			Element e = new Element("pubkeys");

			Vector<OSDXKeyObject> keys = id_keys.get(id);
			if (keys != null && keys.size() > 0) {
				for (OSDXKeyObject k : keys) {
					e.addContent(k.getSimplePubKeyElement());
				}
			}
			resp.setContentElement(e);
			return resp;
		}
		System.out.println("KeyServerResponse | ::error in request");
		return null;
	}
	
	private KeyServerResponse handleIdentitiesRequest(KeyServerRequest request) {
		System.out.println("KeyServerResponse | ::handleIdentitiesRequest");
		String id = request.getHeaderValue("KeyID");
		
		if (id != null) {
			KeyServerResponse resp = new KeyServerResponse(serverid);

			//HT 2011-15-03 why needed?
//			resp.addHeaderValue("Response", "identities");
//			resp.addHeaderValue("KeyID", id);

			Element e = new Element("identities");

			OSDXKeyObject key = keyid_key.get(id); //TODO could be multiple keys for keyid collisions
			if (key != null) {
				Vector<Identity> ids = key.getIdentities();
				for (Identity aid : ids) {
					e.addContent(aid.toElement());
				}
			}
			resp.setContentElement(e);
			return resp;
		}
		return null;
	}
	
	private KeyServerResponse handleKeyStatusRequest(KeyServerRequest request) throws Exception {
		System.out.println("KeyServerResponse | ::handle key status request");
		String id = request.getHeaderValue("KeyID");
		
		if (id != null) {
			KeyServerResponse resp = new KeyServerResponse(serverid);
			//HT 2011-15-03 why needed?
//			resp.addHeaderValue("Response", "keystatus");
//			resp.addHeaderValue("KeyID", id);

			Element e = new Element("keyid_keystatus");
			String[] ks = keystore.getKeyStatusWithDate(id);
			if (ks!=null) {
				e.addContent("keyid",id);
				e.addContent("keystatus",ks[0]);
				e.addContent("keystatus_date",ks[1]);
				byte[] sha1proof = SecurityHelper.getSHA1LocalProof(e);
				e.addContent("sha1localproof", SecurityHelper.HexDecoder.encode(sha1proof, ':', -1));
				e.addContent(Signature.createSignatureFromLocalProof(sha1proof, "keyid and keystatus and date", keyServerSigningKey).toElement());
			}
			resp.setContentElement(e);
			return resp;
		}
		return null;
	}
	
	private KeyServerResponse handleKeyLogsRequest(KeyServerRequest request) throws Exception {
		System.out.println("KeyServerResponse | ::handle keylog request");
		String id = request.getHeaderValue("KeyID");

		if (id != null) {
			KeyServerResponse resp = new KeyServerResponse(serverid);
			//HT 2011-15-03 why needed?
//			resp.addHeaderValue("Response", "keylogs");
//			resp.addHeaderValue("KeyID", id);

			Element e = new Element("keylogs");
			Vector<KeyLog> kls = keyid_log.get(id);
			
			if (kls!=null) {
				for (KeyLog kl : kls) {
					e.addContent(kl.toElement());
				}
				//signoff
				byte[] sha1proof = SecurityHelper.getSHA1LocalProof(e);
				e.addContent("sha1localproof", SecurityHelper.HexDecoder.encode(sha1proof, ':', -1));
				e.addContent(Signature.createSignatureFromLocalProof(sha1proof, "keylogs", keyServerSigningKey).toElement());
			}
			resp.setContentElement(e);
			return resp;
		}
		return null;
	}
	
	private KeyServerResponse handleSubKeyRequest(KeyServerRequest request) throws Exception {
		System.out.println("KeyServerResponse | ::handle subkey request");
		String id = request.getHeaderValue("KeyID");

		if (id != null) {
			KeyServerResponse resp = new KeyServerResponse(serverid);
			//HT 2011-15-03 why needed?
//			resp.addHeaderValue("Response", "subkeys");
//			resp.addHeaderValue("Parent KeyID", id);

			Element e = new Element("pubkeys");
			Vector<OSDXKeyObject> subkeys = keyid_subkeys.get(id);
			if (subkeys!=null) {
				for (OSDXKeyObject key : subkeys) {
					e.addContent(key.getSimplePubKeyElement());
				}
				//signoff
				byte[] sha1proof = SecurityHelper.getSHA1LocalProof(e);
				e.addContent("sha1localproof", SecurityHelper.HexDecoder.encode(sha1proof, ':', -1));
				e.addContent(Signature.createSignatureFromLocalProof(sha1proof, "keylogs", keyServerSigningKey).toElement());
			}
			resp.setContentElement(e);
			return resp;
		}
		return null;
	}
	
	private KeyServerResponse handlePutMasterKeyRequest(KeyServerRequest request) throws Exception {
		System.out.println("KeyServerResponse | ::handle put masterkey request");
		String keyid = request.getHeaderValue("KeyID");
		String id = request.getHeaderValue("Identity");
		System.out.print("GOT THIS CONTENT::");
		request.xml.output(System.out);
		System.out.println("::END OF CONTENT");
		Element e = request.xml.getRootElement();
		if (e.getName().equals("masterpubkey")) {
			PublicKey pubkey = PublicKey.fromSimplePubKeyElement(e.getChild("pubkey"));
			AsymmetricKeyPair akp = new AsymmetricKeyPair(pubkey.getModulusBytes(), pubkey.getPublicExponentBytes(), null);
			
			//generate key
			OSDXKeyObject key = OSDXKeyObject.fromKeyPair(akp);
			Identity idd = Identity.fromElement(e.getChild("identity"));
			key.addIdentity(idd);
			
			//generate keylog for approve pending
			Element ekl = new Element("keylog");
			Element eac = new Element("action");
			eac.addContent("date", OSDXKeyObject.datemeGMT.format(System.currentTimeMillis()));
			eac.addContent("ipv4", "na");
			eac.addContent("ipv6", "na");
			Element ef = new Element("from");
			ef.addContent("id",serverIDemail);
			ef.addContent("keyid",keyServerSigningKey.getKeyID());
			Element et = new Element("to");
			et.addContent("id",idd.getEmail());
			et.addContent("keyid",pubkey.getKeyID());
			Element eap = new Element("approval_pending");
			Element eo = new Element("of");
			eo.addContent(idd.toElement());
			eap.addContent(eo);
			eac.addContent(ef);
			eac.addContent(et);
			eac.addContent(eap);
			ekl.addContent(eac);
			System.out.println("action element ready");
			
			//byte[] sha1proof = SecurityHelper.getSHA1LocalProof(ekl);
			//e.addContent("sha1localproof", SecurityHelper.HexDecoder.encode(sha1proof, ':', -1));
			//e.addContent(Signature.createSignatureFromLocalProof(sha1proof, "keylogs", keyServerSigningKey).toElement());
			
			KeyLog kl = KeyLog.fromElement(ekl,false);
			kl.signoff(keyServerSigningKey);
			
			//save
			keystore.addKey(key);
			keystore.addKeyLog(kl);
			updateCache(key, null);
			
			//send response
			KeyServerResponse resp = new KeyServerResponse(serverid);
			
			//HT 2011-15-03 why needed?
//			resp.addHeaderValue("Response", "subkeys");
//			resp.addHeaderValue("KeyID", keyid);
//			resp.addHeaderValue("Identity", id);
//			resp.addHeaderValue("Message", "please authenticate via email");
			return resp;
		}
		return null;
		
	}
	
	private KeyServerResponse handlePutRevokeKeyRequest(KeyServerRequest request) throws Exception {
		throw new Exception("not implemented");
	}
	

	// public void readKeys(File f, char[] pass_mantra) throws Exception {
	// // KeyRingCollection krc = KeyRingCollection.fromFile(f, pass_mantra);
	// // //get the relevant sign-key from that collection
	// // this.sign_keys = krc.getSomeRandomKeyPair();
	// }

	public void handleSocket(final Socket s) throws Exception {
		// check on *too* many requests from one ip
		Thread t = new Thread() {
			public void run() {
				// should add entry to current_working_threads...
				try {
					InputStream _in = s.getInputStream();
					BufferedInputStream in = new BufferedInputStream(_in);
					KeyServerRequest request = KeyServerRequest.fromInputStream(in, s.getInetAddress().getHostAddress());
					KeyServerResponse response = prepareResponse(request, in); //this is ok since the request is small and can be kept in ram
					
					System.out.println("KeyServerSocket  | ::response ready");
					if (response != null) {
						System.out.print("SENDING THIS::");
						response.toOutput(System.out);
						System.out.println("::/SENDING_THIS");
						
						OutputStream out = s.getOutputStream();
						BufferedOutputStream bout = new BufferedOutputStream(out);
						response.toOutput(bout);
						bout.flush();
						bout.close();
					} 
					else {
						// TODO send error
						Exception ex = new Exception("RESPONSE COULD NOT BE CREATED");
						throw ex;
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};
		t.start();
	}

	public void startService() throws Exception {
		System.out.println("Starting Server on port " + port);
		ServerSocket so = new ServerSocket(port);
		if (address != null) {
			throw new RuntimeException("Not yet implemented...");
		}
		while (true) {
			try {
				final Socket me = so.accept();
				handleSocket(me);
			} catch (Exception ex) {
				ex.printStackTrace();
				Thread.sleep(250);// cooldown...
			}
		}
	}
	
	public KeyServerResponse prepareResponse(KeyServerRequest request, BufferedInputStream in) throws Exception {
		// yeah, switch cmd/method - stuff whatever...
		
		String cmd = request.cmd;
		System.out.println((new Date())+" :: "+request.ipv4+" :: KeyServerRequest | Method: "+request.method+"\tCmd: "+request.cmd);
		
		if (request.method.equals("POST")) {
			if (cmd.equals("/masterkey")) {
				return handlePutMasterKeyRequest(request);
			} 
			else if (cmd.equals("/revokekey")) {
				return handlePutRevokeKeyRequest(request);
			}
		} 
		else if (request.method.equals("GET")) {
			if (cmd.equals("/masterpubkeys")) {
				return handleMasterPubKeyRequest(request);
			} else if (cmd.equals("/identities")) {
				return handleIdentitiesRequest(request);
			} else if (cmd.equals("/keystatus")) {
				return handleKeyStatusRequest(request);
			} else if (cmd.equals("/keylogs")) {
				return handleKeyLogsRequest(request);
			} else if (cmd.equals("/subkeys")) {
				return handleSubKeyRequest(request);
			}
		}
//		else if (request.method.equals("HEAD")) {
//			throw new Exception("NOT IMPLEMENTED"); // correct would be to fire a HTTP_ERR
//		}
		else {
			throw new Exception("NOT IMPLEMENTED::METHOD: "+request.method); // correct would be to fire a HTTP_ERR
		}
		
		System.err.println("KeyServerResponse| ::request command not recognized:: "+cmd);
		return null;
	}

	public static void main(String[] args) throws Exception {
		KeyServerMain ss = new KeyServerMain();
		ss.port = 8889;

		ss.startService();
	}
}
