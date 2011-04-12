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

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.fnppl.opensdx.common.BaseObject;
import org.fnppl.opensdx.common.BaseObjectWithConstraints;
import org.fnppl.opensdx.xml.Document;

public class EditTreeView extends JTree {

	protected JTree tree;
	protected BaseObject base;
	public static ImageIcon iconRemove = null;
	
	public EditTreeView(BaseObject base) {
		tree = this;
		this.base = base;
		initIcons();
		
		MyTreeNode root = new MyTreeNode(base.getClassName(), base, null);
		setModel(new DefaultTreeModel(root));
		MyTreeNodeCellRenderer renderer = new MyTreeNodeCellRenderer(this); 
		setCellRenderer(renderer);
		setCellEditor(new MyTreeCellEditor(tree, renderer));
		setEditable(true);
		expandAllRows();
	}
	
	private void initIcons() {
		int w = 20;
		int h = 14;
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		AlphaComposite clear = AlphaComposite.getInstance(AlphaComposite.CLEAR, 1.0F);
		AlphaComposite full = AlphaComposite.getInstance(AlphaComposite.DST_OVER, 1.0F);
		g.setComposite(clear);
		g.fillRect(0,0,w,h);
		g.setComposite(full);
		g.setColor(Color.BLACK);

		int s = 4;
		g.setColor(Color.red.darker());
		int[] xPoints = new int[] {0,s,w/2,w-s,w,  w/2+s/2,  w,w-s,w/2,s,0,   w/2-s/2};
		int[] yPoints = new int[] {0,0,h/2-s/2,0,0,    h/2,    h,h,h/2+s/2,h,h,   h/2};
		g.fillPolygon(xPoints, yPoints, xPoints.length);
		img.flush();
		iconRemove = new ImageIcon(img);
	}
	
	public BaseObject getBaseObject() {
		return base;
	}
	
	public void setBaseObject(BaseObject base) {
		tree = this;
		this.base = base;
		MyTreeNode root = new MyTreeNode(base.getClassName(), base, null);
		setModel(new DefaultTreeModel(root));
		MyTreeNodeCellRenderer renderer = new MyTreeNodeCellRenderer(this); 
		setCellRenderer(renderer);
		setCellEditor(new MyTreeCellEditor(tree, renderer));
		setEditable(true);
		expandAllRows();
	}
	
	public void createNewObjectFor(MyTreeNode n, TreePath treePath) {
		BaseObject b = getBaseObjectInTree(n);
		int ind = getRow(treePath);
		b.createNewObjectFor(n.name);
		updateTree();
		//System.out.println(""+Arrays.toString(treePath.getPath()));
		tree.expandRow(ind);
		//System.out.println("create object: "+b.getClassName()+"::"+n.name);
	}
	
	public void setNewValue(MyTreeNode n, TreePath treePath) {
		BaseObject b = getBaseObjectInTree(n);
		if (n.value instanceof String[]) {
			String[] s = (String[])n.value;
			s[s.length-1] = n.text.getText();
			b.set(n.name, n.value);
		} else {
			n.value = n.text.getText();
			b.set(n.name, n.value);
		}
		n.text.setBackground(Color.WHITE);
	}
	
	public void removeObjectFor(MyTreeNode n, TreePath treePath) {
		BaseObject b = getBaseObjectInTree(n);
		if (n.value!=null) {
			//System.out.println("removing "+n.name);
			if (n.isLeaf) {
				if (n.parent.value instanceof Vector) {
					((Vector)n.parent.value).remove(n.value);
				} else {
					b.set(n.name, null);
				}
			} else {
				if (n.parent.value instanceof Vector) {
					((Vector)n.parent.value).remove(n.value);
				} else {
					BaseObject b2 = getBaseObjectInTree(n.parent);
					b2.set(n.name, null);
				}
			}
			updateTree();
		}
		
		//System.out.println("create object: "+b.getClassName()+"::"+n.name);
	}
	
	public void addNewObjectFor(MyTreeNode n, TreePath treePath) {
		try {
			if (n.value instanceof Vector) {
				BaseObject b = getBaseObjectInTree(n);
				//System.out.println("baseobject: "+b.getClassName()+"::add "+n.name);
				String className = n.name;
				if (className.startsWith("List of ")) className = className.substring(8);
				int ind = getRow(treePath);
				b.addNewObjectFor(className);
				updateTree();
				tree.expandRow(ind);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void updateTree() {
		Vector<Integer> exp = getExpansion();
		MyTreeNode root = new MyTreeNode(base.getClassName(), base, null);
		setModel(new DefaultTreeModel(root));
		setExpansion(exp);
	}
	
	public Vector<Integer> getExpansion() {
		Vector<Integer> exp = new Vector<Integer>();
		TreePath rowPath = tree.getPathForRow(0);
        int a = tree.getRowCount();
        for(int i=0; i<a; i++){
            TreePath path = tree.getPathForRow(i);
            if(tree.isExpanded(path)) exp.add(i);
        }
        return exp;
	}
	
	public void setExpansion(Vector<Integer> exp) {
		for (Integer i : exp) {
			tree.expandRow(i.intValue());
		}
	}
	
	public int getRow(TreePath treepath) {
	    int a = tree.getRowCount();
        for(int i=0; i<a; i++){
            TreePath path = tree.getPathForRow(i);
            if (path.equals(treepath)) return i;
        }
        return -1;
	}
	
	public BaseObject getBaseObjectInTree(MyTreeNode node) {
		Vector<MyTreeNode> path = new Vector<MyTreeNode>();
		MyTreeNode next = node;
		path.add(node);
		while(next.parent!=null) {
			path.add(0,next.parent);	
			next = next.parent;
		}
		BaseObject b = base;
		Object val = null;
		for (int i=0;i<path.size();i++) {
			MyTreeNode m = path.get(i);
			val = m.value;
			if (m.parent!=null && m.parent.name.startsWith("List of ")) {
//				Vector list = (Vector)m.parent.value;
//				int index = list.indexOf(m.value);
//				System.out.print("list index::"+index+" : ");
			} else {
				val = b.getObject(m.name);
			}
			if (val instanceof BaseObject) {
				b = (BaseObject)val;
			}
			//System.out.println(m.name + " bo::"+b.getClassName()+"   "+(val==null?"":val.getClass().getName()));
		}
		return b;
	}
	
	public void expandAllRows() {
		for (int i = 0; i < tree.getRowCount(); i++) {
	         tree.expandRow(i);
		}
	}
	
	
	public static void main(String[] args) {
		File xml = new File("src/org/fnppl/opensdx/dmi/resources/example_feed.xml");
		
		try {
			final JFrame f = new JFrame("test tree view");
			f.setSize(1000, 600);
			f.setLayout(new BorderLayout());
			BaseObject test = BaseObject.fromElement(org.fnppl.opensdx.xml.Document.fromFile(xml).getRootElement());
			//org.fnppl.opensdx.commonAuto.Feed test = new org.fnppl.opensdx.commonAuto.Feed();
			//test.createNewObjectFor("feedinfo");
			
			final EditTreeView t = new EditTreeView(test);
			final JScrollPane scroll = new JScrollPane(t); 
			f.add(scroll, BorderLayout.CENTER);
			JPanel p = new JPanel();
			f.add(p, BorderLayout.SOUTH);
			JButton b = new JButton("new");
			b.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						t.setBaseObject(new org.fnppl.opensdx.commonAuto.Feed());
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});
			p.add(b);
			b = new JButton("save as xml");
			b.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						File f = Dialogs.chooseSaveFile("Save as xml file", null, "test.xml");
						if (f!=null) {
							BaseObject b = t.getBaseObject();
							Document doc = Document.buildDocument(b.toElement());
							doc.writeToFile(f);
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});
			p.add(b);
			
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			f.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private class MyTreeNode implements MutableTreeNode {

		public MyTreeNode parent = null;
		public String name = null;
		public Object value = null;
		public String constraint = null;
		public JTextField text = null;
		
		public boolean isLeaf = true;
		public int childCount = -1;
		
		private Vector<MyTreeNode> children = new Vector<MyTreeNode>();
		
		public MyTreeNode(String name, Object value, MyTreeNode p) {
			this.name = name;
			this.value = value;
			parent = p;
			
			if (value!=null && value instanceof Vector) {
				this.name = "List of "+name;
				childCount = ((Vector)value).size();
			}
			else if (value!=null && value instanceof BaseObject) {
				childCount = ((BaseObject)value).getNames().size();
			}
			if (childCount>=0) isLeaf = false;
			else childCount = 0;
			
			for (int i=0;i<childCount;i++) {
				children.add(buildChildAt(i));
			}
			
		}
		
		public String toString() {
			String s = name;
			if (value!=null) {
				s += "::"+value.toString();
			}
			return s;
		}
		
		public Enumeration children() {
			Enumeration en = new Enumeration<MyTreeNode>() {
				private int pos = 0;
				public boolean hasMoreElements() {
					if (pos<childCount) return true;
					return false;
				}
				public MyTreeNode nextElement() {
					return children.get(pos);
				}
			};
			return null;
		}
		
		public boolean getAllowsChildren() {
			//if (isLeaf) return false;
			return false;
		}
		
		public MyTreeNode getChildAt(int index) {
			return children.get(index);
		}

		private MyTreeNode buildChildAt(int index) {
			String n = null;
			Object v = null;
			String con = null;
			if (value instanceof Vector) {
				//n = name;
				Vector vec = (Vector)value;
				v = vec.get(index);
				n = v.getClass().getName().substring(v.getClass().getName().lastIndexOf('.')+1).toLowerCase();
			}
			else if (value instanceof BaseObject) {
				n = ((BaseObject)value).getNames().get(index);
				v = ((BaseObject)value).getObject(n);
				if (value instanceof BaseObjectWithConstraints) {
					con = ((BaseObjectWithConstraints)value).getConstraint(index);
				}
			}
			MyTreeNode node = new MyTreeNode(n, v, this);
			node.constraint = con;
			return node;
		}

		public int getChildCount() {
			return childCount;
		}

		public int getIndex(TreeNode node) {
			return children.indexOf(node);
		}

		public TreeNode getParent() {
			return parent;
		}

		public boolean isLeaf() {
			return isLeaf;
		}

		
		public void insert(MutableTreeNode child, int index) {
			children.add(index, (MyTreeNode)child);
			childCount = children.size();
		}

		public void remove(int index) {
			children.remove(index);
			childCount = children.size();
		}

		public void remove(MutableTreeNode node) {
			children.remove(node);
			childCount = children.size();
		}

		public void removeFromParent() {
			parent = null;
		}

		public void setParent(MutableTreeNode newParent) {
			parent = (MyTreeNode)newParent;
		}

		public void setUserObject(Object object) {
			value = object;
		}
		
	}
	
	private class MyTreeNodeCellRenderer extends DefaultTreeCellRenderer {

		protected EditTreeView treeview;

		public MyTreeNodeCellRenderer(EditTreeView tree) {
			super();
			treeview = tree;
		}
		
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, final int row, boolean hasFocus) {
	    	//String addText = " sel="+sel+", expanded="+expanded+", leaf="+leaf+", row="+row+", hasfocus="+hasFocus;
	    	
			int sizeY = 18;
	    	int sizeYp = sizeY+2;
	    	int sizeX1 = 200;
	    	int sizeX2 = 300;
	    	int sizeX3 = 120;
	    	int sizeX4 = 20;
	    	
	    	final MyTreeNode n = (MyTreeNode)value;
	    	String c = n.constraint;
	    	JLabel lab  = new JLabel(n.name);
	    	if (n.name.equals("string;")) {
	    		String name = n.parent.name;
	    		if (name.startsWith("List of "))
	    			name = name.substring(8);
	    		lab.setText(name);
	    	}
	    	JPanel p = new JPanel();
	    	FlowLayout layout = new FlowLayout();
	    	layout.setVgap(1);
	    	layout.setHgap(10);
	    	layout.setAlignment(FlowLayout.LEFT);
	    	p.setLayout(layout);
	    	
	    	p.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    		p.setAlignmentY(JPanel.CENTER_ALIGNMENT);
    		p.add(lab);
    		if (sel) {
    			p.setBackground(Color.lightGray);
			} else {
    			p.setBackground(new Color(230,230,230));
    		}
    		
    		//List with add button
    		if (n.name.startsWith("List of ")) { 
    			createListOf(lab, p, row, n, c, sizeX1, sizeX3, sizeY, sizeYp);
    			return p;
    		}
    		
    		//Leaf
	    	if (leaf) {
	    		Object val = n.value;
	    		lab.setPreferredSize(new Dimension(sizeX1,sizeY));
	    		
	    		//name + create button
	    		if (val==null) {
	    			createWithCreateButton(lab, p, row, n, c, sizeX1, sizeX2, sizeX3, sizeY, sizeYp);
	    			return p;
	    		}
	    		
	    		JTextField text = new JTextField(val.toString());
	    		text.setPreferredSize(new Dimension(sizeX2,sizeY));
	    		text.setFocusable(true);
	    		text.setEditable(true);
	    		n.text = text;
	    		
	    		//name with attributes
	    		if (val instanceof String[]) {
	    			String[] s = (String[])val;
	    			for (int i=0;i<s.length-1;i+=2) {
	    				lab.setText(lab.getText()+"  "+s[i]+"="+s[i+1]);
	    			}
	    			text.setText(s[s.length-1]);
	    		}
	    		
	    		if (sel) {
	    			p.setBackground(Color.lightGray);
	    			DocumentListener chListen = new DocumentListener() {
	    				public void removeUpdate(DocumentEvent e) {action();}
	    				public void insertUpdate(DocumentEvent e) {action();}
	    				public void changedUpdate(DocumentEvent e) {action();}
	    				private void action() {
	    					if (n.value instanceof String[]) {
	    						String[] s = (String[])n.value;
	    						if (n.value!=null && !n.text.getText().equals(s[s.length-1])) {
		    						n.text.setBackground(Color.YELLOW);
		    					} else {
		    						n.text.setBackground(Color.WHITE);
		    					}
	    					} else {
		    					if (n.value!=null && !n.text.getText().equals(n.value.toString())) {
		    						n.text.setBackground(Color.YELLOW);
		    					} else {
		    						n.text.setBackground(Color.WHITE);
		    					}
	    					}
	    				}
	    			};
	    			text.getDocument().addDocumentListener(chListen);
	    			text.addKeyListener(new KeyAdapter() {
		                public void keyPressed(KeyEvent e) {
		                    if(e.getKeyCode() == KeyEvent.VK_ENTER){
		                    	treeview.setNewValue(n,treeview.getPathForRow(row));
		                    }
		                }
		            });
	    		} else {
	    			p.setBackground(new Color(230,230,230));
	    		}
	    		p.add(text);
	    		
	    		//remove button
	    		JButton bu = new JButton(EditTreeView.iconRemove);
				bu.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						treeview.removeObjectFor(n, treeview.getPathForRow(row));;
					}
				});
				bu.setPreferredSize(new Dimension(sizeX4,sizeY));
				p.add(bu);
				
	    		if (c!=null) {
	    			JLabel cst = new JLabel("("+c+")");
	    			p.add(cst);
	    			p.setPreferredSize(new Dimension(sizeX1+sizeX2+sizeX3+sizeX4+50,sizeYp));
	    		} else {
	    			p.setPreferredSize(new Dimension(sizeX1+sizeX2+sizeX4+40,sizeYp));
	    		}
	    		return p;
	        } else {
	        	lab.setPreferredSize(new Dimension(sizeX1,sizeY));
	        	int sizeXe = 0;
	        	if (n.parent!=null) {
	        		sizeXe = sizeX4+10;
			    	//remove button
					JButton bu = new JButton(EditTreeView.iconRemove);
					bu.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							treeview.removeObjectFor(n, treeview.getPathForRow(row));
						}
					});
					bu.setPreferredSize(new Dimension(sizeX4,sizeY));
					p.add(bu);
	        	}
				if (c!=null) {
					JLabel cst = new JLabel("("+c+")");
					cst.setPreferredSize(new Dimension(sizeX3+sizeXe+20,sizeY));
					p.add(cst);
					p.setPreferredSize(new Dimension(sizeX1+sizeX3+sizeXe+30,sizeYp));
				} else {
					p.setPreferredSize(new Dimension(sizeX1+sizeXe+20,sizeYp));
	    		}
	        	return p;
	        }
	    }
		
		private void createListOf(JLabel lab, JPanel p, final int row, final MyTreeNode n, String c, int sizeX1, int sizeX3, int sizeY, int sizeYp) {
			int sizeX = 80;
			lab.setPreferredSize(new Dimension(sizeX1,sizeY));
			JButton bu = new JButton("add");
			bu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					treeview.addNewObjectFor(n, treeview.getPathForRow(row));;
				}
			});
			bu.setPreferredSize(new Dimension(sizeX,sizeY));
			p.add(bu);
			
			if (c!=null) {
    			JLabel cst = new JLabel("("+c+")");
    			p.setPreferredSize(new Dimension(sizeX3,sizeY));
    			p.add(cst);
    			p.setPreferredSize(new Dimension(sizeX1+sizeX+sizeX3+40,sizeYp));
    		} else {
    			p.setPreferredSize(new Dimension(sizeX1+sizeX+20,sizeYp));
    		}
		}
		
		private void createWithCreateButton(JLabel lab, JPanel p, final int row, final MyTreeNode n, String c, int sizeX1, int sizeX2, int sizeX3, int sizeY, int sizeYp) {
			lab.setPreferredSize(new Dimension(sizeX1,sizeY));
			JButton bu = new JButton("create");
			bu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					treeview.createNewObjectFor(n, treeview.getPathForRow(row));;
				}
			});
			bu.setPreferredSize(new Dimension(sizeX2,sizeY));
			p.add(bu);
			
			if (c!=null) {
    			JLabel cst = new JLabel("("+c+")");
    			p.setPreferredSize(new Dimension(sizeX3,sizeY));
    			p.add(cst);
    			p.setPreferredSize(new Dimension(sizeX1+sizeX2+sizeX3+50,sizeYp));
    		} else {
    			p.setPreferredSize(new Dimension(sizeX1+sizeX3+50,sizeYp));
    		}
		}
	}
	
	
	
	private class MyTreeCellEditor extends DefaultTreeCellEditor {
		protected JTree tree;
		protected MyTreeNodeCellRenderer renderer;
		
		public MyTreeCellEditor(JTree tree, MyTreeNodeCellRenderer renderer) {
			super(tree,renderer);
			this.tree = tree;
			this.renderer = renderer;
		}
		
		public boolean isCellEditable(EventObject anEvent) {
			//System.out.println(anEvent.getClass().getName());
//			if (anEvent instanceof MouseEvent) {
//				MouseEvent e = (MouseEvent)anEvent;
//				int selRow = tree.getRowForLocation(e.getX(), e.getY());
//				
//				if(selRow != -1) {
//					TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
//					if (selPath!=null) {
//						Object[] path = selPath.getPath();
//
//						MyTreeNode last = (MyTreeNode)path[path.length-1];
//						if (last.text!=null) {
//							return true;
//						}
//						if (last.value==null) {
//							return true;
//						}
//						if (last.name.startsWith("List of ")) {
//							return true;
//						}
////						System.out.println( "NOT EDITABLE");
//					}
//				}
//			}
//			return false;
			return true;
		}
		
		 public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
		        return renderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);
		 }
	}


}
