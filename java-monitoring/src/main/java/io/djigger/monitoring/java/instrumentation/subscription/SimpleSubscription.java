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

public class SimpleSubscription extends InstrumentSubscription {

	private static final long serialVersionUID = -1137052413341333149L;

	private final String classname;
	
	private final String methodname;

	public SimpleSubscription(String classname, String methodname, boolean tagEvent) {
		super(tagEvent);
		this.classname = classname;
		this.methodname = methodname;
	}

	@Override
	public boolean match(InstrumentationEvent sample) {
		return isRelatedToClass(sample.getClassname()) && isRelatedToMethod(sample.getMethodname());
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
		SimpleSubscription other = (SimpleSubscription) obj;
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

	@Override
	public boolean captureThreadInfo() {
		return false;
	}


}
