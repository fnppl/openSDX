package org.fnppl.opensdx.gui;


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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;
import org.fnppl.opensdx.common.*;
import org.fnppl.opensdx.dmi.FeedGui;
import org.fnppl.opensdx.dmi.FeedGuiTooltips;
import org.fnppl.opensdx.security.KeyApprovingStore;
import org.fnppl.opensdx.security.OSDXKey;
import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.gui.helper.MyObservable;
import org.fnppl.opensdx.gui.helper.MyObserver;
import org.fnppl.opensdx.gui.helper.PanelContractPartner;
import org.fnppl.opensdx.gui.helper.PanelCreator;
import org.fnppl.opensdx.gui.helper.PanelFeedInfoBasics;
import org.fnppl.opensdx.gui.helper.PanelReceiver;
import org.fnppl.opensdx.gui.helper.PanelSavedDMI;
import org.fnppl.opensdx.gui.helper.PanelTriggeredActions;

public class PanelFeedInfo extends javax.swing.JPanel implements MyObservable, MyObserver {

    private FeedInfo feedinfo = null;
    private PanelFeedInfo me;
    private File lastDir = null;
    
    private PanelFeedInfoBasics pBasis;
    private PanelTriggeredActions pTriggeredActions;
    private PanelCreator pCreator;
    private PanelContractPartner pSender;
    private PanelContractPartner pLicensor;
    private PanelContractPartner pLicensee;
    private PanelReceiver pReceiver;
    private FeedGui gui;

    private Vector<MyObserver> observers = new Vector<MyObserver>();
    public void addObserver(MyObserver observer) {
    	observers.add(observer);
    }

    public void update(FeedInfo fi) {
        if (fi==null) {
            long now = System.currentTimeMillis();
            ContractPartner licensee = ContractPartner.make(ContractPartner.ROLE_LICENSEE,"","");
            fi = FeedInfo.make(true, "",now, now, ContractPartner.make(ContractPartner.ROLE_SENDER, "", ""), ContractPartner.make(ContractPartner.ROLE_LICENSOR, "", ""), licensee);
        }
        this.feedinfo = fi;
        
        pBasis.update();
        
        pSender.update();
        pCreator.update();
        
        pLicensor.update();
        pLicensee.update();
        
        pReceiver.update();
        pTriggeredActions.update();
        
    }

    public PanelFeedInfo(FeedGui gui) {
        super();
        this.gui = gui;
        me = this;
        
        File f = new File(lastDir,"openSDX");
        if (f.exists() && f.isDirectory()) {
            lastDir = f;
        }
        initComponents();
        initLayout();
        initTooltips();
    }
    
    public void initTooltips() {
    	pBasis.initTooltips();
		pSender.initTooltips();
		pLicensor.initTooltips();
		pLicensee.initTooltips();
		pReceiver.setToolTipText(FeedGuiTooltips.receiver);
		pCreator.setToolTipText(FeedGuiTooltips.creator);
		pTriggeredActions.setToolTipText(FeedGuiTooltips.triggered_actions);
	}

    public void notifyChange(MyObservable changedIn) {
        notifyChanges();
    }

     public void notifyChanges() {
        for (MyObserver ob : observers) {
            ob.notifyChange(this);
        }
    }

    private void initComponents() {
    	pBasis = new PanelFeedInfoBasics(gui);
    	pBasis.addObserver(this);
    	    
		pCreator = new PanelCreator(gui);
		pCreator.addObserver(this);
		
		pTriggeredActions = new PanelTriggeredActions(gui);
        pTriggeredActions.addObserver(this);
		
		pSender = new PanelContractPartner(gui, ContractPartner.ROLE_SENDER);
		pSender.addObserver(this);
		
		pLicensor = new PanelContractPartner(gui, ContractPartner.ROLE_LICENSOR);
		pLicensor.addObserver(this);
		
		pLicensee = new PanelContractPartner(gui, ContractPartner.ROLE_LICENSEE);
		pLicensee.addObserver(this);
		
		pReceiver = new PanelReceiver(gui);
        pReceiver.addObserver(this);
        
        Dimension dMin = new Dimension(300,100);
        Dimension dMax = new Dimension(450,600);
        Dimension dPref = new Dimension(450, (int)pSender.getPreferredSize().getHeight());
        
        setSizes(pCreator,dMin,dMax,dPref);
        setSizes(pSender,dMin,dMax,dPref);
        setSizes(pLicensor,dMin,dMax,dPref);
        setSizes(pLicensee,dMin,dMax,dPref);

        
       // setSizes(pBasis,dMin,dMax,dPref);
        
        dPref = new Dimension(450, 350);
        setSizes(pTriggeredActions,dMin,dMax,dPref);
        setSizes(pReceiver,dMin,dMax,dPref);
        
    }
    private void setSizes(JPanel p, Dimension dMin, Dimension dMax, Dimension dPref) {
    	p.setMinimumSize(dMin);
        p.setMaximumSize(dMax);
        p.setPreferredSize(dPref);
        
    }
     
    private void initLayout() {
    	GridBagLayout gbl = new GridBagLayout();
    	setLayout(gbl);
    	GridBagConstraints gbc = new GridBagConstraints();

    	gbc.gridx = 0;
    	gbc.gridy = 0;
    	gbc.gridwidth = 2;
    	gbc.gridheight = 1;
    	gbc.weightx = 50.0;
    	gbc.weighty = 0.0;
    	gbc.anchor = GridBagConstraints.CENTER;
    	gbc.fill = GridBagConstraints.BOTH;
    	gbc.ipadx = 0;
    	gbc.ipady = 0;
    	gbc.insets = new Insets(5,5,5,5);
    	gbl.setConstraints(pBasis,gbc);
    	add(pBasis);
    	
    	gbc.gridx = 0;
    	gbc.gridy = 1;
    	gbc.gridwidth = 1;
    	gbc.gridheight = 1;
    	gbc.weightx = 50.0;
    	gbc.weighty = 0.0;
    	gbc.anchor = GridBagConstraints.CENTER;
    	gbc.fill = GridBagConstraints.BOTH;
    	gbl.setConstraints(pSender,gbc);
    	add(pSender);
    	
    	gbc.gridx = 1;
    	gbc.gridy = 1;
    	gbc.gridwidth = 1;
    	gbc.gridheight = 1;
    	gbc.weightx = 50.0;
    	gbc.weighty = 0.0;
    	gbc.anchor = GridBagConstraints.CENTER;
    	gbc.fill = GridBagConstraints.BOTH;
    	gbl.setConstraints(pCreator,gbc);
    	add(pCreator);
    	
    	gbc.gridx = 0;
    	gbc.gridy = 2;
    	gbc.gridwidth = 1;
    	gbc.gridheight = 1;
    	gbc.weightx = 50.0;
    	gbc.weighty = 0.0;
    	gbc.anchor = GridBagConstraints.CENTER;
    	gbc.fill = GridBagConstraints.BOTH;
    	gbl.setConstraints(pLicensor,gbc);
    	add(pLicensor);
    	
    	gbc.gridx = 1;
    	gbc.gridy = 2;
    	gbc.gridwidth = 1;
    	gbc.gridheight = 1;
    	gbc.weightx = 50.0;
    	gbc.weighty = 0.0;
    	gbc.anchor = GridBagConstraints.CENTER;
    	gbc.fill = GridBagConstraints.BOTH;
    	gbl.setConstraints(pLicensee,gbc);
    	add(pLicensee);
    	
    	gbc.gridx = 0;
    	gbc.gridy = 3;
    	gbc.gridwidth = 1;
    	gbc.gridheight = 1;
    	gbc.weightx = 50.0;
    	gbc.weighty = 0.0;
    	gbc.anchor = GridBagConstraints.CENTER;
    	gbc.fill = GridBagConstraints.BOTH;
    	gbl.setConstraints(pReceiver,gbc);
    	add(pReceiver);
    	
    	gbc.gridx = 1;
    	gbc.gridy = 3;
    	gbc.gridwidth = 1;
    	gbc.gridheight = 1;
    	gbc.weightx = 50.0;
    	gbc.weighty = 0.0;
    	gbc.anchor = GridBagConstraints.CENTER;
    	gbc.fill = GridBagConstraints.BOTH;
    	gbl.setConstraints(pTriggeredActions,gbc);
    	add(pTriggeredActions);
    	
    	JLabel filler = new JLabel();
    	gbc.gridx = 0;
    	gbc.gridy = 4;
    	gbc.gridwidth = 2;
    	gbc.gridheight = 1;
    	gbc.weightx = 100.0;
    	gbc.weighty = 100.0;
    	gbc.anchor = GridBagConstraints.CENTER;
    	gbc.fill = GridBagConstraints.BOTH;
    	gbl.setConstraints(filler,gbc);
    	add(filler);
    	

    }

}
