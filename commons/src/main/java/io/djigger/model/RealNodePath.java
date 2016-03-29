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
import java.util.ArrayList;

public class RealNodePath implements Serializable, Poolable {

	private static final long serialVersionUID = 2526447013111523763L;

	private final ArrayList<NodeID> fullPath;

	private Integer cachedHashCode;

	private static final InstancePool<RealNodePath> pool = new InstancePool<RealNodePath>();
	
	public static RealNodePath fromStackTrace(StackTraceElement[] stacktrace, boolean includeLineNumbers) {
		ArrayList<NodeID> nodeIDs = new ArrayList<NodeID>(stacktrace.length);
		for(int i=stacktrace.length-1;i>=0;i--) {
			nodeIDs.add(NodeID.getNewInstance(stacktrace[i], includeLineNumbers));
		}
		return getInstance(nodeIDs);
		
	}
	
	public static RealNodePath getInstance(ArrayList<NodeID> fullPath) {
		return pool.getInstance(new RealNodePath(fullPath));
	}
	
	public static RealNodePath getPooledInstance(ArrayList<NodeID> fullPath) {
		return pool.getInstance(new RealNodePath(fullPath));
	}

	private RealNodePath(ArrayList<NodeID> fullPath) {
		this.fullPath = fullPath;
	}

	@Override
	public int hashCode() {
		if(cachedHashCode==null) {
			cachedHashCode = calculateHashCode();
		}
		return cachedHashCode;
	}

	private int calculateHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((fullPath == null) ? 0 : fullPath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}

	/**
	 * @param path
	 * @return <ul><li>-1 if the path is not contained in this path.</li>
	 * 			<li>0 if the pathes are equals</li>
	 * 			<li>1 if the path is contained in this path</li></ul>
	 */
	public int containsPath(RealNodePath path) {
		if(path!=null && path.fullPath!=null && path.fullPath.size()<=fullPath.size()) {
			for(int i=0;i<path.fullPath.size();i++) {
				if(!path.fullPath.get(i).equals(fullPath.get(i))) {
					return -1;
				}
			}
			return path.fullPath.size()==fullPath.size()?0:1;
		} else {
			return -1;
		}
	}

	public NodeID getLastNode() {
		return fullPath.get(fullPath.size()-1);
	}

	public ArrayList<NodeID> getFullPath() {
		return fullPath;
	}

	@Override
	public Object getPoolIndex() {
		return fullPath;
	}
	
	private Object readResolve() throws ObjectStreamException {
		return pool.getInstance(this);
	}
}
