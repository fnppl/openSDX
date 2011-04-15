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
import org.fnppl.opensdx.common.BaseObjectWithConstraints;
import org.fnppl.opensdx.xml.Element;

public class BundleRelatedInformation extends BaseObjectWithConstraints {


	private BundleRelatedInformation() {
		names.add("type"); values.add(null);  constraints.add("MUST");//physical_distributer, youtube, BundleIDs, 
		names.add("values");values.add(null);  constraints.add("MUST");
	}

	public static BundleRelatedInformation createPhysicalDistributer(String name) {
		BundleRelatedInformation r = new BundleRelatedInformation();
		r.set("type","physical_distributer");
		r.set("values",name);
		return r;
	}

	public static BundleRelatedInformation createPhysicalDistributer(String name, boolean publishable) {
		BundleRelatedInformation r = new BundleRelatedInformation();
		r.set("type","physical_distributer");
		r.set("values",new String[] {"publishable",""+publishable,name});
		return r;
	}
	
	public static BundleRelatedInformation createYouTube(String url, String channel) {
		BundleRelatedInformation r = new BundleRelatedInformation();
		r.set("type","youtube");
		r.set("values",new String[]{url, channel});
		return r;
	}
	
	public static BundleRelatedInformation createBundleIDs(BundleIDs ids) {
		BundleRelatedInformation r = new BundleRelatedInformation();
		r.set("type","bundle_ids");
		r.set("values",ids);
		return r;
	}

	public String getType() {
		return get("type");
	}
	
	public Element toElement() {
		String type = get("type");
		Object value = getObject("values");
		if (value!=null) {
			if (type.equals("physical_distributer")) {
				Element e = new Element(type);
				if (value instanceof String) {
					e.setText((String)value);
				} 
				else if (value instanceof String[]) {
					String[] s = (String[])value;
					for (int j=0;j<s.length-1;j+=2) {
			       		e.setAttribute(s[j*2], s[j*2+1]);
			       	}
			       	e.setText(s[s.length-1]);
				}
				return e;
			}
			else if (type.equals("youtube")) {
				Element e = new Element(type);
				String[] s = (String[])value;
				e.addContent("url", s[0]);
				e.addContent("channel", s[1]);
				return e;
			}
			else if (type.equals("bundle_ids")) {
				Element e = new Element("bundle");
				e.addContent(((BundleIDs)value).toElement("ids"));
				return e;
			}
		}
		return null;
	}
	
	public Element toElement(String name) {
		//Ignore name
		return toElement();
	}
	
}
