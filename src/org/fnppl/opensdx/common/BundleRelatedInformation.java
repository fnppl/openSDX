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

import java.util.Vector;

import org.fnppl.opensdx.xml.ChildElementIterator;


/**
 * 
 * @author Bertram Boedeker <bboedeker@gmx.de>
 * 
 */
public class BundleRelatedInformation extends BusinessObject {

	public static String KEY_NAME = "related";

	private BusinessStringItem physical_distributor;					//COULD
	private BusinessStringItem youtube_url;								//COULD
	private BusinessStringItem youtube_channel;							//COULD
	private Vector<BusinessObject> related_bundles; 					//COULD

	public static BundleRelatedInformation make() {
		BundleRelatedInformation related = new BundleRelatedInformation();
		related.physical_distributor = null;
		related.youtube_url = null;
		related.youtube_channel = null;
		related.related_bundles = new Vector<BusinessObject>();
		return related;
	}
	
	public BundleRelatedInformation addRelatedBundleIDs(IDs ids) {
		BusinessObject bos = new BusinessObject() {
			public String getKeyname() {
				return "bundle";
			}
		};
		bos.addObject(ids);
		bos.setAppendOtherObjectToOutput(true);
		related_bundles.add(bos);
		return this;
	}

	public static BundleRelatedInformation fromBusinessObject(BusinessObject bo) {
		if (bo==null) return null;
		if (!bo.getKeyname().equals(KEY_NAME)) {
			bo = bo.handleBusinessObject(KEY_NAME);
		}
		if (bo==null) return null;
		final BundleRelatedInformation related = new BundleRelatedInformation();
		related.initFromBusinessObject(bo);
		
		related.physical_distributor = BusinessStringItem.fromBusinessObject(bo, "physical_distributor");
		related.youtube_url = BusinessStringItem.fromBusinessObject(bo, "youtube_url");
		related.youtube_channel = BusinessStringItem.fromBusinessObject(bo, "youtube_channel");
		related.related_bundles = new Vector<BusinessObject>();
		new ChildElementIterator(bo, "bundle") {
			public void processBusinessObject(BusinessObject bo) {
				IDs ids = IDs.fromBusinessObject(bo.handleBusinessObject("ids"));
				bo.addObject(ids);
				bo.setAppendOtherObjectToOutput(true);
				related.related_bundles.add(bo);
			}
		};
		return related;
	}


	public BundleRelatedInformation physical_distributor(String physical_distributor) {
		this.physical_distributor = new BusinessStringItem("physical_distributor", physical_distributor);
		return this;
	}
	
	public BundleRelatedInformation physical_distributor(String physical_distributor, boolean publishable) {
		BusinessStringItem item = new BusinessStringItem("physical_distributor", physical_distributor);
		item.setAttribute("publishable",""+publishable);
		this.physical_distributor = item;
		return this;
	}

	public BundleRelatedInformation youtube_url(String youtube_url) {
		this.youtube_url = new BusinessStringItem("youtube_url", youtube_url);
		return this;
	}

	public BundleRelatedInformation youtube_channel(String youtube_channel) {
		this.youtube_channel = new BusinessStringItem("youtube_channel", youtube_channel);
		return this;
	}

	public String getPhysicalDistributor() {
		if (physical_distributor==null) return null;
		return physical_distributor.getString();
	}

	public boolean isPhysicalDistributorPublishable() {
		if (physical_distributor==null) return true;
		String pb = physical_distributor.getAttribute("publishable");
		if (pb==null) return true;
		return Boolean.parseBoolean(pb);
	}

	public String getYoutubeUrl() {
		if (youtube_url==null) return null;
		return youtube_url.getString();
	}

	public String getYoutubeChannel() {
		if (youtube_channel==null) return null;
		return youtube_channel.getString();
	}
	public String getKeyname() {
		return KEY_NAME;
	}
}
