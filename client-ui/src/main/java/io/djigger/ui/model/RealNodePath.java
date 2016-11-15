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
import java.util.ArrayList;

public class RealNodePath implements Serializable, Poolable {

	private static final long serialVersionUID = 2526447013111523763L;

	private final ArrayList<NodeID> fullPath;

	private Integer cachedHashCode;

	private static final InstancePool<RealNodePath> pool = new InstancePool<RealNodePath>();
	
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
