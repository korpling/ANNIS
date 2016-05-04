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
CodeMirror.defineMode("aql", function(config, parserConfig) {
  
  var regexMeta = /meta::([a-zA-Z_%]([a-zA-Z0-9_\-%])*:)?([a-zA-Z_%]([a-zA-Z0-9_\-%])*)/;
  var regexID = /([a-zA-Z_%]([a-zA-Z0-9_\-%])*:)?([a-zA-Z_\%]([a-zA-Z0-9_\-%])*)/;
  
  var regexLeftRightChild = />([a-zA-Z_%][a-zA-Z0-9_\-%]*)?(@(l|r)?)/;
  var regexPrecedenceNearPointingDom = /((\.)|(\^)|(->)|(>))([a-zA-Z_%][a-zA-Z0-9_\-%]*)?(\s*(\*)|([ \t,0-9]+))?/;
  
  var regexSimpleOperators = /(==)|(_=_)|(_i_)|(_o_)|(_l_)|(_r_)|(_ol_)|(_or_)/;
  
  function getNodeClassForString(state)
  {
    if(state.behindAssignment)
    {
      return state.position;
    }
    else
    {
      return getNodeClass(state);
    }
  }
  
  function getNodeClass(state) {
    var mappedNode = state.numberOfNodes;
    if (state.nodeMappings[mappedNode])
    {
      mappedNode = state.nodeMappings[mappedNode];
    }
    return "node_" + mappedNode;
  }
  
  function addNode(state) {
    if (state.numberOfNodes < 16)
    {
      state.numberOfNodes++;
    }
    return getNodeClass(state);
  }
  
  return {
    token: function(stream, state) {
      
      while(stream.eatSpace());
      
      if(state.position === "string")
      {
        if(stream.match("\""))
        {
          state.position = "def";
          if(state.behindAssignment)
          {
             state.behindAssignment = false;
            // the closing quote character should be still highlighted as such
            return "string";
          }
          else
          {
            return getNodeClass(state);
          }
        }
        else
        {
          stream.next();
          return getNodeClassForString(state, "string");
        }
      }
      else if(state.position === "string-2")
      {
        if(stream.match("/"))
        {
          state.position = "def";
          if(state.behindAssignment)
          {
            state.behindAssignment = false;
            // the closing quote character should be still highlighted as such
            return "string-2";
          }
          else
          {
            return getNodeClass(state);
          }
        }
        else
        {
          stream.next();
          return getNodeClassForString(state, "string-2");
        }
      }
      else
      {
        if(stream.match("\""))
        {
          state.position = "string";
          if(state.behindAssignment)
          {
            return "string";
          }
          else
          {
            return addNode(state);
          }
        }
        else if (stream.match("/"))
        {
          state.position = "string-2";
          if(state.behindAssignment)
          {
           return "string-2";
          }
          else
          {
            return addNode(state);
          }
        }
        else if (stream.match("["))
        {
          state.position = "edge-anno";
          return "property";
        }
        else if(stream.match("&") || stream.match("|"))
        {
          return "operator"
        }
        else if(stream.match("(") || stream.match(")"))
        {
          return "bracket";
        }
        else if(stream.match(regexSimpleOperators))
        {
          return "operator";
        }
        else if(stream.match(regexLeftRightChild))
        {
          return "operator";
        }
        else if(stream.match(regexPrecedenceNearPointingDom))
        {
          return "operator";
        }
        else if(stream.match("=") || stream.match("!="))
        {
          state.behindAssignment = true;
          return "operator";
        }
        else if(stream.match(regexMeta))
        {
          return "def";
        }
        else if(stream.match(regexID))
        {
          if(state.position === "edge-anno")
          {
            // dont count edge annotations as nodes
            return "def";
          }
          else
          {
            return addNode(state);
          }
        }
        else if(stream.match(/#[0-9a-zA-Z]+/))
        {
          return "variable-2";
        }
        
        // clearing the edge-anno state if necessary
        if (state.position === "edge-anno" && stream.match("]"))
        {
          state.position = "def";
        }
      }
      
      // always go to th next character per default
      stream.next();

      return state.position;
    },

    startState: function() {
      return {
        position : "def",       // Current position, "def" or "quote"
        behindAssignment: false, // if true we are behind an assignment
        numberOfNodes : 0,  // number of nodes that have been detected yet
        nodeMappings : parserConfig.nodeMappings // maps an absolute node number to a relative one (e.g. for OR queries)
      };
    }

  };
});

CodeMirror.defineMIME("text/x-aql", "aql");


