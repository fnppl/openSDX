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
import java.util.Vector;
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

public class TreeAndTablePanel extends JPanel {

	private JSplitPane split;
	private JTree tree;
	private DefaultTreeModel tree_model;
	private JTable table;
	private DefaultTableModel table_model;
	private TreeAndTableBackend backend;
	
	public TreeAndTablePanel(TreeAndTableBackend backend) {
		this.backend = backend;
		initComponents();
		initLayout();
	}
	
	
	public Vector<TreeAndTableNode> getChildren(TreeAndTableNode node) {
		return backend.getChildren(node, this);
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
		TreeAndTableNode root = backend.getRootNode(this);
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
				table_model = backend.updateTableModel(node);
				table.setModel(table_model);
				for (int i=0;i<colCount;i++) {
					colModel.getColumn(i).setPreferredWidth(width[i]);
					colModel.getColumn(i).setCellRenderer(render[i]);
				}
			}
		});
		
		table = new JTable();
		table_model = backend.updateTableModel(null);
		table.setModel(table_model);
		
		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tree), new JScrollPane(table));
		split.setDividerLocation(230);
		
	}
	
	private void initLayout() {
		this.setLayout(new BorderLayout());
		this.add(split, BorderLayout.CENTER);
	}
}
