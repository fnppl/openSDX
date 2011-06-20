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
import java.util.Vector;
import org.fnppl.opensdx.common.*;
import org.fnppl.opensdx.security.SecurityHelper;

public class PanelFeedInfo extends javax.swing.JPanel {

    private Feed feed = null;

    private Vector<MyObserver> observers = new Vector<MyObserver>();
    public void addObserver(MyObserver observer) {
    	observers.add(observer);
    }

    private void updateToFeed() {
        if (feed!=null) {
            long creation_datetime = -1L;
            long effective_datetime = -1L;
            try {
               creation_datetime = SecurityHelper.parseDate(text_creation_datetime.getText());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
               effective_datetime = SecurityHelper.parseDate(text_effictive_datetime.getText());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            ContractPartner sender = ContractPartner.make(ContractPartner.ROLE_SENDER, text_sender_contractpartnerid.getText(), text_sender_ourcontractpartnerid.getText());
            sender.email(text_sender_email.getText());

            ContractPartner licensor = ContractPartner.make(ContractPartner.ROLE_LICENSOR, text_licensor_contractpartnerid.getText(), text_licensor_ourcontractpartnerid.getText());
            licensor.email(text_licensor_email.getText());

            FeedInfo fi = FeedInfo.make(check_onlytest.isSelected(), text_feedid.getText(), creation_datetime, effective_datetime, sender, licensor);
            fi.creator(text_creator_email.getText(), text_creator_userid.getText());

            byte[] authsha1 = null;
            try {
                authsha1 = SecurityHelper.HexDecoder.decode(text_authsha1.getText());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            fi.receiver(Receiver.make(select_receiver_type.getSelectedItem().toString(), text_receiver_servername.getText(),text_receiver_serveripv4.getText(), select_authtype.getSelectedItem().toString(), authsha1));

            feed.setFeedInfo(fi);
            for (MyObserver ob : observers) {
                ob.notifyChange();
            }
        }
    }

    public void update(Feed feed) {
        this.feed = feed;
        FeedInfo fi = feed.getFeedinfo();

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
            text_authsha1.setText(r.getAuthSha1Text());
        } else {
            select_receiver_type.setSelectedIndex(0);
            text_receiver_servername.setText("");
            text_receiver_serveripv4.setText("");
            select_authtype.setSelectedIndex(0);
            text_authsha1.setText("");
        }
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
        text_authsha1.setText("");
    }


    /** Creates new form PanelFeedInfo */
    public PanelFeedInfo() {
        initComponents();
    }

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
        jLabel15 = new javax.swing.JLabel();
        text_receiver_serveripv4 = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        select_authtype = new javax.swing.JComboBox();
        jLabel17 = new javax.swing.JLabel();
        text_authsha1 = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        check_onlytest = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        buNow = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        text_feedid = new javax.swing.JTextField();
        text_creation_datetime = new javax.swing.JTextField();
        text_effictive_datetime = new javax.swing.JTextField();
        buNew = new javax.swing.JButton();
        buSave = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Feedinfo"));
        setPreferredSize(new java.awt.Dimension(760, 457));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Creator"));
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
                    .addComponent(text_creator_email, javax.swing.GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE)
                    .addComponent(text_creator_userid, javax.swing.GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE))
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
                    .addComponent(text_licensor_email, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                    .addComponent(text_licensor_ourcontractpartnerid, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                    .addComponent(text_licensor_contractpartnerid, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE))
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
                    .addComponent(text_sender_email, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                    .addComponent(text_sender_ourcontractpartnerid, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                    .addComponent(text_sender_contractpartnerid, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE))
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

        jLabel12.setText("type");

        select_receiver_type.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "openSDX fileserver", "ftp", "sftp", "ftps", "webdav" }));
        select_receiver_type.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                select_receiver_typeActionPerformed(evt);
            }
        });

        jLabel13.setText("servername");

        jLabel15.setText("server IPv4");

        jLabel16.setText("auth type");

        select_authtype.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "login", "keyfile", "token", "other" }));

        jLabel17.setText("auth sha1");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel14)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel13)
                            .addComponent(jLabel12)
                            .addComponent(jLabel15)
                            .addComponent(jLabel16)
                            .addComponent(jLabel17))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(select_receiver_type, 0, 0, Short.MAX_VALUE)
                            .addComponent(text_receiver_servername, javax.swing.GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE)
                            .addComponent(text_receiver_serveripv4, javax.swing.GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE)
                            .addComponent(select_authtype, javax.swing.GroupLayout.Alignment.TRAILING, 0, 306, Short.MAX_VALUE)
                            .addComponent(text_authsha1, javax.swing.GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(jLabel14))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(select_receiver_type, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel12))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(text_receiver_servername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel13))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(text_receiver_serveripv4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel15))
                        .addGap(16, 16, 16)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel16)
                            .addComponent(select_authtype, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(text_authsha1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

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

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(jLabel3)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(text_feedid, javax.swing.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
                    .addComponent(text_creation_datetime, javax.swing.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
                    .addComponent(text_effictive_datetime, javax.swing.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(check_onlytest, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buNow, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(text_feedid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(check_onlytest))
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(text_creation_datetime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(text_effictive_datetime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(buNow)
                        .addGap(25, 25, 25))))
        );

        buNew.setText("New");
        buNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buNewActionPerformed(evt);
            }
        });

        buSave.setText("Save");
        buSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buSaveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(299, 299, 299)
                        .addComponent(buNew, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(buSave, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 437, Short.MAX_VALUE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 437, Short.MAX_VALUE))))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jPanel1, jPanel2, jPanel3});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(208, 208, 208)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(buSave)
                            .addComponent(buNew)))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(17, 17, 17))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jPanel2, jPanel3});

    }// </editor-fold>//GEN-END:initComponents

    private void check_onlytestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_check_onlytestActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_check_onlytestActionPerformed

    private void text_licensor_contractpartneridActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_text_licensor_contractpartneridActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_text_licensor_contractpartneridActionPerformed

    private void text_sender_contractpartneridActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_text_sender_contractpartneridActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_text_sender_contractpartneridActionPerformed

    private void select_receiver_typeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_select_receiver_typeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_select_receiver_typeActionPerformed

    private void buNowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buNowActionPerformed
        String s = SecurityHelper.getFormattedDate(System.currentTimeMillis());
        text_creation_datetime.setText(s);
        text_effictive_datetime.setText(s);
    }//GEN-LAST:event_buNowActionPerformed

    private void buSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buSaveActionPerformed
        updateToFeed();
    }//GEN-LAST:event_buSaveActionPerformed

    private void buNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buNewActionPerformed
       newEmpty();
    }//GEN-LAST:event_buNewActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buNew;
    private javax.swing.JButton buNow;
    private javax.swing.JButton buSave;
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
    private javax.swing.JComboBox select_authtype;
    private javax.swing.JComboBox select_receiver_type;
    private javax.swing.JTextField text_authsha1;
    private javax.swing.JTextField text_creation_datetime;
    private javax.swing.JTextField text_creator_email;
    private javax.swing.JTextField text_creator_userid;
    private javax.swing.JTextField text_effictive_datetime;
    private javax.swing.JTextField text_feedid;
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
