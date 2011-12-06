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
public class FeedGuiTooltips {

	public static final String sender = 
		"<html><body>The <b>Sender</b> part should give information about the sender of the feed (typically you / your company),<br />"
		+" where the <b>Key ID</b> should be related to the key that will be used by the sender in further correspondence.<br />"
		+"The <b>ID</b> field should hold the id on the receiver side, the <b>Our ID</b> field should hold the ID of the sender side.</body></html>";
	
	public static final String licensor =
		"<html><body>The <b>Licensor</b> part should give information about the licensor (the one who holds the license) of the feeds content (typically you / your company),<br />"
		+" where the <b>Key ID</b> should be related to the key that will be used by the licensor in further correspondence.<br />"
		+"The <b>ID</b> field should hold the id on the receiver side, the <b>Our ID</b> field should hold the ID of the sender side.</body></html>";
	
	public static final String licensee = 
		"<html><body>The <b>Licensee</b> part should give information about the licensee (the one to whom the license is granted to) of the feeds content (typically the receiver),<br />"
		+" where the <b>Key ID</b> should be related to the key that will be used by the licensee in further correspondence.<br />"
		+"The <b>ID</b> field should hold the id on the receiver side, the <b>Our ID</b> field should hold the ID of the sender side.</body></html>";
	
	public static final String creator = 
		"<html><body>The <b>Creator</b> part should give information about the creator of the feed (typically you / your company),<br />"
		+" where the <b>Key ID</b> should be related to the key that will be used by the creator in further correspondence.<br />"
		+"The <b>User ID</b> field could specify an id of the creator.</body></html>";
	
	public static final String receiver = 
		"<html><body>The <b>Receiver</b> part should give information about the receiver (the one to whom the feed will be delivered) of the feeds content.<br />"
		+" Typically the <b>type</b> will be an <b>openSDX fileserver</b> where you need to enter the servername (e.g. <i>simfy.finetunes.net</i>) and the login data<br />"
		+" (<b>Key ID</b> and <b>username</b>) you agreed on with the operator of the fileserver. The <b>server IPv4</b> field will automatically be filled when pressing the ENTER key<br />"
		+" in the <b>servername</b> field.<br />"
	    +" The button <b>beam me up!</b> will open another window where the feed and all referenced media files could be send to the given receiver.</body></html>";
	
	public static final String triggered_actions = "";
	
}
