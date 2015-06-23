package org.fnppl.opensdx.security;

/*
 * Copyright (C) 2010-2015 
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

import org.fnppl.opensdx.http.HTTPClient;
import org.fnppl.opensdx.http.HTTPClientRequest;
import org.fnppl.opensdx.http.HTTPClientResponse;
import org.fnppl.opensdx.security.*;
import org.fnppl.opensdx.tsaserver.*;
import org.fnppl.opensdx.xml.*;

public class TSAClient extends HTTPClient {
	
	public static int OSDX_TSASERVER_DEFAULT_PORT = 8890;
	public final static String ERROR_WRONG_RESPONE_FORMAT = "ERROR: Wrong format in keyserver's response.";
	
	public TSAClient(String host, int port) {
		super(host, port);
	}
	
	public Signature getTSASignature(Signature signature) {
		
		HTTPClientRequest request = new HTTPClientRequest();
		request.setURI(host, "/tsasignature");
		request.setContentElement(signature.toElement());
		
		try {
			HTTPClientResponse resp = send(request);
			if (log!=null) {
				log.write("--- REQUEST SUBKEYS MASTERPUBKEY ----------\n".getBytes());
				request.toOutputNOT_URL_ENCODED_FOR_TESTING(log);
				log.write("--- END of REQUEST SUBKEYS MASTERPUBKEY ---\n".getBytes());
				if (resp == null) {
					log.write(("-> --- "+ERROR_NO_RESPONSE+" ---\n").getBytes());
				} else {
					log.write("\n--- RESPONSE SUBKEYS MASTERPUBKEY ----------\n".getBytes());
					resp.toOutput(log);
					log.write("--- END of RESPONSE SUBKEYS MASTERPUBKEY ---\n".getBytes());
				}
			}
			
			OSDXMessage msg = OSDXMessage.fromElement(resp.doc.getRootElement());
			//TODO Result result = msg.verifySignatures();
			Result result = Result.succeeded();
			
			if (result.succeeded) {
				Element content = msg.getContent();
				if (!content.getName().equals("tsa_response")) {
					message = ERROR_WRONG_RESPONE_FORMAT;
					return null;
				}
				Element eSig = content.getChild("tsa_signature").getChild("signature");
				Signature tsaSignature = Signature.fromElement(eSig);
				return tsaSignature;
			} else {
				message = result.errorMessage;
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) {
		try {
			MasterKey key = MasterKey.buildNewMasterKeyfromKeyPair(AsymmetricKeyPair.generateAsymmetricKeyPair());
			byte[] testdata = SecurityHelper.getRandomBytes(20);
			Signature testSignature = Signature.createSignatureFromLocalProof(testdata, "testdata", key);
			
			TSAClient tsa = new TSAClient("localhost", OSDX_TSASERVER_DEFAULT_PORT);
			tsa.connect();
			
			Signature tsaSignature = tsa.getTSASignature(testSignature);
			System.out.println("Received TSA Signature:");
			Document.buildDocument(tsaSignature.toElement()).output(System.out);
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
}
