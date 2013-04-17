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

grammar HTMLVisConfig;

WS: [ \t]+;
SEMICOLON : ';';
EQUALS : '=';
TOK : 'tok';
VALUE : 'value';
ANNO : 'anno';
STYLE : 'style';
QUOTE : '"';
NEWLINE : '\n';
COMMENT : '#' ~('\n')+ -> skip;
ID: [a-zA-Z\_\-*?]+;// [a-zA-Z_\-*?0-9.]*;
TXT : (.)+?;

innervalue: ~(QUOTE)+;
value : QUOTE innervalue QUOTE;

innertype: ~(QUOTE)+;
type
  : VALUE # typeValue
  | ANNO # typeAnno
  | QUOTE innertype QUOTE # typeConstant
  ;

element 
  : ID # elementNoStyle
  | ID SEMICOLON WS? STYLE EQUALS value # elementWithStyle
  ;

condition
  : ID # conditionName
  | TOK # conditionTok
  | ID EQUALS value # conditionNameAndValue
  | EQUALS value # conditionValue
  ;

vis 
  : condition WS element (WS type)? WS? NEWLINE*
  ;

start
     : NEWLINE* vis (NEWLINE+ vis)* EOF
     ;