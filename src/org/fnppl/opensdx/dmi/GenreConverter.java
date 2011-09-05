package org.fnppl.opensdx.dmi;

import java.net.URL;
import java.util.*;

import org.fnppl.opensdx.xml.*;


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

public class GenreConverter {
	public static final int SIMFY_TO_OPENSDX = 1;
	public static final int FUDGE_TO_OPENSDX = 2;
	private final static URL CONVERTER_LIST_XML = GenreConverter.class.getResource("resources/genreConverterList.xml");
	private int type;
	private HashMap<String, String> matchMap = new HashMap<String, String>();
	
	private GenreConverter() {
		this.setType(0);
	}
	
	public static GenreConverter getInstance(int type) {
		GenreConverter gc = new GenreConverter();
		gc.setType(type);		
		gc.initMatchMap(CONVERTER_LIST_XML);		
		
		return gc;
	}

	public String convert(String genre) {
		String convertedGenre = null;
		if(genre.length()>0 && matchMap.containsKey(genre.trim().toLowerCase())) {
			convertedGenre = matchMap.get(genre.trim().toLowerCase());	
		}
		else {
			convertedGenre = "[unknown genre] "+genre;
		}
		return convertedGenre;
	}
	
	private void initMatchMap(URL url) {
		try {
			Document doc = Document.fromURL(url);
			
			Vector<Element> matches = doc.getRootElement().getChildren("matches");
        	for (Iterator<Element> itMatches = matches.iterator(); itMatches.hasNext();) {
        		Element match = itMatches.next();
        		if(this.type==SIMFY_TO_OPENSDX) {
        			if(match.getChild("simfy")!=null && match.getChild("opensdx")!=null) {
		        		String key = match.getChildTextNN("simfy").toLowerCase();
		        		String value = match.getChildTextNN("opensdx");
		        		if(!matchMap.containsKey(key)) {
		        			// first entry in xml delivers the value for a key - important if matching the other way
		        			matchMap.put(key, value);
		        		}
        			}
        		}
        		else if(this.type==FUDGE_TO_OPENSDX) {
        			if(match.getChild("fudge")!=null && match.getChild("opensdx")!=null) {
		        		String key = match.getChildTextNN("fudge").toLowerCase();
		        		String value = match.getChildTextNN("opensdx");
		        		if(!matchMap.containsKey(key)) {
		        			// first entry in xml delivers the value for a key - important if matching the other way
		        			matchMap.put(key, value);
		        		}
        			}
        		}         		
            }
            
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
}
