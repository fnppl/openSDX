package org.fnppl.opensdx.xml;


/*
 * Copyright (C) 2010-2015 
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


import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import org.jdom2.Attribute;
import org.jdom2.Comment;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class Element {
	protected org.jdom2.Element base = null;
	
	public static Element buildElement(org.jdom2.Element jdomElement) {
		return new Element(jdomElement);
	}
	public Element(String name) {
		base = new org.jdom2.Element(name);
	}
	
	public Element(String name, String value) {
		base = new org.jdom2.Element(name).setText(value);
		
	}
	protected Element(org.jdom2.Element e) {
		base = e;
	}	
	
	public Element setAttribute(String name, String value) {
		base.setAttribute(name, value);
		return this;
	}
	
	public void setText(String value) {
		base.setText(value);
	}
	
	public Element detach() {
		base.detach();
		return this;
	}
	
	public org.jdom2.Element getJDOMBaseElement() {
		return base;
	}
	
	public void addComment(String comment)  {
		base.addContent(new Comment(comment));
	}
	public void addCommentFirst(String comment)  {
		base.addContent(0, new Comment(comment));
	}
	public void addCommentAfter(String name, String comment)  {
		int ind = base.indexOf(base.getChild(name));
		base.addContent(ind+1, new Comment(comment));
	}
	
	public String getAttribute(String attName) {
		Attribute att = base.getAttribute(attName);
		if (att!=null) {
		
		}
		return base.getAttributeValue(attName);
	}
	
	public Vector<String[]> getAttributes() {
		Vector<String[]> atts = new Vector<String[]>();
		List<Attribute> l = (List<Attribute>)base.getAttributes();
		if (l!=null) {
			for (Attribute a : l) {
				atts.add(new String[]{a.getName(), a.getValue()});
			}
		}
		return atts;
	}
	
	
	public String getName() {
		return base.getName();
	}
	public Element getChild(String name) {
		//beware double-invoke!!!
		org.jdom2.Element b = base.getChild(name);
		if (b==null) return null;
		return new Element(b);
	}
	public String getChildTextNN(String name) {
		//beware double-invoke!!!
		String s = base.getChildText(name);
		if (s == null) {
			return "";
		}
		return s;
	}
	public String getChildText(String name) {
		String s = base.getChildText(name);		
		return s;
	}
	
	public int getChildInt(String name) {
		//beware double-invoke!!!
		try {
			return Integer.parseInt(base.getChildText(name));
		} catch (Exception ex) {
			return Integer.MIN_VALUE;
		}
	}
	public long getChildLong(String name) {
		//beware double-invoke!!!
		try {
			return Long.parseLong(base.getChildText(name));
		} catch (Exception ex) {
			return Long.MIN_VALUE;
		}
	}
	public boolean getChildBoolean(String name, boolean onError) {
		try {
			String s = base.getChildText(name);
			if (s==null) return onError;
			return Boolean.parseBoolean(s);
		} catch (Exception ex) {
			return onError;
		}
	}
	public String getText() {
		//beware double-invoke!!!
		return base.getText();
	}
	public void addContent(String name, String value) {
//		if(value == null) {
//			return;
//		}
		base.addContent((new org.jdom2.Element(name)).setText(value));
	}
	
	
	public void addContent(Element e) {
		base.addContent(e.base);
	}
	
	public Vector<Element> getChildren() {
		Vector<Element> ret = new Vector<Element>();
		List l = base.getChildren();
		for(int i=0;i<l.size();i++) {
			ret.add(new Element((org.jdom2.Element)l.get(i)));
		}
		return ret;
	}
	public Vector<Element> getChildren(String name) {
		Vector<Element> ret = new Vector<Element>();
		List l = base.getChildren(name);
		for(int i=0;i<l.size();i++) {
			ret.add(new Element((org.jdom2.Element)l.get(i)));
		}
		
		return ret;
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
}
