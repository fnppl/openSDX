package org.fnppl.opensdx.common;

public class NameValuePair extends BusinessObject {

	public String key_name = "pair";

	private BusinessStringItem name;				//COULD
	private BusinessStringItem value;				//COULD


	public static NameValuePair make(String name, String value, String KEY_NAME) {
		NameValuePair pair = new NameValuePair();
		pair.key_name = KEY_NAME;
		pair.name = new BusinessStringItem("name", name);
		pair.value = new BusinessStringItem("value", value);
		return pair;
	}




	public static NameValuePair make() {
		NameValuePair pair = new NameValuePair();
		pair.name = null;
		pair.value = null;
		return pair;
	}


	public static NameValuePair fromBusinessObject(BusinessObject bo, String KEY_NAME) {
		if (bo==null) return null;
		if (!bo.getKeyname().equals(KEY_NAME)) {
			bo = bo.handleBusinessObject(KEY_NAME);
		}
		if (bo==null) return null;
		
		NameValuePair pair = new NameValuePair();
		pair.key_name = KEY_NAME;
		pair.initFromBusinessObject(bo);
		
		pair.name = BusinessStringItem.fromBusinessObject(bo, "name");
		pair.value = BusinessStringItem.fromBusinessObject(bo, "value");
		
		return pair;
	}


	public NameValuePair name(String name) {
		this.name = new BusinessStringItem("name", name);
		return this;
	}

	public NameValuePair value(String value) {
		this.value = new BusinessStringItem("value", value);
		return this;
	}




	public String getName() {
		if (name==null) return null;
		return name.getString();
	}

	public String getValue() {
		if (value==null) return null;
		return value.getString();
	}
	
	public String setKeyname() {
		return key_name;
	}
	
	public String getKeyname() {
		return key_name;
	}
}



