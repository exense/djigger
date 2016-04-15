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

import java.util.List;

import io.djigger.aggregation.filter.Filter;
import io.djigger.ui.model.AnalysisNode;
import io.djigger.ui.model.RealNodeAggregation;
import io.djigger.ui.model.NodeID;
import io.djigger.ui.model.RealNode;

public class AnalysisTreeBuilder {

	public AnalysisNode build(RealNode realTree, List<Aggregation> aggregations, PathTransformer pathTransformer, Filter<NodeID> nodeFilter) {
		AnalysisNode root = new AnalysisNode();
		for(Aggregation aggregation:aggregations) {
			loadAggregation(realTree, root, aggregation, pathTransformer);
		}
		
		root.sort();

		return root;
	}

	private void loadAggregation(RealNode realTree, AnalysisNode node, Aggregation aggregation,PathTransformer pathTransformer) {
		node.getAggregations().add(new RealNodeAggregation(null, aggregation));

		List<RealNode> transformedPath = pathTransformer.transformPath(realTree, aggregation.getPath());

		AnalysisNode parent = node,child = null;
		for(RealNode realNode:transformedPath) {
			NodeID nodeID = realNode.getId();

			child = parent.getChildByID(nodeID);
			if(child == null) {
				child = new AnalysisNode(parent, nodeID);
				parent.getChildren().add(child);
			}

			RealNodeAggregation nodeAggregation = new RealNodeAggregation(realNode, aggregation);
			child.getAggregations().add(nodeAggregation);

			parent = child;
		}


	}
}
