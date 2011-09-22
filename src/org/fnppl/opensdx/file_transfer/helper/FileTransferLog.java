package org.fnppl.opensdx.file_transfer.helper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/*
 * Copyright (C) 2010-2011 
 * 							fine people e.V. <opensdx@fnppl.org> 
 * 							Henning Thieß <ht@fnppl.org>
 * 
 * 							http://fnppl.org
 */

/*
 * Software license
 *
 * As far as this file or parts of this file is/are software, rather than documentation, this software-license applies / shall be applied.
 *  
 * This file is part of openSDX
 * openSDX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * openSDX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * and GNU General Public License along with openSDX.
 * If not, see <http://www.gnu.org/licenses/>.
 *      
 */

/*
 * Documentation license
 * 
 * As far as this file or parts of this file is/are documentation, rather than software, this documentation-license applies / shall be applied.
 * 
 * This file is part of openSDX.
 * Permission is granted to copy, distribute and/or modify this document 
 * under the terms of the GNU Free Documentation License, Version 1.3 
 * or any later version published by the Free Software Foundation; 
 * with no Invariant Sections, no Front-Cover Texts, and no Back-Cover Texts. 
 * A copy of the license is included in the section entitled "GNU 
 * Free Documentation License" resp. in the file called "FDL.txt".
 * 
 */
public class FileTransferLog {

	private static String dateformat = "yyyy-MM-dd HH:mm:ss.SSS";
	private static Locale ml = new Locale("en", "DE");
	private final static SimpleDateFormat dateme = new SimpleDateFormat(dateformat, ml);
	
	
	public static String getDateString() {
		return dateme.format(System.currentTimeMillis());
	}
	
	public static FileTransferLog initTmpLog() {
		return new FileTransferLog();
	}
	
	public static FileTransferLog initLog(File logfile) {
		logfile.getParentFile().mkdirs();
		return new FileTransferLog(logfile);
	}
	
	public static FileTransferLog initNoLogging() {
		return new FileTransferLog(null);
	}
	
	private File logfile = null;
	
	private FileTransferLog() {
		String tmpdir = System.getProperty("java.io.tmpdir");
		if (tmpdir!=null && tmpdir.length()>0) {
			File tmppath = new File(tmpdir);
			if (tmppath.exists()) {
				logfile = new File(tmppath,"osdxfiletransferserver_log.txt");
				System.out.println("logging to: "+logfile.getAbsolutePath());
			}
		}
	}
	private FileTransferLog(File log) {
		logfile = log;
		if (log!=null) {
			System.out.println("logging to: "+logfile.getAbsolutePath());
		} else {
			System.out.println("logging disabled.");
		}
	}
	
	private String getTimestamp() {
		return dateme.format(System.currentTimeMillis());
	}
	
	private void appendToLogfile(String[] txt) {
		if (logfile!=null && txt!=null && txt.length>0) {
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(logfile, true));
				out.write("\"");
				for (int i=0;i<txt.length;i++) {
					if (txt[i]!=null) {
						out.write(makeEscapeChars(txt[i]));
					}
					if (i<txt.length-1) {
						out.write("\",\"");
					} else {
						out.write("\"\n");
					}
				}
				out.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			} 
		}
	}
	
	public static String makeEscapeChars(String s) {
		String r = "";
		for (char c : s.toCharArray()) {
			if (c == '\\' || c == '\"') {
				r += '\\';
			}
			r += c;
		}
		return r;
	}
	
	public static String resolveEscapeChars(String s) {
		String r = "";
		char[] chars = s.toCharArray();
		for (int pos = 0; pos < chars.length; pos++) {
			if (chars[pos] == '\\') {
				pos++;
				if (pos<chars.length) {
					r += chars[pos];	
				}
			} else {
				r += chars[pos];
			}
		}
		return r;
	}
	
	
	// make the following methods abstract if other FileTransferLoggers are needed 
	
	public void logServerStart(String addr, int port) {
		appendToLogfile(new String[]{getTimestamp(),"","STARTING SERVER",addr,""+port});
	}
	
	public void logIncomingConnection(String id, String addr, String msg) {
		appendToLogfile(new String[]{getTimestamp(),addr,"INCOMING CONNECT",id,msg});
	}
	
	public void logConnectionClose(String id, String addr, String msg) {
		appendToLogfile(new String[]{getTimestamp(),addr,"CLOSE CONNECT",id,msg});
	}
	
	public void logCommand(String id, String addr, String command, String param, String response) {
		appendToLogfile(new String[]{getTimestamp(),addr,"COMMAND",id,command,param,response});
	}
	
	public void logError(String id, String addr, String msg) {
		appendToLogfile(new String[]{getTimestamp(),addr,"ERROR",id,msg});
	}
	
	public void logFiledataUpload(String id, String addr, String filename, long startPos, int length) {
		appendToLogfile(new String[]{getTimestamp(),addr,"FILEDATA UPLOAD",id,filename,""+startPos,""+length});
	}
}
