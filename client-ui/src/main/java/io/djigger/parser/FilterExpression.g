header {
    package io.djigger.parser;
}

class ThreadDumpLexer extends Lexer;

// Words, which include our operators
WORD: ('a'..'z' | 'A'..'Z' | '0'..'9' |'.')+ ;

// Grouping
LEFT_PAREN: '(';
RIGHT_PAREN: ')';

WHITESPACE
    : (' ' | '\t' | '\r' | '\n') { $setType(Token.SKIP); }
    ;

class ThreadDumpParser extends Parser;

options {
        buildAST=true;
}

orexpression
    :   andexpression ("or"^ andexpression)*
    ;

andexpression
    : notexpression ("and"^ notexpression)*
    ;

notexpression
    : ("not"^)? atom
    ;

atom
    : WORD
    | LEFT_PAREN! orexpression RIGHT_PAREN!
    ;

class FilterExpressionTreeParser extends TreeParser;

{
    private AtomicFilterFactory factory;

    public void setFilterFactory(AtomicFilterFactory factory)
    {
        this.factory = factory;
    }
}

cond returns [Filter r]
{
    Filter a, b;
    r = null;
}
    : #("and" a=cond b=cond) {
        r = new AndExpression(a, b);
    }
    | #("or" a=cond b=cond) {
        r = new OrExpression(a, b);
    }
    | #("not" a=cond) {
        r = new NotExpression(a);
    }
    | c:WORD {
        r = factory.createFilter(c.getText());
    }
    ;
