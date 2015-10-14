package org.fnppl.opensdx.gui.helper;

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
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Color;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.fnppl.opensdx.gui.Dialogs;
import org.fnppl.opensdx.security.KeyApprovingStore;
import org.fnppl.opensdx.security.MasterKey;
import org.fnppl.opensdx.security.OSDXKey;
import org.fnppl.opensdx.security.SubKey;

import java.io.File;

import java.util.HashMap;
import java.util.Vector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class PanelSign extends JPanel {

	//init fields
	private HashMap<String,JComponent> map = new HashMap<String, JComponent>();

	private JLabel label_file;
	private JTextField text_file;
	private JButton bu_file;
	
	private JLabel label_format;
	private JComboBox select_format;
	private DefaultComboBoxModel select_format_model;
	
	private JLabel label_output;
	private JTextField text_output;

	private JCheckBox check_tsa;
	private JTextField text_tsa;
	
	private JLabel label_keystore;
	private JTextField text_keystore;
	private JButton bu_keystore;
	private JLabel label_keyid;
	private JTextField text_keyid;
	private JButton bu_keyid;
	private JLabel label_listkeyids;
	private JList list_keyids;
	private JScrollPane scroll_list_keyids;
	private DefaultListModel list_keyids_model;
	private JButton bu_add;
	private JButton bu_remove;
	private KeyApprovingStore keystore =null;

	public PanelSign(KeyApprovingStore keystore) {
		this.keystore = keystore;
		initComponents();
		initLayout();
	}

	public File getFile() {
		if (text_file.getText().length()==0) {
			return null;
		}
		return new File(text_file.getText());
	}
	public String getKeyID() {
		return text_keyid.getText();
	}
	
	public String getTSAServer() {
		if (check_tsa.isSelected() && text_tsa.getText().length()>0) {
			return text_tsa.getText();
		}
		return null;
	}
	public String[] getKeyIDs() {
		int kc = list_keyids_model.getSize(); 
		if (kc==0) {
			return null;
		}
		String[] ret = new String[kc];
		for (int i=0;i<kc;i++) {
			ret[i] = (String)list_keyids_model.get(i);
		}
		return ret;
	}
	public void init() {

		text_file.setText("");
		select_format.setSelectedIndex(0);
		text_output.setText("");
		check_tsa.setSelected(false);
		text_tsa.setEnabled(false);
		
		label_format.setVisible(false);
		select_format.setVisible(false);
		
		text_keystore.setText("");
		text_keyid.setText("");
		label_keystore.setVisible(false);
		text_keystore.setVisible(false);
		bu_keystore.setVisible(false);
		
		boolean singleKey = true;
		
		label_keyid.setVisible(singleKey);
		text_keyid.setVisible(singleKey);
		bu_keyid.setVisible(singleKey);

		label_listkeyids.setVisible(!singleKey);
		scroll_list_keyids.setVisible(!singleKey);
		bu_add.setVisible(!singleKey);
		bu_remove.setVisible(!singleKey);

	}

	private void initComponents() {
		Vector<JTextComponent> texts = new Vector<JTextComponent>();

		label_file = new JLabel("File");

		text_file = new JTextField("");

		text_file.setName("text_file");
		map.put("text_file", text_file);
		texts.add(text_file);

		bu_file = new JButton("select");
		map.put("bu_file", bu_file);
		bu_file.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bu_file_clicked();
			}
		});

		label_format = new JLabel("Format");

		select_format = new JComboBox();
		select_format_model = new DefaultComboBoxModel();
		select_format.setModel(select_format_model);
		init_select_format_model();
		map.put("select_format", select_format);
		select_format.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				select_format_changed(select_format.getSelectedIndex());
			}
		});

		label_output = new JLabel("Output File");

		text_output = new JTextField("");
		text_output.setName("text_output");
		map.put("text_output", text_output);
		texts.add(text_output);
		
		text_tsa = new JTextField("");
		text_tsa.setName("text_tsa");
		map.put("text_tsa", text_tsa);
		texts.add(text_tsa);

		check_tsa = new JCheckBox("TSA Server");
		map.put("check_tsa", check_tsa);
		check_tsa.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				check_tsa_changed(check_tsa.isSelected());
			}
		});

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

		label_listkeyids = new JLabel("List of Key IDs");

		list_keyids = new JList();
		list_keyids_model = new DefaultListModel();
		list_keyids.setModel(list_keyids_model);
		init_list_files_model();
		map.put("list_files", list_keyids);
		list_keyids.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				list_files_changed(list_keyids.getSelectedIndex());
			}
		});

		bu_add = new JButton("add");
		map.put("bu_add", bu_add);
		bu_add.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bu_add_clicked();
			}
		});

		bu_remove = new JButton("remove");
		map.put("bu_remove", bu_remove);
		bu_remove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bu_remove_clicked();
			}
		});
		
		text_output.setEditable(false);
		text_keyid.setEditable(false);
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

		text_file.setMinimumSize(new Dimension(450,26));
		text_file.setPreferredSize(new Dimension(450,26));
		Container spacer0 = new Container();
		Container spacer1 = new Container();

		int y = 0;
		// Component: label_file
		gbc.gridx = 0;
		gbc.gridy = y;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(label_file,gbc);
		add(label_file);

		// Component: text_file
		gbc.gridx = 1;
		gbc.gridy = y;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 100.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(text_file,gbc);
		add(text_file);

		// Component: bu_file
		gbc.gridx = 2;
		gbc.gridy = y;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(bu_file,gbc);
		add(bu_file);

		y++;
		// Component: label_format
		gbc.gridx = 0;
		gbc.gridy = y;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(label_format,gbc);
		add(label_format);

		// Component: select_format
		gbc.gridx = 1;
		gbc.gridy = y;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(select_format,gbc);
		add(select_format);

		y++;
		// Component: label_output
		gbc.gridx = 0;
		gbc.gridy = y;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(label_output,gbc);
		add(label_output);

		// Component: text_output
		gbc.gridx = 1;
		gbc.gridy = y;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(text_output,gbc);
		add(text_output);

		y++;
		// Component: label_keystore
		gbc.gridx = 0;
		gbc.gridy = y;
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
		gbc.gridy = y;
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
		gbc.gridy = y;
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

		y++;
		// Component: label_keyid
		gbc.gridx = 0;
		gbc.gridy = y;
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
		gbc.gridy = y;
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
		gbc.gridy = y;
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

		y++;	
		// Component: check_tsa
		gbc.gridx = 0;
		gbc.gridy = y;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(check_tsa,gbc);
		add(check_tsa);

		// Component: text_tsa
		gbc.gridx = 1;
		gbc.gridy = y;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(text_tsa,gbc);
		add(text_tsa);
		
		y++;
		// Component: label_listkeyids
		gbc.gridx = 0;
		gbc.gridy = y;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(label_listkeyids,gbc);
		add(label_listkeyids);

		y++;
		// Component: list_keyids
		scroll_list_keyids = new JScrollPane(list_keyids);
		gbc.gridx = 0;
		gbc.gridy = y;
		gbc.gridwidth = 2;
		gbc.gridheight = 3;
		gbc.weightx = 100.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(scroll_list_keyids,gbc);
		add(scroll_list_keyids);

		
		// Component: bu_add
		gbc.gridx = 2;
		gbc.gridy = y;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(bu_add,gbc);
		add(bu_add);
		
		y++;
		// Component: bu_remove
		gbc.gridx = 2;
		gbc.gridy = y;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(bu_remove,gbc);
		add(bu_remove);

		y++;
		// Component: filler
		JLabel filler = new JLabel();
		gbc.gridx = 0;
		gbc.gridy = 12;
		gbc.gridwidth = 3;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 100.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(filler,gbc);
		add(filler);
	}

	public OSDXKey selectSigningKey() {
		if (keystore==null) return null;
		Vector<OSDXKey> keys = keystore.getAllPrivateSigningKeys();
		if (keys.size()==0) {
			Dialogs.showMessage("Sorry, no keys for signtaure in keystore");
			return null;
		}
		Vector<String> select = new Vector<String>();
		int[] map = new int[keys.size()];
		for (int i=0;i<keys.size();i++) {
			OSDXKey k = keys.get(i);
			if (k.isMaster()) {
				select.add(k.getKeyID()+", "+((MasterKey)k).getIDEmailAndMnemonic());
			}
			else if (k.isSub() && ((SubKey)k).getParentKey()!=null) {
				select.add(k.getKeyID()+" subkey of "+((SubKey)k).getParentKey().getIDEmailAndMnemonic());
			}
			else {
				select.add(k.getKeyID());
			}
			map[select.size()-1] = i;
		}
		int ans = Dialogs.showSelectDialog("Select signature key","Please select a key for signature", select);
		if (ans>=0 && ans<select.size()) {
			return keys.get(map[ans]);
		}
		return null;
	}

	private File lastDir = null;
	// ----- action methods --------------------------------
	public void bu_file_clicked() {
		if (lastDir ==null) {
			lastDir = new File(System.getProperty("user.home"));
		}
		File f = Dialogs.chooseOpenFile("Choose File to encrypt", lastDir, "");
		if (f!=null) {
			lastDir = f.getParentFile();
			text_file.setText(f.getAbsolutePath());
			if (select_format.getSelectedIndex()==0) {
				//detatched
				text_output.setText(f.getAbsolutePath()+"_signature.xml");
			} else {
				//inline
				text_output.setText(f.getAbsolutePath()+"_signed.osdx");
			}
		}
	}
	
	public void init_select_format_model() {
		select_format_model.addElement("detatched signature file");
		select_format_model.addElement("inline with original data");
	}
	
	public void select_format_changed(int selected) {
		if (selected==0) {
			//detatched
			String f= text_file.getText();
			if (f.length()>0) {
				text_output.setText(f+"_signed.osdx");
			}
		} else {
			//inline
			String f= text_file.getText();
			if (f.length()>0) {	
				text_output.setText(f+"_signature.xml");
			}
		}
	}
	public void check_tsa_changed(boolean selected) {
		text_tsa.setEnabled(selected);
	}
	public void bu_keystore_clicked() {
		//not implemented 
	}
	public void bu_keyid_clicked() {
		OSDXKey k = selectSigningKey();
		if (k!=null) {
			text_keyid.setText(k.getKeyID());
		}
	}
	public void init_list_files_model() {
		//nothing to do
	}
	public void list_files_changed(int selected) {
		//nothing to do
	}
	public void bu_add_clicked() {
		OSDXKey k = selectSigningKey();
		if (k!=null) {
			list_keyids_model.addElement(k.getKeyID());
		}
	}
	public void bu_remove_clicked() {
		int sel = list_keyids.getSelectedIndex();
		if (sel>=0 && sel<list_keyids_model.getSize()) {
			list_keyids_model.remove(sel);
		}
	}

}
