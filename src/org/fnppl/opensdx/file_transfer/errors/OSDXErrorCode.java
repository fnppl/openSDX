package org.fnppl.opensdx.file_transfer.errors;

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
 * This Class is an Enum of all ErrorCodes 
 *
 * @author Aleksandar Jovanovic
 * @date 21.02.2013
 */
public enum OSDXErrorCode {
	//FILE ERROS
	 ERROR_FILE_RESTRICTED(500),
	 ERROR_FILE_NOT_EXISTS(501),
	 ERROR_FILE_ALREADY_EXISTS(502),
	 ERROR_FILENAME_IS_MISSING(503),
	 ERROR_FILE_LENGTH_PARAM(504),
	 ERROR_CANNOT_DELETE_FILE(505),
	 ERROR_RETRIEVING_FILE_INFO(506),
	 ERROR_WRONG_FILESIZE(507),
	
	//DIRECTORY ERRORS
	 ERROR_CANNOT_DELETE_DIR(600),
	 ERROR_DIRECTORY_NOT_EXISTS(601),
	 ERROR_DIRECTORY_DEPTH(602),
	 ERROR_DIRECTORY_DOWNLOAD_NOT_IMPLEMENTED (603),
	 ERROR_NOT_A_DIRECTORY(604),
	
	//LOGIN ERRORS
	 ERROR_LOGIN_ACCESS_DENIED(701),
	 ERROR_LOGIN_USERNAME_MISSING(702),
	
	//UPLOAD ERRORS
	 ERROR_UPLOAD_IS_NULL(801),
	 ERROR_UPLOAD_CANCEL(802),
	 ERROR_UPLOAD_HALT(803),
	
	//FILESYSTEM ERRORS
	 ERROR_PATH_IS_NOT_ABSOLUTE(900),
	 ERROR_PATH_IS_MISSING(901),
	 ERROR_WRONG_DESTINATION(902),
	 ERROR_CANNOT_RENAME(903),
	 ERROR_PATH_IS_RESTRICTED(904),
	 ERROR_PATH_ALREADY_EXISTS(905),
	 ERROR_MKDIR(906),
	
	//OTHER ERRORS
	 ERROR_WITH_MESSAGE (1000),
	 ERROR_MD5_CHECK (1001),
	 ERROR_RIGHTS_AND_DUTIES(1002);
	
	private final int errorCode;
	
	public int getErrorCode(){
		return this.errorCode;
	}
	
	private OSDXErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
}
