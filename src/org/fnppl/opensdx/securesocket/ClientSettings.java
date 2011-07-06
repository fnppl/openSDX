package org.fnppl.opensdx.securesocket;
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

import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.xml.Element;

public class ClientSettings {
	
	private String username;
	private String keyid;
	private String auth_type = null;
	private byte[] login_sha256 = null;
	private byte[] login_initv = null;
	
	private File local_path;
	
	 
	private ClientSettings() {
		
	}
	
	public static ClientSettings fromElement(Element e) {
		ClientSettings s = new ClientSettings();
		s.username = e.getChildText("username");
		s.keyid = e.getChildText("keyid");
		s.local_path = new File(e.getChildText("local_path"));
		s.auth_type = e.getChildText("auth_type");
		if (s.username==null || s.keyid==null || s.local_path==null || s.auth_type==null) {
			throw new RuntimeException("Format ERROR in client settings");
		}
		if (e.getChild("login")!=null) {
			s.login_sha256 = SecurityHelper.HexDecoder.decode(e.getChild("login").getChildText("sha256"));
			s.login_initv = SecurityHelper.HexDecoder.decode(e.getChild("login").getChildText("initv"));
			if (s.login_initv==null || s.login_sha256==null) {
				throw new RuntimeException("Format ERROR in client settings");
			}
		}
		return s;
	}
	
	public String getSettingsID() {
		return username+"::"+keyid;
	}
	
	public String getKeyID() {
		return keyid;
	}
	
	public File getLocalRootPath() {
		return local_path;
	}
}