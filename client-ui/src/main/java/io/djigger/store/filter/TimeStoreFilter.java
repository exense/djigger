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

package io.djigger.store.filter;

import io.djigger.monitoring.java.instrumentation.InstrumentationSample;
import io.djigger.monitoring.java.model.ThreadInfo;

import java.util.Date;
import java.util.Set;


public class TimeStoreFilter implements StoreFilter {

	private final Set<Long> threadIds;

	private final Long startDate;

	private final Long endDate;

	public TimeStoreFilter(Set<Long> threadIds, Long startDate,
			Long endDate) {
		super();
		this.threadIds = threadIds;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public Set<Long> getThreadIds() {
		return threadIds;
	}

	@Override
	public boolean match(ThreadInfo thread) {
		if((startDate == null || thread.getTimestamp().after(new Date(startDate)))
			&& (endDate == null || thread.getTimestamp().before(new Date(endDate)))) {
			return (threadIds==null || threadIds.contains(thread.getId()));
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
