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
package annis.libgui;

import annis.libgui.visualizers.VisualizerInput;
import annis.model.AnnisConstants;
import static annis.model.AnnisConstants.ANNIS_NS;
import static annis.model.AnnisConstants.FEAT_RELANNIS_NODE;
import annis.model.RelannisNodeFeature;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SLayer;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.util.SaltUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helps to extract page number annotations from {@link SSpan} of a salt
 * document.
 *
 * <p>It uses the following algorithm:</p>
 * <ul>
 * <li>Get all spans which are annoteted with a page number.</li>
 * <li>Create intervalls left and right token index of the annis model with the
 * help of SFeatures and {@link AnnisConstants} and build a mapping from these
 * intervalls to the sspan.</li>
 * <li>Get the best fitting intervall for a specific span.</li>
 * <ul>
 *
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class PDFPageHelper {

  private static final Logger log = LoggerFactory.getLogger(PDFPageHelper.class);

  public static final String MAPPING_PAGE_KEY = "pdf_page_key";
  public static final String DEFAULT_PAGE_NUMBER_ANNOTATION_NAME = "page";

  public static final String PAGE_NUMBER_SEPERATOR = "-";

  public static final String PAGE_NO_VALID_NUMBER = "-1";

  private SortedMap<Integer, TreeMap<Integer, SSpan>> sspans = new TreeMap<Integer, TreeMap<Integer, SSpan>>();

  private VisualizerInput input;

  public PDFPageHelper(VisualizerInput visInput) {
    this.input = visInput;
    getAllSSpanWithPageNumber(visInput.getDocument().getDocumentGraph());
  }

  /**
   * Returns a page annotation for a span, if the span is overlapped by a page
   * annotation.
   */
  public String getPageAnnoForGridEvent(SSpan span) {
    int left = getLeftIndexFromSNode(span);
    int right = getRightIndexFromSNode(span);

    if (sspans == null) {
      log.warn("no page annos found");
      return null;
    }

    // lookup left index
    int leftIdx = -1;
    for (Integer i : sspans.keySet()) {
      if (i <= left) {
        leftIdx = i;
      }
    }

    if (leftIdx == -1) {
      log.debug("no left index found");
      return null;
    }

    // lookup right key
    int rightIdx = -1;
    for (Integer i : sspans.get(leftIdx).keySet()) {
      if (i >= right) {
        rightIdx = i;
      }
    }

    if (rightIdx == -1) {
      log.debug("no right index found");
      return null;
    }

    return getPageFromAnnotation(span);
  }

  /**
   * Returns the value of page annotiation for a node. It takes the visualizer
   * mappings into account. If no mapping is used, this definition is used: {@link
   * #PAGE_NUMBER_ANNOATATION_NAME}
   *
   */
  public String getPageFromAnnotation(SNode node) {
    if (node != null && node.getAnnotations() != null) {

      Set<SLayer> layers = node.getLayers();
      String nodeNamespace = null;

      if(layers != null)
      {
        for (SLayer l : layers) {
          nodeNamespace = l.getName();
        }

        for (SAnnotation anno : node.getAnnotations()) {

          if ((nodeNamespace == null || input.getNamespace() == null)
                  && getPDFPageAnnotationName().equals(anno.getName())) {
            return anno.getValue_STEXT();
          } else if (nodeNamespace.equals(input.getNamespace())
                  && getPDFPageAnnotationName().equals(anno.getName())) {
            return anno.getValue_STEXT();
          }
        }
      }
    }

    return null;
  }

  private void getAllSSpanWithPageNumber(
          SDocumentGraph graph) {
    if (graph == null) {
      log.error("could not get page annos from empty graph");
      return;
    }

    List<SSpan> sSpans = graph.getSpans();

    if (sSpans != null) {
      for (SSpan s : sSpans) {
        Set<SAnnotation> sAnnotations = s.getAnnotations();
        if (sAnnotations != null) {
          for (SAnnotation anno : sAnnotations) {
            // TODO support mappings of resolver vis map
            if (getPDFPageAnnotationName().equals(anno.getName())) {
              int leftIdx = getLeftIndexFromSNode(s);
              int rightIdx = getRightIndexFromSNode(s);

              if (sspans.containsKey(leftIdx)) {
                if (sspans.get(leftIdx).containsKey(rightIdx)) {
                  log.warn("an intervall {}-{} is overrided by: {}", s);
                }
                sspans.get(leftIdx).put(rightIdx, s);
              } else {
                sspans.put(leftIdx, new TreeMap<Integer, SSpan>());
                sspans.get(leftIdx).put(rightIdx, s);
              }
            }
          }
        }
      }
    }
  }

  /**
   * Get the most left token index of a SSpan.
   *
   */
  public int getLeftIndexFromSNode(SSpan s) 
  {
    
    RelannisNodeFeature feat = 
      (RelannisNodeFeature) s.getFeature(SaltUtil.createQName(ANNIS_NS, FEAT_RELANNIS_NODE)).getValue();
    return (int) feat.getLeftToken();
  }

  /**
   * Get the most right token index of a SSpan.
   *
   */
  public int getRightIndexFromSNode(SSpan s) 
  {
    
    RelannisNodeFeature feat =
      (RelannisNodeFeature) s.getFeature(SaltUtil.createQName(ANNIS_NS,
          FEAT_RELANNIS_NODE)).getValue_SOBJECT();
    return (int) feat.getRightToken();
  }

  /**
   * Gets the pdf page annotation name. It takes into acount the mappings
   * defined in {@link VisualizerInput#mappings}.
   *
   */
  public String getPDFPageAnnotationName() {

    Properties mappings = input.getMappings();

    if (mappings != null) {
      return mappings.getProperty(MAPPING_PAGE_KEY,
              DEFAULT_PAGE_NUMBER_ANNOTATION_NAME);
    }

    return DEFAULT_PAGE_NUMBER_ANNOTATION_NAME;
  }

  /**
   * Creates a String (eg. <b>3-9</b> or <b>3</b>), based on the most left and
   * most right page annotation.
   *
   * <p>The page annotation is detected with
   * {@link #getPageFromAnnotation(de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan)}</p>
   *
   * @return A String which represents the start and the end page of a pdf,
   * seperated by {@link #PAGE_NUMBER_SEPERATOR}. If there is no end page, or
   * exactly one page annotation, only a String with one number is returned.
   */
  public String getMostLeftAndMostRightPageAnno() {

    if (sspans == null || sspans.isEmpty()) {
      return null;
    }
    TreeMap<Integer, SSpan> rightTokIdxToSSpan = sspans.get(sspans.firstKey());
    SSpan leftSpan = rightTokIdxToSSpan.get(rightTokIdxToSSpan.firstKey());
    SSpan rightSpan = null;
    Integer rightIdx = null;

    for (Integer leftIdxKey : sspans.keySet()) {
      for (Integer rightIdxKey : sspans.get(leftIdxKey).keySet()) {
        if (rightIdx == null || rightIdx <= rightIdxKey) {
          rightIdx = rightIdxKey;
          rightSpan = sspans.get(leftIdxKey).get(rightIdx);
        }
      }
    }

    if (rightIdx != null) {
      return getPageFromAnnotation(leftSpan)
              + PAGE_NUMBER_SEPERATOR
              + getPageFromAnnotation(rightSpan);
    }

    return getPageFromAnnotation(leftSpan);
  }
}
