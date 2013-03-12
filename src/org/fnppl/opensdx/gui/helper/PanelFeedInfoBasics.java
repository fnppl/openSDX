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

import org.fnppl.opensdx.common.FeedInfo;
import org.fnppl.opensdx.dmi.FeedGui;
import org.fnppl.opensdx.dmi.FeedGuiTooltips;
import org.fnppl.opensdx.security.SecurityHelper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PanelFeedInfoBasics extends JPanel implements MyObservable, TextChangeListener {

	//init fields
	//private DocumentChangeListener documentListener;
	//private KeyAdapter keyAdapter;
	private HashMap<String,JComponent> map = new HashMap<String, JComponent>();

	private JLabel label_feedid;
	private JTextField text_feedid;
	private JButton bu_uuid;
	private JCheckBox check_onlytest;
	private JLabel label_creation_datetime;
	private JTextField text_creation_datetime;
	private JLabel label_effectivedatetime;
	private JTextField text_effective_datetime;
	private JButton bu_now;

	private FeedGui gui;

	public PanelFeedInfoBasics(FeedGui gui) {
		this.gui = gui;
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

	public void initTooltips() {
		text_feedid.setToolTipText(FeedGuiTooltips.feedid);
		bu_uuid.setToolTipText(FeedGuiTooltips.randomUUID);
		check_onlytest.setToolTipText(FeedGuiTooltips.onlytest);
		text_creation_datetime.setToolTipText(FeedGuiTooltips.creation_datetime);
		text_effective_datetime.setToolTipText(FeedGuiTooltips.effective_datetime);
		bu_now.setToolTipText(FeedGuiTooltips.now);
	}

	public void update() {
		FeedInfo fi = getFeedInfo();
		if (fi==null) {
			text_feedid.setText("");
			check_onlytest.setSelected(true);
			text_effective_datetime.setText("");
			text_creation_datetime.setText("");
		} else {
			text_feedid.setText(fi.getFeedID());
			check_onlytest.setSelected(fi.getOnlyTest());
			text_effective_datetime.setText(fi.getEffectiveDatetimeString());
			text_creation_datetime.setText(fi.getCreationDatetimeString());
		}
		//documentListener.saveStates();
	}

	private FeedInfo getFeedInfo() {
		if (gui==null || gui.getCurrentFeed()==null) return null;
		FeedInfo info = gui.getCurrentFeed().getFeedinfo();
		return info;
	}


//	private void initKeyAdapter() {
//		keyAdapter = new KeyAdapter() {
//			public void keyPressed(KeyEvent e) {
//				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
//					if (e.getComponent() instanceof JTextField) {
//						try {
//							JTextComponent text = (JTextComponent)e.getComponent();
//							String t = text.getText();
//							String name = text.getName();
//							if (documentListener.formatOK(name,t)) {
//								text_changed(text);
//								documentListener.saveState(text);
//							}
//						} catch (Exception ex) {
//							ex.printStackTrace();
//						}
//					}
//				}
//				else if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
//					if (e.getComponent() instanceof JTextField) {
//						JTextField text = (JTextField)e.getComponent();
//						text.setText(documentListener.getSavedText(text));
//						text.setBackground(Color.WHITE);
//					}
//				}
//			}
//		};
//	}

	private void initComponents() {
		Vector<JTextComponent> texts = new Vector<JTextComponent>();

		label_feedid = new JLabel("Feed ID");

		text_feedid = new JTextField("");

		text_feedid.setName("text_feedid");
		map.put("text_feedid", text_feedid);
		texts.add(text_feedid);

		bu_uuid = new JButton("random UUID");
		map.put("bu_uuid", bu_uuid);
		bu_uuid.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bu_uuid_clicked();
			}
		});

		check_onlytest = new JCheckBox("only test");
		map.put("check_onlytest", check_onlytest);
		check_onlytest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				check_onlytest_changed(check_onlytest.isSelected());
			}
		});

		label_creation_datetime = new JLabel("creation datetime");

		text_creation_datetime = new JTextField("");

		text_creation_datetime.setName("text_creationdatetime");
		map.put("text_creationdatetime", text_creation_datetime);
		texts.add(text_creation_datetime);
		text_creation_datetime.addFocusListener(new FocusAdapter() {
			 public void focusLost(FocusEvent evt) {
			    if (evt.isTemporary()) {
			      return;
			    }
			    try {
			    	FeedInfo fi = getFeedInfo();
			    	text_creation_datetime.setText(fi.getCreationDatetimeString());
			    } catch (Exception ex) {}
			 }
		});
		
		label_effectivedatetime = new JLabel("effective datetime");

		text_effective_datetime = new JTextField("");

		text_effective_datetime.setName("text_effectivedatetime");
		map.put("text_effectivedatetime", text_effective_datetime);
		texts.add(text_effective_datetime);
		text_effective_datetime.addFocusListener(new FocusAdapter() {
			 public void focusLost(FocusEvent evt) {
			    if (evt.isTemporary()) {
			      return;
			    }
			    try {
			    	FeedInfo fi = getFeedInfo();
			    	text_effective_datetime.setText(fi.getEffectiveDatetimeString());
			    } catch (Exception ex) {}
			 }
		});
		
		

		bu_now = new JButton("now");
		map.put("bu_now", bu_now);
		bu_now.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bu_now_clicked();
			}
		});

//		documentListener = new DocumentChangeListener(texts);
//		for (JTextComponent text : texts) {
//			text.getDocument().addDocumentListener(documentListener);
//			if (text instanceof JTextField) text.addKeyListener(keyAdapter);
//		}
//		documentListener.saveStates();
		
		DocumentInstantChangeListener chl = new DocumentInstantChangeListener(this);
		for (JTextComponent text : texts) {
			if (text instanceof JTextField) {
				chl.addTextComponent(text);
			}
		}
	}



//	public void updateDocumentListener() {
//		documentListener.saveStates();
//	}
//
//	public void updateDocumentListener(JTextComponent t) {
//		documentListener.saveState(t);
//	}
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



		// Component: label_feedid
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
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(label_feedid,gbc);
		add(label_feedid);

		// Component: text_feedid
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
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(text_feedid,gbc);
		add(text_feedid);

		// Component: bu_uuid
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(bu_uuid,gbc);
		add(bu_uuid);

		// Component: check_onlytest
		gbc.gridx = 3;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(check_onlytest,gbc);
		add(check_onlytest);

		// Component: label_creationdatetime
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
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(label_creation_datetime,gbc);
		add(label_creation_datetime);

		// Component: text_creationdatetime
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
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(text_creation_datetime,gbc);
		add(text_creation_datetime);

		// Component: label_effectivedatetime
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(label_effectivedatetime,gbc);
		add(label_effectivedatetime);

		// Component: text_effectivedatetime
		gbc.gridx = 3;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 50.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(text_effective_datetime,gbc);
		add(text_effective_datetime);

		// Component: bu_now
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
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(bu_now,gbc);
		add(bu_now);
	}


	// ----- action methods --------------------------------


	public void bu_uuid_clicked() {
		String uuid = UUID.randomUUID().toString();
		text_feedid.setText(uuid);
		//documentListener.saveState(text_feedid);
		FeedInfo fi = getFeedInfo();
		if (fi!=null)  {
			fi.feedid(uuid);   
			notifyChanges();
		}
	}
	public void check_onlytest_changed(boolean selected) {
		FeedInfo feedinfo = getFeedInfo(); 
		if (feedinfo != null) {
			feedinfo.only_test(check_onlytest.isSelected());
			notifyChanges();
		}
	}
	public void bu_now_clicked() {
		long now = System.currentTimeMillis();
		String s = SecurityHelper.getFormattedDate(now);
		text_creation_datetime.setText(s);
		text_effective_datetime.setText(s);
		//documentListener.saveState(text_creation_datetime);
		//documentListener.saveState(text_effective_datetime);
		FeedInfo fi = getFeedInfo();
		if (fi!=null)  {
			fi.creation_datetime(now);
			fi.effective_datetime(now);
			notifyChanges();
		}
	}

	public void text_changed(JTextComponent text) {
		FeedInfo fi = getFeedInfo();
		if (fi!=null) {
			String t = text.getText();
			if (text == text_feedid) {
				fi.feedid(t);
			}
			else if (text == text_creation_datetime) {
				try {
					fi.creation_datetime(DocumentInstantChangeListener.datetimeformat.parse(t).getTime());
//					String s = fi.getCreationDatetimeString();
//					if (!t.equals(s)) {
//						text.setText(s);
//					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else if (text == text_effective_datetime) {
				try {
					//fi.effective_datetime(SecurityHelper.parseDate(t));
					fi.effective_datetime(DocumentInstantChangeListener.datetimeformat.parse(t).getTime());
//					String s = fi.getEffectiveDatetimeString();
//					if (!t.equals(s)) {
//						text.setText(s);
//					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			notifyChanges();
			//text.requestFocusInWindow();
			//text.transferFocus();
		}

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
