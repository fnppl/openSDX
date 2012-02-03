package org.fnppl.opensdx.keyserverfe.client;

import java.util.Date;

import org.fnppl.opensdx.keyserverfe.shared.KeyInfo;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class KeyDetailsPopupPanel extends PopupPanel {

	private KeyInfo keyInfo = null;
	
	private FlexTable tab = null;
	private DateTimeFormat datetimeformat = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss zzz");
	
	public KeyDetailsPopupPanel() {
		 super(true);
		 VerticalPanel panel = new VerticalPanel();
		 panel.add(new Label("Key Details"));
		 tab = new FlexTable();
		 int row = 0;
		 tab.setHTML(row, 0, "keyid:");
		 tab.setHTML(row, 1, "");
		 row++;
		 tab.setHTML(row, 0, "owner:");
		 tab.setHTML(row, 1, "");
		 row++;
		 tab.setHTML(row, 0, "mnemonic:");
		 tab.setHTML(row, 1, "");
		 row++;
		 tab.setHTML(row, 0, "level:");
		 tab.setHTML(row, 1, "");
		 row++;
		 tab.setHTML(row, 0, "usage:");
		 tab.setHTML(row, 1, "");
		 row++;
		 tab.setHTML(row, 0, "valid from:");
		 tab.setHTML(row, 1, "");
		 row++;
		 tab.setHTML(row, 0, "valid until:");
		 tab.setHTML(row, 1, "");
		 row++;
		 tab.setHTML(row, 0, "status:");
		 tab.setHTML(row, 1, "");
		 row++;
		 tab.setHTML(row, 0, "trusted:");
		 tab.setHTML(row, 1, "");

		 panel.add(tab);
	     setWidget(panel);
	}
	
	public void update(KeyInfo keyInfo) {
		this.keyInfo = keyInfo;
		if (keyInfo == null) {
			for (int i=0;i<9;i++) {
				tab.setHTML(i, 1, "");
			}
		} else {
			int row = 0;
			tab.setHTML(row++, 1, keyInfo.getId());
			tab.setHTML(row++, 1, keyInfo.getOwner());
			tab.setHTML(row++, 1, keyInfo.getMnemonic());
			tab.setHTML(row++, 1, keyInfo.getLevel());
			tab.setHTML(row++, 1, keyInfo.getUsage());
			tab.setHTML(row++, 1, datetimeformat.format(new Date(keyInfo.getValidFrom())));
			tab.setHTML(row++, 1, datetimeformat.format(new Date(keyInfo.getValidUntil())));
			tab.setHTML(row++, 1, keyInfo.getStatusText());
			String trust = "no";
			if (keyInfo.isDirectTrust()) {
				trust = "direct";
			}
			else if (keyInfo.isIndirectTrust()) {
				trust = "indirect";
			}
			tab.setHTML(row++, 1, trust);
		}
	}
	
}
