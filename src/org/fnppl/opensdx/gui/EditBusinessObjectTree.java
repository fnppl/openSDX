package org.fnppl.opensdx.gui;
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
import java.awt.Component;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.fnppl.opensdx.common.BusinessCollection;
import org.fnppl.opensdx.common.BusinessItem;
import org.fnppl.opensdx.common.BusinessObject;
import org.fnppl.opensdx.common.BusinessStringItem;
import org.fnppl.opensdx.common.Feed;
import org.fnppl.opensdx.dmi.FeedCreator;
import org.fnppl.opensdx.xml.Element;
import org.fnppl.opensdx.xml.XMLElementable;

public class EditBusinessObjectTree extends JTree {
	
	private BusinessObject businessObject;
	private HashMap<XMLElementable, TreeNode> nodes = new HashMap<XMLElementable, TreeNode>();
	
	public EditBusinessObjectTree(BusinessObject businessObject) {
		this.businessObject = businessObject;
		
		MyTreeNode root = new MyTreeNode(null,this, businessObject);
		setModel(new DefaultTreeModel(root));
		
		EditBusinessObjectTreeCellRenderer renderer = new EditBusinessObjectTreeCellRenderer(this);
		setCellRenderer(renderer);
		setCellEditor(new MyTreeCellEditor(this, renderer));
		setEditable(true);
		expandAllRows();
	}
	
	public void expandAllRows() {
		for (int i=0;i<this.getRowCount();i++) {
			expandRow(i);
		}
	}
	
	public TreeNode get(XMLElementable xml) {
		return nodes.get(xml);
	}
	
	public void putNode(MyTreeNode node) {
		nodes.put(node.xml, node);
	}

	public static void main(String[] args) {
		JFrame f = new JFrame("Test tree edit");
		f.setSize(800,600);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		BusinessObject bo = new BusinessObject() {
			public String getKeyname() {
				return "test_object";
			}
		};
		bo.addObject(new BusinessStringItem("testitem", "testvalue"));
		bo.addObject(new BusinessStringItem("testitem2", "testvalue2"));
		System.out.println(bo.getOtherObjects().size()+"");
		//EditXMLTree t = new EditXMLTree(bo);
		
		Feed feed = FeedCreator.makeExampleFeed();
		EditBusinessObjectTree t = new EditBusinessObjectTree(feed.getFeedinfo());
		
		f.add(t, BorderLayout.CENTER);
		f.setVisible(true);
		
	}
}

class MyTreeNode implements MutableTreeNode {
	
	//public BusinessObject bo = null;
	//public BusinessItem item = null;
	public XMLElementable xml;
	public TreeNode parent;
	private EditBusinessObjectTree tree;
	private MyTreeNode me;
	
	
	public MyTreeNode(TreeNode parent, EditBusinessObjectTree tree, XMLElementable xml) {
		this.parent = parent;
		this.tree = tree;
		this.xml = xml;
		me = this;
	}
	
	public void insert(MutableTreeNode child, int index) {
		if (xml instanceof BusinessObject && child instanceof MyTreeNode) {
			BusinessObject bo = (BusinessObject)xml;
			bo.addObject(((MyTreeNode)child).xml);
		}
	}

	public void remove(int index) {
		// TODO Auto-generated method stub
		
	}

	public void remove(MutableTreeNode node) {
		// TODO Auto-generated method stub
		
	}

	public void removeFromParent() {
		
	}

	public void setParent(MutableTreeNode newParent) {
		parent = newParent;
	}

	public void setUserObject(Object object) {
		if (object instanceof XMLElementable) {
			xml = (XMLElementable)object;
		}
	}

	public Enumeration children() {
		if (xml instanceof BusinessObject)  {
			BusinessObject bo = (BusinessObject)xml;
			final Vector<XMLElementable> list = bo.getElements();
			final int childCount = list.size();
			Enumeration e = new Enumeration<TreeNode>() {
				private int pos = 0;
				public boolean hasMoreElements() {
					if (pos<childCount) return true;
					return false;
				}
				public TreeNode nextElement() {
					//TreeNode n = tree.get(list.get(pos));
					TreeNode n = new MyTreeNode(me, tree, list.get(pos));
					pos++;
					return n;
				}
				
			};
		}
		if (xml instanceof BusinessCollection)  {
			final BusinessCollection bc = (BusinessCollection)xml;
			final int childCount = bc.size();
			Enumeration e = new Enumeration<TreeNode>() {
				private int pos = 0;
				public boolean hasMoreElements() {
					if (pos<childCount) return true;
					return false;
				}
				public TreeNode nextElement() {
					//TreeNode n = tree.get(list.get(pos));
					TreeNode n = new MyTreeNode(me, tree, (XMLElementable)bc.get(pos));
					pos++;
					return n;
				}
				
			};
		}
		return null;
	}

	public boolean getAllowsChildren() {
		if (xml instanceof BusinessObject)  {
			return true;
		}
		return false;
	}

	public TreeNode getChildAt(int childIndex) {
		if (xml instanceof BusinessObject)  {
			BusinessObject bo = (BusinessObject)xml;
			//TreeNode x = tree.get(bo.getOtherObjects().get(childIndex));
			TreeNode n = new MyTreeNode(me, tree, bo.getElements().get(childIndex));
			return n;
		}
		if (xml instanceof BusinessCollection<?>)  {
			BusinessCollection bc = (BusinessCollection)xml;
			TreeNode n = new MyTreeNode(me, tree, (XMLElementable)bc.get(childIndex));
			return n;
		}
		//System.out.println("ERROR in getChildAt");
		return null;
	}

	public int getChildCount() {
		if (xml instanceof BusinessObject)  {
			BusinessObject bo = (BusinessObject)xml;
			//System.out.println("childcount = "+bo.getOtherObjects().size());
			return bo.getElements().size();
		}
		if (xml instanceof BusinessCollection<?>)  {
			BusinessCollection bc = (BusinessCollection)xml;
			return bc.size();
		}
		//System.out.println("no children");
		return 0;
	}

	public int getIndex(TreeNode node) {
		if (xml instanceof BusinessObject && node instanceof MyTreeNode)  {
			BusinessObject bo = (BusinessObject)xml;
			int ind = bo.getElements().indexOf(((MyTreeNode)node).xml);
			return ind;
		}
		return 0;
	}

	public TreeNode getParent() {
		return parent;
	}

	public boolean isLeaf() {
		if (xml instanceof BusinessObject || xml instanceof BusinessCollection<?>)  {
			return false;
		}
		return true;
	}
	
}

class MyTreeCellEditor extends DefaultTreeCellEditor {
	protected JTree tree;
	protected EditBusinessObjectTreeCellRenderer renderer;
	
	public MyTreeCellEditor(JTree tree, EditBusinessObjectTreeCellRenderer renderer) {
		super(tree,renderer);
		this.tree = tree;
		this.renderer = renderer;
	}
	
	public boolean isCellEditable(EventObject anEvent) {
		return true;
	}
	
	 public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
	     return renderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);
	 }
}
