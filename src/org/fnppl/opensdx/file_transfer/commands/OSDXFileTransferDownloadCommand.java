package org.fnppl.opensdx.file_transfer.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.fnppl.opensdx.common.Util;
import org.fnppl.opensdx.file_transfer.SecureConnection;
import org.fnppl.opensdx.file_transfer.helper.RightsAndDuties;
import org.fnppl.opensdx.file_transfer.model.RemoteFile;
import org.fnppl.opensdx.xml.Document;

public class OSDXFileTransferDownloadCommand extends OSDXFileTransferCommand {
	
	private RemoteFile remote;
	private File localfile = null;
	private long filePos = 0;
	private long fileLen = 0;
	private int nextNum = 0;
	private byte[] md5 = null;
	
	private FileOutputStream fileOut = null;

	private boolean hasNext = true;


	public OSDXFileTransferDownloadCommand(long id, RemoteFile remote, File localfile) {
		super();
		this.command = "GET "+remote.getFilnameWithPath();
		this.fileLen = remote.getLength();
		this.filePos = 0L;
		this.localfile = localfile;
		this.id = id;
	}
	
	public void onProcessStart() throws Exception {
		hasNext = true;
	}
	
	public void onProcessCancel() {
		try {
			fileOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void onProcessEnd() {
		
	}

	public boolean hasNextPackage() {
		return hasNext;
	}

	public void onResponseReceived(int num, byte code, byte[] content) throws Exception {
		if (num==0) {
			if (code == SecureConnection.TYPE_ACK) {
				fileOut = new FileOutputStream(localfile);
				md5 = null;
			}
			else if (code == SecureConnection.TYPE_ACK_WITH_MD5) {
				fileOut = new FileOutputStream(localfile);
				md5 = content;
			}
		}
		else if (code == SecureConnection.TYPE_DATA){
			fileOut.write(content);
			filePos += content.length;
			if (filePos>=fileLen) {
				try {
					fileOut.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (filePos>fileLen) {
					System.out.println("ERROR wrong filesize.");
				} else if (md5!=null){
					//check md5
					//TODO
				}
			}
		}
	}
	
	public void onSendNextPackage(SecureConnection con) throws Exception {
		if (filePos<0) {
			con.setCommand(id, command);
			filePos = 0L;
			num = 1;
		}
		con.sendPackage();
	}

}
