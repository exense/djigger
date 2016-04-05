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
package io.djigger.collector.accessors.stackref.dbmodel;

import org.bson.types.ObjectId;

public class StackTraceEntry {
	
	private ObjectId _id;
	
	private int hashcode;

	private StackTraceElementEntry[] elements;

	public StackTraceEntry() {
		super();
	}

	public StackTraceEntry(ObjectId _id, StackTraceElementEntry[] elements) {
		super();
		this._id = _id;
		
		this.elements = elements;
	}

	public ObjectId get_id() {
		return _id;
	}

	public void set_id(ObjectId _id) {
		this._id = _id;
	}

	public StackTraceElementEntry[] getElements() {
		return elements;
	}

	public void setElements(StackTraceElementEntry[] elements) {
		this.elements = elements;
	}

	public int getHashcode() {
		return hashcode;
	}

	public void setHashcode(int hashcode) {
		this.hashcode = hashcode;
	}
}
