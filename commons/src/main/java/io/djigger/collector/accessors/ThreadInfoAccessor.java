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
package io.djigger.collector.accessors;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.bson.conversions.Bson;

import io.djigger.monitoring.java.model.ThreadInfo;

public interface ThreadInfoAccessor {
	
	public void createIndexesIfNeeded(Long ttl);
	
	public Iterable<ThreadInfo> query(Bson mongoQuery, Date from, Date to);
	
	public long count(Bson mongoQuery, Date from, Date to, long timeout, TimeUnit timeUnit) throws TimeoutException;
	
	public void save(ThreadInfo threadInfo);
}
