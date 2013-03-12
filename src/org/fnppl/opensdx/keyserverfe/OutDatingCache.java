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

package org.fnppl.opensdx.keyserverfe;

import java.util.*;

public class OutDatingCache {
    public boolean noisy = false;
    public String name ="UNNAMED";
    //TODO optimizing :: wahl hibernation-engine
    //TODO optimizing :: maxfillsize
    //TODO optimizing :: cleanup-behaviour
    
    
    private Hashtable cachico ;//(Object)key -> Object[]{value,puttime,outdatetime}
    private long default_alive = 1000*60*60;//1h
    
    /** Creates a new instance of OutdatingCache */
    public OutDatingCache() {
        cachico = new Hashtable();
    }
    public OutDatingCache(long defaultAliveTime) {
        this();
        default_alive = defaultAliveTime;
    }
    
    
    public static int cleancountmax = 1000;
    public static int cleancount = 0;
    private void put(Object key, Object value, long aliveTime) {
        synchronized(cachico) {
            cleancount++;
            if(cleancount>=cleancountmax) {            
                cleancount = 0;
                
                int r = cleanup();
                if(noisy) {
                    System.err.println(name+"::OutdatingCache::cleanup: "+r);
                    System.err.println(name+"::OutdatingCache::cache-size now: "+cachico.size());
                }                
                
                cleancount = 0;
            }
            
            if(noisy) {
                System.err.println(name+"::OutdatingCache::put: "+key.toString());
            }
            
            cachico.put(key, new Object[]{value, new Long(System.currentTimeMillis()), new Long(aliveTime)});
        }
    }
    
    
    public void put(OutDatingCache.ComposedKey key, Object value) {
        put(key.toString(), value, default_alive);
    }
    public void put(String key, String value) {
        put(key, value, default_alive);
    }
    public void put(String key, Object value) {
        put(key, value, default_alive);
    }
    public void put(Object key, Object value) {
        put(key, value, default_alive);
    }
    
    
    public void remove(Object key) {
        cachico.remove(key);
    }
    public void remove(OutDatingCache.ComposedKey key) {
        cachico.remove(key.toString());
    }
    
    public String getS(String key) {
        return (String)get(key);
    }
    public Object get(OutDatingCache.ComposedKey key) {
        return get(key.toString());
    }
    
    public boolean contains(Object key) {
    	Object o = get(key);
    	if(o!=null) {
    		return true;
    	}
    	return false;
    }
    public Object get(Object key) {
        Object[] o = null;
        synchronized(cachico) {
            o = (Object[])cachico.get(key);
        }
        
        
        if(o==null) {
            if(noisy) {
                System.err.println(name+"::get: "+key.toString()+" NOTHING IN CACHE FOR THAT KEY");
            }
            
            return null;
        }
        else {
             if(noisy) {
                System.err.println(name+"::get: "+key.toString()+" -> CACHED");
            }           
        }
        
        Object value = o[0];
        long puttime = ((Long)o[1]).longValue();
        long ttl = ((Long)o[2]).longValue();
        
        if(puttime+ttl < System.currentTimeMillis()) {
            if(noisy) {
                System.err.println(name+"::get: "+key.toString()+" -> outdated (ttl: "+ttl+" puttime: "+puttime+")");
            }
            
            cachico.remove(key);
            
            return null;
        }
        
        
        return value;
    }
    
    
    private int cleanup() {
        int ret = 0;
        synchronized(cachico) {
            Enumeration en = cachico.keys();
            long l = System.currentTimeMillis();
            
            while(en.hasMoreElements()) {
                Object key = en.nextElement();
                Object[] o = (Object[])cachico.get(key.toString());
                if(o==null) {
                    continue;
                }
                
                Object value = o[0];
                long puttime = ((Long)o[1]).longValue();
                long ttl = ((Long)o[2]).longValue();

                if(puttime+ttl < l) {
                    remove(key);
                    ret++;                    
//                    System.out.println("OutdatingCache::get: "+key.toString()+" -> outdated (ttl: "+ttl+" puttime: "+puttime+")");
                }
            }
        }
            
            return ret;
    }
    
    public static class ComposedKey extends Vector {        
        public ComposedKey() {
            super();
        }
        
        @Override
		public void addElement(Object o) {
            if(o==null) {
                
            }
            else {
                super.addElement(o);
            }
            
        }
        @Override
		public String toString() {
            StringBuffer s = new StringBuffer();
            for(int i=0;i<size();i++) {
                s.append(elementAt(i).toString());
                if(i+1<size()) {
                    s.append("::");
                }
            }
            
            return s.toString();
        }
    }
}
