package org.fnppl.opensdx.common;

import java.util.Vector;

import org.fnppl.opensdx.xml.Element;

/*
 * Copyright (C) 2010-2012 
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
	
	private Vector<BusinessStringItem> receiver;
	private BusinessStringItem subject;
	private BusinessStringItem text;
	
	private ActionMailTo() {
		
	}
	
	public static ActionMailTo make(String receiver, String subject, String text) {
		ActionMailTo a = new ActionMailTo();
		a.receiver = new Vector<BusinessStringItem>();
		a.receiver(receiver);
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
		a.receiver = new Vector<BusinessStringItem>();
		BusinessStringItem rec = a.handleBusinessStringItem("receiver");
		while (rec!=null) {
			a.receiver.add(rec);
			rec = a.handleBusinessStringItem("receiver");
		}
		a.subject  = a.handleBusinessStringItem("subject");
		a.text     = a.handleBusinessStringItem("text");
		a.removeOtherObjects();
		return a;
	}
	
	public void execute() {
		//TODO implement
	}
	
	public ActionMailTo receiver(String text) {
		if (text==null) {
			receiver.removeAllElements();
		} else {
			//System.out.println("receiver::"+text);
			String[] rec = text.trim().split("[,; ]");
			for (String r : rec) {
				if (r.length()>1) {
					//System.out.println("receiver: "+r);
					receiver.add(new BusinessStringItem("receiver", r));
				}
			}
		}
		return this;
	}
	
	public ActionMailTo addReceiver(String text) {
		if (text!=null) {
			String[] rec = text.trim().split("[,; ]");
			for (String r : rec) {
				if (r.length()>1) {
					receiver.add(new BusinessStringItem("receiver", r));
				}
			}
		}
		return this;
	}
	
	public String getReceiver(int no) {
		if (no>=0 && no<receiver.size()) {
			return receiver.get(no).getString();
		}
		return null;
	}
	
	public int getReceiverCount() {
		return receiver.size();
	}
	
	public ActionMailTo subject(String text) {
		if (text==null) {
			subject = null;
		} else {
			subject.setString(text);
		}
		return this;
	}
	public ActionMailTo text(String text) {
		if (text==null) {
			this.text = null;
		} else {
			this.text.setString(text);
		}
		return this;
	}
	
	public String getReceiver() {
		if (receiver==null || receiver.size()==0) return null;
		String rec = receiver.get(0).getString();
		for (int i=1;i<receiver.size();i++) {
			rec += "; "+receiver.get(i).getString();
		}
		return rec;
	}
	
	public String getSubject() {
		if (subject==null) return null;
		return subject.getString();
	}
	
	public String getText() {
		if (text==null) return null;
		return text.getString();
	}

	public String getDescription() {
		return getReceiver();
	}
	
	public String getKeyname() {
		return KEY_NAME;
	}


}
