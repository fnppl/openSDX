package org.fnppl.opensdx.security;

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

import java.util.*;


/**
 * Class for signing and verifying File using openpgp and bouncycastle.org implementations
 * digest algo is SHA1
 * 
 * @author Bertram Boedeker <bboedeker@gmx.de>
 * @author Henning Thieß <ht@fnppl.org>
 */

public class SignAndVerify {
	static {
		SecurityHelper.ensureBC();
	}
	
	public static Signature createSignatureFile(File f, AsymmetricKeyPair kp) throws Exception {
		return Signature.createSignature(f, kp);
	}
	    
	
	
	
	 public static boolean verifySignature(File file, KeyRingCollection coll) throws Exception {
	    	
	    	String filename = file.getAbsolutePath(); 
	    	if (filename.toLowerCase().endsWith(".asc"))
	    		filename = filename.substring(0,filename.length()-4);
	    	
	    	InputStream inData = new FileInputStream(filename);
	    	InputStream inSig = PGPUtil.getDecoderStream(new FileInputStream(filename+".asc"));
	    	
	    	PGPObjectFactory pgpFact = new PGPObjectFactory(inSig);
	    	PGPSignatureList p3 = null;
	    	Object o = pgpFact.nextObject();
	    	if (o instanceof PGPCompressedData) {
	    		PGPCompressedData c1 = (PGPCompressedData)o;
	    		pgpFact = new PGPObjectFactory(c1.getDataStream());
	    		p3 = (PGPSignatureList)pgpFact.nextObject();
	    	} else {
	    		p3 = (PGPSignatureList)o;
	    	}
	    	PGPSignature sig = p3.get(0);
	    	try {
		    	PublicKey pk = coll.getPublicKey(sig.getKeyID());
		    	
		    	PGPPublicKey key = pk.getPGPPublicKey();
		    	sig.initVerify(key, "BC");
		    	int ch;
		    	while ((ch = inData.read()) >= 0) {
		    		sig.update((byte)ch);
		    	}
		    	return sig.verify();
	    	} catch (Exception ex) {
	    		if (ex.getMessage().startsWith("NO MATCHING KEY FOUND")) {
	    			System.out.println("NO MATCHING KEY FOUND!");
	    		} else {
	    			ex.printStackTrace();
	    		}
	    		return false;
	    	}
	    }

}
