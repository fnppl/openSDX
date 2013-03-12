package org.fnppl.opensdx.common;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.xml.Element;

public class BusinessBytesItem  extends BusinessItem {


	public BusinessBytesItem(String name, byte[] bytes) {
		super(name,bytes);
	}
	
	public static BusinessBytesItem fromBusinessObject(BusinessObject bo, String name) {
		BusinessStringItem item = bo.handleBusinessStringItem(name);
		if (item==null) {
			return null;
		} else {
			try {
				byte[] l = SecurityHelper.HexDecoder.decode(item.getString());
				BusinessBytesItem result = new BusinessBytesItem(name, l);
				result.addAttributes(item.getAttributes());
				return result;
			} catch (Exception ex) {
				throw new RuntimeException("wrong bytes fromat: "+item.getString());
			}
		}
	}
	
	public void setBytes(byte[] bytes) {
		super.set(bytes);
	}
	
	public byte[] getBytes() {
		Object o = super.get();
		if (o==null) throw new RuntimeException("empty value");
		if (o instanceof byte[]) {
			return (byte[])o;	
		} else {
			throw new RuntimeException("wrong type");
		}
	}
	
	public String getString() {
		return SecurityHelper.HexDecoder.encode(getBytes(), ':', -1);
	}
	
	public Element toElement() {
		if (get() ==null) return null;
		Element e = new Element(getKeyname(), getString());
		return e;
	}
}
