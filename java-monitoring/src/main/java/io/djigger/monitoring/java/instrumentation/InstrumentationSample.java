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
import java.util.UUID;


public class InstrumentationSample implements Serializable {

	private static final long serialVersionUID = 347226760314494168L;
	
	private final UUID contextID;

	private String classname;
	
	private String methodname;

	private final long start;

	private final long end;
	
	private final int duration;
	
	private final InstrumentationAttributesHolder atributesHolder;

	public InstrumentationSample(UUID contextID, String classname, String methodname,
			long start, long end,
			InstrumentationAttributesHolder atributesHolder) {
		super();
		this.contextID = contextID;
		this.classname = classname;
		this.methodname = methodname;
		this.start = start;
		this.end = end;
		this.atributesHolder = atributesHolder;
		this.duration = (int) (end-start);
	}

	public UUID getContextID() {
		return contextID;
	}

	public long getStart() {
		return start;
	}

	public long getEnd() {
		return end;
	}

	public InstrumentationAttributesHolder getAtributesHolder() {
		return atributesHolder;
	}

	public String getClassname() {
		return classname;
	}

	public void setClassname(String classname) {
		this.classname = classname;
	}

	public String getMethodname() {
		return methodname;
	}

	public void setMethodname(String methodname) {
		this.methodname = methodname;
	}

	public int getDuration() {
		return duration;
	}
}
