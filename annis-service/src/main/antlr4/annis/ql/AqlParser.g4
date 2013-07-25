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

parser grammar AqlParser;

options
{
  language=Java;
  tokenVocab=AqlLexer;
}


start 
	: exprTop EOF
	;

textSpec 
	:	START_TEXT_PLAIN  END_TEXT_PLAIN # EmptyExactTextSpec
  | START_TEXT_PLAIN content=TEXT_PLAIN END_TEXT_PLAIN # ExactTextSpec
  | START_TEXT_REGEX END_TEXT_REGEX #EmptyRegexTextSpec
  | START_TEXT_REGEX content=TEXT_REGEX END_TEXT_REGEX # RegexTextSpec
	;

rangeSpec
  : min=DIGITS (COMMA max=DIGITS)?
  ;

qName
	:	namespace=ID COLON name=ID
	|	name=ID
	;

edgeAnno
	:	 name=qName op=(EQ|NEQ) value=textSpec
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
	: left=REF DOMINANCE (layer=ID)?  (LEFT_CHILD | RIGHT_CHILD)? (anno=edgeSpec)? right=REF # DirectDominance
	| left=REF DOMINANCE (layer=ID)? STAR right=REF # IndirectDominance
	| left=REF DOMINANCE (layer=ID)? rangeSpec right=REF # RangeDominance
	;
	
pointing
	: left=REF POINTING label=ID (anno=edgeSpec)? right=REF # DirectPointing
	| left=REF POINTING label=ID (anno=edgeSpec)? STAR right=REF # IndirectPointing
	| left=REF POINTING label=ID (anno=edgeSpec)? COMMA? rangeSpec right=REF # RangePointing
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
	:	precedence # PrecedenceRelation
	|	spanrelation # SpanRelation
	|	dominance # DominanceRelation
	|	pointing # PointingRelation
	|	left=REF COMMON_PARENT (label=ID)? right=REF # CommonParent
	|	left=REF COMMON_PARENT (label=ID)? STAR right=REF # CommonAncestor
  | REF EQ REF # Identity
	;
	
unary_linguistic_term
	:	left=REF ROOT # RootTerm
	|	left=REF ARITY EQ rangeSpec # ArityTerm
	|	left=REF TOKEN_ARITY EQ rangeSpec # TokenArityTerm
	;

variableDefinition
  : REF COLON
  ;

variableExpr
 	: TOK # TokOnlyExpr 
  | NODE # NodeExpr
  | TOK op=(EQ|NEQ) txt=textSpec # TokTextExpr
	|	txt=textSpec # TextOnly // shortcut for tok="..."
  | qName # AnnoOnlyExpr
	| qName op=(EQ|NEQ) txt=textSpec # AnnoEqTextExpr
  ;

expr
  : vardef=variableDefinition variableExpr # VariableTermExpr
  | variableExpr # NoVariableTermExpr
	|	unary_linguistic_term # UnaryTermExpr
	|	binary_linguistic_term #  BinaryTermExpr
  | META DOUBLECOLON id=qName op=EQ txt=textSpec # MetaTermExpr 
  | BRACE_OPEN expr (AND expr)* BRACE_CLOSE # AndExpr
  | BRACE_OPEN expr (OR expr)+ BRACE_CLOSE # OrExpr
  ;


exprTop
  : expr (OR expr)+ # OrTop
  | expr (AND expr)* # AndTop
  | BRACE_OPEN exprTop BRACE_CLOSE # BracedTop
	;
