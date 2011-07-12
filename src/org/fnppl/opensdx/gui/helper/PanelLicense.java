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

import org.fnppl.opensdx.common.LicenseBasis;
import org.fnppl.opensdx.dmi.FeedGui;

import java.util.HashMap;
import java.util.Vector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PanelLicense extends JPanel implements MyObservable {

	//init fields
	private LicenseBasis lb = null;
	private DocumentChangeListener documentListener;
	private KeyAdapter keyAdapter;
	private HashMap<String,JComponent> map = new HashMap<String, JComponent>();

	private JCheckBox check_as_on_bundle;
	private JLabel label_timeframe_from;
	private JTextField text_timeframe_from_datetime;
	private JLabel label_timeframe_to;
	private JTextField text_timeframe_to_datetime;
	private JLabel label_pricing;
	private JComboBox select_pricing;
	private DefaultComboBoxModel select_pricing_model;
	private JTextField text_pricing;
	private JLabel label_h_filler;
	private JCheckBox check_streaming_allowed;
	private JLabel label_channels;
	private JComboBox select_channels;
	private DefaultComboBoxModel select_channels_model;
	private JPanel panel_territories;
	private JLabel label_filler;


	public PanelLicense(LicenseBasis lb) {
		this.lb = lb;
		initKeyAdapter();
		initComponents();
		initLayout();
	}



	public void update(LicenseBasis lb) {
		this.lb = lb;
		
		if (lb == null) {;
			check_as_on_bundle.setSelected(false);
			text_timeframe_from_datetime.setText("");
			text_timeframe_to_datetime.setText("");
			select_pricing.setSelectedItem(0);
			text_pricing.setText("");
			check_streaming_allowed.setSelected(false);
			select_channels.setSelectedItem(0);
			//panel_territories.update());
		}
	}


	private void initKeyAdapter() {
		keyAdapter = new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					if (e.getComponent() instanceof JTextField) {
						try {
							JTextComponent text = (JTextComponent)e.getComponent();
							String t = text.getText();
							String name = text.getName();
							if (documentListener.formatOK(name,t)) {
								text_changed(text);
								documentListener.saveState(text);
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
				else if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					if (e.getComponent() instanceof JTextField) {
						JTextField text = (JTextField)e.getComponent();
						text.setText(documentListener.getSavedText(text));
						text.setBackground(Color.WHITE);
					}
				}
			}
		};
	}

	private void initComponents() {
		Vector<JTextComponent> texts = new Vector<JTextComponent>();
		setBorder(new TitledBorder("Information"));

		check_as_on_bundle = new JCheckBox("as on bundle");
		map.put("check_as_on_bundle", check_as_on_bundle);
		check_as_on_bundle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				check_as_on_bundle_changed(check_as_on_bundle.isSelected());
			}
		});

		label_timeframe_from = new JLabel("timeframe from");

		text_timeframe_from_datetime = new JTextField("");

		text_timeframe_from_datetime.setName("text_timeframe_from_datetime");
		map.put("text_timeframe_from_datetime", text_timeframe_from_datetime);
		texts.add(text_timeframe_from_datetime);

		label_timeframe_to = new JLabel("timeframe until");

		text_timeframe_to_datetime = new JTextField("");

		text_timeframe_to_datetime.setName("text_timeframe_to_datetime");
		map.put("text_timeframe_to_datetime", text_timeframe_to_datetime);
		texts.add(text_timeframe_to_datetime);

		label_pricing = new JLabel("pricing");

		select_pricing = new JComboBox();
		select_pricing_model = new DefaultComboBoxModel();
		select_pricing.setModel(select_pricing_model);
		init_select_pricing_model();
		map.put("select_pricing", select_pricing);
		select_pricing.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				select_pricing_changed(select_pricing.getSelectedIndex());
			}
		});

		text_pricing = new JTextField("");

		text_pricing.setName("text_pricing");
		map.put("text_pricing", text_pricing);
		texts.add(text_pricing);

		label_h_filler = new JLabel("");

		check_streaming_allowed = new JCheckBox("streaming allowed");
		map.put("check_streaming_allowed", check_streaming_allowed);
		check_streaming_allowed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				check_streaming_allowed_changed(check_streaming_allowed.isSelected());
			}
		});

		label_channels = new JLabel("channels");

		select_channels = new JComboBox();
		select_channels_model = new DefaultComboBoxModel();
		select_channels.setModel(select_channels_model);
		init_select_channels_model();
		map.put("select_channels", select_channels);
		select_channels.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				select_channels_changed(select_channels.getSelectedIndex());
			}
		});

		panel_territories = new JPanel();
		panel_territories.setBorder(new TitledBorder("panel_territories")); //TODO dummy

		label_filler = new JLabel("");

		documentListener = new DocumentChangeListener(texts);
		for (JTextComponent text : texts) {
			text.getDocument().addDocumentListener(documentListener);
			if (text instanceof JTextField) text.addKeyListener(keyAdapter);
		}
		documentListener.saveStates();
		
	}



	public void updateDocumentListener() {
		documentListener.saveStates();
	}

	public void updateDocumentListener(JTextComponent t) {
	documentListener.saveState(t);
	}
	public JComponent getComponent(String name) {
		return map.get(name);
	}
	public void setText(String name, String value) {
		JComponent c = map.get(name);
		if (c!=null && c instanceof JTextComponent) {
			((JTextComponent)c).setText(value);
		}
	}

	public String getText(String name) {
	JComponent c = map.get(name);
		if (c!=null && c instanceof JTextComponent) {
			return ((JTextComponent)c).getText();
		}
		return null;
	}


	public void setCheck(String name, boolean value) {
		JComponent c = map.get(name);
		if (c!=null && c instanceof JCheckBox) {
			((JCheckBox)c).setSelected(value);
		}
	}

	public boolean getCheck(String name) {
	JComponent c = map.get(name);
		if (c!=null && c instanceof JCheckBox) {
			return ((JCheckBox)c).isSelected();
		}
		throw new RuntimeException("name "+name+" not a JCheckBox");
	}


public void initLayout() {
	GridBagLayout gbl = new GridBagLayout();
	setLayout(gbl);
	GridBagConstraints gbc = new GridBagConstraints();



	// Component: check_as_on_bundle
	gbc.gridx = 0;
	gbc.gridy = 0;
	gbc.gridwidth = 2;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(check_as_on_bundle,gbc);
	add(check_as_on_bundle);

	// Component: label_timeframe_from
	gbc.gridx = 0;
	gbc.gridy = 1;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(label_timeframe_from,gbc);
	add(label_timeframe_from);

	// Component: text_timeframe_from_datetime
	gbc.gridx = 1;
	gbc.gridy = 1;
	gbc.gridwidth = 2;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(text_timeframe_from_datetime,gbc);
	add(text_timeframe_from_datetime);

	// Component: label_timeframe_to
	gbc.gridx = 0;
	gbc.gridy = 2;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(label_timeframe_to,gbc);
	add(label_timeframe_to);

	// Component: text_timeframe_to_datetime
	gbc.gridx = 1;
	gbc.gridy = 2;
	gbc.gridwidth = 2;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(text_timeframe_to_datetime,gbc);
	add(text_timeframe_to_datetime);

	// Component: label_pricing
	gbc.gridx = 0;
	gbc.gridy = 3;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(label_pricing,gbc);
	add(label_pricing);

	// Component: select_pricing
	gbc.gridx = 1;
	gbc.gridy = 3;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 40.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(select_pricing,gbc);
	add(select_pricing);

	// Component: text_pricing
	gbc.gridx = 2;
	gbc.gridy = 3;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 40.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(text_pricing,gbc);
	add(text_pricing);

	// Component: label_h_filler
	gbc.gridx = 3;
	gbc.gridy = 3;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 20.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(label_h_filler,gbc);
	add(label_h_filler);

	// Component: check_streaming_allowed
	gbc.gridx = 0;
	gbc.gridy = 4;
	gbc.gridwidth = 2;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(check_streaming_allowed,gbc);
	add(check_streaming_allowed);

	// Component: label_channels
	gbc.gridx = 0;
	gbc.gridy = 5;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(label_channels,gbc);
	add(label_channels);

	// Component: select_channels
	gbc.gridx = 1;
	gbc.gridy = 5;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(select_channels,gbc);
	add(select_channels);

	// Component: panel_territories
	gbc.gridx = 0;
	gbc.gridy = 6;
	gbc.gridwidth = 3;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(panel_territories,gbc);
	add(panel_territories);

	// Component: label_filler
	gbc.gridx = 0;
	gbc.gridy = 7;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 50.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(label_filler,gbc);
	add(label_filler);
		JLabel filler = new JLabel();
}


// ----- action methods --------------------------------
	public void check_as_on_bundle_changed(boolean selected) {
		//TODO
	}
	public void init_select_pricing_model() {
		//TODO
	}
	public void select_pricing_changed(int selected) {
		//TODO
	}
	public void check_streaming_allowed_changed(boolean selected) {
		//TODO
	}
	public void init_select_channels_model() {
		//TODO
	}
	public void select_channels_changed(int selected) {
		//TODO
	}
	public void text_changed(JTextComponent text) {
		//TODO
		String t = text.getText();
		if (text == text_timeframe_from_datetime) {
			
		}
		else if (text == text_timeframe_to_datetime) {
			
		}
		else if (text == text_pricing) {
			
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
}