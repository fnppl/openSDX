package org.fnppl.opensdx.xml;

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

/*
 * basically this class mimics the org.jdom-stuff.
 * why is it here? because we want to be independent of jdoms-implementation!
 * 
 */

import java.io.*;
import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;

public class Document {
	org.jdom.Document base = null;
	
	private Document() {
		
	}
	private Document(org.jdom.Document dc) {
		this.base = dc;
	}
	
	public Document fromFile(File f) throws Exception {
//		XMLHelper.fromFile(f);
		
		SAXBuilder sax = new SAXBuilder();		
		Document ret = new Document(sax.build(f));
		
		return ret;
	}
	
	public Element getRootElement() {
		//HT 22.02.2011 - be aware of multiple calls...
		return new Element(base.getRootElement());
	}
}


