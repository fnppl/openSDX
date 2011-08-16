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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.fnppl.opensdx.common.*;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

public class FeedValidator {
	public final static URL FILE_OSDX_0_0_1 = FeedCreator.class.getResource("resources/openSDX_00-00-00-01.xsd");
	
	public boolean validateOSDX_0_0_1(Feed f) throws Exception { //validate against oSDX 0.0.1 (mayor minor sub)
		File file = new File(FILE_OSDX_0_0_1.toURI());
		if(!file.exists()) { throw new Exception("Validation Error. Schema-File not loaded."); }
		
		// ToDo: cut of UTF-8 characters before the first document tag
		// -> org.xml.sax.SAXParseException: Content is not allowed in prolog
		String xml = f.toString().trim().replaceFirst("^([\\W]+)<","<");
		return validateXmlFeed(xml, file);
	}
	
	public String validateOSDX_0_0_1(File f) throws Exception { //validate against oSDX 0.0.1 (mayor minor sub)
		File file = new File(FILE_OSDX_0_0_1.toURI());
		if(!file.exists()) { throw new Exception("Validation Error. Schema-File not loaded."); }
		
		return validateXmlFile(f, file);
	}
	
	public boolean validateOSDX_0_1_0(Feed f) { //validate against oSDX 0.1.0 (mayor minor sub)
		return false;
	}
	
    public boolean validateXmlFeed(String f, File schemaFile) {
    	try
		{
			// use a SchemaFactory and a Schema for validation
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Source schemaSource = new StreamSource(schemaFile);
			Schema schema = schemaFactory.newSchema(schemaSource);

			ByteArrayInputStream bs = new ByteArrayInputStream(f.getBytes());
			
			SAXParserFactory dbf = SAXParserFactory.newInstance();
			dbf.setValidating(false); 
			dbf.setNamespaceAware(true); //so we can correctly validate the file.
			dbf.setSchema(schema);
			SAXParser db = dbf.newSAXParser();
			XMLReader reader = db.getXMLReader();
			
			InputSource is = new InputSource(bs);
			reader.parse(is); // try to parse and validate
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
    	
    	return true;
	}	
	
    public String validateXmlFile(File xmlFile, File schemaFile) {
    	String message ="";
    	try
		{
    		// use a SchemaFactory and a Schema for validation
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Source schemaSource = new StreamSource(schemaFile);
			Schema schema = schemaFactory.newSchema(schemaSource);

			InputStream inputStream= new FileInputStream(xmlFile);
			Reader r = new InputStreamReader(inputStream,"UTF-8");
			
			SAXParserFactory dbf = SAXParserFactory.newInstance();
			dbf.setValidating(false); 
			dbf.setNamespaceAware(true); //so we can correctly validate the file.
			dbf.setSchema(schema);
			SAXParser db = dbf.newSAXParser();
			XMLReader reader = db.getXMLReader();
			 
			InputSource is = new InputSource(r);
			reader.parse(is); // try to parse and validate
			
			message = "Yehaw. File is valid.";
		}
		catch (SAXParseException spe)
		{
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
	
	public static void main(String[] args) {
		
	}
}
