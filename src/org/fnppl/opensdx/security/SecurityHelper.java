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


import java.security.Security;
import java.io.*;
import java.util.*;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/*
 * @author Henning Thieß <ht@fnppl.org>
 * 
 */

public class SecurityHelper {
	public static void ensureBC() {
		if(Security.getProvider("BC")==null) {
			Security.addProvider(new BouncyCastleProvider());
		}
	}

	
	public final static class HexDecoder {	    
	    /**
	     * Initial size of the decoding table.
	     */
	    private static final int DECODING_TABLE_SIZE = 128;

	    /**
	     * Encoding table.
	     */
	    protected static final byte[] ENCODING_TABLE = {
	        (byte) '0', (byte) '1', (byte) '2', (byte) '3',
	        (byte) '4', (byte) '5', (byte) '6', (byte) '7',
	        (byte) '8', (byte) '9', (byte) 'A', (byte) 'B',
	        (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F' 
	    };

	    /**
	     * Decoding table.
	     */
	    protected static final byte[] DECODING_TABLE = new byte[DECODING_TABLE_SIZE];

	    /**
	     * Initialize the decoding table.
	     */
	    protected static void initialiseDecodingTable() {
	        for (int i = 0; i < ENCODING_TABLE.length; i++) {
	            DECODING_TABLE[ENCODING_TABLE[i]] = (byte) i;
	        }

	        // deal with lower case letters as well
	        DECODING_TABLE['a'] = DECODING_TABLE['A'];
	        DECODING_TABLE['b'] = DECODING_TABLE['B'];
	        DECODING_TABLE['c'] = DECODING_TABLE['C'];
	        DECODING_TABLE['d'] = DECODING_TABLE['D'];
	        DECODING_TABLE['e'] = DECODING_TABLE['E'];
	        DECODING_TABLE['f'] = DECODING_TABLE['F'];
	    }

	    static {
	        initialiseDecodingTable();
	    }

	    /**
	     * Creates an instance of this class. 
	     */
	    private HexDecoder() {
	        // Nothing to do ...
	    }

	    /**
	     * Encodes the input data producing a Hex output stream.
	     * @param data The input data to be HEX encoded
	     * @param off Initiak offset
	     * @param length Initial length of the input data array
	     * @param out The {@link OutputStream} instance holding the encoded input data.
	     * @return the number of bytes produced.
	     * @throws IOException If encoding fails.
	     */
	    public static void encode(final byte[] data, final int off, final int length, 
	            final OutputStream out, char pad, int wrapat) throws IOException {
	        
	    	for (int i = off; i < (off + length); i++) {
	            int v = data[i] & 0xff;

	            out.write(ENCODING_TABLE[(v >>> 4)]);
	            out.write(ENCODING_TABLE[v & 0xf]);
	            
	            if(pad == '\0') {
	            	out.write(pad);
	            }
	            if(wrapat!=-1 && i>0 && i%wrapat==0) {
	            	out.write('\n');
	            }
	        }

//	        return length * 2;
	    }

	    /**
	     * Indicates whether a given character should be ignored during en-/decoding.
	     * @param c The character at question.
	     * @return True if the given character should be ignored.
	     */
	    private static boolean ignore(final char c) {
	        return (c == '\n' || c == '\r' || c == ':' || c == '\t' || c == ' ');
	    }

	    /**
	     * Decodes the Hex encoded byte data writing it to the given output stream,
	     * whitespace characters will be ignored.
	     * @param data The data to be encoded
	     * @param off Initial offset.
	     * @param length Initial length
	     * @param out The {@link OutputStream} instance
	     * @return the number of bytes produced.
	     * @throws IOException If encoding failed.
	     */
	    public static int decode(final byte[] data, final int off, final int length,
	            final OutputStream out) throws IOException {
	        byte b1, b2;
	        int outLen = 0;

	        int end = off + length;

	        while (end > off) {
	            if (!ignore((char) data[end - 1])) {
	                break;
	            }

	            end--;
	        }

	        int i = off;
	        while (i < end) {
	            while (i < end && ignore((char) data[i])) {
	                i++;
	            }

	            b1 = DECODING_TABLE[data[i++]];

	            while (i < end && ignore((char) data[i])) {
	                i++;
	            }

	            b2 = DECODING_TABLE[data[i++]];

	            out.write((b1 << 4) | b2);

	            outLen++;
	        }

	        return outLen;
	    }

	    /**
	     * Decodes the Hex encoded String data writing it to the given output stream,
	     * whitespace characters will be ignored.
	     * 
	     * @param data The data to be encoded
	     * @param out The {@link OutputStream} instance
	     * @return the number of bytes produced.
	     * @throws IOException If encoding failed.
	     */
	    public static int decode(final String data, final OutputStream out) throws IOException {
	        byte b1, b2;
	        int length = 0;

	        int end = data.length();

	        while (end > 0) {
	            if (!ignore(data.charAt(end - 1))) {
	                break;
	            }

	            end--;
	        }

	        int i = 0;
	        while (i < end) {
	            while (i < end && ignore(data.charAt(i))) {
	                i++;
	            }

	            b1 = DECODING_TABLE[data.charAt(i++)];

	            while (i < end && ignore(data.charAt(i))) {
	                i++;
	            }

	            b2 = DECODING_TABLE[data.charAt(i++)];

	            out.write((b1 << 4) | b2);

	            length++;
	        }

	        return length;
	    }

	    /**
	     * Encodes the input data producing a Hex output stream.
	     * @param data Input data to encode.
	     * @return the number of bytes produced.
	     */
	    public static String encode(final byte[] data, char pad, int wrapat) {
	        try {
	            final ByteArrayOutputStream out = new ByteArrayOutputStream();
	            encode(data, 0, data.length, out, pad, wrapat);
	            out.close();
	            return new String(out.toByteArray());
	        } catch (IOException e) {
	            e.printStackTrace();
	            throw new RuntimeException(e.getMessage(), e);
	        }
	    }

	    /**
	     * Decodes the HEX input data producing a output stream.
	     * @param data Input data to be decoded.
	     * @return A byte array representing the decoded input data.
	     */
	    public static byte[] decode(final String data) {
	        try {
	            final ByteArrayOutputStream out = new ByteArrayOutputStream();
	            if(data.toLowerCase().indexOf("0x")==0) {
	            	decode(data.substring(2), out);
	            }
	            else {
	            	decode(data, out);
	            }
	            out.close();
	            return out.toByteArray();
	        } catch (IOException e) {
	            e.printStackTrace();
	            throw new RuntimeException(e.getMessage(), e);
	        }
	    }

	}
}

