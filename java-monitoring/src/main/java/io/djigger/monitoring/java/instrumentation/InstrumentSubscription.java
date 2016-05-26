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
package io.djigger.monitoring.java.instrumentation;

import java.io.Serializable;

import javassist.CtClass;
import javassist.CtMethod;


public abstract class InstrumentSubscription implements Serializable {

	private static final long serialVersionUID = -299257813496574472L;
	
	private int id;
	
	private boolean tagEvent;

	public abstract boolean isRelatedToClass(CtClass clazz);
	
	public abstract boolean isRelatedToMethod(CtMethod method);
	
	public abstract boolean retransformClass(Class<?> clazz);

	public InstrumentSubscription(boolean tagEvent) {
		super();
		this.tagEvent = tagEvent;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isTagEvent() {
		return tagEvent;
	}

	public void setTagEvent(boolean tagEvent) {
		this.tagEvent = tagEvent;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		InstrumentSubscription other = (InstrumentSubscription) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
