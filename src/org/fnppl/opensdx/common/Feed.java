package org.fnppl.opensdx.common;

import org.fnppl.opensdx.xml.ChildElementIterator;
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

//import org.fnppl.opensdx.common.*;

public class Feed extends BusinessObject {
	public static String KEY_NAME = "feed";
	
	private FeedInfo feedinfo;								//MUST
	private BusinessCollection<Bundle> bundles;				//SHOULD
	private BusinessCollection<Item> single_items;			//SHOULD
	
	public static Feed make(FeedInfo feedinfo) {
		Feed f = new Feed();
		f.feedinfo = feedinfo;
		f.bundles = new BusinessCollection<Bundle>() {
			public String getKeyname() {
				return "bundles";
			}
		};
		
		f.single_items = new BusinessCollection<Item>() {
			public String getKeyname() {
				return "items";
			}
		};
		return f;
	}
	
	public static Feed fromBusinessObject(BusinessObject bo) {
		if (bo==null) return null;
		if (!bo.getKeyname().equals(KEY_NAME)) {
			bo = bo.handleBusinessObject(KEY_NAME);
		}
		if (bo==null) return null;
		
		final Feed f = new Feed();
		f.initFromBusinessObject(bo);
		
		try {
			f.feedinfo = FeedInfo.fromBusinessObject(f);
			f.bundles = new BusinessCollection<Bundle>() {
				public String getKeyname() {
					return "bundles";
				}
			};
			new ChildElementIterator(bo, "bundles","bundle") {
				public void processBusinessObject(BusinessObject bo) {
					f.addBundle(Bundle.fromBusinessObject(bo));
				}
			};
			f.single_items = new BusinessCollection<Item>() {
				public String getKeyname() {
					return "items";
				}
			};
			new ChildElementIterator(bo, "items","item") {
				public void processBusinessObject(BusinessObject bo) {
					Item item = Item.fromBusinessObject(bo);
					if (item!=null) {
						f.addSingleItem(item);
					}
				}
			};
			return f;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public Feed addBundle(Bundle bundle) {
		bundles.add(bundle);
		return this;
	}
	
	public Feed addSingleItem(Item item) {
		single_items.add(item);
		return this;
	}
	
	public Feed setFeedInfo(FeedInfo feedinfo) {
		this.feedinfo = feedinfo;
		return this;
	}
	
	public FeedInfo getFeedinfo() {
		return feedinfo;
	}

	public Bundle getBundle(int index) {
		if (bundles!=null && index<bundles.size()) {
			return bundles.get(index);
		}
		return null;
	}

	public Item getSingleItems(int index) {
		if (single_items!=null && index<single_items.size()) {
			return single_items.get(index);
		}
		return null;
	}

	public String getKeyname() {
		return KEY_NAME;
	}


}
