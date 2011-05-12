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

import org.fnppl.opensdx.xml.ChildElementIterator;
import org.fnppl.opensdx.xml.Element;
import org.fnppl.opensdx.xml.XMLElementable;

/**
 * 
 * @author Bertram Boedeker <bboedeker@gmx.de>
 * 
 */
@SuppressWarnings("unchecked")
public class TriggeredActions implements XMLElementable {

	public static String KEY_NAME = "actions";
	
	public static int TRIGGER_ONINITIALRECEIVE = 1;
	public static int TRIGGER_ONPROCESSSTART   = 2;
	public static int TRIGGER_ONPROCESSEND     = 3;
	public static int TRIGGER_ONFULLSUCCESS    = 4;
	public static int TRIGGER_ONERROR          = 5;
	
	Vector<TriggeredAction> actions;
	
	
	public TriggeredActions() {
		actions = new Vector<TriggeredAction>();
	}
	public int actionType = 0;
	public static String[] actionTriggerName = new String[] {
		"[TRIGGER NOT SET]", "oninitialreceive", "onprocessstart", "onprocessend", "onfullsuccess", "onerror"
	};
	
	public static TriggeredActions fromBusinessObject(BusinessObject bo)  {
		if (bo==null) return null;
		if (!bo.getKeyname().equals(KEY_NAME)) {
			bo = bo.handleBusinessObject(KEY_NAME);
		}
		if (bo==null) return null;
		
		final TriggeredActions ta = new TriggeredActions();
		for (int i=1;i<=5;i++) {
			final int triggerNo = i;
			new ChildElementIterator(bo, actionTriggerName[triggerNo]) {
				public void processBusinessObject(BusinessObject bo) {
					if (bo.getKeyname().equals(ActionHttp.KEY_NAME)) {
						ActionHttp action = ActionHttp.fromBusinessObject(bo);
						ta.addAction(triggerNo, action);
					}
					else if (bo.getKeyname().equals(ActionMailTo.KEY_NAME)) {
						ActionMailTo action = ActionMailTo.fromBusinessObject(bo);
						ta.addAction(triggerNo, action);
					}
				}
			};
		}
		return ta;
	}

	public void addAction(int trigger, Action action) {
		if (trigger<0 || trigger>5) throw new RuntimeException("wrong trigger");
		actions.add(new TriggeredAction(trigger,action));
	}
	
	public int getTrigger(int actionNo) {
		return actions.get(actionNo).trigger;
	}
	
	public Action getAction(int actionNo) {
		return actions.get(actionNo).action;
	}

	public Element toElement() {
		if (actions==null || actions.size()==0) return null;
		Element e3 = new Element(KEY_NAME);
		for (int i=1;i<=5;i++) {
			String type = actionTriggerName[i];
			Element et = new Element(type);
			for (TriggeredAction a : actions) {
				if (a.trigger==i) {
					et.addContent(a.action.toElement());
				}
			}
			e3.addContent(et);
		}
		return e3;
	}

	public String getKeyname() {
		return KEY_NAME;
	}

	private class TriggeredAction {
		public int trigger;
		public Action action;
		public TriggeredAction(int t, Action a) {
			trigger = t;
			action = a;
		}
	}
	
}
