package org.fnppl.opensdx.common;

import java.util.Vector;
/*
 * Copyright (C) 2010-2011 
 * 							fine people e.V. <opensdx@fnppl.org> 
 * 							Henning Thieß <ht@fnppl.org>
 * 
 * 							http://fnppl.org
*/

import org.fnppl.opensdx.xml.ChildElementIterator;
import org.fnppl.opensdx.xml.Element;

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
public class BundleInformation extends BusinessObject {

	public static String KEY_NAME = "information";

	private BundleTexts texts;											//SHOULD
	private BusinessDatetimeItem physical_release_datetime;				//MUST
	private BusinessDatetimeItem digital_release_datetime;				//MUST
	private BusinessIntegerItem playlength;								//MUST for media files
	private BusinessIntegerItem num;									//SHOULD
	private BusinessIntegerItem setnum;									//SHOULD
	private BusinessStringItem origin_country;							//COULD
	private BusinessStringItem main_language;							//COULD
	private BusinessIntegerItem suggested_prelistening_offset;			//COULD
	private BundleRelatedInformation related;							//SHOULD

	public static BundleInformation make(long physical_release_datetime, long digital_release_datetime) {
		BundleInformation information = new BundleInformation();
		information.texts = new BundleTexts();
		information.physical_release_datetime = new BusinessDatetimeItem("physical_release_datetime", physical_release_datetime);
		information.digital_release_datetime = new BusinessDatetimeItem("digital_release_datetime", digital_release_datetime);
		information.playlength = null;
		information.num = null;
		information.setnum = null;
		information.related = BundleRelatedInformation.make();
		information.origin_country = null;
		information.main_language = null;
		information.suggested_prelistening_offset = null;
		return information;
	}


	public static BundleInformation fromBusinessObject(BusinessObject bo) {
		if (bo==null) return null;
		if (!bo.getKeyname().equals(KEY_NAME)) {
			bo = bo.handleBusinessObject(KEY_NAME);
		}
		if (bo==null) return null;
		final BundleInformation information = new BundleInformation();
		information.initFromBusinessObject(bo);
		
		information.texts = BundleTexts.fromBusinessObject(bo);
		information.physical_release_datetime = BusinessDatetimeItem.fromBusinessObject(bo, "physical_release_datetime");
		information.digital_release_datetime = BusinessDatetimeItem.fromBusinessObject(bo, "digital_release_datetime");
		information.playlength = BusinessIntegerItem.fromBusinessObject(bo, "playlength");
		information.num = BusinessIntegerItem.fromBusinessObject(bo, "num");
		information.setnum = BusinessIntegerItem.fromBusinessObject(bo, "setnum");
		information.related = BundleRelatedInformation.fromBusinessObject(bo);
		information.origin_country = BusinessStringItem.fromBusinessObject(bo, "origin_country");
		information.main_language = BusinessStringItem.fromBusinessObject(bo, "main_language");
		information.suggested_prelistening_offset = BusinessIntegerItem.fromBusinessObject(bo, "suggested_prelistening_offset");
		return information;
	}


	public BundleInformation origin_country(String origin_country) {
		if (origin_country==null) {
			this.origin_country = null; 
		} else {
			this.origin_country = new BusinessStringItem("origin_country", origin_country);
		}
		return this;
	}

	public BundleInformation main_language(String main_language) {
		if (main_language==null) {
			this.main_language = null;
		} else {
			this.main_language = new BusinessStringItem("main_language", main_language);
		}
		return this;
	}
	
	public String getOrigin_country() {
		if (origin_country==null) return null;
		return origin_country.getString();
	}

	public String getMain_language() {
		if (main_language==null) return null;
		return main_language.getString();
	}

	public BundleInformation physical_release_datetime(long physical_release_datetime) {
		this.physical_release_datetime = new BusinessDatetimeItem("physical_release_datetime", physical_release_datetime);
		return this;
	}

	public BundleInformation digital_release_datetime(long digital_release_datetime) {
		this.digital_release_datetime = new BusinessDatetimeItem("digital_release_datetime", digital_release_datetime);
		return this;
	}

	public BundleInformation playlength(Integer playlength_in_seconds) {
		if (playlength_in_seconds==null) {
			this.playlength = null;
		} else {
			this.playlength = new BusinessIntegerItem("playlength", playlength_in_seconds.intValue());
		}
		return this;
	}
	public BundleInformation num(Integer num) {
		if (num==null) {
			this.num = null;
		} else {
			this.num = new BusinessIntegerItem("num", num.intValue());
		}
		return this;
	}
	public BundleInformation setnum(Integer setnum) {
		if (setnum==null) {
			this.setnum = null;
		} else {
			this.setnum = new BusinessIntegerItem("setnum", setnum.intValue());
		}
		return this;
	}
	
	public BundleInformation suggested_prelistening_offset(Integer suggested_prelistening_offset) {
		if (suggested_prelistening_offset==null) {
			this.suggested_prelistening_offset = null;
		} else {
			this.suggested_prelistening_offset = new BusinessIntegerItem("suggested_prelistening_offset", suggested_prelistening_offset.intValue());
		}
		return this;
	}	
	
	public BundleInformation related(BundleRelatedInformation related) {
		this.related = related;
		return this;
	}
	
	public BundleInformation texts(BundleTexts texts) {
		this.texts = texts;
		return this;
	}	
	
	public String getPhysicalReleaseDatetimeText() {
		if (physical_release_datetime==null) return null;
		return physical_release_datetime.getDatetimeStringGMT();
	}
	public long getPhysicalReleaseDatetime() {
		if (physical_release_datetime==null) throw new RuntimeException("value not set");
		return physical_release_datetime.getDatetime();
	}

	public String getDigitalReleaseDatetimeText() {
		if (digital_release_datetime==null) return null;
		return digital_release_datetime.getDatetimeStringGMT();
	}
	public long getDigitalReleaseDatetime() {
		if (digital_release_datetime==null) throw new RuntimeException("value not set");
		return digital_release_datetime.getDatetime();
	}

	public int getPlaylength() {
		if (playlength==null) throw new RuntimeException("value not set");
		return playlength.getIntValue();
	}
	
	public int getSetNum() {
		if (setnum==null) throw new RuntimeException("value not set");
		return setnum.getIntValue();
	}
	
	public int getNum() {
		if (num==null) throw new RuntimeException("value not set");
		return num.getIntValue();
	}
	
	public int getSuggestedPrelistiningOffset() {
		if (suggested_prelistening_offset==null) throw new RuntimeException("value not set");
		return suggested_prelistening_offset.getIntValue();
	}	
	
	public boolean hasPlaylength() {
		if (playlength==null) return false;
		return true;
	}
	public boolean hasNum() {
		if (num==null) return false;
		return true;
	}
	public boolean hasSetNum() {
		if (setnum==null) return false;
		return true;
	}
	
	public boolean hasSuggestedPrelistiningOffset() {
		if (suggested_prelistening_offset==null) return false;
		return true;
	}
	
	public BundleRelatedInformation getRelated() {
		return related;
	}
	
	public BundleTexts getTexts() {
		return texts;
	}	
	
	public String getKeyname() {
		return KEY_NAME;
	}
}
