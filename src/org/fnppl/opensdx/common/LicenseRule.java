package org.fnppl.opensdx.common;

import java.util.Vector;

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

public class LicenseRule extends BusinessObject {

	public static String KEY_NAME = "rule";
	
	private BusinessObject rule_if;
	private BusinessObject rule_then;
	private BusinessObject rule_else;
	
	
	private LicenseRule() {
		
	}
	
	public static LicenseRule make(String rule_if, String rule_then, String rule_else) {
		LicenseRule b = new LicenseRule();
		
		b.rule_if = new BusinessObject() {
			public String getKeyname() {
				return "if";
			}
		};
		b.rule_then = new BusinessObject() {
			public String getKeyname() {
				return "then";
			}
		};
		b.rule_else = null;
		return b;
	}
	
	public static LicenseRule fromBusinessObject(BusinessObject bo) {
		if (bo==null) return null;
		if (!bo.getKeyname().equals(KEY_NAME)) {
			bo = bo.handleBusinessObject(KEY_NAME);
		}
		if (bo==null) return null;
		final LicenseRule b = new LicenseRule();
		b.initFromBusinessObject(bo);
		
		//TODO
		
		return b;
	}
	
	public LicenseRule set_if(String what, String operator, String value) {
		rule_if.setObject(new BusinessStringItem("what", what));
		rule_if.setObject(new BusinessStringItem("operator", operator));
		rule_if.setObject(new BusinessStringItem("value", value));
		return this;
	}
	
	public LicenseRule set_then(String rule_then) {
		//TODO rule_then.setObject(new BusinessStringItem("what", what));
		return this;
	}
	
	public LicenseRule set_else(String rule_else) {
		//TODO rule_else.setObject(new BusinessStringItem("what", what));
		return this;
	}
	public String getKeyname() {
		return KEY_NAME;
	}
	
	
	
}
