package org.fnppl.opensdx.file_transfer.model;

import java.io.File;
import java.util.Vector;


import org.fnppl.opensdx.file_transfer.OSDXFileTransferClient;
import org.fnppl.opensdx.security.OSDXKey;

public class OSDXFileSystem extends RemoteFileSystem {
	
	private OSDXFileTransferClient client;
	private boolean connected = false;
	
	private String host;
	private int port;
	private String prepath;
	private String username;
	private OSDXKey key;
	
	public OSDXFileSystem(String host, int port, String prepath, String username, OSDXKey key) {
		super();
		this.host = host;
		this.port = port;
		this.prepath = prepath;
		this.username = username;
		this.key = key;
	}
	
	public void initRoot() {
		root = new RemoteFile("", "/", 0, 0, true);
	}
	
	public void connect() throws Exception{
		client = new OSDXFileTransferClient();
		connected = client.connect(host, port, prepath,key, username);
		System.out.println("connected: "+connected);
	}
	
	public void disconnect() {
		if (client!=null) {
			try {
				client.closeConnectionDirectly();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public boolean isConnected() {
		if (client==null) return false;
		return connected;
	}
	public void noop() {
		client.noop();
	}
	
	public Vector<RemoteFile> list(RemoteFile file) {
		try {
			String dir = file.getFilnameWithPath();
			Vector<RemoteFile> list = client.list(file);
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
//		Dialogs.showMessage("not implemented");
		try {
			String oldname = from.getFilnameWithPath();
			String newname = to.getFilnameWithPath();
			client.rename(oldname, newname);
//			System.out.println("rename: "+oldname+" -> "+newname);
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
}
