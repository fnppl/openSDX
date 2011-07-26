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

public class OpenSDXToFinetunesExporter extends OpenSDXExporterBase {
	DateFormat ymd = new SimpleDateFormat("yyyyMMdd");
	private Result ir = Result.succeeded();
    static HashMap<String, String> finetunes_contributors_types = new HashMap<String, String>();
    static {
    	finetunes_contributors_types.put(Contributor.TYPE_DISPLAY_ARTIST, "performer");
    	finetunes_contributors_types.put(Contributor.TYPE_VOCALS, "lyricist");
    	finetunes_contributors_types.put(Contributor.TYPE_COMPOSER, "composer");
    	finetunes_contributors_types.put(Contributor.TYPE_COMPILATOR, "arranger");
    };
    
	
	public OpenSDXToFinetunesExporter(ExportType type, Feed expFeed, File savFile) {
		super(type, expFeed, savFile);
	}
	
	public OpenSDXToFinetunesExporter(Feed expFeed) {
		super(ExportType.getExportType("finetunes"), expFeed, null);
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
	        expDoc = Document.buildDocument(new Element("feed"));
	        Element expDocFeed = expDoc.getRootElement();
	        
	        // set attributes
	        expDocFeed.setAttribute("partnerid", "finetunes");
	        expDocFeed.setAttribute("version", "1.4");
	        
	        // feedid
	        String feedid = osdxFeed.getFeedinfo().getFeedID();
	        if (feedid==null || feedid.length()==0) feedid = "[NOT SET]";
	        expDocFeed.setAttribute("feedid", feedid);
	        
	        Element release = new Element("release");
	        expDocFeed.addContent(release);
	        
	        expDocFeed.setAttribute("action", "showinshop");
	        
	        // bundleCount max = 1 -> finetunes spec
        	int i=0;
        	
    		Bundle bundle = osdxFeed.getBundle(i);	        

    		// IDs
			IDs bundleids = bundle.getIds();
			if(bundleids.getFinetunesid()!=null && bundleids.getFinetunesid().length()>0) {
				Element id = new Element("id");
				id.setAttribute("type", "finetunes").setText(bundleids.getFinetunesid());
				release.addContent(id);
			}
			else if(bundleids.getUpc()!=null && bundleids.getUpc().length()>0) {
				Element id = new Element("id");
				id.setAttribute("type", "ean").setText(bundleids.getUpc());
				release.addContent(id);
			}
			
    		// title
	        String title = bundle.getDisplayname();
	        if (title==null) title = "";
	        release.addContent("title", title);
	        
	        // longname
	        String longname = bundle.getName();
	        if (longname==null) longname = "";
	        release.addContent("longname", longname);		        
	        
			
			// streaming allowed?			
			release.addContent("streaming", ""+bundle.getLicense_basis().isStreaming_allowed());	        
	        
    		// version
	        String version = bundle.getVersion();
	        if (version==null) version = "";
	        release.addContent("version", version);			
			
        	// add label / artistname / copyright / production
         	Element artists = new Element("artists");
         	release.addContent(artists);
         	
        	Vector<Contributor> contributors = bundle.getAllContributors();
        	for (Iterator<Contributor> itContributor = contributors.iterator(); itContributor.hasNext();) {
        		Contributor contributor = itContributor.next();
        		if(contributor.getType()==Contributor.TYPE_LABEL) {
        			Element label = new Element("label");
        			release.addContent(label);
        			
        			label.addContent("name", contributor.getName());
        			if(contributor.getWww().getHomepage()!=null && contributor.getWww().getHomepage().length()>0)
        				label.addContent("website", contributor.getWww().getHomepage());
        			
        			IDs labelids = bundle.getIds();
        			if(labelids.getFinetunesid()!=null && labelids.getFinetunesid().length()>0) {
        				Element id = new Element("id");
        				id.setAttribute("type", "finetunes").setText(labelids.getFinetunesid());
        				label.addContent(id);
        			}
        			else if(labelids.getGvl()!=null && labelids.getGvl().length()>0) {
        				Element id = new Element("id");
        				id.setAttribute("type", "gvl").setText(labelids.getGvl());
        				label.addContent(id);
        			}        			
        		}
        		else if(finetunes_contributors_types.containsKey(contributor.getType())) {
        			// artist_name = contributor.getName();
        			Element artist = new Element("artist");
        			artists.addContent(artist);
        			
        			artist.addContent("name", contributor.getName());
        			if(contributor.getWww().getHomepage()!=null && contributor.getWww().getHomepage().length()>0)
        				artist.addContent("website", contributor.getWww().getHomepage());
        			
        			IDs artistids = bundle.getIds();
        			if(artistids.getFinetunesid()!=null && artistids.getFinetunesid().length()>0) {
        				Element id = new Element("id");
        				id.setAttribute("type", "finetunes").setText(artistids.getFinetunesid());
        				artist.addContent(id);
        			}
        			
        			artist.addContent("role", finetunes_contributors_types.get(Contributor.TYPE_DISPLAY_ARTIST).toString());
        			
        		}
        		else if(contributor.getType()==Contributor.TYPE_COPYRIGHT) {
        			String copyright = contributor.getName();
        			if(contributor.getYear().length()>0) copyright = contributor.getYear()+" "+copyright;
        			release.addContent("copyrightinfo", copyright);
        		}
        		else if(contributor.getType()==Contributor.TYPE_PRODUCTION) {
        			String production = contributor.getName();
        			if(contributor.getYear().length()>0) production = contributor.getYear()+" "+production;
        			release.addContent("productioninfo", production);
        		}	        		
        	}
        	
        	// loop the tracks!
	        
	        // ToDo: more export magic if needed!

		} catch (Exception e) {
			// e.printStackTrace();
			ir.succeeded = false;
			ir.errorMessage = e.getMessage();			
			ir.exception = e;			
		}		        
        return expDoc;
	}
	
	public Document getFormatedDocumentFromExport() {			
		return this.getExportDocument();	
	}
	
	public Result getIr() {
		return ir;
	}

	public void setIr(Result ir) {
		this.ir = ir;
	}	

}
