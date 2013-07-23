package org.fnppl.opensdx.common;

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
 * @author Bertram Boedeker <bboedeker@gmx.de>, SP
 * 
 */

public class LicenseSpecifics extends BusinessObject {

	public static String KEY_NAME = "license_specifics";
	
	private BusinessCollection<LicenseRule> rules;  //MUST, if no rules -> empty
	private BusinessBooleanItem asOnBundle; //MUST if no rules are given
	
	
	private LicenseSpecifics() {
		
	}
	
	public static LicenseSpecifics make() {
		LicenseSpecifics b = new LicenseSpecifics();
		return b;
	}
	
	public static LicenseSpecifics makeAsOnBundle() {
		LicenseSpecifics b = new LicenseSpecifics();
		b.rules = null;
		b.asOnBundle = new BusinessBooleanItem("as_on_bundle", true);
		return b;
	}
	
	
	public static LicenseSpecifics fromBusinessObject(BusinessObject bo) {
		if (bo==null) return null;
		if (!bo.getKeyname().equals(KEY_NAME)) {
			BusinessObject boChild = bo.handleBusinessObject(KEY_NAME);
//			if (boChild == null) {
//				//check for empty field
//				XMLElementable other = bo.getOtherObject(KEY_NAME);
//				if (other != null) {
//					System.out.println("other object "+other.getClass().getSimpleName());
//					if (other instanceof BusinessStringItem) {
//						BusinessStringItem otherItem = (BusinessStringItem)other;
//						if (otherItem.getString()==null || otherItem.getString().length()==0) {
//							bo.removeObject(KEY_NAME);
//							return new LicenseSpecifics();
//						}
//					}
//				}
//			}
			bo = boChild;
		}
		if (bo==null) return null;
		final LicenseSpecifics b = new LicenseSpecifics();
		b.initFromBusinessObject(bo);
		
		b.rules = new BusinessCollection<LicenseRule>() {
			public String getKeyname() {
				return "rules";
			}
		};
		new ChildElementIterator(bo, "rules","rule") {
			public void processBusinessObject(BusinessObject bo) {
				b.addRule(LicenseRule.fromBusinessObject(bo));
			}
		};
		b.asOnBundle = BusinessBooleanItem.fromBusinessObject(b, "as_on_bundle");
		return b;
	}
	
	public LicenseSpecifics addRule(LicenseRule rule) {
		if (rules == null) {
			rules = new BusinessCollection<LicenseRule>() {
				public String getKeyname() {
					return "rules";
				}
			};
		}
		rules.add(rule);
		return this;
	}
	
	public BusinessCollection<LicenseRule> getRules() {
		return rules;
	}
	
	public LicenseRule getRule(int i) {
		if (rules != null && i < rules.size()) {
			return rules.get(i);
		}
		return null;
	}
	
	public int getRulesCount() {
		if (rules != null) {
			return rules.size();
		}
		return 0;
	}
	
	public String getKeyname() {
		return KEY_NAME;
	}
	
	public LicenseSpecifics as_on_bundle(boolean value) {
		if (value) {
			asOnBundle = new BusinessBooleanItem("as_on_bundle", value);
		} else {
			asOnBundle = null;
		}
		return this;
	}
	
	public boolean isAsOnBundle() {
		if (asOnBundle == null) {
			return false;
		}
		else {
			return asOnBundle.getBoolean();
		}
	}
}
