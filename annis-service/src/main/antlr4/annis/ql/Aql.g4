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

rangeSpec
  : min=DIGITS (COMMA max=DIGITS)?
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
	: left=REF PRECEDENCE (layer=ID)? right=REF # DirectPrecedence
	| left=REF PRECEDENCE (layer=ID)? STAR right=REF # IndirectPrecedence
	| left=REF PRECEDENCE (layer=ID COMMA?)? rangeSpec right=REF   #RangePrecedence
	;

dominance
	: REF DOMINANCE (anno=edgeSpec)? REF
	| REF DOMINANCE (anno=edgeSpec)? STAR REF
	| REF DOMINANCE (anno=edgeSpec)? rangeSpec REF
	;
	
pointing
	: REF POINTING label=ID (anno=edgeSpec)? REF
	| REF POINTING label=ID (anno=edgeSpec)? STAR REF
	| REF POINTING label=ID (anno=edgeSpec)? COMMA? rangeSpec REF
	;

spanrelation
  : left=REF IDENT_COV right=REF # IdenticalCoverage
	|	left=REF LEFT_ALIGN right=REF # LeftAlign
	|	left=REF RIGHT_ALIGN right=REF # RightAlign
	|	left=REF INCLUSION right=REF # Inclusion
	|	left=REF OVERLAP right=REF # Overlap
	|	left=REF RIGHT_OVERLAP right=REF # RightOverlap
	| left=REF LEFT_OVERLAP right=REF # LeftOverlap
; 

binary_linguistic_term
	:	precedence
	|	spanrelation
	|	dominance
	|	REF LEFT_CHILD REF
	|	REF RIGHT_CHILD REF
	|	pointing
	|	REF COMMON_PARENT REF
	|	REF COMMON_ANCESTOR REF
	;
	
unary_linguistic_term
	:	left=REF ROOT # RootTerm
	|	left=REF ARITY EQ rangeSpec # ArityTerm
	|	left=REF TOKEN_ARITY EQ rangeSpec # TokenArityTerm
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
