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
BRACE_OPEN:'(';
BRACE_CLOSE:')';
BRACKET_OPEN:'[';
BRACKET_CLOSE:']';
SLASH:'/';
DOUBLE_QUOTE:'"';
COLON:':';


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
OCTAL_ESC
    :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7')
    ;

ESC_SEQ
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\'|'/')
    |   UNICODE_ESC
    |   OCTAL_ESC
    ;

start 
	: exprTop EOF
	;

exactText
  : ( ESC_SEQ | ~(DOUBLE_QUOTE) )+
  ;

regexText
  : ( ESC_SEQ | ~(SLASH) )+
  ;

textSpec 
	:	DOUBLE_QUOTE  DOUBLE_QUOTE # EmptyExactTextSpec
  | DOUBLE_QUOTE content=exactText DOUBLE_QUOTE # ExactTextSpec
  | SLASH SLASH #EmptyRegexTextSpec
  | SLASH content=regexText SLASH # RegexTextSpec
	;

qName
	:	namespace=ID COLON name=ID
	|	name=ID
	;

edgeAnno
	:	 name=qName EQ value=textSpec
	;

edgeSpec
	: BRACKET_OPEN edgeAnno+ BRACKET_CLOSE
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
	: REF DOMINANCE (anno=edgeSpec)? REF
	| REF DOMINANCE (anno=edgeSpec)? STAR REF
	| REF DOMINANCE (anno=edgeSpec)? min=DIGITS (COMMA max=DIGITS)? REF
	;
	
pointing
	: REF POINTING label=ID (anno=edgeSpec)? REF
	| REF POINTING label=ID (anno=edgeSpec)? STAR REF
	| REF POINTING label=ID (anno=edgeSpec)? COMMA? min=DIGITS (COMMA max=DIGITS)? REF
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
	:	REF ROOT # RootTerm
	|	REF ARITY EQ DIGITS # ArityTerm
	|	REF TOKEN_ARITY EQ DIGITS # TokenArityTerm
	;


expr
	: TOK # TokOnlyExpr 
  | TOK op=(EQ|NEQ) txt=textSpec # TokTextExpr
	|	txt=textSpec # TextOnly // shortcut for tok="..."
  | qName # AnnoOnlyExpr
	|	qName op=(EQ|NEQ) txt=textSpec # AnnoEqTextExpr
	|	unary_linguistic_term # UnaryTermExpr
	|	binary_linguistic_term #  BinaryTermExpr
  | BRACE_OPEN expr (OR expr)+ BRACE_CLOSE # OrExpr
  | BRACE_OPEN expr (AND expr)+ BRACE_CLOSE # AndExpr
  ;

exprTop
  : expr (AND expr)* # AndTop
  | expr (OR expr)+ # OrTop
	;
