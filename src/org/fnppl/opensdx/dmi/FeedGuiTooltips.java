package org.fnppl.opensdx.dmi;

import javax.swing.ToolTipManager;
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
public class FeedGuiTooltips {

	public static void initDelays() {
		ToolTipManager.sharedInstance().setInitialDelay(000);
		ToolTipManager.sharedInstance().setDismissDelay(20000);
	}
	
	public static final String helpButton         = "<html><body>Activate this button to show a help text when moving the mouse over an element.</body></html>";
	
	
//--- FeedInfo Panel --------------------------------------------
	
	public static final String feedinfo =  "<html><body>"
		+ "The <b>FeedInfo</b> panel contains the basic information of a feed.<br />"
		+"</body></html>";
	
	public static final String feedid             = "<html><body>Every <b>feed</b> needs to have its own <b>feed id</b>, even if it is an update to a former feed.<br />It is good practice to generate a UUID (by pressing the random UUID button) for every feed you create but certainly you can enter your own id.</body></html>";
	public static final String randomUUID         = "<html><body>This will create a <b>random UUID</b> and set it as <b>feed id</b>.</body></html>";
	public static final String onlytest           = "<html><body>Select whether your feed should be a test or real data</body></html>";
	public static final String creation_datetime  = "<html><body>The datetime when this feed is created.<br />Format: yyyy-mm-dd HH:MM:SS GMT+hh:00</body></html>";
	public static final String effective_datetime = "<html><body>The datetime when this feed should come effective.<br />Format: yyyy-mm-dd HH:MM:SS GMT+hh:00</body></html>";
	public static final String now 				  = "<html><body>Set the current datetime as <b>creation</b> and <b>effective datetime</b>.</body></html>";
	
	public static final String sender = "<html><body><div style=\"width:520px\">"
		+"The <b>Sender</b> part should give information about the sender of the feed (typically you / your company),"
		+" where the <b>Key ID</b> should be related to the key that will be used by the sender in further correspondence."
		+"The <b>ID</b> field should hold the id on the receiver side, the <b>Our ID</b> field should hold the ID of the sender side."
		+"</div></body></html>";
		
	public static final String licensor = "<html><body><div style=\"width:520px\">"
		+"The <b>Licensor</b> part should give information about the licensor (the one who holds the license) of the feeds content (typically you / your company).<br />"
		+"The <b>Key ID</b> should be related to the key that will be used by the licensor in further correspondence."
		+" The <b>ID</b> field should hold the id on the receiver side, the <b>Our ID</b> field should hold the ID of the sender side."
		+"</div></body></html>";
	
	public static final String licensee = "<html><body><div style=\"width:520px\">"
		+"The <b>Licensee</b> part should give information about the licensee (the one to whom the license is granted to) of the feeds content (typically the receiver).<br />"
		+"The <b>Key ID</b> should be related to the key that will be used by the licensee in further correspondence.<br />"
		+"The <b>ID</b> field should hold the id on the receiver side, the <b>Our ID</b> field should hold the ID of the sender side."
		+"</div></body></html>";
		
	public static final String creator = "<html><body><div style=\"width:520px\">"
		+"The <b>Creator</b> part should give information about the creator of the feed (typically you / your company),"
		+" where the <b>Key ID</b> should be related to the key that will be used by the creator in further correspondence."
		+" The <b>User ID</b> field could specify an id of the creator."
		+"</div></body></html>";
	
	public static final String receiver = "<html><body><div style=\"width:520px\">"
		+"The <b>Receiver</b> part should give information about the receiver (the one to whom the feed will be delivered) of the feeds content."
		+" Typically the <b>type</b> will be an <b>openSDX fileserver</b> where you need to enter the servername (e.g. <i>simfy.finetunes.net</i>) and the login data"
		+" (<b>Key ID</b> and <b>username</b>) you agreed on with the operator of the fileserver. The <b>server IPv4</b> field will automatically be filled when pressing the ENTER key"
		+" in the <b>servername</b> field.<br />"
	    +" The button <b>beam me up!</b> will open another window where the feed and all referenced media files could be send to the given receiver."
	    +"</div></body></html>";
	
	public static final String triggered_actions = "<html><body><div style=\"width:520px\">"
		+ "<b>Triggerd Actions</b> are notifications send to you by the <b>receiver</b> on the following events:"
		+ "<ul><li>on initial receive: triggered when the feed was received by the receiver</li>"
		+ "<li>on process start: triggered when the processing of the feed starts</li>"
		+ "<li>on process end: triggered when the processing of the feed ends</li>"
		+ "<li>on error: triggered when an error occurs during the processing of the feed</li>"
		+ "<li>on full success: triggered when the processing of the feed was successfully completed</li>"
		+ "</ul>You can decide on which of these events you want to get a notification and whether this should "
		+ "be done by an email (<b>add Mail Action</b>) or by a http call (<b>add HTTP Action</b>). "
		+ "If you don't want to be notified you can leave these fields empty, but it is recommended to add "
		+ "notifications for at least the <b>on error</b> and the <b>on full success</b> cases."
		+"</div></body></html>";
	
//--- End of FeedInfo Panel --------------------------------------------

	
//--- BundlePanel --------------------------------------------

	public static final String bundle = "<html><body><div style=\"width:520px\">"
		+ "A <b>bundle</b> contains information on how to handle a collection of <b>items</b>. This is mainly an album/ep/single. A <b>bundle</b> is identified by "
		+ "one unique identifier, but more unique identifiers could and should be transmitted as well in the <b>IDs</b> panel. Most notably on the <b>bundle</b>-level "
		+ "As basic information of the <b>bundle</b> the <b>name</b>, <b>display name</b>, <b>version</b> and to have this easy at hand, the <b>display artist</b> should be given "
		+ "Moreover you should switch through the subtabs <b>contribtors</b>, <b>Information</b>, <b>License</b>, <b>Tags</b> and <b>Files</b> to enter more detailed information about these "
		+ "topics on the <b>bundle</b> level." 
		+"</div></body></html>";
	
	public static final String bundleIds = "<html><body><div style=\"width:520px\">"
		+ "The <b>IDs</b> panel holds one or more identifiers for this bundle. The <b>IDs</b>-element is also present on <b>item</b>-level and could also be "
		+ "present on <b>contributor</b>-level (in terms of publishing-authorities-id-system). A good unique identifier on bundle-level is the EAN/UPC-code; a "
		+ "good unique identifier on <b>item</b>-level is the ISRC resp. ISWC-code. But you could create your own identifier as well (e.g. in the <b>licensor</b> or <b>licensee</b> fields."
		+"</div></body></html>";

	public static final String bundleContributors = "<html><body><div style=\"width:520px\">"
		+ "Everyone and every company involved in this <b>bundle</b> (and its <b>items</b> placed here), must be denoted here. On "
		+ "<b>item</b>-level these <b>contributors</b> are therefore just referenced. If one (or more) contributor(s) should only be <i>valid</i> on <b>item</b>-level (e.g. a conductor "
		+ "only conducting one <b>item</b>(track) on this release), you could/should mark that contributor (e.g. conductor) to be <b>only on sublevel</b>. Contributors "
		+ "can be of any type: Artist, Composer, Texter, Lyricist, Label, ... just select the <b>type</b> from the list of all currently supported contributor types."
		+ "If you a missing a special <b>type</b> you need, please send us an email and we will update the list.<br />"
		+ "For every <b>contributor</b> you should/could provide identifies (depending on the selected type) and related information-sources (such as Website, Blog, youtube-Channel etc.)."
		+"</div></body></html>";

	public static final String bundleInformation = "<html><body><div style=\"width:520px\">"
		+ "The <b>Information</b>-panel contains informations such as <b>release dates</b>, <b>playlength</b>, <b>main language</b>, <b>origin country</b> and <b>promotion</b> and <b>teaser texts</b> for the whole <b>bundle</b>."
		+"</div></body></html>";

	public static final String bundleLicense = "<html><body><div style=\"width:520px\">"
		+ "The <b>License</b>-panel contains information about the granted license like the <b>timeframe</b>, <b>pricing</b> or allowed and disallowed <b>territoires</b>."
		+"</div></body></html>";

	public static final String bundleTags = "<html><body><div style=\"width:520px\">"
		+ "The <b>Tags</b>-panel contains tags and genres that apply for the whole bundle."
		+"</div></body></html>";

	public static final String bundleFiles = "<html><body><div style=\"width:520px\">"
		+ "In the <b>Files</b>-panel you can specifiy supplemented files like front- or backcover or booklet for the <b>bundle</b>."
		+"</div></body></html>";

	
//--- End of BundlePanel --------------------------------------------
	
	
//--- ItemsPanel --------------------------------------------
	
//--- End of Items Panel --------------------------------------------

	
}
