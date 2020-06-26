/*
 * Copyright 2013 SFB 632.
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
package annis.libgui;

import annis.libgui.visualizers.VisualizerInput;
import annis.model.AnnisConstants;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SLayer;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.util.DataSourceSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helps to extract page number annotations from {@link SSpan} of a salt document.
 *
 * <p>
 * It uses the following algorithm:
 * </p>
 * <ul>
 * <li>Get all spans which are annoteted with a page number.</li>
 * <li>Create intervalls left and right token index of the annis model with the help of SFeatures
 * and {@link AnnisConstants} and build a mapping from these intervalls to the sspan.</li>
 * <li>Get the best fitting intervall for a specific span.</li>
 * </ul>
 *
 *
 * @author Benjamin Weißenfels {@literal <b.pixeldrama@gmail.com>}
 */
public class PDFPageHelper {

  private static final Logger log = LoggerFactory.getLogger(PDFPageHelper.class);

  public static final String MAPPING_PAGE_KEY = "pdf_page_key";
  public static final String DEFAULT_PAGE_NUMBER_ANNOTATION_NAME = "page";

  public static final String PAGE_NUMBER_SEPERATOR = "-";

  public static final String PAGE_NO_VALID_NUMBER = "-1";

  private static Number getCharacterIndex(SSpan span) {

    if (span != null && span.getGraph() != null) {
      List<DataSourceSequence> allDS = span.getGraph().getOverlappedDataSourceSequence(span,
          SALT_TYPE.STEXT_OVERLAPPING_RELATION);
      if (allDS != null) {
        for (DataSourceSequence<?> seq : allDS) {
          if (seq.getDataSource() instanceof STextualDS) {
            return seq.getStart();

          }
        }
      }
    }

    return null;
  }

  private VisualizerInput input;


  public PDFPageHelper(VisualizerInput visInput) {
    this.input = visInput;
  }


  /**
   * Creates a String (eg. <b>3-9</b> or <b>3</b>), based on the most left and most right page
   * annotation.
   *
   * <p>
   * The page annotation is detected with {@link #getPageFromAnnotation(SNode)}
   * </p>
   *
   * @return A String which represents the start and the end page of a pdf, Separated by
   *         {@link #PAGE_NUMBER_SEPERATOR}. If there is no end page, or exactly one page
   *         annotation, only a String with one number is returned.
   */
  public String getMostLeftAndMostRightPageAnno() {

    if (this.input == null || this.input.getDocument() == null
        || this.input.getDocument().getDocumentGraph() == null) {
      return null;
    }

    List<SSpan> sspans = this.input.getDocument().getDocumentGraph().getSpans();
    if (sspans == null || sspans.isEmpty()) {
      return null;
    }

    // iterate over all spans at get their page numbers, store them in ordered map
    // with their character position as key
    TreeMap<Integer, String> pageNumbers = new TreeMap<>();
    for (SSpan span : sspans) {
      String page = getPageFromAnnotation(span);
      if (page != null) {
        Number charIdx = getCharacterIndex(span);
        if (charIdx != null) {
          pageNumbers.put(charIdx.intValue(), page);
        }
      }
    }

    if (pageNumbers.size() == 1) {
      return pageNumbers.firstEntry().getValue();
    } else if (pageNumbers.size() > 1) {
      return pageNumbers.firstEntry().getValue() + PAGE_NUMBER_SEPERATOR
          + pageNumbers.lastEntry().getValue();
    } else {
      return null;
    }
  }

  /**
   * Returns the value of page annotation for a node. It takes the visualizer mappings into account.
   * If no mapping is defined, this default definition is used:
   * {@link #DEFAULT_PAGE_NUMBER_ANNOTATION_NAME}
   *
   */
  public String getPageFromAnnotation(SNode node) {
    if (node != null && node.getAnnotations() != null) {

      Set<SLayer> layers = node.getLayers();
      String nodeNamespace = null;

      if (layers != null) {
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

  /**
   * Gets the pdf page annotation name. It takes into account the mappings defined in
   * {@link VisualizerInput#mappings}.
   *
   */
  public String getPDFPageAnnotationName() {

    Properties mappings = input.getMappings();

    if (mappings != null) {
      return mappings.getProperty(MAPPING_PAGE_KEY, DEFAULT_PAGE_NUMBER_ANNOTATION_NAME);
    }

    return DEFAULT_PAGE_NUMBER_ANNOTATION_NAME;
  }
}
