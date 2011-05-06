package org.fnppl.opensdx.common;

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

/**
 * 
 * @author Bertram Boedeker <bboedeker@gmx.de>
 * 
 */
public class ContractPartner extends BusinessObject {

	public static int ROLE_SENDER = 0;
	public static int ROLE_LICENSOR = 1;
	
	private int role = -1;

	
	private BusinessStringItem contractpartnerid;		 //MUST
	private BusinessStringItem ourcontractpartnerid; 	//MUST
	private BusinessStringItem email; 					//SHOULD
	
	private ContractPartner() {
		
	}
	public static ContractPartner make(int role, String contractpartnerid, String ourcontractpartnerid) {
		ContractPartner p = new ContractPartner();
		p.role = role;
		p.contractpartnerid = new BusinessStringItem("contractpartnerid", contractpartnerid);
		p.ourcontractpartnerid = new BusinessStringItem("ourcontractpartnerid", ourcontractpartnerid);
		p.email = null;
		return p;
	}

	public ContractPartner email(String email) {
		this.email = new BusinessStringItem("email", email);
		return this;
	}
	
	public int getRole() {
		return role;
	}
	
	public String getKeyname() {
		if (role == ROLE_SENDER) return "sender";
		if (role == ROLE_LICENSOR) return "licensor";
		return "contract_partner";
	}
	
	
}
