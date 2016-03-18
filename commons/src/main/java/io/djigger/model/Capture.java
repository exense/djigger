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

package io.djigger.model;

import java.io.Serializable;

public class Capture implements Serializable {

	private static final long serialVersionUID = 6575181579670411579L;

	private final long start;

	private Long end;

	private final int samplingInterval;

	public Capture(int samplingInterval) {
		super();
		this.start = System.currentTimeMillis();
		this.samplingInterval = samplingInterval;
	}

	public long getStart() {
		return start;
	}

	public Long getEnd() {
		return end;
	}

	public int getSamplingInterval() {
		return samplingInterval;
	}

	public void setEnd(Long end) {
		this.end = end;
	}
}
