package org.fnppl.opensdx.keyserverfe.shared;

import java.io.Serializable;

public class KeyInfo implements Serializable {

	private static final long serialVersionUID = 4924340117790639027L;
	
	public final static int VISIBILITY_ALWAYS = 4;
	public final static int VISIBILITY_HIGH   = 3;
	public final static int VISIBILITY_MEDIUM = 2;
	public final static int VISIBILITY_LOW    = 1;
	public final static int VISIBILITY_NONE   = 0;
	
	public final static String[] STATUS_TEXT = new String[] {"unapproved","valid","revoked","outdated","usage not allowed","key not found","status not set"};
	public final static int STATUS_UNAPPROVED = 0;
	public final static int STATUS_VALID = 1;
	public final static int STATUS_REVOKED = 2;
	public final static int STATUS_OUTDATED = 3;
	public final static int STATUS_USAGE_NOT_ALLOWED = 4;
	public final static int STATUS_KEY_NOT_FOUND = 5;
	public final static int STATUS_NOT_SET = 6;
	
	private int no = 0;
	private String id = "unknown";
	private String idShort = "unknown";
	private String level = "unknown";
	private String usage = "unknown";
	private String owner = "unknown";
	private String mnemonic = "unknown";
	private long validFrom = Long.MIN_VALUE;
	private long validUntil = Long.MIN_VALUE;
	
	private boolean myKey = false;
	private int status = STATUS_KEY_NOT_FOUND;
	private boolean directTrust = false;
	private boolean indirectTrust = false;
	
	private boolean incomingLogs = false;
	private boolean outgoingLogs = false;
	
	//display
	private int visibilityLevel = VISIBILITY_ALWAYS;
	private int posX = -1;
	private int posY = -1;
	
	public KeyInfo() {
		
	}

	public KeyInfo(String id, String idShort, String level, String usage, String owner,
			String mnemonic, long validFrom, long validUntil, boolean myKey,
			int status, boolean directTrust, int visibilityLevel, int posX,
			int posY, boolean incomingLogs, boolean outgoingLogs) {
		super();
		this.id = id;
		this.idShort = idShort;
		this.level = level;
		this.usage = usage;
		this.owner = owner;
		this.mnemonic = mnemonic;
		this.validFrom = validFrom;
		this.validUntil = validUntil;
		this.myKey = myKey;
		this.status = status;
		this.directTrust = directTrust;
		this.visibilityLevel = visibilityLevel;
		this.posX = posX;
		this.posY = posY;
		this.incomingLogs = incomingLogs;
		this.outgoingLogs = outgoingLogs;
	}

	public KeyInfo(String id, String idShort, String level, String usage, String owner,
			String mnemonic, long validFrom, long validUntil, boolean myKey,
			int status, boolean directTrust) {
		super();
		this.id = id;
		this.idShort = idShort;
		this.level = level;
		this.usage = usage;
		this.owner = owner;
		this.mnemonic = mnemonic;
		this.validFrom = validFrom;
		this.validUntil = validUntil;
		this.myKey = myKey;
		this.status = status;
		this.directTrust = directTrust;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIdShort() {
		return idShort;
	}

	public void setIdShort(String idShort) {
		this.idShort = idShort;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getUsage() {
		return usage;
	}

	public void setUsage(String usage) {
		this.usage = usage;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getMnemonic() {
		return mnemonic;
	}

	public void setMnemonic(String mnemonic) {
		this.mnemonic = mnemonic;
	}

	public long getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(long validFrom) {
		this.validFrom = validFrom;
	}

	public long getValidUntil() {
		return validUntil;
	}

	public void setValidUntil(long validUntil) {
		this.validUntil = validUntil;
	}

	public boolean isMyKey() {
		return myKey;
	}

	public void setMyKey(boolean myKey) {
		this.myKey = myKey;
	}

	public boolean isValid() {
		return status==STATUS_VALID;
	}

	public int getStatus() {
		return status;
	}
	public String getStatusText() {
		return STATUS_TEXT[status];
	}
	
	public void setStatus(int status) {
		this.status = status;
	}

	public boolean isDirectTrust() {
		return directTrust;
	}

	public void setDirectTrust(boolean directTrust) {
		this.directTrust = directTrust;
	}

	public boolean isIndirectTrust() {
		return indirectTrust;
	}

	public void setIndirectTrust(boolean indirectTrust) {
		this.indirectTrust = indirectTrust;
	}

	public int getVisibilityLevel() {
		return visibilityLevel;
	}

	public void setVisibilityLevel(int visibilityLevel) {
		this.visibilityLevel = visibilityLevel;
	}

	public int getPosX() {
		return posX;
	}

	public void setPosX(int posX) {
		this.posX = posX;
	}

	public int getPosY() {
		return posY;
	}

	public void setPosY(int posY) {
		this.posY = posY;
	}

	public int getNo() {
		return no;
	}

	public void setNo(int no) {
		this.no = no;
	}

	public boolean isIncomingLogs() {
		return incomingLogs;
	}

	public void setIncomingLogs(boolean incomingLogs) {
		this.incomingLogs = incomingLogs;
	}

	public boolean isOutgoingLogs() {
		return outgoingLogs;
	}

	public void setOutgoingLogs(boolean outgoingLogs) {
		this.outgoingLogs = outgoingLogs;
	}
	
}
