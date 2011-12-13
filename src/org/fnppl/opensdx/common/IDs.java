package org.fnppl.opensdx.common;

import java.util.*;

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
	private BusinessStringItem contentauth;			//COULD
	private BusinessStringItem labelordernum;		//COULD
	private BusinessStringItem amzn;				//COULD
	private BusinessStringItem isbn;				//COULD
	private BusinessStringItem finetunes;			//COULD
	private BusinessStringItem licensor;			//COULD
	private BusinessStringItem licensee;			//COULD
	private BusinessStringItem gvl;					//COULD
	private BusinessStringItem amg;					//COULD


	public static IDs make() {
		IDs ids = new IDs();
		ids.grid = null;
		ids.upc = null;
		ids.isrc = null;
		ids.contentauth = null;
		ids.labelordernum = null;
		ids.amzn = null;
		ids.isbn = null;
		ids.finetunes = null;
		ids.licensor = null;
		ids.licensee = null;
		ids.gvl = null;
		ids.amg = null;
		return ids;
	}

	public static IDs fromBusinessObject(BusinessObject bo) {
		if (bo==null) return null;
		if (!bo.getKeyname().equals(KEY_NAME)) {
			bo = bo.handleBusinessObject(KEY_NAME);
		}
		if (bo==null) return null;
		IDs ids = new IDs();
		ids.initFromBusinessObject(bo);
		
		ids.grid = BusinessStringItem.fromBusinessObject(bo, "grid");
		ids.upc = BusinessStringItem.fromBusinessObject(bo, "upc");
		ids.isrc = BusinessStringItem.fromBusinessObject(bo, "isrc");
		ids.contentauth = BusinessStringItem.fromBusinessObject(bo, "contentauth");
		ids.labelordernum = BusinessStringItem.fromBusinessObject(bo, "labelordernum");
		ids.amzn = BusinessStringItem.fromBusinessObject(bo, "amzn");
		ids.isbn = BusinessStringItem.fromBusinessObject(bo, "isbn");
		ids.finetunes = BusinessStringItem.fromBusinessObject(bo, "finetunes");
		ids.licensor = BusinessStringItem.fromBusinessObject(bo, "licensor");
		ids.licensee = BusinessStringItem.fromBusinessObject(bo, "licensee");
		ids.gvl = BusinessStringItem.fromBusinessObject(bo, "gvl");
		ids.amg = BusinessStringItem.fromBusinessObject(bo, "amg");
		
		
		return ids;
	}

	public IDs grid(String grid) {
		if (grid==null) this.grid=null;
		else this.grid = new BusinessStringItem("grid", grid);
		return this;
	}

	public IDs upc(String upc) {
		if (upc==null) this.upc=null;
		else this.upc = new BusinessStringItem("upc", upc);
		return this;
	}

	public IDs isrc(String isrc) {
		if (isrc==null) this.isrc=null;
		else this.isrc = new BusinessStringItem("isrc", isrc);
		return this;
	}

	public IDs contentauth(String contentauth) {
		if (contentauth==null) this.contentauth=null;
		else this.contentauth = new BusinessStringItem("contentauth", contentauth);
		return this;
	}

	public IDs labelordernum(String labelordernum) { 
		if (labelordernum==null) this.labelordernum=null;
		else this.labelordernum = new BusinessStringItem("labelordernum", labelordernum);
		return this;
	}

	public IDs amzn(String amzn) {
		if (amzn==null) this.amzn=null;
		else this.amzn = new BusinessStringItem("amzn", amzn);
		return this;
	}

	public IDs isbn(String isbn) {
		if (isbn==null) this.isbn=null;
		else this.isbn = new BusinessStringItem("isbn", isbn);
		return this;
	}

	public IDs finetunes(String finetunes) {
		if (finetunes==null) this.finetunes=null;
		else this.finetunes = new BusinessStringItem("finetunes", finetunes);
		return this;
	}

	public IDs licensor(String licensor) {
		if (licensor==null) this.licensor=null;
		else this.licensor = new BusinessStringItem("licensor", licensor);
		return this;
	}

	public IDs licensee(String licensee) {
		if (licensee==null) this.licensee=null;
		else this.licensee = new BusinessStringItem("licensee", licensee);
		return this;
	}

	public IDs gvl(String gvl) {
		if (gvl==null) this.gvl=null;
		else this.gvl = new BusinessStringItem("gvl", gvl);
		return this;
	}

	public IDs amg(String amg) {
		if (amg==null) this.amg=null;
		else this.amg = new BusinessStringItem("amg", amg);
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

	public String getContentauth() {
		if (contentauth==null) return null;
		return contentauth.getString();
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

	public String getFinetunes() {
		if (finetunes==null) return null;
		return finetunes.getString();
	}

	public String getLicensor() {
		if (licensor==null) return null;
		return licensor.getString();
	}

	public String getLicensee() {
		if (licensee==null) return null;
		return licensee.getString();
	}
	
	public String getGvl() {
		if (gvl==null) return null;
		return gvl.getString();
	}
	
	public String getAmg() {
		if (amg==null) return null;
		return amg.getString();
	}
	
	public String getKeyname() {
		return KEY_NAME;
	}
	
	public static HashSet<String> getRelevantIDs(String type) {
		//System.out.println("getRelevantIDs for type: "+type);
		
		HashSet<String> relevant = new HashSet<String>();
		if (type.equals(Contributor.TYPE_LABEL)) {
			relevant.add("gvl");
			relevant.add("licensee");
			relevant.add("licensor");
			relevant.add("finetunes");
			relevant.add("contentauth");
			return relevant;
		}
		
		if (    type.equals(Contributor.TYPE_COPYRIGHT)
			||  type.equals(Contributor.TYPE_PRODUCER)
			||  type.equals(Contributor.TYPE_CLEARINGHOUSE)
		   ) {
			relevant.add("contentauth");
			return relevant;
		}
		
		if (    type.equals(Contributor.TYPE_PERFORMER)
		    ||  type.equals(Contributor.TYPE_TEXTER)
		    ||  type.equals(Contributor.TYPE_EDITOR)
		    ||  type.equals(Contributor.TYPE_CONDUCTOR)
		    ||  type.equals(Contributor.TYPE_ORCHESTRA)
		    ||  type.equals(Contributor.TYPE_DISPLAY_ARTIST)
		    ||  type.equals(Contributor.TYPE_SINGER)
		    ||  type.equals(Contributor.TYPE_COMPOSER)
		    ||  type.equals(Contributor.TYPE_MIXER)
		    ||  type.equals(Contributor.TYPE_REMIXER)
		    ||  type.equals(Contributor.TYPE_PRODUCER)
		    ||  type.equals(Contributor.TYPE_FEATURING)
		    ||  type.equals(Contributor.TYPE_WITH)
		    ||  type.equals(Contributor.TYPE_DJ)
		    ||  type.equals(Contributor.TYPE_VERSUS)
		    ||  type.equals(Contributor.TYPE_MEETS)
		    ||  type.equals(Contributor.TYPE_PRESENTS)
		    ||  type.equals(Contributor.TYPE_COMPILATOR)
		    ||  type.equals(Contributor.TYPE_PUBLISHER)
		    ||  type.equals(Contributor.TYPE_AUTHOR)
		    ||  type.equals(Contributor.TYPE_ARRANGER)
		   ) {
			relevant.add("licensor");
			relevant.add("licensee");
			relevant.add("finetunes");
			relevant.add("contentauth");
			relevant.add("gvl");
			return relevant;
		}
		
		if (type.equalsIgnoreCase("bundle")) {
			relevant.add("grid");
			relevant.add("upc");
			relevant.add("contentauth");
			relevant.add("labelordernum");
			relevant.add("amzn");
			relevant.add("isbn");
			relevant.add("finetunes");
			relevant.add("licensor");
			relevant.add("licensee");
			relevant.add("amg");
			return relevant;
		}
		
		if (type.equalsIgnoreCase("bundleditem")) {
			relevant.add("grid");
			relevant.add("isrc");
			relevant.add("contentauth");
			relevant.add("isbn");
			relevant.add("finetunes");
			relevant.add("licensor");
			relevant.add("licensee");
			relevant.add("amg");
			return relevant;
		}
		
		return relevant;
	}
}
