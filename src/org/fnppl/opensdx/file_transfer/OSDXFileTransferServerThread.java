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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;

import org.fnppl.opensdx.common.Util;
import org.fnppl.opensdx.file_transfer.helper.ClientSettings;
import org.fnppl.opensdx.file_transfer.helper.RightsAndDuties;
import org.fnppl.opensdx.file_transfer.model.RemoteFile;
import org.fnppl.opensdx.security.AsymmetricKeyPair;
import org.fnppl.opensdx.security.OSDXKey;
import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.security.SymmetricKey;
import org.fnppl.opensdx.xml.Document;

public class OSDXFileTransferServerThread extends Thread {

	private OSDXFileTransferServer server;
	private Socket socket;
	private BufferedInputStream in;
	private BufferedOutputStream out;
	private SecureConnection data = null;
	private ClientSettings cs = null;
	
	private HashMap<Long, FileUploadInfo> uploads = new HashMap<Long, FileUploadInfo>();

	
	private OSDXKey mySigningKey;
	private String clientID = "";
	private SymmetricKey key= null;
	private String client_keyid = null; 
	private byte[] client_nonce = null;
	private byte[] server_nonce = null;
	
	private long maxFilelengthForMD5 = 100*1024*1024L; //100 MB
	protected int maxPacketSize = 50*1024; //50kB
	

	public OSDXFileTransferServerThread(Socket socket, OSDXKey mySigningKey, OSDXFileTransferServer server) throws Exception {
		this.socket = socket;
		this.mySigningKey = mySigningKey;
		this.server = server;
		
		in = new BufferedInputStream(socket.getInputStream());
		out = new BufferedOutputStream(socket.getOutputStream());
		data = new SecureConnection(null, in,out);
		clientID = socket.getInetAddress().getHostAddress()+":"+socket.getPort();
		System.out.println("connected to client: "+clientID);
	}


	public void run() {
		boolean run = initSecureConnection();
		
		//just wait and listen
		while (run && socket.isConnected()) {
			try {
				run = data.receiveNextPackage();
				if (run) {
					onRequestReceived(data.id, data.num, data.type, data.content);
					//String content = new String(data.content, "UTF-8");
					//System.out.println("SERVER RECEIVED :: "+content);
				}
			} catch (Exception ex) {
				run = false;
				ex.printStackTrace();
			}
		}
		System.out.println("closing socket to client: "+clientID);
	}

	public void onRequestReceived(long commandid, int num, byte code, byte[] content) {
		try {
			System.out.print("RECEIVED REQUEST : id="+commandid);
			if (code == SecureConnection.TYPE_DATA && content !=null) {
				System.out.println(", DATA len="+content.length);
				//handle data
				FileUploadInfo upload = uploads.get(commandid);
				if (upload!=null) {
					if (upload.out==null) {
						upload.out = new FileOutputStream(upload.file, true);
					}
					System.out.println("appending "+content.length+" bytes to "+upload.file.getAbsolutePath());
					upload.out.write(content);
					upload.loaded += content.length;
					if (upload.loaded>=upload.length) {
						//ready with upload
						System.out.println("upload ready: "+upload.file.getAbsolutePath());
						upload.out.close();
						if (upload.loaded == upload.length) {
							boolean ok = true;
							if (upload.md5!=null) {
								//check md5
								try {
									byte[] my_md5 = SecurityHelper.getMD5(upload.file);
									byte[] your_md5 = SecurityHelper.HexDecoder.decode(upload.md5);
									if (!Arrays.equals(my_md5,your_md5)) {
										ok = false;
										System.out.println("MD5 check faild");
									} else {
										System.out.println("MD5 check ok");
									}
								} catch (Exception ex) {
									ok = false;
								}
								if (!ok) {
									data.setError(commandid, num, "md5 check failed");
									data.sendPackage();
								}
							}
							if (ok) {
								data.setAckComplete(commandid, num);
								data.sendPackage();
							}
						} else {
							data.setError(commandid, num, "wrong filesize");
							data.sendPackage();
						}
					} else {
						//dont ack every package ?
					}
				} else {
					System.out.println("No UploadInfo found for id:"+commandid);
				}
				
			}
			else if (content!=null) { //if (code == SecureConnection.TYPE_TEXT) {
				String text = new String(content,"UTF-8");
				System.out.println(", "+SecurityHelper.HexDecoder.encode(new byte[]{(byte)code})+" :: MSG: "+text);
				
				//parse command
				String command = text;
				String param = null;
				int ind = text.indexOf(' ');
				if (ind>0) {
					command = text.substring(0,ind);
					if (text.length()>ind+1) {
						param = text.substring(ind+1);
					}
				}
				//find right handler method
				command = command.toLowerCase();
				try {
					Method commandHandler = getClass().getMethod("handle_"+command, Long.TYPE, Integer.TYPE, Byte.TYPE, String.class);
					commandHandler.invoke(this, commandid, num, code, param);

				} catch (NoSuchMethodException ex) {
					System.out.println("NoSuchMethodException");
					handle_command_not_implemented(commandid, num, code, command, param);
				} catch (InvocationTargetException ex) {
					System.out.println("InvocationTargetException");
					handle_command_not_implemented(commandid, num, code, command, param);
				} catch (IllegalAccessException ex) {
					System.out.println("IllegalAccessException");
					handle_command_not_implemented(commandid, num, code, command, param);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public boolean initSecureConnection() {
		try {
			byte[] clientRequest = data.receiveRawBytesPackage();
			String[] lines = new String(clientRequest,"UTF-8").split("\n");
			if (lines!=null) {
//				for (int i=0;i<lines.length;i++) {
//					System.out.println("("+(i+1)+")"+" "+lines[i]);
//				}

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
					key = new SymmetricKey(key_bytes, iv);
					data.key = key;

					//send packet
					data.sendRawBytes(msg.toString().getBytes("UTF-8"));
					data.sendRawBytes(enc);
					
				//	data.setAck(0, 0);

				} else {
					ok = false;
					//				String msg = version+" 421 You are not authorized to make the connection\n";
					//				msg += host+"\n";
					socket.close();
				}
				return ok;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return false;
	}


	// COMMAND IMPLEMENTATIONS
	
	public void handle_command_not_implemented(long commandid, int num, byte code, String command, String param) throws Exception {
		System.out.println("COMMAND NOT UNDERSTOOD: "+command+" PARAM="+param);
		data.setErrorCommandNotUnderstood(commandid, num);
		data.sendPackage();
	}
	
	public void handle_login(long commandid, int num, byte code, String username) throws Exception {
		System.out.println("handle_login :: "+username);
		if (username!=null) {
			String userid = username+"::"+client_keyid;
			System.out.println("userid: "+userid);
			cs  = server.getClientSetting(userid);
			if (cs!=null) {
				//login ok -> ACK with rights and duties
				String param = Util.makeParamsString(new String[]{client_keyid, Document.buildDocument(cs.getRightsAndDuties().toElement()).toStringCompact()});
				System.out.println("SENDING: ACK :: "+param);
				data.setAck(commandid, num, param);
				data.sendPackage();
			}
			else {
				//login failed
				data.setError(commandid, num, "ERROR IN LOGIN :: ACCESS DENIED");
				data.sendPackage();
			}
		} else {
			//login failed
			data.setError(commandid, num, "ERROR IN LOGIN :: MISSING USERNAME");
			data.sendPackage();
		}
	}
	
	public void handle_mkdir(long commandid, int num, byte code, String param) throws Exception {
		ensureAllowed(RightsAndDuties.ALLOW_MKDIR, commandid, num, "Sorry, no right to make directories.");
		if (param!=null) {
			File path = null;
			if (param.startsWith("/")) {
				path = new File(cs.getLocalRootPath()+param);
				
				if (!cs.isAllowed(path)) {
					data.setError(commandid, num, "restricted path");
					data.sendPackage();
				}
				else if (path.exists()) {
					data.setError(commandid, num, "path already exists");
					data.sendPackage();
				}
				else {
					boolean ok = path.mkdirs();
					if (ok) {
						data.setAck(commandid, num);
						data.sendPackage();
					} else {
						data.setError(commandid, num, "error in mkdir");
						data.sendPackage();
					}
				}
			} else {
				data.setError(commandid, num, "path must be absolute");
				data.sendPackage();
			}
		} else {
			data.setError(commandid, num, "missing path parameter");
			data.sendPackage();
		}
	}
	
	
	public void handle_delete(long commandid, int num, byte code, String param) throws Exception {
		ensureAllowed(RightsAndDuties.ALLOW_DELETE, commandid, num, "Sorry, no right to delete files or directories.");
		if (param!=null) {
			File file = null;
			if (param.startsWith("/")) {
				file = new File(cs.getLocalRootPath()+param);
				
				if (!cs.isAllowed(file)) {
					data.setError(commandid, num, "restricted file");
					data.sendPackage();
				}
				else if (!file.exists()) {
					data.setError(commandid, num, "file does not exist");
					data.sendPackage();
				} else {
					//delete
					System.out.println("DELETING: "+file.getAbsolutePath());
					if (file.isDirectory()) {
						boolean ok = deleteDirectory(file);
						if (ok) {
							data.setAck(commandid, num);
							data.sendPackage();
						} else {
							data.setError(commandid, num, "directory \""+param+"\" could not be deleted");
							data.sendPackage();
						}
					} else {
						boolean ok = file.delete();
						if (ok) {
							data.setAck(commandid, num);
							data.sendPackage();
						} else {
							data.setError(commandid, num, "file \""+param+"\" could not be deleted");
							data.sendPackage();
						}
					}
				}
			}  else {
				data.setError(commandid, num, "path must be absolute");
				data.sendPackage();
			}
		} else {
			data.setError(commandid, num, "missing path/file parameter");
			data.sendPackage();
		}
	}
	
	private static boolean deleteDirectory(File path) {
		if( path.exists() ) {
			File[] list = path.listFiles();
			for(int i=0; i<list.length; i++) {
				if(list[i].isDirectory()) {
					deleteDirectory(list[i]);
				}
				else {
					list[i].delete();
				}
			}
		}
		return(path.delete());
	}
	
	public void handle_rename(long commandid, int num, byte code, String param) throws Exception {
		ensureAllowed(RightsAndDuties.ALLOW_RENAME, commandid, num, "Sorry, no right to rename files or directories.");
		if (param!=null && param.length()>0) {
			String[] p = Util.getParams(param);
			if (p.length==2) {
				if (p[1].contains("/") || p[1].contains("\\")) {
					data.setError(commandid, num, "wrong destination filename");
					data.sendPackage();
				} else {
					File file = null;
					if (p[0].startsWith("/")) {
						file = new File(cs.getLocalRootPath()+p[0]);
						
						if (!cs.isAllowed(file)) {
							data.setError(commandid, num, "restricted file");
							data.sendPackage();
						}
						else if (!file.exists()) {
							data.setError(commandid, num, "file does not exist");
							data.sendPackage();
						} else {
							//init destination file
							File dest = new File(file.getParentFile(),p[1]);
							if (!cs.isAllowed(file)) {
								data.setError(commandid, num, "restricted destination file");
								data.sendPackage();
							}
							else if (dest.exists()) {
								data.setError(commandid, num, "destination file already exists");
								data.sendPackage();
							}
							else {
								//rename
								System.out.println("RENAMEING: "+file.getAbsolutePath()+" -> "+dest.getAbsolutePath());
								boolean ok = file.renameTo(dest);
								if (ok) {
									data.setAck(commandid, num);
									data.sendPackage();
								} else {
									data.setError(commandid, num, "file \""+p[0]+"\" could not be renamed to "+p[1]);
									data.sendPackage();
								}
							}
						}
					}  else {
						data.setError(commandid, num, "path must be absolute");
						data.sendPackage();
					}
				}
			} else {
				data.setError(commandid, num, "missing destination filename parameter");
				data.sendPackage();
			}
		} else {
			data.setError(commandid, num, "missing path/file parameter");
			data.sendPackage();
		}
	}
	
	public void handle_file(long commandid, int num, byte code, String param) throws Exception {
		ensureAllowed(RightsAndDuties.ALLOW_LIST, commandid, num, "Sorry, no right to read fileinfo.");
		if (param!=null) {
			
			File file = null;
			if (param.startsWith("/")) {
				file = new File(cs.getLocalRootPath()+param);
				
				if (!cs.isAllowed(file)) {
					data.setError(commandid, num, "restricted file");
					data.sendPackage();
				}
				else if (!file.exists()) {
					data.setError(commandid, num, "file does not exist");
					data.sendPackage();
				} else {
					//ack -> send fileinfo
					RemoteFile rf = cs.getAsRemoteFile(file);
					if (rf!=null) {
						data.setAck(commandid, num, rf.toParamString());
						data.sendPackage();
					} else {
						data.setError(commandid, num, "error retrieving file information");
						data.sendPackage();
					}
				}
			}  else {
				data.setError(commandid, num, "path must be absolute");
				data.sendPackage();
			}
		} else {
			data.setError(commandid, num, "missing path/file parameter");
			data.sendPackage();
		}
	}
	
	public void handle_list(long commandid, int num, byte code, String param) throws Exception {
		ensureAllowed(RightsAndDuties.ALLOW_LIST, commandid, num, "Sorry, no right to read directory info.");
		if (param!=null) {
			File file = null;
			if (param.startsWith("/")) {
				file = new File(cs.getLocalRootPath()+param);
				if (!cs.isAllowed(file)) {
					data.setError(commandid, num, "restricted file");
					data.sendPackage();
				}
				else if (!file.exists()) {
					data.setError(commandid, num, "directory does not exist");
					data.sendPackage();
				}
				else if (!file.isDirectory()) {
					data.setError(commandid, num, "this is not a directory");
					data.sendPackage();
				}
				else {
					//ack -> send list
					StringBuffer b = new StringBuffer();
					
					File[] list = file.listFiles();
					for (int i=0;i<list.length;i++) {
						RemoteFile rf = cs.getAsRemoteFile(list[i]);
						if (rf!=null) {
							b.append(rf.toParamString());
						}
					}
					data.setAck(commandid, num, b.toString());
					data.sendPackage();
				}
			} else {
				data.setError(commandid, num, "path must be absolute");
				data.sendPackage();
			}
		} else {
			data.setError(commandid, num, "missing path parameter");
			data.sendPackage();
		}
	}
	
	public void handle_put(long commandid, int num, byte code, String param) throws Exception {
		ensureAllowed(RightsAndDuties.ALLOW_UPLOAD, commandid, num, "Sorry, no right to upload files.");
		if (param!=null) {
			String[] p = Util.getParams(param);
			
			File file = null;
			long length = -1L;
			try {
				length = Long.parseLong(p[1]);
			} catch (Exception ex) {}
			if (length<0) {
				data.setError(commandid, num, "missing or wrong file length parameter");
				data.sendPackage();	
			} else {
				if (p[0].startsWith("/")) {
					file = new File(cs.getLocalRootPath()+p[0]);
					if (!cs.isAllowed(file)) {
						data.setError(commandid, num, "restricted file");
						data.sendPackage();
					}
					else if (file.exists()) {
						data.setError(commandid, num, "file already exists");
						data.sendPackage();
					} else {
						//ack -> ready for upload
						data.setAck(commandid, num);
						data.sendPackage();
						if (length==0) {
							file.createNewFile();
						} else {
							FileUploadInfo info = new FileUploadInfo();
							info.file = file;
							info.length = length;
							info.out = null;
							info.loaded = 0L;
							if (p.length>2) {
								info.md5 = p[2];
							} else {
								info.md5 = null;
							}
							uploads.put(commandid,info);
						}
					}
				} else {
					data.setError(commandid, num, "path must be absolute");
					data.sendPackage();
				}
			}
		} else {
			data.setError(commandid, num, "missing path/file parameter");
			data.sendPackage();
		}
	}
	
	public void handle_get(long commandid, int num, byte code, String param) throws Exception {
		ensureAllowed(RightsAndDuties.ALLOW_DOWNLOAD, commandid, num, "Sorry, no right to download files.");
		if (param!=null) {
			
			File file = null;
			if (param.startsWith("/")) {
				file = new File(cs.getLocalRootPath()+param);
				
				if (!cs.isAllowed(file)) {
					data.setError(commandid, num, "restricted file");
					data.sendPackage();
				}
				else if (!file.exists()) {
					data.setError(commandid, num, "file does not exist");
					data.sendPackage();
				}
				else if (file.isDirectory()) {
					data.setError(commandid, num, "directory download not implemented");
					data.sendPackage();
				} else {
					//send ack with file length and md5
					String md5 = null;
					if (file.length()<=maxFilelengthForMD5) {
						try {
							md5 = SecurityHelper.HexDecoder.encode(SecurityHelper.getMD5(file));
						} catch (Exception ex) {
							System.out.println("Error calculating md5 hash of "+file.getAbsolutePath());
							ex.printStackTrace();
							md5 = null;
						}
					}
					String[] response;
					if (md5==null) {
						response = new String[]{""+file.length()};
					} else {
						response = new String[]{""+file.length(),md5};
					}
					data.setAck(commandid, num, Util.makeParamsString(response));
					data.sendPackage();
					
					//send file data
					FileInputStream fileIn = new FileInputStream(file);
					int read = 0;
					byte[] content = new byte[maxPacketSize];
					while ((read = fileIn.read(content))>0) {
						num++;
						if (read<maxPacketSize) {
							data.setData(commandid, num, Arrays.copyOf(content, read));
						} else {
							data.setData(commandid, num, content);
						}
						data.sendPackage();
						
						//TODO it would be a good idea to check for error or cancel messages
						//     when sending large files
					}
				}
			} else {
				data.setError(commandid, num, "path must be absolute");
				data.sendPackage();
			}
		} else {
			data.setError(commandid, num, "missing path/file parameter");
			data.sendPackage();
		}
	}
	
	public void handle_quit(long commandid, int num, byte code, String param) throws Exception {
		//do nothing... socket closes automatically on client disconnection 
	}
	
	
	private boolean ensureAllowed(int rightType, long id, int num, String msg) throws Exception {
		if (cs==null || cs.getRightsAndDuties()==null || !cs.getRightsAndDuties().hasRight(rightType)) {
			data.setError(id, num, msg);
			data.sendPackage();
			return false;
		}
		return true;
	}

	private class FileUploadInfo {
		public File file = null;
		public long length = -1L;
		public long loaded = 0L;
		public FileOutputStream out = null;
		public String md5 = null;
	}
}

