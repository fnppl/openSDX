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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.fnppl.opensdx.file_transfer.RemoteFile;
import org.fnppl.opensdx.file_transfer.RemoteFileSystem;
import org.fnppl.opensdx.gui.Dialogs;

public class TreeAndTablePanel extends JPanel implements MyObservable {

	private JSplitPane split;
	private JTree tree;
	private DefaultTreeModel tree_model;
	private JTable table;
	private DefaultTableModel table_model;
	private Vector<RemoteFile> currentFiles = null;
	
	private RemoteFileSystem fs = null;
	
	private JPanel buttons;
	private JButton buTransfer;
	private JButton buMkdir;
	private JButton buRemove;
	private JButton buRename;
	
	private boolean canUpload = true;
	
	public TreeAndTablePanel(RemoteFileSystem fs, boolean canUpload) {
		this.canUpload = canUpload;
		this.fs = fs;
		if (!fs.isConnected()) {
			try {
				fs.connect();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		initComponents();
		initLayout();
	}
	
	public void closeConnection() {
		fs.disconnect();
	}
	
	public Vector<TreeAndTableNode> getChildren(TreeAndTableNode node) {
		Vector<TreeAndTableNode> children = new Vector<TreeAndTableNode>();
		RemoteFile file = (RemoteFile) node.getUserObject();
		try {
			Vector<RemoteFile> list = fs.list(file);
			if (list == null)
				return children;
			for (RemoteFile f : list) {
				String name = f.getName();
				try {
					if (f.isDirectory()) {
						TreeAndTableNode n = new TreeAndTableNode(this, name, true, f);
						children.add(n);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return children;
	}
	
	public void setPreferredColumnWidth(int colNo, int width) {
		try {
			table.getColumnModel().getColumn(colNo).setPreferredWidth(width);
		} catch (Exception ex) {}
	}
	
	public void setColumnRenderer(int colNo, TableCellRenderer renderer) {
		try {
			table.getColumnModel().getColumn(colNo).setCellRenderer(renderer);
		} catch (Exception ex) {}
	}
	
	private void initComponents() {
		tree = new JTree();
		RemoteFile f = fs.getRoot();
		TreeAndTableNode root = new TreeAndTableNode(this, f.getName(), true, f);
		root.populate();
		tree_model = new DefaultTreeModel(root);
		tree.setModel(tree_model);
		
		TreeExpansionListener expListen = new TreeExpansionListener() {
			public void treeExpanded(TreeExpansionEvent event) {
				TreePath path = event.getPath();
				tree.setSelectionPath(path);
				TreeAndTableNode node = (TreeAndTableNode)path.getLastPathComponent();
				if (node.populate()) {
					tree_model.nodeStructureChanged(node);
				}
			}
			
			public void treeCollapsed(TreeExpansionEvent event) {
				//do nothing
			}
		};
		tree.addTreeExpansionListener(expListen);
		
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				TreeAndTableNode node = (TreeAndTableNode)e.getPath().getLastPathComponent();
				TableColumnModel colModel = table.getColumnModel();
				
				int colCount = colModel.getColumnCount();
				int[] width = new int[colCount];
				TableCellRenderer[] render = new TableCellRenderer[colCount];
				
				for (int i=0;i<colCount;i++) {
					width[i] = colModel.getColumn(i).getWidth();
					render[i] = colModel.getColumn(i).getCellRenderer();
				}
				table_model = updateTableModel(node);
				table.setModel(table_model);
				for (int i=0;i<colCount;i++) {
					colModel.getColumn(i).setPreferredWidth(width[i]);
					colModel.getColumn(i).setCellRenderer(render[i]);
				}
			}
		});
		
		table = new JTable();
		table_model = updateTableModel(null);
		table.setModel(table_model);
		
		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tree), new JScrollPane(table));
		split.setDividerLocation(230);
		buttons = new JPanel();
		if (canUpload) {
			buTransfer = new JButton("upload");
		} else {
			buTransfer = new JButton("download");
		}
		buTransfer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				notifyChanges();
			}
		});
		buMkdir = new JButton("mkdir");
		buMkdir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				RemoteFile dir = getSelectedDir();
				if (dir == null) {
					Dialogs.showMessage("Please choose a parent directory first");
				} else {
					String name = Dialogs.showInputDialog("Make Directory", "Make a new Directory in:\n"+dir.getFilnameWithPath()+"\n\nEnter new directory name:");
					if (name!=null) {
						RemoteFile f = new RemoteFile(dir.getFilnameWithPath(), name, 0, System.currentTimeMillis(), true);
						TreePath path = tree.getSelectionPath();
						fs.mkdir(f);
						refreshView(path);
					}
				}
			}
		});
		buRemove = new JButton("remove");
		buRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Vector<RemoteFile> files = getSelectedFiles();
				if (files!=null && files.size()>0) {
					//remove files
					String msg = "Are you sure you want to remove the following files?";
					for (RemoteFile f : files) {
						msg += "\n"+f.getName();
					}
					int q = Dialogs.showYES_NO_Dialog("Remove Files", msg);
					if (q == Dialogs.YES) {
						for (RemoteFile f : files) {
							fs.remove(f);
						}
						try {
							table_model = updateTableModel((TreeAndTableNode)tree.getSelectionPath().getLastPathComponent());
							table.setModel(table_model);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				} else {
					RemoteFile dir = getSelectedDir();
					if (dir !=null) {
						//remove dir
						String msg = "Are you sure you want to remove the following directory?";
						msg += "\n"+dir.getName();
						int q = Dialogs.showYES_NO_Dialog("Remove Directory", msg);
						if (q == Dialogs.YES) {
							TreePath path = tree.getSelectionPath();
							fs.remove(dir);
							refreshView(path.getParentPath());
						}
					}
				}
			}
		});
		buRename = new JButton("rename");
		buRename.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Vector<RemoteFile> files = getSelectedFiles();
				if (files==null || files.size()!=1) {
					Dialogs.showMessage("Please select one file");
					return;
				}
				
				RemoteFile from = files.get(0);
				String name = Dialogs.showInputDialog("Rename file", "Please enter new filename for file\n"+from.getName()+"\n",from.getName());
				if (name!=null) {
					RemoteFile to = new RemoteFile(from.getPath(), name, from.getLength(), from.getLastModified(), false);
					fs.rename(from, to);
					table_model = updateTableModel((TreeAndTableNode)tree.getSelectionPath().getLastPathComponent());
					table.setModel(table_model);
				}
				
			}
		});	
	}
	
	public void refreshView(TreePath path) {
		if (path!=null) {
			try {
				TreeAndTableNode node = (TreeAndTableNode)path.getLastPathComponent();
				node.populateAgain();
				tree.collapsePath(path);
				tree.expandPath(path);
				//tree.setSelectionPath(path);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			try {
				table_model = updateTableModel((TreeAndTableNode)tree.getSelectionPath().getLastPathComponent());
				table.setModel(table_model);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	private String[] header = new String[] { "name", "type","size"};
	private DefaultTableModel updateTableModel(TreeAndTableNode node) {
		if (node == null) {
			return new DefaultTableModel(new String[0][header.length],header);
		}
		try {
			RemoteFile file = (RemoteFile) node.getUserObject();
			Vector<RemoteFile> list = fs.list(file);
			currentFiles = new Vector<RemoteFile>();
			Vector<String[]> data = new Vector<String[]>();

			for (int i = 0; i < list.size(); i++) {
				RemoteFile f = list.get(i);
				String[] d  = new String[header.length];
				d[0] = f.getName();
				if (f.isDirectory()) {
//					d[2] = "";
//					d[1] = "[DIR]";
//					data.add(d);
				} else {
					d[2] = (f.getLength() / 1000) + " kB";
					d[1] = "";
					int ind = d[0].lastIndexOf('.');
					if (ind > 0 && ind + 1 < d[0].length()) {
						d[1] = d[0].substring(ind + 1);
					}
					data.add(d);
					currentFiles.add(f);
				}
			}
			String[][] tdata = new String[data.size()][header.length];
			for (int i=0;i<data.size();i++) {
				tdata[i] = data.get(i);
			}
			DefaultTableModel model = new DefaultTableModel(tdata, header);
			return model;
		} catch (Exception ex) {
			return new DefaultTableModel(new String[0][header.length],header);
		}
	}
	
	public RemoteFile getSelectedDir() {
		try {
			RemoteFile f = (RemoteFile)((TreeAndTableNode)tree.getSelectionPath().getLastPathComponent()).getUserObject();
			return f;
		} catch (Exception ex)	{
			ex.printStackTrace();
		}
		return null;
	}
	
	public Vector<RemoteFile> getSelectedFiles() {
		Vector<RemoteFile> sel = new Vector<RemoteFile>();
		int[] select = table.getSelectedRows();
		if (select!=null && select.length>0) {
			for (int i=0;i<select.length;i++) {
				//sel.add(currentFiles.get(table.getRowSorter().convertRowIndexToModel(select[i])));
				sel.add(currentFiles.get(select[i]));
			}
		}
		return sel;
	}
	
	private void initLayout() {
		this.setLayout(new BorderLayout());
		this.add(split, BorderLayout.CENTER);
		
		if (canUpload) {
			buttons.setLayout(new FlowLayout(FlowLayout.RIGHT));
			buttons.add(buMkdir);
			buttons.add(buRename);
			buttons.add(buRemove);
			buttons.add(buTransfer);
		} else {
			buttons.setLayout(new FlowLayout(FlowLayout.LEFT));
			buttons.add(buTransfer);
			buttons.add(buMkdir);
			buttons.add(buRename);
			buttons.add(buRemove);
		}
		this.add(buttons, BorderLayout.SOUTH);
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
