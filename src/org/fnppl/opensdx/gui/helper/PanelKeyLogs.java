package org.fnppl.opensdx.gui.helper;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
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

import org.fnppl.opensdx.security.KeyLog;

import java.util.HashMap;
import java.util.Vector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
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
	private String[] columnNames = new String[] {"Key id from", "Key id to","date","action"};
	private String[][] tableData = new String[0][4];
	private Vector<KeyLog> selectedData = new Vector<KeyLog>();
	
	private DefaultTableModel tablemodel;
	private PanelIdentityDetails details;
	private Vector<KeyLog> keylogs = new Vector<KeyLog>();

	public PanelKeyLogs() {
		initComponents();
		initLayout();
	}

	public void list_keyid_selection_changed(String keyid_from, String keyid_to) {
		selectedData.removeAllElements();
		for (KeyLog k : keylogs) {
			if (    (keyid_from==null || keyid_from.equals("[ALL]") || k.getKeyIDFrom().equals(keyid_from))
				&&	(keyid_to==null   || keyid_to.equals("[ALL]")   || k.getKeyIDTo().equals(keyid_to))) {
				selectedData.add(k);
			}
		}
		tableData = new String[selectedData.size()][4];
		for (int i=0;i<selectedData.size();i++) {
			KeyLog k = selectedData.get(i);
			tableData[i][0] = k.getKeyIDFrom();
			tableData[i][1] = k.getKeyIDTo();
			tableData[i][2] = k.getActionDatetimeString();
			tableData[i][3] = k.getAction();
		}
		tablemodel = new DefaultTableModel(tableData,columnNames);
		table.setModel(tablemodel);
		table.setRowSorter(new TableRowSorter<DefaultTableModel>(tablemodel));
	}
	
	public void table_selection_changed(KeyLog keylog) {
		updateDetails(keylog);
	}
	

	public void updateKeyLogs(Vector<KeyLog> keylogs) {
		this.keylogs = keylogs;
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
	
		list_keyid_from = new JList();
		listmodel_keyid_from = new DefaultListModel();
		list_keyid_from.setModel(listmodel_keyid_from);
		list_keyid_from.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				list_keyid_selection_changed((String)list_keyid_from.getSelectedValue(),(String)list_keyid_to.getSelectedValue());
			}
		});
		
		list_keyid_to = new JList();
		listmodel_keyid_to = new DefaultListModel();
		list_keyid_to.setModel(listmodel_keyid_to);
		list_keyid_to.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				list_keyid_selection_changed((String)list_keyid_from.getSelectedValue(),(String)list_keyid_to.getSelectedValue());
			}
		});


		details = new PanelIdentityDetails();
	}
	
	public void initLayout() {
		JPanel p1 = new JPanel();
		p1.setLayout(new BorderLayout());
		p1.add(label_keyid_from,BorderLayout.NORTH);
		p1.add(new JScrollPane(list_keyid_from),BorderLayout.CENTER);
		
		JPanel p2 = new JPanel();
		p2.setLayout(new BorderLayout());
		p2.add(label_keyid_to,BorderLayout.NORTH);
		p2.add(new JScrollPane(list_keyid_to), BorderLayout.CENTER);
		
		JSplitPane pList = new JSplitPane(JSplitPane.VERTICAL_SPLIT, p1, p2);
		pList.setDividerLocation(0.5);
		
		JPanel p3 = new JPanel();
		p3.setLayout(new BorderLayout());
		p3.add(label_table,BorderLayout.NORTH);
		p3.add(new JScrollPane(table),BorderLayout.CENTER);
		
		JSplitPane pRight = new JSplitPane(JSplitPane.VERTICAL_SPLIT, p3, new JScrollPane(details));
		pRight.setDividerLocation(0.5);
		pRight.doLayout();
		
		JSplitPane all = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pList, pRight);
		all.setDividerLocation(0.3);
		all.doLayout();
		
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
		PanelKeyLogs p = new PanelKeyLogs();
		JFrame f = new JFrame("PanelKeyLogs");
		f.setContentPane(p);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(1024,768);
		f.setVisible(true);
	}
}
