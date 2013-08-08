package org.fnppl.opensdx.common;

import org.fnppl.opensdx.xml.ChildElementIterator;
/*
 * Copyright (C) 2010-2013 
 * 							fine people e.V. <opensdx@fnppl.org> 
 * 							Henning Thieß <ht@fnppl.org>
 * 
 * 							http://fnppl.org
*/
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
public class ItemTags extends BusinessObject {

	public static String KEY_NAME = "tags";
	public static String EXPLICIT_LYRICS_TRUE = "true";
	public static String EXPLICIT_LYRICS_FALSE = "false";
	public static String EXPLICIT_LYRICS_CLEANED = "cleaned";
	
	private BusinessCollection<BusinessStringItem> genres;	//COULD
	private BusinessBooleanItem bundle_only;				//COULD
	private BusinessStringItem explicit_lyrics;				//COULD
	private BusinessIntegerItem recommended_age_from;		//COULD
	private BusinessBooleanItem live;						//COULD
	private BusinessBooleanItem accoustic;					//COULD
	private BusinessBooleanItem instrumental;				//COULD
	private BusinessBooleanItem abridged;					//COULD

	public static ItemTags make() {
		ItemTags tags = new ItemTags();
		tags.genres = null;
		tags.bundle_only = null;
		tags.explicit_lyrics = null;
		tags.recommended_age_from = null;
		tags.live = null;
		tags.accoustic = null;
		tags.instrumental = null;
		tags.abridged = null;
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
			public void processBusinessStringItem(BusinessStringItem item) {
				//System.out.println("adding genre::"+item.getString());
				tags.genres.add(item);
				//tags.addGenre(BusinessStringItem.fromBusinessObject(bo, "genre").getString());
			}
		};
		
		tags.bundle_only = BusinessBooleanItem.fromBusinessObject(bo, "bundle_only");
		tags.explicit_lyrics = BusinessStringItem.fromBusinessObject(bo, "explicit_lyrics");
		tags.recommended_age_from = BusinessIntegerItem.fromBusinessObject(bo, "recommended_age_from");
		tags.live = BusinessBooleanItem.fromBusinessObject(bo, "live");
		tags.accoustic = BusinessBooleanItem.fromBusinessObject(bo, "accoustic");
		tags.instrumental = BusinessBooleanItem.fromBusinessObject(bo, "instrumental");
		tags.abridged = BusinessBooleanItem.fromBusinessObject(bo, "abridged");
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
	
	public ItemTags addGenre(String genre, int genreid) {
		if (genres==null) {
			genres = new BusinessCollection<BusinessStringItem>() {
				public String getKeyname() {
					return "genres";
				}
			};
		}
		BusinessStringItem bsi = new BusinessStringItem("genre", genre);
		bsi.setAttribute("id", ""+genreid);
		genres.add(bsi);
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
	
	public int getGenreId(int index) {
		if (genres != null && genres.get(index) != null && genres.get(index).getAttribute("id") != null) {
			return Integer.parseInt(genres.get(index).getAttribute("id"));
		}
		return -1;
	}


	public ItemTags bundle_only(boolean bundle_only) {
		this.bundle_only = new BusinessBooleanItem("bundle_only", bundle_only);
		return this;
	}
	
	public ItemTags explicit_lyrics(String explicit_lyrics) {
		if (explicit_lyrics==null) {
			this.explicit_lyrics = null;
		} else {
			this.explicit_lyrics = new BusinessStringItem("explicit_lyrics", explicit_lyrics);
		}
		return this;
	}
	
	public ItemTags explicit_lyrics(boolean explicit_lyrics) {
		this.explicit_lyrics = new BusinessStringItem("explicit_lyrics", (explicit_lyrics?"true":"false"));
		return this;
	}
	
	public ItemTags recommended_age_from(int recommended_age_from) {
		this.recommended_age_from = new BusinessIntegerItem("recommended_age_from", recommended_age_from);
		return this;
	}	
	
	public ItemTags live(boolean live) {
		this.live = new BusinessBooleanItem("live", live);
		return this;
	}	
	
	public ItemTags accoustic(boolean accoustic) {
		this.accoustic = new BusinessBooleanItem("accoustic", accoustic);
		return this;
	}
	
	public ItemTags instrumental(boolean instrumental) {
		this.instrumental = new BusinessBooleanItem("instrumental", instrumental);
		return this;
	}	
	
	public ItemTags abridged(boolean abridged) {
		this.abridged = new BusinessBooleanItem("abridged", abridged);
		return this;
	}	

	public boolean isBundle_only() {
		if (bundle_only==null) return false;
		return bundle_only.getBoolean();
	}
	
	public boolean hasBundle_only() {
		return (bundle_only != null);
	}
	
	public boolean isExplicit_lyrics() {
		if (explicit_lyrics==null) return false;
		String e = explicit_lyrics.getString();
		if (e.equalsIgnoreCase("true")) return true;
		return false;
	}
	
	public boolean hasExplicit_lyrics() {
		return (explicit_lyrics != null);
	}
		
	public int getRecommended_age_from() {
		if (recommended_age_from==null) return -1;
		return recommended_age_from.getIntValue();
	}
	
	public boolean hasRecommended_age_from() {
		return (recommended_age_from != null);
	}
	
	public String getExplicit_lyrics() {
		if (explicit_lyrics==null) return null;
		return explicit_lyrics.getString();
	}
	
	public boolean isLive() {
		if (live==null) return false;
		return live.getBoolean();
	}
	
	public boolean hasLive() {
		return (live != null);
	}
	
	public boolean isAccoustic() {
		if (accoustic==null) return false;
		return accoustic.getBoolean();
	}
	
	public boolean hasAccoustic() {
		return (accoustic != null);
	}
	
	public boolean isInstrumental() {
		if (instrumental==null) return false;
		return instrumental.getBoolean();
	}
	
	public boolean hasInstrumental() {
		return (instrumental != null);
	}
	
	public boolean isAbridged() {
		if (abridged==null) return false;
		return abridged.getBoolean();
	}
	
	public boolean hasAbridged() {
		return (abridged != null);
	}

	public String getKeyname() {
		return KEY_NAME;
	}
	
	public Element toElement() {
		if (genres == null
		   && bundle_only == null
		   && explicit_lyrics == null) {
			return null;
		}
		return super.toElement();
	}
}
