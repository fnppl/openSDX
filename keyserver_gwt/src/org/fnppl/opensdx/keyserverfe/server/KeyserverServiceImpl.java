package org.fnppl.opensdx.keyserverfe.server;

import java.util.Vector;

import org.fnppl.opensdx.keyserverfe.client.KeyserverService;
import org.fnppl.opensdx.keyserverfe.shared.KeyConnection;
import org.fnppl.opensdx.keyserverfe.shared.KeyInfo;
import org.fnppl.opensdx.keyserverfe.shared.User;
import org.fnppl.opensdx.keyserverfe.shared.NodeState;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class KeyserverServiceImpl  extends RemoteServiceServlet implements KeyserverService {

	private static final long serialVersionUID = -2218898109562808572L;

	public User login(String username, String password)	throws IllegalArgumentException {
		return null;
	}

	public User loginAnonymous(String keyid) throws IllegalArgumentException {
		Vector<String> keyids = new Vector<String>();
		Vector<NodeState> states = new Vector<NodeState>();
		
		keyids.add(keyid);
		User user = updateKeyInfoAndLogs("anonymous", states, keyids, true, true);
		
//		long aYear = 1000L*3600L*24L*356L;
//		User user = new User("anonymous");
//		user.addKey(new KeyInfo("00:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11@it-is-awesome.de", "00:11...11:11", "SUB", "BOTH","boedeker@it-is-awesome.de","[restricted]",System.currentTimeMillis(), System.currentTimeMillis()+aYear, false, KeyInfo.STATUS_VALID, false));
//		user.addKey(new KeyInfo("11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11@it-is-awesome.de", "11:11...11:11", "MASTER", "BOTH","boedeker@it-is-awesome.de","[restricted]",System.currentTimeMillis(), System.currentTimeMillis()+aYear, false, KeyInfo.STATUS_VALID, false));
//		user.addKey(new KeyInfo("22:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11@it-is-awesome.de", "22:11...11:11", "REVOKE", "BOTH","boedeker@it-is-awesome.de","[restricted]",System.currentTimeMillis(), System.currentTimeMillis()+aYear, false, KeyInfo.STATUS_VALID, false));
//		user.addKey(new KeyInfo("33:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11@it-is-awesome.de", "33:11...11:11", "SUB", "BOTH","boedeker@it-is-awesome.de","[restricted]",System.currentTimeMillis(), System.currentTimeMillis()+aYear, false, KeyInfo.STATUS_REVOKED, false));
//		user.addKey(new KeyInfo("44:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11@it-is-awesome.de", "44:11...11:11", "SUB", "BOTH","boedeker@it-is-awesome.de","[restricted]",System.currentTimeMillis(), System.currentTimeMillis()+aYear, false, KeyInfo.STATUS_UNAPPROVED, false));
//		user.addKey(new KeyInfo("55:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11@it-is-awesome.de", "55:11...11:11", "MASTER", "BOTH","boedeker@it-is-awesome.de","[restricted]",System.currentTimeMillis(), System.currentTimeMillis()+aYear, false, KeyInfo.STATUS_OUTDATED, false));
//		user.addKey(new KeyInfo("66:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11@it-is-awesome.de", "66:11...11:11", "SUB", "BOTH","boedeker@it-is-awesome.de","[restricted]",System.currentTimeMillis(), System.currentTimeMillis()+aYear, false, KeyInfo.STATUS_VALID, false));
//		user.addKey(new KeyInfo("77:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11@it-is-awesome.de", "77:11...11:11", "MASTER", "BOTH","boedeker@it-is-awesome.de","[restricted]",System.currentTimeMillis(), System.currentTimeMillis()+aYear, false, KeyInfo.STATUS_VALID, false));
//		user.addKey(new KeyInfo("88:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11@it-is-awesome.de", "88:11...11:11", "MASTER", "BOTH","boedeker@it-is-awesome.de","[restricted]",System.currentTimeMillis(), System.currentTimeMillis()+aYear, false, KeyInfo.STATUS_VALID, false));
//		user.addKey(new KeyInfo("99:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11@it-is-awesome.de", "99:11...11:11", "MASTER", "BOTH","boedeker@it-is-awesome.de","[restricted]",System.currentTimeMillis(), System.currentTimeMillis()+aYear, false, KeyInfo.STATUS_VALID, false));
//		
//		
//		user.addConnection(new KeyConnection(user.getKeys().get(1).getId(), user.getKeys().get(1).getId(), KeyConnection.TYPE_APPROVAL, System.currentTimeMillis()));
//		user.addConnection(new KeyConnection(user.getKeys().get(1).getId(), user.getKeys().get(0).getId(), KeyConnection.TYPE_SUBKEY, System.currentTimeMillis()));		
//		user.addConnection(new KeyConnection(user.getKeys().get(2).getId(), user.getKeys().get(0).getId(), KeyConnection.TYPE_REVOKEKEY, System.currentTimeMillis()));		
//		user.addConnection(new KeyConnection(user.getKeys().get(2).getId(), user.getKeys().get(0).getId(), KeyConnection.TYPE_APPROVAL, System.currentTimeMillis()));		
//		user.addConnection(new KeyConnection(user.getKeys().get(2).getId(), user.getKeys().get(0).getId(), KeyConnection.TYPE_DISAPPROVAL, System.currentTimeMillis()));		
//		
//		
//		user.addConnection(new KeyConnection(user.getKeys().get(0).getId(), user.getKeys().get(1).getId(), KeyConnection.TYPE_APPROVAL, System.currentTimeMillis()));
//		user.addConnection(new KeyConnection(user.getKeys().get(1).getId(), user.getKeys().get(2).getId(), KeyConnection.TYPE_DISAPPROVAL, System.currentTimeMillis()));
//		user.addConnection(new KeyConnection(user.getKeys().get(1).getId(), user.getKeys().get(3).getId(), KeyConnection.TYPE_REVOCATION, System.currentTimeMillis()));
//		user.addConnection(new KeyConnection(user.getKeys().get(3).getId(), user.getKeys().get(1).getId(), KeyConnection.TYPE_APPROVAL_PENDING, System.currentTimeMillis()));
//		user.addConnection(new KeyConnection(user.getKeys().get(4).getId(), user.getKeys().get(0).getId(), KeyConnection.TYPE_APPROVAL, System.currentTimeMillis()));
//		user.addConnection(new KeyConnection(user.getKeys().get(5).getId(), user.getKeys().get(8).getId(), KeyConnection.TYPE_APPROVAL, System.currentTimeMillis()));
//		user.addConnection(new KeyConnection(user.getKeys().get(8).getId(), user.getKeys().get(7).getId(), KeyConnection.TYPE_APPROVAL, System.currentTimeMillis()));
//		user.addConnection(new KeyConnection(user.getKeys().get(8).getId(), user.getKeys().get(7).getId(), KeyConnection.TYPE_APPROVAL, System.currentTimeMillis()));
//		user.addConnection(new KeyConnection(user.getKeys().get(8).getId(), user.getKeys().get(7).getId(), KeyConnection.TYPE_APPROVAL, System.currentTimeMillis()));
//		user.addConnection(new KeyConnection(user.getKeys().get(9).getId(), user.getKeys().get(7).getId(), KeyConnection.TYPE_APPROVAL, System.currentTimeMillis()));
//		user.addConnection(new KeyConnection(user.getKeys().get(6).getId(), user.getKeys().get(5).getId(), KeyConnection.TYPE_APPROVAL, System.currentTimeMillis()));
//		
		return user;
	}
	
	
	/**
	 * Gets an updated lists of KeyInfo and KeyConncetions which
	 * contains all KeyInfo from given keyids and connected keylogs
	 * and all KeyLogs.
	 * Already present KeyInfos / Logs will not be included.
	 * If username does not equal anonymous the users settings will be saved to the db
	 * 
	 * @param username : "anonymous" or email address
	 * @param states : list of {keyid, x ,y , showIn, showOut}
	 * @param keyids : list of new nodes to add
	 * @param inLogs : add keylogs and keys for incoming logs for each keyid from keyids
	 * @param outLogs : add keylogs and keys for outgoing logs for each keyid from keyids
	 * @return
	 */
	public User updateKeyInfoAndLogs(String username, Vector<NodeState> states, Vector<String> keyids, boolean inLogs, boolean outLogs) { 
		if (DBControl.getInstance()==null) {
			long aYear = 1000L*3600L*24L*356L;
			User user = new User(username);
			user.addKey(new KeyInfo("12:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11@it-is-awesome.de", "12:11...11:11", "SUB", "BOTH","boedeker@it-is-awesome.de","[restricted]",System.currentTimeMillis(), System.currentTimeMillis()+aYear, false, KeyInfo.STATUS_VALID, false));			
			return user; 
		}
		return DBControl.getInstance().updateKeyInfoAndLogs(username, states, keyids, inLogs, outLogs);
	}
	

}
