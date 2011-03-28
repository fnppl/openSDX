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

import org.fnppl.opensdx.security.*;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;
import org.fnppl.opensdx.xml.XMLHelper;

//http://de.wikipedia.org/wiki/Hypertext_Transfer_Protocol

public class KeyServerResponse {
//	public int retcode = 404;//fail
//	public String retcodeString = "FAYUL!";
	public int retcode = 200;//fail
	public String retcodeString = "OK";
	public String contentType = "text/xml";
	
	private String serverid = null;
	protected Element contentElement;
	protected String html = null;
	protected OSDXKeyObject signoffkey = null;
	
	public KeyServerResponse(String serverid) {
		this.serverid = serverid;
		contentElement = null;
	}
	public void setRetCode(int code, String msg) {
		this.retcode = code;
		this.retcodeString = msg;
	}
	
	public void setSignoffKey(OSDXKeyObject signoffkey) {
		this.signoffkey = signoffkey;
	}
	
	public void toOutput(OutputStream out) throws Exception {
		//write it to the outputstream...
		if (contentElement != null) {
			Element eContent = contentElement;
			
			//signoff if signoffkey present
			if (signoffkey!=null) {
				eContent = XMLHelper.cloneElement(contentElement);
				//signoff
				byte[] sha1proof = SecurityHelper.getSHA1LocalProof(eContent);
				eContent.addContent("sha1localproof", SecurityHelper.HexDecoder.encode(sha1proof, ':', -1));
				eContent.addContent(Signature.createSignatureFromLocalProof(sha1proof, "signature of sha1localproof", signoffkey).toElement());
			}
			
			ByteArrayOutputStream contentout = new ByteArrayOutputStream();
			
			Document xml = Document.buildDocument(eContent);
			xml.output(contentout);
			contentout.flush();
			contentout.close();
			
			byte[] content = contentout.toByteArray();
			out.write((
					"HTTP/1.1 "+retcode+" "+retcodeString+"\r\n" +
					"Server: "+serverid+"\r\n" +
					"Connection: close\r\n").getBytes("ASCII"));
			
			out.write(("Content-Type: "+contentType+"\r\n").getBytes("ASCII"));
			out.write(("Content-Length: "+content.length+"\r\n").getBytes("ASCII"));
			out.write("\r\n".getBytes("ASCII"));
			out.flush();
			out.write(content);
		} 
		else if (html!=null ){
			out.write((
					"HTTP/1.1 "+retcode+" "+retcodeString+"\r\n" +
					"Server: "+serverid+"\r\n" +
					"Connection: close\r\n").getBytes("ASCII"));
			out.write(("Content-Type: text/html\r\n").getBytes("ASCII"));
			out.write(("Content-Length: "+html.length()+"\r\n").getBytes("ASCII"));
			out.write("\r\n".getBytes("ASCII"));
			out.flush();
			out.write(html.getBytes("ASCII"));
			out.write("\r\n".getBytes("ASCII"));
		}
		else {
			out.write((
					"HTTP/1.1 "+retcode+" "+retcodeString+"\r\n" +
					"Server: "+serverid+"\r\n" +
					"Connection: close\r\n").getBytes("ASCII"));
			out.write("\r\n".getBytes("ASCII"));
		}
		out.flush();
	}
	
	public void createErrorMessageContent(String msg) {
		Element em = new Element("errormessage");
		em.addContent("message",msg);
		setContentElement(em);
	}
	
//	public void addHeaderValue(String name, String value) {
//		header.append(name+": "+value+"\n");
//	}
	
	public void setContentElement(Element e) {
		contentElement = e;
	}
	
	public void setHTML(String html) {
		this.html = html;
	}
	
	public static KeyServerResponse createMasterPubKeyResponse(String serverid, KeyServerRequest request, HashMap<String, Vector<OSDXKeyObject>> id_keys, OSDXKeyObject signoffkey) {
		KeyServerResponse resp = new KeyServerResponse(serverid);
		String id = request.getParamValue("Identity");
		if (id != null) {
			Element e = new Element("masterpubkeys_response");
			e.addContent("identity",id);
			Vector<OSDXKeyObject> keys = id_keys.get(id);
			if (keys != null && keys.size() > 0) {
				Element er = new Element("related_keys");
				e.addContent(er);
				//add key ids
				for (OSDXKeyObject k : keys) {
					er.addContent("keyid",k.getKeyID());
				}
				resp.setSignoffKey(signoffkey); //only signoff if real content
			}
			resp.setContentElement(e);
			return resp;
		}
		resp.setRetCode(404, "FAILED");
		resp.createErrorMessageContent("Missing parameter: Identity");
		return null;
	}
	
	public static KeyServerResponse createIdentityResponse(String serverid, KeyServerRequest request, HashMap<String, OSDXKeyObject> keyid_key, OSDXKeyObject signoffkey) {
		KeyServerResponse resp = new KeyServerResponse(serverid);
		String id = request.getParamValue("KeyID");
		if (id != null) {
			id = OSDXKeyObject.getFormattedKeyIDModulusOnly(id);
			Element e = new Element("identities_response");
			e.addContent("keyid",id);
			OSDXKeyObject key = keyid_key.get(id);
			if (key != null) {
				Vector<Identity> ids = key.getIdentities();
				for (Identity aid : ids) {
					e.addContent(aid.toElement());
				}
				resp.setSignoffKey(signoffkey); //only signoff if real content
			}
			resp.setContentElement(e);
			return resp;
		}
		resp.setRetCode(404, "FAILED");
		resp.createErrorMessageContent("Missing parameter: KeyID");
		return null;
	}
	
	public static KeyServerResponse createKeyStatusyResponse(String serverid, KeyServerRequest request, KeyApprovingStore keystore, OSDXKeyObject signoffkey) {
		KeyServerResponse resp = new KeyServerResponse(serverid);
		String id = request.getParamValue("KeyID");
		if (id != null) {
			id = OSDXKeyObject.getFormattedKeyIDModulusOnly(id);
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
			resp.setContentElement(e);
			resp.setSignoffKey(signoffkey);
			return resp;
		}
		resp.setRetCode(404, "FAILED");
		resp.createErrorMessageContent("Missing parameter: KeyID");
		return null;
	}
	
	public static KeyServerResponse createKeyLogResponse(String serverid, KeyServerRequest request, HashMap<String, Vector<KeyLog>> keyid_log, OSDXKeyObject signoffkey) {
		KeyServerResponse resp = new KeyServerResponse(serverid);
		String id = request.getParamValue("KeyID");
		if (id != null) {
			id = OSDXKeyObject.getFormattedKeyIDModulusOnly(id);
			Element e = new Element("keylogs_response");
			e.addContent("keyid",id);
			Vector<KeyLog> keylogs = keyid_log.get(id);
			if (keylogs!=null && keylogs.size()>0) {
				for (KeyLog log : keylogs) {
					e.addContent(log.toElement());
				}
				
			}
			resp.setContentElement(e);
			resp.setSignoffKey(signoffkey);
			return resp;
		}
		resp.setRetCode(404, "FAILED");
		resp.createErrorMessageContent("Missing parameter: KeyID");
		return null;
	}
	
	
	public static KeyServerResponse createSubKeyResponse(String serverid, KeyServerRequest request, HashMap<String, Vector<OSDXKeyObject>> keyid_subkeys, OSDXKeyObject signoffkey) {
		KeyServerResponse resp = new KeyServerResponse(serverid);
		String id = request.getParamValue("KeyID");
		if (id != null) {
			id = OSDXKeyObject.getFormattedKeyIDModulusOnly(id);
			Element e = new Element("subkeys_response");
			e.addContent("parentkeyid", id);
			Vector<OSDXKeyObject> subkeys = keyid_subkeys.get(id);
			if (subkeys!=null && subkeys.size()>0) {
				for (OSDXKeyObject key : subkeys) {
					if (key.isSub())
						e.addContent("keyid",key.getKeyID());
				}
				
			}
			resp.setSignoffKey(signoffkey);
			resp.setContentElement(e);
			return resp;
		}
		resp.setRetCode(404, "FAILED");
		resp.createErrorMessageContent("Missing parameter: KeyID");
		return null;
	}
	
	public static KeyServerResponse createPubKeyResponse(String serverid, KeyServerRequest request, HashMap<String, OSDXKeyObject> keyid_key, OSDXKeyObject signoffkey) {
		KeyServerResponse resp = new KeyServerResponse(serverid);
		String id = request.getParamValue("KeyID");
		if (id != null) {
			id = OSDXKeyObject.getFormattedKeyIDModulusOnly(id);
			Element e = new Element("pubkey_response");
			OSDXKeyObject key = keyid_key.get(id);
			if (key!=null) {
				e.addContent(key.getSimplePubKeyElement());
				resp.setSignoffKey(signoffkey);
			}
			resp.setContentElement(e);
			return resp;
		}
		resp.setRetCode(404, "FAILED");
		resp.createErrorMessageContent("Missing parameter: KeyID");
		return null;
	}
}
