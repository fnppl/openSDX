package org.fnppl.opensdx.gui.helper;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import org.fnppl.opensdx.common.ActionHttp;
import org.fnppl.opensdx.common.ActionMailTo;
import org.fnppl.opensdx.common.Bundle;
import org.fnppl.opensdx.common.BusinessObject;
import org.fnppl.opensdx.common.ContractPartner;
import org.fnppl.opensdx.common.Contributor;
import org.fnppl.opensdx.common.Feed;
import org.fnppl.opensdx.common.IDs;
import org.fnppl.opensdx.common.ItemTags;
import org.fnppl.opensdx.common.LicenseBasis;
import org.fnppl.opensdx.common.Receiver;
import org.fnppl.opensdx.common.Territorial;
import org.fnppl.opensdx.dmi.FeedGui;
import org.fnppl.opensdx.gui.Dialogs;
import org.fnppl.opensdx.gui.EditCheckBoxTree;
import org.fnppl.opensdx.gui.EditTerritoiresTree;
import org.fnppl.opensdx.gui.PanelActionHTTP;
import org.fnppl.opensdx.gui.PanelActionMailTo;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;
import org.fnppl.opensdx.xml.XMLElementable;

import sun.awt.DefaultMouseInfoPeer;

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

public class PanelSavedDMI extends JPanel {

	private File dataDirectory = null;
	private Vector<DMIObject> objects;
	
	private JLabel labelDataDir;
	private JTextField textDataDir;
	private JButton buSelectDataDir;
	private JButton buReadData;
	private JPanel panelNorth;
	private JSplitPane split;
	private JPanel panelDetails;
	private JPanel panelButtons;
	private JScrollPane scrollTable;
	private JTable table;
	private DefaultTableModel table_model;
	private String[] table_header = new String[] {"type","file","description"};
	private String[][] table_data = new String[0][3];
	
	private FeedGui gui;
	
	public PanelSavedDMI(FeedGui gui) {
		this.gui = gui;
		
		dataDirectory = new File(System.getProperty("user.home")+"/openSDX/dmi_data");
		
		readData();
		initComponents();
		initLayout();
		
	}
	
	public void readData() {
		objects = new Vector<DMIObject>();
		if (textDataDir!=null) {
			dataDirectory = new File(textDataDir.getText());
		}
		File[] list = dataDirectory.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.toLowerCase().endsWith(".xml")) {
					return true;
				}
				return false;
			}
		});
		if (list!=null) {
			for (File f: list) {
				try {
					Element root = Document.fromFile(f).getRootElement();
					String name = root.getName();
					if (name.equals("collection")) {
						Vector<Element> ve = root.getChildren();
						if (ve!=null) {
							for (Element e : ve) {
								addObject(e,f);
							}
						}
					} else {
						addObject(root,f);
					}
					BusinessObject.fromElement(root);
				} catch (Exception ex) {
					System.out.println("Error reading from file: "+f.getAbsolutePath());
				}
			}
		}
	}
	
	private void addObject(Element e, File f) {
		String name = e.getName();
		BusinessObject bo = BusinessObject.fromElement(e);
		if (name.equals("sender")) {
			ContractPartner v = ContractPartner.fromBusinessObject(bo, ContractPartner.ROLE_SENDER);
			if (v!=null) {
				v.role(ContractPartner.ROLE_CONTRACT_PARTNER);
				DMIObject o = new DMIObject();
				o.type = "Contract Partner";
				o.description = v.getEmail();
				o.fromFile = f;
				o.object = v;
				objects.add(o);
			}
		}
		else if (name.equals("licensor")) {
			ContractPartner v = ContractPartner.fromBusinessObject(bo, ContractPartner.ROLE_LICENSOR);
			if (v!=null) {			
				v.role(ContractPartner.ROLE_CONTRACT_PARTNER);
				DMIObject o = new DMIObject();
				o.type = "Contract Partner";
				o.description = v.getEmail();
				o.fromFile = f;
				o.object = v;
				objects.add(o);
			}
		}
		else if (name.equals("receiver")) {
			Receiver v = Receiver.fromBusinessObject(bo);
			if (v!=null) {
				DMIObject o = new DMIObject();
				o.type = "Receiver";
				o.description = v.getType()+":: "+v.getServername();
				o.fromFile = f;
				o.object = v;
				objects.add(o);
			}
		}
		else if (name.equals("contributor")) {
			Contributor v = Contributor.fromBusinessObject(bo);
			if (v!=null) {
				DMIObject o = new DMIObject();
				o.type = "Contributor";
				o.description = v.getName()+" :: "+v.getType();
				o.fromFile = f;
				o.object = v;
				objects.add(o);
			}
		}
		else if (name.equals("ids")) {
			IDs v = IDs.fromBusinessObject(bo);
			if (v!=null) {
				DMIObject o = new DMIObject();
				o.type = "IDs";
				o.description = "";
				o.fromFile = f;
				o.object = v;
				objects.add(o);
			}
		}
		else if (name.equals("territorial")) {
			Territorial v = Territorial.fromBusinessObject(bo);
			if (v!=null) {
				DMIObject o = new DMIObject();
				o.type = "Territorial";
				o.description = "";
				o.fromFile = f;
				o.object = v;
				objects.add(o);
			}
		}
		else if (name.equals("creator")) {
			DMIObject o = new DMIObject();
			o.type = "Creator";
			
			String email = bo.getStringIfExist("email");
			o.description = "";
			if (email!=null) {
				o.description = email;
			}
			o.fromFile = f;
			o.object = bo;
			objects.add(o);
		}
		else if (name.equals("http")) {
			ActionHttp v = ActionHttp.fromBusinessObject(bo);
			if (v!=null) {
				DMIObject o = new DMIObject();
				o.type = "HTTP Action";
				o.description = v.getUrl();
				o.fromFile = f;
				o.object = v;
				objects.add(o);
			}
		}
		else if (name.equals("mailto")) {
			ActionMailTo v = ActionMailTo.fromBusinessObject(bo);
			if (v!=null) {
				DMIObject o = new DMIObject();
				o.type = "MailTo Action";
				o.description = v.getReceiver()+" :: "+v.getSubject();
				o.fromFile = f;
				o.object = v;
				objects.add(o);
			}
		}
		else if (name.equals("genres")) {
			DMIObject o = new DMIObject();
			o.type = "Genre Set";
			o.description = "";
			o.fromFile = f;
			o.object = bo;
			objects.add(o);
		}
		
	}
	
	private void updateTableModel() {
		int count = objects.size();
		table_data = new String[count][3];
		for (int i=0;i<count;i++) {
			DMIObject o = objects.get(i);
			table_data[i][0] = o.type;
			table_data[i][1] = o.fromFile.getName();
			table_data[i][2] = o.description;
		}
		table_model = new DefaultTableModel(table_data, table_header);
		table.setModel(table_model);
		table.setRowSorter(new TableRowSorter<DefaultTableModel>(table_model));
		panelDetails.removeAll();
		panelButtons.removeAll();
		this.validate();
		this.repaint();
		this.paint(this.getGraphics());
	}
	
	private void table_selection_changed(int index) {
		if (index<0 || index >= objects.size()) {
			return;
		}
		final DMIObject o = objects.get(index);
		//System.out.println("Selection changed: "+index+" :: "+o.type+", "+o.description+", "+o.fromFile.getName());
		panelDetails.removeAll();
		panelButtons.removeAll();
		
		if (o.type.equals("Receiver")) {	
			if (o.panel ==null) {
				o.panel = new PanelReceiver((Receiver)o.object);
				JButton bu = new JButton("set as Receiver in FeedInfo");
				bu.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							//clone
							Receiver r = Receiver.fromBusinessObject(BusinessObject.fromElement(((Receiver)o.object).toElement()));
							gui.getCurrentFeed().getFeedinfo().receiver(r);
							gui.update();
						} catch (Exception ex) {
							
						}
					}
				});
				o.buttons.add(bu);
			}
			panelDetails.add(o.panel);
			for (JButton bu : o.buttons) {
				panelButtons.add(bu);
			}
		}
		else if (o.type.equals("Contract Partner")) {
			if (o.panel ==null) {
				ContractPartner c = (ContractPartner)o.object;
				String[][] data = new String[][] {
						{"contract partner id",c.getContractPartnerID()},
						{"our contract partner id",c.getOurContractPartnerID()},
						{"email",c.getEmail()}
				};
				o.panel = buildPanel("Contract Partner", data);
				JButton bu = new JButton("set as Sender in FeedInfo");
				bu.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							//clone
							ContractPartner c = ContractPartner.fromBusinessObject(BusinessObject.fromElement(((ContractPartner)o.object).toElement()), ContractPartner.ROLE_CONTRACT_PARTNER);
							gui.getCurrentFeed().getFeedinfo().sender(c);
							gui.update();
						} catch (Exception ex) {
							
						}
					}
				});
				o.buttons.add(bu);
				bu = new JButton("set as Licensor in FeedInfo");
				bu.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							//clone
							ContractPartner c = ContractPartner.fromBusinessObject(BusinessObject.fromElement(((ContractPartner)o.object).toElement()), ContractPartner.ROLE_CONTRACT_PARTNER);
							gui.getCurrentFeed().getFeedinfo().licensor(c);
							gui.update();
						} catch (Exception ex) {
							
						}
					}
				});
				o.buttons.add(bu);
			}
			panelDetails.add(o.panel);
			for (JButton bu : o.buttons) {
				panelButtons.add(bu);
			}
		}
		else if (o.type.equals("IDs")) {
			if (o.panel ==null) {
				Vector<Element> eids = ((IDs)o.object).toElement().getChildren();
				String[][] data = new String[eids.size()][2];
				for (int i=0;i<eids.size();i++) {
					Element e = eids.get(i);
					data[i][0] = e.getName();
					data[i][1] = e.getText();
				}
				o.panel = buildPanel("IDs", data);
				JButton bu = new JButton("set as Bundle id's");
				bu.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							//clone
							IDs c = IDs.fromBusinessObject(BusinessObject.fromElement(((IDs)o.object).toElement()));
							gui.getCurrentFeed().getBundle(0).ids(c);
							gui.update();
						} catch (Exception ex) {
							
						}
					}
				});
				o.buttons.add(bu);
			}
			panelDetails.add(o.panel);
			for (JButton bu : o.buttons) {
				panelButtons.add(bu);
			}
		}
		else if (o.type.equals("HTTP Action")) {	
			if (o.panel ==null) {
				final PanelActionHTTP p = new PanelActionHTTP();
				p.setActionHTTP((ActionHttp)o.object);
				o.panel = new JPanel();
				o.panel.setLayout(new BorderLayout());
				o.panel.add(p,BorderLayout.WEST);
				JButton bu = new JButton("add to Actions in FeedInfo");
				bu.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							gui.getCurrentFeed().getFeedinfo().addAction(p.getTrigger(), p.getActionHTTP());
							gui.update();
						} catch (Exception ex) {
							
						}
					}
				});
				o.buttons.add(bu);
			}
			panelDetails.add(o.panel);
			for (JButton bu : o.buttons) {
				panelButtons.add(bu);
			}
		}
		else if (o.type.equals("MailTo Action")) {	
			if (o.panel ==null) {
				final PanelActionMailTo p = new PanelActionMailTo();
				p.setActionMailTo((ActionMailTo)o.object);
				o.panel = new JPanel();
				o.panel.setLayout(new BorderLayout());
				o.panel.add(p,BorderLayout.WEST);
				JButton bu = new JButton("add to Actions in FeedInfo");
				bu.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							gui.getCurrentFeed().getFeedinfo().addAction(p.getTrigger(), p.getActionMailTo());
							gui.update();
						} catch (Exception ex) {
							
						}
					}
				});
				o.buttons.add(bu);
			}
			panelDetails.add(o.panel);
			for (JButton bu : o.buttons) {
				panelButtons.add(bu);
			}
		}
		else if (o.type.equals("Creator")) {
			if (o.panel ==null) {
				String[][] data = new String[][] {
						{"email",o.object.getStringIfExist("email")},
						{"user id",o.object.getStringIfExist("userid")}
				};
				o.panel = buildPanel("Creator", data);
				JButton bu = new JButton("set as creator");
				bu.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							gui.getCurrentFeed().getFeedinfo().creator_email(o.object.getStringIfExist("email"));
							gui.getCurrentFeed().getFeedinfo().creator_userid(o.object.getStringIfExist("userid"));
							gui.update();
						} catch (Exception ex) {
							
						}
					}
				});
				o.buttons.add(bu);
			}
			panelDetails.add(o.panel);
			for (JButton bu : o.buttons) {
				panelButtons.add(bu);
			}			
		}
		else if (o.type.equals("Contributor")) {
			if (o.panel ==null) {
				Contributor c = (Contributor)o.object;
				String[][] data = new String[][] {
						{"name",c.getName()},
						{"type",c.getType()}
				};
				o.panel = buildPanel("Contributor", data);
				JButton bu = new JButton("add to Bundle Contributors");
				bu.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							//clone
							Contributor c = Contributor.fromBusinessObject(BusinessObject.fromElement(((Contributor)o.object).toElement()));
							gui.getCurrentFeed().getBundle(0).addContributor(c);
							gui.update();
						} catch (Exception ex) {
							
						}
					}
				});
				o.buttons.add(bu);
			}
			panelDetails.add(o.panel);
			for (JButton bu : o.buttons) {
				panelButtons.add(bu);
			}			
		}
		else if (o.type.equals("Genre Set")) {
			if (o.panel ==null) {
				
				JPanel panel_genres = new JPanel();
				final EditCheckBoxTree tree_genres = new EditCheckBoxTree(FeedGui.getGenres());
		        panel_genres.setLayout(new BorderLayout());
		        panel_genres.add(new JScrollPane(tree_genres), BorderLayout.CENTER);
		        Dimension dim = new Dimension(300, 400);
		        panel_genres.setPreferredSize(dim);
		        panel_genres.setMinimumSize(dim);
		        panel_genres.setMaximumSize(dim);
		        
		        Vector<String> select = new Vector<String>();
		        Vector<Element> genres = o.object.toElement().getChildren("genre");
		        for (Element g : genres) {
		        	select.add(g.getText());
		        }
		        tree_genres.setSelectedNodes(select);
				
				o.panel = panel_genres;
				JButton bu = new JButton("set as Bundle Genres");
				bu.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							ItemTags tags = gui.getCurrentFeed().getBundle(0).getTags();
							if (tags == null) {
								tags = ItemTags.make();
								gui.getCurrentFeed().getBundle(0).tags(tags);
							}
							tags.removeAllGenres();
							Vector<String> select = tree_genres.getSelectedNodes();
							for (String g : select) {
								tags.addGenre(g);
							}
							gui.update();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				});
				o.buttons.add(bu);
			}
			panelDetails.add(o.panel);
			for (JButton bu : o.buttons) {
				panelButtons.add(bu);
			}			
		}
		else if (o.type.equals("Territorial")) {
			if (o.panel ==null) {
				final EditTerritoiresTree tree = new EditTerritoiresTree();
				Territorial t = (Territorial)o.object;
				Document.buildDocument(t.toElement()).output(System.out);
				System.out.println("count: "+t.getTerritorialCount());
				tree.setTerritories(t);
				
				
				JPanel panel_territorial = new JPanel();
				panel_territorial.setLayout(new BorderLayout());
		        panel_territorial.add(new JScrollPane(tree), BorderLayout.CENTER);
		        Dimension dim = new Dimension(300, 400);
		        panel_territorial.setPreferredSize(dim);
		        panel_territorial.setMinimumSize(dim);
		        panel_territorial.setMaximumSize(dim);
				o.panel = panel_territorial;
				JButton bu = new JButton("set as Bundle Territories");
				bu.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							LicenseBasis license = gui.getCurrentFeed().getBundle(0).getLicense_basis();
							if (license == null) {
								license = LicenseBasis.make(tree.getTerritorial(), System.currentTimeMillis(), System.currentTimeMillis());
								gui.getCurrentFeed().getBundle(0).license_basis(license);
							} else {
								license.setTerritorial(tree.getTerritorial());	
							}
							gui.update();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				});
				o.buttons.add(bu);
			}
			panelDetails.add(o.panel);
			for (JButton bu : o.buttons) {
				panelButtons.add(bu);
			}			
		}
		
		this.validate();
		this.repaint();
		this.paint(this.getGraphics());
		//panelDetails.repaint();
		//panelButtons.repaint();
	}
	
	
	private JPanel buildPanel(String title, String[][] data) {
		JPanel p = new JPanel();
		p.setBorder(new TitledBorder(title));
		
		int count = data.length;
		
		GridBagLayout gbl = new GridBagLayout();
		p.setLayout(gbl);
		GridBagConstraints gbc = new GridBagConstraints();

		for (int i=0;i<count;i++) {
			// Component: label
			JLabel l = new JLabel(data[i][0]);
			gbc.gridx = 0;
			gbc.gridy = i;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.CENTER;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.ipadx = 0;
			gbc.ipady = 0;
			gbc.insets = new Insets(5,5,5,5);
			gbl.setConstraints(l,gbc);
			p.add(l);
			
			// Component: text
			JTextField t = new JTextField(data[i][1]);
			t.setEditable(false);
			gbc.gridx = 1;
			gbc.gridy = i;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 1.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.CENTER;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.ipadx = 0;
			gbc.ipady = 0;
			gbc.insets = new Insets(5,5,5,5);
			gbl.setConstraints(t,gbc);
			p.add(t);
		}
		JLabel filler =  new JLabel();
		gbc.gridx = 0;
		gbc.gridy = count;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(filler,gbc);
		p.add(filler);
		return p;
	}
	
	public void initComponents() {
		
		panelNorth = new JPanel();
		labelDataDir = new JLabel("data path");
		textDataDir = new JTextField(dataDirectory.getAbsolutePath());
		buSelectDataDir = new JButton("select");
		buSelectDataDir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File f = Dialogs.chooseOpenDirectory("Select directory for data import", dataDirectory.getParentFile(), dataDirectory.getName());
				if (f!=null) {
					textDataDir.setText(f.getAbsolutePath());
				}
			}
		});
		
		buReadData = new JButton("read data");	
		buReadData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				readData();
				updateTableModel();
			}
		});
		
		panelDetails = new JPanel();
		panelDetails.setLayout(new BorderLayout());
		
		panelButtons = new JPanel();
		FlowLayout fl = new FlowLayout();
		fl.setAlignment(FlowLayout.LEFT);
		panelButtons.setLayout(fl);
		
		table = new JTable();
		updateTableModel();
		scrollTable = new JScrollPane(table);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int row = table.getSelectedRow();
					if (row>=0 && row<table_model.getRowCount()) {
						int index = table.getRowSorter().convertRowIndexToModel(row);
						table_selection_changed(index);
					}
				}
			}
		});
		split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollTable, new JScrollPane(panelDetails));
	}
	
	public void initLayout() {
		
		GridBagLayout gbl = new GridBagLayout();
		panelNorth.setLayout(gbl);
		GridBagConstraints gbc = new GridBagConstraints();

		// Component: label
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(labelDataDir,gbc);
		panelNorth.add(labelDataDir);

		// Component: text
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(textDataDir,gbc);
		panelNorth.add(textDataDir);
		
		// Component: bu select
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(buSelectDataDir,gbc);
		panelNorth.add(buSelectDataDir);
		
		// Component: bu read
		gbc.gridx = 3;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(buReadData,gbc);
		panelNorth.add(buReadData);
		
		Dimension d = new Dimension(120, 28);
		buSelectDataDir.setPreferredSize(d);
		buReadData.setPreferredSize(d);
		split.setDividerLocation(250);
		
		this.setLayout(new BorderLayout());
		this.add(panelNorth, BorderLayout.NORTH);
		this.add(split, BorderLayout.CENTER);
		this.add(panelButtons, BorderLayout.SOUTH);
	}
	
	private class DMIObject {
		public String type = "";
		public String description = "";
		public File fromFile = null;
		public BusinessObject object;
		public JPanel panel = null;
		public Vector<JButton> buttons = new Vector<JButton>();
	}
}
