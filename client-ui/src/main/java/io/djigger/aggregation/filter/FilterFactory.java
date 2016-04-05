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
