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

import java.io.ObjectStreamException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NodeSubscription extends InstrumentSubscription {

	private static final long serialVersionUID = -1137052413341333149L;
	
	private final Pattern classNamePattern;
	private transient Matcher classNameMatcher;
	
	private final Pattern methodNamePattern;
	private transient Matcher methodNameMatcher;

	public NodeSubscription(String classNameRegex, String methodNameRegex, boolean isTransactionEntryPoint) {
		super(isTransactionEntryPoint);
		classNamePattern = Pattern.compile(classNameRegex);
		classNameMatcher = classNamePattern.matcher("");
		methodNamePattern = Pattern.compile(methodNameRegex);
		methodNameMatcher = methodNamePattern.matcher("");
	}
	
	@Override
	public boolean match(InstrumentationSample sample) {
		return isRelatedToClass(sample.getClassname()) && isRelatedToMethod(sample.getMethodname());
	}

	@Override
	public InstrumentationAttributes getInstrumentationAttributes() {
		InstrumentationAttributes attributes = new InstrumentationAttributes();
		attributes.addStacktrace();
		attributes.addThreadId();
		return attributes;
	}

	@Override
	public boolean isRelatedToClass(String classname) {
		classNameMatcher.reset(classname);
		return classNameMatcher.matches();
	}

	@Override
	public boolean isRelatedToMethod(String methodname) {
		methodNameMatcher.reset(methodname);
		return methodNameMatcher.matches();
	}

	@Override
	public String getName() {
		return classNamePattern.pattern() + "/" + methodNamePattern.pattern();
	}
	
	private Object readResolve() throws ObjectStreamException {
		classNameMatcher = classNamePattern.matcher("");
		methodNameMatcher = methodNamePattern.matcher("");
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((classNamePattern == null) ? 0 : classNamePattern.pattern().hashCode());
		result = prime
				* result
				+ ((methodNamePattern == null) ? 0 : methodNamePattern.pattern()
						.hashCode());
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
		NodeSubscription other = (NodeSubscription) obj;
		if (classNamePattern == null) {
			if (other.classNamePattern != null)
				return false;
		} else if (!classNamePattern.pattern().equals(other.classNamePattern.pattern()))
			return false;
		if (methodNamePattern == null) {
			if (other.methodNamePattern != null)
				return false;
		} else if (!methodNamePattern.pattern().equals(other.methodNamePattern.pattern()))
			return false;
		return true;
	}

}
