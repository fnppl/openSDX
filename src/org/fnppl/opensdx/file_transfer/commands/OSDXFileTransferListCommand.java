package org.fnppl.opensdx.file_transfer.commands;

import java.util.Vector;

import org.fnppl.opensdx.file_transfer.SecureConnection;
import org.fnppl.opensdx.file_transfer.model.RemoteFile;

public class OSDXFileTransferListCommand extends OSDXFileTransferCommand {


	private boolean hasNext = true;
	private Vector<RemoteFile> list = null;
	
	public OSDXFileTransferListCommand(long id, String absolutePathname) {
		super();
		this.command = "LIST "+absolutePathname;
		this.id = id;
	}
	
	public void onProcessStart() throws Exception {
		if (DEBUG) System.out.println("Command list started.");
		//hasNext = true;
	}
	
	public void onProcessCancel() {

	}

	public void onProcessEnd() {
		if (DEBUG) System.out.println("Command list end.");
	}
	
	public void onResponseReceived(int num, byte code, byte[] content) throws Exception {
		//TODO parse list
		list = new Vector<RemoteFile>();
		String s = getMessageFromContentNN(content);
		System.out.println("RECEIVED LIST: "+s);
		if (s.length()>0) {
			String[] files = s.split("\n");
			for (int i=0;i<files.length;i++) {
				RemoteFile rf = RemoteFile.fromParamString(files[i]);
				if (rf!=null) {
					list.add(rf);
				}
			}
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

	public  Vector<RemoteFile> getList() {
		return list;
	}
	

}
