package org.fnppl.opensdx.gui.helper;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Vector;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;


public class DocumentChangeListener implements DocumentListener {

    private Vector<JTextComponent> texts;
    private Vector<String> saves;
    private String formatDatetime = "";
    
    public Locale ml = new Locale("en", "DE");
    public SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
    public SimpleDateFormat datetimeformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    
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
	    	}
    	}
    	return formatOK;
    }

    private void action(DocumentEvent e) {
        if (saves == null) {
            for (JTextComponent text : texts) {
                text.setBackground(Color.WHITE);
            }
        } else {
            for (int i = 0; i < texts.size(); i++) {
            	JTextComponent t = texts.get(i);
            	String name = t.getName();
            	if (formatOK(name, t.getText())) {
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
