package org.fnppl.opensdx.demo;

/*
 * Copyright (C) 2010-2013 
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
import java.net.*;
import java.security.Security;

import org.fnppl.opensdx.security.*;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import org.fnppl.opensdx.security.AsymmetricKeyPair;

import org.fnppl.opensdx.security.PublicKey;

public class OpenSDX {

	//HT 21.02.2011 - commented out for transition...
	
//	public static void signFileDetachedClearText(String file, String keyringFile, String passPhrase) {
//		try {
//			File sf = new File(file);
//			KeyRingCollection c = KeyRingCollection.fromFile(new File(keyringFile), passPhrase.toCharArray());
//			SignAndVerify.createSignatureFile(sf, c.getSomeRandomKeyPair());
//			System.out.println("File \""+sf.getName()+"\" is now signed.");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
//	public static void verifyFileDetachedClearText(File signed, File signature, File keyringFile, String passPhrase) {
//		try {
//			if (!keyringFile.exists()) {
//				System.out.println("SORRY, KEYRING-COLLECTION DOES NOT EXIST.");
//				return;
//			}
//			
//			if (!signed.exists()) {
//				System.out.println("SORRY, FILE "+signed.getAbsolutePath()+" DOES NOT EXIST.");
//				return;
//			}
//			if (!signature.exists()) {
//				System.out.println("SORRY, FILE "+signature.getAbsolutePath()+" DOES NOT EXIST.");
//				return;
//			}
//			KeyRingCollection c = KeyRingCollection.fromFile(keyringFile, passPhrase.toCharArray());
//			
//			Signature s = new Signature(signature, signed, c);
//			boolean v = s.tryVerification(); //SignAndVerify.verifySignature(f, c);
//			if (v) {
//				System.out.println("File \""+signed.getName()+"\" verified.");
//			} else {
//				System.out.println("File \""+signed.getName()+"\" NOT verified.");
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
//	public static void addKeyPairToKeyRingCollection(String keyringFile, String userid, String passPhrase) {
//		try {
//			File f = new File(keyringFile);
//			KeyRingCollection c = null;
//			if (f.exists()) {
//				c = KeyRingCollection.fromFile(f, passPhrase.toCharArray());
//			} else {
//				c = KeyRingCollection.generateNewKeyRingOnFile(new File(keyringFile), passPhrase.toCharArray(), true);
//			}
//			System.out.println("Generating new keypair...");
//			c.addAsymmetricKeyPair(userid, KeyPairGenerator.generateAsymmetricKeyPair());
//			c.printAllKeys(false);
//			c.save();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
//	public static void addPublicKeyFromGPG(String keyringFile, String passPhrase, String id) {
//		try {
//			File f = new File(keyringFile);
//			KeyRingCollection c = null;
//			if (f.exists()) {
//				c = KeyRingCollection.fromFile(f, passPhrase.toCharArray());
//			} else {
//				c = KeyRingCollection.generateNewKeyRingOnFile(new File(keyringFile), passPhrase.toCharArray(), false);
//			}
//
//			if (!id.startsWith("0x")) id = "0x"+id;
//			String path = "http://gpg-keyserver.de/pks/lookup?search="+id+"&op=get";
//			String s = readUrl(path);
//			if (s!=null) {
//				try {
//					s = s.substring(s.indexOf("-----BEGIN PGP PUBLIC KEY BLOCK-----"));
//					s = s.substring(0,s.lastIndexOf("-----END PGP PUBLIC KEY BLOCK-----"));
//					s += "-----END PGP PUBLIC KEY BLOCK-----"; //kein bock zu zaehlen;
//					
//					System.out.println("old list");
//					c.printAllKeys(false);
//					PublicKey key = new PublicKey(s);
//					c.addNewPublicKey(key);
//					
//					c.save();
//					System.out.println("Adding public key from gpg-keyserver.de, id: 0x"+Long.toHexString(key.getPGPPublicKey().getKeyID()));
//					c.printAllKeys(false);
//					
//				} catch (Exception ex) {
//					System.out.println("COULD NOT ADD PUBLIC KEY "+id);
//					ex.printStackTrace();
//				}
//				
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
//	private static boolean uploadKey(PGPPublicKey key) {
//
//		StringBuffer s = new StringBuffer();
//		s.append("POST /pks/add HTTP/1.1\n");
//		s.append("Host: gpg-keyserver.de\n");
//		s.append("Content-Type: application/x-www-form-urlencoded\n");
//		s.append("keytext=");
//        try {
//            
//          // Send the request
////             URL url = new URL("http://gpg-keyserver.de/pks/add");
////             URLConnection conn = url.openConnection();
////             conn.setDoOutput(true);
////             OutputStream out = conn.getOutputStream(); 
//        	OutputStream out = System.out;
//
//        	out.write(s.toString().getBytes());
//        	BCPGOutputStream bOut = new BCPGOutputStream(new ArmoredOutputStream(out));
//    		key.encode(bOut);
//    		bOut.close();
//    		out.flush();
//            out.close();
//            
////            //response
////            StringBuffer answer = new StringBuffer();
////            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
////            String line;
////            while ((line = reader.readLine()) != null) {
////                answer.append(line);
////            }
////            out.close();
////            reader.close();
////            
////            //Output the response
////            System.out.println(answer.toString());
//            
//            return true;
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return false;
//			        
////			 POST /pks/add HTTP/1.1
////			 Host: gpg-keyserver.de
////			 User-Agent: Mozilla/5.0 (X11; U; Linux x86_64; de; rv:1.9.2.13) Gecko/20101206 Ubuntu/10.10 (maverick) Firefox/3.6.13
////			 Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
////			 Accept-Language: de-de,de;q=0.8,en-us;q=0.5,en;q=0.3
////			 Accept-Encoding: gzip,deflate
////			 Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7
////			 Keep-Alive: 115
////			 Connection: keep-alive
////			 Referer: http://gpg-keyserver.de/
////			 Cookie: __utma=107703892.160807991.1297962046.1297962046.1298024590.2; __utmz=107703892.1297962046.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); __utmb=107703892.1.10.1298024590; __utmc=107703892
////			 Content-Type: application/x-www-form-urlencoded
////			 Content-Length: 1026
////
////			 keytext=-----BEGIN+PGP+PUBLIC+KEY+BLOCK-----%0D%0AVersion%3A+BCPG+v1.45%0D%0A%0D%0AmQENBE1eSLcBCADuz6%2F2S5oIOux2Mnt652zPjMaLZtialLl3ZDcYgfKBhRhpgVP%2F%0D%0A5eO7oRl0q9%2BNQtGBDG9AW7xlp3D11vE3EuBEgQiNIAsIN%2Bc3uDq00qFx3Uk9KjY5%0D%0ASDh1%2FiZaEXA%2Fyg%2Bys3nOVcbhSjeYXjmvekTm5agcyFkQ6kEE%2FbpSOmUWX%2Bch%2BcA9%0D%0AvOg4ZHKOoKEHlK%2FYrKvATbcQnjYPBEwBmk68p1000cvIbFxBh%2FRF5IQmVaxsdVVv%0D%0AqYfxWT%2F7UpkS1V5ZCz7IiyIPBu4OCtYOtw%2B%2BOEDjAN8tbNfX6GH8mXaNAuXmUabh%0D%0ADIXlucEvK7mEQpRs9DQhYpkl771oJLCMqSz5ABEBAAG0CmJiLXRlc3QtYmKJARwE%0D%0AEwECAAYFAk1eSLcACgkQqjoU%2B9Nvi8W3CQgAkGRMtNHYM5AplQmIyi1X7zZQ7Xug%0D%0A6YiPUQpcONAWr35khUGSlCwtDQs%2FK4eqGny6t16cqHuiPf%2BsLSrfa4egygR7G5VI%0D%0AmcO0n5W4AOfoDHknvFnE1zX84Z47%2BCEdpImMHZcuVpJCuPUtjU%2B0CdZf7Ko56sCd%0D%0ASV5e1E%2FnC5dojE1kQiRgL3HqMPiWYZAIgNSqnsVCAHg11YI4Z1qG%2FPyKiTztnZM5%0D%0AANhwEP2%2BhdM7zxNCFLfDX1OrbGovqgEnQJfbL%2B%2Fvwj2bjFdv%2BSUN73V5%2Bf%2F1l6dD%0D%0ARzR35qvkyULyQHRLtX4YJrNTs19bOXljmqVwunx2DHUcmncOsTOv3Gj2BQ%3D%3D%0D%0A%3Dmc4a%0D%0A-----END+PGP+PUBLIC+KEY+BLOCK-----
//			 
//	}
//	
//	
//	private static String readUrl(String name) {
//		String output = "";
//	    URL url = null;
//	    URLConnection urlConn = null;
//		 
//		try {	
//	        url  = new URL(name);
//	        urlConn = url.openConnection();
//	        urlConn.addRequestProperty("User-Agent","Mozilla/5.0 (X11; U; Linux i686)");
//	        InputStreamReader in = new InputStreamReader(urlConn.getInputStream());
//			
//	        BufferedReader buff= new BufferedReader(in);
//	      
//			int i;
//			String zeile;
//			while ((zeile = buff.readLine())!=null) {
//				output += zeile+"\n";
//			}
//		
//			//System.out.println(output);
//			
//			buff.close();
//			in.close();
//		} catch (Exception e) {
//			System.err.println(e.toString());
//			return null;
//		}
//		return output;
//	}
//	
//	public static void listKeys(String keyringFile, String passPhrase, boolean more) {
//		try {
//			File f = new File(keyringFile);
//			KeyRingCollection c = null;
//			if (f.exists()) {
//				c = KeyRingCollection.fromFile(f, passPhrase.toCharArray());
//				c.printAllKeys(more);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
//	public static void uploadKeyToGPG(String keyringFile, String passPhrase, int nr) {
//		//CAUTION: START COUNTING AT 1
//		try {
//			File f = new File(keyringFile);
//			KeyRingCollection c = null;
//			if (f.exists()) {
//				c = KeyRingCollection.fromFile(f, passPhrase.toCharArray());
//				AsymmetricKeyPair kp = c.getAsymmetricKeyPair(nr);
//				PGPPublicKey k = kp.getPGPPublicKey();
//				KeyRingCollection.printPublicKey(k);
//				
//				
//				InputStreamReader isr = new InputStreamReader(System.in);
//				BufferedReader br = new BufferedReader(isr);
//				System.out.println("Do you really want to upload the public key (id: "+kp.getKeyIDHex()+")? y/n");
//				String a = br.readLine();
//				if (a.toLowerCase().equals("yes")||a.toLowerCase().equals("y")) {
//					System.out.println("uploading key "+kp.getKeyIDHex());
//					uploadKey(kp.getPGPPublicKey());			
//				} else {
//					System.out.println("upload canceled.");
//				}
//			} else {
//				System.out.println("file does not exist");
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
//
//	private static void printUsage() {
//		System.out.println("usage:");
//		System.out.println("- to sign detached-cleartext file:\n  - OpenSDX -s file keyringcollectionFile passphrase\n");
//		System.out.println("- to verify detached-cleartext:\n  - OpenSDX -v file keyringcollectionFile passphrase\n");
//		System.out.println("- add keypair to keyringcollection_file (new collection if not exists):\n  - OpenSDX -a keyringcollectionFile userid passphrase\n");
//		System.out.println("- add pub-key to keyringcollection_file (new collection if not exists) from gpg-keyserver.de:\n  - OpenSDX -gpg keyringcollectionFile passphrase hexkeyid\n");
//		System.out.println("- list keys in keyringcollection_file:\n  - OpenSDX -list keyringcollectionFile passphrase\n");
//		System.out.println("- list keys in keyringcollection_file:\n  - OpenSDX -listLong keyringcollectionFile passphrase\n");
//		System.out.println("- upload key_nr to gpg-keyserver:\n  - OpenSDX -up keyringcollectionFile passphrase key_nr\n");
//		
//	}
//	
//	public static void test() {
//		printUsage();
//		System.out.println("\n\n");
//			
//		String privateCol = "test-set/private-keyring-collection";
//		String id = "bb-test-bb";
//		String pass = "asdgasidughawef";
//		
//		String publicCol = "test-set/public-keyring-collection";
//		String publicPass = "doesnot matter in this case";
//		
//		String testfile = "test-set/feed_1234567890.xml";
//		
//		new File(privateCol).delete();
//		new File(publicCol).delete();
//		
//		addKeyPairToKeyRingCollection(privateCol, id, pass);
//		signFileDetachedClearText(testfile, privateCol, pass);
//		
//		File signed = new File(testfile);
//		File signature = new File(signed.getParentFile(), signed.getName()+".asc");
//		verifyFileDetachedClearText(signed, signature, new File(privateCol), pass);
//		uploadKeyToGPG(privateCol, pass, 1);
//		
//		//manual upload BB20110217
//		addPublicKeyFromGPG(publicCol,publicPass,"0x92a43eeb49d34395");
//		listKeys(publicCol, publicPass, true);
//		
//		
//		try {
//			KeyRingCollection c = KeyRingCollection.fromFile(new File(privateCol), pass.toCharArray());
//			String keyID = c.getAsymmetricKeyPair(1).getKeyIDHex();
//			System.out.println("looking for key: "+keyID+" ...");
//			
//			addPublicKeyFromGPG(publicCol, publicPass, keyID);
//			
////			verifyFileDetachedClearText(testfile, publicCol, publicPass);
//			verifyFileDetachedClearText(signed, signature, new File(publicCol), pass);
//						
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		System.exit(0);
//	}
//	
//	public static void main(String[] a) {
//		SecurityHelper.ensureBC();
//		
//		test();
//		
//		if (a.length<3) {printUsage();return;};
//		
//		if (a[0].equals("-s") && a.length==4) {
//			signFileDetachedClearText(a[1],a[2],a[3]);
//		} else if (a[0].equals("-v") && a.length==5) {
//			verifyFileDetachedClearText(new File(a[1]),new File(a[2]),new File(a[3]), a[4]);
//		} else if (a[0].equals("-a") && a.length==4) {
//			addKeyPairToKeyRingCollection(a[1],a[2],a[3]);
//		} else if (a[0].equals("-gpg") && a.length==4) {
//			addPublicKeyFromGPG(a[1],a[2],a[3]);
//		} else if (a[0].equals("-list") && a.length==3) {
//			listKeys(a[1],a[2], false);
//		} else if (a[0].equals("-listLong") && a.length==3) {
//			listKeys(a[1],a[2],true);
//		} else if (a[0].equals("-up") && a.length==4) {
//			uploadKeyToGPG(a[1],a[2],Integer.parseInt(a[3]));
//		}
//		else {
//			printUsage();
//		}
//		
//	}

}
