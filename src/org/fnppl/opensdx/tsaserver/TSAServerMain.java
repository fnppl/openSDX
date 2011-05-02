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
import org.fnppl.opensdx.http.HTTPServerRequest;
import org.fnppl.opensdx.http.HTTPServerResponse;
import org.fnppl.opensdx.keyserver.KeyServerResponse;
import org.fnppl.opensdx.security.*;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;

public class TSAServerMain {
	
private static String serverid = "OSDX TSAServer v0.1";
	
	private File configFile = new File("tsaserver_config.xml"); 
	private File alterConfigFile = new File("src/org/fnppl/opensdx/tsaserver/resources/tsaserver_config.xml"); 
	
	private String host = "localhost"; //keys.fnppl.org
	private int port = -1;
	private int maxRequestsPerMinute = 100;
	private int maxThreadCount = 30;
	private InetAddress address = null;

	private HashMap<String, int[]> ipRequests = new HashMap<String, int[]>();
	private HashMap<String, Thread> currentWorkingThreads = new HashMap<String, Thread>();	
	
	protected MasterKey signingKey = null;
	
	private MessageHandler messageHandler = new DefaultMessageHandler() {
		public boolean requestOverwriteFile(File file) {//dont ask, just overwrite
			return true;
		}
		public boolean requestIgnoreKeyLogVerificationFailure() {//dont ignore faild keylog verification
			return false;
		}
		public MasterKey requestMasterSigningKey(KeyApprovingStore keystore) throws Exception {
			return signingKey;
		}
		public String[] requestNewPasswordAndMantra(String message) {
			return new String[] {"debug","debug"};
		}
	};
	
	public TSAServerMain(String pwSigning) {
		try {
			readConfig();
			if (signingKey==null) {
			
				pwSigning = "debug";
				
				//generate new keypair
				signingKey = MasterKey.buildNewMasterKeyfromKeyPair(AsymmetricKeyPair.generateAsymmetricKeyPair());
				signingKey.setAuthoritativeKeyServer(host);
				Identity id = Identity.newEmptyIdentity();
				id.setEmail("debug_tsa_signing@tsaserver.fnppl.org");
				id.setIdentNum(1);
				id.createSHA256();	
				signingKey.addIdentity(id);
				
				
				Document d = Document.buildDocument(signingKey.toElement(messageHandler));
				System.out.println("\nTSA Server SigningKey:");
				d.output(System.out);
				
			}
			
			signingKey.unlockPrivateKey(pwSigning);
			
			Document d = Document.buildDocument(signingKey.getSimplePubKeyElement());
			System.out.println("\nTSA Server Public SigningKey:");
			d.output(System.out);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
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
			host = ks.getChildText("host");
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
				if (k instanceof MasterKey) {
					signingKey = (MasterKey)k;
				} else {
					System.out.println("ERROR: rootsigningkey NOT MasterKey");
				}
			} catch (Exception e) {
				System.out.println("ERROR: no signing key in config."); 
			}
			//TODO check localproofs and signatures 

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void exit() {
		System.exit(0);
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
	
	
	public void handleSocket(final Socket s) throws Exception {
		Thread t = new Thread() {
			public void run() {
				String threadID = null;
				try {
					InetAddress addr = s.getInetAddress();
					String remoteIP = addr.getHostAddress();
					int remotePort = s.getPort();
					// check on *too* many requests from one ip
					int[] rc = ipRequests.get(remoteIP);
					if (rc==null) {
						ipRequests.put(remoteIP, new int[]{1});
					} else {
						rc[0]++;
						//System.out.println("anz req: "+rc[0]);
						if (rc[0]>maxRequestsPerMinute) {
							if (rc[0]<=100) {
								System.out.println("WARNING: too many requests ("+rc[0]+") from ip: "+remoteIP);
							}
							return;
						}
					}
					threadID = remoteIP+remotePort;
					currentWorkingThreads.put(threadID,this);
					
					try {
						InputStream _in = s.getInputStream();
						BufferedInputStream in = new BufferedInputStream(_in);
						HTTPServerRequest request = HTTPServerRequest.fromInputStream(in, addr.getHostAddress());
						HTTPServerResponse response = prepareResponse(request);
						
						System.out.println("KeyServerSocket  | ::response ready");
						if (response == null) {
							//send error
							response = new HTTPServerResponse(serverid);
							response.setRetCode(400, "BAD REQUEST");
						}
						if (response != null) {
							System.out.println("SENDING THIS::");response.toOutput(System.out);System.out.println("::/SENDING_THIS");
							
							OutputStream out = s.getOutputStream();
							BufferedOutputStream bout = new BufferedOutputStream(out);
							response.toOutput(bout);
							bout.flush();
							bout.close();
						} 
						else {
							Exception ex = new Exception("RESPONSE COULD NOT BE CREATED");
							throw ex;
						}
					} catch(Exception ex2) {
						ex2.printStackTrace();
					}
					currentWorkingThreads.remove(threadID);
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		};
		t.start();
	}
	
	
	public void startService() throws Exception {
		System.out.println("Starting Server at "+address.getHostAddress()+" on port " + port +"  at "+SecurityHelper.getFormattedDate(System.currentTimeMillis()));
		ServerSocket so = new ServerSocket(port);
		Thread requestMonitor = new Thread() {
			public void run() {
				while (true) {
					updateIPRequestCounter();
					try {
						Thread.sleep(60000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		requestMonitor.start();
		while (true) {
			try {
				while (currentWorkingThreads.size()>maxThreadCount) {
					Thread.sleep(100);
				}
				final Socket me = so.accept();
				handleSocket(me);
			} catch (Exception ex) {
				ex.printStackTrace();
				Thread.sleep(250);// cooldown...
			}
		}
		//System.out.println("Service closed at "+SecurityHelper.getFormattedDate(System.currentTimeMillis()));
	}
	
	private void updateIPRequestCounter() {
		try {
			Vector<String> remove = new Vector<String>();
			for (Entry<String, int[]> e : ipRequests.entrySet()) {
				int[] v = e.getValue();
				v[0] -= maxRequestsPerMinute;
				//System.out.println(e.getKey()+ "  v[0] = "+v[0]);
				if (v[0] < 0) {
					remove.add(e.getKey());
				}
			}
			for (String r : remove) {
				ipRequests.remove(r);
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception {
		if (args==null || args.length!=2 || !args[0].equals("-s")) {
			System.out.println("usage: TsaServer -s \"password signingkey\"");
			return;
		}
		TSAServerMain ss = new TSAServerMain(args[1]);
		ss.port = 8890;
		
		ss.startService();
	}
}

