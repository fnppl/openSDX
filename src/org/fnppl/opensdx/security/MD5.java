package org.fnppl.opensdx.security;

public class MD5 {

	private org.bouncycastle.crypto.digests.MD5Digest md5;
	
	public MD5() {
		md5 = new org.bouncycastle.crypto.digests.MD5Digest();
	}
	
	public void update(byte[] data) {
		md5.update(data, 0, data.length);
	}
	
	public void update(byte[] data, int length) {
		md5.update(data, 0, length);
	}
	
	public byte[] getMD5bytes() {
		byte[] ret = new byte[md5.getDigestSize()];
		md5.doFinal(ret, 0);
		
		return ret;
	}
	
	public String getMD5HexString() {
		return SecurityHelper.HexDecoder.encode(getMD5bytes()); 
	}
	
//	public static void main(String[] a) {
//		MD5 md5 = new MD5();
//		md5.update(new byte[] {1,5,12,55,1,});
//		
//		System.out.println("md5 1   "+md5.getMD5HexString());
//		md5.update(new byte[] {1,5,12,55,1,});
//		System.out.println("md5 2   "+md5.getMD5HexString());
//		
//		md5 = new MD5();
//		md5.update(new byte[] {1,5,12,55,1,});
//		md5.update(new byte[] {1,5,12,55,1,});
//		System.out.println("md5 3   "+md5.getMD5HexString());
//	}
	
}
