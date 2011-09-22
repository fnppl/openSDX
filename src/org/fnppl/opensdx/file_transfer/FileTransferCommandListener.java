package org.fnppl.opensdx.file_transfer;

import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferCommand;


public interface FileTransferCommandListener {
	
	public void onProcessingStarts(OSDXFileTransferCommand command);
	public void onProcessingEnds(OSDXFileTransferCommand command);
	public void onUpdateStatus(OSDXFileTransferCommand command);
	
}
