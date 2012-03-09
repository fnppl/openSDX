package org.fnppl.opensdx.keyserverfe;
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

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.net.*;
import java.text.*;

import org.fnppl.dbaccess.DBResultSet;
import org.jdom.*;
import org.jdom.output.*;


public class Helper {
    public static final String XML_OUTPUTTER_CHARSET ="UTF-8";
    
    private static final IdGenerator IDGen = new IdGenerator();
    
    public static String createTmpId() {
        long jetzt = IDGen.getTimestamp();
        return "tmp"+jetzt;
    }
    public static long getTimeStamp() {
        return IDGen.getTimestamp();
    }
    
    
    
    public static String elementToString(Element e){
        if(e==null)
            return null;
        
        try{
            org.jdom.output.Format f = org.jdom.output.Format.getPrettyFormat();
            f.setEncoding(XML_OUTPUTTER_CHARSET);
            XMLOutputter xmlOutputter = new XMLOutputter(f);


            ByteArrayOutputStream baous = new ByteArrayOutputStream();
            xmlOutputter.output(e, baous);
            return baous.toString();
        }
        catch (Exception x){
            x.printStackTrace();
            return null;
        }
    }
    
    public static String elementToString(Document doc){
        if(doc==null)
            return null;
        
        try{
            org.jdom.output.Format f = org.jdom.output.Format.getPrettyFormat();
            f.setEncoding(XML_OUTPUTTER_CHARSET);
            XMLOutputter xmlOutputter = new XMLOutputter(f);
            
            ByteArrayOutputStream baous = new ByteArrayOutputStream();
            
            return baous.toString();
        }
        catch (Exception x){
            x.printStackTrace();
            return null;
        }
    }
    
    public static void elementToWriter(Document e, OutputStreamWriter w){
        if(e==null)
            return ;
        
        try{
            org.jdom.output.Format f = org.jdom.output.Format.getPrettyFormat();
            f.setEncoding(XML_OUTPUTTER_CHARSET);
            XMLOutputter xmlOutputter = new XMLOutputter(f);
            
            xmlOutputter.output(e, w);            
        }
        catch (Exception x){
            x.printStackTrace();
            return ;
        }
    }
    public static void elementToWriter(Element e, OutputStreamWriter w){
        if(e==null)
            return ;
        
        try{
            org.jdom.output.Format f = org.jdom.output.Format.getPrettyFormat();
            f.setEncoding(XML_OUTPUTTER_CHARSET);
            XMLOutputter xmlOutputter = new XMLOutputter(f);
            
            xmlOutputter.output(e, w);            
        }
        catch (Exception x){
            x.printStackTrace();
            return ;
        }
    }
    
    
    
    
    

   
    
    public static String buildSessionXml(Hashtable hash) {
        Element e = new Element("sessiondata");
        
        Enumeration en = hash.keys();
        while(en.hasMoreElements()) {
            String key = (String)en.nextElement();
            Object value= hash.get(key);
            
            if(value instanceof String) {
                e.addContent((new Element(key)).setText((String)value));
            }
        }
        
        org.jdom.output.XMLOutputter xout = new org.jdom.output.XMLOutputter();
        return xout.outputString(e);
    }
    public static void printHashtable(Hashtable hash) {
        Enumeration en = hash.keys();
        while(en.hasMoreElements()) {
            String key = (String)en.nextElement();
            Object value= hash.get(key);
            
            System.out.println(key+ " -> "+value);
        }
        
    }
    public static Hashtable buildSessionHash(String xml) {
        Hashtable hash = new Hashtable();
        org.jdom.input.SAXBuilder sax = new org.jdom.input.SAXBuilder(false);
        try {
            Element e = sax.build(new StringReader(xml)).getRootElement();
            Iterator it = e.getChildren().iterator();
            while(it.hasNext()) {
                Element ee = (Element)it.next();
                hash.put(ee.getName(), ee.getText());
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            hash = null;
        }
        
        return hash;
    }
    
    
    public static String dbEncode(String s) {
        StringBuffer ret = new StringBuffer();
        
        if(s == null) s = "";
        
        for(int i=0;i<s.length();i++) {
            char c = s.charAt(i);
            
            if(c == '\'') {
                ret.append('\'');//scheint die neue syntax zu sein HT 15.01.2008
            }
            
            ret.append(c);
        }
        
        return ret.toString();
    }
    
    public static String toHex(byte[] b) {
        StringBuffer sb = new StringBuffer();
        for(int i=0;i<b.length;i++) {
            int k = b[i] ;
            if(b[i]<0) {
                k = Byte.MAX_VALUE;
                k += (Byte.MIN_VALUE - b[i]) *-1 + 1;
            }
            if(k<16) {
                sb.append("0");
            }
            sb.append(Integer.toHexString(k));
        }
        
        
        return sb.toString();
    }

    public static String getMD5ForData(byte[] b) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");            

            md.update(b);            
            return toHex(md.digest());
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        
        return "";
    }
    
    
    public static String umlautEncode(String src) {
        StringBuffer s=new StringBuffer();
        int wo;
        for(int i=0; i<src.length(); i++) {
            char c = src.charAt(i);
            
            switch(c) {
                case 'ä':
                    s.append("ae");
                    break;
                case 'Ä':
                    s.append("Ae");
                    break;
                case 'ö':
                    s.append("oe");
                    break;
                case 'Ö':
                    s.append("Oe");
                    break;                
                case 'Ü':
                    s.append("Ue");
                    break;
                case 'ü':
                    s.append("ue");
                    break;
                case 'ß':
                    s.append("ss");
                    break;
                case ' ':
                    s.append("_");
                    break;
                case '"':
                    break;
                case '?':
                    break;
                case '/':
                    s.append("_");
                    break;
                case '!':
                    break;
                case '&':
                    s.append(" and ");
                    break;
                default:
                    s.append(c);
                    break;
            }
        }
        return s.toString();
    }    
    
    
    public static Element dbResultSetToElement(DBResultSet rset){
        return dbResultSetToElement(rset, null);
    }
    
    

    public static Element dbResultSetToElement(DBResultSet rset, Vector<String> columnnames){
        Element raus = new Element("resultset");
        
        if(columnnames==null) {
            columnnames = rset.gimmeColNames();
        }
        
        Element meta = new Element("meta");
        meta.addContent(new Element("columncount").setText(String.valueOf(columnnames.size())));
        meta.addContent(new Element("rowcount").setText(String.valueOf(rset.height())));
        Element e = new Element("columnnames");
        for(int kk=0; kk < columnnames.size(); kk++)
            e.addContent(new Element("col"+kk).setText(columnnames.elementAt(kk)));
        meta.addContent(e);
        
        raus.addContent(meta);
        
        Element row;
        for(int aa=0; aa < rset.height(); aa++){
            row = new Element("row"+aa);
            for(int bb=0; bb < columnnames.size(); bb++){
                row.addContent( new Element("col"+bb).setText( rset.getValueOf(aa, columnnames.elementAt(bb)) ) );
            }
            raus.addContent(row);
        }
        
        return raus;
    }
    
    public static Element dbResultSetToNamelyElement(
    		DBResultSet rset, 
    		String retname, 
    		String linename //, 
    		//String dateColName
    	){
        Element raus = new Element(retname);
        String tmpCol = null;     
        
        Vector<String> columnames = rset.gimmeColNames();
        
        for(int i=0;i<rset.height();i++) {
        	Element me = new Element(linename);
        	raus.addContent(me);
        	
        	for(int ii=0;ii<columnames.size();ii++) {
        		Element e = new Element(columnames.elementAt(ii));
        		me.addContent(e);
        		
        		tmpCol = rset.getValueOf(i, columnames.elementAt(ii));
        		
//        		if(columnames[ii].equalsIgnoreCase(dateColName)){
//        			e.setText(Helper.getDateStringFromMillis(Long.parseLong(tmpCol)));
//        		}
//        		else{
        			e.setText(rset.getValueOf(i, columnames.elementAt(ii)));
//        		}
        	}
        }
        
        
        return raus;
    }
    
    
    public static void handleInput(final String prefix, final Process p) {
        Thread t = new Thread() {
            public void run() {
                try {
                    InputStream out = p.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(out));
                    String zeile="";
                    while((zeile=br.readLine())!=null) {
                        //appendMsg("in: "+zeile+"\n");
//                        Logger.logMsg(prefix+":: "+zeile);
                    }
                    
                } catch(Exception ex) {
                    //appendErr(exceptionToString(ex)+"\n");
                    ex.printStackTrace();
                }
            }
        };
        t.start();
        
        t = new Thread() {
            public void run() {
                try {
                    InputStream out = p.getErrorStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(out));
                    String zeile="";
                    while((zeile=br.readLine())!=null) {
                        //appendMsg("err: "+zeile+"\n");
//                        Logger.logMsg(prefix+":: "+zeile);
                    }
                } catch(Exception ex) {
//                    appendErr(exceptionToString(ex)+"\n");
                    ex.printStackTrace();
                }
            }
        };
        t.start();
    }
    
    private static void putIfPresent(Map tags, String key, String value){
        if (value != null)
            try {
                tags.put(key, value);
            } catch (Exception e) {e.printStackTrace();}
    }
    
    
   public static String getYear(String date) throws Exception {
        long l = Long.parseLong(date);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(l);
        return String.valueOf(cal.get(Calendar.YEAR));
   }
   
   
    public static byte[] getFileAsBytes(String filename) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            File f = new File(filename);
            FileInputStream fin = new FileInputStream(f);
            byte[] b = new byte[32000];
            int r = 0;
            while((r=fin.read(b))!=-1) {
                bout.write(b,0,r);
            }
            fin.close();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
//        Logger.logMsg("file-ret: "+bout.size());
        return bout.toByteArray();
    }
    
    

    public static String join(Collection c, String separator){
        StringBuffer sb = new StringBuffer();
        for(Iterator ito = c.iterator(); ito.hasNext();){
            sb.append( String.valueOf(ito.next()) );
            if(ito.hasNext()){
                sb.append(separator);
            }
        }
        
        return sb.toString();
    }

    public static String join(Object[] o, String separator){
        StringBuffer sb = new StringBuffer();
        for(int ii=0; ii < o.length; ii++){
            sb.append( String.valueOf(o[ii]) );
            if(ii < o.length-1){
                sb.append(separator);
            }
        }
        
        return sb.toString();
    }
    
    public static String join(Object[] o, String separator, String prefix){
        StringBuffer sb = new StringBuffer();
        for(int ii=0; ii < o.length; ii++){
            sb.append( "'"+prefix+String.valueOf(o[ii])+"'" );
            if(ii < o.length-1){
                sb.append(separator);
            }
        }
        
        return sb.toString();
    }
    
    /** MIT klammern */
    public static String asSQLList(Collection c){
        String s = join(c, ",");
        if( s.length() == 0 )
            return "(null)";
        else
            return "("+s+")";
    }
    
    /** MIT klammern */
    public static String asSQLList(Object[] o){
        String s = join(o, ",");
        if( s.length() == 0 )
            return "(null)";
        else
            return "("+s+")";
    }

    
    public static String asSQLList(Object[] o, String prefix){
        String s = join(o, ",", prefix);
        if( s.length() == 0 )
            return "(null)";
        else
        	return "("+s+")";
    }
    
    public static String simplifyMax(String src) {
        StringBuffer s=new StringBuffer();
        
        if(src==null) return "";
        
        int wo;
        for(int i=0; i<src.length(); i++) {
            char c = src.charAt(i);
            switch(c) {
                case 'ä':
                    s.append("ae");
                    break;
                case 'Ä':
                    s.append("Ae");
                    break;
                case 'ö':
                    s.append("oe");
                    break;
                case 'Ö':
                    s.append("Oe");
                    break;                
                case 'Ü':
                    s.append("Ue");
                    break;
                case 'ü':
                    s.append("ue");
                    break;
                case 'ß':
                    s.append("ss");
                    break;
                case ' ':
                    s.append("_");
                    break;
                case ':':
                    s.append("_");
                    break;
                case '.':                    
                    break;
                case ';':                    
                    break;
                case '/':                    
                    break;
                default:
                    if(Character.isLetterOrDigit(c)) {
                        s.append(c);
                    }
                    break;
            }
        }
        return s.toString();
    }
    
    public static void copy(File src, File dst) throws Exception {
        
        FileInputStream fin = new FileInputStream(src);
        FileOutputStream fout = new FileOutputStream(dst);
        
        copy(fin, fout);
        
        fout.close();
        fin.close();
    }
    
    public static void copy(InputStream fin, OutputStream fout) throws Exception {
        byte[] buff = new byte[128000];
        int r = 0;
        
        while((r=fin.read(buff))>0) {
            fout.write(buff,0,r);
        }
        fout.flush();        
    }
    
    public static String cuteCut(String s, int len, char c){
        if(s.length()>len) {
            return s.substring(0, len);
        }
        return paddUp(s.subSequence(0, s.length()), len , c);
    }
    
    public static String paddUp(CharSequence s, int len, char c){
        StringBuffer sb = s == null ? new StringBuffer(len) : new StringBuffer(s.toString());
        for(; sb.length() < len; sb.append(c));
        return sb.toString();
    }
    
    public static boolean eq(Object o1, Object o2){
        return o1==o2 || o1!=null && o1.equals(o2);
    }
    
    public static boolean domBoolean(String arg){
        return 
            arg != null &&
            arg.length() > 0 &&
            ("t".equals(arg) || "true".equals(arg))
            ;
    }
    
    public static String domBigInt(String arg){
        try{
            return String.valueOf(Long.parseLong(arg));
        }
        catch (Exception x){
            return "-1";
        }
    }
    
    public static void handleInput(final Process p) {
        Thread t = new Thread() {
            public void run() {
                try {
                    InputStream out = p.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(out));
                    String zeile="";
                    while((zeile=br.readLine())!=null) {
                        //appendMsg("in: "+zeile+"\n");
                        System.out.println(zeile);
                    }
                    
                } catch(Exception ex) {
                    //appendErr(exceptionToString(ex)+"\n");
                    ex.printStackTrace();
                }
            }
        };
        t.start();
        
        t = new Thread() {
            public void run() {
                try {
                    InputStream out = p.getErrorStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(out));
                    String zeile="";
                    while((zeile=br.readLine())!=null) {
                        //appendMsg("err: "+zeile+"\n");
                        //Logger.logMsg("err: "+zeile+"\n");
                        System.err.println(zeile);
                    }
                } catch(Exception ex) {
                    //appendErr(exceptionToString(ex)+"\n");
                    ex.printStackTrace();
                }
            }
        };
        t.start();
    }
    
}
