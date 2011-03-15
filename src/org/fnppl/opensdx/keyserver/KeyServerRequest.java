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

public class KeyServerRequest {
	public final static String XMLDOCPARAMNAME = "xmldocument";
	
	public Hashtable<String, String> headers = new Hashtable<String, String>();
	public Hashtable<String, String> parameters = new Hashtable<String, String>();
	public Document xml;
	
	public String cmd = null;
	public String method = null;
	public String ipv4 = null;
	
	public static KeyServerRequest fromInputStream(BufferedInputStream in, String ipv4) throws Exception {
		KeyServerRequest ret = new KeyServerRequest();
		ret.ipv4 = ipv4;
		
		String zeile = null;
		zeile = readLineASCII(in, 4096); //cmdline
		
		StringTokenizer st = new StringTokenizer(zeile, " ");
		ret.method = st.nextToken();
		ret.cmd = st.nextToken();
		String proto = st.nextToken();
		
		if(st.hasMoreTokens()) {
			throw new Exception("INVALID HTTP _ MORE TOKEN AS _ "+st.nextToken());
		}
		
		readHeader(in, ret);
		System.out.println("::header end::");
		
		if (ret.method.equals("POST") && ret.headers.get("Content-Type").equals("text/xml")) {
			readXMLPostContent(in, ret);
		}
		else if(ret.method.equals("POST") && ret.headers.get("Content-Type").equals("application/x-www-form-urlencoded")) {
//			Content-Type: application/x-www-form-urlencoded
			readPostParams(in, ret);
		}
		else if(ret.method.equals("GET")) {
			readGetParams(ret);
		}
		
//		if(ret.cmd.indexOf("?")>=0) {
//			readGetParams(in, ret, ret.cmd);
//		}

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
		
		String s = new String(bout.toByteArray(), "ASCII");
		s = URLDecoder.decode(s, "UTF-8"); //urlencoded-form-data
		
		System.out.println("KeyServerRequest::GOT THIS AS DOC: ::START::"+s+"::END::");
		
//		String last = s.substring(s.lastIndexOf(">"));
//		System.out.println("last bytes: "+SecurityHelper.HexDecoder.encode(last.getBytes("UTF-8"), ':',-1));
//		s = s.substring(0, s.lastIndexOf(">")+3);
//		String last2 = s.substring(s.lastIndexOf(">"));
//		System.out.println("last bytes: "+SecurityHelper.HexDecoder.encode(last2.getBytes("UTF-8"), ':',-1));
		
//		System.out.println("GOT THIS AS DOC: ::"+s+"::");
		re.xml = Document.fromStream(new ByteArrayInputStream(s.getBytes("UTF-8")));
	}
	
	private static void readPostParams(InputStream in, KeyServerRequest re) throws Exception {
		System.out.println("KeyServerRequest::reading POST params");
		
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
		
		String s = new String(bout.toByteArray(), "ASCII");//alles ascii
		StringTokenizer st = new StringTokenizer(s, "=");
		while(st.hasMoreTokens()) {
			String pn = URLDecoder.decode(st.nextToken(), "UTF-8");
			String pv = URLDecoder.decode(st.nextToken(), "UTF-8");
		
			if(pn.equalsIgnoreCase(XMLDOCPARAMNAME)) {
				re.xml = Document.fromStream(new ByteArrayInputStream(pv.getBytes("UTF-8")));
			}
			else {
				re.parameters.put(pn, pv);
			}
		}
	}
	private static void readGetParams(KeyServerRequest re) throws Exception {
		String me = null;
		if(re.cmd.indexOf("?")>0) {
			me = re.cmd.substring(re.cmd.indexOf("?")+1);
			re.cmd = re.cmd.substring(0, re.cmd.indexOf("?"));
		}
		
		StringTokenizer st = new StringTokenizer(me, "=");
		while(st.hasMoreTokens()) {
			String pn = URLDecoder.decode(st.nextToken(), "UTF-8");
			String pv = URLDecoder.decode(st.nextToken(), "UTF-8");
		
			if(pn.equalsIgnoreCase(XMLDOCPARAMNAME)) {
				re.xml = Document.fromStream(new ByteArrayInputStream(pv.getBytes("UTF-8")));
			}
			else {
				re.parameters.put(pn, pv);
			}
		}
	}
	
	private static void readHeader(BufferedInputStream in, KeyServerRequest re) throws Exception {
		String zeile = null;

		while ((zeile=readLineASCII(in, 4096)) != null) {
			if(zeile.length() == 0) {
				return;//heade-ende
			}
			//header
//			String[] p = parseHeader(zeile);
//			re.headers.put(p[0], p[1]);
//			StringTokenizer st = new StringTokenizer(zeile, " ");			
//			re.headers.put(st.nextToken(), st.nextToken());
			String n = zeile.substring(0, zeile.indexOf(": "));
			String v = zeile.substring(zeile.indexOf(": ")+2);
//			re.headers.put(URLDecoder.decode(n, "UTF-8"), URLDecoder.decode(v, "UTF_8"));
			re.headers.put(n, v);
			
			System.out.println("KeyServerRequest::readHeader::"+zeile);
		}
	}
	
//	private static String[] parseHeader(String zeile) {
//		StringTokenizer st = new StringTokenizer(zeile, " ");
//		String[] ret = new String[2];
//		ret[0] = st.nextToken();
//		ret[0] = ret[0].substring(0, ret[0].length()-1);//cut off ":"
//		
//		ret[1] = st.nextToken();
//		
//		return ret;
//	}

	private static String readLineASCII(BufferedInputStream in, int maxbytes) throws Exception {
		//if (in.available()<=0) return null;
		ByteArrayOutputStream bout = new ByteArrayOutputStream();

		//HEADERS are ASCII

		byte[] b = new byte[1];
		int r = 0;

		char last='\r';

		while((r=in.read(b)) > 0 && bout.size()<maxbytes) {
			char m = (char)b[0];
			if(m == '\n') {
				break;
			} else if(m != '\r') {
				bout.write(b[0]);
			}
		}

		if(r<0 && bout.size() == 0) {
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