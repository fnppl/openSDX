package org.fnppl.opensdx.file_transfer.model;

import java.io.File;
import java.util.Vector;


public class LocalFileSystem extends RemoteFileSystem {

	public void connect() {};
	public void disconnect() {};
	public void noop() {};
	public boolean isConnected() {
		return true;
	}
	
	public LocalFileSystem() {
		
	}
	
	public void initRoot() {
		File[] f = File.listRoots();
		root = new RemoteFile("", f[0].getAbsolutePath(), 0, 0, true);
	}
	
	public Vector<RemoteFile> list(RemoteFile dir) {
		Vector<RemoteFile> list = new Vector<RemoteFile>();
		File f = new File(dir.getPath(),dir.getName());
		if (dir.getPath().length()==0) {
			f = new File(dir.getName());
		}
		File[] l =  f.listFiles();
		if (l!=null) {
			for (File a : l) {
				if (!a.isHidden()) {
					RemoteFile r = new RemoteFile(a.getParent(), a.getName(), a.length(), a.lastModified(), a.isDirectory());
					list.add(r);
				}
			}
		} else {
			System.out.println("error listing: path="+dir.getPath()+", name="+dir.getName()+", file="+f.getAbsolutePath());
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
	
}
