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

import java.util.HashMap;
import java.util.Vector;

public class TrustGraph {
	
	public Vector<TrustGraphNode> nodes = new Vector<TrustGraphNode>();
	public Vector<TrustGraphEdge> edges = new Vector<TrustGraphEdge>();
	public HashMap<String, TrustRatingOfKey> directRating = new HashMap<String, TrustRatingOfKey>(); 
	
	public void addKeyRating(OSDXKey key, int trustRating) {
		addNode(key);
		directRating.put(key.getKeyID(), new TrustRatingOfKey(key.getKeyID(), trustRating));
	}
	
	public boolean isDirectlyTrusted(String keyid) {
		TrustRatingOfKey tr = directRating.get(keyid);
		if (tr!=null && tr.getTrustRating()>= TrustRatingOfKey.RATING_MARGINAL) {
			return true;
		}
		return false;
	}
	
	public boolean isDirectlyRated(String keyid) {
		TrustRatingOfKey tr = directRating.get(keyid);
		if (tr!=null) {
			return true;
		}
		return false;
	}
	
	private void updateAllTrustRatings() {
		//System.out.println("update trust ratings: ");
		Vector<TrustGraphNode> queue = new Vector<TrustGraphNode>();
		for (TrustGraphNode n : nodes) {
			n.resetTrustRating();
		}
		for (TrustRatingOfKey tr : directRating.values()) {
			int rating = tr.getTrustRating();
			TrustGraphNode n = getNode(tr.getKeyID());
			if (n!=null) {
				n.setTrustRating(rating, true);
				queue.add(n);
				//System.out.println("direct trust: "+rating+" of "+tr.getKeyID());
			}
		}
		while (queue.size()>0) {
			TrustGraphNode v = queue.remove(0);
			for (TrustGraphNode w : getKnownChilderen(v)) {
				if (w.getTrustRating()==-999) {
					w.setTrustRating(v.getTrustRating(), false);
					//System.out.println("indirect trust: "+v.getTrustRating()+" of "+w.getID());
					queue.add(w);
				}
			}
		}
	}

	public Vector<TrustGraphNode> getKnownChilderen(TrustGraphNode v) {
		Vector<TrustGraphNode> children = new Vector<TrustGraphNode>();
		for (TrustGraphEdge w : edges) {
			if (w.from == v) children.add(w.to);
		}
		return children;
	}

	
	public int getTrustRating(String keyid) {
		TrustGraphNode n = getNode(keyid);
		if (n!=null) {
			int rating = n.getTrustRating();
			if (rating==-999) {
				updateAllTrustRatings();
			}
			return n.getTrustRating();
		}
		return TrustRatingOfKey.RATING_UNKNOWN;
	}
	
	public TrustGraphNode addNode(OSDXKey key) {
		String id = key.getKeyID();
		for (TrustGraphNode n : nodes) {
			if (n.getID().equals(id)) {
				return n;
			}
		}
		TrustGraphNode node = new TrustGraphNode(id,this);
		node.setKey(key);
		nodes.add(node);
		return node;
	}
	
	public TrustGraphNode getNode(String keyid) {
		for (TrustGraphNode n : nodes) {
			if (n.getID().equals(keyid)) {
				return n;
			}
		}
		return null;
	}
	public void addEdge(TrustGraphNode from, TrustGraphNode to, int type) {
		edges.add(new TrustGraphEdge(from,to,type));
	}
	
	
	/**
	 * implements a breadth-first-search for a trusted Node
	 * @param start Node to start searching from
	 * @param maxDepth maximum distance from start node
	 * @returns a trusted node or null if no trusted node could be found within the given maxDetph
	 */
	public TrustGraphNode breath_first_search_to_trusted(TrustGraphNode start, int maxDepth) {
		return breadth_first_search_to_trusted(start, maxDepth, true);
	}
	
	/**
	 * implements a breadth-first-traversal up to a given maxDepth 
	 * @param start node to start traversal from
	 * @param maxDepth maximum distance from start node
	 */
	public void breadth_first_search(TrustGraphNode start, int maxDepth) {
		breadth_first_search_to_trusted(start, maxDepth, false);
	}
	
	private TrustGraphNode breadth_first_search_to_trusted(TrustGraphNode start, int maxDepth, boolean stopAtTrusted) {
		if (start.isTrusted()) return start;
		Vector<TrustGraphNode> queue = new Vector<TrustGraphNode>();
		start.setVisited(true);
		start.setDepth(0);
		queue.add(start);
		while (queue.size()>0) {
			TrustGraphNode v = queue.remove(0);
			if (v.getDepth()<=maxDepth) {
				int depth = v.getDepth()+1;
				for (TrustGraphNode w : v.getChildren()) {
					if (!w.isVisited()) {
						w.setVisited(true);
						w.addParent(v);
						w.setDepth(depth);
						if (stopAtTrusted && w.isTrusted()) return w;
						queue.add(w);
					}
				}
			}
		}
		return null;
	}
	
	public void expandNode(TrustGraphNode v) {
		for (TrustGraphNode w : v.getChildren()) {
			if (!w.isVisited()) {
				w.setVisited(true);
				w.addParent(v);
			}
		}
	}
	
}
