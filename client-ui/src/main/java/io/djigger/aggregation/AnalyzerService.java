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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.djigger.monitoring.java.model.ThreadInfo;
import io.djigger.ql.Filter;
import io.djigger.store.Store;
import io.djigger.store.filter.StoreFilter;
import io.djigger.ui.analyzer.TreeType;
import io.djigger.ui.model.AnalysisNode;
import io.djigger.ui.model.NodeID;
import io.djigger.ui.model.RealNode;
import io.djigger.ui.model.RealNodePath;

public class AnalyzerService {

	private Store store;
	
	private Aggregator aggregator;
	
	private RealNodeBuilder realdNodeTreeBuilder;
	
	private RealNode realTree;

	public AnalyzerService(Store store) {
		super();
		this.store = store;
		this.aggregator = new Aggregator();
		this.realdNodeTreeBuilder = new RealNodeBuilder();
	}
	
	public synchronized void load(StoreFilter storeFilter, boolean includeLineNumbers) {
		List<ThreadInfo> threadInfos = store.queryThreadDumps(storeFilter);

		Map<Long, List<ThreadInfo>> threadInfoMap = groupByThreadId(threadInfos);
		List<Thread> threads = createThreadList(includeLineNumbers, threadInfoMap);
		
		realTree = realdNodeTreeBuilder.buildRealNodeTree(threads);
		
		aggregator.aggregate(threads);
	}

	private List<Thread> createThreadList(boolean includeLineNumbers, Map<Long, List<ThreadInfo>> threadInfoMap) {
		List<Thread> threads = new ArrayList<>();
		for(Long threadId:threadInfoMap.keySet()) {
			List<ThreadInfo> entry = threadInfoMap.get(threadId);
			sortThreadInfosByTime(entry);
			Thread thread = transformThreadInfoListToThread(threadId, includeLineNumbers, entry);
			threads.add(thread);
		}
		return threads;
	}

	private Thread transformThreadInfoListToThread(long threadId, boolean includeLineNumbers, List<ThreadInfo> entry) {
		List<RealNodePath> realNodePaths = new ArrayList<>();
		for(ThreadInfo threadInfo:entry) {
			RealNodePath path = RealNodePath.fromStackTrace(threadInfo.getStackTrace(), includeLineNumbers);
			realNodePaths.add(path);
		}
		Thread thread = new Thread(threadId, realNodePaths);
		return thread;
	}

	private void sortThreadInfosByTime(List<ThreadInfo> entry) {
		Collections.sort(entry, new Comparator<ThreadInfo>() {
			@Override
			public int compare(ThreadInfo arg0, ThreadInfo arg1) {
				return Long.compare(arg0.getTimestamp(), arg1.getTimestamp());
			}
		});
	}

	private Map<Long, List<ThreadInfo>> groupByThreadId(List<ThreadInfo> threadInfos) {
		Map<Long, List<ThreadInfo>> threadInfoMap = new HashMap<>();
		
		for(ThreadInfo threadInfo:threadInfos) {
			List<ThreadInfo> entry = threadInfoMap.get(threadInfo.getId());
			if(entry==null) {
				entry = new ArrayList<>();
				threadInfoMap.put(threadInfo.getId(), entry);
			}
			entry.add(threadInfo);
		}
		return threadInfoMap;
	}
	
	public synchronized AnalysisNode buildTree(Filter<RealNodePath> branchFilter, Filter<NodeID> nodeFilter, TreeType treeType) {
		List<Aggregation> aggregations = aggregator.query(branchFilter);

		PathTransformer pathTransformer;
		if(treeType == TreeType.REVERSE) {
			pathTransformer = new RevertTreePathTransformer(nodeFilter);
		} else {
			pathTransformer = new DefaultPathTransformer(nodeFilter);
		}
		
		AnalysisTreeBuilder treeBuilder = new AnalysisTreeBuilder();

		AnalysisNode aggregationTreeNode = treeBuilder.build(realTree, aggregations, pathTransformer, nodeFilter);
		return aggregationTreeNode;
	}
}
