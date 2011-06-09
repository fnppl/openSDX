package org.fnppl.opensdx.securesocket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Vector;
import java.util.Map.Entry;

import org.fnppl.opensdx.http.HTTPServerRequest;
import org.fnppl.opensdx.http.HTTPServerResponse;
import org.fnppl.opensdx.security.MasterKey;
import org.fnppl.opensdx.security.OSDXKey;
import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.uploadserver.UploadServer;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;

public class OSDXSocketServer {

	protected int port = -1;
	protected String prepath = "";
	private String serverid = "serverid";
	
	protected InetAddress address = null;

	private OSDXKey mySigningKey = null;
	private OSDXKey myEncryptionKey = null;
	
	private File configFile = new File("osdxserver_config.xml"); 
	private File alterConfigFile = new File("src/org/fnppl/opensdx/securesocket/resources/config.xml"); 
	

	public OSDXSocketServer(String pwSigning, String pwEncryption) throws Exception {
		readConfig();
		mySigningKey.unlockPrivateKey(pwSigning);
		myEncryptionKey.unlockPrivateKey(pwEncryption);
	}
	
	public void readConfig() {
		try {
			if (!configFile.exists()) {
				configFile = alterConfigFile;
			}
			if (!configFile.exists()) {
				System.out.println("Sorry, uploadserver_config.xml not found.");
				System.exit(0);
			}
			Element root = Document.fromFile(configFile).getRootElement();
			
			//uploadserver base
			Element ks = root.getChild("osdxserver");
//			host = ks.getChildText("host");
			port = ks.getChildInt("port");
			prepath = ks.getChildTextNN("prepath");
			
			String ip4 = ks.getChildText("ipv4");
			try {
				byte[] addr = new byte[4];
				String[] sa = ip4.split("[.]");
				for (int i=0;i<4;i++) {
					int b = Integer.parseInt(sa[i]);
					if (b>127) b = -256+b;
					addr[i] = (byte)b;
				}
				address = InetAddress.getByAddress(addr);
			} catch (Exception ex) {
				System.out.println("CAUTION: error while parsing ip adress");
				ex.printStackTrace();
			}
//			pathUploadedFiles = new File(ks.getChildText("path_uploaded_files"));
//			System.out.println("path for uploaded files: "+pathUploadedFiles.getAbsolutePath());
			
			//SigningKey
			try {
				OSDXKey k = OSDXKey.fromElement(root.getChild("rootsigningkey").getChild("keypair"));
				if (k instanceof MasterKey) {
					mySigningKey = (MasterKey)k;
					myEncryptionKey = (MasterKey)k; //TODO
				} else {
					System.out.println("ERROR: no master signing key in config.");	
				}
			} catch (Exception e) {
				System.out.println("ERROR: no master signing key in config."); 
			}
			//TODO check localproofs and signatures 

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void startService() throws Exception {
		System.out.println("Starting Server at "+address.getHostAddress()+" on port " + port +"  at "+SecurityHelper.getFormattedDate(System.currentTimeMillis()));
		ServerSocket so = new ServerSocket(port);
		while (true) {
			try {
				final Socket me = so.accept();
				OSDXSocketServerThread t = new OSDXSocketServerThread(me, mySigningKey, myEncryptionKey);
				
				t.start();
			} catch (Exception ex) {
				ex.printStackTrace();
				Thread.sleep(250);// cooldown...
			}
		}
	}
	
	private static void makeConfig() {
//		Console console = System.console();
//	    if (console == null) {
//	      return;
//	    }
//	    String host = console.readLine("host: ");
//	    String port = console.readLine("port: ");
//	    String prepath = console.readLine("prepath: ");
//	    String ipv4 = console.readLine("ipv4: ");
//	    String ipv6 = console.readLine("ipv6: ");
//	    String mail_user = console.readLine("mail user: ");
//	    String mail_sender = console.readLine("mail sender: ");
//	    String mail_smtp_host = console.readLine("mail smtp host: ");
//	    String id_email = console.readLine("id email: ");
//	    String id_mnemonic = console.readLine("id mnemonic: ");
//	    String pass = console.readLine("key password: ");
//	    
//	    Element root = new Element("opensdxkeyserver");
//	    Element eKeyServer = new Element("keyserver");
//	    eKeyServer.addContent("port", port);
//	    eKeyServer.addContent("prepath",prepath);
//	    eKeyServer.addContent("ipv4",ipv4);
//	    eKeyServer.addContent("ipv6",ipv6);
//	    Element eMail = new Element("mail");
//	    eMail.addContent("user", mail_user);
//	    eMail.addContent("sender", mail_sender);
//	    eMail.addContent("smtp_host", mail_smtp_host);
//	    eKeyServer.addContent(eMail);
//	    root.addContent(eKeyServer);
//	    
//	    try {
//	    	Element eSig = new Element("rootsigningkey");
//	    	MasterKey key = MasterKey.buildNewMasterKeyfromKeyPair(AsymmetricKeyPair.generateAsymmetricKeyPair());
//			Identity id = Identity.newEmptyIdentity();
//			id.setIdentNum(1);
//			id.setEmail(id_email);
//			id.setMnemonic(id_mnemonic);
//			key.addIdentity(id);
//			key.setAuthoritativeKeyServer(host);
//			key.createLockedPrivateKey("", pass);
//			eSig.addContent(key.toElement(null));
//			root.addContent(eSig);
//			Document.buildDocument(root).writeToFile(new File("keyserver_config.xml"));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	    
	}
	
	public static void main(String[] args) throws Exception {
		if (args!=null && args.length==1 && args[0].equals("--makeconfig")) {
			makeConfig();
			return;
		}
		
		//debug
		String pwS = "upload";
		String pwE = "upload";
		
		OSDXSocketServer s = new OSDXSocketServer(pwS,pwE);
		s.startService();
	}
	
}

