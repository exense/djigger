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
// $ANTLR 2.7.7 (20060906): "FilterExpression.g" -> "FilterExpressionTreeParser.java"$

    package io.djigger.aggregation.filter;

import antlr.TreeParser;
import antlr.Token;
import antlr.collections.AST;
import antlr.RecognitionException;
import antlr.ANTLRException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.collections.impl.BitSet;
import antlr.ASTPair;
import antlr.collections.impl.ASTArray;


public class FilterExpressionTreeParser extends antlr.TreeParser       implements FilterExpressionLexerTokenTypes
 {

    private AtomicFilterFactory factory;

    public void setFilterFactory(AtomicFilterFactory factory)
    {
        this.factory = factory;
    }
public FilterExpressionTreeParser() {
	tokenNames = _tokenNames;
}

	public final Filter  cond(AST _t) throws RecognitionException {
		Filter r;
		
		AST cond_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		AST c = null;
		
		Filter a, b;
		r = null;
		
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case LITERAL_and:
			{
				AST __t21 = _t;
				AST tmp1_AST_in = (AST)_t;
				match(_t,LITERAL_and);
				_t = _t.getFirstChild();
				a=cond(_t);
				_t = _retTree;
				b=cond(_t);
				_t = _retTree;
				_t = __t21;
				_t = _t.getNextSibling();
				
				r = new AndExpression(a, b);
				
				break;
			}
			case LITERAL_or:
			{
				AST __t22 = _t;
				AST tmp2_AST_in = (AST)_t;
				match(_t,LITERAL_or);
				_t = _t.getFirstChild();
				a=cond(_t);
				_t = _retTree;
				b=cond(_t);
				_t = _retTree;
				_t = __t22;
				_t = _t.getNextSibling();
				
				r = new OrExpression(a, b);
				
				break;
			}
			case LITERAL_not:
			{
				AST __t23 = _t;
				AST tmp3_AST_in = (AST)_t;
				match(_t,LITERAL_not);
				_t = _t.getFirstChild();
				a=cond(_t);
				_t = _retTree;
				_t = __t23;
				_t = _t.getNextSibling();
				
				r = new NotExpression(a);
				
				break;
			}
			case WORD:
			{
				c = (AST)_t;
				match(_t,WORD);
				_t = _t.getNextSibling();
				
				r = factory.createFilter(c.getText());
				
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return r;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"WORD",
		"LEFT_PAREN",
		"RIGHT_PAREN",
		"WHITESPACE",
		"OPERATOR",
		"\"or\"",
		"\"and\"",
		"\"not\""
	};
	
	}
	
