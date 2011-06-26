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
public class ItemTags extends BusinessObject {

	public static String KEY_NAME = "tags";

	private BusinessCollection<BusinessStringItem> genres;	//COULD
	private BusinessStringItem origin_country;		//COULD
	private BusinessStringItem main_language;		//COULD
	private BusinessBooleanItem bundle_only;			//COULD
	private BusinessBooleanItem streaming_allowed;	//COULD


	public static ItemTags make(String origin_country, String main_language, boolean bundle_only, boolean streaming_allowed) {
		ItemTags tags = new ItemTags();
		tags.genres = new  BusinessCollection<BusinessStringItem>() {
			public String getKeyname() {
				return "genres";
			}
		};
		tags.origin_country = new BusinessStringItem("origin_country", origin_country);
		tags.main_language = new BusinessStringItem("main_language", main_language);
		tags.bundle_only = new BusinessBooleanItem("bundle_only", bundle_only);
		tags.streaming_allowed = new BusinessBooleanItem("streaming_allowed", streaming_allowed);
		return tags;
	}



	public static ItemTags make() {
		ItemTags tags = new ItemTags();
		tags.genres = null;
		tags.origin_country = null;
		tags.main_language = null;
		tags.bundle_only = null;
		tags.streaming_allowed = null;
		return tags;
	}


	public static ItemTags fromBusinessObject(BusinessObject bo) {
		if (bo==null) return null;
		if (!bo.getKeyname().equals(KEY_NAME)) {
			bo = bo.handleBusinessObject(KEY_NAME);
		}
		if (bo==null) return null;
		final ItemTags tags = new ItemTags();
		tags.initFromBusinessObject(bo);
		
		tags.genres =  new BusinessCollection<BusinessStringItem>() {
			public String getKeyname() {
				return "genres";
			}
		};
		new ChildElementIterator(bo, "genres","genre") {
			public void processBusinessObject(BusinessObject bo) {
				tags.addGenre(BusinessStringItem.fromBusinessObject(bo, "genre").getString());
			}
		};
		tags.origin_country = BusinessStringItem.fromBusinessObject(bo, "origin_country");
		tags.main_language = BusinessStringItem.fromBusinessObject(bo, "main_language");
		tags.bundle_only = BusinessBooleanItem.fromBusinessObject(bo, "bundle_only");
		tags.streaming_allowed = BusinessBooleanItem.fromBusinessObject(bo, "streaming_allowed");
		
		return tags;
	}
	
	public ItemTags addGenre(String genre) {
		if (genres==null) {
			genres = new BusinessCollection<BusinessStringItem>() {
				public String getKeyname() {
					return "genres";
				}
			};
		}
		genres.add(new BusinessStringItem("genre", genre));
		return this;
	}
	
	public void removeGenre(int index) {
		if (genres==null) return;
		genres.remove(index);
	}
	
	public void removeAllGenres() {
		if (genres==null) return;
		genres.removeAll();
	}
	
	public int getGenresCount() {
		if (genres==null) return 0;
		return genres.size();
	}
	
	public String getGenre(int index) {
		if (genres==null) return null;
		return genres.get(index).getString();
	}


	public ItemTags origin_country(String origin_country) {
		if (origin_country==null) this.origin_country = null;
		this.origin_country = new BusinessStringItem("origin_country", origin_country);
		return this;
	}

	public ItemTags main_language(String main_language) {
		if (main_language==null) this.main_language = null;
		this.main_language = new BusinessStringItem("main_language", main_language);
		return this;
	}

	public ItemTags bundle_only(boolean bundle_only) {
		this.bundle_only = new BusinessBooleanItem("bundle_only", bundle_only);
		return this;
	}

	public ItemTags streaming_allowed(boolean streaming_allowed) {
		this.streaming_allowed = new BusinessBooleanItem("streaming_allowed", streaming_allowed);
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

	public boolean isBundle_only() {
		if (bundle_only==null) return false;
		return bundle_only.getBoolean();
	}

	public boolean isStreaming_allowed() {
		if (streaming_allowed==null) return false;
		return streaming_allowed.getBoolean();
	}
	public String getKeyname() {
		return KEY_NAME;
	}
}
