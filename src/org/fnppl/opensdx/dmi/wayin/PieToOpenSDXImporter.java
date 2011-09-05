package org.fnppl.opensdx.dmi.wayin;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.fnppl.opensdx.common.*;
import org.fnppl.opensdx.dmi.GenreConverter;
import org.fnppl.opensdx.xml.*;
import org.fnppl.opensdx.security.*;

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

public class PieToOpenSDXImporter extends OpenSDXImporterBase {
	DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
	private Result ir = Result.succeeded();
	// test?
	boolean onlytest = true;
    
	public PieToOpenSDXImporter(ImportType type, File impFile, File savFile) {
		super(type, impFile, savFile);
	}
	
	public PieToOpenSDXImporter(File impFile) {
		super(ImportType.getImportType("pie"), impFile, null);
	}
	
	public Result formatToOpenSDXFile() {
		try {			
			
			Feed feed = this.getImportFeed();
            
			if(feed!=null) {			
	            // write file
				Document doc = Document.buildDocument(feed.toElement());
				doc.writeToFile(this.saveFile);
				
			}
		} catch (Exception e) {
			// e.printStackTrace();			
			ir.succeeded = false;
			ir.errorMessage = e.getMessage();			
			ir.exception = e;			
		}	
		
		return ir;				
	}
	
	private Feed getImportFeed() {
		// do the import
		Feed feed = null;
		
		try {	        
			// (1) get XML-Data from import document
	        Document impDoc = Document.fromFile(this.importFile);
	        Element root = impDoc.getRootElement();
	          
	        // (2) get FeedInfo from import and create feedid and new FeedInfo for openSDX
	        String feedid = UUID.randomUUID().toString();
	        Calendar cal = Calendar.getInstance();
	        
//	        if(root.getChild("updated_at")!=null && root.getChildTextNN("updated_at").length()>0) {
//	        	cal.setTime(ymd.parse(root.getChildText("updated_at").substring(0, 9)));
//	        }	        
//	        
//	        long creationdatetime = cal.getTimeInMillis();	        
//	        long effectivedatetime = cal.getTimeInMillis();
//	        
//	        String lic = root.getChildTextNN("licensor");
//	        if (lic.length()==0) lic = "[NOT SET]";
//	        
//	        ContractPartner sender = ContractPartner.make(ContractPartner.ROLE_SENDER, lic , "");
//	        ContractPartner licensor = ContractPartner.make(ContractPartner.ROLE_LICENSOR, lic, "");
//	        ContractPartner licensee = ContractPartner.make(ContractPartner.ROLE_LICENSEE,"","");
//	        
//	        FeedInfo feedinfo = FeedInfo.make(onlytest, feedid, creationdatetime, effectivedatetime, sender, licensor, licensee);

		} catch (Exception e) {
			e.printStackTrace();
			ir.succeeded = false;
			ir.errorMessage = e.getMessage();			
			ir.exception = e;			
		}		        
        return feed;
	}
	
	public Feed getFormatedFeedFromImport() {			
		return this.getImportFeed();	
	}
	
	public Result getIr() {
		return ir;
	}

	public void setIr(Result ir) {
		this.ir = ir;
	}	

}
