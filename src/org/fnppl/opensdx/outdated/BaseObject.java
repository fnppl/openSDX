package org.fnppl.opensdx.outdated;
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

import org.fnppl.dbaccess.*;
import org.fnppl.opensdx.common.Util;
import org.fnppl.opensdx.xml.Element;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class BaseObject {
	
	final static String RFC1123_CUT = "yyyy-MM-dd HH:mm:ss zzz";
	final static Locale ml = new Locale("en", "DE");
	public final static SimpleDateFormat datemeGMT = new SimpleDateFormat(RFC1123_CUT, ml);
	static {
		datemeGMT.setTimeZone(java.util.TimeZone.getTimeZone("GMT+00:00"));
	}
	
	protected Vector<String> names = new Vector<String>();
	protected Vector<Object> values = new Vector<Object>();
	protected Hashtable<String, Object> changes = null;
	
	public BaseObject() {
    }
	
	
    public static BaseObject fromElement(Element e) {
    	String name = e.getName();
    	name = Util.firstLetterUp(name);
    	name = Util.cutNumericEnding(name);
    	
    	try {
			Class cl = Class.forName("org.fnppl.opensdx.commonAuto."+name);
			
			BaseObject ob = (BaseObject)cl.newInstance();
			
	    	Vector<Element> vc = e.getChildren();
	    	for (Element c : vc)  {
	    		String n = c.getName();
	    		boolean numeric = false;
	    		String cutN = Util.cutNumericEnding(n);
	        	if (!cutN.equals(n)) {
	        		n = cutN;
	        		numeric = true;
	        	}
				boolean cIsClass = false;
				try {
					Class.forName("org.fnppl.opensdx.commonAuto."+Util.firstLetterUp(n));
					cIsClass = true;
				} catch (Exception ex) {}
				
				Object value = null;
				if (cIsClass) {
					value = BaseObject.fromElement(c);
				} else {
					if (c.getAttributes().size()>0) {
						Vector<String[]> atts = c.getAttributes();
		    			String[] val = new String[atts.size()*2+1];
						for (int i=0;i<atts.size();i++) {
		    				String[] s = atts.get(i);
		    				val[i*2] = s[0];
		    				val[i*2+1] = s[1];
		    			}
						val[val.length-1] = c.getText().trim();
						value = val;
		    		} else {
		    			value = c.getText();
		    		}
				}
				try {
					Object valObj = ob.getObject(n);
					if (valObj==null || !(valObj instanceof Vector)) {
						ob.set(n, value);
					} else {
						Vector v = (Vector)ob.getObject(n);
		    			v.add(value);	
					}
				} catch (Exception ex) {
					System.out.println("ERROR at "+name+"::"+n);
					ex.printStackTrace();
				}
			}
	    	return ob;
    	} catch (Exception e1) {
			e1.printStackTrace();
		}
    	return null;
    }
    
    public String getClassName() {
    	return getClass().getName().substring(getClass().getName().lastIndexOf('.')+1).toLowerCase();
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
        return v.toString().toLowerCase().indexOf("t")==0; //BB 20110215
    }
    /**
     * @param name
     * @return
     */
    public long getLong(String name) {
        Object v = getObject(name);
        if (v==null) return -1L;
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
    	if (!names.contains(name)) return null;
    	Object v = values.get(names.indexOf(name));        
        return v;
    }
    
    public int getIndexOfValue(Object value) { 
        return values.indexOf(value);
    }
    /**
     * @param name
     * @return
     */
    public String get(String name) {
        Object v = values.get(names.indexOf(name));
        if (v!=null)
        	return v.toString();
        return null;
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
    protected boolean valuesAreNull()  {
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
    	//if(v==null) {
    	//	throw new RuntimeException("SOBject::set("+name+") may not be null");
    	//}
    	
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
        
        
//        if(!v.equals(l)) {  
//        	values.set(names.indexOf(name), v);
//        	changes.put(name, v);
//            return true;
//        }
    	if (v==null) {
    		values.set(names.indexOf(name), v);
        	changes.put(name, v);
        	return true;
    	} else {
    		Object l = getObject(name);
    		if (l==null) {
	        	values.set(names.indexOf(name), v);
	        	changes.put(name, "NULL");
	        	return true;
    		} else {
    			if (!v.equals(l)) {
    				values.set(names.indexOf(name), v);
    	        	changes.put(name, v);
    	        	return true;
    			}
    		}
    	}
        return false;
    }
    
    public Vector<String> getNames() {
    	return names;
    }
    
    
    /**
     * @return
     */
    
    public Element toElement() {
    	Class c = getClass();
        String name = c.getName().substring(c.getName().lastIndexOf(".")+1);
        name = name.toLowerCase();
        return toElement(name);
    }
    public Element toElement(String name) {
        Element ret = new Element(name);
        for(int i=0; i<names.size(); i++) {
        	Object o = values.elementAt(i);
        	if (o!=null) {
        		//System.out.println("  "+o.getClass());
	            if (o instanceof BaseObject) {
	            	ret.addContent(((BaseObject) o).toElement());
	            } else if (o instanceof Vector) {
	            	for (Object ob : (Vector)o) {
	            		//System.out.println("  "+o.getClass()+"::"+ob.getClass());
		            	if (ob instanceof BaseObject) {
		            		ret.addContent(((BaseObject)ob).toElement());
		            	} else if (ob instanceof String[]) {
		            		String key = names.elementAt(i);
			               	String[] value = (String[])ob;
			               	Element e = new Element(key);
			               	for (int j=0;j<value.length-1;j+=2) {
			               		e.setAttribute(value[j*2], value[j*2+1]);
			               	}
			               	e.setText(value[value.length-1]);
			               	ret.addContent(e);
			               	//ret.addContent(key, Arrays.toString(value));
		            	} else {
		            		String key = names.elementAt(i);
			               	String value = ob.toString();
			               	ret.addContent(key, value);
		            	}
	            	}
	            } else {
	            	String key = names.elementAt(i);
	               	String value = o.toString();
	               	ret.addContent(key, value);
		        }
	        }
        }
        
        return ret;
    }
    
    public void createNewObjectFor(String name) {
		if (isSet(name)) {
			String lookForName = "get"+Util.firstLetterUp(name);
			Method[] ml = getClass().getMethods();
			for (Method m : ml) {
				//System.out.println(m.getName() + " -> "+m.getReturnType());
				if (m.getName().equals(lookForName)) {
					try {
						Object o = m.getReturnType().newInstance();
						if (o!=null) {
							set(name, o);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					return;
				}
			}
		}
	}
	
	public void addNewObjectFor(String name) {
		if (isSet(name)) {
			String lookForName = "add"+Util.firstLetterUp(name);
			Method[] ml = getClass().getMethods();
			for (Method m : ml) {
				//System.out.println(m.getName() + " -> "+m.getParameterTypes().toString());
				if (m.getName().equals(lookForName)) {
					try {
						Object o = m.getParameterTypes()[0].newInstance();
						if (o!=null) {
							((Vector)getObject(name)).add(o);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					return;
				}
			}
		}
	}
	protected void addElement(Element e, String name, String newName) {
		Object b = getObject(name);
		if (b!=null) {
			e.addContent(((BaseObject)b).toElement(newName));
		}
	}
	protected void add(Element e, String name) {
		String s = get(name);
		if (s!=null)
			e.addContent(name, s);
	}
	
	protected void addWithAttrib(Element e, String name) {
		String[] value = (String[])getObject(name);
		addWithAttrib(e, name, value);
	}
	protected void addWithAttrib(Element e, String name, String[] value) {
		if (value!=null) {
	       	Element se = new Element(name);
	       	for (int j=0;j<value.length-1;j+=2) {
	       		se.setAttribute(value[j*2], value[j*2+1]);
	       	}
	       	se.setText(value[value.length-1]);
	       	e.addContent(se);
		}
	}
	protected void addDate(Element e, String name) {
		long s = getLong(name);
		e.addContent(name, datemeGMT.format(s));
	}
	protected void add(Element e, String name, String newName) {
		String s = get(name);
		if (s!=null)
			e.addContent(newName, s);
	}
}



