package org.fnppl.opensdx.keyserver;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Vector;

import org.fnppl.opensdx.gui.DefaultMessageHandler;
import org.fnppl.opensdx.keyserver.helper.IdGenerator;
import org.fnppl.opensdx.security.Identity;
import org.fnppl.opensdx.security.KeyApprovingStore;
import org.fnppl.opensdx.security.KeyLog;
import org.fnppl.opensdx.security.KeyLogAction;
import org.fnppl.opensdx.security.KeyStatus;
import org.fnppl.opensdx.security.MasterKey;
import org.fnppl.opensdx.security.OSDXKey;
import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.security.Signature;
import org.fnppl.opensdx.security.SubKey;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;

public class PostgresBackend implements KeyServerBackend {

	private String DB_DRIVER = "org.postgresql.Driver";
	
	public Connection con;

	private PostgresBackend() {
		con = null;
		//Load DB_Driver
		try {
			Class.forName(DB_DRIVER);
		} catch (Exception e) {}
	}
	
	public static PostgresBackend init(String user, String pw, String dbname) {
		PostgresBackend be = new PostgresBackend();
		be.connect(user, pw, dbname);
		return be;
	}
	
	public void addKeysAndLogsFromKeyStore(String filename) {
		try {
			File f = new File(filename);
			KeyApprovingStore store = KeyApprovingStore.fromFile(f, new DefaultMessageHandler());
			Vector<OSDXKey> keys = store.getAllKeys();
			for (OSDXKey key : keys) {
				addKey(key);
			}
//			Vector<KeyLog> logs =store.getKeyLogs();
//			for (KeyLog log : logs) {
//				addKeyLog(log);
//			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public boolean hasKey(String keyid) {
		boolean has = false;
		try {
			PreparedStatement sql = con.prepareStatement("SELECT keyid FROM keys WHERE keyid=?");
			sql.setString(1, keyid);
			ResultSet rs = sql.executeQuery();
			if (rs.next()) {
				has = true;
			}
			rs.close();
			sql.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return has;
	}
	
	public void setupEmptyDB() {
		URL emptyDB = PostgresBackend.class.getResource("resources/setupEmptyDB.txt");
		try {
			Statement stmt = con.createStatement();
            BufferedReader in = new BufferedReader(new InputStreamReader(emptyDB.openStream()));
            String line = null;
            String nextCommand = "";
            while ((line = in.readLine())!=null) {
            	int trenn = line.indexOf(";;");
            	if (trenn>=0) {
            		nextCommand += line.substring(0,trenn);
            		//executeCommand
            		if (nextCommand.length()>3) {
    					try {
    					System.out.println("SQL::"+nextCommand);
    					stmt.execute(nextCommand);
    					} catch (SQLException innerEx) {
    						innerEx.printStackTrace();
    					}		
    				}
            		nextCommand = line.substring(trenn+2);
            	} else {
            		nextCommand += line;
            	}
            }
            //execute last command
			if (nextCommand.length()>3) {
				try {
				System.out.println("SQL::"+nextCommand);
				stmt.execute(nextCommand);
				} catch (SQLException innerEx) {
					innerEx.printStackTrace();
				}		
			}
            in.close();
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
	}
	
	public void connect(String user, String pw, String dbname) {
		try {
			con = DriverManager.getConnection(dbname, user, pw);
			System.out.println("Connection established DB: "+dbname); 
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Connection to DB could not be established.");
		}
	}
	
	public void closeDBConnection() {
		try {
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void addKey(OSDXKey key) {
		if (hasKey(key.getKeyID())) return;
		
		try {
			PreparedStatement sql = con.prepareStatement("INSERT INTO keys (keyid, level, usage, valid_from, valid_until, algo, bits, modulus, exponent, parentkeyid) VALUES (?,?,?,?,?,?,?,?,?,?)");
			sql.setString(1, key.getKeyID());
			sql.setString(2, key.getLevelName());
			sql.setString(3, key.getUsageName());
			sql.setTimestamp(4, new Timestamp(key.getValidFrom()));
			sql.setTimestamp(5, new Timestamp(key.getValidUntil()));
			sql.setString(6, "RSA");
			sql.setInt(7, key.getPubKey().getBitCount());
			sql.setBytes(8, key.getPublicModulusBytes());
			sql.setBytes(9, key.getPubKey().getPublicExponentBytes());	
			if (key.isSub() && ((SubKey)key).getParentKeyID()!=null) {
				sql.setString(10, ((SubKey)key).getParentKeyID());
			} else {
				sql.setNull(10, java.sql.Types.VARCHAR); //no parent keyid found	
			}
			sql.executeUpdate();
			sql.close();
			
			//add identities for masterkey
			if (key.isMaster()) {
				Vector<Identity> ids = ((MasterKey)key).getIdentities();
				if (ids!=null) {
					for (Identity id : ids) {
						addIdentity(id, key.getKeyID());
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public OSDXKey getKey(String keyid) {
		OSDXKey key = null;
		try {
			PreparedStatement sql = con.prepareStatement("SELECT * FROM keys WHERE keyid LIKE ?");
			sql.setString(1, keyid+"%");
			//Statement sql = con.createStatement();
			ResultSet rs = sql.executeQuery();//"SELECT * FROM keys WHERE keyid LIKE '"+keyid+"%'");
			if (rs.next()) {
				key = buildKey(rs);
			}
			rs.close();
			sql.close();
			//con.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return key;
	}
	
	private OSDXKey buildKey(ResultSet rs) {
		try {
			Element pk = new Element("pubkey");
			pk.addContent("keyid", rs.getString(1));
			pk.addContent("level", rs.getString(2));
			pk.addContent("usage", rs.getString(3));
			pk.addContent("valid_from", SecurityHelper.getFormattedDate(rs.getTimestamp(4).getTime()));
			pk.addContent("valid_until", SecurityHelper.getFormattedDate(rs.getTimestamp(5).getTime()));
			pk.addContent("algo", rs.getString(6));
			pk.addContent("bits", ""+rs.getInt(7));
			byte[] mod = rs.getBytes(8);
			byte[] exp = rs.getBytes(9);
			pk.addContent("modulus", SecurityHelper.HexDecoder.encode(mod, '\0',-1));
			pk.addContent("exponent", "0x"+SecurityHelper.HexDecoder.encode(exp, '\0',-1));
			//Document.buildDocument(pk).output(System.out);
			OSDXKey key = OSDXKey.fromPubKeyElement(pk);
			if (key.isMaster()) {
				Identity idd = null; //TODO getLastIdentity(keyid);
				if (idd!=null) {
					((MasterKey)key).addIdentity(idd);
				}
			}
			if (key.isSub()) {
				((SubKey)key).setParentKeyID(rs.getString("parentkeyid"));
			}
			return key;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public long addIdentity(Identity id) {
		return addIdentity(id, null);
	}

	public long addIdentity(Identity id, String keyid) {
		long idid = IdGenerator.getTimestamp();
		try {
			PreparedStatement sql = con.prepareStatement("INSERT INTO identities (id, identnum, email, mnemonic, mnemonic_r, company, company_r, unit, unit_r, subunit, subunit_r, function, function_r, surname, surname_r, firstname, firstname_r, middlename, middlename_r, birthday, birthday_r, placeofbirth, placeofbirth_r, city, city_r, postcode, postcode_r, region, region_r, country, country_r, phone, phone_r, fax, fax_r, note, note_r, photo, photo_r) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			sql.setLong(1, idid);
			sql.setInt(2, id.getIdentNum());
			sql.setString(3, id.getEmail());
			sql.setString(4, id.getMnemonic());
			sql.setBoolean(5, id.is_mnemonic_restricted());
			sql.setString(6, id.getCompany());
			sql.setBoolean(7, id.is_company_restricted());
			sql.setString(8, id.getUnit());
			sql.setBoolean(9, id.is_unit_restricted());
			sql.setString(10, id.getSubunit());
			sql.setBoolean(11, id.is_subunit_restricted());
			sql.setString(12, id.getFunction());
			sql.setBoolean(13, id.is_function_restricted());
			sql.setString(14, id.getSurname());
			sql.setBoolean(15, id.is_surname_restricted());
			sql.setString(16, id.getFirstNames());
			sql.setBoolean(17, id.is_firstname_s_restricted());
			sql.setString(18, id.getMiddlename());
			sql.setBoolean(19, id.is_middlename_restricted());
			sql.setString(20, id.getBirthdayGMTString());
			sql.setBoolean(21, id.is_birthday_gmt_restricted());
			sql.setString(22, id.getPlaceOfBirth());
			sql.setBoolean(23, id.is_placeofbirth_restricted());
			sql.setString(24, id.getCity());
			sql.setBoolean(25, id.is_city_restricted());
			sql.setString(26, id.getPostcode());
			sql.setBoolean(27, id.is_postcode_restricted());
			sql.setString(28, id.getRegion());
			sql.setBoolean(29, id.is_region_restricted());
			sql.setString(30, id.getCountry());
			sql.setBoolean(31, id.is_country_restricted());
			sql.setString(32, id.getPhone());
			sql.setBoolean(33, id.is_phone_restricted());
			sql.setString(34, id.getFax());
			sql.setBoolean(35, id.is_fax_restricted());
			sql.setString(36, id.getNote());
			sql.setBoolean(37, id.is_note_restricted());
			sql.setBytes(38, id.getPhotoBytes());
			sql.setBoolean(39, id.is_photo_restricted());
			
			sql.executeUpdate();
			sql.close();
			
			if (keyid!=null) {
				long kiid = IdGenerator.getTimestamp();
				sql = con.prepareStatement("INSERT INTO key_identity (id, keyid, identity) VALUES (?,?,?)");
				sql.setLong(1, kiid);
				sql.setString(2, keyid);
				sql.setLong(3, idid);
				sql.executeUpdate();
				sql.close();
				
			}
			return idid;
		} catch (Exception ex) {
			ex.printStackTrace();
		}	
		return -1L;
	}
	
	public Identity getIdentitiy(long id) {
		try {
			Identity idd = null;
			PreparedStatement sql = con.prepareStatement("SELECT * FROM identities WHERE id=?");
			sql.setLong(1, id);
			
			ResultSet rs = sql.executeQuery();
			if (rs.next()) {
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
					byte[] pb = rs.getBytes("photo");
					if (pb!=null) {
						idd.setPhotoBytes(pb);
					}
					idd.set_photo_restricted(rs.getBoolean("photo_r"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			rs.close();
			sql.close();
			//con.close();
			return idd;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	
	public void removeKey(OSDXKey key) {
		try {
			//TODO remove Identitites to key
			PreparedStatement sql = con.prepareStatement("REMOVE FROM keys WHERE keyid=?");
			sql.setString(1, key.getKeyID());
			sql.executeUpdate();
			sql.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void addKeyLog(KeyLog log) {
		Signature asig = log.getActionSignature();
		Signature sig = log.getSignature();
		
		addKey(asig.getKey());
		addKey(sig.getKey());
		
		try {
			long ts = IdGenerator.getTimestamp();
			
			PreparedStatement sql = con.prepareStatement("INSERT INTO keylogs (id, ipv4, ipv6, keyid_to, action, action_id, action_msg, " +
					"sha256_complete, sha256_restricted, asig_md5, asig_sha1, asig_sha256, asig_datetime, asig_dataname, asig_keyid, asig_bytes," +
												 "sha256, sig_md5,  sig_sha1,  sig_sha256,  sig_datetime,  sig_dataname,  sig_keyid,  sig_bytes)" +
					" VALUES (?,?,?,?,?,? ,?,?,?,?,?,? ,?,?,?,?,?,? ,?,?,?,?,?,?)");
			sql.setLong(1, ts);
			sql.setString(2, log.getIPv4());
			sql.setString(3, log.getIPv6());
			sql.setString(4, log.getKeyIDTo());
			sql.setString(5, log.getAction());
			Identity idd = log.getIdentity();
			if (idd!=null) {
				long idid = addIdentity(idd);
				sql.setLong(6, idid);
			}
			sql.setString(7, log.getMessage());
			
			//action signature
			sql.setBytes(8, log.getActionSha256ProofComplete());
			sql.setBytes(9, log.getActionSha256ProofRestricted());
			sql.setBytes(10, asig.getMD5());
			sql.setBytes(11, asig.getSHA1());
			sql.setBytes(12, asig.getSHA256());
			sql.setTimestamp(13, new Timestamp(asig.getSignDatetime()));
			sql.setString(14, asig.getDataName());
			sql.setString(15, asig.getKey().getKeyID());
			sql.setBytes(16, asig.getSignatureBytes());
			
			//signature
			sql.setBytes(17, log.getSHA256LocalProof());
			sql.setBytes(18, sig.getMD5());
			sql.setBytes(19, sig.getSHA1());
			sql.setBytes(20, sig.getSHA256());
			sql.setTimestamp(21, new Timestamp(sig.getSignDatetime()));
			sql.setString(22, sig.getDataName());
			sql.setString(23, sig.getKey().getKeyID());
			sql.setBytes(24, sig.getSignatureBytes());
			
			sql.executeUpdate();
			sql.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	
	private long getKeylogIndex(KeyLog log) {
		long index = -1L;
		try {
			Signature asig = log.getActionSignature();
			PreparedStatement sql = con.prepareStatement("SELECT id FROM keylogs WHERE keyid_to=? AND asig_bytes=?");
			sql.setString(1, log.getKeyIDTo());
			sql.setBytes(2, asig.getSignatureBytes());
			
			ResultSet rs = sql.executeQuery();
			if (rs.next()) {
				try {
					index = rs.getLong(1);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			rs.close();
			sql.close();
			//con.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return index;
	}
	
	public void addOpenToken(String token, KeyLog log) {
		long klIndex = getKeylogIndex(log);
		if (klIndex==0) {
			addKeyLog(log);
			klIndex = getKeylogIndex(log);
		}
		try {
			PreparedStatement sql = con.prepareStatement("INSERT INTO approval_token (token, keylog) VALUES (?,?)");
			sql.setString(1, token);
			sql.setLong(2, klIndex);
			sql.executeUpdate();
			sql.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public KeyLog getKeyLogFromTokenId(String id) {
		long klIndex = -1;
		KeyLog log = null;
		try {
			PreparedStatement sql = con.prepareStatement("SELECT keylog FROM approval_token WHERE token=?");
			sql.setString(1, id);
			ResultSet rs = sql.executeQuery();
			if (rs.next()) {
				try {
					klIndex = rs.getLong(1);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			rs.close();
			sql.close();
			//con.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (klIndex<0) {
			return null;
		}
		try {
			PreparedStatement sql = con.prepareStatement("SELECT * FROM keylogs WHERE id=?");
			sql.setLong(1, klIndex);
			ResultSet rs = sql.executeQuery();
			if (rs.next()) {
				try {
					log = buildKeylog(rs);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			rs.close();
			sql.close();
			//con.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return log;
	}
	
	private KeyLog buildKeylog(ResultSet rs) {
		try {
			OSDXKey asigKey = getKey(rs.getString("asig_keyid"));
			Element e = new Element("keylog");
			e.addContent("ipv4",rs.getString("ipv4"));
			e.addContent("ipv6",rs.getString("ipv6"));
			
			Element ea = new Element("keylogaction");
			//action content
			ea.addContent("from_keyid", asigKey.getKeyID());
			ea.addContent("to_keyid", rs.getString("keyid_to"));
			Element eaa = new Element(rs.getString("action"));
			Identity idd = getIdentitiy(rs.getLong("action_id"));
			if (idd!=null) {
				eaa.addContent(idd.toElement(true));
			}
			String msg = rs.getString("action_msg");
			if (msg!=null) {
				eaa.addContent("message", msg);
			}
			ea.addContent(eaa);
			ea.addContent("sha256localproof_complete", SecurityHelper.HexDecoder.encode(rs.getBytes("sha256_complete"),':',-1));
			ea.addContent("sha256localproof_restricted", SecurityHelper.HexDecoder.encode(rs.getBytes("sha256_restricted"),':',-1));
			
			Element asig = new Element("signature");
			Element asigData = new Element("data");
			asigData.addContent("md5", SecurityHelper.HexDecoder.encode(rs.getBytes("asig_md5"),':',-1));
			asigData.addContent("sha1", SecurityHelper.HexDecoder.encode(rs.getBytes("asig_sha1"),':',-1));
			asigData.addContent("sha256", SecurityHelper.HexDecoder.encode(rs.getBytes("asig_sha256"),':',-1));
			asigData.addContent("signdatetime", SecurityHelper.getFormattedDate(rs.getTimestamp("asig_datetime").getTime()));
			asigData.addContent("dataname", rs.getString("asig_dataname"));
			
			asig.addContent(asigData);
			asig.addContent(asigKey.getSimplePubKeyElement());
			asig.addContent("signaturebytes", SecurityHelper.HexDecoder.encode(rs.getBytes("asig_bytes"),'\0',-1));
			
			ea.addContent(asig);
			
			e.addContent(ea);
			
			e.addContent("sha256localproof", SecurityHelper.HexDecoder.encode(rs.getBytes("sha256"),':',-1));
			Element sig = new Element("signature");
			Element sigData = new Element("data");
			sigData.addContent("md5", SecurityHelper.HexDecoder.encode(rs.getBytes("sig_md5"),':',-1));
			sigData.addContent("sha1", SecurityHelper.HexDecoder.encode(rs.getBytes("sig_sha1"),':',-1));
			sigData.addContent("sha256", SecurityHelper.HexDecoder.encode(rs.getBytes("sig_sha256"),':',-1));
			sigData.addContent("signdatetime", SecurityHelper.getFormattedDate(rs.getTimestamp("sig_datetime").getTime()));
			sigData.addContent("dataname", rs.getString("sig_dataname"));
			sig.addContent(sigData);
			sig.addContent(getKey(rs.getString("sig_keyid")).getSimplePubKeyElement());
			sig.addContent("signaturebytes", SecurityHelper.HexDecoder.encode(rs.getBytes("sig_bytes"),'\0',-1));
			e.addContent(sig);
			Document.buildDocument(e).output(System.out);
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
			System.out.println("get keylogs");
			PreparedStatement sql = con.prepareStatement("SELECT * FROM keylogs WHERE keyid_to LIKE ? ORDER BY asig_datetime");
			sql.setString(1, keyid+"%");
			System.out.println("SQL: "+sql.toString());
			ResultSet rs = sql.executeQuery();
			
			while (rs.next()) {
				try {
					System.out.println("found from: "+rs.getString("asig_keyid"));
					logs.add(buildKeylog(rs));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			rs.close();
			sql.close();
			//con.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return logs;
	}

	 
	public KeyStatus getKeyStatus(String keyid) {
		Vector<KeyLog> kls = getKeyLogsToID(keyid);
		if (kls==null || kls.size()==0) return null;
		for (KeyLog kl : kls) {
			System.out.println("found keylog... "+kl.getActionDatetimeString());
		}
		KeyLog kl = kls.lastElement();
		String status = kl.getAction();
		int validity = -1;
		if (status.equals(KeyLogAction.APPROVAL)) {
			validity =  KeyStatus.STATUS_VALID;
		}
		else if (status.equals(KeyLogAction.DISAPPROVAL)) {
			validity =  KeyStatus.STATUS_UNAPPROVED;
		}
		else if (status.equals(KeyLogAction.APPROVAL_PENDING)) {
			validity =  KeyStatus.STATUS_UNAPPROVED;
		}
		else if (status.equals(KeyLogAction.REVOCATION)) {
			validity =  KeyStatus.STATUS_REVOKED;
		}
		
		int approvalPoints = 100;
		OSDXKey key = getKey(keyid);
		long datetimeValidFrom = key.getValidFrom();
		long datetimeValidUntil = key.getValidUntil();
		
		KeyStatus ks = new KeyStatus(validity, approvalPoints, datetimeValidFrom, datetimeValidUntil, kl);
		return ks;
	
	}

	 
	public Vector<OSDXKey> getKeysToId(String email) {
		Vector<OSDXKey> keys = new Vector<OSDXKey>();
		try {
			PreparedStatement sql = con.prepareStatement("SELECT DISTINCT keyid FROM identities, key_identity WHERE identities.email=?");
			sql.setString(1, email);
			ResultSet rs = sql.executeQuery();
			while (rs.next()) {
				try {
					OSDXKey key = getKey(rs.getString(1));
					keys.add(key);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			rs.close();
			sql.close();
			//con.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return keys;
	}

	 
	public Vector<OSDXKey> getSubKeysToId(String parentkeyid) {
		Vector<OSDXKey> keys = new Vector<OSDXKey>();
		try {
			PreparedStatement sql = con.prepareStatement("SELECT * FROM keys WHERE parentkeyid LIKE ?");
			sql.setString(1, parentkeyid+"%");
			System.out.println("SQL: "+sql.toString());
			ResultSet rs = sql.executeQuery();
			while (rs.next()) {
				try {
					OSDXKey key = buildKey(rs);
					if (key!=null) keys.add(key);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			rs.close();
			sql.close();
			//con.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return keys;
	} 

	 
	public void removeOpenToken(String token) {
		try {
			PreparedStatement sql = con.prepareStatement("REMOVE FROM approval_token WHERE token=?");
			sql.setString(1, token);
			sql.executeUpdate();
			sql.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	 
	public void updateCache(OSDXKey k, KeyLog l) {
		// do nothing
	}

}

