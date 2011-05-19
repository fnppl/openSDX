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

import java.util.*;
import org.fnppl.opensdx.xml.*;

public class Identity {
	
	public static String RESTRICTED = "[RESTRICTED]";
	
	private int identnum = 0;
	private String email = null;
	private String mnemonic = null; 			private boolean mnemonic_restricted = true;
	
	private String country = null; 				private boolean country_restricted = true;
	private String region = null; 				private boolean region_restricted = true;
	private String city = null; 				private boolean city_restricted = true;
	private String postcode = null; 			private boolean postcode_restricted = true;
	
	private String company = null; 				private boolean company_restricted = true;
	private String unit = null; 				private boolean unit_restricted = true;
	private String subunit = null; 				private boolean subunit_restricted = true;
	private String function = null; 			private boolean function_restricted = true;
	
	private String surname = null; 				private boolean surname_restricted = true;
	private String middlename = null; 			private boolean middlename_restricted = true;
	private String firstname_s = null; 			private boolean firstname_s_restricted = true;
	private long birthday_gmt = Long.MIN_VALUE; private boolean birthday_gmt_restricted = true;
	private String placeofbirth = null; 		private boolean placeofbirth_restricted = true;
	
	private String phone = null; 				private boolean phone_restricted = true;
	private String fax = null; 					private boolean fax_restricted = true;
	
	private String note = null; 				private boolean note_restricted = true;
	private String photo = null; 				private boolean photo_restricted = true;
	
	private byte[] sha256FromElement = null;
	
	private Vector<DataSourceStep> datapath = null;
	private boolean unsavedChanges = false;
	
	private Identity() {
		
	}
	
	public static Identity newEmptyIdentity() {
		Identity idd = new Identity();
		
		idd.datapath = new Vector<DataSourceStep>();
		idd.unsavedChanges = true;
		return idd;
	}
	
	public Identity derive() {
		Identity idd = new Identity();
		idd.identnum = identnum+1;
		idd.email = email;
		idd.mnemonic = mnemonic;		idd.mnemonic_restricted = mnemonic_restricted;
		
		idd.country = country;			idd.country_restricted = country_restricted;
		idd.region = region;			idd.region_restricted = region_restricted;
		idd.city = city;				idd.city_restricted = city_restricted;
		idd.postcode = postcode;		idd.postcode_restricted = postcode_restricted;
		
		idd.company = company;			idd.company_restricted = company_restricted;
		idd.unit = unit;				idd.unit_restricted = unit_restricted;
		idd.subunit = subunit;			idd.subunit_restricted = subunit_restricted;
		idd.function = function;		idd.function_restricted = function_restricted;
		
		idd.surname = surname;			idd.surname_restricted = surname_restricted;
		idd.middlename = middlename;	idd.middlename_restricted = middlename_restricted;
		idd.firstname_s = firstname_s;	idd.firstname_s_restricted = firstname_s_restricted;
		idd.birthday_gmt = birthday_gmt;idd.birthday_gmt_restricted = birthday_gmt_restricted;
		idd.placeofbirth = placeofbirth;idd.placeofbirth_restricted = placeofbirth_restricted;
		
		idd.phone = phone;				idd.phone_restricted = phone_restricted;
		idd.fax = fax;					idd.fax_restricted = fax_restricted;
		
		idd.note = note;				idd.note_restricted = note_restricted;
		idd.phone = photo;				idd.phone_restricted = photo_restricted;
		
		idd.datapath = new Vector<DataSourceStep>();
		idd.unsavedChanges = true;
		return idd;
	}
	
	public static Identity fromElement(Element id) throws Exception {
		Identity idd = new Identity();
		
		idd.email = id.getChildText("email");
		try {
			idd.identnum = Integer.parseInt(id.getChildText("identnum"));
		} catch (Exception ex) {
			idd.identnum = 0;
			System.out.println("CAUTION: Wrong identnum in identity: "+idd.email);
		}
		idd.mnemonic = id.getChildText("mnemonic");			idd.mnemonic_restricted = getRestricted(id, "mnemonic");
	
		idd.country = id.getChildText("country");			idd.country_restricted = getRestricted(id, "country");
		idd.region = id.getChildText("region");				idd.region_restricted = getRestricted(id, "region");
		idd.city = id.getChildText("city");					idd.city_restricted = getRestricted(id, "city");
		
		idd.postcode = id.getChildText("postcode");			idd.postcode_restricted = getRestricted(id, "postcode");
		idd.company = id.getChildText("company");			idd.company_restricted = getRestricted(id, "company");
		idd.unit = id.getChildText("unit");					idd.unit_restricted = getRestricted(id, "unit");
		idd.subunit = id.getChildText("subunit");			idd.subunit_restricted = getRestricted(id, "subunit");

		idd.function = id.getChildText("function");			idd.function_restricted = getRestricted(id, "function");
		idd.surname = id.getChildText("surname");			idd.surname_restricted = getRestricted(id, "surname");
		idd.middlename = id.getChildText("middlename");		idd.middlename_restricted = getRestricted(id, "middlename");
		idd.firstname_s = id.getChildText("name");			idd.firstname_s_restricted = getRestricted(id, "firstname_s");
		idd.birthday_gmt = id.getChildLong("birthday_gmt");	idd.birthday_gmt_restricted = getRestricted(id, "birthday_gmt");
		idd.placeofbirth = id.getChildText("placeofbirth"); idd.placeofbirth_restricted = getRestricted(id, "placeofbirth");
		
		idd.phone = id.getChildText("phone");				idd.phone_restricted = getRestricted(id, "phone");
		idd.fax = id.getChildText("fax");					idd.fax_restricted = getRestricted(id, "fax");
		
		idd.note = id.getChildText("note");					idd.note_restricted = getRestricted(id, "note");
		idd.photo = id.getChildText("photo");				idd.photo_restricted = getRestricted(id, "photo");
		
		
		idd.sha256FromElement = SecurityHelper.HexDecoder.decode(id.getChildTextNN("sha256"));
		
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
			//System.out.println("CAUTION datasource and datainsertdatetime NOT found.");
		}
		
		return idd;
	}
	
	private static boolean getRestricted(Element id, String keyname) {
		Element c = id.getChild(keyname);
		if (c==null) return true;
		String rest = c.getAttribute("restricted");
		if (rest != null && rest.equalsIgnoreCase("false")) return false;
		return true;
	}
	
	public boolean validate() throws Exception {
		byte[] sha256 = calcSHA256();
		//String ssha1 = SecurityHelper.HexDecoder.encode(sha1, '\0', -1);
		return Arrays.equals(sha256,sha256FromElement);
	}
	
	public Element toElement(boolean showRestricted) {
		Element id = new Element("identity");

		for (Element e : getContentElements(showRestricted)) {
			id.addContent(e);
		}
		
		try {
			byte[] sha256b = calcSHA256();
			id.addContent("sha256", SecurityHelper.HexDecoder.encode(sha256b, ':', -1));
			sha256FromElement = sha256b;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		//datapath
		if (datapath!=null && datapath.size()>0) {
			Element edp = new Element("datapath");
			for (int i=0;i<datapath.size();i++) {
				Element edss = new Element("step"+(i+1));
				edss.addContent("datasource",datapath.get(i).getDataSource());
				edss.addContent("datainsertdatetime", datapath.get(i).getDataInsertDatetimeString());
				edp.addContent(edss);
			}
			id.addContent(edp);
		}
		//unsavedChanges = false;
		return id;
	}
	
	public Vector<Element> getContentElements(boolean allow) {
		Vector<Element> idFields = new Vector<Element>();

		idFields.add(new Element("identnum", getIdentNumString()));
		idFields.add(new Element("email", email));
		addContent(idFields, "mnemonic", mnemonic, mnemonic_restricted, allow);
		
		addContent(idFields, "country", country, country_restricted, allow);
		addContent(idFields, "region", region, region_restricted, allow);
		addContent(idFields, "city", city, city_restricted, allow);
		addContent(idFields, "postcode", postcode, postcode_restricted, allow);
		
		addContent(idFields, "company", company, company_restricted, allow);
		addContent(idFields, "unit", unit, unit_restricted, allow);
		addContent(idFields, "subunit", subunit, subunit_restricted, allow);
		addContent(idFields, "function", function, function_restricted, allow);
		
		addContent(idFields, "surname", surname, surname_restricted, allow);
		addContent(idFields, "middlename", middlename, middlename_restricted, allow);
		addContent(idFields, "firstname_s", firstname_s, firstname_s_restricted, allow);
		
		if (birthday_gmt!=Long.MIN_VALUE) addContent(idFields, "birthday_gmt", SecurityHelper.getFormattedDate(birthday_gmt), birthday_gmt_restricted, allow);
		addContent(idFields, "placeofbirth", placeofbirth, placeofbirth_restricted, allow);
		
		addContent(idFields, "phone", phone, phone_restricted, allow);
		addContent(idFields, "fax", fax, fax_restricted, allow);
		
		addContent(idFields, "note", note, note_restricted, allow);
		addContent(idFields, "photo", photo, photo_restricted, allow);

		return idFields;
	}
	
	private static void addContent(Vector<Element> idFields, String keyname, String value, boolean restricted, boolean allow) {
		if (value!=null) {
			Element e;
			if (restricted && !allow) {
				e = new Element(keyname, RESTRICTED);
			} else {
				e = new Element(keyname, value);
			}
			e.setAttribute("restricted", ""+restricted);
			idFields.add(e);
		}
	}
	
//	public Element toElement() {
//		Element id = new Element("identity");
//
//		id.addContent("identnum", getIdentNumString());
//		id.addContent("email", email);
//		id.addContent("mnemonic", mnemonic);
//		
//		id.addContent("country", country);
//		id.addContent("region", region);
//		id.addContent("city", city);
//		id.addContent("postcode", postcode);
//		
//		id.addContent("company", company);
//		id.addContent("unit", unit);
//		id.addContent("subunit", subunit);
//		id.addContent("function", function);
//		
//		id.addContent("surname", surname);
//		id.addContent("middlename", middlename);
//		id.addContent("name", firstname_s);
//		
//		id.addContent("phone", phone);
//		id.addContent("note", note);
//		try {
//			byte[] sha256b = calcSHA256();
//			id.addContent("sha256", SecurityHelper.HexDecoder.encode(sha256b, ':', -1));
//			sha256FromElement = sha256b;
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//		
//		//datapath
//		Element edp = new Element("datapath");
//		if (datapath !=null) {
//			for (int i=0;i<datapath.size();i++) {
//				Element edss = new Element("step"+(i+1));
//				edss.addContent("datasource",datapath.get(i).getDataSource());
//				edss.addContent("datainsertdatetime", datapath.get(i).getDataInsertDatetimeString());
//				edp.addContent(edss);
//			}
//		}
//		id.addContent(edp);
//		unsavedChanges = false;
//		return id;
//	}
	
	
	public boolean validate(byte[] sha256b) throws Exception {
		byte[] _sha256b = calcSHA256();
		return Arrays.equals(_sha256b, sha256b);
	}
	
	
	public byte[] calcSHA256() throws Exception {
		return SecurityHelper.getSHA256LocalProof(getContentElements(true));
	}
	
	public void createSHA256() {
		try {
			sha256FromElement = calcSHA256();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public String getIdentNumString() {
		String sNo = ""+identnum;
		while (sNo.length()<4) sNo = "0"+sNo;
		return sNo;
	}
	public int getIdentNum() {
		return identnum;
	}
	
	public void setIdentNum(int i) {
		unsavedChanges = true;
		identnum = i;
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
	
	public boolean isMnemonicRestricted() {
		return mnemonic_restricted;
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
	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		unsavedChanges = true;
		this.city = city;
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

	public String getFirstNames() {
		return firstname_s;
	}

	public void setName(String name) {
		unsavedChanges = true;
		this.firstname_s = name;
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
	
	public boolean is_mnemonic_restricted() {
		return mnemonic_restricted;
	}

	public void set_mnemonic_restricted(boolean restricted) {
		mnemonic_restricted = restricted;
	}

	public boolean is_country_restricted() {
		return country_restricted;
	}

	public void set_country_restricted(boolean restricted) {
		country_restricted = restricted;
	}

	public boolean is_region_restricted() {
		return region_restricted;
	}

	public void set_region_restricted(boolean restricted) {
		region_restricted = restricted;
	}

	public boolean is_city_restricted() {
		return city_restricted;
	}

	public void set_city_restricted(boolean restricted) {
		city_restricted = restricted;
	}

	public boolean is_postcode_restricted() {
		return postcode_restricted;
	}

	public void set_postcode_restricted(boolean restricted) {
		postcode_restricted = restricted;
	}

	public boolean is_company_restricted() {
		return company_restricted;
	}

	public void set_company_restricted(boolean restricted) {
		company_restricted = restricted;
	}

	public boolean is_unit_restricted() {
		return unit_restricted;
	}

	public void set_unit_restricted(boolean restricted) {
		unit_restricted = restricted;
	}

	public boolean is_subunit_restricted() {
		return subunit_restricted;
	}

	public void set_subunit_restricted(boolean restricted) {
		subunit_restricted = restricted;
	}

	public boolean is_function_restricted() {
		return function_restricted;
	}

	public void set_function_restricted(boolean restricted) {
		function_restricted = restricted;
	}

	public boolean is_surname_restricted() {
		return surname_restricted;
	}

	public void set_surname_restricted(boolean restricted) {
		surname_restricted = restricted;
	}

	public boolean is_middlename_restricted() {
		return middlename_restricted;
	}

	public void set_middlename_restricted(boolean restricted) {
		middlename_restricted = restricted;
	}

	public boolean is_firstname_s_restricted() {
		return firstname_s_restricted;
	}

	public void set_firstname_s_restricted(boolean restricted) {
		firstname_s_restricted = restricted;
	}

	public boolean is_birthday_gmt_restricted() {
		return birthday_gmt_restricted;
	}

	public void set_birthday_gmt_restricted(boolean restricted) {
		birthday_gmt_restricted = restricted;
	}

	public boolean is_placeofbirth_restricted() {
		return placeofbirth_restricted;
	}

	public void set_placeofbirth_restricted(boolean restricted) {
		placeofbirth_restricted = restricted;
	}

	public boolean is_phone_restricted() {
		return phone_restricted;
	}

	public void set_phone_restricted(boolean restricted) {
		phone_restricted = restricted;
	}

	public boolean is_fax_restricted() {
		return fax_restricted;
	}

	public void set_fax_restricted(boolean restricted) {
		fax_restricted = restricted;
	}

	public boolean is_note_restricted() {
		return note_restricted;
	}

	public void set_note_restricted(boolean restricted) {
		note_restricted = restricted;
	}

	public boolean is_photo_restricted() {
		return photo_restricted;
	}

	public void set_photo_restricted(boolean restricted) {
		photo_restricted = restricted;
	}
	
	public boolean hasUnsavedChanges() {
		return unsavedChanges;
	}
}

