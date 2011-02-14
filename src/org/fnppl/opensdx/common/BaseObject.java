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

import org.fnppl.dbaccess.*;
import org.jdom.*;
import java.util.*;

public abstract class BaseObject {
	protected Vector<String> names = new Vector<String>();
	protected Vector<Object> values = new Vector<Object>();
	protected Hashtable<String, Object> changes = null;
	
	public BaseObject() {
    }
    
    public BaseObject fromElement(Element myObjectElement) {
        return null;
    }
    
    
    /**
     * @param name
     * @return
     */
    public boolean getBoolean(String name) {
    	Object v = getObject(name);
        if(v instanceof Boolean) {
        	return ((Boolean)v).booleanValue();
        }        
        return v.toString().indexOf("t")==0;
    }
    /**
     * @param name
     * @return
     */
    public long getLong(String name) {
        Object v = getObject(name);
        if(v instanceof Long) {
        	return ((Long)v).longValue();
        }        
        return Long.parseLong(v.toString());
    }
    /**
     * @param name
     * @return
     */
    public double getDouble(String name) {
        Object v = getObject(name);
        if(v instanceof Double) {
        	return ((Double)v).doubleValue();
        }        
        return Double.parseDouble(v.toString());
    }
    /**
     * @param name
     * @return
     */
    public int getInt(String name) {
        Object v = getObject(name);
        if(v instanceof Integer) {
        	return ((Integer)v).intValue();
        }        
        return Integer.parseInt(v.toString());
    }
    /**
     * @param name
     * @return
     */
    public Object getObject(String name) {
    	Object v = values.get(names.indexOf(name));        
        return v;
    }
    /**
     * @param name
     * @return
     */
    public String get(String name) {
        Object v = values.get(names.indexOf(name));
        return v.toString();
    }
    
    /**
     * @param name
     * @param value
     * @return
     */
    public boolean compareStringValue(String name, String value) {
        Object v = values.get(names.indexOf(name));
        if (v==null && value==null) {
        	return true;
        }
        if (v!=null && value==null) {
        	return false;
        }
        return value.equals(v);
    }

    /**
     * @param name
     * @return
     */
    public boolean isSet(String name)  {
        return names.contains(name);
    }
    
    /**
     * @return
     */
    public boolean valuesAreNull()  {
    	for (int i=0; i < values.size(); i++) {
    		if (values.get(i) != null) {
    			return false;
    		}
    	}
        return true;
    }
    
    /**
     * @param name
     * @param v
     * @return
     */
    public boolean set(String name, long v) {
    	if(changes==null) {
    		changes = new Hashtable<String, Object>();
    	}
        if(!isSet(name)) {
            //values.set(names.indexOf(name), new Long(v));
        	values.addElement(new Long(v));
        	names.addElement(name);
        	changes.put(name, new Long(v));            
            return true;
        }
        
        long l = getLong(name);
        if(v!=l) {            
        	values.set(names.indexOf(name), new Long(v));
        	changes.put(name, v);
            return true;
        }
        
        return false;
    }
    /**
     * @param name
     * @param v
     * @return
     */
    public boolean set(String name, Object v) {
    	if(v==null) {
    		throw new RuntimeException("SOBject::set("+name+") may not be null");
    	}
    	
    	if(changes == null) {
    		changes = new Hashtable<String, Object>();
    	}
    	
    	if(!isSet(name)) {
            //values.set(names.indexOf(name), new Long(v));
        	values.addElement(v);
        	names.addElement(name);
        	changes.put(name, v);            
            return true;
        }
        
        Object l = getObject(name);
        if(!v.equals(l)) {  
        	values.set(names.indexOf(name), v);
        	changes.put(name, v);
            return true;
        }
        
        return false;
    }
    
    
    /**
     * @return
     */
    public Element toElement() {
        Class c = getClass();
        Element ret = new Element(c.getName().substring(c.getName().lastIndexOf(".")+1));
        
        for(int i=0; i<names.size(); i++) {
        	String key = names.elementAt(i);
            Element e = new Element(key);
            ret.addContent(e);
            Object o = values.elementAt(i);
            e.setText(o.toString()); //uargsn HT 20110210
        }
        
        return ret;
    }
}



