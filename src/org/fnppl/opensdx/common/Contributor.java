package org.fnppl.opensdx.common;

import java.util.Vector;

import org.fnppl.opensdx.commonAuto.Ids;
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
 * may be author, writer, composer, texter, special-effects-über-guru
 * can be assigned to items
 * 
 */

public class Contributor extends BaseObjectWithConstraints {
	
	public Contributor() {
		names.add("name"); values.add(null); constraints.add("MUST");
		names.add("type"); values.add(null); constraints.add("MUST");
		names.add("ids"); values.add(null); constraints.add("MUST");
		
		names.add("facebook"); values.add(null); constraints.add("COULD");
		names.add("myspace"); values.add(null); constraints.add("COULD");
		names.add("homepage"); values.add(null); constraints.add("COULD");
		names.add("twitter"); values.add(null); constraints.add("COULD");
		names.add("phone");  values.add(new Vector<String[]>()); constraints.add("COULD");
	}

// methods
	public void setName(String name) {
		set("name", name);
	}

	public String getName() {
		return get("name");
	}

	public void setType(String type) {
		set("type", type);
	}

	public String getType() {
		return get("type");
	}

	public void setBundleIDs(BundleIDs ids) {
		set("ids", ids);
	}

	public BundleIDs getIds() {
		return (BundleIDs)getObject("ids");
	}

	public void setFacebook(String facebook) {
		set("Facebook",new String[]{facebook,"true"});
	}

	public void setFacebook(String facebook, boolean publishable) {
		set("facebook",new String[]{facebook,""+publishable});
	}
	public String getFacebook() {
		String[] s = (String[])getObject("facebook");
		if (s!=null && s.length>1) {
			return s[1];
		}
		return null;
	}

	public void setMyspace(String myspace) {
		set("Myspace",new String[]{myspace,"true"});
	}

	public void setMyspace(String myspace, boolean publishable) {
		set("myspace",new String[]{myspace,""+publishable});
	}
	public String getMyspace() {
		String[] s = (String[])getObject("myspace");
		if (s!=null && s.length>1) {
			return s[1];
		}
		return null;
	}

	public void setHomepage(String homepage) {
		set("Homepage",new String[]{homepage,"true"});
	}

	public void setHomepage(String homepage, boolean publishable) {
		set("homepage",new String[]{homepage,""+publishable});
	}
	public String getHomepage() {
		String[] s = (String[])getObject("homepage");
		if (s!=null && s.length>1) {
			return s[1];
		}
		return null;
	}

	public void setTwitter(String twitter) {
		set("Twitter",new String[]{twitter,"true"});
	}

	public void setTwitter(String twitter, boolean publishable) {
		set("twitter",new String[]{twitter,""+publishable});
	}
	public String getTwitter() {
		String[] s = (String[])getObject("twitter");
		if (s!=null && s.length>1) {
			return s[1];
		}
		return null;
	}

	public void setPhone(String phone) {
		set("Phone",new String[]{phone,"true"});
	}

	public void setPhone(String phone, boolean publishable) {
		set("phone",new String[]{phone,""+publishable});
	}
	public String getPhone() {
		String[] s = (String[])getObject("phone");
		if (s!=null && s.length>1) {
			return s[1];
		}
		return null;
	}
}
