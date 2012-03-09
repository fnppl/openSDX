package org.fnppl.opensdx.file_transfer.gui;

/*
 * Copyright (C) 2010-2012 
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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.fnppl.opensdx.dmi.FeedGui;
import org.fnppl.opensdx.file_transfer.model.FileTransferAccount;
import org.fnppl.opensdx.gui.DefaultMessageHandler;
import org.fnppl.opensdx.gui.Dialogs;
import org.fnppl.opensdx.security.KeyApprovingStore;
import org.fnppl.opensdx.security.OSDXKey;

public class PanelAccount extends JPanel {

	//init fields

	private HashMap<String,JComponent> map = new HashMap<String, JComponent>();

	private JLabel label_type;
	private JComboBox select_type;
	private DefaultComboBoxModel select_type_model;
	private JLabel label_host;
	private JTextField text_host;
	private JLabel label_port;
	private JTextField text_port_integer;
	private JLabel label_prepath;
	private JTextField text_prepath;
	private JLabel label_username;
	private JTextField text_username;
	private JLabel label_keystore;
	private JTextField text_keystore;
	private JButton bu_keystore;
	private JLabel label_keyid;
	private JTextField text_keyid;
	private JButton bu_keyid;
	private JLabel label_filler;

	private FileTransferAccount account;

	public PanelAccount() {
		initComponents();
		initLayout();
		update((FileTransferAccount)null);
		this.setPreferredSize(new Dimension(500,300));
	}



	public void update(FileTransferAccount account) {
		this.account = account;
		if (account == null) {;
			select_type.setSelectedIndex(0);
			text_host.setText("");
			text_port_integer.setText("4221");
			text_prepath.setText("/");
			text_username.setText("");
			text_keystore.setText(new File(new File(System.getProperty("user.home"),"openSDX"),"defaultKeyStore.xml").getAbsolutePath());
			text_keyid.setText("");
			setKeyStroreAndIdVisibility(true);
		} else {
			if (account.type.equals(FileTransferAccount.TYPE_OSDXFILESERVER)) {
				select_type.setSelectedIndex(0);
				text_host.setText(account.host);
				text_port_integer.setText(""+account.port);
				text_prepath.setText(account.prepath);
				text_username.setText(account.username);
				text_keystore.setText(account.keystore_filename);
				text_keyid.setText(account.keyid);
				setKeyStroreAndIdVisibility(true);
			} else {
				select_type.setSelectedIndex(1);
				text_host.setText(account.host);
				//text_port_integer.setText(""+account.port);
				//text_prepath.setText(account.prepath);
				text_port_integer.setText("");
				text_prepath.setText("");
				text_username.setText(account.username);
				text_keystore.setText("");
				text_keyid.setText("");
				setKeyStroreAndIdVisibility(false);
			}
		}
	}

	public FileTransferAccount getAccount() {
		FileTransferAccount a = new FileTransferAccount();
		a.type = (String)select_type.getSelectedItem();
		if (a.type.equals(FileTransferAccount.TYPE_OSDXFILESERVER)) {
			a.host = text_host.getText();
			try  {
				a.port = Integer.parseInt(text_port_integer.getText());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			a.prepath = text_prepath.getText();
			a.username = text_username.getText();
			a.keystore_filename = text_keystore.getText();
			a.keyid = text_keyid.getText();
			return a;
		} else if (a.type.equals(FileTransferAccount.TYPE_FTP)) {
			a.host = text_host.getText();
			a.username = text_username.getText();
			return a;
		}
		return null;
	}

	private void setKeyStroreAndIdVisibility(boolean v) {
		label_port.setVisible(v);
		text_port_integer.setVisible(v);
		label_prepath.setVisible(v);
		text_prepath.setVisible(v);

		label_keystore.setVisible(v);
		text_keystore.setVisible(v);
		bu_keystore.setVisible(v);
		label_keyid.setVisible(v);
		text_keyid.setVisible(v);
		bu_keyid.setVisible(v);
	}

	private void initComponents() {
		Vector<JTextComponent> texts = new Vector<JTextComponent>();

		label_type = new JLabel("Type");

		select_type = new JComboBox();
		select_type_model = new DefaultComboBoxModel();
		select_type.setModel(select_type_model);
		init_select_type_model();
		map.put("select_type", select_type);
		select_type.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				select_type_changed(select_type.getSelectedIndex());
			}
		});

		label_host = new JLabel("Host");

		text_host = new JTextField("");

		text_host.setName("text_host");
		map.put("text_host", text_host);
		texts.add(text_host);

		label_port = new JLabel("Port");

		text_port_integer = new JTextField("");

		text_port_integer.setName("text_port_integer");
		map.put("text_port_integer", text_port_integer);
		texts.add(text_port_integer);

		label_prepath = new JLabel("Prepath");

		text_prepath = new JTextField("");

		text_prepath.setName("text_prepath");
		map.put("text_prepath", text_prepath);
		texts.add(text_prepath);

		label_username = new JLabel("Username");

		text_username = new JTextField("");

		text_username.setName("text_username");
		map.put("text_username", text_username);
		texts.add(text_username);

		label_keystore = new JLabel("Keystore");

		text_keystore = new JTextField("");

		text_keystore.setName("text_keystore");
		map.put("text_keystore", text_keystore);
		texts.add(text_keystore);

		bu_keystore = new JButton("select");
		map.put("bu_keystore", bu_keystore);
		bu_keystore.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bu_keystore_clicked();
			}
		});

		label_keyid = new JLabel("KeyID");

		text_keyid = new JTextField("");

		text_keyid.setName("text_keyid");
		map.put("text_keyid", text_keyid);
		texts.add(text_keyid);

		bu_keyid = new JButton("select");
		map.put("bu_keyid", bu_keyid);
		bu_keyid.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bu_keyid_clicked();
			}
		});

		label_filler = new JLabel("");

	}

	public JComponent getComponent(String name) {
		return map.get(name);
	}
	public void setText(String name, String value) {
		JComponent c = map.get(name);
		if (c!=null && c instanceof JTextComponent) {
			((JTextComponent)c).setText(value);
		}
	}

	public String getText(String name) {
		JComponent c = map.get(name);
		if (c!=null && c instanceof JTextComponent) {
			return ((JTextComponent)c).getText();
		}
		return null;
	}


	public void initLayout() {
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		GridBagConstraints gbc = new GridBagConstraints();



		// Component: label_type
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(label_type,gbc);
		add(label_type);

		// Component: select_type
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 100.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(select_type,gbc);
		add(select_type);

		// Component: label_host
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(label_host,gbc);
		add(label_host);

		// Component: text_host
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(text_host,gbc);
		add(text_host);

		// Component: label_port
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(label_port,gbc);
		add(label_port);

		// Component: text_port_integer
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(text_port_integer,gbc);
		add(text_port_integer);

		// Component: label_prepath
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(label_prepath,gbc);
		add(label_prepath);

		// Component: text_prepath
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(text_prepath,gbc);
		add(text_prepath);

		// Component: label_username
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(label_username,gbc);
		add(label_username);

		// Component: text_username
		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(text_username,gbc);
		add(text_username);

		// Component: label_keystore
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(label_keystore,gbc);
		add(label_keystore);

		// Component: text_keystore
		gbc.gridx = 1;
		gbc.gridy = 5;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 100.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(text_keystore,gbc);
		add(text_keystore);

		// Component: bu_keystore
		gbc.gridx = 2;
		gbc.gridy = 5;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(bu_keystore,gbc);
		add(bu_keystore);

		// Component: label_keyid
		gbc.gridx = 0;
		gbc.gridy = 6;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(label_keyid,gbc);
		add(label_keyid);

		// Component: text_keyid
		gbc.gridx = 1;
		gbc.gridy = 6;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 100.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(text_keyid,gbc);
		add(text_keyid);

		// Component: bu_keyid
		gbc.gridx = 2;
		gbc.gridy = 6;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(bu_keyid,gbc);
		add(bu_keyid);

		// Component: label_filler
		gbc.gridx = 0;
		gbc.gridy = 7;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 100.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(label_filler,gbc);
		add(label_filler);
	}

	private File lastDir = new File(System.getProperty("user.home"));
	// ----- action methods --------------------------------
	public void init_select_type_model() {
		select_type_model.addElement(FileTransferAccount.TYPE_OSDXFILESERVER);
		//select_type_model.addElement(FileTransferAccount.TYPE_FTP);
	}
	public void select_type_changed(int selected) {
		if (selected==0) {
			setKeyStroreAndIdVisibility(true);
		} else {
			setKeyStroreAndIdVisibility(false);
		}
	}
	public void bu_keystore_clicked() {
		File f = Dialogs.chooseOpenFile("Open KeyStore", lastDir, "keystore.xml");
        if (f==null) return;
        lastDir = f.getParentFile();
        text_keystore.setText(f.getAbsolutePath());
	}
	public void bu_keyid_clicked() {
		String filenameKeystore = text_keystore.getText();
        if (filenameKeystore == null || filenameKeystore.length()==0) {
            Dialogs.showMessage("Please select a keystore file first.");
            return;
        }
        File f = new File(filenameKeystore);
        if (!f.exists()) {
            Dialogs.showMessage("Sorry, selected keystore file does not exist.");
            return;
        }
        try {
            KeyApprovingStore keystore = KeyApprovingStore.fromFile(f, new DefaultMessageHandler());
            OSDXKey key = FeedGui.selectPrivateSigningKey(keystore);
            if (key!=null) {
                text_keyid.setText(key.getKeyID());
            }
        } catch (Exception ex) {
            Dialogs.showMessage("Error opening keystore. Please select a valid keytore file.");
            ex.printStackTrace();
        }
	}
}
