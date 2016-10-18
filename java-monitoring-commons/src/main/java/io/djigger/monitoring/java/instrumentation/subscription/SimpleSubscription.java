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

import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;
import io.djigger.monitoring.java.instrumentation.TransformingSubscription;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;

public class SimpleSubscription extends InstrumentSubscription implements TransformingSubscription {

	private static final long serialVersionUID = -1137052413341333149L;

	private final String classname;
	
	private final String methodname;

	public SimpleSubscription(String classname, String methodname, boolean tagEvent) {
		super(tagEvent);
		this.classname = classname;
		this.methodname = methodname;
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
		return this.classname.equals(classname);
	}

	@Override
	public boolean isRelatedToMethod(CtMethod method) {
		return this.methodname.equals(method.getName());
	}

	@Override
	public String toString() {
		return classname + "." + methodname;
	}

	@Override
	public void transform(CtClass clazz, CtMethod method) throws CannotCompileException {
		TimeMeasureTransformer.transform(clazz, method, this, false);
	}
}
