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
import it.sauronsoftware.ftp4j.FTPDataTransferListener;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Vector;


public class FTPClient implements FileTransferClient{

	private it.sauronsoftware.ftp4j.FTPClient client = null;

	private FTPClient() {

	}

	public static FTPClient connect(String host, String username, String password)  throws Exception {
		return connect(host, username, password,21);
	}

	public static FTPClient connect(String host, String username, String password, int port)  throws Exception {
		FTPClient ftpc = new FTPClient();
		ftpc.client = new it.sauronsoftware.ftp4j.FTPClient();
		ftpc.client.connect(host);
		ftpc.client.login(username, password);

		//BB_2011-07-18: set the file transfer encoding parameter:
		//default transfer is "auto" that re-encodes text files to target system encoding
		// -> i think this could cause trouble with file checksums, so use "binary" mode always
		ftpc.client.setType(it.sauronsoftware.ftp4j.FTPClient.TYPE_BINARY);

		//use compression if supported
		if (ftpc.client.isCompressionSupported()) {
			ftpc.client.setCompressionEnabled(true);
		}
		return ftpc;
	}

	public void closeConnection() throws FileTransferException {
		try {
			client.disconnect(true); //send quit (false will close without sendind a message
		} catch (Exception ex) {
			//ex.printStackTrace();
			throw new FileTransferException(ex.getMessage());
		}
	}

	public String pwd() throws FileTransferException {
		try {
			return client.currentDirectory();
		} catch (Exception ex) {
			//ex.printStackTrace();
			throw new FileTransferException(ex.getMessage());
		}
	}

	public void mkdir(String dir) {
		try {
			client.createDirectory(dir);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void cd(String newPath) throws FileTransferException {
		try {
			client.changeDirectory(newPath);
		} catch (Exception ex) {
			//ex.printStackTrace();
			throw new FileTransferException(ex.getMessage());
		}
	}

	public void cd_up() throws FileTransferException {
		try {
			client.changeDirectoryUp();
		} catch (Exception ex) {
			//ex.printStackTrace();
			throw new FileTransferException(ex.getMessage());
		}
	}

	public void rename(String oldname, String newname) throws FileTransferException {
		try {
			client.rename(oldname, newname);
		} catch (Exception ex) {
			//ex.printStackTrace();
			throw new FileTransferException(ex.getMessage());
		}
	}

	public void move(String oldLocation, String newLocation) throws FileTransferException {
		try {
			client.rename(oldLocation, newLocation);
		} catch (Exception ex) {
			//ex.printStackTrace();
			throw new FileTransferException(ex.getMessage());
		}
	}

	public void deleteFile(String filename) throws FileTransferException {
		try {
			client.deleteFile(filename);
		} catch (Exception ex) {
			//ex.printStackTrace();
			throw new FileTransferException(ex.getMessage());
		}
	}

	public void deleteDirectory(String dirname) throws FileTransferException {
		try {
			client.deleteDirectory(dirname);
		} catch (Exception ex) {
			//ex.printStackTrace();
			throw new FileTransferException(ex.getMessage());
		}
	}

	public void uploadFile(File f, final FileTransferProgress progress) throws FileTransferException {
		try {
			FTPDataTransferListener transferListener = new FTPDataTransferListener() {
				public void transferred(int len) {
					System.out.println("transfered: "+len);
				}
				public void started() {
					System.out.println("transfer started");
				}
				public void failed() {
					System.out.println("transfer failed");
					//throw new FileTransferException("upload failed.");
				}
				public void completed() {
					System.out.println("transfer completed");
					if (progress!=null) {
						progress.setComplete();
					}
				}
				public void aborted() {
					System.out.println("transfer aborted");
					//throw new FileTransferException("upload aborted.");
				}
			};
			client.upload(f,transferListener);
		} catch (Exception ex) {
			//ex.printStackTrace();
			throw new FileTransferException(ex.getMessage());
		}
	}

	public void uploadFile(File f, String new_filename, final FileTransferProgress progress) throws FileTransferException {
		try {
			System.out.println("uploading file to: "+new_filename+"  length = "+f.length());

			FileInputStream fileIn = new FileInputStream(f);
			FTPDataTransferListener transferListener = new FTPDataTransferListener() {
				public void transferred(int len) {
					System.out.println("transfered: "+len);
				}
				public void started() {
					System.out.println("transfer started");
				}
				public void failed() {
					System.out.println("transfer failed");
				}
				public void completed() {
					System.out.println("transfer completed");
					if (progress!=null) {
						progress.setComplete();
					}
				}
				public void aborted() {
					System.out.println("transfer aborted");
				}
			};
			client.upload(new_filename, fileIn, 0, 0, transferListener);
		} catch (Exception ex) {
			//ex.printStackTrace();
			throw new FileTransferException(ex.getMessage());
		}
	}

	public void uploadFile(String new_filename, byte[] data, final FileTransferProgress progress) throws FileTransferException {
		if (data.length>0) {
			try {
				System.out.println("uploading file data to: "+new_filename+"  length = "+data.length);
				InputStream in = new ByteArrayInputStream(data);

				FTPDataTransferListener transferListener = new FTPDataTransferListener() {
					public void transferred(int len) {
						System.out.println("transfered: "+len);
					}
					public void started() {
						System.out.println("transfer started");
					}
					public void failed() {
						System.out.println("transfer failed");
					}
					public void completed() {
						System.out.println("transfer completed");
						if (progress!=null) {
							progress.setComplete();
						}
					}
					public void aborted() {
						System.out.println("transfer aborted");
					}
				};
				client.upload(new_filename, in, 0, 0, transferListener);
			} catch (Exception ex) {
				//ex.printStackTrace();
				throw new FileTransferException(ex.getMessage());
			}
		}
	}

	public void downloadFile(String filename, File localFile, final FileTransferProgress progress) throws FileTransferException {
		try {	
			FTPDataTransferListener transferListener = new FTPDataTransferListener() {
				public void transferred(int len) {
					System.out.println("transfered: "+len);
				}
				public void started() {
					System.out.println("transfer started");
				}
				public void failed() {
					System.out.println("transfer failed");
				}
				public void completed() {
					System.out.println("transfer completed");
					if (progress!=null) {
						progress.setComplete();
					}
				}
				public void aborted() {
					System.out.println("transfer aborted");
				}
			};
			client.download(filename, localFile, transferListener);
		} catch (Exception ex) {
			//ex.printStackTrace();
			throw new FileTransferException(ex.getMessage());
		}
	}

	public Vector<RemoteFile> list() throws Exception {
		return list(null);
	}
	public Vector<RemoteFile> list(String filter) throws FileTransferException {
		try {
			Vector<RemoteFile> files = new Vector<RemoteFile>();
			it.sauronsoftware.ftp4j.FTPFile[] list = null;
			if (filter == null) {
				list = client.list();
			} else {
				list = client.list(filter); 
			}
			String path = pwd();
			for (it.sauronsoftware.ftp4j.FTPFile f : list) {
				String name = f.getName();
				long length = f.getSize();
				long lastModified = f.getModifiedDate().getTime();
				boolean dir = (f.getType()==it.sauronsoftware.ftp4j.FTPFile.TYPE_DIRECTORY);
				RemoteFile ftpf = new RemoteFile(path,name, length, lastModified, dir);
				files.add(ftpf);
			}
			return files;
		} catch (Exception ex) {
			//ex.printStackTrace();
			throw new FileTransferException(ex.getMessage());
		}
	}

	/**
	 * sends a noop command to keep connection alive
	 * @throws Exception
	 */
	public void noop() throws FileTransferException{
		try {
			client.noop();
		} catch (Exception ex) {
			//ex.printStackTrace();
			throw new FileTransferException(ex.getMessage());
		}
	}

	/**
	 * sends a noop command at the given time intervall in millisends
	 * @param time_intervall_milliseconds
	 */
	public void noop(long time_intervall_milliseconds) {
		client.setAutoNoopTimeout(time_intervall_milliseconds);
	}
}
