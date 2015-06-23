package org.fnppl.opensdx.gui;

import java.util.Vector;
import javax.swing.DefaultListModel;
import org.fnppl.opensdx.common.ActionHttp;
import org.fnppl.opensdx.common.TriggeredActions;
import org.fnppl.opensdx.gui.helper.MyObservable;
import org.fnppl.opensdx.gui.helper.MyObserver;
import org.fnppl.opensdx.xml.Document;

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
public class PanelActionHTTP extends javax.swing.JPanel implements MyObservable {

    /** Creates new form PanelActionHTTP */
    public PanelActionHTTP() {
        initComponents();
        action = ActionHttp.make("", (String)select_type.getSelectedItem());
        setActionHTTP(action);
        setTrigger(TriggeredActions.TRIGGER_ONFULLSUCCESS);
    }

    private ActionHttp action = null;

    public void setActionHTTP(ActionHttp action) {
    	 this.action = action;
         text_url.setText(action.getUrl());
         select_type.setSelectedItem(action.getType());
         updateHeaderList();
         updateParamList();
         text_new_name.setText("");
         text_new_value.setText("");
    }

    private void updateHeaderList() {
        DefaultListModel lmh = new DefaultListModel();
         for (int i=0;i<action.getHeaderCount();i++) {
        	 String s = action.getHeaderName(i)+"="+action.getHeaderValue(i);
             lmh.addElement(s);
         }
         list_header.setModel(lmh);
    }

    private void updateParamList() {
         DefaultListModel lmp = new DefaultListModel();
         for (int i=0;i<action.getParamCount();i++) {
             lmp.addElement(action.getParamName(i)+"="+action.getParamValue(i));
         }
         list_param.setModel(lmp);
    }

    public ActionHttp getActionHTTP() {
        action = ActionHttp.make(text_url.getText(), (String)select_type.getSelectedItem());
        for (int i=0;i<list_header.getModel().getSize();i++) {
            String[] t = ((String)list_header.getModel().getElementAt(i)).split("=");
            action.addHeader(t[0],t[1]);
        }
        for (int i=0;i<list_param.getModel().getSize();i++) {
            String[] t = ((String)list_param.getModel().getElementAt(i)).split("=");
            action.addParam(t[0],t[1]);
        }
        return action;
    }

    public void setTrigger(int triggerID) {
        if(triggerID>=0 && triggerID<select_trigger.getModel().getSize()) {
            select_trigger.setSelectedIndex(triggerID);
        }
    }
    
    public int getTrigger() {
        return select_trigger.getSelectedIndex();
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        text_url = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        select_type = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        list_header = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        list_param = new javax.swing.JList();
        bu_remove_header = new javax.swing.JButton();
        bu_remove_param = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        text_new_name = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        text_new_value = new javax.swing.JTextField();
        bu_add_header = new javax.swing.JButton();
        bu_add_parameter = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        select_trigger = new javax.swing.JComboBox();

        setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder("HTTP Action")));

        jLabel1.setText("url");

        select_type.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "GET", "POST", "HEAD" }));
        select_type.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                select_typeActionPerformed(evt);
            }
        });

        jLabel2.setText("type");

        list_header.setBorder(javax.swing.BorderFactory.createTitledBorder("Header"));
        jScrollPane1.setViewportView(list_header);

        list_param.setBorder(javax.swing.BorderFactory.createTitledBorder("Parameter"));
        jScrollPane2.setViewportView(list_param);

        bu_remove_header.setText("remove");
        bu_remove_header.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bu_remove_headerActionPerformed(evt);
            }
        });

        bu_remove_param.setText("remove");
        bu_remove_param.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bu_remove_paramActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder("Add Header or Parameter")));

        jLabel3.setText("name");

        jLabel4.setText("value");

        bu_add_header.setText("add to header");
        bu_add_header.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bu_add_headerActionPerformed(evt);
            }
        });

        bu_add_parameter.setText("add to parameter");
        bu_add_parameter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bu_add_parameterActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(18, 18, 18)
                        .addComponent(text_new_name))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(bu_add_header)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(bu_add_parameter))
                            .addComponent(text_new_value, javax.swing.GroupLayout.PREFERRED_SIZE, 357, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {bu_add_header, bu_add_parameter});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(text_new_name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(text_new_value, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bu_add_header)
                    .addComponent(bu_add_parameter))
                .addContainerGap())
        );

        jLabel5.setText("Trigger");

        select_trigger.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "[no trigger]", "on initial receive", "on process start", "on process end", "on full success", "on error" }));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bu_remove_header)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(35, 35, 35)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bu_remove_param)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jLabel1)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 44, Short.MAX_VALUE))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jLabel5)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(32, 32, 32)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(select_type, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(text_url, javax.swing.GroupLayout.PREFERRED_SIZE, 407, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(select_trigger, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(select_trigger, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(text_url, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(select_type, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bu_remove_header))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bu_remove_param)))
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void select_typeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_select_typeActionPerformed
       action.type((String)select_type.getSelectedItem());
       notifyChanges();
    }//GEN-LAST:event_select_typeActionPerformed

    private void bu_add_headerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_add_headerActionPerformed
        String name = text_new_name.getText();
        String value = text_new_value.getText();
        if (name.length()==0 || value.length()==0) {
            Dialogs.showMessage("Name or Value field must not be empty.");
        } else {
            action.addHeader(name, value);
             updateHeaderList();
             text_new_name.setText("");
             text_new_value.setText("");
            notifyChanges();
        }
    }//GEN-LAST:event_bu_add_headerActionPerformed

    private void bu_add_parameterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_add_parameterActionPerformed
        String name = text_new_name.getText();
        String value = text_new_value.getText();
        if (name.length()==0 || value.length()==0) {
            Dialogs.showMessage("Name or Value field must not be empty.");
        } else {
            action.addParam(name, value);
             updateParamList();
             text_new_name.setText("");
             text_new_value.setText("");
            notifyChanges();
        }
    }//GEN-LAST:event_bu_add_parameterActionPerformed

    private void bu_remove_headerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_remove_headerActionPerformed
       int no = list_header.getSelectedIndex();
       if (no<0 || no >= action.getHeaderCount()) return;
       action.removeHeader(no);
       updateHeaderList();
       notifyChanges();
    }//GEN-LAST:event_bu_remove_headerActionPerformed

    private void bu_remove_paramActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bu_remove_paramActionPerformed
        int no = list_param.getSelectedIndex();
       if (no<0 || no >= action.getParamCount()) return;
       action.removeParam(no);
       updateParamList();
       notifyChanges();
    }//GEN-LAST:event_bu_remove_paramActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bu_add_header;
    private javax.swing.JButton bu_add_parameter;
    private javax.swing.JButton bu_remove_header;
    private javax.swing.JButton bu_remove_param;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JList list_header;
    private javax.swing.JList list_param;
    private javax.swing.JComboBox select_trigger;
    private javax.swing.JComboBox select_type;
    private javax.swing.JTextField text_new_name;
    private javax.swing.JTextField text_new_value;
    private javax.swing.JTextField text_url;
    // End of variables declaration//GEN-END:variables

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
