package org.fnppl.opensdx.file_transfer.commands;
/*
 * Copyright (C) 2010-2015 
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

import org.fnppl.opensdx.common.Util;
import org.fnppl.opensdx.file_transfer.SecureConnection;
import org.fnppl.opensdx.file_transfer.helper.RightsAndDuties;
import org.fnppl.opensdx.helper.Logger;
import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.xml.Document;

public class OSDXFileTransferUserPassLoginCommand extends OSDXFileTransferCommand {


	private boolean hasNext = true;
	private RightsAndDuties rightsAndADuties = null;

	private String pass = null;
	public OSDXFileTransferUserPassLoginCommand(long id, String username, String pass) {
		super();		
		this.command = "USERPASSLOGIN "+username+"\t"+getUserPassAuth(username, pass);
		this.id = id;
		this.pass = pass;
	}
	public static String getUserPassAuth(String user, String pass) {
		//see ClientSettings.getUserPassAuth
		return SecurityHelper.HexDecoder.encode(SecurityHelper.getMD5((user+"\0"+pass).getBytes()));
	}
	public void onProcessStart() throws Exception {
	//	if (DEBUG) System.out.println("Command login started.");
		//hasNext = true;
	}
	
	public void onProcessCancel() {

	}

	public void onProcessEnd() {
	//	if (DEBUG) System.out.println("Command login end.");
	}
	
	public void onResponseReceived(int num, byte code, byte[] content) throws Exception {
		if (code == SecureConnection.TYPE_ACK) {
			String msg = new String(content,"UTF-8");
			String[] param = Util.getParams(msg);
			try {
				rightsAndADuties = RightsAndDuties.fromElement(Document.fromString(param[1]).getRootElement(), -1);
				notifySucces();
			} catch (Exception ex) {
				ex.printStackTrace();
				rightsAndADuties = null;
				notifyErrorFromContent(getMessageFromContent(content));
			}
		}
		else if (SecureConnection.isError(code)) {
			notifyErrorFromContent(getMessageFromContent(content));
		}
	}
	
	public RightsAndDuties getRightsAndDuties() {
		return rightsAndADuties;
	}

	public boolean hasNextPackage() {
		return hasNext;
	}

	public void onSendNextPackage(SecureConnection con) throws Exception {
		con.setCommand(id, command);
		if (DEBUG) {
			//System.out.println("SENDING :: "+command);
			Logger.getFileTransferLogger().logMsg("SEND CMD: "+command);
		}
		hasNext = false;
		con.sendPackage();
	}


}
