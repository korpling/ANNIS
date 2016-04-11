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

import com.google.common.base.Joiner;

import annis.exceptions.AnnisQLSemanticsException;
import annis.exceptions.AnnisQLSyntaxException;
import annis.model.AqlParseError;
import annis.model.ParsedEntityLocation;
import annis.ql.AqlLexer;
import annis.ql.AqlParser;
import annis.ql.RawAqlPreParser;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class AnnisParserAntlr
{
  
  private static final Logger log = LoggerFactory.getLogger(AnnisParserAntlr.class);
  private int precedenceBound;
  private List<QueryDataTransformer> postProcessors;
  
  public QueryData parse(String aql, List<Long> corpusList)
  {
    final List<AqlParseError> errors = new LinkedList<>();
    

    AqlLexer lexerNonDNF = new AqlLexer(new ANTLRInputStream(aql));
    lexerNonDNF.removeErrorListeners();
    lexerNonDNF.addErrorListener(new AqlLexerErrorListener(errors));
    
    // bring first into DNF
    RawAqlPreParser rawParser = new RawAqlPreParser(new CommonTokenStream(lexerNonDNF));
    rawParser.removeErrorListeners();
    rawParser.addErrorListener(new AqlParseErrorListener(errors));
    
    RawAqlPreParser.StartContext treeRaw = rawParser.start();
    if (!errors.isEmpty())
    {
      throw new AnnisQLSyntaxException(Joiner.on("\n").join(errors), errors);
    }
    //treeRaw.inspect(rawParser);
    
    ParseTreeWalker walkerRaw = new ParseTreeWalker();
    RawAqlListener listenerRaw = new RawAqlListener();
    walkerRaw.walk(listenerRaw, treeRaw);
    
    LogicClause topNode = listenerRaw.getRoot();
    
    DNFTransformer.toDNF(topNode);
    
    // use the DNF form and parse it again
    TokenSource source = new ListTokenSource(topNode.getCoveredToken());
    AqlParser parserDNF = new AqlParser(new CommonTokenStream(source));
    
    parserDNF.removeErrorListeners();
    parserDNF.addErrorListener(new AqlParseErrorListener(errors));

    AqlParser.StartContext treeDNF = parserDNF.start();
    
    //treeDNF.inspect(parserDNF);
    
    if (!errors.isEmpty())
    {
      throw new AnnisQLSyntaxException(Joiner.on("\n").join(errors), errors);
    }
      
    ParseTreeWalker walker = new ParseTreeWalker();
    NodeIDListener idListener = new NodeIDListener();
    walker.walk(idListener, treeDNF);
    
    QueryNodeListener nodeListener = new QueryNodeListener(idListener.getNodeIntervalToID());

    try
    {
      walker.walk(nodeListener, treeDNF);

      QueryData data = nodeListener.getQueryData();

      data.setCorpusList(corpusList);
      data.addMetaAnnotations(nodeListener.getMetaData());
      
      JoinListener joinListener = new JoinListener(data, precedenceBound, 
        nodeListener.getTokenPositions());
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
      log.warn("Null pointer exception occured during parsing", ex);
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
    
    final List<AqlParseError> errors = new LinkedList<>();

    parser.removeErrorListeners();
    parser.addErrorListener(new AqlParseErrorListener(errors));

    ParseTree tree = parser.start();
    
    if (errors.isEmpty())
    {
      return tree.toStringTree();
    }
    else
    {
      throw new AnnisQLSyntaxException(Joiner.on("\n").join(errors), errors);
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
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e)
    {
      if(errors != null)
      {
        errors.add("line " + line + ":" + charPositionInLine + " " + msg);
      }
    }
  }
  
  public static class AqlLexerErrorListener extends BaseErrorListener
  {

    private final List<AqlParseError> errors;

    public AqlLexerErrorListener(List<AqlParseError> errors)
    {
      this.errors = errors;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e)
    {
      if(errors != null)
      {
        ParsedEntityLocation loc = 
          new ParsedEntityLocation(line, charPositionInLine, line, charPositionInLine);
        errors.add(new AqlParseError(loc, msg));
      }
    }
    
  }
  
  public static class AqlParseErrorListener extends BaseErrorListener
  {
    private final List<AqlParseError> errors;

    public AqlParseErrorListener(List<AqlParseError> errors)
    {
      this.errors = errors;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e)
    {
      if(errors != null && offendingSymbol instanceof Token)
      {
        Token t = (Token) offendingSymbol;
        errors.add(new AqlParseError(getLocation(t, t), msg));
      }
    }
    
  }
  
  
  
  public static ParsedEntityLocation getLocation(Token start, Token stop)
  {
    if(start == null)
    {
      return new ParsedEntityLocation();
    }
    if(stop == null)
    {
      stop = start;
    }
    
    int startLine = start.getLine();
    int endLine = stop.getLine();
    
    int startColumn = start.getCharPositionInLine();
    // We assume a token can be only one line (newline character is whitespace and a separator).
    // Thus the end column of a token is the start position plus its actual text length;
    String stopTokenText = stop.getText();    
    int endColumn = stop.getCharPositionInLine();
    if(stopTokenText != null && !stopTokenText.isEmpty())
    {
      endColumn += stopTokenText.length()-1;
    }
    
    return new ParsedEntityLocation(startLine, startColumn, endLine, endColumn);
  }
  
}
