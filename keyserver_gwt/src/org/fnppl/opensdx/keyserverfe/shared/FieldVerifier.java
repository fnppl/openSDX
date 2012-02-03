package org.fnppl.opensdx.keyserverfe.shared;

public class FieldVerifier {

	public static boolean isValidKeyId(String text) {
		//TODO
		if (text == null || text.length()<1) {
			return false;
		}
		return true;
	}
}
