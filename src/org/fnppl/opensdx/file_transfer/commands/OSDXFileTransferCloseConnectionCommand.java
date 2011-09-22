package org.fnppl.opensdx.file_transfer.commands;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

import org.fnppl.opensdx.common.Util;
import org.fnppl.opensdx.file_transfer.SecureConnection;
import org.fnppl.opensdx.file_transfer.helper.RightsAndDuties;
import org.fnppl.opensdx.xml.Document;

public class OSDXFileTransferCloseConnectionCommand extends OSDXFileTransferCommand {

	private boolean hasNext = true;

	public OSDXFileTransferCloseConnectionCommand(long id) {
		super();
		this.command = "QUIT";
		this.id = id;
	}
	
	public void onProcessStart() throws Exception {
		if (DEBUG) System.out.println("Command close started.");
		hasNext = true;
	}
	
	public void onProcessCancel() {

	}

	public void onProcessEnd() {
		if (DEBUG) System.out.println("Command close end.");
	}

	public boolean hasNextPackage() {
		return hasNext;
	}

	public void onSendNextPackage(SecureConnection con) throws Exception {
		con.setCommand(id, command);
		if (DEBUG) System.out.println("SENDING :: "+command);
		hasNext = false;
		con.sendPackage();
	}

	public void onResponseReceived(int num, byte code, byte[] content) throws Exception {
		
	}
}
