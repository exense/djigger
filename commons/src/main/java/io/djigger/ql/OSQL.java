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
package io.djigger.ql;

import java.io.StringReader;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.bson.conversions.Bson;

public class OSQL {
	
	@SuppressWarnings("unchecked")
	public static <T>Filter<T> createFilter(String expression, FilterFactory<T> filterFactory) throws Exception {
		CommonTree tree = getTree(expression);   
		OSQLTreeParser treeWalker = new OSQLTreeParser(new CommonTreeNodeStream(tree));
		treeWalker.setFilterFactory(filterFactory);

		return (Filter<T>) treeWalker.cond();
	}
	
	public static Bson toMongoQuery(String expression) throws RecognitionException {
		CommonTree tree = getTree(expression);   
		OSQLToMongoQuery treeWalker = new OSQLToMongoQuery(new CommonTreeNodeStream(tree));
		return treeWalker.buildQuery();
	}
	
	private static CommonTree getTree(String expression) throws RecognitionException {
		StringReader reader = null;
		try {
			reader = new StringReader(expression);

			OSQLLexer lexer;
			OSQLParser parser;

			lexer = new OSQLLexer(new ANTLRStringStream(expression));
			parser = new OSQLParser(new CommonTokenStream(lexer));
			
			return (CommonTree)parser.orexpression().getTree();   
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

}
