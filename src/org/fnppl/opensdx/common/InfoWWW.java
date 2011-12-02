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

public class InfoWWW extends BusinessObject {

	public static String KEY_NAME = "www";

	private BusinessStringItem facebook;			//COULD
	private BusinessStringItem myspace;				//COULD
	private BusinessStringItem homepage;			//COULD
	private BusinessStringItem twitter;				//COULD
	private BusinessStringItem blog;				//COULD
	private BusinessStringItem phone;				//COULD


	public static InfoWWW make(String facebook, String myspace, String homepage, String twitter, String phone) {
		InfoWWW www = make();
		www.facebook(facebook);
		www.myspace(myspace);
		www.homepage(homepage);
		www.twitter(twitter);
		www.phone(phone);
		www.blog = null;
		
		return www;
	}

	public static InfoWWW make() {
		InfoWWW www = new InfoWWW();
		www.facebook = null;
		www.myspace = null;
		www.homepage = null;
		www.twitter = null;
		www.phone = null;
		www.blog = null;
		
		return www;
	}


	public static InfoWWW fromBusinessObject(BusinessObject bo) {
		if (bo==null) return null;
		if (!bo.getKeyname().equals(KEY_NAME)) {
			bo = bo.handleBusinessObject(KEY_NAME);
		}
		if (bo==null) return null;
		InfoWWW www = new InfoWWW();
		www.initFromBusinessObject(bo);
		
		www.facebook = BusinessStringItem.fromBusinessObject(bo, "facebook");
		www.myspace = BusinessStringItem.fromBusinessObject(bo, "myspace");
		www.homepage = BusinessStringItem.fromBusinessObject(bo, "homepage");
		www.twitter = BusinessStringItem.fromBusinessObject(bo, "twitter");
		www.blog = BusinessStringItem.fromBusinessObject(bo, "blog");
		www.phone = BusinessStringItem.fromBusinessObject(bo, "phone");
		
		return www;
	}


	public InfoWWW facebook(String facebook) {
		if (facebook==null) {
			this.facebook = null;
		} else {
			this.facebook = new BusinessStringItem("facebook", facebook);
		}
		return this;
	}

	public InfoWWW myspace(String myspace) {
		if (myspace==null) {
			this.myspace = null;
		} else {
			this.myspace = new BusinessStringItem("myspace", myspace);
		}
		return this;
	}

	public InfoWWW homepage(String homepage) {
		if (homepage==null) {
			this.homepage = null;
		} else {
			this.homepage = new BusinessStringItem("homepage", homepage);
		}
		return this;
	}

	public InfoWWW twitter(String twitter) {
		if (twitter==null) {
			this.twitter = null;
		} else {
			this.twitter = new BusinessStringItem("twitter", twitter);
		}
		return this;
	}
	
	public InfoWWW blog(String blog) {
		if (blog==null) {
			this.blog = null;
		} else {
			this.blog = new BusinessStringItem("blog", blog);
		}
		return this;
	}

	public InfoWWW phone(String phone) {
		if (phone==null) {
			this.phone = null;
		} else {
			this.phone = new BusinessStringItem("phone", phone);
		}
		return this;
	}
	
	public InfoWWW phone(String phone, boolean publishable) {
		if (phone==null) {
			this.phone = null;
		} else {
			this.phone = new BusinessStringItem("phone", phone);
			this.phone.setAttribute("publishable", ""+publishable);
		}
		return this;
	}


	public String getFacebook() {
		if (facebook==null) return null;
		return facebook.getString();
	}

	public String getMyspace() {
		if (myspace==null) return null;
		return myspace.getString();
	}

	public String getHomepage() {
		if (homepage==null) return null;
		return homepage.getString();
	}

	public String getTwitter() {
		if (twitter==null) return null;
		return twitter.getString();
	}
	
	public String getBlog() {
		if (blog==null) return null;
		return blog.getString();
	}

	public String getPhone() {
		if (phone==null) return null;
		return phone.getString();
	}
	
	public boolean isPhonePublishable() {
		if (phone==null) return true;
		String pb = phone.getAttribute("publishable");
		if (pb==null) return true;
		return Boolean.parseBoolean(pb);
	}
	
	public String getKeyname() {
		return KEY_NAME;
	}
}

