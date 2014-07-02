package org.fnppl.opensdx.common;

import java.util.Vector;

import org.fnppl.opensdx.xml.Element;
import org.fnppl.opensdx.xml.XMLElementable;

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

/**
 * This class contains necessary informations about Localization
 * 
 * @author ajovanovic
 */
public class Localization extends BusinessObject{
	public static String KEY_NAME = "localization";
	
	public enum Type{
		NAME,
		DISPLAYNAME,
		DISPLAYARTISTNAME // Special case for Displayartistnames on Bundle/Item level
	}
	
	private Type type = null;
	private BusinessObject iamlocalizating = null; // Reference on the localized Object
	private String lang = null; //should be a generic object "Lang" in the future...
	private String value = null;
	
	public Type getType(){
		return type;
	}
	public String getValue(){
		return value;
	}
	public String getLang(){
		return lang;
	}
	public BusinessObject getLocalizating(){
		return iamlocalizating;
	}
	
	public static String typeToString(Type type){
		switch(type){
			case DISPLAYARTISTNAME:
				return "display_artistname";
			case DISPLAYNAME:
				return "displayname";
			case NAME:
				return "name";
			default:
				return null;
		}
	}
	
	public static Type stringToType(String type){
		return Type.valueOf(type.toUpperCase());
	}
	
	//TrackEditor, Contributor.TYPE_EDITOR, IDs.make()
	public static Localization make(BusinessObject iamlocalizating, Type type, String lang, String value){
		Localization ret = new Localization();
		ret.type = type;
		ret.value = value;
		ret.iamlocalizating = iamlocalizating;
		ret.lang = lang;
		return ret;
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof Localization){
			Localization l = (Localization)o;
			return this.type.equals(l.getType()) && value.equals(l.getValue()) && lang.equals(l.getLang());
		}
		return o.equals(this);
	}

	@Override
	public String getKeyname() { 
		return KEY_NAME;
//		return "localization_" + iamlocalizating.getKeyname() +"_"+ type +"_"+ lang;
	}
	
	@Override
	public Element toElement(){
		Element ret = new Element(typeToString(type));
		ret.setText(value);
		ret.setAttribute("lang", lang.toLowerCase());
		return ret;
	}

	public static BusinessCollection<Localization> fromBusinessObject(BusinessObject bo, BusinessObject iamlocalizating) {
		if (bo==null) return null;
		if (!bo.getKeyname().equals(KEY_NAME)) {
			bo = bo.handleBusinessObject(KEY_NAME);
		}
		if (bo==null) return null;
		BusinessCollection<Localization> ret = new BusinessCollection<Localization>() {
			public String getKeyname() {
				return KEY_NAME;
			}
		};
		Vector<XMLElementable> elems = bo.getOtherObjects();
		while(elems.size() > 0){
			BusinessStringItem elem = BusinessStringItem.fromBusinessObject(bo, elems.elementAt(0).getKeyname());
			Type type = stringToType(elem.getKeyname());
			String lang = elem.getAttribute("lang");
			String value = elem.getString();
			ret.add(Localization.make(iamlocalizating, type, lang, value));
		}
		
		return ret;
	}
}
