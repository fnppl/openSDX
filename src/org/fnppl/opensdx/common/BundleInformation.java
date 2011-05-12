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

	private Vector<BusinessStringItem> promotext;						//SHOULD
	private Vector<BusinessStringItem> teasertext;						//SHOULD
	private BusinessDatetimeItem physical_release_datetime;				//MUST
	private BusinessDatetimeItem digital_release_datetime;				//MUST
	private BusinessIntegerItem playlength;								//MUST for media files
	private BundleRelatedInformation related;							//SHOULD


	public static BundleInformation make(long physical_release_datetime, long digital_release_datetime) {
		BundleInformation information = new BundleInformation();
		information.promotext = new Vector<BusinessStringItem>();
		information.teasertext = new Vector<BusinessStringItem>();
		information.physical_release_datetime = new BusinessDatetimeItem("physical_release_datetime", physical_release_datetime);
		information.digital_release_datetime = new BusinessDatetimeItem("digital_release_datetime", digital_release_datetime);
		information.playlength = null;
		information.related = BundleRelatedInformation.make();
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
		
		information.promotext = new Vector<BusinessStringItem>();
		new ChildElementIterator(bo, "promotext") {
			public void processBusinessStringItem(BusinessStringItem item) {
				information.promotext.add(item);
			}
		};
		information.teasertext = new Vector<BusinessStringItem>();
		new ChildElementIterator(bo, "teasertext") {
			public void processBusinessStringItem(BusinessStringItem item) {
				information.teasertext.add(item);
			}
		};
		information.physical_release_datetime = BusinessDatetimeItem.fromBusinessObject(bo, "physical_release_datetime");
		information.digital_release_datetime = BusinessDatetimeItem.fromBusinessObject(bo, "digital_release_datetime");
		information.playlength = BusinessIntegerItem.fromBusinessObject(bo, "playlength");
		information.related = BundleRelatedInformation.fromBusinessObject(bo);
		return information;
	}


	public BundleInformation addPromotext(String language, String promotext) {
		BusinessStringItem text = new BusinessStringItem("promotext", promotext);
		text.setAttribute("lang", language);
		this.promotext.add(text);
		return this;
	}

	public BundleInformation addTeasertext(String language, String teasertext) {
		BusinessStringItem text = new BusinessStringItem("teasertext", teasertext);
		text.setAttribute("lang", language);
		this.teasertext.add(text);
		return this;
	}

	public BundleInformation physical_release_datetime(long physical_release_datetime) {
		this.physical_release_datetime = new BusinessDatetimeItem("physical_release_datetime", physical_release_datetime);
		return this;
	}

	public BundleInformation digital_release_datetime(long digital_release_datetime) {
		this.digital_release_datetime = new BusinessDatetimeItem("digital_release_datetime", digital_release_datetime);
		return this;
	}

	public BundleInformation playlength(int playlength_in_seconds) {
		this.playlength = new BusinessIntegerItem("playlength", playlength_in_seconds);
		return this;
	}
	
	public BundleInformation related(BundleRelatedInformation related) {
		this.related = related;
		return this;
	}

	public String getPromotext(int index) {
		if (promotext==null || index<0 || index>=promotext.size()) return null;
		return promotext.get(index).getString();
	}
	public String getPromotextLanguage(int index) {
		if (promotext==null || index<0 || index>=promotext.size()) return null;
		return promotext.get(index).getAttribute("lang");
	}

	public String getTeasertext(int index) {
		if (teasertext==null || index<0 || index>=teasertext.size()) return null;
		return teasertext.get(index).getString();
	}
	public String getTeasertextLanguage(int index) {
		if (teasertext==null || index<0 || index>=teasertext.size()) return null;
		return teasertext.get(index).getAttribute("lang");
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
	
	public BundleRelatedInformation getRelated() {
		return related;
	}
	
	public String getKeyname() {
		return KEY_NAME;
	}
}
