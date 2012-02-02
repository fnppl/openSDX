package org.fnppl.opensdx.security;


/*
 * Copyright (C) 2010-2011 
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
public class RevokeKey extends SubKey {
	
	protected RevokeKey() {
		super();
		super.setLevel(LEVEL_REVOKE);
	}
	
	public Result uploadToKeyServer(KeyClient client) {
//		if (!hasPrivateKey()) {
//			return Result.error("no private key available");
//		}
//		if (!isPrivateKeyUnlocked()) {
//			return Result.error("private key is locked");
//		}
//		if (authoritativekeyserver.equals("LOCAL")) {
//			return Result.error("authoritative keyserver can not be LOCAL");
//		}
//		//if (authoritativekeyserverPort<=0) return Result.error("authoritative keyserver port not set");
//		if (parentKey==null) return Result.error("missing parent key");
//		try {
//			KeyClient client =  new KeyClient(authoritativekeyserver,
//					80,
//					//KeyClient.OSDX_KEYSERVER_DEFAULT_PORT, 
//					"", keyverificator);
//			boolean ok = client.putRevokeKey(this, parentKey);
//			if (ok) {
//				return Result.succeeded();
//			} else {
//				return Result.error(client.getMessage());
//			}
//		} catch (Exception ex) {
//			return Result.error(ex);
//		}
//		
		
		if (!hasPrivateKey()) {
			System.out.println("uploadToKeyServer::!hasprivatekey");
			return Result.error("no private key available");
		}
		if (!isPrivateKeyUnlocked()) {
			System.out.println("uploadToKeyServer::!privatekeyunlocked");
			return Result.error("private key is locked");
		}
		if (authoritativekeyserver.equals("LOCAL")) {
			System.out.println("uploadToKeyServer::authoritativekeyserver==local");
			return Result.error("authoritative keyserver can not be LOCAL");
		}
		if (client == null) {
			System.out.println("uploadToKeyServer::client==null");
			return Result.error("keyserver not set.");
		}
		if (!client.getHost().equalsIgnoreCase(authoritativekeyserver)) {
			System.out.println("uploadToKeyServer::client.host != authoritativekeyserver");
			return Result.error("keyserver not authoritative.");
		}
		//if (authoritativekeyserverPort<=0) return Result.error("authoritative keyserver port not set");
		if (parentKey==null) {
			System.out.println("uploadToKeyServer::parentkey==null");
			return Result.error("missing parent key");
		}
		try {
			//KeyClient client =  new KeyClient(authoritativekeyserver, KeyClient.OSDX_KEYSERVER_DEFAULT_PORT, "", keyverificator);
//			KeyClient client =  new KeyClient(
//					authoritativekeyserver,
//					80, //TODO HT 2011-06-26 check me!!!
//					//KeyClient.OSDX_KEYSERVER_DEFAULT_PORT, 
//					"", 
//					keyverificator
//				);
			//System.out.println("Before RevokeKey.putSubkey...");
			boolean ok = client.putRevokeKey(this, parentKey);
			//System.out.println("AFTER RevokeKey.putSubkey -> "+ok);
			if (ok) {
				return Result.succeeded();
			} else {
				return Result.error(client.getMessage());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return Result.error(ex);
		}
	}
	
	public void setLevel(int level) {
		throw new RuntimeException("ERROR not allowed to set level for RevokeKey");
	}
}
