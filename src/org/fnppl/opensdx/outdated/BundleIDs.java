package org.fnppl.opensdx.outdated;

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

import org.fnppl.opensdx.outdated.BaseObjectWithConstraints;
import org.fnppl.opensdx.xml.Element;

public class BundleIDs extends BaseObjectWithConstraints {

	public BundleIDs() {
		names.add("grid"); values.add(null); constraints.add("COULD");
		names.add("upc"); values.add(null); constraints.add("SHOULD");
		names.add("isrc"); values.add(null); constraints.add("SHOULD");
		names.add("contentauthid"); values.add(null); constraints.add("SHOULD");
		names.add("labelordernum"); values.add(null); constraints.add("COULD");
		names.add("amzn"); values.add(null); constraints.add("COULD");
		names.add("isbn"); values.add(null); constraints.add("COULD");
		names.add("finetunesid"); values.add(null); constraints.add("COULD");
		names.add("ourid"); values.add(null); constraints.add("COULD");
		names.add("yourid"); values.add(null); constraints.add("COULD");
	}

// methods
	public void setGrid(String grid) {
		set("grid", grid);
	}

	public String getGrid() {
		return get("grid");
	}

	public void setUpc(String upc) {
		set("upc", upc);
	}

	public String getUpc() {
		return get("upc");
	}

	public void setIsrc(String isrc) {
		set("isrc", isrc);
	}

	public String getIsrc() {
		return get("isrc");
	}

	public void setContentauthid(String contentauthid) {
		set("contentauthid", contentauthid);
	}

	public String getContentauthid() {
		return get("contentauthid");
	}

	public void setLabelordernum(String labelordernum) {
		set("labelordernum", labelordernum);
	}

	public String getLabelordernum() {
		return get("labelordernum");
	}

	public void setAmzn(String amzn) {
		set("amzn", amzn);
	}

	public String getAmzn() {
		return get("amzn");
	}

	public void setIsbn(String isbn) {
		set("isbn", isbn);
	}

	public String getIsbn() {
		return get("isbn");
	}

	public void setFinetunesid(String finetunesid) {
		set("finetunesid", finetunesid);
	}

	public String getFinetunesid() {
		return get("finetunesid");
	}

	public void setOurid(String ourid) {
		set("ourid", ourid);
	}

	public String getOurid() {
		return get("ourid");
	}

	public void setYourid(String yourid) {
		set("yourid", yourid);
	}

	public String getYourid() {
		return get("yourid");
	}


	public Element toElement() {
		return toElement("ids");
	}
}
