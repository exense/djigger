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

package io.djigger.monitoring.java.instrumentation.subscription;

import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;
import io.djigger.monitoring.java.instrumentation.InstrumentationAttributes;
import io.djigger.monitoring.java.instrumentation.InstrumentationSample;
import io.djigger.monitoring.java.model.StackTraceElement;
import io.djigger.monitoring.java.model.ThreadInfo;

import java.util.Arrays;

public class RealNodePathSubscription extends InstrumentSubscription {

	static final long serialVersionUID = 173774663260136913L;

	private final StackTraceElement[] path;
	
	private final InstrumentationAttributes attributes;

	public RealNodePathSubscription(StackTraceElement[]  path, boolean isTransactionEntryPoint) {
		super(isTransactionEntryPoint);
		this.path = path;
		this.attributes = new InstrumentationAttributes();
		attributes.addStacktrace();
		attributes.addThreadId();
	}

	@Override
	public boolean match(InstrumentationSample sample) {
		ThreadInfo threadInfo = sample.getAtributesHolder().getStacktrace();
		return Arrays.equals(path, threadInfo.getStackTrace());
	}

	public StackTraceElement[]  getPath() {
		return path;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RealNodePathSubscription other = (RealNodePathSubscription) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}

	@Override
	public InstrumentationAttributes getInstrumentationAttributes() {
		return attributes;
	}

	@Override
	public boolean isRelatedToClass(String classname) {
		StackTraceElement lastNode = path[0] ;
		return lastNode.getClassName().equals(classname);
	}

	@Override
	public boolean isRelatedToMethod(String methodname) {
		StackTraceElement lastNode = path[0] ;
		return lastNode.getMethodName().equals(methodname);
	}

	@Override
	public String getName() {
		StackTraceElement lastNode = path[0] ;
		return ".../" + lastNode.getClassName() + '.' + lastNode.getMethodName();
	}
}
