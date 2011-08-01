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

import java.util.HashMap;
import java.util.Vector;

import org.fnppl.opensdx.http.HTTPServerRequest;
import org.fnppl.opensdx.http.HTTPServerResponse;
import org.fnppl.opensdx.security.Identity;
import org.fnppl.opensdx.security.KeyApprovingStore;
import org.fnppl.opensdx.security.KeyLog;
import org.fnppl.opensdx.security.KeyLogAction;
import org.fnppl.opensdx.security.KeyStatus;
import org.fnppl.opensdx.security.MasterKey;
import org.fnppl.opensdx.security.OSDXKey;
import org.fnppl.opensdx.security.OSDXMessage;
import org.fnppl.opensdx.security.Result;
import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.security.SubKey;
import org.fnppl.opensdx.xml.Element;

//http://de.wikipedia.org/wiki/Hypertext_Transfer_Protocol

public class KeyServerResponse extends HTTPServerResponse {
	
	public KeyServerResponse(String serverid) {
		super(serverid);
	}
	
	public static KeyServerResponse createMasterPubKeyResponse(String serverid, HTTPServerRequest request, KeyServerBackend backend, OSDXKey signoffkey) {
		KeyServerResponse resp = new KeyServerResponse(serverid);
		String id = request.getParamValue("Identity");
		if (id != null) {
			Element e = new Element("masterpubkeys_response");
			e.addContent("identity",id);
			Vector<OSDXKey> keys = backend.getKeysToId(id);
			if (keys != null && keys.size() > 0) {
				Element er = new Element("related_keys");
				e.addContent(er);
				//add key ids
				for (OSDXKey k : keys) {
					er.addContent("keyid",k.getKeyID());
				}
			}
			try {
				OSDXMessage msg = OSDXMessage.buildMessage(e, signoffkey);
				resp.setContentElement(msg.toElement());
			} catch (Exception ex) {
				resp.setRetCode(404, "FAILED");
				resp.createErrorMessageContent("Internal Error"); //should/could never happen
			}
			return resp;
		}
		resp.setRetCode(404, "FAILED");
		resp.createErrorMessageContent("Missing parameter: Identity");
		return null;
	}
	
	public static KeyServerResponse createMasterPubKeyToSubKeyResponse(String serverid, HTTPServerRequest request, KeyServerBackend backend, OSDXKey signoffkey) {
		KeyServerResponse resp = new KeyServerResponse(serverid);
		String id = request.getParamValue("SubKeyID");
		if (id != null) {
			id = OSDXKey.getFormattedKeyIDModulusOnly(id);
			Element e = new Element("masterpubkey_response");
			OSDXKey key = backend.getKey(id);
			if (key!=null && key instanceof SubKey) {
				e.addContent("subkeyid", key.getKeyID());
				e.addContent(((SubKey)key).getParentKey().getSimplePubKeyElement());
			}
			try {
				OSDXMessage msg = OSDXMessage.buildMessage(e, signoffkey);
				resp.setContentElement(msg.toElement());
			} catch (Exception ex) {
				resp.setRetCode(404, "FAILED");
				resp.createErrorMessageContent("Internal Error"); //should/could never happen
			}
			return resp;
		}
		resp.setRetCode(404, "FAILED");
		resp.createErrorMessageContent("Missing parameter: SubKeyID");
		return null;
	}
	
	public static KeyServerResponse createIdentityResponse(String serverid, HTTPServerRequest request, KeyServerBackend backend, OSDXKey signoffkey) {
		KeyServerResponse resp = new KeyServerResponse(serverid);
		String keyid = request.getParamValue("KeyID");
		boolean showRestricted = false;
		if (request.xml!=null && request.xml.getRootElement()!=null) {
			//allow if identity owner approved signing key in osdxmessage
			try {
				OSDXMessage msg = OSDXMessage.fromElement(request.xml.getRootElement());
				Result verify = msg.verifySignaturesWithoutKeyVerification();
				if (verify.succeeded) {
					OSDXKey sign = msg.getSignatures().get(0).getKey();
					showRestricted = allowRestricted(keyid, sign, backend);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		if (keyid != null) {
			keyid = OSDXKey.getFormattedKeyIDModulusOnly(keyid);
			Element e = new Element("identities_response");
			e.addContent("keyid",keyid);
			OSDXKey key = backend.getKey(keyid);
			if (key != null && key instanceof MasterKey) {
				Vector<Identity> ids = ((MasterKey)key).getIdentities();
				for (Identity aid : ids) {
					e.addContent(aid.toElement(showRestricted)); 
				}
			}
			try {
				OSDXMessage msg = OSDXMessage.buildMessage(e, signoffkey);
				resp.setContentElement(msg.toElement());
			} catch (Exception ex) {
				resp.setRetCode(404, "FAILED");
				resp.createErrorMessageContent("Internal Error"); //should/could never happen
			}
			return resp;
		}
		resp.setRetCode(404, "FAILED");
		resp.createErrorMessageContent("Missing parameter: KeyID");
		return null;
	}
	
	private static boolean allowRestricted(String from_keyid, OSDXKey sign, KeyServerBackend backend)  {
		try {
			boolean allow = false; 
			String keyidSign = OSDXKey.getFormattedKeyIDModulusOnly(sign.getKeyID());
			Vector<KeyLog> logs = backend.getKeyLogsToID(keyidSign);
			if (logs!=null) {
				SecurityHelper.sortKeyLogsbyDate(logs);
				for (KeyLog log : logs) {
					System.out.println("log from: "+log.getKeyIDFrom()+" "+log.getAction());
					if (log.getKeyIDFrom().equals(from_keyid) && log.getAction().equals(KeyLogAction.APPROVAL)) {
						allow = true;							
					}
					if (log.getKeyIDFrom().equals(from_keyid) && log.getAction().equals(KeyLogAction.REVOCATION)) {
						allow = false;
					}
				}
			}
			System.out.println("allow: "+allow);
			return allow;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	public static KeyServerResponse createKeyStatusyResponse(String serverid, HTTPServerRequest request, KeyServerBackend backend, OSDXKey signoffkey) {
		KeyServerResponse resp = new KeyServerResponse(serverid);
		String id = request.getParamValue("KeyID");
		if (id != null) {
			id = OSDXKey.getFormattedKeyIDModulusOnly(id);
			Element e = new Element("keystatus_response");
			e.addContent("keyid",id);
			try {
				KeyStatus ks = backend.getKeyStatus(id);
				if (ks!=null) {
					e.addContent(ks.toElement());
//					Vector<Element> status = ks.toElement().getChildren();
//					for (Element es : status) {
//						e.addContent(XMLHelper.cloneElement(es));
//					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			try {
				OSDXMessage msg = OSDXMessage.buildMessage(e, signoffkey);
				resp.setContentElement(msg.toElement());
			} catch (Exception ex) {
				resp.setRetCode(404, "FAILED");
				resp.createErrorMessageContent("Internal Error"); //should/could never happen
			}
			return resp;
		}
		resp.setRetCode(404, "FAILED");
		resp.createErrorMessageContent("Missing parameter: KeyID");
		return null;
	}
	
	public static KeyServerResponse createKeyLogResponse(String serverid, HTTPServerRequest request, KeyServerBackend backend, OSDXKey signoffkey) {
		KeyServerResponse resp = new KeyServerResponse(serverid);
		String keyid = request.getParamValue("KeyID");
		if (keyid != null) {
			String keyid_short = OSDXKey.getFormattedKeyIDModulusOnly(keyid);
			Element e = new Element("keylogactions_response");
			e.addContent("keyid",keyid);
			Vector<KeyLog> keylogs = backend.getKeyLogsToID(keyid_short);
			if (keylogs!=null && keylogs.size()>0) {
				//decide if client can see restricted fields
				boolean showRestricted = false;
				if (request.xml!=null && request.xml.getRootElement()!=null) {
					//allow if identity owner approved signing key in osdxmessage
					try {
						OSDXMessage msg = OSDXMessage.fromElement(request.xml.getRootElement());
						Result verify = msg.verifySignaturesWithoutKeyVerification();
						if (verify.succeeded) {
							OSDXKey sign = msg.getSignatures().get(0).getKey();
							showRestricted = allowRestricted(keyid, sign, backend);
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				for (KeyLog log : keylogs) {	
					e.addContent(log.toElement(showRestricted));
				}
			}
			try {
				OSDXMessage msg = OSDXMessage.buildMessage(e, signoffkey);
				resp.setContentElement(msg.toElement());
			} catch (Exception ex) {
				resp.setRetCode(404, "FAILED");
				resp.createErrorMessageContent("Internal Error"); //should/could never happen
			}
			return resp;
		}
		resp.setRetCode(404, "FAILED");
		resp.createErrorMessageContent("Missing parameter: KeyID");
		return null;
	}
	
	
	public static KeyServerResponse createSubKeyResponse(String serverid, HTTPServerRequest request, KeyServerBackend backend, OSDXKey signoffkey) {
		KeyServerResponse resp = new KeyServerResponse(serverid);
		String id = request.getParamValue("KeyID");
		if (id != null) {
			id = OSDXKey.getFormattedKeyIDModulusOnly(id);
			Element e = new Element("subkeys_response");
			e.addContent("parentkeyid", id);
			Vector<OSDXKey> subkeys = backend.getSubKeysToId(id);
			if (subkeys!=null && subkeys.size()>0) {
				for (OSDXKey key : subkeys) {
					if (key.isSub())
						e.addContent("keyid",key.getKeyID());
				}
				
			}
			try {
				OSDXMessage msg = OSDXMessage.buildMessage(e, signoffkey);
				resp.setContentElement(msg.toElement());
			} catch (Exception ex) {
				resp.setRetCode(404, "FAILED");
				resp.createErrorMessageContent("Internal Error"); //should/could never happen
			}
			return resp;
		}
		resp.setRetCode(404, "FAILED");
		resp.createErrorMessageContent("Missing parameter: KeyID");
		return null;
	}
	
	public static KeyServerResponse createPubKeyResponse(String serverid, HTTPServerRequest request, KeyServerBackend backend, OSDXKey signoffkey) {
		KeyServerResponse resp = new KeyServerResponse(serverid);
		String id = request.getParamValue("KeyID");
		if (id != null) {
			id = OSDXKey.getFormattedKeyIDModulusOnly(id);
			Element e = new Element("pubkey_response");
			OSDXKey key = backend.getKey(id);
			if (key!=null) {
				e.addContent(key.getSimplePubKeyElement());
			}
			try {
				OSDXMessage msg = OSDXMessage.buildMessage(e, signoffkey);
				resp.setContentElement(msg.toElement());
			} catch (Exception ex) {
				resp.setRetCode(404, "FAILED");
				resp.createErrorMessageContent("Internal Error"); //should/could never happen
			}
			return resp;
		}
		resp.setRetCode(404, "FAILED");
		resp.createErrorMessageContent("Missing parameter: KeyID");
		return null;
	}
}
