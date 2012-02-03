package org.fnppl.opensdx.keyserverfe.shared;

import java.io.Serializable;

public class NodeState implements Serializable {
	
	private static final long serialVersionUID = 5033901790310573628L;
	
	private String keyid = "";
	private int posX = -1;
	private int posY = -1;
	private boolean showIn = true;
	private boolean showOut = true;
	private boolean myKey = false;
	private boolean directTrust = false;
	private int visibilityLevel = KeyInfo.VISIBILITY_ALWAYS;
	
	public NodeState() {
		
	}

	public NodeState(String keyid, int posX, int posY, boolean showIn,
			boolean showOut, boolean myKey, boolean directTrust,
			int visibilityLevel) {
		super();
		this.keyid = keyid;
		this.posX = posX;
		this.posY = posY;
		this.showIn = showIn;
		this.showOut = showOut;
		this.myKey = myKey;
		this.directTrust = directTrust;
		this.visibilityLevel = visibilityLevel;
	}

	public String getKeyid() {
		return keyid;
	}

	public void setKeyid(String keyid) {
		this.keyid = keyid;
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

	public boolean isShowIn() {
		return showIn;
	}

	public void setShowIn(boolean showIn) {
		this.showIn = showIn;
	}

	public boolean isShowOut() {
		return showOut;
	}

	public void setShowOut(boolean showOut) {
		this.showOut = showOut;
	}

	public boolean isMyKey() {
		return myKey;
	}

	public void setMyKey(boolean myKey) {
		this.myKey = myKey;
	}

	public boolean isDirectTrust() {
		return directTrust;
	}

	public void setDirectTrust(boolean directTrust) {
		this.directTrust = directTrust;
	}

	public int getVisibilityLevel() {
		return visibilityLevel;
	}

	public void setVisibilityLevel(int visibilityLevel) {
		this.visibilityLevel = visibilityLevel;
	}
	

}
