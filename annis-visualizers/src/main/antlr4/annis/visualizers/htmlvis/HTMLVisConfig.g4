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
ID: [a-zA-Z_-*?]+;//[a-zA-Z_\-*?0-9.]*;
SEMICOLON : ';';
EQUALS : '=';
STYLE : 'style';
QUOTE : '"';
NEWLINE : '\n';


value : QUOTE ~(QUOTE) QUOTE;

element : ID # elementNoStyle
        | ID SEMICOLON STYLE EQUALS value # elementWithStyle
        ;

condition
  : ID # conditionNoValue
  | ID EQUALS value # conditionWithValue
  ;

vis : condition WS element NEWLINE*;

start
     : NEWLINE* vis (NEWLINE+ vis)* EOF
     ;