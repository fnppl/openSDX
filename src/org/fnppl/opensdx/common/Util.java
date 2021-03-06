package org.fnppl.opensdx.common;

/*
 * Copyright (C) 2010-2015 
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
import java.util.*;
import java.util.zip.*;

import org.fnppl.opensdx.dmi.FeedValidator;


/**
 * 
 * @author Bertram Bödeker <bboedeker@gmx.de>
 *
 */
public class Util {
	private static HashSet<String> languages = null;
	
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
	
	
	public static String makeParamsString(String[] txt) {
		if (txt!=null && txt.length>0) {
			try {
				StringBuffer out = new StringBuffer();
				out.append("\"");
				for (int i=0;i<txt.length;i++) {
					if (txt[i]!=null) {
						out.append(makeEscapeChars(txt[i]));
					}
					if (i<txt.length-1) {
						out.append("\",\"");
					} else {
						out.append("\"\n");
					}
				}
				return out.toString();
			} catch (Exception ex) {
				ex.printStackTrace();
			} 
		}
		return "";
	}
	
	public static String makeEscapeChars(String s) {
		String r = "";
		for (char c : s.toCharArray()) {
			if (c == '\\' || c == '\"') {
				r += '\\';
			}
			r += c;
		}
		return r;
	}
	
	public static String[] getParams(String param) {
		if (param == null) return null;
		String[] params = param.split("\",\"");
		params[0] = params[0].substring(1);
		int last = params.length-1;
		if (params[last].endsWith("\"\n")) {
			params[last] = params[last].substring(0,params[last].length()-2);
		} else {
			params[last] = params[last].substring(0,params[last].length()-1);
		}
		for (int i=0;i<params.length;i++) {
			params[i] = resolveEscapeChars(params[i]);
			//System.out.println("PARAMS ("+i+") "+params[i]);
		}
		return params;
	}
	
	public static String resolveEscapeChars(String s) {
		String r = "";
		char[] chars = s.toCharArray();
		for (int pos = 0; pos < chars.length; pos++) {
			if (chars[pos] == '\\') {
				pos++;
				if (pos<chars.length) {
					r += chars[pos];	
				}
			} else {
				r += chars[pos];
			}
		}
		return r;
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
    
//    public static void saveTextToFile(String text, String dateiname) {
//        try {
//            BufferedWriter out = new BufferedWriter(new FileWriter(dateiname));
//            out.write(text);
//            out.close();
//        } catch (IOException ioe) { System.err.println(ioe.toString());}
//    }
    
    public static void saveTextToFile(String text, File file) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            out.write(text);
            out.close();
        } catch (IOException ioe) { System.err.println(ioe.toString());}
    }
    
    public static void saveBytesToFile(byte[] data, File f) {
        try {
        	FileOutputStream out = new FileOutputStream(f);
            out.write(data);
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
    
	public static boolean deleteDirectory(File dir) {
		boolean ok = true;
		Vector<File> files = new Vector<File>();
		Vector<File> dirs = new Vector<File>();
		listFilesAndDirectories(dir, files, dirs);
		
		//delete all files
		while (ok && files.size()>0) {
			//System.out.println("delete file: "+files.get(0).getAbsolutePath());
			ok = files.remove(0).delete();
		}

		//delete all directories (in reverse order)
		Collections.sort(dirs);
		while (ok && dirs.size()>0) {
			//System.out.println("delete dir : "+dirs.get(dirs.size()-1).getAbsolutePath());
			ok = dirs.remove(dirs.size()-1).delete();
		}
		return ok;
	}
    
    public static void listFiles(File directory, Vector<File> result) {
		if (directory.exists()) {
			if (directory.isDirectory()) {
				File[] list = directory.listFiles();
				for(int i=0; i<list.length; i++) {
					if(list[i].isDirectory()) {
						listFiles(list[i],result);
					} else {
						result.add(list[i]);
					}
				}
			} else {
				result.add(directory);
			}
		}
	}
    
    public static void listFiles(File directory, Vector<File> result,  Vector<File> emptyDirectories) {
		if (directory.exists()) {
			if (directory.isDirectory()) {
				File[] list = directory.listFiles();
				if (list.length==0) {
					emptyDirectories.add(directory);
				} else {
					Arrays.sort(list);
					Vector<File> toTheEnd = new Vector<File>();
					for(int i=0; i<list.length; i++) {
						if(list[i].isDirectory()) {
							listFiles(list[i],result, emptyDirectories);
						} else {
							if (list[i].getName().endsWith(".finished")) {
								toTheEnd.add(list[i]);
							} else {
								result.add(list[i]);
							}
						}
					}
					result.addAll(toTheEnd);
				}
			} else {
				result.add(directory);
			}
		}
	}
    
    public static void listFilesAndDirectories(File directory, Vector<File> files,  Vector<File> directories) {
		if (directory.exists()) {
			if (directory.isDirectory()) {
				directories.add(directory);
				File[] list = directory.listFiles();
				for(int i=0; i<list.length; i++) {
					if(list[i].isDirectory()) {
						listFilesAndDirectories(list[i],files, directories);
					} else {
						files.add(list[i]);
					}
				}
			} else {
				files.add(directory);
			}
		}
	}
    
    /**
     * check if the languagecode is part of the openSDX_languages.xsd 
     * (keep in mind that this is case sensitive)
     * @param languagecode language to check
     * @return true if valid else false
     * @author Michael Witt <m.witt@finetunes.net>
     * @throws Exception 
     */
    public static boolean languageCodeExistsInXSD(String languagecode) throws Exception {
    	if (languages == null) {    		
    		
    		org.jdom2.input.SAXBuilder sax = new org.jdom2.input.SAXBuilder();
    		org.jdom2.filter.ElementFilter filter = new org.jdom2.filter.ElementFilter("enumeration");
    		org.jdom2.Document doc = sax.build(org.fnppl.opensdx.dmi.FeedValidator.class.getResourceAsStream("resources/"+org.fnppl.opensdx.dmi.FeedValidator.RESSOURCE_OSDX_0_0_1_LANGUAGES));
    		
    		languages = new HashSet<String>();
    		for(org.jdom2.Element e : doc.getRootElement().getDescendants(filter)) {
    			languages.add(e.getAttribute("value").getValue());
    		}
    	}
    			
    	return languages.contains(languagecode);
    }
}
