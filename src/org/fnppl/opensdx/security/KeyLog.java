package org.fnppl.opensdx.security;

import java.util.Arrays;
import java.util.Vector;

import org.fnppl.opensdx.xml.Element;
import org.fnppl.opensdx.xml.XMLHelper;

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

public class KeyLog {

	private Element ekeylog;
	
	
	private KeyLog() {
		
	}

	public boolean verifySHA1localproofAndSignoff() throws Exception{
		//check localproof
		byte[] sha1localproof =  SecurityHelper.HexDecoder.decode(ekeylog.getChildText("sha1localproof"));
		byte[] bsha1 = SecurityHelper.getSHA1LocalProof(ekeylog.getChildren("action"));
		
		if (!Arrays.equals(bsha1, sha1localproof)) {
			System.out.println("sha1localproof target: "+SecurityHelper.HexDecoder.encode(sha1localproof, '\0', -1));
			System.out.println("sha1localproof real  : "+SecurityHelper.HexDecoder.encode(bsha1, '\0', -1));
			return false;
		}
		
		//check signoff
		Element signoff = ekeylog.getChild("signoff");
		return SignoffElement.verifySignoff(signoff, bsha1);
	}
	
	public void signoff(OSDXKeyObject key) throws Exception {
		byte[] bsha1 = SecurityHelper.getSHA1LocalProof(ekeylog.getChildren("action"));
		SignoffElement s = SignoffElement.getSignoffElement(bsha1, key);
		Element e = new Element("keylog");
		Vector<Element> ea = ekeylog.getChildren("action");
		for (Element el : ea) {
			e.addContent(XMLHelper.cloneElement(el));
		}
		e.addContent("sha1localproof",SecurityHelper.HexDecoder.encode(bsha1, ':', -1));
		e.addContent(s);
		ekeylog = e;
	}
	
	
	public static KeyLog fromElement(Element e)  throws Exception {
		KeyLog k = new KeyLog();
		k.ekeylog = e;
		
		boolean verifySignoff = k.verifySHA1localproofAndSignoff();
		//verifySignoff = true;
		if(!verifySignoff) {
			throw new Exception("KeyStore:  localproof and signoff of keylog failed.");
		}
		
		return k;
	}
	
	public Element toElement() {
	
		return XMLHelper.cloneElement(ekeylog);
	}
}
