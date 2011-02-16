package org.fnppl.opensdx.security.test;
/*
 * Copyright (C) 2010-2011 
 * 							fine people e.V. <opensdx@fnppl.org> 
 * 							Bertram Boedeker <bboedeker@gmx.de>
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
 *      
 */
import org.fnppl.opensdx.security.KeyPairGenerator;
import org.fnppl.opensdx.security.SignAndVerify;

public class Test {

	/**
	 * for Testing
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
/*
		// PGPKeyPairGenerator
		try {
			System.out.println("Testing PGPKeyPairGenerator.generateRSAKeyPair");
			PGPKeyPairGenerator.generateRSAKeyPair("bb_rsa_key_pair", "bbtest", true);
			System.out.println("PASSED: no exception catched");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		//Test PGPSignAndVerify, text files
		try {
			System.out.println("Testing PGPSignAndVerify.signTextFile");
			PGPSignAndVerify.signTextFile("example_feed.xml", "secret.asc","bbtest");
			System.out.println("PASSED: no exception catched");
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			System.out.println("Testing PGPSignAndVerify.verifyTextFile");
			boolean v = PGPSignAndVerify.verifyTextFile("example_feed.xml.asc", "pub.asc"); 
			if (v) {
				System.out.println("signature verified.");
			} else {
				System.out.println("signature verification failed.");
			}
			System.out.println("PASSED: no exception catched");
		} catch (Exception e) {
			e.printStackTrace();
		}
*/

		//Test PGPSignAndVerify, s
		try {
			System.out.println("Testing PGPSignAndVerify.createSignature");
			SignAndVerify.createSignature("example_feed.xml", "secret.asc","bbtest");
			System.out.println("PASSED: no exception catched");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			System.out.println("Testing PGPSignAndVerify.verifySignature");
			boolean v = SignAndVerify.verifySignature("example_feed.xml.asc", "pub.asc"); 
			if (v) {
				System.out.println("signature verified.");
			} else {
				System.out.println("signature verification failed.");
			}
			System.out.println("PASSED: no exception catched");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
