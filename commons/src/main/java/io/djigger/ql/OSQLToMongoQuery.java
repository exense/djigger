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
// $ANTLR 3.5.2 OSQLToMongoQuery.g 2016-03-18 22:05:50

	package io.djigger.ql;
	import static com.mongodb.client.model.Filters.*;
	import org.bson.Document;
	import org.bson.conversions.Bson;


import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class OSQLToMongoQuery extends TreeParser {
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
	public TreeParser[] getDelegates() {
		return new TreeParser[] {};
	}

	// delegators


	public OSQLToMongoQuery(TreeNodeStream input) {
		this(input, new RecognizerSharedState());
	}
	public OSQLToMongoQuery(TreeNodeStream input, RecognizerSharedState state) {
		super(input, state);
	}

	@Override public String[] getTokenNames() { return OSQLToMongoQuery.tokenNames; }
	@Override public String getGrammarFileName() { return "OSQLToMongoQuery.g"; }


	    private StringBuilder builder;

	    public void setBuilder(StringBuilder builder)
	    {
	        this.builder = builder;
	    }



	// $ANTLR start "buildQuery"
	// OSQLToMongoQuery.g:24:1: buildQuery returns [Bson r] : ( ^( 'and' a= buildQuery b= buildQuery ) | ^( 'or' a= buildQuery b= buildQuery ) | ^( 'not' a= buildQuery ) | ^(op= OPERATOR att= ( WORD | QUOTED_STRING ) val= ( WORD | QUOTED_STRING ) ) | ^(op= OPERATOR att= ( WORD | QUOTED_STRING ) val= INT ) |c= ( WORD | QUOTED_STRING ) );
	public final Bson buildQuery() throws RecognitionException {
		Bson r = null;


		CommonTree op=null;
		CommonTree att=null;
		CommonTree val=null;
		CommonTree c=null;
		Bson a =null;
		Bson b =null;

		try {
			// OSQLToMongoQuery.g:25:2: ( ^( 'and' a= buildQuery b= buildQuery ) | ^( 'or' a= buildQuery b= buildQuery ) | ^( 'not' a= buildQuery ) | ^(op= OPERATOR att= ( WORD | QUOTED_STRING ) val= ( WORD | QUOTED_STRING ) ) | ^(op= OPERATOR att= ( WORD | QUOTED_STRING ) val= INT ) |c= ( WORD | QUOTED_STRING ) )
			int alt1=6;
			switch ( input.LA(1) ) {
			case 12:
				{
				alt1=1;
				}
				break;
			case 14:
				{
				alt1=2;
				}
				break;
			case 13:
				{
				alt1=3;
				}
				break;
			case OPERATOR:
				{
				int LA1_4 = input.LA(2);
				if ( (LA1_4==DOWN) ) {
					int LA1_6 = input.LA(3);
					if ( (LA1_6==QUOTED_STRING||LA1_6==WORD) ) {
						int LA1_7 = input.LA(4);
						if ( (LA1_7==QUOTED_STRING||LA1_7==WORD) ) {
							alt1=4;
						}
						else if ( (LA1_7==INT) ) {
							alt1=5;
						}

						else {
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 1, 7, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

					}

					else {
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 1, 6, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 1, 4, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case QUOTED_STRING:
			case WORD:
				{
				alt1=6;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 1, 0, input);
				throw nvae;
			}
			switch (alt1) {
				case 1 :
					// OSQLToMongoQuery.g:25:4: ^( 'and' a= buildQuery b= buildQuery )
					{
					match(input,12,FOLLOW_12_in_buildQuery53); 
					match(input, Token.DOWN, null); 
					pushFollow(FOLLOW_buildQuery_in_buildQuery57);
					a=buildQuery();
					state._fsp--;

					pushFollow(FOLLOW_buildQuery_in_buildQuery61);
					b=buildQuery();
					state._fsp--;

					match(input, Token.UP, null); 


							r = and(a, b);
					    
					}
					break;
				case 2 :
					// OSQLToMongoQuery.g:28:7: ^( 'or' a= buildQuery b= buildQuery )
					{
					match(input,14,FOLLOW_14_in_buildQuery73); 
					match(input, Token.DOWN, null); 
					pushFollow(FOLLOW_buildQuery_in_buildQuery77);
					a=buildQuery();
					state._fsp--;

					pushFollow(FOLLOW_buildQuery_in_buildQuery81);
					b=buildQuery();
					state._fsp--;

					match(input, Token.UP, null); 


					    	r = or(a, b);
					    
					}
					break;
				case 3 :
					// OSQLToMongoQuery.g:31:7: ^( 'not' a= buildQuery )
					{
					match(input,13,FOLLOW_13_in_buildQuery93); 
					match(input, Token.DOWN, null); 
					pushFollow(FOLLOW_buildQuery_in_buildQuery97);
					a=buildQuery();
					state._fsp--;

					match(input, Token.UP, null); 


					        r = not(a);
					    
					}
					break;
				case 4 :
					// OSQLToMongoQuery.g:34:7: ^(op= OPERATOR att= ( WORD | QUOTED_STRING ) val= ( WORD | QUOTED_STRING ) )
					{
					op=(CommonTree)match(input,OPERATOR,FOLLOW_OPERATOR_in_buildQuery111); 
					match(input, Token.DOWN, null); 
					att=(CommonTree)input.LT(1);
					if ( input.LA(1)==QUOTED_STRING||input.LA(1)==WORD ) {
						input.consume();
						state.errorRecovery=false;
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					val=(CommonTree)input.LT(1);
					if ( input.LA(1)==QUOTED_STRING||input.LA(1)==WORD ) {
						input.consume();
						state.errorRecovery=false;
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					match(input, Token.UP, null); 


					    	if((op!=null?op.getText():null).equals("=")) 
					    		r = new Document((att!=null?att.getText():null), (val!=null?val.getText():null));
					    	else if ((op!=null?op.getText():null).equals("~"))
					    		r = regex((att!=null?att.getText():null),(val!=null?val.getText():null)); 
					    
					}
					break;
				case 5 :
					// OSQLToMongoQuery.g:40:7: ^(op= OPERATOR att= ( WORD | QUOTED_STRING ) val= INT )
					{
					op=(CommonTree)match(input,OPERATOR,FOLLOW_OPERATOR_in_buildQuery142); 
					match(input, Token.DOWN, null); 
					att=(CommonTree)input.LT(1);
					if ( input.LA(1)==QUOTED_STRING||input.LA(1)==WORD ) {
						input.consume();
						state.errorRecovery=false;
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					val=(CommonTree)match(input,INT,FOLLOW_INT_in_buildQuery154); 
					match(input, Token.UP, null); 


					    	r = new Document((att!=null?att.getText():null), (val!=null?val.getText():null));
					    
					}
					break;
				case 6 :
					// OSQLToMongoQuery.g:43:7: c= ( WORD | QUOTED_STRING )
					{
					c=(CommonTree)input.LT(1);
					if ( input.LA(1)==QUOTED_STRING||input.LA(1)==WORD ) {
						input.consume();
						state.errorRecovery=false;
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}

					        r = null;
					    
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return r;
	}
	// $ANTLR end "buildQuery"

	// Delegated rules



	public static final BitSet FOLLOW_12_in_buildQuery53 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_buildQuery_in_buildQuery57 = new BitSet(new long[]{0x0000000000007980L});
	public static final BitSet FOLLOW_buildQuery_in_buildQuery61 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_14_in_buildQuery73 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_buildQuery_in_buildQuery77 = new BitSet(new long[]{0x0000000000007980L});
	public static final BitSet FOLLOW_buildQuery_in_buildQuery81 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_13_in_buildQuery93 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_buildQuery_in_buildQuery97 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_OPERATOR_in_buildQuery111 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_set_in_buildQuery115 = new BitSet(new long[]{0x0000000000000900L});
	public static final BitSet FOLLOW_set_in_buildQuery123 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_OPERATOR_in_buildQuery142 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_set_in_buildQuery146 = new BitSet(new long[]{0x0000000000000020L});
	public static final BitSet FOLLOW_INT_in_buildQuery154 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_set_in_buildQuery172 = new BitSet(new long[]{0x0000000000000002L});
}
