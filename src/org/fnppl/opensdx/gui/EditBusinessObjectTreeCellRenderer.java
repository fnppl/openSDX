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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.MutableTreeNode;

import org.fnppl.opensdx.common.BusinessBooleanItem;
import org.fnppl.opensdx.common.BusinessBytesItem;
import org.fnppl.opensdx.common.BusinessCollection;
import org.fnppl.opensdx.common.BusinessDatetimeItem;
import org.fnppl.opensdx.common.BusinessIntegerItem;
import org.fnppl.opensdx.common.BusinessObject;
import org.fnppl.opensdx.common.BusinessStringItem;
import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.xml.XMLElementable;


public class EditBusinessObjectTreeCellRenderer extends DefaultTreeCellRenderer {

		protected EditBusinessObjectTree treeview;
		
		public EditBusinessObjectTreeCellRenderer(EditBusinessObjectTree tree) {
			super();
			treeview = tree;
		}
		
		public Component getTreeCellRendererComponent(JTree tree, Object obValue, boolean sel, boolean expanded, boolean leaf, final int row, boolean hasFocus) {
	    	
			int sizeY = 25;
	    	int sizeYp = sizeY+2;
	    	int sizeX = 0;
	    	
	    	String labelname = null;
	    	XMLElementable value = ((MyTreeNode)obValue).xml;
	    	
	    	if (value instanceof BusinessObject) {
		    	BusinessObject bo = (BusinessObject)value;
		    	int sizeX1 = 200;
		    	JLabel lab  = new JLabel(bo.getKeyname());
		    	JPanel p = new JPanel();
		    	FlowLayout layout = new FlowLayout();
		    	layout.setVgap(1);
		    	layout.setHgap(10);
		    	layout.setAlignment(FlowLayout.LEFT);
		    	p.setLayout(layout);
		    	
		    	p.setAlignmentX(JPanel.LEFT_ALIGNMENT);
	    		p.setAlignmentY(JPanel.CENTER_ALIGNMENT);
	    		
	    		sizeX += sizeX1+10;
	    		lab.setPreferredSize(new Dimension(sizeX1,sizeY));
	    		p.add(lab);
	    		return p;
	    	}
	    	
	    	if (value instanceof BusinessCollection) {
	    		BusinessCollection bo = (BusinessCollection)value;
		    	int sizeX1 = 200;
		    	JLabel lab  = new JLabel(bo.getKeyname());
		    	JPanel p = new JPanel();
		    	FlowLayout layout = new FlowLayout();
		    	layout.setVgap(1);
		    	layout.setHgap(10);
		    	layout.setAlignment(FlowLayout.LEFT);
		    	p.setLayout(layout);
		    	
		    	p.setAlignmentX(JPanel.LEFT_ALIGNMENT);
	    		p.setAlignmentY(JPanel.CENTER_ALIGNMENT);
	    		
	    		sizeX += sizeX1+10;
	    		lab.setPreferredSize(new Dimension(sizeX1,sizeY));
	    		p.add(lab);
	    		return p;
	    	}
	    	
	    	if (value instanceof BusinessStringItem) {
	    		final BusinessStringItem s = (BusinessStringItem)value;
	    		int sizeX1 = 200;
	    		final String origValue = s.getString();
	    		
	    		String lname = s.getKeyname();
	    		Vector<String[]> atts = s.getAttributes();
	    		if (atts!=null && atts.size()>0) {
	    			for (String[] att : atts) {
	    				lname +=  ", "+att[0]+"="+att[1];
	    			}
	    		}
	    		JLabel lab  = new JLabel(lname);
	    		lab.setPreferredSize(new Dimension(sizeX1,sizeY));
	    		
	    		JPanel p = new JPanel();
		    	FlowLayout layout = new FlowLayout();
		    	layout.setVgap(1);
		    	layout.setHgap(10);
		    	layout.setAlignment(FlowLayout.LEFT);
		    	p.setLayout(layout);
		    	
		    	p.setAlignmentX(JPanel.LEFT_ALIGNMENT);
	    		p.setAlignmentY(JPanel.CENTER_ALIGNMENT);
	    		
	    		sizeX += sizeX1+10;
	    		p.add(lab);
	    		
	    		if (sel) {
	    			p.setBackground(Color.lightGray);
				} else {
	    			p.setBackground(new Color(230,230,230));
	    		}
	    		
    			final JTextField text = new JTextField(s.getString());
	    		int sizeX2 = 350;
    			text.setPreferredSize(new Dimension(sizeX2,sizeY));
	    		text.setFocusable(true);
	    		text.setEditable(false); //TODO
	    		
	    		sizeX += sizeX2+20;
	    		p.add(text);
	    		
	    		p.setPreferredSize(new Dimension(sizeX,sizeY));
	    		
	    		DocumentListener chListen = new DocumentListener() {
    				public void removeUpdate(DocumentEvent e) {action();}
    				public void insertUpdate(DocumentEvent e) {action();}
    				public void changedUpdate(DocumentEvent e) {action();}
    				private void action() {
    					
    					if (origValue!=null && !text.getText().equals(origValue)) {
    						text.setBackground(Color.YELLOW);
    					} else {
    						text.setBackground(Color.WHITE);
    					}
    					
    				}
    			};
    			text.getDocument().addDocumentListener(chListen);
    			text.addKeyListener(new KeyAdapter() {
	                public void keyPressed(KeyEvent e) {
	                    if(e.getKeyCode() == KeyEvent.VK_ENTER){
	                    	s.setString(text.getText());
	                    	text.setBackground(Color.WHITE);
	                    }
	                }
	            });
    			
    			return p;
	    	}
	    	
	    	if (value instanceof BusinessIntegerItem) {
	    		final BusinessIntegerItem s = (BusinessIntegerItem)value;
	    		int sizeX1 = 200;
	    		final String origValue = ""+s.getIntValue();
	    		
	    		String lname = s.getKeyname();
	    		Vector<String[]> atts = s.getAttributes();
	    		if (atts!=null && atts.size()>0) {
	    			for (String[] att : atts) {
	    				lname +=  ", "+att[0]+"="+att[1];
	    			}
	    		}
	    		JLabel lab  = new JLabel(lname);
	    		lab.setPreferredSize(new Dimension(sizeX1,sizeY));
	    		
	    		JPanel p = new JPanel();
		    	FlowLayout layout = new FlowLayout();
		    	layout.setVgap(1);
		    	layout.setHgap(10);
		    	layout.setAlignment(FlowLayout.LEFT);
		    	p.setLayout(layout);
		    	
		    	p.setAlignmentX(JPanel.LEFT_ALIGNMENT);
	    		p.setAlignmentY(JPanel.CENTER_ALIGNMENT);
	    		
	    		sizeX += sizeX1+10;
	    		p.add(lab);
	    		
	    		if (sel) {
	    			p.setBackground(Color.lightGray);
				} else {
	    			p.setBackground(new Color(230,230,230));
	    		}
	    		
    			final JTextField text = new JTextField(""+s.getIntValue());
	    		int sizeX2 = 350;
    			text.setPreferredSize(new Dimension(sizeX2,sizeY));
	    		text.setFocusable(true);
	    		text.setEditable(false); //TODO
	    		
	    		sizeX += sizeX2+20;
	    		p.add(text);
	    		
	    		p.setPreferredSize(new Dimension(sizeX,sizeY));
	    		
	    		DocumentListener chListen = new DocumentListener() {
    				public void removeUpdate(DocumentEvent e) {action();}
    				public void insertUpdate(DocumentEvent e) {action();}
    				public void changedUpdate(DocumentEvent e) {action();}
    				private void action() {
    					
    					if (origValue!=null && !text.getText().equals(origValue)) {
    						text.setBackground(Color.YELLOW);
    					} else {
    						text.setBackground(Color.WHITE);
    					}
    					
    				}
    			};
    			text.getDocument().addDocumentListener(chListen);
    			text.addKeyListener(new KeyAdapter() {
	                public void keyPressed(KeyEvent e) {
	                    if(e.getKeyCode() == KeyEvent.VK_ENTER){
	                    	try {
		                    	s.setInteger(Integer.parseInt(text.getText()));
		                    	text.setBackground(Color.WHITE);
	                    	} catch (Exception ex) {
	                    		text.setBackground(Color.RED);
	                    	}
	                    }
	                }
	            });
    			
    			return p;
	    	}
	    	
	    	if (value instanceof BusinessBytesItem) {
	    		final BusinessBytesItem s = (BusinessBytesItem)value;
	    		int sizeX1 = 200;
	    		final String origValue = s.getString();
	    		
	    		String lname = s.getKeyname();
	    		Vector<String[]> atts = s.getAttributes();
	    		if (atts!=null && atts.size()>0) {
	    			for (String[] att : atts) {
	    				lname +=  ", "+att[0]+"="+att[1];
	    			}
	    		}
	    		JLabel lab  = new JLabel(lname);
	    		lab.setPreferredSize(new Dimension(sizeX1,sizeY));
	    		
	    		JPanel p = new JPanel();
		    	FlowLayout layout = new FlowLayout();
		    	layout.setVgap(1);
		    	layout.setHgap(10);
		    	layout.setAlignment(FlowLayout.LEFT);
		    	p.setLayout(layout);
		    	
		    	p.setAlignmentX(JPanel.LEFT_ALIGNMENT);
	    		p.setAlignmentY(JPanel.CENTER_ALIGNMENT);
	    		
	    		sizeX += sizeX1+10;
	    		p.add(lab);
	    		
	    		if (sel) {
	    			p.setBackground(Color.lightGray);
				} else {
	    			p.setBackground(new Color(230,230,230));
	    		}
	    		
    			final JTextField text = new JTextField(s.getString());
	    		int sizeX2 = 350;
    			text.setPreferredSize(new Dimension(sizeX2,sizeY));
	    		text.setFocusable(true);
	    		text.setEditable(false); //TODO
	    		
	    		sizeX += sizeX2+20;
	    		p.add(text);
	    		
	    		p.setPreferredSize(new Dimension(sizeX,sizeY));
	    		
	    		DocumentListener chListen = new DocumentListener() {
    				public void removeUpdate(DocumentEvent e) {action();}
    				public void insertUpdate(DocumentEvent e) {action();}
    				public void changedUpdate(DocumentEvent e) {action();}
    				private void action() {
    					
    					if (origValue!=null && !text.getText().equals(origValue)) {
    						text.setBackground(Color.YELLOW);
    					} else {
    						text.setBackground(Color.WHITE);
    					}
    					
    				}
    			};
    			text.getDocument().addDocumentListener(chListen);
    			text.addKeyListener(new KeyAdapter() {
	                public void keyPressed(KeyEvent e) {
	                    if(e.getKeyCode() == KeyEvent.VK_ENTER){
	                    	try {
		                    	s.setBytes(SecurityHelper.HexDecoder.decode(text.getText()));
		                    	text.setBackground(Color.WHITE);
	                    	} catch (Exception ex) {
	                    		text.setBackground(Color.RED);
	                    	}
	                    }
	                }
	            });
    			
    			return p;
	    	}
	    	
	    	if (value instanceof BusinessBooleanItem) {
	    		final BusinessBooleanItem item = (BusinessBooleanItem)value;
	    		int sizeX1 = 400;
	    		
	    		JPanel p = new JPanel();
		    	FlowLayout layout = new FlowLayout();
		    	layout.setVgap(1);
		    	layout.setHgap(10);
		    	layout.setAlignment(FlowLayout.LEFT);
		    	p.setLayout(layout);
		    	
		    	p.setAlignmentX(JPanel.LEFT_ALIGNMENT);
	    		p.setAlignmentY(JPanel.CENTER_ALIGNMENT);
	    		
	    		sizeX += sizeX1+10;
	    		JCheckBox cb = new JCheckBox(item.getKeyname());
	    		cb.setSelected(item.getBoolean());
	    		cb.setEnabled(false); //TODO
	    		p.add(cb);
	    		
	    		if (sel) {
	    			p.setBackground(Color.lightGray);
				} else {
	    			p.setBackground(new Color(230,230,230));
	    		}
	    		p.setPreferredSize(new Dimension(sizeX,sizeY));
    			return p;
	    	}
	    	
	    	if (value instanceof BusinessDatetimeItem) {
	    		final BusinessDatetimeItem s = (BusinessDatetimeItem)value;
	    		int sizeX1 = 200;
	    		final String origValue = s.getDatetimeStringGMT();
	    		
	    		
	    		JLabel lab  = new JLabel(s.getKeyname());
	    		lab.setPreferredSize(new Dimension(sizeX1,sizeY));
	    		
	    		JPanel p = new JPanel();
		    	FlowLayout layout = new FlowLayout();
		    	layout.setVgap(1);
		    	layout.setHgap(10);
		    	layout.setAlignment(FlowLayout.LEFT);
		    	p.setLayout(layout);
		    	
		    	p.setAlignmentX(JPanel.LEFT_ALIGNMENT);
	    		p.setAlignmentY(JPanel.CENTER_ALIGNMENT);
	    		
	    		sizeX += sizeX1+20;
	    		p.add(lab);
	    		
	    		if (sel) {
	    			p.setBackground(Color.lightGray);
				} else {
	    			p.setBackground(new Color(230,230,230));
	    		}
	    		
    			final JTextField text = new JTextField(s.getDatetimeStringGMT());
	    		int sizeX2 = 350;
    			text.setPreferredSize(new Dimension(sizeX2,sizeY));
	    		text.setFocusable(true);
	    		text.setEditable(false); //TODO
	    		
	    		sizeX += sizeX2+10;
	    		p.add(text);
	    		
	    		p.setPreferredSize(new Dimension(sizeX,sizeY));
	    		
	    		DocumentListener chListen = new DocumentListener() {
    				public void removeUpdate(DocumentEvent e) {action();}
    				public void insertUpdate(DocumentEvent e) {action();}
    				public void changedUpdate(DocumentEvent e) {action();}
    				private void action() {
    					
    					if (origValue!=null && !text.getText().equals(origValue)) {
    						text.setBackground(Color.YELLOW);
    					} else {
    						text.setBackground(Color.WHITE);
    					}
    					
    				}
    			};
    			text.getDocument().addDocumentListener(chListen);
    			text.addKeyListener(new KeyAdapter() {
	                public void keyPressed(KeyEvent e) {
	                    if(e.getKeyCode() == KeyEvent.VK_ENTER){
	                    	try {
								s.setDatetime(SecurityHelper.parseDate(text.getText()));
							} catch (Exception e1) {
								System.out.println("ERROR PARSING DATETIME");
							}
	                    	text.setBackground(Color.WHITE);
	                    }
	                }
	            });
    			
    			return p;
	    	}
	    	
	    	String text =  "UNKNOWN FORMAT";
	    	
	    	if (value instanceof XMLElementable) {
	    		text =  "UNKNOWN FORMAT "+((XMLElementable)value).getKeyname();
	    	}
	    	int sizeX1 = 700;
	    	JLabel lab  = new JLabel(text);
	    	JPanel p = new JPanel();
	    	FlowLayout layout = new FlowLayout();
	    	layout.setVgap(1);
	    	layout.setHgap(10);
	    	layout.setAlignment(FlowLayout.LEFT);
	    	p.setLayout(layout);
	    	
	    	p.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    		p.setAlignmentY(JPanel.CENTER_ALIGNMENT);
    		
    		sizeX += sizeX1+10;
    		lab.setPreferredSize(new Dimension(sizeX1,sizeY));
    		p.add(lab);
    		return p;
	    }
	}

