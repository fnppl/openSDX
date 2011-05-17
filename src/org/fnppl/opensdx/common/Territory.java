package org.fnppl.opensdx.common;

public class Territory extends BusinessStringItem {

	public Territory(String territory, boolean allow) {
		super("territory",territory);
		if (allow) {
			setAttribute("type", "allow");
		} else {
			setAttribute("type", "disallow");
		}
	}
	
	public void setAllow(boolean allow) {
		if (allow) {
			setAttribute("type", "allow");
		} else {
			setAttribute("type", "disallow");
		}
	}
	
	public boolean getAllow() {
		String type = getAttribute("type");
		if (type==null || type.equals("allow")) {
			return true;
		}
		return false;
	}

	public String getTerritoryString() {
		return getString();
	}
}
