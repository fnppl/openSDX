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
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Color;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.fnppl.opensdx.common.Bundle;
import org.fnppl.opensdx.common.Contributor;
import org.fnppl.opensdx.common.IDs;
import org.fnppl.opensdx.dmi.FeedGui;

import java.util.HashMap;
import java.util.Vector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PanelContributors extends JPanel implements MyObservable, MyObserver {

	//init fields
	private Bundle bundle = null;
	private HashMap<String,JComponent> map = new HashMap<String, JComponent>();

	private JList list_contributors;
	private DefaultListModel list_contributors_model;
	private PanelContributorDetails panel_contributor_details;
	private JButton bu_add;
	private JButton bu_remove;


	public PanelContributors(Bundle bundle) {
		this.bundle = bundle;
		initComponents();
		initLayout();
	}

	public void update(Bundle bundle) {
		this.bundle = bundle;
		updateContributorsList();
		int sel = list_contributors.getSelectedIndex();
		Contributor c = null;		
		if (bundle!=null && sel>=0 && sel < bundle.getContributorCount()) {
			c = bundle.getContributor(sel);
		}
		panel_contributor_details.update(c);
		if (c==null) {
			panel_contributor_details.setVisible(false);
		} else {
			panel_contributor_details.setVisible(true);
		}
	}

	private void updateContributorsList() {
		list_contributors_model.removeAllElements();
		if (bundle==null || bundle.getContributorCount()==0) return;

		for (int i=0;i<bundle.getContributorCount();i++) {
			Contributor c = bundle.getContributor(i);
			list_contributors_model.addElement(c.getName()+" ("+c.getType()+")");
		}
	}


	private void initComponents() {
		setBorder(new TitledBorder("Contributors"));

		list_contributors = new JList();
		
		list_contributors_model = new DefaultListModel();
		list_contributors.setModel(list_contributors_model);
		init_list_contributors_model();
		map.put("list_contributors", list_contributors);
		list_contributors.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				list_contributors_changed(list_contributors.getSelectedIndex());
			}
		});

		panel_contributor_details = new PanelContributorDetails(null);
		panel_contributor_details.addObserver(this);

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

	}


	public JComponent getComponent(String name) {
		return map.get(name);
	}
	
	public void initLayout() {

		Dimension d = new Dimension(250,360);
		JScrollPane sList = new JScrollPane(list_contributors);
		
		sList.setPreferredSize(d);
		sList.setMinimumSize(d);
		sList.setMaximumSize(d);
	
		
		this.setLayout(new BorderLayout());
		JPanel west = new JPanel();
		west.setLayout(new BorderLayout());
		west.add(sList, BorderLayout.NORTH);
		JPanel pb = new JPanel();
		pb.setLayout(new FlowLayout(FlowLayout.LEFT));
		pb.add(bu_add);
		pb.add(bu_remove);
		west.setBorder(new TitledBorder("List of Contributors"));
		west.add(pb, BorderLayout.CENTER);
		
		this.add(west, BorderLayout.WEST);
		int h = (int)panel_contributor_details.getPreferredSize().getHeight()+1;
		panel_contributor_details.setPreferredSize(new Dimension(400,h));
		panel_contributor_details.setMinimumSize(new Dimension(200,h));
		panel_contributor_details.setMaximumSize(new Dimension(500,h));
		this.add(panel_contributor_details,BorderLayout.CENTER);
		

	}

	// ----- action methods --------------------------------
	public void init_list_contributors_model() {

	}
	public void list_contributors_changed(int selected) {
		if (bundle==null) return;
		Contributor c = null;
		if (selected>=0 && selected < bundle.getContributorCount()) {
			c = bundle.getContributor(selected);
		}
		panel_contributor_details.update(c);
		if (c==null) {
			panel_contributor_details.setVisible(false);
		} else {
			panel_contributor_details.setVisible(true);
		}
	}
	public void bu_add_clicked() {
		if (bundle != null) {
			Contributor c = Contributor.make("new contributor", "[no type]", IDs.make());
			bundle.addContributor(c);
			updateContributorsList();
			list_contributors.setSelectedIndex(list_contributors.getModel().getSize() - 1);
			notifyChanges();
		}
	}
	public void bu_remove_clicked() {
		if (bundle != null) {
			int sel = list_contributors.getSelectedIndex();
			if (sel >= 0 && sel < bundle.getContributorCount()) {
				bundle.removeContributor(sel);
				updateContributorsList();
				notifyChanges();
			}
		}
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
		int sel = list_contributors.getSelectedIndex();
		updateContributorsList();
		if (sel>=0) {
			list_contributors.setSelectedIndex(sel);
		}
		notifyChanges();
	}
}
