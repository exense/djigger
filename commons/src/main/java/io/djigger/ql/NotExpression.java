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


public class NotExpression<T> implements Filter<T> {

	private final Filter<T> filter;

	public NotExpression(Filter<T> filter) {
		super();
		this.filter = filter;
	}

	@Override
	public boolean isValid(T input) {
		return !filter.isValid(input);
	}

	@Override
	public void write(StringBuilder builder, T input) {
		builder.append("{ $not: ");
		filter.write(builder, input);
		builder.append("}");
	}
}