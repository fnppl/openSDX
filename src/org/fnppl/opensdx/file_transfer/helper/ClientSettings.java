package org.fnppl.opensdx.file_transfer.helper;
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

import org.fnppl.opensdx.file_transfer.model.RemoteFile;
import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.xml.Element;

public class ClientSettings {
	
	private String username;
	private String keyid;
	private String auth_type = null;
	private byte[] login_sha256 = null;
	private byte[] login_initv = null;
	private RightsAndDuties rights_duties = null;
	
	private File local_path;
	
	 
	private ClientSettings() {
		
	}
	
	public static ClientSettings fromElement(Element e) {
		ClientSettings s = new ClientSettings();
		s.username = e.getChildText("username");
		s.keyid = e.getChildText("keyid");
		if (e.getChildText("local_path")!=null) {
			s.local_path = new File(e.getChildText("local_path"));
		}
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
		if (e.getChild("rights_and_duties")!=null) {
			s.rights_duties = RightsAndDuties.fromElement(e.getChild("rights_and_duties"));
		} else {
			s.rights_duties = new RightsAndDuties();
		}
		return s;
	}
	
	public Element toElement() {
		Element e = new Element("client");
		if (username!=null) e.addContent("username",username);
		if (keyid!=null) e.addContent("keyid",keyid);
		if (local_path!=null) e.addContent("local_path",local_path.getAbsolutePath());
		if (auth_type!=null) e.addContent("auth_type",auth_type);
		if (login_initv!=null || login_sha256 != null)  {
			Element el = new Element("login");
			if (login_sha256 != null)  {
				el.addContent("sha256", SecurityHelper.HexDecoder.encode(login_sha256));
			}
			if (login_initv!=null) {
				el.addContent("initv", SecurityHelper.HexDecoder.encode(login_initv));
			}
			e.addContent(el);
		}
		if (rights_duties!=null) {
			e.addContent(rights_duties.toElement());
		}
		return e;
	}
	
	public RightsAndDuties getRightsAndDuties() {
		return rights_duties;
	}
	
	public String getSettingsID() {
		return username+"::"+keyid;
	}
	
	public String getKeyID() {
		return keyid;
	}
	
	public String getUsername() {
		return username;
	}
	
	public File getLocalRootPath() {
		return local_path;
	}
	public void setLocalRootPath(File path) {
		local_path = path;
	}
	
	public boolean isAllowed(File f) {
		try {
			if (f.getCanonicalPath().startsWith(local_path.getCanonicalPath())) {
				return true;
			} else {
				return false;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
	
	public RemoteFile getAsRemoteFile(File f) {
		try {
			if (!f.exists()) return null;
			String path = "";
			String name= ""+f.getName();
			
			if (f.equals(local_path)) {
				path = "";
				name = "/";
			} 
//			else if (f.getParentFile().equals(local_path)) {
//				path = "/"+f.getParent();
//			}
			else {
				path = f.getParentFile().getCanonicalPath().substring(local_path.getCanonicalPath().length());
				if (path.length()==0) {
					path = "/";
				}
			}
			System.out.println("REQUEST : "+f.getAbsolutePath());
			System.out.println("LOCAL   : "+local_path.getAbsolutePath());
			System.out.println("-> PATH : "+path);
			System.out.println("-> NAME : "+name);
			
			RemoteFile rf = new RemoteFile(path, name, f.length(), f.lastModified(), f.isDirectory());
			return rf;
		} catch (Exception ex) {
			System.out.println(f.getAbsolutePath());
			System.out.println(local_path.getAbsolutePath());
			ex.printStackTrace();
			return null;
		}
	}
}
