package org.fnppl.opensdx.common;

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
import org.jdom.*;
import java.util.*;

public abstract class DBObject extends BaseObject {
	public boolean fromDB = false;	
    
    public DBObject() {
    }
    
    
    public static DBObject init(DBObject me, String tablename, String idname, long id) {
    	return init(me, tablename, idname, id);
    }

    public static DBObject init(DBObject me, String tablename, String[] idnames, long[] ids) {
    	if(idnames.length != ids.length || tablename==null || tablename.length()==0) {    		
    		throw new RuntimeException("SObject::init_failed::mismatch "+idnames.length+" != "+ids.length+" for table["+tablename+"]");
    	}    	
    	StringBuffer sb = new StringBuffer();    	
    	sb.append("select * from \""+tablename+"\" where ");
    	
    	for(int i=0;i<idnames.length;i++) {
    		if(i>0) {
    			sb.append(" and ");
    		}
    		sb.append("\""+idnames[i]+"\" = "+ids[i]);
    	}
    	

    	DBResultSet Rs = DBConnManager.execQuery(sb.toString());
		fromDBRAW(me, Rs, 0);
		if(Rs.height()>=1) {
			me.fromDB = true;
		}
		else {
			me.fromDB = false;
		}
		
		return me;
    }
    
    /**
     * @param tablename
     * @param identcol
     * @param identvalue
     * @return
     */
    public boolean delete(String tablename, String identcol, long identvalue) {
    	return delete(tablename, new String[]{identcol}, new long[]{identvalue});
    }
    
    /**
     * @param tablename
     * @param identcols
     * @param identvalues
     * @return
     */
    public boolean delete(String tablename, String[] identcols, long[] identvalues) {
    	StringBuffer sql = new StringBuffer();
    	if(identcols.length != identvalues.length) {
    		throw new RuntimeException("SObject :: Fail::"+identcols.length+" mismatch "+identvalues.length);
    	}
    	
    	sql.append("delete from \""+tablename+ "\" where ");
    	for(int i=0; i<identcols.length; i++) {
    		if(i>0) {
    			sql.append(" and ");
    		}
    		sql.append("\""+identcols[i]+"\" = "+identvalues[i]);
    	}    	
    	//System.out.println("SQL: "+sql);
    	int res = DBConnManager.execUpdate(
			sql.toString()
		);
    	
    	return res == 1;
    }
    
//    public abstract boolean save() ;
    
    /**
     * @param tablename
     * @param identcols
     * @param identvalues
     * @return
     */
    public boolean save(String tablename, String[] identcols, long[] identvalues) {	
		if(changes!=null && changes.size()>0) {
			StringBuffer sb = new StringBuffer("update \""+tablename+"\" set ");
			
			java.util.Enumeration en = changes.keys();
			while(en.hasMoreElements()) {
				String key = (String)en.nextElement();
				Object value = changes.get(key);
				
				sb.append("\""+key+"\"=");
				
				if(value instanceof Long) {
					sb.append(((Long)value).longValue());
				}
				else if(value instanceof Integer) {
					sb.append(((Integer)value).intValue());
				}
				else if(value instanceof Double) {
					sb.append(((Double)value).doubleValue());
				}
				else {
					sb.append("'");
					sb.append(DBConnManager.dbEncode(value.toString()));
					sb.append("'");
				}
				
				if(en.hasMoreElements()) {
					sb.append(", ");
				}
			}
			
			sb.append(" where ");			
	    	for(int i=0; i<identcols.length; i++) {
	    		if(i>0) {
	    			sb.append(" and ");
	    		}
	    		sb.append("\""+identcols[i]+"\" = "+identvalues[i]);
	    	}
			//System.out.println("SQL: "+sb);
			int r = DBConnManager.execUpdate(sb.toString());
			return r==1;
		}
		return true;
	}
    
    /**
     * @param Rs
     */
    public void fromDBRAW(DBResultSet Rs) {
    	fromDBRAW(Rs, 0);
    }
    /**
     * @param mo
     * @param Rs
     * @param line
     */
    public static void fromDBRAW(DBObject mo, DBResultSet Rs, int line) {
    	for(int i=0; i<Rs.width(); i++) {
    		mo.names.addElement(Rs.getNameAt(i));
        	mo.values.addElement(Rs.getSValueAt(line, i));
        }
    }
    
    public static void setIds(DBObject me, long[] myids, String[] identcols) {
		if (myids.length != identcols.length) {
			return;
		}
		for (int i=0; i < myids.length; i++ ) {
			myids[i] = me.getLong(identcols[i]);
		}
	}
    
    /**
     * @param Rs
     * @param line
     */
    public void fromDBRAW(DBResultSet Rs, int line) {
        fromDBRAW(this, Rs, line);
    }
}



