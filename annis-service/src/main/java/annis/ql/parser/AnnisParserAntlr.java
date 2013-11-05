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
import annis.model.LogicClauseDNF;
import annis.model.QueryNode;
import annis.ql.AqlLexer;
import annis.ql.AqlParser;
import annis.ql.RawAqlPreParser;
import annis.sqlgen.model.Join;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.misc.TestRig;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class AnnisParserAntlr
{
  
  private static final Logger log = LoggerFactory.getLogger(AnnisParserAntlr.class);
  private int precedenceBound;
  private List<QueryDataTransformer> postProcessors;

  public QueryData parse(String aql, List<Long> corpusList)
  {
    AqlLexer lexerNonDNF = new AqlLexer(new ANTLRInputStream(aql));
    
    // bring first into DNF
    RawAqlPreParser rawParser = new RawAqlPreParser(new CommonTokenStream(lexerNonDNF));
    
    //rawParser.setBuildParseTree(true);
    
    RawAqlPreParser.StartContext treeRaw = rawParser.start();
    
    //treeRaw.inspect(rawParser);
    
    ParseTreeWalker walkerRaw = new ParseTreeWalker();
    RawAqlListener listenerRaw = new RawAqlListener();
    walkerRaw.walk(listenerRaw, treeRaw);
    
    
    LogicClause topNode = listenerRaw.getRoot();
    DNFTransformer.toDNF(topNode);
    
    // use the DNF form and parse it again
    
    TokenSource source = new ListTokenSource(topNode.getCoveredToken());
    AqlParser parserDNF = new AqlParser(new CommonTokenStream(source));
    final List<String> errors = new LinkedList<String>();

    parserDNF.removeErrorListeners();
    parserDNF.addErrorListener(new BaseErrorListener()
    {
      @Override
      public void syntaxError(Recognizer recognizer, Token offendingSymbol,
        int line, int charPositionInLine, String msg, RecognitionException e)
      {
        errors.add("line " + line + ":" + charPositionInLine + " " + msg);
      }
    });

    ParseTree treeDNF = parserDNF.start();
    
    if (errors.isEmpty())
    {
      
      ParseTreeWalker walker = new ParseTreeWalker();
      QueryNodeListener nodeListener = new QueryNodeListener();
      
      try
      {
        walker.walk(nodeListener, treeDNF);

        QueryData data = nodeListener.getQueryData();

        data.setCorpusList(corpusList);
        data.addMetaAnnotations(nodeListener.getMetaData());

        JoinListener joinListener = new JoinListener(data, precedenceBound);
        walker.walk(joinListener, treeDNF);
        
        if (postProcessors != null)
        {
          for (QueryDataTransformer transformer : postProcessors)
          {
            data = transformer.transform(data);
          }
        }
        return data;
        
      }
      catch(NullPointerException ex)
      {
        log.warn(null, ex);
        throw new AnnisQLSemanticsException(ex.getMessage());
      }
      catch(IllegalArgumentException ex)
      {
        throw new AnnisQLSemanticsException(ex.getMessage());
      }
    }
    else
    {
      throw new AnnisQLSyntaxException("Parser error:\n"
        + Joiner.on("\n").join(errors));
    }
  }
  
  public String dumpTree(String aql)
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
      return tree.toStringTree();
    }
    else
    {
      throw new AnnisQLSyntaxException("Parser error:\n"
        + Joiner.on("\n").join(errors));
    }
  }
  
  @Deprecated
  private QueryData createQueryDataFromTopNode(LogicClauseDNF top)
  {
    QueryData data = new QueryData();
    
      data.setMaxWidth(0);
      
      Preconditions.checkArgument(top.getOp() == LogicClauseDNF.Operator.OR,
        "Toplevel logic clause must be of type OR");
      
      for(LogicClauseDNF andClause : top.getChildren())
      {
        Set<String> alternativeNodeVars = new HashSet<String>();
        List<QueryNode> alternative = new ArrayList<QueryNode>();
        
        Map<Long, QueryNode> alternativeNodesByID = new HashMap<Long, QueryNode>();
        
        // collect nodes
        for(LogicClauseDNF c : andClause.getChildren())
        {
          Preconditions.checkState(c.getOp() == LogicClauseDNF.Operator.LEAF, 
            "alternative child node must be a leaf");
          Preconditions.checkNotNull(c.getContent(), "logical node must have an attached QueryNode");
         
          if(c.getJoin() == null)
          {
            // this is a normal query node and not a join
            QueryNode node = c.getContent();

            alternative.add(node);
            alternativeNodesByID.put(node.getId(), node);
            alternativeNodeVars.add(node.getVariable());
          }
        }
        
        // add joins
        for(LogicClauseDNF c : andClause.getChildren())
        {
          Preconditions.checkState(c.getOp() == LogicClauseDNF.Operator.LEAF, 
            "alternative child node must be a leaf");
          Preconditions.checkNotNull(c.getContent(), "logical node must have an attached QueryNode");
         
          if(c.getContent() != null && c.getJoin() != null)
          {
            Join j = c.getJoin();
            QueryNode node = alternativeNodesByID.get(c.getContent().getId());
            QueryNode target = alternativeNodesByID.get(j.getTarget().getId());
            
            if(node != null && target != null)
            {
              node.addJoin(j);
            }
          }
        }
        
        // set maximal width
        data.setMaxWidth(
          Math.max(data.getMaxWidth(), alternativeNodesByID.size()));
        
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
              else if(!alternativeNodesByID.containsKey(t.getId()))
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
