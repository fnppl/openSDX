package org.fnppl.opensdx.dmi;


/*
 * Copyright (C) 2010-2012 
 * 							fine people e.V. <opensdx@fnppl.org> 
 * 							Stefan Puchta <sp@fnppl.org>
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



import org.fnppl.opensdx.common.*;
import org.fnppl.opensdx.xml.Document;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

public class FeedComparer {
	private Feed originalFeed;
	private Feed newFeed;

	public LinkedHashMap<String, String> addedBundleVals; //Parameter, String{newVal}
	public LinkedHashMap<String, String> deletedBundleVals; //Parameter, String{oldVal}
	public LinkedHashMap<String, String[]> changedBundleVals; //Parameter, String{oldVal,newVal}

	public LinkedHashMap<String, LinkedHashMap<String, String>> addedTrackVals; //setnum-tracknum, < Parameter, String{newVal}>
	public LinkedHashMap<String, LinkedHashMap<String, String>> deletedTrackVals; //setnum-tracknum, < Parameter, String{newVal}>
	public LinkedHashMap<String, LinkedHashMap<String, String[]>> changedTrackVals; //setnum-tracknum, < Parameter, String{oldVal,newVal}>

	public LinkedHashMap<String, String> allTrackSetNumsIsrcs;

	public static final int TYPE_NO_CHANGE = 0;
	public static final int TYPE_ADDED = 1;
	public static final int TYPE_DELETED = 2;
	public static final int TYPE_CHANGED = 3;
	
	public static final int TYPE_NO_CHANNELS = 100;

	public FeedComparer(Feed originalFeed, Feed newFeed) {
		this.originalFeed = originalFeed;
		this.newFeed = newFeed;
	}

	public void compareFeedValues(int compareType) {
		if (originalFeed == null || newFeed == null) {
			return;
		}

		addedBundleVals = new LinkedHashMap<String, String>();
		deletedBundleVals = new LinkedHashMap<String, String>();
		changedBundleVals = new LinkedHashMap<String, String[]>();

		addedTrackVals = new LinkedHashMap<String, LinkedHashMap<String,String>>();
		deletedTrackVals = new LinkedHashMap<String, LinkedHashMap<String,String>>();
		changedTrackVals = new LinkedHashMap<String, LinkedHashMap<String,String[]>>();

		allTrackSetNumsIsrcs = new LinkedHashMap<String, String>();

		//First all bundle values
		parseAndSaveCompareValues("grid",originalFeed.getBundle(0).getIds().getGrid(),newFeed.getBundle(0).getIds().getGrid(), null);
		//upc
		parseAndSaveCompareValues("upc",originalFeed.getBundle(0).getIds().getUpc(),newFeed.getBundle(0).getIds().getUpc(), null);
		//contentauth
		//parseAndSaveCompareValues("contentauth",originalFeed.getBundle(0).getIds().getContentauth(),newFeed.getBundle(0).getIds().getContentauth(), null);
		//labelordernum
		parseAndSaveCompareValues("labelordernum",originalFeed.getBundle(0).getIds().getLabelordernum(),newFeed.getBundle(0).getIds().getLabelordernum(), null);
		//displayname
		parseAndSaveCompareValues("displayname",originalFeed.getBundle(0).getDisplayname(),newFeed.getBundle(0).getDisplayname(), null);
		//name
		parseAndSaveCompareValues("name",originalFeed.getBundle(0).getName(),newFeed.getBundle(0).getName(), null);
		//version
		parseAndSaveCompareValues("version",originalFeed.getBundle(0).getVersion(),newFeed.getBundle(0).getVersion(), null);
		//display_artistname
		parseAndSaveCompareValues("display_artistname",originalFeed.getBundle(0).getDisplay_artistname(),newFeed.getBundle(0).getDisplay_artistname(), null);
		//parse through contributors
		parseAndSaveCompareValues(originalFeed.getBundle(0).getAllBundleContributors(), newFeed.getBundle(0).getAllBundleContributors(), null);
		//parse through Texts
		try {
			parseAndSaveCompareValues(originalFeed.getBundle(0).getInformation().getTexts(), newFeed.getBundle(0).getInformation().getTexts());
			//physical_release_datetime
			parseAndSaveCompareValues("physical_release_datetime",originalFeed.getBundle(0).getInformation().getPhysicalReleaseDatetimeText(),newFeed.getBundle(0).getInformation().getPhysicalReleaseDatetimeText(), null);
			//digital_release_datetime
			parseAndSaveCompareValues("digital_release_datetime",originalFeed.getBundle(0).getInformation().getDigitalReleaseDatetimeText(), newFeed.getBundle(0).getInformation().getDigitalReleaseDatetimeText(), null);
			//playlength
			parseAndSaveCompareValues("playlength (sec)",""+originalFeed.getBundle(0).getInformation().getPlaylength(), ""+newFeed.getBundle(0).getInformation().getPlaylength(), null);
			//origin_country
			parseAndSaveCompareValues("origin_country",originalFeed.getBundle(0).getInformation().getOrigin_country(), newFeed.getBundle(0).getInformation().getOrigin_country(), null);
			//main_language
			parseAndSaveCompareValues("main_language",originalFeed.getBundle(0).getInformation().getMain_language(), newFeed.getBundle(0).getInformation().getMain_language(), null);
			//physical_distributor
			if (originalFeed.getBundle(0).getInformation().getRelated() != null && newFeed.getBundle(0).getInformation().getRelated() != null) {
				parseAndSaveCompareValues("physical_distributor",originalFeed.getBundle(0).getInformation().getRelated().getPhysicalDistributor(), newFeed.getBundle(0).getInformation().getMain_language(), null);
			}
		}
		catch (Exception ex) {
		}

		//parse through Territories
		parseAndSaveCompareValues(originalFeed.getBundle(0).getLicense_basis().getTerritorial(), newFeed.getBundle(0).getLicense_basis().getTerritorial(), null);

		//timeframe
		parseAndSaveCompareValues("timeframe_from",originalFeed.getBundle(0).getLicense_basis().getTimeframeFromText(), newFeed.getBundle(0).getLicense_basis().getTimeframeFromText(), null);
		//timeframe to
		parseAndSaveCompareValues("timeframe_to",originalFeed.getBundle(0).getLicense_basis().getTimeframeToText(), newFeed.getBundle(0).getLicense_basis().getTimeframeToText(), null);

		//pricing PriceCode
		parseAndSaveCompareValues("pricecode",originalFeed.getBundle(0).getLicense_basis().getPricingPricecode(), newFeed.getBundle(0).getLicense_basis().getPricingPricecode(), null);

		//pricing wholesale
		parseAndSaveCompareValues("wholesale",originalFeed.getBundle(0).getLicense_basis().getPricingWholesale(), newFeed.getBundle(0).getLicense_basis().getPricingWholesale(), null);

		//streaming_allowed
		parseAndSaveCompareValues("streaming_allowed",originalFeed.getBundle(0).getLicense_basis().isStreaming_allowed(), newFeed.getBundle(0).getLicense_basis().isStreaming_allowed(), null); 

		//channels ==> LicBasis
		if (compareType != TYPE_NO_CHANNELS) {
			parseAndSaveCompareValues(originalFeed.getBundle(0).getLicense_basis(), newFeed.getBundle(0).getLicense_basis(), null);
		}

		//TODO: LicSpecific

		//TAGS
		//genre
		parseAndSaveCompareValues(originalFeed.getBundle(0).getTags(), newFeed.getBundle(0).getTags(), null);
		//explicit_lyrics
		parseAndSaveCompareValues("explicit_lyrics",originalFeed.getBundle(0).getTags().isExplicit_lyrics(), newFeed.getBundle(0).getTags().isExplicit_lyrics(), null);
		//live
		parseAndSaveCompareValues("live",originalFeed.getBundle(0).getTags().isLive(), newFeed.getBundle(0).getTags().isLive(), null);
		//accoustic
		parseAndSaveCompareValues("accoustic",originalFeed.getBundle(0).getTags().isAccoustic(), newFeed.getBundle(0).getTags().isAccoustic(), null);
		//instrumental
		parseAndSaveCompareValues("instrumental",originalFeed.getBundle(0).getTags().isInstrumental(), newFeed.getBundle(0).getTags().isInstrumental(), null);

		//NOW ITEMS/TRACKS, go through all NEW tracks
		for (int i = 0; i < newFeed.getBundle(0).getItemsCount(); i++) {
			String settracknum = newFeed.getBundle(0).getItem(i).getInformation().getSetNum()+"-"+newFeed.getBundle(0).getItem(i).getInformation().getNum();
			String isrc = newFeed.getBundle(0).getItem(i).getIds().getIsrc();
			Item originalItem = null, newItem = null;
			if (originalFeed.getBundle(0).getItemsCount() > i) {
				originalItem = originalFeed.getBundle(0).getItem(i);
			}
			newItem = newFeed.getBundle(0).getItem(i);

			allTrackSetNumsIsrcs.put(settracknum, isrc);

			//displayname
			parseAndSaveCompareValues("displayname",originalItem!=null?originalItem.getDisplayname():null,newItem.getDisplayname(), settracknum);
			//name
			parseAndSaveCompareValues("name",originalItem!=null?originalItem.getName():null,newItem.getName(), settracknum);
			//version
			parseAndSaveCompareValues("version",originalItem!=null?originalItem.getVersion():null,newItem.getVersion(), settracknum);
			//display_artistname
			parseAndSaveCompareValues("display_artistname",originalItem!=null?originalItem.getDisplay_artistname():null,newItem.getDisplay_artistname(), settracknum);
			//isrc
			parseAndSaveCompareValues("isrc",originalItem!=null?originalItem.getIds().getIsrc():null,newItem.getIds().getIsrc(), settracknum);
			//parse through contibutors
			parseAndSaveCompareValues(originalItem!=null?originalItem.getAllContributors():null, newItem.getAllContributors(), settracknum);
			try {
				//physical_release_datetime
				parseAndSaveCompareValues("physical_release_datetime",originalItem!=null?originalItem.getInformation().getPhysicalReleaseDatetimeText():null,newItem.getInformation().getPhysicalReleaseDatetimeText(), settracknum);
				//digital_release_datetime
				parseAndSaveCompareValues("digital_release_datetime",originalItem!=null?originalItem.getInformation().getDigitalReleaseDatetimeText():null,newItem.getInformation().getDigitalReleaseDatetimeText(), settracknum);
				//playlength
				parseAndSaveCompareValues("playlength (sec)",originalItem!=null?""+originalItem.getInformation().getPlaylength():null, ""+newItem.getInformation().getPlaylength(), settracknum);

				//origin_country
				parseAndSaveCompareValues("origin_country",originalItem!=null?originalItem.getInformation().getOrigin_country():null, newItem.getInformation().getOrigin_country(), settracknum);
				//main_language
				parseAndSaveCompareValues("main_language",originalItem!=null?originalItem.getInformation().getMain_language():null, newItem.getInformation().getMain_language(), settracknum);
				//suggested_prelistening_offset
				parseAndSaveCompareValues("suggested_prelistening_offset (sec)",originalItem!=null?""+originalItem.getInformation().getSuggestedPrelistiningOffset():null, ""+newItem.getInformation().getSuggestedPrelistiningOffset(), settracknum);
			}
			catch (Exception ex) {
			}
			LicenseBasis originalLicB = originalItem!=null?originalItem.getLicense_basis():null;
			LicenseBasis newLicB = newItem.getLicense_basis();
			//Nur wenn beide nicht gleichzeitig asOnBundle sind....
			if (!(originalLicB != null && originalLicB.isAsOnBundle() && newLicB != null && newLicB.isAsOnBundle())) {
				//wenn nur newTrack da ist und dort AsOnBundle is
				if (newLicB.isAsOnBundle()) {
					addToTrackAddData(settracknum, "license_basis", "AsOnBundle");
				}
				else if (originalItem!=null && originalLicB.isAsOnBundle()) {
					addToTrackDeleteData(settracknum, "license_basis", "AsOnBundle");
				}
				//else {
				//				
				//				if (originalLicB != null && originalLicB.isAsOnBundle()) {
				//					originalLicB = originalFeed.getBundle(0).getLicense_basis();
				//				}
				//				if (newLicB != null && newLicB.isAsOnBundle()) {
				//					newLicB = newFeed.getBundle(0).getLicense_basis();
				//				}
				//parse through Territories
				parseAndSaveCompareValues(originalLicB!=null?originalLicB.getTerritorial():null, newLicB.getTerritorial(), settracknum);
				//timeframe
				parseAndSaveCompareValues("timeframe_from",originalLicB!=null?originalLicB.getTimeframeFromText():null, newLicB.getTimeframeFromText(), settracknum);
				//timeframe to
				parseAndSaveCompareValues("timeframe_to",originalLicB!=null?originalLicB.getTimeframeToText():null, newLicB.getTimeframeToText(), settracknum);
				//pricing PriceCode
				parseAndSaveCompareValues("pricecode",originalLicB!=null?originalLicB.getPricingPricecode():null, newLicB.getPricingPricecode(), settracknum);
				//pricing wholesale
				parseAndSaveCompareValues("wholesale",originalLicB!=null?originalLicB.getPricingWholesale():null, newLicB.getPricingWholesale(), settracknum);
				//}
			}
			//genre
			parseAndSaveCompareValues(originalItem!=null?originalItem.getTags():null, newItem.getTags(), settracknum);
			//explicit_lyrics
			parseAndSaveCompareValues("explicit_lyrics",originalItem!=null?originalItem.getTags().isExplicit_lyrics():false, newItem.getTags().isExplicit_lyrics(), settracknum);
			//live
			parseAndSaveCompareValues("live",originalItem!=null?originalItem.getTags().isLive():false, newItem.getTags().isLive(), settracknum);
			//accoustic
			parseAndSaveCompareValues("accoustic",originalItem!=null?originalItem.getTags().isAccoustic():false, newItem.getTags().isAccoustic(), settracknum);
			//instrumental
			parseAndSaveCompareValues("instrumental",originalItem!=null?originalItem.getTags().isInstrumental():false, newItem.getTags().isInstrumental(), settracknum);
		}


		//Delete tracks that are not in the newFeed
		if ( newFeed.getBundle(0).getItemsCount() < originalFeed.getBundle(0).getItemsCount()) {
			int x = originalFeed.getBundle(0).getItemsCount() - newFeed.getBundle(0).getItemsCount();
			for (int i = originalFeed.getBundle(0).getItemsCount()-x; i < originalFeed.getBundle(0).getItemsCount(); i++) {
				String settracknum = originalFeed.getBundle(0).getItem(i).getInformation().getSetNum()+"-"+originalFeed.getBundle(0).getItem(i).getInformation().getNum();
				addToTrackDeleteData(settracknum, "fulltrack", "");
				String isrc = originalFeed.getBundle(0).getItem(i).getIds().getIsrc();
				allTrackSetNumsIsrcs.put(settracknum, isrc);
			}
		}
	}


	private void parseAndSaveCompareValues(String para, String s1, String s2, String settracknum) {
		int type = compareValues(s1, s2);
		switch (type) {		
		case TYPE_ADDED:
			if (settracknum == null) {
				addedBundleVals.put(para, s2);
			}
			else {
				addToTrackAddData(settracknum, para, s2);
			}
			break;
		case TYPE_DELETED:
			if (settracknum == null) {
				deletedBundleVals.put(para, s1);
			}
			else {
				addToTrackDeleteData(settracknum, para, s1);
			}
			break;
		case TYPE_CHANGED:
			if (settracknum == null) {
				changedBundleVals.put(para, new String[]{s1,s2});
			}
			else {
				addToTrackChangeData(settracknum, para, new String[]{s1,s2});
			}
			break;
		default:
			return;
		}
	}


	private void parseAndSaveCompareValues(String para, boolean b1, boolean b2, String settracknum) {
		if (b1 != b2) {
			changedBundleVals.put(para, new String[]{Boolean.toString(b1),Boolean.toString(b2)});
		}		
	}


	private void parseAndSaveCompareValues(Vector<Contributor> c1, Vector<Contributor> c2, String settracknum) {
		if ((c1 == null || c1.size() == 0) && (c2 == null || c2.size() == 0)) {
			return;
		}
		if (c1 == null || c1.size() == 0) {
			//alle aus c2 zu adden
			for (Contributor c : c2) {
				String val = c.getYear() != null ? c.getYear()+" " : "";
				val += c.getName();
				if (settracknum == null) {
					addedBundleVals.put("contributor_"+c.getType(), val);
				}
				else {
					addToTrackAddData(settracknum, "contributor_"+c.getType(), val);
				}
			}
			return;
		}
		else if (c2 == null || c2.size() == 0) {
			//add all to delete
			for (Contributor c : c1) {
				String val = c.getYear() != null ? c.getYear()+" " : "";
				val += c.getName();
				if (settracknum == null) {
					deletedBundleVals.put("contributor_"+c.getType(), val);
				}
				else {
					addToTrackDeleteData(settracknum, "contributor_"+c.getType(), val);
				}
			}
			return;
		}

		//now the tricky part, what is added/deleted
		//First test what is deleted
		outerLoop:	
			for (Contributor cOld : c1) {
				String val1 = cOld.getYear() != null ? cOld.getYear()+" " : "";
				val1 += cOld.getName();
				//get that contributor and compare to new
				for (Contributor cNew : c2) {
					String val2 = cNew.getYear() != null ? cNew.getYear()+" " : "";
					val2 += cNew.getName();
					//Test if "Year Name"type equals that of cNew
					if ((val1+cOld.getType()).equals(val2+cNew.getType())) {
						continue outerLoop; //if yes, just skipp it
					}
				}
				//here we didn't find that contributor, so it must be deleted
				if (settracknum == null) {
					deletedBundleVals.put("contributor_"+cOld.getType(), val1);
				}
				else {
					addToTrackDeleteData(settracknum, "contributor_"+cOld.getType(), val1);
				}
			}

		//now test for added/new contributors
		outerLoop:	
			for (Contributor cNew : c2) {
				String val1 = cNew.getYear() != null ? cNew.getYear()+" " : "";
				val1 += cNew.getName();
				//get that contributor and compare to new
				for (Contributor cOld : c1) {
					String val2 = cOld.getYear() != null ? cOld.getYear()+" " : "";
					val2 += cOld.getName();
					//Test if "Year Name"type equals that of cNew
					if ((val1+cOld.getType()).equals(val2+cNew.getType())) {
						continue outerLoop; //if yes, just skipp it
					}
				}
				//here we didn't find that contributor, so it must be deleted
				if (settracknum == null) {
					addedBundleVals.put("contributor_"+cNew.getType(), val1);
				}
				else {
					addToTrackAddData(settracknum, "contributor_"+cNew.getType(), val1);
				}
			}
	}


	private void parseAndSaveCompareValues(BundleTexts t1, BundleTexts t2) {
		if (t1 == null && t2 == null) {
			return;
		}
		if ((t1 == null || t1.getPromotextCount() == 0) && t2.getPromotextCount() > 0) {
			//alle aus t2 zu adden
			for (int i = 0; i < t2.getPromotextCount(); i++) {
				addedBundleVals.put("promotext_"+t2.getPromotextLanguage(i), t2.getPromotext(i));
			}	
		}
		else if ((t1 == null || t1.getTeasertextCount() == 0) && t2.getTeasertextCount() > 0) {
			//alle aus t2 zu adden
			for (int i = 0; i < t2.getTeasertextCount(); i++) {
				addedBundleVals.put("teasertext_"+t2.getTeasertextLanguage(i), t2.getTeasertext(i));
			}	
		}
		else if ((t2 == null || t2.getPromotextCount() == 0) && t1.getPromotextCount() > 0) {
			//alle aus t1 zu deleten
			for (int i = 0; i < t1.getPromotextCount(); i++) {
				deletedBundleVals.put("promotext_"+t1.getPromotextLanguage(i), t1.getPromotext(i));
			}	
		}
		else if ((t2 == null || t2.getTeasertextCount() == 0) && t1.getTeasertextCount() > 0) {
			//alle aus t1 zu deleten
			for (int i = 0; i < t1.getTeasertextCount(); i++) {
				deletedBundleVals.put("teasertext_"+t1.getTeasertextLanguage(i), t1.getTeasertext(i));
			}	
		}

		if (t1 == null || t2 == null) {
			return;
		}

		HashSet<String> tmpVals = new HashSet<String>();

		//Now test every old with new value
		for (int i = 0; i < t1.getTeasertextCount(); i++) {
			String tmpVal = "t"+t1.getTeasertextLanguage(i);
			tmpVals.add(tmpVal);
			boolean foundEntry = false;
			for (int j = 0; j < t2.getTeasertextCount(); j++) {
				if (t1.getTeasertextLanguage(i).equals(t2.getTeasertextLanguage(j))) {
					foundEntry = true;
					//Teste, ob inhalte gleich sind
					if (!t1.getTeasertext(i).equals(t2.getTeasertext(j))) {
						//value changed
						changedBundleVals.put("teasertext_"+t1.getTeasertextLanguage(i), new String[]{t1.getTeasertext(i),t2.getTeasertext(j)});
					}
					break;
				}				
			}
			if (!foundEntry) {
				deletedBundleVals.put("teasertext_"+t1.getTeasertextLanguage(i), t1.getTeasertext(i));
			}
		}
		//now check newly added texts
		for (int j = 0; j < t2.getTeasertextCount(); j++) {
			if (!tmpVals.contains("t"+t2.getTeasertextLanguage(j))) {
				addedBundleVals.put("teasertext_"+t2.getTeasertextLanguage(j), t2.getTeasertext(j));
			}				
		}

		//PROMO
		tmpVals = new HashSet<String>();
		//Now test every old with new value
		for (int i = 0; i < t1.getPromotextCount(); i++) {
			String tmpVal = "p"+t1.getPromotextLanguage(i);
			tmpVals.add(tmpVal);
			boolean foundEntry = false;
			for (int j = 0; j < t2.getPromotextCount(); j++) {
				if (t1.getPromotextLanguage(i).equals(t2.getPromotextLanguage(j))) {
					foundEntry = true;
					//Teste, ob inhalte gleich sind
					if (!t1.getPromotext(i).equals(t2.getPromotext(j))) {
						//value changed
						changedBundleVals.put("promotext_"+t1.getPromotextLanguage(i), new String[]{t1.getPromotext(i),t2.getPromotext(j)});
					}
					break;
				}				
			}
			if (!foundEntry) {
				deletedBundleVals.put("promotext_"+t1.getPromotextLanguage(i), t1.getPromotext(i));
			}
		}
		//now check newly added texts
		for (int j = 0; j < t2.getPromotextCount(); j++) {
			if (!tmpVals.contains("p"+t2.getPromotextLanguage(j))) {
				addedBundleVals.put("promotext_"+t2.getPromotextLanguage(j), t2.getPromotext(j));
			}				
		}
	}



	private void parseAndSaveCompareValues(Territorial t1, Territorial t2, String settracknum) {
		if (t1 == null && t2 == null) {
			return;
		}
		if ((t1 == null || t1.getTerritorialCount() == 0) && t2.getTerritorialCount() > 0) {
			//alle aus t2 zu adden
			for (int i = 0; i < t2.getTerritorialCount(); i++) {
				if (settracknum == null) {
					addedBundleVals.put("territory_"+t2.getTerritory(i), t2.isTerritoryAllowed(i) ? "allow" : "disallow");
				}
				else {
					addToTrackAddData(settracknum, "territory_"+t2.getTerritory(i), t2.isTerritoryAllowed(i) ? "allow" : "disallow");
				}
			}	
			return;
		}
		else if ((t2 == null || t2.getTerritorialCount() == 0) && t1.getTerritorialCount() > 0) {
			//alle aus t1 zu deleten
			for (int i = 0; i < t1.getTerritorialCount(); i++) {
				if (settracknum == null) {
					deletedBundleVals.put("territory_"+t1.getTerritory(i), t1.isTerritoryAllowed(i) ? "allow" : "disallow");
				}
				else {
					addToTrackDeleteData(settracknum, "territory_"+t1.getTerritory(i), t1.isTerritoryAllowed(i) ? "allow" : "disallow");
				}
			}	
			return;
		}

		HashSet<String> tmpVals = new HashSet<String>();

		//Now test every old with new value
		for (int i = 0; i < t1.getTerritorialCount(); i++) {
			String tmpVal = "t"+t1.getTerritory(i);
			tmpVals.add(tmpVal);
			boolean foundEntry = false;
			for (int j = 0; j < t2.getTerritorialCount(); j++) {
				if (t1.getTerritory(i).equals(t2.getTerritory(j))) {
					foundEntry = true;
					//Teste, ob inhalte gleich sind
//					System.out.println("TErr 1: "+t1.getTerritory(i)+" Allowed? :"+t1.isTerritoryAllowed(i));
//					System.out.println("TErr 2: "+t2.getTerritory(j)+" Allowed? :"+t2.isTerritoryAllowed(j));
					if (t1.isTerritoryAllowed(i) != t2.isTerritoryAllowed(j)) {
						//value changed
						if (settracknum == null) {
							changedBundleVals.put("territory_"+t1.getTerritory(i), new String[]{t1.isTerritoryAllowed(i)?"allow":"disallow",t2.isTerritoryAllowed(j)?"allow":"disallow"});
						}
						else {
							addToTrackChangeData(settracknum, "territory_"+t1.getTerritory(i), new String[]{t1.isTerritoryAllowed(i)?"allow":"disallow",t2.isTerritoryAllowed(j)?"allow":"disallow"});
						}
					}
					break;
				}				
			}
			if (!foundEntry) {
				if (settracknum == null) {
					deletedBundleVals.put("territory_"+t1.getTerritory(i), t1.isTerritoryAllowed(i) ? "allow" : "disallow");
				}
				else {
					addToTrackDeleteData(settracknum, "territory_"+t1.getTerritory(i), t1.isTerritoryAllowed(i) ? "allow" : "disallow");
				}
			}
		}

		//now check newly added texts
		for (int i = 0; i < t2.getTerritorialCount(); i++) {
			if (!tmpVals.contains("t"+t2.getTerritory(i))) {
				if (settracknum == null) {
					addedBundleVals.put("territory_"+t2.getTerritory(i), t2.isTerritoryAllowed(i) ? "allow" : "disallow");
				}
				else {
					addToTrackAddData(settracknum, "territory_"+t2.getTerritory(i), t2.isTerritoryAllowed(i) ? "allow" : "disallow");
				}
			}				
		}
	}



	private void parseAndSaveCompareValues(LicenseBasis l1, LicenseBasis l2, String settracknum) {
		if (l1 == null && l2 == null) {
			return;
		}
		if ((l1 == null || l1.getChannelsCount() == 0) && l2.getChannelsCount() > 0) {
			//alle aus l2 zu adden
			for (int i = 0; i < l2.getChannelsCount(); i++) {
				if (settracknum == null) {
					addedBundleVals.put("channel_"+l2.getChannelName(i), l2.getChannelAllowed(i) ? "allow" : "disallow");
				}
				else {
					addToTrackAddData(settracknum, "channel_"+l2.getChannelName(i), l2.getChannelAllowed(i) ? "allow" : "disallow");
				}
			}	
			return;
		}
		else if ((l2 == null || l2.getChannelsCount() == 0) && l1.getChannelsCount() > 0) {
			//alle aus l1 zu deleten
			for (int i = 0; i < l1.getChannelsCount(); i++) {
				if (settracknum == null) {
					deletedBundleVals.put("channel_"+l1.getChannelName(i), l1.getChannelAllowed(i) ? "allow" : "disallow");
				}
				else {
					addToTrackDeleteData(settracknum, "channel_"+l1.getChannelName(i), l1.getChannelAllowed(i) ? "allow" : "disallow");
				}
			}	
			return;
		}

		HashSet<String> tmpVals = new HashSet<String>();
		//Now test every old with new value
		for (int i = 0; i < l1.getChannelsCount(); i++) {
			String tmpVal = "t"+l1.getChannelName(i);
			tmpVals.add(tmpVal);
			boolean foundEntry = false;
			for (int j = 0; j < l2.getChannelsCount(); j++) {
				if (l1.getChannelName(i).equals(l2.getChannelName(j))) {
					foundEntry = true;
					//Teste, ob inhalte gleich sind
					if (l1.getChannelAllowed(i) != l2.getChannelAllowed(j)) {
						//value changed
						if (settracknum == null) {
							changedBundleVals.put("channel_"+l1.getChannelName(i), new String[]{l1.getChannelAllowed(i)?"allow":"disallow",l2.getChannelAllowed(j)?"allow":"disallow"});
						}
						else {
							addToTrackChangeData(settracknum, "channel_"+l1.getChannelName(i), new String[]{l1.getChannelAllowed(i)?"allow":"disallow",l2.getChannelAllowed(j)?"allow":"disallow"});
						}
					}
					break;
				}				
			}
			if (!foundEntry) {
				if (settracknum == null) {
					deletedBundleVals.put("channel_"+l1.getChannelName(i), l1.getChannelAllowed(i) ? "allow" : "disallow");
				}
				else {
					addToTrackDeleteData(settracknum, "channel_"+l1.getChannelName(i), l1.getChannelAllowed(i) ? "allow" : "disallow");
				}
			}
		}

		//now check newly added texts
		for (int i = 0; i < l2.getChannelsCount(); i++) {
			if (!tmpVals.contains("t"+l2.getChannelName(i))) {
				if (settracknum == null) {
					addedBundleVals.put("channel_"+l2.getChannelName(i), l2.getChannelAllowed(i) ? "allow" : "disallow");
				}
				else {
					addToTrackAddData(settracknum, "channel_"+l2.getChannelName(i), l2.getChannelAllowed(i) ? "allow" : "disallow");
				}
			}				
		}
	}


	//GENRE
	private void parseAndSaveCompareValues(ItemTags t1, ItemTags t2, String settracknum) {
		if (t1 == null && t2 == null) {
			return;
		}
		if ((t1 == null || t1.getGenresCount() == 0) && t2.getGenresCount() > 0) {
			//alle aus t2 zu adden
			for (int i = 0; i < t2.getGenresCount(); i++) {
				if (settracknum == null) {
					addedBundleVals.put("genre_"+(i+1), t2.getGenre(i));
				}
				else {
					addToTrackAddData(settracknum, "genre"+(i+1), t2.getGenre(i));
				}
			}	
			return;
		}
		else if ((t2 == null || t2.getGenresCount() == 0) && t1.getGenresCount() > 0) {
			//alle aus t1 zu deleten
			for (int i = 0; i < t1.getGenresCount(); i++) {
				if (settracknum == null) {
					deletedBundleVals.put("genre"+(i+1), t1.getGenre(i));
				}
				else {
					addToTrackDeleteData(settracknum, "genre"+(i+1), t1.getGenre(i));
				}
			}	
			return;
		}

		//HashSet<String> tmpVals = new HashSet<String>();
		int idx = 0;
		//Now test every old with new value
		for (idx = 0; idx < t1.getGenresCount(); idx++) {
			if (idx >= t2.getGenresCount()) {
				//if more old than new genres
				if (settracknum == null) {
					deletedBundleVals.put("genre_"+(idx+1),t1.getGenre(idx));
				}
				else {
					addToTrackDeleteData(settracknum, "genre"+(idx+1),t1.getGenre(idx));
				}
				continue;
			}			
			//now test the same entry (i) in new Feed
			//Teste, ob inhalte gleich sind
			if (!t1.getGenre(idx).equals(t2.getGenre(idx))) {
				//value changed
				if (settracknum == null) {
					changedBundleVals.put("genre_"+(idx+1),new String[]{t1.getGenre(idx),t2.getGenre(idx)});
				}
				else {
					addToTrackChangeData(settracknum, "genre_"+(idx+1),new String[]{t1.getGenre(idx),t2.getGenre(idx)});
				}
			}				
		}
		//Now the rest must be newly added
		//if more new than old genres
		//alle aus t2 zu adden
		for (int idx2 = idx; idx2 < t2.getGenresCount(); idx2++) {
			if (settracknum == null) {
				addedBundleVals.put("genre_"+(idx2+1), t2.getGenre(idx2));
			}
			else {
				addToTrackAddData(settracknum, "genre"+(idx2+1), t2.getGenre(idx2));
			}
		}			
	}





	private int compareValues(String o1, String o2) {
		if ((o1 == null || o1.length() == 0) && (o2 == null || o2.length() == 0)) {
			return TYPE_NO_CHANGE;
		}
		else if (o1 != null && o1.length() > 0 && (o2 == null || o2.length() == 0)) {
			return TYPE_DELETED;
		}
		else if ((o1 == null || o1.length() == 0) && o2 != null && o2.length() > 0) {
			return TYPE_ADDED;
		}
		return o1.equals(o2) ? TYPE_NO_CHANGE : TYPE_CHANGED;
	}
	
//	private int compareValues(Integer o1, Integer o2) {
//		if (o1 == null && o2 == null) {
//			return TYPE_NO_CHANGE;
//		}
//		return (o1 == o2) ? TYPE_NO_CHANGE : TYPE_CHANGED;
//	}


	private void addToTrackAddData(String settracknum, String para, String value) {
		if (addedTrackVals.containsKey(settracknum)) {
			addedTrackVals.get(settracknum).put(para, value);
		}
		else {
			LinkedHashMap<String, String> values = new LinkedHashMap<String, String>();
			values.put(para, value);
			addedTrackVals.put(settracknum, values);
		}
	}

	private void addToTrackDeleteData(String settracknum, String para, String value) {
		if (deletedTrackVals.containsKey(settracknum)) {
			deletedTrackVals.get(settracknum).put(para, value);
		}
		else {
			LinkedHashMap<String, String> values = new LinkedHashMap<String, String>();
			values.put(para, value);
			deletedTrackVals.put(settracknum, values);
		}
	}

	private void addToTrackChangeData(String settracknum, String para, String[] valuesOldNew) {
		if (changedTrackVals.containsKey(settracknum)) {
			changedTrackVals.get(settracknum).put(para, valuesOldNew);
		}
		else {
			LinkedHashMap<String, String[]> values = new LinkedHashMap<String, String[]>();
			values.put(para, valuesOldNew);
			changedTrackVals.put(settracknum, values);
		}
	}


	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		//args0 = origFile, args1 = newFile

		Document doc1 = Document.fromFile(new File(args[0]));
		Document doc2 = Document.fromFile(new File(args[1]));

		Feed originalFeed = Feed.fromBusinessObject(BusinessObject.fromElement(doc1.getRootElement()));
		Feed newFeed = Feed.fromBusinessObject(BusinessObject.fromElement(doc2.getRootElement()));

		FeedComparer fc = new FeedComparer(originalFeed, newFeed);

		fc.compareFeedValues(0);

		Iterator<Entry<String, String>> it = fc.addedBundleVals.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> e = it.next();
			String para = e.getKey();
			System.out.println("Added to New Album PARA: "+para+" DATA: "+fc.addedBundleVals.get(para));
		}

		it = fc.deletedBundleVals.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> e = it.next();
			String para = e.getKey();
			System.out.println("Delete from New Album PARA: "+para+" DATA: "+fc.deletedBundleVals.get(para));
		}

		Iterator<Entry<String, String[]>> it2 = fc.changedBundleVals.entrySet().iterator();
		while (it2.hasNext()) {
			Entry<String, String[]> e = it2.next();
			String para = e.getKey();
			System.out.println("Change in New Album PARA: "+para+" DATA old: "+fc.changedBundleVals.get(para)[0]+" - DATA new: "+fc.changedBundleVals.get(para)[1]);
		}

		System.out.println("TRACK DATA .... No of tracks: "+fc.allTrackSetNumsIsrcs.size());

		it = fc.allTrackSetNumsIsrcs.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> e1 = it.next();
			String s = e1.getKey();
			String isrc = e1.getValue();
			System.out.println("TRACK No: "+s+" - ISRC: "+isrc);
			boolean change = false;
			if (fc.addedTrackVals.containsKey(s)) {
				Iterator<Entry<String, String>> it3 = fc.addedTrackVals.get(s).entrySet().iterator();
				while (it3.hasNext()) {
					Map.Entry<String, String> e = it3.next();
					String para = e.getKey();
					System.out.println("Added to New Track PARA: "+para+" DATA: "+fc.addedTrackVals.get(s).get(para));
					change = true;
				}	 	    	    
			}
			if (fc.deletedTrackVals.containsKey(s)) {
				Iterator<Entry<String, String>> it3 = fc.deletedTrackVals.get(s).entrySet().iterator();
				while (it3.hasNext()) {
					Map.Entry<String, String> e = it3.next();
					String para = e.getKey();
					System.out.println("Delete from Old Track PARA: "+para+" DATA: "+fc.deletedTrackVals.get(s).get(para));
					change = true;
				}	 	    	    
			}
			if (fc.changedTrackVals.containsKey(s)) {
				it2 = fc.changedTrackVals.get(s).entrySet().iterator();
				while (it2.hasNext()) {
					Map.Entry<String, String[]> e = it2.next();
					String para = e.getKey();
					System.out.println("Changed from Old to New Track PARA: "+para+" DATA old: "+fc.changedTrackVals.get(s).get(para)[0]+" - DATA new: "+fc.changedTrackVals.get(s).get(para)[1]);
					change = true;
				}	 	    	    
			}
			if (!change) {
				System.out.println("... NO CHANGE");
			}

		}

	}
}
