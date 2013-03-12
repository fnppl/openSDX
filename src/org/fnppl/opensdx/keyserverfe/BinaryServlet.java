package org.fnppl.opensdx.keyserverfe;
/*
 * Copyright (C) 2010-2013 
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

import javax.servlet.http.*;

public class BinaryServlet extends MyServlet {
	private static final long serialVersionUID = 1L;
	private static File filesdir;
	
    public BinaryServlet() throws Exception {        
        super();
        
        getFilesDir();        
    }
    
    public static File getFilesDir() throws Exception {
        if(filesdir==null) {
           filesdir = new File("/tmp");
            if(config.getChild("filesdir")!=null) {
                filesdir = new File(config.getChildText("filesdir"));
                filesdir.mkdirs();
            }
        }        
        return filesdir;
    }
    
    
    public void doHead(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
    	doPost(request, response);
    }
    @SuppressWarnings("unchecked")
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
        Vector[] parameter = new Vector[2];
        parameter[0] = new Vector<String>();
        parameter[1] = new Vector<String>();
        
        try {
            Enumeration<String> e = request.getParameterNames();            
            while(e.hasMoreElements()) {
                String schnauze = e.nextElement();
                if(request.getParameter(schnauze).length()!=0) {
                    parameter[0].addElement(schnauze);
                }
            }
            
            for(int i=0; i<parameter[0].size(); i++) {
                parameter[1].addElement(request.getParameter((String)parameter[0].elementAt(i)));
            }
                        
        } catch(Exception e) {
            System.out.println("Fehler: "+e.getMessage()); 
            e.printStackTrace();
        }
        
        String pathinfo = request.getPathInfo();
    	if(pathinfo == null) {
    		pathinfo = "";
    	}
    	
    	String cmd = null;
    	long mid = MyAction.default_mandantid;
    	
    	StringTokenizer kk = new StringTokenizer(pathinfo, "/");
    	int i = 0;
    	Vector[] args = {new Vector<String>(), new Vector<String>()};
    	if(kk.countTokens()!=0){
        	cmd = kk.nextToken();
        }
    	try {
    		if(kk.hasMoreTokens()) {
    			mid = Long.parseLong(kk.nextToken());
    		}
    	} catch(Exception ex) {
    		ex.printStackTrace();
    	}
    	
    	while(kk.hasMoreTokens()) {
    		String kkk = kk.nextToken();
//    		System.out.println("["+method+"] PATHINFO["+i+"]: "+kkk);
    		args[i%2].add(kkk);
    		i++;
    	}
    	conjoinParams(args, parameter);
        
       
//       response.setContentType(att.getMimetype());
//       //response.setHeader("Content-Disposition","attachment; filename=WOOHOO");
//       response.setHeader("Content-Disposition","inline; filename=WOOHOO");
//            	
//       OutputStream out = response.getOutputStream();
//                                           
//       FileInputStream fin = new FileInputStream(f);
//       byte[] buff = new byte[1024 * 128];
//       int r = 0;
//       while((r=fin.read(buff))!=-1) {
//    	   out.write(buff,0,r);                
//       }
//       out.flush();
//       fin.close();
//
//       out.close();
    	
    	String mime = null;
    	String attname = null;
    	
    	File midir = new File(filesdir, ""+mid);
    	if(!midir.exists()) {
    		midir.mkdirs();
    	}
    	
    	File f = null;
        if(cmd.equalsIgnoreCase("bundle_hl")) {
        	long bid = Long.parseLong(gimmeValueOf("bundleid", parameter));
        	f = new File(midir, "bundle_hl_"+bid);
        }
        else if(cmd.equalsIgnoreCase("bundle_add")) {
//        	asd
        }
        
//        if(f!=null) {        	
//        	System.out.println("BinaryServlet:: "+f.getPath());
//        }
        		
        if(f!=null && f.exists() && f.length()>0) {
        	response.setStatus(response.SC_OK);
        	
        	if(mime!=null) {
        		response.setContentType(mime);
        	}
        	if(attname!=null) {
        		response.setHeader("Content-Disposition","attachment; filename="+attname);
        	}
        	
        	OutputStream out =null;
        	BufferedOutputStream bout =null; 
        	FileInputStream fin = null;
    		BufferedInputStream bin = null;
    		
        	try {
        		out = response.getOutputStream();
        		bout = new BufferedOutputStream(out);
        		
        		fin = new FileInputStream(f);
        		bin = new BufferedInputStream(fin);
        		
        		byte[] buff = new byte[1024 * 128];
        		int r = 0;
        		while((r=bin.read(buff))!=-1) {
        			bout.write(buff,0,r);                
        		}
        		bout.flush();
        		bin.close();
        		bout.close();
        	} catch(Exception ex) {
        		if(bout!=null) {
        			try {bout.close();}catch(Exception exc){exc.printStackTrace();}
        		}
        		if(bin!=null) {
        			try {bin.close();}catch(Exception exc){exc.printStackTrace();}
        		}
        	}
        }
        else {
        	//what to do?
        	response.setStatus(response.SC_NOT_FOUND);
        }
       
    }
    public void conjoinParams(Vector<String>[] args, Vector[] parameter) {
		Vector<String> s1 = args[0];
		Vector<String> s2 = args[1];

		//PATHINFO OVERRIDES get/post-args!!!
		for(int i=0; i<s1.size(); i++) {    		
			try {
				String v1=null;
				String n1 = s1.elementAt(i);//name
				if (s1.size() == s2.size() )
					v1 = s2.elementAt(i);//wahhluuhs 
				else
					break;
				int nn = parameter[0].indexOf(n1);
				if(nn >= 0) {
					parameter[1].set(nn, v1);
				}
				else {
					parameter[0].addElement(n1);
					parameter[1].addElement(v1);
				}
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}
    
    
    /*public synchronized void doGet(HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }*/
    
    @SuppressWarnings("unchecked")
	public final boolean isset(String name, Vector[] parameter) {
        for(int i=0;i<parameter[0].size();i++) {
            if(name.equals((String)parameter[0].elementAt(i))) {                                
                return true;
            }
        }
        return false;
    }
    @SuppressWarnings("unchecked")
	public final String gimmeValueAt(int index, Vector[] parameter) {
        if(index < parameter[1].size()) {
                return (String)parameter[1].elementAt(index) ;
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
	public final int gimmeParameterCount(Vector[] parameter) {
        //hier wird also die anzahl (index+1) zur?ckgegeben
        return parameter[0].size() ;
    }
    
    @SuppressWarnings("unchecked")
	public final String gimmeNameAt(int index, Vector[] parameter) {
        if(index < parameter[0].size())
            return (String)parameter[0].elementAt(index) ;
    
        return null;
    }
    
    @SuppressWarnings("unchecked")
	public final String gimmeValueOf(String name, Vector[] parameter) {
        for(int i=0;i<parameter[0].size();i++) {
            if(name.equals((String)parameter[0].elementAt(i))) {                                
                return (String)parameter[1].elementAt(i) ;                
            }
        }
        //return "";
        return null;
    }
    
    public static String umlautEncode(String src) {
        if(src==null) {
            return "";
        }
        StringBuffer s=new StringBuffer();
        for(int i=0; i<src.length(); i++) {
            char c = src.charAt(i);
            
            switch(c) {
                case 'ä':
                    s.append("ae");
                    break;
                case 'Ä':
                    s.append("Ae");
                    break;
                case 'ö':
                    s.append("oe");
                    break;
                case 'Ö':
                    s.append("Oe");
                    break;                
                case 'Ü':
                    s.append("Ue");
                    break;
                case 'ü':
                    s.append("ue");
                    break;
                case 'ß':
                    s.append("ss");
                    break;
                case ' ':
                    s.append("_");
                    break;
                default:
                    s.append(c);
                    break;
            }
        }
        return s.toString();
    }
}
