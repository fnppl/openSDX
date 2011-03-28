package org.fnppl.opensdx.security;

import java.text.ParseException;

import org.fnppl.opensdx.xml.Element;
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
 * For those parts of this file, which are identified as software, rather than documentation, this software-license applies / shall be applied. 
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
 * For those parts of this file, which are identified as documentation, rather than software, this documentation-license applies / shall be applied.
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

public class DataSourceStep {
	
	private String datasource = null;
	private long datainsertdatetime = -1;
	
	private DataSourceStep() {
		
	}
	
	public DataSourceStep(String source, long datetime) {
		datasource = source;
		datainsertdatetime = datetime;
	}
	
	public static DataSourceStep fromElemet(Element step) throws Exception {
		DataSourceStep s = new DataSourceStep();
		s.datasource = step.getChildText("datasource");
		String datainsertdatetime = step.getChildText("datainsertdatetime");
		s.datainsertdatetime = SecurityHelper.parseDate(datainsertdatetime);
		return s;
	}
	
	public Element toElement(int ind) {
		Element edss = new Element("step"+(ind+1));
		edss.addContent("datasource", datasource);
		edss.addContent("datainsertdatetime", getDataInsertDatetimeString());
		return edss;
	}
	
	public String getDataSource() {
		return datasource;
	}
	
	public String getDataInsertDatetimeString() {
		return SecurityHelper.getFormattedDate(datainsertdatetime);
	}
	
	public long getDataInsertDatetime() {
		return datainsertdatetime;
	}
}
