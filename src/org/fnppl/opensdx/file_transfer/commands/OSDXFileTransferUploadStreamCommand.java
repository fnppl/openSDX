package org.fnppl.opensdx.file_transfer.commands;
/*
 * Copyright (C) 2010-2012 
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
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

import org.fnppl.opensdx.common.Util;
import org.fnppl.opensdx.file_transfer.OSDXFileTransferClient;
import org.fnppl.opensdx.file_transfer.SecureConnection;
import org.fnppl.opensdx.file_transfer.errors.OSDXErrorCodes;
import org.fnppl.opensdx.helper.Logger;
import org.fnppl.opensdx.security.MD5;
import org.fnppl.opensdx.security.SecurityHelper;

public class OSDXFileTransferUploadStreamCommand extends OSDXFileTransferCommand {

	private String remoteName = null;
	private long filePos = 0L;
	
	private boolean hasNext = true;
	private OSDXFileTransferClient client = null;
	private boolean eof = false;
	private boolean startUpload = false;
	private MD5 md5 = new MD5();
	private boolean resume = false;
	
	private BufferedInputStream in = null;
	
	
	public OSDXFileTransferUploadStreamCommand(long id, String absoluteRemotePath, BufferedInputStream in, OSDXFileTransferClient client) {
		super();
		this.filePos = 0L;
		this.client = client;
		this.remoteName = absoluteRemotePath;
		this.id = id;
		this.in = in;
		String[] param;
		param = new String[] {remoteName};
		this.command = "PUT "+Util.makeParamsString(param);
		
	}
	
	public OSDXFileTransferUploadStreamCommand(long id, String absoluteRemotePath, BufferedInputStream in, boolean resume, OSDXFileTransferClient client) {
		super();
		this.filePos = 0L;
		this.client = client;
		this.remoteName = absoluteRemotePath;
		this.id = id;
		this.in = in;
		String[] param;
		param = new String[] {remoteName};
		this.resume = resume;
		if (resume) {
			this.command = "PUT_RESUME "+Util.makeParamsString(param);
		} else {
			this.command = "PUT "+Util.makeParamsString(param);
		}
	}
	
	
	public void onProcessStart() throws Exception {
		//if (DEBUG) System.out.println("Command upload start.");
		hasNext = true;
		
	}
	
	public void onProcessCancel() {
		//stop upload
		hasNext = false;
		if (client!=null) {
			client.removeCommandFromInProgress(id);
		}
		try {
			in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void onProcessEnd() {
		//if (DEBUG) System.out.println("Command upload end.");
	}

	public boolean hasNextPackage() {
		return hasNext;
	}

	public void onResponseReceived(int num, byte code, byte[] content) throws Exception {
		if (!SecureConnection.isError(code)) {
			if (code == SecureConnection.TYPE_ACK) {
				//System.out.println("ACK start upload of file: "+remoteName);
				if (resume) {
					String[] param = Util.getParams(getMessageFromContentNN(content));
					if (param.length>=2) {
						long transferred = Long.parseLong(param[0]);
						String md5String = param[1];
						MD5 md5Local = new MD5();
						//System.out.println("Resuming upload at pos "+transferred+"  md5 given = "+md5String);
						filePos = 0L;
						//get md5String
						byte[] buf = new byte[maxPacketSize];
						
						int nextPackSize = (int)(transferred-filePos);
						if (nextPackSize>maxPacketSize) {
							nextPackSize = maxPacketSize;
						}
						while (nextPackSize>0) {
							if (nextPackSize<maxPacketSize) {
								buf = new byte[nextPackSize];
							}
							int read = in.read(buf);
							if (read>0) {
								md5.update(buf,read);
								md5Local.update(buf,read);
								filePos += read;
							}
							
							nextPackSize = (int)(transferred-filePos);
							if (nextPackSize>maxPacketSize) {
								nextPackSize = maxPacketSize;
							}
						}
						//compare md5
						byte[] my_md5 = md5Local.getMD5bytes(); //can only read the md5 bytes one time !!!
						byte[] your_md5 = SecurityHelper.HexDecoder.decode(md5String);
						if (!Arrays.equals(my_md5,your_md5)) {
							//System.out.println("MD5 check failed for resuming upload");
							notifyError(OSDXErrorCodes.UPLOAD_ERROR_MD5_CHECK_FAIL);
							//OLD: notifyError("MD5 check failed for resuming upload");
							
							hasNext = false;
							startUpload = false;
						}
					}
				}
				startUpload = true;
				goOn = true;
			}
			else if (code == SecureConnection.TYPE_ACK_COMPLETE) {
				try {
					in.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				//System.out.println("notify success of "+remoteName);
				notifySucces();
			}
		}
		else {
			//stop upload
			hasNext = false;
			if (client!=null) {
				client.removeCommandFromInProgress(id);
			}
			try {
				in.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			//System.out.println("notifyError: "+getMessageFromContent(content));
			notifyErrorFromContent(getMessageFromContent(content));
		}
	}
	
	private byte[] buffer = new byte[maxPacketSize];
	
	public void onSendNextPackage(SecureConnection con) throws Exception {
		if (!startUpload) {
			con.setCommand(id, command);
			if (DEBUG) {
				//System.out.println("SENDING :: "+command);
				Logger.getFileTransferLogger().logMsg("SEND CMD: "+command);
			}
			//filePos = 0L;
			num = 1;
			goOn = false; //wait for ACK from Server
		}
		else {
			int read = in.read(buffer);
			if (read<0) {
				//end of file
				eof = true;
				String md5String = SecurityHelper.HexDecoder.encode(md5.getMD5bytes());
				String[] param = new String[] {""+filePos, md5String};
				//System.out.println("PUT_EOF "+Util.makeParamsString(param));
				con.setCommand(id, "PUT_EOF "+Util.makeParamsString(param));
				hasNext = false;
			}
			else if (read<maxPacketSize) {
				con.setData(id, num, Arrays.copyOf(buffer, read));
				md5.update(buffer, read);
				filePos += read;
			}
			else {
				con.setData(id, num, buffer);
				md5.update(buffer);
				filePos += read;
			}
			num++;
			notifyUpdate(filePos, Long.MAX_VALUE, null);
		}
		con.sendPackage();
	}
	
	public long getFilePos() {
		return filePos;
	}

}
