package org.fnppl.opensdx.outdated;

import java.util.Vector;

import org.fnppl.opensdx.xml.Element;

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
 * ISO2-country http://www.iso.org/iso/english_country_names_and_code_elements 
 * 
 * regions are made with *TerritoryConjunction*
 * 
 */

public class Territory extends BaseObject {

	
	public Territory() {
		names.add("territory"); values.add(null);
		names.add("allow"); values.add(null);
	}
	
	public static Territory allow(String territory) {
		Territory t = new Territory();
		t.setAllowTerritory(territory);
		return t;
	}
	public static Territory disallow(String territory) {
		Territory t = new Territory();
		t.setDisallowTerritory(territory);
		return t;
	}

// methods
	public void setAllowTerritory(String territory) {
		set("territory", territory);
		set("allow", "true");
	}
	
	public void setDisallowTerritory(String territory) {
		set("territory", territory);
		set("allow", "false");
	}

	public String getTerritory() {
		return get("territory");
	}
	
	public boolean getAllow() {
		return Boolean.parseBoolean(get("allow"));
	}
	
	public Element toElement() {
		return toElement("territory");
	}
	
	public Element toElement(String name) {
		Element e = new Element(name);
		if (!getAllow()) {
			e.setAttribute("type", "disallow");
		}
		e.setText(getTerritory());
		return e;
	}

}
