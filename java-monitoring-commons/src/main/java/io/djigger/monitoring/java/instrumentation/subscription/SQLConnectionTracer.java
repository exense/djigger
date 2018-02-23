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

import java.sql.Connection;

import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;
import io.djigger.monitoring.java.instrumentation.TransformingSubscription;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

public class SQLConnectionTracer extends InstrumentSubscription implements TransformingSubscription  {

	private static final long serialVersionUID = 1170484117770802108L;

	public SQLConnectionTracer() {
		super(false);
	}
	
	public SQLConnectionTracer(boolean tagEvent) {
		super(tagEvent);
	}
	
	@Override
	public boolean isRelatedToClass(CtClass classname) {
		try {
			CtClass connectionClass = ClassPool.getDefault().getCtClass("java.sql.Connection");
			return classname.subtypeOf(connectionClass);
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean retransformClass(Class<?> class_) {
		return Connection.class.isAssignableFrom(class_); 
	}

	@Override
	public boolean isRelatedToMethod(CtMethod methodname) {
		return true;
	}

	@Override
	public void transform(CtClass clazz, CtMethod method) throws CannotCompileException {
		if(!method.isEmpty()) {
			if(method.getName().equals("prepareStatement")||method.getName().equals("prepareCall")) {
				TimeMeasureTransformer.transform(clazz, method, this, false);
				method.insertAfter("io.djigger.agent.InstrumentationEventCollector.attachData($_, new io.djigger.monitoring.java.instrumentation.StringInstrumentationEventData($1));");
			} 
		}
	}
}
