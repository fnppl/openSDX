package org.fnppl.opensdx.dmi.wayin;

import java.io.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fnppl.opensdx.common.*;
import org.fnppl.opensdx.xml.*;
import org.fnppl.opensdx.security.*;


/*
 * Copyright (C) 2010-2012 
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

public class CLDToOpenSDXImporter extends OpenSDXImporterBase {
	static final Pattern durationpattern = Pattern.compile("(\\d{2}):(\\d{2}):(\\d{2})");

	static DecimalFormat dc2 =((DecimalFormat)NumberFormat.getNumberInstance(Locale.GERMANY));
	static DecimalFormat dc3 =(DecimalFormat)NumberFormat.getNumberInstance(Locale.GERMANY);
	static {
		dc2.applyPattern("00");
		dc3.applyPattern("000");
	}
	
	DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
	private Result ir = Result.succeeded();
	// test?
	boolean onlytest = true;

	public CLDToOpenSDXImporter(ImportType type, File impFile, File savFile) {
		super(type, impFile, savFile);
	}

	public CLDToOpenSDXImporter(File impFile) {
		super(ImportType.getImportType("xf"), impFile, null);
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

			ContractPartner sender = ContractPartner.make(ContractPartner.ROLE_SENDER, lic, lic);
			ContractPartner licensor = ContractPartner.make(ContractPartner.ROLE_LICENSOR, lic, lic);
			ContractPartner licensee = ContractPartner.make(ContractPartner.ROLE_LICENSEE, lic, lic);

			FeedInfo feedinfo = FeedInfo.make(onlytest, feedid, creationdatetime, effectivedatetime, sender, licensor, licensee);

			// receiver -> "MUST" -> empty!
			Receiver receiver = Receiver.make(Receiver.TRANSFER_TYPE_OSDX_FILESERVER);
			receiver.servername("localhost");
			receiver.serveripv4("127.0.0.1");
			receiver.authtype("login");
			feedinfo.receiver(receiver);
			
			// (3) create new feed with feedinfo
			feed = Feed.make(feedinfo); 	        

			// path to importfile
			String path = this.importFile.getParent()+File.separator;

			//Element album = root.getChild("ClaudioProduct");

			// Information
			//<availabilityDate>2009-03-09T00:00:00+01:00</availabilityDate>
			String digitalReleaseDate = "2013-02-22T00:00:00+01:00";	   
			Date d = new Date();
			if(digitalReleaseDate.length()>0) {
				d = getDateByISO8601String(digitalReleaseDate);
			}
			long drd = d.getTime();

			String physicalReleaseDate = root.getChildTextNN("releaseDate");	  
			d = new Date();
			if(physicalReleaseDate.length()>0) {
				d = getDateByISO8601String(physicalReleaseDate);
			}
			long prd = d.getTime();
			
//			related>
//			<physical_distributor>
			
			BundleRelatedInformation brel = BundleRelatedInformation.make();
			brel.physical_distributor("Cargo");
			
			BundleInformation binfo = BundleInformation.make(prd, drd);	   
			binfo.related(brel);
			
			BundleTexts bt = BundleTexts.make();
			bt.setPromotext("de", root.getChildTextNN("description"));
			binfo.texts(bt);
			
			// language
			//if ("ger".equalsIgnoreCase(root.getChildText("language"))) {
			binfo.main_language("de");
//			}
//			else {
//				binfo.main_language("en");
//			}

			// IDs of bundle -> more (?)
			IDs bundleids = IDs.make();
			if(root.getChild("relatedIsbn")!=null) bundleids.isbn(root.getChildTextNN("relatedIsbn"));
			String productId = root.getChildTextNN("productId");
			if(productId != null) bundleids.labelordernum(productId);

			//<retailPrice>1</retailPrice>
			String pricing_wholesale = "EUR"+root.getChildTextNN("retailPrice");
			
			// displayname
			String displayname = root.getChildTextNN("title");

			// license basis
			Territorial territorial = Territorial.make();
			//only GAS
			territorial.allow("DE");
			territorial.allow("AT");
			territorial.allow("CH");

			cal.setTime(ymd.parse("2100-01-01"));
			long cdate = cal.getTimeInMillis();

			// Release
			LicenseBasis license_basis = LicenseBasis.make(territorial, drd, cdate);
			license_basis.streaming_allowed(false); //DEFAULT
			license_basis.pricing_wholesale(pricing_wholesale);

			String display_artistname = "N N"; //DEFAULT
			
			Vector<Element> contributors = root.getChild("contributors").getChildren("contributor");
			for (Iterator<Element> itContributors = contributors.iterator(); itContributors.hasNext();) {
				Element contributor = itContributors.next();
				if(contributor.getChild("role")!=null && contributor.getChildTextNN("role").equalsIgnoreCase("AUTHOR")) {
					display_artistname = contributor.getChildTextNN("firstName")+" "+contributor.getChildTextNN("lastName");
				}
			}
						
			// license specifics -> empty!
			LicenseSpecifics license_specifics = LicenseSpecifics.make();  
			Bundle bundle = Bundle.make(bundleids, displayname, displayname, "", display_artistname, binfo, license_basis, license_specifics);  

			// add contributor label
//			 <publisher>
//			    <corporateName>audio media verlag</corporateName><!-- Verlagname -->
//			    <mvbIdentifier>5181355</mvbIdentifier><!-- Verlags Kennnummer (laut ISBN Agentur) -->
//			    <city>München</city><!-- Ort der Veröffentlichung (City of Publication) -->
//			    <countryCode>DE</countryCode><!-- Land der Veröffentlichung (Country of Publication) -->
//			    <subscriptionAllowed>false</subscriptionAllowed>
//			  </publisher>
			
			IDs ids = IDs.make();
			Element publisher = root.getChild("publisher");
			String labelname = publisher.getChildTextNN("corporateName");
			Contributor con = Contributor.make(publisher.getChildTextNN("corporateName"), Contributor.TYPE_LABEL, ids.licensor(publisher.getChildTextNN("mvbIdentifier")));
			bundle.addContributor(con);

			// add contributor display_artist
			con = Contributor.make(display_artistname, Contributor.TYPE_DISPLAY_ARTIST, IDs.make());
			bundle.addContributor(con);

			contributors = root.getChild("contributors").getChildren("contributor");
			Contributor texterContr = null;
			for (Iterator<Element> itContributors = contributors.iterator(); itContributors.hasNext();) {
				Element contributor = itContributors.next();
				String role = contributor.getChildTextNN("role").toLowerCase();
				if(role.equals("speaker")) {
					con = Contributor.make(contributor.getChildTextNN("firstName")+" "+contributor.getChildTextNN("lastName"), Contributor.TYPE_NARRATOR, IDs.make());	
				}
				else if(role.equals("author")) {
					con = Contributor.make(contributor.getChildTextNN("firstName")+" "+contributor.getChildTextNN("lastName"), Contributor.TYPE_AUTHOR, IDs.make());	
					texterContr = Contributor.make(contributor.getChildTextNN("firstName")+" "+contributor.getChildTextNN("lastName"), Contributor.TYPE_TEXTER, IDs.make());	
				}
				
				// Maybe more roles? Insert!
				bundle.addContributor(con);
			}
			
			//productionYear
			String productionYear = root.getChildTextNN("productionYear");
			//<copyright>© 2007 audio media verlag</copyright><!-- Copyright Angabe (aka C-Line) -->
//			String copyright = root.getChildTextNN("copyright").substring(2);
//			//<phonogram>℗ 2007 audio media verlag</phonogram><!-- Phonographische Angabe (aka P-Line) -->
//			String production = root.getChildTextNN("phonogram").substring(2);

			con = Contributor.make(labelname, Contributor.TYPE_COPYRIGHT, IDs.make());
			con.year(productionYear);
			bundle.addContributor(con);  
	
			con = Contributor.make(labelname, Contributor.TYPE_PRODUCTION, IDs.make());
			con.year(productionYear);
			bundle.addContributor(con);  
			
			// add Tags
			ItemTags tags = ItemTags.make();
			tags.addGenre("Word"); //DEFAULT-GENRE
			//recommendedAgeFrom
			int recommended_age_from = -1;
			if (root.getChild("recommendedAgeFrom")!=null && root.getChildInt("recommendedAgeFrom") >= 0) {
				recommended_age_from = root.getChildInt("recommendedAgeFrom");
				tags.recommended_age_from(recommended_age_from);
			}
			bundle.tags(tags);       
			
			//<duration>13560</duration><!-- Gesamtdauer in Sekunden -->			
			int bundleDuration = root.getChildInt("duration");
			binfo.playlength(bundleDuration);
			
			//NOW TRACKS ??
			
			File[] trackFiles = getTrackFiles("wav", importFile.getParentFile());
			Arrays.sort(trackFiles);
			List<File> tfs = Arrays.asList(trackFiles);

			System.out.println("Found: "+tfs.size()+" wav-files...");
			//itarate over totalDiscs
			//<totalDiscs>3</totalDiscs><!-- Gesamtzahl CDs -->
			int totalDiscs = root.getChildInt("totalDiscs");
			//<totalTracks>12</totalTracks><!-- Gesamtzahl Tracks -->
			int totalTracks = root.getChildInt("totalTracks");
			
			if (trackFiles.length < totalTracks) {
				throw new Exception("Not enough track files found");
			}
			
			//int trackNo = 1;
			
			//now get ISRC-List
			BufferedReader br = new BufferedReader(new FileReader("/home/spuchta/used-isrcs.txt"));
			int usedIsrcs = Integer.parseInt(br.readLine().trim());
			br.close();
			System.out.println("No of ISRCs red: "+usedIsrcs);
			
			BufferedReader br2 = new BufferedReader(new FileReader("/home/spuchta/cargo-isrcs.txt"));
			for (int x = 0; x < usedIsrcs; x++) {
				//Skip usedIsrcs
				br2.readLine();
			}
			
			int newUsedIsrcs = 0;
		
	souterLoop:	
			for (int i = 0; i < totalDiscs; i++) {
				int currentSetNo = i+1;
				int currentTrackNoPerSet = 1;
	
				for (int j = 0; j < trackFiles.length; j++) {
					boolean foundFile = false;
					//First check, if trackFilesNames are equal to: CXCL150677_01_001.wav
					String filename = trackFiles[j].getName();
					if (!filename.startsWith(productId) || filename.indexOf("_SAMPLE") != -1) {
						continue;
					}
					//no get trackNo
					String testFilename = "_"+dc2.format(currentSetNo)+"_"+dc3.format(currentTrackNoPerSet);
					System.out.println("Test for: "+testFilename+" in real file: "+filename);
					if (filename.indexOf(testFilename) > 0) {
						foundFile = true;
						//here we find a real track, so setNo and trackNo are given
						//make new trackItem + fileItem
						IDs trackids = IDs.make();
						String isrc = br2.readLine();
						trackids.isrc(isrc);
						newUsedIsrcs++;
						// displayname
						String track_displayname = "Kapitel "+(currentSetNo)+"-"+currentTrackNoPerSet;
						System.out.println("TRack with new ISRC: "+isrc+" get track_displayname: "+track_displayname);
	
						// display_artistname / later set this to track artist if available 
						String track_display_artistname = display_artistname;
	
						BundleInformation track_info = BundleInformation.make(prd, drd);	        	
						// num
						track_info.num(currentTrackNoPerSet);
						// setnum
						track_info.setnum(currentSetNo);
						
						track_info.origin_country("DE"); //REALLY ??
						track_info.main_language("de");
			
						// track license basis
						LicenseBasis track_license_basis = LicenseBasis.make();
	
						Territorial track_territorial = Territorial.make();
						track_license_basis.as_on_bundle(true); //Really??

						track_license_basis.setTerritorial(track_territorial);
	
						// license specifics -> empty!
						LicenseSpecifics track_license_specifics = LicenseSpecifics.make();         	      		
	
						// license_basis of Bundle / license_specifics of Bundle / others (?)
						Item item = Item.make(trackids, track_displayname, track_displayname, "", "audio", track_display_artistname, track_info, track_license_basis, track_license_specifics);
	
						item.type("audiobook");
						
						con = Contributor.make(labelname, Contributor.TYPE_COPYRIGHT, IDs.make());
						con.year(productionYear);
						item.addContributor(con);  
				
						con = Contributor.make(labelname, Contributor.TYPE_PRODUCTION, IDs.make());
						con.year(productionYear);
						item.addContributor(con); 
						
						item.addContributor(texterContr);
						
						// add Tags
						ItemTags track_tags = ItemTags.make();   	
						track_tags.addGenre("Word"); //DEFAULT-GENRE
						if (recommended_age_from >= 0) {
							track_tags.recommended_age_from(recommended_age_from);
						}
						track_tags.bundle_only(true); //Bundle Only
						item.tags(track_tags);	     
												
						ItemFile itemfile = ItemFile.make();
						
						itemfile.type("full");
						
						if(trackFiles[j] != null && trackFiles[j].exists()) {
							itemfile.setFile(trackFiles[j]); //this will also set the filesize and calculate the checksums
							// set delivered path to file 
							itemfile.setLocation(FileLocation.make(filename,filename));   
							item.addFile(itemfile);
						} 
						
						bundle.addItem(item);	
						currentTrackNoPerSet++;
					}
					
					
					
				} 
				
				
			} // End volumes
						
			//now re-write used ISRCs
			BufferedWriter bw = new BufferedWriter(new FileWriter("/home/spuchta/used-isrcs.txt"));
			bw.write(""+(usedIsrcs+newUsedIsrcs));
			bw.flush();
			bw.close();
			System.out.println("Write new used ISCRs: "+newUsedIsrcs+" (ALL: "+(usedIsrcs+newUsedIsrcs)+")");

//			Vector<Element> volumes = root.getChild("volumes").getChildren("volume");
//			for (Iterator<Element> itvolumes = volumes.iterator(); itvolumes.hasNext();) {
//				Element volume = itvolumes.next();
//				String setNum = volume.getAttribute("sequence");
//				if (setNum == null || setNum.length() == 0) {
//					continue;
//				}
//
//				//NOW TRACKS
//				Vector<Element> tracks = volume.getChild("tracks").getChildren("track");
//				for (Iterator<Element> itTracks = tracks.iterator(); itTracks.hasNext();) {
//					Element track = itTracks.next();
//
//					IDs trackids = IDs.make();
//					if(track.getChild("isrc")!=null) trackids.isrc(track.getChildTextNN("isrc"));
//
//					//<price_class_id>1</price_class_id>
//					price_level = fromXFClass(track.getChildTextNN("price_class_id"));
//
//					// displayname
//					String track_displayname = track.getChild("titles").getChildTextNN("title");  
//
//					// display_artistname / later set this to track artist if available 
//					String track_display_artistname = track.getChildTextNN("artist_text");
//
//					BundleInformation track_info = BundleInformation.make(srd, prd);		        	
//
//					// num
//					if(track.getChildTextNN("track_number").length()>0) {
//						track_info.num(Integer.parseInt(track.getChildText("track_number")));
//					}
//
//					// setnum
//					track_info.setnum(Integer.parseInt(setNum));
//					
//					track_info.origin_country("SE"); //REALLY ??
//					
//					// tracklength
//					if(track.getChildTextNN("duration").length()>0) {
//						track_info.playlength(getParsedDuration(track.getChildText("duration"),-1));  
//						bundleDuration += getParsedDuration(track.getChildText("duration"),-1);
//					}
//
//					// suggested prelistining offset
//					if(track.getChild("preview")!=null && track.getChild("preview").getChild("start") != null && track.getChild("preview").getChildTextNN("start").length() > 0) {
//						track_info.suggested_prelistening_offset(getParsedDuration(track.getChild("preview").getChildTextNN("start"),30));     			
//					}        		
//
//					// track license basis
//					LicenseBasis track_license_basis = LicenseBasis.make();
//
//					Territorial track_territorial = Territorial.make();
//					//Test for allowances and disallowances
//					if (track.getChild("country_restrictions") != null) {
//						Vector<Element> terrs = track.getChild("country_restrictions").getChildren();
//						for (Iterator<Element> itTerrs = terrs.iterator(); itTerrs.hasNext();) {
//							Element terr = itTerrs.next();
//							if (terr.getName().equals("allow")) {
//								track_territorial.allow(terr.getText());
//							}
//							else if (terr.getName().equals("disallow")) {
//								track_territorial.disallow(terr.getText());
//							}
//						}
//					}
//					else {
//						track_license_basis.as_on_bundle(true); //Really??
//					}
//
//
//
//					track_license_basis.setTerritorial(track_territorial);
//
//					// license specifics -> empty!
//					LicenseSpecifics track_license_specifics = LicenseSpecifics.make();         	      		
//
//					// license_basis of Bundle / license_specifics of Bundle / others (?)
//					Item item = Item.make(trackids, track_displayname, track_displayname, "", "audio", track_display_artistname, track_info, track_license_basis, track_license_specifics);
//
//					contributors = track.getChild("artists").getChildren("artist");
//					for (Iterator<Element> itContributors = contributors.iterator(); itContributors.hasNext();) {
//						Element contributor = itContributors.next();
//
//						if(contributor.getChild("role")!=null && contributor.getChild("role").getChild("role_type")!=null) {
//							String role = contributor.getChild("role").getChildTextNN("role_type").toLowerCase();
//							if(role.equals("primary performer")) {
//								con = Contributor.make(contributor.getChild("names").getChildTextNN("name"), Contributor.TYPE_PERFORMER, IDs.make());	
//							}
//							else if(role.equals("composer")) {
//								con = Contributor.make(contributor.getChild("names").getChildTextNN("name"), Contributor.TYPE_COMPOSER, IDs.make());	
//							}
//							else if(role.equals("conductor")) {
//								con = Contributor.make(contributor.getChild("names").getChildTextNN("name"), Contributor.TYPE_CONDUCTOR, IDs.make());	
//							}
//							else if(role.equals("producer")) {
//								con = Contributor.make(contributor.getChild("names").getChildTextNN("name"), Contributor.TYPE_PRODUCER, IDs.make());	
//							} 
//							else if(role.equals("featuring")) {
//								con = Contributor.make(contributor.getChild("names").getChildTextNN("name"), Contributor.TYPE_FEATURING, IDs.make());	
//							} 
//
//							item.addContributor(con);
//						}
//					}
//
//					copyright = track.getChildTextNN("copyright_statement");
//					production = copyright; //REALLY??
//
//					if(copyright.length()>0) {
//						con = Contributor.make(copyright.substring(5), Contributor.TYPE_COPYRIGHT, IDs.make());
//						con.year(copyright.substring(0, 4));
//						item.addContributor(con);  
//					}
//
//					if(production.length()>0) {
//						con = Contributor.make(production.substring(5), Contributor.TYPE_PRODUCTION, IDs.make());
//						con.year(production.substring(0, 4));
//						item.addContributor(con);  
//					} 
//
//					// add Tags
//					ItemTags track_tags = ItemTags.make();   	
//
//					Vector<Element> track_genres = track.getChild("genres").getChildren("genre");
//					for (Iterator<Element> itGenres = genres.iterator(); itGenres.hasNext();) {
//						Element genre = itGenres.next();        	
//						track_tags.addGenre(gc.convert(genre.getText(), "Miscellaneous"));
//					}
//
//					item.tags(track_tags);	        	
//
//					ItemFile itemfile = ItemFile.make();
//					itemfile.type("full");
//					// check if file exist at path
//					/*
//					 * 
//					 * <audio_file>
//                <use>fullength audio</use>
//                <filename>1.wav</filename>
//                <checksum type="md5">a194d4570e41df8d498f20b3e9658896</checksum>
//                <file_size>32748640</file_size>
//                <format>wav</format>
//                <bitrate>1411</bitrate>
//                <number_of_channels>2</number_of_channels>
//                <sample_rate>44100</sample_rate>
//                <bit_depth>16</bit_depth>
//              </audio_file>
//					 */
//
//					if (track.getChild("audio_files") != null && track.getChild("audio_files").getChild("audio_file") != null 
//							&& track.getChild("audio_files").getChild("audio_file").getChild("use") != null
//							&& "fullength audio".equals(track.getChild("audio_files").getChild("audio_file").getChildTextNN("use"))) {
//
//						String filename = track.getChild("audio_files").getChild("audio_file").getChildTextNN("filename");
//						File f = new File(path+filename);      		
//						if(f != null && f.exists()) {
//							itemfile.setFile(f); //this will also set the filesize and calculate the checksums
//							// set delivered path to file 
//							itemfile.setLocation(FileLocation.make(filename,filename));   
//							item.addFile(itemfile);
//						} 
//					}        		
//					
//					//bundleDuration
//					if (bundleDuration > 0) {
//						binfo.playlength(bundleDuration);
//					}
//					bundle.addItem(item);
//				}// End tracks
//			} // End volumes

			
			//now cover-file
			//test for cover
			//CXCL150785_COVER.jpg
			String testFilename = productId+"_COVER.jpg";
			File coverFile = new File(importFile.getParentFile(), testFilename);
			if (coverFile.exists()) {
				System.out.println("Found coverfile: "+testFilename);
				ItemFile itemfile = ItemFile.make();
				itemfile.type("frontcover");
				itemfile.setFile(coverFile); //this will also set the filesize and calculate the checksums
				// set delivered path to file 
				itemfile.setLocation(FileLocation.make(testFilename,testFilename));   
				bundle.addFile(itemfile);
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


	public static String fromXFClass(String xf_priceclass){
		if ("1".equalsIgnoreCase(xf_priceclass)) return "mid";
		if ("2".equalsIgnoreCase(xf_priceclass)) return "low";
		return "mid";
	}

	public static int getParsedDuration(String duration, int defaultValue) throws Exception {
		Matcher m;
		m = durationpattern.matcher( duration );
		if( m.matches()) {
			// int seconds = 0;
			if (m.groupCount() > 3) throw new Exception("Can't parse track-duration, has to be of format HH:mm:ss (to many groups)");
			if (m.groupCount() > 2) return (Integer.parseInt(m.group(1))*360) + (Integer.parseInt(m.group(2)) * 60) + Integer.parseInt(m.group(3));
			if (m.groupCount() > 1) return (Integer.parseInt(m.group(1))*60) + Integer.parseInt(m.group(2));
			return Integer.parseInt(m.group(1));
		}
		if (defaultValue > -1) {
			return defaultValue;
		}
		throw new Exception("Can't parse track-duration, has to be of format hh:mm:ss");
	}
	
	public static Date getDateByISO8601String( String input ) throws java.text.ParseException {
		//NOTE: SimpleDateFormat uses GMT[-+]hh:mm for the TZ which breaks
		//things a bit.  Before we go on we have to repair this.
		SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ssz" );
		//this is zero time so we need to add that TZ indicator for 
		if ( input.endsWith( "Z" ) ) {
			input = input.substring( 0, input.length() - 1) + "GMT-00:00";
		} else {
			int inset = 6;

			String s0 = input.substring( 0, input.length() - inset );
			String s1 = input.substring( input.length() - inset, input.length() );

			input = s0 + "GMT" + s1;
		}        
		return df.parse( input );

	}
	
	public static File[] getTrackFiles(String ending, File parentDir) {
		FilenameFilter mp3f = new FilenameFilter() {
			public boolean accept(File d, String name) {
				return name.endsWith(".mp3");
			}
		};		
		FilenameFilter wavf = new FilenameFilter() {
			public boolean accept(File d, String name) {
				return name.endsWith(".wav");
			}
		};		
		//        	File[] files = eanf.listFiles();
		if (ending.indexOf("mp3")!=-1) {
			return parentDir.listFiles(mp3f);        
		}
		else {
			return parentDir.listFiles(wavf);     
		}
	}

}
