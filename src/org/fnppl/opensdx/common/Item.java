package org.fnppl.opensdx.common;

import java.util.Vector;

import org.fnppl.opensdx.dmi.MediaFile;


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
 * may be track, video, pdf, flash, whatever
 * 
 */

public class Item extends BaseObjectWithConstraints {

		public Item() {
			names.add("type"); values.add(null); constraints.add("MUST");
			names.add("ids"); values.add(null); constraints.add("MUST");
			names.add("displayname"); values.add(null); constraints.add("MUST");
			names.add("name"); values.add(null); constraints.add("MUST");
			names.add("version"); values.add(null); constraints.add("MUST");
			names.add("contributors"); values.add(null); constraints.add("MUST");
			names.add("information"); values.add(null); constraints.add("[no comment]");
			
			names.add("territorial"); values.add(new Vector<Territory>()); constraints.add("MUST");
			names.add("timeframe_from"); values.add(null); constraints.add("MUST");
			names.add("timeframe_until"); values.add(null); constraints.add("MUST");
			names.add("pricecode"); values.add(null); constraints.add("COULD");
			names.add("wholesale"); values.add(null); constraints.add("COULD");
			
			//tags
			names.add("genres"); values.add(new Vector<String>()); constraints.add("COULD");
			names.add("origin_country"); values.add(null); constraints.add("COULD");
			names.add("main_language"); values.add(null); constraints.add("COULD");
			names.add("bundle_only"); values.add(null); constraints.add("COULD");
			names.add("streaming_allowed"); values.add(null); constraints.add("COULD");
			//-- end of tags
			
			names.add("files"); values.add(new Vector<MediaFile>()); constraints.add("SHOULD");
		}

	// methods
		public void setType(String type) {
			set("type", type);
		}

		public String getType() {
			return get("type");
		}

		public void setIds(BundleIDs ids) {
			set("ids", ids);
		}

		public BundleIDs getIds() {
			return (BundleIDs)getObject("ids");
		}

		public void setDisplayname(String displayname) {
			set("displayname", displayname);
		}

		public String getDisplayname() {
			return get("displayname");
		}

		public void setName(String name) {
			set("name", name);
		}

		public String getName() {
			return get("name");
		}

		public void setVersion(String version) {
			set("version", version);
		}

		public String getVersion() {
			return get("version");
		}

		public void addContributor(Contributor c) {
			((Vector<Contributor>)values.elementAt(names.indexOf("contributors"))).add(c);
		}

		public void removeContributor(int index) {
			((Vector<Contributor>)values.elementAt(names.indexOf("contributors"))).remove(index);
		}

		public Vector<Contributor> getContributor() {
			return (Vector<Contributor>)values.elementAt(names.indexOf("contributors"));
		}

		public void setInformation(BundleInformation information) {
			set("information", information);
		}

		public BundleInformation getInformation() {
			return (BundleInformation)getObject("information");
		}

		public void addTerritory(Territory t) {
			((Vector<Territory>)values.elementAt(names.indexOf("territorial"))).add(t);
		}

		public void removeTerritory(int index) {
			((Vector<Territory>)values.elementAt(names.indexOf("territorial"))).remove(index);
		}

		public Vector<Territory> getTerritory() {
			return (Vector<Territory>)values.elementAt(names.indexOf("territorial"));
		}
		
		public void setTimeframeFrom(long from) {
			set("timeframe_from", from);
		}

		public long getTimeframeFrom() {
			return getLong("timeframe_from");
		}

		public void setTimeFrameUntil(long to) {
			set("timeframe_until", to);
		}

		public long getTimeFrameUntil() {
			return getLong("timeframe_until");
		}
		public void setPricecode(String pricecode) {
			set("pricecode", pricecode);
		}

		public String getPricecode() {
			return get("pricecode");
		}

		public void setWholesale(String wholesale) {
			set("wholesale", wholesale);
		}

		public String getWholesale() {
			return get("wholesale");
		}
		
		//tags
		public void addGenres(String genre) {
			((Vector<String>)getObject("genres")).add(genre);
		}

		public Vector<String> getGenres() {
			return (Vector<String>)getObject("genres");
		}
		
		public void removeGenres(String genre) {
			((Vector<String>)getObject("genres")).remove(genre);
		}
		public void removeGenres(int ind) {
			((Vector<String>)getObject("genres")).remove(ind);
		}
		
		public void setOrigin_country(String origin_country) {
			set("origin_country", origin_country);
		}

		public String getOrigin_country() {
			return get("origin_country");
		}

		public void setMain_language(String main_language) {
			set("main_language", main_language);
		}

		public String getMain_language() {
			return get("main_language");
		}

		public void setBundle_only(String bundle_only) {
			set("bundle_only", bundle_only);
		}

		public String getBundle_only() {
			return get("bundle_only");
		}

		public void setStreaming_allowed(String streaming_allowed) {
			set("streaming_allowed", streaming_allowed);
		}

		public String getStreaming_allowed() {
			return get("streaming_allowed");
		}
		//-- end of tags

		public void addMediaFile(MediaFile t) {
			((Vector<MediaFile>)values.elementAt(names.indexOf("files"))).add(t);
		}

		public void removeMediaFile(int index) {
			((Vector<MediaFile>)values.elementAt(names.indexOf("files"))).remove(index);
		}

		public Vector<MediaFile> getMediaFile() {
			return (Vector<MediaFile>)values.elementAt(names.indexOf("files"));
		}
		


}
