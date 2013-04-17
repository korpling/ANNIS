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
import static annis.model.AnnisConstants.FEAT_TOKENINDEX;
import annis.visualizers.component.grid.EventExtractor;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.TreeMap;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.io.IOUtils;
import org.eclipse.emf.common.util.EList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.cssinject.CSSInject;

/**
 *
 * <p>
 * <strong>Mappings:</strong><br />
 * <ul>
 * <li>visconfigpath - path of the visualization configuration file</li>
 * </ul>
 * </p>
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
@PluginImplementation
public class HTMLVis extends AbstractVisualizer<Panel>
{

  private static final Logger log = LoggerFactory.getLogger(HTMLVis.class);
  

  @Override
  public String getShortName()
  {
    return "html";
  }

  @Override
  public boolean isUsingText()
  {
    return true;
  }
  
  

  @Override
  public Panel createComponent(VisualizerInput vi, VisualizationToggle vt)
  {
    Panel scrollPanel = new Panel();
    scrollPanel.setSizeFull();
    Label lblResult = new Label("ERROR", ContentMode.HTML);
    lblResult.setSizeUndefined();
    
    try
    {
      // TODO: can we load the file from the corpus media files? Or how do we bundle these kind of files with a corpus?
      String visConfigPath = vi.getMappings().getProperty("visconfigpath");
      InputStream inStream;
      if(visConfigPath == null)
      {
        inStream = HTMLVis.class.getResourceAsStream("defaultvis.config");
      }
      else
      {
        inStream = new FileInputStream(visConfigPath);
      }
      VisParser p = new VisParser(inStream);
      VisualizationDefinition[] definitions = p.getDefinitions();

      List<String> annos = EventExtractor.computeDisplayAnnotations(vi);

      lblResult.setValue(createHTML(vi.getSResult().getSDocumentGraph(), annos,
        definitions));
      
      // TODO: do not add CSSInject multiple times
      String cssContent = IOUtils.toString(HTMLVis.class.getResourceAsStream("htmlvis.css"));
      CSSInject cssInject = new CSSInject(UI.getCurrent());
      cssInject.setStyles(cssContent);

    }
    catch (IOException ex)
    {
      log.error("Could not parse the HTML visualization configuration file", ex);
    }

    scrollPanel.setContent(lblResult);
    
    return scrollPanel;
  }

  private String createHTML(SDocumentGraph graph, List<String> annos,
    VisualizationDefinition[] definitions)
  {
    TreeMap<Long, List<String>> output = new TreeMap<Long, List<String>>();
    StringBuilder sb = new StringBuilder();

    EList<SToken> token = graph.getSortedSTokenByText();

    for (SSpan span : graph.getSSpans())
    {
      for (VisualizationDefinition vis : definitions)
      {
        String matched = vis.getMatcher().matchedAnnotation(span);
        if (matched != null)
        {
          vis.getOutputter().outputHTML(span, matched, output);
        }
      }
    }
    for (SToken t : token)
    {
      // get token index
      long currentIndex = t.getSFeature(ANNIS_NS, FEAT_TOKENINDEX).
        getSValueSNUMERIC();
      
      for (VisualizationDefinition vis : definitions)
      {
        String matched = vis.getMatcher().matchedAnnotation(t);
        if (matched != null)
        {
          vis.getOutputter().outputHTML(t, matched, output);
        }
      }
      
      // output all strings belonging to this token position
      List<String> values = output.get(currentIndex);
      if(values != null)
      {
        for(String s : values)
        {
          sb.append(s);
        }
      }
    }

    return sb.toString();
  }
  
}
