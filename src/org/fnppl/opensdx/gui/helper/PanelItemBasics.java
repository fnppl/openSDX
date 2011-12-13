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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Color;
import java.awt.KeyboardFocusManager;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.fnppl.opensdx.common.Bundle;
import org.fnppl.opensdx.common.Item;
import org.fnppl.opensdx.dmi.FeedGui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PanelItemBasics extends JPanel implements MyObservable, TextChangeListener {

	//init fields
	private Item item = null;
	//private DocumentChangeListener documentListener;
	//private KeyAdapter keyAdapter;
	private HashMap<String,JComponent> map = new HashMap<String, JComponent>();

	private JLabel label_displayname;
	private JTextField text_displayname;
	private JLabel label_version;
	private JTextField text_version;
	private JLabel label_name;
	private JTextField text_name;
	private JLabel label_displayartist;
	private JTextField text_displayartist;
	private JLabel label_type;
	private JComboBox select_type;
	private DefaultComboBoxModel select_type_model;

	public PanelItemBasics() {
		initFocusTraversal();
		initComponents();
		initLayout();
		update((Item)null);
	}
	
	@SuppressWarnings("unchecked")
	private void initFocusTraversal() {
		Set forwardKeys = new HashSet(getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
		forwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
		setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,forwardKeys);
	}

	public void update(Item item) {
		this.item = item;
		if (item == null) {
			text_displayname.setText("");
			text_version.setText("");
			text_name.setText("");
			text_displayartist.setText("");
			select_type.setSelectedItem(0);
		} else {
			text_displayname.setText(item.getDisplayname());
			text_version.setText(item.getVersion());
			text_name.setText(item.getName());
			text_displayartist.setText(item.getDisplay_artistname());
			select_type.setSelectedItem(item.getType());
		}
		//documentListener.saveStates();
	}


	private void initComponents() {
		Vector<JTextComponent> texts = new Vector<JTextComponent>();
		setBorder(new TitledBorder("Item Basics"));

		label_displayname = new JLabel("Display Name");

		text_displayname = new JTextField("");

		text_displayname.setName("text_displayname");
		map.put("text_displayname", text_displayname);
		texts.add(text_displayname);

		label_version = new JLabel("Version");

		text_version = new JTextField("");

		text_version.setName("text_version");
		map.put("text_version", text_version);
		texts.add(text_version);

		label_name = new JLabel("Name");

		text_name = new JTextField("");

		text_name.setName("text_name");
		map.put("text_name", text_name);
		texts.add(text_name);

		label_displayartist = new JLabel("Display Artist");

		text_displayartist = new JTextField("");

		text_displayartist.setName("text_displayartist");
		map.put("text_displayartist", text_displayartist);
		texts.add(text_displayartist);

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



		// Component: label_displayname
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
		gbl.setConstraints(label_displayname,gbc);
		add(label_displayname);

		// Component: text_displayname
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 50.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(text_displayname,gbc);
		add(text_displayname);

		// Component: label_version
		gbc.gridx = 2;
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
		gbl.setConstraints(label_version,gbc);
		add(label_version);

		// Component: text_version
		gbc.gridx = 3;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 50.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(text_version,gbc);
		add(text_version);

		// Component: label_name
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
		gbl.setConstraints(label_name,gbc);
		add(label_name);

		// Component: text_name
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 50.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(text_name,gbc);
		add(text_name);

		// Component: label_displayartist
		gbc.gridx = 2;
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
		gbl.setConstraints(label_displayartist,gbc);
		add(label_displayartist);

		// Component: text_displayartist
		gbc.gridx = 3;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 50.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(text_displayartist,gbc);
		add(text_displayartist);
		
		
		// Component: label_type
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
		gbl.setConstraints(label_type,gbc);
		add(label_type);

		// Component: select_type
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
		gbl.setConstraints(select_type,gbc);
		add(select_type);
		
	}

	// ----- action methods --------------------------------
	public void init_select_type_model() {
		select_type_model.removeAllElements();
		select_type_model.addElement("[not specified]");
		select_type_model.addElement("audio");
		select_type_model.addElement("video");
	}
	public void select_type_changed(int selected) {
		if (item==null) return;
		item.type((String)select_type.getSelectedItem());
		notifyChanges();
	}
	
	public void text_changed(JTextComponent text) {
		if (item == null) return;
		String t = text.getText();
		if (text == text_displayname) {
			item.displayname(t);
		}
		else if (text == text_version) {
			item.version(t);
		}
		else if (text == text_name) {
			item.name(t);
		}
		else if (text == text_displayartist) {
			item.display_artistname(t);
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
