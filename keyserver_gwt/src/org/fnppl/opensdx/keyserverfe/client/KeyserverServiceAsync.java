package org.fnppl.opensdx.keyserverfe.client;

import java.util.Vector;

import org.fnppl.opensdx.keyserverfe.shared.KeyConnection;
import org.fnppl.opensdx.keyserverfe.shared.KeyInfo;
import org.fnppl.opensdx.keyserverfe.shared.NodeState;
import org.fnppl.opensdx.keyserverfe.shared.User;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface KeyserverServiceAsync {

	void login(String username, String password, AsyncCallback<User> callback);
	void loginAnonymous(String keyid, AsyncCallback<User> callback);
	void updateKeyInfoAndLogs(String username, Vector<NodeState> states, Vector<String> keyids, boolean inLogs, boolean outLogs,	AsyncCallback<User> callback);

}
