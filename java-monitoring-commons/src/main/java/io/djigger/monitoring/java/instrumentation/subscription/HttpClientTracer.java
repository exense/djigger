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

public class HttpClientTracer extends InstrumentSubscription implements TransformingSubscription  {

	private static final long serialVersionUID = 341521471983041872L;

	public HttpClientTracer() {
		super(false);
	}
	
	public HttpClientTracer(boolean tagEvent) {
		super(tagEvent);
	}
	
	@Override
	public boolean isRelatedToClass(CtClass clazz) {
		return isRelatedToClass(clazz.getName());
	}

	@Override
	public boolean retransformClass(Class<?> clazz) {
		return isRelatedToClass(clazz.getName());
	}
	
	private boolean isRelatedToClass(String classname) {
		return classname.contains("DefaultBHttpClientConnection");
	}

	@Override
	public boolean isRelatedToMethod(CtMethod method) {
		return method.getName().equals("sendRequestHeader");
	}	

	@Override
	public void transform(CtClass clazz, CtMethod method) throws CannotCompileException {
		method.insertBefore("if(!$1.containsHeader(\"djigger\")){$1.addHeader(\"djigger\",io.djigger.agent.InstrumentationEventCollector.getCurrentTracer());};");
	}
	
	@Override
	public String toString() {
		return "Http Client Tracer";
	}

}
