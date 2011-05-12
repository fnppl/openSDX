package org.fnppl.opensdx.common;

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



/**
 * 
 * @author Bertram Boedeker <bboedeker@gmx.de>
 * 
 */
public class ActionMailTo extends BusinessObject implements Action {

	public static String KEY_NAME = "mailto";
	
	private BusinessStringItem receiver;
	private BusinessStringItem subject;
	private BusinessStringItem text;
	
	private ActionMailTo() {
		
	}
	
	public static ActionMailTo make(String receiver, String subject, String text) {
		ActionMailTo a = new ActionMailTo();
		a.receiver = new BusinessStringItem("receiver", receiver);
		a.subject = new BusinessStringItem("subject", subject);
		a.text = new BusinessStringItem("text", text);
		return a;
	}
	
	public static ActionMailTo fromBusinessObject(BusinessObject bo) {
		if (bo==null) return null;
		if (!bo.getKeyname().equals(KEY_NAME)) {
			bo = bo.handleBusinessObject(KEY_NAME);
		}
		if (bo==null) return null;
		
		ActionMailTo a = new ActionMailTo();
		a.initFromBusinessObject(bo);
		a.receiver = a.handleBusinessStringItem("receiver");
		a.subject  = a.handleBusinessStringItem("subject");
		a.text     = a.handleBusinessStringItem("text");
		a.removeOtherObjects();
		return a;
	}
	
	public void execute() {
		//TODO implement
	}

	
	public String getKeyname() {
		return KEY_NAME;
	}


}
