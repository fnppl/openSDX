package org.fnppl.opensdx.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Vector;

import org.fnppl.opensdx.file_transfer.FTPClient;
import org.fnppl.opensdx.file_transfer.FileTransferClient;
import org.fnppl.opensdx.file_transfer.OSDXFileTransferClient;
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
import org.fnppl.opensdx.xml.ChildElementIterator;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;

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

//import org.fnppl.opensdx.common.*;

public class Feed extends BusinessObject {
	public static String KEY_NAME = "feed";
	
	private FeedInfo feedinfo;								//MUST
	private Vector<Bundle> bundles;							//SHOULD
	private Vector<Item> single_items;						//COULD
	
	public static Feed make(FeedInfo feedinfo) {
		Feed f = new Feed();
		f.feedinfo = feedinfo;
//		f.bundles = new BusinessCollection<Bundle>() {
//			public String getKeyname() {
//				return "bundles";
//			}
//		};
		f.bundles = null;
		
		f.single_items = null;
		return f;
	}
	
	public static Feed fromBusinessObject(BusinessObject bo) {
		if (bo==null) return null;
		if (!bo.getKeyname().equals(KEY_NAME)) {
			bo = bo.handleBusinessObject(KEY_NAME);
		}
		if (bo==null) return null;
		
		final Feed f = new Feed();
		f.initFromBusinessObject(bo);
		
		try {
			f.feedinfo = FeedInfo.fromBusinessObject(f);
			f.bundles = null;
//			new ChildElementIterator(bo, "bundles","bundle") {
//				public void processBusinessObject(BusinessObject bo) {
//					f.addBundle(Bundle.fromBusinessObject(bo));
//				}
//			};
			new ChildElementIterator(bo, "bundle") {
				public void processBusinessObject(BusinessObject bo) {
					f.addBundle(Bundle.fromBusinessObject(bo));
				}
			};
			f.single_items = new Vector<Item>() {
				public String getKeyname() {
					return "items";
				}
			};
			
			new ChildElementIterator(bo, "items","item") {
				public void processBusinessObject(BusinessObject bo) {
					Item item = Item.fromBusinessObject(bo);
					if (item!=null) {
						f.addSingleItem(item);
					}
				}
			};
			return f;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	
	
	public Result sendToReceiver(MessageHandler mh, OSDXKey signatureKey) {
		Receiver receiver = feedinfo.getReceiver();
		if (receiver==null) {
			return Result.error("Please enter complete receiver information in FeedInfo tab first.");
		}
		
		String type = receiver.getType();
		
		FileTransferClient client = null;
		if (type.equals(Receiver.TRANSFER_TYPE_OSDX_FILESERVER)) {
			if (signatureKey==null) {
				Result getSigKey = selectPrivateSigningKey(null, mh); 
				if (getSigKey.succeeded) {
					signatureKey = (OSDXKey)getSigKey.userobject;
				} else {
					return Result.error("no signature key for signing feed.");
				}
			}
			try {
				Result result = initOSDXFileTransferClient(receiver, mh);
				if (!result.succeeded) {
					return result;
				}
				client = (OSDXFileTransferClient)result.userobject;
			} catch (Exception e) {
				e.printStackTrace();
				Result r = Result.error("ERROR: Upload of Feed failed.");
				r.exception = e;
				return r;
			}
		}
		else if (type.equals(Receiver.TRANSFER_TYPE_FTP)) {
			if (signatureKey==null) {
				Result getSigKey = selectPrivateSigningKey(null, mh); 
				if (getSigKey.succeeded) {
					signatureKey = (OSDXKey)getSigKey.userobject;
				} else {
					return Result.error("no signature key for signing feed.");
				}
			}
			try {
				Result result = initFTPClient(receiver, mh);
				if (!result.succeeded) {
					return result;
				}
				client = (OSDXFileTransferClient)result.userobject;
			} catch (Exception e) {
				e.printStackTrace();
				Result r = Result.error("ERROR: Upload of Feed failed.");
				r.exception = e;
				return r;
			}
		}
		else {
			return Result.error("Sorry, receiver type \""+type+"\" not implemented.");
		}
		if (client!=null) {
			try {
				Result result = uploadFeed(client, signatureKey);
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
	
	private Result initOSDXFileTransferClient(Receiver r, MessageHandler mh) {
		
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
			boolean ok = c.connect(servername, port, prepath, mysigning, username);
			if (!ok) {
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
	
	private Result selectPrivateSigningKey(String keystore, MessageHandler mh) {
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
			return Result.error("Abort upload by user.");
		} catch (Exception e1) {
			Result.error("Error opening keystore:\n"+f.getAbsolutePath());
		}
		return Result.error("unknown error");
	}
	
	private Result initFTPClient(Receiver r, MessageHandler mh) {
		String host = r.getServername();
		String username = r.getUsername();
		if (username==null || username.length()==0) {
			return Result.error("Missing parameter: username"); 
		}
		String password = mh.requestPasswordTitleAndMessage("Enter password", "Please enter password for user account:");
		if (password == null) {
			Result.error("ERROR: No password.");
		}
		try {
			FTPClient client = null;
			try {
				client = FTPClient.connect(host, username, password);
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
	
	
//	public Result upload(String host, int port, String prepath, String username, OSDXKey mysigning) {
//		try {
//			OSDXFileTransferClient client = new OSDXFileTransferClient();
//			boolean ok = client.connect(host, port, prepath, mysigning, username);
//			if (!ok) {
//				return Result.error("ERROR: Connection to server could not be established.");
//			}
//			uploadFeed(client);
//			client.closeConnection();
//			
//			//Dialogs.showMessage("Upload of Feed successful.");
//		} catch (Exception e) {
//			e.printStackTrace();
//			Result.error("ERROR: Upload of Feed failed.");
//			//Dialogs.showMessage("ERROR: Upload of Feed failed.");
//		}
//		return Result.succeeded();
//	}
	
//	public Result uploadFTP(String host, String username, String password) {
//		try {
//			FTPClient client = null;
//			try {
//				client = FTPClient.connect(host, username, password);
//			} catch (Exception ex) {
//				ex.printStackTrace();
//				return Result.error("ERROR: Connection to server could not be established.");
//			}
//			if (client==null) {
//				return Result.error("ERROR: Connection to server could not be established.");
//			}
//			uploadFeed(client);			
//			client.closeConnection();
//			
//			//Dialogs.showMessage("Upload of Feed successful.");
//		} catch (Exception e) {
//			e.printStackTrace();
//			Result.error("ERROR: Upload of Feed failed.");
//			//Dialogs.showMessage("ERROR: Upload of Feed failed.");
//		}
//		return Result.succeeded();
//	}
	
	private Result uploadFeed(FileTransferClient client, OSDXKey signaturekey) throws Exception {
		String feedid = getFeedinfo().getFeedID();
		String normFeedid = Util.filterCharactersFile(feedid.toLowerCase());
		System.out.println("norm feedid: "+normFeedid);
		if (normFeedid.length()==0) {
			normFeedid = "unnamed_feed";
		}
		
		//make a copy to remove private information and change to relative file paths  
		Feed copyOfFeed = Feed.fromBusinessObject(BusinessObject.fromElement(this.toElement()));
		
		//remove private info
		try {
			copyOfFeed.getFeedinfo().getReceiver().file_keystore(null);
		} catch (Exception ex) {}
		
		//norm feedid
		String dir = normFeedid+"_"+SecurityHelper.getFormattedDate(System.currentTimeMillis()).substring(0,20);
		dir = Util.filterCharactersFile(dir);
		
		//build file structure, SOMEONE might want to change this
		client.mkdir(dir);
		client.cd(dir);
		
		//upload feed
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		Element eFeed = copyOfFeed.toElement();
		Document.buildDocument(eFeed).output(bOut);
		byte[] feedbytes = bOut.toByteArray();
		client.uploadFile(normFeedid+".xml",feedbytes);
		
		//upload feed signature
		byte[][] checks  = SecurityHelper.getMD5SHA1SHA256(feedbytes);
		Signature feed_sig = Signature.createSignature(checks[1], checks[2], checks[3], "signature of "+normFeedid+".xml",signaturekey);
		bOut = new ByteArrayOutputStream();
		Document.buildDocument(feed_sig.toElement()).output(bOut);
		client.uploadFile(normFeedid+".osdx.sig",bOut.toByteArray());
		
		//upload all bundle and item files
		Bundle bundle = copyOfFeed.getBundle(0);
		if (bundle!=null) {
			//bundle files (cover, booklet, ..)
			for (int j=0;j<bundle.getFilesCount();j++) {
				try {
					ItemFile nextItemFile = bundle.getFile(j);
					File nextFile = new File(nextItemFile.getLocationPath());
					String ending = nextFile.getName();
					if (ending.contains(".")) {
						ending = ending.substring(ending.lastIndexOf('.'));
					} else {
						ending = "";
					}
					String md5 = SecurityHelper.HexDecoder.encode(nextItemFile.getChecksums().getMd5(),'\0',-1);
					String filename = normFeedid+"_0_"+j+"_"+md5+ending;
					nextItemFile.setLocation(FileLocation.make(filename)); //relative filename to location path
					client.uploadFile(nextFile, filename);
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
							String ending = nextFile.getName();
							if (ending.contains(".")) {
								ending = ending.substring(ending.lastIndexOf('.'));
							} else {
								ending = "";
							}
							String md5 = SecurityHelper.HexDecoder.encode(nextItemFile.getChecksums().getMd5(),'\0',-1);
							String filename = normFeedid+"_"+(i+1)+(subIndex?"_"+(j+1):"")+"_"+md5+ending;
							nextItemFile.setLocation(FileLocation.make(filename)); //relative filename to location path
							client.uploadFile(nextFile, filename);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
			}
		}
		
		//upload feed finished file
		client.uploadFile(normFeedid+".finished",new byte[]{0});
		return Result.succeeded();
	}
	
	public Feed addBundle(Bundle bundle) {
		if (bundles == null) bundles = new Vector<Bundle>();
		bundles.add(bundle);
		return this;
	}
	
	public Feed addSingleItem(Item item) {
		if (single_items==null) {
			single_items = new Vector<Item>() {
				public String getKeyname() {
					return "items";
				}
			};
		}
		single_items.add(item);
		return this;
	}
	
	public void removeSingleItem(int index) {
		if (single_items==null) return;
		single_items.remove(index);
		if (single_items.size()==0) {
			single_items = null;
		}
	}
	
	public int getItemsCount() {
		if (single_items==null) return 0;
		return single_items.size();
	}
	
	public Feed setFeedInfo(FeedInfo feedinfo) {
		this.feedinfo = feedinfo;
		return this;
	}
	
	public FeedInfo getFeedinfo() {
		return feedinfo;
	}

	public Bundle getBundle(int index) {
		if (bundles!=null && index<bundles.size()) {
			return bundles.get(index);
		}
		return null;
	}
	
	public int getBundleCount() {
		if (bundles==null) return 0;
		return bundles.size();
	}

	public Item getSingleItems(int index) {
		if (single_items!=null && index<single_items.size()) {
			return single_items.get(index);
		}
		return null;
	}

	public String getKeyname() {
		return KEY_NAME;
	}


}
