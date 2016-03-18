tree grammar OSQLTreeParser;

options {
  ASTLabelType = CommonTree;
  tokenVocab = OSQL;
}

@header {
	package io.djigger.ql;
}

@members {
    private FilterFactory factory;

    public void setFilterFactory(FilterFactory factory)
    {
        this.factory = factory;
    }
}

cond returns [Filter r]
	: ^('and' a=cond b=cond) {
        $r = new AndExpression($a.r, $b.r);
    }
    | ^('or' a=cond b=cond) {
        $r = new OrExpression($a.r, $b.r);
    }
    | ^('not' a=cond) {
        $r = new NotExpression($a.r);
    }
    | ^(op=OPERATOR att=CommonTree val=CommonTree) {
        $r = factory.createAttributeFilter($op.text,$att.text,$val.text);
    }    
    | c=(VALUE) {
        $r = factory.createFullTextFilter($c.text);
    }
    ;
