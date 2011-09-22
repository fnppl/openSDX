package org.fnppl.opensdx.file_transfer.commands;

import java.io.UnsupportedEncodingException;
import java.util.Vector;

import org.fnppl.opensdx.file_transfer.FileTransferCommandListener;
import org.fnppl.opensdx.file_transfer.SecureConnection;

public abstract class OSDXFileTransferCommand {

	protected boolean DEBUG = true;
	
	private final int STATUS_START = 0;
	private final int STATUS_UPDATE = 1;
	private final int STATUS_END = 2;
	
	
	protected long id = -1L;
	protected int num = 0;
	protected String command = null;
	
	protected int maxPacketSize = 50*1024; //50kB
	
	protected long progress = 0;
	protected long progressMax = 100;
	protected boolean block = false;
	
	private Vector<FileTransferCommandListener> listener = new Vector<FileTransferCommandListener>();
	
	
	public void addListener(FileTransferCommandListener listener) {
		this.listener.add(listener);
	}
	
	public void startProcessing() throws Exception {
		onProcessStart();
		notifyListener(STATUS_START);
	}
	public void endProcessing() throws Exception {
		onProcessEnd();
		notifyListener(STATUS_END);
	}
	
	public void cancelProcessing() throws Exception {
		onProcessCancel();
		notifyListener(STATUS_END);
	}
	
	public abstract void onProcessStart() throws Exception;
	public abstract void onProcessEnd();
	public abstract void onProcessCancel();
	public abstract void onResponseReceived(int num, byte code, byte[] content) throws Exception;
	
	public abstract boolean hasNextPackage();
	
	public void sendNextPackage(SecureConnection con) throws Exception {
		onSendNextPackage(con);
		notifyListener(STATUS_UPDATE);
		if (!hasNextPackage()) {
			endProcessing();
		}
	}
	
	public abstract void onSendNextPackage(SecureConnection con) throws Exception;
	
	public long getID() {
		return id;
	}
	
	public void notifyListener(int status) {
		switch (status) {
		
		case STATUS_START:
			for (FileTransferCommandListener l : listener) {
				l.onProcessingStarts(this);
			}
			break;
		case STATUS_END:
			for (FileTransferCommandListener l : listener) {
				l.onProcessingEnds(this);
			}
			break;
		case STATUS_UPDATE:
			for (FileTransferCommandListener l : listener) {
				l.onUpdateStatus(this);
			}
			break;
		}
	}
	
	public OSDXFileTransferCommand setBlocking() {
		block = true;
		return this;
	}
	
	public boolean isBlocking() {
		return block;
	}
	
	public String getMessageFromContentNN(byte[] content) {
		String s = getMessageFromContent(content);
		if (s==null) {
			return "";
		}
		return s;
	}
	
	public String getMessageFromContent(byte[] content) {
		if (content==null) return null;
		try {
			return new String(content,"UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}
}
