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
	// test?
    boolean onlytest = true;
    
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
	                   
	        // (2) get FeedInfo from import and create new FeedInfo for openSDX
	        String feedid = root.getAttribute("feedid");
	        if (feedid == null) feedid = "[NOT SET]";
	        Calendar cal = Calendar.getInstance();
	        long creationdatetime = cal.getTimeInMillis();
	        long effectivedatetime = cal.getTimeInMillis();
	        String partner = root.getAttribute("partner");
	        if (partner==null) partner = "[NOT SET]";
	        ContractPartner sender = ContractPartner.make(0, partner , "");
	        ContractPartner licensor = ContractPartner.make(1, "", "");
	        
	        FeedInfo feedinfo = FeedInfo.make(onlytest, feedid, creationdatetime, effectivedatetime, sender, licensor);

	        // path to importfile
	        String path = this.importFile.getParent()+File.separator;	        
	        
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
	        	if(release.getChildTextNN("longname").length()==0) {
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

	        	try {
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
	        	} catch (Exception ex) {
	        		
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
	        	
	        	if(root.getChild("streaming")!=null) {
	        		license_basis.streaming_allowed(Boolean.parseBoolean(root.getChildText("streaming")));
	        	}
	        	
	        	// license specifics -> empty!
	        	LicenseSpecifics license_specifics = LicenseSpecifics.make();
	        	
	        	// create bundle with gathered information
	        	Bundle bundle = Bundle.make(bundleids, displayname, name, version, display_artist, info, license_basis, license_specifics);
	        	
	        	// add contributor to bundle
	        	Contributor contributor = Contributor.make(label.getChildTextNN("name"), Contributor.TYPE_LABEL, ids);
	        	contributor.www(InfoWWW.make().homepage(label.getChildTextNN("website")));
	        	bundle.addContributor(contributor);
	        	
	        	Vector<Element> artists = release.getChild("artists").getChildren("artist");
	        	for (Iterator<Element> itArtists = artists.iterator(); itArtists.hasNext();) {
	        		Element artist = itArtists.next();
	        		contributor = null;
	        		
	        		vecIds = artist.getChildren("id");
	            	ids = getIDs(vecIds);
	            	
	            	String role = artist.getChildTextNN("role");
	                contributor = Contributor.make(artist.getChildText("name"), getRole(role), ids); 
	                
	                if(role.equals("performer")) {
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
        		   
	        	// add File -> frontcover
	        	Element ressource = release.getChild("resource");
	        	if(ressource != null && ressource.getAttribute("type").equals("frontcover")) {
	        		ItemFile itemfile = ItemFile.make();
	        		itemfile.type("cover");
	        		String width = "";
	        		String height = "";	        		

	        		String filename = ressource.getChildTextNN("uri");
	        		File f = new File(path+filename);
	        		if(f!=null && f.exists()) {
	        			itemfile.setFile(f); //this will also set the filesize and calculate the checksums
	        				        		
		        		Vector<Element> qualities = ressource.getChildren("quality");
			        	for (Iterator<Element> itQualities = qualities.iterator(); itQualities.hasNext();) {
			        		Element quality = itQualities.next();
			        		if(quality.getAttribute("type").equals("width")) {
			        			if(quality.getText().length()>0) {	
			        				width = quality.getText();
			        			}	        			
			        		}
			        		else if(quality.getAttribute("type").equals("height")) {
			        			if(quality.getText().length()>0) {	
			        				height = quality.getText();
			        			}	        			
			        		}			        		
			        	}
			        	
	        		} else {
	        			//file does not exist -> so we have to set the values "manually"
	        			
	        			//-> use filename as location
	        			itemfile.setLocation(FileLocation.make(filename));	        		
	        		
		        		if(ressource.getChild("checksum")!=null && ressource.getChild("checksum").getAttribute("type").equals("md5")) {	        			
	            			String sMd5 =  ressource.getChildText("checksum");
	            			if (sMd5!=null) {
	            				byte[] md5 = SecurityHelper.HexDecoder.decode(sMd5);
	            				itemfile.checksums(Checksums.make().md5(md5));
	            			}
		        		}	
		        				        		
		        		Vector<Element> qualities = ressource.getChildren("quality");
			        	for (Iterator<Element> itQualities = qualities.iterator(); itQualities.hasNext();) {
			        		Element quality = itQualities.next();
			        		if(quality.getAttribute("type").equals("size")) {
			        			if(quality.getText().length()>0) {	
			        				itemfile.bytes(Integer.parseInt(quality.getText()));
			        			}	        			
			        		}
			        		else if(quality.getAttribute("type").equals("width")) {
			        			if(quality.getText().length()>0) {	
			        				width = quality.getText();
			        			}	        			
			        		}
			        		else if(quality.getAttribute("type").equals("height")) {
			        			if(quality.getText().length()>0) {	
			        				height = quality.getText();
			        			}	        			
			        		}
			        		else if(quality.getAttribute("type").equals("datatype")) {
			        			if(quality.getText().length()>0) {	
			        				itemfile.filetype(quality.getText());
			        			}	        			
			        		}			        		
			        	}
			        	
		        		itemfile.filetype(ressource.getChildTextNN("datatype"));
			        				        	
	        		}
	        		
	        		// set dimension of cover
	        		if(width.length()>0 && height.length()>0) itemfile.dimension(Integer.parseInt(width), Integer.parseInt(height));	        		
	        		
	        		bundle.addFile(itemfile);
	        	}
        		
	        	Vector<Element> tracks = release.getChild("tracks").getChildren("track");
	        	for (Iterator<Element> itTracks = tracks.iterator(); itTracks.hasNext();) {
	        		Element track = itTracks.next();
		        	
	        		// IDs of track
	        		Vector<Element> track_vecIds = track.getChildren("id");
		        	IDs trackids = getIDs(track_vecIds);
		
		        	
		        	// displayname
		        	String track_displayname = track.getChildTextNN("title");
		        	
		        	// name
		        	String track_name = "";
		        	if(track.getChildTextNN("longname").length()==0) {
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
		        	
		        	// num
		        	if(track_label.getChildTextNN("position").length()>0) {
		        		track_info.num(Integer.parseInt(track_label.getChildTextNN("position")));
		        	}
		        	
		        	// setnum
		        	if(track_label.getChildTextNN("cdsourcenum").length()>0) {
		        		track_info.setnum(Integer.parseInt(track_label.getChildTextNN("cdsourcenum")));
		        	}
		        	
		        	//origin_country
		        	track_info.origin_country(track.getChildText("origincountry"));
		        	
		        	// license specifics -> empty!
		        	LicenseSpecifics track_license_specifics = LicenseSpecifics.make(); 
		        	
	        		// license_basis of Bundle / license_specifics of Bundle / others (?)
		        	Item item = Item.make(track_labelids, track_displayname, track_name, track_version, "audio", track_display_artist, track_info, LicenseBasis.makeAsOnBundle(), track_license_specifics);
		        	
		        	// add IDs
		        	item.ids(trackids);
		        	
		        	// file
		        	Element track_ressource = track.getChild("resource");		        	
		        	ItemFile itemfile = ItemFile.make();
		        	if(track_ressource.getAttribute("type").equals("audiofile")) itemfile.type("full");
		        		
		        	itemfile.filetype(track_ressource.getChildTextNN("datatype"));
	        		
	        		String track_filename = track_ressource.getChildTextNN("uri");
	        		File f = new File(path+track_filename);
	        		if(f!=null && f.exists()) {
	        			itemfile.setFile(f); //this will also set the filesize and calculate the checksums
	        			
		        		Vector<Element> track_qualities = track_ressource.getChildren("quality");
			        	for (Iterator<Element> track_itQualities = track_qualities.iterator(); track_itQualities.hasNext();) {
			        		Element track_quality = track_itQualities.next();
			        		if(track_quality.getAttribute("type").equals("channelmode")) {
			        			if(track_quality.getText().length()>0) {	
			        				itemfile.channels(track_quality.getText());
			        			}	        			
			        		}		        		
			        		else if(track_quality.getAttribute("type").equals("duration")) {
			        			if(track_quality.getText().length()>0) {	
			        				item.getInformation().playlength(Integer.parseInt(track_quality.getText()));
			        			}	        			
			        		}
			        	}
			        	
	        		} else {
	        			//file does not exist -> so we have to set the values "manually"
	        			
	        			//-> use filename as location
	        			itemfile.setLocation(FileLocation.make(track_filename));	        		
	        		
		        		if(track_ressource.getChild("checksum")!=null && track_ressource.getChild("checksum").getAttribute("type").equals("md5")) {	        			
	            			String sMd5 =  track_ressource.getChildText("checksum");
	            			if (sMd5!=null) {
	            				byte[] md5 = SecurityHelper.HexDecoder.decode(sMd5);
	            				itemfile.checksums(Checksums.make().md5(md5));
	            			}
		        		}	
		        					        	
		        		Vector<Element> track_qualities = track_ressource.getChildren("quality");
			        	for (Iterator<Element> track_itQualities = track_qualities.iterator(); track_itQualities.hasNext();) {
			        		Element track_quality = track_itQualities.next();
			        		if(track_quality.getAttribute("type").equals("size")) {
			        			if(track_quality.getText().length()>0) {	
			        				itemfile.bytes(Integer.parseInt(track_quality.getText()));
			        			}
			        		}
			        		else if(track_quality.getAttribute("type").equals("channelmode")) {
			        			if(track_quality.getText().length()>0) {	
			        				itemfile.channels(track_quality.getText());
			        			}	        			
			        		}			        				        		
			        		else if(track_quality.getAttribute("type").equals("duration")) {
			        			if(track_quality.getText().length()>0) {	
			        				item.getInformation().playlength(Integer.parseInt(track_quality.getText()));
			        			}	        			
			        		}
			        	}			        	
	        		}
	        		
        			item.addFile(itemfile);	
	        		
		        	// add Tags
		        	ItemTags track_tags = ItemTags.make();
		        	
		        	Vector<Element> track_genres = track.getChild("genres").getChildren("genre");
		        	for (Iterator<Element> track_itGenres = track_genres.iterator(); track_itGenres.hasNext();) {
		        		Element track_genre = track_itGenres.next();
		        		
		        		track_tags.addGenre(track_genre.getChildText("name"));
		        	}
		        	
		        	String track_bundle_only = track.getChildTextNN("bundled");
		        	if(track_bundle_only.equals("false")) {
		        		tags.bundle_only(false);	
		        	}
		        	else if(track_bundle_only.equals("true")) {
		        		tags.bundle_only(true);	
		        	}
		        	
	        		item.tags(tags);	
		        	
		        	// add contributor to item
		        	Contributor track_contributor = Contributor.make(track_label.getChildTextNN("name"), Contributor.TYPE_LABEL, ids);
		        	contributor.www(InfoWWW.make().homepage(track_label.getChildTextNN("website")));
		        	item.addContributor(track_contributor);
		        	
		        	Vector<Element> track_artists = track.getChild("artists").getChildren("artist");
		        	for (Iterator<Element> track_itArtists = track_artists.iterator(); track_itArtists.hasNext();) {
		        		Element track_artist = track_itArtists.next();
		        		
		        		Vector<Element> track_artist_vecIds = track_artist.getChildren("id");
		        		IDs track_artist_ids = getIDs(track_artist_vecIds);
		            	
		            	String track_artists_role = track_artist.getChildTextNN("role");
		            	track_contributor = Contributor.make(track_artist.getChildTextNN("name"), getRole(track_artists_role), track_artist_ids);       	
		            	
		            	track_contributor.www(InfoWWW.make().homepage(track_artist.getChildTextNN("website")));
		            	
		            	item.addContributor(track_contributor);
		        	}
		        	
		        	// ToDo: get more information?!
		        	
		        	bundle.addItem(item);
		        }
	        
	        	feed.addBundle(bundle);
	        }
		} catch (Exception e) {
			// e.printStackTrace();
			ir.succeeded = false;
			ir.errorMessage = e.getMessage();			
			ir.exception = e;			
		}		        
        return feed;
	}
	
	public Feed getFormatedFeedFromImport() {			
		return this.getImportFeed();
	
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
	
	private String getRole(String text) {
		String role = "[NOT SET]";

    	if(text.equals("performer")) {
    		role = Contributor.TYPE_DISPLAY_ARTIST;
    	}
    	else if(text.equals("lyricist")) {
    		role = Contributor.TYPE_VOCALS; 
    	}
    	else if(text.equals("composer")) {
    		role = Contributor.TYPE_COMPOSER; 
    	}
    	else if(text.equals("arranger")) {
    		role = Contributor.TYPE_COMPILATOR; 
    	}   

		return role;		
	}	

}
