package org.fnppl.opensdx.file_transfer.errors;

import org.fnppl.opensdx.file_transfer.errors.exceptions.*;


/*
 * Copyright (C) 2010-2013 
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

/**
 * Provides most of the transfere errorss
 * 
 * @author Aleksandar Jovanovic
 * @date 19.02.2013
 */
public enum OSDXErrorCodes {
	DOWNLOAD_ERROR_WRONG_FILE_SIZE(0, "Error downloading \"%s\" :: wrong filesize"),
	DOWNLOAD_ERROR_MD5_CHECK_FAIL(1, "Error downloading \"%s\" :: MD5 check FAILD!"),
	UPLOAD_ERROR_MD5_CHECK_FAIL(2, "MD5 check failed for resuming upload"),
	UPLOAD_ERROR_WRONG_FORMAT(3, "wrong format: file upload resume position not parseable"),
	ERROR_FROM_CONTENT(4, "%s");
	
	//... more errors to come
	
	private String textinsert; //A simple idea to make filenames etc available in the Error enum Object
	private final int code;
	private final String description;
	
	private OSDXErrorCodes(int code, String description) {
		this.code = code;
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
	public int getCode() {
		return code;
	}
	
	public void setTextinsert(String textinsert){
		this.textinsert = textinsert;
	}
	
	public String getTextinsert(){
		return this.textinsert;
	}
	
	public void throwException() throws Exception{
		switch(this){
			case DOWNLOAD_ERROR_WRONG_FILE_SIZE : throw new WrongFileSizeException(DOWNLOAD_ERROR_WRONG_FILE_SIZE.getDescription());
			case DOWNLOAD_ERROR_MD5_CHECK_FAIL : throw new MD5DownloadException(DOWNLOAD_ERROR_MD5_CHECK_FAIL.getDescription());
			case UPLOAD_ERROR_MD5_CHECK_FAIL : throw new MD5CheckException(UPLOAD_ERROR_MD5_CHECK_FAIL.getDescription());
			case UPLOAD_ERROR_WRONG_FORMAT : throw new WrongFormatException(UPLOAD_ERROR_WRONG_FORMAT.getDescription());
			case ERROR_FROM_CONTENT : throw new ErrorFromContentException(ERROR_FROM_CONTENT.getDescription());
			default : throw new Exception("Undefined OSDXErrorCodes Exception");
		}
	}
	
	@Override
	public String toString() {
		return "[Error " + code + "] : " + description;
	}
}
