package org.fnppl.opensdx.keyserver.helper;

public final class IdGenerator {
	private static Object o = new Object();
	private static long lastTimeStamp = System.currentTimeMillis();

	public static long getTimestamp() {
		long jetzt = System.currentTimeMillis();
		synchronized(o) {
			if(jetzt <= lastTimeStamp) {
				jetzt = lastTimeStamp +1 ;
			}
			lastTimeStamp = jetzt;
		}
		return jetzt;
	}
}