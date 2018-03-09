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

import io.djigger.aggregation.Thread.RealNodePathWrapper;
import io.djigger.ql.Filter;
import io.djigger.ui.analyzer.TreeType;
import io.djigger.ui.model.AnalysisNode;
import io.djigger.ui.model.NodeID;
import io.djigger.ui.model.RealNode;
import io.djigger.ui.model.RealNodePath;

import java.util.List;

public class AnalyzerService {

    private Aggregator aggregator;

    private RealNodeBuilder realdNodeTreeBuilder;

    private RealNode realTree;

    public AnalyzerService() {
        super();
        this.aggregator = new Aggregator();
        this.realdNodeTreeBuilder = new RealNodeBuilder();
    }

    public synchronized void load(List<RealNodePathWrapper> threads) {
        realTree = realdNodeTreeBuilder.buildRealNodeTree(threads);
        aggregator.aggregate(threads);
    }

    public synchronized AnalysisNode buildTree(Filter<RealNodePath> branchFilter, Filter<NodeID> nodeFilter, TreeType treeType) {
        List<Aggregation> aggregations = aggregator.query(branchFilter);

        PathTransformer pathTransformer;
        if (treeType == TreeType.REVERSE) {
            pathTransformer = new RevertTreePathTransformer(nodeFilter);
        } else {
            pathTransformer = new DefaultPathTransformer(nodeFilter);
        }

        AnalysisTreeBuilder treeBuilder = new AnalysisTreeBuilder();

        AnalysisNode aggregationTreeNode = treeBuilder.build(realTree, aggregations, pathTransformer, nodeFilter);
        return aggregationTreeNode;
    }
}
