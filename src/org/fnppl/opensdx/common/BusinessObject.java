package org.fnppl.opensdx.common;

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

import java.lang.reflect.Field;
import java.util.Vector;

import org.fnppl.opensdx.xml.Element;
import org.fnppl.opensdx.xml.XMLElementable;
import org.fnppl.opensdx.xml.XMLHelper;


/**
 * 
 * @author Bertram Boedeker <bboedeker@gmx.de>
 * 
 */
public abstract class BusinessObject implements XMLElementable {
	
	private Vector<Element> unhandled_elements  = new Vector<Element>(); 
	
	public abstract String getKeyname();
	
	
	public Element toElement() {
		return toElement(true);
	}
	/***
	 * cool stuff happens here:: this method uses javas reflexion for accessing all XMLElementable fields
	 * coolest stuff:: even private fields can be read out by this!!!
	 */
	public Element toElement(boolean appendUnhandledElements) {
		Element resultElement = new Element(getKeyname());
		
		Field[] fields = this.getClass().getDeclaredFields();
		for (Field f : fields) {
			try {
				f.setAccessible(true);
				Object thisFieldsObject = f.get(this);
				if (thisFieldsObject instanceof XMLElementable) {
					Element e = ((XMLElementable)thisFieldsObject).toElement();
					if (e!=null) {
						resultElement.addContent(e);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		if (appendUnhandledElements) {
			for (Element ue : unhandled_elements) {
				System.out.println("appending unhandled_element:: "+getKeyname()+"::"+ue.getName());
				resultElement.addContent(XMLHelper.cloneElement(ue));
			}
		} else {
			for (Element ue : unhandled_elements) {
				System.out.println("unhandled_element:: "+getKeyname()+"::"+ue.getName());
			}
		}
		return resultElement;
	}
	
	
	
	//for unknown BusinessObject
	public static BusinessObject fromElement(Element e) {
		if (e==null) return null;
		final String keyname = e.getName();
		BusinessObject b = new BusinessObject() {
			public String getKeyname() {				
				return keyname;
			}
		};
		b.unhandled_elements = e.getChildren();
		return b;
	}
	
	public void initFromBusinessObject(BusinessObject bo) {
		this.unhandled_elements = bo.unhandled_elements;
	}
		
	//to get unknown field or extensions
	public void readElements(Element e) {
		if (e==null) return;
		unhandled_elements = e.getChildren();
	}
	
	public Element handleElement(String name) {
		for (Element e :unhandled_elements) {
			if (e.getName().equals(name)) {
				unhandled_elements.remove(e);
				return e;
			}
		}
		return null;
	}
	
	public Vector<Element> handleElements(String name) {
		Vector<Element> elements = new Vector<Element>();
		for (Element e :unhandled_elements) {
			if (e.getName().equals(name)) {
				elements.add(e);
			}
		}
		unhandled_elements.removeAll(elements);
		return elements;
	}
	
	public void removeUnhandledElement(Element e) {
		unhandled_elements.remove(e);
	}
	
	public void removeAllUnhandledElements() {
		unhandled_elements.removeAllElements();
	}
	
	public void addUnhandledElement(Element e) {
		unhandled_elements.add(e);
	}
	
}
