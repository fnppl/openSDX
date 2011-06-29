package org.fnppl.opensdx.dmi;


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


import java.awt.*;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.print.Doc;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;

import org.fnppl.opensdx.common.Bundle;
import org.fnppl.opensdx.common.BundleInformation;
import org.fnppl.opensdx.common.BusinessObject;
import org.fnppl.opensdx.common.Feed;
import org.fnppl.opensdx.common.IDs;
import org.fnppl.opensdx.common.Item;
import org.fnppl.opensdx.common.ItemFile;
import org.fnppl.opensdx.common.ItemTags;
import org.fnppl.opensdx.common.LicenseBasis;
import org.fnppl.opensdx.common.Receiver;
import org.fnppl.opensdx.gui.DefaultMessageHandler;
import org.fnppl.opensdx.gui.Dialogs;
import org.fnppl.opensdx.gui.EditBusinessObjectTree;
import org.fnppl.opensdx.gui.Helper;
import org.fnppl.opensdx.gui.MessageHandler;
import org.fnppl.opensdx.gui.MyObservable;
import org.fnppl.opensdx.gui.MyObserver;
import org.fnppl.opensdx.gui.PanelBundle;
import org.fnppl.opensdx.gui.PanelFeedInfo;
import org.fnppl.opensdx.gui.PanelItems;
import org.fnppl.opensdx.gui.SecurityMainFrame;
import org.fnppl.opensdx.gui.SelectTerritoiresTree;
import org.fnppl.opensdx.securesocket.OSDXFileTransferClient;
import org.fnppl.opensdx.security.*;
import org.fnppl.opensdx.xml.*;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;



public class FeedGui extends JFrame implements MyObserver {
	private static FeedGui instance = null;
	private URL configGenres = FeedCreator.class.getResource("resources/config_genres.xml");
	private static URL configLanguageCodes = FeedGui.class.getResource("resources/iso639-1_language_codes.csv");

	public static FeedGui getInstance() {
		if(instance == null) {
			instance = new FeedGui();
		}
		
		return instance;
	}
	
	private File lastDir = new File(System.getProperty("user.home"));
	
	private JTabbedPane jt = null;
	private StatusBar status = null;
	
//	BundlePanel bundle_panel = null;
//	FeedInfoPanel feedinfo_panel = null;
//	BundledItemsPanel bundled_items_panel = null;
	
	PanelFeedInfo feedinfo_panel = null;
	PanelBundle bundle_panel = null;
	PanelItems bundled_items_panel = null;
	
	JPanel treePanel = null;
	
	private Feed currentFeed = null;
	
	
	private FeedGui() {
		super("fnppl.org :: openSDX :: FeedGui");		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				quit();
			}
		});
		
		setSize(1024, 768);
		makeMenuBar();
		Helper.centerMe(this, null);
	}
	
	
	private void initTooltips() {
		initTooltips(feedinfo_panel);
		initTooltips(bundle_panel);
		initTooltips(bundled_items_panel);
	}
	
	private void initTooltips(Object ob) {
		String configName = "tooltips_"+ob.getClass().getSimpleName()+".txt";
		File config = new File("src/org/fnppl/opensdx/dmi/resources/"+configName);
		
		boolean save = false;
		
		Properties tooltips = new Properties();
		if (config.exists()) {
			try {
			FileInputStream in = new FileInputStream(config);
			tooltips.load(in);
			in.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		Field[] fields = ob.getClass().getDeclaredFields();
         for (Field f : fields) {
            f.setAccessible(true);
            try {
            	Object obj = f.get(ob);
            	if (!(obj instanceof JLabel) && !(obj instanceof JPanel) && !(obj instanceof JScrollPane)) {
	            	Method m = obj.getClass().getMethod("setToolTipText", String.class);
					//System.out.println("tooltip::"+f.getName());
					String key = f.getName();
					if (tooltips.containsKey(key)) {
						String tip = (String)tooltips.get(key);
						if (tip.length()>0) {
							m.invoke(obj, tip);
						}
					} else {
						tooltips.setProperty(key, "");
					}
            	}
            } catch (Exception ex) {
            } 
        }
         if (save) {
 	        try {
 	        	FileOutputStream out = new FileOutputStream(config);
 				tooltips.store(out, null);
 				out.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
         }
	}
	
	public void makeMenuBar() {
		ActionListener ja = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String cmd = e.getActionCommand();

				if(cmd.equalsIgnoreCase("quit")) {
					quit();
				}
				else if(cmd.equalsIgnoreCase("new feed")) {
					newEmptyFeed();
				}
				else if(cmd.equalsIgnoreCase("open feed")) {
					openFeed();
				}
				else if(cmd.equalsIgnoreCase("save feed")) {
					saveFeed();
				}
				else if(cmd.equalsIgnoreCase("send feed")) {
					sendFeedToReceiver();
				}
				else if(cmd.equalsIgnoreCase("init example feed")) {
					init_example_feed();
				}
			}
		};

		JMenuBar jb = new JMenuBar();
		JMenu jm = new JMenu("File");
		jb.add(jm);
		JMenuItem jmi = null;

		jmi = new JMenuItem("New Empty Feed");
		jmi.setActionCommand("new feed");
		jmi.addActionListener(ja);
		jm.add(jmi);
		
		jmi = new JMenuItem("Open xml Feed ...");
		jmi.setActionCommand("open feed");
		jmi.addActionListener(ja);
		jm.add(jmi);
		
		jmi = new JMenuItem("Save Feed to xml ...");
		jmi.setActionCommand("save feed");
		jmi.addActionListener(ja);
		jm.add(jmi);
		
		jm.addSeparator();
		
		jmi = new JMenuItem("Send Feed to Receiver ...");
		jmi.setActionCommand("send feed");
		jmi.addActionListener(ja);
		jm.add(jmi);
		
		jm.addSeparator();
		
		jmi = new JMenuItem("Init Example Feed");
		jmi.setActionCommand("init example feed");
		jmi.addActionListener(ja);
		jm.add(jmi);
		
		jm.addSeparator();
		
		jmi = new JMenuItem("Quit");
		jmi.setActionCommand("quit");
		jmi.addActionListener(ja);
		jm.add(jmi);
		
		setJMenuBar(jb);
	}
	
	public void openFeed() {
		File f = Dialogs.chooseOpenFile("Select Feed", lastDir, "feed.xml");
		if (f!=null && f.exists()) {
			try {
				Document doc = Document.fromFile(f);
				Feed feed = Feed.fromBusinessObject(BusinessObject.fromElement(doc.getRootElement()));
				currentFeed = feed;
				update();
			} catch (Exception e) {
				Dialogs.showMessage("ERROR, could not open feed in file\n"+f.getAbsolutePath());
				e.printStackTrace();
			}
		}
	}
	
	public void saveFeed() {
		if (currentFeed!=null) {
			String name = "feed.xml";
			String feedid = currentFeed.getFeedinfo().getFeedID();
			if (feedid!=null && feedid.length()>0) {
				name = feedid+".xml";
			}
			File f = Dialogs.chooseSaveFile("Select filename for saving feed", lastDir, name);
			if (f!=null) {
				try {
					Document doc = Document.buildDocument(currentFeed.toElement());
					doc.writeToFile(f);
				} catch (Exception ex) {
					Dialogs.showMessage("ERROR, feed could not be saved to "+f.getAbsolutePath());
					ex.printStackTrace();
				}
			}
		}
	}
	
	public void sendFeedToReceiver() {
		if (currentFeed!=null) {
			Receiver receiver = currentFeed.getFeedinfo().getReceiver();
			if (receiver==null) {
				Dialogs.showMessage("Please enter complete receiver information in FeedInfo tab first.");
				return;
			}
			String type = receiver.getType();
			String servername = receiver.getServername();
			if (type.equals("openSDX fileserver")) {
				int ans = Dialogs.showYES_NO_Dialog("Sending Feed", "Do you really want to send the current feed to "+servername+"?");
				if (ans==Dialogs.YES) {
					sendFeedToOSDXFileserver();
				}
			}
			else {
				Dialogs.showMessage("Sorry, sending type \""+type+"\" not implemented.");
			}
		}
	}
	
	private void sendFeedToOSDXFileserver() {
		String servername = currentFeed.getFeedinfo().getReceiver().getServername();
		String keystore = currentFeed.getFeedinfo().getReceiver().getFileKeystore();
		String keyid = currentFeed.getFeedinfo().getReceiver().getKeyID();
		
		File f = null;
		if (keystore!=null) {
			f = new File(keystore);
		} else {
			f = Dialogs.chooseOpenFile("Open KeyStore", lastDir, "keystore.xml");
		}
		if (f==null) return;
		
		OSDXKey mysigning = null;
		MessageHandler mh = new DefaultMessageHandler() {
			public boolean requestOverwriteFile(File file) {
				return false;
			}
			public boolean requestIgnoreVerificationFailure() {
				return false;
			}
			public boolean requestIgnoreKeyLogVerificationFailure() {
				return false;
			}
		};
		try {
			KeyApprovingStore store = KeyApprovingStore.fromFile(f, mh); 
			
			if (keyid!=null) {
				mysigning = store.getKey(keyid);
				if (mysigning==null) {
					Dialogs.showMessage("You given key id \""+keyid+"\"\nfor authentification could not be found in selected keystore.\nPlease select a valid key.");
					return;
				}
			}
			if (mysigning==null) {
				mysigning = selectPrivateSigningKey(store);	
			}
			
		} catch (Exception e1) {
			Dialogs.showMessage("Error opening keystore:\n"+f.getAbsolutePath());
		}
		if (mysigning==null) return;
		mysigning.unlockPrivateKey(mh);
		
		if (!mysigning.isPrivateKeyUnlocked()) {
			Dialogs.showMessage("Sorry, private is is locked.");
			return;
		}
		
		OSDXFileTransferClient s = new OSDXFileTransferClient(servername, 4221);
		try {
			s.connect(mysigning);
			String dir = "feed_upload_"+SecurityHelper.getFormattedDate(System.currentTimeMillis()).substring(0,20);
			
			//build file structure, SOMEONE might want to change this
			s.mkdir(dir);
			s.cd(dir);
			ByteArrayOutputStream bOut = new ByteArrayOutputStream();
			Document.buildDocument(currentFeed.toElement()).output(bOut);
			s.uploadFile("feed.xml",bOut.toByteArray());
			
			//upload all item files
			Bundle bundle = currentFeed.getBundle(0);
			if (bundle!=null) {
				for (int i=0;i<bundle.getItemsCount();i++) {
					Item item = bundle.getItem(i);
					if (item.getFilesCount()>0) {
						String subdir = "item_"+(i+1)+"_files";
						s.mkdir(subdir);
						s.cd(subdir);
						for (int j=0;j<item.getFilesCount();j++) {
							try {
								File nextFile = new File(item.getFile(j).getLocationPath());
								s.uploadFile(nextFile);
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
						s.cd_up();
					}
				}
			}
			
			s.closeConnection();
			Dialogs.showMessage("Upload of Feed successful.");
		} catch (Exception e) {
			e.printStackTrace();
			Dialogs.showMessage("ERROR: Upload of Feed failed.");
		}
	}
	
	public static OSDXKey selectPrivateSigningKey(KeyApprovingStore store) {
		Vector<OSDXKey> storedPrivateKeys = store.getAllPrivateSigningKeys();
		if (storedPrivateKeys==null || storedPrivateKeys.size()==0) {
			Dialogs.showMessage("Sorry, no private key for signing in keystore");
			return null;
		}
		Vector<String> select = new Vector<String>();
		int[] map = new int[storedPrivateKeys.size()];
		for (int i=0;i<storedPrivateKeys.size();i++) {
			OSDXKey k = storedPrivateKeys.get(i);
			if (k.allowsSigning()) {
				if (k.isMaster()) {
					select.add(k.getKeyID()+", "+((MasterKey)k).getIDEmailAndMnemonic());
				}
				else if (k.isSub()) {
					select.add(k.getKeyID()+" subkey of "+((SubKey)k).getParentKey().getIDEmailAndMnemonic());
				}
				else {
					select.add(k.getKeyID());
				}
				map[select.size()-1] = i;
			}
		}
		int ans = Dialogs.showSelectDialog("Select private key","Please select a private key for signing", select);
		if (ans>=0 && ans<select.size()) {
			return storedPrivateKeys.get(map[ans]);
		}
		return null;
	}
	
	public void notifyChange(MyObservable changesIn) {
		if (treePanel!=null) {
			if (currentFeed != null) {
				EditBusinessObjectTree tree = new EditBusinessObjectTree(currentFeed);
				treePanel.removeAll();
				treePanel.add(new JScrollPane(tree),BorderLayout.CENTER);
			} else {
				treePanel.removeAll();
			}
		}
	}
	
	public void newEmptyFeed() {
		currentFeed = FeedCreator.makeEmptyFeedWithBundle();
		update();
	}
	
	public static String showCountryCodeSelector() {
		

		String message = "Please select a country";
		String head = "Country Code";
		
		JPanel p = new JPanel();
		p.setBorder(new TitledBorder(message));
		p.setLayout(new BorderLayout());
		
		final SelectTerritoiresTree tree = new SelectTerritoiresTree();
		p.add(new JScrollPane(tree), BorderLayout.CENTER);
		final String[] code = new String[] {null};
		

		final JDialog dialog = new JDialog((JFrame)null, head, true);
		dialog.setContentPane(p);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setSize(400, 600);
		
		MyObserver observer = new MyObserver() {
			public void notifyChange(MyObservable changedIn) {
				code[0] = tree.getSelectedCode();
				dialog.setVisible(false);
			}
		};
		tree.addObserver(observer);
		dialog.setVisible(true);
		
		//System.out.println("selected code: "+code[0]);
		return code[0];
		
		//String antw = Dialogs.showInputDialog("Country Code", "Please enter country code");
		//return antw;
	}
	
	
	private static Element genres = null;
	public static Element getGenres() {
		if (genres==null) {
			URL configGenres = FeedCreator.class.getResource("resources/config_genres.xml");
			try {
				genres = Document.fromURL(configGenres).getRootElement();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return genres;
	}
	
	private static Vector<String> lang_codes_names = null;
	private static Vector<String> lang_codes = null;
	public static String showLanguageCodeSelector() {
		if (lang_codes==null) {
	        try {
	        	lang_codes = new Vector<String>();
	        	lang_codes_names = new Vector<String>();
	        	
	            BufferedReader in = new BufferedReader(new InputStreamReader(configLanguageCodes.openStream()));
	            String line = in.readLine(); //header
	            while ((line = in.readLine())!=null) {
	            	String[] t = line.split(",");
	            	t[1] = t[1].toUpperCase();
	            	lang_codes_names.add(t[0]+" :: "+t[1]);
	            	//lang_codes_names.add(" :: "+t[1]);
	            	lang_codes.add(t[1]);
	            }
	            in.close();
	        } catch (IOException ioe) { System.err.println(ioe.toString());}
	        
		}
		int sel = Dialogs.showSelectDialog("Language Code", "Please select language code", lang_codes_names);
		if (sel>=0) {
			return lang_codes.get(sel);
		} else {
			return null;
		}
		//String antw = Dialogs.showInputDialog("Language Code", "Please enter language code");
		//return antw;
	}
	
	protected JComponent makeTextPanel(String text) {
        JPanel panel = new JPanel(false);
        JLabel filler = new JLabel(text);
        filler.setHorizontalAlignment(JLabel.CENTER);
        panel.setLayout(new GridLayout(1, 1));
        panel.add(filler);
        return panel;
    }
    
    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = FeedGui.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
    
    public StatusBar makeStatusBar() {
    	status = new StatusBar();
    	status.setStatus("Init.");
    	return status;
    }
    
//    private void readAndSetGenres() {
//    	try {
//			Element root = Document.fromURL(configGenres).getRootElement();
//			Vector<String> genres = new Vector<String>();
//			for (Element e : root.getChildren("genre")) {
//				String name = e.getChildTextNN("name");
//				genres.add(name);
//				Element e2 = e.getChild("subgenres");
//				if (e2!=null) {
//					for (Element e3 : e2.getChildren()) {
//						String subname = e3.getChildText("name");
//						genres.add(name+" :: "+subname);
//					}
//				}
//			}
//			bundled_items_panel.setAvailableGenres(genres);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//    }
    
	private void buildUi() {
		BorderLayout bl = new BorderLayout();
		JPanel jp = new JPanel();
		setContentPane(jp);
		jp.setLayout(bl);
		
		
//		GridBagLayout gb = new GridBagLayout();
//		
//		JPanel jp = new JPanel();
//		setContentPane(jp);
//		
//		jp.setLayout(gb);
//		
//		GridBagConstraints c = new GridBagConstraints();
//		c.gridx = 0;
//		c.gridy = 0;
//		
//		JButton test = new JButton("test");
//		jp.add(test, c);
//		

		jt = new JTabbedPane();
		JTabbedPane tabbedPane = jt; //ref.
//		ImageIcon icon = createImageIcon("images/middle.gif");

		//feedinfo_panel = new FeedInfoPanel(this);
		//bundle_panel = new BundlePanel(this);
		//bundled_items_panel = new BundledItemsPanel(this);
		
		feedinfo_panel = new PanelFeedInfo();
		bundle_panel = new PanelBundle();
		bundled_items_panel = new PanelItems();
		
		//readAndSetGenres();
		
		//observe changes
		feedinfo_panel.addObserver(this);
		bundle_panel.addObserver(this);
		bundle_panel.addObserver(bundled_items_panel); //watch out for changes in contributors
		bundled_items_panel.addObserver(this);
		
		
		treePanel = new JPanel();
		treePanel.setLayout(new BorderLayout());
		
		tabbedPane.addTab("FeedInfo", null, new JScrollPane(feedinfo_panel), "");
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

		tabbedPane.addTab("Bundle", null,new JScrollPane(bundle_panel), "");
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

		tabbedPane.addTab("BundledItems", null, new JScrollPane(bundled_items_panel), "");
		tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);

		tabbedPane.addTab("Tree", null, treePanel, "");
		tabbedPane.setMnemonicAt(3, KeyEvent.VK_4);

		jp.add(jt, BorderLayout.CENTER);
		
		StatusBar sb = makeStatusBar(); //also sets. class-variable.
		jp.add(sb, BorderLayout.SOUTH);
		
		initTooltips();
		newEmptyFeed();
	}
	
	public Feed getCurrentFeed() {
		return currentFeed;
	}
	
	public void update() {
		if (feedinfo_panel!=null) {
			feedinfo_panel.update(currentFeed.getFeedinfo());
		}
		if (bundle_panel!=null) {
			if (currentFeed.getBundle(0)!=null) {
				bundle_panel.update(currentFeed.getBundle(0));
			}
		}
		if (bundled_items_panel!=null) {
			bundled_items_panel.update(currentFeed.getBundle(0));
		}
		if (treePanel!=null) {
			if (currentFeed != null) {
				EditBusinessObjectTree tree = new EditBusinessObjectTree(currentFeed);
				treePanel.removeAll();
				treePanel.add(new JScrollPane(tree),BorderLayout.CENTER);
			} else {
				treePanel.removeAll();
			}
		}
	}
	
	public void init_example_feed() {
		currentFeed = FeedCreator.makeExampleFeed();
		long now = System.currentTimeMillis();
		Receiver receiver = Receiver.make(Receiver.TRANSFER_TYPE_OSDX_FILESERVER)
			.servername("localhost")
			.serveripv4("127.0.0.1")
			.authtype(Receiver.AUTH_TYPE_KEYFILE);
		currentFeed.getFeedinfo().receiver(receiver);
		currentFeed.getBundle(0).addItem(
				Item.make(IDs.make().amzn("item1 id"), "testitem1", "testitem", "v0.1", "video", "display artist",
						BundleInformation.make(now,now), LicenseBasis.makeAsOnBundle(),null)
						.addFile(ItemFile.make(new File("fnppl_contributor_license.pdf")))
					.tags(ItemTags.make()
						.addGenre("Rock")
					)
						
		);
		currentFeed.getBundle(0).getLicense_basis().getTerritorial()
		.allow("DE")
		.allow("GB")
		.disallow("US");
		update();
	}
	
	public void quit() {
		System.exit(0);
	}
	public static void main(String[] args) {
		//HT 28.02.2011
		//		1. Select/Open Keystore 
		//		2. Save Keystore 
		//		3. Create Key(s) 
		//		4. Create Identities 
		//		5. Modify Identities
		//		6. Add arbitrary (foreign) keys (pubkeys) to keystore
		//		7. Sign arbitrary files
		//		8. Check signature of arbitrary files
		//		10. encrypt arbitrary files (AES)
		//		11. decrypt arbitrary files (AES)
		//		12. Modify Keys (in terms of deletion/revokation/submission to server)

		try {
	        UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
	    } catch(Exception ex){
	        System.out.println("Nimbus look & feel not available");
	    }
		FeedGui s = FeedGui.getInstance();
		s.buildUi();
		s.setVisible(true);
	}
	
	public static class StatusBar extends JPanel {
		String info = null;
		public StatusBar() {
			super();
			setPreferredSize(new Dimension(100, 30));
			setSize(new Dimension(100, 30));
			setMinimumSize(new Dimension(100, 30));
		}
		
		public void setStatus(String msg) { 
			//dirty. fast.
			JLabel jl = new JLabel(msg);
			removeAll();
			add(jl);
			repaint();
		}
	}
}
