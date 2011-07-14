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

import org.fnppl.opensdx.common.ItemTags;
import org.fnppl.opensdx.dmi.FeedGui;
import org.fnppl.opensdx.gui.EditCheckBoxTree;

import java.util.HashMap;
import java.util.Vector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PanelTags extends JPanel implements MyObservable, MyObserver {

	//init fields
	private DocumentChangeListener documentListener;
	private KeyAdapter keyAdapter;
	private HashMap<String,JComponent> map = new HashMap<String, JComponent>();

	private JCheckBox check_bundle_only;
	private JPanel panel_genres;
	private EditCheckBoxTree tree_genres;
	private JCheckBox check_explicit_lyrics;
	private JLabel label_main_language;
	private JTextField text_main_language;
	private JButton bu_select_lang;
	private JLabel label_origin_country;
	private JTextField text_origin_country;
	private JButton bu_select_country;
	private JLabel label_filler;

	private ItemTags tags;

	public PanelTags() {
		initKeyAdapter();
		initComponents();
		initLayout();
		tree_genres.addObserver(this);
		update((ItemTags)null);
	}



	public void update(ItemTags tags) {
		this.tags = tags;
		if (tags == null) {;
		check_bundle_only.setSelected(false);
		check_explicit_lyrics.setSelected(false);
		text_main_language.setText("");
		text_origin_country.setText("");
		Vector<String> genres = new Vector<String>();
		tree_genres.setSelectedNodes(genres);

		} else {
			check_bundle_only.setSelected(tags.isBundle_only());
			check_explicit_lyrics.setSelected(tags.isExplicit_lyrics());
			text_main_language.setText(tags.getMain_language());
			text_origin_country.setText(tags.getOrigin_country());
			Vector<String> genres = new Vector<String>();
			for (int i=0;i<tags.getGenresCount();i++) {
				String genre = tags.getGenre(i);
				genres.add(genre);
			}
			tree_genres.setSelectedNodes(genres);
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
		setBorder(new TitledBorder("Tags"));

		check_bundle_only = new JCheckBox("bundle only");
		map.put("check_bundle_only", check_bundle_only);
		check_bundle_only.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				check_bundle_only_changed(check_bundle_only.isSelected());
			}
		});

		panel_genres = new JPanel();
		panel_genres.setBorder(new TitledBorder("Genres"));
		tree_genres = new EditCheckBoxTree(FeedGui.getGenres());

		check_explicit_lyrics = new JCheckBox("explicit lyrics");
		map.put("check_explicit_lyrics", check_explicit_lyrics);
		check_explicit_lyrics.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				check_explicit_lyrics_changed(check_explicit_lyrics.isSelected());
			}
		});

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

		panel_genres.setLayout(new BorderLayout());
		panel_genres.add(new JScrollPane(tree_genres),BorderLayout.CENTER);

		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		GridBagConstraints gbc = new GridBagConstraints();


		// Component: check_bundle_only
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
		gbl.setConstraints(check_bundle_only,gbc);
		add(check_bundle_only);


		// Component: panel_genres
		gbc.gridx = 3;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 5;
		gbc.weightx = 40.0;
		gbc.weighty = 100.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(panel_genres,gbc);
		add(panel_genres);

		// Component: check_explicit_lyrics
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
		gbl.setConstraints(check_explicit_lyrics,gbc);
		add(check_explicit_lyrics);

		// Component: label_main_language
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
		gbl.setConstraints(label_main_language,gbc);
		add(label_main_language);

		// Component: text_main_language
		gbc.gridx = 1;
		gbc.gridy = 2;
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
		gbl.setConstraints(bu_select_lang,gbc);
		add(bu_select_lang);

		// Component: label_origin_country
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
		gbl.setConstraints(label_origin_country,gbc);
		add(label_origin_country);

		// Component: text_origin_country
		gbc.gridx = 1;
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
		gbl.setConstraints(text_origin_country,gbc);
		add(text_origin_country);

		// Component: bu_select_country
		gbc.gridx = 2;
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
		gbl.setConstraints(bu_select_country,gbc);
		add(bu_select_country);

		// Component: label_filler
		gbc.gridx = 4;
		gbc.gridy = 5;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 40.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_filler,gbc);
		add(label_filler);
	}


	// ----- action methods --------------------------------
	public void check_bundle_only_changed(boolean selected) {
		if (tags==null) return;
		tags.bundle_only(selected);
		notifyChanges();
	}
	public void check_explicit_lyrics_changed(boolean selected) {
		if (tags==null) return;
		tags.explicit_lyrics(selected);
		notifyChanges();
	}
	public void bu_select_lang_clicked() { 
		if (tags==null) return;
		String lang = FeedGui.showLanguageCodeSelector();
		if (lang!=null) {
			text_main_language.setText(lang);
			documentListener.saveState(text_main_language);
			tags.main_language(lang);
			notifyChanges();
		}
	}

	public void bu_select_country_clicked() {
		if (tags==null) return;
		String country = FeedGui.showCountryCodeSelector();
		if (country!=null) {
			text_origin_country.setText(country);
			documentListener.saveState(text_origin_country);
			tags.origin_country(country);
			notifyChanges();
		}
	}
	
	public void text_changed(JTextComponent text) {
		if (tags==null) return;
		String t = text.getText();
		if (text == text_main_language) {
			tags.main_language(t.toLowerCase());
		}
		else if (text == text_origin_country) {
			tags.origin_country(t);
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


	public void notifyChange(MyObservable changedIn) {
		if (tags==null) return;
		if (changedIn == tree_genres) {
	        tags.removeAllGenres();
	        Vector<String> selectedGenres = tree_genres.getSelectedNodes();
	        for (String g : selectedGenres) {
	            tags.addGenre(g);
	        }
		}
		notifyChanges();
	}
}
