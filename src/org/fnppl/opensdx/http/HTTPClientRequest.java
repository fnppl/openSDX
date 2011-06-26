package org.fnppl.opensdx.http;


import java.util.*;
import java.io.*;
import java.net.*;

import org.fnppl.opensdx.security.*;
import org.fnppl.opensdx.security.SecurityHelper.HexDecoder;
import org.fnppl.opensdx.xml.*;

//import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

//http://de.wikipedia.org/wiki/Hypertext_Transfer_Protocol

public class HTTPClientRequest {
	public final static String XMLDOCPARAMNAME = "xmldocument";//check out KeyServerRequest

	public final static String TYPE_OSDX_ENCRYPTED = "application/osdx-encrypted";
	private final static String version = "HTTP/1.1";
	private String method = "POST";
	
	private String contentType = "application/x-www-form-urlencoded";
	
	private String uri;
	private String host;
	private Element contentElement;
	private byte[] contentData = null;
	private Hashtable<String, String> parameters = new Hashtable<String, String>();
	//private Hashtable<String, String> headers = new Hashtable<String, String>();
	private Vector<String[]> headers = new Vector<String[]>();
	protected OSDXKey signoffkey = null;
	
	
	public void setSignoffKey(OSDXKey signoffkey) {
		this.signoffkey = signoffkey;
	}
	
	public String getURI() {
		return uri;
	}
	public void setURI(String host, String uri) {
		this.uri = uri;
		this.host = host;
	}
	public void setContentType(String ct) {
		this.contentType = ct;
	}
	public void toggleGETMode() {
		this.method = "GET";
		toggleFormDataMode();
	}
	public void toggleFormDataMode() {
		this.contentType = "application/x-www-form-urlencoded";
	}
	public void toggleXMLDataMode() {
		this.contentType = "text/xml";
	}
	
	public void togglePOSTMode() {
		this.method = "POST";
		toggleFormDataMode();
	}
	
	public void setContentEncryptedData(byte[] bytes) {
		togglePOSTMode();
		contentType = TYPE_OSDX_ENCRYPTED;
		contentData = bytes;
	}
	
	public HTTPClientRequest() {
	}
	private String getCMDLine() throws Exception {
		StringBuffer ret = new StringBuffer();
		ret.append(method);
		ret.append(" ");
		ret.append(uri);
		if(method.equals("GET")) {
			ret.append("?");
			Iterator<String> its = parameters.keySet().iterator();
			while(its.hasNext()) {
				String pn = its.next();
				String pv = parameters.get(pn);
				ret.append(URLEncoder.encode(pn, "UTF-8")+"="+URLEncoder.encode(pv, "UTF-8"));
				if(its.hasNext()) {
					ret.append("&");
				}
			}
		}
		ret.append(" ");
		ret.append(version);
		return ret.toString();
	}
	
	private String getCMDLineNOT_URL_ENCODED_FOR_TESTING() throws Exception {
		StringBuffer ret = new StringBuffer();
		ret.append(method);
		ret.append(" ");
		ret.append(uri);
		if(method.equals("GET")) {
			ret.append("?");
			Iterator<String> its = parameters.keySet().iterator();
			while(its.hasNext()) {
				String pn = its.next();
				String pv = parameters.get(pn);
				ret.append(pn+"="+pv);
				if(its.hasNext()) {
					ret.append("&");
				}
			}
		}
		ret.append(" ");
		ret.append(version);
		return ret.toString();
	}
	
	private static String getHeader(int contentlength, String contentType, String hostname) {
		StringBuffer ret = new StringBuffer();
		ret.append("Content-Type: "+contentType+"\r\n");
		ret.append("Content-Length: "+contentlength+"\r\n");
		ret.append("Host: "+hostname+"\r\n");
//		ret.append("\r\n");
		return ret.toString();
	}
	
	//public void toOutput(BufferedOutputStream out) throws Exception {
	public void toOutput(OutputStream out) throws Exception {
		//write it to the outputstream...
		if(contentType.equalsIgnoreCase("application/x-www-form-urlencoded")) {
			if (contentElement != null) {
				Element eContent = contentElement;
				//signoff if signoffkey present
				if (signoffkey != null) {
					eContent = XMLHelper.cloneElement(contentElement); //HT 23.03.2011 - why cloned?!
					//signoff
					byte[] sha1proof = SecurityHelper.getSHA1LocalProof(eContent);
					eContent.addContent("sha1localproof", SecurityHelper.HexDecoder.encode(sha1proof, ':', -1));
					eContent.addContent(Signature.createSignatureFromLocalProof(sha1proof, "signature of sha1localproof", signoffkey).toElement());
				}
				
				Document xml = Document.buildDocument(eContent);
				
				StringBuffer toSend = new StringBuffer();
				toSend.append(XMLDOCPARAMNAME+"=");
				toSend.append(URLEncoder.encode(xml.toString(), "UTF-8"));
				
				Iterator<String> its = parameters.keySet().iterator();
				while(its.hasNext()) {
					toSend.append("&");
					String pn = its.next();
					String pv = parameters.get(pn);
					toSend.append(URLEncoder.encode(pn, "UTF-8")+"="+URLEncoder.encode(pv, "UTF-8"));
				}
				
				byte[] content = toSend.toString().getBytes("ASCII");
				toSend = null;
				
				out.write(getCMDLine().getBytes("ASCII"));
				out.write("\r\n".getBytes("ASCII"));
				out.write(getHeader(content.length, contentType, host).getBytes("ASCII"));
				out.write("\r\n".getBytes("ASCII"));
				
				out.flush();
				out.write(content);
			}
			else {
				//content-element==null
				out.write(getCMDLine().getBytes("ASCII"));
				out.write("\r\n".getBytes("ASCII"));
				out.write(getHeader(0, contentType, host).getBytes("ASCII"));
				out.write("\r\n".getBytes("ASCII"));
			}
			
			out.flush();
		}
		else if(contentType.equalsIgnoreCase(TYPE_OSDX_ENCRYPTED) && contentData!=null) {
			out.write(getCMDLine().getBytes("ASCII"));
			out.write("\r\n".getBytes("ASCII"));
			for (String[] h : headers) {
				out.write((h[0]+": "+h[1]+"\r\n").getBytes("ASCII"));
			}
			out.write(getHeader(contentData.length, contentType, host).getBytes("ASCII"));
			out.write("\r\n".getBytes("ASCII"));
			out.write(contentData);
			out.flush();
		}
		else if(contentType.equalsIgnoreCase("text/xml")) {
			if(true) {
				throw new RuntimeException("Hmm, thou shalt not use me...");
			}
			
//			if (contentElement != null) {
//				Element eContent = contentElement;
//				//signoff if signoffkey present
//				if (signoffkey!=null) {
//					eContent = XMLHelper.cloneElement(contentElement);//HT 23.03.2011 - why cloned?!
//					//signoff
//					byte[] sha1proof = SecurityHelper.getSHA1LocalProof(eContent);
//					eContent.addContent("sha1localproof", SecurityHelper.HexDecoder.encode(sha1proof, ':', -1));
//					eContent.addContent(Signature.createSignatureFromLocalProof(sha1proof, "signature of sha1localproof", signoffkey).toElement());
//				}
//				
//				
//				ByteArrayOutputStream contentout = new ByteArrayOutputStream();
//				
//				Document xml = Document.buildDocument(eContent);
//				xml.output(contentout);
//				contentout.flush();
//				contentout.close();
//				
//				byte[] content = contentout.toByteArray();
//				contentout = null;
//				
//				out.write(getCMDLine().getBytes("ASCII"));
//				out.write("\r\n".getBytes("ASCII"));
//				out.write(getHeader(content.length, contentType).getBytes("ASCII"));
//				out.write("\r\n".getBytes("ASCII"));
//				
//				out.flush();
//				out.write(content);
//			} 
//			else {
//				out.write(getCMDLine().getBytes("ASCII"));
//				out.write("\r\n".getBytes("ASCII"));
//				out.write(getHeader(0, contentType).getBytes("ASCII"));
//				out.write("\r\n".getBytes("ASCII"));
//			}
//			
//			out.flush();
		}
	}
	
	public void send(Socket socket) throws Exception {
		if (!socket.isConnected()) {
			throw new RuntimeException("not connected");
		}
		OutputStream out = socket.getOutputStream();
		BufferedOutputStream bout = new BufferedOutputStream(out);
		toOutput(bout);
		bout.flush();	
	}
//	public void setRequest(String request) {
//		this.request = request;
//	}
	public void addHeaderValue(String name, String value) {
		//headers.put(name, value);
		headers.add(new String[] {name, value});
	}
	public void addRequestParam(String name, String value) {
		if(contentElement != null) {
			toggleFormDataMode();
		}
		parameters.put(name, value);
	}
	
	public void setContentElement(Element e) {
		if(parameters.size() != 0) {
			toggleFormDataMode();			
		}
		contentElement = e;
	}
	
	public void toOutputNOT_URL_ENCODED_FOR_TESTING(OutputStream out) throws Exception {
		//write it to the outputstream...
		if(contentType.equalsIgnoreCase("application/x-www-form-urlencoded")) {
			if (contentElement != null) {
				Element eContent = contentElement;
				//signoff if signoffkey present
				if (signoffkey != null) {
					eContent = XMLHelper.cloneElement(contentElement); //HT 23.03.2011 - why cloned?!
					//signoff
					byte[] sha1proof = SecurityHelper.getSHA1LocalProof(eContent);
					eContent.addContent("sha1localproof", SecurityHelper.HexDecoder.encode(sha1proof, ':', -1));
					eContent.addContent(Signature.createSignatureFromLocalProof(sha1proof, "signature of sha1localproof", signoffkey).toElement());
				}
				
				Document xml = Document.buildDocument(eContent);
				
				StringBuffer toSend = new StringBuffer();
				toSend.append(XMLDOCPARAMNAME+"=");
				toSend.append(xml.toString());
				
				Iterator<String> its = parameters.keySet().iterator();
				while(its.hasNext()) {
					toSend.append("&");
					String pn = its.next();
					String pv = parameters.get(pn);
					toSend.append(pn+"="+pv);
				}
				
				byte[] content = toSend.toString().getBytes("ASCII");
				toSend = null;
				
				out.write(getCMDLineNOT_URL_ENCODED_FOR_TESTING().getBytes("ASCII"));
				out.write("\r\n".getBytes("ASCII"));
				out.write(getHeader(content.length, contentType, host).getBytes("ASCII"));
				out.write("\r\n".getBytes("ASCII"));
				
				out.flush();
				out.write(content);
				out.write("\r\n".getBytes("ASCII"));
			}
			else {
				//content-element==null
				out.write(getCMDLineNOT_URL_ENCODED_FOR_TESTING().getBytes("ASCII"));
				out.write("\r\n".getBytes("ASCII"));
				out.write(getHeader(0, contentType, host).getBytes("ASCII"));
				out.write("\r\n".getBytes("ASCII"));
			}
			
			out.flush();
		}
		else if(contentType.equalsIgnoreCase(TYPE_OSDX_ENCRYPTED) && contentData!=null) {
			out.write(getCMDLineNOT_URL_ENCODED_FOR_TESTING().getBytes("ASCII"));
			out.write("\r\n".getBytes("ASCII"));
			for (String[] h : headers) {
				out.write((h[0]+": "+h[1]+"\r\n").getBytes("ASCII"));
			}
			out.write(getHeader(contentData.length, contentType, host).getBytes("ASCII"));
			out.write("\r\n".getBytes("ASCII"));
			out.write(contentData);
			out.flush();
		}
		else if(contentType.equalsIgnoreCase("text/xml")) {
			if(true) {
				throw new RuntimeException("Hmm, thou shalt not use me...");
			}
		}
	}
	
}
