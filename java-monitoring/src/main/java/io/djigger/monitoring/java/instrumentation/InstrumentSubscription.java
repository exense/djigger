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


public abstract class InstrumentSubscription implements Serializable {

	private static final long serialVersionUID = -299257813496574472L;

	private boolean transactionEntryPoint;
	
	public abstract boolean isRelatedToClass(String classname);
	
	public abstract boolean isRelatedToMethod(String methodname);

	public abstract boolean match(InstrumentationSample sample);
	
	public abstract InstrumentationAttributes getInstrumentationAttributes();
	
	public abstract String getName();

	public InstrumentSubscription(boolean transactionEntryPoint) {
		super();
		this.transactionEntryPoint = transactionEntryPoint;
	}

	public boolean isTransactionEntryPoint() {
		return transactionEntryPoint;
	}

	public void setTransactionEntryPoint(boolean transactionEntryPoint) {
		this.transactionEntryPoint = transactionEntryPoint;
	}
	
	@Override
	public String toString() {
		return getName();
	}

	@Override
	public abstract int hashCode();

	@Override
	public abstract boolean equals(Object obj);
}
