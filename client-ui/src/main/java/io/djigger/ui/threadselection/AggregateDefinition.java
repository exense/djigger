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
		this.rangeInterval = (1.0*(end-start))/rangeNumber;
		this.linearRange = linearRange;
	}

	public long getStart() {
		return start;
	}

	public long getEnd() {
		return end;
	}
	
	public int getRangeId(long cursor) {
		return Math.min((int)((1.0*(cursor-start))/rangeInterval),rangeNumber-1);
	}
	
	public long getCursor(int rangeId) {
		if(linearRange) {
			return start+rangeId;
		} else {
			throw new RuntimeException("This method should only be called for continuous RangeDefinition");
		}
	}

	public int getRangeNumber() {
		return rangeNumber;
	}
	
	
	
	

}
