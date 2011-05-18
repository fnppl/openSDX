package org.fnppl.opensdx.common;

import java.util.Vector;

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

public class LicenseBasis extends BusinessObject {

	public static String KEY_NAME = "license_basis";
	
	private Territorial territorial;  							 //MUST
	private BusinessCollection<BusinessDatetimeItem> timeframe;	 //MUST
	private BusinessObject pricing;								 //SHOULD
	
	private LicenseBasis() {
		
	}
	
	public static LicenseBasis make(Territorial territorial, long from, long to) {
		LicenseBasis b = new LicenseBasis();
		b.territorial = territorial;
		b.timeframe = new BusinessCollection<BusinessDatetimeItem>() {
			public String getKeyname() {
				return "timeframe";
			}
		};
		b.timeframe.add(new BusinessDatetimeItem("from", from));
		b.timeframe.add(new BusinessDatetimeItem("to", to));
		b.pricing = null;
		return b;
	}
	
	public static LicenseBasis fromBusinessObject(BusinessObject bo) {
		if (bo==null) return null;
		if (!bo.getKeyname().equals(KEY_NAME)) {
			bo = bo.handleBusinessObject(KEY_NAME);
		}
		if (bo==null) return null;
		final LicenseBasis b = new LicenseBasis();
		b.initFromBusinessObject(bo);
		
		b.territorial = null; //TODO
		b.timeframe = new BusinessCollection<BusinessDatetimeItem>() {
			public String getKeyname() {
				return "timeframe";
			}
		};
		BusinessObject boTimeFrame = bo.handleBusinessObject("timeframe");
		if (boTimeFrame!=null) {
			BusinessDatetimeItem from = BusinessDatetimeItem.fromBusinessObject(boTimeFrame, "from");
			if (from!=null) b.timeframe.add(from);
			BusinessDatetimeItem to = BusinessDatetimeItem.fromBusinessObject(boTimeFrame, "to");
			if (to!=null) b.timeframe.add(to);
		}
		b.pricing = bo.handleBusinessObject("pricing");
		return b;
	}
	
	public LicenseBasis pricing_pricecode(String pricecode) {
		if (pricing==null) {
			pricing = new BusinessObject() {
				public String getKeyname() {
					return "pricing";
				}
			};
		}
		pricing.setObject(new BusinessStringItem("pricecode", pricecode));
		return this;
	}

	public LicenseBasis pricing_wholesale(String wholesale) {
		if (pricing==null) {
			pricing = new BusinessObject() {
				public String getKeyname() {
					return "pricing";
				}
			};
		}
		pricing.setObject(new BusinessStringItem("wholesale", wholesale));
		return this;
	}
	
	public String getKeyname() {
		return KEY_NAME;
	}
	
	
	
}
