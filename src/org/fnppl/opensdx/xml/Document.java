package org.fnppl.opensdx.xml;


/*
 * Copyright (C) 2010-2013 
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
import java.net.URL;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;

public class Document {
	private org.jdom.Document base = null;
	private Element rt = null;
	
	private Document() {
		
	}
	private Document(org.jdom.Document dc) {
		this.base = dc;
		rt = new Element(base.getRootElement());
		
	}
	
	public static Document fromFile(File f) throws Exception {
//		XMLHelper.fromFile(f);
		
		SAXBuilder sax = new SAXBuilder();		
		
		Document ret = new Document(sax.build(f));
		
		return ret;
	}
	
	public static Document fromURL(URL url) throws Exception {		
		SAXBuilder sax = new SAXBuilder();
		Document ret = new Document(sax.build(url));
		return ret;
	}
	
	public static Document fromStream(InputStream in) throws Exception {
		SAXBuilder sax = new SAXBuilder();
		Document ret = new Document(sax.build(in));
		return ret;
	}
	
	public static Document fromString(String xml) throws Exception {
		SAXBuilder sax = new SAXBuilder();
		//System.out.println("|"+xml);
		Document ret = new Document(sax.build(new StringReader(xml)));
		return ret;
	}
	
	public Element getRootElement() {		
		return rt;
	}
	
	public static Document buildDocument(Element root) {
		Document d = new Document();
		d.base = new org.jdom.Document((org.jdom.Element)root.base.detach());
		d.rt = root;
		return d;
	}
	
	public void writeToFile(File file) throws Exception {
		FileOutputStream out = new FileOutputStream(file);
		output(out);
		out.flush();
		out.close();
	}
	
//	public void output(OutputStream out) throws Exception {
//		String head = "<?xml version= \"1.0\" encoding=\"UTF-8\"?>\n";
//		out.write(head.getBytes("UTF-8"));
//		rt.output(out);
//	}
	public void output(OutputStream out) {
		try {
			Format f = Format.getPrettyFormat();
			f.setEncoding("UTF-8");
			XMLOutputter outp = new XMLOutputter(f);
			outp.output(base, out);      
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void outputCompact(OutputStream out) {
		try {
			Format f = Format.getCompactFormat();
			f.setEncoding("UTF-8");
			XMLOutputter outp = new XMLOutputter(f);
			outp.output(base, out);      
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public String toString() {
		try {
			Format f = Format.getPrettyFormat();
			f.setEncoding("UTF-8");
			XMLOutputter outp = new XMLOutputter(f);
			StringWriter sw = new StringWriter();
			outp.output(base, sw);      
			sw.flush();
			
			return sw.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public byte[] toByteArray() {
		try {
			Format f = Format.getPrettyFormat();
			f.setEncoding("UTF-8");
			XMLOutputter outp = new XMLOutputter(f);
			return outp.outputString(base).getBytes("UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String toStringCompact() {
		try {
			Format f = Format.getCompactFormat();
			f.setEncoding("UTF-8");
			XMLOutputter outp = new XMLOutputter(f);
			StringWriter sw = new StringWriter();
			outp.output(base, sw);      
			sw.flush();
			
			return sw.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return null;
	}
	
	public static org.jdom.Document buildJDOMDocument(Element openSDXRoot) {
		return buildJDOMDocument(openSDXRoot, "openSDX_00-00-00-01.xsd");
	}
	public static org.jdom.Document buildJDOMDocument(Element openSDXRoot, String url) {
		org.jdom.Element root = (org.jdom.Element)openSDXRoot.base.detach();
		Namespace ns = Namespace.getNamespace("xsi","http://www.w3.org/2001/XMLSchema-instance");
		root.setAttribute("noNamespaceSchemaLocation", url, ns);
		Document d = new Document();
		d.base = new org.jdom.Document(root);
		return d.base;
	}
}
