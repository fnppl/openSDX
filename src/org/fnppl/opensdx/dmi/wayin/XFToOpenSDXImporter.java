package org.fnppl.opensdx.dmi.wayin;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fnppl.opensdx.common.*;
import org.fnppl.opensdx.dmi.GenreConverter;
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

public class XFToOpenSDXImporter extends OpenSDXImporterBase {
	static final Pattern durationpattern = Pattern.compile("(\\d{2}):(\\d{2}):(\\d{2})");

	DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
	private Result ir = Result.succeeded();
	// test?
	boolean onlytest = true;

	public XFToOpenSDXImporter(ImportType type, File impFile, File savFile) {
		super(type, impFile, savFile);
	}

	public XFToOpenSDXImporter(File impFile) {
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

			Element album = root.getChild("album");

			// Information
			String streetReleaseDate = album.getChildTextNN("sale_start_date");	        
			if(streetReleaseDate.length()>0) {
				cal.setTime(ymd.parse(streetReleaseDate));
			}
			else {
				// MUST: when not provided then today
				cal.setTime(new Date());
			}
			long srd = cal.getTimeInMillis();

			String physicalReleaseDate = album.getChildTextNN("original_release_date");	        
			if(physicalReleaseDate.length()>0) {
				cal.setTime(ymd.parse(physicalReleaseDate));
			}
			else {
				// MUST: when not provided then today
				cal.setTime(new Date());
			}

			long prd = cal.getTimeInMillis();

			BundleInformation info = BundleInformation.make(srd, prd);	        

			// language
			info.main_language("en");

			// IDs of bundle -> more (?)
			IDs bundleids = IDs.make();
			if(album.getChild("upc")!=null) bundleids.upc(album.getChildTextNN("upc"));
			if(album.getChild("catalog_number")!=null) bundleids.labelordernum(album.getChildTextNN("catalog_number"));

			//<price_class_id>1</price_class_id>
			String	price_level = fromXFClass(album.getChildTextNN("price_class_id"));

			// displayname
			String displayname = album.getChild("titles").getChildTextNN("title");

			// display_artistname
			String display_artistname = album.getChildTextNN("artist_text");

			// license basis
			Territorial territorial = Territorial.make();

			//Test for allowances and disallowances
			if (album.getChild("country_restrictions") != null) {
				Vector<Element> terrs = album.getChild("country_restrictions").getChildren();
				for (Iterator<Element> itTerrs = terrs.iterator(); itTerrs.hasNext();) {
					Element terr = itTerrs.next();
					if (terr.getName().equals("allow")) {
						territorial.allow(terr.getText());
					}
					else if (terr.getName().equals("disallow")) {
						territorial.disallow(terr.getText());
					}
				}
			}
			else {
				territorial.allow("WW");
			}

			String cancellationDate = album.getChildTextNN("cancellation_date");	        
			if(cancellationDate.length()>0) {
				cal.setTime(ymd.parse(cancellationDate));
			}
			else {
				cal.setTime(ymd.parse("2099-12-31"));
			}

			long cdate = cal.getTimeInMillis();

			// Release
			LicenseBasis license_basis = LicenseBasis.make(territorial, srd, cdate);
			license_basis.streaming_allowed(true); //DEFAULT
			license_basis.pricing_pricecode(price_level);

			// license specifics -> empty!
			LicenseSpecifics license_specifics = LicenseSpecifics.make();  
			Bundle bundle = Bundle.make(bundleids, displayname, displayname, "", display_artistname, info, license_basis, license_specifics);  

			// add contributor label
			Contributor con = Contributor.make(album.getChildTextNN("label_name"), Contributor.TYPE_LABEL, IDs.make());
			bundle.addContributor(con);

			// add contributor display_artist
			con = Contributor.make(display_artistname, Contributor.TYPE_DISPLAY_ARTIST, IDs.make());
			bundle.addContributor(con);

			Vector<Element> contributors = album.getChild("artists").getChildren("artist");
			for (Iterator<Element> itContributors = contributors.iterator(); itContributors.hasNext();) {
				Element contributor = itContributors.next();

				if(contributor.getChild("role")!=null && contributor.getChild("role").getChild("role_type")!=null) {
					String role = contributor.getChild("role").getChildTextNN("role_type").toLowerCase();
					if(role.equals("primary performer")) {
						con = Contributor.make(contributor.getChild("names").getChildTextNN("name"), Contributor.TYPE_PERFORMER, IDs.make());	
					}
					else if(role.equals("composer")) {
						con = Contributor.make(contributor.getChild("names").getChildTextNN("name"), Contributor.TYPE_COMPOSER, IDs.make());	
					}
					else if(role.equals("conductor")) {
						con = Contributor.make(contributor.getChild("names").getChildTextNN("name"), Contributor.TYPE_CONDUCTOR, IDs.make());	
					}
					else if(role.equals("producer")) {
						con = Contributor.make(contributor.getChild("names").getChildTextNN("name"), Contributor.TYPE_PRODUCER, IDs.make());	
					} 
					else if(role.equals("featuring")) {
						con = Contributor.make(contributor.getChild("names").getChildTextNN("name"), Contributor.TYPE_FEATURING, IDs.make());	
					} 

					// Maybe more roles? Insert!

					bundle.addContributor(con);
				}
				//				else {
				//					con = Contributor.make(contributor.getChildTextNN("name"), Contributor.TYPE_PERFORMER, IDs.make());
				//					bundle.addContributor(con);
				//				}
			}

			String copyright = album.getChildTextNN("copyright_statement");
			String production = copyright; //REALLY??

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
			if(album.getChild("artwork_files")!=null && album.getChild("artwork_files").getChild("artwork_file") != null) {
				Element cover = album.getChild("artwork_files").getChild("artwork_file");

				//<use>cover art</use>				
				if ("cover art".equals(cover.getChildTextNN("use"))) {
					ItemFile itemfile = ItemFile.make(); 
					itemfile.type("frontcover");
					// check if file exist at path
					String filename = cover.getChildTextNN("filename");
					File f = new File(path+filename);
					if(f!=null && f.exists()) {
						itemfile.setFile(f);
						// set delivered path to file 
						itemfile.setLocation(FileLocation.make(filename,filename));
						bundle.addFile(itemfile);
					}
				}

				//			} else {
				//					//file does not exist -> so we have to set the values "manually"
				//
				//					//-> use filename for location
				//					itemfile.setLocation(FileLocation.make(filename,filename));
				//
				//					//file size
				//					if(cover.getChild("size")!=null) {
				//						itemfile.bytes(Integer.parseInt(cover.getChildText("size")));
				//					}        		
				//
				//					// checksum md5
				//					if(cover.getChild("checksum")!=null) {
				//						Element cs = cover.getChild("checksum");
				//						if (cs!=null) {
				//							if(cs.getAttribute("type").equals("md5")) {
				//								String sMd5 =  cover.getChildText("checksum");
				//								byte[] md5 = SecurityHelper.HexDecoder.decode(sMd5);
				//								itemfile.checksums(Checksums.make().md5(md5));
				//							}
				//						}
				//					}
				//				}


			}

			// init GenreConverter
			GenreConverter gc = GenreConverter.getInstance(GenreConverter.XF_TO_OPENSDX);

			// add Tags
			ItemTags tags = ItemTags.make();

			Vector<Element> genres = album.getChild("genres").getChildren("genre");
			for (Iterator<Element> itGenres = genres.iterator(); itGenres.hasNext();) {
				Element genre = itGenres.next();        	
				tags.addGenre(gc.convert(genre.getText(), "Miscellaneous"));
			}

			bundle.tags(tags);        	        

			//NOW TRACKS
			//itarate over volumes

			Vector<Element> volumes = album.getChild("volumes").getChildren("volume");
			for (Iterator<Element> itvolumes = volumes.iterator(); itvolumes.hasNext();) {
				Element volume = itvolumes.next();
				String setNum = volume.getAttribute("sequence");

				//NOW TRACKS

				Vector<Element> tracks = volume.getChild("tracks").getChildren("track");
				for (Iterator<Element> itTracks = tracks.iterator(); itTracks.hasNext();) {
					Element track = itTracks.next();

					IDs trackids = IDs.make();
					if(track.getChild("isrc")!=null) trackids.isrc(track.getChildTextNN("isrc"));

					//<price_class_id>1</price_class_id>
					price_level = fromXFClass(track.getChildTextNN("price_class_id"));

					// displayname
					String track_displayname = track.getChild("titles").getChildTextNN("title");  

					// display_artistname / later set this to track artist if available 
					String track_display_artistname = track.getChildTextNN("artist_text");

					BundleInformation track_info = BundleInformation.make(srd, prd);		        	

					// num
					if(track.getChildTextNN("track_number").length()>0) {
						track_info.num(Integer.parseInt(track.getChildText("track_number")));
					}

					// setnum
					if(track.getChildTextNN("track_volume_number").length()>0) {
						track_info.setnum(Integer.parseInt(setNum));
					} 

					// tracklength
					if(track.getChildTextNN("duration").length()>0) {
						track_info.playlength(getParsedDuration(track.getChildText("duration")));     			
					}

					// suggested prelistining offset
					if(track.getChild("preview")!=null && track.getChild("preview").getChild("start") != null && track.getChild("preview").getChildTextNN("start").length() > 0) {
						track_info.suggested_prelistening_offset(getParsedDuration(track.getChild("preview").getChildTextNN("start")));     			
					}        		

					// track license basis
					LicenseBasis track_license_basis = LicenseBasis.make();

					Territorial track_territorial = Territorial.make();
					//Test for allowances and disallowances
					if (track.getChild("country_restrictions") != null) {
						Vector<Element> terrs = track.getChild("country_restrictions").getChildren();
						for (Iterator<Element> itTerrs = terrs.iterator(); itTerrs.hasNext();) {
							Element terr = itTerrs.next();
							if (terr.getName().equals("allow")) {
								track_territorial.allow(terr.getText());
							}
							else if (terr.getName().equals("disallow")) {
								track_territorial.disallow(terr.getText());
							}
						}
					}
					else {
						track_license_basis.as_on_bundle(true); //Really??
					}



					track_license_basis.setTerritorial(track_territorial);

					// license specifics -> empty!
					LicenseSpecifics track_license_specifics = LicenseSpecifics.make();         	      		

					// license_basis of Bundle / license_specifics of Bundle / others (?)
					Item item = Item.make(trackids, track_displayname, track_displayname, "", "audio", track_display_artistname, track_info, track_license_basis, track_license_specifics);

					contributors = track.getChild("artists").getChildren("artist");
					for (Iterator<Element> itContributors = contributors.iterator(); itContributors.hasNext();) {
						Element contributor = itContributors.next();

						if(contributor.getChild("role")!=null && contributor.getChild("role").getChild("role_type")!=null) {
							String role = contributor.getChild("role").getChildTextNN("role_type").toLowerCase();
							if(role.equals("primary performer")) {
								con = Contributor.make(contributor.getChild("names").getChildTextNN("name"), Contributor.TYPE_PERFORMER, IDs.make());	
							}
							else if(role.equals("composer")) {
								con = Contributor.make(contributor.getChild("names").getChildTextNN("name"), Contributor.TYPE_COMPOSER, IDs.make());	
							}
							else if(role.equals("conductor")) {
								con = Contributor.make(contributor.getChild("names").getChildTextNN("name"), Contributor.TYPE_CONDUCTOR, IDs.make());	
							}
							else if(role.equals("producer")) {
								con = Contributor.make(contributor.getChild("names").getChildTextNN("name"), Contributor.TYPE_PRODUCER, IDs.make());	
							} 
							else if(role.equals("featuring")) {
								con = Contributor.make(contributor.getChild("names").getChildTextNN("name"), Contributor.TYPE_FEATURING, IDs.make());	
							} 

							item.addContributor(con);
						}
					}

					copyright = track.getChildTextNN("copyright_statement");
					production = copyright; //REALLY??

					if(copyright.length()>0) {
						con = Contributor.make(copyright.substring(5), Contributor.TYPE_COPYRIGHT, IDs.make());
						con.year(copyright.substring(0, 4));
						item.addContributor(con);  
					}

					if(production.length()>0) {
						con = Contributor.make(production.substring(5), Contributor.TYPE_PRODUCTION, IDs.make());
						con.year(production.substring(0, 4));
						item.addContributor(con);  
					} 

					// add Tags
					ItemTags track_tags = ItemTags.make();   	

					Vector<Element> track_genres = track.getChild("genres").getChildren("genre");
					for (Iterator<Element> itGenres = genres.iterator(); itGenres.hasNext();) {
						Element genre = itGenres.next();        	
						track_tags.addGenre(gc.convert(genre.getText(), "Miscellaneous"));
					}

					item.tags(track_tags);	        	

					ItemFile itemfile = ItemFile.make();
					itemfile.type("full");
					// check if file exist at path
					/*
					 * 
					 * <audio_file>
                <use>fullength audio</use>
                <filename>1.wav</filename>
                <checksum type="md5">a194d4570e41df8d498f20b3e9658896</checksum>
                <file_size>32748640</file_size>
                <format>wav</format>
                <bitrate>1411</bitrate>
                <number_of_channels>2</number_of_channels>
                <sample_rate>44100</sample_rate>
                <bit_depth>16</bit_depth>
              </audio_file>
					 */

					if (track.getChild("audio_files") != null && track.getChild("audio_files").getChild("audio_file") != null 
							&& track.getChild("audio_files").getChild("audio_file").getChild("use") != null
							&& "fullength audio".equals(track.getChild("audio_files").getChild("audio_file").getChildTextNN("use"))) {

						String filename = track.getChild("audio_files").getChild("audio_file").getChildTextNN("filename");
						File f = new File(path+filename);      		
						if(f != null && f.exists()) {
							itemfile.setFile(f); //this will also set the filesize and calculate the checksums
							// set delivered path to file 
							itemfile.setLocation(FileLocation.make(filename,filename));   
							item.addFile(itemfile);
						} 
					}        		
					bundle.addItem(item);
				}// End tracks
			} // End volumes

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

	public static int getParsedDuration(String duration) throws Exception {
		Matcher m;
		m = durationpattern.matcher( duration );
		if( m.matches()) {
			// int seconds = 0;
			if (m.groupCount() > 3) throw new Exception("Can't parse track-duration, has to be of format HH:mm:ss (to many groups)");
			if (m.groupCount() > 2) return (Integer.parseInt(m.group(1))*360) + (Integer.parseInt(m.group(2)) * 60) + Integer.parseInt(m.group(3));
			if (m.groupCount() > 1) return (Integer.parseInt(m.group(1))*60) + Integer.parseInt(m.group(2));
			return Integer.parseInt(m.group(1));
		}
		throw new Exception("Can't parse track-duration, has to be of format hh:mm:ss");
	}

}
