tree grammar OSQLToMongoQuery;

options {
  ASTLabelType = CommonTree;
  tokenVocab = OSQL;
}

@header {
	package io.djigger.ql;
	import static com.mongodb.client.model.Filters.*;
	import org.bson.Document;
	import org.bson.conversions.Bson;
}

@members {
    private StringBuilder builder;

    public void setBuilder(StringBuilder builder)
    {
        this.builder = builder;
    }
}

buildQuery returns [Bson r]
	: ^('and' a=buildQuery b=buildQuery) {
		$r = and($a.r, $b.r);
    }
    | ^('or' a=buildQuery b=buildQuery) {
    	$r = or($a.r, $b.r);
    }
    | ^('not' a=buildQuery) {
        $r = not($a.r);
    }
    | ^(op=OPERATOR att=(WORD|QUOTED_STRING) val=(WORD|QUOTED_STRING)) {
    	if($op.text.equals("=")) 
    		$r = new Document($att.text, $val.text);
    	else if ($op.text.equals("~"))
    		$r = regex($att.text,$val.text); 
    } 
    | ^(op=OPERATOR att=(WORD|QUOTED_STRING) val=INT) {
    	$r = new Document($att.text, $val.text);
    }     
    | c=(WORD|QUOTED_STRING) {
        $r = null;
    }
    ;
