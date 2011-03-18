package org.fnppl.opensdx.gui;

import java.io.File;
import java.util.Vector;

import org.fnppl.opensdx.security.Identity;
import org.fnppl.opensdx.security.KeyApprovingStore;
import org.fnppl.opensdx.security.OSDXKeyObject;

public class DefaultMessageHandler implements MessageHandler {

	public boolean requestIgnoreVerificationFailure() {
		int a = Dialogs.showYES_NO_Dialog("Verification failed", "KeyStore:  localproof and signoff of keypairs failed.\nIgnore?");
		if (a==Dialogs.YES) return true;
		return false;
	}

	public OSDXKeyObject requestMasterSigningKey(KeyApprovingStore keystore) throws Exception {
		OSDXKeyObject signkey = null;
		Vector<OSDXKeyObject> signoffkeys = keystore.getAllSigningMasterKeys();
		if (signoffkeys.size()==0) {
			Dialogs.showMessage("Sorry, no masterkeys for signing available.");
		}
		
		Vector<String> keynames = new Vector<String>();
		for (int i=0; i<signoffkeys.size(); i++) {
			Vector<Identity> ids = signoffkeys.get(i).getIdentities();
			String name = signoffkeys.get(i).getKeyID();
			for (Identity id : ids) {
				name += ", "+id.getEmail();
			}
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

	public String requestPassword(String keyid, String mantra) {
		return Dialogs.showPasswordDialog("UNLOCK PRIVATE KEY", "KeyID: "+keyid+"\nPlease enter passphrase for mantra: \""+mantra+"\"");
	}

	public String[] requestNewPasswordAndMantra(String message) {
		return Dialogs.showNewMantraPasswordDialog(message);
	}

	@Override
	public void fireWrongPasswordMessage() {
		Dialogs.showMessage("Sorry, wrong password!");
	}
	
	
}
