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

//http://de.wikipedia.org/wiki/Hypertext_Transfer_Protocol

public class KeyServerResponse {
//	public int retcode = 404;//fail
//	public String retcodeString = "FAYUL!";
	public int retcode = 200;//fail
	public String retcodeString = "OK";
	public String contentType = "text/xml";
	
	private String serverid = null;
	private Element contentElement;
	
	
	public KeyServerResponse(String serverid) {
		this.serverid = serverid;
		contentElement = null;
	}
	public void setRetCode(int code, String msg) {
		this.retcode = code;
		this.retcodeString = msg;
	}
	
	public void toOutput(OutputStream out) throws Exception {
		//write it to the outputstream...
		if (contentElement != null) {
			ByteArrayOutputStream contentout = new ByteArrayOutputStream();
			
			Document xml = Document.buildDocument(contentElement);
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
		else {
			out.write((
					"HTTP/1.1 "+retcode+" "+retcodeString+"\r\n" +
					"Server: "+serverid+"\r\n" +
					"Connection: close\r\n").getBytes("ASCII"));
			out.write("\r\n".getBytes("ASCII"));
		}
		out.flush();
	}
	
//	public void addHeaderValue(String name, String value) {
//		header.append(name+": "+value+"\n");
//	}
	
	public void setContentElement(Element e) {
		contentElement = e;
	}
}
