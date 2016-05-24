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
import io.djigger.monitoring.java.instrumentation.TransformingSubscription;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;

public class RegexSubscription extends InstrumentSubscription implements TransformingSubscription {

	private static final long serialVersionUID = -1137052413341333149L;
	
	private Pattern classNamePattern;
	private transient Matcher classNameMatcher;
	
	private Pattern methodNamePattern;
	private transient Matcher methodNameMatcher;

	public RegexSubscription(String classNameRegex, String methodNameRegex, boolean tagEvent) {
		super(tagEvent);
		setClassNamePattern(classNameRegex);
		setMethodNamePattern(methodNameRegex);
	}
	
	public void setClassNamePattern(String pattern) {
		classNamePattern = Pattern.compile(pattern);
		classNameMatcher = classNamePattern.matcher("");
	}

	public void setMethodNamePattern(String pattern) {
		methodNamePattern = Pattern.compile(pattern);
		methodNameMatcher = methodNamePattern.matcher("");
	}

	@Override
	public boolean isRelatedToClass(CtClass classname) {
		return isRelatedToClass(classname.getName());
	}

	@Override
	public boolean retransformClass(Class<?> classname) {
		return isRelatedToClass(classname.getName());
	}

	private boolean isRelatedToClass(String classname) {
		classNameMatcher.reset(classname);
		return classNameMatcher.matches();
	}

	@Override
	public boolean isRelatedToMethod(CtMethod method) {
		return isMethodMatch(method.getName());
	}

	private boolean isMethodMatch(String method) {
		methodNameMatcher.reset(method);
		return methodNameMatcher.matches();
	}

	@Override
	public String toString() {
		return classNamePattern.pattern() + "/" + methodNamePattern.pattern();
	}
	
	protected Object readResolve() throws ObjectStreamException {
		classNameMatcher = classNamePattern.matcher("");
		methodNameMatcher = methodNamePattern.matcher("");
		return this;
	}

	@Override
	public void transform(CtClass clazz, CtMethod method) throws CannotCompileException {
		TimeMeasureTransformer.transform(clazz, method, this, false);
	}
}
