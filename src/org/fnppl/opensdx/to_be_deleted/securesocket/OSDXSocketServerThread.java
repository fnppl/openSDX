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
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

import org.fnppl.opensdx.file_transfer.FileTransferLog;
import org.fnppl.opensdx.security.AsymmetricKeyPair;
import org.fnppl.opensdx.security.OSDXKey;
import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.security.SymmetricKey;

public class OSDXSocketServerThread extends Thread implements OSDXSocketSender, OSDXSocketLowLevelDataHandler {

	private static boolean debug = true;
	private static String version = "openSDX 0.2";
	public final static String ERROR_NO_RESPONSE = "ERROR: server does not respond.";
	public final static String ERROR_WRONG_RESPONE_FORMAT = "ERROR: Wrong format in uploadserver's response.";
	public final static String ERROR_UNKNOWN_KEY_OR_USER = "ERROR: login failed: unknown signature key or wrong username.";
	public final static String ERROR_MISSING_ENCRYTION_KEY = "ERROR: missing encryption key";
	private long timeout = 30000;
	
	private Socket socket;
	private BufferedOutputStream socketOut = null;
	private String remoteIP = null;
	private int remotePort = -1;
	private OSDXKey mySigningKey = null;
	private String client_keyid = null; 
	private byte[] client_nonce = null;
	private byte[] server_nonce = null;
	private SymmetricKey agreedEncryptionKey = null;
	
	private String message = null;
	private boolean secure_connection_established = false;
	private String userID = "unknown_user";

	private OSDXSocketDataHandler dataHandler = null;
	private OSDXSocketReceiver receiver = null;
	private long nextTimeOut = Long.MAX_VALUE;
	
	public OSDXSocketServerThread(Socket socket, OSDXKey mySigningKey, OSDXSocketDataHandler dataHandler) {
		this.socket = socket;
		try {
			socketOut = new BufferedOutputStream(socket.getOutputStream());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		this.mySigningKey = mySigningKey;
		this.dataHandler = dataHandler;
	}
	
	public boolean initConnection(String[] lines) {
		try {
		if (lines!=null) {
//			for (int i=0;i<lines.length;i++) {
//				System.out.println("("+(i+1)+")"+" "+lines[i]);
//			}
			
			boolean ok = true;
			String version = lines[0];
			String host = lines[1];
			
			client_nonce = SecurityHelper.HexDecoder.decode(lines[2]);
			client_keyid = lines[3];
			byte[] client_mod = SecurityHelper.HexDecoder.decode(lines[4]);
			byte[] client_exp = SecurityHelper.HexDecoder.decode(lines[5]);
			byte[] client_signature = SecurityHelper.HexDecoder.decode(lines[6]);
			AsymmetricKeyPair client_pubkey = new AsymmetricKeyPair(client_mod, client_exp, null);
			
			byte[][] checks = SecurityHelper.getMD5SHA1SHA256(client_nonce);
			boolean verifySig = client_pubkey.verify(client_signature, checks[1],checks[2],checks[3],0L);
			if (verifySig) {
				//generate response
				server_nonce = SecurityHelper.getRandomBytes(32);
				StringBuffer msg = new StringBuffer();
				msg.append(" 200\n");
				msg.append(host+"\n");
				msg.append(mySigningKey.getKeyID()+"\n");
				msg.append(SecurityHelper.HexDecoder.encode(mySigningKey.getPublicModulusBytes(),':',-1)+"\n");
				msg.append(SecurityHelper.HexDecoder.encode(mySigningKey.getPublicExponentBytes(),':',-1)+"\n");
				
				//encrypted part
				StringBuffer encmsg = new StringBuffer();
				encmsg.append(SecurityHelper.HexDecoder.encode(client_nonce,':',-1)+"\n");
				encmsg.append(SecurityHelper.HexDecoder.encode(server_nonce,':',-1)+"\n");
				encmsg.append(lines[3]+"\n");
				encmsg.append(lines[4]+"\n");
				encmsg.append(lines[5]+"\n");
				byte[] enc = client_pubkey.encryptBlocks(encmsg.toString().getBytes("UTF-8"));

				//sign enc part
				checks = SecurityHelper.getMD5SHA1SHA256(enc);				
				byte[] sigOfEnc = mySigningKey.sign(checks[1],checks[2],checks[3],0L);
				
				//add signature bytes and enc part
				msg.append(SecurityHelper.HexDecoder.encode(sigOfEnc,':',-1)+"\n");
				msg.append("ENC "+enc.length+"\n");
				
				//build sym key of client_nonce and server nonce
				byte[] concat_nonce = SecurityHelper.concat(client_nonce, server_nonce);
				byte[] key_bytes = SecurityHelper.getSHA256(concat_nonce); 			//32 bytes = 256 bit
				byte[] iv = Arrays.copyOf(SecurityHelper.getMD5(concat_nonce),16);	//16 bytes = 128 bit
				agreedEncryptionKey = new SymmetricKey(key_bytes, iv);
				receiver.setEncryptionKey(agreedEncryptionKey);
				
				//send packet
				sendBytesPacket(msg.toString().getBytes("UTF-8"), TYPE_NULL,false);
				sendBytesPacket(enc, TYPE_NULL,false);
				
			} else {
				ok = false;
				String msg = version+" 421 You are not authorized to make the connection\n";
				msg += host+"\n";
				sendBytesPacket(msg.getBytes("UTF-8"), TYPE_NULL,false);
			}
			return ok;
		}
	} catch (Exception e1) {
		e1.printStackTrace();
	}
	message = "Unknown Error.";
	return false;
	}
	
	public void handleNewText(String text, OSDXSocketSender sender) {
		//System.out.println("handle text: "+text);
		nextTimeOut = System.currentTimeMillis()+timeout;
		//System.out.println("next timeout = "+SecurityHelper.getFormattedDate(nextTimeOut));
		if (secure_connection_established) {
			if (text.equals("Connection closed.")) {
				try {
				//	closeConnection();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (dataHandler!=null) {
				dataHandler.handleNewText(text, this);
			} else {
				System.out.println("RECEIVED TEXT: "+text);
			}
		}
	}
	
	public void handleNewInitMsg(String[] lines, byte[] data, OSDXSocketSender sender) {
		try {
			secure_connection_established = initConnection(lines);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public String getClientKeyID() {
		return client_keyid;
	}
	
	public String getID() {
		return userID;
	}
	
	public void setID(String id) {
		this.userID = id;
	}
	
	public void handleNewData(byte[] data, OSDXSocketSender sender) {
		nextTimeOut = System.currentTimeMillis()+timeout;
		//System.out.println("next timeout = "+SecurityHelper.getFormattedDate(nextTimeOut));
		if (secure_connection_established && dataHandler!=null) {
			dataHandler.handleNewData(data, this);
		} else {
			System.out.println("RECEIVED DATA length = "+data.length+" bytes");
		}
	}
	
	public void run() {
		try {
			InetAddress addr = socket.getInetAddress();
			remoteIP = addr.getHostAddress();
			remotePort = socket.getPort();
			//int localPort = socket.getLocalPort();
			receiver =OSDXSocketReceiver.initServerReceiver(socket.getInputStream(),this,this);
			System.out.println(FileTransferLog.getDateString()+" :: serverthread started, remote_ip="+remoteIP+", remote_port="+remotePort);
			
			nextTimeOut = System.currentTimeMillis()+timeout;
			boolean loop = true;
			
			while (loop) {
				while (System.currentTimeMillis()<nextTimeOut && isConnected()) {
					//System.out.println("next timeout = "+SecurityHelper.getFormattedDate(nextTimeOut)+"\t"+this.toString());
					sleep(500);
					//System.out.println("connected: "+isConnected());
				}
				//wrong timeout if receiving large files on slow connections
				if (System.currentTimeMillis()>receiver.getLastReceivedBytesAt()+timeout) {
					loop = false;
				}
			}
			if (isConnected()) {
				System.out.println(FileTransferLog.getDateString()+" :: timeout, closing connection for remote port "+remotePort);
			} else {
				System.out.println(FileTransferLog.getDateString()+" :: connection closed by remote socket, remote port "+remotePort);
			}
			closeConnection();
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public String getRemoteIP() {
		return remoteIP;
	}
	
	public int getRemotePort() {
		return remotePort;
	}
	
	public boolean isConnected() {
		return (socket.isConnected() && receiver!=null && receiver.isRunning());
	}
	
	private void closeConnection() throws Exception { 
		receiver.stop();
		if (socket != null) {
			socket.close();
		} 
		System.out.println(FileTransferLog.getDateString()+" :: Connection closed.");
		remoteIP = null;
		remotePort = -1;
	}
	
	private static byte TYPE_TEXT = 84;
	private static byte TYPE_DATA = 68;
	private static byte TYPE_NULL = 0;
	private Object sync_obj = new Object();
	
	private boolean sendBytesPacket(byte[] data, byte type, boolean encrypt) {
		synchronized (sync_obj) {
			if (socketOut!=null) {
				try {
					if (type==TYPE_NULL) {
						socketOut.write(data);
						socketOut.flush();
						if (debug) System.out.println("sending [NULL] :: "+(new String(data,"UTF-8")));
					} else {
						byte[] send = new byte[data.length+1];
						send[0] = type;
						System.arraycopy(data, 0, send, 1, data.length);;
						
						if (debug) { 
							if (type==TYPE_TEXT) {
								System.out.println("sending [TEXT] :: "+(new String(data,"UTF-8")));
							} else if (type==TYPE_DATA) {
								System.out.println("sending [DATA] :: length="+data.length);
							} else {
								System.out.println("sending [UNKNOWN TYPE] :: length="+data.length);
							}
						}
						
						if (encrypt && agreedEncryptionKey!=null) {
							send = agreedEncryptionKey.encrypt(send);
						}
						socketOut.write((send.length+"\n").getBytes("UTF-8"));
						socketOut.write(send);
						socketOut.flush();
					}
					return true;
				} catch (Exception ex) {
					ex.printStackTrace();
					message = ex.getMessage();
				}
			}
		}
		return false;
	}
	
	public boolean sendEncryptedData(byte[] data) {
		if (agreedEncryptionKey!=null) {
			try {
				return sendBytesPacket(data,TYPE_DATA,true);
			} catch (Exception ex) {
				ex.printStackTrace();
				message = ex.getMessage();
			}
		}
		return false;
	}
	
	public boolean sendEncryptedText(String text) {
		if (agreedEncryptionKey!=null) {
			try {
				return sendBytesPacket(text.getBytes("UTF-8"),TYPE_TEXT,true);
			} catch (Exception e) {
				e.printStackTrace();
				message = e.getMessage();
			}
		}
		return false;
	}
	
}
