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
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import org.fnppl.opensdx.gui.Dialogs;
import org.fnppl.opensdx.security.SecurityHelper;

import java.nio.ByteOrder;
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
	
	private JLabel label_main_language;
	private JTextField text_main_language;
	private JButton bu_select_lang;
	private JLabel label_origin_country;
	private JTextField text_origin_country;
	private JButton bu_select_country;
	
	
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
			text_main_language.setText("");
			text_origin_country.setText("");
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
			text_main_language.setText(info.getMain_language());
			text_origin_country.setText(info.getOrigin_country());
			updateLanguageList();
			updatePromoAndTeaserText();
		}
		documentListener.saveStates();
	}
	
	private void updateLanguageList() {
        list_language_model.removeAllElements();
        if (info !=null) {
            int anzP = info.getPromotextCount();
            int anzT = info.getTeasertextCount();
            Vector<String> lang = new Vector<String>();
            for (int i=0;i<anzP;i++) {
                lang.add(info.getPromotextLanguage(i));
            }
            for (int i=0;i<anzT;i++) {
                String l = info.getTeasertextLanguage(i);
                if (!lang.contains(l)) {
                    lang.add(l);
                }
            }
            for (String s : lang) {
                if (s!=null && s.length()>0) {
                    list_language_model.addElement(s);
                }
            }
            list_language.setModel(list_language_model);
        }
	}
	
	private void updatePromoAndTeaserText() {
		String lang = (String)list_language.getSelectedValue();
		if (lang==null) {
           text_promotion.setText("");
           text_teaser.setText("");
        } else {
           text_promotion.setText(info.getPromotext(lang));
           text_teaser.setText(info.getTeasertext(lang));
        }
        documentListener.saveState(text_promotion);
        documentListener.saveState(text_teaser);
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

		label_main_language = new JLabel("main language");

		text_main_language = new JTextField("");

		text_main_language.setName("text_main_language");
		map.put("text_main_language", text_main_language);
		texts.add(text_main_language);

		bu_select_lang = new JButton("select");
		map.put("bu_select_lang", bu_select_lang);
		bu_select_lang.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bu_select_lang_clicked();
			}
		});

		label_origin_country = new JLabel("origin country");

		text_origin_country = new JTextField("");

		text_origin_country.setName("text_origin_country");
		map.put("text_origin_country", text_origin_country);
		texts.add(text_origin_country);

		bu_select_country = new JButton("select");
		map.put("bu_select_country", bu_select_country);
		bu_select_country.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bu_select_country_clicked();
			}
		});
		
		list_language = new JList();

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
	
	JPanel pLang = new JPanel();
	pLang.setLayout(new BorderLayout());
	
	Dimension d = new Dimension(180,400);
	
	JScrollPane sLang = new JScrollPane(list_language);
	pLang.setPreferredSize(d);
	pLang.setMinimumSize(d);
	pLang.setMaximumSize(d);
	pLang.add(sLang, BorderLayout.CENTER);
	
	JPanel pLangButtons = new JPanel();
	pLangButtons.setLayout(new FlowLayout(FlowLayout.LEFT));
	pLangButtons.add(bu_lang_add);
	pLangButtons.add(bu_lan_remove);
	pLang.setBorder(new TitledBorder("Languages"));
	pLang.add(pLangButtons, BorderLayout.SOUTH);
	this.add(pLang, BorderLayout.WEST);
	
	JPanel pPromo = new JPanel();
	pPromo.setBorder(new TitledBorder("Promotion Text"));
	pPromo.setLayout(new BorderLayout());
	
	JScrollPane sPromo = new JScrollPane(text_promotion);
	pPromo.add(sPromo, BorderLayout.CENTER);

	JPanel pPromoButtons = new JPanel();
	pPromoButtons.setLayout(new FlowLayout(FlowLayout.LEFT));
	pPromoButtons.add(bu_promotion_update);
	pPromoButtons.add(bu_promotion_reset);
	pPromo.add(pPromoButtons, BorderLayout.SOUTH);
	
	JPanel pTeaser = new JPanel();
	pTeaser.setBorder(new TitledBorder("Teaser Text"));
	pTeaser.setLayout(new BorderLayout());
	
	JScrollPane sTeaser = new JScrollPane(text_teaser);
	pTeaser.add(sTeaser, BorderLayout.CENTER);

	JPanel pTeaserButtons = new JPanel();
	pTeaserButtons.setLayout(new FlowLayout(FlowLayout.LEFT));
	pTeaserButtons.add(bu_teaser_update);
	pTeaserButtons.add(bu_teaser_reset);
	pTeaser.add(pTeaserButtons, BorderLayout.SOUTH);
	
	
	JPanel pRest = new JPanel();
	pRest.setLayout(new BorderLayout());
	JPanel pRest2 = new JPanel();
	pRest2.setLayout(new BoxLayout(pRest2, BoxLayout.PAGE_AXIS));
	pRest2.add(pPromo);
	pRest2.add(pTeaser);
	
	pRest.add(pLang,BorderLayout.WEST);
	pRest.add(pRest2,BorderLayout.CENTER);
	
	
	GridBagLayout gbl = new GridBagLayout();
	setLayout(gbl);
	GridBagConstraints gbc = new GridBagConstraints();

	// Component: label_physical_release_datetime
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
	gbl.setConstraints(label_physical_release_datetime,gbc);
	add(label_physical_release_datetime);

	// Component: text_physical_release_datetime
	gbc.gridx = 1;
	gbc.gridy = 0;
	gbc.gridwidth = 3;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(text_physical_release_datetime,gbc);
	add(text_physical_release_datetime);

	JLabel hfill = new JLabel();
	gbc.gridx = 3;
	gbc.gridy = 0;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 30.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(hfill,gbc);
	add(hfill);
	
	// Component: label_digital_release_datetime
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
	gbl.setConstraints(label_digital_release_datetime,gbc);
	add(label_digital_release_datetime);

	// Component: text_digital_release_datetime
	gbc.gridx = 1;
	gbc.gridy = 1;
	gbc.gridwidth = 3;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
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
	gbc.gridwidth = 1;
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
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(text_playlength_integer,gbc);
	add(text_playlength_integer);

	int y = 3;
	// Component: label_main_language
	gbc.gridx = 0;
	gbc.gridy = y;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(label_main_language,gbc);
	add(label_main_language);

	// Component: text_main_language
	gbc.gridx = 1;
	gbc.gridy = y;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 20.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(text_main_language,gbc);
	add(text_main_language);

	// Component: bu_select_lang
	gbc.gridx = 2;
	gbc.gridy = y;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(bu_select_lang,gbc);
	add(bu_select_lang);

	y++;
	// Component: label_origin_country
	gbc.gridx = 0;
	gbc.gridy = y;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(label_origin_country,gbc);
	add(label_origin_country);

	// Component: text_origin_country
	gbc.gridx = 1;
	gbc.gridy = y;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 10.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(text_origin_country,gbc);
	add(text_origin_country);

	// Component: bu_select_country
	gbc.gridx = 2;
	gbc.gridy = y;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(bu_select_country,gbc);
	add(bu_select_country);
	
	
	y++;
	// pRest
	gbc.gridx = 0;
	gbc.gridy = y;
	gbc.gridwidth = 5;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(pRest,gbc);
	add(pRest);
	
	
	// Component: filler
	Container filler = new Container();
	gbc.gridx = 4;
	gbc.gridy = 0;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 70.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(filler,gbc);
	add(filler);
	
	
	// Component: filler2
	filler = new Container();
	gbc.gridx = 0;
	gbc.gridy = 6;
	gbc.gridwidth = 3;
	gbc.gridheight = 1;
	gbc.weightx = 0.0;
	gbc.weighty = 100.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(filler,gbc);
	add(filler);
	

}


// ----- action methods --------------------------------

	public void bu_select_lang_clicked() { 
		if (info==null) return;
		String lang = FeedGui.showLanguageCodeSelector();
		if (lang!=null) {
			text_main_language.setText(lang);
			documentListener.saveState(text_main_language);
			info.main_language(lang);
			notifyChanges();
		}
	}
	
	public void bu_select_country_clicked() {
		if (info==null) return;
		String country = FeedGui.showCountryCodeSelector();
		if (country!=null) {
			text_origin_country.setText(country);
			documentListener.saveState(text_origin_country);
			info.origin_country(country);
			notifyChanges();
		}
	}

	public void init_list_language_model() {
		list_language_model = new DefaultListModel();
	}
	public void list_language_changed(int selected) {
		updatePromoAndTeaserText();
	}
	
	public void bu_lang_add_clicked() {
		if (info != null) {
            String lang = FeedGui.showLanguageCodeSelector();
            if (lang!=null) {
                String p = info.getPromotext(lang);
                String t = info.getTeasertext(lang);
                if (t!=null || p !=null) {
                    Dialogs.showMessage("Selected language \""+lang+"\" is already in list.");
                    return;
                }
                info.setPromotext(lang, "");
                updateLanguageList();
                list_language.setSelectedIndex(list_language.getModel().getSize()-1);
                notifyChanges();
            }
        }
	}
	
	public void bu_lan_remove_clicked() {
		int sel = list_language.getSelectedIndex();
        if (sel>=0) {
            String lang = (String)list_language.getModel().getElementAt(sel);
            info.removePromotext(lang);
            info.removeTeasertext(lang);
            updateLanguageList();
            updatePromoAndTeaserText();
            notifyChanges();
        }
	}
	public void bu_promotion_update_clicked() {
		if (info==null) return;
        String lang = (String)list_language.getSelectedValue();
        if (lang!=null) {
        	info.setPromotext(lang, text_promotion.getText());
            text_promotion.setBackground(Color.WHITE);
            documentListener.saveState(text_promotion);
            notifyChanges();
        }
	}
	public void bu_promotion_reset_clicked() {
		text_promotion.setText(documentListener.getSavedText(text_promotion));
		text_promotion.setBackground(Color.WHITE);
	}
	public void bu_teaser_update_clicked() {
		if (info==null) return;
        String lang = (String)list_language.getSelectedValue();
        if (lang!=null) {
        	info.setTeasertext(lang, text_teaser.getText());
            text_teaser.setBackground(Color.WHITE);
            documentListener.saveState(text_teaser);
            notifyChanges();
        }
	}
	public void bu_teaser_reset_clicked() {
		text_teaser.setText(documentListener.getSavedText(text_teaser));
	    text_teaser.setBackground(Color.WHITE);
	}
	
	public void text_changed(JTextComponent text) {
		if (info==null) return;
		String t = text.getText();
		if (text == text_physical_release_datetime) {
			try {
				info.physical_release_datetime(SecurityHelper.parseDate(t));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		else if (text == text_digital_release_datetime) {
			try {
				info.digital_release_datetime(SecurityHelper.parseDate(t));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		else if (text == text_playlength_integer) {
			if (t.equals("")) {
				info.playlength(-1);
			} else {
				info.playlength(Integer.parseInt(t));
			}
		}
		else if (text == text_main_language) {
			info.main_language(t.toLowerCase());
		}
		else if (text == text_origin_country) {
			info.origin_country(t);
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
