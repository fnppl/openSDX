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
	
	
	
//	public Result sendToReceiver(MessageHandler mh, OSDXKey signatureKey) {
//		
//	}
	
	
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
