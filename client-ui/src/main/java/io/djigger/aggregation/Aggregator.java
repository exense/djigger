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

import io.djigger.aggregation.filter.Filter;
import io.djigger.model.RealNodePath;
import io.djigger.monitoring.java.model.ThreadInfo;
import io.djigger.store.Store;
import io.djigger.store.filter.StoreFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class Aggregator {

	private final Map<RealNodePath,Aggregation> aggregations;
	
	private final Store store;

	private StoreFilter filter;

	public Aggregator(Store store) {
		super();
		aggregations = new HashMap<RealNodePath,Aggregation>();
		this.store = store;
	}

	private void aggregate(List<ThreadInfo> threadDumps) {
		for(ThreadInfo thread:threadDumps) {
			RealNodePath nodePath = RealNodePath.fromStackTrace(thread.getStackTrace());
			//
			if(nodePath.getFullPath().size()>0) {
				Aggregation aggregation = aggregations.get(nodePath);
				if(aggregation == null) {
					aggregation = new Aggregation(nodePath);
					aggregations.put(nodePath, aggregation);
				}
				aggregation.addSample(thread);
			}
		}
		for(Aggregation aggregation:aggregations.values()) {
			aggregation.trim();
		}
	}
	
	public synchronized void setStoreFilter(StoreFilter storeFilter) {
		this.filter = storeFilter;
	}

	public synchronized void reload() {
		aggregations.clear();
		List<ThreadInfo> threadDumps = store.queryThreadDumps(filter);
		aggregate(threadDumps);
	}

	public synchronized List<Aggregation> getAggregations() {
		List<Aggregation> result = new ArrayList<Aggregation>();
		result.addAll(aggregations.values());
		return result;
	}

	public synchronized List<Aggregation> query(Filter<RealNodePath> filter) {
		List<Aggregation> result = new ArrayList<Aggregation>();
		for(Aggregation aggregation:aggregations.values()) {
			if(filter==null || filter.isValid(aggregation.getPath())) {
				result.add(aggregation);
			}
		}
		return result;
	}

}
