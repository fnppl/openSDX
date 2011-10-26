package org.fnppl.opensdx.gui;

import java.io.Console;
import java.io.File;
import java.util.Arrays;
import java.util.Vector;

import org.fnppl.opensdx.security.Identity;
import org.fnppl.opensdx.security.KeyApprovingStore;
import org.fnppl.opensdx.security.MasterKey;
import org.fnppl.opensdx.security.OSDXKey;

public class DefaultConsoleMessageHandler implements MessageHandler {

	private Console console = System.console();
	
	public void showErrorMessage(String title, String message) {
		System.out.println(message);
	}
	
	public boolean requestIgnoreVerificationFailure() {
		if (console!=null) {
			String antw = console.readLine("KeyStore:  localproof and signoff of keypairs failed.\nIgnore? (y/n)");
			if (antw.equalsIgnoreCase("y")) {
				return true;
			}
		}
		return false;
	}
	
	public boolean requestIgnoreKeyLogVerificationFailure() {
		if (console!=null) {
			String antw = console.readLine("KeyStore:  localproof and signoff of keylog failed.\nIgnore? (y/n)");
			if (antw.equalsIgnoreCase("y")) {
				return true;
			}
		}
		return false;
	}

	public MasterKey requestMasterSigningKey(KeyApprovingStore keystore) throws Exception {
		if (console==null) {
			System.out.println("ERROR, console not available!");
			throw new Exception("KeyStore:  signoff of localproof of keypairs failed.");
		}
		MasterKey signkey = null;
		Vector<MasterKey> signoffkeys = keystore.getAllSigningMasterKeys();
		if (signoffkeys.size()==0) {
			System.out.println("Sorry, no masterkeys for signing available.");
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
		
		System.out.println("Please select a MASTER key to sign all unsigned keypairs and keylogs in keystore");
		for (int i=0;i<keynames.size();i++) {
			System.out.println("("+(i+1)+") "+keynames.get(i));
		}
		String ant = console.readLine("Select number: ");
		
		int ans = Integer.parseInt(ant)-1;
		if (ans >= 0) {
			signkey = signoffkeys.elementAt(ans); 
		} else {
			throw new Exception("KeyStore:  signoff of localproof of keypairs failed.");
		}
		return signkey;
	}

	public boolean requestOverwriteFile(File file) {
		if (console==null) {
			System.out.println("ERROR, console not available!");
			return false;
		}
		String antw = console.readLine("File \""+file.getName()+"\" exits?\nDo you really want to overwrite? (y/n)");
		if (antw.equalsIgnoreCase("y")) {
			return true;
		}
		return false;
	}
	
	public char[] requestPasswordTitleAndMessage(String title, String message) {
		if (console==null) {
			System.out.println("ERROR, console not available!");
			return null;
		}
		System.out.println("\n"+title);
		System.out.println(message);
		String passw = console.readLine("Password: ");
		return passw.toCharArray();
	}

	public char[] requestPassword(String keyid, String mantra) {
		if (console==null) {
			System.out.println("ERROR, console not available!");
			return null;
		}
		System.out.println("\nUNLOCK PRIVATE KEY");
		System.out.println("KeyID: "+keyid+"\nPlease enter passphrase for mantra: \""+mantra+"\"");
		String passw = console.readLine(": ");
		return passw.toCharArray();
	}

	public String[] requestNewPasswordAndMantra(String message) {
		System.out.println("\nNEW PASSWORD AND MANTRA");
		
		boolean ok = false;
		String pw = "";
		while (!ok) {
			pw = console.readLine("Please enter a passphrase: ");
			String pw2 = console.readLine("Please repeat passphrase: ");
			if (!pw.equals(pw2)) {
				System.out.println("repeated password does not match password, please reenter...");
	    	} else {
	    		ok = true;
	    	}
		}
		String mantra = console.readLine("Please enter a mantra: ");
		return new String[] {mantra,pw};
	}

	public void fireWrongPasswordMessage() {
		System.out.println("Sorry, wrong password!");
	}

	public File chooseOriginalFileForSignature(File dir, String selectFile) {
		if (console==null) {
			System.out.println("ERROR, console not available!");
			return null;
		}
		//return Dialogs.chooseOpenFile("Please select original file for signature verification", dir, selectFile);
		System.out.println("Please enter name of original file for signature verification: ");
		String filename = console.readLine();
		return new File(filename);
	}

	public File requestOpenKeystore() {
		if (console==null) {
			System.out.println("ERROR, console not available!");
			return null;
		}
		String filename = console.readLine("Open KeyStore : please enter filename: ");
		return new File(filename);
	}
	
}
