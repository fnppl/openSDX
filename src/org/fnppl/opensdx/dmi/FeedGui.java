package org.fnppl.opensdx.dmi;


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
import org.fnppl.opensdx.gui.MyObserver;
import org.fnppl.opensdx.gui.PanelBundle;
import org.fnppl.opensdx.gui.PanelFeedInfo;
import org.fnppl.opensdx.gui.PanelItems;
import org.fnppl.opensdx.gui.SecurityMainFrame;
import org.fnppl.opensdx.security.*;
import org.fnppl.opensdx.xml.*;


public class FeedGui extends JFrame implements MyObserver {
	private static FeedGui instance = null;
	public static FeedGui getInstance() {
		if(instance == null) {
			instance = new FeedGui();
		}
		
		return instance;
	}

	private JTabbedPane jt = null;
	private StatusBar status = null;
	
//	BundlePanel bundle_panel = null;
//	FeedInfoPanel feedinfo_panel = null;
//	BundledItemsPanel bundled_items_panel = null;
	
	PanelFeedInfo feedinfo_panel = null;
	PanelBundle bundle_panel = null;
	PanelItems bundled_items_panel = null;
	
	JPanel treePanel = null;
	
	private Feed currentFeed = null;
	
	
	private FeedGui() {
		super("fnppl.org :: openSDX :: FeedGui");		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				quit();
			}
		});
		
		setSize(1024, 768);
		makeMenuBar();
		Helper.centerMe(this, null);
	}
	
	public void makeMenuBar() {
		ActionListener ja = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String cmd = e.getActionCommand();

				if(cmd.equalsIgnoreCase("quit")) {
					quit();
				}
				else if(cmd.equalsIgnoreCase("init example feed")) {
					init_example_feed();
				}
			}

		};

		JMenuBar jb = new JMenuBar();
		JMenu jm = new JMenu("File");
		jb.add(jm);
		JMenuItem jmi = null;

		jmi = new JMenuItem("init example feed");
		jmi.setActionCommand("init example feed");
		jmi.addActionListener(ja);
		jm.add(jmi);
		
		jmi = new JMenuItem("Quit");
		jmi.setActionCommand("quit");
		jmi.addActionListener(ja);
		jm.add(jmi);
		
		setJMenuBar(jb);
	}
	
	public void notifyChange() {
		update();
	}
	
	protected JComponent makeTextPanel(String text) {
        JPanel panel = new JPanel(false);
        JLabel filler = new JLabel(text);
        filler.setHorizontalAlignment(JLabel.CENTER);
        panel.setLayout(new GridLayout(1, 1));
        panel.add(filler);
        return panel;
    }
    
    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = FeedGui.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
    
    public StatusBar makeStatusBar() {
    	status = new StatusBar();
    	status.setStatus("Init.");
    	return status;
    }
    
	private void buildUi() {
		BorderLayout bl = new BorderLayout();
		JPanel jp = new JPanel();
		setContentPane(jp);
		jp.setLayout(bl);
		
		
//		GridBagLayout gb = new GridBagLayout();
//		
//		JPanel jp = new JPanel();
//		setContentPane(jp);
//		
//		jp.setLayout(gb);
//		
//		GridBagConstraints c = new GridBagConstraints();
//		c.gridx = 0;
//		c.gridy = 0;
//		
//		JButton test = new JButton("test");
//		jp.add(test, c);
//		

		jt = new JTabbedPane();
		JTabbedPane tabbedPane = jt; //ref.
//		ImageIcon icon = createImageIcon("images/middle.gif");

		//feedinfo_panel = new FeedInfoPanel(this);
		//bundle_panel = new BundlePanel(this);
		//bundled_items_panel = new BundledItemsPanel(this);
		
		feedinfo_panel = new PanelFeedInfo();
		feedinfo_panel.addObserver(this);
		

		bundle_panel = new PanelBundle();
		bundle_panel.addObserver(this);
		
		
		bundled_items_panel = new PanelItems();
		bundled_items_panel.addObserver(this);

		treePanel = new JPanel();
		treePanel.setLayout(new BorderLayout());
		
		tabbedPane.addTab("FeedInfo", null, feedinfo_panel, "Does nothing !!!change_me_");
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

		tabbedPane.addTab("Bundle", null,new JScrollPane(bundle_panel), "Does twice as much nothing !!!change_me_");
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

		tabbedPane.addTab("BundledItems", null, bundled_items_panel, "Still does nothing !!!change_me_");
		tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);

		tabbedPane.addTab("Tree", null, treePanel, "");
		tabbedPane.setMnemonicAt(3, KeyEvent.VK_4);

		jp.add(jt, BorderLayout.CENTER);
		
		StatusBar sb = makeStatusBar(); //also sets. class-variable.
		jp.add(sb, BorderLayout.SOUTH);
	}
	
	public Feed getCurrentFeed() {
		return currentFeed;
	}
	
	public void update() {
		if (feedinfo_panel!=null) {
			feedinfo_panel.update(currentFeed);
		}
		if (bundle_panel!=null) {
			bundle_panel.update(currentFeed);
		}
		if (bundled_items_panel!=null) {
			bundled_items_panel.update(currentFeed);
		}
		if (treePanel!=null) {
			if (currentFeed != null) {
				EditBusinessObjectTree tree = new EditBusinessObjectTree(currentFeed);
				treePanel.removeAll();
				treePanel.add(new JScrollPane(tree),BorderLayout.CENTER);
			} else {
				treePanel.removeAll();
			}
		}
	}
	
	public void init_example_feed() {
		currentFeed = FeedCreator.makeExampleFeed();
		update();
	}
	
	public void quit() {
		System.exit(0);
	}
	public static void main(String[] args) {
		//HT 28.02.2011
		//		1. Select/Open Keystore 
		//		2. Save Keystore 
		//		3. Create Key(s) 
		//		4. Create Identities 
		//		5. Modify Identities
		//		6. Add arbitrary (foreign) keys (pubkeys) to keystore
		//		7. Sign arbitrary files
		//		8. Check signature of arbitrary files
		//		10. encrypt arbitrary files (AES)
		//		11. decrypt arbitrary files (AES)
		//		12. Modify Keys (in terms of deletion/revokation/submission to server)

		FeedGui s = FeedGui.getInstance();
		s.buildUi();
		s.setVisible(true);
	}
	
	public static class StatusBar extends JPanel {
		String info = null;
		public StatusBar() {
			super();
			setPreferredSize(new Dimension(100, 30));
			setSize(new Dimension(100, 30));
			setMinimumSize(new Dimension(100, 30));
		}
		
		public void setStatus(String msg) { 
			//dirty. fast.
			JLabel jl = new JLabel(msg);
			removeAll();
			add(jl);
			repaint();
		}
	}
}
