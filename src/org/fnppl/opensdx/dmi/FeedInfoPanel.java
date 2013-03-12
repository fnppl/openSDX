package org.fnppl.opensdx.dmi;
/*
 * Copyright (C) 2010-2013 
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


import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.fnppl.opensdx.common.Feed;
import org.fnppl.opensdx.gui.EditBusinessObjectTree;

public class FeedInfoPanel extends JPanel {
	private JPanel mainContent = null;
	
	public FeedInfoPanel() {
		super();
		buildUi();
	}
	
	
	public void update() {
		System.out.println("FeedInfoPanel::update");
		Feed feed = FeedGui.getInstance().getCurrentFeed();
		if (feed != null && feed.getFeedinfo()!=null) {
			EditBusinessObjectTree tree = new EditBusinessObjectTree(feed.getFeedinfo());
			System.out.println("feedinfo not null");
			int anz = mainContent.getComponentCount();
			System.out.println("anz comp: "+anz);
			mainContent.removeAll();
			mainContent.add(new JScrollPane(tree),BorderLayout.CENTER);
		} else {
			mainContent.removeAll();
			//mainContent.add(new JButton("no feedinfo"), BorderLayout.CENTER);
		}
	}
	
//	private JPanel makeMainBlock() {
//		JPanel ret = new JPanel();
////		<onlytest>true</onlytest><!-- MUST testmode true|false -->
////		<feedid>kaakka</feedid><!-- MUST id is provided by sender; should be unique -->
////		<creationdatetime>2010-10-01 00:00:00 GMT+00:00</creationdatetime><!-- MUST datetime of creation of this feed -->
////		<effectivedatetime>2010-10-01 00:00:00 GMT+00:00</effectivedatetime><!-- MUST ; most probably the same as creationdate ; datetime when this feed may be come ACTIVE as earliest; mainly used for deferred update-feeds with an effective-date not *asap* -->
//		return ret;
//	}
	
	private void buildUi() {
		System.out.println("FeedInfoPanel::buildUi");
		setLayout(new BorderLayout());
		mainContent = new JPanel();
		mainContent.setLayout(new BorderLayout());
		add(mainContent, BorderLayout.CENTER);
		update();
		
		
//		GridBagLayout gb = new GridBagLayout();
//		GridBagConstraints c = new GridBagConstraints();
//		c.gridx = 0; c.gridy = 0;
//		c.anchor = GridBagConstraints.NORTHWEST;
//		
//		setLayout(gb);
//		
//		JLabel l = new JLabel("Me is FeedInfoPanel.java");
//		add(l, c);
//		
//
//		c.gridy++;
//		c.gridx++;
//		JLabel filler = new JLabel(); //invisible
//		c.weightx = 1.0;
//		c.weighty = 1.0;
//		add(filler, c);
//		
//		makeMainBlock();
//		
		
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
}
