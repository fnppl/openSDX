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

public class DDSToOpenSDXImporter extends OpenSDXImporterBase {
	DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
	private Result ir = Result.succeeded();
	// test?
	boolean onlytest = true;

	public DDSToOpenSDXImporter(ImportType type, File impFile, File savFile) {
		super(type, impFile, savFile);
	}

	public DDSToOpenSDXImporter(File impFile) {
		super(ImportType.getImportType("dds"), impFile, null);
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
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+00:00"));

			//	        if(root.getChild("updated_at")!=null && root.getChildTextNN("updated_at").length()>0) {
			//	        	cal.setTime(ymd.parse(root.getChildText("updated_at").substring(0, 9)));
			//	        }	        

			long creationdatetime = cal.getTimeInMillis();	        
			long effectivedatetime = cal.getTimeInMillis();

			String lic = root.getChildTextNN("AggregatorName");
			if (lic.length()==0) lic = "[NOT SET]";

			ContractPartner sender = ContractPartner.make(ContractPartner.ROLE_SENDER, lic , "");
			ContractPartner licensor = ContractPartner.make(ContractPartner.ROLE_LICENSOR, lic, "");
			ContractPartner licensee = ContractPartner.make(ContractPartner.ROLE_LICENSEE,"","");

			FeedInfo feedinfo = FeedInfo.make(onlytest, feedid, creationdatetime, effectivedatetime, sender, licensor, licensee);

			// path to importfile
			String path = this.importFile.getParent()+File.separator;

			// (3) create new feed with feedinfo
			feed = Feed.make(feedinfo);    

			Element albumEl = root.getChild("Album"); 

			// Information
			String streetReleaseDate = albumEl.getChildTextNN("OriginalReleaseDate");	        
			if(streetReleaseDate.length() > 0) {
				cal.setTime(ymd.parse(streetReleaseDate));
			}
			else {
				// MUST: when not provided then today
				cal.setTime(new Date());
			}

			long srd = cal.getTimeInMillis();

			// ReleaseDate = dig.Release (?)
			String releaseDate = albumEl.getChildTextNN("ReleaseDate");	        
			if(releaseDate.length() > 0) {
				cal.setTime(ymd.parse(releaseDate));
				//System.out.println("DATE 1: "+releaseDate+" - 2: "+cal.getTime());
			}
			else {
				// MUST: when not provided then today
				cal.setTime(new Date());
			}
			long prd = cal.getTimeInMillis();

			BundleInformation info = BundleInformation.make(srd, prd);
			//System.out.println("DATE 3: "+info.getPhysicalReleaseDatetimeText());

			// IDs of bundle -> more (?)
			IDs bundleids = IDs.make();
			if(albumEl.getChild("Barcode")!=null) bundleids.upc(albumEl.getChildTextNN("Barcode"));
			if(albumEl.getChild("AlbumProductGRID")!=null) bundleids.grid(albumEl.getChildTextNN("AlbumProductGRID"));
			if(albumEl.getChild("CatalogNumber")!=null) bundleids.labelordernum(albumEl.getChildTextNN("CatalogNumber"));

			// displayname
			String displayname = albumEl.getChildTextNN("ReleaseTitle");

			// display_artistname
			String display_artistname = albumEl.getChildTextNN("ReleaseArtist");

			// license basis
			Territorial territorial = Territorial.make();

			if(albumEl.getChild("CountriesAvailable") != null) {
				Vector<Element> release_rights = albumEl.getChild("CountriesAvailable").getChildren("Country");
				for (Iterator<Element> itRights = release_rights.iterator(); itRights.hasNext();) {
					Element release_right = itRights.next();
					String r = release_right.getText();
					if(r.length() > 0) {
						if(r.equals("**")) { 
							r = "WW";
							//							// if worldwide then add streamable information -> keep an eye on these (!)
							//							String streamable_from = track_right.getChildTextNN("streamable_from");	        
							//							if(streamable_from.length()>0) {
							//								cal.setTime(ymd.parse(streamable_from));
							//								track_license_basis.timeframe_from_datetime(cal.getTimeInMillis());
							//							}
							//
							//							// stremable_to is a "Must" but is not delivered
							//							String streamable_to = track_right.getChildTextNN("streamable_to");
							//							if(streamable_to.length()>0) {
							//								cal.setTime(ymd.parse(streamable_to));
							//								track_license_basis.timeframe_to_datetime(cal.getTimeInMillis());
							//							}
							//							else {
							//								// 20 year from now
							//								cal = Calendar.getInstance();
							//								cal.add(Calendar.YEAR, 20);
							//								track_license_basis.timeframe_to_datetime(cal.getTimeInMillis());
							//							}
							//
							//							if(track_right.getChild("allows_streaming")!=null) {
							//								track_license_basis.streaming_allowed(Boolean.parseBoolean(track_right.getChildText("allows_streaming")));
							//							}
						}
						territorial.allow(r);
					}
				} 
			}

			// Release
			LicenseBasis license_basis = LicenseBasis.make();
			license_basis.setTerritorial(territorial);

			// license specifics -> empty!
			LicenseSpecifics license_specifics = LicenseSpecifics.make();  

			// receiver -> "MUST" -> empty!
			feedinfo.receiver(Receiver.make(Receiver.TRANSFER_TYPE_OSDX_FILESERVER));

			Bundle bundle = Bundle.make(bundleids, displayname, displayname, "", display_artistname, info, license_basis, license_specifics);

			// init GenreConverter
			GenreConverter gc = GenreConverter.getInstance(GenreConverter.SIMFY_TO_OPENSDX);
			// add Tags
			ItemTags tags = ItemTags.make();   	
			Vector<Element> genres = albumEl.getChild("ReleaseGenres").getChildren("ReleaseGenre");
			for (Iterator<Element> itgenres = genres.iterator(); itgenres.hasNext();) {
				Element genreEl = itgenres.next();
				if (genreEl.getChildText("ReleaseGenre") != null) {
					String genre = gc.convert(genreEl.getChildText("ReleaseGenre"));
					if (genre.indexOf("[unknown genre]") > -1) {
						genre = "Miscellaneous"+" ["+genre.substring(genre.indexOf("[unknown genre]")+16)+"]";
					}
					tags.addGenre(genre);
				}
				if (genreEl.getChildText("ReleaseSubGenre") != null) {
					String genre = gc.convert(genreEl.getChildText("ReleaseSubGenre"));
					if (genre.indexOf("[unknown genre]") > -1) {
						genre = "Miscellaneous"+" ["+genre.substring(genre.indexOf("[unknown genre]")+16)+"]";
					}
					tags.addGenre(genre);
				}
			}        		
			bundle.tags(tags);        	

			Contributor contributor = Contributor.make(root.getChild("Label").getChildTextNN("LabelName"), Contributor.TYPE_LABEL, IDs.make());
			if (root.getChild("Label").getChildText("LabelURL") != null) {
				InfoWWW iww = InfoWWW.make("", "", root.getChild("Label").getChildText("LabelURL"), "", "");
				contributor.www(iww);
			}
			if (root.getChild("Label").getChild("LabelCode") != null 
					&& root.getChild("Label").getChild("LabelCode").getAttribute("type").equals("gvl") 
					&& root.getChild("Label").getChildText("LabelCode") != null) {
				IDs ids = IDs.make();
				ids.gvl(root.getChild("Label").getChildText("LabelCode"));
				contributor.ids(ids);
			}

			bundle.addContributor(contributor);

			contributor = Contributor.make(albumEl.getChildTextNN("ReleaseArtist"), Contributor.TYPE_DISPLAY_ARTIST, IDs.make());
			bundle.addContributor(contributor);

			if (albumEl.getChild("ReleaseCredits") != null) {
				String copyright = albumEl.getChild("ReleaseCredits").getChildTextNN("ReleaseCreditC");
				String production = albumEl.getChild("ReleaseCredits").getChildTextNN("ReleaseCreditP");

				if(copyright.length()>0) {
					contributor = Contributor.make(copyright, Contributor.TYPE_COPYRIGHT, IDs.make());
					contributor.year(albumEl.getChild("ReleaseCredits").getChildTextNN("ReleaseCreditCYear"));
					bundle.addContributor(contributor);  
				}

				if(production.length()>0) {
					contributor = Contributor.make(production, Contributor.TYPE_PRODUCTION, IDs.make());
					contributor.year(albumEl.getChild("ReleaseCredits").getChildTextNN("ReleaseCreditPYear"));
					bundle.addContributor(contributor);  
				}         	
			}

			// cover: license_basis & license_specifics from bundle, right?
			Element cover = albumEl.getChild("Artwork");
			if(cover != null) {
				ItemFile itemfile = ItemFile.make(); 
				itemfile.type("frontcover");
				// check if file exist at path
				String filename = cover.getChildTextNN("Filename");
				File f = new File(path+filename);
				if(f!=null && f.exists()) {
					itemfile.setFile(f);

					// set delivered path to file 
					itemfile.setLocation(FileLocation.make(filename, filename));
				} else {
					//file does not exist -> so we have to set the values "manually"

					//-> use filename for location
					itemfile.setLocation(FileLocation.make(filename, filename));

					//file size
					//							if(cover.getChild("file_size")!=null) {
					//								itemfile.bytes(Integer.parseInt(cover.getChildText("file_size")));
					//							}        		

					// checksum md5
					if("MD5".equals(cover.getChildText("artworkCRCTType")) && cover.getChildText("artworkCRCTValue") != null) {
						String sMd5 =  cover.getChildText("artworkCRCTValue");
						if (sMd5!=null) {
							byte[] md5 = SecurityHelper.HexDecoder.decode(sMd5);
							itemfile.checksums(Checksums.make().md5(md5));
						}
					}
				}

				// set dimension of cover
				String width = cover.getChildTextNN("artworkWidth");
				String height = cover.getChildTextNN("artworkHeight");
				if(width.length()>0 && height.length()>0) itemfile.dimension(Integer.parseInt(width), Integer.parseInt(height));

				bundle.addFile(itemfile);
			}

			if (albumEl.getChild("ReleaseDescription") != null) {
				BundleTexts texts = BundleTexts.make();

				Vector<Element> descrs = albumEl.getChild("ReleaseDescription").getChildren("Description");
				for (Iterator<Element> itdescrs = descrs.iterator(); itdescrs.hasNext();) {
					Element descr = itdescrs.next();
					if (descr.getChildTextNN("texttype").equals("short")) {
						texts.setTeasertext(descr.getChildTextNN("language"), descr.getChildTextNN("text"));
					}
					else if (descr.getChildTextNN("texttype").equals("full")) {
						texts.setPromotext(descr.getChildTextNN("language"), descr.getChildTextNN("text"));
					}
				}
			}


			//Disks
			Vector<Element> discs = albumEl.getChild("Disks").getChildren("Disk");
			for (Iterator<Element> itdiscs = discs.iterator(); itdiscs.hasNext();) {
				Element disc = itdiscs.next();
				String discno = disc.getChildTextNN("DiskNo");

				Vector<Element> tracks = disc.getChild("Tracks").getChildren("Track");
				for (Iterator<Element> itTracks = tracks.iterator(); itTracks.hasNext();) {
					Element track = itTracks.next();

					IDs trackids = IDs.make();
					if(track.getChild("ISRC")!=null) trackids.isrc(track.getChildTextNN("ISRC"));

					// displayname
					String track_displayname = track.getChildTextNN("TrackTitle");  

					// display_artistname
					String track_display_artistname = track.getChildTextNN("TrackArtistDisplay");

					String version = "";
					if ( track.getChildText("TrackMixVersion") != null) {
						version = track.getChildText("TrackMixVersion");
					}
					BundleInformation track_info = BundleInformation.make(srd, prd);		        	

					// num
					if(track.getChildTextNN("TrackNo").length() > 0) {
						track_info.num(Integer.parseInt(track.getChildText("TrackNo")));
					}

					// setnum
					if(discno.length()>0) {
						track_info.setnum(Integer.parseInt(discno));
					} 

					// track license basis
					LicenseBasis track_license_basis = LicenseBasis.make();
					track_license_basis.as_on_bundle(true);

					// license specifics -> empty!
					LicenseSpecifics track_license_specifics = LicenseSpecifics.make(); 
					track_license_specifics.as_on_bundle(true);

					// license_basis of Bundle / license_specifics of Bundle / others (?)
					Item item = Item.make(trackids, track_displayname, track_displayname, version, "audio", track_display_artistname, track_info, track_license_basis, track_license_specifics);

					// add contributor
					Contributor track_contributor = Contributor.make(track.getChildTextNN("TrackArtistDisplay"), Contributor.TYPE_DISPLAY_ARTIST, IDs.make());
					item.addContributor(track_contributor);  

					// add publisher
					if (track.getChild("Publishers") != null) {
						Contributor track_publisher = Contributor.make(track.getChild("Publishers").getChildTextNN("NamePublisher"), Contributor.TYPE_PUBLISHER, IDs.make());
						item.addContributor(track_publisher);  
					}
					/* <TrackArtists>
			              <TrackArtist>
			                <role>remixer</role>
			                <name>Dave Spritz</name>
			              </TrackArtist>
			            </TrackArtists>
					 */
					if (track.getChild("TrackArtists") != null) {
						Vector<Element> trackartists = track.getChild("TrackArtists").getChildren("TrackArtist");
						for (Iterator<Element> ittrackartists = trackartists.iterator(); ittrackartists.hasNext();) {
							Element trackartistEl = ittrackartists.next();
							if ("remixer".equals(trackartistEl.getChildText("role")) && trackartistEl.getChildText("name") != null) {
								Contributor track_remixer = Contributor.make(trackartistEl.getChildText("name"), Contributor.TYPE_REMIXER, IDs.make());
								item.addContributor(track_remixer);
							}
						}
					}
					// add Tags
					ItemTags track_tags = ItemTags.make();   	
					Vector<Element> trackgenres = track.getChild("TrackGenres").getChildren("Trackgenre");
					for (Iterator<Element> ittrackgenres = trackgenres.iterator(); ittrackgenres.hasNext();) {
						Element trackgenreEl = ittrackgenres.next();
						if (trackgenreEl.getChildText("TrackGenre") != null) {
							String genre = gc.convert(trackgenreEl.getChildText("TrackGenre"));
							if (genre.indexOf("[unknown genre]") > -1) {
								genre = "Miscellaneous";
							}
							track_tags.addGenre(genre);
						}
						if (trackgenreEl.getChildText("TrackSubGenre") != null) {
							String genre = gc.convert(trackgenreEl.getChildText("TrackSubGenre"));
							if (genre.indexOf("[unknown genre]") > -1) {
								genre = "Miscellaneous";
							}
							track_tags.addGenre(genre);
						}
					}        		
					
					// explicit_lyrics
					if(track.getChildTextNN("Explicit").length()>0) {
						if(track.getChildTextNN("Explicit").equalsIgnoreCase("false")) {
							track_tags.explicit_lyrics(ItemTags.EXPLICIT_LYRICS_FALSE);  
						}
						else if(track.getChildTextNN("Explicit").equalsIgnoreCase("true")) {
							track_tags.explicit_lyrics(ItemTags.EXPLICIT_LYRICS_TRUE);  
						}            		
					}
					// bundle only
					if(track.getChildTextNN("Bundled").length()>0) {
						if(track.getChildTextNN("Bundled").equalsIgnoreCase("false")) {
							track_tags.bundle_only(false);  
						}
						else if(track.getChildTextNN("Bundled").equalsIgnoreCase("true")) {
							track_tags.bundle_only(true);  
						}            		
					}

					item.tags(track_tags);	        	

					if (track.getChild("TrackAudioFile") != null) {
						// tracklength
						if(track.getChild("TrackAudioFile").getChildTextNN("TrackLength").length() > 0) {
							track_info.playlength(Integer.parseInt(track.getChild("TrackAudioFile").getChildText("TrackLength")));     			
						}

						ItemFile itemfile = ItemFile.make();
						itemfile.type("full");
						// check if file exist at path
						String filename = track.getChild("TrackAudioFile").getChildTextNN("AudioFileName");
						File f = new File(path+filename);      		
						if(f!=null && f.exists()) {
							itemfile.setFile(f); //this will also set the filesize and calculate the checksums

							// set delivered path to file 
							itemfile.setLocation(FileLocation.make(filename, filename));        			
						} else {
							//file does not exist -> so we have to set the values "manually"

							//-> use filename as location
							itemfile.setLocation(FileLocation.make(filename, filename));

							//							//file size
							//							if(track.getChild("file_size")!=null) {
							//								itemfile.bytes(Integer.parseInt(track.getChildText("file_size")));
							//							}        		

							// checksum md5 
							if("MD5".equals(track.getChild("TrackAudioFile").getChildText("TrackCRCType")) && track.getChild("TrackAudioFile").getChildText("TrackCRCValue") != null) {
								String sMd5 = track.getChild("TrackAudioFile").getChildText("TrackCRCValue");
								if (sMd5 != null) {
									byte[] md5 = SecurityHelper.HexDecoder.decode(sMd5);
									itemfile.checksums(Checksums.make().md5(md5));
								}
							}
							// checksum md5
							if(track.getChild("TrackAudioFile").getChildText("PreviewStart") != null) {
								itemfile.prelistening_offset(Integer.parseInt(track.getChild("TrackAudioFile").getChildText("PreviewStart")));
							}							
						}        		

						item.addFile(itemfile);

						bundle.addItem(item);
					}
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
