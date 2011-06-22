package org.fnppl.opensdx.gui;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Vector;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.swing.text.Document;
import org.fnppl.opensdx.common.Bundle;
import org.fnppl.opensdx.common.BundleInformation;
import org.fnppl.opensdx.common.Contributor;
import org.fnppl.opensdx.common.Feed;
import org.fnppl.opensdx.common.IDs;
import org.fnppl.opensdx.common.InfoWWW;
import org.fnppl.opensdx.common.LicenseBasis;
import org.fnppl.opensdx.common.Territorial;
import org.fnppl.opensdx.common.Territory;
import org.fnppl.opensdx.security.SecurityHelper;


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
public class PanelBundle extends javax.swing.JPanel {

    private Bundle bundle = null;
    private MyDocListener changeListener;
    private Vector<String[]> bindings;
    private PanelFeedInfo me;
    private Vector<MyObserver> observers = new Vector<MyObserver>();

    public void addObserver(MyObserver observer) {
        observers.add(observer);
    }

    /** Creates new form PanelBundle */
    public PanelBundle() {
        initComponents();
        list_allowed_territories.setModel(new DefaultListModel());
        list_disallowed_territories.setModel(new DefaultListModel());
        initChangeListeners();
    }

    public static void main(String[] args) {
        Field[] fields = PanelBundle.class.getDeclaredFields();
        
        //newEmpty
        System.out.println("public void newEmpty() {");
        for (Field f : fields) {
            f.setAccessible(true);
            if (f.getName().startsWith("text_")) {
                System.out.println(f.getName() + ".setText(\"\");");
            } else if (f.getName().startsWith("check_")) {
                System.out.println(f.getName() + ".setSelected(true);");
            } else if (f.getName().startsWith("select_")) {
                System.out.println(f.getName() + ".setSelectedIndex(0);");
            }
        }
        System.out.println("}");

        System.out.println("public update(Bundle bundle) {");
        System.out.println("this.bundle = bundle;");
        System.out.println("");

        for (Field f : fields) {
            f.setAccessible(true);
            if (f.getName().startsWith("text_")) {
                System.out.println(f.getName() + ".setText();");
            } else if (f.getName().startsWith("check_")) {
                System.out.println(f.getName() + ".setSelected();");
            } else if (f.getName().startsWith("select_")) {
                System.out.println(f.getName() + ".setSelectedItem();");
            }
        }
        System.out.println("}");
        
        System.out.println("Hallo");
        
        for (Field f : fields) {
            f.setAccessible(true);
            if (f.getName().startsWith("text_")) {
                System.out.println("texts.add("+f.getName()+ ");");
            }
        }

        for (Field f : fields) {
            f.setAccessible(true);
            if (f.getName().startsWith("text_")) {
                System.out.println(f.getName()+ ".addKeyListener(keyAdapt);");
            }
        }

        for (Field f : fields) {
            f.setAccessible(true);
            if (f.getName().startsWith("text_")) {
                System.out.println("else if(text == "+f.getName()+ ") c.getIDs().contentauthid(t);");
            }
        }
   
    }

    public void newEmpty() {
        check_contributor_publish_phone.setSelected(false);
        select_contributor_type.setSelectedIndex(0);
        text_amazon.setText("");
        text_contentauthid.setText("");
        text_contributor_contentauthid.setText("");
        text_contributor_facebook.setText("");
        text_contributor_finetunesid.setText("");
        text_contributor_gvl.setText("");
        text_contributor_homepage.setText("");
        text_contributor_myspace.setText("");
        text_contributor_name.setText("");
        text_contributor_ourid.setText("");
        text_contributor_phone.setText("");
        text_contributor_twitter.setText("");
        text_contributor_yourid.setText("");
        text_digital_release_date.setText("");
        text_display_artist.setText("");
        text_displayname.setText("");
        text_finetunesid.setText("");
        text_grid.setText("");
        text_isbn.setText("");
        text_isrc.setText("");
        text_labelordernum.setText("");
        text_license_from_datetime.setText("");
        text_license_to_datetime.setText("");
        text_name.setText("");
        text_ourid.setText("");
        text_physical_release_datetime.setText("");
        text_upc.setText("");
        text_version.setText("");
        text_yourid.setText("");
        text_license_pricing.setText("");
    }

    public void update(Bundle newBundle) {
        bundle = newBundle;

        //basics
        text_display_artist.setText(bundle.getDisplay_artist());
        text_displayname.setText(bundle.getDisplayname());
        text_name.setText(bundle.getName());
        text_version.setText(bundle.getVersion());

        updateContributorList();

        Contributor c = bundle.getContributor(0);
        updateContributor(c);

        IDs ids = bundle.getIds();
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
        BundleInformation info = bundle.getInformation();
        if (info != null) {
            text_physical_release_datetime.setText(info.getPhysicalReleaseDatetimeText());
            text_digital_release_date.setText(info.getDigitalReleaseDatetimeText());
            table_promotext.setModel(new PromotextTableModel(info));
        }

        //License
        LicenseBasis lb = bundle.getLicense_basis();
        if (lb!=null) {
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
            Territorial t = lb.getTerritorial();
            int count = t.getTerritorialCount();
            DefaultListModel lmAllow = (DefaultListModel)list_allowed_territories.getModel();
            DefaultListModel lmDisallow = (DefaultListModel)list_disallowed_territories.getModel();
            lmAllow.removeAllElements();
            lmDisallow.removeAllElements();
            for (int i=0;i<count;i++) {
                if (t.isTerritoryAllowed(i)) {
                    lmAllow.addElement(t.getTerritory(i));
                } else {
                    lmDisallow.addElement(t.getTerritory(i));
                }
            }
        } else {
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
        
        changeListener.saveStates();
    }

    private void notifyChanges() {
        for (MyObserver ob : observers) {
            ob.notifyChange();
        }
    }

    private void updateContributor(Contributor c) {
        if (c != null) {
            select_contributor_type.setSelectedItem(c.getType());
            text_contributor_name.setText(c.getName());

            IDs cid = c.getIDs();
            if (cid != null) {
                text_contributor_contentauthid.setText(cid.getContentauthid());
                text_contributor_finetunesid.setText(cid.getFinetunesid());
                text_contributor_gvl.setText(cid.getGvl());
                text_contributor_ourid.setText(cid.getOurid());
                text_contributor_yourid.setText(cid.getYourid());
            } else {
                text_contributor_contentauthid.setText("");
                text_contributor_finetunesid.setText("");
                text_contributor_gvl.setText("");
                text_contributor_ourid.setText("");
                text_contributor_yourid.setText("");
            }
            InfoWWW www = c.getWww();
            if (www != null) {
                text_contributor_facebook.setText(www.getFacebook());
                text_contributor_homepage.setText(www.getHomepage());
                text_contributor_myspace.setText(www.getMyspace());
                text_contributor_phone.setText(www.getPhone());
                text_contributor_twitter.setText(www.getTwitter());
                check_contributor_publish_phone.setSelected(www.isPhonePublishable());
            } else {
                 text_contributor_facebook.setText("");
                text_contributor_homepage.setText("");
                text_contributor_myspace.setText("");
                text_contributor_phone.setText("");
                text_contributor_twitter.setText("");
                check_contributor_publish_phone.setSelected(false);
            }
        } else {
            select_contributor_type.setSelectedItem(0);
            text_contributor_name.setText("");
            text_contributor_contentauthid.setText("");
            text_contributor_finetunesid.setText("");
            text_contributor_gvl.setText("");
            text_contributor_ourid.setText("");
            text_contributor_yourid.setText("");
            text_contributor_facebook.setText("");
            text_contributor_homepage.setText("");
            text_contributor_myspace.setText("");
            text_contributor_phone.setText("");
            text_contributor_twitter.setText("");
            check_contributor_publish_phone.setSelected(false);
        }
        changeListener.saveState(text_contributor_name);
        changeListener.saveState(text_contributor_contentauthid);
        changeListener.saveState(text_contributor_finetunesid);
        changeListener.saveState(text_contributor_gvl);
        changeListener.saveState(text_contributor_homepage);
        changeListener.saveState(text_contributor_myspace);
        changeListener.saveState(text_contributor_ourid);
        changeListener.saveState(text_contributor_phone);
        changeListener.saveState(text_contributor_twitter);
        changeListener.saveState(text_contributor_yourid);
        changeListener.saveState(text_contributor_facebook);

    }

    private void updateContributorList() {
        int anzContributors = bundle.getContributorCount();
        DefaultListModel lm = new DefaultListModel();
        for (int i = 0; i < anzContributors; i++) {
            lm.addElement(bundle.getContributor(i).getName() + " (" + bundle.getContributor(i).getType() + ")");
        }
        list_contributors.setModel(lm);
        list_contributors.setSelectedIndex(0);
        list_contributors.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list_contributors.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                int sel = e.getFirstIndex();
                if (sel >= 0 && sel < bundle.getContributorCount()) {
                    Contributor c = bundle.getContributor(sel);
                    updateContributor(c);
                }
            }
        });
    }

    private class PromotextTableModel implements TableModel {

        private BundleInformation info;
        private Vector<String[]> rows = new Vector<String[]>();
        private Vector<int[]> index = new Vector<int[]>();

        private void updateRows() {
            rows = new Vector<String[]>();
            int countPromotext = info.getPromotextCount();
            for (int i = 0; i < countPromotext; i++) {
                rows.add(new String[]{info.getPromotextLanguage(i), info.getPromotext(i), ""});
                index.add(new int[] {i,-1});
            }
            int countTeasertext = info.getTeasertextCount();
            for (int i = 0; i < countTeasertext; i++) {
                boolean add = true;
                String lang = info.getTeasertextLanguage(i);
                for (int j=0;j<rows.size();j++) {
                    String[] row = rows.get(j);
                    if (row[0].equals(lang)) {
                        add = false;
                        row[2] = info.getTeasertext(i);
                        index.get(j)[1] = i;
                    }
                }
                if (add) {
                    rows.add(new String[]{lang, "", info.getTeasertext(i)});
                    index.add(new int[] {-1,i});
                }
            }
        }

        public PromotextTableModel(BundleInformation info) {
            this.info = info;
            updateRows();
        }

        public int getRowCount() {
            return rows.size();
        }

        public int getColumnCount() {
            return 3;
        }

        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) {
                return "language";
            }
            if (columnIndex == 1) {
                return "promotext";
            }
            return "teasertext";
        }

        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            return rows.get(rowIndex)[columnIndex];
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            rows.get(rowIndex)[columnIndex] = (String) aValue;
            if (columnIndex==1) {
                int row = index.get(rowIndex)[0];
                if (row==-1) {
                    info.addPromotext((String)table_promotext.getValueAt(rowIndex, 0), (String)aValue);
                } else {
                    info.promotext(row, (String)aValue);
                }
            }
            else if(columnIndex == 2) {
                 int row = index.get(rowIndex)[1];
                if (row==-1) {
                    info.addTeasertext((String)table_promotext.getValueAt(rowIndex, 0), (String)aValue);
                } else {
                   info.teasertext(row, (String)aValue);
                }
            }
            else if(columnIndex == 0) {
                info.promotext_language(index.get(rowIndex)[0], (String)aValue);
                info.teasertext_language(index.get(rowIndex)[1], (String)aValue);
            }
            org.fnppl.opensdx.xml.Document.buildDocument(info.toElement()).output(System.out);
            notifyChanges();
        }

        public void addTableModelListener(TableModelListener l) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }

        public void removeTableModelListener(TableModelListener l) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private void initChangeListeners() {
        Vector<JTextField> texts = new Vector<JTextField>();
        texts.add(text_amazon);
        texts.add(text_contentauthid);
        texts.add(text_contributor_contentauthid);
        texts.add(text_contributor_facebook);
        texts.add(text_contributor_finetunesid);
        texts.add(text_contributor_gvl);
        texts.add(text_contributor_homepage);
        texts.add(text_contributor_myspace);
        texts.add(text_contributor_name);
        texts.add(text_contributor_ourid);
        texts.add(text_contributor_phone);
        texts.add(text_contributor_twitter);
        texts.add(text_contributor_yourid);
        texts.add(text_digital_release_date);
        texts.add(text_display_artist);
        texts.add(text_displayname);
        texts.add(text_finetunesid);
        texts.add(text_grid);
        texts.add(text_isbn);
        texts.add(text_isrc);
        texts.add(text_labelordernum);
        texts.add(text_license_from_datetime);
        texts.add(text_license_to_datetime);
        texts.add(text_name);
        texts.add(text_ourid);
        texts.add(text_physical_release_datetime);
        texts.add(text_upc);
        texts.add(text_version);
        texts.add(text_yourid);
        texts.add(text_license_pricing);
        changeListener = new MyDocListener(texts);
        for (JTextField text : texts) {
            text.getDocument().addDocumentListener(changeListener);
        }

        KeyAdapter keyAdapt = new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER){
                    if (e.getComponent() instanceof JTextField) {
                        JTextField text = (JTextField)e.getComponent();
                        String t = text.getText();
                        if (t.equals("")) t = null;
                        int selCon = list_contributors.getSelectedIndex();
                        Contributor c = null;
                        if (selCon>=0 && selCon < bundle.getContributorCount()) {
                            c = bundle.getContributor(selCon);
                        }
                        IDs ids = bundle.getIds();
                        BundleInformation info = bundle.getInformation();
                        try {
                            if (text == text_amazon) bundle.getIds().amzn(t);
                            else if(text == text_contentauthid) ids.contentauthid(t);
                            else if(text == text_contributor_contentauthid) c.getIDs().contentauthid(t);
                            else if(text == text_contributor_facebook) c.getWww().facebook(t);
                            else if(text == text_contributor_finetunesid) c.getIDs().finetunesid(t);
                            else if(text == text_contributor_gvl) c.getIDs().gvl(t);
                            else if(text == text_contributor_homepage) c.getWww().homepage(t);
                            else if(text == text_contributor_myspace) c.getWww().myspace(t);
                            else if(text == text_contributor_name) {
                                c.name(t);
                                DefaultListModel lm = (DefaultListModel)list_contributors.getModel();
                                lm.set(selCon, c.getName() + " (" + c.getType() + ")");
                            }
                            else if(text == text_contributor_ourid) c.getIDs().ourid(t);
                            else if(text == text_contributor_phone) c.getWww().phone(t);
                            else if(text == text_contributor_twitter) c.getWww().twitter(t);
                            else if(text == text_contributor_yourid) c.getIDs().yourid(t);
                            else if(text == text_digital_release_date) info.physical_release_datetime(SecurityHelper.parseDate(t));
                            else if(text == text_display_artist) bundle.display_artist(t);
                            else if(text == text_displayname) bundle.displayname(t);
                            else if(text == text_finetunesid) ids.finetunesid(t);
                            else if(text == text_grid) ids.grid(t);
                            else if(text == text_isbn) ids.isbn(t);
                            else if(text == text_isrc) ids.isrc(t);
                            else if(text == text_labelordernum) ids.labelordernum(t);
                            else if(text == text_license_from_datetime) bundle.getLicense_basis().timeframe_from_datetime(SecurityHelper.parseDate(t));
                            else if(text == text_license_to_datetime) bundle.getLicense_basis().timeframe_to_datetime(SecurityHelper.parseDate(t));
                            else if(text == text_name) bundle.name(t);
                            else if(text == text_ourid) ids.ourid(t);
                            else if(text == text_physical_release_datetime) info.physical_release_datetime(SecurityHelper.parseDate(t));
                            else if(text == text_upc) ids.upc(t);
                            else if(text == text_version) bundle.version(t);
                            else if(text == text_yourid) ids.yourid(t);
                            else if(text == text_license_pricing) {
                                bundle.getLicense_basis().pricing_pricecode(null);
                                bundle.getLicense_basis().pricing_wholesale(t);
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
        text_amazon.addKeyListener(keyAdapt);
        text_contentauthid.addKeyListener(keyAdapt);
        text_contributor_contentauthid.addKeyListener(keyAdapt);
        text_contributor_facebook.addKeyListener(keyAdapt);
        text_contributor_finetunesid.addKeyListener(keyAdapt);
        text_contributor_gvl.addKeyListener(keyAdapt);
        text_contributor_homepage.addKeyListener(keyAdapt);
        text_contributor_myspace.addKeyListener(keyAdapt);
        text_contributor_name.addKeyListener(keyAdapt);
        text_contributor_ourid.addKeyListener(keyAdapt);
        text_contributor_phone.addKeyListener(keyAdapt);
        text_contributor_twitter.addKeyListener(keyAdapt);
        text_contributor_yourid.addKeyListener(keyAdapt);
        text_digital_release_date.addKeyListener(keyAdapt);
        text_display_artist.addKeyListener(keyAdapt);
        text_displayname.addKeyListener(keyAdapt);
        text_finetunesid.addKeyListener(keyAdapt);
        text_grid.addKeyListener(keyAdapt);
        text_isbn.addKeyListener(keyAdapt);
        text_isrc.addKeyListener(keyAdapt);
        text_labelordernum.addKeyListener(keyAdapt);
        text_license_from_datetime.addKeyListener(keyAdapt);
        text_license_to_datetime.addKeyListener(keyAdapt);
        text_name.addKeyListener(keyAdapt);
        text_ourid.addKeyListener(keyAdapt);
        text_physical_release_datetime.addKeyListener(keyAdapt);
        text_upc.addKeyListener(keyAdapt);
        text_version.addKeyListener(keyAdapt);
        text_yourid.addKeyListener(keyAdapt);
        text_license_pricing.addKeyListener(keyAdapt);

        /*


        ChangeListener chListen = new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
        updateStatusColors();
        }
        };
        ActionListener actionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        updateStatusColors();
        }
        };
        check_onlytest.addChangeListener(chListen);
        select_receiver_type.addActionListener(actionListener);
        select_authtype.addActionListener(actionListener);
         * */

    }

    private class MyDocListener implements DocumentListener {

        private Vector<JTextField> texts;
        private Vector<String> saves;

        public MyDocListener(Vector<JTextField> texts) {
            this.texts = texts;

        }

        public void saveStates() {
            saves = new Vector<String>();
            for (JTextField text : texts) {
                saves.add(text.getText());
                text.setBackground(Color.WHITE);
            }
        }

        public void saveState(JTextField t) {
            int ind = texts.indexOf(t);
            if (ind<0 || saves == null) return;
            saves.set(ind, t.getText());
            t.setBackground(Color.WHITE);
        }

        public String getSavedText(JTextField t) {
            int ind = texts.indexOf(t);
            if (ind<0 || saves == null) return "";
            return saves.get(ind);
        }

        public void removeUpdate(DocumentEvent e) {
            action(e);
        }

        public void insertUpdate(DocumentEvent e) {
            action(e);
        }

        public void changedUpdate(DocumentEvent e) {
            action(e);
        }

        private void action(DocumentEvent e) {
            if (saves == null) {
                for (JTextField text : texts) {
                    text.setBackground(Color.WHITE);
                }
            } else {
                for (int i = 0; i < texts.size(); i++) {
                    if (texts.get(i).getText().equals(saves.get(i))) {
                        texts.get(i).setBackground(Color.WHITE);
                    } else {
                        texts.get(i).setBackground(Color.YELLOW);
                    }
                }
            }
        }
    }

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
        text_display_artist = new javax.swing.JTextField();
        jTabbedPane1 = new javax.swing.JTabbedPane();
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
        jScrollPane1 = new javax.swing.JScrollPane();
        list_contributors = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        text_contributor_name = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        select_contributor_type = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        text_contributor_gvl = new javax.swing.JTextField();
        text_contributor_finetunesid = new javax.swing.JTextField();
        text_contributor_ourid = new javax.swing.JTextField();
        text_contributor_yourid = new javax.swing.JTextField();
        text_contributor_contentauthid = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        text_contributor_facebook = new javax.swing.JTextField();
        text_contributor_myspace = new javax.swing.JTextField();
        text_contributor_homepage = new javax.swing.JTextField();
        text_contributor_twitter = new javax.swing.JTextField();
        text_contributor_phone = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        check_contributor_publish_phone = new javax.swing.JCheckBox();
        jLabel29 = new javax.swing.JLabel();
        bu_contributor_add = new javax.swing.JButton();
        bu_contributor_remove = new javax.swing.JButton();
        panelInformation = new javax.swing.JPanel();
        text_physical_release_datetime = new javax.swing.JTextField();
        jLabel30 = new javax.swing.JLabel();
        text_digital_release_date = new javax.swing.JTextField();
        jLabel31 = new javax.swing.JLabel();
        scroll_table_promotext = new javax.swing.JScrollPane();
        table_promotext = new javax.swing.JTable();
        bu_add_promotext = new javax.swing.JButton();
        bu_remove_promotext = new javax.swing.JButton();
        panelLicense = new javax.swing.JPanel();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        text_license_from_datetime = new javax.swing.JTextField();
        text_license_to_datetime = new javax.swing.JTextField();
        text_license_pricing = new javax.swing.JTextField();
        jLabel34 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        list_disallowed_territories = new javax.swing.JList();
        jScrollPane4 = new javax.swing.JScrollPane();
        list_allowed_territories = new javax.swing.JList();
        bu_add_allowed_territory = new javax.swing.JButton();
        bu_remove_allowed_territory = new javax.swing.JButton();
        bu_remove_disallowed_territory = new javax.swing.JButton();
        bu_add_disallowed_territory = new javax.swing.JButton();
        add_new_territory_text = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        select_license_pricing = new javax.swing.JComboBox();

        setPreferredSize(new java.awt.Dimension(760, 989));

        panelBasics.setBorder(javax.swing.BorderFactory.createTitledBorder("Basics"));

        jLabel4.setText("name");

        jLabel5.setText("displayname");

        jLabel6.setText("version");

        jLabel7.setText("display artist");

        javax.swing.GroupLayout panelBasicsLayout = new javax.swing.GroupLayout(panelBasics);
        panelBasics.setLayout(panelBasicsLayout);
        panelBasicsLayout.setHorizontalGroup(
            panelBasicsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBasicsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelBasicsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel4))
                .addGap(18, 18, 18)
                .addGroup(panelBasicsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(text_name, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(text_displayname, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(37, 37, 37)
                .addGroup(panelBasicsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(panelBasicsLayout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(text_display_artist, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelBasicsLayout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 48, Short.MAX_VALUE)
                        .addComponent(text_version, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelBasicsLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {text_display_artist, text_displayname, text_name, text_version});

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
                        .addComponent(text_name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(text_display_artist, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel7))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                    .addComponent(jLabel11)
                    .addComponent(jLabel12)
                    .addComponent(jLabel13)
                    .addComponent(jLabel14)
                    .addComponent(jLabel15)
                    .addComponent(jLabel16)
                    .addComponent(jLabel17)
                    .addComponent(jLabel8)
                    .addComponent(jLabel10)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelIDsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(text_isrc)
                    .addComponent(text_amazon)
                    .addComponent(text_isbn)
                    .addComponent(text_upc)
                    .addComponent(text_finetunesid)
                    .addComponent(text_ourid)
                    .addComponent(text_yourid, javax.swing.GroupLayout.DEFAULT_SIZE, 517, Short.MAX_VALUE)
                    .addComponent(text_grid)
                    .addComponent(text_labelordernum)
                    .addComponent(text_contentauthid))
                .addContainerGap())
        );

        panelIDsLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {text_amazon, text_contentauthid, text_finetunesid, text_grid, text_isbn, text_isrc, text_labelordernum, text_ourid, text_upc, text_yourid});

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
                .addContainerGap(275, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("IDs", panelIDs);

        panelContributors.setBorder(javax.swing.BorderFactory.createTitledBorder("Contributors"));

        list_contributors.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Contributor 1", "Contributor 2", "Contributor 3", "Contributor 4", "Contributor 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        list_contributors.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                list_contributorsValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(list_contributors);

        jLabel1.setText("name");

        text_contributor_name.setText("jTextField1");

        jLabel2.setText("type");

        select_contributor_type.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "label", "composer", "texter", "writer", "conductor" }));
        select_contributor_type.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                select_contributor_typeActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Ubuntu", 1, 15));
        jLabel3.setText("List of contributors");

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("IDs"));

        text_contributor_gvl.setText("jTextField21");

        text_contributor_finetunesid.setText("jTextField22");

        text_contributor_ourid.setText("jTextField23");

        text_contributor_yourid.setText("jTextField24");

        text_contributor_contentauthid.setText("jTextField25");

        jLabel24.setText("gvl");

        jLabel25.setText("finetunes id");

        jLabel26.setText("our id");

        jLabel27.setText("your id");

        jLabel28.setText("content auth id");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel25)
                    .addComponent(jLabel24)
                    .addComponent(jLabel26)
                    .addComponent(jLabel27)
                    .addComponent(jLabel28))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(text_contributor_contentauthid, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)
                    .addComponent(text_contributor_yourid, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)
                    .addComponent(text_contributor_ourid, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)
                    .addComponent(text_contributor_finetunesid, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)
                    .addComponent(text_contributor_gvl, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(text_contributor_gvl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25)
                    .addComponent(text_contributor_finetunesid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel26)
                    .addComponent(text_contributor_ourid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel27)
                    .addComponent(text_contributor_yourid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel28)
                    .addComponent(text_contributor_contentauthid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("www"));

        text_contributor_facebook.setText("jTextField16");

        text_contributor_myspace.setText("jTextField17");

        text_contributor_homepage.setText("jTextField18");

        text_contributor_twitter.setText("jTextField19");

        text_contributor_phone.setText("jTextField20");

        jLabel18.setText("facebook");

        jLabel19.setText("myspace");

        jLabel20.setText("homepage");

        jLabel21.setText("twitter");

        jLabel22.setText("phone");

        check_contributor_publish_phone.setText("phone publishable");
        check_contributor_publish_phone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                check_contributor_publish_phoneActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel18)
                    .addComponent(jLabel19)
                    .addComponent(jLabel20)
                    .addComponent(jLabel21)
                    .addComponent(jLabel22))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(check_contributor_publish_phone)
                    .addComponent(text_contributor_facebook, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                    .addComponent(text_contributor_myspace, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                    .addComponent(text_contributor_homepage, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                    .addComponent(text_contributor_twitter, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                    .addComponent(text_contributor_phone, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(text_contributor_facebook, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(text_contributor_myspace, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(text_contributor_homepage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(text_contributor_twitter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(text_contributor_phone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(check_contributor_publish_phone)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel29.setFont(new java.awt.Font("Ubuntu", 1, 15));
        jLabel29.setText("Contributor details");

        bu_contributor_add.setText("add");
        bu_contributor_add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bu_contributor_addActionPerformed(evt);
            }
        });

        bu_contributor_remove.setText("remove");
        bu_contributor_remove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bu_contributor_removeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelContributorsLayout = new javax.swing.GroupLayout(panelContributors);
        panelContributors.setLayout(panelContributorsLayout);
        panelContributorsLayout.setHorizontalGroup(
            panelContributorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelContributorsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelContributorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addGroup(panelContributorsLayout.createSequentialGroup()
                        .addComponent(bu_contributor_add, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(bu_contributor_remove))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(66, 66, 66)
                .addGroup(panelContributorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel29)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelContributorsLayout.createSequentialGroup()
                        .addGroup(panelContributorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addGap(18, 18, 18)
                        .addGroup(panelContributorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(select_contributor_type, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(text_contributor_name, javax.swing.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE))))
                .addGap(376, 376, 376))
        );

        panelContributorsLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {bu_contributor_add, bu_contributor_remove});

        panelContributorsLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jPanel3, jPanel4});

        panelContributorsLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {select_contributor_type, text_contributor_name});

        panelContributorsLayout.setVerticalGroup(
            panelContributorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelContributorsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelContributorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelContributorsLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(panelContributorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(bu_contributor_add)
                            .addComponent(bu_contributor_remove)))
                    .addGroup(panelContributorsLayout.createSequentialGroup()
                        .addComponent(jLabel29)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(panelContributorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(text_contributor_name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(panelContributorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(select_contributor_type, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addGap(18, 18, 18)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Contributors", panelContributors);

        panelInformation.setBorder(javax.swing.BorderFactory.createTitledBorder("Information"));

        text_physical_release_datetime.setText("jTextField26");

        jLabel30.setText("physical release date");

        text_digital_release_date.setText("jTextField27");

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
        scroll_table_promotext.setViewportView(table_promotext);
        table_promotext.getColumnModel().getColumn(0).setResizable(false);
        table_promotext.getColumnModel().getColumn(1).setResizable(false);
        table_promotext.getColumnModel().getColumn(2).setResizable(false);

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

        javax.swing.GroupLayout panelInformationLayout = new javax.swing.GroupLayout(panelInformation);
        panelInformation.setLayout(panelInformationLayout);
        panelInformationLayout.setHorizontalGroup(
            panelInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelInformationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scroll_table_promotext, javax.swing.GroupLayout.DEFAULT_SIZE, 639, Short.MAX_VALUE)
                    .addGroup(panelInformationLayout.createSequentialGroup()
                        .addGroup(panelInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel30)
                            .addComponent(jLabel31))
                        .addGap(18, 18, 18)
                        .addGroup(panelInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(text_physical_release_datetime)
                            .addComponent(text_digital_release_date, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)))
                    .addGroup(panelInformationLayout.createSequentialGroup()
                        .addComponent(bu_add_promotext, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(bu_remove_promotext)))
                .addContainerGap())
        );
        panelInformationLayout.setVerticalGroup(
            panelInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelInformationLayout.createSequentialGroup()
                .addGroup(panelInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel30)
                    .addComponent(text_physical_release_datetime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(text_digital_release_date, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel31))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scroll_table_promotext, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bu_add_promotext)
                    .addComponent(bu_remove_promotext))
                .addContainerGap(334, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Information", panelInformation);

        panelLicense.setBorder(javax.swing.BorderFactory.createTitledBorder("License basis"));

        jLabel32.setText("timeframe from");

        jLabel33.setText("timeframe to");

        jLabel34.setText("pricing");

        list_disallowed_territories.setBorder(javax.swing.BorderFactory.createTitledBorder("Disallowed Territories"));
        jScrollPane3.setViewportView(list_disallowed_territories);

        list_allowed_territories.setBorder(javax.swing.BorderFactory.createTitledBorder("Allowed Territories"));
        jScrollPane4.setViewportView(list_allowed_territories);

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

        javax.swing.GroupLayout panelLicenseLayout = new javax.swing.GroupLayout(panelLicense);
        panelLicense.setLayout(panelLicenseLayout);
        panelLicenseLayout.setHorizontalGroup(
            panelLicenseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLicenseLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelLicenseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelLicenseLayout.createSequentialGroup()
                        .addComponent(jLabel23)
                        .addGap(44, 44, 44)
                        .addGroup(panelLicenseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelLicenseLayout.createSequentialGroup()
                                .addComponent(bu_add_allowed_territory)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 161, Short.MAX_VALUE))
                            .addComponent(add_new_territory_text, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)))
                    .addGroup(panelLicenseLayout.createSequentialGroup()
                        .addGroup(panelLicenseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(bu_remove_allowed_territory))
                        .addGap(26, 26, 26)
                        .addGroup(panelLicenseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(bu_remove_disallowed_territory)
                            .addGroup(panelLicenseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(bu_add_disallowed_territory)
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(panelLicenseLayout.createSequentialGroup()
                        .addGroup(panelLicenseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel33)
                            .addComponent(jLabel34)
                            .addComponent(jLabel32))
                        .addGap(39, 39, 39)
                        .addGroup(panelLicenseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(panelLicenseLayout.createSequentialGroup()
                                .addComponent(select_license_pricing, 0, 98, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addComponent(text_license_pricing, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(text_license_to_datetime, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE)
                            .addComponent(text_license_from_datetime, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE))))
                .addGap(251, 251, 251))
        );

        panelLicenseLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jScrollPane3, jScrollPane4});

        panelLicenseLayout.setVerticalGroup(
            panelLicenseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLicenseLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelLicenseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(text_license_from_datetime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(panelLicenseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel33)
                    .addComponent(text_license_to_datetime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(panelLicenseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel34)
                    .addComponent(text_license_pricing, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(select_license_pricing, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addGroup(panelLicenseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelLicenseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bu_remove_allowed_territory)
                    .addComponent(bu_remove_disallowed_territory))
                .addGap(18, 18, 18)
                .addGroup(panelLicenseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(add_new_territory_text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelLicenseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bu_add_allowed_territory)
                    .addComponent(bu_add_disallowed_territory))
                .addContainerGap(185, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("License", panelLicense);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                    .addComponent(panelBasics, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jTabbedPane1, panelBasics});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelBasics, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(160, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void check_contributor_publish_phoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_check_contributor_publish_phoneActionPerformed
	 int selCon = list_contributors.getSelectedIndex();
         Contributor c = null;
         if (selCon>=0 && selCon < bundle.getContributorCount()) {
            c = bundle.getContributor(selCon);
            if (c.getWww()==null) {
                c.www(InfoWWW.make());
            }
            if (c.getWww().getPhone()==null) {
                c.getWww().phone("");
            }
            c.getWww().phone(c.getWww().getPhone(), check_contributor_publish_phone.isSelected());
            notifyChanges();
        }
    }//GEN-LAST:event_check_contributor_publish_phoneActionPerformed

    private void bu_contributor_addActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_contributor_addActionPerformed
        if (bundle != null) {
            Contributor c = Contributor.make("new contributor", "new type", IDs.make());
            bundle.addContributor(c);
            updateContributorList();
            list_contributors.setSelectedIndex(list_contributors.getModel().getSize() - 1);
            notifyChanges();
        }
    }//GEN-LAST:event_bu_contributor_addActionPerformed

    private void bu_add_promotextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_add_promotextActionPerformed
        if (bundle != null && bundle.getInformation() != null) {
            bundle.getInformation().addPromotext("NEW_LANG", "");
            table_promotext.setModel(new PromotextTableModel(bundle.getInformation()));
            notifyChanges();
        }
    }//GEN-LAST:event_bu_add_promotextActionPerformed

    private void bu_contributor_removeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_contributor_removeActionPerformed
        if (bundle != null) {
            int sel = list_contributors.getSelectedIndex();
            if (sel >= 0 && sel < bundle.getContributorCount()) {
                bundle.removeContributor(sel);
                updateContributorList();
            }
        }
    }//GEN-LAST:event_bu_contributor_removeActionPerformed

    private void list_contributorsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_list_contributorsValueChanged
        int selCon = list_contributors.getSelectedIndex();
        Contributor c = null;
         if (selCon>=0 && selCon < bundle.getContributorCount()) {
            c = bundle.getContributor(selCon);
            updateContributor(c);
        }
    }//GEN-LAST:event_list_contributorsValueChanged

    private void select_contributor_typeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_select_contributor_typeActionPerformed
        int selCon = list_contributors.getSelectedIndex();
        Contributor c = null;
         if (selCon>=0 && selCon < bundle.getContributorCount()) {
            c = bundle.getContributor(selCon);
            c.type((String)select_contributor_type.getSelectedItem());
            DefaultListModel lm = (DefaultListModel)list_contributors.getModel();
            lm.set(selCon, c.getName() + " (" + c.getType() + ")");
            notifyChanges();
        }
    }//GEN-LAST:event_select_contributor_typeActionPerformed

    private void bu_remove_promotextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_remove_promotextActionPerformed
        int sel = table_promotext.getSelectedRow();
        if (sel>=0) {
            String lang = (String)table_promotext.getValueAt(sel, 0);
            bundle.getInformation().removePromotext(lang);
            bundle.getInformation().removeTeasertext(lang);
            table_promotext.setModel(new PromotextTableModel(bundle.getInformation()));
            notifyChanges();
        }
    }//GEN-LAST:event_bu_remove_promotextActionPerformed

    private void bu_add_allowed_territoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_add_allowed_territoryActionPerformed
       if (!add_new_territory_text.getText().equals("")) {
           DefaultListModel lm = (DefaultListModel)list_allowed_territories.getModel();
           String t = add_new_territory_text.getText();
           bundle.getLicense_basis().getTerritorial().allow(t);
           lm.addElement(t);
           add_new_territory_text.setText("");
           notifyChanges();
        }
    }//GEN-LAST:event_bu_add_allowed_territoryActionPerformed

    private void bu_add_disallowed_territoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_add_disallowed_territoryActionPerformed
         if (!add_new_territory_text.getText().equals("")) {
           DefaultListModel lm = (DefaultListModel)list_disallowed_territories.getModel();
           String t = add_new_territory_text.getText();
           bundle.getLicense_basis().getTerritorial().disallow(t);
           lm.addElement(t);
           add_new_territory_text.setText("");
           notifyChanges();
        }
    }//GEN-LAST:event_bu_add_disallowed_territoryActionPerformed

    private void bu_remove_allowed_territoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_remove_allowed_territoryActionPerformed
       int sel = list_allowed_territories.getSelectedIndex();
       if (sel>=0) {
           DefaultListModel lm = (DefaultListModel)list_allowed_territories.getModel();
           String s = (String)lm.getElementAt(sel);
           bundle.getLicense_basis().getTerritorial().remove(s);
           lm.remove(sel);
           notifyChanges();
       }
    }//GEN-LAST:event_bu_remove_allowed_territoryActionPerformed

    private void select_license_pricingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_select_license_pricingActionPerformed
        int sel = select_license_pricing.getSelectedIndex();
        if (sel == 0) { //other
            bundle.getLicense_basis().pricing_pricecode(null);
            bundle.getLicense_basis().pricing_wholesale(text_license_pricing.getText());
            text_license_pricing.setEnabled(true);
        } else {
            bundle.getLicense_basis().pricing_pricecode((String)select_license_pricing.getSelectedItem());
            bundle.getLicense_basis().pricing_wholesale(null);
            text_license_pricing.setText("");
            text_license_pricing.setEnabled(false);
        }
        notifyChanges();
    }//GEN-LAST:event_select_license_pricingActionPerformed

    private void bu_remove_disallowed_territoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_remove_disallowed_territoryActionPerformed
      int sel = list_disallowed_territories.getSelectedIndex();
       if (sel>=0) {
           DefaultListModel lm = (DefaultListModel)list_disallowed_territories.getModel();
           String s = (String)lm.getElementAt(sel);
           bundle.getLicense_basis().getTerritorial().remove(s);
           lm.remove(sel);
           notifyChanges();
       }
    }//GEN-LAST:event_bu_remove_disallowed_territoryActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField add_new_territory_text;
    private javax.swing.JButton bu_add_allowed_territory;
    private javax.swing.JButton bu_add_disallowed_territory;
    private javax.swing.JButton bu_add_promotext;
    private javax.swing.JButton bu_contributor_add;
    private javax.swing.JButton bu_contributor_remove;
    private javax.swing.JButton bu_remove_allowed_territory;
    private javax.swing.JButton bu_remove_disallowed_territory;
    private javax.swing.JButton bu_remove_promotext;
    private javax.swing.JCheckBox check_contributor_publish_phone;
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
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
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
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JList list_allowed_territories;
    private javax.swing.JList list_contributors;
    private javax.swing.JList list_disallowed_territories;
    private javax.swing.JPanel panelBasics;
    private javax.swing.JPanel panelContributors;
    private javax.swing.JPanel panelIDs;
    private javax.swing.JPanel panelInformation;
    private javax.swing.JPanel panelLicense;
    private javax.swing.JScrollPane scroll_table_promotext;
    private javax.swing.JComboBox select_contributor_type;
    private javax.swing.JComboBox select_license_pricing;
    private javax.swing.JTable table_promotext;
    private javax.swing.JTextField text_amazon;
    private javax.swing.JTextField text_contentauthid;
    private javax.swing.JTextField text_contributor_contentauthid;
    private javax.swing.JTextField text_contributor_facebook;
    private javax.swing.JTextField text_contributor_finetunesid;
    private javax.swing.JTextField text_contributor_gvl;
    private javax.swing.JTextField text_contributor_homepage;
    private javax.swing.JTextField text_contributor_myspace;
    private javax.swing.JTextField text_contributor_name;
    private javax.swing.JTextField text_contributor_ourid;
    private javax.swing.JTextField text_contributor_phone;
    private javax.swing.JTextField text_contributor_twitter;
    private javax.swing.JTextField text_contributor_yourid;
    private javax.swing.JTextField text_digital_release_date;
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
    private javax.swing.JTextField text_physical_release_datetime;
    private javax.swing.JTextField text_upc;
    private javax.swing.JTextField text_version;
    private javax.swing.JTextField text_yourid;
    // End of variables declaration//GEN-END:variables
}
