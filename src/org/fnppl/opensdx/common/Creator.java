package org.fnppl.opensdx.common;
/*
 * Copyright (C) 2010-2013 
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

public class Creator extends BusinessObject {

	public static String KEY_NAME = "creator";

	private BusinessStringItem email;				//COULD
	private BusinessStringItem userid;				//COULD
	private BusinessStringItem keyid;				//COULD


	public static Creator make(String email, String userid, String keyid) {
		Creator creator = make();
		if (email!=null) creator.email = new BusinessStringItem("email", email);
		if (userid!=null) creator.userid = new BusinessStringItem("userid", userid);
		if (keyid!=null) creator.keyid = new BusinessStringItem("keyid", keyid);
		return creator;
	}




	public static Creator make() {
		Creator creator = new Creator();
		creator.email = null;
		creator.userid = null;
		creator.keyid = null;
		return creator;
	}


	public static Creator fromBusinessObject(BusinessObject bo) {
		if (bo==null) return null;
		if (!bo.getKeyname().equals(KEY_NAME)) {
			bo = bo.handleBusinessObject(KEY_NAME);
		}
		if (bo==null) return null;
		Creator creator = new Creator();
		creator.initFromBusinessObject(bo);
		
		creator.email = BusinessStringItem.fromBusinessObject(creator, "email");
		creator.userid = BusinessStringItem.fromBusinessObject(creator, "userid");
		creator.keyid = BusinessStringItem.fromBusinessObject(creator, "keyid");
		
		return creator;
	}


	public Creator email(String email) {
		if (email==null) {
			this.email=null;
		} else {
			this.email = new BusinessStringItem("email", email);
		}
		return this;
	}

	public Creator userid(String userid) {
		if (userid==null) {
			this.userid = null;
		} else {
			this.userid = new BusinessStringItem("userid", userid);
		}
		return this;
	}

	public Creator keyid(String keyid) {
		if (keyid==null) {
			this.keyid = null;
		} else {
			this.keyid = new BusinessStringItem("keyid", keyid);
		}
		return this;
	}




	public String getEmail() {
		if (email==null) return null;
		return email.getString();
	}

	public String getUserid() {
		if (userid==null) return null;
		return userid.getString();
	}

	public String getKeyid() {
		if (keyid==null) return null;
		return keyid.getString();
	}
	public String getKeyname() {
		return KEY_NAME;
	}
}
