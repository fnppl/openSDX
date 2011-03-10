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
import java.util.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.fnppl.opensdx.security.AsymmetricKeyPair;
import org.fnppl.opensdx.security.DataSourceStep;
import org.fnppl.opensdx.security.Identity;
import org.fnppl.opensdx.security.KeyApprovingStore;
import org.fnppl.opensdx.security.OSDXKeyObject;
import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.security.Signature;
import org.fnppl.opensdx.security.SymmetricKey;
import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;

import sun.misc.BASE64Encoder;


public class SecurityMainFrame extends JFrame {
	private KeyApprovingStore currentKeyStore = null;
	
	//private File lastDir = new File(System.getProperty("user.home"));
	private File lastDir = new File("src/org/fnppl/opensdx/security/resources");
	
	private JTable tKeysIDs;
	private KeysAndIdentitiesTableModel mKeysIDs;
	
	private JTable tKeylogs;
	
	
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
	}
	
	public void quit() {
		closeCurrentStore();
		System.exit(0);
	}
	
	public boolean openDefauktKeyStore() {
		File f = new File(System.getProperty("user.home"));
		f = new File(f, "openSDX");
		if(!f.exists()) {
			f.mkdirs();
		}
		
		f = new File(f, "defaultKeyStore.xml");
		
		return openKeyStore(f);
	}

	private void initUICurrentKeyStore() {
		if (currentKeyStore!=null) {
			this.getContentPane().setVisible(true);
			mKeysIDs = new KeysAndIdentitiesTableModel(currentKeyStore.getAllKeys());
			tKeysIDs.setModel(mKeysIDs);
			fitAllColumnWidth(tKeysIDs);
			
		} else {
			this.getContentPane().setVisible(false);
			mKeysIDs = new KeysAndIdentitiesTableModel(null);
			tKeysIDs.setModel(mKeysIDs);
			fitAllColumnWidth(tKeysIDs);
		}
	}

	public boolean openKeyStore(File f) {
		try {
			if(f.exists()) {
				KeyApprovingStore kas = KeyApprovingStore.fromFile(f);
				this.currentKeyStore = kas;
				initUICurrentKeyStore();
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
				else if(cmd.equalsIgnoreCase("generatekeypair")) {
					generateKeyPair(true);
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
		
		jmi = new JMenuItem("GenerateKeyPair");
		jmi.setActionCommand("generatekeypair");
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
		makeMenuBar();
		
		JPanel jp = new JPanel();
		setContentPane(jp);
		GridBagLayout gb = new GridBagLayout();		
		jp.setLayout(gb);
		
		
		//keys and identities
		tKeysIDs = new JTable();
		tKeysIDs.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent ev) {
					if (ev.getClickCount()>1) {
						int row = tKeysIDs.rowAtPoint(ev.getPoint());
						int col = tKeysIDs.columnAtPoint(ev.getPoint());
						if (row>=0 && row<tKeysIDs.getModel().getRowCount() && col>=0 && col<tKeysIDs.getModel().getColumnCount()) {
							showKeyEditDialog(currentKeyStore.getAllKeys().get(row),false);
							tKeysIDs.setModel(new KeysAndIdentitiesTableModel(currentKeyStore.getAllKeys()));
							fitAllColumnWidth(tKeysIDs);
						}
					}
				}
		});
		initUICurrentKeyStore();
		
		JPanel pKI = new JPanel();
		pKI.setLayout(new BorderLayout());
		pKI.setBorder(new TitledBorder("Keys and Identities"));
		pKI.add(new JScrollPane(tKeysIDs), BorderLayout.CENTER);


		//keylogs
		tKeylogs = new JTable();
		tKeylogs.setModel(new DefaultTableModel(new String[]{"keylog","test","bla"}, 5));
		JPanel pKL = new JPanel();
		pKL.setLayout(new BorderLayout());
		pKL.setBorder(new TitledBorder("Keylogs"));
		pKL.add(new JScrollPane(tKeylogs), BorderLayout.CENTER);
		
		
		
		addComponent(jp,gb,pKI,0,0,1,1,1.0,0.5);
		//addComponent(jp,gb,pKL,0,1,1,1,1.0,0.5);
		
	}
	
	private static void addComponent(Container cont, GridBagLayout gbl, Component c, int x, int y, int w, int h, double wx, double wy) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = w;
		gbc.gridheight = h;
		gbc.weightx = wx;
		gbc.weighty = wy;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbl.setConstraints(c, gbc);
		cont.add(c);
	}
	

	private void decryptFile() {
		File f = Dialogs.chooseOpenFile("Please select file for decryption", lastDir.getAbsolutePath(), "");
		if (f!=null) {
			try {
				boolean detached = f.getName().toLowerCase().endsWith(".xml");
				Element e = null;
				FileInputStream in = null;
				if (detached) {
					e = Document.fromFile(f).getRootElement();
				} else {
					in = new FileInputStream(f);
					String first = readLine(in);
					StringBuffer b = new StringBuffer();
					String z = null;
					boolean terminationFound = false;
					while (!terminationFound && (z=readLine(in))!=null) {
						if (z.equals("#### openSDX encrypted file ####")) terminationFound = true;
						else b.append(z);
					}
					if (terminationFound) {
						e = Document.fromStream(new ByteArrayInputStream(b.toString().getBytes("UTF-8"))).getRootElement();
					} else {
						Dialogs.showMessage("Sorry, wrong file format");
						return;
					}
				}
				String mantra = e.getChildText("mantraname");
				String p = Dialogs.showPasswordDialog("Enter password", "Please enter password for mantra:\n"+mantra);
				if (p!=null) {
				
					if (!Arrays.equals(
						SecurityHelper.getSHA1(p.getBytes()),
						SecurityHelper.HexDecoder.decode(e.getChildText("pass_sha1"))
					)) {
						Dialogs.showMessage("Sorry, wrong password.");
						return;
					}
							
					byte[] initv = SecurityHelper.HexDecoder.decode(e.getChildText("initvector"));
					SymmetricKey key = SymmetricKey.getKeyFromPass(p.toCharArray(), initv);
					
					File fdec = new File(f.getParent(),e.getChildText("dataname")+".dec");
					
					if (detached) {
						File fenc = new File(f.getAbsolutePath().substring(0,f.getAbsolutePath().lastIndexOf('.')));
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
		File f = Dialogs.chooseOpenFile("Please select file for encryption", lastDir.getAbsolutePath(), "");
		if (f!=null) {
			int detached = Dialogs.showYES_NO_Dialog("Create detached metadata", "Do you want to create a detached metadata file?");
			String[] p = Dialogs.showNewMantraPasswordDialog();
			if (p!=null) {
				try {
					byte[] initv = SecurityHelper.getRandomBytes(16);
					SymmetricKey key = SymmetricKey.getKeyFromPass(p[1].toCharArray(), initv);
					
					Element e = new Element("symmetric_encrytion");
					e.addContent("dataname",f.getName());
					e.addContent("mantraname",p[0]);
					e.addContent("pass_sha1",SecurityHelper.HexDecoder.encode(SecurityHelper.getSHA1(p[1].getBytes()), ':', -1));
					e.addContent("algo","AES@256");
					e.addContent("initvector",SecurityHelper.HexDecoder.encode(initv, ':', -1));
					e.addContent("padding","CBC/PKCS#5");
					Document d = Document.buildDocument(e);
					
					if (detached == Dialogs.YES) {
						encryptFileDetached(f,key,d);
					} else {
						encryptFileInline(f,key,d);
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
		out.write("#### openSDX encrypted file ####\n".getBytes("UTF-8"));
		key.encrypt(in, out);
		in.close();
		out.close();
	}
	
	private void verifySignature() {
		if (currentKeyStore!=null) {
			File f = Dialogs.chooseOpenFile("Please select signature file for verification", lastDir.getAbsolutePath(), "");
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
						origFile = Dialogs.chooseOpenFile("Please select original file for signature verification", lastDir.getAbsolutePath(), "");
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
			Vector<OSDXKeyObject> keys = currentKeyStore.getAllSigningKeys(); 
			if (keys.size()==0) {
				Dialogs.showMessage("Sorry, no signing keys in keystore");
				return;
			}
			Vector<String> keyids = new Vector<String>();
			for (OSDXKeyObject k: keys) {
				keyids.add(k.getKeyID());
			}
			File f = Dialogs.chooseOpenFile("Please select file for signing", lastDir.getAbsolutePath(), "");
			if (f!=null) {
				int a = Dialogs.showSelectDialog("Select key", "Please select key for signing", keyids);
				if (a>=0) {
					OSDXKeyObject key = keys.get(a);
					try {
						File fileout = new File(f.getAbsolutePath()+"_signature.xml");
						Signature.createSignatureFile(f, fileout, key);
						if (fileout.exists())
							Dialogs.showMessage("Signature creation succeeded. \nfile: "+fileout.getAbsolutePath());
					} catch (Exception ex) {
						Dialogs.showMessage("ERROR: Creating signature for file: "+f.getAbsolutePath()+" failed");
						ex.printStackTrace();
					}
				}
			}
		}
	}	
	
	
	private boolean showKeyEditDialog(final OSDXKeyObject key, boolean canCancel) {
		final JDialog d = new JDialog(instance);
		d.setTitle("Edit Key");
		
		final boolean[] isOK = new boolean[] {!canCancel};
		
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.setBorder(new TitledBorder("Edit Key: "+key.getKeyID()));
		
		final JTable edit = new JTable();
		edit.setModel(new KeyTableModel(key));
		fitAllColumnWidth(edit);
		
		edit.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent ev) {
				if (ev.getClickCount()>1) {
					int row = edit.rowAtPoint(ev.getPoint());
					int col = edit.columnAtPoint(ev.getPoint());
					if (row>=0 && row<edit.getModel().getRowCount() && col>=0 && col<edit.getModel().getColumnCount()) {
						if (((String)edit.getValueAt(row, 0)).startsWith("identity")) {
							int firstID = row;
							while (firstID>0 && ((String)edit.getValueAt(firstID-1, 0)).startsWith("identity")) {
								firstID--;
							}
							Identity id = key.getIdentities().get(row-firstID);
							showIdentityEditDialog(id,false);
							edit.setModel(new KeyTableModel(key));
							fitAllColumnWidth(edit);
						}
						if (edit.getValueAt(row, 0).equals("level")) {
							int a = Dialogs.showSelectDialog("Select Level", "Please select key level", OSDXKeyObject.level_name);
							if (a>=0) {
								key.setLevel(a);
								edit.setModel(edit.getModel()); //update
							}
						}
						if (edit.getValueAt(row, 0).equals("usage")) {
							int a = Dialogs.showSelectDialog("Select Usage", "Please select key usage", OSDXKeyObject.usage_name);
							if (a>=0) {
								key.setUsage(a);
								edit.setModel(edit.getModel()); //update
							}
						}
						if (edit.getValueAt(row, 0).equals("parentkeyid")) {
							Vector<OSDXKeyObject> keys = currentKeyStore.getAllSigningKeys(); 
							if (keys.size()>0) {
								Vector<String> keyids = new Vector<String>();
								for (OSDXKeyObject k: keys) {
									keyids.add(k.getKeyID());
								}
								int a = Dialogs.showSelectDialog("Select parent key id", "Please select parent key id", keyids);
								if (a>=0) {
									key.setParentKey(keys.get(a));
									edit.setModel(edit.getModel()); //update
								}
							}
						}
						
					}
					
				}
			}
		});
		p.add(new JScrollPane(edit), BorderLayout.CENTER);
		
		JPanel pb = new JPanel();
		p.add(pb,BorderLayout.SOUTH);
		pb.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		JButton addID = new JButton("add identity");
		addID.setPreferredSize(new Dimension(150,25));
		addID.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Identity id = Identity.newEmptyIdentity();
					boolean ok = showIdentityEditDialog(id, true);
					if (ok) {
						key.addIdentity(id);
						edit.setModel(new KeyTableModel(key));
						fitAllColumnWidth(edit);
						edit.validate();
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		pb.add(addID);
		
		JButton removeID = new JButton("remove identity");
		removeID.setPreferredSize(new Dimension(150,25));
		removeID.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					int[] sel = edit.getSelectedRows();
					if (sel==null) Dialogs.showMessage("Sorry, no identities selected.");
					
					Vector<Identity> ids = new Vector<Identity>(); 
					for (int i=0;i<sel.length;i++) {
						String name = (String)edit.getModel().getValueAt(sel[i],0); 
						if (name.startsWith("identity")) {
							int no = Integer.parseInt(name.substring(9))-1;
							ids.add(key.getIdentities().get(no));
							
						}
					}
					if (ids.size()>0) {
						String txt = "Are you sure you want to remove the following id(s)?";
						for (Identity id : ids) txt += "\n"+id.getEmail();
						int a = Dialogs.showYES_NO_Dialog("Confirm removal",txt);
						if (a==Dialogs.YES) {
							for (Identity id : ids)
								key.removeIdentity(id);
							edit.setModel(new KeyTableModel(key));
							fitAllColumnWidth(edit);
							edit.validate();
						}
					} else {
						Dialogs.showMessage("Sorry, no identities selected.");
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		pb.add(removeID);
		
		
		JPanel ps = new JPanel();
		ps.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton ok = new JButton("ok");
		ok.setPreferredSize(new Dimension(150,25));
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
		
		d.setVisible(true);
		
		return isOK[0];
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
		initUICurrentKeyStore();
	}
	
	public void generateKeyPair(boolean master) {
		if (currentKeyStore!=null) {
			try {
				AsymmetricKeyPair kp =  AsymmetricKeyPair.generateAsymmetricKeyPair();
				OSDXKeyObject k = OSDXKeyObject.fromKeyPair(kp);
				boolean ok = showKeyEditDialog(k, true);
				if (ok) {
					currentKeyStore.addKey(k);
					tKeysIDs.setModel(tKeysIDs.getModel()); //update
				}
				releaseUILock();
				initUICurrentKeyStore();
			} catch (Exception ex) {
				releaseUILock();
				Dialogs.showMessage("ERROR: could not generate new keypair.");
				ex.printStackTrace();
			}
		}
	}
	
	public boolean openKeystore() {
		closeCurrentStore();
		File f = Dialogs.chooseOpenFile("Select keystore filename", lastDir.getAbsolutePath(), "mykeystore.xml");
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
				f = Dialogs.chooseSaveFile("Select keystore filename", lastDir.getAbsolutePath(), "mykeystore.xml");
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
		File f = Dialogs.chooseSaveFile("Select keystore filename", lastDir.getAbsolutePath(), "mykeystore.xml");
		if (f!=null) {
			try {
				currentKeyStore = KeyApprovingStore.createNewKeyApprovingStore(f);
				initUICurrentKeyStore();
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
	
	class KeyTableModel extends DefaultTableModel {
	
		private String[] header = new String[] {"name","value"};
		private Vector<String> rows = new Vector<String>();
		
		private OSDXKeyObject key;
		private Vector<Identity> ids;
		private Vector<DataSourceStep> datapath;
		private int startIds = 0;
		private int startDataPath = 0;
		
		public KeyTableModel(OSDXKeyObject key) {
			this.key = key;
			ids = key.getIdentities();
			datapath = key.getDatapath();
			
			rows = new Vector<String>();
			rows.add("id");
			rows.add("level");
			rows.add("usage");
			rows.add("parentkeyid");
			rows.add("authoritativekeyserver");
			startIds = rows.size();
			for (int i=0;i<ids.size();i++) {
				rows.add("identity "+(i+1));
			}
			startDataPath = rows.size();
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
				return key.getKeyID();
			else if (rowIndex==1) {
				return key.getLevelName();
			}
			else if (rowIndex==2)
				return key.getUsageName();
			else if (rowIndex==3)
				return key.getParentKeyID();
			else if (rowIndex==4)
				return key.getAuthoritativekeyserver();
			else if (rowIndex>=startIds && rowIndex<startIds+ids.size())
				return ids.get(rowIndex-startIds).getEmail();
			else if (rowIndex>=startDataPath && rowIndex<startDataPath+datapath.size()) {
				DataSourceStep s = datapath.get(rowIndex-startDataPath);
				return s.getDataSource()+" at "+s.getDataInsertDatetimeString();
			}
			return null;
		}
	
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex==1) {
				if (rowIndex == 3) return true;
			}
			return false;
		}
	
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (columnIndex==1) {
				if (rowIndex == 3) {
					key.setParentKeyID((String)aValue);
				}
			}
		}
	}
	
	class KeysAndIdentitiesTableModel extends DefaultTableModel {
		
		private String[] header = new String[] {"key id","level","usage","identities", "parent key id"};
		private Vector<OSDXKeyObject> keys;
		
		public KeysAndIdentitiesTableModel(Vector<OSDXKeyObject> keys) {
			this.keys = keys;
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
			if (keys==null) return 0;
			return keys.size();
		}
	
		public Object getValueAt(int rowIndex, int columnIndex) {
			OSDXKeyObject k = keys.get(rowIndex);
			if (columnIndex==0)
				return k.getKeyID();
			else if (columnIndex==1)
				return k.getLevelName();
			else if (columnIndex==2)
				return k.getUsageName();
			else if (columnIndex==3) {
				String ids = null;
				for (Identity id : k.getIdentities()) { 
					if (ids==null) ids = id.getEmail();
					else ids += ", "+id.getEmail();
				}
				return ids;
			} else if (columnIndex==4) {
				String p = k.getParentKeyID();
				if (p==null || p.length()==0) return "[no parent]";
				return p;
			}
			
			return null;
		}
	
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}
	
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			//do nothing
		}
	}
	
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
				return id.getPostcode();
			else if (rowIndex==6)
				return id.getCompany();
			else if (rowIndex==7)
				return id.getUnit();
			else if (rowIndex==8)
				return id.getSubunit();
			else if (rowIndex==9)
				return id.getFunction();
			else if (rowIndex==10)
				return id.getSurname();
			else if (rowIndex==11)
				return id.getMiddlename();
			else if (rowIndex==12)
				return id.getName();
			else if (rowIndex==13)
				return id.getNote();
			else if (rowIndex>=14 && rowIndex<14+datapath.size()) {
				DataSourceStep s = datapath.get(rowIndex-14);
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
			if (rowIndex==0)
				id.setEmail(s);
			else if (rowIndex==1)
				id.setMnemonic(s);
			else if (rowIndex==2)
				id.setPhone(s);
			else if (rowIndex==3)
				id.setCountry(s);
			else if (rowIndex==4)
				id.setRegion(s);
			else if (rowIndex==5)
				id.setPostcode(s);
			else if (rowIndex==6)
				id.setCompany(s);
			else if (rowIndex==7)
				id.setUnit(s);
			else if (rowIndex==8)
				id.setSubunit(s);
			else if (rowIndex==9)
				id.setFunction(s);
			else if (rowIndex==10)
				id.setSurname(s);
			else if (rowIndex==11)
				id.setMiddlename(s);
			else if (rowIndex==12)
				id.setName(s);
			else if (rowIndex==13)
				id.setNote(s);
		}
	}
	
}

