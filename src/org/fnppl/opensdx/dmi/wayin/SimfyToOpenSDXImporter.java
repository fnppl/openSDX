package org.fnppl.opensdx.dmi.wayin;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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

public class SimfyToOpenSDXImporter extends OpenSDXImporterBase {
	DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
	private Result ir = Result.succeeded();
	// test?
	boolean onlytest = true;
    
	public SimfyToOpenSDXImporter(ImportType type, File impFile, File savFile) {
		super(type, impFile, savFile);
	}
	
	public SimfyToOpenSDXImporter(File impFile) {
		super(ImportType.getImportType("simfy"), impFile, null);
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
	        
	        if(root.getChild("updated_at")!=null && root.getChildTextNN("updated_at").length()>0) {
	        	cal.setTime(ymd.parse(root.getChildText("updated_at").substring(0, 9)));
	        }	        
	        
	        long creationdatetime = cal.getTimeInMillis();	        
	        long effectivedatetime = cal.getTimeInMillis();
	        
	        String lic = root.getChildTextNN("licensor");
	        if (lic.length()==0) lic = "[NOT SET]";
	        
	        ContractPartner sender = ContractPartner.make(0, lic , "");
	        ContractPartner licensor = ContractPartner.make(1, lic, "");
	        ContractPartner licensee = ContractPartner.make(ContractPartner.ROLE_LICENSEE,"","");
	        
	        FeedInfo feedinfo = FeedInfo.make(onlytest, feedid, creationdatetime, effectivedatetime, sender, licensor, licensee);
	        
	        // path to importfile
	        String path = this.importFile.getParent()+File.separator;
	        
	        // (3) create new feed with feedinfo
	        feed = Feed.make(feedinfo);              
	
	        // Information
        	String streetReleaseDate = root.getChildTextNN("original_released_on");	        
	        if(streetReleaseDate.length()>0) {
	        	cal.setTime(ymd.parse(streetReleaseDate));
	        }
	        else {
	        	// MUST: when not provided then today
	        	cal.setTime(new Date());
	        }
	        
	        // streetRelease = physicalRelease (?)
	        long srd = cal.getTimeInMillis();
        	long prd = cal.getTimeInMillis();
        	
        	BundleInformation info = BundleInformation.make(srd, prd);
        	
        	// IDs of bundle -> more (?)
        	IDs bundleids = IDs.make();
        	if(root.getChild("upc")!=null) bundleids.upc(root.getChildTextNN("upc"));
        	if(root.getChild("isrc")!=null) bundleids.isrc(root.getChildTextNN("isrc"));

        	// displayname
        	String displayname = root.getChildTextNN("title");
        	
        	// display_artistname
        	String display_artistname = root.getChildTextNN("artist_name");
        	
        	// license basis
        	Territorial territorial = Territorial.make();
        	
        	// Release
        	LicenseBasis license_basis = LicenseBasis.make(territorial, srd, prd);
        	
        	// license specifics -> empty!
        	LicenseSpecifics license_specifics = LicenseSpecifics.make();  
        	
        	
        	
        	Bundle bundle = Bundle.make(bundleids, displayname, displayname, "", display_artistname, info, license_basis, license_specifics);
        	
        	// add Tags
        	ItemTags tags = ItemTags.make();   		
        	tags.addGenre(root.getChildTextNN("genre"));
        	
    		bundle.tags(tags);        	
        	
        	Contributor contributor = Contributor.make(root.getChildTextNN("label"), Contributor.TYPE_LABEL, IDs.make());
        	bundle.addContributor(contributor);
        	
        	contributor = Contributor.make(root.getChildTextNN("artist_name"), Contributor.TYPE_DISPLAY_ARTIST, IDs.make());
         	bundle.addContributor(contributor);
         	
         	String copyright = root.getChildTextNN("c_line");
         	String production = root.getChildTextNN("p_line");
         	
         	if(copyright.length()>0) {
	         		contributor = Contributor.make(copyright.substring(5), Contributor.TYPE_COPYRIGHT, IDs.make());
	         		contributor.year(copyright.substring(0, 4));
	         		bundle.addContributor(contributor);  
         	}
         	
         	if(production.length()>0) {
	         		contributor = Contributor.make(production.substring(5), Contributor.TYPE_PRODUCTION, IDs.make());
	         		contributor.year(production.substring(0, 4));
	         		bundle.addContributor(contributor);  
         	}         	
        	
        	// cover: license_basis & license_specifics from bundle, right?
        	Element cover = root.getChild("cover");
        	if(cover != null) {
        		ItemFile itemfile = ItemFile.make(); 
        		itemfile.type("cover");
        		// check if file exist at path
        		String filename = cover.getChildTextNN("file_name");
        		File f = new File(path+filename);
        		if(f!=null && f.exists()) {
        			itemfile.setFile(f);
        			
        			// set delivered path to file 
        			itemfile.setLocation(FileLocation.make(filename));
        		} else {
        			//file does not exist -> so we have to set the values "manually"
        			
        			//-> use filename for location
        			itemfile.setLocation(FileLocation.make(filename));
        		
        			//file size
        			if(cover.getChild("file_size")!=null) {
            			itemfile.bytes(Integer.parseInt(cover.getChildText("file_size")));
            		}        		
            		
            		// checksum md5
            		if(cover.getChild("file_checksum")!=null) {
            			String sMd5 =  cover.getChildText("file_checksum");
            			if (sMd5!=null) {
            				byte[] md5 = SecurityHelper.HexDecoder.decode(sMd5);
            				itemfile.checksums(Checksums.make().md5(md5));
            			}
            		}
        		}
        		
        		// set dimension of cover
        		String width = cover.getChildTextNN("width");
        		String height = cover.getChildTextNN("height");
        		if(width.length()>0 && height.length()>0) itemfile.dimension(Integer.parseInt(width), Integer.parseInt(height));
       
        		bundle.addFile(itemfile);
        	}
        	
        	Vector<Element> tracks = root.getChild("tracks").getChildren("track");
        	for (Iterator<Element> itTracks = tracks.iterator(); itTracks.hasNext();) {
        		Element track = itTracks.next();

        		IDs trackids = IDs.make();
            	if(track.getChild("upc")!=null) trackids.upc(track.getChildTextNN("upc"));
            	if(track.getChild("isrc")!=null) trackids.isrc(track.getChildTextNN("isrc"));
        		
	        	// displayname
	        	String track_displayname = track.getChildTextNN("title");  
	        	
	        	// display_artistname
	        	String track_display_artistname = track.getChildTextNN("artist_name");
	        	
	        	BundleInformation track_info = BundleInformation.make(srd, prd);		        	
	        	
	        	// num
	        	if(track.getChildTextNN("track_number").length()>0) {
	        		track_info.num(Integer.parseInt(track.getChildText("track_number")));
	        	}
	        	
	        	// setnum
	        	if(track.getChildTextNN("disk_number").length()>0) {
	        		track_info.setnum(Integer.parseInt(track.getChildText("disk_number")));
	        	} 
	        	
	        	// tracklength
        		if(track.getChildTextNN("duration").length()>0) {
        			track_info.playlength(Integer.parseInt(track.getChildText("duration")));     			
        		}
        		
        		// track license basis
        		LicenseBasis track_license_basis = LicenseBasis.make();
        		
        		Territorial track_territorial = Territorial.make();
        		
        		if(track.getChild("rights")!=null) {
	            	Vector<Element> tracks_rights = track.getChild("rights").getChildren("right");
	            	for (Iterator<Element> itRights = tracks_rights.iterator(); itRights.hasNext();) {
	            		Element track_right = itRights.next();
	            		String r = track_right.getChildText("country_code");
	            		if(r.length()>0) {
	            			if(r.equals("**")) { 
	            				r="WW";
	            				// if worldwide then add streamable information -> keep an eye on these (!)
	            	        	String streamable_from = track_right.getChildTextNN("streamable_from");	        
	            		        if(streamable_from.length()>0) {
	            		        	cal.setTime(ymd.parse(streamable_from));
	            		        	track_license_basis.timeframe_from_datetime(cal.getTimeInMillis());
	            		        }	        
	            		        
	            	        	if(track_right.getChild("allows_streaming")!=null) {
	            	        		track_license_basis.streaming_allowed(Boolean.parseBoolean(track_right.getChildText("allows_streaming")));
	            	        	}
	            			}
	            			track_territorial.allow(r);
	            		}
	            	} 
        		}
	        	
            	track_license_basis.setTerritorial(track_territorial);
            	
	        	// license specifics -> empty!
	        	LicenseSpecifics track_license_specifics = LicenseSpecifics.make();         	
	        	
        		// license_basis of Bundle / license_specifics of Bundle / others (?)
	        	Item item = Item.make(trackids, track_displayname, track_displayname, "", "audio", track_display_artistname, track_info, track_license_basis, track_license_specifics);
	        	            	
        		// add contributor
        		Contributor track_contributor = Contributor.make(track.getChildTextNN("artist_name"), Contributor.TYPE_DISPLAY_ARTIST, IDs.make());
             	item.addContributor(track_contributor);  
             	
            	// add Tags
            	ItemTags track_tags = ItemTags.make();   		
            	track_tags.addGenre(track.getChildTextNN("genre"));            	
            	
            	// explicit_lyrics
            	if(track.getChildTextNN("explicit_lyrics").length()>0) {
            		if(track.getChildTextNN("explicit_lyrics").toLowerCase().equals("false")) {
            			track_tags.explicit_lyrics(ItemTags.EXPLICIT_LYRICS_FALSE);  
            		}
            		else if(track.getChildTextNN("explicit_lyrics").toLowerCase().equals("true")) {
            			track_tags.explicit_lyrics(ItemTags.EXPLICIT_LYRICS_TRUE);  
            		}            		
            	}
            	
            	item.tags(track_tags);	        	
	        	
        		ItemFile itemfile = ItemFile.make();
        		itemfile.type("full");
        		// check if file exist at path
        		String filename = track.getChildTextNN("file_name");
        		File f = new File(path+filename);      		
        		if(f!=null && f.exists()) {
        			itemfile.setFile(f); //this will also set the filesize and calculate the checksums
        			
        			// set delivered path to file 
        			itemfile.setLocation(FileLocation.make(filename));        			
        		} else {
        			//file does not exist -> so we have to set the values "manually"
        			
        			//-> use filename as location
        			itemfile.setLocation(FileLocation.make(filename));
        		
        			//file size
        			if(track.getChild("file_size")!=null) {
            			itemfile.bytes(Integer.parseInt(track.getChildText("file_size")));
            		}        		
            		
            		// checksum md5
            		if(track.getChild("file_checksum")!=null) {
            			String sMd5 =  track.getChildText("file_checksum");
            			if (sMd5!=null) {
            				byte[] md5 = SecurityHelper.HexDecoder.decode(sMd5);
            				itemfile.checksums(Checksums.make().md5(md5));
            			}
            		}
        		}        		
        		
	        	item.addFile(itemfile);
	        	
	        	bundle.addItem(item);
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
