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
import javassist.NotFoundException;

public class ServletTracer extends InstrumentSubscription implements TransformingSubscription  {

	private static final long serialVersionUID = 1170484117770802108L;

	public ServletTracer() {
		super(false);
	}
	
	public ServletTracer(boolean tagEvent) {
		super(tagEvent);
	}
	
	@Override
	public boolean isRelatedToClass(CtClass classname) {
		
		try {
			for(CtClass interface_:classname.getInterfaces()) {
				if(interface_.getName().equals("javax.servlet.Servlet")) {
					return true;
				}
			}
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean retransformClass(Class<?> class_) {
		for(Class<?> interface_:class_.getInterfaces()) {
			if(interface_.getName().equals("javax.servlet.Servlet")) {
				return true;
			}
		}
		//System.out.println("entering "+class_.getName());
		return false;
	}

	@Override
	public boolean isRelatedToMethod(CtMethod methodname) {
		return methodname.getName().equals("service");
	}

	@Override
	public String toString() {
		return "Servlet Tracer";
	}

	@Override
	public void transform(CtClass clazz, CtMethod method) throws CannotCompileException {
		method.insertBefore("if(arg0 instanceof javax.servlet.http.HttpServletRequest) {"+
				"String tr = ((javax.servlet.http.HttpServletRequest) $1).getHeader(\"djigger\");"+
				"io.djigger.agent.InstrumentationEventCollector.applyTracer(tr);"+
				"}");
		TimeMeasureTransformer.transform(clazz, method, this, false);
	}
}
