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
package io.djigger.monitoring.java.instrumentation.subscription;

import java.io.ObjectStreamException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;
import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;

public class RegexSubscription extends InstrumentSubscription {

	private static final long serialVersionUID = -1137052413341333149L;
	
	private final Pattern classNamePattern;
	private transient Matcher classNameMatcher;
	
	private final Pattern methodNamePattern;
	private transient Matcher methodNameMatcher;

	public RegexSubscription(String classNameRegex, String methodNameRegex) {
		super();
		classNamePattern = Pattern.compile(classNameRegex);
		classNameMatcher = classNamePattern.matcher("");
		methodNamePattern = Pattern.compile(methodNameRegex);
		methodNameMatcher = methodNamePattern.matcher("");
	}
	
	@Override
	public boolean match(InstrumentationEvent sample) {
		return isRelatedToClass(sample.getClassname()) && isRelatedToMethod(sample.getMethodname());
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
		RegexSubscription other = (RegexSubscription) obj;
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

	@Override
	public boolean captureThreadInfo() {
		return false;
	}

}
