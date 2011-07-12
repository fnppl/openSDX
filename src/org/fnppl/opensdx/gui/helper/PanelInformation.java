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

import org.fnppl.opensdx.common.BundleInformation;
import org.fnppl.opensdx.dmi.FeedGui;

import java.util.HashMap;
import java.util.Vector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PanelInformation extends JPanel implements MyObservable {

	//init fields
	private BundleInformation info = null;
	private DocumentChangeListener documentListener;
	private KeyAdapter keyAdapter;
	private HashMap<String,JComponent> map = new HashMap<String, JComponent>();

	private JLabel label_physical_release_datetime;
	private JTextField text_physical_release_datetime;
	private JLabel label_digital_release_datetime;
	private JTextField text_digital_release_datetime;
	private JLabel label_playlength_integer;
	private JTextField text_playlength_integer;
	private JList list_language;
	private DefaultListModel list_language_model;
	private JTextArea text_promotion;
	private JButton bu_promotion_update;
	private JButton bu_promotion_reset;
	private JLabel label_filler1;
	private JLabel label_filler2;
	private JLabel label_h_spacer;
	private JButton bu_lang_add;
	private JButton bu_lan_remove;
	private JTextArea text_teaser;
	private JButton bu_teaser_update;
	private JButton bu_teaser_reset;
	private JLabel label_filler;


	public PanelInformation(BundleInformation info) {
		this.info = info;
		initKeyAdapter();
		initComponents();
		initLayout();
	}



	public void update(BundleInformation info) {
		this.info = info;
		if (info == null) {;
			text_physical_release_datetime.setText("");
			text_digital_release_datetime.setText("");
			text_playlength_integer.setText("");
			updateLanguageList();
			updatePromoAndTeaserText();
		} else {
			text_physical_release_datetime.setText(info.getPhysicalReleaseDatetimeText());
			text_digital_release_datetime.setText(info.getDigitalReleaseDatetimeText());
			if (info.hasPlaylength()) {
				text_playlength_integer.setText(""+info.getPlaylength());
			} else {
				text_playlength_integer.setText("");
			}
			updateLanguageList();
			updatePromoAndTeaserText();
		}
		documentListener.saveStates();
	}
	
	private void updateLanguageList() {
		text_promotion.setText("");
		text_teaser.setText("");
	}
	
	private void updatePromoAndTeaserText() {
		text_promotion.setText("");
		text_teaser.setText("");
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

		label_physical_release_datetime = new JLabel("physical release date");

		text_physical_release_datetime = new JTextField("");

		text_physical_release_datetime.setName("text_physical_release_datetime");
		map.put("text_physical_release_datetime", text_physical_release_datetime);
		texts.add(text_physical_release_datetime);

		label_digital_release_datetime = new JLabel("digital release date");

		text_digital_release_datetime = new JTextField("");

		text_digital_release_datetime.setName("text_digital_release_datetime");
		map.put("text_digital_release_datetime", text_digital_release_datetime);
		texts.add(text_digital_release_datetime);

		label_playlength_integer = new JLabel("playlength in seconds");

		text_playlength_integer = new JTextField("");

		text_playlength_integer.setName("text_playlength_integer");
		map.put("text_playlength_integer", text_playlength_integer);
		texts.add(text_playlength_integer);

		list_language = new JList();
		list_language.setBorder(new TitledBorder("Languages"));
		list_language_model = new DefaultListModel();
		list_language.setModel(list_language_model);
		init_list_language_model();
		map.put("list_language", list_language);
		list_language.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				list_language_changed(list_language.getSelectedIndex());
			}
		});

		text_promotion = new JTextArea("");
		text_promotion.setBorder(new TitledBorder("Teaser Text"));
		
		text_promotion.setName("text_promotion");
		map.put("text_promotion", text_promotion);
		texts.add(text_promotion);

		bu_promotion_update = new JButton("update");
		map.put("bu_promotion_update", bu_promotion_update);
		bu_promotion_update.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bu_promotion_update_clicked();
			}
		});

		bu_promotion_reset = new JButton("reset");
		map.put("bu_promotion_reset", bu_promotion_reset);
		bu_promotion_reset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bu_promotion_reset_clicked();
			}
		});

		label_filler1 = new JLabel("");

		label_filler2 = new JLabel("");

		label_h_spacer = new JLabel("");
		label_h_spacer.setPreferredSize(new Dimension(20,20));

		bu_lang_add = new JButton("add");
		map.put("bu_lang_add", bu_lang_add);
		bu_lang_add.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bu_lang_add_clicked();
			}
		});

		bu_lan_remove = new JButton("remove");
		map.put("bu_lan_remove", bu_lan_remove);
		bu_lan_remove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bu_lan_remove_clicked();
			}
		});

		text_teaser = new JTextArea("");
		text_teaser.setBorder(new TitledBorder("Teaser Text"));
		
		text_teaser.setName("text_teaser");
		map.put("text_teaser", text_teaser);
		texts.add(text_teaser);

		bu_teaser_update = new JButton("update");
		map.put("bu_teaser_update", bu_teaser_update);
		bu_teaser_update.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bu_teaser_update_clicked();
			}
		});

		bu_teaser_reset = new JButton("reset");
		map.put("bu_teaser_reset", bu_teaser_reset);
		bu_teaser_reset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bu_teaser_reset_clicked();
			}
		});

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


public void initLayout() {
	GridBagLayout gbl = new GridBagLayout();
	setLayout(gbl);
	GridBagConstraints gbc = new GridBagConstraints();

	Container spacer0 = new Container();
	Container spacer1 = new Container();
	Container spacer2 = new Container();
	Container spacer3 = new Container();


	// Component: label_physical_release_datetime
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
	gbl.setConstraints(label_physical_release_datetime,gbc);
	add(label_physical_release_datetime);

	// Component: text_physical_release_datetime
	gbc.gridx = 2;
	gbc.gridy = 0;
	gbc.gridwidth = 3;
	gbc.gridheight = 1;
	gbc.weightx = 30.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(text_physical_release_datetime,gbc);
	add(text_physical_release_datetime);

	// Component: label_digital_release_datetime
	gbc.gridx = 0;
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
	gbl.setConstraints(label_digital_release_datetime,gbc);
	add(label_digital_release_datetime);

	// Component: text_digital_release_datetime
	gbc.gridx = 2;
	gbc.gridy = 1;
	gbc.gridwidth = 3;
	gbc.gridheight = 1;
	gbc.weightx = 30.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(text_digital_release_datetime,gbc);
	add(text_digital_release_datetime);

	// Component: label_playlength_integer
	gbc.gridx = 0;
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
	gbl.setConstraints(label_playlength_integer,gbc);
	add(label_playlength_integer);

	// Component: text_playlength_integer
	gbc.gridx = 2;
	gbc.gridy = 2;
	gbc.gridwidth = 3;
	gbc.gridheight = 1;
	gbc.weightx = 30.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(text_playlength_integer,gbc);
	add(text_playlength_integer);

	// Component: list_language
	gbc.gridx = 0;
	gbc.gridy = 3;
	gbc.gridwidth = 2;
	gbc.gridheight = 3;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(list_language,gbc);
	add(list_language);

	// Component: text_promotion
	gbc.gridx = 2;
	gbc.gridy = 3;
	gbc.gridwidth = 5;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(text_promotion,gbc);
	add(text_promotion);

	// Component: bu_promotion_update
	gbc.gridx = 2;
	gbc.gridy = 4;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(bu_promotion_update,gbc);
	add(bu_promotion_update);

	// Component: bu_promotion_reset
	gbc.gridx = 3;
	gbc.gridy = 4;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(bu_promotion_reset,gbc);
	add(bu_promotion_reset);

	// Component: label_filler1
	gbc.gridx = 4;
	gbc.gridy = 4;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 30.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(label_filler1,gbc);
	add(label_filler1);

	// Component: label_filler2
	gbc.gridx = 5;
	gbc.gridy = 4;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 70.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(label_filler2,gbc);
	add(label_filler2);

	// Component: spacer0
	gbc.gridx = 2;
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
	gbl.setConstraints(spacer0,gbc);
	add(spacer0);

	// Component: spacer1
	gbc.gridx = 3;
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
	gbl.setConstraints(spacer1,gbc);
	add(spacer1);

	// Component: label_h_spacer
	gbc.gridx = 4;
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
	gbl.setConstraints(label_h_spacer,gbc);
	add(label_h_spacer);

	// Component: bu_lang_add
	gbc.gridx = 0;
	gbc.gridy = 6;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.NORTH;
	gbc.fill = GridBagConstraints.NONE;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(bu_lang_add,gbc);
	add(bu_lang_add);

	// Component: bu_lan_remove
	gbc.gridx = 1;
	gbc.gridy = 6;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.NORTH;
	gbc.fill = GridBagConstraints.NONE;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(bu_lan_remove,gbc);
	add(bu_lan_remove);

	// Component: text_teaser
	gbc.gridx = 2;
	gbc.gridy = 6;
	gbc.gridwidth = 5;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(text_teaser,gbc);
	add(text_teaser);

	// Component: spacer2
	gbc.gridx = 0;
	gbc.gridy = 7;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(spacer2,gbc);
	add(spacer2);

	// Component: spacer3
	gbc.gridx = 1;
	gbc.gridy = 7;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(spacer3,gbc);
	add(spacer3);

	// Component: bu_teaser_update
	gbc.gridx = 2;
	gbc.gridy = 7;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(bu_teaser_update,gbc);
	add(bu_teaser_update);

	// Component: bu_teaser_reset
	gbc.gridx = 3;
	gbc.gridy = 7;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(bu_teaser_reset,gbc);
	add(bu_teaser_reset);

	// Component: label_filler
	gbc.gridx = 0;
	gbc.gridy = 8;
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
	public void init_list_language_model() {
		list_language_model = new DefaultListModel();
	}
	public void list_language_changed(int selected) {
		updatePromoAndTeaserText();
	}
	public void bu_lang_add_clicked() {
		//TODO
	}
	public void bu_lan_remove_clicked() {
		//TODO
	}
	public void bu_promotion_update_clicked() {
		//TODO
	}
	public void bu_promotion_reset_clicked() {
		//TODO
	}
	public void bu_teaser_update_clicked() {
		//TODO
	}
	public void bu_teaser_reset_clicked() {
		//TODO
	}
	
	public void text_changed(JTextComponent text) {
		//TODO
		String t = text.getText();
		if (text == text_physical_release_datetime) {
			
		}
		else if (text == text_digital_release_datetime) {
			
		}
		else if (text == text_playlength_integer) {
			
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
