package org.fnppl.opensdx.file_transfer.trigger;
/*
 * Copyright (C) 2010-2012 
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
import java.util.HashMap;
import java.util.Vector;

import org.fnppl.opensdx.xml.Element;

public class Trigger {

	public static String TRIGGER_LOGIN = "login";
	public static String TRIGGER_LOGOUT = "logout";
	public static String TRIGGER_MKDIR = "mkdir";
	public static String TRIGGER_DELETE = "delete";
	public static String TRIGGER_RENAME = "rename";
	public static String TRIGGER_UPLOAD_START = "upload_start";
	public static String TRIGGER_DOWNLOAD_START = "download_start";
	public static String TRIGGER_UPLOAD_END = "upload_end";
	public static String TRIGGER_DOWNLOAD_END = "download_end";

	public static Vector<String> TRIGGER_LIST = new Vector<String>();
	static {
		TRIGGER_LIST.add(TRIGGER_LOGIN);
		TRIGGER_LIST.add(TRIGGER_LOGOUT);
		TRIGGER_LIST.add(TRIGGER_MKDIR);
		TRIGGER_LIST.add(TRIGGER_DELETE);
		TRIGGER_LIST.add(TRIGGER_RENAME);
		TRIGGER_LIST.add(TRIGGER_UPLOAD_START);
		TRIGGER_LIST.add(TRIGGER_UPLOAD_END);
		TRIGGER_LIST.add(TRIGGER_DOWNLOAD_START);
		TRIGGER_LIST.add(TRIGGER_DOWNLOAD_END);		
	};

	private boolean async = false;
	private String event = null;
	private boolean replaceDefault = false;
	private Vector<FunctionCall> calls = new Vector<FunctionCall>();

	public Trigger() {

	}

	public static Trigger fromElement(Element eTrigger) {
		if (!eTrigger.getName().equals("trigger")) {
			throw new RuntimeException("Trigger: Error in Trigger config xml");
		}
		Trigger trigger = new Trigger();
		trigger.event = eTrigger.getChildTextNN("event");
		Element eEvent = eTrigger.getChild("event");
		if (eEvent!=null) {
			String rep = eEvent.getAttribute("replace_default");
			if (rep!=null && Boolean.parseBoolean(rep)) {
				trigger.replaceDefault = true;
			}
		}
		if (eTrigger.getChildTextNN("async").equalsIgnoreCase("true")) {
			trigger.async = true;
		}
		Vector<Element> eList = eTrigger.getChildren();
		for (Element e : eList) {
			if (e.getName().equals("api_call")) {
				trigger.calls.add(APICall.fromElemet(e));
			}
			else if (e.getName().equals("system_exec_call")) {
				trigger.calls.add(SystemExecCall.fromElemet(e));
			}
		}


		return trigger;
	}


	public String getEventType() {
		return event;
	}

	public void doAction(HashMap<String, Object> context) {
		for (FunctionCall c : calls) {
			c.run(async, context);
		}
	}


	public void setAsynchron(boolean asynchron) {
		this.async = asynchron;
	}

	public boolean isAsynchron() {
		return async;
	}

	public boolean isReplaceDefault() {
		return replaceDefault;
	}

	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("Trigger\n-------\nEvent: "+event+"\nasync: "+async);
		for (FunctionCall c : calls) {
			b.append("\n"+c.toString());
		}

		return b.toString();

	}


	//TEST, TEST, TEST
	public void writeln(String text) {
		System.out.println(text);
	}

}

