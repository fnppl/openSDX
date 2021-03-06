package org.fnppl.opensdx.gui;

import java.io.File;

import org.fnppl.opensdx.security.KeyApprovingStore;
import org.fnppl.opensdx.security.MasterKey;

public interface MessageHandler {

	public static int CANCEL = -2;
	public static int OK = 0;
	public static int YES = 1;
	public static int NO = -1;
	
	public boolean requestIgnoreVerificationFailure();
	public boolean requestIgnoreKeyLogVerificationFailure();
	public MasterKey requestMasterSigningKey(KeyApprovingStore keystore) throws Exception;
	public boolean requestOverwriteFile(File file);
	public char[] requestPassword(String keyid, String mantra);
	public String[] requestNewPasswordAndMantra(String message);
	public void fireWrongPasswordMessage();
	public File chooseOriginalFileForSignature(File dir, String selectFile);
	public char[] requestPasswordTitleAndMessage(String title, String message);
	public File requestOpenKeystore();
	public void showErrorMessage(String title, String message);
}
