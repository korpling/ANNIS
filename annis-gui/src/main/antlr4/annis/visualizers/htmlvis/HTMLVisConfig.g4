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
ESCAPED_VALUE : 'escaped_value';
ANNO : 'anno';
META : 'meta';
STYLE : 'style';
COLON : ':';
BEGIN : 'annis:BEGIN';
END : 'annis:END';
ALL : 'annis:ALL';
QUOTE : '"';
NEWLINE : '\n';
COMMENT : '#' ~('\n')+ -> skip;
ID: [a-zA-Z0-9\_\-*?]+;
TXT : (.)+?;
TEMPVALUE :'%%value%%';
TEMPANNO: '%%anno%%';

innervalue: ~(QUOTE)+;
value : QUOTE innervalue QUOTE;
temp: TEMPVALUE|TEMPANNO;
innertype: ~(QUOTE|TEMPVALUE|TEMPANNO)+;
innerhtmltemp: (innertype* temp innertype*)+; 
innermeta: ~(QUOTE|WS|NEWLINE)+;

type
  : VALUE # typeValue
  | ESCAPED_VALUE # typeEscapedValue
  | ANNO # typeAnno
  | QUOTE innerhtmltemp QUOTE # typeHtmlTemp
  | QUOTE innertype QUOTE # typeConstant
  | META COLON COLON innermeta # typeMeta
  ;

element 
  : ID # elementNoStyle
  | ID COLON ID # elementNoStyleAttribute
  | ID SEMICOLON WS? STYLE EQUALS value # elementWithStyle
  | ID COLON ID SEMICOLON WS? STYLE EQUALS value # elementWithStyleAttribute
  ;

qName
  : (namespace=ID COLON)? name=ID;

condition
  : BEGIN # conditionBegin
  | END # conditionEnd
  | ALL # conditionAll
  | qName # conditionName
  | TOK # conditionTok
  | qName EQUALS value # conditionNameAndValue
  | EQUALS value # conditionValue
  ;

vis 
  : condition WS element (WS type)? WS? NEWLINE*
  ;

start
     : NEWLINE* vis (NEWLINE+ vis)* EOF
     ;