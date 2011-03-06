package org.fnppl.opensdx.xml;


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

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class Element {
	protected org.jdom.Element base = null;
	
	public Element(String name) {
		base = new org.jdom.Element(name);
	}
	protected Element(org.jdom.Element e) {
		base = e;
	}	
	
	public Element setAttribute(String name, String value) {
		base.setAttribute(name, value);
		return this;
	}
	public String getName() {
		return base.getName();
	}
	public Element getChild(String name) {
		//beware double-invoke!!!
		org.jdom.Element b = base.getChild(name);
		if (b==null) return null;
		return new Element(b);
	}
	public String getChildText(String name) {
		//beware double-invoke!!!
		String s = base.getChildText(name);
		if (s==null) return ""; //TODO ist das sinn der sache??
		return s;
	}
	public int getChildInt(String name) {
		//beware double-invoke!!!
		return Integer.parseInt(base.getChildText(name));
	}
	public String getText() {
		//beware double-invoke!!!
		return base.getText();
	}
	public void addContent(String name, String value) {
//		if(value == null) {
//			return;
//		}
		base.addContent((new org.jdom.Element(name)).setText(value));
	}
	public void addContent(Element e) {
		base.addContent(e.base);
	}
	
	public Vector<Element> getChildren() {
		Vector<Element> ret = new Vector<Element>();
		List l = base.getChildren();
		for(int i=0;i<l.size();i++) {
			ret.add(new Element((org.jdom.Element)l.get(i)));
		}
		return ret;
	}
	public Vector<Element> getChildren(String name) {
		Vector<Element> ret = new Vector<Element>();
		List l = base.getChildren(name);
		for(int i=0;i<l.size();i++) {
			ret.add(new Element((org.jdom.Element)l.get(i)));
		}
		
		return ret;
	}
	
	public void output(OutputStream out) {
		try {
			XMLOutputter outp= new XMLOutputter(Format.getPrettyFormat());
			outp.output(base, out);       
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
