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
		for(State state:State.values()) {
			if(stateCount[state.ordinal()]>max) {
				max = stateCount[state.ordinal()];
				averageState = state;
			}
		}
	}

	public Thread.State getAverageState() {
		return averageState;
	}
}