package org.fnppl.opensdx.gui.helper;

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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Color;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;

import org.fnppl.opensdx.common.*;
import org.fnppl.opensdx.dmi.FeedGui;
import org.fnppl.opensdx.gui.DefaultMessageHandler;
import org.fnppl.opensdx.gui.Dialogs;
import org.fnppl.opensdx.security.KeyApprovingStore;
import org.fnppl.opensdx.security.OSDXKey;

import java.io.File;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Vector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PanelReceiver extends JPanel implements MyObservable {

	//init fields
	private DocumentChangeListener documentListener;
	private KeyAdapter keyAdapter;
	private HashMap<String,JComponent> map = new HashMap<String, JComponent>();

	private JLabel label_type;
	private JComboBox select_type;
	private DefaultComboBoxModel select_type_model;
	private JLabel label_servername;
	private JTextField text_servername;
	private JLabel label_serveripv4;
	private JTextField text_serveripv4;
	private JLabel label_auth_type;
	private JComboBox select_auth_type;
	private DefaultComboBoxModel select_auth_type_model;
	private JLabel label_keystore;
	private JTextField text_keystore;
	private JButton bu_keystore_select;
	private JButton bu_keystore_remove;
	private JLabel label_keyid;
	private JTextField text_keyid;
	private JButton bu_keyid_select;
	private JButton bu_keyid_remove;
	private JLabel label_username;
	private JTextField text_username;
	private JButton bu_beam_me_up;
	
	private FeedGui gui = null;
	private Receiver receiver = null;
	
	public PanelReceiver(FeedGui gui) {
		this.gui = gui;
		this.receiver = null;
		initKeyAdapter();
		initComponents();
		initLayout();
		setFieldsVisibility();
	}
	
	public PanelReceiver(Receiver receiver) {
		this.gui = null;
		this.receiver = receiver;
		initKeyAdapter();
		initComponents();
		initLayout();
		update();
		//set editable false
		select_type.setEnabled(false);
		text_servername.setEditable(false);
		text_serveripv4.setEditable(false);
		select_auth_type.setEnabled(false);
		text_keystore.setEditable(false);
		text_keyid.setEditable(false);
		text_username.setEditable(false);
		
		bu_keyid_select.setVisible(false);
		bu_keyid_remove.setVisible(false);
		bu_keystore_select.setVisible(false);
		bu_keystore_remove.setVisible(false);
		
		bu_beam_me_up.setVisible(false);
	}


	private void initKeyAdapter() {
		keyAdapter = new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					if (e.getComponent() instanceof JTextField) {
						try {
							JTextComponent text = (JTextComponent)e.getComponent();
							String t = text.getText();
							String name = text.getName();
							if (documentListener.formatOK(name,t)) {
								text_changed(name);
								documentListener.saveState(text);
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
				else if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					if (e.getComponent() instanceof JTextField) {
						JTextField text = (JTextField)e.getComponent();
						text.setText(documentListener.getSavedText(text));
						text.setBackground(Color.WHITE);
					}
				}
			}
		};
	}

	private void initComponents() {
		Vector<JTextComponent> texts = new Vector<JTextComponent>();

		this.setBorder(new TitledBorder("Receiver"));
		
		label_type = new JLabel("type");

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

		label_servername = new JLabel("servername");

		text_servername = new JTextField("");

		text_servername.setName("text_servername");
		map.put("text_servername", text_servername);
		texts.add(text_servername);

		label_serveripv4 = new JLabel("server IPv4");

		text_serveripv4 = new JTextField("");

		text_serveripv4.setName("text_serveripv4");
		map.put("text_serveripv4", text_serveripv4);
		texts.add(text_serveripv4);

		label_auth_type = new JLabel("auth type");

		select_auth_type = new JComboBox();
		select_auth_type_model = new DefaultComboBoxModel();
		select_auth_type.setModel(select_auth_type_model);
		init_select_auth_type_model();
		map.put("select_auth_type", select_auth_type);
		select_auth_type.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				select_auth_type_changed(select_auth_type.getSelectedIndex());
			}
		});

		label_keystore = new JLabel("KeyStore");

		text_keystore = new JTextField("");
		text_keystore.setEditable(false);
		text_keystore.setName("text_keystore");
		map.put("text_keystore", text_keystore);
		//texts.add(text_keystore);

		bu_keystore_select = new JButton("select");
		map.put("bu_keystore_select", bu_keystore_select);
		bu_keystore_select.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bu_keystore_select_clicked();
			}
		});

		bu_keystore_remove = new JButton("x");
		map.put("bu_keystore_remove", bu_keystore_remove);
		bu_keystore_remove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bu_keystore_remove_clicked();
			}
		});

		label_keyid = new JLabel("Key ID");

		text_keyid = new JTextField("");
		text_keyid.setEditable(false);
		text_keyid.setName("text_keyid");
		map.put("text_keyid", text_keyid);
		//texts.add(text_keyid);

		bu_keyid_select = new JButton("select");
		map.put("bu_keyid_select", bu_keyid_select);
		bu_keyid_select.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bu_keyid_select_clicked();
			}
		});

		bu_keyid_remove = new JButton("x");
		map.put("bu_keyid_remove", bu_keyid_remove);
		bu_keyid_remove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bu_keyid_remove_clicked();
			}
		});

		label_username = new JLabel("username");

		text_username = new JTextField("");

		text_username.setName("text_username");
		map.put("text_username", text_username);
		texts.add(text_username);

	
		bu_beam_me_up = new JButton("<html><b>beam me up!</b></html>");
		map.put("bu_beam_me_up", bu_beam_me_up);
		bu_beam_me_up.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bu_beam_me_up_clicked();
			}
		});
		
		documentListener = new DocumentChangeListener(texts);
		for (JTextComponent text : texts) {
			text.getDocument().addDocumentListener(documentListener);
			if (text instanceof JTextField) text.addKeyListener(keyAdapter);
		}
		documentListener.saveStates();	}



	public void updateDocumentListener() {
		documentListener.saveStates();
	}

	public void updateDocumentListener(JTextComponent t) {
		documentListener.saveState(t);
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


	public void setCheck(String name, boolean value) {
		JComponent c = map.get(name);
		if (c!=null && c instanceof JCheckBox) {
			((JCheckBox)c).setSelected(value);
		}
	}

	public boolean getCheck(String name) {
		JComponent c = map.get(name);
		if (c!=null && c instanceof JCheckBox) {
			return ((JCheckBox)c).isSelected();
		}
		throw new RuntimeException("name "+name+" not a JCheckBox");
	}


	public void initLayout() {
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
		int insets = 5;
		if (gui==null) insets = 2;
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
		gbc.insets = new Insets(insets,insets,insets,insets);
		gbl.setConstraints(label_type,gbc);
		add(label_type);

		// Component: select_type
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 3;
		gbc.gridheight = 1;
		gbc.weightx = 50.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
	//	gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(select_type,gbc);
		add(select_type);

		// Component: label_servername
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
	//	gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(label_servername,gbc);
		add(label_servername);

		// Component: text_servername
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 3;
		gbc.gridheight = 1;
		gbc.weightx = 50.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
	//	gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(text_servername,gbc);
		add(text_servername);

		// Component: label_serveripv4
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
	//	gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(label_serveripv4,gbc);
		add(label_serveripv4);

		// Component: text_serveripv4
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridwidth = 3;
		gbc.gridheight = 1;
		gbc.weightx = 50.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
	//	gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(text_serveripv4,gbc);
		add(text_serveripv4);

		// Component: label_auth_type
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
	//	gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(label_auth_type,gbc);
		add(label_auth_type);

		// Component: select_auth_type
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.gridwidth = 3;
		gbc.gridheight = 1;
		gbc.weightx = 50.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
	//	gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(select_auth_type,gbc);
		add(select_auth_type);

		// Component: label_keystore
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
	//	gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(label_keystore,gbc);
		add(label_keystore);

		// Component: text_keystore
		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 50.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
//		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(text_keystore,gbc);
		add(text_keystore);

		// Component: bu_keystore_select
		gbc.gridx = 2;
		gbc.gridy = 4;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
//		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(bu_keystore_select,gbc);
		add(bu_keystore_select);

		// Component: bu_keystore_remove
		gbc.gridx = 3;
		gbc.gridy = 4;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
//		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(bu_keystore_remove,gbc);
		add(bu_keystore_remove);

		// Component: label_keyid
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
//		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(label_keyid,gbc);
		add(label_keyid);

		// Component: text_keyid
		gbc.gridx = 1;
		gbc.gridy = 5;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 50.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
//		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(text_keyid,gbc);
		add(text_keyid);

		// Component: bu_keyid_select
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
//		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(bu_keyid_select,gbc);
		add(bu_keyid_select);

		// Component: bu_keyid_remove
		gbc.gridx = 3;
		gbc.gridy = 5;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
//		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(bu_keyid_remove,gbc);
		add(bu_keyid_remove);

		// Component: label_username
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
//		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(label_username,gbc);
		add(label_username);

		// Component: text_usernam
		gbc.gridx = 1;
		gbc.gridy = 6;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 50.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
//		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(text_username,gbc);
		add(text_username);
				
		// Component: filler
		JLabel filler = new JLabel();
		gbc.gridx = 1;
		gbc.gridy = 7;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 50.0;
		gbc.weighty = 100.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
//		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(filler,gbc);
		add(filler);
		
		// Component: bu_beam_me_up
		Dimension dim = new Dimension(450,45);
		bu_beam_me_up.setMinimumSize(dim);
		bu_beam_me_up.setMaximumSize(dim);
		bu_beam_me_up.setPreferredSize(dim);
		
		gbc.gridx = 0;
		gbc.gridy = 8;
		gbc.gridwidth = 4;
		gbc.gridheight = 1;
		gbc.weightx = 50.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(bu_beam_me_up,gbc);
		add(bu_beam_me_up);
	}
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch(Exception ex){
			System.out.println("Nimbus look & feel not available");
		}
		PanelReceiver p = new PanelReceiver((Receiver)null);
		JFrame f = new JFrame("PanelReceiver");
		f.setContentPane(p);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(1024,768);
		f.setVisible(true);
	}


	// ----- action methods --------------------------------
    private File lastDir = null;

    
    public void update() {
    	Receiver r = getReceiver();
    	if (r==null) {
    		r = receiver;
    	}
    	if (r==null) {
    		text_servername.setText("");
    		text_serveripv4.setText("");
    		text_keystore.setText("");
    		text_keyid.setText("");
    		text_username.setText("");
    		documentListener.saveStates();
    		select_type.setSelectedIndex(0);
    		select_auth_type.setSelectedIndex(1);
    		setFieldsVisibility();
    	} else {
    		text_servername.setText(r.getServername());
    		text_serveripv4.setText(r.getServerIPv4());
    		text_keystore.setText(r.getFileKeystore());
    		text_keyid.setText(r.getKeyID());
    		text_username.setText(r.getUsername());
    		documentListener.saveStates();
    		select_type.setSelectedItem(r.getType());
    		select_auth_type.setSelectedItem(r.getAuthType());
    		setFieldsVisibility();
    	}
    }
    
	private Receiver getReceiver() {
		if (gui!=null) {
			Feed feed = gui.getCurrentFeed();
			if (feed!=null && feed.getFeedinfo()!=null) {
				return feed.getFeedinfo().getReceiver();
			}
		}
		return null;
	}
	
	public void init_select_type_model() {
		for (String t : org.fnppl.opensdx.common.Receiver.SUPPORTED_TRANSFER_TYPES) {
			select_type_model.addElement(t);	
		}
	}
	public void select_type_changed(int selected) {
		setFieldsVisibility();
		Receiver r = getReceiver();
		if (r==null) return;
		r.type((String)select_type_model.getElementAt(selected));
		notifyChanges();
	}
	
	public void init_select_auth_type_model() {
		for (String t : org.fnppl.opensdx.common.Receiver.SUPPORTED_AUTH_TYPES) {
			select_auth_type_model.addElement(t);
		}
	}
	public void select_auth_type_changed(int selected) {
		setFieldsVisibility();
		Receiver r = getReceiver();
		if (r==null) return;
		r.authtype((String)select_auth_type_model.getElementAt(selected));
		notifyChanges();
	}
	
	private void setFieldsVisibility() {
		String type = (String)select_type.getSelectedItem();
		if (type==null) return;
		if (type.equals(Receiver.TRANSFER_TYPE_FTP)||type.equals(Receiver.TRANSFER_TYPE_SFTP)||type.equals(Receiver.TRANSFER_TYPE_FTPS)||type.equals(Receiver.TRANSFER_TYPE_WEBDAV)) {
			label_servername.setVisible(true);
			text_servername.setVisible(true);
			label_serveripv4.setVisible(true);
			text_serveripv4.setVisible(true);
			label_auth_type.setVisible(true);
			select_auth_type.setVisible(true);
			select_auth_type.setEnabled(false);
			select_auth_type.setSelectedItem(Receiver.AUTH_TYPE_LOGIN);
			label_username.setVisible(true);
			text_username.setVisible(true);
			
			boolean visible = false;
			label_keystore.setVisible(visible);
			text_keystore.setVisible(visible);
			bu_keystore_select.setVisible(visible);
			bu_keystore_remove.setVisible(visible);
			
			label_keyid.setVisible(visible);
			text_keyid.setVisible(visible);
			bu_keyid_select.setVisible(visible);
			bu_keyid_remove.setVisible(visible);
		}
		else if (type.equals(Receiver.TRANSFER_TYPE_OSDX_FILESERVER)) {
			label_servername.setVisible(true);
			text_servername.setVisible(true);
			label_serveripv4.setVisible(true);
			text_serveripv4.setVisible(true);
			label_auth_type.setVisible(true);
			select_auth_type.setVisible(true);
			select_auth_type.setEnabled(false);
			select_auth_type.setSelectedItem(Receiver.AUTH_TYPE_KEYFILE);
			
			boolean visible = true;
			label_username.setVisible(visible);
			text_username.setVisible(visible);
			
			label_keystore.setVisible(visible);
			text_keystore.setVisible(visible);
			bu_keystore_select.setVisible(visible);
			bu_keystore_remove.setVisible(visible);
			
			label_keyid.setVisible(visible);
			text_keyid.setVisible(visible);
			bu_keyid_select.setVisible(visible);
			bu_keyid_remove.setVisible(visible);

		}
		
		//String authType = (String)select_auth_type.getSelectedItem();
		//if (authType==null) return;
		
		
		
	}
	public void bu_keystore_select_clicked() {
		Receiver r = getReceiver();
		if (r==null) return;
		File f = Dialogs.chooseOpenFile("Open KeyStore", lastDir, "keystore.xml");
        if (f==null) return;
        lastDir = f.getParentFile();
        r.file_keystore(f.getAbsolutePath());
        text_keystore.setText(f.getAbsolutePath());
		notifyChanges();
	}
	public void bu_keystore_remove_clicked() {
		Receiver r = getReceiver();
		if (r==null) return;
		r.file_keystore(null);
		text_keystore.setText("");
		r.keyid(null);
		text_keyid.setText("");
		notifyChanges();
	}
	public void bu_keyid_select_clicked() {
		Receiver r = getReceiver();
		if (r==null) return;
		
		String filenameKeystore = r.getFileKeystore();
        if (filenameKeystore == null) {
            Dialogs.showMessage("Please select a keystore file first.");
            return;
        }
        File f = new File(filenameKeystore);
        if (!f.exists()) {
            Dialogs.showMessage("Sorry. selected keystore file does not exist.");
            return;
        }
        try {
            KeyApprovingStore keystore = KeyApprovingStore.fromFile(f, new DefaultMessageHandler());
            OSDXKey key = FeedGui.selectPrivateSigningKey(keystore);
            if (key!=null) {
                r.keyid(key.getKeyID());
                text_keyid.setText(key.getKeyID());
                notifyChanges();
            }
        } catch (Exception ex) {
            Dialogs.showMessage("Error opening keystore. Please select a valid keytore file.");
            ex.printStackTrace();
        }
		notifyChanges();
	}
	public void bu_keyid_remove_clicked() {
		Receiver r = getReceiver();
		if (r==null) return;
		r.keyid(null);
		text_keyid.setText("");
		notifyChanges();		
	}
	
	public void bu_beam_me_up_clicked() {
		if(gui!=null) {
			gui.sendFeedToReceiver();
		}
	}
	
	public void text_changed(String name) {
		Receiver r = getReceiver();
		if (r==null) return;
		if (name.equals("text_servername")) {
			String hostname = text_servername.getText();
			r.servername(hostname);
			if (hostname != null && !hostname.equals(""))  {
				try {
					
					InetAddress addr = InetAddress.getByName(hostname);
					String ip = addr.getHostAddress();
					text_serveripv4.setText(ip);
					r.serveripv4(ip);
					documentListener.saveState(text_serveripv4);
				} catch (Exception ex) {
					text_serveripv4.setText("IP address could not be resolved");
					r.serveripv4("");
					//documentListener.saveState(text_serveripv4);	
				}
			} else {
				text_serveripv4.setText("");
				r.serveripv4("");
				documentListener.saveState(text_serveripv4);
			}
		} else if (name.equals("text_serveripv4")) {
			r.serveripv4(text_serveripv4.getText());
		} else if (name.equals("text_username")) {
			r.username(text_username.getText());
		}
		notifyChanges();
	}
	
	//observable
	private Vector<MyObserver> observers = new Vector<MyObserver>();
	public void addObserver(MyObserver observer) {
		observers.add(observer);
	}
	public void notifyChanges() {
		for (MyObserver ob : observers) {
			ob.notifyChange(this);
		}
	}

}
