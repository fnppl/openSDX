package org.fnppl.opensdx.gui.helper;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Color;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.fnppl.opensdx.common.Bundle;
import org.fnppl.opensdx.common.Feed;
import org.fnppl.opensdx.common.Item;
import org.fnppl.opensdx.common.ItemFile;
import org.fnppl.opensdx.dmi.BundleItemStructuredName;
import org.fnppl.opensdx.gui.Dialogs;

import java.util.HashMap;
import java.util.Vector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

public class PanelFiles extends JPanel implements MyObservable, MyObserver {

	//init fields
	private HashMap<String,JComponent> map = new HashMap<String, JComponent>();

	private Feed feed = null;
	private Bundle bundle = null;
	private Item item = null;
	private JList list_files;
	private JScrollPane scroll_list_files;
	private DefaultListModel list_files_model;
	private JButton bu_add;
	private JButton bu_remove;
	private PanelFileProperties panel_properties;
	private File lastDir = null;
	
	public PanelFiles() {
		initComponents();
		initLayout();
		update((Item)null, null);
	}

	public void update(Item item, Feed feed){
		this.item = item;
		this.bundle = null;
		this.feed = feed;
		int sel = list_files.getSelectedIndex();
		updateFileList();
		if (sel>=0) {
			int anz = 0;
			if (item!=null) {
				anz = item.getFilesCount();
			}
			if (sel<anz) {
				list_files.setSelectedIndex(sel);
			}
		}
		updateProperties();
	}

	public void update(Bundle bundle, Feed feed){
		this.bundle = bundle;
		this.item = null;
		this.feed = feed;
		int sel = list_files.getSelectedIndex();
		updateFileList();
		if (sel>=0) {
			int anz = 0;
			if (bundle!=null) {
				anz = bundle.getFilesCount();
			}
			if (sel<anz) {
				list_files.setSelectedIndex(sel);
			}
		}
		updateProperties();
	}

	public void setTypeBundle() {
		panel_properties.setTypeBundle();
	}

	public void setTypeItem() {
		panel_properties.setTypeItem();
	}
	
	private void updateFileList() {
		list_files_model.removeAllElements();

		if (bundle!=null) {
			int anz = bundle.getFilesCount();
			for (int i = 0; i < anz; i++) {
				String name = bundle.getFile(i).getOriginLocationPath();
				if (name==null || name.length()==0) {
					name = bundle.getFile(i).getLocationPath();
				}
				list_files_model.addElement("File: "+name);
			}
		}
		if (item!=null) {
			int anz = item.getFilesCount();
			for (int i = 0; i < anz; i++) {
				String name = item.getFile(i).getOriginLocationPath();
				if (name==null || name.length()==0) {
					name = item.getFile(i).getLocationPath();
				}
				list_files_model.addElement("File: "+name);
			}
		}
		list_files.setModel(list_files_model);
		list_files.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//list_files.setSelectedIndex(0);

	}

	private void updateProperties() {
		ItemFile file = getSelectedFile();
		String structuredName = "";
		if (file!=null && feed!=null) {
			BundleItemStructuredName sn = feed.getStructuredFilename(file);
			if (sn!=null) {
				structuredName = sn.new_filename;
			}
		}
		panel_properties.update(file, structuredName);
		if (file==null) {
			panel_properties.setVisible(false);
		} else {
			panel_properties.setVisible(true);	
		}
	}

	private ItemFile getSelectedFile() {
		int sel = list_files.getSelectedIndex();
		if (sel>=0) {
			if (bundle!=null && sel<bundle.getFilesCount()) {
				return bundle.getFile(sel);
			}
			if (item!=null && sel<item.getFilesCount()) {
				return item.getFile(sel);
			}
		}
		return null;
	}

	private void initComponents() {
		Vector<JTextComponent> texts = new Vector<JTextComponent>();
		
		list_files = new JList();
		list_files_model = new DefaultListModel();
		list_files.setModel(list_files_model);
		init_list_files_model();
		map.put("list_files", list_files);
		list_files.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				list_files_changed(list_files.getSelectedIndex());
			}
		});
		scroll_list_files = new JScrollPane(list_files);
		
		bu_add = new JButton("add");
		map.put("bu_add", bu_add);
		bu_add.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bu_add_clicked();
			}
		});

		bu_remove = new JButton("remove");
		map.put("bu_remove", bu_remove);
		bu_remove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bu_remove_clicked();
			}
		});


		panel_properties = new PanelFileProperties();
		panel_properties.addObserver(this);
	}

	public JComponent getComponent(String name) {
		return map.get(name);
	}

	public void initLayout() {

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		JPanel north = new JPanel();
		north.setBorder(new TitledBorder("List of Files"));
		
		GridBagLayout gbl = new GridBagLayout();
		north.setLayout(gbl);
		GridBagConstraints gbc = new GridBagConstraints();

		Container spacer0 = new Container();
		Container spacer1 = new Container();


		// Component: scroll_list_files
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 3;
		gbc.weightx = 100.0;
		gbc.weighty = 100.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(scroll_list_files,gbc);
		north.add(scroll_list_files);

		// Component: spacer0
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(spacer0,gbc);
		north.add(spacer0);

		// Component: bu_add
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(bu_add,gbc);
		north.add(bu_add);

		// Component: spacer1
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(spacer1,gbc);
		north.add(spacer1);

		// Component: bu_remove
		gbc.gridx = 2;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(bu_remove,gbc);
		north.add(bu_remove);

		int h = 200;
		north.setPreferredSize(new Dimension(800,h));
		north.setMinimumSize(new Dimension(200,h));
		north.setMaximumSize(new Dimension(2000,h));

		this.add(north);
		this.add(panel_properties);

	}

	// ----- action methods --------------------------------
	public void init_list_files_model() {

	}
	public void list_files_changed(int selected) {
		updateProperties();
	}
	public void bu_add_clicked() {
		if (bundle==null && item==null) return;

		File path = null;
		ItemFile selFile = getSelectedFile();
		if (selFile!=null) {
			try {
				path = new File(selFile.getOriginLocationPath()).getParentFile();
			} catch (Exception ex) {
			}
		}
		if (path==null) path = lastDir;
		File f = Dialogs.chooseOpenFile("Choose file", path, "");
		if (f==null || !f.exists() || f.isDirectory()) return;
		lastDir = f.getParentFile();
		
		ItemFile file = ItemFile.make(f);
		if (panel_properties.isTypeItem()) {
			file.type("full");
		} else {
			file.calculateDimensionFromFile();
		}
		
		if (bundle!=null) {
			bundle.addFile(file);
			updateFileList();
			list_files.setSelectedIndex(bundle.getFilesCount()-1);
			updateProperties();
		}
		if (item!=null) {
			item.addFile(file);
			updateFileList();
			list_files.setSelectedIndex(item.getFilesCount()-1);
			updateProperties();
		}
		notifyChanges();
	}
	public void bu_remove_clicked() {
		if (bundle==null && item==null) return;
		int sel = list_files.getSelectedIndex();
		if (bundle!=null) {
			if (sel>=0 && sel < bundle.getFilesCount()) {
				bundle.removeFile(sel);
			}
			updateFileList();
			if (sel<bundle.getFilesCount()) {
				list_files.setSelectedIndex(sel);
			} else if (bundle.getFilesCount()>0) {
				list_files.setSelectedIndex(bundle.getFilesCount()-1);
			}
			notifyChanges();
		}
		if (item!=null) {
			if (sel>=0 && sel < item.getFilesCount()) {
				item.removeFile(sel);
			}
			updateFileList();
			if (sel<item.getFilesCount()) {
				list_files.setSelectedIndex(sel);
			} else if (item.getFilesCount()>0) {
				list_files.setSelectedIndex(item.getFilesCount()-1);
			}
			notifyChanges();
		}
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

	//observer
	public void notifyChange(MyObservable changedIn) {
		notifyChanges();
	}
}
