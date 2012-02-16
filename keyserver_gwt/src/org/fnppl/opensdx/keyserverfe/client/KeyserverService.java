package org.fnppl.opensdx.keyserverfe.client;

import org.fnppl.opensdx.keyserverfe.shared.NodeState;
import org.fnppl.opensdx.keyserverfe.shared.User;
import java.util.Vector;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("service")
public interface KeyserverService extends RemoteService {
	
	User loginAnonymous(String keyid) throws IllegalArgumentException;
	User login(String username, String password) throws IllegalArgumentException;
	User updateKeyInfoAndLogs(String username, Vector<NodeState> states, Vector<String> keyids, boolean inLogs, boolean outLogs) throws IllegalArgumentException;
	
}

