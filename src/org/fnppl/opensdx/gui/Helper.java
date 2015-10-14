package org.fnppl.opensdx.gui;

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

import java.awt.*;
import java.util.*;
import java.io.*;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JWindow;


/**
 * @author Henning Thieß <ht@fnppl.org>
 * 
 */

public class Helper {
	public static void centerMe(Container toCenter, Container relativeTo) {
    	Toolkit tk = Toolkit.getDefaultToolkit();
    	
        Dimension d = tk.getScreenSize();
        
        Dimension i = toCenter.getSize();
        
        int dwidth = d.width-i.width;
        int dheight = d.height-i.height;
        
        if(relativeTo == null) {        	
            //hier nur auf dem Screen zentrieren
            if(toCenter instanceof JDialog) { 
                JDialog jd = (JDialog)toCenter;
                jd.setLocationRelativeTo(null);
                jd.setLocation(dwidth/2,dheight/2);
            }
            else if(toCenter instanceof JFrame) { 
                JFrame jd = (JFrame)toCenter;
                jd.setLocationRelativeTo(null);
                jd.setLocation(dwidth/2,dheight/2);
            }
            else if(toCenter instanceof JWindow) { 
                JWindow jd = (JWindow)toCenter;
                jd.setLocationRelativeTo(null);
                jd.setLocation(dwidth/2,dheight/2);
            }
        }
        else if(relativeTo instanceof JDialog) {
            JDialog jf = (JDialog)relativeTo;
            
            d = jf.getSize();
            
            dwidth = d.width-i.width;
            dheight = d.height-i.height;
            
            dwidth = dwidth/2;
            dheight = dheight/2;
            
            Point f = jf.getLocation();
            
            dwidth += f.getX();
            dheight += f.getY();
            
            if(toCenter instanceof JDialog) { 
                JDialog jd = (JDialog)toCenter;
                jd.setLocation(dwidth,dheight);
            }
            else if(toCenter instanceof JFrame) { 
                JFrame jd = (JFrame)toCenter;
                jd.setLocation(dwidth,dheight);
            }
            else if(toCenter instanceof JWindow) { 
                JWindow jd = (JWindow)toCenter;
                jd.setLocation(dwidth,dheight);
            }
        }
        else if(relativeTo instanceof JFrame) {
            JFrame jf = (JFrame)relativeTo;
            
            d = jf.getSize();
            
            dwidth = d.width-i.width;
            dheight = d.height-i.height;
            
            dwidth = dwidth/2;
            dheight = dheight/2;
            
            Point f = jf.getLocation();
            
            dwidth += f.getX();
            dheight += f.getY();
            
            if(toCenter instanceof JDialog) { 
                JDialog jd = (JDialog)toCenter;
                jd.setLocation(dwidth,dheight);
            }
            else if(toCenter instanceof JFrame) { 
                JFrame jd = (JFrame)toCenter;
                jd.setLocation(dwidth,dheight);
            }
            else if(toCenter instanceof JWindow) { 
                JWindow jd = (JWindow)toCenter;
                jd.setLocation(dwidth,dheight);
            }
        }
    }


}
