package org.fnppl.opensdx.file_transfer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.fnppl.opensdx.http.HTTPServer;
import org.fnppl.opensdx.http.HTTPServerRequest;
import org.fnppl.opensdx.http.HTTPServerResponse;

public class OSDXFileTransferLogfileServer extends HTTPServer {
	
	
	private String logfilePath = null;
	private File logPath = null;
	
	public OSDXFileTransferLogfileServer(int port) {
		super();
		this.port = port;
	}
	
	public void startService() throws Exception { 
		logPath = new File(logfilePath);
		logPath.mkdirs();
		System.out.println("logfile path = "+logPath.getAbsolutePath());
		super.startService();
		
	}
	
	public static void main(String[] args) {
		int port = 8899;
		String logpath = "/tmp/logfiles";
		if (args.length==2) {
			port = Integer.parseInt(args[0]);
			logpath = args[1];
		}
		OSDXFileTransferLogfileServer server = new OSDXFileTransferLogfileServer(port);
		server.logfilePath = logpath;
		server.readConfig();
		
		try {
			server.startService();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getServerID() {
		return "osdx filetransfer logfile server v0.1";
	}

	public HTTPServerResponse prepareResponse(HTTPServerRequest request) throws Exception {
		String meth = request.method;
		String cmd = request.cmd;
		if (meth.equalsIgnoreCase("put") && cmd.equals("/logfile")) {
			try {
			byte[] data = request.contentData;
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(logPath,"osdx_filetransfer_"+System.currentTimeMillis()+".log")));
			out.write(data);
			out.flush();
			out.close();
				HTTPServerResponse resp = new HTTPServerResponse(getServerID());
				return resp;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}

	public void readConfig() {
		//TODO
	}

}
