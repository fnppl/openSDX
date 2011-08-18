package org.fnppl.opensdx.gui.helper;

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

import org.fnppl.opensdx.common.BusinessStringItem;
import org.fnppl.opensdx.common.Checksums;
import org.fnppl.opensdx.common.ItemFile;
import org.fnppl.opensdx.security.SecurityHelper;

import java.util.HashMap;
import java.util.Vector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

public class PanelFileProperties extends JPanel implements MyObservable {

	//init fields
	private DocumentChangeListener documentListener;
	private KeyAdapter keyAdapter;
	private HashMap<String,JComponent> map = new HashMap<String, JComponent>();

	private ItemFile file;
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
	private JLabel label_md5;
	private JTextField text_md5;
	private JLabel label_sha1;
	private JTextField text_sha1;
	private JLabel label_filler;


	public PanelFileProperties() {
		initKeyAdapter();
		initComponents();
		initLayout();

		//text_path.setEditable(false);
		text_length.setEditable(false);
		text_md5.setEditable(false);
		text_sha1.setEditable(false);

		file = null;
		update(file);
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
		
		select_type_model.removeAllElements();
		select_type_model.addElement("[not specified]");
		select_type_model.addElement("full");
		select_type_model.addElement("pre-listening");
	}

	public void update(ItemFile file) {
		this.file = file;
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
		} else {
			text_path.setText(file.getLocationPath());
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

		// Component: label_length
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
		gbl.setConstraints(label_length,gbc);
		add(label_length);

		// Component: text_length
		gbc.gridx = 1;
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
		gbl.setConstraints(text_length,gbc);
		add(text_length);

		// Component: label_samplerate
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
		gbl.setConstraints(label_samplerate,gbc);
		add(label_samplerate);

		// Component: text_samplerate
		gbc.gridx = 1;
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
		gbl.setConstraints(text_samplerate,gbc);
		add(text_samplerate);
		
		// Component: label_samplesize
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
		gbl.setConstraints(label_samplesize,gbc);
		add(label_samplesize);

		// Component: text_samplesize
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
		gbl.setConstraints(text_samplesize,gbc);
		add(text_samplesize);
		
		// Component: label_bitrate
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
		gbl.setConstraints(label_bitrate,gbc);
		add(label_bitrate);

		// Component: text_bitrate
		gbc.gridx = 1;
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
		gbl.setConstraints(text_bitrate,gbc);
		add(text_bitrate);
		
		// Component: label_bitratetype
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
		gbl.setConstraints(label_bitratetype,gbc);
		add(label_bitratetype);

		// Component: text_bitratetype
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
		gbl.setConstraints(text_bitratetype,gbc);
		add(text_bitratetype);	
		
		// Component: label_codec
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
		gbl.setConstraints(label_codec,gbc);
		add(label_codec);

		// Component: text_codec
		gbc.gridx = 1;
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
		gbl.setConstraints(text_codec,gbc);
		add(text_codec);
		
		// Component: label_codecsettings
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
		gbl.setConstraints(label_codecsettings,gbc);
		add(label_codecsettings);

		// Component: text_codecsettings
		gbc.gridx = 1;
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
		gbl.setConstraints(text_codecsettings,gbc);
		add(text_codecsettings);		

		// Component: label_md5
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
		gbl.setConstraints(label_md5,gbc);
		add(label_md5);

		// Component: text_md5
		gbc.gridx = 1;
		gbc.gridy = 10;
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
		gbl.setConstraints(label_sha1,gbc);
		add(label_sha1);

		// Component: text_sha1
		gbc.gridx = 1;
		gbc.gridy = 11;
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
		notifyChanges();
	}
	public void text_changed(JTextComponent text) {
		if (file==null) return;
		String t = text.getText();
		if (text == text_format) {
			file.filetype(t);
		}
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
