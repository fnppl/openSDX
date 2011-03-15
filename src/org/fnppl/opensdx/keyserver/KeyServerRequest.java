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


public class KeyServerRequest {
	public Hashtable<String, String> headers = new Hashtable<String, String>();
	public Hashtable<String, String> parameters = new Hashtable<String, String>();
	
	public String cmd = null;
	public String method = null;
	public Document xml;
	
	public static KeyServerRequest fromInputStream(InputStream in) throws Exception {
		//parse data
		//reject stupid requests (e.g. myriard-long single-lines in header)
		KeyServerRequest ret = new KeyServerRequest();
		ret.xml = null;
//		ret.headers = new Hashtable<String, String>(); //already done in class-definition
//		ret.parameters = new Hashtable<String, String>(); //already done in class-definition
		
		String zeile = null;
		zeile = readLineASCII(in); //cmdline
		
		String[] t = zeile.split(" ");
		ret.method = t[0];
		ret.cmd = t[1];
		String proto = t[2]; // HTTP/1.0 or HTTP/1.1
		
		readHeader(in, ret);
		System.out.println("::header end::");
		//if (ret.headers.containsKey("Content-Type") && ret.headers.get("Content-Type").equals("text/xml")) {
		if(ret.method.equals("POST")) {
			readXMLPostContent(in, ret);
		}
		else if(ret.method.equals("GET")) {
			//HT 2011-03-15 parse GET-params
		}

		System.out.println("KeyServerRequest | end of request");
		
		//-------------------------return ret;
		
		return ret;
	}
	
	private static void readXMLPostContent(InputStream in, KeyServerRequest re) throws Exception {
		System.out.println("KeyServerRequest::reading xml POST content");
//		while(in.available() == 0) { //wait for data
//			Thread.sleep(100); 
//		}
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		int read = 0;
		int toread = Integer.parseInt(re.headers.get("Content-Length"));
		System.out.println("KeyServerRequest::Content-length: "+toread);
		byte[] buff = new byte[4096];
		//while((read=in.read(buff))!=-1) {
		while((read = in.read(buff,0, Math.min(buff.length, toread))) != -1) {
//			read = in.read(buff); 
			bout.write(buff, 0, read);
			toread -= read;
			if(toread == 0) {
				break;
			}
		}
		
		String s = new String(bout.toByteArray());
		s = URLDecoder.decode(s, "UTF-8"); //urlencoded-form-data
		
		System.out.println("KeyServerRequest::GOT THIS AS DOC: ::START::"+s+"::END::");
		
//		String last = s.substring(s.lastIndexOf(">"));
//		System.out.println("last bytes: "+SecurityHelper.HexDecoder.encode(last.getBytes("UTF-8"), ':',-1));
//		s = s.substring(0, s.lastIndexOf(">")+3);
//		String last2 = s.substring(s.lastIndexOf(">"));
//		System.out.println("last bytes: "+SecurityHelper.HexDecoder.encode(last2.getBytes("UTF-8"), ':',-1));
		
//		System.out.println("GOT THIS AS DOC: ::"+s+"::");
		re.xml = Document.fromStream(new ByteArrayInputStream(s.getBytes()));
	}
	
	private static void readHeader(InputStream in, KeyServerRequest re) throws Exception {
		String zeile = null;

		while ((zeile=readLineASCII(in)) != null) {
			if(zeile.length() == 0) {
				return;//heade-ende
			}
			//header
			String[] p = parseHeader(zeile);
			re.headers.put(p[0], p[1]);
			System.out.println("h: "+zeile);
		}
	}
	
	private static String[] parseHeader(String zeile) {
		StringTokenizer st = new StringTokenizer(zeile, " ");
		String[] ret = new String[2];
		ret[0] = st.nextToken();
		ret[0] = ret[0].substring(0, ret[0].length()-1);//cut off ":"
		
		ret[1] = st.nextToken();
		
		return ret;
	}

	private static String readLineASCII(InputStream in) throws Exception {
		//if (in.available()<=0) return null;
		ByteArrayOutputStream bout = new ByteArrayOutputStream();

		//HEADERS are ASCII

		byte[] b = new byte[1];
		int r = 0;

		char last='\r';

		while((r=in.read(b)) > 0) {
			char m = (char)b[0];
			if(m == '\n') {
				break;
			} else if(m != '\r') {
				bout.write(b[0]);
			}  
		}

		if(r<0 && bout.size()==0) {
			return null;
		}
		String s = new String(bout.toByteArray(), "ASCII");
		// System.out.println("OSDXKeyServerClient | "+s);
		return s;
	}
	
	public String getHeaderValue(String headerName) {
		return headers.get(headerName);
	}
	public String getParamValue(String paramName) {
		return parameters.get(paramName);
	}
}


