package org.fnppl.opensdx.dmi;


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



//import org.fnppl.opensdx.commonAuto.Information;

import org.fnppl.opensdx.common.*;
import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.xml.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;


public class FeedCreator {
	private ContractPartner sender;
	private ContractPartner licensor;
	private String creator_email;
	private String creator_userid;
	
	
	public FeedCreator(ContractPartner sender, ContractPartner licensor, String creator_email, String creator_userid) {
		this.sender = sender;
		this.licensor = licensor;
		this.creator_userid = creator_userid;
		this.creator_email = creator_email;
	}
	
	public static Feed makeExampleFeed() {

		ContractPartner sender
			= ContractPartner.make(
				ContractPartner.ROLE_SENDER,
				"contractpartnerid",
				"ourcontractpartnerid"
			)
			.email("sender@example.org");
		
		ContractPartner licensor
			= ContractPartner.make(
				ContractPartner.ROLE_LICENSOR,
				"contractpartnerid",
				"ourcontractpartnerid"
			)
			.email("licensor@example.org");
		
		String creator_email = "creator@example.org";
		String creator_userid = "creator_userid";
		
		FeedCreator fc = new FeedCreator(sender, licensor, creator_email, creator_userid);
		
		Feed feed = Feed.make(
				fc.makeExampleFeedInfo()
			)
			.addBundle(
				fc.makeExampleBundle()
			)
		;
		
		
		
		return feed;
	}
	
	public static Feed makeEmptyFeedWithBundle() {
		
		ContractPartner sender = ContractPartner.make(ContractPartner.ROLE_SENDER,"","");
		ContractPartner licensor = ContractPartner.make(ContractPartner.ROLE_LICENSOR,"","");
		ContractPartner licensee = ContractPartner.make(ContractPartner.ROLE_LICENSEE,"","");
		Receiver receiver = Receiver.make(Receiver.TRANSFER_TYPE_OSDX_FILESERVER)
//								.servername("localhost")
//								.serveripv4("127.0.0.1")
//								.authtype(Receiver.AUTH_TYPE_KEYFILE)
								;
		
		long now = System.currentTimeMillis();
		FeedInfo feedinfo = FeedInfo.make(true, "", now, now, sender, licensor, licensee)
									.creator(Creator.make("", "", null))
									.receiver(receiver);
		
		BundleInformation info = BundleInformation.make(now,now);
		LicenseBasis license_basis = LicenseBasis.make(Territorial.make(), now, now);
		LicenseSpecifics license_specifics = null;
		Bundle bundle = Bundle.make(IDs.make(), "","", "", "", info, license_basis, license_specifics);
		Feed feed = Feed.make(feedinfo)
			.addBundle(bundle);
		
		
		return feed;
	}
	
	private FeedInfo makeExampleFeedInfo() {
		
		boolean onlytest =true;
		String feedid = "example feedid";
		long creationdatetime = System.currentTimeMillis();
		long effectivedatetime = System.currentTimeMillis();
		ContractPartner licensee = ContractPartner.make(ContractPartner.ROLE_LICENSEE,"","");
		FeedInfo feedinfo
			= FeedInfo.make(
				onlytest,
				feedid,
				creationdatetime,
				effectivedatetime,
				sender,
				licensor,
				licensee
			)
			.creator(Creator.make(creator_email, creator_userid, null))
			.receiver(
				Receiver.make(Receiver.TRANSFER_TYPE_OSDX_FILESERVER)
						.servername("localhost")
						.serveripv4("127.0.0.1")
						.authtype(Receiver.AUTH_TYPE_KEYFILE)
			 	)
			.addAction(
				TriggeredActions.TRIGGER_ONINITIALRECEIVE,
				ActionHttp.make(
					"check.fnppl.org",
					"GET"
				)
			    .addHeader("header1", "value1")
			    .addHeader("header2", "value2")
			    .addParam("param1", "value3")
			)
			.addAction(
				TriggeredActions.TRIGGER_ONERROR,
				ActionMailTo.make(
					"receiver@alert.com",
					"subject",
					"text"
				)
			)
			;
		return feedinfo;
	}
	
	public Bundle makeExampleBundle() {
		Bundle bundle
		= Bundle.make(
			IDs.make()
				.amzn("amazon")
				.finetunes("fineid")
				.upc("a2312")
			,
			"displayname",
			"name",
			"version 1.0",
			"display artist",
			BundleInformation.make(
					System.currentTimeMillis(),
					System.currentTimeMillis()
				)
				.playlength(987)
				.texts(BundleTexts.make()
						.setPromotext("en", "EN promotext")
						.setPromotext("de", "DE promotetext")
						.setTeasertext("de", "DE teasertext")
				)
				.related(BundleRelatedInformation.make()
						.physical_distributor("published physical distributor")
						.physical_distributor("secret physical distributor", false)
						.youtube_url("my.youtube.url")
						.youtube_url("my.youtube.channel")
						.addRelatedBundleIDs(IDs.make()
								.licensor("our id")
								.licensee("your id")
						)
				)
			,
			LicenseBasis.make(
					Territorial.make(),
					System.currentTimeMillis(),
					System.currentTimeMillis()
				)
			,
			LicenseSpecifics.make()
		)
		.addContributor(
			Contributor.make(
				"super label",
				"label",
				IDs.make()
				   .labelordernum("123124124")
			)
			.www(InfoWWW.make()
				.homepage("super-label-homepage.nät")
				.phone("+49 44 9191919", false)
			)
		)
		;
		return bundle;
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

	
		
//		//test output
//		Element eFeedinfo = feedinfo.toElement();
//		
//		//test read document
//		FeedInfo feedinfo2 = FeedInfo.fromBusinessObject(BusinessObject.fromElement(eFeedinfo));
//		
//		Element eFeedinfo2 = feedinfo2.toElement();
//		
//		BusinessObject bo = BusinessObject.fromElement(eFeedinfo);
//		Element eFeedinfo3 = bo.toElement();
		
		
//		System.out.println("\n\nEXAMPLE FEEDINFO\n--------------------");
//		Document.buildDocument(eFeedinfo).output(System.out);
//		
//		System.out.println("\n\nRE-READ FEEDINFO\n--------------------");
//		Document.buildDocument(eFeedinfo2).output(System.out);
//		
//		System.out.println("\n\nRE-READ FEEDINFO with BusinessObject\n--------------------");
//		Document.buildDocument(eFeedinfo3).output(System.out);
//	
		
//		try {
//			System.out.println("feedinfo:                sha1: "+SecurityHelper.HexDecoder.encode(SecurityHelper.getSHA1LocalProof(eFeedinfo),'\0',-1));
//			System.out.println("feedinfo re-read         sha1: "+SecurityHelper.HexDecoder.encode(SecurityHelper.getSHA1LocalProof(eFeedinfo2),'\0',-1));
//			System.out.println("feedinfo business object sha1: "+SecurityHelper.HexDecoder.encode(SecurityHelper.getSHA1LocalProof(eFeedinfo3),'\0',-1));
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		
		
		
		
//		Element eBundle = bundle.toElement();
//		Element eBundle2 = Bundle.fromBusinessObject(BusinessObject.fromElement(eBundle)).toElement();
//		
//		System.out.println("\n\nEXAMPLE BUNDLE\n--------------------");
//		Document.buildDocument(eBundle).output(System.out);
//		
//		System.out.println("\n\nRE-READ BUNDLE\n--------------------");
//		Document.buildDocument(eBundle2).output(System.out);
//
//		try {
//			System.out.println("bundle                 sha1: "+SecurityHelper.HexDecoder.encode(SecurityHelper.getSHA1LocalProof(eBundle),'\0',-1));
//			System.out.println("bundle re-read         sha1: "+SecurityHelper.HexDecoder.encode(SecurityHelper.getSHA1LocalProof(eBundle2),'\0',-1));			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		
	}
}
