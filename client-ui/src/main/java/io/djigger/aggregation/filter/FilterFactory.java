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

package io.djigger.aggregation.filter;

import java.io.StringReader;


import antlr.collections.AST;


public class FilterFactory<T> {

	private final AtomicFilterFactory<T> atomicFilterFactory;

	public FilterFactory(AtomicFilterFactory<T> atomicFilterFactory) {
		super();
		this.atomicFilterFactory = atomicFilterFactory;
	}

	@SuppressWarnings("unchecked")
	public Filter<T> getCompositeFilter(String expression) throws ParsingException {
		Filter<T> filter = null;
		StringReader reader = null;

		try {
			reader = new StringReader(expression);

			FilterExpressionLexer lexer;
			FilterExpressionParser parser;
			FilterExpressionTreeParser tree;

			lexer = new FilterExpressionLexer(reader);
			parser = new FilterExpressionParser(lexer);
			tree = new FilterExpressionTreeParser();
			tree.setFilterFactory(atomicFilterFactory);

			parser.orexpression();
			AST t = parser.getAST();
			filter = tree.cond(t);
		} catch (Exception e) {
			throw new ParsingException(e.getMessage());
		} finally {
			if (reader != null) {
				reader.close();
			}
		}

		return filter;
	}

}
