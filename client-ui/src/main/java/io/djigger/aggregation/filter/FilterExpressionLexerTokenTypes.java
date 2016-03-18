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
// $ANTLR 2.7.7 (20060906): "FilterExpression.g" -> "FilterExpressionLexer.java"$

    package io.djigger.aggregation.filter;

public interface FilterExpressionLexerTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int WORD = 4;
	int LEFT_PAREN = 5;
	int RIGHT_PAREN = 6;
	int WHITESPACE = 7;
	int OPERATOR = 8;
	int LITERAL_or = 9;
	int LITERAL_and = 10;
	int LITERAL_not = 11;
}
