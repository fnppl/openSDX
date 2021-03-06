package org.fnppl.opensdx.common;
/*
 * Copyright (C) 2010-2015 
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
public class FileLocation extends BusinessObject {

	public static String KEY_NAME = "location";

	
	private BusinessStringItem path;			//COULD
	private BusinessStringItem origin_file;		//COULD

	public static FileLocation make(String path, String origin_file) {
		FileLocation location = new FileLocation();
		 location.origin_file = origin_file==null?null:new BusinessStringItem("origin_file", origin_file);
		 location.path = path==null?null:new BusinessStringItem("path", path);
		return location;
	}

	public static FileLocation make() {
		FileLocation location = new FileLocation();
		location.origin_file = null;
		location.path = null;
		return location;
	}


	public static FileLocation fromBusinessObject(BusinessObject bo) {
		if (bo==null) return null;
		if (!bo.getKeyname().equals(KEY_NAME)) {
			bo = bo.handleBusinessObject(KEY_NAME);
		}
		if (bo==null) return null;
		FileLocation location = new FileLocation();
		location.initFromBusinessObject(bo);
		
		location.origin_file = BusinessStringItem.fromBusinessObject(bo, "origin_file");
		location.path = BusinessStringItem.fromBusinessObject(bo, "path");
		
		return location;
	}


	public FileLocation file_origin(String file_origin) {
		this.origin_file = new BusinessStringItem("origin_file", file_origin);
		return this;
	}

	public FileLocation path(String path) {
		this.path = new BusinessStringItem("path", path);
		return this;
	}


	public String getOriginFile() {
		if (origin_file==null) return null;
		return origin_file.getString();
	}
	
	public String getPath() {
		if (path==null) return null;
		return path.getString();
	}
	
	public String getKeyname() {
		return KEY_NAME;
	}
}
