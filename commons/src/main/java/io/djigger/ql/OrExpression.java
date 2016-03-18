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

package io.djigger.ql;


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
	
	@Override
	public void write(StringBuilder builder, T input) {
		builder.append("{ $or: [");
		filter1.write(builder, input);
		builder.append(",");
		filter2.write(builder, input);
		builder.append("]}");
	}

}
