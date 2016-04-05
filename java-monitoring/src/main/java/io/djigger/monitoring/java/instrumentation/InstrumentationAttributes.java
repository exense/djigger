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
import java.util.Arrays;

public class InstrumentationAttributes implements Serializable {
	
	private static final long serialVersionUID = 974490523750684955L;
	
	/**
	 * 0: stacktrace
	 * 1: threadid
	 */
	boolean[] attributes;

	public InstrumentationAttributes() {
		super();
		attributes = new boolean[1];
		Arrays.fill(attributes, false);
	}
	
	public void addStacktrace() {
		attributes[0] = true;
	}
	
	public static boolean hasStacktrace(boolean[] attributes) {
		return attributes[0];
	}
	
	public void addThreadId() {
		//attributes[1] = true;
	}
	
	public boolean[] getAttributes() {
		return attributes;
	}
	
	public void merge(InstrumentationAttributes attributesToMerge) {
		for(int i=0;i<attributes.length;i++) {
			attributes[i] = attributes[i] || attributesToMerge.attributes[i];			
		}
	}
}
