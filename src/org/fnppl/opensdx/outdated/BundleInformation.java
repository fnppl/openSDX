package org.fnppl.opensdx.outdated;

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

import org.fnppl.opensdx.outdated.BaseObjectWithConstraints;
import org.fnppl.opensdx.xml.Element;

public class BundleInformation extends BaseObjectWithConstraints {

	public BundleInformation() {
		names.add("promotext");  values.add(new Vector<String[]>()); constraints.add("COULD");
		names.add("teasertext");  values.add(new Vector<String[]>()); constraints.add("COULD");
		names.add("physical_release_datetime"); values.add(null); constraints.add("SHOULD");
		names.add("digital_release_datetime"); values.add(null); constraints.add("SHOULD");
		names.add("related"); values.add(new Vector<BundleRelatedInformation>()); constraints.add("COULD");
	}

// methods
	public Vector<String[]> getPromotext() {
		return (Vector<String[]>)values.elementAt(names.indexOf("promotext"));
	}
	public String getPromotext(String lang) {
		Vector<String[]> vs = (Vector<String[]>)values.elementAt(names.indexOf("promotext"));
		for (String[] s : vs)  {
			if (s[1].equals(lang) && s[0].equals("lang")) {
				return s[2];
			}
		}
		return null;
	}

	public void addPromotext(String lang, String promotext) {
		((Vector<String[]>)values.elementAt(names.indexOf("promotext"))).add(new String[]{"lang", lang, promotext});
	}

	public void removePromotext(int index) {
		((Vector<String[]>)values.elementAt(names.indexOf("promotext"))).remove(index);
	}

	public Vector<String[]> getTeasertext() {
		return (Vector<String[]>)values.elementAt(names.indexOf("teasertext"));
	}
	
	public String getTeasertext(String lang) {
		Vector<String[]> vs = (Vector<String[]>)values.elementAt(names.indexOf("teasertext"));
		for (String[] s : vs)  {
			if (s[1].equals(lang) && s[0].equals("lang")) {
				return s[2];
			}
		}
		return null;
	}

	public void addTeasertext(String lang, String text) {
		((Vector<String[]>)values.elementAt(names.indexOf("teasertext"))).add(new String[]{"lang", lang, text});
	}

	public void removeTeasertext(int index) {
		((Vector<String[]>)values.elementAt(names.indexOf("teasertext"))).remove(index);
	}

	public void setPhysical_release_datetime(long physical_release_datetime) {
		set("physical_release_datetime", physical_release_datetime);
	}

	public long getPhysical_release_datetime() {
		return getLong("physical_release_datetime");
	}

	public void setDigital_release_datetime(long digital_release_datetime) {
		set("digital_release_datetime", digital_release_datetime);
	}

	public long getDigital_release_datetime() {
		return getLong("digital_release_datetime");
	}

	
	public void addRelatedInformation(BundleRelatedInformation info) {
		((Vector<BundleRelatedInformation>)values.elementAt(names.indexOf("related"))).add(info);
	}

	public void removeRelatedInformation(int index) {
		((Vector<String[]>)values.elementAt(names.indexOf("related"))).remove(index);
	}

	public Vector<BundleRelatedInformation> getRelatedInformation() {
		return (Vector<BundleRelatedInformation>)values.elementAt(names.indexOf("related"));
	}
	
	public Element toElement() {
		return toElement("information");
	}
	
	public Element toElement(String name) {
		Element e = new Element(name);
		Vector<String[]> t = (Vector<String[]>)getObject("promotext");
		for (String[] s  : t) {
			addWithAttrib(e, "promotext", s);
		}
		t = (Vector<String[]>)getObject("teasertext");
		for (String[] s  : t) {
			addWithAttrib(e, "teasertext", s);
		}
		addDate(e,"physical_release_datetime");
		addDate(e,"digital_release_datetime");
		Element e2 = new Element("related"); e.addContent(e2);
		Vector<BundleRelatedInformation> rel = (Vector<BundleRelatedInformation>)getObject("related");
		for (BundleRelatedInformation r  : rel) {
			Element erel = r.toElement();
			if (erel!=null) e2.addContent(erel);
		}
		return e;
	}

}
