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
package annis.ql.parser;

import annis.exceptions.AnnisQLSemanticsException;
import annis.exceptions.AnnisQLSyntaxException;
import annis.model.LogicClause;
import annis.model.QueryNode;
import annis.ql.AqlLexer;
import annis.ql.AqlParser;
import annis.sqlgen.model.Join;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class AnnisParserAntlr
{
  private int precedenceBound;
  private List<QueryDataTransformer> postProcessors;

  public QueryData parse(String aql, List<Long> corpusList)
  {
    AqlLexer lexer = new AqlLexer(new ANTLRInputStream(aql));
    AqlParser parser = new AqlParser(new CommonTokenStream(
      lexer));
    
    final List<String> errors = new LinkedList<String>();

    parser.removeErrorListeners();
    parser.addErrorListener(new BaseErrorListener()
    {
      @Override
      public void syntaxError(Recognizer recognizer, Token offendingSymbol,
        int line, int charPositionInLine, String msg, RecognitionException e)
      {
        errors.add("line " + line + ":" + charPositionInLine + " " + msg);
      }
    });

    ParseTree tree = parser.start();
    if (errors.isEmpty())
    {
      
      ParseTreeWalker walker = new ParseTreeWalker();
      AqlListener listener = new AqlListener(precedenceBound);
      
      try
      {
        walker.walk(listener, tree);
      }
      catch(NullPointerException ex)
      {
        throw new AnnisQLSemanticsException(ex.getMessage());
      }
      catch(IllegalArgumentException ex)
      {
        throw new AnnisQLSemanticsException(ex.getMessage());
      }
      LogicClause top = listener.getTop();
      DNFTransformer.toDNF(top);
      
      QueryData data = createQueryDataFromTopNode(top);
    
      data.setCorpusList(corpusList);
      data.addMetaAnnotations(listener.getMetaData());
      
      if (postProcessors != null)
      {
        for (QueryDataTransformer transformer : postProcessors)
        {
          data = transformer.transform(data);
        }
      }
      return data;
    }
    else
    {
      throw new AnnisQLSyntaxException("Parser error:\n"
        + Joiner.on("\n").join(errors));
    }
  }
  
  private QueryData createQueryDataFromTopNode(LogicClause top)
  {
    QueryData data = new QueryData();
    
      data.setMaxWidth(0);
      
      for(LogicClause andClause : top.getChildren())
      {
        Set<Long> alternativeNodeIds = new HashSet<Long>();
        Set<String> alternativeNodeVars = new HashSet<String>();
        List<QueryNode> alternative = new ArrayList<QueryNode>();
        for(LogicClause c : andClause.getChildren())
        {
          QueryNode node = new QueryNode(c.getContent());
          Preconditions.checkNotNull(node, "logical node must have an attached QueryNode");
          
          alternative.add(node);
          alternativeNodeIds.add(node.getId());
          alternativeNodeVars.add(node.getVariable());
        }
        
        // set maximal width
        data.setMaxWidth(
          Math.max(data.getMaxWidth(), alternativeNodeIds.size()));
        
        // check for invalid edge joins
        for(QueryNode node : alternative)
        {
          ListIterator<Join> itJoins = node.getJoins().listIterator();
          while(itJoins.hasNext())
          {
            Join j = itJoins.next();
            QueryNode t = j.getTarget();
            if(t != null)
            {
              if(!alternativeNodeVars.contains(t.getVariable()))
              {
                // the join partner is not contained in the alternative
                throw new AnnisQLSemanticsException("Target node \"" + t.
                  getVariable()
                  + "\" is not contained in alternative. Normalized alternative: \n"
                  + QueryData.toAQL(alternative));
              }
              else if(!alternativeNodeIds.contains(t.getId()))
              {
                // silently remove it
                itJoins.remove();
              }
            } // end if target node not null
          } // end for each join
        }
        
        data.addAlternative(alternative);
      } // end for each alternative
    
    return data;
  }

  public int getPrecedenceBound()
  {
    return precedenceBound;
  }

  public void setPrecedenceBound(int precedenceBound)
  {
    this.precedenceBound = precedenceBound;
  }

  public List<QueryDataTransformer> getPostProcessors()
  {
    return postProcessors;
  }

  public void setPostProcessors(
    List<QueryDataTransformer> postProcessors)
  {
    this.postProcessors = postProcessors;
  }
  
  
}
