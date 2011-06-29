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
import java.awt.Dimension;
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
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;
import org.fnppl.opensdx.common.*;
import org.fnppl.opensdx.dmi.FeedGui;
import org.fnppl.opensdx.security.KeyApprovingStore;
import org.fnppl.opensdx.security.OSDXKey;
import org.fnppl.opensdx.security.SecurityHelper;

public class PanelFeedInfo extends javax.swing.JPanel implements MyObservable {

    private FeedInfo feedinfo = null;
    private DocumentChangeListener changeListener;
    private PanelFeedInfo me;
    private File lastDir = null;

    private Vector<MyObserver> observers = new Vector<MyObserver>();
    public void addObserver(MyObserver observer) {
    	observers.add(observer);
    }

//    private void updateToFeed() {
//        if (feed!=null) {
//            long creation_datetime = -1L;
//            long effective_datetime = -1L;
//            try {
//               creation_datetime = SecurityHelper.parseDate(text_creation_datetime.getText());
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//            try {
//               effective_datetime = SecurityHelper.parseDate(text_effictive_datetime.getText());
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//            ContractPartner sender = ContractPartner.make(ContractPartner.ROLE_SENDER, text_sender_contractpartnerid.getText(), text_sender_ourcontractpartnerid.getText());
//            sender.email(text_sender_email.getText());
//
//            ContractPartner licensor = ContractPartner.make(ContractPartner.ROLE_LICENSOR, text_licensor_contractpartnerid.getText(), text_licensor_ourcontractpartnerid.getText());
//            licensor.email(text_licensor_email.getText());
//
//            FeedInfo fi = FeedInfo.make(check_onlytest.isSelected(), text_feedid.getText(), creation_datetime, effective_datetime, sender, licensor);
//            fi.creator(text_creator_email.getText(), text_creator_userid.getText());
//
//            byte[] authsha1 = null;
//            try {
//                authsha1 = SecurityHelper.HexDecoder.decode(text_authsha1.getText());
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//            fi.receiver(Receiver.make(select_receiver_type.getSelectedItem().toString(), text_receiver_servername.getText(),text_receiver_serveripv4.getText(), select_authtype.getSelectedItem().toString(), authsha1));
//
//            feed.setFeedInfo(fi);
//            for (MyObserver ob : observers) {
//                ob.notifyChange();
//            }
//        }
//    }

    public void update(FeedInfo fi) {
        if (fi==null) {
            long now = System.currentTimeMillis();
            fi = FeedInfo.make(true, "",now, now, ContractPartner.make(ContractPartner.ROLE_SENDER, "", ""), ContractPartner.make(ContractPartner.ROLE_LICENSOR, "", ""));
        }
        this.feedinfo = fi;
        check_onlytest.setSelected(fi.getOnlyTest());
        text_feedid.setText(fi.getFeedID());
        text_creation_datetime.setText(fi.getCreationDatetimeString());
        text_effictive_datetime.setText(fi.getEffectiveDatetimeString());

        text_creator_userid.setText(fi.getCreatorUserID());
        text_creator_email.setText(fi.getCreatorEmail());

        text_sender_contractpartnerid.setText(fi.getSender().getContractPartnerID());
        text_sender_ourcontractpartnerid.setText(fi.getSender().getOurContractPartnerID());
        text_sender_email.setText(fi.getSender().getEmail());

        text_licensor_contractpartnerid.setText(fi.getLicensor().getContractPartnerID());
        text_licensor_ourcontractpartnerid.setText(fi.getLicensor().getOurContractPartnerID());
        text_licensor_email.setText(fi.getLicensor().getEmail());

        Receiver r = fi.getReceiver();
        if (r!=null) {
            select_receiver_type.setSelectedItem(r.getType());
            text_receiver_servername.setText(r.getServername());
            text_receiver_serveripv4.setText(r.getServerIPv4());
            select_authtype.setSelectedItem(r.getAuthType());
            text_keyid.setText(r.getKeyID());
            text_keystore.setText(r.getFileKeystore());
        } else {
            select_receiver_type.setSelectedIndex(0);
            text_receiver_servername.setText("");
            text_receiver_serveripv4.setText("");
            select_authtype.setSelectedIndex(0);
            text_keyid.setText("");
            text_keystore.setText("");
        }

        //actions
        updateActionsTable(fi);

        changeListener.saveStates();

        Dimension maxSize= new Dimension(440, 32767);
        jPanel1.setMaximumSize(maxSize);
        jPanel2.setMaximumSize(maxSize);
        jPanel3.setMaximumSize(maxSize);
        jPanel4.setMaximumSize(maxSize);
        jPanel5.setMaximumSize(maxSize);
        jPanel6.setMaximumSize(maxSize);
        
    }

    private void updateActionsTable(FeedInfo fi) {
        int count = fi.getActionCount();
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

    private void newEmpty() {
        check_onlytest.setSelected(true);
        text_feedid.setText("");
        text_creation_datetime.setText("");
        text_effictive_datetime.setText("");

        text_creator_userid.setText("");
        text_creator_email.setText("");

        text_sender_contractpartnerid.setText("");
        text_sender_ourcontractpartnerid.setText("");
        text_sender_email.setText("");

        text_licensor_contractpartnerid.setText("");
        text_licensor_ourcontractpartnerid.setText("");
        text_licensor_email.setText("");

        select_receiver_type.setSelectedIndex(0);
        text_receiver_servername.setText("");
        text_receiver_serveripv4.setText("");
        select_authtype.setSelectedIndex(0);

    }


    /** Creates new form PanelFeedInfo */
    public PanelFeedInfo() {
        super();
        me = this;
        initComponents();
        text_creation_datetime.setName("datetime");
        text_effictive_datetime.setName("datetime");
        lastDir = new File(System.getProperty("user.home"));
        File f = new File(lastDir,"openSDX");
        if (f.exists() && f.isDirectory()) {
            lastDir = f;
        }
        initChangeListeners();
    }

    private void initChangeListeners() {
        Vector<JTextComponent> texts = new Vector<JTextComponent>();
        texts.add(text_feedid);
        texts.add(text_creation_datetime);
        texts.add(text_effictive_datetime);

        texts.add(text_creator_userid);
        texts.add(text_creator_email);

        texts.add(text_sender_contractpartnerid);
        texts.add(text_sender_ourcontractpartnerid);
        texts.add(text_sender_email);

        texts.add(text_licensor_contractpartnerid);
        texts.add(text_licensor_ourcontractpartnerid);
        texts.add(text_licensor_email);

        texts.add(text_receiver_servername);
        texts.add(text_receiver_serveripv4);
        
        changeListener = new DocumentChangeListener(texts);

         KeyAdapter keyAdapt = new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER){
                    if (e.getComponent() instanceof JTextField) {
                        JTextComponent text = (JTextComponent)e.getComponent();
                        String t = text.getText();
                        if (t.equals("")) t = null;

                        try {
                            if (text == text_feedid) feedinfo.feedid(t);
                            else if(text == text_creation_datetime) feedinfo.creation_datetime(SecurityHelper.parseDate(t));
                            else if(text == text_effictive_datetime) feedinfo.effective_datetime(SecurityHelper.parseDate(t));

                            else if(text == text_sender_contractpartnerid) feedinfo.getSender().contractpartnerid(t);
                            else if(text == text_sender_ourcontractpartnerid) feedinfo.getSender().ourcontractpartnerid(t);
                            else if(text == text_sender_email) feedinfo.getSender().email(t);

                            else if(text == text_licensor_contractpartnerid) feedinfo.getLicensor().contractpartnerid(t);
                            else if(text == text_licensor_ourcontractpartnerid) feedinfo.getLicensor().ourcontractpartnerid(t);
                            else if(text == text_licensor_email) feedinfo.getLicensor().email(t);

                            else if(text == text_receiver_servername) feedinfo.getReceiver().servername(t);
                            else if(text == text_receiver_serveripv4) feedinfo.getReceiver().serveripv4(t);
                            
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
            text.addKeyListener(keyAdapt);
        }

    }

     public void notifyChanges() {
        for (MyObserver ob : observers) {
            ob.notifyChange(this);
        }
    }

//    private void updateStatusColors() {
//        if (feed==null) return;
//            FeedInfo fi = feed.getFeedinfo();
//            if (fi==null) return;
//            for (String[] b : bindings) {
//                try {
//                    Field field = me.getClass().getDeclaredField(b[0]);
//                    field.setAccessible(true);
//                    Object ob = field.get(me);
//                    Method getter = null;
//                    Object getOb = null;
//                    if (b[1].contains(".")) {
//                      String[] t = b[1].split("[.]");
//                      getOb = fi;
//                      for (int i=0;i<t.length;i++) {
//                          getter = getOb.getClass().getMethod(t[i]);
//                          getOb = getter.invoke(getOb);
//                       }
//                    } else {
//                        getter = fi.getClass().getMethod(b[1]);
//                        getOb = getter.invoke(fi);
//                    }
//                    String text = null;
//                    Color color = Color.WHITE;
//                    if (ob instanceof JTextField) {
//                        text = ((JTextField)ob).getText();
//                    } else if (ob instanceof JComboBox) {
//                        text = ((JComboBox)ob).getSelectedItem().toString();
//                    } else if (ob instanceof JCheckBox) {
//                        if (((JCheckBox)ob).isSelected() != ((Boolean)getOb).booleanValue()) {
//                            color = Color.YELLOW;
//                        }
//                    }
//                    if (text !=null && getOb instanceof String) {
//                        boolean wrongFormat = false;
//                        if (field.getName().contains("datetime")) {
//                          try {
//                              SecurityHelper.parseDate(text);
//                          }  catch (Exception ex) {
//                              wrongFormat = true;
//                          }
//                        }
//                        if (wrongFormat) {
//                           color = Color.RED;
//                        } else {
//                            if (text.equals((String)getOb)) {
//                               color= Color.WHITE;
//                            } else {
//                                color = Color.YELLOW;
//                            }
//                        }
//                    }
//                    if (ob instanceof JTextField) {
//                        ((JTextField)ob).setBackground(color);
//                    } else if (ob instanceof JComboBox) {
//                        ((JComboBox)ob).setBackground(color);
//                    } else if (ob instanceof JCheckBox) {
//                        ((JCheckBox)ob).setBackground(color);
//                    }
//                } catch (Exception ex) {
//                  ex.printStackTrace();
//                }
//        }
//    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        text_creator_email = new javax.swing.JTextField();
        text_creator_userid = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        text_licensor_contractpartnerid = new javax.swing.JTextField();
        text_licensor_ourcontractpartnerid = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        text_licensor_email = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        text_sender_contractpartnerid = new javax.swing.JTextField();
        text_sender_ourcontractpartnerid = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        text_sender_email = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        select_receiver_type = new javax.swing.JComboBox();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        text_receiver_servername = new javax.swing.JTextField();
        text_receiver_serveripv4 = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        select_authtype = new javax.swing.JComboBox();
        panel_osdxfileserver_settings = new javax.swing.JPanel();
        text_keystore = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        bu_select_keyid = new javax.swing.JButton();
        jLabel18 = new javax.swing.JLabel();
        text_keyid = new javax.swing.JTextField();
        bu_select_keystore = new javax.swing.JButton();
        bu_remove_keystore = new javax.swing.JButton();
        bu_remove_keyid = new javax.swing.JButton();
        jLabel15 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        check_onlytest = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        buNow = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        text_feedid = new javax.swing.JTextField();
        text_creation_datetime = new javax.swing.JTextField();
        text_effictive_datetime = new javax.swing.JTextField();
        bu_uuid = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        table_actions = new javax.swing.JTable();
        bu_action_remove = new javax.swing.JButton();
        bu_action_add_http = new javax.swing.JButton();
        bu_action_add_mail = new javax.swing.JButton();
        bu_action_edit = new javax.swing.JButton();

        setBorder(null);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Creator"));
        jPanel1.setMaximumSize(new java.awt.Dimension(440, 32767));
        jPanel1.setPreferredSize(new java.awt.Dimension(375, 113));

        jLabel4.setText("email");

        jLabel5.setText("user id");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5))
                .addGap(44, 44, 44)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(text_creator_email, javax.swing.GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
                    .addComponent(text_creator_userid, javax.swing.GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(text_creator_email, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(text_creator_userid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Licensor"));
        jPanel2.setMaximumSize(new java.awt.Dimension(440, 32767));
        jPanel2.setPreferredSize(new java.awt.Dimension(375, 147));

        jLabel6.setText("contract partner id");

        jLabel7.setText("our contract partner id");

        text_licensor_contractpartnerid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                text_licensor_contractpartneridActionPerformed(evt);
            }
        });

        jLabel8.setText("email");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jLabel8)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(text_licensor_email, javax.swing.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
                    .addComponent(text_licensor_ourcontractpartnerid, javax.swing.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
                    .addComponent(text_licensor_contractpartnerid, javax.swing.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(text_licensor_contractpartnerid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(text_licensor_ourcontractpartnerid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(text_licensor_email, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Sender"));
        jPanel3.setMaximumSize(new java.awt.Dimension(440, 32767));

        jLabel9.setText("contract partner id");

        jLabel10.setText("our contract partner id");

        text_sender_contractpartnerid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                text_sender_contractpartneridActionPerformed(evt);
            }
        });

        jLabel11.setText("email");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addComponent(jLabel11)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(text_sender_email, javax.swing.GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE)
                    .addComponent(text_sender_ourcontractpartnerid, javax.swing.GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE)
                    .addComponent(text_sender_contractpartnerid, javax.swing.GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(text_sender_contractpartnerid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(text_sender_ourcontractpartnerid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(text_sender_email, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Receiver"));
        jPanel4.setMaximumSize(new java.awt.Dimension(440, 32767));

        jLabel12.setText("  type");

        select_receiver_type.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "openSDX fileserver", "ftp", "sftp", "ftps", "webdav" }));
        select_receiver_type.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                select_receiver_typeActionPerformed(evt);
            }
        });

        jLabel13.setText("  servername");

        jLabel16.setText("  auth type");

        select_authtype.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "login", "keyfile", "token", "other" }));
        select_authtype.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                select_authtypeActionPerformed(evt);
            }
        });

        panel_osdxfileserver_settings.setBorder(javax.swing.BorderFactory.createTitledBorder("openSDX  Fileserver Settings"));

        text_keystore.setEditable(false);

        jLabel17.setText("KeyStore");

        bu_select_keyid.setText("select");
        bu_select_keyid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bu_select_keyidActionPerformed(evt);
            }
        });

        jLabel18.setText("Key ID");

        text_keyid.setEditable(false);

        bu_select_keystore.setText("select");
        bu_select_keystore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bu_select_keystoreActionPerformed(evt);
            }
        });

        bu_remove_keystore.setText("x");
        bu_remove_keystore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bu_remove_keystoreActionPerformed(evt);
            }
        });

        bu_remove_keyid.setText("x");
        bu_remove_keyid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bu_remove_keyidActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panel_osdxfileserver_settingsLayout = new javax.swing.GroupLayout(panel_osdxfileserver_settings);
        panel_osdxfileserver_settings.setLayout(panel_osdxfileserver_settingsLayout);
        panel_osdxfileserver_settingsLayout.setHorizontalGroup(
            panel_osdxfileserver_settingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel_osdxfileserver_settingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panel_osdxfileserver_settingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel17)
                    .addComponent(jLabel18))
                .addGap(25, 25, 25)
                .addGroup(panel_osdxfileserver_settingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(text_keyid)
                    .addComponent(text_keystore, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panel_osdxfileserver_settingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(bu_select_keystore, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(bu_select_keyid, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panel_osdxfileserver_settingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bu_remove_keystore)
                    .addComponent(bu_remove_keyid))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        panel_osdxfileserver_settingsLayout.setVerticalGroup(
            panel_osdxfileserver_settingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel_osdxfileserver_settingsLayout.createSequentialGroup()
                .addGroup(panel_osdxfileserver_settingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(text_keystore, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17)
                    .addComponent(bu_select_keystore)
                    .addComponent(bu_remove_keystore))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panel_osdxfileserver_settingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(text_keyid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bu_select_keyid)
                    .addComponent(bu_remove_keyid)))
        );

        jLabel15.setText("server IPv4");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                            .addComponent(jLabel12)
                            .addGap(64, 64, 64)
                            .addComponent(select_receiver_type, 0, 303, Short.MAX_VALUE))
                        .addGroup(jPanel4Layout.createSequentialGroup()
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel4Layout.createSequentialGroup()
                                    .addComponent(jLabel14)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jLabel15))
                                .addComponent(jLabel13)
                                .addComponent(jLabel16))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(select_authtype, javax.swing.GroupLayout.PREFERRED_SIZE, 303, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(text_receiver_serveripv4, javax.swing.GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE)
                                .addComponent(text_receiver_servername, javax.swing.GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE))))
                    .addComponent(panel_osdxfileserver_settings, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel4Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {select_authtype, select_receiver_type, text_receiver_serveripv4, text_receiver_servername});

        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(select_receiver_type, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(49, 49, 49)
                        .addComponent(jLabel14))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(text_receiver_servername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel13))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(text_receiver_serveripv4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel15))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(select_authtype, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16))
                .addGap(17, 17, 17)
                .addComponent(panel_osdxfileserver_settings, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel5.setMaximumSize(new java.awt.Dimension(440, 32767));

        check_onlytest.setText("onlytest");
        check_onlytest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                check_onlytestActionPerformed(evt);
            }
        });

        jLabel2.setText("creation datetime");

        jLabel3.setText("effective datetime");

        buNow.setText("now");
        buNow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buNowActionPerformed(evt);
            }
        });

        jLabel1.setText("feed id");

        bu_uuid.setText("random UUID");
        bu_uuid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bu_uuidActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel5Layout.createSequentialGroup()
                            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel1)
                                .addComponent(jLabel2)
                                .addComponent(jLabel3))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(text_feedid, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
                                .addGroup(jPanel5Layout.createSequentialGroup()
                                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(text_creation_datetime, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                                        .addComponent(text_effictive_datetime, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(buNow)))
                            .addGap(19, 19, 19))
                        .addGroup(jPanel5Layout.createSequentialGroup()
                            .addComponent(check_onlytest, javax.swing.GroupLayout.DEFAULT_SIZE, 285, Short.MAX_VALUE)
                            .addGap(153, 153, 153)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                        .addComponent(bu_uuid, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(98, 98, 98))))
        );

        jPanel5Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {text_creation_datetime, text_effictive_datetime});

        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(check_onlytest)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(text_feedid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bu_uuid, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(text_creation_datetime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(text_effictive_datetime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3)))
                    .addComponent(buNow)))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Actions"));
        jPanel6.setMaximumSize(new java.awt.Dimension(440, 32767));

        table_actions.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null}
            },
            new String [] {
                "Trigger", "Type", "Description"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(table_actions);

        bu_action_remove.setText("remove");
        bu_action_remove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bu_action_removeActionPerformed(evt);
            }
        });

        bu_action_add_http.setText("add HTTP Action");
        bu_action_add_http.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bu_action_add_httpActionPerformed(evt);
            }
        });

        bu_action_add_mail.setText("add Mail Action");
        bu_action_add_mail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bu_action_add_mailActionPerformed(evt);
            }
        });

        bu_action_edit.setText("edit");
        bu_action_edit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bu_action_editActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 452, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(bu_action_edit)
                        .addGap(5, 5, 5)
                        .addComponent(bu_action_remove)
                        .addGap(18, 18, 18)
                        .addComponent(bu_action_add_http)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(bu_action_add_mail)))
                .addContainerGap())
        );

        jPanel6Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {bu_action_add_http, bu_action_add_mail});

        jPanel6Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {bu_action_edit, bu_action_remove});

        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bu_action_remove)
                    .addComponent(bu_action_add_http)
                    .addComponent(bu_action_add_mail)
                    .addComponent(bu_action_edit))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 488, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jPanel1, jPanel2, jPanel6});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, 307, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jPanel2, jPanel3});

    }// </editor-fold>//GEN-END:initComponents

    private void check_onlytestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_check_onlytestActionPerformed
        if (feedinfo != null) {
            feedinfo.only_test(check_onlytest.isSelected());
            notifyChanges();
        }
    }//GEN-LAST:event_check_onlytestActionPerformed

    private void text_licensor_contractpartneridActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_text_licensor_contractpartneridActionPerformed
       
    }//GEN-LAST:event_text_licensor_contractpartneridActionPerformed

    private void text_sender_contractpartneridActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_text_sender_contractpartneridActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_text_sender_contractpartneridActionPerformed

    private void select_receiver_typeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_select_receiver_typeActionPerformed
        if (feedinfo != null && feedinfo.getReceiver()!=null) {
            feedinfo.getReceiver().type((String)select_receiver_type.getSelectedItem());

            String type = feedinfo.getReceiver().getType();
            String authType = feedinfo.getReceiver().getAuthType();
            if (type!=null && authType!=null && type.equals(Receiver.TRANSFER_TYPE_OSDX_FILESERVER)
                    &&  authType.equals(Receiver.AUTH_TYPE_KEYFILE)) {
                panel_osdxfileserver_settings.setVisible(true);
            } else {
                panel_osdxfileserver_settings.setVisible(false);
            }
            notifyChanges();
        }
    }//GEN-LAST:event_select_receiver_typeActionPerformed

    private void buNowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buNowActionPerformed
        long now = System.currentTimeMillis();
        String s = SecurityHelper.getFormattedDate(now);
        feedinfo.creation_datetime(now);
        feedinfo.effective_datetime(now);
        text_creation_datetime.setText(s);
        text_effictive_datetime.setText(s);
        changeListener.saveState(text_creation_datetime);
        changeListener.saveState(text_effictive_datetime);

    }//GEN-LAST:event_buNowActionPerformed

    private void select_authtypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_select_authtypeActionPerformed
        if (feedinfo != null && feedinfo.getReceiver()!=null) {
            String type = feedinfo.getReceiver().getType();
            String authType = (String)select_authtype.getSelectedItem();
            
            feedinfo.getReceiver().authtype(authType);
            
            if (type!=null && authType!=null && type.equals(Receiver.TRANSFER_TYPE_OSDX_FILESERVER)
                    &&  authType.equals(Receiver.AUTH_TYPE_KEYFILE)) {
                panel_osdxfileserver_settings.setVisible(true);
            } else {
                panel_osdxfileserver_settings.setVisible(false);
            }
            notifyChanges();
        }
    }//GEN-LAST:event_select_authtypeActionPerformed

    private void bu_action_removeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_action_removeActionPerformed
        if (feedinfo==null) return;
        int no = table_actions.getSelectedRow();
        if (no<0 || no>=feedinfo.getActionCount()) return;
        feedinfo.removeAction(no);
        updateActionsTable(feedinfo);
        notifyChanges();
    }//GEN-LAST:event_bu_action_removeActionPerformed

    private void bu_action_add_httpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_action_add_httpActionPerformed
        if (feedinfo!=null) {
            ActionHttp a = ActionHttp.make("", ActionHttp.TYPE_GET);
            Vector result = editActionHttpDialog(a, TriggeredActions.TRIGGER_ONFULLSUCCESS);
            if (result!=null)  {
                ActionHttp newAction = (ActionHttp)result.get(0);
                int trigger = ((Integer)result.get(1)).intValue();
                feedinfo.addAction(0, newAction);
                updateActionsTable(feedinfo);
                notifyChanges();
            }
        }
    }//GEN-LAST:event_bu_action_add_httpActionPerformed

    private void bu_action_editActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_action_editActionPerformed
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

    }//GEN-LAST:event_bu_action_editActionPerformed

    private void bu_action_add_mailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_action_add_mailActionPerformed
        if (feedinfo!=null) {
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
    }//GEN-LAST:event_bu_action_add_mailActionPerformed

    private void bu_uuidActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_uuidActionPerformed
        String uuid = UUID.randomUUID().toString();
        text_feedid.setText(uuid);
        changeListener.saveState(text_feedid);
        feedinfo.feedid(uuid);
        notifyChanges();
    }//GEN-LAST:event_bu_uuidActionPerformed

    private void bu_select_keystoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_select_keystoreActionPerformed
        if (feedinfo==null) return;
        File f = Dialogs.chooseOpenFile("Open KeyStore", lastDir, "keystore.xml");
        if (f==null) return;
        Receiver r = feedinfo.getReceiver();
        r.file_keystore(f.getAbsolutePath());
        text_keystore.setText(f.getAbsolutePath());
        notifyChanges();
    }//GEN-LAST:event_bu_select_keystoreActionPerformed

    private void bu_select_keyidActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_select_keyidActionPerformed
            if (feedinfo == null) {
                return;
            }
            Receiver r = feedinfo.getReceiver();
            String filenameKeystore = r.getFileKeystore();
            if (filenameKeystore == null) {
                Dialogs.showMessage("Please select a keystore file first.");
                return;
            }
            File f = new File(filenameKeystore);
            if (!f.exists()) {
                Dialogs.showMessage("Sorry. selected keystore file does not exist.");
                return;
            }
            try {
                KeyApprovingStore keystore = KeyApprovingStore.fromFile(f, new DefaultMessageHandler());
                OSDXKey key = FeedGui.selectPrivateSigningKey(keystore);
                if (key!=null) {
                    r.keyid(key.getKeyID());
                    text_keyid.setText(key.getKeyID());
                    notifyChanges();
                }
            } catch (Exception ex) {
                Dialogs.showMessage("Error opening keystore. Please select a valid keytore file.");
                ex.printStackTrace();
            }
    }//GEN-LAST:event_bu_select_keyidActionPerformed

    private void bu_remove_keyidActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_remove_keyidActionPerformed
        if (feedinfo==null) {
            return;
        }
        feedinfo.getReceiver().keyid(null);
        text_keyid.setText("");
        notifyChanges();
}//GEN-LAST:event_bu_remove_keyidActionPerformed

    private void bu_remove_keystoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_remove_keystoreActionPerformed
        if (feedinfo==null) {
            return;
        }
        feedinfo.getReceiver().file_keystore(null);
        feedinfo.getReceiver().keyid(null);
        text_keystore.setText("");
        text_keyid.setText("");
        notifyChanges();
    }//GEN-LAST:event_bu_remove_keystoreActionPerformed

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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buNow;
    private javax.swing.JButton bu_action_add_http;
    private javax.swing.JButton bu_action_add_mail;
    private javax.swing.JButton bu_action_edit;
    private javax.swing.JButton bu_action_remove;
    private javax.swing.JButton bu_remove_keyid;
    private javax.swing.JButton bu_remove_keystore;
    private javax.swing.JButton bu_select_keyid;
    private javax.swing.JButton bu_select_keystore;
    private javax.swing.JButton bu_uuid;
    private javax.swing.JCheckBox check_onlytest;
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
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel panel_osdxfileserver_settings;
    private javax.swing.JComboBox select_authtype;
    private javax.swing.JComboBox select_receiver_type;
    private javax.swing.JTable table_actions;
    private javax.swing.JTextField text_creation_datetime;
    private javax.swing.JTextField text_creator_email;
    private javax.swing.JTextField text_creator_userid;
    private javax.swing.JTextField text_effictive_datetime;
    private javax.swing.JTextField text_feedid;
    private javax.swing.JTextField text_keyid;
    private javax.swing.JTextField text_keystore;
    private javax.swing.JTextField text_licensor_contractpartnerid;
    private javax.swing.JTextField text_licensor_email;
    private javax.swing.JTextField text_licensor_ourcontractpartnerid;
    private javax.swing.JTextField text_receiver_serveripv4;
    private javax.swing.JTextField text_receiver_servername;
    private javax.swing.JTextField text_sender_contractpartnerid;
    private javax.swing.JTextField text_sender_email;
    private javax.swing.JTextField text_sender_ourcontractpartnerid;
    // End of variables declaration//GEN-END:variables

}
