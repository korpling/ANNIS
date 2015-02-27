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
lexer grammar AqlLexer;

@lexer::members {
	boolean ignoreWS = true;
}

// additional tokens
tokens {
	RANGE,
	ANNO,
	FROM_TO,
	DOM
}
  
TOK:'tok' {ignoreWS = true;};
NODE:'node' {ignoreWS = true;};
META:'meta' {ignoreWS = true;};
AND:'&' {ignoreWS = true;};
OR:'|' {ignoreWS = true;};
EQ_VAL:'==' {ignoreWS = true;};
EQ: '=' {ignoreWS = true;};
NEQ:'!=' {ignoreWS = true;};
DOMINANCE: '>' {ignoreWS = false;};
POINTING:'->' {ignoreWS = false;};
PRECEDENCE:'.' {ignoreWS = false;};
NEAR:'^' {ignoreWS = true;};
IDENT_COV:'_=_' {ignoreWS = true;};
INCLUSION:'_i_' {ignoreWS = true;};
OVERLAP:'_o_' {ignoreWS = true;};
LEFT_ALIGN:'_l_' {ignoreWS = true;};
RIGHT_ALIGN:'_r_' {ignoreWS = true;};
LEFT_OVERLAP:'_ol_' {ignoreWS = true;};
RIGHT_OVERLAP:'_or_' {ignoreWS = true;};
LEFT_CHILD:'@l' {ignoreWS = true;};
RIGHT_CHILD:'@r' {ignoreWS = true;};
COMMON_PARENT:'$' {ignoreWS = true;};
IDENTITY:'_ident_' {ignoreWS = true;};
ROOT:':root' {ignoreWS = true;};
ARITY:':arity' {ignoreWS = true;};
TOKEN_ARITY:':tokenarity' {ignoreWS = true;};
COMMA:',' {ignoreWS = true;};
STAR:'*' {ignoreWS = true;};
BRACE_OPEN:'(' {ignoreWS = true;};
BRACE_CLOSE:')' {ignoreWS = true;};
BRACKET_OPEN:'[' {ignoreWS = true;};
BRACKET_CLOSE:']' {ignoreWS = true;};
COLON:':' {ignoreWS = true;};
DOUBLECOLON:'::' {ignoreWS = true;};


WS  :   [ \t\r\n]+ {if(ignoreWS) { skip();} ignoreWS=true;};  

VAR_DEF
	:	[a-zA-Z] [0-9a-zA-Z]* '#' {ignoreWS = true;}
	;

REF
	:	'#' [0-9a-zA-Z]+ {ignoreWS = true;}
	;

ID  :	[a-zA-Z_] [a-zA-Z0-9_-]* {ignoreWS = true;}
    ;

DIGITS : [0-9]+ {ignoreWS = true;};


START_TEXT_REGEX : '/' -> pushMode(IN_REGEX);
START_TEXT_PLAIN:'"' -> pushMode(IN_TEXT);


mode IN_REGEX;

END_TEXT_REGEX : '/' -> popMode;
TEXT_REGEX : (~'/')+;

mode IN_TEXT;

END_TEXT_PLAIN : '"' -> popMode;
TEXT_PLAIN : (~'"')+;