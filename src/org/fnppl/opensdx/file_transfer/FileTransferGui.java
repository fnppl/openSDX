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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
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
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import org.fnppl.opensdx.gui.DefaultMessageHandler;
import org.fnppl.opensdx.gui.Dialogs;
import org.fnppl.opensdx.gui.Helper;
import org.fnppl.opensdx.gui.MessageHandler;
import org.fnppl.opensdx.gui.helper.MyObservable;
import org.fnppl.opensdx.gui.helper.MyObserver;
import org.fnppl.opensdx.gui.helper.PanelAccount;
import org.fnppl.opensdx.gui.helper.PanelEncrypt;
import org.fnppl.opensdx.gui.helper.TreeAndTablePanel;
import org.fnppl.opensdx.security.KeyApprovingStore;
import org.fnppl.opensdx.security.OSDXKey;
import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.security.SymmetricKey;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;

public class FileTransferGui extends JFrame implements MyObserver {

	private Vector<FileTransferAccount> accounts = new Vector<FileTransferAccount>();
	private JPanel panelNorth;
	private JComboBox selectAccount;
	private JButton buConnect;
	private JButton buEdit;
	private JButton buRemove;

	private DefaultComboBoxModel selectAccount_model;
	private TreeAndTablePanel panelLocal;
	private RemoteFileSystem fsLocal;
	
	private JPanel panelRemote;
	private TreeAndTablePanel ttpanelRemote;
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
		panelNorth.add(buConnect);
		panelNorth.add(buEdit);
		panelNorth.add(buRemove);

		fsLocal = RemoteFileSystem.initLocalFileSystem();
		panelLocal = new TreeAndTablePanel(fsLocal,true);
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
					ttpanelRemote = new TreeAndTablePanel(fsRemote,false);
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
										sleep(8000);
									} catch (Exception ex) {}
									fsRemote.noop();
								}
							}
						};
						t.start();
					}
					ttpanelRemote = new TreeAndTablePanel(fsRemote,false);
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
						public void onUpdate() {
							setStatus(pos, msg+"   "+getProgressString());
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
					final int pos = addStatus(pre);
					FileTransferProgress progress = new FileTransferProgress(remoteFile.getLength()) {
						public void onUpdate() {
							setStatus(pos, pre+"   "+getProgressString());
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
