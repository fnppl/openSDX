package org.fnppl.opensdx.gui;

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


/**
 * @author Bertram Boedeker <bboedeker@gmx.de>
 * 
 */
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

public class Dialogs {

	public static int CANCEL = -2;
	public static int OK = 0;
	public static int YES = 1;
	public static int NO = -1;
	
	public static File lastDir = new File(System.getProperty("user.home"));
	public static Vector<File> lastDirs = new Vector<File>();
	public static boolean saveLastDir = true;
	
	public static int showYES_NO_Dialog(String title, String message) {
		if (message.contains("\n")) {
			message = "<HTML><BODY>"+message.replace("\n", "<BR>")+"</BODY><HTML>";
		}
		int ans = JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION);
		if (ans == JOptionPane.YES_OPTION) return YES;
		return NO;
	}
	
	public static void showMessage(String message) {
		JOptionPane.showMessageDialog(null, message);
	}
	
	public static String showInputDialog(String title, String message) {
		return JOptionPane.showInputDialog(null, message, title, JOptionPane.PLAIN_MESSAGE);
	}
	
	public static final String showPasswordDialog(String head, String message) {
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		if (message.contains("\n")) {
			message = "<HTML><BODY>"+message.replace("\n", "<BR>")+"</BODY><HTML>";
		}
		JLabel l = new JLabel(message);
		JPasswordField pf = new JPasswordField();
		pf.setEchoChar('*');
		
		p.add(l, BorderLayout.CENTER);
		p.add(pf, BorderLayout.SOUTH);
	    int ans = JOptionPane.showConfirmDialog(null,p,head,JOptionPane.OK_CANCEL_OPTION);
	    //JOptionPane.showMessageDialog(null,p,head,JOptionPane.OK_OPTION);
	    if (ans == JOptionPane.OK_OPTION && pf.getPassword()!=null) {
	    	return new String(pf.getPassword());
	    }
	    return null;
	}
	
	public static final String[] showNewMantraPasswordDialog(String message) {
		if (message.contains("\n")) {
			message = "<HTML><BODY>"+message.replace("\n", "<BR>")+"</BODY><HTML>";
		}
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(new JLabel(message),BorderLayout.NORTH);

		JPanel pMantra = new JPanel();
		pMantra.setBorder(new TitledBorder("mantraname:"));
		JTextField text = new JTextField();
		text.setPreferredSize(new Dimension(250,25));
		pMantra.add(text,BorderLayout.CENTER);
		
		JPanel pPassword = new JPanel();
		pPassword.setBorder(new TitledBorder("passphrase:"));
		
		JPasswordField pf = new JPasswordField();		
		pf.setEchoChar('*');
		pf.setPreferredSize(new Dimension(250,25));
		pPassword.add(pf,BorderLayout.SOUTH);
		
		p.add(pMantra, BorderLayout.CENTER);
		p.add(pPassword, BorderLayout.SOUTH);
	    int ans = JOptionPane.showConfirmDialog(null,p,"NEW PASSWORD",JOptionPane.OK_CANCEL_OPTION);
	    //JOptionPane.showMessageDialog(null,p,head,JOptionPane.OK_OPTION);
	    if (ans == JOptionPane.OK_OPTION && pf.getPassword()!=null) {
	    	return new String[] {text.getText(),new String(pf.getPassword())};
	    }
	    return null;
	}
	
	public static JDialog getWaitDialog(final String message) {
		final String msg;
		if (message.contains("\n")) {
			msg = "<HTML><BODY>"+message.replace("\n", "<BR>")+"</BODY><HTML>";
		} else {
			msg = message;
		}
			
		Object[] options = { "Cancel" } ;
        JOptionPane optionPane = new JOptionPane(msg,JOptionPane.INFORMATION_MESSAGE,JOptionPane.CANCEL_OPTION, null,options, options[0]);
        JDialog d = optionPane.createDialog(null, "Please wait..." );
		return d;
	}
	
	public static int showSelectDialog(String title, String message, Vector values) {
		Object[] os = values.toArray();
		
//		File[] select = new File[values.size()];
//		for (int i=0;i<select.length;i++) {
//			select[i] = values.get(i);
//		}
		Object ans = (Object)JOptionPane.showInputDialog(null,message,title,JOptionPane.QUESTION_MESSAGE, null, os, os[0]);
		if (ans==null) return -1;
		return values.indexOf(ans);
//		
//		if (ans != null) {
//			for (int i=0; i<select.length; i++) {
//				if (select[i].equals(ans)) {
//					return i;
//				}
//			}
//		}
//		return -1;
	}
	
	
	public static File chooseSaveFile(String title, File dir, String selname) {
		return chooseDialog(title,dir,selname,false,true); 
    }
	public static File chooseSaveDirectory(String title, File dir, String selname) {
		return chooseDialog(title,dir,selname,false,false); 
    }
	public static File chooseOpenDirectory(String title, File dir, String selname) {
		return chooseDialog(title,dir,selname,true,false); 
    }
	public static File chooseOpenFile(String title, File dir, String selname) {
		return chooseDialog(title,dir,selname,true,true); 
    }
	public static File chooseOpenFile(String title, File dir, String selname, String[] filter) {
		return chooseDialog(title,dir,selname,true,true, filter); 
    }
	
	public static File[] chooseOpenMultiFile(String title, File dir, String selname, String[] filter) {
		return chooseDialog(title,dir,selname,true,true,true,filter); 
	}
	public static File[] chooseOpenMultiDirs(String title, File dir, String selname, String[] filter) {
		return chooseDialog(title,dir,selname,true,false,true,filter); 
    }
	
	private static File chooseDialog(String title, File dir, String selname ,boolean open, boolean filesonly) {
		File[] result = chooseDialog(title, dir, selname, open, filesonly,false,null); 
		if ((result==null)||(result.length==0)) return null;
		else return result[0];
	}
	private static File chooseDialog(String title, File dir, String selname ,boolean open, boolean filesonly, String[] filter) {
		File[] result = chooseDialog(title, dir, selname, open, filesonly,false,filter); 
		if ((result==null)||(result.length==0)) return null;
		else return result[0];
	}
	
	public static File[] chooseDialog(
			String title, 
			File dir, 
			String selname,
			boolean open, 
			boolean filesonly, 
			boolean multi, 
			String[] filter
		) {
		
		final JFileChooser fd = new JFileChooser(title);
		fd.setDialogTitle(title);
		
		if (lastDirs.size()>0) {			
			final JButton buSel = new JButton("v");
			buSel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
					int sel = Dialogs.showSelectDialog(
							"Select Directory",
							"Last visited directories", 
						lastDirs
					);
					if (sel>=0) {
						fd.setCurrentDirectory(lastDirs.elementAt(sel));
					}
				}
			});	
			fd.setAccessory(buSel);
		}
		fd.setCurrentDirectory(dir);
		if(selname != null && selname.length()>0) {
			fd.setSelectedFile(new File(dir, selname));
		}
		
		if (filesonly) {
			fd.setFileSelectionMode(JFileChooser.FILES_ONLY);
		} else {
			fd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}
		fd.setMultiSelectionEnabled(multi);
		if (filter == null) {
			filter = new String[] {"*.*","*.xml","*.csv"};
		}

		FileFilter[] ff = new FileFilter[filter.length];
		final String[] finalFilter = filter;
		for (int i=0;i<ff.length;i++) {
			final int nr = i;
			final String[] parts = finalFilter[i].split("; ");
			
			for (int j=0;j<parts.length;j++) {
				String regEx = parts[j].replace(".", "\\.");
				regEx = regEx.replace("*", ".ZAPPELDAP");
				regEx = regEx.replace("ZAPPELDAP", "*");
				parts[j] = regEx;
			}
			ff[i] = new FileFilter() {
				public boolean accept(File f) {
					if (f.isDirectory()) return true;
					for (int j=0;j<parts.length;j++) {
						if (f.getName().matches(parts[j])) {
							return true;
						}
					}
					return false;
				}
				public String getDescription() {
					return finalFilter[nr];
				}
			};
			fd.addChoosableFileFilter(ff[i]);
		}
		fd.setFileFilter(ff[0]);
		
		int returnVal = 0;
		if (open) {
			returnVal = fd.showOpenDialog(null);
		} else {
			returnVal = fd.showSaveDialog(null);
		}
		
		File[] result = null;
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	    	if (multi) {
	    		result = fd.getSelectedFiles();
	    	} 
	    	else {
	    		result = new File[1];
	    		result[0] = fd.getSelectedFile();
	    	}
	    	if (saveLastDir) {
		    	if (result[0].isDirectory()) {
		    		lastDir = result[0];
		    	} 
		    	else { 
		    		lastDir = result[0].getParentFile();
		    	}
		    	
		    	int ind = lastDirs.indexOf(lastDir); 
		    	if (ind >= 0) {
		    		lastDirs.remove(ind);
		    	}
		    	lastDirs.add(0,lastDir);
		    	if (lastDirs.size()>15) {
		    		lastDirs.setSize(15);
		    	}
	    	}
	    }		 
		return result;
	}
}
