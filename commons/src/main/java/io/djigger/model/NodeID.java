/*******************************************************************************
 * (C) Copyright  2016 Jérôme Comte and others.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Contributors:
 *    - Jérôme Comte
 *******************************************************************************/

package io.djigger.model;

import io.djigger.monitoring.java.model.StackTraceElement;

import java.io.ObjectStreamException;
import java.io.Serializable;


public class NodeID implements Serializable, Poolable {

	private static final long serialVersionUID = -2486310135334134770L;

	private final String className;

	private final String methodName;

	private final int hashcode;

	private static final InstancePool<NodeID> pool = new InstancePool<NodeID>();

	public static final NodeID ROOT = getInstance("","");

	public static NodeID getInstance(String className, String methodName) {
		return pool.getInstance(new NodeID(className, methodName));
	}

	public static NodeID getInstance(io.djigger.monitoring.java.model.StackTraceElement element) {
		return pool.getInstance(new NodeID(element));
	}
	
	public static NodeID getNewInstance(io.djigger.monitoring.java.model.StackTraceElement element) {
		return new NodeID(element);
	}

	private NodeID(io.djigger.monitoring.java.model.StackTraceElement element) {
		this.className = element.getClassName();
		this.methodName = element.getMethodName();
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

	public String getFullname() {
		return className + "." + methodName;
	}

	@Override
	public String toString() {
		return className + "." + methodName;
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
