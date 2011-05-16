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
	ContractPartner sender;
	ContractPartner licensor;
	BusinessObject creator;
	
	public Feed makeExampleFeed() {
		return null;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// -- test Feedinfo --
		boolean onlytest =true;
		String feedid = "example feedid";
		long creationdatetime = System.currentTimeMillis();
		long effectivedatetime = System.currentTimeMillis();

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
		
		
		FeedInfo feedinfo
			= FeedInfo.make(
				onlytest,
				feedid,
				creationdatetime,
				effectivedatetime,
				sender,
				licensor
			)
			.creator("creator@example.org", "creator_userid")
			.receiver(
				Receiver.make(
					Receiver.TRANSFER_TYPE_FTP,
					"it-is-awesome.de",
					"127.0.0.1",
					Receiver.AUTH_TYPE_LOGIN,
					SecurityHelper.getSHA1("LOGIN".getBytes()))
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
		
		
		Bundle bundle
		= Bundle.make(
			IDs.make()
				.amzn("amazon")
				.finetunesid("fineid")
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
				.addPromotext("en", "EN promotext")
				.addPromotext("de", "DE promotetext")
				.addTeasertext("de", "DE teasertext")
				.related(BundleRelatedInformation.make()
						.physical_distributor("published physical distributor")
						.physical_distributor("secret physical distributor", false)
						.youtube_url("my.youtube.url")
						.youtube_url("my.youtube.channel")
						.addRelatedBundleIDs(IDs.make()
								.ourid("our id")
								.yourid("your id")
						)
				)
			,
			"license_basis",
			"license_specifics"
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
		
		
		Element eBundle = bundle.toElement();
		Element eBundle2 = Bundle.fromBusinessObject(BusinessObject.fromElement(eBundle)).toElement();
		
		System.out.println("\n\nEXAMPLE BUNDLE\n--------------------");
		Document.buildDocument(eBundle).output(System.out);
		
		System.out.println("\n\nRE-READ BUNDLE\n--------------------");
		Document.buildDocument(eBundle2).output(System.out);

		try {
			System.out.println("bundle                 sha1: "+SecurityHelper.HexDecoder.encode(SecurityHelper.getSHA1LocalProof(eBundle),'\0',-1));
			System.out.println("bundle re-read         sha1: "+SecurityHelper.HexDecoder.encode(SecurityHelper.getSHA1LocalProof(eBundle2),'\0',-1));			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
}
