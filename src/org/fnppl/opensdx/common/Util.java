package org.fnppl.opensdx.common;
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
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 
 * @author Bertram Boedeker
 *
 */
public class Util {

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

}
