/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.ql.parser;

import annis.exceptions.AnnisQLSyntaxException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.PushbackReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;



import annis.ql.analysis.DepthFirstAdapter;
import annis.ql.lexer.Lexer;
import annis.ql.lexer.LexerException;
import annis.ql.node.Start;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnnisParser
{

  private static final Logger log = LoggerFactory.getLogger(AnnisParser.class);

  // extra class to allow stubbing in tests
  public static class InternalParser
  {

    public Start parse(String input) throws ParserException, LexerException, IOException
    {
      return new Parser(new Lexer(new PushbackReader(new StringReader(input), 3000))).parse();
    }
  }
  private InternalParser internalParser;
  // holds a list of post-processors
  private List<DepthFirstAdapter> postProcessors;

  /**
   * Creates a parser for AnnisQL statements.
   */
  public AnnisParser()
  {
    postProcessors = new ArrayList<DepthFirstAdapter>();
    postProcessors.add(new NodeSearchNormalizer());
    postProcessors.add(new TokenSearchNormalizer());
    postProcessors.add(new QueryValidator());
    internalParser = new InternalParser();
  }

  public Start parse(String annisQuery)
  {
    try
    {
      log.debug("parsing ANNIS query: " + annisQuery);

      // build and post-process syntax tree
      Start start = getInternalParser().parse(annisQuery);

      for(DepthFirstAdapter postProcessor : getPostProcessors())
      {
        log.debug("applying post processor to syntax tree: " + postProcessor.getClass().getSimpleName());
        start.apply(postProcessor);
      }

      log.debug("syntax tree is:\n" + dumpTree(start));
      return start;

    }
    catch(ParserException e)
    {
      log.warn("an exception occured on the query: " + annisQuery, e);
      throw new AnnisQLSyntaxException(e.getLocalizedMessage());
    }
    catch(LexerException e)
    {
      log.warn("an exception occured on the query: " + annisQuery, e);
      throw new AnnisQLSyntaxException(e.getLocalizedMessage());
    }
    catch(IOException e)
    {
      log.warn("an exception occured on the query: " + annisQuery, e);
      throw new AnnisQLSyntaxException(e.getLocalizedMessage());
    }
  }

  public String dumpTree(String annisQuery)
  {
    return dumpTree(parse(annisQuery));
  }

  public static String dumpTree(Start start)
  {
    try
    {
      StringWriter result = new StringWriter();
      start.apply(new TreeDumper(new PrintWriter(result)));
      return result.toString();
    }
    catch(RuntimeException e)
    {
      String errorMessage = "could not serialize syntax tree";
      log.warn(errorMessage, e);
      return errorMessage;
    }
  }

  ///// Getter / Setter
  public List<DepthFirstAdapter> getPostProcessors()
  {
    return postProcessors;
  }

  public void setPostProcessors(List<DepthFirstAdapter> postProcessors)
  {
    this.postProcessors = postProcessors;
  }

  protected void setInternalParser(InternalParser parser)
  {
    this.internalParser = parser;
  }

  protected InternalParser getInternalParser()
  {
    return internalParser;
  }

}
