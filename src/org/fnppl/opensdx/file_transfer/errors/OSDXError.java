package org.fnppl.opensdx.file_transfer.errors;

import org.fnppl.opensdx.file_transfer.SecureConnection;

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

/**
 * Represents an OSDX Error.<br>
 * All ERROR_CODES, the server is returning are collected in the OSDXErrorCodes class.
 * 
 * @author Aleksandar Jovanovic
 * @date 21.02.2013
 * @see SecureConnection
 * @see OSDXErrorCode
 */
public class OSDXError {
	
	private long id;
	private int num;
	private String message;
	private OSDXErrorCode type;
	
	public long getId() {
		return id;
	}

	public int getNum() {
		return num;
	}

	public String getMessage() {
		return message;
	}

	public OSDXErrorCode getType() {
		return type;
	}

	/**
	 * This constructor is used to generate an OSDXError within an SecureConenction context.<br>
	 * That means those kinds of errors are only generated in the SecureConnection class.<br>
	 * 
	 * @param id commandId
	 * @param num
	 * @param message Errormessage
	 * @param type Errortype
	 */
	public OSDXError(long id, int num, String message, OSDXErrorCode type){
		this.id = id;
		this.num = num;
		this.message = message;
		this.type = type;
	};
	
	/**
	 * This constructor is used to generate an OSDXError without context to a specific class.<br>
	 * That means those kind of errors contains just the error Type and Errormessage and can be generated from everywhere.<br>
	 * <br>
	 * id, num are initialized with -1 by default
	 * 
	 * @param message Errormessage
	 * @param type Errortype
	 */
	public OSDXError(String message, OSDXErrorCode type){
		this.id = -1;
		this.num = -1;
		this.message = message;
		this.type = type;
	};
	
	/**
	 * This constructor is used to generate an OSDXError without context to a specific class.<br>
	 * That means those kind of errors contains just the errormessage.<br>
	 * <br>
	 * id, num are initialized with -1 by default.<br>
	 * type is initialized with SecureConnection.TYPE_ERROR which means "Error with message"
	 * 
	 * @param message
	 */
	public OSDXError(String message){
		this.id = -1;
		this.num = -1;
		this.message = message;
		this.type = OSDXErrorCode.ERROR_WITH_MESSAGE; //Error with errormessage, nothing else
	}
	
	/**
	 * Throws an OSDXError depending on the error type
	 */
	public void throwException() throws OSDXException{
		switch(type){
		case ERROR_CANNOT_DELETE_DIR:
			break;
		case ERROR_CANNOT_DELETE_FILE:
			break;
		case ERROR_CANNOT_RENAME:
			break;
		case ERROR_DIRECTORY_DEPTH:
			break;
		case ERROR_DIRECTORY_DOWNLOAD_NOT_IMPLEMENTED:
			break;
		case ERROR_DIRECTORY_NOT_EXISTS:
			break;
		case ERROR_FILENAME_IS_MISSING:
			break;
		case ERROR_FILE_ALREADY_EXISTS:
			break;
		case ERROR_FILE_LENGTH_PARAM:
			break;
		case ERROR_FILE_NOT_EXISTS:
			break;
		case ERROR_FILE_RESTRICTED:
			break;
		case ERROR_LOGIN_ACCESS_DENIED:
			break;
		case ERROR_LOGIN_USERNAME_MISSING:
			break;
		case ERROR_MD5_CHECK:
			break;
		case ERROR_MKDIR:
			break;
		case ERROR_NOT_A_DIRECTORY:
			break;
		case ERROR_PATH_ALREADY_EXISTS:
			break;
		case ERROR_PATH_IS_MISSING:
			break;
		case ERROR_PATH_IS_NOT_ABSOLUTE:
			break;
		case ERROR_PATH_IS_RESTRICTED:
			break;
		case ERROR_RETRIEVING_FILE_INFO:
			break;
		case ERROR_RIGHTS_AND_DUTIES:
			break;
		case ERROR_UPLOAD_CANCEL:
			break;
		case ERROR_UPLOAD_HALT:
			break;
		case ERROR_UPLOAD_IS_NULL:
			break;
		case ERROR_WRONG_DESTINATION:
			break;
		case ERROR_WRONG_FILESIZE:
			break;
		case ERROR_WITH_MESSAGE:
		default:
			break;
		}
	}
}
