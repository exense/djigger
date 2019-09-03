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
package io.djigger.sequencetree;

import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;
import io.djigger.ql.Filter;
import io.djigger.store.Store;
import io.djigger.ui.analyzer.TreeType;
import io.djigger.ui.model.PseudoInstrumentationEvent;

import java.util.List;
import java.util.Stack;
import java.util.UUID;

public class SequenceTreeService {

    private Store store;

    private RealNodeSequenceTreeBuilder realdNodeTreeBuilder;

    private InstrumentationEventNode realTree;

    public SequenceTreeService(Store store) {
        super();
        this.store = store;
        this.realdNodeTreeBuilder = new RealNodeSequenceTreeBuilder();
    }

    public synchronized void load(UUID transactionID, boolean includeLineNumbers) {
        List<InstrumentationEvent> threadInfos = query(transactionID);

        realTree = realdNodeTreeBuilder.buildRealNodeTree(threadInfos);
    }

    public synchronized void load(PseudoInstrumentationEvent pseudoEvent) {
        List<InstrumentationEvent> threadInfos = query(pseudoEvent);
        realTree = realdNodeTreeBuilder.buildRealNodeTree(threadInfos);
    }

    private List<InstrumentationEvent> query(final PseudoInstrumentationEvent pseudoEvent) {
        return store.getInstrumentationEvents().query(new Filter<InstrumentationEvent>() {

            @Override
            public boolean isValid(InstrumentationEvent input) {
                return pseudoEvent.getGlobalThreadId() == input.getGlobalThreadId() && pseudoEvent.getStart() <= input.getStart() && pseudoEvent.getEnd() >= input.getEnd();
            }
        });
    }

    private List<InstrumentationEvent> query(final UUID transactionID) {
        return store.getInstrumentationEvents().query(new Filter<InstrumentationEvent>() {

            @Override
            public boolean isValid(InstrumentationEvent input) {
                return transactionID.equals(input.getTransactionID());
            }
        });
    }

    public synchronized SequenceTreeNode buildTree(Filter<Stack<InstrumentationEvent>> branchFilter, Filter<InstrumentationEvent> nodeFilter, TreeType treeType) {
//		List<Aggregation> aggregations = aggregator.query(branchFilter);
//
//		PathTransformer pathTransformer;
//		if(treeType == TreeType.REVERSE) {
//			pathTransformer = new RevertTreePathTransformer(nodeFilter);
//		} else {
//			pathTransformer = new DefaultPathTransformer(nodeFilter);
//		}
//		
        SequenceTreeBuilder treeBuilder = new SequenceTreeBuilder();

        SequenceTreeNode aggregationTreeNode = treeBuilder.build(realTree, branchFilter, nodeFilter);

        aggregationTreeNode.sort();

        return aggregationTreeNode;
    }

    private void filterBranches(SequenceTreeNode aggregationTreeNode, Filter<InstrumentationEvent> branchFilter) {

    }
}
