package org.fnppl.opensdx.file_transfer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.UnsupportedEncodingException;

import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.security.SymmetricKey;

public class SecureConnection {

	private boolean DEBUG = false;
	
	//request
	public static byte TYPE_TEXT          = -52; // = CC
	public static byte TYPE_DATA          = -35; // = DD

	//response
	public static byte TYPE_ACK           = -86; // = AA
	public static byte TYPE_ACK_WITH_MD5  = -95; // = A1  for upload/download of file (COULD)
	public static byte TYPE_ACK_COMPLETE  = -94; // = A2  upload/download complete
	
	public static byte TYPE_ABORT_COMMAND = -69; // = BB

	public static byte TYPE_ERROR_E0      = -32; // = E0 //command not understood
	public static byte TYPE_ERROR_E1      = -31; // = E1 
	public static byte TYPE_ERROR_E2      = -30; // = E2
	public static byte TYPE_ERROR_E3      = -29; // = E3
	public static byte TYPE_ERROR_E4      = -28; // = E4
	public static byte TYPE_ERROR_E5      = -27; // = E5
	public static byte TYPE_ERROR_E6      = -26; // = E6
	public static byte TYPE_ERROR_E7      = -25; // = E7
	public static byte TYPE_ERROR_E8      = -24; // = E8
	public static byte TYPE_ERROR_E9      = -23; // = E9
	public static byte TYPE_ERROR_EA      = -22; // = EA
	public static byte TYPE_ERROR_EB      = -21; // = EB
	public static byte TYPE_ERROR_EC      = -20; // = EC
	public static byte TYPE_ERROR_ED      = -19; // = ED
	public static byte TYPE_ERROR         = -18; // = EE // error with error message
	
	public long id;
	public int num;
	public byte type;
	int len = 0;
	public byte[] content;
	public SymmetricKey key;
	
	public BufferedInputStream in;
	public BufferedOutputStream out;

	public SecureConnection(SymmetricKey key, BufferedInputStream in, BufferedOutputStream out) {
		this.key = key;
		this.out = out;
		this.in = in;
	}

	public boolean receiveNextPackage() throws Exception {

		//read package header
		byte[] header = new byte[32];
		int read = 1;
		int offset = 0;
		boolean headerNotComplete = true;
		while (headerNotComplete && read>0) {//don't block, if socket is closed
			read = in.read(header,offset,32-offset); //this one blocks
			if (read>0) {
				offset += read;
				if (offset>=32) {
					//System.out.println("enc header = "+SecurityHelper.HexDecoder.encode(header));
					//header complete -> read values
					header = key.decrypt(header);
					id = bytesToLong(header, 0);
					num = bytesToInt(header, 8);
					type = header[12];
					header[12] = (byte)0;
					len = bytesToInt(header, 12);
					
					if (DEBUG) header[12] = type; System.out.println("header = "+SecurityHelper.HexDecoder.encode(header)+" :: id="+id+"\ttype="+SecurityHelper.HexDecoder.encode(new byte[]{type})+"\tlen="+len);
	
					headerNotComplete = false;
				}
			}
			else if (read<0){
				return false; //socket closed
			}
		}
		//read content
		if (len<=0) { 
			content = null;
		} else {
			content = new byte[len];
			offset=0;
			while (read>0 && offset<len) {//don't block, if socket is closed
				read = in.read(content,offset,len-offset); //this one blocks
				if (read>0) {
					offset += read;
				}
				else if (read<0){
					return false; //socket closed
				}
			}
			if (offset==len) {
				//System.out.println("enc content = "+SecurityHelper.HexDecoder.encode(content));
				//content complete -> decrypt
				content = key.decrypt(content);
			}
		}
		return true;
	}

	public void setAck(long id, int num) {
		this.id = id;
		this.num = num;
		this.type = TYPE_ACK;
		this.content = null;
	}
	public void setAck(long id, int num, String message) {
		this.id = id;
		this.num = num;
		this.type = TYPE_ACK;
		setContent(message);
	}
	public void setAckComplete(long id, int num) {
		this.id = id;
		this.num = num;
		this.type = TYPE_ACK_COMPLETE;
		this.content = null;
	}

	public void setError(long id, int num, String message) {
		this.id = id;
		this.num = num;
		this.type = TYPE_ERROR;
		setContent(message);
	}
	
	public void setErrorCommandNotUnderstood(long id, int num) {
		this.id = id;
		this.num = num;
		this.type = TYPE_ERROR_E0;
		this.content = null;
	}

	public void setData(long id, int num, byte[] content) {
		System.out.println("SETTING DATA:: len="+content.length);
		this.id = id;
		this.num = num;
		this.type = TYPE_DATA;
		this.content = content;
	}

	public void setCommand(long id, String command) {
		this.id = id;
		this.num = 0;
		this.type = TYPE_TEXT;
		setContent(command);
	}
	
	private void setContent(String command) {
		try {
			this.content = command.getBytes("UTF-8");
		} catch (UnsupportedEncodingException ex) {	ex.printStackTrace();}	
	}

	public void sendPackage() throws Exception {
		//content
		len = 0;
		byte[] encContent = null;
		if (content!=null) {
			encContent = key.encrypt(content);
			len = encContent.length;
			if (len>16777216) {
				throw new RuntimeException("Max. 16Mb of content allowed.");
			}
		}

		//header
		byte[] header = buildPackageHeader(id, num, type, len);
		byte[] encHeader = key.encrypt(header);

		//package
		if (DEBUG) {
			System.out.println("header:      "+SecurityHelper.HexDecoder.encode(header));
			System.out.println("header: enc  "+SecurityHelper.HexDecoder.encode(encHeader));
			System.out.println("content:     "+SecurityHelper.HexDecoder.encode(content));
			System.out.println("content: enc "+SecurityHelper.HexDecoder.encode(encContent));
			//System.out.println("package:     "+SecurityHelper.HexDecoder.encode(encHeader)+SecurityHelper.HexDecoder.encode(encContent));
		}
		
		out.write(encHeader);
		if (encContent!=null) {
			out.write(encContent);
		}
		out.flush();
	}
	
	public void sendRawBytes(byte[] data) throws Exception {
		byte[] len = new byte[4];
		intToBytes(data.length, len, 0);
		out.write(len);
		out.write(data);
		out.flush();
	}
	
	public byte[] receiveRawBytesPackage() throws Exception {
		//read package header
		byte[] header = new byte[4];
		int read = 1;
		int offset = 0;
		boolean headerNotComplete = true;
		int rawDataLen = 0;
		while (headerNotComplete && read>0) {//don't block, if socket is closed
			read = in.read(header,offset,4-offset); //this one blocks
			if (read>0) {
				offset += read;
				if (offset>=4) {
					rawDataLen = bytesToInt(header, 0);
					headerNotComplete = false;
				}
			}
			else if (read<0){
				return null; //socket closed
			}
		}
		//read content
		byte[] content = null;
		if (rawDataLen>0) { 
			content = new byte[rawDataLen];
			offset=0;
			while (read>0 && offset<rawDataLen) {//don't block, if socket is closed
				read = in.read(content,offset,rawDataLen-offset); //this one blocks
				if (read>0) {
					offset += read;
				}
				else if (read<0){
					return null; //socket closed
				}
			}
		}
		return content;
	}
	
	public void setKey(SymmetricKey key) {
		this.key = key;
	}
	
	public boolean isError() {
		if (type <= TYPE_ERROR && type >= TYPE_ERROR_E0) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isError(byte code) {
		if (code <= TYPE_ERROR && code >= TYPE_ERROR_E0) {
			return true;
		} else {
			return false;
		}
	}

	private static byte[] buildPackageHeader(long id, int num, byte type, int length) {
		byte[] b = new byte[16];
		longToBytes(id, b, 0);
		intToBytes(num, b, 8);
		intToBytes(length, b, 12);
		b[12] = type;
		return b;
	}

	private static long bytesToLong(byte[] b, int startPos) {
		long l = 0;
		for (int i = 0; i < 8; i++) {
			l = (l << 8) + (b[i+startPos] & 0xff);
		}
		return l;
	}

	private static int bytesToInt(byte[] b, int startPos) {
		int v = 0;
		for (int i = 0; i < 4; i++) {
			v = (v << 8) + (b[i+startPos] & 0xff);
		}
		return v;
	}

//	private static byte[] longToByte(long l) {
//		byte[] b = new byte[8];
//		for(int i= 0; i < 8; i++){  
//			b[7 - i] = (byte)(l >>> (i * 8));  
//		} 
//		return b;
//	}

	private static void longToBytes(long l, byte[] b, int startPos) {
		startPos += 7; //reverse order
		for(int i= 0; i < 8; i++){  
			b[startPos-i] = (byte)(l >>> (i * 8));  
		}
	}

	private static void intToBytes(int v, byte[] b, int startPos) {
		startPos += 3; //reverse order
		for(int i= 0; i < 4; i++){  
			b[startPos-i] = (byte)(v >>> (i * 8));  
		}
	}

	
	private static Object sync_id = new Object();
	private static long lastTimeStamp = System.currentTimeMillis();

	public static long getID() {
		long now = System.currentTimeMillis();
		synchronized(sync_id) {
			if(now <= lastTimeStamp) {
				now = lastTimeStamp +1 ;
			}
			lastTimeStamp = now;
		}
		return now;
	}
	
	
	
	public static void main(String args[]) {
		try {
			
			for (byte b =-126;b<127;b++) {
				System.out.println(b+" "+SecurityHelper.HexDecoder.encode(new byte[]{b}));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
