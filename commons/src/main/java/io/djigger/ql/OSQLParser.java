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
// $ANTLR 3.5.2 OSQL.g 2016-03-18 22:05:50

    package io.djigger.ql;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

import org.antlr.runtime.tree.*;


@SuppressWarnings("all")
public class OSQLParser extends Parser {
	public static final String[] tokenNames = new String[] {
		"<invalid>", "<EOR>", "<DOWN>", "<UP>", "ESCAPED_QUOTE", "INT", "LEFT_PAREN", 
		"OPERATOR", "QUOTED_STRING", "RIGHT_PAREN", "WHITESPACE", "WORD", "'and'", 
		"'not'", "'or'"
	};
	public static final int EOF=-1;
	public static final int T__12=12;
	public static final int T__13=13;
	public static final int T__14=14;
	public static final int ESCAPED_QUOTE=4;
	public static final int INT=5;
	public static final int LEFT_PAREN=6;
	public static final int OPERATOR=7;
	public static final int QUOTED_STRING=8;
	public static final int RIGHT_PAREN=9;
	public static final int WHITESPACE=10;
	public static final int WORD=11;

	// delegates
	public Parser[] getDelegates() {
		return new Parser[] {};
	}

	// delegators


	public OSQLParser(TokenStream input) {
		this(input, new RecognizerSharedState());
	}
	public OSQLParser(TokenStream input, RecognizerSharedState state) {
		super(input, state);
	}

	protected TreeAdaptor adaptor = new CommonTreeAdaptor();

	public void setTreeAdaptor(TreeAdaptor adaptor) {
		this.adaptor = adaptor;
	}
	public TreeAdaptor getTreeAdaptor() {
		return adaptor;
	}
	@Override public String[] getTokenNames() { return OSQLParser.tokenNames; }
	@Override public String getGrammarFileName() { return "OSQL.g"; }


	public static class compilationUnit_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "compilationUnit"
	// OSQL.g:33:1: compilationUnit : orexpression EOF ;
	public final OSQLParser.compilationUnit_return compilationUnit() throws RecognitionException {
		OSQLParser.compilationUnit_return retval = new OSQLParser.compilationUnit_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token EOF2=null;
		ParserRuleReturnScope orexpression1 =null;

		Object EOF2_tree=null;

		try {
			// OSQL.g:33:17: ( orexpression EOF )
			// OSQL.g:33:19: orexpression EOF
			{
			root_0 = (Object)adaptor.nil();


			pushFollow(FOLLOW_orexpression_in_compilationUnit181);
			orexpression1=orexpression();
			state._fsp--;

			adaptor.addChild(root_0, orexpression1.getTree());

			EOF2=(Token)match(input,EOF,FOLLOW_EOF_in_compilationUnit183); 
			EOF2_tree = (Object)adaptor.create(EOF2);
			adaptor.addChild(root_0, EOF2_tree);

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "compilationUnit"


	public static class orexpression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "orexpression"
	// OSQL.g:35:1: orexpression : andexpression ( 'or' ^ andexpression )* ;
	public final OSQLParser.orexpression_return orexpression() throws RecognitionException {
		OSQLParser.orexpression_return retval = new OSQLParser.orexpression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal4=null;
		ParserRuleReturnScope andexpression3 =null;
		ParserRuleReturnScope andexpression5 =null;

		Object string_literal4_tree=null;

		try {
			// OSQL.g:36:5: ( andexpression ( 'or' ^ andexpression )* )
			// OSQL.g:36:9: andexpression ( 'or' ^ andexpression )*
			{
			root_0 = (Object)adaptor.nil();


			pushFollow(FOLLOW_andexpression_in_orexpression197);
			andexpression3=andexpression();
			state._fsp--;

			adaptor.addChild(root_0, andexpression3.getTree());

			// OSQL.g:36:23: ( 'or' ^ andexpression )*
			loop1:
			while (true) {
				int alt1=2;
				int LA1_0 = input.LA(1);
				if ( (LA1_0==14) ) {
					alt1=1;
				}

				switch (alt1) {
				case 1 :
					// OSQL.g:36:24: 'or' ^ andexpression
					{
					string_literal4=(Token)match(input,14,FOLLOW_14_in_orexpression200); 
					string_literal4_tree = (Object)adaptor.create(string_literal4);
					root_0 = (Object)adaptor.becomeRoot(string_literal4_tree, root_0);

					pushFollow(FOLLOW_andexpression_in_orexpression203);
					andexpression5=andexpression();
					state._fsp--;

					adaptor.addChild(root_0, andexpression5.getTree());

					}
					break;

				default :
					break loop1;
				}
			}

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "orexpression"


	public static class andexpression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "andexpression"
	// OSQL.g:39:1: andexpression : notexpression ( 'and' ^ notexpression )* ;
	public final OSQLParser.andexpression_return andexpression() throws RecognitionException {
		OSQLParser.andexpression_return retval = new OSQLParser.andexpression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal7=null;
		ParserRuleReturnScope notexpression6 =null;
		ParserRuleReturnScope notexpression8 =null;

		Object string_literal7_tree=null;

		try {
			// OSQL.g:40:5: ( notexpression ( 'and' ^ notexpression )* )
			// OSQL.g:40:7: notexpression ( 'and' ^ notexpression )*
			{
			root_0 = (Object)adaptor.nil();


			pushFollow(FOLLOW_notexpression_in_andexpression222);
			notexpression6=notexpression();
			state._fsp--;

			adaptor.addChild(root_0, notexpression6.getTree());

			// OSQL.g:40:21: ( 'and' ^ notexpression )*
			loop2:
			while (true) {
				int alt2=2;
				int LA2_0 = input.LA(1);
				if ( (LA2_0==12) ) {
					alt2=1;
				}

				switch (alt2) {
				case 1 :
					// OSQL.g:40:22: 'and' ^ notexpression
					{
					string_literal7=(Token)match(input,12,FOLLOW_12_in_andexpression225); 
					string_literal7_tree = (Object)adaptor.create(string_literal7);
					root_0 = (Object)adaptor.becomeRoot(string_literal7_tree, root_0);

					pushFollow(FOLLOW_notexpression_in_andexpression228);
					notexpression8=notexpression();
					state._fsp--;

					adaptor.addChild(root_0, notexpression8.getTree());

					}
					break;

				default :
					break loop2;
				}
			}

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "andexpression"


	public static class notexpression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "notexpression"
	// OSQL.g:43:1: notexpression : ( 'not' ^)? atom ;
	public final OSQLParser.notexpression_return notexpression() throws RecognitionException {
		OSQLParser.notexpression_return retval = new OSQLParser.notexpression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal9=null;
		ParserRuleReturnScope atom10 =null;

		Object string_literal9_tree=null;

		try {
			// OSQL.g:44:5: ( ( 'not' ^)? atom )
			// OSQL.g:44:7: ( 'not' ^)? atom
			{
			root_0 = (Object)adaptor.nil();


			// OSQL.g:44:7: ( 'not' ^)?
			int alt3=2;
			int LA3_0 = input.LA(1);
			if ( (LA3_0==13) ) {
				alt3=1;
			}
			switch (alt3) {
				case 1 :
					// OSQL.g:44:8: 'not' ^
					{
					string_literal9=(Token)match(input,13,FOLLOW_13_in_notexpression248); 
					string_literal9_tree = (Object)adaptor.create(string_literal9);
					root_0 = (Object)adaptor.becomeRoot(string_literal9_tree, root_0);

					}
					break;

			}

			pushFollow(FOLLOW_atom_in_notexpression253);
			atom10=atom();
			state._fsp--;

			adaptor.addChild(root_0, atom10.getTree());

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "notexpression"


	public static class atom_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "atom"
	// OSQL.g:47:1: atom : ( ( WORD | QUOTED_STRING ) ( OPERATOR ^ ( INT | WORD | QUOTED_STRING ) )? | LEFT_PAREN ! orexpression RIGHT_PAREN !);
	public final OSQLParser.atom_return atom() throws RecognitionException {
		OSQLParser.atom_return retval = new OSQLParser.atom_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token set11=null;
		Token OPERATOR12=null;
		Token set13=null;
		Token LEFT_PAREN14=null;
		Token RIGHT_PAREN16=null;
		ParserRuleReturnScope orexpression15 =null;

		Object set11_tree=null;
		Object OPERATOR12_tree=null;
		Object set13_tree=null;
		Object LEFT_PAREN14_tree=null;
		Object RIGHT_PAREN16_tree=null;

		try {
			// OSQL.g:48:5: ( ( WORD | QUOTED_STRING ) ( OPERATOR ^ ( INT | WORD | QUOTED_STRING ) )? | LEFT_PAREN ! orexpression RIGHT_PAREN !)
			int alt5=2;
			int LA5_0 = input.LA(1);
			if ( (LA5_0==QUOTED_STRING||LA5_0==WORD) ) {
				alt5=1;
			}
			else if ( (LA5_0==LEFT_PAREN) ) {
				alt5=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 5, 0, input);
				throw nvae;
			}

			switch (alt5) {
				case 1 :
					// OSQL.g:48:7: ( WORD | QUOTED_STRING ) ( OPERATOR ^ ( INT | WORD | QUOTED_STRING ) )?
					{
					root_0 = (Object)adaptor.nil();


					set11=input.LT(1);
					if ( input.LA(1)==QUOTED_STRING||input.LA(1)==WORD ) {
						input.consume();
						adaptor.addChild(root_0, (Object)adaptor.create(set11));
						state.errorRecovery=false;
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					// OSQL.g:48:28: ( OPERATOR ^ ( INT | WORD | QUOTED_STRING ) )?
					int alt4=2;
					int LA4_0 = input.LA(1);
					if ( (LA4_0==OPERATOR) ) {
						alt4=1;
					}
					switch (alt4) {
						case 1 :
							// OSQL.g:48:29: OPERATOR ^ ( INT | WORD | QUOTED_STRING )
							{
							OPERATOR12=(Token)match(input,OPERATOR,FOLLOW_OPERATOR_in_atom277); 
							OPERATOR12_tree = (Object)adaptor.create(OPERATOR12);
							root_0 = (Object)adaptor.becomeRoot(OPERATOR12_tree, root_0);

							set13=input.LT(1);
							if ( input.LA(1)==INT||input.LA(1)==QUOTED_STRING||input.LA(1)==WORD ) {
								input.consume();
								adaptor.addChild(root_0, (Object)adaptor.create(set13));
								state.errorRecovery=false;
							}
							else {
								MismatchedSetException mse = new MismatchedSetException(null,input);
								throw mse;
							}
							}
							break;

					}

					}
					break;
				case 2 :
					// OSQL.g:49:7: LEFT_PAREN ! orexpression RIGHT_PAREN !
					{
					root_0 = (Object)adaptor.nil();


					LEFT_PAREN14=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_atom296); 
					pushFollow(FOLLOW_orexpression_in_atom299);
					orexpression15=orexpression();
					state._fsp--;

					adaptor.addChild(root_0, orexpression15.getTree());

					RIGHT_PAREN16=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_atom301); 
					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "atom"

	// Delegated rules



	public static final BitSet FOLLOW_orexpression_in_compilationUnit181 = new BitSet(new long[]{0x0000000000000000L});
	public static final BitSet FOLLOW_EOF_in_compilationUnit183 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_andexpression_in_orexpression197 = new BitSet(new long[]{0x0000000000004002L});
	public static final BitSet FOLLOW_14_in_orexpression200 = new BitSet(new long[]{0x0000000000002940L});
	public static final BitSet FOLLOW_andexpression_in_orexpression203 = new BitSet(new long[]{0x0000000000004002L});
	public static final BitSet FOLLOW_notexpression_in_andexpression222 = new BitSet(new long[]{0x0000000000001002L});
	public static final BitSet FOLLOW_12_in_andexpression225 = new BitSet(new long[]{0x0000000000002940L});
	public static final BitSet FOLLOW_notexpression_in_andexpression228 = new BitSet(new long[]{0x0000000000001002L});
	public static final BitSet FOLLOW_13_in_notexpression248 = new BitSet(new long[]{0x0000000000000940L});
	public static final BitSet FOLLOW_atom_in_notexpression253 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_set_in_atom270 = new BitSet(new long[]{0x0000000000000082L});
	public static final BitSet FOLLOW_OPERATOR_in_atom277 = new BitSet(new long[]{0x0000000000000920L});
	public static final BitSet FOLLOW_set_in_atom280 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LEFT_PAREN_in_atom296 = new BitSet(new long[]{0x0000000000002940L});
	public static final BitSet FOLLOW_orexpression_in_atom299 = new BitSet(new long[]{0x0000000000000200L});
	public static final BitSet FOLLOW_RIGHT_PAREN_in_atom301 = new BitSet(new long[]{0x0000000000000002L});
}
