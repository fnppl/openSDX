package org.fnppl.opensdx.file_transfer;
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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferCloseConnectionCommand;
import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferCommand;
import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferDeleteCommand;
import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferDownloadCommand;
import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferFileInfoCommand;
import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferListCommand;
import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferLoginCommand;
import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferMkDirCommand;
import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferRenameCommand;
import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferUploadCommand;
import org.fnppl.opensdx.file_transfer.helper.RightsAndDuties;
import org.fnppl.opensdx.file_transfer.model.RemoteFile;
import org.fnppl.opensdx.keyserver.helper.IdGenerator;
import org.fnppl.opensdx.security.AsymmetricKeyPair;
import org.fnppl.opensdx.security.OSDXKey;
import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.security.SymmetricKey;
import org.fnppl.opensdx.xml.Document;

public class OSDXFileTransferClient implements UploadClient {
	
	
	private static boolean DEBUG = false;
	
	private static String version = "openSDX 0.2";
	private String host;
	private int port;
	private String prepath;
	
	private Socket socket;
	private BufferedInputStream in;
	private BufferedOutputStream out;
	private SecureConnection dataOut = null;
	private SecureConnection dataIn = null;
	
	private OSDXFileTransferClientReceiverThread receiver = null;
	private OSDXFileTransferClientCommandHandlerThread commandHandler = null;
	
	private boolean secureConnectionEstablished = false;
	private byte[] client_nonce = null;
	private byte[] server_nonce = null;
	private OSDXKey mySigningKey;
	private String username = null;
	protected RightsAndDuties rights_duties = null;
	
	private Vector<OSDXFileTransferCommand> queueWaiting = new Vector<OSDXFileTransferCommand>();
	private HashMap<Long,OSDXFileTransferCommand> commandsInProgress = new HashMap<Long, OSDXFileTransferCommand>();
	
	private long commandIdBlocks = -1L;
	private long commandBlockTimeout = 10000; //10 sec
	private long nextCommandBlockTimeout = -1L;
	
	private Vector<CommandResponseListener> responseListener = new Vector<CommandResponseListener>();
	
	
	private RemoteFile root = new RemoteFile("", "/", 0L, System.currentTimeMillis(), true);
	
	public OSDXFileTransferClient() {
		try {
			//agreedEncryptionKey = SymmetricKey.getKeyFromPass("test".toCharArray(), new byte[] {3,14,15,92,65,35,89,79,32,38,46,26,43,38,32,79});
			dataOut = null;
			dataIn = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void addResponseListener(CommandResponseListener listener) {
		responseListener.add(listener);
	}
	
	public void addCommand(OSDXFileTransferCommand command) {
		for (CommandResponseListener l : responseListener) {
			command.addListener(l);
		}
		queueWaiting.add(command);
	}
	
	public void addCommandNotListen(OSDXFileTransferCommand command) {
		queueWaiting.add(command);
	}

	
	public boolean hasNextCommand() {
		if (queueWaiting.size()>0) {
			return true;
		}
		return false;
	}
			
	public OSDXFileTransferCommand getNextCommand() {
		if (queueWaiting.size()>0 && System.currentTimeMillis()>nextCommandBlockTimeout) {
			OSDXFileTransferCommand c = queueWaiting.remove(0);
			if (DEBUG) System.out.println("command: "+c.getClass().getName());
			
			//check connected
		//	System.out.println("established="+secureConnectionEstablished+"    socket_connected="+socket.isConnected()+"    socket_closed="+socket.isClosed());
			if (!(c instanceof OSDXFileTransferCloseConnectionCommand || c instanceof OSDXFileTransferLoginCommand) && !isConnected()) {
				System.out.println("No connection to server.");
				for (CommandResponseListener l : responseListener) {
					l.onError(c, "No connection to server.");
				}
				return null;
			}
			//check allowed
			boolean allowed = true;
			if (c instanceof OSDXFileTransferDeleteCommand) {
				allowed = isAllowed(RightsAndDuties.ALLOW_DELETE); 
			}
			else if (c instanceof OSDXFileTransferMkDirCommand) {
				allowed = isAllowed(RightsAndDuties.ALLOW_MKDIR); 
			}
			else if (c instanceof OSDXFileTransferFileInfoCommand) {
				allowed = isAllowed(RightsAndDuties.ALLOW_LIST); 
			}
			else if (c instanceof OSDXFileTransferRenameCommand) {
				allowed = isAllowed(RightsAndDuties.ALLOW_RENAME); 
			}
			if (allowed) {
				commandsInProgress.put(c.getID(), c);
				if (c.isBlocking()) {
					commandIdBlocks = c.getID();
					nextCommandBlockTimeout = System.currentTimeMillis()+commandBlockTimeout;
				}
				return c;
			} else {
				System.out.println("Command not allowed!");
				for (CommandResponseListener l : responseListener) {
					l.onError(c, "Command not allowed!");
				}
				return null;
			}
		}
		return null;
	}
	
	public void removeCommandFromInProgress(long id) {
		commandsInProgress.remove(id);
	}
	
	public void alertBrokenPipe() {
		secureConnectionEstablished = false;
		for (CommandResponseListener l : responseListener) {
			l.onError(null, "Connection to server terminated.");
		}
	}
	
	public void cancelCommands() {
		queueWaiting.removeAllElements();
		//TODO cancel current command
		
	}
	
	public boolean connect(String host, int port, String prepath, OSDXKey mySigningKey, String username) throws Exception {
		this.host = host;
		this.port = port;
		this.username = username;
		
		if (prepath==null || prepath.length()==0) {
			this.prepath = "/";
		} else {
			this.prepath = prepath;
		}
		this.mySigningKey = mySigningKey;
		
		
		secureConnectionEstablished = false;
		client_nonce = null;
		server_nonce = null;
		socket = new Socket(host, port);
		
//		System.out.println("inner connect ok: "+socket.isConnected());
		if (socket.isConnected()) {
			in = new BufferedInputStream(socket.getInputStream());
			out = new BufferedOutputStream(socket.getOutputStream());
			dataOut = new SecureConnection(null, null, out);
			dataIn = new SecureConnection(null, in, null);
			//secureConnectionEstablished =
			initSecureConnection(host, mySigningKey);
			return true;
		} else {
//			System.out.println("ERROR: Connection to server could NOT be established!");
			return false;
		}
	}
	
	public boolean isConnected() {
		return socket.isConnected() && secureConnectionEstablished;
	}
	
	private void initSecureConnection(String host, OSDXKey key) {
		try {
			
			//send request
			client_nonce = SecurityHelper.getRandomBytes(32);
			String init = version +"\n";
			init += host+"\n";
			init += SecurityHelper.HexDecoder.encode(client_nonce,':',-1)+"\n";
			init += key.getKeyID()+"\n";
			init += SecurityHelper.HexDecoder.encode(key.getPublicModulusBytes(),':',-1)+"\n";
			init += SecurityHelper.HexDecoder.encode(key.getPublicExponentBytes(),':',-1)+"\n";
			byte[][] checks = SecurityHelper.getMD5SHA1SHA256(client_nonce);
			init += SecurityHelper.HexDecoder.encode(key.sign(checks[1],checks[2],checks[3],0L),':',-1)+"\n";
			init += "\n";
			
			dataOut.sendRawBytes(init.getBytes("UTF-8"));
			byte[] responsePartKeyData = dataIn.receiveRawBytesPackage();
			byte[] responsePartEncData = dataIn.receiveRawBytesPackage();
			
			//process response
			try {
				String[] lines = new String(responsePartKeyData,"UTF-8").split("\n");
//				for (int i=0;i<lines.length;i++) {
//					System.out.println("("+(i+1)+")"+" "+lines[i]);
//				}

				
				//check signature
				byte[] server_mod = SecurityHelper.HexDecoder.decode(lines[3]);
				byte[] server_exp = SecurityHelper.HexDecoder.decode(lines[4]);
				byte[] server_signature = SecurityHelper.HexDecoder.decode(lines[5]);
				
				AsymmetricKeyPair server_pubkey = new AsymmetricKeyPair(server_mod, server_exp, null);
				
				byte[] encdata = responsePartEncData;
				checks = SecurityHelper.getMD5SHA1SHA256(encdata);
				boolean verifySig = server_pubkey.verify(server_signature, checks[1],checks[2],checks[3],0L);
				
				//System.out.println("signature verified: "+verifySig);
				if (verifySig)  {
					//System.out.println("init msg signature verified!");
					//build enc key
					
					byte[] decData =  mySigningKey.decryptBlocks(encdata);
					String[] encLines = new String(decData, "UTF-8").split("\n");
//					for (int i=0;i<encLines.length;i++) {
//						System.out.println("ENC "+(i+1)+" :: "+encLines[i]);
//					}
					server_nonce = SecurityHelper.HexDecoder.decode(encLines[1]);
					
					byte[] concat_nonce = SecurityHelper.concat(client_nonce, server_nonce);
//					System.out.println("byte len :: concat_nonce = "+concat_nonce.length);
					byte[] key_bytes = SecurityHelper.getSHA256(concat_nonce); 			//32 bytes = 256 bit
					byte[] iv = Arrays.copyOf(SecurityHelper.getMD5(concat_nonce),16);	//16 bytes = 128 bit				
//					System.out.println("byte len :: iv = "+iv.length+"  b = "+key_bytes.length);
//					System.out.println(SecurityHelper.HexDecoder.encode(iv, '\0', -1));
//					System.out.println(SecurityHelper.HexDecoder.encode(key_bytes, '\0', -1));
					dataOut.key = new SymmetricKey(key_bytes, iv);
					dataIn.key = dataOut.key;
					secureConnectionEstablished = true;
					//start receiver and commandhandler
					receiver = new OSDXFileTransferClientReceiverThread(this, dataIn);
					commandHandler = new OSDXFileTransferClientCommandHandlerThread(this,dataOut);
					commandHandler.start();
					
					receiver.start();
					login();
					
				} else {
					System.out.println("init msg signature NOT verified!");
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public RemoteFile getRoot() {
		return root;
	}

	
	private void login() {
		addCommand(new OSDXFileTransferLoginCommand(IdGenerator.getTimestamp(), username).setBlocking());
	}
	
	public void mkdir(String absoluteDirectoryName) {
		addCommand(new OSDXFileTransferMkDirCommand(IdGenerator.getTimestamp(),absoluteDirectoryName));
	}
	
	public void delete(String absoluteDirectoryName) {
		addCommand(new OSDXFileTransferDeleteCommand(IdGenerator.getTimestamp(),absoluteDirectoryName));
	}
	
	public void rename(String absoluteDirectoryName, String newfilename) {
		addCommand(new OSDXFileTransferRenameCommand(IdGenerator.getTimestamp(),absoluteDirectoryName,newfilename));
	}
	
	public void list(String absoluteDirectoryName, CommandResponseListener listener) {
		addCommand(new OSDXFileTransferListCommand(IdGenerator.getTimestamp(),absoluteDirectoryName, listener));
	}
	
	public long download(String absoluteRemoteFilename, File localFile) {
		long id = IdGenerator.getTimestamp();
		addCommand(new OSDXFileTransferDownloadCommand(id,absoluteRemoteFilename, localFile,this));
		return id;
	}

	public long upload(File localFile, String absoluteRemotePath) {
		long id = IdGenerator.getTimestamp();
		addCommand(new OSDXFileTransferUploadCommand(id,localFile, absoluteRemotePath,this));
		return id;
	}
	
	public void fileinfo(String absoluteDirectoryName) {
		addCommand(new OSDXFileTransferFileInfoCommand(IdGenerator.getTimestamp(),absoluteDirectoryName));
	}
	
//	public void uploadFile(File f) throws FileTransferException {
//		if (!rights_duties.allowsUpload()) {
//			throw new FileTransferException("UPLOAD NOT ALLOWED");
//		}
//		uploadFile(f, null, false, null);
//	}
	
	
	public void closeConnection() {
		addCommandNotListen(new OSDXFileTransferCloseConnectionCommand(IdGenerator.getTimestamp()));
	}
	
	//use CloseConnectionCommand for closing after all previous commands are send
	public void closeConnectionDirectly() {
		if (socket != null) {
			try {
				socket.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			socket = null;
		}
		commandHandler.close();
		receiver.close();
	}
	
	private boolean isAllowed(int rightType) {
		if (rights_duties==null) {
			return false;
		}
		return rights_duties.hasRight(rightType);
	}
	
	//calls from receiver thread come in here
	public void onResponseReceived(long commandid, int num, byte code, byte[] content) {
		try {
			if (DEBUG) {
				System.out.println("RESPONSE commandid="+commandid);
				if (code == SecureConnection.TYPE_DATA) {
					//System.out.println("DATA: len="+content.length);
				} else {
					if (content!=null) { //if (code == SecureConnection.TYPE_TEXT) {
						String txt = new String(content,"UTF-8");
						System.out.println(SecurityHelper.HexDecoder.encode(new byte[]{code})+" :: MSG: "+txt);
					} else {
						System.out.println(SecurityHelper.HexDecoder.encode(new byte[]{code})+" :: NO MSG");
					}
				}
			}
			
			OSDXFileTransferCommand command = commandsInProgress.get(commandid);
			
			if (command!=null) {
				if (DEBUG) System.out.println("command found");
				
				//forward response to right command
				command.onResponseReceived(num, code, content);
				
				//handle if there is something to handle
				if (command instanceof OSDXFileTransferLoginCommand) {
					if (code == SecureConnection.TYPE_ACK) {
						rights_duties = ((OSDXFileTransferLoginCommand)command).getRightsAndDuties();
						secureConnectionEstablished = true;
						System.out.println("Login successful.");
					}
					else if (SecureConnection.isError(code)) {
						System.out.println("Error in login :: "+getMessageFromContentNN(content));
					}
				}
				
				//handle only for debugging
				if (DEBUG) {
					if (command instanceof OSDXFileTransferMkDirCommand) {
						if (code == SecureConnection.TYPE_ACK) {
							System.out.println("MkDir successful.");
						}
						else if (SecureConnection.isError(code)) {
							System.out.println("Error in MkDir :: "+getMessageFromContentNN(content));
						}
					}
					else if (command instanceof OSDXFileTransferDeleteCommand) {
						if (code == SecureConnection.TYPE_ACK) {
							System.out.println("Delete successful.");
						}
						else if (SecureConnection.isError(code)) {
							System.out.println("Error in delete :: "+getMessageFromContentNN(content));
						}
					}
					else if (command instanceof OSDXFileTransferRenameCommand) {
						if (code == SecureConnection.TYPE_ACK) {
							System.out.println("Rename successful.");
						}
						else if (SecureConnection.isError(code)) {
							System.out.println("Error in rename :: "+getMessageFromContentNN(content));
						}
					}
					else if (command instanceof OSDXFileTransferFileInfoCommand) {
						if (code == SecureConnection.TYPE_ACK) {
							RemoteFile rf = ((OSDXFileTransferFileInfoCommand)command).getRemoteFile();
							if (rf!=null) {
								System.out.println("FileInfo: "+rf.toString());
							} else {
								System.out.println("Error in FileInfo");
							}
						}
						else if (SecureConnection.isError(code)) {
							System.out.println("Error in delete :: "+getMessageFromContentNN(content));
						}
					}
					else if (command instanceof OSDXFileTransferListCommand) {
						if (code == SecureConnection.TYPE_ACK) {
							Vector<RemoteFile> list = ((OSDXFileTransferListCommand)command).getList();
							if (list!=null) {
								for (int i=0;i<list.size();i++) {
									System.out.println(list.get(i).toString());
								}
							} else {
								System.out.println("Error in List");
							}
						}
						else if (SecureConnection.isError(code)) {
							System.out.println("Error in delete :: "+getMessageFromContentNN(content));
						}
					}
				}
				
			} else {
				System.out.println("ERROR: command not found.");
			}
			
			//release block
			if (commandIdBlocks == commandid) {
				nextCommandBlockTimeout = -1L;
			}
			if (!(command instanceof OSDXFileTransferDownloadCommand || command instanceof OSDXFileTransferUploadCommand)) {
				removeCommandFromInProgress(commandid);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getMessageFromContentNN(byte[] content) {
		if (content==null) return "";
		try {
			return new String(content,"UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		return "";
	}
	
	
	
	//Methods from UploadClient
	
	public void uploadFile(File f, String remoteAbsoluteFilename, CommandResponseListener listener) {
		long id = IdGenerator.getTimestamp();
		OSDXFileTransferUploadCommand c = new OSDXFileTransferUploadCommand(id,f, remoteAbsoluteFilename,this);
		if (listener!=null) {
			c.addListener(listener);
		}
		addCommand(c);
	}

	public void  uploadFile(byte[] data, String remoteAbsoluteFilename, CommandResponseListener listener) {
		long id = IdGenerator.getTimestamp();
		OSDXFileTransferUploadCommand c = new OSDXFileTransferUploadCommand(id,data, remoteAbsoluteFilename,this);
		if (listener!=null) {
			c.addListener(listener);
		}
		addCommand(c);
	}
	
	
	
	public static void main(String args[]) {
		try {

			OSDXFileTransferClient client = new OSDXFileTransferClient();

			File downloadPath = new File("../../openSDX/files");
			OSDXKey mysigning = OSDXKey.fromElement(Document.fromString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>  <keypair><identities><identity><identnum>0001</identnum><email>test</email><mnemonic restricted=\"true\">test</mnemonic><sha256>2D:83:44:CA:3A:4C:85:3A:FB:E9:A3:15:D1:B4:70:BA:CC:7C:16:C7:DC:80:D9:AF:4F:E5:3D:74:4A:58:47:CE</sha256></identity></identities><sha1fingerprint>27:61:62:78:C1:29:F3:C6:A9:03:44:D2:18:36:37:22:E2:9F:63:BF</sha1fingerprint><authoritativekeyserver>localhost</authoritativekeyserver><datapath><step1><datasource>LOCAL</datasource><datainsertdatetime>2011-06-08 04:42:04 GMT+00:00</datainsertdatetime></step1></datapath><valid_from>2011-06-08 04:42:04 GMT+00:00</valid_from><valid_until>2036-06-07 10:42:04 GMT+00:00</valid_until><usage>ONLYSIGN</usage><level>MASTER</level><parentkeyid /><algo>RSA</algo><bits>3072</bits><modulus>00:88:FA:94:53:C5:EC:A9:31:63:FD:20:E3:38:A6:C8:B7:7F:32:41:4B:84:07:A1:AF:81:48:21:6F:D8:87:58:40:BF:DC:1A:E5:BD:A9:8E:ED:90:84:ED:84:BB:2E:04:FB:4F:33:F9:46:8B:0D:D0:58:F5:85:5C:F7:43:93:60:3A:BF:10:4B:92:65:DF:86:13:11:C0:6A:46:6F:4B:68:C3:5B:C3:48:BF:8E:16:00:36:68:A5:0E:C6:03:3B:87:7C:49:0C:18:FE:34:DA:78:03:F8:4B:B3:22:B9:D5:63:D8:74:B8:88:4C:E9:4D:A4:CE:A5:7C:09:B2:19:55:77:55:81:4C:FF:76:CD:87:69:B9:D4:B8:53:BE:9C:07:52:ED:53:09:D7:66:82:FC:A5:0A:79:2B:0D:06:5A:A7:76:77:F9:C4:27:B3:FD:BB:4A:80:44:8F:83:0F:DA:7E:A1:E7:22:24:D0:CC:EA:B2:F0:7F:03:BF:FC:FA:BB:B5:2D:17:63:40:1F:78:41:34:E4:ED:5A:F7:A2:1A:C5:75:FC:0F:93:44:95:AD:09:0D:10:90:D1:77:6B:D4:02:3C:8B:43:0B:91:3C:F5:F9:4A:94:0C:D4:EC:DD:2B:56:D4:AB:B9:C1:A4:74:AC:85:2E:6C:7C:AE:21:17:11:41:CB:9D:1C:16:98:1A:4F:03:8A:34:80:C5:2E:F6:E8:29:DB:3F:1C:EA:B1:B7:21:A9:5F:FA:93:D3:47:FA:DD:28:8F:4F:AA:53:1B:16:32:61:3B:B2:41:0E:37:DB:16:5B:14:AA:A9:D4:6C:C3:3E:0E:8D:90:B9:C4:83:C1:A6:6A:BF:E0:7F:56:AF:7D:7F:47:E5:4E:9C:8E:E9:E4:27:06:F9:0A:8A:22:7A:85:2D:FE:B2:AF:10:EC:5B:36:E7:96:60:E2:77:C5:9F:78:B9:51:A4:CE:7C:1D:D3:43:BF:4F:B1:2C:3F:DF:30:04:B6:7E:40:7E:F3:0E:F1:12:42:78:C6:4A:07</modulus><pubkey><exponent>01:00:01</exponent></pubkey><privkey><exponent><locked><mantraname>mantra: password</mantraname><algo>AES@256</algo><initvector>6F:67:A3:5C:C0:5A:67:F6:30:32:9A:0E:1E:3A:8B:1B</initvector><padding>CBC/PKCS#5</padding><bytes>12:9A:B4:1C:1F:8D:8B:88:39:CD:CD:C4:C9:4D:BE:65:56:2C:48:40:E8:3A:ED:09:F3:BF:0D:A7:A8:09:77:B7:C1:15:FB:8C:93:57:B6:38:F3:31:9B:A8:1E:21:27:40:9E:93:E9:4A:1F:B1:41:02:CF:40:96:2C:A6:17:2B:48:68:58:70:AD:B7:E1:52:6F:09:19:11:67:59:BC:1F:FD:BE:88:C0:B2:FF:76:34:EF:1B:26:DA:9F:4C:47:66:0E:87:BB:1C:09:CB:F2:77:BC:CB:AE:89:CA:C4:65:98:DA:D1:6E:ED:22:08:70:FC:BB:E2:CC:41:7F:5C:12:7B:A6:D3:32:73:FA:BB:E2:95:A6:1C:34:3E:FD:A8:90:D0:9A:0B:4E:96:06:89:DD:6F:35:02:E5:FB:CA:0A:E7:0D:2E:A1:B3:81:17:DE:8D:7F:96:F4:36:AA:02:4E:EF:C0:EF:56:37:C0:53:FB:B9:E3:C0:5B:69:9E:7C:EC:1F:A3:0B:C5:99:B7:5D:54:52:28:17:4A:B1:3D:C8:36:54:2A:94:0C:32:F7:1B:6A:11:37:91:B5:43:5D:BF:DB:6F:D3:B4:37:18:32:81:81:C1:72:80:B6:95:0E:B0:61:FF:05:CE:FC:98:E5:F1:E4:D1:33:B7:EF:B8:EB:EF:6B:A7:FE:C6:37:77:CF:43:12:C3:5F:2B:2A:51:19:E8:C4:6D:F6:0E:15:C4:C3:AD:BE:4C:FE:D6:D5:3A:00:D8:E0:0B:00:78:A9:5F:D8:21:28:06:B8:74:F2:06:23:63:81:B8:CC:03:EC:2C:ED:6B:74:23:E4:31:C1:4E:9C:B2:24:F0:93:A4:7D:6C:6A:E2:C1:95:EC:EA:DF:DC:85:2B:60:15:24:DE:FD:DD:94:BF:CF:C9:8D:74:DE:8A:D8:89:DC:16:FA:9D:28:37:EE:65:44:AD:61:FB:33:D4:E8:66:D3:BA:D8:38:E1:16:F1:EF:97:FF:01:D7:C3:7D:76:CE:A2:12:1C:24:AC:EC:AE:AC:2B:08:99:7A:D8:A4:41:49:A4:0E:AF:83</bytes></locked></exponent></privkey><gpgkeyserverid /></keypair>  ").getRootElement());
			mysigning.unlockPrivateKey("password");			
			String username = "testuser";

			client.connect("localhost", 4221,"/", mysigning, username);
			//client.mkdir("/blub");
			//client.fileinfo("/blub");
			client.delete("/FDL.txt");
			
			client.upload(new File("FDL.txt"), "/FDL.txt");
			//client.rename("/FDL.txt", "blablub");
			//client.list("/");
			client.download("/FDL2.txt", new File("/tmp/FDL2.txt"));
			
			Thread.sleep(1000);
			client.closeConnection();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
