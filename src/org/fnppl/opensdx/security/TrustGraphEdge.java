package org.fnppl.opensdx.security;
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

public class TrustGraphEdge {
	
	public static int TYPE_UNKNOWN = 0;
	public static int TYPE_APPROVE = 1;
	public static int TYPE_SUBKEY = 2;
	public static int TYPE_DISAPPROVE = -2;
	public static int TYPE_REVOKE = -1;
	public static int TYPE_PATH = 99;
	
//	public TrustGraphNode from;
//	public TrustGraphNode to;
//	public int type;
//	public long datetime;
//	
//	public TrustGraphEdge(TrustGraphNode from, TrustGraphNode to, int type, long datetime) {
//		super();
//		this.from = from;
//		this.to = to;
//		this.type = type;
//		this.datetime = datetime;
//	}
	
	public String keyid_from;
	public String keyid_to;
	public int type;
	public long datetime;
	
	public TrustGraphEdge(String keyid_from, String keyid_to, int type, long datetime) {
		super();
		this.keyid_from = keyid_from;
		this.keyid_to = keyid_to;
		this.type = type;
		this.datetime = datetime;
	}
}