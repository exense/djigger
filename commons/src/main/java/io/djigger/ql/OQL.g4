grammar OQL;

@header {
    package io.djigger.ql;
}

query
    : orexpression EOF
    ;
    
orexpression
    :   andexpression ('or' andexpression)*
    ;

andexpression
    : notexpression ('and' notexpression)*
    ;

notexpression
    : ('not')? WORD
    ;    

WORD: ('a'..'z' | 'A'..'Z' | '0'..'9' |'.'|'$')+ ;

WS
    : (' '
    | '\t'
    | '\n'
    | '\r')+ ->skip
    ;