package org.fnppl.opensdx.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.fnppl.opensdx.xml.Element;

public class BusinessLongItem  extends BusinessItem {


	public BusinessLongItem(String name, long longValue) {
		super(name,longValue);
	}
	
	public static BusinessLongItem fromBusinessObject(BusinessObject bo, String name) {
		BusinessStringItem item = bo.handleBusinessStringItem(name);
		if (item==null) {
			return null;
		} else {
			try {
				long l = Long.parseLong(item.getString());
				BusinessLongItem result = new BusinessLongItem(name, l);
				result.addAttributes(item.getAttributes());
				return result;
			} catch (Exception ex) {
				throw new RuntimeException("wrong long fromat: "+item.getString());
			}
		}
	}
	
	public void setLong(long l) {
		super.set(l);
	}
	
	public int getLongValue() {
		Object o = super.get();
		if (o==null) throw new RuntimeException("empty value");
		if (o instanceof Long) {
			return ((Long)o).intValue();	
		} else {
			throw new RuntimeException("wrong type");
		}
	}
	
	public Element toElement() {
		if (get() ==null) return null;
		Element e = new Element(getKeyname(), ""+getLongValue());
		return e;
	}
}