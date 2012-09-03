grammar Aql;

options {
	output=AST;
}


tokens {
	ID;
	AND='&';
	OR='|';
	EQ='=';
	NEQ='!=';
	EXPR;
}

@parser::header {package annis.ql;}
@lexer::header {package annis.ql;}


start 
	: expr^
	;


ID  :	('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*
    ;

fragment
HEX_DIGIT : ('0'..'9'|'a'..'f'|'A'..'F') ;

fragment
UNICODE_ESC
    :   '\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
    ;

fragment
ESC_SEQ
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
    |   UNICODE_ESC
    |   OCTAL_ESC
    ;

fragment
OCTAL_ESC
    :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7')
    ;
    
text_spec 
	:	'"'^ ( ESC_SEQ | ~('"') )* '"'!
	|	'/'^ ( ESC_SEQ | ~('/') )* '/'!
	;

qName
	: namespace=ID ':' name=ID -> ^($name $namespace)
	| ID^
	;

term
	: qName^
	|	qName EQ text_spec -> ^('=' qName text_spec)
	| qName NEQ text_spec -> ^('=' qName text_spec)
	| '('! expr^ ')'!
	;
	
	
or_tail
	:	OR! term^
	;

and_tail 
	: AND! term^
	;	


expr
	: term
		(	and_tail+ -> ^('&' term and_tail+)
		|	or_tail+ -> ^('|' term or_tail+)
		)?
	;

	
	