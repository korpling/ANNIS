/*
 * Copyright 2015 SFB 632.
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

import annis.ql.AqlParser;
import annis.ql.AqlParserBaseListener;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.antlr.v4.runtime.misc.Interval;

/**
 * Generates a mapping from a overlapped range to an ID.
 * Nodes are ordered according to their token position.
 * 
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class NodeIDListener extends AqlParserBaseListener
{
  /**
   * Maps a global character position in the input stream to a parse token interval.
   */
  private final TreeMap<Integer, Interval> allNodeIntervals = new TreeMap<>();
  /**
   * Maps a parse token interval to the node ID it overlaps.
   */
  private final Map<Interval, Long> nodeIntervalToID = new HashMap<>();
  
  private void addNode(AqlParser.VariableExprContext ctx)
  {
    // first only collect all node intervals
    Interval i = ctx.getSourceInterval();
    allNodeIntervals.put(ctx.getStart().getStartIndex(), i);
  }

  @Override
  public void enterAnnoEqTextExpr(AqlParser.AnnoEqTextExprContext ctx)
  {
    addNode(ctx);
  }

  @Override
  public void enterTokOnlyExpr(AqlParser.TokOnlyExprContext ctx)
  {
    addNode(ctx);
  }

  @Override
  public void enterNodeExpr(AqlParser.NodeExprContext ctx)
  {
    addNode(ctx);
  }

  @Override
  public void enterTokTextExpr(AqlParser.TokTextExprContext ctx)
  {
    addNode(ctx);
  }

  @Override
  public void enterTextOnly(AqlParser.TextOnlyContext ctx)
  {
    addNode(ctx);
  }

  @Override
  public void enterAnnoOnlyExpr(AqlParser.AnnoOnlyExprContext ctx)
  {
    addNode(ctx);
  }
  

  @Override
  public void exitStart(AqlParser.StartContext ctx)
  {
    // Iterate over all intervals in order and set the IDs.
    // Since the map is ordered by the global character position
    // nodes which are mentioned first get their ID first.
    long id=1;
    for(Interval i : allNodeIntervals.values())
    {
      nodeIntervalToID.put(i, id++);
    }
  }

  
  public Map<Interval, Long> getNodeIntervalToID()
  {
    return nodeIntervalToID;
  }
  
  
}
