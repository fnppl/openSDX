package org.fnppl.opensdx.file_transfer.commands;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

import org.fnppl.opensdx.common.Util;
import org.fnppl.opensdx.file_transfer.SecureConnection;
import org.fnppl.opensdx.file_transfer.helper.RightsAndDuties;
import org.fnppl.opensdx.file_transfer.model.RemoteFile;
import org.fnppl.opensdx.xml.Document;

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
		}
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

	public RemoteFile getRemoteFile() {
		return rf;
	}

}
