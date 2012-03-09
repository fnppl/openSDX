package org.fnppl.opensdx.keyserver;

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
import java.io.File;
import java.util.HashMap;
import java.util.Vector;

import org.fnppl.opensdx.gui.DefaultMessageHandler;
import org.fnppl.opensdx.gui.MessageHandler;
import org.fnppl.opensdx.security.Identity;
import org.fnppl.opensdx.security.KeyApprovingStore;
import org.fnppl.opensdx.security.KeyLog;
import org.fnppl.opensdx.security.KeyStatus;
import org.fnppl.opensdx.security.MasterKey;
import org.fnppl.opensdx.security.OSDXKey;
import org.fnppl.opensdx.security.SubKey;

public class KeyStoreFileBackend implements KeyServerBackend {

	private HashMap<String, Vector<OSDXKey>> id_keys = null;
	private HashMap<String, OSDXKey> keyid_key = null;
	private HashMap<String, Vector<KeyLog>> keyid_log = null;
	private HashMap<String, Vector<OSDXKey>> keyid_subkeys = null;
	private HashMap<String, KeyLog> openTokens = null;
	private KeyApprovingStore keystore;
	
	private MasterKey keyServerSigningKey = null;
	private MessageHandler messageHandler = new DefaultMessageHandler() {
		public boolean requestOverwriteFile(File file) {//dont ask, just overwrite
			return true;
		}
		public boolean requestIgnoreKeyLogVerificationFailure() {//dont ignore faild keylog verification
			return false;
		}
		public MasterKey requestMasterSigningKey(KeyApprovingStore keystore) throws Exception {
			return keyServerSigningKey;
		}
	};
	private KeyStoreFileBackend() {
		id_keys = new HashMap<String, Vector<OSDXKey>>();
		keyid_key = new HashMap<String, OSDXKey>();
		keyid_log = new HashMap<String, Vector<KeyLog>>();
		keyid_subkeys = new HashMap<String, Vector<OSDXKey>>();
		openTokens = new HashMap<String, KeyLog>();
	}
	
	public static KeyStoreFileBackend init(OSDXKey keyServerSigningKey) {
		KeyStoreFileBackend be = new KeyStoreFileBackend();
		
		be.openDefaultKeyStore();
		be.keystore.setSigningKey(keyServerSigningKey);
		be.keyServerSigningKey = (MasterKey)keyServerSigningKey;
		be.updateCache(keyServerSigningKey, null);
		
		return be;
	}
	
	public OSDXKey getKey(String keyid) {
		return keyid_key.get(keyid);
	}
	
	public Vector<OSDXKey> getKeysToId(String id) {
		return id_keys.get(id);
	}
	
	public Vector<OSDXKey> getSubKeysToId(String id) {
		return keyid_subkeys.get(id);
	}
	
	public Vector<KeyLog> getKeyLogsToID(String keyid) {
		return keyid_log.get(keyid);
	}
	
	public KeyLog getKeyLogFromTokenId(String id) {
		return openTokens.get(id);
	}
	
	public void addOpenToken(String token, KeyLog log) {
		openTokens.put(token, log);
	}
	
	public void removeOpenToken(String token) {
		openTokens.remove(token);
	}
	
	public KeyStatus getKeyStatus(String keyid) {
		return getKeyStatus(keyid, null, System.currentTimeMillis(), null);
	}

	public KeyStatus getKeyStatus(String keyid, String usage, long datetime, String keyidKeyserver) {
		try {
			return keystore.getKeyStatus(keyid, usage, datetime, keyidKeyserver);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void addKey(OSDXKey key) {
		keystore.addKey(key);
		saveKeyStore();
	}
	
	public void addKeyLog(KeyLog keylog) {
		keystore.addKeyLog(keylog);
		saveKeyStore();
	}
	
	public void removeKey(OSDXKey key){
		keystore.removeKey(key);
		saveKeyStore();
	}
	
	public void updateCache(OSDXKey k, KeyLog l) {
		if (k!=null) {
			keyid_key.put(k.getKeyModulusSHA1(), k);
			System.out.println("adding keyid_key: "+k.getKeyModulusSHA1()+"::OSDXKey");
			if (k instanceof MasterKey) {
				Vector<Identity> ids = ((MasterKey)k).getIdentities();
				if (ids != null) {
					//use only currentIdentity for searching for known keys to email
					Identity id = ((MasterKey)k).getCurrentIdentity();
					if (!id_keys.containsKey(id.getEmail())) {
						id_keys.put(id.getEmail(),
								new Vector<OSDXKey>());
					}
					Vector<OSDXKey> listId = id_keys.get(id.getEmail());
					if (!listId.contains(k)) {
						listId.add(k);
					}
					System.out.println("adding id_keys: "+id.getEmail()+"::"+k.getKeyModulusSHA1());
						
//					for (Identity id : ids) {
//						if (!id_keys.containsKey(id.getEmail())) {
//							id_keys.put(id.getEmail(),
//									new Vector<OSDXKey>());
//						}
//						id_keys.get(id.getEmail()).add(k);
//						System.out.println("adding id_keys: "+id.getEmail()+"::"+k.getKeyModulusSHA1());
//					}
				}
			}
			if (k instanceof SubKey) {
				String parentKeyID = ((SubKey)k).getParentKeyID();
				if (parentKeyID!=null && parentKeyID.length()>0) {
					parentKeyID = OSDXKey.getFormattedKeyIDModulusOnly(parentKeyID);
					if (!keyid_subkeys.containsKey(parentKeyID)) {
						keyid_subkeys.put(parentKeyID, new Vector<OSDXKey>());
					}
					keyid_subkeys.get(parentKeyID).add(k);
					System.out.println("adding subkey: "+k.getKeyModulusSHA1()+" for parent key: "+parentKeyID);
				}
			}
		}
		if (l!=null) {
			//String keyid = l.getKeyIDTo();
			String keyid = OSDXKey.getFormattedKeyIDModulusOnly(l.getKeyIDTo());
			if (!keyid_log.containsKey(keyid)) {
				keyid_log.put(keyid, new Vector<KeyLog>());
			}
			keyid_log.get(keyid).add(l);
		}
		//saveKeyStore();
	}
	
	public boolean openDefaultKeyStore() {
		File f = KeyServerMain.getDefaultDir();
		f = new File(f, "keyserver_keystore.xml");
		if (f.exists()) {
			try {
				keystore = KeyApprovingStore.fromFile(f, messageHandler);
				Vector<OSDXKey> keys = keystore.getAllKeys();
				if (keys != null) {
					for (OSDXKey k : keys) {
						updateCache(k, null);
					}
				}
				Vector<KeyLog> keylogs = keystore.getKeyLogs();
				if (keylogs != null) {
					for (KeyLog l : keylogs) {
						updateCache(null, l);
					}
				}
				return true;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			try {
				keystore = KeyApprovingStore.createNewKeyApprovingStore(f, messageHandler);
				saveKeyStore();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return false;
	}
	
	private void saveKeyStore() {
		try {
			keystore.toFile(keystore.getFile());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
