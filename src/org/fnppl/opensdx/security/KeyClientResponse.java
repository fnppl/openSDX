package org.fnppl.opensdx.security;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import org.fnppl.opensdx.security.*;
import org.fnppl.opensdx.xml.*;

//http://de.wikipedia.org/wiki/Hypertext_Transfer_Protocol

public class KeyClientResponse {

	public static boolean debug = false;
	public Hashtable<String, String> headers = null;
	public Vector<String> headerName = new Vector<String>();
	public Document doc;
	public String status;
	
	public static KeyClientResponse fromStream(BufferedInputStream in, long timeout) throws Exception {
		KeyClientResponse re = new KeyClientResponse();
		re.headers = new Hashtable<String, String>();
		re.doc = null;
		re.status = readLine(in);
		if (debug) System.out.println(re.status);
		
		readHeader(in, re);
		if (re.headers.containsKey("Content-Type") && re.headers.get("Content-Type").equals("text/xml")) {
			readXMLContent(in, re);
		}
		if (debug) if (re.doc!=null) re.doc.output(System.out);
		
		if (debug) System.out.println("OSDXKeyServerClient | end requestMasterPubKeys");
		
		return re;
	}
	
	public boolean hasErrorMessage() {
		return (doc!=null && doc.getRootElement().getName().equals("errormessage"));
	}
	
	public String getErrorMessage() {
		if (hasErrorMessage())
			return doc.getRootElement().getChildText("message");
		return null;
	}
	
	private static void readXMLContent(BufferedInputStream in, KeyClientResponse re) throws Exception {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		int read = 0;
		byte[] buff = new byte[1024];
		while((read=in.read(buff))!=-1) {
			bout.write(buff, 0, read);
		}
		String s = new String(bout.toByteArray());
		String last = s.substring(s.lastIndexOf(">"));
		//System.out.println("last bytes: "+SecurityHelper.HexDecoder.encode(last.getBytes("UTF-8"), ':',-1));
		s = s.substring(0, s.lastIndexOf(">")+3);
		String last2 = s.substring(s.lastIndexOf(">"));
		//System.out.println("last bytes: "+SecurityHelper.HexDecoder.encode(last2.getBytes("UTF-8"), ':',-1));
		
		//System.out.println("GOT THIS AS DOC: ::"+s+"::");
		re.doc = Document.fromStream(new ByteArrayInputStream(s.getBytes()));
		//re.doc = Document.fromStream(new ByteArrayInputStream(bout.toByteArray()));
	}
	
	private static void readHeader(BufferedInputStream in, KeyClientResponse re) throws Exception {
		String zeile = null;

		while ((zeile=readLine(in))!=null) {
			if (debug) System.out.println(zeile);
			if(zeile.length()==0) {
				return;//heade-ende
			}
			//header
			String[] p = parseHeader(zeile);
			re.headers.put(p[0], p[1]);
			re.headerName.add(p[0]);
			//System.out.println("h: "+zeile);
		}

	}

	private static String[] parseHeader(String zeile) {
		String[] ret = new String[2];
		ret[0] = zeile.substring(0, zeile.indexOf(" "));
		if (ret[0].endsWith(":")) ret[0] = ret[0].substring(0,ret[0].length()-1);
		ret[1] = zeile.substring(zeile.indexOf(" ")+1);
		return ret;
	}

	private static String readLine(BufferedInputStream in) throws Exception {
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

	public void toOutput(OutputStream out) {
		try {	
			out.write((status+"\n").getBytes());
			
			for (String key : headerName) {
				out.write((key+": "+headers.get(key)+"\n").getBytes());
			}
			if (doc!=null) {
				doc.output(out);
			}
			out.write(("\n").getBytes());
		} catch (Exception ex) {
			
		}
	}
}
