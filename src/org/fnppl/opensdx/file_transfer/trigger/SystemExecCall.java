package org.fnppl.opensdx.file_transfer.trigger;
/*
 * Copyright (C) 2010-2015 
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;
import java.util.Map.Entry;

import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.xml.Element;

public class SystemExecCall implements FunctionCall {

	private String[] cmdArray = null;
	private String[] envp = null; 		//TODO implement if needed
	private File dir = null;
		
	private boolean sysout = true;
	private File outputFile = null;
	private boolean appendToOutputFile = true;
	private HashMap<Integer,String> placeHolder = new HashMap<Integer, String>();
	
//	<system_exec_call>
//		<command>ls</command> <!-- direct command (does not resolve wildcards), arguments as param -->
//		<param>-All</param>
//		<pwd>~/workspace</pwd>	
//		<sysout>true</sysout> <!-- true (default if not output_file) / false -->
//		<output_file append="false">/tmp/osdx_outputs.txt</output_file> <!-- optional output to file, default: append = true -->
//	</system_exec_call>
//
//	<system_exec_call>
//		<bash>ls -All *.xml</bash> <!-- run with bash (resolves wildcards) -->
//		<output_file append="false">/tmp/osdx_bash_outputs.txt</output_file> <!-- default: append = true -->
//	</system_exec_call>
	
	private SystemExecCall() {
		
	}
	
	public static SystemExecCall fromElemet(Element e) {
		SystemExecCall c = new SystemExecCall();
		String bash = e.getChildText("bash");
		if (bash!=null) {
			//bash command
			c.cmdArray = new String[] {"/bin/bash","-c",bash};
		} else {
			//direct command
			Vector<Element> params = e.getChildren("param");
			int pCount = 0;
			if (params!=null) {
				pCount = params.size();
			}
			c.cmdArray = new String[pCount+1];
			
			
			c.cmdArray[0] = e.getChildText("command"); 
			for (int i=0;i<pCount;i++) {
				Element ep = params.get(i);
				String pUse = ep.getAttribute("use");
				if (pUse!=null) {
					c.placeHolder.put(i+1, pUse);
					c.cmdArray[i+1] = "${"+pUse+"}";
				}
				else {
					c.cmdArray[i+1] = ep.getText();
				}
			}
		}
		
		//pwd
		String pwd = e.getChildText("pwd");
		if (pwd!=null) {
			if (pwd.startsWith("~/")) {
				pwd = System.getProperty("user.home")+pwd.substring(1);
			}
			c.dir = new File(pwd);
			try {
				System.out.println("pwd: "+c.dir.getCanonicalPath());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			if (!c.dir.exists() || !c.dir.isDirectory()) {
				c.dir = null;
			}
		}	
		
		//output
		String ssysout = e.getChildText("sysout");
		
		Element eOutfile = e.getChild("output_file");
		if (eOutfile!=null) {
			c.outputFile = new File(eOutfile.getText());
			try {
				System.out.println(c.outputFile.getCanonicalPath());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			String app = eOutfile.getAttribute("append");
			if (app!=null) {
				c.appendToOutputFile = Boolean.parseBoolean(app);
			}
			if (ssysout==null) {
				c.sysout = false;
			}
			else {
				c.sysout = Boolean.parseBoolean(ssysout);
			}
		} else {
			if (ssysout!=null) {
				c.sysout = Boolean.parseBoolean(ssysout);
			}
		}
		return c;
	}
	
	public void run(boolean async, HashMap<String, Object> context) {
		//TODO replace cmdArray with context values
		String[] myCmdArray = prepareCommands(context);
		if (myCmdArray!=null) {
			makeSystemExecCall(myCmdArray, envp, dir, async, sysout, outputFile, appendToOutputFile);
		}
	}
	
	private String[] prepareCommands(HashMap<String, Object> context) {
		if (placeHolder.isEmpty()) return cmdArray;
		
		String[] myCmdArray = new String[cmdArray.length];
		for (int i=0;i<cmdArray.length;i++) {
			myCmdArray[i] = cmdArray[i];
		}
		
		for (Entry<Integer, String> e : placeHolder.entrySet()) {
			int i = e.getKey().intValue();
			String contextKey = e.getValue();
			Object value = context.get(contextKey);
			if (value==null) {
				System.out.println("ERROR in API CALL :: MISSING CONTEXT VARIABLE: "+contextKey);
				return null;
			}
			if (value instanceof File) {
				myCmdArray[i] = ((File)value).getAbsolutePath();
			}
			else if (value instanceof Long) {
				myCmdArray[i] = SecurityHelper.getFormattedDate(((Long)value).longValue());
			}
			else {
				myCmdArray[i] = value.toString();
			}
		}
		return myCmdArray;
	}
	
	public void makeSystemExecCall(String[] cmdArray, String[] envp, File dir, boolean async, final boolean sysout, final File outputFile, final boolean appendToOutputFile) {
        try {
            final Process p = Runtime.getRuntime().exec(cmdArray, envp, dir);
            if (outputFile!=null || sysout) {
	            Thread t = new Thread() {
	            	public void run() {
	            		BufferedReader read = new BufferedReader(new InputStreamReader(p.getInputStream()));
	            		FileWriter writer = null;
	            		try {
		            		if (outputFile!=null) {
		            			writer = new FileWriter(outputFile, appendToOutputFile);
		            		}
	            		} catch (Exception ex) {
	            			System.out.println("Error opening file: "+outputFile.getAbsolutePath()+" for writing");
	            		}
	            		String line = null;
	            		try {
							while ((line = read.readLine())!=null) {
								if (sysout) {
									System.out.println("> "+line);
								}
								try {
				            		if (writer!=null) {
				            			writer.append(line);
				            			writer.append("\n");
				            		}
			            		} catch (Exception ex) {
			            			System.out.println("Error writing to file: "+outputFile.getAbsolutePath());
			            		}
							}
						} catch (IOException ex) {
							ex.printStackTrace();
						}
						try {
		            		if (writer!=null) {
		            			writer.close();
		            		}
	            		} catch (Exception ex) {
	            			System.out.println("Error closing file: "+outputFile.getAbsolutePath());
	            		}
	            	}
	            };
	            t.start();
            }
            if (!async) {
            	int r = p.waitFor();
            }
        } catch(Exception ex) {
        	ex.printStackTrace();
        }
	}
	
	public String toString() {
		return "pwd: "+dir+"\nSystem exec call: "+Arrays.toString(cmdArray);
	}
	
}
