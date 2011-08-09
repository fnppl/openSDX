package org.fnppl.opensdx.security;
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
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import org.fnppl.opensdx.keyserver.PostgresBackend;
import org.fnppl.opensdx.pdf.PDFUtil;
import org.fnppl.opensdx.xml.Element;

import com.sun.org.apache.bcel.internal.generic.GETSTATIC;

public class ReportGenerator {
	
	public static void buildFileSignatureVerificationReport(Element report, File outputPDF) {
		URL html_template = ReportGenerator.class.getResource("resources/file_verification_report_DE.html");
		try {
		    BufferedReader in = new BufferedReader(new InputStreamReader(html_template.openStream()));
            StringBuffer textBuffer = new StringBuffer();
            String line = null;
            while ((line = in.readLine())!=null) {
            	textBuffer.append(line+"\n");
            }
             
            String text = textBuffer.toString();
	        int sigStart = text.indexOf("<!-- signature start -->");
	        int sigEnd = text.indexOf("<!-- signature end -->");
            
	        
	        String preSig = text.substring(0,sigStart);
	        String postSig = text.substring(sigEnd);
	        String sigTemplate = text.substring(sigStart,sigEnd);
	        
	        int idStart = sigTemplate.indexOf("<!-- identity field start -->");
	        int idEnd = sigTemplate.indexOf("<!-- identity field end -->");
	        String preID = sigTemplate.substring(0,idStart);
	        String idTemplate = sigTemplate.substring(idStart, idEnd);
	        String postID = sigTemplate.substring(idEnd);
	        
	        preSig = preSig.replace("[check_datetime]", report.getChildTextNN("check_datetime"));
	        preSig = preSig.replace("[orig_filename]", report.getChildTextNN("signed_filename"));
	        preSig = preSig.replace("[signature_filename]", report.getChildTextNN("signature_filename"));
            
	        preSig = preSig.replace("[orig_md5]", report.getChildTextNN("md5"));
	        preSig = preSig.replace("[orig_sha1]", report.getChildTextNN("sha1"));
	        preSig = preSig.replace("[orig_sha256]", report.getChildTextNN("sha256"));
            
            StringBuffer tb = new StringBuffer();
            tb.append(preSig+"\n");
            Vector<Element> sigs = report.getChildren("signature_verification_report");
            for (Element es : sigs) {
            	
	            String sig = ""+preID;
	            
	            String keyid = es.getChildTextNN("keyid");
	            sig = sig.replace("[sig_datetime]", es.getChildTextNN("signature_datetime"));
	            sig = sig.replace("[sig_keyid]", keyid);
	            sig = sig.replace("[sig_key_valid_from]", es.getChildTextNN("key_valid_from"));
	            sig = sig.replace("[sig_key_valid_until]", es.getChildTextNN("key_valid_until"));
	            sig = sig.replace("[sig_keyserver]", keyid.substring(keyid.indexOf('@')+1));
	           
	            Element eID = es.getChild("identity");
	            if (eID!=null) {
	            	Vector<Element> eIDparts = eID.getChildren();
	            	for (Element ep : eIDparts) {
	            		String name = ep.getName();
	            		if (!name.equals("photo") && !name.equals("sha256")) {
		            		String part = idTemplate.replace("[id_name]", name);
		            		part = part.replace("[id_value]", ep.getText());
		            		sig += part;
	            		}
	            	}
	            }
	            
	            String sigAppend = ""+postID;
	            Vector<Element> checks = es.getChildren("check");
	            boolean md5ok = false, sha1ok = false, sha256ok = false;
	            for (Element ec : checks) {
	            	String msg = ec.getChildTextNN("message");
	            	String value = ec.getChildTextNN("result");
	            	if (msg.equals("md5 hash matches")) {
	            		if (value.equals("OK")) {
	            			md5ok = true;
	            		}
	            	}
	            	else if (msg.equals("sha1 hash matches")) {
	            		if (value.equals("OK")) {
	            			sha1ok = true;
	            		}
	            	}
	            	else if (msg.equals("sha256 hash matches")) {
	            		if (value.equals("OK")) {
	            			sha256ok = true;
	            		}
	            	}
	            	else if (msg.equals("key id matches sha1 of modulus")) {
	            		sigAppend = sigAppend.replace("[check_keyid]", getStatusMsg(value));	
	            	}
	            	else if (msg.equals("key valid at signature datetime")) {
	            		sigAppend = sigAppend.replace("[check_valid]", getStatusMsg(value));	
	            	}
	            	else if (msg.equals("signature bytes sign hashes and datetime")) {
	            		sigAppend = sigAppend.replace("[check_sig_bytes]", getStatusMsg(value));	
	            		
	            	}
	            	else if (msg.equals("key allows signing")) {
	            		sigAppend = sigAppend.replace("[check_can_sign]", getStatusMsg(value));	
	            	}
	            }
	            if (md5ok && sha1ok && sha256ok) {
	            	sigAppend = sigAppend.replace("[check_hash]", getStatusMsg("OK"));
	            } else {
	            	sigAppend = sigAppend.replace("[check_hash]", getStatusMsg("FAILED"));
	            }
	            
	            //key verificator report
	            
	            sigAppend = sigAppend.replace("[check_key_keyserver]", "?");
	      
	            tb.append(sig);
	            tb.append(sigAppend);
	            tb.append("\n");
            }
            tb.append(postSig);
            
            PDFUtil.fromHTMLtoPDF(tb.toString(), outputPDF);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	private static String sOK = "OK";
	private static String sFAILED = "NEIN";
	private static String sUNKNOWN = "?";
	
	private static String getStatusMsg(String value) {
		if (value.equals("OK")) {
			return sOK;	
		} else if (value.equals("FAILED")) {
			return sFAILED;
		} else {
			return sUNKNOWN;
		}
	}
}
