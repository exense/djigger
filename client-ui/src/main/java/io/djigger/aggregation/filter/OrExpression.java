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
package io.djigger.aggregation.filter;


public class OrExpression<T> implements Filter<T> {

	private final Filter<T> filter1;

	private final Filter<T> filter2;

	public OrExpression(Filter<T> filter1, Filter<T> filter2) {
		super();
		this.filter1 = filter1;
		this.filter2 = filter2;
	}

	@Override
	public boolean isValid(T input) {
		return filter1.isValid(input) || filter2.isValid(input);
	}

}
