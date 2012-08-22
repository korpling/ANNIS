grammar Aql;

options {
	output=AST;
}

@parser::header {package annis.ql;}
@lexer::header {package annis.ql;}

start 
	: (expr)+;

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
	: namespace+=ID ':' name+=ID -> ^($name $namespace)
	| ID^
	;

expr 
	: qName
	|	qName '=' text_spec -> ^('=' qName text_spec)
	| qName '!=' text_spec -> ^('=' qName text_spec)
	|	'(' expr+ ')' -> ^('(' expr+)
	|	'&'^ expr
	| '|'^ expr
	;
	
	