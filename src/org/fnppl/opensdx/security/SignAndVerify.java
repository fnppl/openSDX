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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.SignatureException;
import java.util.Iterator;

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

/**
 * Class for signing and verifying File using openpgp and bouncycastle.org implementations
 * digest algo is SHA1
 * 
 * @author Bertram Boedeker
 */
public class SignAndVerify {
	
	/**
	 * signs a simple text file and saves it to filesystem with .asc extension
	 * 
	 * @param filename file to sign
	 * @param filenameKey filename with secret-key
	 * @param passPhrase secret passphrase
	 * @throws Exception
	 */
	public static void signTextFile(String filename, String filenameKey, String passPhrase) throws Exception {
    	Security.addProvider(new BouncyCastleProvider());
        InputStream        keyIn = PGPUtil.getDecoderStream(new FileInputStream(filenameKey));
        FileOutputStream   out = new FileOutputStream(filename+".asc");
        
        PGPSecretKey pgpSecKey = readSecretKey(keyIn);
        PGPPrivateKey pgpPrivKey = pgpSecKey.extractPrivateKey(passPhrase.toCharArray(), "BC");        
        PGPSignatureGenerator sGen = new PGPSignatureGenerator(pgpSecKey.getPublicKey().getAlgorithm(), PGPUtil.SHA1, "BC");
        PGPSignatureSubpacketGenerator spGen = new PGPSignatureSubpacketGenerator();
              
        sGen.initSign(PGPSignature.CANONICAL_TEXT_DOCUMENT, pgpPrivKey);
        
        Iterator it = pgpSecKey.getPublicKey().getUserIDs();
        if (it.hasNext()) {
            spGen.setSignerUserID(false, (String)it.next());
            sGen.setHashedSubpackets(spGen.generate());
        }
        
        FileInputStream        fIn = new FileInputStream(filename);
        ArmoredOutputStream    aOut = new ArmoredOutputStream(out);
        
        aOut.beginClearText(PGPUtil.SHA1);

        ByteArrayOutputStream lineOut = new ByteArrayOutputStream();
        int lookAhead = readInputLine(lineOut, fIn);

        processLine(aOut, sGen, lineOut.toByteArray());

        if (lookAhead != -1) {
            do {
                lookAhead = readInputLine(lineOut, lookAhead, fIn);
                sGen.update((byte)'\r');
                sGen.update((byte)'\n');
                processLine(aOut, sGen, lineOut.toByteArray());
            } while (lookAhead != -1);
        }
        
        aOut.endClearText();
        BCPGOutputStream bOut = new BCPGOutputStream(aOut);
        sGen.generate().encode(bOut);
        aOut.close();
    }
    
	/**
	 * verifies a text file with pgp header and saves original file to filesystem
	 * filename needs extension .asc
	 * 
	 * @param filename file to verify
	 * @param filenameKey filename with public-key
	 * @return true when verification was successful
	 * @throws Exception
	 */
    public static boolean verifyTextFile(String filename, String filenameKey) throws Exception {
    	Security.addProvider(new BouncyCastleProvider());
    	if (!filename.toLowerCase().endsWith(".asc")) {
    		throw new Exception("filename needs to end with \".asc\"");
    	}
    	
        InputStream keyIn = PGPUtil.getDecoderStream(new FileInputStream(filenameKey));
        ArmoredInputStream aIn = new ArmoredInputStream(new FileInputStream(filename));
        
        String resultname = filename.substring(0, filename.length() - 4);
        OutputStream out = new BufferedOutputStream(new FileOutputStream(resultname));

        ByteArrayOutputStream lineOut = new ByteArrayOutputStream();
        int lookAhead = readInputLine(lineOut, aIn);
        byte[] lineSep = getLineSeparator();

        if (lookAhead != -1 && aIn.isClearText()) {
            byte[] line = lineOut.toByteArray();
            out.write(line, 0, getLengthWithoutSeparator(line));
            out.write(lineSep);

            while (lookAhead != -1 && aIn.isClearText()) {
                lookAhead = readInputLine(lineOut, lookAhead, aIn);
                line = lineOut.toByteArray();
                out.write(line, 0, getLengthWithoutSeparator(line));
                out.write(lineSep);
            }
        }

        out.close();
        PGPPublicKeyRingCollection pgpRings = new PGPPublicKeyRingCollection(keyIn);
        PGPObjectFactory pgpFact = new PGPObjectFactory(aIn);
        PGPSignatureList p3 = (PGPSignatureList)pgpFact.nextObject();
        PGPSignature sig = p3.get(0);
        sig.initVerify(pgpRings.getPublicKey(sig.getKeyID()), "BC");
        
        InputStream sigIn = new BufferedInputStream(new FileInputStream(resultname));

        lookAhead = readInputLine(lineOut, sigIn);
        processLine(sig, lineOut.toByteArray());

        if (lookAhead != -1) {
            do {
                lookAhead = readInputLine(lineOut, lookAhead, sigIn);
                sig.update((byte)'\r');
                sig.update((byte)'\n');
                processLine(sig, lineOut.toByteArray());
            } while (lookAhead != -1);
        }
        return sig.verify();
    }
	
    /**
	 * signs a file and saves the signature to filesystem with .asc extension
	 * 
	 * @param filename file to sign
	 * @param filenameKey filename with secret-key
	 * @param passPhrase secret passphrase
	 * @throws Exception
	 */
    public static void createSignature(String filename, String filenameKey, String passPhrase) throws Exception {
    	Security.addProvider(new BouncyCastleProvider());
    	InputStream keyIn = PGPUtil.getDecoderStream(new FileInputStream(filenameKey));
		
		ArmoredOutputStream out = new ArmoredOutputStream(new FileOutputStream(filename+".asc"));
    	PGPSecretKey pgpSec = readSecretKey(keyIn);
    	PGPPrivateKey pgpPrivKey = pgpSec.extractPrivateKey(passPhrase.toCharArray(), "BC");        
    	PGPSignatureGenerator sGen = new PGPSignatureGenerator(pgpSec.getPublicKey().getAlgorithm(), PGPUtil.SHA1, "BC");

    	sGen.initSign(PGPSignature.BINARY_DOCUMENT, pgpPrivKey);
    	BCPGOutputStream bOut = new BCPGOutputStream(out);
    	FileInputStream fIn = new FileInputStream(filename);
    	
    	int ch = 0;
    	while ((ch = fIn.read()) >= 0) {
    		sGen.update((byte)ch);
    	}
    	sGen.generate().encode(bOut);
    	out.close();
    }
    
    /**
	 * verifies a given file, signature filename is supposed to be filename + .asc
	 * 
	 * @param filename file to verify
	 * @param filenameKey filename with public-key
	 * @return true when verification was successful
	 * @throws Exception
	 */
    public static boolean verifySignature(String filename, String filenameKey) throws Exception {
    	Security.addProvider(new BouncyCastleProvider());
    	
    	InputStream keyIn = PGPUtil.getDecoderStream(new FileInputStream(filenameKey));
        InputStream in = PGPUtil.getDecoderStream(new FileInputStream(filename+".asc"));

    	PGPObjectFactory pgpFact = new PGPObjectFactory(in);
    	PGPSignatureList p3 = null;

    	Object o = pgpFact.nextObject();
    	if (o instanceof PGPCompressedData) {
    		PGPCompressedData c1 = (PGPCompressedData)o;
    		pgpFact = new PGPObjectFactory(c1.getDataStream());
    		p3 = (PGPSignatureList)pgpFact.nextObject();
    	} else {
    		p3 = (PGPSignatureList)o;
    	}

    	PGPPublicKeyRingCollection pgpPubRingCollection = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(keyIn));
    	InputStream dIn = new FileInputStream(filename);
    	
    	int ch;
    	PGPSignature sig = p3.get(0);
    	PGPPublicKey key = pgpPubRingCollection.getPublicKey(sig.getKeyID());
    	sig.initVerify(key, "BC");
    	while ((ch = dIn.read()) >= 0) {
    		sig.update((byte)ch);
    	}
    	return sig.verify();
    }
    
      
	 /**
     * A simple routine that opens a key ring file and loads the first available key suitable for
     * signature generation.
     * 
     * @param in  stream to read the secret key ring collection from.
     * @return  a secret key.
     * @throws IOException on a problem with using the input stream.
     * @throws PGPException if there is an issue parsing the input stream.
     */
    private static PGPSecretKey readSecretKey(InputStream in) throws IOException, PGPException
    {    
        PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(in);

        //
        // we just loop through the collection till we find a key suitable for encryption, in the real
        // world you would probably want to be a bit smarter about this.
        //
        PGPSecretKey    key = null;
        
        //
        // iterate through the key rings.
        //
        Iterator rIt = pgpSec.getKeyRings();
        
        while (key == null && rIt.hasNext()) {
            PGPSecretKeyRing    kRing = (PGPSecretKeyRing)rIt.next();    
            Iterator            kIt = kRing.getSecretKeys();
            while (key == null && kIt.hasNext()) {
                PGPSecretKey    k = (PGPSecretKey)kIt.next();
                if (k.isSigningKey()) {
                    key = k;
                }
            }
        }
        if (key == null){
            throw new IllegalArgumentException("Can't find signing key in key ring.");
        }
        return key;
    }

    private static int readInputLine(ByteArrayOutputStream bOut, InputStream fIn) throws IOException {
        bOut.reset();
        int lookAhead = -1;
        int ch;
        while ((ch = fIn.read()) >= 0) {
            bOut.write(ch);
            if (ch == '\r' || ch == '\n') {
                lookAhead = readPassedEOL(bOut, ch, fIn);
                break;
            }
        }
        return lookAhead;
    }

    private static int readInputLine(ByteArrayOutputStream bOut, int lookAhead, InputStream fIn) throws IOException  {
        bOut.reset();
        int ch = lookAhead;
        do {
            bOut.write(ch);
            if (ch == '\r' || ch == '\n')
            {
                lookAhead = readPassedEOL(bOut, ch, fIn);
                break;
            }
        } while ((ch = fIn.read()) >= 0);

        if (ch < 0) {
            lookAhead = -1;
        }
        return lookAhead;
    }

    private static int readPassedEOL(ByteArrayOutputStream bOut, int lastCh, InputStream fIn) throws IOException {
        int lookAhead = fIn.read();
        if (lastCh == '\r' && lookAhead == '\n') {
            bOut.write(lookAhead);
            lookAhead = fIn.read();
        }
        return lookAhead;
    }

    private static byte[] getLineSeparator()  {
        String nl = System.getProperty("line.separator");
        byte[] nlBytes = new byte[nl.length()];
        for (int i = 0; i != nlBytes.length; i++) {
            nlBytes[i] = (byte)nl.charAt(i);
        }
        return nlBytes;
    }

    private static void processLine(PGPSignature sig, byte[] line) throws SignatureException, IOException {
        int length = getLengthWithoutWhiteSpace(line);
        if (length > 0) {
            sig.update(line, 0, length);
        }
    }

    private static void processLine(OutputStream aOut, PGPSignatureGenerator sGen, byte[] line)
        throws SignatureException, IOException {
        int length = getLengthWithoutWhiteSpace(line);
        if (length > 0) {
            sGen.update(line, 0, length);
        }
        aOut.write(line, 0, line.length);
    }

    private static int getLengthWithoutSeparator(byte[] line) {
        int    end = line.length - 1;
        while (end >= 0 && isLineEnding(line[end])) {
            end--;
        }
        return end + 1;
    }

    private static boolean isLineEnding(byte b) {
        return b == '\r' || b == '\n';
    }

    private static int getLengthWithoutWhiteSpace(byte[] line) {
        int    end = line.length - 1;
        while (end >= 0 && isWhiteSpace(line[end])) {
            end--;
        }
        return end + 1;
    }

    private static boolean isWhiteSpace(byte b) {
        return b == '\r' || b == '\n' || b == '\t' || b == ' ';
    }

}
