package org.fnppl.opensdx.file_transfer;

import java.io.File;

import org.fnppl.opensdx.security.Result;

public interface UploadClient {

	public void uploadFile(File f, String remoteAbsoluteFilename, FileTransferCommandListener listener);
	public void uploadFile(byte[] data, String remoteAbsoluteFilename, FileTransferCommandListener listener);
	public void closeConnection();
	
}
