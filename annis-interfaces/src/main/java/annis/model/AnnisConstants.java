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
package annis.model;

import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;

/**
 *
 * @author thomas
 */
public class AnnisConstants
{

  public static final String ANNIS_NS = "annis";

  /**
   * Comma-sperated list of matched node Salt-IDs. Feature is applied to
   * {@link SDocument}.
   */
  public static final String FEAT_MATCHEDIDS = "matchedids";

  /**
   * The number of the query node if matched. Feature is applied to
   * {@link SNode}.
   */
  public static final String FEAT_MATCHEDNODE = "matchednode";

  /**
   * A number of relANNIS specific node values encoded as {@link RelannisNodeFeature}.   * 
   */
  public static final String FEAT_RELANNIS_NODE = "relannis_node";
  
  /**
   * A number of relANNIS specific edge values encoded as {@link RelannisEdgeFeature}.   * 
   */
  public static final String FEAT_RELANNIS_EDGE = "relannis_edge";

}
