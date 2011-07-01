package org.fnppl.opensdx.gui.helper;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Color;
import javax.swing.*;
import javax.swing.text.JTextComponent;

import org.fnppl.opensdx.security.Identity;
import org.fnppl.opensdx.security.KeyLog;

import java.util.HashMap;
import java.util.Vector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class PanelIdentityDetails extends JPanel {

	//init fields
	private JLabel label_identnum;
	private JTextField text_identnum;
	private JLabel label_email;
	private JTextField text_email;
	private JButton bu_photo;
	private JLabel label_mnemonic;
	private JTextField text_mnemonic;
	private JLabel label_company;
	private JTextField text_company;
	private JLabel label_unit;
	private JTextField text_unit;
	private JLabel label_subunit;
	private JTextField text_subunit;
	private JLabel label_function;
	private JTextField text_function;
	private JLabel label_surname;
	private JTextField text_surname;
	private JLabel label_firstnames;
	private JTextField text_firstnames;
	private JLabel label_middlename;
	private JTextField text_middlename;
	private JLabel label_birthyday;
	private JTextField text_birthday_date;
	private JLabel label_place_of_birth;
	private JTextField text_place_of_birth;
	private JLabel label_city;
	private JTextField text_city;
	private JLabel label_postcode;
	private JTextField text_postcode;
	private JLabel label_region;
	private JTextField text_region;
	private JLabel label_country;
	private JTextField text_country;
	private JLabel label_phone;
	private JTextField text_phone;
	private JLabel label_fax;
	private JTextField text_fax;
	private JLabel label_note;
	private JTextField text_note;


	public PanelIdentityDetails() {
		initComponents();
		initLayout();
	}
	
	public void updateDetails(KeyLog keylog) {
		Identity id = null;
		if (keylog != null) {
			id = keylog.getIdentity();
		}
		if (id == null) {
			this.setVisible(false);
			text_identnum.setText("");
			text_email.setText("");
			text_mnemonic.setText("");
			text_company.setText("");
			text_unit.setText("");
			text_subunit.setText("");
			text_function.setText("");
			text_surname.setText("");
			text_firstnames.setText("");
			text_middlename.setText("");
			text_birthday_date.setText("");
			text_place_of_birth.setText("");
			text_city.setText("");
			text_postcode.setText("");
			text_region.setText("");
			text_country.setText("");
			text_phone.setText("");
			text_fax.setText("");
			text_note.setText("");
			bu_photo.setIcon(new ImageIcon(noPhoto));
		} else {
			this.setVisible(true);
			text_identnum.setText(id.getIdentNumString());
			text_email.setText(id.getEmail());
			text_mnemonic.setText(id.getMnemonic());
			text_company.setText(id.getCompany());
			text_unit.setText(id.getUnit());
			text_subunit.setText(id.getSubunit());
			text_function.setText(id.getFunction());
			text_surname.setText(id.getSurname());
			text_firstnames.setText(id.getFirstNames());
			text_middlename.setText(id.getMiddlename());
			text_birthday_date.setText(id.getBirthdayGMTString());
			text_place_of_birth.setText(id.getPlaceOfBirth());
			text_city.setText(id.getCity());
			text_postcode.setText(id.getPostcode());
			text_region.setText(id.getRegion());
			text_country.setText(id.getCountry());
			text_phone.setText(id.getPhone());
			text_fax.setText(id.getFax());
			text_note.setText(id.getNote());
			BufferedImage photo = id.getPhoto();
			if (photo==null) {
				photo = noPhoto;
			}
			bu_photo.setIcon(new ImageIcon(photo));
		}
	}
	
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

	private void initComponents() {
		Vector<JTextComponent> texts = new Vector<JTextComponent>();

		label_identnum = new JLabel("identnum");

		text_identnum = new JTextField("");

		text_identnum.setName("text_identnum");
		texts.add(text_identnum);

		label_email = new JLabel("email");

		text_email = new JTextField("");

		text_email.setName("text_email");
		texts.add(text_email);

		bu_photo = new JButton();
		bu_photo.setPreferredSize(new Dimension(photoW,photoH));
		bu_photo.setMinimumSize(new Dimension(photoW,photoH));
		bu_photo.setMaximumSize(new Dimension(photoW,photoH));
		bu_photo.setVerticalAlignment(SwingConstants.CENTER);
		bu_photo.setHorizontalAlignment(SwingConstants.CENTER);
		
		label_mnemonic = new JLabel("mnemonic");
		text_mnemonic = new JTextField("");
		text_mnemonic.setName("text_mnemonic");
		texts.add(text_mnemonic);
		label_company = new JLabel("company");
		text_company = new JTextField("");
		text_company.setName("text_companyl");
		texts.add(text_company);

		label_unit = new JLabel("unit");

		text_unit = new JTextField("");

		text_unit.setName("text_unit");
		texts.add(text_unit);

		label_subunit = new JLabel("subunit");

		text_subunit = new JTextField("");

		text_subunit.setName("text_subunit");
		texts.add(text_subunit);

		label_function = new JLabel("function");

		text_function = new JTextField("");

		text_function.setName("text_function");
		texts.add(text_function);

		label_surname = new JLabel("surname");

		text_surname = new JTextField("");

		text_surname.setName("text_surname");
		texts.add(text_surname);

		label_firstnames = new JLabel("firstname(s)");

		text_firstnames = new JTextField("");

		text_firstnames.setName("text_firstnamest");
		texts.add(text_firstnames);

		label_middlename = new JLabel("middlename");

		text_middlename = new JTextField("");

		text_middlename.setName("text_middlename");
		texts.add(text_middlename);

		label_birthyday = new JLabel("birthday (GMT)");

		text_birthday_date = new JTextField("");

		text_birthday_date.setName("text_birthday_date");
		texts.add(text_birthday_date);

		label_place_of_birth = new JLabel("place of birth");

		text_place_of_birth = new JTextField("");

		text_place_of_birth.setName("text_label_place_of_birth");
		texts.add(text_place_of_birth);

		label_city = new JLabel("city");

		text_city = new JTextField("");

		text_city.setName("text_city");
		texts.add(text_city);

		label_postcode = new JLabel("postcode");

		text_postcode = new JTextField("");

		text_postcode.setName("text_postcode");
		texts.add(text_postcode);

		label_region = new JLabel("region");

		text_region = new JTextField("");

		text_region.setName("text_region");
		texts.add(text_region);

		label_country = new JLabel("country");

		text_country = new JTextField("");

		text_country.setName("text_country");
		texts.add(text_country);

		label_phone = new JLabel("phone");

		text_phone = new JTextField("");

		text_phone.setName("text_phone");
		texts.add(text_phone);

		label_fax = new JLabel("fax");

		text_fax = new JTextField("");

		text_fax.setName("text_fax");
		texts.add(text_fax);

		label_note = new JLabel("note");

		text_note = new JTextField("");

		text_note.setName("text_note");
		texts.add(text_note);

		for (JTextComponent text : texts) {
			if (text instanceof JTextField) text.setEditable(false);
		}
	}


	public void initLayout() {
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		GridBagConstraints gbc = new GridBagConstraints();



		// Component: label_identnum
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
		gbl.setConstraints(label_identnum,gbc);
		add(label_identnum);

		// Component: text_identnum
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 50.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(text_identnum,gbc);
		add(text_identnum);

		// Component: label_email
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_email,gbc);
		add(label_email);

		// Component: text_email
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 50.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(text_email,gbc);
		add(text_email);

		// Component: bu_photo
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 5;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbl.setConstraints(bu_photo,gbc);
		add(bu_photo);

		// Component: label_mnemonic
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_mnemonic,gbc);
		add(label_mnemonic);

		// Component: text_mnemonic
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 50.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(text_mnemonic,gbc);
		add(text_mnemonic);

		// Component: label_company
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_company,gbc);
		add(label_company);

		// Component: text_companyl
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 50.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(text_company,gbc);
		add(text_company);

		// Component: label_unit
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_unit,gbc);
		add(label_unit);

		// Component: text_unit
		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 50.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(text_unit,gbc);
		add(text_unit);

		// Component: label_subunit
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_subunit,gbc);
		add(label_subunit);

		// Component: text_subunit
		gbc.gridx = 1;
		gbc.gridy = 5;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 50.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(text_subunit,gbc);
		add(text_subunit);

		// Component: label_function
		gbc.gridx = 0;
		gbc.gridy = 6;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_function,gbc);
		add(label_function);

		// Component: text_function
		gbc.gridx = 1;
		gbc.gridy = 6;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 50.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(text_function,gbc);
		add(text_function);

		// Component: label_surname
		gbc.gridx = 0;
		gbc.gridy = 7;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_surname,gbc);
		add(label_surname);

		// Component: text_surname
		gbc.gridx = 1;
		gbc.gridy = 7;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 50.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(text_surname,gbc);
		add(text_surname);

		// Component: label_firstnames
		gbc.gridx = 0;
		gbc.gridy = 8;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_firstnames,gbc);
		add(label_firstnames);

		// Component: text_firstnamest
		gbc.gridx = 1;
		gbc.gridy = 8;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 50.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(text_firstnames,gbc);
		add(text_firstnames);

		// Component: label_middlename
		gbc.gridx = 0;
		gbc.gridy = 9;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_middlename,gbc);
		add(label_middlename);

		// Component: text_middlename
		gbc.gridx = 1;
		gbc.gridy = 9;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 50.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(text_middlename,gbc);
		add(text_middlename);

		// Component: label_birthyday
		gbc.gridx = 0;
		gbc.gridy = 10;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_birthyday,gbc);
		add(label_birthyday);

		// Component: text_birthday_date
		gbc.gridx = 1;
		gbc.gridy = 10;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 50.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(text_birthday_date,gbc);
		add(text_birthday_date);

		// Component: label_place_of_birth
		gbc.gridx = 0;
		gbc.gridy = 11;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_place_of_birth,gbc);
		add(label_place_of_birth);

		// Component: text_label_place_of_birth
		gbc.gridx = 1;
		gbc.gridy = 11;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 50.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(text_place_of_birth,gbc);
		add(text_place_of_birth);

		// Component: label_city
		gbc.gridx = 0;
		gbc.gridy = 12;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_city,gbc);
		add(label_city);

		// Component: text_city
		gbc.gridx = 1;
		gbc.gridy = 12;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 50.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(text_city,gbc);
		add(text_city);

		// Component: label_postcode
		gbc.gridx = 0;
		gbc.gridy = 13;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_postcode,gbc);
		add(label_postcode);

		// Component: text_postcode
		gbc.gridx = 1;
		gbc.gridy = 13;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 50.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(text_postcode,gbc);
		add(text_postcode);

		// Component: label_region
		gbc.gridx = 0;
		gbc.gridy = 14;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_region,gbc);
		add(label_region);

		// Component: text_region
		gbc.gridx = 1;
		gbc.gridy = 14;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 50.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(text_region,gbc);
		add(text_region);

		// Component: label_country
		gbc.gridx = 0;
		gbc.gridy = 15;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_country,gbc);
		add(label_country);

		// Component: text_country
		gbc.gridx = 1;
		gbc.gridy = 15;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 50.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(text_country,gbc);
		add(text_country);

		// Component: label_phone
		gbc.gridx = 0;
		gbc.gridy = 16;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_phone,gbc);
		add(label_phone);

		// Component: text_phone
		gbc.gridx = 1;
		gbc.gridy = 16;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 50.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(text_phone,gbc);
		add(text_phone);

		// Component: label_fax
		gbc.gridx = 0;
		gbc.gridy = 17;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_fax,gbc);
		add(label_fax);

		// Component: text_fax
		gbc.gridx = 1;
		gbc.gridy = 17;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 50.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(text_fax,gbc);
		add(text_fax);

		// Component: label_note
		gbc.gridx = 0;
		gbc.gridy = 18;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(label_note,gbc);
		add(label_note);

		// Component: text_note
		gbc.gridx = 1;
		gbc.gridy = 18;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 50.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbl.setConstraints(text_note,gbc);
		add(text_note);
	}
}
