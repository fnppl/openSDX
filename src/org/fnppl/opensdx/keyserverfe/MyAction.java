/*
 * Copyright (C) 2010-2013 
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

package org.fnppl.opensdx.keyserverfe;

import org.jdom2.*;
import org.jdom2.input.*;
import org.jdom2.output.*;

import java.io.*;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.*;

import org.fnppl.dbaccess.*;

import org.apache.velocity.*;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;



@SuppressWarnings("unchecked")
public abstract class MyAction {
	public final static boolean always_session_wanted = false;
	public final static long default_mandantid = 101;
	
	
	public String mode = "UNDEFINED";  
	public Vector<String>[] parameter = new Vector[2];

	protected OutputStream outStream = null; 

	public MultiTypeRequest request;
	public HttpServletResponse response;

	public String encoding = "UTF-8";

	public boolean needssessionid = false;

	public static Element config;

	public ActiveUser user;
	public String templateprefix = "shop_";
	
	/**
	 * Setzt die MaxAge Eigenschaft des Cookies auf null, dieser wird somit vom browser gelöscht
	 * @param sessionid ID der Session
	 */
	public void deleteCookie(String sessionid){
		//Hier den Cookie killen
		Cookie kill = new Cookie("MERCHSTORE_ADMIN_sessionid", sessionid);
		kill.setMaxAge(0);
		kill.setPath("/");
		response.addCookie(kill);
	}

	private static final void readConfig() throws Exception {
		SAXBuilder sax = new SAXBuilder();        
		Class<MyAction> c = MyAction.class;        
		config = sax.build(new InputStreamReader(c.getResourceAsStream("resources/config.xml"))).getRootElement();
	}

	static {
		try {
			readConfig();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	public final static String moneyF(String s) {
		return moneyF(Double.parseDouble(s));
	}
	public final static String moneyF(double f) {
		DecimalFormat df = (DecimalFormat)DecimalFormat.getInstance(Locale.GERMANY);
		df.applyPattern("#0.00");
		
		return df.format(f);
	}
	
	public final static String simplify(String s, int maxlength) {
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<s.length();i++) {
			char c = s.charAt(i);
			switch(c) {
				case 'ä':
					sb.append("ae");
					break;
				case 'Ä':
					sb.append("ae");
					break;
				case 'ö':
					sb.append("oe");
					break;
				case 'Ö':
					sb.append("Oe");
					break;
				case 'ü':
					sb.append("ue");
					break;
				case 'Ü':
					sb.append("Ue");
					break;
				case 'ß':
					sb.append("ss");
					break;
					
				default:
					sb.append(c);
			}			
		}
		
		if(sb.length() > maxlength) {
			return sb.substring(0, maxlength);
		}
		
		return sb.toString();
	}

	public final static String replace(String inwhat, String what, String withme) {
		StringBuffer ret = new StringBuffer();
		StringBuffer runbuff = new StringBuffer();
		try {
			for(int i=0;i<inwhat.length();i++) {
				char c = inwhat.charAt(i);

				runbuff.append(c);
				if(runbuff.toString().equals(what)) {
					ret.append(withme);
					runbuff.setLength(0);
				}
				else {
					if(runbuff.length()==what.length() && runbuff.length()>0) {
						ret.append(runbuff.charAt(0));
						runbuff.deleteCharAt(0);
					}
				}
			}

			ret.append(runbuff.toString());
		} catch(Exception ex) {
			//            ex.printStackTrace();
		}

		return ret.toString();
	}
	
	public final static String elementToString(Element e) {
		return Helper.elementToString(e);
	}
	
	public final static String htmlEncode(String s) {
		StringBuilder b = new StringBuilder(s.length());
	     for (int i = 0; i < s.length(); i++)
	     {
	       char ch = s.charAt(i);
	       if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9')
	       {
	         // safe
	         b.append(ch);
	       }
	       else if (Character.isWhitespace(ch))
	       {
	         // paranoid version: whitespaces are unsafe - escape
	         // conversion of (int)ch is naive
	         b.append("&#").append((int) ch).append(";");
	       }
	       else if (Character.isISOControl(ch))
	       {
	         // paranoid version:isISOControl which are not isWhitespace removed !
	         // do nothing do not include in output !
	       }
	       else
	       {
	         // paranoid version
	         // the rest is unsafe, including <127 control chars
	         b.append("&#" + (int) ch + ";");
	       }
	     }
	     return b.toString();
	}

	public final static String urlEncode(String s) {
		if(s==null) return "";

		try {
			return java.net.URLEncoder.encode(s,"UTF-8");
		} catch(Exception ex) {
			ex.printStackTrace();
		}

		return "";
	}
	public final static String urlDecode(String s) {
		if(s==null) return "";

		try {
			return java.net.URLDecoder.decode(s,"UTF-8");
		} catch(Exception ex) {
			ex.printStackTrace();
		}

		return "";
	}

	public MyAction(MultiTypeRequest request, HttpServletResponse response) {
		this(request, response, false);
	}
	public MyAction(MultiTypeRequest request, HttpServletResponse response, boolean createHeader) {
		parameter[0] = new Vector<String>();
		parameter[1] = new Vector<String>();

		this.request = request;
		this.response = response;

		//        if(createHeader) {
			//            if(request.getParameter("xmlrequest")!=null && request.getParameter("xmlrequest").equals("true")) {
		//                encoding = "UTF-8";
		//                prepareOut(response, "text/xml;charset="+encoding);
		//            }
		//            else {
		////                response.addHeader("X-HEADADD", "2");
		//                prepareOut(response, "text/html;charset="+encoding);
		//            }
		//        }


		if(createHeader) {
			prepareOut(response, "text/html;charset="+encoding);
		}
	}

	public void conjoinParams(Vector<String>[] args) {
		Vector<String> s1 = args[0];
		Vector<String> s2 = args[1];

		//PATHINFO OVERRIDES get/post-args!!!
		for(int i=0; i<s1.size(); i++) {    		
			try {
				String v1=null;
				String n1 = s1.elementAt(i);//name
				if (s1.size() == s2.size() )
					v1 = s2.elementAt(i);//wahhluuhs 
				else
					break;
				int nn = parameter[0].indexOf(n1);
				if(nn >= 0) {
					parameter[1].set(nn, v1);
				}
				else {
					parameter[0].addElement(n1);
					parameter[1].addElement(v1);
				}
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	public final static Vector[] sortVectors(Vector[] toSort, int whichOneIsSorting) {
		Vector sortVector = (Vector)toSort[whichOneIsSorting].clone();

		Vector[] ret = new Vector[toSort.length];        

		Vector snums = new Vector();//kommen die indexe als string rein...

		while(snums.size() != sortVector.size()) {
			String least = null;
			int w = -1;
			for(int x=0;x<sortVector.size();x++) {
				if(!snums.contains(""+x)) {

					least = (String)sortVector.elementAt(x);
					w = x;

					//System.out.println("found free at "+w);
					break;
				}
			}

			if(w==-1) {
				break;
			}

			for(int i=0;i<sortVector.size();i++) {
				String me = (String)sortVector.elementAt(i);
				if(!snums.contains(""+i)) {
					if(me.compareTo(least) <= 0) {
						//System.out.println("true");
						w = i;
						least = me;
					}
					else {
						//System.out.println("false");
					}
				}
				else {
					//System.out.println("snums contains "+w);
				}
			}

			//System.out.println("added "+w+" to snums");
			snums.addElement(""+w);
			//sortVector.removeElementAt(w);
		}

		//System.out.println("sortVector.size(): "+sortVector.size());
		//System.out.println("snums.size(): "+snums.size());

		for(int i=0;i<ret.length;i++) {
			ret[i] = new Vector();
		}

		for(int i=0;i<toSort[0].size();i++) {
			int grabAt = Integer.parseInt((String)snums.elementAt(i));
			for(int x=0;x<ret.length;x++) {
				ret[x].addElement(toSort[x].elementAt(grabAt));
			}
		}

		return ret;
	}

	public final static String getStringXML(Element e) {
		return getStringXML(e, "UTF-8");

	}
	public final static String getStringXML(Element e, String encoding) {
		org.jdom2.output.Format f = org.jdom2.output.Format.getCompactFormat();
		f.setEncoding(encoding);
		XMLOutputter xout = new XMLOutputter(f);

		return xout.outputString(e);

	}
	public final static String getCuteXML(Element e) {
		org.jdom2.output.Format f = org.jdom2.output.Format.getPrettyFormat();
		f.setEncoding("UTF-8");
		XMLOutputter xout = new XMLOutputter(f);

		return xout.outputString(e);

	}


	public abstract void performAction(String reqAddress, VelocityContext c) throws Exception;

	public void performAllAction(Vector[] args) throws Exception {
		//String clienti p = request.getRemoteAddr();
		try {
			Enumeration<String> e = request.getParameterNames();            
			while(e.hasMoreElements()) {
				String schnauze = e.nextElement();
				if(request.getParameter(schnauze).length()!=0) {
					parameter[0].addElement(schnauze);
				}
			}

			for(int i=0; i<parameter[0].size(); i++) {
				parameter[1].addElement(request.getParameter((String)parameter[0].elementAt(i)));
			}

		} catch(Exception e) {
			System.out.println("Fehler: "+e.getMessage()); 
			//this.errortext=e.getMessage();
			e.printStackTrace();
		}
		
		conjoinParams(args);

		user = new ActiveUser();
		
		/*
         * Hier Session Checken (Um es nicht jedes mal in den Actions machen zu müssen (wie im PT)
         * Und zwar gehen wir nach folgenden Regeln vor: 
         * 
         * -> Cookie vorhanden?
         * 		|
         * 		|`-> JA!
         * 		|	|
         * 		|	`-> Sessionid noch gültig?
         * 		|		|
         * 		|		|`->JA!
         * 		|		|	`-> Anhand der Parameter einfach weiterleiten!
         * 		|		|
         * 		|		`->NEIN!
         * 		|			|
         * 		|			`-> Checken ob MID vorhanden ist (Als Parameter und im Cookie).
         * 		|				|
         * 		|				|`-> JA!
         * 		|				|	|	
         * 		|				|	`-> Login unter dieser MID vorbereiten (Formular anpassen)
         * 		|				|
         * 		|				`-> NEIN!
         * 		|					|
         * 		|					`-> Login so vorbereiten das man die MID auswählen muss.
         * 		| 
         * 		`->NEIN!
         * 			|
         * 			`-> Checken ob MID im parameter vorhanden ist.
         * 				|
         * 				|`-> JA!
         * 				|	|
         * 				|	`-> Login unter dieser MID vorbereiten.
         * 				|
         * 				`-> NEIN!
         * 					|
         * 					`-> Login so vorbereiten das man die MID auswählen muss.
         */
        
        //Cookie lesen:
        Cookie[] cooks = request.getCookies();
        String sessionid = null;
        boolean cookieFound = false;
        System.out.println("---------------\nSearching 4 cookie: MERCHSTORE_ADMIN_sessionid");
        for(int j=0; cooks!=null && j<cooks.length && !cookieFound; j++) {
        	Cookie coo = cooks[j];
        	if(coo.getName().equals("MERCHSTORE_ADMIN_sessionid")) {
        		System.out.print("Cookie Found!\nDoes an active session exist --->");
        		cookieFound = true;
        		sessionid = coo.getValue();
        		
        		
        		//Hier Prüfen obs eine Aktive session in der DB gibt
        		boolean exists = BalancingConnectionManager.execQuery(
        				"select exists(select * from activeusers where sessionid = '"+Helper.dbEncode(sessionid)+"')"
        			).getValueAt(0, 0).indexOf("t") == 0;  
        		
        		if(exists){
        			System.out.println("YES!");
        			System.out.println("select mandantid from activeusers where sessionid = '"+sessionid+"'");
        			//Checken ob die Session noch gültig ist -> Nicht älter als 12h
    				long mid = BalancingConnectionManager.execQuery(
    								"select mandantid from activeusers where sessionid = '"+sessionid+"'"
    							).getLongOf(0, "mandantid");
    				
    				user.sessionid = sessionid;
    				user.mandantid = mid;
        
        		}else{
        			System.out.println("NO!");
        			// Keine aktive session in der DB, der Cookie kann gelöscht werden
    				deleteCookie(sessionid);        			
        		}
        		System.out.println("---------------");
            }
        } 		
        
        if(!cookieFound){ //Kein Cookie gefunden, es wird ein "Leerer" User erzeugt um ein Login zu erzwingen.
        	System.out.println("Cookie NOT found! Please Login!\n---------------");
        	user = new ActiveUser();
        	return;
        }
		
		//hier jetzt abgelaufene sessions killen und neue setzen!
		if(user.sessionid != null) {
			if(!ActiveUser.detectSessionIdInDB(user.sessionid)) {
				System.out.println("OUTDATED SESSIONID: "+user.sessionid);
				user.sessionid = null;
			}
		}
		
		if(request.getServerName().indexOf("recordmakers")>=0) {
			user.mandantid = 333; //wirklich ein long!
		}
		
//		user.mandant = Mandant.getMandant(user.mandantid);
//		templateprefix = user.mandant.get("templateprefix");
		
		if(user.sessionid != null) {
			try {
				Cookie ccc = new Cookie("MERCHSTORE_ADMIN_sessionid", user.sessionid);
				ccc.setMaxAge(60*60*12);//12 Stunden in Sekunden
				ccc.setPath("/");
				response.addCookie(ccc);
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		
		if(user.sessionid != null) {
//			user.ensureSessionData();
		}
	}
	
	public void makeOutput(Template t, VelocityContext c) throws Exception {        
		if(outStream == null) {
			prepareOut(response, "text/html; charset="+encoding);
		}
		
		OutputStreamWriter ow = new OutputStreamWriter(outStream, encoding);
		
		t.merge(c, ow);
		
		ow.flush();
	}   

	public final String gimmeValueAt(int index) {
		if(index < parameter[1].size()) {
			return (String)parameter[1].elementAt(index) ;
		}
		return null;
	}

	public final int gimmeParameterCount() {
		return parameter[0].size() ;
	}

	public final String gimmeNameAt(int index) {
		if(index < parameter[0].size())
			return (String)parameter[0].elementAt(index) ;

		return null;
	}
	
	public final Long gimmeLongValueOf(String name){
		try{
			return Long.parseLong(gimmeValueOf(name));
		}catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public final String gimmeValueOf(String name) {
		for(int i=0;i<parameter[0].size();i++) {
			if(name.equals((String)parameter[0].elementAt(i))) {                                
				return (String)parameter[1].elementAt(i) ;                
			}
		}
		return null;
	}
	
	public final boolean isset(String name) {
		for(int i=0;i<parameter[0].size();i++) {
			if(name.equals((String)parameter[0].elementAt(i))) {                                
				return true;
			}
		}
		return false;
	}

	public final void sendRedirect(String wohin) {
		try {
			response.sendRedirect(wohin);
		} catch (Exception e) {
			try {
				sendJavaRedirect(wohin);
			}catch(Exception e2) {
				e.printStackTrace();
				e2.printStackTrace();
			}
			e.printStackTrace();
		}
	}

	public final void sendJavaRedirect(String wohin) throws Exception {
		outStream.write(("<script type=\"text/javascript\">location.href=\""+wohin+"\"</script>redirecting to: "+wohin).getBytes());
	}

	public final void prepareOut(HttpServletResponse response, String _mime) {
		String mime = _mime;
		if(_mime.indexOf("text/")>=0 && _mime.indexOf("chars")<0) {
			mime = _mime+";charset="+encoding;
		}
		long ll = System.currentTimeMillis() - 1000*60*60*24;

		response.addDateHeader("expires", ll);//5 sekunden
		response.setDateHeader("Last-Modified", ll);

		response.addHeader("Content-Type", mime);
		response.setContentType(mime);
		response.setHeader("Connection", "close");

		try  {
			outStream = response.getOutputStream();
		} catch(Exception ex) {
			ex.printStackTrace();
		}        
	}


	public final static void printHashtable(Hashtable<Object, Object> hash) {
		Enumeration<Object> en = hash.keys();
		while(en.hasMoreElements()) {
			Object key = en.nextElement();
			Object value = hash.get(key);

			System.out.println(key.toString()+": "+value.toString());
		}
	}
	
    public final static double add(double d1, double d2) {
        return d1+d2;
    }
    public final static double add(String d1, String d2) {
        return Double.parseDouble(d1)+Double.parseDouble(d2);
    }
    public final static double add(String d1, double d2) {
        return Double.parseDouble(d1)+d2;
    }
    public final static double add(double d1, String d2) {
        return Double.parseDouble(d2)+d1;
    }
    
    public final static double multiply(double d1, double d2) {
        return d1*d2;
    }
    public final static double multiply(String d1, String d2) {
        return Double.parseDouble(d1)*Double.parseDouble(d2);
    }
    public final static double multiply(String d1, double d2) {
        return Double.parseDouble(d1)*d2;
    }
    public final static double multiply(double d1, String d2) {
        return Double.parseDouble(d2)*d1;
    }    
    
	public static String dropParameterFromURLString(String url, String valuename) {
		String ret = url;

		while(ret.indexOf(valuename+"=")>=0) {
			String s1 = ret.substring(0,ret.indexOf(valuename+"="));
			String s2 = ret.substring(ret.indexOf(valuename+"=") + (valuename+"=").length());

			if(s2.indexOf("&amp;")>=0) {
				s2 = s2.substring(s2.indexOf("&amp;")+5);

				//s1 = s1.substring(0, s1.length()-1);//& abschneiden...
			}
			else if(s2.indexOf("&")>=0) {
				s2 = s2.substring(s2.indexOf("&")+1);
			}
			else {
				s2 ="";
			}

			ret = s1 + s2;
		}

		return ret;
	}

	public static String setParameterInURL(String url, String valuename, String value) {
		String ret = dropParameterFromURLString(url, valuename);

		if(ret.indexOf('?')>=0) {
			ret += "&"+valuename+"="+urlEncode(value);
		}
		else {
			ret += "?"+valuename+"="+urlEncode(value);
		}

		return ret;
	}
	
	 
	public static String dropParameterFromURL(String url, String parameter) {
		if(url.indexOf('/'+parameter+'/')>=0){
			int startpos =url.indexOf("/"+parameter+"/");
			String sub = url.substring(startpos+("/"+parameter+"/").length());
			int endpos;
			if(sub.indexOf("/")>0){
				//falls weitere parameter vorhanden sind
				System.out.println("next / at - " + sub.indexOf("/"));
				endpos = sub.indexOf("/") + startpos + ("/"+parameter+"/").length();
			}
			else {
				//ansonsten bis zum ende des strings
				endpos=sub.length() + startpos + ("/"+parameter+"/").length();
			}
			String newurl=url.substring(0,startpos);
			newurl+=url.substring(endpos);
			return newurl;
		}
		return url;
	}
}  
