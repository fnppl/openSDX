package org.fnppl.opensdx.file_transfer.commands;
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
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import org.fnppl.opensdx.file_transfer.CommandResponseListener;
import org.fnppl.opensdx.file_transfer.SecureConnection;

public abstract class OSDXFileTransferCommand {

	protected boolean DEBUG = false;
	
	
	protected long id = -1L;
	protected int num = 0;
	protected String command = null;
	
	protected int maxPacketSize = 50*1024; //50kB
	
	protected long progress = 0;
	protected long progressMax = 100;
	protected boolean block = false;
	protected boolean goOn = true;
	
	private Vector<CommandResponseListener> listener = new Vector<CommandResponseListener>();
	
	
	public void addListener(CommandResponseListener listener) {
		this.listener.add(listener);
	}
	
	public void startProcessing() throws Exception {
		onProcessStart();
	}
	public void endProcessing() throws Exception {
		onProcessEnd();
	}
	
	public void cancelProcessing() throws Exception {
		onProcessCancel();
	}
	public boolean goOn() {
		return goOn;
	}
	public abstract void onProcessStart() throws Exception;
	public abstract void onProcessEnd();
	public abstract void onProcessCancel();
	public abstract void onResponseReceived(int num, byte code, byte[] content) throws Exception;
	
	public abstract boolean hasNextPackage();
	
	public void sendNextPackage(SecureConnection con) throws Exception {
		onSendNextPackage(con);
		if (!hasNextPackage()) {
			endProcessing();
		}
	}
	
	public abstract void onSendNextPackage(SecureConnection con) throws Exception;
	
	public long getID() {
		return id;
	}
	
//	public void notifyListener(int status) {
//		switch (status) {
//		
//		case STATUS_START:
//			for (CommandResponseListener l : listener) {
//				l.onProcessingStarts(this);
//			}
//			break;
//		case STATUS_END:
//			for (CommandResponseListener l : listener) {
//				l.onProcessingEnds(this);
//			}
//			break;
//		case STATUS_UPDATE:
//			for (FileTransferCommandListener l : listener) {
//				l.onUpdateStatus(this);
//			}
//			break;
//		}
//	}
	
	protected void notifySucces() {
		for (CommandResponseListener l : listener) {
			l.onSuccess(this);
		}
	}
	protected void notifyError(String msg) {
		for (CommandResponseListener l : listener) {
			l.onError(this, msg);
		}
	}
	protected void notifyUpdate(long progress, long progressMax, String msg) {
		for (CommandResponseListener l : listener) {
			l.onStatusUpdate(this, progress, progressMax, msg);
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
