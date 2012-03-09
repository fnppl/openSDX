package org.fnppl.opensdx.dmi;

/*
 * Copyright (C) 2010-2012 
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


import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.*;

import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;

import org.fnppl.opensdx.common.Feed;
import org.fnppl.opensdx.gui.EditBusinessObjectTree;
import org.fnppl.opensdx.gui.Helper;
import org.fnppl.opensdx.gui.SecurityMainFrame;
import org.fnppl.opensdx.security.*;
import org.fnppl.opensdx.xml.*;

public class BundledItemsPanel extends JPanel {
	private JPanel mainContent = null;
	
	public BundledItemsPanel() {
		super();
		buildUi();
	}
	
	public void update() {
		Feed feed = FeedGui.getInstance().getCurrentFeed();
		if (feed != null && feed.getSingleItems(0)!=null) {
			EditBusinessObjectTree tree = new EditBusinessObjectTree(feed.getSingleItems(0));
			int anz = mainContent.getComponentCount();
			mainContent.removeAll();
			mainContent.add(new JScrollPane(tree),BorderLayout.CENTER);
		} else {
			mainContent.removeAll();
		}
	}
	
	private void buildUi() {
		setLayout(new BorderLayout());
		mainContent = new JPanel();
		mainContent.setLayout(new BorderLayout());
		add(mainContent, BorderLayout.CENTER);
		update();
	
//		GridBagLayout gb = new GridBagLayout();
//		GridBagConstraints c = new GridBagConstraints();
//		c.gridx = 0; c.gridy = 0;
//		c.anchor = GridBagConstraints.NORTHWEST;
//		
//		setLayout(gb);
//
//		JLabel l = new JLabel("Me is BundledItemsPanel.java");
//		add(l, c);
//		
//		
//
//		c.gridy++;
//		c.gridx++;
//		JLabel filler = new JLabel(); //invisible
//		c.weightx = 1.0;
//		c.weighty = 1.0;
//		add(filler, c);
	}
}
