package org.fnppl.opensdx.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.fnppl.opensdx.common.BusinessBooleanItem;
import org.fnppl.opensdx.common.BusinessBytesItem;
import org.fnppl.opensdx.common.BusinessCollection;
import org.fnppl.opensdx.common.BusinessDatetimeItem;
import org.fnppl.opensdx.common.BusinessIntegerItem;
import org.fnppl.opensdx.common.BusinessObject;
import org.fnppl.opensdx.common.BusinessStringItem;
import org.fnppl.opensdx.common.Feed;
import org.fnppl.opensdx.common.LicenseBasis;
import org.fnppl.opensdx.dmi.FeedCreator;
import org.fnppl.opensdx.gui.helper.MyObservable;
import org.fnppl.opensdx.gui.helper.MyObserver;
import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;
import org.fnppl.opensdx.xml.XMLElementable;

public class EditCheckBoxTree extends JTree implements MyObservable {
	
	private Element root;
	private Vector<MyCheckBoxTreeNode> nodes = new Vector<MyCheckBoxTreeNode>();
	private ActionListener changeListener = null;
	
	public EditCheckBoxTree(Element root) {
		changeListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				notifyChanges();
			}
		};
		TreeNode rootNode = new MyCheckBoxTreeNode(null, this, root);
		setModel(new DefaultTreeModel(rootNode));
	
		EditCheckBoxTreeCellRenderer renderer = new EditCheckBoxTreeCellRenderer(this); 
		setCellRenderer(renderer);
		setCellEditor(new MyCheckBoxTreeCellEditor(this, renderer));
		setEditable(true);
		
		
		//expandAllRows();
	}

	private Vector<MyObserver> observers = new Vector<MyObserver>();
	public void addObserver(MyObserver observer) {
		observers.add(observer);
	}

	public void notifyChanges() {
		 for (MyObserver ob : observers) {
	            ob.notifyChange(this);
	        }
	}
	
	public ActionListener getChangeListener() {
		return changeListener;
	}


	public void expandAllRows() {
		for (int i=0;i<this.getRowCount();i++) {
			expandRow(i);
		}
	}
	
	public Vector<String> getSelectedNodes() {
		Vector<String> result = new Vector<String>();
		for (MyCheckBoxTreeNode n : nodes) {
			if (n.isSelected()) {
				result.add(n.getName());
			}
		}
		return result;
	}

	public void setSelectedNodes(Vector<String> select) {
		for (MyCheckBoxTreeNode n : nodes) {
			if (select.contains(n.getName())) {
				n.setSelected(true);
			} else {
				n.setSelected(false);
			}
		}
		this.repaint();
	}

	protected void addNode(MyCheckBoxTreeNode node) {
		nodes.add(node);
	}
	
	public static void main(String[] args) {
		try {
	        UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
	    } catch(Exception ex){
	        System.out.println("Nimbus look & feel not available");
	    }
		JFrame f = new JFrame("Test tree edit");
		f.setSize(800,600);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		URL configGenres = FeedCreator.class.getResource("resources/config_genres.xml");
		Element root = null;
		try {
			root = Document.fromURL(configGenres).getRootElement();
		} catch (Exception e) {
			e.printStackTrace();
		}
		EditCheckBoxTree t = new EditCheckBoxTree(root);
		
		
		f.add(t, BorderLayout.CENTER);
		f.setVisible(true);
		
	}
}

class MyCheckBoxTreeNode implements TreeNode {
	
	private Element e;
	public TreeNode parent;
	private EditCheckBoxTree tree;
	private Vector<MyCheckBoxTreeNode> children = null;	
	private boolean leaf;
	private JCheckBox check;
	private String name;
	
//	private MyTreeNode me;
	
	
	public MyCheckBoxTreeNode(TreeNode parent, EditCheckBoxTree tree, Element e) {
		this.parent = parent;
		this.tree = tree;
		this.e = e;
		
		if (parent==null) {
			name = e.getName();
			check = null;
		} else {
			name = e.getChildTextNN("name");
			check = new JCheckBox(name);
			check.addActionListener(tree.getChangeListener());
		}
		Vector<Element> ch = e.getChildren(); 
		if (ch !=null && ch.size()>0) {
			children = new Vector<MyCheckBoxTreeNode>();
			if (e.getChild("subgenres")!=null) {
				ch = e.getChild("subgenres").getChildren();
			}
			if (ch!=null) {
				for (Element c : ch) {
					if (!c.getName().equals("name")) {
						children.add(new MyCheckBoxTreeNode(this, tree, c));
					}
				}
			}
			if (children.size()==0) children = null;
		}
		
		leaf = children==null;
		tree.addNode(this);
	}
	
	
	public String getName() {
		return name;
	}
	public JCheckBox getCheckBox() {
		return check;
	}
	
	public void setSelected(boolean value) {
		if (check==null) return;
		check.setSelected(value);
	}
	
	public boolean isSelected() {
		if (check==null) return false;
		return check.isSelected();
	}
	
//	public void insert(MutableTreeNode child, int index) {
//		if (xml instanceof BusinessObject && child instanceof MyTreeNode) {
//			BusinessObject bo = (BusinessObject)xml;
//			bo.addObject(((MyTreeNode)child).xml);
//		}
//	}
//
//	public void remove(int index) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	public void remove(MutableTreeNode node) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	public void removeFromParent() {
//		
//	}

//	public void setParent(MutableTreeNode newParent) {
//		parent = newParent;
//	}

//	public void setUserObject(Object object) {
//		if (object instanceof XMLElementable) {
//			xml = (XMLElementable)object;
//		}
//	}

	public Enumeration children() {
		final MyCheckBoxTreeNode me = this;
		Enumeration e = new Enumeration<TreeNode>() {
			private int pos = 0;
			
			public boolean hasMoreElements() {
				if (children==null) return false;
				if (pos<children.size()) {
					return true;
				}
				return false;
			}
			public TreeNode nextElement() {
				TreeNode n = children.get(pos);
				pos++;
				return  n;
			}
		};
		return e;
	}

	public boolean getAllowsChildren() {
		return true;
	}

	public TreeNode getChildAt(int pos) {
		return children.get(pos);
	}

	public int getChildCount() {
		if (children==null) return 0;
		return children.size();
	}

	public int getIndex(TreeNode node) {
		return children.indexOf(node);
	}

	public TreeNode getParent() {
		return parent;
	}

	public boolean isLeaf() {
		return leaf;
	}
	
}

class MyCheckBoxTreeCellEditor extends DefaultTreeCellEditor {
	protected JTree tree;
	protected EditCheckBoxTreeCellRenderer renderer;
	
	public MyCheckBoxTreeCellEditor(JTree tree, EditCheckBoxTreeCellRenderer renderer) {
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

class EditCheckBoxTreeCellRenderer extends DefaultTreeCellRenderer {

	protected EditCheckBoxTree treeview;
	
	public EditCheckBoxTreeCellRenderer(EditCheckBoxTree tree) {
		super();
		treeview = tree;
	}
	
	public Component getTreeCellRendererComponent(JTree tree, Object obValue, boolean sel, boolean expanded, boolean leaf, final int row, boolean hasFocus) {
		MyCheckBoxTreeNode node = (MyCheckBoxTreeNode)obValue;
		if (node.getCheckBox()==null) {
			return new JLabel(node.getName());
		}
		return node.getCheckBox();
    }
}