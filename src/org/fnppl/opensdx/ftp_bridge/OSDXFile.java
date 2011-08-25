package org.fnppl.opensdx.ftp_bridge;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.Remote;
import java.util.List;
import java.util.Vector;

import org.apache.ftpserver.ftplet.FtpFile;
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
		System.out.println("new osdxfile: path:: |"+file.getPath()+"|  filename:: |"+file.getName()+"|");
	}
	
	public InputStream createInputStream(long offset) throws IOException {
		if (offset>0) {
			throw new IOException("offset not implmented");
		}
		throw new IOException("create input stream not implmented");
	}
	
	public OutputStream createOutputStream(long offset) throws IOException {
		if (offset>0) {
			throw new IOException("offset not implmented");
		}
		throw new IOException("create input stream not implmented");
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
		if (file.getPath().equals("/")) {
			return null;
		}
		if (file.getPath().equals(" ")) {
			return "";
		}
		return file.getPath();
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
		if (file.getName().equals("/")) {
			return "";
		}
		if (file.getName().equals(" ")) {
			return "";
		}
		return file.getName();
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
		return false;
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
