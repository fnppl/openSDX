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

import org.fnppl.opensdx.xml.Element;

public class Feedinfo extends BaseObjectWithConstraints {

	public Feedinfo() {
		names.add("onlytest"); values.add(null); constraints.add("MUST");
		names.add("feedid"); values.add(null); constraints.add("MUST");
		names.add("creationdatetime"); values.add(null); constraints.add("MUST");
		names.add("effectivedatetime"); values.add(null); constraints.add("MUST");
		names.add("creator_email"); values.add(null); constraints.add("?");
		names.add("creator_userid"); values.add(null); constraints.add("?");
		names.add("receiver"); values.add(null); constraints.add("[no comment]");
		names.add("sender"); values.add(null); constraints.add("MUST");
		names.add("licensor"); values.add(null); constraints.add("MUST");
		names.add("actions"); values.add(new Vector<Action>()); constraints.add("SHOULD");
	}

// methods
	public void setOnlytest(boolean onlytest) {
		set("onlytest", ""+onlytest);
	}

	public boolean getOnlytest() {
		return Boolean.parseBoolean(get("onlytest"));
	}

	public void setFeedid(String feedid) {
		set("feedid", feedid);
	}

	public String getFeedid() {
		return get("feedid");
	}

	public void setCreationdatetime(String creationdatetime) {
		set("creationdatetime", creationdatetime);
	}

	public String getCreationdatetime() {
		return get("creationdatetime");
	}

	public void setEffectivedatetime(String effectivedatetime) {
		set("effectivedatetime", effectivedatetime);
	}

	public String getEffectivedatetime() {
		return get("effectivedatetime");
	}

	public void setCreatorUserID(String userid) {
		set("creator_userid", userid);
	}

	public String getCreatorUserID() {
		return (String)getObject("creator_userid");
	}
	public void setCreatorEmail(String email) {
		set("creator_email", email);
	}

	public String getCreatorEmail() {
		return (String)getObject("creator_email");
	}

	public void setReceiver(Receiver receiver) {
		set("receiver", receiver);
	}

	public Receiver getReceiver() {
		return (Receiver)getObject("receiver");
	}

	public void setSender(ContractPartner sender) {
		set("sender", sender);
	}

	public ContractPartner getSender() {
		return (ContractPartner)getObject("sender");
	}

	public void setLicensor(ContractPartner licensor) {
		set("licensor", licensor);
	}

	public ContractPartner getLicensor() {
		return (ContractPartner)getObject("licensor");
	}

	public Vector<Action> getActions() {
		return (Vector<Action>)values.elementAt(names.indexOf("actions"));
	}
	public void addAction(Action action) {
		((Vector<Action>)values.elementAt(names.indexOf("actions"))).add(action);
	}

	public void removeAction(Action action) {
		((Vector<Action>)values.elementAt(names.indexOf("actions"))).remove(action);
	}
	
	public Element toElement() {
		return toElement("feedindo");
	}
	
	public Element toElement(String name) {
		Element e = new Element(name);
		add(e,"onlytest");
		add(e,"feedid");
		add(e,"creationdatetime");
		add(e,"effectivedatetime");
		Element e2 = new Element("creator"); e.addContent(e2);
		add(e2,"creator_email","email");
		add(e2,"creator_userid", "userid");
		addElement(e, "receiver","receiver");
		addElement(e, "sender","sender");
		addElement(e, "licensor","licensor");
		
		Vector<Action> actions = (Vector<Action>)getObject("actions");
		if (actions!=null && actions.size()>0) {
			Element e3 = new Element("actions"); e.addContent(e3);
			for (int i=1;i<=5;i++) {
				String type = Action.actionTypeName[i];
				Element et = new Element(type);
				for (Action a : actions) {
					if (a.getActionType()==i) {
						et.addContent(a.toElement());
					}
				}
				e3.addContent(et);
			}
		}
		return e;
	}
	
	private void addElement(Element e, String name, String newName) {
		Object b = getObject(name);
		if (b!=null) {
			e.addContent(((BaseObject)b).toElement(newName));
		}
	}
	private void add(Element e, String name) {
		String s = get(name);
		if (s!=null)
			e.addContent(name, s);
	}
	private void add(Element e, String name, String newName) {
		String s = get(name);
		if (s!=null)
			e.addContent(newName, s);
	}

}
