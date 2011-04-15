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



import java.util.Vector;
import org.fnppl.opensdx.common.BaseObjectWithConstraints;
import org.fnppl.opensdx.xml.Element;

public class ActionHttp extends Action {

	public ActionHttp(int actionType) {
		setActionType(actionType);
		names.add("url"); values.add(null); constraints.add("[no comment]");
		names.add("type"); values.add(null); constraints.add("?");
		names.add("header"); values.add(new Vector<String[]>()); constraints.add("?");
		names.add("params"); values.add(new Vector<String[]>()); constraints.add("[no comment]");
	}
	

	public boolean doAction() {
		// TODO 
		return false;
	}

// methods
	public void setUrl(String url) {
		set("url", url);
	}

	public String getUrl() {
		return get("url");
	}

	public void setType(String type) {
		set("type", type);
	}

	public String getType() {
		return get("type");
	}

	public void addHeader(String name, String value) {
		((Vector<String[]>)getObject("header")).add(new String[]{name,value});
	}

	public Vector<String[]> getHeader() {
		return (Vector<String[]>)getObject("header");
	}

	public void addParam(String name, String value) {
		((Vector<String[]>)getObject("params")).add(new String[]{name,value});
	}

	public Vector<String[]> getParams() {
		return (Vector<String[]>)getObject("params");
	}
	

	public Element toElement() {
		return toElement("http");
	}
	
	public Element toElement(String name) {
		Element e = new Element(name);
		add(e,"url");
		add(e,"type");
		
		Element e2 = new Element("addheader"); e.addContent(e2);
		for (String[] s : (Vector<String[]>)getObject("header")) {
			Element et = new Element("header");
			et.addContent("name",s[0]);
			et.addContent("value",s[1]);
			e2.addContent(et);
		}
		Element e3 = new Element("addparams"); e.addContent(e3);
		for (String[] s : (Vector<String[]>)getObject("params")) {
			Element et = new Element("param");
			et.addContent("name",s[0]);
			et.addContent("value",s[1]);	
			e3.addContent(et);
		}
		return e;
	}
	
}
