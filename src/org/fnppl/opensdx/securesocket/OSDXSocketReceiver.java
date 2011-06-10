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
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import org.fnppl.opensdx.security.SymmetricKey;

public class OSDXSocketReceiver {

	private InputStream intputStream;
	private SymmetricKey agreedEncryptionKey = null;
	private Vector<OSDXSocketDataHandler> listeners = new Vector<OSDXSocketDataHandler>();
	private OSDXSocketSender sender;
	private boolean run = true;
	
	public OSDXSocketReceiver(InputStream input, OSDXSocketSender sender) {
		intputStream = input;
		agreedEncryptionKey = null; //start without encryption
		this.sender = sender;
		Thread t = new Thread() {
			public void run() {
				while (run) {
					receiveData();
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
	public void addEventListener(OSDXSocketDataHandler listener) {
		listeners.add(listener);
	}
	
	public void setEncryptionKey(SymmetricKey agreedEncryptionKey) {
		this.agreedEncryptionKey = agreedEncryptionKey;
	}
	
	public void receiveData() {
		try {
			final StringBuffer commandBuffer = new StringBuffer();
			byte[] b = new byte[1];
			int read;
			boolean commandNotComplete = true;
			while (commandNotComplete) {
				read = intputStream.read(b); //this one blocks
				if (read>0) {
					if (b[0] == 58) { // data starts after :
						commandNotComplete = false;
					} else {
						commandBuffer.append((char)b[0]);
					}
				}
			}	
			String command = commandBuffer.toString();
			
			boolean lastReceivedWasEncrypted = false;
			
			int byteCount = 0;
			if (command.startsWith("TEXT") || command.startsWith("DATA"))  {
				byteCount = Integer.parseInt(command.substring(4));
			}
			else if (command.startsWith("ENCTEXT")||command.startsWith("ENCDATA"))  {
				byteCount = Integer.parseInt(command.substring(7));
				lastReceivedWasEncrypted = true;
			}
			
			if (byteCount>0) {
				//read bytes
				byte[] data = new byte[byteCount];
				intputStream.read(data);
				
				//decrypt if necessary
				if (lastReceivedWasEncrypted) {
					try {
						if (agreedEncryptionKey!=null) {
							data = agreedEncryptionKey.decrypt(data);
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				if (command.contains("TEXT")) {
					String text = new String(data, "UTF-8");
					//System.out.println("RECEIVED MESSAGE::"+lastReceivedText);
					for (OSDXSocketDataHandler l : listeners) {
						l.handleNewText(text, sender);
					}
				} else {
					for (OSDXSocketDataHandler l : listeners) {
						l.handleNewData(data, sender);
					}
				}
			}
		} catch (IOException e) {
			if (e.getMessage().startsWith("Socket closed")) {
				stop();
			} else {
				e.printStackTrace();
			}
		}
	}
}

