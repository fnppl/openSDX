package org.fnppl.opensdx.file_transfer.errors;
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
 * OSDX Exception Class
 * 
 * @author Aleksandar Jovanovic
 * @date 19.02.2013
 */
public class OSDXException extends Exception{

	private static final long serialVersionUID = -3752395896495500741L;
	
	//Individual Exception for all error codes
	//FILE ERROS
	
	//ERROR_FILE_RESTRICTED(500),
	public class FileRestrictedException extends OSDXException{
		private static final long serialVersionUID = 3448110258011642393L;

		public FileRestrictedException(){};
		
		public FileRestrictedException(String msg){
			super(msg);
		};
		
		public FileRestrictedException(Throwable cause){
			super(cause);
		};
		
		public FileRestrictedException(String msg, Throwable cause){
			super(msg, cause);
		}		
	}
	
	//ERROR_FILE_NOT_EXISTS(501),
	public class FileDoesNotExistException extends OSDXException{

		private static final long serialVersionUID = 5117811012491670720L;

		public FileDoesNotExistException(){};
		
		public FileDoesNotExistException(String msg){
			super(msg);
		};
		
		public FileDoesNotExistException(Throwable cause){
			super(cause);
		};
		
		public FileDoesNotExistException(String msg, Throwable cause){
			super(msg, cause);
		}	
	}
	
	//ERROR_FILE_ALREADY_EXISTS(502),
	public class FileAlreadyExistsException extends OSDXException{
		
		private static final long serialVersionUID = 2641694490607288485L;

		public FileAlreadyExistsException(){};
		
		public FileAlreadyExistsException(String msg){
			super(msg);
		};
		
		public FileAlreadyExistsException(Throwable cause){
			super(cause);
		};
		
		public FileAlreadyExistsException(String msg, Throwable cause){
			super(msg, cause);
		}		
	}
	
	//ERROR_FILENAME_IS_MISSING(503),
	public class FileNameIsMissingException extends OSDXException{
		
		private static final long serialVersionUID = -8961255735635861469L;

		public FileNameIsMissingException(){};
		
		public FileNameIsMissingException(String msg){
			super(msg);
		};
		
		public FileNameIsMissingException(Throwable cause){
			super(cause);
		};
		
		public FileNameIsMissingException(String msg, Throwable cause){
			super(msg, cause);
		}		
	}
	
	//ERROR_FILE_LENGTH_PARAM(504),
	public class FileFileLengthParamException extends OSDXException{
		
		private static final long serialVersionUID = -8283401801726956945L;

		public FileFileLengthParamException(){};
		
		public FileFileLengthParamException(String msg){
			super(msg);
		};
		
		public FileFileLengthParamException(Throwable cause){
			super(cause);
		};
		
		public FileFileLengthParamException(String msg, Throwable cause){
			super(msg, cause);
		}		
	}
	
	//ERROR_CANNOT_DELETE_FILE(505),
	public class FileDeletionException extends OSDXException{
		
		private static final long serialVersionUID = 6763043809593283367L;

		public FileDeletionException(){};
		
		public FileDeletionException(String msg){
			super(msg);
		};
		
		public FileDeletionException(Throwable cause){
			super(cause);
		};
		
		public FileDeletionException(String msg, Throwable cause){
			super(msg, cause);
		}		
	}
	//ERROR_RETRIEVING_FILE_INFO(506),
	public class FileInfoRetrievingException extends OSDXException{
		
		private static final long serialVersionUID = -1272897068502922407L;

		public FileInfoRetrievingException(){};
		
		public FileInfoRetrievingException(String msg){
			super(msg);
		};
		
		public FileInfoRetrievingException(Throwable cause){
			super(cause);
		};
		
		public FileInfoRetrievingException(String msg, Throwable cause){
			super(msg, cause);
		}		
	}
	//ERROR_WRONG_FILESIZE(507),
	public class FileSizeException extends OSDXException{
		
		private static final long serialVersionUID = -6224985381356962479L;

		public FileSizeException(){};
		
		public FileSizeException(String msg){
			super(msg);
		};
		
		public FileSizeException(Throwable cause){
			super(cause);
		};
		
		public FileSizeException(String msg, Throwable cause){
			super(msg, cause);
		}		
	}

	//DIRECTORY ERRORS
	//ERROR_CANNOT_DELETE_DIR(600),
	public class DirectoryDeletionException extends OSDXException{
		
		private static final long serialVersionUID = -8551998581854521060L;

		public DirectoryDeletionException(){};
		
		public DirectoryDeletionException(String msg){
			super(msg);
		};
		
		public DirectoryDeletionException(Throwable cause){
			super(cause);
		};
		
		public DirectoryDeletionException(String msg, Throwable cause){
			super(msg, cause);
		}		
	}
	
	//ERROR_DIRECTORY_NOT_EXISTS(601),
	public class DirectoryDoesNotExistException extends OSDXException{
		
		private static final long serialVersionUID = -5981854632347119118L;

		public DirectoryDoesNotExistException(){};
		
		public DirectoryDoesNotExistException(String msg){
			super(msg);
		};
		
		public DirectoryDoesNotExistException(Throwable cause){
			super(cause);
		};
		
		public DirectoryDoesNotExistException(String msg, Throwable cause){
			super(msg, cause);
		}		
	}

	//ERROR_DIRECTORY_DEPTH(602),
	public class DirectoryDepthException extends OSDXException{
		
		private static final long serialVersionUID = -3560066612652812604L;

		public DirectoryDepthException(){};
		
		public DirectoryDepthException(String msg){
			super(msg);
		};
		
		public DirectoryDepthException(Throwable cause){
			super(cause);
		};
		
		public DirectoryDepthException(String msg, Throwable cause){
			super(msg, cause);
		}		
	}

	//ERROR_DIRECTORY_DOWNLOAD_NOT_IMPLEMENTED (603),
	public class DirectoryDownloadException extends OSDXException{
		
		private static final long serialVersionUID = 4290265586641998345L;

		public DirectoryDownloadException(){};
		
		public DirectoryDownloadException(String msg){
			super(msg);
		};
		
		public DirectoryDownloadException(Throwable cause){
			super(cause);
		};
		
		public DirectoryDownloadException(String msg, Throwable cause){
			super(msg, cause);
		}
	}

	//ERROR_NOT_A_DIRECTORY(604),
	public class IsNotADirectoryException extends OSDXException{
		
		private static final long serialVersionUID = 426283082979776951L;

		public IsNotADirectoryException(){};
		
		public IsNotADirectoryException(String msg){
			super(msg);
		};
		
		public IsNotADirectoryException(Throwable cause){
			super(cause);
		};
		
		public IsNotADirectoryException(String msg, Throwable cause){
			super(msg, cause);
		}		
	}

	//LOGIN ERRORS
	//ERROR_LOGIN_ACCESS_DENIED(701),
	public class LoginAccesDeniedException extends OSDXException{
		
		private static final long serialVersionUID = -7600902797074889316L;

		public LoginAccesDeniedException(){};
		
		public LoginAccesDeniedException(String msg){
			super(msg);
		};
		
		public LoginAccesDeniedException(Throwable cause){
			super(cause);
		};
		
		public LoginAccesDeniedException(String msg, Throwable cause){
			super(msg, cause);
		}		
	}
	
	//ERROR_LOGIN_USERNAME_MISSING(702),
	public class LoginUsernaneIsMissingException extends OSDXException{
		
		private static final long serialVersionUID = -5779769192589063015L;

		public LoginUsernaneIsMissingException(){};
		
		public LoginUsernaneIsMissingException(String msg){
			super(msg);
		};
		
		public LoginUsernaneIsMissingException(Throwable cause){
			super(cause);
		};
		
		public LoginUsernaneIsMissingException(String msg, Throwable cause){
			super(msg, cause);
		}		
	}

	//UPLOAD ERRORS
	//ERROR_UPLOAD_IS_NULL(801),
	public class UploadIsNullException extends OSDXException{
		
		private static final long serialVersionUID = 707275941684635775L;

		public UploadIsNullException(){};
		
		public UploadIsNullException(String msg){
			super(msg);
		};
		
		public UploadIsNullException(Throwable cause){
			super(cause);
		};
		
		public UploadIsNullException(String msg, Throwable cause){
			super(msg, cause);
		}		
	}
	
	//ERROR_UPLOAD_CANCEL(802),
	public class UploadCancellationException extends OSDXException{
		
		private static final long serialVersionUID = -6690665045077919657L;

		public UploadCancellationException(){};
		
		public UploadCancellationException(String msg){
			super(msg);
		};
		
		public UploadCancellationException(Throwable cause){
			super(cause);
		};
		
		public UploadCancellationException(String msg, Throwable cause){
			super(msg, cause);
		}		
	}

	//ERROR_UPLOAD_HALT(803),
	public class UploadHaltException extends OSDXException{
		
		private static final long serialVersionUID = 266289786817112105L;

		public UploadHaltException(){};
		
		public UploadHaltException(String msg){
			super(msg);
		};
		
		public UploadHaltException(Throwable cause){
			super(cause);
		};
		
		public UploadHaltException(String msg, Throwable cause){
			super(msg, cause);
		}		
	}

	//FILESYSTEM ERRORS
	//ERROR_PATH_IS_NOT_ABSOLUTE(900),
	public class PathIsNotAbsoluteException extends OSDXException{
		
		private static final long serialVersionUID = -5025318443553851246L;

		public PathIsNotAbsoluteException(){};
		
		public PathIsNotAbsoluteException(String msg){
			super(msg);
		};
		
		public PathIsNotAbsoluteException(Throwable cause){
			super(cause);
		};
		
		public PathIsNotAbsoluteException(String msg, Throwable cause){
			super(msg, cause);
		}		
	}

	//ERROR_PATH_IS_MISSING(901),
	public class PathIsMissingException extends OSDXException{
		
		private static final long serialVersionUID = -3670088922737560331L;

		public PathIsMissingException(){};
		
		public PathIsMissingException(String msg){
			super(msg);
		};
		
		public PathIsMissingException(Throwable cause){
			super(cause);
		};
		
		public PathIsMissingException(String msg, Throwable cause){
			super(msg, cause);
		}		
	}

	//ERROR_WRONG_DESTINATION(902),
	public class WrongDestinationException extends OSDXException{
		
		private static final long serialVersionUID = 4155730655653212440L;

		public WrongDestinationException(){};
		
		public WrongDestinationException(String msg){
			super(msg);
		};
		
		public WrongDestinationException(Throwable cause){
			super(cause);
		};
		
		public WrongDestinationException(String msg, Throwable cause){
			super(msg, cause);
		}		
	}

	//ERROR_CANNOT_RENAME(903),
	public class RenameException extends OSDXException{
		
		private static final long serialVersionUID = -1959658979355168685L;

		public RenameException(){};
		
		public RenameException(String msg){
			super(msg);
		};
		
		public RenameException(Throwable cause){
			super(cause);
		};
		
		public RenameException(String msg, Throwable cause){
			super(msg, cause);
		}		
	}

	//ERROR_PATH_IS_RESTRICTED(904),
	public class RestrictedPathException extends OSDXException{
		
		private static final long serialVersionUID = -1996213777539874618L;

		public RestrictedPathException(){};
		
		public RestrictedPathException(String msg){
			super(msg);
		};
		
		public RestrictedPathException(Throwable cause){
			super(cause);
		};
		
		public RestrictedPathException(String msg, Throwable cause){
			super(msg, cause);
		}		
	}
	
	//ERROR_PATH_ALREADY_EXISTS(905),
	public class PathAlreadyExistsException extends OSDXException{
		
		private static final long serialVersionUID = 1236599167862562498L;

		public PathAlreadyExistsException(){};
		
		public PathAlreadyExistsException(String msg){
			super(msg);
		};
		
		public PathAlreadyExistsException(Throwable cause){
			super(cause);
		};
		
		public PathAlreadyExistsException(String msg, Throwable cause){
			super(msg, cause);
		}		
	}

	//ERROR_MKDIR(906),
	public class MakeDirectoryException extends OSDXException{

		private static final long serialVersionUID = -3201810658385660612L;

		public MakeDirectoryException(){};
		
		public MakeDirectoryException(String msg){
			super(msg);
		};
		
		public MakeDirectoryException(Throwable cause){
			super(cause);
		};
		
		public MakeDirectoryException(String msg, Throwable cause){
			super(msg, cause);
		}		
	}

	//OTHER ERRORS
	//ERROR_MD5_CHECK (1000),
	public class MD5CheckException extends OSDXException{

		private static final long serialVersionUID = 3211627888535323754L;

		public MD5CheckException(){};
		
		public MD5CheckException(String msg){
			super(msg);
		};
		
		public MD5CheckException(Throwable cause){
			super(cause);
		};
		
		public MD5CheckException(String msg, Throwable cause){
			super(msg, cause);
		}		
	}

	//ERROR_RIGHTS_AND_DUTIES(1001);
	public class RightsAndDutiesException extends OSDXException{

		private static final long serialVersionUID = -8461999702352166276L;
	
		public RightsAndDutiesException(){};
		
		public RightsAndDutiesException(String msg){
			super(msg);
		};
		
		public RightsAndDutiesException(Throwable cause){
			super(cause);
		};
		
		public RightsAndDutiesException(String msg, Throwable cause){
			super(msg, cause);
		}	
	}
	
	//Programm Errors
	public class InitSecureUserPassConnectionException extends OSDXException{
		private static final long serialVersionUID = -936738326425741731L;

		public InitSecureUserPassConnectionException(){};
		
		public InitSecureUserPassConnectionException(String msg){
			super(msg);
		};
		
		public InitSecureUserPassConnectionException(Throwable cause){
			super(cause);
		};
		
		public InitSecureUserPassConnectionException(String msg, Throwable cause){
			super(msg, cause);
		}	
	}
	
	public class SocketNotConnectedException extends OSDXException{

		private static final long serialVersionUID = -7183614070555778351L;

		public SocketNotConnectedException(){};
		
		public SocketNotConnectedException(String msg){
			super(msg);
		};
		
		public SocketNotConnectedException(Throwable cause){
			super(cause);
		};
		
		public SocketNotConnectedException(String msg, Throwable cause){
			super(msg, cause);
		}	
	}	
	
	public OSDXException(){};
	
	public OSDXException(String msg){
		super(msg);
	};
	
	public OSDXException(Throwable cause){
		super(cause);
	};
	
	public OSDXException(String msg, Throwable cause){
		super(msg, cause);
	}
}
