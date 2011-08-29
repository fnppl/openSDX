package org.fnppl.opensdx.ftp_bridge;

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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.Remote;
import java.util.List;
import java.util.Vector;

import org.apache.ftpserver.ftplet.FtpFile;
import org.fnppl.opensdx.file_transfer.FileTransferProgress;
import org.fnppl.opensdx.file_transfer.OSDXFileTransferClient;
import org.fnppl.opensdx.file_transfer.RemoteFile;
import org.fnppl.opensdx.file_transfer.RightsAndDuties;

public class OSDXFile implements FtpFile{

	private OSDXFileTransferClient client = null;
	private RightsAndDuties rights_duties = null;
	private RemoteFile file = null;
	
	public OSDXFile(RemoteFile file, OSDXFileTransferClient client) {
		this.file = file;
		this.client = client;
		this.rights_duties = client.getRightsAndDuties();
		//System.out.println("new osdxfile: path:: |"+file.getPath()+"|  filename:: |"+file.getName()+"|");
	}
	
	public InputStream createInputStream(long offset) throws IOException {
		System.out.println("create inputstream: "+getAbsolutePath());
		if (offset>0) {
			throw new IOException("offset not implemented");
		}
		throw new IOException("create input stream not implmented");
	}
	
	public OutputStream createOutputStream(long offset) throws IOException {
		System.out.println("create outputstream: "+getAbsolutePath());
		if (offset>0) {
			throw new IOException("offset not implemented");
		}
		ByteArrayOutputStream bout = new ByteArrayOutputStream() {
			boolean sended = false;
			public void close() throws IOException {
				if (!sended) {
					sended = true;
					byte[] data = this.toByteArray();
					System.out.println("output stream closed: data size = "+data.length);
					final long len = data.length;
					FileTransferProgress progress = new FileTransferProgress() {
						public void onUpdate() {
							System.out.println(" - upload progress: "+(this.getProgress()*100/len)+"%");
						}
					};
					client.uploadFile(getAbsolutePath(), data, progress);
					super.close();
				}
			}
		};
		return bout;
	}

	public boolean delete() {
		if (!rights_duties.allowsDelete()) {
			return false;
		}
		client.delete(file.getFilnameWithPath());
		return true;
	}

	public boolean doesExist() {
		return (file.isDirectory()||file.isFile());
	}

	public String getAbsolutePath() {
		String path = file.getFilnameWithPath();
		if (path.equals("/.")) {
			return "/";
		}
		return path;
	}
	
	public String getPathOnly() {
		String path = file.getPath();
		if (path.equals("/.")) {
			return "/";
		}
		return path;
	}

	public String getGroupName() {
		return "group";
	}

	public String getOwnerName() {
		return "unknown";
	}
	
	public long getLastModified() {
		return file.getLastModified();
	}
	
	public boolean setLastModified(long date) {
		return false;
	}

	public int getLinkCount() {
		return 0;
	}

	public String getName() {
		String name = file.getName();
		if (name.equals("/")) {
			return "";
		}
		if (name.equals(".")) {
			return "";
		}
		return name;
	}
	
	public long getSize() {
		return file.getLength();
	}

	public boolean isDirectory() {
		return file.isDirectory();
	}

	public boolean isFile() {
		return file.isFile();
	}
	
	public boolean isHidden() {
		return false;
	}

	public boolean isReadable() {
		return true;
	}

	public boolean isRemovable() {
		return rights_duties.allowsDelete();
	}

	public boolean isWritable() {
		return !doesExist();
	}

	public List<FtpFile> listFiles() {
		System.out.println("list files: ");
		Vector<RemoteFile> list = client.list();
		Vector<FtpFile> files = new Vector<FtpFile>();
		for (RemoteFile f : list) {
			files.add(new OSDXFile(f,client));
		}
		return files;
	}

	public boolean mkdir() {
		if (!rights_duties.allowsMkdir()) return false;
		try {
			client.mkdir(file.getFilnameWithPath());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean move(FtpFile arg0) {
		throw new RuntimeException("move file not implmented");
	}
	
}
