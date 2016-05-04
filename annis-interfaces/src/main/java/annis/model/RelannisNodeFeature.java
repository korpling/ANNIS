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
package annis.model;

import static annis.model.AnnisConstants.ANNIS_NS;
import static annis.model.AnnisConstants.FEAT_RELANNIS_NODE;
import java.io.Serializable;
import org.corpus_tools.salt.core.SFeature;
import org.corpus_tools.salt.core.SNode;

/**
 * Common Features included in the Salt graph that are available
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class RelannisNodeFeature implements Serializable
{
  static final long serialVersionUID = 0L;
  
  private long internalID;
  
  private long corpusRef;

  private long textRef;

  private long left;

  private long leftToken;

  private long right;

  private long rightToken;

  private long tokenIndex;

  private long segIndex;

  private String segName;
  
  private Long matchedNode;
  
  private boolean continuous;

  public long getInternalID()
  {
    return internalID;
  }

  public void setInternalID(long internalID)
  {
    this.internalID = internalID;
  }

  
  
  public long getCorpusRef()
  {
    return corpusRef;
  }

  public void setCorpusRef(long corpusRef)
  {
    this.corpusRef = corpusRef;
  }

  public long getTextRef()
  {
    return textRef;
  }

  public void setTextRef(long textRef)
  {
    this.textRef = textRef;
  }

  public long getLeft()
  {
    return left;
  }

  public void setLeft(long left)
  {
    this.left = left;
  }

  public long getLeftToken()
  {
    return leftToken;
  }

  public void setLeftToken(long leftToken)
  {
    this.leftToken = leftToken;
  }

  public long getRight()
  {
    return right;
  }

  public void setRight(long right)
  {
    this.right = right;
  }

  public long getRightToken()
  {
    return rightToken;
  }

  public void setRightToken(long rightToken)
  {
    this.rightToken = rightToken;
  }

  public long getTokenIndex()
  {
    return tokenIndex;
  }

  public void setTokenIndex(long tokenIndex)
  {
    this.tokenIndex = tokenIndex;
  }

  public long getSegIndex()
  {
    return segIndex;
  }

  public void setSegIndex(long seg_index)
  {
    this.segIndex = seg_index;
  }

  public String getSegName()
  {
    return segName;
  }

  public void setSegName(String segName)
  {
    this.segName = segName;
  }

  /**
   * Returns the number of the query node if matched or null if otherwise.
   * @return 
   */
  public Long getMatchedNode()
  {
    return matchedNode;
  }

  public void setMatchedNode(Long matchedNode)
  {
    this.matchedNode = matchedNode;
  }
  
  

  @Override
  public String toString()
  {
    return "[" +
      "internalID=" + internalID + "," +
      "corpusRef=" + corpusRef + "," +
      "textRef=" + textRef + "," +
      "left=" + left +  "," +
      "leftToken=" + leftToken + "," +
      "right=" + right + "," +
      "rightToken=" + rightToken + "," +
      "tokenIndex=" + tokenIndex + "," +
      "segIndex=" + segIndex + "," +
      "segName=" + segName + ", " +
      "matchedNode=" + (matchedNode == null ? "[none]" : matchedNode) +
      "continuous=" + continuous +
      "]"
      ;
    
  }
  
  public static RelannisNodeFeature extract(SNode node)
  {
    RelannisNodeFeature featNode = null;
    SFeature sfeatNode = node.getFeature(ANNIS_NS +"::"+ FEAT_RELANNIS_NODE);
    if(sfeatNode != null)
    {
      featNode = (RelannisNodeFeature) sfeatNode.getValue();
    }
    return featNode;
  }
  
  
  
}
