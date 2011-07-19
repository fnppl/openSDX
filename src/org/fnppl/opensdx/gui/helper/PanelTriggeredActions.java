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
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;

import org.fnppl.opensdx.common.Action;
import org.fnppl.opensdx.common.ActionHttp;
import org.fnppl.opensdx.common.ActionMailTo;
import org.fnppl.opensdx.common.FeedInfo;
import org.fnppl.opensdx.common.TriggeredActions;
import org.fnppl.opensdx.dmi.FeedGui;
import org.fnppl.opensdx.gui.PanelActionHTTP;
import org.fnppl.opensdx.gui.PanelActionMailTo;
import org.fnppl.opensdx.security.SecurityHelper;

import java.util.HashMap;
import java.util.UUID;
import java.util.Vector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PanelTriggeredActions extends JPanel implements MyObservable {

	//init fields
	private FeedGui gui;
	private JTable table_actions;
	private JButton bu_action_remove;
	private JButton bu_action_add_mail;
	private JButton bu_action_add_http;
	private JButton bu_action_edit;


	public PanelTriggeredActions(FeedGui gui) {
		this.gui = gui;
		initComponents();
		initLayout();
	}

	public void update() {
		updateActionsTable(getFeedInfo());
	}

	private void updateActionsTable(FeedInfo fi) {
		int count = 0;
		if (fi!=null) {
			count = fi.getActionCount();
		}
		String[] header = new String[] {"Trigger","Type","Description"};
		String[][] data = new String[count][3];
		for (int i=0;i<count;i++) {
			Action a = fi.getAction(i);
			data[i][0] = TriggeredActions.actionTriggerName[fi.getTrigger(i)];
			if (a instanceof ActionHttp) {
				data[i][1] = "HTTP";
			} else if (a instanceof ActionMailTo) {
				data[i][1] = "MAIL TO";
			}
			data[i][2] = a.getDescription();
		}
		table_actions.setModel(new DefaultTableModel(data, header) {
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		});

	}

	private FeedInfo getFeedInfo() {
		if (gui==null || gui.getCurrentFeed()==null) return null;
		FeedInfo info = gui.getCurrentFeed().getFeedinfo();
		return info;
	}


	private void initComponents() {
		this.setBorder(new TitledBorder("Triggered Actions"));

		table_actions = new JTable();
		
		bu_action_remove = new JButton("remove");
		bu_action_remove.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				bu_action_remove_clicked();
			}
		});

		bu_action_add_http = new JButton("add HTTP Action");
		bu_action_add_http.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				bu_action_add_http_clicked();
			}
		});

		bu_action_add_mail = new JButton("add Mail Action");
		bu_action_add_mail.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				bu_action_add_mail_clicked();
			}
		});

		bu_action_edit = new JButton("edit");
		bu_action_edit.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				bu_action_edit_clicked();
			}
		});
	}


	public void initLayout() {
		this.setLayout(new BorderLayout());
		this.add(new JScrollPane(table_actions),BorderLayout.CENTER);
		JPanel buttons = new JPanel();
		buttons.setLayout(new FlowLayout(FlowLayout.LEFT));
		buttons.add(bu_action_edit);
		buttons.add(bu_action_remove);
		buttons.add(bu_action_add_http);
		buttons.add(bu_action_add_mail);
		this.add(buttons, BorderLayout.SOUTH);
	}


	// ----- action methods --------------------------------


	public void bu_action_remove_clicked() {
		FeedInfo feedinfo = getFeedInfo();
		if (feedinfo!=null)  {
			int no = table_actions.getSelectedRow();
			if (no<0 || no>=feedinfo.getActionCount()) return;
			feedinfo.removeAction(no);
			updateActionsTable(feedinfo);
			notifyChanges();
		}
	}

	public void bu_action_add_http_clicked() {
		FeedInfo feedinfo = getFeedInfo();
		if (feedinfo!=null)  {
			ActionHttp a = ActionHttp.make("", ActionHttp.TYPE_GET);
			Vector result = editActionHttpDialog(a, TriggeredActions.TRIGGER_ONFULLSUCCESS);
			if (result!=null)  {
				ActionHttp newAction = (ActionHttp)result.get(0);
				int trigger = ((Integer)result.get(1)).intValue();
				feedinfo.addAction(trigger, newAction);
				updateActionsTable(feedinfo);
				notifyChanges();
			}
		}
	}

	public void bu_action_add_mail_clicked() {
		FeedInfo feedinfo = getFeedInfo();
		if (feedinfo!=null)  {
			ActionMailTo a = ActionMailTo.make("", "", "");
			Vector result = editActionMailToDialog(a, TriggeredActions.TRIGGER_ONFULLSUCCESS);
			if (result!=null)  {
				ActionMailTo newAction = (ActionMailTo)result.get(0);
				int trigger = ((Integer)result.get(1)).intValue();
				feedinfo.addAction(trigger, newAction);
				updateActionsTable(feedinfo);
				notifyChanges();
			}
		}
	}

	public void bu_action_edit_clicked() {
		FeedInfo feedinfo = getFeedInfo();
		if (feedinfo==null) return;
		int no = table_actions.getSelectedRow();
		if (no<0 || no>=feedinfo.getActionCount()) return;
		Action a = feedinfo.getAction(no);
		if (a instanceof ActionHttp) {
			Vector result = editActionHttpDialog((ActionHttp)a, feedinfo.getTrigger(no));
			if (result!=null)  {
				ActionHttp newAction = (ActionHttp)result.get(0);
				int trigger = ((Integer)result.get(1)).intValue();
				feedinfo.replaceAction(no, trigger, newAction);
				updateActionsTable(feedinfo);
				notifyChanges();
			}
		} else if (a instanceof ActionMailTo) {
			Vector result = editActionMailToDialog((ActionMailTo)a, feedinfo.getTrigger(no));
			if (result!=null)  {
				ActionMailTo newAction = (ActionMailTo)result.get(0);
				int trigger = ((Integer)result.get(1)).intValue();
				feedinfo.replaceAction(no, trigger, newAction);
				updateActionsTable(feedinfo);
				notifyChanges();
			}
		} else {
			System.out.println("unknown action");
		}

	}

	private Vector editActionHttpDialog(ActionHttp action, int trigger) {
		PanelActionHTTP p = new PanelActionHTTP();
		p.setActionHTTP(action);
		p.setTrigger(trigger);
		int ans = JOptionPane.showConfirmDialog(null,p,"HTTP Action",JOptionPane.OK_CANCEL_OPTION);
		if (ans == JOptionPane.OK_OPTION) {
			Vector result = new Vector();
			result.add(p.getActionHTTP());
			result.add(p.getTrigger());
			return result;
		} else {
			return null;
		}
	}

	private Vector editActionMailToDialog(ActionMailTo action, int trigger) {
		PanelActionMailTo p = new PanelActionMailTo();
		p.setActionMailTo(action);
		p.setTrigger(trigger);
		int ans = JOptionPane.showConfirmDialog(null,p,"MailTo Action",JOptionPane.OK_CANCEL_OPTION);
		if (ans == JOptionPane.OK_OPTION) {
			Vector result = new Vector();
			result.add(p.getActionMailTo());
			result.add(p.getTrigger());
			return result;
		} else {
			return null;
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
