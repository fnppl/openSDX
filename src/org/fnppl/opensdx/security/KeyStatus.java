package org.fnppl.opensdx.security;

import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Vector;

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

public class KeyStatus {
	public KeyLog referencedKeyLog = null;
	
	public static int STATUS_UNAPPROVED = 0;
	public static int STATUS_VALID = 1;
	public static int STATUS_REVOKED = 2;
	public static int STATUS_OUTDATED = 3;
	public static int STATUS_USAGE_NOT_ALLOWED = 4;
	public static int STATUS_KEY_NOT_FOUND = 5;
	
	public static final Vector<String> VALIDITY_NAME = new Vector<String>();
	static {
		VALIDITY_NAME.addElement("unapproved");
		VALIDITY_NAME.addElement("valid");
		VALIDITY_NAME.addElement("revoked");
		VALIDITY_NAME.addElement("outdated");
		VALIDITY_NAME.addElement("usage not allowed");
		VALIDITY_NAME.addElement("key not found");
	};
	
	private int validityStatus;
		
	private int approvalPoints; //from start at 100 up to 200 down to 0 
	private long validFrom;
	private long validUntil;
	
	private KeyStatus() {
		approvalPoints = 100;
		validFrom = System.currentTimeMillis();
		validUntil = validFrom;
	}
	
	public static KeyStatus getKeyStatus(OSDXKey key, Vector<KeyLog> keylogs, String usage, long datetime, String keyidKeyserver) {
		String keyid = key.getKeyID();
		System.out.println("Checking keyvalid:\n  keyid: "+keyid+"\n  usage: "+(usage==null?"not checked":usage)+"\n  datetime: "+SecurityHelper.getFormattedDate(datetime));
		
		boolean valid = false;
		//check usage
		if (usage!=null) {
			if (usage.equals("sign") && key.allowsSignatures()) valid = true;
			else if (usage.equals("crypt") && key.allowsCrypt()) valid = true;
			else if (usage.equals("both") && key.getUsage()==OSDXKey.USAGE_WHATEVER) valid = true;
		} else {
			valid = true;
		}
			
		if (!valid) {
			return new KeyStatus(STATUS_USAGE_NOT_ALLOWED, 0, key.getValidFrom(), key.getValidUntil(), null);
		}
			
		 //check timeframe
		 System.out.println("timeframe: "+SecurityHelper.getFormattedDate(key.getValidFrom())+"  ...   "+SecurityHelper.getFormattedDate(key.getValidUntil()));
		 if (key.getValidFrom() > datetime || datetime > key.getValidUntil()) {
			 return new KeyStatus(STATUS_OUTDATED, 0, key.getValidFrom(), key.getValidUntil(), null);
		 }
			 
		 //check keyserver approval and not revoked
		 System.out.println("Keyserverkeyid: "+keyidKeyserver);
		 String alternativeKeyIDKeyserver = null;
		 boolean hasKeyServerApproval = false;
		 boolean approval_pending = false;
		 KeyLog referenced = null;
		 for (KeyLog kl : keylogs) {
			 System.out.println("KEYLOG :: "+kl.getKeyIDFrom()+" -> "+kl.getKeyIDTo()+" "+kl.getAction()+" "+kl.getActionDatetimeString());
			 if (kl.getAction().equals(KeyLogAction.REVOCATION)) {
				 return new KeyStatus(STATUS_REVOKED, 0, key.getValidFrom(), key.getValidUntil(), kl);
			 }
			 else if (kl.getAction().equals(KeyLogAction.APPROVAL_PENDING)) {
				 alternativeKeyIDKeyserver = kl.getKeyIDFrom();
				 approval_pending = true;
			 }
			 else if (kl.getAction().equals(KeyLogAction.APPROVAL)) {
				 if (keyidKeyserver!=null && kl.getKeyIDFrom().equals(keyidKeyserver)) {
					 hasKeyServerApproval = true;
					 referenced = kl;
				 }
				 else if (alternativeKeyIDKeyserver!=null && kl.getKeyIDFrom().equals(alternativeKeyIDKeyserver)) {
					 hasKeyServerApproval = true;
					 referenced = kl;
				 }
			 }
		 }
		 if (hasKeyServerApproval) { //and not revoked
			 return new KeyStatus(STATUS_VALID, 100, key.getValidFrom(), key.getValidUntil(), referenced);
		 }
		 else if (approval_pending) { //and not keyserver approval and not revoked
			 return new KeyStatus(STATUS_UNAPPROVED, 100, key.getValidFrom(), key.getValidUntil(), referenced);
		 } else {
			 //this should not happen, since the keyserver should always build the approval_pending keylog 
			 return new KeyStatus(STATUS_UNAPPROVED, 100, key.getValidFrom(), key.getValidUntil(), null);
		 }
	}
	
	public KeyStatus(int validity, int approvalPoints, long datetimeValidFrom, long datetimeValidUntil, KeyLog kl) {
		validityStatus = validity;
		this.approvalPoints = approvalPoints;
		validFrom = datetimeValidFrom;
		validUntil = datetimeValidUntil;
		this.referencedKeyLog = kl;
	}
	
	public static KeyStatus fromElement(Element e) throws Exception {
		KeyStatus k = new KeyStatus();
		k.validityStatus = VALIDITY_NAME.indexOf(e.getChildText("validity_status"));
		k.approvalPoints = e.getChildInt("approval_points");
		k.validFrom = SecurityHelper.parseDate(e.getChildText("valid_from"));
		k.validUntil = SecurityHelper.parseDate(e.getChildText("valid_until"));
		return k;
	}
	
	public Element toElement() {
		Element e = new Element("keystatus");
		e.addContent("validity_status", VALIDITY_NAME.get(validityStatus));
		e.addContent("approval_points",""+approvalPoints);
		e.addContent("valid_from",SecurityHelper.getFormattedDate(validFrom));
		e.addContent("valid_until",SecurityHelper.getFormattedDate(validUntil));	
		return e;
	}
	
	public int getValidityStatus() {
		return validityStatus;
	}

	public String getValidityStatusName() {
		return VALIDITY_NAME.get(validityStatus);
	}
	
	public boolean isValid() {
		if (validityStatus == STATUS_VALID) return true;
		else return false;
	}
	
	public boolean isUnapproved() {
		if (validityStatus == STATUS_UNAPPROVED) return true;
		else return false;
	}
}
