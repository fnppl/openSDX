package org.fnppl.opensdx.demo;

/*
 * Copyright (C) 2010-2011 
 * 							fine people e.V. <opensdx@fnppl.org> 
 * 							Henning Thieß <ht@fnppl.org>
 * 
 * 							http://fnppl.org
 * 
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

import java.io.File;
import java.util.Properties;
import java.util.Vector;

import org.fnppl.opensdx.common.Util;
import org.fnppl.opensdx.security.KeyPairGenerator;
import org.fnppl.opensdx.security.SignAndVerify;

/**
 * 
 * @author Bertram Boedeker
 *
 */
public class CreateSignedPackage {
	
	public static void createSignedPackage(File xml, Vector<File> additionalFiles, Properties properties) throws Exception {
	/*	//sign xml file
		SignAndVerify.signTextFile(xml.getAbsolutePath(), properties.getProperty("filename.secret.key"), properties.getProperty("passphrase"));
		
		//collect files
		Vector<File> allFiles = new Vector<File>();
		allFiles.add(new File(xml.getAbsoluteFile()+".asc"));
		allFiles.addAll(additionalFiles);
		
		//pack in zip archive
		File archive = new File(xml.getAbsoluteFile()+".zip");
		Util.zipFiles(archive, allFiles);
		
		//create zip signature
		SignAndVerify.createSignature(archive.getAbsolutePath(), properties.getProperty("filename.secret.key"), properties.getProperty("passphrase"));
		
		//create timestamp signature
		//TODO getTimeStampSignFromServer
		 
	 */
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*
		// PGPKeyPairGenerator
		try {
			System.out.println("generating RSAKeyPair");
			String identitiy = "debug-key";
			String passPhrase = "debug";
			boolean formatASC = true;
			KeyPairGenerator.generateKeyPair(identitiy, passPhrase, formatASC);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
		//Create Signed Package
		System.out.println("Create Signed Package");
		Properties properties = new Properties();
		properties.setProperty("filename.secret.key","keypairs/secret.asc");
		properties.setProperty("passphrase","debug");
		//properties.setProperty("timestamp.signature.request.url","localhost:87654");
		
		File xml = new File("test-set/feed_1234567890.xml");
		Vector<File> additionalFiles = new Vector<File>();
		additionalFiles.add(new File("test-set/testdata.txt"));

		try {
			CreateSignedPackage.createSignedPackage(xml, additionalFiles,properties);
		} catch (Exception e) {
			e.printStackTrace();
		}
		 */
	}

}
