package org.fnppl.opensdx.gui.helper;
/*
 * Copyright (C) 2010-2013 
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.fnppl.opensdx.common.BusinessStringItem;
import org.fnppl.opensdx.common.Checksums;
import org.fnppl.opensdx.common.Feed;
import org.fnppl.opensdx.common.FileLocation;
import org.fnppl.opensdx.common.ItemFile;
import org.fnppl.opensdx.dmi.BundleItemStructuredName;
import org.fnppl.opensdx.gui.Dialogs;
import org.fnppl.opensdx.security.SecurityHelper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

public class PanelFileProperties extends JPanel implements MyObservable, TextChangeListener {

	//init fields
	//private DocumentChangeListener documentListener;
	//private KeyAdapter keyAdapter;
	private HashMap<String,JComponent> map = new HashMap<String, JComponent>();

	private ItemFile file;
	private Feed feed = null;
	private JLabel label_path;
	private JTextField text_path;
	private JButton bu_change;
	private JLabel label_format;
	private JTextField text_format;	
	private JLabel label_samplerate;
	private JTextField text_samplerate;
	private JLabel label_samplesize;
	private JTextField text_samplesize;
	private JLabel label_bitrate;
	private JTextField text_bitrate;
	private JLabel label_bitratetype;
	private JTextField text_bitratetype;
	private JLabel label_codec;
	private JTextField text_codec;
	private JLabel label_codecsettings;
	private JTextField text_codecsettings;	
	private JLabel label_channels;
	private JComboBox select_channels;
	private DefaultComboBoxModel select_channels_model;
	private JLabel label_type;
	private JComboBox select_type;
	private DefaultComboBoxModel select_type_model;
	private JLabel label_hfiller;
	private JLabel label_length;
	private JTextField text_length;
	private JLabel label_width;
	private JTextField text_width_integer;
	private JLabel label_height;
	private JTextField text_height_integer;
	private JLabel label_md5;
	private JTextField text_md5;
	private JLabel label_sha1;
	private JTextField text_sha1;
	private JLabel label_structuredname;
	private JTextField text_structuredname;
	private JLabel label_filler;


	public PanelFileProperties() {
		initFocusTraversal();
		initComponents();
		initLayout();

		//text_path.setEditable(false);
		text_length.setEditable(false);
		text_md5.setEditable(false);
		text_sha1.setEditable(false);
		text_structuredname.setEditable(false);

		file = null;
		update(file,null);
	}
	
	@SuppressWarnings("unchecked")
	private void initFocusTraversal() {
		Set forwardKeys = new HashSet(getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
		forwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
		setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,forwardKeys);
	}

	public void setTypeBundle() {
		label_channels.setVisible(false);
		select_channels.setVisible(false);		
		label_samplerate.setVisible(false);
		text_samplerate.setVisible(false);
		label_samplesize.setVisible(false);
		text_samplesize.setVisible(false);
		label_bitrate.setVisible(false);
		text_bitrate.setVisible(false);
		label_bitratetype.setVisible(false);
		text_bitratetype.setVisible(false);
		label_codec.setVisible(false);
		text_codec.setVisible(false);
		label_codecsettings.setVisible(false);
		text_codecsettings.setVisible(false);
		
		//text_width.setVisible(false); //false as long as type not given
		//text_height.setVisible(false);
		
		select_type_model.removeAllElements();
		select_type_model.addElement("[not specified]");
		select_type_model.addElement("frontcover");
		select_type_model.addElement("backcover");
		select_type_model.addElement("booklet");
	}

	public void setTypeItem() {
		label_channels.setVisible(true);
		select_channels.setVisible(true);
		label_samplerate.setVisible(true);
		text_samplerate.setVisible(true);
		label_samplesize.setVisible(true);
		text_samplesize.setVisible(true);
		label_bitrate.setVisible(true);
		text_bitrate.setVisible(true);
		label_bitratetype.setVisible(true);
		text_bitratetype.setVisible(true);
		label_codec.setVisible(true);
		text_codec.setVisible(true);
		label_codecsettings.setVisible(true);
		text_codecsettings.setVisible(true);
		label_width.setVisible(false);
		label_height.setVisible(false);
		text_width_integer.setVisible(false);
		text_height_integer.setVisible(false);
		
		select_type_model.removeAllElements();
		select_type_model.addElement("[not specified]");
		select_type_model.addElement("full");
		select_type_model.addElement("pre-listening");
	}
	
	public boolean isTypeItem() {
		return label_channels.isVisible();
	}

	public void update(ItemFile file, Feed feed) {
		this.file = file;
		this.feed = feed;
		if (file == null) {
			text_path.setText("");
			text_format.setText("");
			select_channels.setSelectedItem(0);
			select_type.setSelectedItem(0);
			text_length.setText("");
			text_md5.setText("");
			text_sha1.setText("");
			text_samplerate.setText("");
			text_samplesize.setText("");
			text_bitrate.setText("");
			text_bitratetype.setText("");
			text_codec.setText("");
			text_codecsettings.setText("");
			text_structuredname.setText("");
			text_width_integer.setText("");
			text_height_integer.setText("");
			label_width.setVisible(false);
			label_height.setVisible(false);
			text_width_integer.setVisible(false);
			text_height_integer.setVisible(false);
		} else {
			text_path.setText(file.getOriginLocationPath());
			text_format.setText(file.getFiletype());
			select_channels.setSelectedItem(file.getChannels());
			String type = file.getType();
			if (type==null) {
				select_type.setSelectedIndex(0);		
			} else {
				select_type.setSelectedItem(file.getType());
			}
			text_length.setText(""+file.getBytes());
			Checksums c = file.getChecksums();
			if (c!=null) {
				text_md5.setText(c.getMd5String());
				text_sha1.setText(c.getSha1String());
			} else {
				text_md5.setText("");
				text_sha1.setText("");
			}
			text_samplerate.setText(file.getSamplerate());
			text_samplesize.setText(file.getSamplesize());
			text_bitrate.setText(file.getBitrate());
			text_bitratetype.setText(file.getBitratetype());
			text_codec.setText(file.getCodec());
			text_codecsettings.setText(file.getCodecsettings());
			String structuredName = "";
			if (feed!=null) {
				BundleItemStructuredName sn = feed.getStructuredFilename(file);
				if (sn!=null) {
					structuredName = sn.new_filename;
				}
			}
			text_structuredname.setText(structuredName);

			//dimension
			Integer w = file.getDimensionWidth();
			if (w==null) {
				text_width_integer.setText("");
			} else {
				text_width_integer.setText(""+w);
			}
			Integer h = file.getDimensionHeight();
			if (h==null) {
				text_height_integer.setText("");
			} else {
				text_height_integer.setText(""+h);
			}
			String ftype = file.getType();
			boolean dimVisible = false;
			if (h!=null || w!=null || ftype!=null && (ftype.equals("frontcover") || ftype.equals("backcover"))) {
				dimVisible = true;
			}
			label_width.setVisible(dimVisible);
			label_height.setVisible(dimVisible);
			text_width_integer.setVisible(dimVisible);
			text_height_integer.setVisible(dimVisible);
		}
		//documentListener.saveStates();
	}


	private void initComponents() {
		Vector<JTextComponent> texts = new Vector<JTextComponent>();
		setBorder(new TitledBorder("File Properties"));

		label_path = new JLabel("Path");

		text_path = new JTextField("");

		text_path.setName("text_path");
		map.put("text_path", text_path);
		texts.add(text_path);

		bu_change = new JButton("change");
		map.put("bu_change", bu_change);
		bu_change.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bu_change_clicked();
			}
		});

		label_format = new JLabel("Format");

		text_format = new JTextField("");

		text_format.setName("text_format");
		map.put("text_format", text_format);
		texts.add(text_format);

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

		label_type = new JLabel("Type");

		select_type = new JComboBox();
		select_type_model = new DefaultComboBoxModel();
		select_type.setModel(select_type_model);
		init_select_type_model();
		map.put("select_type", select_type);
		select_type.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				select_type_changed(select_type.getSelectedIndex());
			}
		});

		label_hfiller = new JLabel("");

		label_length = new JLabel("Length in Bytes");
		text_length = new JTextField("");
		text_length.setName("text_length");
		map.put("text_length", text_length);
		texts.add(text_length);
		
		label_width = new JLabel("Dimension width in px");
		text_width_integer = new JTextField("");
		text_width_integer.setName("text_width_integer");
		map.put("text_width_integer", text_width_integer);
		texts.add(text_width_integer);

		label_height = new JLabel("Dimension height in px");
		text_height_integer = new JTextField("");
		text_height_integer.setName("text_height_integer");
		map.put("text_height_integer", text_height_integer);
		texts.add(text_height_integer);
		
		label_samplerate = new JLabel("Samplerate");
		text_samplerate = new JTextField("");
		text_samplerate.setName("text_samplerate");
		map.put("text_samplerate", text_samplerate);
		texts.add(text_samplerate);		

		label_samplesize = new JLabel("Samplesize");
		text_samplesize = new JTextField("");
		text_samplesize.setName("text_samplesize");
		map.put("text_samplesize", text_samplesize);
		texts.add(text_samplesize);
		
		label_bitrate = new JLabel("Bitrate");
		text_bitrate = new JTextField("");
		text_bitrate.setName("text_bitrate");
		map.put("text_bitrate", text_bitrate);
		texts.add(text_bitrate);
		
		label_bitratetype = new JLabel("Type of bitrate");
		text_bitratetype = new JTextField("");
		text_bitratetype.setName("text_bitratetype");
		map.put("text_bitratetype", text_bitratetype);
		texts.add(text_bitratetype);
		
		label_codec = new JLabel("Codec");
		text_codec = new JTextField("");
		text_codec.setName("text_codec");
		map.put("text_codec", text_codec);
		texts.add(text_codec);
		
		label_codecsettings = new JLabel("Settings of codec");
		text_codecsettings = new JTextField("");
		text_codecsettings.setName("text_codecsettings");
		map.put("text_codecsettings", text_codecsettings);
		texts.add(text_codecsettings);		
		
		label_md5 = new JLabel("MD5 checksum");

		text_md5 = new JTextField("");

		text_md5.setName("text_md5");
		map.put("text_md5", text_md5);
		texts.add(text_md5);

		label_sha1 = new JLabel("SHA1 checksum");
		
		text_sha1 = new JTextField("");

		text_sha1.setName("text_sha1");
		map.put("text_sha1", text_sha1);
		texts.add(text_sha1);

		label_structuredname = new JLabel("structured name");
		
		text_structuredname = new JTextField("");

		text_structuredname.setName("text_structuredname");
		map.put("text_structuredname", text_structuredname);
		texts.add(text_structuredname);
		
		
		label_filler = new JLabel("");

		DocumentInstantChangeListener chl = new DocumentInstantChangeListener(this);
		for (JTextComponent text : texts) {
			if (text instanceof JTextField) {
				chl.addTextComponent(text);
			}
		}
		text_path.setEditable(false);
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



		// Component: label_path
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
		gbl.setConstraints(label_path,gbc);
		add(label_path);

		// Component: text_path
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 3;
		gbc.gridheight = 1;
		gbc.weightx = 70.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(text_path,gbc);
		add(text_path);

		// Component: bu_change
		gbc.gridx = 4;
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
		gbl.setConstraints(bu_change,gbc);
		add(bu_change);

		// Component: label_format
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
		gbl.setConstraints(label_format,gbc);
		add(label_format);

		// Component: text_format
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
		gbl.setConstraints(text_format,gbc);
		add(text_format);

		// Component: label_channels
		gbc.gridx = 3;
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
		gbl.setConstraints(label_channels,gbc);
		add(label_channels);

		// Component: select_channels
		gbc.gridx = 4;
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
		gbl.setConstraints(select_channels,gbc);
		add(select_channels);

		// Component: label_type
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
		gbl.setConstraints(label_type,gbc);
		add(label_type);

		// Component: select_type
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 40.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(select_type,gbc);
		add(select_type);

		// Component: label_hfiller
		gbc.gridx = 2;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 60.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_hfiller,gbc);
		add(label_hfiller);

		// Component: label_width
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_width,gbc);
		add(label_width);

		// Component: text_width
		gbc.gridx = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(text_width_integer,gbc);
		add(text_width_integer);
		
		// Component: label_height
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_height,gbc);
		add(label_height);

		// Component: text_height
		gbc.gridx = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(text_height_integer,gbc);
		add(text_height_integer);
		
		// Component: label_length
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_length,gbc);
		add(label_length);

		// Component: text_length
		gbc.gridx = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(text_length,gbc);
		add(text_length);

		// Component: label_samplerate
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_samplerate,gbc);
		add(label_samplerate);

		// Component: text_samplerate
		gbc.gridx = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(text_samplerate,gbc);
		add(text_samplerate);
		
		// Component: label_samplesize
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_samplesize,gbc);
		add(label_samplesize);

		// Component: text_samplesize
		gbc.gridx = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(text_samplesize,gbc);
		add(text_samplesize);
		
		// Component: label_bitrate
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_bitrate,gbc);
		add(label_bitrate);

		// Component: text_bitrate
		gbc.gridx = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(text_bitrate,gbc);
		add(text_bitrate);
		
		// Component: label_bitratetype
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_bitratetype,gbc);
		add(label_bitratetype);

		// Component: text_bitratetype
		gbc.gridx = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(text_bitratetype,gbc);
		add(text_bitratetype);	
		
		// Component: label_codec
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_codec,gbc);
		add(label_codec);

		// Component: text_codec
		gbc.gridx = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(text_codec,gbc);
		add(text_codec);
		
		// Component: label_codecsettings
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_codecsettings,gbc);
		add(label_codecsettings);

		// Component: text_codecsettings
		gbc.gridx = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(text_codecsettings,gbc);
		add(text_codecsettings);		

		// Component: label_md5
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_md5,gbc);
		add(label_md5);

		// Component: text_md5
		gbc.gridx = 1;
		gbc.gridwidth = 4;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(text_md5,gbc);
		add(text_md5);

		// Component: label_sha1
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_sha1,gbc);
		add(label_sha1);

		// Component: text_sha1
		gbc.gridx = 1;
		gbc.gridwidth = 4;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(text_sha1,gbc);
		add(text_sha1);
		
		// Component: label_structuredname
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_structuredname,gbc);
		add(label_structuredname);

		// Component: text_structuredname
		gbc.gridx = 1;
		gbc.gridwidth = 4;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(text_structuredname,gbc);
		add(text_structuredname);
		
		// Component: label_filler
		gbc.gridx = 0;
		gbc.gridy++;
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
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch(Exception ex){
			System.out.println("Nimbus look & feel not available");
		}
		PanelFileProperties p = new PanelFileProperties();
		JFrame f = new JFrame("PanelFileProperties");
		f.setContentPane(p);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(1024,768);
		f.setVisible(true);
	}


	// ----- action methods --------------------------------
	public void bu_change_clicked() {
		File path = null;
		
		if (file!=null && file.getOriginFile()!=null) {
			try {
				path = new File(file.getOriginFile()).getParentFile();
		 	if (!path.exists()) path = null;
			} catch (Exception ex) {
				path = null;
			}
		}
		
		File f = Dialogs.chooseOpenFile("Choose file", path, "");
		if (f==null || !f.exists() || f.isDirectory()) {
			Dialogs.showMessage("Please select a valid file.");
			return;
		}
		if (file == null) {
			file = ItemFile.make(f);
			file.type("full");
		} else {
			file.setFile(f);
		}
		notifyChanges();
		update(file, feed);
	}
	
	public void init_select_channels_model() {
		select_channels_model.removeAllElements();
		select_channels_model.addElement("[no audio]");
		select_channels_model.addElement("stereo");
		select_channels_model.addElement("mono");
		select_channels_model.addElement("joint-stereo");
		select_channels_model.addElement("5.1");
	}
	public void select_channels_changed(int selected) {
		if (file==null) return;
		file.channels((String)select_channels.getSelectedItem());
		notifyChanges();
	}
	public void init_select_type_model() {
		select_type_model.removeAllElements();
		select_type_model.addElement("[not specified]");
	}
	public void select_type_changed(int selected) {
		if (file==null) return;
		file.type((String)select_type.getSelectedItem());
		
		//update dimension
		Integer w = file.getDimensionWidth();
		if (w==null) {
			text_width_integer.setText("");
		} else {
			text_width_integer.setText(""+w);
		}
		Integer h = file.getDimensionHeight();
		if (h==null) {
			text_height_integer.setText("");
		} else {
			text_height_integer.setText(""+h);
		}
		String ftype = file.getType();
		boolean dimVisible = false;
		if (h!=null || w!=null || ftype!=null && (ftype.equals("frontcover") || ftype.equals("backcover"))) {
			dimVisible = true;
		}
		label_width.setVisible(dimVisible);
		label_height.setVisible(dimVisible);
		text_width_integer.setVisible(dimVisible);
		text_height_integer.setVisible(dimVisible);
		
		notifyChanges();
		
	}
	public void text_changed(JTextComponent text) {
		if (file==null) return;
		String t = text.getText();
		if (t.length()==0) t=null;
		
		if (text == text_format) {
			file.filetype(t);
		}
//		else if (text == text_path) {
//			file.setLocation(FileLocation.make(t,t));
//		}
		else if (text == text_samplerate) {
			file.samplerate(t);
		}
		else if (text == text_bitrate) {
			file.bitrate(t);
		}
		else if (text == text_bitratetype) {
			file.bitratetype(t);
		}
		else if (text == text_codec) {
			file.codec(t);
		}
		else if (text == text_codecsettings) {
			file.codecsettings(t);
		}
		else if (text == text_length) {
			if (t!=null) {
				try {
					long len = Long.parseLong(t);
					file.bytes(len);
				} catch (Exception ex) {
					Dialogs.showMessage("Error parsing Length in Bytes.");
					return;
				}
			} else {
				file.bytes(-1L);
			}
		}
		else if (text == text_width_integer) {
			if (t!=null) {
				try {
					int w = Integer.parseInt(t);
					file.dimension(new Integer(w),file.getDimensionHeight());
				} catch (Exception ex) {
					Dialogs.showMessage("Error parsing \"Dimension width in pixel\"");	
					return;
				}
			} else {
				file.dimension(null,file.getDimensionHeight());
			}
 		}
		else if (text == text_height_integer) {
			if (t!=null) {
				try {
					int h = Integer.parseInt(t);
					file.dimension(file.getDimensionWidth(),new Integer(h));
				} catch (Exception ex) {
					Dialogs.showMessage("Error parsing \"Dimension height in pixel\"");
					return;
				}
			} else {
				file.dimension(file.getDimensionWidth(),null);
			}
 		}
		
		
		//text_md5;
		//text_sha1;
		//text_structuredname;
		
//		if (text == text_path) {
//			file.setFile(new File(t));
//		}
		
		//		else if (text == text_length) {
		//			try {
		//				file.bytes(Integer.parseInt(t));
		//			} catch (Exception ex) {
		//				text_length.setText(""+file.getBytes());
		//			}
		//		}
		//		else if (text == text_md5) {
		//			try {
		//				file.md5(SecurityHelper.HexDecoder.decode(t));
		//			} catch (Exception ex) {
		//				if (file.getChecksums()==null) {
		//					text_md5.setText("");
		//				} else {
		//					text_md5.setText(file.getChecksums().getMd5String());
		//				}
		//			}
		//		}
		//		else if (text == text_sha1) {
		//			try {
		//				file.sha1(SecurityHelper.HexDecoder.decode(t));
		//			} catch (Exception ex) {
		//				if (file.getChecksums()==null) {
		//					text_sha1.setText("");
		//				} else {
		//					text_sha1.setText(file.getChecksums().getSha1String());
		//				}
		//			}
		//		}
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
