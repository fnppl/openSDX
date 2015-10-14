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

public class ExactMobileToOpenSDXImporter extends OpenSDXImporterBase {
	DateFormat ymd = new SimpleDateFormat("yyyyMMdd");
	private Result ir = Result.succeeded();
	// test?
	boolean onlytest = true;
    
	public ExactMobileToOpenSDXImporter(ImportType type, File impFile, File savFile) {
		super(type, impFile, savFile);
	}
	
	public ExactMobileToOpenSDXImporter(File impFile) {
		super(ImportType.getImportType("exactmobile"), impFile, null);
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
	        
	        long creationdatetime = cal.getTimeInMillis();	        
	        long effectivedatetime = cal.getTimeInMillis();
	        
	        
	        String lic = root.getAttribute("primaryRightsHolder");
	        if (lic==null || lic.length()==0) lic = "[NOT SET]";
	        	
	        ContractPartner sender = ContractPartner.make(ContractPartner.ROLE_SENDER, lic , "");
	        ContractPartner licensor = ContractPartner.make(ContractPartner.ROLE_LICENSOR, lic, "");
	        ContractPartner licensee = ContractPartner.make(ContractPartner.ROLE_LICENSEE,"","");
	        
	        FeedInfo feedinfo = FeedInfo.make(onlytest, feedid, creationdatetime, effectivedatetime, sender, licensor, licensee);
	        
	        // path to importfile
	        String path = this.importFile.getParent();
	        
	        // (3) create new feed with feedinfo
	        feed = Feed.make(feedinfo);              
        	
        	// IDs of bundle -> more (?)
        	IDs bundleids = IDs.make();
        	if(root.getAttribute("grid")!=null) bundleids.grid(root.getAttribute("grid"));
        	if(root.getAttribute("productUPC")!=null) bundleids.upc(root.getAttribute("productUPC"));
        	if(root.getAttribute("partnerProductId")!=null) bundleids.labelordernum(root.getAttribute("partnerProductId"));
        	
        	// displayname
        	String displayname = root.getAttribute("title");
        	
        	// license basis
        	Territorial territorial = Territorial.make();
        	
	        // Information
        	String streetReleaseDate = root.getAttribute("originalReleaseDate");	        
	        if(streetReleaseDate!=null && streetReleaseDate.length()>0) {
	        	cal.setTime(ymd.parse(streetReleaseDate));
	        }
	        else {
	        	// MUST: when not provided then today
	        	cal.setTime(new Date());
	        }
	        
	        // streetRelease
	        long srd = cal.getTimeInMillis(); 
	        
	        String physicalReleaseDate = "";
	        String deleteDate = "";
        	Vector<Element> territories = root.getChild("offerings").getChildren("offering");
        	for (Iterator<Element> itTerritories = territories.iterator(); itTerritories.hasNext();) {
        		Element territory = itTerritories.next();        	
        		String tn = territory.getAttribute("territory");
        		territorial.allow(tn);
        		
        		physicalReleaseDate = territory.getAttribute("releaseDate")!=null ? territory.getAttribute("releaseDate") : "";
        		deleteDate = territory.getAttribute("deleteDate")!=null ? territory.getAttribute("deleteDate") : "";
        	}
        	
        	// only one territory -> use releasedate for whole bundle       
	        if(physicalReleaseDate.length()>0 && territories.size()==1) {
	        	cal.setTime(ymd.parse(physicalReleaseDate));
	        }
	        else {
	        	// MUST: when not provided then today
	        	cal.setTime(new Date());
	        }	        
	        
	        // physicalRelease
        	long prd = cal.getTimeInMillis();        	
        	
        	BundleInformation info = BundleInformation.make(srd, prd);
        	
        	// Release
        	LicenseBasis license_basis = LicenseBasis.make(territorial, srd, prd);
        	
        	System.out.println("physicalReleaseDate.length(): " +physicalReleaseDate.length()+" / "+physicalReleaseDate);
        	System.out.println("deleteDate.length(): " +deleteDate.length()+" / "+deleteDate);
	        if(physicalReleaseDate.length()>0 && deleteDate.length()>0 && territories.size()==1) {
	        	System.out.println("HALLO");
	        	cal.setTime(ymd.parse(deleteDate));
	        	license_basis.timeframe_from_datetime(prd);
	        	cal.setTime(ymd.parse(deleteDate));
	        	license_basis.timeframe_to_datetime(cal.getTimeInMillis());
	        }	        
        	
        	// license specifics -> empty!
        	LicenseSpecifics license_specifics = LicenseSpecifics.make();        	
        	
        	// init GenreConverter
        	GenreConverter gc = GenreConverter.getInstance(GenreConverter.EXACTMOBILE_TO_OPENSDX);        	       	
        	
        	// receiver -> "MUST" -> empty!
        	feedinfo.receiver(Receiver.make(Receiver.TRANSFER_TYPE_OSDX_FILESERVER));
        	
        	Bundle bundle = Bundle.make(bundleids, displayname, displayname, "", "", info, license_basis, license_specifics);
        	
        	// add Tags
        	ItemTags tags = ItemTags.make();   		
        	tags.addGenre(gc.convert(root.getAttribute("genre")));
        	
        	// explicit_lyrics
        	if(root.getAttribute("explicitLyrics").length()>0) {
        		if(root.getAttribute("explicitLyrics").toLowerCase().equals("false")) {
        			tags.explicit_lyrics(ItemTags.EXPLICIT_LYRICS_FALSE);  
        		}
        		else if(root.getAttribute("explicitLyrics").toLowerCase().equals("true")) {
        			tags.explicit_lyrics(ItemTags.EXPLICIT_LYRICS_TRUE);  
        		}            		
        	}
        	
    		bundle.tags(tags);        	
        	
        	Contributor contributor = Contributor.make(root.getAttribute("label"), Contributor.TYPE_LABEL, IDs.make());
        	bundle.addContributor(contributor);
        	BundleTexts bt = BundleTexts.make();
        	
        	// display_artistname
        	String display_artistname = "";
        	
        	Vector<Element> artists = root.getChild("artists").getChildren("artist");
        	for (Iterator<Element> itArtists = artists.iterator(); itArtists.hasNext();) {
        		Element artist = itArtists.next();        	
        		String role = artist.getAttribute("role");
        		if(role!=null && role.toLowerCase().equals("performer")) {
        			contributor = Contributor.make(artist.getAttribute("knownAs"), Contributor.TYPE_PERFORMER, IDs.make());
        		}
        		else {
        			contributor = Contributor.make(artist.getAttribute("knownAs"), Contributor.TYPE_DISPLAY_ARTIST, IDs.make());
        		}
        		
        		String isPrimary = artist.getAttribute("isPrimary");
        		if(isPrimary!=null && isPrimary.toLowerCase().equals("true")) {
        			display_artistname = artist.getAttribute("knownAs");
        		}
        		
        		bundle.addContributor(contributor);
        	}
        	// set display artist name of bundle
        	bundle.display_artistname(display_artistname);
        	
        	info.texts(bt);
         	
         	String copyright = "";
         	if(root.getAttribute("cLine")!=null) { copyright = root.getAttribute("cLine"); }
         	String production = "";
         	if(root.getAttribute("pLine")!=null) { production = root.getAttribute("pLine"); }
         	
         	if(copyright.length()>0) {
	         		contributor = Contributor.make(copyright, Contributor.TYPE_COPYRIGHT, IDs.make());
	         		bundle.addContributor(contributor);  
         	}
         	
         	if(production.length()>0) {
	         		contributor = Contributor.make(production, Contributor.TYPE_PRODUCTION, IDs.make());
	         		bundle.addContributor(contributor);  
         	}         	
        	        	
        	// cover
        	String cover = root.getAttribute("packshot");
        	if(cover!=null && cover.length()>0) {
        		ItemFile itemfile = ItemFile.make(); 
        		itemfile.type("frontcover");

        		// check if file exist at path
        		File f = new File(path+cover);
        		if(f!=null && f.exists()) {
        			itemfile.setFile(f);
        			
        			// set delivered path to file 
        			itemfile.setLocation(FileLocation.make(cover,cover));
        		} else {
        			//file does not exist -> so we have to set the values "manually"
        			
        			//-> use filename for location
        			itemfile.setLocation(FileLocation.make(cover, cover));
        		}
        		
        		bundle.addFile(itemfile);
        	}
        	
        	Vector<Element> volumes = root.getChild("volumes").getChildren("volume");
        	for (Iterator<Element> itVolumes = volumes.iterator(); itVolumes.hasNext();) {
        		Element volume = itVolumes.next();
	        	
	        	Vector<Element> tracks = volume.getChild("tracks").getChildren("track");
	        	for (Iterator<Element> itTracks = tracks.iterator(); itTracks.hasNext();) {
	        		Element track = itTracks.next();
	
	        		IDs trackids = IDs.make();
	            	if(track.getAttribute("grid")!=null && track.getAttribute("grid").length()>0) trackids.grid(track.getAttribute("upc"));
	            	if(track.getAttribute("isrc")!=null && track.getAttribute("isrc").length()>0) trackids.isrc(track.getAttribute("isrc"));
	        		
		        	// displayname
		        	String track_displayname = track.getAttribute("title");  
		        	
		        	BundleInformation track_info = BundleInformation.make(srd, prd);		        	
		        	
		        	// num
		        	if(track.getAttribute("trackSequence")!=null && track.getAttribute("trackSequence").length()>0) {
		        		track_info.num(Integer.parseInt(track.getAttribute("trackSequence")));
		        	}
		        	
		        	// setnum
		        	if(volume.getAttribute("sequenceNumber")!=null && volume.getAttribute("sequenceNumber").length()>0) {
		        		track_info.setnum(Integer.parseInt(volume.getAttribute("sequenceNumber")));
		        	} 
		        	
		        	// tracklength
		        	String duration = track.getAttribute("duration");
		        	
		        	if(duration!=null && duration.length()>0) {
		        		String[] arrdur = new String[3];
		        		arrdur = duration.split(":");
		        		int dur = (Integer.parseInt(arrdur[0]) * 3600) + (Integer.parseInt(arrdur[1]) * 60) + Integer.parseInt(arrdur[2]);
	        			track_info.playlength(dur);     			
	        		}
		        	
		        	// territorial for license basis
		        	Territorial track_territorial = Territorial.make();
	        		
	        		// track license basis
	        		String track_physicalReleaseDate = "";
	    	        String track_deleteDate = "";
	    	        String track_pricecode = "";
	    	        Calendar track_cal = Calendar.getInstance();
	            	Vector<Element> track_territories = root.getChild("offerings").getChildren("offering");
	            	for (Iterator<Element> itTerritories = track_territories.iterator(); itTerritories.hasNext();) {
	            		Element track_territory = itTerritories.next();        	
	            		String tn = track_territory.getAttribute("territory");
	            		track_territorial.allow(tn);
	            		
	            		track_physicalReleaseDate = track_territorial.getAttribute("releaseDate")!=null ? track_territorial.getAttribute("releaseDate") : "";
	            		track_deleteDate = track_territorial.getAttribute("deleteDate")!=null ? track_territorial.getAttribute("deleteDate") : "";
	            		track_pricecode = track_territorial.getAttribute("priceBand")!=null ? track_territorial.getAttribute("priceBand") : "";
	            	}
	            	
	        		LicenseBasis track_license_basis = LicenseBasis.make();
	        		if(track_territories.size()==0) {
	        			track_license_basis.as_on_bundle(true);
	        		}
	        		else {
	        			track_license_basis.setTerritorial(track_territorial);
	        		}
	        		
	        		if(track_physicalReleaseDate.length()>0 && track_territories.size()==1) {
	        			track_cal.setTime(ymd.parse(track_physicalReleaseDate));
	        			track_license_basis.timeframe_from_datetime(track_cal.getTimeInMillis());
	    	        }
	            	
	    	        if(track_physicalReleaseDate.length()>0 && track_deleteDate.length()>0 && track_territories.size()==1) {
	    	        	track_cal.setTime(ymd.parse(track_deleteDate));
	    	        	track_license_basis.timeframe_to_datetime(cal.getTimeInMillis());
	    	        }
	    	        
	    	        if(track_territories.size()==1 && track_pricecode.length()==0) {
	    	        	track_license_basis.pricing_pricecode(track_pricecode);
	    	        }
	            	
		        	// license specifics -> empty!
		        	LicenseSpecifics track_license_specifics = LicenseSpecifics.make(); 
		        	track_license_specifics.as_on_bundle(true);
		        	
	        		// license_basis of Bundle / license_specifics of Bundle / others (?)
		        	Item item = Item.make(trackids, track_displayname, track_displayname, "", "audio", "", track_info, track_license_basis, track_license_specifics);
		        	            	
	            	// add Tags
	            	ItemTags track_tags = ItemTags.make(); 
	            	
	            	String genre = track.getAttribute("genre");
	            	if(genre!=null && genre.length()>0) {
	            		track_tags.addGenre(gc.convert(genre)); 
	            	}
	            	
	            	// explicit_lyrics
	            	if(track.getAttribute("explicitLyrics").length()>0) {
	            		if(track.getAttribute("explicitLyrics").toLowerCase().equals("false")) {
	            			track_tags.explicit_lyrics(ItemTags.EXPLICIT_LYRICS_FALSE);  
	            		}
	            		else if(track.getAttribute("explicitLyrics").toLowerCase().equals("true")) {
	            			track_tags.explicit_lyrics(ItemTags.EXPLICIT_LYRICS_TRUE);  
	            		}            		
	            	}
	            	
	            	item.tags(track_tags);
	        		
	            	track_info.texts(BundleTexts.make());

		        	// display_artistname
		        	String track_display_artistname = "";
	        		
	            	Vector<Element> track_artists = track.getChild("artists").getChildren("artist");
	            	for (Iterator<Element> itTrackArtists = track_artists.iterator(); itTrackArtists.hasNext();) {
	            		Element track_artist = itTrackArtists.next();        	
	            		String role = track_artist.getAttribute("role");
	            		if(role!=null && role.toLowerCase().equals("performer")) {
	            			contributor = Contributor.make(track_artist.getAttribute("knownAs"), Contributor.TYPE_PERFORMER, IDs.make());
	            		}
	            		else {
	            			contributor = Contributor.make(track_artist.getAttribute("knownAs"), Contributor.TYPE_DISPLAY_ARTIST, IDs.make());
	            		}
	            		String isPrimary = track_artist.getAttribute("isPrimary");
	            		if(isPrimary!=null && isPrimary.toLowerCase().equals("true")) {
	            			track_display_artistname = track_artist.getAttribute("knownAs");
	            		}

	            		item.addContributor(contributor);
	            		           		
	            	} 
	            	
	            	// set display artist name of track
	            	item.display_artistname(track_display_artistname);
	            	
	        		// add contributor
	        		Contributor track_contributor = Contributor.make(track_display_artistname, Contributor.TYPE_DISPLAY_ARTIST, IDs.make());
	             	item.addContributor(track_contributor); 	            	
	            	
	             	String track_production = null;
	             	if(track.getAttribute("pLine")!=null) { 
	             		track_production = track.getAttribute("pLine"); 
		             	if(track_production.length()>0) {
		    	         		contributor = Contributor.make(track_production, Contributor.TYPE_PRODUCTION, IDs.make());
		    	         		item.addContributor(contributor);  
		             	}
	             	}
	            	
	             	Vector<Element> files = track.getChild("avMediaItems").getChildren("avMediaItem");
	            	for (Iterator<Element> itFiles = files.iterator(); itFiles.hasNext();) {
	            		Element file = itFiles.next();
	             	
		        		ItemFile itemfile = ItemFile.make();
		        		
		        		if(file.getAttribute("length")!=null && file.getAttribute("length").toLowerCase().equals("full")) {
		        			itemfile.type("full");	
		        		}
		        		else if(file.getAttribute("length")!=null && file.getAttribute("length").toLowerCase().equals("sample")){
		        			itemfile.type("prelistening");
		        		}
		        		
		        		// check if file exist at path		        		
		        		String filename = file.getAttribute("name");
		        		File f = new File(path+filename);      		
		        		if(f!=null && f.exists()) {
		        			itemfile.setFile(f); //this will also set the filesize and calculate the checksums
		        			
		        			// set delivered path to file 
		        			itemfile.setLocation(FileLocation.make(filename,filename));
		        			
		        		} else {
		        			//file does not exist -> so we have to set the values "manually"
		        			
		        			//-> use filename as location
		        			itemfile.setLocation(FileLocation.make(filename,filename));
		        		
		        			//bitrate
		        			if(file.getAttribute("bitrate")!=null) {
		            			itemfile.bitrate(file.getAttribute("bitrate"));
		            		}
		        			
		        			//file type
		        			if(file.getAttribute("format")!=null) {
		            			itemfile.filetype(file.getAttribute("format"));
		            		}
		        			
		        		} 
		        		
			        	item.addFile(itemfile);
	            	}
	            	
		        	bundle.addItem(item);
	        	}
        	}
         	
        	feed.addBundle(bundle);
	        
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
