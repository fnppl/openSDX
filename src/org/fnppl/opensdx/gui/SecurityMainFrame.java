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
import java.security.KeyStore;
import java.util.*;

import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;

import org.fnppl.opensdx.keyserver.OSDXKeyServerClient;
import org.fnppl.opensdx.security.*;
import org.fnppl.opensdx.xml.*;


public class SecurityMainFrame extends JFrame {
	
	private int maxWidth = 1200;
	
	private KeyApprovingStore currentKeyStore = null;
	private MessageHandler messageHandler = new DefaultMessageHandler();
	
	private File configFile = new File("src/org/fnppl/opensdx/security/resources/config.xml"); 
	private Vector<KeyServerIdentity> keyservers = null;
	private Vector<PublicKey> knownpublickeys = null;
	
	private File lastDir = getDefaultDir(); //new File(System.getProperty("user.home"));
//	private File lastDir = new File("src/org/fnppl/opensdx/security/resources");
	

	
	
	private HashMap<String, String> props = new HashMap<String, String>(); //GUI layout properties
	
	private ImageIcon iconUp;
	private ImageIcon iconDown;
	private ImageIcon iconRemove;
	
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
		setSize(1024, 768);
		
		readConfig();
	}
	
	private void readConfig() {
		try {
			Element root = Document.fromFile(configFile).getRootElement();
			if (root.getChild("defaultkeyservers")!=null) {
				keyservers = new Vector<KeyServerIdentity>();
				Vector<Element> v = root.getChild("defaultkeyservers").getChildren("keyserver");
				for (Element e : v) {
					keyservers.add(KeyServerIdentity.fromElement(e));
				}
			}
			if (root.getChild("knownapprovedkeys")!=null) {
				knownpublickeys = new Vector<PublicKey>();
				Vector<Element> v = root.getChild("knownapprovedkeys").getChildren("pubkey");
				for (Element e : v) {
					knownpublickeys.add(PublicKey.fromSimplePubKeyElement(e));
				}
			}
			//TODO check localproofs and signatures 
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void initIcons() {
		int w = 20;
		int h = 14;
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		AlphaComposite clear = AlphaComposite.getInstance(AlphaComposite.CLEAR, 1.0F);
		AlphaComposite full = AlphaComposite.getInstance(AlphaComposite.DST_OVER, 1.0F);
	    g.setComposite(clear);
	    g.fillRect(0,0,w,h);
	    g.setComposite(full);
	    g.setColor(Color.BLACK);
	    
	    int s = 4;
	    int posP = h*6/10;
	    int[] xPoints = new int[] {w/2, w   , w/2+s, w/2+s, w/2-s, w/2-s, 0   };
	    int[] yPoints = new int[] {h  , posP, posP , 0    , 0    , posP , posP};
	    g.fillPolygon(xPoints, yPoints, xPoints.length);
	    img.flush();
	    iconDown = new ImageIcon(img);
	    

	    posP = h-posP;
	    img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		g = img.createGraphics();
	    g.setComposite(clear);
	    g.fillRect(0,0,w,h);
	    g.setComposite(full);
	    g.setColor(Color.BLACK);
	    
	    xPoints = new int[] {w/2, w   , w/2+s, w/2+s, w/2-s, w/2-s, 0   };
	    yPoints = new int[] {0  , posP, posP , h    , h    , posP , posP};
	    g.fillPolygon(xPoints, yPoints, xPoints.length);
	    img.flush();
	    iconUp = new ImageIcon(img);
	    
	    posP = h-posP;
	    img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		g = img.createGraphics();
	    g.setComposite(clear);
	    g.fillRect(0,0,w,h);
	    g.setComposite(full);
	    g.setColor(Color.RED);
	    

	    xPoints = new int[] {0,s,w/2,w-s,w,  w/2+s/2,  w,w-s,w/2,s,0,   w/2-s/2};
	    yPoints = new int[] {0,0,h/2-s/2,0,0,    h/2,    h,h,h/2+s/2,h,h,   h/2};
	    g.fillPolygon(xPoints, yPoints, xPoints.length);
	    img.flush();
	    iconRemove = new ImageIcon(img);
	}
	
	public void quit() {
		closeCurrentStore();
		System.exit(0);
	}
	public static File getDefaultDir() {
		File f = new File(System.getProperty("user.home"));
		f = new File(f, "openSDX");
		if(!f.exists()) {
			f.mkdirs();
		}
		return f;
	}
	public boolean openDefauktKeyStore() {
		File f = getDefaultDir();
		
		f = new File(f, "defaultKeyStore.xml");
		if (f.exists()) return openKeyStore(f);
		return false;
	}

//	private void initUICurrentKeyStore() {
//		if (currentKeyStore != null) {
//			this.getContentPane().setVisible(true);
//			mKeysIDs = new KeysAndIdentitiesTableModel(currentKeyStore.getAllKeys());
//			tKeysIDs.setModel(mKeysIDs);
//			fitAllColumnWidth(tKeysIDs);
//			
//		}
//		else {
//			this.getContentPane().setVisible(false);
//			mKeysIDs = new KeysAndIdentitiesTableModel(null);
//			tKeysIDs.setModel(mKeysIDs);
//			fitAllColumnWidth(tKeysIDs);
//		}
//		updateUI();
//	}

	public boolean openKeyStore(File f) {
		try {
			if(f.exists()) {
				KeyApprovingStore kas = KeyApprovingStore.fromFile(f, messageHandler);
				this.currentKeyStore = kas;
				updateUI();
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
				else if(cmd.equalsIgnoreCase("closekeystore")) {
					closeCurrentStore();
				}
				else if(cmd.equalsIgnoreCase("writekeystore")) {
					writeCurrentKeyStore(true);
				}
				else if(cmd.equalsIgnoreCase("generatemasterkeyset")) {
					generateMasterKeySet();
				}
				else if(cmd.equalsIgnoreCase("generatemasterkey")) {
					generateMasterKeyPair();
				}
				else if(cmd.equalsIgnoreCase("encryptfile")) {
					encryptFile();
				}
				else if(cmd.equalsIgnoreCase("decryptfile")) {
					decryptFile();
				}
				else if(cmd.equalsIgnoreCase("signfile")) {
					signFile();
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
		
		jmi = new JMenuItem("CloseKeyStore");
		jmi.setActionCommand("closekeystore");
		jmi.addActionListener(ja);
		jm.add(jmi);
		
		jmi = new JMenuItem("WriteKeyStore to new file");
		jmi.setActionCommand("writekeystore");
		jmi.addActionListener(ja);
		jm.add(jmi);
		
		jmi = new JMenuItem("Quit");
		jmi.setActionCommand("quit");
		jmi.addActionListener(ja);
		jm.add(jmi);
		
		
		jm = new JMenu("Keys");
		jb.add(jm);
		
		jmi = new JMenuItem("Generate new MASTER Key Set");
		jmi.setActionCommand("generatemasterkeyset");
		jmi.addActionListener(ja);
		jm.add(jmi);
		
		jmi = new JMenuItem("Generate new MASTER Key");
		jmi.setActionCommand("generatemasterkey");
		jmi.addActionListener(ja);
		jm.add(jmi);
		
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
		
		jmi = new JMenuItem("EncryptFile (symmetric)");
		jmi.setActionCommand("encryptfile");
		jmi.addActionListener(ja);
		jm.add(jmi);
		
		jmi = new JMenuItem("DecryptFile (symmetric)");
		jmi.setActionCommand("decryptfile");
		jmi.addActionListener(ja);
		jm.add(jmi);
		
		setJMenuBar(jb);
	}
	
	private void buildUi() {
		initIcons();
		makeMenuBar();
		updateUI();
		Helper.centerMe(this, null);
	}
	
	
	private void updateUI() {
		
		JPanel p = new JPanel();
		JScrollPane scroll = new JScrollPane(p);
		setContentPane(scroll);
		
		p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
		
		if (currentKeyStore!=null) {
			Vector<OSDXKeyObject> all = currentKeyStore.getAllKeys();
			int y = 0;
			for (int i=0;i<all.size();i++) {
				OSDXKeyObject key = all.get(i);
				if (key.isMaster()) {
					Vector<OSDXKeyObject> revokekeys = currentKeyStore.getRevokeKeys(key.getKeyID());
					Vector<OSDXKeyObject> subkeys = currentKeyStore.getSubKeys(key.getKeyID());
					Component comp = buildComponent(key, revokekeys, subkeys);
					p.add(comp);
					y++;
				}
			}
		}
		if (keyservers!=null) {
			JPanel pk = new JPanel();
			pk.setBorder(new TitledBorder("KeyServers:"));
			pk.setLayout(new BoxLayout(pk, BoxLayout.PAGE_AXIS));
			for (KeyServerIdentity ksid : keyservers) {
				pk.add(buildComponentKeyServer(ksid));
			}
			p.add(pk);
		}
		if (knownpublickeys!=null) {
			JPanel pk = new JPanel();
			pk.setBorder(new TitledBorder("Known Public Keys:"));
			pk.setLayout(new BoxLayout(pk, BoxLayout.PAGE_AXIS));
			pk.add(buildComponentKnownKeys(knownpublickeys));
			p.add(pk);
		}
		validate();
	}
	
	private Component buildComponent(OSDXKeyObject masterkey, Vector<OSDXKeyObject> revokekeys, Vector<OSDXKeyObject> subkeys) {
		final JPanel p = new JPanel();
		p.setBorder(new TitledBorder("KeyGroup:"));
		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
		p.add(buildComponentMasterKey(masterkey));
		for (OSDXKeyObject key : revokekeys) {
			p.add(buildComponentRevokeKey(key));
		}
		for (OSDXKeyObject key : subkeys) {
			p.add(buildComponentSubKey(key));
		}
		return p;
	}
	
	private Component buildComponentMasterKey(final OSDXKeyObject key) {
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
		addLabelTextFieldPart("authoritative keyserver:", key.getAuthoritativekeyserver(), a, c, y);
		
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
							updateUI();
						}
					} else if (c.equals("edit")) {
						showIdentityEditDialog(ids.get(no),false);
						updateUI();
					} else if (c.equals("up")) {
						key.moveIdentityAtPositionUp(no);
						updateUI();
					}  else if (c.equals("down")) {
						key.moveIdentityAtPositionDown(no);
						updateUI();
					}
				}
			};
			for (int i=0;i<ids.size();i++) {
				y++;
				addIdentityPart(i, ids.size()-1, ids.get(i), a, c, y, editRemoveListener);
			}
		}
		
		Vector<DataSourceStep> dp = key.getDatapath();
		for (int i=0;i<dp.size();i++) {
			y++;
			DataSourceStep s = dp.get(i);
			addLabelTextFieldPart("datapath "+(i+1)+":", s.getDataSource()+" at "+s.getDataInsertDatetimeString(), a, c, y);
		}


		final int w = 600;
		final int h = y*30 + 80;

		JButton head = createHeaderButton("MASTER Key:       "+key.getKeyID(), key.getKeyID(), content, p, w, h);
		
		JPanel b = new JPanel();
		b.setLayout(new FlowLayout(FlowLayout.LEFT));
		int buWidth = 200;
		JButton bu = new JButton("add identity");
		bu.setPreferredSize(new Dimension(buWidth,25));
		bu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Identity id = Identity.newEmptyIdentity();
					boolean ok = showIdentityEditDialog(id, true);
					if (ok) {
						key.addIdentity(id);
						updateUI();
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		b.add(bu);
		
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
	
	private void addIdentityPart(int i, int maxI, Identity id, JPanel a, GridBagConstraints c, int y, ActionListener al) {
		
	JLabel l = new JLabel("identity "+(i+1)+":");
	c.weightx = 0;
	c.weighty = 0.1;
	c.fill = GridBagConstraints.NONE;
	c.gridx = 0;
	c.gridy = y;
	a.add(l, c);
	
	JTextField t = new JTextField(id.getEmail());
	t.setEditable(false);
	c.weightx = 1;
	c.fill = GridBagConstraints.HORIZONTAL;
	c.gridx = 1;
	c.gridy = y;
	c.gridwidth = 1;
	a.add(t,c);
	
	JButton b = new JButton("edit");
	b.setActionCommand("edit:"+i);
	b.addActionListener(al);
	b.setPreferredSize(new Dimension(80, 20));
	c.weightx = 0;
	c.fill = GridBagConstraints.NONE;
	c.gridx = 2;
	c.gridy = y;
	c.gridwidth = 1;
	a.add(b,c);
	
	b = new JButton(iconRemove);
	b.setActionCommand("remove:"+i);
	b.setToolTipText("remove identitiy");
	b.addActionListener(al);
	b.setPreferredSize(new Dimension(30, 20));
	c.weightx = 0;
	c.fill = GridBagConstraints.NONE;
	c.gridx = 3;
	c.gridy = y;
	c.gridwidth = 1;
	a.add(b,c);
	
	if (i!=0) {
		b = new JButton(iconUp);
		b.setActionCommand("up:"+i);
		b.addActionListener(al);
		b.setPreferredSize(new Dimension(30, 20));
		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;
		c.gridx = 4;
		c.gridy = y;
		c.gridwidth = 1;
		a.add(b,c);
	}
	if (i<maxI) {
		b = new JButton(iconDown);
		b.setActionCommand("down:"+i);
		b.addActionListener(al);
		b.setPreferredSize(new Dimension(30, 20));
		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;
		c.gridx = 5;
		c.gridy = y;
		c.gridwidth = 1;
		a.add(b,c);
	}
	}
	
	private Component buildComponentRevokeKey(final OSDXKeyObject key) {
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
		addLabelTextFieldPart("authoritative keyserver:", key.getAuthoritativekeyserver(), a, c, y);
		
		Vector<DataSourceStep> dp = key.getDatapath();
		for (int i=0;i<dp.size();i++) {
			y++;
			DataSourceStep s = dp.get(i);
			addLabelTextFieldPart("datapath "+(i+1)+":", s.getDataSource()+" at "+s.getDataInsertDatetimeString(), a, c, y);
		}


		final int w = 600;
		final int h = y*30 + 80;

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
			@Override
			public void actionPerformed(ActionEvent e) {
				uploadRevokeKeyToKeyServer(key);
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
		
		final JButton head = new JButton("+   "+title);
		String visible = props.get(keyID);
		if (visible==null || visible.equals("NOT VISIBLE")) {
			props.put(keyID,"NOT VISIBLE");
			head.setText("+   "+title);
			p.setPreferredSize(new Dimension(w,28));
			p.setMinimumSize(new Dimension(10,0));
			p.setMaximumSize(new Dimension(maxWidth,28));
			content.setVisible(false);
		} else {
			props.put(keyID,"VISIBLE");
			head.setText("-   "+title);
			p.setPreferredSize(new Dimension(w,h));
			p.setMinimumSize(new Dimension(10,0));
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
					p.setMinimumSize(new Dimension(10,0));
					p.setMaximumSize(new Dimension(maxWidth,28));
					content.setVisible(false);
				} else {
					props.put(keyID,"VISIBLE");
					head.setText("-   "+title);
					p.setPreferredSize(new Dimension(w,h));
					p.setMinimumSize(new Dimension(10,0));
					p.setMaximumSize(new Dimension(maxWidth,h));
					content.setVisible(true);
					props.put(keyID,"VISIBLE");
				}
			}
		});
		return head;
	}
	
	private Component buildComponentSubKey(final OSDXKeyObject key) {
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
		addLabelTextFieldPart("authoritative keyserver:", key.getAuthoritativekeyserver(), a, c, y);
		
		Vector<DataSourceStep> dp = key.getDatapath();
		for (int i=0;i<dp.size();i++) {
			y++;
			DataSourceStep s = dp.get(i);
			addLabelTextFieldPart("datapath "+(i+1)+":", s.getDataSource()+" at "+s.getDataInsertDatetimeString(), a, c, y);
		}


		final int w = 600;
		final int h = y*30 + 80;

		JButton head = createHeaderButton("SUB Key:      "+key.getKeyID(), key.getKeyID(), content, p, w, h);
		
		JPanel b = new JPanel();
		b.setLayout(new FlowLayout(FlowLayout.LEFT));
		JButton bu = new JButton("sign file");
		bu.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				File f = Dialogs.chooseOpenFile("Please select file for signing", lastDir, "");
				if (f!=null && f.exists()) {
					signFile(key,f);
				}
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
		
		final JTextField tHost = addLabelTextFieldPart("host:", host, a, c, y,true); y++;
		final JTextField tPort = addLabelTextFieldPart("port:", ""+port, a, c, y,true); y++;
		final JButton bu = new JButton("save changes");
		bu.setEnabled(false);
		DocumentListener chListen = new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				action();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				action();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				action();
			}
			private void action() {
				if (tHost.getText().equals(keyserver.getHost()) && tPort.getText().equals(""+keyserver.getPort())) {
					bu.setEnabled(false);
				} else {
					bu.setEnabled(true);
				}
			}
		};
		tHost.getDocument().addDocumentListener(chListen);
		tPort.getDocument().addDocumentListener(chListen);
		
		Vector<PublicKey> keys = keyserver.getKnownKeys();
		
		for (int i=0;i<keys.size();i++) {
			y++;
			addLabelTextFieldPart("known public key "+(i+1)+":", keys.get(i).getKeyID(), a, c, y);
		}

		final int w = 600;
		final int h = y*30 + 80;

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
				props.put(keyserver.getHost()+":"+keyserver.getPort(), "VISIBLE");
				updateUI();
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
	
	private Component buildComponentKnownKeys(Vector<PublicKey> keys) {
		final JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		
		final JPanel content = new JPanel();
		content.setLayout(new BorderLayout());
				
		JPanel a = new JPanel();
		int y = -1;
		GridBagLayout gb = new GridBagLayout();		
		a.setLayout(gb);
		
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.insets = new Insets(5, 5, 0, 0);
		
		for (int i=0;i<keys.size();i++) {
			y++;
			addLabelTextFieldPart("known public key "+(i+1)+":", keys.get(i).getKeyID(), a, c, y);
		}

		final int w = 600;
		final int h = y*30 + 80;

		JButton head = createHeaderButton("known public keys:", "known public keys", content, p, w, h);
		
		p.add(head, BorderLayout.NORTH);
		JScrollPane scrollContent = new JScrollPane(content);
		p.add(scrollContent, BorderLayout.CENTER);
		content.add(a,BorderLayout.CENTER);
		
		return p;
	}
	
//	private static void addComponent(Container cont, GridBagLayout gbl, Component c, int x, int y, int w, int h, double wx, double wy) {
//		GridBagConstraints gbc = new GridBagConstraints();
//		gbc.fill = GridBagConstraints.BOTH;
//		gbc.gridx = x;
//		gbc.gridy = y;
//		gbc.gridwidth = w;
//		gbc.gridheight = h;
//		gbc.weightx = wx;
//		gbc.weighty = wy;
//		gbc.insets = new Insets(5, 5, 5, 5);
//		gbl.setConstraints(c, gbc);
//		cont.add(c);
//	}
	
	
	private void decryptFile() {
		File f = Dialogs.chooseOpenFile("Please select file for decryption", lastDir, "");
		if (f != null) {
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
						if (z.equals("#### openSDX symmetrical encrypted file ####")) terminationFound = true;
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
				String mantra = e.getChildText("mantraname");
				String p = Dialogs.showPasswordDialog("Enter password", "Please enter password for mantra:\n"+mantra);
				if (p != null) {
					if (!Arrays.equals(
						SecurityHelper.getSHA256(p.getBytes()),
						SecurityHelper.HexDecoder.decode(e.getChildText("pass_sha256"))
					)) {
						Dialogs.showMessage("Sorry, wrong password.");
						return;
					}
							
					byte[] initv = SecurityHelper.HexDecoder.decode(e.getChildText("initvector"));
					SymmetricKey key = SymmetricKey.getKeyFromPass(p.toCharArray(), initv);
					
					File fdec = new File(f.getParent(),e.getChildText("dataname")+".dec");
					
					if (detached) {
						File fenc = new File(f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf('.')));
						in = new FileInputStream(fenc);
					}
					
					FileOutputStream out = new FileOutputStream(fdec);
					key.decrypt(in, out);
					in.close();
					out.close();
					
					Dialogs.showMessage("Decryption succeeded.");
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

	private void encryptFile() {
		//Dialogs.showMessage("feature not implented.");
		File f = Dialogs.chooseOpenFile("Please select file for encryption", lastDir, "");
		if (f != null) {
			int detached = Dialogs.showYES_NO_Dialog("Create detached metadata", "Do you want to create a detached metadata file?");
			String[] p = Dialogs.showNewMantraPasswordDialog("Passphrase for encryption of file:\n"+f.getName());
			if (p != null) {
				try {
					byte[] initv = SecurityHelper.getRandomBytes(16);
					SymmetricKey key = SymmetricKey.getKeyFromPass(p[1].toCharArray(), initv);
					
					Element e = new Element("symmetric_encrytion");
					e.addContent("dataname", f.getName());
					e.addContent("origlength", ""+f.length());
					e.addContent("lastmodified", OSDXKeyObject.datemeGMT.format(new Date(f.lastModified())));
					e.addContent("mantraname",p[0]);
					e.addContent("pass_sha256", SecurityHelper.HexDecoder.encode(SecurityHelper.getSHA256(p[1].getBytes()), ':', -1));
					e.addContent("algo","AES@256");
					e.addContent("initvector", SecurityHelper.HexDecoder.encode(initv, ':', -1));
					e.addContent("padding", "CBC/PKCS#7");
					Document d = Document.buildDocument(e);
					
					if (detached == Dialogs.YES) {
						encryptFileDetached(f, key, d);
					} else {
						encryptFileInline(f, key, d);
					}
					
					Dialogs.showMessage("Encryption succeeded.");
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	
	private void encryptFileDetached(File f, SymmetricKey key, Document d) throws Exception {
		File fenc = new File(f.getAbsolutePath()+".enc");
		FileInputStream in = new FileInputStream(f);
		FileOutputStream out = new FileOutputStream(fenc);
		key.encrypt(in, out);
		in.close();
		out.close();
		
		File fxml = new File(f.getAbsolutePath()+".enc.xml");
		d.writeToFile(fxml);
	}
	
	private void encryptFileInline(File f, SymmetricKey key, Document d) throws Exception {
		File fenc = new File(f.getAbsolutePath()+".osdx");
		
		FileInputStream in = new FileInputStream(f);
		FileOutputStream out = new FileOutputStream(fenc);
		out.write("#### openSDX symmetrical encrypted file ####\n".getBytes("UTF-8"));
		d.output(out);
//		out.write("\n".getBytes("UTF-8"));
		out.write("#### openSDX symmetrical encrypted file ####\n".getBytes("UTF-8"));
		key.encrypt(in, out);
		in.close();
		out.close();
	}
	
	private void verifySignature() {
		if (currentKeyStore!=null) {
			File f = Dialogs.chooseOpenFile("Please select signature file for verification", lastDir, "");
			if (f!=null && f.exists()) {
				try {
					Signature s = Signature.fromFile(f);
					File origFile = null;
					if (f.getName().endsWith("_signature.xml")) {
						origFile = new File(f.getAbsolutePath().substring(0,f.getAbsolutePath().length()-14));
						System.out.println("checking file: "+origFile.getAbsolutePath());
						if (!origFile.exists()) origFile = null;
					}
					if (origFile == null) {
						origFile = Dialogs.chooseOpenFile("Please select original file for signature verification", lastDir, "");
					}
					if (origFile != null) {
						boolean v = s.tryVerificationFile(origFile);
						if (v) {
							Dialogs.showMessage("Signature verified!");
						} else {
							Dialogs.showMessage("Signature NOT verified!");
						}
					}
				} catch (Exception e) {
					Dialogs.showMessage("ERROR: verifying signature for file: "+f.getAbsolutePath()+" failed");
					e.printStackTrace();
				}
			}
		}
	}

	private void signFile() {
		if (currentKeyStore!=null) {
			Vector<OSDXKeyObject> keys = currentKeyStore.getAllSigningSubKeys(); 
			if (keys.size()==0) {
				Dialogs.showMessage("Sorry, no subkeys for signing in keystore");
				return;
			}
			Vector<String> keyids = new Vector<String>();
			for (OSDXKeyObject k: keys) {
				String id = k.getKeyID();
				keyids.add(id);
			}
			File f = Dialogs.chooseOpenFile("Please select file for signing", lastDir, "");
			if (f!=null) {
				int a = Dialogs.showSelectDialog("Select key", "Please select key for signing", keyids);
				if (a>=0) {
					OSDXKeyObject key = keys.get(a);
					signFile(key,f);
				}
			}
		}
	}
	
	private void signFile(OSDXKeyObject key, File file) {
		try {
			if (!key.isPrivateKeyUnlocked()) key.unlockPrivateKey(messageHandler);
			
			File fileout = new File(file.getAbsolutePath()+"_signature.xml");
			Signature.createSignatureFile(file, fileout, key);
			if (fileout.exists())
				Dialogs.showMessage("Signature creation succeeded. \nfile: "+fileout.getAbsolutePath());
		} catch (Exception ex) {
			Dialogs.showMessage("ERROR: Creating signature for file: "+file.getAbsolutePath()+" failed");
			ex.printStackTrace();
		}
	}
	
	private void generateRevokeKey(final OSDXKeyObject parentKey) {
		final JDialog d = Dialogs.getWaitDialog("Generating new REVOKE Key,\nplease wait...");
	   Thread t = new Thread() {
	    	public void run() {
				try {
					AsymmetricKeyPair kp =  AsymmetricKeyPair.generateAsymmetricKeyPair();
					OSDXKeyObject k = OSDXKeyObject.fromKeyPair(kp);
					k.setLevel(OSDXKeyObject.LEVEL_REVOKE);
					k.setUsage(OSDXKeyObject.USAGE_SIGN);
					k.setParentKey(parentKey);
					currentKeyStore.addKey(k);
					releaseUILock();
					updateUI();
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
			updateUI();
	    }
	}
	
	private void generateSubKey(final OSDXKeyObject parentKey) {
		final JDialog d = Dialogs.getWaitDialog("Generating new SUB Key,\nplease wait...");
	    Thread t = new Thread() {
	    	public void run() {
				try {
					AsymmetricKeyPair kp =  AsymmetricKeyPair.generateAsymmetricKeyPair();
					OSDXKeyObject k = OSDXKeyObject.fromKeyPair(kp);
					k.setLevel(OSDXKeyObject.LEVEL_SUB);
					k.setUsage(OSDXKeyObject.USAGE_SIGN);
					k.setParentKey(parentKey);
					currentKeyStore.addKey(k);
					releaseUILock();
					updateUI();
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
			updateUI();
	    }
	}
	
	private boolean uploadMasterKeyToKeyServer(OSDXKeyObject key) {
		if (keyservers == null) {
			Dialogs.showMessage("Sorry, no keyservers found.");
			return false;
		}
		Vector<String> keyservernames = new Vector<String>();
		for (KeyServerIdentity id : keyservers) {
			keyservernames.add(id.getHost()+":"+id.getPort());
		}
		int ans = Dialogs.showSelectDialog("Select KeyServer", "Please select a KeyServer for uploading MASTER Key.", keyservernames);
		if (ans>=0) {
			return uploadMasterKeyToKeyServer(key, keyservers.get(ans));
		}
		return false;
	}
	
	private boolean uploadRevokeKeyToKeyServer(OSDXKeyObject key) {
		String parent = key.getParentKeyID();
		int iAt = parent.indexOf('@');
		if (iAt<0 || parent.toLowerCase().endsWith("@local")) {
			Dialogs.showMessage("Can only upload if authoritative keyserver of MASTER Key is not LOCAL");
			return false;
		}
		if (keyservers==null || keyservers.size()==0) {
			Dialogs.showMessage("No keyserver available.");
			return false;
		}
		
		String host = parent.substring(iAt+1).toLowerCase();
		KeyServerIdentity keyserver = null;
		for (KeyServerIdentity kid : keyservers) {
			if (kid.getHost().toLowerCase().equals(host)) {
				keyserver = kid;
				break;
			}
		}
		if (keyserver!=null) {
			try {
				int confirm = Dialogs.showYES_NO_Dialog("Confirm upload", "Are you sure you want to upload the REVOKE Key:\n"+key.getKeyID()+"\nfor MASTER Key: "+parent+"\nto KeyServer: "+keyserver.getHost()+"?\nThis will also change your authoritative keyserver for this key.");
				if (confirm==Dialogs.YES) {
					OSDXKeyServerClient client =  new OSDXKeyServerClient(keyserver.getHost(), keyserver.getPort());
					OSDXKeyObject masterkey = currentKeyStore.getKey(parent);
					if (!masterkey.isPrivateKeyUnlocked()) masterkey.unlockPrivateKey(messageHandler);
					boolean ok = client.putRevokeKey(key.getPubKey(), masterkey);
					key.setAuthoritativeKeyServer(keyserver.getHost());
					props.put(key.getKeyID(), "VISIBLE");
					updateUI();
					Dialogs.showMessage("Upload of REVOKE Key:\n"+key.getKeyID()+"\nto KeyServer: "+keyserver.getHost()+"\nsuccessful!");
					return ok;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}	
		}
		return false;
	}

	private boolean uploadMasterKeyToKeyServer(OSDXKeyObject key, KeyServerIdentity keyserver) {
		Vector<Identity> ids = key.getIdentities();
		if (ids!=null && ids.size()!=0) {
			try {
				int confirm = Dialogs.showYES_NO_Dialog("Confirm upload", "Are you sure you want to upload the MASTER Key:\n"+key.getKeyID()+"\nwith Identity: "+ids.get(0).getEmail()+"\nto KeyServer: "+keyserver.getHost()+"?\nThis will also change your authoritative keyserver for this key.");
				if (confirm==Dialogs.YES) {
					OSDXKeyServerClient client =  new OSDXKeyServerClient(keyserver.getHost(), keyserver.getPort());
					boolean ok = client.putMasterKey(key.getPubKey(), ids.get(0));
					Vector<OSDXKeyObject> childkeys = currentKeyStore.getRevokeKeys(key.getKeyID());
					childkeys.addAll(currentKeyStore.getSubKeys(key.getKeyID()));
					
					key.setAuthoritativeKeyServer(keyserver.getHost());
					for (OSDXKeyObject k : childkeys) {
						k.setParentKey(key);
					}
					props.put(key.getKeyID(), "VISIBLE");
					updateUI();
					Dialogs.showMessage("Upload of MASTER Key:\n"+key.getKeyID()+"\nwith Identity: "+ids.get(0).getEmail()+"\nto KeyServer: "+keyserver.getHost()+"\nsuccessful!");
					return ok;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			Dialogs.showMessage("Upload failed. No identity found.");
		}
		return false;
	}
	
	private boolean showIdentityEditDialog(Identity id, boolean canCancel) {
		final JDialog d = new JDialog(instance);
		d.setTitle("Edit Identity");
		final boolean[] isOK = new boolean[] {!canCancel};		
		
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		
		JTable edit = new JTable();
		edit.setModel(new IdentityTableModel(id));
		fitAllColumnWidth(edit);
		edit.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		TableColumn column = edit.getColumnModel().getColumn(0);
		column.setPreferredWidth(100);
		column.setMaxWidth(100);
		
		p.add(new JScrollPane(edit), BorderLayout.CENTER);
			
		JPanel ps = new JPanel();
		JButton ok = new JButton("ok");
		ok.setPreferredSize(new Dimension(200,30));
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isOK[0] = true;
				d.dispose();
			}
		});
		ps.add(ok);
		
		
		d.setLayout(new BorderLayout());
		
		d.setSize(700, 400);
		d.add(p, BorderLayout.CENTER);
		d.add(ps, BorderLayout.SOUTH);
		d.setModal(true);
		
		Helper.centerMe(d, null);
		
		d.setVisible(true);
		return isOK[0];
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
		if (currentKeyStore!=null && currentKeyStore.hasUnsavedChanges()) {
			int a = Dialogs.showYES_NO_Dialog("Save keystore", "Your current keystore has unsaved changes.\nDo you want to save it?");
			if (a==Dialogs.YES) {
				writeCurrentKeyStore(false);
			}
		}
		currentKeyStore = null;
		updateUI();
	}
	
	public void generateMasterKeyPair() {
		if (currentKeyStore!=null) {
			try {
				AsymmetricKeyPair kp =  AsymmetricKeyPair.generateAsymmetricKeyPair();
				OSDXKeyObject k = OSDXKeyObject.fromKeyPair(kp);
				k.setUsage(OSDXKeyObject.USAGE_SIGN);
				k.setLevel(OSDXKeyObject.LEVEL_MASTER);
				k.createLockedPrivateKey(messageHandler);
				currentKeyStore.addKey(k);
				updateUI();
				releaseUILock();
			} catch (Exception ex) {
				releaseUILock();
				Dialogs.showMessage("ERROR: could not generate new keypair.");
				ex.printStackTrace();
			}
		}
	}
	
	public void generateMasterKeySet() {
		if (currentKeyStore!=null) {
			boolean ok = false;
			final Identity id;
			try {
				id = Identity.newEmptyIdentity();
				ok = showIdentityEditDialog(id, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (!ok) return;
			
			final JDialog wait = Dialogs.getWaitDialog("Generating new MASTER KEY, REVOKE KEY, SUB KEY set,\n please wait ...");
			Thread t = new Thread() {
				public void run() {
					try {
						AsymmetricKeyPair masterkp =  AsymmetricKeyPair.generateAsymmetricKeyPair();
						AsymmetricKeyPair revokekp =  AsymmetricKeyPair.generateAsymmetricKeyPair();
						AsymmetricKeyPair subkp =  AsymmetricKeyPair.generateAsymmetricKeyPair();
						
						
						OSDXKeyObject masterkey = OSDXKeyObject.fromKeyPair(masterkp);
						masterkey.setLevel(OSDXKeyObject.LEVEL_MASTER);
						masterkey.setUsage(OSDXKeyObject.USAGE_SIGN);
						masterkey.createLockedPrivateKey(messageHandler);
						

						OSDXKeyObject revokekey = OSDXKeyObject.fromKeyPair(revokekp);
						revokekey.setLevel(OSDXKeyObject.LEVEL_REVOKE);
						revokekey.setUsage(OSDXKeyObject.USAGE_SIGN);
						revokekey.setParentKey(masterkey);
						revokekey.createLockedPrivateKey(messageHandler);
						
						OSDXKeyObject subkey = OSDXKeyObject.fromKeyPair(subkp);
						subkey.setLevel(OSDXKeyObject.LEVEL_SUB);
						subkey.setUsage(OSDXKeyObject.USAGE_SIGN);
						subkey.setParentKey(masterkey);
						subkey.createLockedPrivateKey(messageHandler);
						
						currentKeyStore.addKey(masterkey);
						currentKeyStore.addKey(revokekey);
						currentKeyStore.addKey(subkey);
						
						wait.dispose();
						releaseUILock();
						updateUI();
					} catch (Exception ex) {
						releaseUILock();
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
		File f = Dialogs.chooseOpenFile("Select keystore filename", lastDir, "mykeystore.xml");
		if (f!=null && f.exists()) {
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
		if (currentKeyStore!=null) {
			File f = null;
			if (chooseFile) {
				f = Dialogs.chooseSaveFile("Select keystore filename", lastDir, "mykeystore.xml");
			} else {
				f = currentKeyStore.getFile();
			}
			if (f!=null) {
				try {
					currentKeyStore.toFile(f);
					return true;
				} catch (Exception ex) {
					Dialogs.showMessage("ERROR: keystore could not be saved to "+currentKeyStore.getFile().getAbsolutePath());
					ex.printStackTrace();
				}
			}
		}
		return false;
	}
	
	public void createKeyStore() {
		closeCurrentStore();
		File f = Dialogs.chooseSaveFile("Select keystore filename", lastDir, "mykeystore.xml");
		if (f!=null) {
			try {
				currentKeyStore = KeyApprovingStore.createNewKeyApprovingStore(f, messageHandler);
				updateUI();
			} catch (Exception e) {
				Dialogs.showMessage("ERROR: could not create keystore in file "+f.getAbsolutePath());
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
//		private OSDXKeyObject key;
//		private Vector<Identity> ids;
//		private Vector<DataSourceStep> datapath;
//		private int startIds = 0;
//		private int startDataPath = 0;
//		
//		public KeyTableModel(OSDXKeyObject key) {
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
//		private Vector<OSDXKeyObject> keys;
//		
//		public KeysAndIdentitiesTableModel(Vector<OSDXKeyObject> keys) {
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
//			OSDXKeyObject k = keys.get(rowIndex);
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
			if (rowIndex==0)
				return id.getEmail();
			else if (rowIndex==1)
				return id.getMnemonic();
			else if (rowIndex==2)
				return id.getPhone();
			else if (rowIndex==3)
				return id.getCountry();
			else if (rowIndex==4)
				return id.getRegion();
			else if (rowIndex==5)
				return id.getCity();
			else if (rowIndex==6)
				return id.getPostcode();
			else if (rowIndex==7)
				return id.getCompany();
			else if (rowIndex==8)
				return id.getUnit();
			else if (rowIndex==9)
				return id.getSubunit();
			else if (rowIndex==10)
				return id.getFunction();
			else if (rowIndex==11)
				return id.getSurname();
			else if (rowIndex==12)
				return id.getMiddlename();
			else if (rowIndex==13)
				return id.getName();
			else if (rowIndex==14)
				return id.getNote();
			else if (rowIndex>=15 && rowIndex<15+datapath.size()) {
				DataSourceStep s = datapath.get(rowIndex-15);
				return s.getDataSource()+" at "+s.getDataInsertDatetimeString();
			}
			return null;
		}
	
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex==1 && rowIndex<14) 
				return true;
			return false;
		}
	
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (columnIndex==0) return;
			String s = (String)aValue;
			if      (rowIndex== 0) id.setEmail(s);
			else if (rowIndex== 1) id.setMnemonic(s);
			else if (rowIndex== 2) id.setPhone(s);
			else if (rowIndex== 3) id.setCountry(s);
			else if (rowIndex== 4) id.setRegion(s);
			else if (rowIndex== 5) id.setCity(s);
			else if (rowIndex== 6) id.setPostcode(s);
			else if (rowIndex== 7) id.setCompany(s);
			else if (rowIndex== 8) id.setUnit(s);
			else if (rowIndex== 9) id.setSubunit(s);
			else if (rowIndex==10) id.setFunction(s);
			else if (rowIndex==11) id.setSurname(s);
			else if (rowIndex==12) id.setMiddlename(s);
			else if (rowIndex==13) id.setName(s);
			else if (rowIndex==14) id.setNote(s);
		}
	}


	
}

