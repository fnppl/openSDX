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

import org.fnppl.opensdx.xml.Document;

/**
 * 
 * @author Bertram Boedeker <bboedeker@gmx.de>
 * 
 */
public class FeedInfo extends BusinessObject {

	private BusinessBooleanItem onlytest;					//MUST
	private BusinessStringItem feedid;						//MUST
	private BusinessDatetimeItem creationdatetime;			//MUST
	private BusinessDatetimeItem effectivedatetime; 		//MUST
	private BusinessCollection<BusinessStringItem> creator; //COULD
	private Receiver receiver;								//TODO COULD or what?
	private ContractPartner sender;							//MUST
	private ContractPartner licensor;						//MUST
	private TriggeredActions actions;						//SHOULD
	
	private FeedInfo() { 
		
	}
	
	/***
	 * the makeFeedInfo method constructs a FeedInfo object with the following MUST have attributes 
	 * @param onlytest :: testmode true|false
	 * @param feedid :: id is provided by sender; should be unique
	 * @param creationdatetime :: datetime of creation of this feed
	 * @param effectivedatetime :: most probably the same as creationdate ; datetime when this feed may become ACTIVE as earliest; mainly used for deferred update-feeds with an effective-date not *asap*
	 * @param sender :: sender of this feed
	 * @param licensor :: could be identical to sender, but has to be given
	 * @return newly instantiated FeedInfo
	 */
	public static FeedInfo make(boolean onlytest, String feedid, long creationdatetime, long effectivedatetime, ContractPartner sender, ContractPartner licensor) {
		FeedInfo f = new FeedInfo();
		f.onlytest = new BusinessBooleanItem("onlytest", onlytest);
		f.feedid = new BusinessStringItem("feedid", feedid);
		f.creationdatetime = new BusinessDatetimeItem("creationdatetime", creationdatetime);
		f.effectivedatetime = new BusinessDatetimeItem("effectivedatetime", effectivedatetime);
		f.creator = null;
		f.receiver = null;
		f.sender = sender;
		f.licensor = licensor;
		return f;
	}
	
	public boolean getOnlyTest() {
		return onlytest.getBoolean();
	}
	
	public String getFeedID() {
		return feedid.getString();
	}
	
	public long getCreationDatetime() {
		return creationdatetime.getDatetime();
	}
	
	public String getCreationDatetimeString() {
		return creationdatetime.getDatetimeStringGMT();
	}
	
	public long getEffectiveDatetime() {
		return effectivedatetime.getDatetime();
	}
	
	public String getEffectiveDatetimeString() {
		return effectivedatetime.getDatetimeStringGMT();
	}
	
	public String getCreatorEmail() {
		if (creator==null) return null;
		return creator.get(0).getString();
	}
	
	public String getCreatorUserID() {
		if (creator==null) return null;
		return creator.get(1).getString();
	}
	
	public Receiver getReceiver() {
		return receiver;
	}

	public ContractPartner getSender() {
		return sender;
	}

	public ContractPartner getLicensor() {
		return licensor;
	}

	public FeedInfo addAction(int trigger, Action action) {
		if (actions==null) {
			actions = new TriggeredActions();
		}
		actions.addAction(trigger, action);
		return this;
	}
	
	public FeedInfo creator(String email, String userid) {
		if (creator==null) {
			creator = new BusinessCollection<BusinessStringItem>() {
				public String getKeyname() {
					return "creator";
				}
			};
			creator.add(new BusinessStringItem("email", email));
			creator.add(new BusinessStringItem("userid", userid));
		} else {
			creator.get(0).setString(email);
			creator.get(1).setString(userid);
		}
		return this;
	}
	
	
	public String getKeyname() {
		return "feedinfo";
	}
	
	
	public static void main(String[] args) {
		
		// -- test Feedinfo --
		boolean onlytest =true;
		String feedid = "example feedid";
		long creationdatetime = System.currentTimeMillis();
		long effectivedatetime = System.currentTimeMillis();
		String creatorEmail = "creator@example.org";
		String creatorUserID = "creator";
		
		ContractPartner sender = ContractPartner.make(ContractPartner.ROLE_SENDER, "contractpartnerid","ourcontractpartnerid")
						.email("sender@example.org");
		ContractPartner licensor = ContractPartner.make(ContractPartner.ROLE_LICENSOR, "contractpartnerid","ourcontractpartnerid")
						.email("licensor@example.org");
		
		
		FeedInfo feedinfo = FeedInfo.make(onlytest, feedid, creationdatetime, effectivedatetime, sender, licensor)
									.creator("creator@example.org", "creator_userid")
									.addAction(
											TriggeredActions.TRIGGER_ONINITIALRECEIVE,
											ActionHttp.make(
													"check.fnppl.org",
													"GET")
											        .addHeader("header1", "value1")
											        .addHeader("header2", "value2")
											        .addParam("param1", "value3"))
									.addAction(
											TriggeredActions.TRIGGER_ONERROR,
											ActionMailTo.make(
													"receiver@alert.com",
													"subject",
													"text"));
		
		Document.buildDocument(feedinfo.toElement()).output(System.out);
	}

}
