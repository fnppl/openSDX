package org.fnppl.opensdx.file_transfer.helper;
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
import java.io.File;
import java.util.*;

import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferUserPassLoginCommand;
import org.fnppl.opensdx.file_transfer.model.RemoteFile;
import org.fnppl.opensdx.file_transfer.trigger.Trigger;
import org.fnppl.opensdx.file_transfer.trigger.TriggerCollection;
import org.fnppl.opensdx.keyserverfe.Helper;
import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;
import org.fnppl.opensdx.xml.XMLHelper;

public class ClientSettings {
	
	private String username;
	private String pass;
	
	private String keyid;
	private String auth_type = null;
	private byte[] login_sha256 = null;
	private byte[] login_initv = null;
	private RightsAndDuties rights_duties = null;
	private TriggerCollection triggers = null;
	private Element eTriggers = null;
	
	private File local_path;
	
	 
	private ClientSettings() {
		
	}
	
	public static Vector<ClientSettings> fromElement(Element e, int defaultMaxDirDepth, TriggerCollection defaultTriggers) {
		Vector<ClientSettings> ret = new Vector<ClientSettings>();
		
		System.out.println(e.toString());
		
		List<Element> keyids = e.getChildren("keyid");
		int key_index = 0;
		while(true) {
			ClientSettings s = new ClientSettings();
			
			if(key_index<keyids.size()) {
				s.keyid = keyids.get(key_index).getText();
				key_index++;
			}
			else {
				s.keyid = "";
			}
			
			s.username = e.getChildText("username");
			s.pass = e.getChildText("pass");
			if (e.getChildText("local_path")!=null) {
				s.local_path = new File(e.getChildText("local_path"));
			}
			s.auth_type = e.getChildText("auth_type");
			if (s.username==null || s.keyid==null || s.local_path==null || s.auth_type==null) {
				throw new RuntimeException("Format ERROR in client settings");
			}
			//TODO HT 2012-03-20 
			if (e.getChild("login") != null) {
				s.login_sha256 = SecurityHelper.HexDecoder.decode(e.getChild("login").getChildText("sha256"));
				s.login_initv = SecurityHelper.HexDecoder.decode(e.getChild("login").getChildText("initv"));
				if (s.login_initv==null || s.login_sha256==null) {
					throw new RuntimeException("Format ERROR in client settings");
				}
			}
			if(e.getChild("rights_and_duties") != null) {
				s.rights_duties = RightsAndDuties.fromElement(e.getChild("rights_and_duties"),defaultMaxDirDepth);
			} else {
				s.rights_duties = new RightsAndDuties(defaultMaxDirDepth);
			}
			
			//Trigger
			s.eTriggers = e.getChild("triggers");
			Vector<Trigger> triggList = new Vector<Trigger>(); 
			if (s.eTriggers!=null) {
				Vector<Element> triggers = s.eTriggers.getChildren("trigger");
				for (Element et : triggers) {
					triggList.add(Trigger.fromElement(et));
				}	
			}
			
			if (defaultTriggers==null) {
				s.triggers = new TriggerCollection();
			} else {
				s.triggers = defaultTriggers.getCopy();
				
				//replace system trigger
				int tlCount = Trigger.TRIGGER_LIST.size();
				boolean[] replaceSystemTrigger = new boolean[tlCount];
				for (int i=0;i<tlCount;i++) {
					replaceSystemTrigger[i] = false;
				}
				for (Trigger t : triggList) {
					if (t.isReplaceDefault()) {
						int ind = Trigger.TRIGGER_LIST.indexOf(t.getEventType());
						if (ind>=0) {
							replaceSystemTrigger[ind] = true;
						}
					}
				}
				for (int i=0;i<tlCount;i++) {
					if (replaceSystemTrigger[i]) {
						s.triggers.removeTriggersForEvent(Trigger.TRIGGER_LIST.get(i));
					}
				}
			}
			//add new trigger
			for (Trigger t : triggList) {
				s.triggers.addTrigger(t);
			}
			
			
			if(key_index<keyids.size()) {
				
			}
			
			ret.addElement(s);
			
			if(key_index>=keyids.size()) {
				break;
			}
		}
		
		return ret;
	}
	
	public Element toElement() {
		Element e = new Element("client");
		if (username!=null) e.addContent("username",username);
		if (keyid != null) {
			e.addContent("keyid",keyid);
		}
		if (local_path!=null) e.addContent("local_path",local_path.getAbsolutePath());
		if (auth_type!=null) e.addContent("auth_type",auth_type);
		if (pass!=null) e.addContent("pass", pass);
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
			e.addContent(rights_duties.toElement(false));
		}
		if (eTriggers!=null) {
			e.addContent(XMLHelper.cloneElement(eTriggers));
		}
		return e;
	}

	public void triggerEvent(String event, HashMap<String, Object> context) { 
		if (triggers!=null) {
			triggers.triggerEvent(event, context);
		}
	}
	
	public RightsAndDuties getRightsAndDuties() {
		return rights_duties;
	}
	
	public static String getUserPassAuth(String user, String pass) {
		//see OSDXFileTransferUserPassLoginCommand.getUserPassAuth
		return SecurityHelper.HexDecoder.encode(SecurityHelper.getMD5((user+"\0"+pass).getBytes()));
	}
	
	//public static String getSettingsID(String auth_type, String username, String keyid, String pass) {
	public String getSettingsID() {
		if(auth_type.equalsIgnoreCase("keyfile")) {
			return username+"::"+keyid;
		}
		if(auth_type.equalsIgnoreCase("login")) {
			return username+"::"+getUserPassAuth(username, pass);
		}
		
		return null;
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
	
	public boolean isAllowedDepthFile(File f) {
		return isAllowedDepthDir(f.getParentFile());
	}
	
	public boolean isAllowedDepthDir(File dir) {
		try {
			int baseDepth = getDirDepth(local_path.getCanonicalFile());
			int dirDepth = getDirDepth(dir.getCanonicalFile());
			return rights_duties.isAllowedDepth(dirDepth-baseDepth);
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
	
	private int getDirDepth(File dir) {
		int d = 1;
		while (dir.getParentFile()!=null) {
			d++;
			dir = dir.getParentFile();
		}
		return d;
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
//			System.out.println("REQUEST : "+f.getAbsolutePath());
//			System.out.println("LOCAL   : "+local_path.getAbsolutePath());
//			System.out.println("-> PATH : "+path);
//			System.out.println("-> NAME : "+name);
			
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
