package org.fnppl.opensdx.gui.helper;
/*
 * Copyright (C) 2010-2015 
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

import org.fnppl.opensdx.common.ContractPartner;
import org.fnppl.opensdx.common.InfoWWW;
import org.fnppl.opensdx.dmi.FeedGui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PanelWWW extends JPanel implements MyObservable, TextChangeListener {

	//init fields
	private InfoWWW www = null;
	//private DocumentChangeListener documentListener;
	//private KeyAdapter keyAdapter;
	private HashMap<String,JComponent> map = new HashMap<String, JComponent>();

	private JLabel label_facebook;
	private JTextField text_facebook;
	private JLabel label_myspace;
	private JTextField text_myspace;
	private JLabel label_homepage;
	private JTextField text_homepage;
	private JLabel label_twitter;
	private JTextField text_twitter;
	private JLabel label_blog;
	private JTextField text_blog;
	private JLabel label_phone;
	private JTextField text_phone;
	private JCheckBox check_phone_publishable;
	private JLabel label_filler;


	public PanelWWW(InfoWWW www) {
		this.www = www;
		initFocusTraversal();
		initComponents();
		initLayout();
	}

	@SuppressWarnings("unchecked")
	private void initFocusTraversal() {
		Set forwardKeys = new HashSet(getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
		forwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
		setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,forwardKeys);
	}
	
	public void update(InfoWWW www) {
		this.www = www;
		if (www == null) {;
			text_facebook.setText("");
			text_myspace.setText("");
			text_homepage.setText("");
			text_twitter.setText("");
			text_phone.setText("");
			text_blog.setText("");
			check_phone_publishable.setSelected(false);
		} else {
			text_facebook.setText(www.getFacebook());
			text_myspace.setText(www.getMyspace());
			text_homepage.setText(www.getHomepage());
			text_twitter.setText(www.getTwitter());
			text_blog.setText(www.getBlog());
			text_phone.setText(www.getPhone());
			check_phone_publishable.setSelected(www.isPhonePublishable());
		}
		//documentListener.saveStates();
	}


	private void initComponents() {
		Vector<JTextComponent> texts = new Vector<JTextComponent>();
		setBorder(new TitledBorder("WWW"));

		label_facebook = new JLabel("Facebook");

		text_facebook = new JTextField("");

		text_facebook.setName("text_facebook");
		map.put("text_facebook", text_facebook);
		texts.add(text_facebook);

		label_myspace = new JLabel("MySpace");

		text_myspace = new JTextField("");

		text_myspace.setName("text_myspace");
		map.put("text_myspace", text_myspace);
		texts.add(text_myspace);

		label_homepage = new JLabel("Homepage");

		text_homepage = new JTextField("");

		text_homepage.setName("text_homepage");
		map.put("text_homepage", text_homepage);
		texts.add(text_homepage);

		label_twitter = new JLabel("Twitter");

		text_twitter = new JTextField("");

		text_twitter.setName("text_twitter");
		map.put("text_twitter", text_twitter);
		texts.add(text_twitter);
		
		label_blog = new JLabel("Blog");

		text_blog = new JTextField("");

		text_blog.setName("text_blog");
		map.put("text_blog", text_blog);
		texts.add(text_blog);

		label_phone = new JLabel("Phone");

		text_phone = new JTextField("");

		text_phone.setName("text_phone");
		map.put("text_phone", text_phone);
		texts.add(text_phone);

		check_phone_publishable = new JCheckBox("phone publishable");
		map.put("check_phone_publishable", check_phone_publishable);
		check_phone_publishable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				check_phone_publishable_changed(check_phone_publishable.isSelected());
			}
		});

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

	Container spacer0 = new Container();


	// Component: label_facebook
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
	gbl.setConstraints(label_facebook,gbc);
	add(label_facebook);

	// Component: text_facebook
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
	gbl.setConstraints(text_facebook,gbc);
	add(text_facebook);

	// Component: label_myspace
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
	gbl.setConstraints(label_myspace,gbc);
	add(label_myspace);

	// Component: text_myspace
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
	gbl.setConstraints(text_myspace,gbc);
	add(text_myspace);

	// Component: label_homepage
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
	gbl.setConstraints(label_homepage,gbc);
	add(label_homepage);

	// Component: text_homepage
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
	gbl.setConstraints(text_homepage,gbc);
	add(text_homepage);

	// Component: label_twitter
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
	gbl.setConstraints(label_twitter,gbc);
	add(label_twitter);

	// Component: text_twitter
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
	gbl.setConstraints(text_twitter,gbc);
	add(text_twitter);
	
	
	// Component: label_blog
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
	gbl.setConstraints(label_blog,gbc);
	add(label_blog);

	// Component: text_blog
	gbc.gridx = 1;
	//gbc.gridy;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 50.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(text_blog,gbc);
	add(text_blog);

	// Component: label_phone
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
	gbl.setConstraints(label_phone,gbc);
	add(label_phone);

	// Component: text_phone
	gbc.gridx = 1;
	//gbc.gridy = 4;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 50.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(text_phone,gbc);
	add(text_phone);

	// Component: spacer0
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
	gbl.setConstraints(spacer0,gbc);
	add(spacer0);

	// Component: check_phone_publishable
	gbc.gridx = 1;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 50.0;
	gbc.weighty = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(2,2,2,2);
	gbl.setConstraints(check_phone_publishable,gbc);
	add(check_phone_publishable);

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
}
	
// ----- action methods --------------------------------
	public void check_phone_publishable_changed(boolean selected) {
		if (www==null) return;
		if (www.getPhone()!=null) {
			www.phone(www.getPhone(), check_phone_publishable.isSelected());
		}
		notifyChanges();
	}
	
	public InfoWWW getWWW() {
		return www;
	}
	
	public void text_changed(JTextComponent text) {
		if (www==null) {
			www = InfoWWW.make();
		}
		String t = text.getText();
		if (t!=null && t.length()==0) {
			t = null;
		}
		if (text == text_facebook) {
			www.facebook(t);
		}
		else if (text == text_myspace) {
			www.myspace(t);
		}
		else if (text == text_homepage) {
			www.homepage(t);
		}
		else if (text == text_twitter) {
			www.twitter(t);
		}
		else if (text == text_blog) {
			www.blog(t);
		}
		else if (text == text_phone) {
			www.phone(t, check_phone_publishable.isSelected());	
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
