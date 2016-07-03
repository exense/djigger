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
package io.djigger.aggregation;

import io.djigger.ql.Filter;
import io.djigger.ui.model.NodeID;
import io.djigger.ui.model.RealNode;
import io.djigger.ui.model.RealNodePath;

import java.util.ArrayList;
import java.util.List;


public class RevertTreePathTransformer implements PathTransformer {

	private final Filter<NodeID> nodeFilter;

	public RevertTreePathTransformer(Filter<NodeID> nodeFilter) {
		super();
		this.nodeFilter = nodeFilter;
	}

	@Override
	public List<RealNode> transformPath(RealNode realTree, RealNodePath path) {
		List<RealNode> transformations = new ArrayList<>(path.getFullPath().size());

		RealNode currentNode = realTree;
		for(NodeID nodeID:path.getFullPath()) {
			currentNode = currentNode.getChild(nodeID);
			if(nodeFilter == null || nodeFilter.isValid(nodeID)) {
				transformations.add(0,currentNode);
			}
		}

		return transformations;
	}
}
