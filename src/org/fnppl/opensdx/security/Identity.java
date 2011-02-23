package org.fnppl.opensdx.security;

import java.util.Arrays;

import org.fnppl.opensdx.common.Territory;

import org.fnppl.opensdx.xml.Element;


/*
 * Copyright (C) 2010-2011 
 * 							fine people e.V. <opensdx@fnppl.org> 
 * 							Henning Thieß <ht@fnppl.org>
 * 
 * 							http://fnppl.org
 * 
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


public class Identity {
	String email = null;
	String mnemonic = null;
	String phone = null;
//	Territory country = null;
	String country = null;
	String region = null;
	String postcode = null;
	String company = null;
	String unit = null;
	String subunit = null;
	
	String function = null;
	String surname = null;
	String middlename = null;
	String name = null;
	
	String note = null;
	
	private Identity() {
		
	}
	
	public static Identity fromElement(Element id) {
		Identity idd = new Identity();
		idd.email = id.getChildText("email");
		idd.mnemonic = id.getChildText("mnemonic");
		idd.phone = id.getChildText("phone");
		idd.country = id.getChildText("country");
		idd.region = id.getChildText("region");
		idd.postcode = id.getChildText("postcode");
		idd.company = id.getChildText("company");
		idd.unit = id.getChildText("unit");
		idd.subunit = id.getChildText("subunit");

		idd.function = id.getChildText("function");
		idd.surname = id.getChildText("surname");
		idd.middlename = id.getChildText("middlename");
		idd.name = id.getChildText("name");
		idd.note = id.getChildText("note");
		
		return idd;
	}
	public Element toElement() {
		Element id = new Element("identity");
		
		id.addContent("email", email);
		id.addContent("mnemonic", mnemonic);
		id.addContent("phone", phone);
		id.addContent("country", country);
		id.addContent("region", region);
		id.addContent("postcode", postcode);
		id.addContent("company", company);
		id.addContent("unit", unit);
		id.addContent("subunit", subunit);
		id.addContent("function", function);
		id.addContent("surname", surname);
		id.addContent("middlename", middlename);
		id.addContent("name", name);
		id.addContent("note", note);
		
		return id;
	}
	
	public boolean validate(byte[] sha1b) throws Exception {
		byte[] ret = new byte[160];
		org.bouncycastle.crypto.digests.SHA1Digest sha1 = new org.bouncycastle.crypto.digests.SHA1Digest();
		
		byte[] k = null;
		k = email.getBytes("UTF-8"); sha1.update(k, 0, k.length);
		k = mnemonic.getBytes("UTF-8"); sha1.update(k, 0, k.length);
		k = phone.getBytes("UTF-8"); sha1.update(k, 0, k.length);
		k = country.getBytes("UTF-8"); sha1.update(k, 0, k.length);
		k = region.getBytes("UTF-8"); sha1.update(k, 0, k.length);
		k = postcode.getBytes("UTF-8"); sha1.update(k, 0, k.length);
		k = company.getBytes("UTF-8"); sha1.update(k, 0, k.length);
		k = unit.getBytes("UTF-8"); sha1.update(k, 0, k.length);
		k = subunit.getBytes("UTF-8"); sha1.update(k, 0, k.length);
		k = function.getBytes("UTF-8"); sha1.update(k, 0, k.length);
		k = surname.getBytes("UTF-8"); sha1.update(k, 0, k.length);
		k = middlename.getBytes("UTF-8"); sha1.update(k, 0, k.length);
		k = name.getBytes("UTF-8"); sha1.update(k, 0, k.length);
		k = note.getBytes("UTF-8"); sha1.update(k, 0, k.length);
		
		sha1.doFinal(ret, 0);
		
		return Arrays.equals(ret, sha1b);
	}
}

