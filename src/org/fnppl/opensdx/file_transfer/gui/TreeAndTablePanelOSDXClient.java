package org.fnppl.opensdx.file_transfer.gui;

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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
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

import org.fnppl.opensdx.file_transfer.CommandResponseListener;
import org.fnppl.opensdx.file_transfer.OSDXFileTransferClient;
import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferCommand;
import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferListCommand;
import org.fnppl.opensdx.file_transfer.model.RemoteFile;
import org.fnppl.opensdx.gui.Dialogs;
import org.fnppl.opensdx.gui.helper.MyObservable;
import org.fnppl.opensdx.gui.helper.MyObserver;

public class TreeAndTablePanelOSDXClient extends JPanel implements MyObservable, TreeAndTableChildrenGetter {

	
	private long timeoutDuration = 4000;
	private JSplitPane split;
	private JTree tree;
	private TreeAndTableNode root;
	private DefaultTreeModel tree_model;
	private JTable table;
	private DefaultTableModel table_model;
	private Vector<RemoteFile> currentFiles = null;
	
	
	private JPanel buttons;
	private JButton buTransfer;
	private JButton buMkdir;
	private JButton buRemove;
	private JButton buRename;
	private JButton buRefresh;
	
	private Comparator<TreeAndTableNode> compareNodes;

	private OSDXFileTransferClient client = null;
	
	public TreeAndTablePanelOSDXClient(OSDXFileTransferClient client) {
		this.client = client;
		
		compareNodes = new Comparator<TreeAndTableNode>() {
			public int compare(TreeAndTableNode n1, TreeAndTableNode n2) {
				return n1.toString().compareTo(n2.toString());
			}
		};
		initComponents();
		initLayout();
		
		
	}
	
	public void closeConnection() {
		client.closeConnection();
	}
	
	private Vector<RemoteFile> nextList = null;
	private boolean hasAnswer = false;
	
	private Vector<RemoteFile> list(String absolutPath) {
		nextList = null;
		hasAnswer = false;
		System.out.println("list :: "+absolutPath);
		client.list(absolutPath,new CommandResponseListener() {
			public void onError(OSDXFileTransferCommand command, String msg) {
				//System.out.println("END OF LIST COMMAND :: ERROR");
				nextList = null;
				hasAnswer = true;
			}
			public void onStatusUpdate(OSDXFileTransferCommand command,long progress, long maxProgress, String msg) {}
			public void onSuccess(OSDXFileTransferCommand command) {
				//System.out.println("END OF LIST COMMAND :: SUCCESS");
				nextList = ((OSDXFileTransferListCommand)command).getList();
				hasAnswer = true;
			}
		});
		
		//block until answer or timeout
		long timeout = System.currentTimeMillis()+timeoutDuration;
		while (!hasAnswer && timeout > System.currentTimeMillis()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (!hasAnswer) {
			Dialogs.showMessage("Error: Timeout when requesting list for directory: "+absolutPath);
		}
		return nextList;
	}
	private Object objSync = new Object();
	
	public Vector<TreeAndTableNode> getChildren(TreeAndTableNode node) {
		
		RemoteFile file = (RemoteFile) node.getUserObject();
		System.out.println("getChildren :: "+file.getFilnameWithPath());
		try {
			Vector<RemoteFile> list = list(file.getFilnameWithPath());
			currentFiles = new Vector<RemoteFile>();
			if (list!=null) {
				Vector<TreeAndTableNode> children = new Vector<TreeAndTableNode>();
				//build list
				for (RemoteFile f : list) {
					String name = f.getName();
					try {
						if (f.isDirectory()) {
							System.out.println("adding directory: "+f.getPath()+"/"+name);
							TreeAndTableNode n = new TreeAndTableNode(this, name, true, f);
							children.add(n);
						} else {
							currentFiles.add(f);
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				//sort
				Collections.sort(children, compareNodes);
				return children;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
		
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
		RemoteFile f = client.getRoot();
		root = new TreeAndTableNode(this, f.getName(), true, f);
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
		buTransfer = new JButton("download");
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
						if (name.startsWith("/")) {
							Dialogs.showMessage("Directory name has to be relative.");
							return;
						}
						//RemoteFile f = new RemoteFile(dir.getFilnameWithPath(), name, 0, System.currentTimeMillis(), true);
						
						final TreePath path = tree.getSelectionPath();
						String newDir = dir.getFilnameWithPath();
						if (!newDir.endsWith("/")) {
							newDir += "/";
						}
						newDir += name;
						client.mkdir(newDir);
						//refreshView(path);					
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
							client.delete(f.getFilnameWithPath());
						}
//						try {
//							updateTable((TreeAndTableNode)tree.getSelectionPath().getLastPathComponent());
//						} catch (Exception ex) {
//							//ex.printStackTrace();
//							try {
//								updateTable(root);
//							} catch (Exception ex2) {
//								ex2.printStackTrace();
//							}
//						}
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
							tree.setSelectionPath(path.getParentPath());
							client.delete(dir.getFilnameWithPath());
//							refreshView(path.getParentPath());
						}
					}
				}
			}
		});
		buRename = new JButton("rename");
		buRename.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				RemoteFile from = null;
				boolean isDir = false;
				Vector<RemoteFile> files = getSelectedFiles();
				if (files == null || files.size()==0) {
					from = getSelectedDir();
					isDir = true;
				} else {
					if (files.size()==1) {
						from = files.get(0);
					}
				}
				if (from==null) {
					Dialogs.showMessage("Please select one file");
					return;
				}

				String name = Dialogs.showInputDialog("Rename file", "Please enter new filename for file\n"+from.getName()+"\n",from.getName());
				if (name!=null) {
					//RemoteFile to = new RemoteFile(from.getPath(), name, from.getLength(), from.getLastModified(), false);
					if (isDir) {
						tree.setSelectionPath(tree.getSelectionPath().getParentPath());
					}
					client.rename(from.getFilnameWithPath(), name);
//					updateTable((TreeAndTableNode)tree.getSelectionPath().getLastPathComponent());
				}
			}
		});
		
		buRefresh = new JButton("refresh");
		buRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshView(true);
			}
		});
	}
	
	public void refreshView(boolean updateTableFromServer) {
		refreshView(tree.getSelectionPath(),updateTableFromServer);
	}
	
	public void refreshView(final TreePath path, final boolean updateTableFromServer) {
		synchronized (objSync) {
			
		
		Thread t = new Thread() {
			public void run() {
				TreeAndTableNode node = root;
				if (path!=null) {
					node = (TreeAndTableNode)path.getLastPathComponent();
					System.out.println("refreshView :: "+Arrays.toString(path.getPath())); //TODO
				}
				try {
					if (updateTableFromServer) {
						node.files = null;
					}
					node.populateAgain();
					
					tree.collapsePath(path);
					tree.expandPath(path);
					tree.setSelectionPath(path);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				try {
					//updateTable((TreeAndTableNode)tree.getSelectionPath().getLastPathComponent());
					updateTable(node);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};
		t.start();
		
//		TreeAndTableNode node = root;
//		if (path!=null) {
//			node = (TreeAndTableNode)path.getLastPathComponent();
//			System.out.println("refreshView :: "+Arrays.toString(path.getPath())); //TODO
//		}
//		try {
//			node.populateAgain();
//			tree.collapsePath(path);
//			tree.expandPath(path);
//			//tree.setSelectionPath(path);
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//		try {
//			//updateTable((TreeAndTableNode)tree.getSelectionPath().getLastPathComponent());
//			updateTable(node);
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
		
		}
	}

	
	private String[] header = new String[] { "name", "type","size"};
	
	private DefaultTableModel updateTableModel(TreeAndTableNode node) {
		return new DefaultTableModel(buildTableModelData(node),header);
	}
	
	private String[][] buildTableModelData(TreeAndTableNode node) {
		if (node == null) {
			return new String[0][header.length];
		}
		try {
			RemoteFile file = (RemoteFile) node.getUserObject();
			if (node.files==null) {
				node.files = list(file.getFilnameWithPath());
			}
			if (currentFiles==null) {
				currentFiles = new Vector<RemoteFile>();
			} else {
				currentFiles.removeAllElements();
			}
			if (node.files==null || node.files.size()==0) {
				return new String[0][header.length];
			}
			Vector<String[]> data = new Vector<String[]>();
			for (RemoteFile f : node.files) {
				if (!f.isDirectory()) {
					currentFiles.add(f);	
					String[] d  = new String[header.length];
					d[0] = f.getName();
					d[2] = (f.getLength() / 1000) + " kB";
					d[1] = "";
					int ind = d[0].lastIndexOf('.');
					if (ind > 0 && ind + 1 < d[0].length()) {
						d[1] = d[0].substring(ind + 1);
					}
					data.add(d);
				}
			}
			String[][] tdata = new String[data.size()][header.length];
			for (int i=0;i<data.size();i++) {
				tdata[i] = data.get(i);
			}
			return tdata;
		} catch (Exception ex) {
			return new String[0][header.length];
		}
	}
	
	public void updateTable() {
		Thread t = new Thread() {
			public void run() {
				try {
					TreeAndTableNode node = (TreeAndTableNode)tree.getSelectionPath().getLastPathComponent();
					node.files = null; //fetch from server
					updateTable(node);
				} catch (Exception ex) {
					ex.printStackTrace();	
				}
			}
		};
		t.start();
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
	
	public RemoteFile getSelectedDir() {
		try {
			RemoteFile f = (RemoteFile)((TreeAndTableNode)tree.getSelectionPath().getLastPathComponent()).getUserObject();
			return f;
		} catch (Exception ex)	{
			//null pointer -> nothing selected
			//ex.printStackTrace();
		}
		return null;
	}
	
	public Vector<RemoteFile> getSelectedFiles() {
		Vector<RemoteFile> sel = new Vector<RemoteFile>();
		int[] select = table.getSelectedRows();
//		for (RemoteFile f : currentFiles) {
//			System.out.println("currentFiles::"+f.getFilnameWithPath()+", "+f.isDirectory());
//		}
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
		
		buttons.setLayout(new FlowLayout(FlowLayout.LEFT));
		buttons.add(buTransfer);
		buttons.add(buMkdir);
		buttons.add(buRename);
		buttons.add(buRemove);
		buttons.add(buRefresh);
		
		
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
