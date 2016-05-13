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
import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;
import io.djigger.monitoring.java.instrumentation.TransformingSubscription;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;

public class HttpClientTracer extends InstrumentSubscription implements TransformingSubscription  {

	public HttpClientTracer() {
		super(false);
		// TODO Auto-generated constructor stub
	}
	
	public HttpClientTracer(boolean tagEvent) {
		super(tagEvent);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean isRelatedToClass(String classname) {
		// TODO Auto-generated method stub
		return classname.contains("DefaultBHttpClientConnection");
	}

	@Override
	public boolean isRelatedToMethod(String methodname) {
		// TODO Auto-generated method stub
		return methodname.equals("sendRequestHeader");
	}

	@Override
	public boolean match(InstrumentationEvent sample) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean captureThreadInfo() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Http Client Tracer";
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this;
	}

	@Override
	public void transform(CtClass clazz, CtMethod method) {
		try {
			method.insertBefore("if(!$1.containsHeader(\"djigger\")){$1.addHeader(\"djigger\",io.djigger.agent.InstrumentationEventCollector.getCurrentTracer());};");
		} catch (CannotCompileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

}
