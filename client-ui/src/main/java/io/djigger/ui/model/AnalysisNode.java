/*******************************************************************************
 * (C) Copyright 2016 Jérôme Comte and Dorian Cransac
 *  
 *  This file is part of djigger
 *  
 *  djigger is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  djigger is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *  
 *  You should have received a copy of the GNU Affero General Public License
 *  along with djigger.  If not, see <http://www.gnu.org/licenses/>.
 *
 *******************************************************************************/
package io.djigger.ui.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class AnalysisNode implements Comparable<AnalysisNode> {

	private final NodeID id;

	private final AnalysisNode parent;

	private final List<AnalysisNode> children = new ArrayList<AnalysisNode>();

	private final List<RealNodeAggregation> aggregations = new ArrayList<RealNodeAggregation>(1);

	public AnalysisNode(AnalysisNode parent, NodeID id) {
		super();
		this.id = id;
		this.parent = parent;
	}

	public AnalysisNode() {
		this.id = null;
		this.parent = null;
	}

	public void sort() {
		for(AnalysisNode child:getChildren()) {
			child.sort();
		}
		Collections.sort(children);
	}
	
	public AnalysisNode getChildByID(NodeID id) {
		for(AnalysisNode child:children) {
			if(id.equals(child.getId())) {
				return child;
			}
		}
		return null;
	}

	public List<AnalysisNode> getChildren() {
		return children;
	}

	public List<RealNodeAggregation> getAggregations() {
		return aggregations;
	}
	public int getWeight() {
		int weight = 0;
		for(RealNodeAggregation aggregation:aggregations) {
			weight += aggregation.getAggregation().getSamples().size();
		}
		return weight;
	}

	public int getOwnWeight() {
		int childWeightCount = 0;
		for(AnalysisNode child:children) {
			childWeightCount += child.getWeight();
		}
		return getWeight() - childWeightCount;
	}

	@Override
	public int compareTo(AnalysisNode o) {
		if(getWeight()>o.getWeight()) {
			return -1;
		} else if (getWeight()==o.getWeight()) {
			return 0;
		} else {
			return 1;
		}	
	}

	public NodeID getId() {
		return id;
	}

	// TODO: this is not correct. A node can have more than one path due to the node filtering
	public RealNodePath getRealNodePath() {
		if(aggregations.size()>0 && parent!=null) {
			return aggregations.get(0).getRealNode().getPath();
		} else {
			return null;
		}
	}

	public boolean isLeaf() {
		return children.size() == 0;
	}

	public AnalysisNode getRoot() {
		if(parent == null) {
			return this;
		} else {
			return parent.getRoot();
		}
	}

	public AnalysisNodePath getPath() {
		AnalysisNodePath path = new AnalysisNodePath();
		AnalysisNode currentNode = this;
		while(currentNode!=null) {
			if(currentNode.id!=null) {
				path.add(0,currentNode.id);
			}
			currentNode = currentNode.parent;
		}
		return path;
	}
	
	public List<AnalysisNode> getTreePath() {
		ArrayList<AnalysisNode> path = new ArrayList<AnalysisNode>();
		AnalysisNode currentNode = this;
		while(currentNode!=null) {
			path.add(0,currentNode);
			currentNode = currentNode.parent;
		}
		return path;
	}

	// TODO: see if this method really belongs to this class.
	public AnalysisNode find(AnalysisNodePath path) {
		if(path!=null && path.size()>0) {
			LinkedList<NodeID> stack = new LinkedList<NodeID>();
			stack.addAll(path);
			return findInChildren(stack);
		} else {
			return null;
		}
	}

	// TODO: does such a method really belongs to this class?
	private AnalysisNode findInChildren(LinkedList<NodeID> branch) {
		NodeID currentID = branch.pop();
		try {
			AnalysisNode node = getChildByID(currentID);
			if(branch.size()>0) {
				if(node == null) {
					return null;
				} else {
					return node.findInChildren(branch);
				}
			} else {
				return node;
			}
		} finally {
			branch.push(currentID);
		}
	}

	@Override
	public String toString() {
		if(id!=null) {
			return id.toString();			
		} else {
			return super.toString();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AnalysisNode other = (AnalysisNode) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
