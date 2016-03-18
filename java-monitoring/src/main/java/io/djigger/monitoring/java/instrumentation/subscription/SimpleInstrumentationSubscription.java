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

public class SimpleInstrumentationSubscription extends InstrumentSubscription {

	private static final long serialVersionUID = -1137052413341333149L;

	private final String classname;
	
	private final String methodname;

	public SimpleInstrumentationSubscription(boolean transactionEntryPoint, String classname, String methodname) {
		super(transactionEntryPoint);
		this.classname = classname;
		this.methodname = methodname;
	}

	@Override
	public boolean match(InstrumentationSample sample) {
		return isRelatedToClass(sample.getClassname()) && isRelatedToMethod(sample.getMethodname());
	}

	@Override
	public InstrumentationAttributes getInstrumentationAttributes() {
		return new InstrumentationAttributes();
	}

	@Override
	public boolean isRelatedToClass(String classname) {
		return this.classname.equals(classname);
	}

	@Override
	public boolean isRelatedToMethod(String methodname) {
		return this.methodname.equals(methodname);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((classname == null) ? 0 : classname.hashCode());
		result = prime * result
				+ ((methodname == null) ? 0 : methodname.hashCode());
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
		SimpleInstrumentationSubscription other = (SimpleInstrumentationSubscription) obj;
		if (classname == null) {
			if (other.classname != null)
				return false;
		} else if (!classname.equals(other.classname))
			return false;
		if (methodname == null) {
			if (other.methodname != null)
				return false;
		} else if (!methodname.equals(other.methodname))
			return false;
		return true;
	}

	@Override
	public String getName() {
		return classname + "." + methodname;
	}


}
