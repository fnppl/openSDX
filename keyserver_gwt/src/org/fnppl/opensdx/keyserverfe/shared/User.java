package org.fnppl.opensdx.keyserverfe.shared;

import java.io.Serializable;
import java.util.Vector;

public class User implements Serializable {

	private static final long serialVersionUID = -7780531669750929639L;
	
	private String name = "unknown";
	private Vector<KeyInfo> keys = new Vector<KeyInfo>();
	private Vector<KeyConnection> connections = new Vector<KeyConnection>();

	
	public User() {
		
	}
	
	public User(String name) {
		super();
		this.name = name;
	}

	public User(String name, Vector<KeyInfo> keys, Vector<KeyConnection> connections) {
		super();
		this.name = name;
		this.keys = keys;
		this.connections = connections;
	}

	public void addKey(KeyInfo ki) {
		keys.add(ki);
	}

	public void addConnection(KeyConnection kc) {
		if (!connections.contains(kc)) {
			connections.add(kc);
		}
	}
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Vector<KeyInfo> getKeys() {
		return keys;
	}

	public void setKeys(Vector<KeyInfo> keys) {
		this.keys = keys;
	}

	public Vector<KeyConnection> getConnections() {
		return connections;
	}

	public void setConnections(Vector<KeyConnection> connections) {
		this.connections = connections;
	}
	
	public void removeKey(String keyid) {
		Vector<KeyInfo> removeKeys = new Vector<KeyInfo>();
		for (KeyInfo ki : keys) {
			if (ki.getId().equals(keyid)) {
				removeKeys.add(ki);
			}
		}
		keys.removeAll(removeKeys);
		Vector<KeyConnection> removeConn = new Vector<KeyConnection>();
		for (KeyConnection c : connections) {
			if (c.getFromId().equals(keyid) || c.getToId().equals(keyid)) {
				removeConn.add(c);
			}
		}
		connections.removeAll(removeConn);
	}
	
	public Vector<KeyConnection> getLogs(String keyid) {
		Vector<KeyConnection> result1 = new Vector<KeyConnection>();
		Vector<KeyConnection> result2 = new Vector<KeyConnection>();
		for (KeyConnection c : connections) {
			if (c.getFromId().equals(keyid)) {
				result1.add(c);
			}
			else if (c.getToId().equals(keyid)) {
				result2.add(c);
			}
		}
		result1.addAll(result2);
		return result1;
	}

}
