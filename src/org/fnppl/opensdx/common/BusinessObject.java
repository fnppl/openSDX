package org.fnppl.opensdx.common;

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

import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;

//import org.fnppl.opensdx.automatisation.BusinessStringItemGenerator;
import org.fnppl.opensdx.xml.Element;
import org.fnppl.opensdx.xml.XMLElementable;
import org.fnppl.opensdx.xml.XMLHelper;


/**
 * 
 * @author Bertram Boedeker <bboedeker@gmx.de>
 * 
 */
public abstract class BusinessObject implements XMLElementable {
	
	private HashMap<String,String> attribs = null;
	private Vector<XMLElementable> otherObjects = new Vector<XMLElementable>();
	private boolean appendOtherObjects = true;
	public boolean showOtherObjectsMessage = true;
	
 	
	public abstract String getKeyname();
	
	
	/***
	 * cool stuff happens here:: this method uses javas reflexion for accessing all XMLElementable fields
	 * coolest stuff:: even private fields can be read out by this!!!
	 */
	
	protected static Hashtable<Class, Field[]> getDeclaredFieldsCache = new Hashtable<Class, Field[]>();
	public Element toElement() {
		System.out.println(getKeyname());
		Element resultElement = new Element(getKeyname());
		if (attribs!=null) {
			for (Entry<String,String> a : attribs.entrySet()) {
				resultElement.setAttribute(a.getKey(), a.getValue());
			}
		}
		
		Field[] fields = getDeclaredFieldsCache.get(this.getClass());
		if(fields == null) {
			fields = this.getClass().getDeclaredFields();
			getDeclaredFieldsCache.put(this.getClass(), fields);
			
			for(int i=0; i<fields.length; i++) {
				fields[i].setAccessible(true);
			}
		}
		
		for (Field f : fields) {
			if (!f.getName().equals("this$0")) { //argg, watch out when directly using BusinessObjects
				try {	
					Object thisFieldsObject = f.get(this);
					if (thisFieldsObject instanceof XMLElementable) {
						Element e = ((XMLElementable)thisFieldsObject).toElement();
						if (e!=null) {
							resultElement.addContent(e);
						}
					}
					else if (thisFieldsObject instanceof Vector<?>) {
						Vector<?> vector = (Vector<?>)thisFieldsObject;
						for (Object vectorsObject : vector) {
							if (vectorsObject instanceof XMLElementable) {
								Element e = ((XMLElementable)vectorsObject).toElement();
								if (e!=null) {
									resultElement.addContent(e);
								}
							}
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		if (appendOtherObjects) {
			for (XMLElementable ue : otherObjects) {
				if (showOtherObjectsMessage) {
					System.out.println("appending other object:: "+getKeyname()+"::"+ue.getKeyname());
					System.out.println(ue.toElement().toString());
				}
				if (!showOtherObjectsMessage && ue instanceof BusinessObject) { //forward appending behavior
					//System.out.println("forwarding");
					BusinessObject tfo = (BusinessObject)ue;
					tfo.appendOtherObjects = this.appendOtherObjects;
					tfo.showOtherObjectsMessage = this.showOtherObjectsMessage;
				}
				resultElement.addContent(ue.toElement());
			}
		} else {
			if (showOtherObjectsMessage) {
				for (XMLElementable ue : otherObjects) {
						System.out.println("unhandled object:: "+getKeyname()+"::"+ue.getKeyname());
				}
			}
		}
		return resultElement;
	}
	
	public void setAttribute(String key, String value) {
		if (attribs==null) attribs = new HashMap<String, String>();
		attribs.put(key, value);
	}
	
	public String getAttribute(String key) {
		if (attribs==null) return null;
		return attribs.get(key);
	}
	
	public Vector<String[]> getAttributes() {
		if (attribs==null) return null;
		Vector<String[]> a = new Vector<String[]>();
		for (String key : attribs.keySet()) {
			a.add(new String[] {key,attribs.get(key)});
		}
		return a;
	}
	
	public Vector<XMLElementable> getElements() {
		Vector<XMLElementable> result = new Vector<XMLElementable>();
		
		Field[] fields = getDeclaredFieldsCache.get(this.getClass());
		if(fields == null) {
			fields = this.getClass().getDeclaredFields();
			getDeclaredFieldsCache.put(this.getClass(), fields);
			
			for(int i=0; i<fields.length; i++) {
				fields[i].setAccessible(true);
			}
		}
		
		for (Field f : fields) {
			if (!f.getName().equals("this$0")) { //argg, watch out when directly using BusinessObjects
				try {	
					//System.out.println(f.getName());
					Object thisFieldsObject = f.get(this);
					if (thisFieldsObject instanceof XMLElementable) {
						result.add((XMLElementable)thisFieldsObject);
					}
					else if (thisFieldsObject instanceof Vector<?>) {
						Vector<?> vector = (Vector<?>)thisFieldsObject;
						for (Object vectorsObject : vector) {
							if (vectorsObject instanceof XMLElementable) {
								result.add((XMLElementable)vectorsObject);
							}
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		if (appendOtherObjects) {
			for (XMLElementable ue : otherObjects) {
				//System.out.println("appending other object:: "+getKeyname()+"::"+ue.getKeyname());
				result.add(ue);
			}
		} else {
			for (XMLElementable ue : otherObjects) {
				//System.out.println("unhandled object:: "+getKeyname()+"::"+ue.getKeyname());
			}
		}
		return result;
	}
	
	//for anonymous BusinessObject -> can be transformed by child classes
	public static BusinessObject fromElement(Element e) {
		if (e==null) return null;
		final String keyname = e.getName();
		BusinessObject b = new BusinessObject() {
			public String getKeyname() {				
				return keyname;
			}
		};
		Vector<String[]> att = e.getAttributes();
		if (att!=null && att.size()>0) {
			b.attribs = new HashMap<String, String>();
			for (String[] s : att) {
				b.attribs.put(s[0],s[1]);
			}
		} else {
			b.attribs = null;
		}
		b.readElements(e);
		return b;
	}

	//to get all elements to BusinessObject logic
	public void readElements(Element e) {
		if (e==null) return;
		Vector<Element> children = e.getChildren();
		for (Element eChild : children) {
			if (eChild.getChildren().size()>0) {
				//baseobject
				otherObjects.add(BusinessObject.fromElement(eChild));
			} else {
				//string item
				otherObjects.add(BusinessStringItem.fromElement(eChild));
			}
		}
	}
	
	public void initFromBusinessObject(BusinessObject bo) {
		this.otherObjects = bo.otherObjects;
	}
	
	public XMLElementable handleObject(String name) {
		for (XMLElementable b : otherObjects) {
			if (b.getKeyname().equals(name)) {
				otherObjects.remove(b);
				return b;
			}
		}
		return null;
	}
	
	public Vector<XMLElementable> handleObjects(String name) {
		Vector<XMLElementable> result = new Vector<XMLElementable>();
		for (XMLElementable b : otherObjects) {
			if (b.getKeyname().equals(name)) {
				result.add(b);
				
			}
		}
		otherObjects.removeAll(result);
		return result;
	}
	
	public BusinessObject handleBusinessObject(final String name) {
		for (XMLElementable b : otherObjects) {
			if (b.getKeyname().equals(name)) {
				if (b instanceof BusinessObject) {
					otherObjects.remove(b);
					return (BusinessObject)b;
				}
				else {
					//check for empty item that was wrongly parsed to StringItem
					if (b instanceof BusinessStringItem) {
						BusinessStringItem bItem = (BusinessStringItem)b;
						if (bItem.getString()==null || bItem.getString().length()==0) {
							otherObjects.remove(b);
							return new BusinessObject() {
								public String getKeyname() {
									return name;
								}
							};
						}
					}
				}
				otherObjects.remove(b);
				return null;
			}
		}
		return null;
	}
	
	public BusinessStringItem handleBusinessStringItem(String name) {
		for (XMLElementable b : otherObjects) {
			if (b.getKeyname().equals(name)) {
				if (b instanceof BusinessStringItem) {
					otherObjects.remove(b);
					return (BusinessStringItem)b;
				}
				return null;
			}
		}
		return null;
	}
	
	public Vector<XMLElementable> getOtherObjects() {
		return otherObjects;
	}
	
	public XMLElementable getOtherObject(String name) {
		for (XMLElementable b : otherObjects) {
			if (b.getKeyname().equals(name)) {
				return b;
			}
		}
		return null;
	}
	
	public BusinessObject getBusinessObject(String name) {
		XMLElementable b = getOtherObject(name);
		if (b==null) return null;
		if (b instanceof BusinessObject) {
			return (BusinessObject)b;
		}
		return null;
	}
	
	public BusinessStringItem getBusinessStringItem(String name) {
		XMLElementable b = getOtherObject(name);
		if (b==null) return null;
		if (b instanceof BusinessStringItem) {
			return (BusinessStringItem)b;
		}
		return null;
	}
	
	public String getStringIfExist(String name) {
		BusinessStringItem bs = getBusinessStringItem(name);
		if (bs==null) return null;
		return bs.getString();
	}
	
	public void removeOtherObjects() {
		otherObjects.removeAllElements();
	}
	
	
	public void setObject(XMLElementable object) {
		String keyname = object.getKeyname();
		for (int p=0;p<otherObjects.size();p++) {
			if (otherObjects.get(p).getKeyname().equals(keyname)) {
				otherObjects.remove(p);
				otherObjects.add(p,object);
				return;
			}
		}
		addObject(object);
	}
	
	public void removeObject(String keyname) {
		for (int p=0;p<otherObjects.size();p++) {
			if (otherObjects.get(p).getKeyname().equals(keyname)) {
				otherObjects.remove(p);
				return;
			}
		}
	}
	
	public void addObject(XMLElementable object) {
		otherObjects.add(object);
	}

	public boolean isAppendOtherObjects() {
		return appendOtherObjects;
	}

	public BusinessObject setAppendOtherObjectToOutput(boolean appendOtherObjects) {
		this.appendOtherObjects = appendOtherObjects;
		return this;
	}
	
	public static boolean getNotNullBoolean(Boolean b, boolean value_when_b_is_null) {
		if(b==null) {
			return value_when_b_is_null;
		}
		return b.booleanValue();
	}
	public static int getNotNullInteger(Integer b, int value_when_b_is_null) {
		if(b==null) {
			return value_when_b_is_null;
		}
		return b.intValue();
	}
}
