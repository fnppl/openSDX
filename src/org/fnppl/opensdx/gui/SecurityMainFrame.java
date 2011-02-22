package org.fnppl.opensdx.gui;

import java.awt.*;
import javax.swing.*;

/*
 * Copyright (C) 2010-2011 
 * 							fine people e.V. <opensdx@fnppl.org> 
 * 							Henning Thieß <ht@fnppl.org>
 * 
 * 							http://fnppl.org
 * 
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


public class SecurityMainFrame extends JFrame {
	private static SecurityMainFrame instance = null;
	public static SecurityMainFrame getInstance() {
		if(instance == null) {
			instance = new SecurityMainFrame();
		}
		return instance;
	}
	private SecurityMainFrame() {
		super("fnppl.org :: openSDX :: SecurityMainFrame");
		
		setSize(1024, 768);
	}
	
	public void makeMenuBar() {
		JMenuBar jb = new JMenuBar();
		setJMenuBar(jb);
	}
	private void buildUi() {
		makeMenuBar();
		
		JPanel jp = new JPanel();
		setContentPane(jp);
		GridBagLayout gb = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();		
		jp.setLayout(gb);
	}
	
	public static void main(String[] args) {
		SecurityMainFrame s = SecurityMainFrame.getInstance();
		s.buildUi();
		s.setVisible(true);
	}
}

