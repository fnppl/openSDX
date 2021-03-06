package org.fnppl.opensdx.common;

import java.util.Vector;
/*
 * Copyright (C) 2010-2015 
 * 							fine people e.V. <opensdx@fnppl.org> 
 * 							Henning Thieß <ht@fnppl.org>
 * 
 * 							http://fnppl.org
*/

import org.fnppl.opensdx.xml.ChildElementIterator;
import org.fnppl.opensdx.xml.Element;

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

/**
 * 
 * @author Michael Reincke <mreincke@finetunes.net>
 * 
 */
public class BundleTexts extends BusinessObject {

	public static String KEY_NAME = "texts";

	private Vector<BusinessStringItem> promotext;						//SHOULD
	private Vector<BusinessStringItem> teasertext;						//SHOULD


	public static BundleTexts make() {
		BundleTexts texts = new BundleTexts();
		texts.promotext = new Vector<BusinessStringItem>();
		texts.teasertext = new Vector<BusinessStringItem>();
		return texts;
	}


	public static BundleTexts fromBusinessObject(BusinessObject bo) {
		if (bo==null) return null;
		if (!bo.getKeyname().equals(KEY_NAME)) {
			bo = bo.handleBusinessObject(KEY_NAME);
		}
		if (bo==null) return null;
		final BundleTexts texts = new BundleTexts();
		texts.initFromBusinessObject(bo);
		
		texts.promotext = new Vector<BusinessStringItem>();
		new ChildElementIterator(bo, "promotext") {
			public void processBusinessStringItem(BusinessStringItem item) {
				texts.promotext.add(item);
			}
		};
		texts.teasertext = new Vector<BusinessStringItem>();
		new ChildElementIterator(bo, "teasertext") {
			public void processBusinessStringItem(BusinessStringItem item) {
				texts.teasertext.add(item);
			}
		};
		return texts;
	}
	
	public BundleTexts setPromotext(String language, String promotext) {
		for (int i=0;i<this.promotext.size();i++) {
			if (this.promotext.get(i).getAttribute("lang").equals(language)) {
				this.promotext.get(i).setString(promotext);
				return this;
			}
		}
		BusinessStringItem text = new BusinessStringItem("promotext", promotext);
		text.setAttribute("lang", language);
		this.promotext.add(text);
		return this;
	}
	
	public void removePromotext(String lang) {
		if (promotext==null) return;
		for (int i=0;i<promotext.size();i++) {
			if (getPromotextLanguage(i)!=null && getPromotextLanguage(i).equals(lang)) {
				promotext.remove(i);
				i--;
			}
		}
	}
	
	public void removeTeasertext(String lang) {
		if (teasertext==null) return;
		for (int i=0;i<teasertext.size();i++) {
			if (getTeasertextLanguage(i)!=null && getTeasertextLanguage(i).equals(lang)) {
				teasertext.remove(i);
				i--;
			}
		}
	}

	public BundleTexts setTeasertext(String language, String teasertext) {
		for (int i=0;i<this.teasertext.size();i++) {
			if (this.teasertext.get(i).getAttribute("lang").equals(language)) {
				this.teasertext.get(i).setString(teasertext);
				return this;
			}
		}
		BusinessStringItem text = new BusinessStringItem("teasertext", teasertext);
		text.setAttribute("lang", language);
		this.teasertext.add(text);
		return this;
	}

	public String getPromotext(int index) {
		if (promotext==null || index<0 || index>=promotext.size()) return null;
		return promotext.get(index).getString();
	}
	
	public String getPromotext(String lang) {
		if (promotext==null) return null;
		for (int i=0;i<promotext.size();i++) {
			if (promotext.get(i).getAttribute("lang").equals(lang)) {
				return promotext.get(i).getString();
			}
		}
		return null;
	}
	
	public String getPromotextLanguage(int index) {
		if (promotext==null || index<0 || index>=promotext.size()) return null;
		return promotext.get(index).getAttribute("lang");
	}
	
	public void promotext_language(int index, String language) {
		if (promotext == null || index < 0 || index >= promotext.size()) return;
		promotext.get(index).setAttribute("lang",language);
	}
	public void promotext(int index, String text) {
		if (promotext == null || index < 0 || index >= promotext.size()) return;
		promotext.get(index).setString(text);
	}
	
	public int getPromotextCount() {
		if (promotext==null) return 0;
		return promotext.size();
	}

	public String getTeasertext(int index) {
		if (teasertext==null || index<0 || index>=teasertext.size()) return null;
		return teasertext.get(index).getString();
	}
	public String getTeasertextLanguage(int index) {
		if (teasertext==null || index<0 || index>=teasertext.size()) return null;
		return teasertext.get(index).getAttribute("lang");
	}
	
	public String getTeasertext(String lang) {
		if (teasertext==null) return null;
		for (int i=0;i<teasertext.size();i++) {
			if (teasertext.get(i).getAttribute("lang").equals(lang)) {
				return teasertext.get(i).getString();
			}
		}
		return null;
	}
	
	public void teasertext_language(int index, String language) {
		if (teasertext == null || index < 0 || index >= teasertext.size()) return;
		teasertext.get(index).setAttribute("lang",language);
	}
	public void teasertext(int index, String text) {
		if (teasertext == null || index < 0 || index >= teasertext.size()) return;
		teasertext.get(index).setString(text);
	}
	
	public int getTeasertextCount() {
		if (teasertext==null) return 0;
		return teasertext.size();
	}
		
	public String getKeyname() {
		return KEY_NAME;
	}
}
