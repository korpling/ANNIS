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
package annis.visualizers.iframe.partitur;

import annis.model.AnnisNode;
import java.util.List;

/**
 *
 * @author benjamin
 */
public class DetectHoles
{

  private List<AnnisNode> token;

  public DetectHoles(List<AnnisNode> token)
  {
    this.token = token;
  }

  public AnnisNode getLeftBorder(AnnisNode n)
  {
    AnnisNode tmp = null;

    for (AnnisNode tok : token)
    {
      if (n.getLeftToken() == tok.getTokenIndex())
      {
        return tok;
      }

      if (n.getLeftToken() <= tok.getTokenIndex() && tmp == null)
      {
        tmp = tok;
      }
    }
    return tmp;
  }

  public AnnisNode getRightBorder(AnnisNode n)
  {
    AnnisNode tmp = null;
    for (AnnisNode tok : token)
    {
      if (n.getRightToken() == tok.getTokenIndex())
      {
        return tok;
      }

      if (tok.getTokenIndex() <= n.getRightToken())
      {
        tmp = tok;
      }
    }

    return tmp;
  }
}
