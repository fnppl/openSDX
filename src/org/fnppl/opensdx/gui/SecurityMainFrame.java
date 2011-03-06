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

import java.awt.*;
import java.io.*;
import java.util.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.*;

import org.fnppl.opensdx.security.KeyApprovingStore;


public class SecurityMainFrame extends JFrame {
	private KeyApprovingStore currentKeyStore = null;
	private File lastDir = new File(System.getProperty("user.home"));
	
	private static SecurityMainFrame instance = null;
	public static SecurityMainFrame getInstance() {
		if(instance == null) {
			instance = new SecurityMainFrame();
		}
		return instance;
	}
	private SecurityMainFrame() {
		super("fnppl.org :: openSDX :: SecurityMainFrame");		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setSize(1024, 768);
	}
	
	public boolean openDefauktKeyStore() {
		File f = new File(System.getProperty("user.home"));
		f = new File(f, "openSDX");
		if(!f.exists()) {
			f.mkdirs();
		}
		
		f = new File(f, "defaultKeyStore.xml");
		
		return openKeyStore(f);
	}
	public boolean openKeyStore(File f) {
		try {
			if(f.exists()) {
				KeyApprovingStore kas = KeyApprovingStore.fromFile(f);
				this.currentKeyStore = kas;
				return true;
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	public void makeMenuBar() {
		ActionListener ja = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String cmd = e.getActionCommand();
				
				if(cmd.equalsIgnoreCase("quit")) {
					System.exit(0);
				}
				else if(cmd.equalsIgnoreCase("createnewkeystore")) {
					JOptionPane.showMessageDialog(instance, "createnewkeystore :: popup");
				}
				else if(cmd.equalsIgnoreCase("openkeystore")) {
					JOptionPane.showMessageDialog(instance, "openkeystore :: popup");
				}
				else if(cmd.equalsIgnoreCase("writekeystore")) {
					JOptionPane.showMessageDialog(instance, "writekeystore :: popup");
				}
				else if(cmd.equalsIgnoreCase("generatekeypair")) {
					JOptionPane.showMessageDialog(instance, "generatekeystore :: popup");
				}
				else if(cmd.equalsIgnoreCase("encryptfile")) {
					JOptionPane.showMessageDialog(instance, "encryptfile :: popup");
				}
				else if(cmd.equalsIgnoreCase("decryptfile")) {
					JOptionPane.showMessageDialog(instance, "decryptfile :: popup");
				}
				else if(cmd.equalsIgnoreCase("signfile")) {
					JOptionPane.showMessageDialog(instance, "signfile :: popup");
				}
				else if(cmd.equalsIgnoreCase("verifysignature")) {
					JOptionPane.showMessageDialog(instance, "verifysignature :: popup");
				}
			}
		};
		
		JMenuBar jb = new JMenuBar();
		JMenu jm = new JMenu("File");
		jb.add(jm);
		JMenuItem jmi = null;
		
		jmi = new JMenuItem("CreateNewKeyStore");
		jmi.setActionCommand("createnewkeystore");
		jmi.addActionListener(ja);
		jm.add(jmi);
		
		jmi = new JMenuItem("OpenKeyStore");
		jmi.setActionCommand("openkeystore");
		jmi.addActionListener(ja);
		jm.add(jmi);
		
		jmi = new JMenuItem("CloseKeyStore");
		jmi.setActionCommand("writekeystore");
		jmi.addActionListener(ja);
		jm.add(jmi);
		
		jmi = new JMenuItem("Quit");
		jmi.setActionCommand("quit");
		jmi.addActionListener(ja);
		jm.add(jmi);
		
		
		jm = new JMenu("Keys");
		jb.add(jm);
		
		jmi = new JMenuItem("GenerateKeyPair");
		jmi.setActionCommand("generatekeypair");
		jmi.addActionListener(ja);
		jm.add(jmi);
		
		jm = new JMenu("Signature");
		jb.add(jm);
		
		jmi = new JMenuItem("VerifySignature");
		jmi.setActionCommand("verifysignature");
		jmi.addActionListener(ja);
		jm.add(jmi);
		
		jmi = new JMenuItem("SignFile");
		jmi.setActionCommand("signfile");
		jmi.addActionListener(ja);
		jm.add(jmi);
		
		jm = new JMenu("<html>Encryption<br>Decryption</html>");
		jb.add(jm);
		
		jmi = new JMenuItem("EncryptFile");
		jmi.setActionCommand("encryptfile");
		jmi.addActionListener(ja);
		jm.add(jmi);
		
		jmi = new JMenuItem("DecryptFile");
		jmi.setActionCommand("decryptfile");
		jmi.addActionListener(ja);
		jm.add(jmi);
		
		setJMenuBar(jb);
	}
	private void buildUi() {
		makeMenuBar();
		
		JPanel jp = new JPanel();
		setContentPane(jp);
		GridBagLayout gb = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();		
		jp.setLayout(gb);
	}

	private static final MouseListener consumeMouseListener 
	= new MouseAdapter(){
		@Override
		public void mousePressed(MouseEvent e){ e.consume(); }
		@Override
		public void mouseReleased(MouseEvent e){ e.consume(); }
		@Override
		public void mouseClicked(MouseEvent e){ e.consume(); }
	};

	public void lockUI(){
		JRootPane r = getRootPane();
		Component l = r.getGlassPane();

		l.addMouseListener(consumeMouseListener);
		l.setVisible(true);
		l.setEnabled(true);

		l.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		//		MainFrame.getInstance().setEnabled(false);    	
	}
	public void releaseUILock() {
		JRootPane r = getRootPane();
		Component l = r.getGlassPane();

		l.removeMouseListener(consumeMouseListener);
		l.setVisible(false);
		l.setEnabled(false);

		//		MainFrame.getInstance().setEnabled(true);
		l.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	
	public static void main(String[] args) {
		//HT 28.02.2011
//		1. Select/Open Keystore 
//		2. Save Keystore 
//		3. Create Key(s) 
//		4. Create Identities 
//		5. Modify Identities
//		6. Add arbitrary (foreign) keys (pubkeys) to keystore
//		7. Sign arbitrary files
//		8. Check signature of arbitrary files
//		10. encrypt arbitrary files (AES)
//		11. decrypt arbitrary files (AES)
//		12. Modify Keys (in terms of deletion/revokation/submission to server)
		
		SecurityMainFrame s = SecurityMainFrame.getInstance();
		s.buildUi();
		s.openDefauktKeyStore();
		s.setVisible(true);
	}
}

