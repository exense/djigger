package io.djigger.ql;

import static com.mongodb.client.model.Filters.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import io.djigger.ql.OQLParser.AndExprContext;
import io.djigger.ql.OQLParser.EqualityExprContext;
import io.djigger.ql.OQLParser.NonQuotedStringAtomContext;
import io.djigger.ql.OQLParser.NotExprContext;
import io.djigger.ql.OQLParser.OrExprContext;
import io.djigger.ql.OQLParser.ParExprContext;
import io.djigger.ql.OQLParser.StringAtomContext;

public class OQLMongoDBQueryVisitor extends OQLBaseVisitor<Bson>{

	StringBuilder builder = new StringBuilder();

	public OQLMongoDBQueryVisitor() {
		super();
	}

	@Override
	public Bson visitAndExpr(AndExprContext ctx) {
		return and(this.visit(ctx.expr(0)), this.visit(ctx.expr(1)));
	}

	@Override
	public Bson visitEqualityExpr(EqualityExprContext ctx) {
		String op = ctx.op.getText();
		if(op.equals("=")) 
    		return new Document(ctx.expr(0).getText(), ctx.expr(1).getText());
    	else if (op.equals("~"))
    		return regex(ctx.expr(0).getText(), ctx.expr(1).getText()); 
    	else 
    		throw new RuntimeException("Invalid operator: '"+op+"'");
	}

	@Override
	public Bson visitOrExpr(OrExprContext ctx) {
		return or(this.visit(ctx.expr(0)), this.visit(ctx.expr(1)));
	}

	@Override
	public Bson visitNotExpr(NotExprContext ctx) {
		return not(this.visit(ctx.expr()));
	}

	@Override
	public Bson visitParExpr(ParExprContext ctx) {
		return this.visit(ctx.expr());
	}

	@Override
	public Bson visitNonQuotedStringAtom(NonQuotedStringAtomContext ctx) {
		throw new RuntimeException("Missing assignment");
	}

	@Override
	public Bson visitStringAtom(StringAtomContext ctx) {
		throw new RuntimeException("Missing assignment");
	}


}
