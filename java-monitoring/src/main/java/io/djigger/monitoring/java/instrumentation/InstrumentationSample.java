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
import java.util.UUID;


public class InstrumentationSample implements Serializable {

	private static final long serialVersionUID = 347226760314494168L;
	
	private final UUID contextID;

	private String classname;
	
	private String methodname;

	private final long start;

	private final long end;
	
	private final int duration;
	
	private final InstrumentationAttributesHolder atributesHolder;

	public InstrumentationSample(UUID contextID, String classname, String methodname,
			long start, long end,
			InstrumentationAttributesHolder atributesHolder) {
		super();
		this.contextID = contextID;
		this.classname = classname;
		this.methodname = methodname;
		this.start = start;
		this.end = end;
		this.atributesHolder = atributesHolder;
		this.duration = (int) (end-start);
	}

	public UUID getContextID() {
		return contextID;
	}

	public long getStart() {
		return start;
	}

	public long getEnd() {
		return end;
	}

	public InstrumentationAttributesHolder getAtributesHolder() {
		return atributesHolder;
	}

	public String getClassname() {
		return classname;
	}

	public void setClassname(String classname) {
		this.classname = classname;
	}

	public String getMethodname() {
		return methodname;
	}

	public void setMethodname(String methodname) {
		this.methodname = methodname;
	}

	public int getDuration() {
		return duration;
	}
}
