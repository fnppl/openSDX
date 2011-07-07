package org.fnppl.opensdx.gui.helper;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.fnppl.opensdx.common.BusinessObject;
import org.fnppl.opensdx.common.ContractPartner;
import org.fnppl.opensdx.common.Contributor;
import org.fnppl.opensdx.common.IDs;
import org.fnppl.opensdx.common.LicenseBasis;
import org.fnppl.opensdx.common.Receiver;
import org.fnppl.opensdx.common.Territorial;
import org.fnppl.opensdx.dmi.FeedGui;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;

import sun.awt.DefaultMouseInfoPeer;

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

public class PanelSavedDMI extends JPanel {

	private File dataDirectory = null;
	private Vector<DMIObject> objects;
	
	private JLabel labelDataDir;
	private JTextField textDataDir;
	private JButton buSelectDataDir;
	private JButton buReadData;
	private JPanel panelNorth;
	private JSplitPane split;
	private JPanel panelDetails;
	private JPanel panelButtons;
	private JScrollPane scrollTable;
	private JTable table;
	private DefaultTableModel table_model;
	private String[] table_header = new String[] {"type","file","description"};
	private String[][] table_data = new String[0][3];
	
	private JButton buAddToFeedInfo;
	
	private FeedGui gui;
	
	public PanelSavedDMI(FeedGui gui) {
		this.gui = gui;
		
		dataDirectory = new File(System.getProperty("user.home")+"/openSDX/dmi_data");
		
		readData();
		initComponents();
		initLayout();
		
	}
	
	public void readData() {
		objects = new Vector<DMIObject>();
		File[] list = dataDirectory.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.toLowerCase().endsWith(".xml")) {
					return true;
				}
				return false;
			}
		});
		if (list!=null) {
			for (File f: list) {
				try {
					Element root = Document.fromFile(f).getRootElement();
					String name = root.getName();
					if (name.equals("collection")) {
						Vector<Element> ve = root.getChildren();
						if (ve!=null) {
							for (Element e : ve) {
								addObject(e,f);
							}
						}
					} else {
						addObject(root,f);
					}
					BusinessObject.fromElement(root);
				} catch (Exception ex) {
					System.out.println("Error reading from file: "+f.getAbsolutePath());
				}
			}
		}
	}
	
	private void addObject(Element e, File f) {
		String name = e.getName();
		BusinessObject bo = BusinessObject.fromElement(e);
		if (name.equals("sender")) {
			ContractPartner v = ContractPartner.fromBusinessObject(bo, ContractPartner.ROLE_SENDER);
			if (v!=null) {
				v.role(ContractPartner.ROLE_CONTRACT_PARTNER);
				DMIObject o = new DMIObject();
				o.type = "Contract Partner";
				o.description = v.getEmail();
				o.fromFile = f;
				o.object = v;
				objects.add(o);
			}
		}
		else if (name.equals("licensor")) {
			ContractPartner v = ContractPartner.fromBusinessObject(bo, ContractPartner.ROLE_LICENSOR);
			if (v!=null) {			
				v.role(ContractPartner.ROLE_CONTRACT_PARTNER);
				DMIObject o = new DMIObject();
				o.type = "Contract Partner";
				o.description = v.getEmail();
				o.fromFile = f;
				o.object = v;
				objects.add(o);
			}
		}
		else if (name.equals("receiver")) {
			Receiver v = Receiver.fromBusinessObject(bo);
			if (v!=null) {
				DMIObject o = new DMIObject();
				o.type = "Receiver";
				o.description = v.getType()+":: "+v.getServername();
				o.fromFile = f;
				o.object = v;
				objects.add(o);
			}
		}
		else if (name.equals("contributor")) {
			Contributor v = Contributor.fromBusinessObject(bo);
			if (v!=null) {
				DMIObject o = new DMIObject();
				o.type = "Contributor";
				o.description = v.getName()+" :: "+v.getType();
				o.fromFile = f;
				o.object = v;
				objects.add(o);
			}
		}
		else if (name.equals("ids")) {
			IDs v = IDs.fromBusinessObject(bo);
			if (v!=null) {
				DMIObject o = new DMIObject();
				o.type = "IDs";
				o.description = "";
				o.fromFile = f;
				o.object = v;
				objects.add(o);
			}
		}
		else if (name.equals("territorial")) {
			Territorial v = Territorial.fromBusinessObject(bo);
			if (v!=null) {
				DMIObject o = new DMIObject();
				o.type = "Territorial";
				o.description = "";
				o.fromFile = f;
				o.object = v;
				objects.add(o);
			}
		}
	}
	
	private void updateTableModel() {
		int count = objects.size();
		table_data = new String[count][3];
		for (int i=0;i<count;i++) {
			DMIObject o = objects.get(i);
			table_data[i][0] = o.type;
			table_data[i][1] = o.fromFile.getName();
			table_data[i][2] = o.description;
		}
		table_model = new DefaultTableModel(table_data, table_header);
		table.setModel(table_model);
	}
	
	private void table_selection_changed(int index) {
		if (index<0 || index >= objects.size()) {
			return;
		}
		DMIObject o = objects.get(index);
		System.out.println("Selection changed: "+index+" :: "+o.type+", "+o.description+", "+o.fromFile.getName());
		panelDetails.removeAll();
		panelButtons.removeAll();
		
		if (o.type.equals("Receiver")) {	
			panelDetails.add(new PanelReceiver((Receiver)o.object));
			panelButtons.add(buAddToFeedInfo);
		}
		this.validate();
		//panelDetails.repaint();
		//panelButtons.repaint();
	}
	
	public void initComponents() {
		
		panelNorth = new JPanel();
		labelDataDir = new JLabel("data path");
		textDataDir = new JTextField(dataDirectory.getAbsolutePath());
		buSelectDataDir = new JButton("select");
		buReadData = new JButton("read data");
		
		buAddToFeedInfo = new JButton("set in feedinfo");
		
		panelDetails = new JPanel();
		panelDetails.setLayout(new BorderLayout());
		
		panelButtons = new JPanel();
		FlowLayout fl = new FlowLayout();
		fl.setAlignment(FlowLayout.LEFT);
		panelButtons.setLayout(fl);
		
		table = new JTable();
		updateTableModel();
		scrollTable = new JScrollPane(table);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					table_selection_changed(table.getSelectedRow());
				}
			}
		});
		split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollTable, new JScrollPane(panelDetails));
	}
	
	public void initLayout() {
		
		GridBagLayout gbl = new GridBagLayout();
		panelNorth.setLayout(gbl);
		GridBagConstraints gbc = new GridBagConstraints();

		// Component: label
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(labelDataDir,gbc);
		panelNorth.add(labelDataDir);

		// Component: text
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(textDataDir,gbc);
		panelNorth.add(textDataDir);
		
		// Component: bu select
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(buSelectDataDir,gbc);
		panelNorth.add(buSelectDataDir);
		
		// Component: bu read
		gbc.gridx = 3;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(buReadData,gbc);
		panelNorth.add(buReadData);
		
		Dimension d = new Dimension(120, 28);
		buSelectDataDir.setPreferredSize(d);
		buReadData.setPreferredSize(d);
		split.setDividerLocation(250);
		
		this.setLayout(new BorderLayout());
		this.add(panelNorth, BorderLayout.NORTH);
		this.add(split, BorderLayout.CENTER);
		this.add(panelButtons, BorderLayout.SOUTH);
	}
	
	private class DMIObject {
		public String type = "";
		public String description = "";
		public File fromFile = null;
		public BusinessObject object;
	}
}
