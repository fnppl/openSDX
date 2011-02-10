package org.fnppl.opensdx.dbaccess;

public class DBConnManager {
	private static DBConnManager defaultInstance = null;
	
	public static DBResultSet execQuery(String sql) {
		return defaultInstance.execQ(sql);
	}
	public DBResultSet execQ(String sql) {
		return null;
	}
}
