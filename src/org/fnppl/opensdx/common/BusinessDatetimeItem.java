package org.fnppl.opensdx.common;

/*
 * Copyright (C) 2010-2012 
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
import java.util.Vector;

import org.fnppl.opensdx.xml.Element;

/**
 * 
 * @author Bertram Boedeker <bboedeker@gmx.de>
 * 
 */
public class BusinessDatetimeItem extends BusinessItem {

	final static String RFC1123_CUT = "yyyy-MM-dd HH:mm:ss zzz";
	final static Locale ml = new Locale("en", "DE");
	public final static SimpleDateFormat datemeGMT = new SimpleDateFormat(RFC1123_CUT, ml);
	static {
		datemeGMT.setTimeZone(java.util.TimeZone.getTimeZone("GMT+00:00"));
	}
	public final static SimpleDateFormat datemeLocal = new SimpleDateFormat(RFC1123_CUT, ml);
	static {
		datemeLocal.setTimeZone(java.util.TimeZone.getDefault());
	}

	public BusinessDatetimeItem(String name, long datetime) {
		super(name,datetime);
	}
	
	public BusinessDatetimeItem(String name, String datetime) throws ParseException {
		super(name,datemeGMT.parse(datetime).getTime());
	}
	
	public static BusinessDatetimeItem fromBusinessObject(BusinessObject bo, String name) {
		BusinessStringItem item = bo.handleBusinessStringItem(name);
		if (item==null) {
			return null;
		} else {
			try {
				long l = datemeGMT.parse(item.getString()).getTime();
				BusinessDatetimeItem result = new BusinessDatetimeItem(name, l);
				result.addAttributes(item.getAttributes());
				return result;
			} catch (Exception ex) {
				throw new RuntimeException("wrong datetime fromat: "+item.getString());
			}
		}
	}
	
	public void setDatetime(long datetime) {
		super.set(datetime);
	}
	
	
	public long getDatetime() {
		Object o = super.get();
		if (o instanceof Long) {
			return ((Long)o).longValue();	
		} else {
			throw new RuntimeException("wrong type");
		}
	}
	
	public String getDatetimeStringGMT() {
		return datemeGMT.format(getDatetime());
	}
	
	public String getDatetimeStringLocal() {
		return datemeLocal.format(getDatetime());
	}
		
	public Element toElement() {
		if (get() ==null) return null;
		Element e = new Element(getKeyname(), getDatetimeStringGMT());
		return e;
	}
	
}
