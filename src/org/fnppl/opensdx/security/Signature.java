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

import org.bouncycastle.bcpg.ArmoredInputStream;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.bouncycastle.openpgp.PGPUtil;

/*
 * @author Henning Thieß <ht@fnppl.org>
 * 
 */

public class Signature {
	static {
		SecurityHelper.ensureBC();
	}
	
	private File signature;
	private File signedFile;
	private AsymmetricKeyPair keypair;
	
	//TODO HT 20.02.2011 - add byte-array/stream-based constructor
	
	public Signature(File signature, File signedfile, AsymmetricKeyPair keypair) {
		this.signature = signature;
		this.signedFile = signedfile;
		this.keypair = keypair;
	}
	
	public static Signature createSignature(File tosign, AsymmetricKeyPair keypair) throws Exception {
		File s = new File(tosign.getParentFile(), tosign.getName()+".asc");
		return createSignature(tosign, s, keypair);
	}
	public static Signature createSignature(File tosign, File signaturefile, AsymmetricKeyPair keypair) throws Exception {
		FileInputStream in = new FileInputStream(tosign);
		ArmoredOutputStream out = new ArmoredOutputStream(new FileOutputStream(signaturefile));
    	BCPGOutputStream bOut = new BCPGOutputStream(out);
		PGPSignature sig = createSignature(in, keypair);
		sig.encode(bOut);
		bOut.close();
		out.close();
		
		return new Signature(signaturefile, tosign, keypair);
	}
	
	public static PGPSignature createSignature(InputStream in, AsymmetricKeyPair kp) throws Exception {
		BufferedInputStream bin = null;
		if(in instanceof BufferedInputStream) {
			bin = (BufferedInputStream)in;
		}
		else {
			bin = new BufferedInputStream(in);
		}
    	PGPPrivateKey pgpPrivKey = kp.getPGPPrivateKey();
    	PGPPublicKey pgpPubKey = kp.getPGPPublicKey();
    	PGPSignatureGenerator sGen = new PGPSignatureGenerator(
    			pgpPubKey.getAlgorithm(), //HT 20.02.2011 @beboe: warum der pubkey-algo??? 
//    			pgpPrivKey.getKey().getAlgorithm(), //HT 20.02.2011 warum geht das hier nicht???
    			PGPUtil.SHA1,
    			"BC"
    		);
    	sGen.initSign(PGPSignature.BINARY_DOCUMENT, pgpPrivKey);
    	byte[] buff = new byte[512];
    	int read = 0;
    	while ((read=in.read(buff)) != -1) {
    		sGen.update(buff, 0, read);
    	}
    	return sGen.generate();
    }
}
