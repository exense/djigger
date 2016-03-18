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

package io.djigger.aggregation;

import io.djigger.model.RealNodePath;
import io.djigger.monitoring.java.model.ThreadInfo;

import java.util.ArrayList;
import java.util.List;


public class Aggregation {

	private final RealNodePath path;

	private final ArrayList<ThreadInfo> samples;

	public Aggregation(RealNodePath path) {
		super();
		samples = new ArrayList<ThreadInfo>();
		this.path = path;
	}

	public void addSample(ThreadInfo sample) {
		samples.add(sample);
	}

	public void trim() {
		samples.trimToSize();
	}

	public RealNodePath getPath() {
		return path;
	}

	public List<ThreadInfo> getSamples() {
		return samples;
	}
}
