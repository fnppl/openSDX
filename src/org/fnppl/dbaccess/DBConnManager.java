package org.fnppl.dbaccess;

public class DBConnManager {
	private static DBConnManager defaultInstance = null;
	
	public static DBResultSet execQuery(String sql) {
		return defaultInstance.execQ(sql);
	}
	public DBResultSet execQ(String sql) {
		return null;
	}
	
	public static int execUpdate(String sql) {
		return defaultInstance.execU(sql);
	}
	public int execU(String sql) {
		return -1;
	}
	
	public static String dbEncode(String s) {
		//HT 20110210 - encode quotes etc.
		return s;
	}
}
