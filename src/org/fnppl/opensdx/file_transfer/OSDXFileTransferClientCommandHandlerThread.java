package org.fnppl.opensdx.file_transfer;
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
import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferCloseConnectionCommand;
import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferCommand;
import org.fnppl.opensdx.helper.Logger;


public class OSDXFileTransferClientCommandHandlerThread extends Thread {

	private Logger logger = Logger.getNoLogging();
	
	private OSDXFileTransferClient client;
	private SecureConnection dataOut;
	private OSDXFileTransferCommand command = null;
	
	public OSDXFileTransferClientCommandHandlerThread(OSDXFileTransferClient client, SecureConnection dataOut) {
		this.client = client;
		this.dataOut = dataOut;
	}
	
	private boolean run = true;
	private boolean cancelRequest = false;
	
	public void setLogger(Logger logger) {
		this.logger = logger;
	}
	
	public void abortCommand() {
		if (command != null) {
			cancelRequest = true;
		}
	}
	
	public void run() {
		run = true;
		while (run) {
			//System.out.println("COMMAND HANDLER :: get next command");
			command = client.getNextCommand();
			if (command!=null) {
				try {
					// -- run command -- 
					command.startProcessing();
					while (command.hasNextPackage() && !cancelRequest) {
						//System.out.println("has next: "+command.hasNextPackage());
						//System.out.println("COMMAND HANDLER :: send next package");
						if (command.goOn()) {
							command.sendNextPackage(dataOut);
						} else {
							sleep(50);
						}
					}
					if (cancelRequest) {
						command.cancelProcessing();
					}
					// -- run command end -- 
					if (command instanceof OSDXFileTransferCloseConnectionCommand) {
						client.closeConnectionDirectly();
						close();
					}
				} catch (Exception exCommand) {
					String msg = exCommand.getMessage();
					if (msg!=null) {
						msg = msg.toLowerCase();
						if (msg.startsWith("broken pipe") || msg.startsWith("socket closed")) {
							System.out.println("Bropen Pipe :: closing connection.");
							client.alertBrokenPipe();
							close();
						} else {
							exCommand.printStackTrace();
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					} else {
						exCommand.printStackTrace();
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				cancelRequest = false;
				command = null;
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
