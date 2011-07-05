package org.fnppl.opensdx.dmi.wayin;

import java.io.*;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;

import org.fnppl.opensdx.common.*;
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

public class FinetunesToOpenSDXImporter extends OpenSDXImporterBase {
	
	public FinetunesToOpenSDXImporter(ImportType type, File impFile, File savFile) {
		super(type, impFile, savFile);
	}
	
	public ImportResult formatToOpenSDX() {
		ImportResult ir = null;		
		// do the import
		try {			
			// (1) get XML-Data from import document
            Document impDoc = Document.fromFile(this.importFile);
            Element root = impDoc.getRootElement();
                       
            // (2) get FeedInfo from import and set create new FeedInfo for openSDX
            boolean onlytest = true;
            String feedid = root.getAttribute("feedid");
            Calendar cal = Calendar.getInstance();
            long creationdatetime = cal.getTimeInMillis();
            long effectivedatetime = cal.getTimeInMillis();
            ContractPartner sender = ContractPartner.make(0, root.getAttribute("partner"), "");
            ContractPartner licensor = ContractPartner.make(1, "", "");
            
            FeedInfo feedinfo = FeedInfo.make(onlytest, feedid, creationdatetime, effectivedatetime, sender, licensor);
            
            // (3) create new feed with feedinfo
            Feed feed = Feed.make(feedinfo);              

            // (4) formating stuff -> put importdata (bundles/items) in OSDX format
            // -> releases to bundles/items
            Vector<Element> releases = root.getChildren("release");
            for (Iterator<Element> it = releases.iterator(); it.hasNext();) {
            	Element e = it.next();
            	
            }
            
            // ToDo: some format-magic here
            
            // (5) write file
			Document doc = Document.buildDocument(feed.toElement());
			doc.writeToFile(this.saveFile);
			ir = ImportResult.succeeded();
			
		} catch (Exception e) {
			ir = ImportResult.error(e.getMessage(), e);
		}		
		
		return ir;			
	}

}
