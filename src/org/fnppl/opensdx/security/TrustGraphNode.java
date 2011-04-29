package org.fnppl.opensdx.security;
/*
 * Copyright (C) 2010-2011 
 * 							fine people e.V. <opensdx@fnppl.org> 
 * 							Henning Thieß <ht@fnppl.org>
 * 
 * 							http://fnppl.org
 */

/*
 * Software license
 *
 * As far as this file or parts of this file is/are software, rather than documentation, this software-license applies / shall be applied.
 *  
 * This file is part of openSDX
 * openSDX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * openSDX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * and GNU General Public License along with openSDX.
 * If not, see <http://www.gnu.org/licenses/>.
 *      
 */

/*
 * Documentation license
 * 
 * As far as this file or parts of this file is/are documentation, rather than software, this documentation-license applies / shall be applied.
 * 
 * This file is part of openSDX.
 * Permission is granted to copy, distribute and/or modify this document 
 * under the terms of the GNU Free Documentation License, Version 1.3 
 * or any later version published by the Free Software Foundation; 
 * with no Invariant Sections, no Front-Cover Texts, and no Back-Cover Texts. 
 * A copy of the license is included in the section entitled "GNU 
 * Free Documentation License" resp. in the file called "FDL.txt".
 * 
 */

import java.util.Vector;

public class TrustGraphNode {
	private String id = null;
	private OSDXKey key =  null;
	private Vector<TrustGraphNode> children = null;
	private Vector<TrustGraphNode> parents = new Vector<TrustGraphNode>();
	private int trusted = -1;
	private boolean visited = false;
	private int depth = 0;
	private TrustGraph g;
	private int trustRating = -999;
	private boolean directTrust = false;
	
	public TrustGraphNode(String id, TrustGraph graph) {
		this.id = id;
		this.g = graph;
	}
	
	public Vector<TrustGraphNode> reloadChildren() {
		children = null;
		return getChildren();
	}
	
	public Vector<TrustGraphNode> getChildren() {
		if (children == null) {
			children = new Vector<TrustGraphNode>();
			//check keylogs for keys that trust this key
			OSDXKey key = getKey();
			System.out.println("requesting keylogs for: "+key.getKeyID()+ "  depth = "+depth);
			// request keylogs includes verification of keyserver-key,
			// when keyserver key is not trusted, no keylogs are found !!! 
			Vector<KeyLog> keylogs = KeyVerificator.requestKeyLogs(key); 
			if (key.isSub()) {
				//sub keys can only have revocation keylogs
				boolean hasRevokeLog = false;
				for (KeyLog keylog : keylogs) {
					try {
						String action = keylog.getAction();
						if (action.equals(KeyLog.REVOCATION)) {
							if (!KeyVerificator.isNotTrustedKey(keylog.getKeyIDFrom())) {
								hasRevokeLog = true;
								System.out.println("found revocation for subkey: "+key.getKeyID());
								TrustGraphNode n = g.addNode(keylog.getActionSignatureKey());
								g.addEdge(n, this, TrustGraphEdge.TYPE_REVOKE, keylog.getDate());
							}
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				if (!hasRevokeLog) {
					//next step: verify parent-key
					MasterKey parent = KeyVerificator.requestParentKey((SubKey)key);
					if (parent!=null) {
						((SubKey)key).setParentKey(parent);
						TrustGraphNode n = g.addNode(parent);
						children.add(n);
						g.addEdge(this, n, TrustGraphEdge.TYPE_SUBKEY, -1L);
					}
				}
			} else {
				if (keylogs!=null) {
					boolean hasRevokeLog = false;
					boolean hasDisapproveLog = false;
					for (KeyLog keylog : keylogs) {
						try {
							//System.out.println("  found verified keylog from "+keylog.getKeyIDFrom());
							String action = keylog.getAction();
							if (action.equals(KeyLog.REVOCATION)) {
								if (!KeyVerificator.isNotTrustedKey(keylog.getKeyIDFrom())) {
									hasRevokeLog = true;
									TrustGraphNode n = g.addNode(keylog.getActionSignatureKey());
									g.addEdge(n, this, TrustGraphEdge.TYPE_REVOKE, keylog.getDate());
								}
							}
							else if (action.equals(KeyLog.DISAPPROVAL)) { 
								if (!KeyVerificator.isNotTrustedKey(keylog.getKeyIDFrom())) {
									hasDisapproveLog = true;
									TrustGraphNode n = g.addNode(keylog.getActionSignatureKey());
									g.addEdge(n, this, TrustGraphEdge.TYPE_DISAPPROVE, keylog.getDate());
								}
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					if (!hasRevokeLog || !hasDisapproveLog) {
						//add all approvals as new nodes
						for (KeyLog keylog : keylogs) {
							try {
								if (!KeyVerificator.isNotTrustedKey(keylog.getKeyIDFrom())) {
									//System.out.println("  found verified keylog from "+keylog.getKeyIDFrom());
									String action = keylog.getAction();
									if (action.equals(KeyLog.APPROVAL)) { 
										TrustGraphNode n = g.addNode(keylog.getActionSignatureKey());
										children.add(n);
										//check ob nachher disapproval -> dann edge raus
										//check ob nachher revoke -> 
										
										g.addEdge(n, this, TrustGraphEdge.TYPE_APPROVE, keylog.getDate());
									}
								}
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					} else {
						//do i trust disapprove or revoke signing key?
						//yes, i do, when not explicitly in not-trusted-keys
						//-> consequently my search for a chain of trust ends for this path
						
					}
				}
			}
			
		}
		return children;
	}
	
	public void addParent(TrustGraphNode n) {
		parents.add(n);
	}
	public Vector<TrustGraphNode> getParents() {
		return parents;
	}
	
	public OSDXKey getKey() {
		return key;
	}
	public void setKey(OSDXKey key) {
		this.key = key;
	}
	public void setTrusted(boolean trusted) {
		this.trusted = (trusted?1:0);
	}
	
	public boolean isTrusted() {
		if  (trusted<0) {
			if (KeyVerificator.isTrustedKey(id)) {
				trusted = 1;
			} else {
				trusted = 0;
			}
		}
		return (trusted==1?true:false);
	}
	
	
	public int getTrustRating() {
		return trustRating;
	}

	public boolean isDirectTrust() {
		return directTrust;
	}


	public void setTrustRating(int trustRating, boolean directTrust) {
		this.trustRating = trustRating;
		this.directTrust = directTrust;
	}
	
	public void resetTrustRating() {
		trustRating = -999;
		directTrust = false;
	}
	
	public void setVisited(boolean visited) {
		this.visited = visited;
	}
	public boolean isVisited() {
		return visited;
	}
	public void setDepth(int depth) {
		this.depth = depth;
	}
	public int getDepth() {
		return depth;
	}
	
	public String getID() {
		return id;
	}
	
	public TrustGraph getGraph() {
		return g;
	}
}
