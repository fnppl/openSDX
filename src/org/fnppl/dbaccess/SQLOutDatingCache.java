package org.fnppl.dbaccess;

import java.util.*;

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


public class SQLOutDatingCache {
    //TODO optimizing :: wahl hibernation-engine
    //TODO optimizing :: maxfillsize
    //TODO optimizing :: cleanup-behaviour
    
    
    private Hashtable<ComposedKey, Object[]> cachico = new Hashtable<ComposedKey, Object[]>();//(Object)key -> Object[]{value,puttime,outdatetime}
    private long default_alive = 1000*60*30;//30 min.
    public static int putcount = 0;
    public final static int putcountTH = 1024; 
    
    /** Creates a new instance of OutdatingCache */
    public SQLOutDatingCache() {}
    public SQLOutDatingCache(long defaultAliveTime) {
        this();
        default_alive = defaultAliveTime;
    }
    
    private int cleanup() {
        putcount=0;
        int ret = 0;
        
        //cleanup wird nur aus einem synchronized context aufgerufen!
//        synchronized(cachico) {
        	Enumeration<ComposedKey> en = cachico.keys();
        	long l = System.currentTimeMillis();

        	while(en.hasMoreElements()) {
        		ComposedKey key = en.nextElement();
        		Object[] o = (Object[])cachico.get(key.toString());
        		if(o==null) {
        			continue;
        		}
        		DBResultSet value = (DBResultSet)o[0];
        		long puttime = ((Long)o[1]).longValue();
        		long ttl = ((Long)o[2]).longValue();

        		if(puttime+ttl < l) {
        			remove(key);
        			ret++;
        			//                    System.out.println("OutdatingCache::get: "+key.toString()+" -> outdated (ttl: "+ttl+" puttime: "+puttime+")");
        		}
        	}
//        }

        return ret;
    }
    
    
    
    private void put(ComposedKey key, DBResultSet value, long aliveTime) {
//        System.out.println("OutdatingCache::put: "+key.toString()+" -> "+value.toString());
        
        synchronized(cachico) {
            putcount++;
            
            if(putcount >= putcountTH) {
                cleanup();
            }
            
            cachico.put(
                key, 
                new Object[]{
                        value, 
                        new Long(System.currentTimeMillis()), 
                        new Long(aliveTime)
                }
            );
        }
    }
    
    
    public void put(SQLOutDatingCache.ComposedKey key, DBResultSet value) {
//        System.out.println("OutdatingCache::put "+key.toString()+" -> "+value.toString());
        
        put(key, value, default_alive);
    }
    
//    public void put(String key, String value) {
//        put(key, value, default_alive);
//    }
//    public void put(String key, DBResultSet value) {
//    	SQLOutDatingCache.ComposedKey keyC = new ComposedKey();
//    	keyC.addElement(key);
//    	
//        put(keyC, value, default_alive);
//    }
    
    
    public void remove(ComposedKey key) {
        cachico.remove(key);
    }
    
//    public String getS(String key) {
//        return (String)get(key);
//    }
    public DBResultSet get(ComposedKey key) {
//        System.out.println("OutdatingCache::get: "+key.toString());
        
        synchronized(cachico) {
            putcount++;
            
            if(putcount >= putcountTH) {
                cleanup();
            }
            
            Object[] o = (Object[])cachico.get(key.toString());

            if(o==null) {
                return null;
            }

            DBResultSet value = (DBResultSet)o[0];
            long puttime = ((Long)o[1]).longValue();
            long ttl = ((Long)o[2]).longValue();

            if(puttime+ttl < System.currentTimeMillis()) {
                remove(key);
                return null;
            }            
            return value;
        }
    }
    
    
    
    public static class ComposedKey extends Vector {
    	private String cs = null;
        public ComposedKey() {
            super();
        }
        
        public static ComposedKey createNew(String key) {
        	ComposedKey ret = new ComposedKey();
        	ret.addElement(key);
        	return ret;
        }
        
        public ComposedKey addKeyElement(Object o) {
        	addElement(o);
        	return this;
        }
        
        @Override
		public void addElement(Object o) {
            if(o==null) {
                
            }
            else {
                super.addElement(o);
                cs = null;
            }
        }
        
        @Override
		public String toString() {
        	if(cs != null) {
        		return cs;
        	}
            StringBuffer s = new StringBuffer();
            for(int i=0;i<size();i++) {
                s.append(elementAt(i).toString());
                if(i+1<size()) {
                    s.append("::");
                }
            }
            cs = s.toString();
            return cs;
        }
        
        public int hashCode() {
        	if(cs == null) {
        		toString(); //setzt cs implizit
        	}
        	return cs.hashCode();
        }
    }
}



