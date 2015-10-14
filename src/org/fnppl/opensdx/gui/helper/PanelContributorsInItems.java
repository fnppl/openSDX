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
import org.fnppl.opensdx.common.Item;
import org.fnppl.opensdx.dmi.FeedGui;

import java.util.HashMap;
import java.util.Vector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PanelContributorsInItems extends JPanel implements MyObservable {

	//init fields
	private Bundle bundle = null;
	private Item item = null;

	private JList list_contributors;
	private DefaultListModel list_contributors_model;
	
	private JList list_contributors_bundle;
	private DefaultListModel list_contributors_bundle_model;
	
	private JButton bu_add;
	private JButton bu_addAll;
	private JButton bu_remove;
	private JButton bu_up;
	private JButton bu_down;
	

	public PanelContributorsInItems() {
		initComponents();
		initLayout();
		update(null,null);
	}

	public void update(Bundle bundle, Item item) {
		this.bundle = bundle;
		this.item = item;
		int sel = list_contributors_bundle.getSelectedIndex();
		updateContributorsList();
		if (bundle!=null && sel>=0 && sel < bundle.getContributorCount()) {
			list_contributors_bundle.setSelectedIndex(sel);
		}
	}

	private void updateContributorsList() {
		list_contributors_model.removeAllElements();
		list_contributors_bundle_model.removeAllElements();
		
		if (bundle==null) return;
		Vector<Contributor> contribs = bundle.getAllContributors();
		
		for (Contributor c : contribs) {
			list_contributors_bundle_model.addElement(c);
		}
		if (item==null) return;
		//System.out.println("item contrib count: "+item.getContributorCount());
		for (int i=0;i<item.getContributorCount();i++) {
			Contributor c = item.getContributor(i);
			list_contributors_model.addElement(c);
		}
	}


	private void initComponents() {
		setBorder(new TitledBorder("Contributors"));

		list_contributors = new JList();
		list_contributors_model = new DefaultListModel();
		list_contributors.setModel(list_contributors_model);

		
		list_contributors_bundle = new JList();
		list_contributors_bundle_model = new DefaultListModel();
		list_contributors_bundle.setModel(list_contributors_bundle_model);


		bu_add = new JButton("add");
		bu_add.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bu_add_clicked();
			}
		});
		
		bu_addAll = new JButton("add all");
		bu_addAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bu_addAll_clicked();
			}
		});

		bu_remove = new JButton("remove");
		bu_remove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bu_remove_clicked();
			}
		});
		
		bu_up = new JButton("<");
		bu_up.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bu_up_clicked();
			}
		});
		
		bu_down = new JButton(">");
		bu_down.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bu_down_clicked();
			}
		});
	}

	public void initLayout() {
		Dimension d = new Dimension(250,360);
		
		//contributors for item
		JScrollPane sList = new JScrollPane(list_contributors);
		sList.setPreferredSize(d);
		sList.setMinimumSize(d);
		sList.setMaximumSize(d);
	
		JPanel pForItem = new JPanel();
		pForItem.setLayout(new BorderLayout());
		pForItem.add(sList, BorderLayout.NORTH);
		JPanel pb2 = new JPanel();
		pb2.setLayout(new FlowLayout(FlowLayout.LEFT));
		//pb2.add(bu_add);
		pb2.add(bu_remove);
		pb2.add(bu_up);
		pb2.add(bu_down);
		pForItem.setBorder(new TitledBorder("List of Contributors"));
		pForItem.add(pb2, BorderLayout.CENTER);
		
		
		//contributors in bundle
		JScrollPane sListBundle = new JScrollPane(list_contributors_bundle);
		sListBundle.setPreferredSize(d);
		sListBundle.setMinimumSize(d);
		sListBundle.setMaximumSize(d);
	
		JPanel pInBundle = new JPanel();
		pInBundle.setLayout(new BorderLayout());
		pInBundle.add(sListBundle, BorderLayout.NORTH);
		JPanel pb = new JPanel();
		pb.setLayout(new FlowLayout(FlowLayout.LEFT));
		pb.add(bu_add);
		pb.add(bu_addAll);
		
		//pb.add(bu_remove);
		pInBundle.setBorder(new TitledBorder("List of all Contributors"));
		pInBundle.add(pb, BorderLayout.CENTER);
		
		
		//main layout
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbl.setConstraints(pForItem,gbc);
		add(pForItem);
		
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbl.setConstraints(pInBundle,gbc);
		add(pInBundle);
		
		JLabel filler = new JLabel();
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.weightx = 100.0;
		gbc.weighty = 100.0;
		gbl.setConstraints(filler,gbc);
		add(filler);
		

	}

	// ----- action methods --------------------------------
		
	public void bu_add_clicked() {
		if (bundle != null && item !=null) {
			Object[] sel = list_contributors_bundle.getSelectedValues();
			if (sel.length>0) {
				for (Object s : sel) {
					Contributor c = (Contributor)s;
					item.addContributor(c);
				}
				updateContributorsList();
				notifyChanges();
			}
		}
	}
	
	public void bu_addAll_clicked() {
		if (bundle != null && item !=null) {
			Object[] sel = list_contributors_bundle_model.toArray();
			if (sel.length>0) {
				for (Object s : sel) {
					Contributor c = (Contributor)s;
					item.addContributor(c);
				}
				updateContributorsList();
				notifyChanges();
			}
		}
	}
	
	public void bu_remove_clicked() {
		if (item != null) {
			Object[] sel = list_contributors.getSelectedValues();
			if (sel.length>0) {
				for (Object s : sel) {
					Contributor c = (Contributor)s;
					item.removeContributor(c);
				}
				updateContributorsList();
				notifyChanges();
			}
		}
	}
	
	public void bu_up_clicked() {
		if (item != null) {
			int selInd = list_contributors.getSelectedIndex(); 
			if (selInd==0) return;
			Object sel = list_contributors.getSelectedValue();
			if (sel!=null) {
				Contributor c = (Contributor)sel;
				item.moveContributorUp(c);
				updateContributorsList();
				notifyChanges();
				list_contributors.setSelectedIndex(selInd-1);
			}
		}
	}
	
	public void bu_down_clicked() {
		if (item != null) {
			int selInd = list_contributors.getSelectedIndex(); 
			if (selInd == list_contributors_model.getSize()-1) return;
			Object sel = list_contributors.getSelectedValue();
			if (sel!=null) {
				Contributor c = (Contributor)sel;
				item.moveContributorDown(c);
				updateContributorsList();
				notifyChanges();
				list_contributors.setSelectedIndex(selInd+1);
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

}
