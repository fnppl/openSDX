package org.fnppl.opensdx.dmi;
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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.fnppl.opensdx.common.BusinessObject;
import org.fnppl.opensdx.common.Feed;
import org.fnppl.opensdx.common.Receiver;
import org.fnppl.opensdx.file_transfer.Beamer;
import org.fnppl.opensdx.gui.DefaultMessageHandler;
import org.fnppl.opensdx.gui.Dialogs;
import org.fnppl.opensdx.gui.Helper;
import org.fnppl.opensdx.gui.helper.PanelReceiver;
import org.fnppl.opensdx.helper.Logger;
import org.fnppl.opensdx.http.HTTPClient;
import org.fnppl.opensdx.http.HTTPClientPutRequest;
import org.fnppl.opensdx.http.HTTPClientResponse;
import org.fnppl.opensdx.security.KeyApprovingStore;
import org.fnppl.opensdx.security.OSDXKey;
import org.fnppl.opensdx.security.Result;
import org.fnppl.opensdx.xml.Document;

public class BeamMeUpGui extends JFrame {

	private static BeamMeUpGui instance = null;

	public static BeamMeUpGui getInstance() {
		if(instance == null) {
			instance = new BeamMeUpGui();
		}
		return instance;
	}

	private File lastDir = new File(System.getProperty("user.home"));
	private Feed currentFeed = null;

	private JPanel pFeed;
	private JLabel label_feed;
	private JTextField text_feed;
	private JButton bu_feed_open;

	private JPanel pSignature;
	private JLabel label_keystore;
	private JTextField text_keystore;
	private JButton bu_keystore_select;
	private JLabel label_keyid;
	private JTextField text_keyid;
	private JButton bu_keyid_select;
	private JLabel label_pw;
	private JPasswordField text_pw;
	
	private JPanel pReceiver;
	private JPanel pSummary;
	private JTextArea text_summary;
	private JScrollPane scroll_summary;
	
	private JButton bu_beam;


	public BeamMeUpGui(Feed feed) {
		super("fnppl.org :: openSDX :: Beam me up");		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				returnToFeedGui();
			}
		});
		setSize(700, 768);
		Helper.centerMe(this, null);
		currentFeed = feed;
		
		buildUi();
		
		//set values to gui
		label_feed.setText("Feed ID:");
		text_feed.setText(feed.getFeedinfo().getFeedID());
		bu_feed_open.setVisible(false);
		
		try {
			String keystore = feed.getFeedinfo().getReceiver().getFileKeystore();
			if (keystore!=null && keystore.length()>0) {
				text_keystore.setText(keystore);
			}
			String keyid = feed.getFeedinfo().getReceiver().getKeyID();
			if (keyid!=null && keyid.length()>0) {
				text_keyid.setText(keyid);
			}
		} catch (Exception ex) {
			
		}
		
		pReceiver.removeAll();
		pReceiver.add(new PanelReceiver(feed.getFeedinfo().getReceiver()));

		Vector<String[]> extrafiles = Beamer.getUploadExtraFiles(currentFeed);
		String ef = "";
		if (extrafiles.size()==0) {
			ef = "[no bundle / item file uploads]";
		} else {
			ef = "bundle / item file uploads:\n";
			for (String[] s : extrafiles) {
				ef += s[0]+"\n";
			}
		}
		text_summary.setText(ef);
		pSummary.setVisible(true);
	}
	
	private BeamMeUpGui() {
		super("fnppl.org :: openSDX :: Beam me up");		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				quit();
			}
		});
		setSize(700, 768);
		Helper.centerMe(this, null);
	}

	public void quit() {
		System.exit(0);
	}
	
	public void returnToFeedGui() {
		this.dispose();
	}

	private void buildUi() {
		initComponents();
		initLayout();
	}

	private void initComponents() {
		//feed
		pFeed = new JPanel();
		pFeed.setBorder(new TitledBorder("Feed"));
		label_feed = new JLabel("file");
		bu_feed_open = new JButton("open");
		bu_feed_open.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bu_feed_open_clicked();
			}
		});
		text_feed = new JTextField();
		text_feed.setEditable(false);
		text_feed.setName("text_feed");

		//signature
		pSignature = new JPanel();
		pSignature.setBorder(new TitledBorder("Signature"));
		
		
		label_keystore = new JLabel("KeyStore");
		
		
		text_keystore = new JTextField("");
		File defStore = new File(System.getProperty("user.home")+File.separator+"openSDX"+File.separator+"defaultKeyStore.xml");
		if (defStore.exists()) {
			text_keystore.setText(defStore.getAbsolutePath());
		}
		text_keystore.setEditable(false);
		text_keystore.setName("text_keystore");

		bu_keystore_select = new JButton("select");
		bu_keystore_select.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bu_keystore_select_clicked();
			}
		});

		label_keyid = new JLabel("Key ID");

		text_keyid = new JTextField("");
		text_keyid.setEditable(false);
		text_keyid.setName("text_keyid");

		bu_keyid_select = new JButton("select");
		bu_keyid_select.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bu_keyid_select_clicked();
			}
		});
		
		label_pw = new JLabel("Password");
		text_pw = new JPasswordField();
		
		//receiver
		pReceiver = new JPanel();

		//summary
		pSummary = new JPanel();
		pSummary.setBorder(new TitledBorder("Summary - Uploading files"));
		text_summary = new JTextArea("");
		scroll_summary = new JScrollPane(text_summary);
		pSummary.setVisible(false);
		
		//beam
		bu_beam = new JButton("<html><b>beam me up!</b></html>");
		bu_beam.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bu_beam_clicked();
			}
		});

	}
	
	private void initLayout() {
		
		//layout pFeed
		GridBagLayout gbl = new GridBagLayout();
		pFeed.setLayout(gbl);
		GridBagConstraints gbc = new GridBagConstraints();

		//label_feed
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
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_feed,gbc);
		pFeed.add(label_feed);
		
		//text_feed
		gbc.gridx = 1;
		gbc.weightx = 100.0;
		gbl.setConstraints(text_feed,gbc);
		pFeed.add(text_feed);
		
		//bu_feed_open
		gbc.gridx = 2;
		gbc.weightx = 0.0;
		gbl.setConstraints(bu_feed_open,gbc);
		pFeed.add(bu_feed_open);
		
		//layout pSignature
		gbl = new GridBagLayout();
		pSignature.setLayout(gbl);
		gbc = new GridBagConstraints();

		//label_keystore
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
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_keystore,gbc);
		pSignature.add(label_keystore);
		
		//text_keystore
		gbc.gridx = 1;
		gbc.weightx = 100.0;
		gbl.setConstraints(text_keystore,gbc);
		pSignature.add(text_keystore);
		
		//bu_keystore
		gbc.gridx = 2;
		gbc.weightx = 0.0;
		gbl.setConstraints(bu_keystore_select,gbc);
		pSignature.add(bu_keystore_select);
		
		
		//label_keyid
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbl.setConstraints(label_keyid,gbc);
		pSignature.add(label_keyid);
		
		//text_keyid
		gbc.gridx = 1;
		gbc.weightx = 100.0;
		gbl.setConstraints(text_keyid,gbc);
		pSignature.add(text_keyid);
		
		//bu_keyid
		gbc.gridx = 2;
		gbc.weightx = 0.0;
		gbl.setConstraints(bu_keyid_select,gbc);
		pSignature.add(bu_keyid_select);
		
		//label_pw
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbl.setConstraints(label_pw,gbc);
		pSignature.add(label_pw);
		
		//text_pw
		gbc.gridx = 1;
		gbc.weightx = 100.0;
		gbl.setConstraints(text_pw,gbc);
		pSignature.add(text_pw);
		
		//layout pSummary
		pSummary.setLayout(new BorderLayout());
		pSummary.add(scroll_summary, BorderLayout.CENTER);
		
		//layout pReceiver
		pReceiver.setLayout(new BorderLayout());
		
		
		
		//layout gui
		
		JPanel content = new JPanel();
		
		
		gbl = new GridBagLayout();
		content.setLayout(gbl);
		gbc = new GridBagConstraints();

		//pFeed
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 100.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(pFeed,gbc);
		content.add(pFeed);
		
		//pSignature
		gbc.gridy = 1;
		gbl.setConstraints(pSignature,gbc);
		content.add(pSignature);
		
		//pReceiver
		gbc.gridy = 2;
		gbl.setConstraints(pReceiver,gbc);
		content.add(pReceiver);
		
		//pSummary
		gbc.gridy = 3;
		gbc.weighty = 99.0;
		gbl.setConstraints(pSummary,gbc);
		content.add(pSummary);
		
		//spacer
		JLabel spacer =new JLabel();
		gbc.gridy = 4;
		gbc.weighty = 1.0;
		gbl.setConstraints(spacer,gbc);
		content.add(spacer);
		
		setLayout(new BorderLayout());
		JScrollPane scroll = new JScrollPane(content);
		add(scroll, BorderLayout.CENTER);
		
		//beam button
		Dimension dim = new Dimension(450,45);
		bu_beam.setMinimumSize(dim);
		bu_beam.setMaximumSize(dim);
		bu_beam.setPreferredSize(dim);
		
		add(bu_beam, BorderLayout.SOUTH);
		
	}

	public void bu_feed_open_clicked() {
		File file = Dialogs.chooseOpenFile("Open Feed", lastDir, "feed.xml");
		if (file!=null) {
			lastDir = file.getParentFile();
			try {
				Document doc = Document.fromFile(file);
				currentFeed = Feed.fromBusinessObject(BusinessObject.fromElement(doc.getRootElement()));
				
				//has receiver
				Receiver r = currentFeed.getFeedinfo().getReceiver();
				if (r==null) {
					Dialogs.showMessage("Sorry, missing receiver in selected feed.");
					return;
				}
				
				//TODO validate
				
				
				//set values to gui
				text_feed.setText(file.getAbsolutePath());
				pReceiver.removeAll();
				pReceiver.add(new PanelReceiver(r));

				Vector<String[]> extrafiles = Beamer.getUploadExtraFiles(currentFeed);
				String ef = "";
				if (extrafiles.size()==0) {
					ef = "[no bundle / item file uploads]";
				} else {
					ef = "bundle / item file uploads:\n";
					for (String[] s : extrafiles) {
						ef += s[0]+"\n";
					}
				}
				text_summary.setText(ef);
				pSummary.setVisible(true);
			} catch (Exception ex) {
				Dialogs.showMessage("Sorry, wrong format in selected feed file.");
				ex.printStackTrace();
			}
		}
	}
	
	public void bu_keystore_select_clicked() {
		File f = Dialogs.chooseOpenFile("Open KeyStore", lastDir, "keystore.xml");
        if (f==null) return;
        lastDir = f.getParentFile();
        text_keystore.setText(f.getAbsolutePath());
        
	}
	
	public void bu_keyid_select_clicked() {
		String filenameKeystore = text_keystore.getText();
        if (filenameKeystore == null || filenameKeystore.length()==0) {
            Dialogs.showMessage("Please select a keystore file first.");
            return;
        }
        File f = new File(filenameKeystore);
        if (!f.exists()) {
            Dialogs.showMessage("Sorry. selected keystore file does not exist.");
            return;
        }
        try {
            KeyApprovingStore keystore = KeyApprovingStore.fromFile(f, new DefaultMessageHandler());
            OSDXKey key = FeedGui.selectPrivateSigningKey(keystore);
            text_keyid.setText(key.getKeyID());
        } catch (Exception ex) {
            Dialogs.showMessage("Error opening keystore. Please select a valid keytore file.");
            ex.printStackTrace();
        }
	}
	
	public  void bu_beam_clicked() {
		if (currentFeed==null) {
			Dialogs.showMessage("Please open a feed for upload first.");
			return;
		}
		//check signature key
		OSDXKey signatureKey = null;
		 try {
            KeyApprovingStore keystore = KeyApprovingStore.fromFile(new File(text_keystore.getText()), new DefaultMessageHandler());
            signatureKey = keystore.getKey(text_keyid.getText());
        } catch (Exception ex) {
        	//ex.printStackTrace();
        	Dialogs.showMessage("Please select a valid keystore and keyid for signing the feed.");
            return;
        }
        
        try {
        	signatureKey.unlockPrivateKey(text_pw.getPassword()); 
        } catch (Exception ex) {
        	//ex.printStackTrace();
        	Dialogs.showMessage("Sorry, wrong password for signature key.");
            return;
        }
        String buText = bu_beam.getText();
        bu_beam.setEnabled(false);
        bu_beam.setText("uploading... please wait");
        Result result = Beamer.beamUpFeed(currentFeed, signatureKey, new DefaultMessageHandler());
        if (result.succeeded) {
        	Dialogs.showMessage("Upload succeeded.");
        } else {
        	String msg = "Upload failed";
        	if (result.errorMessage==null) {
        		msg += ".";
        	} else {
        		msg += ":\n"+result.errorMessage;
        	}
        	//Dialogs.showMessage(msg);
        	
        	
        	msg  += "\n\nDo you want to send a report?";
			int ans = Dialogs.showYES_NO_Dialog("Test connection successful.",msg);
			if (ans==Dialogs.YES) {
				sendLogFile();
			}
        }
        bu_beam.setText(buText);
        bu_beam.setEnabled(true);
	}
	
	private void sendLogFile() {
		Logger logger = Logger.getFileTransferLogger();
		File log = Logger.getFileTransferLogger().getLogFile();
		if (log!=null) {	
			HTTPClient httpclient = new HTTPClient(logger.getLogfileUploadHost(), logger.getLogfileUploadPort());
			try {
				HTTPClientResponse resp = httpclient.sendPut(new HTTPClientPutRequest(log, logger.getLogfileUploadCommand()));
				Dialogs.showMessage("Send logging :: "+resp.status);
				
			} catch (Exception e) {
				e.printStackTrace();
				Dialogs.showMessage("Error sending logfile.\nThe logfile has been saved to\n"+log.getAbsolutePath()+"\nYou can send it by email.");
			}
		} else {
			Dialogs.showMessage("Logfile not found.");
		}
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch(Exception ex){
			System.out.println("Nimbus look & feel not available");
		}
		BeamMeUpGui s = BeamMeUpGui.getInstance();
		s.buildUi();
		s.setVisible(true);
	}

}
