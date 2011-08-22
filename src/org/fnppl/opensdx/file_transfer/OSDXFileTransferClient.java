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
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;

import org.fnppl.opensdx.common.Util;
import org.fnppl.opensdx.gui.MessageHandler;
import org.fnppl.opensdx.securesocket.OSDXSocket;
import org.fnppl.opensdx.securesocket.OSDXSocketDataHandler;
import org.fnppl.opensdx.securesocket.OSDXSocketReceiver;
import org.fnppl.opensdx.securesocket.OSDXSocketSender;
import org.fnppl.opensdx.security.KeyApprovingStore;
import org.fnppl.opensdx.security.MasterKey;
import org.fnppl.opensdx.security.OSDXKey;
import org.fnppl.opensdx.security.Signature;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;

public class OSDXFileTransferClient implements FileTransferClient {

	private OSDXSocket socket = null;
	private OSDXSocketDataHandler dataHandler = null;
	private String username = null;
	private OSDXKey key = null;
	
	private Vector<DownloadFile> nextDownloadFile = new Vector<DownloadFile>();
	private Vector<String> textQueue = new Vector<String>();
	private Vector<Element> xmlQueue = new Vector<Element>();
	private RightsAndDuties rights_duties = null;
	private int maxByteLength = 4*1024*1024;
	
	public OSDXFileTransferClient() {
		//super(host, port, prepath);
		rights_duties = new RightsAndDuties();
		dataHandler = new OSDXSocketDataHandler() {
			public void handleNewText(String text, OSDXSocketSender sender) {
				//System.out.println("RECEIVED TEXT: "+text);
				if (text.startsWith("<?xml")) {
					try {
						Element e = Document.fromString(text).getRootElement();
						if (e.getName().equals("rights_and_duties")) {
							rights_duties = RightsAndDuties.fromElement(e);
						} else {
							xmlQueue.add(e);
							while (xmlQueue.size()>100) {
								xmlQueue.remove(0);
							}
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				} else {
					textQueue.add(text);
					while (textQueue.size()>1000) {
						textQueue.remove(0);
					}
				}
			}

			public void handleNewData(byte[] data, OSDXSocketSender sender) {
				//if data arrives, it must be a file
				if (nextDownloadFile!=null && nextDownloadFile.size()>0) {
					//only process if requested
					try {
						DownloadFile f = nextDownloadFile.get(0);
						System.out.println("Saving to file: "+f.file.getAbsolutePath());
						f.file.getParentFile().mkdirs();
						FileOutputStream fout = new FileOutputStream(f.file,true);
						fout.write(data);
						fout.close();
						//remove from download list if complete length has arrived
						if (f.file.length()==f.length) {
							nextDownloadFile.remove(0);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
	}
	
	
	
	
	public boolean connect(String host, int port, String prepath, OSDXKey mySigningKey, String username) throws Exception {
		socket = new OSDXSocket();
		this.key = mySigningKey;
		this.username = username;
		socket.connect(host, port, prepath, mySigningKey);
		socket.setDataHandler(dataHandler);
		if (socket.isConnected()) {
			return login(username);
		} else {
			System.out.println("ERROR: Connection to server could NOT be established!");
			return false;
		}
	}
	
	public void closeConnection() throws Exception {
		if (socket != null) {
			socket.closeConnection();
		}
		socket = null;
		username = null;
		key = null;
		nextDownloadFile = new Vector<DownloadFile>();
		textQueue = new Vector<String>();
		xmlQueue = new Vector<Element>();
		rights_duties = new RightsAndDuties();
	}
	
	private void sendEncryptedText(String text) {
		if (socket!=null) {
			socket.sendEncryptedText(text);
		}
	}
	
	private void sendEncryptedData(byte[] data) {
		if (socket!=null) {
			socket.sendEncryptedData(data);
		}
	}
	
	public void cd(String dir) {
		if (!rights_duties.allowsCD())  return;
		sendEncryptedText("CD "+dir);
	}
	
	public void cd_up() {
		if (!rights_duties.allowsCD()) return;
		sendEncryptedText("CDUP");
	}
	
	public void mkdir(String dir) throws Exception {
		if (!rights_duties.allowsMkdir()) {
			throw new Exception("MKDIR NOT ALLOWED");
//			return;
		}
		sendEncryptedText("MKDIR "+dir);
	}
	
	public void delete(String file) {
		if (!rights_duties.allowsDelete()) return;
		sendEncryptedText("DELETE "+file);
	}
	
	public boolean login(String username) {
		sendEncryptedText("LOGIN "+username);
		long timeout = System.currentTimeMillis()+2000;
		while (System.currentTimeMillis()<timeout) {
			for (int i=0;i<textQueue.size();i++) {
				if (textQueue.get(i).startsWith("ACK LOGIN :: ")) {
					String txt = textQueue.remove(i).substring(13);
					//System.out.println("LOGIN: "+txt);
					String[] param = Util.getParams(txt);
					try {
						rights_duties = RightsAndDuties.fromElement(Document.fromString(param[1]).getRootElement());
						return true;
					} catch (Exception ex) {
						ex.printStackTrace();
						return false;
					}
				}
				if (textQueue.get(i).startsWith("ERROR IN LOGIN :: ")) {
					String txt = textQueue.remove(i);
					System.out.println("LOGIN: "+txt);
					return false;
				}
			}
		}
		return false;
	}
	
	public String pwd() {
		if (!rights_duties.allowsPWD()) return null;
		//remove all previous ACK PWD from queue
		for (int i=0;i<textQueue.size();i++) {
			if (textQueue.get(i).startsWith("ACK PWD :: ")) {
				textQueue.remove(i);
				i--;
			}
		}
		sendEncryptedText("PWD");
		String pwd = null;
		long timeout = System.currentTimeMillis()+2000;
		while (pwd==null && System.currentTimeMillis()<timeout) {
			for (int i=0;i<textQueue.size();i++) {
				if (textQueue.get(i).startsWith("ACK PWD :: ")) {
					pwd = textQueue.remove(i).substring(11);
					System.out.println("PWD: "+pwd);
					return pwd;
				}
			}
		}
		return null;
	}
	
	public void noop() {
		sendEncryptedText("NOOP");
	}
	
	
	public void uploadFile(File f) {
		if (!rights_duties.allowsUpload()) return;
		uploadFile(f, null, false);
	}
	
	public void resumeuploadFile(File f) {
		if (!rights_duties.allowsUpload()) return;
		resumeuploadFile(f, null);
	}
	
	public Vector<RemoteFile> list() {
		if (!rights_duties.allowsList()) return null;
		sendEncryptedText("LIST");
		String list = null;
		long timeout = System.currentTimeMillis()+2000;
		while (list==null && System.currentTimeMillis()<timeout) {
			for (int i=0;i<textQueue.size();i++) {
				if (textQueue.get(i).startsWith("ACK LIST :: ")) {
					list = textQueue.remove(i).substring(12);
					System.out.println("list :: "+list);					
					//parse list
					Vector<RemoteFile> fl = new Vector<RemoteFile>();
					if (!list.contains(",,")) { //empty dir
						return fl;
					}
					String[] parts = list.split(";;");
					for (String p : parts) {
						try {
							String[] att = p.split(",,");
							RemoteFile f = new RemoteFile(RemoteFileSystem.resolveEscapeChars(att[0]), RemoteFileSystem.resolveEscapeChars(att[1]), Long.parseLong(att[2]), Long.parseLong(att[3]), Boolean.parseBoolean(att[4]));
							System.out.println(f.toString());
							fl.add(f);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					return fl;
				}
			}
		}
		return null;
	}
	public void uploadFile(File f, String new_filename) {
		uploadFile(f, new_filename, false);
	}
	
	public void uploadFile(File f, String new_filename, boolean sign) {
		if (!rights_duties.allowsUpload()) return;
		if (f.exists()) {
			if (!f.isDirectory()) {
				long filelenght = f.length();
				String param = null;
				if (new_filename==null) {
					new_filename = f.getName();
				}
				if (sign || rights_duties.needsSignature(new_filename)) {
					try {
						Signature sig = Signature.createSignature(f, key);
						String sigText = Document.buildDocument(sig.toElement()).toStringCompact();
						param = Util.makeParamsString(new String[]{new_filename, ""+filelenght, sigText});
					} catch (Exception ex) {
						ex.printStackTrace();
						param = Util.makeParamsString(new String[]{new_filename,""+filelenght});
					}
				} else {
					param = Util.makeParamsString(new String[]{new_filename,""+filelenght});
				}
				
				sendEncryptedText("PUT "+param);
				//wait for ACK or ERROR of PUT
				String msg = null;
				long timeout = System.currentTimeMillis()+2000;
				while (msg==null && System.currentTimeMillis()<timeout) {
					for (int i=0;i<textQueue.size();i++) {
						if (textQueue.get(i).startsWith("ACK PUT :: ") || textQueue.get(i).startsWith("ERROR IN PUT :: ")) {
							msg = textQueue.remove(i);
						}
					}
				}
				if (msg!=null && msg.startsWith("ACK")) {
					if (filelenght<=maxByteLength) {
						//send in one data package
						try {
							ByteArrayOutputStream bOut = new ByteArrayOutputStream();
							FileInputStream fin = new FileInputStream(f);
							byte[] buffer = new byte[1024];
							int read;
							while ((read = fin.read(buffer))>0) {
								bOut.write(buffer, 0, read);
							}
							sendEncryptedData(bOut.toByteArray());
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					} else {
						//send in multiple data packages
						long nextStart = 0;
						try {
							FileInputStream fin = new FileInputStream(f);
							byte[] buffer = new byte[maxByteLength];
							int read;
							while ((read = fin.read(buffer))>0) {
								param = Util.makeParamsString(new String[]{new_filename, ""+nextStart, ""+read});
								sendEncryptedText("PUTPART "+param);
								ByteArrayOutputStream bOut = new ByteArrayOutputStream();
								bOut.write(buffer, 0, read);
								sendEncryptedData(bOut.toByteArray());
								nextStart += read;
								//if (Math.random()>0.5) return; //BB random errors to test resumeupload
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				} else {
					if (msg==null) {
						System.out.println("ERROR uploading file: "+f.getName()+" :: TIMEOUT");
					} else {
						System.out.println("ERROR uploading file: "+f.getName()+" :: "+msg.substring(msg.indexOf(" :: ")+4));
					}
				}
			}
		}
	}
	
	public void resumeuploadFile(File f, String new_filename) {
		if (!rights_duties.allowsUpload()) return;
		if (f.exists()) {
			if (!f.isDirectory()) {
				long filelenght = f.length();
				String param = null;
				if (new_filename==null) {
					new_filename = f.getName();
				}
				param = Util.makeParamsString(new String[]{new_filename,""+filelenght});
				sendEncryptedText("RESUMEPUT "+param);
				
				//wait for ACK or ERROR of PUT
				String msg = null;
				long timeout = System.currentTimeMillis()+2000;
				while (msg==null && System.currentTimeMillis()<timeout) {
					for (int i=0;i<textQueue.size();i++) {
						if (textQueue.get(i).startsWith("ACK RESUMEPUT :: ") || textQueue.get(i).startsWith("ERROR IN RESUMEPUT :: ")) {
							msg = textQueue.remove(i);
						}
					}
				}
				if (msg!=null && msg.startsWith("ACK")) {
					//send in multiple data packages
					long nextStart = -1;
					try {
						//parse next start
						nextStart = Long.parseLong(msg.substring(msg.lastIndexOf('=')+1));
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					if (nextStart<0) {
						System.out.println("ERROR resume uploading file: "+f.getName()+" :: WRONG ACK FORMAT IN SERVERS RESPONSE");
						return;
					}
					try {
						FileInputStream fin = new FileInputStream(f);
						if (nextStart>0) fin.skip(nextStart);
						
						byte[] buffer = new byte[maxByteLength];
						int read;
						while ((read = fin.read(buffer))>0) {
							param = Util.makeParamsString(new String[]{new_filename, ""+nextStart, ""+read});
							sendEncryptedText("PUTPART "+param);
							ByteArrayOutputStream bOut = new ByteArrayOutputStream();
							bOut.write(buffer, 0, read);
							sendEncryptedData(bOut.toByteArray());
							nextStart += read;
						}
						
						fin.close();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				else if (msg!=null && msg.equals("ERROR IN RESUMEPUT :: FILE DOES NOT EXIST, PLEASE USE PUT INSTEAD")) {
					uploadFile(f, new_filename);
				}
				else {
					if (msg==null) {
						System.out.println("ERROR uploading file: "+f.getName()+" :: TIMEOUT");
					} else {
						System.out.println("ERROR uploading file: "+f.getName()+" :: "+msg.substring(msg.indexOf(" :: ")+4));
					}
				}
			}
		}
	}
	
	public void uploadFile(String filename, byte[] data) {
		if (!rights_duties.allowsUpload()) return;
		sendEncryptedText("PUT "+filename);
		//wait for ACK or ERROR of PUT
		String msg = null;
		long timeout = System.currentTimeMillis()+2000;
		while (msg==null && System.currentTimeMillis()<timeout) {
			for (int i=0;i<textQueue.size();i++) {
				if (textQueue.get(i).startsWith("ACK PUT :: ") || textQueue.get(i).startsWith("ERROR IN PUT :: ")) {
					msg = textQueue.remove(i);
				}
			}
		}
		if (msg.startsWith("ACK")) {
			sendEncryptedData(data);
		} else {
			System.out.println("ERROR uploading file: "+filename+" :: "+msg.substring(msg.indexOf(" :: ")+4));
		}
		
	}
	
	public void downloadFile(String filename, File localFile) {
		if (!rights_duties.allowsDownload()) return;
		if (localFile.isDirectory()) {
			localFile = new File(localFile,filename);
		}
		if (localFile.exists()) {
			System.out.println("Error downloading file: "+filename+" :: local file: "+localFile.getAbsolutePath()+" exists.");
			return;
		}
		sendEncryptedText("GET "+filename);
		//wait for ACK or ERROR of GET
		String msg = null;
		long timeout = System.currentTimeMillis()+2000;
		while (msg==null && System.currentTimeMillis()<timeout) {
			for (int i=0;i<textQueue.size();i++) {
				if (textQueue.get(i).startsWith("ACK GET :: ") || textQueue.get(i).startsWith("ERROR IN GET :: ")) {
					msg = textQueue.remove(i);
				}
			}
		}
		if (msg!=null && msg.startsWith("ACK")) {
			long length = 0;
			try {
				length = Long.parseLong(msg.substring(msg.lastIndexOf('=')+1));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			nextDownloadFile.add(new DownloadFile(localFile, length));
		} else if (msg!=null) {
			System.out.println("Error downloading file: "+filename+" :: "+msg);
		} else {
			System.out.println("Error downloading file: "+filename+" :: TIMEOUT");
		}
	}
	
	
	public static void test() {
		OSDXFileTransferClient s = new OSDXFileTransferClient();
		try {
			File downloadPath = new File("../../openSDX/files");
			OSDXKey mysigning = OSDXKey.fromElement(Document.fromString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>  <keypair><identities><identity><identnum>0001</identnum><email>test</email><mnemonic restricted=\"true\">test</mnemonic><sha256>2D:83:44:CA:3A:4C:85:3A:FB:E9:A3:15:D1:B4:70:BA:CC:7C:16:C7:DC:80:D9:AF:4F:E5:3D:74:4A:58:47:CE</sha256></identity></identities><sha1fingerprint>27:61:62:78:C1:29:F3:C6:A9:03:44:D2:18:36:37:22:E2:9F:63:BF</sha1fingerprint><authoritativekeyserver>localhost</authoritativekeyserver><datapath><step1><datasource>LOCAL</datasource><datainsertdatetime>2011-06-08 04:42:04 GMT+00:00</datainsertdatetime></step1></datapath><valid_from>2011-06-08 04:42:04 GMT+00:00</valid_from><valid_until>2036-06-07 10:42:04 GMT+00:00</valid_until><usage>ONLYSIGN</usage><level>MASTER</level><parentkeyid /><algo>RSA</algo><bits>3072</bits><modulus>00:88:FA:94:53:C5:EC:A9:31:63:FD:20:E3:38:A6:C8:B7:7F:32:41:4B:84:07:A1:AF:81:48:21:6F:D8:87:58:40:BF:DC:1A:E5:BD:A9:8E:ED:90:84:ED:84:BB:2E:04:FB:4F:33:F9:46:8B:0D:D0:58:F5:85:5C:F7:43:93:60:3A:BF:10:4B:92:65:DF:86:13:11:C0:6A:46:6F:4B:68:C3:5B:C3:48:BF:8E:16:00:36:68:A5:0E:C6:03:3B:87:7C:49:0C:18:FE:34:DA:78:03:F8:4B:B3:22:B9:D5:63:D8:74:B8:88:4C:E9:4D:A4:CE:A5:7C:09:B2:19:55:77:55:81:4C:FF:76:CD:87:69:B9:D4:B8:53:BE:9C:07:52:ED:53:09:D7:66:82:FC:A5:0A:79:2B:0D:06:5A:A7:76:77:F9:C4:27:B3:FD:BB:4A:80:44:8F:83:0F:DA:7E:A1:E7:22:24:D0:CC:EA:B2:F0:7F:03:BF:FC:FA:BB:B5:2D:17:63:40:1F:78:41:34:E4:ED:5A:F7:A2:1A:C5:75:FC:0F:93:44:95:AD:09:0D:10:90:D1:77:6B:D4:02:3C:8B:43:0B:91:3C:F5:F9:4A:94:0C:D4:EC:DD:2B:56:D4:AB:B9:C1:A4:74:AC:85:2E:6C:7C:AE:21:17:11:41:CB:9D:1C:16:98:1A:4F:03:8A:34:80:C5:2E:F6:E8:29:DB:3F:1C:EA:B1:B7:21:A9:5F:FA:93:D3:47:FA:DD:28:8F:4F:AA:53:1B:16:32:61:3B:B2:41:0E:37:DB:16:5B:14:AA:A9:D4:6C:C3:3E:0E:8D:90:B9:C4:83:C1:A6:6A:BF:E0:7F:56:AF:7D:7F:47:E5:4E:9C:8E:E9:E4:27:06:F9:0A:8A:22:7A:85:2D:FE:B2:AF:10:EC:5B:36:E7:96:60:E2:77:C5:9F:78:B9:51:A4:CE:7C:1D:D3:43:BF:4F:B1:2C:3F:DF:30:04:B6:7E:40:7E:F3:0E:F1:12:42:78:C6:4A:07</modulus><pubkey><exponent>01:00:01</exponent></pubkey><privkey><exponent><locked><mantraname>mantra: password</mantraname><algo>AES@256</algo><initvector>6F:67:A3:5C:C0:5A:67:F6:30:32:9A:0E:1E:3A:8B:1B</initvector><padding>CBC/PKCS#5</padding><bytes>12:9A:B4:1C:1F:8D:8B:88:39:CD:CD:C4:C9:4D:BE:65:56:2C:48:40:E8:3A:ED:09:F3:BF:0D:A7:A8:09:77:B7:C1:15:FB:8C:93:57:B6:38:F3:31:9B:A8:1E:21:27:40:9E:93:E9:4A:1F:B1:41:02:CF:40:96:2C:A6:17:2B:48:68:58:70:AD:B7:E1:52:6F:09:19:11:67:59:BC:1F:FD:BE:88:C0:B2:FF:76:34:EF:1B:26:DA:9F:4C:47:66:0E:87:BB:1C:09:CB:F2:77:BC:CB:AE:89:CA:C4:65:98:DA:D1:6E:ED:22:08:70:FC:BB:E2:CC:41:7F:5C:12:7B:A6:D3:32:73:FA:BB:E2:95:A6:1C:34:3E:FD:A8:90:D0:9A:0B:4E:96:06:89:DD:6F:35:02:E5:FB:CA:0A:E7:0D:2E:A1:B3:81:17:DE:8D:7F:96:F4:36:AA:02:4E:EF:C0:EF:56:37:C0:53:FB:B9:E3:C0:5B:69:9E:7C:EC:1F:A3:0B:C5:99:B7:5D:54:52:28:17:4A:B1:3D:C8:36:54:2A:94:0C:32:F7:1B:6A:11:37:91:B5:43:5D:BF:DB:6F:D3:B4:37:18:32:81:81:C1:72:80:B6:95:0E:B0:61:FF:05:CE:FC:98:E5:F1:E4:D1:33:B7:EF:B8:EB:EF:6B:A7:FE:C6:37:77:CF:43:12:C3:5F:2B:2A:51:19:E8:C4:6D:F6:0E:15:C4:C3:AD:BE:4C:FE:D6:D5:3A:00:D8:E0:0B:00:78:A9:5F:D8:21:28:06:B8:74:F2:06:23:63:81:B8:CC:03:EC:2C:ED:6B:74:23:E4:31:C1:4E:9C:B2:24:F0:93:A4:7D:6C:6A:E2:C1:95:EC:EA:DF:DC:85:2B:60:15:24:DE:FD:DD:94:BF:CF:C9:8D:74:DE:8A:D8:89:DC:16:FA:9D:28:37:EE:65:44:AD:61:FB:33:D4:E8:66:D3:BA:D8:38:E1:16:F1:EF:97:FF:01:D7:C3:7D:76:CE:A2:12:1C:24:AC:EC:AE:AC:2B:08:99:7A:D8:A4:41:49:A4:0E:AF:83</bytes></locked></exponent></privkey><gpgkeyserverid /></keypair>  ").getRootElement());
			mysigning.unlockPrivateKey("password");			
			String username = "testuser";
			
			s.connect("localhost", 4221,"/", mysigning, username);
			
			//some test commands
//			s.mkdir("test-dir");
//			s.cd("test-dir");
			s.pwd();
//			s.uploadFile(new File("README"));
//			s.downloadFile("README", downloadPath);
			
			s.downloadFile("test.data", downloadPath);
			
			Thread.sleep(10000);
			s.closeConnection();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void upload_files(String host, int port, String prepath, OSDXKey mysigning, String username, Vector<File> files, String remoteDir, boolean resume) {
		if (prepath==null || prepath.length()==0) prepath = "/";
		OSDXFileTransferClient s = new OSDXFileTransferClient();
		try {
			s.connect(host, port, prepath,mysigning, username);
			if (remoteDir!=null) {
				s.mkdir(remoteDir);
				s.cd(remoteDir);
				s.pwd();
			}
			
			recursePut(files, s, 0, resume);
			
			Thread.sleep(1000);
			s.closeConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void recursePut(Vector<File> files, OSDXFileTransferClient s, int depth, boolean resume) throws Exception {
		for (int i=0; i<files.size(); i++) {
			System.out.println("Depth["+depth+"] uploading file "+(i+1)+" of "+files.size()+" :: "+files.get(i).getAbsolutePath());
			
			File l = files.elementAt(i);
			if(l.isDirectory()) {
				s.mkdir(l.getName());
				s.cd(l.getName());
				
				try {
					Vector<File> nfiles = new Vector<File>(Arrays.asList(l.listFiles()));
					recursePut(nfiles, s, depth+1, resume);
				} catch(Exception ex) {
					s.cd_up();
					throw ex;
				}
				
				s.cd_up();
			}
			else {
				if (resume) {
					s.resumeuploadFile(files.get(i));
				} else {
					s.uploadFile(files.get(i));
				}
			}
		}
	}
	
	public static void main(String[] args) {
		//test(); if (2 == 1+1) return;
		
		//System.out.println("args: "+Arrays.toString(args));
		OSDXKey key = null;
		String host = null; 	// --host
		int port = -1;			// --port
		String prepath = "/";	// --prepath
		String username = null; // --user
		String remotepath =null;// --remotepath
		String keystore = null; // --keystore
		String keyid = null;	// --keyid
		String keypw = null;	// --keypw
		String keypwfile = null;// --keypwfile
		String config = null;	// --config
		boolean resume = false;	// --resume
		
		
		Vector<File> files = new Vector<File>();
		int i=0;
		boolean start_files = false;
		try {
			while (i<args.length) {
				String s = args[i];
				if (start_files) {
					File f = new File(s);
					if (f.exists()){
						files.add(f);
					} else {
						error("Error: file "+s+" does not exists");
					}
					i++;
				} else {
					if (s.equals("--host")) {
						host = args[i+1];
						i+=2;
					}
					else if (s.equals("--port")) {
						port = Integer.parseInt(args[i+1]);
						i+=2;
					}
					else if (s.equals("--prepath")) {
						prepath = args[i+1];
						i+=2;
					}
					else if (s.equals("--user")) {
						username = args[i+1];
						i+=2;
					}
					else if (s.equals("--remotepath")) {
						remotepath = args[i+1];
						i+=2;
					}
					else if (s.equals("--keystore")) {
						keystore = args[i+1];
						i+=2;
					}
					else if (s.equals("--keyid")) {
						keyid = args[i+1];
						i+=2;
					}
					else if (s.equals("--keypw")) {
						keypw = args[i+1];
						i+=2;
					}
					else if (s.equals("--keypwfile")) {
						keypwfile = args[i+1];
						i+=2;
					}
					else if (s.equals("--config")) {
						config = args[i+1];
						i+=2;
					}
					else if (s.equals("--resume")) {
						resume = true;
						i++;
					}
					else if (s.startsWith("--")) {
						System.out.println("CANT UNDERSTAND ARGUMENT: "+s+" "+args[i+1]);
						i+=2;
					}
					else {
						start_files = true;
					}
				}
			}
			if (config!=null) {
				//read config
				Element ec = Document.fromFile(new File(config)).getRootElement();
				if (host==null && ec.getChild("host")!=null) host = ec.getChildText("host");
				if (port == -1 && ec.getChild("port")!=null) port = Integer.parseInt(ec.getChildText("port"));
				if (prepath==null && ec.getChild("prepath")!=null) prepath = ec.getChildText("prepath");
				if (remotepath==null && ec.getChild("remotepath")!=null) remotepath = ec.getChildText("remotepath");
				if (username==null) {
					if (ec.getChild("username")!=null) username = ec.getChildText("username");
					if (ec.getChild("user")!=null) username = ec.getChildText("user");
				}
				if (keystore==null && ec.getChild("keystore")!=null) keystore = ec.getChildText("keystore");
				if (keyid==null && ec.getChild("keyid")!=null) keyid = ec.getChildText("keyid");
				if (keypw==null && ec.getChild("keypw")!=null) keypw = ec.getChildText("keypw");
				if (keypwfile==null && ec.getChild("keypwfile")!=null) keypwfile = ec.getChildText("keypwfile");
				
				//private key
				if (ec.getChild("keypair")!=null) {
					key = OSDXKey.fromElement(ec.getChild("keypair"));
				}
			}
			//message handler
			MessageHandler mh = new MessageHandler() {
				public String requestPassword(String keyid, String mantra) {
					System.out.println("please enter password for keyid: "+keyid+", mantra: "+mantra);
					System.out.print("password: ");
					char[] pw = System.console().readPassword();
					String p = "";
					for (int i=0;i<pw.length;i++) {
						p += pw[i];
					}
					return p;
				}
				
				public boolean requestOverwriteFile(File file) {
					return false;
				}
				
				public String[] requestNewPasswordAndMantra(String message) {
					return null;
				}
				
				public MasterKey requestMasterSigningKey(KeyApprovingStore keystore)
						throws Exception {
					return null;
				}
				public boolean requestIgnoreVerificationFailure() {
					System.out.println("verification of keystore failed.");
					return false;
				}
				public boolean requestIgnoreKeyLogVerificationFailure() {
					return false;
				}
				public void fireWrongPasswordMessage() {
					System.out.println("Sorry, wrong password.");
					System.exit(1);
				}
				public File chooseOriginalFileForSignature(File dir, String selectFile) {
					return null;
				}
			};
		
			
			//check if we have everything we need
			if (host==null) error("missing paramenter: host");
			if (port==-1) error("missing parameter: port");
			if (username==null) error("missing paramenter: user");
			if (files.size()==0) error("missing parameter: file to upload");
			
			//init key
			if (key==null) {
				if (keystore==null) {
					error("missing paramenter: key in configfile or keystore");
				}
				if (keyid==null) {
					error("missing paramenter: key in configfile or keyid");
				}
				KeyApprovingStore ks = KeyApprovingStore.fromFile(new File(keystore), mh);
				key = ks.getKey(keyid);
				if (key==null) error("error: keyid: "+keyid+" not found in given keystore.");
			}
			
			//unlock key
			if (keypw!=null) {
				key.unlockPrivateKey(keypw);
			} else if (keypwfile!=null) {
				keypw = Util.loadText(keypwfile);
				key.unlockPrivateKey(keypw);
			} else {
				key.unlockPrivateKey(mh);
			}
			if (!key.isPrivateKeyUnlocked()) {
				error("can not unlock private key");
			}
			
			//yes, we can finally execute the uploads
			upload_files(host, port, prepath, key, username, files, remotepath, resume);
			
		} catch (Exception ex) {
			System.out.println("usage: OSDXFileTransferClient --host localhost --port 4221 --prepath \"/\" --user username --keystore defautlKeyStore.xml --keyid 11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE:FF:11:22:33:44:55 --keypw key-password [file or list of files to upload]");
			System.out.println("usage: OSDXFileTransferClient --config configfile.xml [file or list of files to upload]");
			ex.printStackTrace();
		}
	}
	
	private static void error(String msg) {
		System.out.println(msg);
		System.out.println("usage: OSDXFileTransferClient --host localhost --port 4221 --prepath \"/\" --user username --keystore defautlKeyStore.xml --keyid 11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE:FF:11:22:33:44:55 --keypw key-password --remotepath \"/bla/blub\" [file or list of files to upload]");
		System.out.println("or   : OSDXFileTransferClient --host localhost --port 4221 --prepath \"/\" --user username --keystore defautlKeyStore.xml --keyid 11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE:FF:11:22:33:44:55 --keypwfile key-password.txt [file or list of files to upload]");
		System.out.println("or   : OSDXFileTransferClient --config configfile.xml [file or list of files to upload]");
		
		System.exit(1);
	}
	
	private class DownloadFile {
		public File file;
		public long length;
		public DownloadFile(File file, long lenghth) {
			this.file = file;
			this.length = lenghth;
		}
	}
}
