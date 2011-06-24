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
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.Vector;
import javax.swing.DefaultListModel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.fnppl.opensdx.common.Bundle;
import org.fnppl.opensdx.common.BundleInformation;
import org.fnppl.opensdx.common.Contributor;
import org.fnppl.opensdx.common.IDs;
import org.fnppl.opensdx.common.Item;
import org.fnppl.opensdx.common.LicenseBasis;
import org.fnppl.opensdx.common.Territorial;
import org.fnppl.opensdx.security.SecurityHelper;

/**
 *
 * @author Bertram Boedeker <bboedeker@gmx.de>
 */
public class PanelItems extends javax.swing.JPanel implements MyObservable, MyObserver {


    private Bundle bundle;
    private DocumentChangeListener changeListener;
    private PanelFeedInfo me;

    private Vector<MyObserver> observers = new Vector<MyObserver>();
    public void addObserver(MyObserver observer) {
    	observers.add(observer);
    }

    public void notifyChange() {
      //check for  changes in bundle contributors
      if (bundle==null || bundle.getItemsCount()==0) return;
      int selItem = list_items.getSelectedIndex();
      if (selItem<0) return;
      Item item = bundle.getItem(selItem);
      updateContributorList(item);
    }

     public void update(Bundle bundle) {
        this.bundle = bundle;
        updateItemList();
        if (bundle!=null) {
            int count = bundle.getItemsCount();
            if (count >0) {
                list_items.setSelectedIndex(0);
                Item item = bundle.getItem(0);
                updateItem(item);
            }
        }
    }

     private void updateItemList() {
        DefaultListModel lm = new DefaultListModel();
    	 if (bundle!=null) {
	        int anz = bundle.getItemsCount();
	        for (int i = 0; i < anz; i++) {
	            lm.addElement("Item: "+bundle.getItem(i).getDisplayname());
	        }
    	 }
        list_items.setModel(lm);
        list_items.setSelectedIndex(0);
        list_items.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list_items.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int sel = e.getFirstIndex();
                if (sel >= 0 && sel < bundle.getItemsCount()) {
                    Item item = bundle.getItem(sel);
                    updateItem(item);
                }
            }
	 });
    }


      private void updateItem(Item item) {
        if (item != null) {

         //basics
        text_display_artist.setText(item.getDisplay_artist());
        text_displayname.setText(item.getDisplayname());
        text_name.setText(item.getName());
        text_version.setText(item.getVersion());
        select_type.setSelectedItem(item.getType());

        updateContributorList(item);


        IDs ids = item.getIds();
        if (ids != null) {
            text_contentauthid.setText(ids.getContentauthid());
            text_amazon.setText(ids.getAmzn());
            text_finetunesid.setText(ids.getFinetunesid());
            text_grid.setText(ids.getGrid());
            text_isbn.setText(ids.getIsbn());
            text_isrc.setText(ids.getIsrc());
            text_labelordernum.setText(ids.getLabelordernum());
            text_ourid.setText(ids.getOurid());
            text_upc.setText(ids.getUpc());
            text_yourid.setText(ids.getYourid());
        }

        //information
        BundleInformation info = item.getInformation();
        if (info != null) {
            text_physical_realease_datetime.setText(info.getPhysicalReleaseDatetimeText());
            text_digital_release_datetime.setText(info.getDigitalReleaseDatetimeText());
            table_promotext.setModel(new PromotextTableModel(info,this));
        }

        //License
        LicenseBasis lb = item.getLicense_basis();
        if (lb!=null) {
            if (lb.isAsOnBundle()) {
                checkLicenseAsOnBundle.setSelected(true);
                panelLicense1.setVisible(false);
                checkLicenseAsOnBundle.setSelected(true);
                panelLicense1.setVisible(false);
                text_license_from_datetime.setText("");
                text_license_to_datetime.setText("");
                select_license_pricing.setSelectedIndex(0);
                text_license_pricing.setText("");
                text_license_pricing.setEnabled(true);
                DefaultListModel lmAllow = (DefaultListModel)list_allowed_territories.getModel();
                DefaultListModel lmDisallow = (DefaultListModel)list_disallowed_territories.getModel();
                lmAllow.removeAllElements();
                lmDisallow.removeAllElements();
            } else {
                checkLicenseAsOnBundle.setSelected(false);
                panelLicense1.setVisible(true);
                text_license_from_datetime.setText(lb.getTimeframeFromText());
                text_license_to_datetime.setText(lb.getTimeframeToText());
                if (lb.getPricingPricecode()==null) {
                    select_license_pricing.setSelectedItem(lb.getPricingPricecode());
                    text_license_pricing.setText("");
                    text_license_pricing.setEnabled(false);
                } else {
                    select_license_pricing.setSelectedIndex(0);
                    text_license_pricing.setText(lb.getPricingWholesale());
                    text_license_pricing.setEnabled(true);
                }
                DefaultListModel lmAllow = (DefaultListModel)list_allowed_territories.getModel();
                DefaultListModel lmDisallow = (DefaultListModel)list_disallowed_territories.getModel();
                lmAllow.removeAllElements();
                lmDisallow.removeAllElements();
                Territorial t = lb.getTerritorial();
                if (t!=null) {
                int count = t.getTerritorialCount();
                    for (int i=0;i<count;i++) {
                        if (t.isTerritoryAllowed(i)) {
                            lmAllow.addElement(t.getTerritory(i));
                        } else {
                            lmDisallow.addElement(t.getTerritory(i));
                        }
                    }
                }
            }
        } else {
            checkLicenseAsOnBundle.setSelected(true);
            panelLicense1.setVisible(false);
            text_license_from_datetime.setText("");
            text_license_to_datetime.setText("");
            select_license_pricing.setSelectedIndex(0);
            text_license_pricing.setText("");
            text_license_pricing.setEnabled(true);
            DefaultListModel lmAllow = (DefaultListModel)list_allowed_territories.getModel();
            DefaultListModel lmDisallow = (DefaultListModel)list_disallowed_territories.getModel();
            lmAllow.removeAllElements();
            lmDisallow.removeAllElements();
        }
        checkLicenseAsOnBundleActionPerformed(null);
        
        changeListener.saveStates();
        }
    }

     private void updateContributorList(Item item) {
        int anzContributors = bundle.getContributorCount();
        DefaultListModel lm = new DefaultListModel();
        for (int i = 0; i < anzContributors; i++) {
            lm.addElement(bundle.getContributor(i).getName() + " (" + bundle.getContributor(i).getType() + ")");
        }
        list_all_contributors.setModel(lm);
        list_all_contributors.setSelectedIndex(0);
        list_all_contributors.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        if (item!=null) {
            anzContributors = item.getContributorCount();
            lm = new DefaultListModel();
            for (int i = 0; i < anzContributors; i++) {
                lm.addElement(item.getContributor(i).getName() + " (" + bundle.getContributor(i).getType() + ")");
            }
            list_contributors.setModel(lm);
            list_contributors.setSelectedIndex(0);
            list_contributors.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         }
    }
    
     public static void main(String[] args) {
        Field[] fields = PanelItems.class.getDeclaredFields();
        
        for (Field f : fields) {
            f.setAccessible(true);
            if (f.getName().startsWith("text_")) {
                System.out.println("texts.add("+f.getName()+ ");");
            }
        }
    }

     private void initChangeListeners() {
        Vector<JTextField> texts = new Vector<JTextField>();
        //basics
        texts.add(text_displayname);
        texts.add(text_name);
        texts.add(text_version);
        texts.add(text_display_artist);

        //ids
        texts.add(text_amazon);
        texts.add(text_contentauthid);
        texts.add(text_finetunesid);
        texts.add(text_grid);
        texts.add(text_isbn);
        texts.add(text_isrc);
        texts.add(text_labelordernum);
        texts.add(text_ourid);
        texts.add(text_upc);
        texts.add(text_yourid);

        //information
        texts.add(text_digital_release_datetime);
        texts.add(text_physical_realease_datetime);
        texts.add(text_playlength);

        //license
        texts.add(text_license_from_datetime);
        texts.add(text_license_pricing);
        texts.add(text_license_to_datetime);


        changeListener = new DocumentChangeListener(texts);

         KeyAdapter keyAdapt = new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER){
                    if (e.getComponent() instanceof JTextField) {
                        JTextField text = (JTextField)e.getComponent();
                        String t = text.getText();
                        if (t.equals("")) t = null;
                        int sel = list_items.getSelectedIndex();
                        if (sel<0) return;
                        Item item = bundle.getItem(sel);
                        try {
                            IDs ids = item.getIds();
                            if (text == text_displayname) {
                                item.displayname(t);
                                ((DefaultListModel)list_items.getModel()).set(sel, "Item: "+t);
                            }
                            else if (text == text_name) item.name(t);
                            else if (text == text_version) item.version(t);
                            else if (text == text_display_artist) item.display_artist(t);

                            else if(text == text_amazon) ids.amzn(t);
                            else if(text == text_contentauthid) ids.contentauthid(t);
                            else if(text == text_finetunesid) ids.finetunesid(t);
                            else if(text == text_grid) ids.grid(t);
                            else if(text == text_isbn) ids.isbn(t);
                            else if(text == text_isrc) ids.isrc(t);
                            else if(text == text_labelordernum) ids.labelordernum(t);
                            else if(text == text_ourid) ids.ourid(t);
                            else if(text == text_upc) ids.upc(t);
                            else if(text == text_yourid) ids.yourid(t);

                            else if(text == text_digital_release_datetime) item.getInformation().digital_release_datetime(SecurityHelper.parseDate(t));
                            else if(text == text_physical_realease_datetime) item.getInformation().physical_release_datetime(SecurityHelper.parseDate(t));
                            else if(text == text_playlength) {
                                if (t==null) {
                                    item.getInformation().playlength(-1);
                                } else {
                                    item.getInformation().playlength(Integer.parseInt(t));
                                }
                            }
                            else if(text == text_license_from_datetime) item.getLicense_basis().timeframe_from_datetime(SecurityHelper.parseDate(t));
                            else if(text == text_license_to_datetime) item.getLicense_basis().timeframe_to_datetime(SecurityHelper.parseDate(t));
                            else if(text == text_license_pricing) {
                                item.getLicense_basis().pricing_pricecode(null);
                                item.getLicense_basis().pricing_wholesale(t);
                            }
                            text.setBackground(Color.WHITE);
                            changeListener.saveState(text);
                            notifyChanges();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                    }
                }
                else if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    if (e.getComponent() instanceof JTextField) {
                        JTextField text = (JTextField)e.getComponent();
                        text.setText(changeListener.getSavedText(text));
                        text.setBackground(Color.WHITE);
                    }
                }
            }
        };


        for (JTextField text : texts) {
            text.getDocument().addDocumentListener(changeListener);
            text.addKeyListener(keyAdapt);
        }

    }

    public void notifyChanges() {
        for (MyObserver ob : observers) {
            ob.notifyChange();
        }
    }
    /** Creates new form PanelItems */
    public PanelItems() {
        initComponents();
        checkLicenseAsOnBundleStateChanged(null);
        text_digital_release_datetime.setName("datetime");
        text_physical_realease_datetime.setName("datetime");
        text_license_from_datetime.setName("datetime");
        text_license_to_datetime.setName("datetime");
        text_playlength.setName("integer");

        list_allowed_territories.setModel(new DefaultListModel());
        list_disallowed_territories.setModel(new DefaultListModel());
        list_items.setModel(new DefaultListModel());
        
        initChangeListeners();

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelBasics = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        text_displayname = new javax.swing.JTextField();
        text_name = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        text_version = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        select_type = new javax.swing.JComboBox();
        text_display_artist = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        panelIDsBig = new javax.swing.JPanel();
        panelIDs = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        text_grid = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        text_upc = new javax.swing.JTextField();
        text_isrc = new javax.swing.JTextField();
        text_contentauthid = new javax.swing.JTextField();
        text_labelordernum = new javax.swing.JTextField();
        text_amazon = new javax.swing.JTextField();
        text_isbn = new javax.swing.JTextField();
        text_finetunesid = new javax.swing.JTextField();
        text_ourid = new javax.swing.JTextField();
        text_yourid = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        panelContributors = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        list_contributors = new javax.swing.JList();
        bu_add_contributor = new javax.swing.JButton();
        bu_remove_contributor = new javax.swing.JButton();
        jLabel19 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        list_all_contributors = new javax.swing.JList();
        panelInformation = new javax.swing.JPanel();
        text_physical_realease_datetime = new javax.swing.JTextField();
        jLabel30 = new javax.swing.JLabel();
        text_digital_release_datetime = new javax.swing.JTextField();
        jLabel31 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        table_promotext = new javax.swing.JTable();
        bu_add_promotext = new javax.swing.JButton();
        bu_remove_promotext = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        text_playlength = new javax.swing.JTextField();
        panelLicense = new javax.swing.JPanel();
        checkLicenseAsOnBundle = new javax.swing.JCheckBox();
        panelLicense1 = new javax.swing.JPanel();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        text_license_from_datetime = new javax.swing.JTextField();
        text_license_to_datetime = new javax.swing.JTextField();
        text_license_pricing = new javax.swing.JTextField();
        jLabel34 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        list_disallowed_territories = new javax.swing.JList();
        jScrollPane6 = new javax.swing.JScrollPane();
        list_allowed_territories = new javax.swing.JList();
        bu_add_allowed_territory = new javax.swing.JButton();
        bu_remove_allowed_territory = new javax.swing.JButton();
        bu_remove_disallowed_territory = new javax.swing.JButton();
        bu_add_disallowed_territory = new javax.swing.JButton();
        add_new_territory_text = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        select_license_pricing = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        list_items = new javax.swing.JList();
        bu_add_item = new javax.swing.JButton();
        bu_remove_item = new javax.swing.JButton();

        panelBasics.setBorder(javax.swing.BorderFactory.createTitledBorder("Item Basics"));

        jLabel4.setText("name");

        jLabel5.setText("display name");

        text_displayname.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                text_displaynameActionPerformed(evt);
            }
        });

        jLabel6.setText("version");

        jLabel7.setText("type");

        select_type.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "audio", "video" }));

        text_display_artist.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                text_display_artistActionPerformed(evt);
            }
        });

        jLabel2.setText("display artist");

        javax.swing.GroupLayout panelBasicsLayout = new javax.swing.GroupLayout(panelBasics);
        panelBasics.setLayout(panelBasicsLayout);
        panelBasicsLayout.setHorizontalGroup(
            panelBasicsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBasicsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelBasicsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel4)
                    .addComponent(jLabel2))
                .addGap(18, 18, 18)
                .addGroup(panelBasicsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(text_display_artist, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(text_name, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
                    .addComponent(text_displayname, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE))
                .addGap(37, 37, 37)
                .addGroup(panelBasicsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 48, Short.MAX_VALUE)
                .addGroup(panelBasicsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(select_type, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(text_version, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelBasicsLayout.setVerticalGroup(
            panelBasicsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBasicsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelBasicsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(text_displayname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(text_version, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelBasicsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelBasicsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel4)
                        .addComponent(text_name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelBasicsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel7)
                        .addComponent(select_type, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelBasicsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(text_display_artist, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addContainerGap(21, Short.MAX_VALUE))
        );

        panelIDs.setBorder(javax.swing.BorderFactory.createTitledBorder("IDs"));

        jLabel8.setText("grid");

        jLabel9.setText("upc");

        jLabel10.setText("isrc");

        jLabel11.setText("content auth id");

        jLabel12.setText("label order num");

        jLabel13.setText("amazon");

        jLabel14.setText("isbn");

        jLabel15.setText("finetunes id");

        jLabel16.setText("our id");

        jLabel17.setText("your id");

        javax.swing.GroupLayout panelIDsLayout = new javax.swing.GroupLayout(panelIDs);
        panelIDs.setLayout(panelIDsLayout);
        panelIDsLayout.setHorizontalGroup(
            panelIDsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelIDsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelIDsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelIDsLayout.createSequentialGroup()
                        .addGroup(panelIDsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addComponent(jLabel10)
                            .addComponent(jLabel9))
                        .addGap(95, 95, 95)
                        .addGroup(panelIDsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(text_isrc, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
                            .addComponent(text_grid, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
                            .addComponent(text_upc, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)))
                    .addGroup(panelIDsLayout.createSequentialGroup()
                        .addGroup(panelIDsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11)
                            .addComponent(jLabel12)
                            .addComponent(jLabel13)
                            .addComponent(jLabel14)
                            .addComponent(jLabel15)
                            .addComponent(jLabel16)
                            .addComponent(jLabel17))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelIDsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(text_labelordernum, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
                            .addComponent(text_contentauthid, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
                            .addComponent(text_amazon, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
                            .addComponent(text_isbn, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
                            .addComponent(text_finetunesid, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
                            .addComponent(text_ourid, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
                            .addComponent(text_yourid, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE))))
                .addContainerGap())
        );
        panelIDsLayout.setVerticalGroup(
            panelIDsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelIDsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelIDsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(text_grid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelIDsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(text_upc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelIDsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel10)
                    .addComponent(text_isrc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelIDsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(text_contentauthid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelIDsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(text_labelordernum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelIDsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(text_amazon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelIDsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(text_isbn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelIDsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(text_finetunesid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelIDsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(text_ourid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelIDsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(text_yourid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelIDsBigLayout = new javax.swing.GroupLayout(panelIDsBig);
        panelIDsBig.setLayout(panelIDsBigLayout);
        panelIDsBigLayout.setHorizontalGroup(
            panelIDsBigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 687, Short.MAX_VALUE)
            .addGroup(panelIDsBigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelIDsBigLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(panelIDs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(324, Short.MAX_VALUE)))
        );
        panelIDsBigLayout.setVerticalGroup(
            panelIDsBigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 704, Short.MAX_VALUE)
            .addGroup(panelIDsBigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelIDsBigLayout.createSequentialGroup()
                    .addGap(19, 19, 19)
                    .addComponent(panelIDs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(300, Short.MAX_VALUE)))
        );

        jTabbedPane1.addTab("IDs", panelIDsBig);

        jLabel18.setFont(new java.awt.Font("Ubuntu", 1, 15));
        jLabel18.setText("List of contributors for this item");

        list_contributors.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Contributor 1", "Contributor 2", "Contributor 3", "Contributor 4", "Contributor 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(list_contributors);

        bu_add_contributor.setText("add");
        bu_add_contributor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bu_add_contributorActionPerformed(evt);
            }
        });

        bu_remove_contributor.setText("remove");
        bu_remove_contributor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bu_remove_contributorActionPerformed(evt);
            }
        });

        jLabel19.setFont(new java.awt.Font("Ubuntu", 1, 15));
        jLabel19.setText("List of all contributors");

        list_all_contributors.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Contributor 1", "Contributor 2", "Contributor 3", "Contributor 4", "Contributor 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane3.setViewportView(list_all_contributors);

        javax.swing.GroupLayout panelContributorsLayout = new javax.swing.GroupLayout(panelContributors);
        panelContributors.setLayout(panelContributorsLayout);
        panelContributorsLayout.setHorizontalGroup(
            panelContributorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelContributorsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelContributorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel18)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bu_remove_contributor))
                .addGap(79, 79, 79)
                .addGroup(panelContributorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel19)
                    .addComponent(bu_add_contributor, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(100, Short.MAX_VALUE))
        );

        panelContributorsLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jScrollPane2, jScrollPane3});

        panelContributorsLayout.setVerticalGroup(
            panelContributorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelContributorsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelContributorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(panelContributorsLayout.createSequentialGroup()
                        .addComponent(jLabel18)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelContributorsLayout.createSequentialGroup()
                        .addComponent(jLabel19)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane3, 0, 0, Short.MAX_VALUE)))
                .addGroup(panelContributorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelContributorsLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bu_remove_contributor))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelContributorsLayout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(bu_add_contributor)))
                .addContainerGap(499, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Contributors", panelContributors);

        panelInformation.setBorder(javax.swing.BorderFactory.createTitledBorder("Information"));

        jLabel30.setText("physical release date");

        jLabel31.setText("digital release date");

        table_promotext.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"", null, null}
            },
            new String [] {
                "Language", "Promotext", "Teasertext"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane4.setViewportView(table_promotext);

        bu_add_promotext.setText("add");
        bu_add_promotext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bu_add_promotextActionPerformed(evt);
            }
        });

        bu_remove_promotext.setText("remove");
        bu_remove_promotext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bu_remove_promotextActionPerformed(evt);
            }
        });

        jLabel1.setText("playlength in seconds");

        javax.swing.GroupLayout panelInformationLayout = new javax.swing.GroupLayout(panelInformation);
        panelInformation.setLayout(panelInformationLayout);
        panelInformationLayout.setHorizontalGroup(
            panelInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelInformationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelInformationLayout.createSequentialGroup()
                        .addComponent(bu_add_promotext, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bu_remove_promotext))
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 651, Short.MAX_VALUE)
                    .addGroup(panelInformationLayout.createSequentialGroup()
                        .addGroup(panelInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel30)
                            .addComponent(jLabel31)
                            .addComponent(jLabel1))
                        .addGap(18, 18, 18)
                        .addGroup(panelInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(text_playlength)
                            .addComponent(text_physical_realease_datetime)
                            .addComponent(text_digital_release_datetime, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE))))
                .addContainerGap())
        );
        panelInformationLayout.setVerticalGroup(
            panelInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelInformationLayout.createSequentialGroup()
                .addGroup(panelInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel30)
                    .addComponent(text_physical_realease_datetime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(text_digital_release_datetime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel31))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(text_playlength, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGap(19, 19, 19)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bu_add_promotext)
                    .addComponent(bu_remove_promotext))
                .addContainerGap(343, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Information", panelInformation);

        panelLicense.setBorder(javax.swing.BorderFactory.createTitledBorder("License basis"));

        checkLicenseAsOnBundle.setSelected(true);
        checkLicenseAsOnBundle.setText("as on bundle");
        checkLicenseAsOnBundle.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                checkLicenseAsOnBundleStateChanged(evt);
            }
        });
        checkLicenseAsOnBundle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkLicenseAsOnBundleActionPerformed(evt);
            }
        });

        panelLicense1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jLabel32.setText("timeframe from");

        jLabel33.setText("timeframe to");

        jLabel34.setText("pricing");

        list_disallowed_territories.setBorder(javax.swing.BorderFactory.createTitledBorder("Disallowed Territories"));
        jScrollPane5.setViewportView(list_disallowed_territories);

        list_allowed_territories.setBorder(javax.swing.BorderFactory.createTitledBorder("Allowed Territories"));
        jScrollPane6.setViewportView(list_allowed_territories);

        bu_add_allowed_territory.setText("add allowed");
        bu_add_allowed_territory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bu_add_allowed_territoryActionPerformed(evt);
            }
        });

        bu_remove_allowed_territory.setText("remove");
        bu_remove_allowed_territory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bu_remove_allowed_territoryActionPerformed(evt);
            }
        });

        bu_remove_disallowed_territory.setText("remove");
        bu_remove_disallowed_territory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bu_remove_disallowed_territoryActionPerformed(evt);
            }
        });

        bu_add_disallowed_territory.setText("add disallowed");
        bu_add_disallowed_territory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bu_add_disallowed_territoryActionPerformed(evt);
            }
        });

        jLabel23.setText("new terrotory");

        select_license_pricing.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "[other]", "LOW", "MEDIUM", "HIGH" }));
        select_license_pricing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                select_license_pricingActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelLicense1Layout = new javax.swing.GroupLayout(panelLicense1);
        panelLicense1.setLayout(panelLicense1Layout);
        panelLicense1Layout.setHorizontalGroup(
            panelLicense1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLicense1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelLicense1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelLicense1Layout.createSequentialGroup()
                        .addComponent(jLabel23)
                        .addGap(44, 44, 44)
                        .addGroup(panelLicense1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelLicense1Layout.createSequentialGroup()
                                .addComponent(bu_add_allowed_territory)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 160, Short.MAX_VALUE))
                            .addComponent(add_new_territory_text, javax.swing.GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)))
                    .addGroup(panelLicense1Layout.createSequentialGroup()
                        .addGroup(panelLicense1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(bu_remove_allowed_territory))
                        .addGap(26, 26, 26)
                        .addGroup(panelLicense1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(bu_remove_disallowed_territory)
                            .addGroup(panelLicense1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(bu_add_disallowed_territory)
                                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(panelLicense1Layout.createSequentialGroup()
                        .addGroup(panelLicense1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel33)
                            .addComponent(jLabel34)
                            .addComponent(jLabel32))
                        .addGap(39, 39, 39)
                        .addGroup(panelLicense1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(panelLicense1Layout.createSequentialGroup()
                                .addComponent(select_license_pricing, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addComponent(text_license_pricing, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(text_license_to_datetime, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                            .addComponent(text_license_from_datetime, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE))))
                .addGap(24, 24, 24))
        );
        panelLicense1Layout.setVerticalGroup(
            panelLicense1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLicense1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelLicense1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(text_license_from_datetime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(panelLicense1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel33)
                    .addComponent(text_license_to_datetime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(panelLicense1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel34)
                    .addComponent(text_license_pricing, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(select_license_pricing, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addGroup(panelLicense1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelLicense1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bu_remove_allowed_territory)
                    .addComponent(bu_remove_disallowed_territory))
                .addGap(18, 18, 18)
                .addGroup(panelLicense1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(add_new_territory_text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelLicense1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bu_add_allowed_territory)
                    .addComponent(bu_add_disallowed_territory))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelLicenseLayout = new javax.swing.GroupLayout(panelLicense);
        panelLicense.setLayout(panelLicenseLayout);
        panelLicenseLayout.setHorizontalGroup(
            panelLicenseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLicenseLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelLicenseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelLicense1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(checkLicenseAsOnBundle))
                .addContainerGap(226, Short.MAX_VALUE))
        );
        panelLicenseLayout.setVerticalGroup(
            panelLicenseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLicenseLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkLicenseAsOnBundle)
                .addGap(18, 18, 18)
                .addComponent(panelLicense1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(173, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("License", panelLicense);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 687, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 704, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Tags", jPanel2);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 687, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 704, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Files", jPanel3);

        jLabel3.setFont(new java.awt.Font("Ubuntu", 1, 15));
        jLabel3.setText("List of items");

        list_items.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(list_items);

        bu_add_item.setText("add");
        bu_add_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bu_add_itemActionPerformed(evt);
            }
        });

        bu_remove_item.setText("remove");
        bu_remove_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bu_remove_itemActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelBasics, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 695, Short.MAX_VALUE)
                    .addComponent(jLabel3)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 490, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(bu_remove_item)
                            .addComponent(bu_add_item, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {bu_add_item, bu_remove_item});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(bu_add_item)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bu_remove_item)))
                .addGap(18, 18, 18)
                .addComponent(panelBasics, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void bu_add_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_add_itemActionPerformed
       if (bundle!=null) {
           long now = System.currentTimeMillis();
           Item newItem = Item.make(IDs.make(), "new item", "", "", "audio", "", BundleInformation.make(now,now), LicenseBasis.makeAsOnBundle(),null);
           bundle.addItem(newItem);
           ((DefaultListModel)list_items.getModel()).addElement("Item: "+newItem.getDisplayname());
           list_items.setSelectedIndex(list_items.getModel().getSize()-1);
           updateItem(newItem);
       }
}//GEN-LAST:event_bu_add_itemActionPerformed

    private void bu_add_contributorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_add_contributorActionPerformed
        if (bundle==null || bundle.getItemsCount()==0) return;
        int selItem = list_items.getSelectedIndex();
        if (selItem<0) return;
        Item item = bundle.getItem(selItem);

        int sel = list_all_contributors.getSelectedIndex();
        if (sel<0) return;

        Contributor c = bundle.getContributor(sel);
        item.addContributor(c);
        ((DefaultListModel)list_contributors.getModel()).addElement(list_all_contributors.getSelectedValue());
        notifyChanges();
}//GEN-LAST:event_bu_add_contributorActionPerformed

    private void bu_add_promotextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_add_promotextActionPerformed
        if (bundle==null || bundle.getItemsCount()==0) return;
        int sel = list_items.getSelectedIndex();
        if (sel<0) return;
        
        Item item = bundle.getItem(sel);
        item.getInformation().addPromotext("NEW_LANG", "");
        table_promotext.setModel(new PromotextTableModel(item.getInformation(),this));
        notifyChanges();

}//GEN-LAST:event_bu_add_promotextActionPerformed

    private void checkLicenseAsOnBundleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkLicenseAsOnBundleActionPerformed
        if (bundle==null || bundle.getItemsCount()==0) return;
        int sel = list_items.getSelectedIndex();
        if (sel<0) return;
        
        boolean licenseAsOnBundle = checkLicenseAsOnBundle.isSelected();
        Item item = bundle.getItem(sel);
        item.getLicense_basis().as_on_bundle(licenseAsOnBundle);
        if (licenseAsOnBundle) {
            panelLicense1.setVisible(false);
        } else {
            panelLicense1.setVisible(true);
        }
        notifyChanges();
    }//GEN-LAST:event_checkLicenseAsOnBundleActionPerformed

    private void checkLicenseAsOnBundleStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_checkLicenseAsOnBundleStateChanged
        //boolean enabled = !checkLicenseAsOnBundle.isSelected();
       
    }//GEN-LAST:event_checkLicenseAsOnBundleStateChanged

    private void text_display_artistActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_text_display_artistActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_text_display_artistActionPerformed

    private void bu_add_allowed_territoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_add_allowed_territoryActionPerformed
        if (!add_new_territory_text.getText().equals("")) {
            DefaultListModel lm = (DefaultListModel)list_allowed_territories.getModel();
            String t = add_new_territory_text.getText();

          //  bundle.getLicense_basis().getTerritorial().allow(t);
            lm.addElement(t);
            add_new_territory_text.setText("");
            notifyChanges();
        }
}//GEN-LAST:event_bu_add_allowed_territoryActionPerformed

    private void bu_remove_allowed_territoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_remove_allowed_territoryActionPerformed
        int sel = list_allowed_territories.getSelectedIndex();
        if (sel>=0) {
            DefaultListModel lm = (DefaultListModel)list_allowed_territories.getModel();
            String s = (String)lm.getElementAt(sel);
           // bundle.getLicense_basis().getTerritorial().remove(s);
            lm.remove(sel);
            notifyChanges();
        }
}//GEN-LAST:event_bu_remove_allowed_territoryActionPerformed

    private void bu_remove_disallowed_territoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_remove_disallowed_territoryActionPerformed
        int sel = list_disallowed_territories.getSelectedIndex();
        if (sel>=0) {
            DefaultListModel lm = (DefaultListModel)list_disallowed_territories.getModel();
            String s = (String)lm.getElementAt(sel);
           // bundle.getLicense_basis().getTerritorial().remove(s);
            lm.remove(sel);
            notifyChanges();
        }
}//GEN-LAST:event_bu_remove_disallowed_territoryActionPerformed

    private void bu_add_disallowed_territoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_add_disallowed_territoryActionPerformed
        if (!add_new_territory_text.getText().equals("")) {
            DefaultListModel lm = (DefaultListModel)list_disallowed_territories.getModel();
            String t = add_new_territory_text.getText();
          //  bundle.getLicense_basis().getTerritorial().disallow(t);
            lm.addElement(t);
            add_new_territory_text.setText("");
            notifyChanges();
        }
}//GEN-LAST:event_bu_add_disallowed_territoryActionPerformed

    private void select_license_pricingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_select_license_pricingActionPerformed
        int sel = select_license_pricing.getSelectedIndex();
        if (sel == 0) { //other
         //   bundle.getLicense_basis().pricing_pricecode(null);
         //   bundle.getLicense_basis().pricing_wholesale(text_license_pricing.getText());
            text_license_pricing.setEnabled(true);
        } else {
         //   bundle.getLicense_basis().pricing_pricecode((String)select_license_pricing.getSelectedItem());
        //    bundle.getLicense_basis().pricing_wholesale(null);
            text_license_pricing.setText("");
            text_license_pricing.setEnabled(false);
        }
        notifyChanges();
}//GEN-LAST:event_select_license_pricingActionPerformed

    private void bu_remove_promotextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_remove_promotextActionPerformed
        if (bundle==null || bundle.getItemsCount()==0) return;
        int selItem = list_items.getSelectedIndex();
        if (selItem<0) return;
        Item item = bundle.getItem(selItem);
        int sel = table_promotext.getSelectedRow();
        if (sel>=0) {
            String lang = (String)table_promotext.getValueAt(sel, 0);
            item.getInformation().removePromotext(lang);
            item.getInformation().removeTeasertext(lang);
            table_promotext.setModel(new PromotextTableModel(bundle.getInformation(),this));
            notifyChanges();
        }
    }//GEN-LAST:event_bu_remove_promotextActionPerformed

    private void text_displaynameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_text_displaynameActionPerformed

    }//GEN-LAST:event_text_displaynameActionPerformed

    private void bu_remove_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_remove_itemActionPerformed
        if (bundle==null || bundle.getItemsCount()==0) return;
        int selItem = list_items.getSelectedIndex();
        if (selItem<0) return;
        bundle.removeItem(selItem);
        list_items.remove(selItem);
        int s = list_items.getModel().getSize();
        if (s>0) {
            if (s>selItem) {
                list_items.setSelectedIndex(selItem);
            } else {
                list_items.setSelectedIndex(s-1);
            }
        }
    }//GEN-LAST:event_bu_remove_itemActionPerformed

    private void bu_remove_contributorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_remove_contributorActionPerformed
        if (bundle==null || bundle.getItemsCount()==0) return;
        int selItem = list_items.getSelectedIndex();
        if (selItem<0) return;
        Item item = bundle.getItem(selItem);

        int sel = list_contributors.getSelectedIndex();
        if (sel<0) return;
        item.removeContributor(sel);
        list_contributors.remove(sel);
        notifyChanges();
    }//GEN-LAST:event_bu_remove_contributorActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField add_new_territory_text;
    private javax.swing.JButton bu_add_allowed_territory;
    private javax.swing.JButton bu_add_contributor;
    private javax.swing.JButton bu_add_disallowed_territory;
    private javax.swing.JButton bu_add_item;
    private javax.swing.JButton bu_add_promotext;
    private javax.swing.JButton bu_remove_allowed_territory;
    private javax.swing.JButton bu_remove_contributor;
    private javax.swing.JButton bu_remove_disallowed_territory;
    private javax.swing.JButton bu_remove_item;
    private javax.swing.JButton bu_remove_promotext;
    private javax.swing.JCheckBox checkLicenseAsOnBundle;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JList list_all_contributors;
    private javax.swing.JList list_allowed_territories;
    private javax.swing.JList list_contributors;
    private javax.swing.JList list_disallowed_territories;
    private javax.swing.JList list_items;
    private javax.swing.JPanel panelBasics;
    private javax.swing.JPanel panelContributors;
    private javax.swing.JPanel panelIDs;
    private javax.swing.JPanel panelIDsBig;
    private javax.swing.JPanel panelInformation;
    private javax.swing.JPanel panelLicense;
    private javax.swing.JPanel panelLicense1;
    private javax.swing.JComboBox select_license_pricing;
    private javax.swing.JComboBox select_type;
    private javax.swing.JTable table_promotext;
    private javax.swing.JTextField text_amazon;
    private javax.swing.JTextField text_contentauthid;
    private javax.swing.JTextField text_digital_release_datetime;
    private javax.swing.JTextField text_display_artist;
    private javax.swing.JTextField text_displayname;
    private javax.swing.JTextField text_finetunesid;
    private javax.swing.JTextField text_grid;
    private javax.swing.JTextField text_isbn;
    private javax.swing.JTextField text_isrc;
    private javax.swing.JTextField text_labelordernum;
    private javax.swing.JTextField text_license_from_datetime;
    private javax.swing.JTextField text_license_pricing;
    private javax.swing.JTextField text_license_to_datetime;
    private javax.swing.JTextField text_name;
    private javax.swing.JTextField text_ourid;
    private javax.swing.JTextField text_physical_realease_datetime;
    private javax.swing.JTextField text_playlength;
    private javax.swing.JTextField text_upc;
    private javax.swing.JTextField text_version;
    private javax.swing.JTextField text_yourid;
    // End of variables declaration//GEN-END:variables


}
