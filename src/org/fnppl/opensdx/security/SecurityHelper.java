package org.fnppl.opensdx.security;


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

import java.security.SecureRandom;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.io.*;
import java.util.*;

import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.fnppl.opensdx.xml.Element;

/*
 * @author Henning Thieß <ht@fnppl.org>
 * @author Bertram Boedeker <bboedeker@gmx.de>
 */

public class SecurityHelper {
	final static String RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";
	final static String RFC1123_CUT = "yyyy-MM-dd HH:mm:ss zzz";
	final static String RFC1036 = "EEEE, dd-MMM-yy HH:mm:ss zzz";
	final static String ASCTIME = "EEE MMM dd HH:mm:ss yyyy zzz";
	
	final static Locale ml = new Locale("en", "DE");
	private final static SimpleDateFormat datemeGMT = new SimpleDateFormat(RFC1123_CUT, ml);
	private final static SimpleDateFormat dayGMT = new SimpleDateFormat("yyyy-MM-dd", ml);

	static {
		datemeGMT.setTimeZone(java.util.TimeZone.getTimeZone("GMT+00:00"));
		dayGMT.setTimeZone(java.util.TimeZone.getTimeZone("GMT+00:00"));
	}

	public static String getFormattedDateDay(long date) {
		String s = dayGMT.format(new Date(date));
		return s;
	}
	
	public static long parseDateDay(String date) throws Exception {
		return dayGMT.parse(date).getTime();
	}
	
	public static String getFormattedDate(long date) {
		String s = datemeGMT.format(new Date(date));
		return s;
	}
	public static long parseDate(String date) throws Exception {
		//return 50923486509234860L;
		return datemeGMT.parse(date).getTime();
	}
	
	public static void ensureBC() {
		if(Security.getProvider("BC")==null) {
			Security.addProvider(new BouncyCastleProvider());
		}
	}
	
	public static void sortKeyLogsbyDate(Vector<KeyLog> keylogs) {
		Collections.sort(keylogs, new Comparator<KeyLog>() { 
			
			public int compare(KeyLog kl1, KeyLog kl2) {
				try {
					return (int)(kl1.getActionDatetime()-kl2.getActionDatetime());
				}	catch (Exception ex) {
					ex.printStackTrace();
				}
				return 0;
			}
		});
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
	            
	            if(pad != '\0' && i < (off + length)-1) {
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
	    	if(data == null) {
	    		return null;
	    	}
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
	    	if(data == null) {
	    		return null;
	    	}
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
	
//	public static byte[] getSHA1MD5(byte[] data) {
//		byte[] ret = new byte[20 + 16]; //160 bit = 20 byte + md5 128bit = 16
//		org.bouncycastle.crypto.digests.SHA1Digest sha1 = new org.bouncycastle.crypto.digests.SHA1Digest();
//		sha1.update(data, 0,data.length);
//		sha1.doFinal(ret, 0);
//		
//		org.bouncycastle.crypto.digests.MD5Digest md5 = new org.bouncycastle.crypto.digests.MD5Digest();
//		md5.update(data, 0, data.length);
//		md5.doFinal(ret, 20);
//		
//		return ret;
//	}
	
	public static byte[][] getMD5SHA1SHA256(byte[] data) {
		byte[] ret = new byte[16 + 20 + 32]; //160 bit = 20 byte + md5 128bit = 16 + sha256 256bit = 32 byte 
		byte[] md5ret = new byte[16];
		byte[] sha1ret = new byte[20];
		byte[] sha256ret = new byte[32];
		
		org.bouncycastle.crypto.digests.MD5Digest md5 = new org.bouncycastle.crypto.digests.MD5Digest();
		md5.update(data, 0, data.length);
		md5.doFinal(ret, 0);
		
		org.bouncycastle.crypto.digests.SHA1Digest sha1 = new org.bouncycastle.crypto.digests.SHA1Digest();
		sha1.update(data, 0,data.length);
		sha1.doFinal(ret, 16);
		
		org.bouncycastle.crypto.digests.SHA256Digest sha256 = new org.bouncycastle.crypto.digests.SHA256Digest();
		sha256.update(data, 0,data.length);
		sha256.doFinal(ret, 16+20);
		
		System.arraycopy(ret, 0, md5ret, 0, md5ret.length);
		System.arraycopy(ret, 16, sha1ret, 0, sha1ret.length);
		System.arraycopy(ret, 16+20, sha256ret, 0, sha256ret.length);
		
		return new byte[][]{
				ret,
				md5ret,
				sha1ret,
				sha256ret
		};
	}
	public static byte[][] getMD5SHA1SHA256(File f) throws Exception {
		FileInputStream fin_ = new FileInputStream(f);
		BufferedInputStream fin = new BufferedInputStream(fin_);
		
		byte[][] ret =  getMD5SHA1SHA256(fin);
		fin.close();
		
		return ret;
	}
	
	
	public static byte[][] getMD5SHA1SHA256(InputStream fin) throws Exception {
		byte[] ret = new byte[16 + 20 + 32]; //160 bit = 20 byte + md5 128bit = 16 + sha256 256bit = 32 byte 
		byte[] md5ret = new byte[16];
		byte[] sha1ret = new byte[20];
		byte[] sha256ret = new byte[32];
		
		org.bouncycastle.crypto.digests.MD5Digest md5 = new org.bouncycastle.crypto.digests.MD5Digest();
		org.bouncycastle.crypto.digests.SHA1Digest sha1 = new org.bouncycastle.crypto.digests.SHA1Digest();
		org.bouncycastle.crypto.digests.SHA256Digest sha256 = new org.bouncycastle.crypto.digests.SHA256Digest();
		
		int read = -1;
		byte[] buff = new byte[4096];
		while((read=fin.read(buff))!=-1) {
			md5.update(buff, 0, read);
			sha1.update(buff, 0, read);
			sha256.update(buff, 0, read);
		}
		
		sha1.doFinal(ret, 16);
		md5.doFinal(ret, 0);
		sha256.doFinal(ret, 16+20);
		
		System.arraycopy(ret, 0, md5ret, 0, md5ret.length);
		System.arraycopy(ret, 16, sha1ret, 0, sha1ret.length);
		System.arraycopy(ret, 16+20, sha256ret, 0, sha256ret.length);
		
		return new byte[][]{
				ret,
				md5ret,
				sha1ret,
				sha256ret
		};
	}
	
//	public static byte[] getSHA1MD5(InputStream in) throws Exception {
//		byte[] ret = new byte[20 + 16];//160 bit = 20 byte + 128bit = 16 byte
//		
//		org.bouncycastle.crypto.digests.MD5Digest md5 = new org.bouncycastle.crypto.digests.MD5Digest();
//		org.bouncycastle.crypto.digests.SHA1Digest sha1 = new org.bouncycastle.crypto.digests.SHA1Digest();
//		
//		int read = -1;
//		byte[] buff = new byte[1024];
//		while((read=in.read(buff))!=-1) {
//			sha1.update(buff, 0, read);
//			md5.update(buff, 0, read);
//		}
//		
//		sha1.doFinal(ret, 0);
//		md5.doFinal(ret, 20);
//		
//		return ret;
//	}
	
//	public static byte[] getSHA256MD5(InputStream in) {
//	
//		byte[] ret = new byte[48]; ////256 bit = 32 byte + 128bit = 16 byte
//		org.bouncycastle.crypto.digests.MD5Digest md5 = new org.bouncycastle.crypto.digests.MD5Digest();
//		org.bouncycastle.crypto.digests.SHA256Digest sha256 = new org.bouncycastle.crypto.digests.SHA256Digest();
//		int l = 0;
//		byte[] buffer = new byte[512];
//		try {
//			while ((l=in.read(buffer))>0) {
//				md5.update(buffer, 0, l);
//				sha256.update(buffer, 0, l);
//			}
//			sha256.doFinal(ret, 0);
//			md5.doFinal(ret, 32);
//			return ret;
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
	
	public static byte[] getSHA1(byte[] data) {
		byte[] ret = new byte[20]; //160 bit = 20 byte
		org.bouncycastle.crypto.digests.SHA1Digest sha1 = new org.bouncycastle.crypto.digests.SHA1Digest();
		sha1.update(data,0,data.length);
		sha1.doFinal(ret, 0);
		return ret;
	}
	
	public static byte[] getSHA1(InputStream in) throws Exception {
		byte[] ret = new byte[20];//160 bit = 20 byte
		org.bouncycastle.crypto.digests.SHA1Digest sha1 = new org.bouncycastle.crypto.digests.SHA1Digest();
		int read = -1;
		byte[] buff = new byte[1024];
		while((read=in.read(buff))!=-1) {
			sha1.update(buff, 0, read);
		}
		
		sha1.doFinal(ret, 0);
		return ret;
	}
	
	public static byte[] getSHA1LocalProof(Element e) throws Exception {
		Vector<Element> ve = new Vector<Element>();
		ve.add(e);
		return getSHA1LocalProof(ve);
	}
	public static byte[] getSHA1LocalProof(Vector<Element> ve) throws Exception {
		byte[] ret = new byte[20];//160 bit = 20 byte
		SHA1Digest sha1 = new SHA1Digest();
		//System.out.println("--- sha1localproof ---");
		for (Element e : ve) {
			rekursiveUpdateSHA1(sha1, e);
		}
		sha1.doFinal(ret, 0);
		//System.out.println("--- RESULT ----");
		//System.out.println(SecurityHelper.HexDecoder.encode(ret, ':',-1));
		return ret;
	}
	
	private static void rekursiveUpdateSHA1(SHA1Digest sha1, Element e) {
		Vector<Element> ve = e.getChildren();
		if (ve.size()>0) {
			for (Element el : ve) {
				rekursiveUpdateSHA1(sha1, el);
			}
		} else {
			String name = e.getName();
			Vector<String[]> attributes = e.getAttributes();
			String text = e.getText();
			if (name!=null && name.length()>0) {
				updateSha1(sha1, name);
			}
			if (attributes!=null) {
				for (String[] att : attributes) {
					for (int i=0;i<att.length;i++) {
						updateSha1(sha1,att[i]);
					}
				}
			}
			if (text!=null && text.length()>0) {
				updateSha1(sha1,text);
			}
		}
	}
	
	public static byte[] getSHA256LocalProof(Element e) throws Exception {
		Vector<Element> ve = new Vector<Element>();
		ve.add(e);
		return getSHA256LocalProof(ve);
	}
	public static byte[] getSHA256LocalProof(Vector<Element> ve) throws Exception {
		byte[] ret = new byte[32];//256 bit = 32 byte
		SHA256Digest sha256 = new SHA256Digest();
		//System.out.println("--- sha1localproof ---");
		for (Element e : ve) {
			rekursiveUpdateSHA256(sha256, e);
		}
		sha256.doFinal(ret, 0);
		//System.out.println("--- RESULT ----");
		//System.out.println(SecurityHelper.HexDecoder.encode(ret, ':',-1));
		return ret;
	}
	
	private static void rekursiveUpdateSHA256(SHA256Digest sha256, Element e) {
		Vector<Element> ve = e.getChildren();
		if (ve.size()>0) {
			for (Element el : ve) {
				rekursiveUpdateSHA256(sha256, el);
			}
		} else {
			String name = e.getName();
			Vector<String[]> attributes = e.getAttributes();
			String text = e.getText();
			if (name!=null && name.length()>0) {
				updateSha256(sha256, name);
			}
			if (attributes!=null) {
				for (String[] att : attributes) {
					for (int i=0;i<att.length;i++) {
						updateSha256(sha256,att[i]);
					}
				}
			}
			if (text!=null && text.length()>0) {
				updateSha256(sha256,text);
			}
		}
	}
	
	private static void updateSha1(SHA1Digest sha1, String s) {
		try {
			byte[] b = s.getBytes("UTF-8");
			sha1.update(b, 0, b.length);
			//System.out.println("update sha1: "+s);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private static void updateSha256(SHA256Digest sha256, String s) {
		try {
			byte[] b = s.getBytes("UTF-8");
			//System.out.println("sha256::"+s);
			sha256.update(b, 0, b.length);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static byte[] getSHA256(byte[] data) {
		byte[] ret = new byte[32]; //256 bit = 32 byte
		org.bouncycastle.crypto.digests.SHA256Digest sha256 = new org.bouncycastle.crypto.digests.SHA256Digest();		
		sha256.update(data, 0, data.length);
		sha256.doFinal(ret, 0);
		return ret;
	}
//	public static byte[] getSHA256(InputStream in) {
//		org.bouncycastle.crypto.digests.SHA256Digest sha256 = new org.bouncycastle.crypto.digests.SHA256Digest();		
//		int l = 0;
//		byte[] buffer = new byte[512];
//		try {
//			while ((l=in.read(buffer))>0) {
//				sha256.update(buffer, 0, l);
//			}
//			byte[] ret = new byte[32]; //256 bit = 32 byte
//			sha256.doFinal(ret, 0);
//			return ret;
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
	
	
	public static byte[] getMD5(byte[] data) {
		org.bouncycastle.crypto.digests.MD5Digest md5 = new org.bouncycastle.crypto.digests.MD5Digest();
		byte[] ret = new byte[md5.getDigestSize()];
		md5.update(data, 0, data.length);
		md5.doFinal(ret, 0);
		return ret;
	}
	
	public static byte[] getMD5(InputStream in) {
		org.bouncycastle.crypto.digests.MD5Digest md5 = new org.bouncycastle.crypto.digests.MD5Digest();
		int l = 0;
		byte[] buffer = new byte[512];
		try {
			while ((l=in.read(buffer))>0) {
				md5.update(buffer, 0, l);
			}
			byte[] ret = new byte[md5.getDigestSize()];
			md5.doFinal(ret, 0);
			return ret;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static byte[] concat(byte[] a, byte[] b) {
		byte[] c = new byte[a.length+b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}
	
	public static byte[] getRandomBytes(int bytecount) {
		SecureRandom sc = new SecureRandom();//TODO HT 20.02.2011 - quite good, but should swirl it twice with tiger, or aes/rijndael itself		
		byte[] rb = new byte[bytecount];
        sc.nextBytes(rb);
        return rb;
	}
	
	public static void signoffElement(Element e, OSDXKey signoffkey) throws Exception {
		//signoff
		byte[] sha1proof = SecurityHelper.getSHA1LocalProof(e);
		e.addContent("sha1localproof", SecurityHelper.HexDecoder.encode(sha1proof, ':', -1));
		e.addContent(Signature.createSignatureFromLocalProof(sha1proof, "signature of sha1localproof", signoffkey).toElement());
	}
	
//	public static boolean checkElementsSHA1localproofAndSignature(Element e, Vector<OSDXKey> trustedKeys) throws Exception {
//		OSDXKey signingKey = null;
//		byte[] modulus = SecurityHelper.HexDecoder.decode(e.getChild("signature").getChild("pubkey").getChildText("modulus"));
//		for (OSDXKey k: trustedKeys) {
//			if (Arrays.equals(k.getPubKey().getModulusBytes(), modulus)) {
//				signingKey = k;
//				break;
//			}
//		}
//		if (signingKey !=null) {
//			return SecurityHelper.checkElementsSHA1localproofAndSignature(e, signingKey);
//		} else {
//			//TODO if (signingKey == null) try to find approved signing key on server
//			throw new RuntimeException("signing key NOT in trusted keys: keyid: "+e.getChild("signature").getChildText("keyid"));
//		}
//	}
	
//	public static boolean checkSHA1localproofAndSignature(Vector<Element> toProof, byte[] givenSha1localproof, Signature signature, PublicKey signingKey) {
//		
//		boolean verified = true;
//		try {
//			byte[] calcSha1localproof = SecurityHelper.getSHA1LocalProof(toProof);
//			if (!Arrays.equals(givenSha1localproof, calcSha1localproof)) {
//				System.out.println("given sha1: "+SecurityHelper.HexDecoder.encode(givenSha1localproof, ':', -1));
//				System.out.println("calc  sha1: "+SecurityHelper.HexDecoder.encode( calcSha1localproof, ':', -1));
//				verified = false;
//			}
//		} catch (Exception ex) {
//			ex.printStackTrace();
//			return false;
//		}
//		
//		//if (verified) System.out.println("checking modulus");
//		
//		//check modulus belongs to keyid
//		byte[] givenModulus = signature.getKey().getPubKey().getModulusBytes();
//		if (verified && !Arrays.equals(signingKey.getModulusBytes(),givenModulus)) {
//			verified = false;
//			System.out.println("given     modulus: "+SecurityHelper.HexDecoder.encode(givenModulus, ':', -1));
//			System.out.println("masterkey modulus: "+SecurityHelper.HexDecoder.encode(signingKey.getModulusBytes(), ':', -1));
//			System.out.println("modulus verification FAILED!");
//		}
//		
//		//check signaturebytes match sha1localproof
//		if (verified) {
//			try {
//				verified = signature.tryVerificationMD5SHA1SHA256(givenSha1localproof);
//				return verified;
//			} catch (Exception ex) {
//				ex.printStackTrace();
//				return false;
//			}
////			System.out.println("checking signaturebytes");	
////			System.out.println("signature data   md5   : "+e.getChild("signature").getChild("data").getChildText("md5"));
////			System.out.println("signature data   sha1  : "+e.getChild("signature").getChild("data").getChildText("sha1"));
////			System.out.println("signature data   sha256: "+e.getChild("signature").getChild("data").getChildText("sha256"));
////			byte[][] data = SecurityHelper.getMD5SHA1SHA256(givenSha1localproof);
////			System.out.println("calc      data   md5   : "+SecurityHelper.HexDecoder.encode(data[1],':',-1));
////			System.out.println("calc      data   sha1  : "+SecurityHelper.HexDecoder.encode(data[2],':',-1));
////			System.out.println("calc      data   sha256: "+SecurityHelper.HexDecoder.encode(data[3],':',-1));
//		}
//		return verified;
//	}
	
//	public static boolean checkElementsSHA1localproofAndSignature(Element e, OSDXKey signingKey) throws Exception {
//		//get sha1localproof
//		byte[] givenSha1localproof = SecurityHelper.HexDecoder.decode(e.getChildText("sha1localproof"));
//		//get signature
//		Signature signature = Signature.fromElement(e.getChild("signature"));
//		
//		//build toProof <- all content but sha1localproof and Signature
//		Vector<Element> toProof = new Vector<Element>();
//		Vector<Element> children = e.getChildren();
//		for (Element c : children) {
//			if (!c.getName().equals("sha1localproof") && !c.getName().equals("signature")) {
//				toProof.add(c);
//			}
//		}
//		
//		return checkSHA1localproofAndSignature(toProof, givenSha1localproof, signature, signingKey.getPubKey());
//		
//	}
	
}

