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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;
import io.djigger.ui.model.InstrumentationEventWrapper;
import io.djigger.ui.model.RealNodePath;

public class InstrumentationStatisticsCache {
			
	private final Map<RealNodePath, InstrumentationStatistics> instrumentationStatisticsCache;
		
	private List<InstrumentationEventWrapper> samples;
	
	public InstrumentationStatisticsCache() {
		super();
		instrumentationStatisticsCache = new HashMap<RealNodePath, InstrumentationStatistics>();
	}

	public void reload(List<InstrumentationEventWrapper> samples) {
		this.samples = samples;
		
		instrumentationStatisticsCache.clear();
		
		for (InstrumentationEventWrapper sample : samples) {			
			if (sample.getEventPath() != null) {
				RealNodePath path = sample.getEventPath().getPath();

				// statistics cache
				if (!instrumentationStatisticsCache.containsKey(path)) {
					instrumentationStatisticsCache.put(path, new InstrumentationStatistics());
				}
				instrumentationStatisticsCache.get(path).update(sample.getEvent());
			}
		}
			
		
	}
	
	public synchronized InstrumentationStatistics getInstrumentationStatistics(RealNodePath path) {
		return instrumentationStatisticsCache.get(path);
	}
	

	public synchronized InstrumentationStatistics getInstrumentationStatistics(InstrumentSubscription subscription) {
		InstrumentationStatistics statistics = new InstrumentationStatistics();
		for(InstrumentationEventWrapper sample:samples) {
			if(subscription.getId()==sample.getEvent().getSubscriptionID()) {
				statistics.update(sample.getEvent());
			}
		}
		
		return statistics;
	}
}
