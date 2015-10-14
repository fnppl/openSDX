package org.fnppl.opensdx.security;
/*
 * Copyright (C) 2010-2015 
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

import java.util.Vector;
import org.fnppl.opensdx.xml.Element;

public class KeyServerIdentity {

	private String prepath = "";
	private String host = null;
	private int port = 80;
	
	private Vector<OSDXKey> knownkeys = new Vector<OSDXKey>();
	
	private KeyServerIdentity() {
		
	}
	
	public static KeyServerIdentity make(String host, int port, String prepath) {
		KeyServerIdentity k = new KeyServerIdentity();
		k.host = host;
		k.port = port;
		k.prepath = prepath;
		if (prepath == null || prepath.equals("/")) {
			k.prepath = "";
		}
		return k;
	}
	
	public static KeyServerIdentity fromElement(Element e) throws Exception {
		KeyServerIdentity k = new KeyServerIdentity();
		k.host = e.getChildText("host");
		k.prepath = e.getChildText("prepath");
		if(k.prepath == null) {
			k.prepath = "";
		}
		
		try {
			k.port = Integer.parseInt(e.getChildText("port"));
		} catch (Exception ex) {
			System.out.println("KeyServerIdentity: Wrong Port Format!");
			k.port = 80;
		}
		
		if (e.getChild("knownkeys")!=null) {
			Vector<Element> keys = e.getChild("knownkeys").getChildren("pubkey");
			for (Element key: keys) {
				k.knownkeys.add(OSDXKey.fromPubKeyElement(key));
			}
		}
		return k;
	}
	
	public Element toElement() throws Exception {
		Element e = new Element("keyserver");
		e.addContent("host", host);
		e.addContent("port", ""+port);
		e.addContent("prepath", ""+prepath);
		
		Element kk = new Element("knownkeys");
		e.addContent(kk);
		for (OSDXKey k : knownkeys) {
			kk.addContent(k.getSimplePubKeyElement());
		}
		return e;
	}

	public String getHost() {
		return host;
	}


	public void setHost(String host) {
		this.host = host;
	}
	
	public void setPrepath(String prepath) {
		this.prepath = prepath;
	}

	public String getPrepath() {
		return prepath;
	}

	public int getPort() {
		return port;
	}


	public void setPort(int port) {
		this.port = port;
	}

	public Vector<OSDXKey> getKnownKeys() {
		return knownkeys;
	}
	
	public boolean hasKnownKey(String keyid) {
		for (OSDXKey k : knownkeys) {
			if (k.getKeyID().equals(keyid)) {
				return true;
			}
		}
		return false;
	}

	public void addKnownKey(OSDXKey knownkey) {
		knownkeys.add(knownkey);
	}
	
	public void removeKnownKey(OSDXKey knownkey) {
		knownkeys.remove(knownkey);
	}
	
}
