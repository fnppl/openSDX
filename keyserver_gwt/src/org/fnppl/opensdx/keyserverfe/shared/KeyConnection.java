package org.fnppl.opensdx.keyserverfe.shared;

import java.io.Serializable;
import java.util.Vector;


public class KeyConnection implements Serializable {
	
	private static final long serialVersionUID = -5426138524887561862L;
	
	public final static int TYPE_UNKNOWN          = 0;
	public final static int TYPE_APPROVAL         = 1;
	public final static int TYPE_APPROVAL_PENDING = 2;
	public final static int TYPE_DISAPPROVAL      = 3;
	public final static int TYPE_REVOCATION       = 4;
	public final static int TYPE_SUBKEY           = 5;
	public final static int TYPE_REVOKEKEY        = 6;
	private final static String[] TYPE_TEXT = new String[] {
		"unknown",
		"approval",
		"app. pending",
		"disapproval",
		"revocation",
		"subkey",
		"revokekey"
	};
		
	private String fromId = "unknown";
	private String toId = "unknown";
	private int type = TYPE_UNKNOWN;
	private long date = Long.MIN_VALUE;
	
	//display
	private Vector<int[]> path = null;

	public KeyConnection() {
		
	}
	
	public KeyConnection(String fromId, String toId, int type, long date,Vector<int[]> path) {
		super();
		this.fromId = fromId;
		this.toId = toId;
		this.type = type;
		this.date = date;
		this.path = path;
	}

	public KeyConnection(String fromId, String toId, int type, long date) {
		super();
		this.fromId = fromId;
		this.toId = toId;
		this.type = type;
		this.date = date;
	}

	public String getFromId() {
		return fromId;
	}

	public void setFromId(String fromId) {
		this.fromId = fromId;
	}

	public String getToId() {
		return toId;
	}

	public void setToId(String toId) {
		this.toId = toId;
	}

	public int getType() {
		return type;
	}
	
	public String getTypeText() {
		if (type>=0 && type < TYPE_TEXT.length) {
			return TYPE_TEXT[type];
		}
		return "unknown";
	}

	public void setType(String ttype) {
		type = TYPE_UNKNOWN;
		if (ttype.equalsIgnoreCase("approval")) type = TYPE_APPROVAL;
		else if (ttype.equalsIgnoreCase("approval_pending")) type = TYPE_APPROVAL_PENDING;
		else if (ttype.equalsIgnoreCase("disapproval")) type = TYPE_DISAPPROVAL;
		else if (ttype.equalsIgnoreCase("revocation")) type = TYPE_REVOCATION;
		else {
			for (int i=0;i<TYPE_TEXT.length;i++) {
				if (TYPE_TEXT[i].equalsIgnoreCase(ttype)) {
					type = i;
					return;
				}
			}
		}
	}
	
	public void setType(int type) {
		this.type = type;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public Vector<int[]> getPath() {
		return path;
	}

	public void setPath(Vector<int[]> path) {
		this.path = path;
	}
	
	public boolean hasPath() {
		if (path==null) {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof KeyConnection)) return false;
		KeyConnection kc = (KeyConnection)obj;
		if (date!=kc.date) return false;
		if (!fromId.equals(kc.fromId)) return false;
		if (!toId.equals(kc.toId)) return false;
		if (type!=kc.type) return false;
		return true;
	}
	
}
