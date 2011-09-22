package org.fnppl.opensdx.uploadserver;

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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

import org.fnppl.opensdx.http.HTTPClient;
import org.fnppl.opensdx.http.HTTPClientRequest;
import org.fnppl.opensdx.http.HTTPClientResponse;
import org.fnppl.opensdx.security.*;
import org.fnppl.opensdx.xml.*;

public class UploadClient extends HTTPClient {
	
	public static int OSDX_UPLOADSERVER_DEFAULT_PORT = 8899;
	public final static String ERROR_WRONG_RESPONE_FORMAT = "ERROR: Wrong format in uploadserver's response.";
	
	private String prepath;
	private KeyVerificator keyverificator = null;
	
	
	public boolean upload(File f, OSDXKey mySigning, OSDXKey myCrypt) throws Exception {
		
		log = System.out;
		
		//PART I::upload request
		Element requestElement = new Element("upload_file_request");
		requestElement.addContent("filename", f.getName());
		Signature s = Signature.createSignature(f, mySigning);
		requestElement.addContent(s.toElement());
		Element eCryptKey = new Element("encrypt_with_key");
		eCryptKey.addContent(myCrypt.getSimplePubKeyElement());
		requestElement.addContent(eCryptKey);
		
		HTTPClientRequest req = new HTTPClientRequest();
		req.setURI(host, prepath+"/request_upload");
		
		req.setContentElement(requestElement);
		
		HTTPClientResponse resp = send(req);
		writeLog(req, resp, "UPLOAD FILE");
		
		//PART II::send file data
		Element encryptedResponseElement = resp.doc.getRootElement();
		OSDXMessage msg = OSDXMessage.fromElement(encryptedResponseElement);
		
		Element responseElement = msg.getDecryptedContent(myCrypt);
		Document.buildDocument(responseElement).output(System.out);
		if (responseElement==null || !responseElement.getName().equals("upload_file_encryption_key")) {
			message = ERROR_WRONG_RESPONE_FORMAT;
			return false;
		}
		
		byte[] iv = SecurityHelper.HexDecoder.decode(responseElement.getChildText("init_vector"));
		byte[] key_bytes = SecurityHelper.HexDecoder.decode(responseElement.getChildText("key_bytes"));
		String token_id = responseElement.getChildTextNN("token_id");
		
		SymmetricKey enc = new SymmetricKey(key_bytes, iv);
		
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		FileInputStream fin = new FileInputStream(f);
		enc.encrypt(fin, bOut);
		
		HTTPClientRequest reqFileData  = new HTTPClientRequest();
		reqFileData.setURI(host, prepath+"/file_data");
		reqFileData.addHeaderValue("token_id", token_id);
		reqFileData.setContentEncryptedData(bOut.toByteArray());
		
		HTTPClientResponse respFileData = send(reqFileData);
		
		writeLog(reqFileData, respFileData, "UPLOAD FILE DATA");
		
	    return false;
	}
	
	public UploadClient(String host, int port, String prepath, KeyVerificator keyverificator) {
		super(host, port);
		this.prepath = prepath;
		this.keyverificator = keyverificator;
	}
	
	public void setKeyVerificator(KeyVerificator keyverificator) {
		this.keyverificator = keyverificator;
	}

	public static void main(String[] args) {
		try {
			//OSDXKey mysigning = buildDebugKey("localhost", "test", "test");
//			OSDXKey mycrypt = ((MasterKey)mysigning).buildNewSubKeyfromKeyPair(AsymmetricKeyPair.generateAsymmetricKeyPair());
//			mycrypt.setUsage(OSDXKey.USAGE_CRYPT);
//			mycrypt.createLockedPrivateKey("password", "password");
//			System.out.println(mycrypt.getJavaCodeString());
			
			OSDXKey mysigning = OSDXKey.fromElement(Document.fromString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>  <keypair><identities><identity><identnum>0001</identnum><email>test</email><mnemonic restricted=\"true\">test</mnemonic><sha256>2D:83:44:CA:3A:4C:85:3A:FB:E9:A3:15:D1:B4:70:BA:CC:7C:16:C7:DC:80:D9:AF:4F:E5:3D:74:4A:58:47:CE</sha256></identity></identities><sha1fingerprint>27:61:62:78:C1:29:F3:C6:A9:03:44:D2:18:36:37:22:E2:9F:63:BF</sha1fingerprint><authoritativekeyserver>localhost</authoritativekeyserver><datapath><step1><datasource>LOCAL</datasource><datainsertdatetime>2011-06-08 04:42:04 GMT+00:00</datainsertdatetime></step1></datapath><valid_from>2011-06-08 04:42:04 GMT+00:00</valid_from><valid_until>2036-06-07 10:42:04 GMT+00:00</valid_until><usage>ONLYSIGN</usage><level>MASTER</level><parentkeyid /><algo>RSA</algo><bits>3072</bits><modulus>00:88:FA:94:53:C5:EC:A9:31:63:FD:20:E3:38:A6:C8:B7:7F:32:41:4B:84:07:A1:AF:81:48:21:6F:D8:87:58:40:BF:DC:1A:E5:BD:A9:8E:ED:90:84:ED:84:BB:2E:04:FB:4F:33:F9:46:8B:0D:D0:58:F5:85:5C:F7:43:93:60:3A:BF:10:4B:92:65:DF:86:13:11:C0:6A:46:6F:4B:68:C3:5B:C3:48:BF:8E:16:00:36:68:A5:0E:C6:03:3B:87:7C:49:0C:18:FE:34:DA:78:03:F8:4B:B3:22:B9:D5:63:D8:74:B8:88:4C:E9:4D:A4:CE:A5:7C:09:B2:19:55:77:55:81:4C:FF:76:CD:87:69:B9:D4:B8:53:BE:9C:07:52:ED:53:09:D7:66:82:FC:A5:0A:79:2B:0D:06:5A:A7:76:77:F9:C4:27:B3:FD:BB:4A:80:44:8F:83:0F:DA:7E:A1:E7:22:24:D0:CC:EA:B2:F0:7F:03:BF:FC:FA:BB:B5:2D:17:63:40:1F:78:41:34:E4:ED:5A:F7:A2:1A:C5:75:FC:0F:93:44:95:AD:09:0D:10:90:D1:77:6B:D4:02:3C:8B:43:0B:91:3C:F5:F9:4A:94:0C:D4:EC:DD:2B:56:D4:AB:B9:C1:A4:74:AC:85:2E:6C:7C:AE:21:17:11:41:CB:9D:1C:16:98:1A:4F:03:8A:34:80:C5:2E:F6:E8:29:DB:3F:1C:EA:B1:B7:21:A9:5F:FA:93:D3:47:FA:DD:28:8F:4F:AA:53:1B:16:32:61:3B:B2:41:0E:37:DB:16:5B:14:AA:A9:D4:6C:C3:3E:0E:8D:90:B9:C4:83:C1:A6:6A:BF:E0:7F:56:AF:7D:7F:47:E5:4E:9C:8E:E9:E4:27:06:F9:0A:8A:22:7A:85:2D:FE:B2:AF:10:EC:5B:36:E7:96:60:E2:77:C5:9F:78:B9:51:A4:CE:7C:1D:D3:43:BF:4F:B1:2C:3F:DF:30:04:B6:7E:40:7E:F3:0E:F1:12:42:78:C6:4A:07</modulus><pubkey><exponent>01:00:01</exponent></pubkey><privkey><exponent><locked><mantraname>mantra: password</mantraname><algo>AES@256</algo><initvector>6F:67:A3:5C:C0:5A:67:F6:30:32:9A:0E:1E:3A:8B:1B</initvector><padding>CBC/PKCS#5</padding><bytes>12:9A:B4:1C:1F:8D:8B:88:39:CD:CD:C4:C9:4D:BE:65:56:2C:48:40:E8:3A:ED:09:F3:BF:0D:A7:A8:09:77:B7:C1:15:FB:8C:93:57:B6:38:F3:31:9B:A8:1E:21:27:40:9E:93:E9:4A:1F:B1:41:02:CF:40:96:2C:A6:17:2B:48:68:58:70:AD:B7:E1:52:6F:09:19:11:67:59:BC:1F:FD:BE:88:C0:B2:FF:76:34:EF:1B:26:DA:9F:4C:47:66:0E:87:BB:1C:09:CB:F2:77:BC:CB:AE:89:CA:C4:65:98:DA:D1:6E:ED:22:08:70:FC:BB:E2:CC:41:7F:5C:12:7B:A6:D3:32:73:FA:BB:E2:95:A6:1C:34:3E:FD:A8:90:D0:9A:0B:4E:96:06:89:DD:6F:35:02:E5:FB:CA:0A:E7:0D:2E:A1:B3:81:17:DE:8D:7F:96:F4:36:AA:02:4E:EF:C0:EF:56:37:C0:53:FB:B9:E3:C0:5B:69:9E:7C:EC:1F:A3:0B:C5:99:B7:5D:54:52:28:17:4A:B1:3D:C8:36:54:2A:94:0C:32:F7:1B:6A:11:37:91:B5:43:5D:BF:DB:6F:D3:B4:37:18:32:81:81:C1:72:80:B6:95:0E:B0:61:FF:05:CE:FC:98:E5:F1:E4:D1:33:B7:EF:B8:EB:EF:6B:A7:FE:C6:37:77:CF:43:12:C3:5F:2B:2A:51:19:E8:C4:6D:F6:0E:15:C4:C3:AD:BE:4C:FE:D6:D5:3A:00:D8:E0:0B:00:78:A9:5F:D8:21:28:06:B8:74:F2:06:23:63:81:B8:CC:03:EC:2C:ED:6B:74:23:E4:31:C1:4E:9C:B2:24:F0:93:A4:7D:6C:6A:E2:C1:95:EC:EA:DF:DC:85:2B:60:15:24:DE:FD:DD:94:BF:CF:C9:8D:74:DE:8A:D8:89:DC:16:FA:9D:28:37:EE:65:44:AD:61:FB:33:D4:E8:66:D3:BA:D8:38:E1:16:F1:EF:97:FF:01:D7:C3:7D:76:CE:A2:12:1C:24:AC:EC:AE:AC:2B:08:99:7A:D8:A4:41:49:A4:0E:AF:83</bytes></locked></exponent></privkey><gpgkeyserverid /></keypair>  ").getRootElement());
			OSDXKey mycrypt = OSDXKey.fromElement(Document.fromString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>  <keypair><sha1fingerprint>83:16:8A:C4:97:0F:9C:6A:E5:3F:F9:F5:DF:87:8D:E1:EA:94:E1:D5</sha1fingerprint><authoritativekeyserver>localhost</authoritativekeyserver><datapath><step1><datasource>LOCAL</datasource><datainsertdatetime>2011-06-08 14:32:45 GMT+00:00</datainsertdatetime></step1></datapath><valid_from>2011-06-08 14:32:45 GMT+00:00</valid_from><valid_until>2036-06-07 20:32:45 GMT+00:00</valid_until><usage>ONLYCRYPT</usage><level>SUB</level><parentkeyid>27:61:62:78:C1:29:F3:C6:A9:03:44:D2:18:36:37:22:E2:9F:63:BF@localhost</parentkeyid><algo>RSA</algo><bits>3072</bits><modulus>00:9F:0E:67:EF:48:C3:59:48:71:F0:9E:8D:41:A4:44:3D:A5:3B:14:0B:35:C8:EE:95:6F:C6:9B:35:DE:F1:2F:BA:D6:97:BB:34:BE:92:F1:7F:A8:B9:D2:27:87:64:02:21:6E:32:DF:AD:E1:C7:66:DA:1D:71:75:07:E7:AC:7E:56:2D:EC:B9:F8:67:A2:66:98:82:61:83:1D:86:23:E4:2D:28:C2:6E:A3:F5:1D:9F:AA:24:A9:FD:84:3A:D8:1D:8B:DC:0B:EC:34:04:6B:6B:57:58:21:47:5C:41:2E:09:15:79:08:7F:01:CC:AB:E4:28:1C:CE:D7:8F:D6:C6:7E:5C:CC:D4:E0:74:47:51:0D:40:0B:0B:DD:D3:03:8C:18:56:68:88:C4:B5:DC:48:BB:36:32:C6:4A:B3:EF:08:E6:81:3F:80:96:68:25:93:58:EE:76:8F:DB:3B:39:B0:9B:8E:29:40:67:8D:C5:02:1F:F1:4A:C5:6A:D0:2A:02:F8:5C:DD:B3:0A:8C:2B:04:A5:4A:AF:25:39:89:DB:D8:4A:7F:4A:4D:10:28:10:88:6D:A4:0B:31:50:D8:C7:2E:9E:3F:EF:C8:A0:D0:19:97:EB:80:CE:DE:A0:1B:2D:4B:C7:D9:FA:39:8B:8E:10:D8:05:40:29:FD:71:EF:0D:7D:2B:8F:B6:2E:F5:FE:A7:51:84:22:CF:BA:CA:A8:63:78:E1:23:8E:F3:4D:D7:66:2F:6A:D5:CC:ED:AC:5D:35:86:E6:A9:9C:2D:7E:93:3F:77:87:6A:34:04:4D:58:97:6D:C1:67:B4:AE:17:5D:8D:75:5E:59:C7:18:82:AA:51:C7:E1:27:24:90:FE:3E:9D:1D:39:83:5A:27:61:BC:89:03:47:04:53:D6:58:0B:D1:A7:97:D3:6E:BD:BE:B6:0E:40:9D:87:76:C7:11:4A:A6:39:68:23:FB:11:21:96:43:34:C2:7F:09:C4:F4:D4:59:B0:10:01:04:9B:0F:CA:5D:FF:01:57</modulus><pubkey><exponent>01:00:01</exponent></pubkey><privkey><exponent><locked><mantraname>password</mantraname><algo>AES@256</algo><initvector>44:18:F8:24:D3:B5:FB:01:DD:DE:96:90:4D:2B:A9:E8</initvector><padding>CBC/PKCS#5</padding><bytes>93:2F:3D:72:BC:29:D5:5F:CA:01:3A:08:90:E4:5C:43:E1:69:A0:0D:41:23:18:F5:E9:5C:D1:54:68:2B:84:5E:B2:9B:0B:F4:C6:D7:C9:CE:68:FC:37:10:75:B5:11:47:4D:D8:42:D6:9D:70:14:81:75:29:17:C7:0A:26:19:25:99:FA:46:C4:F8:BE:56:B1:31:F7:BF:22:5F:3D:7F:D7:AB:E5:F0:86:18:71:C4:7C:EC:24:77:61:28:E0:7B:27:6B:A3:45:3E:50:9E:F4:03:F5:E3:68:D7:DF:1D:D3:F9:9F:1B:21:FC:4C:6B:DA:3A:65:27:00:94:43:50:14:C0:92:F9:6E:02:DC:BA:56:42:91:2B:5B:6D:FE:15:41:CF:E4:B6:4C:47:F4:27:03:9F:59:65:CC:62:55:93:D5:72:9C:B9:FF:3B:26:D1:0A:20:B4:5B:5D:21:9A:E0:8A:CD:A3:A4:FE:14:8A:42:56:59:14:8E:79:05:33:09:F3:F1:85:8A:51:5C:22:7B:BD:AA:60:E9:A2:4D:85:98:75:BE:C5:F4:30:91:58:AA:A4:F5:AB:5B:BD:E0:D4:1E:A0:25:5C:D2:EC:1A:F5:9B:23:74:1A:14:5C:7E:ED:0C:1E:E0:65:83:46:F1:D4:F2:E7:E0:52:5C:81:A7:93:D5:F6:C7:3C:66:83:13:BF:E3:B7:32:D6:06:1F:4E:22:27:CE:90:1E:A0:2F:66:76:6A:ED:E7:1B:A9:45:49:28:F0:75:AC:15:DA:EE:8C:01:78:50:C3:70:53:0C:89:7A:FF:FE:BA:28:8A:D8:6E:45:D4:EC:93:7F:3B:EE:22:6C:5D:0E:A8:D6:9D:61:6F:B1:62:C5:46:10:3E:AC:6B:F9:9A:A7:87:EC:D7:7C:8B:8A:8E:F1:70:81:52:8C:2E:83:7C:1C:A7:72:FF:7D:02:82:46:BA:E1:4F:0F:57:CF:A8:C1:61:7F:DC:6B:CC:C1:97:57:95:B8:BE:46:A2:8B:53:D6:32:E7:5C:DC:74:5B:0C:94:79:33:D9:3B:6A:CD:B7:52:C0:1F</bytes></locked></exponent></privkey><gpgkeyserverid /></keypair>  ").getRootElement());
			mysigning.unlockPrivateKey("password");
			mycrypt.unlockPrivateKey("password");
			
			UploadClient uc = new UploadClient("localhost", 8899, "", null);
			uc.upload(new File("README"), mysigning, mycrypt);
		} catch (Exception ex)  {
			ex.printStackTrace();
		}
	}
	
}
