package org.fnppl.opensdx.dmi.wayin;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.fnppl.opensdx.common.*;
import org.fnppl.opensdx.dmi.GenreConverter;
import org.fnppl.opensdx.xml.*;
import org.fnppl.opensdx.security.*;
import org.jdom2.Namespace;

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

public class PieToOpenSDXImporter extends OpenSDXImporterBase {
	DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
	private boolean forceImport = true; //in case we need to import a takedown xmls, this flag needs to be set on true.
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
	        
	        //(1.1) sets the activen namespace for this root element to avoid parameter overhead for all Element getters.
	        root.setActiveNamespace(root.getNamespace());
	          
	        // (2) get FeedInfo from import and create feedid and new FeedInfo for openSDX
	        String feedid = UUID.randomUUID().toString();
	        Calendar cal = Calendar.getInstance();        
	        
	        long creationdatetime = cal.getTimeInMillis();	        
	        long effectivedatetime = cal.getTimeInMillis();
	        
	        String lic = root.getChildTextNN("provider");
	        if (lic.length()==0) lic = "[NOT SET]";
	        
	        ContractPartner sender = ContractPartner.make(ContractPartner.ROLE_SENDER, lic , "1");
	        ContractPartner licensor = ContractPartner.make(ContractPartner.ROLE_LICENSOR, lic, "1");
	        ContractPartner licensee = ContractPartner.make(ContractPartner.ROLE_LICENSEE,"1","1");
	        
	        FeedInfo feedinfo = FeedInfo.make(onlytest, feedid, creationdatetime, effectivedatetime, sender, licensor, licensee);
	        
	        // (3) create new feed with feedinfo
	        feed = Feed.make(feedinfo); 	        
	        
	        // path to importfile
	        String path = this.importFile.getParent()+File.separator;
	        
	        Element album = root.getChild("album");
	        
	        // Information
        	String streetReleaseDate = album.getChildTextNN("original_release_date");	        
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
        	if(album.getChild("upc")!=null) bundleids.upc(album.getChildTextNN("upc"));
        	
        	// displayname
        	String displayname = album.getChildTextNN("title");
        	      	
        	// display_artistname
        	String display_artistname = null; //album.getChildTextNN("album_display_artist");
        	//Get first performer
        	Vector<Element> contributors = album.getChild("artists").getChildren("artist");
        	for (Iterator<Element> itContributors = contributors.iterator(); itContributors.hasNext();) {
        		Element contributor = itContributors.next();
        		if(contributor.getChild("roles")!=null && contributor.getChild("roles").getChild("role")!=null) {
        			String role = contributor.getChild("roles").getChildTextNN("role").trim().toLowerCase();
        			if(role.equals("performer") && "true".equals(contributor.getChildText("primary"))) {
        				display_artistname = contributor.getChildTextNN("artist_name");	
        				break;
        			}
        		}
        	}
        	
        	// license basis
        	Territorial territorial = Territorial.make();
        	
        	// Release
        	LicenseBasis license_basis = LicenseBasis.make(territorial, srd, prd);
        	
        	// license specifics -> empty!
        	LicenseSpecifics license_specifics = LicenseSpecifics.make();  
        	
        	// receiver -> "MUST" -> empty!
        	Receiver rec = Receiver.make(Receiver.TRANSFER_TYPE_OSDX_FILESERVER);
        	rec.servername("servername");
        	rec.serveripv4("0.0.0.0");
        	rec.authtype("login");
        	rec.username("testuser");
        	feedinfo.receiver(rec);
        	
        	// Album Territories, Streaming and Download
        	HashMap<String, Element> albumTerritories = new HashMap<String, Element>(); //Global album territories to match with Pies track territories
        	boolean downloadAllowed = false;
        	boolean streamingAllowed = false;
        	
        	//calculate major streaming & sales date
        	HashMap<String, String> majorDates = calculateDateMajorities(album.getChild("products").getChildren("product").iterator());
        	
        	Element albumProducts = album.getChild("products");
        	if(albumProducts != null){
        		Vector<Element> products = albumProducts.getChildren("product");
        		if(products != null && products.size() > 0){
        			for(Element p: products){
        				albumTerritories.put(p.getChildTextNN("territory"), p); //Puts the current territory for later track territory comparison
        				String territory = p.getChildText("territory");
        				if(forceImport){
        					//no rules necessary, just import. Download and streaming allowed = true, streaming start & enddate are the regular releasedate
        					downloadAllowed = true;
        					streamingAllowed = true;
        					territorial.allow(territory);
        				} else {
        					downloadAllowed |=  p.getChildBoolean("cleared_for_sale", false);
        					streamingAllowed |= p.getChildBoolean("cleared_for_stream", false);
        					if(p.getChildBoolean("cleared_for_sale", false) || p.getChildBoolean("cleared_for_stream", false)){
        						territorial.allow(territory);
            					//rules in case <sales_start_date> != <stream_start_date>
            					if(!p.getChildTextNN("sales_start_date").equals(p.getChildTextNN("stream_start_date"))){
            						//add rule
            						LicenseRule rule = LicenseRule.make(1, "streaming_allowed", "equals", "true");
            						rule.addThenProclaim("timeframe_from", p.getChildTextNN("sales_start_date"));
            						license_specifics.addRule(rule);
            					}
            					
            					if(!p.getChildTextNN("sales_start_date").equals(majorDates.get("sales"))){
            						//add rule for sales
            						LicenseRule rule = LicenseRule.make(2, "territory", "equals", territory);
            						rule.addThenProclaim("timeframe_from", majorDates.get("sales"));
            						license_specifics.addRule(rule);
            					}
            					
            					if(!p.getChildTextNN("stream_start_date").equals(majorDates.get("stream"))){
            						//add rule for streaming
            						LicenseRule rule = LicenseRule.make(3, "territory", "equals", territory);
            						rule.addThenProclaim("streaming_timeframe_from", majorDates.get("stream"));
            						license_specifics.addRule(rule);
            					}        						
        					} else {
        						territorial.disallow(territory);
        					}
        				}
        			}
        		}
        	}
        	
        	//allow download and streaming based on territories.
        	license_basis.download_allowed(downloadAllowed);
        	license_basis.streaming_allowed(streamingAllowed);
        	
        	Bundle bundle = Bundle.make(bundleids, displayname, displayname, "", display_artistname, info, license_basis, license_specifics);  
        	
        	// add contributor label
        	Contributor con = Contributor.make(album.getChildTextNN("label_name"), Contributor.TYPE_LABEL, IDs.make());
        	bundle.addContributor(con);
        	
        	// add contributor display_artist
        	con = Contributor.make(display_artistname, Contributor.TYPE_DISPLAY_ARTIST, IDs.make());
        	bundle.addContributor(con);
        	
        	contributors = album.getChild("artists").getChildren("artist");
        	for (Iterator<Element> itContributors = contributors.iterator(); itContributors.hasNext();) {
        		Element contributor = itContributors.next();
 
        		if(contributor.getChild("roles")!=null && contributor.getChild("roles").getChild("role")!=null) {
        			String role = contributor.getChild("roles").getChildTextNN("role").trim().toLowerCase();
        			if(role.equals("performer")) {
        				con = Contributor.make(contributor.getChildTextNN("artist_name"), Contributor.TYPE_PERFORMER, IDs.make());	
        			}
        			else if(role.equals("featuring")) {
        				con = Contributor.make(contributor.getChildTextNN("artist_name"), Contributor.TYPE_FEATURING, IDs.make());	
        			}
        			else if(role.equals("composer")) {
        				con = Contributor.make(contributor.getChildTextNN("artist_name"), Contributor.TYPE_COMPOSER, IDs.make());	
        			}
        			else if(role.equals("producer")) {
        				con = Contributor.make(contributor.getChildTextNN("artist_name"), Contributor.TYPE_PRODUCER, IDs.make());	
        			} 
        			
        			// Maybe more roles? Insert!
        			
                	bundle.addContributor(con);
        		}
        		else {
        			con = Contributor.make(contributor.getChildTextNN("artist_name"), Contributor.TYPE_PERFORMER, IDs.make());
                	bundle.addContributor(con);
        		}
        	}
        	
         	String copyright = album.getChildTextNN("copyright_cline");
         	String production = album.getChildTextNN("copyright_pline");
         	
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
        	if(album.getChild("artwork_files")!=null && album.getChild("artwork_files").getChild("file") != null) {
        		Element cover = album.getChild("artwork_files").getChild("file");
        		
        		ItemFile itemfile = ItemFile.make(); 
        		itemfile.type("frontcover");
        		// check if file exist at path
        		String filename = cover.getChildTextNN("file_name");
        		File f = new File(path+filename);
        		if(f!=null && f.exists()) {
        			itemfile.setFile(f);
        			
        			// set delivered path to file 
        			itemfile.setLocation(FileLocation.make(filename,filename));
        		} else {
        			//file does not exist -> so we have to set the values "manually"
        			
        			//-> use filename for location
        			itemfile.setLocation(FileLocation.make(filename,filename));
        		
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
        	
        	Vector<Element> genres = album.getChild("genres").getChildren("genre");
        	for (Iterator<Element> itGenres = genres.iterator(); itGenres.hasNext();) {
        		Element genre = itGenres.next();        	
        		tags.addGenre(gc.convert(genre.getAttribute("code")));
        	}
        	
    		bundle.tags(tags);        	        	
//        	
        	Vector<Element> tracks = album.getChild("tracks").getChildren("track");
        	for (Iterator<Element> itTracks = tracks.iterator(); itTracks.hasNext();) {
        		Element track = itTracks.next();

        		IDs trackids = IDs.make();
            	if(track.getChild("isrc")!=null) trackids.isrc(track.getChildTextNN("isrc"));
        		
	        	// displayname
	        	String track_displayname = track.getChildTextNN("title");  
	        	
	        	// display_artistname / later set this to track artist if available
	        	String track_display_artistname = display_artistname;
	        	
	        	BundleInformation track_info = BundleInformation.make(srd, prd);		        	
	        	
	        	// num
	        	if(track.getChildTextNN("number").length()>0) {
	        		track_info.num(Integer.parseInt(track.getChildText("number")));
	        	}
	        	
	        	// setnum
	        	if(track.getChildTextNN("volume_number").length()>0) {
	        		track_info.setnum(Integer.parseInt(track.getChildText("volume_number")));
	        	} 
	        	
	        	// tracklength
        		if(track.getChildTextNN("duration").length()>0) {
        			track_info.playlength(Integer.parseInt(track.getChildText("duration")));     			
        		}
        		
	        	// suggested prelistining offset
        		if(track.getChild("preview_start_index")!=null && track.getChildTextNN("preview_start_index").length()>0) {
        			track_info.suggested_prelistening_offset(Integer.parseInt(track.getChildText("preview_start_index")));     			
        		}        		
        		
        		// track license basis
        		LicenseBasis track_license_basis = LicenseBasis.make();
        		
        		Territorial track_territorial = Territorial.make();
        			        	
            	track_license_basis.setTerritorial(track_territorial);
            	
	        	// license specifics -> empty!
	        	LicenseSpecifics track_license_specifics = LicenseSpecifics.make();         	      		
	        	
	        	// license_basis of Bundle / license_specifics of Bundle / others (?)
	        	Item item = Item.make(trackids, track_displayname, track_display_artistname, "", "audio", track_display_artistname, track_info, track_license_basis, track_license_specifics);
	        	
	        	contributors = track.getChild("artists").getChildren("artist");
	        	for (Iterator<Element> itContributors = contributors.iterator(); itContributors.hasNext();) {
	        		Element contributor = itContributors.next();
	 
	        		boolean display_artist_isSet = false;
	        		if(contributor.getChild("primary")!=null) {
	        			if(contributor.getChildTextNN("primary").equals("true")&&!display_artist_isSet) { item.display_artistname(contributor.getChildTextNN("artist_name")); display_artist_isSet=true; }
	        		}
	        		
	        		if(contributor.getChild("roles")!=null && contributor.getChild("roles").getChild("role")!=null) {
	        			String role = contributor.getChild("roles").getChildTextNN("role").trim().toLowerCase();
	        			if(role.equals("performer")) {
	        				con = Contributor.make(contributor.getChildTextNN("artist_name"), Contributor.TYPE_PERFORMER, IDs.make());	
	        			}
	        			else if(role.equals("featuring")) {
	        				con = Contributor.make(contributor.getChildTextNN("artist_name"), Contributor.TYPE_FEATURING, IDs.make());	
	        			}
	        			else if(role.equals("composer")) {
	        				con = Contributor.make(contributor.getChildTextNN("artist_name"), Contributor.TYPE_COMPOSER, IDs.make());	
	        			}
	        			else if(role.equals("producer")) {
	        				con = Contributor.make(contributor.getChildTextNN("artist_name"), Contributor.TYPE_PRODUCER, IDs.make());	
	        			} 
	        			
	        			// Maybe more roles? Insert!
	        			
	                	item.addContributor(con);
	        		}
	        		else {
	        			con = Contributor.make(contributor.getChildTextNN("name"), Contributor.TYPE_PERFORMER, IDs.make());
	        			item.addContributor(con);
	        		}
	        	}	        
	        	
	        	// Check if track territory equal to those on bundle level
	        	if(forceImport){
	        		//In case of force import, just like on bundle (Just allow everything)
	        		track_license_basis.as_on_bundle(true);
	        	} else {
	        		boolean asOnBundle = asOnBundle(albumTerritories, track.getChild("products").getChildren("product"));
	        		if(asOnBundle){
	        			track_license_basis.as_on_bundle(asOnBundle);
	        		} else {
	        			//Check track territories again.
	        			Vector<Element> tmp = track.getChild("products").getChildren("product");
	        			for(Element t: tmp){
	        				String territory = t.getChildText("territory");
	        				if(t.getChildBoolean("cleared_for_sale", false) || t.getChildBoolean("cleared_for_stream", false)){
	        					track_territorial.allow(territory);
	        				} else {
	        					track_territorial.disallow(territory);
	        				}
	        			}
	        		}
	        	}
	        	
            	// add Tags
            	ItemTags track_tags = ItemTags.make();   	
            	
            	Vector<Element> track_genres = track.getChild("genres").getChildren("genre");
            	for (Iterator<Element> itTrackGenres = track_genres.iterator(); itTrackGenres.hasNext();) {
            		Element genre = itTrackGenres.next();        	
            		tags.addGenre(gc.convert(genre.getAttribute("code")));
            	}          	
            	
            	item.tags(track_tags);	        	
	        	
        		ItemFile itemfile = ItemFile.make();
        		itemfile.type("full");
        		// check if file exist at path
        		String filename = track.getChild("audio_file").getChildTextNN("file_name");
        		File f = new File(path+filename);      		
        		if(f!=null && f.exists()) {
        			itemfile.setFile(f); //this will also set the filesize and calculate the checksums
        			
        			// set delivered path to file 
        			itemfile.setLocation(FileLocation.make(filename,filename));        			
        		} else {
        			//file does not exist -> so we have to set the values "manually"
        			
        			//-> use filename as location
        			itemfile.setLocation(FileLocation.make(filename,filename));
        		
        			//file size
        			if(track.getChild("audio_file").getChild("size")!=null) {
            			itemfile.bytes(Integer.parseInt(track.getChild("audio_file").getChildText("size")));
            		}        		
            		
            		// checksum md5
            		if(track.getChild("audio_file").getChild("checksum")!=null) {
            			Element cs = track.getChild("audio_file").getChild("checksum");
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

	private HashMap<String, String> calculateDateMajorities(Iterator<Element> it){
		HashMap<String, String> ret = new HashMap<String, String>();
    	HashMap<String, Integer> salesDates = new HashMap<String, Integer>(); // kay = date | value = count
    	HashMap<String, Integer> streamingDates = new HashMap<String, Integer>(); // kay = date | value = count
    	String majorSalesDate = null;
    	int majorSalesCount = -1;
    	String majorStreamDate = null;
    	int majorStreamCount = -1;
    	
    	while(it.hasNext()){
    		Element e = it.next();
    		String territory = e.getChildTextNN("territory");
    		String stream_date = e.getChildTextNN("sales_start_date");
    		String streamDate = e.getChildTextNN("stream_start_date");
			//count major sales & streaming date
			if(stream_date.length() > 0){
				//sales
				if(salesDates.get(stream_date) == null){
					//add
					salesDates.put(stream_date, 1);
				} else {
					//increment
					salesDates.put(stream_date, salesDates.get(stream_date)+1);
				}
			}
			
			if(streamDate.length() > 0){
				//stream
				if(streamingDates.get(streamDate) == null){
					//add
					streamingDates.put(streamDate, 1);
				} else {
					//increment
					streamingDates.put(streamDate, streamingDates.get(streamDate)+1);
				}
			}
    	}	
    	
    	Iterator<String> itSale = salesDates.keySet().iterator();
    	while(itSale.hasNext()){
    		String key = itSale.next();
    		int i = salesDates.get(key);
    		if(i > majorSalesCount){
    			majorSalesCount = i;
    			majorSalesDate = key;
    		}
    	}
    	
    	Iterator<String> itStream= streamingDates.keySet().iterator();
    	while(itStream.hasNext()){
    		String key = itSale.next();
    		int i = streamingDates.get(key);
    		if(i > majorStreamCount){
    			majorStreamCount = i;
    			majorStreamDate = key;
    		}
    		
    	}

    	ret.put("stream", majorStreamDate);
    	ret.put("sale", majorSalesDate);
    	
    	return ret;
	}
	
	/**
	 * Checks if the trackTerritories are equal to those on bundle level.
	 * 
	 * @param albumTerritories HashMap with all Territories on bundle leve.
	 * @param trackTerritories Element with all Territories for the track.
	 * 
	 * @return true if albumTerritories equal to trackTerritories
	 */
	private boolean asOnBundle(HashMap<String, Element> albumTerritories, Vector<Element> trackTerritories){
		boolean ret = false;
		if(albumTerritories != null && trackTerritories != null 
				&& albumTerritories.size() == trackTerritories.size()){
			ret = true;
			Iterator<Element> it = trackTerritories.iterator();
			while(it.hasNext() && ret){
				Element trackT = it.next();
				Element albumT = albumTerritories.get(trackT.getChildTextNN("territory"));
				
				ret = (trackT.getChildTextNN("cleared_for_sale").equals(albumT.getChildTextNN("cleared_for_sale")) &&
						trackT.getChildTextNN("cleared_for_stream").equals(albumT.getChildTextNN("cleared_for_stream")));
			}
		}
		return ret;
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
	
	/**
	 * In case you need to import a "Pie" takedown XML, you can enforce it to act
	 * like a regualr import XML.
	 */
	public void forceTakedownImport(){
		this.forceImport = true;
	}
	
	/**
	 * For testing purpose, TODO: DELETE
	 * @param args
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception{
//		File file = new File("/home/ajovanovic/Arbeitsfläche/metadata-iTunes.xml");
		File file = new File("/home/ajovanovic/Arbeitsfläche/metadata.xml");
		File dstF = new File("/home/ajovanovic/Desktop/opensdx_out.xml");
		if(!dstF.exists()){
			dstF.createNewFile();
		}
		
		ImportType it = ImportType.getImportType("pie");
		PieToOpenSDXImporter p2o = new PieToOpenSDXImporter(it, file, dstF);
//		p2o.forceTakedownImport(); //enforce takedown to be imported
		Result result = p2o.formatToOpenSDXFile();
		System.out.println(result.toString());
	}
}
