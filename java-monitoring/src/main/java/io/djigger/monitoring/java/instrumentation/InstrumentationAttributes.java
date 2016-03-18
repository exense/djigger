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
package io.djigger.monitoring.java.instrumentation;

import java.io.Serializable;
import java.util.Arrays;

public class InstrumentationAttributes implements Serializable {
	
	private static final long serialVersionUID = 974490523750684955L;
	
	/**
	 * 0: stacktrace
	 * 1: threadid
	 */
	boolean[] attributes;

	public InstrumentationAttributes() {
		super();
		attributes = new boolean[1];
		Arrays.fill(attributes, false);
	}
	
	public void addStacktrace() {
		attributes[0] = true;
	}
	
	public static boolean hasStacktrace(boolean[] attributes) {
		return attributes[0];
	}
	
	public void addThreadId() {
		//attributes[1] = true;
	}
	
	public boolean[] getAttributes() {
		return attributes;
	}
	
	public void merge(InstrumentationAttributes attributesToMerge) {
		for(int i=0;i<attributes.length;i++) {
			attributes[i] = attributes[i] || attributesToMerge.attributes[i];			
		}
	}
}
