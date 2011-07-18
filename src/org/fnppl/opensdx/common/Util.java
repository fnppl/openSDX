package org.fnppl.opensdx.common;

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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * 
 * @author Bertram Bödeker <bboedeker@gmx.de>
 *
 */
public class Util {
	
	private static char[] goodChars = null;
	public static String filterCharacters(String s) {
		if (goodChars==null) {
			String good = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ,.-_!?%&/() $=<>ßÄÖÜäöü*<>|0123456789{[]}\\;:@\"'";
			goodChars = new char[good.length()];
			for (int i=0;i<goodChars.length;i++) {
				goodChars[i] = good.charAt(i);
			}
			Arrays.sort(goodChars);
		}
		String f = "";
		for (int i=0;i<s.length();i++) {
			if (Arrays.binarySearch(goodChars,s.charAt(i))>=0) {
				f += s.charAt(i);
			} else {
				//System.out.println("wrong char: "+s.charAt(i));
			}
		}
		return f;
	}
	
	private static char[] goodCharsFile = null;
	public static String filterCharactersFile(String s) {
		if (goodCharsFile==null) {
			String good = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ,.-_ ßÄÖÜäöü0123456789";
			goodCharsFile = new char[good.length()];
			for (int i=0;i<goodCharsFile.length;i++) {
				goodCharsFile[i] = good.charAt(i);
			}
			Arrays.sort(goodCharsFile);
		}
		String f = "";
		for (int i=0;i<s.length();i++) {
			if (Arrays.binarySearch(goodCharsFile,s.charAt(i))>=0) {
				f += s.charAt(i);
			} else {
			//	System.out.println("wrong char: "+s.charAt(i));
			}
		}
		return f;
	}
	 public static void saveObjectToFile(File datei, Object obj) {
	    	//System.out.println("Saving Object to file: "+datei.getAbsolutePath());
	        try {
	            FileOutputStream file = new FileOutputStream( datei );
	            ObjectOutputStream o = new ObjectOutputStream( file );
	            o.writeObject(obj);
	            o.close();
	        } catch ( IOException e ) {
	        	System.out.println("Fehler beim Speichern.");
	        	e.printStackTrace();
	        }
	    }
	    
	    public static Object loadObject(File datei) {
	       // System.out.println("Lade "+datei.getAbsolutePath());
	        Object ob = null;
	        try {
	            FileInputStream file = new FileInputStream( datei );
	            ObjectInputStream in = new ObjectInputStream( file );
	            ob = in.readObject();
	            in.close();
	        } catch ( Exception e ) {System.out.println("Fehler beim Laden");e.printStackTrace(); }
	        //System.out.println("OK!");
	        return ob;
	    }

	public static void zipFiles(File archive, Vector<File> files) {
		final int BUFFER = 2048;  
		try {
			BufferedInputStream origin = null;
			FileOutputStream dest = new	FileOutputStream(archive);
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
			byte data[] = new byte[BUFFER];
			
			for (int i=0; i<files.size(); i++) {
				FileInputStream fi = new 
				FileInputStream(files.get(i));
				origin = new BufferedInputStream(fi, BUFFER);
				ZipEntry entry = new ZipEntry(files.get(i).getName());
				out.putNextEntry(entry);
				int count;
				while((count = origin.read(data, 0, BUFFER)) != -1) {
					out.write(data, 0, count);
				}
				origin.close();
			}
			out.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
    public static String loadText(String filename) {
    	StringBuffer text = new StringBuffer();
        try {
        	String zeile;
            BufferedReader in = new BufferedReader(new FileReader(filename));
            while ((zeile = in.readLine())!=null) {
            	text.append(zeile+"\n");
            }
            in.close();
           
        } catch (IOException ioe) { System.err.println(ioe.toString());}
        return text.toString();
    }
    
    public static void saveTextToFile(String text, String dateiname) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(dateiname));
            out.write(text);
            out.close();
        } catch (IOException ioe) { System.err.println(ioe.toString());}
    }

   
    
    public static String firstLetterUp(String name) {
    	return name.substring(0,1).toUpperCase()+name.substring(1).toLowerCase();
    }
    
    public static String cutNumericEnding(String n) {
    	if (!n.endsWith("ipv4") && !n.endsWith("ipv6") && !n.endsWith("sha1")) {
    		while (getPositiveNumber(n.substring(n.length()-1))>=0) {
        		n = n.substring(0,n.length()-1);
        	}
        	if (n.endsWith("_")) n = n.substring(0,n.length()-1);	
    	}
    	return n;
    }
    
    public static int getPositiveNumber(String s) {
    	try {
    		int i = Integer.parseInt(s);
    		return i;
    	} catch (Exception ex) {
    		return -1;
    	}
    }
    
}
