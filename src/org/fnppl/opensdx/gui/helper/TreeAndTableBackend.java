package org.fnppl.opensdx.gui.helper;

import java.io.File;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;


public interface TreeAndTableBackend {

	public TreeAndTableNode getRootNode(TreeAndTablePanel main);
	public Vector<TreeAndTableNode> getChildren(TreeAndTableNode node, TreeAndTablePanel main);
	public DefaultTableModel updateTableModel(TreeAndTableNode node);
	public void closeConnection();
	public void bu_transfer_pressed();
	public void bu_mkdir_pressed();
	public void bu_remove_pressed();
	public void bu_rename_pressed();
	
}
