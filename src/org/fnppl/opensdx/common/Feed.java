package org.fnppl.opensdx.common;

import java.io.File;
import java.util.Vector;

import org.fnppl.opensdx.dmi.BundleItemStructuredName;
import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.xml.ChildElementIterator;
import org.fnppl.opensdx.xml.Element;

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
	
	public String getNormFeedID() {
		String feedid = getFeedinfo().getFeedID();
		String normFeedid = Util.filterCharactersFile(feedid.toLowerCase());
		//System.out.println("norm feedid: "+normFeedid);
		if (normFeedid.length()==0) {
			normFeedid = "unnamed_feed";
		}
		return normFeedid;
	}
	
	public BundleItemStructuredName getStructuredFilename(ItemFile file) {
		String normFeedid = getNormFeedID();
		int num = 1;
		for (int b=0;b<getBundleCount();b++) {
			Bundle bundle = getBundle(b);
			if (bundle!=null) {
				//bundle files (cover, booklet, ..)
				for (int j=0;j<bundle.getFilesCount();j++) {
					try {
						ItemFile nextItemFile = bundle.getFile(j);
						if (nextItemFile==file) {
							File nextFile = new File(nextItemFile.getOriginLocationPath());
							String md5 = SecurityHelper.HexDecoder.encode(nextItemFile.getChecksums().getMd5(),'\0',-1);
							String filename = normFeedid+"_"+num+"_"+md5;
							return new BundleItemStructuredName(nextItemFile, nextFile, filename);
						}
						num++;
					} catch (Exception ex) {
						ex.printStackTrace();
						return null;
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
								if (nextItemFile==file) {
									String origFilename = nextItemFile.getOriginLocationPath();
									if (origFilename==null) {
										System.out.println("WARNING :: file not found: "+origFilename);
										return null;
									}
									File nextFile = new File(origFilename);
									if (!nextFile.exists()) {
										System.out.println("WARNING :: file not found: "+origFilename);
										return null;
									}
									String md5 = SecurityHelper.HexDecoder.encode(nextItemFile.getChecksums().getMd5(),'\0',-1);
									String filename = normFeedid+"_"+num+"_"+md5;
									return new BundleItemStructuredName(nextItemFile, nextFile, filename);
								}
								num++;
							} catch (Exception ex) {
								ex.printStackTrace();
								return null;
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	public String getStructuredFilenameWithoutFilecheck(ItemFile file) {
		String normFeedid = getNormFeedID();
		int num = 1;
		for (int b=0;b<getBundleCount();b++) {
			Bundle bundle = getBundle(b);
			if (bundle!=null) {
				//bundle files (cover, booklet, ..)
				for (int j=0;j<bundle.getFilesCount();j++) {
					try {
						ItemFile nextItemFile = bundle.getFile(j);
						if (nextItemFile==file) {
							String md5 = SecurityHelper.HexDecoder.encode(nextItemFile.getChecksums().getMd5(),'\0',-1);
							String filename = normFeedid+"_"+num+"_"+md5;
							return filename;
						}
						num++;
					} catch (Exception ex) {
						ex.printStackTrace();
						return null;
					}
				}
				
				//item files
				for (int i=0;i<bundle.getItemsCount();i++) {
					Item item = bundle.getItem(i);
					if (item.getFilesCount()>0) {
						for (int j=0;j<item.getFilesCount();j++) {
							try {
								ItemFile nextItemFile = item.getFile(j);
								if (nextItemFile==file) {
									String md5 = SecurityHelper.HexDecoder.encode(nextItemFile.getChecksums().getMd5(),'\0',-1);
									String filename = normFeedid+"_"+num+"_"+md5;
									return filename;
								}
								num++;
							} catch (Exception ex) {
								ex.printStackTrace();
								return null;
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	public Vector<BundleItemStructuredName> getStructuredFilenames() {
		Vector<BundleItemStructuredName> files = new Vector<BundleItemStructuredName>();
		
		String normFeedid = getNormFeedID();
		int num = 1;
		for (int b=0;b<getBundleCount();b++) {
			Bundle bundle = getBundle(b);
			if (bundle!=null) {
				//bundle files (cover, booklet, ..)
				for (int j=0;j<bundle.getFilesCount();j++) {
					try {
						ItemFile nextItemFile = bundle.getFile(j);
						String origFilename = nextItemFile.getOriginLocationPath();
						if (origFilename!=null) {
							File nextFile = new File(origFilename);
							if (nextFile.exists()) {
								String md5 = SecurityHelper.HexDecoder.encode(nextItemFile.getChecksums().getMd5(),'\0',-1);
								String filename = normFeedid+"_"+num+"_"+md5;
								files.add(new BundleItemStructuredName(nextItemFile, nextFile, filename));
							} else {
								System.out.println("WARNING :: file not found: "+origFilename);
							}
						} else {
							System.out.println("WARNING :: file not found: "+origFilename);
						}
						num++;
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				
				//item files
				for (int i=0;i<bundle.getItemsCount();i++) {
					Item item = bundle.getItem(i);
					if (item.getFilesCount()>0) {
						//boolean subIndex = (item.getFilesCount()>1);
						for (int j=0;j<item.getFilesCount();j++) {
							try {
								ItemFile nextItemFile = item.getFile(j);
								String origFilename = nextItemFile.getOriginLocationPath();
								if (origFilename!=null) {
									File nextFile = new File(origFilename);
									if (nextFile.exists()) {
										String md5 = SecurityHelper.HexDecoder.encode(nextItemFile.getChecksums().getMd5(),'\0',-1);
										String filename = normFeedid+"_"+num+"_"+md5;	
										files.add(new BundleItemStructuredName(nextItemFile, nextFile, filename));
									}
								}
								num++;
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
	
	public Element toElement() {
		return toElement(false);
	}
	public Element toElement(boolean doStructureFileNames) {
		if(doStructureFileNames) {
			Vector<BundleItemStructuredName> itemNames = getStructuredFilenames();
			for (BundleItemStructuredName sn : itemNames) {
				sn.itemFile.path(sn.new_filename);
			}
		}
		return super.toElement();
	}

}
