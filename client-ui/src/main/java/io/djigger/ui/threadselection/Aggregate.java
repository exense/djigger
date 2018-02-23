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

import io.djigger.monitoring.java.model.ThreadInfo;

import java.lang.Thread.State;

public class Aggregate {

    private Thread.State averageState;

    private final int[] stateCount;

    public Aggregate() {
        super();
        stateCount = new int[State.values().length];
    }

    public void add(ThreadInfo thread) {
        State state = thread.getState();
        stateCount[state.ordinal()]++;
    }

    public void calculate() {
        int max = 0;
        for (State state : State.values()) {
            if (stateCount[state.ordinal()] > max) {
                max = stateCount[state.ordinal()];
                averageState = state;
            }
        }
    }

    public Thread.State getAverageState() {
        return averageState;
    }
}
