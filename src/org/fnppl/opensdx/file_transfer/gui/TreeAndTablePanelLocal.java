package org.fnppl.opensdx.file_transfer.gui;

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
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.fnppl.opensdx.common.Util;
import org.fnppl.opensdx.gui.Dialogs;
import org.fnppl.opensdx.gui.helper.MyObservable;
import org.fnppl.opensdx.gui.helper.MyObserver;

public class TreeAndTablePanelLocal extends JPanel implements MyObservable, TreeAndTableChildrenGetter {

	private JSplitPane split;
	private JTree tree;
	private TreeAndTableNode oneRoot;
	private TreeAndTableNode root[];
	private DefaultTreeModel tree_model;
	private JTable table;
	private DefaultTableModel table_model;
	private JPanel buttons;
	private JButton buTransfer;
	private JButton buMkdir;
	private JButton buRemove;
	private JButton buRename;
	
	private Vector<File> currentFiles = null;

	private Comparator<TreeAndTableNode> compareNodes;
	
	public TreeAndTablePanelLocal() {
		compareNodes = new Comparator<TreeAndTableNode>() {
			public int compare(TreeAndTableNode n1, TreeAndTableNode n2) {
				return n1.toString().compareTo(n2.toString());
			}
		};
		initComponents();
		initLayout();
	}
	
	public Vector<TreeAndTableNode> getChildren(TreeAndTableNode node) {
		boolean showHidden = false;
		Vector<TreeAndTableNode> children = new Vector<TreeAndTableNode>();
		if (node == oneRoot) {
			for (int i=0;i<root.length;i++) {
				children.add(root[i]);
			}
			return children;
		} else {
			File file = (File) node.getUserObject();
			try {
				File[] list = file.listFiles();
				if (list == null)
					return children;
				for (File f : list) {
					try {
						if (f.isDirectory()) {
							if (showHidden || !f.isHidden()) {
								String name = f.getName();
								TreeAndTableNode n = new TreeAndTableNode(this, name, true, f);
								children.add(n);
							}
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			//sort
			Collections.sort(children, compareNodes);
			return children;
		}
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
		File[] roots = File.listRoots();
		int anzRoot = roots.length;
		System.out.println("roots count = "+anzRoot);
		root = new TreeAndTableNode[roots.length];
		for (int i=0;i<anzRoot;i++) {
			System.out.println("root["+i+"] = "+roots[i].getPath());
			root[i] = new TreeAndTableNode(this, roots[i].getPath(), true, roots[i]);
			
		}
		
		if (anzRoot == 1) {
			root[0].populate();
			oneRoot = root[0];
		} else {
			oneRoot = new TreeAndTableNode(this, "Computer", true, roots);
			oneRoot.populate();
		}
		
		tree_model = new DefaultTreeModel(oneRoot);
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
				updateTable(node);
				
				for (int i=0;i<colCount;i++) {
					colModel.getColumn(i).setPreferredWidth(width[i]);
					colModel.getColumn(i).setCellRenderer(render[i]);
				}
			}
		});
		
		table = new JTable();
		updateTable(null);
		
		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tree), new JScrollPane(table));
		split.setDividerLocation(230);
		buttons = new JPanel();
		buTransfer = new JButton("upload");
		
		buTransfer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				notifyChanges();
			}
		});
		buMkdir = new JButton("mkdir");
		buMkdir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File dir = getSelectedDir();
				if (dir == null) {
					Dialogs.showMessage("Please choose a parent directory first");
				} else {
					String name = Dialogs.showInputDialog("Make Directory", "Make a new Directory in:\n"+dir.getAbsolutePath()+"\n\nEnter new directory name:");
					if (name!=null) {
						TreePath path = tree.getSelectionPath();
							File f = new File(dir,name);
							boolean ok = f.mkdirs();
						if (!ok) {
							Dialogs.showMessage("Error, could not create directory:\n"+name+" in "+dir.getAbsolutePath());
						}
						refreshView(path);
					}
				}
			}
		});
		buRemove = new JButton("remove");
		buRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Vector<File> files = getSelectedFiles();
				if (files!=null && files.size()>0) {
					//remove files
					String msg = "Are you sure you want to remove the following files?";
					for (File f : files) {
						msg += "\n"+f.getName();
					}
					int q = Dialogs.showYES_NO_Dialog("Remove Files", msg);
					if (q == Dialogs.YES) {
						for (File f : files) {
							boolean ok = f.delete();
							if (!ok) {
								Dialogs.showMessage("Error, could not remove:\n"+f.getAbsolutePath());
							}
						}
						try {
							updateTable((TreeAndTableNode)tree.getSelectionPath().getLastPathComponent());
						} catch (Exception ex) {
							//ex.printStackTrace();
							try {
								updateTable(oneRoot);
							} catch (Exception ex2) {
								ex2.printStackTrace();
							}
						}
					}
				} else {
					File dir = getSelectedDir();
					if (dir !=null) {
						//remove dir
						String msg = "Are you sure you want to remove the following directory?";
						msg += "\n"+dir.getName();
						int q = Dialogs.showYES_NO_Dialog("Remove Directory", msg);
						if (q == Dialogs.YES) {
							TreePath path = tree.getSelectionPath();
							//recursive delete
							boolean ok = Util.deleteDirectory(dir);	
							if (!ok) {
								Dialogs.showMessage("Error, could not remove:\n"+dir.getAbsolutePath());
							}
							refreshView(path.getParentPath());
						}
					}
				}
			}
		});
		buRename = new JButton("rename");
		buRename.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Vector<File> files = getSelectedFiles();
				if (files==null || files.size()!=1) {
					Dialogs.showMessage("Please select one file");
					return;
				}
				
				File from = files.get(0);
				String name = Dialogs.showInputDialog("Rename file", "Please enter new filename for file\n"+from.getName()+"\n",from.getName());
				if (name!=null) {
					File to =new File(from.getParentFile(),name);
					boolean ok = from.renameTo(to);
					if (!ok) {
						Dialogs.showMessage("Error, could not rename:\n"+from.getAbsolutePath());
					}
					updateTable((TreeAndTableNode)tree.getSelectionPath().getLastPathComponent());
				}
				
			}
		});	
	}
	
	private void updateTable(TreeAndTableNode node) {
		if (table_model == null) {
			table_model = updateTableModel(node);
			RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(table_model) {
				private Comparator colSizeComparator = new Comparator<String>() {
					public int compare(String s1, String s2) {
						int i1 = Integer.parseInt(s1.substring(0,s1.length()-2).trim());
						int i2 = Integer.parseInt(s2.substring(0,s2.length()-2).trim());
						return i1-i2;
					}
				};
				@Override
				public Comparator<?> getComparator(int column) {
					if (column==2) {
						return colSizeComparator;
					}
					return super.getComparator(column);
				}
			};
			table.setRowSorter(sorter);
			table.setModel(table_model);
		} else {
			java.util.List<? extends SortKey> sortkeys = null;
			
			if (table.getRowSorter()!=null) {
				sortkeys = table.getRowSorter().getSortKeys();
			}
			
			table_model.setDataVector(buildTableModelData(node), header);
			
			if (table.getRowSorter()!=null) {
				table.getRowSorter().setSortKeys(sortkeys);
			}
		}
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
				updateTable((TreeAndTableNode)tree.getSelectionPath().getLastPathComponent());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	public void refreshView() {
		try {
			TreePath path = tree.getSelectionPath();
			
			TreeAndTableNode node;
			if (path==null) {
				node = oneRoot;
			} else {
				node = (TreeAndTableNode)path.getLastPathComponent();
			}
			node.populateAgain();
			tree.collapsePath(path);
			tree.expandPath(path);
			//tree.setSelectionPath(path);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			updateTable((TreeAndTableNode)tree.getSelectionPath().getLastPathComponent());
		} catch (Exception ex) {
			//ex.printStackTrace();
			try {
				updateTable(oneRoot);
			} catch (Exception ex2) {
				ex2.printStackTrace();
			}
		}
	}
	
	private String[] header = new String[] { "name", "type","size"};
	private DefaultTableModel updateTableModel(TreeAndTableNode node) {
		boolean showHidden = false;
		if (node == null) {
			return new DefaultTableModel(new String[0][header.length],header);
		}
		try {
			File file = (File) node.getUserObject();
			File[] list = file.listFiles();
			currentFiles = new Vector<File>();
			Vector<String[]> data = new Vector<String[]>();

			for (int i = 0; i < list.length; i++) {
				File f = list[i];
				String[] d  = new String[header.length];
				d[0] = f.getName();
				if (f.isDirectory()) {
//					d[2] = "";
//					d[1] = "[DIR]";
//					data.add(d);
				} else {
					if (showHidden || !f.isHidden()) {
						d[2] = (f.length() / 1000) + " kB";
						d[1] = "";
						int ind = d[0].lastIndexOf('.');
						if (ind > 0 && ind + 1 < d[0].length()) {
							d[1] = d[0].substring(ind + 1);
						}
						data.add(d);
						currentFiles.add(f);
					}
				}
			}
			String[][] tdata = new String[data.size()][header.length];
			for (int i=0;i<data.size();i++) {
				tdata[i] = data.get(i);
			}
			DefaultTableModel model = new DefaultTableModel(tdata, header) {
				public boolean isCellEditable(int row, int column) {
					return false;
				}
			};
			return model;
		} catch (Exception ex) {
			return new DefaultTableModel(new String[0][header.length],header);
		}
	}
	
	private String[][] buildTableModelData(TreeAndTableNode node) {
		boolean showHidden = false;
		String[][] tdata = new String[0][header.length];
		currentFiles = new Vector<File>();
		
		if (node == null || node == oneRoot) {
			return tdata;
		}
		try {
			File file = (File) node.getUserObject();
			Vector<String[]> data = new Vector<String[]>();
			if (file.exists()) {
				File[] list = file.listFiles();
				for (int i = 0; i < list.length; i++) {
					File f = list[i];
					String[] d  = new String[header.length];
					d[0] = f.getName();
					if (f.isDirectory()) {
	//					d[2] = "";
	//					d[1] = "[DIR]";
	//					data.add(d);
					} else {
						if (showHidden || !f.isHidden()) {
							d[2] = (f.length() / 1000) + " kB";
							d[1] = "";
							int ind = d[0].lastIndexOf('.');
							if (ind > 0 && ind + 1 < d[0].length()) {
								d[1] = d[0].substring(ind + 1);
							}
							data.add(d);
							currentFiles.add(f);
						}
					}
				}
			}
			tdata = new String[data.size()][header.length];
			for (int i=0;i<data.size();i++) {
				tdata[i] = data.get(i);
			}
			return tdata;
		} catch (Exception ex) {
			ex.printStackTrace();
			return new String[0][header.length];
		}
	}
	
	public File getSelectedDir() {
		try {
			File f = (File)((TreeAndTableNode)tree.getSelectionPath().getLastPathComponent()).getUserObject();
			return f;
		} catch (Exception ex)	{
			//ex.printStackTrace();
		}
		return null;
	}
	
	public void setSelectedDir(File dir) {
		Vector<String> parts = new Vector<String>();
		//System.out.println("adding: "+dir.getName());
		String name = dir.getName();
		if (name.length()==0) {
			name = dir.getAbsolutePath();
		}
		parts.add(name);
		File parent = dir.getParentFile();
		while (parent!=null) {
			name = parent.getName();
			if (name.length()==0) {
				name = parent.getAbsolutePath();
			}
			parts.add(0,name);
			//System.out.println("adding: "+name);
			parent = parent.getParentFile();
		}
		
		for (int j=0;j<parts.size();j++) {
			String searchNext = parts.get(j);
			//System.out.println("searching for: "+searchNext);
			boolean search = true;
			int rowCount = tree.getRowCount();
			for (int i=0;i<rowCount && search;i++) {
				TreePath cp = tree.getPathForRow(i);
				if (cp.getLastPathComponent().toString().equals(searchNext)) {
					//System.out.println("found: "+searchNext);
					//tree.setSelectionPath(cp);
					tree.setSelectionRow(i);
					tree.expandRow(i);
					search = false;
				}
			}
		}
		
	}
	
	public Vector<File> getSelectedFiles() {
		Vector<File> sel = new Vector<File>();
		int[] select = table.getSelectedRows();
		if (select!=null && select.length>0) {
			for (int i=0;i<select.length;i++) {
				sel.add(currentFiles.get(table.getRowSorter().convertRowIndexToModel(select[i])));
				//sel.add(currentFiles.get(select[i]));
			}
		}
		return sel;
	}
	
	private void initLayout() {
		this.setLayout(new BorderLayout());
		this.add(split, BorderLayout.CENTER);
		buttons.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttons.add(buMkdir);
		buttons.add(buRename);
		buttons.add(buRemove);
		buttons.add(buTransfer);
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
