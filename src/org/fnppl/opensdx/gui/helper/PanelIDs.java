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

import org.fnppl.opensdx.common.IDs;
import org.fnppl.opensdx.dmi.FeedGui;
import org.fnppl.opensdx.dmi.wayin.FinetunesToOpenSDXImporter;

import java.util.HashMap;
import java.util.Vector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PanelIDs extends JPanel implements MyObservable {

	//init fields
	private IDs ids = null;
	private DocumentChangeListener documentListener;
	private KeyAdapter keyAdapter;
	private HashMap<String,JComponent> map = new HashMap<String, JComponent>();

	private JLabel label_gvl;
	private JTextField text_gvl;
	private JLabel label_grid;
	private JTextField text_grid;
	private JLabel label_upc;
	private JTextField text_upc;
	private JLabel label_isrc;
	private JTextField text_isrc;
	private JLabel label_contentauthid;
	private JTextField text_contentauthid;
	private JLabel label_labelordernum;
	private JTextField text_labelordernum;
	private JLabel label_amazon;
	private JTextField text_amazon;
	private JLabel label_isbn;
	private JTextField text_isbn;
	private JLabel label_finetunesid;
	private JTextField text_finetunesid;
	private JLabel label_ourid;
	private JTextField text_ourid;
	private JLabel label_yourid;
	private JTextField text_yourid;
	private JLabel label_filler;
	private Vector<JTextComponent> texts = new Vector<JTextComponent>();
	private Vector<JLabel> labels = new Vector<JLabel>();

	public PanelIDs(IDs ids) {
		this.ids = ids;
		initKeyAdapter();
		initComponents();
		text_ourid.setPreferredSize(new Dimension(150,(int)text_ourid.getPreferredSize().getHeight()));
		initLayout();
	}


	public void onlyShowFields(Vector<String> show) {
		for (JTextComponent t : texts) {
			if (show.contains(t.getName().substring(5))) {
				t.setVisible(true);
			} else {
				t.setVisible(false);
			}
		}
		for (JLabel l : labels) {
			if (show.contains(l.getName().substring(6))) {
				l.setVisible(true);
			} else {
				l.setVisible(false);
			}
		}
	}

	public void update(IDs ids) {
		this.ids = ids;
		if (ids == null) {;
			text_gvl.setText("");
			text_grid.setText("");
			text_upc.setText("");
			text_isrc.setText("");
			text_contentauthid.setText("");
			text_labelordernum.setText("");
			text_amazon.setText("");
			text_isbn.setText("");
			text_finetunesid.setText("");
			text_ourid.setText("");
			text_yourid.setText("");
		} else {
			text_gvl.setText(ids.getGvl());
			text_grid.setText(ids.getGrid());
			text_upc.setText(ids.getUpc());
			text_isrc.setText(ids.getIsrc());
			text_contentauthid.setText(ids.getContentauthid());
			text_labelordernum.setText(ids.getLabelordernum());
			text_amazon.setText(ids.getAmzn());
			text_isbn.setText(ids.getIsbn());
			text_finetunesid.setText(ids.getFinetunesid());
			text_ourid.setText(ids.getOurid());
			text_yourid.setText(ids.getYourid());
		}
		documentListener.saveStates();
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
		
		setBorder(new TitledBorder("IDs"));

		label_gvl = new JLabel("GVL");
		label_gvl.setName("label_gvl");
		labels.add(label_gvl);
		
		
		text_gvl = new JTextField("");
		text_gvl.setName("text_gvl");
		map.put("text_gvl", text_gvl);
		texts.add(text_gvl);

		label_grid = new JLabel("GRID");
		label_grid.setName("label_grid");
		labels.add(label_grid);
		
		text_grid = new JTextField("");
		text_grid.setName("text_grid");
		map.put("text_grid", text_grid);
		texts.add(text_grid);

		label_upc = new JLabel("UPC");
		label_upc.setName("label_upc");
		labels.add(label_upc);
		text_upc = new JTextField("");

		text_upc.setName("text_upc");
		map.put("text_upc", text_upc);
		texts.add(text_upc);

		label_isrc = new JLabel("ISRC");
		label_isrc.setName("label_isrc");
		labels.add(label_isrc);
		text_isrc = new JTextField("");

		text_isrc.setName("text_isrc");
		map.put("text_isrc", text_isrc);
		texts.add(text_isrc);

		label_contentauthid = new JLabel("Content Auth ID");
		label_contentauthid.setName("label_contentauthid");
		labels.add(label_contentauthid);
		text_contentauthid = new JTextField("");

		text_contentauthid.setName("text_contentauthid");
		map.put("text_contentauthid", text_contentauthid);
		texts.add(text_contentauthid);

		label_labelordernum = new JLabel("Label Order Num");
		label_labelordernum.setName("label_labelordernum");
		labels.add(label_labelordernum);
		text_labelordernum = new JTextField("");

		text_labelordernum.setName("text_labelordernum");
		map.put("text_labelordernum", text_labelordernum);
		texts.add(text_labelordernum);

		label_amazon = new JLabel("Amazon");
		label_amazon.setName("label_amazon");
		labels.add(label_amazon);
		text_amazon = new JTextField("");

		text_amazon.setName("text_amazon");
		map.put("text_amazon", text_amazon);
		texts.add(text_amazon);

		label_isbn = new JLabel("ISBN");
		label_isbn.setName("label_isbn");
		labels.add(label_isbn);
		
		text_isbn = new JTextField("");

		text_isbn.setName("text_isbn");
		map.put("text_isbn", text_isbn);
		texts.add(text_isbn);

		label_finetunesid = new JLabel("finetunes ID");
		label_finetunesid.setName("label_finetunesid");
		labels.add(label_finetunesid);
		text_finetunesid = new JTextField("");

		text_finetunesid.setName("text_finetunesid");
		map.put("text_finetunesid", text_finetunesid);
		texts.add(text_finetunesid);

		label_ourid = new JLabel("Our ID");
		label_ourid.setName("label_ourid");
		labels.add(label_ourid);
		text_ourid = new JTextField("");

		text_ourid.setName("text_ourid");
		map.put("text_ourid", text_ourid);
		texts.add(text_ourid);

		label_yourid = new JLabel("Your ID");
		label_yourid.setName("label_yourid");
		labels.add(label_yourid);
		text_yourid = new JTextField("");

		text_yourid.setName("text_yourid");
		map.put("text_yourid", text_yourid);
		texts.add(text_yourid);

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



	// Component: label_gvl
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
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(label_gvl,gbc);
	add(label_gvl);

	// Component: text_gvl
	gbc.gridx = 1;
	gbc.gridy = 0;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 50.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(text_gvl,gbc);
	add(text_gvl);

	// Component: label_grid
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
	gbl.setConstraints(label_grid,gbc);
	add(label_grid);

	// Component: text_grid
	gbc.gridx = 1;
	gbc.gridy = 1;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 50.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(text_grid,gbc);
	add(text_grid);

	// Component: label_upc
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
	gbl.setConstraints(label_upc,gbc);
	add(label_upc);

	// Component: text_upc
	gbc.gridx = 1;
	gbc.gridy = 2;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 50.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(text_upc,gbc);
	add(text_upc);

	// Component: label_isrc
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
	gbl.setConstraints(label_isrc,gbc);
	add(label_isrc);

	// Component: text_isrc
	gbc.gridx = 1;
	gbc.gridy = 3;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 50.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(text_isrc,gbc);
	add(text_isrc);

	// Component: label_contentauthid
	gbc.gridx = 0;
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
	gbl.setConstraints(label_contentauthid,gbc);
	add(label_contentauthid);

	// Component: text_contentauthid
	gbc.gridx = 1;
	gbc.gridy = 4;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 50.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(text_contentauthid,gbc);
	add(text_contentauthid);

	// Component: label_labelordernum
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
	gbl.setConstraints(label_labelordernum,gbc);
	add(label_labelordernum);

	// Component: text_labelordernum
	gbc.gridx = 1;
	gbc.gridy = 5;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 50.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(text_labelordernum,gbc);
	add(text_labelordernum);

	// Component: label_amazon
	gbc.gridx = 0;
	gbc.gridy = 6;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(label_amazon,gbc);
	add(label_amazon);

	// Component: text_amazon
	gbc.gridx = 1;
	gbc.gridy = 6;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 50.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(text_amazon,gbc);
	add(text_amazon);

	// Component: label_isbn
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
	gbl.setConstraints(label_isbn,gbc);
	add(label_isbn);

	// Component: text_isbn
	gbc.gridx = 1;
	gbc.gridy = 7;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 50.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(text_isbn,gbc);
	add(text_isbn);

	// Component: label_finetunesid
	gbc.gridx = 0;
	gbc.gridy = 8;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(label_finetunesid,gbc);
	add(label_finetunesid);

	// Component: text_finetunesid
	gbc.gridx = 1;
	gbc.gridy = 8;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 50.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(text_finetunesid,gbc);
	add(text_finetunesid);

	// Component: label_ourid
	gbc.gridx = 0;
	gbc.gridy = 9;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(label_ourid,gbc);
	add(label_ourid);

	// Component: text_ourid
	gbc.gridx = 1;
	gbc.gridy = 9;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 50.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(text_ourid,gbc);
	add(text_ourid);

	// Component: label_yourid
	gbc.gridx = 0;
	gbc.gridy = 10;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(label_yourid,gbc);
	add(label_yourid);

	// Component: text_yourid
	gbc.gridx = 1;
	gbc.gridy = 10;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 50.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(text_yourid,gbc);
	add(text_yourid);

	// Component: label_filler
	gbc.gridx = 0;
	gbc.gridy = 11;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 100.0;
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
	public void text_changed(JTextComponent text) {
		if (ids==null) return;
		String t = text.getText();
		if (text == text_gvl) {
			ids.gvl(t);
		}
		else if (text == text_grid) {
			ids.grid(t);
		}
		else if (text == text_upc) {
			ids.upc(t);
		}
		else if (text == text_isrc) {
			ids.isrc(t);
		}
		else if (text == text_contentauthid) {
			ids.contentauthid(t);
		}
		else if (text == text_labelordernum) {
			ids.labelordernum(t);
		}
		else if (text == text_amazon) {
			ids.amzn(t);
		}
		else if (text == text_isbn) {
			ids.isbn(t);
		}
		else if (text == text_finetunesid) {
			ids.finetunesid(t);
		}
		else if (text == text_ourid) {
			ids.ourid(t);
		}
		else if (text == text_yourid) {
			ids.yourid(t);
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
