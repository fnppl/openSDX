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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;
import java.util.WeakHashMap;

import org.fnppl.opensdx.xml.Document;


/**
 * Provides methods for the verification of keys by resolving a "chain-of-trust"
 * to given keys trusted by the user
 *
 * @author Bertram Boedeker <boedeker@it-is-awesome.de>
 */
public class KeyVerificator {
	
	private static WeakHashMap<Long,KeyVerificator> instances = null;
	
	private static KeyVerificator defInstance = null;
	
	
	private long instanceDatetime = 0;
	private static TrustGraph trustGraph = new TrustGraph(); 
	private static Vector<OSDXKey> checkInProgress = new Vector<OSDXKey>();
	
	private void KeyVerificator() {
		instanceDatetime = System.currentTimeMillis();
		
	}
	
	public static KeyVerificator getDefaultInstance() {
		if (defInstance == null) {
			defInstance = new KeyVerificator();
		}
		else if(System.currentTimeMillis() - defInstance.instanceDatetime >= 1000*60*60*2) {
			//create new defInstance;
			defInstance = new KeyVerificator();
		}
		return defInstance;
	}
	
	public static KeyVerificator getInstance(long stamp) {
		long mstamp = stamp / (60*30*1000);//30-minuten-normalisiert...
		KeyVerificator k = instances.get(mstamp);
		if(k == null) {
			//create
			
			instances.put(mstamp, k);
		}
		return k;
	}
	
	
	
	public static void addRatedKey(OSDXKey key, int rating) {
		trustGraph.addKeyRating(key, rating);
	}
	
	public static void removeDirectRating(OSDXKey key) {
		trustGraph.removeDirectRating(key);
	}
	
	
	public static Result verifyKey(OSDXKey key) {
		//sha1 of key modulus = keyid
		byte[] keyid = SecurityHelper.HexDecoder.decode(OSDXKey.getFormattedKeyIDModulusOnly(key.getKeyID()));
		if (!Arrays.equals(keyid, SecurityHelper.getSHA1(key.getPublicModulusBytes()))) {
			return Result.error("keyid dos not match sha1 of key modulus");
		}
		//already trusted key
		if (isTrustedKey(key.getKeyID())) return Result.succeeded();
		
		//find a chain of trust by breath-first-search of keylogs
		return findChainOfTrustTo(key);
	}
	
	public static boolean isTrustedKey(String keyid) {
		int rating = trustGraph.getTrustRating(keyid);
		if (rating>=TrustRatingOfKey.RATING_MARGINAL) return true;
		return false;
	}
	
	public static TrustGraph getTrustGraph() {
		return trustGraph;
	}
	public static Vector<KeyLog> requestKeyLogs(OSDXKey key) {
		try {
			KeyClient client = new KeyClient(key.getAuthoritativekeyserver(),KeyClient.OSDX_KEYSERVER_DEFAULT_PORT);
			Vector<KeyLog> result = client.requestKeyLogs(key.getKeyID());
			if (client.getMessage()!=null) {
				System.out.println("request Keylogs: Message: "+client.getMessage());
			}
			// verify
			Vector<KeyLog> verified = new Vector<KeyLog>();
			if (result!=null) {
				for (KeyLog keylog : result) {
					try {
						Result verifyKeylog = keylog.verify(); //internal check
						if (verifyKeylog.succeeded)  {
							verified.add(keylog);
						} else {
							System.out.println("  found keylog from "+keylog.getKeyIDFrom()+" <- FORGET cause NOT verified: "+verifyKeylog.errorMessage);
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				return verified;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public static MasterKey requestParentKey(SubKey sub) {
		try {
			KeyClient client = new KeyClient(sub.getAuthoritativekeyserver(),KeyClient.OSDX_KEYSERVER_DEFAULT_PORT);
			MasterKey parent = client.requestMasterPubKey(sub.getKeyID());
			if (client.getMessage()!=null) {
				System.out.println("request parentkey: Message: "+client.getMessage());
			}
			return parent;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public static Vector<Identity> requestIdentity(MasterKey key) {
		try {
			KeyClient client = new KeyClient(key.getAuthoritativekeyserver(),KeyClient.OSDX_KEYSERVER_DEFAULT_PORT);
			Vector<Identity> ids = client.requestIdentities(key.getKeyID());
			if (client.getMessage()!=null) {
				System.out.println("request parentkey: Message: "+client.getMessage());
			}
			return ids;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	
//	private static void testGraphTraverseWithKeys() {
//		try {
//			boolean buildKeys = false;
//			boolean uploadKeys = true;
//			boolean uploadKeyLogs = true;
//
//			String host = "localhost";
//			int port = 8889;
//			
//			
//			KeyClient client = new KeyClient(host, port);
//			client.connect();
//			KeyServerIdentity ksid = client.requestKeyServerIdentity();
//			if (ksid!=null) {
//				for (OSDXKey keyserversKey : ksid.getKnownKeys()) {
//					System.out.println("ADDING Keyserver Key to TRUSTED: "+keyserversKey.getKeyID());
//					addRatedKey(keyserversKey, TrustRatingOfKey.RATING_MARGINAL);
//				}
//			}
//			
//			MasterKey[] k = null;
//			if (buildKeys) {
//				int keyCount = 10;
//				k = new MasterKey[keyCount];
//
//				StringBuffer s  = new StringBuffer();
//				for (int i=0;i<keyCount;i++) {
//					MasterKey m = MasterKey.buildNewMasterKeyfromKeyPair(AsymmetricKeyPair.generateAsymmetricKeyPair());
//					m.setAuthoritativeKeyServer(host);
//					Identity id = Identity.newEmptyIdentity();
//					id.setIdentNum(1);
//					id.setEmail("debug_key_"+i+"@it-is-awesome.de");
//					m.addIdentity(id);
//					m.createLockedPrivateKey("mantra: password","password");					
//
//					OutputStream out = new OutputStream() {
//						private StringBuilder string = new StringBuilder();
//						public void write(int b) throws IOException {
//							this.string.append((char) b );
//						}
//						public String toString(){
//							return string.toString();
//						}
//					};
//					Document.buildDocument(m.toElement(null)).outputCompact(out);
//
//					s.append("k["+i+"] = (MasterKey)OSDXKey.fromElement(Document.fromString(\"");
//					s.append(out.toString().replace('\n',' ').replace('\r',' ').replace("\"", "\\\""));
//					s.append("\").getRootElement());\n");
//					k[i] = m;
//
//				}
//				System.out.println(s.toString());
//			} else {
//				k = new MasterKey[10];
//				k[0] = (MasterKey)OSDXKey.fromElement(Document.fromString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>  <keypair><identities><identity><identnum>0001</identnum><email>debug_key_0@it-is-awesome.de</email><mnemonic /><country /><region /><city /><postcode /><company /><unit /><subunit /><function /><surname /><middlename /><name /><phone /><note /><sha1>BC:3A:6D:C7:F5:69:E1:39:A6:BC:6B:A5:96:A6:08:F7:E0:6F:3F:32</sha1><datapath /></identity></identities><sha1fingerprint>D9:1C:42:50:94:E3:BF:B1:2E:86:91:07:D8:54:95:CB:C5:8F:07:71</sha1fingerprint><authoritativekeyserver>localhost</authoritativekeyserver><authoritativekeyserver_port>8889</authoritativekeyserver_port><datapath><step1><datasource>LOCAL</datasource><datainsertdatetime>2011-04-21 09:44:58 GMT+00:00</datainsertdatetime></step1></datapath><valid_from>2011-04-21 09:44:58 GMT+00:00</valid_from><valid_until>2036-04-20 15:44:58 GMT+00:00</valid_until><usage>ONLYSIGN</usage><level>MASTER</level><parentkeyid /><algo>RSA</algo><bits>3072</bits><modulus>00:96:FD:FD:B1:8B:91:78:99:43:FD:04:D9:CE:7F:FA:76:6A:DD:93:79:76:01:83:CA:B8:6C:DC:0C:94:57:39:5B:B1:72:C1:C3:BA:21:0C:95:0D:5C:EC:FE:4F:4C:3A:32:94:66:2A:13:5F:9E:D7:9B:54:61:F3:7C:A6:B7:1C:D9:83:9A:F8:C7:05:7E:00:1B:F5:34:F6:13:47:2D:A0:32:72:F0:F7:04:80:F4:D6:12:D0:23:D2:D4:19:C6:2E:7B:0F:0D:83:4D:8F:45:01:75:F2:13:D8:50:8C:E0:6D:BE:43:96:D5:60:30:F6:CE:B9:DE:D1:DB:72:3B:0B:04:36:DF:E5:FD:D8:8B:09:66:5C:6B:A3:F8:AC:04:D8:58:67:31:FB:A3:B0:23:9F:D8:0E:B0:49:20:7A:46:60:BC:17:E9:50:27:F2:9B:9B:83:64:ED:6E:12:47:C3:AD:9E:56:82:25:B2:6A:ED:9D:97:BA:9E:6E:08:8F:C1:0D:A4:E8:AE:86:93:E4:E0:6D:49:CF:59:64:E3:C0:8A:21:95:7E:A5:9D:39:CB:E9:24:21:B1:86:AA:EC:FC:FA:9E:04:3A:39:18:E3:24:02:45:E4:1F:A5:1F:24:B0:BF:13:80:90:D4:2A:5B:E1:04:CE:AC:72:54:76:8F:0C:A3:CD:DD:21:96:52:14:1A:DD:7C:81:49:07:32:9C:15:8E:BD:1B:2B:30:A9:89:48:99:8A:D1:EC:7B:E4:88:E4:DC:64:1D:91:4F:FC:5E:76:4F:42:E2:F3:8F:95:99:8F:F2:5A:E1:1E:08:DE:D9:BF:19:73:DA:24:B0:77:A8:24:17:D5:C1:E5:15:EC:87:E0:6C:C8:EB:1E:77:AC:93:CA:EB:DD:28:3B:90:E2:55:46:E6:D0:4B:F5:9B:A1:72:90:7D:CB:7B:46:B8:62:C8:9B:E3:1C:46:A7:B8:D7:90:B7:C5:3C:E2:3B:6C:2D:CD:DD:83:FD:2E:BD:D6:40:2F:39:DA:96:07:7D</modulus><pubkey><exponent>01:00:01</exponent></pubkey><privkey><exponent><locked><mantraname>mantra: password</mantraname><algo>AES@256</algo><initvector>58:69:A2:16:9B:94:F7:EA:A6:E9:8F:BC:6E:C1:38:69</initvector><padding>CBC/PKCS#5</padding><bytes>22:46:81:13:E5:A2:34:E2:A5:58:A8:E4:C0:49:04:FD:CF:28:9C:DE:20:96:C1:C2:4B:4C:34:16:54:53:B6:0A:CF:13:DA:19:9E:64:C2:6F:8F:29:EC:DC:B8:64:3E:D3:8B:FD:80:A9:E2:F8:D9:EF:E4:BF:85:95:28:91:D4:EC:22:89:55:98:5C:88:CB:DB:13:3C:B8:1C:9E:01:FB:CE:4E:22:C8:3C:A3:30:1E:29:1B:11:CA:F9:ED:A5:B1:B3:C0:85:97:80:C6:67:49:3D:83:E0:DB:CC:62:32:CF:D0:41:56:9A:1D:72:62:D7:4E:DD:4F:E1:B3:8F:8F:72:B2:E3:CB:7A:C1:F9:C6:3A:F1:50:42:48:36:32:CD:0E:16:02:BA:54:1A:1B:A4:8F:D4:22:1D:B6:20:34:7C:50:2F:BA:38:3F:96:FC:4F:0E:18:1A:05:DF:6A:1A:42:14:EF:43:EC:E9:E6:AA:BA:AA:87:E1:A6:18:96:04:BE:74:13:4F:44:0B:3E:18:8D:8B:A4:87:3E:3C:D1:DB:CE:AB:AF:49:21:4E:0D:EA:C9:E4:F6:B8:75:AE:BD:4D:DD:CB:01:D8:41:36:0D:7C:3E:6F:CF:7C:44:10:B2:85:7F:80:5C:1C:2B:07:04:71:0A:52:16:91:ED:FF:73:C9:21:42:A1:30:F7:89:EB:9B:AF:75:CA:7C:76:9E:7E:68:2C:B0:AD:BE:ED:B0:D6:16:F9:C7:88:FF:DC:B0:09:C1:E4:F4:61:41:A3:34:EF:E4:0F:F8:2B:A3:3D:B9:92:78:65:5D:A1:92:1E:1E:CD:08:1F:70:77:6E:99:09:32:1A:6B:A5:37:90:E1:B9:39:24:8C:4A:02:84:66:11:D6:82:C1:AF:24:48:75:B8:BD:DA:9D:47:BE:95:3F:FA:E8:F4:54:73:30:82:3B:55:0E:09:DD:04:2E:AA:9F:6C:4B:D9:FA:3F:07:5C:D1:E6:6E:F0:92:97:FE:2A:B2:88:CF:DF:41:A8:40:A9:BD:3F:93:6F:62:05:53:90:FF:DF:A7:A6:0E:FC:C7</bytes></locked></exponent></privkey><gpgkeyserverid /></keypair>  ").getRootElement());
//				k[1] = (MasterKey)OSDXKey.fromElement(Document.fromString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>  <keypair><identities><identity><identnum>0001</identnum><email>debug_key_1@it-is-awesome.de</email><mnemonic /><country /><region /><city /><postcode /><company /><unit /><subunit /><function /><surname /><middlename /><name /><phone /><note /><sha1>46:77:49:F0:66:7C:5D:AE:02:EC:A4:FD:6B:2D:36:53:A0:4E:AF:67</sha1><datapath /></identity></identities><sha1fingerprint>01:D2:47:5D:93:BA:54:01:29:AE:6B:99:71:72:94:95:AB:A0:94:DD</sha1fingerprint><authoritativekeyserver>localhost</authoritativekeyserver><authoritativekeyserver_port>8889</authoritativekeyserver_port><datapath><step1><datasource>LOCAL</datasource><datainsertdatetime>2011-04-21 09:44:59 GMT+00:00</datainsertdatetime></step1></datapath><valid_from>2011-04-21 09:44:59 GMT+00:00</valid_from><valid_until>2036-04-20 15:44:59 GMT+00:00</valid_until><usage>ONLYSIGN</usage><level>MASTER</level><parentkeyid /><algo>RSA</algo><bits>3072</bits><modulus>00:C5:49:1F:53:0C:80:8B:76:AA:D9:97:95:EF:DE:D7:DB:23:18:63:F2:68:8D:D3:3C:0C:D2:E1:11:11:CC:56:1C:48:8F:75:6B:55:49:93:F7:A2:48:77:7A:60:EA:A2:B6:E2:ED:BD:C9:01:76:4C:67:71:11:8E:91:A7:61:B3:A9:04:A6:1D:ED:81:4B:52:BF:88:85:8A:F9:81:1B:1A:C9:07:6D:5E:CC:F7:C7:D7:01:4C:CB:EC:A2:07:2A:06:DF:77:18:A5:14:6C:5E:8A:25:43:3A:8E:90:8E:79:4E:98:1E:28:8F:02:66:B0:4F:01:0F:88:16:C4:1A:BD:4C:5F:7E:91:1A:F7:DF:13:B2:99:FD:CC:60:31:A3:EC:C0:0F:8F:DA:73:6D:62:8A:91:58:4B:02:FB:AE:A6:C9:69:28:05:DB:1E:85:A1:F8:CC:E5:36:FB:7C:E7:7A:34:64:B0:1B:41:1F:9B:6D:B1:39:66:86:13:DF:22:77:E0:6F:50:00:2E:E6:EF:D5:74:2B:A2:72:85:E6:23:4F:0B:59:56:0C:B7:A6:20:07:67:D4:F2:0D:9C:CB:28:01:50:9D:43:07:98:52:BF:07:7C:FF:3C:D8:E6:EB:2B:07:11:64:E3:D9:1E:26:36:C4:29:2C:F7:F1:3E:7B:2C:20:11:87:CE:DD:2E:38:CD:69:48:2D:B0:AC:19:63:39:01:1A:2E:8E:4D:EE:52:55:50:A8:4F:83:29:42:A6:9D:99:8A:49:D3:8F:28:A8:54:BA:CC:AA:C0:04:6D:AB:36:29:B9:F7:8C:8E:E7:BA:0E:53:A1:70:83:72:E6:99:89:68:BC:3D:12:80:11:77:49:DA:31:73:4F:D0:82:9D:8B:13:D7:EA:5C:F3:A6:F9:13:6F:AD:64:7C:76:06:75:13:08:07:C9:F6:AA:E0:FD:5D:97:14:3E:34:D1:FC:9C:5C:80:76:09:A9:46:15:82:9A:C1:AC:E2:BA:82:58:06:72:8B:92:3C:1F</modulus><pubkey><exponent>01:00:01</exponent></pubkey><privkey><exponent><locked><mantraname>mantra: password</mantraname><algo>AES@256</algo><initvector>D3:76:99:86:29:C9:B1:B5:7C:7D:AD:03:0C:5A:28:8E</initvector><padding>CBC/PKCS#5</padding><bytes>EB:FB:4A:53:74:B1:2A:C4:02:4F:8B:85:80:AD:A3:5D:77:C7:13:45:B3:BD:12:3D:39:0B:DE:59:33:9C:92:D3:54:8D:B5:8B:7D:5B:44:C3:A5:AF:33:5B:AD:A8:8E:16:1E:34:21:41:97:5E:25:F4:E2:DE:57:16:EE:2E:16:FC:72:9B:90:29:4E:05:4A:8A:82:B9:9B:5E:C5:D7:84:EF:FA:FA:C8:F3:27:19:1B:E0:21:05:EF:33:66:88:05:DA:C8:CE:E8:3C:BF:AB:57:AA:9C:38:30:FC:5C:B7:04:96:65:2A:FA:1A:4A:DF:EE:5A:F6:2D:AC:C0:09:B3:A7:8A:00:C7:06:D8:1E:66:5F:53:8B:C5:17:3C:0B:95:BD:E2:8C:9B:34:D5:76:32:0F:0C:94:C1:FA:1D:91:E5:92:0C:DE:03:2C:46:BA:87:FD:87:5C:F5:42:89:9B:44:FD:F3:17:96:4C:C0:48:BD:EC:72:EA:17:D4:89:A4:06:5B:51:FD:CC:3B:F5:2C:0F:47:50:13:41:59:F8:9A:41:9F:8C:08:6D:4C:8E:BD:F8:DD:7B:92:F7:86:D0:7C:03:A1:3C:6B:FD:8C:EC:2E:28:93:49:99:D8:92:F9:AB:D1:8E:B4:25:F9:D9:6A:90:6D:7F:7F:1E:51:62:92:75:B4:94:38:2B:F4:FE:15:8F:44:CA:98:54:CB:2A:BC:4C:1E:34:9C:29:54:D5:4B:5D:60:ED:44:CA:74:F2:71:C1:6E:CC:5F:25:90:0A:FD:A7:03:91:E2:CA:E2:49:A0:5A:BD:96:0C:81:D9:D8:F6:FE:2F:C4:B9:F2:AF:44:C3:9F:B5:70:60:09:EF:29:00:0F:11:33:B3:96:39:A3:06:47:8B:3E:FA:CB:69:A1:00:06:0B:3C:F1:4E:A6:3F:A2:FA:EF:98:58:CC:45:6F:86:10:A0:95:26:20:07:41:8C:9F:04:62:EE:29:3B:67:DE:B3:20:B1:D3:E4:96:6C:8C:4F:3C:DA:FA:1E:F7:BC:B7:32:CF:C6:F2:D9:52:8A:37:33:B4:3E:8B</bytes></locked></exponent></privkey><gpgkeyserverid /></keypair>  ").getRootElement());
//				k[2] = (MasterKey)OSDXKey.fromElement(Document.fromString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>  <keypair><identities><identity><identnum>0001</identnum><email>debug_key_2@it-is-awesome.de</email><mnemonic /><country /><region /><city /><postcode /><company /><unit /><subunit /><function /><surname /><middlename /><name /><phone /><note /><sha1>5D:F3:38:2B:09:71:D5:85:A1:A1:15:91:EC:DD:1B:86:27:61:CE:E9</sha1><datapath /></identity></identities><sha1fingerprint>1E:94:F2:77:57:74:9D:40:18:2A:E7:19:23:DB:49:EE:52:54:59:45</sha1fingerprint><authoritativekeyserver>localhost</authoritativekeyserver><authoritativekeyserver_port>8889</authoritativekeyserver_port><datapath><step1><datasource>LOCAL</datasource><datainsertdatetime>2011-04-21 09:45:01 GMT+00:00</datainsertdatetime></step1></datapath><valid_from>2011-04-21 09:45:01 GMT+00:00</valid_from><valid_until>2036-04-20 15:45:01 GMT+00:00</valid_until><usage>ONLYSIGN</usage><level>MASTER</level><parentkeyid /><algo>RSA</algo><bits>3072</bits><modulus>00:99:5F:FD:CC:62:B4:38:07:20:AC:B7:89:AD:A9:A3:36:F6:F5:63:0A:E3:47:1D:F7:76:E3:CD:AE:D5:4B:80:13:60:38:1A:10:62:5C:CD:FB:21:C4:EE:14:EA:95:3E:5E:48:9D:86:3B:81:77:FB:95:34:C8:F5:C5:B8:11:1F:A0:70:F9:09:D0:06:07:FF:25:EF:CD:8D:B4:45:FE:79:C0:32:80:B8:A2:49:32:E5:AE:60:1D:33:BD:8D:AE:22:80:81:E2:D2:BE:CF:89:8F:8F:03:34:CA:B8:83:E6:74:39:FF:C8:33:8A:CF:CF:BD:EC:24:4C:77:27:38:D8:3D:B5:AB:B6:83:75:FC:8E:8E:0D:A2:38:2F:6E:B6:F8:D1:CD:ED:1E:02:B6:F4:E8:DC:F3:6C:B3:8B:34:2E:6A:FD:C9:52:8F:4E:04:BF:BA:25:40:3E:A8:26:75:FD:C5:26:74:53:CB:FE:AF:DF:45:D8:3D:78:34:11:F8:EB:61:EE:D1:FD:AC:5A:0E:FF:89:26:B1:DD:89:E8:70:F5:D4:3E:15:34:EB:7C:EE:B9:69:F1:9A:6D:25:C6:B8:24:37:F7:E5:A4:37:95:91:9E:2F:46:EE:F6:AE:38:6D:EF:2A:99:85:4C:47:13:3D:3C:D5:B4:71:E5:C7:03:4C:5E:B8:5D:11:9D:E4:96:1D:7E:B0:85:E6:10:C6:72:99:EB:CD:61:D7:1C:A8:17:3D:9B:C0:E2:0D:A3:59:64:66:7D:88:76:0C:CD:F1:A4:6F:32:09:DF:D8:F1:7B:1C:9A:D3:84:0E:AB:AA:5E:96:7D:3F:1A:99:80:FB:6F:B8:53:7E:E1:9B:4E:9A:57:BF:20:75:EC:98:00:6D:F6:88:15:BB:8E:2A:34:61:F6:AC:FD:89:C5:10:93:C0:5F:58:20:56:9B:DA:00:5F:96:EA:92:23:41:D8:E3:F7:7A:FA:19:56:05:4B:39:88:B9:B3:2F:C6:B1:AB:8A:A8:D0:28:DE:5F:95:72:6F</modulus><pubkey><exponent>01:00:01</exponent></pubkey><privkey><exponent><locked><mantraname>mantra: password</mantraname><algo>AES@256</algo><initvector>BC:8D:7F:D3:E8:82:B4:8C:E1:6A:1D:7B:E7:66:EB:E3</initvector><padding>CBC/PKCS#5</padding><bytes>E8:33:BB:6C:7B:1B:93:DA:C1:FB:A5:DE:F7:1D:C8:35:B1:DD:4E:6F:19:C3:07:8D:B7:F3:3A:49:C5:48:73:3A:65:F3:57:28:F1:F6:51:ED:54:DB:A5:17:67:1A:19:6A:EA:B8:E1:C6:56:03:E9:88:69:E0:3A:6B:CA:8D:C2:E1:51:F9:BE:F1:93:5B:CF:A0:F0:27:C4:61:6D:43:EF:C6:F5:E8:4E:98:44:94:3E:DE:D2:99:BA:EF:DC:A9:1E:D9:54:9A:9D:E2:5D:96:99:B5:57:A0:53:E1:A6:A9:2E:33:38:35:E4:AC:07:0D:BC:23:2D:19:B0:E4:1A:5A:CC:A4:12:49:96:5D:04:92:BC:1D:97:F1:C9:31:B8:EC:FE:0F:F7:6B:CC:C7:55:D3:9F:94:E0:0A:01:66:22:CF:2A:75:82:9E:E8:3E:AE:53:0E:3D:0D:10:EC:F2:00:81:B1:37:7C:2C:7B:CB:C9:4D:0A:0E:0F:89:5C:A2:C8:D7:21:9D:3B:79:6E:1D:97:A3:D0:29:36:A8:CA:E1:48:FF:7D:FB:04:27:23:EA:13:2A:1C:CB:6C:57:7C:DC:18:96:C3:DB:E7:C7:86:81:93:8D:44:9A:60:99:D8:9C:C7:15:41:D9:C8:42:CC:A7:75:0A:BE:50:48:E4:DE:C3:78:24:9C:9B:D1:47:A7:D3:DA:87:65:03:AA:55:0B:AA:C6:DA:C7:8A:65:5F:43:09:2A:3D:42:B9:0F:2F:71:80:37:18:8B:33:A6:3B:66:16:E6:45:86:85:F4:51:32:09:16:99:BF:44:90:E4:DB:13:DE:62:C5:98:96:A9:90:06:3C:62:22:FA:2C:5B:64:D7:B3:92:97:36:7B:51:77:B9:9C:B5:38:96:1D:4B:62:BA:F5:40:7C:53:E8:48:A4:9E:74:7B:6E:6F:54:DD:AB:B3:D1:AA:4F:55:44:DA:2E:CC:39:CE:F8:89:F4:66:72:4E:DB:09:A1:EE:41:9A:4D:A4:58:EC:7C:40:7F:B8:FA:7F:53:D0:67:6E:67:88:40:89:24:3A:5A:A6</bytes></locked></exponent></privkey><gpgkeyserverid /></keypair>  ").getRootElement());
//				k[3] = (MasterKey)OSDXKey.fromElement(Document.fromString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>  <keypair><identities><identity><identnum>0001</identnum><email>debug_key_3@it-is-awesome.de</email><mnemonic /><country /><region /><city /><postcode /><company /><unit /><subunit /><function /><surname /><middlename /><name /><phone /><note /><sha1>6B:89:A2:80:5B:6D:03:80:9B:B4:6E:7A:C0:03:B5:36:0E:A9:86:7E</sha1><datapath /></identity></identities><sha1fingerprint>55:32:7D:CD:4E:37:D1:88:EB:02:B9:B9:5A:9F:8A:CF:45:2D:44:D3</sha1fingerprint><authoritativekeyserver>localhost</authoritativekeyserver><authoritativekeyserver_port>8889</authoritativekeyserver_port><datapath><step1><datasource>LOCAL</datasource><datainsertdatetime>2011-04-21 09:45:08 GMT+00:00</datainsertdatetime></step1></datapath><valid_from>2011-04-21 09:45:08 GMT+00:00</valid_from><valid_until>2036-04-20 15:45:08 GMT+00:00</valid_until><usage>ONLYSIGN</usage><level>MASTER</level><parentkeyid /><algo>RSA</algo><bits>3072</bits><modulus>00:86:2B:BE:DA:48:49:ED:29:77:7D:00:18:CE:5D:55:EE:32:18:84:94:8B:17:D8:12:30:9A:3A:78:14:A5:E3:AE:00:19:56:E4:E8:FC:EF:03:77:E3:1B:4F:0D:4D:C8:D2:D6:31:A1:F9:58:44:59:5C:FC:C9:D7:43:5B:FE:77:BF:34:E7:C9:D2:87:E2:36:67:8C:DA:A2:29:73:DE:E0:02:F5:62:19:03:33:C9:4B:75:5F:1F:C9:5C:3F:65:D3:54:1F:3C:65:F0:D5:D7:B7:CB:0F:D9:31:64:EB:5E:1E:F4:7B:EA:C8:FB:65:31:0C:57:90:15:6C:E2:F2:29:CF:EC:68:C7:C9:F3:BA:95:73:AC:83:FE:0A:19:27:6F:C5:CD:61:68:6E:87:00:51:F5:8E:AE:7E:3C:E2:A1:FB:A5:27:6A:CB:71:61:42:5F:DF:E9:77:1E:C7:C3:8D:89:A4:0E:69:7E:F5:C2:71:DF:5C:49:8C:C0:09:91:5C:12:23:D2:ED:8F:67:22:FB:8D:6D:8D:BC:3C:8F:57:FF:9A:C1:1B:49:45:48:09:33:46:68:3F:DF:3D:BB:99:57:21:9F:5B:0B:B7:4C:AC:EA:3B:E8:7E:59:C7:F2:D5:28:D5:E9:BE:36:E9:88:B1:FC:3F:5F:8B:93:BA:9A:A2:AF:8B:E6:91:25:64:45:27:C7:45:E8:68:AE:57:38:EF:81:F3:A5:5E:A1:1C:E0:CA:89:52:D2:4C:B3:E9:ED:C3:E0:7C:E6:0F:02:C7:7C:D2:89:56:C5:03:1E:36:27:E7:DB:2B:25:67:22:0B:BA:91:50:D1:21:42:39:B2:33:D1:5C:F5:31:31:15:96:39:61:51:6B:ED:CD:52:D2:E3:FA:3B:3F:69:6B:D2:74:71:19:6F:0D:3C:7D:77:A8:28:09:1D:0D:4E:C4:AA:D2:7F:BA:84:C3:9C:14:55:38:95:A8:A5:94:C7:A5:24:D1:DC:02:E6:C0:A8:65:7E:19:33:0A:74:58:36:EB</modulus><pubkey><exponent>01:00:01</exponent></pubkey><privkey><exponent><locked><mantraname>mantra: password</mantraname><algo>AES@256</algo><initvector>43:97:F9:BC:C8:9C:33:7B:44:A5:A7:12:5D:14:55:C3</initvector><padding>CBC/PKCS#5</padding><bytes>4A:14:5E:2E:9D:55:53:27:DB:61:FA:C9:B0:13:7E:7A:51:A5:93:EF:43:42:12:E4:F8:C0:7F:C9:E6:46:30:DD:D7:B4:F1:D0:A8:05:C6:DF:E6:BA:45:6C:B8:61:A4:D4:78:DC:92:B0:C7:D8:63:CF:12:CD:2A:CC:28:E8:F7:1F:31:51:77:D0:30:27:BD:86:8E:87:25:77:68:F2:A1:6E:3B:2C:92:F0:66:C6:CB:C3:64:86:04:BC:1B:EA:AA:4E:03:E8:33:BB:66:4C:A8:08:F3:63:B1:1A:1D:4F:9E:77:20:F5:67:09:83:18:31:8C:21:08:9F:6B:30:0D:E5:E7:75:D1:22:A1:E3:99:5B:8C:23:42:4E:EE:ED:CD:3A:73:4C:75:C8:3F:34:EA:C6:B7:BC:99:32:FF:67:F2:10:A0:69:12:5C:A0:11:C1:00:E6:00:A1:58:45:35:70:1D:D9:0D:B3:28:18:21:57:39:E1:13:EE:BF:60:47:63:DF:E4:E6:7E:5B:2A:8E:DF:E0:A8:85:13:23:BB:A0:44:4E:DE:6F:51:00:27:E0:B9:F6:03:19:A6:AA:C7:BF:7C:19:1F:E5:6C:E9:FB:1B:A4:01:F3:92:C8:BA:C5:43:D0:D6:E0:D6:16:6F:C9:9C:45:6B:96:03:80:6D:32:48:F6:64:CE:96:DB:F9:A6:42:49:F4:53:A8:24:D7:3D:F6:FC:47:4C:5A:9F:AF:95:CC:0C:A2:3A:0A:3A:30:C7:52:B6:DB:97:AB:9C:CE:22:31:C8:7D:93:52:8C:5D:48:5D:0F:98:E5:E3:05:CB:F6:F7:2B:86:9F:4B:FC:F6:F5:07:5F:45:0F:BB:85:2D:1A:B9:67:BE:47:64:83:10:F2:59:8A:16:9A:06:49:DB:35:70:A7:32:C1:19:C1:C6:DC:17:BD:CB:A6:0F:7D:12:A8:CC:80:8A:9D:13:87:47:C3:10:FF:B0:8A:C1:EB:5D:7E:B2:C5:5A:52:50:A8:2D:49:48:61:19:30:A0:95:E1:64:B0:15:B2:4F:D2:08:9F:76:D2:83:D2:16</bytes></locked></exponent></privkey><gpgkeyserverid /></keypair>  ").getRootElement());
//				k[4] = (MasterKey)OSDXKey.fromElement(Document.fromString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>  <keypair><identities><identity><identnum>0001</identnum><email>debug_key_4@it-is-awesome.de</email><mnemonic /><country /><region /><city /><postcode /><company /><unit /><subunit /><function /><surname /><middlename /><name /><phone /><note /><sha1>9E:F2:D1:13:A7:4A:F7:6E:2C:60:16:20:FB:CE:55:16:36:C2:6A:E4</sha1><datapath /></identity></identities><sha1fingerprint>A8:D9:0E:0A:BC:E2:3D:7E:B2:F7:EC:99:1D:11:FE:E4:0E:65:DD:11</sha1fingerprint><authoritativekeyserver>localhost</authoritativekeyserver><authoritativekeyserver_port>8889</authoritativekeyserver_port><datapath><step1><datasource>LOCAL</datasource><datainsertdatetime>2011-04-21 09:45:10 GMT+00:00</datainsertdatetime></step1></datapath><valid_from>2011-04-21 09:45:10 GMT+00:00</valid_from><valid_until>2036-04-20 15:45:10 GMT+00:00</valid_until><usage>ONLYSIGN</usage><level>MASTER</level><parentkeyid /><algo>RSA</algo><bits>3072</bits><modulus>00:8C:7A:27:66:01:39:2C:52:7A:F4:32:C1:28:81:C1:C3:BC:5F:66:50:31:94:F8:9D:FB:B0:90:92:FF:1F:82:74:C3:E3:0A:E3:FC:B9:DD:02:8D:47:9E:33:FE:70:F5:89:F2:CC:2D:2E:81:71:07:31:78:E5:A3:E0:BA:C3:B4:DC:B8:60:A8:06:99:9D:B2:64:08:FF:AA:B8:8E:99:00:BA:6B:E2:D4:DE:31:0A:B7:BA:A0:AC:19:4A:4E:1F:1A:CB:6D:BE:ED:16:D9:F8:A8:77:3C:A8:7E:99:62:99:86:86:D6:2B:10:56:B5:89:B0:13:13:A0:04:FE:B6:02:62:1E:54:BA:06:CF:39:6D:2C:83:2D:B1:27:1B:BD:42:21:3F:91:72:17:B7:08:3D:14:3F:E7:6F:64:0A:97:31:38:0F:FA:42:35:42:5B:5D:65:16:84:62:12:F3:30:B7:95:59:C4:4F:40:97:B3:40:CE:02:25:7B:26:6C:05:77:DF:93:C8:95:36:41:0A:48:47:1D:81:CF:63:3A:87:6D:22:42:42:52:35:9B:F1:A0:61:C3:C6:E2:FC:3A:35:47:FB:7B:D5:02:D5:D4:32:E3:3D:DD:0E:63:A5:77:5F:64:A7:CF:B9:7C:DA:24:82:F4:5E:6C:13:B4:E0:5E:2E:87:49:A7:6F:B1:F8:FE:C9:24:05:AD:B4:85:69:67:38:0C:4B:C6:41:84:B1:89:AD:3C:95:B7:DD:0E:9B:CB:17:EB:57:69:99:86:DD:78:EF:B5:5A:4E:78:1C:49:E8:4C:F3:B6:32:7B:C3:03:59:51:2A:DD:E2:15:AA:C6:D2:09:0D:90:F7:F0:2D:D4:2E:91:71:7B:CB:0E:E7:BE:AE:05:93:3C:BC:0B:99:70:64:55:BA:A2:13:BD:2D:60:D3:5A:78:5E:8C:B6:CA:9E:31:3C:A0:36:3F:4A:B8:83:8A:3F:E3:4A:FC:9D:D9:70:C9:D4:B5:0B:FB:D6:C0:51:A9:C4:8F:1D:F1</modulus><pubkey><exponent>01:00:01</exponent></pubkey><privkey><exponent><locked><mantraname>mantra: password</mantraname><algo>AES@256</algo><initvector>4D:A2:79:70:FF:11:62:BC:6F:36:31:95:AB:B2:CD:3F</initvector><padding>CBC/PKCS#5</padding><bytes>35:EE:D0:60:7F:6C:7C:8D:D7:60:55:20:5E:FA:FF:D0:AC:B1:9D:0C:7F:12:17:1B:BE:A0:E3:0D:FB:F9:CC:EC:87:4F:E2:B3:7F:AB:23:08:20:02:B0:48:5D:37:23:FE:B9:C8:D7:1F:DD:7A:E7:64:67:24:56:FF:86:4C:83:25:83:48:20:DB:93:54:9A:31:C3:6B:88:63:50:59:B5:47:DA:74:E7:BB:D8:C0:99:8B:9D:87:C6:0B:38:FF:67:6B:2F:4D:12:94:B0:6F:BC:E3:12:EF:6B:7D:EF:83:9F:09:60:6E:4D:F1:67:47:4C:C4:E9:F4:C0:F4:A4:08:7D:3F:49:6F:65:83:BC:4D:A7:31:02:0F:4E:28:AA:5A:7D:29:1D:54:56:65:F0:DF:36:4D:EE:F3:DB:0E:83:5E:E4:69:0A:EF:84:48:38:92:6F:3E:82:80:38:95:3F:1E:7C:BE:EA:D3:08:12:85:06:5B:22:82:C2:0B:C8:DC:AE:08:20:42:AE:1D:C4:7A:56:FF:30:D9:0B:95:22:61:F0:D6:5B:19:46:7F:DE:BB:C5:25:EE:A3:80:E8:49:70:8B:C2:84:0A:7B:4E:15:1A:B9:7A:4B:D8:69:83:66:D5:4A:C7:4B:2D:90:E5:F8:16:65:43:DD:FB:CA:7C:1A:B0:5C:C7:E8:CD:0F:0A:7E:25:2A:CE:C7:E5:59:21:9D:D3:DB:28:06:CE:11:EC:D4:E3:CF:82:5A:44:A5:47:CB:43:3E:C6:65:D1:E1:6F:B5:C1:D5:E6:4A:6F:A3:58:7C:0E:37:01:08:BE:75:7A:06:69:84:63:8B:E9:16:87:31:92:38:90:C0:66:88:E6:DC:13:8D:FD:DE:76:3A:36:59:71:95:FB:77:03:95:CB:9D:31:0E:3B:70:DA:92:14:AC:83:83:C8:30:7F:97:09:00:B3:F5:C9:13:FA:05:65:D8:0A:C3:80:12:E9:62:EC:7B:86:14:A5:EE:C6:D9:70:A7:4C:12:AA:93:29:D4:01:12:0E:C7:96:A5:BF:5A:FD:C4:28:4F:94:AA</bytes></locked></exponent></privkey><gpgkeyserverid /></keypair>  ").getRootElement());
//				k[5] = (MasterKey)OSDXKey.fromElement(Document.fromString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>  <keypair><identities><identity><identnum>0001</identnum><email>debug_key_5@it-is-awesome.de</email><mnemonic /><country /><region /><city /><postcode /><company /><unit /><subunit /><function /><surname /><middlename /><name /><phone /><note /><sha1>86:54:7F:A6:B6:30:8D:5E:5E:85:6A:EE:99:D9:F4:B3:5B:32:CE:C4</sha1><datapath /></identity></identities><sha1fingerprint>C7:C7:C6:74:9F:C3:92:F8:A1:C2:C8:C9:DB:58:D4:3D:BF:3A:F6:1A</sha1fingerprint><authoritativekeyserver>localhost</authoritativekeyserver><authoritativekeyserver_port>8889</authoritativekeyserver_port><datapath><step1><datasource>LOCAL</datasource><datainsertdatetime>2011-04-21 09:45:14 GMT+00:00</datainsertdatetime></step1></datapath><valid_from>2011-04-21 09:45:14 GMT+00:00</valid_from><valid_until>2036-04-20 15:45:14 GMT+00:00</valid_until><usage>ONLYSIGN</usage><level>MASTER</level><parentkeyid /><algo>RSA</algo><bits>3072</bits><modulus>00:85:30:18:37:B1:8C:27:4D:9B:F3:55:59:86:1C:DD:DD:CE:9E:C3:25:9C:53:8D:3F:C4:EB:1F:62:8C:60:D3:42:6F:D5:AF:B2:25:FA:60:CA:28:77:B7:7C:C2:6B:7D:C1:D9:06:AF:B0:D3:26:39:F1:66:A5:D8:42:98:4E:0D:38:70:F3:3D:64:98:76:B0:6F:27:43:BE:61:B9:AB:89:BD:8A:53:C6:5C:75:E8:58:89:CD:0F:FE:C7:14:20:2C:62:56:96:F8:07:7A:15:7A:F7:EA:C3:FC:9C:96:34:B4:A2:A4:48:BD:4A:F8:21:85:AD:9C:81:D2:C7:46:8B:C2:9D:FF:CA:F0:DB:45:CA:9D:F9:29:E8:E7:C5:E4:BD:38:32:CD:D7:6B:D7:40:76:76:D2:2A:BF:81:1C:D6:BF:9A:13:94:70:7D:15:7D:AF:AA:D3:7B:98:83:32:F7:2D:2E:87:F6:8D:5C:A4:69:08:E2:78:32:0C:EA:1F:E6:CE:03:D1:B5:B5:19:F2:0E:7C:35:47:8D:A9:D1:50:06:29:C2:4E:70:EC:3C:20:AF:1D:95:7A:19:EE:3C:15:88:2E:54:04:EB:D7:73:C5:B8:48:F3:59:77:6C:3B:B6:04:35:E3:FD:3C:C1:18:E7:D9:64:BF:D7:47:A7:F0:31:D2:33:18:EC:3F:46:0E:55:C6:3D:E3:40:57:49:C9:B5:89:03:F6:CD:48:37:91:F5:87:58:3A:98:23:C0:7F:01:C9:52:92:AD:8D:34:3C:24:72:BC:D8:77:6D:E2:E2:45:8F:D8:0C:9B:9A:02:B7:95:A4:49:5A:0C:EA:FB:91:E0:2E:5C:43:CB:94:B5:F0:FD:12:54:20:AB:56:B9:FC:B6:A9:CC:BE:0D:DF:D7:D7:F0:12:F7:24:A7:47:14:61:F5:AF:60:64:0A:5D:91:73:88:FD:98:E8:BC:7D:FF:7A:49:EE:43:B2:FA:34:7B:93:38:DC:06:47:99:D5:77:F4:4C:DE:58:BD:CF</modulus><pubkey><exponent>01:00:01</exponent></pubkey><privkey><exponent><locked><mantraname>mantra: password</mantraname><algo>AES@256</algo><initvector>04:B4:77:D2:9C:E4:C3:99:37:2B:24:E5:A2:CB:18:5E</initvector><padding>CBC/PKCS#5</padding><bytes>F1:E5:21:AC:1D:AE:0A:EF:D7:AF:ED:5C:42:04:22:D7:A4:11:2E:09:C4:2D:FF:F1:96:F8:08:67:26:75:54:24:83:4D:8C:E2:C9:C8:4B:4F:8C:5D:7E:10:CA:EF:F3:68:1F:07:0C:9C:E7:79:91:FC:01:BA:5D:84:85:C3:3B:B1:30:16:FE:73:FC:18:C1:FB:47:31:AD:F7:BF:C4:4B:4D:E1:0D:99:ED:AE:8E:CB:98:68:70:73:D0:BD:16:D7:47:76:ED:94:E6:08:91:CB:D3:BF:D5:B0:68:C7:AC:29:DD:57:F1:DC:99:FE:22:C5:8D:1A:07:61:5E:AC:4B:F7:AB:F1:53:5F:2B:41:F7:A4:1A:3E:1B:32:5E:42:9F:EF:92:14:81:4F:57:30:45:0B:3B:78:51:C7:58:EB:83:B3:53:16:D0:27:EB:9E:AB:77:0C:93:C1:F6:7C:9F:B5:BB:A4:D1:C8:2A:98:97:D6:7D:C7:6E:EB:E6:60:D6:73:01:18:AD:29:B2:30:1A:2D:44:13:3D:6F:FC:29:49:12:59:B2:DB:69:E7:EC:24:64:09:EE:ED:AD:85:D4:5D:0D:20:06:EA:C4:76:80:3F:0D:5C:3B:8A:B1:68:60:86:21:D7:1D:47:63:06:C7:0B:A6:B0:2A:30:41:D2:3C:CE:82:AE:AB:80:CD:98:B9:87:54:19:35:57:81:B4:7E:BB:F8:AF:07:FB:08:6E:DC:17:95:AF:A8:C4:02:1E:38:E5:75:A7:7B:A5:26:50:A2:0B:B3:0A:B4:41:F6:4B:C4:51:FA:1D:0D:DA:77:F4:12:CC:21:4E:D9:76:43:CB:A6:C2:5F:F4:5E:DC:2C:D8:0A:F4:8D:CF:FB:02:1A:A3:53:F7:81:ED:03:1D:6C:74:D3:F0:CC:D8:F8:0F:4B:B3:8D:0C:12:E8:EA:C7:1F:B8:76:30:0B:C7:CB:1E:6E:AE:47:60:DB:53:AF:88:00:35:6B:0C:7A:43:87:B0:61:96:36:75:E2:BA:60:48:F0:A5:25:F5:C5:BB:1F:C4:CA:56:C2:D3:0B:A5:68</bytes></locked></exponent></privkey><gpgkeyserverid /></keypair>  ").getRootElement());
//				k[6] = (MasterKey)OSDXKey.fromElement(Document.fromString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>  <keypair><identities><identity><identnum>0001</identnum><email>debug_key_6@it-is-awesome.de</email><mnemonic /><country /><region /><city /><postcode /><company /><unit /><subunit /><function /><surname /><middlename /><name /><phone /><note /><sha1>BA:17:B4:D4:2D:55:42:52:A5:C4:68:99:E4:63:60:03:7A:93:14:F6</sha1><datapath /></identity></identities><sha1fingerprint>71:4D:31:86:3B:4B:6E:9D:7A:1A:6E:38:1C:E9:FF:38:CB:7D:90:4D</sha1fingerprint><authoritativekeyserver>localhost</authoritativekeyserver><authoritativekeyserver_port>8889</authoritativekeyserver_port><datapath><step1><datasource>LOCAL</datasource><datainsertdatetime>2011-04-21 09:45:17 GMT+00:00</datainsertdatetime></step1></datapath><valid_from>2011-04-21 09:45:17 GMT+00:00</valid_from><valid_until>2036-04-20 15:45:17 GMT+00:00</valid_until><usage>ONLYSIGN</usage><level>MASTER</level><parentkeyid /><algo>RSA</algo><bits>3072</bits><modulus>00:B8:D9:48:E8:02:36:39:CD:CA:E5:2C:C3:81:F9:78:47:8C:CB:EF:E6:71:3F:06:3B:84:6A:DE:A0:08:4C:DD:68:4C:45:A8:0F:0B:1B:BE:7A:F8:14:BA:49:DE:26:8C:21:B3:BB:CD:F3:A0:87:EF:3F:83:F5:7A:3A:98:49:D0:9D:AC:F6:AA:67:6F:C2:54:6E:86:26:54:79:66:54:6E:EE:C9:41:E1:BF:22:A2:FE:F8:CE:7E:CB:25:65:EE:50:EF:06:ED:AD:93:D9:F0:AA:50:A3:94:3D:C2:E2:1E:3F:4E:F4:52:2A:A6:BF:F7:CA:6B:90:7C:0A:38:B0:D8:BF:6F:B3:21:82:8B:EF:FA:D7:24:58:47:DF:80:50:39:FB:B9:0F:32:7B:39:85:7C:B7:84:A0:86:D2:D2:E4:93:40:BB:BF:00:AD:6F:9D:43:69:ED:B2:64:37:D1:5C:6A:17:27:4A:72:C7:7B:C1:73:B9:9C:E1:34:6C:DE:E2:F2:94:2D:8E:BE:C2:04:1F:29:58:82:50:5A:EE:8D:3E:FC:BE:DD:33:8D:61:AE:BE:15:9A:DB:93:68:45:04:0E:CC:DE:B7:BF:64:AC:84:06:13:16:11:7F:69:23:25:81:23:71:DB:40:04:5A:50:89:3F:C7:CD:C3:67:A8:91:D9:9E:1B:CC:95:3C:4A:18:BB:58:DC:8D:EF:4F:73:02:99:50:4B:7D:EA:63:FA:57:97:84:58:57:45:F4:E2:AE:FD:8F:68:23:82:4B:33:2B:E1:67:64:DE:2E:B2:43:AA:85:AB:59:72:2E:45:65:19:76:3E:4E:18:21:4B:EA:08:20:63:6E:33:BB:9B:79:3C:DC:2A:F4:50:CE:D1:DE:C0:5B:6B:F3:20:D7:C6:CE:3E:AC:9D:36:2F:AE:62:14:C7:B8:BA:B6:92:27:72:DC:F0:B4:B8:2E:AE:26:AB:69:27:9B:92:AD:CF:F2:58:EE:0B:F5:3E:2D:C0:66:E8:9D:38:A1:84:9D:FD</modulus><pubkey><exponent>01:00:01</exponent></pubkey><privkey><exponent><locked><mantraname>mantra: password</mantraname><algo>AES@256</algo><initvector>69:C9:17:32:B4:78:74:F5:74:86:1D:5E:46:27:68:39</initvector><padding>CBC/PKCS#5</padding><bytes>1F:CB:DA:1A:A6:2A:F0:28:34:05:EB:B9:B8:14:29:8D:C2:80:D5:F1:8A:05:FA:60:32:E1:05:E9:86:F5:0B:D8:23:83:4E:8A:81:E8:8D:B9:53:AB:DD:E9:47:70:70:2D:75:32:95:B0:B8:65:2F:9A:32:70:60:CB:E2:CD:88:7F:41:70:75:7F:EA:DC:3F:0A:D3:6C:4F:44:B1:BE:44:49:BA:5F:6B:AC:A0:F5:50:DC:DE:FC:C2:5E:D9:64:F5:DD:2F:14:51:C2:E2:26:45:8F:5A:7D:48:0E:94:9B:38:12:09:AC:B1:A1:8C:A5:9A:7E:43:73:7F:8F:AF:BF:EC:0A:4B:D5:81:4D:20:12:9B:FA:5F:AA:30:D6:90:53:50:CB:8C:93:3D:0F:79:4D:B1:DA:E9:8C:97:A9:FE:BC:41:E5:32:1A:50:DC:52:73:F1:BB:61:25:5B:D1:70:7E:00:5A:CB:FB:A8:02:BD:B6:50:52:D8:79:CA:A7:8B:90:07:D3:F9:87:F0:C6:1D:09:46:08:9A:72:E4:99:C6:BF:62:59:A5:3C:BF:5B:63:3A:15:CE:EB:1B:D2:BD:6A:4F:6C:D9:4A:FC:37:0C:BA:76:8D:5F:2C:77:5F:5D:92:C0:9B:D1:39:C1:1B:68:49:0E:F0:B2:C4:2E:40:32:79:C2:A7:A2:E3:07:EB:8E:CA:A7:1D:2E:90:E0:4B:F9:80:1A:6B:62:92:EF:AF:CC:BF:D1:1D:4E:A2:FA:6F:21:2F:02:D7:C5:BD:FB:7B:2A:53:A2:B2:4E:31:E3:BC:34:3E:46:3E:C0:DA:97:96:51:BB:DC:6C:89:B2:F7:F5:2B:69:5B:25:5C:4C:30:CA:47:16:C8:E4:48:30:AF:0B:42:B9:9E:35:F3:01:55:00:72:33:CC:2B:4F:C1:72:95:71:55:B7:8D:A7:AC:05:63:13:99:B8:26:9A:EB:FC:E4:4E:3A:42:28:EB:8E:9F:41:20:3C:24:85:DF:54:7E:E3:FA:BA:16:E1:B0:2B:70:BF:39:BC:C9:B0:9D:88:0A:D8:3A:45:43:F1:26</bytes></locked></exponent></privkey><gpgkeyserverid /></keypair>  ").getRootElement());
//				k[7] = (MasterKey)OSDXKey.fromElement(Document.fromString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>  <keypair><identities><identity><identnum>0001</identnum><email>debug_key_7@it-is-awesome.de</email><mnemonic /><country /><region /><city /><postcode /><company /><unit /><subunit /><function /><surname /><middlename /><name /><phone /><note /><sha1>4A:D9:0D:92:48:8E:CC:A5:14:3A:DF:65:1C:B8:73:B3:7C:C3:1C:9E</sha1><datapath /></identity></identities><sha1fingerprint>98:4F:94:1B:BA:CB:84:A5:68:B4:DD:79:D2:51:A4:78:86:92:8B:98</sha1fingerprint><authoritativekeyserver>localhost</authoritativekeyserver><authoritativekeyserver_port>8889</authoritativekeyserver_port><datapath><step1><datasource>LOCAL</datasource><datainsertdatetime>2011-04-21 09:45:22 GMT+00:00</datainsertdatetime></step1></datapath><valid_from>2011-04-21 09:45:22 GMT+00:00</valid_from><valid_until>2036-04-20 15:45:22 GMT+00:00</valid_until><usage>ONLYSIGN</usage><level>MASTER</level><parentkeyid /><algo>RSA</algo><bits>3072</bits><modulus>00:A7:5C:AC:24:F4:DA:EF:93:7B:A1:67:AB:69:E3:B5:9E:CA:83:7E:5B:56:29:00:64:87:06:29:5A:C7:60:6B:85:DD:4F:7E:EC:1A:20:2D:0B:01:62:E3:A4:95:4A:48:8F:86:08:64:42:44:F5:1E:F0:D5:8F:ED:BF:A8:3A:E8:53:31:E4:96:E2:99:1E:95:C7:52:F2:73:A6:3F:CB:18:97:C3:D9:4A:E8:50:A8:DA:02:05:40:B2:C1:44:CA:61:06:63:B9:31:2A:E2:7A:28:1A:28:86:0F:DC:CF:FA:59:70:B0:78:FF:9D:85:19:BC:9A:50:A7:DF:D6:F3:98:44:58:C5:1A:3A:07:65:09:49:87:FE:39:F8:55:05:2D:3C:A3:5E:2C:59:AA:8F:46:97:0F:B2:02:56:53:D5:BC:43:98:46:B2:E4:7D:4C:09:A4:E4:C8:AB:11:5C:8E:6A:1A:A6:3A:EA:9B:24:44:D8:43:01:12:00:13:73:3F:19:6D:D8:C7:34:C6:B0:4E:7E:6B:EE:21:80:7C:3D:3B:41:9B:DB:91:3B:38:CD:78:CD:95:94:B9:C5:BC:5A:83:AC:23:74:F8:88:9C:C3:6B:BF:78:15:48:B2:00:36:0C:AA:AB:7C:DA:4A:D8:3E:66:EB:5E:6E:A8:5D:C5:51:6E:14:64:05:C4:5D:6F:82:DD:63:2A:19:32:0B:41:75:73:DA:8A:EF:88:B9:34:23:C0:98:16:C0:A9:A3:73:73:D2:7E:16:87:7D:BB:9B:E3:DC:54:84:EF:B3:75:43:89:AF:14:C5:23:54:E8:FE:F7:B7:0B:EE:FB:4C:01:B8:13:4C:CF:26:C1:F8:98:FB:C6:CA:EA:4D:3A:0F:C6:3B:BD:11:8D:50:BF:B3:E0:90:1A:20:D2:A6:07:D9:E5:E7:42:80:82:D5:C7:62:59:66:A9:6E:30:D7:E4:B2:6F:61:7B:40:44:53:78:CD:0A:6D:CF:B6:9F:7E:6A:69:C4:AF:88:12:97:E5:AD</modulus><pubkey><exponent>01:00:01</exponent></pubkey><privkey><exponent><locked><mantraname>mantra: password</mantraname><algo>AES@256</algo><initvector>B8:E9:ED:B0:8C:42:BA:CE:3C:E0:6C:4C:AA:27:3E:1D</initvector><padding>CBC/PKCS#5</padding><bytes>F4:DA:45:F1:2E:C0:5A:19:FB:63:98:4A:3F:8A:0E:0F:29:E1:73:FA:11:9B:74:14:F2:58:8F:69:C9:BB:2E:DB:7A:3E:4D:CB:9A:2F:2C:8E:B9:C0:48:1F:93:A5:8E:0C:B2:0C:45:7B:DD:8A:80:4B:24:C7:4C:49:52:D8:9E:20:13:CF:23:E4:A9:37:1B:12:6D:A7:4A:6C:70:36:CB:2A:E3:45:81:90:CE:B2:C6:18:3D:18:84:44:CD:31:FA:D6:B5:4E:D1:17:12:08:E6:A4:69:B4:56:DE:67:8E:90:09:11:E9:E4:9E:AC:A1:3F:56:61:06:F1:BC:A8:46:EC:65:63:2F:7A:EF:45:CE:F4:2B:68:80:1E:3D:E5:43:09:9A:E4:7A:99:50:F9:A9:FC:C1:91:5B:F0:C8:E3:18:D6:68:C2:91:3D:15:19:66:7C:BA:6A:1E:81:14:63:1C:36:38:4B:E8:33:00:2D:57:C5:30:71:41:3A:07:62:E4:88:B4:EC:B5:90:16:67:28:C0:4A:EC:6C:8B:08:3D:52:7C:66:F3:D4:DF:C3:A6:DC:96:F7:26:5D:3A:81:61:F6:31:5E:EE:3A:07:E8:CC:63:C4:38:86:BB:06:74:F8:AD:A1:DD:76:5F:E4:32:71:03:72:80:55:0D:71:3D:B4:5A:6A:7F:82:B3:D5:A8:CA:1C:B7:1C:78:F9:C7:DC:D4:76:79:0F:FF:E3:10:D5:8C:62:65:F3:0A:4E:93:76:9F:60:AB:E6:93:E8:62:17:BB:33:76:7A:2B:AD:8F:77:3D:B6:CF:AB:39:5E:3F:84:C1:A6:ED:57:B1:2D:28:A8:8E:74:A5:BC:88:FB:F5:6D:21:00:C1:D0:BC:53:F7:D2:F2:06:36:19:82:9E:E9:9C:45:D7:BE:31:0E:6E:72:85:46:10:54:8C:FE:00:2F:FE:81:28:5E:75:B4:7D:F4:5F:29:43:62:3F:62:F1:EC:F5:46:68:99:77:9F:6B:AF:AA:73:D5:0D:2B:CE:AB:8B:1B:88:87:4D:15:B0:35:3F:1D:FE:F7:D0:B6</bytes></locked></exponent></privkey><gpgkeyserverid /></keypair>  ").getRootElement());
//				k[8] = (MasterKey)OSDXKey.fromElement(Document.fromString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>  <keypair><identities><identity><identnum>0001</identnum><email>debug_key_8@it-is-awesome.de</email><mnemonic /><country /><region /><city /><postcode /><company /><unit /><subunit /><function /><surname /><middlename /><name /><phone /><note /><sha1>71:0C:B8:CC:4E:BC:B7:07:49:50:63:F4:B9:8B:C5:B1:71:D6:B7:50</sha1><datapath /></identity></identities><sha1fingerprint>8B:B9:50:63:A9:9F:91:27:B1:E0:49:77:96:E3:E3:F6:6D:0A:BC:DB</sha1fingerprint><authoritativekeyserver>localhost</authoritativekeyserver><authoritativekeyserver_port>8889</authoritativekeyserver_port><datapath><step1><datasource>LOCAL</datasource><datainsertdatetime>2011-04-21 09:45:24 GMT+00:00</datainsertdatetime></step1></datapath><valid_from>2011-04-21 09:45:24 GMT+00:00</valid_from><valid_until>2036-04-20 15:45:24 GMT+00:00</valid_until><usage>ONLYSIGN</usage><level>MASTER</level><parentkeyid /><algo>RSA</algo><bits>3072</bits><modulus>00:90:B7:82:D8:F2:3A:B4:F1:47:EC:61:82:43:F9:D4:EF:B5:19:BB:D6:45:AC:AD:A1:0C:14:94:E6:4C:86:58:AE:4A:B8:18:FD:F4:3D:C0:A1:9D:47:71:6E:63:2D:F8:37:E6:94:BD:7A:AD:9F:E7:20:CF:59:AE:C3:9A:82:64:B9:7B:FA:0D:3D:D0:8D:93:E2:DB:8E:07:02:C9:D3:6A:08:25:6B:B5:45:E3:2C:DF:2F:3A:A4:50:25:42:1C:66:F6:20:0F:49:A5:30:31:BB:EC:E5:7C:4D:C8:79:0B:FD:8F:B4:5D:80:39:C7:65:08:53:C2:85:12:81:7B:27:3F:50:E3:1B:B4:A3:FE:FF:5F:D3:B3:96:CC:69:45:B9:15:92:08:7E:BC:78:44:21:A2:7F:4B:F4:87:59:E0:83:4B:33:82:9C:0D:5F:E6:F4:1B:06:48:23:75:E0:9B:8F:C9:A0:82:CF:D6:CE:74:25:76:E5:80:EB:7A:6E:FF:AC:90:35:52:21:5B:09:6B:5C:0B:C4:1C:D7:7E:C5:61:BF:CC:10:D5:74:DB:78:FC:D6:6A:B6:35:75:D1:EF:6D:FB:51:CD:8E:51:44:4D:B3:DF:1E:5A:D9:2C:32:58:AC:36:C3:3B:F2:45:CE:E3:7D:A6:64:98:5B:7A:1E:B5:AC:0D:A8:50:CF:15:25:2E:5C:07:0F:94:D0:0F:72:6F:12:4F:0E:AD:D9:5F:BF:DE:D0:98:EA:AD:6D:82:76:3C:B3:C0:AC:B3:41:F9:6F:57:60:89:3A:0B:19:C0:CF:81:EE:2B:94:AF:4C:A5:23:42:D8:28:CD:B6:7F:E5:60:AA:82:99:1C:02:00:6C:42:24:EB:F6:25:31:AC:9A:83:FA:53:36:71:15:9B:20:7F:9F:00:4A:E5:AA:50:8E:D3:C5:03:2B:D1:D6:DA:D4:A8:E9:A1:8B:56:27:CB:DD:E6:D5:E0:73:3C:58:CA:64:3B:B3:D8:BE:81:FB:86:D1:2B:68:1E:1B:58:2B</modulus><pubkey><exponent>01:00:01</exponent></pubkey><privkey><exponent><locked><mantraname>mantra: password</mantraname><algo>AES@256</algo><initvector>8C:EA:82:96:CF:8E:75:EE:97:4E:5B:C3:BC:C5:0A:B7</initvector><padding>CBC/PKCS#5</padding><bytes>0A:63:33:F9:D5:F4:30:6E:E9:D1:4C:83:CE:65:42:FE:41:21:B1:99:AC:42:AC:99:FA:31:38:2A:94:0B:38:AF:22:86:D3:57:F1:3E:05:61:31:43:B5:F6:2E:BE:A1:48:31:EC:A6:D0:EF:EB:AA:84:85:AC:2B:58:0F:B3:FA:56:EF:DB:A2:51:15:2B:75:6B:AC:2F:CD:C3:5F:BC:7F:42:C3:0D:C4:24:FE:0E:24:E0:87:13:3E:62:DF:59:85:48:78:94:9A:C2:A2:4F:53:5D:6E:4C:45:77:82:A2:5D:E6:0B:6D:1E:1C:C4:AC:4D:6A:F0:6A:82:5D:8F:76:E4:37:77:BD:3B:87:71:EA:F9:5C:9F:7A:F6:A7:A6:71:F6:0A:9E:DD:86:17:C5:3E:76:A9:15:B9:60:17:9E:C1:3D:C5:A7:E7:05:D9:3D:87:F3:42:48:AB:AC:66:DF:B1:7F:CD:80:FE:88:AA:C7:00:7E:07:6A:3F:54:43:B4:EF:FB:5F:8D:95:5A:12:A3:F5:20:5F:53:2C:2D:5A:57:C6:2D:83:AC:87:15:D7:84:1B:41:66:99:3A:C7:4E:A6:25:D0:F7:F0:2C:EB:A7:40:05:9F:45:18:97:71:EE:81:DD:ED:61:DB:71:80:C9:E6:95:96:2B:80:B4:9C:26:6B:72:20:62:80:0F:2B:04:84:55:37:09:8C:1F:39:18:78:51:58:5D:66:D2:BF:DD:96:09:BA:0A:07:BC:20:2A:56:61:D9:1D:F4:B9:95:01:C9:B2:B9:BD:DC:3A:23:5E:29:4D:1F:4C:B1:D2:36:B6:68:3A:EB:18:47:31:72:58:30:CA:AB:1C:B1:62:50:ED:59:DC:22:C8:50:E8:6E:AA:8C:FE:1D:9B:2E:75:88:A4:AB:E8:84:E8:45:2F:64:BE:55:3D:B4:2B:2B:55:4C:F9:55:06:4D:A0:D9:8F:FF:02:A6:09:24:95:21:C0:D9:1D:A7:F1:F0:79:10:2E:8B:ED:B6:40:16:A9:89:B0:D1:0C:92:9B:F0:E0:42:EC:63:6C:72:D7:70:89</bytes></locked></exponent></privkey><gpgkeyserverid /></keypair>  ").getRootElement());
//				k[9] = (MasterKey)OSDXKey.fromElement(Document.fromString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>  <keypair><identities><identity><identnum>0001</identnum><email>debug_key_9@it-is-awesome.de</email><mnemonic /><country /><region /><city /><postcode /><company /><unit /><subunit /><function /><surname /><middlename /><name /><phone /><note /><sha1>7D:CB:14:1F:55:57:66:45:FC:49:DA:59:9D:EC:F9:A9:64:2D:E8:17</sha1><datapath /></identity></identities><sha1fingerprint>2C:52:14:A8:01:B1:22:FD:34:75:10:36:46:D6:E1:C5:6A:82:2C:9A</sha1fingerprint><authoritativekeyserver>localhost</authoritativekeyserver><authoritativekeyserver_port>8889</authoritativekeyserver_port><datapath><step1><datasource>LOCAL</datasource><datainsertdatetime>2011-04-21 09:45:25 GMT+00:00</datainsertdatetime></step1></datapath><valid_from>2011-04-21 09:45:25 GMT+00:00</valid_from><valid_until>2036-04-20 15:45:25 GMT+00:00</valid_until><usage>ONLYSIGN</usage><level>MASTER</level><parentkeyid /><algo>RSA</algo><bits>3072</bits><modulus>00:91:17:03:14:BA:01:6E:B3:FB:8F:B6:E4:52:22:F2:77:C3:B4:21:CE:FD:38:87:50:70:7B:6F:17:FD:4E:5D:01:A7:22:16:14:0F:86:D3:8C:BD:5A:8E:90:AE:59:72:1F:B6:1B:B8:F4:C4:8A:E8:AF:CD:49:B9:0B:ED:DD:8B:0A:38:98:96:99:6B:3E:C1:35:06:5B:5D:58:EF:03:05:43:51:A7:02:2E:89:FB:BB:C1:53:FC:09:0C:C5:FE:F4:7C:FF:D2:0E:02:47:00:76:06:CC:E4:68:47:11:C0:79:08:37:69:65:A3:81:EA:C6:87:01:6E:9A:14:09:30:B0:45:11:87:39:C2:E7:E1:B0:B6:F7:EB:78:AA:C6:FB:11:5B:10:64:91:EF:0D:9E:E6:B6:BE:B6:ED:66:A4:63:3E:B7:B4:7F:32:10:93:CA:70:5C:00:19:36:C9:D1:3B:8A:F7:76:C1:08:1C:B4:07:45:98:84:6E:48:83:15:BF:BA:BF:98:07:99:07:1D:B8:05:8D:85:93:5D:C6:84:A4:A9:D5:AC:5A:02:2D:68:18:9D:6E:3E:37:05:72:B6:13:0B:2B:1F:B7:09:A6:26:D5:8F:42:1A:CE:04:90:D4:BA:A9:67:AA:28:C8:18:67:69:AA:3C:C5:B2:C6:6B:65:D3:9C:B5:87:48:A7:08:67:48:A3:64:CB:EA:58:93:CD:56:D8:5D:98:69:10:7A:43:96:86:BC:1C:81:90:7F:B3:B6:F6:4E:D9:C2:F5:BD:21:E5:A5:D1:5E:02:DC:B9:E8:C5:52:94:E5:6C:A3:A0:12:41:C1:1D:C6:43:3F:36:2F:A8:9E:05:AD:A3:A1:AC:40:7D:8D:EC:82:72:80:EC:50:5E:A1:B1:74:2F:93:4E:C9:31:18:E0:9B:F1:93:96:DB:62:89:C7:4E:08:9B:E9:95:0F:1D:43:3C:48:76:6B:58:04:73:6D:1B:F1:1F:DA:61:AD:2C:12:A5:ED:93:91:E2:EA:56:33</modulus><pubkey><exponent>01:00:01</exponent></pubkey><privkey><exponent><locked><mantraname>mantra: password</mantraname><algo>AES@256</algo><initvector>84:C5:C9:0D:9B:0D:9B:BA:B7:6F:D2:13:82:06:F9:C2</initvector><padding>CBC/PKCS#5</padding><bytes>67:DC:72:1D:26:31:4D:5A:55:E6:70:4D:E9:53:8E:43:F7:2C:2D:57:28:A2:02:FE:6B:8B:7F:A2:23:1F:F6:D8:8F:70:F6:CC:D3:C2:97:61:10:F8:23:03:A5:91:45:B8:B5:D0:86:9F:7E:F9:60:55:6D:E0:F3:A2:79:84:36:BB:06:68:EC:E5:81:EF:D0:6E:1D:D0:25:BC:16:C8:5A:F2:C0:B1:48:A9:87:85:09:62:C8:CA:0D:E2:62:67:9C:27:F4:FD:77:DC:00:80:10:E2:2C:94:4F:8C:BB:D4:44:CD:79:30:E1:AC:1A:F3:1B:1D:06:6B:83:28:35:14:6A:D2:49:99:26:7C:E8:F2:07:7C:E9:18:E4:A9:34:6F:7C:0A:C8:ED:64:03:F6:8F:98:CA:5E:DC:5D:44:BA:DA:E1:B6:59:3C:08:59:49:AD:16:F8:F8:52:F1:9B:DA:A4:B1:9E:28:F3:0C:A8:0B:24:AB:D9:38:78:C9:FA:89:C9:1C:4B:D5:AC:DD:8F:2A:BC:3D:C7:13:68:66:42:EB:32:EE:DE:DE:44:AC:88:4A:AC:01:FB:D9:EB:61:EA:C5:02:1A:27:20:AD:2A:A7:31:9D:E4:47:F8:AE:FF:50:68:9B:83:4F:AA:97:D8:1F:E3:F5:8C:86:31:8E:FB:D3:D8:0C:54:66:46:27:A1:E8:71:27:8F:FC:15:56:94:00:E5:F8:21:EC:D6:B0:1E:51:E0:70:AE:E4:A2:B0:17:44:50:5F:53:AB:34:F0:E6:2C:F5:FD:ED:E0:BD:85:B5:A1:4F:83:8B:1B:F7:07:0D:39:C9:DA:6D:A8:C5:3F:40:D3:51:5A:C2:8E:4F:4C:AB:29:44:08:16:71:A9:E7:79:F5:AD:54:65:AB:19:6D:30:9E:10:07:C7:D4:AE:7F:6F:48:E4:03:57:B5:A9:B4:50:BC:CF:6D:1B:60:38:60:FE:86:F8:9D:AD:D1:12:E4:2B:91:9E:1F:58:C1:16:0E:CD:1F:15:D9:17:35:C7:2E:D2:E6:C8:BA:E8:6D:A8:FA:91:54:6D:0C:71:A3</bytes></locked></exponent></privkey><gpgkeyserverid /></keypair>  ").getRootElement());
//				for (int i=0;i<k.length;i++) {
//					k[i].unlockPrivateKey("password");
//					System.out.println(k[i].getKeyID());
//				}
//			}
//
//			if (uploadKeys) {
//				//client.log = System.out;
//				for (int i=0;i<k.length;i++) {
//					System.out.println("uploading key: "+k[i].getKeyID());
//					client.putMasterKey(k[i], k[i].getIdentity0001());
//				}
//			}
//
//			MasterKey a = k[0]; //D9:1C:42:50:94:E3:BF:B1:2E:86:91:07:D8:54:95:CB:C5:8F:07:71@localhost
//			MasterKey b = k[1]; //01:D2:47:5D:93:BA:54:01:29:AE:6B:99:71:72:94:95:AB:A0:94:DD@localhost
//			MasterKey c = k[2]; //1E:94:F2:77:57:74:9D:40:18:2A:E7:19:23:DB:49:EE:52:54:59:45@localhost
//			MasterKey d = k[3]; //55:32:7D:CD:4E:37:D1:88:EB:02:B9:B9:5A:9F:8A:CF:45:2D:44:D3@localhost
//			MasterKey e = k[4]; //A8:D9:0E:0A:BC:E2:3D:7E:B2:F7:EC:99:1D:11:FE:E4:0E:65:DD:11@localhost
//			MasterKey f = k[5]; //C7:C7:C6:74:9F:C3:92:F8:A1:C2:C8:C9:DB:58:D4:3D:BF:3A:F6:1A@localhost
//			MasterKey g = k[6]; //71:4D:31:86:3B:4B:6E:9D:7A:1A:6E:38:1C:E9:FF:38:CB:7D:90:4D@localhost
//			MasterKey h = k[7]; //98:4F:94:1B:BA:CB:84:A5:68:B4:DD:79:D2:51:A4:78:86:92:8B:98@localhost
//			MasterKey i = k[8]; //8B:B9:50:63:A9:9F:91:27:B1:E0:49:77:96:E3:E3:F6:6D:0A:BC:DB@localhost
//			MasterKey j = k[9]; //2C:52:14:A8:01:B1:22:FD:34:75:10:36:46:D6:E1:C5:6A:82:2C:9A@localhost
//
//
//			if (uploadKeyLogs) {	
//				createTestApprovalKeyLog(b,a, client);
//				createTestApprovalKeyLog(c,a, client);
//				createTestApprovalKeyLog(d,a, client);
//				createTestApprovalKeyLog(e,b, client);
//				createTestApprovalKeyLog(c,b, client);
//				createTestApprovalKeyLog(f,c, client);
//				createTestApprovalKeyLog(g,c, client);
//				createTestApprovalKeyLog(g,d, client);
//				createTestApprovalKeyLog(h,d, client);
//				createTestApprovalKeyLog(i,e, client);
//				createTestApprovalKeyLog(g,f, client);
//				createTestApprovalKeyLog(j,g, client);
//				createTestApprovalKeyLog(j,h, client);
//				createTestApprovalKeyLog(j,i, client);
//			}
//			KeyVerificator.addRatedKey(j, TrustRatingOfKey.RATING_COMPLETE);
//
//			Result result = findChainOfTrustTo(a);
//
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//	}
	
	private static Result findChainOfTrustTo(OSDXKey key) {
		if (isCheckInProgress(key.getKeyID())) {
			return Result.error("key check is already progessing -> no loop");
		}
		if (isNotTrustedKey(key.getKeyID())) {
			return Result.error("key is already registered as not trusted");
		}
		checkInProgress.add(key);
		//find a way to a trusted node -> breadth-first-search

		System.out.println("\nTRYING TO FIND A CHAIN-OF-TRUST FOR KEY "+key.getKeyID());
		TrustGraphNode trusted = null;
		try {
			trusted = findTrusted(key, 3);
		} catch(Exception ex) {
			checkInProgress.remove(key);
			ex.printStackTrace();
//			return null;
		}
		checkInProgress.remove(key);
		if (trusted != null) {
			System.out.println("FOUND A TRUST-CHAIN TO TRUSTED KEY: "+trusted.getKey().getKeyID());  
			//all all (new) trusted nodes to trustedKeys: parents of trusted
			Vector<TrustGraphNode> newTrustedNodes = findPredecessors(trusted);
			for (TrustGraphNode v : newTrustedNodes) {
				System.out.println("  new trusted key: "+v.getKey().getKeyID());
				//addRatedKey(v.getKey(), TrustRatingOfKey.RATING_MARGINAL);
			}
			return Result.succeeded();
		} else {
			//addRatedKey(key, TrustRatingOfKey.RATING_NOT_TRUSTED);
			return Result.error("could not find a chain-of-trust for given key");
		}
	}
	
	public static void traverseGraphFrom(OSDXKey key, int maxDepth) {
		if (isCheckInProgress(key.getKeyID())) return;
		if (isNotTrustedKey(key.getKeyID())) return;
		checkInProgress.add(key);
		TrustGraphNode nodeStart = trustGraph.addNode(key);
		trustGraph.breadth_first_search(nodeStart, maxDepth);
		checkInProgress.remove(key);
	}
	
	public static boolean isNotTrustedKey(String keyid) {
		int rating = trustGraph.getTrustRating(keyid);
		if (rating==TrustRatingOfKey.RATING_NOT_TRUSTED) return true;
		return false;
	}
	
	public static boolean isDirectlyRated(String keyid) {
		return trustGraph.isDirectlyRated(keyid);
	}
	
	public static int getTrustRating(String keyid) {
		return trustGraph.getTrustRating(keyid);
	}
	
	public static boolean isCheckInProgress(String keyid) {
		for (OSDXKey t : checkInProgress) {
			if (t.getKeyID().equals(keyid)) {
				return true;
			}
		}
		return false;
	}
	
	private static void createTestApprovalKeyLog(MasterKey from, MasterKey to, KeyClient client) {
		try {
			KeyLog keylog = KeyLog.buildNewKeyLog(KeyLog.APPROVAL, from, to.getKeyID(), "127.0.0.1", "127.0.01", to.getIdentity0001());
			client.putKeyLog(keylog, from);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static TrustGraphNode findTrusted(OSDXKey start, int maxDepth) {
		TrustGraphNode nodeStart = trustGraph.addNode(start);
		return trustGraph.breadth_first_search_to_trusted(nodeStart, maxDepth);
	}
	
	public static Vector<TrustGraphNode> findPredecessors(TrustGraphNode start) {
 		Vector<TrustGraphNode> predecessors = new Vector<TrustGraphNode>();
 		Vector<TrustGraphNode> queue = new Vector<TrustGraphNode>();
		queue.add(start);
		while (queue.size()>0) {
			TrustGraphNode v = queue.remove(0);
			predecessors.add(v);
			//System.out.println("predececcor: "+v.getID());
			for (TrustGraphNode w : v.getParents()) {
				if (!predecessors.contains(w)) {
					queue.add(w);
				}
			}
		}
		predecessors.remove(0);
		return predecessors;	
	}
	
	
	public static void main(String[] args) {
//		testGraphTraverseWithKeys();
//		try {
//			String host = "localhost";
//			int port = 8889;
//			KeyClient client = new KeyClient(host, port);
//			client.connect();
//			KeyServerIdentity ksid = client.requestKeyServerIdentity();
//			if (ksid!=null) {
//				for (OSDXKey keyserversKey : ksid.getKnownKeys()) {
//					System.out.println("ADDING Keyserver Key to TRUSTED: "+keyserversKey.getKeyID());
//					addRatedKey(keyserversKey, TrustRatingOfKey.RATING_MARGINAL);
//				}
//			}
//			client.requestMasterPubKey("D8:A0:AE:B3:88:95:B2:17:CD:BD:CF:E8:7C:81:AA:A0:02:66:B8:9E@localhost");
//			
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
	}
}

