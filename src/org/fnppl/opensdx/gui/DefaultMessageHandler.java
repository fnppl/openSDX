package org.fnppl.opensdx.gui;

import java.io.File;
import java.util.Vector;

import org.fnppl.opensdx.security.Identity;
import org.fnppl.opensdx.security.KeyApprovingStore;
import org.fnppl.opensdx.security.MasterKey;
import org.fnppl.opensdx.security.OSDXKey;

public class DefaultMessageHandler implements MessageHandler {

	public void showErrorMessage(String title, String message) {
		Dialogs.showMessage(message);
	}
	
	public boolean requestIgnoreVerificationFailure() {
		int a = Dialogs.showYES_NO_Dialog("Verification failed", "KeyStore:  localproof and signoff of keypairs failed.\nIgnore?");
		if (a==Dialogs.YES) return true;
		return false;
	}
	
	public boolean requestIgnoreKeyLogVerificationFailure() {
		int a = Dialogs.showYES_NO_Dialog("Verification failed", "KeyStore:  localproof and signoff of keylog failed.\nIgnore?");
		if (a==Dialogs.YES) return true;
		return false;
	}

	public MasterKey requestMasterSigningKey(KeyApprovingStore keystore) throws Exception {
		MasterKey signkey = null;
		Vector<MasterKey> signoffkeys = keystore.getAllSigningMasterKeys();
		if (signoffkeys.size()==0) {
			Dialogs.showMessage("Sorry, no masterkeys for signing available.");
		}
		
		Vector<String> keynames = new Vector<String>();
		for (int i=0; i<signoffkeys.size(); i++) {
			//Vector<Identity> ids = signoffkeys.get(i).getIdentities();
			MasterKey mk = signoffkeys.get(i);
			String name = mk.getKeyID()+", "+mk.getIDEmailAndMnemonic();
//			for (Identity id : ids) {
//				name += ", "+id.getID;
//			}
			keynames.add(name);
		}
		
		
		int ans = Dialogs.showSelectDialog("Select signing key", "Please select a MASTER key to sign all unsigned keypairs and keylogs in keystore", keynames);
		if (ans >= 0) {
			signkey = signoffkeys.elementAt(ans); 
		} else {
			throw new Exception("KeyStore:  signoff of localproof of keypairs failed.");
		}
		return signkey;
	}

	public boolean requestOverwriteFile(File file) {
		return Dialogs.YES == Dialogs.showYES_NO_Dialog("OVERWRITE?", "File \""+file.getName()+"\" exits?\nDo you really want to overwrite?");
	}
	
	public char[] requestPasswordTitleAndMessage(String title, String message) {
		return Dialogs.showPasswordDialog(title, message);
	}

	public char[] requestPassword(String keyid, String mantra) {
		return Dialogs.showPasswordDialog("UNLOCK PRIVATE KEY", "KeyID: "+keyid+"\nPlease enter passphrase for mantra: \""+mantra+"\"");
	}

	public String[] requestNewPasswordAndMantra(String message) {
		return Dialogs.showNewMantraPasswordDialog(message);
	}

	public void fireWrongPasswordMessage() {
		Dialogs.showMessage("Sorry, wrong password!");
	}

	public File chooseOriginalFileForSignature(File dir, String selectFile) {
		return Dialogs.chooseOpenFile("Please select original file for signature verification", dir, selectFile);
	}

	public File requestOpenKeystore() {
		return Dialogs.chooseOpenFile("Open KeyStore", new File("openSDX"), "keystore.xml");
	}
	
}
