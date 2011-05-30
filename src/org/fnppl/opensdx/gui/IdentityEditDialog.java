package org.fnppl.opensdx.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableColumn;

import org.fnppl.opensdx.gui.SecurityMainFrame.IdentityTableModel;
import org.fnppl.opensdx.security.Identity;

public class IdentityEditDialog extends JDialog {

	public static long MAX_PHOTO_SIZE = 50*1024L;
	protected boolean isOK;
	protected IdentityEditDialog dialog;

	private Identity id;
	private Frame parent;
	private JPanel mainPanel;
	private HashMap<String,JTextField> texts = new HashMap<String, JTextField>();
	private HashMap<String,JCheckBox> checks = new HashMap<String, JCheckBox>();
	private JButton cancel;
		
	private JButton photo; 				private JCheckBox photo_restricted;
	private File photoFile = null;
	private BufferedImage photoImage = null;
	
	private static int photoW = 90;
	private static int photoH = 120;
	private static BufferedImage noPhoto = new BufferedImage(photoW, photoH, BufferedImage.TYPE_INT_RGB);
	static {
		Graphics g = noPhoto.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0,0,photoW,photoH);
		g.setColor(Color.GRAY);
		g.setFont(new Font("arial", Font.BOLD, 12));
		g.drawString("no photo", photoW/2-25 ,photoH/2);
	}
	
	int absX = 10;
	int w1 = 180;
	int w2 = 320;
	int w3 = 100;
	int x1 = absX;
	int x2 = x1+w1+absX;
	int x3 = x2+w2+absX;
	int x4 = x3+w3+absX+absX+absX;
	
	int absY = 10;
	int h = 25;
	
	int y = 20;
	
	public IdentityEditDialog(Frame parent) {
		super(parent);
		this.parent = parent;
		dialog = this;
		initComponents();
	}
	
	private void initComponents() {
		setTitle("Edit Identity");
		mainPanel = new JPanel();
		mainPanel.setLayout(null);
		
		
		addComponent("identnum", false); texts.get("identnum").setEditable(false);
		y += absY;
		
		photo = new JButton();
		photo.setIcon(new ImageIcon(noPhoto));
		photo.setBounds(x4, y, photoW, photoH);
		mainPanel.add(photo);
		JCheckBox c = new JCheckBox("restricted");
		c.setBounds(x4, y+photoH+absY, w3, h);
		mainPanel.add(c);
		checks.put("photo",c);
		
		
		addComponent("email", false);
		addComponent("mnemonic", true);
		addComponent("company", true);
		addComponent("unit", true);
		addComponent("subunit", true);
		addComponent("function", true);
		y += absY;
		addComponent("surname", true);
		addComponent("firstname(s)", true);
		addComponent("middlename", true);
		addComponent("birthday (gmt)", true);
		addComponent("place of birth", true);
		y += absY;
		addComponent("city", true);
		addComponent("postcode", true);
		addComponent("region", true);
		addComponent("country", true);
		y += absY;
		addComponent("phone", true);
		addComponent("fax", true);
		y += absY;
		addComponent("note", true);
		y += absY+absY;
		JButton ok = new JButton("ok");
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (getText("email").equals("")) {
					Dialogs.showMessage("Please enter email adress");
					return;
				}
				if (getText("mnemonic").equals("")) {
					Dialogs.showMessage("Please enter mnemonic");
					return;
				}
				isOK = true;
				dialog.dispose();
			}
		});
		ok.setBounds(x1, y, w1, h);
		mainPanel.add(ok);

		cancel = new JButton("cancel");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isOK = false;
				dispose();
			}
		});
		cancel.setBounds(x2, y, w1, h);
		mainPanel.add(cancel);

		this.add(mainPanel, BorderLayout.CENTER);
		
		
		//Photobutton
		photo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (photoImage!=null) {
					final JPopupMenu popup = new JPopupMenu();
					JMenuItem change = popup.add("change photo");
					JMenuItem remove = popup.add("remove photo");
					change.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							choosePhoto();
							popup.setVisible(false);
						}
					});
					remove.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							photoFile = null;
							photoImage = null;
							updatePhoto();
							popup.setVisible(false);
						}
					});
					Point loc = photo.getLocationOnScreen();
					popup.setLocation(loc.x+10, loc.y+10);
					Thread tHide = new Thread() {
						public void run() {
							try {
								sleep(4000);
							} catch (Exception ex) {}
							popup.setVisible(false);
						}
					};
					tHide.start();
					popup.setVisible(true);
				} else {
					choosePhoto();
				}
			}
		});
	}
	
	private void choosePhoto() {
		File f = Dialogs.chooseOpenFile("Open Image File", null, "image.png");
		if (f!=null) {
			try {
				if (f.length()>MAX_PHOTO_SIZE) {
					Dialogs.showMessage("Sorry, max filesize of photo is 50kB");
					return;
				}
				photoImage = ImageIO.read(f);
				photoFile = f;
				updatePhoto();
			} catch (Exception ex) {
				ex.printStackTrace();
				Dialogs.showMessage("Error setting selected photo. Wrong format?");
			}
		}
	}
	
	private void updatePhoto() {
		if (photoImage==null) {
			photo.setIcon(new ImageIcon(noPhoto));
		} else {
			int scaleW = photoW;
			float fact = (float)photoW/(float)photoImage.getWidth();
			int scaleH = Math.round(fact*photoImage.getHeight());
			if (scaleH > photoH) {
				fact = (float)photoH/(float)photoImage.getHeight();
				scaleH = photoH;
				scaleW = Math.round(fact*photoImage.getWidth());
			}
			photo.setIcon(new ImageIcon(photoImage.getScaledInstance(scaleW, scaleH, BufferedImage.SCALE_SMOOTH)));
		}
	}
	
	private String getText(String name) {
		JTextField t = texts.get(name);
		if (t==null) return null;
		String text = t.getText();
		if (text.equals("")) return null;
		return text;
	}
	
	private boolean getRestricted(String name) {
		JCheckBox c = checks.get(name);
		if (c==null) return true;
		return c.isSelected();
	}
	
	private void setText(String name, String text) {
		JTextField t = texts.get(name);
		if (t==null) return;
		if (text==null) {
			t.setText("");
		} else {
			t.setText(text);
		}
	}
	
	private void setRestrict(String name, boolean restrict) {
		JCheckBox c = checks.get(name);
		if (c==null) return;
		c.setSelected(restrict);
	}
	
	private void addComponent(String name, boolean canRestrict) {
		JLabel l = new JLabel(name);
		l.setBounds(x1, y, w1, h);
		mainPanel.add(l);
		
		JTextField text = new JTextField();
		text.setBounds(x2, y, w2, h);
		mainPanel.add(text);
		texts.put(name, text);
		
		if (canRestrict) {
			JCheckBox check = new JCheckBox("restricted");
			check.setBounds(x3, y, w3, h);
			mainPanel.add(check);
			checks.put(name, check);	
		}
		y += absY+h;
	}
	
	private void setFromIdentity() {
		if (id ==null) {
			for (Entry<String, JTextField> e : texts.entrySet()) {
				e.getValue().setText("");
			}
			for (Entry<String, JCheckBox> e : checks.entrySet()) {
				e.getValue().setSelected(true);
			}
			return;
		}
		setText("identnum", id.getIdentNumString());
		setText("email", id.getEmail());
		setText("mnemonic", id.getMnemonic());
		setText("company", id.getCompany());
		setText("unit", id.getUnit());
		setText("subunit", id.getSubunit());
		setText("function", id.getFunction());
		
		setText("surname", id.getSurname());
		setText("firstname(s)", id.getFirstNames());
		setText("middlename", id.getMiddlename());
		setText("birthday (gmt)", id.getBirthdayGMTString());
		setText("place of birth", id.getPlaceOfBirth());
		
		setText("city", id.getCity());
		setText("postcode", id.getPostcode());
		setText("region", id.getRegion());
		setText("country", id.getCountry());
		setText("phone", id.getPhone());
		setText("fax", id.getFax());
		setText("note", id.getNote());
		
		photoImage = id.getPhoto();
		updatePhoto();
		
		setRestrict("mnemonic", id.isMnemonicRestricted());
		setRestrict("company", id.is_company_restricted());
		setRestrict("unit", id.is_unit_restricted());
		setRestrict("subunit", id.is_subunit_restricted());
		setRestrict("function", id.is_function_restricted());
		
		setRestrict("surname", id.is_surname_restricted());
		setRestrict("firstname(s)", id.is_firstname_s_restricted());
		setRestrict("middlename", id.is_middlename_restricted());
		setRestrict("birthday (gmt)", id.is_birthday_gmt_restricted());
		setRestrict("place of birth", id.is_placeofbirth_restricted());
		
		setRestrict("city", id.is_city_restricted());
		setRestrict("postcode", id.is_postcode_restricted());
		setRestrict("region", id.is_region_restricted());
		setRestrict("country", id.is_country_restricted());
		setRestrict("phone", id.is_phone_restricted());
		setRestrict("fax", id.is_fax_restricted());
		setRestrict("note", id.is_note_restricted());
		
		setRestrict("photo", id.is_photo_restricted());
	}
	
	private void setToIdentity() {
		if (id ==null) {
			for (Entry<String, JTextField> e : texts.entrySet()) {
				e.getValue().setText("");
			}
			for (Entry<String, JCheckBox> e : checks.entrySet()) {
				e.getValue().setSelected(true);
			}
			return;
		}
		id.setEmail(getText("email"));
		id.setMnemonic(getText("mnemonic"));
		
		id.setCompany(getText("company"));
		id.setUnit(getText("unit"));
		id.setSubunit(getText("subunit"));
		id.setFunction(getText("function"));
		
		id.setSurname(getText("surname"));
		id.setFirstNames(getText("firstname(s)"));
		id.setMiddlename(getText("middlename"));
	
		id.setBirthday_gmt(getText("birthday (gmt)"));
		id.setPlaceofbirth(getText("place of birth"));
		
		id.setCity(getText("city"));
		id.setPostcode(getText("postcode"));
		id.setRegion(getText("region"));
		id.setCountry(getText("country"));
		id.setPhone(getText("phone"));
		id.setFax(getText("fax"));
		id.setNote(getText("note"));
		
		if (photoFile!=null) {
			id.setPhoto(photoFile);
		} else {
			id.setPhoto(photoImage);
		}
		
		id.set_mnemonic_restricted(getRestricted("mnemonic"));
		
		id.set_company_restricted(getRestricted("company"));
		id.set_unit_restricted(getRestricted("unit"));
		id.set_subunit_restricted(getRestricted("subunit"));
		id.set_function_restricted(getRestricted("function"));
		
		id.set_surname_restricted(getRestricted("surname"));
		id.set_firstname_s_restricted(getRestricted("firstname(s)"));
		id.set_middlename_restricted(getRestricted("middlename"));
	
		id.set_birthday_gmt_restricted(getRestricted("birthday (gmt)"));
		id.set_placeofbirth_restricted(getRestricted("place of birth"));
		
		id.set_city_restricted(getRestricted("city"));
		id.set_postcode_restricted(getRestricted("postcode"));
		id.set_region_restricted(getRestricted("region"));
		id.set_country_restricted(getRestricted("country"));
		id.set_phone_restricted(getRestricted("phone"));
		id.set_fax_restricted(getRestricted("fax"));
		id.set_note_restricted(getRestricted("note"));
		id.set_photo_restricted(getRestricted("photo"));
		
	}
	
	public boolean show(Identity id, boolean canCancel) {
		this.id = id;
		setFromIdentity();
		if (canCancel) {
			cancel.setVisible(true);
		} else {
			cancel.setVisible(false);
		}
		setSize(800, y+100);
		setModal(true);
		Helper.centerMe(this, null);
		setVisible(true);
		if (isOK) {
			setToIdentity();
		}
		return isOK;
	}
	
}
