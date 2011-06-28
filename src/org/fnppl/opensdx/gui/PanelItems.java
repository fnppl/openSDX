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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Vector;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;
import org.fnppl.opensdx.common.Bundle;
import org.fnppl.opensdx.common.BundleInformation;
import org.fnppl.opensdx.common.Contributor;
import org.fnppl.opensdx.common.IDs;
import org.fnppl.opensdx.common.Item;
import org.fnppl.opensdx.common.ItemFile;
import org.fnppl.opensdx.common.ItemTags;
import org.fnppl.opensdx.common.LicenseBasis;
import org.fnppl.opensdx.common.Territorial;
import org.fnppl.opensdx.dmi.FeedGui;
import org.fnppl.opensdx.security.SecurityHelper;

/**
 *
 * @author Bertram Boedeker <bboedeker@gmx.de>
 */
public class PanelItems extends javax.swing.JPanel implements MyObservable, MyObserver {


    private Bundle bundle;
    private DocumentChangeListener changeListener;
    private PanelFeedInfo me;
    protected Vector<String> availableGenres = new Vector<String>();
    protected Vector<String> selectedGenres = new Vector<String>();
     private EditTerritoiresTree tree_territories;

    private Vector<MyObserver> observers = new Vector<MyObserver>();
    public void addObserver(MyObserver observer) {
    	observers.add(observer);
    }

    public void setAvailableGenres(Vector<String> genres) {
        availableGenres = genres;
        DefaultListModel model = new DefaultListModel();
        for (String genre : genres) {
            model.addElement(genre);
        }
        list_genres.setModel(model);
        list_genres.setCellRenderer(new ListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
               final String genre = (String)value;
               final JCheckBox check = new JCheckBox(genre);
               if (selectedGenres.contains(genre)) {
                   check.setSelected(true);
               } else {
                   check.setSelected(false);
               }
               check.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (check.isSelected()) {
                            if (!selectedGenres.contains(genre)) selectedGenres.add(genre);
                        } else {
                            if (selectedGenres.contains(genre)) selectedGenres.remove(genre);
                        }
                        list_genres.updateUI();
                    }
                });
               
               
               JPanel p = new JPanel();
               p.setLayout(new BorderLayout());
               p.add(check, BorderLayout.CENTER);
               if (isSelected) {
                   p.setBackground(UIManager.getColor("List.background"));
               } else {
                   p.setBackground(Color.WHITE);
               }

               return p;
            }
        });
        list_genres.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list_genres.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int index = list_genres.locationToIndex(e.getPoint());
                if (index >= 0) {
                  String genre = availableGenres.get(index);
                  if (selectedGenres.contains(genre)) {
                      selectedGenres.remove(genre);
                  } else {
                      selectedGenres.add(genre);
                  }
                  updateGenres();
                  repaint();
                }
            }
        });
        list_genres.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_SPACE){
                  String genre = (String)list_genres.getSelectedValue();
                  if (genre!=null) {
                      if (selectedGenres.contains(genre)) {
                          selectedGenres.remove(genre);
                      } else {
                          selectedGenres.add(genre);
                      }
                      updateGenres();
                      repaint();
                   }
                }
            }
        });

    }

    private void updateGenres() {
        Item item = getSelectedItem();
        if (item==null) return;
        ItemTags tags = item.getTags();
        if (tags==null) {
            tags = ItemTags.make();
            item.tags(tags);
        }
        tags.removeAllGenres();
        for (String g : selectedGenres) {
            tags.addGenre(g);
        }
        notifyChanges( );
    }

    public void notifyChange() {
      //check for  changes in bundle contributors
      if (bundle==null || bundle.getItemsCount()==0) return;
      int selItem = list_items.getSelectedIndex();
      if (selItem<0) return;
      Item item = bundle.getItem(selItem);
      updateContributorList(item);

      item.getLicense_basis().setTerritorial(tree_territories.getTerritorial());
      updateLicense(item);
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
            } else {
                updateItem(null);
            }
        } else {
            updateItem(null);
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
        if (lm.size()>0) {
            list_items.setSelectedIndex(0);
        }
        list_items.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        /*list_items.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int sel = e.getFirstIndex();
                if (sel >= 0 && sel < bundle.getItemsCount()) {
                    Item item = bundle.getItem(sel);
                    updateItem(item);
                }
            }
	 });*/
    }

     private void updateFileList(final Item item) {
        DefaultListModel lm = new DefaultListModel();
    	 if (item!=null) {
	        int anz = item.getFilesCount();
	        for (int i = 0; i < anz; i++) {
	            lm.addElement("File: "+item.getFile(i).getLocationPath());
	        }
    	 }
        list_files.setModel(lm);
        list_files.setSelectedIndex(0);
        list_files.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list_files.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int sel = list_files.getSelectedIndex();
                if (sel>= 0 && sel < item.getFilesCount()) {
                    ItemFile file = item.getFile(sel);
                    updateFile(file);
                }
            }
	 });
    }

    private void updateFile(ItemFile file) {
        if (file != null) {
            text_file_path.setText(file.getLocationPath());
            text_file_format.setText(file.getFiletype());
            String channel = file.getChannels();
            if (channel == null) {
                channel =  "[no audio]";
            }
            select_file_channel.setSelectedItem(channel);
            String type = file.getType();
            if (type == null) {
                type = "[not specified]";
            }
            select_file_type.setSelectedItem(type);
            text_file_length.setText(""+file.getBytes());
            text_file_md5.setText(file.getChecksums().getMd5String());
            text_file_sha1.setText(file.getChecksums().getSha1String());
            changeListener.saveState(text_file_format);
        }
    }

    private void updateTags(ItemTags tags) {
        if (tags!=null) {
            text_tags_main_language.setText(tags.getMain_language());
            text_tags_origin_country.setText(tags.getOrigin_country());
            check_tags_bundle_only.setSelected(tags.isBundle_only());
            check_tags_stream_allowed.setSelected(tags.isStreaming_allowed());
            selectedGenres.removeAllElements();
            for (int i=0;i<tags.getGenresCount();i++) {
                String genre = tags.getGenre(i);
                selectedGenres.add(genre);
                if (!availableGenres.contains(genre)) {
                    availableGenres.add(genre);
                }
            }
        }
    }

      private void updateItem(Item item) {
          //System.out.println("update item");
        if (item != null) {
            panelBasics.setVisible(true);
            tab_items.setVisible(true);
            
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
            updateLanguageList();
            //updateLicense(null);
        }

        //License
        updateLicense(item);

        //tags
        updateTags(item.getTags());

        //files
        updateFileList(item);
        ItemFile itemfile = getSelectedFile();
        if (itemfile!=null) {
            updateFile(itemfile);
        }
        changeListener.saveStates();
        } else {
            panelBasics.setVisible(false);
            tab_items.setVisible(false);
        }
    }

      private void updateLicense(Item item) {
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
                if (lb.getPricingPricecode()!=null) {
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
                    tree_territories.setTerritories(t);
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
            tree_territories.setTerritories(Territorial.make());
            DefaultListModel lmAllow = (DefaultListModel)list_allowed_territories.getModel();
            DefaultListModel lmDisallow = (DefaultListModel)list_disallowed_territories.getModel();
            lmAllow.removeAllElements();
            lmDisallow.removeAllElements();
        }
        checkLicenseAsOnBundleActionPerformed(null);
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
        Vector<JTextComponent> texts = new Vector<JTextComponent>();
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
        texts.add(text_promotext);
        texts.add(text_teasertext);
        
        //license
        texts.add(text_license_from_datetime);
        texts.add(text_license_pricing);
        texts.add(text_license_to_datetime);

        //tags
        //texts.add(text_tags_main_language);
        //texts.add(text_tags_origin_country);

        //files
        texts.add(text_file_format);


        changeListener = new DocumentChangeListener(texts);

         KeyAdapter keyAdapt = new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER){
                    if (e.getComponent() instanceof JTextField) {
                        JTextComponent text = (JTextComponent)e.getComponent();
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
                            else if(text == text_file_format) {
                                ItemFile file = item.getFile(list_files.getSelectedIndex());
                                file.filetype(t);
                            }
                             else if (text == text_tags_main_language) {
                                 ItemTags tags = item.getTags();
                                 if (tags == null)  {
                                     tags = ItemTags.make();
                                     item.tags(tags);
                                 }
                                 tags.main_language(t);
                             }
                            else if (text == text_tags_origin_country) {
                                 ItemTags tags = item.getTags();
                                 if (tags == null)  {
                                     tags = ItemTags.make();
                                     item.tags(tags);
                                 }
                                 tags.origin_country(t);
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


        for (JTextComponent text : texts) {
            text.getDocument().addDocumentListener(changeListener);
            if (text instanceof JTextField) {
              text.addKeyListener(keyAdapt);
            }
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
        list_items.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int sel = e.getFirstIndex();
                if (bundle==null) return;
                if (sel >= 0 && sel < bundle.getItemsCount()) {
                    Item item = bundle.getItem(sel);
                    updateItem(item);
                }
            }
	 });
        tree_territories = new EditTerritoiresTree();
        tree_territories.addObserver(this);

        panel_territories.setLayout(new BorderLayout());
        panel_territories.add(new JScrollPane(tree_territories), BorderLayout.CENTER);
        

        //test genres
        availableGenres.add("genre 1");
        availableGenres.add("genre 2");
        availableGenres.add("genre 3");
       // setAvailableGenres(availableGenres);

        DefaultListModel lm = new DefaultListModel();
        list_language.setModel(lm);
        list_language.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list_language.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                //System.out.println("value changed");
                int sel = list_language.getSelectedIndex();
                if (sel>=0 && sel<list_language.getModel().getSize()) {
                    String lang = (String)list_language.getModel().getElementAt(sel);
                   // System.out.println("   update lang: "+lang);
                    updatePromoTexts(lang);
                }
            }
        });

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
        tab_items = new javax.swing.JTabbedPane();
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
        jLabel1 = new javax.swing.JLabel();
        text_playlength = new javax.swing.JTextField();
        jScrollPane4 = new javax.swing.JScrollPane();
        list_language = new javax.swing.JList();
        jScrollPane9 = new javax.swing.JScrollPane();
        text_promotext = new javax.swing.JTextArea();
        bu_promotext_update = new javax.swing.JButton();
        bu_promotext_reset = new javax.swing.JButton();
        jScrollPane10 = new javax.swing.JScrollPane();
        text_teasertext = new javax.swing.JTextArea();
        bu_teasertext_update = new javax.swing.JButton();
        bu_teasertext_reset = new javax.swing.JButton();
        bu_remove_language = new javax.swing.JButton();
        bu_add_language = new javax.swing.JButton();
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
        select_license_pricing = new javax.swing.JComboBox();
        panel_territories = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        check_tags_bundle_only = new javax.swing.JCheckBox();
        check_tags_stream_allowed = new javax.swing.JCheckBox();
        jScrollPane7 = new javax.swing.JScrollPane();
        list_genres = new javax.swing.JList();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        text_tags_main_language = new javax.swing.JTextField();
        text_tags_origin_country = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        bu_main_language = new javax.swing.JButton();
        bu_select_country = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane8 = new javax.swing.JScrollPane();
        list_files = new javax.swing.JList();
        jLabel24 = new javax.swing.JLabel();
        bu_add_file = new javax.swing.JButton();
        bu_remove_file = new javax.swing.JButton();
        panelBasics1 = new javax.swing.JPanel();
        jLabel26 = new javax.swing.JLabel();
        text_file_path = new javax.swing.JTextField();
        jLabel28 = new javax.swing.JLabel();
        select_file_type = new javax.swing.JComboBox();
        jLabel25 = new javax.swing.JLabel();
        text_file_format = new javax.swing.JTextField();
        jLabel27 = new javax.swing.JLabel();
        select_file_channel = new javax.swing.JComboBox();
        jLabel29 = new javax.swing.JLabel();
        text_file_length = new javax.swing.JTextField();
        jLabel35 = new javax.swing.JLabel();
        text_file_md5 = new javax.swing.JTextField();
        jLabel36 = new javax.swing.JLabel();
        text_file_sha1 = new javax.swing.JTextField();
        bu_change_file = new javax.swing.JButton();
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
        select_type.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                select_typeActionPerformed(evt);
            }
        });

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
                .addGroup(panelIDsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelIDsLayout.createSequentialGroup()
                        .addGroup(panelIDsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addComponent(jLabel10)
                            .addComponent(jLabel9))
                        .addGap(95, 95, 95)
                        .addGroup(panelIDsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(text_isrc, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                            .addComponent(text_grid, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                            .addComponent(text_upc, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)))
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
                        .addGroup(panelIDsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(text_labelordernum, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(text_contentauthid, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(text_amazon, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(text_isbn, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(text_finetunesid, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(text_ourid, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(text_yourid, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE))))
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelIDsBigLayout = new javax.swing.GroupLayout(panelIDsBig);
        panelIDsBig.setLayout(panelIDsBigLayout);
        panelIDsBigLayout.setHorizontalGroup(
            panelIDsBigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 690, Short.MAX_VALUE)
            .addGroup(panelIDsBigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelIDsBigLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(panelIDs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(319, Short.MAX_VALUE)))
        );
        panelIDsBigLayout.setVerticalGroup(
            panelIDsBigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 999, Short.MAX_VALUE)
            .addGroup(panelIDsBigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelIDsBigLayout.createSequentialGroup()
                    .addGap(19, 19, 19)
                    .addComponent(panelIDs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(595, Short.MAX_VALUE)))
        );

        tab_items.addTab("IDs", panelIDsBig);

        jLabel18.setFont(new java.awt.Font("Ubuntu", 1, 15));
        jLabel18.setText("List of contributors for this item");

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
                .addContainerGap(103, Short.MAX_VALUE))
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
                .addContainerGap(794, Short.MAX_VALUE))
        );

        tab_items.addTab("Contributors", panelContributors);

        panelInformation.setBorder(javax.swing.BorderFactory.createTitledBorder("Information"));

        jLabel30.setText("physical release date");

        jLabel31.setText("digital release date");

        jLabel1.setText("playlength in seconds");

        list_language.setBorder(javax.swing.BorderFactory.createTitledBorder("Language"));
        list_language.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane4.setViewportView(list_language);

        text_promotext.setColumns(20);
        text_promotext.setRows(5);
        text_promotext.setBorder(javax.swing.BorderFactory.createTitledBorder("Promotion Text"));
        jScrollPane9.setViewportView(text_promotext);

        bu_promotext_update.setText("update");
        bu_promotext_update.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bu_promotext_updateActionPerformed(evt);
            }
        });

        bu_promotext_reset.setText("reset");
        bu_promotext_reset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bu_promotext_resetActionPerformed(evt);
            }
        });

        text_teasertext.setColumns(20);
        text_teasertext.setRows(5);
        text_teasertext.setBorder(javax.swing.BorderFactory.createTitledBorder("Teaser Text"));
        jScrollPane10.setViewportView(text_teasertext);

        bu_teasertext_update.setText("update");
        bu_teasertext_update.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bu_teasertext_updateActionPerformed(evt);
            }
        });

        bu_teasertext_reset.setText("reset");
        bu_teasertext_reset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bu_teasertext_resetActionPerformed(evt);
            }
        });

        bu_remove_language.setText("remove");
        bu_remove_language.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bu_remove_languageActionPerformed(evt);
            }
        });

        bu_add_language.setText("add");
        bu_add_language.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bu_add_languageActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelInformationLayout = new javax.swing.GroupLayout(panelInformation);
        panelInformation.setLayout(panelInformationLayout);
        panelInformationLayout.setHorizontalGroup(
            panelInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelInformationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelInformationLayout.createSequentialGroup()
                        .addGroup(panelInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel30)
                            .addComponent(jLabel31)
                            .addComponent(jLabel1))
                        .addGap(18, 18, 18)
                        .addGroup(panelInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(text_playlength)
                            .addComponent(text_physical_realease_datetime)
                            .addComponent(text_digital_release_datetime, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)))
                    .addGroup(panelInformationLayout.createSequentialGroup()
                        .addGroup(panelInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(panelInformationLayout.createSequentialGroup()
                                .addComponent(bu_add_language)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(bu_remove_language))
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(panelInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelInformationLayout.createSequentialGroup()
                                .addComponent(bu_teasertext_update)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(bu_teasertext_reset))
                            .addComponent(jScrollPane10, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 501, Short.MAX_VALUE)
                            .addGroup(panelInformationLayout.createSequentialGroup()
                                .addComponent(bu_promotext_update)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(bu_promotext_reset))
                            .addComponent(jScrollPane9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 501, Short.MAX_VALUE))))
                .addContainerGap())
        );

        panelInformationLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jScrollPane10, jScrollPane9});

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
                .addGap(34, 34, 34)
                .addGroup(panelInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelInformationLayout.createSequentialGroup()
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(panelInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(bu_add_language)
                            .addComponent(bu_remove_language)))
                    .addGroup(panelInformationLayout.createSequentialGroup()
                        .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(bu_promotext_update)
                            .addComponent(bu_promotext_reset))
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bu_teasertext_update)
                    .addComponent(bu_teasertext_reset))
                .addContainerGap(456, Short.MAX_VALUE))
        );

        panelInformationLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jScrollPane10, jScrollPane9});

        tab_items.addTab("Information", panelInformation);

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

        select_license_pricing.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "[other]", "LOW", "MEDIUM", "HIGH" }));
        select_license_pricing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                select_license_pricingActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panel_territoriesLayout = new javax.swing.GroupLayout(panel_territories);
        panel_territories.setLayout(panel_territoriesLayout);
        panel_territoriesLayout.setHorizontalGroup(
            panel_territoriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 628, Short.MAX_VALUE)
        );
        panel_territoriesLayout.setVerticalGroup(
            panel_territoriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 556, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout panelLicense1Layout = new javax.swing.GroupLayout(panelLicense1);
        panelLicense1.setLayout(panelLicense1Layout);
        panelLicense1Layout.setHorizontalGroup(
            panelLicense1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLicense1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelLicense1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelLicense1Layout.createSequentialGroup()
                        .addComponent(panel_territories, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(panelLicense1Layout.createSequentialGroup()
                        .addGroup(panelLicense1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelLicense1Layout.createSequentialGroup()
                                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(26, 26, 26)
                                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelLicense1Layout.createSequentialGroup()
                                .addGroup(panelLicense1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel33)
                                    .addComponent(jLabel34)
                                    .addComponent(jLabel32))
                                .addGap(39, 39, 39)
                                .addGroup(panelLicense1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(panelLicense1Layout.createSequentialGroup()
                                        .addComponent(select_license_pricing, 0, 121, Short.MAX_VALUE)
                                        .addGap(18, 18, 18)
                                        .addComponent(text_license_pricing, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(text_license_to_datetime, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
                                    .addComponent(text_license_from_datetime, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE))))
                        .addGap(217, 217, 217))))
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
                .addGap(18, 18, 18)
                .addComponent(panel_territories, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout panelLicenseLayout = new javax.swing.GroupLayout(panelLicense);
        panelLicense.setLayout(panelLicenseLayout);
        panelLicenseLayout.setHorizontalGroup(
            panelLicenseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLicenseLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelLicenseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelLicense1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(checkLicenseAsOnBundle))
                .addContainerGap())
        );
        panelLicenseLayout.setVerticalGroup(
            panelLicenseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLicenseLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkLicenseAsOnBundle)
                .addGap(18, 18, 18)
                .addComponent(panelLicense1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        tab_items.addTab("License", panelLicense);

        check_tags_bundle_only.setText("bundle only");
        check_tags_bundle_only.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                check_tags_bundle_onlyActionPerformed(evt);
            }
        });

        check_tags_stream_allowed.setText("streaming allowed");
        check_tags_stream_allowed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                check_tags_stream_allowedActionPerformed(evt);
            }
        });

        list_genres.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Contributor 1", "Contributor 2", "Contributor 3", "Contributor 4", "Contributor 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane7.setViewportView(list_genres);

        jLabel20.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel20.setText("List of genres for this item");

        jLabel21.setText("main language");

        text_tags_main_language.setEditable(false);

        text_tags_origin_country.setEditable(false);

        jLabel22.setText("origin country");

        bu_main_language.setText("select");
        bu_main_language.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bu_main_languageActionPerformed(evt);
            }
        });

        bu_select_country.setText("select");
        bu_select_country.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bu_select_countryActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(check_tags_stream_allowed)
                    .addComponent(check_tags_bundle_only)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel21)
                            .addComponent(jLabel22))
                        .addGap(38, 38, 38)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(text_tags_origin_country)
                            .addComponent(text_tags_main_language, javax.swing.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(bu_main_language, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(bu_select_country, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(47, 47, 47)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane7)
                    .addComponent(jLabel20))
                .addContainerGap(131, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(49, 49, 49)
                        .addComponent(check_tags_bundle_only)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(check_tags_stream_allowed)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel21)
                            .addComponent(text_tags_main_language, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(bu_main_language))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(text_tags_origin_country, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel22)
                            .addComponent(bu_select_country)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(jLabel20)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(732, Short.MAX_VALUE))
        );

        tab_items.addTab("Tags", jPanel2);

        jScrollPane8.setViewportView(list_files);

        jLabel24.setFont(new java.awt.Font("Ubuntu", 1, 15));
        jLabel24.setText("List of files");

        bu_add_file.setText("add");
        bu_add_file.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bu_add_fileActionPerformed(evt);
            }
        });

        bu_remove_file.setText("remove");
        bu_remove_file.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bu_remove_fileActionPerformed(evt);
            }
        });

        panelBasics1.setBorder(javax.swing.BorderFactory.createTitledBorder("File properties"));

        jLabel26.setText("path");

        text_file_path.setEditable(false);
        text_file_path.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                text_file_pathActionPerformed(evt);
            }
        });

        jLabel28.setText("type");

        select_file_type.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "[not specified]", "full", "pre-listening", "cover" }));
        select_file_type.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                select_file_typeActionPerformed(evt);
            }
        });

        jLabel25.setText("format");

        jLabel27.setText("channels");

        select_file_channel.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "[no audio]", "stereo", "mono", "joint-stereo", "5.1" }));
        select_file_channel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                select_file_channelActionPerformed(evt);
            }
        });

        jLabel29.setText("length in bytes");

        text_file_length.setEditable(false);

        jLabel35.setText("md5 checksum");

        text_file_md5.setEditable(false);

        jLabel36.setText("sha1 checksum");

        text_file_sha1.setEditable(false);

        bu_change_file.setText("change");
        bu_change_file.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bu_change_fileActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelBasics1Layout = new javax.swing.GroupLayout(panelBasics1);
        panelBasics1.setLayout(panelBasics1Layout);
        panelBasics1Layout.setHorizontalGroup(
            panelBasics1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBasics1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelBasics1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel29)
                    .addGroup(panelBasics1Layout.createSequentialGroup()
                        .addGroup(panelBasics1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel26)
                            .addComponent(jLabel28)
                            .addComponent(jLabel25)
                            .addComponent(jLabel35)
                            .addComponent(jLabel36))
                        .addGap(51, 51, 51)
                        .addGroup(panelBasics1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelBasics1Layout.createSequentialGroup()
                                .addComponent(text_file_path, javax.swing.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addComponent(bu_change_file))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelBasics1Layout.createSequentialGroup()
                                .addGroup(panelBasics1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(text_file_md5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 423, Short.MAX_VALUE)
                                    .addGroup(panelBasics1Layout.createSequentialGroup()
                                        .addGroup(panelBasics1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(text_file_format, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)
                                            .addComponent(select_file_type, javax.swing.GroupLayout.Alignment.LEADING, 0, 202, Short.MAX_VALUE)
                                            .addComponent(text_file_length, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE))
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel27)
                                        .addGap(18, 18, 18)
                                        .addComponent(select_file_channel, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(text_file_sha1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 423, Short.MAX_VALUE))
                                .addGap(26, 26, 26)))))
                .addContainerGap())
        );
        panelBasics1Layout.setVerticalGroup(
            panelBasics1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBasics1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelBasics1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel26)
                    .addComponent(text_file_path, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bu_change_file))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelBasics1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(text_file_format, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel25)
                    .addComponent(jLabel27)
                    .addComponent(select_file_channel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14)
                .addGroup(panelBasics1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel28)
                    .addComponent(select_file_type, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(40, 40, 40)
                .addGroup(panelBasics1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel29)
                    .addComponent(text_file_length, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelBasics1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel35)
                    .addComponent(text_file_md5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelBasics1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel36)
                    .addComponent(text_file_sha1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelBasics1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel24)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(bu_remove_file)
                            .addComponent(bu_add_file, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(8, 8, 8)))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {bu_add_file, bu_remove_file});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel24)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(bu_add_file)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bu_remove_file))
                    .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(panelBasics1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(51, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(448, Short.MAX_VALUE))
        );

        tab_items.addTab("Files", jPanel3);

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
                    .addComponent(tab_items, 0, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 490, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(bu_remove_item)
                                    .addComponent(bu_add_item, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(panelBasics, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(12, Short.MAX_VALUE))))
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
                .addGap(18, 18, Short.MAX_VALUE)
                .addComponent(panelBasics, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tab_items, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void bu_add_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_add_itemActionPerformed
       if (bundle!=null) {
           long now = System.currentTimeMillis();
           Item newItem = Item.make(IDs.make(), "new item", "", "", "audio", "", BundleInformation.make(now,now), LicenseBasis.makeAsOnBundle(),null);
           bundle.addItem(newItem);
           updateItemList();
           updateItem(newItem);
           list_items.setSelectedIndex(list_items.getModel().getSize()-1);
           notifyChanges();
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
            item.getLicense_basis().setTerritorial(tree_territories.getTerritorial());
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

    private void select_license_pricingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_select_license_pricingActionPerformed
        int sel = select_license_pricing.getSelectedIndex();
        Item item = getSelectedItem();
        if (sel == 0) { //other
            if (item!=null) {
                item.getLicense_basis().pricing_pricecode(null);
                item.getLicense_basis().pricing_wholesale(text_license_pricing.getText());
            }
            text_license_pricing.setEnabled(true);
            changeListener.saveState(text_license_pricing);
        } else {
            if (item!=null) {
               item.getLicense_basis().pricing_pricecode((String)select_license_pricing.getSelectedItem());
               item.getLicense_basis().pricing_wholesale(null);
            }
            text_license_pricing.setText("");
            text_license_pricing.setEnabled(false);
            changeListener.saveState(text_license_pricing);
        }
        notifyChanges();
}//GEN-LAST:event_select_license_pricingActionPerformed

    private void text_displaynameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_text_displaynameActionPerformed

    }//GEN-LAST:event_text_displaynameActionPerformed

    private void bu_remove_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_remove_itemActionPerformed
        if (bundle==null || bundle.getItemsCount()==0) return;
        int selItem = list_items.getSelectedIndex();
        if (selItem<0) return;
        bundle.removeItem(selItem);
        updateItemList();
        int s = list_items.getModel().getSize();
        if (s>0) {
            if (s>selItem) {
                list_items.setSelectedIndex(selItem);
            } else {
                list_items.setSelectedIndex(s-1);
            }
        } else {
           updateItem(null);
        }
        notifyChanges();
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

    private void bu_add_fileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_add_fileActionPerformed
        Item item = getSelectedItem();
        if (item==null) return;
        File path = null;
        ItemFile selFile = getSelectedFile();
        if (selFile!=null) {
            try {
                path = new File(selFile.getLocationPath()).getParentFile();
            } catch (Exception ex) {
            }
        }
        File f = Dialogs.chooseOpenFile("Choose file", path, "");
        if (f==null || !f.exists() || f.isDirectory()) return;
        ItemFile file = ItemFile.make(f);
        item.addFile(file);
        updateFileList(item);
        list_files.setSelectedIndex(item.getFilesCount()-1);
        updateFile(file);
        notifyChanges();
    }//GEN-LAST:event_bu_add_fileActionPerformed

    private void bu_remove_fileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_remove_fileActionPerformed
        Item item = getSelectedItem();
        if (item==null) return;
        int sel = list_files.getSelectedIndex();
        if (sel>=0 && sel < item.getFilesCount()) {
            item.removeFile(sel);
        }
        updateFileList(item);
        if (sel<item.getFilesCount()) {
            list_files.setSelectedIndex(sel);
        } else if (item.getFilesCount()>0) {
            list_files.setSelectedIndex(item.getFilesCount()-1);
        }
        notifyChanges();
    }//GEN-LAST:event_bu_remove_fileActionPerformed

    private void text_file_pathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_text_file_pathActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_text_file_pathActionPerformed

    private void bu_change_fileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_change_fileActionPerformed
        File lastFile = new File(text_file_path.getText());
        File f = Dialogs.chooseOpenFile("Choose file", lastFile.getParentFile(), lastFile.getName());
        if (f==null || !f.exists() || f.isDirectory()) return;
        ItemFile file = getSelectedFile();
        if (file!=null) {
            file.setFile(f);
            updateFile(file);
            notifyChanges();
        }
    }//GEN-LAST:event_bu_change_fileActionPerformed

    private void select_file_channelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_select_file_channelActionPerformed
       ItemFile file = getSelectedFile();
       if (file!=null) {
           int sel = select_file_channel.getSelectedIndex();
           if (sel==0) {
               file.channels(null);
           } else {
               file.channels((String)select_file_channel.getSelectedItem());
           }
           notifyChanges();
       }
    }//GEN-LAST:event_select_file_channelActionPerformed

    private void select_file_typeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_select_file_typeActionPerformed
       ItemFile file = getSelectedFile();
       if (file!=null) {
           int sel = select_file_type.getSelectedIndex();
           if (sel==0) {
               file.type(null);
           } else {
               file.type((String)select_file_type.getSelectedItem());
           }
           notifyChanges();
       }
    }//GEN-LAST:event_select_file_typeActionPerformed

    private void check_tags_bundle_onlyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_check_tags_bundle_onlyActionPerformed
       Item item = getSelectedItem();
       if (item==null) return;
       ItemTags tags = item.getTags();
       if (tags==null) {
           tags = ItemTags.make();
           item.tags(tags);
       }
       tags.bundle_only(check_tags_bundle_only.isSelected());
       notifyChanges();
    }//GEN-LAST:event_check_tags_bundle_onlyActionPerformed

    private void check_tags_stream_allowedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_check_tags_stream_allowedActionPerformed
       Item item = getSelectedItem();
       if (item==null) return;
       ItemTags tags = item.getTags();
       if (tags==null) {
           tags = ItemTags.make();
           item.tags(tags);
       }
       tags.streaming_allowed(check_tags_stream_allowed.isSelected());
       notifyChanges();
    }//GEN-LAST:event_check_tags_stream_allowedActionPerformed

    private void select_typeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_select_typeActionPerformed
        Item item = getSelectedItem();
        if (item!=null) {
            item.type((String)select_type.getSelectedItem());
            notifyChanges();
        }
    }//GEN-LAST:event_select_typeActionPerformed

    private void bu_promotext_updateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_promotext_updateActionPerformed
        Item item = getSelectedItem();
        if (item==null) return;
        String lang = (String)list_language.getSelectedValue();
        if (lang!=null) {
            item.getInformation().setPromotext(lang, text_promotext.getText());
            text_promotext.setBackground(Color.WHITE);
            changeListener.saveState(text_promotext);
            notifyChanges();
        }
}//GEN-LAST:event_bu_promotext_updateActionPerformed

    private void bu_promotext_resetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_promotext_resetActionPerformed
        text_promotext.setText(changeListener.getSavedText(text_promotext));
        text_promotext.setBackground(Color.WHITE);
}//GEN-LAST:event_bu_promotext_resetActionPerformed

    private void bu_teasertext_updateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_teasertext_updateActionPerformed
        if (bundle==null) return;
        Item item = getSelectedItem();
        if (item==null) return;
        String lang = (String)list_language.getSelectedValue();
        if (lang!=null) {
            item.getInformation().setTeasertext(lang, text_teasertext.getText());
            text_teasertext.setBackground(Color.WHITE);
            changeListener.saveState(text_teasertext);
            notifyChanges();
        }
}//GEN-LAST:event_bu_teasertext_updateActionPerformed

    private void bu_remove_languageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_remove_languageActionPerformed
        int sel = list_language.getSelectedIndex();
        Item item = getSelectedItem();
        if (item==null) return;
        if (sel>=0) {
            String lang = (String)list_language.getModel().getElementAt(sel);
            item.getInformation().removePromotext(lang);
            item.getInformation().removeTeasertext(lang);
            updateLanguageList();
            updatePromoTexts(null);
            notifyChanges();
        }
}//GEN-LAST:event_bu_remove_languageActionPerformed
private void  updateLanguageList() {
        DefaultListModel lm = (DefaultListModel)list_language.getModel();
        lm.removeAllElements();
        Item item = getSelectedItem();
        if (item !=null) {
            BundleInformation info = item.getInformation();
            int anzP = info.getPromotextCount();
            int anzT = info.getPromotextCount();
            Vector<String> lang = new Vector<String>();
            for (int i=0;i<anzP;i++) {
                lang.add(info.getPromotextLanguage(i));
            }
            for (int i=0;i<anzT;i++) {
                String l = info.getTeasertextLanguage(i);
                if (!lang.contains(l)) {
                    lang.add(l);
                }
            }
            for (String s : lang) {
                if (s!=null && s.length()>0) {
                    lm.addElement(s);
                }
            }
        }
    }

    private void updatePromoTexts(String lang) {
        Item item = getSelectedItem();
        if (lang==null || item==null) {
           text_promotext.setText("");
           text_teasertext.setText("");
        } else {
           text_promotext.setText(item.getInformation().getPromotext(lang));
           text_teasertext.setText(item.getInformation().getTeasertext(lang));
        }
        changeListener.saveState(text_promotext);
        changeListener.saveState(text_teasertext);
    }
    private void bu_add_languageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_add_languageActionPerformed
        Item item = getSelectedItem();
        if (item != null) {
            String lang = FeedGui.showLanguageCodeSelector();
            if (lang!=null) {
                String p = item.getInformation().getPromotext(lang);
                String t = item.getInformation().getTeasertext(lang);
                if (t!=null || p !=null) {
                    Dialogs.showMessage("Selected language \""+lang+"\" is already in list.");
                    return;
                }
                item.getInformation().setPromotext(lang, "");
                updateLanguageList();
                list_language.setSelectedIndex(list_language.getModel().getSize()-1);
                notifyChanges();
            }
        }
}//GEN-LAST:event_bu_add_languageActionPerformed

    private void bu_teasertext_resetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_teasertext_resetActionPerformed
       text_teasertext.setText(changeListener.getSavedText(text_teasertext));
       text_teasertext.setBackground(Color.WHITE);
    }//GEN-LAST:event_bu_teasertext_resetActionPerformed

    private void bu_main_languageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_main_languageActionPerformed
       Item item = getSelectedItem();
       if (item==null) return;
       String lang = FeedGui.showLanguageCodeSelector();
       if (lang!=null) {
           if (item.getTags()==null) {
               item.tags(ItemTags.make());
           }
           item.getTags().main_language(lang);
           text_tags_main_language.setText(lang);
           notifyChanges();
       }
    }//GEN-LAST:event_bu_main_languageActionPerformed

    private void bu_select_countryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_select_countryActionPerformed
       Item item = getSelectedItem();
       if (item==null) return;
       String country = FeedGui.showCountryCodeSelector();
       if (country!=null) {
           if (item.getTags()==null) {
               item.tags(ItemTags.make());
           }
           item.getTags().origin_country(country);
           text_tags_origin_country.setText(country);
           notifyChanges();
       }
    }//GEN-LAST:event_bu_select_countryActionPerformed

    private Item getSelectedItem() {
        if (bundle==null || bundle.getItemsCount()==0) return null;
        int selItem = list_items.getSelectedIndex();
        if (selItem<0) return null;
        Item item = bundle.getItem(selItem);
        return item;
    }
    private ItemFile getSelectedFile() {
        Item item = getSelectedItem();
        if (item==null) return null;
        int sel = list_files.getSelectedIndex();
        if (sel<0 || sel>=item.getFilesCount()) return null;
        ItemFile file = item.getFile(sel);
        return file;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bu_add_contributor;
    private javax.swing.JButton bu_add_file;
    private javax.swing.JButton bu_add_item;
    private javax.swing.JButton bu_add_language;
    private javax.swing.JButton bu_change_file;
    private javax.swing.JButton bu_main_language;
    private javax.swing.JButton bu_promotext_reset;
    private javax.swing.JButton bu_promotext_update;
    private javax.swing.JButton bu_remove_contributor;
    private javax.swing.JButton bu_remove_file;
    private javax.swing.JButton bu_remove_item;
    private javax.swing.JButton bu_remove_language;
    private javax.swing.JButton bu_select_country;
    private javax.swing.JButton bu_teasertext_reset;
    private javax.swing.JButton bu_teasertext_update;
    private javax.swing.JCheckBox checkLicenseAsOnBundle;
    private javax.swing.JCheckBox check_tags_bundle_only;
    private javax.swing.JCheckBox check_tags_stream_allowed;
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
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JList list_all_contributors;
    private javax.swing.JList list_allowed_territories;
    private javax.swing.JList list_contributors;
    private javax.swing.JList list_disallowed_territories;
    private javax.swing.JList list_files;
    private javax.swing.JList list_genres;
    private javax.swing.JList list_items;
    private javax.swing.JList list_language;
    private javax.swing.JPanel panelBasics;
    private javax.swing.JPanel panelBasics1;
    private javax.swing.JPanel panelContributors;
    private javax.swing.JPanel panelIDs;
    private javax.swing.JPanel panelIDsBig;
    private javax.swing.JPanel panelInformation;
    private javax.swing.JPanel panelLicense;
    private javax.swing.JPanel panelLicense1;
    private javax.swing.JPanel panel_territories;
    private javax.swing.JComboBox select_file_channel;
    private javax.swing.JComboBox select_file_type;
    private javax.swing.JComboBox select_license_pricing;
    private javax.swing.JComboBox select_type;
    private javax.swing.JTabbedPane tab_items;
    private javax.swing.JTextField text_amazon;
    private javax.swing.JTextField text_contentauthid;
    private javax.swing.JTextField text_digital_release_datetime;
    private javax.swing.JTextField text_display_artist;
    private javax.swing.JTextField text_displayname;
    private javax.swing.JTextField text_file_format;
    private javax.swing.JTextField text_file_length;
    private javax.swing.JTextField text_file_md5;
    private javax.swing.JTextField text_file_path;
    private javax.swing.JTextField text_file_sha1;
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
    private javax.swing.JTextArea text_promotext;
    private javax.swing.JTextField text_tags_main_language;
    private javax.swing.JTextField text_tags_origin_country;
    private javax.swing.JTextArea text_teasertext;
    private javax.swing.JTextField text_upc;
    private javax.swing.JTextField text_version;
    private javax.swing.JTextField text_yourid;
    // End of variables declaration//GEN-END:variables


}
