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
public class SubKey extends OSDXKey {

	protected MasterKey parentKey = null;
	protected String parentkeyid = null;//could be the parentkey is not loaded - then *only* the id is present

	protected SubKey() {
		super();
		super.setLevel(LEVEL_SUB);
	}
	
	//public Result uploadToKeyServer(KeyVerificator keyverificator) {
	public Result uploadToKeyServer(KeyClient client) {
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
			//System.out.println("Before SubKey.putSubkey...");
			boolean ok = client.putSubKey(this, parentKey);
			//System.out.println("AFTER SubKey.putSubkey -> "+ok);
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
	
	public String getParentKeyID() {
		if (parentKey!=null) return parentKey.getKeyID();
		else return parentkeyid;
	}
	
	public void setParentKey(MasterKey parent) {
		unsavedChanges = true;
		parentKey = parent;
		parentkeyid = parent.getKeyID();
		authoritativekeyserver = parent.authoritativekeyserver;
		//authoritativekeyserverPort = parent.authoritativekeyserverPort;
	}
	
	public MasterKey getParentKey() {
		return parentKey;
	}
	
	public void setLevel(int level) {
		if (this instanceof RevokeKey && isSub()) {
			super.setLevel(LEVEL_REVOKE);
		} else {
			throw new RuntimeException("ERROR not allowed to set level for SubKey");
		}
	}
	
	public void setParentKeyID(String id) {
		unsavedChanges = true;
		parentkeyid = id;
		parentKey = null;
	}
	
}
