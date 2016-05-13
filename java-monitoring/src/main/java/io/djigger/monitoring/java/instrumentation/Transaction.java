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

import java.util.Stack;
import java.util.UUID;

import org.bson.types.ObjectId;

public class Transaction {

	private UUID id;
	
	private ObjectId parentId;
	
	private Stack<InstrumentationEvent> eventStack = new Stack<>();
	
	public Transaction(UUID id) {
		super();
		this.id = id;
	}

	public Transaction() {
		super();
		id = UUID.randomUUID();
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getId() {
		return id;
	}
	
	public ObjectId getParentId() {
		return parentId;
	}

	public void setParentId(ObjectId parentId) {
		this.parentId = parentId;
	}

	public void pushEvent(InstrumentationEvent currentEvent) {
		eventStack.push(currentEvent);
	}
	
	public InstrumentationEvent popEvent() {
		return eventStack.pop();
	}
	
	public InstrumentationEvent peekEvent() {
		return eventStack.peek();
	}
	
	public boolean isStackEmpty() {
		return eventStack.isEmpty();
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		Transaction other = (Transaction) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
}
