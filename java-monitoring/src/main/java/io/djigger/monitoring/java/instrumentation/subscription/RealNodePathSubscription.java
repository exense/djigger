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
