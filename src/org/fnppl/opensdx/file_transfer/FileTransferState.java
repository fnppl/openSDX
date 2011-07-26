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
import java.io.File;

public class FileTransferState {
	
	private File rootPath = null;
	private File currentPath = null;
	private File writeFile = null;
	
	
	
	public File getRootPath() {
		return rootPath;
	}

	public void setRootPath(File rootPath) {
		this.rootPath = rootPath;
		this.currentPath = rootPath;
		rootPath.mkdirs();
	}

	public File getCurrentPath() {
		return currentPath;
	}

	public void setCurrentPath(File currentPath) {
		if (isAllowed(currentPath)) {
			this.currentPath = currentPath;	
		} else {
			System.out.println("ALERT ::  TRYING TO SET PATH OUT OF ROOT DIRECTORY");
		}
	}

	public File getWriteFile() {
		return writeFile;
	}

	public void setWriteFile(File writeFile) {
		if (writeFile==null || isAllowed(writeFile)) {
			this.writeFile = writeFile;
		} else {
			System.out.println("ALERT :: TRYING TO SET WRITEFILE OUT OF ROOT DIRECTORY");
		}
	}
	
	public boolean isAllowed(File f) {
		try {
			if (f.getCanonicalPath().startsWith(rootPath.getAbsolutePath())) {
				return true;
			} else {
				return false;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public String getRelativPath() {
		return getRelativPath(currentPath);
	}
	
	public String getRelativPath(File f) {
		String rp = f.getAbsolutePath().substring(rootPath.getAbsolutePath().length());
		if (rp.length()==0) rp = "/";
		if (rp.startsWith("//")) rp = rp.substring(1);
		return rp;
	}
	
	public boolean cdup() {
		if (!currentPath.equals(rootPath)) {
			currentPath = currentPath.getParentFile();
			return true;
		}
		return false;
	}
}
