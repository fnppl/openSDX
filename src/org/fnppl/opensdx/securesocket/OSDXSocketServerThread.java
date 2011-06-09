package org.fnppl.opensdx.securesocket;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;

import org.fnppl.opensdx.security.OSDXKey;
import org.fnppl.opensdx.security.OSDXMessage;
import org.fnppl.opensdx.security.Result;
import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.security.SymmetricKey;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;

public class OSDXSocketServerThread extends Thread {

	public final static String ERROR_NO_RESPONSE = "ERROR: server does not respond.";
	public final static String ERROR_WRONG_RESPONE_FORMAT = "ERROR: Wrong format in uploadserver's response.";
	
	private Socket socket;
	private OSDXKey mySigningKey = null;
	private OSDXKey myEncryptionKey = null;
	private SymmetricKey agreedEncryptionKey = null;
	private String message = null;
	private boolean secure_connection_established = false;
	
	private String lastReceivedText = null;
	private byte[] lastReceivedData = null;
	private boolean lastReceivedWasEncrypted = false;
	
	
	public OSDXSocketServerThread(Socket socket, OSDXKey mySigningKey, OSDXKey myEncryptionKey) {
		this.socket = socket;
		this.mySigningKey = mySigningKey;
		this.myEncryptionKey = myEncryptionKey;
	}
	
	public void run() {
		try {
			InetAddress addr = socket.getInetAddress();
			String remoteIP = addr.getHostAddress();
			int remotePort = socket.getPort();
			
			sendHello();
			secure_connection_established = initSymEncKey();
			
			if (secure_connection_established) {
				sendEncryptedText("Secure Connection Established!");
			}
			
			while (secure_connection_established) {
				processNextCommand();
			}
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void processNextCommand() {
		try {
			receiveData();
			if (lastReceivedText!=null) {
				if (lastReceivedWasEncrypted) {
					System.out.println("received encrypted text: "+lastReceivedText);
				} else {
					System.out.println("received plain text: "+lastReceivedText);
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	private void sendHello() {
		try {
			Element e = new Element("hello_client");
			e.addContent("message","please choose a symmetric key and send it encrypted with my following public key");
			e.addContent(myEncryptionKey.getSimplePubKeyElement());
			OSDXMessage msg = OSDXMessage.buildMessage(e, mySigningKey);
			sendPlainText(Document.buildDocument(msg.toElement()).toString());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	private boolean initSymEncKey() {
		try {
			receiveData();
			if (lastReceivedText!=null) {
				//get serves encryption key out of message
				OSDXMessage msg = OSDXMessage.fromElement(Document.fromString(lastReceivedText).getRootElement());
				//TODO check with Key
				Result ok = msg.verifySignaturesWithoutKeyVerification();
				if (!ok.succeeded) {
					message = ok.errorMessage;
					return false;
				}
				
				Element responseElement = msg.getDecryptedContent(myEncryptionKey);
				//Document.buildDocument(responseElement).output(System.out);
				if (responseElement==null || !responseElement.getName().equals("session_encryption_key")) {
					message = ERROR_WRONG_RESPONE_FORMAT;
					return false;
				}
				
				byte[] iv = SecurityHelper.HexDecoder.decode(responseElement.getChildText("init_vector"));
				byte[] key_bytes = SecurityHelper.HexDecoder.decode(responseElement.getChildText("key_bytes"));
				agreedEncryptionKey = new SymmetricKey(key_bytes, iv);
				return true;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return false;
	}
	
	private boolean sendBytes(byte[] data, String type) {
		try {
			OutputStream out = socket.getOutputStream();
			out.write((type+data.length+":").getBytes("UTF-8"));
			out.write(data);
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			message = ex.getMessage();
		}
		return false;
	}
	
	public boolean sendEncryptedData(byte[] data) {
		try {
			byte[] encData = agreedEncryptionKey.encrypt(data);
			sendBytes(encData,"ENCDATA");
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			message = ex.getMessage();
		}
		return false;
	}
	
	public boolean sendPlainData(byte[] data) {
		return sendBytes(data,"DATA");
	}
	
	public boolean sendEncryptedText(String text) {
		try {
			byte[] encText = agreedEncryptionKey.encrypt(text.getBytes("UTF-8"));
			return sendBytes(encText,"ENCTEXT");
		} catch (Exception e) {
			e.printStackTrace();
			message = e.getMessage();
			return false;
		}
	}
	
	public boolean sendPlainText(String text) {
		try {
			return sendBytes(text.getBytes("UTF-8"),"TEXT");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public void receiveData() {
		try {
			lastReceivedData = null;
			lastReceivedText = null;
			lastReceivedWasEncrypted = false;
			
			InputStream s = socket.getInputStream();
			
			String command = "";
			boolean commandNotComplete = true;
			byte[] b = new byte[1];
			int read;
			while (commandNotComplete && (read = s.read(b))!=-1) {
				if (b[0] == 58) { // data starts after :
					commandNotComplete = false;
				} else {
					command += (char)b[0];
				}
			}
			
			int byteCount = 0;
			if (command.startsWith("TEXT") || command.startsWith("DATA"))  {
				byteCount = Integer.parseInt(command.substring(4));
			}
			else if (command.startsWith("ENCTEXT")||command.startsWith("ENCDATA"))  {
				byteCount = Integer.parseInt(command.substring(7));
				lastReceivedWasEncrypted = true;
			}
			
			if (byteCount>0) {
				//read bytes
				byte[] data = new byte[byteCount];
				s.read(data);
				
				//decrypt if necessary
				if (lastReceivedWasEncrypted) {
					try {
						data = agreedEncryptionKey.decrypt(data);
						System.out.println("  --> decryption ok");
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				if (command.contains("TEXT")) {
					lastReceivedText = new String(data, "UTF-8");
					System.out.println("RECEIVED MESSAGE::"+lastReceivedText);
				} else {
					lastReceivedData = data;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
