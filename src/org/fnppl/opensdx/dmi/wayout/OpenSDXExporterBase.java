package org.fnppl.opensdx.dmi.wayout;


import java.io.File;

import org.fnppl.opensdx.common.BusinessObject;
import org.fnppl.opensdx.common.Feed;
import org.fnppl.opensdx.security.*;
import org.fnppl.opensdx.xml.Document;

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

public class OpenSDXExporterBase {
	public ExportType exportType;
	public Feed exportFeed;
	public File saveFile;
	
	public OpenSDXExporterBase(ExportType expType, Feed expFeed, File savFile)  {
		this.exportType = expType;
		this.exportFeed = expFeed;
		this.saveFile = savFile;
	}
		
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		try {
			
			if(args.length!=3) {
				System.out.println("Please provide following arguments: Type / File to export / File to save");
				System.exit(0);
			}
			
			ExportType expType = ExportType.getExportType(args[0]);
			File expFile = new File(args[1]);
			File savFile = new File(args[2]);
			
			if(!expFile.exists()) {
				System.out.println("ERROR: File to export not exist! Please check and try again.");
				System.exit(0);
			}
			
			Document osdxDoc = Document.fromFile(expFile);
			Feed osdxFeed = Feed.fromBusinessObject(BusinessObject.fromElement(osdxDoc.getRootElement()));
			
			Result ir = null;
			switch(expType.getType()) {
				case ExportType.FINETUNES:
					OpenSDXToFinetunesExporter expFt = new OpenSDXToFinetunesExporter(expType, osdxFeed, savFile);
					ir = expFt.formatToExternalFile();
					break;
				case ExportType.SIMFY:
					if(osdxFeed.getBundleCount()>1) {
						System.out.println("ERROR: File to export contains "+osdxFeed.getBundleCount()+" bundles! Simfy only accept one bundle per feed.");
						System.exit(0);	
					}
					OpenSDXToSimfyExporter expSimfy = new OpenSDXToSimfyExporter(expType, osdxFeed, savFile);
					ir = expSimfy.formatToExternalFile();	
					break;
				default:
					break;
			}
			if(ir.succeeded) {
				System.out.println("Export succeeded! Nice!");
			}
			else {
				System.out.println("Export NOT succeeded! ERROR: "+ir.errorMessage);
			}
		} catch (Exception ex) {
			System.out.println("Failed! Please provide following arguments: Type / File to export / File to save");
		}

	}
}
