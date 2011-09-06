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
	        
	        long creationdatetime = cal.getTimeInMillis();	        
	        long effectivedatetime = cal.getTimeInMillis();
	        
	        String lic = root.getChildTextNN("provider");
	        if (lic.length()==0) lic = "[NOT SET]";
	        
	        ContractPartner sender = ContractPartner.make(ContractPartner.ROLE_SENDER, lic , "");
	        ContractPartner licensor = ContractPartner.make(ContractPartner.ROLE_LICENSOR, lic, "");
	        ContractPartner licensee = ContractPartner.make(ContractPartner.ROLE_LICENSEE,"","");
	        
	        FeedInfo feedinfo = FeedInfo.make(onlytest, feedid, creationdatetime, effectivedatetime, sender, licensor, licensee);
	        
	        // (3) create new feed with feedinfo
	        feed = Feed.make(feedinfo); 	        
	        
	        // path to importfile
	        String path = this.importFile.getParent()+File.separator;
	        
	        Element album = root.getChild("album");
	        
	        // Information
        	String streetReleaseDate = album.getChildTextNN("album_release_date");	        
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
	        
        	// language
	        if(root.getChild("language")!=null && root.getChildTextNN("language").length()>0) {
	        	info.main_language(root.getChildText("language").substring(0, 2));
	        }
	        	        
        	// IDs of bundle -> more (?)
        	IDs bundleids = IDs.make();
        	if(album.getChild("album_upc")!=null) bundleids.upc(album.getChildTextNN("album_upc"));
        	
        	// displayname
        	String displayname = album.getChildTextNN("album_title");
        	      	
        	// display_artistname
        	String display_artistname = album.getChildTextNN("album_display_artist");
        	
        	// license basis
        	Territorial territorial = Territorial.make();
        	
        	// Release
        	LicenseBasis license_basis = LicenseBasis.make(territorial, srd, prd);
        	
        	// license specifics -> empty!
        	LicenseSpecifics license_specifics = LicenseSpecifics.make();  
        	
        	// receiver -> "MUST" -> empty!
        	feedinfo.receiver(Receiver.make(Receiver.TRANSFER_TYPE_OSDX_FILESERVER));
        	
        	Bundle bundle = Bundle.make(bundleids, displayname, displayname, "", display_artistname, info, license_basis, license_specifics);  

        	// add contributor label
        	Contributor con = Contributor.make(album.getChildTextNN("album_label_name"), Contributor.TYPE_LABEL, IDs.make());
        	bundle.addContributor(con);
        	
        	// add contributor display_artist
        	con = Contributor.make(display_artistname, Contributor.TYPE_DISPLAY_ARTIST, IDs.make());
        	bundle.addContributor(con);
        	
        	Vector<Element> contributors = album.getChild("album_artists").getChildren("artist");
        	for (Iterator<Element> itContributors = contributors.iterator(); itContributors.hasNext();) {
        		Element contributor = itContributors.next();
 
        		if(contributor.getChild("roles")!=null && contributor.getChild("roles").getChild("role")!=null) {
        			String role = contributor.getChild("roles").getChildTextNN("role").trim().toLowerCase();
        			if(role.equals("performer")) {
        				con = Contributor.make(contributor.getChildTextNN("name"), Contributor.TYPE_PERFORMER, IDs.make());	
        			}
        			else if(role.equals("featuring")) {
        				con = Contributor.make(contributor.getChildTextNN("name"), Contributor.TYPE_FEATURING, IDs.make());	
        			}
        			else if(role.equals("composer")) {
        				con = Contributor.make(contributor.getChildTextNN("name"), Contributor.TYPE_COMPOSER, IDs.make());	
        			}
        			else if(role.equals("producer")) {
        				con = Contributor.make(contributor.getChildTextNN("name"), Contributor.TYPE_PRODUCER, IDs.make());	
        			} 
        			
        			// Maybe more roles? Insert!
        			
                	bundle.addContributor(con);
        		}
        		else {
        			con = Contributor.make(contributor.getChildTextNN("name"), Contributor.TYPE_PERFORMER, IDs.make());
                	bundle.addContributor(con);
        		}
        	}
        	
         	String copyright = album.getChildTextNN("album_copyright_cline");
         	String production = album.getChildTextNN("album_copyright_pline");
         	
         	if(copyright.length()>0) {
	         		con = Contributor.make(copyright.substring(5), Contributor.TYPE_COPYRIGHT, IDs.make());
	         		con.year(copyright.substring(0, 4));
	         		bundle.addContributor(con);  
         	}
         	
         	if(production.length()>0) {
	         		con = Contributor.make(production.substring(5), Contributor.TYPE_PRODUCTION, IDs.make());
	         		con.year(production.substring(0, 4));
	         		bundle.addContributor(con);  
         	} 
         	
         	// cover: license_basis & license_specifics from bundle, right?        	
        	if(album.getChild("album_artwork_files")!=null && album.getChild("album_artwork_files").getChild("file") != null) {
        		Element cover = album.getChild("album_artwork_files").getChild("file");
        		
        		ItemFile itemfile = ItemFile.make(); 
        		itemfile.type("frontcover");
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
        			if(cover.getChild("size")!=null) {
            			itemfile.bytes(Integer.parseInt(cover.getChildText("size")));
            		}        		
            		
            		// checksum md5
            		if(cover.getChild("checksum")!=null) {
            			Element cs = cover.getChild("checksum");
            			if (cs!=null) {
            				if(cs.getAttribute("type").equals("md5")) {
            					String sMd5 =  cover.getChildText("checksum");
	            				byte[] md5 = SecurityHelper.HexDecoder.decode(sMd5);
	            				itemfile.checksums(Checksums.make().md5(md5));
            				}
            			}
            		}
        		}
       
        		bundle.addFile(itemfile);
        	}
        	
        	
        	// init GenreConverter
        	GenreConverter gc = GenreConverter.getInstance(GenreConverter.PIE_TO_OPENSDX);
        	       	
        	// add Tags
        	ItemTags tags = ItemTags.make();
        	
        	Vector<Element> genres = album.getChild("album_genres").getChildren("genre");
        	for (Iterator<Element> itGenres = genres.iterator(); itGenres.hasNext();) {
        		Element genre = itGenres.next();        	
        		tags.addGenre(gc.convert(genre.getText()));
        	}
        	
    		bundle.tags(tags);        	        	
        	
        	Vector<Element> tracks = album.getChild("album_tracks").getChildren("track");
        	for (Iterator<Element> itTracks = tracks.iterator(); itTracks.hasNext();) {
        		Element track = itTracks.next();

        		IDs trackids = IDs.make();
            	if(track.getChild("track_isrc")!=null) trackids.isrc(track.getChildTextNN("track_isrc"));
        		
	        	// displayname
	        	String track_displayname = track.getChildTextNN("track_title");  
	        	
	        	// display_artistname / later set this to track artist if available
	        	String track_display_artistname = display_artistname;
	        	
	        	BundleInformation track_info = BundleInformation.make(srd, prd);		        	
	        	
	        	// num
	        	if(track.getChildTextNN("track_number").length()>0) {
	        		track_info.num(Integer.parseInt(track.getChildText("track_number")));
	        	}
	        	
	        	// setnum
	        	if(track.getChildTextNN("track_volume_number").length()>0) {
	        		track_info.setnum(Integer.parseInt(track.getChildText("track_volume_number")));
	        	} 
	        	
	        	// tracklength
        		if(track.getChildTextNN("duration").length()>0) {
        			track_info.playlength(Integer.parseInt(track.getChildText("duration")));     			
        		}
        		
	        	// suggested prelistining offset
        		if(track.getChild("track_preview_start_index")!=null && track.getChildTextNN("track_preview_start_index").length()>0) {
        			track_info.suggested_prelistening_offset(Integer.parseInt(track.getChildText("track_preview_start_index")));     			
        		}        		
        		
        		// track license basis
        		LicenseBasis track_license_basis = LicenseBasis.make();
        		
        		Territorial track_territorial = Territorial.make();
        			        	
            	track_license_basis.setTerritorial(track_territorial);
            	
	        	// license specifics -> empty!
	        	LicenseSpecifics track_license_specifics = LicenseSpecifics.make();         	      		
	        	
	        	// license_basis of Bundle / license_specifics of Bundle / others (?)
	        	Item item = Item.make(trackids, track_displayname, track_displayname, "", "audio", track_display_artistname, track_info, track_license_basis, track_license_specifics);
             	
	        	contributors = track.getChild("track_artists").getChildren("artist");
	        	for (Iterator<Element> itContributors = contributors.iterator(); itContributors.hasNext();) {
	        		Element contributor = itContributors.next();
	 
	        		boolean display_artist_isSet = false;
	        		if(contributor.getChild("primary")!=null) {
	        			if(contributor.getChildTextNN("primary").equals("true")&&!display_artist_isSet) { item.display_artistname(contributor.getChildTextNN("name")); display_artist_isSet=true; }
	        		}
	        		
	        		if(contributor.getChild("roles")!=null && contributor.getChild("roles").getChild("role")!=null) {
	        			String role = contributor.getChild("roles").getChildTextNN("role").trim().toLowerCase();
	        			if(role.equals("performer")) {
	        				con = Contributor.make(contributor.getChildTextNN("name"), Contributor.TYPE_PERFORMER, IDs.make());	
	        			}
	        			else if(role.equals("featuring")) {
	        				con = Contributor.make(contributor.getChildTextNN("name"), Contributor.TYPE_FEATURING, IDs.make());	
	        			}
	        			else if(role.equals("composer")) {
	        				con = Contributor.make(contributor.getChildTextNN("name"), Contributor.TYPE_COMPOSER, IDs.make());	
	        			}
	        			else if(role.equals("producer")) {
	        				con = Contributor.make(contributor.getChildTextNN("name"), Contributor.TYPE_PRODUCER, IDs.make());	
	        			} 
	        			
	        			// Maybe more roles? Insert!
	        			
	                	item.addContributor(con);
	        		}
	        		else {
	        			con = Contributor.make(contributor.getChildTextNN("name"), Contributor.TYPE_PERFORMER, IDs.make());
	        			item.addContributor(con);
	        		}
	        	}	        	
	        	
            	// add Tags
            	ItemTags track_tags = ItemTags.make();   	
            	
            	Vector<Element> track_genres = track.getChild("track_genres").getChildren("genre");
            	for (Iterator<Element> itTrackGenres = track_genres.iterator(); itTrackGenres.hasNext();) {
            		Element genre = itTrackGenres.next();        	
            		tags.addGenre(gc.convert(genre.getText()));
            	}          	
            	
            	item.tags(track_tags);	        	
	        	
        		ItemFile itemfile = ItemFile.make();
        		itemfile.type("full");
        		// check if file exist at path
        		String filename = track.getChild("track_audio_file").getChildTextNN("file_name");
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
        			if(track.getChild("track_audio_file").getChild("size")!=null) {
            			itemfile.bytes(Integer.parseInt(track.getChild("track_audio_file").getChildText("size")));
            		}        		
            		
            		// checksum md5
            		if(track.getChild("track_audio_file").getChild("checksum")!=null) {
            			Element cs = track.getChild("track_audio_file").getChild("checksum");
            			if (cs!=null) {
            				if(cs.getAttribute("type").equals("md5")) {
            					String sMd5 =  cs.getText();
	            				byte[] md5 = SecurityHelper.HexDecoder.decode(sMd5);
	            				itemfile.checksums(Checksums.make().md5(md5));
            				}
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
