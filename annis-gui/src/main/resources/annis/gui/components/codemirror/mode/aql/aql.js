/* 
 * Copyright 2013 Corpuslinguistic working group Humboldt University Berlin.
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
CodeMirror.defineMode("aql", function() {
  return {
    token: function(stream, state) {
      
      while(stream.eatSpace());
      
      if(state.position === "string")
      {
        if(stream.match("\""))
        {
          state.position = "def";
          // the closing quote character should be still highlighted as such
          return "string";
        }
      }
      else if(state.position === "string-2")
      {
        if(stream.match("/"))
        {
          state.position = "def";
          // the closing quote character should be still highlighted as such
          return "string-2";
        }
      }
      else
      {
        if(stream.match("\""))
        {
          state.position = "string"
          return "string";
        }
        else if (stream.match("/"))
        {
          state.position = "string-2";
          return "string-2";
        }
        else if(stream.match("&") || stream.match("|"))
        {
          return "operator"
        }
        else if(stream.match("(") || stream.match(")"))
        {
          return "bracket";
        }
        else if(stream.match("tok") || stream.match("node"))
        {
          return "keyword";
        }
        else if(stream.match(/(\.\*)|(\.)|(_=_)|(_i_)|(_o_)|(_l_)|(_r_)|(->)|(>@l)|(>@r)|(>\*)|(>)|(\$\*)|(\$)/))
        {
          return "operator";
        }
        else if(stream.match(/#[0-9a-zA-Z]+/))
        {
          return "variable-2";
        }
      }
      
      // always go to th next character per default
      stream.next();

      return state.position;
    },

    startState: function() {
      return {
        position : "def"       // Current position, "def" or "quote"
      };
    }

  };
});

CodeMirror.defineMIME("text/x-aql", "aql");


