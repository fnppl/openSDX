package org.fnppl.opensdx.gui;
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

import org.fnppl.opensdx.common.Bundle;
import org.fnppl.opensdx.common.BundleInformation;
import org.fnppl.opensdx.common.Feed;
import org.fnppl.opensdx.common.IDs;
import org.fnppl.opensdx.common.ItemTags;
import org.fnppl.opensdx.common.LicenseBasis;
import org.fnppl.opensdx.dmi.FeedGui;
import org.fnppl.opensdx.gui.helper.MyObservable;
import org.fnppl.opensdx.gui.helper.MyObserver;
import org.fnppl.opensdx.gui.helper.PanelBundleBasics;
import org.fnppl.opensdx.gui.helper.PanelContributors;
import org.fnppl.opensdx.gui.helper.PanelFiles;
import org.fnppl.opensdx.gui.helper.PanelIDs;
import org.fnppl.opensdx.gui.helper.PanelInformation;
import org.fnppl.opensdx.gui.helper.PanelLicense;
import org.fnppl.opensdx.gui.helper.PanelTags;

import java.util.HashMap;
import java.util.Vector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PanelBundle extends JPanel implements MyObservable, MyObserver {

	//init fields
	private FeedGui gui = null;
	private PanelBundleBasics pBasics;
	private PanelIDs pIDs;
	private PanelContributors pContributors;
	private PanelInformation pInformation;
	private PanelLicense pLicense;
	private PanelTags pTags;
	private PanelFiles pFiles;
	

	private JTabbedPane tabs;

	public PanelBundle(FeedGui gui) {
		this.gui = gui;
		initComponents();
		initLayout();
		pLicense.showAsOnBundle(false);
	}

	private Bundle getBundle() {
		if (gui==null) return null;
		Feed feed = gui.getCurrentFeed();
		if (feed==null || feed.getBundleCount()==0) return null;
		return feed.getBundle(0);
	}

	public void update() {
		Bundle bundle = getBundle();
		if (bundle ==null) {
			//update empty
			pBasics.update((Bundle)null);
			pIDs.update((IDs)null);
			pContributors.update((Bundle)null);
			pInformation.update((BundleInformation)null);
			pLicense.update((LicenseBasis)null);
			pTags.update((ItemTags)null);
			pFiles.update((Bundle)null);
		} else {
			IDs ids = bundle.getIds();
			BundleInformation info = bundle.getInformation();
			LicenseBasis lb = bundle.getLicense_basis();
			ItemTags tags = bundle.getTags();
			if (tags==null) {
				tags = ItemTags.make();
				bundle.tags(tags);
			}
			pBasics.update((Bundle)bundle);
			pIDs.update((IDs)ids);
			pContributors.update((Bundle)bundle);
			pInformation.update((BundleInformation)info);
			pLicense.update((LicenseBasis)lb);
			pTags.update(tags);
			pFiles.update((Bundle)bundle);
		}
	}

	private void initComponents() {
		Bundle bundle = null;
		IDs ids = null;
		BundleInformation info = null;
		LicenseBasis lb = null;
		pBasics = new PanelBundleBasics(bundle);
		pBasics.addObserver(this);
		
		pIDs = new PanelIDs(ids);
		pIDs.addObserver(this);
		
		pContributors = new PanelContributors(bundle);
		pContributors.addObserver(this);
		
		pInformation = new PanelInformation(info);
		pInformation.setTypeBundle();
		pInformation.addObserver(this);
		
		pLicense = new PanelLicense(lb);
		pLicense.addObserver(this);
		
		pTags = new PanelTags();
		pTags.addObserver(this);
		
		pFiles = new PanelFiles();
		pFiles.setTypeBundle();
		pFiles.addObserver(this);
		
		tabs = new JTabbedPane();
		tabs.add(buildEmbeddedPanel(pIDs),"IDs");
//		tabs.add(new JScrollPane(pContributors),"Contributors");
//		tabs.add(new JScrollPane(pInformation),"Information");
//		tabs.add(new JScrollPane(pLicense),"License");
		tabs.add(buildEmbeddedPanel(pContributors),"Contributors");
		tabs.add(new JScrollPane(pInformation),"Information");
		tabs.add(buildEmbeddedPanel(pLicense),"License");
		tabs.add(new JScrollPane(pTags),"Tags");
		tabs.add(buildEmbeddedPanel(pFiles),"Files");
		
	}
	
	private JPanel buildEmbeddedPanel(JPanel main) {
		JPanel p = new JPanel();
		
		GridBagLayout gbl = new GridBagLayout();
		p.setLayout(gbl);
		GridBagConstraints gbc = new GridBagConstraints();

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
		gbl.setConstraints(new JScrollPane(main),gbc);
		p.add(main);
		
		JLabel filler = new JLabel();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 100.0;
		gbc.weighty = 100.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(filler,gbc);
		p.add(filler);
		
		return p;
	}


	public void initLayout() {
		pBasics.setPreferredSize(new Dimension(800, (int)pBasics.getPreferredSize().getHeight()));
		
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 20.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(pBasics,gbc);
		add(pBasics);

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
		gbl.setConstraints(tabs,gbc);
		add(tabs);


		JLabel label_filler = new JLabel();
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 80.0;
		gbc.weighty = 100.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_filler,gbc);
		add(label_filler);

	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch(Exception ex){
			System.out.println("Nimbus look & feel not available");
		}
		PanelBundle p = new PanelBundle(null);
		JFrame f = new JFrame("PanelBundle");
		f.setContentPane(p);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(1024,768);
		f.setVisible(true);
	}


	// ----- action methods --------------------------------


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
	
	//MyObserver
	public void notifyChange(MyObservable changedIn) {	
		notifyChanges();
	}
}
