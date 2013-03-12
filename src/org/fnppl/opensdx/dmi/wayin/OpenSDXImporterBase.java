package org.fnppl.opensdx.dmi.wayin;

import java.io.File;
import java.sql.Savepoint;

import org.fnppl.opensdx.dmi.FeedValidator;
import org.fnppl.opensdx.security.*;

/*
 * Copyright (C) 2010-2013 
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
			
			if(args.length!=3) {
				System.out.println("Please provide following arguments: Type / File to import / File to save");
				System.exit(0);
			}
			
			ImportType impType = ImportType.getImportType(args[0].toLowerCase().trim());
			File impFile = new File(args[1].trim());
			File savFile = new File(args[2].trim());
			
			if(!impFile.exists()) {
				System.out.println("ERROR: File to import not exist! Please check and try again.");
				System.exit(0);
			}
			
			Result ir = null;
			switch(impType.getType()) {
				case ImportType.FINETUNES:
					FinetunesToOpenSDXImporter impFt = new FinetunesToOpenSDXImporter(impType, impFile, savFile);
					ir = impFt.formatToOpenSDXFile();
					break;
				case ImportType.SIMFY:
					SimfyToOpenSDXImporter impSimfy = new SimfyToOpenSDXImporter(impType, impFile, savFile);
					ir = impSimfy.formatToOpenSDXFile();		
					break;
				case ImportType.FUDGE:
					FudgeToOpenSDXImporter impFudge = new FudgeToOpenSDXImporter(impType, impFile, savFile);
					ir = impFudge.formatToOpenSDXFile();		
					break;	
				case ImportType.PIE:
					PieToOpenSDXImporter impPie = new PieToOpenSDXImporter(impType, impFile, savFile);
					ir = impPie.formatToOpenSDXFile();		
					break;	
				case ImportType.DDS:
					DDSToOpenSDXImporter impDDS = new DDSToOpenSDXImporter(impType, impFile, savFile);
					ir = impDDS.formatToOpenSDXFile();		
					break;
				case ImportType.EXACTMOBILE:
					ExactMobileToOpenSDXImporter impEM = new ExactMobileToOpenSDXImporter(impType, impFile, savFile);
					ir = impEM.formatToOpenSDXFile();		
					break;	
				case ImportType.XF:
					XFToOpenSDXImporter xfEM = new XFToOpenSDXImporter(impType, impFile, savFile);
					ir = xfEM.formatToOpenSDXFile();		
					break;	
				case ImportType.CLD:
					CLDToOpenSDXImporter impCLD = new CLDToOpenSDXImporter(impType, impFile, savFile);
					ir = impCLD.formatToOpenSDXFile();		
					break;		
				default:
					break;
			}
			if(ir.succeeded) {
				System.out.println("Import succeeded! Nice!");
				System.out.println("But what about validation? Lets have a look.\n");
				System.out.println("#+++++++++++++++++++++++++++++++++++++++++++++++++++#\n");
				System.out.println(new FeedValidator().validateOSDX_0_0_1(savFile));
				System.out.println("#+++++++++++++++++++++++++++++++++++++++++++++++++++#\n");
				
			}
			else {
				System.out.println("Import NOT succeeded! ERROR: "+ir.errorMessage);
			}
		} catch (Exception ex) {
			System.out.println("Failed! Please provide following arguments: Type / File to import / File to save");
		}

	}
}
