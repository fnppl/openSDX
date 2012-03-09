package org.fnppl.opensdx.common;

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

import java.util.HashMap;
import java.util.Vector;
import java.util.Map.Entry;

import javax.smartcardio.ATR;

import org.fnppl.opensdx.xml.Element;
import org.fnppl.opensdx.xml.XMLElementable;

/**
 * 
 * @author Bertram Boedeker <bboedeker@gmx.de>
 * 
 */
public class BusinessItem implements XMLElementable {
	private String name;
	private Object value;
	private HashMap<String,String> attribs;
	private Class type;
	private boolean hasChanged;
	
	public BusinessItem(String name, Object value) {
		if (value == null) throw new RuntimeException("emtpy values are not allowed");
		this.name = name;
		this.value = value;
		type = value.getClass();
		hasChanged = false;
		attribs = null;
	}
	
	public BusinessItem(String name, Class type) {
		this.name = name;
		value = null;
		this.type = type;
		hasChanged = false;
		attribs = null;
	}
	
	
	public void set(Object value) {
		if ((this.value==null && value!=null) || value ==null || this.value.equals(value)) {
			if (value!=null && !type.isInstance(value)) {
				throw new RuntimeException("wrong type of value");
			}
			this.value = value;
			hasChanged = true;
		} else {
			if (value!=null && value!=null && !type.isInstance(value)) {
				throw new RuntimeException("wrong type of value");
			} else {
				this.value = value;
			}
		}
	}
	
	public String getAttribute(String key) {
		if (attribs==null) return null;
		return attribs.get(key);
	}
	
	public Vector<String[]> getAttributes() {
		if (attribs==null) return null;
		Vector<String[]> a = new Vector<String[]>();
		for (String key : attribs.keySet()) {
			a.add(new String[] {key,attribs.get(key)});
		}
		return a;
	}
	
	public void addAttributes(Element item) {
		Vector<String[]> attribs = item.getAttributes();
		addAttributes(attribs);
	}
	
	public void addAttributes(Vector<String[]> attribs) {
		if (attribs!=null) {
			for (String[] a : attribs) {
				this.setAttribute(a[0], a[1]);
			}
		}
	}
	
	public void setAttribute(String key, String value) {
		if (attribs==null) attribs = new HashMap<String, String>();
		attribs.put(key, value);
	}
	
	public Object get() {
		return value;
	}
	
	public String getKeyname() {
		return name;
	}
	
	public Element toElement() {
		if (value ==null) return null;
		Element e = new Element(name, value.toString());
		if (attribs!=null) {
			Vector<String[]> a = new Vector<String[]>();
			for (String key : attribs.keySet()) {
				e.setAttribute(key, attribs.get(key));
			}
//			for (Entry<String,String> a : attribs.entrySet()) {
//				e.setAttribute(a.getKey(), a.getValue());
//			}
		}
		return e;
	}
	
	public void setChanged(boolean changed) { 
		this.hasChanged = changed;
	}
	
	public boolean hasChanged() {
		return hasChanged;
	}
	
}
