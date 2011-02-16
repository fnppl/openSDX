package org.fnppl.opensdx.tsas;

/*
 * Copyright (C) 2010-2011 
 * 							fine people e.V. <opensdx@fnppl.org> 
 * 							Henning Thieß <ht@fnppl.org>
 * 
 * 							http://fnppl.org
 * 
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

import java.io.*;
import java.net.*;
import java.util.*;
import org.fnppl.opensdx.security.*;

public class TSAServerMain {
	int port = -1;
	Inet4Address address = null;
	private AsymmetricKeyPair sign_keys = null;
	
	public TSAServerMain() {
		
	}
	
	public static TsaServerResponse prepareResponse(TsaServerRequest request) {
		//yeah, switch cmd/method - stuff whatever...
		return null;
	}
	
	public void readKeys(File f, char[] pass_mantra) throws Exception {
		KeyRingCollection krc = KeyRingCollection.fromFile(f, pass_mantra, true);
		//get the relevant sign-key from that collection
		this.sign_keys = null;//assign that keys from that...
	}
	
	public void handleSocket(final Socket s) throws Exception {
		//check on *too* many requests from one ip
		
		Thread t = new Thread() {
			public void run() {
				try {
					InputStream in = s.getInputStream();
					TsaServerRequest request = TsaServerRequest.fromInputStream(in);
					
					TsaServerResponse response = prepareResponse(request);//this is ok since the response is small and can be kept in mem - no need for directly kick it on socket... 
					
					OutputStream out = s.getOutputStream();
					response.toOutput(out);
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		};
		t.start();
	}
	
	public void startService() throws Exception {
		ServerSocket so = new ServerSocket(port);
		if(address!=null) {
			throw new RuntimeException("Not yet implemented...");
		}
		while(true) {
			try {
				final Socket me = so.accept();
				handleSocket(me);
			} catch(Exception ex) {
				ex.printStackTrace();
				Thread.sleep(250);//cooldown...
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		TSAServerMain ss = new TSAServerMain();
		ss.port = 8889;
		
		ss.startService();
	}
}

