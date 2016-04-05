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
package io.djigger.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RealNode {

	private final NodeID id;

	private final RealNode parent;

	private final Map<NodeID, RealNode> children = new HashMap<NodeID, RealNode>();

	public static final RealNode root = new RealNode(null,null);

	public static RealNode getInstance(RealNodePath path) {
		return find(path);
	}

	private RealNode(NodeID id, RealNode parent) {
		super();
		this.id = id;
		this.parent = parent;
	}

	private static RealNode find(RealNodePath path) {
		RealNode parent = root,child = null;
		for(NodeID nodeID:path.getFullPath()) {
			child = getOrCreateChild(parent, nodeID);
			parent = child;
		}
		return child;
	}

	private static RealNode getOrCreateChild(RealNode parent, NodeID nodeID) {
		RealNode child = parent.children.get(nodeID);
		if(child == null) {
			child = new RealNode(nodeID, parent);
			parent.children.put(nodeID, child);
		}
		return child;
	}

	public RealNode getChild(NodeID nodeID) {
		return getOrCreateChild(this, nodeID);
	}

	public RealNodePath getPath() {
		ArrayList<NodeID> path = new ArrayList<NodeID>();
		buildPath(path);
		return RealNodePath.getPooledInstance(path);
	}

	private void buildPath(List<NodeID> pathBuilder) {
		if(parent!=null) {
			parent.buildPath(pathBuilder);
			pathBuilder.add(id);
		}
	}
}
