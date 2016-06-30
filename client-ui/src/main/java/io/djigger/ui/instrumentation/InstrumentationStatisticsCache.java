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

import io.djigger.aggregation.Thread.RealNodePathWrapper;
import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;
import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;
import io.djigger.monitoring.java.instrumentation.InstrumentationEventWithThreadInfo;
import io.djigger.store.Store;
import io.djigger.store.filter.StoreFilter;
import io.djigger.ui.extensions.java.JavaBridge;
import io.djigger.ui.model.RealNodePath;

public class InstrumentationStatisticsCache {
	
	private Store store;
	
	private StoreFilter storeFilter;
	
	private Map<RealNodePath, InstrumentationStatistics> instrumentationStatisticsCache;
		
	private List<InstrumentationEvent> samples;
	
	public InstrumentationStatisticsCache(Store store) {
		super();
		this.store = store;
		reload();
	}

	public void setStoreFilter(StoreFilter storeFilter) {
		this.storeFilter = storeFilter;
	}

	public void reload() {
		samples = new ArrayList<InstrumentationEvent>();
		instrumentationStatisticsCache = new HashMap<RealNodePath, InstrumentationStatistics>();
		
		samples = store.getInstrumentationEvents().query(storeFilter!=null?storeFilter.getInstrumentationEventsFilter():null);

		for (InstrumentationEvent sample : samples) {
			if(sample instanceof InstrumentationEventWithThreadInfo) {
				RealNodePathWrapper pathWrapper = JavaBridge.toRealNodePath(((InstrumentationEventWithThreadInfo)sample).getThreadInfo(), false);
				if (pathWrapper != null) {
					RealNodePath path = pathWrapper.getPath();

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
		for(InstrumentationEvent sample:samples) {
			if(subscription.getId()==sample.getSubscriptionID()) {
				statistics.update(sample);
			}
		}
		
		return statistics;
	}
	
	
	public synchronized void exportSamples(File file) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(file)); 
		DateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");
		for(InstrumentationEvent sample:samples) {
			writer.write(format.format(new Date(sample.getStart()))+","+sample.getClassname()+","+sample.getMethodname()+","+sample.getDuration()+","+sample.getThreadID());
			writer.newLine();
		}
		writer.close();
	}
}
