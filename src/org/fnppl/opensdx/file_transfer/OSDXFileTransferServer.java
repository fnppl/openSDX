package org.fnppl.opensdx.file_transfer;

import java.io.Console;
import java.io.File;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Vector;

import org.fnppl.opensdx.file_transfer.helper.ClientSettings;
import org.fnppl.opensdx.file_transfer.helper.FileTransferLog;
import org.fnppl.opensdx.security.MasterKey;
import org.fnppl.opensdx.security.OSDXKey;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;

public class OSDXFileTransferServer {

	private File configFile = new File("osdxserver_config.xml"); 
	private File alterConfigFile = new File("src/org/fnppl/opensdx/file_transfer/resources/osdxfiletransferserver_config.xml");
	
	protected int port = 8899;
	protected InetAddress address = null;
	
	private OSDXKey mySigningKey = null;
	private File clients_config_file = null;
	private boolean backupClientsConfigOnUpdate = true;

	
	//accessable for serverthreads
	private FileTransferLog log = null;
	private int maxByteLength = 4*1024*1024;
	private HashMap<String, ClientSettings> clients = null; //client id := username::keyid
	//private HashMap<OSDXSocketServerThread, FileTransferState> states = null;
	
	
	public OSDXFileTransferServer(String pwS) throws Exception {
		readConfig();
		if (mySigningKey!=null && !mySigningKey.isPrivateKeyUnlocked()) {
			mySigningKey.unlockPrivateKey(pwS);
		}
		if (mySigningKey==null || !mySigningKey.hasPrivateKey() || !mySigningKey.isPrivateKeyUnlocked()) {
			throw new Exception("signing key not accessable");
		}
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
			Element ks = root.getChild("osdxfiletransferserver");
			//			host = ks.getChildText("host");
			port = ks.getChildInt("port");
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

			String logFile = ks.getChildText("logfile");
			if (logFile==null) {
				log = FileTransferLog.initNoLogging();
			} else {
				log = FileTransferLog.initLog(new File(logFile));
			}

			String extraClients = ks.getChildText("clients_config_file");
			if (extraClients==null || extraClients.length()==0) {
				clients_config_file = null;
			} else {
				clients_config_file = new File(extraClients);
				new File(clients_config_file.getAbsolutePath()).getParentFile().mkdirs();
				Element ecf = ks.getChild("clients_config_file");
				String doBackup = ecf.getAttribute("backup");
				if (doBackup!=null) {
					try {
						backupClientsConfigOnUpdate = Boolean.parseBoolean(doBackup);
					} catch (Exception ex) {
						backupClientsConfigOnUpdate = true;
						ex.printStackTrace();
					}
				}
			}

			///Clients
			clients = new HashMap<String, ClientSettings>();
			//System.out.println("init clients");
			Element eClients = root.getChild("clients");
			Vector<Element> ecClients = eClients.getChildren("client");
			for (Element e : ecClients) {
				try {
					ClientSettings cs = ClientSettings.fromElement(e);
					clients.put(cs.getSettingsID(),cs);
					System.out.println("adding client: "+cs.getSettingsID()+" -> "+cs.getLocalRootPath().getAbsolutePath());

					cs.getLocalRootPath().mkdirs();

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			//clients from clients config file
			if (clients_config_file!=null && clients_config_file.exists()) {
				try {
					eClients = Document.fromFile(clients_config_file).getRootElement();
					ecClients = eClients.getChildren("client");
					for (Element e : ecClients) {
						try {
							ClientSettings cs = ClientSettings.fromElement(e);
							clients.put(cs.getSettingsID(),cs);
							System.out.println("adding extra client: "+cs.getSettingsID()+" -> "+cs.getLocalRootPath().getAbsolutePath());							
							cs.getLocalRootPath().mkdirs();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				} catch (Exception ex2) {
					ex2.printStackTrace();
				}
			}

			//SigningKey
			try {
				OSDXKey k = OSDXKey.fromElement(root.getChild("rootsigningkey").getChild("keypair"));
				if (k instanceof MasterKey) {
					mySigningKey = (MasterKey)k;					
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
	
	public ClientSettings getClientSetting(String userid) {
		return clients.get(userid);
	}
	
	public void startService() throws Exception {
		ServerSocket so = new ServerSocket(port);
		while (true) {
			try {
				final Socket socket = so.accept();
				OSDXFileTransferServerThread t = new OSDXFileTransferServerThread(socket, mySigningKey,this);
				t.start();	
			} catch (Exception ex) {
				ex.printStackTrace();
				Thread.sleep(250);// cooldown...
			}
		}
	}
	
	public static void main(String args[]) {
		try {
			if (args!=null && args.length==1 && args[0].equals("--makeconfig")) {
				//makeConfig();
				System.out.println("makeconfig not implemented");
				return;
			}

			String pwS = null;
			if(args.length > 0 ) {
				pwS = args[0];
			}
			else {
				Console console = System.console();
				pwS = console.readLine("Please enter password for unlocking private-key: ");
			}
			OSDXFileTransferServer server = new OSDXFileTransferServer(pwS);
			server.startService();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
