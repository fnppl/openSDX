package org.fnppl.opensdx.ftp;

import java.io.File;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import org.fnppl.opensdx.gui.Dialogs;
import org.fnppl.opensdx.securesocket.OSDXFileTransferClient;
import org.fnppl.opensdx.security.OSDXKey;

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
	
	
	private RemoteFileSystem() {
		
	}
	public RemoteFile getRoot()  {
		return root;
	}
	
	public abstract void connect() throws Exception;
	public abstract void disconnect();
	public abstract boolean isConnected();
	public abstract void mkdir(RemoteFile dir);
	public abstract void remove(RemoteFile file);
	public abstract void rename(RemoteFile from, RemoteFile to);
	public abstract Vector<RemoteFile> list(RemoteFile dir);
	public abstract void upload(File local, RemoteFile remote);
	public abstract void download(File local, RemoteFile remote);
	
	public static RemoteFileSystem initLocalFileSystem() {
		RemoteFileSystem local = new RemoteFileSystem() {
			public void connect() {};
			public void disconnect() {};
			public boolean isConnected() {
				return true;
			}
			public Vector<RemoteFile> list(RemoteFile dir) {
				Vector<RemoteFile> list = new Vector<RemoteFile>();
				File f = new File(dir.getPath(),dir.getName());
				File[] l =  f.listFiles();
				for (File a : l) {
					RemoteFile r = new RemoteFile(a.getParent(), a.getName(), a.length(), a.lastModified(), a.isDirectory());
					list.add(r);
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
			public void upload(File local, RemoteFile remote) {
				//NOTHING TO DO
			}
			public void download(File local, RemoteFile remote) {
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
			public void upload(File local, RemoteFile remote) {
				try {
					System.out.println("upload file: "+local.getAbsolutePath()+" -> "+remote.getFilnameWithPath());
					ftp.uploadFile(local, remote.getFilnameWithPath());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			public void download(File local, RemoteFile remote) {
				try {
					System.out.println("download file: "+remote.getFilnameWithPath()+" -> "+local.getAbsolutePath());
					ftp.downloadFile(remote.getFilnameWithPath(), local);
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
				client = new OSDXFileTransferClient(host, port, prepath);
				connected = client.connect(key, username);
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
				Dialogs.showMessage("not implemented");
				try {
					String f = file.getFilnameWithPath();
//					System.out.println("remove "+f);
//					if (file.isDirectory()) {
//						client.deleteDirectory(f);
//					} else {
//						client.deleteFile(f);
//					}
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
			public void upload(File local, RemoteFile remote) {
				try {
					System.out.println("upload file: "+local.getAbsolutePath()+" -> "+remote.getFilnameWithPath());
					client.uploadFile(local, remote.getFilnameWithPath());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			public void download(File local, RemoteFile remote) {
				try {
					System.out.println("download file: "+remote.getFilnameWithPath()+" -> "+local.getAbsolutePath());
					client.downloadFile(remote.getFilnameWithPath(), local);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			
		};
		fs.root = new RemoteFile("", "/", 0, 0, true);
		return fs;
	}
	
}
