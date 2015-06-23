package org.fnppl.opensdx.file_transfer.errors;

import org.fnppl.opensdx.file_transfer.SecureConnection;
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

/**
 * This Class is an Enum of all ErrorCodes.<br>
 * OSDXErrorCode also priovides a varitie of methods to convert different error codes.
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
	
	/**
	 * Konverts a Byte error code into an OSDXErrorCode.<br>
	 * Most of the codes are from the SecureConnection class.
	 * 
	 * @param code the byte error code 
	 * @return OSDXErrorCode
	 * @see SecureConnection
	 */
	public static OSDXErrorCode byteToOSDXErrorCode(byte code){
		switch (code){
			//FILE ERROS
			case SecureConnection.ERROR_FILE_RESTRICTED: return ERROR_FILE_RESTRICTED;
			case SecureConnection.ERROR_FILE_NOT_EXISTS: return ERROR_FILE_NOT_EXISTS;
			case SecureConnection.ERROR_FILE_ALREADY_EXISTS: return ERROR_FILE_ALREADY_EXISTS;
			case SecureConnection.ERROR_FILENAME_IS_MISSING: return ERROR_FILENAME_IS_MISSING;
			case SecureConnection.ERROR_FILE_LENGTH_PARAM: return ERROR_FILE_LENGTH_PARAM;
			case SecureConnection.ERROR_CANNOT_DELETE_FILE: return ERROR_CANNOT_DELETE_FILE;
			case SecureConnection.ERROR_RETRIEVING_FILE_INFO: return ERROR_RETRIEVING_FILE_INFO;
			case SecureConnection.ERROR_WRONG_FILESIZE: return ERROR_WRONG_FILESIZE;
			
			//DIRECTORY SecureConnection.ERRORS
			case SecureConnection.ERROR_CANNOT_DELETE_DIR: return ERROR_CANNOT_DELETE_DIR;
			case SecureConnection.ERROR_DIRECTORY_NOT_EXISTS: return ERROR_DIRECTORY_NOT_EXISTS;
			case SecureConnection.ERROR_DIRECTORY_DEPTH: return ERROR_DIRECTORY_DEPTH;
			case SecureConnection.ERROR_DIRECTORY_DOWNLOAD_NOT_IMPLEMENTED : return ERROR_DIRECTORY_DOWNLOAD_NOT_IMPLEMENTED;
			case SecureConnection.ERROR_NOT_A_DIRECTORY: return ERROR_NOT_A_DIRECTORY;
			
			//LOGIN SecureConnection.ERRORS
			case SecureConnection.ERROR_LOGIN_ACCESS_DENIED: return ERROR_LOGIN_ACCESS_DENIED;
			case SecureConnection.ERROR_LOGIN_USERNAME_MISSING: return ERROR_LOGIN_USERNAME_MISSING;
			
			//UPLOAD SecureConnection.ERRORS
			case SecureConnection.ERROR_UPLOAD_IS_NULL: return ERROR_UPLOAD_IS_NULL;
			case SecureConnection.ERROR_UPLOAD_CANCEL: return ERROR_UPLOAD_CANCEL;
			case SecureConnection.ERROR_UPLOAD_HALT: return ERROR_UPLOAD_HALT;
			
			//FILESYSTEM SecureConnection.ERRORS
			case SecureConnection.ERROR_PATH_IS_NOT_ABSOLUTE: return ERROR_PATH_IS_NOT_ABSOLUTE;
			case SecureConnection.ERROR_PATH_IS_MISSING: return ERROR_PATH_IS_MISSING;
			case SecureConnection.ERROR_WRONG_DESTINATION: return ERROR_WRONG_DESTINATION;
			case SecureConnection.ERROR_CANNOT_RENAME: return ERROR_CANNOT_RENAME;
			case SecureConnection.ERROR_PATH_IS_RESTRICTED: return ERROR_PATH_IS_RESTRICTED;
			case SecureConnection.ERROR_PATH_ALREADY_EXISTS: return ERROR_PATH_ALREADY_EXISTS;
			case SecureConnection.ERROR_MKDIR: return ERROR_MKDIR;
			
			//OTHER SecureConnection.ERRORS
			case SecureConnection.ERROR_MD5_CHECK : return ERROR_MD5_CHECK;
			case SecureConnection.ERROR_RIGHTS_AND_DUTIES: return ERROR_RIGHTS_AND_DUTIES;
			default: return ERROR_WITH_MESSAGE;
		}
	}
	
	/**
	 * Konverts a OSDXError code into a byte code.
	 * 
	 * @param code the OSDXErrorCode
	 * @return byte error code
	 * @see SecureConnection
	 */
	public static byte OSDXErrorCodeToByte(OSDXErrorCode code){
		switch (code){
			//FILE ERROS
			case ERROR_FILE_RESTRICTED: return SecureConnection.ERROR_FILE_RESTRICTED;
			case ERROR_FILE_NOT_EXISTS: return SecureConnection.ERROR_FILE_NOT_EXISTS; 
			case ERROR_FILE_ALREADY_EXISTS: return SecureConnection.ERROR_FILE_ALREADY_EXISTS; 
			case ERROR_FILENAME_IS_MISSING: return SecureConnection.ERROR_FILENAME_IS_MISSING; 
			case ERROR_FILE_LENGTH_PARAM: return SecureConnection.ERROR_FILE_LENGTH_PARAM;
			case ERROR_CANNOT_DELETE_FILE: return SecureConnection.ERROR_CANNOT_DELETE_FILE; 
			case ERROR_RETRIEVING_FILE_INFO: return SecureConnection.ERROR_RETRIEVING_FILE_INFO; 
			case ERROR_WRONG_FILESIZE: return SecureConnection.ERROR_WRONG_FILESIZE; 
			
			//DIRECTORY SecureConnection.ERRORS
			case ERROR_CANNOT_DELETE_DIR: return SecureConnection.ERROR_CANNOT_DELETE_DIR;
			case ERROR_DIRECTORY_NOT_EXISTS: return SecureConnection.ERROR_DIRECTORY_NOT_EXISTS;
			case ERROR_DIRECTORY_DEPTH: return SecureConnection.ERROR_DIRECTORY_DEPTH;
			case ERROR_DIRECTORY_DOWNLOAD_NOT_IMPLEMENTED: return SecureConnection.ERROR_DIRECTORY_DOWNLOAD_NOT_IMPLEMENTED;
			case ERROR_NOT_A_DIRECTORY: return SecureConnection.ERROR_NOT_A_DIRECTORY;
			
			//LOGIN SecureConnection.ERRORS
			case ERROR_LOGIN_ACCESS_DENIED: return SecureConnection.ERROR_LOGIN_ACCESS_DENIED;
			case ERROR_LOGIN_USERNAME_MISSING: return SecureConnection.ERROR_LOGIN_USERNAME_MISSING;
			
			//UPLOAD SecureConnection.ERRORS
			case ERROR_UPLOAD_IS_NULL: return SecureConnection.ERROR_UPLOAD_IS_NULL;
			case ERROR_UPLOAD_CANCEL: return SecureConnection.ERROR_UPLOAD_CANCEL;
			case ERROR_UPLOAD_HALT: return SecureConnection.ERROR_UPLOAD_HALT;
			
			//FILESYSTEM SecureConnection.ERRORS
			case ERROR_PATH_IS_NOT_ABSOLUTE: return SecureConnection.ERROR_PATH_IS_NOT_ABSOLUTE;
			case ERROR_PATH_IS_MISSING: return SecureConnection.ERROR_PATH_IS_MISSING;
			case ERROR_WRONG_DESTINATION: return SecureConnection.ERROR_WRONG_DESTINATION;
			case ERROR_CANNOT_RENAME: return SecureConnection.ERROR_CANNOT_RENAME;
			case ERROR_PATH_IS_RESTRICTED: return SecureConnection.ERROR_PATH_IS_RESTRICTED;
			case ERROR_PATH_ALREADY_EXISTS: return SecureConnection.ERROR_PATH_ALREADY_EXISTS;
			case ERROR_MKDIR: return SecureConnection.ERROR_MKDIR;
			
			//OTHER SecureConnection.ERRORS
			case ERROR_MD5_CHECK: return SecureConnection.ERROR_MD5_CHECK ;
			case ERROR_RIGHTS_AND_DUTIES: return SecureConnection.ERROR_RIGHTS_AND_DUTIES;
			default: return SecureConnection.ERROR_WITH_MESSAGE;
		}
	}
	
	/**
	 * Throws an OSDXError depending on the error type
	 */
	public void throwException(String msg) throws OSDXException{
		switch(this){
			case ERROR_CANNOT_DELETE_DIR: throw new OSDXException().new DirectoryDeletionException(msg);
			case ERROR_CANNOT_DELETE_FILE: throw new OSDXException().new FileDeletionException(msg);
			case ERROR_CANNOT_RENAME: throw new OSDXException().new RenameException(msg);
			case ERROR_DIRECTORY_DEPTH: throw new OSDXException().new DirectoryDepthException(msg);
			case ERROR_DIRECTORY_DOWNLOAD_NOT_IMPLEMENTED: throw new OSDXException().new DirectoryDownloadException(msg);
			case ERROR_DIRECTORY_NOT_EXISTS: throw new OSDXException().new DirectoryDoesNotExistException(msg);
			case ERROR_FILENAME_IS_MISSING: throw new OSDXException().new FileNameIsMissingException(msg);
			case ERROR_FILE_ALREADY_EXISTS: throw new OSDXException().new FileAlreadyExistsException(msg);
			case ERROR_FILE_LENGTH_PARAM: throw new OSDXException().new FileFileLengthParamException(msg);
			case ERROR_FILE_NOT_EXISTS: throw new OSDXException().new FileDoesNotExistException(msg);
			case ERROR_FILE_RESTRICTED: throw new OSDXException().new FileRestrictedException(msg);
			case ERROR_LOGIN_ACCESS_DENIED: throw new OSDXException().new LoginAccesDeniedException(msg);
			case ERROR_LOGIN_USERNAME_MISSING: throw new OSDXException().new LoginUsernaneIsMissingException(msg);
			case ERROR_MD5_CHECK: throw new OSDXException().new MD5CheckException(msg);
			case ERROR_MKDIR: throw new OSDXException().new MakeDirectoryException(msg);
			case ERROR_NOT_A_DIRECTORY: throw new OSDXException().new IsNotADirectoryException(msg);
			case ERROR_PATH_ALREADY_EXISTS: throw new OSDXException().new PathAlreadyExistsException(msg);
			case ERROR_PATH_IS_MISSING: throw new OSDXException().new PathIsMissingException(msg);
			case ERROR_PATH_IS_NOT_ABSOLUTE: throw new OSDXException().new PathIsNotAbsoluteException(msg);
			case ERROR_PATH_IS_RESTRICTED: throw new OSDXException().new RestrictedPathException(msg);
			case ERROR_RETRIEVING_FILE_INFO: throw new OSDXException().new FileInfoRetrievingException(msg);
			case ERROR_RIGHTS_AND_DUTIES: throw new OSDXException().new RightsAndDutiesException(msg);
			case ERROR_UPLOAD_CANCEL: throw new OSDXException().new UploadCancellationException(msg);
			case ERROR_UPLOAD_HALT: throw new OSDXException().new UploadHaltException(msg);
			case ERROR_UPLOAD_IS_NULL: throw new OSDXException().new UploadIsNullException(msg);
			case ERROR_WRONG_DESTINATION: throw new OSDXException().new WrongDestinationException(msg);
			case ERROR_WRONG_FILESIZE: throw new OSDXException().new FileSizeException(msg);
			default: throw new OSDXException(msg); //That pretty much represents ERROR_WITH_MESSAGE
		}
	}	
}
