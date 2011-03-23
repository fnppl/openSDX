package org.fnppl.opensdx.keyserver;


import java.util.*;
import java.io.*;
import java.net.*;

import org.fnppl.opensdx.security.*;
import org.fnppl.opensdx.xml.*;

//import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

//http://de.wikipedia.org/wiki/Hypertext_Transfer_Protocol

public class OSDXKeyServerClientRequest {
	public final static String XMLDOCPARAMNAME = "xmldocument";//check out KeyServerRequest

	private final static String version = "HTTP/1.1";
	private String method = "POST";
	
	private String contentType = "text/xml";
	
	private String uri;
	private String host;
//	private String request;
//	private StringBuffer header;
	private Element contentElement;
	private Hashtable<String, String> parameters = new Hashtable<String, String>();
	private Hashtable<String, String> headers = new Hashtable<String, String>();
	protected OSDXKeyObject signoffkey = null;
	
	
	public void setSignoffKey(OSDXKeyObject signoffkey) {
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
	
	public OSDXKeyServerClientRequest() {
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
	private static String getHeader(int contentlength, String contentType) {
		StringBuffer ret = new StringBuffer();
		ret.append("Content-Type: "+contentType+"\r\n");
		ret.append("Content-Length: "+contentlength+"\r\n");
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
				out.write(getHeader(content.length, contentType).getBytes("ASCII"));
				out.write("\r\n".getBytes("ASCII"));
				
				out.flush();
				out.write(content);
			}
			else {
				//content-element==null
				out.write(getCMDLine().getBytes("ASCII"));
				out.write("\r\n".getBytes("ASCII"));
				out.write(getHeader(0, contentType).getBytes("ASCII"));
				out.write("\r\n".getBytes("ASCII"));
			}
			
			out.flush();
		}
		else if(contentType.equalsIgnoreCase("text/xml")) {
			if(true) {
				throw new RuntimeException("Hmm, thou shalt not use me...");
			}
			
			if (contentElement != null) {
				Element eContent = contentElement;
				//signoff if signoffkey present
				if (signoffkey!=null) {
					eContent = XMLHelper.cloneElement(contentElement);//HT 23.03.2011 - why cloned?!
					//signoff
					byte[] sha1proof = SecurityHelper.getSHA1LocalProof(eContent);
					eContent.addContent("sha1localproof", SecurityHelper.HexDecoder.encode(sha1proof, ':', -1));
					eContent.addContent(Signature.createSignatureFromLocalProof(sha1proof, "signature of sha1localproof", signoffkey).toElement());
				}
				
				
				ByteArrayOutputStream contentout = new ByteArrayOutputStream();
				
				Document xml = Document.buildDocument(eContent);
				xml.output(contentout);
				contentout.flush();
				contentout.close();
				
				byte[] content = contentout.toByteArray();
				contentout = null;
				
				out.write(getCMDLine().getBytes("ASCII"));
				out.write("\r\n".getBytes("ASCII"));
				out.write(getHeader(content.length, contentType).getBytes("ASCII"));
				out.write("\r\n".getBytes("ASCII"));
				
				out.flush();
				out.write(content);
			} 
			else {
				out.write(getCMDLine().getBytes("ASCII"));
				out.write("\r\n".getBytes("ASCII"));
				out.write(getHeader(0, contentType).getBytes("ASCII"));
				out.write("\r\n".getBytes("ASCII"));
			}
			
			out.flush();
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
		headers.put(name, value);
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
	
	
	
	/*
	 * 
	 * HT 15.03.2011 here go the possible request-creation-codes - could also be placed in a "factory-class"
	 * 
	 */
	
	public static OSDXKeyServerClientRequest getRequestIdentities(String host, String keyid) {
		OSDXKeyServerClientRequest req = new OSDXKeyServerClientRequest();
		req.setURI(host, "/identities");
		req.toggleGETMode();
		req.addRequestParam("KeyID", keyid);
		
		return req;
	}
	public static OSDXKeyServerClientRequest getRequestKeyStatus(String host, String keyid) {
		OSDXKeyServerClientRequest req = new OSDXKeyServerClientRequest();
		req.setURI(host, "/keystatus");
		req.addRequestParam("KeyID", keyid);
		req.toggleGETMode();
		
		return req;
	}
	public static OSDXKeyServerClientRequest getRequestMasterPubKeys(String host, String idemail) {
		OSDXKeyServerClientRequest req = new OSDXKeyServerClientRequest();
		req.setURI(host, "/masterpubkeys");
		req.toggleGETMode();
		req.addRequestParam("Identity", idemail);		
	
		return req;
	}
	public static OSDXKeyServerClientRequest getRequestSubkeys(String host, String masterkeyid) {
		OSDXKeyServerClientRequest req = new OSDXKeyServerClientRequest();
		req.setURI(host, "/subkeys");
		req.toggleGETMode();
		req.addRequestParam("KeyID", masterkeyid);		
	
		return req;
	}
	
	public static OSDXKeyServerClientRequest getRequestPublicKey(String host, String keyid) {
		OSDXKeyServerClientRequest req = new OSDXKeyServerClientRequest();
		req.setURI(host, "/pubkey");
		req.toggleGETMode();
		req.addRequestParam("KeyID", keyid);		
	
		return req;
	}
//	public static OSDXKeyServerClientRequest getRequestPubKeys(String host, String idemail) {
//		OSDXKeyServerClientRequest req = new OSDXKeyServerClientRequest();
//		req.setURI(host, "/pubkeys");
//		req.toggleGETMode();
//		req.addRequestParam("Identity", idemail);		
//	
//		return req;
//	}
	public static OSDXKeyServerClientRequest getRequestKeyLogs(String host, String keyid) {
		OSDXKeyServerClientRequest req = new OSDXKeyServerClientRequest();
		req.setURI(host, "/keylogs");
		req.toggleGETMode();
		req.addRequestParam("KeyID", keyid);		
	
		return req;
	}
	
	
	public static OSDXKeyServerClientRequest getRequestPutMasterKey(String host, OSDXKeyObject masterkey, Identity id) throws Exception {
		OSDXKeyServerClientRequest req = new OSDXKeyServerClientRequest();
		req.setURI(host, "/masterkey");
		req.togglePOSTMode();
		
		req.addRequestParam("KeyID", masterkey.getKeyID());		
		req.addRequestParam("Identity", id.getEmail());
	
		Element e = new Element("masterpubkey");
		e.addContent(masterkey.getSimplePubKeyElement());
		e.addContent(id.toElement());
		req.setContentElement(e);
		req.setSignoffKey(masterkey); //self-signoff with masterkey
		return req;
	}
	
	
	public static OSDXKeyServerClientRequest getRequestPutRevokeKey(String host, OSDXKeyObject revokekey, OSDXKeyObject relatedMasterKey) throws Exception {
		OSDXKeyServerClientRequest req = new OSDXKeyServerClientRequest();
		req.setURI(host, "/revokekey");
		
		Element e = new Element("revokekey");
		e.addContent("masterkeyid", relatedMasterKey.getKeyModulusSHA1());
		e.addContent(revokekey.getSimplePubKeyElement());
		 //self signoff with revokekey
		byte[] sha1proof = SecurityHelper.getSHA1LocalProof(e);
		e.addContent("sha1localproof", SecurityHelper.HexDecoder.encode(sha1proof, ':', -1));
		e.addContent(Signature.createSignatureFromLocalProof(sha1proof, "signature of sha1localproof", revokekey).toElement());
		req.setContentElement(e);
		req.setSignoffKey(relatedMasterKey); //signoff with masterkeyy
		
		return req;
	}
	
	public static OSDXKeyServerClientRequest getRequestPutSubKey(String host, OSDXKeyObject subkey, OSDXKeyObject relatedMasterKey) throws Exception {
		OSDXKeyServerClientRequest req = new OSDXKeyServerClientRequest();
		req.setURI(host, "/subkey");
		
		Element e = new Element("subkey");
		e.addContent("masterkeyid", relatedMasterKey.getKeyModulusSHA1());
		e.addContent(subkey.getSimplePubKeyElement());
		
		req.setSignoffKey(relatedMasterKey); //Signoff with masterkey
		req.setContentElement(e);
		
		return req;
	}
	
	public static OSDXKeyServerClientRequest getRequestPutKeyLogs(String host, Vector<KeyLog> keylogs, OSDXKeyObject signingKey) throws Exception {
		OSDXKeyServerClientRequest req = new OSDXKeyServerClientRequest();
		req.setURI(host, "/keylogs");
		
		Element e = new Element("keylogs");
		for (KeyLog k : keylogs) {
			e.addContent(k.toElement());
		}
		req.setSignoffKey(signingKey); //Signoff
		req.setContentElement(e);
		
		return req;
	}
}
