package io.djigger.ql;

import io.djigger.ql.OQLParser.AndexpressionContext;
import io.djigger.ql.OQLParser.NotexpressionContext;
import io.djigger.ql.OQLParser.OrexpressionContext;
import io.djigger.ql.OQLParser.QueryContext;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

public class OQLFilterFactory {

	public static void getFilter(String expression) {
		QueryContext context = parse(expression);
		
		ParseTreeWalker walker = new ParseTreeWalker();
		OQLListener listener = new OQLListener() {
			
			@Override
			public void visitTerminal(TerminalNode arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void visitErrorNode(ErrorNode arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void exitEveryRule(ParserRuleContext arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void enterEveryRule(ParserRuleContext arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void exitQuery(QueryContext ctx) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void enterQuery(QueryContext ctx) {
				
			}

			@Override
			public void enterOrexpression(OrexpressionContext ctx) {
				System.out.println(ctx.getText()+ "-");
			}

			@Override
			public void exitOrexpression(OrexpressionContext ctx) {
			}

			@Override
			public void enterAndexpression(AndexpressionContext ctx) {
				// TODO Auto-generated method stub
				System.out.println(ctx.getText() + "-");
				
			}

			@Override
			public void exitAndexpression(AndexpressionContext ctx) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void enterNotexpression(NotexpressionContext ctx) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void exitNotexpression(NotexpressionContext ctx) {
				// TODO Auto-generated method stub
				
			}
		};
	    walker.walk(listener, context);
	}
	
	private static QueryContext parse(String expression) {
		OQLLexer lexer = new OQLLexer(new ANTLRInputStream(expression));
		OQLParser parser = new OQLParser(new CommonTokenStream(lexer));
		parser.addErrorListener(new BaseErrorListener() {
	        @Override
	        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
	            throw new IllegalStateException("failed to parse at line " + line + " due to " + msg, e);
	        }
	    });
		return parser.query();
		
	}
}
