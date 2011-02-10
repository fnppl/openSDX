package org.fnppl.opensdx.common;

import org.jdom.*;
import java.util.*;

public abstract class BaseObject {
	protected Vector<String> names = new Vector<String>();
	protected Vector<Object> values = new Vector<Object>();
	protected Hashtable<String, Object> changes = null;
	
	public boolean fromDB = false;	
    
    public BaseObject() {
    }
    
    public BaseObject fromElement(Element myObjectElement) {
        return null;
    }
    
    public static BaseObject init(SObject me, String tablename, String idname, long id) {
    	return init(me, tablename, idname, id);
    }

    public static BaseObject init(SObject me, String tablename, String[] idnames, long[] ids) {
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
    	
    	//System.out.println("SQL: "+sb);
    	DBResultSet Rs = BalancingConnectionManager.execQuery(sb.toString());
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
    	int res = BalancingConnectionManager.execUpdate(
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
					sb.append(Helper.dbEncode(value.toString()));
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
			int r = BalancingConnectionManager.execUpdate(sb.toString());
			return r==1;
		}
		return true;
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
    public static void fromDBRAW(SObject mo, DBResultSet Rs, int line) {
    	for(int i=0; i<Rs.width(); i++) {
    		mo.names.addElement(Rs.gimmeNameAt(i));
        	mo.values.addElement(Rs.getValueAt(line, i));
        }
    }
    
    public static void setIds(SObject me, long[] myids, String[] identcols) {
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
    
    
    /**
     * @return
     */
    public Element toElement() {
        Class c = getClass();
        Element ret = new Element(c.getName().substring(c.getName().lastIndexOf(".")+1));
        
        for(int i=0;i<names.size();i++) {
        	String key = names.elementAt(i);
            Element e = new Element(key);
            ret.addContent(e);
            Object o = values.elementAt(i);
            e.setText(o.toString());//NOCH UNSCHÖN HT 18.08.2008
        }
        
        return ret;
    }
}



