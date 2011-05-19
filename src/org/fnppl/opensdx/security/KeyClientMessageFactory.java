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

import java.util.Vector;

import org.fnppl.opensdx.http.HTTPClientRequest;
import org.fnppl.opensdx.xml.Element;

public class KeyClientMessageFactory {
	
	public static String KEYSERVER_SETTINGS_RESPONSE = "keyserver";
	public static String MASTERPUBKEYS_RESPONSE = "masterpubkeys_response";
	public static String MASTERPUBKEY_RESPONSE = "masterpubkey_response";
	public static String IDENTITIES_RESPONSE = "identities_response";
	public static String KEYSTATUS_RESPONSE = "keystatus_response";
	public static String SUBKEYS_RESPONSE = "subkeys_response";
	public static String KEYLOGS_RESPONSE = "keylogs_response";
	public static String PUBLICKEY_RESPONSE = "pubkey_response";
	
	
	public static HTTPClientRequest buildRequestKeyServerIdentity(String host, String prepath) {
		HTTPClientRequest req = new HTTPClientRequest();
		req.setURI(host, prepath+"/keyserversettings");
		req.toggleGETMode();
		return req;
	}
	
	public static HTTPClientRequest buildRequestIdentities(String host, String prepath, String keyid) {
		HTTPClientRequest req = new HTTPClientRequest();
		req.setURI(host, prepath+"/identities");
		req.toggleGETMode();
		req.addRequestParam("KeyID", keyid);
		return req;
	}
	
	public static HTTPClientRequest buildRequestKeyStatus(String host, String prepath, String keyid) {
		HTTPClientRequest req = new HTTPClientRequest();
		req.setURI(host, prepath+"/keystatus");
		req.addRequestParam("KeyID", keyid);
		req.toggleGETMode();
		
		return req;
	}
	
	public static HTTPClientRequest buildRequestMasterPubKeys(String host, String prepath, String idemail) {
		HTTPClientRequest req = new HTTPClientRequest();
		req.setURI(host, prepath+"/masterpubkeys");
		req.toggleGETMode();
		req.addRequestParam("Identity", idemail);		
		return req;
	}
	
	public static HTTPClientRequest buildRequestMasterPubKey(String host, String prepath, String subkeyid) {
		HTTPClientRequest req = new HTTPClientRequest();
		req.setURI(host, prepath+"/masterpubkey");
		req.toggleGETMode();
		req.addRequestParam("SubKeyID", subkeyid);		
		return req;
	}
	
	public static HTTPClientRequest buildRequestSubkeys(String host, String prepath, String masterkeyid) {
		HTTPClientRequest req = new HTTPClientRequest();
		req.setURI(host, prepath+"/subkeys");
		req.toggleGETMode();
		req.addRequestParam("KeyID", masterkeyid);		
	
		return req;
	}
	
	public static HTTPClientRequest buildRequestPublicKey(String host, String prepath, String keyid) {
		HTTPClientRequest req = new HTTPClientRequest();
		req.setURI(host, prepath+"/pubkey");
		req.toggleGETMode();
		req.addRequestParam("KeyID", keyid);		
	
		return req;
	}
	
	
//	public static OSDXKeyServerClientRequest getRequestPubKeys(String host, String idemail) {
//		OSDXKeyServerClientRequest req = new OSDXKeyServerClientRequest();
//		req.setURI(host, "/pubkeys");
//		req.toggleGETMode();
//		req.addRequestParam("Identity", idemail);		
//	
//		return req;
//	}
	
	public static HTTPClientRequest buildRequestKeyLogs(String host, String prepath, String keyid) {
		HTTPClientRequest req = new HTTPClientRequest();
		req.setURI(host, prepath+"/keylogs");
		req.toggleGETMode();
		req.addRequestParam("KeyID", keyid);
		return req;
	}
	
	
	public static HTTPClientRequest buildPutRequestMasterKey(String host, String prepath, OSDXKey masterkey, Identity id) throws Exception {
		HTTPClientRequest req = new HTTPClientRequest();
		req.setURI(host, prepath+"/masterkey");
		
		req.addRequestParam("KeyID", masterkey.getKeyID());		
		req.addRequestParam("Identity", id.getEmail());
	
		Element content = new Element("masterpubkey");
		content.addContent(masterkey.getSimplePubKeyElement());
		content.addContent(id.toElement(true));
		OSDXMessage msg = OSDXMessage.buildMessage(content, masterkey); //self-signoff with masterkey
		req.setContentElement(msg.toElement());
		return req;
	}
	
	public static HTTPClientRequest buildPutRequestRevokeKey(String host, String prepath, OSDXKey revokekey, OSDXKey relatedMasterKey) throws Exception {
		HTTPClientRequest req = new HTTPClientRequest();
		req.setURI(host, prepath+"/revokekey");
		
		Element content = new Element("revokekey");
		content.addContent("masterkeyid", relatedMasterKey.getKeyModulusSHA1());
		content.addContent(revokekey.getSimplePubKeyElement());
		
		OSDXMessage msg = OSDXMessage.buildMessage(content, revokekey);//first signoff with revokekey
		//then signoff with relatedMasterKey
		msg.signLastSignature(relatedMasterKey, "signatue of signaturebytes of revokekey");
		req.setContentElement(msg.toElement());
		
		return req;
	}
	
	public static HTTPClientRequest buildPutRequestRevokeMasterKey(String host, String prepath, OSDXKey revokekey, OSDXKey relatedMasterKey, String message) throws Exception {
		HTTPClientRequest req = new HTTPClientRequest();
		req.setURI(host, prepath+"/revokemasterkey");
		
		Element content = new Element("revokemasterkey");
		content.addContent("from_keyid", revokekey.getKeyID());
		content.addContent("to_keyid", relatedMasterKey.getKeyID());
		if (message!=null && message.length()>0)
			content.addContent("message",message);
		
		OSDXMessage msg = OSDXMessage.buildMessage(content, revokekey);  //signoff with revokekey
		req.setContentElement(msg.toElement());		
		return req;
	}
	
	public static HTTPClientRequest buildPutRequestRevokeSubKey(String host, String prepath,  OSDXKey subkey, OSDXKey relatedMasterKey, String message) throws Exception {
		HTTPClientRequest req = new HTTPClientRequest();
		req.setURI(host, prepath+"/revokesubkey");
		
		Element content = new Element("revokesubkey");
		content.addContent("from_keyid", relatedMasterKey.getKeyID());
		content.addContent("to_keyid", subkey.getKeyID());
		if (message!=null && message.length()>0)
			content.addContent("message",message);
		
		OSDXMessage msg = OSDXMessage.buildMessage(content, relatedMasterKey);  //signoff with masterkey
		req.setContentElement(msg.toElement());		
		return req;
	}
	
	public static HTTPClientRequest buildPutRequestSubKey(String host, String prepath, OSDXKey subkey, OSDXKey relatedMasterKey) throws Exception {
		HTTPClientRequest req = new HTTPClientRequest();
		req.setURI(host, prepath+"/subkey");
		
		Element content = new Element("subkey");
		content.addContent("masterkeyid", relatedMasterKey.getKeyModulusSHA1());
		content.addContent(subkey.getSimplePubKeyElement());
		
		OSDXMessage msg = OSDXMessage.buildMessage(content, subkey);//first signoff with subkey
		//then signoff with relatedMasterKey
		msg.signLastSignature(relatedMasterKey, "signatue of signaturebytes of subkey");
		req.setContentElement(msg.toElement());
		
		return req;
	}
	
	public static HTTPClientRequest getPutRequestKeyLogs(String host, String prepath, Vector<KeyLog> keylogs, OSDXKey signingKey) throws Exception {
		HTTPClientRequest req = new HTTPClientRequest();
		req.setURI(host, prepath+"/keylogs");
		
		Element content = new Element("keylogactions");
		for (KeyLog k : keylogs) {
			content.addContent(k.toKeyLogActionElement());
		}
		
		OSDXMessage msg = OSDXMessage.buildMessage(content, signingKey);
		req.setContentElement(msg.toElement());
		
		return req;
	}
	
}
