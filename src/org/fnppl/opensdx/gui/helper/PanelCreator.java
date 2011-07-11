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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Color;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;

import org.fnppl.opensdx.common.Creator;
import org.fnppl.opensdx.common.FeedInfo;
import org.fnppl.opensdx.dmi.FeedGui;
import org.fnppl.opensdx.gui.DefaultMessageHandler;
import org.fnppl.opensdx.gui.Dialogs;
import org.fnppl.opensdx.security.KeyApprovingStore;
import org.fnppl.opensdx.security.OSDXKey;

import java.io.File;
import java.util.HashMap;
import java.util.Vector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PanelCreator extends JPanel implements MyObservable {

	//init fields
	private DocumentChangeListener documentListener;
	private KeyAdapter keyAdapter;
	private HashMap<String,JComponent> map = new HashMap<String, JComponent>();

	private JLabel label_email;
	private JTextField text_email;
	private JLabel label_userid;
	private JTextField text_userid;
	private JLabel label_keyid;
	private JTextField text_keyid;
	private JButton bu_keyid_select;
	private JButton bu_keyid_remove;
	private TitledBorder border;
	
	private FeedGui gui = null;

	public PanelCreator(FeedGui gui) {
		this.gui = gui;
		initKeyAdapter();
		initComponents();
		initLayout();
	}

	public void update() {
		Creator c = getCreator();
		if (c==null) {
			text_userid.setText("");
			text_email.setText("");
			text_keyid.setText("");
		} else {
			text_userid.setText(c.getUserid());
			text_email.setText(c.getEmail());
			text_keyid.setText(c.getKeyid());
		}
		documentListener.saveStates();
	}
	
	private Creator getCreator() {
		if (gui==null || gui.getCurrentFeed()==null) return null;
		FeedInfo info = gui.getCurrentFeed().getFeedinfo();
		if (info ==null) return null;
		Creator c = info.getCreator();
		return c;
	}
	
	private FeedInfo getFeedInfo() {
		if (gui==null || gui.getCurrentFeed()==null) return null;
		FeedInfo info = gui.getCurrentFeed().getFeedinfo();
		return info;
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
								text_changed(text);
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
		border = new TitledBorder("Creator");
		this.setBorder(border);
		
		Vector<JTextComponent> texts = new Vector<JTextComponent>();
		
		label_userid = new JLabel("User ID");

		text_userid = new JTextField("");

		text_userid.setName("text_userid");
		map.put("text_userid", text_userid);
		texts.add(text_userid);
		
		label_email = new JLabel("Email");

		text_email = new JTextField("");

		text_email.setName("text_email");
		map.put("text_email", text_email);
		texts.add(text_email);


		label_keyid = new JLabel("Key ID");

		text_keyid = new JTextField("");

		text_keyid.setName("text_keyid");
		map.put("text_keyid", text_keyid);
		texts.add(text_keyid);

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

		documentListener = new DocumentChangeListener(texts);
		for (JTextComponent text : texts) {
			text.getDocument().addDocumentListener(documentListener);
			if (text instanceof JTextField) text.addKeyListener(keyAdapter);
		}
		documentListener.saveStates();
	}



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


public void initLayout() {
	GridBagLayout gbl = new GridBagLayout();
	setLayout(gbl);
	GridBagConstraints gbc = new GridBagConstraints();

	// Component: label_email
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
	gbl.setConstraints(label_email,gbc);
	add(label_email);

	// Component: text_email
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
	gbc.insets = new Insets(5,5,5,5);
	gbl.setConstraints(text_email,gbc);
	add(text_email);

	// Component: label_userid
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
	gbl.setConstraints(label_userid,gbc);
	add(label_userid);

	// Component: text_userid
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
	gbc.insets = new Insets(5,5,5,5);
	gbl.setConstraints(text_userid,gbc);
	add(text_userid);

	// Component: label_keyid
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
	gbl.setConstraints(label_keyid,gbc);
	add(label_keyid);

	// Component: text_keyid
	gbc.gridx = 1;
	gbc.gridy = 2;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 50.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(5,5,5,5);
	gbl.setConstraints(text_keyid,gbc);
	add(text_keyid);

	// Component: bu_keyid_select
	gbc.gridx = 2;
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
	gbl.setConstraints(bu_keyid_select,gbc);
	add(bu_keyid_select);

	// Component: bu_keyid_remove
	gbc.gridx = 3;
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
	gbl.setConstraints(bu_keyid_remove,gbc);
	add(bu_keyid_remove);
}


// ----- action methods --------------------------------
	private File lastDir = null;
	public void bu_keyid_select_clicked() {
		Creator cp = getCreator();
		if (cp!=null) {
			File f = Dialogs.chooseOpenFile("Open KeyStore", lastDir, "keystore.xml");
			if (f==null) return;
			lastDir = f.getParentFile();
	        if (!f.exists()) {
	            Dialogs.showMessage("Sorry. selected keystore file does not exist.");
	            return;
	        }
	        try {
	            KeyApprovingStore keystore = KeyApprovingStore.fromFile(f, new DefaultMessageHandler());
	            OSDXKey key = FeedGui.selectFromAllKeys(keystore, "Please select a key from keystore");
	            if (key!=null) {
	                cp.keyid(key.getKeyID());
	                text_keyid.setText(key.getKeyID());
	                notifyChanges();
	            }
	        } catch (Exception ex) {
	            Dialogs.showMessage("Error opening keystore. Please select a valid keytore file.");
	            ex.printStackTrace();
	        }
		}
	}
	
	public void bu_keyid_remove_clicked() {
		Creator cp = getCreator();
		if (cp!=null) {
			cp.keyid(null);
			notifyChanges();
		}
	}
	
	public void text_changed(JTextComponent text) {
		Creator c = getCreator();
		if (c==null) {
			FeedInfo info = getFeedInfo();
			if (info!=null) {
				c = Creator.make();
				c.userid(text_userid.getText());
				c.email(text_email.getText());
				c.keyid(text_keyid.getText());
				notifyChanges();
			}
			return;
		}

		String t = text.getText();
		if (text == text_userid) {
			c.userid(t);
		}
		else if (text == text_email) {
			c.email(t);
		}
		else if (text == text_keyid) {
			c.keyid(t);
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
