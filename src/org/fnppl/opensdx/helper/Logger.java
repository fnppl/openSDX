package org.fnppl.opensdx.helper;

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

public class Logger {

	private static Logger instanceFileTransfer = null;
	private static Logger instanceNoLogging = null;
	
	private static String dateformat = "yyyy-MM-dd HH:mm:ss.SSS";
	private static Locale ml = new Locale("en", "DE");
	private final static SimpleDateFormat dateme = new SimpleDateFormat(dateformat, ml);
	private File logfile = null;
	
	private Logger(File file) {
		logfile = file;
	}
	
	public static Logger getFileTransferLogger() {
		if (instanceFileTransfer==null) {
			try {
				String tmpdir = System.getProperty("java.io.tmpdir");
				if (tmpdir!=null && tmpdir.length()>0) {
					File tmppath = new File(tmpdir);
					if (tmppath.exists()) {
						File logfile_old = new File(tmppath,"osdx_filetransfer_old.log");
						if (logfile_old.exists()) {
							logfile_old.delete();
						}
						File logfile = new File(tmppath,"osdx_filetransfer.log");
						if (logfile.exists()) {
							logfile.renameTo(logfile_old);
						}
						logfile = new File(tmppath,"osdx_filetransfer.log");
						instanceFileTransfer = new Logger(logfile);		
						System.out.println("logging to: "+logfile.getAbsolutePath());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return instanceFileTransfer;
	}
	
	public File getLogFile() {
		return logfile;
	}
	
	public static Logger getNoLogging() {
		if (instanceNoLogging==null) {
			instanceNoLogging = new Logger(null);
		}
		return instanceNoLogging;
	}
	
	public void logMsg(String msg) {
		if (logfile!=null) {
			appendToLogfile(msg,"MSG");
		}
	}
	
	public void logError(String msg) {
		if (logfile!=null) {
			appendToLogfile(msg,"ERR");
		}
	}
	
	public void logException(Exception ex) {
		if (logfile!=null) {
			StringBuffer b = new StringBuffer();
			b.append(ex.toString());
			b.append("\n");
			StackTraceElement[] trace = ex.getStackTrace();
			for (StackTraceElement e : trace) {
				b.append("   ");
				b.append(e.toString());
				b.append("\n");
			}
			appendToLogfile(b.toString(),"EXP");
		}
	}
	
	private void appendToLogfile(String msg, String type) {
		if (msg!=null && msg.length()>0) {
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(logfile, true));
				out.write(dateme.format(System.currentTimeMillis()));
				out.write(" ");
				out.write(type);
				out.write(" ");
				out.write(msg);
				out.write("\n");
				out.flush();
				out.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			} 
		}
	}
	
}
