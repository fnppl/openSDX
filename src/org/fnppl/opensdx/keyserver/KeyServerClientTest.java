package org.fnppl.opensdx.keyserver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Vector;

import org.fnppl.opensdx.security.KeyClient;
import org.fnppl.opensdx.security.KeyClientMessageFactory;
import org.fnppl.opensdx.security.KeyClientRequest;
import org.fnppl.opensdx.security.KeyLog;
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
	
		//init test data
		MasterKey masterkey = (MasterKey)OSDXKey.fromElement(Document.fromString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<keypair><identities><identity><identnum>0001</identnum><email>debug@it-is-awesome.de</email><mnemonic>debug key set</mnemonic><country>DE</country><region /><city /><postcode /><company /><unit /><subunit /><function /><surname>Boedeker</surname><middlename /><name>Bertram</name><phone /><note /><sha1>80:4A:8D:81:18:66:15:47:BD:72:D8:EE:3C:B3:E3:81:1F:49:BC:6E</sha1><datapath /></identity></identities><sha1fingerprint>3C:02:78:36:55:2C:82:3C:10:30:3C:85:3C:C1:DB:4F:8C:8D:80:56</sha1fingerprint><authoritativekeyserver>LOCAL</authoritativekeyserver><authoritativekeyserver_port>8889</authoritativekeyserver_port><datapath><step1><datasource>LOCAL</datasource><datainsertdatetime>2011-04-20 08:20:06 GMT+00:00</datainsertdatetime></step1></datapath><valid_from>2011-04-20 08:20:06 GMT+00:00</valid_from><valid_until>2036-04-19 14:20:06 GMT+00:00</valid_until><usage>ONLYSIGN</usage><level>MASTER</level><parentkeyid /><algo>RSA</algo><bits>3072</bits><modulus>00:98:89:BC:D3:55:AD:56:2A:6E:64:4B:FD:D7:56:A7:4D:A6:04:ED:85:A8:39:80:89:D1:7B:60:79:46:10:98:5C:69:26:C0:F7:A7:29:F2:45:0B:82:7D:DB:8D:8E:11:46:6B:A7:AE:54:5D:82:7C:32:61:21:0D:5F:F9:81:8C:FB:58:B7:8B:11:1D:96:36:D8:DE:62:2A:5F:67:3B:8F:05:C6:D5:DC:7E:81:7E:22:CB:5C:BD:06:62:68:0C:05:22:F8:6C:48:56:1F:1B:2C:6E:B1:B7:28:EB:BC:14:6E:74:AA:5E:11:57:FE:B7:49:4E:92:0F:85:E7:97:3C:B9:69:67:0D:B1:E4:63:FC:45:41:C2:5C:68:48:6D:7A:AD:DE:8E:CA:C8:9D:55:BB:63:FB:4D:61:1A:4F:3B:92:DB:94:07:0E:0A:D9:15:85:DE:A5:EC:E0:96:4D:89:C4:63:2B:5B:09:6C:71:CB:5A:34:4F:A8:CC:45:DA:11:C4:BD:33:C9:4E:44:8C:B9:8F:E8:47:F2:82:60:1B:1A:A2:FA:0C:E2:BF:8D:06:33:1D:2D:D1:79:D2:1D:49:37:41:51:3F:E8:4E:A7:9E:E0:C0:78:14:ED:6D:16:21:0D:3E:3A:1D:CA:69:9F:F2:BD:07:98:D7:DF:1C:48:1E:3F:75:A6:32:B2:94:3E:FE:9F:F0:D7:6A:74:17:9C:83:02:5B:28:11:F6:F6:28:27:79:D2:21:A7:56:88:9F:01:5D:56:E6:5E:01:A3:1D:46:48:D6:82:B5:6A:BF:E5:65:A5:F3:BA:15:7A:CA:AF:98:02:E4:A5:7A:BF:C7:FA:9E:33:CB:D9:C1:2C:38:1E:58:76:12:6A:D2:3C:58:4E:A1:26:BA:83:A8:61:92:3C:DE:E5:22:19:42:C4:CD:43:EE:47:55:6A:92:3E:72:2F:17:22:00:14:98:D1:F1:DD:C5:B2:6D:AF:8C:9E:CB:99:79:77:7D:1E:E7:DA:EE:41:A1:7A:0F:C1:59</modulus><pubkey><exponent>01:00:01</exponent></pubkey><privkey><exponent><locked><mantraname>master</mantraname><algo>AES@256</algo><initvector>D0:F1:9F:14:B5:C5:4F:F2:95:07:17:BF:25:20:FB:5F</initvector><padding>CBC/PKCS#5</padding><bytes>16:CD:78:4F:A9:47:40:CA:11:5C:D3:7C:99:38:93:09:CB:9B:3F:A7:7F:55:7B:E8:CC:C5:56:27:D5:B0:CD:DD:10:E2:48:B1:4D:0E:08:36:64:29:0F:FA:67:C7:40:36:D8:9E:A2:1F:BE:27:1D:B2:D9:B4:59:29:32:58:07:79:CB:C6:D6:F0:57:43:0B:97:D9:07:6D:AB:2A:41:89:29:52:DC:DF:B8:0A:90:9F:CB:C1:28:2F:B4:2B:AE:16:46:A0:AF:00:8F:AA:26:6D:0B:E1:0C:57:48:3F:24:D6:7A:F5:92:E6:3B:F6:C1:37:7F:6B:E3:D6:60:B9:5C:07:F2:CA:6D:0E:A6:06:E7:BA:50:5E:1A:CE:B8:E5:4F:E4:D2:AD:5E:60:D3:3D:5C:B8:EE:A8:83:56:6B:95:AE:63:76:52:03:16:12:D6:EB:5C:82:73:3A:1B:AF:2C:F4:0C:11:CE:0A:36:0E:1E:AF:9A:83:F6:69:B2:1F:5A:C2:35:76:3B:80:BD:A3:40:E1:EF:6D:52:9C:1E:2F:ED:5C:49:7A:D4:2C:EE:31:CB:4B:AC:96:F7:FE:5F:A4:18:E7:7E:9E:5E:86:1E:CC:62:8F:3C:B3:23:77:C8:CB:0D:B1:46:76:9B:85:02:10:C3:38:EF:10:B1:2E:13:4F:C8:FB:0B:12:BA:AC:C2:FA:A3:F2:37:5A:35:25:42:BF:3B:A1:72:94:2F:BB:D0:2D:74:43:BF:2E:29:1A:A1:9E:0D:A1:0B:03:22:5A:F1:23:79:6D:3E:EC:62:C0:18:A8:8E:11:51:26:C2:A1:59:7C:45:5A:7E:47:5F:17:8F:30:57:5F:14:8B:49:D7:6A:9F:28:76:DE:66:DE:4E:98:F9:23:8D:FC:0F:E0:F2:B3:D0:B9:77:EB:19:2C:0B:59:DA:53:42:D1:21:4E:FD:17:A3:9B:29:12:45:D1:B9:AB:0A:FB:CB:38:71:B3:5B:85:23:8B:85:18:13:09:05:42:B0:06:63:48:8F:9D:AE:48:D2:41:98:89:FC:4E:B0:EC:3D:06:07:CB:D5</bytes></locked></exponent></privkey><gpgkeyserverid /></keypair>").getRootElement());
		RevokeKey revokekey = (RevokeKey)OSDXKey.fromElement(Document.fromString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<keypair><sha1fingerprint>1A:88:F5:D2:AB:56:2C:B1:6F:1A:FA:DF:00:19:93:1C:BE:F1:EE:24</sha1fingerprint><authoritativekeyserver>LOCAL</authoritativekeyserver><authoritativekeyserver_port>8889</authoritativekeyserver_port><datapath><step1><datasource>LOCAL</datasource><datainsertdatetime>2011-04-20 08:20:14 GMT+00:00</datainsertdatetime></step1></datapath><valid_from>2011-04-20 08:20:14 GMT+00:00</valid_from><valid_until>2036-04-19 14:20:14 GMT+00:00</valid_until><usage>ONLYSIGN</usage><level>REVOKE</level><parentkeyid>3C:02:78:36:55:2C:82:3C:10:30:3C:85:3C:C1:DB:4F:8C:8D:80:56@LOCAL</parentkeyid><algo>RSA</algo><bits>3072</bits><modulus>00:8D:1B:0D:9C:DA:5C:ED:A2:CD:17:DA:5F:6E:D5:7D:01:2F:E2:E5:20:E6:D1:2C:A1:D9:C9:F4:EF:0B:2C:86:29:F6:BA:D3:42:0A:8C:06:3D:2D:4A:3B:94:5E:35:2B:FD:53:A0:B9:0C:80:87:72:71:6B:DA:2D:DE:16:B5:59:5A:09:E0:D7:19:A2:17:E6:F9:D1:4F:3A:21:33:89:73:F7:F4:ED:0F:F6:63:C6:1B:3C:F3:9C:3A:3B:32:01:C1:E4:B2:A8:BE:0B:AD:74:98:D5:D9:EF:CF:60:85:EF:32:57:6F:89:D9:D3:C8:0A:CB:76:15:30:AB:C6:EC:1B:C8:6A:66:8D:F1:CB:9C:64:CD:29:3C:01:14:FB:82:30:43:4A:CC:48:2C:90:87:C8:4B:69:C8:D0:D5:BB:8D:0E:83:BF:DC:E0:77:3F:00:CC:12:03:F9:D6:98:6D:DF:1A:63:D8:89:3C:2D:B4:5E:E8:3E:06:F3:E3:03:8A:AB:32:2A:8F:AD:61:77:50:17:31:A9:6E:29:FA:EA:51:1C:B8:EC:59:2B:0A:91:2A:AE:4F:A1:CD:CC:04:EE:E4:90:7E:FC:39:B3:72:50:F9:25:D7:E9:0D:4B:18:DA:C6:9B:BF:F7:DB:6D:78:44:AB:B6:6C:CB:0D:E3:E5:97:C2:D6:CA:DE:C0:88:79:87:5C:83:1F:AB:CF:7B:6F:4A:30:33:BA:AB:EB:B3:88:5C:08:7E:CB:00:CC:72:8D:C5:F4:AE:61:27:DA:05:34:70:C2:D6:20:77:5E:C1:64:1A:87:E0:1E:F5:F8:F6:4D:C7:C5:04:66:F8:F1:58:4D:AA:BB:55:D5:60:ED:DD:D7:47:6B:3A:2D:FE:6B:83:F5:C2:67:62:44:3E:4E:94:5A:EE:D3:B4:0A:7F:CE:3E:FC:19:40:E0:A1:B6:F1:75:BA:7D:FB:D9:BD:8A:CC:35:FB:E5:BD:D8:31:D1:C0:A4:78:89:D0:E1:9D:4D:4E:A5:32:5D:BC:C4:C2:88:7F</modulus><pubkey><exponent>01:00:01</exponent></pubkey><privkey><exponent><locked><mantraname>revoke</mantraname><algo>AES@256</algo><initvector>4D:88:A7:2A:D8:7F:6B:92:80:EB:5B:7F:D4:97:A0:5C</initvector><padding>CBC/PKCS#5</padding><bytes>8A:61:8A:3C:62:6F:2A:68:C4:4D:5C:FF:CC:C5:B1:2C:3F:AC:7A:AD:A1:62:71:1B:F1:9E:DA:E6:67:99:59:3D:2C:A9:28:B6:D5:47:02:1F:B3:FD:35:23:55:3F:84:C4:1A:6B:84:26:2E:4E:2F:33:33:3F:AD:40:86:F0:E5:8C:20:36:F8:0B:B5:82:11:B0:27:21:6D:19:EC:DC:A6:9E:FD:9F:F5:CD:ED:74:E9:0D:D6:6B:68:FD:04:B1:D0:80:28:43:EE:82:E1:DA:AB:B1:FD:4B:B2:E5:B0:5B:0A:74:23:68:3D:19:2F:C9:32:37:E1:3D:27:7D:11:CA:C4:D1:B6:34:31:43:4D:4D:9F:69:31:79:07:36:3A:8A:74:B9:50:34:96:25:75:3C:CE:87:F0:89:5B:E9:48:40:84:4A:58:33:42:B5:27:F5:26:B6:B9:67:24:E3:42:7C:07:40:4E:88:97:6A:6C:5F:21:F2:32:82:38:B6:8A:67:41:0E:AF:3A:D1:8A:41:CA:E8:4E:F0:2C:17:AC:BB:FC:B5:85:8B:F7:93:5D:BC:AB:54:6A:12:88:FD:2C:8D:9B:70:A7:0C:B8:54:42:2D:3E:09:92:0F:91:A9:E8:11:8E:DF:28:A9:58:1C:CA:BF:83:17:D8:09:2C:76:8E:74:5C:32:48:F7:DD:18:31:A8:DF:A3:8C:43:ED:E9:1A:44:B7:B0:78:20:BC:08:74:A9:8B:21:D3:3F:17:09:22:45:D4:55:2C:A7:D5:26:D0:49:BF:E4:4A:00:69:25:85:EF:20:97:8E:B8:DC:79:E2:2E:64:7D:D0:45:E1:99:31:F4:55:8E:79:79:BE:12:CD:2C:26:EB:CD:2D:19:94:D5:33:FF:64:0A:38:F3:52:C2:09:25:06:29:CA:03:14:18:99:D5:41:9D:E6:EA:85:44:52:F4:D6:B5:7A:F5:5B:70:68:3C:18:00:B4:64:66:49:10:9A:07:09:BE:90:86:9B:C2:FF:02:78:AF:0A:53:DD:12:21:ED:C7:BD:6B:39:49:D1:7C:E9:AE</bytes></locked></exponent></privkey><gpgkeyserverid /></keypair>").getRootElement());
		SubKey subkey = (SubKey)OSDXKey.fromElement(Document.fromString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<keypair><sha1fingerprint>48:E5:C5:4A:45:B9:E5:59:A7:FB:15:91:24:0B:7C:48:33:57:BF:99</sha1fingerprint><authoritativekeyserver>LOCAL</authoritativekeyserver><authoritativekeyserver_port>8889</authoritativekeyserver_port><datapath><step1><datasource>LOCAL</datasource><datainsertdatetime>2011-04-20 08:20:19 GMT+00:00</datainsertdatetime></step1></datapath><valid_from>2011-04-20 08:20:19 GMT+00:00</valid_from><valid_until>2036-04-19 14:20:19 GMT+00:00</valid_until><usage>ONLYSIGN</usage><level>SUB</level><parentkeyid>3C:02:78:36:55:2C:82:3C:10:30:3C:85:3C:C1:DB:4F:8C:8D:80:56@LOCAL</parentkeyid><algo>RSA</algo><bits>3072</bits><modulus>00:85:80:5B:B5:82:6E:1F:E9:8F:B9:A9:8C:78:4D:9D:E5:C7:95:01:3C:13:9E:E0:61:33:88:AC:CD:86:AB:98:FA:DE:79:A3:B6:43:F4:20:A5:61:F1:33:EB:4A:21:57:AF:47:A9:5C:E5:C8:93:AE:6E:07:5C:2F:10:A6:EF:32:79:14:85:AD:52:24:2F:2D:D4:0C:AB:3C:31:46:67:1F:FA:DE:B0:EA:21:E8:76:41:65:89:40:78:22:6D:88:A6:32:12:55:A2:91:AB:22:44:CA:39:4C:3C:D8:A4:E2:B4:B7:D8:00:51:AA:41:D7:7B:55:39:7E:C0:79:9C:A9:37:3A:8D:3F:14:BF:05:F6:3F:8B:39:AE:C6:44:2C:7C:20:EB:FA:59:99:D1:AC:12:D2:29:71:E4:48:38:62:95:B3:91:49:4A:BC:B1:95:6F:CC:E6:41:11:8E:D4:99:F5:C4:D6:4E:F7:CD:22:6F:9F:B2:79:2E:BB:E3:00:7D:CC:E2:41:3B:FF:FD:08:7B:57:17:F5:2C:99:86:FF:7D:AB:2B:4B:08:6A:CE:64:09:BE:D8:AE:D3:65:A3:2C:1F:41:7A:E6:D6:2A:01:93:EF:63:D4:90:9C:57:4B:80:F3:9D:56:DA:CD:F0:A6:7B:DF:75:21:80:DC:BB:8F:E4:72:77:C7:A8:D8:4D:57:48:E5:52:37:A9:2A:9D:25:15:3D:9C:A6:3D:D7:15:F8:6B:3F:4B:6D:E9:68:C7:69:3A:F7:15:D2:96:4C:85:FC:BC:64:D7:FB:6C:98:4D:A6:99:4C:C7:90:9B:EF:1C:1B:C8:5A:48:7B:31:B9:D3:B3:BF:FB:24:7A:83:3E:E9:8C:D7:35:06:CA:69:E3:A7:03:89:34:20:A2:F9:85:D4:AD:A8:27:8C:66:C2:A6:9F:7F:C4:D3:A0:B5:D3:0A:C9:B3:37:75:BD:7C:1D:44:1A:9D:3C:73:90:EA:B8:88:16:27:30:6D:1F:CC:9F:66:6A:A7:13:31:8C:86:6B</modulus><pubkey><exponent>01:00:01</exponent></pubkey><privkey><exponent><locked><mantraname>sub</mantraname><algo>AES@256</algo><initvector>75:60:F1:88:58:82:F8:1B:2A:D9:3B:06:69:98:3F:81</initvector><padding>CBC/PKCS#5</padding><bytes>05:75:A8:53:3E:81:29:69:87:B2:09:5D:7F:72:38:C0:CF:70:F3:7B:F2:11:46:2E:B0:43:76:F0:00:59:FE:72:ED:3E:39:C9:67:8D:29:3C:3A:9A:51:E0:56:E1:D0:33:BA:5F:57:B6:DA:6A:70:29:B8:2D:ED:DC:DF:73:D4:99:A4:A6:73:99:BC:1F:06:BC:D7:35:AF:9B:C8:53:55:73:4F:8D:58:86:76:88:C6:1A:03:58:FA:5F:D5:AB:2D:FE:ED:EA:2C:B7:17:D6:D4:40:F8:78:F2:47:E9:B4:8E:AD:8A:8C:FE:F6:71:26:7B:7E:ED:4C:7D:27:8B:C9:D2:4A:53:FB:B7:23:81:C4:75:5B:AC:8D:3F:61:8C:3A:A9:6C:67:3F:B7:FA:4C:4E:70:3F:33:AF:CF:F6:4A:4C:AD:85:FF:9B:D8:9D:71:3D:EC:16:10:F5:BC:B1:7F:51:65:10:F0:43:FE:6B:4E:E8:17:5E:2D:3C:B3:FE:40:F0:B9:9B:65:50:F3:8C:66:19:37:D6:70:30:3B:F5:65:D4:8D:E4:76:FE:CF:C4:D5:5E:5C:91:48:16:CA:F3:D3:C4:02:C5:1D:BD:13:C7:4C:B2:6A:1F:BA:77:BC:84:2B:86:5D:4A:36:86:D1:CD:13:A5:1F:E0:33:80:BE:4F:52:F8:11:81:3F:B8:6C:BC:79:3F:DA:A0:C4:8E:5B:18:D8:AB:9E:87:E0:A2:0F:75:59:26:9F:1B:79:3F:3E:B3:64:F1:A6:F7:79:82:1B:48:70:EF:09:01:5D:9C:D8:56:19:73:F3:53:7F:8F:1E:AD:9C:65:16:E9:28:09:6F:20:12:82:D3:6B:A5:DA:E1:F7:10:00:02:8A:D5:93:F1:A0:1E:6F:10:DF:79:8F:54:E4:A1:D4:69:44:2A:8A:BC:6D:E3:94:0B:C8:B4:85:A9:92:3C:ED:9A:85:BE:E6:34:33:36:2A:E9:81:6E:5E:58:D9:A0:E2:D5:47:35:CD:77:BA:5D:CA:0B:77:93:48:F7:2D:68:CA:63:AA:8A:8C:3E:F0:26:F4:54:0C</bytes></locked></exponent></privkey><gpgkeyserverid /></keypair>").getRootElement());
		masterkey.addRevokeKey(revokekey);
		masterkey.addSubKey(subkey);
		masterkey.unlockPrivateKey("master");
		revokekey.unlockPrivateKey("revoke");
		subkey.unlockPrivateKey("sub");
		
		Vector<KeyLog> keylogs = null;
		
		//testing:: output all requests
		
		//String host = "it-is-awesome.de";
		String host = "localhost";
		int port = 8889;
		
		KeyClient client = new KeyClient(host, port);
		OutputStream out = System.out;
		out = new FileOutputStream(new File("test_keyserver_client.txt"));
		
		client.log = out;
		
		masterkey.setAuthoritativeKeyServer(host, port);
		
		String idemail = masterkey.getIdentity0001().getEmail();
		
		KeyClientRequest req;
		
		client.connect();
		
		boolean ok;
		
		KeyServerIdentity keyserverID = client.requestKeyServerIdentity();
		KeyVerificator.addRatedKey(keyserverID.getKnownKeys().get(0), TrustRatingOfKey.RATING_MARGINAL);
		
		ok = client.putMasterKey(masterkey, masterkey.getIdentity0001()); out.write("\n\n\n\n\n".getBytes());
		ok = client.putRevokeKey(revokekey, masterkey);out.write("\n\n\n\n\n".getBytes());
		ok = client.putSubKey(subkey, masterkey);out.write("\n\n\n\n\n".getBytes());
		
		client.requestKeyLogs(masterkey.getKeyID());out.write("\n\n\n\n\n".getBytes());
		
		//self approval keylog
		KeyLog keylogSelfApproval = KeyLog.buildKeyLogAction(KeyLog.APPROVAL, masterkey, masterkey.getKeyID(), masterkey.getIdentity0001());
		client.putKeyLog(keylogSelfApproval, masterkey);out.write("\n\n\n\n\n".getBytes());
		
		client.requestMasterPubKeys(idemail);out.write("\n\n\n\n\n".getBytes());
		client.requestIdentities(masterkey.getKeyID());out.write("\n\n\n\n\n".getBytes());
		client.requestPublicKey(masterkey.getKeyID());out.write("\n\n\n\n\n".getBytes());
		client.requestSubKeys(masterkey.getKeyID());out.write("\n\n\n\n\n".getBytes());
		client.requestKeyStatus(masterkey.getKeyID());
				
		client.putRevokeMasterKeyRequest(revokekey, masterkey, "test revocation");out.write("\n\n\n\n\n".getBytes());
		
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
