/*
 * Copyright 2013 SFB 632.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

grammar Aql;

options
{
  language = Java;
}

// additional tokens
tokens {
	RANGE,
	ANNO,
	FROM_TO,
	DOM
}
  
TOK:'tok';
AND:'&';
OR:'|';
EQ:'=';
NEQ:'!=';
DOMINANCE:'>';
POINTING:'->';
PRECEDENCE:'.';
IDENT_COV:'_=_';
INCLUSION:'_i_';
OVERLAP:'_o_';
LEFT_ALIGN:'_l_';
RIGHT_ALIGN:'_r_';
LEFT_OVERLAP:'_ol_';
RIGHT_OVERLAP:'_or_';
LEFT_CHILD:'>@l';
RIGHT_CHILD:'>@r';
COMMON_PARENT:'$';
COMMON_ANCESTOR:'$*';
ROOT:':root';
ARITY:':arity';
TOKEN_ARITY:':tokenarity';
COMMA:',';
STAR:'*';

WS  :   ( ' ' | '\t' | '\r' | '\n' )+ -> skip ;  

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

start 
	: expr EOF
	;

text_spec 
	:	'"' ( ESC_SEQ | ~('"') )* '"'
	|	'/' ( ESC_SEQ | ~('/') )* '/'
	;

qName
	:	namespace=ID ':' name=ID
	|	TOK
	|	ID
	;

edge_anno
	:	 name=qName EQ value=text_spec
	;

edge_spec
	: '[' edge_anno+ ']'
	;


precedence
	: REF PRECEDENCE REF
	| REF PRECEDENCE STAR REF
	| REF PRECEDENCE min=DIGITS (COMMA max=DIGITS)? REF
	| REF PRECEDENCE layer=ID REF
	| REF PRECEDENCE layer=ID STAR REF
	| REF PRECEDENCE layer=ID COMMA? min=DIGITS (COMMA max=DIGITS)? REF
	;

dominance
	: REF DOMINANCE (anno=edge_spec)? REF
	| REF DOMINANCE (anno=edge_spec)? STAR REF
	| REF DOMINANCE (anno=edge_spec)? min=DIGITS (COMMA max=DIGITS)? REF
	;
	
pointing
	: REF POINTING label=ID (anno=edge_spec)? REF
	| REF POINTING label=ID (anno=edge_spec)? STAR REF
	| REF POINTING label=ID (anno=edge_spec)? COMMA? min=DIGITS (COMMA max=DIGITS)? REF
	;

binary_linguistic_term
	:	precedence
	|	REF IDENT_COV REF
	|	REF LEFT_ALIGN REF
	|	REF RIGHT_ALIGN REF
	|	REF INCLUSION REF
	|	REF OVERLAP REF
	|	REF RIGHT_OVERLAP REF
	| REF LEFT_OVERLAP REF
	|	dominance
	|	REF LEFT_CHILD REF
	|	REF RIGHT_CHILD REF
	|	pointing
	|	REF COMMON_PARENT REF
	|	REF COMMON_ANCESTOR REF
	;
	
unary_linguistic_term
	:	REF ROOT
	|	REF ARITY EQ DIGITS
	|	REF TOKEN_ARITY EQ DIGITS
	;

term
	:	qName
	|	text_spec // shortcut for tok="..."
	|	qName EQ text_spec
	|	qName NEQ text_spec
	|	'(' expr ')'
	|	unary_linguistic_term
	|	binary_linguistic_term
	;
	

expr
	: term # Single
  | term (OR term)+ # Or
  | term (AND term)+ # And
	;
