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
