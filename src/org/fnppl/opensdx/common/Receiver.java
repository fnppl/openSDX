package org.fnppl.opensdx.common;

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



import java.util.Vector;
import org.fnppl.opensdx.common.BaseObjectWithConstraints;
import org.fnppl.opensdx.xml.Element;

public class Receiver extends BaseObjectWithConstraints {

	public Receiver() {
		names.add("type"); values.add(null); constraints.add("?");
		names.add("servername"); values.add(null); constraints.add("MUST");
		names.add("serveripv4"); values.add(null); constraints.add("MUST");
		names.add("serveripv6"); values.add(null); constraints.add("COULD");
		names.add("authtype"); values.add(null); constraints.add("MUST");
		names.add("authsha1"); values.add(null); constraints.add("MUST");
		names.add("crypto_relatedemail"); values.add(null); constraints.add("COULD");
		names.add("crypto_usedkeyid"); values.add(null); constraints.add("COULD");
		names.add("crypto_usedpubkey"); values.add(null); constraints.add("COULD");
	}

// methods
	public void setType(String type) {
		set("type", type);
	}

	public String getType() {
		return get("type");
	}

	public void setServername(String servername) {
		set("servername", servername);
	}

	public String getServername() {
		return get("servername");
	}

	public void setServeripv4(String serveripv4) {
		set("serveripv4", serveripv4);
	}

	public String getServeripv4() {
		return get("serveripv4");
	}

	public void setServeripv6(String serveripv6) {
		set("serveripv6", serveripv6);
	}

	public String getServeripv6() {
		return get("serveripv6");
	}

	public void setAuthtype(String authtype) {
		set("authtype", authtype);
	}

	public String getAuthtype() {
		return get("authtype");
	}

	public void setAuthsha1(String authsha1) {
		set("authsha1", authsha1);
	}

	public String getAuthsha1() {
		return get("authsha1");
	}

	public void setCryptoRelatedEmail(String email) {
		set("crypto_relatedemail", email);
	}

	public String getCryptoRelatedEmail() {
		return (String)getObject("crypto_relatedemail");
	}
	
	public void setCryptoUsedKeyID(String keyid) {
		set("crypto_usedkeyid", keyid);
	}

	public String getCryptoUsedKeyID() {
		return (String)getObject("crypto_usedkeyid");
	}
	
	public void setCryptoUsedPubKey(String pubkey) {
		set("crypto_usedpubkey", pubkey);
	}

	public String getCryptoUsedPubKey() {
		return (String)getObject("crypto_usedpubkey");
	}
	
	public Element toElement() {
		return toElement("receiver");
	}
	
	public Element toElement(String name) {
		Element e = new Element(name);
		add(e,"type");
		add(e,"servername");
		add(e,"serveripv4");
		add(e,"serveripv6");
		add(e,"authtype");
		add(e,"authsha1");
		
		Element e2 = new Element("crypto"); e.addContent(e2);
		add(e2,"crypto_relatedemail","relatedemail");
		add(e2,"crypto_usedkeyid", "usedkeyid");
		add(e2,"crypto_usedpubkey", "usedpubkey");
		
		return e;
	}
	
	private void addElement(Element e, String name, String newName) {
		Object b = getObject(name);
		if (b!=null) {
			e.addContent(((BaseObject)b).toElement(newName));
		}
	}
	private void add(Element e, String name) {
		String s = get(name);
		if (s!=null)
			e.addContent(name, s);
	}
	private void add(Element e, String name, String newName) {
		String s = get(name);
		if (s!=null)
			e.addContent(newName, s);
	}

}
