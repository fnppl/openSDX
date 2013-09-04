package org.fnppl.opensdx.common;

import java.util.Vector;

import org.fnppl.opensdx.xml.Document;
import org.fnppl.opensdx.xml.Element;
import org.fnppl.opensdx.xml.XMLElementable;
import org.fnppl.opensdx.xml.XMLHelper;

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
 * @author Bertram Boedeker <bboedeker@gmx.de>
 * 
 */

public class LicenseRule extends BusinessObject {

	public static String KEY_NAME = "rule";
	
	public static String OPERATOR_EQUALS = "equals";
	public static String OPERATOR_BEFORE = "before";
	public static String OPERATOR_AFTER = "after";
	public static String OPERATOR_CONTAINS = "contains";
	public static String OPERATOR_CONTAINED_IN = "containedin";
	
	private int num;
	private String if_what;
	private String if_operator;
	private String if_value;
	private Vector<Element> thens;
	private Vector<Element> elses;
	
	private LicenseRule() {
		num = -1;
		if_what = null;
		if_operator = null;
		if_value = null;
		thens = new Vector<Element>();
		elses = new Vector<Element>();
	}
	
	public static LicenseRule make(int num, String if_what, String if_operator, String if_value) {
		LicenseRule b = new LicenseRule();
		b.num = num;
		b.if_what = if_what;
		b.if_operator = if_operator;
		b.if_value = if_value;
		b.thens = new Vector<Element>();
		b.elses = new Vector<Element>();
		return b;
	}
	
	public static LicenseRule fromBusinessObject(BusinessObject bo) {
		if (bo==null) return null;
		if (!bo.getKeyname().equals(KEY_NAME)) {
			bo = bo.handleBusinessObject(KEY_NAME);
		}
		if (bo==null) return null;
		
		final LicenseRule b = new LicenseRule();
		b.initFromBusinessObject(bo);
		try {
			String snum = b.getAttribute("num");
			if (snum==null) {
				b.num = -1;
			} else {
				b.num = Integer.parseInt(snum);
			}
			
			XMLElementable xIf = b.handleObject("if");
			if (xIf!=null) {
				if (xIf instanceof BusinessObject) {
					((BusinessObject)xIf).setAppendOtherObjectToOutput(true).showOtherObjectsMessage=false;
				}
				Element eIf = xIf.toElement();
				b.if_what = eIf.getChildText("what");
				b.if_operator = eIf.getChildText("operator");
				b.if_value = eIf.getChildText("value");
			}
			b.thens = new Vector<Element>();
			XMLElementable xThen = b.handleObject("then");
			if (xThen!=null) {
				if (xThen instanceof BusinessObject) {
					((BusinessObject)xThen).setAppendOtherObjectToOutput(true).showOtherObjectsMessage=false;
				}
				Element eThen = xThen.toElement();
				//Document.buildDocument(eThen).output(System.out);
				for (Element c : eThen.getChildren()) {
					b.thens.add(XMLHelper.cloneElement(c));
				}
			}
			
			b.elses = new Vector<Element>();
			XMLElementable xElse = b.handleObject("else");
			if (xElse!=null) {
				if (xElse instanceof BusinessObject) {
					((BusinessObject)xElse).setAppendOtherObjectToOutput(true).showOtherObjectsMessage=false;
				}
				Element eElse = xElse.toElement();	
				for (Element c : eElse.getChildren()) {
					b.elses.add(XMLHelper.cloneElement(c));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return b;
	}
	
	public LicenseRule addThenProclaim(String what, String s_for) {
		Element e = new Element("proclaim");
		e.addContent("what", what);
		e.addContent("for",s_for);
		thens.add(e);
		return this;
	}
	
	public LicenseRule addThenProclaim(String what, Element e_for) {
		Element e = new Element("proclaim");
		e.addContent("what", what);
		Element eFor = new Element("for");
		eFor.addContent(e_for);
		e.addContent(eFor);
		thens.add(e);
		return this;
	}
	
	public LicenseRule addThenEcho(String s) {
		thens.add(new Element("echo",s));
		return this;
	}
	
	public LicenseRule addThenBreak() {
		thens.add(new Element("break",""));
		return this;
	}
	
	public LicenseRule addElseProclaim(String what, String s_for) {
		Element e = new Element("proclaim");
		e.addContent("what", what);
		e.addContent("for",s_for);
		elses.add(e);
		return this;
	}
	
	public LicenseRule addElseProclaim(String what, Element e_for) {
		Element e = new Element("proclaim");
		e.addContent("what", what);
		Element eFor = new Element("for");
		eFor.addContent(e_for);
		e.addContent(eFor);
		elses.add(e);
		return this;
	}
	
	public LicenseRule addElseEcho(String s) {
		elses.add(new Element("echo",s));
		return this;
	}
	
	public LicenseRule addElseBreak() {
		elses.add(new Element("break",""));
		return this;
	}
	
	public Element toElement() {
		Element e = new Element("rule");
		e.setAttribute("num", ""+num);
		//IF
		Element eIf = new Element("if");
		eIf.addContent("what", if_what);
		eIf.addContent("operator", if_operator);
		eIf.addContent("value", if_value);
		e.addContent(eIf);
		
		//THEN
		if (thens!=null && thens.size()>0) {
			Element eThen = new Element("then");
			for (Element c : thens) {
				c.detach();
				eThen.addContent(c);
			}		
			e.addContent(eThen);			
		}
		
		//ELSE
		if (elses!=null && elses.size()>0) {
			Element eElse = new Element("else");
			for (Element c : elses) {
				c.detach();
				eElse.addContent(c);
			}
			e.addContent(eElse);
		}
		
		return e;
	}
	
	
	public int getNum() {
		return num;
	}

	public String getIf_what() {
		return if_what;
	}

	public String getIf_operator() {
		return if_operator;
	}

	public String getIf_value() {
		return if_value;
	}

	public Vector<Element> getThens() {
		return thens;
	}

	public Vector<Element> getElses() {
		return elses;
	}

	public String getKeyname() {
		return KEY_NAME;
	}
	
	
	
}
