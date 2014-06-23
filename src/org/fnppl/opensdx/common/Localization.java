package org.fnppl.opensdx.common;

import java.util.Hashtable;

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
	public enum Type{
		RELEASE,
		TRACK,
		ARTIST,
		UNDEFINED
	}
	
	private Type type = Type.UNDEFINED;
	
	/*
	 * Hashtable depends on the Localization.Type
	 * If the type is a Release or Artist, the Key is an Integer (Iso code) and
	 * the value is a String. Otherwise if the type is a Track the key is a 
	 * String (combination of "tracknumber_setnumber") and the value is another
	 * Hashtable with Key: Integer (Iso code) and Value: String.
	 */
	private Hashtable<?, ?> data = null;
	
	public Type getType(){
		return type;
	}
	
	public Hashtable<?, ?> getData(){
		return data;
	}
	
	//TrackEditor, Contributor.TYPE_EDITOR, IDs.make()
	public static Localization make(Hashtable<?, ?> data, Type type){
		Localization ret = new Localization();
		ret.type = type;
		ret.data = data;
		return ret;
	}
	
	@Override
	public String getKeyname() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean equals(Object o){
		if(o instanceof Localization){
			Localization l = (Localization)o;
			return this.type.equals(l.getType());
		}
		return false;
	}
}
