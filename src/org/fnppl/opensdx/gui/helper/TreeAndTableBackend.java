package org.fnppl.opensdx.gui.helper;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;


public interface TreeAndTableBackend {

	public TreeAndTableNode getRootNode(TreeAndTablePanel main);
	public Vector<TreeAndTableNode> getChildren(TreeAndTableNode node, TreeAndTablePanel main);
	public DefaultTableModel updateTableModel(TreeAndTableNode node);
}
