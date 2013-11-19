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
import annis.ql.AqlLexer;
import annis.ql.AqlParser;
import annis.ql.RawAqlPreParser;
import com.google.common.base.Joiner;
import java.util.LinkedList;
import java.util.List;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;
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
    final List<String> errors = new LinkedList<String>();
    
    AqlLexer lexerNonDNF = new AqlLexer(new ANTLRInputStream(aql));
    
    // bring first into DNF
    RawAqlPreParser rawParser = new RawAqlPreParser(new CommonTokenStream(lexerNonDNF));
    rawParser.removeErrorListeners();
    rawParser.addErrorListener(new StringListErrorListener(errors));
    
    RawAqlPreParser.StartContext treeRaw = rawParser.start();
    if (!errors.isEmpty())
    {
      throw new AnnisQLSyntaxException(Joiner.on("\n").join(errors));
    }
    
    ParseTreeWalker walkerRaw = new ParseTreeWalker();
    RawAqlListener listenerRaw = new RawAqlListener();
    walkerRaw.walk(listenerRaw, treeRaw);
    
    LogicClause topNode = listenerRaw.getRoot();
    DNFTransformer.toDNF(topNode);
    
    // use the DNF form and parse it again
    TokenSource source = new ListTokenSource(topNode.getCoveredToken());
    AqlParser parserDNF = new AqlParser(new CommonTokenStream(source));
    
    parserDNF.removeErrorListeners();
    parserDNF.addErrorListener(new StringListErrorListener(errors));

    ParseTree treeDNF = parserDNF.start();
    
    if (!errors.isEmpty())
    {
      throw new AnnisQLSyntaxException(Joiner.on("\n").join(errors));
    }
      
    ParseTreeWalker walker = new ParseTreeWalker();
    QueryNodeListener nodeListener = new QueryNodeListener();

    try
    {
      walker.walk(nodeListener, treeDNF);

      QueryData data = nodeListener.getQueryData();

      data.setCorpusList(corpusList);
      data.addMetaAnnotations(nodeListener.getMetaData());

      JoinListener joinListener = new JoinListener(data, precedenceBound, 
        nodeListener.getTokenPositionToNode());
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
  
  public String dumpTree(String aql)
  {
    AqlLexer lexer = new AqlLexer(new ANTLRInputStream(aql));
    AqlParser parser = new AqlParser(new CommonTokenStream(
      lexer));
    
    final List<String> errors = new LinkedList<String>();

    parser.removeErrorListeners();
    parser.addErrorListener(new StringListErrorListener(errors));

    ParseTree tree = parser.start();
    
    if (errors.isEmpty())
    {
      return tree.toStringTree();
    }
    else
    {
      throw new AnnisQLSyntaxException(Joiner.on("\n").join(errors));
    }
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
  
  public static class StringListErrorListener extends BaseErrorListener
  {
    private final List<String> errors;

    public StringListErrorListener(List<String> errors)
    {
      this.errors = errors;
    }
    
     @Override
    public void syntaxError(Recognizer recognizer, Token offendingSymbol,
      int line, int charPositionInLine, String msg, RecognitionException e)
    {
      if(errors != null)
      {
        errors.add("line " + line + ":" + charPositionInLine + " " + msg);
      }
    }
  }
  
}
