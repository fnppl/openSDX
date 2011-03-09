package org.fnppl.opensdx.keyserver;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Hashtable;

import org.fnppl.opensdx.xml.Document;

public class OSDXKeyServerClientResponse {

	public Hashtable<String, String> headers = null;
	public Document doc;
	public String status;

	public static OSDXKeyServerClientResponse fromStream(InputStream in, long timeout) throws Exception {
		OSDXKeyServerClientResponse re = new OSDXKeyServerClientResponse();
		re.headers = new Hashtable<String, String>();
		re.doc = null;


		boolean responseReady = false;
		String contentType = null;
		int contentLen = -1;
		boolean startContent = false;

		//StringBuffer content = new StringBuffer();

		long startTime = System.currentTimeMillis();
		while (System.currentTimeMillis()<startTime+timeout || responseReady) {
			String z = readLine(in);
			if (z!=null) {
				if (re.status==null) re.status = z;
				if (z.equals("") && !startContent) {
					startContent = true;
				} else {
					if (!startContent) {
						//header
						String[] p = parseHeader(z);
						re.headers.put(p[0], p[1]);
						System.out.println("h: "+z);
					}
				}
				if (startContent) {
					System.out.println("c: ::StartContent::");
					if (re.headers.containsKey("Content-Type") && re.headers.get("Content-Type").equals("text/xml")) {
						re.doc = Document.fromStream(in);
					}
					//content.append(z);
				}
			}
		}

		System.out.println("OSDXKeyServerClient | end requestMasterPubKeys");

		if (re.doc!=null) {
			re.doc.output(System.out);
		}
		
		return re;
	}

	private static String[] parseHeader(String zeile) {
		String[] ret = new String[2];
		ret[0] = zeile.substring(0, zeile.indexOf(" "));
		if (ret[0].endsWith(":")) ret[0] = ret[0].substring(0,ret[0].length()-1);
		ret[1] = zeile.substring(zeile.indexOf(" ")+1);
		return ret;
	}

	private static String readLine(InputStream in) throws Exception {
		if (in.available()<=0) return null;
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

		if(bout.size()==0) {
			return "";
		}
		String s = new String(bout.toByteArray(), "ASCII");
		// System.out.println("OSDXKeyServerClient | "+s);
		return s;
	}

}
