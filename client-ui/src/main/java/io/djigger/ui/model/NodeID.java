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

	private String id;
	
	private Object attachment;

	private final int hashcode;

	private static final InstancePool<NodeID> pool = new InstancePool<NodeID>();

	public static NodeID getInstance(String id) {
		return pool.getInstance(new NodeID(id));
	}

	private NodeID(String id) {
		this.id = id;
		this.hashcode = calculateHashCode();
	}
	
	public Object getAttachment() {
		return attachment;
	}
	
	public void setAttachment(Object attachment) {
		this.attachment = attachment;
	}

	@Override
	public String toString() {
		return id;
	}

	@Override
	public int hashCode() {
		return hashcode;
	}

	public int calculateHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((id == null) ? 0 : id.hashCode());
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
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
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
