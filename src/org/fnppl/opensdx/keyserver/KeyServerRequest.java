package org.fnppl.opensdx.keyserver;


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
import java.net.*;
import java.util.*;
import org.fnppl.opensdx.security.*;


public class KeyServerRequest {
	public Hashtable<String, String> headers = new Hashtable<String, String>();
	public String cmd = null;
	public String method = null;
	
	public static KeyServerRequest fromInputStream(InputStream in) throws Exception {
		//parse data
		//reject stupid requests (e.g. myriard-long single-lines in header)
		KeyServerRequest ret = new KeyServerRequest();
		
		String zeile = null;
		zeile = readHeaderLine(in);//cmdline
		
		String[] t = zeile.split(" ");
		ret.method = t[0];
		ret.cmd = t[1];
		String proto = t[2]; // HTTP/1.0 or HTTP/1.1
		
		//System.out.println("Method: "+ret.method+", cmd: "+ret.cmd+", proto: "+proto);
		
		while((zeile=readHeaderLine(in)) != null) {
			if(zeile.length() == 0) {
				break;//HTTP-header-sep
			}
			
			String header_name = zeile.substring(0, zeile.indexOf(" "));
			if (header_name.endsWith(":")) header_name = header_name.substring(0,header_name.length()-1);
			String header_value = zeile.substring(zeile.indexOf(" ")+1);
			System.out.println("adding header: "+header_name+"|"+header_value);
			
			ret.headers.put(header_name, header_value);//multiple-headers not possible then...
		}
		System.out.println("KeyServerRequest | end of request");
		
		return ret;
	}
	
	public String getHeaderValue(String headerName) {
		return headers.get(headerName);
	}
	
	private static String readHeaderLine(InputStream in) throws Exception {
		if (in.available()==0) return null;
        
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
        //HEADERS are ASCII
        
        byte[] b = new byte[1];
        int r = -1;

        char last='\r';
        
        
        while((r=in.read(b)) > 0) {
            char m = (char)b[0];
            if(m == '\n') {
                break;
            }
            else if(m != '\r') {
                bout.write(b[0]);
            }                   
        }
                
        if(r<0 && bout.size()==0) {
            return null;
        }
        String s = new String(bout.toByteArray(), "ASCII");
        System.out.println("KeyServerRequest | "+s);
        return s;
    }
	
}


