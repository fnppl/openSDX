package org.fnppl.opensdx.file_transfer;

import java.net.SocketException;

public class OSDXFileTransferClientReceiverThread extends Thread {

	
	private SecureConnection con;
	private OSDXFileTransferClient client;
	private boolean run = true;
	
	public OSDXFileTransferClientReceiverThread(OSDXFileTransferClient client, SecureConnection con) {
		this.client = client;
		this.con = con;
	}
	
	public void run() {
		while(run) {
			try {
				run = con.receiveNextPackage();
				if (run) {
					client.onResponseReceived(con.id, con.num, con.type, con.content);
				}
			} catch (SocketException socketEx) {
				//socket closed -> stopp running
				run = false;
				client.closeConnectionDirectly();
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public void close() {
		run = false;
	}
	
}
