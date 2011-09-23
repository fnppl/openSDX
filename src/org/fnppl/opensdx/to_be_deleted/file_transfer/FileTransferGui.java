package org.fnppl.opensdx.file_transfer;

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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import org.fnppl.opensdx.file_transfer.RemoteFile;
import org.fnppl.opensdx.file_transfer.RemoteFileSystem;
import org.fnppl.opensdx.file_transfer_new.gui.PanelAccount;
import org.fnppl.opensdx.file_transfer_new.gui.TreeAndTablePanel;
import org.fnppl.opensdx.gui.DefaultMessageHandler;
import org.fnppl.opensdx.gui.Dialogs;
import org.fnppl.opensdx.gui.Helper;
import org.fnppl.opensdx.gui.MessageHandler;
import org.fnppl.opensdx.gui.helper.MyObservable;
import org.fnppl.opensdx.gui.helper.MyObserver;
import org.fnppl.opensdx.security.KeyApprovingStore;
import org.fnppl.opensdx.security.OSDXKey;
import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;

public class FileTransferGui extends JFrame implements MyObserver {

	private Vector<FileTransferAccount> accounts = new Vector<FileTransferAccount>();
	private JPanel panelNorth;
	private JComboBox selectAccount;
	private JButton buConnect;
	private JButton buEdit;
	private JButton buRemove;
	private JButton buTest;
	

	private DefaultComboBoxModel selectAccount_model;
	private TreeAndTablePanelOSDXClient panelLocal;
	private RemoteFileSystem fsLocal;
	
	private JPanel panelRemote;
	private TreeAndTablePanelOSDXClient ttpanelRemote;
	private RemoteFileSystem fsRemote;
	
	private TableCellRenderer leftRenderer;
	private TableCellRenderer centerRenderer;
	private TableCellRenderer rightRenderer;
	
	private JPanel panelSouth;
	private JList log;
	private DefaultListModel log_model;
	private File userHome = null;

	public FileTransferGui() {
		initUserHome();
		initSettings();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});
		buildUi();
	}
	
	private void initUserHome() {
		//init user home
		userHome =  new File(System.getProperty("user.home"));
		File f = new File(userHome,"openSDX");
		if (f.exists() && f.isDirectory()) userHome = f;
		System.out.println("home directory: "+userHome.getAbsolutePath());
	}
	
	private void initSettings() {
		if (userHome == null) return;
		File f = new File(userHome,"file_transfer_settings.xml");
		if (!f.exists()) {
			System.out.println("Could not load settings from: "+f.getAbsolutePath());
			return;
		}
		try {
			Element root =  Document.fromFile(f).getRootElement();
			Vector<Element> eAccounts = root.getChildren("account");
			for (Element e : eAccounts) {
				try {
					FileTransferAccount a = new FileTransferAccount();
					a.type = e.getChildText("type");
					if (a.type.equals(a.TYPE_FTP)) {
						a.username = e.getChildText("username");
						a.host = e.getChildText("host");
						accounts.add(a);
					} else if (a.type.equals(a.TYPE_OSDXFILESERVER)) {
						a.username = e.getChildText("username");
						a.host = e.getChildText("host");
						a.port = Integer.parseInt(e.getChildTextNN("port"));
						a.prepath = e.getChildTextNN("prepath");
						a.keyid = e.getChildText("keyid");
						a.keystore_filename = e.getChildTextNN("keystore");
						accounts.add(a);
					} else {
						a.type += "[NOT SUPPORTED]";
						accounts.add(a);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void exit() {
		//TODO close open connections
		
		this.dispose();
	}

	private void updateAccounts() {
		selectAccount_model.removeAllElements();
		selectAccount_model.addElement("Create new account ...");
		selectAccount_model.addElement("[separator]");
		for (FileTransferAccount a : accounts) {
			if (a.type.equals(a.TYPE_FTP)) {
				selectAccount_model.addElement(a.type+" :: "+a.username+"@"+a.host);
			} 
			else if (a.type.equals(a.TYPE_OSDXFILESERVER)) {
				String keyidShort;
				try {
					keyidShort = a.keyid.substring(0,8)+" ... "+a.keyid.substring(51);
				} catch (Exception e) {
					keyidShort = a.keyid;
				}
				String name = a.type+" :: "+a.username+", "+keyidShort+", "+a.host;
				selectAccount_model.addElement(name);	
			}
			else {
				selectAccount_model.addElement(a.type+" :: "+a.username+"@"+a.host);
			}
		}
		selectAccount.setModel(selectAccount_model);
	}

	private void initComponents() {
		panelNorth = new JPanel();
		panelNorth.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		selectAccount_model = new DefaultComboBoxModel();
		selectAccount = new JComboBox();
		updateAccounts();
		selectAccount.setRenderer(new ListCellRenderer() {
			JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
			JLabel l = null;

			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				String str = (value == null) ? "" : value.toString();
				if (str.equals("[separator]")) {
					return separator;
				}
				if (l == null) {
					l = new JLabel();
					l.setOpaque(true);
					l.setBorder(new EmptyBorder(1, 1, 1, 1));
					l.setFont(list.getFont());
				}
				if (isSelected) {
					l.setBackground(list.getSelectionBackground());
					l.setForeground(list.getSelectionForeground());
				} else {
					l.setBackground(list.getBackground());
					l.setForeground(list.getForeground());
				}
				l.setText(str);
				return l;
			}
		});
		panelNorth.add(selectAccount);
		buConnect = new JButton("connect");
		buConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				button_connect_clicked();
			}
		});
		buEdit = new JButton("edit");
		buEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				button_edit_clicked();
			}
		});
		buRemove = new JButton("remove");
		buRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				button_remove_clicked();
			}
		});
		
		buTest = new JButton("test");
		buTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				button_test_clicked();
			}
		});
		panelNorth.add(buConnect);
		panelNorth.add(buEdit);
		panelNorth.add(buRemove);
		panelNorth.add(buTest);
		

		fsLocal = RemoteFileSystem.initLocalFileSystem();
		panelLocal = new TreeAndTablePanelOSDXClient(fsLocal,true);
		panelLocal.setPreferredColumnWidth(1, 20);
		panelLocal.setPreferredColumnWidth(2, 30);

		leftRenderer = new TableCellRenderer() {
			private JLabel label;
			
			public Component getTableCellRendererComponent(JTable table,Object value, boolean isSelected, boolean hasFocus,	int row, int column) {
				if (label==null) {
					label = new JLabel();
					label.setOpaque(true);
					label.setBorder(new EmptyBorder(1, 1, 1, 1));
					label.setFont(table.getFont());
					label.setHorizontalAlignment(SwingConstants.LEFT);
				}
				String str = (value == null) ? "" : value.toString();
				if (isSelected) {
					label.setBackground(table.getSelectionBackground());
					label.setForeground(table.getSelectionForeground());
				} else {
					label.setBackground(table.getBackground());
					label.setForeground(table.getForeground());
				}
				label.setText(str);
				return label;
			}
		};
		centerRenderer = new TableCellRenderer() {
			private JLabel label;
			
			public Component getTableCellRendererComponent(JTable table,Object value, boolean isSelected, boolean hasFocus,	int row, int column) {
				if (label==null) {
					label = new JLabel();
					label.setOpaque(true);
					label.setBorder(new EmptyBorder(1, 1, 1, 1));
					label.setFont(table.getFont());
					label.setHorizontalAlignment(SwingConstants.CENTER);
				}
				String str = (value == null) ? "" : value.toString();
				if (isSelected) {
					label.setBackground(table.getSelectionBackground());
					label.setForeground(table.getSelectionForeground());
				} else {
					label.setBackground(table.getBackground());
					label.setForeground(table.getForeground());
				}
				label.setText(str);
				return label;
			}
		};
		rightRenderer = new TableCellRenderer() {
			private JLabel label;
			
			public Component getTableCellRendererComponent(JTable table,Object value, boolean isSelected, boolean hasFocus,	int row, int column) {
				if (label==null) {
					label = new JLabel();
					label.setOpaque(true);
					label.setBorder(new EmptyBorder(1, 1, 1, 1));
					label.setFont(table.getFont());
					label.setHorizontalAlignment(SwingConstants.RIGHT);
				}
				String str = (value == null) ? "" : value.toString();
				if (isSelected) {
					label.setBackground(table.getSelectionBackground());
					label.setForeground(table.getSelectionForeground());
				} else {
					label.setBackground(table.getBackground());
					label.setForeground(table.getForeground());
				}
				label.setText(str);
				return label;
			}
		};
		panelLocal.setColumnRenderer(0, leftRenderer);
		panelLocal.setColumnRenderer(1, centerRenderer);		
		panelLocal.setColumnRenderer(2, rightRenderer);
		panelLocal.addObserver(this);
		
		panelRemote = new JPanel();
		panelRemote.setLayout(new BorderLayout());
		
		panelSouth = new JPanel();
		log = new JList();
		log_model = new DefaultListModel();
		log.setModel(log_model);

	}
	
	private void initLayout() {
		//panelNorth
//		GridBagLayout gbl = new GridBagLayout();
//		panelNorth.setLayout(gbl);
//		GridBagConstraints gbc = new GridBagConstraints();
//
//		// selectAccount
//		gbc.gridx = 0;
//		gbc.gridy = 0;
//		gbc.gridwidth = 1;
//		gbc.gridheight = 1;
//		gbc.weightx = 100.0;
//		gbc.weighty = 0.0;
//		gbc.anchor = GridBagConstraints.CENTER;
//		gbc.fill = GridBagConstraints.BOTH;
//		gbc.ipadx = 0;
//		gbc.ipady = 0;
//		gbc.insets = new Insets(2,2,2,2);
//		gbl.setConstraints(selectAccount, gbc);
//		panelNorth.add(selectAccount);
//		
//		//buttons
//		gbc.gridx = 1;
//		gbc.weightx = 0.0;
//		gbl.setConstraints(buConnect, gbc);
//		panelNorth.add(buConnect);
//		
//		gbc.gridx = 2;
//		gbl.setConstraints(buEdit, gbc);
//		panelNorth.add(buEdit);
//		
//		gbc.gridx = 3;
//		gbl.setConstraints(buRemove, gbc);
//		panelNorth.add(buRemove);
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(panelNorth, BorderLayout.NORTH);
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				panelLocal, panelRemote);
		split.setDividerLocation(500);
		getContentPane().add(split, BorderLayout.CENTER);

		panelSouth.setLayout(new BorderLayout());
		JScrollPane scrollLog = new JScrollPane(log);
		panelSouth.add(scrollLog, BorderLayout.CENTER);
		scrollLog.setPreferredSize(new Dimension(200, 100));
		getContentPane().add(panelSouth, BorderLayout.SOUTH);

	}

	private void buildUi() {
		setTitle("openSDX :: FileTransfer GUI");
		setSize(1024, 768);
		initComponents();
		initLayout();
		Helper.centerMe(this, null);
	}

	public static void main(String[] args) {
		try {
			UIManager
					.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception ex) {
			System.out.println("Nimbus look & feel not available");
		}
		FileTransferGui gui = new FileTransferGui();
		gui.setVisible(true);
	}

	private void button_connect_clicked() {
		if (buConnect.getText().equals("connect")) {
			System.out.println("connect");
			int sel = selectAccount.getSelectedIndex()-2;
			if (sel<0) {
				button_edit_clicked();
			}
			else if (sel>=0 && sel < accounts.size()) {
				FileTransferAccount a = accounts.get(sel);
				System.out.println("account: "+a.type+" :: "+a.username);
				if (a.type.equals(a.TYPE_FTP)) {
					char[] pw = Dialogs.showPasswordDialog("Enter Password","Please enter password for ftp account:\nhost: "+a.host+"\nusername: "+a.username);
					if (pw==null) {
						return;
					}
					fsRemote = RemoteFileSystem.initFTPFileSystem(a.host, a.username, pw.toString());
					if (!fsRemote.isConnected()) {
						try {
							fsRemote.connect();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					if (!fsRemote.isConnected()) {
						addStatus("ERROR, could not connect to "+a.username+"@"+a.host+".");
						Dialogs.showMessage("Sorry, could not connect to given account.");
						return;
					} else {
						addStatus("Connection to "+a.username+"@"+a.host+" established.");
					}
					ttpanelRemote = new TreeAndTablePanelOSDXClient(fsRemote,false);
					ttpanelRemote.addObserver(this);
					ttpanelRemote.setPreferredColumnWidth(1, 20);
					ttpanelRemote.setPreferredColumnWidth(2, 30);
					ttpanelRemote.setColumnRenderer(0, leftRenderer);
					ttpanelRemote.setColumnRenderer(1, centerRenderer);		
					ttpanelRemote.setColumnRenderer(2, rightRenderer);
					
					panelRemote.removeAll();
					panelRemote.add(ttpanelRemote, BorderLayout.CENTER);
					buConnect.setText("disconnect");
					this.validate();
					this.repaint();
				}
				else if (a.type.equals(a.TYPE_OSDXFILESERVER)) {
					
					//check pre-conditions:
					if (a.username==null || a.username.length()==0) {
						Dialogs.showMessage("Sorry, missing username in account settings");
						return;
					}
					if (a.host==null || a.host.length()==0) {
						Dialogs.showMessage("Sorry, missing host in account settings");
						return;
					}
					if (a.keystore_filename==null || a.keystore_filename.length()==0) {
						Dialogs.showMessage("Sorry, missing keystore filename in account settings");
						return;
					}
					if (a.keyid==null || a.keyid.length()==0) {
						Dialogs.showMessage("Sorry, missing key id in account settings");
						return;
					}
					
					//get osdx key out of keystoreOSDXKey key = null;
					MessageHandler mh = new DefaultMessageHandler();
					if (a.key==null || !a.key.getKeyID().equals(a.keyid)) {
						File fKeyStore = new File(a.keystore_filename);
						if (fKeyStore==null || !fKeyStore.exists()) {
							Dialogs.showMessage("Could not open KeyStore:\n"+a.keystore_filename);
							return;
						}
						try {
							KeyApprovingStore keystore = KeyApprovingStore.fromFile(fKeyStore, mh);
							a.key = keystore.getKey(a.keyid);
							if (!a.key.hasPrivateKey()) {
								Dialogs.showMessage("Sorry, no private key information for key id:\n"+a.keyid+" found in keystore");
								return;
							}
							if (!a.key.isPrivateKeyUnlocked()) {
								a.key.unlockPrivateKey(mh);
								if (!a.key.isPrivateKeyUnlocked()) {
									Dialogs.showMessage("Connection failed: private key is locked!");
									return;
								}
							}
						} catch (Exception ex) {
							ex.printStackTrace();
							Dialogs.showMessage("Error while opening KeyStore");
							return;
						}
						if (a.key==null) {
							Dialogs.showMessage("Given keyid not found.");
							return;
						}
					} else {
						if (!a.key.isPrivateKeyUnlocked()) {
							a.key.unlockPrivateKey(mh);
							if (!a.key.isPrivateKeyUnlocked()) {
								Dialogs.showMessage("Connection failed: private key is locked!");
								return;
							}
						}
					}
						
					fsRemote = RemoteFileSystem.initOSDXFileServerConnection(a.host, a.port, a.prepath, a.username, a.key);
					if (!fsRemote.isConnected()) {
						try {
							fsRemote.connect();
							//give it a seconds
							Thread.sleep(1000);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					if (!fsRemote.isConnected()) {
						addStatus("ERROR, could not connect to "+a.username+"@"+a.host+".");
						Dialogs.showMessage("Sorry, could not connect to given account.");
						return;
					} else {
						addStatus("Connection to "+a.username+"@"+a.host+" established.");
						Thread t = new Thread() {
							public void run() {
								while (fsRemote.isConnected()) {
									try {
										sleep(20000);
									} catch (Exception ex) {}
									fsRemote.noop();
								}
							}
						};
						t.start();
					}
					ttpanelRemote = new TreeAndTablePanelOSDXClient(fsRemote,false);
					ttpanelRemote.addObserver(this);
					ttpanelRemote.setPreferredColumnWidth(1, 20);
					ttpanelRemote.setPreferredColumnWidth(2, 30);
					ttpanelRemote.setColumnRenderer(0, leftRenderer);
					ttpanelRemote.setColumnRenderer(1, centerRenderer);		
					ttpanelRemote.setColumnRenderer(2, rightRenderer);
					
					panelRemote.removeAll();
					panelRemote.add(ttpanelRemote, BorderLayout.CENTER);
					buConnect.setText("disconnect");
					this.validate();
					this.repaint();
				} 
			}
		} else {
			//System.out.println("disconnect");
			addStatus("Disconnecting remote filesystem ...");
			ttpanelRemote.closeConnection();
			panelRemote.removeAll();
			this.validate();
			this.repaint();
			buConnect.setText("connect");
		}
	}

	private void button_edit_clicked() {
		int sel = selectAccount.getSelectedIndex()-2;
		if (sel>=0 && sel < accounts.size()) {
			FileTransferAccount a = accounts.get(sel);
			PanelAccount pAcc = new PanelAccount();
			pAcc.update(a);
			int ans = JOptionPane.showConfirmDialog(null,pAcc,"Edit Account",JOptionPane.OK_CANCEL_OPTION);
	    	if (ans == JOptionPane.OK_OPTION) {
	    		accounts.set(sel, pAcc.getAccount());
	    		updateAccounts();
	    		selectAccount.setSelectedIndex(sel+2);
		    }
		} else {
			PanelAccount pAcc = new PanelAccount();
			FileTransferAccount a_new = new FileTransferAccount();
			a_new.type = FileTransferAccount.TYPE_OSDXFILESERVER;
			a_new.host = "simfy.finetunes.net";
			a_new.port = 4221;
			a_new.prepath = "/";
			a_new.username = "";
			a_new.keystore_filename = System.getProperty("user.home")+File.separator+"openSDX"+File.separator+"defaultKeyStore.xml";
			if (!new File(a_new.keystore_filename).exists()) {
				a_new.keystore_filename = "";
			}
			a_new.keyid = "";
			pAcc.update(a_new);
			int ans = JOptionPane.showConfirmDialog(null,pAcc,"New Account",JOptionPane.OK_CANCEL_OPTION);
	    	if (ans == JOptionPane.OK_OPTION) {
	    		accounts.add(pAcc.getAccount());
	    		updateAccounts();
	    		selectAccount.setSelectedIndex(sel+2);
		    }
		}
		saveAccounts();
	}
	
	private void saveAccounts() {
		File f = new File(userHome,"file_transfer_settings.xml");
		boolean save = false;
		if (!f.exists()) {
			int ans = Dialogs.showYES_NO_Dialog("Save Account Settings", "Do you want to save your account settings to\n"+f.getAbsolutePath()+"?");
			if (ans == Dialogs.YES) {
				save = true;
			}
		} else {
			save = true;
		}
		if (save) {
			Element e = new Element("file_transfer_settings");
			for (FileTransferAccount a : accounts) {
				Element ea = new Element("account");
				if (a.type.equals(FileTransferAccount.TYPE_OSDXFILESERVER)) {
					ea.addContent("type", a.type);
					ea.addContent("host", a.host);
					ea.addContent("port", ""+a.port);
					if (a.prepath!=null && a.prepath.length()>0) {
						ea.addContent("prepath", a.prepath);
					}
					ea.addContent("username", a.username);
					ea.addContent("keystore", a.keystore_filename);
					ea.addContent("keyid", a.keyid);
					e.addContent(ea);
				}
				else if (a.type.equals(FileTransferAccount.TYPE_FTP)) {
					ea.addContent("type", a.type);
					ea.addContent("host", a.host);
					ea.addContent("username", a.username);
					e.addContent(ea);
				}
				else {
					Dialogs.showMessage("unsupported account type: "+a.type);
				}
			}
			try {
				Document.buildDocument(e).writeToFile(f);
			} catch (Exception ex) {
				ex.printStackTrace();
				Dialogs.showMessage("Error writing settings to file:\n"+f.getAbsolutePath());
			}
		}
	}
	
	private void button_remove_clicked() {
		int sel = selectAccount.getSelectedIndex()-2;
		if (sel>=0 && sel < accounts.size()) {
			int ans = Dialogs.showYES_NO_Dialog("Remove Account", "Are you sure you want to remove the selected account?");
			if (ans==Dialogs.YES) {
				accounts.remove(sel);
				updateAccounts();
			}
		}
	}
	
	private void button_test_clicked() {
		try {
			System.out.println("Test connection");
			FileTransferAccount a = new FileTransferAccount();
			a.host = "simfy.finetunes.net";
		//	a.host = "localhost";
			a.port = 4221;
			a.prepath = "/";
			a.username = "test";
			
			StringBuffer b = new StringBuffer();
			b.append("    <keypair>\n");
			b.append("      <identities>\n");
			b.append("        <identity>\n");
			b.append("          <identnum>0001</identnum>\n");
			b.append("          <email>test@fnppl.org</email>\n");
			b.append("          <mnemonic restricted=\"false\">testkey</mnemonic>\n");
			b.append("          <sha256>BF:13:53:A4:42:F1:12:09:7F:B6:BE:11:20:69:7D:C3:3F:AC:73:0F:F3:1B:E2:93:21:46:EA:1D:45:E7:10:3F</sha256>\n");
			b.append("        </identity>\n");
			b.append("      </identities>\n");
			b.append("      <sha1fingerprint>F0:BB:9F:32:FF:EB:92:A4:D4:85:94:CD:BB:5E:7A:D7:4C:DE:D3:78</sha1fingerprint>\n");
			b.append("      <authoritativekeyserver>keyserver.fnppl.org</authoritativekeyserver>\n");
			b.append("      <datapath>\n");
			b.append("        <step1>\n");
			b.append("          <datasource>LOCAL</datasource>\n");
			b.append("          <datainsertdatetime>2011-09-12 08:45:25 GMT+00:00</datainsertdatetime>\n");
			b.append("        </step1>\n");
			b.append("      </datapath>\n");
			b.append("      <valid_from>2011-09-12 08:45:25 GMT+00:00</valid_from>\n");
			b.append("      <valid_until>2036-09-11 14:45:25 GMT+00:00</valid_until>\n");
			b.append("      <usage>ONLYSIGN</usage>\n");
			b.append("      <level>MASTER</level>\n");
			b.append("      <parentkeyid />\n");
			b.append("      <algo>RSA</algo>\n");
			b.append("      <bits>3072</bits>\n");
			b.append("      <modulus>00:A6:77:B2:1E:CD:A5:9B:2C:F1:AF:C1:C0:F3:C8:A1:7B:24:76:49:F9:59:2E:33:00:95:5D:86:1A:F0:AE:67:35:1D:64:E3:DE:CC:06:B6:74:CE:18:56:E3:D9:74:DA:83:4E:EE:1F:AA:E3:63:14:51:49:DB:24:3B:FF:55:04:38:F4:D1:F8:0A:CF:3A:84:E9:33:D5:E9:23:18:84:3A:E9:06:7E:10:07:14:8E:BC:1D:0E:8D:74:39:CA:06:F2:9D:E8:F9:22:BF:F0:ED:3F:9E:57:A6:DE:CA:46:97:09:E1:F7:B4:30:BD:15:A6:0F:DE:32:6F:D0:B0:A6:D4:F9:38:98:72:A0:5F:A6:CE:B1:34:87:AB:7C:BC:94:F0:A8:54:C3:AC:05:BF:4A:AC:D9:E8:B8:E3:DE:76:DD:70:68:89:F1:50:62:44:B7:94:1A:C4:04:DD:82:B4:D6:C9:E0:B1:98:68:49:D5:DB:F0:86:1E:CA:58:95:13:17:42:99:8C:F3:A2:4D:5D:07:2C:39:01:5D:04:C0:D9:AE:23:97:58:77:4B:32:E5:3D:D2:E5:C5:D2:48:21:58:6A:A6:D6:CF:FE:BB:A0:AD:17:87:56:7F:F1:F1:DF:BA:95:53:11:5E:2D:07:AA:90:59:A3:C4:BB:77:C1:D8:F1:47:0A:F8:9E:71:AC:C6:97:62:24:C9:BF:C1:1F:B7:E9:F8:8A:8F:28:1C:D8:3D:8F:B0:B5:7D:61:8A:30:B3:4B:5E:CA:DA:47:BB:CB:67:F9:B2:B5:AB:67:96:A8:10:36:60:07:49:11:3F:A1:D3:7A:DA:83:C7:6C:C3:A7:0B:B4:BA:0E:7B:E9:0B:EC:23:C8:FF:8C:9E:8D:7C:D8:FC:20:88:8D:5E:48:D5:A0:5E:20:0E:90:9E:3D:6D:93:29:24:C1:DA:5F:1A:16:02:5A:B8:30:11:C9:C6:8C:88:05:18:4F:58:E5:F4:46:0C:67:1E:0C:19:F5:CB:94:5B:E8:EF:F9:51:3E:C9</modulus>\n");
			b.append("      <pubkey>\n");
			b.append("        <exponent>01:00:01</exponent>\n");
			b.append("      </pubkey>\n");
			b.append("      <privkey>\n");
			b.append("        <exponent>\n");
			b.append("          <locked>\n");
			b.append("            <mantraname>password: test</mantraname>\n");
			b.append("            <algo>AES@256</algo>\n");
			b.append("            <initvector>94:98:85:DA:CF:3A:01:92:72:02:8B:19:FD:C5:E4:D4</initvector>\n");
			b.append("            <padding>CBC/PKCS#5</padding>\n");
			b.append("            <bytes>B8:71:7A:AC:E0:30:2E:C3:36:BE:F3:BF:27:A7:72:4C:E9:43:D1:D9:CD:1F:8C:9E:46:D7:71:C4:F0:94:23:07:44:79:8D:CE:8F:15:08:9E:8B:07:E4:85:8E:72:3F:32:FD:26:1F:52:A7:9D:C1:0B:2F:15:C0:BD:F0:29:25:AB:F7:E3:BD:49:40:40:D7:61:56:66:AD:79:91:88:B9:3D:72:4F:B6:4B:F6:CD:99:59:19:69:B9:1F:77:CE:02:98:CF:EF:ED:B9:7D:3F:65:1C:35:A1:26:F9:0D:BE:C1:45:41:CF:6D:66:70:0D:DD:37:F7:F1:2D:E6:34:97:58:E2:D5:BB:BE:8E:60:8B:82:EF:B0:B4:3F:F6:F3:FB:A3:5B:EF:E1:DA:03:C6:5B:8D:93:19:8F:85:89:18:7E:E9:C4:34:D8:E3:57:8E:4B:4F:AF:5C:1A:15:77:04:F2:77:52:CE:07:B8:99:A6:88:0B:C8:6F:03:BC:2E:C5:A7:97:A5:79:F7:BA:C8:94:6E:1A:AB:C1:9A:E4:2D:98:38:6D:B1:9A:4B:FA:31:C3:1C:40:F3:E8:DB:34:38:36:19:28:0B:2C:54:59:09:35:A5:E0:60:49:50:B3:65:2B:A5:8C:51:A4:E6:FB:1C:6D:F5:0F:1E:74:3D:70:AB:92:95:5A:9C:84:13:AB:0D:6E:E1:3D:60:E1:D0:84:FC:03:D1:6B:DF:A7:5E:4B:69:08:CF:7B:2F:C3:08:F0:98:E9:25:02:C0:52:FA:ED:F7:36:08:08:FA:39:25:32:2D:B7:23:29:35:E0:BF:84:F2:6A:82:F1:63:CB:BE:EE:F6:E5:7F:F8:AF:A1:40:70:28:C3:C8:81:CA:B0:C8:5D:49:30:7D:DD:8D:E4:2D:5E:B9:F8:14:EA:BD:B8:09:7C:61:7F:17:6E:BC:5B:30:08:28:4D:AD:C2:9C:A9:39:CB:CD:34:0E:34:48:C5:85:A8:20:89:D2:49:2D:85:80:A9:54:FF:0F:0A:26:E2:FD:F7:5A:72:B6:AE:30:48:4A:7B:EE:45:BB:5E:89</bytes>\n");
			b.append("          </locked>\n");
			b.append("        </exponent>\n");
			b.append("      </privkey>\n");
			b.append("      <gpgkeyserverid />\n");
			b.append("    </keypair>\n");
	
			a.key = OSDXKey.fromElement(Document.fromString(b.toString()).getRootElement());
			a.key.unlockPrivateKey("test");
			
			fsRemote = RemoteFileSystem.initOSDXFileServerConnection(a.host, a.port, a.prepath, a.username, a.key);
			
			if (!fsRemote.isConnected()) {
				try {
					fsRemote.connect();
					//give it a seconds
					Thread.sleep(1000);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			if (!fsRemote.isConnected()) {
				addStatus("ERROR, could not connect to "+a.username+"@"+a.host+".");
				Dialogs.showMessage("Sorry, could not connect to given account.");
				return;
			} else {
				addStatus("Connection to "+a.username+"@"+a.host+" established.");
				Thread t = new Thread() {
					public void run() {
						while (fsRemote.isConnected()) {
							try {
								sleep(20000);
							} catch (Exception ex) {}
							fsRemote.noop();
						}
					}
				};
				t.start();
			}
			ttpanelRemote = new TreeAndTablePanelOSDXClient(fsRemote,false);
			ttpanelRemote.addObserver(this);
			ttpanelRemote.setPreferredColumnWidth(1, 20);
			ttpanelRemote.setPreferredColumnWidth(2, 30);
			ttpanelRemote.setColumnRenderer(0, leftRenderer);
			ttpanelRemote.setColumnRenderer(1, centerRenderer);		
			ttpanelRemote.setColumnRenderer(2, rightRenderer);
			
			panelRemote.removeAll();
			panelRemote.add(ttpanelRemote, BorderLayout.CENTER);
			buConnect.setText("disconnect");
			this.validate();
			this.repaint();
			
			//test upload
			if (fsRemote.isConnected()) {
				int ant = Dialogs.showYES_NO_Dialog("Test connection", "Test connection established.\nTry to upload / download a file?");
				if (ant==Dialogs.YES) {
					long now = System.currentTimeMillis();
					File local = File.createTempFile("test_upload_"+now, ".tmp");
					FileOutputStream out = new FileOutputStream(local);
					
					byte[] data = SecurityHelper.getRandomBytes(3000); 
					out.write(data);
					out.flush();
					out.close();
					
					RemoteFile remote = new RemoteFile("/",local.getName(), local.length(), System.currentTimeMillis(), false);
					System.out.println("uploading: "+local.getAbsolutePath()+" -> "+remote.getFilnameWithPath());
					fsRemote.upload(local, remote, null);
					ttpanelRemote.refreshView();
					local.delete();
					
					int antDown = Dialogs.showYES_NO_Dialog("Test connection", "Try to download the uploaded file?");
					if (antDown==Dialogs.YES) {
						File local2 = File.createTempFile("test_download_"+now, ".tmp");
						local2.delete();
						System.out.println("downloading: "+local2.getAbsolutePath()+" <- "+remote.getFilnameWithPath());
						fsRemote.download(local2, remote, null);
						Thread.sleep(3000);
						fsRemote.remove(remote);
						FileInputStream fin = new FileInputStream(local2);
						byte[] data2 = new byte[3000];
						int read = fin.read(data2);
						if (read!=3000 || !Arrays.equals(data, data2)) {
							Dialogs.showMessage("Error downloading file.");
						} else {
							Dialogs.showMessage("File downloading successful.");
						}
						local2.delete();
					}
				}
				return;
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void button_upload_clicked() {
		Thread t = new Thread() { //dont block ui
			public void run() {
				System.out.println("upload");
				Vector<RemoteFile> localFiles = panelLocal.getSelectedFiles();
				if (localFiles==null || localFiles.size()==0) {
					Dialogs.showMessage("Please select files to upload on local side.");
					return;
				}
				RemoteFile tragetDirectory = ttpanelRemote.getSelectedDir();
				if (tragetDirectory==null) {
					Dialogs.showMessage("Please select a target directory on remote side.");
					return;
				}
				for (RemoteFile localFile : localFiles) {
					final File from = new File(localFile.getPath(), localFile.getName());
					RemoteFile to = new RemoteFile(tragetDirectory.getFilnameWithPath(), from.getName(), from.length(), from.lastModified(), false);
					final String msg = "uploading "+from.getAbsolutePath()+" -> "+to.getFilnameWithPath();
					final int pos = addStatus("uploading "+from.getAbsolutePath()+" -> "+to.getFilnameWithPath());
					
					FileTransferProgress progress = new FileTransferProgress(from.length()) {
						private long time = -1L;
						private long dataAtTime = 0L;
						public void onUpdate() {
							String transferRate = "";
							if (time==-1L) {
								time = System.currentTimeMillis();
								dataAtTime = getProgress();
							} else {
								long now = System.currentTimeMillis();
								long dataNow = getProgress();
								long tr = ((dataNow-dataAtTime)*1000)/((now-time)*1024); //in kB / s
								transferRate = String.format("  (%d kB/s)", tr);
							}
							setStatus(pos, msg+"   "+getProgressString()+transferRate);
						}
						public void onError() {
							setStatus(pos, msg+"   ERROR");
							String msg = getErrorMsg();
							if (msg == null) {
								Dialogs.showMessage("Error uploading file:\n"+from.getAbsolutePath());
							} else {
								Dialogs.showMessage("Error uploading file:\n"+from.getAbsolutePath()+"\n"+msg);
							}
						}
					};
					try {
						fsRemote.upload(from, to, progress);
					} catch (FileTransferException ex) {
						progress.setError(ex.getMessage());
					}
				}
				//update view
				ttpanelRemote.refreshView();
			}
		};
		t.start();
	}
	
	private void button_download_clicked() {
		System.out.println("download");
		final RemoteFile local = panelLocal.getSelectedDir();
		if (local==null) {
			Dialogs.showMessage("Please select a local directory.");
			return;
		}
		final Vector<RemoteFile> remote = ttpanelRemote.getSelectedFiles();
		if (remote==null || remote.size()==0) {
			Dialogs.showMessage("Please select files to download on remote side.");
			return;
		}
		//don't block ui
		Thread tDownload = new Thread() {
			public void run() {
				for (final RemoteFile remoteFile : remote) {
					final File target = new File(local.getFilnameWithPath(),remoteFile.getName());
					final String pre = "downloading "+remoteFile.getFilnameWithPath()+" -> "+target.getAbsolutePath(); 
					final int pos = addStatus(pre+"   (waiting)");
					FileTransferProgress progress = new FileTransferProgress(remoteFile.getLength()) {
						private long time = -1L;
						private long dataAtTime = 0L;
						public void onUpdate() {
							String transferRate = "";
							if (time==-1L) {
								time = System.currentTimeMillis();
								dataAtTime = getProgress();
							} else {
								long now = System.currentTimeMillis();
								long dataNow = getProgress();
								long tr = ((dataNow-dataAtTime)*1000)/((now-time)*1024); //in kB / s
								transferRate = String.format("  (%d kB/s)", tr);
							}
							setStatus(pos, pre+"   "+getProgressString()+transferRate);
							if (hasFinished()) {
								panelLocal.refreshView();
							}
						}
						public void onError() {
							setStatus(pos, pre+"   ERROR");
							String msg = getErrorMsg();
							if (msg==null) {
								Dialogs.showMessage("Error downloading file:\n"+remoteFile.getFilnameWithPath());
							} else {
								Dialogs.showMessage("Error downloading file:\n"+remoteFile.getFilnameWithPath()+"\n"+msg);
							}
						}
					};
					try {
						fsRemote.download(target, remoteFile, progress);
					} catch (FileTransferException ex) {
						progress.setError(ex.getMessage());
					}
				}
			}
		};
		tDownload.start();
		//update view
		panelLocal.refreshView();
	}
	
	Object sync_object = new Object();
	public int addStatus(String message) {
		synchronized (sync_object) {
			log_model.addElement(message);
			int pos = log_model.size()-1;
			log.setSelectedIndex(pos);
			log.repaint();	
			return pos;
		}
	}
	
	public void setStatus(int pos, String message) {
		synchronized (sync_object) {
			log_model.set(pos, message);
			log.setSelectedIndex(pos);
			log.ensureIndexIsVisible(pos);
			log.ensureIndexIsVisible(pos);
			log.repaint();
		}
	}
	
	public void setLastStatus(String message) {
		synchronized (sync_object) {
			int pos = log_model.getSize()-1;
			log_model.set(pos, message);
			log.setSelectedIndex(pos);
			log.ensureIndexIsVisible(pos);
			log.repaint();
		}
	}
	
	public String getLastLogEntry() {
		return (String)log_model.lastElement();
	}
	
	public void notifyChange(MyObservable changedIn) {
		if (changedIn == ttpanelRemote) {
			button_download_clicked();
		} else if (changedIn == panelLocal) {
			button_upload_clicked();
		}
	}
}
