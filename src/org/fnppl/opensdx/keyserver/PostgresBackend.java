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
import org.fnppl.opensdx.security.Identity;
import org.fnppl.opensdx.security.KeyApprovingStore;
import org.fnppl.opensdx.security.KeyLog;
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
	
	private void addKeysAndLogsFromKeyStore(String filename) {
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
        addKeysAndLogsFromKeyStore("/home/neo/openSDX/keyserver_keystore.xml");
	}
	
	public void connect(String user, String pw, String dbname) {
		try {
			con = DriverManager.getConnection(dbname, user, pw);
			System.out.println("Connection established DB: "+dbname); 
			setupEmptyDB();
			
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
		try {
			PreparedStatement sql = con.prepareStatement("INSERT INTO keys (keyid, level, usage, valid_from, valid_until, algo, bits, modulus, exponent, email, identnum, parentkeyid) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");
			sql.setString(1, key.getKeyID());
			sql.setString(2, key.getLevelName());
			sql.setString(3, key.getUsageName());
			sql.setTimestamp(4, new Timestamp(key.getValidFrom()));
			sql.setTimestamp(5, new Timestamp(key.getValidUntil()));
			sql.setString(6, "RSA");
			sql.setInt(7, key.getPubKey().getBitCount());
			sql.setBytes(8, key.getPublicModulusBytes());
			sql.setBytes(9, key.getPubKey().getPublicExponentBytes());
			if (key.isMaster()) {
				Identity id = ((MasterKey)key).getCurrentIdentity();
				sql.setString(10, id.getEmail());
				sql.setInt(11,id.getIdentNum());
			} else {
				sql.setNull(10, java.sql.Types.VARCHAR);
				sql.setNull(11, java.sql.Types.INTEGER);
			}
			if (key.isSub() && ((SubKey)key).getParentKeyID()!=null) {
				sql.setString(12, ((SubKey)key).getParentKeyID());
			} else {
				sql.setNull(12, java.sql.Types.VARCHAR);
			}
			sql.executeUpdate();
			sql.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public OSDXKey getKey(String keyid) {
		OSDXKey key = null;
		try {
			PreparedStatement sql = con.prepareStatement("SELECT id,level, usage, valid_from, valid_until, algo, bits, modulus,exponent FROM keys WHERE keyid=?");
			sql.setString(1, keyid);
			ResultSet rs = sql.executeQuery();
			if (rs.next()) {
				try {
					int id = rs.getInt(1);
					Element pk = new Element("pubkey");
					pk.addContent("keyid", keyid);
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
					key = OSDXKey.fromPubKeyElement(pk);
					if (key.isMaster()) {
						Identity idd = Identity.newEmptyIdentity();
						idd.setIdentNum(rs.getInt("identnum"));
						idd.setEmail(rs.getString("email"));
						((MasterKey)key).addIdentity(idd);
					}
					if (key.isSub()) {
						((SubKey)key).setParentKeyID(rs.getString("parentkeyid"));
					}
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
		return key;
	}
	
	private OSDXKey getKey(int id) {
		OSDXKey key = null;
		try {
			PreparedStatement sql = con.prepareStatement("SELECT keyid,level, usage, valid_from, valid_until, algo, bits, modulus,exponent FROM keys WHERE id=?");
			sql.setInt(1, id);
			ResultSet rs = sql.executeQuery();
			if (rs.next()) {
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
					key = OSDXKey.fromPubKeyElement(pk);
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
		return key;
	}
	
	public void removeKey(OSDXKey key) {
		try {
			PreparedStatement sql = con.prepareStatement("REMOVE FROM keys WHERE keyid=?");
			sql.setString(1, key.getKeyID());
			sql.executeUpdate();
			sql.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private int getKeyIndex(String keyid) {
		int index = -1;
		try {
			PreparedStatement sql = con.prepareStatement("SELECT id FROM keys WHERE keyid=?");
			sql.setString(1, keyid);
			ResultSet rs = sql.executeQuery();
			if (rs.next()) {
				try {
					index = rs.getInt(1);
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

	 
	public void addKeyLog(KeyLog log) {
		Signature asig = log.getActionSignature();
		Signature sig = log.getSignature();
		
		int keyIndexASig = getKeyIndex(asig.getKey().getKeyID());
		if (keyIndexASig<0) {
			addKey(asig.getKey());
			keyIndexASig = getKeyIndex(asig.getKey().getKeyID());
		}
		int keyIndexSig = getKeyIndex(sig.getKey().getKeyID());
		if (keyIndexSig<0) {
			addKey(sig.getKey());
			keyIndexSig = getKeyIndex(sig.getKey().getKeyID());
		}
		try {
			PreparedStatement sql = con.prepareStatement("INSERT INTO keylogs (ipv4, ipv6, keyid_to, action, action_content, sha256_complete, sha256_restricted," +
					"asig_md5, asig_sha1, asig_sha256, asig_datetime, asig_key, asig_bytes, sha256, sig_md5, sig_sha1, sig_sha256, sig_datetime, sig_key, sig_bytes) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			sql.setString(1, log.getIPv4());
			sql.setString(2, log.getIPv6());
			sql.setString(3, log.getKeyIDTo());
			sql.setString(4, log.getAction());
			sql.setString(5, log.getActionElementString());
			//action signature
			sql.setBytes(6, log.getActionSha256ProofComplete());
			sql.setBytes(7, log.getActionSha256ProofRestricted());
			sql.setBytes(8, asig.getMD5());
			sql.setBytes(9, asig.getSHA1());
			sql.setBytes(10, asig.getSHA256());
			sql.setTimestamp(11, new Timestamp(asig.getSignDatetime()));
			sql.setString(12, asig.getDataName());
			sql.setInt(13, keyIndexASig);
			sql.setBytes(14, asig.getSignatureBytes());
			//signature
			sql.setBytes(15, log.getSHA256LocalProof());
			sql.setBytes(16, sig.getMD5());
			sql.setBytes(17, sig.getSHA1());
			sql.setBytes(18, sig.getSHA256());
			sql.setTimestamp(19, new Timestamp(sig.getSignDatetime()));
			sql.setString(20, sig.getDataName());
			sql.setInt(21, keyIndexSig);
			sql.setBytes(22, sig.getSignatureBytes());
			
			sql.executeUpdate();
			sql.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	
	private int getKeylogIndex(KeyLog log) {
		int index = -1;
		try {
			Signature asig = log.getActionSignature();
			PreparedStatement sql = con.prepareStatement("SELECT id FROM keylogs WHERE keyid_to=? AND asig_bytes=?");
			sql.setString(1, log.getKeyIDTo());
			sql.setBytes(2, asig.getSignatureBytes());
			
			ResultSet rs = sql.executeQuery();
			if (rs.next()) {
				try {
					index = rs.getInt(1);
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
		int klIndex = getKeylogIndex(log);
		if (klIndex==0) {
			addKeyLog(log);
			klIndex = getKeylogIndex(log);
		}
		try {
			PreparedStatement sql = con.prepareStatement("INSERT INTO approval_token (token, keylog) VALUES (?,?)");
			sql.setString(1, token);
			sql.setInt(2, klIndex);
			sql.executeUpdate();
			sql.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public KeyLog getKeyLogFromTokenId(String id) {
		int klIndex = -1;
		KeyLog log = null;
		try {
			PreparedStatement sql = con.prepareStatement("SELECT keylog FROM approval_token WHERE token=?");
			sql.setString(1, id);
			ResultSet rs = sql.executeQuery();
			if (rs.next()) {
				try {
					klIndex = rs.getInt(1);
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
			sql.setInt(1, klIndex);
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
			Element e = new Element("keylog");
			e.addContent("ipv4",rs.getString("ipv4"));
			e.addContent("ipv6",rs.getString("ipv6"));
			Element ea = new Element("keylogaction");
			//action content
			ea.addContent(Document.fromString(rs.getString("action_content")).getRootElement());
			ea.addContent("sha256localproof_complete", SecurityHelper.HexDecoder.encode(rs.getBytes("sha256_complete"),':',-1));
			ea.addContent("sha256localproof_restricted", SecurityHelper.HexDecoder.encode(rs.getBytes("sha256_restricted"),':',-1));
			
			Element asig = new Element("signature");
			Element asigData = new Element("data");
			asigData.addContent("md5", SecurityHelper.HexDecoder.encode(rs.getBytes("asig_md5"),':',-1));
			asigData.addContent("sha1", SecurityHelper.HexDecoder.encode(rs.getBytes("asig_sha1"),':',-1));
			asigData.addContent("sha256", SecurityHelper.HexDecoder.encode(rs.getBytes("asig_sha256"),':',-1));
			asig.addContent(asigData);
			asig.addContent(getKey(rs.getInt("asig_key")).getSimplePubKeyElement());
			asig.addContent("signaturebytes", SecurityHelper.HexDecoder.encode(rs.getBytes("asig_bytes"),'\0',-1));
			ea.addContent(asig);
			e.addContent(ea);
			
			e.addContent("sha256localproof", SecurityHelper.HexDecoder.encode(rs.getBytes("sha256"),':',-1));
			Element sig = new Element("signature");
			Element sigData = new Element("data");
			sigData.addContent("md5", SecurityHelper.HexDecoder.encode(rs.getBytes("sig_md5"),':',-1));
			sigData.addContent("sha1", SecurityHelper.HexDecoder.encode(rs.getBytes("sig_sha1"),':',-1));
			sigData.addContent("sha256", SecurityHelper.HexDecoder.encode(rs.getBytes("sig_sha256"),':',-1));
			sig.addContent(sigData);
			sig.addContent(getKey(rs.getInt("sig_key")).getSimplePubKeyElement());
			sig.addContent("signaturebytes", SecurityHelper.HexDecoder.encode(rs.getBytes("sig_bytes"),'\0',-1));
			e.addContent(asig);
			
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
			PreparedStatement sql = con.prepareStatement("SELECT * FROM keylogs WHERE keyid_to=?");
			sql.setString(1, keyid);
			ResultSet rs = sql.executeQuery();
			
			while (rs.next()) {
				try {
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
		// TODO Auto-generated method stub
		return null;
	}

	 
	public Vector<OSDXKey> getKeysToId(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	 
	public Vector<OSDXKey> getSubKeysToId(String id) {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		
	}

}
