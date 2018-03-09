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
