package org.fnppl.opensdx.common;

import java.util.Vector;

import org.fnppl.opensdx.xml.Element;


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
 * a bundle combines one to many Items
 * 
 */

public class Bundle extends BaseObjectWithConstraints {

	public Bundle() {
		names.add("ids"); values.add(null); constraints.add("MUST");
		names.add("displayname"); values.add(null); constraints.add("MUST");
		names.add("name"); values.add(null); constraints.add("MUST");
		names.add("version"); values.add(null); constraints.add("MUST");
		names.add("display_artist"); values.add(null); constraints.add("SHOULD");
		names.add("contributors"); values.add(new Vector<Contributor>()); constraints.add("MUST");
		names.add("information"); values.add(null); constraints.add("[no comment]");
		names.add("territorial"); values.add(new Vector<Territory>()); constraints.add("MUST");
		names.add("from"); values.add(null); constraints.add("MUST");
		names.add("until"); values.add(null); constraints.add("MUST");
		names.add("pricecode"); values.add(null); constraints.add("COULD");
		names.add("wholesale"); values.add(null); constraints.add("COULD");
		names.add("license_rules"); values.add(new Vector<LicenseRule>()); constraints.add("COULD");
		names.add("items"); values.add(new Vector<Item>()); constraints.add("[no comment]");
	}

//// methods
	public void setIDs(BundleIDs ids) {
		set("ids", ids);
	}

	public BundleIDs getIDs() {
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

	public void setDisplay_artist(String display_artist) {
		set("display_artist", display_artist);
	}

	public String getDisplay_artist() {
		return get("display_artist");
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

	public void addTerritory(Territory t) {
		((Vector<Territory>)values.elementAt(names.indexOf("territorial"))).add(t);
	}

	public void removeTerritory(int index) {
		((Vector<Territory>)values.elementAt(names.indexOf("territorial"))).remove(index);
	}

	public Vector<Territory> getTerritory() {
		return (Vector<Territory>)values.elementAt(names.indexOf("territorial"));
	}

	public void setInformation(BundleInformation information) {
		set("information", information);
	}

	public BundleInformation getInformation() {
		return (BundleInformation)getObject("information");
	}
	
	public void setTimeframeFrom(long from) {
		set("from", from);
	}

	public long getTimeframeFrom() {
		return getLong("from");
	}

	public void setTimeFrameUntil(long to) {
		set("until", to);
	}

	public long getTimeFrameUntil() {
		return getLong("until");
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

	public void addItem(Item i) {
		((Vector<Item>)values.elementAt(names.indexOf("items"))).add(i);
	}

	public void removeItem(int index) {
		((Vector<Item>)values.elementAt(names.indexOf("items"))).remove(index);
	}

	public Vector<Item> getItem() {
		return (Vector<Item>)values.elementAt(names.indexOf("items"));
	}
	
	public Element toElement() {
		return toElement("bundle");
	}
	
	public Element toElement(String name) {
		Element e = new Element(name);
		addElement(e, "ids", "ids");
		
		add(e,"displayname");
		add(e,"name");
		add(e,"version");
		add(e,"display_artist");
		Vector<Contributor> cont = (Vector<Contributor>)getObject("contributors");
		Element ec = new Element("contributors"); e.addContent(ec);
		for (Contributor c : cont) {
			ec.addContent(c.toElement());
		}
		addElement(e,"information","information");
		
		Element e2 = new Element("license_basis"); e.addContent(e2);
		Vector<Territory> terr = (Vector<Territory>)getObject("territorial"); 
		Element et = new Element("territorial"); e2.addContent(et);
		for (Territory t : terr) {
			et.addContent(t.toElement());
		}
		Element etf = new Element("timeframe"); e2.addContent(etf);
		addDate(etf,"from");
		addDate(etf,"until");

		Element ep = new Element("pricing"); e2.addContent(ep);
		add(ep,"pricecode");
		add(ep,"wholesale");
		
		Element e3 = new Element("license_specifics"); e.addContent(e3);
		Vector<LicenseRule> rules = (Vector<LicenseRule>)getObject("license_rules");
		for (LicenseRule r : rules) {
			e3.addContent(r.toElement());
		}
		Element e4 = new Element("items"); e.addContent(e4);
		Vector<Item> items = (Vector<Item>)getObject("items");
		for (Item i : items) {
			e4.addContent(i.toElement());
		}
		return e;
	}
	
}