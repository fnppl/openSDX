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

import javax.swing.event.CellEditorListener;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import org.fnppl.opensdx.common.BusinessCollection;
import org.fnppl.opensdx.common.BusinessObject;
import org.fnppl.opensdx.common.Territorial;
import org.fnppl.opensdx.dmi.FeedCreator;
import org.fnppl.opensdx.dmi.FeedGui;
import org.fnppl.opensdx.gui.helper.MyObservable;
import org.fnppl.opensdx.gui.helper.MyObserver;
import org.fnppl.opensdx.security.KeyApprovingStore;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;
import org.fnppl.opensdx.xml.XMLElementable;

import com.sun.xml.internal.ws.message.RootElementSniffer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.CellEditor;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;

public class SelectTerritoiresTree extends JTree implements MyObservable {

	
	private URL configTerritories = FeedCreator.class.getResource("resources/config_territories.xml");
	private Element rootElement = null;
	private HashMap<Object,JPanel> map = new HashMap<Object, JPanel>();
	private String selectedCode = null;
	
	private Vector<MyObserver> observers = new Vector<MyObserver>();
    public void addObserver(MyObserver observer) {
        observers.add(observer);
    }
    
    
    public String getSelectedCode() {
    	return selectedCode;
    }
    public void select(String code) {
    	selectedCode = code;
        for (MyObserver ob : observers) {
            ob.notifyChange(this);
        }
    }
    
    public void notifyChanges() {
        for (MyObserver ob : observers) {
            ob.notifyChange(this);
        }
    }
    
	public SelectTerritoiresTree() {
		try {
			rootElement = Document.fromURL(configTerritories).getRootElement();
			rootElement = rootElement.getChild("WW");
		} catch (Exception e) {
			rootElement = new Element("ww");
			e.printStackTrace();
		}
		
		TerritoryTreeNode root = new TerritoryTreeNode(null,this, rootElement);
		setModel(new DefaultTreeModel(root));
		
		setCellRenderer(new TreeCellRenderer() {
			private Dimension label_dimension = new Dimension(150, 18);
			private Dimension labelCode_dimension = new Dimension(40, 18);
			private Dimension button_dimension = new Dimension(50, 18);
			//private Dimension panel_dimension = new Dimension(400, 20);
			
			public Component getTreeCellRendererComponent(JTree tree, Object value,	boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
				TerritoryTreeNode node = (TerritoryTreeNode)value;
				final String code = node.xml.getName();
				final String name = node.xml.getAttribute("name");
				JPanel p = map.get(value);
				if (p==null) {
					p = new JPanel();
					p.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 1));
					final JPanel pan = p;
					JLabel label = new JLabel(name);
					label.setPreferredSize(label_dimension);
					p.add(label);
					if (!code.equals("territories") && node.getChildCount()==0) {
						JLabel lc = new JLabel(code);
						lc.setPreferredSize(labelCode_dimension);
						p.add(lc);
						JButton buAllow = new JButton("OK");
						buAllow.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								select(code);
							}
						});
						buAllow.setPreferredSize(button_dimension);
						p.add(buAllow);
					}
					map.put(value,p);
				}
				return p;
			}
		});
		setCellEditor(new TreeCellEditor() {
			public Component getTreeCellEditorComponent(JTree tree,
					Object value, boolean isSelected, boolean expanded,
					boolean leaf, int row) {
				Component c = tree.getCellRenderer().getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, true);
				return c;
			}

			public void addCellEditorListener(CellEditorListener l) {
	
			}
			public void cancelCellEditing() {
								
			}
			public Object getCellEditorValue() {
				return null;
			}

			public boolean isCellEditable(EventObject anEvent) {
				return true;
			}
			public void removeCellEditorListener(CellEditorListener l) {
				
			}
			public boolean shouldSelectCell(EventObject anEvent) {
				return false;
			}
			public boolean stopCellEditing() {
				return false;
			}			
		});
		setEditable(true);
		//expandAllRows();
	}

	public void expandAllRows() {
		for (int i=0;i<this.getRowCount();i++) {
			expandRow(i);
		}
	}
	
	public static void main(String[] args) {
		try {
	        UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
	    } catch(Exception ex){
	        System.out.println("Nimbus look & feel not available");
	    }
//		JFrame f = new JFrame("test territories tree");
//		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		f.setSize(600, 800);
//		f.setContentPane(new JScrollPane(new SelectTerritoiresTree()));
//		f.setVisible(true);
	    FeedGui.showCountryCodeSelector();
	}
	
	
	
	private class TerritoryTreeNode implements TreeNode {
		
		public Element xml;
		public TerritoryTreeNode parent;
		public SelectTerritoiresTree tree;
		private TerritoryTreeNode me;
		
		
		public TerritoryTreeNode(TerritoryTreeNode parent, SelectTerritoiresTree tree, Element xml) {
			this.parent = parent;
			this.tree = tree;
			this.xml = xml;
			me = this;
		}

		public Enumeration children() {
			Enumeration e = new Enumeration<TreeNode>() {
				private int pos = 0;
				public boolean hasMoreElements() {
					if (pos<getChildCount()) return true;
					return false;
				}
				public TreeNode nextElement() {
					TreeNode n = getChildAt(pos);
					pos++;
					return n;
				}
			};
			return e;
		}


		public boolean getAllowsChildren() {
			return !isLeaf();
		}

		public TreeNode getChildAt(int childIndex) {
			if (isLeaf()) return null;
			return new TerritoryTreeNode(this,tree,xml.getChildren().get(childIndex));
		}


		public int getChildCount() {
			return xml.getChildren().size();
		}

		public int getIndex(TreeNode node) {
			TerritoryTreeNode n = (TerritoryTreeNode)node;
			int index = xml.getChildren().indexOf(n.xml);
			int count = xml.getChildren().size();
			for (int i=0;i<count;i++) {
				if (xml.getChildren().get(i).getName().equals(n.xml.getName())) {
					index = i;
					break;
				}
			}
			System.out.println("index of "+n.xml.getName()+" :: "+index);
			return index;
		}

		public TreeNode getParent() {
			return parent;
		}

		public boolean isLeaf() {
			if (xml.getChildren().size()==0) return true;
			return false;
		}
	}

	
//		public void insert(MutableTreeNode child, int index) {
////			if (xml instanceof BusinessObject && child instanceof MyTreeNode) {
////				BusinessObject bo = (BusinessObject)xml;
////				bo.addObject(((MyTreeNode)child).xml);
////			}
//		}
//
//		public void remove(int index) {
//						
//		}
//
//		public void remove(MutableTreeNode node) {
//			
//		}
//
//		public void removeFromParent() {
//			
//		}
//
//		public void setParent(MutableTreeNode newParent) {
//			parent = newParent;
//		}
//
//		public void setUserObject(Object object) {
//			if (object instanceof XMLElementable) {
//				xml = (XMLElementable)object;
//			}
//		}
//
//		public Enumeration children() {
//			if (xml instanceof BusinessObject)  {
//				BusinessObject bo = (BusinessObject)xml;
//				final Vector<XMLElementable> list = bo.getElements();
//				final int childCount = list.size();
//				Enumeration e = new Enumeration<TreeNode>() {
//					private int pos = 0;
//					public boolean hasMoreElements() {
//						if (pos<childCount) return true;
//						return false;
//					}
//					public TreeNode nextElement() {
//						//TreeNode n = tree.get(list.get(pos));
//						TreeNode n = new MyTreeNode(me, tree, list.get(pos));
//						pos++;
//						return n;
//					}
//					
//				};
//			}
//			if (xml instanceof BusinessCollection)  {
//				final BusinessCollection bc = (BusinessCollection)xml;
//				final int childCount = bc.size();
//				Enumeration e = new Enumeration<TreeNode>() {
//					private int pos = 0;
//					public boolean hasMoreElements() {
//						if (pos<childCount) return true;
//						return false;
//					}
//					public TreeNode nextElement() {
//						//TreeNode n = tree.get(list.get(pos));
//						TreeNode n = new MyTreeNode(me, tree, (XMLElementable)bc.get(pos));
//						pos++;
//						return n;
//					}
//					
//				};
//			}
//			return null;
//		}
	
}
