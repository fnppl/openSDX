package org.fnppl.opensdx.securesocket;

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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Vector;

import org.bouncycastle.crypto.RuntimeCryptoException;
import org.fnppl.opensdx.security.AsymmetricKeyPair;
import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.security.SymmetricKey;

public class OSDXSocketReceiver {

	private InputStream intputStream;
	private SymmetricKey agreedEncryptionKey = null;
	private OSDXSocketLowLevelDataHandler dataHandler = null;
	private boolean isServerReceiver;
	private OSDXSocketSender sender;
	private boolean run = true;
	
	public static OSDXSocketReceiver initServerReceiver(InputStream input, OSDXSocketSender sender, OSDXSocketLowLevelDataHandler dataHandler) {
		return new OSDXSocketReceiver(input, sender, true, dataHandler);
	}
	public static OSDXSocketReceiver initClientReceiver(InputStream input, OSDXSocketSender sender, OSDXSocketLowLevelDataHandler dataHandler) {
		return new OSDXSocketReceiver(input, sender, false, dataHandler);
	}
	
	private OSDXSocketReceiver(InputStream input, OSDXSocketSender sender, final boolean isServerReceiver, OSDXSocketLowLevelDataHandler dataHandler) {
		this.dataHandler = dataHandler;
		this.isServerReceiver = isServerReceiver;
		intputStream = input;
		agreedEncryptionKey = null; //start without encryption
		this.sender = sender;
		Thread t = new Thread() {
			public void run() {
				if (isServerReceiver) {
					waitForClientInitMsg();
				} else {
					waitForServerInitMsg();
				}
				if (agreedEncryptionKey!=null) {
					while (run) {
						receiveData();
					}
				}
				System.out.println("Receiver stopped");
			}
		};
		t.start();
	}
	
	public void stop() {
		run = false;
	}
	
	public boolean isRunning() {
		return run;
	}
	
	public void setEncryptionKey(SymmetricKey agreedEncryptionKey) {
		this.agreedEncryptionKey = agreedEncryptionKey;
	}
	
	private void waitForServerInitMsg() {
		try {
			String[] lines = new String[7];
			int count = 0;
			while (count<7) {
				final StringBuffer lineBuffer = new StringBuffer();
				byte[] b = new byte[1];
				int read;
				boolean lineNotComplete = true;
				while (lineNotComplete) {
					read = intputStream.read(b); //this one blocks
					if (read>0) {
						if (b[0] == '\n') { // data starts after :
							lineNotComplete = false;
						} else {
							lineBuffer.append((char)b[0]);
						}
					}
				}	
				lines[count] = lineBuffer.toString();
				//System.out.println((count+1)+" :: "+lines[count]);
				count++;
			}
			byte[] data = null;
			if (lines[6].startsWith("ENC ")) {
				int byteCount = Integer.parseInt(lines[6].substring(4));
				if (byteCount>0) {
					//read bytes
					ByteArrayOutputStream bout = new ByteArrayOutputStream();
					//byte[] data = new byte[byteCount];
					int bufferSize = 1024;
					byte[] buffer = new byte[bufferSize];
					byte[] b = new byte[1];
					int read;
					int sum = 0;
					while (sum<byteCount) { //block until complete data is read
						if (byteCount-sum > bufferSize) {
							read = intputStream.read(buffer);
							if (read>0) {
								bout.write(buffer, 0, read);
								sum += read;
							}
						} else { //avoid reading next command
							read = intputStream.read(b);
							if (read>0) {
								bout.write(b, 0, read);
								sum += read;
							}
						}
					}
					data = bout.toByteArray();
				}
			}
			if (dataHandler!=null) {
				dataHandler.handleNewInitMsg(lines, data, sender);
			}	
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void waitForClientInitMsg() {
		try {
		String[] lines = new String[8];
		int count = 0;
		while (count<8) {
			final StringBuffer lineBuffer = new StringBuffer();
			byte[] b = new byte[1];
			int read;
			boolean lineNotComplete = true;
			while (lineNotComplete) {
				read = intputStream.read(b); //this one blocks
				if (read>0) {
					if (b[0] == '\n') { // data starts after :
						lineNotComplete = false;
					} else {
						lineBuffer.append((char)b[0]);
					}
				}
			}	
			lines[count] = lineBuffer.toString();
			count++;
		}
		if (dataHandler!=null) {
			dataHandler.handleNewInitMsg(lines, null, sender);
		}
	} catch (Exception ex) {
		ex.printStackTrace();
	}
	}
	
	private static byte TYPE_TEXT = 84;
	private static byte TYPE_DATA = 68;
	private void receiveData() {
		try {
			final StringBuffer commandBuffer = new StringBuffer();
			byte[] b = new byte[1];
			int read;
			boolean commandNotComplete = true;
			while (commandNotComplete) {
				read = intputStream.read(b); //this one blocks
				if (read>0) {
					if (b[0] == '\n') { // data starts after :
						commandNotComplete = false;
					} else {
						commandBuffer.append((char)b[0]);
					}
				}
			}
			String command = commandBuffer.toString();
			//System.out.println("receiving bytes: "+command);
			
			int byteCount = Integer.parseInt(command);
			
			if (byteCount>0) {
				//read bytes
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				//byte[] data = new byte[byteCount];
				int bufferSize = 1024;
				byte[] buffer = new byte[bufferSize];
				
				int sum = 0;
				while (sum<byteCount) { //block until complete data is read
					if (byteCount-sum > bufferSize) {
						read = intputStream.read(buffer);
						if (read>0) {
							bout.write(buffer, 0, read);
							sum += read;
						}
					} else { //avoid reading next command
						read = intputStream.read(b);
						if (read>0) {
							bout.write(b, 0, read);
							sum += read;
						}
					}
				}
				byte[] data = bout.toByteArray();
				
				//assert(data.length==byteCount);
				
				//decrypt
				try {
					if (agreedEncryptionKey!=null) {
						data = agreedEncryptionKey.decrypt(data);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			
				if (data[0] == TYPE_TEXT) {
					String text = new String(Arrays.copyOfRange(data, 1, data.length), "UTF-8");
					//System.out.println("RECEIVED MESSAGE::"+text);
					if (dataHandler!=null) {
						dataHandler.handleNewText(text, sender);
					}
				} else if (data[0] == TYPE_DATA) {
					if (dataHandler!=null) {
						dataHandler.handleNewData(Arrays.copyOfRange(data, 1, data.length), sender);
					}
				} else {
					System.out.println("ERROR in received package!");
				}
			}
		} catch (IOException e) {
			if (e.getMessage().startsWith("Socket closed") || e.getMessage().startsWith("Connection reset")) {
				stop();
			} else {
				e.printStackTrace();
			}
		}
	}
}

