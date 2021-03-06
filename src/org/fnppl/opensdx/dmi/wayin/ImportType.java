package org.fnppl.opensdx.dmi.wayin;

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

public class ImportType {
	public static final int FINETUNES = 1;
	public static final int SIMFY = 2;
	public static final int FUDGE = 3;
	public static final int PIE = 4;
	public static final int DDS = 5;
	public static final int EXACTMOBILE = 6;
	public static final int XF = 7;
	public static final int CLD = 8;
	
	private int type;
	
	private ImportType() {
		this.setType(0);
	}
	
	public static ImportType getImportType(String type) {
		ImportType it = new ImportType();
		if(type.equals("finetunes")) {
			it.setType(FINETUNES);
		}
		else if(type.equals("simfy")) {
			it.setType(SIMFY);
		}	
		else if(type.equals("fudge")) {
			it.setType(FUDGE);
		}
		else if(type.equals("pie")) {
			it.setType(PIE);
		}		
		else if(type.equals("dds")) {
			it.setType(DDS);
		}
		else if(type.equals("exactmobile")) {
			it.setType(EXACTMOBILE);
		}
		else if(type.equals("xf")) {
			it.setType(XF);
		}
		else if(type.equals("cld")) {
			it.setType(CLD);
		}
		return it;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
}
