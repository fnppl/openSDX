package org.fnppl.opensdx.http;


/*
 * Copyright (C) 2010-2012 
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

import org.fnppl.opensdx.keyserver.KeyServerResponse;
import org.fnppl.opensdx.security.*;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;
import org.fnppl.opensdx.xml.XMLHelper;

//http://de.wikipedia.org/wiki/Hypertext_Transfer_Protocol

public class HTTPServerResponse {
//	public int retcode = 404;//fail
//	public String retcodeString = "FAYUL!";
	public int retcode = 200;//fail
	public String retcodeString = "OK";
	public String contentType = "text/xml";
	
	private String serverid = null;
	protected Element contentElement;
	protected String html = null;
	protected String text = null;
	protected OSDXKey signoffkey = null;
	
	public HTTPServerResponse(String serverid) {
		this.serverid = serverid;
		contentElement = null;
	}
	public void setRetCode(int code, String msg) {
		this.retcode = code;
		this.retcodeString = msg;
	}
	
	public void setSignoffKey(OSDXKey signoffkey) {
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
			byte[] tc = html.getBytes("ASCII");
			out.write((
					"HTTP/1.1 "+retcode+" "+retcodeString+"\r\n" +
					"Server: "+serverid+"\r\n" +
					"Connection: close\r\n").getBytes("ASCII"));
			out.write(("Content-Type: text/html\r\n").getBytes("ASCII"));
			out.write(("Content-Length: "+tc.length+"\r\n").getBytes("ASCII"));
			out.write("\r\n".getBytes("ASCII"));
			out.flush();
			out.write(tc);
			out.write("\r\n".getBytes("ASCII"));
		}
		else if (text!=null ){
			byte[] tc = text.getBytes("ASCII");
			out.write((
					"HTTP/1.1 "+retcode+" "+retcodeString+"\r\n" +
					"Server: "+serverid+"\r\n" +
					"Connection: close\r\n").getBytes("ASCII"));
			out.write(("Content-Type: text/plain\r\n").getBytes("ASCII"));
			out.write(("Content-Length: "+tc.length+"\r\n").getBytes("ASCII"));
			out.write("\r\n".getBytes("ASCII"));
			out.flush();
			out.write(tc);
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
	
	public void setContentElement(Element e) {
		contentElement = e;
	}
	
	public void setHTML(String html) {
		this.html = html;
	}
	
	public void setContentText(String text) {
		this.text = text;
	}
	
	public static HTTPServerResponse createResponse(String serverid, Element xmlContent, OSDXKey signoffkey) {
		HTTPServerResponse resp = new HTTPServerResponse(serverid);
		try {
			OSDXMessage msg = OSDXMessage.buildMessage(xmlContent, signoffkey);
			resp.setContentElement(msg.toElement());
		} catch (Exception ex) {
			resp.setRetCode(404, "FAILED");
			resp.createErrorMessageContent("Internal Error"); //should/could never happen
		}
		return resp;
	}
	
	public static HTTPServerResponse errorMessage(String serverid, String msg) {
		KeyServerResponse resp = new KeyServerResponse(serverid);
		resp.setRetCode(404, "FAILED");
		resp.createErrorMessageContent(msg);
		return resp;
	}
	
}
