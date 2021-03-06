package org.fnppl.opensdx.common;

import org.fnppl.opensdx.xml.Element;

/*
 * Copyright (C) 2010-2015 
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

/**
 * 
 * @author Bertram Boedeker <bboedeker@gmx.de>
 * 
 */
public class ContractPartner extends BusinessObject {

	public static int ROLE_CONTRACT_PARTNER = -1;  public static String KEY_NAME_CONTRACT_PARTNER = "contract_partner";
	public static int ROLE_SENDER = 0;             public static String KEY_NAME_SENDER = "sender";
	public static int ROLE_LICENSOR = 1;           public static String KEY_NAME_LICENSOR = "licensor";
	public static int ROLE_LICENSEE = 2;           public static String KEY_NAME_LICENSEE = "licensee";
	
	
	private int role = -1;

	
	private BusinessStringItem contractpartnerid;		//MUST
	private BusinessStringItem ourcontractpartnerid; 	//MUST
	private BusinessStringItem email; 					//SHOULD
	private BusinessStringItem keyid; 					//SHOULD
	
	
	private ContractPartner() {
		
	}
	
	/***
	 * the make method constructs a ContractPartner object with the following MUST have attributes 
	 * @param role :: see e.g. ROLE_SENDER, ROLE_LICENSOR
	 * @param contractpartnerid :: your side of "ID" ; worst case: the company's correct trade-register-name/number 
	 * @param ourcontractpartnerid :: our side of "ID"
	 * @return newly instantiated ContractPartner in given role
	 * 
	 * other fields in ContractPartner:
	 * 	email :: for SENDER  :: MUST esp. for signature-check ; could of course be a generic account e.g. deliveryteam@dsphouse.nät
	 *  email :: for LICENSOR:: SHOULD good, but not necessarily needed
	 */
	public static ContractPartner make(int role, String contractpartnerid, String ourcontractpartnerid) {
		ContractPartner p = new ContractPartner();
		p.role = role;
		p.contractpartnerid = new BusinessStringItem("contractpartnerid", contractpartnerid);
		p.ourcontractpartnerid = new BusinessStringItem("ourcontractpartnerid", ourcontractpartnerid);
		p.email = null;
		p.keyid = null;
		return p;
	}
	
	public ContractPartner contractpartnerid(String value) {
		contractpartnerid.setString(value);
		return this;
	}
	
	public ContractPartner ourcontractpartnerid(String value) {
		if (value == null) {
			ourcontractpartnerid.setString("");
		} else {
			ourcontractpartnerid.setString(value);
		}
		return this;
	}
	
	public ContractPartner role(int role) {
		this.role = role;
		return this;
	}
	
	public static ContractPartner fromBusinessObject(BusinessObject bo, int role) {
		if (bo==null) return null;
		if (!bo.getKeyname().equals(getKeyname(role))) {
			bo = bo.handleBusinessObject(getKeyname(role));
		}
		if (bo==null) return null;
		
		ContractPartner p = new ContractPartner();
		p.initFromBusinessObject(bo);
		p.role = role;
		p.contractpartnerid = BusinessStringItem.fromBusinessObject(p,"contractpartnerid");
		p.ourcontractpartnerid = BusinessStringItem.fromBusinessObject(p,"ourcontractpartnerid");
		if (p.contractpartnerid==null) {
			p.contractpartnerid = new BusinessStringItem("contractpartnerid", "");
		}
		if (p.ourcontractpartnerid==null) {
			p.ourcontractpartnerid = new BusinessStringItem("ourcontractpartnerid", "");
		}
		p.email = BusinessStringItem.fromBusinessObject(p,"email");
		p.keyid = BusinessStringItem.fromBusinessObject(p, "keyid");
		return p;
	}

	public ContractPartner email(String email) {
		if (email==null) {
			this.email = null;
		} else {
			this.email = new BusinessStringItem("email", email);
		}
		return this;
	}
	public String getEmail() {
		if (email==null) return null;
		return email.getString();
	}
	
	public ContractPartner keyid(String value) {
		if (value==null) {
			this.keyid = null;
		} else {
			this.keyid = new BusinessStringItem("keyid", value);
		}
		return this;
	}
	public String getKeyid() {
		if (keyid==null) return null;
		return keyid.getString();
	}
	
	public int getRole() {
		return role;
	}
	
	public static String getKeyname(int role) {
		if (role == ROLE_SENDER) return KEY_NAME_SENDER;
		if (role == ROLE_LICENSOR) return KEY_NAME_LICENSOR;
		if (role == ROLE_LICENSEE) return KEY_NAME_LICENSEE;
		return KEY_NAME_CONTRACT_PARTNER;
	}
	
	public String getKeyname() {
		return getKeyname(role);
//		if (role == ROLE_SENDER) return KEY_NAME_SENDER;
//		if (role == ROLE_LICENSOR) return KEY_NAME_LICENSOR;
//		if (role == ROLE_LICENSEE) return KEY_NAME_LICENSEE;
//		return KEY_NAME_CONTRACT_PARTNER;
	}
	
	public String getContractPartnerID() {
		if (contractpartnerid==null) return null;
		return contractpartnerid.getString();
	}
	
	public String getOurContractPartnerID() {
		if (ourcontractpartnerid==null) return null;
		return ourcontractpartnerid.getString();
	}
	
}
