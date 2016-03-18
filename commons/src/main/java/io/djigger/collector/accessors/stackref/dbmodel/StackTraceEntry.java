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
