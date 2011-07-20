package org.fnppl.opensdx.dmi.wayout;

import java.io.*;
import java.text.*;
import java.util.*;

import org.fnppl.opensdx.common.*;
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

public class OpenSDXToSimfyExporter extends OpenSDXExporterBase {
	DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
	Result ir = Result.succeeded();
    
	
	public OpenSDXToSimfyExporter(ExportType type, Feed expFeed, File savFile) {
		super(type, expFeed, savFile);
	}
	
	public OpenSDXToSimfyExporter(Feed expFeed) {
		super(ExportType.getExportType("simfy"), expFeed, null);
	}	
	
	public Result formatToExternalFile() {	
		try {			
			
			Document doc = this.getExportDocument();
            
			if(doc!=null) {			
	            // write file
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
		
	private Document getExportDocument() {
		// do the export
		Document expDoc = null;
		
		try {
			// (1) get XML-Data from export document
			Feed osdxFeed = this.exportFeed;
	        
	        // (2) create XML-Data for export document
			int bundleCount = osdxFeed.getBundleCount();
			
			// create root node "albums"  
			expDoc = Document.buildDocument(new Element("albums"));
			Element expDocRoot = expDoc.getRootElement();
			
        	for (int i=0;i<bundleCount;i++) {
        		Bundle bundle = osdxFeed.getBundle(i);
		        
        		// create node "album" for each bundle
        		expDocRoot.addContent(new Element("album"));
        		Element expDocAlbum = expDocRoot.getChild("album");
		        
		        // feedid
		        String upc = bundle.getIds().getUpc();
		        if (upc==null || upc.length()==0) upc = "[NOT SET]";
		        expDocAlbum.addContent("code", upc);
		        
		        Calendar cal = Calendar.getInstance();
		        
		        long creationdatetime = osdxFeed.getFeedinfo().getCreationDatetime();
	        	cal.setTimeInMillis(creationdatetime);
	        	expDocAlbum.addContent("updated_at", ymd.format(cal.getTime()));
	
		        // licensor
		        String lic = osdxFeed.getFeedinfo().getLicensor().getContractPartnerID();
		        if (lic==null) lic = "";		        
		        expDocAlbum.addContent("licensor", lic);
		        
	        	// add label / artistname / copyright / production
		        String label = "";
		        String artist_name = "";
	         	String copyright = "";
	         	String production = "";
	        	Vector<Contributor> contributors = bundle.getAllContributors();
	        	for (Iterator<Contributor> itContributor = contributors.iterator(); itContributor.hasNext();) {
	        		Contributor contributor = itContributor.next();
	        		if(contributor.getType()==Contributor.TYPE_LABEL) {
	        			label = contributor.getName();
	        		}
	        		else if(contributor.getType()==Contributor.TYPE_DISPLAY_ARTIST) {
	        			artist_name = contributor.getName();
	        		}
	        		else if(contributor.getType()==Contributor.TYPE_COPYRIGHT) {
	        			copyright = contributor.getName();
	        			if(contributor.getYear().length()>0) copyright = contributor.getYear()+" "+copyright;
	        		}
	        		else if(contributor.getType()==Contributor.TYPE_PRODUCTION) {
	        			production = contributor.getName();
	        			if(contributor.getYear().length()>0) production = contributor.getYear()+" "+production;
	        		}	        		
	        	}
	        	
	        	expDocAlbum.addContent("label", label);
	        	expDocAlbum.addContent("c_line", copyright);
	        	expDocAlbum.addContent("p_line", production);
	        	expDocAlbum.addContent("artist_name", artist_name);
		        
	        	long releaseDate = bundle.getInformation().getPhysicalReleaseDatetime();
	        	String original_released_on = "";
	        	if (original_released_on!=null) cal.setTimeInMillis(releaseDate); original_released_on = ymd.format(cal.getTime());
	        	expDocAlbum.addContent("original_released_on", original_released_on);
	        	
		        // title
		        String title = bundle.getDisplayname();
		        if (title==null) title = "";
		        expDocAlbum.addContent("title", title);		        
		        
		        // ToDo: export magic here!
        	}
		        
		} catch (Exception e) {
			e.printStackTrace();
			ir.succeeded = false;
			ir.errorMessage = e.getMessage();			
			ir.exception = e;			
		}		        
        return expDoc;
	}
	
	public Document getFormatedDocumentFromExport() {			
		return this.getExportDocument();	
	}

}
