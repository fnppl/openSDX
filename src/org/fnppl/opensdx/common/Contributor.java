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

/**
 * 
 * @author Bertram Boedeker <bboedeker@gmx.de>
 * 
 */
public class Contributor extends BusinessObject {

	public static String KEY_NAME = "contributor";

	public static String TYPE_LABEL = "label";
	public static String TYPE_COMPOSER = "composer";
	public static String TYPE_TEXTER = "texter";
	public static String TYPE_WRITER = "writer";
	public static String TYPE_VOCALS = "vocals";
	public static String TYPE_CONDUCTOR = "conductor";	
	public static String TYPE_DISPLAY_ARTIST = "display_artist";
	public static String TYPE_COMPILATOR = "compilator";
	
	
	private BusinessStringItem name;	//MUST
	private BusinessStringItem type;	//MUST
	private IDs ids;					//MUST
	private InfoWWW www;				//SHOULD


	public static Contributor make(String name, String type, IDs ids) {
		Contributor contributor = new Contributor();
		contributor.name = new BusinessStringItem("name", name);
		contributor.type = new BusinessStringItem("type", type);
		contributor.ids = ids;
		contributor.www = null;
		return contributor;
	}


	public static Contributor fromBusinessObject(BusinessObject bo) {
		if (bo==null) return null;
		if (!bo.getKeyname().equals(KEY_NAME)) {
			bo = bo.handleBusinessObject(KEY_NAME);
		}
		if (bo==null) return null;
		
		Contributor contributor = new Contributor();
		contributor.initFromBusinessObject(bo);
		
		contributor.name = BusinessStringItem.fromBusinessObject(bo, "name");
		contributor.type = BusinessStringItem.fromBusinessObject(bo, "type");
		contributor.ids = IDs.fromBusinessObject(bo);
		contributor.www = InfoWWW.fromBusinessObject(bo);
		
		return contributor;
	}


	public Contributor name(String name) {
		this.name = new BusinessStringItem("name", name);
		return this;
	}

	public Contributor type(String type) {
		this.type = new BusinessStringItem("type", type);
		return this;
	}

	public Contributor ids(IDs ids) {
		this.ids = ids;
		return this;
	}

	public Contributor www(InfoWWW www) {
		this.www = www;
		return this;
	}


	public String getName() {
		if (name==null) return null;
		return name.getString();
	}

	public String getType() {
		if (type==null) return null;
		return type.getString();
	}

	public IDs getIDs() {
		return ids;
	}

	public InfoWWW getWww() {
		return www;
	}
	
	public String getKeyname() {
		return KEY_NAME;
	}
}
