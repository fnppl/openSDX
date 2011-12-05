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
import org.fnppl.opensdx.common.LicenseSpecifics;
import org.fnppl.opensdx.common.Receiver;
import org.fnppl.opensdx.common.Util;
import org.fnppl.opensdx.dmi.BundleItemStructuredName;
import org.fnppl.opensdx.dmi.FeedCreator;
import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferCommand;
import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferListCommand;
import org.fnppl.opensdx.file_transfer.model.RemoteFile;
import org.fnppl.opensdx.gui.DefaultMessageHandler;
import org.fnppl.opensdx.gui.Dialogs;
import org.fnppl.opensdx.gui.MessageHandler;
import org.fnppl.opensdx.helper.ProgressListener;
import org.fnppl.opensdx.keyserverfe.Helper;
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

	public static Result beamUpFeed(Feed feed, OSDXKey signatureKey, MessageHandler mh, String defaultKeystore, boolean includeFiles) {
		return beamUpFeed(feed, signatureKey, mh, defaultKeystore, includeFiles, null);
	}
	
	public static Result beamUpFeed(Feed feed, OSDXKey signatureKey, MessageHandler mh, String defaultKeystore, boolean includeFiles, ProgressListener pg) {
		Receiver receiver = feed.getFeedinfo().getReceiver();
		if (receiver==null) {
			return Result.error("Please enter complete receiver information in FeedInfo tab first.");
		}
		
		String type = receiver.getType();
		
		UploadClient client = null;
		
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
				Result result = initOSDXFileTransferClient(receiver, mh, defaultKeystore, signatureKey);
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
			try {
				Result result = initFTPClient(receiver, mh);
				if (!result.succeeded) {
					return result;
				}
				client = (UploadClient)result.userobject;
			} catch (Exception e) {
				e.printStackTrace();
				Result r = Result.error("ERROR: Upload of Feed failed.");
				r.exception = e;
				return r;
			}
		}
		
		if (client!=null) {
			try {
				Beamer beam = new Beamer();
				Result result = beam.uploadFeed(feed, client, signatureKey,mh, includeFiles, pg);
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
		Vector<BundleItemStructuredName> files = feed.getStructuredFilenames();
		Vector<String[]> sfiles = new Vector<String[]>();
		for (BundleItemStructuredName f : files) {
			sfiles.add(new String[] {f.file.getAbsolutePath(), f.new_filename});
		}	
		return sfiles;
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
		
	
	
	private long timeoutDuration = 4000;
	private Vector<RemoteFile> list = null;
	private boolean hasAnswer = false;
	
	//private Result currentUploadResult = null; 
	public Result uploadFeed(Feed currentFeed, UploadClient client, OSDXKey signaturekey, final MessageHandler mh, final boolean includeFiles, final ProgressListener pg) throws Exception {
		final String normFeedid = currentFeed.getNormFeedID();
		
		final Result[] result = new Result[] {Result.succeeded()};
		
		final boolean[] ready = new boolean[]{false};
		
		
		//make a copy to remove private information and change to relative file paths  
		//Feed copyOfFeed = Feed.fromBusinessObject(BusinessObject.fromElement(feed.toElement()));
		//copy feed and set structeredNames
		Feed feed = Feed.fromBusinessObject(BusinessObject.fromElement(currentFeed.toElement(true)));
		
		//check include files
		int bundleCount = feed.getBundleCount();
		for (int i=0;i<bundleCount;i++) { // for each bundle
			Bundle b = feed.getBundle(i);
			//bundle files
			int bundlefilesCount = b.getFilesCount();
			for (int k=0;k<bundlefilesCount;k++) { // for each bundle file
				ItemFile itf = b.getFile(k);
				itf.no_file_given(!includeFiles); //set no_file_given if not includeFiles
			}
			//item files
			int itemCount = b.getItemsCount();
			for (int j=0;j<itemCount;j++) {// for each item in bundle
				Item it = b.getItem(j);
				int filesCount = it.getFilesCount();
				for (int k=0;k<filesCount;k++) {// for each file in item
					ItemFile itf = it.getFile(k);
					itf.no_file_given(!includeFiles); //set no_file_given if not includeFiles
				}
			}
		}
		
		
//		//remove private info
//		try {
//			copyOfFeed.getFeedinfo().getReceiver().file_keystore(null);
//		} catch (Exception ex) {}
		
		//directory for date
		//String datedir = Util.filterCharactersFile(SecurityHelper.getFormattedDateDay(System.currentTimeMillis()));
		
		//norm feedid
		String dir = normFeedid;
		//String dir = normFeedid+"_"+SecurityHelper.getFormattedDate(System.currentTimeMillis()).substring(0,19).replace(' ', '_');
		//dir = Util.filterCharactersFile(dir);
		
		
		//test if dir already exists
		list = null;
		boolean exists = false;
		if (client instanceof FTPClient) {
			list = ((FTPClient)client).list();
		}
		else if (client instanceof OSDXFileTransferClient) {	
			hasAnswer = false;
			((OSDXFileTransferClient)client).list("/",new CommandResponseListener() {
				public void onError(OSDXFileTransferCommand command, String msg) {
					//System.out.println("END OF LIST COMMAND :: ERROR");
					list = null;
					hasAnswer = true;
				}
				public void onStatusUpdate(OSDXFileTransferCommand command,long progress, long maxProgress, String msg) {}
				public void onSuccess(OSDXFileTransferCommand command) {
					//System.out.println("END OF LIST COMMAND :: SUCCESS");
					list = ((OSDXFileTransferListCommand)command).getList();
					hasAnswer = true;
				}
			});
			
			//block until answer or timeout
			long timeout = System.currentTimeMillis()+timeoutDuration;
			while (!hasAnswer && timeout > System.currentTimeMillis()) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (!hasAnswer) {
				return Result.error("Error: Timeout when requesting server.");
			} else {
				if (list==null) {
					return Result.error("Error: Server filelist request failed.");
				}
				for (RemoteFile rf : list) {
					if (rf.isDirectory() && rf.getName().equals(normFeedid)) {
						exists = true;
						break;
					}
				}
			}
			((OSDXFileTransferClient)client).list("/", new CommandResponseListener() {
				public void onSuccess(OSDXFileTransferCommand command) {
					
				}
				
				public void onStatusUpdate(OSDXFileTransferCommand command, long progress,long maxProgress, String msg) {}
				public void onError(OSDXFileTransferCommand command, String msg) {
					
				}
			});
		}
		
		if (exists) {
			return Result.error("A feed with this id already exists on the server.\nPlease select another feedid.");
		}
		
		//build file structure
		if (client instanceof FTPClient) {
		//	((FTPClient)client).mkdir(datedir);
		//	((FTPClient)client).cd(datedir);
			((FTPClient)client).mkdir(dir);
			((FTPClient)client).cd(dir);
		}
		
		//String absolutePath = "/"+datedir+"/"+dir+"/";
		String absolutePath = "/"+dir+"/";
		
		long maxProgress = 0;
		long progress = 0;
		//upload feed
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		//Element eFeed = copyOfFeed.toElement();
		Element eFeed = feed.toElement(true);
		Document.buildDocument(eFeed).output(bOut);
		byte[] feedbytes = bOut.toByteArray();
		maxProgress += feedbytes.length;
		
		try {
			System.out.println("\nUploading "+normFeedid+".xml ...");
			hasAnswer = false;
			final long[] timeout = new long[]{System.currentTimeMillis()+timeoutDuration};
			client.uploadFile(feedbytes, absolutePath+normFeedid+".xml", new CommandResponseListener() {
				public void onSuccess(OSDXFileTransferCommand command) {
					System.out.println("Upload of "+normFeedid+".xml successful.");
					hasAnswer = true;
				}
				
				public void onStatusUpdate(OSDXFileTransferCommand command, long progress, long maxProgress, String msg) {
					timeout[0] = System.currentTimeMillis()+timeoutDuration;
				}
				public void onError(OSDXFileTransferCommand command, String msg) {
					if (mh!=null) {
						mh.showErrorMessage("Upload failed", "Upload of feed xml failed.");
					}
					result[0] = Result.error("Upload of feed xml failed.");
					hasAnswer = true;
				}
			});
			
			//block until answer or timeout
			while (!hasAnswer && timeout[0] > System.currentTimeMillis()) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (!hasAnswer) {
				if (mh!=null) {
					mh.showErrorMessage("Upload failed", "Upload of feed xml failed.");
				}
				result[0] = Result.error("Upload of feed xml failed. Timeout.");
				return result[0];
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			if (mh!=null) {
				mh.showErrorMessage("Upload failed", "Upload of feed xml failed.");
			}
			result[0] = Result.error("Upload of feed xml failed.");
		}
		
		//upload feed signature
		byte[][] checks  = SecurityHelper.getMD5SHA1SHA256(feedbytes);
		Signature feed_sig = Signature.createSignature(checks[1], checks[2], checks[3], normFeedid+".xml",signaturekey);
		bOut = new ByteArrayOutputStream();
		Document.buildDocument(feed_sig.toElement()).output(bOut);
		byte[] feedSignatureBytes = bOut.toByteArray();
		maxProgress += feedSignatureBytes.length;
		try {
			System.out.println("\nUploading signature ...");
			hasAnswer = false;
			final long[] timeout = new long[]{System.currentTimeMillis()+timeoutDuration};
			client.uploadFile(feedSignatureBytes, absolutePath+normFeedid+".xml.osdx.sig", new CommandResponseListener() {
				public void onSuccess(OSDXFileTransferCommand command) {
					System.out.println("Upload of signature successful.");
					hasAnswer = true;
				}
				
				public void onStatusUpdate(OSDXFileTransferCommand command, long progress, long maxProgress, String msg) {
					timeout[0] = System.currentTimeMillis()+timeoutDuration;
				}
				public void onError(OSDXFileTransferCommand command, String msg) {
					if (mh!=null) {
						mh.showErrorMessage("Upload failed", "Upload of signature failed.");
					}
					result[0] = Result.error("Upload of signature failed.");
					hasAnswer = true;
				}
			});
			
			//block until answer or timeout
			while (!hasAnswer && timeout[0] > System.currentTimeMillis()) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (!hasAnswer) {
				if (mh!=null) {
					mh.showErrorMessage("Upload failed", "Upload of feed signature xml failed. Timeout.");
				}
				result[0] = Result.error("Upload of feed signature xml failed. Timeout.");
				return result[0];
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			if (mh!=null) {
				mh.showErrorMessage("Upload failed", "Upload of signature failed.");
			}
			result[0] = Result.error("Upload of signature failed.");
		}
		
		
		
		//upload all bundle and item files
		if (includeFiles) {
			Vector<BundleItemStructuredName> files = feed.getStructuredFilenames();
			
			if (pg!=null) { // count max progress
				progress = maxProgress;
				pg.setProgress(progress);
				for (BundleItemStructuredName f : files) {
					maxProgress += f.file.length();
				}
				pg.setMaxProgress(maxProgress);
				pg.onUpate();
			}
			final long[] progressSave = new long[1];
			for (BundleItemStructuredName f : files) {
				try {
					//ItemFile nextItemFile = f.itemFile;
					//nextItemFile.setLocation(FileLocation.make(filename)); //relative filename to location path
					
					File nextFile = f.file;
					final String filename = f.new_filename;
					final long len = f.file.length();
					try {
						final long[] timeout = new long[]{System.currentTimeMillis()+timeoutDuration};
						hasAnswer = false;
						System.out.println("\nUploading "+filename+" ...");
						if (pg!=null) {
							progressSave[0] = pg.getProgress();
						}
						client.uploadFile(nextFile, absolutePath+filename, new CommandResponseListener() {
							public void onSuccess(OSDXFileTransferCommand command) {
								System.out.println("Upload of "+filename+" successful.");
								hasAnswer = true;
								if (pg!=null) {
									pg.setProgress(progressSave[0]+len);
									pg.onUpate();
								}
							}
							
							public void onStatusUpdate(OSDXFileTransferCommand command, long progress, long maxProgress, String msg) {
								//System.out.println("status update: "+progress+" / "+maxProgress);
								timeout[0] = System.currentTimeMillis()+timeoutDuration;
								if (pg!=null) {
									pg.setProgress(progressSave[0]+progress);
									pg.onUpate();
								}
							}
							public void onError(OSDXFileTransferCommand command, String msg) {
								if (mh!=null) {
									mh.showErrorMessage("Upload failed", "Upload of "+filename+" failed.");
								}
								result[0] = Result.error("Upload of "+filename+" failed.");
								hasAnswer = true;
							}
						});
						
						//block until answer or timeout
						while (!hasAnswer && timeout[0] > System.currentTimeMillis()) {
							try {
								Thread.sleep(50);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						if (!hasAnswer) {
							if (mh!=null) {
								mh.showErrorMessage("Upload failed", "Upload of "+filename+" failed. Timeout.");
							}
							result[0] = Result.error("Upload of "+filename+" failed. Timeout.");
							return result[0];
						}
					} catch (Exception ex) {
						ex.printStackTrace();
						if (mh!=null) {
							mh.showErrorMessage("Upload failed", "Upload of "+filename+" failed.");
						}
						result[0] = Result.error("Upload of "+filename+" failed.");
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		
		//upload feed finished file
		try {
			System.out.println("\nUploading finish token ...");
			hasAnswer = false;
			client.uploadFile(new byte[]{0},absolutePath+normFeedid+".finished", new CommandResponseListener() {
				public void onSuccess(OSDXFileTransferCommand command) {
					System.out.println("Upload of finish token successful.");
					hasAnswer = true;
				}
				
				public void onStatusUpdate(OSDXFileTransferCommand command, long progress, long maxProgress, String msg) {
					
				}
				public void onError(OSDXFileTransferCommand command, String msg) {
					if (mh!=null) {
						mh.showErrorMessage("Upload failed", "Upload of finish token failed.");
					}
					result[0] = Result.error("Upload of finish token failed.");
					hasAnswer = true;
				}
			});
			
			//block until answer or timeout
			long timeout = System.currentTimeMillis()+timeoutDuration;
			while (!hasAnswer && timeout > System.currentTimeMillis()) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (!hasAnswer) {
				if (mh!=null) {
					mh.showErrorMessage("Upload failed", "Upload of finish token failed. Timeout.");
				}
				result[0] = Result.error("Upload of finish token failed. Timeout.");
				return result[0];
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			if (mh!=null) {
				mh.showErrorMessage("Upload failed", "Upload of finish token failed.");
			}
			result[0] = Result.error("Upload of finish token failed.");
		}
		
		
		return result[0];
	}
	

	public static Result exportFeedToDirectory(Feed currentFeed, File targetDir, OSDXKey signaturekey, boolean includeFiles) throws Exception {
		if (targetDir==null) {
			return Result.error("Missing target directory for feed output.");
		}
		final String normFeedid = currentFeed.getNormFeedID();		
		final Result result = Result.succeeded();

		//test if dir already exists
		File path = new File(targetDir, normFeedid);
		 
		if (path.exists()) {
			return Result.error("A feed with this id already exists in given directory.\nPlease select another feedid.");
		}
		path.mkdirs();
		
		//copy feed and set structeredNames
		Feed feed = Feed.fromBusinessObject(BusinessObject.fromElement(currentFeed.toElement(true)));
		
		//check include files
		int bundleCount = feed.getBundleCount();
		for (int i=0;i<bundleCount;i++) { // for each bundle
			Bundle b = feed.getBundle(i);
			//bundle files
			int bundlefilesCount = b.getFilesCount();
			for (int k=0;k<bundlefilesCount;k++) { // for each bundle file
				ItemFile itf = b.getFile(k);
				itf.no_file_given(!includeFiles); //set no_file_given if not includeFiles
			}
			//item files
			int itemCount = b.getItemsCount();
			for (int j=0;j<itemCount;j++) {// for each item in bundle
				Item it = b.getItem(j);
				int filesCount = it.getFilesCount();
				for (int k=0;k<filesCount;k++) {// for each file in item
					ItemFile itf = it.getFile(k);
					itf.no_file_given(!includeFiles); //set no_file_given if not includeFiles
				}
			}
		}
		
		
		
		//save feed
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		Element eFeed = feed.toElement();
		
		File feedFile = new File(path, normFeedid+".xml");
		Document.buildDocument(eFeed).writeToFile(feedFile);
		
		//save feed signature
		byte[][] checks  = SecurityHelper.getMD5SHA1SHA256(feedFile);
		Signature feed_sig = Signature.createSignature(checks[1], checks[2], checks[3], normFeedid+".xml",signaturekey);
		File feedSigFile = new File(path, normFeedid+".xml.osdx.sig");
		Document.buildDocument(feed_sig.toElement()).writeToFile(feedSigFile);
		
		
		//copy all bundle and item files
		if (includeFiles) {
			Vector<BundleItemStructuredName> files = feed.getStructuredFilenames();
			for (BundleItemStructuredName f : files) {
				try {
					File nextFileSrc = f.file;
					File nextFileDest = new File(path,f.new_filename);
					Helper.copy(nextFileSrc, nextFileDest);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		
		//save feed finished file
		try {
			File finishFile = new File(path,normFeedid+".finish");
			finishFile.createNewFile();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return result;
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

	
	private static Result initOSDXFileTransferClient(Receiver r, MessageHandler mh, String keystore, OSDXKey key) {
		
		String servername = r.getServername();
		int port = r.getPort();
		if (port<=0) port = 4221;
		String prepath = r.getPrepath();
		String username = r.getUsername();
		if (username==null || username.length()==0) {
			return Result.error("Missing parameter: username"); 
		}
		
		File f = null;
		
		String keyid = r.getKeyID();
		
		OSDXKey mysigning = null;
		if (key!=null && keyid.equals(key.getKeyID())) {
			mysigning = key;
		}
		
		if (mysigning==null) { // get key from keystore
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
				
				if (keyid!=null) {
					mysigning = store.getKey(keyid);
					while (mysigning==null) {
						String msg = "You given key id \""+keyid+"\"\nfor authentification could not be found in default keystore.\nPlease select the corresponding keystore.";
						mh.showErrorMessage("Key not found.", msg);
						f = mh.requestOpenKeystore();
						if (f==null || !f.exists()) {
							return Result.error("Key for authentification could not be found.");	
						}
						store = KeyApprovingStore.fromFile(f, mh2);
						mysigning = store.getKey(keyid);
					}
				}
				
			} catch (Exception e1) {
				Result.error("Error opening keystore:\n"+f.getAbsolutePath());
		}
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
				client = FTPClient.connect(host, username, String.valueOf(password));
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
	
	
	
//	public static void main(String[] args) {
//		// init example feed
//		Feed currentFeed = FeedCreator.makeExampleFeed();
//		long now = System.currentTimeMillis();
//		
//		Receiver receiver = Receiver.make(Receiver.TRANSFER_TYPE_OSDX_FILESERVER)
//			.servername("localhost")
//			.serveripv4("127.0.0.1")
//			.authtype(Receiver.AUTH_TYPE_KEYFILE)
//		//	.file_keystore("/home/neo/openSDX/defaultKeyStore.xml")
//			.keyid("AF:08:7F:7E:92:D8:48:98:24:7B:56:00:71:F8:47:65:62:8A:46:EA@localhost")
//			.username("user_1");
//		
//		String defaultKeystore = "/home/neo/openSDX/defaultKeyStore.xml";
//		
//		Receiver receiver2 = Receiver.make(Receiver.TRANSFER_TYPE_FTP)
//		.servername("it-is-awesome.de")
//		.authtype(Receiver.AUTH_TYPE_LOGIN)
//		.username("baumbach");
//		
//		
//		currentFeed.getFeedinfo().receiver(receiver2);
//		
//		currentFeed.getBundle(0).addItem(
//				Item.make(IDs.make().amzn("item1 id"), "testitem1", "testitem", "v0.1", "video", "display artist",
//						BundleInformation.make(now,now), LicenseBasis.makeAsOnBundle(),null)
//						.addFile(ItemFile.make(new File("fnppl_contributor_license.pdf")))
//					.tags(ItemTags.make()
//						.addGenre("Rock")
//					)
//						
//		);
//		currentFeed.getBundle(0).getLicense_basis().getTerritorial()
//		.allow("DE")
//		.allow("GB")
//		.disallow("US");
//
//		OSDXKey signKey = null;
//		Result sk = selectPrivateSigningKey("/home/neo/openSDX/defaultKeyStore.xml", new DefaultMessageHandler());
//		if (sk.succeeded) signKey = (OSDXKey)sk.userobject;
//		
//		Result result = beamUpFeed(currentFeed, signKey, new DefaultMessageHandler(), defaultKeystore);
//		
//		if (result.succeeded) {
//			System.out.println("beaming successful");
//		} else {
//			System.out.println("error in beaming: "+result.errorMessage);
//		}
//	}
}

