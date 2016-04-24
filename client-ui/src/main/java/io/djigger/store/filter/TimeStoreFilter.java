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
package io.djigger.store.filter;

import io.djigger.monitoring.java.instrumentation.InstrumentationSample;
import io.djigger.monitoring.java.model.ThreadInfo;
import io.djigger.ql.Filter;

import java.util.Set;


public class TimeStoreFilter implements StoreFilter {

	private final Filter<ThreadInfo> threadnameFilter;
	
	private final Set<Long> threadIds;

	private final Long startDate;

	private final Long endDate;

	public TimeStoreFilter(Filter<ThreadInfo> threadnameFilter, Set<Long> threadIds, Long startDate,
			Long endDate) {
		super();
		this.threadnameFilter = threadnameFilter;
		this.threadIds = threadIds;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public Set<Long> getThreadIds() {
		return threadIds;
	}

	@Override
	public boolean match(ThreadInfo thread) {
		if((startDate == null || thread.getTimestamp()>startDate)
			&& (endDate == null || thread.getTimestamp()<endDate)) {
			return ((threadIds==null || threadIds.contains(thread.getId())) && 
					(threadnameFilter==null || threadnameFilter.isValid(thread)));
		} else {
			return false;
		}
	}

	@Override
	public boolean match(InstrumentationSample sample) {
		return (threadIds==null || threadIds.contains(sample.getAtributesHolder().getThreadID()))
				&& (startDate == null || sample.getStart()>=startDate)
				&& (endDate == null || sample.getEnd()<=endDate);
	}

}
