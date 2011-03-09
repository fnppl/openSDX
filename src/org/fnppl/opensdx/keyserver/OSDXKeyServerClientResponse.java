package org.fnppl.opensdx.keyserver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Hashtable;

import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.xml.Document;

public class OSDXKeyServerClientResponse {

	public Hashtable<String, String> headers = null;
	public Document doc;
	public String status;

	
	public static OSDXKeyServerClientResponse fromStream(InputStream in, long timeout) throws Exception {
		OSDXKeyServerClientResponse re = new OSDXKeyServerClientResponse();
		re.headers = new Hashtable<String, String>();
		re.doc = null;

		readHeader(in, re);
		System.out.println("::header end::");
		if (re.headers.containsKey("Content-Type") && re.headers.get("Content-Type").equals("text/xml")) {
			readXMLContent(in, re);
		}

		System.out.println("OSDXKeyServerClient | end requestMasterPubKeys");

		if (re.doc!=null) {
			re.doc.output(System.out);
		}
		return re;
	}
	
	private static void readXMLContent(InputStream in, OSDXKeyServerClientResponse re) throws Exception {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		int read = 0;
		byte[] buff = new byte[1024];
		while((read=in.read(buff))!=-1) {
			bout.write(buff, 0, read);
		}
		String s = new String(bout.toByteArray());
		String last = s.substring(s.lastIndexOf(">"));
		System.out.println("last bytes: "+SecurityHelper.HexDecoder.encode(last.getBytes("UTF-8"), ':',-1));
		s = s.substring(0, s.lastIndexOf(">")+3);
		String last2 = s.substring(s.lastIndexOf(">"));
		System.out.println("last bytes: "+SecurityHelper.HexDecoder.encode(last2.getBytes("UTF-8"), ':',-1));
		
		//System.out.println("GOT THIS AS DOC: ::"+s+"::");
		re.doc = Document.fromStream(new ByteArrayInputStream(s.getBytes()));
		//re.doc = Document.fromStream(new ByteArrayInputStream(bout.toByteArray()));
	}
	
	private static void readHeader(InputStream in, OSDXKeyServerClientResponse re) throws Exception {
		String zeile = null;

		while ((zeile=readLine(in))!=null) {
			if(zeile.length()==0) {
				return;//heade-ende
			}
			//header
			String[] p = parseHeader(zeile);
			re.headers.put(p[0], p[1]);
			System.out.println("h: "+zeile);
		}

	}

	private static String[] parseHeader(String zeile) {
		String[] ret = new String[2];
		ret[0] = zeile.substring(0, zeile.indexOf(" "));
		if (ret[0].endsWith(":")) ret[0] = ret[0].substring(0,ret[0].length()-1);
		ret[1] = zeile.substring(zeile.indexOf(" ")+1);
		return ret;
	}

	private static String readLine(InputStream in) throws Exception {
		//if (in.available()<=0) return null;
		ByteArrayOutputStream bout = new ByteArrayOutputStream();

		//HEADERS are ASCII

		byte[] b = new byte[1];
		int r = 0;

		char last='\r';

		while((r=in.read(b)) > 0) {
			char m = (char)b[0];
			if(m == '\n') {
				break;
			} else if(m != '\r') {
				bout.write(b[0]);
			}  
		}

		if(r<0 && bout.size()==0) {
			return null;
		}
		String s = new String(bout.toByteArray(), "ASCII");
		// System.out.println("OSDXKeyServerClient | "+s);
		return s;
	}

}
