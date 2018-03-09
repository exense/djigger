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
package io.djigger.ui.threadselection;

public class AggregateDefinition {

    private final long start;

    private final long end;

    private final double rangeInterval;

    private final int rangeNumber;

    private boolean linearRange;

    public AggregateDefinition(long start, long end, final int rangeNumber, boolean linearRange) {
        super();
        this.start = start;
        this.end = end;
        this.rangeNumber = rangeNumber;
        this.rangeInterval = (1.0 * (end - start)) / rangeNumber;
        this.linearRange = linearRange;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public int getRangeId(long cursor) {
        return Math.min((int) ((1.0 * (cursor - start)) / rangeInterval), rangeNumber - 1);
    }

    public long getCursor(int rangeId) {
        if (linearRange) {
            return start + rangeId;
        } else {
            throw new RuntimeException("This method should only be called for continuous RangeDefinition");
        }
    }

    public int getRangeNumber() {
        return rangeNumber;
    }


}
