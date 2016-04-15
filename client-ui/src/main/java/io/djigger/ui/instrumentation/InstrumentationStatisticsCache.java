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
package io.djigger.ui.instrumentation;

import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;
import io.djigger.monitoring.java.instrumentation.InstrumentationSample;
import io.djigger.store.Store;
import io.djigger.store.filter.StoreFilter;
import io.djigger.ui.model.RealNodePath;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstrumentationStatisticsCache {
	
	private Store store;
	
	private StoreFilter storeFilter;

	private Map<RealNodePath, List<InstrumentationSample>> instrumentationSamples;
	
	private Map<RealNodePath, InstrumentationStatistics> instrumentationStatisticsCache;
		
	private List<InstrumentationSample> samples;
	
	public InstrumentationStatisticsCache(Store store) {
		super();
		this.store = store;
		reload();
	}

	public void setStoreFilter(StoreFilter storeFilter) {
		this.storeFilter = storeFilter;
	}

	public void reload() {
		samples = new ArrayList<InstrumentationSample>();
		instrumentationSamples = new HashMap<RealNodePath, List<InstrumentationSample>>();
		instrumentationStatisticsCache = new HashMap<RealNodePath, InstrumentationStatistics>();
		
		samples = store.queryInstrumentationSamples(storeFilter);

		for (InstrumentationSample sample : samples) {
			if(sample.getAtributesHolder().getStacktrace()!=null) {
				RealNodePath path = RealNodePath.fromStackTrace(sample.getAtributesHolder().getStacktrace().getStackTrace(), false);
				if (path != null) {
					// samples by RealNodePath
					if (!instrumentationSamples.containsKey(path)) {
						instrumentationSamples.put(path,
								new ArrayList<InstrumentationSample>());
					}
					instrumentationSamples.get(path).add(sample);
	
					// statistics cache
					if (!instrumentationStatisticsCache.containsKey(path)) {
						instrumentationStatisticsCache.put(path, new InstrumentationStatistics());
					}
					instrumentationStatisticsCache.get(path).update(sample);
				}
			}
		}
	}
	
	public synchronized InstrumentationStatistics getInstrumentationStatistics(RealNodePath path) {
		return instrumentationStatisticsCache.get(path);
	}
	

	public synchronized InstrumentationStatistics getInstrumentationStatistics(InstrumentSubscription subscription) {
		InstrumentationStatistics statistics = new InstrumentationStatistics();
		for(InstrumentationSample sample:samples) {
			if(subscription.match(sample)) {
				statistics.update(sample);
			}
		}
		
		return statistics;
	}
	
	public synchronized List<InstrumentationSample> getInstrumentationSamples(InstrumentSubscription subscription) {
		List<InstrumentationSample> result = new ArrayList<InstrumentationSample>();
		for(InstrumentationSample sample:samples) {
			if(subscription.match(sample)) {
				result.add(sample);
			}
		}
		
		return result;
	}
	
	public synchronized void exportSamples(File file) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(file)); 
		DateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");
		for(InstrumentationSample sample:samples) {
			writer.write(format.format(new Date(sample.getStart()))+","+sample.getClassname()+","+sample.getMethodname()+","+sample.getDuration()+","+sample.getAtributesHolder().getThreadID());
			writer.newLine();
		}
		writer.close();
	}
}
