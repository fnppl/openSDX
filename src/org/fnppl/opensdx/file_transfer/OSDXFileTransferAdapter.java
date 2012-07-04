package org.fnppl.opensdx.file_transfer;
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
import java.io.*;
import java.net.Socket;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import org.apache.velocity.runtime.parser.node.SetPropertyExecutor;
import org.fnppl.opensdx.common.Util;
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
import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferUserPassLoginCommand;
import org.fnppl.opensdx.file_transfer.helper.RightsAndDuties;
import org.fnppl.opensdx.file_transfer.model.FileTransferAccount;
import org.fnppl.opensdx.file_transfer.model.RemoteFile;
import org.fnppl.opensdx.file_transfer.model.Transfer;
import org.fnppl.opensdx.gui.DefaultConsoleMessageHandler;
import org.fnppl.opensdx.gui.Dialogs;
import org.fnppl.opensdx.gui.MessageHandler;
import org.fnppl.opensdx.helper.Logger;
import org.fnppl.opensdx.helper.ProgressListener;
import org.fnppl.opensdx.keyserver.helper.IdGenerator;
import org.fnppl.opensdx.security.AsymmetricKeyPair;
import org.fnppl.opensdx.security.Identity;
import org.fnppl.opensdx.security.KeyApprovingStore;
import org.fnppl.opensdx.security.MasterKey;
import org.fnppl.opensdx.security.OSDXKey;
import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.security.SymmetricKey;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;

public class OSDXFileTransferAdapter {


	private static boolean DEBUG = false;

	private static String version = "osdx_ftclient_sync v.2012-01-13";
	protected int maxPacketSize = 50*1024; //50kB

	private Logger logger = Logger.getFileTransferLogger();

	private String host;
	private int port;
	private String prepath;
	
	private OSDXKey mySigningKey;
	
	private String username = null;
	
	private static AsymmetricKeyPair userSessionKey = null; //HT 2012-03-20 really good idea to have it static?!
	private String password = null;

	private Socket socket;
	private BufferedInputStream in;
	private BufferedOutputStream out;
	private SecureConnection dataOut = null;
	private SecureConnection dataIn = null;
	private String errorMsg = null;

	private boolean secureConnectionEstablished = false;
	private byte[] client_nonce = null;
	private byte[] server_nonce = null;

	protected RightsAndDuties rightsAndDuties = null;
	private RemoteFile root = new RemoteFile("", "/", 0L, System.currentTimeMillis(), true);

	public OSDXFileTransferAdapter() {
		try {
			dataOut = null;
			dataIn = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean connect(String host, int port, String prepath, String username, String pass) throws Exception {
		this.host = host;
		this.port = port;
		
		this.username = username;
		this.password = pass;
		this.mySigningKey = null;
		
		if (prepath==null || prepath.length()==0) {
			this.prepath = "/";
		} else {
			this.prepath = prepath;
		}
		
		secureConnectionEstablished = false;
		client_nonce = null;
		server_nonce = null;
		try {
			logger.logMsg("trying to connect to host: "+host+" port: "+port+" version: "+version);
			socket = new Socket(host, port);
		} catch (Exception ex) {
			logger.logException(ex);
			throw ex;
		}
		logger.logMsg("Socket connected.");

		//		System.out.println("inner connect ok: "+socket.isConnected());
		if (socket.isConnected()) {
			in = new BufferedInputStream(socket.getInputStream());
			out = new BufferedOutputStream(socket.getOutputStream());
			dataOut = new SecureConnection(null, null, out);
			dataIn = new SecureConnection(null, in, null);
			//secureConnectionEstablished =
			
			initSecureUserPassConnection(host);
			return true;
		} else {
			//			System.out.println("ERROR: Connection to server could NOT be established!");
			return false;
		}
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
		try {
			logger.logMsg("trying to connect to host: "+host+" port: "+port+" version: "+version);
			socket = new Socket(host, port);
		} catch (Exception ex) {
			logger.logException(ex);
			throw ex;
		}
		logger.logMsg("Socket connected.");

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

	private void ensureAsymmetricKeyPair() throws Exception {
		if(userSessionKey == null) {
			logger.logMsg("Creating asymmetric keyPair for this session...");
			long l = System.currentTimeMillis();
			userSessionKey = AsymmetricKeyPair.generateAsymmetricKeyPair();
			l = System.currentTimeMillis() -l ;
			logger.logMsg("Creating asymmetric keyPair for this session took "+l+"ms");
		}
	}
	
	private boolean initSecureUserPassConnection(String host) {
		try {
			logger.logMsg("init secure connection to host: "+host+"...");

			ensureAsymmetricKeyPair();
			
			//send request
			client_nonce = SecurityHelper.getRandomBytes(32);
//			userSessionKey
//			sessionKey = SymmetricKey.getKeyFromPass(pass.toCharArray(), client_nonce);
			
//			String pass_username
//			asd
			String init = version +"\n";
			init += host+"\n";
			init += SecurityHelper.HexDecoder.encode(client_nonce,':',-1)+"\n";			
			init += userSessionKey.getKeyIDHex()+"\n";
			init += SecurityHelper.HexDecoder.encode(userSessionKey.getPublicModulus(),':',-1)+"\n";
			init += SecurityHelper.HexDecoder.encode(userSessionKey.getPublicExponent(),':',-1)+"\n";
			byte[][] checks = SecurityHelper.getMD5SHA1SHA256(client_nonce);
			init += SecurityHelper.HexDecoder.encode(userSessionKey.sign(checks[1],checks[2],checks[3],0L),':',-1)+"\n";
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

					byte[] decData =  userSessionKey.decryptBlocks(encdata);
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

					if (loginUserPass()) {
						System.out.println("Login successful...");
					} else {
						if (errorMsg == null) {
							System.out.println("ERROR at Login :: unknown error");
						} else {
							System.out.println("ERROR at Login :: "+errorMsg);
						}
					}

				} else {
					System.out.println("init msg signature NOT verified!");
					logger.logError("init msg signature NOT verified!");
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				logger.logException(ex);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			logger.logException(ex);
		}
		
		return false;
	}
	
	private void initSecureConnection(String host, OSDXKey key) {
		try {
			logger.logMsg("init secure connection to host: "+host+" with keyid: "+key.getKeyID()+" ...");
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

					if (login()) {
						System.out.println("Login successful...");
					} else {
						if (errorMsg == null) {
							System.out.println("ERROR at Login :: unknown error");
						} else {
							System.out.println("ERROR at Login :: "+errorMsg);
						}
					}

				} else {
					System.out.println("init msg signature NOT verified!");
					logger.logError("init msg signature NOT verified!");
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				logger.logException(ex);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			logger.logException(ex);
		}
	}

	public RemoteFile getRoot() {
		return root;
	}

	private boolean login() {
		SimpleCommand cmd = new SimpleCommand(dataIn, dataOut) {
			public boolean onACK() {
				try {
					String msg = new String(dataIn.content,"UTF-8");
					String[] param = Util.getParams(msg);
					try {
						rightsAndDuties = RightsAndDuties.fromElement(Document.fromString(param[1]).getRootElement(), -1);
						return true;
					} catch (Exception ex) {
						ex.printStackTrace();
						rightsAndDuties = null;
						errorMsg = getMessageFromContent(dataIn.content);
						return false;
					}
				} catch (UnsupportedEncodingException ex) {
					errorMsg = "unsupported encoding";
					return false;
				}
			}
		};
		boolean ok = cmd.process("LOGIN "+username);
		errorMsg = cmd.errorMsg;
		return ok;
	}
	
	private boolean loginUserPass() {
		SimpleCommand cmd = new SimpleCommand(dataIn, dataOut) {
			public boolean onACK() {
				try {
					String msg = new String(dataIn.content,"UTF-8");
					String[] param = Util.getParams(msg);
					try {
						rightsAndDuties = RightsAndDuties.fromElement(Document.fromString(param[1]).getRootElement(), -1);
						return true;
					} catch (Exception ex) {
						ex.printStackTrace();
						rightsAndDuties = null;
						errorMsg = getMessageFromContent(dataIn.content);
						return false;
					}
				} catch (UnsupportedEncodingException ex) {
					errorMsg = "unsupported encoding";
					return false;
				}
			}
		};
		boolean ok = cmd.process("USERPASSLOGIN "+username+"\t"+OSDXFileTransferUserPassLoginCommand.getUserPassAuth(username, password));
		errorMsg = cmd.errorMsg;
		return ok;
	}

	private void handleUnexpectedPackageID() {
		System.out.println("Unexpected Package Received:");
		System.out.println("id     = "+dataIn.id);
		System.out.println("type   = "+SecurityHelper.HexDecoder.encode(new byte[]{dataIn.type}));
		System.out.println("length = "+dataIn.len);
	}

	private void handleConnectionClosed() {
		System.out.println("Connection closed by Server.");
	}

	public boolean mkdir(String absoluteDirectoryName) {
		SimpleCommand cmd = new SimpleCommand(dataIn, dataOut);
		boolean ok = cmd.process("MKDIR "+absoluteDirectoryName);
		errorMsg = cmd.errorMsg;
		return ok;
	}
	public String getMostRecentErrorMSG() {
		return errorMsg;
	}
//	public Exception getMostRecentException() {
//		return last_exception;
//	}

	public boolean delete(String absoluteRemoteFilename) {
		SimpleCommand cmd = new SimpleCommand(dataIn, dataOut);
		boolean ok = cmd.process("DELETE "+absoluteRemoteFilename);
		errorMsg = cmd.errorMsg;
		return ok;
	}

	public boolean rename(String absoluteRemoteFilename, String newfilename) {
		String[] params = new String[] {absoluteRemoteFilename,newfilename};
		String command = "RENAME "+Util.makeParamsString(params);
		SimpleCommand cmd = new SimpleCommand(dataIn, dataOut);

		boolean ok = cmd.process(command);
		errorMsg = cmd.errorMsg;
		return ok;
	}

	public Vector<RemoteFile> list(String absoluteDirectoryName) {
		final Vector<RemoteFile> list = new Vector<RemoteFile>();
		SimpleCommand cmd = new SimpleCommand(dataIn, dataOut) {
			public boolean onACK() {
				//parse list
				String s = getMessageFromContentNN(dataIn.content);
				if (DEBUG) System.out.println("RECEIVED LIST: "+s);
				if (s.length()>0) {
					String[] files = s.split("\n");
					for (int i=0;i<files.length;i++) {
						RemoteFile rf = RemoteFile.fromParamString(files[i]);
						if (rf!=null) {
							list.add(rf);
						}
					}
				}
				return true;
			}
		};

		boolean ok = cmd.process("LIST "+absoluteDirectoryName);
		errorMsg = cmd.errorMsg;
		if (!ok) {
			return null;
		} else {
			return list;
		}
	}

	public RemoteFile fileinfo(String absoluteRemoteFilename) {
		final Vector<RemoteFile> list = new Vector<RemoteFile>();
		SimpleCommand cmd = new SimpleCommand(dataIn, dataOut) {
			public boolean onACK() {
				//parse fileinfo
				RemoteFile rf = RemoteFile.fromParamString(getMessageFromContentNN(dataIn.content));
				if (rf!=null) {
					list.add(rf);
					return true;
				} else {
					return false;	
				}
			}
		};

		boolean ok = cmd.process("FILE "+absoluteRemoteFilename);
		errorMsg = cmd.errorMsg;
		if (!ok) {
			return null;
		} else {
			if (list.size()>0) {
				return list.get(0);
			} else {
				return null;
			}
		}
	}

	public boolean upload(File localFile, String absoluteRemotePath) {
		boolean ret = false;
		try {
			System.out.println("Upload of file: "+localFile.getCanonicalPath()+" -> "+absoluteRemotePath);
			boolean ok = upload(localFile, absoluteRemotePath, false, null);
			if (ok) {
				System.out.println("Upload finished.\n");
				ret = true;
			} 
			else {
				if(errorMsg==null) {
					System.out.println("ERROR\n");
				} 
				else {
					System.out.println("ERROR: "+errorMsg+"\n");
				}
				
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ret;
	}

	public void uploadResume(File localFile, String absoluteRemotePath) {
		try {
			System.out.println("Upload of file: "+localFile.getCanonicalPath()+" -> "+absoluteRemotePath);
			boolean ok = upload(localFile, absoluteRemotePath, false, null);
			if (ok) {
				System.out.println("Upload finished.\n");
			} else {
				if (errorMsg==null) {
					System.out.println("ERROR\n");
				} else {
					System.out.println("ERROR: "+errorMsg+"\n");
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public boolean upload(File localFile, String absoluteRemoteFilename, boolean resume, ProgressListener pg) throws Exception {
		errorMsg = null;
		long id = SecureConnection.getID();
		int num = 0;

		long filePos = -1L;
		long fileLen = localFile.length();
		FileInputStream fileIn = null;
		long maxFilelengthForMD5 = 100*1024*1024L; //100 MB

		boolean hasNext = true;

		if (pg!=null) {
			if (fileLen>0) {
				pg.setMaxProgress(fileLen);
			} else {
				pg.setMaxProgress(1);
			}
			pg.setProgress(0);	
			pg.onUpate();
		}
		String command = null;
		String[] param;
		if (fileLen>0) {
			byte[] md5 = null;
			if (fileLen<maxFilelengthForMD5) {
				try {
					md5 = SecurityHelper.getMD5(localFile);
				} catch (Exception e) {
					System.out.println("Error calculating md5 hash of "+localFile.getAbsolutePath());
					e.printStackTrace();
					md5 = null;
				}
			}
			if (md5!=null) {
				param = new String[] {absoluteRemoteFilename,""+fileLen,SecurityHelper.HexDecoder.encode(md5)};
			} else {
				param = new String[] {absoluteRemoteFilename,""+fileLen};
			}
		} else {
			fileLen = 0L;
			param = new String[] {absoluteRemoteFilename,""+fileLen};
		}

		if (resume) {
			command = "RESUMEPUT "+Util.makeParamsString(param);
		} else {
			command = "PUT "+Util.makeParamsString(param);
		}

		dataOut.setCommand(id, command);
		if (DEBUG) {
			Logger.getFileTransferLogger().logMsg("SEND CMD: "+command);
		}
		dataOut.sendPackage();


		boolean hasPkg = false;
		while ((hasPkg = dataIn.receiveNextPackage()) && dataIn.id != id) {
			handleUnexpectedPackageID();
		}
		while (hasPkg) {
			if (!SecureConnection.isError(dataIn.type)) {
				if (dataIn.type == SecureConnection.TYPE_ACK) {
					//System.out.println("ACK upload of file: "+remoteName);
					if (fileLen<=0) {
						//progress ready for file of length 0
						if (pg!=null) {
							pg.setProgress(pg.getMaxProgress());
							pg.onUpate();
						}
						return true;
					}

					//open file for transfer
					fileIn = new FileInputStream(localFile);

					if (resume) {
						String msg = getMessageFromContent(dataIn.content);
						System.out.println("resume msg :: "+msg+"  filePos before = "+filePos);
						if (msg!=null && msg.equals("upload already complete")) {
							hasNext = false;
							System.out.println(msg);
							//notifyUpdate(fileLen-1, fileLen, null);
							//progress ready
							if (pg!=null) {
								pg.setProgress(pg.getMaxProgress());
								pg.onUpate();
							}
							return true;
						} else {
							try {
								filePos = Long.parseLong(getMessageFromContent(dataIn.content));
								System.out.println("file pos = "+filePos);
								if (filePos>=fileLen) {
									errorMsg = "file position > file length";
									return false;	
								}
								fileIn.skip(filePos);//skip forward to filepos
								if (pg!=null) {
									pg.setProgress(filePos);
									pg.onUpate();
								}
							} catch (Exception ex) {
								//ex.printStackTrace();
								errorMsg = "wrong format: file upload resume position not parseable";
								return false;
							}
						}
					}

					//send data
					byte[] data = new byte[maxPacketSize];
					while (hasNext) {
						//read from file
						int read = fileIn.read(data);
						if (read>0) {
							num++;
							if (read<maxPacketSize) {
								dataOut.setData(id, num, Arrays.copyOf(data, read));
							} else {
								dataOut.setData(id, num, data);
							}
							dataOut.sendPackage();
							filePos += read;
							if (pg!=null) {
								pg.setProgress(filePos);
								pg.onUpate();
							}
						}
						else if (read==-1) {
							hasNext = false;
						}
						//notifyUpdate(filePos, fileLen, null);
						if (filePos>=fileLen) {
							hasNext = false;
						}
					}

					//receive next package
					while ((hasPkg = dataIn.receiveNextPackage()) && dataIn.id != id) {
						handleUnexpectedPackageID();
					}

					if (!hasPkg) {
						handleConnectionClosed();
						return false;
					}
				}
				else if (dataIn.type == SecureConnection.TYPE_ACK_COMPLETE) {
					//System.out.println("Upload complete");
					//progress ready after receiving ACK_COMPLETE
					if (pg!=null) {
						pg.setProgress(pg.getMaxProgress());
						pg.onUpate();
					}
					try {
						fileIn.close();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					return true;
				}
			} else {
				//stop upload
				hasNext = false;
				try {
					fileIn.close();
				} catch (Exception ex) {
					//ex.printStackTrace();
				}
				errorMsg = getMessageFromContent(dataIn.content);
				return false;
			}
		}
		if (!hasPkg) {
			handleConnectionClosed();
			return false;
		} else {
			System.out.println("should never be in this state!!!");
			return false;
		}
	}

	public boolean upload(byte[] data, String absoluteRemoteFilename, boolean resume, ProgressListener pg) throws Exception {
		errorMsg = null;
		long id = SecureConnection.getID();
		int num = 0;

		long dataPos = -1L;
		long dataLen = data.length;
		long maxFilelengthForMD5 = 100*1024*1024L; //100 MB

		boolean hasNext = true;

		if (pg!=null) {
			if (dataLen>0) {
				pg.setMaxProgress(dataLen);
			} else {
				pg.setMaxProgress(1);
			}
			pg.setProgress(0);	
			pg.onUpate();
		}
		String command = null;
		String[] param;
		if (dataLen>0) {
			byte[] md5 = null;
			if (dataLen<maxFilelengthForMD5) {
				try {
					md5 = SecurityHelper.getMD5(data);
				} catch (Exception e) {
					System.out.println("Error calculating md5 hash of data");
					e.printStackTrace();
					md5 = null;
				}
			}
			if (md5!=null) {
				param = new String[] {absoluteRemoteFilename,""+dataLen,SecurityHelper.HexDecoder.encode(md5)};
			} else {
				param = new String[] {absoluteRemoteFilename,""+dataLen};
			}
		} else {
			dataLen = 0L;
			param = new String[] {absoluteRemoteFilename,""+dataLen};
		}

		if (resume) {
			command = "RESUMEPUT "+Util.makeParamsString(param);
		} else {
			command = "PUT "+Util.makeParamsString(param);
		}

		dataOut.setCommand(id, command);
		if (DEBUG) {
			Logger.getFileTransferLogger().logMsg("SEND CMD: "+command);
		}
		dataOut.sendPackage();


		boolean hasPkg = false;
		while ((hasPkg = dataIn.receiveNextPackage()) && dataIn.id != id) {
			handleUnexpectedPackageID();
		}
		while (hasPkg) {
			if (!SecureConnection.isError(dataIn.type)) {
				if (dataIn.type == SecureConnection.TYPE_ACK) {
					//System.out.println("ACK upload of file: "+remoteName);
					if (dataLen<=0) {
						//progress ready for file of length 0
						if (pg!=null) {
							pg.setProgress(pg.getMaxProgress());
							pg.onUpate();
						}
						return true;
					}

					dataPos = 0;

					if (resume) {
						String msg = getMessageFromContent(dataIn.content);
						//System.out.println("resume msg :: "+msg+"  filePos before = "+dataPos);
						if (msg!=null && msg.equals("upload already complete")) {
							hasNext = false;
							System.out.println(msg);
							//notifyUpdate(fileLen-1, fileLen, null);
							//progress ready
							if (pg!=null) {
								pg.setProgress(pg.getMaxProgress());
								pg.onUpate();
							}
							return true;
						} else {
							try {
								dataPos = Long.parseLong(getMessageFromContent(dataIn.content));
								//System.out.println("data pos = "+dataPos);
								if (dataPos>=dataLen) {
									errorMsg = "data position > data length";
									return false;	
								}
								if (pg!=null) {
									pg.setProgress(dataPos);
									pg.onUpate();
								}
							} catch (Exception ex) {
								//ex.printStackTrace();
								errorMsg = "wrong format: data upload resume position not parseable";
								return false;
							}
						}
					}

					//send data
					while (hasNext) {
						//read from data
						int nextPackSize = (int)(dataLen-dataPos);
						if (nextPackSize>maxPacketSize) {
							nextPackSize = maxPacketSize;
						}
						if (nextPackSize>0) {
							num++;
							dataOut.setData(id, num, Arrays.copyOfRange(data, (int)dataPos, (int)dataPos+nextPackSize));
							dataOut.sendPackage();
							dataPos += nextPackSize;
						}
						//notify update
						if (pg!=null) {
							pg.setProgress(dataPos);
							pg.onUpate();
						}
						if (dataPos>=dataLen) {
							hasNext = false;
						}
					}

					//receive next package
					while ((hasPkg = dataIn.receiveNextPackage()) && dataIn.id != id) {
						handleUnexpectedPackageID();
					}

					if (!hasPkg) {
						handleConnectionClosed();
						return false;
					}
				}
				else if (dataIn.type == SecureConnection.TYPE_ACK_COMPLETE) {
					//System.out.println("Upload complete");
					//progress ready after receiving ACK_COMPLETE
					if (pg!=null) {
						pg.setProgress(pg.getMaxProgress());
						pg.onUpate();
					}
					return true;
				}
			} else {
				//stop upload
				hasNext = false;
				errorMsg = getMessageFromContent(dataIn.content);
				return false;
			}
		}
		if (!hasPkg) {
			handleConnectionClosed();
			return false;
		} else {
			System.out.println("should never be in this state!!!");
			return false;
		}
	}


	public void download(String absoluteRemoteFilename, File localFile, boolean showProgress) {
		try {
			System.out.println("Download of file: "+absoluteRemoteFilename+" -> "+localFile.getCanonicalPath());
			ProgressListener pg = null;
			if (showProgress) {
				pg = new ProgressListener() {
					int lastProg = -1;
					public void onUpate() {
						if (getMaxProgress()>0) {
							int prog = (int) (getProgress()*10L / getMaxProgress());
							if (prog > lastProg) {
								System.out.println("  progress: "+prog*10+"%");
								lastProg = prog;
							}
						}
					}
				};
			}
			
			boolean ok = download(absoluteRemoteFilename, localFile, false, pg);
			if (ok) {
				System.out.println("Download finished.\n");
			} else {
				if (errorMsg==null) {
					System.out.println("ERROR\n");
				} else {
					System.out.println("ERROR: "+errorMsg+"\n");
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public boolean download(String absoluteRemoteFilename, File localFile, boolean resume, ProgressListener pg) throws Exception {
		errorMsg = null;
		long id = SecureConnection.getID();
		int num = 0;
		
		long fileLen = -1L;
		long filePos = -1L;
		byte[] md5 = null;

		FileOutputStream fileOut = null;

		String command;
		if (resume && localFile.exists()) {
			long length = localFile.length();
			String param = Util.makeParamsString(new String[]{absoluteRemoteFilename,""+length});
			command = "RESUMEGET "+param;
			filePos = length;
		}
		else {
			resume = false;
			command = "GET "+absoluteRemoteFilename;
			filePos = 0L;
		}
		
		dataOut.setCommand(id, command);
		if (DEBUG) {
			Logger.getFileTransferLogger().logMsg("SEND CMD: "+command);
		}
		dataOut.sendPackage();

		boolean hasPkg = false;
		while ((hasPkg = dataIn.receiveNextPackage()) && dataIn.id != id) {
			handleUnexpectedPackageID();
		}
		while (hasPkg) {
			if (!SecureConnection.isError(dataIn.type)) {
				if (dataIn.type == SecureConnection.TYPE_ACK) {
					String[] p = Util.getParams(getMessageFromContentNN(dataIn.content));
					fileLen = Long.parseLong(p[0]);
					//System.out.println("filelength = "+fileLen);
					if (p.length==2) {
						try {
							md5 = SecurityHelper.HexDecoder.decode(p[1]);
							System.out.println("md5 = "+p[1]);
						} catch (Exception ex) {
							System.out.println("Warning: could not parse md5 hash: "+p[1]);
							md5 = null;
						}
					}	
					if (fileLen==0) {
						localFile.createNewFile();
						if (pg!=null) {
							pg.setMaxProgress(1);
							pg.setProgress(1);
							pg.onUpate();
						}
						return true;

					} else {
						//open file for output
						localFile.getParentFile().mkdirs();
						if (resume) {
							fileOut = new FileOutputStream(localFile,true); //append if resume	
						} else {
							fileOut = new FileOutputStream(localFile); //new if not resume
						}
					}
					if (resume) { //already finished download
						if (filePos>fileLen) {
							//System.out.println("ERROR wrong filesize.");
							//close file
							try {
								fileOut.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
							errorMsg = "Error downloading \""+absoluteRemoteFilename+"\" :: wrong filesize";
							return false;
						}
						else if  (fileLen==filePos) {
							if (pg!=null) {
								pg.setMaxProgress(fileLen);
								pg.setProgress(filePos);
								pg.onUpate();
							}
							if (md5!=null){
								//check md5
								byte[] myMd5 = SecurityHelper.getMD5(localFile);
								if (Arrays.equals(md5, myMd5)) {
									System.out.println("MD5 check ok");
									return true;
								} else {
									System.out.println("MD5 check FAILD!");
									errorMsg = "Error downloading \""+absoluteRemoteFilename+"\" :: MD5 check FAILD!";
									return false;
								}
							} else {
								return true;
							}
						}
					}
					
					if (pg!=null) {
						pg.setMaxProgress(fileLen);
						pg.setProgress(filePos);
						pg.onUpate();
					}
					
					//receive next package
					while ((hasPkg = dataIn.receiveNextPackage()) && dataIn.id != id) {
						handleUnexpectedPackageID();
					}
					if (!hasPkg) {
						handleConnectionClosed();
						return false;
					}
				}
				else if (dataIn.type == SecureConnection.TYPE_DATA) {
					//write content
					fileOut.write(dataIn.content);
					filePos += dataIn.content.length;

					//update progress
					if (pg!=null) {
						pg.setMaxProgress(fileLen);
						pg.setProgress(filePos);
						pg.onUpate();
					}
					
					//finish if filePos at end
					if (filePos>=fileLen) { //finished
						//System.out.println("Download finished: "+localFile.getAbsolutePath());
						//close file
						try {
							fileOut.flush();
							fileOut.close();
						} catch (IOException e) {
							e.printStackTrace();
						}

						if (filePos>fileLen) {
							errorMsg = "Error downloading \""+absoluteRemoteFilename+"\" :: wrong filesize";
							//System.out.println("ERROR wrong filesize.");
							return false;
						} else {
							//successfully finished download
							if (md5!=null){
								//check md5
								byte[] myMd5 = SecurityHelper.getMD5(localFile);
								if (Arrays.equals(md5, myMd5)) {
									System.out.println("MD5 check ok");
									return true;
								} else {
									System.out.println("MD5 check FAILD!");
									errorMsg = "Error downloading \""+absoluteRemoteFilename+"\" :: MD5 check FAILD!";
									return false;
								}
							} else {
								//notifyUpdate(filePos, fileLen, null);
								return true;
							}
						}
					}
					else {  //not finished yet
						
						//receive next package
						while ((hasPkg = dataIn.receiveNextPackage()) && dataIn.id != id) {
							handleUnexpectedPackageID();
						}
						if (!hasPkg) {
							handleConnectionClosed();
							return false;
						}
					}
				}
			} //end of if !error
			else {
				//stop download
				try {
					if (fileOut!=null) {
						fileOut.close();
					}
				} catch (Exception ex) {
					//ex.printStackTrace();
				}
				errorMsg = getMessageFromContent(dataIn.content);
				return false;
			}
		}

		if (!hasPkg) {
			handleConnectionClosed();
			return false;
		} else {
			System.out.println("should never be in this state!!!");
			return false;
		}
	}
	
	public boolean download(String absoluteRemoteFilename, OutputStream out, ProgressListener pg) throws Exception {
		errorMsg = null;
		long id = SecureConnection.getID();
		int num = 0;
		
		long fileLen = -1L;
		long filePos = -1L;
		byte[] md5 = null;

		String command;
		
		command = "GET "+absoluteRemoteFilename;
		filePos = 0L;
		
		dataOut.setCommand(id, command);
		if (DEBUG) {
			Logger.getFileTransferLogger().logMsg("SEND CMD: "+command);
		}
		dataOut.sendPackage();

		boolean hasPkg = false;
		while ((hasPkg = dataIn.receiveNextPackage()) && dataIn.id != id) {
			handleUnexpectedPackageID();
		}
		while (hasPkg) {
			if (!SecureConnection.isError(dataIn.type)) {
				if (dataIn.type == SecureConnection.TYPE_ACK) {
					String[] p = Util.getParams(getMessageFromContentNN(dataIn.content));
					fileLen = Long.parseLong(p[0]);
					//System.out.println("filelength = "+fileLen);
					if (p.length==2) {
						try {
							md5 = SecurityHelper.HexDecoder.decode(p[1]);
							System.out.println("md5 = "+p[1]);
						} catch (Exception ex) {
							System.out.println("Warning: could not parse md5 hash: "+p[1]);
							md5 = null;
						}
					}	
					if (fileLen==0) {
//						localFile.createNewFile();
						if (pg!=null) {
							pg.setMaxProgress(1);
							pg.setProgress(1);
							pg.onUpate();
						}
						return true;

					} 
					else {
						//open file for output
					}
					
					
					if (pg!=null) {
						pg.setMaxProgress(fileLen);
						pg.setProgress(filePos);
						pg.onUpate();
					}
					
					//receive next package
					while ((hasPkg = dataIn.receiveNextPackage()) && dataIn.id != id) {
						handleUnexpectedPackageID();
					}
					if (!hasPkg) {
						handleConnectionClosed();
						return false;
					}
				}
				else if (dataIn.type == SecureConnection.TYPE_DATA) {
					//write content
					out.write(dataIn.content);
					filePos += dataIn.content.length;

					//update progress
					if (pg!=null) {
						pg.setMaxProgress(fileLen);
						pg.setProgress(filePos);
						pg.onUpate();
					}
					
					//finish if filePos at end
					if (filePos>=fileLen) { //finished
						//System.out.println("Download finished: "+localFile.getAbsolutePath());
						try {
							out.flush();
//							fileOut.close();
						} catch (IOException e) {
							e.printStackTrace();
						}

						if (filePos>fileLen) {
							errorMsg = "Error downloading \""+absoluteRemoteFilename+"\" :: wrong filesize";
							//System.out.println("ERROR wrong filesize.");
							return false;
						} 
						else {
							//successfully finished download
//							if (md5 != null) {
//								//check md5
//								byte[] myMd5 = SecurityHelper.getMD5(localFile);
//								if (Arrays.equals(md5, myMd5)) {
//									System.out.println("MD5 check ok");
//									return true;
//								} else {
//									System.out.println("MD5 check FAILD!");
//									errorMsg = "Error downloading \""+absoluteRemoteFilename+"\" :: MD5 check FAILD!";
//									return false;
//								}
//							} else {
								//notifyUpdate(filePos, fileLen, null);
								return true;
//							}
						}
					}
					else {  //not finished yet
						
						//receive next package
						while ((hasPkg = dataIn.receiveNextPackage()) && dataIn.id != id) {
							handleUnexpectedPackageID();
						}
						if (!hasPkg) {
							handleConnectionClosed();
							return false;
						}
					}
				}
			} //end of if !error
			else {
				//stop download
				try {
//					if (fileOut!=null) {
//						fileOut.close();
//					}
				} catch (Exception ex) {
					//ex.printStackTrace();
				}
				errorMsg = getMessageFromContent(dataIn.content);
				return false;
			}
		}

		if (!hasPkg) {
			handleConnectionClosed();
			return false;
		} else {
			System.out.println("should never be in this state!!!");
			return false;
		}
	}


	public void closeConnection() {
		System.out.println("Closing connection.");
		errorMsg = null;
		long id = SecureConnection.getID();
		String command = "QUIT ";
		dataOut.setCommand(id, command);
		if (DEBUG) {
			Logger.getFileTransferLogger().logMsg("SEND CMD: "+command);
		}
		try {
			dataOut.sendPackage();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean isAllowed(int rightType) {
		if (rightsAndDuties==null) {
			return false;
		}
		return rightsAndDuties.hasRight(rightType);
	}

	private String getMessageFromContent(byte[] content) {
		if (content==null) return null;
		try {
			return new String(content,"UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}

	private String getMessageFromContentNN(byte[] content) {
		if (content==null) return "";
		try {
			return new String(content,"UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		return "";
	}


	//	//file.getParent() does NOT work for Files from args[] 
	//	private static String getParentDir(File f) throws Exception {
	//		String d = f.getCanonicalPath();
	//		d = d.substring(0,d.length()-f.getName().length()-1);
	//		return d;
	//	}


	public static void main(String[] args) throws Exception {
		testPlain();

	}

	public static void testPlain() throws Exception {
		OSDXFileTransferAdapter client = new OSDXFileTransferAdapter();

		String username = "testuser";
		String pass = "testpass";

		client.connect("localhost", 4221,"/", username, pass);
		Vector<RemoteFile> rfs = client.list("/");
		for(int i=0;i<rfs.size();i++) {
			RemoteFile r = rfs.elementAt(i);
			System.out.println(r.toString());
		}
	}
	public static void test() {
		try {

			OSDXFileTransferAdapter client = new OSDXFileTransferAdapter();

			OSDXKey mysigning = OSDXKey.fromElement(Document.fromString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>  <keypair><identities><identity><identnum>0001</identnum><email>test</email><mnemonic restricted=\"true\">test</mnemonic><sha256>2D:83:44:CA:3A:4C:85:3A:FB:E9:A3:15:D1:B4:70:BA:CC:7C:16:C7:DC:80:D9:AF:4F:E5:3D:74:4A:58:47:CE</sha256></identity></identities><sha1fingerprint>27:61:62:78:C1:29:F3:C6:A9:03:44:D2:18:36:37:22:E2:9F:63:BF</sha1fingerprint><authoritativekeyserver>localhost</authoritativekeyserver><datapath><step1><datasource>LOCAL</datasource><datainsertdatetime>2011-06-08 04:42:04 GMT+00:00</datainsertdatetime></step1></datapath><valid_from>2011-06-08 04:42:04 GMT+00:00</valid_from><valid_until>2036-06-07 10:42:04 GMT+00:00</valid_until><usage>ONLYSIGN</usage><level>MASTER</level><parentkeyid /><algo>RSA</algo><bits>3072</bits><modulus>00:88:FA:94:53:C5:EC:A9:31:63:FD:20:E3:38:A6:C8:B7:7F:32:41:4B:84:07:A1:AF:81:48:21:6F:D8:87:58:40:BF:DC:1A:E5:BD:A9:8E:ED:90:84:ED:84:BB:2E:04:FB:4F:33:F9:46:8B:0D:D0:58:F5:85:5C:F7:43:93:60:3A:BF:10:4B:92:65:DF:86:13:11:C0:6A:46:6F:4B:68:C3:5B:C3:48:BF:8E:16:00:36:68:A5:0E:C6:03:3B:87:7C:49:0C:18:FE:34:DA:78:03:F8:4B:B3:22:B9:D5:63:D8:74:B8:88:4C:E9:4D:A4:CE:A5:7C:09:B2:19:55:77:55:81:4C:FF:76:CD:87:69:B9:D4:B8:53:BE:9C:07:52:ED:53:09:D7:66:82:FC:A5:0A:79:2B:0D:06:5A:A7:76:77:F9:C4:27:B3:FD:BB:4A:80:44:8F:83:0F:DA:7E:A1:E7:22:24:D0:CC:EA:B2:F0:7F:03:BF:FC:FA:BB:B5:2D:17:63:40:1F:78:41:34:E4:ED:5A:F7:A2:1A:C5:75:FC:0F:93:44:95:AD:09:0D:10:90:D1:77:6B:D4:02:3C:8B:43:0B:91:3C:F5:F9:4A:94:0C:D4:EC:DD:2B:56:D4:AB:B9:C1:A4:74:AC:85:2E:6C:7C:AE:21:17:11:41:CB:9D:1C:16:98:1A:4F:03:8A:34:80:C5:2E:F6:E8:29:DB:3F:1C:EA:B1:B7:21:A9:5F:FA:93:D3:47:FA:DD:28:8F:4F:AA:53:1B:16:32:61:3B:B2:41:0E:37:DB:16:5B:14:AA:A9:D4:6C:C3:3E:0E:8D:90:B9:C4:83:C1:A6:6A:BF:E0:7F:56:AF:7D:7F:47:E5:4E:9C:8E:E9:E4:27:06:F9:0A:8A:22:7A:85:2D:FE:B2:AF:10:EC:5B:36:E7:96:60:E2:77:C5:9F:78:B9:51:A4:CE:7C:1D:D3:43:BF:4F:B1:2C:3F:DF:30:04:B6:7E:40:7E:F3:0E:F1:12:42:78:C6:4A:07</modulus><pubkey><exponent>01:00:01</exponent></pubkey><privkey><exponent><locked><mantraname>mantra: password</mantraname><algo>AES@256</algo><initvector>6F:67:A3:5C:C0:5A:67:F6:30:32:9A:0E:1E:3A:8B:1B</initvector><padding>CBC/PKCS#5</padding><bytes>12:9A:B4:1C:1F:8D:8B:88:39:CD:CD:C4:C9:4D:BE:65:56:2C:48:40:E8:3A:ED:09:F3:BF:0D:A7:A8:09:77:B7:C1:15:FB:8C:93:57:B6:38:F3:31:9B:A8:1E:21:27:40:9E:93:E9:4A:1F:B1:41:02:CF:40:96:2C:A6:17:2B:48:68:58:70:AD:B7:E1:52:6F:09:19:11:67:59:BC:1F:FD:BE:88:C0:B2:FF:76:34:EF:1B:26:DA:9F:4C:47:66:0E:87:BB:1C:09:CB:F2:77:BC:CB:AE:89:CA:C4:65:98:DA:D1:6E:ED:22:08:70:FC:BB:E2:CC:41:7F:5C:12:7B:A6:D3:32:73:FA:BB:E2:95:A6:1C:34:3E:FD:A8:90:D0:9A:0B:4E:96:06:89:DD:6F:35:02:E5:FB:CA:0A:E7:0D:2E:A1:B3:81:17:DE:8D:7F:96:F4:36:AA:02:4E:EF:C0:EF:56:37:C0:53:FB:B9:E3:C0:5B:69:9E:7C:EC:1F:A3:0B:C5:99:B7:5D:54:52:28:17:4A:B1:3D:C8:36:54:2A:94:0C:32:F7:1B:6A:11:37:91:B5:43:5D:BF:DB:6F:D3:B4:37:18:32:81:81:C1:72:80:B6:95:0E:B0:61:FF:05:CE:FC:98:E5:F1:E4:D1:33:B7:EF:B8:EB:EF:6B:A7:FE:C6:37:77:CF:43:12:C3:5F:2B:2A:51:19:E8:C4:6D:F6:0E:15:C4:C3:AD:BE:4C:FE:D6:D5:3A:00:D8:E0:0B:00:78:A9:5F:D8:21:28:06:B8:74:F2:06:23:63:81:B8:CC:03:EC:2C:ED:6B:74:23:E4:31:C1:4E:9C:B2:24:F0:93:A4:7D:6C:6A:E2:C1:95:EC:EA:DF:DC:85:2B:60:15:24:DE:FD:DD:94:BF:CF:C9:8D:74:DE:8A:D8:89:DC:16:FA:9D:28:37:EE:65:44:AD:61:FB:33:D4:E8:66:D3:BA:D8:38:E1:16:F1:EF:97:FF:01:D7:C3:7D:76:CE:A2:12:1C:24:AC:EC:AE:AC:2B:08:99:7A:D8:A4:41:49:A4:0E:AF:83</bytes></locked></exponent></privkey><gpgkeyserverid /></keypair>  ").getRootElement());
			mysigning.unlockPrivateKey("password");			
			String username = "testuser";

			client.connect("localhost", 4221,"/", mysigning, username);
			
			client.upload(new File("FDL.txt"), "/FDL.txt");
			client.download("/FDL.txt", new File("/tmp/FDL.txt"),true);
			
			client.mkdir("/blub");
			RemoteFile rf = client.fileinfo("/blub");
			if (rf != null) {
				System.out.println("FileInfo:: "+rf.toString());
			}
			client.rename("/blub", "blablub");

			Vector<RemoteFile> list = client.list("/");
			if (list!=null) {
				System.out.println("LIST");
				for (RemoteFile f : list) {
					System.out.println(f.toString());
				}
				System.out.println();
			}
			client.delete("/blablub");
			client.delete("/FDL.txt");

			list = client.list("/");
			if (list!=null) {
				System.out.println("LIST");
				for (RemoteFile f : list) {
					System.out.println(f.toString());
				}
				System.out.println();
			}

			



			client.closeConnection();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private class SimpleCommand {
		public String errorMsg = null;

		//hook in here
		public boolean onACK() {
			return true;
		}

		private SecureConnection dataIn;
		private SecureConnection dataOut;

		public SimpleCommand(SecureConnection dataIn, SecureConnection dataOut) {
			this.dataIn = dataIn;
			this.dataOut = dataOut;
		}

		public boolean process(String command) {
			long id = SecureConnection.getID();
			errorMsg = null; 
			dataOut.setCommand(id, command);
			if (DEBUG) {
				Logger.getFileTransferLogger().logMsg("SEND CMD: "+command);
			}
			try {
				dataOut.sendPackage();

				boolean hasPkg = false;
				while ((hasPkg = dataIn.receiveNextPackage()) && dataIn.id != id) {
					handleUnexpectedPackageID();
				}
				if (hasPkg) {
					if (dataIn.type == SecureConnection.TYPE_ACK) {
						return onACK();
					}
					else if (SecureConnection.isError(dataIn.type)) {
						errorMsg = getMessageFromContent(dataIn.content);
						return false;
					}
					return false;
				} else {
					handleConnectionClosed();
					return false;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				errorMsg = ex.getMessage();
				return false;
			}
		}
	}

}
