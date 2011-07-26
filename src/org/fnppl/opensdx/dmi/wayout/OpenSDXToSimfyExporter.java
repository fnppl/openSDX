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
	private Result ir = Result.succeeded();

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
		Calendar cal = Calendar.getInstance();
		
		try {
			// (1) get XML-Data from export document
			Feed osdxFeed = this.exportFeed;
	        
	        // (2) create XML-Data for export document		
			// create root node "album"  
			expDoc = Document.buildDocument(new Element("album"));
			Element expDocAlbum = expDoc.getRootElement();
			
			// bundleCount max = 1 -> simfy spec
        	int i=0;
        	
    		Bundle bundle = osdxFeed.getBundle(i);
	        
	        // feedid
	        String upc = bundle.getIds().getUpc();
	        if (upc==null || upc.length()==0) upc = "[NOT SET]";
	        expDocAlbum.addContent("code", upc);
	        
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
        	
        	expDocAlbum.addContent("c_line", copyright);
        	expDocAlbum.addContent("p_line", production);
	        expDocAlbum.addContent("upc", upc);
        	expDocAlbum.addContent("label", label);	        	
        	expDocAlbum.addContent("artist_name", artist_name);
	        
	        // title
	        String title = bundle.getDisplayname();
	        if (title==null) title = "";
	        expDocAlbum.addContent("title", title);		        	
        	
        	long releaseDate = bundle.getInformation().getPhysicalReleaseDatetime();
        	String original_released_on = "";
        	if (original_released_on!=null) cal.setTimeInMillis(releaseDate); original_released_on = ymd.format(cal.getTime());
        	expDocAlbum.addContent("original_released_on", original_released_on);
        	
	        long creationdatetime = osdxFeed.getFeedinfo().getCreationDatetime();
        	cal.setTimeInMillis(creationdatetime);
        	expDocAlbum.addContent("updated_at", ymd.format(cal.getTime()));	        	
	        
        	int genreCount = bundle.getTags().getGenresCount();
        	for (int j=0;j<genreCount;j++) {
        		if(bundle.getTags().getGenre(j).length()>0) expDocAlbum.addContent("genre", bundle.getTags().getGenre(j));
        	}
        	
        	int fileCount = bundle.getFilesCount();
        	for (int j=0;j<fileCount;j++) {
        		if(bundle.getFile(j).getType().equals("cover")) {
        			Element cover = new Element("cover");
        			expDocAlbum.addContent(cover);
        				        			
        			cover.addContent("height", ""+bundle.getFile(j).getDimensionHeight());
        			cover.addContent("width", ""+bundle.getFile(j).getDimensionHeight());
        			
        			String filename = bundle.getFile(j).getLocationPath();
        			cover.addContent("file_name", filename);
        			cover.addContent("file_size", ""+bundle.getFile(j).getBytes());
        			
            		File f = new File(filename);
            		if(f!=null && f.exists()) {
            			byte[][] sums = SecurityHelper.getMD5SHA1(f);
            			cover.addContent("file_checksum", Checksums.make(sums[0],sums[1],null).getMd5String());
            		} else {
            			//file does not exist -> so we have to set the values "manually"
            			// checksum md5
            			if(bundle.getFile(j).getChecksums().getMd5String()!=null)
            				cover.addContent("file_checksum", SecurityHelper.HexDecoder.encode(bundle.getFile(j).getChecksums().getMd5(),'\0',-1).toLowerCase());
            		}	        			
        		}
        	}
        	
        	Element expDocTracks = new Element("tracks");
    		expDocAlbum.addContent(expDocTracks);
        	
        	int itemCount = bundle.getItemsCount();
        	int trackCount = 0;
        	int trackNum = 0;
        	int discCount = 0;
        	for (int j=0;j<itemCount;j++) {
        		// fulltracks holen
        		if(bundle.getItem(j).getType().equals("audio")) {	        					
        			// add track 
        			Element track = new Element("track");
        			expDocTracks.addContent(track);
	        		
        			trackCount++;
        			// get max disc number
        			if(bundle.getItem(j).getInformation().getSetNum()>discCount) discCount = bundle.getItem(j).getInformation().getSetNum();	
        			trackNum = bundle.getItem(j).getInformation().getNum();
        			
        			String ext = ""+trackNum;
        			
        			if(ext.length()<2) ext="0"+ext;
        			String trackCode = upc+"_"+ext;
        			track.addContent("track_code", trackCode);
        			
        			// add track number / disc number
        			track.addContent("track_number", ""+trackNum);
        			track.addContent("disk_number", ""+bundle.getItem(j).getInformation().getSetNum());        			
        			
        			IDs ids = bundle.getItem(j).getIds();
        			if(ids.getIsrc()!=null && ids.getIsrc().length()>0)
        				track.addContent("isrc", ids.getIsrc());
        			
        			if(ids.getUpc()!=null && ids.getUpc().length()>0)
        				track.addContent("upc", ids.getUpc());
        			
        			// title
        			if(bundle.getItem(j).getDisplayname()!=null)
        				track.addContent("title", bundle.getItem(j).getDisplayname());

    	        	// display_artist
        			if(bundle.getItem(j).getDisplay_artist()!=null)
        				track.addContent("artist_name", bundle.getItem(j).getDisplay_artist());
        			
    	        	// duration
       				track.addContent("duration", ""+bundle.getItem(j).getInformation().getPlaylength());
        			
        			// explicit_lyrics	    	        	
                	String explicit_lyrics = bundle.getItem(j).getTags().getExplicit_lyrics();
                	if(explicit_lyrics!=null)
                		track.addContent("explicit_lyrics", explicit_lyrics);
                	
    	        	int trackFileCount = bundle.getFilesCount();
    	        	for (int k=0;k<trackFileCount;k++) {
    	        		if(bundle.getItem(j).getFile(k).getType().equals("full")) {
    	        			
    	        			String track_filename = bundle.getItem(j).getFile(k).getLocationPath();
                			track.addContent("file_name", track_filename);
                			track.addContent("file_size", ""+bundle.getItem(j).getFile(k).getBytes());
                			
    	            		File f = new File(track_filename);
    	            		if(f!=null && f.exists()) {
    	            			byte[][] sums = SecurityHelper.getMD5SHA1(f);
    	            			track.addContent("file_checksum", Checksums.make(sums[0],sums[1],null).getMd5String());
    	            		} else {
    	            			// file does not exist -> so we have to set the values "manually"
    	            			// checksum md5  
    	            			if(bundle.getItem(j).getFile(k).getChecksums().getMd5()!=null)	    	            				
    	            				track.addContent("file_checksum", SecurityHelper.HexDecoder.encode(bundle.getItem(j).getFile(k).getChecksums().getMd5(),'\0',-1).toLowerCase());
    	            		}	    	        			
    	        			
    	        		
    	                	/*
    	                	 * ToDo: <rights/> (streamable, from, to etc.) for every track and territory / not yet available in openSDX! 
    	                	 *  
    	                	 */
    	        		}
    	        	}
        		}
        	}	        	

        	expDocAlbum.addContent("tracks_count", ""+trackCount);
        	if(discCount>0) {
        		expDocAlbum.addContent("disks_count", ""+discCount);
        	}
        	
	        // ToDo: more export magic here if needed!
		        
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
