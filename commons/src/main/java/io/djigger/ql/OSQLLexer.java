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
// $ANTLR 3.5.2 OSQL.g 2016-03-18 22:05:50

    package io.djigger.ql;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class OSQLLexer extends Lexer {
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
	// delegators
	public Lexer[] getDelegates() {
		return new Lexer[] {};
	}

	public OSQLLexer() {} 
	public OSQLLexer(CharStream input) {
		this(input, new RecognizerSharedState());
	}
	public OSQLLexer(CharStream input, RecognizerSharedState state) {
		super(input,state);
	}
	@Override public String getGrammarFileName() { return "OSQL.g"; }

	// $ANTLR start "T__12"
	public final void mT__12() throws RecognitionException {
		try {
			int _type = T__12;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// OSQL.g:6:7: ( 'and' )
			// OSQL.g:6:9: 'and'
			{
			match("and"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__12"

	// $ANTLR start "T__13"
	public final void mT__13() throws RecognitionException {
		try {
			int _type = T__13;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// OSQL.g:7:7: ( 'not' )
			// OSQL.g:7:9: 'not'
			{
			match("not"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__13"

	// $ANTLR start "T__14"
	public final void mT__14() throws RecognitionException {
		try {
			int _type = T__14;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// OSQL.g:8:7: ( 'or' )
			// OSQL.g:8:9: 'or'
			{
			match("or"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__14"

	// $ANTLR start "INT"
	public final void mINT() throws RecognitionException {
		try {
			int _type = INT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// OSQL.g:15:4: ( ( '0' .. '9' )+ )
			// OSQL.g:15:6: ( '0' .. '9' )+
			{
			// OSQL.g:15:6: ( '0' .. '9' )+
			int cnt1=0;
			loop1:
			while (true) {
				int alt1=2;
				int LA1_0 = input.LA(1);
				if ( ((LA1_0 >= '0' && LA1_0 <= '9')) ) {
					alt1=1;
				}

				switch (alt1) {
				case 1 :
					// OSQL.g:
					{
					if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					if ( cnt1 >= 1 ) break loop1;
					EarlyExitException eee = new EarlyExitException(1, input);
					throw eee;
				}
				cnt1++;
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "INT"

	// $ANTLR start "WORD"
	public final void mWORD() throws RecognitionException {
		try {
			int _type = WORD;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// OSQL.g:18:5: ( ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '.' | '$' )+ )
			// OSQL.g:18:7: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '.' | '$' )+
			{
			// OSQL.g:18:7: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '.' | '$' )+
			int cnt2=0;
			loop2:
			while (true) {
				int alt2=2;
				int LA2_0 = input.LA(1);
				if ( (LA2_0=='$'||LA2_0=='.'||(LA2_0 >= '0' && LA2_0 <= '9')||(LA2_0 >= 'A' && LA2_0 <= 'Z')||(LA2_0 >= 'a' && LA2_0 <= 'z')) ) {
					alt2=1;
				}

				switch (alt2) {
				case 1 :
					// OSQL.g:
					{
					if ( input.LA(1)=='$'||input.LA(1)=='.'||(input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					if ( cnt2 >= 1 ) break loop2;
					EarlyExitException eee = new EarlyExitException(2, input);
					throw eee;
				}
				cnt2++;
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "WORD"

	// $ANTLR start "ESCAPED_QUOTE"
	public final void mESCAPED_QUOTE() throws RecognitionException {
		try {
			int _type = ESCAPED_QUOTE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// OSQL.g:20:14: ( '\\\\\"' )
			// OSQL.g:20:16: '\\\\\"'
			{
			match("\\\""); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ESCAPED_QUOTE"

	// $ANTLR start "QUOTED_STRING"
	public final void mQUOTED_STRING() throws RecognitionException {
		try {
			int _type = QUOTED_STRING;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// OSQL.g:21:14: ( '\"' ( ESCAPED_QUOTE |~ ( '\\n' | '\\r' | '\"' ) )* '\"' )
			// OSQL.g:21:16: '\"' ( ESCAPED_QUOTE |~ ( '\\n' | '\\r' | '\"' ) )* '\"'
			{
			match('\"'); 
			// OSQL.g:21:20: ( ESCAPED_QUOTE |~ ( '\\n' | '\\r' | '\"' ) )*
			loop3:
			while (true) {
				int alt3=3;
				int LA3_0 = input.LA(1);
				if ( (LA3_0=='\\') ) {
					int LA3_2 = input.LA(2);
					if ( (LA3_2=='\"') ) {
						int LA3_4 = input.LA(3);
						if ( ((LA3_4 >= '\u0000' && LA3_4 <= '\t')||(LA3_4 >= '\u000B' && LA3_4 <= '\f')||(LA3_4 >= '\u000E' && LA3_4 <= '\uFFFF')) ) {
							alt3=1;
						}
						else {
							alt3=2;
						}

					}
					else if ( ((LA3_2 >= '\u0000' && LA3_2 <= '\t')||(LA3_2 >= '\u000B' && LA3_2 <= '\f')||(LA3_2 >= '\u000E' && LA3_2 <= '!')||(LA3_2 >= '#' && LA3_2 <= '\uFFFF')) ) {
						alt3=2;
					}

				}
				else if ( ((LA3_0 >= '\u0000' && LA3_0 <= '\t')||(LA3_0 >= '\u000B' && LA3_0 <= '\f')||(LA3_0 >= '\u000E' && LA3_0 <= '!')||(LA3_0 >= '#' && LA3_0 <= '[')||(LA3_0 >= ']' && LA3_0 <= '\uFFFF')) ) {
					alt3=2;
				}

				switch (alt3) {
				case 1 :
					// OSQL.g:21:22: ESCAPED_QUOTE
					{
					mESCAPED_QUOTE(); 

					}
					break;
				case 2 :
					// OSQL.g:21:38: ~ ( '\\n' | '\\r' | '\"' )
					{
					if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\t')||(input.LA(1) >= '\u000B' && input.LA(1) <= '\f')||(input.LA(1) >= '\u000E' && input.LA(1) <= '!')||(input.LA(1) >= '#' && input.LA(1) <= '\uFFFF') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					break loop3;
				}
			}

			match('\"'); 
			setText(getText().substring(1,getText().length()-1));
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "QUOTED_STRING"

	// $ANTLR start "LEFT_PAREN"
	public final void mLEFT_PAREN() throws RecognitionException {
		try {
			int _type = LEFT_PAREN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// OSQL.g:24:11: ( '(' )
			// OSQL.g:24:13: '('
			{
			match('('); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LEFT_PAREN"

	// $ANTLR start "RIGHT_PAREN"
	public final void mRIGHT_PAREN() throws RecognitionException {
		try {
			int _type = RIGHT_PAREN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// OSQL.g:25:12: ( ')' )
			// OSQL.g:25:14: ')'
			{
			match(')'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "RIGHT_PAREN"

	// $ANTLR start "OPERATOR"
	public final void mOPERATOR() throws RecognitionException {
		try {
			int _type = OPERATOR;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// OSQL.g:27:9: ( ( '=' | '~' | '>' | '<' ) )
			// OSQL.g:
			{
			if ( (input.LA(1) >= '<' && input.LA(1) <= '>')||input.LA(1)=='~' ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "OPERATOR"

	// $ANTLR start "WHITESPACE"
	public final void mWHITESPACE() throws RecognitionException {
		try {
			int _type = WHITESPACE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// OSQL.g:30:5: ( ( ' ' | '\\t' | '\\r' | '\\n' ) )
			// OSQL.g:30:7: ( ' ' | '\\t' | '\\r' | '\\n' )
			{
			if ( (input.LA(1) >= '\t' && input.LA(1) <= '\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			 _channel=HIDDEN; 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "WHITESPACE"

	@Override
	public void mTokens() throws RecognitionException {
		// OSQL.g:1:8: ( T__12 | T__13 | T__14 | INT | WORD | ESCAPED_QUOTE | QUOTED_STRING | LEFT_PAREN | RIGHT_PAREN | OPERATOR | WHITESPACE )
		int alt4=11;
		alt4 = dfa4.predict(input);
		switch (alt4) {
			case 1 :
				// OSQL.g:1:10: T__12
				{
				mT__12(); 

				}
				break;
			case 2 :
				// OSQL.g:1:16: T__13
				{
				mT__13(); 

				}
				break;
			case 3 :
				// OSQL.g:1:22: T__14
				{
				mT__14(); 

				}
				break;
			case 4 :
				// OSQL.g:1:28: INT
				{
				mINT(); 

				}
				break;
			case 5 :
				// OSQL.g:1:32: WORD
				{
				mWORD(); 

				}
				break;
			case 6 :
				// OSQL.g:1:37: ESCAPED_QUOTE
				{
				mESCAPED_QUOTE(); 

				}
				break;
			case 7 :
				// OSQL.g:1:51: QUOTED_STRING
				{
				mQUOTED_STRING(); 

				}
				break;
			case 8 :
				// OSQL.g:1:65: LEFT_PAREN
				{
				mLEFT_PAREN(); 

				}
				break;
			case 9 :
				// OSQL.g:1:76: RIGHT_PAREN
				{
				mRIGHT_PAREN(); 

				}
				break;
			case 10 :
				// OSQL.g:1:88: OPERATOR
				{
				mOPERATOR(); 

				}
				break;
			case 11 :
				// OSQL.g:1:97: WHITESPACE
				{
				mWHITESPACE(); 

				}
				break;

		}
	}


	protected DFA4 dfa4 = new DFA4(this);
	static final String DFA4_eotS =
		"\1\uffff\3\5\1\17\7\uffff\2\5\1\22\1\uffff\1\23\1\24\3\uffff";
	static final String DFA4_eofS =
		"\25\uffff";
	static final String DFA4_minS =
		"\1\11\1\156\1\157\1\162\1\44\7\uffff\1\144\1\164\1\44\1\uffff\2\44\3\uffff";
	static final String DFA4_maxS =
		"\1\176\1\156\1\157\1\162\1\172\7\uffff\1\144\1\164\1\172\1\uffff\2\172"+
		"\3\uffff";
	static final String DFA4_acceptS =
		"\5\uffff\1\5\1\6\1\7\1\10\1\11\1\12\1\13\3\uffff\1\4\2\uffff\1\3\1\1\1"+
		"\2";
	static final String DFA4_specialS =
		"\25\uffff}>";
	static final String[] DFA4_transitionS = {
			"\2\13\2\uffff\1\13\22\uffff\1\13\1\uffff\1\7\1\uffff\1\5\3\uffff\1\10"+
			"\1\11\4\uffff\1\5\1\uffff\12\4\2\uffff\3\12\2\uffff\32\5\1\uffff\1\6"+
			"\4\uffff\1\1\14\5\1\2\1\3\13\5\3\uffff\1\12",
			"\1\14",
			"\1\15",
			"\1\16",
			"\1\5\11\uffff\1\5\1\uffff\12\4\7\uffff\32\5\6\uffff\32\5",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\20",
			"\1\21",
			"\1\5\11\uffff\1\5\1\uffff\12\5\7\uffff\32\5\6\uffff\32\5",
			"",
			"\1\5\11\uffff\1\5\1\uffff\12\5\7\uffff\32\5\6\uffff\32\5",
			"\1\5\11\uffff\1\5\1\uffff\12\5\7\uffff\32\5\6\uffff\32\5",
			"",
			"",
			""
	};

	static final short[] DFA4_eot = DFA.unpackEncodedString(DFA4_eotS);
	static final short[] DFA4_eof = DFA.unpackEncodedString(DFA4_eofS);
	static final char[] DFA4_min = DFA.unpackEncodedStringToUnsignedChars(DFA4_minS);
	static final char[] DFA4_max = DFA.unpackEncodedStringToUnsignedChars(DFA4_maxS);
	static final short[] DFA4_accept = DFA.unpackEncodedString(DFA4_acceptS);
	static final short[] DFA4_special = DFA.unpackEncodedString(DFA4_specialS);
	static final short[][] DFA4_transition;

	static {
		int numStates = DFA4_transitionS.length;
		DFA4_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA4_transition[i] = DFA.unpackEncodedString(DFA4_transitionS[i]);
		}
	}

	protected class DFA4 extends DFA {

		public DFA4(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 4;
			this.eot = DFA4_eot;
			this.eof = DFA4_eof;
			this.min = DFA4_min;
			this.max = DFA4_max;
			this.accept = DFA4_accept;
			this.special = DFA4_special;
			this.transition = DFA4_transition;
		}
		@Override
		public String getDescription() {
			return "1:1: Tokens : ( T__12 | T__13 | T__14 | INT | WORD | ESCAPED_QUOTE | QUOTED_STRING | LEFT_PAREN | RIGHT_PAREN | OPERATOR | WHITESPACE );";
		}
	}

}
