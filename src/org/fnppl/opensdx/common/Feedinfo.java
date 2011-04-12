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
		return (Vector<Action>)values.elementAt(names.indexOf("action"));
	}
	public void addAction(Action action) {
		((Vector<Action>)values.elementAt(names.indexOf("action"))).add(action);
	}

	public void removeAction(Action action) {
		((Vector<Action>)values.elementAt(names.indexOf("action"))).remove(action);
	}

}
