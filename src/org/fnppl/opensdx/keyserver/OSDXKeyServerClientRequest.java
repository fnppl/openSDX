package org.fnppl.opensdx.keyserver;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

public class OSDXKeyServerClientRequest {
	
	private String request;
	private StringBuffer header;
	private Element contentElement;
	
	
	public OSDXKeyServerClientRequest() {
		request = null;
		header = new StringBuffer();
		contentElement = null;
	}
	
	public void toOutput(OutputStream out) throws Exception {
		//write it to the outputstream...
		if (contentElement!=null) {
			ByteOutputStream contentout = new ByteOutputStream();
			
			Document xml = Document.buildDocument(contentElement);
			xml.output(contentout);
			contentout.flush();
			contentout.close();
			
			byte[] content = contentout.getBytes();
			
			out.write((request+"\r\n").getBytes("ASCII"));
			out.write(header.toString().getBytes("ASCII"));
			
			out.write("Content-Type: text/xml\r\n".getBytes("ASCII"));
			out.write(("Content-Length: "+content.length+"\r\n").getBytes("ASCII"));
			out.write("\r\n".getBytes("ASCII"));
			out.flush();
			out.write(content);
		} else {
			out.write((request+"\r\n").getBytes("ASCII"));
			out.write(header.toString().getBytes("ASCII"));
			out.write("\r\n".getBytes("ASCII"));
		}
		out.flush();
	}
	
	public void send(Socket socket) throws Exception {
		if (!socket.isConnected()) throw new RuntimeException("not connected");
		OutputStream out = socket.getOutputStream();
	//	toOutput(System.out);
		toOutput(out);
		out.flush();	
	}
	public void setRequest(String request) {
		this.request = request;
	}
	
	public void addHeaderValue(String name, String value) {
		header.append(name+": "+value+"\r\n");
	}
	
	public void setContentElement(Element e) {
		contentElement = e;
	}

}
