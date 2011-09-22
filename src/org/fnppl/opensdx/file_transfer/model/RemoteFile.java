package org.fnppl.opensdx.file_transfer.model;

import org.fnppl.opensdx.common.Util;
import org.fnppl.opensdx.security.SecurityHelper;


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
public class RemoteFile {

	private String path;
	private String name;
	private long length;
	private long lastModified;
	private boolean directory;
	
	
	public RemoteFile(String path, String name, long length, long lastModified, boolean directory) {
		this.path = path;
		this.name = name;
		this.length = length;
		this.lastModified = lastModified;
		this.directory = directory;
	}

	public String getFilnameWithPath() {
		String filename = ""+path;
		if (!filename.endsWith("/")) filename += "/";
		if (!name.equals("/")) {
			filename += name;
		}
		return filename;
	}
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	public boolean isDirectory() {
		return directory;
	}
	public boolean isFile() {
		return !directory;
	}
	
	public String toString() {
		return String.format("%-20s | %8dkB  %-6s  %s",name,(length/1000),(directory?"[DIR]":"[FILE]"),SecurityHelper.getFormattedDate(lastModified));
	}
	
	public String toParamString() {
		String[] param = new String[5];
		param[0] = path;
		param[1] = name;
		param[2] = ""+length;
		param[3] = ""+lastModified;
		param[4] = ""+directory;
		return Util.makeParamsString(param);
	}
	
	public static RemoteFile fromParamString(String param) {
		try {
			String[] p = Util.getParams(param);
			System.out.println("FROM PARAM: "+param+"   dir="+p[4]+"|");
			RemoteFile rf = new RemoteFile(p[0], p[1], Long.parseLong(p[2]), Long.parseLong(p[3]), Boolean.parseBoolean(p[4]));
			return rf;
		} catch (Exception ex) {
			return null;
		}
	}
	
	
}
