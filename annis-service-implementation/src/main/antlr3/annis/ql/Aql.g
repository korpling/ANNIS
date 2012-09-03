grammar Aql;

options {
	output=AST;
}


tokens {
	TOK='tok';
	ID;
	AND='&';
	OR='|';
	EQ='=';
	NEQ='!=';
	DOMINANCE='>';
	POINTING='->';
	PRECEDENCE='.';
	IDENT_COV='_=_';
	INCLUSION='_i_';
	OVERLAP='_o_';
	LEFT_ALIGN='_l_';
	RIGHT_ALIGN='_r_';
	LEFT_OVERLAP='_ol_';
	RIGHT_OVERLAP='_or_';
	LEFT_CHILD='>@l';
	RIGHT_CHILD='>@r';
	COMMON_PARENT='$';
	ROOT=':root';
	ARITY=':arity';
	TOKEN_ARITY=':tokenarity';
	REF;
	DIGITS;
}

@parser::header {package annis.ql;}
@lexer::header {package annis.ql;}


start 
	: expr^
	;


ID  :	('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*
    ;
    
REF
	:	'#' ( '0' .. '9' )+
	;

fragment
HEX_DIGIT : ('0'..'9'|'a'..'f'|'A'..'F') ;

fragment
UNICODE_ESC
    :   '\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
    ;

fragment
ESC_SEQ
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\'|'/')
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
	| TOK^
	| ID^
	;

fragment
binary_linguistic_term
	:	REF IDENT_COV^ REF
	|	REF LEFT_ALIGN^ REF
	|	REF RIGHT_ALIGN^ REF
	|	REF INCLUSION^ REF
	|	REF OVERLAP^ REF
	|	REF RIGHT_OVERLAP^ REF
	| 	REF LEFT_OVERLAP^REF
	|	REF DOMINANCE^ REF
	|	REF POINTING^ REF
	|	REF COMMON_PARENT^ REF
	;
	
fragment
unary_linguistic_term
	:	REF ROOT^
	|	REF ARITY^
	|	REF TOKEN_ARITY^
	;

term
	:	qName^
	|	text_spec -> ^(EQ TOK text_spec) // shortcut for tok="..."
	|	qName EQ text_spec -> ^('=' qName text_spec)
	|	qName NEQ text_spec -> ^('=' qName text_spec)
	|	'('! expr^ ')'!
	|	unary_linguistic_term
	|	binary_linguistic_term
	;
	
	
or_tail
	:	OR! term^
	;

and_tail 
	:	AND! term^
	;	


expr
	: (term -> term )
		(	and_tail+ -> ^(AND term and_tail+)
		|	or_tail+ -> ^(OR term or_tail+)
		)?
	;

	
	