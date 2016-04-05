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
	
	boolean includeLineNumbers = false;

	public Aggregator(Store store) {
		super();
		aggregations = new HashMap<RealNodePath,Aggregation>();
		this.store = store;
	}

	public boolean isIncludeLineNumbers() {
		return includeLineNumbers;
	}

	public void setIncludeLineNumbers(boolean includeLineNumbers) {
		this.includeLineNumbers = includeLineNumbers;
	}

	private void aggregate(List<ThreadInfo> threadDumps) {
		for(ThreadInfo thread:threadDumps) {
			RealNodePath nodePath = RealNodePath.fromStackTrace(thread.getStackTrace(), includeLineNumbers);
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
