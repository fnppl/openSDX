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


/*
 * a bundle combines one to many Items
 * 
 */

public class Bundle extends BaseObjectWithConstraints {

	public Bundle() {
		names.add("ids"); values.add(null); constraints.add("MUST");
		names.add("displayname"); values.add(null); constraints.add("MUST");
		names.add("name"); values.add(null); constraints.add("MUST");
		names.add("version"); values.add(null); constraints.add("MUST");
		names.add("display_artist"); values.add(null); constraints.add("SHOULD");
		names.add("contributors"); values.add(null); constraints.add("MUST");
		names.add("information"); values.add(null); constraints.add("[no comment]");
		names.add("license_basis"); values.add(null); constraints.add("?");
		names.add("license_specifics"); values.add(null); constraints.add("MUST");
		names.add("items"); values.add(null); constraints.add("[no comment]");
	}

//// methods
//	public void setIds(Ids ids) {
//		set("ids", ids);
//	}
//
//	public Ids getIds() {
//		return (Ids)getObject("ids");
//	}
//
//	public void setDisplayname(String displayname) {
//		set("displayname", displayname);
//	}
//
//	public String getDisplayname() {
//		return get("displayname");
//	}
//
//	public void setName(String name) {
//		set("name", name);
//	}
//
//	public String getName() {
//		return get("name");
//	}
//
//	public void setVersion(String version) {
//		set("version", version);
//	}
//
//	public String getVersion() {
//		return get("version");
//	}
//
//	public void setDisplay_artist(String display_artist) {
//		set("display_artist", display_artist);
//	}
//
//	public String getDisplay_artist() {
//		return get("display_artist");
//	}
//
//	public void setContributors(Contributors contributors) {
//		set("contributors", contributors);
//	}
//
//	public Contributors getContributors() {
//		return (Contributors)getObject("contributors");
//	}
//
//	public void setInformation(Information information) {
//		set("information", information);
//	}
//
//	public Information getInformation() {
//		return (Information)getObject("information");
//	}
//
//	public void setLicense_basis(License_basis license_basis) {
//		set("license_basis", license_basis);
//	}
//
//	public License_basis getLicense_basis() {
//		return (License_basis)getObject("license_basis");
//	}
//
//	public void setLicense_specifics(License_specifics license_specifics) {
//		set("license_specifics", license_specifics);
//	}
//
//	public License_specifics getLicense_specifics() {
//		return (License_specifics)getObject("license_specifics");
//	}
//
//	public void setItems(Items items) {
//		set("items", items);
//	}
//
//	public Items getItems() {
//		return (Items)getObject("items");
//	}
}