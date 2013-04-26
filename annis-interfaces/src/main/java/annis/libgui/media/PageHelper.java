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
package annis.libgui.media;

import annis.model.AnnisConstants;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import static annis.model.AnnisConstants.*;
import java.util.SortedMap;
import java.util.TreeMap;
import org.eclipse.emf.common.util.EList;
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
 * TODO mappings
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class PageHelper
{

  private static final Logger log = LoggerFactory.getLogger(PageHelper.class);

  public static final String PAGE_NUMBER_ANNOATION_NAMESPACE = "annis";

  public static final String PAGE_NUMBER_ANNOATATION_NAME = "page";

  private SortedMap<Integer, TreeMap<Integer, SSpan>> sspans = new TreeMap<Integer, TreeMap<Integer, SSpan>>();

  public PageHelper(SDocumentGraph graph)
  {
    getAllSSpanWithPageNumber(graph);
  }

  /**
   * Returns a page annotation for a span, if the span is overlapped by a page
   * annoation.
   */
  public String getPageAnnoForGridEvent(SSpan span)
  {
    int left = getLeftIndexFromSNode(span);
    int right = getRightIndexFromSNode(span);

    if (sspans == null)
    {
      log.warn("no page annos found");
      return null;
    }

    // lookup left index
    int leftIdx = -1;
    for (Integer i : sspans.keySet())
    {
      if (i <= left)
      {
        leftIdx = i;
      }
    }

    if (leftIdx == -1)
    {
      log.debug("no left index found");
      return null;
    }

    // lookup right key
    int rightIdx = -1;
    for (Integer i : sspans.get(leftIdx).keySet())
    {
      if (i >= right)
      {
        rightIdx = i;
      }
    }

    if (rightIdx == -1)
    {
      log.debug("no right index found");
      return null;
    }

    return getPageFromAnnotation(span);
  }

  public String getPageFromAnnotation(SSpan node)
  {
    if (node != null && node.getSAnnotations() != null)
    {
      for (SAnnotation anno : node.getSAnnotations())
      {
        if (getQualifiedPageNumberAnnotationName().equals(anno.getQName()))
        {
          return anno.getSValueSTEXT();
        }
      }
    }

    return null;
  }

  /**
   * Gets the complete name of the page annotation, including the seperator.
   *
   * @return
   */
  public String getQualifiedPageNumberAnnotationName()
  {
    return PAGE_NUMBER_ANNOATION_NAMESPACE + "::" + PAGE_NUMBER_ANNOATATION_NAME;
  }

  private void getAllSSpanWithPageNumber(
    SDocumentGraph graph)
  {
    if (graph == null)
    {
      log.error("could not get page annos from empty graph");
      return;
    }

    EList<SSpan> sSpans = graph.getSSpans();

    if (sSpans != null)
    {
      for (SSpan s : sSpans)
      {
        EList<SAnnotation> sAnnotations = s.getSAnnotations();
        if (sAnnotations != null)
        {
          for (SAnnotation anno : sAnnotations)
          {
            // TODO support mappings of resolver vis map
            if (PAGE_NUMBER_ANNOATATION_NAME.equals(anno.getName()))
            {
              int leftIdx = getLeftIndexFromSNode(s);
              int rightIdx = getRightIndexFromSNode(s);

              if (sspans.containsKey(leftIdx))
              {
                if (sspans.get(leftIdx).containsKey(rightIdx))
                {
                  log.warn("an intervall {}-{} is overrided by: {}", s);
                }
                sspans.get(leftIdx).put(rightIdx, s);
              }
              else
              {
                sspans.put(leftIdx, new TreeMap<Integer, SSpan>());
                sspans.get(leftIdx).put(rightIdx, s);
              }
            }
          }
        }
      }
    }

  }

  public int getLeftIndexFromSNode(SSpan s)
  {
    return (int) (long) s.getSFeature(ANNIS_NS, FEAT_LEFTTOKEN).
      getSValueSNUMERIC();
  }

  public int getRightIndexFromSNode(SSpan s)
  {
    return (int) (long) s.getSFeature(ANNIS_NS, FEAT_RIGHTTOKEN).
      getSValueSNUMERIC();
  }
}
