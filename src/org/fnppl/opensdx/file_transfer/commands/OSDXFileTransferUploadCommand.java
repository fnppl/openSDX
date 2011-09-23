package org.fnppl.opensdx.file_transfer.commands;
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
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

import org.fnppl.opensdx.common.Util;
import org.fnppl.opensdx.file_transfer.OSDXFileTransferClient;
import org.fnppl.opensdx.file_transfer.SecureConnection;
import org.fnppl.opensdx.security.SecurityHelper;

public class OSDXFileTransferUploadCommand extends OSDXFileTransferCommand {

	private File file = null;
	private String remoteName = null;
	private long filePos = 0;
	private long fileLen = 0;
	private FileInputStream fileIn = null;

	private long maxFilelengthForMD5 = 10*1024*1024L; //10 MB
	private boolean hasNext = true;
	private OSDXFileTransferClient client = null;

	public OSDXFileTransferUploadCommand(long id, File file, String absolutePathname, OSDXFileTransferClient client) {
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
	
	private byte[] data = null;
	
	public OSDXFileTransferUploadCommand(long id,byte[] data, String absolutePathname, OSDXFileTransferClient client) {
		super();
		fileLen = data.length;
		byte[] md5 = null;
		if (fileLen<maxFilelengthForMD5) {
			try {
				md5 = SecurityHelper.getMD5(data);
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
		this.file = null;
		this.remoteName = absolutePathname;
		this.id = id;
	}
	
	public void onProcessStart() throws Exception {
		if (DEBUG) System.out.println("Command upload start.");
		hasNext = true;
		if (fileLen>0 && data!=null) {
			fileIn = new FileInputStream(file);
		}
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
		if (!SecureConnection.isError(code)) {
			if (code == SecureConnection.TYPE_ACK) {
				System.out.println("ACK upload of file: "+file.getAbsolutePath()+" -> "+remoteName);
			}
			else if (code == SecureConnection.TYPE_ACK_COMPLETE) {
				try {
					fileIn.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				notifySucces();
			}
		} else {
			//stop upload
			hasNext = false;
			if (client!=null) {
				client.removeCommandFromInProgress(id);
			}
			if (data!=null) {
				try {
					fileIn.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			notifyError(getMessageFromContent(content));
		}
	}
	
	public void onSendNextPackage(SecureConnection con) throws Exception {
		if (filePos<0) {
			con.setCommand(id, command);
			filePos = 0L;
			num = 1;
			notifyUpdate(filePos, fileLen, null);
			if (fileLen<=0) {
				hasNext = false;
				notifyUpdate(filePos, fileLen, null);
			}
		} else {
			byte[] content = new byte[maxPacketSize];
			if (data!=null) {
				int read = fileIn.read(content);
				if (read<maxPacketSize) {
					con.setData(id, num, Arrays.copyOf(content, read));
				} else {
					con.setData(id, num, content);
				}
				filePos += read;
				notifyUpdate(filePos, fileLen, null);
				if (filePos>=fileLen) {
					hasNext = false;
				}
				num++;
			} else {
				//TODO 
			}
			
		}
		con.sendPackage();
	}

}
