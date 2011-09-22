package org.fnppl.opensdx.file_transfer.model;

import java.io.File;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import org.fnppl.opensdx.file_transfer.FTPClient;
import org.fnppl.opensdx.file_transfer.FileTransferException;
import org.fnppl.opensdx.file_transfer.FileTransferProgress;
import org.fnppl.opensdx.file_transfer.OSDXFileTransferClient;
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

	protected RemoteFile root = null;
	protected RemoteFile pwd = null;
	
	public RemoteFileSystem() {
		initRoot();
	}
	public RemoteFile getRoot()  {
		return root;
	}
	
	public abstract void initRoot();
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
	
	
//	public static RemoteFileSystem initFTPFileSystem(final String host, final String username, final String password) {
//		final RemoteFileSystem fs = new RemoteFileSystem() {
//			private FTPClient ftp = null;
//			
//			public void connect() throws Exception{
//				ftp = FTPClient.connect(host, username, password);
//			};
//			public void disconnect() {
//				if (ftp!=null) {
//					try {
//						ftp.closeConnection();
//					} catch (Exception ex) {
//						ex.printStackTrace();
//					}
//				}
//			};
//			public boolean isConnected() {
//				if (ftp==null) return false;
//				return true;
//			}
//			public void noop() {
//				try {
//					ftp.noop();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			};
//			public Vector<RemoteFile> list(RemoteFile file) {
//				try {
//					String dir = file.getFilnameWithPath();
//					System.out.println("cd "+dir);
//					ftp.cd(dir);
//					Vector<RemoteFile> list = ftp.list();
//					return list;
//				} catch (Exception ex) {
//					ex.printStackTrace();
//				}
//				return null;
//			}
//			public void mkdir(RemoteFile dir) {
//				try {
//					String f = dir.getFilnameWithPath();
//					System.out.println("mkdir "+dir);
//					ftp.mkdir(f);
//				} catch (Exception ex) {
//					ex.printStackTrace();
//				}
//			}
//			public void remove(RemoteFile file) {
//				try {
//					String f = file.getFilnameWithPath();
//					System.out.println("remove "+f);
//					if (file.isDirectory()) {
//						ftp.deleteDirectory(f);
//					} else {
//						ftp.deleteFile(f);
//					}
//				} catch (Exception ex) {
//					ex.printStackTrace();
//				}
//			}
//			public void rename(RemoteFile from, RemoteFile to) {
//				try {
//					String oldname = from.getFilnameWithPath();
//					String newname = to.getFilnameWithPath();
//					ftp.rename(oldname, newname);
//					System.out.println("rename: "+oldname+" -> "+newname);
//				} catch (Exception ex) {
//					ex.printStackTrace();
//				}
//				
//			}
//			public void upload(File local, RemoteFile remote, FileTransferProgress progress) {
//				try {
//					System.out.println("upload file: "+local.getAbsolutePath()+" -> "+remote.getFilnameWithPath());
//					ftp.uploadFile(local, remote.getFilnameWithPath(),progress);
//					if (progress!=null) {
//						progress.setComplete();
//					}
//				} catch (Exception ex) {
//					ex.printStackTrace();
//				}
//			}
//			public void download(File local, RemoteFile remote, FileTransferProgress progress) {
//				try {
//					System.out.println("download file: "+remote.getFilnameWithPath()+" -> "+local.getAbsolutePath());
//					ftp.downloadFile(remote.getFilnameWithPath(), local,progress);
//				} catch (Exception ex) {
//					ex.printStackTrace();
//				}
//			}
//			
//		};
//		fs.root = new RemoteFile("", "/", 0, 0, true);
//		return fs;
//	}

}
