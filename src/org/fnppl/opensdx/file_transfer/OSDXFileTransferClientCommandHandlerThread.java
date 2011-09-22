package org.fnppl.opensdx.file_transfer;

import java.util.Vector;

import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferCommand;
import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferCloseConnectionCommand;


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
