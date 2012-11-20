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
	COMMON_ANCESTOR='$*';
	ROOT=':root';
	ARITY=':arity';
	TOKEN_ARITY=':tokenarity';
	DIGITS;
	COMMA=',';
	STAR='*';
	REF;
	RANGE;
	FROM_TO;
	DOM;
}

@parser::header {package annis.ql;}
@lexer::header {package annis.ql;}


start 
	: expr^
	;

WS  :   ( ' ' | '\t' | '\r' | '\n' )+ {$channel=HIDDEN; } ;  

ID  :	('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*
    ;
    
REF
	:	'#' ( '0' .. '9' )+
	;

DIGITS : ('0'..'9')+;


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
	:	namespace=ID ':' name=ID -> ^($name $namespace)
	|	TOK^
	|	ID^
	;

edge_anno
	:	'[' qName EQ text_spec ']' -> ^(EQ qName text_spec)
	;

precedence
	: REF PRECEDENCE REF -> ^(PRECEDENCE  TOK ^(FROM_TO REF REF) ^(RANGE))
	| REF PRECEDENCE STAR REF -> ^(PRECEDENCE TOK ^(FROM_TO REF REF) ^(RANGE STAR))
	| REF PRECEDENCE min=DIGITS (COMMA max=DIGITS)? REF -> ^(PRECEDENCE TOK ^(FROM_TO REF REF) ^(RANGE $min $max?))
	| REF PRECEDENCE layer=ID REF -> ^(PRECEDENCE  $layer ^(FROM_TO REF REF) ^(RANGE))
	| REF PRECEDENCE layer=ID STAR REF -> ^(PRECEDENCE $layer ^(FROM_TO REF REF) ^(RANGE STAR))
	| REF PRECEDENCE layer=ID COMMA? min=DIGITS (COMMA max=DIGITS)? REF -> ^(PRECEDENCE $layer ^(FROM_TO REF REF) ^(RANGE $min $max?))
	;

dominance
	: REF DOMINANCE REF -> ^(DOMINANCE ^(FROM_TO REF REF) ^(RANGE))
	| REF DOMINANCE STAR REF -> ^(DOMINANCE ^(FROM_TO REF REF) ^(RANGE STAR))
	| REF DOMINANCE min=DIGITS (COMMA max=DIGITS)? REF -> ^(DOMINANCE ^(FROM_TO REF REF) ^(RANGE $min $max?))
	;
	
pointing
	: REF POINTING label=ID REF -> ^(POINTING  $label ^(FROM_TO REF REF) ^(RANGE))
	| REF POINTING label=ID STAR REF -> ^(POINTING $label ^(FROM_TO REF REF) ^(RANGE STAR))
	| REF PRECEDENCE label=ID COMMA? min=DIGITS (COMMA max=DIGITS)? REF -> ^(POINTING $label ^(FROM_TO REF REF) ^(RANGE $min $max?))
	;

binary_linguistic_term
	:	precedence
	|	REF IDENT_COV^ REF
	|	REF LEFT_ALIGN^ REF
	|	REF RIGHT_ALIGN^ REF
	|	REF INCLUSION^ REF
	|	REF OVERLAP^ REF
	|	REF RIGHT_OVERLAP^ REF
	| 	REF LEFT_OVERLAP^ REF
	|	dominance
	|	REF LEFT_CHILD^ REF
	|	REF RIGHT_CHILD^ REF
	|	pointing
	|	REF COMMON_PARENT^ REF
	|	REF COMMON_ANCESTOR^ REF
	;
	
unary_linguistic_term
	:	REF ROOT^
	|	REF ARITY^ EQ! DIGITS
	|	REF TOKEN_ARITY^ EQ! DIGITS
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

	
	