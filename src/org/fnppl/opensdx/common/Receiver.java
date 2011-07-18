package org.fnppl.opensdx.common;

import java.util.Vector;

import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.xml.Element;

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

/**
 * 
 * @author Bertram Boedeker <bboedeker@gmx.de>
 * 
 */
public class Receiver extends BusinessObject {

	public static String KEY_NAME = "receiver";
	
	public static String TRANSFER_TYPE_OSDX_FILESERVER = "openSDX fileserver";
	public static String TRANSFER_TYPE_FTP = "ftp";
	public static String TRANSFER_TYPE_SFTP = "sftp";	
	public static String TRANSFER_TYPE_FTPS = "ftps";
	public static String TRANSFER_TYPE_WEBDAV = "webdav";
	public static Vector<String> SUPPORTED_TRANSFER_TYPES = new Vector<String>();
	static {
		SUPPORTED_TRANSFER_TYPES.add(TRANSFER_TYPE_OSDX_FILESERVER);
		SUPPORTED_TRANSFER_TYPES.add(TRANSFER_TYPE_FTP);
		SUPPORTED_TRANSFER_TYPES.add(TRANSFER_TYPE_SFTP);
		SUPPORTED_TRANSFER_TYPES.add(TRANSFER_TYPE_FTPS);
		SUPPORTED_TRANSFER_TYPES.add(TRANSFER_TYPE_WEBDAV);
	}
	
	public static String AUTH_TYPE_LOGIN = "login";
	public static String AUTH_TYPE_KEYFILE = "keyfile";
	public static String AUTH_TYPE_TOKEN = "token";
	public static String AUTH_TYPE_OTHER = "other";
	public static Vector<String> SUPPORTED_AUTH_TYPES = new Vector<String>();
	static {
		SUPPORTED_AUTH_TYPES.add(AUTH_TYPE_LOGIN);
		SUPPORTED_AUTH_TYPES.add(AUTH_TYPE_KEYFILE);
		SUPPORTED_AUTH_TYPES.add(AUTH_TYPE_TOKEN);
		SUPPORTED_AUTH_TYPES.add(AUTH_TYPE_OTHER);
	}
	
	private BusinessStringItem type;						//MUST
	private BusinessStringItem servername;  				//MUST
	private BusinessStringItem serveripv4; 					//MUST
	private BusinessStringItem serveripv6; 					//COULD
	private BusinessStringItem authtype; 					//MUST
	private BusinessStringItem username;					//MUST for OSDXFileTransferServer
	private BusinessBytesItem authsha1; 					//MUST
	private BusinessObject crypto;							//COULD
	private BusinessStringItem file_keystore;
	private BusinessStringItem keyid;
	
	
	
	private Receiver()  {
		
	}
	
	/***
	 * the make method constructs a Receiver object with the following MUST have attributes 
	 * @param type :: tftp|sftp|ftps|webdav see TRANSFER_TYPES
	 * @param servername ::  providers hostname 
	 * @param serveripv4 :: resolved servername ipv4
	 * @param authtype :: login|keyfile|token|other see AUTH_TYPE
	 * @param authsha1 :: case(login): SHA1(USERNAME:PASS) case(keyfile): SHA1(KEYFILE-data); case(token): SHA1(TOKEN-data) case(other): SHA1(relevant-data)
	 * @return newly instantiated Receiver
	 * 
	 * OPTIONAL fields in Receiver:
	 * 	COULD:  serveripv6 :: see serveripv6(String ipv6)
	 * 	COULD: 	crypto:: see crypto(BusinessObject crypto)
	 */
	public static Receiver make(String type) {
		Receiver r = new Receiver();
		r.type = new BusinessStringItem("type", type);
//		r.servername = new BusinessStringItem("servername", servername);
//		r.serveripv4 = new BusinessStringItem("serveripv4", serveripv4);
//		r.authtype = new BusinessStringItem("authtype", authtype);
//		r.authsha1 = new BusinessStringItem("authsha1", SecurityHelper.HexDecoder.encode(authsha1, ':', -1));
		
		r.servername = null;
		r.serveripv4 = null;
		r.authtype = null;
		r.authsha1 = null;
		
		r.serveripv6 = null;
		r.crypto = null;
		r.file_keystore = null;
		r.keyid = null;
		r.username = null;
		return r;
	}
	
	public static Receiver fromBusinessObject(BusinessObject bo) {
		if (bo==null) return null;
		if (!bo.getKeyname().equals(KEY_NAME)) {
			bo = bo.handleBusinessObject(KEY_NAME);
		}
		if (bo==null) return null;
		Receiver r = new Receiver();
		r.initFromBusinessObject(bo);
		
		r.type = BusinessStringItem.fromBusinessObject(bo, "type");
		r.servername = BusinessStringItem.fromBusinessObject(bo, "servername");
		r.serveripv4 = BusinessStringItem.fromBusinessObject(bo, "serveripv4");
		r.authtype = BusinessStringItem.fromBusinessObject(bo, "authtype");
		r.authsha1 = BusinessBytesItem.fromBusinessObject(bo, "authsha1");
		
		r.serveripv6 = BusinessStringItem.fromBusinessObject(bo, "serveripv6");
		r.crypto = bo.handleBusinessObject("crypto");
		r.file_keystore = BusinessStringItem.fromBusinessObject(bo, "file_keystore");
		r.keyid = BusinessStringItem.fromBusinessObject(bo, "keyid");
		r.username = BusinessStringItem.fromBusinessObject(bo, "username");
		return r;
	}

	public Receiver type(String value) {
		if (value == null) {
			type = null;
		} else if (type == null) {
			type = new BusinessStringItem("type", value);
			if (!value.equals(TRANSFER_TYPE_OSDX_FILESERVER)) {
				authtype(AUTH_TYPE_LOGIN);
			} else {
				authtype(AUTH_TYPE_KEYFILE);
			}
		} else {
			type.setString(value);
		}
		return this;
	}
	
	public Receiver username(String value) {
		if (value == null) {
			username = null;
		} else if (username == null) {
			username = new BusinessStringItem("username", value);
		} else {
			username.setString(value);
		}
		return this;
	}
	public Receiver servername(String value) {
		if (value == null) {
			servername = null;
		} else if (servername == null) {
			servername = new BusinessStringItem("servername", value);
		} else {
			servername.setString(value);
		}
		return this;
	}
	public Receiver serveripv4(String value) {
		if (value == null) {
			serveripv4 = null;
		} else if (serveripv4 == null) {
			serveripv4 = new BusinessStringItem("serveripv4", value);
		} else {
			serveripv4.setString(value);
		}
		return this;
	}
	public Receiver authtype(String value) {
		if (value == null) {
			authtype = null;
		} else if (authtype == null) {
			authtype = new BusinessStringItem("authtype", value);
		} else {
			authtype.setString(value);
		}
		return this;
	}
	public Receiver authsha1(byte[] authsha1) {
		if (authsha1 == null) {
			this.authsha1 = null;
		} else if (this.authsha1 == null) {
			 this.authsha1 = new BusinessBytesItem("authsha1", authsha1);
		} else {
			this.authsha1.setBytes(authsha1);
		}
		return this;
	}
	
	public Receiver serveripv6(String value) {
		if (value == null) {
			serveripv6 = null;
		} else if (serveripv6 == null) {
			serveripv6 = new BusinessStringItem("serveripv6", value);
		} else {
			serveripv6.setString(value);
		}
		return this;
	}
	
	public Receiver file_keystore(String value) {
		if (value == null) {
			file_keystore = null;
		}
		else if (file_keystore==null) {
			file_keystore = new BusinessStringItem("file_keystore", value);
		}
		else {
			file_keystore.setString(value);
		}
		return this;
	}
	public String getFileKeystore() {
		if (file_keystore==null) return null;
		return file_keystore.getString();
	}
	public Receiver keyid(String value) {
		if (value == null) {
			keyid = null;
		}
		else if (keyid==null) {
			keyid = new BusinessStringItem("keyid", value);
		}
		else {
			keyid.setString(value);
		}
		return this;
	}
	
	public String getUsername() {
		if (username==null) return null;
		return username.getString();
	}
	
	public String getKeyID() {
		if (keyid==null) return null;
		return keyid.getString();
	}
	public Receiver crypto(BusinessObject crypto) {
		if (crypto==null) return null;
		if (crypto.getKeyname().equals("crypto")) {
			this.crypto = crypto;
		} else {
			throw new RuntimeException("Receiver::crypto element MUST have \"crypto\" as keyname!");
		}
		return this;
	}
	
	public String getKeyname() {
		return KEY_NAME;
	}
	
	public String getType() {
		if (type==null) return null;
		return type.getString();
	}

	public String getServername() {
		if (servername==null) return null;
		return servername.getString();
	}
	
	public String getServerIPv4() {
		if (serveripv4==null) return null;
		return serveripv4.getString();
	}
	
	public String getServerIPv6() {
		if (serveripv6==null) return null;
		return serveripv6.getString();
	}
	
	public String getAuthType() {
		if (authtype==null) return null;
		return authtype.getString();
	}
	
	public byte[] getAuthSha1() {
		String s = getAuthSha1Text();
		if (s==null) return null;
		return SecurityHelper.HexDecoder.decode(s);
	}
	public String getAuthSha1Text() {
		if (authsha1==null) return null;
		return authsha1.getString();
	}
	public BusinessObject getCrypto() {
		return crypto;
	}
	
	public Element toElement() {
		//remove fields that are useless for selected type and auth_type
		String type = getType();
		String authtype = getAuthType();
//		if (type==null || !type.equals(TRANSFER_TYPE_OSDX_FILESERVER)) {
//			file_keystore(null);
//			keyid(null);
//		}
		if (authtype==null || !authtype.equals(AUTH_TYPE_KEYFILE)) {
			file_keystore(null);
			keyid(null);
		}
		return super.toElement();
	}

}
