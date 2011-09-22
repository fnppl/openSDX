package org.fnppl.opensdx.file_transfer.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.fnppl.opensdx.common.Util;
import org.fnppl.opensdx.file_transfer.SecureConnection;
import org.fnppl.opensdx.file_transfer.helper.RightsAndDuties;
import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.xml.Document;

public class OSDXFileTransferUploadCommand extends OSDXFileTransferCommand {

	private File file = null;
	private String remoteName = null;
	private long filePos = 0;
	private long fileLen = 0;
	private FileInputStream fileIn = null;

	private long maxFilelengthForMD5 = 10*1024*1024L; //10 MB
	private boolean hasNext = true;


	public OSDXFileTransferUploadCommand(long id, File file, String absolutePathname) {
		super();
		fileLen = file.length();
		byte[] md5 = null;
		if (fileLen<maxFilelengthForMD5) {
			try {
				md5 = SecurityHelper.getMD5(file);
			} catch (Exception e) {
				System.out.println("Error calculating md5 hash of "+file.getAbsolutePath());
				e.printStackTrace();
				md5 = null;
			}
		}
		String[] param;
		if (md5!=null) {
			param = new String[] {absolutePathname,""+fileLen,SecurityHelper.HexDecoder.encode(md5)};
		} else {
			param = new String[] {absolutePathname,""+fileLen};
		}
		this.command = "PUT "+Util.makeParamsString(param);
		this.filePos = -1L;
		this.file = file;
		this.remoteName = absolutePathname;
		this.id = id;
	}
	
	public void onProcessStart() throws Exception {
		if (DEBUG) System.out.println("Command upload start.");
		hasNext = true;
		fileIn = new FileInputStream(file);
	}
	
	public void onProcessCancel() {

	}

	public void onProcessEnd() {
		if (DEBUG) System.out.println("Command upload end.");
	}

	public boolean hasNextPackage() {
		return hasNext;
	}

	public void onResponseReceived(int num, byte code, byte[] content) throws Exception {
		if (code == SecureConnection.TYPE_ACK) {
			//String msg = new String(content,"UTF-8");
			System.out.println("ACK upload of file: "+file.getAbsolutePath()+" -> "+remoteName);
		}
		else if (SecureConnection.isError(code)) {
			//stop upload
			hasNext = false;
			try {
				fileIn.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		else if (code == SecureConnection.TYPE_ACK_COMPLETE) {
			try {
				fileIn.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public void onSendNextPackage(SecureConnection con) throws Exception {
		if (filePos<0) {
			con.setCommand(id, command);
			filePos = 0L;
			num = 1;
		} else {
			byte[] content = new byte[maxPacketSize];
			int read = fileIn.read(content);
			if (read<maxPacketSize) {
				con.setData(id, num, Arrays.copyOf(content, read));
			} else {
				con.setData(id, num, content);
			}
			filePos += read;
			if (filePos>=fileLen) {
				hasNext = false;
			}
			num++;
		}
		con.sendPackage();
	}

}
