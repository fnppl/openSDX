package org.fnppl.opensdx.dmi.wayout;

import java.io.*;
import java.text.*;
import java.util.*;

import org.fnppl.opensdx.common.*;
import org.fnppl.opensdx.xml.*;
import org.fnppl.opensdx.security.*;

import org.jdom.output.XMLOutputter;

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

public class OpenSDXToFinetunesExporter extends OpenSDXExporterBase {
	DateFormat ymd = new SimpleDateFormat("yyyyMMdd");
	private Result ir = Result.succeeded();
    static HashMap<String, String> finetunes_contributors_types = new HashMap<String, String>();
    static {
    	finetunes_contributors_types.put(Contributor.TYPE_DISPLAY_ARTIST, "performer");
    	finetunes_contributors_types.put(Contributor.TYPE_TEXTER, "lyricist");
    	finetunes_contributors_types.put(Contributor.TYPE_COMPOSER, "composer");
    	finetunes_contributors_types.put(Contributor.TYPE_COMPILATOR, "arranger");
    };
    
	
	public OpenSDXToFinetunesExporter(ExportType type, Feed expFeed, File savFile) {
		super(type, expFeed, savFile);
	}
	
	public OpenSDXToFinetunesExporter(ExportType type, Feed expFeed) {
		super(type, expFeed, null);
	}
	
	public OpenSDXToFinetunesExporter(Feed expFeed) {
		super(ExportType.getExportType("finetunes"), expFeed, null);
	}	
	
	public Result formatToExternalFile() {	
		try {			
			Document doc = this.getExportDocument();
            
			if(doc != null && saveFile != null) {			
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
	
	public byte[] formatToBuffer() {	
		try {	
			Document doc = this.getExportDocument();
			if(doc != null) {			
				return doc.toByteArray();
			}			
		} catch (Exception e) {
			// e.printStackTrace();			
//			ir.succeeded = false;
//			ir.errorMessage = e.getMessage();			
//			ir.exception = e;	
			
		}		
		return null;
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
	        
	        release.setAttribute("action", "showinshop");
	        
	        // bundleCount max = 1 -> finetunes spec
        	int i=0;
        	
    		Bundle bundle = osdxFeed.getBundle(i);	        

    		// IDs
			IDs bundleids = bundle.getIds();
			if(bundleids.getFinetunes()!=null && bundleids.getFinetunes().length()>0) {
				Element id = new Element("id");
				id.setAttribute("type", "finetunes").setText(bundleids.getFinetunes());
				release.addContent(id);
			}
			else if(bundleids.getUpc()!=null && bundleids.getUpc().length()>0) {
				Element id = new Element("id");
				id.setAttribute("type", "ean").setText(bundleids.getUpc());
				release.addContent(id);
			}
			else if(bundleids.getIsrc()!=null && bundleids.getIsrc().length()>0) {
				Element id = new Element("id");
				id.setAttribute("type", "isrc").setText(bundleids.getIsrc());
				release.addContent(id);
			} 
			else if(bundleids.getIsbn()!=null && bundleids.getIsbn().length()>0) {
				Element id = new Element("id");
				id.setAttribute("type", "isbn").setText(bundleids.getIsrc());
				release.addContent(id);
			}
			else if(bundleids.getLabelordernum()!=null && bundleids.getLabelordernum().length()>0) {
				release.addContent("catno", bundleids.getLabelordernum());
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
			//release.addContent("streaming", String.valueOf(BusinessObject.getNotNullBoolean(bundle.getLicense_basis().isStreaming_allowed(), false)));
	        release.addContent("streaming", ""+bundle.getLicense_basis().isStreaming_allowed());
			
			// pricecode
        	if(bundle.getLicense_basis().getPricingPricecode()!=null) {
        		release.addContent("priceband", ""+bundle.getLicense_basis().getPricingPricecode());
        	}
			
    		// version
	        String version = bundle.getVersion();
	        if (version==null) version = "";
	        release.addContent("version", version);			
			
        	// add label / artistname / copyright / production
         	Element artists = new Element("artists");
         	
        	Vector<Contributor> contributors = bundle.getAllContributors();
        	for (Iterator<Contributor> itContributor = contributors.iterator(); itContributor.hasNext();) {
        		Contributor contributor = itContributor.next();
        		if(contributor.getType().equals(Contributor.TYPE_LABEL)) {
        			Element label = new Element("label");
        			release.addContent(label);
        			
        			label.addContent("name", contributor.getName());
        			if(contributor.getWww()!=null && contributor.getWww().getHomepage()!=null && contributor.getWww().getHomepage().length()>0)
        				label.addContent("website", contributor.getWww().getHomepage());
        			
        			IDs labelids = contributor.getIDs();
        			if(labelids.getFinetunes()!=null && labelids.getFinetunes().length()>0) {
        				Element id = new Element("id");
        				id.setAttribute("type", "finetunes").setText(labelids.getFinetunes());
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
        			if(contributor.getWww()!=null && contributor.getWww().getHomepage()!=null && contributor.getWww().getHomepage().length()>0)
        				artist.addContent("website", contributor.getWww().getHomepage());
        			
        			IDs artistids = contributor.getIDs();
        			if(artistids != null && artistids.getFinetunes() != null && artistids.getFinetunes().length() > 0) {
        				Element id = new Element("id");
        				id.setAttribute("type", "finetunes").setText(artistids.getFinetunes());
        				artist.addContent(id);
        			}
        			
        			artist.addContent("role", finetunes_contributors_types.get(contributor.getType()).toString());
        			
        		}
        		else if(contributor.getType().equals(Contributor.TYPE_COPYRIGHT)) {
        			String copyright = contributor.getName();
        			if(contributor.getYear()!=null && contributor.getYear().length()>0) copyright = contributor.getYear()+" "+copyright;
        			release.addContent("copyrightinfo", copyright);
        		}
        		else if(contributor.getType().equals(Contributor.TYPE_PRODUCTION)) {
        			String production = contributor.getName();
        			if(contributor.getYear()!=null && contributor.getYear().length()>0) production = contributor.getYear()+" "+production;
        			release.addContent("productioninfo", production);
        		}	        		
        	}
        	
        	release.addContent(artists);        	
        	
        	// add  genres
        	Element genres = new Element("genres");
        	release.addContent(genres);
        	
        	int genrecount = bundle.getTags().getGenresCount();
        	for(int j=0; j<genrecount;j++) {
        		Element genre = new Element("genre");
        		genre.setText(bundle.getTags().getGenre(j));
        		genres.addContent(genre);
        	}
        	
        	int filecount = bundle.getFilesCount();
        	for(int j=0; j<filecount;j++) {
        		ItemFile file = bundle.getFile(j);
        		if(file.getType().equals("cover")) {
	        		Element resource = new Element("resource");
	        		resource.setAttribute("type", "frontcover");
	        		if(file.getFiletype()!=null) { resource.addContent("datatype", file.getFiletype());}

	        		Element quality = new Element("quality");
	        		quality.setAttribute("type", "width").setText(""+file.getDimensionWidth());
	        		resource.addContent(quality);
	        		
	        		quality = new Element("quality");
	        		quality.setAttribute("type", "height").setText(""+file.getDimensionHeight());
	        		resource.addContent(quality);	        		

	        		quality = new Element("quality");
	        		quality.setAttribute("type", "size").setText(""+file.getBytes());
	        		resource.addContent(quality);	        		
        			
        			String filename = file.getLocationPath();
        			resource.addContent("uri", filename);
        			
            		File f = new File(filename);
            		if(f!=null && f.exists()) {
            			byte[][] sums = SecurityHelper.getMD5SHA1(f);
            			Element cs = new Element("checksum");
            			cs.setAttribute("type", "md5").setText(Checksums.make(sums[0],sums[1],null).getMd5String());
            			resource.addContent(cs);
            		} else {
            			//file does not exist -> so we have to set the values "manually"
            			// checksum md5
            			if(file.getChecksums().getMd5String()!=null) {
                			Element cs = new Element("checksum");
                			cs.setAttribute("type", "md5").setText(SecurityHelper.HexDecoder.encode(file.getChecksums().getMd5(),'\0',-1).toLowerCase());
                			resource.addContent(cs);
            			}
            		}
	        		
	        		release.addContent(resource);
        		}
        	}
        	
        	// infotexts (promo/teaser)
        	Element infotexts = new Element("infotexts");
        	release.addContent(infotexts);
        	int promocount = bundle.getInformation().getTexts().getPromotextCount();
        	for(int j=0; j<promocount;j++) {
        		String lang = bundle.getInformation().getTexts().getPromotextLanguage(j);
        		String text = bundle.getInformation().getTexts().getPromotext(j);
        		Element infotext = new Element("infotext");
        		infotexts.addContent(infotext);
        		infotext.setAttribute("lang", lang).setText(text);
        	} 
        	
        	int teasercount = bundle.getInformation().getTexts().getTeasertextCount();
        	for(int j=0; j<teasercount;j++) {
        		String lang = bundle.getInformation().getTexts().getTeasertextLanguage(j);
        		String text = bundle.getInformation().getTexts().getTeasertext(j);
        		Element infotext = new Element("infotext");
        		infotexts.addContent(infotext);
        		infotext.setAttribute("lang", lang).setText(text);
        	}        	
        	
        	// distribution territories
        	Element distributionterritories = new Element("distributionterritories");
        	release.addContent(distributionterritories);

        	Element allowances = new Element("allowances");
        	
        	int territorycount = bundle.getLicense_basis().getTerritorial().getTerritorialCount();
        	int allow = 0;
        	for(int j=0; j<territorycount;j++) {
        		if(bundle.getLicense_basis().getTerritorial().isTerritoryAllowed(j))
        			allowances.addContent("territory", bundle.getLicense_basis().getTerritorial().getTerritory(j)); allow++;
        	}
        	
        	if(allow>0) distributionterritories.addContent(allowances);
        	        	
        	// schedules / releasedates
        	Element schedules = new Element("schedules");
        	release.addContent(schedules);
        	
        	Calendar cal = Calendar.getInstance();
        	cal.setTimeInMillis(bundle.getInformation().getPhysicalReleaseDatetime());
        	schedules.addContent("streetreleasedate", ymd.format(cal.getTime()));
        	
        	cal.setTimeInMillis(bundle.getInformation().getDigitalReleaseDatetime());
        	schedules.addContent("digitalreleasedate", ymd.format(cal.getTime()));
        	
        	// may release from (?)
        	if(bundle.getLicense_basis().getTimeframeFrom()!=null) {
        		cal.setTimeInMillis(bundle.getLicense_basis().getTimeframeFrom());
        		schedules.addContent("youmayreleasefrom", ymd.format(cal.getTime()));
        	}
        	
        	// may release until (?)
        	if(bundle.getLicense_basis().getTimeframeTo()!=null) {
	        	cal.setTimeInMillis(bundle.getLicense_basis().getTimeframeTo());
	        	schedules.addContent("youmayreleaseuntil", ymd.format(cal.getTime()));        	
        	}
        	
        	// tracks
        	Element tracks = new Element("tracks");
        	release.addContent(tracks);        	
        	
        	int itemcount = bundle.getItemsCount();
        	tracks.setAttribute("count", ""+itemcount);
        	for(int j=0; j<itemcount;j++) {
        		Item item = bundle.getItem(j);
        		Element track = new Element("track");
        		tracks.addContent(track);
        		
        		// Item IDs
    			IDs itemids = item.getIds();
    			if(itemids.getFinetunes()!=null && itemids.getFinetunes().length()>0) {
    				Element id = new Element("id");
    				id.setAttribute("type", "finetunes").setText(itemids.getFinetunes());
    				track.addContent(id);
    			}
    			
    			if(itemids.getUpc()!=null && itemids.getUpc().length()>0) {
    				Element id = new Element("id");
    				id.setAttribute("type", "ean").setText(itemids.getUpc());
    				track.addContent(id);
    			}
    			
    			if(itemids.getIsrc()!=null && itemids.getIsrc().length()>0) {
    				Element id = new Element("id");
    				id.setAttribute("type", "isrc").setText(itemids.getIsrc());
    				track.addContent(id);
    			} 
    			
    			if(itemids.getIsbn()!=null && itemids.getIsbn().length()>0) {
    				Element id = new Element("id");
    				id.setAttribute("type", "isbn").setText(itemids.getIsrc());
    				track.addContent(id);
    			}    			
    			if(item.getInformation().hasNum())
    				track.addContent("position", ""+item.getInformation().getNum());
    			
        		if(item.getInformation().hasSetNum()) 
        			track.addContent("cdsourcenum", ""+item.getInformation().getSetNum());

        		if(item.getInformation().hasPlaylength()) 
        			track.addContent("tracklength", ""+item.getInformation().getPlaylength());
        		
        		if(item.getTags().getExplicit_lyrics()!=null)
        			track.addContent("explicitlyrics", ""+item.getTags().getExplicit_lyrics());
        		
        		if(item.getInformation().getOrigin_country()!=null)
        			track.addContent("origincountry", item.getInformation().getOrigin_country());
        		
        		track.addContent("live", ""+item.getTags().isLive());
        		track.addContent("acoustic", ""+item.getTags().isAccoustic());
        		track.addContent("instrumental", ""+item.getTags().isInstrumental());
        		
	        	// suggested_prelistening_offset
	        	if(item.getInformation().hasSuggestedPrelistiningOffset()) {
	        		track.addContent("prelisteningoffset", ""+item.getInformation().getSuggestedPrelistiningOffset());
	        	}        		
        		       		
        		// item title
    	        title = item.getDisplayname();
    	        if (title==null) title = "";
    	        track.addContent("title", title);
    	        
    	        // item longname
    	        longname = item.getName();
    	        if (longname==null) longname = "";
    	        track.addContent("longname", longname);		        
    	        
    			
    			// item streaming allowed?			
//    	        track.addContent("streaming", String.valueOf(BusinessObject.getNotNullBoolean(item.getLicense_basis().isStreaming_allowed(), false)));
    	        track.addContent("streaming", ""+item.getLicense_basis().isStreaming_allowed());

        		// item version
    	        version = item.getVersion();
    	        if (version==null) version = "";
    	        track.addContent("version", version);			
    			
            	// add label / artistname / copyright / production
             	artists = new Element("artists");
             	track.addContent(artists); 
             	
               	contributors = item.getAllContributors();
            	for (Iterator<Contributor> itContributor = contributors.iterator(); itContributor.hasNext();) {
            		Contributor contributor = itContributor.next();
            		if(contributor.getType().equals(Contributor.TYPE_LABEL)) {
            			Element label = new Element("label");
            			track.addContent(label);
            			
            			label.addContent("name", contributor.getName());
            			if(contributor.getWww()!=null && contributor.getWww().getHomepage()!=null && contributor.getWww().getHomepage().length()>0)
            				label.addContent("website", contributor.getWww().getHomepage());
            			
            			IDs labelids = contributor.getIDs();
            			if(labelids.getFinetunes()!=null && labelids.getFinetunes().length()>0) {
            				Element id = new Element("id");
            				id.setAttribute("type", "finetunes").setText(labelids.getFinetunes());
            				label.addContent(id);
            			}
            			
            			if(labelids.getGvl()!=null && labelids.getGvl().length()>0) {
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
            			if(contributor.getWww()!=null && contributor.getWww().getHomepage()!=null && contributor.getWww().getHomepage().length()>0)
            				artist.addContent("website", contributor.getWww().getHomepage());
            			
            			IDs artistids = contributor.getIDs();
            			if(artistids != null && artistids.getFinetunes() != null && artistids.getFinetunes().length() > 0) {
            				Element id = new Element("id");
            				id.setAttribute("type", "finetunes").setText(artistids.getFinetunes());
            				artist.addContent(id);
            			}
            			
            			artist.addContent("role", finetunes_contributors_types.get(contributor.getType()).toString());
            			
            		}
            		else if(contributor.getType().equals(Contributor.TYPE_COPYRIGHT)) {
            			String copyright = contributor.getName();
            			if(contributor.getYear().length()>0) copyright = contributor.getYear()+" "+copyright;
            			track.addContent("copyrightinfo", copyright);
            		}
            		else if(contributor.getType().equals(Contributor.TYPE_PRODUCTION)) {
            			String production = contributor.getName();
            			if(contributor.getYear().length()>0) production = contributor.getYear()+" "+production;
            			track.addContent("productioninfo", production);
            		}
            		else if(contributor.getType().equals(Contributor.TYPE_CLEARINGHOUSE)) {
            			String clearinghouse = contributor.getName();
            			track.addContent("collectingsociety", clearinghouse);
            		}            		
            	}
            	
            	// add  genres
            	genres = new Element("genres");
            	track.addContent(genres);
            	
            	genrecount = item.getTags().getGenresCount();
            	for(int k=0; k<genrecount;k++) {
            		Element genre = new Element("genre");
            		genre.setText(item.getTags().getGenre(k));
            		genres.addContent(genre);
            	}
            	
	        	boolean track_bundle_only = item.getTags().isBundle_only();
	        	if(track_bundle_only) {
	        		track.addContent("bundle", "true");	
	        	}
	        	else {
	        		track.addContent("bundle", "false");	
	        	}
	        	
	        	boolean explicitlyrics = item.getTags().isExplicit_lyrics();
	        	if(explicitlyrics) {
	        		track.addContent("explicitlyrics", "true");	
	        	}
	        	else {
	        		track.addContent("explicitlyrics", "false");
	        	}            	
            	
            	// distribution territories
            	distributionterritories = new Element("distributionterritories");
            	track.addContent(distributionterritories);

            	allowances = new Element("allowances");
            	distributionterritories.addContent(allowances);
            	
            	if(item.getLicense_basis().getTerritorial()!=null) {
	            	territorycount = item.getLicense_basis().getTerritorial().getTerritorialCount();
	            	for(int k=0; k<territorycount;k++) {
	            		if(item.getLicense_basis().getTerritorial().isTerritoryAllowed(k))
	            			allowances.addContent("territory", item.getLicense_basis().getTerritorial().getTerritory(k));
	            	}
            	}
            	
    			// pricecode
            	if(item.getLicense_basis().getPricingPricecode()!=null) {
            		track.addContent("priceband", ""+item.getLicense_basis().getPricingPricecode());
            	}            	
            	        	
            	// schedules / releasedates
            	schedules = new Element("schedules");
            	track.addContent(schedules);
            	
            	cal.setTimeInMillis(item.getInformation().getPhysicalReleaseDatetime());
            	schedules.addContent("streetreleasedate", ymd.format(cal.getTime()));
            	
            	cal.setTimeInMillis(item.getInformation().getDigitalReleaseDatetime());
            	schedules.addContent("digitalreleasedate", ymd.format(cal.getTime()));            	
             	
	        	int trackFileCount = item.getFilesCount();
	        	for (int k=0;k<trackFileCount;k++) {
	        		ItemFile file = item.getFile(k);
	        		if(file.getType().equals("full")) {
	        			Element resource = new Element("resource");
		        		resource.setAttribute("type", "audiofile");
		        		
		        		if(file.getFiletype()!=null) { resource.addContent("datatype", file.getFiletype());}

		        		Element quality = new Element("quality");
		        		quality.setAttribute("type", "duration").setText(""+item.getInformation().getPlaylength());
		        		resource.addContent(quality);
		        		
		        		if(file.getChannels()!=null) {
			        		quality = new Element("quality");
			        		quality.setAttribute("type", "channelmode").setText(""+file.getChannels());
			        		resource.addContent(quality);
		        		}

		        		if(file.getSamplerate()!=null) {
			        		quality = new Element("quality");
			        		quality.setAttribute("type", "samplerate").setText(file.getSamplerate());
			        		resource.addContent(quality);
		        		}		        		
		        		
		        		quality = new Element("quality");
		        		quality.setAttribute("type", "size").setText(""+file.getBytes());
		        		resource.addContent(quality);	        		
	        			
	        			String filename = file.getLocationPath();
	        			resource.addContent("uri", filename);
	        			
	            		File f = new File(filename);
	            		if(f!=null && f.exists()) {
	            			byte[][] sums = SecurityHelper.getMD5SHA1(f);
	            			Element cs = new Element("checksum");
	            			cs.setAttribute("type", "md5").setText(Checksums.make(sums[0],sums[1],null).getMd5String());
	            			resource.addContent(cs);
	            		} else {
	            			//file does not exist -> so we have to set the values "manually"
	            			// checksum md5
	            			if(file.getChecksums().getMd5String()!=null) {
	                			Element cs = new Element("checksum");
	                			cs.setAttribute("type", "md5").setText(SecurityHelper.HexDecoder.encode(file.getChecksums().getMd5(),'\0',-1).toLowerCase());
	                			resource.addContent(cs);
	            			}
	            		}
	            		
	            		track.addContent(resource);
	        		}
	        	}            	
        	}         	
        	
	        // ToDo: more export magic if needed!

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
	
	public Result getIr() {
		return ir;
	}

	public void setIr(Result ir) {
		this.ir = ir;
	}	

}
