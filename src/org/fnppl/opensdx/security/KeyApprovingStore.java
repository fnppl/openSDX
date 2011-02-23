package org.fnppl.opensdx.security;


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

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import org.fnppl.opensdx.xml.*;

public class KeyApprovingStore {
	private File f = null;
	private Vector<OSDXKeyObject> keys = null;
	
	public KeyApprovingStore() {
		
	}
	
	public static KeyApprovingStore fromFile(File f) throws Exception {
		Document d = Document.fromFile(f);
		Element e = d.getRootElement();
		if(!e.getName().equals("keystore")) {
			throw new Exception("KeyStorefile must have \"keystore\" as root-element");
		}
		KeyApprovingStore kas = new KeyApprovingStore();
		Element keys = e.getChild("keys");
		Vector<Element> ves = e.getChildren("keypair");
		
		for(int i=0; i<ves.size(); i++) {
			Element ee = ves.elementAt(i);
			OSDXKeyObject osdxk = OSDXKeyObject.fromElement(ee);
			
		}
		
		return kas;
	}
}

