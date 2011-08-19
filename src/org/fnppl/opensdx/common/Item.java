package org.fnppl.opensdx.common;

import java.util.Vector;

import org.fnppl.opensdx.xml.ChildElementIterator;
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

public class Item extends BusinessObject {
	public static String KEY_NAME = "item";
	
	private BusinessStringItem displayname;					//MUST
	private BusinessStringItem name;						//MUST
	private BusinessStringItem version;						//MUST
	private BusinessStringItem type;						//MUST
	private BusinessStringItem display_artistname;				//SHOULD
	private IDs ids;										//MUST
	private BusinessCollection<Contributor> contributors;	//MUST
	private BundleInformation information;					//MUST
	private LicenseBasis license_basis;						//MUST
	private LicenseSpecifics license_specifics;				//MUST
	private ItemTags tags;									//SHOULD
	private Fingerprint fingerprint;						//COULD
	private BusinessCollection<ItemFile> files;
	
	
	private Item() {
		
	}
	
	public static Item make(IDs ids, String displayname, String name, String version, String type, String display_artistname, BundleInformation information, LicenseBasis license_basis, LicenseSpecifics license_specifics) {
		Item item = new Item();
		item.ids = ids;
		item.displayname = new BusinessStringItem("displayname", displayname);
		item.name = new BusinessStringItem("name", name);
		item.version = new BusinessStringItem("version", version);
		item.type = new BusinessStringItem("type", type);
		item.display_artistname = new BusinessStringItem("display_artistname", display_artistname);
		item.contributors = new BusinessCollection<Contributor>() {
			public String getKeyname() {
				return "contributors";
			}
		};
		item.information = information;
		item.license_basis = license_basis;
		item.license_specifics = license_specifics;
		item.tags = null;
		item.files = null;
		return item;
	}
	
	public static Item fromBusinessObject(BusinessObject bo) {
		if (bo==null) return null;
		if (!bo.getKeyname().equals(KEY_NAME)) {
			bo = bo.handleBusinessObject(KEY_NAME);
		}
		if (bo==null) return null;
		final Item item = new Item();
		item.initFromBusinessObject(bo);
		
		item.ids = IDs.fromBusinessObject(bo);
		item.displayname = BusinessStringItem.fromBusinessObject(bo, "displayname");
		item.name = BusinessStringItem.fromBusinessObject(bo, "name");
		item.version = BusinessStringItem.fromBusinessObject(bo, "version");
		item.type = BusinessStringItem.fromBusinessObject(bo, "type");
		item.display_artistname = BusinessStringItem.fromBusinessObject(bo, "display_artistname");
		item.contributors =  new BusinessCollection<Contributor>() {
			public String getKeyname() {
				return "contributors";
			}
		};
		new ChildElementIterator(bo, "contributors","contributor") {
			public void processBusinessObject(BusinessObject bo) {
				item.addContributor(Contributor.fromBusinessObject(bo));
			}
		};
		
		
		item.information = BundleInformation.fromBusinessObject(bo);
		item.license_basis = LicenseBasis.fromBusinessObject(bo);
		item.license_specifics = LicenseSpecifics.fromBusinessObject(bo);
		item.files =  new BusinessCollection<ItemFile>() {
			public String getKeyname() {
				return "files";
			}
		};
		new ChildElementIterator(bo, "files","file") {
			public void processBusinessObject(BusinessObject bo) {
				item.addFile(ItemFile.fromBusinessObject(bo));
			}
		};
		item.tags = ItemTags.fromBusinessObject(bo);
		item.fingerprint = Fingerprint.fromBusinessObject(bo);
		
		return item;
	}
	
	public Item addContributor(Contributor contributor) {
		if (contributor == null) {
			return this;
		}
		contributor.setAttribute("num", ""+(contributors.size()+1));
		contributors.add(contributor);
		return this;
	}
	
	public void removeContributor(int index) {
		if (contributors==null) return;
		contributors.remove(index);
	}
	
	public void removeContributor(Contributor c) {
		if (contributors==null) return;
		if (c!=null) {
			int index = contributors.indexOf(c);
			if (index>0) contributors.remove(index);
		}
	}
	
	public int getContributorCount() {
		if (contributors==null) return 0;
		return contributors.size();
	}
	
	public Vector<Contributor> getAllContributors() {
		Vector<Contributor> all = new Vector<Contributor>();
		for (int i=0;i<getContributorCount();i++) {
			all.add(getContributor(i));
		}
		return all;
	}	
	
	public Item ids(IDs ids) {
		this.ids = ids;
		return this;
	}

	public Item displayname(String displayname) {
		this.displayname = new BusinessStringItem("displayname", displayname);
		return this;
	}

	public Item name(String name) {
		this.name = new BusinessStringItem("name", name);
		return this;
	}
	
	public Item type(String type) {
		this.type = new BusinessStringItem("type", type);
		return this;
	}

	public Item version(String version) {
		this.version = new BusinessStringItem("version", version);
		return this;
	}

	public Item display_artistname(String display_artistname) {
		this.display_artistname = new BusinessStringItem("display_artistname", display_artistname);
		return this;
	}

	public Item information(BundleInformation information) {
		this.information = information;
		return this;
	}

	public Item license_basis(LicenseBasis license_basis) {
		this.license_basis = license_basis;
		return this;
	}

	public Item license_specifics(LicenseSpecifics license_specifics) {
		this.license_specifics =license_specifics;
		return this;
	}


	public IDs getIds() {
		return ids;
	}

	public String getDisplayname() {
		if (displayname==null) return null;
		return displayname.getString();
	}

	public String getName() {
		if (name==null) return null;
		return name.getString();
	}

	public String getVersion() {
		if (version==null) return null;
		return version.getString();
	}
	
	public String getType() {
		if (type==null) return null;
		return type.getString();
	}

	public String getDisplay_artistname() {
		if (display_artistname==null) return null;
		return display_artistname.getString();
	}

	public Contributor getContributor(int index) {
		if (contributors==null) return null;
		if (index<0 || index>=contributors.size()) return null;
		return contributors.get(index);
	}

	public Item tags(ItemTags tags) {
		this.tags = tags;
		return this;
	}
	
	public ItemTags getTags() {
		return tags;
	}

	public BundleInformation getInformation() {
		if (information==null) return null;
		return information;
	}

	public LicenseBasis getLicense_basis() {
		return license_basis;
	}

	public LicenseSpecifics getLicense_specifics() {
		return license_specifics;
	}
	
	public Item addFile(ItemFile file) {
		if (files==null) {
			files = new BusinessCollection<ItemFile>() {
				public String getKeyname() {
					return "files";
				}
			};
		}
		files.add(file);
		return this;
	}
	
	public void removeFile(int index) {
		if (files==null) return;
		files.remove(index);
	}
	
	public int getFilesCount() {
		if (files==null) return 0;
		return files.size();
	}
	public ItemFile getFile(int index) {
		if (files==null) return null;
		if (index<0 || index>=files.size()) return null;
		return files.get(index);
	}
	
	
	public Item setEchoprint(String echoprint) {
		if (fingerprint==null) {
			fingerprint = Fingerprint.make();
		}
		fingerprint.echoprint(echoprint);
		return this;
	}
	
	public String getEchoprint() {
		if (fingerprint==null) {
			return null;
		}
		return fingerprint.getEchoprint();
	}
	
	public String getKeyname() {
		return KEY_NAME;
	}



}
