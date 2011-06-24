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

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.fnppl.opensdx.common.BundleInformation;

public class PromotextTableModel implements TableModel {

    private BundleInformation info;
    private MyObservable notify;
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

    public PromotextTableModel(BundleInformation info, MyObservable notify) {
        this.info = info;
        this.notify = notify;
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
                info.addPromotext((String)getValueAt(rowIndex, 0), (String)aValue);
            } else {
                info.promotext(row, (String)aValue);
            }
        }
        else if(columnIndex == 2) {
             int row = index.get(rowIndex)[1];
            if (row==-1) {
                info.addTeasertext((String)getValueAt(rowIndex, 0), (String)aValue);
            } else {
               info.teasertext(row, (String)aValue);
            }
        }
        else if(columnIndex == 0) {
            info.promotext_language(index.get(rowIndex)[0], (String)aValue);
            info.teasertext_language(index.get(rowIndex)[1], (String)aValue);
        }
        org.fnppl.opensdx.xml.Document.buildDocument(info.toElement()).output(System.out);
        notify.notifyChanges();
    }

    public void addTableModelListener(TableModelListener l) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeTableModelListener(TableModelListener l) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
}
