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

public class IDs extends BusinessObject {

	public static String KEY_NAME = "ids";

	private BusinessStringItem grid;				//COULD
	private BusinessStringItem upc;					//COULD
	private BusinessStringItem isrc;				//COULD
	private BusinessStringItem contentauthid;		//COULD
	private BusinessStringItem labelordernum;		//COULD
	private BusinessStringItem amzn;				//COULD
	private BusinessStringItem isbn;				//COULD
	private BusinessStringItem finetunesid;			//COULD
	private BusinessStringItem ourid;				//COULD
	private BusinessStringItem yourid;				//COULD


	public static IDs make() {
		IDs ids = new IDs();
		ids.grid = null;
		ids.upc = null;
		ids.isrc = null;
		ids.contentauthid = null;
		ids.labelordernum = null;
		ids.amzn = null;
		ids.isbn = null;
		ids.finetunesid = null;
		ids.ourid = null;
		ids.yourid = null;
		return ids;
	}

	public static IDs fromBusinessObject(BusinessObject bo) {
		if (bo==null) return null;
		if (!bo.getKeyname().equals(KEY_NAME)) {
			bo = BusinessObject.fromElement(bo.handleElement(KEY_NAME));
		}
		if (bo==null) return null;
		IDs ids = new IDs();
		ids.initFromBusinessObject(bo);
		
		ids.grid = BusinessStringItem.fromBusinessObject(bo, "grid");
		ids.upc = BusinessStringItem.fromBusinessObject(bo, "upc");
		ids.isrc = BusinessStringItem.fromBusinessObject(bo, "isrc");
		ids.contentauthid = BusinessStringItem.fromBusinessObject(bo, "contentauthid");
		ids.labelordernum = BusinessStringItem.fromBusinessObject(bo, "labelordernum");
		ids.amzn = BusinessStringItem.fromBusinessObject(bo, "amzn");
		ids.isbn = BusinessStringItem.fromBusinessObject(bo, "isbn");
		ids.finetunesid = BusinessStringItem.fromBusinessObject(bo, "finetunesid");
		ids.ourid = BusinessStringItem.fromBusinessObject(bo, "ourid");
		ids.yourid = BusinessStringItem.fromBusinessObject(bo, "yourid");
		
		return ids;
	}

	public IDs grid(String grid) {
		this.grid = new BusinessStringItem("grid", grid);
		return this;
	}

	public IDs upc(String upc) {
		this.upc = new BusinessStringItem("upc", upc);
		return this;
	}

	public IDs isrc(String isrc) {
		this.isrc = new BusinessStringItem("isrc", isrc);
		return this;
	}

	public IDs contentauthid(String contentauthid) {
		this.contentauthid = new BusinessStringItem("contentauthid", contentauthid);
		return this;
	}

	public IDs labelordernum(String labelordernum) {
		this.labelordernum = new BusinessStringItem("labelordernum", labelordernum);
		return this;
	}

	public IDs amzn(String amzn) {
		this.amzn = new BusinessStringItem("amzn", amzn);
		return this;
	}

	public IDs isbn(String isbn) {
		this.isbn = new BusinessStringItem("isbn", isbn);
		return this;
	}

	public IDs finetunesid(String finetunesid) {
		this.finetunesid = new BusinessStringItem("finetunesid", finetunesid);
		return this;
	}

	public IDs ourid(String ourid) {
		this.ourid = new BusinessStringItem("ourid", ourid);
		return this;
	}

	public IDs yourid(String yourid) {
		this.yourid = new BusinessStringItem("yourid", yourid);
		return this;
	}




	public String getGrid() {
		if (grid==null) return null;
		return grid.getString();
	}

	public String getUpc() {
		if (upc==null) return null;
		return upc.getString();
	}

	public String getIsrc() {
		if (isrc==null) return null;
		return isrc.getString();
	}

	public String getContentauthid() {
		if (contentauthid==null) return null;
		return contentauthid.getString();
	}

	public String getLabelordernum() {
		if (labelordernum==null) return null;
		return labelordernum.getString();
	}

	public String getAmzn() {
		if (amzn==null) return null;
		return amzn.getString();
	}

	public String getIsbn() {
		if (isbn==null) return null;
		return isbn.getString();
	}

	public String getFinetunesid() {
		if (finetunesid==null) return null;
		return finetunesid.getString();
	}

	public String getOurid() {
		if (ourid==null) return null;
		return ourid.getString();
	}

	public String getYourid() {
		if (yourid==null) return null;
		return yourid.getString();
	}
	public String getKeyname() {
		return KEY_NAME;
	}
}
