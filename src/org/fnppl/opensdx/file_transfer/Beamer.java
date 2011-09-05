package org.fnppl.opensdx.file_transfer;
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Vector;

import org.fnppl.opensdx.common.Bundle;
import org.fnppl.opensdx.common.BundleInformation;
import org.fnppl.opensdx.common.BusinessObject;
import org.fnppl.opensdx.common.Feed;
import org.fnppl.opensdx.common.FileLocation;
import org.fnppl.opensdx.common.IDs;
import org.fnppl.opensdx.common.Item;
import org.fnppl.opensdx.common.ItemFile;
import org.fnppl.opensdx.common.ItemTags;
import org.fnppl.opensdx.common.LicenseBasis;
import org.fnppl.opensdx.common.Receiver;
import org.fnppl.opensdx.common.Util;
import org.fnppl.opensdx.dmi.FeedCreator;
import org.fnppl.opensdx.gui.DefaultMessageHandler;
import org.fnppl.opensdx.gui.Dialogs;
import org.fnppl.opensdx.gui.MessageHandler;
import org.fnppl.opensdx.security.KeyApprovingStore;
import org.fnppl.opensdx.security.MasterKey;
import org.fnppl.opensdx.security.OSDXKey;
import org.fnppl.opensdx.security.Result;
import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.security.Signature;
import org.fnppl.opensdx.security.SubKey;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;

public class Beamer {


	public static Result beamUpFeed(Feed feed, OSDXKey signatureKey, MessageHandler mh) {
		Receiver receiver = feed.getFeedinfo().getReceiver();
		if (receiver==null) {
			return Result.error("Please enter complete receiver information in FeedInfo tab first.");
		}
		
		String type = receiver.getType();
		
		FileTransferClient client = null;
		
		if (!type.equals(Receiver.TRANSFER_TYPE_OSDX_FILESERVER) && !type.equals(Receiver.TRANSFER_TYPE_FTP)) {
			return Result.error("Sorry, receiver type \""+type+"\" not implemented.");
		}
		//get signature key, if null
		if (signatureKey==null) {
			Dialogs.showMessage("You need a private key for signing the uploaded feed.\nPlease open your keystore and select your key in the next steps.");
			Result getSigKey = selectPrivateSigningKey(null, mh); 
			if (getSigKey.succeeded) {
				signatureKey = (OSDXKey)getSigKey.userobject;
			} else {
				return Result.error("no signature key for signing feed.");
			}
		}
		
		if (type.equals(Receiver.TRANSFER_TYPE_OSDX_FILESERVER)) {
			try {
				Result result = initOSDXFileTransferClient(receiver, mh);
				if (!result.succeeded) {
					return result;
				}
				client = (FileTransferClient)result.userobject;
			} catch (Exception e) {
				e.printStackTrace();
				Result r = Result.error("ERROR: Upload of Feed failed.");
				r.exception = e;
				return r;
			}
		}
		else if (type.equals(Receiver.TRANSFER_TYPE_FTP)) {
			try {
				Result result = initFTPClient(receiver, mh);
				if (!result.succeeded) {
					return result;
				}
				client = (FileTransferClient)result.userobject;
			} catch (Exception e) {
				e.printStackTrace();
				Result r = Result.error("ERROR: Upload of Feed failed.");
				r.exception = e;
				return r;
			}
		}
		
		if (client!=null) {
			try {
				Result result = uploadFeed(feed, client, signatureKey);
				client.closeConnection();
				return result;
			} catch (Exception ex) {
				ex.printStackTrace();
				Result r = Result.error("ERROR: Upload of Feed failed.");
				r.exception = ex;
				return r;
			}
		}
		return Result.error("unknown error");
	}
	
	public static Vector<String[]> getUploadExtraFiles(Feed feed) {
		Vector<ExtraFile> files = getUploadExtraFile(feed);
		Vector<String[]> sfiles = new Vector<String[]>();
		for (ExtraFile f : files) {
			sfiles.add(new String[] {f.file.getAbsolutePath(), f.new_filename});
		}	
		return sfiles;
	}
	
	private static Vector<ExtraFile> getUploadExtraFile(Feed feed) {
		Vector<ExtraFile> files = new Vector<ExtraFile>();
		
		String normFeedid = getNormFeedID(feed);
		int num = 1;
		for (int b=0;b<feed.getBundleCount();b++) {
			Bundle bundle = feed.getBundle(b);
			if (bundle!=null) {
				//bundle files (cover, booklet, ..)
				for (int j=0;j<bundle.getFilesCount();j++) {
					try {
						ItemFile nextItemFile = bundle.getFile(j);
						File nextFile = new File(nextItemFile.getLocationPath());
						String md5 = SecurityHelper.HexDecoder.encode(nextItemFile.getChecksums().getMd5(),'\0',-1);
						String filename = normFeedid+"_"+num+"_"+md5;
						num++;
						files.add(new ExtraFile(nextItemFile, nextFile, filename));
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				
				//item files
				for (int i=0;i<bundle.getItemsCount();i++) {
					Item item = bundle.getItem(i);
					if (item.getFilesCount()>0) {
						boolean subIndex = (item.getFilesCount()>1);
						for (int j=0;j<item.getFilesCount();j++) {
							try {
								ItemFile nextItemFile = item.getFile(j);
								File nextFile = new File(nextItemFile.getLocationPath());
								String md5 = SecurityHelper.HexDecoder.encode(nextItemFile.getChecksums().getMd5(),'\0',-1);
								String filename = normFeedid+"_"+num+"_"+md5;
								num++;
								files.add(new ExtraFile(nextItemFile, nextFile, filename));
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					}
				}
			}
		}
		return files;
	}
			
			//old
//			if (bundle!=null) {
//				//bundle files (cover, booklet, ..)
//				for (int j=0;j<bundle.getFilesCount();j++) {
//					try {
//						ItemFile nextItemFile = bundle.getFile(j);
//						File nextFile = new File(nextItemFile.getLocationPath());
////						String ending = nextFile.getName();
////						if (ending.contains(".")) {
////							ending = ending.substring(ending.lastIndexOf('.'));
////						} else {
////							ending = "";
////						}
//						String ending = "";
//						String md5 = SecurityHelper.HexDecoder.encode(nextItemFile.getChecksums().getMd5(),'\0',-1);
//						String filename = normFeedid+"_0_"+j+"_"+md5+ending;
//						nextItemFile.setLocation(FileLocation.make(filename)); //relative filename to location path
//						client.uploadFile(nextFile, filename);
//					} catch (Exception ex) {
//						ex.printStackTrace();
//					}
//				}
//				
//				//item files
//				for (int i=0;i<bundle.getItemsCount();i++) {
//					Item item = bundle.getItem(i);
//					if (item.getFilesCount()>0) {
//						boolean subIndex = (item.getFilesCount()>1);
//						for (int j=0;j<item.getFilesCount();j++) {
//							try {
//								ItemFile nextItemFile = item.getFile(j);
//								File nextFile = new File(nextItemFile.getLocationPath());
////								String ending = nextFile.getName();
////								if (ending.contains(".")) {
////									ending = ending.substring(ending.lastIndexOf('.'));
////								} else {
////									ending = "";
////								}
//								String ending = "";
//								String md5 = SecurityHelper.HexDecoder.encode(nextItemFile.getChecksums().getMd5(),'\0',-1);
//								String filename = normFeedid+"_"+(i+1)+(subIndex?"_"+(j+1):"")+"_"+md5+ending;
//								nextItemFile.setLocation(FileLocation.make(filename)); //relative filename to location path
//								client.uploadFile(nextFile, filename);
//							} catch (Exception ex) {
//								ex.printStackTrace();
//							}
//						}
//					}
//				}
//			}
//		}
		
	
	public static String getNormFeedID(Feed feed) {
		String feedid = feed.getFeedinfo().getFeedID();
		String normFeedid = Util.filterCharactersFile(feedid.toLowerCase());
		System.out.println("norm feedid: "+normFeedid);
		if (normFeedid.length()==0) {
			normFeedid = "unnamed_feed";
		}
		return normFeedid;
	}
	
	public static Result uploadFeed(Feed feed, FileTransferClient client, OSDXKey signaturekey) throws Exception {
		String normFeedid = getNormFeedID(feed);
		
		//make a copy to remove private information and change to relative file paths  
		Feed copyOfFeed = Feed.fromBusinessObject(BusinessObject.fromElement(feed.toElement()));
		
		//remove private info
		try {
			copyOfFeed.getFeedinfo().getReceiver().file_keystore(null);
		} catch (Exception ex) {}
		
		//directory for date
		String datedir = Util.filterCharactersFile(SecurityHelper.getFormattedDateDay(System.currentTimeMillis()));
		
		//norm feedid
		String dir = normFeedid;
		//String dir = normFeedid+"_"+SecurityHelper.getFormattedDate(System.currentTimeMillis()).substring(0,19).replace(' ', '_');
		//dir = Util.filterCharactersFile(dir);
		
		//build file structure
		client.mkdir(datedir);
		client.cd(datedir);
		
		client.mkdir(dir);
		client.cd(dir);
		
		//upload feed
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		Element eFeed = copyOfFeed.toElement();
		Document.buildDocument(eFeed).output(bOut);
		byte[] feedbytes = bOut.toByteArray();
		client.uploadFile(normFeedid+".xml",feedbytes, null);
		
		//upload feed signature
		byte[][] checks  = SecurityHelper.getMD5SHA1SHA256(feedbytes);
		Signature feed_sig = Signature.createSignature(checks[1], checks[2], checks[3], normFeedid+".xml",signaturekey);
		bOut = new ByteArrayOutputStream();
		Document.buildDocument(feed_sig.toElement()).output(bOut);
		client.uploadFile(normFeedid+".osdx.sig",bOut.toByteArray(),null);
		
		//upload all bundle and item files
		Vector<ExtraFile> files = getUploadExtraFile(copyOfFeed);
		for (ExtraFile f : files) {
			try {
				ItemFile nextItemFile = f.itemFile;
				File nextFile = f.file;
				String filename = f.new_filename;
				nextItemFile.setLocation(FileLocation.make(filename)); //relative filename to location path
				client.uploadFile(nextFile, filename, null);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		//upload feed finished file
		client.uploadFile(normFeedid+".finished",new byte[]{0},null);
		return Result.succeeded();
		
//		Bundle bundle = copyOfFeed.getBundle(0);
//		if (bundle!=null) {
//			//bundle files (cover, booklet, ..)
//			for (int j=0;j<bundle.getFilesCount();j++) {
//				try {
//					ItemFile nextItemFile = bundle.getFile(j);
//					File nextFile = new File(nextItemFile.getLocationPath());
////					String ending = nextFile.getName();
////					if (ending.contains(".")) {
////						ending = ending.substring(ending.lastIndexOf('.'));
////					} else {
////						ending = "";
////					}
//					String ending = "";
//					String md5 = SecurityHelper.HexDecoder.encode(nextItemFile.getChecksums().getMd5(),'\0',-1);
//					String filename = normFeedid+"_0_"+j+"_"+md5+ending;
//					nextItemFile.setLocation(FileLocation.make(filename)); //relative filename to location path
//					client.uploadFile(nextFile, filename);
//				} catch (Exception ex) {
//					ex.printStackTrace();
//				}
//			}
//			
//			//item files
//			for (int i=0;i<bundle.getItemsCount();i++) {
//				Item item = bundle.getItem(i);
//				if (item.getFilesCount()>0) {
//					boolean subIndex = (item.getFilesCount()>1);
//					for (int j=0;j<item.getFilesCount();j++) {
//						try {
//							ItemFile nextItemFile = item.getFile(j);
//							File nextFile = new File(nextItemFile.getLocationPath());
////							String ending = nextFile.getName();
////							if (ending.contains(".")) {
////								ending = ending.substring(ending.lastIndexOf('.'));
////							} else {
////								ending = "";
////							}
//							String ending = "";
//							String md5 = SecurityHelper.HexDecoder.encode(nextItemFile.getChecksums().getMd5(),'\0',-1);
//							String filename = normFeedid+"_"+(i+1)+(subIndex?"_"+(j+1):"")+"_"+md5+ending;
//							nextItemFile.setLocation(FileLocation.make(filename)); //relative filename to location path
//							client.uploadFile(nextFile, filename);
//						} catch (Exception ex) {
//							ex.printStackTrace();
//						}
//					}
//				}
//			}
//		}
	}
	
	
	private static Result selectPrivateSigningKey(String keystore, MessageHandler mh) {
		
		File f;
		if (keystore!=null) {
			f = new File(keystore);
		} else {
			f = mh.requestOpenKeystore();
		}
		if (f==null) return Result.error("keystore could not be opened.");
		
		MessageHandler mh2 = new DefaultMessageHandler() {
			public boolean requestOverwriteFile(File file) {
				return false;
			}
			public boolean requestIgnoreVerificationFailure() {
				return false;
			}
			public boolean requestIgnoreKeyLogVerificationFailure() {
				return false;
			}
		};
		try {
			KeyApprovingStore store = KeyApprovingStore.fromFile(f, mh2); 
			
			Vector<OSDXKey> storedPrivateKeys = store.getAllPrivateSigningKeys();
			if (storedPrivateKeys==null || storedPrivateKeys.size()==0) {
				return Result.error("Sorry, no private key for signing in keystore");
			}
			Vector<String> select = new Vector<String>();
			int[] map = new int[storedPrivateKeys.size()];
			for (int i=0;i<storedPrivateKeys.size();i++) {
				OSDXKey k = storedPrivateKeys.get(i);
				if (k.allowsSigning()) {
					if (k.isMaster()) {
						select.add(k.getKeyID()+", "+((MasterKey)k).getIDEmailAndMnemonic());
					}
					else if (k.isSub()) {
						select.add(k.getKeyID()+" subkey of "+((SubKey)k).getParentKey().getIDEmailAndMnemonic());
					}
					else {
						select.add(k.getKeyID());
					}
					map[select.size()-1] = i;
				}
			}
			int ans = Dialogs.showSelectDialog("Select private key","Please select a private key for signing", select);
			if (ans>=0 && ans<select.size()) {
				OSDXKey key = storedPrivateKeys.get(map[ans]);
				Result res = Result.succeeded();
				res.userobject = key;
				key.unlockPrivateKey(mh);
				return res;
			}
			return Result.error("Aborted by user.");
		} catch (Exception e1) {
			Result.error("Error opening keystore:\n"+f.getAbsolutePath());
		}
		return Result.error("unknown error");
	}

	
	private static Result initOSDXFileTransferClient(Receiver r, MessageHandler mh) {
		
		String servername = r.getServername();
		int port = r.getPort();
		if (port<=0) port = 4221;
		String prepath = r.getPrepath();
		String username = r.getUsername();
		if (username==null || username.length()==0) {
			return Result.error("Missing parameter: username"); 
		}
		
		File f = null;
		
		String keystore = r.getFileKeystore();
		String keyid = r.getKeyID();
		
		if (keystore!=null) {
			f = new File(keystore);
		} else {
			f = mh.requestOpenKeystore();
		}
		if (f==null) return Result.error("keystore could not be opened.");
		
		OSDXKey mysigning = null;
		MessageHandler mh2 = new DefaultMessageHandler() {
			public boolean requestOverwriteFile(File file) {
				return false;
			}
			public boolean requestIgnoreVerificationFailure() {
				return false;
			}
			public boolean requestIgnoreKeyLogVerificationFailure() {
				return false;
			}
		};
		try {
			KeyApprovingStore store = KeyApprovingStore.fromFile(f, mh2); 
			
			if (keyid!=null) {
				mysigning = store.getKey(keyid);
				if (mysigning==null) {
					return Result.error("You given key id \""+keyid+"\"\nfor authentification could not be found in selected keystore.\nPlease select a valid key.");
				}
			}
			
		} catch (Exception e1) {
			Result.error("Error opening keystore:\n"+f.getAbsolutePath());
		}
		if (mysigning==null) return Result.error("no signing key");
		try {
			mysigning.unlockPrivateKey(mh);
		} catch (Exception e1) {
			//e1.printStackTrace();
			return Result.error("Sorry, wrong password.");
		}
		
		if (!mysigning.isPrivateKeyUnlocked()) {
			return Result.error("Sorry, private is is locked.");
		}
		
		try {
			OSDXFileTransferClient c = new OSDXFileTransferClient();
			
			try {
				boolean ok = c.connect(servername, port, prepath, mysigning, username);
				if (!ok) {
					return Result.error("ERROR: Connection to server could not be established.");
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				return Result.error("ERROR: Connection to server could not be established.");
			}
			Result res = Result.succeeded();
			res.userobject = c;
			
			//uploadFeed(client);
			//c.closeConnection();
			
			return res; 
		} catch (Exception e) {
			e.printStackTrace();
			Result res = Result.error("ERROR: Upload of Feed failed.");
			res.exception = e;
			return res;
		}
//		
	}
	
	private static Result initFTPClient(Receiver r, MessageHandler mh) {
		String host = r.getServername();
		String username = r.getUsername();
		if (username==null || username.length()==0) {
			return Result.error("Missing parameter: username"); 
		}
		char[] password = mh.requestPasswordTitleAndMessage("Enter password", "Please enter password for user account:");
		if (password == null) {
			Result.error("ERROR: No password.");
		}
		try {
			FTPClient client = null;
			try {
				client = FTPClient.connect(host, username, password.toString());
			} catch (Exception ex) {
				ex.printStackTrace();
				return Result.error("ERROR: Connection to server could not be established.");
			}
			if (client==null) {
				return Result.error("ERROR: Connection to server could not be established.");
			}
			Result res = Result.succeeded();
			res.userobject = client;
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			Result res = Result.error("ERROR: Upload of Feed failed.");
			res.exception = e;
			return res;
		}
//		
	}
	
	
	
	public static void main(String[] args) {
		// init example feed
		Feed currentFeed = FeedCreator.makeExampleFeed();
		long now = System.currentTimeMillis();
		Receiver receiver = Receiver.make(Receiver.TRANSFER_TYPE_OSDX_FILESERVER)
			.servername("localhost")
			.serveripv4("127.0.0.1")
			.authtype(Receiver.AUTH_TYPE_KEYFILE)
			.file_keystore("/home/neo/openSDX/defaultKeyStore.xml")
			.keyid("AF:08:7F:7E:92:D8:48:98:24:7B:56:00:71:F8:47:65:62:8A:46:EA@localhost")
			.username("user_1");
		Receiver receiver2 = Receiver.make(Receiver.TRANSFER_TYPE_FTP)
		.servername("it-is-awesome.de")
		.authtype(Receiver.AUTH_TYPE_LOGIN)
		.username("baumbach");
		currentFeed.getFeedinfo().receiver(receiver2);
		currentFeed.getBundle(0).addItem(
				Item.make(IDs.make().amzn("item1 id"), "testitem1", "testitem", "v0.1", "video", "display artist",
						BundleInformation.make(now,now), LicenseBasis.makeAsOnBundle(),null)
						.addFile(ItemFile.make(new File("fnppl_contributor_license.pdf")))
					.tags(ItemTags.make()
						.addGenre("Rock")
					)
						
		);
		currentFeed.getBundle(0).getLicense_basis().getTerritorial()
		.allow("DE")
		.allow("GB")
		.disallow("US");

		OSDXKey signKey = null;
		Result sk = selectPrivateSigningKey("/home/neo/openSDX/defaultKeyStore.xml", new DefaultMessageHandler());
		if (sk.succeeded) signKey = (OSDXKey)sk.userobject;
		
		Result result = beamUpFeed(currentFeed, signKey, new DefaultMessageHandler());
		
		if (result.succeeded) {
			System.out.println("beaming successful");
		} else {
			System.out.println("error in beaming: "+result.errorMessage);
		}
	}
}

class ExtraFile {
	
	ItemFile itemFile = null;
	File file = null;
	String new_filename = null;
	
	public ExtraFile(ItemFile itemFile, File file, String new_filename) {
		this.itemFile = itemFile;
		this.file = file;
		this.new_filename = new_filename;
	}
	
}
