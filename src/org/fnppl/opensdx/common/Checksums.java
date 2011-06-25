package org.fnppl.opensdx.common;
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
public class Checksums extends BusinessObject {

	public static String KEY_NAME = "checksums";

	private BusinessBytesItem md5;		//COULD
	private BusinessBytesItem sha1;		//COULD
	private BusinessBytesItem sha256;	//COULD


	public static Checksums make(byte[] md5,byte[] sha1, byte[] sha256) {
		Checksums checksums = new Checksums();
		checksums.md5 = null;
		checksums.sha1 = null;
		checksums.sha256 = null;
		if (md5!=null)      checksums.md5    = new BusinessBytesItem("md5", md5);
		if (sha1!=null)  	checksums.sha1   = new BusinessBytesItem("sha1", sha1);
		if (sha256!=null)	checksums.sha256 = new BusinessBytesItem("sha256", sha256);
		
		return checksums;
	}


	public static Checksums make() {
		Checksums checksums = new Checksums();
		checksums.md5 = null;
		checksums.sha1 = null;
		checksums.sha256 = null;
		return checksums;
	}


	public static Checksums fromBusinessObject(BusinessObject bo) {
		if (bo==null) return null;
		if (!bo.getKeyname().equals(KEY_NAME)) {
			bo = bo.handleBusinessObject(KEY_NAME);
		}
		if (bo==null) return null;
		Checksums checksums = new Checksums();
		checksums.initFromBusinessObject(bo);
		
		checksums.md5 = BusinessBytesItem.fromBusinessObject(bo, "md5");
		checksums.sha1 = BusinessBytesItem.fromBusinessObject(bo, "sha1");
		checksums.sha256 = BusinessBytesItem.fromBusinessObject(bo, "sha256");
		
		return checksums;
	}


	public Checksums md5(byte[] md5) {
		this.md5 = new BusinessBytesItem("md5", md5);
		return this;
	}

	public Checksums sha1(byte[] sha1) {
		this.sha1 = new BusinessBytesItem("sha1", sha1);
		return this;
	}

	public Checksums sha256(byte[] sha256) {
		this.sha256 = new BusinessBytesItem("sha256", sha256);
		return this;
	}
	
	public byte[] getMd5() {
		if (md5==null) return null;
		return md5.getBytes();
	}

	public byte[] getSha1() {
		if (sha1==null) return null;
		return sha1.getBytes();
	}

	public byte[] getSha256() {
		if (sha256==null) return null;
		return sha256.getBytes();
	}


	public String getMd5String() {
		if (md5==null) return null;
		return md5.getString();
	}

	public String getSha1String() {
		if (sha1==null) return null;
		return sha1.getString();
	}

	public String getSha256String() {
		if (sha256==null) return null;
		return sha256.getString();
	}
	public String getKeyname() {
		return KEY_NAME;
	}
}
