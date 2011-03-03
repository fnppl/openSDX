package org.fnppl.opensdx.xml;


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

import java.io.*;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;

public class Document {
	private org.jdom.Document base = null;
	private Element rt = null;
	
	private Document() {
		
	}
	private Document(org.jdom.Document dc) {
		this.base = dc;
		rt = new Element(base.getRootElement());
		
	}
	
	public static Document fromFile(File f) throws Exception {
//		XMLHelper.fromFile(f);
		
		SAXBuilder sax = new SAXBuilder();		
		Document ret = new Document(sax.build(f));
		
		return ret;
	}
	
	public Element getRootElement() {		
		return rt;
	}
	
	public static Document buildDocument(Element root) {
		Document d = new Document();
		d.base = null;
		d.rt = root;
		return d;
	}
	
	public void writeToFile(File file) throws Exception {
		FileOutputStream out = new FileOutputStream(file);
		String head = "<?xml version= \"1.0\" encoding=\"UTF-8\"?>\n";
		out.write(head.getBytes("UTF-8"));
		rt.output(out);
		out.flush();
		out.close();
	}
}


