/*
 * Copyright 2012 SFB 632.
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
package annis.gui.visualizers.partitur;

import annis.model.AnnisNode;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author benjamin
 */
public class DetectHoles
{

  private List<AnnisNode> token;
  private List<Tripel> intervalls;

  private class Tripel
  {

    long first;
    long second;
    long offset;

    Tripel(long first, long second, long offset)
    {
      this.first = first;
      this.second = second;
      this.offset = offset;
    }
  }

  public DetectHoles(List<AnnisNode> token)
  {
    this.token = token;
    this.intervalls = createIntervalls(token);
  }

  public AnnisNode getLeftBorder(AnnisNode n)
  {
    Tripel tripel = null;
    long leftTokIdx = n.getLeftToken();

    for (Tripel tmp : intervalls)
    {
      if (tmp.first <= leftTokIdx && leftTokIdx <= tmp.second)
      {
        tripel = tmp;
        break;
      }

      if (tripel == null)
      {
        tripel = tmp;
        continue;
      }

      if (tmp.first <= leftTokIdx)
      {
        tripel = tmp;
      }

    }

    long leftBorder = tripel.first <= leftTokIdx ? leftTokIdx : tripel.first;
    return token.get((int) (leftBorder - tripel.offset));
  }

  public AnnisNode getRightBorder(AnnisNode n)
  {
    Tripel tupel = null;
    long rightTokIdx = n.getRightToken();

    for (Tripel tmp : intervalls)
    {
      if (tmp.first <= rightTokIdx && rightTokIdx <= tmp.second)
      {
        tupel = tmp;
        break;
      }

      if (rightTokIdx >= tmp.second)
      {
        tupel = tmp;
      }
    }

    long rightBorder = tupel.second >= rightTokIdx ? rightTokIdx : tupel.second;
    return token.get((int) (rightBorder - tupel.offset));
  }

  private List<Tripel> createIntervalls(List<AnnisNode> token)
  {

    intervalls = new LinkedList<Tripel>();
    intervalls.add(new Tripel(-1, -1, 0));

    for (int i = 0; i < token.size(); i++)
    {
      long tokenIdx = token.get(i).getTokenIndex();
      Tripel tupel = intervalls.get(intervalls.size() - 1);

      // initialize
      if (tupel.first == -1)
      {
        tupel.first = tokenIdx;
        tupel.second = token.get(i).getTokenIndex();
        tupel.offset = tokenIdx - i;
        continue;
      }

      //check for hole
      if (tokenIdx - 1 - tupel.second == 0)
      {
        tupel.second = tokenIdx;
        continue;
      }
      else
      {
        tupel = new Tripel(tokenIdx, tokenIdx, tokenIdx - i);
        intervalls.add(tupel);
      }
    }
    return intervalls;
  }
}
