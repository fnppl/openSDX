package org.fnppl.opensdx.security;


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

import java.io.*;
import java.util.*;

import org.fnppl.opensdx.common.Util;
import org.fnppl.opensdx.gui.DefaultConsoleMessageHandler;
import org.fnppl.opensdx.gui.MessageHandler;
import org.fnppl.opensdx.xml.*;

public class SecurityManagerCommandline {
	
	private boolean help = false;   // --help
	private String cmd = null;		//sign, verify, encrypt, decrpyt
	private String keystore = null; // --keystore
	private String keyid = null;    // --keyid
	private String keypw = null;    // --keypw
	private String keypwfile = null;// --keypwfile
	private String config = null;   // --config
	private String inputName = null;// --in
	private String outputName = null;// --out
	private String tsa = null;		// --tsa

	
	private KeyApprovingStore store = null;
	private OSDXKey key = null;
	private SecurityControl sec = null;
	
	private MessageHandler mh;
	
	public SecurityManagerCommandline() {
		mh = new DefaultConsoleMessageHandler();
	}
	
	public void parseArgs(String[] args) {
		//System.out.println("args: "+Arrays.toString(args));
		
		int i=0;
		try {
			while (i<args.length) {
				String s = args[i];
				if (s.startsWith("--")) {
					if (s.equals("--keystore")) {
						keystore = args[i+1];
						i+=2;
					}
					else if (s.equals("--keyid")) {
						keyid = args[i+1];
						i+=2;
					}
					else if (s.equals("--keypw")) {
						keypw = args[i+1];
						i+=2;
					}
					else if (s.equals("--keypwfile")) {
						keypwfile = args[i+1];
						i+=2;
					}
					else if (s.equals("--config")) {
						config = args[i+1];
						i+=2;
					}
					else if (s.equals("--in")) {
						inputName = args[i+1];
						i+=2;
					}
					else if (s.equals("--out")) {
						outputName = args[i+1];
						i+=2;
					}
					else if (s.equals("--tsa")) {
						tsa = args[i+1];
						i+=2;
					}
					else if (s.equals("--help")) {
						help = true;
						i+=1;
					}
					else if (s.startsWith("--")) {
						System.out.println("CANT UNDERSTAND ARGUMENT: "+s+" "+args[i+1]);
						i+=2;
					}
				} else {
					cmd = s.toLowerCase();
					i+=1;
				}
			}
			
			if (config!=null) {
				//read config
				Element ec = Document.fromFile(new File(config)).getRootElement();
//				if (host==null && ec.getChild("host")!=null) host = ec.getChildText("host");
//				if (port == -1 && ec.getChild("port")!=null) port = Integer.parseInt(ec.getChildText("port"));
//				if (prepath==null && ec.getChild("prepath")!=null) prepath = ec.getChildText("prepath");
//				if (remotepath==null && ec.getChild("remotepath")!=null) remotepath = ec.getChildText("remotepath");
//				if (username==null) {
//					if (ec.getChild("username")!=null) username = ec.getChildText("username");
//					if (ec.getChild("user")!=null) username = ec.getChildText("user");
//				}
				if (keystore==null && ec.getChild("keystore")!=null) keystore = ec.getChildText("keystore");
				if (keyid==null && ec.getChild("keyid")!=null) keyid = ec.getChildText("keyid");
				if (keypw==null && ec.getChild("keypw")!=null) keypw = ec.getChildText("keypw");
				if (keypwfile==null && ec.getChild("keypwfile")!=null) keypwfile = ec.getChildText("keypwfile");

				//private key
				if (ec.getChild("keypair")!=null) {
					key = OSDXKey.fromElement(ec.getChild("keypair"));
				}
			}

			
		} catch (Exception ex) {
			System.out.println("usage: SecurityManagerCommandline --host localhost --port 4221 --prepath \"/\" --user username --keystore defautlKeyStore.xml --keyid 11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE:FF:11:22:33:44:55 --keypw key-password [file or list of files to upload]");
			System.out.println("usage: SecurityManagerCommandline --config configfile.xml [file or list of files to upload]");
			ex.printStackTrace();
		}
	}
	
	private void initKey() {
		try {
			//init key
			if (key==null) {
				if (keystore==null) {
					error("missing paramenter: key in configfile or keystore");
				}
				if (keyid==null) {
					error("missing paramenter: key in configfile or keyid");
				}
				store = KeyApprovingStore.fromFile(new File(keystore), mh);
				key = store.getKey(keyid);
				if (key==null) error("error: keyid: "+keyid+" not found in given keystore.");
			}
		} catch (Exception ex) {
			error("Error opening keystore from file "+keystore);
		}
		try {
			//unlock key
			if (keypw!=null) {
				key.unlockPrivateKey(keypw);
			} else if (keypwfile!=null) {
				keypw = Util.loadText(keypwfile);
				key.unlockPrivateKey(keypw);
			} else {
				key.unlockPrivateKey(mh);
			}
			if (!key.isPrivateKeyUnlocked()) {
				error("can not unlock private key");
			}
		} catch (Exception ex) {
			error("can not unlock private key");
		}
	}
	
	private static void error(String msg) {
		if (msg!=null) System.out.println(msg);
		System.out.println("use the --help argument to get more information");
		
		System.exit(1);
	}
	
	private void showHelp() {
		System.out.println("------------------------------------------");
		System.out.println("OpenSDX Security Package Command Line Tool");
		System.out.println("------------------------------------------");
		
		if (cmd==null || cmd.equals("sign")) {
			System.out.println("\nCommand SIGN");
			System.out.println(  "-------------------------");
			System.out.println("  Creates a signature for a given file.");
			System.out.println("\n  arguments");
			System.out.println("   --keystore FILENAME-KEYSTORE");
			System.out.println("   --keyid KEYID-IN-KEYSTORE");
			System.out.println("   --keypw PASSWORD");
			System.out.println("   --keypwfile FILENAME-PASSWORD");
			System.out.println("   --tsa HOSTNAME:PORT: COULD for extra TSA signature, e.g. tsa.fnppl.org:8890");
			System.out.println("   --in FILENAME-INPUT");
			System.out.println("   --out FILENAME-OUTPUT: default = FILENAME-INPUT_signature.xml");
			
			
			System.out.println("");
			System.out.println("  The key's password can be provided by --keypw OR --keypwfile argument OR you will be asked for it at the console");
			System.out.println("\n  usage example: SIGN --keystore defautlKeyStore.xml --keyid 11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE:FF:11:22:33:44:55 --keypwfile key-password.txt --in example.xml");
			
		}
		if (cmd==null || cmd.equals("verify")) {
			System.out.println("\nCommand VERIFY");
			System.out.println(  "---------------------------");
			System.out.println("  Verifies a signature for a given file in the context of a given keystore.");
			System.out.println("\n  arguments");
			System.out.println("   --keystore FILENAME-KEYSTORE: a keystore is needed for knowing which keys to trust and knowing the right keyserver settings");
			System.out.println("   --in FILENAME-INPUT");
			System.out.println("   --out FILENAME-OUTPUT: default = FILENAME-INPUT_verify_report.xml");
			
			System.out.println("\n  usage example: VERIFY --keystore defautlKeyStore.xml --in example_signature.xml");
		}
		System.out.println("\n");
	}
	
	public void process(String[] args) {
		if (args==null || args.length==0) {
			showHelp();
			System.exit(0);
		}
		parseArgs(args);
		if (help) {
			showHelp();
			return;
		}
		if (cmd==null) {
			error("Error: Missing command");
		}
		else if (cmd.equals("sign")) {
			sign();
		}
		else if (cmd.equals("verify")) {
			int ret = verify();
			System.exit(ret);
		}
	}
	
	private void sign() {
		File fileIn = getFileIn();
		
		initKey();
		File fileOut = getFileOut(fileIn, "_signature.xml");
		if (outputName == null) {
			fileOut = new File(fileIn.getAbsolutePath()+"_signature.xml");
		} else {
			fileOut = new File(outputName);
		}
		try {
			System.out.println("Creating signature");
			System.out.println("key id      : "+key.getKeyID());
			System.out.println("input file  : "+fileIn.getAbsolutePath());
			System.out.println("output file : "+fileOut.getAbsolutePath());
			if (tsa==null) {
				Signature.createSignatureFile(fileIn, fileOut, key);	
			} else {
				System.out.println("TSA instance: "+tsa);
				Signature.createSignatureFile(fileIn, fileOut, key, tsa);
			}
			
			System.out.println("Ready.");
		} catch (Exception e) {
			//e.printStackTrace();
			error("Error: Signature creation failed.");
		}
	}
	
	public static int justCheckSignature(File keystore, File signature) throws Exception {
		SecurityControl sec = new SecurityControl();
		sec.setMessageHandler(null);
		
		KeyApprovingStore store = KeyApprovingStore.fromFile(keystore, null);
		
		sec.setKeyStore(store);
		
		KeyVerificator kv = KeyVerificator.make();
		Vector<OSDXKey> privKeys = store.getAllPrivateSigningKeys();
		for (OSDXKey key : privKeys) {
			kv.addKeyRating(key, TrustRatingOfKey.RATING_ULTIMATE);
		}
		sec.setKeyverificator(kv);
		sec.resetKeyClients();;
		
		File fileIn = signature;
		
		int ret = 1; //fail
		
		Result res = sec.verifyFileSignature(fileIn);
		if (res.succeeded) {
			System.out.println("VERIFICATION SUCCEEDED.");
			ret = 0;
		} else {
			System.out.println("VERIFICATION FAILED.");
		}
		File fileOut = new File(signature.getParentFile(), signature.getName()+"_verify_report.xml");
		try {
			Document.buildDocument(res.report).writeToFile(fileOut);
			System.out.println("Report created in "+fileOut.getAbsolutePath());
		} catch (Exception e) {
			System.out.println("Warning: Report creation failed.");
			//e.printStackTrace();
		}
		return ret;
	}
	
	private int verify() {
		initSecurityControl();
		File fileIn = getFileIn();
		
		int ret = 1; //fail
		
		Result res = sec.verifyFileSignature(fileIn);
		if (res.succeeded) {
			System.out.println("VERIFICATION SUCCEEDED.");
			ret = 0;
		} else {
			System.out.println("VERIFICATION FAILED.");
		}
		File fileOut = getFileOut(fileIn, "_verify_report.xml");
		try {
			Document.buildDocument(res.report).writeToFile(fileOut);
			System.out.println("Report created in "+fileOut.getAbsolutePath());
		} catch (Exception e) {
			System.out.println("Warning: Report creation failed.");
			//e.printStackTrace();
		}
		return ret;
	}
	
	private void initSecurityControl() {
		sec = new SecurityControl();
		sec.setMessageHandler(mh);
		if (keystore==null) {
			error("Missing parameter: --keystore FILENAME-KEYSTORE");
		}
		try {
			store = KeyApprovingStore.fromFile(new File(keystore), mh);
		} catch (Exception ex) {
			error("Error opening keystore from file "+keystore);
		}
		sec.setKeyStore(store);
		
		KeyVerificator kv = KeyVerificator.make();
		Vector<OSDXKey> privKeys = store.getAllPrivateSigningKeys();
		for (OSDXKey key : privKeys) {
			kv.addKeyRating(key, TrustRatingOfKey.RATING_ULTIMATE);
		}
		sec.setKeyverificator(kv);
		sec.resetKeyClients();;
		
	}
	
	
	
	private File getFileIn() {
		if (inputName==null) {
			error("Missing parameter: --in FILENAME");
		}
		File fileIn = new File(inputName);
		if (!fileIn.exists()) {
			error("Input file does not exist.");
		}
		if (fileIn.isDirectory()) {
			error("Input file may not be a directory");
		}
		return fileIn;
	}
	
	private File getFileOut(File fileIn, String ext) {
		File fileOut = null;
		if (outputName == null) {
			fileOut = new File(fileIn.getAbsolutePath()+ext);
		} else {
			fileOut = new File(outputName);
		}
		return fileOut;
	}
	public static void main(String[] args) throws Exception {
		SecurityManagerCommandline cmd = new SecurityManagerCommandline();
		cmd.process(args);
	}
}


