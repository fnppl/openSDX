package org.fnppl.opensdx.file_transfer.commands;
/*
 * Copyright (C) 2010-2012 
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
import org.fnppl.opensdx.file_transfer.SecureConnection;
import org.fnppl.opensdx.file_transfer.model.RemoteFile;
import org.fnppl.opensdx.helper.Logger;

public class OSDXFileTransferFileInfoCommand extends OSDXFileTransferCommand {


	private boolean hasNext = true;
	private RemoteFile rf = null;
	
	public OSDXFileTransferFileInfoCommand(long id, String absolutePathname) {
		super();
		this.command = "FILE "+absolutePathname;
		this.id = id;
	}
	
	public void onProcessStart() throws Exception {
		if (DEBUG) System.out.println("Command fileinfo started id="+id);
		//hasNext = true;
	}
	
	public void onProcessCancel() {

	}

	public void onProcessEnd() {
		if (DEBUG) System.out.println("Command fileinfo end.");
	}
	
	public void onResponseReceived(int num, byte code, byte[] content) throws Exception {
		if (!SecureConnection.isError(code)) {
			rf = RemoteFile.fromParamString(getMessageFromContentNN(content));
			notifySucces();
		} else {
			notifyError(getMessageFromContent(content));
		}
	}

	public boolean hasNextPackage() {
		return hasNext;
	}

	public void onSendNextPackage(SecureConnection con) throws Exception {
		con.setCommand(id, command);
		if (DEBUG) {
			System.out.println("SENDING :: "+command);
			Logger.getFileTransferLogger().logMsg("SEND CMD: "+command);
		}
		hasNext = false;
		con.sendPackage();
	}

	public RemoteFile getRemoteFile() {
		return rf;
	}

}
