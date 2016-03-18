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
