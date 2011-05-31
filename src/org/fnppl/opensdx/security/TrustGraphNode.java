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
	
	
	private String keyid = null;
	private int level = 0;
	private long valid_from = Long.MIN_VALUE;
	private long valid_to = Long.MAX_VALUE;
	
	private Vector<TrustGraphEdge> keylogsFrom;
	private TrustGraph graph = null;
	
	public TrustGraphNode(String keyid, int level, long valid_from, long valid_to, TrustGraph graph) {
		this.keyid = keyid;
		this.level = level;
		this.valid_from = valid_from;
		this.valid_to = valid_to;
		this.graph = graph;
		keylogsFrom = new Vector<TrustGraphEdge>();
	}
	
	public void addKeyLogFrom(String keyid_from, int type, long datetime) {
		keylogsFrom.add(new TrustGraphEdge(keyid_from, keyid, type, datetime));
	}

	public String getKeyid() {
		return keyid;
	}

	public void setKeyid(String keyid) {
		this.keyid = keyid;
	}

	public Vector<TrustGraphEdge> getKeylogsFrom() {
		return keylogsFrom;
	}

	public void setKeylogsFrom(Vector<TrustGraphEdge> keylogsFrom) {
		this.keylogsFrom = keylogsFrom;
	}

	public TrustGraph getGraph() {
		return graph;
	}

	public void setGraph(TrustGraph graph) {
		this.graph = graph;
	}
	
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public long getValid_from() {
		return valid_from;
	}

	public void setValid_from(long validFrom) {
		valid_from = validFrom;
	}

	public long getValid_to() {
		return valid_to;
	}

	public void setValid_to(long validTo) {
		valid_to = validTo;
	}

	public void addKeylogsFrom(Vector<TrustGraphEdge> keylogsFrom) {
		for (TrustGraphEdge v : keylogsFrom) {
			boolean found = false;
			for (TrustGraphEdge w : this.keylogsFrom) {
				if (v.datetime == w.datetime && v.keyid_from.equals(w.keyid_from) && v.type == w.type && v.keyid_to.equals(w.keyid_to)) {
					found = true;
					break;
				}
			}
			if (!found) {
				this.keylogsFrom.add(v);
			}
		}
	}
	
	
	
	
//	private String id = null;
//	private OSDXKey key =  null;
//	
//	private Vector<TrustGraphEdge> trusts;
//	private Vector<TrustGraphEdge> isTrustedBy;
//	
//	private int trusted = -1;
//	private boolean visited = false;
//	private int depth = 0;
//	private TrustGraph g;
//	private int trustRating = -999;
//	private boolean directTrust = false;
//	
//	public TrustGraphNode(String id, TrustGraph graph) {
//		this.id = id;
//		this.g = graph;
//	}
//	
//	public Vector<TrustGraphEdge> reloadIsTrustedBy(KeyVerificator keyverificator) {
//		isTrustedBy = null;
//		return getIsTrustedBy(keyverificator);
//	}
//	
//	public Vector<TrustGraphEdge> getIsTrustedBy(KeyVerificator keyverificator) {
//		if (isTrustedBy == null) {
//			isTrustedBy = new Vector<TrustGraphEdge>();
//			//check keylogs for keys that trust this key
//			OSDXKey key = getKey();
//			System.out.println("requesting keylogs for: "+key.getKeyID()+ "  depth = "+depth);
//			// request keylogs includes verification of keyserver-key,
//			// when keyserver key is not trusted, no keylogs are found !!! 
//			Vector<KeyLog> keylogs = keyverificator.requestKeyLogs(key); 
//			
//			for (KeyLog keylog : keylogs) {
//				try {
//					if (!keyverificator.isNotTrustedKey(keylog.getKeyIDFrom())) {
//						String action = keylog.getAction();
//						if (action.equals(KeyLogAction.APPROVAL)) {
//							TrustGraphNode n = g.addOrGetNode(keylog.getActionSignatureKey());
//							g.addEdge(n, this, TrustGraphEdge.TYPE_APPROVE, keylog.getActionDatetime());
//						}
//						else if (action.equals(KeyLogAction.REVOCATION)) {
//							TrustGraphNode n = g.addOrGetNode(keylog.getActionSignatureKey());
//							g.addEdge(n, this, TrustGraphEdge.TYPE_REVOKE, keylog.getActionDatetime());
//						}
//						else if (action.equals(KeyLogAction.DISAPPROVAL)) {
//							TrustGraphNode n = g.addOrGetNode(keylog.getActionSignatureKey());
//							g.addEdge(n, this, TrustGraphEdge.TYPE_DISAPPROVE, keylog.getActionDatetime());
//						}
//					}
//				} catch (Exception ex) {
//					ex.printStackTrace();
//				}
//			}	
//		}
//		return isTrustedBy;
//	}
//	
////	public Vector<TrustGraphNode> getIsTrustedBy(KeyVerificator keyverificator) {
////		if (isTrustedBy == null) {
////			isTrustedBy = new Vector<TrustGraphEdge>();
////			//check keylogs for keys that trust this key
////			OSDXKey key = getKey();
////			System.out.println("requesting keylogs for: "+key.getKeyID()+ "  depth = "+depth);
////			// request keylogs includes verification of keyserver-key,
////			// when keyserver key is not trusted, no keylogs are found !!! 
////			Vector<KeyLog> keylogs = keyverificator.requestKeyLogs(key); 
////			if (key.isSub()) {
////				//sub keys can only have revocation keylogs
////				boolean hasRevokeLog = false;
////				for (KeyLog keylog : keylogs) {
////					try {
////						String action = keylog.getAction();
////						if (action.equals(KeyLogAction.REVOCATION)) {
////							if (!keyverificator.isNotTrustedKey(keylog.getKeyIDFrom())) {
////								hasRevokeLog = true;
////								System.out.println("found revocation for subkey: "+key.getKeyID());
////								TrustGraphNode n = g.addNode(keylog.getActionSignatureKey());
////								g.addEdge(n, this, TrustGraphEdge.TYPE_REVOKE, keylog.getActionDatetime());
////							}
////						}
////					} catch (Exception ex) {
////						ex.printStackTrace();
////					}
////				}
////				if (!hasRevokeLog) {
////					//next step: verify parent-key
////					MasterKey parent = keyverificator.requestParentKey((SubKey)key);
////					if (parent!=null) {
////						((SubKey)key).setParentKey(parent);
////						TrustGraphNode n = g.addNode(parent);
////						children.add(n);
////						g.addEdge(this, n, TrustGraphEdge.TYPE_SUBKEY, -1L);
////					}
////				}
////			} else {
////				if (keylogs!=null) {
////					//run throug all keylogs
////					// if approval: add to pre-children
////					// if disapproval: remove from pre-Child-Keylogs, if no prior approval -> ignore
////					// if revocation: remove from pre-Child-Keylogs
////					// at the end -> add all pre-ChildKeylogs to children
////					Vector<KeyLog> preChildKeylogs = new Vector<KeyLog>();
////					SecurityHelper.sortKeyLogsbyDate(keylogs);
////					for (KeyLog keylog : keylogs) {
////						try {
////							System.out.println("  found verified keylog from "+keylog.getKeyIDFrom()+" from date: "+keylog.getActionDatetime());
////							if (!keyverificator.isNotTrustedKey(keylog.getKeyIDFrom())) {
////								String action = keylog.getAction();
////								if (action.equals(KeyLogAction.APPROVAL)) {
////									//only the newest approval should be a child
////									OSDXKey fromKey = keylog.getActionSignatureKey();
////									String fromKeyID = fromKey.getKeyID();
////									KeyLog found = null;
////									for (KeyLog child : preChildKeylogs) {
////										if (child.getKeyIDFrom().equals(fromKeyID)) {
////											found = child;
////											break;
////										}
////									}
////									if (found!=null) {
////										preChildKeylogs.remove(found);
////									}
////									preChildKeylogs.add(keylog);
////								}
////								else if (action.equals(KeyLogAction.REVOCATION)) {
////									TrustGraphNode n = g.addNode(keylog.getActionSignatureKey());
////									g.addEdge(n, this, TrustGraphEdge.TYPE_REVOKE, keylog.getActionDatetime());
////								}
////								else if (action.equals(KeyLogAction.DISAPPROVAL)) {
////									//check if key is in preChildKeyLogs
////									OSDXKey fromKey = keylog.getActionSignatureKey();
////									String fromKeyID = fromKey.getKeyID();
////									KeyLog found = null;
////									for (KeyLog child : preChildKeylogs) {
////										if (child.getKeyIDFrom().equals(fromKeyID)) {
////											found = child;
////											break;
////										}
////									}
////									if (found!=null) {
////										preChildKeylogs.remove(found);
////										TrustGraphNode n = g.addNode(keylog.getActionSignatureKey());
////										g.addEdge(n, this, TrustGraphEdge.TYPE_DISAPPROVE, keylog.getActionDatetime());
////									}
////								}
////							}
////						} catch (Exception ex) {
////							ex.printStackTrace();
////						}
////					}
////					for (KeyLog child : preChildKeylogs) {
////						try {
////							TrustGraphNode n = g.addNode(child.getActionSignatureKey());
////							g.addEdge(n, this, TrustGraphEdge.TYPE_APPROVE, child.getActionDatetime());
////						} catch (Exception ex) {
////							ex.printStackTrace();
////						}
////					}
////					
////				}
////			}
////			
////		}
////		return children;
////	}
//	
//	public void addTrusts(TrustGraphEdge n) {
//		if (trusts==null) trusts = new Vector<TrustGraphEdge>();
//		trusts.add(n);
//	}
//	
//	public Vector<TrustGraphEdge> getTrusts() {
//		return trusts;
//	}
//	
//	public OSDXKey getKey() {
//		return key;
//	}
//	public void setKey(OSDXKey key) {
//		this.key = key;
//	}
//	public void setTrusted(boolean trusted) {
//		this.trusted = (trusted?1:0);
//	}
//	
//	public boolean isTrusted(KeyVerificator keyverificator) {
//		if  (trusted<0) {
//			if (keyverificator.isTrustedKey(id)) {
//				trusted = 1;
//			} else {
//				trusted = 0;
//			}
//		}
//		return (trusted==1?true:false);
//	}
//	
//	
//	public int getTrustRating() {
//		return trustRating;
//	}
//
//	public boolean isDirectTrust() {
//		return directTrust;
//	}
//
//
//	public void setTrustRating(int trustRating, boolean directTrust) {
//		this.trustRating = trustRating;
//		this.directTrust = directTrust;
//	}
//	
//	public void resetTrustRating() {
//		trustRating = -999;
//		directTrust = false;
//	}
//	
//	public void setVisited(boolean visited) {
//		this.visited = visited;
//	}
//	public boolean isVisited() {
//		return visited;
//	}
//	public void setDepth(int depth) {
//		this.depth = depth;
//	}
//	public int getDepth() {
//		return depth;
//	}
//	
//	public String getID() {
//		return id;
//	}
//	
//	public TrustGraph getGraph() {
//		return g;
//	}
}
