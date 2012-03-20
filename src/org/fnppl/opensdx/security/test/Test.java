package org.fnppl.opensdx.security.test;

/*
 * Copyright (C) 2010-2012 
 * 							fine people e.V. <opensdx@fnppl.org> 
 * 							Henning Thieß <ht@fnppl.org>
 * 
 * 							http://fnppl.org
*/

/*
 * Software license
 *
 * For those parts of this file, which are identified as software, rather than documentation, this software-license applies / shall be applied. 
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
 * For those parts of this file, which are identified as documentation, rather than software, this documentation-license applies / shall be applied.
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

/**
 * 
 * @author Bertram Bödeker <bboedeker@gmx.de>
 *
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigInteger;
import java.util.UUID;
import java.util.Vector;

import org.fnppl.opensdx.gui.DefaultMessageHandler;
import org.fnppl.opensdx.gui.MessageHandler;
import org.fnppl.opensdx.security.AsymmetricKeyPair;
import org.fnppl.opensdx.security.DataSourceStep;
import org.fnppl.opensdx.security.Identity;
import org.fnppl.opensdx.security.KeyApprovingStore;
import org.fnppl.opensdx.security.KeyClient;
import org.fnppl.opensdx.security.KeyLog;
import org.fnppl.opensdx.security.KeyLogAction;
import org.fnppl.opensdx.security.KeyServerIdentity;
import org.fnppl.opensdx.security.KeyVerificator;
import org.fnppl.opensdx.security.MasterKey;
import org.fnppl.opensdx.security.OSDXKey;
import org.fnppl.opensdx.security.PublicKey;
import org.fnppl.opensdx.security.Result;
import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.security.SubKey;
import org.fnppl.opensdx.security.SymmetricKey;
import org.fnppl.opensdx.security.TrustRatingOfKey;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;



public class Test {
	

	
	public static void makeExampleKeyStore() {
		try {
			File testPath = new File(System.getProperty("user.home"), "openSDX_test");
			testPath.mkdirs();
			
			File fKeyStore = new File(testPath,"new_example_keystore.xml");
			
			//build MessageHandler
			MessageHandler messageHandler = new DefaultMessageHandler() {
				public boolean requestOverwriteFile(File file) {//dont ask, just overwrite
					return true;
				}
				public boolean requestIgnoreKeyLogVerificationFailure() {//dont ignore failed keylog verification
					return false;
				}
			};
			
			//build keystore
			KeyApprovingStore keystore = KeyApprovingStore.createNewKeyApprovingStore(fKeyStore, messageHandler);
			
			//create master and subkey
			MasterKey master = MasterKey.buildNewMasterKeyfromKeyPair(AsymmetricKeyPair.generateAsymmetricKeyPair());
			Identity id = Identity.newEmptyIdentity();
			id.setIdentNum(1);
			id.setEmail("example@fnppl.org");
			id.setMnemonic("example");
		
			id.setCountry("Example country");
			id.setRegion("exmaple region");
			id.setPostcode("12345");
			id.setCity("Example City");
			
			id.setCompany("fnppl e.V."); id.set_company_restricted(false);
			id.setUnit("A");
			id.setSubunit("111");
			id.setFunction("CIO");
			
			id.setFirstNames("Herr");
			id.setMiddlename("B.");
			id.setSurname("Ert");
			id.setBirthday_gmt("1919-01-01");
			id.setPlaceofbirth("Somewhere");
			
			id.setPhone("+44 99 002020221");
			id.setFax("+44 99 002020222");
			
			id.setNote("example note");
			BufferedImage img = new BufferedImage(90,120,BufferedImage.TYPE_INT_RGB);
			Graphics g = img.getGraphics();
			g.setColor(Color.BLACK);
			g.fillRect(0,0,90,120);
			g.setColor(Color.YELLOW);
			g.fillOval(10, 25, 70, 70);
			g.setColor(Color.BLACK);
			g.setFont(new Font("Arial", Font.BOLD, 30));
			g.drawString(";-)",25, 70);
			img.flush();
			id.setPhoto(img);
			id.getDatapath().add(new DataSourceStep("LOCAL", System.currentTimeMillis()));
			
			master.addIdentity(id);
			SubKey sub = master.buildNewSubKeyfromKeyPair(AsymmetricKeyPair.generateAsymmetricKeyPair());
			
			//protect with password
			master.createLockedPrivateKey("password", "password");
			sub.createLockedPrivateKey("password", "password");
			
			//add keys to keystore
			keystore.addKey(master);
			keystore.addKey(sub);
			keystore.setSigningKey(master);
			
			//create a keylog from subkey to masterkey
			Identity approveId = Identity.newEmptyIdentity();
			approveId.setIdentNum(id.getIdentNum());
			approveId.setEmail(id.getEmail());
			approveId.setMnemonic(id.getMnemonic());
			
			KeyLogAction kla = KeyLogAction.buildKeyLogAction(KeyLogAction.APPROVAL, sub, master.getKeyID(), approveId,null);
			KeyLog kl = KeyLog.buildNewKeyLog(kla, "127.0.0.1", "127.0.0.1", sub);
			
			//add keylog to keystore
			keystore.addKeyLog(kl);

			//add comments
			
			Element eks = keystore.toElement();
			
			//keys
			Element e = eks.getChild("keys").getChild("keypair"); 
			e.addCommentFirst("example of asymmetric key pair");
			e.addCommentAfter("identities","identities should only occur on MASTER-key");
			e.addCommentAfter("usage","ONLYSIGN|CRYPT|BOTH ; MASTER-keys/REVOKE-keys MUST only have SIGN-capabilities... ");
			e.addCommentAfter("level","MASTER|REVOKE|SUB");
			e.addCommentAfter("parentkeyid","COULD ; should be missing for MASTER-key; sha1-fingerprint in hex without leading 0x for modulus of key which this one is child of");
			e.addCommentAfter("algo","RSA ; others not implemented yet");
			e.addCommentAfter("modulus","as hex-string without leading 0x ; only for RSA?!");
			e.getChild("pubkey").addCommentAfter("exponent","as hex-string with leading 0x");
			e.addCommentAfter("privkey","privkey may be missing");
			eks.getChild("keys").addCommentAfter("sha256localproof","the sha256 of all above elements (fieldnames, content and attributes)");
			eks.getChild("keys").getChildren("keypair").lastElement().addComment("could have x more keypairs");
			
			//signature
			e = eks.getChild("keys").getChild("signature").getChild("data");
			e.addCommentAfter("md5","the md5 of sha256localproof-bytes ; optional, but should be there ; there MUST be one data-hash-value given (md5, sha1 or sha256 - recommended to provide all three");
			e.addCommentAfter("sha1","the sha1 of sha256localproof-bytes ; optional, but should be there ; there MUST be one data-hash-value given (md5, sha1 or sha256 - recommended to provide all three");
			e.addCommentAfter("sha256","the sha256 of sha256localproof-bytes ; optional, but should be there ; there MUST be one data-hash-value given (md5, sha1 or sha256 - recommended to provide all three");
			e.addCommentAfter("signdatetime","MUST");
			e.addCommentAfter("dataname","SHOULD");
			e = eks.getChild("keys").getChild("signature");
			e.getChild("pubkey").addCommentFirst("given pubkey; should be verified from server/yourself");
			e.addCommentAfter("signaturebytes","as hex-string");
			
			
			//identity
			e = eks.getChild("keys").getChild("keypair").getChild("identities").getChild("identity");
			e.addCommentAfter("identnum","MUST");
			e.addCommentAfter("email","MUST");
			e.addCommentAfter("mnemonic","MUST ; shorthandle for this identities-purpose \"residency\" or \"work\" or whatever");
			e.addCommentAfter("country","COULD");
			e.addCommentAfter("region","COULD");
			e.addCommentAfter("postcode","COULD");
			e.addCommentAfter("city","COULD");
			e.addCommentAfter("company","COULD");
			e.addCommentAfter("unit","COULD");
			e.addCommentAfter("subunit","COULD");
			e.addCommentAfter("function","COULD ; function of that person");
			e.addCommentAfter("firstname_s","COULD");
			e.addCommentAfter("surname","COULD");
			e.addCommentAfter("middlename","COULD");
			e.addCommentAfter("birthday_gmt","COULD");
			e.addCommentAfter("placeofbirth","COULD");
			e.addCommentAfter("phone","COULD");
			e.addCommentAfter("fax","COULD");
			e.addCommentAfter("note","COULD");
			e.addCommentAfter("photo","SHOULD ; base64 .png ; maxrawdatasize: 100k");
			
			
			//keylogs
			eks.addCommentAfter("keylog", "could have x other keylogs");
			
			e = eks.getChild("keylog");
			e.addCommentAfter("ipv4","lets assume, those actions are always made via net");
			e.addCommentAfter("ipv6","lets assume, those actions are always made via net");
			e.addCommentAfter("sha256localproof","localproof of keylogaction");
			e.getChild("signature").addCommentFirst("signature of sha256localproof; typically from keyserver");
			e = e.getChild("keylogaction");
			e.addCommentFirst("keylogaction is generated and signed on client and sent to keyserver");
			e.addCommentAfter("from_keyid","must be the same as signing key");
			e.addCommentAfter("to_keyid","the key to which the keylog action refers to");
			e.getChild("approval").addCommentFirst("action: approval/disapproval/revocation/approval pending");
			e.getChild("approval").getChild("identity").addCommentFirst("list of all identity fields that are approved here");
			e.addCommentAfter("sha256localproof_complete","sha256 localproof if all identitiy fields are visible;  clients with a unrestricted view can verify the complete localproof");
			e.addCommentAfter("sha256localproof_restricted","sha256 localproof if restricted identitiy fields are not visible; clients with a restricted view can verify the restricted localproof");
			e.getChild("signature").addCommentFirst("signature of the concatenation of sha256 complete and restricted localproof; clients should verify one localproof depended an their restriction level and can take the other for granted.");
			
			eks.addCommentAfter("keystore_sha256_proof", "complete proof of all above element; signed by signing key below");
			
			
			//write to file
			Document.buildDocument(eks).writeToFile(fKeyStore);
			
			//System.out.println(kl.getSignDatetime()+" ?= "+kl.getActionSignatureKey().getValidFrom());
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	
	/**
	 * for Testing
	 * @param args
	 */
	public static void main(String[] args) {
//		
//		//try to read from example_keystore.xml
//		try {
//			KeyApprovingStore store = KeyApprovingStore.fromFile(new File("src/org/fnppl/opensdx/security/resources/example_keystore.xml"), new DefaultMessageHandler());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		//testGenerateMasterKeyPair();
		//testGeneratePublicKey();
		
		//testHeadEmployeeSigning();
		//makeExampleKeyStore();
		UUID id = UUID.randomUUID();
		id = UUID.nameUUIDFromBytes("hallo".getBytes());
		System.out.println(id);
	}
	
	
	public static void testHeadEmployeeSigning() {
		try {
			// first we need
			// a keyserver
			// an employees keystore with master and subkey
			// an head of department keystore with masterkey
			// a contractkey (which is a subkey of head of departments masterkey
			
			
			File testPath = new File(System.getProperty("user.home"), "openSDX_test");
			testPath.mkdirs();
			
			File fEmployeeKeyStore = new File(testPath,"ksEmployee.xml");
			File fHeadKeyStore = new File(testPath,"ksHead.xml");
			
			System.out.println(testPath.getAbsolutePath());
			
			MessageHandler messageHandler = new DefaultMessageHandler() {
				public boolean requestOverwriteFile(File file) {//dont ask, just overwrite
					return true;
				}
				public boolean requestIgnoreKeyLogVerificationFailure() {//dont ignore failed keylog verification
					return false;
				}
			};
			
			
			//keyserver
			KeyServerIdentity keyserver = KeyServerIdentity.make("localhost", 8889, "");
			
			KeyVerificator keyverificator = new KeyVerificator();
			keyverificator.addKeyServer(keyserver);
			
			KeyClient client = new KeyClient(keyserver, keyverificator);
			client.connect();
			
			KeyServerIdentity ksid = client.requestKeyServerIdentity();
			OSDXKey serverSigning = ksid.getKnownKeys().get(0);
			keyserver.addKnownKey(serverSigning);
			keyverificator.addKeyRating(serverSigning, TrustRatingOfKey.RATING_MARGINAL);
			
			
			
			//employees keystore
			KeyApprovingStore ksEmployee = null;
			if (fEmployeeKeyStore.exists()) {
				ksEmployee = KeyApprovingStore.fromFile(fEmployeeKeyStore, messageHandler);
			} else {
				//generate keys
				ksEmployee = KeyApprovingStore.createNewKeyApprovingStore(fEmployeeKeyStore, messageHandler);
				MasterKey master = MasterKey.buildNewMasterKeyfromKeyPair(AsymmetricKeyPair.generateAsymmetricKeyPair());
				Identity id = Identity.newEmptyIdentity();
				id.setEmail("employee@fnppl.org");
				id.setMnemonic("employee");
				master.addIdentity(id);
				SubKey sub = master.buildNewSubKeyfromKeyPair(AsymmetricKeyPair.generateAsymmetricKeyPair());
				
				//protect with password
				master.createLockedPrivateKey("password", "password");
				sub.createLockedPrivateKey("password", "password");
				
				//add to keystore
				ksEmployee.addKey(master);
				ksEmployee.addKey(sub);
				ksEmployee.setSigningKey(master);
				
				//and upload to server
				master.uploadToKeyServer(client);
				sub.uploadToKeyServer(client);
				
				ksEmployee.toFile(ksEmployee.getFile());
			}
			
			//head of departments keystore
			KeyApprovingStore ksHead = null;
			if (fHeadKeyStore.exists()) {
				ksHead = KeyApprovingStore.fromFile(fHeadKeyStore, messageHandler);
			} else {
				//generate keys
				ksHead = KeyApprovingStore.createNewKeyApprovingStore(fHeadKeyStore, messageHandler);
				MasterKey master = MasterKey.buildNewMasterKeyfromKeyPair(AsymmetricKeyPair.generateAsymmetricKeyPair());
				Identity id = Identity.newEmptyIdentity();
				id.setEmail("head@fnppl.org");
				id.setMnemonic("head of department");
				master.addIdentity(id);
				SubKey sub = master.buildNewSubKeyfromKeyPair(AsymmetricKeyPair.generateAsymmetricKeyPair());
				
				//protect with password
				master.createLockedPrivateKey("password", "password");
				sub.createLockedPrivateKey("password", "password");
				
				//add to keystore
				ksHead.addKey(master);
				ksHead.addKey(sub);
				ksHead.setSigningKey(master);
				
				//and upload to server
				master.uploadToKeyServer(client);
				sub.uploadToKeyServer(client);
				
				ksHead.toFile(ksHead.getFile());
			}
			
			//
			MasterKey masterHead = ksHead.getAllMasterKeys().get(0);
			SubKey contractKey = masterHead.getSubKeys().get(0);
			
			MasterKey masterEmployee = ksEmployee.getAllMasterKeys().get(0);
			SubKey subEmployee = masterEmployee.getSubKeys().get(0);
			
			masterHead.unlockPrivateKey("password");
			contractKey.unlockPrivateKey("password");
			masterEmployee.unlockPrivateKey("password");
			subEmployee.unlockPrivateKey("password");
			
			//output
			System.out.println("MasterKey Head     :: "+masterHead.getKeyID());
			System.out.println("Sub Contract       :: "+contractKey.getKeyID());
			System.out.println("MasterKey Employee :: "+masterEmployee.getKeyID());
			System.out.println("SubKey    Employee :: "+subEmployee.getKeyID());
			System.out.println("KeyServer          :: "+keyserver.getKnownKeys().get(0).getKeyID());
			
			
			
			//build approval from contractKey to subEmployee Key
			
			//check if approval already exits
			boolean approval = false;
			Vector<KeyLog> logs = client.requestKeyLogs(masterEmployee.getKeyID(),null);
			for (KeyLog kl : logs) {
				if (kl.getAction().equals(KeyLogAction.APPROVAL) && kl.getKeyIDFrom().equals(contractKey.getKeyID())) {
					approval = true;
				}
			}
			//System.out.println("approval: "+approval);
			if (!approval) {
				//no approval -> build it!
	 			KeyLogAction kl = KeyLogAction.buildKeyLogAction(KeyLogAction.APPROVAL, contractKey, masterEmployee.getKeyID(), masterEmployee.getCurrentIdentity(),null);
//				Result ok = kl.verifySignature();
//				if (ok.succeeded) {
//					System.out.println("ok");
//				} else {
//					System.out.println("error");
//				}
//	 			
//	 			Document.buildDocument(kl.toElement(true)).output(System.out);
//	 			KeyLogAction kl2 = KeyLogAction.fromElement(kl.toElement(false));
//	 			Document.buildDocument(kl2.toElement(true)).output(System.out);
//	 			ok = kl2.verifySignature();
//	 			if (ok.succeeded) {
//					System.out.println("ok");
//				} else {
//					System.out.println("error");
//				}
//	 			if (1==1) return;
	 			
	 			Result upload = kl.uploadToKeyServer(client, masterEmployee);
				if (upload.succeeded) {
					System.out.println("Generation of keylog on keyserver successful");
				} else {
					System.out.println("ERROR generating keylog on keyserver :: "+upload.errorMessage);
				}
			}
			
			//contract partner wants to verify a message signed by the subEmployee Key
			//chain of trust:: contract partner -> contractKey -> masterEmployee -> subEmployee
			KeyVerificator partnerKeyverificator = new KeyVerificator();
			partnerKeyverificator.addKeyServer(keyserver);
			partnerKeyverificator.addKeyRating(keyserver.getKnownKeys().get(0), TrustRatingOfKey.RATING_MARGINAL);
			partnerKeyverificator.addKeyRating(contractKey, TrustRatingOfKey.RATING_COMPLETE);
			
			Result verifySubEmployeeKey = partnerKeyverificator.verifyKey(subEmployee, System.currentTimeMillis());
			if (verifySubEmployeeKey.succeeded) {
				System.out.println("VERIFICATION of subEmployee Key SUCCESSFUL!");
			} else {
				System.out.println("WARNING: subEmployee Key could not be verified!\n"+verifySubEmployeeKey.errorMessage);
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
		
	}
	
	
	public static void testGenerateMasterKeyPair() {
		try {
			String mantraname = "masterkeypassword";
			String password = "password";
			String initv = "00112233445566778899AABBCCDDEEFF";
			SymmetricKey sk = SymmetricKey.getKeyFromPass(password.toCharArray(), SecurityHelper.HexDecoder.decode(initv));
			Vector<Identity> ids = new Vector<Identity>();
			Element id = new Element("identity");
			id.addContent("email", "test@fnppl.org");
			ids.add(Identity.fromElement(id));
			
			Element ekp = generateMasterKeyPair(ids, mantraname, sk);
			
			Document.buildDocument(ekp).output(System.out);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static Element generateMasterKeyPair(Vector<Identity> ids, String mantraname, SymmetricKey sk) throws Exception {
		
		AsymmetricKeyPair kp = AsymmetricKeyPair.generateAsymmetricKeyPair();
		
		//build structure
		Element ekp = new Element("keypair");
		//id part
		Element eids = new Element("identities");
		for (int i=0;i<ids.size();i++) {
			eids.addContent(ids.get(0).toElement(true));	
		}
		ekp.addContent(eids);
		
	  //key part
		//keyid
		ekp.addContent("sha1fingerprint", kp.getKeyID());
		ekp.addContent("authoritativekeyserver","keys.fnppl.org");
		
		//datapath
		Element edp = new Element("datapath");
		Element es1 = new Element("step1");
		es1.addContent("datasource", "keys.fnppl.org");
		es1.addContent("datainsertdatetime","2011-02-21 00:00:00 GMT+00:00");
		Element es2 = new Element("step2");
		es2.addContent("datasource", "keys.fnppl.org");
		es2.addContent("datainsertdatetime","2011-02-21 00:00:00 GMT+00:00");
		edp.addContent(es1);
		edp.addContent(es2);
		ekp.addContent(edp);
		
		//
		ekp.addContent("usage","ONLYSIGN");
		ekp.addContent("level","MASTER");
		ekp.addContent("parentkeyid","N.A.");
		
		ekp.addContent("algo","RSA");
		ekp.addContent("bits","3072");
		ekp.addContent("modulus",kp.getPublicModulusAsHex());
		
		//public key
		Element epk = new Element("pubkey");
		epk.addContent("exponent", kp.getPublicExponentAsHex());
		ekp.addContent(epk);
		
		//private key
		Element esk = new Element("privkey");
		Element eexp = new Element("exponent");
		Element eloc = new Element("locked");
		
		eloc.addContent("mantraname",mantraname);
		eloc.addContent("algo","AES@256");
		eloc.addContent("initvector",SecurityHelper.HexDecoder.encode(sk.getInitVector(),'\0',-1));
		eloc.addContent("padding","CBC/PKCS#7");
		eloc.addContent("bytes",SecurityHelper.HexDecoder.encode(kp.getEncrytedPrivateKey(sk),'\0',-1));
		eexp.addContent(eloc);
		esk.addContent(eexp);
		ekp.addContent(esk);
		
		//gpgkeyserverid
		ekp.addContent("gpgkeyserverid","");
		
		return ekp;
	}
	
	public static void testGeneratePublicKey() {
		try {
			String mod = "00B1C4337FE2E77E251D86334B1578C0BCA46AEB9CFE1BE7001ADA8E4C2C8BC2EE557296C46EC3D0470A6DEEC09634A424243B576F3DCA41E372F9AC2FEAA0B668F2AA000CCDCC0396BE4517B2F4B179FCCB7ACB9B3AF027DDAC3466AE80D70BEFBBC0C97E9E4AAF7D184DFE183F74BC9FFA5A5F85149B5A9808C7E12EFDEF42C4A936661F06BA15844DD0BCAF3C0CB8E04949263660A1E71DC1B4A0056519A6E662CEAB25F1B42DA537C21AD6584C2BF72092A0EC57A5C7E9A6458CC8BC06102B5902D90BD86850DA411DB004D66399F5B362EEA0DD5178AC89423FA60E63405290536067AF3EBF9F26E1DFE66E11B23209B62E062ED7F177B6F41CC97F1D6517F2542F40660ABC8D17D7B99778997013D69837FE410B137A283B461D6323F5042A49A59CFEF343B4829B751495400151514CD77ADCB9011F0054D6DF5E6B073EE2A96ECCDDE9029F18DA6C6361DA0147DD7FA59A44B7C87B1A82BDFEFD0DD8E6FCEB696E4883E27EDE204B669887B37D6C927071CFAE555BD235A3E19165C9B7";
			String exp = "010001";
			PublicKey k = new PublicKey(
					new BigInteger(SecurityHelper.HexDecoder.decode(mod)),
					new BigInteger(SecurityHelper.HexDecoder.decode(exp)));
			String parentkeyid = "BEEE542006AF8301096BF0305AB4632E9982AA94@keys.fnppl.org";

			Element epk = generateGeneratePublicKeyElement(k, parentkeyid);

			Document.buildDocument(epk).output(System.out);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	public static Element generateGeneratePublicKeyElement(PublicKey k, String parentkeyid) throws Exception {
		
		//build structure
		Element ekp = new Element("keypair");
		//no id part
		
	  //key part
		//keyid
		ekp.addContent("sha1fingerprint", k.getKeyID());
		ekp.addContent("authoritativekeyserver","keys.fnppl.org");
		
		//datapath
		Element edp = new Element("datapath");
		Element es1 = new Element("step1");
		es1.addContent("datasource", "keys.fnppl.org");
		es1.addContent("datainsertdatetime","2011-02-21 00:00:00 GMT+00:00");
		Element es2 = new Element("step2");
		es2.addContent("datasource", "keys.fnppl.org");
		es2.addContent("datainsertdatetime","2011-02-21 00:00:00 GMT+00:00");
		edp.addContent(es1);
		edp.addContent(es2);
		ekp.addContent(edp);
		
		//
		ekp.addContent("usage","ONLYSIGN");
		ekp.addContent("level","SUB");
		ekp.addContent("parentkeyid",parentkeyid);
		
		ekp.addContent("algo","RSA");
		ekp.addContent("bits","3072");
		ekp.addContent("modulus",k.getModulusAsHex());
		
		//public key
		Element epk = new Element("pubkey");
		epk.addContent("exponent", k.getPublicExponentAsHex());
		ekp.addContent(epk);
		
		//no private key
		
		//gpgkeyserverid
		ekp.addContent("gpgkeyserverid","");
		
		return ekp;
	}
	
}
