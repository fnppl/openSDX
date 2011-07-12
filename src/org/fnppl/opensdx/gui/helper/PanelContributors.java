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
import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.fnppl.opensdx.common.Bundle;
import org.fnppl.opensdx.common.Contributor;
import org.fnppl.opensdx.dmi.FeedGui;

import java.util.HashMap;
import java.util.Vector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PanelContributors extends JPanel implements MyObservable {

	//init fields
	private Bundle bundle = null;
	private DocumentChangeListener documentListener;
	private KeyAdapter keyAdapter;
	private HashMap<String,JComponent> map = new HashMap<String, JComponent>();

	private JLabel label_list_contributors;
	private JLabel label_v_filler;
	private JList list_contributors;
	private DefaultListModel list_contributors_model;
	private PanelContributorDetails panel_contributor_details;
	private JButton bu_add;
	private JButton bu_remove;
	private JLabel label_fillerbu;
	private JLabel label_h1_filler;
	private JLabel label_h2_filler;


	public PanelContributors(Bundle bundle) {
		this.bundle = bundle;
		initKeyAdapter();
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
	}
	
	private void updateContributorsList() {
		//TODO
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
		Vector<JTextComponent> texts = new Vector<JTextComponent>();
		setBorder(new TitledBorder("Contributors"));

		label_list_contributors = new JLabel("List of Contributors");

		label_v_filler = new JLabel("");

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

		label_fillerbu = new JLabel("");

		label_h1_filler = new JLabel("");

		label_h2_filler = new JLabel("");

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
public void initLayout() {
	GridBagLayout gbl = new GridBagLayout();
	setLayout(gbl);
	GridBagConstraints gbc = new GridBagConstraints();

	Container spacer0 = new Container();


	// Component: label_list_contributors
	gbc.gridx = 0;
	gbc.gridy = 0;
	gbc.gridwidth = 3;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(label_list_contributors,gbc);
	add(label_list_contributors);

	// Component: spacer0
	gbc.gridx = 3;
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
	gbl.setConstraints(spacer0,gbc);
	add(spacer0);

	// Component: label_v_filler
	gbc.gridx = 4;
	gbc.gridy = 0;
	gbc.gridwidth = 1;
	gbc.gridheight = 5;
	gbc.weightx = 50.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(label_v_filler,gbc);
	add(label_v_filler);

	// Component: list_contributors
	gbc.gridx = 0;
	gbc.gridy = 1;
	gbc.gridwidth = 3;
	gbc.gridheight = 1;
	gbc.weightx = 50.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(list_contributors,gbc);
	add(list_contributors);

	// Component: panel_contributor_detaisl
	gbc.gridx = 3;
	gbc.gridy = 1;
	gbc.gridwidth = 1;
	gbc.gridheight = 3;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(panel_contributor_details,gbc);
	add(panel_contributor_details);

	// Component: bu_add
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
	gbl.setConstraints(bu_add,gbc);
	add(bu_add);

	// Component: bu_remove
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
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(bu_remove,gbc);
	add(bu_remove);

	// Component: label_fillerbu
	gbc.gridx = 2;
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
	gbl.setConstraints(label_fillerbu,gbc);
	add(label_fillerbu);

	// Component: label_h1_filler
	gbc.gridx = 0;
	gbc.gridy = 3;
	gbc.gridwidth = 3;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 50.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(label_h1_filler,gbc);
	add(label_h1_filler);

	// Component: label_h2_filler
	gbc.gridx = 0;
	gbc.gridy = 4;
	gbc.gridwidth = 4;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 50.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(label_h2_filler,gbc);
	add(label_h2_filler);
		JLabel filler = new JLabel();
}

// ----- action methods --------------------------------
	public void init_list_contributors_model() {
		//TODO
	}
	public void list_contributors_changed(int selected) {
		//TODO
	}
	public void bu_add_clicked() {
		//TODO
	}
	public void bu_remove_clicked() {
		//TODO
	}
	public void text_changed(JTextComponent text) {
		//TODO
		String t = text.getText();
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
