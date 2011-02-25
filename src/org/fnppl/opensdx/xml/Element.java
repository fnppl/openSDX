package org.fnppl.opensdx.xml;

/*
 * Copyright (C) 2010-2011 
 * 							fine people e.V. <opensdx@fnppl.org> 
 * 							Henning Thieß <ht@fnppl.org>
 * 
 * 							http://fnppl.org
 * 
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
 * basically this class mimics the org.jdom-stuff.
 * why is it here? because we want to be independent of jdoms-implementation!
 * 
 */

import java.util.*;

public class Element {
	private org.jdom.Element base = null;
	
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
		return new Element(base.getChild("name"));
	}
	public String getChildText(String name) {
		//beware double-invoke!!!
		return base.getChildText("name");
	}
	public int getChildInt(String name) {
		//beware double-invoke!!!
		return Integer.parseInt(base.getChildText("name"));
	}
	public String getText() {
		//beware double-invoke!!!
		return base.getText();
	}
	public void addContent(String name, String value) {
		base.addContent((new org.jdom.Element(name)).setText(value));
	}
	public void addContent(Element e) {
		base.addContent(e.base);
	}
	
	public Vector<Element> getChildren() {
		Vector<Element> ret = new Vector<Element>();
		List l = base.getChildren();
		for(int i=0;i<l.size();i++) {
			ret.addElement((Element)l.get(i));
		}
		return ret;
	}
	public Vector<Element> getChildren(String name) {
		Vector<Element> ret = new Vector<Element>();
		List l = base.getChildren(name);
		for(int i=0;i<l.size();i++) {
			ret.addElement((Element)l.get(i));
		}
		return ret;
	}
}
