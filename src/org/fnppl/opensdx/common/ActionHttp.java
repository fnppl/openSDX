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
	
	public static String TYPE_GET = "GET";
	public static String TYPE_POST = "POST";
	public static String TYPE_HEAD = "HEAD";
	
	
	private BusinessStringItem url;
	private BusinessStringItem type;
	private BusinessCollection<NameValuePair> header;
	private BusinessCollection<NameValuePair> params;
	
	private ActionHttp() {
		
	}
	
	public static ActionHttp make(String url, String type) {
		ActionHttp a = new ActionHttp();
		a.url = new BusinessStringItem("url", url);
		a.type = new BusinessStringItem("type", type);
		a.header = new BusinessCollection<NameValuePair>() {
			public String getKeyname() {
				return KEY_NAME_HEADER_COLLECTION;
			}
		};
		a.params = new BusinessCollection<NameValuePair>() {
			public String getKeyname() {
				return KEY_NAME_PARAM_COLLECTION;
			}
		};
		return a;
	}

	public static ActionHttp fromBusinessObject(BusinessObject bo) {
		if (bo==null) return null;
		if (!bo.getKeyname().equals(KEY_NAME)) {
			bo = bo.handleBusinessObject(KEY_NAME);
		}
		if (bo==null) return null;
		
		final ActionHttp a = new ActionHttp();
		a.initFromBusinessObject(bo);
		
		a.url = a.handleBusinessStringItem("url");
		a.type = a.handleBusinessStringItem("type");
		
		a.header = new BusinessCollection<NameValuePair>() {
			public String getKeyname() {
				return KEY_NAME_HEADER_COLLECTION;
			}
		};
		new ChildElementIterator(bo, KEY_NAME_HEADER_COLLECTION, KEY_NAME_HEADER) {
			public void processBusinessObject(BusinessObject bo) {
				a.addHeader(
					bo.getBusinessStringItem("name").getString(),
					bo.getBusinessStringItem("value").getString()
				);
			}
		};
		a.params = new BusinessCollection<NameValuePair>() {
			public String getKeyname() {
				return KEY_NAME_PARAM_COLLECTION;
			}
		};
		new ChildElementIterator(bo, KEY_NAME_PARAM_COLLECTION, KEY_NAME_PARAM) {
			public void processBusinessObject(BusinessObject bo) {
				a.addParam(
					bo.getBusinessStringItem("name").getString(),
					bo.getBusinessStringItem("value").getString()
				);
			}
		};
		a.removeOtherObjects();
		return a;
	}
	
	public String getDescription() {
		if (url==null) return "";
		return url.getString();
	}
	public ActionHttp url(String url) {
		if (url==null) {
			this.url = null;
		} else {
			this.url.setString(url);
		}
		return this;
	}
	
	public ActionHttp type(String type) {
		if (type==null) {
			this.type = null;
		} else {
			this.type.setString(type);
		}
		return this;
	}
	
	public String getUrl() {
		if (url==null) return null;
		return url.getString();
	}
	
	public String getType() {
		if (type==null) return null;
		return type.getString();
	}
	
	public ActionHttp addHeader(String name, String value) {
		NameValuePair h = NameValuePair.make(name,value,KEY_NAME_HEADER);		
		header.add(h);
		return this;
	}
	
	public ActionHttp addParam(String name, String value) {
		NameValuePair p = NameValuePair.make(name,value,KEY_NAME_PARAM);
		params.add(p);
		return this;
	}
	
	public int getHeaderCount() {
		if (header==null) return 0;
		return header.size();
	}
	
	public int getParamCount() {
		if (params==null) return 0;
		return params.size();
	}
	
	public String getHeaderName(int index) {
		if (header==null || index<0 || index>=getHeaderCount()) return null;
		return header.get(index).getName();
	}
	
	public void removeHeader(int index) {
		if (header==null || index<0 || index>=getHeaderCount()) return;
		header.remove(index);
	}
	
	public void removeParam(int index) {
		if (params==null || index<0 || index>=getHeaderCount()) return;
		params.remove(index);
	}
	public String getHeaderValue(int index) {
		if (header==null || index<0 || index>=getHeaderCount()) return null;
		return header.get(index).getValue();
	}
	
	public String getParamName(int index) {
		if (params==null || index<0 || index>=getParamCount()) return null;
		return params.get(index).getName();
	}
	
	public String getParamValue(int index) {
		if (params==null || index<0 || index>=getParamCount()) return null;
		return params.get(index).getValue();
	}
	
	
	public void execute() {
		//TODO implement
	}

	
	public String getKeyname() {
		return KEY_NAME;
	}
	
	
	

	
}
