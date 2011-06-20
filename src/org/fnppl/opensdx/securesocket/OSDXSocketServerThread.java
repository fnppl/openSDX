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
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import org.fnppl.opensdx.security.OSDXKey;
import org.fnppl.opensdx.security.OSDXMessage;
import org.fnppl.opensdx.security.Result;
import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.security.SymmetricKey;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;

public class OSDXSocketServerThread extends Thread implements OSDXSocketSender, OSDXSocketDataHandler {

	public final static String ERROR_NO_RESPONSE = "ERROR: server does not respond.";
	public final static String ERROR_WRONG_RESPONE_FORMAT = "ERROR: Wrong format in uploadserver's response.";
	
	private long timeout = 10000;
	
	private Socket socket;
	private OSDXKey mySigningKey = null;
	private OSDXKey myEncryptionKey = null;
	private SymmetricKey agreedEncryptionKey = null;
	private String message = null;
	private boolean secure_connection_established = false;
	private String userID = "unknown_user";

	private OSDXSocketDataHandler dataHandler = null;
	private OSDXSocketReceiver receiver = null;
	private long nextTimeOut = Long.MAX_VALUE;
	
	public OSDXSocketServerThread(Socket socket, OSDXKey mySigningKey, OSDXKey myEncryptionKey, OSDXSocketDataHandler dataHandler) {
		this.socket = socket;
		this.mySigningKey = mySigningKey;
		this.myEncryptionKey = myEncryptionKey;
		this.dataHandler = dataHandler;
	}
	
	public void handleNewText(String text, OSDXSocketSender sender) {
		//System.out.println("handle text: "+text);
		nextTimeOut = System.currentTimeMillis()+timeout;
		if (secure_connection_established) {
			if (text.equals("Connection closed.")) {
				try {
					closeConnection();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (dataHandler!=null) {
				dataHandler.handleNewText(text, this);
			} else {
				System.out.println("RECEIVED TEXT: "+text);
			}
		} else {
			if (!secure_connection_established) {
				secure_connection_established = initSymEncKey(text);
				if (secure_connection_established) {
					sendEncryptedText("Secure Connection Established!");
				}
			}
		}
	}
	
	public String getID() {
		return userID;
	}
	
	public void handleNewData(byte[] data, OSDXSocketSender sender) {
		nextTimeOut = System.currentTimeMillis()+timeout;
		if (secure_connection_established && dataHandler!=null) {
			dataHandler.handleNewData(data, this);
		} else {
			System.out.println("RECEIVED DATA length = "+data.length+" bytes");
		}
	}
	
	public void run() {
		try {
			InetAddress addr = socket.getInetAddress();
			String remoteIP = addr.getHostAddress();
			int remotePort = socket.getPort();
			receiver = new OSDXSocketReceiver(socket.getInputStream(),this);
			receiver.addEventListener(this);
			
			sendHello();
			nextTimeOut = System.currentTimeMillis()+timeout;
			while (System.currentTimeMillis()<nextTimeOut) {
				sleep(100);
			}
			closeConnection();
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void sendHello() {
		try {
			Element e = new Element("hello_client");
			e.addContent("message","please choose a symmetric key and send it encrypted with my following public key");
			e.addContent(myEncryptionKey.getSimplePubKeyElement());
			OSDXMessage msg = OSDXMessage.buildMessage(e, mySigningKey);
			sendPlainText(Document.buildDocument(msg.toElement()).toString());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	private void closeConnection() throws Exception { 
		receiver.stop();
		if (socket != null) {
			sendPlainText("Connection closed.");
			socket.close();
		}
		nextTimeOut = System.currentTimeMillis();
		System.out.println("Connection closed.");
	}
	
	private boolean initSymEncKey(String symkeymsg) {
		try {
			if (symkeymsg!=null) {
				//get serves encryption key out of message
				OSDXMessage msg = OSDXMessage.fromElement(Document.fromString(symkeymsg).getRootElement());
				//TODO check with Key
				Result ok = msg.verifySignaturesWithoutKeyVerification();
				if (!ok.succeeded) {
					message = ok.errorMessage;
					return false;
				}
				String id =msg.getSignatures().get(0).getKey().getKeyID();
				userID = id.replace(':', '-');
				
				Element responseElement = msg.getDecryptedContent(myEncryptionKey);
				//Document.buildDocument(responseElement).output(System.out);
				if (responseElement==null || !responseElement.getName().equals("session_encryption_key")) {
					message = ERROR_WRONG_RESPONE_FORMAT;
					return false;
				}
				
				byte[] iv = SecurityHelper.HexDecoder.decode(responseElement.getChildText("init_vector"));
				byte[] key_bytes = SecurityHelper.HexDecoder.decode(responseElement.getChildText("key_bytes"));
				agreedEncryptionKey = new SymmetricKey(key_bytes, iv);
				receiver.setEncryptionKey(agreedEncryptionKey);
				//System.out.println("symmetric key init OK");
				return true;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return false;
	}
	
	private boolean sendBytes(byte[] data, String type) {
		if (socket!=null) {
			try {
				OutputStream out = socket.getOutputStream();
				out.write((type+data.length+":").getBytes("UTF-8"));
				out.write(data);
				return true;
			} catch (SocketException sex) {
				//sex.printStackTrace();
				message = sex.getMessage();
				if (receiver.isRunning()) {
					try {
						closeConnection();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			 }  catch (Exception ex) {
				ex.printStackTrace();
				message = ex.getMessage();
			}
		}
		return false;
	}
	
	public boolean sendEncryptedData(byte[] data) {
		try {
			byte[] encData = agreedEncryptionKey.encrypt(data);
			sendBytes(encData,"ENCDATA");
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			message = ex.getMessage();
		}
		return false;
	}
	
	public boolean sendPlainData(byte[] data) {
		return sendBytes(data,"DATA");
	}
	
	public boolean sendEncryptedText(String text) {
		try {
			byte[] encText = agreedEncryptionKey.encrypt(text.getBytes("UTF-8"));
			return sendBytes(encText,"ENCTEXT");
		} catch (Exception e) {
			e.printStackTrace();
			message = e.getMessage();
			return false;
		}
	}
	
	public boolean sendPlainText(String text) {
		try {
			return sendBytes(text.getBytes("UTF-8"),"TEXT");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}
	}
	
}