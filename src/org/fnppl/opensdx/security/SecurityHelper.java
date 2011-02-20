package org.fnppl.opensdx.security;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class SecurityHelper {
	public static void ensureBC() {
		if(Security.getProvider("BC")==null) {
			Security.addProvider(new BouncyCastleProvider());
		}
	}
}
