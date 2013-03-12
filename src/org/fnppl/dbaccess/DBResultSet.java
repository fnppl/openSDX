package org.fnppl.dbaccess;

import java.sql.*;
import java.util.*;

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


//wrapper around a fully-fetched database-resultset

public class DBResultSet implements java.io.Serializable, Cloneable {
    static final long serialVersionUID = 6;
    
        private String errortext = null;

        private Vector<String> columnNames = new Vector<String>();//columname, columnlabel
        private Vector<String> columnLabels = new Vector<String>();//columname, columnlabel
        
        private String[][] data; //better be objects
        
        
        private ColumnNameMappings 
            colNameMappings = DUMMY_MAPPINGS,
            qualifiedMappings = DUMMY_MAPPINGS
            ;

        private int width=-1;
        private int height=-1;

        @Override
		public Object clone() {
            System.out.println("DBRESULTSET::clone");
            try {
                DBResultSet ret = (DBResultSet) super.clone();
                ret.data = data.clone();
                return ret;
            }
            catch (CloneNotSupportedException x){
                throw new InternalError(x.toString());
            }
        }
        
        public DBResultSet(Statement Stmt, String sql) {
            Object[] key = new Object[]{sql,new Long(System.currentTimeMillis())};
            try {
                if(DBStatement.statme) {                            
                    DBStatement.currentexecs.add(key);
                }
                ResultSet rs = Stmt.executeQuery(sql);
                if(DBStatement.statme) {
                    DBStatement.currentexecs.remove(key);
                }
                
                //Stmt.setFetchSize(rs.getMetaData().
                fetch(rs);
                
//                System.err.println("rs.closing");
                rs.close();
//                System.err.println("rs.closed");
//                Stmt.close();
            }
            catch (SQLException E) {
            		java.util.Date d = new java.util.Date();
                	this.errortext = "SQLException ["+d+"]: \n\t" + E.getMessage()+"\n\tSQLState: " + E.getSQLState()+"\n\tVendorError: " + E.getErrorCode() ;
                    
                    if(DBStatement.statme) {
                        DBStatement.currentexecs.remove(key);
                    }
            }
        }
        
        public DBResultSet(ResultSet rs) {
            try{
                fetch( rs );
            }
            catch (SQLException E){
            	java.util.Date d = new java.util.Date();
                this.errortext = "SQLException ["+d+"]: \n\t" + E.getMessage()+"\n\tSQLState: " + E.getSQLState()+"\n\tVendorError: " + E.getErrorCode() ;
            }
        }
        
        private void fetch(ResultSet rs) throws SQLException {
            if( rs == null )
                return ;
            
            ResultSetMetaData meta = rs.getMetaData();
            
            int widthcache = meta.getColumnCount();
            
            columnNames.clear(); // = new String[ widthcache ];
            columnLabels.clear(); // = new String[ widthcache ];
            
            Vector<String> qualified = new Vector<String>();
            //[ widthcache ];
            
//            for(int xx=0, nn = columnNames.size(); xx < nn; xx++){
            for(int xx=0; xx<widthcache; xx++){
                columnNames.addElement(meta.getColumnName(xx+1)); //
                columnLabels.addElement(meta.getColumnLabel(xx+1)); //
                
//                columnLabels[xx] = meta.getColumnLabel(xx+1); //columnnames 1 basiert.-
                
//                qualified[xx] = meta.getTableName(xx+1) + "." + columnNames.lastElement();
                qualified.addElement(meta.getTableName(xx+1) + "." + columnNames.lastElement());
            }
            
            //4:
            colNameMappings = new ColumnNameMappings(columnNames);
            qualifiedMappings = new ColumnNameMappings(qualified);
            
//            System.err.println("going to fetch 3...");
            
            
            List l = new LinkedList();
            String[] row;
            for(; rs.next(); ){
                row = new String[ widthcache ];
                for(int xx=0; xx < widthcache; xx++){
                    row[xx] = rs.getString(xx+1); //ALLE indices 1 basiert ?!-
                }
                l.add(row);
            }
            
            data = (String[][]) l.toArray(new String[ height = l.size() ][]);
            width = widthcache;
            
//            System.err.println("going to fetch 4...");
        }
        
        
        //4:
        @Override
		protected void finalize() throws Throwable {
            data = null;
            
            //4:
            columnNames = null;
            colNameMappings = null;
            
            super.finalize();
        }
        
        public String gimmeNameAt(int index) {
            if(index >= 0 && index < width) {
                return columnNames.elementAt(index);
                //[index] ;
            }

            return null;
        }
        public String gimmeLabelAt(int index) {
            if(index >= 0 && index < width) {
                return columnLabels.elementAt(index);
                //[index] ;
            }

            return null;
        }
        
        public static String[] vtoa(Vector<String> v) {
        	String[] a = new String[v.size()];
        	for(int i=0;i<a.length;i++) {
        		a[i] = v.elementAt(i);
        	}
        	return a;
        }
        public Vector<String> gimmeColNames() {
            return (Vector<String>)columnNames.clone();
        }
        public Vector<String> gimmeColLabels() {
            return (Vector<String>)columnLabels.clone();
        }
        
        public Vector<String> gimmeColNamesRef(){
            return columnNames;
        }

        public int gimmeIndexOf(String name) {
            return colNameMappings.getColumnIndex(name);
        }
        
        public int gimmeQualifiedIndexOf(String table, String column){
            return qualifiedMappings.getColumnIndex(table+"."+column);
        }
        
        public String[] getColumnData(int columnIndex){
            if( columnIndex < 0 || columnIndex >= width ){
                throw new IndexOutOfBoundsException("columnIndex out of bounds: "+columnIndex);
            }
            
            String ret[] = new String[ height() ];
            for(int xx=0; xx < ret.length; xx++){
                ret[xx] = data[xx][columnIndex];
            }
            
            return ret;
        }
        
        public String[] getColumnData(String colname){
            try{
                return getColumnData( gimmeIndexOf(colname) );
            }
            catch (IndexOutOfBoundsException x){
                throw new IllegalArgumentException("Column does not exist: "+colname);
            }
        }
        
        public String[] getRowData(int rowIndex){
            if( rowIndex < 0 || rowIndex >= height ){
                throw new IndexOutOfBoundsException("rowIndex out of bounds: "+rowIndex);
            }
            
            return data[ rowIndex ]; //kein kopiern. Scheiss drauf.
        }
        
        public String getValueAt(int rowIndex, int columnIndex) {
            if( rowIndex < 0 || rowIndex >= height() )
//                throw new IndexOutOfBoundsException("rowIndex out of bounds: "+rowIndex);
                return null;
            if( columnIndex < 0 || columnIndex >= width() )
//                throw new IndexOutOfBoundsException("columnIndex out of bounds: "+columnIndex);
                return null;
            
            return data[rowIndex][columnIndex];
        }

        public String getValueOf(int row, String columnname) {
            int index = gimmeIndexOf(columnname);
            return index >= 0 ? getValueAt(row, index) : null;
        }

        public String getValueOf(int row, String table, String column){
            int index = gimmeQualifiedIndexOf(table, column);
            return index >= 0 ? getValueAt(row, index) : null;
        }
        public long getLongOf(int i, String columname) {
        	try {
        		return Long.parseLong(getValueOf(i, columname));
			} catch (Exception e) {
				// TODO: handle exception
			}
    		return -1;
    	}
        public long getLongAt(int row, int column) {
        	try {
        		return Long.parseLong(getValueAt(row, column));
			} catch (Exception e) {
				// TODO: handle exception
			}
    		return -1;
    	}
        public int getIntOf(int i, String columname) {
        	try {
        		return Integer.parseInt(getValueOf(i, columname));
			} catch (Exception e) {
				// TODO: handle exception
			}
    		return -1;
    	}
        public int getIntAt(int row, int column) {
        	try {
        		return Integer.parseInt(getValueAt(row, column));
			} catch (Exception e) {
				// TODO: handle exception
			}
    		return -1;
    	}
        
        public boolean getBooleanOf(int i, String columname) {
        	try {
        		return getValueOf(i, columname).indexOf("t")==0;
			} catch (Exception e) {
				// TODO: handle exception
			}
    		return false;
    	}
        public boolean getBooleanAt(int row, int column) {
        	try {
        		return getValueAt(row, column).indexOf("t")==0;
			} catch (Exception e) {
				// TODO: handle exception
			}
    		return false;
    	}
        
        public int width() {
            return this.width ;
        }

        public int height() {
            return this.height;
        }

        public boolean errorOccured() {
            if(errortext==null)
                return false;
            else
                return true;
        }

        public String gimmeErrorText() {
            return this.errortext;
        }
        
        public void reset() { 
//            data = new String[0][]; 
//            columnNames = new String[0]; 
//            colNameMappings = null;
//            width = height = 0; 
        }
        public void closeRS() { }
        
        // ICH WILL POSTGRES 8.0 !
        public synchronized void addColumn(String name){
            String[] niounames = new String[ width + 1 ];
            for(int i=0;i<columnNames.size();i++){
            	niounames[i] = columnNames.elementAt(i);
            }
            
//            System.arraycopy(columnNames, 0, niounames, 0, width);
            niounames[width] = name;
            
            String[][] rev = (String[][]) reverseArray(data, width, height);
            
            String[][] nioudata = new String[ width + 1 ][];
            System.arraycopy(rev, 0, nioudata, 0, width); //hier kopiere ich Spalten
            nioudata[width] = new String[ height ];
            Arrays.fill(nioudata[width], "");
            
            nioudata = (String[][]) reverseArray(nioudata, height, width+1);
            
            columnNames.clear();
            for(int i=0; i<niounames.length; i++) {
            	columnNames.addElement(niounames[i]);
            }
            columnLabels.addElement(name);
            
            colNameMappings = new ColumnNameMappings(columnNames);
            
            data = nioudata;
            width++;
        }
        
//    public static Object[][] addColumn(Object[][] array, int width, int height){
//        
//        Object[][] rev = reverseArray(array, width, height);
//        
//        Object[][] nioudata = new Object[ width + 1 ][];
//        System.arraycopy(rev, 0, nioudata, 0, width); 
//        nioudata[width] = new Object[ height ];
//        
//        nioudata = reverseArray(nioudata, height, ++width);
//        
//        width++;
//        return nioudata;
//    }
    
        public synchronized void removeRow(int rowIndex){
            if(rowIndex < 0 || rowIndex >= height()) 
                throw new IndexOutOfBoundsException();
            
            //hier nur einmal verschieben...
            System.arraycopy(data, rowIndex+1, data, rowIndex, data.length-rowIndex-1);
            height--;
        }
        
        public synchronized void setValueAt(int rowindex, int columnindex, String value){
            if(rowindex < 0 || rowindex >= height() )
                throw new IndexOutOfBoundsException("Row index out of bounds: "+rowindex);
            if( columnindex < 0 || columnindex >= width())
                throw new IndexOutOfBoundsException("Column index out of bounds: "+rowindex);
            
            data[rowindex][columnindex] = encode(value);
        }
        
        public synchronized void addRow(){
            String[][] no = new String[height+1][width];
            
            //hier nur einmal verschieben...
            System.arraycopy(data, 0, no, 0, data.length);
            
            String[] col= new String[width];
            Arrays.fill(col, "");
            
            no[height] = col;
            
            data = no;
            
            height++;
        }
        
        
        public synchronized void setValueOf(int rowindex, String column, String value){
            setValueAt(rowindex, gimmeIndexOf(column), value);
        }
        
        public String encode(String s) {
        //ï¿½berfï¿½hrt einen string in einen html-string
            if(s==null) return "";

            return s;
        }
    public String[] toArray(int col) {
        int h = height();
        if(h < 0) {
            h = 0;
        }
        String[] ret = new String[h];
        for(int i=0;i<ret.length;i++) {
            ret[i] = getValueAt(i, col);
        }
        
        return ret;
    }
    
    public void addResultSet(DBResultSet Rs, int offsetRow, int rowCount) throws Exception {
    	if(Rs.width() != width()) {
    		throw new Exception("WIDTHs does not match: "+Rs.width()+"!="+width());
    	}
    	
    	 String[][] no = new String[height + (rowCount>Rs.height() ? Rs.height() : rowCount)][width];
         
         //hier nur einmal verschieben...
         System.arraycopy(data, 0, no, 0, data.length);
         
         for(int i=0;i<Rs.height && i<rowCount; i++) {
        	 no[i+height] = Rs.getRowData(i + offsetRow);
         }
         
         data = no;
         
         height = height + Rs.height();
    }
    
    public String[] toArray(String colname) {
        int col = gimmeIndexOf(colname);
        return toArray(col);
    }
    
    private static Object[][] reverseArray(Object[][] array, int width, int height){
        if( width == height ){
            reverseArray0(array, array, width, height);
            return array;
        }
        
        String[][] rev = new String[width][];
        for(int jj=0; jj < rev.length; jj++) rev[jj] = new String[height];
        reverseArray0(array, rev, width, height);
        return rev;
    }
    
    
 
    //TODO optimizien über zweistufige HASHTABLE HT 26.10.2007
    public int getLineOfValue(String value, String colname) {
        int col = gimmeIndexOf(colname);
        return getLineOfValue(value,col);
    }
    public int getLineOfValue(String value, int col) {
        for(int i=0;i<height();i++) {
            String s = getValueAt(i, col);
            if(s.equals(value)) {
                return i;
            }
        }
        return -1;
    }
    
    private static void reverseArray0(Object[][] array, Object[][] output, int width, int height){
        if( array == output ){
            Object tmp;
            for(int xx=0; xx < width; xx++){
                for(int yy=xx; yy < height; yy++){
                    tmp = array[xx][yy];
                    array[xx][yy] = array[yy][xx];
                    array[yy][xx] = tmp;
                }
            }
        }
        else {
            for(int xx=0; xx < width; xx++){
                for(int yy=0; yy < height; yy++){
                    output[xx][yy] = array[yy][xx];
                }
            }
        }
    }
    
    public void distinctMe(String columname) {
        HashSet vs = new HashSet();
        Vector dels = new Vector();
        for(int i=0;i<height();i++) {
            String v = ""+getValueOf(i, columname);
            if(vs.contains(v)) {
                dels.addElement(new Integer(i));
            }
            else {
                vs.add(v);
            }
        }
        
        for(int i=dels.size()-1;i>=0;i--) {
            Integer ii = (Integer)dels.elementAt(i);
            removeRow(ii.intValue());
        }
    }
 
    //4:
    private static class ColumnNameMappings
    implements java.io.Serializable, Cloneable
    {
        String[] sortedNames;
        int[] indices;
        
        public ColumnNameMappings(Vector<String> colnames){
            sortedNames = colnames.toArray(new String[]{});
            Arrays.sort(sortedNames);
            
            indices = new int[ colnames.size()];
            
            for(int ii = colnames.size() - 1; ii >= 0; ii--){
                int index = index0(colnames.elementAt(ii));
                indices[index] = ii;
            }
        }
        
        private int index0(String name){
            return name == null ? -1 : Arrays.binarySearch(sortedNames, name);
        }
        
        public int getColumnIndex(String colname){
            int index = index0(colname);
            return index >= 0 ? indices[index] : -1;
        }
        
        @Override
		public Object clone(){
            try{
                ColumnNameMappings clone = (ColumnNameMappings) super.clone();
                clone.sortedNames = sortedNames.clone();
                clone.indices = indices.clone();

                return clone;
            }
            catch (CloneNotSupportedException x){
                throw new InternalError(x.toString());
            }
        }
        
        static final long serialVersionUID = 2;
    }        
 
    private static final ColumnNameMappings
        DUMMY_MAPPINGS = new ColumnNameMappings(new Vector<String>()) {
            @Override
			public int getColumnIndex(String s){
                return -1;
            }
        }
    ;
}