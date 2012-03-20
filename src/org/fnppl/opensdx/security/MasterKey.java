package org.fnppl.opensdx.security;

import java.util.Vector;

import org.fnppl.opensdx.gui.Dialogs;

/*
 * Copyright (C) 2010-2012 
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

public class MasterKey extends OSDXKey {

	protected Vector<Identity> identities = new Vector<Identity>();
	protected Vector<RevokeKey> revokekeys = new Vector<RevokeKey>();
	protected Vector<SubKey> subkeys = new Vector<SubKey>();
	
	protected MasterKey() {
		super();
		super.setLevel(LEVEL_MASTER);
	}
	
	public static MasterKey buildNewMasterKeyfromKeyPair(AsymmetricKeyPair kp) throws Exception {

		MasterKey ret = new MasterKey();
		ret.akp = kp;
		ret.usage = USAGE_SIGN;
		ret.authoritativekeyserver = "LOCAL";
		ret.modulussha1 = SecurityHelper.getSHA1(kp.getPublicModulus());
		ret.datapath = new Vector<DataSourceStep>();
		long now = System.currentTimeMillis();
		now = now - now%1000; //no milliseconds in datetime;
		ret.validFrom = now;
		ret.validUntil = now + 25L*ONE_YEAR;
		ret.datapath.add(new DataSourceStep("LOCAL", now));
		ret.unsavedChanges = true;
		
		return ret;
	}
	
//	public void setAuthoritativeKeyServer(String host, int port) {
//		authoritativekeyserver = host;
//		authoritativekeyserverPort = port;
//		unsavedChanges = true;
//		for (SubKey k : subkeys) {
//			k.authoritativekeyserver = authoritativekeyserver;
//			k.authoritativekeyserverPort = port;
//		}
//		for (RevokeKey k : revokekeys) {
//			k.authoritativekeyserver = authoritativekeyserver;
//			k.authoritativekeyserverPort = port;
//		}
//	}
	
	public void setAuthoritativeKeyServer(String host) {
		authoritativekeyserver = host;
		unsavedChanges = true;
		for (SubKey k : subkeys) {
			k.authoritativekeyserver = authoritativekeyserver;
		}
		for (RevokeKey k : revokekeys) {
			k.authoritativekeyserver = authoritativekeyserver;
		}
	}
	
	public void addSubKey(SubKey k) {
		if (k.isSub()) {
			subkeys.add(k);
			//System.out.println("adding subkey");
			unsavedChanges = true;
		}
	}
	
	public Vector<SubKey> getSubKeys() {
		return subkeys;
	}
	
	public Vector<RevokeKey> getRevokeKeys() {
		return revokekeys;
	}
	
	public void addRevokeKey(RevokeKey k) {
		if (k.isRevoke()) {
			revokekeys.add(k);
			unsavedChanges = true;
		}
	}
	public SubKey buildNewSubKeyfromKeyPair(AsymmetricKeyPair kp) throws Exception {
		SubKey ret = new SubKey();
		ret.akp = kp;
		ret.usage = USAGE_SIGN;
		ret.authoritativekeyserver = authoritativekeyserver;
		ret.modulussha1 = SecurityHelper.getSHA1(kp.getPublicModulus());
		ret.datapath = new Vector<DataSourceStep>();
		long now = System.currentTimeMillis();
		now = now - now%1000; //no milliseconds in datetime;
		ret.validFrom = now;
		ret.validUntil = now + 25L*ONE_YEAR;
		ret.datapath.add(new DataSourceStep("LOCAL", now));
		ret.unsavedChanges = true;
		ret.setParentKey(this);
		this.addSubKey(ret);
		return ret;
	}
	
	public RevokeKey buildNewRevokeKeyfromKeyPair(AsymmetricKeyPair kp) throws Exception {
		RevokeKey ret = new RevokeKey();
		ret.akp = kp;
		ret.usage = USAGE_SIGN;
		ret.authoritativekeyserver = authoritativekeyserver;
		ret.modulussha1 = SecurityHelper.getSHA1(kp.getPublicModulus());
		ret.datapath = new Vector<DataSourceStep>();
		long now = System.currentTimeMillis();
		now = now - now%1000; //no milliseconds in datetime;
		ret.validFrom = now;
		ret.validUntil = now + 25L*ONE_YEAR;
		ret.datapath.add(new DataSourceStep("LOCAL", now));
		ret.unsavedChanges = true;
		ret.setParentKey(this);
		this.addRevokeKey(ret);
		return ret;
	}
	
//	public Result uploadToKeyServer(KeyServerIdentity keyserver, KeyVerificator keyverificator) {
//		if (authoritativekeyserver.equals("LOCAL")) {
//			setAuthoritativeKeyServer(keyserver.getHost());
//		} else {
//			if (!authoritativekeyserver.equals(keyserver.getHost())) {
//				return Result.error("authoritative keyserver does not match given keyserver");
//			}
//		}
//		return uploadToKeyServer(keyverificator);
//	}
	public Result uploadToKeyServer(KeyClient client) {
		if (authoritativekeyserver.equals("LOCAL")) {
			setAuthoritativeKeyServer(client.getHost());
		} else {
			if (!authoritativekeyserver.equals(client.getHost())) {
				return Result.error("authoritative keyserver does not match given keyserver");
			}
		}
		if (!hasPrivateKey()) return Result.error("no private key available");
		if (!isPrivateKeyUnlocked()) return Result.error("private key is locked");
		if (authoritativekeyserver.equals("LOCAL")) return Result.error("authoritative keyserver can not be LOCAL");
		//if (authoritativekeyserverPort<=0) return Result.error("authoritative keyserver port not set");
		Identity id = getCurrentIdentity();
		if (id==null) return Result.error("No Identity found.");
		try {
			boolean ok = client.putMasterKey(this, id);
			if (ok) {
				return Result.succeeded();
			} else {
				return Result.error(client.getMessage());
			}
			
		} catch (Exception ex) {
			return Result.error(ex);
		}
	}
	
	
	public String getIDEmails() {
		if (identities!=null && identities.size()>0) {
			String ids = identities.get(0).getEmail();
			for (int i=1;i<identities.size();i++) {
				ids += ", "+identities.get(i).getEmail();
			}
			return ids;
		}
		return null;
	}
	public String getIDEmailAndMnemonic() {
		if (identities!=null && identities.size()>0) {
			String ids = getCurrentIdentity().getEmail()+" ("+getCurrentIdentity().getMnemonic()+")";
//			String ids = identities.get(0).getEmail()+" ("+identities.get(0).getMnemonic()+")";
//			for (int i=1;i<identities.size();i++) {
//				ids += ", "+identities.get(i).getEmail()+" ("+identities.get(i).getMnemonic()+")";
//			}
			return ids;
		}
		return null;
	}
	
	public void addIdentity(Identity id) {
		unsavedChanges = true;
		identities.add(id);
	}
	
	public Vector<Identity> getIdentities() {
		return identities;
	}
	
	public Identity getCurrentIdentity() {
//		for (Identity id : identities) {
//			if (id.getIdentNum()==1) return id;
//		}
		if (identities == null || identities.size()==0) {
			return null;
		}
		return identities.lastElement();
	}
	
	public void removeIdentity(Identity id) {
		unsavedChanges = true;
		identities.remove(id);
	}
	
	public void moveIdentityAtPositionUp(int oldPosition) {
		if (oldPosition>0 && oldPosition<identities.size()) {
			Identity id = identities.remove(oldPosition);
			identities.add(oldPosition-1, id);
			unsavedChanges = true;
		}
	}
	public void moveIdentityAtPositionDown(int oldPosition) {
		if (oldPosition>=0 && oldPosition<identities.size()-1) {
			Identity id = identities.remove(oldPosition);
			identities.add(oldPosition+1, id);
			unsavedChanges = true;
		}
	}
	
	public boolean hasUnsavedChanges() {
		if (unsavedChanges) return true;
		else {
			for (Identity id : identities) {
				if (id.hasUnsavedChanges()) {
					//System.out.println("unsaved changes in id: "+id.getEmail());
					return true;
				}
			}
		}
		return false;
	}
	
	public void setLevel(int level) {
		throw new RuntimeException("ERROR not allowed to set level for MasterKey");
	}

}
