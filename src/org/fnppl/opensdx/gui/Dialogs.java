package org.fnppl.opensdx.gui;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

public class Dialogs {

	
	public static final String ShowPasswordDialog(String head, String message) {
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		if (message.contains("\n")) {
			message = "<HTML><BODY>"+message.replace("\n", "<BR>")+"</BODY><HTML>";
		}
		JLabel l = new JLabel(message);
		JPasswordField pf = new JPasswordField();
		pf.setEchoChar('*');
		
		p.add(l, BorderLayout.CENTER);
		p.add(pf, BorderLayout.SOUTH);
	    int ans = JOptionPane.showConfirmDialog(null,p,head,JOptionPane.OK_CANCEL_OPTION);
	    //JOptionPane.showMessageDialog(null,p,head,JOptionPane.OK_OPTION);
	    if (ans == JOptionPane.OK_OPTION && pf.getPassword()!=null) {
	    	return new String(pf.getPassword());
	    }
	    return null;
	}
}
