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
import javax.swing.event.ListDataListener;
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
	private JLabel label_explicit_lyrics;
	private JComboBox select_explicit_lyrics;
	private DefaultComboBoxModel select_explicit_lyrics_model;

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
		if (tags == null) {
			check_bundle_only.setSelected(false);
			select_explicit_lyrics.setSelectedIndex(0);
			Vector<String> genres = new Vector<String>();
			tree_genres.setSelectedNodes(genres);
		} else {
			check_bundle_only.setSelected(tags.isBundle_only());
			if (tags.getExplicit_lyrics()==null) {
				select_explicit_lyrics.setSelectedIndex(0);
			} else {
				select_explicit_lyrics.setSelectedItem(tags.getExplicit_lyrics());
			}
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

		label_explicit_lyrics = new JLabel("explicit lyrics");
		select_explicit_lyrics = new JComboBox();
		select_explicit_lyrics_model = new DefaultComboBoxModel();
		init_select_explicit_lyrics_model();
		select_explicit_lyrics.setModel(select_explicit_lyrics_model);
		map.put("select_explicit_lyrics", select_explicit_lyrics);
		select_explicit_lyrics.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				select_explicit_lyrics_changed(select_explicit_lyrics.getSelectedIndex());
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

		// Component: label_explicit_lyrics
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
		gbl.setConstraints(label_explicit_lyrics,gbc);
		add(label_explicit_lyrics);
		
		// Component: check_explicit_lyrics
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
		gbl.setConstraints(select_explicit_lyrics,gbc);
		add(select_explicit_lyrics);

		

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
	
	public void init_select_explicit_lyrics_model() {
		select_explicit_lyrics_model.removeAllElements();
		select_explicit_lyrics_model.addElement("[not set]");
		select_explicit_lyrics_model.addElement(ItemTags.EXPLICIT_LYRICS_TRUE);
		select_explicit_lyrics_model.addElement(ItemTags.EXPLICIT_LYRICS_FALSE);
		select_explicit_lyrics_model.addElement(ItemTags.EXPLICIT_LYRICS_CLEANED);
	}
	public void select_explicit_lyrics_changed(int selected) {
		if (tags==null) return;
		if (selected==0) {
			tags.explicit_lyrics(null);
		} else {
			tags.explicit_lyrics((String)select_explicit_lyrics_model.getElementAt(selected));
		}
		notifyChanges();
	}
	

	
	public void text_changed(JTextComponent text) {
		if (tags==null) return;
//		
//		notifyChanges();
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
