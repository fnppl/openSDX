/*
 * Copyright (C) 2010-2012 
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
package org.fnppl.opensdx.keyserverfe;

import java.io.*;
import java.net.URL;
import java.util.*;

import javax.servlet.http.*;

import org.jdom.*;
import org.jdom.input.*;

import org.fnppl.dbaccess.*;


public class MyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected static Element config = new Element("fucker");
	protected static Vector searchengines;
    
    public MyServlet() throws Exception{
        System.out.println("Initialising MyServlet");
        
        synchronized(config) {                        
            if(config==null || config.getChildren().size()==0) {                
                try {
                    Locale.setDefault(Locale.GERMANY);
                    System.setProperty("file.encoding","UTF-8");
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
                
                try {
                    readConfig();
                    initLoadDB();
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
                
//                try {
//                	String g = config.getChildText("hibernatedir");
//                	File f = new File(g);
//                	if(f.exists() && f.isDirectory()) {
//                		XMLServerConnector.hibernatedir = f;
//                	}
//                } catch(Exception ex) {
////                	ex.printStackTrace();
//                }
            }
        }//synchronized   
    }
    
    
    private static final void readConfig() throws Exception {
        SAXBuilder sax = new SAXBuilder();
        
        Class<MyServlet> c = MyServlet.class;
        
        config = sax.build(new InputStreamReader(c.getResourceAsStream("resources/config.xml"))).getRootElement();
        
        searchengines = new Vector();        
        SAXBuilder builder = new SAXBuilder();
        try {
        	Iterator it = config.getChild("searchengines").getChildren().iterator();
        	while(it.hasNext()) {
        		Element e = (Element)it.next();
        		searchengines.addElement(e.getText().trim().toLowerCase());
        	}
        
    		ActionServlet.allowsearchengines = ! "false".equals(config.getChildText("allowsearchengines"));
    	} catch(Exception ex) {
//    		ex.printStackTrace();
    	}
    }
    
    public static void initOFFDB() throws Exception {
//        MyServlet m = new MyServlet();
        MyServlet.readConfig();
        
        System.out.println("Trying to init DB local: true");
        
        MyServlet.initLoadDB(true, 1);
    }
    
    public static void initLoadDB() {        
        initLoadDB(false, -1);
    }
    public static void initLoadDB(boolean local, int limitconns) {
        Element lbconfig = config.getChild("dbloadbalancer");
        BalancingConnectionManager.init(lbconfig,local,limitconns);
    }
        
    
    protected  byte[] getRessourceFile(String fileName) {
        try {
            Class<? extends MyServlet> c = this.getClass();
            InputStream in = c.getResourceAsStream("resources/"+fileName);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte[] b = new byte[512];//besoffnen buffer nehmen!!!
            int read = 0;
            while((read=in.read(b)) >= 0) {
                bout.write(b,0,read);
            }

            return bout.toByteArray();
        }
        catch(Exception e){e.printStackTrace();}
        
        return new byte[0];
    }
    
    
//    public void doPost(HttpServletRequest request, HttpServletResponse response) {
//        
//    }               
//    
//    
//    public void doGet(HttpServletRequest request, HttpServletResponse response) {
//    }
}
