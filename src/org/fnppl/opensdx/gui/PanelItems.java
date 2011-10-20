package org.fnppl.opensdx.gui;

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
import org.fnppl.opensdx.common.BundleInformation;
import org.fnppl.opensdx.common.IDs;
import org.fnppl.opensdx.common.Item;
import org.fnppl.opensdx.common.LicenseBasis;
import org.fnppl.opensdx.gui.helper.MyObservable;
import org.fnppl.opensdx.gui.helper.MyObserver;

import java.util.HashMap;
import java.util.Vector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class PanelItems extends JPanel implements MyObservable, MyObserver {

	//init fields
	private HashMap<String,JComponent> map = new HashMap<String, JComponent>();

	private Bundle bundle = null;
	private JList list_items;
	private JScrollPane scroll_list_items;
	private DefaultListModel list_items_model;
	private JButton bu_add;
	private JButton bu_remove;
	private PanelItem panel_item;

	public PanelItems() {
		initComponents();
		initLayout();
		update((Bundle)null);
	}

	public void update(Bundle bundle){
		this.bundle = bundle;
		int sel = list_items.getSelectedIndex();
		updateItemList();
		if (sel>=0) {
			int anz = 0;
			if (bundle!=null) {
				anz = bundle.getItemsCount();
			}
			if (sel<anz) {
				list_items.setSelectedIndex(sel);
			}
		}
		updateItem();
	}

	private void updateItemList() {
		list_items_model.removeAllElements();
		if (bundle!=null) {
			int anz = bundle.getItemsCount();
			for (int i = 0; i < anz; i++) {
				list_items_model.addElement("Item: "+bundle.getItem(i).getDisplayname());
			}
		}
		list_items.setModel(list_items_model);
		list_items.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

	}
	
	private void checkItemList() {
		if (bundle!=null) {
			int anz = Math.min(bundle.getItemsCount(), list_items_model.getSize());
			for (int i = 0; i < anz; i++) {
				list_items_model.set(i, "Item: "+bundle.getItem(i).getDisplayname());
			}
		}
	}

	private void updateItem() {
		Item item = getSelectedItem();
		panel_item.update(item,bundle);
		if (item==null) {
			panel_item.setVisible(false);
		} else {
			panel_item.setVisible(true);	
		}
	}

	private Item getSelectedItem() {
		int sel = list_items.getSelectedIndex();
		if (sel>=0) {
			if (bundle!=null && sel<bundle.getItemsCount()) {
				return bundle.getItem(sel);
			}
		}
		return null;
	}

	private void initComponents() {
		list_items = new JList();
		list_items_model = new DefaultListModel();
		list_items.setModel(list_items_model);
		init_list_items_model();
		map.put("list_items", list_items);
		list_items.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				list_items_changed(list_items.getSelectedIndex());
			}
		});
		scroll_list_items = new JScrollPane(list_items);
		
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


		panel_item = new PanelItem();
		panel_item.addObserver(this);
	}

	public JComponent getComponent(String name) {
		return map.get(name);
	}

	public void initLayout() {

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		JPanel north = new JPanel();
		north.setBorder(new TitledBorder("List of Items"));

		GridBagLayout gbl = new GridBagLayout();
		north.setLayout(gbl);
		GridBagConstraints gbc = new GridBagConstraints();

		Container spacer0 = new Container();
		Container spacer1 = new Container();


		// Component: scroll_list_items
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
		gbl.setConstraints(scroll_list_items,gbc);
		north.add(scroll_list_items);

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
		this.add(panel_item);

	}

	// ----- action methods --------------------------------
	public void init_list_items_model() {

	}
	public void list_items_changed(int selected) {
		updateItem();
	}
	public void bu_add_clicked() {
		if (bundle==null) return;
		long now = System.currentTimeMillis();
		Item newItem = Item.make(IDs.make(), "new item", "", "", "[not specified]", "", BundleInformation.make(now,now), LicenseBasis.makeAsOnBundle(),null);
		bundle.addItem(newItem);
		updateItemList();
		list_items.setSelectedIndex(list_items.getModel().getSize()-1);
		//panel_item.update(newItem, bundle);
		notifyChanges();
	}
	public void bu_remove_clicked() {
		if (bundle==null || bundle.getItemsCount()==0) return;
		int selItem = list_items.getSelectedIndex();
		if (selItem<0) return;
		bundle.removeItem(selItem);
		updateItemList();
		int s = list_items.getModel().getSize();
		if (s>0) {
			if (s>selItem) {
				list_items.setSelectedIndex(selItem);
			} else {
				list_items.setSelectedIndex(s-1);
			}
		} else {
			updateItem();
		}
		notifyChanges();
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
		if (changedIn == panel_item) {
			checkItemList();
		}
		notifyChanges();
	}
}
