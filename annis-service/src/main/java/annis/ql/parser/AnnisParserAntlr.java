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
      walker.walk(listener, tree);
      
      LogicClause top = listener.getTop();
      DNFTransformer.toDNF(top);
      
      QueryData result = new QueryData();
      for(LogicClause andClause : top.getChildren())
      {
        Set<Long> alternativeNodeIds = new HashSet<Long>();
        List<QueryNode> alternative = new ArrayList<QueryNode>();
        for(LogicClause c : andClause.getChildren())
        {
          QueryNode node = new QueryNode(c.getContent());
          Preconditions.checkNotNull(node, "logical node must have an attached QueryNode");
          
          alternative.add(node);
          alternativeNodeIds.add(node.getId());
        }
        
        // remove all invalid edge joins
        for(QueryNode node : alternative)
        {
          ListIterator<Join> itJoins = node.getJoins().listIterator();
          while(itJoins.hasNext())
          {
            Join j = itJoins.next();
            QueryNode t = j.getTarget();
            if(t!= null && !alternativeNodeIds.contains(t.getId()))
            {
              itJoins.remove();
            }
          }
        }
        
        result.addAlternative(alternative);
      } // end for each alternative
      result.setCorpusList(corpusList);
      
      // TODO: what more do we need to set in the QueryData?
      
      return result;
    }
    else
    {
      throw new AnnisQLSyntaxException("Parser error:\n"
        + Joiner.on("\n").join(errors));
    }
  }
  
  private QueryData createQueryDataFromTopNode(QueryNode topNode)
  {
    QueryData data = new QueryData();
    
    // TODO: add Metadata to QueryData
    // TODO: create normalized 
    
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
}
