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
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;
/**
 * 
 * @author Bertram Bödeker <bboedeker@gmx.de>
 *
 */
public class Util {
	
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

    public static void createBaseClassesFromXML(File xml, File header, File saveToPath) {
    	try {
    		if (!saveToPath.exists()) {
    			saveToPath.mkdirs();
    		}
    		String head = null;
    		if (header.exists()) head = loadText(header.getAbsolutePath());
    		Vector<String> classesReady = new Vector<String>();
			Element e =  Document.fromFile(xml).getRootElement();
			buildClass(e, head, saveToPath, classesReady);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
    }
    
    private static void buildClass(Element e, String head, File saveToPath, Vector<String> classesReady) {
    	String name = e.getName();
    	if (e.getAttribute("lang")!=null) {
    		System.out.println("att: "+e.getAttribute(name));
    	}
    	name = firstLetterUp(name);
    	name = cutNumericEnding(name);
    	
    	if (classesReady.contains(name)) return;
    	
    	StringBuffer b = new StringBuffer();
    	StringBuffer m = new StringBuffer();
    	
    	if (head!=null) {
    		b.append(head);
    		b.append("\n");
    	}
    	b.append("import java.util.Vector;\n");
    	b.append("import org.fnppl.opensdx.common.BaseObject;\n");
    	b.append("\n");
    	b.append("public class "+name+" extends BaseObject {\n");
    	b.append("\n");
    	//constructor
    	b.append("	public "+name+"() {\n");
		Vector<String> vars = new Vector<String>();
    	Vector<Element> vc = e.getChildren();
    	
    	for (Element c : vc)  {
    		String n = c.getName();
    		boolean numeric = false;
    		//check for numeric ending
			String cutN = cutNumericEnding(n);
        	if (!cutN.equals(n)) {
        		n = cutN;
        		numeric = true;
        	}
    		
    		
    		String vName = n;
    		//check variable name
    		if (n.equals("break")) vName = "sBreak";
    		if (n.equals("for")) vName = "sFor";
    		if (n.equals("if")) vName = "sIf";
    		if (n.equals("else")) vName = "sElse";
    		
    		boolean hasAttributes = false;
    		String args1 = "";
    		String args2 = "";
    		if (c.getAttributes().size()>0) {
    			hasAttributes = true;
    			for (String[] s : c.getAttributes()) {
    				//System.out.println("   "+s[0]+"::"+s[1]);
    				args1 += "String "+s[0]+", ";
    				args2 += "\""+s[0]+"\", "+s[0]+", ";
    			}
    		}
    		//one or more times
			int count = e.getChildren(n).size();
			if (numeric) {
				//some more effort needed
				Vector<Element> child = e.getChildren();
				count = 0;
				for (Element ec : child) {
					String cn = cutNumericEnding(ec.getName());
					if (n.equals(cn)) {
						count++;
					}
				}
			}
			boolean add = false;
			String type = firstLetterUp(n);
			
			//System.out.println(c.getName()+" has children: "+c.getChildren().size());
			if (c.getChildren().size()>0) {
    			buildClass(c,head,saveToPath, classesReady);
    		} else {
    			type = "String";
    		}
			int anzTab = (30-n.length())/5;
			if ((30-n.length())%5>0) anzTab++;
    		if (count == 1 && !hasAttributes) {
	    		b.append("		names.add(\""+n+"\"); "); for (int i=0;i<anzTab;i++) b.append("\t");
				b.append("	values.add(null);\n");
    		} else {
    			if (!vars.contains(n)) {
    				if (hasAttributes) {
    					b.append("		names.add(\""+n+"\"); "); for (int i=0;i<anzTab;i++) b.append("\t");
	    				b.append("	values.add(new Vector<String[]>());\n");
	    				vars.add(n);
    					add = true;
    				} else {
	    				b.append("		names.add(\""+n+"\"); "); for (int i=0;i<anzTab;i++) b.append("\t");
	    				b.append("	values.add(new Vector<"+type+">());\n");
	    				vars.add(n);
	    				add = true;
    				}
    			}
    		}
//    		if (name.equals("Contributors")) {
//				System.out.println(name+"::count = "+count);
//				System.out.println(name+"::hasAtt= "+hasAttributes);
//				System.out.println(name+"::add = "+add);
//			}
			if (count == 1) {
    			//set and get methods
    			m.append("	public void set"+firstLetterUp(n)+"(String "+vName+") {\n");
    			m.append("		set(\""+n+"\", "+vName+");\n");
    			m.append("	}\n");
    			m.append("\n");
    			m.append("	public String get"+firstLetterUp(n)+"() {\n");
    			m.append("		return get(\""+n+"\");\n");
    			m.append("	}\n");
    			m.append("\n");
			} else {
				if (add) {
					if (hasAttributes) {
						//get, add and remove methods
						m.append("	public Vector<String[]> get"+firstLetterUp(n)+"() {\n");
		    			m.append("		return (Vector<String[]>)values.elementAt(names.indexOf(\""+n+"\"));\n");
		    			m.append("	}\n");
		    			m.append("\n");
						m.append("	public void add"+firstLetterUp(n)+"("+args1+"String "+vName+") {\n");
	    				m.append("		((Vector<String[]>)values.elementAt(names.indexOf(\""+n+"\"))).add(new String[]{"+args2+vName+"});\n");
		    			m.append("	}\n");
		    			m.append("\n");
		    			m.append("	public void remove"+firstLetterUp(n)+"(int index) {\n");
		    			m.append("		((Vector<String[]>)values.elementAt(names.indexOf(\""+n+"\"))).remove(index);\n");
		    			m.append("	}\n");
		    			m.append("\n");
					} else {
	    				//get, add and remove methods
						m.append("	public Vector<"+type+"> get"+firstLetterUp(n)+"() {\n");
		    			m.append("		return (Vector<"+type+">)values.elementAt(names.indexOf(\""+n+"\"));\n");
		    			m.append("	}\n");
						m.append("	public void add"+firstLetterUp(n)+"("+type+" "+vName+") {\n");
	    				m.append("		((Vector<"+type+">)values.elementAt(names.indexOf(\""+n+"\"))).add("+vName+");\n");
		    			m.append("	}\n");
		    			m.append("\n");
		    			m.append("	public void remove"+firstLetterUp(n)+"("+type+" "+vName+") {\n");
		    			m.append("		((Vector<"+type+">)values.elementAt(names.indexOf(\""+n+"\"))).remove("+vName+");\n");
		    			m.append("	}\n");
		    			m.append("\n");
					}
				}
			}
    		
    	}
    	b.append("	}\n"); // end of constructor
    	b.append("\n");
    	b.append("// methods\n");
    	b.append(m);
    	b.append("}\n");
    	//System.out.println("Class: \t"+name);
    	saveTextToFile(b.toString(), saveToPath.getAbsolutePath()+"/"+name+".java");
    	classesReady.add(name);
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
    
    public static void main(String[] args) {
    	File xml = new File("src/org/fnppl/opensdx/dmi/resources/example_feed.xml");
    	File saveToPath = new File("src/org/fnppl/opensdx/commonAuto");
    	
    	File header = new File("header.txt");
    	
    	createBaseClassesFromXML(xml, header, saveToPath);	//if (1==1) return;
    	
    
    	try {
	    	BaseObject test = BaseObject.fromElement(Document.fromFile(xml).getRootElement());
	    	System.out.println(test.getClass().getName());
	    	//Feed feed = (Feed)test;
	    	Document.buildDocument(test.toElement()).output(System.out);
	    	
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }
}
