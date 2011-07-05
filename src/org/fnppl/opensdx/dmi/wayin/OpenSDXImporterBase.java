package org.fnppl.opensdx.dmi.wayin;

import java.io.File;

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

public class OpenSDXImporterBase {	
	public ImportType importType;
	public File importFile;
	public File saveFile;
	
	public OpenSDXImporterBase(ImportType impType, File impFile, File savFile)  {
		this.importType = impType;
		this.importFile = impFile;
		this.saveFile = savFile;
	}
		
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		try {
			System.out.println("Testing import for: "+args[0]);
			System.out.println("File to import: "+args[1]);
			System.out.println("File to save: "+args[2]);
			
			if(args.length!=3) {
				System.out.println("Please provide following arguments: Type / File to import / File to save");
				System.exit(0);
			}
			
			ImportType impType = ImportType.getImportType(args[0]);
			File impFile = new File(args[1]);
			File savFile = new File(args[2]);
			
			ImportResult ir = null;
			switch(impType.getType()) {
				case ImportType.FINETUNES:
					FinetunesToOpenSDXImporter impFt = new FinetunesToOpenSDXImporter(impType, impFile, savFile);
					ir = impFt.formatToOpenSDX();
					break;
				case ImportType.SIMFY:
					SimfyToOpenSDXImporter impSimfy = new SimfyToOpenSDXImporter(impType, impFile, savFile);
					ir = impSimfy.formatToOpenSDX();		
					break;
				default:
					break;
			}
			
			System.out.println("Import succeeded? "+ir.succeeded);
		} catch (Exception ex) {
			System.out.println("Failed! Please provide following arguments: Type / File to import / File to save");
			ex.printStackTrace();
		}

	}
}
