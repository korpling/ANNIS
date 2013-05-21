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

  public static final String FEAT_INTERNALID = "internalid";

  public static final String FEAT_CORPUSREF = "corpusref";

  public static final String FEAT_TEXTREF = "textref";

  public static final String FEAT_COMPONENTID = "componentid";

  public static final String FEAT_LEFT = "left";

  public static final String FEAT_LEFTTOKEN = "lefttoken";

  public static final String FEAT_RIGHT = "right";

  public static final String FEAT_RIGHTTOKEN = "righttoken";

  public static final String FEAT_TOKENINDEX = "tokenindex";

  public static final String FEAT_SEGINDEX = "segindex";

  public static final String FEAT_SEGNAME = "segname";

  public static final String FEAT_ARTIFICIAL_DOMINANCE_COMPONENT = "artificialdominancecomponent";

  public static final String FEAT_ARTIFICIAL_DOMINANCE_PRE = "artificialdominancepre";

}
