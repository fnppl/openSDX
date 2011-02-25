package org.fnppl.opensdx.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

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
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setSize(1024, 768);
	}
	
	public void makeMenuBar() {
		ActionListener ja = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String cmd = e.getActionCommand();
				
				if(cmd.equalsIgnoreCase("quit")) {
					System.exit(0);
				}
			}
		};
		
		JMenuBar jb = new JMenuBar();
		JMenu jm = new JMenu("File");
		JMenuItem jmi = null;
		
		jmi = new JMenuItem("OpenKeyStore");
		jmi.setActionCommand("openkeystore");
		jmi.addActionListener(ja);
		
		jmi = new JMenuItem("WriteKeyStore");
		jmi.setActionCommand("writekeystore");
		jmi.addActionListener(ja);
		
		
		jmi = new JMenuItem("Quit");
		jmi.setActionCommand("quit");
		jmi.addActionListener(ja);
		
		
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

	private static final MouseListener consumeMouseListener 
	= new MouseAdapter(){
		@Override
		public void mousePressed(MouseEvent e){ e.consume(); }
		@Override
		public void mouseReleased(MouseEvent e){ e.consume(); }
		@Override
		public void mouseClicked(MouseEvent e){ e.consume(); }
	};

	public void lockUI(){
		JRootPane r = getRootPane();
		Component l = r.getGlassPane();

		l.addMouseListener(consumeMouseListener);
		l.setVisible(true);
		l.setEnabled(true);

		l.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		//		MainFrame.getInstance().setEnabled(false);    	
	}
	public void releaseUILock() {
		JRootPane r = getRootPane();
		Component l = r.getGlassPane();

		l.removeMouseListener(consumeMouseListener);
		l.setVisible(false);
		l.setEnabled(false);

		//		MainFrame.getInstance().setEnabled(true);
		l.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	
	public static void main(String[] args) {
		SecurityMainFrame s = SecurityMainFrame.getInstance();
		s.buildUi();
		s.setVisible(true);
	}
}

