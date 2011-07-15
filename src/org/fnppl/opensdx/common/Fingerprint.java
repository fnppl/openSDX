package org.fnppl.opensdx.common;

public class Fingerprint extends BusinessObject {

	public static String KEY_NAME = "fingerprint";

	private BusinessStringItem echoprint;				//COULD


	public static Fingerprint make() {
		Fingerprint fingerprint = new Fingerprint();
		fingerprint.echoprint = null;
		return fingerprint;
	}


	public static Fingerprint fromBusinessObject(BusinessObject bo) {
		if (bo==null) return null;
		if (!bo.getKeyname().equals(KEY_NAME)) {
			bo = bo.handleBusinessObject(KEY_NAME);
		}
		if (bo==null) return null;
		Fingerprint fingerprint = new Fingerprint();
		fingerprint.initFromBusinessObject(bo);
		
		fingerprint.echoprint = BusinessStringItem.fromBusinessObject(fingerprint, "echoprint");
		
		return fingerprint;
	}


	public Fingerprint echoprint(String echoprint) {
		if (echoprint==null) {
			this.echoprint=null;
		} else {
			this.echoprint = new BusinessStringItem("echoprint", echoprint);
		}
		return this;
	}

	public String getEchoprint() {
		if (echoprint==null) return null;
		return echoprint.getString();
	}

	public String getKeyname() {
		return KEY_NAME;
	}
}

