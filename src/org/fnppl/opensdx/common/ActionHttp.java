package org.fnppl.opensdx.common;

import java.util.Vector;

import org.fnppl.opensdx.xml.ChildElementIterator;
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


/**
 * 
 * @author Bertram Boedeker <bboedeker@gmx.de>
 * 
 */
public class ActionHttp extends BusinessObject implements Action {

	public static String KEY_NAME = "http";
	
	private static String KEY_NAME_HEADER_COLLECTION = "addheader";
	private static String KEY_NAME_HEADER = "header";
	private static String KEY_NAME_PARAM_COLLECTION = "addparams";
	private static String KEY_NAME_PARAM = "param";
	
	
	
	private BusinessStringItem url;
	private BusinessStringItem type;
	private BusinessCollection<BusinessCollection> header;
	private BusinessCollection<BusinessCollection> params;
	
	private ActionHttp() {
		
	}
	
	public static ActionHttp make(String url, String type) {
		ActionHttp a = new ActionHttp();
		a.url = new BusinessStringItem("url", url);
		a.type = new BusinessStringItem("type", type);
		a.header = new BusinessCollection<BusinessCollection>() {
			public String getKeyname() {
				return KEY_NAME_HEADER_COLLECTION;
			}
		};
		a.params = new BusinessCollection<BusinessCollection>() {
			public String getKeyname() {
				return KEY_NAME_PARAM_COLLECTION;
			}
		};
		return a;
	}

	public static ActionHttp fromElement(Element e) {
		if (e==null) return null;
		
		final ActionHttp a = new ActionHttp();
		a.readElements(e);
		a.url = BusinessStringItem.fromBusinessObject(a, "url");
		a.type = BusinessStringItem.fromBusinessObject(a, "type");
		
		a.header = new BusinessCollection<BusinessCollection>() {
			public String getKeyname() {
				return KEY_NAME_HEADER_COLLECTION;
			}
		};
		new ChildElementIterator(e, KEY_NAME_HEADER_COLLECTION, KEY_NAME_HEADER) {
			public void processChild(Element child) {
				a.addHeader(child.getChildText("name"), child.getChildText("value"));
			}
		};
		a.params = new BusinessCollection<BusinessCollection>() {
			public String getKeyname() {
				return KEY_NAME_PARAM_COLLECTION;
			}
		};
		new ChildElementIterator(e, KEY_NAME_PARAM_COLLECTION, KEY_NAME_PARAM) {
			public void processChild(Element child) {
				a.addParam(child.getChildText("name"), child.getChildText("value"));
			}
		};
		a.removeAllUnhandledElements();
		return a;
	}
	
	
	public ActionHttp addHeader(String name, String value) {
		BusinessCollection<BusinessStringItem> h = new BusinessCollection<BusinessStringItem>() {
			public String getKeyname() {
				return KEY_NAME_HEADER;
			}
		};
		h.add(new BusinessStringItem("name",name));
		h.add(new BusinessStringItem("value",value));
		header.add(h);
		return this;
	}
	
	public ActionHttp addParam(String name, String value) {
		BusinessCollection<BusinessStringItem> p = new BusinessCollection<BusinessStringItem>() {
			public String getKeyname() {
				return "param";
			}
		};
		p.add(new BusinessStringItem("name",name));
		p.add(new BusinessStringItem("value",value));
		params.add(p);
		return this;
	}
	
	public void execute() {
		//TODO implement
	}

	
	public String getKeyname() {
		return KEY_NAME;
	}
	
	
	

	
}
