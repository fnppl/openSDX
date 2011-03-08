package org.fnppl.opensdx.security;


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

import java.util.Arrays;
import java.util.Vector;

import org.bouncycastle.jce.provider.JDKMessageDigest.SHA1;
import org.fnppl.opensdx.common.Territory;

import org.fnppl.opensdx.xml.Element;
import org.fnppl.opensdx.xml.XMLHelper;



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
	byte[] sha1FromElement = null;
	
	Vector<DataSourceStep> datapath = null;
	private boolean unsavedChanges = false;
	
	private Identity() {
		
	}
	
	public static Identity newEmptyIdentity() throws Exception {
		Identity idd = new Identity();
		idd.email = "";
		idd.mnemonic = "";
		idd.phone = "";
		idd.country = "";
		idd.region = "";
		idd.postcode = "";
		idd.company = "";
		idd.unit = "";
		idd.subunit = "";
		idd.function = "";
		idd.surname = "";
		idd.middlename = "";
		idd.name = "";
		idd.note = "";
		idd.datapath = new Vector<DataSourceStep>();
		idd.unsavedChanges = true;
		return idd;
	}
	
	public static Identity fromElement(Element id) throws Exception {
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
		
		idd.sha1FromElement = SecurityHelper.HexDecoder.decode(id.getChildText("sha1"));
		
		//datapath
		Element dp = id.getChild("datapath");
		boolean dsOK = false;
		if (dp!=null) {
			idd.datapath = new Vector<DataSourceStep>();
			Vector<Element> steps = dp.getChildren();
			for (Element st : steps)
			if (st.getName().startsWith("step")) {
				DataSourceStep dst = DataSourceStep.fromElemet(st);
				idd.datapath.add(dst);
				dsOK = true;
			}
		}
		if (!dsOK) {
			System.out.println("CAUTION datasource and datainsertdatetime NOT found.");
		}
		
		return idd;
	}
	
	public boolean validate() throws Exception {
		byte[] sha1 = calcSHA1();
		//String ssha1 = SecurityHelper.HexDecoder.encode(sha1, '\0', -1);
		return Arrays.equals(sha1,sha1FromElement);
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
		try {
			byte[] sha1b = calcSHA1();
			id.addContent("sha1", SecurityHelper.HexDecoder.encode(sha1b, ':', -1));
			sha1FromElement = sha1b;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		//datapath
		Element edp = new Element("datapath");
		for (int i=0;i<datapath.size();i++) {
			Element edss = new Element("step"+(i+1));
			edss.addContent("datasource",datapath.get(i).getDataSource());
			edss.addContent("datainsertdatetime", datapath.get(i).getDataInsertDatetimeString());
			edp.addContent(edss);
		}
		id.addContent(edp);
		unsavedChanges = false;
		return id;
	}
	
	public boolean validate(byte[] sha1b) throws Exception {
		byte[] _sha1b = calcSHA1();
		return Arrays.equals(_sha1b, sha1b);
	}
	
	
	public byte[] calcSHA1() throws Exception {
		byte[] ret = new byte[20];  //160bit = 20 byte
		org.bouncycastle.crypto.digests.SHA1Digest sha1 = new org.bouncycastle.crypto.digests.SHA1Digest();
		
		byte[] k = null;
		k = email.getBytes("UTF-8"); if (k.length>0) sha1.update(k, 0, k.length);
		k = mnemonic.getBytes("UTF-8"); if (k.length>0) sha1.update(k, 0, k.length);
		k = phone.getBytes("UTF-8"); if (k.length>0) sha1.update(k, 0, k.length);
		k = country.getBytes("UTF-8");if (k.length>0) sha1.update(k, 0, k.length);
		k = region.getBytes("UTF-8"); if (k.length>0) sha1.update(k, 0, k.length);
		k = postcode.getBytes("UTF-8");if (k.length>0) sha1.update(k, 0, k.length);
		k = company.getBytes("UTF-8");if (k.length>0) sha1.update(k, 0, k.length);
		k = unit.getBytes("UTF-8"); if (k.length>0) sha1.update(k, 0, k.length);
		k = subunit.getBytes("UTF-8"); if (k.length>0) sha1.update(k, 0, k.length);
		k = function.getBytes("UTF-8");if (k.length>0) sha1.update(k, 0, k.length);
		k = surname.getBytes("UTF-8"); if (k.length>0)sha1.update(k, 0, k.length);
		k = middlename.getBytes("UTF-8"); if (k.length>0) sha1.update(k, 0, k.length);
		k = name.getBytes("UTF-8"); if (k.length>0) sha1.update(k, 0, k.length);
		k = note.getBytes("UTF-8"); if (k.length>0) sha1.update(k, 0, k.length);
		
		sha1.doFinal(ret, 0);
		//System.out.println("sha1: "+SecurityHelper.HexDecoder.encode(ret, ':', -1));
		return ret;
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		unsavedChanges = true;
		this.email = email;
	}

	public String getMnemonic() {
		return mnemonic;
	}

	public void setMnemonic(String mnemonic) {
		unsavedChanges = true;
		this.mnemonic = mnemonic;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		unsavedChanges = true;
		this.phone = phone;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		unsavedChanges = true;
		this.country = country;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		unsavedChanges = true;
		this.region = region;
	}

	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		unsavedChanges = true;
		this.postcode = postcode;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		unsavedChanges = true;
		this.company = company;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		unsavedChanges = true;
		this.unit = unit;
	}

	public String getSubunit() {
		return subunit;
	}

	public void setSubunit(String subunit) {
		unsavedChanges = true;
		this.subunit = subunit;
	}

	public String getFunction() {
		return function;
	}

	public void setFunction(String function) {
		unsavedChanges = true;
		this.function = function;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		unsavedChanges = true;
		this.surname = surname;
	}

	public String getMiddlename() {
		return middlename;
	}

	public void setMiddlename(String middlename) {
		unsavedChanges = true;
		this.middlename = middlename;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		unsavedChanges = true;
		this.name = name;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		unsavedChanges = true;
		this.note = note;
	}

	public Vector<DataSourceStep> getDatapath() {
		return datapath;
	}

	public void setDatapath(Vector<DataSourceStep> datapath) {
		unsavedChanges = true;
		this.datapath = datapath;
	}
	
	public boolean hasUnsavedChanges() {
		return unsavedChanges;
	}
}

