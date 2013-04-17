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
package annis.visualizers.htmlvis;

import annis.libgui.VisualizationToggle;
import annis.libgui.visualizers.AbstractVisualizer;
import annis.libgui.visualizers.VisualizerInput;
import static annis.model.AnnisConstants.ANNIS_NS;
import static annis.model.AnnisConstants.FEAT_LEFTTOKEN;
import static annis.model.AnnisConstants.FEAT_RIGHTTOKEN;
import static annis.model.AnnisConstants.FEAT_TOKENINDEX;
import annis.visualizers.component.grid.EventExtractor;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.eclipse.emf.common.util.EList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
@PluginImplementation
public class HTMLVis extends AbstractVisualizer<Label>
{

  private static final Logger log = LoggerFactory.getLogger(HTMLVis.class);

  @Override
  public String getShortName()
  {
    return "html";
  }

  @Override
  public Label createComponent(VisualizerInput vi, VisualizationToggle vt)
  {
    Label lblResult = new Label("NOT IMPLEMENTED YET", ContentMode.HTML);
    try
    {
      // TODO: use mapping to get the right file
      VisParser p = new VisParser(HTMLVis.class.getResourceAsStream(
        "defaultvis.config"));
      VisualizationDefinition[] definitions = p.getDefinitions();

      List<String> annos = EventExtractor.computeDisplayAnnotations(vi);

      lblResult.setValue(createHTML(vi.getSResult().getSDocumentGraph(), annos,
        definitions));

    }
    catch (IOException ex)
    {
      log.error("Could not parse the HTML visualization configuration file", ex);
    }

    return lblResult;
  }

  private String createHTML(SDocumentGraph graph, List<String> annos,
    VisualizationDefinition[] definitions)
  {

    StringBuilder sb = new StringBuilder();

    EList<SToken> token = graph.getSortedSTokenByText();
    Map<Long, List<SSpan>> eventsByLeft = groupSpansByLeftToken(graph);
    Map<Long, List<SSpan>> eventsByRight = groupSpansByRightToken(graph);

    for (SToken t : token)
    {
      // get token index
      long currentIndex = t.getSFeature(ANNIS_NS, FEAT_TOKENINDEX).
        getSValueSNUMERIC();

      List<SSpan> startingEvents = eventsByLeft.get(currentIndex);
      List<SSpan> endEvents = eventsByRight.get(currentIndex);

      if (endEvents != null)
      {
        for (SSpan span : endEvents)
        {
          for (VisualizationDefinition vis : definitions)
          {
            if(vis.getMatcher().matches(span))
            {
              sb.append(outputForEvent(span, vis));
            }
          }
        }
      }
      if (startingEvents != null)
      {
      }
    }

    return sb.toString();
  }

  private Map<Long, List<SSpan>> groupSpansByLeftToken(SDocumentGraph graph)
  {
    Map<Long, List<SSpan>> result = new TreeMap<Long, List<SSpan>>();

    EList<SSpan> allSpans = graph.getSSpans();
    if (allSpans != null)
    {
      for (SSpan span : allSpans)
      {
        long startIndex = span.getSFeature(ANNIS_NS, FEAT_LEFTTOKEN).
          getSValueSNUMERIC();
        if(result.get(startIndex) == null)
        {
          result.put(startIndex, new ArrayList<SSpan>());
        }
        result.get(startIndex).add(span);
      }
    }
    return result;
  }
  
  private Map<Long, List<SSpan>> groupSpansByRightToken(SDocumentGraph graph)
  {
    Map<Long, List<SSpan>> result = new TreeMap<Long, List<SSpan>>();

    EList<SSpan> allSpans = graph.getSSpans();
    if (allSpans != null)
    {
      for (SSpan span : allSpans)
      {
        long endIndex = span.getSFeature(ANNIS_NS, FEAT_RIGHTTOKEN).
          getSValueSNUMERIC();
        if(result.get(endIndex) == null)
        {
          result.put(endIndex, new ArrayList<SSpan>());
        }
        result.get(endIndex).add(span);
      }
    }
    return result;
  }

  private String outputForEvent(SSpan span,
    VisualizationDefinition definition)
  {
    StringBuilder sb = new StringBuilder();
    
    
    // TODO: output the matched definition
    
    return sb.toString();
  }
}
