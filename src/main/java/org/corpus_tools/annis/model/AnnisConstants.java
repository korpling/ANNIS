/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.corpus_tools.annis.model;

/**
 *
 * @author thomas
 */
public class AnnisConstants {

  public static final String ANNIS_NS = "annis";

  /**
   * Comma-separated list of matched node Salt-IDs. Feature is applied to instances of the SDocument
   * class.
   */
  public static final String FEAT_MATCHEDIDS = "matchedids";

  /**
   * Comma-separated list of matched annotations. If the node itself and not an annotation was
   * matched the string is empty.
   * 
   * Feature is applied to instances of the SDocument class
   */
  public static final String FEAT_MATCHEDANNOS = "matchedannos";

  /**
   * The number of the query node if matched. Feature is applied to {instances of the SNode and
   * SAnnotation classes.
   */
  public static final String FEAT_MATCHEDNODE = "matchednode";

}
