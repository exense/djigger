/*******************************************************************************
 * (C) Copyright  2016 Jérôme Comte and others.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Contributors:
 *    - Jérôme Comte
 *******************************************************************************/

package io.djigger.ui.model;

import io.djigger.aggregation.Aggregation;
import io.djigger.aggregation.PathTransformer;
import io.djigger.aggregation.PathTransformerResult;
import io.djigger.aggregation.filter.Filter;
import io.djigger.model.NodeID;
import io.djigger.model.RealNodePath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class Node implements Comparable<Node> {

	private final NodeID id;

	private final Node parent;

	private final List<Node> children = new ArrayList<Node>();

	private final List<NodeAggregation> aggregations = new ArrayList<NodeAggregation>(1);

	private boolean nodeFilterMark;

	private Node(Node parent, NodeID id) {
		super();
		this.id = id;
		this.parent = parent;
		nodeFilterMark = true;
	}

	private Node() {
		this.id = null;
		this.parent = null;
		nodeFilterMark = true;
	}


	public static Node buildNode(List<Aggregation> aggregations, PathTransformer pathTransformer, Filter<NodeID> nodeFilter) {
		Node root = new Node();
		for(Aggregation aggregation:aggregations) {
			root.loadAggregation(aggregation, pathTransformer);
		}
		
		root.sort();

		return root;
	}
	
	private void sort() {
		for(Node child:getChildren()) {
			child.sort();
		}
		Collections.sort(children);
	}

	public void loadAggregation(Aggregation aggregation,PathTransformer pathTransformer) {
		aggregations.add(new NodeAggregation(null, aggregation));

		List<PathTransformerResult> transformations = pathTransformer.transformPath(aggregation.getPath());

		Node parent = this,child = null;
		for(PathTransformerResult transformation:transformations) {
			NodeID nodeID = transformation.getNodeID();
			child = parent.getChildByID(nodeID);
			if(child == null) {
				child = new Node(parent, nodeID);
				parent.children.add(child);
			}
			NodeAggregation nodeAggregation = new NodeAggregation(transformation.getRealPath(), aggregation);
			child.aggregations.add(nodeAggregation);

			parent = child;
		}


	}
	
	private Node getChildByID(NodeID id) {
		for(Node child:children) {
			if(id.equals(child.getId())) {
				return child;
			}
		}
		return null;
	}

	public void applyNodeFilter(Filter<NodeID> filter) {
		nodeFilterMark = filter.isValid(id);
		for(Node child:children) {
			child.applyNodeFilter(filter);
		}
	}

	public List<Node> getNodeFilteredChildren() {
		ArrayList<Node> result = new ArrayList<Node>();
		getNextNodes(result);
		return result;
	}

	private int getNextNodes(List<Node> nextNodes) {
		for(Node child:children) {
			if(child.nodeFilterMark) {
				nextNodes.add(child);
			} else {
				if(!child.isLeaf()) {
					child.getNextNodes(nextNodes);
				}
			}
		}

		return 0;
	}

	public List<Node> getChildren() {
		return children;
	}

	public List<NodeAggregation> getAggregations() {
		return aggregations;
	}
	public int getWeight() {
		int weight = 0;
		for(NodeAggregation aggregation:aggregations) {
			weight += aggregation.getAggregation().getSamples().size();
		}
		return weight;
	}

	public int getOwnWeight() {
		int childWeightCount = 0;
		for(Node child:children) {
			childWeightCount += child.getWeight();
		}
		return getWeight() - childWeightCount;
	}

	@Override
	public int compareTo(Node o) {
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
	public RealNodePath getPath() {
		if(aggregations.size()>0 && parent!=null) {
			return aggregations.get(0).getPath().getPath();
		} else {
			return null;
		}
	}

	public boolean isLeaf() {
		return children.size() == 0;
	}

	public Node getRoot() {
		if(parent == null) {
			return this;
		} else {
			return parent.getRoot();
		}
	}

	public NodePath getTreeNodePath() {
		NodePath path = new NodePath();
		Node currentNode = this;
		while(currentNode!=null) {
			if(currentNode.id!=null) {
				path.add(0,currentNode.id);
			}
			currentNode = currentNode.parent;
		}
		return path;
	}
	
	public List<Node> getTreePath() {
		ArrayList<Node> path = new ArrayList<Node>();
		Node currentNode = this;
		while(currentNode!=null) {
			path.add(0,currentNode);
			currentNode = currentNode.parent;
		}
		return path;
	}

	// TODO: see if this method really belongs to this class.
	public Node find(NodePath path) {
		if(path!=null && path.size()>0) {
			LinkedList<NodeID> stack = new LinkedList<NodeID>();
			stack.addAll(path);
			return findInChildren(stack);
		} else {
			return null;
		}
	}

	// TODO: does such a method really belongs to this class?
	private Node findInChildren(LinkedList<NodeID> branch) {
		NodeID currentID = branch.pop();
		try {
			Node node = getChildByID(currentID);
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
		Node other = (Node) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
