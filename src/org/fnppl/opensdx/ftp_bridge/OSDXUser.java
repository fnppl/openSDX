package org.fnppl.opensdx.ftp_bridge;

import org.fnppl.opensdx.security.OSDXKey;

public class OSDXUser {
	
	public String host = null;
	public int port = 4221;
	public String prepath = "/";
	public String username = null;
	public OSDXKey signingKey = null;
}
