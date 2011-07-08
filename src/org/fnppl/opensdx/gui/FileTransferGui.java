package org.fnppl.opensdx.gui;

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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.fnppl.opensdx.gui.helper.TreeAndTableBackend;
import org.fnppl.opensdx.gui.helper.TreeAndTableNode;
import org.fnppl.opensdx.gui.helper.TreeAndTablePanel;
import org.fnppl.opensdx.security.KeyApprovingStore;
import org.fnppl.opensdx.security.OSDXKey;

import sun.swing.BakedArrayList;

public class FileTransferGui extends JFrame {

	private Vector<Account> accounts = new Vector<Account>();
	private JPanel panelNorth;
	private JComboBox selectAccount;
	private JButton buConnect;
	private JButton buEdit;

	private DefaultComboBoxModel selectAccount_model;
	private TreeAndTablePanel panelLocal;
	private JPanel panelRemote;

	private JPanel panelSouth;
	private JTextArea log;

	public FileTransferGui() {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});
		buildUi();
	}
	
	private void exit() {
		//TODO close open connections
		
		this.dispose();
	}

	private void updateAccounts() {
		selectAccount_model.removeAllElements();
		selectAccount_model.addElement("Create new account ...");
		selectAccount_model.addElement("[separator]");
		for (Account a : accounts) {
			selectAccount_model.addElement(a.username + " :: " + a.keyid);
		}
		selectAccount.setModel(selectAccount_model);
	}

	private void initComponents() {
		panelNorth = new JPanel();
		panelNorth.setLayout(new FlowLayout(FlowLayout.LEFT));
		selectAccount_model = new DefaultComboBoxModel();
		selectAccount = new JComboBox();
		updateAccounts();
		selectAccount.setRenderer(new ListCellRenderer() {
			JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
			JLabel l = null;

			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				String str = (value == null) ? "" : value.toString();
				if (str.equals("[separator]")) {
					return separator;
				}
				if (l == null) {
					l = new JLabel();
					l.setOpaque(true);
					l.setBorder(new EmptyBorder(1, 1, 1, 1));
					l.setFont(list.getFont());
				}
				if (isSelected) {
					l.setBackground(list.getSelectionBackground());
					l.setForeground(list.getSelectionForeground());
				} else {
					l.setBackground(list.getBackground());
					l.setForeground(list.getForeground());
				}
				l.setText(str);
				return l;
			}
		});
		panelNorth.add(selectAccount);
		buConnect = new JButton("connect");
		buConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				button_connect_clicked();
			}
		});
		buEdit = new JButton("edit");
		buEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				button_edit_clicked();
			}
		});
		panelNorth.add(buConnect);
		panelNorth.add(buEdit);

		TreeAndTableBackend backendLocal = new TreeAndTableBackend() {
			public TreeAndTableNode getRootNode(TreeAndTablePanel main) {
				String dir = System.getProperty("user.home");
				return new TreeAndTableNode(main, dir, true, new File(dir));
			}

			public Vector<TreeAndTableNode> getChildren(TreeAndTableNode node,
					TreeAndTablePanel main) {
				Vector<TreeAndTableNode> children = new Vector<TreeAndTableNode>();
				File file = (File) node.getUserObject();
				File[] list = file.listFiles();
				if (list == null)
					return children;
				for (File f : list) {
					String name = f.getName();
					try {
						// boolean canPopulate = false;
						// if (f.isDirectory()) {
						// canPopulate = true;
						// }
						// TreeAndTableNode n = new TreeAndTableNode(main, name,
						// canPopulate, f);
						// children.add(n);
						if (f.isDirectory()) {
							TreeAndTableNode n = new TreeAndTableNode(main,
									name, true, f);
							children.add(n);
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				return children;
			}

			private String[] header = new String[] { "name", "type","size"};

			public DefaultTableModel updateTableModel(TreeAndTableNode node) {
				if (node == null) {
					return new DefaultTableModel(new String[0][header.length],header);
				}
				File file = (File) node.getUserObject();
				File[] list = file.listFiles();
				if (list == null) {
					return new DefaultTableModel(new String[0][header.length],header);
				}

				Vector<String[]> data = new Vector<String[]>();

				for (int i = 0; i < list.length; i++) {
					File f = list[i];
					if (f.isFile()) {
						String[] d  = new String[header.length];
						d[0] = f.getName();
						if (f.isDirectory()) {
							d[2] = "";
							d[1] = "[DIR]";
						} else {
							d[2] = (f.length() / 1000) + " kB";
							d[1] = "";
							int ind = d[0].lastIndexOf('.');
							if (ind > 0 && ind + 1 < d[0].length()) {
								d[1] = d[0].substring(ind + 1);
							}
						}
						data.add(d);
					}
				}
				String[][] tdata = new String[data.size()][header.length];
				for (int i=0;i<data.size();i++) {
					tdata[i] = data.get(i);
				}
				DefaultTableModel model = new DefaultTableModel(tdata, header);
				return model;
			}
		};
		panelLocal = new TreeAndTablePanel(backendLocal);
		panelLocal.setPreferredColumnWidth(1, 20);
		panelLocal.setPreferredColumnWidth(2, 30);

		TableCellRenderer leftRenderer = new TableCellRenderer() {
			private JLabel label;
			
			public Component getTableCellRendererComponent(JTable table,Object value, boolean isSelected, boolean hasFocus,	int row, int column) {
				if (label==null) {
					label = new JLabel();
					label.setOpaque(true);
					label.setBorder(new EmptyBorder(1, 1, 1, 1));
					label.setFont(table.getFont());
					label.setHorizontalAlignment(SwingConstants.LEFT);
				}
				String str = (value == null) ? "" : value.toString();
				if (isSelected) {
					label.setBackground(table.getSelectionBackground());
					label.setForeground(table.getSelectionForeground());
				} else {
					label.setBackground(table.getBackground());
					label.setForeground(table.getForeground());
				}
				label.setText(str);
				return label;
			}
		};
		TableCellRenderer centerRenderer = new TableCellRenderer() {
			private JLabel label;
			
			public Component getTableCellRendererComponent(JTable table,Object value, boolean isSelected, boolean hasFocus,	int row, int column) {
				if (label==null) {
					label = new JLabel();
					label.setOpaque(true);
					label.setBorder(new EmptyBorder(1, 1, 1, 1));
					label.setFont(table.getFont());
					label.setHorizontalAlignment(SwingConstants.CENTER);
				}
				String str = (value == null) ? "" : value.toString();
				if (isSelected) {
					label.setBackground(table.getSelectionBackground());
					label.setForeground(table.getSelectionForeground());
				} else {
					label.setBackground(table.getBackground());
					label.setForeground(table.getForeground());
				}
				label.setText(str);
				return label;
			}
		};
		TableCellRenderer rightRenderer = new TableCellRenderer() {
			private JLabel label;
			
			public Component getTableCellRendererComponent(JTable table,Object value, boolean isSelected, boolean hasFocus,	int row, int column) {
				if (label==null) {
					label = new JLabel();
					label.setOpaque(true);
					label.setBorder(new EmptyBorder(1, 1, 1, 1));
					label.setFont(table.getFont());
					label.setHorizontalAlignment(SwingConstants.RIGHT);
				}
				String str = (value == null) ? "" : value.toString();
				if (isSelected) {
					label.setBackground(table.getSelectionBackground());
					label.setForeground(table.getSelectionForeground());
				} else {
					label.setBackground(table.getBackground());
					label.setForeground(table.getForeground());
				}
				label.setText(str);
				return label;
			}
		};
		panelLocal.setColumnRenderer(0, leftRenderer);
		panelLocal.setColumnRenderer(1, centerRenderer);		
		panelLocal.setColumnRenderer(2, rightRenderer);

		
		panelRemote = new JPanel();

		panelSouth = new JPanel();
		log = new JTextArea();
		log.setEditable(false);

	}

	private void initLayout() {
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(panelNorth, BorderLayout.NORTH);
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				panelLocal, panelRemote);
		split.setDividerLocation(500);
		getContentPane().add(split, BorderLayout.CENTER);

		panelSouth.setLayout(new BorderLayout());
		panelSouth.add(log, BorderLayout.CENTER);
		log.setPreferredSize(new Dimension(200, 100));
		getContentPane().add(panelSouth, BorderLayout.SOUTH);

	}

	private void buildUi() {
		setTitle("openSDX :: FileTransfer GUI");
		setSize(1024, 768);
		initComponents();
		initLayout();
		Helper.centerMe(this, null);
	}

	public static void main(String[] args) {
		try {
			UIManager
					.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception ex) {
			System.out.println("Nimbus look & feel not available");
		}
		FileTransferGui gui = new FileTransferGui();
		gui.setVisible(true);
	}

	private void button_connect_clicked() {

	}

	private void button_edit_clicked() {

	}

	private class Account {
		public String username = null;
		public String keystore_filename = null;
		public String keyid = null;
		public KeyApprovingStore keystore = null;
		public OSDXKey key = null;
	}
}
