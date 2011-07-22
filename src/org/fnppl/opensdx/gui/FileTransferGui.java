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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.fnppl.opensdx.ftp.FTPClient;
import org.fnppl.opensdx.ftp.RemoteFile;
import org.fnppl.opensdx.ftp.RemoteFileSystem;
import org.fnppl.opensdx.gui.helper.MyObservable;
import org.fnppl.opensdx.gui.helper.MyObserver;
import org.fnppl.opensdx.gui.helper.TreeAndTableNode;
import org.fnppl.opensdx.gui.helper.TreeAndTablePanel;
import org.fnppl.opensdx.security.KeyApprovingStore;
import org.fnppl.opensdx.security.OSDXKey;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;

import sun.swing.BakedArrayList;

public class FileTransferGui extends JFrame implements MyObserver {

	private Vector<Account> accounts = new Vector<Account>();
	private JPanel panelNorth;
	private JComboBox selectAccount;
	private JButton buConnect;
	private JButton buEdit;

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
	private JTextArea log;
	private File userHome = null;

	public FileTransferGui() {
		initUserHome();
		initSettings();
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
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
					Account a = new Account();
					a.type = e.getChildText("type");
					if (a.type.equals(a.TYPE_FTP)) {
						a.username = e.getChildText("username");
						a.host = e.getChildText("host");
					}
					accounts.add(a);
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
		for (Account a : accounts) {
			if (a.type.equals(a.TYPE_FTP)) {
				selectAccount_model.addElement(a.type+" :: "+a.username+"@"+a.host);
			} 
			else if (a.type==a.TYPE_OSDXFILESERVER) {
				String name = a.type+" :: "+a.username+"::"+a.keyid+"@"+a.host;
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
		panelNorth.add(buConnect);
		panelNorth.add(buEdit);

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
		log = new JTextArea();
		log.setEditable(false);

	}
	
	private void initLayout() {
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(panelNorth, BorderLayout.NORTH);
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				panelLocal, panelRemote);
		split.setDividerLocation(500);
		getContentPane().add(split, BorderLayout.CENTER);

		panelSouth.setLayout(new BorderLayout());
		panelSouth.add(log, BorderLayout.CENTER);
		log.setPreferredSize(new Dimension(200, 100));
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
			if (sel>=0 && sel < accounts.size()) {
				Account a = accounts.get(sel);
				System.out.println("account: "+a.type+" :: "+a.username);
				if (a.type.equals(a.TYPE_FTP)) {
					String pw = Dialogs.showPasswordDialog("Enter Password","Please enter password for ftp account:\nhost: "+a.host+"\nusername: "+a.username);
					if (pw==null) {
						return;
					}
					fsRemote = RemoteFileSystem.initFTPFileSystem(a.host, a.username, pw);
					if (!fsRemote.isConnected()) {
						try {
							fsRemote.connect();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					if (!fsRemote.isConnected()) {
						addStatus("ERROR, could not connect to "+a.username+"@"+a.host+" established.");
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
					this.validate();
					this.repaint();
				}
				buConnect.setText("disconnect");
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

	}
	
	private void button_upload_clicked() {
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
			File from = new File(localFile.getPath(), localFile.getName());
			RemoteFile to = new RemoteFile(tragetDirectory.getFilnameWithPath(), from.getName(), from.length(), from.lastModified(), false);
			addStatus("uploading "+from.getAbsolutePath()+" -> "+to.getFilnameWithPath());
			fsRemote.upload(from, to);
		}
	}
	
	private void button_download_clicked() {
		System.out.println("download");
		RemoteFile local = panelLocal.getSelectedDir();
		if (local==null) {
			Dialogs.showMessage("Please select a local directory.");
			return;
		}
		Vector<RemoteFile> remote = ttpanelRemote.getSelectedFiles();
		if (remote==null || remote.size()==0) {
			Dialogs.showMessage("Please select files to download on remote side.");
			return;
		}
		for (RemoteFile remoteFile : remote) {
			File target = new File(local.getFilnameWithPath(),remoteFile.getName());
			addStatus("downloading "+remoteFile.getFilnameWithPath()+" -> "+target.getAbsolutePath());
			fsRemote.download(target, remoteFile);
			
		}
	}
	
	public void addStatus(String message) {
		log.append(message+"\n");
	}
	
	public void notifyChange(MyObservable changedIn) {
		if (changedIn == ttpanelRemote) {
			button_download_clicked();
		} else if (changedIn == panelLocal) {
			button_upload_clicked();
		}
	}

	private class Account {
		public final String TYPE_FTP = "ftp";
		public final String TYPE_OSDXFILESERVER = "openSDX fileserver";
		
		public String type = null;
		public String host = null;
		public String username = null;
		public String keystore_filename = null;
		public String keyid = null;
		public KeyApprovingStore keystore = null;
		public OSDXKey key = null;
	}
}
