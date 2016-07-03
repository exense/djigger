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

import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;
import io.djigger.monitoring.java.model.ThreadInfo;

import java.util.Set;


public class IdStoreFilter implements StoreFilter {

	private final Set<Long> threadIds;

	private final Long threadDumpId1;

	private final Long threadDumpId2;

	public IdStoreFilter(Set<Long> threadIds, Long threadDumpId1,
			Long threadDumpId2) {
		super();
		this.threadIds = threadIds;
		this.threadDumpId1 = threadDumpId1;
		this.threadDumpId2 = threadDumpId2;
	}

	public Set<Long> getThreadIds() {
		return threadIds;
	}

	public Long getThreadDumpId1() {
		return threadDumpId1;
	}

	public Long getThreadDumpId2() {
		return threadDumpId2;
	}

	@Override
	public boolean match(InstrumentationEvent sample) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public boolean match(ThreadInfo dump) {
		return threadIds.contains(dump.getId());
	}
}
