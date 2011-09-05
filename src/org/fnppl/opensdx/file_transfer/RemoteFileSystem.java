package org.fnppl.opensdx.file_transfer;

import java.io.File;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import org.fnppl.opensdx.gui.Dialogs;
import org.fnppl.opensdx.security.OSDXKey;
import org.fnppl.opensdx.xml.Document;

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
public abstract class  RemoteFileSystem {

	private RemoteFile root;
	
	
	public RemoteFileSystem() {
		
	}
	public RemoteFile getRoot()  {
		return root;
	}
	
	public abstract void connect() throws Exception;
	public abstract void disconnect();
	public abstract boolean isConnected();
	public abstract void mkdir(RemoteFile dir) throws FileTransferException;
	public abstract void remove(RemoteFile file) throws FileTransferException;
	public abstract void rename(RemoteFile from, RemoteFile to) throws FileTransferException;
	public abstract Vector<RemoteFile> list(RemoteFile dir) throws FileTransferException;
	public abstract void upload(File local, RemoteFile remote, FileTransferProgress progress) throws FileTransferException;
	public abstract void download(File local, RemoteFile remote, FileTransferProgress progress) throws FileTransferException;
	public abstract void noop();
	
	public static RemoteFileSystem initLocalFileSystem() {
		RemoteFileSystem local = new RemoteFileSystem() {
			public void connect() {};
			public void disconnect() {};
			public void noop() {};
			public boolean isConnected() {
				return true;
			}
			public Vector<RemoteFile> list(RemoteFile dir) {
				Vector<RemoteFile> list = new Vector<RemoteFile>();
				File f = new File(dir.getPath(),dir.getName());
				File[] l =  f.listFiles();
				for (File a : l) {
					if (!a.isHidden()) {
						RemoteFile r = new RemoteFile(a.getParent(), a.getName(), a.length(), a.lastModified(), a.isDirectory());
						list.add(r);
					}
				}
				return list;
			}
			public void mkdir(RemoteFile dir) {
				File f = new File(dir.getPath(),dir.getName());
				f.mkdirs();
			}
			public void remove(RemoteFile file) {
				File f = new File(file.getPath(),file.getName());
				if (f.exists()) {
					f.delete();
				}
			}
			public void rename(RemoteFile from, RemoteFile to) {
				File fFrom = new File(from.getPath(),from.getName());
				File fTo = new File(to.getPath(),to.getName());
				fFrom.renameTo(fTo);
			}
			public void upload(File local, RemoteFile remote, FileTransferProgress progress) {
				//NOTHING TO DO
			}
			public void download(File local, RemoteFile remote, FileTransferProgress progress) {
				//NOTHING TO DO
			}
		};
		File f = new File("/");
		local.root = new RemoteFile("", f.getAbsolutePath(), 0, 0, true);
		return local;
	}
	
	
	public static RemoteFileSystem initFTPFileSystem(final String host, final String username, final String password) {
		final RemoteFileSystem fs = new RemoteFileSystem() {
			private FTPClient ftp = null;
			
			public void connect() throws Exception{
				ftp = FTPClient.connect(host, username, password);
			};
			public void disconnect() {
				if (ftp!=null) {
					try {
						ftp.closeConnection();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			};
			public boolean isConnected() {
				if (ftp==null) return false;
				return true;
			}
			public void noop() {
				try {
					ftp.noop();
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
			public Vector<RemoteFile> list(RemoteFile file) {
				try {
					String dir = file.getFilnameWithPath();
					System.out.println("cd "+dir);
					ftp.cd(dir);
					Vector<RemoteFile> list = ftp.list();
					return list;
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				return null;
			}
			public void mkdir(RemoteFile dir) {
				try {
					String f = dir.getFilnameWithPath();
					System.out.println("mkdir "+dir);
					ftp.mkdir(f);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			public void remove(RemoteFile file) {
				try {
					String f = file.getFilnameWithPath();
					System.out.println("remove "+f);
					if (file.isDirectory()) {
						ftp.deleteDirectory(f);
					} else {
						ftp.deleteFile(f);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			public void rename(RemoteFile from, RemoteFile to) {
				try {
					String oldname = from.getFilnameWithPath();
					String newname = to.getFilnameWithPath();
					ftp.rename(oldname, newname);
					System.out.println("rename: "+oldname+" -> "+newname);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				
			}
			public void upload(File local, RemoteFile remote, FileTransferProgress progress) {
				try {
					System.out.println("upload file: "+local.getAbsolutePath()+" -> "+remote.getFilnameWithPath());
					ftp.uploadFile(local, remote.getFilnameWithPath(),progress);
					if (progress!=null) {
						progress.setComplete();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			public void download(File local, RemoteFile remote, FileTransferProgress progress) {
				try {
					System.out.println("download file: "+remote.getFilnameWithPath()+" -> "+local.getAbsolutePath());
					ftp.downloadFile(remote.getFilnameWithPath(), local,progress);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			
		};
		fs.root = new RemoteFile("", "/", 0, 0, true);
		return fs;
	}
	
	public static RemoteFileSystem initOSDXFileServerConnection(final String host, final int port, final String prepath, final String username, final OSDXKey key) {
		final RemoteFileSystem fs = new RemoteFileSystem() {
			private OSDXFileTransferClient client;
			private boolean connected = false;
			public void connect() throws Exception{
				
//				System.out.println("connectiing to: ");
//				System.out.println("host: "+host);
//				System.out.println("port: "+port);
//				System.out.println("prepath: "+prepath);
//				System.out.println("username: "+username);
//				System.out.println("key: "+key.getKeyID());
//				
//				client = new OSDXFileTransferClient("localhost", 4221,"/");
//				OSDXKey mysigning = OSDXKey.fromElement(Document.fromString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>  <keypair><identities><identity><identnum>0001</identnum><email>test</email><mnemonic restricted=\"true\">test</mnemonic><sha256>2D:83:44:CA:3A:4C:85:3A:FB:E9:A3:15:D1:B4:70:BA:CC:7C:16:C7:DC:80:D9:AF:4F:E5:3D:74:4A:58:47:CE</sha256></identity></identities><sha1fingerprint>27:61:62:78:C1:29:F3:C6:A9:03:44:D2:18:36:37:22:E2:9F:63:BF</sha1fingerprint><authoritativekeyserver>localhost</authoritativekeyserver><datapath><step1><datasource>LOCAL</datasource><datainsertdatetime>2011-06-08 04:42:04 GMT+00:00</datainsertdatetime></step1></datapath><valid_from>2011-06-08 04:42:04 GMT+00:00</valid_from><valid_until>2036-06-07 10:42:04 GMT+00:00</valid_until><usage>ONLYSIGN</usage><level>MASTER</level><parentkeyid /><algo>RSA</algo><bits>3072</bits><modulus>00:88:FA:94:53:C5:EC:A9:31:63:FD:20:E3:38:A6:C8:B7:7F:32:41:4B:84:07:A1:AF:81:48:21:6F:D8:87:58:40:BF:DC:1A:E5:BD:A9:8E:ED:90:84:ED:84:BB:2E:04:FB:4F:33:F9:46:8B:0D:D0:58:F5:85:5C:F7:43:93:60:3A:BF:10:4B:92:65:DF:86:13:11:C0:6A:46:6F:4B:68:C3:5B:C3:48:BF:8E:16:00:36:68:A5:0E:C6:03:3B:87:7C:49:0C:18:FE:34:DA:78:03:F8:4B:B3:22:B9:D5:63:D8:74:B8:88:4C:E9:4D:A4:CE:A5:7C:09:B2:19:55:77:55:81:4C:FF:76:CD:87:69:B9:D4:B8:53:BE:9C:07:52:ED:53:09:D7:66:82:FC:A5:0A:79:2B:0D:06:5A:A7:76:77:F9:C4:27:B3:FD:BB:4A:80:44:8F:83:0F:DA:7E:A1:E7:22:24:D0:CC:EA:B2:F0:7F:03:BF:FC:FA:BB:B5:2D:17:63:40:1F:78:41:34:E4:ED:5A:F7:A2:1A:C5:75:FC:0F:93:44:95:AD:09:0D:10:90:D1:77:6B:D4:02:3C:8B:43:0B:91:3C:F5:F9:4A:94:0C:D4:EC:DD:2B:56:D4:AB:B9:C1:A4:74:AC:85:2E:6C:7C:AE:21:17:11:41:CB:9D:1C:16:98:1A:4F:03:8A:34:80:C5:2E:F6:E8:29:DB:3F:1C:EA:B1:B7:21:A9:5F:FA:93:D3:47:FA:DD:28:8F:4F:AA:53:1B:16:32:61:3B:B2:41:0E:37:DB:16:5B:14:AA:A9:D4:6C:C3:3E:0E:8D:90:B9:C4:83:C1:A6:6A:BF:E0:7F:56:AF:7D:7F:47:E5:4E:9C:8E:E9:E4:27:06:F9:0A:8A:22:7A:85:2D:FE:B2:AF:10:EC:5B:36:E7:96:60:E2:77:C5:9F:78:B9:51:A4:CE:7C:1D:D3:43:BF:4F:B1:2C:3F:DF:30:04:B6:7E:40:7E:F3:0E:F1:12:42:78:C6:4A:07</modulus><pubkey><exponent>01:00:01</exponent></pubkey><privkey><exponent><locked><mantraname>mantra: password</mantraname><algo>AES@256</algo><initvector>6F:67:A3:5C:C0:5A:67:F6:30:32:9A:0E:1E:3A:8B:1B</initvector><padding>CBC/PKCS#5</padding><bytes>12:9A:B4:1C:1F:8D:8B:88:39:CD:CD:C4:C9:4D:BE:65:56:2C:48:40:E8:3A:ED:09:F3:BF:0D:A7:A8:09:77:B7:C1:15:FB:8C:93:57:B6:38:F3:31:9B:A8:1E:21:27:40:9E:93:E9:4A:1F:B1:41:02:CF:40:96:2C:A6:17:2B:48:68:58:70:AD:B7:E1:52:6F:09:19:11:67:59:BC:1F:FD:BE:88:C0:B2:FF:76:34:EF:1B:26:DA:9F:4C:47:66:0E:87:BB:1C:09:CB:F2:77:BC:CB:AE:89:CA:C4:65:98:DA:D1:6E:ED:22:08:70:FC:BB:E2:CC:41:7F:5C:12:7B:A6:D3:32:73:FA:BB:E2:95:A6:1C:34:3E:FD:A8:90:D0:9A:0B:4E:96:06:89:DD:6F:35:02:E5:FB:CA:0A:E7:0D:2E:A1:B3:81:17:DE:8D:7F:96:F4:36:AA:02:4E:EF:C0:EF:56:37:C0:53:FB:B9:E3:C0:5B:69:9E:7C:EC:1F:A3:0B:C5:99:B7:5D:54:52:28:17:4A:B1:3D:C8:36:54:2A:94:0C:32:F7:1B:6A:11:37:91:B5:43:5D:BF:DB:6F:D3:B4:37:18:32:81:81:C1:72:80:B6:95:0E:B0:61:FF:05:CE:FC:98:E5:F1:E4:D1:33:B7:EF:B8:EB:EF:6B:A7:FE:C6:37:77:CF:43:12:C3:5F:2B:2A:51:19:E8:C4:6D:F6:0E:15:C4:C3:AD:BE:4C:FE:D6:D5:3A:00:D8:E0:0B:00:78:A9:5F:D8:21:28:06:B8:74:F2:06:23:63:81:B8:CC:03:EC:2C:ED:6B:74:23:E4:31:C1:4E:9C:B2:24:F0:93:A4:7D:6C:6A:E2:C1:95:EC:EA:DF:DC:85:2B:60:15:24:DE:FD:DD:94:BF:CF:C9:8D:74:DE:8A:D8:89:DC:16:FA:9D:28:37:EE:65:44:AD:61:FB:33:D4:E8:66:D3:BA:D8:38:E1:16:F1:EF:97:FF:01:D7:C3:7D:76:CE:A2:12:1C:24:AC:EC:AE:AC:2B:08:99:7A:D8:A4:41:49:A4:0E:AF:83</bytes></locked></exponent></privkey><gpgkeyserverid /></keypair>  ").getRootElement());
//				String username = "testuser";
//				mysigning.unlockPrivateKey("password");			
//				
//				connected = client.connect(key, username);
//				//connected = client.connect(mysigning, username);

				
				client = new OSDXFileTransferClient();
				connected = client.connect(host, port, prepath,key, username);
				System.out.println("connected: "+connected);
			};
			public void disconnect() {
				if (client!=null) {
					try {
						client.closeConnection();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			};
			public boolean isConnected() {
				if (client==null) return false;
				return connected;
			}
			public void noop() {
				client.noop();
			};
			public Vector<RemoteFile> list(RemoteFile file) {
				try {
					String dir = file.getFilnameWithPath();
					System.out.println("cd "+dir);
					client.cd(dir);
					Vector<RemoteFile> list = client.list();
					return list;
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				return null;
			}
			public void mkdir(RemoteFile dir) {
				try {
					String f = dir.getFilnameWithPath();
					System.out.println("mkdir "+dir);
					client.mkdir(f);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			public void remove(RemoteFile file) {
				//Dialogs.showMessage("not implemented");
				try {
					String f = file.getFilnameWithPath();
					System.out.println("remove "+f);
					client.delete(f);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			public void rename(RemoteFile from, RemoteFile to) {
				Dialogs.showMessage("not implemented");
				try {
					String oldname = from.getFilnameWithPath();
					String newname = to.getFilnameWithPath();
//					client.rename(oldname, newname);
//					System.out.println("rename: "+oldname+" -> "+newname);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				
			}
			public void upload(File local, RemoteFile remote, FileTransferProgress progress) throws FileTransferException {
				System.out.println("upload file: "+local.getAbsolutePath()+" -> "+remote.getFilnameWithPath());
				client.uploadFile(local, remote.getFilnameWithPath(),progress);
			}
			
			public void download(File local, RemoteFile remote, FileTransferProgress progress) throws FileTransferException {
				System.out.println("download file: "+remote.getFilnameWithPath()+" -> "+local.getAbsolutePath());
				client.downloadFile(remote.getFilnameWithPath(), local,progress);
			}
			
		};
		fs.root = new RemoteFile("", "/", 0, 0, true);
		return fs;
	}
	
	public static String makeEscapeChars(String s) {
		String r = "";
		for (char c : s.toCharArray()) {
			if (c == ',' || c == '\\' || c == ';') {
				r += '\\';
			}
			r += c;
		}
		return r;
	}
	
	public static String resolveEscapeChars(String s) {
		String r = "";
		char[] chars = s.toCharArray();
		for (int pos = 0; pos < chars.length; pos++) {
			if (chars[pos] == '\\') {
				pos++;
				if (pos<chars.length) {
					r += chars[pos];	
				}
			} else {
				r += chars[pos];
			}
		}
		return r;
	}
}
