package org.fnppl.opensdx.gui;
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
import java.awt.Color;
import java.util.Vector;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.fnppl.opensdx.security.SecurityHelper;

public class DocumentChangeListener implements DocumentListener {

    private Vector<JTextComponent> texts;
    private Vector<String> saves;

    public DocumentChangeListener(Vector<JTextComponent> texts) {
        this.texts = texts;

    }

    public void saveStates() {
        saves = new Vector<String>();
        for (JTextComponent text : texts) {
            saves.add(text.getText());
            text.setBackground(Color.WHITE);
        }
    }

    public void saveState(JTextComponent t) {
        int ind = texts.indexOf(t);
        if (ind<0 || saves == null) return;
        saves.set(ind, t.getText());
        t.setBackground(Color.WHITE);
    }

    public String getSavedText(JTextComponent t) {
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
            for (JTextComponent text : texts) {
                text.setBackground(Color.WHITE);
            }
        } else {
            for (int i = 0; i < texts.size(); i++) {
            	JTextComponent t = texts.get(i);
            	boolean formatOK = true;
            	String name = t.getName();
            	if (name!=null) {
	            	if (name.contains("bytes")) {
	            		try {
	            			SecurityHelper.HexDecoder.decode(t.getText());
	            		} catch (Exception ex) {
	            			formatOK = false;
	            		}
	            	} else if (name.contains("datetime")) {
	            		try {
	            			SecurityHelper.parseDate(t.getText());
	            		} catch (Exception ex) {
	            			formatOK = false;
	            		}
	            	}
            	}
            	if (formatOK) {
	                if (t.getText().equals(saves.get(i))) {
	                    t.setBackground(Color.WHITE);
	                } else {
	                    t.setBackground(Color.YELLOW);
	                }
            	} else {
            		t.setBackground(Color.RED);
            	}
            }
        }
    }
}
