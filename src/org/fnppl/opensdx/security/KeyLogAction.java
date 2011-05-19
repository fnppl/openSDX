package org.fnppl.opensdx.security;

import org.fnppl.opensdx.xml.Element;

import com.sun.org.apache.bcel.internal.generic.GETSTATIC;

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

public class KeyLogAction {

	private String fromKeyid = null;
	private OSDXKey fromKey = null;
	private String toKeyid = null;
	
	private String action; // approval/disapproval/revocation/approval pending
	
	private Identity id = null;
	private String message = null;
	private byte[] sha256localproof_complete = null;
	private byte[] sha256localproof_restricted = null;
	private Signature signature = null;

	private KeyLogAction() {
		
	}
	
	public static KeyLogAction buildKeyLogAction(String action, OSDXKey from, String toKeyID, Identity id) throws Exception {
		KeyLogAction a  = new KeyLogAction();
		a.action = action;
		a.id = id;
		a.message = null;
		a.fromKey = from;
		a.fromKeyid = from.getKeyID();
		a.toKeyid = toKeyID;
		a.sha256localproof_complete = a.getSha1LocalProof(true);
		a.sha256localproof_restricted = a.getSha1LocalProof(false);
		
		//signature
		byte[] localproof = SecurityHelper.concat(a.sha256localproof_complete, a.sha256localproof_restricted);
		a.signature = Signature.createSignatureFromLocalProof(localproof, "signature of sha256localproof_complete + sha256localproof_restricted", from); 
		return a;
	}
	
	public Element toElement(boolean showRestricted) {
		Element e = getElementWithoutSignature(showRestricted);
		e.addContent("sha256localproof_complete", SecurityHelper.HexDecoder.encode(sha256localproof_complete,':',-1));
		e.addContent("sha256localproof_restricted", SecurityHelper.HexDecoder.encode(sha256localproof_restricted,':',-1));
		e.addContent(signature.toElement());
		return e;
	}
	
	private byte[] getSha1LocalProof(boolean showRestricted) throws Exception {
		return SecurityHelper.getSHA256LocalProof(getElementWithoutSignature(showRestricted));
	}
		
	
	private Element getElementWithoutSignature(boolean showRestricted) {
		Element e = new Element("keylogaction");
		e.addContent("from_keyid",fromKey.getKeyID());
		e.addContent("to_keyid",toKeyid);
		Element ea =  new Element(action);
		e.addContent(ea);
		ea.addContent(id.toElement(showRestricted));
		return e;
	}
}
