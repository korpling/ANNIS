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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import annis.exceptions.AnnisQLSyntaxException;
import annis.model.QueryAnnotation;
import annis.model.QueryNode;
import annis.ql.AqlParser;
import annis.ql.AqlParserBaseListener;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class QueryNodeListener extends AqlParserBaseListener
{

  private Map<Interval, Long> nodeIntervalToID;
  
  private QueryData data = null;

  /**
   * Maps the node ID to the query node.
   */
  private final TreeMap<Long, QueryNode> currentAlternative = new TreeMap<>();
  
  private String lastVariableDefinition = null;

  private final Multimap<String, QueryNode> localNodes = HashMultimap.create();
  
  private List<Map<Interval, QueryNode>> tokenPositions;
  private final Map<Interval, QueryNode> currentTokenPosition = Maps.newHashMap();
  
  private final List<QueryAnnotation> metaData = new ArrayList<>();

  public QueryNodeListener(Map<Interval, Long> nodeIntervalToID)
  {
    this.nodeIntervalToID = nodeIntervalToID;
    if(this.nodeIntervalToID == null)
    {
      this.nodeIntervalToID = new HashMap<>();
    }
  }

  public QueryData getQueryData()
  {
    return data;
  }

  public List<QueryAnnotation> getMetaData()
  {
    return metaData;
  }
  
  @Override
  public void enterOrTop(AqlParser.OrTopContext ctx)
  {
    data = new QueryData();
    tokenPositions = new ArrayList<>();
  }

  @Override
  public void enterAndExpr(AqlParser.AndExprContext ctx)
  {
    currentAlternative.clear();
    localNodes.clear();
    currentTokenPosition.clear();
  }

  @Override
  public void exitAndExpr(AqlParser.AndExprContext ctx)
  {
    data.addAlternative(new ArrayList<>(currentAlternative.values()));
    tokenPositions.add(new HashMap<>(currentTokenPosition));
  }

  

  @Override
  public void enterTokOnlyExpr(AqlParser.TokOnlyExprContext ctx)
  {
    QueryNode target = newNode(ctx);
    target.setToken(true);
  }

  @Override
  public void enterNodeExpr(AqlParser.NodeExprContext ctx)
  {
    newNode(ctx);
  }
  

  @Override
  public void enterTokTextExpr(AqlParser.TokTextExprContext ctx)
  {
    QueryNode target = newNode(ctx);
    target.setToken(true);
    QueryNode.TextMatching txtMatch = textMatchingFromSpec(ctx.textSpec(),
      ctx.NEQ() != null);
    String content = textFromSpec(ctx.textSpec());
    target.setSpannedText(content, txtMatch);
  }

  @Override
  public void enterTextOnly(AqlParser.TextOnlyContext ctx)
  {
    QueryNode target = newNode(ctx);
    target.setSpannedText(textFromSpec(ctx.txt),
      textMatchingFromSpec(ctx.txt, false));
  }

  @Override
  public void enterAnnoOnlyExpr(AqlParser.AnnoOnlyExprContext ctx)
  {
    QueryNode target = newNode(ctx);
    String namespace = ctx.qName().namespace == null ? null : ctx.qName().namespace.getText();
    QueryAnnotation anno = new QueryAnnotation(namespace,
      ctx.qName().name.getText());
    target.addNodeAnnotation(anno);
  }

  @Override
  public void enterAnnoEqTextExpr(AqlParser.AnnoEqTextExprContext ctx)
  {
    QueryNode target = newNode(ctx);
    String namespace = ctx.qName().namespace == null ? 
      null : ctx.qName().namespace.getText();
    String name = ctx.qName().name.getText();
    String value = textFromSpec(ctx.txt);
    QueryNode.TextMatching matching = textMatchingFromSpec(ctx.txt,
      ctx.NEQ() != null);
    QueryAnnotation anno = new QueryAnnotation(namespace, name, value, matching);
    target.addNodeAnnotation(anno);
  }

  
  
  
  
  @Override
  public void enterMetaTermExpr(AqlParser.MetaTermExprContext ctx)
  {
    // TODO: we have to disallow OR expressions with metadata, how can we
    // achvieve that?
    String namespace = ctx.id.namespace == null ? 
      null : ctx.id.namespace.getText();
    String name = ctx.id.name.getText();
    String value = textFromSpec(ctx.txt);
    QueryNode.TextMatching textMatching = textMatchingFromSpec(ctx.txt, ctx.NEQ() != null);
    
    QueryAnnotation anno = new QueryAnnotation(namespace,
      name, value, textMatching);
    metaData.add(anno);
  }

  @Override
  public void enterNamedVariableTermExpr(AqlParser.NamedVariableTermExprContext ctx)
  {
    lastVariableDefinition = null;
    if(ctx != null)
    {
      String text = ctx.VAR_DEF().getText();
      // remove the trailing "#"
      if(text.endsWith("#"))
      {
        lastVariableDefinition = text.substring(0, text.length()-1);
      }
      else
      {
        lastVariableDefinition = text;
      }
    }
  }

  @Override
  public void enterReferenceNode(AqlParser.ReferenceNodeContext ctx)
  {
    if(ctx != null && ctx.VAR_DEF() != null)
    {
      lastVariableDefinition = null;
    
      String text = ctx.VAR_DEF().getText();
      // remove the trailing "#"
      if(text.endsWith("#"))
      {
        lastVariableDefinition = text.substring(0, text.length()-1);
      }
      else
      {
        lastVariableDefinition = text;
      }
    
    }
  }
  
  
  

  protected static String textFromSpec(AqlParser.TextSpecContext txtCtx)
  {
    if (txtCtx instanceof AqlParser.EmptyExactTextSpecContext || txtCtx instanceof AqlParser.EmptyRegexTextSpecContext)
    {
      return "";
    }
    else if (txtCtx instanceof AqlParser.ExactTextSpecContext)
    {
      return ((AqlParser.ExactTextSpecContext) txtCtx).content.getText();
    }
    else if (txtCtx instanceof AqlParser.RegexTextSpecContext)
    {
      
      String text = ((AqlParser.RegexTextSpecContext) txtCtx).content.getText();
     /* 
      * For regular expressions we should also check if these are valid ones.
      * Valid for AQL means in this case complient with Java Regex. Since
      * the actual query is executed by PostgreSQL we might accept queries
      * as valid even if PostgreSQL can't execute them. The purpose of this check
      * is to avoid cryptic error messages. Thus we ignore the different syntax
      * for corner cases of regular expressions in Java an PostgreSQL.
      */
      try
      {
        Pattern.compile(text);
      }
      catch(PatternSyntaxException ex)
      {
        throw new AnnisQLSyntaxException("Invalid regular expression: " + ex.getMessage());
      }
      return text;
    }
    return null;
  }
  
  protected static QueryNode.TextMatching textMatchingFromSpec(
    AqlParser.TextSpecContext txt, boolean not)
  {
    if (txt instanceof AqlParser.ExactTextSpecContext 
      || txt instanceof AqlParser.EmptyExactTextSpecContext)
    {
      return not ? QueryNode.TextMatching.EXACT_NOT_EQUAL : 
        QueryNode.TextMatching.EXACT_EQUAL;
    }
    else if (txt instanceof AqlParser.RegexTextSpecContext
      || txt instanceof AqlParser.EmptyRegexTextSpecContext)
    {
     return  not ? QueryNode.TextMatching.REGEXP_NOT_EQUAL : 
       QueryNode.TextMatching.REGEXP_EQUAL;
    }
    return null;
  }

  private QueryNode newNode(ParserRuleContext ctx)
  {
    Long existingID = nodeIntervalToID.get(ctx.getSourceInterval());
    
    if(existingID == null)
    {
      throw new IllegalStateException("Could not find a node ID for interval " + ctx.getSourceInterval().toString());
    }
    
    QueryNode n = new QueryNode(existingID);
    if(lastVariableDefinition == null)
    {
      n.setVariable("" + n.getId());
    }
    else
    {
      n.setVariable(lastVariableDefinition);
    }
    lastVariableDefinition = null;
    
    n.setParseLocation(AnnisParserAntlr.getLocation(ctx.getStart(), ctx.getStop()));
    
    currentAlternative.put(existingID, n);
    localNodes.put(n.getVariable(), n);
    currentTokenPosition.put(ctx.getSourceInterval(), n);
    
    return n;
  }

  public List<Map<Interval, QueryNode>> getTokenPositions()
  {
    return tokenPositions;
  }
  
  

  
}
