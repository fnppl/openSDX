package org.fnppl.opensdx.security;
/*
 * Copyright (C) 2010-2012 
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

import java.util.HashMap;
import java.util.Vector;

public class TrustGraph {
	
	private HashMap<String, TrustGraphNode> nodes = new HashMap<String, TrustGraphNode>();
	
	public TrustGraph() {
		
	}
	
	public TrustGraphNode addOrGetNode(String keyid, int level, long valid_from, long valid_to) {
		TrustGraphNode node = nodes.get(keyid);
		if (node == null) {
			node = new TrustGraphNode(keyid, level, valid_from, valid_to, this);
			nodes.put(keyid, node);
		}
		return node;
	}
	
	public TrustGraphNode addOrGetNode(TrustGraphNode node) {
		if (nodes.containsKey(node.getKeyid())) {
			TrustGraphNode nodeIn = nodes.get(node.getKeyid());
			nodeIn.addKeylogsFrom(node.getKeylogsFrom());
			return nodeIn;
		} else {
			node.setGraph(this);
			nodes.put(node.getKeyid(), node);
			return node;
		}
	}
	
	public TrustGraphNode getNode(String keyid) {
		return nodes.get(keyid);
	}
	
	public void addFromKeyStore(KeyApprovingStore store) {
		//adding keys with subkey edges
		Vector<OSDXKey> keys = store.getAllKeys();
		for (OSDXKey key : keys) {
			TrustGraphNode node = addOrGetNode(key.getKeyID(), key.getLevel(), key.validFrom, key.validUntil);
			//add parentkey if key is subkey
			if (key.isSub()) {
				MasterKey master = ((SubKey)key).getParentKey();
				if (master!=null) {
					TrustGraphNode n = addOrGetNode(master.getKeyID(), master.getLevel(), master.validFrom, master.validUntil);
					node.addKeyLogFrom(master.getKeyID(), TrustGraphEdge.TYPE_SUBKEY, Long.MIN_VALUE);	
				}
			}
		}
		//adding keylogs
		Vector<KeyLog> keylogs = store.getKeyLogs();
		for (KeyLog keylog : keylogs) {
			try {
				TrustGraphNode node = getNode(keylog.getKeyIDTo());
				if (node!=null) {
					String action = keylog.getAction();
					if (action.equals(KeyLogAction.APPROVAL)) {
						OSDXKey sigKey = keylog.getActionSignatureKey();
						TrustGraphNode n = addOrGetNode(sigKey.getKeyID(), sigKey.getLevel(), sigKey.validFrom, sigKey.validUntil);
						node.addKeyLogFrom(keylog.getKeyIDFrom(), TrustGraphEdge.TYPE_APPROVE, keylog.getActionDatetime());
					}
					else if (action.equals(KeyLogAction.REVOCATION)) {
						OSDXKey sigKey = keylog.getActionSignatureKey();
						TrustGraphNode n = addOrGetNode(sigKey.getKeyID(), sigKey.getLevel(), sigKey.validFrom, sigKey.validUntil);
						node.addKeyLogFrom(keylog.getKeyIDFrom(), TrustGraphEdge.TYPE_REVOKE, keylog.getActionDatetime());
					}
					else if (action.equals(KeyLogAction.DISAPPROVAL)) {
						OSDXKey sigKey = keylog.getActionSignatureKey();
						TrustGraphNode n = addOrGetNode(sigKey.getKeyID(), sigKey.getLevel(), sigKey.validFrom, sigKey.validUntil);
						node.addKeyLogFrom(keylog.getKeyIDFrom(), TrustGraphEdge.TYPE_DISAPPROVE, keylog.getActionDatetime());
					}
				}
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public void addEdgesForKey(OSDXKey key, KeyVerificator keyverificator) {
		TrustGraphNode node = addOrGetNode(key.getKeyID(), key.getLevel(), key.validFrom, key.validUntil);
		
		//add parentkey if key is subkey
		if (key.isSub()) {
			MasterKey master = ((SubKey)key).getParentKey();
			if (master == null) {
				//request at server
				master = keyverificator.requestParentKey((SubKey)key);
			}
			if (master!=null) {
				TrustGraphNode n = addOrGetNode(master.getKeyID(), master.getLevel(), master.validFrom, master.validUntil);
				node.addKeyLogFrom(master.getKeyID(), TrustGraphEdge.TYPE_SUBKEY, Long.MIN_VALUE);	
			}
		}
		
		System.out.println("requesting keylogs for: "+key.getKeyID());
		Vector<KeyLog> logs = keyverificator.requestKeyLogs(key);
		
		// request keylogs includes verification of keyserver-key,
		// when keyserver key is not trusted, no keylogs are found !!! 
		Vector<KeyLog> keylogs = keyverificator.requestKeyLogs(key); 
			
		for (KeyLog keylog : keylogs) {
			try {
				String action = keylog.getAction();
				if (action.equals(KeyLogAction.APPROVAL)) {
					OSDXKey sigKey = keylog.getActionSignatureKey();
					TrustGraphNode n = addOrGetNode(sigKey.getKeyID(), sigKey.getLevel(), sigKey.validFrom, sigKey.validUntil);
					node.addKeyLogFrom(keylog.getKeyIDFrom(), TrustGraphEdge.TYPE_APPROVE, keylog.getActionDatetime());
				}
				else if (action.equals(KeyLogAction.REVOCATION)) {
					OSDXKey sigKey = keylog.getActionSignatureKey();
					TrustGraphNode n = addOrGetNode(sigKey.getKeyID(), sigKey.getLevel(), sigKey.validFrom, sigKey.validUntil);
					node.addKeyLogFrom(keylog.getKeyIDFrom(), TrustGraphEdge.TYPE_REVOKE, keylog.getActionDatetime());
				}
				else if (action.equals(KeyLogAction.DISAPPROVAL)) {
					OSDXKey sigKey = keylog.getActionSignatureKey();
					TrustGraphNode n = addOrGetNode(sigKey.getKeyID(), sigKey.getLevel(), sigKey.validFrom, sigKey.validUntil);
					node.addKeyLogFrom(keylog.getKeyIDFrom(), TrustGraphEdge.TYPE_DISAPPROVE, keylog.getActionDatetime());
				}
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}	
	}
	
	
//	private Vector<TrustGraphNode> nodes = new Vector<TrustGraphNode>();
//	//private Vector<TrustGraphEdge> edges = new Vector<TrustGraphEdge>();
//	private HashMap<String, TrustRatingOfKey> directRating = new HashMap<String, TrustRatingOfKey>(); 
//	
//	public void addKeyRating(OSDXKey key, int trustRating) {
//		addOrGetNode(key);
//		TrustRatingOfKey tr = directRating.get(key.getKeyID());
//		if (tr==null || tr.getTrustRating()!=trustRating) {
//			directRating.put(key.getKeyID(), new TrustRatingOfKey(key.getKeyID(), trustRating));
//			//updateAllTrustRatings();
//		}
//	}
//	public void removeDirectRating(OSDXKey key) {
//		directRating.remove(key.getKeyID());
//	}
//	
//	public void removeAllDirectRatings() {
//		directRating.clear();
//	}
//	
//	
//	public boolean isDirectlyTrusted(String keyid) {
//		TrustRatingOfKey tr = directRating.get(keyid);
//		if (tr!=null && tr.getTrustRating()>= TrustRatingOfKey.RATING_MARGINAL) {
//			return true;
//		}
//		return false;
//	}
//	
//	public boolean isDirectlyRated(String keyid) {
//		TrustRatingOfKey tr = directRating.get(keyid);
//		if (tr!=null) {
//			return true;
//		}
//		return false;
//	}
//	
//	private void updateAllTrustRatings(long datetime) {
//		//System.out.println("update trust ratings: ");
//		Vector<TrustGraphNode> queue = new Vector<TrustGraphNode>();
//		for (TrustGraphNode n : nodes) {
//			n.resetTrustRating();
//		}
//		for (TrustRatingOfKey tr : directRating.values()) {
//			int rating = tr.getTrustRating();
//			TrustGraphNode n = getNode(tr.getKeyID());
//			if (n!=null) {
//				n.setTrustRating(rating, true);
//				queue.add(n);
//				//System.out.println("direct trust: "+rating+" of "+tr.getKeyID());
//			}
//		}
//		while (queue.size()>0) {
//			TrustGraphNode v = queue.remove(0);
//			int rating = v.getTrustRating();
//			if (rating<TrustRatingOfKey.RATING_MARGINAL) rating = TrustRatingOfKey.RATING_UNKNOWN;
//			for (TrustGraphNode w : getKnownChilderen(v, datetime)) {
//				
//				if (w.getTrustRating()==TrustRatingOfKey.RATING_UNKNOWN && rating>=TrustRatingOfKey.RATING_MARGINAL) {
//					w.setTrustRating(rating, false);
//					queue.add(w);
//				}
//				else if (w.getTrustRating()==-999) {
//					w.setTrustRating(rating, false);
//					//System.out.println("indirect trust: "+v.getTrustRating()+" of "+w.getID());
//					queue.add(w);
//				}
//			}
//		}
//	}
//
//	public Vector<TrustGraphNode> getKnownChilderen(TrustGraphNode v, long datetime) {
//		Vector<TrustGraphEdge> isTrustedBy = v.getIsTrustedBy(keyverificator);
//		
//		Vector<TrustGraphNode> children = new Vector<TrustGraphNode>();
//		for (TrustGraphEdge w : edges) {
//			if (w.from == v && w.datetime <= datetime) children.add(w.to);
//		}
//		return children;
//	}
//
//	
//	public int getTrustRating(String keyid, long datetime) {
//		TrustGraphNode n = getNode(keyid);
//		if (n!=null) {
//			updateAllTrustRatings(datetime);
//			return n.getTrustRating();
//		}
//		return TrustRatingOfKey.RATING_UNKNOWN;
//	}
//	
//	public TrustGraphNode addOrGetNode(OSDXKey key) {
//		String id = key.getKeyID();
//		TrustGraphNode node = getNode(id);
//		if (node==null) {
//			node = new TrustGraphNode(id,this);
//			node.setKey(key);
//			nodes.add(node);
//		}
//		return node;
//	}
//	
//	public TrustGraphNode getNode(String keyid) {
//		for (TrustGraphNode n : nodes) {
//			if (n.getID().equals(keyid)) {
//				return n;
//			}
//		}
//		return null;
//	}
//	
//	public void addEdge(TrustGraphNode from, TrustGraphNode to, int type, long datetime) {
//		//check if already present
//		for (TrustGraphEdge e : edges) {
//			if (e.from==from && e.to==to && e.type == type) {
//				return;
//			}
//		}
//		edges.add(new TrustGraphEdge(from,to,type, datetime));
//	}
//	
//	
////	/**
////	 * implements a breadth-first-search for a trusted Node
////	 * @param start Node to start searching from
////	 * @param maxDepth maximum distance from start node
////	 * @returns a trusted node or null if no trusted node could be found within the given maxDetph
////	 */
////	public TrustGraphNode breadth_first_search_to_trusted(TrustGraphNode start, int maxDepth, KeyVerificator keyverificator) {
////		return breadth_first_search_to_trusted(start, maxDepth, true, keyverificator);
////	}
////	
////	/**
////	 * implements a breadth-first-traversal up to a given maxDepth 
////	 * @param start node to start traversal from
////	 * @param maxDepth maximum distance from start node
////	 */
////	public void breadth_first_search(TrustGraphNode start, int maxDepth, KeyVerificator keyverificator) {
////		breadth_first_search_to_trusted(start, maxDepth, false, keyverificator);
////	}
////	
////	private TrustGraphNode breadth_first_search_to_trusted(TrustGraphNode start, int maxDepth, boolean stopAtTrusted, KeyVerificator keyverificator) {
////		if (start.isTrusted(keyverificator)) return start;
////		Vector<TrustGraphNode> queue = new Vector<TrustGraphNode>();
////		start.setVisited(true);
////		start.setDepth(0);
////		queue.add(start);
////		while (queue.size()>0) {
////			TrustGraphNode v = queue.remove(0);
////			if (v.getDepth()<=maxDepth) {
////				int depth = v.getDepth()+1;
////				for (TrustGraphNode w : v.getIsTrustedBy(keyverificator)) {
////					if (!w.isVisited()) {
////						w.setVisited(true);
////						w.addParent(v);
////						w.setDepth(depth);
////						if (stopAtTrusted && w.isTrusted(keyverificator)) return w;
////						queue.add(w);
////					}
////				}
////			}
////		}
////		return null;
////	}
////	
////	public void expandNode(TrustGraphNode v, KeyVerificator keyverificator) {
////		for (TrustGraphNode w : v.reloadIsTrustedBy(keyverificator)) {
////			if (!w.isVisited()) {
////				w.setVisited(true);
////				w.addParent(v);
////			}
////		}
////	}
	
}
