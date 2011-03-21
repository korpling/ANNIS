/*
 *  Copyright 2010 thomas.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package annis.ql.parser;

import annis.ql.analysis.DepthFirstAdapter;
import annis.ql.node.AAndExpr;
import annis.ql.node.ADominanceLingOp;
import annis.ql.node.AIdentityLingOp;
import annis.ql.node.ALinguisticConstraintExpr;
import annis.ql.node.APointingRelationLingOp;
import annis.ql.node.ASiblingLingOp;
import annis.ql.node.Node;
import annis.ql.node.PExpr;
import annis.ql.node.PLingOp;
import annis.ql.node.TDigits;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.springframework.util.Assert;

/**
 *
 * @author thomas
 */
public class NodeRelationNormalizer extends DepthFirstAdapter
{

  @Override
  public void caseAAndExpr(AAndExpr node)
  {
    boolean check = true;

    while(check)
    {
      check = false;

      SearchExpressionCounter counter = new SearchExpressionCounter();
      node.apply(counter);
      
      RelationCollector relcheck = new RelationCollector();
      node.apply(relcheck);
      
      for(Entry<String,List<ALinguisticConstraintExpr>> e : relcheck.getIsIn().entrySet())
      {
        if(e.getValue().size() > 1)
        {
          ALinguisticConstraintExpr relation = e.getValue().get(0);

          Assert.notNull(relation.getRhs(), "Only binary operators should be considerd in NodeRelationNormalizer");

          boolean leftSide = true;

          if(relation.getRhs().getText().equals(e.getKey()))
          {
            leftSide = false;
          }

          split(node, relation, counter, leftSide);
          check = true;
          break;
        }
      }
    }
  }



  public void split(AAndExpr expr, ALinguisticConstraintExpr relation,
    SearchExpressionCounter counter, boolean replaceLeft)
  {

    if(relation.getRhs() == null)
    {
      // nothing to split
      return;
    }

    int oldId = -1;
    int newId = counter.getCount()+1;

    ALinguisticConstraintExpr updatedRelation = (ALinguisticConstraintExpr) relation.clone();

    if(replaceLeft)
    {
      oldId = Integer.parseInt(relation.getLhs().getText());
      updatedRelation.getLhs().setText("" + newId);
    }
    else
    {
      oldId = Integer.parseInt(relation.getRhs().getText());
      updatedRelation.getRhs().setText("" + newId);
    }

    LinkedList<PExpr> result = expr.getExpr();

    Node nodeToDuplicate = counter.getSearchExpression(oldId);
    Node duplicatedNode = (Node) nodeToDuplicate.clone();
    result.add((PExpr) duplicatedNode);

    // the updated relation must be behind the new node definition (otherwise
    // the node id will be unknown)
    result.remove(relation);
    result.add(updatedRelation);

    ALinguisticConstraintExpr newIdentityRelation = new ALinguisticConstraintExpr();
    newIdentityRelation.setLhs(new TDigits("" + oldId));
    newIdentityRelation.setLingOp(new AIdentityLingOp());
    newIdentityRelation.setRhs(new TDigits("" + newId));
    result.add(newIdentityRelation);
  }

  public static class RelationCollector extends DepthFirstAdapter
  {
    private Map<String, List<ALinguisticConstraintExpr>> isIn = new LinkedHashMap<String, List<ALinguisticConstraintExpr>>();

    @Override
    public void caseALinguisticConstraintExpr(ALinguisticConstraintExpr node)
    {
      PLingOp op = node.getLingOp();
      String left = node.getLhs().getText();
      String right = null;

      if(node.getRhs() != null)
      {
        right = node.getRhs().getText();
      }

      if(op instanceof ADominanceLingOp 
        || op instanceof APointingRelationLingOp
        || op instanceof ASiblingLingOp)
      {
        if(isIn.get(left) == null)
        {
          isIn.put(left, new ArrayList<ALinguisticConstraintExpr>());
        }

        isIn.get(left).add(node);


        if(right != null)
        {
          if(isIn.get(right) == null)
          {
            isIn.put(right, new ArrayList<ALinguisticConstraintExpr>());
          }

          isIn.get(right).add(node);
        }

      }
    }

    public Map<String, List<ALinguisticConstraintExpr>> getIsIn()
    {
      return isIn;
    }


  }

}
