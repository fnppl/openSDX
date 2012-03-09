package org.fnppl.opensdx.keyserver.helper;
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
import java.sql.Timestamp;

public class SQLStatement {

	private String[] sql_parts = null;
	private String[] sql_values = null;
	
	public SQLStatement(String sql) {
		sql_parts = sql.split("[?]");
		sql_values = new String[sql_parts.length];
		for (int i=0;i<sql_values.length;i++) {
			sql_values[i] = null;
		}
	}
	
	public void setString(int pos, String value) {
		if (value==null) {
			sql_values[pos-1] = "NULL";
		} else {
			sql_values[pos-1] = dbEncodeInApostrophes(value);	
		}
	}
	
	public void setBoolean(int pos, boolean b) {
		sql_values[pos-1] = dbEncodeInApostrophes(""+b);
	}
	
	public void setInt(int pos, int value) {
		sql_values[pos-1] = dbEncodeInApostrophes(""+value);
	}
	
	public void setLong(int pos, long value) {
		sql_values[pos-1] = dbEncodeInApostrophes(""+value);
	}
	
	public void setTimestamp(int pos, Timestamp value) {
		if (value==null) {
			sql_values[pos-1] = "NULL";
		} else {
			sql_values[pos-1] = dbEncodeInApostrophes(value.toString());
		}
	}
	
	public String toString() {
		StringBuffer b = new StringBuffer();
		for (int i=0;i<sql_parts.length;i++) {
			b.append(sql_parts[i]);
			if (sql_values[i]!=null)
				b.append(sql_values[i]);
		}
		return b.toString();
	}
	
	public static String dbEncodeInApostrophes(String s) {
		StringBuffer ret = new StringBuffer();
		ret.append('\'');
		if(s == null) s = "";
		for(int i=0;i<s.length();i++) {
			char c = s.charAt(i);
			if(c == '\'') {
				ret.append('\'');//HT 15.01.2008 scheint die neue syntax zu sein
			}
			ret.append(c);
		}
		ret.append('\'');
		
		return ret.toString();
	}

}
