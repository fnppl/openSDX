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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Color;
import java.awt.LayoutManager;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.JTextComponent;

import org.fnppl.opensdx.gui.Dialogs;
import org.fnppl.opensdx.gui.SecurityMainFrame;
import org.fnppl.opensdx.security.KeyApprovingStore;
import org.fnppl.opensdx.security.KeyLog;

import java.util.HashMap;
import java.util.Vector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PanelKeyLogs extends JPanel {

	//init fields
	private JLabel label_keyid_from;
	private JLabel label_keyid_to;
	private JLabel label_table;
	private JList list_keyid_from;
	private DefaultListModel listmodel_keyid_from;
	private JList list_keyid_to;
	private DefaultListModel listmodel_keyid_to;
	private JTable table;
	private String[] columnNames = new String[] {"Key id from", "Key id to","date","action", "email"};
	private String[][] tableData = new String[0][5];
	private Vector<KeyLog> selectedData = new Vector<KeyLog>();
	private JButton bu_remove;
	
	private DefaultTableModel tablemodel;
	private PanelIdentityDetails details;
	private Vector<KeyLog> keylogs = new Vector<KeyLog>();
	private KeyApprovingStore currentKeyStore = null;
	private SecurityMainFrame main_gui = null;
	
	public PanelKeyLogs(SecurityMainFrame main_gui) {
		this.main_gui = main_gui;
		initComponents();
		initLayout();
	}

	public void list_keyid_selection_changed(Vector<String> keyid_from,Vector<String> keyid_to) {
		selectedData.removeAllElements();
		for (KeyLog k : keylogs) {
			if (    (keyid_from==null || keyid_from.contains("[ALL]") || keyid_from.contains(k.getKeyIDFrom()))
				&&	(keyid_to==null   || keyid_to.contains("[ALL]")   || keyid_to.contains(k.getKeyIDTo()))) {
				selectedData.add(k);
			}
		}
		tableData = new String[selectedData.size()][5];
		for (int i=0;i<selectedData.size();i++) {
			KeyLog k = selectedData.get(i);
			tableData[i][0] = k.getKeyIDFrom();
			tableData[i][1] = k.getKeyIDTo();
			tableData[i][2] = k.getActionDatetimeString();
			tableData[i][3] = k.getAction();
			if (k.getIdentity()!=null) {
				tableData[i][4] = k.getIdentity().getEmail();
			} else {
				tableData[i][4] = "[unknown]";
			}
		}
		tablemodel = new DefaultTableModel(tableData,columnNames);
		table.setModel(tablemodel);
		table.setRowSorter(new TableRowSorter<DefaultTableModel>(tablemodel));
	}
	
	public void table_selection_changed(KeyLog keylog) {
		updateDetails(keylog);
	}
	
	public void removeSelectedKeylogs() {
		int[] sel = table.getSelectedRows();
		if (sel==null) return;
		int ans = Dialogs.showYES_NO_Dialog("Remove Keylogs", "Are you sure you want to remove the selected keylogs?");
		if (ans != Dialogs.YES) return;
		
		for (int i=0;i<sel.length;i++) {
			int index = table.getRowSorter().convertRowIndexToModel(sel[i]);
			KeyLog log = selectedData.get(index);
			currentKeyStore.removeKeyLog(log);
		}
		main_gui.update();
	}
	

	public void updateKeyLogs(KeyApprovingStore currentKeyStore) {
		this.currentKeyStore = currentKeyStore;
		this.keylogs = currentKeyStore.getKeyLogs();
		listmodel_keyid_from.removeAllElements();
		listmodel_keyid_from.addElement("[ALL]");
		listmodel_keyid_to.removeAllElements();
		listmodel_keyid_to.addElement("[ALL]");
		
		for (KeyLog l : keylogs) {
			String id_from = l.getKeyIDFrom();
			String id_to = l.getKeyIDTo();
			if (!listmodel_keyid_from.contains(id_from)) {
				listmodel_keyid_from.addElement(id_from);	
			}
			if (!listmodel_keyid_to.contains(id_to)) {
				listmodel_keyid_to.addElement(id_to);	
			}
		}
		updateDetails(null);
		list_keyid_from.setSelectedIndex(0);
		list_keyid_to.setSelectedIndex(0);
	}
	
	public void updateDetails(KeyLog keylog) {
		details.updateDetails(keylog);
	}
	
	private void initComponents() {
		label_keyid_from = new JLabel("Key ID from");
		label_keyid_to = new JLabel("Key ID to");
		label_table = new JLabel("Selected Keylogs");
		
		table = new JTable();
		table.setRowSorter(new TableRowSorter<DefaultTableModel>(tablemodel));
		tablemodel = new DefaultTableModel(tableData, columnNames);		
		table.setModel(tablemodel);
		table.setRowSorter(new TableRowSorter<DefaultTableModel>(tablemodel));
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				int sel = table.getSelectedRow();
				KeyLog selected = null;
				if (sel>=0 && sel<selectedData.size()) {
					selected = selectedData.get(sel);
				}
				table_selection_changed(selected);
			}
		});
		bu_remove = new JButton("remove selected keylogs");
		bu_remove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeSelectedKeylogs();
			}
		});
		
		list_keyid_from = new JList();
		listmodel_keyid_from = new DefaultListModel();
		list_keyid_from.setModel(listmodel_keyid_from);
		list_keyid_from.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				Vector<String> selFrom = new Vector<String>();
				int[] selF = list_keyid_from.getSelectedIndices();
				for (int i=0;i<selF.length;i++) {
					//System.out.println("from "+i+" :: "+(String)listmodel_keyid_from.get(selF[i]));
					selFrom.add((String)listmodel_keyid_from.get(selF[i]));
				}
				Vector<String> selTo = new Vector<String>();
				int[] selT = list_keyid_to.getSelectedIndices();
				for (int i=0;i<selT.length;i++) {
					//System.out.println("to "+i+" :: "+(String)listmodel_keyid_to.get(selT[i]));
					selTo.add((String)listmodel_keyid_to.get(selT[i]));
				}
				list_keyid_selection_changed(selFrom, selTo);
			}
		});
		
		list_keyid_to = new JList();
		listmodel_keyid_to = new DefaultListModel();
		list_keyid_to.setModel(listmodel_keyid_to);
		list_keyid_to.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				Vector<String> selFrom = new Vector<String>();
				int[] selF = list_keyid_from.getSelectedIndices();
				for (int i=0;i<selF.length;i++) {
					selFrom.add((String)listmodel_keyid_from.get(selF[i]));
				}
				Vector<String> selTo = new Vector<String>();
				int[] selT = list_keyid_to.getSelectedIndices();
				for (int i=0;i<selT.length;i++) {
					selTo.add((String)listmodel_keyid_to.get(selT[i]));
				}
				list_keyid_selection_changed(selFrom, selTo);
			}
		});


		details = new PanelIdentityDetails();
	}
	
	public void initLayout() {
		final JPanel p1 = new JPanel();
		p1.setLayout(new BorderLayout());
		p1.add(label_keyid_from,BorderLayout.NORTH);
		p1.add(new JScrollPane(list_keyid_from),BorderLayout.CENTER);
		
		final JPanel p2 = new JPanel();
		p2.setLayout(new BorderLayout());
		p2.add(label_keyid_to,BorderLayout.NORTH);
		p2.add(new JScrollPane(list_keyid_to), BorderLayout.CENTER);
		
		final JSplitPane pList = new JSplitPane(JSplitPane.VERTICAL_SPLIT, p1, p2);
		Dimension minimumSize = new Dimension(120, 50);
	    p1.setMinimumSize(minimumSize);
	    p2.setMinimumSize(minimumSize);
		
		JPanel p3 = new JPanel();
		p3.setLayout(new BorderLayout());
		p3.add(label_table,BorderLayout.NORTH);
		p3.add(new JScrollPane(table),BorderLayout.CENTER);
		JPanel pButtons = new JPanel();
		FlowLayout lf = new FlowLayout();
		lf.setAlignment(FlowLayout.LEFT);
		pButtons.setLayout(lf);
		pButtons.add(bu_remove);
		p3.add(pButtons, BorderLayout.SOUTH);
		p3.setMinimumSize(minimumSize);
		
		JPanel pDetails = new JPanel();
		pDetails.setMinimumSize(minimumSize);
		pDetails.setLayout(new BorderLayout());
		pDetails.add(new JScrollPane(details), BorderLayout.CENTER);
		JSplitPane pRight = new JSplitPane(JSplitPane.VERTICAL_SPLIT, p3, pDetails);
		pRight.setDividerLocation(300);
		JSplitPane all = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pList, pRight);
		
		
		this.setLayout(new BorderLayout());
		this.add(all,BorderLayout.CENTER);
		
		
//	GridBagLayout gbl = new GridBagLayout();
//	setLayout(gbl);
//	GridBagConstraints gbc = new GridBagConstraints();
//
//
//
//	// Component: label_keyid_from
//	gbc.gridx = 0;
//	gbc.gridy = 0;
//	gbc.gridwidth = 1;
//	gbc.gridheight = 1;
//	gbc.weightx = 10.0;
//	gbc.weighty = 0.0;
//	gbc.anchor = GridBagConstraints.CENTER;
//	gbc.fill = GridBagConstraints.BOTH;
//	gbc.ipadx = 0;
//	gbc.ipady = 0;
//	gbc.insets = new Insets(5,5,5,5);
//	gbl.setConstraints(label_keyid_from,gbc);
//	add(label_keyid_from);
//
//	// Component: label_keyid_to
//	gbc.gridx = 1;
//	gbc.gridy = 0;
//	gbc.gridwidth = 1;
//	gbc.gridheight = 1;
//	gbc.weightx = 10.0;
//	gbc.weighty = 0.0;
//	gbc.anchor = GridBagConstraints.CENTER;
//	gbc.fill = GridBagConstraints.BOTH;
//	gbc.ipadx = 0;
//	gbc.ipady = 0;
//	gbc.insets = new Insets(5,5,5,5);
//	gbl.setConstraints(label_keyid_to,gbc);
//	add(label_keyid_to);
//
//	// Component: label_table
//	gbc.gridx = 2;
//	gbc.gridy = 0;
//	gbc.gridwidth = 1;
//	gbc.gridheight = 1;
//	gbc.weightx = 80.0;
//	gbc.weighty = 0.0;
//	gbc.anchor = GridBagConstraints.CENTER;
//	gbc.fill = GridBagConstraints.BOTH;
//	gbc.ipadx = 0;
//	gbc.ipady = 0;
//	gbc.insets = new Insets(5,5,5,5);
//	gbl.setConstraints(label_table,gbc);
//	add(label_table);
//
//	// Component: list_keyid_from
//	gbc.gridx = 0;
//	gbc.gridy = 1;
//	gbc.gridwidth = 1;
//	gbc.gridheight = 1;
//	gbc.weightx = 0.0;
//	gbc.weighty = 50.0;
//	gbc.anchor = GridBagConstraints.CENTER;
//	gbc.fill = GridBagConstraints.BOTH;
//	gbc.ipadx = 0;
//	gbc.ipady = 0;
//	gbc.insets = new Insets(5,5,5,5);
//	gbl.setConstraints(list_keyid_from,gbc);
//	add(list_keyid_from);
//
//	// Component: list_keyid_to
//	gbc.gridx = 1;
//	gbc.gridy = 1;
//	gbc.gridwidth = 1;
//	gbc.gridheight = 1;
//	gbc.weightx = 0.0;
//	gbc.weighty = 50.0;
//	gbc.anchor = GridBagConstraints.CENTER;
//	gbc.fill = GridBagConstraints.BOTH;
//	gbc.ipadx = 0;
//	gbc.ipady = 0;
//	gbc.insets = new Insets(5,5,5,5);
//	gbl.setConstraints(list_keyid_to,gbc);
//	add(list_keyid_to);
//
//	// Component: table
//	gbc.gridx = 2;
//	gbc.gridy = 1;
//	gbc.gridwidth = 1;
//	gbc.gridheight = 1;
//	gbc.weightx = 0.0;
//	gbc.weighty = 50.0;
//	gbc.anchor = GridBagConstraints.CENTER;
//	gbc.fill = GridBagConstraints.BOTH;
//	gbc.ipadx = 0;
//	gbc.ipady = 0;
//	gbc.insets = new Insets(5,5,5,5);
//	JScrollPane scrolltable = new JScrollPane(table);
//	gbl.setConstraints(scrolltable,gbc);
//	add(scrolltable);
//
//	// Component: details
//	gbc.gridx = 0;
//	gbc.gridy = 2;
//	gbc.gridwidth = 3;
//	gbc.gridheight = 1;
//	gbc.weightx = 100.0;
//	gbc.weighty = 50.0;
//	gbc.anchor = GridBagConstraints.CENTER;
//	gbc.fill = GridBagConstraints.BOTH;
//	gbc.ipadx = 0;
//	gbc.ipady = 0;
//	gbc.insets = new Insets(5,5,5,5);
//	JScrollPane scroll = new JScrollPane(details);
//	gbl.setConstraints(scroll,gbc);
//	add(scroll);
	}
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch(Exception ex){
			System.out.println("Nimbus look & feel not available");
	}
		PanelKeyLogs p = new PanelKeyLogs(null);
		JFrame f = new JFrame("PanelKeyLogs");
		f.setContentPane(p);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(1024,768);
		f.setVisible(true);
	}
}
