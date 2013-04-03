/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
   * Comma-sperated list of matched node Salt-IDs. 
   * Feature is applied to {@link SDocument}.
   */
  public static final String FEAT_MATCHEDIDS = "matchedids";
  /** 
   * The number of the query node if matched. 
   * Feature is applied to {@link SNode}. 
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
