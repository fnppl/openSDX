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

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.util.*;

import org.fnppl.opensdx.security.*;
import org.fnppl.opensdx.tsas.*;
import org.fnppl.opensdx.xml.*;

public class OSDXKeyServerClient {
	private Socket socket = null;
	private long timeout = 2000;
	private String host = null;
	private int port = -1;
	private String message = null;
	
	public OSDXKeyServerClient(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public boolean connect() throws Exception {
		socket = new Socket(host, port);
		if (socket.isConnected()) {
			System.out.println("Connection established.");
			return true;
		} else {
			System.out.println("ERROR: Connection to server could NOT be established!");
			return false;
		}
	}

	public void close() throws Exception {
		if (socket != null)
			socket.close();
	}
	
	public String getMessage() {
		return message;
	}
	public OSDXKeyServerClientResponse send(OSDXKeyServerClientRequest req) throws Exception {
		if (!connect()) {
			throw new RuntimeException("ERROR: Can not connect to keyserver.");
		}
		System.out.println("OSDXKeyServerClient | start "+req.getURI());
		//req.toOutput(System.out);
		req.send(socket);
		
		//processing response
	    System.out.println("OSDXKeyServerClient | waiting for response");
	    BufferedInputStream bin = new BufferedInputStream(socket.getInputStream());
	    
	    OSDXKeyServerClientResponse re = OSDXKeyServerClientResponse.fromStream(bin, timeout);
	    close();
	    
	    if(re == null) {
	    	throw new RuntimeException("ERROR: Keyserver does not respond.");
	    }
	    return re;
	}
	
	// 1. Ich, als fremder user, möchte beim keyserver (z.B. keys.fnppl.org) den/die (MASTER) pubkey(s) zu der identity thiess@finetunes.net suchen können
	public Vector<String> requestMasterPubKeys(final String idemail) throws Exception {
		OSDXKeyServerClientRequest req = OSDXKeyServerClientRequest.getRequestMasterPubKeys(host, idemail);
		OSDXKeyServerClientResponse resp = send(req);
		
		Element e = resp.doc.getRootElement();
		if (!e.getName().equals("masterpubkeys_responset")) {
			throw new RuntimeException("ERROR: Wrong format in keyserver's response");
		}
		Vector<String> ret = new Vector<String>();
		Vector<Element> keyids = e.getChildren("keyid");
		for (Element k : keyids) {
			ret.add(k.getText());
		}
	 	//TODO verify signature
		return ret;
	}
	
	
//	public Vector<OSDXKeyObject> requestPubKeys(final String idemail) throws Exception {
//		OSDXKeyServerClientRequest req = OSDXKeyServerClientRequest.getRequestPubKeys(host, idemail);
//		OSDXKeyServerClientResponse resp = send(req);
//		
//		Element e = resp.doc.getRootElement();
//		if (!e.getName().equals("pubkeys")) {
//			throw new RuntimeException("ERROR: Wrong format in keyserver's response");
//		}
//		Vector<OSDXKeyObject> ret = new Vector<OSDXKeyObject>();
//		Vector<Element> pks = e.getChildren("keypair");
//		for (Element pk : pks) {
//			OSDXKeyObject key = OSDXKeyObject.fromElement(pk);
//			ret.add(key);
//		}
//		return ret;
//	}
	
	
	//2. Ich, als fremder user, möchte beim keyserver die weiteren identities (identity-details) zu einem pubkey bekommen können
	public Vector<Identity> requestIdentities(String keyid) throws Exception {
		OSDXKeyServerClientRequest req = OSDXKeyServerClientRequest.getRequestIdentities(host, keyid);
		OSDXKeyServerClientResponse resp = send(req);
		
	    Element e = resp.doc.getRootElement();
	    if (!e.getName().equals("identities_response")) {
	    	resp.doc.output(System.out);
	    	throw new RuntimeException("ERROR: Wrong format in keyserver's response");
	    }
	    Vector<Identity> ret = new Vector<Identity>();
	 	Vector<Element> eid = e.getChildren("identity");
	 	for (Element id : eid) {
	 		ret.add(Identity.fromElement(id));
	 	}
	 	//TODO verify signature
	    return ret;
	}
	
	//3. Ich, als fremder user, möchte beim keyserver den aktuellen (beim keyserver bekannten) status zu einem pubkey bekommen können (valid/revoked/etc.)
	public KeyStatus requestKeyStatus(String keyid) throws Exception {
		OSDXKeyServerClientRequest req = OSDXKeyServerClientRequest.getRequestKeyStatus(host, keyid);
		OSDXKeyServerClientResponse resp = send(req);
		if (resp==null || resp.status == null) return null;
		
		Element e = resp.doc.getRootElement();
		if (!e.getName().equals("keystatus_response")) {
			throw new RuntimeException("ERROR: Wrong format in keyserver's response");
		}
		KeyStatus ks = KeyStatus.fromElement(e);
		//TODO verify signature		
		
		return ks;
	}
	
	//4. Ich, als fremder user, möchte beim keyserver die keylogs eines (beliebigen) pubkeys bekommen können
	public Vector<KeyLog> requestKeyLogs(String keyid) throws Exception {
		OSDXKeyServerClientRequest req = OSDXKeyServerClientRequest.getRequestKeyLogs(host, keyid);
		OSDXKeyServerClientResponse resp = send(req);
		if (resp==null || resp.status == null) return null;
		
		Element e = resp.doc.getRootElement();
		if (!e.getName().equals("keylogs_response")) {
			throw new RuntimeException("ERROR: Wrong format in keyserver's response");
		}
		Vector<Element> ekls = e.getChildren("keylog");
		Vector<KeyLog> vkl = new Vector<KeyLog>();
		for (Element ekl : ekls) {
			vkl.add(KeyLog.fromElement(ekl));
		}
		//TODO verify signature		
		return vkl;
	}
	
	//5. Ich, als fremder user, möchte beim keyserver die weiteren pubkeys zu einem parent-pubkey (MASTER) bekommen können
	public Vector<String> requestSubKeys(String masterkeyid) throws Exception {
		OSDXKeyServerClientRequest req = OSDXKeyServerClientRequest.getRequestSubkeys(host, masterkeyid);
		OSDXKeyServerClientResponse resp = send(req);

		Element e = resp.doc.getRootElement();
		if (!e.getName().equals("subkeys_response")) {
			throw new RuntimeException("ERROR: Wrong format in keyserver's response");
		}
		Vector<String> ret = new Vector<String>();
		Vector<Element> keyids = e.getChildren("keyid");
		for (Element k : keyids) {
			ret.add(k.getText());
		}		
		//TODO verify signature		
		return ret;
	}
	
	public OSDXKeyObject requestPublicKey(String keyid) throws Exception {
		OSDXKeyServerClientRequest req = OSDXKeyServerClientRequest.getRequestPublicKey(host, keyid);
		OSDXKeyServerClientResponse resp = send(req);

		Element e = resp.doc.getRootElement();
		if (!e.getName().equals("pubkey_response")) {
			throw new RuntimeException("ERROR: Wrong format in keyserver's response");
		}
		Vector<String> ret = new Vector<String>();
		Element key = e.getChild("pubkey");
		if (key!=null) {
			//TODO verify signature
			
		}	
		return null;
	}
	
	//   1. Ich, als user, möchte auf dem keyserver meinen MASTER-pubkey ablegen können
	//   includes  2. Ich, als user, möchte, daß der keyserver meinen MASTER-pubkey per email-verifikation (der haupt-identity) akzeptiert (sonst ist der status pending oder so -> erst, wenn die email mit irgendeinem token-link drin aktiviert wurde, wird der pubkey akzeptiert)
	public boolean putMasterKey(OSDXKeyObject masterkey, Identity id) throws Exception {
		try {
			OSDXKeyServerClientRequest req = OSDXKeyServerClientRequest.getRequestPutMasterKey(host, masterkey, id);
			OSDXKeyServerClientResponse resp = send(req);
			if (resp==null || resp.status == null) return false;
			if (resp.status.endsWith("OK"))	return true;
			if (resp.hasErrorMessage()) {
				message = resp.getErrorMessage();
			}
		} catch (Exception ex) {
			message = ex.getMessage();
		}
		return false;
	}
	
	//3. Ich, als user, möchte auf dem keyserver meinen REVOKE-key für meinen master-key abspeichern können (der sollte sogar nicht sichtbar für irgendwen sonst sein!!!)
	public boolean putRevokeKey(OSDXKeyObject revokekey, OSDXKeyObject relatedMasterKey) throws Exception {
		OSDXKeyServerClientRequest req = OSDXKeyServerClientRequest.getRequestPutRevokeKey(host, revokekey, relatedMasterKey);
		OSDXKeyServerClientResponse resp = send(req);
		if (resp==null || resp.status == null) {
			message = "Keyserver does not respond.";
			return false;
		}
		if (resp.status.endsWith("OK"))	return true;
		if (resp.hasErrorMessage()) {
			message = resp.getErrorMessage();
		}
		return false;
	}
	
	public boolean putSubKey(OSDXKeyObject subkey, OSDXKeyObject relatedMasterKey) throws Exception {
		OSDXKeyServerClientRequest req = OSDXKeyServerClientRequest.getRequestPutSubKey(host, subkey, relatedMasterKey);
		OSDXKeyServerClientResponse resp = send(req);
		if (resp==null || resp.status == null) return false;
		if (resp.status.endsWith("OK"))	return true;
		if (resp.hasErrorMessage()) {
			message = resp.getErrorMessage();
		}
		return false;
	}

	//   5. Ich, als user, möchte meine keylogs auf dem server ablegen können (ein löschen von keylogs ist NICHT möglich - für einen aktuellen status ist die "kette ist chronologisch abzuarbeiten")
	public boolean putKeyLog(KeyLog keylog) throws Exception {
		throw new RuntimeException("not implemented");
		//TODO why should i do that?
	
		//example request
		
//		POST / HTTP/1.1
//		Host: keys.fnppl.org
//		Request: keylogdelivery
//		Content-Type: text/xml
//		Content-Length: 235
//
//		<keylogdelivery>
//		  <keylog>
//		    <action>
//		      <date>2011-01-01 00:00:00 GMT+00:00</date>
//		      <ipv4>919191</ipv4>
//		      <ipv6>91921929</ipv6>
//		      <from>
//		        <keyid>kakaka@llalal.nät</keyid>
//		        <sha1fingerprint />
//		      </from>
//		      <to>
//		        <keyid>kakaka2@llalal.nät</keyid>
//		        <sha1fingerprint />
//		      </to>
//		      <approval>
//		        <of>
//		          <identity>
//		            <email>akakak@lalal</email>
//		            <function />
//		            <sha1 />
//		          </identity>
//		        </of>
//		      </approval>
//		      <datapath>
//		        <step1>
//		          <datasource>keys.fnppl.org</datasource>
//		          <datainsertdatetime>2011-02-21 00:00:00 GMT+00:00</datainsertdatetime>
//		        </step1>
//		        <step2>
//		          <datasource>keys.fnppl.org</datasource>
//		          <datainsertdatetime>2011-02-21 00:00:00 GMT+00:00</datainsertdatetime>
//		        </step2>
//		      </datapath>
//		    </action>
//		    <sha1localproof>85:31:63:9E:83:F8:94:BA:39:91:6E:2E:B5:86:1C:57:1D:41:9A:90</sha1localproof>
//		    <signature>
//		      <data>
//		        <sha1>85:31:63:9E:83:F8:94:BA:39:91:6E:2E:B5:86:1C:57:1D:41:9A:90</sha1>
//		        <signdatetime>2011-03-09 09:27:44 GMT+00:00</signdatetime>
//		        <dataname>localsignoff for sha1 85:31:63:9E:83:F8:94:BA:39:91:6E:2E:B5:86:1C:57:1D:41:9A:90</dataname>
//		      </data>
//		      <signoff>
//		        <keyid>BEEE542006AF8301096BF0305AB4632E9982AA94</keyid>
//		        <pubkey>
//		          <algo>RSA</algo>
//		          <bits>3072</bits>
//		          <modulus>00CBFC6AE1B8C3B2E31DF52214F1CFB4EFBB9E77CAF63A61F85B8AAD1BB43A3C138F1FC1C8E7D6F3368E7985AC1719A07F77F16C4D26E7BCC0A3EE079F9132BA1ACA7E9E279852F6D2821EE3FCD9C3519D15B7DA34345597D1EA38B716891793E76D4C34270257C010E03B4D8F39BE1025931B6D104C4E52D542DA7A8A1CCD2368C3075D5C71DEE146FBBB77A0DD72DB14A444D556B12681497565B7D82B5F2DA30B667AE8D8E364993E9F2746AB3AA92FBF2B0A8376870CC9C890805EA27541B997B9196916DBAFF7D3F180A2DBD4D5A54F23006FEF4544345A0FD390CCEEEDB4C8C505DF7D11C4A8687E9BF865F1A28008355B17EDB31509A5F122856AB11DDC56A4D51F02A79908E2E89D0EA7EE80DEEAE2119C89D9C25BEDE96386504CEA40C95DCF630E78514FB28F8F9EC9BC280B4F0D2C798988B8E27348AAEA94A3FA58BDE172BE6D07D1FDFD493C632DCC2E45B79353231A92A1CD3924DF5DFB0AB20D889D89FCD26F0A32421CC669BC89F239FDC91636A638B55A784BF5846CFAD007</modulus>
//		          <exponent>010001</exponent>
//		        </pubkey>
//		        <signaturebytes>19:17:23:BC:6A:0D:85:E2:78:77:89:62:81:4C:EB:15:9C:D9:BF:24:E4:95:48:B0:20:62:57:C1:85:4F:34:BE:AD:67:0F:6E:C9:9A:41:F1:BC:2F:80:4A:B5:2E:77:0B:80:1F:3E:B3:54:4A:FD:1A:2F:79:E7:76:6E:39:4C:0C:55:DB:C9:FC:E4:C8:49:F4:7D:02:2D:E3:D4:03:B3:47:70:7E:92:EB:9E:3B:E9:B8:BF:BE:87:2F:E3:C6:94:C9:42:84:D8:EB:49:C3:35:89:F8:A5:04:5E:41:0A:01:C2:93:96:90:E2:E6:60:53:AA:03:06:3E:EC:C9:7F:71:A9:84:84:51:A7:A4:EA:B0:D2:98:8B:87:7C:02:E5:91:93:0B:78:86:B5:5B:8D:05:44:EB:68:EA:1E:17:A4:60:49:49:F3:D7:27:1D:72:A5:BE:0C:31:B0:90:CE:17:1F:9F:84:27:8D:38:28:7C:A3:EA:BD:51:04:85:B1:82:AF:EE:62:16:CC:0D:14:F7:2A:7D:32:93:E8:36:80:E0:2D:6F:19:90:B9:22:10:84:C0:37:C3:FE:B8:21:FE:39:77:D9:41:D8:E7:86:68:F3:9E:8F:24:40:46:02:4F:71:49:DD:FD:7D:6F:2D:75:0F:5E:EA:FB:AB:A5:93:2E:FE:C8:1F:A9:D5:D6:C6:6D:E6:84:AF:BE:4B:01:B8:94:42:1B:53:69:BE:80:45:C8:43:06:45:0C:07:4E:FE:D4:35:92:38:EA:14:65:F8:26:71:82:26:B5:EC:38:24:90:D0:CD:50:1B:84:B5:B6:4F:46:B4:F8:FF:90:99:83:1F:9D:CA:7F:7C:21:46:10:EC:A5:3C:F2:2A:C2:05:9E:56:50:2A:56:7D:57:AA:A5:0D:4F:C8:0A:27:34:25:DA:1B:03:9B:E2:45:FC:55:B6:82:D7:8E:80:C9:4E:BE:33:7A:40:B5:03:73:30:FF:49:A0:FE:98:73:27:24:EA:20:47:DC:43:03</signaturebytes>
//		      </signoff>
//		    </signature>
//		</keylogdelivery>
		
	}
	
	//TODO
	//   4. Ich, als user, möchte eigentlich, daß alle meine Aktionen auf meinem MASTER-key - bzw. allen meiner keys durch einen entsprechenden signature-proof des entsprechenden private-keys validierbar sind
	//   6. Ich, als user, möchte alle kommunikation vom/zum keyserver mit einem vom keyserver definierten root-MASTER-key approveden key signiert wissen
	//   7. Ich, als user, möchte diese keyserver-root-keys vordefiniert in meiner openSDX-suite finden, aber auch simpelst selbst nachrüsten können

	
	public static void main(String[] args) throws Exception {
		//testing
		OSDXKeyServerClient c = new OSDXKeyServerClient("localhost", 8889);
		

//		Vector<PublicKey> keys = c.requestMasterPubKeys("test@fnppl.org");
//		for (PublicKey k : keys) {
//			System.out.println("public key: "+k.getKeyID());
//		}

		
//		Vector<Identity> ids = c.requestIdentities("7610FF13E234ED7694333FF67F312E0DEA45AC99");
//		for (Identity id : ids) {
//			System.out.println("identity email: "+id.getEmail());
//		}

		
//		Vector<KeyLog> keylogs = c.requestKeyLogs("85D9EB452CA5CD270CA9EC73724ACDAC9E6A6281@LOCAL");
//		System.out.println("received "+keylogs.size()+" keylogs");

		
//		Vector<PublicKey> keys = c.requestSubKeys("85D9EB452CA5CD270CA9EC73724ACDAC9E6A6281@LOCAL");
//		for (PublicKey k : keys) {
//			System.out.println("public sub key: "+k.getKeyID());
//		}

		
//		AsymmetricKeyPair kp =  AsymmetricKeyPair.generateAsymmetricKeyPair();
//		OSDXKeyObject key = OSDXKeyObject.fromKeyPair(kp);
//		Identity id = Identity.newEmptyIdentity();
//		id.setEmail("test@fnppl.org");


//		boolean ok = c.putMasterKey(key.getPubKey(), id);
//		System.out.println("put master key: "+(ok?"OK":"FAILED"));

	
	//	String[] status = c.requestKeyStatus("C930CEEF52E4D6808A4253AC2C0EF5F6E578C603");
	//	System.out.println("key status: "+status[0]+" from date: "+status[1]);


//		boolean ok2 = c.putRevokeKey(key.getPubKey(), key);
//		System.out.println("put revoke key: "+(ok2?"OK":"FAILED"));

		
	}
	
}
