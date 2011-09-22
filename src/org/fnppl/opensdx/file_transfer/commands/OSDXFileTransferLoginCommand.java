package org.fnppl.opensdx.file_transfer.commands;


import org.fnppl.opensdx.common.Util;
import org.fnppl.opensdx.file_transfer.SecureConnection;
import org.fnppl.opensdx.file_transfer.helper.RightsAndDuties;
import org.fnppl.opensdx.xml.Document;

public class OSDXFileTransferLoginCommand extends OSDXFileTransferCommand {


	private boolean hasNext = true;
	private RightsAndDuties rightsAndADuties = null;

	public OSDXFileTransferLoginCommand(long id, String username) {
		super();
		this.command = "LOGIN "+username;
		this.id = id;
	}
	
	public void onProcessStart() throws Exception {
		if (DEBUG) System.out.println("Command login started.");
		//hasNext = true;
	}
	
	public void onProcessCancel() {

	}

	public void onProcessEnd() {
		if (DEBUG) System.out.println("Command login end.");
	}
	
	public void onResponseReceived(int num, byte code, byte[] content) throws Exception {
		if (code == SecureConnection.TYPE_ACK) {
			String msg = new String(content,"UTF-8");
			String[] param = Util.getParams(msg);
			try {
				rightsAndADuties = RightsAndDuties.fromElement(Document.fromString(param[1]).getRootElement());
			} catch (Exception ex) {
				ex.printStackTrace();
				rightsAndADuties = null;
			}
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
		if (DEBUG) System.out.println("SENDING :: "+command);
		hasNext = false;
		con.sendPackage();
	}


}
