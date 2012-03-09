package org.fnppl.opensdx.ftp_bridge;
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Vector;


import org.fnppl.opensdx.file_transfer.CommandResponseListener;
import org.fnppl.opensdx.file_transfer.OSDXFileTransferClient;
import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferCommand;
import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferDeleteCommand;
import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferListCommand;
import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferMkDirCommand;
import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferUploadCommand;
import org.fnppl.opensdx.file_transfer.model.RemoteFile;
import org.fnppl.opensdx.file_transfer.model.Transfer;

public class FTP_OSDX_BridgeThread extends Thread implements CommandResponseListener {

	private FTP_OSDX_Bridge control = null;
	private String host;
	private int next_port = -1;
	private boolean running;
	
	private Socket ftpsocket;
	private PrintWriter out;
	
	private OSDXFileTransferClient osdxclient = null;
	private FTP_OSDX_BridgeUser user = null;
	
	private HashMap<Long,Transfer> transfersInProgress = new HashMap<Long, Transfer>();
	
	private String pwd = "/";
	
	public FTP_OSDX_BridgeThread(Socket ftpsocket, FTP_OSDX_Bridge control) {
		this.ftpsocket = ftpsocket;
		this.control = control;
	}
	
	
	public void run() {
		
		InetAddress inet;
		try {
			osdxclient = new OSDXFileTransferClient();
			osdxclient.addResponseListener(this);
			inet = ftpsocket.getInetAddress();
			host = inet.getHostName();
			
			System.out.println("host: "+host);
			
			//init connection
			BufferedReader in = new BufferedReader(new InputStreamReader(ftpsocket.getInputStream()));
			out = new PrintWriter(ftpsocket.getOutputStream(), true);
			out.println("220 FTP Server ready.\r");

			
			running = true;
			while (running) {
				try {
					String str = in.readLine();
					
					String command = str;
					String param = null;
					int ind = str.indexOf(' ');
					if (ind>0) {
						command = str.substring(0,ind);
						if (str.length()>ind+1) {
							param = str.substring(ind+1);
						}
					}
					command = command.toUpperCase();
					try {
						Method commandHandler = getClass().getMethod("handle_"+command, String.class);
						commandHandler.invoke(this, param);
	
					} catch (NoSuchMethodException ex) {
						handle_command_not_implemented(str);
					} catch (InvocationTargetException ex) {
						handle_command_not_implemented(str);
					} catch (IllegalAccessException ex) {
						handle_command_not_implemented(str);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					running = false;
				}
			}
			ftpsocket.close();
			osdxclient.closeConnection();
		} catch (Exception e) { // System.out.println(e);
			e.printStackTrace();
		}
	}
	
//	public boolean ensureConnection() {
//		boolean connected = osdxclient.isConnected();
//		if (!connected) {
//			try {
//				connected = osdxclient.connect(user.host, user.port, user.prepath, user.signingKey, user.username);
//				
//			} catch (Exception ex) {
//				ex.printStackTrace();
//				try {
//					System.out.println("Closing connection to osdx server ...");	
//					connected = false;
//					osdxclient.closeConnection();
//				} catch (Exception ex2) {
//					ex2.printStackTrace();
//				}				
//			}
//		}
//		if (!connected) {
//			System.out.println("ensureConnection -> false");
//			out.println("426 Connection closed; transfer aborted.");
//		}
//		return connected;
//	}
	
	public void handle_RETR(final String param) {

		Thread t = new Thread() {
			public void run() {
				try {
				//	if (ensureConnection()) {
						//loading file data to tmp file
						String filename = param;
						if (!filename.startsWith("/")) {
							if (pwd.equals("/")) {
								filename = "/"+param;
							} else {
								filename = pwd+"/"+param;
							}
						}
						
						System.out.println("downloading file: "+filename);
						final File tmpFile = File.createTempFile("osdx"+System.currentTimeMillis(), ".tmp");
						tmpFile.delete();
						
						long id = osdxclient.download(filename, tmpFile);
						Transfer t = new Transfer();
						t.type = "download";
						t.file = tmpFile;
						
						transfersInProgress.put(id,t);
						
//						final FileTransferProgress progress = new FileTransferProgress(FileTransferProgress.END_UNKNOWN) {
//							public void onUpdate() {
//								super.onUpdate();
//								if (hasFinished()) {
//									try {
//										if (tmpFile.exists() && tmpFile.length()==getProgressEnd()) {
//											out.println("150 Binary data connection");
//											
//											//transfer downloaded file to ftp client
//											FileInputStream fin = new FileInputStream(tmpFile);
//											
//											Socket t = new Socket(host, next_port);
//											OutputStream out2 = t.getOutputStream();
//											byte buffer[] = new byte[1024];
//											int read;
//											try {
//												while ((read = fin.read(buffer)) != -1) {
//													out2.write(buffer, 0, read);
//												}
//												out2.close();
//												out.println("226 transfer complete");
//												fin.close();
//												tmpFile.delete();
//												t.close();
//											} catch (IOException e) {
//												e.printStackTrace();
//											}
//										} else {
//											out.println("550 Requested action not taken. File unavailable (e.g., file not found, no access).");
//										}
//									} catch (Exception ex) {
//										ex.printStackTrace();
//									}
//								}
//							};
//						};
//						osdxclient.downloadFile(filename, tmpFile, progress);
//						
//					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};
		t.start();
	}
	
	public void handle_STOR(final String param) { //upload file to osdx server
		Thread t = new Thread() {
			public void run() {
				try {
					//if (ensureConnection()) {
						out.println("150 Binary data connection");
						System.out.println("HANDLE_STOR :: "+param);
						File tmpFile = File.createTempFile("osdx"+System.currentTimeMillis(), ".tmp");
						tmpFile.deleteOnExit();
						FileOutputStream fout = new FileOutputStream(tmpFile);
						String filename = param;
						if (!filename.startsWith("/")) {
							if (pwd.equals("/")) {
								filename = "/"+param;
							} else {
								filename = pwd+"/"+param;
							}
						}
						Socket t = new Socket(host, next_port);
						InputStream in2 = t.getInputStream();
						byte buffer[] = new byte[1024];
						int read;
						try {
							while ((read = in2.read(buffer)) != -1) {
								fout.write(buffer, 0, read);
							}
							in2.close();
							fout.close();
							t.close();
							System.out.println("starting upload ...");
							long id = osdxclient.upload(tmpFile, filename);
							Transfer transfer = new Transfer();
							transfer.type = "upload";
							transfer.file = tmpFile;
							System.out.println("STOR command "+id);
							transfersInProgress.put(id,transfer);
							
						} catch (IOException e) {
							e.printStackTrace();
						}
					//}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};
		t.start();
	}
		
	
	public void handle_TYPE(String str) {
		out.println("200 type set");
	}
	
	public void handle_DELE(String str) {
		//TODO
		try {
			//if (ensureConnection()) {
			String filename = str;
			if (!filename.startsWith("/")) {
				if (pwd.equals("/")) {
					filename = "/"+str;
				} else {
					filename = pwd+"/"+str;
				}
			}
			osdxclient.delete(filename);
				//out.println("250 CWD command succesful");
			//}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	public void handle_CDUP(String str) {
		try {
			//if (ensureConnection()) {
				//osdxclient.cd_up();
				if (!pwd.equals("/")) {
					int ind = pwd.lastIndexOf('/');
					if (ind>0) {
						pwd = pwd.substring(0,ind);
					} else {
						pwd="/";
					}
					out.println("250 CWD command succesful");
				}
				//
			//}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	public void handle_CWD(String param) {
		try {
			//if (ensureConnection()) {
				if (param.startsWith("/")) {
					pwd = param;
				} else {
					if (pwd.equals("/")) {
						pwd = "/"+param;
					} else {
						pwd = pwd+"/"+param;
					}
				}
				//osdxclient.cd(param);
				out.println("250 CWD command succesful");
			//}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	public void handle_QUIT(String param) {
		osdxclient.closeConnection();
		running = false;
		out.println("221 Goodbye");
	}
	public void handle_USER(String param) {
		user = control.getUser(param);
		out.println("331 Password");	
	}
	
	public void handle_PASS(String param) {
		if (user!=null && user.ftppassword.equals(param)) { //&& ensureConnection()) {
			try {
				osdxclient.connect(user.host, user.port, user.prepath, user.signingKey, user.username);
				out.println("230 User " + user.ftpusername + " logged in.");	
			} catch (Exception e) {
				e.printStackTrace();
				out.println("430 Error: cannot connect to given account");
			}
		} else {
			out.println("430 Invalid username or password");
		}
	}
	
	public void handle_PWD(String str) {
		try {
			//if (ensureConnection()) {
				//String pwd = osdxclient.pwd();
				out.println("257 \""+pwd+"\" is current directory");
			//}
		} catch (Exception ex) {
			
			ex.printStackTrace();
		}
	}
	
	public void handle_MKD(String param) {
		try {
			//if (ensureConnection()) {
				if (param.startsWith("/")) {
					osdxclient.mkdir(param);
				} else {
					if (pwd.equals("/")) {
						osdxclient.mkdir("/"+param);
					} else {
						osdxclient.mkdir(pwd+"/"+param);
					}
				}
				
			//}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void handle_SYS(String str) {
		out.println("500 SYS not understood");
	}
	public void handle_PORT(String str) {
		try {
			out.println("200 PORT command successful");
			int lng, lng2, lng1;
			String a1="", a2="";
			
			lng = str.length() - 1;
			lng2 = str.lastIndexOf(",");
			lng1 = str.lastIndexOf(",", lng2 - 1);
			for (int i = lng1 + 1; i < lng2; i++) {
				a1 = a1 + str.charAt(i);
			}
			for (int i = lng2 + 1; i <= lng; i++) {
				a2 = a2 + str.charAt(i);
			}
			int ip1 = Integer.parseInt(a1);
			int ip2 = Integer.parseInt(a2);
			next_port = ip1 * 16 * 16 + ip2;
			//System.out.println("next port = "+next_port);	
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void handle_LIST(String param) {
		try {
			//if (ensureConnection()) {
			if (param==null || param.length()==0) {
				osdxclient.list(pwd,null);
			}
			else if (param.startsWith("/")) {
				osdxclient.list(param,null);
			} else {
				if (pwd.equals("/")) {
					osdxclient.list("/"+param,null);
				} else {
					osdxclient.list(pwd+"/"+param,null);	
				}
			}
			//}
		} catch (Exception ex) {
			ex.printStackTrace();
//			try {
//				System.out.println("Closing connection to osdx server ...");	
//				osdxclient.closeConnection();
//			} catch (Exception ex2) {
//				ex2.printStackTrace();
//			}
//			out.println("426 Connection closed; transfer aborted.");
		}
	}
	
	public void handle_FEAT(String str) {
		out.println("211-Features supported\n211 End");
	}
	
	public void handle_NOOP(String str) {
		out.println("200 NOOP command successful");
	}
	
	public void handle_PASV(String str) {
		int port = ftpsocket.getLocalPort();
		int p1 = port/256;
		int p2 = port%256;
		out.println("227 Entering Passive Mode (127,0,0,1,"+p1+","+p2+")");
	}
	public void handle_command_not_implemented(String str) {
		out.println("500 "+str+" not understood");
		System.out.println("command: "+str+" not implemented");
	}


	public void onSuccess(OSDXFileTransferCommand command) {
		System.out.println("Command "+command.getID()+" successful :: "+command.getClass().getSimpleName());
		Transfer transfer = transfersInProgress.get(command.getID());
		if (transfer==null) {
			System.out.println("no transfer type");
		}
		if (transfer!=null) {
			if (transfer.type.equals("upload")) {
				if (command instanceof OSDXFileTransferUploadCommand) {
					
					System.out.println("upload success");
					
					out.println("226 transfer complete");
					//transfer.file.delete();
					//transfersInProgress.remove(command.getID());
				}
			} else {
				try {
					File tmpFile = transfer.file;
					if (tmpFile.exists()) {
						out.println("150 Binary data connection");
						
						//transfer downloaded file to ftp client
						FileInputStream fin = new FileInputStream(tmpFile);
						
						Socket t = new Socket(host, next_port);
						OutputStream out2 = t.getOutputStream();
						byte buffer[] = new byte[1024];
						int read;
						try {
							while ((read = fin.read(buffer)) != -1) {
								out2.write(buffer, 0, read);
							}
							out2.close();
							out.println("226 transfer complete");
							fin.close();
							tmpFile.delete();
							t.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else {
						out.println("550 Requested action not taken. File unavailable (e.g., file not found, no access).");
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} else {
			if (command instanceof OSDXFileTransferDeleteCommand) {
				out.println("250 CWD command succesful");
			}
			else if (command instanceof OSDXFileTransferListCommand) {
				try {
					out.println("150 ASCII data");
					Socket t = new Socket(host, next_port);
					PrintWriter out2 = new PrintWriter(t.getOutputStream(),	true);
					Vector<RemoteFile> list = ((OSDXFileTransferListCommand)command).getList();
					for (RemoteFile f : list) {
						//System.out.println("LIST::"+f.getName());
						String di;
						if (f.isDirectory()) {
							di = "drwxr-xr-x ";
						} else {
							di = "-rw-r--r--";
						}
						String name = f.getName();
						//name = name.replace(' ', '_');
						String e = di+"1 user group "+f.getLength()+" Jul 04 20:00 "+name+"";
						out2.println(e);
					}
					t.close();
					out.println("226 transfer complete");
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				
			}
			else if (command instanceof OSDXFileTransferMkDirCommand) {
				out.println("250 MKD command succesful");
			}
		}
	}
	
	public void onError(OSDXFileTransferCommand command, String msg) {
		//Transfer transfer = transfersInProgress.get(command.getID());
		//if (transfer!=null) {
			out.println("550 Requested action not taken. File unavailable (e.g., file not found, no access).");
		//}
	}

	public void onStatusUpdate(OSDXFileTransferCommand command, long progress,long maxProgress, String msg) {
		
	}


	
}
