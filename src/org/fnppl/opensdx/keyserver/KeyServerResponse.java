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
import java.net.*;
import java.util.*;

import org.fnppl.opensdx.http.HTTPServerRequest;
import org.fnppl.opensdx.http.HTTPServerResponse;
import org.fnppl.opensdx.security.*;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;
import org.fnppl.opensdx.xml.XMLHelper;

//http://de.wikipedia.org/wiki/Hypertext_Transfer_Protocol

public class KeyServerResponse extends HTTPServerResponse {
	
	public KeyServerResponse(String serverid) {
		super(serverid);
	}
	
	public static KeyServerResponse createMasterPubKeyResponse(String serverid, HTTPServerRequest request, HashMap<String, Vector<OSDXKey>> id_keys, OSDXKey signoffkey) {
		KeyServerResponse resp = new KeyServerResponse(serverid);
		String id = request.getParamValue("Identity");
		if (id != null) {
			Element e = new Element("masterpubkeys_response");
			e.addContent("identity",id);
			Vector<OSDXKey> keys = id_keys.get(id);
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
	
	public static KeyServerResponse createMasterPubKeyToSubKeyResponse(String serverid, HTTPServerRequest request, HashMap<String, OSDXKey> keyid_key, OSDXKey signoffkey) {
		KeyServerResponse resp = new KeyServerResponse(serverid);
		String id = request.getParamValue("SubKeyID");
		if (id != null) {
			id = OSDXKey.getFormattedKeyIDModulusOnly(id);
			Element e = new Element("masterpubkey_response");
			OSDXKey key = keyid_key.get(id);
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
	
	public static KeyServerResponse createIdentityResponse(String serverid, HTTPServerRequest request, HashMap<String, OSDXKey> keyid_key, OSDXKey signoffkey) {
		KeyServerResponse resp = new KeyServerResponse(serverid);
		String id = request.getParamValue("KeyID");
		if (id != null) {
			id = OSDXKey.getFormattedKeyIDModulusOnly(id);
			Element e = new Element("identities_response");
			e.addContent("keyid",id);
			OSDXKey key = keyid_key.get(id);
			if (key != null && key instanceof MasterKey) {
				Vector<Identity> ids = ((MasterKey)key).getIdentities();
				for (Identity aid : ids) {
					e.addContent(aid.toElement(false));  //TODO allow if approval of identity owner 
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
	
	public static KeyServerResponse createKeyStatusyResponse(String serverid, HTTPServerRequest request, KeyApprovingStore keystore, OSDXKey signoffkey) {
		KeyServerResponse resp = new KeyServerResponse(serverid);
		String id = request.getParamValue("KeyID");
		if (id != null) {
			id = OSDXKey.getFormattedKeyIDModulusOnly(id);
			Element e = new Element("keystatus_response");
			e.addContent("keyid",id);
			try {
				KeyStatus ks = keystore.getKeyStatus(id);
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
	
	public static KeyServerResponse createKeyLogResponse(String serverid, HTTPServerRequest request, HashMap<String, Vector<KeyLog>> keyid_log, OSDXKey signoffkey) {
		KeyServerResponse resp = new KeyServerResponse(serverid);
		String id = request.getParamValue("KeyID");
		if (id != null) {
			id = OSDXKey.getFormattedKeyIDModulusOnly(id);
			Element e = new Element("keylogs_response");
			e.addContent("keyid",id);
			Vector<KeyLog> keylogs = keyid_log.get(id);
			if (keylogs!=null && keylogs.size()>0) {
				for (KeyLog log : keylogs) {
					e.addContent(log.toFullElement());
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
	
	
	public static KeyServerResponse createSubKeyResponse(String serverid, HTTPServerRequest request, HashMap<String, Vector<OSDXKey>> keyid_subkeys, OSDXKey signoffkey) {
		KeyServerResponse resp = new KeyServerResponse(serverid);
		String id = request.getParamValue("KeyID");
		if (id != null) {
			id = OSDXKey.getFormattedKeyIDModulusOnly(id);
			Element e = new Element("subkeys_response");
			e.addContent("parentkeyid", id);
			Vector<OSDXKey> subkeys = keyid_subkeys.get(id);
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
	
	public static KeyServerResponse createPubKeyResponse(String serverid, HTTPServerRequest request, HashMap<String, OSDXKey> keyid_key, OSDXKey signoffkey) {
		KeyServerResponse resp = new KeyServerResponse(serverid);
		String id = request.getParamValue("KeyID");
		if (id != null) {
			id = OSDXKey.getFormattedKeyIDModulusOnly(id);
			Element e = new Element("pubkey_response");
			OSDXKey key = keyid_key.get(id);
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
