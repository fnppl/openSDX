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

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.*;

import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;

import org.fnppl.opensdx.gui.helper.PanelEncrypt;
import org.fnppl.opensdx.gui.helper.PanelKeyLogs;
import org.fnppl.opensdx.gui.helper.PanelSign;
import org.fnppl.opensdx.security.*;
import org.fnppl.opensdx.xml.*;
import org.w3c.dom.ranges.RangeException;


public class SecurityMainFrame extends JFrame {

	private int maxWidth = 1200;

	private SecurityControl control;

	private URL configURL = KeyApprovingStore.class.getResource("resources/config.xml");

	private HashMap<OSDXKey, KeyStatus> key_status = new HashMap<OSDXKey, KeyStatus>();
	private Vector<OSDXKey> storedPrivateKeys = new Vector<OSDXKey>();
	private Vector<OSDXKey> storedPublicKeys = new Vector<OSDXKey>();
	private Vector<OSDXKey> storedTrustedPublicKeys = new Vector<OSDXKey>();
	private JTabbedPane tab = null;
	private JTabbedPane tabsKeyGroups = null;
	
	//menu items
	private JMenuItem jmiCloseKeyStore;
	private JMenuItem jmiSaveKeyStore;
	private JMenuItem jmiWriteKeyStoreToFile;
	private JMenuItem jmiGenerateMaster;
	private JMenuItem jmiGenerateSet;
	private JMenuItem jmiRequestKeys;
	private JMenuItem jmiAddKeyServer;


	private HashMap<String, String> props = new HashMap<String, String>(); //GUI layout properties

	//	private ImageIcon iconUp;
	//	private ImageIcon iconDown;
	//	private ImageIcon iconRemove;

	private static SecurityMainFrame instance = null;
	public static SecurityMainFrame getInstance() {
		if(instance == null) {
			instance = new SecurityMainFrame();
		}

		return instance;
	}

	private SecurityMainFrame() {
		super("fnppl.org :: openSDX :: SecurityMainFrame");		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				quit();
			}
		});
		control = new SecurityControl();
		control.setMessageHandler(new DefaultMessageHandler());
		control.setKeyverificator(KeyVerificator.make());

		setSize(1024, 768);
	}

	//	private void initIcons() {
	//		int w = 20;
	//		int h = 14;
	//		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	//		Graphics2D g = img.createGraphics();
	//		AlphaComposite clear = AlphaComposite.getInstance(AlphaComposite.CLEAR, 1.0F);
	//		AlphaComposite full = AlphaComposite.getInstance(AlphaComposite.DST_OVER, 1.0F);
	//		g.setComposite(clear);
	//		g.fillRect(0,0,w,h);
	//		g.setComposite(full);
	//		g.setColor(Color.BLACK);
	//
	//		int s = 4;
	//		int posP = h*6/10;
	//		int[] xPoints = new int[] {w/2, w   , w/2+s, w/2+s, w/2-s, w/2-s, 0   };
	//		int[] yPoints = new int[] {h  , posP, posP , 0    , 0    , posP , posP};
	//		g.fillPolygon(xPoints, yPoints, xPoints.length);
	//		img.flush();
	//		iconDown = new ImageIcon(img);
	//
	//
	//		posP = h-posP;
	//		img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	//		g = img.createGraphics();
	//		g.setComposite(clear);
	//		g.fillRect(0,0,w,h);
	//		g.setComposite(full);
	//		g.setColor(Color.BLACK);
	//
	//		xPoints = new int[] {w/2, w   , w/2+s, w/2+s, w/2-s, w/2-s, 0   };
	//		yPoints = new int[] {0  , posP, posP , h    , h    , posP , posP};
	//		g.fillPolygon(xPoints, yPoints, xPoints.length);
	//		img.flush();
	//		iconUp = new ImageIcon(img);
	//
	//		posP = h-posP;
	//		img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	//		g = img.createGraphics();
	//		g.setComposite(clear);
	//		g.fillRect(0,0,w,h);
	//		g.setComposite(full);
	//		g.setColor(Color.RED);
	//
	//
	//		xPoints = new int[] {0,s,w/2,w-s,w,  w/2+s/2,  w,w-s,w/2,s,0,   w/2-s/2};
	//		yPoints = new int[] {0,0,h/2-s/2,0,0,    h/2,    h,h,h/2+s/2,h,h,   h/2};
	//		g.fillPolygon(xPoints, yPoints, xPoints.length);
	//		img.flush();
	//		iconRemove = new ImageIcon(img);
	//	}

	public void quit() {
		closeCurrentStore();
		System.exit(0);
	}

	public boolean openDefauktKeyStore() {
		File f = SecurityControl.getDefaultDir();
		f = new File(f, "defaultKeyStore.xml");
		if (f.exists()) return openKeyStore(f);
		return false;
	}

	public boolean openKeyStore(File f) {
		try {
			if(f.exists()) {
				KeyApprovingStore kas = KeyApprovingStore.fromFile(f, control.getMessageHandler());
				control.setKeyStore(kas);	
				control.setKeyverificator(KeyVerificator.make());
				control.resetKeyClients();

				//				MasterKey m = kas.getAllMasterKeys().get(0);
				//				Document.buildDocument(m.toElement(null)).outputCompact(System.out);
				//				Document.buildDocument(m.getRevokeKeys().get(0).toElement(null)).outputCompact(System.out);
				//				Document.buildDocument(m.getSubKeys().get(0).toElement(null)).outputCompact(System.out);
				update();
				return true;
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

	public void makeMenuBar() {
		ActionListener ja = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String cmd = e.getActionCommand();

				if(cmd.equalsIgnoreCase("quit")) {
					quit();
				}
				else if(cmd.equalsIgnoreCase("createnewkeystore")) {
					createKeyStore();
				}
				else if(cmd.equalsIgnoreCase("openkeystore")) {
					openKeystore();
				}
				else if(cmd.equalsIgnoreCase("savekeystore")) {
					writeCurrentKeyStore(false);
				}
				else if(cmd.equalsIgnoreCase("closekeystore")) {
					closeCurrentStore();
				}
				else if(cmd.equalsIgnoreCase("writekeystore")) {
					writeCurrentKeyStore(true);
				}
				else if(cmd.equalsIgnoreCase("addkeyserver")) {
					addKeyServer();
				}
				else if(cmd.equalsIgnoreCase("generatemasterkeyset")) {
					generateMasterKeySet();
				}
				else if(cmd.equalsIgnoreCase("generatemasterkey")) {
					generateMasterKeyPair();
				}
				else if(cmd.equalsIgnoreCase("request keys from server")) {
					requestKeysFromServer();
				}
				else if(cmd.equalsIgnoreCase("encryptfiledialog")) {
					showEncryptFileDialog();
				}
//				else if(cmd.equalsIgnoreCase("encryptfile")) {
//					encryptFile();
//				}
//				else if(cmd.equalsIgnoreCase("arsencryptfile")) {
//					asymmetricEncryptedRandomSymmetricKeyEncryptionOfFile();
//				}
//				else if(cmd.equalsIgnoreCase("aencryptfile")) {
//					asymmetricEncryptionOfFile();
//				}
				else if(cmd.equalsIgnoreCase("decryptfile")) {
					decryptFile();
				}
				else if(cmd.equalsIgnoreCase("signfile")) {
					//signFile();
					showSignFileDialog();
				}
				else if(cmd.equalsIgnoreCase("verifysignature")) {
					verifySignature();
				}
			}

		};

		JMenuBar jb = new JMenuBar();
		JMenu jm = new JMenu("File");
		jb.add(jm);
		JMenuItem jmi = null;

		jmi = new JMenuItem("CreateNewKeyStore");
		jmi.setActionCommand("createnewkeystore");
		jmi.addActionListener(ja);
		jm.add(jmi);

		jmi = new JMenuItem("OpenKeyStore");
		jmi.setActionCommand("openkeystore");
		jmi.addActionListener(ja);
		jm.add(jmi);

		jmiSaveKeyStore = new JMenuItem("SaveKeyStore");
		jmiSaveKeyStore.setActionCommand("savekeystore");
		jmiSaveKeyStore.addActionListener(ja);
		jm.add(jmiSaveKeyStore);

		jmiCloseKeyStore = new JMenuItem("CloseKeyStore");
		jmiCloseKeyStore.setActionCommand("closekeystore");
		jmiCloseKeyStore.addActionListener(ja);
		jm.add(jmiCloseKeyStore);

		jmiWriteKeyStoreToFile = new JMenuItem("WriteKeyStore to new file");
		jmiWriteKeyStoreToFile.setActionCommand("writekeystore");
		jmiWriteKeyStoreToFile.addActionListener(ja);
		jm.add(jmiWriteKeyStoreToFile);

		jmi = new JMenuItem("Quit");
		jmi.setActionCommand("quit");
		jmi.addActionListener(ja);
		jm.add(jmi);


		jm = new JMenu("Keys");
		jb.add(jm);

		jmiGenerateSet = new JMenuItem("Generate new MASTER Key Set");
		jmiGenerateSet.setActionCommand("generatemasterkeyset");
		jmiGenerateSet.addActionListener(ja);
		jm.add(jmiGenerateSet);

		jmiGenerateMaster = new JMenuItem("Generate new MASTER Key");
		jmiGenerateMaster.setActionCommand("generatemasterkey");
		jmiGenerateMaster.addActionListener(ja);
		jm.add(jmiGenerateMaster);

		jm.addSeparator();

		jmiRequestKeys = new JMenuItem("Request Keys from KeyServer");
		jmiRequestKeys.setActionCommand("request keys from server");
		jmiRequestKeys.addActionListener(ja);
		jm.add(jmiRequestKeys);


		jm = new JMenu("KeyServer");
		jmiAddKeyServer = new JMenuItem("add keyserver");
		jmiAddKeyServer.setActionCommand("addkeyserver");
		jmiAddKeyServer.addActionListener(ja);
		jm.add(jmiAddKeyServer);

		jb.add(jm);

		jb.add(jm);


		jm = new JMenu("Signature");
		jb.add(jm);

		jmi = new JMenuItem("VerifySignature");
		jmi.setActionCommand("verifysignature");
		jmi.addActionListener(ja);
		jm.add(jmi);

		jmi = new JMenuItem("SignFile");
		jmi.setActionCommand("signfile");
		jmi.addActionListener(ja);
		jm.add(jmi);

		jm = new JMenu("<html>Encryption<br>Decryption</html>");
		jb.add(jm);

		//		jmi = new JMenuItem("EncryptFile (symmetric)");
		//		jmi.setActionCommand("encryptfile");
		//		jmi.addActionListener(ja);
		//		jm.add(jmi);
		//
		//		jmi = new JMenuItem("EncryptFile (random symm. key encrypted with asymm. encryption)");
		//		jmi.setActionCommand("arsencryptfile");
		//		jmi.addActionListener(ja);
		//		jm.add(jmi);
		//		
		//		jmi = new JMenuItem("EncryptFile (asymmetric)");
		//		jmi.setActionCommand("aencryptfile");
		//		jmi.addActionListener(ja);
		//		jm.add(jmi);

		jmi = new JMenuItem("Encrypt File ...");
		jmi.setActionCommand("encryptfiledialog");
		jmi.addActionListener(ja);
		jm.add(jmi);

		jmi = new JMenuItem("Decrypt File");
		jmi.setActionCommand("decryptfile");
		jmi.addActionListener(ja);
		jm.add(jmi);


		setJMenuBar(jb);
	}

	private void setMenuOptionVisible(boolean keystoreOpend) {
		jmiCloseKeyStore.setEnabled(keystoreOpend);
		jmiSaveKeyStore.setEnabled(keystoreOpend);
		jmiWriteKeyStoreToFile.setEnabled(keystoreOpend);
		jmiGenerateMaster.setEnabled(keystoreOpend);
		jmiGenerateSet.setEnabled(keystoreOpend);
		jmiRequestKeys.setEnabled(keystoreOpend);
		jmiAddKeyServer.setEnabled(keystoreOpend);
	}

	private void buildUi() {
		//initIcons();
		makeMenuBar();
		update();
		Helper.centerMe(this, null);
	}

	private void updateKeyVerificatior() {
		if (control.getKeyStore()==null) {
			control.getKeyverificator().removeAllDirectRatings();
		} else {
			if (control.getKeyStore().getKeyServer()!=null) {
				for (KeyServerIdentity ks : control.getKeyStore().getKeyServer()) {
					for (OSDXKey k : ks.getKnownKeys()) {
						control.getKeyverificator().addKeyRating(k, TrustRatingOfKey.RATING_MARGINAL);
					}
				}
				for (OSDXKey k : storedTrustedPublicKeys) {
					control.getKeyverificator().addKeyRating(k, TrustRatingOfKey.RATING_COMPLETE);
				}
				for (OSDXKey k : storedPrivateKeys) {
					control.getKeyverificator().addKeyRating(k, TrustRatingOfKey.RATING_ULTIMATE);
				}
			}
		}
	}


	public void update() {
		if (control.getKeyStore()==null) {
			setMenuOptionVisible(false);
		} else {
			setMenuOptionVisible(true);
		}
		int lastOpenTab = -1;
		if (tab!=null) lastOpenTab = tab.getSelectedIndex();

		int lastOpenTabPrivateKeys = -1;
		if (tabsKeyGroups!=null) lastOpenTabPrivateKeys = tabsKeyGroups.getSelectedIndex();


		tab = new JTabbedPane();
		setContentPane(tab);

		//		JPanel p = new JPanel();
		//		JScrollPane scroll = new JScrollPane(p);
		//		tab.add("Key Groups", p);
		//		p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));

		storedPrivateKeys = new Vector<OSDXKey>();
		storedPublicKeys = new Vector<OSDXKey>();
		storedTrustedPublicKeys = new Vector<OSDXKey>();

		if (control.getKeyStore()!=null) {

			//keylogs
			Vector<KeyLog> keylogs = control.getKeyStore().getKeyLogs();
			JPanel pKeyLogs = null;
			if (keylogs!=null && keylogs.size()>0) {
				pKeyLogs = new JPanel();
				pKeyLogs.setBorder(new TitledBorder("Keylogs in KeyStore:"));
				pKeyLogs.setLayout(new BoxLayout(pKeyLogs, BoxLayout.PAGE_AXIS));
				for (KeyLog keylog : keylogs) {
					pKeyLogs.add(buildComponentKeyLog(keylog,false));
				}
			}

			//keys
			JPanel p = new JPanel();
			//p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
			//JScrollPane scroll = new JScrollPane(p);
			tab.add("My Private Keys", p);

			tabsKeyGroups = new JTabbedPane();
			BorderLayout layout = new BorderLayout();
			p.setLayout(layout);
			p.add(tabsKeyGroups,BorderLayout.CENTER);


			Vector<OSDXKey> all = control.getKeyStore().getAllKeys();
			int y = 0;
			for (int i=0;i<all.size();i++) {
				OSDXKey key = all.get(i);
				if (key instanceof MasterKey && key.isMaster() && key.hasPrivateKey()) {
					Vector<RevokeKey> revokekeys = control.getKeyStore().getRevokeKeys(key.getKeyID());
					Vector<SubKey> subkeys = control.getKeyStore().getSubKeys(key.getKeyID());
					storedPrivateKeys.add(key);
					storedPrivateKeys.addAll(subkeys);
					Component comp = buildComponent((MasterKey)key, revokekeys, subkeys);
					String identities = ((MasterKey)key).getIDEmailAndMnemonic();
					String tabName = "KeyGroup:"+(identities!=null?"   "+identities:"");
					tabsKeyGroups.add(tabName, new JScrollPane(comp));
					//p.add(comp);
					//y++;
				} else {
					if (!key.hasPrivateKey()) {
						storedPublicKeys.add(key);
					}
				}
			}
			if (lastOpenTabPrivateKeys>=0 && lastOpenTabPrivateKeys<tabsKeyGroups.getTabCount()) {
				tabsKeyGroups.setSelectedIndex(lastOpenTabPrivateKeys);
			}
			// end of private keys

			//divide storedkeys in trusted and unrated
			for (int i=0;i<storedPublicKeys.size();i++) {
				OSDXKey key = storedPublicKeys.get(i);
				//if approved keylog -> trusted else stored
				boolean approved = false;
				Vector<KeyLog> klogs = control.getKeyStore().getKeyLogs(key.getKeyID());
				for (KeyLog klog : klogs) {
					//System.out.println("keylog: "+klog.getKeyIDFrom()+"  to "+klog.getKeyIDTo());
					if (isStoredPrivateKey(klog.getKeyIDFrom())) {
						//System.out.println("private from key");
						if (klog.getAction().equals(KeyLogAction.APPROVAL)) {
							approved = true;
						}
						else if (klog.getAction().equals(KeyLogAction.REVOCATION)) {
							approved = false;
						}
					} else {
						//System.out.println("NOT private from key");
					}
				}
				if (approved) {
					storedTrustedPublicKeys.add(key);
					storedPublicKeys.remove(i);
					i--;
				}
			}

			//known public keys from keystore
			p = new JPanel();
			p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
			JScrollPane scroll = new JScrollPane(p);
			tab.add("Known Public Keys", scroll);

			if (storedTrustedPublicKeys!=null && storedTrustedPublicKeys.size()>0) {
				p.add(buildComponentTrustedKeys(storedTrustedPublicKeys));
			}
			p.add(buildComponentKnownKeys(storedPublicKeys));


			//keylogs
			p = new JPanel();
			p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
			scroll = new JScrollPane(p);
			tab.add("KeyLogs", scroll);
			if (pKeyLogs!=null) {
				p.add(pKeyLogs);
			}

			//keylogs new
			p = new JPanel();
			p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
			//scroll = new JScrollPane(p);
			tab.add("KeyLogs (sorted)", p);
			PanelKeyLogs kl = new PanelKeyLogs(this);
			kl.updateKeyLogs(control.getKeyStore());
			p.add(kl);


			//keyserver
			p = new JPanel();
			p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
			scroll = new JScrollPane(p);
			tab.add("Key Server", scroll);
			p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
			Vector<KeyServerIdentity> keyservers = control.getKeyStore().getKeyServer();
			if (keyservers!=null) {
				JPanel pks = new JPanel();
				pks.setBorder(new TitledBorder("KeyServers:"));
				pks.setLayout(new BoxLayout(pks, BoxLayout.PAGE_AXIS));
				for (KeyServerIdentity ksid : keyservers) {
					pks.add(buildComponentKeyServer(ksid));
				}
				p.add(pks);
			}
		}

		//		if (knownpublickeys!=null) {
		//			JPanel pk = new JPanel();
		//			pk.setBorder(new TitledBorder("Known Public Keys:"));
		//			pk.setLayout(new BoxLayout(pk, BoxLayout.PAGE_AXIS));
		//			pk.add(buildComponentKnownKeys(knownpublickeys));
		//			p.add(pk);
		//		}
		updateKeyVerificatior();
		validate();
		if (lastOpenTab>=0 && lastOpenTab<tab.getTabCount()) {
			tab.setSelectedIndex(lastOpenTab);
		}

	}

	private boolean isStoredPrivateKey(String keyid) {
		if (storedPrivateKeys==null) return false;
		for (OSDXKey k : storedPrivateKeys) {
			if (k.getKeyID().equals(keyid)) {
				return true;
			}
		}
		return false;
	}

	private Component buildComponent(MasterKey masterkey, Vector<RevokeKey> revokekeys, Vector<SubKey> subkeys) {
		final JPanel p = new JPanel();
		String identities = masterkey.getIDEmailAndMnemonic();
		//p.setBorder(new TitledBorder("KeyGroup:"+(identities!=null?"   "+identities:"")));

		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(buildComponentMasterKey(masterkey));
		for (RevokeKey key : revokekeys) {
			p.add(buildComponentRevokeKey(key));
		}
		for (SubKey key : subkeys) {
			p.add(buildComponentSubKey(key));
		}
		return p;
	}

	private Component buildComponentMasterKey(final MasterKey key) {
		final JPanel p = new JPanel();
		p.setLayout(new BorderLayout());

		final JPanel content = new JPanel();
		content.setLayout(new BorderLayout());

		JPanel a = new JPanel();
		int y = 0;
		GridBagLayout gb = new GridBagLayout();		
		a.setLayout(gb);

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.insets = new Insets(5, 5, 0, 0);

		addLabelTextFieldPart("Key ID:", key.getKeyID(), a, c, y); y++;
		addLabelTextFieldPart("usage:", key.getUsageName(), a, c, y); y++;
		addLabelTextFieldPart("usage restriction", key.getUsageRestriction(), a, c, y, false); y++;
		addLabelTextAreaPart("usage note", key.getUsageNote(), a, c, y, false); y++;
		addLabelTextFieldPart("valid_from:", key.getValidFromString(), a, c, y); y++;
		final JTextField tValid = addLabelTextFieldPart("valid_until:", key.getValidUntilString(), a, c, y,true); y++;
		final JTextField tAuth = addLabelTextFieldPart("authoritative keyserver:", key.getAuthoritativekeyserver(), a, c, y, true);

		tValid.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode()==10) {//enter pressed
					try {
						String v = tValid.getText();
						long datetime = SecurityHelper.parseDate(v);
						tValid.setText(SecurityHelper.getFormattedDate(datetime));
						key.setValidUntil(datetime);
					} catch (Exception ex) {
						Dialogs.showMessage("Sorry, wrong date format.");
						tValid.setText(key.getValidUntilString());
					}
					tValid.setBackground(Color.WHITE);
				}
			}
			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {}
		});
		DocumentListener chListen = new DocumentListener() {
			public void removeUpdate(DocumentEvent e) {action();}
			public void insertUpdate(DocumentEvent e) {action();}
			public void changedUpdate(DocumentEvent e) {action();}
			private void action() {
				if (key.getValidUntilString().equals(tValid.getText())) {
					tValid.setBackground(Color.WHITE);
				} else {
					tValid.setBackground(Color.YELLOW);
				}
			}
		};
		tValid.getDocument().addDocumentListener(chListen);

		tAuth.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode()==10) {//enter pressed
					String v = tAuth.getText();
					key.setAuthoritativeKeyServer(v);
					//tAuth.setBackground(Color.WHITE);
					update();
				}
			}
			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {}
		});
		DocumentListener chAuthListen = new DocumentListener() {
			public void removeUpdate(DocumentEvent e) {action();}
			public void insertUpdate(DocumentEvent e) {action();}
			public void changedUpdate(DocumentEvent e) {action();}
			private void action() {
				if (key.getAuthoritativekeyserver().equals(tAuth.getText())) {
					tAuth.setBackground(Color.WHITE);
				} else {
					tAuth.setBackground(Color.YELLOW);
				}
			}
		};
		tAuth.getDocument().addDocumentListener(chAuthListen);

		final Vector<Identity> ids = key.getIdentities();
		if (ids.size()>0) {
			ActionListener editRemoveListener = new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					String cmd = e.getActionCommand();
					if (cmd.indexOf(":")<=0) return;
					String[] sno = cmd.split(":");
					String c = sno[0]; 
					int no = Integer.parseInt(sno[1]);
					if (c.equals("remove")) {
						String txt = "Are you sure you want to remove the following id:\n"+ids.get(no).getEmail()+"?";
						int a = Dialogs.showYES_NO_Dialog("Confirm removal",txt);
						if (a==Dialogs.YES) {
							key.removeIdentity(ids.get(no));
							update();
						}
					} else if (c.equals("edit")) {
						Identity id = ids.get(no).derive();
						id.setIdentNum(key.getIdentities().size()+1);
						boolean ok = showIdentityEditDialog(id, true);
						if (ok) {
							key.addIdentity(id);
							update();
						}
					} else if (c.equals("up")) {
						key.moveIdentityAtPositionUp(no);
						update();
					}  else if (c.equals("down")) {
						key.moveIdentityAtPositionDown(no);
						update();
					}
				}
			};
			for (int i=ids.size()-1;i>=0;i--) {
				y++;
				addIdentityPart(i, ids.size()-1, ids.get(i), a, c, y, editRemoveListener, (i==ids.size()-1));
			}
		}

		Vector<DataSourceStep> dp = key.getDatapath();
		for (int i=0;i<dp.size();i++) {
			y++;
			DataSourceStep s = dp.get(i);
			addLabelTextFieldPart("datapath "+(i+1)+":", s.getDataSource()+" at "+s.getDataInsertDatetimeString(), a, c, y);
		}


		final int w = 600;
		final int h = y*30 + 120+40;

		JButton head = createHeaderButton("MASTER Key:       "+key.getKeyID(), key.getKeyID(), content, p, w, h);

		JPanel b = new JPanel();
		//		b.setLayout(new FlowLayout(FlowLayout.LEFT));
		b.setLayout(new GridLayout(2, 4));

		int buWidth = 150;
		int buWidth2 = 130;

		JButton bu;

		if (key.getIdentities().size()==0) {
			bu = new JButton("set identity");
			bu.setPreferredSize(new Dimension(buWidth,25));
			bu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						Identity id = Identity.newEmptyIdentity();
						id.setIdentNum(key.getIdentities().size()+1);
						boolean ok = showIdentityEditDialog(id, true);
						if (ok) {
							key.addIdentity(id);
							update();
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			});
			b.add(bu);
		}

		bu = new JButton("generate REVOKE Key");
		bu.setPreferredSize(new Dimension(buWidth,25));
		bu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				generateRevokeKey(key);
			}
		});
		b.add(bu);

		bu = new JButton("generate SUB Key");
		bu.setPreferredSize(new Dimension(buWidth,25));
		bu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				generateSubKey(key);
			}
		});
		b.add(bu);

		bu = new JButton("upload to KeyServer");
		bu.setPreferredSize(new Dimension(buWidth,25));
		bu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uploadMasterKeyToKeyServer(key);
			}
		});
		b.add(bu);

		bu = new JButton("remove");
		bu.setPreferredSize(new Dimension(buWidth2,25));
		bu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeKey(key);
			}
		});
		b.add(bu);

		bu = new JButton("generate keylog");
		bu.setPreferredSize(new Dimension(buWidth2,25));
		bu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showGenerateKeyLogDialog(key);
			}
		});
		b.add(bu);

		bu = new JButton("request keylogs");
		bu.setPreferredSize(new Dimension(buWidth2,25));
		bu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				requestKeyLogs(key);
			}
		});
		b.add(bu);

		bu = new JButton("request subkeys");
		bu.setPreferredSize(new Dimension(buWidth2,25));
		bu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				requestSubKeys(key);
			}
		});
		b.add(bu);

		p.add(head, BorderLayout.NORTH);

		JScrollPane scrollContent = new JScrollPane(content);
		p.add(scrollContent, BorderLayout.CENTER);

		content.add(a,BorderLayout.CENTER);
		content.add(b,BorderLayout.SOUTH);

		return p;
	}

	private void addLabelTextFieldPart(String textLabel, String textValue, JPanel a, GridBagConstraints c, int y) {
		addLabelTextFieldPart(textLabel, textValue, a, c, y, false);
	}

	private JTextField addLabelTextFieldPart(String textLabel, String textValue, JPanel a, GridBagConstraints c, int y, boolean edit) {
		JLabel l = new JLabel(textLabel);
		l.setPreferredSize(new Dimension(200,20));
		c.weightx = 0;
		c.weighty = 0.1;
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		c.gridy = y;
		a.add(l, c);

		JTextField t = new JTextField(textValue);
		t.setEditable(edit);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = y;
		c.gridwidth = 5;
		a.add(t,c);
		return t;
	}

	private JTextArea addLabelTextAreaPart(String textLabel, String textValue, JPanel a, GridBagConstraints c, int y, boolean edit) {
		JLabel l = new JLabel(textLabel);
		l.setPreferredSize(new Dimension(200,20));
		c.weightx = 0;
		c.weighty = 0.1;
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		c.gridy = y;
		a.add(l, c);

		JTextArea t = new JTextArea(textValue);
		t.setEditable(edit);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = y;
		c.gridwidth = 5;
		c.gridheight = 1;
		a.add(t,c);
		return t;
	}

	private JComboBox addLabelComboBoxPart(String textLabel, Vector<String> textItems, int selected, JPanel a, GridBagConstraints c, int y, boolean edit) {
		JLabel l = new JLabel(textLabel);
		l.setPreferredSize(new Dimension(200,20));
		c.weightx = 0;
		c.weighty = 0.1;
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		c.gridy = y;
		a.add(l, c);

		JComboBox t = new JComboBox(textItems);
		t.setSelectedIndex(selected);
		t.setEditable(edit);
		c.fill = GridBagConstraints.NONE;//GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = y;
		c.gridwidth = 1;
		a.add(t,c);
		return t;
	}

	private void addIdentityPart(int i, int maxI, Identity id, JPanel a, GridBagConstraints c, int y, ActionListener al, boolean canEdit) {

		JLabel l = new JLabel("identity "+(i+1)+":");
		c.weightx = 0;
		c.weighty = 0.1;
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		c.gridy = y;
		a.add(l, c);

		JTextField t = new JTextField(id.getIdentNumString()+"    "+id.getEmail());
		t.setEditable(false);
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = y;
		c.gridwidth = 1;
		a.add(t,c);
		JButton b;
		if (canEdit) {
			b = new JButton("edit");
			b.setActionCommand("edit:"+i);
			b.addActionListener(al);
			b.setPreferredSize(new Dimension(80, 20));
			c.weightx = 0;
			c.fill = GridBagConstraints.NONE;
			c.gridx = 2;
			c.gridy = y;
			c.gridwidth = 1;
			a.add(b,c);
		}

		//		b = new JButton(iconRemove);
		//		b.setActionCommand("remove:"+i);
		//		b.setToolTipText("remove identitiy");
		//		b.addActionListener(al);
		//		b.setPreferredSize(new Dimension(30, 20));
		//		c.weightx = 0;
		//		c.fill = GridBagConstraints.NONE;
		//		c.gridx = 3;
		//		c.gridy = y;
		//		c.gridwidth = 1;
		//		a.add(b,c);

		//		if (i!=0) {
		//			b = new JButton(iconUp);
		//			b.setActionCommand("up:"+i);
		//			b.addActionListener(al);
		//			b.setPreferredSize(new Dimension(30, 20));
		//			c.weightx = 0;
		//			c.fill = GridBagConstraints.NONE;
		//			c.gridx = 4;
		//			c.gridy = y;
		//			c.gridwidth = 1;
		//			a.add(b,c);
		//		}
		//		if (i<maxI) {
		//			b = new JButton(iconDown);
		//			b.setActionCommand("down:"+i);
		//			b.addActionListener(al);
		//			b.setPreferredSize(new Dimension(30, 20));
		//			c.weightx = 0;
		//			c.fill = GridBagConstraints.NONE;
		//			c.gridx = 5;
		//			c.gridy = y;
		//			c.gridwidth = 1;
		//			a.add(b,c);
		//		}
	}

	private Component buildComponentRevokeKey(final RevokeKey key) {
		final JPanel p = new JPanel();
		p.setLayout(new BorderLayout());

		final JPanel content = new JPanel();
		content.setLayout(new BorderLayout());

		JPanel a = new JPanel();
		int y = 0;
		GridBagLayout gb = new GridBagLayout();		
		a.setLayout(gb);

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.insets = new Insets(5, 5, 0, 0);

		addLabelTextFieldPart("Key ID:", key.getKeyID(), a, c, y); y++;
		addLabelTextFieldPart("usage:", key.getUsageName(), a, c, y); y++;
		addLabelTextFieldPart("valid_from:", key.getValidFromString(), a, c, y); y++;
		final JTextField tValid = addLabelTextFieldPart("valid_until:", key.getValidUntilString(), a, c, y,true); y++;
		addLabelTextFieldPart("authoritative keyserver:", key.getAuthoritativekeyserver(), a, c, y);
		tValid.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode()==10) {//enter pressed
					try {
						String v = tValid.getText();
						long datetime = SecurityHelper.parseDate(v);
						tValid.setText(SecurityHelper.getFormattedDate(datetime));
						key.setValidUntil(datetime);
					} catch (Exception ex) {
						Dialogs.showMessage("Sorry, wrong date format.");
						tValid.setText(key.getValidUntilString());
					}
					tValid.setBackground(Color.WHITE);
				}
			}
			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {}
		});
		DocumentListener chListen = new DocumentListener() {
			public void removeUpdate(DocumentEvent e) {action();}
			public void insertUpdate(DocumentEvent e) {action();}
			public void changedUpdate(DocumentEvent e) {action();}
			private void action() {
				if (key.getValidUntilString().equals(tValid.getText())) {
					tValid.setBackground(Color.WHITE);
				} else {
					tValid.setBackground(Color.YELLOW);
				}
			}
		};
		tValid.getDocument().addDocumentListener(chListen);

		Vector<DataSourceStep> dp = key.getDatapath();
		for (int i=0;i<dp.size();i++) {
			y++;
			DataSourceStep s = dp.get(i);
			addLabelTextFieldPart("datapath "+(i+1)+":", s.getDataSource()+" at "+s.getDataInsertDatetimeString(), a, c, y);
		}


		final int w = 600;
		final int h = y*30 + 120;

		JButton head = createHeaderButton("REVOKE Key:      "+key.getKeyID(), key.getKeyID(), content, p, w, h);

		JPanel b = new JPanel();
		b.setLayout(new FlowLayout(FlowLayout.LEFT));
		JButton bu = new JButton("upload to keyserver");

		String parent = key.getParentKeyID();
		if (parent.toLowerCase().endsWith("@local")) {
			bu.setEnabled(false);
			bu.setToolTipText("Can only upload if authoritative keyserver of MASTER Key is not LOCAL");
		}

		bu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uploadRevokeKeyToKeyServer(key);
			}
		});
		b.add(bu);

		bu = new JButton("revoke masterkey on keyserver");
		if (key.getAuthoritativekeyserver().toLowerCase().equals("local")) {
			bu.setEnabled(false);
			bu.setToolTipText("Can only revoke if authoritative keyserver of is not LOCAL");
		}
		bu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				revokeMasterKeyWithRevokeKey(key);
			}
		});
		b.add(bu);

		bu = new JButton("remove");
		bu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeKey(key);
			}
		});
		b.add(bu);


		content.add(b,BorderLayout.SOUTH);

		p.add(head, BorderLayout.NORTH);
		JScrollPane scrollContent = new JScrollPane(content);
		p.add(scrollContent, BorderLayout.CENTER);
		content.add(a,BorderLayout.CENTER);

		return p;
	}

	
	private JButton createHeaderButton(final String title, final String keyID, final JPanel content, final JPanel p, final int w,final int h) {
		return createHeaderButton(title, keyID, content, p, w, h,null); 
	}
	
	private JButton createHeaderButton(final String title, final String keyID, final JPanel content, final JPanel p, final int w,final int h, String tooltipText) {

		final JButton head = new JButton("+   "+title);
		if (tooltipText!=null) {
			head.setToolTipText(tooltipText);
		}
		String visible = props.get(keyID);
		if (visible==null || visible.equals("NOT VISIBLE")) {
			props.put(keyID,"NOT VISIBLE");
			head.setText("+   "+title);
			p.setPreferredSize(new Dimension(w,28));
			p.setMinimumSize(new Dimension(10,28));
			p.setMaximumSize(new Dimension(maxWidth,28));
			content.setVisible(false);
		} else {
			props.put(keyID,"VISIBLE");
			head.setText("-   "+title);
			p.setPreferredSize(new Dimension(w,h));
			p.setMinimumSize(new Dimension(10,28));
			p.setMaximumSize(new Dimension(maxWidth,h));
			content.setVisible(true);
		}

		head.setHorizontalAlignment(JButton.LEFT);
		head.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (content.isVisible()) {
					props.put(keyID,"NOT VISIBLE");
					head.setText("+   "+title);
					p.setPreferredSize(new Dimension(w,28));
					p.setMinimumSize(new Dimension(10,28));
					p.setMaximumSize(new Dimension(maxWidth,28));
					content.setVisible(false);
				} else {
					props.put(keyID,"VISIBLE");
					head.setText("-   "+title);
					p.setPreferredSize(new Dimension(w,h));
					p.setMinimumSize(new Dimension(10,28));
					p.setMaximumSize(new Dimension(maxWidth,h));
					content.setVisible(true);
					props.put(keyID,"VISIBLE");
				}
			}
		});
		head.setPreferredSize(new Dimension(w,28));
		head.setMinimumSize(new Dimension(10,28));
		head.setMaximumSize(new Dimension(maxWidth,28));
		return head;
	}

	private Component buildComponentSubKey(final SubKey key) {
		final JPanel p = new JPanel();
		p.setLayout(new BorderLayout());

		final JPanel content = new JPanel();
		content.setLayout(new BorderLayout());

		JPanel a = new JPanel();
		int y = 0;
		GridBagLayout gb = new GridBagLayout();		
		a.setLayout(gb);

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.insets = new Insets(5, 5, 0, 0);

		//		boolean usage_editable = false;
		//		if (key.getAuthoritativekeyserver().equalsIgnoreCase("LOCAL") && key.getUsageNote()==null && key.getUsageRestriction()==null) {
		//			usage_editable = true;
		//		}
		addLabelTextFieldPart("Key ID:", key.getKeyID(), a, c, y); y++;
		final JComboBox cUsage = addLabelComboBoxPart("usage:", OSDXKey.usage_name, key.getUsage(), a, c, y,false); y++;
		addLabelTextFieldPart("usage restriction:", key.getUsageRestriction(), a, c, y,false); y++;
		addLabelTextAreaPart("usage note:", key.getUsageNote(), a, c, y,false); y++;
		addLabelTextFieldPart("valid from:", key.getValidFromString(), a, c, y); y++;
		final JTextField tValid = addLabelTextFieldPart("valid until:", key.getValidUntilString(), a, c, y,true); y++;
		addLabelTextFieldPart("authoritative keyserver:", key.getAuthoritativekeyserver(), a, c, y);
		tValid.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode()==10) {//enter pressed
					try {
						String v = tValid.getText();
						long datetime = SecurityHelper.parseDate(v);
						tValid.setText(SecurityHelper.getFormattedDate(datetime));
						key.setValidUntil(datetime);
					} catch (Exception ex) {
						Dialogs.showMessage("Sorry, wrong date format.");
						tValid.setText(key.getValidUntilString());
					}
					tValid.setBackground(Color.WHITE);
				}
			}
			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {}
		});
		DocumentListener chListen = new DocumentListener() {
			public void removeUpdate(DocumentEvent e) {action();}
			public void insertUpdate(DocumentEvent e) {action();}
			public void changedUpdate(DocumentEvent e) {action();}
			private void action() {
				if (key.getValidUntilString().equals(tValid.getText())) {
					tValid.setBackground(Color.WHITE);
				} else {
					tValid.setBackground(Color.YELLOW);
				}
			}
		};
		tValid.getDocument().addDocumentListener(chListen);
		//		if (usage_editable) {
		//			tUsageNote.addKeyListener(new KeyListener() {
		//				public void keyPressed(KeyEvent e) {
		//					if (e.getKeyCode()==10) {//enter pressed
		//						key.setUsageNote(tUsageNote.getText());
		//						tUsageNote.setBackground(Color.WHITE);
		//						tUsageNote.setEditable(false);
		//					}
		//				}
		//				public void keyReleased(KeyEvent e) {}
		//				public void keyTyped(KeyEvent e) {}
		//			});
		//			chListen = new DocumentListener() {
		//				public void removeUpdate(DocumentEvent e) {action();}
		//				public void insertUpdate(DocumentEvent e) {action();}
		//				public void changedUpdate(DocumentEvent e) {action();}
		//				private void action() {
		//					String t = tUsageNote.getText();
		//					if ((key.getUsageNote()==null && t.length()==0) || (key.getUsageNote()!=null && key.getUsageNote().equals(t))) {
		//						tUsageNote.setBackground(Color.WHITE);
		//					} else {
		//						tUsageNote.setBackground(Color.YELLOW);
		//					}
		//				}
		//			};
		//			tUsageNote.getDocument().addDocumentListener(chListen);
		//			
		//			tUsageRestriction.addKeyListener(new KeyListener() {
		//				public void keyPressed(KeyEvent e) {
		//					if (e.getKeyCode()==10) {//enter pressed
		//						key.setUsageRestricton(tUsageRestriction.getText());
		//						tUsageRestriction.setBackground(Color.WHITE);
		//						tUsageRestriction.setEditable(false);
		//					}
		//				}
		//				public void keyReleased(KeyEvent e) {}
		//				public void keyTyped(KeyEvent e) {}
		//			});
		//			chListen = new DocumentListener() {
		//				public void removeUpdate(DocumentEvent e) {action();}
		//				public void insertUpdate(DocumentEvent e) {action();}
		//				public void changedUpdate(DocumentEvent e) {action();}
		//				private void action() {
		//					String t = tUsageRestriction.getText();
		//					if ((key.getUsageRestriction()==null && t.length()==0) || (key.getUsageRestriction()!=null && key.getUsageRestriction().equals(t))) {
		//						tUsageRestriction.setBackground(Color.WHITE);
		//					} else {
		//						tUsageRestriction.setBackground(Color.YELLOW);
		//					}
		//				}
		//			};
		//			tUsageRestriction.getDocument().addDocumentListener(chListen);
		//		}

		Vector<DataSourceStep> dp = key.getDatapath();
		for (int i=0;i<dp.size();i++) {
			y++;
			DataSourceStep s = dp.get(i);
			addLabelTextFieldPart("datapath "+(i+1)+":", s.getDataSource()+" at "+s.getDataInsertDatetimeString(), a, c, y);
		}
		cUsage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				key.setUsage(cUsage.getSelectedIndex());
			}
		});


		final int w = 600;
		final int h = y*30 + 120;

		JButton head = createHeaderButton("SUB Key:      "+key.getKeyID(), key.getKeyID(), content, p, w, h);

		JPanel b = new JPanel();
		b.setLayout(new FlowLayout(FlowLayout.LEFT));
		JButton bu = new JButton("sign file");
		bu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File f = Dialogs.chooseOpenFile("Please select file for signing", control.getLastDir(), "");
				if (f!=null && f.exists()) {
					control.setLastDir(f.getParentFile());
					signFile(key,f,null);
				}
			}
		});
		b.add(bu);

		bu = new JButton("upload to keyserver");
		bu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uploadSubKeyToKeyServer(key);
			}
		});
		b.add(bu);

		bu = new JButton("remove");
		bu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeKey(key);
			}
		});
		b.add(bu);

		content.add(b,BorderLayout.SOUTH);

		p.add(head, BorderLayout.NORTH);
		JScrollPane scrollContent = new JScrollPane(content);
		p.add(scrollContent, BorderLayout.CENTER);
		content.add(a,BorderLayout.CENTER);

		return p;
	}

	private Component buildComponentKeyServer(final KeyServerIdentity keyserver) {
		final JPanel p = new JPanel();
		p.setLayout(new BorderLayout());

		final JPanel content = new JPanel();
		content.setLayout(new BorderLayout());

		JPanel a = new JPanel();
		int y = 0;
		GridBagLayout gb = new GridBagLayout();		
		a.setLayout(gb);

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.insets = new Insets(5, 5, 0, 0);
		String host = keyserver.getHost();
		int port = keyserver.getPort();
		String prepath = keyserver.getPrepath();

		final JTextField tHost = addLabelTextFieldPart("host:", host, a, c, y,true); y++;
		final JTextField tPort = addLabelTextFieldPart("port:", ""+port, a, c, y,true); y++;
		final JTextField tPrepath = addLabelTextFieldPart("prepath:", ""+prepath, a, c, y,true); y++;

		final JButton bu = new JButton("save changes");
		bu.setEnabled(false);

		DocumentListener chListen = new DocumentListener() {
			public void removeUpdate(DocumentEvent e) {
				action();
			}
			public void insertUpdate(DocumentEvent e) {
				action();
			}
			public void changedUpdate(DocumentEvent e) {
				action();
			}
			private void action() {
				if (tHost.getText().equals(keyserver.getHost()) && tPort.getText().equals(""+keyserver.getPort())  && tPrepath.getText().equals(""+keyserver.getPrepath())) {
					bu.setEnabled(false);
				} else {
					bu.setEnabled(true);
				}
			}
		};
		tHost.getDocument().addDocumentListener(chListen);
		tPort.getDocument().addDocumentListener(chListen);
		tPrepath.getDocument().addDocumentListener(chListen);

		Vector<OSDXKey> keys = keyserver.getKnownKeys();

		for (int i=0;i<keys.size();i++) {
			y++;
			addLabelTextFieldPart("known public key "+(i+1)+":", keys.get(i).getKeyID(), a, c, y);
		}

		final int w = 600;
		final int h = y*30 + 120;

		JButton head = createHeaderButton("KeyServer:      "+host, host+":"+port, content, p, w, h);

		JPanel b = new JPanel();
		b.setLayout(new FlowLayout(FlowLayout.LEFT));
		bu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				keyserver.setHost(tHost.getText());
				try {
					int port = Integer.parseInt(tPort.getText());
					keyserver.setPort(port);
				} catch (Exception ex) {
					tPort.setText(""+keyserver.getPort());
				}
				keyserver.setPrepath(tPrepath.getText());
				props.put(keyserver.getHost()+":"+keyserver.getPort(), "VISIBLE");
				update();
			}
		});

		b.add(bu);

		JButton bTest = new JButton("test settings");
		bTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!testKeyServerSettings(keyserver)) {
					Dialogs.showMessage("Sorry, could not connect to keyserver: "+keyserver.getHost()+", port: "+keyserver.getPort()+"\nPlease check keyserver settings.");
				} else {
					Dialogs.showMessage("Connection to keyserver: "+keyserver.getHost()+", port: "+keyserver.getPort()+"\nsuccessful.");
				}
			}
		});
		b.add(bTest);


		JButton bRemove = new JButton("remove");
		bRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				control.getKeyStore().removeKeyServer(keyserver);
				update();
			}
		});
		b.add(bRemove);


		content.add(b,BorderLayout.SOUTH);

		p.add(head, BorderLayout.NORTH);
		JScrollPane scrollContent = new JScrollPane(content);
		p.add(scrollContent, BorderLayout.CENTER);
		content.add(a,BorderLayout.CENTER);

		return p;
	}

	private Component buildComponentKnownKeys(Vector<OSDXKey> keys) {

		final JPanel p = new JPanel();
		p.setBorder(new TitledBorder("Known public keys"));
		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
		for (OSDXKey key : keys) {
			p.add(buildComponentKnownPubKey(key));
		}

		JPanel buP = new JPanel();
		buP.setLayout(new FlowLayout(FlowLayout.LEFT));

		JButton bu = new JButton("request keys from server");
		//bu.setPreferredSize(new Dimension(buWidth,25));
		bu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				requestKeysFromServer();
			}
		});
		buP.add(bu);
		p.add(buP);

		return p;
	}

	private Component buildComponentTrustedKeys(Vector<OSDXKey> keys) {

		final JPanel p = new JPanel();
		p.setBorder(new TitledBorder("Known public trusted keys"));
		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
		for (OSDXKey key : keys) {
			p.add(buildComponentTrustedPubKey(key));
		}

		//		JPanel buP = new JPanel();
		//		buP.setLayout(new FlowLayout(FlowLayout.LEFT));
		//		
		//		JButton bu = new JButton("request keys from server");
		//		//bu.setPreferredSize(new Dimension(buWidth,25));
		//		bu.addActionListener(new ActionListener() {
		//			public void actionPerformed(ActionEvent e) {
		//				requestKeysFromServer();
		//			}
		//		});
		//		buP.add(bu);
		//		p.add(buP);

		return p;
	}


	private Component buildComponentKnownPubKey(final OSDXKey key) {
		final JPanel p = new JPanel();
		p.setLayout(new BorderLayout());

		final JPanel content = new JPanel();
		content.setLayout(new BorderLayout());


		JPanel a = new JPanel();
		int y = 0;
		GridBagLayout gb = new GridBagLayout();		
		a.setLayout(gb);

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.insets = new Insets(5, 5, 0, 0);

		addLabelTextFieldPart("Key ID:", key.getKeyID(), a, c, y); y++;
		//		KeyStatus ks = key_status.get(key);
		//		if (ks==null) {
		//			addLabelTextFieldPart("status:", "unknown", a, c, y,false); y++;	
		//		} else {
		//			addLabelTextFieldPart("status:", ks.getValidityStatusName(), a, c, y,false); y++;
		//		}
		addLabelTextFieldPart("level:", key.getLevelName(), a, c, y,false); y++;
		if (key instanceof MasterKey) {
			String ids = ((MasterKey)key).getIDEmails();
			if (ids!=null) addLabelTextFieldPart("identities:", ids, a, c, y); y++;
		}
		addLabelTextFieldPart("usage:", key.getUsageName(), a, c, y); y++;
		addLabelTextFieldPart("usage restriction:", key.getUsageRestriction(), a, c, y); y++;
		addLabelTextAreaPart("usage note:", key.getUsageNote(), a, c, y,false); y++;
		addLabelTextFieldPart("valid_from:", key.getValidFromString(), a, c, y); y++;
		addLabelTextFieldPart("valid_until:", key.getValidUntilString(), a, c, y); y++;
		//addLabelTextFieldPart("authoritative keyserver:", key.getAuthoritativekeyserver(), a, c, y);
		if (control.getKeyStore() != null) {
			Vector<KeyLog> logs = control.getKeyStore().getKeyLogs(key.getKeyID());
			if (logs!=null) {
				for (KeyLog kl : logs) {
					y++;
					Component ckl = buildComponentKeyLog(kl, true);
					c.weightx = 1;
					c.weighty = 0.1;
					c.fill = GridBagConstraints.BOTH;
					c.gridx = 0;
					c.gridy = y;
					c.gridwidth = 6;
					a.add(ckl, c);
				}
			}
		}

		Vector<DataSourceStep> dp = key.getDatapath();
		for (int i=0;i<dp.size();i++) {
			y++;
			DataSourceStep s = dp.get(i);
			addLabelTextFieldPart("datapath "+(i+1)+":", s.getDataSource()+" at "+s.getDataInsertDatetimeString(), a, c, y);
		}

		final int w = 800;
		final int h = y*30 + 80;

		String title = "known public key:      "+getKeyIDMnemonicShort(key.getKeyID());
		JButton head = createHeaderButton(title, "known public key:      "+key.getKeyID(), content, p, w, h, key.getKeyID());

		JPanel b = new JPanel();
		b.setLayout(new FlowLayout(FlowLayout.LEFT));

		//		JButton bu = new JButton("update status");
		//		bu.addActionListener(new ActionListener() {
		//			public void actionPerformed(ActionEvent e) {
		//				updateStatus(key);
		//			}
		//		});
		//		b.add(bu);

		JButton bu = new JButton("request keylogs");
		bu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				requestKeyLogs(key);
			}
		});
		b.add(bu);

		if (key.isMaster()) {
			bu = new JButton("request subkeys");
			bu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					requestSubKeys((MasterKey)key);
				}
			});
			b.add(bu);
		} else if (key.isSub() && !key.isRevoke()) {
			bu = new JButton("request parent key");
			bu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					requestParentKey((SubKey)key);
				}
			});
			b.add(bu);
		}

		bu = new JButton("generate keylog");
		bu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showGenerateKeyLogDialog(key);
			}
		});
		b.add(bu);

		bu = new JButton("set as trusted");
		bu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				generateLOCALKeyLog(key,true);
			}
		});
		b.add(bu);

		bu = new JButton("remove");
		bu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				control.getKeyStore().removeKey(key);
				update();
			}
		});
		b.add(bu);


		content.add(b,BorderLayout.SOUTH);

		p.add(head, BorderLayout.NORTH);
		JScrollPane scrollContent = new JScrollPane(content);
		p.add(scrollContent, BorderLayout.CENTER);
		content.add(a,BorderLayout.CENTER);

		return p;
	}
	
	public String getKeyIDMnemonicShort(String keyid) {
		String title = keyid.substring(0,8)+" ... "+keyid.substring(51);
		String emailMnemonic = control.getKeyStore().getEmailAndMnemonic(keyid);
		if (emailMnemonic!=null) {
			title += " :: "+emailMnemonic;
		} else {
			String ks = control.getKeyStore().getKeyServerNameForKey(keyid);
			if (ks!=null) {
				title += " :: KeyServer: "+ks;
			} else {
				title += " :: [unknown]";
			}
		}
		return title;
	}

	private Component buildComponentTrustedPubKey(final OSDXKey key) {
		final JPanel p = new JPanel();
		p.setLayout(new BorderLayout());

		final JPanel content = new JPanel();
		content.setLayout(new BorderLayout());


		JPanel a = new JPanel();
		int y = 0;
		GridBagLayout gb = new GridBagLayout();		
		a.setLayout(gb);

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.insets = new Insets(5, 5, 0, 0);

		addLabelTextFieldPart("Key ID:", key.getKeyID(), a, c, y); y++;
		//		KeyStatus ks = key_status.get(key);
		//		if (ks==null) {
		//			addLabelTextFieldPart("status:", "unknown", a, c, y,false); y++;	
		//		} else {
		//			addLabelTextFieldPart("status:", ks.getValidityStatusName(), a, c, y,false); y++;
		//		}
		addLabelTextFieldPart("level:", key.getLevelName(), a, c, y,false); y++;
		if (key instanceof MasterKey) {
			String ids = ((MasterKey)key).getIDEmails();
			if (ids!=null) addLabelTextFieldPart("identities:", ids, a, c, y); y++;
		}
		addLabelTextFieldPart("usage:", key.getUsageName(), a, c, y); y++;
		addLabelTextFieldPart("usage restriction:", key.getUsageRestriction(), a, c, y); y++;
		addLabelTextAreaPart("usage note:", key.getUsageNote(), a, c, y,false); y++;
		addLabelTextFieldPart("valid_from:", key.getValidFromString(), a, c, y); y++;
		addLabelTextFieldPart("valid_until:", key.getValidUntilString(), a, c, y); y++;
		//		addLabelTextFieldPart("authoritative keyserver:", key.getAuthoritativekeyserver(), a, c, y);
		if (control.getKeyStore() != null) {
			Vector<KeyLog> logs = control.getKeyStore().getKeyLogs(key.getKeyID());
			if (logs!=null) {
				for (KeyLog kl : logs) {
					y++;
					Component ckl = buildComponentKeyLog(kl,true);
					c.weightx = 1;
					c.weighty = 0.1;
					c.fill = GridBagConstraints.BOTH;
					c.gridx = 0;
					c.gridy = y;
					c.gridwidth = 6;
					a.add(ckl, c);
				}
			}
		}

		Vector<DataSourceStep> dp = key.getDatapath();
		for (int i=0;i<dp.size();i++) {
			y++;
			DataSourceStep s = dp.get(i);
			addLabelTextFieldPart("datapath "+(i+1)+":", s.getDataSource()+" at "+s.getDataInsertDatetimeString(), a, c, y);
		}

		final int w = 800;
		final int h = y*30 + 80;
		String title = "known public key:      "+getKeyIDMnemonicShort(key.getKeyID());
		JButton head = createHeaderButton(title, "known public key:      "+key.getKeyID(), content, p, w, h, key.getKeyID());

		JPanel b = new JPanel();
		b.setLayout(new FlowLayout(FlowLayout.LEFT));
		//		JButton bu = new JButton("update status");
		//		bu.addActionListener(new ActionListener() {
		//			public void actionPerformed(ActionEvent e) {
		//				updateStatus(key);
		//			}
		//		});
		//		b.add(bu);

		JButton bu = new JButton("request keylogs");
		bu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				requestKeyLogs(key);
			}
		});
		b.add(bu);

		if (key.isMaster()) {
			bu = new JButton("request subkeys");
			bu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					requestSubKeys((MasterKey)key);
				}
			});
			b.add(bu);
		} else if (key.isSub() && !key.isRevoke()) {
			bu = new JButton("request parent key");
			bu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					requestParentKey((SubKey)key);
				}
			});
			b.add(bu);
		}
		
		bu = new JButton("generate keylog");
		bu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showGenerateKeyLogDialog(key);
			}
		});
		b.add(bu);

		bu = new JButton("remove from trusted");
		bu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				generateLOCALKeyLog(key, false);
			}
		});
		b.add(bu);

		bu = new JButton("remove");
		bu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				control.getKeyStore().removeKey(key);
				update();
			}
		});
		b.add(bu);


		content.add(b,BorderLayout.SOUTH);

		p.add(head, BorderLayout.NORTH);
		JScrollPane scrollContent = new JScrollPane(content);
		p.add(scrollContent, BorderLayout.CENTER);
		content.add(a,BorderLayout.CENTER);

		return p;
	}

	private Component buildComponentKeyLog(final KeyLog keylog, boolean innerPublicKey) {
		final JPanel p = new JPanel();
		p.setLayout(new BorderLayout());

		final JPanel content = new JPanel();
		content.setLayout(new BorderLayout());

		JPanel a = new JPanel();
		int y = 0;
		GridBagLayout gb = new GridBagLayout();		
		a.setLayout(gb);

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.insets = new Insets(5, 5, 0, 0);

		addLabelTextFieldPart("from keyid :", keylog.getKeyIDFrom(), a, c, y);y++;
		addLabelTextFieldPart("to keyid :", keylog.getKeyIDTo(), a, c, y);y++;
		addLabelTextFieldPart("action date :", keylog.getActionDatetimeString(), a, c, y);y++;
		addLabelTextFieldPart("IPv4 :", keylog.getIPv4(), a, c, y);y++;
		addLabelTextFieldPart("IPv6 :", keylog.getIPv6(), a, c, y);y++;
		addLabelTextFieldPart("action :", keylog.getAction(), a, c, y);y++;
		String message = keylog.getMessage();
		if (message!=null && message.length()>0) {
			addLabelTextFieldPart("message :", message, a, c, y);y++;	
		}
		Identity id = keylog.getIdentity();
		if (id !=null) {
			Vector<Element> idsFields = id.getContentElements(true);
			if (idsFields!=null && idsFields.size()>0) {
				for (Element e : idsFields) {
					if (e.getText()!=null) {
						y++;
						String name = e.getName();
						if (name.equals("photo") && !e.getText().equals(Identity.RESTRICTED)) {
							addLabelTextFieldPart("  "+name+":", "[available]", a, c, y);
						} else if (name.equals("note")) {
							addLabelTextAreaPart("  "+name+":", e.getText(), a, c, y, false);
						} else {
							addLabelTextFieldPart("  "+e.getName()+":", e.getText(), a, c, y);
						}
					}
				}
			}
		}
		Vector<DataSourceStep> dp = keylog.getDataPath();
		if (dp!=null) {
			for (int i=0;i<dp.size();i++) {
				y++;
				DataSourceStep s = dp.get(i);
				addLabelTextFieldPart("datapath "+(i+1)+":", s.getDataSource()+" at "+s.getDataInsertDatetimeString(), a, c, y);
			}
		}
		final int w = 600;
		final int h = y*32 + 120;
		String buText = "";
		String tooltip = null;
		if(innerPublicKey) {
			buText = "KeyLog "+keylog.getActionDatetimeString().substring(0,20)+" from KeyID: "+getKeyIDMnemonicShort(keylog.getKeyIDFrom());
			tooltip = keylog.getKeyIDFrom();
		} else {
			buText = "KeyLog "+keylog.getActionDatetimeString().substring(0,20)+" for KeyID: "+getKeyIDMnemonicShort(keylog.getKeyIDTo());
			tooltip = keylog.getKeyIDTo();
		}
		
		JButton head = createHeaderButton(buText, buText , content, p, w, h,tooltip);

		JPanel b = new JPanel();
		b.setLayout(new FlowLayout(FlowLayout.LEFT));
		//		JButton bu = new JButton("upload to keyserver");
		//		bu.addActionListener(new ActionListener() {
		//			public void actionPerformed(ActionEvent e) {
		//				uploadKeyLogToKeyServer(keylog);
		//			}
		//		});
		//		b.add(bu);

		JButton bu = new JButton("remove");
		bu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				control.getKeyStore().removeKeyLog(keylog);
				update();
			}
		});
		b.add(bu);

		bu = new JButton("remove all keylogs with same key id");
		bu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Vector<KeyLog> logs = control.getKeyStore().getKeyLogs(keylog.getKeyIDTo());
				for (KeyLog log : logs) {
					control.getKeyStore().removeKeyLog(log);
				}
				update();
			}
		});
		b.add(bu);

		p.add(head, BorderLayout.NORTH);
		JScrollPane scrollContent = new JScrollPane(content);
		p.add(scrollContent, BorderLayout.CENTER);
		content.add(a,BorderLayout.CENTER);
		content.add(b, BorderLayout.SOUTH);

		return p;
	}

	private void showSignFileDialog() {
		PanelSign pSign = new PanelSign(control.getKeyStore());
		pSign.init();
		int ans = JOptionPane.showConfirmDialog(null,pSign,"Sign File",JOptionPane.OK_CANCEL_OPTION);
		if (ans == JOptionPane.OK_OPTION) {
			File f = pSign.getFile();
			if (f==null) {
				Dialogs.showMessage("Sorry, no file for signing selected.");
				return;
			}
			try {
				String keyid = pSign.getKeyID(); 
				if (keyid==null || keyid.length()==0) {
					Dialogs.showMessage("Sorry, no signing key selected.");
					return;
				}
				OSDXKey key = control.getKeyStore().getKey(keyid);
				String tsa_server = pSign.getTSAServer();
				signFile(key, f, tsa_server);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}


	private void decryptFile() {
		File f = Dialogs.chooseOpenFile("Please select file for decryption", control.getLastDir(), "");
		if (f != null) {
			control.setLastDir(f.getParentFile());
			try {
				boolean detached = f.getName().toLowerCase().endsWith(".xml");
				Element e = null;
				FileInputStream in = null;
				if (detached) {
					e = Document.fromFile(f).getRootElement();
				}
				else {
					in = new FileInputStream(f);
					String first = readLine(in);
					StringBuffer b = new StringBuffer();
					String z = null;
					boolean terminationFound = false;
					while (!terminationFound && (z=readLine(in))!=null) {
						if (z.equals("#### openSDX symmetrical encrypted file ####") || z.equals("#### openSDX asymmetrical encrypted file ####")) {
							terminationFound = true;
						}
						else b.append(z);
					}
					if (terminationFound) {
						e = Document.fromStream(new ByteArrayInputStream(b.toString().getBytes("UTF-8"))).getRootElement();
					} 
					else {
						Dialogs.showMessage("Sorry, wrong file format");
						return;
					}
				}
				if (e == null || !(e.getName().equals("symmetric_encryption") || e.getName().equals("asymmetric_encryption"))) {
					Dialogs.showMessage("Error, wrong or missing metadata");
					return;
				}

				if (e.getName().equals("symmetric_encryption")) {
					SymmetricKey symkey = null;

					if (e.getChild("encrypted_symmetric_key")==null) {
						String mantra = e.getChildTextNN("mantraname");
						char[] p = Dialogs.showPasswordDialog("Enter password", "Please enter password for mantra:\n"+mantra);
						if (p != null) {
							if (!Arrays.equals(
									SecurityHelper.getSHA256(p.toString().getBytes()),
									SecurityHelper.HexDecoder.decode(e.getChildText("pass_sha256"))
							)) {
								Dialogs.showMessage("Sorry, wrong password.");
								return;
							}

							byte[] initv = SecurityHelper.HexDecoder.decode(e.getChildText("initvector"));
							symkey = SymmetricKey.getKeyFromPass(p.toString().toCharArray(), initv);
						}
					} else {
						Vector<Element> eEncKeys = e.getChildren("encrypted_symmetric_key");
						OSDXKey private_akey = null;
						Element eEncKey = null;
						for (Element eEncK : eEncKeys) {
							OSDXKey akey = OSDXKey.fromPubKeyElement(eEncK.getChild("pubkey"));
							private_akey = control.getKeyStore().getKey(akey.getKeyID());
							if (private_akey!=null && !private_akey.hasPrivateKey()) {
								private_akey = null;
							}
							if (private_akey!=null) {
								eEncKey = eEncK;
								break;
							}
						}

						if (private_akey==null || !private_akey.hasPrivateKey()) {
							Dialogs.showMessage("Decryption failed, no matching private key found in current keystore.");
							return;
						}
						if (!private_akey.isPrivateKeyUnlocked()) {
							private_akey.unlockPrivateKey(control.getMessageHandler());
						}
						if (!private_akey.isPrivateKeyUnlocked()) {
							return;
						}


						//extract sym key
						byte[] initv = private_akey.decrypt(SecurityHelper.HexDecoder.decode(eEncKey.getChildText("enc_initvector")));
						byte[] keybytes = private_akey.decrypt(SecurityHelper.HexDecoder.decode(eEncKey.getChildText("enc_keybytes")));

						symkey = new SymmetricKey(keybytes, initv);
					}

					if (symkey!=null) {
						File fdec = new File(f.getParent(),"decrypt_"+e.getChildText("dataname"));

						if (detached) {
							File fenc = new File(f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf('.')));
							in = new FileInputStream(fenc);
						}

						FileOutputStream out = new FileOutputStream(fdec);
						symkey.decrypt(in, out);
						in.close();
						out.close();

						Dialogs.showMessage("Decryption succeeded.\nfilename: "+fdec.getName());
					}
				} else {
					//asymmetric decryption
					if (e.getChild("pubkey")==null) {
						Dialogs.showMessage("Decryption failed, missing pubkey element.");
						return;
					}
					if (e.getChildText("block_size")==null) {
						Dialogs.showMessage("Decryption failed, missing block_size element.");
						return;
					}
					OSDXKey akey = OSDXKey.fromPubKeyElement(e.getChild("pubkey"));
					OSDXKey private_akey = control.getKeyStore().getKey(akey.getKeyID());
					if (private_akey==null || !private_akey.hasPrivateKey()) {
						Dialogs.showMessage("Decryption failed, no private key with keyid:\n"+akey.getKeyID()+"\nfound in current keystore.");
						return;
					}
					if (!private_akey.isPrivateKeyUnlocked()) {
						private_akey.unlockPrivateKey(control.getMessageHandler());
					}
					if (!private_akey.isPrivateKeyUnlocked()) {
						return;
					}
					//int blockSize = Integer.parseInt(e.getChildText("block_size"));


					File fdec = new File(f.getParent(),"decrypt_"+e.getChildText("dataname"));

					if (detached) {
						File fenc = new File(f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf('.')));
						in = new FileInputStream(fenc);
					}

					FileOutputStream out = new FileOutputStream(fdec);
					byte[] buffer = new byte[384];
					int read = -1;
					byte[] decrypt = null;
					while ((read = in.read(buffer))>0) {
						if (read==384) {
							decrypt = private_akey.decrypt(buffer);
						} else {
							decrypt = private_akey.decrypt(Arrays.copyOf(buffer, read));
						}
						//System.out.println("decrypt len="+decrypt.length+"\tread="+read);
						if (decrypt!=null) {
							out.write(decrypt);
						}
					}
					in.close();
					out.close();

					Dialogs.showMessage("Decryption succeeded.\nfilename: "+fdec.getName());
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private static String readLine(InputStream in) throws Exception {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		byte[] b = new byte[1];
		int r = 0;
		char last='\r';
		while((r=in.read(b)) > 0) {
			char m = (char)b[0];
			if(m == '\n') {
				break;
			} else if(m != '\r') {
				bout.write(b[0]);
			}  
		}
		if(r<0 && bout.size()==0) {
			return null;
		}
		String s = new String(bout.toByteArray(), "UTF-8");
		return s;
	}

	private void showEncryptFileDialog() {
		PanelEncrypt pEnc = new PanelEncrypt(control.getKeyStore());
		pEnc.init();
		int ans = JOptionPane.showConfirmDialog(null,pEnc,"Encrypt File",JOptionPane.OK_CANCEL_OPTION);
		if (ans == JOptionPane.OK_OPTION) {
			File f = pEnc.getFile();
			if (f==null) return;
			int method = pEnc.getEncMethod();
			int fFormat = pEnc.getEncFormat();
			if (method==0) {
				//sym with password
				try {
					byte[] initv = SecurityHelper.getRandomBytes(16);
					String pw = pEnc.getPassword().toString();
					SymmetricKey key = SymmetricKey.getKeyFromPass(pw.toCharArray(), initv);

					Element e = new Element("symmetric_encryption");
					e.addContent("dataname", f.getName());
					e.addContent("origlength", ""+f.length());
					e.addContent("lastmodified", SecurityHelper.getFormattedDate(f.lastModified()));
					//e.addContent("mantraname",p[0]);
					e.addContent("pass_sha256", SecurityHelper.HexDecoder.encode(SecurityHelper.getSHA256(pw.getBytes()), ':', -1));
					e.addContent("algo","AES@256");
					e.addContent("initvector", SecurityHelper.HexDecoder.encode(initv, ':', -1));
					e.addContent("padding", "CBC/PKCS#7");
					Document d = Document.buildDocument(e);

					if (fFormat == 0) {
						File[] saveEnc = control.encryptFileDetached(f, key, d);
						Dialogs.showMessage("Detached encryption succeeded.\nencrypt file: "+saveEnc[0].getName()+"\nsignature filename: "+saveEnc[1].getName());
					} else {
						File saveEnc = control.encryptFileInline(f, key, d);
						Dialogs.showMessage("Inline encryption succeeded.\nfilename: "+saveEnc.getName());
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			else if (method==1) {

				String[] keys = pEnc.getKeyIDs();
				if (keys==null) {
					Dialogs.showMessage("No selected keys for encrypting random symmetric key");
					return;
				}

				//rand sym with asymm enc keys
				try {
					SymmetricKey symkey = SymmetricKey.getRandomKey();

					Element e = new Element("symmetric_encryption");
					e.addContent("dataname", f.getName());
					e.addContent("origlength", ""+f.length());
					e.addContent("lastmodified", SecurityHelper.getFormattedDate(f.lastModified()));

					for (String k : keys) {
						OSDXKey key = control.getKeyStore().getKey(k);
						Element eKey = new Element("encrypted_symmetric_key");
						eKey.addContent(key.getSimplePubKeyElement());
						eKey.addContent("asymmetric_encryption_algo","RSA");
						byte[] enc_initv = key.encrypt(symkey.getInitVector());
						byte[] enc_keybytes = key.encrypt(symkey.getKeyBytes());
						eKey.addContent("enc_initvector", SecurityHelper.HexDecoder.encode(enc_initv, ':', -1));
						eKey.addContent("enc_keybytes", SecurityHelper.HexDecoder.encode(enc_keybytes, ':', -1));
						eKey.addContent("symmetric_encryption_algo","AES@256");
						eKey.addContent("padding", "CBC/PKCS#7");
						e.addContent(eKey);
					}
					Document d = Document.buildDocument(e);

					if (fFormat == 0) {
						File[] saveEnc = control.encryptFileDetached(f, symkey, d);
						Dialogs.showMessage("Detached encryption succeeded.\nencrypt file: "+saveEnc[0].getName()+"\nsignature filename: "+saveEnc[1].getName());
					} else {
						File saveEnc = control.encryptFileInline(f, symkey, d);
						Dialogs.showMessage("Inline encryption succeeded.\nfilename: "+saveEnc.getName());
					}

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			else if (method==2) {
				//asymm
				try {
					SymmetricKey symkey = SymmetricKey.getRandomKey();
					String k = pEnc.getKeyID();

					OSDXKey key = control.getKeyStore().getKey(k);

					Element e = new Element("symmetric_encryption");
					e.addContent("dataname", f.getName());
					e.addContent("origlength", ""+f.length());
					e.addContent("lastmodified", SecurityHelper.getFormattedDate(f.lastModified()));
					e.addContent(key.getSimplePubKeyElement());
					e.addContent("asymmetric_encryption_algo","RSA");
					byte[] enc_initv = key.encrypt(symkey.getInitVector());
					byte[] enc_keybytes = key.encrypt(symkey.getKeyBytes());
					e.addContent("enc_initvector", SecurityHelper.HexDecoder.encode(enc_initv, ':', -1));
					e.addContent("enc_keybytes", SecurityHelper.HexDecoder.encode(enc_keybytes, ':', -1));
					e.addContent("symmetric_encryption_algo","AES@256");
					e.addContent("padding", "CBC/PKCS#7");
					Document d = Document.buildDocument(e);

					if (fFormat == 0) {
						File[] saveEnc = control.encryptFileDetached(f, symkey, d);
						Dialogs.showMessage("Detached encryption succeeded.\nencrypt file: "+saveEnc[0].getName()+"\nsignature filename: "+saveEnc[1].getName());
					} else {
						File saveEnc = control.encryptFileInline(f, symkey, d);
						Dialogs.showMessage("Inline encryption succeeded.\nfilename: "+saveEnc.getName());
					}

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

//	private void encryptFile() {
//		//Dialogs.showMessage("feature not implented.");
//		File f = Dialogs.chooseOpenFile("Please select file for encryption", control.getLastDir(), "");
//		if (f != null) {
//			control.setLastDir(f.getParentFile());
//			int detached = Dialogs.showYES_NO_Dialog("Create detached metadata", "Do you want to create a detached metadata file?");
//			String[] p = Dialogs.showNewMantraPasswordDialog("Passphrase for encryption of file:\n"+f.getName());
//			if (p != null) {
//				try {
//					byte[] initv = SecurityHelper.getRandomBytes(16);
//					SymmetricKey key = SymmetricKey.getKeyFromPass(p[1].toCharArray(), initv);
//
//					Element e = new Element("symmetric_encryption");
//					e.addContent("dataname", f.getName());
//					e.addContent("origlength", ""+f.length());
//					e.addContent("lastmodified", SecurityHelper.getFormattedDate(f.lastModified()));
//					e.addContent("mantraname",p[0]);
//					e.addContent("pass_sha256", SecurityHelper.HexDecoder.encode(SecurityHelper.getSHA256(p[1].getBytes()), ':', -1));
//					e.addContent("algo","AES@256");
//					e.addContent("initvector", SecurityHelper.HexDecoder.encode(initv, ':', -1));
//					e.addContent("padding", "CBC/PKCS#7");
//					Document d = Document.buildDocument(e);
//
//					if (detached == Dialogs.YES) {
//						File[] saveEnc = encryptFileDetached(f, key, d);
//						Dialogs.showMessage("Detached encryption succeeded.\nencrypt file: "+saveEnc[0].getName()+"\nsignature filename: "+saveEnc[1].getName());
//					} else {
//						File saveEnc = encryptFileInline(f, key, d);
//						Dialogs.showMessage("Inline encryption succeeded.\nfilename: "+saveEnc.getName());
//					}
//
//				} catch (Exception ex) {
//					ex.printStackTrace();
//				}
//			}
//		}
//	}

	private void removeKey(OSDXKey key) {
		int ans = Dialogs.showYES_NO_Dialog("Remove Key", "Are you sure you want to remove the following key from keystore.\n"+key.getKeyID());
		if (ans==Dialogs.YES) {
			if (key.isRevoke()) {
				int ans2 = Dialogs.showYES_NO_Dialog("Really remove Key", "The selected key is a revokekey.\nIf you remove this key, you will not be able to revoke its parent masterkey with this key.\nAre you really sure you want to remove it?");
				if (ans2==Dialogs.YES) {
					control.getKeyStore().removeKey(key);
					update();		
				}
			} else if (key.isSub()) {
				int ans2 = Dialogs.showYES_NO_Dialog("Really remove Key", "The selected key is a subkey.\nIf you remove this key there is no way of ever getting it back.\nAre you really sure you want to remove it?");
				if (ans2==Dialogs.YES) {
					control.getKeyStore().removeKey(key);
					update();		
				}
			} else if (key.isMaster()) {
				Vector<SubKey> subkeys = control.getKeyStore().getSubKeys(key.getKeyID());
				Vector<RevokeKey> revokekeys = control.getKeyStore().getRevokeKeys(key.getKeyID());
				String msg = "The selected key is a masterkey.\nIf you remove this key there is absolutely no way of getting it back\nAre you really sure you want to remove it?";

				if ((subkeys!=null && subkeys.size()>0) || (revokekeys!=null && revokekeys.size()>0)) {
					msg = "The selected key is a masterkey.\nIf you remove this key the following subkeys / revokekeys will also be removed:\n";
					for (SubKey s : subkeys) {
						msg += " -> "+s.getKeyID()+"\n";
					}
					for (RevokeKey s : revokekeys) {
						msg += " -> "+s.getKeyID()+"\n";
					}
					msg += "\nThere is absolutely no way of getting these keys back\nAre you really sure you want to remove them?";
				}


				int ans2 = Dialogs.showYES_NO_Dialog("Really remove Key", msg);
				if (ans2==Dialogs.YES) {
					if (subkeys!=null && subkeys.size()>0) {
						for (SubKey s : subkeys) {
							control.getKeyStore().removeKey(s);
						}
					}
					control.getKeyStore().removeKey(key);
					update();		
				}
			}
		}
	}

	private void verifySignature() {
		if (control.getKeyStore()!=null) {
			File f = Dialogs.chooseOpenFile("Please select signature file for verification", control.getLastDir(), "");
			if (f!=null && f.exists()) {
				control.setLastDir(f.getParentFile());
				Result verify = control.verifyFileSignature(f);
				if (verify.succeeded) {
					Dialogs.showMessage("Signature verified!");
				} else {
					Dialogs.showMessage("Signature NOT verified!");
				}
				if (verify.report!=null) {
					Dialogs.showText("Key Verification Report", Document.buildDocument(verify.report).toString());
					File fPDF = Dialogs.chooseSaveFile("Save report as PDF", control.getLastDir(), f.getName()+"_sig_verif.pdf");
					if (fPDF!=null) {
						ReportGenerator.buildFileSignatureVerificationReport(verify.report, fPDF);
					}
				}
			}
		}
	}

	private void asymmetricEncryptedRandomSymmetricKeyEncryptionOfFile() {
		if (control.getKeyStore()!=null) {
			OSDXKey key = selectEncryptionKey();
			if (key==null) {
				return;
			}
			File f = Dialogs.chooseOpenFile("Please select file for encryption", control.getLastDir(), "");
			if (f!=null) {
				control.setLastDir(f.getParentFile());
				int detached = Dialogs.showYES_NO_Dialog("Create detached metadata", "Do you want to create a detached metadata file?");

				try {
					SymmetricKey symkey = SymmetricKey.getRandomKey();

					Element e = new Element("symmetric_encryption");
					e.addContent("dataname", f.getName());
					e.addContent("origlength", ""+f.length());
					e.addContent("lastmodified", SecurityHelper.getFormattedDate(f.lastModified()));
					e.addContent("asymmetric_encryption_algo","RSA");
					e.addContent(key.getSimplePubKeyElement());
					byte[] enc_initv = key.encrypt(symkey.getInitVector());
					byte[] enc_keybytes = key.encrypt(symkey.getKeyBytes());
					e.addContent("enc_initvector", SecurityHelper.HexDecoder.encode(enc_initv, ':', -1));
					e.addContent("enc_keybytes", SecurityHelper.HexDecoder.encode(enc_keybytes, ':', -1));
					e.addContent("symmetric_encryption_algo","AES@256");
					e.addContent("padding", "CBC/PKCS#7");
					Document d = Document.buildDocument(e);

					if (detached == Dialogs.YES) {
						File[] saveEnc = control.encryptFileDetached(f, symkey, d);
						Dialogs.showMessage("Detached encryption succeeded.\nencrypt file: "+saveEnc[0].getName()+"\nsignature filename: "+saveEnc[1].getName());
					} else {
						File saveEnc = control.encryptFileInline(f, symkey, d);
						Dialogs.showMessage("Inline encryption succeeded.\nfilename: "+saveEnc.getName());
					}

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

//	private void asymmetricEncryptionOfFile() {
//		if (control.getKeyStore()!=null) {
//			OSDXKey key = selectEncryptionKey();
//			if (key==null) {
//				return;
//			}
//			File f = Dialogs.chooseOpenFile("Please select file for encryption", control.getLastDir(), "");
//			if (f!=null) {
//				control.setLastDir(f.getParentFile());
//				int detached = Dialogs.showYES_NO_Dialog("Create detached metadata", "Do you want to create a detached metadata file?");
//
//				try {
//					int blockSize = 342;
//					Element e = new Element("asymmetric_encryption");
//					e.addContent("dataname", f.getName());
//					e.addContent("origlength", ""+f.length());
//					e.addContent("lastmodified", SecurityHelper.getFormattedDate(f.lastModified()));
//					e.addContent("asymmetric_encryption_algo","RSA");
//					e.addContent("block_size",""+blockSize);
//					e.addContent(key.getSimplePubKeyElement());
//					Document d = Document.buildDocument(e);
//
//					if (detached == Dialogs.YES) {
//						File[] saveEnc = control.asymmetricEncryptFileDetached(f, key, d, blockSize);
//						Dialogs.showMessage("Detached encryption succeeded.\nencrypt file: "+saveEnc[0].getName()+"\nsignature filename: "+saveEnc[1].getName());
//					} else {
//						File saveEnc = control.asymmetricEncryptFileInline(f, key, d, blockSize);
//						Dialogs.showMessage("Inline encryption succeeded.\nfilename: "+saveEnc.getName());
//					}
//				} catch (Exception ex) {
//					ex.printStackTrace();
//				}
//			}
//		}
//	}

	private void signFile() {
		if (control.getKeyStore()!=null) {
			OSDXKey key = selectPrivateSigningKey();
			if (key==null) {
				return;
			}
			File f = Dialogs.chooseOpenFile("Please select file for signing", control.getLastDir(), "");
			if (f!=null) {
				control.setLastDir(f.getParentFile());
				signFile(key,f,null);
			}

			//			Vector<SubKey> keys = currentKeyStore.getAllSigningSubKeys(); 
			//			if (keys.size()==0) {
			//				Dialogs.showMessage("Sorry, no subkeys for signing in keystore");
			//				return;
			//			}
			//			Vector<String> keyids = new Vector<String>();
			//			for (OSDXKey k: keys) {
			//				String id = k.getKeyID();
			//				keyids.add(id);
			//			}
			//			File f = Dialogs.chooseOpenFile("Please select file for signing", lastDir, "");
			//			if (f!=null) {
			//				int a = Dialogs.showSelectDialog("Select key", "Please select key for signing", keyids);
			//				if (a>=0) {
			//					OSDXKey key = keys.get(a);
			//					signFile(key,f);
			//				}
			//			}
		}
	}

	private void signFile(OSDXKey key, File file, String tsa_server) {
		try {
			if (!key.isPrivateKeyUnlocked()) key.unlockPrivateKey(control.getMessageHandler());
			File fileout = new File(file.getAbsolutePath()+"_signature.xml");
			if (tsa_server==null) {
				Signature.createSignatureFile(file, fileout, key);
			} else {
				Signature.createSignatureFile(file, fileout, key, tsa_server);
			}
			if (fileout.exists())
				Dialogs.showMessage("Signature creation succeeded. \nfile: "+fileout.getAbsolutePath());

		} catch (Exception ex) {
			ex.printStackTrace();
			if (tsa_server==null) {
				Dialogs.showMessage("ERROR: Creating signature for file: "+file.getAbsolutePath()+" failed");
			} else {
				Dialogs.showMessage("ERROR: Creating signature for file: "+file.getAbsolutePath()+" incl. TSA failed");
			}
		}
	}

	private void addKeyServer() {
		String s = Dialogs.showInputDialog("Add KeyServer", "Please enter host name");
		if (s!=null) {
			addKeyServer(s, KeyClient.OSDX_KEYSERVER_DEFAULT_PORT, "");
		}
	}

	private void addKeyServer(String host, int port, String prepath) {
		control.getKeyStore().addKeyServer(KeyServerIdentity.make(host, port, prepath));
		update();
	}

	private void generateRevokeKey(final MasterKey parentKey) {
		final JDialog d = Dialogs.getWaitDialog("Generating new REVOKE Key,\nplease wait...");
		Thread t = new Thread() {
			public void run() {
				try {
					AsymmetricKeyPair kp =  AsymmetricKeyPair.generateAsymmetricKeyPair();
					RevokeKey k = parentKey.buildNewRevokeKeyfromKeyPair(kp);
					k.setParentKey(parentKey);
					control.getKeyStore().addKey(k);
					releaseUILock();
					update();
				} catch (Exception ex) {
					releaseUILock();
					Dialogs.showMessage("ERROR: could not generate new keypair.");
					ex.printStackTrace();
				}
				d.dispose();
			}
		};    
		t.start() ;
		d.show();
		if (t.isAlive()) {
			t.stop();
			releaseUILock();
			update();
		}
	}

	private void generateSubKey(final MasterKey parentKey) {
		final String[] usage = Dialogs.showUsageDialog("Genrating new SUB Key", "You can set a usage restirction and/or usage note for this subkey.\nThese values cannot be changed afterwards.\nClick cancel to generate a subkey without restrictions and notes.\n");

		final JDialog d = Dialogs.getWaitDialog("Generating new SUB Key,\nplease wait...");
		Thread t = new Thread() {
			public void run() {
				try {
					AsymmetricKeyPair kp =  AsymmetricKeyPair.generateAsymmetricKeyPair();
					SubKey k = parentKey.buildNewSubKeyfromKeyPair(kp); //MasterKey.buildNewMasterKeyfromKeyPair(kp);
					if (usage!=null) {
						if (usage[0]!=null && usage[0].length()>0) {
							k.setUsageRestricton(usage[0]);
						}
						if (usage[1]!=null && usage[1].length()>0) {
							k.setUsageNote(usage[1]);
						}
					}
					control.getKeyStore().addKey(k);
					releaseUILock();
					update();
				} catch (Exception ex) {
					releaseUILock();
					Dialogs.showMessage("ERROR: could not generate new keypair.");
					ex.printStackTrace();
				}
				d.dispose();
			}
		};    
		t.start();
		d.setVisible(true);
		if (t.isAlive()) {
			t.stop();
			releaseUILock();
			update();
		}
	}

	protected void generateLOCALKeyLog(OSDXKey to, boolean trust) {
		OSDXKey from = selectPrivateSigningKey();
		if (from==null) {
			return;
		}
		from.unlockPrivateKey(control.getMessageHandler());

		if (!from.isPrivateKeyUnlocked()) {
			return;
		}
		Identity id = null;
		try {
			KeyLogAction action;
			if (trust) {
				action = KeyLogAction.buildKeyLogAction(KeyLogAction.APPROVAL, from, to.getKeyID(), id, null);
			} else {
				action = KeyLogAction.buildRevocationKeyLogAction(from, to.getKeyID(), "revoked by user");
			}
			KeyLog log = KeyLog.buildNewKeyLog(action, "LOCAL", "LOCAL", from);
			control.getKeyStore().addKeyLog(log);
			if (!trust) {
				control.getKeyverificator().removeDirectRating(to);
			}
			update();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public OSDXKey selectPrivateSigningKey() {
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

	public OSDXKey selectEncryptionKey() {

		Vector<SubKey> keys = control.getKeyStore().getAllEncyrptionSubKeys();
		if (keys.size()==0) {
			Dialogs.showMessage("Sorry, no keys for encryption in keystore");
			return null;
		}
		Vector<String> select = new Vector<String>();
		int[] map = new int[keys.size()];
		for (int i=0;i<keys.size();i++) {
			OSDXKey k = keys.get(i);
			if (k.isMaster()) {
				select.add(k.getKeyID()+", "+((MasterKey)k).getIDEmailAndMnemonic());
			}
			else if (k.isSub() && ((SubKey)k).getParentKey()!=null) {
				select.add(k.getKeyID()+" subkey of "+((SubKey)k).getParentKey().getIDEmailAndMnemonic());
			}
			else {
				select.add(k.getKeyID());
			}
			map[select.size()-1] = i;
		}
		int ans = Dialogs.showSelectDialog("Select encryption key","Please select a key for encryption", select);
		if (ans>=0 && ans<select.size()) {
			return keys.get(map[ans]);
		}
		return null;
	}

	protected void showGenerateKeyLogDialog(final OSDXKey to) {
		if (storedPrivateKeys==null || storedPrivateKeys.size()==0) {
			Dialogs.showMessage("Sorry, no private key for signing in keystore");
			return;
		}

		Vector<Identity> ids = control.requestIdentitiyDetails(to.getKeyID(),null);
		if (ids==null) {
			return;
		}
		final Identity[] id = new Identity[1];
		id[0] = null;
		if (ids!=null && ids.size()>0) {
			id[0] = ids.lastElement();
		}
		if (id[0]==null) {
			Dialogs.showMessage("No identities found for "+to.getKeyID());
			return;
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

		//String ip4 = "127.0.0.1";
		//String ip6 = "127.0.0.1";
		//Identity id = Identity.newEmptyIdentity();

		JPanel p = new JPanel();
		//p.setLayout(new BorderLayout());
		GridBagLayout gb = new GridBagLayout();		
		p.setLayout(gb);

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.insets = new Insets(5, 5, 0, 0);
		int y = -1;
		String head = "Generate KeyLog";

		final JPanel pDialog =  new JPanel();
		final JPanel[] pSouth =  new JPanel[1];
		final Vector<JCheckBox> checks = new Vector<JCheckBox>();
		final Vector<JTextField> texts = new Vector<JTextField>();
		final Vector<JButton> buttons = new Vector<JButton>();


		y++;
		JLabel l = new JLabel("Key ID to:");
		c.weightx = 0;
		c.weighty = 0.1;
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		c.gridy = y;
		c.gridwidth = 1;
		p.add(l, c);

		l = new JLabel(to.getKeyID());
		c.weightx = 1;
		c.weighty = 0.1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = y;
		c.gridwidth = 1;
		p.add(l, c);

		y++;
		l = new JLabel("set action:");
		c.weightx = 0;
		c.weighty = 0.1;
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		c.gridy = y;
		c.gridwidth = 1;
		p.add(l, c);

		Vector<String> vStatus = new Vector<String>();
		vStatus.add(KeyLogAction.APPROVAL);
		vStatus.add(KeyLogAction.DISAPPROVAL);
		//vStatus.add(KeyLog.REVOCATION);

		JComboBox selectStatus = new JComboBox(vStatus);
		selectStatus.setEditable(false);
		selectStatus.setSelectedIndex(0);
		c.weightx = 1;
		c.weighty = 0.1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = y;
		c.gridwidth = 1;
		p.add(selectStatus, c);

		//		Vector<MasterKey> masterkeys = currentKeyStore.getAllSigningMasterKeys();
		//		if (masterkeys == null || masterkeys.size()==0) {
		//			Dialogs.showMessage("Sorry, no signing masterkey in keystore.");
		//		}
		//		Vector<String> mkeys = new Vector<String>();
		//		for (MasterKey k : masterkeys) {
		//			mkeys.add(k.getKeyID()+", "+k.getIDEmails());
		//		}

		y++;
		l = new JLabel("Key ID from:");
		c.weightx = 0;
		c.weighty = 0.1;
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		c.gridy = y;
		c.gridwidth = 1;
		p.add(l, c);

		JComboBox selectMasterKey = new JComboBox(select);
		selectMasterKey.setEditable(false);
		selectMasterKey.setSelectedIndex(0);
		c.weightx = 1;
		c.weighty = 0.1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = y;
		c.gridwidth = 1;
		p.add(selectMasterKey, c);

		y++;

		JButton requestId = new JButton("request (restricted) identity details from keyserver");
		c.weightx = 0;
		c.weighty = 0.1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = y;
		c.gridwidth = 2;
		p.add(requestId, c);
		requestId.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//select signing key
				OSDXKey signinigKey = selectPrivateSigningKey();
				if (signinigKey!=null) {
					signinigKey.unlockPrivateKey(control.getMessageHandler());
				}
				Vector<Identity> ids = control.requestIdentitiyDetails(to.getKeyID(), signinigKey);	

				Identity idd = null;
				if (ids!=null && ids.size()>0) {
					idd = ids.lastElement();
				}
				if (idd==null) {
					Dialogs.showMessage("No identities found for "+to.getKeyID());
					return;
				}
				Vector<Element> content = idd.getContentElements(true);
				for (int i=0;i<content.size();i++) {
					Element ec = content.get(i);
					String name = ec.getName();
					String value = ec.getText();
					texts.get(i).setText(value);
					boolean restricted = Boolean.parseBoolean(ec.getAttribute("restricted"));
					if (restricted) {
						restricted = value.equals(Identity.RESTRICTED);
					}
					if (restricted) {
						checks.get(i).setEnabled(false);
						checks.get(i).setSelected(false);
						texts.get(i).setBackground(Color.WHITE);
					} else {
						checks.get(i).setEnabled(true);
					}
					if  (name.equals("identnum")) {
						checks.get(i).setEnabled(false);
						checks.get(i).setSelected(true);
						texts.get(i).setBackground(Color.GREEN);
					}
					if  (name.equals("photo")) {
						if (restricted) {
							checks.get(i).setEnabled(false);
							checks.get(i).setSelected(false);
							int photoW = 90;
							int photoH = 120;
							BufferedImage img = new BufferedImage(photoW, photoH, BufferedImage.TYPE_INT_RGB);
							Graphics g = img.getGraphics();
							g.setColor(Color.WHITE);
							g.fillRect(0,0,photoW,photoH);
							g.setColor(Color.GRAY);
							g.setFont(new Font("arial", Font.BOLD, 12));
							g.drawString("[RESTRICTED]", photoW/2-39 ,photoH/2);
							buttons.get(0).setIcon(new ImageIcon(img));
						} else {
							checks.get(i).setEnabled(true);
							buttons.get(0).setIcon(new ImageIcon(idd.getPhoto()));
						}
					}
				}
				id[0] = idd;
			}
		});
		y++;
		l = new JLabel("Message:");
		c.weightx = 0;
		c.weighty = 0.1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = y;
		c.gridwidth = 2;
		p.add(l, c);
		y++;
		JTextArea text_message = new JTextArea("");
		c.weightx = 0;
		c.weighty = 0.1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = y;
		c.gridwidth = 2;
		p.add(text_message, c);


		y++;
		l = new JLabel("Please select fields for status update:");
		c.weightx = 0;
		c.weighty = 0.1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = y;
		c.gridwidth = 2;
		p.add(l, c);


		Dimension d = new Dimension(700,4*40+80);
		p.setPreferredSize(d);
		p.setMinimumSize(d);
		p.setMaximumSize(d);

		pDialog.setLayout(new BorderLayout());
		pDialog.add(p, BorderLayout.NORTH);
		pSouth[0] = buildIDElement(id[0], checks, texts, buttons);
		pDialog.add(pSouth[0], BorderLayout.CENTER);

		int ans = JOptionPane.showConfirmDialog(null,pDialog,head,JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (ans == JOptionPane.OK_OPTION) {
			//delete all unchecked from id;
			Vector<Element> content = id[0].getContentElements(true);
			for (int i=0;i<content.size();i++) {
				Element ec = content.get(i);
				boolean selected = checks.get(i).isSelected();
				if (!selected) {
					String name = ec.getName();
					if (name.equals("email")) id[0].setEmail(null);
					else if (name.equals("mnemonic")) id[0].setMnemonic(null);
					else if (name.equals("country")) id[0].setCountry(null);
					else if (name.equals("region")) id[0].setRegion(null);
					else if (name.equals("city")) id[0].setCity(null);
					else if (name.equals("postcode")) id[0].setPostcode(null);
					else if (name.equals("company")) id[0].setCompany(null);
					else if (name.equals("unit")) id[0].setUnit(null);
					else if (name.equals("subunit")) id[0].setSubunit(null);
					else if (name.equals("function")) id[0].setFunction(null);
					else if (name.equals("surname")) id[0].setSurname(null);
					else if (name.equals("middlename")) id[0].setMiddlename(null);
					else if (name.equals("name")) id[0].setFirstNames(null);
					else if (name.equals("birthday_gmt")) id[0].setBirthday_gmt(Long.MIN_VALUE);
					else if (name.equals("placeofbirth")) id[0].setPlaceofbirth(null);
					else if (name.equals("phone")) id[0].setPhone(null);
					else if (name.equals("fax")) id[0].setFax(null);
					else if (name.equals("note")) id[0].setNote(null);
					else if (name.equals("photo")) id[0].setPhoto((BufferedImage)null);
				}
			}

			OSDXKey from = storedPrivateKeys.get(map[selectMasterKey.getSelectedIndex()]);
			if (!from.isPrivateKeyUnlocked()) from.unlockPrivateKey(control.getMessageHandler());
			try {
				String status = (String)selectStatus.getSelectedItem();
				uploadKeyLogActionToKeyServer(status, from, to.getKeyID(), id[0], text_message.getText());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private JPanel buildIDElement(Identity id, Vector<JCheckBox> checks, Vector<JTextField> texts, Vector<JButton> buttons) {
		JPanel p = new JPanel();
		checks.removeAllElements();
		texts.removeAllElements();
		buttons.removeAllElements();

		//p.setLayout(new BorderLayout());
		GridBagLayout gb = new GridBagLayout();		
		p.setLayout(gb);

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.insets = new Insets(5, 5, 0, 0);
		int y = -1;
		JLabel l;
		Vector<Element> content = id.getContentElements(true);
		int contentSize = 0;
		for (int i=0;i<content.size();i++) {
			y++;
			Element ec = content.get(i);
			String name = ec.getName();
			String value = ec.getText();
			boolean restricted = Boolean.parseBoolean(ec.getAttribute("restricted"));

			l = new JLabel(name);

			final JTextField t = new JTextField(value);
			t.setBackground(Color.WHITE);
			t.setEditable(false);

			final JCheckBox check = new JCheckBox();
			check.setPreferredSize(new Dimension(20,20));
			check.setSelected(false);
			check.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (check.isSelected()) {
						t.setBackground(Color.GREEN);
					} else {
						t.setBackground(Color.WHITE);
					}
				}
			});
			//System.out.println(Arrays.toString(row));
			if (restricted) {
				check.setEnabled(false);
			}
			if  (name.equals("identnum")) {
				check.setEnabled(false);
				check.setSelected(true);
				t.setBackground(Color.GREEN);
			}
			c.weightx = 0;
			c.weighty = 0.1;
			c.fill = GridBagConstraints.NONE;
			c.gridx = 0;
			c.gridy = y;
			p.add(check, c);

			l.setPreferredSize(new Dimension(100,20));
			c.weightx = 0;
			c.weighty = 0.1;
			c.fill = GridBagConstraints.NONE;
			c.gridx = 1;
			c.gridy = y;
			p.add(l, c);

			if (name.equals("photo")) {
				contentSize += 125;
				JButton buPhoto = new JButton();
				BufferedImage img = id.getPhoto();
				if (img==null) {
					int photoW = 90;
					int photoH = 120;
					img = new BufferedImage(photoW, photoH, BufferedImage.TYPE_INT_RGB);
					Graphics g = img.getGraphics();
					g.setColor(Color.WHITE);
					g.fillRect(0,0,photoW,photoH);
					g.setColor(Color.GRAY);
					g.setFont(new Font("arial", Font.BOLD, 12));
					g.drawString("[RESTRICTED]", photoW/2-39 ,photoH/2);
				}
				buPhoto.setIcon(new ImageIcon(img));
				Dimension d = new Dimension(90,120);
				buPhoto.setMinimumSize(d);
				buPhoto.setMaximumSize(d);
				buPhoto.setPreferredSize(d);
				c.fill = GridBagConstraints.NONE;
				c.weightx = 1;
				c.gridx = 2;
				c.gridy = y;
				c.gridwidth = 1;
				p.add(buPhoto,c);
				checks.add(check);
				texts.add(t);
				buttons.add(buPhoto);
			} else {
				contentSize += 31;
				t.setEditable(false);
				c.fill = GridBagConstraints.HORIZONTAL;
				c.weightx = 1;
				c.gridx = 2;
				c.gridy = y;
				c.gridwidth = 1;
				p.add(t,c);
				checks.add(check);
				texts.add(t);
			}
		}
		Dimension d = new Dimension(700,contentSize);
		p.setPreferredSize(d);
		p.setMinimumSize(d);
		p.setMaximumSize(d);
		return p;
	}

	private boolean testKeyServerSettings(KeyServerIdentity keyserver) {
		//test keyserversettings and server signing key
		boolean connectionOK = true;
		KeyClient client = new KeyClient(keyserver, control.getKeyverificator());
		try {
			boolean connected = client.connect();
			if (connected) {
				KeyServerIdentity ksid = client.requestKeyServerIdentity();
				if (ksid == null) {
					connectionOK = false;
				} else {
					Vector<OSDXKey> knownkeys = ksid.getKnownKeys();
					for (OSDXKey key : knownkeys) {
						if (!keyserver.hasKnownKey(key.getKeyID())) {
							int answer = Dialogs.showYES_NO_Dialog("Add KeyServer Key", "Add KeyServers Key:\n"+key.getKeyID()+"\nto known keys?");
							if (answer == Dialogs.YES) {
								keyserver.addKnownKey(key);
								update();
							}
						}
					}
				}
			} else {
				connectionOK = false;
			}
		} catch (Exception e) {
			connectionOK = false;
			e.printStackTrace();
		}
		return connectionOK;
	}

	protected void requestKeyLogs(final OSDXKey key) {
		final Vector<KeyLog> logs = new Vector<KeyLog>();
		final OSDXKey sign = selectPrivateSigningKey();
		if (sign!=null) {
			sign.unlockPrivateKey(control.getMessageHandler());
		}
		final KeyClient client = control.getKeyClient(key.getAuthoritativekeyserver());
		final JDialog wait = Dialogs.getWaitDialog("Requesting keylogs for "+key.getKeyID()+".\n please wait ...");
		Thread t = new Thread() {
			public void run() {
				try {
					Vector<KeyLog> rlogs = client.requestKeyLogs(key.getKeyID(),sign);
					if (rlogs!=null && rlogs.size()>0) {
						logs.addAll(rlogs);
					}
				} catch (Exception ex) {
					if (ex.getMessage()!=null && ex.getLocalizedMessage().startsWith("Connection refused")) {
						releaseUILock();
						wait.dispose();
						Dialogs.showMessage("Sorry, could not connect to server.");
						return;
					} else {
						ex.printStackTrace();
					}
				}
				if (logs!=null && logs.size()>0) {
					long datetime = System.currentTimeMillis();
					for (KeyLog kl : logs) {
						kl.addDataPath(new DataSourceStep(client.getHost(), datetime));
						control.getKeyStore().addKeyLog(kl);
					}
					update();
				} else {
					releaseUILock();
					wait.dispose();
					Dialogs.showMessage("Sorry, no keylogs for key:"+ key.getKeyID()+"\navailable on keyserver.");
					return;
				}
				releaseUILock();
				wait.dispose();
			}
		};
		t.start();
		wait.setVisible(true);
	}

	protected void updateStatus(OSDXKey key) {
		KeyClient client =  control.getKeyClient(key.getAuthoritativekeyserver());
		String keyid = key.getKeyID();
		KeyStatus status = null;
		try {
			status = client.requestKeyStatus(keyid);
		} catch (Exception ex) {
			if (ex.getLocalizedMessage().startsWith("Connection refused")) {
				Dialogs.showMessage("Sorry, could not connect to server.");
				return;
			} else {
				ex.printStackTrace();
			}
			//}
		}
		if (status != null) {
			key_status.put(key, status);
			update();
		} else {
			Dialogs.showMessage("Sorry, keystatus not available on keyserver.");
		}
	}

	protected void requestKeysFromServer() {
		if (control.getKeyStore().getKeyServer()==null || control.getKeyStore().getKeyServer().size()==0) {
			Dialogs.showMessage("Sorry, no keyservers found.");
			return;
		}

		final String email = Dialogs.showInputDialog("Request key", "Please enter corresponding email adresse for searching for keys on keyserver.");
		if (email!=null) {
			Vector<String> keyservernames = new Vector<String>();
			for (KeyServerIdentity id : control.getKeyStore().getKeyServer()) {
				keyservernames.add(id.getHost()+":"+id.getPort());
			}
			final int ans = Dialogs.showSelectDialog("Select KeyServer", "Please select a KeyServer.", keyservernames);
			if (ans>=0) {
				final JDialog wait = Dialogs.getWaitDialog("Requesting keys for "+email+".\n please wait ...");
				Thread t = new Thread() {
					public void run() {
						KeyServerIdentity keyserver = control.getKeyStore().getKeyServer().get(ans);
						KeyClient client =  control.getKeyClient(keyserver.getHost());

						try {
							Vector<String> masterkeys = null;
							try {
								masterkeys = client.requestMasterPubKeys(email);
							} catch (Exception ex) {
								releaseUILock();
								wait.dispose();
								if (ex.getLocalizedMessage()!=null && ex.getLocalizedMessage().startsWith("Connection refused")) {
									Dialogs.showMessage("Sorry, could not connect to server.");
									return;
								} else {
									ex.printStackTrace();
								}
							}

							String kt = "";
							if (masterkeys!=null && masterkeys.size()>0) {
								for (String masterkey : masterkeys) {
									System.out.println("requesting key: "+masterkey);
									OSDXKey mkey = client.requestPublicKey(masterkey);
									//remove old key
									String newkeyid = OSDXKey.getFormattedKeyIDModulusOnly(mkey.getKeyID());
									for (OSDXKey k : storedPublicKeys) {
										if (newkeyid.equals(OSDXKey.getFormattedKeyIDModulusOnly(k.getKeyID()))) {
											control.getKeyStore().removeKey(k);
											break;
										}
									}
									for (OSDXKey k : storedTrustedPublicKeys) {
										if (newkeyid.equals(OSDXKey.getFormattedKeyIDModulusOnly(k.getKeyID()))) {
											control.getKeyStore().removeKey(k);
											break;
										}
									}
									control.getKeyStore().addKey(mkey);
									kt += "\n  MASTER: "+mkey.getKeyID();
									Vector<String> subkeys = client.requestSubKeys(masterkey);
									if (subkeys!=null && subkeys.size()>0) {
										for (String subkey : subkeys) {
											OSDXKey skey = client.requestPublicKey(subkey);
											if (skey.isSub() && mkey.isMaster()) {
												((SubKey)skey).setParentKey((MasterKey)mkey);
											}
											//remove old key
											newkeyid = OSDXKey.getFormattedKeyIDModulusOnly(skey.getKeyID());
											for (OSDXKey k : storedPublicKeys) {
												if (newkeyid.equals(OSDXKey.getFormattedKeyIDModulusOnly(k.getKeyID()))) {
													control.getKeyStore().removeKey(k);
													break;
												}
											}
											control.getKeyStore().addKey(skey);
											kt += "\n    -> "+subkey;	
										}
									}
								}
								update();
								releaseUILock();
								wait.dispose();
								Dialogs.showMessage("Added key(s) for \""+email+"\":"+kt);
							} else {
								releaseUILock();
								wait.dispose();
								Dialogs.showMessage("No keys for \""+email+"\" found on keyserver "+keyserver.getHost()+".");
							}
						} catch (Exception ex) {
							releaseUILock();
							wait.dispose();
							ex.printStackTrace();
						}
					}
				};
				t.start();
				wait.setVisible(true);
			}
		}
	}

	private void requestSubKeys(MasterKey masterkey) {
		try {
			KeyClient client =  control.getKeyClient(masterkey.getAuthoritativekeyserver());
			if (client==null) {
				return;
			}
			Vector<String> subkeys = client.requestSubKeys(masterkey.getKeyID());
			if (subkeys!=null && subkeys.size()>0) {
				String kt = "Subkeys from MASTER: "+masterkey.getKeyID()+":";
				for (String subkey : subkeys) {
					OSDXKey skey = client.requestPublicKey(subkey);
					if (skey.isSub()) {
						((SubKey)skey).setParentKey(masterkey);
					}
					//remove old key
					String newkeyid = OSDXKey.getFormattedKeyIDModulusOnly(skey.getKeyID());
					for (OSDXKey k : storedPublicKeys) {
						if (newkeyid.equals(OSDXKey.getFormattedKeyIDModulusOnly(k.getKeyID()))) {
							control.getKeyStore().removeKey(k);
							break;
						}
					}
					control.getKeyStore().addKey(skey);
					kt += "\n  -> "+subkey;	
				}
				update();
				releaseUILock();
				Dialogs.showMessage(kt);
			} else {
				releaseUILock();
				Dialogs.showMessage("No Subkeys for MASTER "+masterkey.getKeyID()+" found.");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void requestParentKey(SubKey key) {
		try {
			KeyClient client =  control.getKeyClient(key.getAuthoritativekeyserver());
			if (client==null) {
				return;
			}
			MasterKey masterkey = client.requestMasterPubKey(key.getKeyID());
			if (masterkey!=null) {
				boolean add = true;
				//remove old key (not if it has private key)
				String newkeyid = OSDXKey.getFormattedKeyIDModulusOnly(masterkey.getKeyID());
				for (OSDXKey k : storedPublicKeys) {
					if (newkeyid.equals(OSDXKey.getFormattedKeyIDModulusOnly(k.getKeyID()))) {
						if (!k.hasPrivateKey()) {
							control.getKeyStore().removeKey(k);
							break;
						} else {
							add = false;
						}
					}
				}
				if (add) {
					control.getKeyStore().addKey(masterkey);
					key.setParentKey(masterkey);
				}
				update();
				releaseUILock();
				Dialogs.showMessage("Found parent key: "+masterkey.getKeyID()+"\nfor subkey: "+key.getKeyID());
			} else {
				releaseUILock();
				Dialogs.showMessage("No Parent Key for Subkey "+key.getKeyID()+" found.");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	//	private boolean uploadKeyLogActionToKeyServer(KeyLogAction log) {
	//		if (currentKeyStore!=null) {
	//			Vector<MasterKey> keys = currentKeyStore.getAllSigningMasterKeys();
	//			if (keys.size()==0) {
	//				Dialogs.showMessage("Sorry, no masterkeys for signing in keystore");
	//				return false;
	//			}
	//			Vector<String> keyids = new Vector<String>();
	//			for (OSDXKey k: keys) {
	//				String id = k.getKeyID();
	//				keyids.add(id);
	//			}
	//			int a = Dialogs.showSelectDialog("Select key", "Please select key for signing", keyids);
	//			if (a>=0) {
	//				OSDXKey key = keys.get(a);
	//				if (!key.isPrivateKeyUnlocked()) key.unlockPrivateKey(messageHandler);
	//				return uploadKeyLogToKeyServer(log, key);
	//			}
	//		} 
	//		return false;
	//	}
	//	private boolean uploadKeyLogToKeyServer(KeyLog log, OSDXKey signingKey) {
	//		if (keyservers == null) {
	//			Dialogs.showMessage("Sorry, no keyservers found.");
	//			return false;
	//		}
	//		Vector<String> keyservernames = new Vector<String>();
	//		for (KeyServerIdentity id : keyservers) {
	//			keyservernames.add(id.getHost()+":"+id.getPort());
	//		}
	//		int ans = Dialogs.showSelectDialog("Select KeyServer", "Please select a KeyServer for uploading KeyLog.", keyservernames);
	//		if (ans>=0) {
	//			return uploadKeyLogToKeyServer(log, keyservers.get(ans), signingKey);
	//		}
	//		return false;
	//	}

	public boolean uploadMasterKeyToKeyServer(final MasterKey key) {
		if (key.getAuthoritativekeyserver().toLowerCase().equals("local")) {
			//select keyserver
			if (control.getKeyStore().getKeyServer() == null) {
				Dialogs.showMessage("Sorry, no keyservers found.");
				return false;
			}
			Vector<String> keyservernames = new Vector<String>();
			for (KeyServerIdentity id : control.getKeyStore().getKeyServer()) {
				keyservernames.add(id.getHost()+":"+id.getPort());
			}
			int ans = Dialogs.showSelectDialog("Select KeyServer", "Please select a KeyServer for uploading MASTER Key.", keyservernames);
			if (ans>=0) {
				KeyServerIdentity keyserver = control.getKeyStore().getKeyServer().get(ans);
				key.setAuthoritativeKeyServer(keyserver.getHost());
			}
		}
		if (!key.getAuthoritativekeyserver().toLowerCase().equals("local")) {
			final Result[] r = new Result[]{Result.error("unknown error")};
			final Result[] rKeylog = new Result[]{Result.error("unknown error")};
			if (key.getCurrentIdentity()==null) r[0] = Result.error("No Identity found.");
			else {
				int confirm = Dialogs.showYES_NO_Dialog("Confirm upload", "Are you sure you want to upload the MASTER Key:\n"+key.getKeyID()+"\nwith Identity: "+key.getCurrentIdentity().getEmail()+"\nto KeyServer: "+key.getAuthoritativekeyserver()+"?\n");
				if (confirm==Dialogs.YES) {
					if (!key.isPrivateKeyUnlocked()) key.unlockPrivateKey(control.getMessageHandler());
					final KeyClient client = control.getKeyClient(key.getAuthoritativekeyserver());
					final JDialog wait = Dialogs.getWaitDialog("Uploading master key\n please wait ...");
					Thread t = new Thread() {
						public void run() {
							try {
								r[0] = key.uploadToKeyServer(client);
								releaseUILock();
								wait.dispose();
							} catch (Exception ex) {
								releaseUILock();
								wait.dispose();
								Dialogs.showMessage("ERROR: could not generate new keypair.");
								ex.printStackTrace();
							}
						}
					};
					t.start();
					wait.setVisible(true);

					if (r[0].succeeded) {
						props.put(key.getKeyID(), "VISIBLE");
						update();

						//self approval keylog 
						try {
							final KeyLogAction klaction = KeyLogAction.buildKeyLogAction(KeyLogAction.APPROVAL, key, key.getKeyID(), key.getCurrentIdentity(), "self approval");
							final JDialog wait2 = Dialogs.getWaitDialog("Uploading self approval\n please wait ...");
							t = new Thread() {
								public void run() {
									try {
										rKeylog[0] = klaction.uploadToKeyServer(client, key);
										releaseUILock();
										wait2.dispose();
									} catch (Exception ex) {
										releaseUILock();
										wait2.dispose();
										Dialogs.showMessage("ERROR: could not generate new keypair.");
										ex.printStackTrace();
									}
								}
							};
							t.start();
							wait2.setVisible(true);


						} catch (Exception ex) {
							ex.printStackTrace();
							rKeylog[0] = Result.error(ex);
						}
					}
				}
			}
			if (r[0].succeeded) {
				Dialogs.showMessage("Upload of MASTER Key:\n"+key.getKeyID()+"\nwith Identity: "+key.getCurrentIdentity().getEmail()+"\nto KeyServer: "+key.getAuthoritativekeyserver()+"\nsuccessful!");
				if (!rKeylog[0].succeeded) {
					Dialogs.showMessage("Upload of self-approval keylog FAILED.");
				}
				return true;
			} else {
				String msg = r[0].errorMessage;
				Dialogs.showMessage("Upload of MASTER Key:\n"+key.getKeyID()+"\nto KeyServer: "+key.getAuthoritativekeyserver()+"\nFAILED!"+(msg!=null?"\n\n"+msg:""));
				return false;
			}
		}
		return false;
	}

	private boolean uploadRevokeKeyToKeyServer(final RevokeKey key) {
		System.out.println("UploadingOfRevokeKeyToServer...started...");

		final JDialog wait = Dialogs.getWaitDialog("Uploading revocation key\n please wait ...");
		final boolean[] result = new boolean[] {false};
		Thread t = new Thread() {
			public void run() {
				try {
					result[0] = uploadSubOrRevokeKeyToKeyServer(key);
					releaseUILock();
					wait.dispose();
				} catch (Exception ex) {
					releaseUILock();
					wait.dispose();
					result[0] = false;
					ex.printStackTrace();
				}
			}
		};
		t.start();

		System.out.println("Before wait blocks...");
		wait.setVisible(true); //blocks...

		System.out.println("AFTER wait blocks...");
		return result[0];
	}

	private boolean uploadSubKeyToKeyServer(SubKey key) {
		return uploadSubOrRevokeKeyToKeyServer(key);
	}

	private boolean uploadSubOrRevokeKeyToKeyServer(final SubKey key) {
		if (key.getParentKey()==null) {
			Dialogs.showMessage("Parent Key for subkey not found.");
			return false;
		}
		String keyLevel = "SUB";
		if (key instanceof RevokeKey) {
			keyLevel = "REVOKE";
		}
		int confirm = Dialogs.showYES_NO_Dialog("Confirm upload", "Are you sure you want to upload the "+keyLevel+" Key:\n"+key.getKeyID()+"\nfor MASTER Key: "+key.getParentKeyID()+"\nto KeyServer: "+key.getAuthoritativekeyserver()+"?");
		if (confirm==Dialogs.YES) {
			if (!key.isPrivateKeyUnlocked()) {
				key.unlockPrivateKey(control.getMessageHandler());
			}
			if (!key.getParentKey().isPrivateKeyUnlocked()) {
				key.getParentKey().unlockPrivateKey(control.getMessageHandler());
			}
			final Result[] r = new Result[] {Result.succeeded()};
			final JDialog wait = Dialogs.getWaitDialog("Uploading key "+key.getKeyID()+"\n please wait ...");
			Thread t = new Thread() {
				public void run() {
					try {
						KeyClient client = control.getKeyClient(key.getAuthoritativekeyserver());
						System.out.println("Before calling key.uploadtoKeyServer...");
						r[0] = key.uploadToKeyServer(client);
						System.out.println("AFTER calling key.uploadtoKeyServer...");
						releaseUILock();

						wait.dispose();
					} catch (Exception ex) {
						releaseUILock();
						wait.dispose();
						r[0] = Result.error(ex);
						ex.printStackTrace();
					}
				}
			};
			t.start();

			System.out.println("Before WAIT2 blocks...");
			wait.setVisible(true);
			System.out.println("After WAIT2 blocks...");
			if (r[0].succeeded) {
				props.put(key.getKeyID(), "VISIBLE");
				update();
				Dialogs.showMessage("Upload of "+keyLevel+" Key:\n"+key.getKeyID()+"\nto KeyServer: "+key.getAuthoritativekeyserver()+"\nsuccessful!");
				return true;
			} else {
				String msg = r[0].errorMessage;
				Dialogs.showMessage("Upload of "+keyLevel+" Key:\n"+key.getKeyID()+"\nto KeyServer: "+key.getAuthoritativekeyserver()+"\nFAILED!"+(msg!=null?"\n\n"+msg:""));
				return false;
			}
		}
		return false;
	}
	//System.out.println("selected status: "+status);

	private boolean uploadKeyLogActionToKeyServer(final String status, final OSDXKey from, final String tokeyid, final Identity id, final String message) {
		try {
			String authserver = tokeyid.substring(tokeyid.indexOf('@')+1);
			int confirm = Dialogs.showYES_NO_Dialog("Confirm upload", "Are you sure you want to generate a KeyLog of key:\n"+tokeyid+"\non KeyServer: "+authserver+"?");
			if (confirm==Dialogs.YES) {
				final KeyClient client =  control.getKeyClient(authserver);
				final Result[] upload = new Result[1];
				final JDialog wait = Dialogs.getWaitDialog("Uploading keylogaction.\n please wait ...");
				Thread t = new Thread() {
					public void run() {
						try {
							String msg = message;
							if (msg !=null && msg.length()==0) {
								msg = null;
							}
							KeyLogAction klaction = KeyLogAction.buildKeyLogAction(status, from, tokeyid, id, msg);
							upload[0] = klaction.uploadToKeyServer(client, from);
							releaseUILock();
							wait.dispose();
						} catch (Exception ex) {
							releaseUILock();
							wait.dispose();
							ex.printStackTrace();
						}
					}
				};
				t.start();
				wait.setVisible(true);

				if (upload[0].succeeded) {
					Dialogs.showMessage("Generation of KeyLog successful!");
					return true;
				} else {
					String msg = upload[0].errorMessage;
					Dialogs.showMessage("Generation of KeyLog FAILED!"+(msg!=null?"\n\n"+msg:""));
					return false;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

	private boolean showIdentityEditDialog(final Identity id, boolean canCancel) {
		IdentityEditDialog d = new IdentityEditDialog(this);
		return d.show(id, canCancel);

		//		final JDialog d = new JDialog(instance);
		//		d.setTitle("Edit Identity");
		//		final boolean[] isOK = new boolean[] {!canCancel};		
		//
		//		JPanel p = new JPanel();
		//		p.setLayout(new BorderLayout());
		//
		//		JTable edit = new JTable();
		//		edit.setModel(new IdentityTableModel(id));
		//		fitAllColumnWidth(edit);
		//		edit.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		//		TableColumn column = edit.getColumnModel().getColumn(0);
		//		column.setPreferredWidth(100);
		//		column.setMaxWidth(100);
		//
		//		p.add(new JScrollPane(edit), BorderLayout.CENTER);
		//
		//		JPanel ps = new JPanel();
		//		JButton ok = new JButton("ok");
		//		ok.setPreferredSize(new Dimension(200,30));
		//		ok.addActionListener(new ActionListener() {
		//			public void actionPerformed(ActionEvent e) {
		//				if (id.getEmail()==null || id.getEmail().equals("")) {
		//					Dialogs.showMessage("Please enter email adress");
		//					return;
		//				}
		//				if (id.getMnemonic()==null || id.getMnemonic().equals("")) {
		//					Dialogs.showMessage("Please enter mnemonic");
		//					return;
		//				}
		//				isOK[0] = true;
		//				d.dispose();
		//			}
		//		});
		//		ps.add(ok);
		//
		//		if (canCancel) {
		//			JButton cancel = new JButton("cancel");
		//			cancel.setPreferredSize(new Dimension(200,30));
		//			cancel.addActionListener(new ActionListener() {
		//				public void actionPerformed(ActionEvent e) {
		//					isOK[0] = false;
		//					d.dispose();
		//				}
		//			});
		//			ps.add(cancel);
		//		}
		//
		//		d.setLayout(new BorderLayout());
		//
		//		d.setSize(700, 400);
		//		d.add(p, BorderLayout.CENTER);
		//		d.add(ps, BorderLayout.SOUTH);
		//		d.setModal(true);
		//
		//		Helper.centerMe(d, null);
		//
		//		d.setVisible(true);
		//		return isOK[0];
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


	public void closeCurrentStore() {
		if (control.getKeyStore()!=null && control.getKeyStore().hasUnsavedChanges()) {
			int a = Dialogs.showYES_NO_Dialog("Save keystore", "Your current keystore has unsaved changes.\nDo you want to save it?");
			if (a==Dialogs.YES) {
				writeCurrentKeyStore(false);
			}
		}
		control.setKeyStore(null);
		update();
	}


	public void generateMasterKeyPair() {
		if (control.getKeyStore()!=null) {
			final JDialog wait = Dialogs.getWaitDialog("Generating new MASTER KEY\n please wait ...");
			Thread t = new Thread() {
				public void run() {
					try {
						AsymmetricKeyPair kp =  AsymmetricKeyPair.generateAsymmetricKeyPair();
						MasterKey k = MasterKey.buildNewMasterKeyfromKeyPair(kp);
						k.createLockedPrivateKey(control.getMessageHandler());
						control.getKeyStore().addKey(k);
						update();
						releaseUILock();
						wait.dispose();
					} catch (Exception ex) {
						releaseUILock();
						wait.dispose();
						Dialogs.showMessage("ERROR: could not generate new keypair.");
						ex.printStackTrace();
					}
				}
			};
			t.start();
			wait.setVisible(true);
		}
	}

	public boolean revokeMasterKeyWithRevokeKey(RevokeKey revokekey) {
		String message = Dialogs.showInputDialog("Confirm REVOCATION", "Please confirm REVOCATION of Masterkey.\nYou can enter a revocatoin message:");
		if (message!=null) {
			return revokeMasterKeyWithRevokeKey(revokekey,message);
		}
		return false;
	}

	public boolean revokeMasterKeyWithRevokeKey(RevokeKey revokekey, String message) {

		String parent = revokekey.getParentKeyID();
		OSDXKey mkey = control.getKeyStore().getKey(parent);
		MasterKey masterkey = null;
		if (mkey instanceof MasterKey) {
			masterkey = (MasterKey)mkey;
		}
		String host = masterkey.getAuthoritativekeyserver().toLowerCase();
		KeyClient client = control.getKeyClient(host);

		if (client!=null) {
			try {
				if (!revokekey.isPrivateKeyUnlocked()) revokekey.unlockPrivateKey(control.getMessageHandler());	

				boolean ok = client.putRevokeMasterKeyRequest(revokekey, masterkey, message);
				if (ok) {
					Dialogs.showMessage("REVOCATION of Key:\n"+masterkey.getKeyID()+"\non KeyServer: "+client.getHost()+"\nsuccessful!");
					return ok;
				} else {
					String msg = client.getMessage();
					Dialogs.showMessage("REVOCATION of Key:\n"+masterkey.getKeyID()+"\non KeyServer: "+client.getHost()+"\nFAILED!"+(msg!=null?"\n\n"+msg:""));
					return false;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}	
		}
		return false;
	}

	public void generateMasterKeySet() {
		if (control.getKeyStore()!=null) {
			boolean ok = false;
			Identity id = null;
			try {
				id = Identity.newEmptyIdentity();
				id.setIdentNum(1);
				ok = showIdentityEditDialog(id, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (!ok) return;
			final Identity idd = id;
			final JDialog wait = Dialogs.getWaitDialog("Generating new MASTER KEY, REVOKE KEY, SUB KEY set,\n please wait ...");
			Thread t = new Thread() {
				public void run() {
					try {
						AsymmetricKeyPair masterkp =  AsymmetricKeyPair.generateAsymmetricKeyPair();
						AsymmetricKeyPair revokekp =  AsymmetricKeyPair.generateAsymmetricKeyPair();
						AsymmetricKeyPair subkp =  AsymmetricKeyPair.generateAsymmetricKeyPair();


						MasterKey masterkey = MasterKey.buildNewMasterKeyfromKeyPair(masterkp);
						masterkey.createLockedPrivateKey(control.getMessageHandler());
						masterkey.addIdentity(idd);

						RevokeKey revokekey = masterkey.buildNewRevokeKeyfromKeyPair(revokekp);
						revokekey.createLockedPrivateKey(control.getMessageHandler());

						SubKey subkey = masterkey.buildNewSubKeyfromKeyPair(subkp);
						subkey.createLockedPrivateKey(control.getMessageHandler());

						control.getKeyStore().addKey(masterkey);
						control.getKeyStore().addKey(revokekey);
						control.getKeyStore().addKey(subkey);

						wait.dispose();
						releaseUILock();
						update();
					} catch (Exception ex) {
						releaseUILock();
						wait.dispose();
						Dialogs.showMessage("ERROR: could not generate new keypair.");
						ex.printStackTrace();
					}
				}
			};
			t.start();
			wait.setVisible(true);

		}
	}


	public boolean openKeystore() {
		closeCurrentStore();
		File f = Dialogs.chooseOpenFile("Select keystore filename", control.getLastDir(), "mykeystore.xml");
		if (f!=null && f.exists()) {
			control.setLastDir(f.getParentFile());
			try {
				boolean open = openKeyStore(f);
				return open;
			} catch (Exception e) {
				Dialogs.showMessage("ERROR: could not create keystore in file "+f.getAbsolutePath());
				e.printStackTrace();
			}
		}
		return false;
	}

	public boolean writeCurrentKeyStore(boolean chooseFile) {
		if (control.getKeyStore()!=null) {
			File f = null;
			if (chooseFile) {
				f = Dialogs.chooseSaveFile("Select keystore filename", control.getLastDir(), "mykeystore.xml");
			} else {
				f = control.getKeyStore().getFile();
			}
			if (f!=null) {
				try {
					control.getKeyStore().toFile(f);
					return true;
				} catch (Exception ex) {
					Dialogs.showMessage("ERROR: keystore could not be saved to "+control.getKeyStore().getFile().getAbsolutePath());
					ex.printStackTrace();
				}
			}
		}
		return false;
	}

	public void createKeyStore() {
		closeCurrentStore();
		File f = Dialogs.chooseSaveFile("Select keystore filename", control.getLastDir(), "mykeystore.xml");
		if (f!=null) {
			control.setLastDir(f.getParentFile());
			try {
				control.setKeyStore(KeyApprovingStore.createNewKeyApprovingStore(f, control.getMessageHandler()));
				control.getKeyStore().addKeyserverAndPublicKeysFromConfig(configURL);
				update();
			} catch (Exception e) {
				StackTraceElement[] st = e.getStackTrace();
				String er = "";
				for (int i=0;i<st.length;i++) {
					er += st[i].getLineNumber()+" :: "+st[i].getClassName()+" :: "+st[i].getFileName()+" :: "+st[i].getMethodName()+"\n";
				}
				Dialogs.showMessage("ERROR: could not create keystore in file "+f.getAbsolutePath()+"\n"+e.toString()+"\n"+er);
				e.printStackTrace();
			}
		}
	}

	private static void fitAllColumnWidth(JTable t) {
		if (t!=null) {
			t.setAutoResizeMode(0);
			int anz = t.getColumnCount();
			for (int i=0;i<anz;i++) {
				fitColumnWidth(i,t);
			}
		}
	}

	private static void fitColumnWidth(int colIndex,JTable t) {
		try {
			TableColumn column = t.getColumnModel().getColumn(colIndex);
			if (column == null)
				return;

			int modelIndex = column.getModelIndex();
			TableCellRenderer renderer, headerRenderer;
			Component component;
			int colContentWidth = 0;
			int headerWidth = 0;
			int rows = t.getRowCount();

			//		 Get width of column header
			headerRenderer = column.getHeaderRenderer();
			if (headerRenderer == null)
				headerRenderer = t.getTableHeader().getDefaultRenderer();

			Component comp = headerRenderer.getTableCellRendererComponent(
					t, column.getHeaderValue(), false, false, 0, 0);
			headerWidth = comp.getPreferredSize().width + t.getIntercellSpacing().width;

			//		 Get max width of column content
			for (int i = 0; i < rows; i++)
			{
				renderer = t.getCellRenderer(i, modelIndex);
				Object valueAt = t.getValueAt(i, modelIndex);
				component = renderer.getTableCellRendererComponent(t, valueAt, false, false,
						i, modelIndex);
				colContentWidth = Math.max(colContentWidth,
						component.getPreferredSize().width +
						t.getIntercellSpacing().width);
			}
			int colWidth = Math.max(colContentWidth, headerWidth)+15;
			column.setPreferredWidth(colWidth);
			//column.setWidth(colWidth);
			//System.out.println("requiredWidth="+colWidth);
		} catch (Exception ex) {
			return;
		}
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
		SecurityMainFrame s = SecurityMainFrame.getInstance();
		s.buildUi();
		s.openDefauktKeyStore();
		s.setVisible(true);
	}

	//	class KeyTableModel extends DefaultTableModel {
	//	
	//		private String[] header = new String[] {"name","value"};
	//		private Vector<String> rows = new Vector<String>();
	//		
	//		private OSDXKey key;
	//		private Vector<Identity> ids;
	//		private Vector<DataSourceStep> datapath;
	//		private int startIds = 0;
	//		private int startDataPath = 0;
	//		
	//		public KeyTableModel(OSDXKey key) {
	//			this.key = key;
	//			ids = key.getIdentities();
	//			datapath = key.getDatapath();
	//			
	//			rows = new Vector<String>();
	//			rows.add("id");
	//			rows.add("level");
	//			rows.add("usage");
	//			rows.add("parentkeyid");
	//			rows.add("authoritativekeyserver");
	//			startIds = rows.size();
	//			for (int i=0;i<ids.size();i++) {
	//				rows.add("identity "+(i+1));
	//			}
	//			startDataPath = rows.size();
	//			for (int i=0;i<datapath.size();i++) {
	//				rows.add("datapath "+(i+1));
	//			}
	//			
	//		}
	//		
	// 		public Class<?> getColumnClass(int columnIndex) {
	//			return String.class;
	//		}
	//	
	//		public int getColumnCount() {
	//			return header.length;
	//		}
	//	
	//		public String getColumnName(int columnIndex) {
	//			return header[columnIndex];
	//		}
	//	
	//		public int getRowCount() {
	//			if (rows==null) return 0;
	//			return rows.size();
	//		}
	//	
	//		public Object getValueAt(int rowIndex, int columnIndex) {
	//			if (columnIndex==0) {
	//				return rows.get(rowIndex);
	//			}
	//			if (rowIndex==0)
	//				return key.getKeyID();
	//			else if (rowIndex==1) {
	//				return key.getLevelName();
	//			}
	//			else if (rowIndex==2)
	//				return key.getUsageName();
	//			else if (rowIndex==3)
	//				return key.getParentKeyID();
	//			else if (rowIndex==4)
	//				return key.getAuthoritativekeyserver();
	//			else if (rowIndex>=startIds && rowIndex<startIds+ids.size())
	//				return ids.get(rowIndex-startIds).getEmail();
	//			else if (rowIndex>=startDataPath && rowIndex<startDataPath+datapath.size()) {
	//				DataSourceStep s = datapath.get(rowIndex-startDataPath);
	//				return s.getDataSource()+" at "+s.getDataInsertDatetimeString();
	//			}
	//			return null;
	//		}
	//	
	//		public boolean isCellEditable(int rowIndex, int columnIndex) {
	//			if (columnIndex==1) {
	//				if (rowIndex == 3) return true;
	//			}
	//			return false;
	//		}
	//	
	//		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	//			if (columnIndex==1) {
	//				if (rowIndex == 3) {
	//					key.setParentKeyID((String)aValue);
	//				}
	//			}
	//		}
	//		
	//		
	//		
	//	}
	//	
	//	class KeysAndIdentitiesTableModel extends DefaultTableModel {
	//		
	//		private String[] header = new String[] {"key id","level","usage","identities", "parent key id"};
	//		private Vector<OSDXKey> keys;
	//		
	//		public KeysAndIdentitiesTableModel(Vector<OSDXKey> keys) {
	//			this.keys = keys;
	//		}
	//		
	// 		public Class<?> getColumnClass(int columnIndex) {
	//			return String.class;
	//		}
	//	
	//		public int getColumnCount() {
	//			return header.length;
	//		}
	//	
	//		public String getColumnName(int columnIndex) {
	//			return header[columnIndex];
	//		}
	//	
	//		public int getRowCount() {
	//			if (keys==null) return 0;
	//			return keys.size();
	//		}
	//	
	//		public Object getValueAt(int rowIndex, int columnIndex) {
	//			OSDXKey k = keys.get(rowIndex);
	//			if (columnIndex==0)
	//				return k.getKeyID();
	//			else if (columnIndex==1)
	//				return k.getLevelName();
	//			else if (columnIndex==2)
	//				return k.getUsageName();
	//			else if (columnIndex==3) {
	//				String ids = null;
	//				for (Identity id : k.getIdentities()) { 
	//					if (ids==null) ids = id.getEmail();
	//					else ids += ", "+id.getEmail();
	//				}
	//				return ids;
	//			} else if (columnIndex==4) {
	//				String p = k.getParentKeyID();
	//				if (p==null || p.length()==0) return "[no parent]";
	//				return p;
	//			}
	//			
	//			return null;
	//		}
	//	
	//		public boolean isCellEditable(int rowIndex, int columnIndex) {
	//			return false;
	//		}
	//	
	//		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	//			//do nothing
	//		}
	//	}

	class IdentityTableModel extends DefaultTableModel {

		private String[] header = new String[] {"name","value"};
		private Vector<String> rows = new Vector<String>();

		private Identity id;
		private Vector<DataSourceStep> datapath;

		public IdentityTableModel(Identity id) {
			this.id = id;			
			datapath = id.getDatapath();

			rows = new Vector<String>();
			rows.add("identnum");
			rows.add("email");
			rows.add("mnemonic");
			rows.add("phone");
			rows.add("country");
			rows.add("region");
			rows.add("city");
			rows.add("postcode");
			rows.add("company");
			rows.add("unit");
			rows.add("subunit");
			rows.add("function");
			rows.add("surname");
			rows.add("middlename");
			rows.add("name");
			rows.add("note");

			for (int i=0;i<datapath.size();i++) {
				rows.add("datapath "+(i+1));
			}
		}

		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		public int getColumnCount() {
			return header.length;
		}

		public String getColumnName(int columnIndex) {
			return header[columnIndex];
		}

		public int getRowCount() {
			if (rows==null) return 0;
			return rows.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex==0) {
				return rows.get(rowIndex);
			}
			if (rowIndex==0) return id.getIdentNumString();
			else if (rowIndex==1) return id.getEmail();
			else if (rowIndex==2) return id.getMnemonic();
			else if (rowIndex==3) return id.getPhone();
			else if (rowIndex==4) return id.getCountry();
			else if (rowIndex==5) return id.getRegion();
			else if (rowIndex==6) return id.getCity();
			else if (rowIndex==7) return id.getPostcode();
			else if (rowIndex==8) return id.getCompany();
			else if (rowIndex==9) return id.getUnit();
			else if (rowIndex==10) return id.getSubunit();
			else if (rowIndex==11) return id.getFunction();
			else if (rowIndex==12) return id.getSurname();
			else if (rowIndex==13) return id.getMiddlename();
			else if (rowIndex==14) return id.getFirstNames();
			else if (rowIndex==15) return id.getNote();
			else if (rowIndex>=16 && rowIndex<16+datapath.size()) {
				DataSourceStep s = datapath.get(rowIndex-16);
				return s.getDataSource()+" at "+s.getDataInsertDatetimeString();
			}
			return null;
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex==1 && rowIndex<16 && rowIndex>0) 
				return true;
			return false;
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (columnIndex==0) return;
			String s = (String)aValue;

			if (rowIndex== 0) {
				try {
					id.setIdentNum(Integer.parseInt(s));
				} catch (Exception ex) {
					Dialogs.showMessage("Sorry, wrong number format in field identnum.");
				}
			}
			else if (rowIndex== 1) id.setEmail(s);
			else if (rowIndex== 2) id.setMnemonic(s);
			else if (rowIndex== 3) id.setPhone(s);
			else if (rowIndex== 4) id.setCountry(s);
			else if (rowIndex== 5) id.setRegion(s);
			else if (rowIndex== 6) id.setCity(s);
			else if (rowIndex== 7) id.setPostcode(s);
			else if (rowIndex== 8) id.setCompany(s);
			else if (rowIndex== 9) id.setUnit(s);
			else if (rowIndex==10) id.setSubunit(s);
			else if (rowIndex==11) id.setFunction(s);
			else if (rowIndex==12) id.setSurname(s);
			else if (rowIndex==13) id.setMiddlename(s);
			else if (rowIndex==14) id.setFirstNames(s);
			else if (rowIndex==15) id.setNote(s);
			id.createSHA256();
		}
	}



}

