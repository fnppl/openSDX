package org.fnppl.opensdx.file_transfer.trigger;
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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;
import java.util.Map.Entry;

import org.fnppl.opensdx.xml.Element;

public class APICall implements FunctionCall {
		
	
	@SuppressWarnings("unchecked")
	private Class[] paramTypes = null;	
	private String classname = null;
	private String methodname = null;
	private Object[] params = null;
	private HashMap<Integer,String> placeHolder = new HashMap<Integer, String>();
	
	
//	<api_call>
//		<class>org.fnppl.opensdx.file_transfer.trigger.Trigger</class>
//		<method>writeln</method>
//		<param type="String">Hello World!</param> <!--  type: {String (default), int, byte, long, double, float, File}  -->
//	</api_call>
	
	public static APICall fromElemet(Element e) {
		APICall c = new APICall();
		c.classname = e.getChildText("class");
		c.methodname = e.getChildText("method");
		Vector<Element> params = e.getChildren("param");
		if (params==null || params.size()==0) {
			c.paramTypes = null;
			c.params = null;
		} else {
			int pCount = params.size();
			c.paramTypes = new Class[pCount];
			c.params = new Object[pCount];
			for (int i=0;i<pCount;i++) {
				Element ep = params.get(i);
				String pUse = ep.getAttribute("use");
				if (pUse!=null) {
					//replace with parameter
					c.paramTypes[i] = String.class;
					c.params[i] = "${"+pUse+"}";
					c.placeHolder.put(i, pUse);
				} else {
					String pType = ep.getAttribute("type");
					String value = ep.getText();
					if (value==null) value = "";
					
					if (pType.equalsIgnoreCase("int")) {
						c.paramTypes[i] = Integer.TYPE;
						c.params[i] = Integer.parseInt(value);
					}
					else if (pType.equalsIgnoreCase("byte")) {
						c.paramTypes[i] = Byte.TYPE;
						c.params[i] = Byte.parseByte(value);
					}
					else if (pType.equalsIgnoreCase("long")) {
						c.paramTypes[i] = Long.TYPE;
						c.params[i] = Long.parseLong(value);
					}
					else if (pType.equalsIgnoreCase("double")) {
						c.paramTypes[i] = Double.TYPE;
						c.params[i] = Double.parseDouble(value);
					}
					else if (pType.equalsIgnoreCase("float")) {
						c.paramTypes[i] = Float.TYPE;
						c.params[i] = Float.parseFloat(value);
					}
					else if (pType.equalsIgnoreCase("file")) {
						c.paramTypes[i] = File.class;
						c.params[i] = new File(value);
					} else {
						//use String for no type or anything else
						c.paramTypes[i] = String.class;
						c.params[i] = value;	
					}
				}
			}
		}
		return c;
	}
	
	public void run(boolean async, final HashMap<String, Object> context) {
		//prepare params
		
		if (async) {
			Thread t = new Thread() { 
				public void run() {
					Object[] myParams = prepareParams(context);
					if (myParams!=null) {
						makeAPICall(classname, methodname, paramTypes, myParams);
					}
				}
			};
			t.start();
		} else {
			Object[] myParams = prepareParams(context);
			if (myParams!=null) {
				makeAPICall(classname, methodname, paramTypes, myParams);
			}
		}
	}
	
	private Object[] prepareParams(HashMap<String, Object> context) {
		if (placeHolder.isEmpty()) return params;
		
		Object[] myParams = new Object[params.length];
		for (int i=0;i<params.length;i++) {
			myParams[i] = params[i];
		}
		
		for (Entry<Integer, String> e : placeHolder.entrySet()) {
			int i = e.getKey().intValue();
			String contextKey = e.getValue();
			Object value = context.get(contextKey);
			if (value==null) {
				System.out.println("ERROR in API CALL :: MISSING CONTEXT VARIABLE: "+contextKey);
				return null;
			}
			paramTypes[i] = value.getClass();
			myParams[i] = value;
		}
		return myParams;
	}
	
	public String toString() {
		return "API call: "+classname+" :: "+methodname+(params==null?"":" Param: "+Arrays.toString(params));
	}
	
	/**
	 * 
	 * @param classname :: class has to provide empty constructor
	 * @param methodname
	 * @param paramTypes
	 * @param params
	 */
	@SuppressWarnings("unchecked")
	public static void makeAPICall(String classname, String methodname, Class[] paramTypes, Object[] params) {
		try {
			Class c = Class.forName(classname);
			Method m = c.getMethod(methodname, paramTypes);
			m.invoke(c.newInstance(), params);
			
		} catch (NoSuchMethodException ex) {
			System.out.println("NoSuchMethodException: Method: "+methodname+" in class: "+classname);
			ex.printStackTrace();
		} catch (InvocationTargetException ex) {
			System.out.println("InvocationTargetException: Method: "+methodname+" in class: "+classname);
			ex.getCause().printStackTrace();
		} catch (IllegalAccessException ex) {
			System.out.println("IllegalAccessException: Method: "+methodname+" in class: "+classname);
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			System.out.println("ClassNotFoundException: Class: "+classname);
			ex.printStackTrace();
		} catch (IllegalArgumentException ex) {
			System.out.println("IllegalArgumentException: Class: "+classname);
		} catch (InstantiationException ex) {
			ex.printStackTrace();
		}
	}
}
