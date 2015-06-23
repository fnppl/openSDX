package org.fnppl.opensdx.gui.helper;

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

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.HashMap;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;


public class DocumentInstantChangeListener implements DocumentListener {

    private String formatDatetime = "";
    private TextChangeListener listener = null;
    private HashMap<Document, JTextComponent> texts = null;
    
    public Locale ml = new Locale("en", "DE");
    public static SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat datetimeformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz");

    
    public DocumentInstantChangeListener(TextChangeListener listener) {
        this.listener = listener;
        texts = new HashMap<Document, JTextComponent>();
    }
    
    public void addTextComponent(JTextComponent c) {
    	texts.put(c.getDocument(), c);
    	c.getDocument().addDocumentListener(this);
    }
    
    private void action(DocumentEvent e) {
    	JTextComponent t = texts.get(e.getDocument());
    	if (t!=null) {
    		String name = t.getName();
    		if (formatOK(name, t.getText())) {
    			if (listener!=null) {
    				listener.text_changed(t);
    			}
    			t.setBackground(Color.WHITE);
    		} else {
    			t.setBackground(Color.RED);
    		}
    	}
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
    
    public boolean formatOK(String name, String text) {
    	boolean formatOK = true;
    	if (name!=null && text!=null) {
	    	if (name.contains("bytes")) {
	    		try {
	    			//SecurityHelper.HexDecoder.decode(t.getText());
	    		} catch (Exception ex) {
	    			formatOK = false;
	    		}
	    	} else if (name.contains("datetime")) {
	    		try {
	    			datetimeformat.parse(text);
	    		} catch (Exception ex) {
	    			formatOK = false;
	    		}
	    	} else if (name.contains("date")) {
	    		try {
	    			dateformat.parse(text);
	    		} catch (Exception ex) {
	    			formatOK = false;
	    		}
	    	} else if (name.contains("integer") && text.length()>0) {
	    		try {
	    			Integer.parseInt(text);
	    		} catch (Exception ex) {
	    			formatOK = false;
	    		}
	    	}
    	}
    	return formatOK;
    }
   
}
