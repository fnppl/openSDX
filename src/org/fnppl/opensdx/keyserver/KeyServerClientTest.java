package org.fnppl.opensdx.keyserver;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Vector;

import javax.print.Doc;

import org.fnppl.opensdx.http.HTTPClientRequest;
import org.fnppl.opensdx.security.AsymmetricKeyPair;
import org.fnppl.opensdx.security.Identity;
import org.fnppl.opensdx.security.KeyClient;
import org.fnppl.opensdx.security.KeyClientMessageFactory;
import org.fnppl.opensdx.security.KeyLog;
import org.fnppl.opensdx.security.KeyLogAction;
import org.fnppl.opensdx.security.KeyServerIdentity;
import org.fnppl.opensdx.security.KeyVerificator;
import org.fnppl.opensdx.security.MasterKey;
import org.fnppl.opensdx.security.OSDXKey;
import org.fnppl.opensdx.security.Result;
import org.fnppl.opensdx.security.RevokeKey;
import org.fnppl.opensdx.security.SubKey;
import org.fnppl.opensdx.security.TrustRatingOfKey;
import org.fnppl.opensdx.xml.Document;

public class KeyServerClientTest {


	public static void main(String[] args) {
		try {
	
		//generate testdata
		boolean gen = false;
		if (gen) {
			MasterKey masterkey = MasterKey.buildNewMasterKeyfromKeyPair(AsymmetricKeyPair.generateAsymmetricKeyPair());
			Identity id = Identity.newEmptyIdentity();
			id.setEmail("debug@it-is-awesome.de");
			id.setMnemonic("debug");
			masterkey.addIdentity(id);
			SubKey sub = masterkey.buildNewSubKeyfromKeyPair(AsymmetricKeyPair.generateAsymmetricKeyPair());
			RevokeKey revoke = masterkey.buildNewRevokeKeyfromKeyPair(AsymmetricKeyPair.generateAsymmetricKeyPair());
			
			masterkey.createLockedPrivateKey("master", "master");
			sub.createLockedPrivateKey("sub", "sub");
			revoke.createLockedPrivateKey("revoke", "revoke");
			
			System.out.print("MasterKey masterkey = (MasterKey)OSDXKey.fromElement(Document.fromString(\""+Document.buildDocument(masterkey.toElement(null)).toStringCompact().replace("\"","\\\"")+"\").getRootElement());");
			System.out.print("RevokeKey revokekey = (RevokeKey)OSDXKey.fromElement(Document.fromString(\""+Document.buildDocument(revoke.toElement(null)).toStringCompact().replace("\"","\\\"")+"\").getRootElement());");
			System.out.print("SubKey subkey = (SubKey)OSDXKey.fromElement(Document.fromString(\""+Document.buildDocument(sub.toElement(null)).toStringCompact().replace("\"","\\\"")+"\").getRootElement());");
			
		}
			
		//init test data
		MasterKey masterkey = (MasterKey)OSDXKey.fromElement(Document.fromString("<?xml version=\"1.0\" encoding=\"UTF-8\"?><keypair><identities><identity><identnum>0000</identnum><email>debug@it-is-awesome.de</email><mnemonic restricted=\"true\">debug</mnemonic><sha256>64:A0:A0:CD:71:82:32:BC:A0:4F:8D:A1:1A:8F:C7:4A:83:C9:11:D0:A4:3E:6B:08:19:D7:DB:B9:D2:90:11:77</sha256></identity></identities><sha1fingerprint>1F:94:74:9F:73:65:46:9F:12:17:76:D3:E7:C5:DF:6A:B7:04:7F:C9</sha1fingerprint><authoritativekeyserver>LOCAL</authoritativekeyserver><datapath><step1><datasource>LOCAL</datasource><datainsertdatetime>2011-05-20 09:16:30 GMT+00:00</datainsertdatetime></step1></datapath><valid_from>2011-05-20 09:16:30 GMT+00:00</valid_from><valid_until>2036-05-19 15:16:30 GMT+00:00</valid_until><usage>ONLYSIGN</usage><level>MASTER</level><parentkeyid /><algo>RSA</algo><bits>3072</bits><modulus>00:9E:35:7A:EE:6C:B8:02:04:A6:13:F1:44:B2:7B:3C:81:9E:02:80:C9:D0:50:6F:11:74:E6:04:C7:C6:2D:BB:04:BE:4B:B4:97:C8:0A:92:2E:84:4C:DF:37:F8:F2:04:4A:35:12:EA:B2:67:1D:94:EB:66:41:3D:03:3A:13:E3:86:C3:3C:15:69:4A:2D:78:D1:85:9F:D6:61:C7:D4:62:86:19:B5:B4:F8:FD:97:BC:BE:7B:3B:48:2E:56:AA:85:3C:35:DD:65:58:A8:11:34:F1:B3:7B:5F:53:49:28:9F:6E:88:55:9D:ED:E1:9F:A4:A1:F6:69:13:E4:1F:7A:C4:CA:BE:D3:43:D7:FB:3F:70:43:70:C5:D6:C0:DE:1A:3C:42:B2:08:C4:4E:A5:68:7E:AA:F1:26:4A:D3:6F:87:E8:C7:35:0F:32:14:62:C3:45:13:EA:10:69:D3:1E:56:1A:51:47:3A:7A:F5:E5:31:E8:90:F7:C6:DA:C2:39:6A:DD:AA:6F:B9:A3:DF:9F:80:32:BA:D1:ED:F2:B3:15:A0:7C:00:AB:F4:15:D8:D8:70:41:E9:06:83:26:49:8F:6A:A5:D3:A1:CC:DF:C3:D7:2C:88:39:C4:A4:CF:F8:69:E3:53:E0:26:F0:C2:94:48:EC:C4:D5:7C:84:FF:FD:87:91:FB:1C:BD:BF:3E:9A:5B:BD:A5:A9:36:31:ED:5B:3E:AA:28:1D:E7:47:B4:5C:23:D8:CD:CC:4B:B2:F7:E8:BF:09:DA:E8:BE:D8:CB:F5:A2:2C:23:37:48:99:93:EA:24:BB:CA:B8:95:24:C3:97:A3:47:E8:E9:E1:8D:A1:73:41:41:F6:2C:51:88:4F:D0:82:20:97:BD:15:89:09:77:56:B8:BA:61:59:12:2A:31:0F:7C:48:53:D7:A4:F9:34:2A:05:A2:A6:5F:7D:3E:2F:C8:F2:98:F5:A9:52:4E:85:B3:CB:C0:8F:26:6A:79:61:39:4E:20:33:B2:33:E4:86:AE:0D:11:29</modulus><pubkey><exponent>01:00:01</exponent></pubkey><privkey><exponent><locked><mantraname>master</mantraname><algo>AES@256</algo><initvector>D5:48:0E:FF:74:8A:B4:57:DA:4D:86:67:9B:F9:E9:2B</initvector><padding>CBC/PKCS#5</padding><bytes>EF:3D:12:DE:73:60:FF:62:FC:51:E3:6A:F8:37:C3:E7:96:12:C9:6D:C2:09:F6:5B:C2:E6:CC:C2:9F:15:21:B5:2F:7F:AF:13:C4:B9:07:73:08:1E:66:C6:05:01:4D:B2:DD:92:C2:4D:FA:D4:46:10:C1:27:FD:0E:43:EA:8F:F8:9D:3E:AF:CB:03:32:DA:F6:CA:F7:6C:A0:77:77:DA:0E:94:DC:B3:0F:ED:DC:00:B8:9E:58:49:7E:8E:1C:74:39:C9:10:78:E8:D3:A5:E2:BF:06:E2:2F:74:52:84:CA:30:17:8C:78:72:83:B9:C3:B0:39:4F:FC:63:6D:BA:72:A2:8E:EB:08:96:96:4A:DD:17:4C:71:A1:C2:20:92:A2:B0:B3:FF:96:67:46:00:30:D7:A9:68:A3:57:78:58:E5:84:C6:1B:CE:DA:3E:21:DD:50:59:3D:BF:90:4F:17:C6:04:D0:C2:21:47:64:40:3D:D4:D1:43:F5:0F:FA:F4:ED:2E:35:B3:24:D6:86:AC:93:C9:FA:DD:0E:BF:79:D9:70:72:9E:27:CA:F1:C4:B9:01:29:C1:C6:67:7D:B0:74:DB:28:DE:D0:B8:80:D3:59:21:45:28:D3:B0:B9:C2:13:28:A3:3B:5D:CD:9C:6E:55:7A:C5:A5:09:96:12:B0:7D:F3:07:1B:BD:D7:2E:5D:B3:E3:EB:75:03:CC:84:E8:FB:2C:2A:ED:A9:2F:0F:2B:C9:3F:92:A7:CC:B0:59:60:38:36:1A:CD:F5:E9:DF:18:9C:68:7D:05:2F:C1:B0:FD:34:FD:A2:B9:B7:91:3E:7D:A7:C8:8A:44:7E:15:FA:29:26:F0:78:B1:D3:9F:D6:2F:93:86:8B:34:A7:A2:A5:E4:8E:44:29:D9:A2:82:80:AE:57:03:50:D1:0A:C1:2D:D1:9F:3C:26:FD:A0:9A:34:D8:08:78:03:02:E4:54:FE:87:FF:99:75:2D:CB:DB:28:EF:61:7A:CD:05:AB:29:0D:FD:99:59:DC:A5:5B:04:40:A4:2F:27:51:8B:C4:B7:04:99:6F:04:4A</bytes></locked></exponent></privkey><gpgkeyserverid /></keypair>").getRootElement());
		RevokeKey revokekey = (RevokeKey)OSDXKey.fromElement(Document.fromString("<?xml version=\"1.0\" encoding=\"UTF-8\"?><keypair><sha1fingerprint>6D:7B:4E:AC:F6:70:3C:BC:11:16:A1:57:2D:2A:08:24:6E:94:02:FE</sha1fingerprint><authoritativekeyserver>LOCAL</authoritativekeyserver><datapath><step1><datasource>LOCAL</datasource><datainsertdatetime>2011-05-20 09:16:36 GMT+00:00</datainsertdatetime></step1></datapath><valid_from>2011-05-20 09:16:36 GMT+00:00</valid_from><valid_until>2036-05-19 15:16:36 GMT+00:00</valid_until><usage>ONLYSIGN</usage><level>REVOKE</level><parentkeyid>1F:94:74:9F:73:65:46:9F:12:17:76:D3:E7:C5:DF:6A:B7:04:7F:C9@LOCAL</parentkeyid><algo>RSA</algo><bits>3072</bits><modulus>00:BF:07:B5:EB:34:7E:E5:4A:5E:72:DC:64:44:C3:E6:3B:4F:1D:73:F9:3A:DB:7A:AF:77:58:99:BF:E1:73:99:94:DA:87:6B:FE:A0:EB:22:86:98:58:41:74:64:01:59:EB:E3:2D:BA:69:BC:E8:01:CB:7D:4F:E5:F2:94:3F:A9:9F:5B:36:ED:4F:CB:75:ED:75:A3:4B:2B:01:31:D6:12:98:B9:1F:FB:45:A4:0D:DD:EB:EE:DF:93:01:AA:F1:DC:15:E6:D8:55:5D:52:B7:3D:86:03:C0:52:30:5C:DB:43:B6:96:8E:7B:29:ED:21:CB:2F:72:35:47:F3:99:C4:0C:DD:75:DD:11:23:4A:B3:4F:74:77:89:0D:99:ED:2A:53:CE:5B:39:88:56:86:CD:7E:F8:6C:6C:E2:FC:58:F2:27:B4:F3:F1:B4:AF:77:62:0E:73:96:41:42:C6:37:E5:54:6C:27:A8:70:0A:AD:FC:18:E0:15:29:7B:AB:12:2D:96:73:41:EC:58:AC:6C:A2:7B:EA:C8:37:F6:D1:F2:7E:2D:25:45:DD:84:CD:83:DA:0D:64:3F:5E:65:B4:58:F0:54:15:B7:85:AC:9E:E0:E5:C6:76:0F:EC:0A:40:0E:24:19:66:F6:60:7F:5A:CB:B1:4C:95:AD:42:27:D3:33:32:AE:39:5A:19:28:AF:EF:AF:5F:BB:AA:01:BC:B1:31:47:9B:51:23:53:37:AF:28:3C:CD:3E:AC:5B:CB:C2:E8:20:84:99:11:91:2E:57:3F:AF:1E:71:45:4A:6D:C2:1F:FB:56:3F:3E:6E:79:F7:1E:81:D9:1A:26:56:5E:19:E1:20:5A:F5:B1:D9:E2:B1:63:D4:61:FD:E3:83:9A:7D:11:E8:13:51:AB:DE:B8:70:61:A8:A4:6B:C1:57:AA:01:4C:D8:AF:DE:CE:AB:6F:88:7D:36:BE:29:B8:29:C5:42:77:1C:71:0D:B9:23:E4:6B:D0:B7:5F:D5:FF:29:82:C4:36:B4:11:D5</modulus><pubkey><exponent>01:00:01</exponent></pubkey><privkey><exponent><locked><mantraname>revoke</mantraname><algo>AES@256</algo><initvector>12:F7:FB:CD:1E:14:66:D9:E8:E4:D4:77:C0:AE:FD:FE</initvector><padding>CBC/PKCS#5</padding><bytes>9D:2A:F6:BA:C6:B2:D1:F6:F7:64:60:E8:E5:ED:0C:E9:6C:70:D3:78:3E:16:B9:DC:B1:8E:CB:76:BE:4E:6A:70:BF:58:2D:38:7D:84:06:D2:F5:2F:39:B0:25:3E:6F:48:A4:04:A7:5E:25:6D:D9:FC:07:6F:D2:87:43:1E:C1:69:A1:9A:D6:B2:DD:28:07:21:5F:83:F3:D5:36:1A:BF:14:E4:6D:A2:8F:C2:D6:B7:82:6A:59:07:DE:4D:7E:20:E7:5B:64:95:0B:79:D8:3F:22:BC:16:83:5B:AF:8C:35:EF:5E:A4:1C:8C:52:51:1A:9E:E6:D1:22:2D:E8:B4:52:7B:0A:FC:18:99:27:E9:9A:52:E7:57:47:2B:2B:21:7B:B3:CC:0D:D3:AA:3F:1A:D7:55:FE:E5:30:F0:E3:59:B1:FC:EA:5A:78:00:B9:95:CD:27:B3:04:20:29:90:87:FF:A7:84:B2:3F:9D:F0:31:29:66:3A:BA:91:76:03:F0:5F:FC:BC:1F:29:CE:9A:78:44:E6:34:F8:E0:20:6F:CA:8F:F9:31:D3:FA:99:78:36:CF:14:6B:BF:BE:F3:2E:55:3C:9E:2A:B8:15:3E:9D:B2:01:B7:7F:E3:70:2F:D6:37:67:ED:12:B1:0D:D5:67:D0:FD:C8:5F:FF:2F:33:3A:0C:F6:76:53:67:DB:6B:E2:DD:0C:9F:82:C1:70:2F:2B:5D:69:6F:CA:9C:44:58:8A:8B:17:DD:2B:7B:C2:EC:44:9E:EF:08:7B:07:09:24:8B:07:D2:77:C2:01:13:29:60:87:6D:A2:E2:CC:C4:3A:AB:08:97:57:A9:1A:1C:41:A7:48:8F:EE:B3:6B:19:CD:3A:A5:FB:E4:D3:FC:55:C4:54:74:FB:4F:D5:DF:30:F6:03:81:26:2D:7B:41:14:8A:A3:BA:DB:D6:36:0C:B4:55:F8:42:0B:C8:E9:34:26:B1:A8:E3:CD:A3:BD:70:29:01:E7:46:2D:79:76:03:1E:1B:AD:13:E2:8F:64:F7:89:CC:D5:CE:C8:4A:67:D1:BE:4A:3A:ED:23:B4</bytes></locked></exponent></privkey><gpgkeyserverid /></keypair>").getRootElement());
		SubKey subkey = (SubKey)OSDXKey.fromElement(Document.fromString("<?xml version=\"1.0\" encoding=\"UTF-8\"?><keypair><sha1fingerprint>59:67:0C:08:5B:CF:BB:F7:75:92:5B:5D:16:BC:27:03:C3:6F:F5:3D</sha1fingerprint><authoritativekeyserver>LOCAL</authoritativekeyserver><datapath><step1><datasource>LOCAL</datasource><datainsertdatetime>2011-05-20 09:16:32 GMT+00:00</datainsertdatetime></step1></datapath><valid_from>2011-05-20 09:16:32 GMT+00:00</valid_from><valid_until>2036-05-19 15:16:32 GMT+00:00</valid_until><usage>ONLYSIGN</usage><level>SUB</level><parentkeyid>1F:94:74:9F:73:65:46:9F:12:17:76:D3:E7:C5:DF:6A:B7:04:7F:C9@LOCAL</parentkeyid><algo>RSA</algo><bits>3072</bits><modulus>00:9E:6C:65:A3:B7:96:81:49:19:8F:C5:46:C2:DC:28:0C:2F:58:20:6E:F3:EA:1A:DF:4D:C2:0D:8B:05:C6:0E:7B:E2:16:B3:FE:AA:9E:F8:94:F9:2F:BA:7C:4D:7F:E5:EA:12:2D:31:7C:15:64:60:4E:F8:20:F8:C4:94:44:93:B2:52:CF:B4:BD:9A:18:A6:02:D6:C4:D8:B5:33:23:EA:51:8E:37:E0:4E:B9:4C:0A:E2:EC:F3:74:9F:B7:70:71:3E:AE:23:BC:BE:BD:C4:C4:D8:4F:6C:67:A6:B4:1F:51:9F:CE:6B:2F:05:33:1B:80:A4:BA:50:52:3F:F5:74:B9:E8:8B:2C:74:3A:28:64:19:6D:3A:CA:10:7D:F5:F9:B4:46:D7:37:54:FD:E6:4C:FB:A4:EF:B9:DB:F5:26:E9:AC:E5:60:CB:AD:33:82:A7:52:E6:8A:1D:B4:1D:08:EB:78:A2:FF:23:38:70:4C:50:01:97:EF:9F:2F:CA:FE:58:3D:5F:FB:6F:E4:97:5F:41:23:47:3E:F6:95:79:79:3C:26:32:3D:8D:82:AE:FF:C4:F0:B2:E7:9F:85:C7:E3:A9:AB:9E:14:A2:ED:8F:F8:3A:B8:D5:C6:98:8D:03:09:EB:87:A5:A3:75:05:29:02:E1:96:BA:69:D6:3E:2E:37:18:5C:1D:BA:56:52:B9:93:2B:F4:B6:A2:56:CD:32:67:66:B3:A2:4E:F2:9E:87:D5:B9:98:40:3B:E7:E9:C1:06:00:90:58:52:3C:98:07:43:76:92:85:A6:77:C1:F0:CA:8E:2C:74:41:04:C2:68:EA:DE:3A:F9:43:9B:1B:A8:1F:DC:13:4C:64:B9:5B:56:2D:E7:D3:43:A1:20:A0:D3:1E:11:15:16:12:66:80:3C:80:2A:D1:CE:71:32:0D:DE:9F:26:74:ED:D0:A0:A6:00:76:9B:8C:78:55:00:C9:81:DA:0E:DA:E2:E1:C4:EA:CD:5C:00:63:99:06:F8:9E:D9:4B:6D:C9:65</modulus><pubkey><exponent>01:00:01</exponent></pubkey><privkey><exponent><locked><mantraname>sub</mantraname><algo>AES@256</algo><initvector>D1:F9:E1:94:05:C5:1D:D6:8F:5D:C9:F5:EF:59:D6:D1</initvector><padding>CBC/PKCS#5</padding><bytes>16:98:55:F5:6F:43:E1:8F:BD:F6:DE:62:34:99:FF:81:1B:72:7B:59:B0:70:24:C3:4E:17:C0:B9:89:F7:8E:4C:33:1B:28:E3:27:98:F3:F2:C5:1C:01:AA:F5:59:29:02:40:14:46:E6:D9:3E:90:FE:EB:3B:23:82:AD:61:DA:69:2E:27:00:6D:3D:EB:CC:8C:9A:1C:E0:49:B3:4F:F9:5E:C9:4F:55:6A:F1:05:7E:1D:14:57:5C:AF:3F:8A:A5:EC:E9:29:84:99:05:03:52:79:34:19:39:C4:CE:80:A2:CD:E9:FE:8B:EA:FE:FA:3B:48:DD:22:19:0D:1C:7E:58:4E:9E:F3:ED:12:06:D7:C7:B2:BE:AF:5B:FF:E4:19:C6:CC:26:FB:55:EB:31:59:D5:67:EE:FF:7B:BB:CF:1F:76:53:50:A9:C4:8F:8A:0E:57:6D:9C:56:0E:8B:2C:D8:10:98:53:7F:52:A6:5B:84:32:9C:BC:D1:73:2F:6E:4D:D9:24:A0:18:2D:83:28:0B:0A:DD:27:5F:BD:59:3A:69:E9:F1:A0:65:1C:0C:26:ED:D6:43:2A:FE:CA:3D:FD:42:0C:8A:8F:CB:40:BD:83:0D:AC:8B:B6:32:81:B7:03:C9:24:71:46:8D:64:B6:55:D9:A0:97:70:12:96:08:91:B6:95:34:85:0F:7C:49:91:2A:0E:1C:B0:24:CB:0D:28:63:D0:65:1C:A5:C4:E9:05:CF:3D:8F:4A:63:05:9A:6A:10:53:C5:0E:64:B6:25:56:11:49:51:3B:E6:EB:15:55:55:DF:AB:AE:10:2F:9A:A0:12:8F:10:FB:1F:77:34:48:3A:4E:98:FE:09:99:22:B3:89:EE:D6:DC:FC:17:E4:24:85:DF:12:39:0E:AD:62:BA:3E:12:7F:1D:A1:35:00:AC:9C:E6:9A:4E:20:B7:6F:36:6C:15:2C:5B:A9:7E:05:30:BB:69:ED:4F:F1:C2:C3:20:C7:E5:E6:3E:72:BE:18:1E:F7:16:EF:CB:20:C0:CA:EA:C5:59:91:44:E3:93:32:F2:B4:88:BE</bytes></locked></exponent></privkey><gpgkeyserverid /></keypair>").getRootElement());
		masterkey.addRevokeKey(revokekey);
		masterkey.addSubKey(subkey);
		masterkey.unlockPrivateKey("master");
		revokekey.unlockPrivateKey("revoke");
		subkey.unlockPrivateKey("sub");
		
		Vector<KeyLog> keylogs = null;
		
		//testing:: output all requests
		
		//String host = "it-is-awesome.de";
		String host = "localhost";
		KeyVerificator keyverificator = KeyVerificator.make();
		
		KeyClient client = new KeyClient(host, KeyClient.OSDX_KEYSERVER_DEFAULT_PORT, "", keyverificator);
		client.setKeyVerificator(keyverificator);
		
		OutputStream out = System.out;
		//out = new FileOutputStream(new File("test_keyserver_client_110520.txt"));
		
		client.log = out;
		
		masterkey.setAuthoritativeKeyServer(host);
		
		String idemail = masterkey.getCurrentIdentity().getEmail();
		
		HTTPClientRequest req;
		
		client.connect();
		
		boolean ok;
		
		KeyServerIdentity keyserverID = client.requestKeyServerIdentity();
		keyverificator.addKeyRating(keyserverID.getKnownKeys().get(0), TrustRatingOfKey.RATING_MARGINAL);
		
		ok = client.putMasterKey(masterkey, masterkey.getCurrentIdentity()); out.write("\n\n\n\n\n".getBytes());
		ok = client.putRevokeKey(revokekey, masterkey);out.write("\n\n\n\n\n".getBytes());
		ok = client.putSubKey(subkey, masterkey);out.write("\n\n\n\n\n".getBytes());
		
		client.requestKeyLogs(masterkey.getKeyID(),null);out.write("\n\n\n\n\n".getBytes());
		//self approval keylog
		KeyLogAction keylogSelfApproval = KeyLogAction.buildKeyLogAction(KeyLogAction.APPROVAL, masterkey, masterkey.getKeyID(), masterkey.getCurrentIdentity());
		keylogSelfApproval.uploadToKeyServer(client, masterkey);out.write("\n\n\n\n\n".getBytes());
		
		client.requestKeyLogs(masterkey.getKeyID(),masterkey);out.write("\n\n\n\n\n".getBytes());
		
		client.requestMasterPubKeys(idemail);out.write("\n\n\n\n\n".getBytes());
		client.requestIdentities(masterkey.getKeyID(),null);out.write("\n\n\n\n\n".getBytes());
		client.requestIdentities(masterkey.getKeyID(),masterkey);out.write("\n\n\n\n\n".getBytes());
		client.requestPublicKey(masterkey.getKeyID());out.write("\n\n\n\n\n".getBytes());
		client.requestSubKeys(masterkey.getKeyID());out.write("\n\n\n\n\n".getBytes());
		client.requestKeyStatus(masterkey.getKeyID());
				
		client.putRevokeSubKeyRequest(subkey, masterkey, "test subekey revocation");out.write("\n\n\n\n\n".getBytes());
		client.putRevokeMasterKeyRequest(revokekey, masterkey, "test masterkey revocation");out.write("\n\n\n\n\n".getBytes());
		
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
