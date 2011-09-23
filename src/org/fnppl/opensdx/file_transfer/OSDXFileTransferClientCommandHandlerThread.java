package org.fnppl.opensdx.file_transfer;
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
import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferCloseConnectionCommand;
import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferCommand;


public class OSDXFileTransferClientCommandHandlerThread extends Thread {

	private OSDXFileTransferClient client;
	private SecureConnection dataOut;
	
	public OSDXFileTransferClientCommandHandlerThread(OSDXFileTransferClient client, SecureConnection dataOut) {
		this.client = client;
		this.dataOut = dataOut;
	}
	
	private boolean run = true;
	
	
	public void abortAllCommands() {
		
	}
	
	public void run() {
		run = true;
		while (run) {
			//System.out.println("COMMAND HANDLER :: get next command");
			OSDXFileTransferCommand command = client.getNextCommand();
			if (command!=null) {
				try {
					// -- run command -- 
					command.startProcessing();
					while (command.hasNextPackage()) {
						//System.out.println("has next: "+command.hasNextPackage());
						//System.out.println("COMMAND HANDLER :: send next package");
						command.sendNextPackage(dataOut);	
					}
					// -- run command end -- 
					if (command instanceof OSDXFileTransferCloseConnectionCommand) {
						client.closeConnectionDirectly();
					}
				} catch (Exception exCommand) {
					exCommand.printStackTrace();
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} else {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void close() {
		run = false;
	}
	
}