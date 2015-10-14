package org.fnppl.opensdx.keyserver_web;
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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

public class RenderVelocityAction {

	protected HashMap<String, String> parameter = new HashMap<String,String>();
	protected HashMap<String, Object> objects = new HashMap<String,Object>();
	
	protected Vector<String> keys = new Vector<String>();
	protected VelocityContext c = null;
	protected String templateprefix = "";
	protected String tmpl = null;
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	protected String encoding = "UTF-8";
	protected OutputStream outStream = null; 
	protected String method = "";
	
    public RenderVelocityAction(HttpServletRequest request, HttpServletResponse response, String method, String vmTemplate) {
    	this.request = request;
    	this.response = response;
    	tmpl = vmTemplate;
    	this.method = method;
    }
    
    //hook in here for business logic
    protected void process() {
    	
    	//add business objects that should be uses in vm template to objects
    	
    }
    
    
    public final void process(VelocityContext c) throws Exception {
    	if(tmpl == null) {
    		new RuntimeException("Missing Template");
    	}
    	this.c = c;
    	process();
    	Template t = null;
    	if(tmpl.indexOf(".vm")<0) {
    		t = Velocity.getTemplate(templateprefix + tmpl+".vm");
    	}
    	else {
    		t = Velocity.getTemplate(templateprefix + tmpl);
    	}
        makeOutput(t);    	
    }

    private void makeOutput(Template t) throws Exception {    
    	
    	//add present keys
    	Object[] cKeys = c.getKeys();
    	for (Object key : cKeys) {
    		keys.add(key.toString());
    	}
    	
    	//add parameter
		Iterator<Entry<String,String>> itParams = parameter.entrySet().iterator();
		while (itParams.hasNext()) {
			Entry<String,String> e = itParams.next();
			String key = "param_"+e.getKey();
            c.put(key, e.getValue());
        	keys.add(key);
        }
		
		//add objects
		Iterator<Entry<String,Object>> itObjects = objects.entrySet().iterator();
		while (itObjects.hasNext()) {
			Entry<String,Object> e = itObjects.next();
            c.put(e.getKey(), e.getValue());
            keys.add(e.getKey());
        }
		
		c.put("action_keys", keys);
    	
		if(outStream == null) {
			prepareOut(response, "text/html; charset="+encoding);
		}
		OutputStreamWriter ow = new OutputStreamWriter(outStream, encoding);
		t.merge(c, ow);
		ow.flush();
	} 
    
    private final void prepareOut(HttpServletResponse response, String _mime) {
		String mime = _mime;
		if(_mime.indexOf("text/")>=0 && _mime.indexOf("chars")<0) {
			mime = _mime+";charset="+encoding;
		}
//		long ll = System.currentTimeMillis() - 1000*60*60*24;
//
//		response.addDateHeader("expires", ll);//5 sekunden
//		response.setDateHeader("Last-Modified", ll);

		response.addHeader("Content-Type", mime);
		response.setContentType(mime);
		response.setHeader("Connection", "close");

		try  {
			outStream = response.getOutputStream();
		} catch(Exception ex) {
			ex.printStackTrace();
		}        
	}
    
    public void putParam(String key, String value) {
    	parameter.put(key, value);
    }
    
    public void putObject(String key, Object value) {
    	objects.put(key, value);
    }
    
	public final String getParamString(String name) {
		return parameter.get(name);
	}
	
	public final String getContextString(String name) {
		try {
			Object obj = c.get(name);
			return obj.toString();
		} catch (Exception ex) {
			return "Error parsing value.";
		}
	}
}
