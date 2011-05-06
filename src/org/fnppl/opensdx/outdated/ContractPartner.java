package org.fnppl.opensdx.outdated;

import org.fnppl.opensdx.xml.Element;


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


/*
 * can be any type of label, publishing house, aggregator, whatever
 * basically, i would recommend even for a "label-contract-partner" to create subunits for that "single-releaser"...
 */


public class ContractPartner extends BaseObjectWithConstraints {

	public ContractPartner() {
		names.add("contractpartnerid"); values.add(null); constraints.add("MUST");
		names.add("ourcontractpartnerid"); values.add(null); constraints.add("MUST");
		names.add("email"); values.add(null); constraints.add("SHOULD");
	}

// methods
	public void setContractpartnerid(String contractpartnerid) {
		set("contractpartnerid", contractpartnerid);
	}

	public String getContractpartnerid() {
		return get("contractpartnerid");
	}

	public void setOurcontractpartnerid(String ourcontractpartnerid) {
		set("ourcontractpartnerid", ourcontractpartnerid);
	}

	public String getOurcontractpartnerid() {
		return get("ourcontractpartnerid");
	}

	public void setEmail(String email) {
		set("email", email);
	}

	public String getEmail() {
		return get("email");
	}

	public Element toElement() {
		return toElement("contractpartner");
	}
	
	public Element toElement(String name) {
		Element e = new Element(name);
		add(e,"contractpartnerid");
		add(e,"ourcontractpartnerid");
		add(e,"email");
		return e;
	}
	
}
