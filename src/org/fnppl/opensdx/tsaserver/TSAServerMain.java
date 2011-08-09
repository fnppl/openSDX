package org.fnppl.opensdx.tsaserver;


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
import java.net.*;
import java.util.*;
import java.util.Map.Entry;

import org.fnppl.opensdx.gui.DefaultMessageHandler;
import org.fnppl.opensdx.gui.MessageHandler;
import org.fnppl.opensdx.http.HTTPServer;
import org.fnppl.opensdx.http.HTTPServerRequest;
import org.fnppl.opensdx.http.HTTPServerResponse;
import org.fnppl.opensdx.keyserver.KeyServerResponse;
import org.fnppl.opensdx.security.*;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;

public class TSAServerMain extends HTTPServer {
	
	private String serverid = "OSDX TSAServer v0.1";
	private File configFile = new File("tsaserver_config.xml"); 
	private File alterConfigFile = new File("src/org/fnppl/opensdx/tsaserver/resources/tsaserver_config.xml"); 
	private String servername = null;
	
	private MessageHandler messageHandler = new DefaultMessageHandler() {
		public boolean requestOverwriteFile(File file) {//dont ask, just overwrite
			return true;
		}
		public boolean requestIgnoreKeyLogVerificationFailure() {//dont ignore faild keylog verification
			return false;
		}
		public String[] requestNewPasswordAndMantra(String message) {
			return new String[] {"debug","debug"};
		}
	};
	
	public void init(String pwSigning) {
		serverid = getServerID();
		try {
			readConfig();
			if (signingKey==null) {
				signingKey = createNewSigningKey(pwSigning, servername);
			}
			signingKey.unlockPrivateKey(pwSigning);
			
			Document d = Document.buildDocument(signingKey.getSimplePubKeyElement());
			System.out.println("\nServer Public SigningKey:");
			d.output(System.out);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	public TSAServerMain(String pwSigning) {
		super();
		init(pwSigning);
	}
	
	public OSDXKey createNewSigningKey(String pwSigning, String servername) {
		try {
			pwSigning = "debug";
			
			//generate new keypair
			MasterKey newSigningKey = MasterKey.buildNewMasterKeyfromKeyPair(AsymmetricKeyPair.generateAsymmetricKeyPair());
			newSigningKey.setAuthoritativeKeyServer(servername);
			Identity id = Identity.newEmptyIdentity();
			id.setEmail("debug@it-is-awesome.de");
			id.setMnemonic("TSA Signature");
			id.set_mnemonic_restricted(false);
			id.setIdentNum(1);
			id.createSHA256();	
			newSigningKey.addIdentity(id);
			newSigningKey.setAuthoritativeKeyServer("keyserver.fnppl.org");
			newSigningKey.createLockedPrivateKey("", pwSigning);
			
			//upload to fnppl.org
			KeyVerificator verify = KeyVerificator.make();
			KeyClient client = new KeyClient("keyserver.fnppl.org", 80, "", verify);
			client.putMasterKey(newSigningKey, id);
			//upload self approval
			//client.putKeyLogAction(KeyLogAction.buildKeyLogAction(KeyLogAction.APPROVAL, newSigningKey, newSigningKey.getKeyID(), id, "self approval"), newSigningKey);
			client.close();
			
			Document d = Document.buildDocument(newSigningKey.toElement(messageHandler));
			System.out.println("\nTSAServerSigningKey:");
			d.output(System.out);
			
			return newSigningKey;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public String getServerID() {
		return serverid;
	}
	
	public void readConfig() {
		try {
			if (!configFile.exists()) {
				configFile = alterConfigFile;
			}
			if (!configFile.exists()) {
				System.out.println("Sorry, tsaserver_config.xml not found.");
				exit();
			}
			Element root = Document.fromFile(configFile).getRootElement();
			//keyserver base
			Element ks = root.getChild("tsaserver");
//			host = ks.getChildText("host");
			port = ks.getChildInt("port");
			String ip4 = ks.getChildText("ipv4");
			try {
				byte[] addr = new byte[4];
				String[] sa = ip4.split("[.]");
				for (int i=0;i<4;i++) {
					int b = Integer.parseInt(sa[i]);
					if (b>127) b = -256+b;
					addr[i] = (byte)b;
				}
				address = InetAddress.getByAddress(addr);
			} catch (Exception ex) {
				System.out.println("CAUTION: error while parsing ip adress");
				ex.printStackTrace();
			}
			
			//SigningKey
			try {
				OSDXKey k = OSDXKey.fromElement(root.getChild("rootsigningkey").getChild("keypair"));
				signingKey = k;
			} catch (Exception e) {
				System.out.println("ERROR: no signing key in config."); 
			}
			//TODO check localproofs and signatures 

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	public HTTPServerResponse prepareResponse(HTTPServerRequest request) throws Exception {
		if (request.method==null) return null;
		
		if(request.method.equals("POST")) {
			if (request.cmd.equals("/tsasignature")) {
				return createTSASignatureResponse(request);
			}
		}
		else if(request.method.equals("HEAD")) {
			//throw new Exception("NOT IMPLEMENTED"); //correct would be to fire a HTTP_ERR
			return null;
		}
		else if(request.method.equals("GET")) {
			
		}
		return null;
	}
	
	public HTTPServerResponse createTSASignatureResponse(HTTPServerRequest request) {
		HTTPServerResponse resp = new HTTPServerResponse(serverid);
		try {
//			OSDXMessage msgRequest;
//			try {
//				msgRequest = OSDXMessage.fromElement(request.xml.getRootElement());
//			} catch (Exception ex) {
//				return HTTPServerResponse.errorMessage(serverid, "ERROR in opensdx_message");
//			}
//			Result verified = msgRequest.verifySignaturesWithoutKeyVerification();
//			if (!verified.succeeded) {
//				return HTTPServerResponse.errorMessage(serverid,"verification of signature failed"+(verified.errorMessage!=null?": "+verified.errorMessage:""));
//			}
//			Document.buildDocument(msgRequest.toElement()).output(System.out);			
//			Element contentRequest = msgRequest.getContent();
			Element contentRequest = request.xml.getRootElement();
			if (!contentRequest.getName().equals("signature")) {
				return HTTPServerResponse.errorMessage(serverid, "missing signature");
			}
			Signature signatureRequest = null;
			try {
				signatureRequest = Signature.fromElement(contentRequest);
			} catch (Exception ex) {
				signatureRequest =null;
			}
			if (signatureRequest==null) {
				return HTTPServerResponse.errorMessage(serverid, "error in signature");
			}
			Signature signatureTSA = Signature.createSignatureFromLocalProof(signatureRequest.getSignatureBytes(), "TSA signature of given signaturebytes", signingKey);
			
			Element content = new Element("tsa_response");
			Element eTSASignature = new Element("tsa_signature");
			eTSASignature.addContent(signatureTSA.toElement());
			Element eOrigSignature = new Element("received_signature");
			eOrigSignature.addContent(signatureRequest.toElement());
			content.addContent(eTSASignature);
			content.addContent(eOrigSignature);
			OSDXMessage msg = OSDXMessage.buildMessage(content, signingKey);
			resp.setContentElement(msg.toElement());
		} catch (Exception ex) {
			resp.setRetCode(404, "FAILED");
			resp.createErrorMessageContent("Internal Error"); //should/could never happen
			ex.printStackTrace();
		}
		return resp;
	}
	
	
	public static void main(String[] args) throws Exception {
		if (args==null || args.length!=4 || !args[0].equals("-s")|| !args[2].equals("-h")) {
			System.out.println("usage: TsaServer -s \"password signingkey\" -h servername");
			return;
		}
		TSAServerMain ss = new TSAServerMain(args[1]);
		ss.port = 8890;
		ss.servername = args[3];
		
		ss.startService();
	}

}

