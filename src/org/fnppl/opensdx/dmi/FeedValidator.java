package org.fnppl.opensdx.dmi;
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

import java.io.*;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.fnppl.opensdx.common.*;
import org.fnppl.opensdx.security.SecurityHelper;
import org.xml.sax.*;


public class FeedValidator {
	//public final static URL FILE_OSDX_0_0_1 = FeedValidator.class.getResource("resources/openSDX_00-00-00-01.xsd");
	public final static String  RESSOURCE_OSDX_0_0_1 = "openSDX_00-00-00-01.xsd";
	public final static String  RESSOURCE_OSDX_0_0_1_COUNTRIES = "openSDX_countryCodes.xsd";
	public final static String  RESSOURCE_OSDX_0_0_1_GENRES = "openSDX_genres.xsd";
	public final static String  RESSOURCE_OSDX_0_0_1_LANGUAGES = "openSDX_languages.xsd";
	
	private String message = "";
	private int errorCount = 0;
	private int errorLengthToShow = 200; // set to "-1" to show all

	public File xsdDir = null;
	
	public FeedValidator() {
		initXSDs();
	}
	
	public void initXSDs() {
		if(xsdDir != null) {
			//TODO HT 2011-11-29 - check each .xsd to be present as well...
			return;
		}
		
		try {
			File f = new File(System.getProperty("user.home"), "openSDX");
			f = new File(f, "xsd");
			if(!f.exists()) {
				boolean r = f.mkdirs();
				if(r) {
					System.out.println("Created openSDX-subdir \"xsd\" to store current schema-files.\nLocation: "+f.getAbsolutePath());
				}
			}
			if(!f.exists()) {
				//dir-creation failed - trying to go for tmpdir
				f = new File(System.getProperty("java.io.tmpdir"), "openSDX");
				f = new File(f, "xsd");
				if(!f.exists()) {
					boolean r = f.mkdirs();
					if(r) {
						System.out.println("Created TEMPORARY openSDX-subdir \"xsd\" to store current schema-files.\nLocation: "+f.getAbsolutePath());
					}
				}
				if(!f.exists()) {
					f = f.getParentFile(); //tmpdir then...					
				}								
			}
			xsdDir = f;

//			System.out.println("Getting resources/"+RESSOURCE_OSDX_0_0_1);
			SecurityHelper.copyResource(FeedValidator.class.getResourceAsStream("resources/"+RESSOURCE_OSDX_0_0_1), xsdDir, RESSOURCE_OSDX_0_0_1);
			
//			System.out.println("Getting resources/"+RESSOURCE_OSDX_0_0_1_COUNTRIES);
			SecurityHelper.copyResource(FeedValidator.class.getResourceAsStream("resources/"+RESSOURCE_OSDX_0_0_1_COUNTRIES), xsdDir, RESSOURCE_OSDX_0_0_1_COUNTRIES);
			
//			System.out.println("Getting resources/"+RESSOURCE_OSDX_0_0_1_LANGUAGES);
			SecurityHelper.copyResource(FeedValidator.class.getResourceAsStream("resources/"+RESSOURCE_OSDX_0_0_1_LANGUAGES), xsdDir, RESSOURCE_OSDX_0_0_1_LANGUAGES);
			
//			System.out.println("Getting resources/"+RESSOURCE_OSDX_0_0_1_GENRES);
			SecurityHelper.copyResource(FeedValidator.class.getResourceAsStream("resources/"+RESSOURCE_OSDX_0_0_1_GENRES), xsdDir, RESSOURCE_OSDX_0_0_1_GENRES);
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("XSDs inited in "+xsdDir.getAbsolutePath());
	}
	
	public String validateOSDX_latest(String xml) throws Exception {
		return validateOSDX_0_0_1(xml);
	}
	
	public String validateOSDX_latest(File f) throws Exception { 
		return validateOSDX_0_0_1(f);
	}
	public String validateOSDX_latest(Feed f) throws Exception { 
		return validateOSDX_0_0_1(f);
	}
	
	public String validateOSDX_0_0_1(String s) { //validate against oSDX 0.0.1 (mayor minor sub)
		
		///File file = new File(FILE_OSDX_0_0_1.toURI());
		//if(!file.exists()) { throw new Exception("Validation Error. Schema-File not loaded."); }

		String xml = s.trim().replaceFirst("^([\\W]+)<","<"); //HT 2011-11-29 WHY?!?!?!
				
		//return validateXmlFeed(xml, file);
		return validateXmlFeed(xml, RESSOURCE_OSDX_0_0_1);
	}
	
	public String validateOSDX_0_0_1(File f) { //validate against oSDX 0.0.1 (mayor minor sub)
		//File file = new File(FILE_OSDX_0_0_1.toURI());
		//if(!file.exists()) { throw new Exception("Validation Error. Schema-File not loaded."); }
		//return validateXmlFile(f, file);
		return validateXmlFile(f, RESSOURCE_OSDX_0_0_1);
	}
	
	public String validateOSDX_0_0_1(Feed f) { //validate against oSDX 0.1.0 (mayor minor sub)
		return validateOSDX_0_0_1(f.toElement().toString());  		
	}
	
   // public String validateXmlFeed(String f, File schemaFile) {
	 public String validateXmlFeed(String xml, String schemaName) {
    	try {
			// use a SchemaFactory and a Schema for validation
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Source schemaSource = new StreamSource(new File(xsdDir, schemaName));
			Schema schema = schemaFactory.newSchema(schemaSource);
			
			ByteArrayInputStream bs = new ByteArrayInputStream(xml.getBytes("UTF-8"));
			
			SAXParserFactory dbf = SAXParserFactory.newInstance();
			dbf.setValidating(false); 
			dbf.setNamespaceAware(true); //so we can correctly validate the file.
			dbf.setSchema(schema);
			SAXParser db = dbf.newSAXParser();
			XMLReader reader = db.getXMLReader();
			
		    reader.setErrorHandler(new ValidationErrorHandler());
			
			InputSource is = new InputSource(bs);
			reader.parse(is); // try to parse and validate	
			
			if(errorCount>0) {
				if(errorCount==1) {
					message = "Validation error! "+errorCount+" error occurred!\n"+ message+"\nPlease check.\n";
				}
				else {
					message = "Validation error! "+errorCount+" errors occurred!\n\n"+ message+"\nPlease check.\n";
				}
			}
		    
		}
		catch (SAXParseException spe)
		{
			spe.printStackTrace();
			
			// error generated by the parser
			message = "Validation error! \n  URI: " + spe.getSystemId() + "\n  Line: " + spe.getLineNumber() + "\n  Message: " + spe.getMessage();
			Exception x = spe;
			if (spe.getException() != null)
				x = spe.getException();
			
			message += " "+x;
		}
		catch (SAXException sxe)
		{
			Exception x = sxe;
			if (sxe.getException() != null)
				x = sxe.getException();
			
			message = "Error during validation." + x;

		}
		catch (ParserConfigurationException pce)
		{
			message = "Validator with specified options can't be built." + pce;
		}
		catch (IOException ioe)
		{
			// I/O error
			message = "Error validating file." + ioe;
		}  	
    	
    	return message;
	}	
	
    public String validateXmlFile(File xmlFile, String schemaName) {
    	try {
    		// use a SchemaFactory and a Schema for validation
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
//			Source schemaSource = new StreamSource(schemaStream);
			Source schemaSource = new StreamSource(new File(xsdDir, schemaName));
			Schema schema = schemaFactory.newSchema(schemaSource);

			InputStream inputStream= new FileInputStream(xmlFile);
			Reader r = new InputStreamReader(inputStream,"UTF-8");
			
			SAXParserFactory dbf = SAXParserFactory.newInstance();
			dbf.setValidating(false); 
			dbf.setNamespaceAware(true); //so we can correctly validate the file.
			dbf.setSchema(schema);
			SAXParser db = dbf.newSAXParser();
			XMLReader reader = db.getXMLReader();
			
			reader.setErrorHandler(new ValidationErrorHandler());

			InputSource is = new InputSource(r);
			reader.parse(is); // try to parse and validate
			
			if(errorCount>0) {
				if(errorCount==1) {
					message = "Validation error! "+errorCount+" error occurred!\n\n"+ message+"\nPlease check.\n";
				}
				else {
					message = "Validation error! "+errorCount+" errors occurred!\n\n"+ message+"\nPlease check.\n";
				}
			}			

		}
		catch (SAXParseException spe)
		{
			spe.printStackTrace();
			// error generated by the parser
			message = "Validation error! \n  URI: " + spe.getSystemId() + "\n  Line: " + spe.getLineNumber() + "\n  Message: " + spe.getMessage();
			Exception x = spe;
			if (spe.getException() != null)
				x = spe.getException();
			
			message += " "+x;
			// throw new Exception(message, x);
		
		}
		catch (SAXException sxe)
		{
			Exception x = sxe;
			if (sxe.getException() != null)
				x = sxe.getException();
			
			message = "Error during validation." + x;
			// throw new Exception("Error during validation.", x);

		}
		catch (ParserConfigurationException pce)
		{
			message = "Validator with specified options can't be built." + pce;
			// throw new Exception("Validator with specified options can't be built.", pce);
		}
		catch (IOException ioe)
		{
			// I/O error
			message = "Error validating file." + ioe;
			// throw new Exception("Error validating file.", ioe);
		}  	
    	
    	return message;
	}
    
    public class ValidationErrorHandler implements ErrorHandler {
        public void warning(SAXParseException e) throws SAXException {
        	errorCount++;
        	String msg = e.getMessage();
        	if(errorLengthToShow!=-1 && msg.length()>errorLengthToShow) msg = msg.substring(0, errorLengthToShow)+"...";
        	message += errorCount +". ["+e.getLineNumber() + "::"+e.getColumnNumber()+"] Message: "+msg+"\n"; 
        }

        public void error(SAXParseException e) throws SAXException {
        	errorCount++;  
        	String msg = e.getMessage();
        	if(errorLengthToShow!=-1 && msg.length()>errorLengthToShow) msg = msg.substring(0, errorLengthToShow)+"...";        	
        	message += errorCount +". ["+e.getLineNumber() + "::"+e.getColumnNumber()+"] Message: "+msg+"\n"; 
        }

        public void fatalError(SAXParseException e) throws SAXException {
        	errorCount++;
        	String msg = e.getMessage();
        	if(errorLengthToShow!=-1 && msg.length()>errorLengthToShow) msg = msg.substring(0, errorLengthToShow)+"...";        	
        	message += errorCount +". ["+e.getLineNumber() + "::"+e.getColumnNumber()+"] Message: "+msg+"\n"; 
        }
    }    
	
	public int getErrorLengthToShow() {
		return errorLengthToShow;
	}

	public void setErrorLengthToShow(int errorLengthToShow) {
		this.errorLengthToShow = errorLengthToShow;
	}    
    
	public int getErrorCount() {
		return errorCount;
	}
	public static void main(String[] args) {
		File f = new File(args[0]);
		FeedValidator fv = new FeedValidator();
		String msg = fv.validateOSDX_0_0_1(f);
		
		if(fv.errorCount > 0) {
			System.out.println("Errors occured: "+fv.errorCount);
		}
		System.out.println(msg);
	}
}
