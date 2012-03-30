package org.fnppl.opensdx.file_transfer.trigger;
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

import java.io.File;
import java.util.Vector;

import org.fnppl.opensdx.xml.Element;
import org.fnppl.opensdx.xml.Document;


public class TriggerCollection {

	private Vector<Trigger> triggers = new Vector<Trigger>();
	
	
	public TriggerCollection() {}
	
	public void addTrigger(Trigger trigger) {
		triggers.add(trigger);
	}
	
	public void removeTriggersForEvent(String event) {
		Vector<Trigger> remove = new Vector<Trigger>();
		for (Trigger t : triggers) {
			if (t.getEventType().equals(event)) {
				remove.add(t);
			}
		}
		triggers.removeAll(remove);
	}
	
	public void triggerEvent(String event) { 
		for (Trigger t : triggers) {
			if (t.getEventType().equals(event)) {
				t.doAction();
			}
		}
	}
	
	//use this for copying the system triggers
	public TriggerCollection getCopy() {
		TriggerCollection c = new TriggerCollection();
		c.triggers.addAll(triggers);
		return c;
	}
	
	public String toString() {
		StringBuffer b = new StringBuffer();
		for (Trigger t : triggers) {
			b.append(t.toString()+"\n\n");
		}
		return b.toString();
	}
	
	
	//TEST
	public static void main(String[] args) {
		try {
			TriggerCollection col = new TriggerCollection();
			Element config = Document.fromFile(new File("src/org/fnppl/opensdx/file_transfer/resources/osdxfiletransferserver_config.xml")).getRootElement();
			Element eTriggers = config.getChild("osdxfiletransferserver").getChild("triggers");
			Vector<Element> triggers = eTriggers.getChildren("trigger");
			for (Element e : triggers) {
				col.addTrigger(Trigger.fromElement(e));
			}
			System.out.println(col.toString());
			
			col.triggerEvent(Trigger.TRIGGER_MKDIR);
			col.triggerEvent(Trigger.TRIGGER_LOGIN);
			col.triggerEvent(Trigger.TRIGGER_LOGOUT);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
}
