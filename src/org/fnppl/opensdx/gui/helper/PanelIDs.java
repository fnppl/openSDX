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
import java.awt.KeyboardFocusManager;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.border.TitledBorder;

import org.fnppl.opensdx.common.IDs;
import org.fnppl.opensdx.dmi.FeedGui;
import org.fnppl.opensdx.dmi.FeedGuiTooltips;
import org.fnppl.opensdx.dmi.wayin.FinetunesToOpenSDXImporter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PanelIDs extends JPanel implements MyObservable, TextChangeListener {

	//init fields
	private IDs ids = null;
	//private DocumentChangeListener documentListener;
	//private KeyAdapter keyAdapter;
	private HashMap<String,JComponent> map = new HashMap<String, JComponent>();

	private JLabel label_gvl;
	private JTextField text_gvl;
	private JLabel label_grid;
	private JTextField text_grid;
	private JLabel label_upc;
	private JTextField text_upc;
	private JLabel label_isrc;
	private JTextField text_isrc;
	private JLabel label_contentauth;
	private JTextField text_contentauth;
	private JLabel label_labelordernum;
	private JTextField text_labelordernum;
	private JLabel label_amzn;
	private JTextField text_amzn;
	private JLabel label_isbn;
	private JTextField text_isbn;
	private JLabel label_finetunes;
	private JTextField text_finetunes;
	private JLabel label_licensor;
	private JTextField text_licensor;
	private JLabel label_licensee;
	private JTextField text_licensee;
	private JLabel label_amg;
	private JTextField text_amg;
	
	private JLabel label_filler;
	private Vector<JTextComponent> texts = new Vector<JTextComponent>();
	private Vector<JLabel> labels = new Vector<JLabel>();

	public PanelIDs(IDs ids) {
		this.ids = ids;
		initFocusTraversal();
		initComponents();
		text_licensor.setPreferredSize(new Dimension(150,(int)text_licensor.getPreferredSize().getHeight()));
		initLayout();
		initToolTips();
	}
	
	public void initToolTips() {
		label_gvl.setToolTipText(FeedGuiTooltips.idGVL);
		text_gvl.setToolTipText(FeedGuiTooltips.idGVL);
		label_grid.setToolTipText(FeedGuiTooltips.idGRID);
		text_grid.setToolTipText(FeedGuiTooltips.idGRID);
		label_upc.setToolTipText(FeedGuiTooltips.idUPC);
		text_upc.setToolTipText(FeedGuiTooltips.idUPC);
		label_isrc.setToolTipText(FeedGuiTooltips.idISRC);
		text_isrc.setToolTipText(FeedGuiTooltips.idISRC);
		label_contentauth.setToolTipText(FeedGuiTooltips.idContentAuth);
		text_contentauth.setToolTipText(FeedGuiTooltips.idContentAuth);
		label_labelordernum.setToolTipText(FeedGuiTooltips.idLabelOrderNum);
		text_labelordernum.setToolTipText(FeedGuiTooltips.idLabelOrderNum);
		label_amzn.setToolTipText(FeedGuiTooltips.idAmazon);
		text_amzn.setToolTipText(FeedGuiTooltips.idAmazon);
		label_isbn.setToolTipText(FeedGuiTooltips.idISBN);
		text_isbn.setToolTipText(FeedGuiTooltips.idISBN);
		label_finetunes.setToolTipText(FeedGuiTooltips.idFinetunes);
		text_finetunes.setToolTipText(FeedGuiTooltips.idFinetunes);
		label_licensor.setToolTipText(FeedGuiTooltips.idLicensor);
		text_licensor.setToolTipText(FeedGuiTooltips.idLicensor);
		label_licensee.setToolTipText(FeedGuiTooltips.idLicensee);
		text_licensee.setToolTipText(FeedGuiTooltips.idLicensee);
		label_amg.setToolTipText(FeedGuiTooltips.idAMG);
		text_amg.setToolTipText(FeedGuiTooltips.idAMG);
	}

	@SuppressWarnings("unchecked")
	private void initFocusTraversal() {
		Set forwardKeys = new HashSet(getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
		forwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
		setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,forwardKeys);
	}
	
	public void onlyShowFields(HashSet<String> show) {
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
			text_contentauth.setText("");
			text_labelordernum.setText("");
			text_amzn.setText("");
			text_isbn.setText("");
			text_finetunes.setText("");
			text_licensor.setText("");
			text_licensee.setText("");
		} else {
			text_gvl.setText(ids.getGvl());
			text_grid.setText(ids.getGrid());
			text_upc.setText(ids.getUpc());
			text_isrc.setText(ids.getIsrc());
			text_contentauth.setText(ids.getContentauth());
			text_labelordernum.setText(ids.getLabelordernum());
			text_amzn.setText(ids.getAmzn());
			text_isbn.setText(ids.getIsbn());
			text_finetunes.setText(ids.getFinetunes());
			text_licensor.setText(ids.getLicensor());
			text_licensee.setText(ids.getLicensee());
		}
		//documentListener.saveStates();
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

		label_contentauth = new JLabel("Content Auth");
		label_contentauth.setName("label_contentauth");
		labels.add(label_contentauth);
		text_contentauth = new JTextField("");

		text_contentauth.setName("text_contentauth");
		map.put("text_contentauth", text_contentauth);
		texts.add(text_contentauth);

		label_labelordernum = new JLabel("Label Order Num");
		label_labelordernum.setName("label_labelordernum");
		labels.add(label_labelordernum);
		text_labelordernum = new JTextField("");

		text_labelordernum.setName("text_labelordernum");
		map.put("text_labelordernum", text_labelordernum);
		texts.add(text_labelordernum);

		label_amzn = new JLabel("Amazon");
		label_amzn.setName("label_amzn");
		labels.add(label_amzn);
		text_amzn = new JTextField("");

		text_amzn.setName("text_amzn");
		map.put("text_amzn", text_amzn);
		texts.add(text_amzn);

		label_isbn = new JLabel("ISBN");
		label_isbn.setName("label_isbn");
		labels.add(label_isbn);
		
		text_isbn = new JTextField("");

		text_isbn.setName("text_isbn");
		map.put("text_isbn", text_isbn);
		texts.add(text_isbn);

		label_finetunes = new JLabel("finetunes");
		label_finetunes.setName("label_finetunes");
		labels.add(label_finetunes);
		text_finetunes = new JTextField("");

		text_finetunes.setName("text_finetunes");
		map.put("text_finetunes", text_finetunes);
		texts.add(text_finetunes);

		label_licensor = new JLabel("Licensor");
		label_licensor.setName("label_licensor");
		labels.add(label_licensor);
		text_licensor = new JTextField("");

		text_licensor.setName("text_licensor");
		map.put("text_licensor", text_licensor);
		texts.add(text_licensor);

		label_licensee = new JLabel("Licensee");
		label_licensee.setName("label_licensee");
		labels.add(label_licensee);
		text_licensee = new JTextField("");

		text_licensee.setName("text_licensee");
		map.put("text_licensee", text_licensee);
		texts.add(text_licensee);
		
		label_amg = new JLabel("AMG");
		label_amg.setName("label_amg");
		labels.add(label_amg);
		text_amg = new JTextField("");

		text_amg.setName("text_amg");
		map.put("text_amg", text_amg);
		texts.add(text_amg);

		label_filler = new JLabel("");

		DocumentInstantChangeListener chl = new DocumentInstantChangeListener(this);
		for (JTextComponent text : texts) {
			if (text instanceof JTextField) {
				chl.addTextComponent(text);
			}
		}
		
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

	// Component: label_contentauth
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
	gbl.setConstraints(label_contentauth,gbc);
	add(label_contentauth);

	// Component: text_contentauth
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
	gbl.setConstraints(text_contentauth,gbc);
	add(text_contentauth);

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

	// Component: label_amzn
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
	gbl.setConstraints(label_amzn,gbc);
	add(label_amzn);

	// Component: text_amzn
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
	gbl.setConstraints(text_amzn,gbc);
	add(text_amzn);

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

	// Component: label_finetunes
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
	gbl.setConstraints(label_finetunes,gbc);
	add(label_finetunes);

	// Component: text_finetunes
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
	gbl.setConstraints(text_finetunes,gbc);
	add(text_finetunes);

	// Component: label_licensor
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
	gbl.setConstraints(label_licensor,gbc);
	add(label_licensor);

	// Component: text_licensor
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
	gbl.setConstraints(text_licensor,gbc);
	add(text_licensor);

	// Component: label_licensee
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
	gbl.setConstraints(label_licensee,gbc);
	add(label_licensee);

	// Component: text_licensee
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
	gbl.setConstraints(text_licensee,gbc);
	add(text_licensee);
	
	// Component: label_amg
	gbc.gridx = 0;
	gbc.gridy = 11;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(label_amg,gbc);
	add(label_amg);
	
	// Component: text_amg
	gbc.gridx = 1;
	gbc.gridy = 11;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 50.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(text_amg,gbc);
	add(text_amg);

	// Component: label_filler
	gbc.gridx = 0;
	gbc.gridy = 12;
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
		if (t!=null && t.length()==0) {
			t = null;
		}
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
		else if (text == text_contentauth) {
			ids.contentauth(t);
		}
		else if (text == text_labelordernum) {
			ids.labelordernum(t);
		}
		else if (text == text_amzn) {
			ids.amzn(t);
		}
		else if (text == text_isbn) {
			ids.isbn(t);
		}
		else if (text == text_finetunes) {
			ids.finetunes(t);
		}
		else if (text == text_licensor) {
			ids.licensor(t);
		}
		else if (text == text_licensee) {
			ids.licensee(t);
		}
		else if (text == text_amg) {
			ids.amg(t);
		}
		notifyChanges();
		
		//text.requestFocusInWindow();
		//text.transferFocus();
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
