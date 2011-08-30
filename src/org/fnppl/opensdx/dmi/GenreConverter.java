package org.fnppl.opensdx.dmi;

import java.io.File;
import java.net.URL;
import java.util.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;


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
	private final static URL SIMFY_TO_OPENSDX_XML = FeedValidator.class.getResource("resources/genreConverter_simfyToOpenSDX.xml");
	private int type;
	private HashMap<String, String> matchMap = null;
	
	private GenreConverter() {
		this.setType(0);
	}
	
	public static GenreConverter getInstance(int type) {
		GenreConverter gc = new GenreConverter();
		gc.setType(type);
		
		if(type==SIMFY_TO_OPENSDX) {
			gc.initMatchMap(SIMFY_TO_OPENSDX_XML);	
		}		
		
		return gc;
	}

	public String convertGenre(String genre) {
		String convertedGenre = null;
		
		if(matchMap.containsKey(genre)) {
			convertedGenre = matchMap.get(genre);	
		}
		
		return convertedGenre;
	}
	
	private void initMatchMap(URL url) {
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(new File(url.toURI()));
			
			// normalize text representation
			doc.getDocumentElement().normalize();
			
			NodeList listOfMatches = doc.getElementsByTagName("matches");

            for(int i=0; i<listOfMatches.getLength() ; i++){
            	String key = listOfMatches.item(i).getChildNodes().item(0).getNodeValue().trim();
            	String value = listOfMatches.item(i).getChildNodes().item(1).getNodeValue().trim();
            	matchMap.put(key, value);
            }
            
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
