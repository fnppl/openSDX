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


import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;

/**
 * 
 * @author Bertram Boedeker <bboedeker@gmx.de>
 * 
 */
public class FeedInfo extends BusinessObject {

	public static String KEY_NAME = "feedinfo";
	
	private BusinessBooleanItem onlytest;					//MUST
	private BusinessStringItem feedid;						//MUST
	private BusinessDatetimeItem creationdatetime;			//MUST
	private BusinessDatetimeItem effectivedatetime; 		//MUST
	private BusinessObject creator; 						//COULD
	private Receiver receiver;								//TODO COULD or what?
	private ContractPartner sender;							//MUST
	private ContractPartner licensor;						//MUST
	private TriggeredActions actions;						//SHOULD
		
	private FeedInfo() { 
		
	}
	
	/***
	 * the make method constructs a FeedInfo object with the following MUST have attributes 
	 * @param onlytest :: testmode true|false
	 * @param feedid :: id is provided by sender; should be unique
	 * @param creationdatetime :: datetime of creation of this feed
	 * @param effectivedatetime :: most probably the same as creationdate ; datetime when this feed may become ACTIVE as earliest; mainly used for deferred update-feeds with an effective-date not *asap*
	 * @param sender :: sender of this feed
	 * @param licensor :: could be identical to sender, but has to be given
	 * @return newly instantiated FeedInfo
	 * 
	 * OPTIONAL fields in FeedInfo:
	 * 	COULD:  creator :: see creator(String email, String userid)
	 * 	COULD: 	receiver:: see receiver(Receiver receiver)
	 *  SHOULD: actions :: see addAction(int trigger, Action action)
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
		f.actions = null;
		return f;
	}
	
	public static FeedInfo fromBusinessObject(BusinessObject bo) {
		if (bo==null) return null;
		if (!bo.getKeyname().equals(KEY_NAME)) {
			bo = bo.handleBusinessObject(KEY_NAME);
		}
		if (bo==null) return null;
		
		FeedInfo f = new FeedInfo();
		f.initFromBusinessObject(bo);
		
		try {
			f.onlytest = BusinessBooleanItem.fromBusinessObject(f,"onlytest");
			f.feedid =   BusinessStringItem.fromBusinessObject(f,"feedid");
			f.creationdatetime =  BusinessDatetimeItem.fromBusinessObject(f,"creationdatetime");
			f.effectivedatetime = BusinessDatetimeItem.fromBusinessObject(f,"effectivedatetime");
			
			f.creator  = f.handleBusinessObject("creator");
			f.receiver = Receiver.fromBusinessObject(f);
			f.sender   = ContractPartner.fromBusinessObject(f, ContractPartner.ROLE_SENDER);
			f.licensor = ContractPartner.fromBusinessObject(f, ContractPartner.ROLE_LICENSOR);
			f.actions  = TriggeredActions.fromBusinessObject(f);
			return f;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
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
		BusinessStringItem s = creator.getBusinessStringItem("email");
		if (s==null) return null;
		return s.getString();
	}
	
	public String getCreatorUserID() {
		if (creator==null) return null;
		BusinessStringItem s = creator.getBusinessStringItem("userid");
		if (s==null) return null;
		return s.getString();
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
			creator = new BusinessObject() {
				public String getKeyname() {
					return "creator";
				}
			};
		};
		creator.setObject(new BusinessStringItem("email", email));
		creator.setObject(new BusinessStringItem("userid", userid));
		return this;
	}
	
	public FeedInfo receiver(Receiver receiver) {
		this.receiver = receiver;
		return this;
	}
	
	private static BusinessCollection<BusinessStringItem> makeCreator(String email, String userid) {
		BusinessCollection<BusinessStringItem> creator = new BusinessCollection<BusinessStringItem>() {
			public String getKeyname() {
				return "creator";
			}
		};
		creator.add(new BusinessStringItem("email", email));
		creator.add(new BusinessStringItem("userid", userid));
		return creator;
	}
	
	
	public String getKeyname() {
		return KEY_NAME;
	}
	

}
