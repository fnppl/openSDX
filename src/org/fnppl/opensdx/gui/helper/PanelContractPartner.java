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
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;

import org.fnppl.opensdx.common.ContractPartner;
import org.fnppl.opensdx.common.FeedInfo;
import org.fnppl.opensdx.dmi.FeedGui;
import org.fnppl.opensdx.dmi.FeedGuiTooltips;
import org.fnppl.opensdx.gui.DefaultMessageHandler;
import org.fnppl.opensdx.gui.Dialogs;
import org.fnppl.opensdx.security.KeyApprovingStore;
import org.fnppl.opensdx.security.OSDXKey;

public class PanelContractPartner extends JPanel implements MyObservable, TextChangeListener {

	//init fields
	private HashMap<String,JComponent> map = new HashMap<String, JComponent>();

	private JLabel label_contractpartnerid;
	private JTextField text_contractpartnerid;
	private JLabel label_ourcontractpartnerid;
	private JTextField text_ourcontractpartnerid;
	private JLabel label_email;
	private JTextField text_email;
	private JLabel label_keyid;
	private JTextField text_keyid;
	private JButton bu_keyid_select;
	private JButton bu_keyid_remove;
	private TitledBorder border;
	
	private FeedGui gui = null;
	private int contractPartnerType = 0;
	private ContractPartner contractpartner = null;

	public PanelContractPartner(FeedGui gui, int contractPartnerType) {
		this.gui = gui;
		this.contractPartnerType = contractPartnerType;
		initFocusTraversal();
		initComponents();
		initLayout();
	}
	
	public void initTooltips() {
		if (contractPartnerType == ContractPartner.ROLE_SENDER) {
			this.setToolTipText(FeedGuiTooltips.sender);
		}
		else if (contractPartnerType == ContractPartner.ROLE_LICENSOR) {
			this.setToolTipText(FeedGuiTooltips.licensor);
		}
		else if (contractPartnerType == ContractPartner.ROLE_LICENSEE) {
			this.setToolTipText(FeedGuiTooltips.licensee);
		}
		
	}

	public void update() {
		ContractPartner cp = getContractPartner();
		if (cp==null) {
			text_contractpartnerid.setText("");
			text_ourcontractpartnerid.setText("");
			text_email.setText("");
			text_keyid.setText("");
		} else {
			text_contractpartnerid.setText(cp.getContractPartnerID());
			text_ourcontractpartnerid.setText(cp.getOurContractPartnerID());
			text_email.setText(cp.getEmail());
			text_keyid.setText(cp.getKeyid());
		}
	}
	
	private ContractPartner getContractPartner() {
		if (gui==null || gui.getCurrentFeed()==null) return null;
		FeedInfo info = gui.getCurrentFeed().getFeedinfo();
		if (info ==null) return null;
		ContractPartner cp = null;
		if (contractPartnerType==ContractPartner.ROLE_SENDER) {
			cp = info.getSender();
		}
		else if (contractPartnerType==ContractPartner.ROLE_LICENSOR) {
			cp = info.getLicensor();
		}
		else if (contractPartnerType==ContractPartner.ROLE_LICENSEE) {
			cp = info.getLicensee();
		}
		return cp;
	}
	
	private FeedInfo getFeedInfo() {
		if (gui==null || gui.getCurrentFeed()==null) return null;
		FeedInfo info = gui.getCurrentFeed().getFeedinfo();
		return info;
	}


	@SuppressWarnings("unchecked")
	private void initFocusTraversal() {
		Set forwardKeys = new HashSet(getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
		forwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
		setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,forwardKeys);
	}

	private void initComponents() {
		String tit = ContractPartner.getKeyname(contractPartnerType);
		tit = tit.substring(0,1).toUpperCase()+tit.substring(1);
		border = new TitledBorder(tit);
		this.setBorder(border);
		
		Vector<JTextComponent> texts = new Vector<JTextComponent>();
		
		label_contractpartnerid = new JLabel("ID");

		text_contractpartnerid = new JTextField("");

		text_contractpartnerid.setName("text_contractpartnerid");
		map.put("text_contractpartnerid", text_contractpartnerid);
		texts.add(text_contractpartnerid);

		label_ourcontractpartnerid = new JLabel("Our ID");

		text_ourcontractpartnerid = new JTextField("");

		text_ourcontractpartnerid.setName("text_ourcontractpartnerid");
		map.put("text_ourcontractpartnerid", text_ourcontractpartnerid);
		texts.add(text_ourcontractpartnerid);
		
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

		DocumentInstantChangeListener chl = new DocumentInstantChangeListener(this);
		for (JTextComponent text : texts) {
			if (text instanceof JTextField) {
				chl.addTextComponent(text);
			}
		}
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
	
	// Component: label_contractpartnerid
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
	gbl.setConstraints(label_contractpartnerid,gbc);
	add(label_contractpartnerid);

	// Component: text_contractpartnerid
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
	gbl.setConstraints(text_contractpartnerid,gbc);
	add(text_contractpartnerid);

	// Component: label_ourcontractpartnerid
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
	gbl.setConstraints(label_ourcontractpartnerid,gbc);
	add(label_ourcontractpartnerid);

	// Component: text_ourcontractpartnerid
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
	gbl.setConstraints(text_ourcontractpartnerid,gbc);
	add(text_ourcontractpartnerid);

	// Component: label_email
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
	gbl.setConstraints(label_email,gbc);
	add(label_email);

	// Component: text_email
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
	gbc.insets = new Insets(5,5,5,5);
	gbl.setConstraints(text_email,gbc);
	add(text_email);

	// Component: label_keyid
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
	gbl.setConstraints(label_keyid,gbc);
	add(label_keyid);

	// Component: text_keyid
	gbc.gridx = 1;
	gbc.gridy = 3;
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
	gbl.setConstraints(bu_keyid_select,gbc);
	add(bu_keyid_select);

	// Component: bu_keyid_remove
	gbc.gridx = 3;
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
	gbl.setConstraints(bu_keyid_remove,gbc);
	add(bu_keyid_remove);
}
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch(Exception ex){
			System.out.println("Nimbus look & feel not available");
	}
		PanelContractPartner p = new PanelContractPartner(null,1);
		JFrame f = new JFrame("PanelContractPartner");
		f.setContentPane(p);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(1024,768);
		f.setVisible(true);
	}


// ----- action methods --------------------------------
	private File lastDir = null;
	public void bu_keyid_select_clicked() {
		ContractPartner cp = getContractPartner();
		if (cp!=null) {
			
			File f = null;
			String filenameKeystore = gui.getDefaultKeyStore();
	        if (filenameKeystore != null) {
	        	f = new File(filenameKeystore);
	        	if (!f.exists()) {
	        		f = null;
		        }
	        }	        
	        if (f==null) {
	        	f = Dialogs.chooseOpenFile("Open KeyStore", lastDir, "keystore.xml");
	        }
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
		ContractPartner cp = getContractPartner();
		if (cp!=null) {
			cp.keyid(null);
			notifyChanges();
		}
	}
	
	public void text_changed(JTextComponent text) {
		ContractPartner cp = getContractPartner();
		if (cp==null) {
			FeedInfo info = getFeedInfo();
			if (info!=null) {
				cp = ContractPartner.make(contractPartnerType, text_contractpartnerid.getText(), text_ourcontractpartnerid.getText());
				String t = text_email.getText();
				if (t!=null && t.length()>0) cp.email(t);
				t = text_keyid.getText();
				if (t!=null && t.length()>0) cp.keyid(t);
				if (contractPartnerType == ContractPartner.ROLE_SENDER) {
					info.sender(cp);
				}
				else if (contractPartnerType == ContractPartner.ROLE_LICENSOR) {
					info.licensor(cp);
				}
				else if (contractPartnerType == ContractPartner.ROLE_LICENSEE) {
					info.licensee(cp);
				}
				notifyChanges();
			}
			return;
		}

		String t = text.getText();
		if (t!=null && t.length()==0) t = null;
		if (text == text_contractpartnerid) {
			cp.contractpartnerid(t);
		}
		else if (text == text_ourcontractpartnerid) {
			cp.ourcontractpartnerid(t);
		}
		else if (text == text_email) {
			cp.email(t);
		}
		else if (text == text_keyid) {
			cp.keyid(t);
		}
		notifyChanges();
		//text.requestFocusInWindow();
		//text.transferFocus();
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
