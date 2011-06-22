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
public class Territorial extends BusinessObject {

	public static String KEY_NAME = "terrtorial";
	public Vector<BusinessStringItem> territories = new Vector<BusinessStringItem>();
	

	private Territorial() {
		
	}
	
	public static Territorial make() {
		Territorial t = new Territorial();
		
		return t;
	}
	
	public static Territorial fromBusinessObject(BusinessObject bo) {
		if (bo==null) return null;
		if (!bo.getKeyname().equals(KEY_NAME)) {
			bo = bo.handleBusinessObject(KEY_NAME);
		}
		if (bo==null) return null;
		final Territorial t = new Territorial();
		t.initFromBusinessObject(bo);
		
		new ChildElementIterator(bo, "promotext") {
			public void processBusinessStringItem(BusinessStringItem item) {
				t.territories.add(item);			}
		};		
		return t;
	}
	
	public void allow(String territory) {
		BusinessStringItem item = new BusinessStringItem("territory", territory);
		item.setAttribute("type", "allow");
		territories.add(item);
	}
	
	public void disallow(String territory) {
		BusinessStringItem item = new BusinessStringItem("territory", territory);
		item.setAttribute("type", "disallow");
		territories.add(item);
	}
	
	public void remove(String territory) {
		for (int i=0;i<territories.size();i++) {
			if (territories.get(i).getString().equals(territory)) {
				territories.remove(i);
				i--;
			}
		}
	}
	
	

	public int getTerritorialCount() {
		return territories.size();
	}
	
	public String getTerritory(int index) {
		if (index<0 || index >= territories.size()) return null;
		return territories.get(index).getString();
	}
	
	public boolean isTerritoryAllowed(int index) {
		if (index<0 || index >= territories.size()) return false;
		return territories.get(index).getAttribute("type").equals("allow");
	}
		
	public String getKeyname() {
		return KEY_NAME;
	}
	
	
}
