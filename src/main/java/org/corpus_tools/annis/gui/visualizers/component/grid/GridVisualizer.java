/*
 * Copyright 2012 Corpuslinguistic working group Humboldt University Berlin.
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
package org.corpus_tools.annis.gui.visualizers.component.grid;

import java.util.List;
import org.corpus_tools.annis.gui.media.MediaController;
import org.corpus_tools.annis.gui.media.PDFController;
import org.corpus_tools.annis.gui.resultview.VisualizerPanel;
import org.corpus_tools.annis.gui.visualizers.AbstractVisualizer;
import org.corpus_tools.annis.gui.visualizers.VisualizerInput;
import org.corpus_tools.salt.common.STextualDS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Visualizes annotations of a spans.
 *
 *
 * Mappings: <br/>
 * <ul>
 * <li>It is possible to specify the order of annotation layers in each grid. Use <b>annos:
 * anno_name1, anno_name2, anno_name3</b> to specify the order or annotation layers. If <b>anno:</b>
 * is used, additional annotation layers not present in the list will not be visualized. If mappings
 * is left empty, layers will be ordered alphabetically. You can also use regular expressions in the
 * <b>annos</b> mapping by enclosing it in slashes like in
 * <code>annos:/.*::v.+/,/(ns1::a|ns1::b|ns2::c)/</code>. Please note that the regular expressions
 * have to match the whole qualified name including the namespace.</li>
 * <li><b>tok_anno:true</b> can be used to also display the annotations of the token.</li>
 * <li><b>hide_tok:true</b> switches the line with the token value off.</li>
 * <li><b>show_ns</b> If set to "true" always show the namespace of the annotations. Alternativly a
 * comma-separated list of fully qualified annotation names (e.g. {@code myns::lemma}) or regular
 * expressions enclosed in slashes (e.g. {@code /my.*::lem.+/} for which the namespace should be
 * shown can be given.</li>
 * <li><b>pdf_page_key</b>: name of the annotation that contains the pdf page number</li>
 * </ul>
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
@Component
public class GridVisualizer extends AbstractVisualizer { // NO_UCD (unused code)

  /**
   * 
   */
  private static final long serialVersionUID = -3086993988435369808L;
  private static final Logger log = LoggerFactory.getLogger(GridVisualizer.class);

  @Override
  public GridComponent createComponent(VisualizerInput visInput, VisualizerPanel visPanel) {
    MediaController mediaController =
        visInput.getUI().getSession().getAttribute(MediaController.class);
    PDFController pdfController = visInput.getUI().getSession().getAttribute(PDFController.class);
    GridComponent component = null;
    try {

      List<STextualDS> texts = visInput.getDocument().getDocumentGraph().getTextualDSs();
      if (texts.size() == 1) {
        component = new SingleGridComponent(visInput, mediaController, pdfController, true, null);
      } else {
        component = new MultipleGridComponent(visInput, mediaController, pdfController, true);
      }
    } catch (Exception ex) {
      log.error("create {} failed", GridVisualizer.class.getName(), ex);
    }
    return component;
  }

  @Override
  public String getShortName() {
    return "grid";
  }
}
