package org.fnppl.opensdx.gui.helper;
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

import org.fnppl.opensdx.common.Bundle;
import org.fnppl.opensdx.common.Contributor;
import org.fnppl.opensdx.common.Feed;
import org.fnppl.opensdx.common.IDs;
import org.fnppl.opensdx.common.InfoWWW;
import org.fnppl.opensdx.common.Item;
import org.fnppl.opensdx.dmi.FeedGui;

import java.util.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PanelContributorDetails extends JPanel implements MyObservable, MyObserver, TextChangeListener {

	//init fields
	private Contributor contributor = null;
	private Bundle bundle = null;
	private HashMap<String,JComponent> map = new HashMap<String, JComponent>();

	private JLabel label_name;
	private JTextField text_name;
	private JTextField text_year;
	private JLabel label_year;
	private JLabel label_type;
	private JComboBox select_type;
	private DefaultComboBoxModel select_type_model;
	private JCheckBox check_sublevel;
	private PanelIDs panel_ids;
	private PanelWWW panel_www;
	private JLabel label_filler;


	public PanelContributorDetails(Contributor contributor, Bundle bundle) {
		this.contributor = contributor;
		this.bundle = bundle;
		initFocusTraversal();
		initComponents();
		initLayout();
		HashSet<String> show;
		if (contributor!=null) {
			show = IDs.getRelevantIDs(contributor.getType());
		} else {
			show = IDs.getRelevantIDs(Contributor.TYPE_DJ);	
		}
		
		 
//		show.add("gvl");
//		show.add("contentauth");
//		show.add("finetunes");
//		show.add("our");
//		show.add("your");
		

		panel_ids.onlyShowFields(show);
		panel_ids.addObserver(this);
		panel_www.addObserver(this);
	}
	
	@SuppressWarnings("unchecked")
	private void initFocusTraversal() {
		Set forwardKeys = new HashSet(getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
		forwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
		setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,forwardKeys);
	}


	public void update(Contributor contributor, Bundle bundle) {
		this.contributor = contributor;
		this.bundle = bundle;
		if (contributor == null) {;
			text_name.setText("");
			text_year.setText("");
			select_type.setSelectedItem(0);
			check_sublevel.setSelected(false);
			panel_ids.update((IDs)null);
			panel_www.update((InfoWWW)null);
		} else {
			text_name.setText(contributor.getName());
			text_year.setText(contributor.getYear());
			select_type.setSelectedItem(contributor.getType());
			check_sublevel.setSelected(contributor.getOnSubLevelOnly());
			panel_ids.update(contributor.getIDs());
			panel_www.update(contributor.getWww());
		}
	}


	private void initComponents() {
		Vector<JTextComponent> texts = new Vector<JTextComponent>();
		setBorder(new TitledBorder("Contributor Details"));

		label_name = new JLabel("Name");

		text_name = new JTextField("");

		text_name.setName("text_name");
		map.put("text_name", text_name);
		texts.add(text_name);

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

		label_year = new JLabel("Year");

		text_year = new JTextField("");

		text_year.setName("text_year");
		map.put("text_year", text_year);
		texts.add(text_year);		

		check_sublevel = new JCheckBox("only on Sublevel");
		map.put("check_sublevel", check_sublevel);
		check_sublevel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				check_sublevel_changed(check_sublevel.isSelected());
			}
		});

		panel_ids = new PanelIDs(null);
		panel_www = new PanelWWW(null);

		label_filler = new JLabel("");

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

		Container spacer0 = new Container();


		// Component: label_name
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
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_name,gbc);
		add(label_name);

		// Component: text_name
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
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(text_name,gbc);
		add(text_name);

		// Component: label_type
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
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_type,gbc);
		add(label_type);

		// Component: select_type
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
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(select_type,gbc);
		add(select_type);

		// Component: label_year
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
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_year,gbc);
		add(label_year);

		// Component: text_year
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
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(text_year,gbc);
		add(text_year);	

		// Component: spacer0
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
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(spacer0,gbc);
		add(spacer0);

		// Component: check_sublevel
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
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(check_sublevel,gbc);
		add(check_sublevel);

		// Component: panel_ids
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weightx = 50.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(panel_ids,gbc);
		add(panel_ids);

		// Component: panel_www
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weightx = 50.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(panel_www,gbc);
		add(panel_www);

		// Component: label_filler
		gbc.gridx = 0;
		gbc.gridy = 6;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 100.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_filler,gbc);
		add(label_filler);
	}
	// ----- action methods --------------------------------
	public void init_select_type_model() {
		select_type_model.removeAllElements();
		select_type_model.addElement("[no type]");
		for (String t : Contributor.TYPES) {
			select_type_model.addElement(t);
		}
	}
	public void select_type_changed(int selected) {
		if (contributor==null) return;
		String oldType = contributor.getType();
		contributor.type((String)select_type_model.getSelectedItem());
		if (bundle!=null) {
			bundle.updateItemsContributors(contributor, contributor.getName(), oldType);
		}
		HashSet<String> show;
		if (contributor!=null) {
			show = IDs.getRelevantIDs(contributor.getType());
		} else {
			show = IDs.getRelevantIDs(Contributor.TYPE_DJ);	
		}
		panel_ids.onlyShowFields(show);
		
		notifyChanges();
		
	}

	public void check_sublevel_changed(boolean selected) {
		if (contributor==null) return;
		contributor.on_sublevel_only(check_sublevel.isSelected());
		notifyChanges();
	}

	public void text_changed(JTextComponent text) {
		if (contributor==null) return;
		String t = text.getText();
		if (text == text_name) {
			String oldName = contributor.getName();
			if (!t.equals(oldName)) {
				contributor.name(t);
				if (bundle!=null) {
					bundle.updateItemsContributors(contributor, oldName, contributor.getType());
				}
			}
		}
		else if (text == text_year) {
			if (t==null || t.length()==0) {
				contributor.year(null);
			} else {
				contributor.year(t);
			}
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


	public void notifyChange(MyObservable changedIn) {
		if (changedIn == panel_www) {
			if (contributor!=null && contributor.getWww()==null) {
				contributor.www(panel_www.getWWW());
			}
		}
		if (bundle!=null) {
			bundle.updateItemsContributors(contributor, contributor.getName(), contributor.getType());
		}
		notifyChanges();
	}
}
