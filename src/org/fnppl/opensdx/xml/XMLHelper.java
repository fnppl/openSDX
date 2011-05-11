package org.fnppl.opensdx.xml;

import java.util.Vector;

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

/*
 * basically this class mimics the org.jdom-stuff.
 * why is it here? because we want to be independent of jdoms-implementation!
 * 
 * this class should provide nice stuff for parsing/writing xml
 * + DOM-mode
 * + SAX-mode
 * 
 * most probably, we will shift this in specific classes then...
 * 
 */

public class XMLHelper {

	
	public static Element cloneElement(Element e) {
		Element c = new Element(e.getName());
		rekursiveAddContent(e, c);
		return c;
	}
	
	private static void rekursiveAddContent(Element from, Element to) {
		Vector<Element> ve = from.getChildren();
		if (ve.size()>0) {
			for (Element el : ve) {
				if (el.getChildren()==null || el.getChildren().size()==0) {
					rekursiveAddContent(el, to);
				} else {
					//System.out.println("adding element: "+el.getName());
					Element sub = new Element(el.getName());
					to.addContent(sub);
					rekursiveAddContent(el, sub);
				}
			}
		} else {
			String n = from.getName();
			String t = from.getText();
			//System.out.println("adding value: "+n+" : "+t);
			to.addContent(n,t);
			Vector<String[]> atts = from.getAttributes();
			if (atts!=null) {
				for (String[] a : atts) {
					to.setAttribute(a[0], a[1]);
				}
			}
		}
	}
}
