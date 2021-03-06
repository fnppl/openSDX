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

package org.fnppl.opensdx.keyserverfe;


import java.io.*;
import java.util.*;
import org.jdom2.*;
import org.apache.velocity.*;
import org.apache.velocity.app.*;

import javax.servlet.http.*;



import javax.servlet.http.*;

/**
 *
 * @author Henning Thieß <ht@fnppl.org>
 */
public class EchoPageAction extends MyAction {
	public String tmpl = null;
	public boolean admin = false;
	
    public EchoPageAction(MultiTypeRequest request, HttpServletResponse response) {
        super(request, response);
    }
    
    public void performAction(String reqAddress, VelocityContext c) throws Exception {
    	if(tmpl == null) {
    		String g = gimmeValueOf("tmpl");
    		tmpl = g;
    	}
    
    	if(admin) {
    		if(tmpl.indexOf("admin_")!=0) {
    			throw new Exception("INVALID NOT_ADMIN");
    		}
    	}
    	else {
    		if(tmpl.indexOf("admin_")==0) {
    			throw new Exception("INVALID ADMIN");
    		}
    	}
    	
    	Template t = null;
    	if(tmpl.indexOf(".vm")<0) {
    		t = Velocity.getTemplate(templateprefix + tmpl+".vm");
    	}
    	else {
    		t = Velocity.getTemplate(templateprefix + tmpl);
    	}
        makeOutput(t, c);    	
    }
}
