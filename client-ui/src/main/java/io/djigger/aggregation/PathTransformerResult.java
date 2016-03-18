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

package io.djigger.aggregation;

import io.djigger.model.NodeID;
import io.djigger.model.RealNode;

public class PathTransformerResult {

	private final NodeID nodeID;

	private final RealNode realNode;

	public PathTransformerResult(NodeID nodeID, RealNode realNode) {
		super();
		this.nodeID = nodeID;
		this.realNode = realNode;
	}

	public RealNode getRealPath() {
		return realNode;
	}

	public NodeID getNodeID() {
		return nodeID;
	}

}
