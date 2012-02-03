package org.fnppl.opensdx.keyserverfe.server;

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

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import org.fnppl.opensdx.keyserver.helper.SQLStatement;
import org.fnppl.opensdx.keyserverfe.shared.KeyConnection;
import org.fnppl.opensdx.keyserverfe.shared.KeyInfo;
import org.fnppl.opensdx.keyserverfe.shared.User;
import org.fnppl.opensdx.keyserverfe.shared.NodeState;
import org.fnppl.opensdx.security.Identity;
import org.fnppl.opensdx.security.KeyLog;
import org.fnppl.opensdx.security.KeyStatus;
import org.fnppl.opensdx.security.MasterKey;
import org.fnppl.opensdx.security.OSDXKey;
import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.security.Signature;
import org.fnppl.opensdx.security.SubKey;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;

public class DBControl {

	private String DB_DRIVER = "org.postgresql.Driver";
	
	public Connection con;
	private File data_path = null;
	
	private DBControl() {
		con = null;
		try {
			Class.forName(DB_DRIVER);
		} catch (Exception e) {}
	}
	
	private static File[] configFileLocations =   new File[] {
		new File("keyserverfe_config.xml"),
		new File("WEB-INF/keyserverfe_config.xml"),
		new File("webapps/keyserver/WEB-INF/keyserverfe_config.xml")
	};

	private static DBControl instance = null;
	
	public static DBControl getInstance() {
		if (instance == null) {
			//init from config file
			try {
				File configFile = null;
				for (int i=0;i<configFileLocations.length;i++) {
					if (configFileLocations[i].exists()) {
						configFile = configFileLocations[i];
						break;
					} else {
						System.out.println("No config file found at "+configFileLocations[i].getCanonicalPath());
					}
				}				
				if (configFile == null) {
					System.out.println("ERROR, keyserverfe_config.xml not found.");
					return null;
				}
				System.out.println("USING CONFIG FILE :: "+configFile.getCanonicalPath());
				Element root = Document.fromFile(configFile).getRootElement();

				//db
				Element eDB = root.getChild("db");
				if (eDB!=null) {
					try {
						File dp = null;
						String data_path = eDB.getChildText("data_path");
						if (data_path!=null) {
							dp = new File(data_path);
						}
						instance = DBControl.init(eDB.getChildText("user"), eDB.getChildText("password"), eDB.getChildText("name"),dp);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return instance;
	}
	
	public static DBControl init(String user, String pw, String dbname, File data_path) {
		DBControl be = new DBControl();
		if (data_path==null) {
			be.data_path = new File(System.getProperty("user.home"), "db_data");
		} else {
			be.data_path = data_path;
		}
		be.data_path.mkdirs();
		
		System.out.println("DBControl::init::using data_path: "+be.data_path.getAbsolutePath());
		be.connect(user, pw, dbname);
		return be;
	}
	
	private File getFileFromID(long id, String ending) {
		String name = ""+id;
		File result = data_path;
		if (name.length()>5) {
			result = new File(result, name.substring(0,name.length()-5));
		}
		if (ending!=null) name += ending;
		return new File(result, name);
	}
	
	public void connect(String user, String pw, String dbname) {
		try {
			con = DriverManager.getConnection(dbname, user, pw);
			System.out.println("Connection established DB: "+dbname); 
		} catch (Exception e) {
			con = null;
			e.printStackTrace();
			throw new RuntimeException("Connection to DB could not be established.");
		}
	}
	
	public boolean isConnected() {
		if (con==null) {
			return false;
		} else {
			return true;
		}
	}
	
	
	public void closeDBConnection() {
		try {
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//user information
	public User loadUserInformation(String username) {
		//TABLE user_keyids  //user, keyid, x, y, showIn, showOut
		
		//load list of keys
		
		return null;
	}
	
	public void saveUserInformation(String username, Vector<NodeState> states) {
		//data

	}
	
	
	/**
	 * Gets an updated lists of KeyInfo and KeyConncetions which
	 * contains all KeyInfo from given keyids and connected keylogs
	 * and all KeyLogs.
	 * Already present KeyInfos / Logs will not be included.
	 * If username does not equal anonymous the users settings will be saved to the db
	 * 
	 * @param username : "anonymous" or email address
	 * @param states : list of UserNodes -> client state
	 * @param keyids : list of new nodes to add
	 * @param inLogs : add keylogs and keys for incoming logs for each keyid from keyids
	 * @param outLogs : add keylogs and keys for outgoing logs for each keyid from keyids
	 * @return
	 */
	public User updateKeyInfoAndLogs(String username, Vector<NodeState> states, Vector<String> keyids, boolean inLogs, boolean outLogs) { 
		User user = new User(username);
		HashMap<String, NodeState> keyid_state = new HashMap<String, NodeState>(); 
		for (int i=0;i<states.size();i++) {
			NodeState d = states.get(i);
			keyid_state.put(d.getKeyid(), d);
		}
		
		for (int i=0;i<keyids.size();i++) {
			String keyid = keyids.get(i);
			NodeState s = keyid_state.get(keyid);
			if (s==null) {
				s = addNewKeyToUser(getKey(keyid), user, keyid_state, inLogs);
			}
			
			if (s!=null && inLogs && !s.isShowIn()) {
				s.setShowIn(true);
				Vector<KeyLog> keylogs = getKeyLogsToID(keyid);
				if (keylogs==null) {
					keylogs = new Vector<KeyLog>();
				}
				for (KeyLog kl : keylogs) {
					String fromKey = kl.getKeyIDFrom();
					
					//add fromKey if not present
					NodeState sFrom = keyid_state.get(fromKey);
					if (sFrom==null) {
						addNewKeyToUser(getKey(fromKey),user, keyid_state, false);
					}
					
					KeyConnection kc = new KeyConnection(kl.getKeyIDFrom(), kl.getKeyIDTo(), 0, kl.getActionDatetime());
					kc.setType(kl.getAction());
					user.addConnection(kc);
				}
			}
			
			if (s!=null && outLogs && !s.isShowOut()) {
				s.setShowOut(true);
				Vector<KeyLog> keylogs = getKeyLogsFromID(keyid);
				if (keylogs==null) {
					keylogs = new Vector<KeyLog>();
				}
				for (KeyLog kl : keylogs) {
					String toKey = kl.getKeyIDTo();
					
					//add toKey if not present
					NodeState sTo = keyid_state.get(toKey);
					if (sTo==null) {
						addNewKeyToUser(getKey(toKey),user, keyid_state, false);
					}
					//add connection
					KeyConnection kc = new KeyConnection(kl.getKeyIDFrom(), kl.getKeyIDTo(), 0, kl.getActionDatetime());
					kc.setType(kl.getAction());
					user.addConnection(kc);
				}
				
				//add sub- and revokekeys
				Vector<OSDXKey> childKeys = getSubAndRevokeKeysTo(keyid);
				for (OSDXKey key : childKeys) {
					//add key if not present
					NodeState sTo = keyid_state.get(key.getKeyID());
					if (sTo==null) {
						addNewKeyToUser(key, user, keyid_state,false);
					}
					//add connection
					int type  = KeyConnection.TYPE_SUBKEY;
					if (key.isRevoke()) {
						type = KeyConnection.TYPE_REVOKEKEY;
					}
					KeyConnection kc = new KeyConnection(keyid, key.getKeyID(), type, -1L);
					user.addConnection(kc);
				}
			}
		}

	
		if (!user.getName().equals("anonymous")) {
			saveUserInformation(username, states);
		}
		return user;
	}

	private NodeState addNewKeyToUser(OSDXKey key, User user, HashMap<String, NodeState> keyid_state, boolean addInLogs) {
		NodeState s = null;
		if (key!=null) { //key exists
			//get incomig keylogs
			Vector<KeyLog> keylogs = getKeyLogsToID(key.getKeyID());
			if (keylogs==null) {
				keylogs = new Vector<KeyLog>();
			}
			//keystatus
			KeyStatus ks = KeyStatus.getKeyStatus(key, keylogs, null, System.currentTimeMillis(), null);
			
			//build keyinfo
			KeyInfo ki = buildKeyInfo(key, ks);
			user.addKey(ki);					
			s = new NodeState(ki.getId(), -1, -1, false, false, false, false, KeyInfo.VISIBILITY_ALWAYS);
			keyid_state.put(s.getKeyid(), s);
			
			//since we already have the incoming keylogs for the keystatus, we can set the incoming connections here
			if (addInLogs) {  
				s.setShowIn(true);
				ki.setIncomingLogs(true);
				
				for (KeyLog kl : keylogs) {
					String fromKey = kl.getKeyIDFrom();
					//add fromKeyF if not present
					NodeState sFrom = keyid_state.get(fromKey);
					if (sFrom==null) {
						addNewKeyToUser(getKey(fromKey),user, keyid_state, false);
					}
					KeyConnection kc = new KeyConnection(kl.getKeyIDFrom(), kl.getKeyIDTo(), 0, kl.getActionDatetime());
					kc.setType(kl.getAction());
					user.addConnection(kc);
				}
			}
		}
		return s;
	}
	
	
	public Vector<OSDXKey> getSubAndRevokeKeysTo(String keyid) {
		keyid = OSDXKey.getFormattedKeyIDModulusOnly(keyid);
		Vector<OSDXKey> result = new Vector<OSDXKey>();
		try {
			SQLStatement sql = new SQLStatement("SELECT * FROM keys WHERE parentkeysha1=?");
			sql.setString(1, keyid);
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql.toString());
			while (rs.next()) {
				try {
					OSDXKey key = buildKey(rs);
					result.add(key);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			rs.close();
			stmt.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}
	
	
	public boolean hasKey(String keyid) {
		keyid = OSDXKey.getFormattedKeyIDModulusOnly(keyid);
		boolean has = false;
		try {
			SQLStatement sql = new SQLStatement("SELECT keysha1 FROM keys WHERE keysha1=?");
			sql.setString(1, keyid);
			
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql.toString());
			if (rs.next()) {
				has = true;
			}
			rs.close();
			stmt.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return has;
	}
	
	public KeyInfo getKeyInfo(String keyid) {
		String keysha1 = OSDXKey.getFormattedKeyIDModulusOnly(keyid);
		KeyInfo key = null;
		try {
			SQLStatement sql = new SQLStatement("SELECT * FROM keys WHERE keysha1=?");
			sql.setString(1, keysha1);
			
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql.toString());
			
			if (rs.next()) {
				key = buildKeyInfo(rs);
			}
			rs.close();
			stmt.close();
			//con.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return key;
	}
	
	private KeyInfo buildKeyInfo(OSDXKey key, KeyStatus ks) {
		try {
			String id = key.getKeyID();
			String idShort = key.getKeyModulusSHA1();
			if (idShort == null) idShort = "";
			int idLen = idShort.length();
			if (idLen > 5) {
				idShort = idShort.substring(0,5)+"..."+idShort.substring(idLen-5, idLen-1);
			}
			String level = key.getLevelName();
			String usage = key.getUsageName();
			long validFrom = key.getValidFrom();
			long validUntil = key.getValidUntil();
			boolean myKey = false;
			int status = KeyInfo.STATUS_KEY_NOT_FOUND;
			if (ks!=null) {
				status = ks.getValidityStatus();
			}
			boolean directTrust = false;
			
			String owner = "-";
			String mnemonic = "-";
			
			if (key.isMaster()) {
				Identity cid = ((MasterKey)key).getCurrentIdentity();
				if (cid!=null) {
					owner = cid.getEmail();
					if (cid.is_mnemonic_restricted()) {
						mnemonic = "[restricted]";
					} else {
						mnemonic = cid.getMnemonic();
					}
				}
			}
			else if (key.isSub()) {
				//owner = "subkey of "+((SubKey)key).getParentKeyID();
				Identity cid = getLastIdentity(((SubKey)key).getParentKeyID());
				if (cid!=null) {
					owner = cid.getEmail();
					mnemonic = "[subkey]";
				}
			}
			
			KeyInfo ki = new KeyInfo(id, idShort, level, usage, owner, mnemonic, validFrom, validUntil, myKey, status, directTrust);
			return ki;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private KeyInfo buildKeyInfo(ResultSet rs) {
		try {
			String id = rs.getString("keysha1");
			String idShort = "";
			int idLen = id.length();
			if (id!=null && idLen > 5) {
				idShort = id.substring(0,5)+"..."+id.substring(idLen-5+idLen-1);
			}
			id +="@"+rs.getString("keyserver");
			String level = rs.getString("level");
			String usage = rs.getString("usage");
			long validFrom = rs.getTimestamp("valid_from").getTime();
			long validUntil = rs.getTimestamp("valid_until").getTime();
			boolean myKey = false;
			int status = KeyInfo.STATUS_NOT_SET;
			boolean directTrust = false;
			String owner = "";
			String mnemonic = "";
			
			KeyInfo ki = new KeyInfo(id, idShort, level, usage, owner, mnemonic, validFrom, validUntil, myKey, status, directTrust);
			return ki;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public OSDXKey getKey(String keyid) {
		String keysha1 = OSDXKey.getFormattedKeyIDModulusOnly(keyid);
		OSDXKey key = null;
		try {
			SQLStatement sql = new SQLStatement("SELECT * FROM keys WHERE keysha1=?");
			sql.setString(1, keysha1);
			
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql.toString());
			
			if (rs.next()) {
				key = buildKey(rs);
			}
			rs.close();
			stmt.close();
			//con.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return key;
	}
	
	private OSDXKey buildKey(ResultSet rs) {
		try {
			Element pk = new Element("pubkey");
			pk.addContent("keyid", rs.getString("keysha1")+"@"+rs.getString("keyserver"));
			pk.addContent("level", rs.getString("level"));
			pk.addContent("usage", rs.getString("usage"));
			pk.addContent("valid_from", SecurityHelper.getFormattedDate(rs.getTimestamp("valid_from").getTime()));
			pk.addContent("valid_until", SecurityHelper.getFormattedDate(rs.getTimestamp("valid_until").getTime()));
			pk.addContent("algo", rs.getString("algo"));
			pk.addContent("bits", ""+rs.getInt("bits"));
			pk.addContent("modulus", rs.getString("modulus"));
			pk.addContent("exponent", rs.getString("exponent"));
			//Document.buildDocument(pk).output(System.out);
			OSDXKey key = OSDXKey.fromPubKeyElement(pk);
			if (key.isMaster()) {
				Identity idd = getLastIdentity(key.getKeyID());
				if (idd!=null) {
					((MasterKey)key).addIdentity(idd);
				}
			}
			if (key instanceof SubKey) {
				String parentkeysha1 = rs.getString("parentkeysha1");
				if (parentkeysha1!=null && parentkeysha1.length()>0) {
					((SubKey)key).setParentKeyID(parentkeysha1+"@"+rs.getString("parentkeyserver"));
				}
			}
			return key;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Identity getLastIdentity(String keyid) {
		try {
			String keysha1 = OSDXKey.getFormattedKeyIDModulusOnly(keyid);
			SQLStatement sql = new SQLStatement("SELECT * FROM identities WHERE keysha1=? ORDER BY identnum DESC LIMIT 1");
			sql.setString(1, keysha1);
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql.toString());
			Identity id = null;
			if (rs.next()) {
				id = buildIdentitiy(rs);
			}
			rs.close();
			stmt.close();
			//con.close();
			return id;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	private Identity buildIdentitiy(ResultSet rs) {
		try {
			Identity idd = null;
//			SQLStatement sql = new SQLStatement("SELECT * FROM identities WHERE id=?");
//			sql.setLong(1, id);
//			Statement stmt = con.createStatement();
//			ResultSet rs = stmt.executeQuery(sql.toString());
//			if (rs.next()) {
				try {
					idd = Identity.newEmptyIdentity();
					idd.setIdentNum(rs.getInt("identnum"));
					idd.setEmail(rs.getString("email"));
					idd.setMnemonic(rs.getString("mnemonic"));
					idd.set_mnemonic_restricted(rs.getBoolean("mnemonic_r"));
					idd.setCompany(rs.getString("company"));
					idd.set_company_restricted(rs.getBoolean("company_r"));
					idd.setUnit(rs.getString("unit"));
					idd.set_unit_restricted(rs.getBoolean("unit_r"));
					idd.setSubunit(rs.getString("subunit"));
					idd.set_subunit_restricted(rs.getBoolean("subunit_r"));
					idd.setFunction(rs.getString("function"));
					idd.set_function_restricted(rs.getBoolean("function_r"));
					idd.setSurname(rs.getString("surname"));
					idd.set_surname_restricted(rs.getBoolean("surname_r"));
					idd.setMiddlename(rs.getString("middlename"));
					idd.set_middlename_restricted(rs.getBoolean("middlename_r"));
					String bd = rs.getString("birthday");
					if (bd!=null) {
						idd.setBirthday_gmt(bd);
					}
					idd.set_birthday_gmt_restricted(rs.getBoolean("birthday_r"));
					idd.setPlaceofbirth(rs.getString("placeofbirth"));
					idd.set_placeofbirth_restricted(rs.getBoolean("placeofbirth_r"));
					idd.setCity(rs.getString("city"));
					idd.set_city_restricted(rs.getBoolean("city_r"));
					idd.setPostcode(rs.getString("postcode"));
					idd.set_postcode_restricted(rs.getBoolean("postcode_r"));
					idd.setRegion(rs.getString("region"));
					idd.set_region_restricted(rs.getBoolean("region_r"));
					idd.setCountry(rs.getString("country"));
					idd.set_country_restricted(rs.getBoolean("country_r"));
					idd.setPhone(rs.getString("phone"));
					idd.set_phone_restricted(rs.getBoolean("phone_r"));
					idd.setFax(rs.getString("fax"));
					idd.set_fax_restricted(rs.getBoolean("fax_r"));
					idd.setNote(rs.getString("note"));
					idd.set_note_restricted(rs.getBoolean("note_r"));
					long photoId = rs.getLong("photo_id");
					if (photoId!=-1L) {
						File f = getFileFromID(photoId, ".png");
						if (!f.exists()) {	
							throw new RuntimeException("DB DataBackend Error: File "+f.getAbsolutePath()+" does not exist.");
						}
						byte[] calc_md5 = SecurityHelper.getMD5(f);
						byte[] given_md5 = SecurityHelper.HexDecoder.decode(rs.getString("photo_md5"));
						if (!Arrays.equals(calc_md5, given_md5)) {
							throw new RuntimeException("DB DataBackend Error: MD5 Check for file "+f.getAbsolutePath()+" FAILED!");
						}
						idd.setPhoto(f);
					}
					idd.set_photo_restricted(rs.getBoolean("photo_r"));
				} catch (Exception e) {
					e.printStackTrace();
				}
//			}
//			rs.close();
//			stmt.close();
//			//con.close();
			return idd;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	private KeyLog buildKeylog(ResultSet rs) {
		try {
			long id = rs.getLong("keylogid");
			File f = getFileFromID(id, "_keylog.xml");
			if (!f.exists()) {	
				throw new RuntimeException("DB DataBackend Error: File "+f.getAbsolutePath()+" does not exist.");
			}
			byte[] calc_md5 = SecurityHelper.getMD5(f);
			byte[] given_md5 = SecurityHelper.HexDecoder.decode(rs.getString("keylog_md5"));
			if (!Arrays.equals(calc_md5, given_md5)) {
				throw new RuntimeException("DB DataBackend Error: MD5 Check for file "+f.getAbsolutePath()+" FAILED!");
			}
			Element e = Document.fromFile(f).getRootElement();
			KeyLog log = KeyLog.fromElement(e);
			return log;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null; 
	}
	
	 
	public Vector<KeyLog> getKeyLogsToID(String keyid) {
		Vector<KeyLog> logs = new Vector<KeyLog>();
		try {
			String keysha1 = OSDXKey.getFormattedKeyIDModulusOnly(keyid);
			SQLStatement sql = new SQLStatement("SELECT * FROM keylogs WHERE keysha1_to=? ORDER BY asig_datetime");
			sql.setString(1, keysha1);
			System.out.println("SQL: "+sql.toString());
			
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql.toString());
			
			while (rs.next()) {
				try {
					logs.add(buildKeylog(rs));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			rs.close();
			stmt.close();
			//con.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return logs;
	}
	
	public Vector<KeyLog> getKeyLogsFromID(String keyid) {
		Vector<KeyLog> logs = new Vector<KeyLog>();
		try {
			String keysha1 = OSDXKey.getFormattedKeyIDModulusOnly(keyid);
			SQLStatement sql = new SQLStatement("SELECT * FROM keylogs WHERE keysha1_from=? ORDER BY asig_datetime");
			sql.setString(1, keysha1);
			System.out.println("SQL: "+sql.toString());
			
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql.toString());
			
			while (rs.next()) {
				try {
					logs.add(buildKeylog(rs));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			rs.close();
			stmt.close();
			//con.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return logs;
	}

	
	public KeyStatus getKeyStatus(String keyid) {
		return getKeyStatus(keyid, null, System.currentTimeMillis(), null);
	}

	public KeyStatus getKeyStatus(String keyid, String usage, long datetime, String keyidKeyserver) {
		OSDXKey key = getKey(keyid);
		if (key==null) {
			return null;
		}
		Vector<KeyLog> keylogs = getKeyLogsToID(keyid);
		if (keylogs==null) {
			keylogs = new Vector<KeyLog>();
		}
		return KeyStatus.getKeyStatus(key, keylogs, usage, datetime, keyidKeyserver);
	
	}
	 
	public Vector<OSDXKey> getKeysToId(String email) {
		Vector<OSDXKey> keys = new Vector<OSDXKey>();
		try {
			SQLStatement sql = new SQLStatement("SELECT DISTINCT keysha1,keyserver FROM identities WHERE email=?");
			sql.setString(1, email);
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql.toString());
			while (rs.next()) {
				try {
					OSDXKey key = getKey(rs.getString(1));
					keys.add(key);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			rs.close();
			stmt.close();
			//con.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return keys;
	}

	 
	public Vector<OSDXKey> getSubKeysToId(String parentkeyid) {
		Vector<OSDXKey> keys = new Vector<OSDXKey>();
		try {
			String keysha1 = OSDXKey.getFormattedKeyIDModulusOnly(parentkeyid);
			SQLStatement sql = new SQLStatement("SELECT * FROM keys WHERE parentkeysha1=?");
			sql.setString(1, keysha1);
			System.out.println("SQL: "+sql.toString());
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql.toString());
			while (rs.next()) {
				try {
					OSDXKey key = buildKey(rs);
					if (key!=null) keys.add(key);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			rs.close();
			stmt.close();
			//con.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return keys;
	}

	private void initEmptyUserTables() {
		String tUser = 
			"CREATE TABLE \"fe_user\" ("
		+	"		\"email\"			VARCHAR(200) not null,"
		+	"		\"passwordsha1\"	CHAR(60) not null,"
		+	"		\"firstname\"		VARCHAR(200),"
		+	"		\"surname\"			VARCHAR(200),"
		+	"		\"company\"			VARCHAR(200),"
		+	"		PRIMARY KEY(keysha1)"
		+	"	);";
		
		String tNodes = 
			"CREATE TABLE \"fe_user_nodes\" ("
		+	"		\"id\"		    BIGINT not null,"
		+	"		\"email\"		VARCHAR(200),"
		+	"		\"keysha1\"		VARCHAR(200),"
		+	"		\"keyserver\"	VARCHAR(200) not null default 'LOCAL',"
		+	"		\"posx\"		INTEGER not null default -1,"
		+	"		\"posy\"		INTEGER not null default -1,"
		+	"		\"showin\"		BOOLEAN,"
		+	"		\"showout\"		BOOLEAN,"
		+	"		\"mykey\"		BOOLEAN,"
		+	"		\"directtrust\"	BOOLEAN,"
		+	"		\"vislevel\"	INTEGER not null default 4,"
		+	"		PRIMARY KEY(id)"
		+	"	);";
	}
}

