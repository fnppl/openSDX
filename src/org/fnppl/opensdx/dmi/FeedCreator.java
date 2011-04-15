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



import org.fnppl.opensdx.common.Action;
import org.fnppl.opensdx.common.ActionHttp;
import org.fnppl.opensdx.common.ActionMailTo;
import org.fnppl.opensdx.common.Bundle;
import org.fnppl.opensdx.common.BundleIDs;
import org.fnppl.opensdx.common.BundleInformation;
import org.fnppl.opensdx.common.BundleRelatedInformation;
import org.fnppl.opensdx.common.ContractPartner;
import org.fnppl.opensdx.common.Contributor;
import org.fnppl.opensdx.common.Feedinfo;
import org.fnppl.opensdx.common.Feed;
import org.fnppl.opensdx.common.Item;
import org.fnppl.opensdx.common.LicenseRule;
import org.fnppl.opensdx.common.Receiver;
import org.fnppl.opensdx.common.Territory;
import org.fnppl.opensdx.commonAuto.Information;
import org.fnppl.opensdx.xml.*;
import java.io.*;import java.text.SimpleDateFormat;
import java.util.*;

import javax.print.Doc;

public class FeedCreator {

	
	public static Item makeItem(String type, BundleIDs ids, String displayname, String name, String version, Vector<Contributor> contributors,
			BundleInformation information, Vector<Territory> territorial, long from, long until, String pricecode, String wholesale,
			Vector<String> genres, String origin_country, String main_language, boolean bundle_only, boolean streaming_allowed, Vector<MediaFile> files) {
		Item i = new Item();
		i.setType(type);
		i.setIds(ids);
		i.setDisplayname(displayname);
		i.setName(name);
		i.setVersion(version);
		if (contributors!=null) {
			for (Contributor c : contributors) {
				i.addContributor(c);
			}
		}
		i.setInformation(information);
		if (territorial != null) {
			for (Territory t : territorial) {
				i.addTerritory(t);
			}
		}
		i.setTimeframeFrom(from);
		i.setTimeFrameUntil(until);
		
		i.setPricecode(pricecode);
		i.setWholesale(wholesale);
		if (genres!=null) {
			for (String s : genres) {
				i.addGenres(s);
			}
		}
		i.setOrigin_country(origin_country);
		i.setMain_language(main_language);
		i.setBundle_only(bundle_only);
		i.setStreaming_allowed(streaming_allowed);
		
		if (files!=null) {
			for (MediaFile f : files) {
				i.addMediaFile(f);
			}
		}
		return i;
	}
	
	public static Receiver makeReceiver(String typ, String servername, String ipv4, String ipv6, String authtype, String authsha1, String cryptoEmail, String cryptoKeyID, String cryptoPubkey) {
		Receiver receiver = new Receiver();
		receiver.setType(typ);
		receiver.setServername(servername);
		receiver.setServeripv4(ipv4);
		receiver.setServeripv6(ipv6);
		receiver.setAuthtype(authtype);
		receiver.setAuthsha1(authsha1);
		receiver.setCryptoRelatedEmail(cryptoEmail);
		receiver.setCryptoUsedKeyID(cryptoKeyID);
		receiver.setCryptoUsedPubKey(cryptoPubkey);
		return receiver;
	}
	
	
	public static ActionMailTo makeActionMailTo(int actionType, String receiver, String subject, String text) {
		ActionMailTo a = new ActionMailTo(actionType);
		a.setReceiver(receiver);
		a.setSubject(subject);
		a.setText(text);
		return a;
	}
	
	public static ActionHttp makeActionHttp(int actionType, String url, String type, String[][] header, String[][] param) {
		ActionHttp a = new ActionHttp(actionType);
		a.setUrl(url);
		a.setType(type);
		if (header!=null) {
			for (String[] h: header) {
				a.addHeader(h[0], h[1]);
			}
		}
		if (param!=null) {
			for (String[] p: param) {
				a.addParam(p[0], p[1]);
			}
		}
		return a;
	}
	
	
	public static ContractPartner makeContractPartner(String email, String contractpartnerid, String ourcontractpartnerid) {
		ContractPartner c = new ContractPartner();
		c.setEmail(email);
		c.setContractpartnerid(contractpartnerid);
		c.setOurcontractpartnerid(ourcontractpartnerid);
		return c;
	}
	
	public static Feedinfo makeFeedinfo(boolean onlytest, String feedid, long creationdatetime, long effectivedatetime,
			String creatorEmail, String creatorUserID, Receiver receiver,
			ContractPartner sender, ContractPartner licensor, Vector<Action> actions)
	{
		Feedinfo f = new Feedinfo(); 
		f.setOnlytest(onlytest);
		f.setFeedid(feedid);
		f.setCreationdatetime(creationdatetime);
		f.setEffectivedatetime(effectivedatetime);
		f.setCreatorEmail(creatorEmail);
		f.setCreatorUserID(creatorUserID);
		f.setReceiver(receiver);
		f.setSender(sender);
		f.setLicensor(licensor);
		for (Action a:actions) {
			f.addAction(a);
		}
		return f;
//	<feedinfo>
//		<onlytest>true</onlytest><!-- MUST testmode true|false -->
//		<feedid>kaakka</feedid><!-- MUST id is provided by sender; should be unique -->
//		<creationdatetime>2010-10-01 00:00:00 GMT+00:00</creationdatetime><!-- MUST datetime of creation of this feed -->
//		<effectivedatetime>2010-10-01 00:00:00 GMT+00:00</effectivedatetime><!-- MUST ; most probably the same as creationdate ; datetime when this feed may be come ACTIVE as earliest; mainly used for deferred update-feeds with an effective-date not *asap* -->
//		<creator>
//			<email>user@sendingparty.nät</email><!-- should be an email-address of the *user* on the sending side -->
//			<userid>1919kdkdk12929</userid><!-- should be an unique id of the *user* on the sending side-->
//		</creator><!-- creator is a mere info-field for the sending-party ; receiving party may totally feel free to ignore this -->
//
//		<receiver>
//			<type>ftp</type><!-- ftp|sftp|ftps|webdav| -->
//			<servername></servername><!-- MUST provider should really give hostname instead of ip ; if hostname is given as ip, then put that one here -->
//			<serveripv4></serveripv4><!-- MUST resolved servername ipv4 -->
//			<serveripv6></serveripv6><!-- COULD resolved servername ipv6 -->
//			<authtype>login</authtype><!-- MUST login|keyfile|token|other -->
//			<authsha1>login</authsha1><!-- MUST case(login): SHA1(USERNAME:PASS) case(keyfile): SHA1(KEYFILE-data); case(token): SHA1(TOKEN-data) case(other): SHA1(relevant-data) -->
//
//			<crypto>
//				<relatedemail></relatedemail><!-- SHOULD -->
//				<usedkeyid></usedkeyid><!-- COULD ; keyid on gpgkesyerver -->
//				<usedpubkey></usedpubkey><!-- SHOULD ; ascii-armored / base64-form ; either this or keyid must be given -->
//			</crypto><!-- COULD -->					
//		</receiver>
//		
//		<sender>
//			<contractpartnerid></contractpartnerid><!-- MUST your side of "ID" ; worst case: the sending-company's correct trade-register-name/number -->
//			<ourcontractpartnerid></ourcontractpartnerid><!-- MUST our side of "ID" -->
//			<email></email><!-- MUST esp. for signature-check ; could of course be a generic account e.g. deliveryteam@dsphouse.nät -->		
//		</sender><!-- MUST -->
//		<licensor>
//			<contractpartnerid></contractpartnerid><!-- MUST your side of "ID" ; worst case: the license-giving-company's correct trade-register-name/number -->
//			<ourcontractpartnerid></ourcontractpartnerid><!-- MUST our side of "ID" -->
//			<email></email><!-- SHOULD good, but not neccessarily needed -->
//		</licensor><!-- MUST could be identical to sender, but has to be given -->
//	
//	
//		<actions>
//			<oninitialreceive>
//				<mailto>
//					<receiver>lala@nowhere.nät, lala1@nowhere.nät</receiver><!-- MUST -->
//					<subject></subject><!-- SHOULD this should be added to (an possibly empty) the emails subject ; SHOULD not be more than 200 7-byte-chars-->
//					<text></text><!-- SHOULD this should be added to (an possibly empty) the emails text ; SHOULD not be more than 4048 7-byte-chars-->
//				</mailto>
//				<http>
//					<url>http://nowhere.nät/callme.php?w=initireceive&amp;k=true</url>
//					<type>GET</type><!-- GET|POST|HEAD ; most probably GET -->
//					<addheader>
//						<header>
//							<name>gumpy</name>
//							<value>RWJ</value>
//						</header>
//					</addheader><!-- those headers are then added "X-" to the call - the url-caller may choose to ignore any header (especially those already used by himself) -->
//					<addparams>
//						<param>
//							<name>cmd</name>
//							<value>ehlo</value>
//						</param>
//						<param>
//							<name>jump</name>
//							<value>neverfrombridgbe</value>
//						</param>
//					</addparams>
//				</http>
//				<http>
//					<url>http://nowhere.nät/callme.php?w=initireceive&amp;k=true</url>
//					<type>POST</type><!-- GET|POST|HEAD ; most probably GET ; in case of POST, there SHOULD no (GET)params included in url-call ; so, this example shows *bad* stuff -->
//					<addheader>
//						<header>
//							<name>gumpy</name>
//							<value>RWJ</value>
//						</header>
//					</addheader><!-- those headers are then added "X-" to the call - the url-caller may choose to ignore any header (especially those already used by himself) -->
//					<addparams>
//						<param>
//							<name>cmd</name>
//							<value>ehlo</value>
//						</param>
//						<param>
//							<name>jump</name>
//							<value>neverfrombridgbe</value>
//						</param>
//					</addparams>
//				</http>
//			</oninitialreceive><!-- SHOULD when the *machine* picks up this delivery (aka pulls it away from *inbox*) -->
//			<onprocessstart></onprocessstart><!-- SHOULD when the *machine* start processing this feed -->
//			<onprocessend></onprocessend><!-- SHOULD when *machine* has ended working on this -->
//			<onfullsuccess>
//				<fax>
//					<number>+44 77 919191919</number>
//					<to>
//						<name>yeah company ltd.</name>
//						<department>it crowd</department><!-- resolves to: dep. *department* -->
//						<nameperson>jonathan jones</nameperson><!-- resolves to: Attn. *nameperson* -->
//						<street>main street 1010</street>
//						<postcode>112233</postcode>
//						<country>DK</country><!-- ISO -->
//						<additionaladdressinfo>Building 7</additionaladdressinfo>
//					</to>
//					<text></text><!-- text which MUST be on that fax -->
//					<costscoveredby>
//						<contractpartnerid></contractpartnerid><!-- MUST contractpartnerid on your side ; see above -->
//						<ourcontractpartnerid></ourcontractpartnerid><!-- MUST contractpartnerid on our side ; see above -->					
//						<maxcostscovered>EUR 0.77</maxcostscovered>
//					</costscoveredby>
//				</fax><!-- would be total awesome, if someone would provide even *non*-digital reports; of course this costs money ; receving party can decide to ignore this -->
//				
//				<letter>
//					<registered>true</registered><!-- MUST true|false ; must be registered letter yes/no -->
//					<to>
//						<name>yeah company ltd.</name>
//						<department>it crowd</department><!-- resolves to: dep. *department* -->
//						<nameperson>jonathan jones</nameperson><!-- resolves to: Attn. *nameperson* -->
//						<street>main street 1010</street>
//						<postcode>112233</postcode>
//						<country>DK</country><!-- ISO -->
//						<additionaladdressinfo>Building 7</additionaladdressinfo>
//					</to>
//					<text></text><!-- text which MUST be on that letter -->
//					<costscoveredby>
//						<contractpartnerid></contractpartnerid><!-- MUST contractpartnerid on your side ; see above -->
//						<ourcontractpartnerid></ourcontractpartnerid><!-- MUST contractpartnerid on our side ; see above -->					
//						<maxcostscovered>EUR 0.77</maxcostscovered>
//					</costscoveredby>
//				</letter><!-- would be total awesome, if someone would provide even *non*-digital reports; of course this costs money ; receving party can decide to ignore this -->			
//			</onfullsuccess><!-- SHOULD when *machine* thinks, everything was read and successfully process - without ANY error -->
//			<onerror></onerror><!-- SHOULD when *machine* could process this, but some error, or total fail occured -->
//		</actions><!-- SHOULD - hopefully the receiving party also respects this ; this would ease a lot in the chain... -->
//	</feedinfo>
	}
	
	public static Bundle makeBundle(BundleIDs ids, String displayname, String name, String version, String display_artist, Vector<Contributor> contributors, BundleInformation information, Vector<Territory> territories, long timeframeFrom, long timeFrameUntil, String pricecode, String wholesale, Vector<LicenseRule> licenseRules, Vector<Item> items) {
		Bundle b = new Bundle();
		b.setIDs(ids);
		b.setDisplayname(displayname);
		b.setName(name);
		b.setVersion(version);
		b.setDisplay_artist(display_artist);
		for (Contributor c : contributors) {
			b.addContributor(c);
		}
		b.setInformation(information);
		for (Territory t : territories) {
			b.addTerritory(t);
		}
		b.setTimeframeFrom(timeframeFrom);
		b.setTimeFrameUntil(timeFrameUntil);
		b.setPricecode(pricecode);
		b.setWholesale(wholesale);
		for (Item i : items) {
			b.addItem(i);
		}
		
// 		<ids></ids><!-- MUST -->
//		<displayname></displayname><!-- MUST this is how this should be displayed in pos -->
//		<name></name><!-- MUST name - should be without "version" or stuff could be totally same as in displayname -->
//		<version></version><!-- MUST version if different versions of this bundle exist - can be empty -->
//		<display_artist></display_artist><!-- SHOULD ; convenience-info receiver can decide to ignore this and to create from the give contributors -->	
//		<contributors>
//       ...		
//		</contributors>
//		
//		<information>
//		</information>
//		
//		<license_basis>
//			<territorial>
//				<territory type="allow">GB</territory><!-- type explicitly given -->
//				<territory type="disallow">US</territory><!-- for use for eg. WW -AT -->
//				<territory></territory><!-- type omitted: allow -->
//				<!-- U more territories -->
//			</territorial>
//			<timeframe>
//				<from>1960-02-13 00:00:00 GMT+01:00</from><!-- MUST ; most-recent-release-date from which on receiver may use this -->
//				<to>2050-02-13 23:59:59 GMT+01:00</to><!-- MUST ; cancellation-date -->
//			</timeframe>
//				
//			<pricing>
//				<pricecode>MEDIUM</pricecode><!-- COULD ; arbitrary-info ; mainly agreed in  -->
//				<wholesale>EUR 9.99</wholesale><!-- COULD ; an explicitly given wholesale-price overrides the basic pricecode-given-wp ; most probably either one of pricecode OR wholesaleprice should be given -->
//			</pricing>
//		</license_basis><!-- these are the basic rules under which this bundle is provided to u (of course on basis of those agreed on in the contracting phase) -->
//		
//		<license_specifics>
//			<!-- all territories here MUST be given in *basis* -->
//			<!-- all times here MUST be included in the basis-timeframe -->
//			<!-- HT 14.02.2011 - using some *basic-if-then-else*-style here because of clarity and easy implemenation-possibility -->
//			<rule_1><!-- HT 14.02.2011 yeah, starting at #1 ;-) -->
//				...				
//			</rule_1>
//		</license_specifics><!-- MUST ; if no specific rules should be applied: empty node -->
//		
//		<items>
		return b;
	}
	
	public static BundleIDs makeBundleIDs(String grid, String upc, String isrc, String contentauthid, String labelordernum, String amzn, String isbn, String finetunesid, String ourid, String yourid) {
		BundleIDs b = new BundleIDs();
		b.setGrid(grid);
		b.setUpc(upc);
		b.setIsrc(isrc);
		b.setContentauthid(contentauthid);
		b.setLabelordernum(labelordernum);
		b.setAmzn(amzn);
		b.setIsbn(isbn);
		b.setFinetunesid(finetunesid);
		b.setOurid(ourid);
		b.setYourid(yourid);
		return b;
		
//		<grid></grid><!-- COULD grid -->
//		<upc></upc><!-- SHOULD ean/upc -->
//		<isrc></isrc><!-- SHOULD isrc - not applicable on bundle-level, but for  -->
//		<contentauthid></contentauthid><!-- SHOULD content-authority-id - https://contentauthority.com  -->
//		<labelordernum></labelordernum><!-- COULD -->
//		<amzn></amzn><!-- COULD ; amazon-id-stuff -->
//		<isbn></isbn><!-- COULD -->
//		<finetunesid></finetunesid><!-- COULD -->
//		<ourid>ourid</ourid><!-- COULD -->
//		<yourid></yourid><!-- COULD -->
		
	}
	
	public static BundleInformation makeBundleInformation(Vector<String[]> promotexts, Vector<String[]> teasertexts,long digital_release_datetime, long physical_release_datetime, Vector<BundleRelatedInformation> related) {
		BundleInformation i = new BundleInformation();
		for (String[] s : promotexts) {
			i.addPromotext(s[0],s[1]);
		}
		for (String[] s : teasertexts) {
			i.addTeasertext(s[0],s[1]);
		}
		i.setDigital_release_datetime(digital_release_datetime);
		i.setPhysical_release_datetime(physical_release_datetime);
		for (BundleRelatedInformation r : related) {
			i.addRelatedInformation(r);
		}
		return i;
	}
	
	public static Contributor makeContributor(String name, String type, String glv, String finetunesid, String ourid, String yourid, String contentauthid) {
		Contributor c = new Contributor();
		c.setName(name);
		c.setType(type);
		c.setGLV(glv);
		c.setFinetunesid(finetunesid);
		c.setOurid(ourid);
		c.setYourid(yourid);
		c.setContentauthid(contentauthid);
		return c;
	}
	
	public static Feed makeExampleFeed() {
		Feed f = new Feed();
		
	  // -- Feedinfo --
		boolean onlytest =true;
		String feedid = "example feedid";
		long creationdatetime = System.currentTimeMillis();
		long effectivedatetime = System.currentTimeMillis();
		String creatorEmail = "creator@example.org";
		String creatorUserID = "creator";
		
		Receiver receiver = makeReceiver("ftp","testserver.fnppl.org","127.0.0.1",null,"password","login","a","b","c");
		ContractPartner sender = makeContractPartner("sender@example.org","contractpartnerid","ourcontractpartnerid");
		ContractPartner licensor = makeContractPartner("licensor@example.org","contractpartnerid","ourcontractpartnerid");
		
		//actions
		Vector<Action> actions = new Vector<Action>();
		actions.add(makeActionHttp(Action.TYPE_ONINITIALRECEIVE, "check.fnppl.org", "GET", new String[][]{{"header1","value1"}}, new String[][]{{"param1","value1"},{"param2","value2"}}));
		actions.add(makeActionMailTo(Action.TYPE_ONERROR,"receiver", "subject", "error"));
		actions.add(makeActionMailTo(Action.TYPE_ONFULLSUCCESS,"receiver", "subject", "successful"));
		
		f.setFeedinfo(makeFeedinfo(onlytest, feedid, creationdatetime, effectivedatetime, creatorEmail, creatorUserID, receiver, sender, licensor, actions));
	  // -- end of Feedinfo --
		
	  // -- Bundle --
		
		//BundleIDs
		BundleIDs ids = makeBundleIDs("grid", "upc", "isrc", "contentauthid", "labelordernum", "amzn", "isbn", "finetunesid", "ourid", "yourid");
		String displayname = "Yeah Yeah it's fine";
		String name = "fine so fine";
		String version ="v0.9999";
		String display_artist = "finest artist";
		
		//Contributors
		Vector<Contributor> contributors = new Vector<Contributor>();
		Contributor c = makeContributor("SupaLabel", "label", "glv", "finetunesid", "our id", "your id", "contentauth");
		boolean publishable = true;
		c.setFacebook("facebook", publishable);
		c.setMyspace("myspace", publishable);
		c.setHomepage("www.homepage.org", publishable);
		c.setTwitter("twitter", publishable);
		c.setPhone("0124981240912", false);
		contributors.add(c);
		
		//Information
		Vector<String[]> promotexts = new Vector<String[]>();
		promotexts.add(new String[]{"EN","This is the english promotext."});
		promotexts.add(new String[]{"DE","Dies ist der deutsche promotext."});
		Vector<String[]> teasertexts = new Vector<String[]>();
		teasertexts.add(new String[]{"EN","This is the english teasertext."});
		teasertexts.add(new String[]{"DE","Dies ist der deutsche teasertext."});
		
		long digital_release_datetime = System.currentTimeMillis();
		long physical_release_datetime = System.currentTimeMillis();
		Vector<BundleRelatedInformation> related = new Vector<BundleRelatedInformation>();
		related.add(BundleRelatedInformation.createPhysicalDistributer("ExampleDistrubutor"));
		related.add(BundleRelatedInformation.createYouTube("url.to.youtube.file", "channel"));
		related.add(BundleRelatedInformation.createBundleIDs(makeBundleIDs(null, "upc", null, null, null, "amzn", "isbn", null, null, null)));
		BundleInformation information = makeBundleInformation(promotexts, teasertexts, digital_release_datetime, physical_release_datetime, related);
		
		//Territorial
		Vector<Territory> territorial = new Vector<Territory>();
		territorial.add(Territory.allow("DE"));
		territorial.add(Territory.allow("GB"));
		territorial.add(Territory.disallow("US"));
		
		long timeframeFrom = System.currentTimeMillis();
		long timeFrameUntil = timeframeFrom + 365*24*60*60*1000L;
		String pricecode = "MEDIUM";
		String wholesale = null;
		
		//License Rules
		Vector<LicenseRule> licenseRules = new Vector<LicenseRule>();
		//TODO makeLicenseRule
		
		//Items
		Vector<Item> items = new Vector<Item>();
		String itype = "audio";
		BundleIDs iids = null;
		String idisplayname = displayname;
		String iname = name;
		String iversion = "studio";
		Vector<Contributor> icontributors = contributors; 
		BundleInformation iinformation = information;
		Vector<Territory> iterritorial = territorial;
		long ifrom = System.currentTimeMillis();
		long iuntil = ifrom + 30*24*60*60*1000L;
		String ipricecode = null;
		String iwholesale = null;
		Vector<String> igenres = new Vector<String>();
		igenres.add("POP");
		String iorigin_country ="GB";
		String imain_language ="EN";
		boolean ibundle_only = true;
		boolean istreaming_allowed = false;
		Vector<MediaFile> ifiles = null;
		//TODO makeMediaFile
		
		Item item = makeItem(itype, iids, idisplayname, iname, iversion, icontributors, iinformation, iterritorial, ifrom, iuntil, ipricecode, iwholesale, igenres, iorigin_country, imain_language, ibundle_only, istreaming_allowed, ifiles);
		items.add(item);
		
		
		Bundle bundle = makeBundle(
				ids, displayname, name, version, display_artist, contributors, information,
				territorial, timeframeFrom, timeFrameUntil, pricecode, wholesale, licenseRules, items);
		f.addBundle(bundle);
	  // -- end of Bundle --
		
		
		return f;
		
//		<feed>
//			<feedinfo>
//			   ...
// 			</feedinfo>
//			
//			
//			<bundle>
//				<ids>
//					<grid></grid><!-- COULD grid -->
//					<upc></upc><!-- SHOULD ean/upc -->
//					<isrc></isrc><!-- SHOULD isrc - not applicable on bundle-level, but for  -->
//					<contentauthid></contentauthid><!-- SHOULD content-authority-id - https://contentauthority.com  -->
//					<labelordernum></labelordernum><!-- COULD -->
//					<amzn></amzn><!-- COULD ; amazon-id-stuff -->
//					<isbn></isbn><!-- COULD -->
//					<finetunesid></finetunesid><!-- COULD -->
//					<ourid>ourid</ourid><!-- COULD -->
//					<yourid></yourid><!-- COULD -->
//				</ids><!-- MUST -->
//				<displayname></displayname><!-- MUST this is how this should be displayed in pos -->
//				<name></name><!-- MUST name - should be without "version" or stuff could be totally same as in displayname -->
//				<version></version><!-- MUST version if different versions of this bundle exist - can be empty -->
//				<display_artist></display_artist><!-- SHOULD ; convenience-info receiver can decide to ignore this and to create from the give contributors -->	
//				<contributors>
//					<contributor1>
//						<name></name><!-- MUST could be labelname -->
//						<type>label</type><!-- MUST e.g. label|composer|texter|writer|conductor -->
//						<ids>
//							<gvl></gvl><!-- gvl -->
//							<finetunesid></finetunesid><!-- finetunes-strange-id -->
//							<ourid></ourid><!-- must be unique for that contributor/sender/licensor -->
//							<yourid></yourid><!-- if given some kind of "your" id: thats it -->
//							<contentauthid></contentauthid><!-- SHOULD content-authority-id - https://contentauthority.com  -->
//						</ids><!-- MUST -->
//						<www>
//							<facebook></facebook>
//							<myspace></myspace>
//							<homepage></homepage>
//							<twitter></twitter>
//							<phone publishable="false">+49 44 9191919</phone><!-- COULD international format -->
//						</www><!-- SHOULD further information on this ; every single information-entry cold be tagged *publishable* which would then mean wether customers of receiver are also allowed to be given this information ; if publishable is not given, then this is granted -->
//					</contributor1><!-- MUST -->
//					<contributor2>
//						<name></name><!-- could be artistname -->
//						<type>display_artist</type><!-- e.g. display_artist -->
//						<ids>
//							<finetunes></finetunes><!-- finetunes-strange-id -->
//							<own></own><!-- must be unique for that contributor/licensor -->
//							<your></your><!-- if given some kind of "your" id: thats it -->					
//							<contentauthid></contentauthid><!-- SHOULD content-authority-id - https://contentauthority.com  -->
//						</ids>
//						<www>
//							<facebook></facebook>
//							<myspace></myspace>
//							<homepage></homepage>
//							<twitter></twitter>
//							<phone publishable="false">+49 44 9191919</phone><!-- COULD international format -->
//						</www><!-- SHOULD further information on this ; every single information-entry cold be tagged *publishable* which would then mean wether customers of receiver are also allowed to be given this information ; if publishable is not given, then this is granted -->				
//					</contributor2>
//					<contributor3>
//						<name></name><!-- could be compilator -->
//						<type>compilator</type><!-- e.g. compilator -->
//						<ids>
//							<finetunes></finetunes><!-- finetunes-strange-id -->
//							<own></own><!-- must be unique for that contributor/licensor -->
//							<your></your><!-- if given some kind of "your" id: thats it -->					
//							<contentauthid></contentauthid><!-- SHOULD content-authority-id - https://contentauthority.com  -->
//						</ids>
//						<www>
//							<facebook></facebook>
//							<myspace></myspace>
//							<homepage></homepage>
//							<twitter></twitter>
//							<phone publishable="false">+49 44 9191919</phone><!-- COULD international format -->
//						</www><!-- SHOULD further information on this ; every single information-entry cold be tagged *publishable* which would then mean wether customers of receiver are also allowed to be given this information ; if publishable is not given, then this is granted -->				
//					</contributor3>
//					<contributor4>
//						<name></name><!-- could be compilator -->
//						<type>vocals</type><!-- e.g. vocals -->
//						<ids>
//							<finetunes></finetunes><!-- finetunes-strange-id -->
//							<own></own><!-- must be unique for that contributor/licensor -->
//							<your></your><!-- if given some kind of "your" id: thats it -->
//							<contentauthid></contentauthid><!-- SHOULD content-authority-id - https://contentauthority.com  -->					
//						</ids>
//						<www>
//							<facebook></facebook>
//							<myspace></myspace>
//							<homepage></homepage>
//							<twitter></twitter>
//							<phone publishable="false">+49 44 9191919</phone><!-- COULD international format -->
//					</www><!-- SHOULD further information on this ; every single information-entry cold be tagged *publishable* which would then mean wether customers of receiver are also allowed to be given this information ; if publishable is not given, then this is granted -->				
//					</contributor4>
//					<!-- HT 14.02.2011 - could also have some "restrictions/conditions" on it - e.g. when an artist is different for some territory or such... -->
//				</contributors><!-- MUST the contributors are ordered from 1 to n - most significant ones (per type) are the ones with the smalles n -->
//				
//				<information>
//					<promotext lang="en">
//					EN asdasd
//					</promotext>
//					<promotext lang="de">
//					DE asdasd
//					</promotext>
//					<teasertext lang="de">
//					DE asdasd
//					</teasertext>
//					
//					<physical_release_datetime>1960-02-13 00:00:00 GMT+00:00</physical_release_datetime>
//					<digital_release_datetime>1960-02-13 00:00:00 GMT+00:00</digital_release_datetime>
//					
//					<related>
//						<physical_distributor></physical_distributor><!-- COULD (one of) the physical distributors  -->
//						<physical_distributor publishable="false"></physical_distributor><!-- COULD (one of) the physical distributors  -->
//						<utube>
//							<url></url>
//							<channel></channel>
//						</utube>
//						<bundle>
//							<ids>
//								<upc></upc>
//								<amzn></amzn>
//								<isbn></isbn>
//							</ids>
//							<purchase>
//								<pos>itms</pos>
//								<url>http://itms/ajsjasj</url>
//							</purchase><!-- COULD ; most probably not, when this feeds recipient is a POS -->
//						</bundle>
//						<bundle>
//							<ids>
//								<upc></upc>
//								<amzn></amzn>
//								<isbn></isbn>
//							</ids>
//						</bundle>
//					</related><!-- SHOULD ; if available any other forms of this product (versions, cover, tracks included in mix etc.) -->
//				</information>
//				
//				<license_basis>
//					<territorial>
//						<territory type="allow">GB</territory><!-- type explicitly given -->
//						<territory type="disallow">US</territory><!-- for use for eg. WW -AT -->
//						<territory></territory><!-- type omitted: allow -->
//						<!-- U more territories -->
//					</territorial>
//					<timeframe>
//						<from>1960-02-13 00:00:00 GMT+01:00</from><!-- MUST ; most-recent-release-date from which on receiver may use this -->
//						<to>2050-02-13 23:59:59 GMT+01:00</to><!-- MUST ; cancellation-date -->
//					</timeframe>
//						
//					<pricing>
//						<pricecode>MEDIUM</pricecode><!-- COULD ; arbitrary-info ; mainly agreed in  -->
//						<wholesale>EUR 9.99</wholesale><!-- COULD ; an explicitly given wholesale-price overrides the basic pricecode-given-wp ; most probably either one of pricecode OR wholesaleprice should be given -->
//					</pricing>
//				</license_basis><!-- these are the basic rules under which this bundle is provided to u (of course on basis of those agreed on in the contracting phase) -->
//				
//				
//				<license_specifics>
//					<!-- all territories here MUST be given in *basis* -->
//					<!-- all times here MUST be included in the basis-timeframe -->
//					<!-- HT 14.02.2011 - using some *basic-if-then-else*-style here because of clarity and easy implemenation-possibility -->
//					<rule_1><!-- HT 14.02.2011 yeah, starting at #1 ;-) -->
//						<if>
//							<what>territory</what>
//							<operator>containedin</operator><!-- equals/before/after/contains/containedin -->
//							<value>EU</value>
//						</if>
//						<then>
//							<echo>me is debugging output for happy programmers...</echo><!-- HT 14.02.2011 - much appreciated... -->
//							<break /><!-- this means to not process any more rules...; just leave it away to "continue" -->
//						</then>
//						<else><!-- HT 14.02.2011 we should not cascade this now..., but could easily do -->
//							<proclaim>
//								<what>price</what>
//								<for>EUR 9.99</for>
//							</proclaim>
//							<proclaim>
//								<what>pricelevel</what>
//								<for>medium</for>
//							</proclaim>
//							<break />
//						</else>
//					</rule_1><!-- numbered since we need an ordered mode here - first come first match -->
//					
//					<rule_2><!-- HT 14.02.2011 yeah, starting at #1 ;-) -->
//						<if>
//							<what>territory</what><!-- *field* -->
//							<operator>equals</operator><!-- equals/before/after/contains/containedin -->
//							<value>DE</value>
//						</if>
//						<then>
//							<echo>YOU CANNOT EVEN REACH ME - since RULE1 breaks and DE is in EU</echo>					
//						</then>				
//					</rule_2>
//				</license_specifics><!-- MUST ; if no specific rules should be applied: empty node -->
//				
//				<items> <!-- those items aka tracks/videos/flash/apps/whatever contained in this bundle -->
//					<item>
//						<type>audio</type><!-- MUST ; audio, video, android-app, win32-program, whatever -->
//						<ids>
//							<grid></grid><!-- COULD grid -->
//							<isrc></isrc><!-- SHOULD isrc - not applicable on bundle-level, but for -->
//							<contentauthid></contentauthid><!-- SHOULD content-authority-id - https://contentauthority.com -->
//							<finetunes></finetunes><!-- strange finetunes id -->
//						</ids><!-- MUST -->
//						<displayname></displayname><!-- MUST this is how this should be displayed in pos -->
//						<name></name><!-- MUST name - should be without "version" or stuff could be totally same as in displayname -->
//						<version></version><!-- MUST version if different versions of this bundle exist - can be empty -->
//						<contributors>
//							<contributor1>
//								<name></name><!-- could be artistname -->
//								<type>display_artist</type><!-- e.g. display_artist -->
//								<ids>
//									<finetunes></finetunes><!-- finetunes-strange-id -->
//									<own></own><!-- must be unique for that contributor/licensor -->
//									<your></your><!-- if given some kind of "your" id: thats it -->					
//									<contentauthid></contentauthid><!-- SHOULD content-authority-id - https://contentauthority.com  -->
//								</ids>
//								<www>
//									<facebook></facebook>
//									<myspace></myspace>
//									<homepage></homepage>
//									<twitter></twitter>
//									<phone publishable="false">+49 44 9191919</phone><!-- COULD international format -->
//								</www><!-- SHOULD further information on this ; every single information-entry cold be tagged *publishable* which would then mean wether customers of receiver are also allowed to be given this information ; if publishable is not given, then this is granted -->				
//							</contributor1>
//							<!-- HT 14.02.2011 - could also have some "restrictions/conditions" on it - e.g. when an artist is different for some territory or such... -->
//						</contributors><!-- MUST the contributors are ordered from 1 to n - most significant ones (per type) are the ones with the smalles n -->
//				
//						<information>
//							<promotext lang="en">
//							EN asdasd
//							</promotext>
//							<promotext lang="de">
//							DE asdasd
//							</promotext>
//							<teasertext lang="de">
//							DE asdasd
//							</teasertext>
//					
//							<physical_release_datetime>1960-02-13 00:00:00 GMT+00:00</physical_release_datetime>
//							<digital_release_datetime>1960-02-13 00:00:00 GMT+00:00</digital_release_datetime>
//					
//							<playlength>987</playlength><!-- MUST ; in seconds ; at least for media-files -->
//							
//							<related>					
//								<utube>
//									<url></url>
//								</utube>
//								<utube>
//									<channel></channel>
//								</utube>
//							</related><!-- SHOULD ; if available any other forms of this product (versions, cover, tracks included in mix etc.) -->
//						</information>
//
//						<license_basis>
//							<asonbundlelevel /><!-- COULD ; if asonbundlelevel-node given, this means to take the enclosing bundle's license-basis ; could also be omitted ; could otherwise be the same rules as above -->
//						</license_basis>
//						<license_specifics>
//							<asonbundlelevel /><!-- COULD ; if asonbundlelevel-node given, this means to take the enclosing bundle's license-basis ; could also be omitted ; could otherwise be the same rules as above -->
//						</license_specifics>
//
//						<tags>
//							<genres>
//								<genre1></genre1>
//								<genre2></genre2>
//							</genres>
//							<origin_country>DE</origin_country>
//							<main_language>de</main_language>
//							<bundle_only>false</bundle_only>
//							<streaming_allowed>true</streaming_allowed>
//						</tags>
//						
//						<files>
//							<file>
//								<type>full</type><!-- MUST ; full|prelistening|cover -->
//								<filetype></filetype><!-- MUST ; FILEFORMAT/BITRATE[KBIT/s]/CODEC/CODECSETTINGS e.g. mp3/320/unknown/CBR -->
//								<channels>stereo</channels><!-- MUST ; mono, stereo, joint-stereo, 5.1 -->
//								<bytes>84848483838</bytes><!-- length of file in bytes -->
//								<location>
//									<http>
//										<url></url><!-- MUST ; should be tls -->
//										<user></user><!-- COULD ; http-basic-header-auth -->
//										<pass></pass><!-- COULD ; http-basic-header-auth -->
//										<expiresdatetime>2011-06-10 20:24:00 GMT+00:00</expiresdatetime><!-- MUST ; until when this file is definitely available to be called -->
//									</http><!-- http means that, this file is not delivered, but it can be pulled by the receiving parties ; integrity is assured by checksums -->
//								</location>
//								<checksums>
//									<sha1></sha1><!-- SHOULD sha1-checksum of that file -->
//									<md5></md5><!-- SHOULD sha1-checksum of that file -->
//								</checksums><!-- MUST one checksum must be given; -->
//							</file>
//							<file>
//								<type>prelistening</type><!-- MUST ; full|prelistening|cover -->
//								<filetype></filetype><!-- MUST ; FILEFORMAT/BITRATE[KBIT/s]/CODEC/CODECSETTINGS e.g. mp3/320/unknown/CBR -->
//								<channels>stereo</channels>
//								<bytes>84848483838</bytes><!-- length of file in bytes -->
//								<location>
//									<ftp>
//										<server></server>
//										<port></port>
//										<path></path>
//										<user></user><!-- COULD ; -->
//										<pass></pass><!-- COULD ; -->
//										<expiresdatetime>2011-06-10 20:24:00 GMT+00:00</expiresdatetime><!-- MUST ; until when this file is definitely available to be called -->
//									</ftp><!-- http means that, this file is not delivered, but it can be pulled by the receiving parties ; integrity is assured by checksums -->
//								</location>
//								<checksums>
//									<sha1></sha1><!-- SHOULD sha1-checksum of that file -->
//									<md5></md5><!-- SHOULD sha1-checksum of that file -->
//								</checksums><!-- MUST one checksum must be given; if encrypted file is referenced, those bytes/checksums are those of the encrypted file -->
//								<decryptinfo>
//									<cipher>AES/256/CTR</cipher><!-- MUST ; AES, RIJNDAEL, XOR, Arcfour, whatever - should be "convenient" -->
//									<initvector>19191919199</initvector>
//									<key></key><!-- MUST ; base64-encoded this key is encrypted by the public-key, therefore only the receiver (in possession of the private key) can decrypt this key and with that decrypted key decrypt the file -->
//									<bytes></bytes>
//									<checksums>
//										<sha1></sha1><!-- SHOULD sha1-checksum of that file -->
//										<md5></md5><!-- SHOULD sha1-checksum of that file -->
//									</checksums>
//								</decryptinfo>
//							</file>
//							<file>
//								<type>flv</type><!-- MUST ; full|prelistening|cover -->
//								<filetype>FLV/96/YUV/ajja</filetype><!-- MUST ; FILEFORMAT/BITRATE[KBIT/s]/CODEC/CODECSETTINGS e.g. mp3/320/unknown/CBR -->
//								<dimension>330x600</dimension><!-- MUST ; WIDTHxHEIGHT ; square-pixels -->
//								<bytes>84848483838</bytes><!-- length of file in bytes -->
//								<location>
//									<path>../ajajaj.flv</path><!-- relative-paths are relative to this xml -->
//								</location>
//								<checksums>
//									<sha1></sha1><!-- SHOULD sha1-checksum of that file -->
//									<md5></md5><!-- SHOULD sha1-checksum of that file -->
//								</checksums><!-- MUST one checksum must be given; if encrypted file is referenced, those bytes/checksums are those of the encrypted file -->						
//							</file>
//						</files>
//					</item>
//				</items>
//				
//			</bundle>
//			<!--  could have X more bundles -->
//			<bundle>
//			</bundle>
//
//			<!-- it is also possible to only send "an item" - aka track although this is quite inconvenient... -->	
//			<item>
//			</item>
//			<!--  could have Z more items -->
//		</feed>
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Feed example = makeExampleFeed();
		
		Element root = example.toElement();
		
		try {
			Document.buildDocument(root).writeToFile(new File("osdx_example_feed.xml"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		afc.toScreen();
//		afc.toFile(new File(args[0]));
	}

}
