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

/**
 * @author Bertram Bödeker <bboedeker@gmx.de>
 */

import java.io.File;
import java.io.FilenameFilter;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fnppl.opensdx.xml.Element;

public class RightsAndDuties {

	private boolean allow_mkdir = true;
	private boolean allow_delete = true;
	private boolean allow_pwd = true;
	private boolean allow_cd = true;
	private boolean allow_list = true;
	private boolean allow_upload = true;
	private boolean allow_download = true;
	private Vector<String> signature_needed = null;
	private Pattern signature_needed_pattern = null;
	
	public RightsAndDuties() {
		
	}

	public static RightsAndDuties fromElement(Element e) {
		RightsAndDuties r = new RightsAndDuties();
		r.allow_mkdir = parse(e,"allow_mkdir",true);
		r.allow_delete = parse(e,"allow_delete",true);
		r.allow_pwd = parse(e,"allow_pwd",true);
		r.allow_cd = parse(e,"allow_cd",true);
		r.allow_list = parse(e,"allow_list",true);
		r.allow_upload = parse(e,"allow_upload",true);
		r.allow_download = parse(e,"allow_download",true);
		Vector<Element> sigs = e.getChildren("signature_needed");
		if (sigs!=null && sigs.size()>0) {
			r.signature_needed = new Vector<String>();
			for (Element es : sigs) {
				r.signature_needed.add(es.getText());
			}
		}
		return r;
	}
	
	public void removeAllSignatureNeeded() {
		signature_needed = null;
	}
	public void addSignatureNeeded(String pattern) {
		if (signature_needed==null) {
			signature_needed = new Vector<String>();
		}
		signature_needed.add(pattern);
		signature_needed_pattern = null;
	}
	
	private static boolean parse(Element e, String allow, boolean default_value) {
		if (e.getChild(allow)==null) {
			return default_value;
		} else {
			try {
				return Boolean.parseBoolean(e.getChildText(allow));
			} catch (Exception ex) {
				return default_value;
			}
		}
	}

	public boolean needsSignature(String filename) {
		if (signature_needed==null || signature_needed.size()==0) return false;
		if (signature_needed_pattern == null) {
			String ps = signature_needed.get(0).toLowerCase();
			for (int i=1;i<signature_needed.size();i++) {
				ps += "|"+signature_needed.get(i).toLowerCase();
			}
			String rps = ps.replace(".", "\\.");
			rps = rps.replace("*", ".*");
			signature_needed_pattern = Pattern.compile(rps);
		}
		Matcher m = signature_needed_pattern.matcher(filename.toLowerCase());
		if (m.matches()) {
			return true;
		}
		return false;
	}
	
	public boolean allowsMkdir() {
		return allow_mkdir;
	}
	
	public boolean allowsDelete() {
		return allow_delete;
	}
	
	public boolean allowsPWD() {
		return allow_pwd;
	}
	
	public boolean allowsCD() {
		return allow_cd;
	}
	
	public boolean allowsList() {
		return allow_list;
	}
	
	public boolean allowsUpload() {
		return allow_upload;
	}
	
	public boolean allowsDownload() {
		return allow_download;
	}
	
	public Element toElement() {
		Element e = new Element("rights_and_duties");
		if (!allow_mkdir) e.addContent("allow_mkdir", "false");
		if (!allow_delete) e.addContent("allow_delete", "false");
		if (!allow_pwd) e.addContent("allow_pwd", "false");
		if (!allow_cd) e.addContent("allow_cd", "false");
		if (!allow_list) e.addContent("allow_list", "false");
		if (!allow_upload) e.addContent("allow_upload", "false");
		if (!allow_download) e.addContent("allow_download", "false");
		if (signature_needed!=null && signature_needed.size()>0) {
			for (String s : signature_needed) {
				e.addContent("signature_needed", s);
			}
		}
		return e;
	}
	
	
//	<rights_and_duties>
//	  <allow_mkdir>true</allow_mkdir>       <!--  default true -->
//	  <allow_delete>true</allow_delete>     <!--  default true -->
//	  <allow_pwd>true</allow_pwd>           <!--  default true -->
//	  <allow_cd>true</allow_cd>             <!--  default true -->
//	  <allow_list>true</allow_list>         <!--  default true -->
//	  <allow_upload>true</allow_upload>     <!--  default true -->
//	  <allow_download>true</allow_download> <!--  default true -->
//	  <signature_needed>*.pdf</signature_needed> <!--  default no signatures needed -->
//	  <signature_needed>*.txt</signature_needed>
//	</rights_and_duties> 
}