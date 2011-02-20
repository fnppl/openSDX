package org.fnppl.opensdx.keyserver;

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
		
		ret.method = zeile.substring(0, zeile.indexOf(" "));
		ret.cmd = zeile.substring(zeile.indexOf(" ")+1);
		String proto = ret.cmd.substring(ret.cmd.indexOf(" ")+1); // HTTP/1.0 or HTTP/1.1
		ret.cmd = ret.cmd.substring(0, ret.cmd.indexOf(" "));
		
		while((zeile=readHeaderLine(in)) != null) {
			if(zeile.length() == 0) {
				break;//HTTP-header-sep
			}
			
			String header_name = zeile.substring(0, zeile.indexOf(" "));
			String header_value = zeile.substring(zeile.indexOf(" ")+1);
			
			ret.headers.put(header_name, header_value);//multiple-headers not possible then...
		}
		
		return ret;
	}
	
	private static String readHeaderLine(InputStream in) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        
        //HEADERS are ASCII
        
        byte[] b = new byte[1];
        int r = 0;

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
        
        return new String(bout.toByteArray(), "ASCII");
    }
}


