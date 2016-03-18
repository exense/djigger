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

import java.util.Date;
import java.util.Set;

public class InstrumentationStatisticsFilter {

	private final Set<Long> threadIds;

	private final Date startDate;

	private final Date endDate;

	public InstrumentationStatisticsFilter(Set<Long> threadIds, Date startDate, Date endDate) {
		super();
		this.threadIds = threadIds;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public Set<Long> getThreadIds() {
		return threadIds;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

}
