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
// $ANTLR 3.5.2 OSQLTreeParser.g 2016-03-18 22:05:50

	package io.djigger.ql;


import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class OSQLTreeParser extends TreeParser {
	public static final String[] tokenNames = new String[] {
		"<invalid>", "<EOR>", "<DOWN>", "<UP>", "ESCAPED_QUOTE", "INT", "LEFT_PAREN", 
		"OPERATOR", "QUOTED_STRING", "RIGHT_PAREN", "WHITESPACE", "WORD", "'and'", 
		"'not'", "'or'", "CommonTree", "VALUE"
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
	public static final int CommonTree=15;
	public static final int VALUE=16;

	// delegates
	public TreeParser[] getDelegates() {
		return new TreeParser[] {};
	}

	// delegators


	public OSQLTreeParser(TreeNodeStream input) {
		this(input, new RecognizerSharedState());
	}
	public OSQLTreeParser(TreeNodeStream input, RecognizerSharedState state) {
		super(input, state);
	}

	@Override public String[] getTokenNames() { return OSQLTreeParser.tokenNames; }
	@Override public String getGrammarFileName() { return "OSQLTreeParser.g"; }


	    private FilterFactory factory;

	    public void setFilterFactory(FilterFactory factory)
	    {
	        this.factory = factory;
	    }



	// $ANTLR start "cond"
	// OSQLTreeParser.g:21:1: cond returns [Filter r] : ( ^( 'and' a= cond b= cond ) | ^( 'or' a= cond b= cond ) | ^( 'not' a= cond ) | ^(op= OPERATOR att= CommonTree val= CommonTree ) |c= ( VALUE ) );
	public final Filter cond() throws RecognitionException {
		Filter r = null;


		CommonTree op=null;
		CommonTree att=null;
		CommonTree val=null;
		CommonTree c=null;
		Filter a =null;
		Filter b =null;

		try {
			// OSQLTreeParser.g:22:2: ( ^( 'and' a= cond b= cond ) | ^( 'or' a= cond b= cond ) | ^( 'not' a= cond ) | ^(op= OPERATOR att= CommonTree val= CommonTree ) |c= ( VALUE ) )
			int alt1=5;
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
				alt1=4;
				}
				break;
			case VALUE:
				{
				alt1=5;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 1, 0, input);
				throw nvae;
			}
			switch (alt1) {
				case 1 :
					// OSQLTreeParser.g:22:4: ^( 'and' a= cond b= cond )
					{
					match(input,12,FOLLOW_12_in_cond53); 
					match(input, Token.DOWN, null); 
					pushFollow(FOLLOW_cond_in_cond57);
					a=cond();
					state._fsp--;

					pushFollow(FOLLOW_cond_in_cond61);
					b=cond();
					state._fsp--;

					match(input, Token.UP, null); 


					        r = new AndExpression(a, b);
					    
					}
					break;
				case 2 :
					// OSQLTreeParser.g:25:7: ^( 'or' a= cond b= cond )
					{
					match(input,14,FOLLOW_14_in_cond73); 
					match(input, Token.DOWN, null); 
					pushFollow(FOLLOW_cond_in_cond77);
					a=cond();
					state._fsp--;

					pushFollow(FOLLOW_cond_in_cond81);
					b=cond();
					state._fsp--;

					match(input, Token.UP, null); 


					        r = new OrExpression(a, b);
					    
					}
					break;
				case 3 :
					// OSQLTreeParser.g:28:7: ^( 'not' a= cond )
					{
					match(input,13,FOLLOW_13_in_cond93); 
					match(input, Token.DOWN, null); 
					pushFollow(FOLLOW_cond_in_cond97);
					a=cond();
					state._fsp--;

					match(input, Token.UP, null); 


					        r = new NotExpression(a);
					    
					}
					break;
				case 4 :
					// OSQLTreeParser.g:31:7: ^(op= OPERATOR att= CommonTree val= CommonTree )
					{
					op=(CommonTree)match(input,OPERATOR,FOLLOW_OPERATOR_in_cond111); 
					match(input, Token.DOWN, null); 
					att=(CommonTree)match(input,CommonTree,FOLLOW_CommonTree_in_cond115); 
					val=(CommonTree)match(input,CommonTree,FOLLOW_CommonTree_in_cond119); 
					match(input, Token.UP, null); 


					        r = factory.createAttributeFilter((op!=null?op.getText():null),(att!=null?att.getText():null),(val!=null?val.getText():null));
					    
					}
					break;
				case 5 :
					// OSQLTreeParser.g:34:7: c= ( VALUE )
					{
					// OSQLTreeParser.g:34:9: ( VALUE )
					// OSQLTreeParser.g:34:10: VALUE
					{
					c=(CommonTree)match(input,VALUE,FOLLOW_VALUE_in_cond137); 
					}


					        r = factory.createFullTextFilter((c!=null?c.getText():null));
					    
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
	// $ANTLR end "cond"

	// Delegated rules



	public static final BitSet FOLLOW_12_in_cond53 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_cond_in_cond57 = new BitSet(new long[]{0x0000000000017080L});
	public static final BitSet FOLLOW_cond_in_cond61 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_14_in_cond73 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_cond_in_cond77 = new BitSet(new long[]{0x0000000000017080L});
	public static final BitSet FOLLOW_cond_in_cond81 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_13_in_cond93 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_cond_in_cond97 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_OPERATOR_in_cond111 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_CommonTree_in_cond115 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_CommonTree_in_cond119 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_VALUE_in_cond137 = new BitSet(new long[]{0x0000000000000002L});
}
