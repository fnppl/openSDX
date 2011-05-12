package org.fnppl.opensdx.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.fnppl.opensdx.xml.Element;

public class BusinessIntegerItem  extends BusinessItem {


	public BusinessIntegerItem(String name, int integer) {
		super(name,integer);
	}
	
	public static BusinessIntegerItem fromBusinessObject(BusinessObject bo, String name) {
		BusinessStringItem item = bo.handleBusinessStringItem(name);
		if (item==null) {
			return null;
		} else {
			try {
				int l = Integer.parseInt(item.getString());
				BusinessIntegerItem result = new BusinessIntegerItem(name, l);
				result.addAttributes(item.getAttributes());
				return result;
			} catch (Exception ex) {
				throw new RuntimeException("wrong integer fromat: "+item.getString());
			}
		}
	}
	
	public void setInteger(int integer) {
		super.set(integer);
	}
	
	public int getIntValue() {
		Object o = super.get();
		if (o==null) throw new RuntimeException("empty value");
		if (o instanceof Integer) {
			return ((Integer)o).intValue();	
		} else {
			throw new RuntimeException("wrong type");
		}
	}
	
	public Element toElement() {
		if (get() ==null) return null;
		Element e = new Element(getKeyname(), ""+getIntValue());
		return e;
	}
}