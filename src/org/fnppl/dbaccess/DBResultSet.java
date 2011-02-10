package org.fnppl.dbaccess;

//wrapper around a fully-fetched database-resultset

public class DBResultSet {
	public int width() {
		return -1;
	}
	
	public String getNameAt(int w) {
		return null;
	}
	public String getSValueAt(int line, int column) {
		return null;
	}
	public String getSValueOf(int line, String name) {
		return null;
	}
	public int height() {
		return -1;
	}
}
