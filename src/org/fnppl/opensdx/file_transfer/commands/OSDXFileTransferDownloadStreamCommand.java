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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.fnppl.opensdx.common.Util;
import org.fnppl.opensdx.file_transfer.OSDXFileTransferClient;
import org.fnppl.opensdx.file_transfer.SecureConnection;
import org.fnppl.opensdx.helper.Logger;
import org.fnppl.opensdx.security.SecurityHelper;

public class OSDXFileTransferDownloadStreamCommand extends OSDXFileTransferCommand {
	
	private String absoluteRemotePath = null;
	private long fileLen = -1L;
	private long filePos = -1L;
	private byte[] md5 = null;
	
	private BufferedOutputStream fileOut = null;

	private boolean hasNext = true;
	private OSDXFileTransferClient client;

	public OSDXFileTransferDownloadStreamCommand(long id, String absoluteRemotePath, BufferedOutputStream fileOut, OSDXFileTransferClient client) {
		super();
		this.client = client;
		this.absoluteRemotePath = absoluteRemotePath;
		this.fileOut = fileOut;
		this.id = id;
		this.command = "GET "+absoluteRemotePath;
		filePos = 0L;
		
	}
	
	public void onProcessStart() throws Exception {
		//System.out.println("Download "+absoluteRemotePath);
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
	
		if (code == SecureConnection.TYPE_ACK) {
			String[] p = Util.getParams(getMessageFromContentNN(content));
			fileLen = Long.parseLong(p[0]);
			System.out.println("filelength = "+fileLen);
			if (p.length==2) {
				try {
					md5 = SecurityHelper.HexDecoder.decode(p[1]);
					System.out.println("md5 = "+p[1]);
				} catch (Exception ex) {
					System.out.println("Warning: could not parse md5 hash: "+p[1]);
					md5 = null;
				}
			}
			if (fileLen == 0) {
				fileOut.close();
				notifyUpdate(filePos, fileLen, null);
				notifySucces();
				
				//release me from progress
				client.removeCommandFromInProgress(id);
			}
		}
		else if (code == SecureConnection.TYPE_DATA && fileLen>0){
			//write content
			fileOut.write(content);
			fileOut.flush();
			filePos += content.length;
			
			//finish if filePos at end
			if (filePos>=fileLen) {
				System.out.println("Download finished.");
				try {
					fileOut.flush();
					fileOut.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (filePos>fileLen) {
					notifyUpdate(filePos, fileLen, null);
					notifyError("Error downloading \""+absoluteRemotePath+"\" :: wrong filesize");
					System.out.println("ERROR wrong filesize.");
				} else {
					if (md5!=null){
						//check md5
						System.out.println("MD5 check for stream downloads not implemented");
						notifyUpdate(filePos, fileLen, null);
						notifySucces();
//						byte[] myMd5 = SecurityHelper.getMD5(localfile);
//						if (Arrays.equals(md5, myMd5)) {
//							System.out.println("MD5 check ok");
//							notifyUpdate(filePos, fileLen, null);
//							notifySucces();
//						} else {
//							System.out.println("MD5 check FAILD!");
//							notifyUpdate(filePos, fileLen, null);
//							notifyError("Error downloading \""+absoluteRemotePath+"\" :: MD5 check FAILD!");
//						}
					} else {
						notifyUpdate(filePos, fileLen, null);
						notifySucces();
					}
				}
				
				//release me from progress
				client.removeCommandFromInProgress(id);
			} else {
				notifyUpdate(filePos, fileLen, null);
			}
		}
	}
	
	public void onSendNextPackage(SecureConnection con) throws Exception {
		hasNext = false;
		con.setCommand(id, command);
		if (DEBUG) {
			System.out.println("SENDING :: "+command);
			Logger.getFileTransferLogger().logMsg("SEND CMD: "+command);
		}
		con.sendPackage();
	}

}
