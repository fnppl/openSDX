package org.fnppl.opensdx.dmi.wayin;

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

public class FinetunesToOpenSDXImporter extends OpenSDXImporterBase {
	DateFormat ymd = new SimpleDateFormat("yyyyMMdd");
	Result ir = Result.succeeded();
	
	public FinetunesToOpenSDXImporter(ImportType type, File impFile, File savFile) {
		super(type, impFile, savFile);
	}
	
	public FinetunesToOpenSDXImporter(File impFile) {
		super(ImportType.getImportType("finetunes"), impFile, null);
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
			e.printStackTrace();			
			ir.succeeded = false;
			ir.errorMessage = e.getMessage();			
			ir.exception = e;			
		}	
		
		return ir;			
	}
	
	private IDs getIDs(Vector<Element> vec) {
		IDs ids = IDs.make();
    	for (Iterator<Element> itIDs = vec.iterator(); itIDs.hasNext();) {
    		Element id = itIDs.next();
    		String idType = id.getAttribute("type");
    		if(idType.equals("finetunes")) {
    			ids.finetunesid(id.getText());
    		}
    		else if(idType.equals("ean")) {
    			ids.upc(id.getText());
    		}
    		else if(idType.equals("gvl")) {
    			ids.gvl(id.getText());
    		}   
    		else if(idType.equals("isrc")) {
    			ids.isrc(id.getText());
    		}    		
    	}
		return ids;		
	}
	
	private Feed getImportFeed() {
		// do the import
		Feed feed = null;
		
		try {
			// (1) get XML-Data from import document
	        Document impDoc = Document.fromFile(this.importFile);
	        Element root = impDoc.getRootElement();
	                   
	        // (2) get FeedInfo from import and set create new FeedInfo for openSDX
	        boolean onlytest = true;
	        String feedid = root.getAttribute("feedid");
	        Calendar cal = Calendar.getInstance();
	        long creationdatetime = cal.getTimeInMillis();
	        long effectivedatetime = cal.getTimeInMillis();
	        ContractPartner sender = ContractPartner.make(0, root.getAttribute("partner"), "");
	        ContractPartner licensor = ContractPartner.make(1, "", "");
	        
	        FeedInfo feedinfo = FeedInfo.make(onlytest, feedid, creationdatetime, effectivedatetime, sender, licensor);
	        
	        // (3) create new feed with feedinfo
	        feed = Feed.make(feedinfo);              
	
	        // (4) formating stuff -> put importdata (bundles/items) in OSDX format
	        // -> releases to bundles/items
	        Vector<Element> releases = root.getChildren("release");
	        for (Iterator<Element> itReleases = releases.iterator(); itReleases.hasNext();) {
	        	Element release = itReleases.next();
	   
	        	// IDs of bundle
	        	Vector<Element> vecIds = release.getChildren("id");
	        	IDs bundleids = getIDs(vecIds);
	
	        	// displayname
	        	String displayname = release.getChildText("title");
	        	
	        	// name
	        	String name = "";
	        	if(release.getChildText("longname").length()==0) {
	        		name = release.getChildText("title");
	        	}
	        	else {
	        		name = release.getChildText("longname");
	        	}
	        	
	        	// version
	        	String version = release.getChildText("version");
	        	
	        	// display_artist
	        	String display_artist = "";
	        	
	        	// contributors label/artists/...
	        	Element label = release.getChild("label");
	        	
	        	vecIds = label.getChildren("id");
	        	IDs ids = getIDs(vecIds); 
	        	
	        	// Information
	        	String streetReleaseDate = release.getChild("schedules").getChildText("streetreleasedate");
	        	cal.setTime(ymd.parse(streetReleaseDate));
	        	long srd = cal.getTimeInMillis();
	        	String digitalReleaseDate = release.getChild("schedules").getChildText("digitalreleasedate");
	        	cal.setTime(ymd.parse(digitalReleaseDate));
	        	long prd = cal.getTimeInMillis();
	        	
	        	BundleInformation info = BundleInformation.make(srd, prd);
	        	
	        	Vector<Element> infotexts = release.getChild("infotexts").getChildren();
	        	for (Iterator<Element> itInfotexts = infotexts.iterator(); itInfotexts.hasNext();) {
	        		Element infotext = itInfotexts.next();
	        		
	        		if(infotext.getName().equals("promotext")) {
	        			info.setPromotext(infotext.getAttribute("lang"), infotext.getText());
	        		}
	        		else if(infotext.getName().equals("teasertext")) {
	        			info.setTeasertext(infotext.getAttribute("lang"), infotext.getText());
	        		}
	        	}            	
	        	
	        	// license basis
	        	String releaseFrom = release.getChild("schedules").getChildText("youmayreleasefrom");
	        	cal.setTime(ymd.parse(releaseFrom));
	        	long rf = cal.getTimeInMillis();
	        	String releaseTo = release.getChild("schedules").getChildText("youmayreleaseuntil");
	        	cal.setTime(ymd.parse(releaseTo));
	        	long rt = cal.getTimeInMillis();   
	        	
	        	Territorial territorial = Territorial.make();
	        	
	        	Vector<Element> territories = release.getChild("distributionterritories").getChild("allowances").getChildren("territory");
	        	for (Iterator<Element> itTerritories= territories.iterator(); itTerritories.hasNext();) {            		
	        		Element territory = itTerritories.next();
	        		
	        		territorial.allow(territory.getText());
	        	}
	        	
	        	LicenseBasis license_basis = LicenseBasis.make(territorial, rf, rt);
	        	
	        	// license specifics -> empty!
	        	LicenseSpecifics license_specifics = LicenseSpecifics.make();
	        	
	        	// create bundle with gathered information
	        	Bundle bundle = Bundle.make(bundleids, displayname, name, version, display_artist, info, license_basis, license_specifics);
	        	
	        	// add contributor to bundle
	        	Contributor contributor = Contributor.make(label.getChildText("name"), Contributor.TYPE_LABEL, ids);
	        	contributor.www(InfoWWW.make().homepage(label.getChildText("website")));
	        	bundle.addContributor(contributor);
	        	
	        	Vector<Element> artists = release.getChild("artists").getChildren("artist");
	        	for (Iterator<Element> itArtists = artists.iterator(); itArtists.hasNext();) {
	        		Element artist = itArtists.next();
	        		contributor = null;
	        		
	        		vecIds = artist.getChildren("id");
	            	ids = getIDs(vecIds);
	            	
	            	String role = artist.getChildText("role");
	            	if(role.equals("performer")) {
	                	contributor = Contributor.make(artist.getChildText("name"), Contributor.TYPE_DISPLAY_ARTIST, ids); 
	                	display_artist = artist.getChildText("name");
	            	}
	            	
	            	contributor.www(InfoWWW.make().homepage(label.getChildText("website")));
	            	bundle.addContributor(contributor);
	        	}
	        	
	        	// add Tags
	        	ItemTags tags = ItemTags.make();
	        	
	        	Vector<Element> genres = release.getChild("genres").getChildren("genre");
	        	for (Iterator<Element> itGenres = genres.iterator(); itGenres.hasNext();) {
	        		Element genre = itGenres.next();
	        		
	        		tags.addGenre(genre.getChildText("name"));
	        	}
        		bundle.tags(tags);	 
	        	
	        	/* ToDO !!!
	        	// add Items
	        	Element ressource = release.getChild("resource");
	        	if(ressource.getAttribute("type").equals("frontcover")) {
	        		Item item = Item.make(null, null, null, "cover", null, null, null, null, null);
	        		ItemFile file = ItemFile.make();
	        		file.type(ressource.getChildText("datatype"));
	        		if(ressource.getChild("checksum").getAttribute("type").equals("md5")) {
	        			file.checksums(Checksums.make().md5(ressource.getChildText("checksum").getBytes()));
	        		}
	        		item.addFile(file);
	        		bundle.addItem(item);
	        	}

	        	Vector<Element> tracks = release.getChild("tracks").getChildren("track");
	        	for (Iterator<Element> itTracks = tracks.iterator(); itTracks.hasNext();) {
	        		Element track = itTracks.next();
		        	
	        		// IDs of track
	        		Vector<Element> track_vecIds = track.getChildren("id");
		        	IDs trackids = getIDs(track_vecIds);
		
		        	// displayname
		        	String track_displayname = track.getChildText("title");
		        	
		        	// name
		        	String track_name = "";
		        	if(track.getChildText("longname").length()==0) {
		        		track_name = track.getChildText("title");
		        	}
		        	else {
		        		track_name = track.getChildText("longname");
		        	}
		        	
		        	// version
		        	String track_version = track.getChildText("version");
		        	
		        	// display_artist
		        	String track_display_artist = "";
		        	
		        	// contributors label/artists/...
		        	Element track_label = track.getChild("label");
		        	
		        	track_vecIds = track_label.getChildren("id");
		        	IDs track_labelids = getIDs(track_vecIds);
		        	
		        	BundleInformation track_info = BundleInformation.make(srd, prd);		        	
		        	
	        		// license_basis of Bundle / license_specifics of Bundle / others (?)
		        	Item item = Item.make(track_labelids, track_displayname, track_name, track_version, "audio", track_display_artist, track_info, license_basis, license_specifics);
		        	
		        	// ToDo: gather more information!
	        		
		        	bundle.addItem(item);
	        	}
	        	*/   	
	        	
	            feed.addBundle(bundle);
	        }
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

}
