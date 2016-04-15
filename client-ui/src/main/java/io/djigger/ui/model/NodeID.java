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
package io.djigger.ui.model;

import java.io.ObjectStreamException;
import java.io.Serializable;


public class NodeID implements Serializable, Poolable {

	private static final long serialVersionUID = -2486310135334134770L;

	private final String className;

	private final String methodName;
	
	private String cachedFullname;

	private final int hashcode;

	private static final InstancePool<NodeID> pool = new InstancePool<NodeID>();

	public static final NodeID ROOT = getInstance("","");

	public static NodeID getInstance(String className, String methodName) {
		return pool.getInstance(new NodeID(className, methodName));
	}

	public static NodeID getInstance(io.djigger.monitoring.java.model.StackTraceElement element, boolean includeLineNumbers) {
		return pool.getInstance(new NodeID(element, includeLineNumbers));
	}

	private NodeID(io.djigger.monitoring.java.model.StackTraceElement element, boolean includeLineNumbers) {
		this.className = element.getClassName();
		if(includeLineNumbers) {
			this.methodName = element.getMethodName()+"("+element.getLineNumber()+")";
		} else {
			this.methodName = element.getMethodName();
		}
		this.hashcode = calculateHashCode();
	}

	private NodeID(String className, String methodName) {
		this.className = className;
		this.methodName = methodName;
		this.hashcode = calculateHashCode();
	}

	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}
	
	private final static String DELIMITER = ".";
	
	private String buildFullname() {
		StringBuilder builder = new StringBuilder(className.length()+1+methodName.length());
		builder.append(className).append(DELIMITER).append(methodName);
		return builder.toString();
	}

	public synchronized String getFullname() {
		if(cachedFullname==null) {
			cachedFullname = buildFullname();
		}
		return cachedFullname;
	}

	@Override
	public String toString() {
		return getFullname();
	}

	@Override
	public int hashCode() {
		return hashcode;
	}

	public int calculateHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((className == null) ? 0 : className.hashCode());
		result = prime * result
				+ ((methodName == null) ? 0 : methodName.hashCode());
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
		NodeID other = (NodeID) obj;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (methodName == null) {
			if (other.methodName != null)
				return false;
		} else if (!methodName.equals(other.methodName))
			return false;
		return true;
	}

	private Object readResolve() throws ObjectStreamException {
		return pool.getInstance(this);
	}

	@Override
	public Object getPoolIndex() {
		return this;
	}

}
