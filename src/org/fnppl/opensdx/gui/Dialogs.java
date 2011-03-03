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


/**
 * @author Bertram Boedeker <bboedeker@gmx.de>
 * 
 */
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
